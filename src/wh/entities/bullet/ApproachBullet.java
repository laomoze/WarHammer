package wh.entities.bullet;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.ai.types.MissileAI;
import mindustry.entities.Mover;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;
import wh.gen.EntityRegister;
import wh.util.WHUtils;

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
    public float initAngleRand = 0f;
    public float initSpeedRand = 0f;

    // Optional behavior switches. Defaults preserve legacy behavior.
    public boolean followOwnerVelocity = false;
    public float ownerVelocityScale = 1f;
    // Starts inheriting owner movement only after own speed decays near zero.
    public float followOwnerSpeedThreshold = 0.08f;
    public boolean shootAngleFollowsOwner = false;
    public boolean shootIgnoreRange = false;
    public boolean shootIgnoreAngle = false;
    public boolean shootWithAimWhenNoTarget = false;
    public boolean continuousAimOwner = false;
    public float continuousChildFadeTime = 12f;

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
        /* Draw.color(b.team.color.cpy());*/
        Draw.color(Team.crux.color.cpy());

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
        b.reload -= Time.delta;

        trailUpdate.update(b);
        float baseSpeedNow;

        if(b.time > homingDelay){
            baseSpeedNow = 0f;
            b.initVel(b.rotation(), 0f);
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

                float bulletX = Angles.trnsx(b.rotation() - 90, this.shootX, this.shootY) + b.x;
                float bulletY = Angles.trnsy(b.rotation() - 90, this.shootX, this.shootY) + b.y;
                float targetRotation = Angles.angle(bulletX, bulletY, b.target.x(), b.target.y());
                boolean inRange = shootIgnoreRange || b.within(b.target, homingRange + 8);
                boolean angleOk = shootIgnoreAngle || Angles.within(b.rotation(), targetRotation, 10);

                if (inRange && bulletType != null) {
                    if (b.timer.get(3, 20) && b.reload < 0.0001f && angleOk) {
                        shoot(b, b.x, b.y, b.rotation());
                        b.reload = reload;
                    }
                }
            } else if (shootWithAimWhenNoTarget && bulletType != null) {
                float ax = b.aimX < 0f ? b.x + Angles.trnsx(b.rotation(), 64f) : b.aimX;
                float ay = b.aimY < 0f ? b.y + Angles.trnsy(b.rotation(), 64f) : b.aimY;
                float bulletX = Angles.trnsx(b.rotation() - 90, this.shootX, this.shootY) + b.x;
                float bulletY = Angles.trnsy(b.rotation() - 90, this.shootX, this.shootY) + b.y;
                float aimRotation = Angles.angle(bulletX, bulletY, ax, ay);
                b.rotation(Angles.moveToward(b.rotation(), aimRotation, rotateSpeed * Time.delta));
                boolean angleOk = shootIgnoreAngle || Angles.within(b.rotation(), aimRotation, 10f);
                if (b.timer.get(3, 20) && b.reload < 0.0001f && angleOk) {
                    shoot(b, b.x, b.y, b.rotation());
                    b.reload = reload;
                }
            }else{
                b.ang = Mathf.random(360);
            }
        }else{
            float fout = 1 - Math.min(1, b.time / homingDelay);
            baseSpeedNow = speed * Interp.fastSlow.apply(fout);
            b.initVel(b.rotation(), baseSpeedNow);
        }

        if (followOwnerVelocity && b.owner instanceof Velc ownerVel) {
            float speedThreshold = speed * followOwnerSpeedThreshold;
            if (baseSpeedNow <= speedThreshold) {
                b.set(
                        b.x + ownerVel.vel().x * Time.delta * ownerVelocityScale,
                        b.y + ownerVel.vel().y * Time.delta * ownerVelocityScale
                );
            }
        }

        if(b.bu != null && b.bu.type instanceof ContinuousBulletType){
            float fireRotation = b.rotation();
            float bulletX = Angles.trnsx(fireRotation - 90f, this.shootX, this.shootY) + b.x;
            float bulletY = Angles.trnsy(fireRotation - 90f, this.shootX, this.shootY) + b.y;
            if (continuousAimOwner && b.owner instanceof Unit ownerUnit) {
                float ang = Angles.angle(bulletX, bulletY, ownerUnit.aimX, ownerUnit.aimY);
                b.rotation(ang);
                b.bu.rotation(b.rotation());
                b.bu.aimX = ownerUnit.aimX;
                b.bu.aimY = ownerUnit.aimY;
                b.bu.set(bulletX, bulletY);
            } else {
                b.bu.rotation(fireRotation);
                b.bu.set(bulletX, bulletY);
            }
        }
    }

    protected float shootRotation(AB b) {
        if (shootAngleFollowsOwner && b.owner instanceof Rotc ownerRot) {
            return ownerRot.rotation();
        }
        return b.rotation();
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
        float aimX = target != null ? target.x() : (b.aimX < 0f ? b.x : b.aimX);
        float aimY = target != null ? target.y() : (b.aimY < 0f ? b.y : b.aimY);
        float fireRotation = shootRotation(b);
        float
        xSpread = Mathf.range(xRand),
        ySpread = Mathf.range(yRand),
                bulletX = b.x + Angles.trnsx(fireRotation - 90f, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread),
                bulletY = b.y + Angles.trnsy(fireRotation - 90f, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread),
                lifeScl = bulletType.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, aimX, aimY) / bulletType.range) : 1f;

        float angle = angleOffset + fireRotation + Mathf.range(bulletType.inaccuracy);

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

    @Override
    public void removed(Bullet b) {
        if (b instanceof AB ab && ab.bu != null && ab.bu.isAdded() && ab.bu.type instanceof ContinuousBulletType) {
            float fade = Math.max(1f, continuousChildFadeTime);
            ab.bu.time = Math.max(ab.bu.time, ab.bu.lifetime - fade);
        }
        super.removed(b);
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
        Bullet created = WHUtils.anyOtherCreate(ab, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
        if (created instanceof AB createdAb && (initAngleRand > 0f || initSpeedRand > 0f)) {
            float minScale = Math.max(0f, 1f - initSpeedRand);
            float initAngle = angle + Mathf.range(initAngleRand);
            float initSpeed = speed * Mathf.random(minScale, 1f + initSpeedRand);
            createdAb.initVel(initAngle, initSpeed);
        }
        return created;
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
