package wh.entities.bullet.laser;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
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
import wh.entities.bullet.*;
import wh.graphics.*;
import wh.util.*;

import static mindustry.Vars.indexer;

public class SizeDamageBullet extends EffectBulletType{

    public float maxDamageMultiple;
    public float maxHitSizeScale=UnitTypes.sei.hitSize;
    public Interp damageInterp = Interp.linear;
    public float hitSizeLightingScale = 0;
    public float hitSizeDamage;
    public Color hitSizeColor;
    public getB getSizeDamageCreate = b -> {
    };


    public interface getB{
        void getBullet(BulletType b);
    }

    protected BulletType sizeDamageCreate = new EffectBulletType(15){
        {
            hittable = false;
            scaledSplashDamage = true;
            collidesTiles = collidesGround = collides = collidesAir = true;
            hitColor= lightColor = lightningColor = trailColor ;
            hitShake = despawnShake = 5;
        }

        @Override
        public void init(){
            super.init();
            hitSizeColor = hitColor;
            hitEffect = despawnEffect;
        }

        @Override
        public void createSplashDamage(Bullet b, float x, float y){
            if(splashDamageRadius > 0 && !b.absorbed){
                float hitSize = (float)b.data;
                float dynamic = (1 + maxDamageMultiple*damageInterp.apply(Mathf.clamp(hitSize / maxHitSizeScale, 0, 1)));
                Damage.damage(b.team, x, y, splashDamageRadius * Mathf.clamp(dynamic, 1, maxDamageMultiple * 0.5f), splashDamage * dynamic * b.damageMultiplier(), splashDamagePierce, collidesAir, collidesGround, scaledSplashDamage, b);

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
        public void hit(Bullet b, float x, float y){
            hitEffect.at(x, y, b.rotation(), this.hitColor);
            hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
            Effect.shake(hitShake, hitShake, b);

            if(fragOnHit){
                createFrags(b, x, y);
            }
            createSplashDamage(b, x, y);

            float hitSize = (float)b.data;
            float dynamic = (1 + hitSizeLightingScale*damageInterp.apply(Mathf.clamp(hitSize / maxHitSizeScale, 0, 1)));

            for(int i = 0; i < lightning; ++i){
                Lightning.create(b, lightningColor, this.lightningDamage < 0.0F ? damage * dynamic :  this.lightningDamage * dynamic, b.x, b.y,
                b.rotation() + Mathf.range(lightningCone / 2.0F) + lightningAngle,
                lightningLength + Mathf.random(lightningLengthRand));
            }

            if(fragOnHit){
                if(delayFrags && fragBullet != null && fragBullet.delayFrags){
                    Time.run(0f, () -> createFrags(b, x, y));
                }else{
                    createFrags(b, x, y);
                }
            }
            createPuddles(b, x, y);
            createIncend(b, x, y);
            createUnits(b, x, y);

            if(suppressionRange > 0){
                //bullets are pooled, require separate Vec2 instance
                Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, new Vec2(b.x, b.y), suppressColor);
            }
        }
    };

    public SizeDamageBullet(){
        lifetime = 40;
        despawnHit = true;
        hitColor = WHPal.WHYellow;
        splashDamageRadius = 36;
        /*  hitSound = Sounds.explosionbig;*/

        scaledSplashDamage = true;
        collidesAir = this.collidesGround = this.collidesTiles = true;
    }
    @Override
    public void draw(Bullet b){
        if(!(b instanceof SizeDamage sizeDamage)) return;
        Seq<Sized> data = sizeDamage.hitSizes;

        Draw.color(b.team.color, Color.white, b.fin() * 0.7f);
        Draw.alpha(b.fin(Interp.pow3Out) * 1.1f);
        Lines.stroke(2 * b.fout());
        for(Sized s : data){
            if(s instanceof Building){
                Fill.square(s.getX(), s.getY(), s.hitSize() / 2);
            }else{
                Lines.spikes(s.getX(), s.getY(), s.hitSize() * (0.5f + b.fout() * 2f),
                s.hitSize() / 2f * b.fslope() + 12 * b.fin(), 4, 45);
            }
        }

        Drawf.light(b.x, b.y, b.fdata, hitColor, 0.3f + b.fin() * 0.8f);
    }

    public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
        float size = Math.min(s.hitSize(), 75);
        if(Mathf.chance(0.32) || data.size < 8){
            float sd = Mathf.random(size * 3f, size * 12f);
            WHFx.shuttleDark.at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), 45, b.team.color, sd);
        }
    }

    @Override
    public void hit(Bullet b, float x, float y){
        super.hit(b, x, y);

        if(!(b instanceof SizeDamage sizeDamage)) return;
        Seq<Sized> data = sizeDamage.hitSizes;
        for(Sized s : data){
            dynamicHitEffect(s, data, b);
            float dynamic = (1 + maxDamageMultiple*damageInterp.apply(Mathf.clamp(s.hitSize() / maxHitSizeScale, 0, 1)));
            sizeDamage.splash = splashDamage * dynamic;
            float dynamicLighting = (1 + hitSizeLightingScale*damageInterp.apply(Mathf.clamp(s.hitSize() / maxHitSizeScale, 0, 1)));
            sizeDamage.sizeLightingDamage = lightningDamage * dynamicLighting;
            hitSize(b, b.team, s.getX(), s.getY(),dynamic, s.hitSize());
        }

        createSplashDamage(b, b.x, b.y);
    }

    @Override
    public void createSplashDamage(Bullet b, float x, float y){
        if(!(b instanceof SizeDamage sizeDamage)) return;
        if(splashDamageRadius > 0 && !b.absorbed){
            Damage.damage(b.team, x, y, splashDamageRadius, sizeDamage.splash * b.damageMultiplier(), splashDamagePierce, collidesAir, collidesGround, scaledSplashDamage, b);

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

    public void hitSize(Bullet b, Team team, float x, float y,float dynamic, float data){
        if(!(b instanceof SizeDamage sizeDamage)) return;
        for(int i = 0; i < lightning; i++){
            Lightning.create(team, team.color, sizeDamage.sizeLightingDamage, x, y, Mathf.random(360),
            lightningLength + Mathf.random(lightningLengthRand));
        }
        sizeDamageCreate.create(b.owner, team, x, y, 0, hitSizeDamage * dynamic, 1, 1, data);
        getSizeDamageCreate.getBullet(sizeDamageCreate);
    }

    @Override
    public void update(Bullet b){
        super.update(b);

        if(!(b instanceof SizeDamage sizeDamage) || b.timer(0, 5)) return;
        Seq<Sized> data = sizeDamage.hitSizes;
        data.remove(d -> !((Healthc)d).isValid());
        data.removeAll(e -> e == null || !((Healthc)e).isAdded());
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        if(!(b instanceof SizeDamage sizeDamage)) return;
        sizeDamage.splash = splashDamage;
        sizeDamage.sizeLightingDamage = lightningDamage;
        b.fdata = splashDamageRadius;

        Vars.indexer.eachBlock(null, b.x, b.y, b.fdata, bu -> bu.team != b.team, building -> sizeDamage.hitSizes.add(building));

        Groups.unit.intersect(b.x - b.fdata / 2, b.y - b.fdata / 2, b.fdata, b.fdata, u -> {
            if(u.team != b.team) sizeDamage.hitSizes.add(u);
        });

    }

    @Override
    public @Nullable Bullet create(
    @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
    float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
    ){
        SizeDamage bullet = SizeDamage.create();
        bullet.hitSizes.clear();
        return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
    }

    public static class SizeDamage extends Bullet{

        public Seq<Sized> hitSizes = new Seq<>();
        public float splash;
        public float sizeLightingDamage;

        @Override
        public void reset(){
            super.reset();
            splash = sizeLightingDamage = 0;
            hitSizes.clear();
        }

        public static SizeDamage create(){
            return Pools.obtain(SizeDamage.class, SizeDamage::new);
        }
    }
}
