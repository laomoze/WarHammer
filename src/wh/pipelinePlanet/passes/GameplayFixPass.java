package wh.pipelinePlanet.passes;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

/**
 * 可玩性约束：
 * - 保持出生区可建造且开阔
 * - 用 spawn 覆层标记敌方锚点
 */
public class GameplayFixPass implements GenPass{
    @Override
    public String name(){
        return "GameplayFixPass";
    }

    @Override
    public void apply(GenContext ctx){
        if(ctx.spawnRoom == null) return;

        int clearRadius = ctx.spawnRoom.radius + 5;
        for(int x = -clearRadius; x <= clearRadius; x++){
            for(int y = -clearRadius; y <= clearRadius; y++){
                int wx = ctx.spawnRoom.x + x;
                int wy = ctx.spawnRoom.y + y;
                if(!ctx.tiles.in(wx, wy)) continue;

                float edgeNoise = Simplex.noise2d(ctx.seed + 401, 2, 0.58f, 1f / 8f, wx, wy);
                float localRadius = clearRadius + edgeNoise * 1.35f;
                if(x * x + y * y > localRadius * localRadius) continue;

                Tile tile = ctx.tiles.getn(wx, wy);
                tile.setBlock(Blocks.air);
                tile.setOverlay(Blocks.air);

                // 保证出生区域不为水面，确保前期建造空间稳定。
                if(tile.floor().isLiquid){
                    tile.setFloor(findNearbyLandFloor(ctx, wx, wy, 10).asFloor());
                }
            }
        }

        // 硬性保证：出生区内圈绝不保留液体地面。
        int hardRadius = ctx.spawnRoom.radius + 3;
        int hardRadius2 = hardRadius * hardRadius;
        for(int x = -hardRadius; x <= hardRadius; x++){
            for(int y = -hardRadius; y <= hardRadius; y++){
                if(x * x + y * y > hardRadius2) continue;
                int wx = ctx.spawnRoom.x + x;
                int wy = ctx.spawnRoom.y + y;
                Tile tile = ctx.tiles.get(wx, wy);
                if(tile != null && tile.floor().isLiquid){
                    tile.setFloor(findNearbyLandFloor(ctx, wx, wy, 12).asFloor());
                }
            }
        }

        for(RoomAnchor enemy : ctx.enemyRooms){
            if(ctx.tiles.in(enemy.x, enemy.y)){
                ctx.tiles.getn(enemy.x, enemy.y).setOverlay(Blocks.spawn);
            }
        }
    }

    private Block findNearbyLandFloor(GenContext ctx, int x, int y, int search){
        for(int r = 1; r <= search; r++){
            for(Point2 p : Geometry.d8){
                int wx = x + p.x * r;
                int wy = y + p.y * r;
                Tile tile = ctx.tiles.get(wx, wy);
                if(tile == null) continue;
                if(tile.floor().hasSurface() && !tile.floor().isLiquid){
                    return tile.floor();
                }
            }
        }
        return Blocks.stone;
    }
}
