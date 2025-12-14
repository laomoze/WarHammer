package wh.entities.bullet;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.*;

import static wh.content.WHFx.rand;

public class CritMissileBulletType extends CritBulletType{
    public CritMissileBulletType(){
        super();
    }

    public CritMissileBulletType(float speed, float damage, String bulletSprite){
        super(speed, damage, bulletSprite);
    }

    public CritMissileBulletType(float speed, float damage){
        super(speed, damage);
    }

    public float[] lengthWidthPans = {
    1.12f, 1.3f, 0.32f,
    1f, 1f, 0.3f,
    0.8f, 0.9f, 0.2f,
    0.5f, 0.8f, 0.15f,
    0.25f, 0.7f, 0.1f,
    };
    public Color[] colors = new Color[]{Color.valueOf("465ab8").a(0.55f), Color.valueOf("66a6d2").a(0.7f), Color.valueOf("89e8b6").a(0.8f), Color.valueOf("cafcbe"), Color.white};
    public float flameWidth = 3.7f, oscScl = 5f, oscMag = 0.06f;
    public int divisions = 15;
    public float flameLength = 10f;
    public float lengthOffset = 10f;
    public Interp lengthInterp = Interp.slope;
    public boolean weaveMagOnce = false;
    public boolean drawMissile = false;
    public boolean drawTeamColor = false;

    public Sound loopSound = Sounds.missileTrail;
    public float loopSoundVolume = 0.1f;

    @Override
    public void load(){
        super.load();
    }

    @Override
    public void draw(Bullet b){
        Draw.z(drawMissile ? Layer.flyingUnitLow : layer);
        super.draw(b);
        Draw.reset();
        float mult = b.fin(lengthInterp);
        float sin = Mathf.sin(Time.time, oscScl, oscMag);
        Draw.z(Layer.bullet - 0.01f);
        if(drawTeamColor && b.team != null){
            colors = new Color[]{b.team.color.cpy().a(0.4f), b.team.color.cpy().a(0.8f), b.team.color.cpy().lerp(Color.white, 0.8f)};
        }
        for(int i = 0; i < colors.length; i++){
            Draw.color(colors[i].write(Tmp.c1).mul(0.9f).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));
            Tmp.v1.trns(b.rotation(), lengthOffset);
            Drawf.flame(b.x - Tmp.v1.x, b.y - Tmp.v1.y, divisions, b.rotation() - 180,
            flameLength * lengthWidthPans[i * 3] * (1f - sin),
            flameWidth * lengthWidthPans[i * 3 + 1] * mult * (1f + sin),
            lengthWidthPans[i * 3 + 2]
            );
        }
    }

    @Override
    public void init(Bullet b){
        super.init(b);
    }

    public void lookAt(float angle, Bullet b){
        b.rotation(Angles.moveToward(b.rotation(), angle, homingPower * Time.delta));
    }

    public void lookAt(float x, float y, Bullet b){
        lookAt(b.angleTo(x, y), b);
    }


    @Override
    public void update(Bullet b){
        super.update(b);
        if(!Vars.headless && loopSound != Sounds.none){
            Vars.control.sound.loop(loopSound, b, loopSoundVolume);
        }
    }

    @Override
    public void updateHoming(Bullet b){
        if(homingPower > 0.0001f && b.time >= homingDelay){
            float realAimX = b.aimX < 0 ? b.x : b.aimX;
            float realAimY = b.aimY < 0 ? b.y : b.aimY;

            Teamc target;
            //home in on allies if possible
            if(heals()){
                target = Units.closestTarget(null, realAimX, realAimY, homingRange,
                e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id)
                );
            }else{
                if(b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team && collidesGround && !b.hasCollided(b.aimTile.build.id)){
                    target = b.aimTile.build;
                }else{
                    target = Units.closestTarget(b.team, realAimX, realAimY, homingRange,
                    e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                    t -> t != null && collidesGround && !b.hasCollided(t.id));
                }
            }

            if(target != null){
                b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
            }
        }

        if(followAimSpeed > 0f){
            if(b.shooter instanceof Unit u){
                float angle = b.angleTo(u.aimX, u.aimY);
                b.vel.setAngle(Angles.moveToward(b.vel.angle(), angle, followAimSpeed * Time.delta));
            }
            if(b.shooter instanceof ControlBlock bld){
                Unit unit = bld.unit();
                float angle = b.angleTo(unit.aimX, unit.aimY);
                b.vel.setAngle(Angles.moveToward(b.vel.angle(), angle, followAimSpeed * Time.delta));
            }
        }
    }

    @Override
    public void updateWeaving(Bullet b){
        super.updateWeaving(b);

        if(weaveMag != 0 && weaveMagOnce){
            rand.setSeed(b.id);
            float weaveRange = /*b.type.range/Vars.tilesize*/b.lifetime / Mathf.pi;
            b.vel.rotateRadExact((float)Math.sin((b.time + Math.PI * weaveRange / 2f) / weaveRange) * weaveMag * rand.random(0.6f, 1f) *
            (weaveRandom ? (Mathf.randomSeed(b.id, 0, 1) == 1 ? -1 : 1) : 1f) * Time.delta * Mathf.degRad);
        }
    }

    @Override
    public void drawTrail(Bullet b){
        if(trailLength > 0 && b.trail != null){
            //draw below bullets? TODO
            float z = Draw.z();
            Draw.z(z - 0.0002f);
            if(drawTeamColor){
                b.trail.draw(b.team != null ? b.team.color.cpy() : trailColor, trailWidth);
            }else{
                b.trail.draw(trailColor, trailWidth);
            }
            Draw.z(z);
        }
    }
}
