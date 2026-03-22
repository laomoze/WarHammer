package wh.pipelinePlanet.passes;

import arc.math.*;
import arc.struct.*;
import mindustry.ai.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

/**
 * 中文说明：连通阶段：打通出生与敌点，保证路径可达。
 */
public class ConnectivityPass implements GenPass{
    @Override
    public String name(){
        return "ConnectivityPass";
    }

    @Override
    public void apply(GenContext ctx){
        if(ctx.spawnRoom == null) return;
        if(ctx.enemyRooms.isEmpty()) return;

        RoomAnchor spawn = ctx.spawnRoom;
        RoomAnchor end = ctx.enemyRooms.first();
        float maxd = Mathf.dst(ctx.width() / 2f, ctx.height() / 2f);

        // Erekir-style single trunk with broad combat spaces.
        erase(ctx, spawn.x, spawn.y, 24);
        Seq<Tile> path = Astar.pathfind(spawn.x, spawn.y, end.x, end.y,
        tile -> (tile.solid() ? 300f : 0f) + maxd - tile.dst(ctx.width() / 2f, ctx.height() / 2f) / 10f,
        Astar.manhattan,
        tile -> true
        );
        brush(ctx, path, 14);
        carveBasins(ctx, path);
        carveOpenRooms(ctx, path);
        erase(ctx, end.x, end.y, 24);
    }

    private void brush(GenContext ctx, Seq<Tile> path, int radius){
        for(Tile tile : path){
            erase(ctx, tile.x, tile.y, radius);
        }
    }

    private void carveBasins(GenContext ctx, Seq<Tile> path){
        if(path == null || path.isEmpty()) return;

        int segments = 5;
        for(int i = 1; i <= segments; i++){
            int index = Mathf.clamp((int)((path.size - 1f) * (i / (segments + 1f))), 0, path.size - 1);
            Tile pivot = path.get(index);
            int radius = ctx.rand.random(20, 34);
            erase(ctx, pivot.x, pivot.y, radius);
        }
    }

    private void carveOpenRooms(GenContext ctx, Seq<Tile> path){
        if(path == null || path.size < 12) return;

        int rooms = 2;
        for(int i = 0; i < rooms; i++){
            int idx = Mathf.clamp((int)((path.size - 1f) * (0.25f + i * 0.45f)), 0, path.size - 2);
            Tile anchor = path.get(idx);
            Tile next = path.get(idx + 1);

            float dx = next.x - anchor.x;
            float dy = next.y - anchor.y;
            float len = Math.max(1f, Mathf.dst(0f, 0f, dx, dy));
            float nx = -dy / len;
            float ny = dx / len;

            int offset = ctx.rand.random(24, 42) * (ctx.rand.chance(0.5f) ? 1 : -1);
            int rx = Mathf.clamp(Math.round(anchor.x + nx * offset), 16, ctx.width() - 17);
            int ry = Mathf.clamp(Math.round(anchor.y + ny * offset), 16, ctx.height() - 17);

            int corridorRadius = ctx.rand.random(7, 10);
            int roomRadius = ctx.rand.random(24, 38);
            carveLine(ctx, anchor.x, anchor.y, rx, ry, corridorRadius);
            erase(ctx, rx, ry, roomRadius);
        }
    }

    private void carveLine(GenContext ctx, int x1, int y1, int x2, int y2, int radius){
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if(steps <= 0){
            erase(ctx, x1, y1, radius);
            return;
        }

        for(int i = 0; i <= steps; i++){
            float t = i / (float)steps;
            int x = Math.round(Mathf.lerp(x1, x2, t));
            int y = Math.round(Mathf.lerp(y1, y2, t));
            erase(ctx, x, y, radius);
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
