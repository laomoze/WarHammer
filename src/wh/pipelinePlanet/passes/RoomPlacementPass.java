package wh.pipelinePlanet.passes;

import arc.math.*;
import mindustry.world.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

/**
 * 放置出生房间与敌方房间锚点。
 * 这里只做锚点计算，不负责挖地或连通。
 */
public class RoomPlacementPass implements GenPass{
    @Override
    public String name(){
        return "RoomPlacementPass";
    }

    @Override
    public void apply(GenContext ctx){
        ctx.allRooms.clear();
        ctx.enemyRooms.clear();

        int w = ctx.width();
        int h = ctx.height();
        int cx = w / 2, cy = h / 2;

        // 接近原版：在地图中心周围放置中立房间。
        float mapRadius = w / 2f / Mathf.sqrt3;
        float constraint = 1.3f;
        int minRooms = Math.max(0, ctx.cfg.minRooms);
        int maxRooms = Math.max(minRooms, ctx.cfg.maxRooms);
        int neutralRooms = ctx.rand.random(minRooms, maxRooms);

        for(int i = 0; i < neutralRooms; i++){
            float angle = ctx.rand.random(360f);
            float dist = ctx.rand.random(mapRadius / constraint);
            float rx = cx + Angles.trnsx(angle, dist);
            float ry = cy + Angles.trnsy(angle, dist);
            float maxrad = Math.max(10f, mapRadius - dist);
            int radius = Mathf.clamp((int)Math.min(ctx.rand.random(9f, maxrad / 2f), 30f), 8, 26);

            int x = Mathf.clamp(Math.round(rx), 8, w - 9);
            int y = Mathf.clamp(Math.round(ry), 8, h - 9);
            if(tooCloseToExisting(ctx, x, y, radius, 4f)) continue;

            ctx.allRooms.add(new RoomAnchor(x, y, radius));
        }

        RoomAnchor spawn = pickSpawn(ctx, cx, cy);
        ctx.spawnRoom = spawn;
        ctx.allRooms.add(spawn);

        int maxEnemies = Math.min(Math.max((int)(ctx.sector.threat * 4f * ctx.cfg.enemyRoomScale), 1), 2);
        int enemyCount = ctx.rand.random(1, maxEnemies);

        for(int i = 0; i < enemyCount; i++){
            float enemyOffset = ctx.rand.range(60f);
            float dx = spawn.x - cx;
            float dy = spawn.y - cy;
            int ex = Mathf.clamp(Math.round((dx * Mathf.cosDeg(180f + enemyOffset) - dy * Mathf.sinDeg(180f + enemyOffset)) + cx), 8, w - 9);
            int ey = Mathf.clamp(Math.round((dx * Mathf.sinDeg(180f + enemyOffset) + dy * Mathf.cosDeg(180f + enemyOffset)) + cy), 8, h - 9);
            int er = ctx.rand.random(8, 16);

            if(tooCloseToExisting(ctx, ex, ey, er, 5f)) continue;
            RoomAnchor enemy = new RoomAnchor(ex, ey, er);
            ctx.enemyRooms.add(enemy);
            ctx.allRooms.add(enemy);
        }

        if(ctx.enemyRooms.isEmpty()){
            float dx = spawn.x - cx;
            float dy = spawn.y - cy;
            int ex = Mathf.clamp(Math.round(cx - dx), 8, w - 9);
            int ey = Mathf.clamp(Math.round(cy - dy), 8, h - 9);
            RoomAnchor enemy = new RoomAnchor(ex, ey, 12);
            ctx.enemyRooms.add(enemy);
            ctx.allRooms.add(enemy);
        }
    }

    private RoomAnchor pickSpawn(GenContext ctx, int cx, int cy){
        int w = ctx.width(), h = ctx.height();
        int offset = ctx.rand.nextInt(360);
        float length = w / 2.55f - ctx.rand.random(13f, 23f);
        int angleStep = 5;

        for(int i = 0; i < 360; i += angleStep){
            int angle = offset + i;
            int sx = Mathf.clamp(Math.round(cx + Angles.trnsx(angle, length)), 8, w - 9);
            int sy = Mathf.clamp(Math.round(cy + Angles.trnsy(angle, length)), 8, h - 9);

            if(waterTilesAround(ctx, sx, sy, 5) <= 4 || i + angleStep >= 360){
                return new RoomAnchor(sx, sy, ctx.rand.random(8, 15));
            }
        }

        return new RoomAnchor(cx, cy, 12);
    }

    private int waterTilesAround(GenContext ctx, int x, int y, int radius){
        int water = 0;
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile tile = ctx.tiles.get(x + rx, y + ry);
                if(tile == null || tile.floor().isLiquid){
                    water++;
                }
            }
        }
        return water;
    }

    private boolean tooCloseToExisting(GenContext ctx, int x, int y, int radius, float padding){
        for(RoomAnchor room : ctx.allRooms){
            float min = room.radius + radius + padding;
            if(Mathf.within(x, y, room.x, room.y, min)){
                return true;
            }
        }
        return false;
    }
}
