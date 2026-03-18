package wh.pipelinePlanet.passes;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.ai.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

/**
 * 使用 A* 路径加笔刷挖掘实现接近原版的房间连通。
 * 避免出现过度圆润的“团块走廊”观感。
 */
public class ConnectivityPass implements GenPass{
    @Override
    public String name(){
        return "ConnectivityPass";
    }

    @Override
    public void apply(GenContext ctx){
        if(ctx.spawnRoom == null) return;
        if(ctx.allRooms.isEmpty()) return;

        for(RoomAnchor room : ctx.allRooms){
            erase(ctx, room.x, room.y, room.radius);
        }

        for(RoomAnchor room : ctx.allRooms){
            if(room != ctx.spawnRoom){
                join(ctx, ctx.spawnRoom.x, ctx.spawnRoom.y, room.x, room.y);
            }
        }
    }

    private void join(GenContext ctx, int x1, int y1, int x2, int y2){
        float maxd = Mathf.dst(ctx.width() / 2f, ctx.height() / 2f);
        // Wider roads for clearer logistics lanes on large sectors.
        int stroke = Math.max(11, ctx.width() / 48);
        Seq<Tile> path = Astar.pathfind(x1, y1, x2, y2,
        tile -> (tile.solid() ? 300f : 0f) + maxd - tile.dst(ctx.width() / 2f, ctx.height() / 2f) / 10f,
        Astar.manhattan,
        tile -> true
        );
        brush(ctx, path, stroke);
    }

    private void brush(GenContext ctx, Seq<Tile> path, int radius){
        for(Tile tile : path){
            erase(ctx, tile.x, tile.y, radius);
        }
    }

    private void erase(GenContext ctx, int cx, int cy, int radius){
        for(int x = -radius; x <= radius; x++){
            for(int y = -radius; y <= radius; y++){
                int wx = cx + x, wy = cy + y;
                if(!ctx.tiles.in(wx, wy)) continue;
                if(!Mathf.within(x, y, radius)) continue;
                ctx.tiles.getn(wx, wy).setBlock(Blocks.air);
            }
        }
    }
}
