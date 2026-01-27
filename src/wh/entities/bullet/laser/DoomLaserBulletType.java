package wh.entities.bullet.laser;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.environment.*;
import wh.content.*;
import wh.graphics.*;
import wh.util.*;

import static mindustry.Vars.headless;
import static wh.content.WHFx.groundEffect;


/**
 * 给我干哪来了，Forts？
 */
public class DoomLaserBulletType extends ContinuousBulletType{
    public float startOffset = 900;
    public float fixedAngle = 20;
    public float sideAngle = 80f;
    public float sideLength = 50f;
    public float triSize = 50f;
    public float width = 40;
    public Color[] colors = {Pal.lancerLaser.cpy().mul(1f, 1f, 1f, 0.4f), Pal.lancerLaser, Pal.lancerLaser, Color.white};

    public float triLifetime = 90f;
    public float shake = 0f;
    public float oscScl = 2, oscMag = 0.5f;

    public boolean autoHoming = true;

    public DoomLaserBulletType(){
        reflectable = false;
        collides = false;
        pierce = true;
        hittable = false;
        absorbable = false;
        damage = 150f;
        splashDamage = 150f;
        splashDamageRadius = width;
        homingRange = 100f;
        lifetime = 60f * 5f;
        speed = 0.5f;
        trailLength = 10;
        optimalLifeFract = 0.5f;
        shootEffect = smokeEffect = Fx.none;
        trailColor = hitColor = Pal.lancerLaser;
        despawnEffect = hitEffect = WHFx.generalExplosion(10, hitColor, 30, 10, false);
        homingPower = 0.03f;
    }

    @Override
    public void load(){
        super.load();
    }

    @Override
    public float estimateDPS(){
        if(!continuous) return super.estimateDPS();
        return (damage + splashDamage) * (60 / damageInterval);
    }

    @Override
    public void init(){
        super.init();
        drawSize = Math.max(drawSize, startOffset * 2f);
    }

    @Override
    public void draw(Bullet b){
        float fadeInTime = 60f;
        float fadeIn = Mathf.clamp(b.time / fadeInTime, 0, 1);
        float fout2 = b.fout(Interp.pow10Out) * fadeIn;

        float rx = b.x, ry = b.y;
        for(int i = 0; i < colors.length; i++){
            Draw.z(Layer.bullet);
            float strokeWidth = (width + Mathf.absin(Time.time, oscScl, oscMag)) * (1 - i * 0.2f) * fout2;
            Draw.color(Tmp.c1.set(colors[i]).mul(1f + Mathf.absin(Time.time + i * 10f, 1f, 0.1f)));
            Tmp.v2.trns(fixedAngle, startOffset).add(rx, ry);
            Fill.circle(Tmp.v2.x, Tmp.v2.y, width * fout2 * (1.1f - i * 0.2f));
            Lines.stroke(strokeWidth);
            Lines.lineAngle(Tmp.v2.x, Tmp.v2.y, fixedAngle - 180, startOffset, false);
            Fill.circle(rx, ry, width * fout2 * (1f - i * 0.2f));
            Drawf.tri(Tmp.v2.x, Tmp.v2.y, strokeWidth, sideLength * fout2 * (0.8f - i * 0.2f), sideAngle);
            Drawf.tri(Tmp.v2.x, Tmp.v2.y, strokeWidth, sideLength * fout2 * (0.8f - i * 0.2f), sideAngle - 180);

            Rand rand = WHUtils.rand(b.id);
            float base = (Time.time / triLifetime * rand.random(0.3f, 1f));

            int particles = 5;
            for(int a = 0; a < particles; a++){
                Draw.z(Layer.bullet + 0.01f);
                float fin1 = (rand.random(1f) + base) % 1f, fout1 = 1f - fin1,
                fadeIn1 = Mathf.curve(b.fin(), 0f, 0.15f),
                progress = Interp.pow2In.apply(fadeIn1) * Interp.pow5Out.apply(fout1),
                angle1 = b.rotation() - 180,
                size = width / 1.5f * (0.8f - i * 0.2f) * fout2,
                length = rand.random(triSize * 0.5f, triSize * 2) * (1 - i * 0.2f) * fout2 * progress;
                Draw.color(Tmp.c1.set(colors[i]).mul(1f + Mathf.absin(Time.time + i * 10f, 1f, 0.15f)));
                Drawn.tri(rx, ry, size, triSize / 3 * (0.8f - i * 0.2f), angle1 - 180);
                Drawn.tri(rx, ry, size * progress, length * (0.8f - i * 0.2f), angle1 + 10 * particles * rand.random(1));

                if(trailLength > 0 && b.trail != null){
                    Color colorIndex = colors[i % (colors.length - 1)];
                    Color trailColor = Tmp.c1.set(colorIndex).mul(1f + Mathf.absin(Time.time + i * 10f, 1f, 0.15f)).lerp(Color.black, 0.05f);
                    b.trail.draw(trailColor, width / 3.5f * (0.8f - i * 0.2f) * fout2);
                }
            }
        }

        int particles = 70;
        float particleLife = 90f;
        float particleLen = 20;
        Rand rand = WHUtils.rand(b.id);
        float base = (Time.time / particleLife);
        for(int i = 0; i < particles; i++){
            Draw.z(Layer.bullet + 0.03f);
            float fin1 = (rand.random(1f) + base) % 1f, fout1 = 1f - fin1;
            float len = rand.random(particleLen * 0.7f, particleLen * 1.3f);
            float centerDeg = rand.random(Mathf.pi);

            float progress = Interp.pow2Out.apply(fin1);
            float offset = rand.range(width * 2) * (1 - progress) * Mathf.cos(centerDeg) * (fout1 + 0.1f);

            Tmp.v4.set(Tmp.v2).add(rand.random(-width, width), rand.random(-width, width));

            float moveAngle = Angles.angle(rx, ry, Tmp.v4.x, Tmp.v4.y);

            Tmp.v3.trns(moveAngle, -startOffset * fin1).add(Tmp.v2.x, Tmp.v2.y);

            Tmp.v3.add(rand.range(offset), rand.range(offset));

            Draw.color(hitColor);
            Lines.stroke(Mathf.curve(fout1, 0, 1f) * fout1 * rand.random(width / 8f) * fout2);
            Lines.lineAngleCenter(Tmp.v3.x, Tmp.v3.y, moveAngle, len * fout1);

            Lines.stroke(Mathf.curve(fin1, 0, 1f) * fin1 * Math.max(rand.random(1, 1.2f), width / 8f) * fout2);
            Lines.lineAngleCenter(Tmp.v3.x, Tmp.v3.y, moveAngle, len * fin1);
        }

        Draw.reset();
    }

    public void lookAt(float x, float y, Bullet b){
        lookAt(b.angleTo(x, y), b);
    }

    public void lookAt(float angle, Bullet b){
        b.rotation(Angles.moveToward(b.rotation(), angle, homingPower * Time.delta));
    }

    @Override
    public void updateHoming(Bullet b){
        if(homingPower > 0.0001f && b.time >= homingDelay){
            float realAimX = b.aimX < 0 ? b.data instanceof Position ? ((Position)b.data).getX() : b.x : b.aimX;
            float realAimY = b.aimY < 0 ? b.data instanceof Position ? ((Position)b.data).getY() : b.y : b.aimY;

            Teamc target;
            if(heals()){
                target = Units.closestTarget(null, realAimX, realAimY, homingRange,
                e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id)
                );
            }else{
                if(b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team &&
                collidesGround && !b.hasCollided(b.aimTile.build.id)){
                    target = b.aimTile.build;
                }else{
                    target = Units.closestTarget(b.team, realAimX, realAimY, homingRange,
                    e -> e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                    t -> collidesGround && !b.hasCollided(t.id));
                }
            }

            if(reflectable) return;
            if(target != null && autoHoming){
                b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta));
            }else{
                @Nullable Unit shooter = null;
                if(b.owner instanceof Unit) shooter = (Unit)b.owner;
                if(b.owner instanceof ControlBlock) shooter = ((ControlBlock)b.owner).unit();
                if(shooter != null){
                    //if(!Vars.net.client() || shooter.isPlayer()) lookAt(EUGet.pos(shooter.aimX, shooter.aimY), b);
                    if(shooter.isPlayer()) lookAt(shooter.aimX, shooter.aimY, b);
                    else{
                        if(b.data instanceof Position p)
                            lookAt(p.getX(), p.getY(), b);
                        else
                            lookAt(realAimX, realAimY, b);
                    }
                }
            }
        }
    }

    @Override
    public void updateTrail(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new Trail(trailLength);
            }
            b.trail.length = trailLength;
            b.trail.update(b.x, b.y, trailInterp.apply(b.fin(Interp.pow3Out)) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
        }
    }

    @Override
    public void removed(Bullet b){
    }

    @Override
    public void update(Bullet b){
        if(shake > 0){
            Effect.shake(shake, shake, b);
        }
        float targetX = b.x;
        float targetY = b.y;
        if(b.timer(0, damageInterval)){
            Damage.collidePoint(b, b.team, hitEffect, targetX, targetY);
            hit(b, targetX, targetY);
        }
        Floor floor = Vars.world.floorWorld(targetX, targetY);
        if(floor != null && b.timer(1, damageInterval)){
            groundEffect.at(targetX, targetY, b.rotation(), floor.mapColor.cpy().lerp(Color.black, 0.2f));
        }
        updateHoming(b);
        updateTrail(b);
        updateBulletInterval(b);
    }
}