package wh.entities.bullet.laser;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.content.*;
import wh.graphics.*;
import wh.util.*;

import static mindustry.Vars.*;

public class ChargePointLaser extends PointLaserBulletType{
    private final Color tmpColor = new Color();
    public float oscScl = 2f, oscMag = 0.3f;
    public float width = 7f, length = 400, inaccuracy = 2.2f;
    public Color from = Pal.heal.cpy().lerp(Pal.coalBlack, 0.1f), to = Pal.heal.cpy().lerp(Color.white, 0.12f);
    public float damageInterval = 5f;
    public float chargeReload = 300;
    public float lerpReload = 10f;
    public float maxDamageMultiplier = 5f;
    public boolean DrawLighting = false;
    public float curveInterval = 15f;
    public boolean DrawCurve = true;

    public ChargePointLaser(){

    }

    public boolean charged(ChargePointLaserBullet b){
        return b.chargeTimer > chargeReload;
    }

    public Color getColor(ChargePointLaserBullet b){
        return tmpColor.set(from).lerp(to, warmup(b));
    }

    public float warmup(ChargePointLaserBullet b){
        return Mathf.curve(b.chargeTimer, chargeReload - lerpReload / 2f, chargeReload + lerpReload / 2f);
    }

    @Override
    public void init(Bullet c){
        super.init(c);
        range = length;
        if(!(c instanceof ChargePointLaserBullet b)) return;
        b.splash = splashDamage;
    }

    @Override
    public float continuousDamage(){
        return (damage + splashDamage) / damageInterval * 60f;
    }


    @Override
    public void updateTrail(Bullet c){
        if(!(c instanceof ChargePointLaserBullet b)) return;
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new Trail(trailLength);
            }
            b.trail.length = trailLength;
            b.trail.update(b.curveEnd.x, b.curveEnd.y, b.fslope() * (1f - (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
        }
    }

    @Override
    public void updateTrailEffects(Bullet c){
        if(!(c instanceof ChargePointLaserBullet b)) return;
        if(trailChance > 0){
            if(Mathf.chanceDelta(trailChance)){
                trailEffect.at(b.curveEnd.x, b.curveEnd.y, trailRotation ? b.angleTo(b.aimX, b.aimY) : (trailParam * b.fslope()), trailColor);
            }
        }

        if(trailInterval > 0f){
            if(b.timer(0, trailInterval)){
                trailEffect.at(b.curveEnd.x, b.curveEnd.y, trailRotation ? b.angleTo(b.aimX, b.aimY) : (trailParam * b.fslope()), trailColor);
            }
        }
    }

    @Override
    public void updateBulletInterval(Bullet c){
        if(!(c instanceof ChargePointLaserBullet b)) return;
        if(intervalBullet != null && b.time >= intervalDelay && b.timer.get(2, bulletInterval)){
            float ang = b.rotation();
            for(int i = 0; i < intervalBullets; i++){
                intervalBullet.create(b, b.curveEnd.x, b.curveEnd.y, ang + Mathf.range(intervalRandomSpread) + intervalAngle + ((i - (intervalBullets - 1f) / 2f) * intervalSpread));
            }
        }
    }

    @Override
    public void update(Bullet c){
        if(!(c instanceof ChargePointLaserBullet b)) return;
        updateTrail(b);
        updateTrailEffects(b);
        updateBulletInterval(b);
        b.chargeTimer = Math.min(b.chargeTimer, chargeReload + lerpReload / 2f);


        b.curveStart.set(b.x, b.y);
        float fin = b.dst(b.aimX, b.aimY) / length;
        float OffectX = Mathf.cos(Time.time, 20, 1) * Mathf.cos(Time.time, 30, 1) * inaccuracy * tilesize * fin;
        float OffectY = Mathf.sin(Time.time, 20, 1) * Mathf.sin(Time.time, 30, 1) * inaccuracy * tilesize * fin;
        float endX = b.aimX + OffectX;
        float endY = b.aimY + OffectY;

        b.curveEnd.set(endX, endY);

        if(b.timer.get(0, damageInterval)){
            Damage.collidePoint(b, b.team, hitEffect, b.curveEnd.x, b.curveEnd.y);
            createSplashDamage(b, b.curveEnd.x, b.curveEnd.y);
        }

        if(b.timer.get(1, beamEffectInterval)){
            beamEffect.at(b.curveEnd.x, b.curveEnd.y, beamEffectSize * b.fslope(), hitColor);
        }

        if(charged(b)){
            b.damage = b.damageMultiplier() * damage * (1 + warmup(b) * maxDamageMultiplier);
            b.splash = b.damageMultiplier() * splashDamage * (1 + warmup(b) * maxDamageMultiplier);
            if(!Vars.headless && b.timer(3, 10)){
                if(DrawLighting) PositionLightning.createEffect(b, Tmp.v1.set(b.curveEnd.x, b.curveEnd.y), to, 1, 2);
                if(Mathf.chance(0.4)) WHFx.hitSparkLarge.at(b.x, b.y, to);
            }

          /*  if(!Vars.headless && DrawCurve && b.timer(4, curveInterval)){
                activeCurves.add(new DrawEnergyCurve(b.curveStart, b.curveEnd, getColor(b)));
            }*/

        }
    }

    public void createSplashDamage(Bullet c, float x, float y){
        if(!(c instanceof ChargePointLaserBullet b)) return;
        if(splashDamageRadius > 0 && !b.absorbed){
            Damage.damage(b.team, x, y, splashDamageRadius, b.splash, splashDamagePierce, collidesAir, collidesGround, scaledSplashDamage, b);

            if(status != StatusEffects.none){
                Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
            }

            if(heals()){
                indexer.eachBlock(b.team, x, y, splashDamageRadius, Building::damaged, other -> {
                    healEffect.at(other.x, other.y, 0f, healColor, other.block);
                    other.heal(healPercent / 100f * other.maxHealth() + healAmount);
                });
            }

            if(makeFire){
                indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
            }
        }
    }

    @Override
    public void draw(Bullet c){
        if(!(c instanceof ChargePointLaserBullet b)) return;
        float darkenPartWarmup = warmup(b);
        float stroke = b.fslope() * (1f - oscMag + Mathf.absin(Time.time, oscScl, oscMag)) * (darkenPartWarmup + 1) * width;

        if(trailLength > 0 && b.trail != null){
            float z = Draw.z();
            Draw.z(z - 0.0001f);
            b.trail.draw(getColor(b), stroke);
            Draw.z(z);
        }

        Draw.color(getColor(b));
        Drawn.basicLaser(b.x, b.y, b.curveEnd.x, b.curveEnd.y, stroke);
        Draw.color(Color.white);
        Drawn.basicLaser(b.x, b.y, b.curveEnd.x, b.curveEnd.y, stroke * 0.64f * (2 + darkenPartWarmup) / 3f);

        Drawf.light(b.aimX, b.aimY, b.x, b.y, stroke, tmpColor, 0.76f);
        Drawf.light(b.x, b.y, stroke * 4, tmpColor, 0.76f);
        Drawf.light(b.aimX, b.aimY, stroke * 3, tmpColor, 0.76f);

        Draw.color(tmpColor);
        if(charged(b)){
            float qW = Mathf.curve(warmup(b), 0.5f, 0.7f);

            for(int s : Mathf.signs){
                Drawf.tri(b.x, b.y, 6, 21 * qW, 90 * s + Time.time * 1.8f);
            }

            for(int s : Mathf.signs){
                Drawf.tri(b.x, b.y, 7.2f, 25 * qW, 90 * s + Time.time * -1.1f);
            }
        }
        if(darkenPartWarmup > 0.005f){
            tmpColor.lerp(Color.black, 0.86f);
            Draw.color(tmpColor);
            Drawn.basicLaser(b.x, b.y, b.curveEnd.x, b.curveEnd.y, stroke * 0.55f * darkenPartWarmup);
        }

        float fin = warmup(b);
        if(fin <= 0.01f) return;
        Draw.color(to);
        float num = 2;
        float phaseOffset = 360 / num;

        for(int i = 0; i < num; i++){
            float a = phaseOffset * i + Time.time * 0.5f;
            Lines.stroke(1.5f * (1 + Mathf.sin(Time.time, 12, 0.3f)));
            Tmp.v1.trns(a, width / 2);
            Drawn.drawSine2Modifier(b.x + Tmp.v1.x, b.y + Tmp.v1.y, b.curveEnd.x + Tmp.v1.x, b.curveEnd.y + Tmp.v1.y, Time.time * 0.4f, 6f, 0.6f, phaseOffset * Mathf.degreesToRadians, width / 2, 6, ((x1, y1) -> {
                Fill.circle(x1, y1, Lines.getStroke());
            }));
            Fill.circle(b.curveEnd.x, b.curveEnd.y, Lines.getStroke());
        }

      /*  for(DrawEnergyCurve curve : activeCurves){
            float alpha = Mathf.lerp(1f, 1f, curve.lifeProgress());
            Draw.color(to,alpha);
            Drawn.drawCurve(curve.getPoints(), to, 0.3f, curve.lifeProgress());
        }*/

        Draw.reset();
    }


    @Override
    public @Nullable Bullet create(
    @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
    float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
    ){
        ChargePointLaserBullet bullet = ChargePointLaserBullet.create();
        return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
    }

    public static class ChargePointLaserBullet extends Bullet{
        public float chargeTimer;
        public Vec2 curveStart = new Vec2();
        public Vec2 curveEnd = new Vec2();
        public Vec2 endX, endY;
        public float splash;

        @Override
        public void reset(){
            super.reset();
            chargeTimer = 0;
        }

        public static ChargePointLaserBullet create(){
            return Pools.obtain(ChargePointLaserBullet.class, ChargePointLaserBullet::new);
        }
    }
}
