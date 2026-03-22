package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

/**
 * 中文说明：Karvex 地图校验与纠偏，保证边界与可玩性稳定。
 */
public class KarvexMapValidationPass implements GenPass{
    private static final int borderSealRadius = 8;

    @Override
    public String name(){
        return "KarvexMapValidationPass";
    }

    @Override
    public void apply(GenContext ctx){
        ensureSpawnAnchor(ctx);
        validateLiquids(ctx);
        validateTopology(ctx);
        finalizeBordersAndResources(ctx);
    }

    private void ensureSpawnAnchor(GenContext ctx){
        if(ctx.spawnRoom != null) return;
        ctx.spawnRoom = new RoomAnchor(ctx.width() / 2, ctx.height() / 2, 10);
        ctx.allRooms.add(ctx.spawnRoom);
    }

    private void validateLiquids(GenContext ctx){
        normalizeLiquidBorders(ctx);
        enrichRadiationShore(ctx);
        mergeRadiationWaters(ctx);
        enforceLiquidShoreSand(ctx);
        separateSlagFromPromethiumAndWater(ctx, 2);
        stripExcludedModFloors(ctx);
    }

    private void validateTopology(GenContext ctx){
        pruneDisconnectedOpenArea(ctx);
        smoothFragmentedWalls(ctx);
        widenMainlandChokes(ctx);
        softenEdgeFishbone(ctx);
        stabilizeOpenArea(ctx);
        sealNoisyBoundary(ctx);
        addBoundaryRockNoise(ctx);
        softenEdgeFishbone(ctx);
        pruneDisconnectedOpenArea(ctx);
        if(hasBorderAirLeak(ctx, 5)){
            pruneDisconnectedOpenArea(ctx);
        }
    }

    private void finalizeBordersAndResources(GenContext ctx){
        ensureRadiationOre(ctx);
        sealJaggedBoundaryRing(ctx, borderSealRadius);
        // Noisy closure first, hard ring fallback second.
        sealBorderFallbackRing(ctx);
        sealOuterLiquidLeaks(ctx);
        restoreInteriorLiquidOpenTiles(ctx);
    }

    private void stripExcludedModFloors(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            if(!isExcludedModFloor(floor)) continue;

            tile.setFloor(resolveExcludedReplacement(ctx, tile.x, tile.y).asFloor());
            if(tile.floor().attributes.get(Attribute.steam) == 0f && tile.overlay().needsSurface && !tile.floor().hasSurface()){
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void ensureRadiationOre(GenContext ctx){
        Block uranium = WHBlocksEnvironment.uraniumOre;
        Block chromium = WHBlocksEnvironment.chromiumOre;
        if(uranium == null && chromium == null) return;

        int radiationTiles = 0;
        int radiationOre = 0;
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!isRadiationFloor(tile.floor())) continue;
            radiationTiles++;
            if(tile.overlay() == uranium || tile.overlay() == chromium) radiationOre++;
        }

        if(radiationTiles <= 0) return;
        int target = Math.max(5, radiationTiles / 54);
        int need = Math.max(target - radiationOre, 0);

        for(Tile tile : ctx.tiles){
            if(need <= 0) break;
            if(tile.block() != Blocks.air) continue;
            if(!isRadiationFloor(tile.floor())) continue;
            if(nearLiquid(ctx, tile.x, tile.y, 1) && tile.floor() != WHBlocksEnvironment.radiationSandWater) continue;
            if(!ctx.rand.chance(0.16f)) continue;
            Block pick = ctx.rand.chance(0.64f) ? uranium : chromium;
            if(pick == null) pick = uranium != null ? uranium : chromium;
            if(pick == null) break;
            tile.setOverlay(pick);
            need--;
        }
    }

    private void normalizeLiquidBorders(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            if(floor == WHBlocksEnvironment.radiationWaterDeep && nearSolid(ctx, tile.x, tile.y, 1)){
                tile.setFloor(WHBlocksEnvironment.mineralSandRadiationWater.asFloor());
            }else if(floor == WHBlocksEnvironment.effluentDeep && nearSolid(ctx, tile.x, tile.y, 1)){
                tile.setFloor(resolveEffluentShallowFor(ctx, tile.x, tile.y).asFloor());
            }else if(floor == Blocks.deepTaintedWater && nearSolid(ctx, tile.x, tile.y, 1)){
                tile.setFloor(Blocks.darksandTaintedWater.asFloor());
            }else if(floor.asFloor().hasSurface() && !floor.asFloor().isLiquid){
                int liquidNeighbors = countShoreLiquidNeighbors(ctx, tile.x, tile.y);
                if(liquidNeighbors >= 4){
                    if(isRadiationNear(ctx, tile.x, tile.y)){
                        Block target = liquidNeighbors >= 6 ? WHBlocksEnvironment.radiationSand : WHBlocksEnvironment.radiationRockFloor;
                        tile.setFloor(target.asFloor());
                    }else{
                        if(canEffluentShoreConvert(floor)){
                            tile.setFloor((liquidNeighbors >= 6 ? WHBlocksEnvironment.mineralSand : WHBlocksEnvironment.mineralSandstone).asFloor());
                        }
                    }
                }
            }
        }
    }

    private void enrichRadiationShore(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(!isRadiationWater(tile.floor())) continue;

            for(int rx = -3; rx <= 3; rx++){
                for(int ry = -3; ry <= 3; ry++){
                    if(rx == 0 && ry == 0) continue;
                    Tile near = ctx.tiles.get(tile.x + rx, tile.y + ry);
                    if(near == null || near.block() != Blocks.air) continue;
                    if(!near.floor().hasSurface() || near.floor().isLiquid) continue;

                    float dst = Mathf.dst(rx, ry);
                    if(dst > 3.05f) continue;

                    if(dst <= 1.2f){
                        near.setFloor(WHBlocksEnvironment.radiationSand.asFloor());
                    }else if(dst <= 2.2f){
                        if(ctx.rand.chance(0.62f)){
                            near.setFloor(WHBlocksEnvironment.radiationSand.asFloor());
                        }
                    }else{
                        if(ctx.rand.chance(0.22f)){
                            near.setFloor((ctx.rand.chance(0.72f) ? WHBlocksEnvironment.radiationSand : WHBlocksEnvironment.radiationRockFloor).asFloor());
                        }
                    }
                }
            }
        }
    }

    private void mergeRadiationWaters(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            if(floor == WHBlocksEnvironment.mineralSandEffluentWater || floor == Blocks.darksandTaintedWater || floor == WHBlocksEnvironment.effluent){
                if(nearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.radiationSand, 1) && nearRadiationWater(ctx, tile.x, tile.y, 1)){
                    tile.setFloor(WHBlocksEnvironment.mineralSandRadiationWater.asFloor());
                }
            }else if(floor == WHBlocksEnvironment.effluentDeep || floor == Blocks.deepTaintedWater){
                if(nearRadiationWater(ctx, tile.x, tile.y, 1)){
                    tile.setFloor(WHBlocksEnvironment.radiationWaterDeep.asFloor());
                }
            }
        }
    }

    private void enforceLiquidShoreSand(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(!isShoreWater(tile.floor())) continue;

            boolean radiation = isRadiationWater(tile.floor());
            boolean darksandEffluent = tile.floor() == Blocks.darksandTaintedWater || tile.floor() == Blocks.deepTaintedWater;
            for(Point2 p : Geometry.d8){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near == null || near.block() != Blocks.air) continue;
                if(!near.floor().hasSurface() || near.floor().isLiquid) continue;
                if(near.floor().attributes.get(Attribute.steam) != 0f) continue;

                Block nearFloor = near.floor();
                boolean cardinal = Math.abs(p.x) + Math.abs(p.y) == 1;

                if(radiation){
                    if(!canRadiationShoreConvert(nearFloor)) continue;
                    if(cardinal || ctx.rand.chance(0.72f)){
                        near.setFloor((cardinal || ctx.rand.chance(0.82f) ? WHBlocksEnvironment.radiationSand : WHBlocksEnvironment.radiationRockFloor).asFloor());
                    }
                }else{
                    if(!canEffluentShoreConvert(nearFloor)) continue;
                    if(cardinal){
                        if(darksandEffluent){
                            near.setFloor((ctx.rand.chance(0.68f) ? Blocks.darksand : WHBlocksEnvironment.darkRock).asFloor());
                        }else{
                            near.setFloor((ctx.rand.chance(0.58f) ? WHBlocksEnvironment.mineralSand : WHBlocksEnvironment.mineralSandstone).asFloor());
                        }
                    }else if(ctx.rand.chance(0.42f)){
                        near.setFloor((darksandEffluent && ctx.rand.chance(0.58f) ? WHBlocksEnvironment.darkRock : WHBlocksEnvironment.mineralSandstone).asFloor());
                    }
                }
            }
        }
    }

    private void pruneDisconnectedOpenArea(GenContext ctx){
        int w = ctx.width(), h = ctx.height();
        Tile start = ctx.tiles.get(ctx.spawnRoom.x, ctx.spawnRoom.y);
        if(start == null) return;
        if(start.block() != Blocks.air) start.setBlock(Blocks.air);

        boolean[] visited = new boolean[w * h];
        IntSeq queue = new IntSeq();
        visited[start.y * w + start.x] = true;
        queue.add(Point2.pack(start.x, start.y));

        for(int qi = 0; qi < queue.size; qi++){
            int packed = queue.get(qi);
            int x = Point2.x(packed), y = Point2.y(packed);
            for(Point2 p : Geometry.d4){
                int nx = x + p.x, ny = y + p.y;
                Tile near = ctx.tiles.get(nx, ny);
                if(near == null || near.block() != Blocks.air) continue;
                int idx = ny * w + nx;
                if(visited[idx]) continue;
                visited[idx] = true;
                queue.add(Point2.pack(nx, ny));
            }
        }

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(visited[tile.y * w + tile.x]) continue;
            tile.setBlock(resolveWallFor(ctx, tile));
            tile.setOverlay(Blocks.air);
        }
    }

    private void smoothFragmentedWalls(GenContext ctx){
        int w = ctx.width();
        byte[] marks = new byte[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 8f, 6f)) continue;

            int walls = countWallNeighbors(ctx, tile.x, tile.y);
            int open4 = countOpen4(ctx, tile.x, tile.y);
            int idx = tile.y * w + tile.x;

            if(tile.block() != Blocks.air){
                if(walls <= 2) marks[idx] = 1;
            }else{
                if(walls >= 8) marks[idx] = 2;
                if(open4 <= 1 && walls >= 6) marks[idx] = 2;
                if(isSnakeBend(ctx, tile.x, tile.y) && walls >= 6) marks[idx] = 2;
            }
        }

        for(Tile tile : ctx.tiles){
            int mark = marks[tile.y * w + tile.x];
            if(mark == 1){
                tile.setBlock(Blocks.air);
            }else if(mark == 2){
                tile.setBlock(resolveWallFor(ctx, tile));
            }
        }
    }

    private void clampWindingCorridors(GenContext ctx){
        int w = ctx.width();
        boolean[] close = new boolean[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 11f, 8f)) continue;

            int walls = countWallNeighbors(ctx, tile.x, tile.y);
            int open4 = countOpen4(ctx, tile.x, tile.y);

            if((open4 <= 1 && walls >= 5) || (isSnakeBend(ctx, tile.x, tile.y) && walls >= 5)){
                close[tile.y * w + tile.x] = true;
            }
        }

        for(Tile tile : ctx.tiles){
            if(close[tile.y * w + tile.x]){
                tile.setBlock(resolveWallFor(ctx, tile));
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void stabilizeOpenArea(GenContext ctx){
        float minOpen = 0.58f, maxOpen = 0.90f;
        float open = openRatio(ctx);

        if(open < minOpen){
            int target = (int)((minOpen - open) * ctx.width() * ctx.height());
            for(Tile tile : ctx.tiles){
                if(target <= 0) break;
                if(tile.block() == Blocks.air) continue;
                if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
                if(isNearCriticalRoom(ctx, tile.x, tile.y, 9f, 7f)) continue;
                if(!nearAir4(ctx, tile.x, tile.y)) continue;
                if(!ctx.rand.chance(0.48f)) continue;
                tile.setBlock(Blocks.air);
                target--;
            }
        }else if(open > maxOpen){
            int target = (int)((open - maxOpen) * ctx.width() * ctx.height());
            for(Tile tile : ctx.tiles){
                if(target <= 0) break;
                if(tile.block() != Blocks.air) continue;
                if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
                if(isNearCriticalRoom(ctx, tile.x, tile.y, 10f, 8f)) continue;
                int walls = countWallNeighbors(ctx, tile.x, tile.y);
                int open4 = countOpen4(ctx, tile.x, tile.y);
                if(walls < 6 || open4 > 1) continue;
                if(!ctx.rand.chance(0.40f)) continue;
                tile.setBlock(resolveWallFor(ctx, tile));
                target--;
            }
        }
    }

    private void separateSlagFromPromethiumAndWater(GenContext ctx, int iterations){
        int width = ctx.width(), height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] writes = new Block[width * height];
            boolean changed = false;

            for(Tile tile : ctx.tiles){
                int idx = tile.x + tile.y * width;
                Block floor = tile.floor();

                if(floor == Blocks.slag){
                    boolean nearPromethium = nearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.promethium, 2)
                    || nearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.promethiumSand, 1);
                    boolean nearWater = nearNonSlagLiquid(ctx, tile.x, tile.y, 2);
                    if(!nearPromethium && !nearWater) continue;

                    writes[idx] = resolveSlagConflictFloor(ctx, tile.x, tile.y, nearPromethium);
                    changed = true;
                    continue;
                }

                if((floor == WHBlocksEnvironment.promethium || floor == WHBlocksEnvironment.promethiumSand)
                && nearFloor(ctx, tile.x, tile.y, Blocks.slag, 2)){
                    Block target = floor == WHBlocksEnvironment.promethium ? WHBlocksEnvironment.promethiumSand : WHBlocksEnvironment.mineralSandstone;
                    if(target != floor){
                        writes[idx] = target;
                        changed = true;
                    }
                    continue;
                }

                if(isNonSlagWater(floor) && nearFloor(ctx, tile.x, tile.y, Blocks.slag, 2)){
                    writes[idx] = resolveWaterSlagConflictFloor(ctx, tile.x, tile.y);
                    changed = true;
                }
            }

            if(!changed) break;

            for(int i = 0; i < writes.length; i++){
                Block target = writes[i];
                if(target == null) continue;

                int x = i % width, y = i / width;
                ctx.tiles.getn(x, y).setFloor(target.asFloor());
            }
        }
    }

    private Block resolveSlagConflictFloor(GenContext ctx, int x, int y, boolean nearPromethium){
        if(nearPromethium){
            return WHBlocksEnvironment.promethiumSand;
        }
        if(nearRadiationWater(ctx, x, y, 1)){
            return WHBlocksEnvironment.radiationRockFloor;
        }
        if(nearFloor(ctx, x, y, Blocks.darksandTaintedWater, 1) || nearFloor(ctx, x, y, Blocks.deepTaintedWater, 1)){
            return Blocks.darksand;
        }
        if(nearFloor(ctx, x, y, WHBlocksEnvironment.effluent, 1)
        || nearFloor(ctx, x, y, WHBlocksEnvironment.effluentDeep, 1)
        || nearFloor(ctx, x, y, WHBlocksEnvironment.mineralSandEffluentWater, 1)){
            return WHBlocksEnvironment.mineralSandstone;
        }
        return WHBlocksEnvironment.darkRock;
    }

    private Block resolveWaterSlagConflictFloor(GenContext ctx, int x, int y){
        boolean nearPromethium = nearFloor(ctx, x, y, WHBlocksEnvironment.promethium, 2)
        || nearFloor(ctx, x, y, WHBlocksEnvironment.promethiumSand, 1);
        return resolveSlagConflictFloor(ctx, x, y, nearPromethium);
    }

    private void sealNoisyBoundary(GenContext ctx){
        int w = ctx.width(), h = ctx.height();
        int boundaryRadius = borderSealRadius;

        for(Tile tile : ctx.tiles){
            int x = tile.x, y = tile.y;
            int edgeDist = distanceToMapEdge(x, y, w, h);
            if(edgeDist > boundaryRadius) continue;

            float broad = Simplex.noise2d(ctx.seed + 1301, 2, 0.58f, 1f / 92f, x + 19.3f, y - 11.7f);
            float detail = Simplex.noise2d(ctx.seed + 1309, 3, 0.62f, 1f / 37f, x - 21.9f, y + 15.4f);
            float fringe = Simplex.noise2d(ctx.seed + 1317, 1, 1f, 1f / 19f, x + 7.6f, y + 4.2f);
            float warpA = Simplex.noise2d(ctx.seed + 1327, 2, 0.60f, 1f / 57f, x - 13.7f, y + 24.1f);
            float warpB = Simplex.noise2d(ctx.seed + 1331, 2, 0.65f, 1f / 24f, x + 33.4f, y - 9.2f);
            float ridge = Math.abs(Simplex.noise2d(ctx.seed + 1337, 2, 0.58f, 1f / 31f, x - 5.1f, y + 14.6f));

            // Radius-8 jagged closure band.
            float warpedDist = edgeDist + warpA * 1.2f + warpB * 0.8f - ridge * 0.7f;
            float hardBand = Mathf.clamp(7.05f + broad * 1.05f + detail * 0.85f + ridge * 0.55f, 6.1f, 8.9f);
            float softBand = Mathf.clamp(hardBand + 0.95f + fringe * 0.55f + warpA * 0.28f, hardBand + 0.4f, 9.8f);

            boolean seal = edgeDist <= 2 || warpedDist <= hardBand;
            if(!seal && warpedDist <= softBand){
                float t = (softBand - warpedDist) / Math.max(softBand - hardBand, 0.001f);
                float jitter = 0.74f
                + Simplex.noise2d(ctx.seed + 1321, 1, 1f, 1f / 16f, x - 3.2f, y + 6.8f) * 0.20f
                + Simplex.noise2d(ctx.seed + 1341, 1, 1f, 1f / 11f, x + 17.6f, y - 12.3f) * 0.10f;
                seal = t > jitter;
            }

            if(!seal) continue;

            boolean floorUnsafe = tile.floor().isLiquid || !tile.floor().hasSurface();
            if(floorUnsafe){
                tile.setFloor(resolveBoundaryFloor(ctx, x, y).asFloor());
                tile.setBlock(resolveWallFor(ctx, tile));
                tile.setOverlay(Blocks.air);
                continue;
            }

            if(tile.block() == Blocks.air && edgeDist >= 3 && warpedDist > (hardBand - 0.2f) && ctx.rand.chance(0.42f)){
                continue;
            }

            boolean strictEdge = edgeDist <= 2 || warpedDist <= (hardBand - 0.45f);
            if(strictEdge || tile.block() != Blocks.air){
                tile.setBlock(resolveWallFor(ctx, tile));
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void addBoundaryRockNoise(GenContext ctx){
        int w = ctx.width(), h = ctx.height();
        for(Tile tile : ctx.tiles){
            int edgeDist = distanceToMapEdge(tile.x, tile.y, w, h);
            if(edgeDist < 2 || edgeDist > 8) continue;
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isRadiationNear(ctx, tile.x, tile.y)) continue;
            if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 2) || nearFloor(ctx, tile.x, tile.y, Blocks.tar, 2) || nearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.promethium, 2)){
                continue;
            }

            float macro = Simplex.noise2d(ctx.seed + 1381, 2, 0.60f, 1f / 19f, tile.x + 14.1f, tile.y - 8.7f);
            float detail = Simplex.noise2d(ctx.seed + 1387, 2, 0.64f, 1f / 9f, tile.x - 6.3f, tile.y + 11.4f);
            float field = macro * 0.72f + detail * 0.28f;
            Block floor = tile.floor();

            if(floor == WHBlocksEnvironment.darkRock && field > 0.34f){
                tile.setFloor((ctx.rand.chance(0.56f) ? Blocks.stone : Blocks.shale).asFloor());
            }else if((floor == Blocks.stone || floor == Blocks.shale || floor == Blocks.carbonStone) && field < -0.24f){
                tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
            }
        }
    }

    private void widenMainlandChokes(GenContext ctx){
        int width = ctx.width(), height = ctx.height();
        for(int it = 0; it < 2; it++){
            boolean[] carve = new boolean[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() == Blocks.air) continue;
                    if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
                    if(isNearCriticalRoom(ctx, x, y, 11f, 8f)) continue;

                    int open4 = countOpen4(ctx, x, y);
                    int walls = countWallNeighbors(ctx, x, y);
                    if(open4 < 1 || walls > 6) continue;

                    float macro = Simplex.noise2d(ctx.seed + 1451 + it * 13, 2, 0.60f, 1f / 24f, x + 7.3f, y - 4.9f);
                    if(macro < -0.18f) continue;
                    carve[y * width + x] = true;
                }
            }

            for(int i = 0; i < carve.length; i++){
                if(!carve[i]) continue;
                int x = i % width, y = i / width;
                carveOpenPatch(ctx, x, y, 1);
            }
        }
    }

    private void softenEdgeFishbone(GenContext ctx){
        int width = ctx.width(), height = ctx.height();
        boolean[] carve = new boolean[width * height];

        for(int x = 1; x < width - 1; x++){
            for(int y = 1; y < height - 1; y++){
                Tile tile = ctx.tiles.getn(x, y);
                if(tile.block() == Blocks.air) continue;
                if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
                if(isNearCriticalRoom(ctx, x, y, 12f, 9f)) continue;

                int edgeDist = distanceToMapEdge(x, y, width, height);
                if(edgeDist < 9 || edgeDist > 18) continue;

                int open4 = countOpen4(ctx, x, y);
                int walls = countWallNeighbors(ctx, x, y);
                if(!(open4 >= 3 || (open4 >= 2 && walls <= 4))) continue;

                float mainlandBias = Simplex.noise2d(ctx.seed + 1469, 2, 0.60f, 1f / 26f, x + 11.7f, y - 15.2f);
                if(edgeDist >= 8 && mainlandBias < 0.15f) continue;
                carve[y * width + x] = true;
            }
        }

        for(int i = 0; i < carve.length; i++){
            if(!carve[i]) continue;
            int x = i % width, y = i / width;
            Tile tile = ctx.tiles.getn(x, y);
            tile.setBlock(Blocks.air);
            tile.setOverlay(Blocks.air);
        }
    }

    private void carveOpenPatch(GenContext ctx, int cx, int cy, int radius){
        int r2 = radius * radius;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                int x = cx + ox, y = cy + oy;
                Tile tile = ctx.tiles.get(x, y);
                if(tile == null) continue;
                if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
                if(isNearCriticalRoom(ctx, x, y, 11f, 8f)) continue;
                tile.setBlock(Blocks.air);
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private boolean hasBorderAirLeak(GenContext ctx, int margin){
        int w = ctx.width(), h = ctx.height();

        for(int x = 0; x < w; x++){
            for(int y = 0; y < margin; y++){
                Tile top = ctx.tiles.getn(x, y);
                Tile bot = ctx.tiles.getn(x, h - 1 - y);
                if(top.block() == Blocks.air || bot.block() == Blocks.air){
                    return true;
                }
            }
        }

        for(int y = margin; y < h - margin; y++){
            for(int x = 0; x < margin; x++){
                Tile left = ctx.tiles.getn(x, y);
                Tile right = ctx.tiles.getn(w - 1 - x, y);
                if(left.block() == Blocks.air || right.block() == Blocks.air){
                    return true;
                }
            }
        }

        return false;
    }

    private void sealBorderFallbackRing(GenContext ctx){
        int margin = 2;
        int w = ctx.width(), h = ctx.height();

        for(int x = 0; x < w; x++){
            for(int y = 0; y < margin; y++) sealBorderTile(ctx, x, y);
            for(int y = h - margin; y < h; y++) sealBorderTile(ctx, x, y);
        }

        for(int y = margin; y < h - margin; y++){
            for(int x = 0; x < margin; x++) sealBorderTile(ctx, x, y);
            for(int x = w - margin; x < w; x++) sealBorderTile(ctx, x, y);
        }
    }

    private void sealOuterLiquidLeaks(GenContext ctx){
        int w = ctx.width(), h = ctx.height();
        int band = 2;

        for(Tile tile : ctx.tiles){
            int edgeDist = distanceToMapEdge(tile.x, tile.y, w, h);
            if(edgeDist > band) continue;

            boolean invalidFloor = tile.floor().isLiquid || !tile.floor().hasSurface();
            boolean edgeAirLeak = tile.block() == Blocks.air && edgeDist <= 1;
            if(!invalidFloor && !edgeAirLeak) continue;

            if(invalidFloor){
                tile.setFloor(resolveBoundaryFloor(ctx, tile.x, tile.y).asFloor());
            }
            tile.setBlock(resolveWallFor(ctx, tile));
            tile.setOverlay(Blocks.air);
        }
    }

    private void restoreInteriorLiquidOpenTiles(GenContext ctx){
        int w = ctx.width(), h = ctx.height();
        int edgeMargin = borderSealRadius + 1;

        for(Tile tile : ctx.tiles){
            if(!tile.floor().isLiquid) continue;
            if(distanceToMapEdge(tile.x, tile.y, w, h) <= edgeMargin) continue;
            if(tile.block() == Blocks.air) continue;

            tile.setBlock(Blocks.air);
            tile.setOverlay(Blocks.air);
        }
    }

    private void sealJaggedBoundaryRing(GenContext ctx, int radius){
        int w = ctx.width(), h = ctx.height();
        int minDepth = Math.max(radius - 1, 1);

        for(Tile tile : ctx.tiles){
            int x = tile.x, y = tile.y;
            int edgeDist = distanceToMapEdge(x, y, w, h);
            if(edgeDist > radius) continue;

            int side = nearestMapEdgeSide(x, y, w, h);
            int along = (side == 0 || side == 1) ? y : x;

            // 2-tile teeth: alternate between 7/8-depth bands, then perturb with noise.
            int toothIndex = along / 2;
            int targetDepth = ((toothIndex + side + (ctx.seed & 7)) & 1) == 0 ? radius : minDepth;

            float macro = Simplex.noise2d(ctx.seed + 1511 + side * 11, 2, 0.60f, 1f / 27f, along + 9.3f, toothIndex - 5.7f);
            float detail = Simplex.noise2d(ctx.seed + 1517 + side * 17, 1, 0.67f, 1f / 9f, toothIndex + 3.1f, along - 7.9f);
            float jag = macro * 0.72f + detail * 0.28f;

            if(jag > 0.40f){
                targetDepth = radius;
            }else if(jag < -0.46f){
                targetDepth = minDepth;
            }

            if(edgeDist > targetDepth) continue;

            if(tile.floor().isLiquid || !tile.floor().hasSurface()){
                tile.setFloor(resolveBoundaryFloor(ctx, x, y).asFloor());
            }
            tile.setBlock(resolveWallFor(ctx, tile));
            tile.setOverlay(Blocks.air);
        }
    }

    private int nearestMapEdgeSide(int x, int y, int w, int h){
        int left = x, right = w - 1 - x, bottom = y, top = h - 1 - y;
        int best = left;
        int side = 0;

        if(right < best){
            best = right;
            side = 1;
        }
        if(bottom < best){
            best = bottom;
            side = 2;
        }
        if(top < best){
            side = 3;
        }

        return side;
    }

    private void sealBorderTile(GenContext ctx, int x, int y){
        Tile tile = ctx.tiles.getn(x, y);
        boolean needsFloorFix = tile.floor().isLiquid || !tile.floor().hasSurface();
        boolean needsWallFix = tile.block() == Blocks.air;

        // Keep existing valid border walls/floors; only patch leaks.
        if(!needsFloorFix && !needsWallFix) return;

        if(needsFloorFix){
            tile.setFloor(resolveBoundaryFloor(ctx, tile.x, tile.y).asFloor());
        }
        tile.setBlock(resolveWallFor(ctx, tile));
        tile.setOverlay(Blocks.air);
    }

    private Block resolveBoundaryFloor(GenContext ctx, int x, int y){
        if(isRadiationNear(ctx, x, y)) return WHBlocksEnvironment.radiationRockFloor;
        Block dominant = dominantBoundaryFloor(ctx, x, y, 4);
        if(dominant != null) return dominant;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near == null) continue;
            Block floor = near.floor();
            if(floor == WHBlocksEnvironment.darkRock
            || floor == WHBlocksEnvironment.scorchedEarth
            || floor == WHBlocksEnvironment.scorchedStone
            || floor == WHBlocksEnvironment.darkHotRock
            || floor == WHBlocksEnvironment.darkMagmaRock){
                return WHBlocksEnvironment.darkRock;
            }
        }
        return WHBlocksEnvironment.mineralSandstone;
    }

    private Block dominantBoundaryFloor(GenContext ctx, int x, int y, int radius){
        Block[] candidates = new Block[24];
        int[] counts = new int[24];
        int unique = 0;
        int bestCount = 0;
        Block best = null;

        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near == null) continue;

                Block floor = normalizeBoundaryFloor(near.floor());
                if(floor == null) continue;

                int slot = -1;
                for(int i = 0; i < unique; i++){
                    if(candidates[i] == floor){
                        slot = i;
                        break;
                    }
                }

                if(slot == -1){
                    if(unique >= candidates.length) continue;
                    slot = unique++;
                    candidates[slot] = floor;
                    counts[slot] = 0;
                }

                int count = ++counts[slot];
                if(count > bestCount){
                    bestCount = count;
                    best = floor;
                }
            }
        }

        return bestCount >= 5 ? best : null;
    }

    private Block normalizeBoundaryFloor(Block floor){
        if(floor == null || !floor.asFloor().hasSurface() || floor.asFloor().isLiquid){
            return null;
        }
        if(floor == Blocks.slag || floor == Blocks.tar || floor == WHBlocksEnvironment.promethium){
            return WHBlocksEnvironment.darkRock;
        }
        if(floor == WHBlocksEnvironment.trachyte){
            return WHBlocksEnvironment.darkRock;
        }
        if(floor == WHBlocksEnvironment.promethiumSand){
            return WHBlocksEnvironment.mineralSandstone;
        }
        if(floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock){
            return WHBlocksEnvironment.darkRock;
        }
        if(floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters){
            return WHBlocksEnvironment.radiationRockFloor;
        }
        return floor;
    }

    private int distanceToMapEdge(int x, int y, int w, int h){
        int dx = Math.min(x, w - 1 - x);
        int dy = Math.min(y, h - 1 - y);
        return Math.min(dx, dy);
    }

    private Block resolveWallFor(GenContext ctx, Tile tile){
        Block wall = tile.floor().asFloor().wall;
        if(wall != null && wall != Blocks.air) return wall;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
            if(near == null) continue;
            Block nwall = near.floor().asFloor().wall;
            if(nwall != null && nwall != Blocks.air) return nwall;
        }
        return Blocks.stoneWall;
    }

    private boolean nearAir4(GenContext ctx, int x, int y){
        for(Point2 p : Geometry.d4){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.block() == Blocks.air) return true;
        }
        return false;
    }

    private int countWallNeighbors(GenContext ctx, int x, int y){
        int walls = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.block() != Blocks.air) walls++;
        }
        return walls;
    }

    private int countOpen4(GenContext ctx, int x, int y){
        int open = 0;
        for(Point2 p : Geometry.d4){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.block() == Blocks.air) open++;
        }
        return open;
    }

    private boolean isSnakeBend(GenContext ctx, int x, int y){
        Tile l = ctx.tiles.get(x - 1, y), r = ctx.tiles.get(x + 1, y), u = ctx.tiles.get(x, y + 1), d = ctx.tiles.get(x, y - 1);
        boolean hl = l != null && l.block() == Blocks.air;
        boolean hr = r != null && r.block() == Blocks.air;
        boolean hu = u != null && u.block() == Blocks.air;
        boolean hd = d != null && d.block() == Blocks.air;

        return (hl && hu && !hr && !hd)
        || (hu && hr && !hl && !hd)
        || (hr && hd && !hl && !hu)
        || (hd && hl && !hr && !hu);
    }

    private float openRatio(GenContext ctx){
        int total = 0, open = 0;
        for(Tile tile : ctx.tiles){
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            total++;
            if(tile.block() == Blocks.air) open++;
        }
        return total == 0 ? 0f : (float)open / total;
    }

    private boolean nearSolid(GenContext ctx, int x, int y, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile tile = ctx.tiles.get(x + rx, y + ry);
                if(tile != null && tile.floor().hasSurface() && !tile.floor().isLiquid) return true;
            }
        }
        return false;
    }

    private int countShoreLiquidNeighbors(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isShoreWater(near.floor())) count++;
        }
        return count;
    }

    private boolean isRadiationNear(GenContext ctx, int x, int y){
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near == null) continue;
            if(isRadiationWater(near.floor())) return true;
        }
        return false;
    }

    private Block resolveEffluentShallowFor(GenContext ctx, int x, int y){
        if(nearFloor(ctx, x, y, Blocks.darksand, 1) || nearFloor(ctx, x, y, Blocks.shale, 1)){
            return Blocks.darksandTaintedWater;
        }
        return WHBlocksEnvironment.mineralSandEffluentWater;
    }

    private boolean isExcludedModFloor(Block floor){
        return floor == WHBlocksEnvironment.cementFloor
        || floor == WHBlocksEnvironment.cementTile1
        || floor == WHBlocksEnvironment.cementTile2
        || floor == WHBlocksEnvironment.cementTile3
        || floor == WHBlocksEnvironment.cementTile4
        || floor == WHBlocksEnvironment.cementVent;
    }

    private Block resolveExcludedReplacement(GenContext ctx, int x, int y){
        if(isRadiationNear(ctx, x, y)){
            return WHBlocksEnvironment.radiationRockFloor;
        }

        if(nearFloor(ctx, x, y, Blocks.darksandTaintedWater, 1) || nearFloor(ctx, x, y, Blocks.deepTaintedWater, 1)){
            return Blocks.darksand;
        }

        if(nearFloor(ctx, x, y, WHBlocksEnvironment.effluent, 1)
        || nearFloor(ctx, x, y, WHBlocksEnvironment.effluentDeep, 1)
        || nearFloor(ctx, x, y, WHBlocksEnvironment.mineralSandEffluentWater, 1)){
            return WHBlocksEnvironment.mineralSandstone;
        }

        if(nearFloor(ctx, x, y, WHBlocksEnvironment.darkHotRock, 1)
        || nearFloor(ctx, x, y, WHBlocksEnvironment.darkMagmaRock, 1)){
            return WHBlocksEnvironment.darkRock;
        }

        return ctx.rand.chance(0.60f) ? WHBlocksEnvironment.darkRock : Blocks.stone;
    }

    private boolean canRadiationShoreConvert(Block floor){
        if(floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters){
            return true;
        }

        if(isVolcanicFloor(floor) || isPolarRedFloor(floor)){
            return false;
        }

        return canEffluentShoreConvert(floor);
    }

    private boolean canEffluentShoreConvert(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.carbonStone
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.shale
        || floor == Blocks.stone
        || floor == Blocks.craters
        || floor == Blocks.darksand
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.dacite
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private boolean isVolcanicFloor(Block floor){
        return floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock;
    }

    private boolean isPolarRedFloor(Block floor){
        return floor == Blocks.redmat
        || floor == Blocks.redStone
        || floor == Blocks.denseRedStone
        || floor == Blocks.redIce;
    }

    private boolean nearFloor(GenContext ctx, int x, int y, Block floor, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.floor() == floor) return true;
            }
        }
        return false;
    }

    private boolean nearLiquid(GenContext ctx, int x, int y, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile tile = ctx.tiles.get(x + rx, y + ry);
                if(tile != null && tile.floor().isLiquid) return true;
            }
        }
        return false;
    }

    private boolean nearNonSlagLiquid(GenContext ctx, int x, int y, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                if(rx == 0 && ry == 0) continue;
                Tile tile = ctx.tiles.get(x + rx, y + ry);
                if(tile == null) continue;
                Block floor = tile.floor();
                if(isNonSlagWater(floor)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNonSlagWater(Block floor){
        return floor != Blocks.slag && floor != Blocks.tar && floor.asFloor().isLiquid;
    }

    private boolean nearRadiationWater(GenContext ctx, int x, int y, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile tile = ctx.tiles.get(x + rx, y + ry);
                if(tile != null && isRadiationWater(tile.floor())){
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

    private boolean isRadiationFloor(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationSandWater;
    }

    private boolean isRadiationWater(Block floor){
        return floor == WHBlocksEnvironment.radiationWater
        || floor == WHBlocksEnvironment.radiationWaterDeep
        || floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationSandWater;
    }

    private boolean isShoreWater(Block floor){
        return floor == WHBlocksEnvironment.effluent
        || floor == WHBlocksEnvironment.effluentDeep
        || floor == WHBlocksEnvironment.mineralSandEffluentWater
        || floor == Blocks.darksandTaintedWater
        || floor == Blocks.deepTaintedWater
        || isRadiationWater(floor);
    }
}
