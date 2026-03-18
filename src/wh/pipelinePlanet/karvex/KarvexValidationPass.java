package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

import static mindustry.Vars.world;

public class KarvexValidationPass implements GenPass{
    @Override
    public String name(){
        return "KarvexValidationPass";
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
        stripExcludedModFloors(ctx);
    }

    private void validateTopology(GenContext ctx){
        pruneDisconnectedOpenArea(ctx);
        smoothFragmentedWalls(ctx);
        clampWindingCorridors(ctx);
        smoothFragmentedWalls(ctx);
        stabilizeOpenArea(ctx);
        sealDarknessBoundary(ctx);
        pruneDisconnectedOpenArea(ctx);
    }

    private void finalizeBordersAndResources(GenContext ctx){
        ensureRadiationOre(ctx);
        // Erekir-like priority: flood-fill closure first, edge fallback second.
        sealMapBorders(ctx);
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
                if(liquidNeighbors >= 5){
                    if(isRadiationNear(ctx, tile.x, tile.y)){
                        Block target = liquidNeighbors >= 7 ? WHBlocksEnvironment.radiationSand : WHBlocksEnvironment.radiationRockFloor;
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

            for(int rx = -2; rx <= 2; rx++){
                for(int ry = -2; ry <= 2; ry++){
                    if(rx == 0 && ry == 0) continue;
                    Tile near = ctx.tiles.get(tile.x + rx, tile.y + ry);
                    if(near == null || near.block() != Blocks.air) continue;
                    if(!near.floor().hasSurface() || near.floor().isLiquid) continue;

                    float dst = Mathf.dst(rx, ry);
                    if(dst > 2.1f) continue;

                    if(dst <= 1.1f){
                        near.setFloor(WHBlocksEnvironment.radiationSand.asFloor());
                    }else{
                        float chance = 0.35f;
                        if(ctx.rand.chance(chance)){
                            near.setFloor(WHBlocksEnvironment.radiationSand.asFloor());
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
                    if(cardinal || ctx.rand.chance(0.55f)){
                        near.setFloor((cardinal || ctx.rand.chance(0.70f) ? WHBlocksEnvironment.radiationSand : WHBlocksEnvironment.radiationRockFloor).asFloor());
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
                if(walls >= 7) marks[idx] = 2;
                if(open4 <= 1 && walls >= 5) marks[idx] = 2;
                if(isSnakeBend(ctx, tile.x, tile.y) && walls >= 5) marks[idx] = 2;
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
        float minOpen = 0.27f, maxOpen = 0.36f;
        float open = openRatio(ctx);

        if(open < minOpen){
            int target = (int)((minOpen - open) * ctx.width() * ctx.height());
            for(Tile tile : ctx.tiles){
                if(target <= 0) break;
                if(tile.block() == Blocks.air) continue;
                if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
                if(isNearCriticalRoom(ctx, tile.x, tile.y, 9f, 7f)) continue;
                if(!nearAir4(ctx, tile.x, tile.y)) continue;
                if(!ctx.rand.chance(0.36f)) continue;
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
                if(countWallNeighbors(ctx, tile.x, tile.y) < 5) continue;
                if(!ctx.rand.chance(0.66f)) continue;
                tile.setBlock(resolveWallFor(ctx, tile));
                target--;
            }
        }
    }

    private void sealDarknessBoundary(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 6f, 5f)) continue;

            float max = 0f;
            for(Point2 p : Geometry.d8){
                max = Math.max(max, world.getDarkness(tile.x + p.x, tile.y + p.y));
            }

            if(max > 0f){
                tile.setBlock(resolveWallFor(ctx, tile));
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void sealMapBorders(GenContext ctx){
        // Light fallback only; main closure should come from flood-fill.
        int margin = 4;
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

    private void sealBorderTile(GenContext ctx, int x, int y){
        Tile tile = ctx.tiles.getn(x, y);
        boolean needsFloorFix = tile.floor().isLiquid || !tile.floor().hasSurface();
        boolean needsWallFix = tile.block() == Blocks.air;

        // Keep existing valid border walls/floors; only patch leaks.
        if(!needsFloorFix && !needsWallFix) return;

        if(needsFloorFix){
            tile.setFloor(resolveBorderFloor(ctx, tile.x, tile.y).asFloor());
        }
        tile.setBlock(resolveWallFor(ctx, tile));
        tile.setOverlay(Blocks.air);
    }

    private Block resolveBorderFloor(GenContext ctx, int x, int y){
        if(isRadiationNear(ctx, x, y)) return WHBlocksEnvironment.radiationRockFloor;
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
        return floor == WHBlocksEnvironment.parasiticTrachyte
        || floor == WHBlocksEnvironment.cementFloor
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
