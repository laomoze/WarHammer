package wh.entities.bullet.laser;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.graphics.*;
import wh.util.*;

public class LightingContinuousLaserBullet extends ContinuousLaserBulletType{
    public Color lightningColor;
    public boolean drawLightning = false;
    public float fadeTime = 20f;

    public int rings = 3;
    public float ringLenScl = 0.8f, ringWidthScl = 0.3f, ringWidthScl2 = 0.08f, ringSpacing = 13f;

    public int particles = 30;
    public float particleLife = 90;
    public float particleLen = 15;
    public float particleWidth = 2;

    public boolean rotate = true;
    public float rotateAngle = 6;

    public LightingContinuousLaserBullet(){
    }

    public LightingContinuousLaserBullet(float damage){
        super(damage);
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        if(!(b instanceof LCLBullet data)) return;
        if(rotate){
            Rand id = WHUtils.rand(b.id);
            float ran = id.random(1f);
            data.rotateAngle = rotateAngle * Mathf.sign(ran > 0.5f);
            data.rotationStart = b.rotation() - data.rotateAngle / 2;
            b.rotation(data.rotationStart);
        }
    }

    @Override
    public void draw(Bullet b){
        if(!(b instanceof LCLBullet data)) return;
        float fadeIn = Mathf.clamp(b.time < fadeTime ? b.time / fadeTime : 1f);
        float fout = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f);
        float fade = fadeIn * fout;
        float rot = b.rotation() + data.rotation;
        float realLength = WHUtils.findLaserPierceLength2(b, pierceCap, laserAbsorb, length * fadeIn, rot);
        float sinWidth = Mathf.absin(Time.time, oscScl, oscMag);

        if(drawLightning && lightningColor != null && b.timer.get(2, 5)){
            PositionLightning.createEffect(b, Tmp.v2.trns(b.rotation(), realLength).add(b), lightningColor, 2, Mathf.random(2, 3));
        }

        for(int i = 0; i < colors.length; i++){
            Draw.color(Tmp.c1.set(colors[i]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));

            float colorFin = i / (float)(colors.length - 1);
            float baseStroke = Mathf.lerp(strokeFrom, strokeTo, colorFin);
            float stroke = (width + sinWidth) * fade * baseStroke;
            float ellipseLenScl = Mathf.lerp(1 - i / (float)(colors.length), 1f, pointyScaling);

            Lines.stroke(stroke);
            Lines.lineAngle(b.x, b.y, rot, Math.max(0, realLength - frontLength), false);

            //back ellipse
            Drawf.flameFront(b.x, b.y, divisions, rot + 180f, backLength, stroke / 2f);

            //front ellipse
            Tmp.v1.trnsExact(rot, Math.max(0, realLength - frontLength));
            Drawf.flameFront(b.x + Tmp.v1.x, b.y + Tmp.v1.y, divisions, rot, frontLength * ellipseLenScl, stroke / 2f);
        }

        Tmp.v1.trns(b.rotation(), realLength * 1.1f);

        Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, lightStroke, lightColor, lightOpacity);
        Draw.reset();

        Rand rand = WHUtils.rand(b.id);
        float base = (Time.time / particleLife);
        for(int i = 0; i < particles; i++){
            Draw.z(Layer.bullet + 1f);
            float fin1 = (rand.random(1f) + base) % 1f, fout1 = 1f - fin1;
            float len = rand.random(particleLen * 0.7f, particleLen * 1.3f);
            float centerDeg = rand.random(Mathf.pi);

            float progress = Interp.pow2In.apply(fin1);
            float offset = rand.random(-width, width) * Mathf.cos(centerDeg) * fade;

            Tmp.v1.trns(rot, backLength).add(offset, offset).add(b.x, b.y);

            Tmp.v2.trns(rot, realLength).add(Tmp.v1);

            float moveAngle = Angles.angle(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);

            Tmp.v3.trns(moveAngle, Math.max(0, realLength - frontLength) * progress).add(Tmp.v1);

            Draw.color(hitColor);
            Lines.stroke(Interp.pow2Out.apply(fout1) * Mathf.curve(fin1, 0, 0.3f) * particleWidth * fade);
            Lines.lineAngleCenter(Tmp.v3.x, Tmp.v3.y, moveAngle, len * Mathf.curve(fin1, 0, 0.3f) * Interp.pow3Out.apply(fout1) * fade);
        }

        for(int i = 0; i < rings; i++){
            Draw.z(Layer.bullet - 0.001f);
            Lines.stroke((2 - i * 0.15f + sinWidth) * fade);
            Tmp.v1.trns(rot, i * ringSpacing).add(b.x, b.y);
            float wscl = (ringWidthScl - i * ringWidthScl2) * fade, lscl = (ringLenScl - i * ringWidthScl2 + sinWidth * 0.05f) * fade;
            Draw.color(hitColor);
            Lines.ellipse(Tmp.v1.x, Tmp.v1.y, width * 4, wscl, lscl, rot);
        }

        Draw.reset();
    }


    @Override
    public float currentLength(Bullet b){
        float fadeIn = Mathf.clamp(b.time < fadeTime ? b.time / fadeTime : 1f);
        return length * fadeIn;
    }

    @Override
    public void update(Bullet b){
        if(!(b instanceof LCLBullet data)) return;
        if(!continuous) return;
        float rot;
        if(rotate){
            data.rotation = data.rotateAngle * b.fin();
            rot = b.rotation() + data.rotation;
        }else rot = b.rotation();

        if(b.timer(1, damageInterval)){
            applyDamage(b, rot);
        }

        if(shake > 0){
            Effect.shake(shake, shake, b);
        }

        updateBulletInterval(b);

    }

    public void applyDamage(Bullet b, float rot){
        float damage = b.damage;
        if(timescaleDamage && b.owner instanceof Building build){
            b.damage *= build.timeScale();
        }
        Damage.collideLine(b, b.team, b.x, b.y, rot, currentLength(b), largeHit, laserAbsorb, pierceCap);
        b.damage = damage;
    }

    @Override
    public @Nullable Bullet create(
    @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
    float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
    ){
        LCLBullet bullet = LCLBullet.create();
        return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
    }

    public static class LCLBullet extends Bullet{
        public float rotation, rotationStart, rotateAngle;

        @Override
        public void reset(){
            super.reset();
            rotation = rotationStart = rotateAngle = 0f;
        }

        public static LCLBullet create(){
            return Pools.obtain(LCLBullet.class, LCLBullet::new);
        }
    }
}

