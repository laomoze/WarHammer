package wh.entities.bullet.laser;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.util.*;

/** @author EyeOfDarkness */
public class AcceleratingLaserBulletType extends BulletType {
    public float maxLength = 1000f;
    public float laserSpeed = 15f;
    public float accel = 25f;
    public float width = 12f, collisionWidth = 8f;
    public float fadeTime = 60f;
    public float fadeInTime = 8f;
    public float oscOffset = 1.4f, oscScl = 1.1f;
    public float pierceAmount = 4f;
    public boolean fastUpdateLength = true;
    public Color[] colors = {Color.valueOf("ec745855"), Color.valueOf("ec7458aa"), Color.valueOf("ff9c5a"), Color.white};
    public Boolf2<Bullet, Building> buildingInsulator = (b, building) -> building.block.absorbLasers || building.health > (damage * buildingDamageMultiplier) / 2f;
    public Boolf2<Bullet, Unit> unitInsulator = (b, unit) -> unit.health > damage / 2f && unit.hitSize > width;


    public AcceleratingLaserBulletType(float damage){
        this.damage = damage;
        speed = 0f;
        despawnEffect = Fx.none;
        collides = false;
        pierce = true;
        impact = true;
        keepVelocity = false;
        hittable = false;
        absorbable = true;
    }

    @Override
    public float estimateDPS(){
        return damage * (lifetime / 2f) / 5f * 3f;
    }

    @Override
    public float continuousDamage(){
        return damage / 5f * 60f;
    }

    @Override
    public float calculateRange(){
        return maxRange > 0 ? maxRange : maxLength / 1.5f;
    }

    @Override
    public void init(){
        super.init();
        drawSize = maxLength * 2f;
    }

    @Override
    public void draw(Bullet b){
        float fadeIn = fadeInTime <= 0f ? 1f : Mathf.clamp(b.time / fadeInTime);
        float fade = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f) * fadeIn;
        float tipHeight = width / 2f;

        Lines.lineAngle(b.x, b.y, b.rotation(), b.fdata);
        for(int i = 0; i < colors.length; i++){
            float f = ((float)(colors.length - i) / colors.length);
            float w = f * (width + Mathf.absin(Time.time + (i * oscOffset), oscScl, width / 4)) * fade;

            Tmp.v2.trns(b.rotation(), b.fdata - tipHeight).add(b);
            Tmp.v1.trns(b.rotation(), width * 2f).add(Tmp.v2);
            Draw.color(colors[i]);
            Fill.circle(b.x, b.y, w / 2f);
            Lines.stroke(w);
            Lines.line(b.x, b.y, Tmp.v2.x, Tmp.v2.y, false);
            for(int s : Mathf.signs){
                Tmp.v3.trns(b.rotation(), w * -0.7f, w * s);
                Fill.tri(Tmp.v2.x, Tmp.v2.y, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x + Tmp.v3.x, Tmp.v2.y + Tmp.v3.y);
            }
        }
        Tmp.v2.trns(b.rotation(), b.fdata + tipHeight).add(b);
        Drawf.light(b.x, b.y, Tmp.v2.x, Tmp.v2.y, width * 2f, colors[0], 0.5f);
        Draw.reset();
    }

    @Override
    public void drawLight(Bullet b){

    }

    @Override
    public void init(Bullet b){
        super.init(b);
        b.data = new LaserData();
    }

       /**
     * 更新激光子弹的状态
     *
     * @param b 激光子弹对象
     */
    @Override
    public void update(Bullet b){
        // 检查是否达到 5 秒的计时器
        boolean timer = b.timer(0, 5f);

        // 如果子弹数据是 LaserData 类型
        if(b.data instanceof LaserData){
            LaserData vec = (LaserData)b.data;
            // 如果重启时间大于等于 5 秒
            if(vec.restartTime >= 5f){
                // 如果加速度大于 0.01
                if(accel > 0.01f){
                    // 计算并限制速度
                    vec.velocity = Mathf.clamp((vec.velocityTime / accel) + vec.velocity, 0f, laserSpeed);
                    // 更新飞行距离并限制在最大长度内
                    b.fdata = Mathf.clamp(b.fdata + (vec.velocity * Time.delta), 0f, maxLength);
                    // 更新速度时间
                    vec.velocityTime += Time.delta;
                // 如果达到计时器时间
                }else if(timer){
                    // 设置飞行距离为最大长度
                    b.fdata = maxLength;
                }
            // 如果重启时间小于 5 秒
            }else{
                // 增加重启时间
                vec.restartTime += Time.delta;
                // 如果启用快速更新长度并且目标不为空
                if(fastUpdateLength && vec.target != null){
                    // 平滑更新穿透偏移量
                    vec.pierceOffsetSmooth = Mathf.lerpDelta(vec.pierceOffsetSmooth, vec.pierceOffset, 0.2f);
                    // 计算临时向量
                    Tmp.v2.trns(b.rotation(), maxLength * 1.5f).add(b);
                    // 计算距离
                    float dst = Intersector.distanceLinePoint(b.x, b.y, Tmp.v2.x, Tmp.v2.y, vec.target.getX(), vec.target.getY());
                    // 更新飞行距离
                    b.fdata = ((b.dst(vec.target) - vec.targetSize) + dst) + pierceAmount + (vec.pierceOffsetSmooth * vec.targetSize);
                }
            }
        }

        // 如果达到计时器时间
        if(timer){
            // 判断是否有穿透限制
            boolean p = pierceCap > 0;
            // 计算临时向量
            Tmp.v1.trns(b.rotation(), b.fdata).add(b);
            // 如果子弹数据是 LaserData 类型
            if(b.data instanceof LaserData data){
                // 如果有穿透限制
                if(p){
                    // 重置穿透得分和穿透偏移量
                    data.pierceScore = 0f;
                    data.pierceOffset = 0f;
                }

                // 碰撞检测
                WHUtils.collideLineRawEnemyRatio(b.team, b.x, b.y, Tmp.v1.x, Tmp.v1.y, collisionWidth, (building, ratio, direct) -> {
                    // 判断是否绝缘
                    boolean h = buildingInsulator.get(b, building);
                    if(direct){
                        if(h){
                            // 如果有穿透限制，增加穿透得分
                            if(p) data.pierceScore += building.block.size * (building.block.absorbLasers ? 3f : 1f) * ratio;
                            // 如果没有穿透限制或穿透得分超过限制
                            if(!p || data.pierceScore >= pierceCap){
                                // 计算临时向量
                                Tmp.v2.trns(b.rotation(), maxLength * 1.5f).add(b);
                                // 计算距离
                                float dst = Intersector.distanceLinePoint(b.x, b.y, Tmp.v2.x, Tmp.v2.y, building.x, building.y);
                                // 重置速度、重启时间和速度时间
                                data.velocity = 0f;
                                data.restartTime = 0f;
                                data.velocityTime = 0f;
                                // 计算穿透偏移量
                                data.pierceOffset = 1f - Mathf.clamp(data.pierceScore - pierceCap);
                                // 如果启用快速更新长度
                                if(fastUpdateLength){
                                    // 如果目标不是当前建筑，平滑更新穿透偏移量
                                    if(building != data.target) data.pierceOffsetSmooth = data.pierceOffset;
                                    // 设置目标为当前建筑
                                    data.target = building;
                                    // 计算目标大小
                                    data.targetSize = building.block.size * Vars.tilesize / 2f;
                                }
                                // 更新飞行距离
                                b.fdata = ((b.dst(building) - (building.block.size * Vars.tilesize / 2f)) + dst) + pierceAmount + (data.pierceOffsetSmooth * data.targetSize);
                            }
                        }
                        // 对建筑造成伤害
                        building.damage(damage * buildingDamageMultiplier * ratio);
                    }
                    // 返回是否绝缘
                    return !p ? h : data.pierceScore >= pierceCap;
                }, (unit, ratio) -> {
                    // 判断是否绝缘
                    boolean h = unitInsulator.get(b, unit);
                    if(h){
                        // 如果有穿透限制，增加穿透得分
                        if(p) data.pierceScore += (((unit.hitSize / Vars.tilesize) / 2f) + (unit.health / 4000f)) * ratio;
                        // 如果没有穿透限制或穿透得分超过限制
                        if(!p || data.pierceScore >= pierceCap){
                            // 计算临时向量
                            Tmp.v2.trns(b.rotation(), maxLength * 1.5f).add(b);
                            // 计算距离
                            float dst = Intersector.distanceLinePoint(b.x, b.y, Tmp.v2.x, Tmp.v2.y, unit.x, unit.y);
                            // 重置速度、重启时间和速度时间
                            data.velocity = 0f;
                            data.restartTime = 0f;
                            data.velocityTime = 0f;
                            // 计算穿透偏移量
                            data.pierceOffset = 1f - Mathf.clamp(data.pierceScore - pierceCap);
                            // 如果启用快速更新长度
                            if(fastUpdateLength){
                                // 如果目标不是当前单位，平滑更新穿透偏移量
                                if(unit != data.target) data.pierceOffsetSmooth = data.pierceOffset;
                                // 设置目标为当前单位
                                data.target = unit;
                                // 计算目标大小
                                data.targetSize = unit.hitSize / 2f;
                            }
                            // 更新飞行距离
                            b.fdata = ((b.dst(unit) - (unit.hitSize / 2f)) + dst) + pierceAmount + (data.pierceOffsetSmooth * data.targetSize);
                        }
                    }
                    // 对单位造成伤害
                    //hitEntity(b, unit, unit.health);
                    hitEntityAlt(b, unit, b.damage * ratio);
                    // 返回是否绝缘
                    return !p ? h : data.pierceScore >= pierceCap;
                }, (ex, ey) -> hit(b, ex, ey));
            }
        }
    }

    void hitEntityAlt(Bullet b, Unit unit, float damage){
        unit.damage(damage);
        Tmp.v3.set(unit).sub(b).nor().scl(knockback * 80f);
        if(impact) Tmp.v3.setAngle(b.rotation() + (knockback < 0 ? 180f : 0f));
        unit.impulse(Tmp.v3);
        unit.apply(status, statusDuration);
    }

    public static class LaserData{
        public float lastLength, lightningTime, velocity, velocityTime, targetSize, pierceOffset, pierceOffsetSmooth, pierceScore, restartTime = 5f;
        public Position target;
    }
}
