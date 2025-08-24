//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.content;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.*;

import arc.util.pooling.*;
import mindustry.Vars;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import mindustry.gen.Healthc;
import mindustry.gen.Hitboxc;
import mindustry.gen.Sounds;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.graphics.*;
import wh.entities.bullet.*;
import wh.entities.bullet.laser.*;
import wh.gen.*;
import wh.graphics.Drawn;
import wh.graphics.PositionLightning;
import wh.graphics.WHPal;
import wh.math.WHInterp;
import wh.util.WHUtils;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.circleVertices;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static mindustry.gen.Sounds.plasmaboom;
import static wh.content.WHFx.*;
import static wh.core.WarHammerMod.name;

public final class WHBullets{
    public static BulletType basicMissile;
    public static BulletType boidMissle;
    public static BulletType sapArtilleryFrag;
    public static BulletType continuousSapLaser;
    public static BulletType ancientArtilleryProjectile;
    public static BulletType executor;
    public static BulletType ultFireball;
    public static BulletType basicSkyFrag;
    public static BulletType annMissile;
    public static BulletType vastBulletStrafeLaser;
    public static BulletType vastBulletAccel;
    public static BulletType vastBulletLightningBall;
    public static BulletType hyperBlast;
    public static BulletType hyperBlastLinker;
    public static BulletType arc9000frag;
    public static BulletType arc9000;
    public static BulletType arc9000hyper;

    public static BulletType PlasmaFireBall;
    //建筑破坏
    public static BulletType sealedPromethiumMillBreak;
    //单位
    public static BulletType hitter;
    public static BulletType ncBlackHole;
    public static BulletType nuBlackHole;
    public static BulletType warpBreak;
    public static BulletType AGFrag;
    public static BulletType tankAG7;
    public static BulletType air5Missile;
    //建筑
    public static BulletType collaspsePf;
    public static BulletType collapseSp;
    public static BulletType SK;

    //空袭
    public static BulletType airRaiderMissile;
    public static BulletType airRaiderBomb;

    //炮塔
    public static BulletType CrushBulletLead;
    public static BulletType CrushBulletMetaGlass;
    public static BulletType CrushBulletTiSteel;

    public static BulletType AutoGunGraphite;
    public static BulletType AutoGunSilicon;
    public static BulletType AutoGunPyratite;
    public static BulletType AutoGunTiSteel;

    public static BulletType LcarusBullet;
    public static BulletType LcarusBulletEnhanced;

    public static BulletType SSWordTiSteel;
    public static BulletType SSWordPyratite;
    public static BulletType SSWordSurgeAlloy;
    public static BulletType SSWordPlastanium;

    public static BulletType PreventPyratite;
    public static BulletType PreventThorium;
    public static BulletType PreventTungsten;
    public static BulletType PreventCarbide;

    public static BulletType ShardTungsten;
    public static BulletType ShardMolybdenumAlloy;
    public static BulletType ShardRefineCeramite;

    public static BulletType HeavyHammerThorium;
    public static BulletType HeavyHammerMolybdenumAlloy;

    public static BulletType IonizePhaseFabricBullet;
    public static BulletType IonizeResonantCrystalBullet;


    private WHBullets(){
    }

    public static void load(){
        basicMissile = new MissileBulletType(4.2F, 15.0F){
            {
                homingPower = 0.12F;
                width = 8.0F;
                height = 8.0F;
                shrinkX = shrinkY = 0.0F;
                drag = -0.003F;
                homingRange = 80.0F;
                keepVelocity = false;
                splashDamageRadius = 35.0F;
                splashDamage = 30.0F;
                lifetime = 62.0F;
                trailColor = Pal.missileYellowBack;
                hitEffect = Fx.blastExplosion;
                despawnEffect = Fx.blastExplosion;
                weaveScale = 8.0F;
                weaveMag = 2.0F;
            }
        };
        sapArtilleryFrag = new ArtilleryBulletType(2.3F, 30.0F){
            {
                hitEffect = Fx.sapExplosion;
                knockback = 0.8F;
                lifetime = 70.0F;
                width = height = 20.0F;
                collidesTiles = false;
                splashDamageRadius = 70.0F;
                splashDamage = 60.0F;
                backColor = Pal.sapBulletBack;
                frontColor = lightningColor = Pal.sapBullet;
                lightning = 2;
                lightningLength = 5;
                smokeEffect = Fx.shootBigSmoke2;
                hitShake = 5.0F;
                lightRadius = 30.0F;
                lightColor = Pal.sap;
                lightOpacity = 0.5F;
                status = StatusEffects.sapped;
                statusDuration = 600.0F;
            }
        };
        boidMissle = new BoidBulletType(2.7F, 30.0F){
            {
                damage = 50.0F;
                homingPower = 0.02F;
                lifetime = 500.0F;
                keepVelocity = false;
                shootEffect = Fx.shootHeal;
                smokeEffect = Fx.hitLaser;
                hitEffect = despawnEffect = Fx.hitLaser;
                hitSound = Sounds.none;
                healPercent = 5.5F;
                collidesTeam = true;
                trailColor = Pal.heal;
                backColor = Pal.heal;
            }
        };
        continuousSapLaser = new ContinuousLaserBulletType(60f){
            {
                colors = new Color[]{Pal.sapBulletBack.cpy().a(0.3f), Pal.sapBullet.cpy().a(0.6f), Pal.sapBullet, Color.white};
                length = 190f;
                width = 5f;
                shootEffect = WHFx.sapPlasmaShoot;
                hitColor = lightColor = lightningColor = Pal.sapBullet;
                hitEffect = WHFx.coloredHitSmall;
                status = StatusEffects.sapped;
                statusDuration = 80f;
                lifetime = 180f;
                incendChance = 0f;
                largeHit = false;
            }

            @Override
            public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
                super.hitTile(b, build, x, y, initialHealth, direct);
                if(b.owner instanceof Healthc owner){
                    owner.heal(Math.max(initialHealth - build.health(), 0f) * 0.2f);
                }
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(entity instanceof Healthc h && b.owner instanceof Healthc owner){
                    owner.heal(Math.max(health - h.health(), 0f) * 0.2f);
                }
            }
        };
        ancientArtilleryProjectile = new ShieldBreakerType(7f, 6000f, "missile-large", 7000f){{
            backColor = trailColor = lightColor = lightningColor = hitColor = WHPal.ancientLightMid;
            frontColor = WHPal.ancientLight;
            trailEffect = WHFx.hugeTrail;
            trailParam = 6f;
            trailChance = 0.2f;
            trailInterval = 3;

            lifetime = 200f;
            scaleLife = true;

            trailWidth = 5f;
            trailLength = 55;
            trailInterp = Interp.slope;

            lightning = 6;
            lightningLength = lightningLengthRand = 22;
            splashDamage = damage;
            lightningDamage = damage / 15;
            splashDamageRadius = 120;
            scaledSplashDamage = true;
            despawnHit = true;
            collides = false;

            shrinkY = shrinkX = 0.33f;
            width = 17f;
            height = 55f;

            despawnShake = hitShake = 12f;

            hitEffect = new MultiEffect(WHFx.square(hitColor, 200f, 20, splashDamageRadius + 80, 10), WHFx.lightningHitLarge, WHFx.hitSpark(hitColor, 130, 85, splashDamageRadius * 1.5f, 2.2f, 10f),
            WHFx.subEffect(140, splashDamageRadius + 12, 33, 34f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
                float fout = Interp.pow2Out.apply(1 - fin);
                for(int s : Mathf.signs){
                    Drawf.tri(x, y, 12 * fout, 45 * Mathf.curve(fin, 0, 0.1f) * WHFx.fout(fin, 0.25f), rot + s * 90);
                }
            })));
            despawnEffect = WHFx.circleOut(145f, splashDamageRadius + 15f, 3f);

            shootEffect = new WrapEffect(WHFx.missileShoot, hitColor);//NHFx.blast(hitColor, 45f);
            smokeEffect = WHFx.instShoot(hitColor, frontColor);

            despawnSound = hitSound = Sounds.largeExplosion;

            fragBullets = 22;
            fragBullet = new BasicBulletType(2f, 300f, name("circle-bolt")){{
                width = height = 10f;
                shrinkY = shrinkX = 0.7f;
                backColor = trailColor = lightColor = lightningColor = hitColor = WHPal.ancientLightMid;
                frontColor = WHPal.ancientLight;
                trailEffect = Fx.missileTrail;
                trailParam = 3.5f;
                splashDamage = 80f;
                splashDamageRadius = 40f;

                lifetime = 18f;

                lightning = 2;
                lightningLength = lightningLengthRand = 4;
                lightningDamage = 30f;

                hitSoundVolume /= 2.2f;
                despawnShake = hitShake = 4f;
                despawnSound = hitSound = Sounds.dullExplosion;

                trailWidth = 5f;
                trailLength = 35;
                trailInterp = Interp.slope;

                despawnEffect = WHFx.blast(hitColor, 40f);
                hitEffect = WHFx.hitSparkHuge;
            }};

            fragLifeMax = 5f;
            fragLifeMin = 1.5f;
            fragVelocityMax = 2f;
            fragVelocityMin = 0.35f;
        }};
        hitter = new EffectBulletType(15.0F){
            {
                speed = 0.0F;
                hittable = false;
                scaledSplashDamage = true;
                collidesTiles = collidesGround = collides = collidesAir = true;
                lightningDamage = 1000.0F;
                lightColor = lightningColor = trailColor = hitColor = WHPal.WHYellow;
                lightning = 5;
                lightningLength = 12;
                lightningLengthRand = 16;
                splashDamageRadius = 60.0F;
                hitShake = despawnShake = 20.0F;
                hitSound = despawnSound = Sounds.explosionbig;
                hitEffect = despawnEffect = new MultiEffect(WHFx.square45_8_45, WHFx.hitSparkHuge, WHFx.crossBlast_45);
            }

            public void despawned(Bullet b){
                if(despawnHit){
                    hit(b);
                }else{
                    createUnits(b, b.x, b.y);
                }

                if(!fragOnHit){
                    createFrags(b, b.x, b.y);
                }

                despawnEffect.at(b.x, b.y, b.rotation(), lightColor);
                despawnSound.at(b);
                Effect.shake(despawnShake, despawnShake, b);
            }

            public void hit(Bullet b, float x, float y){
                hitEffect.at(x, y, b.rotation(), lightColor);
                hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
                Effect.shake(hitShake, hitShake, b);
                if(fragOnHit){
                    createFrags(b, x, y);
                }

                createPuddles(b, x, y);
                createIncend(b, x, y);
                createUnits(b, x, y);
                if(suppressionRange > 0.0F){
                    Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0.0F, suppressionEffectChance, new Vec2(b.x, b.y));
                }

                createSplashDamage(b, x, y);

                for(int i = 0; i < lightning; ++i){
                    Lightning.create(b, lightColor, lightningDamage < 0.0F ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone / 2.0F) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
                }

            }
        };

        ncBlackHole = new EffectBulletType(120){
            {
                despawnHit = true;
                splashDamageRadius = 240;

                lightningDamage = 2000;
                lightning = 2;
                lightningLength = 4;
                lightningLengthRand = 8;

                scaledSplashDamage = true;
                collidesAir = collidesGround = collidesTiles = true;
                splashDamage = 300;
                damage = 1000;
            }

            @Override
            public void draw(Bullet b){
                if(!(b.data instanceof Seq)) return;
                Seq<Sized> data = (Seq<Sized>)b.data;

                Draw.color(b.team.color, Color.white, b.fin() * 0.7f);
                Draw.alpha(b.fin(Interp.pow3Out) * 1.1f);
                Lines.stroke(2 * b.fout());
                for(Sized s : data){
                    if(s instanceof Building){
                        Fill.square(s.getX(), s.getY(), s.hitSize() / 2);
                    }else{
                        Lines.spikes(s.getX(), s.getY(), s.hitSize() * (0.5f + b.fout() * 2f), s.hitSize() / 2f * b.fslope() + 12 * b.fin(), 4, 45);
                    }
                }

                Drawf.light(b.x, b.y, b.fdata, b.team.color, 0.3f + b.fin() * 0.8f);
            }

            public void hitT(Sized target, Entityc o, Team team, float x, float y){
                for(int i = 0; i < lightning; i++){
                    Lightning.create(team, team.color, lightningDamage, x, y, Mathf.random(360), lightningLength + Mathf.random(lightningLengthRand));
                }

                if(target instanceof Unit){
                    if(((Unit)target).health > 1000) WHBullets.hitter.create(o, team, x, y, 0);
                }
            }

            @Override
            public void update(Bullet b){
                super.update(b);

                if(!(b.data instanceof Seq)) return;
                //noinspection unchecked
                Seq<Sized> data = (Seq<Sized>)b.data;
                data.remove(d -> !((Healthc)d).isValid());
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);

                float rad = 33;

                Vec2 v = new Vec2().set(b);
                Team t = b.team;

                for(int i = 0; i < 5; i++){
                    Time.run(i * 0.35f + Mathf.random(2), () -> {
                        Tmp.v1.rnd(rad / 3).scl(Mathf.random());
                        WHFx.shuttle.at(v.x + Tmp.v1.x, v.y + Tmp.v1.y, Tmp.v1.angle(), t.color, Mathf.random(rad * 3f, rad * 12f));
                    });
                }

                if(!(b.data instanceof Seq)) return;
                Entityc o = b.owner();
                //noinspection unchecked
                Seq<Sized> data = (Seq<Sized>)b.data;
                for(Sized s : data){
                    float size = Math.min(s.hitSize(), 85);
                    Time.run(Mathf.random(44), () -> {
                        if(Mathf.chance(0.32) || data.size < 8)
                            WHFx.shuttle.at(s.getX(), s.getY(), 45, t.color, Mathf.random(size * 3f, size * 12f));
                        hitT(s, o, t, s.getX(), s.getY());
                    });
                }

                createSplashDamage(b, b.x, b.y);
            }

            @Override
            public void init(Bullet b){
                super.init(b);
                if(!(b.data instanceof Float)) return;
                float fdata = (Float)b.data();

                Seq<Sized> data = new Seq<>();

                Vars.indexer.eachBlock(null, b.x, b.y, fdata, bu -> bu.team != b.team, data::add);

                Groups.unit.intersect(b.x - fdata / 2, b.y - fdata / 2, fdata, fdata, u -> {
                    if(u.team != b.team) data.add(u);
                });

                b.data = data;

                WHFx.circleOut.at(b.x, b.y, fdata * 1.25f, b.team.color);
            }
        };


        nuBlackHole = new EffectBulletType(20){
            {
                despawnHit = true;
                hitColor = WHPal.WHYellow;
                splashDamageRadius = 36;

                lightningDamage = 2000;
                lightning = 2;
                lightningLength = 4;
                lightningLengthRand = 8;

                scaledSplashDamage = true;
                collidesAir = collidesGround = collidesTiles = true;
                splashDamage = 0;
                damage = 10000;
            }

            @Override
            public void draw(Bullet b){
                if(!(b.data instanceof Seq)) return;
                //noinspection unchecked
                Seq<Sized> data = (Seq<Sized>)b.data;

                Draw.color(b.team.color, Color.white, b.fin() * 0.7f);
                Draw.alpha(b.fin(Interp.pow3Out) * 1.1f);
                Lines.stroke(2 * b.fout());
                for(Sized s : data){
                    if(s instanceof Building){
                        Fill.square(s.getX(), s.getY(), s.hitSize() / 2);
                    }else{
                        Lines.spikes(s.getX(), s.getY(), s.hitSize() * (0.5f + b.fout() * 2f), s.hitSize() / 2f * b.fslope() + 12 * b.fin(), 4, 45);
                    }
                }

                Drawf.light(b.x, b.y, b.fdata, hitColor, 0.3f + b.fin() * 0.8f);
            }

            public void hitT(Entityc o, Team team, float x, float y){
                for(int i = 0; i < lightning; i++){
                    Lightning.create(team, team.color, lightningDamage, x, y, Mathf.random(360), lightningLength + Mathf.random(lightningLengthRand));
                }

                WHBullets.hitter.create(o, team, x, y, 0, 3000, 1, 1, null);
            }

            @Override
            public void update(Bullet b){
                super.update(b);

                if(!(b.data instanceof Seq) || b.timer(0, 5)) return;
                //noinspection unchecked
                Seq<Sized> data = (Seq<Sized>)b.data;
                data.remove(d -> !((Healthc)d).isValid());
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);

                float rad = 33;

                if(!(b.data instanceof Seq)) return;
                Entityc o = b.owner();
                Seq<Sized> data = (Seq<Sized>)b.data;
                for(Sized s : data){
                    float size = Math.min(s.hitSize(), 75);
                    if(Mathf.chance(0.32) || data.size < 8){
                        float sd = Mathf.random(size * 3f, size * 12f);

                        WHFx.shuttleDark.at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), 45, b.team.color, sd);
                    }
                    hitT(o, b.team, s.getX(), s.getY());
                }

                createSplashDamage(b, b.x, b.y);
            }

            @Override
            public void init(Bullet b){
                super.init(b);
                b.fdata = splashDamageRadius;

                Seq<Sized> data = new Seq<>();

                Vars.indexer.eachBlock(null, b.x, b.y, b.fdata, bu -> bu.team != b.team, data::add);

                Groups.unit.intersect(b.x - b.fdata / 2, b.y - b.fdata / 2, b.fdata, b.fdata, u -> {
                    if(u.team != b.team) data.add(u);
                });

                b.data = data;

            }
        };

        executor = new TrailFadeBulletType(28.0F, 1800.0F){
            {
                lifetime = 40.0F;
                trailLength = 90;
                trailWidth = 3.6F;
                tracers = 2;
                tracerFadeOffset = 20;
                keepVelocity = true;
                tracerSpacing = 10.0F;
                tracerUpdateSpacing *= 1.25F;
                removeAfterPierce = false;
                hitColor = backColor = lightColor = lightningColor = WHPal.ancient;
                trailColor = WHPal.ancientLightMid;
                frontColor = WHPal.ancientLight;
                width = 18.0F;
                height = 60.0F;
                homingPower = 0.01F;
                homingRange = 300.0F;
                homingDelay = 5.0F;
                hitSound = Sounds.plasmaboom;
                despawnShake = hitShake = 18.0F;
                statusDuration = 1200.0F;
                pierce = pierceArmor = pierceBuilding = true;
                lightning = 3;
                lightningLength = 6;
                lightningLengthRand = 18;
                lightningDamage = 400.0F;
                smokeEffect = new WrapEffect(WHFx.hitSparkHuge, hitColor);
                shootEffect = WHFx.instShoot(backColor, frontColor);
                despawnEffect = WHFx.lightningHitLarge;
                hitEffect = new MultiEffect(new Effect[]{WHFx.hitSpark(backColor, 75.0F, 24, 90.0F, 2.0F, 12.0F), WHFx.square45_6_45, WHFx.lineCircleOut(backColor, 18.0F, 20.0F, 2.0F), WHFx.sharpBlast(backColor, frontColor, 120.0F, 40.0F)});
            }

            public void createFrags(Bullet b, float x, float y){
                super.createFrags(b, x, y);
                WHBullets.nuBlackHole.create(b, x, y, 0.0F);
            }
        };
        basicSkyFrag = new BasicBulletType(3.8F, 50.0F){
            {
                speed = 6.0F;
                trailLength = 12;
                trailWidth = 2.0F;
                lifetime = 60.0F;
                despawnEffect = WHFx.square45_4_45;
                knockback = 4.0F;
                width = 15.0F;
                height = 37.0F;
                lightningDamage = damage * 0.65F;
                backColor = lightColor = lightningColor = trailColor = hitColor = frontColor = Pal.techBlue;
                lightning = 2;
                lightningLength = lightningLengthRand = 3;
                smokeEffect = Fx.shootBigSmoke2;
                trailChance = 0.2F;
                trailEffect = WHFx.skyTrail;
                drag = 0.015F;
                hitShake = 2.0F;
                hitSound = Sounds.explosion;
                hitEffect = new Effect(45.0F, (e) -> {
                    Fx.rand.setSeed(e.id);
                    Draw.color(lightColor, e.fin());
                    Lines.stroke(1.75F * e.fout());
                    Lines.spikes(e.x, e.y, (float)Fx.rand.random(14, 28) * e.finpow(), (float)Fx.rand.random(1, 5) * e.fout() + (float)Fx.rand.random(5, 8) * e.fin(WHInterp.parabola4Reversed), 4, 45.0F);
                    Lines.square(e.x, e.y, (float)Fx.rand.random(4, 14) * e.fin(Interp.pow3Out), 45.0F);
                });
            }

            public void hit(Bullet b){
                super.hit(b);
                PlasmaFire.createChance(b, 12.0F, 0.0075F);
            }
        };
        annMissile = new BasicBulletType(5.6F, 80.0F, "wh-strike"){
            {
                trailColor = lightningColor = backColor = lightColor = frontColor = Pal.techBlue;
                lightning = 3;
                lightningCone = 360.0F;
                lightningLengthRand = lightningLength = 9;
                splashDamageRadius = 60.0F;
                splashDamage = lightningDamage = damage * 0.7F;
                range = 320.0F;
                scaleLife = true;
                width = 12.0F;
                height = 30.0F;
                trailLength = 15;
                drawSize = 250.0F;
                trailParam = 1.4F;
                trailChance = 0.35F;
                lifetime = 50.0F;
                homingDelay = 10.0F;
                homingPower = 0.05F;
                homingRange = 150.0F;
                hitEffect = WHFx.lightningHitLarge(lightColor);
                shootEffect = WHFx.hugeSmokeGray;
                smokeEffect = new Effect(45.0F, (e) -> {
                    Draw.color(lightColor, Color.white, e.fout() * 0.7F);
                    Angles.randLenVectors(e.id, 8, 5.0F + 55.0F * e.fin(), e.rotation, 45.0F, (x, y) -> {
                        Fill.circle(e.x + x, e.y + y, e.fout() * 3.0F);
                    });
                });
                despawnEffect = new Effect(32.0F, (e) -> {
                    Draw.color(Color.gray);
                    Angles.randLenVectors(e.id + 1, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> {
                        Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F);
                    });
                    Draw.color(lightColor, Color.white, e.fin());
                    Lines.stroke(e.fout() * 2.0F);
                    Fill.circle(e.x, e.y, e.fout() * e.fout() * 13.0F);
                    Angles.randLenVectors(e.id, 4, 7.0F + 40.0F * e.fin(), (x, y) -> {
                        Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8.0F + 3.0F);
                    });
                });
            }
        };
        vastBulletAccel = new AccelBulletType(2.0F, 180.0F){
            {
                width = 22.0F;
                height = 40.0F;
                velocityBegin = 1.0F;
                velocityIncrease = 11.0F;
                accelInterp = WHInterp.inOut;
                accelerateBegin = 0.045F;
                accelerateEnd = 0.675F;
                pierceCap = 3;
                splashDamage = damage / 4.0F;
                splashDamageRadius = 24.0F;
                trailLength = 30;
                trailWidth = 3.0F;
                lifetime = 160.0F;
                trailEffect = WHFx.trailFromWhite;
                pierceArmor = true;
                trailRotation = false;
                trailChance = 0.35F;
                trailParam = 4.0F;
                homingRange = 640.0F;
                homingPower = 0.075F;
                homingDelay = 5.0F;
                lightning = 3;
                lightningLengthRand = 10;
                lightningLength = 5;
                lightningDamage = damage / 4.0F;
                shootEffect = smokeEffect = Fx.none;
                hitEffect = despawnEffect = new MultiEffect(new Effect(65.0F, (b) -> {
                    Draw.color(b.color);
                    Fill.circle(b.x, b.y, 6.0F * b.fout(Interp.pow3Out));
                    Angles.randLenVectors((long)b.id, 6, 35.0F * b.fin() + 5.0F, (x, y) -> {
                        Fill.circle(b.x + x, b.y + y, 4.0F * b.fout(Interp.pow2Out));
                    });
                }), WHFx.hitSparkLarge);
                despawnHit = false;
                rangeOverride = 480.0F;
            }


            public void update(Bullet b) {
                super.update(b);
            }

            public void updateTrailEffects(Bullet b) {
                if (trailChance > 0f) {
                    if (Mathf.chanceDelta(trailChance)) {
                        trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
                    }
                }

                if (trailInterval > 0f) {
                    if (b.timer(0, trailInterval)) {
                        trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
                    }
                }
            }

            @Override
            public void hit(Bullet b, float x, float y) {
                b.hit = true;
                hitEffect.at(x, y, b.rotation(), b.team.color);
                hitSound.at(x, y, hitSoundPitch, hitSoundVolume);

                Effect.shake(hitShake, hitShake, b);

                if (splashDamageRadius > 0f && !b.absorbed) {
                    Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);

                    if (status != StatusEffects.none) {
                        Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
                    }
                }

                for (int i = 0; i < lightning; i++) Lightning.create(b, b.team.color, lightningDamage < 0f ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));

                if (!(b.owner instanceof Unit from)) return;
                if (from.dead || !from.isAdded() || from.healthf() > 0.99f) return;
                WHFx.chainLightningFade.at(b.x, b.y, Mathf.random(12, 20), b.team.color, from);
                from.heal(damage / 8f);
            }

            @Override
            public void despawned(Bullet b) {
                despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);
                Effect.shake(despawnShake, despawnShake, b);
            }

            @Override
            public void removed(Bullet b) {
                if (trailLength > 0 && b.trail != null && b.trail.size() > 0) {
                    Fx.trailFade.at(b.x, b.y, trailWidth, b.team.color, b.trail.copy());
                }
            }

            @Override
            public void init(Bullet b) {
                super.init(b);

                b.vel.rotate(WHUtils.rand(b.id).random(360));
            }

            @Override
            public void draw(Bullet b) {
                Tmp.c1.set(b.team.color).lerp(Color.white, Mathf.absin(4f, 0.3f));

                if (trailLength > 0 && b.trail != null) {
                    float z = Draw.z();
                    Draw.z(z - 0.01f);
                    b.trail.draw(Tmp.c1, trailWidth);
                    Draw.z(z);
                }

                Draw.color(b.team.color, Color.white, 0.35f);
                Drawn.arrow(b.x, b.y, 5f, 35f, -6f, b.rotation());
                Draw.color(Tmp.c1);
                Drawn.arrow(b.x, b.y, 5f, 35f, 12f, b.rotation());

                Draw.reset();
            }
        };


        vastBulletLightningBall = new LightningLinkerBulletType(3f, 120f) {{
            lifetime = 120f;
            keepVelocity = false;
            lightningDamage = damage = splashDamage = 80f;
            splashDamageRadius = 50f;
            homingDelay = 20f;
            homingRange = 300f;
            homingPower = 0.025f;
            smokeEffect = shootEffect = Fx.none;
            effectLingtning = 0;
            maxHit = 6;
            hitShake = despawnShake = 5f;
            hitSound = despawnSound = Sounds.plasmaboom;
            size = 7.2f;
            trailWidth = 3f;
            trailLength = 16;
            linkRange = 80f;
            scaleLife = false;
            despawnHit = false;
            collidesAir = collidesGround = true;
            despawnEffect = hitEffect = new MultiEffect(WHFx.lightningHitLarge, WHFx.hitSparkHuge);
            trailEffect = slopeEffect = WHFx.trailFromWhite;
            spreadEffect = new Effect(32f, e -> Angles.randLenVectors(e.id, 2, 6 + 45 * e.fin(), (x, y) -> {
                Draw.color(e.color);
                Fill.circle(e.x + x, e.y + y, e.fout() * size / 2f);
                Draw.color(Color.black);
                Fill.circle(e.x + x, e.y + y, e.fout() * (size / 3f - 1f));
            })).layer(Layer.effect + 0.00001f);
        }
            public final Effect RshootEffect = new Effect(24f, e -> {
                e.scaled(10f, (b) -> {
                    Draw.color(e.color);
                    Lines.stroke(b.fout() * 3f + 0.2f);
                    Lines.circle(b.x, b.y, b.fin() * 70f);
                });
                Draw.color(e.color);

                for (int i : Mathf.signs) {
                    Drawn.tri(e.x, e.y, 8f * e.fout(), 85f, e.rotation + 90f * i);
                }

                Draw.color(Color.black);

                for (int i : Mathf.signs) {
                    Drawn.tri(e.x, e.y, 3f * e.fout(), 38f, e.rotation + 90f * i);
                }
            });

            public final Effect RsmokeEffect = WHFx.hitSparkLarge;

            public Color getColor(Bullet b) {
                return Tmp.c1.set(b.team.color).lerp(Color.white, 0.1f + Mathf.absin(4f, 0.15f));
            }

            @Override
            public void update(Bullet b) {
                updateTrail(b);
                updateHoming(b);
                updateWeaving(b);
                updateBulletInterval(b);

                Effect.shake(hitShake, hitShake, b);
                if (b.timer(5, hitSpacing)) {
                    slopeEffect.at(b.x + Mathf.range(size / 4f), b.y + Mathf.range(size / 4f), Mathf.random(2f, 4f), b.team.color);
                    spreadEffect.at(b.x, b.y, b.team.color);
                    PositionLightning.createRange(b, collidesAir, collidesGround, b, b.team, linkRange, maxHit, b.team.color,
                    Mathf.chanceDelta(randomLightningChance), lightningDamage, lightningLength, PositionLightning.WIDTH, boltNum,
                    p -> liHitEffect.at(p.getX(), p.getY(), b.team.color));
                }

                if (Mathf.chanceDelta(0.1)) {
                    slopeEffect.at(b.x + Mathf.range(size / 4f), b.y + Mathf.range(size / 4f), Mathf.random(2f, 4f), b.team.color);
                    spreadEffect.at(b.x, b.y, b.team.color);
                }

                if (randomGenerateRange > 0f && Mathf.chance(Time.delta * randomGenerateChance) && b.lifetime - b.time > PositionLightning.lifetime)
                    PositionLightning.createRandomRange(b, b.team, b, randomGenerateRange, backColor, Mathf.chanceDelta(randomLightningChance),
                    0, 0, boltWidth, boltNum, randomLightningNum, hitPos -> {
                    randomGenerateSound.at(hitPos, Mathf.random(0.9f, 1.1f));
                    Damage.damage(b.team, hitPos.getX(), hitPos.getY(), splashDamageRadius / 8, splashDamage * b.damageMultiplier() / 8, collidesAir, collidesGround);
                    WHFx.lightningHitLarge.at(hitPos.getX(), hitPos.getY(), b.team.color);

                    hitModifier.get(hitPos);
                });

                if (Mathf.chanceDelta(effectLightningChance) && b.lifetime - b.time > Fx.chainLightning.lifetime) {
                    for (int i = 0; i < effectLingtning; i++) {
                        Vec2 v = randVec.rnd(effectLightningLength + Mathf.random(effectLightningLengthRand)).add(b).add(Tmp.v1.set(b.vel).scl(Fx.chainLightning.lifetime / 2));
                        Fx.chainLightning.at(b.x, b.y, 12f, b.team.color, v.cpy());
                        WHFx.lightningHitSmall.at(v.x, v.y, 20f, b.team.color);
                    }
                }
            }

            @Override
            public void init(Bullet b) {
                super.init(b);

                b.lifetime *= Mathf.randomSeed(b.id, 0.875f , 1.125f);

                RsmokeEffect.at(b.x, b.y, b.team.color);
                RshootEffect.at(b.x, b.y, b.rotation(), b.team.color);
            }

            @Override
            public void drawTrail(Bullet b) {
                if (trailLength > 0 && b.trail != null) {
                    float z = Draw.z();
                    Draw.z(z - 0.0001f);
                    b.trail.draw(getColor(b), trailWidth);
                    Draw.z(z);
                }
            }

            @Override
            public void draw(Bullet b) {
                drawTrail(b);

                Draw.color(Tmp.c1);
                Fill.circle(b.x, b.y, size);

                float[] param = {
                9f, 28f, 1f,
                9f, 22f, -1.25f,
                12f, 16f, -0.45f,
                };

                for (int i = 0; i < param.length / 3; i++) {
                    for (int j : Mathf.signs) {
                        Drawf.tri(b.x, b.y, param[i * 3] * b.fout(), param[i * 3 + 1] * b.fout(), b.rotation() + 90f * j + param[i * 3 + 2] * Time.time);
                    }
                }

                Draw.color(Color.black);

                Fill.circle(b.x, b.y, size / 6.125f + size / 3 * Mathf.curve(b.fout(), 0.1f, 0.35f));

                Drawf.light(b.x, b.y, size * 6.85f, b.team.color, 0.7f);
            }

            @Override
            public void despawned(Bullet b) {
                PositionLightning.createRandomRange(b, b.team, b, randomGenerateRange, b.team.color, Mathf.chanceDelta(randomLightningChance), 0, 0, boltWidth, boltNum, randomLightningNum, hitPos -> {
                    Damage.damage(b.team, hitPos.getX(), hitPos.getY(), splashDamageRadius, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
                    WHFx.lightningHitLarge.at(hitPos.getX(), hitPos.getY(), b.team.color);
                    liHitEffect.at(hitPos);
                    for (int j = 0; j < lightning; j++) {
                        Lightning.create(b, b.team.color, lightningDamage < 0f ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone / 2f) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
                    }
                    hitSound.at(hitPos, Mathf.random(0.9f, 1.1f));

                    hitModifier.get(hitPos);
                });

                if (despawnHit) {
                    hit(b);
                } else {
                    createUnits(b, b.x, b.y);
                }

                if (!fragOnHit) {
                    createFrags(b, b.x, b.y);
                }

                despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);
                despawnSound.at(b);

                Effect.shake(despawnShake, despawnShake, b);
            }

            @Override
            public void hit(Bullet b, float x, float y) {
                hitEffect.at(x, y, b.rotation(), b.team.color);
                hitSound.at(x, y, hitSoundPitch, hitSoundVolume);

                Effect.shake(hitShake, hitShake, b);

                if (fragOnHit) {
                    createFrags(b, x, y);
                }
                createPuddles(b, x, y);
                createIncend(b, x, y);
                createUnits(b, x, y);

                if (suppressionRange > 0) {
                    //bullets are pooled, require separate Vec2 instance
                    Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, new Vec2(b.x, b.y));
                }

                createSplashDamage(b, x, y);

                for (int i = 0; i < lightning; i++) {
                    Lightning.create(b, b.team.color, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone/2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
                }
            }

            @Override
            public void removed(Bullet b) {
                if (trailLength > 0 && b.trail != null && b.trail.size() > 0) {
                    Fx.trailFade.at(b.x, b.y, trailWidth, b.team.color, b.trail.copy());
                }
            }
        };
        vastBulletStrafeLaser = new StrafeLaser( 300f) {{
            strafeAngle = 0;
        }
            public void init(Bullet b) {
                super.init(b);
                Sounds.laserblast.at(b);
            }

            public void hit(Bullet b, float x, float y) {
                super.hit(b, x, y);
                if (b.owner instanceof Unit from) {
                    if (from.dead || !from.isAdded() || from.healthf() > 0.99f) return;

                    from.heal(damage / 20f);
                    if (headless) return;

                    PositionLightning.createEffect(b, from, b.team.color, 2, Mathf.random(1.5f, 3f));
                }
            }

            public void draw(Bullet b) {
                Tmp.c1.set(b.team.color).lerp(Color.white, Mathf.absin(4f, 0.1f));
                super.draw(b);
                Draw.z(110f);
                float fout = b.fout(0.25f) * Mathf.curve(b.fin(), 0f, 0.125f);
                Draw.color(Tmp.c1);
                Fill.circle(b.x, b.y, width / 1.225f * fout);
                if (b.owner instanceof Unit unit) {
                    if (!unit.dead) {
                        Draw.z(100f);
                        Lines.stroke((width / 3f + Mathf.absin(Time.time, 4f, 0.8f)) * fout);
                        Lines.line(b.x, b.y, unit.x, unit.y, false);
                    }
                }

                for (int i : Mathf.signs) {
                    Drawn.tri(b.x, b.y, 6f * fout, 10f + 50f * fout, Time.time * 1.5f + (float) (90 * i));
                    Drawn.tri(b.x, b.y, 6f * fout, 20f + 60f * fout, Time.time * -1f + (float) (90 * i));
                }

                Draw.z(110.001f);
                Draw.color(b.team.color, Color.white, 0.25f);
                Fill.circle(b.x, b.y, width / 1.85f * fout);
                Draw.color(Color.black);
                Fill.circle(b.x, b.y, width / 2.155f * fout);
                Draw.z(100f);
                Draw.reset();
                float rotation = dataRot ? b.fdata : b.rotation() + getRotation(b);
                float maxRangeFout = maxRange * fout;
                float realLength = WHUtils.findLaserLength(b, rotation, maxRangeFout);
                Tmp.v1.trns(rotation, realLength);
                Tmp.v2.trns(rotation, 0f, width / 2f * fout);
                Tmp.v3.setZero();
                if (realLength < maxRangeFout) {
                    Tmp.v3.set(Tmp.v2).scl((maxRangeFout - realLength) / maxRangeFout);
                }

                Draw.color(Tmp.c1);
                Tmp.v2.scl(0.9f);
                Tmp.v3.scl(0.9f);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
                if (realLength < maxRangeFout) {
                    Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
                }

                Tmp.v2.scl(1.2f);
                Tmp.v3.scl(1.2f);
                Draw.alpha(0.5f);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y,
                b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
                if (realLength < maxRangeFout) {
                    Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
                }

                Draw.alpha(1f);
                Draw.color(Color.black);
                Draw.z(Draw.z() + 0.01f);
                Tmp.v2.scl(0.5f);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y,
                b.x + (Tmp.v1.x + Tmp.v3.x) / 3f, b.y + (Tmp.v1.y + Tmp.v3.y) / 3f, b.x + (Tmp.v1.x - Tmp.v3.x) / 3f, b.y + (Tmp.v1.y - Tmp.v3.y) / 3f);
                Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.5f, getColor(b), 0.7f);
                Draw.reset();
                Draw.z(Draw.z() - 0.01f);
            }
        };
        hyperBlast = new BasicBulletType(3.3F, 400.0F){
            {
                lifetime = 60.0F;
                trailLength = 15;
                drawSize = 250.0F;
                drag = 0.0075F;
                despawnEffect = hitEffect = WHFx.lightningHitLarge(Pal.techBlue);
                knockback = 12.0F;
                width = 15.0F;
                height = 37.0F;
                splashDamageRadius = 40.0F;
                splashDamage = lightningDamage = damage * 0.75F;
                backColor = lightColor = lightningColor = trailColor = Pal.techBlue;
                frontColor = Color.white;
                lightning = 3;
                lightningLength = 8;
                smokeEffect = Fx.shootBigSmoke2;
                trailChance = 0.6F;
                trailEffect = WHFx.trailToGray;
                hitShake = 3.0F;
                hitSound = Sounds.plasmaboom;
            }
        };
        hyperBlastLinker = new LightningLinkerBulletType(5.0F, 220.0F){
            {
                effectLightningChance = 0.15F;
                backColor = trailColor = lightColor = lightningColor = hitColor = Pal.techBlue;
                size = 8.0F;
                frontColor = Pal.techBlue.cpy().lerp(Color.white, 0.25F);
                range = 200.0F;
                trailWidth = 8.0F;
                trailLength = 20;
                linkRange = 280.0F;
                maxHit = 8;
                drag = 0.085F;
                hitSound = Sounds.explosionbig;
                splashDamageRadius = 120.0F;
                splashDamage = lightningDamage = damage / 4.0F;
                lifetime = 50.0F;
                scaleLife = false;
                despawnEffect = WHFx.lightningHitLarge(hitColor);
                hitEffect = new MultiEffect(new Effect[]{WHFx.hitSpark(backColor, 65.0F, 22, splashDamageRadius, 4.0F, 16.0F), WHFx.blast(backColor, splashDamageRadius / 2.0F)});
                shootEffect = WHFx.hitSpark(backColor, 45.0F, 12, 60.0F, 3.0F, 8.0F);
                smokeEffect = WHFx.hugeSmokeGray;
            }
        };
        arc9000frag = new FlakBulletType(3.75F, 200.0F){
            {
                trailColor = lightColor = lightningColor = backColor = frontColor = Pal.techBlue;
                trailLength = 14;
                trailWidth = 2.7F;
                trailRotation = true;
                trailInterval = 3.0F;
                trailEffect = WHFx.polyTrail(backColor, frontColor, 4.65F, 22.0F);
                trailChance = 0.0F;
                despawnEffect = hitEffect = WHFx.techBlueExplosion;
                knockback = 12.0F;
                lifetime = 90.0F;
                width = 17.0F;
                height = 42.0F;
                hittable = false;
                collidesTiles = false;
                splashDamageRadius = 60.0F;
                splashDamage = damage * 0.6F;
                lightning = 3;
                lightningLength = 8;
                smokeEffect = Fx.shootBigSmoke2;
                hitShake = 8.0F;
                hitSound = Sounds.plasmaboom;
                status = StatusEffects.shocked;
            }
        };
        arc9000 = new LightningLinkerBulletType(2.75F, 200.0F){
            {
                trailWidth = 4.5F;
                trailLength = 66;
                chargeEffect = new MultiEffect(new Effect[]{WHFx.techBlueCharge, WHFx.techBlueChargeBegin});
                spreadEffect = slopeEffect = Fx.none;
                trailEffect = WHFx.hitSparkHuge;
                trailInterval = 5.0F;
                backColor = trailColor = hitColor = lightColor = lightningColor = frontColor = Pal.techBlue;
                randomGenerateRange = 340.0F;
                randomLightningNum = 3;
                linkRange = 280.0F;
                range = 800.0F;
                drawSize = 500.0F;
                drag = 0.0035F;
                fragLifeMin = 0.3F;
                fragLifeMax = 1.0F;
                fragVelocityMin = 0.3F;
                fragVelocityMax = 1.25F;
                fragBullets = 14;
                intervalBullets = 2;
                intervalBullet = fragBullet = WHBullets.arc9000frag;
                hitSound = Sounds.explosionbig;
                splashDamageRadius = 120.0F;
                splashDamage = 1000.0F;
                lightningDamage = 375.0F;
                hittable = false;
                collidesTiles = true;
                pierce = false;
                collides = false;
                ammoMultiplier = 1.0F;
                lifetime = 300.0F;
                despawnEffect = WHFx.circleOut(hitColor, splashDamageRadius * 1.5F);
                hitEffect = WHFx.largeTechBlueHit;
                shootEffect = WHFx.techBlueShootBig;
                smokeEffect = WHFx.techBlueSmokeBig;
                hitSpacing = 3.0F;
            }

            public void update(Bullet b){
                super.update(b);
                if(b.timer(1, 6.0F)){
                    for(int j = 0; j < 2; ++j){
                        Drawn.randFadeLightningEffect(b.x, b.y, (float)Mathf.random(360), (float)Mathf.random(7, 12), backColor, Mathf.chance(0.5));
                    }
                };
            }

            public void draw(Bullet b){
                Draw.color(backColor);
                Drawn.surround((long)b.id, b.x, b.y, size * 1.45F, 14, 7.0F, 11.0F, (b.fin(WHInterp.parabola4Reversed) + 1.0F) / 2.0F * b.fout(0.1F));
                drawTrail(b);
                Draw.color(backColor);
                Fill.circle(b.x, b.y, size);
                Draw.z(110.0001F);
                Draw.color(frontColor);
                Fill.circle(b.x, b.y, size * 0.62F);
                Draw.z(99.89F);
                Draw.color(frontColor);
                Fill.circle(b.x, b.y, size * 0.66F);
                Draw.z(100.0F);
                Drawf.light(b.x, b.y, size * 1.85F, backColor, 0.7F);
            }
        };
        arc9000hyper = new AccelBulletType(10f, 1000f){
            {
                lifetime = 120f;
                drawSize = 1200f;
                width = height = shrinkX = shrinkY = 0;
                collides = false;
                despawnHit = false;
                collidesAir = collidesGround = collidesTiles = true;
                splashDamage = 4000f;

                velocityBegin = 6f;
                velocityIncrease = -5.9f;

                accelerateEnd = 0.75f;
                accelerateBegin = 0.1f;

                accelInterp = Interp.pow2;
                trailInterp = Interp.pow10Out;

                despawnSound = Sounds.plasmaboom;
                hitSound = Sounds.explosionbig;
                hitShake = 60;
                despawnShake = 100;
                lightning = 12;
                lightningDamage = 2000f;
                lightningLength = 50;
                lightningLengthRand = 80;

                statusDuration = 1200f;
                //					ammoMultiplier = 0.1f;

                fragBullets = 1;
                fragBullet = WHBullets.arc9000;
                fragVelocityMin = 0.4f;
                fragVelocityMax = 0.6f;
                fragLifeMin = 0.5f;
                fragLifeMax = 0.7f;

                trailWidth = 12F;
                trailLength = 120;
                ammoMultiplier = 1;

                hittable = false;

                /*scaleLife = true;*/
                splashDamageRadius = 400f;
                hitColor = lightColor = lightningColor = trailColor = Pal.techBlue;
                Effect effect = WHFx.crossBlast(hitColor, 420f, 45);
                effect.lifetime += 180;
                despawnEffect = WHFx.circleOut(hitColor, splashDamageRadius);
                hitEffect = new MultiEffect(WHFx.blast(hitColor, 200f), new Effect(180F, 600f, e -> {
                    float rad = 120f;

                    float f = (e.fin(Interp.pow10Out) + 8) / 9 * Mathf.curve(Interp.slowFast.apply(e.fout(0.75f)), 0f, 0.85f);

                    Draw.alpha(0.9f * e.foutpowdown());
                    Draw.color(Color.white, e.color, e.fin() + 0.6f);
                    Fill.circle(e.x, e.y, rad * f);

                    e.scaled(45f, i -> {
                        Lines.stroke(7f * i.fout());
                        Lines.circle(i.x, i.y, rad * 3f * i.finpowdown());
                        Lines.circle(i.x, i.y, rad * 2f * i.finpowdown());
                    });


                    Draw.color(Color.white);
                    Fill.circle(e.x, e.y, rad * f * 0.75f);

                    Drawf.light(e.x, e.y, rad * f * 2f, Draw.getColor(), 0.7f);
                }).layer(Layer.effect + 0.001f)
                , effect, new Effect(260, 460f, e -> {
                    Draw.blend(Blending.additive);
                    Draw.z(Layer.flyingUnit - 0.8f);
                    float radius = e.fin(Interp.pow3Out) * 230;
                    Fill.light(e.x, e.y, circleVertices(radius), radius, Color.clear, Tmp.c1.set(Pal.techBlue).a(e.fout(Interp.pow10Out)));
                    Draw.blend();
                }));
            }

            @Override
            public void draw(Bullet b){
                super.draw(b);

                Draw.color(Pal.techBlue, Color.white, b.fout() * 0.25f);

                float rand = Mathf.randomSeed(b.id, 60f);
                float extend = Mathf.curve(b.fin(Interp.pow10Out), 0.075f, 1f);
                float rot = b.fout(Interp.pow10In);

                float chargeCircleFrontRad = 20;
                float width = chargeCircleFrontRad * 1.2f;
                Fill.circle(b.x, b.y, width * (b.fout() + 4) / 3.5f);

                float rotAngle = b.fdata;

                for(int i : Mathf.signs){
                    Drawn.tri(b.x, b.y, width * b.foutpowdown(), 200 + 570 * extend, rotAngle + 90 * i - 45);
                }

                for(int i : Mathf.signs){
                    Drawn.tri(b.x, b.y, width * b.foutpowdown(), 200 + 570 * extend, rotAngle + 90 * i + 45);
                }


                float cameraFin = (1 + 2 * Drawn.cameraDstScl(b.x, b.y, Vars.mobile ? 200 : 320)) / 3f;
                float triWidth = b.fout() * chargeCircleFrontRad * cameraFin;

                for(int i : Mathf.signs){
                    Fill.tri(b.x, b.y + triWidth, b.x, b.y - triWidth,
                    b.x + i * cameraFin * chargeCircleFrontRad * (23 + Mathf.absin(10f, 0.75f)) * (b.fout() * 1.25f + 1f), b.y);
                }


                float rad = splashDamageRadius * b.fin(Interp.pow5Out) * Interp.circleOut.apply(b.fout(0.15f));
                Lines.stroke(8f * b.fin(Interp.pow2Out));
                Lines.circle(b.x, b.y, rad);

                Draw.color(Color.white);
                Fill.circle(b.x, b.y, width * (b.fout() + 4) / 5.5f);

                Drawf.light(b.x, b.y, rad, hitColor, 0.5f);
            }

            @Override
            public void init(Bullet b){
                super.init(b);
                b.fdata = Mathf.randomSeed(b.id, 90);
            }

            @Override
            public void update(Bullet b){
                super.update(b);
                b.fdata += b.vel.len() / 3f;
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);

                float rad = 120;
                float spacing = 3f;


                Angles.randLenVectors(b.id, 8, splashDamageRadius / 1.25f, ((x, y) -> {
                    float nowX = b.x + x;
                    float nowY = b.y + y;

                    //					hitEffect.at(nowX, nowY, 0, hitColor);
                    //					hit(b, nowX, nowY);

                    Vec2 vec2 = new Vec2(nowX, nowY);
                    Team team = b.team;
                    float mul = b.damageMultiplier();
                    Time.run(Mathf.random(6f, 24f) + Mathf.sqrt(x * x + y * y) / splashDamageRadius * 3f, () -> {
                        if(Mathf.chanceDelta(0.4f)) hitSound.at(vec2.x, vec2.y, hitSoundPitch, hitSoundVolume);
                        despawnSound.at(vec2);
                        Effect.shake(hitShake, hitShake, vec2);

                        for(int i = 0; i < lightning / 2; i++){
                            Lightning.create(team, lightningColor, lightningDamage, vec2.x, vec2.y, Mathf.random(360f), lightningLength + Mathf.random(lightningLengthRand));
                        }

                        hitEffect.at(vec2.x, vec2.y, 0, hitColor);
                        hitSound.at(vec2.x, vec2.y, hitSoundPitch, hitSoundVolume);

                        if(fragBullet != null){
                            for(int i = 0; i < fragBullets; i++){
                                fragBullet.create(team.cores().firstOpt(), team, vec2.x, vec2.y, Mathf.random(360), Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                            }
                        }

                        if(splashDamageRadius > 0 && !b.absorbed){
                            Damage.damage(team, vec2.x, vec2.y, splashDamageRadius, splashDamage * mul, collidesAir, collidesGround);

                            if(status != StatusEffects.none){
                                Damage.status(team, vec2.x, vec2.y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
                            }
                        }
                    });
                }));
            }
        };


        PlasmaFireBall = new FireBulletType(1.0F, 30.0F){
            {
                colorFrom = colorMid = WHPal.SkyBlue;
                lifetime = 12.0F;
                radius = 4.0F;
                trailEffect = WHFx.PlasmaFireBurn;
            }

            public void draw(Bullet b){
                Draw.color(colorFrom, colorMid, colorTo, b.fin());
                Fill.poly(b.x, b.y, 6, b.fout() * radius, 0);
                Draw.reset();
            }

            public void update(Bullet b){
                if(Mathf.chanceDelta(fireTrailChance)){
                    PlasmaFire.create(b.tileOn());
                }

                if(Mathf.chanceDelta(fireEffectChance)){
                    trailEffect.at(b.x, b.y);
                }

                if(Mathf.chanceDelta(fireEffectChance2)){
                    trailEffect2.at(b.x, b.y);
                }
            }
        };

        sealedPromethiumMillBreak = new LightningLinkerBulletType(){
            {
                speed = 0f;
                lifetime = 180f;
                frontColor = Color.white;
                backColor = hitColor = lightColor = lightningColor = Pal.sapBullet;
                damage = 50f;
                absorbable = hittable = false;
                size = 30f;
                shrinkX = 1f;
                shrinkY = 1f;
                maxHit = 3;
                lightning = 1;
                lightningLength = 5;
                lightningLengthRand = 8;
                lightningDamage = 50f;
                effectLingtning = 1;
                effectLightningChance = 0.08F;
                splashDamage = 300f;
                splashDamageRadius = 50f;
                effectLightningLength = 60;
                despawnEffect = hitEffect = WHFx.multipRings(Pal.sapBullet, 60f, 3, 60f);
            }

            public final float bullrtRadius = 60f;

            @Override
            public void update(Bullet b){
                super.update(b);
                if(b.timer(1, 12F)){
                    for(int j = 0; j < 2; ++j){
                        Drawn.randFadeLightningEffect(b.x, b.y, Mathf.random(100), Mathf.random(7, 12), backColor, Mathf.chance(0.5));
                    }
                }
            }

            @Override
            public void draw(Bullet b){
                Draw.color(backColor);
                for(int i = 0; i < 4; i++){
                    Drawf.tri(b.x, b.y, 6f, 100 * b.fout(), i * 90);
                }
                Draw.color();
                for(int i = 0; i < 4; i++){
                    Drawf.tri(b.x, b.y, 3f, 50 * b.fout(), i * 90);
                }
                Draw.color(backColor, backColor, b.fout());
                Lines.stroke(2);
                Lines.circle(b.x, b.y, bullrtRadius);
            }
        };

        warpBreak = new BasicBulletType(0, 100.0F){
            {
                instantDisappear = true;
                status = StatusEffects.slow;
                statusDuration = 180.0F;
                hitShake = 8.0F;
                hitSound = Sounds.plasmaboom;
                hitSoundVolume = 3.0F;
                hitColor = lightColor = lightningColor = backColor = Pal.sapBullet;
                despawnEffect = hitEffect = new MultiEffect(new WrapEffect(Fx.dynamicSpikes, hitColor, 70),
                new WrapEffect(Fx.titanExplosion, hitColor));
                lightning = 5;
                lightningLength = 10;
                lightningLengthRand = 5;
                lightningDamage = 30;
                fragBullets = 15;
                fragLifeMax = 2f;
                fragBullet = new LightningBulletType(){
                    {
                        lifetime = 30;
                        hitColor = lightningColor = Pal.sapBullet;
                        despawnEffect = hitEffect = Fx.hitLancer;
                        lightning = 1;
                        lightningDamage = 30;
                        lightningLength = 15;
                    }
                };
                hitSound = WHSounds.hugeBlast;
                hitSoundVolume = 4.0F;

            }
        };


        AGFrag = new LightningLinkerBulletType(){
            {
                effectLightningChance = 0.15F;
                damage = 200.0F;
                backColor = trailColor = lightColor = lightningColor = hitColor = Color.valueOf("FFC397FF");
                size = 10.0F;
                frontColor = Color.valueOf("000000ff");
                range = 600.0F;
                spreadEffect = Fx.none;
                trailWidth = 8.0F;
                trailLength = 20;
                speed = 6.0F;
                linkRange = 280.0F;
                maxHit = 12;
                drag = 0.0065F;
                hitSound = Sounds.explosionbig;
                splashDamageRadius = 60.0F;
                splashDamage = lightningDamage = damage / 3.0F;
                lifetime = 130.0F;
                despawnEffect = WHFx.lightningHitLarge(hitColor);
                hitEffect = WHFx.sharpBlast(hitColor, frontColor, 35.0F, splashDamageRadius * 1.25F);
                shootEffect = WHFx.hitSpark(backColor, 45.0F, 12, 60.0F, 3.0F, 8.0F);
                smokeEffect = WHFx.hugeSmoke;
            }
        };
        tankAG7 = new EffectBulletType(480.0F){
            {
                hittable = false;
                collides = false;
                collidesTiles = collidesAir = collidesGround = true;
                speed = 0.1F;
                despawnHit = true;
                keepVelocity = false;
                splashDamageRadius = 480.0F;
                splashDamage = 800.0F;
                lightningDamage = 200.0F;
                lightning = 36;
                lightningLength = 60;
                lightningLengthRand = 60;
                hitShake = despawnShake = 40.0F;
                drawSize = 800.0F;
                hitColor = lightColor = trailColor = lightningColor = Color.valueOf("FFC397FF");
                buildingDamageMultiplier = 1.1F;
                fragBullets = 22;
                fragBullet = WHBullets.AGFrag;
                hitSound = WHSounds.hugeBlast;
                hitSoundVolume = 4.0F;
                fragLifeMax = 1.1F;
                fragLifeMin = 0.7F;
                fragVelocityMax = 0.6F;
                fragVelocityMin = 0.2F;
                status = StatusEffects.shocked;
                shootEffect = WHFx.lightningHitLarge(hitColor);
                hitEffect = WHFx.hitSpark(hitColor, 240.0F, 220, 900.0F, 8.0F, 27.0F);
                despawnEffect = WHFx.TankAG7BulletExplode;
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);

                Vec2 vec = new Vec2().set(b);

                float damageMulti = b.damageMultiplier();
                Team team = b.team;
                for(int i = 0; i < splashDamageRadius / (tilesize * 3.5f); i++){
                    int finalI = i;
                    Time.run(i * despawnEffect.lifetime / (splashDamageRadius / (tilesize * 2)), () -> {
                        Damage.damage(team, vec.x, vec.y, tilesize * (finalI + 6), splashDamage * damageMulti, true);
                    });
                }

                Units.nearby(team, vec.x, vec.y, splashDamageRadius * 2, u -> {
                    u.heal((1 - u.healthf()) / 3 * u.maxHealth());
                    u.apply(StatusEffects.overclock, 360f);
                });


                float rad = 120;
                float spacing = 2.5f;

                for(int k = 0; k < (despawnEffect.lifetime - WHFx.chainLightningFadeReversed.lifetime) / spacing; k++){
                    Time.run(k * spacing, () -> {
                        for(int j : Mathf.signs){
                            Vec2 v = Tmp.v6.rnd(rad * 2 + Mathf.random(rad * 4)).add(vec);
                            (j > 0 ? WHFx.chainLightningFade : WHFx.chainLightningFadeReversed).at(v.x, v.y, 12f, hitColor, vec);
                        }
                    });
                }
            }

            @Override
            public void update(Bullet b){
                float rad = 120;

                Effect.shake(8 * b.fin(), 6, b);

                if(b.timer(1, 12)){
                    Seq<Teamc> entites = new Seq<>();

                    Units.nearbyEnemies(b.team, b.x, b.y, rad * 2.5f * (1 + b.fin()) / 2, entites::add);

                    Units.nearbyBuildings(b.x, b.y, rad * 2.5f * (1 + b.fin()) / 2, e -> {
                        if(e.team != b.team) entites.add(e);
                    });

                    entites.shuffle();
                    entites.truncate(15);

                    for(Teamc e : entites){
                        PositionLightning.create(b, b.team, b, e, lightningColor, false, lightningDamage, 5 + Mathf.random(5), PositionLightning.WIDTH, 1, p -> {
                            WHFx.lightningHitSmall.at(p.getX(), p.getY(), 0, lightningColor);
                        });
                    }
                }

                if(b.lifetime() - b.time() > WHFx.chainLightningFadeReversed.lifetime) for(int i = 0; i < 2; i++){
                    if(Mathf.chanceDelta(0.2 * Mathf.curve(b.fin(), 0, 0.8f))){
                        for(int j : Mathf.signs){
                            Sounds.spark.at(b.x, b.y, 1f, 0.3f);
                            Vec2 v = Tmp.v6.rnd(rad / 2 + Mathf.random(rad * 2) * (1 + Mathf.curve(b.fin(), 0, 0.9f)) / 1.5f).add(b);
                            (j > 0 ? WHFx.chainLightningFade : WHFx.chainLightningFadeReversed).at(v.x, v.y, 12f, hitColor, b);
                        }
                    }
                }

                if(b.fin() > 0.05f && Mathf.chanceDelta(b.fin() * 0.3f + 0.02f)){
                    Tmp.v1.rnd(rad / 4 * b.fin());
                    WHFx.shuttleLerp.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v1.angle(), hitColor, Mathf.random(rad, rad * 3f) * (Mathf.curve(b.fin(Interp.pow2In), 0, 0.7f) + 2) / 3);
                }
            }

            public void draw(Bullet b){
                float fin = Mathf.curve(b.fin(), 0.0F, 0.02F);
                float f = fin * Mathf.curve(b.fout(), 0.0F, 0.1F);
                float rad = 120.0F;
                float z = Draw.z();
                float circleF = (b.fout(Interp.pow2In) + 1.0F) / 2.0F;
                Draw.color(hitColor);
                Lines.stroke(rad / 20.0F * b.fin());
                Lines.circle(b.x, b.y, rad * b.fout(Interp.pow3In));
                Lines.circle(b.x, b.y, b.fin(Interp.circleOut) * rad * 3.0F * Mathf.curve(b.fout(), 0.0F, 0.05F));
                Rand rand = WHFx.rand;
                rand.setSeed((long)b.id);

                for(int i = 0; i < (int)(rad / 3.0F); ++i){
                    Tmp.v1.trns(rand.random(360.0F) + rand.range(1.0F) * rad / 5.0F * b.fin(Interp.pow2Out), rad / 2.05F * circleF + rand.random(rad * (1.0F + b.fin(Interp.circleOut)) / 1.8F));
                    float angle = Tmp.v1.angle();
                    Drawn.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, (b.fin() + 1.0F) / 2.0F * 28.0F + (float)rand.random(0, 8), rad / 16.0F * (b.fin(Interp.exp5In) + 0.25F), angle);
                    Drawn.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, (b.fin() + 1.0F) / 2.0F * 12.0F + (float)rand.random(0, 2), rad / 12.0F * (b.fin(Interp.exp5In) + 0.5F) / 1.2F, angle - 180.0F);
                }

                Angles.randLenVectors((long)(b.id + 1), (int)(rad / 3.0F), rad / 4.0F * circleF, rad * (1.0F + b.fin(Interp.pow3Out)) / 3.0F, (x, y) -> {
                    float angle = Mathf.angle(x, y);
                    Drawn.tri(b.x + x, b.y + y, rad / 8.0F * (1.0F + b.fout()) / 2.2F, (b.fout() * 3.0F + 1.0F) / 3.0F * 25.0F + (float)rand.random(4, 12) * (b.fout(Interp.circleOut) + 1.0F) / 2.0F, angle);
                    Drawn.tri(b.x + x, b.y + y, rad / 8.0F * (1.0F + b.fout()) / 2.2F, (b.fout() * 3.0F + 1.0F) / 3.0F * 9.0F + (float)rand.random(0, 2) * (b.fin() + 1.0F) / 2.0F, angle - 180.0F);
                });
                Drawf.light(b.x, b.y, rad * f * (b.fin() + 1.0F) * 2.0F, Draw.getColor(), 0.7F);
                Draw.z(110.001F);
                Draw.color(hitColor);
                Fill.circle(b.x, b.y, rad * fin * circleF / 2.0F);
                Draw.color(Color.valueOf("000000ff"));
                Fill.circle(b.x, b.y, rad * fin * circleF * 0.75F / 2.0F);
                Draw.z(99.9F);
                Draw.color(Color.valueOf("FFC397FF"));
                Fill.circle(b.x, b.y, rad * fin * circleF * 0.8F / 2.0F);
                Draw.z(z);
            }
        };
        collaspsePf = new LightningLinkerBulletType(){
            {
                frontColor = Color.valueOf("000000ff");
                backColor = Color.valueOf("788AD7FF");
                hittable = false;
                size = 13.0F;
                damage = 100.0F;
                speed = 8.0F;
                lifetime = 56.25F;
                splashDamageRadius = 80.0F;
                splashDamage = 100.0F;
                linkRange = 60.0F;
                lightningColor = Color.valueOf("788AD7FF");
                buildingDamageMultiplier = 0.2F;
                status = WHStatusEffects.palsy;
                statusDuration = 300.0F;
                lightningDamage = 65.0F;
                lightning = 1;
                lightningLength = 10;
                lightningLengthRand = 10;
                trailColor = Color.valueOf("788AD7FF");
                despawnEffect = new MultiEffect(new Effect[]{WHFx.circleOut(130.0F, splashDamageRadius, 3.0F), WHFx.smoothColorCircle(Color.valueOf("788AD7FF"), splashDamageRadius, 130.0F)});
                fragBullet = new DOTBulletType(){
                    {
                        DOTDamage = damage = 40.0F;
                        DOTRadius = 12.0F;
                        radIncrease = 0.25F;
                        fx = WHFx.squSpark1;
                        lightningColor = Color.valueOf("788AD7FF");
                    }
                };
                fragBullets = 1;
            }
        };
        collapseSp = new LightningLinkerBulletType(){
            {
                frontColor = Color.valueOf("000000ff");
                backColor = Color.valueOf("DBD58C");
                trailColor = Color.valueOf("DBD58C");
                hittable = false;
                speed = 8.0F;
                lifetime = 62.5F;
                size = 13.0F;
                rangeChange = 50.0F;
                damage = 100.0F;
                splashDamageRadius = 80.0F;
                splashDamage = 100.0F;
                lightningLength = 10;
                lightningLengthRand = 3;
                effectLightningChance = 0.25F;
                lightningCone = 360.0F;
                lightningDamage = 200.0F;
                linkRange = 60.0F;
                lightningColor = Color.valueOf("DBD58C");
                buildingDamageMultiplier = 0.2F;
                status = WHStatusEffects.palsy;
                statusDuration = 480.0F;
                lightningDamage = 65.0F;
                lightning = 1;
                lightningLength = 10;
                lightningLengthRand = 10;
                ammoMultiplier = 2.0F;
                fragBullet = new DOTBulletType(){
                    {
                        DOTDamage = damage = 70.0F;
                        DOTRadius = 12.0F;
                        radIncrease = 0.25F;
                        fx = WHFx.triSpark2;
                        lightningColor = Color.valueOf("DBD58C");
                    }
                };
                fragBullets = 1;
                despawnEffect = new MultiEffect(new Effect[]{WHFx.circleOut(130.0F, splashDamageRadius, 3.0F), WHFx.smoothColorCircle(Color.valueOf("DBD58C"), splashDamageRadius, 130.0F), WHFx.ScrossBlastArrow45, WHFx.subEffect(130.0F, 85.0F, 12, 30.0F, Interp.pow2Out, (i, x, y, rot, fin) -> {
                    float fout = Interp.pow2Out.apply(1.0F - fin);
                    float finpow = Interp.pow3Out.apply(fin);
                    Tmp.v1.trns(rot, 25.0F * finpow);
                    Draw.color(Color.valueOf("DBD58C"));
                    int[] var8 = Mathf.signs;
                    int var9 = var8.length;
                    for(int var10 = 0; var10 < var9; ++var10){
                        int s = var8[var10];
                        Drawf.tri(x, y, 14.0F * fout, 30.0F * Mathf.curve(finpow, 0.0F, 0.3F) * WHFx.fout(fin, 0.15F), rot + (float)(s * 90));
                    }

                })});
            }
        };
        SK = new StrafeLaser(300.0F){
            {
                strafeAngle = 0;
            }

            @Override
            public void draw(Bullet b){
                Tmp.c1.set(b.team.color).lerp(Color.white, Mathf.absin(4.0F, 0.1F));
                super.draw(b);
                Draw.z(110.0F);
                float fout = b.fout(0.25F) * Mathf.curve(b.fin(), 0.0F, 0.125F);
                Draw.color(Tmp.c1);
                Fill.circle(b.x, b.y, this.width / 1.225F * fout);
                for(int i : Mathf.signs){
                    WHUtils.tri(b.x, b.y, 6.0F * fout, 10.0F + 50.0F * fout, Time.time * 1.5F + (float)(90 * i));
                    WHUtils.tri(b.x, b.y, 6.0F * fout, 20.0F + 60.0F * fout, Time.time * -1.0F + (float)(90 * i));
                }

                Draw.z(110.001F);
                Draw.color(b.team.color, Color.white, 0.25F);
                Fill.circle(b.x, b.y, this.width / 1.85F * fout);
                Draw.color(Color.black);
                Fill.circle(b.x, b.y, this.width / 2.155F * fout);
                Draw.z(100.0F);
                Draw.reset();
                float rotation = this.dataRot ? b.fdata : b.rotation() + this.getRotation(b);
                float maxRange = this.maxRange * fout;
                float realLength = WHUtils.findLaserLength(b, rotation, maxRange);
                Tmp.v1.trns(rotation, realLength);
                Tmp.v2.trns(rotation, 0.0F, this.width / 2.0F * fout);
                Tmp.v3.setZero();
                if(realLength < maxRange){
                    Tmp.v3.set(Tmp.v2).scl((maxRange - realLength) / maxRange);
                }

                Draw.color(Tmp.c1);
                Tmp.v2.scl(0.9F);
                Tmp.v3.scl(0.9F);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
                if(realLength < maxRange){
                    Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
                }

                Tmp.v2.scl(1.2F);
                Tmp.v3.scl(1.2F);
                Draw.alpha(0.5F);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
                if(realLength < maxRange){
                    Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
                }

                Draw.alpha(1.0F);
                Draw.color(Color.black);
                Draw.z(Draw.z() + 0.01f);
                Tmp.v2.scl(0.5F);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + (Tmp.v1.x + Tmp.v3.x) / 3.0F, b.y + (Tmp.v1.y + Tmp.v3.y) / 3.0F, b.x + (Tmp.v1.x - Tmp.v3.x) / 3.0F, b.y + (Tmp.v1.y - Tmp.v3.y) / 3.0F);
                Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, this.width * 1.5F, this.getColor(b), 0.7F);
                Draw.reset();
                Draw.z(Draw.z() - 0.01f);
            }
        };

        airRaiderMissile = new MissileBulletType(){
            {
                trailEffect = new Effect(50, e -> {
                    Draw.color(WHPal.OR);
                    Angles.randLenVectors(e.id, 1, -20 * e.finpow(), e.rotation, 80, (x, y) ->
                    Fill.square(e.x + x, e.y + y, 5 * e.foutpow(), Mathf.randomSeed(e.id, 360) + e.time));
                });
                width = 50;
                height = 60;
                sprite = "wh-重型导弹";
                speed = 10;
                lifetime = 35;
                drag = -0.004f;
                homingDelay = 15;
                homingPower = 0.12f;
                homingRange = 30;
                trailLength = 10;
                trailWidth = 3;
                trailColor = WHPal.OR;
                shrinkY = 0.5f;
                shrinkX = 0.5f;
                backColor = WHPal.ORL;
                frontColor = WHPal.WHYellow2;
                hitEffect = despawnEffect = new MultiEffect(
                WHFx.blast(WHPal.OR, 10),
                WHFx.line45Explosion(WHPal.OR, WHPal.ORL, 10));
                hitSound = plasmaboom;
                hitShake = 1;
                shootEffect = WHFx.shootLineSmall(backColor);
                smokeEffect = hugeSmoke;
                hittable = false;
                damage = 100;
                shieldDamageMultiplier = 3;
                splashDamageRadius = 56;
                splashDamage = 100;
                lightningDamage = 30;
                lightning = 1;
                lightningLength = 10;
                lightningColor = WHPal.OR;
            }

            final float Mag = 20;

            @Override
            public void updateWeaving(Bullet b){
                super.updateWeaving(b);
                rand.setSeed(b.id);
                var progress = b.fin() * Math.PI - Math.PI / 2;
                float sign = weaveRandom ? (Mathf.randomSeed(b.id, 0, 1) == 1 ? -1 : 1) : 1f;
                b.vel.rotateRadExact(-sign * Mathf.sin((float)progress, range / Mathf.PI / tilesize, rand.random(0, 1f) * sign * Mag) * Time.delta * Mathf.degRad);
            }
        };

        air5Missile = new MissileBulletType(){
            {
                trailEffect = new Effect(50, e -> {
                    Draw.color(WHPal.OR);
                    Angles.randLenVectors(e.id, 1, -20 * e.finpow(), e.rotation, 80, (x, y) ->
                    Fill.square(e.x + x, e.y + y, 5 * e.foutpow(), Mathf.randomSeed(e.id, 360) + e.time));
                });
                width = 40;
                height = 45;
                sprite = name("large-missile");
                speed = 14;
                lifetime = 35;
                homingDelay = 15;
                homingPower = 0.2f;
                homingRange = 30;
                trailLength = 9;
                trailWidth = 3;
                weaveRandom = true;
                trailColor = WHPal.OR;
                shrinkY = 0;
                shrinkX = 0;
                backColor = WHPal.ORL;
                frontColor = WHPal.WHYellow2;
                hitEffect = despawnEffect = new MultiEffect(
                WHFx.blast(WHPal.OR, 10),
                WHFx.line45Explosion(WHPal.ORL, WHPal.OR, 10));
                hitSound = plasmaboom;
                hitShake = 5;
                shootEffect = WHFx.shootLineSmall(backColor);
                smokeEffect = hugeSmoke;
                damage = 80;
                splashDamageRadius = 56;
                splashDamage = 90;
                lightningDamage = 30;
                lightning = 1;
                lightningLength = 10;
                lightningColor = WHPal.OR;
            }

            final float Mag = 20;

            @Override
            public void updateWeaving(Bullet b){
                super.updateWeaving(b);
                rand.setSeed(b.id);
                var progress = b.fin() * Math.PI - Math.PI / 2;
                float sign = weaveRandom ? (Mathf.randomSeed(b.id, 0, 1) == 1 ? -1 : 1) : 1f;
                b.vel.rotateRadExact(-sign * Mathf.sin((float)progress, range / Mathf.PI / tilesize, rand.random(0, 1f) * sign * Mag) * Time.delta * Mathf.degRad);
            }
        };
        airRaiderBomb = new BasicBulletType(5, 1000){
            {
                drawSize = 1200f;
                width = height = shrinkX = shrinkY = 0;
                collides = false;
                despawnHit = false;
                collidesAir = collidesGround = collidesTiles = true;
                drag = 0.1f;
                lifetime = 180f;
                shieldDamageMultiplier = 0.3f;

                despawnSound = Sounds.plasmaboom;
                hitSound = Sounds.explosionbig;
                hitShake = 10;
                lightning = 10;
                lightningDamage = 100;
                lightningLength = 20;
                lightningLengthRand = 15;

                trailWidth = 12F;
                trailLength = 120;

                hittable = false;

                splashDamageRadius = 300;
                splashDamage = 400;
                hitColor = lightColor = lightningColor = trailColor = WHPal.OR;
                Effect effect = WHFx.crossBlast(hitColor, splashDamageRadius, 0);
                effect.lifetime = 180;

                despawnEffect = new MultiEffect(WHFx.circleOut(hitColor, splashDamageRadius));
                hitEffect = new MultiEffect(WHFx.blast(hitColor, 200f), effect, WHFx.circleOut(hitColor, splashDamageRadius));
            }

            @Override
            public void init(Bullet b){
                super.init(b);
                b.fdata = Mathf.randomSeed(b.id, 180);
            }

            @Override
            public void update(Bullet b){
                super.update(b);
                b.fdata += b.vel.len() / 3f;
            }

            @Override
            public void draw(Bullet b){
                super.draw(b);

                Draw.color(hitColor, hitColor.cpy().lerp(Color.white, 0.5f), b.fout() * 0.25f);

                float fin = Mathf.curve(b.fin(Interp.pow10Out), 0, 0.9f);
                float fout = Mathf.curve(b.fout(Interp.pow10Out), 0.3f, 1);

                float chargeCircleFrontRad = 10;
                float width = chargeCircleFrontRad * 1.2f;
                Fill.circle(b.x, b.y, width * (b.fout() + 4) / 3.5f);

                float rotAngle = b.fdata * fout * fin;

                for(int i = 0; i < 4; i++){
                    Drawn.tri(b.x, b.y, width * b.foutpowdown(), splashDamageRadius / 2 * fout + splashDamageRadius * 1.2f * fin * fout, rotAngle + 90 * i);
                }

                float rad = splashDamageRadius * b.fin(Interp.pow5Out) * Interp.circleOut.apply(b.fout(0.15f));
                Lines.stroke(8f * b.fin(Interp.pow2Out));
                Lines.circle(b.x, b.y, rad);

                Draw.color(Color.white);
                Fill.circle(b.x, b.y, width * (b.fout() + 4) / 5.5f);

                Drawf.light(b.x, b.y, rad, hitColor, 0.5f);
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);
                Vec2 vec = new Vec2().set(b);
                float damageMulti = b.damageMultiplier();
                Team team = b.team;
                for(int i = 0; i < splashDamageRadius / (tilesize * 5f); i++){
                    int finalI = i;
                    Time.run(i * despawnEffect.lifetime / (splashDamageRadius / (tilesize * 2)), () -> {
                        Damage.damage(team, vec.x, vec.y, tilesize * (finalI + 6), splashDamage * damageMulti, true);
                    });
                }
            }
        };

        CrushBulletLead = new FlakBulletType(6.5f, 10){{
            ammoMultiplier = 2;
            speed = 6.5f;
            lifetime = 180 / 6.5f;
            width = 6;
            height = 8;
            trailWidth = 2f;
            trailLength = 5;
            trailSinScl = 4f;
            trailSinMag = 0.12f;
            explodeRange = splashDamageRadius = 24;
            shieldDamageMultiplier = 0.5f;
            shootEffect = Fx.shootSmall;
            hitEffect = Fx.flakExplosion;
            collidesGround = true;
        }};

        CrushBulletMetaGlass = new FlakBulletType(6.5f, 10){{
            ammoMultiplier = 2;
            speed = 6.5f;
            lifetime = 180 / 6.5f;
            width = 9;
            height = 9;
            trailWidth = 2f;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.1f;
            damage = splashDamage = 30f;
            explodeRange = splashDamageRadius = 24;
            shieldDamageMultiplier = 0.5f;
            backColor = hitColor = trailColor = Pal.glassAmmoBack;
            shootEffect = Fx.shootSmall;
            hitEffect = Fx.flakExplosion;
            fragBullet = new BasicBulletType(3f, 12, name("tall")){{
                width = 10f;
                height = 10f;
                shrinkY = 1f;
                lifetime = 20f;
                backColor = Pal.gray;
                frontColor = Color.white;
                despawnEffect = Fx.none;
            }};
        }};

        CrushBulletTiSteel = new FlakBulletType(6.5f, 10){{
            ammoMultiplier = 2;
            speed = 6.5f;
            lifetime = 180 / 6.5f;
            width = 6;
            height = 8;
            trailWidth = 2f;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.1f;
            damage = splashDamage = 45f;
            explodeRange = splashDamageRadius = 28;
            shieldDamageMultiplier = 0.5f;
            status = StatusEffects.slow;
            statusDuration = 20;
            shootEffect = Fx.shootSmall;
            hitEffect = new WrapEffect(Fx.flakExplosion, WHPal.TiSteelColor);
            backColor = hitColor = trailColor = WHPal.TiSteelColor;
            collidesGround = true;
        }};

        AutoGunGraphite = new CritBulletType(8f, 30){{
            critChance = 0.1f;
            critMultiplier = 2f;
            lifetime = 240 / 8f;
            backColor = hitColor = trailColor = Items.graphite.color;
            width = 8.3f;
            height = 18f;
            trailWidth = 2f;
            trailLength = 5;
            splashDamageRadius = 20;
            splashDamage = 34;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            trailChance = 0.1f;
            critEffect = WHFx.square(Items.graphite.color, 20, 1, 10, 6f);
            despawnEffect = hitEffect = Fx.explosion;
        }};

        AutoGunSilicon = new CritBulletType(8.5f, 25){{
            critChance = 0.1f;
            critMultiplier = 2f;
            lifetime = 240 / 8.5f;
            backColor = hitColor = trailColor = Items.silicon.color;
            width = 8.3f;
            height = 16f;
            trailWidth = 2f;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.08f;
            homingDelay = 10;
            homingPower = 0.01f;
            homingRange = 20;
            splashDamageRadius = 20;
            splashDamage = 42;
            trailChance = 0.1f;
            critEffect = WHFx.square(Items.silicon.color, 20, 1, 10, 6f);
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            despawnEffect = hitEffect = Fx.explosion;
        }};
        AutoGunPyratite = new CritBulletType(8.5f, 30){{
            rangeChange = 16f;
            critChance = 2f;
            critMultiplier = 1.5f;
            knockback = 0.8f;
            lifetime = 240 / 8.5f;
            backColor = hitColor = trailColor = Items.pyratite.color;
            width = 8.3f;
            height = 18f;
            trailWidth = 2f;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.08f;
            makeFire = true;
            status = StatusEffects.burning;
            statusDuration = 60;
            splashDamageRadius = 20;
            splashDamage = 55;
            trailChance = 0.1f;
            critEffect = WHFx.square(Items.pyratite.color, 20, 1, 10, 6f);
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            despawnEffect = hitEffect = Fx.explosion;
        }};
        AutoGunTiSteel = new CritBulletType(8.5f, 30){{
            critChance = 0.1f;
            critMultiplier = 2f;
            knockback = 0.8f;
            lifetime = 240 / 8.5f;
            backColor = hitColor = trailColor = WHPal.TiSteelColor;
            width = 8.3f;
            height = 18f;
            trailWidth = 2f;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.12f;
            status = StatusEffects.slow;
            statusDuration = 60;
            splashDamageRadius = 20;
            splashDamage = 55;
            trailChance = 0.1f;
            critEffect = WHFx.square(WHItems.titaniumSteel.color, 20, 1, 10, 6f);
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            despawnEffect = hitEffect = Fx.explosion;
        }};

        LcarusBullet = new DelayedPointBulletType(){{
            colors = new Color[]{WHPal.SkyBlue.cpy().mul(1f, 1f, 1f, 0.4f), WHPal.SkyBlue.cpy(), Pal.lancerLaser};
            damage = 50;
            splashDamageRadius = 40;
            splashDamage = 80;
            width = 20;
            laser = Fx.none;
            delayEffectLifeTime = 28f;
            hitColor = WHPal.SkyBlue.cpy();
            hitEffect = despawnEffect = new ExplosionEffect(){{
                waveColor = sparkColor = WHPal.SkyBlue;
                waveRad = splashDamageRadius;
                waveStroke = 2f;
                waveLife = 15f;
                sparks = 9;
                sparkRad = 30;
                sparkLen = 8;
            }};
            shootEffect = new MultiEffect(WHFx.lineCircleOut(WHPal.SkyBlue, 30, 13, 2f),
            WHFx.shootCircleSmall(WHPal.SkyBlue));
        }};

        LcarusBulletEnhanced = new DelayedPointBulletType(){{
            colors = new Color[]{WHPal.ORL.cpy().mul(1f, 1f, 1f, 0.4f), WHPal.ORL.cpy(), Pal.lancerLaser};
            damage = 120;
            rangeChange = 16f;
            reloadMultiplier = 0.5f;
            shieldDamageMultiplier = 2;
            splashDamageRadius = 50;
            splashDamage = 100;
            laser = Fx.none;
            width = 20;
            delayEffectLifeTime = 28f;
            hitColor = WHPal.ORL;
            hitEffect = despawnEffect = new ExplosionEffect(){{
                waveColor = sparkColor = WHPal.ORL;
                waveRad = splashDamageRadius;
                waveLife = 15f;
                waveStroke = 2f;
                sparks = 9;
                sparkRad = 30;
                sparkLen = 8;
            }};
            shootEffect = new MultiEffect(WHFx.lineCircleOut(WHPal.ORL, 30, 13, 2f),
            WHFx.shootCircleSmall(WHPal.ORL));
        }};

        SSWordTiSteel = new CritMissileBulletType(){{
            critChance = 0.1f;
            critMultiplier = 2f;
            speed = 8f;
            damage = 50;
            splashDamageRadius = 32;
            splashDamage = 60;
            lifetime = 55;
            homingDelay = lifetime / 2;
            homingPower = 0.08f;
            homingRange = 80;
            followAimSpeed = 8f;
            weaveRandom = true;
            weaveScale = 12f;
            weaveMag = 0.3f;
            trailWidth = 2;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.12f;
            sprite = name("large-missile");
            hitColor = trailColor = backColor = WHItems.titaniumSteel.color.cpy();
            width = 18f;
            height = 60;
            hitSize = 10f;
            keepVelocity = false;
            ammoMultiplier = 3;
            smokeEffect = Fx.shootSmallFlame;
            shootEffect = new MultiEffect(Fx.shootBigColor, Fx.colorSparkBig);
            hitEffect = despawnEffect = new MultiEffect(Fx.flakExplosionBig,
            WHFx.hitSpark(WHItems.titaniumSteel.color, 30, 8, 40, 1, 5));
            critEffect = WHFx.square(WHItems.titaniumSteel.color, 30, 1, 10, 5f);
            flameWidth = 3f;
            flameLength = 16f;
            lengthOffset = 5;
            colors = new Color[]{WHItems.titaniumSteel.color.cpy().a(0.4f), WHItems.titaniumSteel.color.cpy().a(0.8f), Pal.lancerLaser};
        }};
        SSWordPlastanium = new CritMissileBulletType(){{
            critChance = 0.1f;
            critMultiplier = 2f;
            speed = 8f;
            damage = 40;
            lifetime = 55;
            homingDelay = lifetime / 2;
            homingPower = 0.08f;
            homingRange = 80;
            followAimSpeed = 8f;
            weaveRandom = true;
            weaveScale = 12f;
            weaveMag = 0.3f;
            trailWidth = 2;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.12f;
            trailInterp = Interp.exp5In;
            sprite = name("large-missile");
            hitColor = trailColor = backColor = Items.plastanium.color.cpy();
            width = 18f;
            height = 60;
            hitSize = 10f;
            keepVelocity = false;
            ammoMultiplier = 3;
            reloadMultiplier = 1.5f;
            fragBullets = 6;
            fragBullet = new BasicBulletType(2.5f, 25, name("tall")){{
                splashDamageRadius = 40;
                splashDamage = 30;
                width = 10f;
                height = 12f;
                shrinkY = 1f;
                lifetime = 15f;
                backColor = Pal.plastaniumBack;
                frontColor = Pal.plastaniumFront;
                despawnEffect = Fx.none;
            }};
            smokeEffect = Fx.shootSmallFlame;
            shootEffect = new MultiEffect(Fx.shootBigColor, Fx.colorSparkBig);
            hitEffect = despawnEffect = new MultiEffect(Fx.flakExplosionBig,
            WHFx.hitSpark(Items.plastanium.color, 30, 8, 40, 1, 5));
            critEffect = WHFx.square(Items.plastanium.color, 30, 1, 10, 5f);
            flameWidth = 3f;
            flameLength = 16f;
            lengthOffset = 5;
            colors = new Color[]{Items.plastanium.color.cpy().a(0.4f), Items.plastanium.color.cpy().a(0.8f), Items.plastanium.color.cpy().lerp(Color.white, 0.8f)};
        }};

        SSWordPyratite = new CritMissileBulletType(){{
            critChance = 0.1f;
            critMultiplier = 2f;
            speed = 8f;
            damage = 70;
            splashDamageRadius = 50;
            splashDamage = 70;
            lifetime = 55;
            homingDelay = lifetime / 2;
            homingPower = 0.08f;
            homingRange = 80;
            followAimSpeed = 8f;
            weaveRandom = true;
            weaveScale = 12f;
            weaveMag = 0.3f;
            trailWidth = 2;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.12f;
            sprite = name("large-missile");
            hitColor = trailColor = backColor = Items.pyratite.color.cpy();
            width = 18f;
            height = 60;
            hitSize = 10f;
            keepVelocity = false;
            ammoMultiplier = 3;
            makeFire = true;
            incendSpread = 10f;
            incendChance = 0.1f;
            smokeEffect = Fx.shootSmallFlame;
            shootEffect = new MultiEffect(Fx.shootBigColor, Fx.colorSparkBig);
            hitEffect = despawnEffect = new MultiEffect(Fx.flakExplosionBig,
            WHFx.hitSpark(Items.pyratite.color, 30, 8, 40, 1, 5));
            trailChance = 0.1f;
            trailEffect = critEffect = new Effect(30f, e -> {
                Draw.color(Items.pyratite.color.cpy());
                rand.setSeed(e.id);
                randLenVectors(e.id, 1, 10 * e.fin(), e.rotation, 22, (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, e.fout() * rand.random(1.5f, 3.2f));
                    Drawf.light(e.x + x, e.y + y, e.fout() * 4.5f, Items.pyratite.color.cpy(), 0.7f);
                });
            });
            flameWidth = 3f;
            flameLength = 16f;
            lengthOffset = 5;
            colors = new Color[]{Items.pyratite.color.cpy().a(0.4f), Items.pyratite.color.cpy().a(0.8f), Items.pyratite.color.cpy().lerp(Color.white, 0.8f)};
        }};

        SSWordSurgeAlloy = new CritMissileBulletType(){{
            critChance = 0.1f;
            critMultiplier = 2f;
            speed = 8f;
            damage = 90;
            splashDamageRadius = 40;
            splashDamage = 50;
            lifetime = 55;
            homingDelay = lifetime / 2;
            homingPower = 0.08f;
            homingRange = 80;
            followAimSpeed = 8f;
            weaveRandom = true;
            weaveScale = 12f;
            weaveMag = 0.3f;
            trailWidth = 2;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.12f;
            sprite = name("large-missile");
            hitColor = trailColor = backColor = Items.surgeAlloy.color.cpy();
            width = 18f;
            height = 60;
            hitSize = 10f;
            keepVelocity = false;
            ammoMultiplier = 2;
            lightning = 2;
            lightningLength = 8;
            lightningDamage = 20f;
            lightningColor = Items.surgeAlloy.color;
            smokeEffect = Fx.shootSmallFlame;
            shootEffect = new MultiEffect(Fx.shootBigColor, Fx.colorSparkBig);
            hitEffect = despawnEffect = new MultiEffect(Fx.flakExplosionBig,
            WHFx.hitSpark(Items.surgeAlloy.color.cpy(), 30, 8, 40, 1, 5),
            WHFx.instHit(Items.surgeAlloy.color.cpy(), false, 3, 18));
            critEffect = WHFx.square(Items.surgeAlloy.color.cpy(), 30, 1, 10, 3);
            flameWidth = 3f;
            flameLength = 16f;
            lengthOffset = 5;
            colors = new Color[]{Items.surgeAlloy.color.cpy().a(0.4f), Items.surgeAlloy.color.cpy().a(0.8f), Items.surgeAlloy.color.cpy().lerp(Color.white, 0.8f)};
        }};

        PreventPyratite = new CritBulletType(12, 40){{
            critChance = 0.1f;
            critMultiplier = 2f;
            buildingDamageMultiplier = 0.3f;

            lifetime = 280 / speed;
            splashDamageRadius = 10f;
            splashDamage = 50f;
            status = StatusEffects.burning;
            statusDuration = 300f;
            makeFire = true;
            hitColor = backColor = trailColor = Items.pyratite.color.cpy();
            trailWidth = 10 / 4f;
            trailLength = 4;
            width = 10f;
            height = 14f;
            ammoMultiplier = 3;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            critEffect = new WrapEffect(Fx.missileTrail, Items.pyratite.color.cpy());
            hitEffect = Fx.hitBulletColor;
            despawnEffect = Fx.flakExplosion;
        }};

        PreventThorium = new CritBulletType(12, 70){{
            critChance = 0.2f;
            critMultiplier = 2.5f;
            buildingDamageMultiplier = 0.3f;

            lifetime = 280 / speed;
            hitColor = backColor = trailColor = Items.thorium.color.cpy();
            trailWidth = 10 / 4f;
            trailLength = 4;
            width = 10f;
            height = 14f;
            ammoMultiplier = 2;
            pierce = true;
            pierceCap = 3;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            critEffect = new WrapEffect(Fx.missileTrail, Items.thorium.color.cpy());
            hitEffect = despawnEffect = Fx.hitBulletColor;
        }};

        PreventTungsten = new CritBulletType(12, 80){{
            critChance = 0.1f;
            critMultiplier = 2f;
            buildingDamageMultiplier = 0.3f;

            lifetime = 280 / speed;
            hitColor = backColor = trailColor = Items.tungsten.color.cpy();
            trailWidth = 10 / 4f;
            trailLength = 4;
            width = 10f;
            height = 14f;
            ammoMultiplier = 2;
            pierce = true;
            pierceCap = 3;
            rangeChange = 2f * 8f;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            fragOnHit = false;
            fragRandomSpread = 0f;
            fragBullets = 1;
            fragBullet = new ShrapnelBulletType(){{
                damage = 30;
                length = 8;
                width = 2;
                toColor = Items.tungsten.color.cpy();
                pierceCap = 3;
                serrations = 2;
                serrationSpaceOffset = 10f;
            }};
            critEffect = new WrapEffect(Fx.missileTrail, Items.tungsten.color.cpy());
            hitEffect = despawnEffect = Fx.hitBulletColor;
        }};

        PreventCarbide = new CritBulletType(12, 100){{
            critChance = 0.1f;
            critMultiplier = 2f;
            buildingDamageMultiplier = 0.3f;

            lifetime = (280 - 64 + 4 * 8) / speed;
            hitColor = backColor = trailColor = Color.valueOf("ab8ec5");
            trailWidth = 10 / 4f;
            trailLength = 4;
            width = 10f;
            height = 14f;
            ammoMultiplier = 4;
            pierce = true;
            pierceCap = 5;
            reloadMultiplier = 0.5f;
            trailEffect = Fx.disperseTrail;
            trailInterval = 2f;
            rangeChange = 4f * 8f;
            buildingDamageMultiplier = 0.3f;

            fragBullets = 1;
            fragRandomSpread = 10f;

            fragBullet = new BasicBulletType(8f, 90){{
                lifetime = 8f;
                width = 11f;
                height = 14f;
                hitSize = 7f;
                pierceCap = 2;
                pierce = true;
                pierceBuilding = true;
                hitColor = backColor = trailColor = Color.valueOf("ab8ec5");
                frontColor = Color.white;
                trailWidth = 1.8f;
                trailLength = 11;
                hitEffect = despawnEffect = Fx.hitBulletColor;
                buildingDamageMultiplier = 0.2f;
            }};

            trailRotation = true;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;
            critEffect = new WrapEffect(Fx.missileTrail, Color.valueOf("ab8ec5"));
            hitEffect = despawnEffect = Fx.hitBulletColor;
        }};

        ShardTungsten = new CritBulletType(6, 60){{
            critChance = 0.1f;
            critMultiplier = 2f;
            damage = 60;
            lifetime = 200f / 6f;
            splashDamageRadius = 24f;
            splashDamage = 25f;
            buildingDamageMultiplier = 0.2f;
            pierce = true;
            pierceCap = 5;
            knockback = 0.7f;
            backColor = trailColor = hitColor = Items.tungsten.color.cpy();
            width = 10f;
            height = 30f;
            homingPower = 0.08f;
            homingRange = 40f;
            trailLength = 5;
            trailWidth = 2.5f;
            smokeEffect = Fx.shootSmallFlame;
            shootEffect = new MultiEffect(WHFx.shootLineSmall(Pal.lightOrange), Fx.shootBig);
            hitEffect = WHFx.hitSpark(Items.tungsten.color.cpy(), 20, 3, 20, 1f, 5);
            despawnEffect = new MultiEffect(Fx.hitBulletColor, Fx.hitLancer);

            fragRandomSpread = 0f;
            fragBullets = 1;
            fragBullet = new ShrapnelBulletType(){{
                damage = 30;
                pierceArmor = true;
                length = 8;
                width = 2;
                toColor = Items.tungsten.color.cpy();
                pierceCap = 3;
                serrations = 2;
                serrationSpaceOffset = 10f;
            }};
        }};

        ShardMolybdenumAlloy = new CritBulletType(10, 100){{
            critChance = 0.1f;
            critMultiplier = 4;
            lifetime = 200f / 10f;
            splashDamageRadius = 24f;
            splashDamage = 50f;
            buildingDamageMultiplier = 0.2f;
            pierce = true;
            pierceCap = 5;
            knockback = 0.7f;
            backColor = trailColor = hitColor = WHItems.molybdenumAlloy.color.cpy();
            width = 10f;
            height = 30f;
            homingPower = 0.08f;
            homingRange = 40f;
            trailLength = 5;
            trailWidth = 2.5f;
            smokeEffect = Fx.shootSmallFlame;
            shootEffect = new MultiEffect(WHFx.shootLineSmall(Pal.lightOrange), Fx.shootBig);
            hitEffect = WHFx.hitSpark(WHItems.molybdenumAlloy.color.cpy(), 20, 3, 20, 1f, 5);
            despawnEffect = new MultiEffect(WHFx.instBombSize(WHItems.molybdenumAlloy.color.cpy(), 4, 50), Fx.hitBulletColor, Fx.hitLancer);
        }};

        ShardRefineCeramite = new TrailFadeBulletType(6, 150){{

            lifetime = (200f + 48f) / 6f;
            rangeChange = 8 * 6f;
            splashDamageRadius = 24f;
            splashDamage = 35f;
            buildingDamageMultiplier = 0.2f;
            pierce = true;
            pierceCap = 8;
            impact = false;
            pierceBuilding = true;
            knockback = 0.7f;
            lightningColor = backColor = trailColor = hitColor = WHItems.refineCeramite.color.cpy();
            width = 11f;
            height = 30f;
            trailLength = 12;
            trailWidth = 2.5f;

            tracers = 1;
            tracerStroke = 2;
            tracerSpacing = 5;
            tracerRandX = 3;
            tracerFadeOffset = 4;
            tracerStrokeOffset = 9;
            tracerUpdateSpacing = 2;

            addBeginPoint = false;
            hitBlinkTrail = false;
            despawnBlinkTrail = false;

            homingDelay = 10;
            homingPower = 0.12f;
            homingRange = 80;
            followAimSpeed = 12f;
            weaveRandom = true;
            weaveScale = 12f;
            weaveMag = 0.1f;

            lightning = 3;
            lightningDamage = 35;
            lightningLength = 6;
            lightningAngle = 30f;
            smokeEffect = Fx.shootSmallFlame;
            shootEffect = new MultiEffect(WHFx.shootLineSmall(Pal.lightOrange), Fx.shootBig);
            hitEffect = WHFx.hitSpark(WHItems.refineCeramite.color.cpy(), 20, 3, 20, 1f, 5);
            despawnEffect = new MultiEffect(WHFx.instHit(WHItems.refineCeramite.color.cpy(), true, 2, 18), Fx.hitBulletColor, Fx.hitLancer);

        }};

        HeavyHammerThorium = new CritMissileBulletType(){{

            critChance = 0.15f;
            critMultiplier = 1.5f;
            sprite = "shell";
            Color colorOrange = Color.valueOf("ea8878").lerp(Pal.redLight, 0.5f);
            colors = new Color[]{colorOrange.cpy().a(0.4f), colorOrange.cpy().a(0.8f), colorOrange.cpy().lerp(Color.white, 0.8f)};
            lengthOffset = 5;
            flameLength = 18f;
            flameWidth = 2f;
            hitEffect = new MultiEffect(WHFx.circleOut(colorOrange, 50f), Fx.titanExplosionSmall, Fx.titanSmoke);
            despawnEffect = Fx.none;
            knockback = 3f;
            speed = 5f;
            height = 28f;
            width = 15f;
            ammoMultiplier = 3f;
            damage = 300f;
            splashDamageRadius = 64f;
            splashDamage = 350f;
            collidesTiles = false;
            collides = false;
            collidesAir = false;
            scaleLife = true;
            reloadMultiplier = 1;
            scaledSplashDamage = true;
            backColor = hitColor = trailColor = Color.valueOf("ea8878").lerp(Pal.redLight, 0.5f);
            frontColor = Color.white;
            hitSound = Sounds.titanExplosion;

            status = StatusEffects.blasted;

            trailLength = 32;
            trailWidth = 3.35f;
            trailSinScl = 2.5f;
            trailSinMag = 0.5f;
            trailEffect = Fx.disperseTrail;
            trailInterval = 2f;
            despawnShake = 7f;

            shootEffect = Fx.shootTitan;
            smokeEffect = Fx.shootSmokeTitan;
            trailRotation = true;

            trailInterp = v -> Math.max(Mathf.slope(v), 0.8f);
            shrinkX = 0.2f;
            shrinkY = 0.1f;
            buildingDamageMultiplier = 0.5f;
            fragLifeMin = 1.5f;

            fragBullets = 3;
            fragBullet = new CritBulletType(0.5f, 50, "shell"){{
                hitEffect = new MultiEffect(Fx.titanExplosionFrag, Fx.titanLightSmall, new WaveEffect(){{
                    lifetime = 8f;
                    strokeFrom = 1f;
                    sizeTo = 8f;
                }});

                collidesAir = false;
                despawnEffect = Fx.hitBulletColor;
                width = 8f;
                height = 12f;
                lifetime = 50f;
                knockback = 0.5f;
                splashDamageRadius = 22f;
                splashDamage = 50f;
                scaledSplashDamage = true;
                pierceArmor = true;
                backColor = hitColor = Color.valueOf("ea8878").lerp(Pal.redLight, 0.5f);
                frontColor = Color.white;
                buildingDamageMultiplier = 0.3f;
                shrinkY = 0.3f;
            }};
        }};

        HeavyHammerMolybdenumAlloy = new ShieldBreakerType(5, 200, "missile-large", 150){{

            despawnEffect = new MultiEffect(WHFx.instHit(WHItems.molybdenumAlloy.color.cpy(), true, 4, 30),
            WHFx.hitSpark(WHItems.molybdenumAlloy.color.cpy(), 20, 5, 30, 1f, 8));
            hitEffect = Fx.titanExplosionSmall;
            knockback = 3f;
            drag = -0.03f;
            speed = 5f;
            rangeChange = 9 * 8f;
            lifetime = 36.9f;
            height = 40f;
            width = 15f;
            ammoMultiplier = 3f;
            pierce = true;
            pierceCap = 4;
            collidesAir = false;
            backColor = hitColor = trailColor = WHItems.molybdenumAlloy.color.cpy();
            hitSound = Sounds.titanExplosion;

            status = StatusEffects.shocked;

            trailLength = 15;
            trailWidth = 3.35f;
            trailSinScl = 2.5f;
            trailSinMag = 0.5f;
            trailEffect = Fx.disperseTrail;
            trailInterval = 1f;
            despawnShake = 7f;

            shootEffect = Fx.shootTitan;
            smokeEffect = Fx.shootSmokeTitan;
            trailRotation = true;

            trailInterp = v -> Math.max(Mathf.slope(v), 0.8f);
            shrinkX = 0.2f;
            shrinkY = 0.1f;
            buildingDamageMultiplier = 0.5f;
            fragLifeMin = 1.5f;

            fragBullets = 3;
            fragRandomSpread = fragAngle = 0;
            fragSpread = 30f;
            fragBullet = new CritBulletType(10f, 80, name("pierce")){{
                despawnEffect = hitEffect = new MultiEffect(hitSpark(WHItems.molybdenumAlloy.color.cpy(), 20, 5, 30, 1f, 8),
                WHFx.instHit(WHItems.molybdenumAlloy.color.cpy(), true, 2, 10));
                critChance = 0.25f;
                critMultiplier = 3f;
                width = 15;
                height = 60f;
                lifetime = (50 + 9 * 8) / 10f;
                trailRotation = true;
                pierceCap = 2;
                trailEffect = Fx.disperseTrail;
                trailChance = 0.8f;
                backColor = hitColor = trailColor = WHItems.molybdenumAlloy.color.cpy();
                trailLength = 8;
                trailWidth = 3;

                fragBullets = 1;
                fragRandomSpread = fragAngle = 0;
                fragBullet = new LaserBulletType(80){
                    {
                        pierceCap = 2;
                        colors = new Color[]{WHItems.molybdenumAlloy.color.cpy().a(0.4f), WHItems.molybdenumAlloy.color.cpy().a(0.8f), WHItems.molybdenumAlloy.color.cpy().lerp(Color.white, 0.8f)};
                        hitColor = trailColor = WHItems.molybdenumAlloy.color;
                        length = 90f;
                        width = 10f;
                        sideLength = 15;
                        sideAngle = 0;
                    }
                };
            }};
        }};

        IonizePhaseFabricBullet = new MultiTrailBulletType(){{
            sprite = name("energy-bullet");
            subTrailWidth = 2;
            hitEffect = despawnEffect = new MultiEffect(WHFx.instHit(WHPal.SkyBlue.cpy(), true, 4, 30),
            WHFx.hitSpark(WHPal.SkyBlue.cpy(), 60, 15, 30, 1f, 8), WHFx.circleOut(60, 40, 2f),
            WHFx.smoothColorCircle(WHPal.SkyBlue, 8 * tilesize, 180));
            knockback = 3f;
            drag = -0.03f;
            speed = 4f;
            lifetime = 46.8f;
            height = 40f;
            width = 12f;
            damage = 200;
            hittable = false;
            frontColor = WHPal.SkyBlueF.cpy();
            backColor = hitColor = trailColor = WHPal.SkyBlue.cpy();
            hitSound = Sounds.titanExplosion;

            status = WHStatusEffects.plasma;
            statusDuration = 30;

            chargeEffect = new MultiEffect(
            WHFx.genericChargeCircle(WHPal.SkyBlue.cpy(), 5, 30, 120),
            WHFx.TrailCharge(WHPal.SkyBlue.cpy(), 20, 2.5f, 30, 3, 120));

            trailEffect = WHFx.square(WHPal.SkyBlue.cpy(), 20f, 1, 25, 4);
            trailLength = 10;
            trailWidth = 3f;
            trailSinScl = 2.5f;
            trailSinMag = 0.5f;
            trailInterval = 1f;

            shootEffect = Fx.shootTitan;
            smokeEffect = Fx.shootSmokeTitan;
            trailRotation = true;

            trailInterp = v -> Math.max(Mathf.slope(v), 0.8f);
            fragBullets = 1;
            fragRandomSpread = fragAngle = 0;
            fragBullet = new DOTBulletType(){{
                lifetime = 120;
                damageInterval = 15;
                DOTRadius = 64;
                radIncrease = 0.5f;
                sprite = "large-orb";
                fx = WHFx.squSpark1;
                lightColor = lightningColor = WHPal.SkyBlue;
                damage = 100;
            }};
        }};

        IonizeResonantCrystalBullet = new LightningLinkerBulletType(5, 200){{

            randomLightningChance = 0.05f;
            hitSpacing = 20;
            maxHit = 12;
            lightningDamage = 100;
            linkRange = 80;
            chargeEffect = new MultiEffect(
            WHFx.genericChargeCircle(WHItems.resonantCrystal.color.cpy(), 5, 30, 120),
            WHFx.TrailCharge(WHItems.resonantCrystal.color.cpy(), 20, 2.5f, 30, 3, 120));

            sprite = name("energy-bullet");
            intervalBullet = WHBulletsOther.IonizeInterval;
            intervalBullets = 2;
            bulletInterval = 15;
            intervalRandomSpread = 0;
            intervalSpread = 180;

            scaleLife = despawnHit = false;

            hittable = false;
            drawCircle = false;
            knockback = 3f;
            speed = 4f;
            drag = -0.02f;
            lifetime = 55.47f;
            height = 40f;
            width = 12f;
            lightning = 4;
            lightningDamage = 40;
            lightningLength = 8;
            splashDamage = 300;
            reloadMultiplier = 0.5f;
            lightningColor = trailColor = backColor = lightColor = hitColor = WHItems.resonantCrystal.color.cpy();
            trailEffect = WHFx.square(WHItems.resonantCrystal.color.cpy(), 20f, 1, 25, 4);
            trailLength = 10;
            trailWidth = 3f;
            trailSinScl = 2.5f;
            trailSinMag = 0.5f;
            trailInterval = 1f;
            hitEffect = despawnEffect = new MultiEffect(
            WHFx.sharpBlast(WHItems.resonantCrystal.color.cpy(), WHItems.resonantCrystal.color.cpy(), 120, 60),
            WHFx.hitSpark(WHItems.resonantCrystal.color.cpy(), 60, 15, 30, 2f, 8),
            WHFx.circleOut(60, 40, 2f),
            WHFx.circleOut(60, 40, 2f).startDelay(15),
            WHFx.fillCircle(WHItems.resonantCrystal.color.cpy(), 10, 60, Interp.pow3Out));
        }};
    }
}

