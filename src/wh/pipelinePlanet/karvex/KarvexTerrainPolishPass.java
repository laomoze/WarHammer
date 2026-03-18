package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

/**
 * Light terrain cleanup pass to reduce tiny fragments and snake-like tunnels.
 */
public class KarvexTerrainPolishPass implements GenPass{
    private static final int protectedRoomPadding = 3;

    @Override
    public String name(){
        return "KarvexTerrainPolishPass";
    }

    @Override
    public void apply(GenContext ctx){
        warpBoundaries(ctx);
        smoothOpenMask(ctx, 3);
        cleanupSingles(ctx);
        rebalanceMetalFloors(ctx);
        smoothFloorTransitions(ctx, 3);
    }

    private void warpBoundaries(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        boolean[] open = snapshotOpen(ctx);

        for(int x = 1; x < width - 1; x++){
            for(int y = 1; y < height - 1; y++){
                if(isProtected(ctx, x, y)) continue;

                Tile tile = ctx.tiles.getn(x, y);
                if(!tile.floor().hasSurface()) continue;

                int index = indexOf(x, y, width);
                boolean isOpen = open[index];
                int around = countOpen8(open, width, height, x, y);
                if(around != 4) continue;

                float noise = Simplex.noise2d(ctx.seed + 91, 2, 0.6f, 1f / 22f, x + 17.3f, y - 9.1f);

                if(!isOpen && noise > 0.33f){
                    tile.setBlock(Blocks.air);
                }else if(isOpen && noise < -0.24f){
                    setWallFromFloor(tile);
                }
            }
        }
    }

    private void smoothOpenMask(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            boolean[] open = snapshotOpen(ctx);

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(isProtected(ctx, x, y)) continue;

                    Tile tile = ctx.tiles.getn(x, y);
                    if(!tile.floor().hasSurface()) continue;

                    boolean isOpen = open[indexOf(x, y, width)];
                    int around = countOpen8(open, width, height, x, y);

                    if(isOpen){
                        if(around <= 2){
                            setWallFromFloor(tile);
                        }
                    }else if(tile.block().isStatic() && around >= 7){
                        tile.setBlock(Blocks.air);
                    }
                }
            }
        }
    }

    private void cleanupSingles(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        boolean[] open = snapshotOpen(ctx);

        for(int x = 1; x < width - 1; x++){
            for(int y = 1; y < height - 1; y++){
                if(isProtected(ctx, x, y)) continue;

                Tile tile = ctx.tiles.getn(x, y);
                if(!tile.floor().hasSurface()) continue;

                boolean isOpen = open[indexOf(x, y, width)];
                int d4Open = countOpen4(open, width, height, x, y);

                if(isOpen){
                    if(d4Open <= 1){
                        setWallFromFloor(tile);
                    }
                }else if(tile.block().isStatic() && d4Open == 4){
                    tile.setBlock(Blocks.air);
                }
            }
        }
    }

    private void rebalanceMetalFloors(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isProtected(ctx, tile.x, tile.y)) continue;
            if(nearLiquid(ctx, tile.x, tile.y, 1)) continue;

            Block floor = tile.floor();
            if(floor == WHBlocksEnvironment.chromiteStone){
                int same = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.chromiteStone);
                if(same <= 2 || ctx.rand.chance(0.76f)){
                    tile.setFloor((same >= 4 && ctx.rand.chance(0.28f) ? WHBlocksEnvironment.manganeseStone : WHBlocksEnvironment.darkRock).asFloor());
                }
            }else if(floor == WHBlocksEnvironment.cobaltStone){
                int same = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.cobaltStone);
                if(same <= 3 || ctx.rand.chance(0.58f)){
                    tile.setFloor((ctx.rand.chance(0.66f) ? WHBlocksEnvironment.chromiteStone : WHBlocksEnvironment.darkRock).asFloor());
                }
            }else if(floor == Blocks.shale){
                int same = countNearFloor(ctx, tile.x, tile.y, Blocks.shale);
                if(same <= 4 && ctx.rand.chance(0.68f)){
                    tile.setFloor((ctx.rand.chance(0.72f) ? WHBlocksEnvironment.darkRock : Blocks.stone).asFloor());
                }
            }
        }
    }

    private void smoothFloorTransitions(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            Block[] floors = snapshotFloors(ctx);

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(isProtected(ctx, x, y)) continue;
                    if(nearLiquid(ctx, x, y, 1)) continue;

                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isBlendableFloor(floor)) continue;

                    int same = 0;
                    int bestCount = 0;
                    Block best = null;
                    Block[] candidates = new Block[8];
                    int[] counts = new int[8];
                    int unique = 0;

                    for(Point2 p : Geometry.d8){
                        Block near = floors[indexOf(x + p.x, y + p.y, width)];
                        if(!isBlendableFloor(near)) continue;
                        if(!isFloorCompatible(floor, near)) continue;

                        if(near == floor){
                            same++;
                        }

                        int slot = -1;
                        for(int n = 0; n < unique; n++){
                            if(candidates[n] == near){
                                slot = n;
                                break;
                            }
                        }

                        if(slot == -1){
                            slot = unique++;
                            candidates[slot] = near;
                        }

                        int count = ++counts[slot];
                        if(near != floor && count > bestCount){
                            bestCount = count;
                            best = near;
                        }
                    }

                    if(best == null) continue;

                    if(bestCount >= 5 && same <= 2){
                        tile.setFloor(best.asFloor());
                    }else if(bestCount >= 4 && same <= 1 && ctx.rand.chance(0.58f)){
                        tile.setFloor(best.asFloor());
                    }
                }
            }
        }
    }

    private boolean isProtected(GenContext ctx, int x, int y){
        if(ctx.spawnRoom != null){
            int radius = ctx.spawnRoom.radius + protectedRoomPadding;
            if(Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, radius)){
                return true;
            }
        }

        for(RoomAnchor enemy : ctx.enemyRooms){
            int radius = enemy.radius + 1;
            if(Mathf.within(x, y, enemy.x, enemy.y, radius)){
                return true;
            }
        }

        return false;
    }

    private Block[] snapshotFloors(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        Block[] floors = new Block[width * height];

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                floors[indexOf(x, y, width)] = ctx.tiles.getn(x, y).floor();
            }
        }

        return floors;
    }

    private boolean[] snapshotOpen(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        boolean[] open = new boolean[width * height];

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                open[indexOf(x, y, width)] = ctx.tiles.getn(x, y).block() == Blocks.air;
            }
        }

        return open;
    }

    private int countOpen4(boolean[] open, int width, int height, int x, int y){
        int result = 0;
        for(Point2 point : Geometry.d4){
            int wx = x + point.x;
            int wy = y + point.y;
            if(wx < 0 || wy < 0 || wx >= width || wy >= height) continue;
            if(open[indexOf(wx, wy, width)]){
                result++;
            }
        }
        return result;
    }

    private int countOpen8(boolean[] open, int width, int height, int x, int y){
        int result = 0;
        for(Point2 point : Geometry.d8){
            int wx = x + point.x;
            int wy = y + point.y;
            if(wx < 0 || wy < 0 || wx >= width || wy >= height) continue;
            if(open[indexOf(wx, wy, width)]){
                result++;
            }
        }
        return result;
    }

    private int countNearFloor(GenContext ctx, int x, int y, Block floor){
        int count = 0;
        for(Point2 point : Geometry.d8){
            Tile near = ctx.tiles.get(x + point.x, y + point.y);
            if(near != null && near.floor() == floor){
                count++;
            }
        }
        return count;
    }

    private int indexOf(int x, int y, int width){
        return x + y * width;
    }

    private boolean nearLiquid(GenContext ctx, int x, int y, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.floor().isLiquid){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBlendableFloor(Block floor){
        if(floor == null) return false;
        return floor.asFloor().hasSurface()
        && !floor.asFloor().isLiquid
        && floor.asFloor().attributes.get(Attribute.steam) == 0f
        && floorGroup(floor) != 0;
    }

    private boolean isFloorCompatible(Block a, Block b){
        int ga = floorGroup(a), gb = floorGroup(b);
        if(ga == 0 || gb == 0) return false;
        if(ga == gb) return true;

        return (ga == 1 && gb == 3) || (ga == 3 && gb == 1)
        || (ga == 3 && gb == 4) || (ga == 4 && gb == 3)
        || (ga == 2 && gb == 3) || (ga == 3 && gb == 2);
    }

    private int floorGroup(Block floor){
        if(floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.darksand){
            return 1;
        }

        if(floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters){
            return 2;
        }

        if(floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.carbonStone
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.shale
        || floor == Blocks.stone
        || floor == Blocks.craters
        || floor == Blocks.crystallineStone
        || floor == Blocks.crystalFloor
        || floor == Blocks.dacite
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters){
            return 3;
        }

        if(floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock){
            return 4;
        }

        if(floor == Blocks.redmat
        || floor == Blocks.redStone
        || floor == Blocks.denseRedStone
        || floor == Blocks.redIce){
            return 5;
        }

        return 0;
    }

    private void setWallFromFloor(Tile tile){
        Block wall = tile.floor().asFloor().wall;
        if(wall != null && wall != Blocks.air){
            tile.setBlock(wall);
        }
    }
}
