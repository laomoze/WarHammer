package wh.entities.abilities;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.content.*;
import wh.graphics.*;

import static wh.core.WarHammerMod.name;

/** 改造版弧形护盾，可吸收或反射子弹，并可阻挡近身敌军。 */
public class PcShieldArcAbility extends ShieldArcAbility{
    private static Unit paramUnit;
    private static PcShieldArcAbility paramField;
    private static final Vec2 paramPos = new Vec2();

    private static final Cons<Bullet> shieldConsumer = b -> {
        if(b.team != paramUnit.team && b.type.absorbable && paramField.data > 0 &&
        !b.within(paramPos, paramField.radius - paramField.width) &&
        (Tmp.v1.set(b).add(b.vel).within(paramPos, paramField.radius + paramField.width / 2f)
        || b.within(paramPos, paramField.radius + paramField.width)) &&
        (Angles.within(paramPos.angleTo(b), paramUnit.rotation + paramField.angleOffset, paramField.angle / 2f))){

            if(paramField.chanceDeflect > 0f && b.vel.len() >= 0.1f
            && b.type.reflectable && Mathf.chance(paramField.chanceDeflect)
            && b.damage < paramField.max && b.damage < paramUnit.maxHealth * 0.5f){

                // 成功偏转时先播放一次弹开音效。
                paramField.deflectSound.at(paramPos, Mathf.random(0.9f, 1.1f));

                // 把子弹回退到碰撞前的位置，再做反弹，避免留在盾内重复判定。
                b.trns(-b.vel.x, -b.vel.y);

                float penX = Math.abs(paramPos.x - b.x), penY = Math.abs(paramPos.y - b.y);

                if(penX > penY){
                    b.vel.x *= -1;
                }else{
                    b.vel.y *= -1;
                }

                b.owner = paramUnit;
                b.team = paramUnit.team;
                b.time += 1f;

            }else{
                b.absorb();
                Fx.absorb.at(b);
            }

            // 护盾值不够时直接击破，并扣掉额外冷却惩罚。
            if(paramField.data <= b.damage()){
                paramField.data = -1;
                paramField.data -= paramField.cooldown * paramField.regen;

                WHFx.arcShieldBreak.at(paramPos.x, paramPos.y, 0, paramField.color == null ? paramUnit.type.shieldColor(paramUnit) : paramField.color, paramUnit);
            }

            // 统一使用子弹自己的护盾伤害计算。
            paramField.data -= b.type.shieldDamage(b);
            paramField.alpha = 1f;
        }
    };


    protected static final Cons<Unit> unitConsumer = unit -> {
        // 核心单位不参与这里的近身阻挡。
        if(!unit.isMissile() && paramField.data > 0 && unit.targetable(paramUnit.team) &&
        !unit.within(paramPos, paramField.radius - paramField.width) &&
        (Tmp.v1.set(unit).add(unit.vel).within(paramPos, paramField.radius + paramField.width / 2f) || unit.within(paramPos, paramField.radius + paramField.width)) &&
        Angles.within(paramPos.angleTo(unit), paramUnit.rotation + paramField.angleOffset, paramField.angle / 2f)){
            if(paramField.pushUnits && !((unit.isFlying() && !paramUnit.isFlying()) || (!unit.isFlying() && paramUnit.isFlying()))){

                float reach = paramField.radius + paramField.width;
                float overlapDst = reach - unit.dst(paramPos.x, paramPos.y);

                if(overlapDst > 0){
                    // 进入护盾扇区的敌军会被强制减速/停住。
                    unit.vel.setZero();
                    // 给一个朝外的位移，把敌军从护盾边缘顶出去。
                    unit.move(Tmp.v1.set(unit).sub(paramUnit).setLength(overlapDst + 0.01f));

                    if(Mathf.chanceDelta(0.5f * Time.delta)){
                        Fx.circleColorSpark.at(unit.x, unit.y, paramUnit.team.color);
                    }
                }
            }
        }
    };


    protected float widthScale, alpha;

    @Override
    public void update(Unit unit){

        if(data < max){
            data += Time.delta * regen;
        }

        boolean active = data > 0 && (unit.isShooting || !whenShooting);
        alpha = Math.max(alpha - Time.delta / 10f, 0f);

        if(active){
            widthScale = Mathf.lerpDelta(widthScale, 1f, 0.06f);
            paramUnit = unit;
            paramField = this;
            paramPos.set(x, y).rotate(unit.rotation - 90f).add(unit);

            float reach = radius + width;
            Groups.bullet.intersect(paramPos.x - reach, paramPos.y - reach, reach * 2f, reach * 2f, shieldConsumer);
            Units.nearbyEnemies(paramUnit.team, paramPos.x - reach, paramPos.y - reach, reach * 2f, reach * 2f, unitConsumer);
        }else{
            widthScale = Mathf.lerpDelta(widthScale, 0f, 0.15f);
        }
    }

    @Override
    public void draw(Unit unit){
        var pos = paramPos.set(x, y).rotate(unit.rotation - 90f).add(unit);

        if(widthScale > 0.001f){
            Draw.z(Layer.shields);

            Draw.color(color == null ? unit.type.shieldColor(unit) : color, Color.white, Mathf.clamp(alpha));
            if(!Vars.renderer.animateShields){
                Draw.alpha(0.4f);
            }

            if(region != null){
                Vec2 rp = offsetRegion ? pos : Tmp.v1.set(unit);
                Draw.yscl = widthScale;
                Draw.rect(region, rp.x, rp.y, unit.rotation - 90);
                Draw.yscl = 1f;
            }

            if(drawArc){
                Lines.stroke(width * widthScale);
                Lines.arc(pos.x, pos.y, radius, angle / 360f, unit.rotation + angleOffset - angle / 2f);
            }
        }
        if(data < 0){
            Draw.z(Layer.flyingUnitLow);
            Draw.color(color == null ? unit.type.shieldColor(unit) : color, Color.white, Mathf.clamp(alpha));
            Lines.stroke(6);
            Draw.alpha(0.3f);
            Drawn.arcProcess(pos.x, pos.y, radius, angle / 360f,
            unit.rotation + angleOffset - angle / 2f, (int)angle, 1 - (-data / (cooldown * regen)));
        }
        Draw.reset();
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability." + name("better-arc-field-ability"));
    }
}
