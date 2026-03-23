package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

import static mindustry.Vars.*;

/**
 * Final map sanity pass with trimDark border and wall cleanup.
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
        addEdgeFloorNoise(ctx);
        trimDark(ctx);
        expandDarkRimWalls(ctx);
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

            for(Tile tile : ctx.tiles){
                Block block = content.block(next[tile.x + tile.y * w]);
                tile.setBlock(block == null ? Blocks.air : block);
            }
        }
    }

    private void trimDark(GenContext ctx){
        if(world == null) return;

        for(Tile tile : ctx.tiles){
            boolean any = world.getDarkness(tile.x, tile.y) > 0f;
            for(int i = 0; i < 4 && !any; i++){
                any = world.getDarkness(tile.x + Geometry.d4[i].x, tile.y + Geometry.d4[i].y) > 0f;
            }

            if(any){
                Block wall = wallForFloor(tile.floor());
                if(wall != Blocks.air){
                    tile.setBlock(wall);
                }
            }
        }
    }

    private void expandDarkRimWalls(GenContext ctx){
        if(world == null) return;

        int w = ctx.width();
        short[] next = new short[w * ctx.height()];
        for(Tile tile : ctx.tiles){
            next[tile.x + tile.y * w] = tile.block().id;
        }

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 8f, 6f)) continue;

            int dark1 = darknessNeighborCount(tile.x, tile.y, 1);
            int dark2 = darknessNeighborCount(tile.x, tile.y, 2);
            if(dark1 <= 0 && dark2 <= 0) continue;

            int nearWalls = staticNeighborCount8(ctx, tile.x, tile.y);
            float chance = 0.04f + dark1 * 0.17f + dark2 * 0.03f + nearWalls * 0.03f;
            if(!ctx.rand.chance(Mathf.clamp(chance, 0f, 0.56f))) continue;

            Block wall = wallForFloor(tile.floor());
            if(wall != Blocks.air){
                next[tile.x + tile.y * w] = wall.id;
            }
        }

        for(Tile tile : ctx.tiles){
            Block block = content.block(next[tile.x + tile.y * w]);
            tile.setBlock(block == null ? Blocks.air : block);
        }
    }

    private void addEdgeFloorNoise(GenContext ctx){
        if(world == null) return;
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            next[tile.x + tile.y * w] = tile.floor().id;
        }

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(tile.floor().isLiquid || !tile.floor().hasSurface()) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 11f, 8f)) continue;
            if(world.getDarkness(tile.x, tile.y) > 0f) continue;

            int dark1 = darknessNeighborCount(tile.x, tile.y, 1);
            int dark2 = darknessNeighborCount(tile.x, tile.y, 2);
            if(dark1 <= 0) continue;
            if(dark2 > 8) continue;

            float edgeStrength = Mathf.clamp(dark1 * 0.28f + dark2 * 0.08f);
            float n1 = rimNoise(ctx, ctx.seed + 1701, tile.x + 31f, tile.y - 43f, 2, 0.66, 19f);
            float n2 = rimNoise(ctx, ctx.seed + 1709, tile.x - 71f, tile.y + 27f, 1, 1f, 8.5f);
            float n3 = rimNoise(ctx, ctx.seed + 1723, tile.x + 17f, tile.y + 13f, 1, 1f, 42f);
            float field = n1 * 0.56f + n2 * 0.34f + n3 * 0.18f;
            float chance = Mathf.clamp(0.04f + edgeStrength * 0.24f + Math.abs(field) * 0.10f, 0f, 0.28f);
            if(!ctx.rand.chance(chance)) continue;

            int idx = tile.x + tile.y * w;
            if(field > 0.82f){
                next[idx] = WHBlocksEnvironment.mineralSand.id;
            }else if(field > 0.62f){
                next[idx] = WHBlocksEnvironment.mineralSandstone.id;
            }else if(field < -0.82f){
                next[idx] = WHBlocksEnvironment.trachyte.id;
            }else if(field < -0.62f){
                next[idx] = WHBlocksEnvironment.mineralSandstone.id;
            }else if(field > 0.30f && ctx.rand.chance(0.55f)){
                next[idx] = WHBlocksEnvironment.mineralSand.id;
            }
        }

        for(Tile tile : ctx.tiles){
            Block floor = content.block(next[tile.x + tile.y * w]);
            if(floor != null && floor.asFloor() != null){
                tile.setFloor(floor.asFloor());
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

    private int darknessNeighborCount(int x, int y, int radius){
        if(world == null) return 0;
        int count = 0;
        int r2 = radius * radius;

        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox == 0 && oy == 0) continue;
                if(ox * ox + oy * oy > r2) continue;
                if(world.getDarkness(x + ox, y + oy) > 0f){
                    count++;
                }
            }
        }

        return count;
    }

    private float rimNoise(GenContext ctx, int seed, float x, float y, double octaves, double falloff, double scl){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z);
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
