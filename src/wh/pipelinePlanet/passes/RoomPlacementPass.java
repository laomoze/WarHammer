package wh.pipelinePlanet.passes;

import arc.math.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

/**
 * 中文说明：房间布置阶段：生成出生房、敌房与普通房。
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
        int cx = w / 2;
        int cy = h / 2;

        // Pull endpoints slightly inward to avoid very long needle-shaped layouts.
        float length = w / 3.1f;
        float angle = ctx.rand.random(360f);

        int spawnX = Mathf.clamp(Math.round(cx + Angles.trnsx(angle, length)), 8, w - 9);
        int spawnY = Mathf.clamp(Math.round(cy + Angles.trnsy(angle, length)), 8, h - 9);
        int endX = Mathf.clamp(Math.round(cx - Angles.trnsx(angle, length)), 8, w - 9);
        int endY = Mathf.clamp(Math.round(cy - Angles.trnsy(angle, length)), 8, h - 9);

        RoomAnchor spawn = new RoomAnchor(spawnX, spawnY, 15);
        RoomAnchor end = new RoomAnchor(endX, endY, 15);

        ctx.spawnRoom = spawn;
        ctx.allRooms.add(spawn);
        ctx.enemyRooms.add(end);
        ctx.allRooms.add(end);
    }
}
