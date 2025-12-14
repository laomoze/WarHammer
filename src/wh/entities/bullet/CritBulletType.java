package wh.entities.bullet;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.entities.world.entities.*;
import wh.util.*;

import static mindustry.Vars.*;

public class CritBulletType extends BasicBulletType{
    protected static Rand critRand = new Rand();

    public float critChance = 0.1f, critMultiplier = 2f;
    public Effect critEffect = Fx.none;
    public boolean bouncing, despawnHitEffects = true;
    public boolean makePlaFire;
    public float plaFireChance = -1;

    public CritBulletType(float speed, float damage, String sprite){
        super(speed, damage, sprite);
        impact = true;
        ammoMultiplier = 1;
        shootEffect = Fx.shootBig;
        smokeEffect = Fx.shootBigSmoke;
        hitEffect = Fx.none;
        hitColor = Pal.lightOrange;
        trailLength = 10;
        trailWidth = -1f;
        status = StatusEffects.none;
    }

    public CritBulletType(float speed, float damage){
        this(speed, damage, "bullet");
    }

    public CritBulletType(){

    }

    public static boolean critChance(Bullet b, float chance){
        if(chance >= 1) return true;

        critRand.setSeed(b.id);
        return critRand.nextFloat() < chance;
    }


    @Override
    public void init(){
        super.init();

        if(trailWidth < 0f) trailWidth = width * (10f / 52f);
    }

    @Override
    public void init(Bullet b){
        if(b.data == null){
            b.data = critChance(b, critChance) ? "crit" : null;
        }
        if(b instanceof CritBullet c){
            if(isCrit(b)){
                c.splash = splashDamage * critMultiplier;
            }else{
                c.splash = splashDamage;
            }
        }
        if(isCrit(b)) b.damage *= critMultiplier;

        super.init(b);
    }

    @Override
    public void updateTrail(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new Trail(trailLength);
            }
            b.trail.length = trailLength;
            b.trail.update(b.x, b.y, trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
        }
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(Mathf.chanceDelta(1) && isCrit(b)){
            critEffect.at(b.x, b.y, b.rotation(), b.team.color);
        }
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc other, float initialHealth){
        super.hitEntity(b, other, initialHealth);
        bounce(b);
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
        super.hitTile(b, build, x, y, initialHealth, direct);
        if(makePlaFire && plaFireChance > 0){
            PlasmaFire.createChance(x, y, splashDamageRadius, plaFireChance, b.team);
        }
        if(direct){
            bounce(b);
        }
    }

    @Override
    public void despawned(Bullet b){
        b.fdata = 1f;
        super.despawned(b);
    }


    @Override
    public void hit(Bullet b, float x, float y){
        if(b.fdata != 1f || despawnHitEffects){
            hitEffect.at(x, y, b.rotation(), hitColor);
            hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
        }

        Effect.shake(hitShake, hitShake, b);

        if(fragOnHit){
            createFrags(b, x, y);
        }
        createPuddles(b, x, y);
        createIncend(b, x, y);

        if(suppressionRange > 0){
            //bullets are pooled, require separate Vec2 instance
            Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, new Vec2(b.x, b.y));
        }

        createSplashDamage(b, x, y);

        for(int i = 0; i < lightning; i++){
            Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone / 2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
        }
    }

    public void createSplashDamage(Bullet b, float x, float y){
        if(splashDamageRadius > 0 && !b.absorbed){

            if(!(b instanceof CritBullet c)) return;

            Damage.damage(b.team, x, y, splashDamageRadius,
            c.splash * b.damageMultiplier(), splashDamagePierce, collidesAir, collidesGround, scaledSplashDamage, b);

            if(status != StatusEffects.none){
                Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
            }

            if(heals()){
                indexer.eachBlock(b.team, x, y, splashDamageRadius, Building::damaged, other -> {
                    healEffect.at(other.x, other.y, 0f, healColor, other.block);
                    other.heal(healPercent / 100f * other.maxHealth() + healAmount);
                });
            }

            if(makePlaFire && plaFireChance > 0){
                PlasmaFire.createChance(x, y, splashDamageRadius, plaFireChance, b.team);
            }else if(plaFireChance > 1){
                indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> PlasmaFire.create(other.tile));
            }

            if(makeFire){
                indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
            }
        }
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        if(fragBullet != null){
            for(int i = 0; i < fragBullets; i++){
                float len = Mathf.random(1f, 7f);
                float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + fragSpread * i - (fragBullets - 1) * fragSpread / 2f;
                ;
                Bullet f = fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                if(f.type instanceof CritBulletType) f.data = b.data;
            }
            b.frags++;
        }
    }

    public void bounce(Bullet b){
        if(bouncing){
            Teamc target = Units.closestTarget(b.team, b.x, b.y, range * b.fout(),
            e -> e.isValid() && e.checkTarget(collidesAir, collidesGround) && !b.collided.contains(e.id),
            t -> t.isValid() && collidesGround && !b.collided.contains(t.id)
            );
            if(target != null){
                b.vel.setAngle(b.angleTo(target));
            }
        }
    }

    public boolean isCrit(Bullet b){
        return b.data instanceof String;
    }

    @Override
    public @Nullable Bullet create(
    @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
    float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
    ){
        CritBullet bullet = CritBullet.create();

        return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
    }

    public static class CritBullet extends Bullet{
        public float splash;

        public static CritBullet create(){
            return Pools.obtain(CritBullet.class, CritBullet::new);
        }

        @Override
        public void reset(){
            super.reset();
            splash = 0;
        }
    }
}
