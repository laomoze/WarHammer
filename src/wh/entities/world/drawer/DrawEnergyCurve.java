package wh.entities.world.drawer;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

public class DrawEnergyCurve{
    private final Vec2 startOrb;
    private final Vec2 endOrb;
    private final Seq<Vec2> controlPoints = new Seq<>();
    public final Color color;
    private final long spawnTime;
    private static final float LIFESPAN = 600;

    public DrawEnergyCurve(Vec2 startOrb, Vec2 endOrb, Color color){
        this.startOrb = startOrb;
        this.endOrb = endOrb;
        this.color = color;
        this.spawnTime = Time.millis();

        int pointCount = 2 + Mathf.random(2);
        for(int i = 0; i < pointCount; i++){
            // 使用平方分布，让控制点更偏向 startOrb（能量团1）
            float raw = Mathf.random();
            float t = raw * raw * 0.8f + 0.1f;

            // 插值获取基础点
            Vec2 midPoint = startOrb.cpy().lerp(endOrb, t);

            // 计算线角度
            float lineAngle = Angles.angle(startOrb.x, startOrb.y, endOrb.x, endOrb.y);

            // 偏移概率控制
            float offsetAngle;
            float maxOffset = startOrb.dst(endOrb) * 0.25f;

            if(Mathf.chance(0.8f)){
                offsetAngle = lineAngle + 180f;
                maxOffset *= 2.0f;    // 下方偏移范围更大
            }else{
                // 上下垂直偏移
                offsetAngle = Mathf.chance(0.5f) ? lineAngle + 90f : lineAngle - 90f;
            }

            float randomOffset = Mathf.range(maxOffset);

            // 应用偏移
            midPoint.add(
            Angles.trnsx(offsetAngle, randomOffset),
            Angles.trnsy(offsetAngle, randomOffset)
            );

            // 添加控制点
            controlPoints.add(midPoint);
        }
    }

    public boolean isExpired(){

        return Time.millis() - spawnTime > LIFESPAN;
    }

    public float lifeProgress(){
        return Mathf.clamp((Time.time / spawnTime) % 1f);
    }

    public Seq<Vec2> getPoints(){
        Seq<Vec2> points = new Seq<>();
        points.add(startOrb);
        points.addAll(controlPoints);
        points.add(endOrb);
        return points;
    }
}


