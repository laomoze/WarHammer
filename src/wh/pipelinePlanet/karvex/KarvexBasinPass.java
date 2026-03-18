package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

public class KarvexBasinPass implements GenPass{
    @Override
    public String name(){
        return "KarvexBasinPass";
    }

    @Override
    public void apply(GenContext ctx){
        seedBasins(ctx);
        widenBasins(ctx, 3);
        ensureMinimumBasins(ctx);
        cleanupSingles(ctx);
        reinforceHeatAndPromethiumEdges(ctx);
    }

    private void seedBasins(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 34f, 20f)) continue;

            Block floor = tile.floor();

            float promA = noise(ctx, ctx.seed + 451, tile.x + 310f, tile.y - 220f, 5, 0.74f, 150f);
            float promB = noise(ctx, ctx.seed + 457, tile.x - 130f, tile.y + 500f, 3, 0.78f, 46f);
            if(isPromethiumCandidate(floor) && promA > 0.78f && promB > 0.50f){
                tile.setFloor(WHBlocksEnvironment.promethium.asFloor());
                tile.setOverlay(Blocks.air);
                continue;
            }

            float oilA = noise(ctx, ctx.seed + 401, tile.x - 440f, tile.y + 210f, 5, 0.72f, 125f);
            float oilB = noise(ctx, ctx.seed + 409, tile.x + 91f, tile.y - 370f, 2, 0.92f, 35f);
            float oilC = noise(ctx, ctx.seed + 433, tile.x - 180f, tile.y + 620f, 5, 0.78f, 105f);
            float oilD = noise(ctx, ctx.seed + 439, tile.x + 250f, tile.y - 540f, 2, 0.91f, 32f);

            boolean oilMatch = isTarCandidate(floor)
            && ((oilA > 0.72f && oilB > 0.52f) || (oilC > 0.66f && oilD > 0.46f));
            if(oilMatch && !nearFloor(ctx, tile.x, tile.y, Blocks.slag, 2)){
                tile.setFloor(Blocks.tar.asFloor());
                tile.setOverlay(Blocks.air);
                continue;
            }

            float slagA = noise(ctx, ctx.seed + 421, tile.x + 720f, tile.y - 190f, 6, 0.74f, 140f);
            float slagB = noise(ctx, ctx.seed + 429, tile.x - 210f, tile.y + 470f, 3, 0.78f, 44f);
            if(isSlagCandidate(floor) && slagA > 0.69f && slagB > 0.45f){
                if(nearFloor(ctx, tile.x, tile.y, Blocks.tar, 1) || nearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.promethium, 1)){
                    continue;
                }
                tile.setFloor(Blocks.slag.asFloor());
                tile.setOverlay(Blocks.air);
                continue;
            }

            float radA = noise(ctx, ctx.seed + 463, tile.x + 402f, tile.y - 163f, 4, 0.74f, 132f);
            float radB = noise(ctx, ctx.seed + 467, tile.x - 210f, tile.y + 410f, 2, 0.86f, 52f);
            if(isRadiationCandidate(floor) && radA > 0.74f && radB > 0.48f){
                tile.setFloor((radA > 0.82f && !nearSolid(ctx, tile.x, tile.y, 1)
                ? WHBlocksEnvironment.radiationWaterDeep
                : WHBlocksEnvironment.mineralSandRadiationWater).asFloor());
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void widenBasins(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            byte[] marks = new byte[width * height];

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                if(isNearCriticalRoom(ctx, tile.x, tile.y, 30f, 16f)) continue;

                int tar = 0, slag = 0, prom = 0, rad = 0;
                for(Point2 p : Geometry.d8){
                    Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                    if(near == null) continue;

                    if(near.floor() == Blocks.tar) tar++;
                    if(near.floor() == Blocks.slag) slag++;
                    if(near.floor() == WHBlocksEnvironment.promethium) prom++;
                    if(isRadiationWater(near.floor())) rad++;
                }

                int idx = tile.x + tile.y * width;
                if(prom >= 3 && isPromethiumCandidate(tile.floor())){
                    marks[idx] = 3;
                }else if(tar >= 4 && isTarCandidate(tile.floor()) && !nearFloor(ctx, tile.x, tile.y, Blocks.slag, 1)){
                    marks[idx] = 1;
                }else if(slag >= 3 && isSlagCandidate(tile.floor()) && !nearFloor(ctx, tile.x, tile.y, Blocks.tar, 1)){
                    marks[idx] = 2;
                }else if(rad >= 5 && isRadiationCandidate(tile.floor())){
                    marks[idx] = (byte)(rad >= 7 && ctx.rand.chance(0.34f) ? 5 : 4);
                }
            }

            for(Tile tile : ctx.tiles){
                int mark = marks[tile.x + tile.y * width];
                if(mark == 0) continue;

                if(mark == 1){
                    tile.setFloor(Blocks.tar.asFloor());
                }else if(mark == 2){
                    tile.setFloor(Blocks.slag.asFloor());
                }else if(mark == 3){
                    tile.setFloor(WHBlocksEnvironment.promethium.asFloor());
                }else if(mark == 4){
                    tile.setFloor(WHBlocksEnvironment.mineralSandRadiationWater.asFloor());
                }else{
                    tile.setFloor(WHBlocksEnvironment.radiationWaterDeep.asFloor());
                }
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void ensureMinimumBasins(GenContext ctx){
        int area = ctx.width() * ctx.height();
        int minTar = Math.max(70, area / 2200);
        int minSlag = Math.max(120, area / 1500);
        int minPromethium = Math.max(40, area / 4500);
        int minRadiation = Math.max(26, area / 5200);

        int tries = 0;
        while(countFloor(ctx, Blocks.tar) < minTar && tries++ < 15){
            stampPatch(ctx, 0);
        }

        tries = 0;
        while(countFloor(ctx, Blocks.slag) < minSlag && tries++ < 17){
            stampPatch(ctx, 1);
        }

        tries = 0;
        while(countFloor(ctx, WHBlocksEnvironment.promethium) < minPromethium && tries++ < 14){
            stampPatch(ctx, 2);
        }

        tries = 0;
        while(countRadiationWater(ctx) < minRadiation && tries++ < 14){
            stampPatch(ctx, 3);
        }
    }

    private void stampPatch(GenContext ctx, int type){
        int cx = ctx.rand.random(12, ctx.width() - 13);
        int cy = ctx.rand.random(12, ctx.height() - 13);
        if(isNearCriticalRoom(ctx, cx, cy, 30f, 16f)) return;

        int radius;
        if(type == 1){
            radius = ctx.rand.random(7, 11);
        }else if(type == 3){
            radius = ctx.rand.random(4, 7);
        }else{
            radius = ctx.rand.random(5, 8);
        }
        int r2 = radius * radius;

        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null || tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;

                float edge = Mathf.dst(ox, oy) / Math.max(radius, 1f);
                if(!ctx.rand.chance(Mathf.lerp(0.90f, 0.36f, edge))) continue;

                if(type == 0){
                    if(!isTarCandidate(tile.floor())) continue;
                    tile.setFloor(Blocks.tar.asFloor());
                }else if(type == 1){
                    if(!isSlagCandidate(tile.floor())) continue;
                    tile.setFloor(Blocks.slag.asFloor());
                }else if(type == 2){
                    if(!isPromethiumCandidate(tile.floor())) continue;
                    tile.setFloor(WHBlocksEnvironment.promethium.asFloor());
                }else{
                    if(!isRadiationCandidate(tile.floor())) continue;
                    boolean deep = edge < 0.54f && ctx.rand.chance(0.32f);
                    tile.setFloor((deep ? WHBlocksEnvironment.radiationWaterDeep : WHBlocksEnvironment.mineralSandRadiationWater).asFloor());
                }

                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void cleanupSingles(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            if(floor != Blocks.tar && floor != Blocks.slag && floor != WHBlocksEnvironment.promethium && !isRadiationWater(floor)) continue;

            int same = 0;
            for(Point2 p : Geometry.d8){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near == null) continue;
                if(near.floor() == floor) same++;
            }

            if(same > 1) continue;

            if(floor == Blocks.slag){
                tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
            }else if(floor == Blocks.tar){
                tile.setFloor((nearFloor(ctx, tile.x, tile.y, Blocks.darksand, 1) || nearFloor(ctx, tile.x, tile.y, Blocks.shale, 1)
                ? Blocks.darksand
                : WHBlocksEnvironment.mineralSandstone).asFloor());
            }else if(floor == WHBlocksEnvironment.promethium){
                tile.setFloor(WHBlocksEnvironment.promethiumSand.asFloor());
            }else{
                tile.setFloor(WHBlocksEnvironment.radiationSand.asFloor());
            }

            tile.setOverlay(Blocks.air);
        }
    }

    private void reinforceHeatAndPromethiumEdges(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            if(floor != Blocks.slag && floor != WHBlocksEnvironment.promethium) continue;

            for(Point2 p : Geometry.d8){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near == null || near.block() != Blocks.air) continue;

                if(near.floor().isLiquid){
                    if(near.floor() != Blocks.slag && near.floor() != WHBlocksEnvironment.promethium){
                        near.setFloor((floor == WHBlocksEnvironment.promethium ? WHBlocksEnvironment.promethiumSand : WHBlocksEnvironment.darkHotRock).asFloor());
                    }
                    continue;
                }

                if(!near.floor().hasSurface()) continue;
                if(isNearCriticalRoom(ctx, near.x, near.y, 24f, 12f)) continue;

                boolean cardinal = Math.abs(p.x) + Math.abs(p.y) == 1;
                float chance = cardinal ? 0.70f : 0.38f;
                if(!ctx.rand.chance(chance)) continue;

                if(floor == Blocks.slag){
                    if(canHeatTint(near.floor())){
                        near.setFloor((ctx.rand.chance(0.62f) ? WHBlocksEnvironment.darkHotRock : WHBlocksEnvironment.scorchedStone).asFloor());
                    }
                }else if(canPromethiumTint(near.floor())){
                    near.setFloor((ctx.rand.chance(0.62f) ? WHBlocksEnvironment.promethiumSand : WHBlocksEnvironment.mineralSandstone).asFloor());
                }
            }
        }
    }

    private boolean isTarCandidate(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.carbonStone
        || floor == Blocks.shale
        || floor == Blocks.stone
        || floor == Blocks.darksand;
    }

    private boolean isSlagCandidate(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock
        || floor == Blocks.dacite
        || floor == Blocks.craters;
    }

    private boolean isPromethiumCandidate(Block floor){
        return floor == WHBlocksEnvironment.promethiumSand
        || floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor;
    }

    private boolean isRadiationCandidate(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == WHBlocksEnvironment.darkRock;
    }

    private boolean canHeatTint(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock
        || floor == Blocks.dacite
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite;
    }

    private boolean canPromethiumTint(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == Blocks.darksand
        || floor == Blocks.shale;
    }

    private boolean isRadiationWater(Block floor){
        return floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationWaterDeep
        || floor == WHBlocksEnvironment.radiationSandWater
        || floor == WHBlocksEnvironment.radiationWater;
    }

    private int countFloor(GenContext ctx, Block block){
        int count = 0;
        for(Tile tile : ctx.tiles){
            if(tile.floor() == block) count++;
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

    private boolean nearFloor(GenContext ctx, int x, int y, Block floor, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.floor() == floor){
                    return true;
                }
            }
        }
        return false;
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
        if(ctx.spawnRoom != null && Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + spawnDst)) return true;
        for(RoomAnchor enemy : ctx.enemyRooms){
            if(Mathf.within(x, y, enemy.x, enemy.y, enemy.radius + enemyDst)) return true;
        }
        return false;
    }

    private float noise(GenContext ctx, int seed, float x, float y, double octaves, double falloff, double scl){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z);
    }
}
