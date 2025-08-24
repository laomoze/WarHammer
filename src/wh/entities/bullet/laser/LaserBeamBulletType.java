package wh.entities.bullet.laser;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.content.WHFx;
import wh.graphics.PositionLightning;
import wh.util.*;

import static mindustry.Vars.*;

/**
 * 这么不是我们boomBeach的光束刃吗
 */
public class LaserBeamBulletType extends ContinuousBulletType{
    public Color[] colors = {Pal.lancerLaser.cpy().mul(1f, 1f, 1f, 0.4f), Pal.lancerLaser, Color.white};
    public float length = 160f;
    public float extensionProportion = 0.5f;
    public float width = 15f;
    public float lengthFalloff = 0.8f;
    public float oscScl = 2f, oscMag = 0.15f;
    public float lightningSpacing = -1, lightningDelay = 0.1f, lightningAngleRand;
    public boolean drawPositionLighting = false;
    public boolean checkAbsorber = true;
    public float lightningChance = 0.1f;
    public float damageMult=3f;
    public Effect LasergroundEffect = WHFx.BeamLaserGroundEffect;
    public Interp moveInterp = Interp.pow2In;

    public LaserBeamBulletType(float damage){
        this.damage = damage;
        this.speed = 0f;
        hitEffect = Fx.hitLaserBlast;
        hitColor = colors[2];
        despawnEffect = Fx.none;
        shootEffect = Fx.hitLancer;
        smokeEffect = Fx.none;
        hitSize = 4;
        lifetime = 100f;
        impact = true;
        keepVelocity = false;
        collides = false;
        pierce = true;
        pierceCap = 1;
        hittable = false;
        absorbable = false;
        removeAfterPierce = false;
        largeHit = true;
    }

    public LaserBeamBulletType(){
        this(1f);
    }

    @Override
    public float estimateDPS(){
        return super.estimateDPS() * 3f;
    }

    @Override
    public void init(){
        super.init();
        drawSize = Math.max(drawSize, length * 2f);
    }

    @Override
    protected float calculateRange(){
        return Math.max(length + extensionProportion * length, maxRange);
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        if(b.data == null) b.data = LaserData.create();
        LaserData data = (LaserData)b.data;
        float baseLength = Math.min(Mathf.dst(b.x, b.y, b.aimX, b.aimY), length);

        data.currentLength = baseLength + b.fin(moveInterp) * length * extensionProportion;
    }
    private float findAbsorber(Bullet b){
        float baseLength = Math.min(Mathf.dst(b.x, b.y, b.aimX, b.aimY), length);

        float targetLength = baseLength + b.fin(moveInterp) * length * extensionProportion;
        return WHUtils.findPierceLength(b,pierceCap, checkAbsorber, targetLength);
    }
    @Override
    public void draw(Bullet b){
        float realLength = b.fdata;

        float cwidth = width;

        float compound = 1f;

        Lines.lineAngle(b.x, b.y, b.rotation(), realLength);
        for(Color color : colors){
            Tmp.v1.trns(b.rotation(), realLength);
            float fadeTime = 0.1f*b.lifetime;
            float
            fadeOut = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f),
            fadeIn = Mathf.clamp(b.time < fadeTime? b.time / fadeTime : 1f);
            float fade = fadeIn * fadeOut;
            float sin = 1f - oscMag + Mathf.absin(Time.time, oscScl, oscMag);
            float w = (cwidth *= lengthFalloff) * fade * sin;

            Draw.color(color);
            Lines.stroke(w);
            Lines.lineAngle(b.x, b.y, b.rotation(), realLength, false);

            Fill.circle(b.x, b.y, cwidth * fade * sin);
            float ellipseLenScl = Mathf.lerp(1 - cwidth / (cwidth *= lengthFalloff), 1f, 0.75f);

            Drawf.flameFront(b.x + Tmp.v1.x, b.y + Tmp.v1.y, 30, b.rotation(),
            (realLength * 0.08f + 5) * ellipseLenScl * fade, w / 2);

            compound *= lengthFalloff;
        }
        Draw.reset();
        Tmp.v1.trns(b.rotation(), realLength * 1.1f);
        Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.2f * b.fout(), colors[0], 0.6f);
    }

    @Override
    public void drawLight(Bullet b){
    }

    @Override
    public void applyDamage(Bullet b){
        super.applyDamage(b);

        float resultLength =b.fdata, rot = b.rotation();

        if(lightningSpacing > 0){
            int idx = 0;
            for(float i = 0; i <= resultLength; i += lightningSpacing){
                float cx = b.x + Angles.trnsx(rot, i),
                cy = b.y + Angles.trnsy(rot, i);

                int f = idx++;

                for(int s : Mathf.signs){
                    Time.run(f * lightningDelay, () -> {
                        if(b.isAdded() && b.type == this && Mathf.random() <= lightningChance){
                            Lightning.create(b, lightningColor,
                            lightningDamage < 0 ? damage : lightningDamage,
                            cx, cy,
                            rot + (s == 1 ? 0 : -180) + Mathf.range(lightningAngleRand),
                            lightningLength + Mathf.random(lightningLengthRand));
                        }
                    });
                }
            }
        }
    }

    @Override
    public float currentLength(Bullet b){
        return b.fdata;
    }

    @Override
    public void removed(Bullet b){
        super.removed(b);
    }

    @Override
    public void update(Bullet b){
        b.damage=damage*(1+b.fin(moveInterp)*damageMult);
        if(drawPositionLighting  && b.timer.get(2, 10) && Mathf.chanceDelta(0.004) && !state.isPaused()){
            PositionLightning.createEffect(b, Tmp.v2.trns(b.rotation(), b.fdata).add(b), lightningColor, 2, Mathf.random(1, 2));
        }
        super.update(b);
        LaserData data = (LaserData)b.data;

        b.fdata=findAbsorber(b);
        if(data.lastLength != b.fdata){
            b.fdata= Mathf.lerpDelta(data.lastLength, b.fdata, 0.03f+0.03f*b.fout(moveInterp));
        }
        data.currentLength = b.fdata;
        Tmp.v1.trns(b.rotation(), b.fdata);
        float effectX = b.x + Tmp.v1.x;
        float effectY = b.y + Tmp.v1.y;

        if(b.timer(0, damageInterval)){
            LasergroundEffect.at(effectX, effectY, b.rotation(), hitColor, b.fdata);
        }
        data.lastLength = data.currentLength;
    }

    public static class LaserData implements Pool.Poolable{
        public float currentLength;
        public float lastLength;

        public static LaserData create(){
            return Pools.obtain(LaserData.class, LaserData::new);
        }
        @Override
        public void reset(){
            lastLength=currentLength=0;
        }
    }
}