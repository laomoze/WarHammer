package wh.entities.bullet;

import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.gen.*;
import wh.util.*;

import static wh.util.WHUtils.rand;

public class ApproachBullet extends BulletType{
    public ShootPattern shootType = new ShootPattern();
    public BulletType bulletType;
    public Color color;

    public float retarget = 12f;
    public float reload = 90;

    public float shootX = 0, shootY = 0;
    public float xRand = 0, yRand = 0;
    public float velocityRnd = 0.08f;
    public float rotateSpeed = 2;

    public float soundPitchMin = 1, soundPitchMax = 1;
    public Sound chargeSound = Sounds.chargeLancer;

    public ApproachBullet(){
    }

    {
        lifetime = 500;
        keepVelocity = collides = absorbable = hittable = false;
        homingRange = 15 * 8f;
        homingDelay = 60;
        drag = 0.005f;
        speed = 5;
    }

    public Approach approach = b -> {
        if(b.target != null){
            float
            bulletX = Angles.trnsx(b.rotation() - 90, this.shootX, this.shootY) + b.x,
            bulletY = Angles.trnsy(b.rotation() - 90, this.shootX, this.shootY) + b.y,
            targetRotation = Angles.angle(bulletX, bulletY, b.target.x(), b.target.y()),
            rotation = Angles.moveToward(b.rotation(), targetRotation, rotateSpeed * Time.delta);
            b.rotation(rotation);
            rand.setSeed(b.id);
            float dx = WHUtils.dx(b.target.x(), homingRange * 0.5f * rand.random(0.7f, 1), b.ang + Time.time / 2),
            dy = WHUtils.dy(b.target.y(), homingRange * 0.5f * rand.random(0.7f, 1), b.ang + Time.time / 2);

            WHUtils.movePoint(b, dx, dy, speed / 100f);
        }
    };

    public BulletDrawer drawer = b -> {
        float t = 20;
        float fadeIn = Mathf.clamp(b.time / t);
        float fadeOut = b.time > lifetime - t ? Mathf.clamp(1 - (b.time - lifetime) / t) : 1;

        float radius = 10f * fadeOut * fadeIn;

        Draw.z(Layer.bullet + b.layer);
        Draw.color(b.team.color.cpy());

        Tmp.v1.set(b);
        float ex = Tmp.v1.x, ey = Tmp.v1.y;

        Fill.circle(ex, ey, (radius + Mathf.absin(Time.time, 4f, radius / 4f)));
        float ang = Time.time * 1.5f;
        for(int i : Mathf.signs){
            WHUtils.tri(ex, ey, radius / 3f, radius * 2.35f, ang + 90 * i);
        }
        ang *= -1.5f;
        for(int i : Mathf.signs){
            WHUtils.tri(ex, ey, radius / 4f, radius * 1.85f, ang + 90 * i);
        }
        Draw.color(Color.black);
        Fill.circle(ex, ey, (radius + Mathf.absin(Time.time, 4f, radius / 4f)) * 0.7f);
    };

    public int trailAmount = 2;
    public float trailWidth = 1.5f;
    public TrailUpdate trailUpdate = b -> {
        for(int i = 0; i < trailAmount; i++){
            if(!Vars.headless){
                if(b.trails[i] == null) b.trails[i] = new Trail(12);
                b.trails[i].length = 12;
                rand.setSeed(b.id);

                float dx = WHUtils.dx(b.x, 13, (Time.time / 2 * rand.random(0.7f, 1) * (8 - (i % 2 == 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 180 * i),
                dy = WHUtils.dy(b.y, 13, (Time.time / 2 * rand.random(0.7f, 1) * (8 - (i % 2 != 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 180 * i);
                if(!Vars.headless) b.trails[i].update(dx, dy, trailInterp.apply(b.fin()) * trailWidth);
            }
        }
    };

    @Override
    public void init(Bullet b){
        super.init(b);
        if(!(b instanceof AB ab)) return;
        init(ab);
    }

    public void init(AB b){
        b.trails = new Trail[trailAmount];
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        if(b instanceof AB ab){
            update(ab);
        }
    }

    public void update(AB b){
        float
        bulletX = Angles.trnsx(b.rotation() - 90, this.shootX, this.shootY) + b.x,
        bulletY = Angles.trnsy(b.rotation() - 90, this.shootX, this.shootY) + b.y;

        b.reload -= Time.delta;

        trailUpdate.update(b);

        if(b.time > homingDelay){
            b.initVel(b.rotation(), 0);
            if(b.timer.get(4, retarget) && b.find){
                float rx = b.x, ry = b.y;
                if(b.target != null){
                    rx = b.target.x();
                    ry = b.target.y();
                }
                b.target = Units.closestTarget(b.team, rx, ry, homingRange,
                e -> e != null && e.checkTarget(collidesAir, collidesGround),
                t -> t != null && collidesGround);
            }
            if(b.target != null){
                approach.update(b);

                float targetRotation = Angles.angle(bulletX, bulletY, b.target.x(), b.target.y());

                if(b.within(b.target, homingRange + 8) && bulletType != null){
                    if(b.timer.get(3, 20) && b.reload < 0.0001f && Angles.within(b.rotation(), targetRotation, 10)){
                        shoot(b, b.x, b.y, b.rotation());
                        b.reload = reload;
                    }
                }
            }else{
                b.ang = Mathf.random(360);
            }
        }else{
            float fout = 1 - Math.min(1, b.time / homingDelay);
            b.initVel(b.rotation(), speed * Interp.fastSlow.apply(fout));
        }

        if(b.bu != null && b.bu.type instanceof ContinuousBulletType){
            b.bu.rotation(b.rotation());
            b.bu.set(bulletX, bulletY);
        }
    }

    protected void shoot(AB b, float shootX, float shootY, float rotation){

        if(shootType.firstShotDelay > 0){
            chargeSound.at(shootX, shootY, Mathf.random(soundPitchMin, soundPitchMax));
            bulletType.chargeEffect.at(shootX, shootY, rotation, bulletType.keepVelocity ? b : null);
        }

        shootType.shoot(1, (xOffset, yOffset, angle, delay, mover) -> {
            if(delay > 0f){
                Time.run(delay, () -> bullet(b, xOffset, yOffset, angle, mover));
            }else{
                bullet(b, xOffset, yOffset, angle, mover);
            }
        }, () -> {
        });
    }

    protected void bullet(AB b, float xOffset, float yOffset, float angleOffset, Mover mover){

        Teamc target = b.target;
        float
        xSpread = Mathf.range(xRand),
        ySpread = Mathf.range(yRand),
        bulletX = b.x + Angles.trnsx(b.rotation() - 90, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread),
        bulletY = b.y + Angles.trnsy(b.rotation() - 90, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread),
        lifeScl = bulletType.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, target.x(), target.y()) / bulletType.range) : 1f,
        angle = angleOffset + b.rotation() + Mathf.range(bulletType.inaccuracy);

        Entityc shooter = b.owner instanceof MissileAI ai ? ai.shooter : b.owner;
        b.bu = bulletType.create(b, shooter, b.team, bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd),
        lifeScl, null, mover, -1, -1, b.target);

        shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));

        bulletType.shootEffect.at(bulletX, bulletY, angle, bulletType.hitColor, shooter);
        bulletType.smokeEffect.at(bulletX, bulletY, angle, bulletType.hitColor, shooter);
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);

        if(b instanceof AB ab){
            drawer.draw(ab);
            for(Trail trail : ab.trails){
                if(trail != null){
                    trail.draw(color, trailWidth);
                    trail.drawCap(color, trailWidth);
                }
            }
        }
    }

    public interface Approach{
        void update(AB b);
    }

    public interface TrailUpdate{
        void update(AB b);
    }

    public interface BulletDrawer{
        void draw(AB b);
    }

    @Override
    public @Nullable Bullet create(
    @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
    float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
    ){
        AB ab = AB.create();

        ab.target = null;
        ab.ang = Mathf.random(360);

        ab.trails = new Trail[trailAmount];
        for(int i = 0; i < trailAmount; i++){
            if(ab.trails[i] != null){
                ab.trails[i].clear();
            }
        }

        return WHUtils.anyOtherCreate(ab, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
    }

    public static class AB extends Bullet{
        public @Nullable Teamc target = null;
        public Trail[] trails;

        public float ang, reload, layer;
        public boolean find = false;

        public Bullet bu;

        @Override
        public int classId(){
            return EntityRegister.getId(AB.class);
        }


        @Override
        public void reset(){
            super.reset();
            target = null;
            trails = null;
            bu = null;
            layer = reload = ang = 0;
            find = false;
        }

        public static AB create(){
            return Pools.obtain(AB.class, AB::new);
        }
    }
}
