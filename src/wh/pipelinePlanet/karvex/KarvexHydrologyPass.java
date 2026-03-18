package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

public class KarvexHydrologyPass implements GenPass{
    @Override
    public String name(){
        return "KarvexHydrologyPass";
    }

    @Override
    public void apply(GenContext ctx){
        if(!ctx.cfg.enableLakes) return;

        seedPollutedPools(ctx);
        spreadPollutedPools(ctx, 2);
        deepenBasins(ctx);
        ensureMinimumCoverage(ctx);
        cleanupSingles(ctx);
        polishWaterShapes(ctx, 2);
        protectCriticalRooms(ctx);
    }

    private void seedPollutedPools(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 28f, 16f)) continue;

            float effA = noise(ctx, ctx.seed + 701, tile.x + 782f, tile.y, 5, 0.75f, 250f);
            float effB = noise(ctx, ctx.seed + 709, tile.x - 210f, tile.y + 380f, 2, 0.92f, 74f);
            float effShape = effA + effB * 0.11f;

            float radA = noise(ctx, ctx.seed + 721, tile.x + 165f, tile.y - 240f, 4, 0.76f, 138f);
            float radB = noise(ctx, ctx.seed + 727, tile.x - 95f, tile.y + 540f, 2, 0.9f, 44f);

            if(isRadiationCandidate(tile.floor()) && radA > 0.74f && radB > 0.52f){
                tile.setFloor((radA > 0.79f && !nearSolid(ctx, tile.x, tile.y, 1)
                ? WHBlocksEnvironment.radiationWaterDeep
                : WHBlocksEnvironment.mineralSandRadiationWater).asFloor());
                tile.setOverlay(Blocks.air);
                continue;
            }

            if(!canPaintEffluent(tile.floor())) continue;
            float threshold = isDarksandEffluentBase(tile.floor()) ? 0.72f : 0.67f;
            if(effShape > threshold){
                tile.setFloor(effluentForBase(tile.floor(), effShape > threshold + 0.09f && !nearSolid(ctx, tile.x, tile.y, 1)).asFloor());
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void spreadPollutedPools(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            byte[] marks = new byte[width * height];

            for(Tile tile : ctx.tiles){
                if(!isPollutedWater(tile.floor())) continue;

                for(Point2 p : Geometry.d8){
                    int nx = tile.x + p.x, ny = tile.y + p.y;
                    Tile near = ctx.tiles.get(nx, ny);
                    if(near == null || near.block() != Blocks.air) continue;
                    if(!near.floor().hasSurface() || near.floor().isLiquid) continue;
                    if(isNearCriticalRoom(ctx, nx, ny, 28f, 16f)) continue;

                    boolean cardinal = Math.abs(p.x) + Math.abs(p.y) == 1;
                    if(isRadiationWater(tile.floor())){
                        if(!isRadiationCandidate(near.floor())) continue;
                        float chance = cardinal ? 0.24f : 0.13f;
                        if(ctx.rand.chance(chance)){
                            marks[nx + ny * width] = (byte)(ctx.rand.chance(0.36f) ? 4 : 3);
                        }
                    }else{
                        if(!canPaintEffluent(near.floor())) continue;
                        float chance = cardinal ? 0.36f : 0.20f;
                        if(ctx.rand.chance(chance)){
                            boolean deep = ctx.rand.chance(cardinal ? 0.24f : 0.14f) && !nearSolid(ctx, nx, ny, 1);
                            marks[nx + ny * width] = (byte)(deep ? 2 : 1);
                        }
                    }
                }
            }

            for(Tile tile : ctx.tiles){
                int mark = marks[tile.x + tile.y * width];
                if(mark == 0) continue;

                if(mark == 1 || mark == 2){
                    tile.setFloor(effluentForBase(tile.floor(), mark == 2).asFloor());
                }else{
                    tile.setFloor((mark == 4 ? WHBlocksEnvironment.radiationWaterDeep : WHBlocksEnvironment.mineralSandRadiationWater).asFloor());
                }
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void deepenBasins(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.floor() == WHBlocksEnvironment.mineralSandEffluentWater || tile.floor() == Blocks.darksandTaintedWater){
                if(surroundedBySameLiquid(ctx, tile.x, tile.y) && ctx.rand.chance(tile.floor() == Blocks.darksandTaintedWater ? 0.36f : 0.46f)){
                    tile.setFloor((tile.floor() == Blocks.darksandTaintedWater ? Blocks.deepTaintedWater : WHBlocksEnvironment.effluentDeep).asFloor());
                }
            }else if(tile.floor() == WHBlocksEnvironment.mineralSandRadiationWater){
                if(surroundedBySameLiquid(ctx, tile.x, tile.y) && ctx.rand.chance(0.42f)){
                    tile.setFloor(WHBlocksEnvironment.radiationWaterDeep.asFloor());
                }
            }
        }
    }

    private void ensureMinimumCoverage(GenContext ctx){
        int area = ctx.width() * ctx.height();
        int minEffluent = Math.max(170, area / 1350);
        int minRadiation = Math.max(54, area / 3000);

        int tries = 0;
        while(countEffluent(ctx) < minEffluent && tries++ < 14){
            paintPatch(ctx, false);
        }

        tries = 0;
        while(countRadiationWater(ctx) < minRadiation && tries++ < 10){
            paintPatch(ctx, true);
        }
    }

    private void paintPatch(GenContext ctx, boolean radiation){
        int cx = ctx.rand.random(12, ctx.width() - 13);
        int cy = ctx.rand.random(12, ctx.height() - 13);
        if(isNearCriticalRoom(ctx, cx, cy, 28f, 16f)) return;

        int radius = ctx.rand.random(radiation ? 5 : 7, radiation ? 9 : 12);
        int r2 = radius * radius;

        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null || tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;

                if(radiation){
                    if(!isRadiationCandidate(tile.floor())) continue;
                }else{
                    if(!canPaintEffluent(tile.floor())) continue;
                }

                float edge = Mathf.dst(ox, oy) / Math.max(radius, 1f);
                if(!ctx.rand.chance(Mathf.lerp(0.9f, 0.36f, edge))) continue;

                if(radiation){
                    boolean deep = edge < 0.56f && ctx.rand.chance(0.42f);
                    tile.setFloor((deep ? WHBlocksEnvironment.radiationWaterDeep : WHBlocksEnvironment.mineralSandRadiationWater).asFloor());
                }else{
                    boolean deep = edge < 0.54f && ctx.rand.chance(0.45f);
                    tile.setFloor(effluentForBase(tile.floor(), deep).asFloor());
                }
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void cleanupSingles(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(!isPollutedWater(tile.floor())) continue;

            int pollutedNear = pollutedNeighborCount(ctx, tile.x, tile.y);
            if(pollutedNear <= 1){
                tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 6).asFloor());
                tile.setOverlay(Blocks.air);
            }else if(isDeepPolluted(tile.floor()) && pollutedNear <= 2){
                tile.setFloor(shallowVersion(tile.floor()).asFloor());
            }
        }
    }

    private void polishWaterShapes(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            byte[] marks = new byte[width * height];

            for(Tile tile : ctx.tiles){
                int idx = tile.x + tile.y * width;
                Block floor = tile.floor();

                if(isPollutedWater(floor)){
                    int pollutedNear = pollutedNeighborCount(ctx, tile.x, tile.y);
                    if(pollutedNear <= 2){
                        marks[idx] = 1;
                    }else if(isDeepPolluted(floor) && pollutedNear <= 3){
                        marks[idx] = 2;
                    }
                }else if(tile.block() == Blocks.air && floor.asFloor().hasSurface() && !floor.asFloor().isLiquid){
                    int pollutedNear = pollutedNeighborCount(ctx, tile.x, tile.y);
                    if(pollutedNear >= 6 && canPaintEffluent(floor)){
                        marks[idx] = 3;
                    }else if(pollutedNear >= 5 && isRadiationCandidate(floor) && nearRadiationWater(ctx, tile.x, tile.y)){
                        marks[idx] = 4;
                    }
                }
            }

            for(Tile tile : ctx.tiles){
                int mark = marks[tile.x + tile.y * width];
                if(mark == 0) continue;

                if(mark == 1){
                    tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 4).asFloor());
                }else if(mark == 2){
                    tile.setFloor(shallowVersion(tile.floor()).asFloor());
                }else if(mark == 3){
                    tile.setFloor(effluentForBase(tile.floor(), false).asFloor());
                }else if(mark == 4){
                    tile.setFloor(WHBlocksEnvironment.mineralSandRadiationWater.asFloor());
                }
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void protectCriticalRooms(GenContext ctx){
        if(ctx.spawnRoom != null){
            cleanRoomLiquid(ctx, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + 8);
        }

        for(RoomAnchor enemy : ctx.enemyRooms){
            cleanRoomLiquid(ctx, enemy.x, enemy.y, enemy.radius + 4);
        }
    }

    private void cleanRoomLiquid(GenContext ctx, int cx, int cy, int radius){
        int r2 = radius * radius;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null || !tile.floor().isLiquid) continue;
                tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 8).asFloor());
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private Block effluentForBase(Block base, boolean deep){
        if(isDarksandEffluentBase(base)){
            return deep ? Blocks.deepTaintedWater : Blocks.darksandTaintedWater;
        }
        return deep ? WHBlocksEnvironment.effluentDeep : WHBlocksEnvironment.mineralSandEffluentWater;
    }

    private boolean canPaintEffluent(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == Blocks.darksand
        || floor == Blocks.shale
        || floor == Blocks.dacite
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates;
    }

    private boolean isRadiationCandidate(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters
        || floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.darkRock
        || floor == Blocks.darksand;
    }

    private boolean isDarksandEffluentBase(Block floor){
        return floor == Blocks.darksand || floor == Blocks.shale;
    }

    private boolean isPollutedWater(Block floor){
        return floor == WHBlocksEnvironment.effluent
        || floor == WHBlocksEnvironment.effluentDeep
        || floor == WHBlocksEnvironment.mineralSandEffluentWater
        || floor == Blocks.darksandTaintedWater
        || floor == Blocks.deepTaintedWater
        || floor == WHBlocksEnvironment.radiationWater
        || floor == WHBlocksEnvironment.radiationWaterDeep
        || floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationSandWater;
    }

    private boolean isRadiationWater(Block floor){
        return floor == WHBlocksEnvironment.radiationWater
        || floor == WHBlocksEnvironment.radiationWaterDeep
        || floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationSandWater;
    }

    private boolean isDeepPolluted(Block floor){
        return floor == WHBlocksEnvironment.effluentDeep
        || floor == Blocks.deepTaintedWater
        || floor == WHBlocksEnvironment.radiationWaterDeep;
    }

    private Block shallowVersion(Block floor){
        if(floor == WHBlocksEnvironment.effluentDeep) return WHBlocksEnvironment.mineralSandEffluentWater;
        if(floor == Blocks.deepTaintedWater) return Blocks.darksandTaintedWater;
        if(floor == WHBlocksEnvironment.radiationWaterDeep) return WHBlocksEnvironment.mineralSandRadiationWater;
        return floor;
    }

    private int pollutedNeighborCount(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isPollutedWater(near.floor())){
                count++;
            }
        }
        return count;
    }

    private boolean nearRadiationWater(GenContext ctx, int x, int y){
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isRadiationWater(near.floor())){
                return true;
            }
        }
        return false;
    }

    private boolean surroundedBySameLiquid(GenContext ctx, int x, int y){
        Block floor = ctx.tiles.getn(x, y).floor();
        for(Point2 p : Geometry.d4){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near == null || near.floor() != floor){
                return false;
            }
        }
        return true;
    }

    private int countEffluent(GenContext ctx){
        int count = 0;
        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            if(floor == WHBlocksEnvironment.effluent
            || floor == WHBlocksEnvironment.effluentDeep
            || floor == WHBlocksEnvironment.mineralSandEffluentWater
            || floor == Blocks.darksandTaintedWater
            || floor == Blocks.deepTaintedWater){
                count++;
            }
        }
        return count;
    }

    private int countRadiationWater(GenContext ctx){
        int count = 0;
        for(Tile tile : ctx.tiles){
            if(isRadiationWater(tile.floor())) count++;
        }
        return count;
    }

    private Floor findNearbyLandFloor(GenContext ctx, int x, int y, int radius){
        for(int r = 1; r <= radius; r++){
            for(int ox = -r; ox <= r; ox++){
                for(int oy = -r; oy <= r; oy++){
                    Tile near = ctx.tiles.get(x + ox, y + oy);
                    if(near == null) continue;
                    if(near.floor().hasSurface() && !near.floor().isLiquid){
                        return near.floor();
                    }
                }
            }
        }
        return WHBlocksEnvironment.mineralSandstone.asFloor();
    }

    private boolean nearSolid(GenContext ctx, int x, int y, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.floor().hasSurface() && !near.floor().isLiquid){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNearCriticalRoom(GenContext ctx, int x, int y, float spawnDst, float enemyDst){
        if(ctx.spawnRoom != null && Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + spawnDst)){
            return true;
        }
        for(RoomAnchor enemy : ctx.enemyRooms){
            if(Mathf.within(x, y, enemy.x, enemy.y, enemy.radius + enemyDst)){
                return true;
            }
        }
        return false;
    }

    private float noise(GenContext ctx, int seed, float x, float y, double octaves, double falloff, double scl){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z);
    }
}
