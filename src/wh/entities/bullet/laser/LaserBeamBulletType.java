package wh.entities.bullet.laser;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Interval;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import wh.content.WHFx;
import wh.core.WHSettings;
import wh.graphics.MainRenderer;
import wh.graphics.PositionLightning;
import wh.util.WHUtils;

import static mindustry.Vars.state;

/**
 * 这么不是我们boomBeach的激光吗
 */
public class LaserBeamBulletType extends ContinuousBulletType{
    public Color[] colors = {Pal.lancerLaser.cpy().mul(1f, 1f, 1f, 0.4f), Pal.lancerLaser, Color.white};
    public float length = 160f;
    public float extensionProportion = 0.5f;
    public float width = 15f;
    public float lengthFalloff = 0.8f;
    public float oscScl = 2f, oscMag = 0.15f;
    public float lightningSpacing = -1, lightningDelay = 0.1f, lightningAngleRand;
    public boolean tri = false, triCap = false;
    public float triCapTipLengthScale = 2f;
    public float sideLength = 29f, sideWidth = 0.7f;
    public float sideAngle = 90f;
    public boolean drawPositionLighting = false;
    public boolean renderingDistortion = false;
    public float pathDistortionRectWidthScale = 2f;
    public float pathDistortionRectStrength = 0.8f;
    public float pathDistortionRectLife = 40;
    public boolean rotate = false;
    public float rotateAngle = 20;
    public float damageMult = 0;
    public boolean debugDrawLengths = false;
    public float debugLengthLaneSpacing = 9f;
    public Effect LasergroundEffect = WHFx.BeamLaserGroundEffect;
    public Interp moveInterp = Interp.pow2In;
    public float moveSpeed = 0.03f;
    public float absorbMoveSpeed = 3f;
    public float fadeTime = 10f;

    private final Vec2 Tmp1 = new Vec2();
    private final Vec2 Tmp2 = new Vec2();

    public LaserBeamBulletType(float damage){
        this.damage = damage;
        this.speed = 0f;
        hitEffect = Fx.hitLaserBlast;
        hitSize = 12;
        lifetime = 100f;
        impact = true;
        drawSize = 420f;
        pierceCap = -1;
        largeHit = false;
        keepVelocity = false;
        collides = false;
        hittable = false;
        absorbable = false;
        laserAbsorb = true;
    }

    public LaserBeamBulletType(){
        laserAbsorb = true;
    }


    @Override
    public float continuousDamage(){
        if(!continuous) return -1f;
        return Mathf.round(damage / damageInterval * 60f * (1 + damageMult * 0.2f));
    }

    @Override
    public float estimateDPS(){
        if(!continuous) return super.estimateDPS();
        //assume firing duration is about 100 by default, may not be accurate there's no way of knowing in this method
        //assume it pierces 3 blocks/units
        return Mathf.round(damage / damageInterval * 60f * (1 + damageMult * 0.2f));
    }

    @Override
    public void init(){
        super.init();
        drawSize = Math.max(drawSize, length * 2.5f);
    }

    @Override
    protected float calculateRange(){
        return Math.max(length + extensionProportion * length, maxRange);
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        if(!(b instanceof LaserDataBullet data)) return;
        if(rotate){
            Rand id = WHUtils.rand(b.id);
            float ran = id.random(1f);
            data.rotateAngle = rotateAngle * Mathf.sign(ran > 0.5f);
            data.rotationStart = b.rotation() - data.rotateAngle;
            b.rotation(data.rotationStart);
        }
        data.currentLength = data.startLength = Math.min(Mathf.dst(b.x, b.y, b.aimX, b.aimY), length);
    }

    private float findAbsorber(Bullet b){
        if(!(b instanceof LaserDataBullet data)) return 0;
        float baseLength = data.startLength;
        float targetLength = baseLength + b.fin(moveInterp) * length * extensionProportion;
        if(timescaleDamage && b.owner instanceof Building build){
            targetLength = baseLength + b.fin(moveInterp) * length * extensionProportion * build.timeScale();
        }
        return WHUtils.findLaserPierceLength(b, pierceCap, laserAbsorb, targetLength, b.rotation() + data.rotation);
    }


    @Override
    public void draw(Bullet b){
        if(!(b instanceof LaserDataBullet data)) return;
        float realLength = data.currentLength, rot = b.rotation() + data.rotation;

        float cwidth = width;

        float compound = 1f;

        for(Color color : colors){
            Tmp.v1.trns(rot, realLength);
            float
            fadeOut = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f),
            fadeIn = Mathf.clamp(b.time < fadeTime ? b.time / fadeTime : 1f);
            float fade = fadeIn * fadeOut;
            float sin = 1f - oscMag + Mathf.absin(Time.time, oscScl, oscMag);
            float w = (cwidth *= lengthFalloff) * fade * sin;

            Draw.color(color);
            Lines.stroke(w);
            Lines.lineAngle(b.x, b.y, rot, realLength, false);

            if(tri) for(int i : Mathf.signs){
                Drawf.tri(b.x, b.y, sideWidth * b.fout() * cwidth,
                sideLength * compound, rot + sideAngle * i);
            }

            Fill.circle(b.x, b.y, cwidth * fade * sin);
            float ellipseLenScl = Mathf.lerp(1 - cwidth / (cwidth *= lengthFalloff), 1f, 0.75f);

            if(!triCap){
                Drawf.flameFront(b.x + Tmp.v1.x, b.y + Tmp.v1.y, 30, rot,
                (realLength * 0.08f + 5) * ellipseLenScl * fade, w / 2);
            }else{
                float tipHeight = width * 0.7f;
                Tmp.v2.trns(rot, realLength - tipHeight).add(b);
                Tmp.v1.trns(rot, width * triCapTipLengthScale).add(Tmp.v2);
                Draw.color(color);
                for(int s : Mathf.signs){
                    Tmp.v3.trns(rot, w * -0.7f, w * s);
                    Fill.tri(Tmp.v2.x, Tmp.v2.y, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x + Tmp.v3.x, Tmp.v2.y + Tmp.v3.y);
                }
            }

            compound *= lengthFalloff;
        }
        Draw.reset();
        Tmp.v1.trns(rot, realLength * 1.1f);
        Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.2f * b.fout(), colors[0], 0.6f);
        Draw.reset();
        if (debugDrawLengths || WHSettings.laserDebugLengths()) {
            Draw.z(Layer.flyingUnit + 1f);
            drawDebugLengthLine(b, rot, data.currentLength, -1.5f, Color.green);
            drawDebugLengthLine(b, rot, b.fdata, -0.5f, Color.blue);
            drawDebugLengthLine(b, rot, data.deltaLength, 0.5f, Color.orange);
            drawDebugLengthLine(b, rot, data.debugResultLength, 1.5f, Color.red);
        }
    }

    @Override
    public void drawLight(Bullet b){
    }

    @Override
    public void applyDamage(Bullet b){
        if(!(b instanceof LaserDataBullet data)) return;
        float rot = b.rotation() + data.rotation;
        float lengthBase = Math.max(data.currentLength, b.fdata);
        float lengthPad = triCap ? Math.max(0f, width * (triCapTipLengthScale - 0.7f)) + width * 0.5f : width * 0.5f;
        float resultLength = Math.max(lengthBase, 0f) + lengthPad;
        data.debugResultLength = resultLength;

        float damage = b.damage;
        if(timescaleDamage && b.owner instanceof Building build){
            b.damage *= build.timeScale();
        }

        WHUtils.collideLine(b, b.team, b.x, b.y, rot, resultLength, largeHit, laserAbsorb, pierceCap);

        b.damage = damage;

       /* if(lightningSpacing > 0){
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
        }*/
    }

    @Override
    public float currentLength(Bullet b){
        return b.fdata;
    }

    private void drawDebugLengthLine(Bullet b, float rot, float length, float lane, Color color) {
        if (length <= 0.001f) return;
        float offset = lane * debugLengthLaneSpacing;
        float sx = b.x + Angles.trnsx(rot + 90f, offset);
        float sy = b.y + Angles.trnsy(rot + 90f, offset);
        Draw.color(color);
        Draw.alpha(0.95f);
        Lines.stroke(1.6f);
        Lines.lineAngle(sx, sy, rot, length, false);
    }


    @Override
    public void update(Bullet b){
        if (!(b instanceof LaserDataBullet data)) return;
        b.fdata = findAbsorber(b);
        super.update(b);

        b.damage = damage * (1 + b.fin(moveInterp) * damageMult) * b.damageMultiplier();

        float lerp = rotate ? moveSpeed : moveSpeed * 0.6f + b.fin(moveInterp) * moveSpeed * 0.4f;
        if (rotate) {
            data.rotation = data.rotateAngle * b.fout();
        }
        float rot = b.rotation() + data.rotation;

        boolean stop = data.stop, maxPierceCap = b.type.pierceCap != -1 && b.collided.size >= b.type.pierceCap;

        float baseLength = data.startLength, maxLen = baseLength + b.fin(moveInterp) * length * extensionProportion;
        float len = Math.min(data.currentLength + Time.delta * absorbMoveSpeed, maxLen);

        if(stop/*||maxPierceCap*/){
            data.deltaLength = b.fdata = WHUtils.findLaserPierceLength(b, pierceCap, laserAbsorb, len, rot);
            data.currentLength = Mathf.lerpDelta(data.currentLength, b.fdata, 0.15f);
        }else{
            if(data.currentLength < b.fdata && data.deltaLength != 0){
                b.fdata = WHUtils.findLaserPierceLength(b, pierceCap, laserAbsorb, len, rot);
            }
            data.currentLength = b.fdata;
            data.currentLength = Mathf.lerpDelta(data.currentLength, b.fdata, lerp);
        }

        Tmp.v1.trns(rot, data.currentLength);
        float effectX = b.x + Tmp.v1.x;
        float effectY = b.y + Tmp.v1.y;

        addRectPathDistortion(b, data, rot);

        if(b.timer(0, 3 * b.fin() + 2)){
            LasergroundEffect.at(effectX, effectY, 0, hitColor, data.currentLength);
        }

        if(drawPositionLighting && b.timer(3, 10) && !state.isPaused()){
            PositionLightning.createEffect(b, Tmp.v2.trns(rot, b.fdata).add(b), lightningColor, 2, Mathf.random(1, 2));
        }

    }

    @Override
    public @Nullable Bullet create(
    @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
    float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
    ){
        LaserDataBullet bullet = LaserDataBullet.create();
        return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
    }

    private void addRectPathDistortion(Bullet b, LaserDataBullet data, float rot){
        if(renderingDistortion && b.timer(4, pathDistortionRectLife / 2)){

            float rectLength = Math.max(data.currentLength, 1f);
            float rectWidth = Math.max(width * pathDistortionRectWidthScale, 1f);

            Tmp1.trns(rot, -width);
            Tmp2.trns(rot, rectLength * 0.5f + width / 2);
            MainRenderer.addShockRect(
            b.x + Tmp1.x + Tmp2.x, b.y + Tmp1.y + Tmp2.y,
            rectLength, rectWidth, rot, pathDistortionRectLife, pathDistortionRectStrength);
        }
    }

    public static class LaserDataBullet extends Bullet{
        public float currentLength, startLength, deltaLength, length;
        public float debugResultLength;
        public float rotation, rotationStart, rotateAngle;
        public boolean stop;
        public Interval timer2 = new Interval(8);

        @Override
        public void reset(){
            super.reset();
            stop = false;
            deltaLength = currentLength = startLength = length = debugResultLength = rotation = rotationStart = rotateAngle = 0f;
        }

        public static LaserDataBullet create(){
            return Pools.obtain(LaserDataBullet.class, LaserDataBullet::new);
        }
    }
}
