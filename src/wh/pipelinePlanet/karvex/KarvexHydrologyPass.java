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

/**
 * 中文说明：Karvex 水文阶段：水体、污水与辐射水分布。
 */
public class KarvexHydrologyPass implements GenPass{
    @Override
    public String name(){
        return "KarvexHydrologyPass";
    }

    @Override
    public void apply(GenContext ctx){
        if(!ctx.cfg.enableLakes) return;

        seedPollutedPools(ctx);
        ensureMinimumCoverage(ctx);
        spreadPollutedPools(ctx, 3);
        deepenBasins(ctx);
        polishWaterShapes(ctx, 3);
        smoothLiquidBasins(ctx, 2);
        roughenShoreline(ctx, 2);
        cleanupSingles(ctx);
        enforceMaximumCoverage(ctx);
        protectCriticalRooms(ctx);
    }

    private void seedPollutedPools(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 28f, 16f)) continue;

            float effBasin = basinMask(ctx, tile.x, tile.y, 811);
            float radBasin = basinMask(ctx, tile.x, tile.y, 877);

            float effA = noise(ctx, ctx.seed + 701, tile.x + 782f, tile.y, 5, 0.75f, 250f);
            float effB = noise(ctx, ctx.seed + 709, tile.x - 210f, tile.y + 380f, 2, 0.92f, 74f);
            float effShape = effA + effB * 0.11f;

            float radA = noise(ctx, ctx.seed + 721, tile.x + 165f, tile.y - 240f, 4, 0.76f, 138f);
            float radB = noise(ctx, ctx.seed + 727, tile.x - 95f, tile.y + 540f, 2, 0.9f, 44f);

            if(isRadiationCandidate(tile.floor()) && radBasin > 0.50f && radA > 0.74f && radB > 0.50f){
                tile.setFloor(WHBlocksEnvironment.mineralSandRadiationWater.asFloor());
                tile.setOverlay(Blocks.air);
                continue;
            }

            if(!canPaintEffluent(tile.floor())) continue;
            float threshold = isDarksandEffluentBase(tile.floor()) ? 0.67f : 0.63f;
            if(effBasin > 0.42f && effShape > threshold){
                tile.setFloor(effluentForBase(tile.floor(), false).asFloor());
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
                        float basin = basinMask(ctx, nx, ny, 877);
                        if(basin < 0.37f) continue;
                        if(effluentNeighborCount(ctx, nx, ny) > 1) continue;
                        int around = radiationNeighborCount(ctx, nx, ny);
                        if(around < (cardinal ? 1 : 1)) continue;
                        float chance = cardinal ? 0.42f : 0.27f;
                        if(ctx.rand.chance(chance)){
                            marks[nx + ny * width] = 3;
                        }
                    }else{
                        if(!canPaintEffluent(near.floor())) continue;
                        float basin = basinMask(ctx, nx, ny, 811);
                        if(basin < 0.30f) continue;
                        if(radiationNeighborCount(ctx, nx, ny) > 0) continue;
                        int around = effluentNeighborCount(ctx, nx, ny);
                        if(around < (cardinal ? 1 : 2)) continue;
                        float chance = cardinal ? 0.62f : 0.41f;
                        if(ctx.rand.chance(chance)){
                            marks[nx + ny * width] = 1;
                        }
                    }
                }
            }

            for(Tile tile : ctx.tiles){
                int mark = marks[tile.x + tile.y * width];
                if(mark == 0) continue;

                if(mark == 1){
                    tile.setFloor(effluentForBase(tile.floor(), mark == 2).asFloor());
                }else{
                    tile.setFloor(WHBlocksEnvironment.mineralSandRadiationWater.asFloor());
                }
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void deepenBasins(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            if(floor == WHBlocksEnvironment.mineralSandEffluentWater || floor == Blocks.darksandTaintedWater){
                int near = effluentNeighborCount(ctx, tile.x, tile.y);
                float basin = basinMask(ctx, tile.x, tile.y, 811);
                if(near >= 7 && basin > 0.55f){
                    tile.setFloor((floor == Blocks.darksandTaintedWater ? Blocks.deepTaintedWater : WHBlocksEnvironment.effluentDeep).asFloor());
                }
            }else if(floor == WHBlocksEnvironment.effluentDeep || floor == Blocks.deepTaintedWater){
                int near = effluentNeighborCount(ctx, tile.x, tile.y);
                if(near <= 5){
                    tile.setFloor((floor == Blocks.deepTaintedWater ? Blocks.darksandTaintedWater : WHBlocksEnvironment.mineralSandEffluentWater).asFloor());
                }
            }else if(floor == WHBlocksEnvironment.mineralSandRadiationWater){
                int near = radiationNeighborCount(ctx, tile.x, tile.y);
                float basin = basinMask(ctx, tile.x, tile.y, 877);
                if(near >= 7 && basin > 0.60f){
                    tile.setFloor(WHBlocksEnvironment.radiationWaterDeep.asFloor());
                }
            }else if(floor == WHBlocksEnvironment.radiationWaterDeep){
                int near = radiationNeighborCount(ctx, tile.x, tile.y);
                if(near <= 5){
                    tile.setFloor(WHBlocksEnvironment.mineralSandRadiationWater.asFloor());
                }
            }
        }
    }

    private void ensureMinimumCoverage(GenContext ctx){
        int area = ctx.width() * ctx.height();
        int minEffluent = Math.max(860, area / 360);
        int minRadiation = Math.max(260, area / 900);

        int effluentCount = countEffluent(ctx);
        int radiationCount = countRadiationWater(ctx);

        int tries = 0;
        while(effluentCount < minEffluent && tries++ < 30){
            effluentCount += paintPatch(ctx, false);
        }

        tries = 0;
        while(radiationCount < minRadiation && tries++ < 24){
            radiationCount += paintPatch(ctx, true);
        }
    }

    private int paintPatch(GenContext ctx, boolean radiation){
        int cx = ctx.rand.random(12, ctx.width() - 13);
        int cy = ctx.rand.random(12, ctx.height() - 13);
        if(isNearCriticalRoom(ctx, cx, cy, 28f, 16f)) return 0;
        if(basinMask(ctx, cx, cy, radiation ? 877 : 811) < (radiation ? 0.36f : 0.32f)) return 0;

        int radius = ctx.rand.random(radiation ? 9 : 11, radiation ? 16 : 20);
        int r2 = radius * radius;
        int placed = 0;

        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null || tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;

                if(radiation){
                    if(!isRadiationCandidate(tile.floor())) continue;
                    if(basinMask(ctx, tile.x, tile.y, 877) < 0.34f) continue;
                }else{
                    if(!canPaintEffluent(tile.floor())) continue;
                    if(basinMask(ctx, tile.x, tile.y, 811) < 0.24f) continue;
                }

                float edge = Mathf.dst(ox, oy) / Math.max(radius, 1f);
                float coreChance = radiation ? 0.92f : 0.97f;
                float rimChance = radiation ? 0.66f : 0.79f;
                if(!ctx.rand.chance(Mathf.lerp(coreChance, rimChance, edge))) continue;

                if(radiation){
                    tile.setFloor(WHBlocksEnvironment.mineralSandRadiationWater.asFloor());
                }else{
                    tile.setFloor(effluentForBase(tile.floor(), false).asFloor());
                }
                tile.setOverlay(Blocks.air);
                placed++;
            }
        }

        return placed;
    }

    private void cleanupSingles(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(!isPollutedWater(tile.floor())) continue;

            int near = isRadiationWater(tile.floor()) ? radiationNeighborCount(ctx, tile.x, tile.y) : effluentNeighborCount(ctx, tile.x, tile.y);
            if(near <= 1){
                tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 6).asFloor());
                tile.setOverlay(Blocks.air);
            }else if(isDeepPolluted(tile.floor()) && near <= 2){
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
                    int near = isRadiationWater(floor) ? radiationNeighborCount(ctx, tile.x, tile.y) : effluentNeighborCount(ctx, tile.x, tile.y);
                    if(near <= 1){
                        marks[idx] = 1;
                    }else if(isDeepPolluted(floor) && near <= 2){
                        marks[idx] = 2;
                    }
                }else if(tile.block() == Blocks.air && floor.asFloor().hasSurface() && !floor.asFloor().isLiquid){
                    int effNear = effluentNeighborCount(ctx, tile.x, tile.y);
                    int radNear = radiationNeighborCount(ctx, tile.x, tile.y);
                    if(effNear >= 4 && canPaintEffluent(floor) && basinMask(ctx, tile.x, tile.y, 811) > 0.39f){
                        marks[idx] = 3;
                    }else if(radNear >= 4 && isRadiationCandidate(floor) && basinMask(ctx, tile.x, tile.y, 877) > 0.43f){
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

    private void smoothLiquidBasins(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            byte[] marks = new byte[width * height];

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;

                int idx = tile.x + tile.y * width;
                Block floor = tile.floor();
                int effNear = effluentNeighborCount(ctx, tile.x, tile.y);
                int radNear = radiationNeighborCount(ctx, tile.x, tile.y);

                if(isEffluentWater(floor)){
                    if(effNear <= 1){
                        marks[idx] = 1;
                    }
                }else if(isRadiationWater(floor)){
                    if(radNear <= 1){
                        marks[idx] = 2;
                    }
                }else if(floor.asFloor().hasSurface() && !floor.asFloor().isLiquid){
                    if(effNear >= 4 && effNear >= radNear + 1 && canPaintEffluent(floor) && basinMask(ctx, tile.x, tile.y, 811) > 0.40f){
                        marks[idx] = 3;
                    }else if(radNear >= 4 && radNear >= effNear + 1 && isRadiationCandidate(floor) && basinMask(ctx, tile.x, tile.y, 877) > 0.44f){
                        marks[idx] = 4;
                    }
                }
            }

            for(Tile tile : ctx.tiles){
                int mark = marks[tile.x + tile.y * width];
                if(mark == 0) continue;

                if(mark == 1 || mark == 2){
                    tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 6).asFloor());
                }else if(mark == 3){
                    tile.setFloor(effluentForBase(tile.floor(), false).asFloor());
                }else{
                    tile.setFloor(WHBlocksEnvironment.mineralSandRadiationWater.asFloor());
                }
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void roughenShoreline(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            byte[] marks = new byte[width * height];

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;
                int idx = tile.x + tile.y * width;
                Block floor = tile.floor();

                int effNear = effluentNeighborCount(ctx, tile.x, tile.y);
                int radNear = radiationNeighborCount(ctx, tile.x, tile.y);
                float shore = noise(ctx, ctx.seed + 933 + i * 11, tile.x + 53f, tile.y - 29f, 2, 0.64f, 18f);

                if(isEffluentWater(floor)){
                    if(effNear <= 2 && shore < -0.20f){
                        marks[idx] = 1;
                    }
                }else if(isRadiationWater(floor)){
                    if(radNear <= 2 && shore < -0.16f){
                        marks[idx] = 2;
                    }
                }else if(floor.asFloor().hasSurface() && !floor.asFloor().isLiquid){
                    if(effNear >= 2 && effNear >= radNear + 1 && canPaintEffluent(floor) && basinMask(ctx, tile.x, tile.y, 811) > 0.40f && shore > 0.12f){
                        marks[idx] = 3;
                    }else if(radNear >= 2 && radNear >= effNear && isRadiationCandidate(floor) && basinMask(ctx, tile.x, tile.y, 877) > 0.44f && shore > 0.12f){
                        marks[idx] = 4;
                    }
                }
            }

            for(Tile tile : ctx.tiles){
                int mark = marks[tile.x + tile.y * width];
                if(mark == 0) continue;

                if(mark == 1 || mark == 2){
                    tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 5).asFloor());
                }else if(mark == 3){
                    tile.setFloor(effluentForBase(tile.floor(), false).asFloor());
                }else{
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

    private void enforceMaximumCoverage(GenContext ctx){
        int area = ctx.width() * ctx.height();
        int maxEffluent = Math.max(1800, area / 70);
        int maxRadiation = Math.max(980, area / 125);

        int effluentCount = countEffluent(ctx);
        int radiationCount = countRadiationWater(ctx);

        int passes = 0;
        while(effluentCount > maxEffluent && passes++ < 6){
            effluentCount -= trimExcessLiquid(ctx, false, effluentCount - maxEffluent);
        }

        passes = 0;
        while(radiationCount > maxRadiation && passes++ < 6){
            radiationCount -= trimExcessLiquid(ctx, true, radiationCount - maxRadiation);
        }
    }

    private int trimExcessLiquid(GenContext ctx, boolean radiation, int targetTrim){
        if(targetTrim <= 0) return 0;

        int removed = 0;
        for(Tile tile : ctx.tiles){
            if(removed >= targetTrim) break;
            if(tile.block() != Blocks.air) continue;
            if(radiation){
                if(!isRadiationWater(tile.floor())) continue;
            }else{
                if(!isEffluentWater(tile.floor())) continue;
            }

            int near = radiation ? radiationNeighborCount(ctx, tile.x, tile.y) : effluentNeighborCount(ctx, tile.x, tile.y);
            float basin = basinMask(ctx, tile.x, tile.y, radiation ? 877 : 811);
            if(near >= 4 && basin > (radiation ? 0.56f : 0.50f)) continue;

            tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 8).asFloor());
            tile.setOverlay(Blocks.air);
            removed++;
        }

        return removed;
    }

    private boolean canPaintEffluent(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == Blocks.darksand
        || floor == Blocks.shale
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

    private boolean isEffluentWater(Block floor){
        return floor == WHBlocksEnvironment.effluent
        || floor == WHBlocksEnvironment.effluentDeep
        || floor == WHBlocksEnvironment.mineralSandEffluentWater
        || floor == Blocks.darksandTaintedWater
        || floor == Blocks.deepTaintedWater;
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

    private int effluentNeighborCount(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isEffluentWater(near.floor())){
                count++;
            }
        }
        return count;
    }

    private int radiationNeighborCount(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isRadiationWater(near.floor())){
                count++;
            }
        }
        return count;
    }

    private float basinMask(GenContext ctx, int x, int y, int seedOffset){
        float broad = noise(ctx, ctx.seed + seedOffset, x + 91f, y - 63f, 2, 0.62f, 360f);
        float region = noise(ctx, ctx.seed + seedOffset + 5, x - 173f, y + 121f, 2, 0.64f, 220f);
        return broad * 0.68f + region * 0.32f;
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
