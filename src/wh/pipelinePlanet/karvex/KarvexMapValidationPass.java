package wh.pipelinePlanet.karvex;

import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;
import wh.content.WHBlocksEnvironment;
import wh.pipelinePlanet.core.GenContext;
import wh.pipelinePlanet.core.GenPass;
import wh.pipelinePlanet.data.RoomAnchor;

import static mindustry.Vars.content;
import static mindustry.Vars.world;

/**
 * Final map sanity pass with room and wall cleanup.
 */
public class KarvexMapValidationPass implements GenPass{
    @Override
    public String name(){
        return "KarvexMapValidationPass";
    }

    @Override
    public void apply(GenContext ctx){
        ensureSpawnAnchor(ctx);
        keepCriticalRoomsPlayable(ctx);
        sanitizeOverlays(ctx);
        cleanupFragmentWalls(ctx, 2);
        cleanupFragmentWalls(ctx, 1);
    }

    private void ensureSpawnAnchor(GenContext ctx){
        if(ctx.spawnRoom != null) return;
        ctx.spawnRoom = new RoomAnchor(ctx.width() / 2, ctx.height() / 2, 12);
        ctx.allRooms.add(ctx.spawnRoom);
    }

    private void keepCriticalRoomsPlayable(GenContext ctx){
        clearRoom(ctx, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + 6);
        for(int i = 0; i < ctx.enemyRooms.size; i++){
            RoomAnchor enemy = ctx.enemyRooms.get(i);
            clearRoom(ctx, enemy.x, enemy.y, 8);
        }
    }

    private void clearRoom(GenContext ctx, int cx, int cy, int radius){
        int r2 = radius * radius;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null) continue;

                if(tile.floor().isLiquid || !tile.floor().hasSurface()){
                    tile.setFloor(findNearbyLand(ctx, tile.x, tile.y, 12).asFloor());
                }
                tile.setBlock(Blocks.air);
                if(tile.overlay().needsSurface && !tile.floor().hasSurface()){
                    tile.setOverlay(Blocks.air);
                }
            }
        }
    }

    private void sanitizeOverlays(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.overlay().needsSurface && !tile.floor().hasSurface()){
                tile.setOverlay(Blocks.air);
            }
            if(tile.floor().isLiquid && tile.block().solid){
                tile.setBlock(Blocks.air);
            }
        }
    }

    private void cleanupFragmentWalls(GenContext ctx, int iterations){
        int w = ctx.width();

        for(int iter = 0; iter < iterations; iter++){
            short[] next = new short[w * ctx.height()];
            for(Tile tile : ctx.tiles){
                next[tile.x + tile.y * w] = tile.block().id;
            }

            for(Tile tile : ctx.tiles){
                if(!tile.block().isStatic()) continue;
                if(isDarkTile(tile.x, tile.y)) continue;
                if(tile.floor().isLiquid || !tile.floor().hasSurface()){
                    next[tile.x + tile.y * w] = Blocks.air.id;
                    continue;
                }

                int n4 = staticNeighborCount4(ctx, tile.x, tile.y);
                int n8 = staticNeighborCount8(ctx, tile.x, tile.y);
                if(n4 <= 1 && n8 <= 2){
                    next[tile.x + tile.y * w] = Blocks.air.id;
                }
            }

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;
                if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
                if(isDarkTile(tile.x, tile.y)) continue;
                if(isNearRoom(ctx, tile.x, tile.y, 7f, 5f)) continue;

                int n4 = staticNeighborCount4(ctx, tile.x, tile.y);
                if(n4 >= 3){
                    Block wall = wallForFloor(tile.floor());
                    if(wall != Blocks.air){
                        next[tile.x + tile.y * w] = wall.id;
                    }
                }
            }

            for (Tile tile : ctx.tiles) {
                Block block = content.block(next[tile.x + tile.y * w]);
                tile.setBlock(block == null ? Blocks.air : block);
            }
        }
    }

    private int staticNeighborCount4(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d4){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.block().isStatic()){
                count++;
            }
        }
        return count;
    }

    private int staticNeighborCount8(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.block().isStatic()){
                count++;
            }
        }
        return count;
    }

    private boolean isNearRoom(GenContext ctx, int x, int y, float spawnExtra, float enemyExtra){
        if(ctx.spawnRoom != null && Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + spawnExtra)){
            return true;
        }
        for(int i = 0; i < ctx.enemyRooms.size; i++){
            RoomAnchor room = ctx.enemyRooms.get(i);
            if(Mathf.within(x, y, room.x, room.y, room.radius + enemyExtra)){
                return true;
            }
        }
        return false;
    }

    private boolean isDarkTile(int x, int y){
        if(world == null) return false;
        if(world.getDarkness(x, y) > 0f) return true;
        for(int i = 0; i < 4; i++){
            if(world.getDarkness(x + Geometry.d4[i].x, y + Geometry.d4[i].y) > 0f){
                return true;
            }
        }
        return false;
    }

    private Block wallForFloor(Block floor){
        if(floor == null || floor.asFloor() == null) return WHBlocksEnvironment.darkRockWall;
        Block wall = floor.asFloor().wall;
        if(wall == null || wall == Blocks.air){
            wall = WHBlocksEnvironment.darkRockWall;
        }
        return wall == null ? Blocks.air : wall;
    }

    private Block findNearbyLand(GenContext ctx, int x, int y, int radius){
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
        return WHBlocksEnvironment.defaultMineralFloor();
    }
}
