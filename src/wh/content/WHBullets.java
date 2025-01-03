//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.content;

import arc.func.Boolf;
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
import arc.util.Time;
import arc.util.Tmp;
import java.util.Iterator;
import java.util.Objects;
import mindustry.Vars;
import mindustry.ai.BlockIndexer;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Lightning;
import mindustry.entities.Sized;
import mindustry.entities.Units;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.entities.bullet.FireBulletType;
import mindustry.entities.bullet.FlakBulletType;
import mindustry.entities.bullet.MissileBulletType;
import mindustry.entities.effect.MultiEffect;
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
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import wh.entities.bullet.*;
import wh.entities.effect.WrapperEffect;
import wh.gen.UltFire;
import wh.gen.WHSounds;
import wh.graphics.Drawn;
import wh.graphics.PositionLightning;
import wh.graphics.WHPal;
import wh.math.WHInterp;
import wh.util.WHUtils;

public final class WHBullets {
    public static BulletType basicMissile;
    public static BulletType boidMissle;
    public static BulletType sapArtilleryFrag;
    public static BulletType continuousSapLaser;
    public static BulletType ancientArtilleryProjectile;
    public static BulletType hitter;
    public static BulletType ncBlackHole;
    public static BulletType nuBlackHole;
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
    public static BulletType cMoonExplosion;
    public static BulletType AGFrag;
    public static BulletType tankAG7;
    public static BulletType collaspsePf;
    public static BulletType collapseSp;
    public static BulletType SK;

    private WHBullets() {
    }

    public static void load() {
        basicMissile = new MissileBulletType(4.2F, 15.0F) {
            {
                  homingPower = 0.12F;
                  width = 8.0F;
                  height = 8.0F;
                  shrinkX =   shrinkY = 0.0F;
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
        sapArtilleryFrag = new ArtilleryBulletType(2.3F, 30.0F) {
            {
                  hitEffect = Fx.sapExplosion;
                  knockback = 0.8F;
                  lifetime = 70.0F;
                  width =   height = 20.0F;
                  collidesTiles = false;
                  splashDamageRadius = 70.0F;
                  splashDamage = 60.0F;
                  backColor = Pal.sapBulletBack;
                  frontColor =   lightningColor = Pal.sapBullet;
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
        boidMissle = new BoidBulletType(2.7F, 30.0F) {
            {
                  damage = 50.0F;
                  homingPower = 0.02F;
                  lifetime = 500.0F;
                  keepVelocity = false;
                  shootEffect = Fx.shootHeal;
                  smokeEffect = Fx.hitLaser;
                  hitEffect =   despawnEffect = Fx.hitLaser;
                  hitSound = Sounds.none;
                  healPercent = 5.5F;
                  collidesTeam = true;
                  trailColor = Pal.heal;
                  backColor = Pal.heal;
            }
        };
        continuousSapLaser = new ContinuousLaserBulletType(60.0F) {
            {
                  colors = new Color[]{Pal.sapBulletBack.cpy().a(0.3F), Pal.sapBullet.cpy().a(0.6F), Pal.sapBullet, Color.white};
                  length = 190.0F;
                  width = 5.0F;
                  shootEffect = WHFx.sapPlasmaShoot;
                  hitColor =   lightColor =   lightningColor = Pal.sapBullet;
                  hitEffect = WHFx.coloredHitSmall;
                  status = StatusEffects.sapped;
                  statusDuration = 80.0F;
                  lifetime = 180.0F;
                  incendChance = 0.0F;
                  largeHit = false;
            }

            public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct) {
                super.hitTile(b, build, x, y, initialHealth, direct);
                Entityc var8 = b.owner;
                if (var8 instanceof Healthc) {
                    Healthc owner = (Healthc)var8;
                    owner.heal(Math.max(initialHealth - build.health(), 0.0F) * 0.2F);
                }

            }

            public void hitEntity(Bullet b, Hitboxc entity, float health) {
                super.hitEntity(b, entity, health);
                if (entity instanceof Healthc) {
                    Healthc h = (Healthc)entity;
                    Entityc var6 = b.owner;
                    if (var6 instanceof Healthc) {
                        Healthc owner = (Healthc)var6;
                        owner.heal(Math.max(health - h.health(), 0.0F) * 0.2F);
                    }
                }

            }
        };
        ancientArtilleryProjectile = new ShieldBreakerType(7.0F, 6000.0F, "missile-large", 7000.0F) {
            {
                  backColor =   trailColor =   lightColor =   lightningColor =   hitColor = WHPal.ancientLightMid;
                  frontColor = WHPal.ancientLight;
                  trailEffect = WHFx.hugeTrail;
                  trailParam = 6.0F;
                  trailChance = 0.2F;
                  trailInterval = 3.0F;
                  lifetime = 200.0F;
                  scaleLife = true;
                  trailWidth = 5.0F;
                  trailLength = 55;
                  trailInterp = Interp.slope;
                  lightning = 6;
                  lightningLength =   lightningLengthRand = 22;
                  splashDamage =   damage;
                  lightningDamage =   damage / 15.0F;
                  splashDamageRadius = 120.0F;
                  scaledSplashDamage = true;
                  despawnHit = true;
                  collides = false;
                  shrinkY =   shrinkX = 0.33F;
                  width = 17.0F;
                  height = 55.0F;
                  despawnShake =   hitShake = 12.0F;
                  hitEffect = new MultiEffect(new Effect[]{WHFx.square(  hitColor, 200.0F, 20,   splashDamageRadius + 80.0F, 10.0F), WHFx.lightningHitLarge, WHFx.hitSpark(  hitColor, 130.0F, 85,   splashDamageRadius * 1.5F, 2.2F, 10.0F), WHFx.subEffect(140.0F,   splashDamageRadius + 12.0F, 33, 34.0F, Interp.pow2Out, (i, x, y, rot, fin) -> {
                    float fout = Interp.pow2Out.apply(1.0F - fin);
                    int[] var7 = Mathf.signs;
                    int var8 = var7.length;

                    for(int var9 = 0; var9 < var8; ++var9) {
                        int s = var7[var9];
                        Drawf.tri(x, y, 12.0F * fout, 45.0F * Mathf.curve(fin, 0.0F, 0.1F) * WHFx.fout(fin, 0.25F), rot + (float)(s * 90));
                    }

                })});
                  despawnEffect = WHFx.circleOut(145.0F,   splashDamageRadius + 15.0F, 3.0F);
                  shootEffect = WrapperEffect.wrap(WHFx.missileShoot,   hitColor);
                  smokeEffect = WHFx.instShoot(  hitColor,   frontColor);
                  despawnSound =   hitSound = Sounds.largeExplosion;
                  fragBullets = 22;
                  fragBullet = new BasicBulletType(2.0F, 300.0F, "wh-circle-bolt") {
                    {
                          width =   height = 10.0F;
                          shrinkY =   shrinkX = 0.7F;
                          backColor =   trailColor =   lightColor =   lightningColor =   hitColor = WHPal.ancientLightMid;
                          frontColor = WHPal.ancientLight;
                          trailEffect = Fx.missileTrail;
                          trailParam = 3.5F;
                          splashDamage = 80.0F;
                          splashDamageRadius = 40.0F;
                          lifetime = 18.0F;
                          lightning = 2;
                          lightningLength =   lightningLengthRand = 4;
                          lightningDamage = 30.0F;
                          hitSoundVolume /= 2.2F;
                          despawnShake =   hitShake = 4.0F;
                          despawnSound =   hitSound = Sounds.dullExplosion;
                          trailWidth = 5.0F;
                          trailLength = 35;
                          trailInterp = Interp.slope;
                          despawnEffect = WHFx.blast(  hitColor, 40.0F);
                          hitEffect = WHFx.hitSparkHuge;
                    }
                };
                  fragLifeMax = 5.0F;
                  fragLifeMin = 1.5F;
                  fragVelocityMax = 2.0F;
                  fragVelocityMin = 0.35F;
            }
        };
        hitter = new EffectBulletType(15.0F) {
            {
                  speed = 0.0F;
                  hittable = false;
                  scaledSplashDamage = true;
                  collidesTiles =   collidesGround =   collides =   collidesAir = true;
                  lightningDamage = 1000.0F;
                  lightColor =   lightningColor =   trailColor =   hitColor = WHPal.pop;
                  lightning = 5;
                  lightningLength = 12;
                  lightningLengthRand = 16;
                  splashDamageRadius = 60.0F;
                  hitShake =   despawnShake = 20.0F;
                  hitSound =   despawnSound = Sounds.explosionbig;
                  hitEffect =   despawnEffect = new MultiEffect(new Effect[]{WHFx.square45_8_45, WHFx.hitSparkHuge, WHFx.crossBlast_45});
            }

            public void despawned(Bullet b) {
                if (  despawnHit) {
                      hit(b);
                } else {
                      createUnits(b, b.x, b.y);
                }

                if (!  fragOnHit) {
                      createFrags(b, b.x, b.y);
                }

                  despawnEffect.at(b.x, b.y, b.rotation(),   lightColor);
                  despawnSound.at(b);
                Effect.shake(  despawnShake,   despawnShake, b);
            }

            public void hit(Bullet b, float x, float y) {
                  hitEffect.at(x, y, b.rotation(),   lightColor);
                  hitSound.at(x, y,   hitSoundPitch,   hitSoundVolume);
                Effect.shake(  hitShake,   hitShake, b);
                if (  fragOnHit) {
                      createFrags(b, x, y);
                }

                  createPuddles(b, x, y);
                  createIncend(b, x, y);
                  createUnits(b, x, y);
                if (  suppressionRange > 0.0F) {
                    Damage.applySuppression(b.team, b.x, b.y,   suppressionRange,   suppressionDuration, 0.0F,   suppressionEffectChance, new Vec2(b.x, b.y));
                }

                  createSplashDamage(b, x, y);

                for(int i = 0; i <   lightning; ++i) {
                    Lightning.create(b,   lightColor,   lightningDamage < 0.0F ?   damage :   lightningDamage, b.x, b.y, b.rotation() + Mathf.range(  lightningCone / 2.0F) +   lightningAngle,   lightningLength + Mathf.random(  lightningLengthRand));
                }

            }
        };
        ncBlackHole = new EffectBulletType(120.0F) {
            {
                  despawnHit = true;
                  splashDamageRadius = 240.0F;
                  hittable = false;
                  lightColor = WHPal.pop;
                  lightningDamage = 1000.0F;
                  lightning = 2;
                  lightningLength = 4;
                  lightningLengthRand = 8;
                  scaledSplashDamage = true;
                  collidesAir =   collidesGround =   collidesTiles = true;
            }

            public void draw(Bullet b) {
                if (b.data instanceof Seq) {
                    Seq<Sized> data = (Seq)b.data;
                    Draw.color(  lightColor, Color.white, b.fin() * 0.7F);
                    Draw.alpha(b.fin(Interp.pow3Out) * 1.1F);
                    Lines.stroke(2.0F * b.fout());
                    Iterator var3 = data.iterator();

                    while(var3.hasNext()) {
                        Sized s = (Sized)var3.next();
                        if (s instanceof Building) {
                            Fill.square(s.getX(), s.getY(), s.hitSize() / 2.0F);
                        } else {
                            Lines.spikes(s.getX(), s.getY(), s.hitSize() * (0.5F + b.fout() * 2.0F), s.hitSize() / 2.0F * b.fslope() + 12.0F * b.fin(), 4, 45.0F);
                        }
                    }

                    Drawf.light(b.x, b.y, b.fdata,   lightColor, 0.3F + b.fin() * 0.8F);
                }
            }

            public void hitT(Sized target, Entityc o, Team team, float x, float y) {
                for(int i = 0; i <   lightning; ++i) {
                    Lightning.create(team,   lightColor,   lightningDamage, x, y, (float)Mathf.random(360),   lightningLength + Mathf.random(  lightningLengthRand));
                }

                if (target instanceof Unit) {
                    Unit unit = (Unit)target;
                    if (unit.health > 1000.0F) {
                        WHBullets.hitter.create(o, team, x, y, 0.0F);
                    }
                }

            }

            @Override
            public void update(Bullet b){
                super.update(b);

                if(!(b.data instanceof Seq))return;
                Seq<Sized> data = (Seq<Sized>)b.data;
                data.remove(d -> !((Healthc)d).isValid());
            }


            public void despawned(Bullet b) {
                super.despawned(b);
                float rad = 33.0F;
                Vec2 v = (new Vec2()).set(b);

                for(int i = 0; i < 5; ++i) {
                    Time.run((float)i * 0.35F + (float)Mathf.random(2), () -> {
                        Tmp.v1.rnd(rad / 3.0F).scl(Mathf.random());
                        WHFx.shuttle.at(v.x + Tmp.v1.x, v.y + Tmp.v1.y, Tmp.v1.angle(),   lightColor, Mathf.random(rad * 3.0F, rad * 12.0F));
                    });
                }

                if (b.data instanceof Seq) {
                    Entityc o = b.owner();
                    Seq<Sized> data = (Seq)b.data;
                    Iterator var6 = data.iterator();

                    while(var6.hasNext()) {
                        Sized s = (Sized)var6.next();
                        float size = Math.min(s.hitSize(), 85.0F);
                        Time.run((float)Mathf.random(44), () -> {
                            if (Mathf.chance(0.3) || data.size < 8) {
                                WHFx.shuttle.at(s.getX(), s.getY(), 45.0F,   lightColor, Mathf.random(size * 3.0F, size * 12.0F));
                            }

                              hitT(s, o, b.team, s.getX(), s.getY());
                        });
                    }

                      createSplashDamage(b, b.x, b.y);
                }
            }
            @Override
            public void init(Bullet b){
                super.init(b);
                b.fdata = splashDamageRadius;

                Seq<Sized> data = new Seq<>();

                Vars.indexer.eachBlock(null, b.x, b.y, b.fdata, bu -> bu.team != b.team, data::add);

                Groups.unit.intersect(b.x - b.fdata / 2, b.y - b.fdata / 2, b.fdata, b.fdata, u -> {
                    if(u.team != b.team)data.add(u);
                });

                b.data = data;

            }
            };
        nuBlackHole = new EffectBulletType(20.0F) {
            {
                  despawnHit = true;
                  hitColor = WHPal.pop;
                  splashDamageRadius = 36.0F;
                  lightningDamage = 600.0F;
                  lightning = 2;
                  lightningLength = 6;
                  lightningLengthRand = 4;
                  scaledSplashDamage = false;
                  collidesAir =   collidesGround =   collidesTiles = true;
                  splashDamage = 1000.0F;
                  damage = 1000.0F;
            }

            public void draw(Bullet b) {
                if (b.data instanceof Seq) {
                    Seq<Sized> data = (Seq)b.data;
                    Draw.color(b.team.color, Color.white, b.fin() * 0.7F);
                    Draw.alpha(b.fin(Interp.pow3Out) * 1.1F);
                    Lines.stroke(2.0F * b.fout());
                    Iterator var3 = data.iterator();

                    while(var3.hasNext()) {
                        Sized s = (Sized)var3.next();
                        if (s instanceof Building) {
                            Fill.square(s.getX(), s.getY(), s.hitSize() / 2.0F);
                        } else {
                            Lines.spikes(s.getX(), s.getY(), s.hitSize() * (0.5F + b.fout() * 2.0F), s.hitSize() / 2.0F * b.fslope() + 12.0F * b.fin(), 4, 45.0F);
                        }
                    }

                    Drawf.light(b.x, b.y, b.fdata,   hitColor, 0.3F + b.fin() * 0.8F);
                }
            }

            public void hitT(Entityc o, Team team, float x, float y) {
                for(int i = 0; i <   lightning; ++i) {
                    Lightning.create(team, team.color,   lightningDamage, x, y, (float)Mathf.random(360),   lightningLength + Mathf.random(  lightningLengthRand));
                }

                WHBullets.hitter.create(o, team, x, y, 0.0F, 500.0F, 1.0F, 1.0F, (Object)null);
            }

            public void update(Bullet b) {
                super.update(b);
                if (b.data instanceof Seq && !b.timer(0, 10.0F)) {
                    Seq<Sized> data = (Seq)b.data;
                    data.remove((d) -> {
                        return !((Healthc)d).isValid();
                    });
                }
            }

            public void despawned(Bullet b) {
                super.despawned(b);
                float rad = 20.0F;
                if (b.data instanceof Seq) {
                    Entityc o = b.owner();
                    Seq<Sized> data = (Seq)b.data;

                    Sized s;
                    for(Iterator var5 = data.iterator(); var5.hasNext();   hitT(o, b.team, s.getX(), s.getY())) {
                        s = (Sized)var5.next();
                        float size = Math.min(s.hitSize(), 75.0F);
                        if (Mathf.chance(0.2) || data.size < 8) {
                            float sd = Mathf.random(size * 3.0F, size * 12.0F);
                            WHFx.shuttleDark.at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), 45.0F, b.team.color, sd);
                        }
                    }

                      createSplashDamage(b, b.x, b.y);
                }
            }


            @Override
            public void init(Bullet b){
                super.init(b);
                b.fdata = splashDamageRadius;

                Seq<Sized> data = new Seq<>();

                Vars.indexer.eachBlock(null, b.x, b.y, b.fdata, bu -> bu.team != b.team, data::add);

                Groups.unit.intersect(b.x - b.fdata / 2, b.y - b.fdata / 2, b.fdata, b.fdata, u -> {
                    if(u.team != b.team)data.add(u);
                });

                b.data = data;

            }
        };
        ultFireball = new FireBulletType(1.0F, 10.0F) {
            {
                  colorFrom =   colorMid = Pal.techBlue;
                  lifetime = 12.0F;
                  radius = 4.0F;
                  trailEffect = WHFx.ultFireBurn;
            }

            public void draw(Bullet b) {
                Draw.color(  colorFrom,   colorMid,   colorTo, b.fin());
                Fill.square(b.x, b.y,   radius * b.fout(), 45.0F);
                Draw.reset();
            }

            public void update(Bullet b) {
                if (Mathf.chanceDelta((double)  fireTrailChance)) {
                    UltFire.create(b.tileOn());
                }

                if (Mathf.chanceDelta((double)  fireEffectChance)) {
                      trailEffect.at(b.x, b.y);
                }

                if (Mathf.chanceDelta((double)  fireEffectChance2)) {
                      trailEffect2.at(b.x, b.y);
                }

            }
        };
        executor = new TrailFadeBulletType(28.0F, 1800.0F) {
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
                  hitColor =   backColor =   lightColor =   lightningColor = WHPal.ancient;
                  trailColor = WHPal.ancientLightMid;
                  frontColor = WHPal.ancientLight;
                  width = 18.0F;
                  height = 60.0F;
                  homingPower = 0.01F;
                  homingRange = 300.0F;
                  homingDelay = 5.0F;
                  hitSound = Sounds.plasmaboom;
                  despawnShake =   hitShake = 18.0F;
                  statusDuration = 1200.0F;
                  pierce =   pierceArmor =   pierceBuilding = true;
                  lightning = 3;
                  lightningLength = 6;
                  lightningLengthRand = 18;
                  lightningDamage = 400.0F;
                  smokeEffect = WrapperEffect.wrap(WHFx.hitSparkHuge,   hitColor);
                  shootEffect = WHFx.instShoot(  backColor,   frontColor);
                  despawnEffect = WHFx.lightningHitLarge;
                  hitEffect = new MultiEffect(new Effect[]{WHFx.hitSpark(  backColor, 75.0F, 24, 90.0F, 2.0F, 12.0F), WHFx.square45_6_45, WHFx.lineCircleOut(  backColor, 18.0F, 20.0F, 2.0F), WHFx.sharpBlast(  backColor,   frontColor, 120.0F, 40.0F)});
            }

            public void createFrags(Bullet b, float x, float y) {
                super.createFrags(b, x, y);
                WHBullets.nuBlackHole.create(b, x, y, 0.0F);
            }
        };
        basicSkyFrag = new BasicBulletType(3.8F, 50.0F) {
            {
                  speed = 6.0F;
                  trailLength = 12;
                  trailWidth = 2.0F;
                  lifetime = 60.0F;
                  despawnEffect = WHFx.square45_4_45;
                  knockback = 4.0F;
                  width = 15.0F;
                  height = 37.0F;
                  lightningDamage =   damage * 0.65F;
                  backColor =   lightColor =   lightningColor =   trailColor =   hitColor =   frontColor = Pal.techBlue;
                  lightning = 2;
                  lightningLength =   lightningLengthRand = 3;
                  smokeEffect = Fx.shootBigSmoke2;
                  trailChance = 0.2F;
                  trailEffect = WHFx.skyTrail;
                  drag = 0.015F;
                  hitShake = 2.0F;
                  hitSound = Sounds.explosion;
                  hitEffect = new Effect(45.0F, (e) -> {
                    Fx.rand.setSeed((long)e.id);
                    Draw.color(  lightColor, e.fin());
                    Lines.stroke(1.75F * e.fout());
                    Lines.spikes(e.x, e.y, (float)Fx.rand.random(14, 28) * e.finpow(), (float)Fx.rand.random(1, 5) * e.fout() + (float)Fx.rand.random(5, 8) * e.fin(WHInterp.parabola4Reversed), 4, 45.0F);
                    Lines.square(e.x, e.y, (float)Fx.rand.random(4, 14) * e.fin(Interp.pow3Out), 45.0F);
                });
            }

            public void hit(Bullet b) {
                super.hit(b);
                UltFire.createChance(b, 12.0F, 0.0075F);
            }
        };
        annMissile = new BasicBulletType(5.6F, 80.0F, "wh-strike") {
            {
                  trailColor =   lightningColor =   backColor =   lightColor =   frontColor = Pal.techBlue;
                  lightning = 3;
                  lightningCone = 360.0F;
                  lightningLengthRand =   lightningLength = 9;
                  splashDamageRadius = 60.0F;
                  splashDamage =   lightningDamage =   damage * 0.7F;
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
                  hitEffect = WHFx.lightningHitLarge(  lightColor);
                  shootEffect = WHFx.hugeSmokeGray;
                  smokeEffect = new Effect(45.0F, (e) -> {
                    Draw.color(  lightColor, Color.white, e.fout() * 0.7F);
                    Angles.randLenVectors((long)e.id, 8, 5.0F + 55.0F * e.fin(), e.rotation, 45.0F, (x, y) -> {
                        Fill.circle(e.x + x, e.y + y, e.fout() * 3.0F);
                    });
                });
                  despawnEffect = new Effect(32.0F, (e) -> {
                    Draw.color(Color.gray);
                    Angles.randLenVectors((long)(e.id + 1), 8, 2.0F + 30.0F * e.finpow(), (x, y) -> {
                        Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F);
                    });
                    Draw.color(  lightColor, Color.white, e.fin());
                    Lines.stroke(e.fout() * 2.0F);
                    Fill.circle(e.x, e.y, e.fout() * e.fout() * 13.0F);
                    Angles.randLenVectors((long)e.id, 4, 7.0F + 40.0F * e.fin(), (x, y) -> {
                        Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 8.0F + 3.0F);
                    });
                });
            }
        };
        vastBulletAccel = new AccelBulletType(2.0F, 180.0F) {
            {
                  width = 22.0F;
                  height = 40.0F;
                  velocityBegin = 1.0F;
                  velocityIncrease = 11.0F;
                  accelInterp = WHInterp.inOut;
                  accelerateBegin = 0.045F;
                  accelerateEnd = 0.675F;
                  pierceCap = 3;
                  splashDamage =   damage / 4.0F;
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
                  lightningDamage =   damage / 4.0F;
                  shootEffect =   smokeEffect = Fx.none;
                  hitEffect =   despawnEffect = new MultiEffect(new Effect[]{new Effect(65.0F, (b) -> {
                    Draw.color(b.color);
                    Fill.circle(b.x, b.y, 6.0F * b.fout(Interp.pow3Out));
                    Angles.randLenVectors((long)b.id, 6, 35.0F * b.fin() + 5.0F, (x, y) -> {
                        Fill.circle(b.x + x, b.y + y, 4.0F * b.fout(Interp.pow2Out));
                    });
                }), WHFx.hitSparkLarge});
                  despawnHit = false;
                  rangeOverride = 480.0F;
            }

            public void update(Bullet b) {
                super.update(b);
            }

            public void updateTrailEffects(Bullet b) {
                if (  trailChance > 0.0F && Mathf.chanceDelta((double)  trailChance)) {
                      trailEffect.at(b.x, b.y,   trailRotation ? b.rotation() :   trailParam, b.team.color);
                }

                if (  trailInterval > 0.0F && b.timer(0,   trailInterval)) {
                      trailEffect.at(b.x, b.y,   trailRotation ? b.rotation() :   trailParam, b.team.color);
                }

            }

            public void hit(Bullet b, float x, float y) {
                b.hit = true;
                  hitEffect.at(x, y, b.rotation(), b.team.color);
                  hitSound.at(x, y,   hitSoundPitch,   hitSoundVolume);
                Effect.shake(  hitShake,   hitShake, b);
                if (  splashDamageRadius > 0.0F && !b.absorbed) {
                    Damage.damage(b.team, x, y,   splashDamageRadius,   splashDamage * b.damageMultiplier(),   collidesAir,   collidesGround);
                    if (  status != StatusEffects.none) {
                        Damage.status(b.team, x, y,   splashDamageRadius,   status,   statusDuration,   collidesAir,   collidesGround);
                    }
                }

                for(int i = 0; i <   lightning; ++i) {
                    Lightning.create(b, b.team.color,   lightningDamage < 0.0F ?   damage :   lightningDamage, b.x, b.y, b.rotation() + Mathf.range(  lightningCone / 2.0F) +   lightningAngle,   lightningLength + Mathf.random(  lightningLengthRand));
                }

                Entityc var5 = b.owner;
                if (var5 instanceof Unit) {
                    Unit from = (Unit)var5;
                    if (!from.dead && from.isAdded() && !(from.healthf() > 0.99F)) {
                        WHFx.chainLightningFade.at(b.x, b.y, (float)Mathf.random(12, 20), b.team.color, from);
                        from.heal(  damage / 8.0F);
                    }
                }
            }

            public void despawned(Bullet b) {
                  despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);
                Effect.shake(  despawnShake,   despawnShake, b);
            }

            public void removed(Bullet b) {
                if (  trailLength > 0 && b.trail != null && b.trail.size() > 0) {
                    Fx.trailFade.at(b.x, b.y,   trailWidth, b.team.color, b.trail.copy());
                }

            }

            public void init(Bullet b) {
                super.init(b);
                b.vel.rotate((float)WHUtils.rand((long)b.id).random(360));
            }

            public void draw(Bullet b) {
                Tmp.c1.set(b.team.color).lerp(Color.white, Mathf.absin(4.0F, 0.3F));
                if (  trailLength > 0 && b.trail != null) {
                    float z = Draw.z();
                    Draw.z(z - 0.01F);
                    b.trail.draw(Tmp.c1,   trailWidth);
                    Draw.z(z);
                }

                Draw.color(b.team.color, Color.white, 0.35F);
                Drawn.arrow(b.x, b.y, 5.0F, 35.0F, -6.0F, b.rotation());
                Draw.color(Tmp.c1);
                Drawn.arrow(b.x, b.y, 5.0F, 35.0F, 12.0F, b.rotation());
                Draw.reset();
            }
        };
        vastBulletLightningBall = new LightningLinkerBulletType(3.0F, 120.0F) {
            public final Effect RshootEffect;
            public final Effect RsmokeEffect;

            {
                  lifetime = 120.0F;
                  keepVelocity = false;
                  lightningDamage =   damage =   splashDamage = 80.0F;
                  splashDamageRadius = 50.0F;
                  homingDelay = 20.0F;
                  homingRange = 300.0F;
                  homingPower = 0.025F;
                  smokeEffect =   shootEffect = Fx.none;
                  effectLingtning = 0;
                  maxHit = 6;
                  hitShake =   despawnShake = 5.0F;
                  hitSound =   despawnSound = Sounds.plasmaboom;
                  size = 7.2F;
                  trailWidth = 3.0F;
                  trailLength = 16;
                  linkRange = 80.0F;
                  scaleLife = false;
                  despawnHit = false;
                  collidesAir =   collidesGround = true;
                  despawnEffect =   hitEffect = new MultiEffect(new Effect[]{WHFx.lightningHitLarge, WHFx.hitSparkHuge});
                  trailEffect =   slopeEffect = WHFx.trailFromWhite;
                  spreadEffect = (new Effect(32.0F, (e) -> {
                    Angles.randLenVectors((long)e.id, 2, 6.0F + 45.0F * e.fin(), (x, y) -> {
                        Draw.color(e.color);
                        Fill.circle(e.x + x, e.y + y, e.fout() *   size / 2.0F);
                        Draw.color(Color.black);
                        Fill.circle(e.x + x, e.y + y, e.fout() * (  size / 3.0F - 1.0F));
                    });
                })).layer(110.00001F);
                  RshootEffect = new Effect(24.0F, (e) -> {
                    e.scaled(10.0F, (b) -> {
                        Draw.color(e.color);
                        Lines.stroke(b.fout() * 3.0F + 0.2F);
                        Lines.circle(b.x, b.y, b.fin() * 70.0F);
                    });
                    Draw.color(e.color);
                    int[] var1 = Mathf.signs;
                    int var2 = var1.length;

                    int var3;
                    int i;
                    for(var3 = 0; var3 < var2; ++var3) {
                        i = var1[var3];
                        Drawn.tri(e.x, e.y, 8.0F * e.fout(), 85.0F, e.rotation + 90.0F * (float)i);
                    }

                    Draw.color(Color.black);
                    var1 = Mathf.signs;
                    var2 = var1.length;

                    for(var3 = 0; var3 < var2; ++var3) {
                        i = var1[var3];
                        Drawn.tri(e.x, e.y, 3.0F * e.fout(), 38.0F, e.rotation + 90.0F * (float)i);
                    }

                });
                  RsmokeEffect = WHFx.hitSparkLarge;
            }

            public Color getColor(Bullet b) {
                return Tmp.c1.set(b.team.color).lerp(Color.white, 0.1F + Mathf.absin(4.0F, 0.15F));
            }

            public void update(Bullet b) {
                  updateTrail(b);
                  updateHoming(b);
                  updateWeaving(b);
                  updateBulletInterval(b);
                Effect.shake(  hitShake,   hitShake, b);
                if (b.timer(5,   hitSpacing)) {
                      slopeEffect.at(b.x + Mathf.range(  size / 4.0F), b.y + Mathf.range(  size / 4.0F), Mathf.random(2.0F, 4.0F), b.team.color);
                      spreadEffect.at(b.x, b.y, b.team.color);
                    PositionLightning.createRange(b,   collidesAir,   collidesGround, b, b.team,   linkRange,   maxHit, b.team.color, Mathf.chanceDelta((double)  randomLightningChance),   lightningDamage,   lightningLength, 2.5F,   boltNum, (p) -> {
                          liHitEffect.at(p.getX(), p.getY(), b.team.color);
                    });
                }

                if (Mathf.chanceDelta(0.1)) {
                      slopeEffect.at(b.x + Mathf.range(  size / 4.0F), b.y + Mathf.range(  size / 4.0F), Mathf.random(2.0F, 4.0F), b.team.color);
                      spreadEffect.at(b.x, b.y, b.team.color);
                }

                if (  randomGenerateRange > 0.0F && Mathf.chance((double)(Time.delta *   randomGenerateChance)) && b.lifetime - b.time > PositionLightning.lifetime) {
                    PositionLightning.createRandomRange(b, b.team, b,   randomGenerateRange,   backColor, Mathf.chanceDelta((double)  randomLightningChance), 0.0F, 0,   boltWidth,   boltNum,   randomLightningNum, (hitPos) -> {
                          randomGenerateSound.at(hitPos, Mathf.random(0.9F, 1.1F));
                        Damage.damage(b.team, hitPos.getX(), hitPos.getY(),   splashDamageRadius / 8.0F,   splashDamage * b.damageMultiplier() / 8.0F,   collidesAir,   collidesGround);
                        WHFx.lightningHitLarge.at(hitPos.getX(), hitPos.getY(), b.team.color);
                          hitModifier.get(hitPos);
                    });
                }

                if (Mathf.chanceDelta((double)  effectLightningChance) && b.lifetime - b.time > Fx.chainLightning.lifetime) {
                    for(int i = 0; i <   effectLingtning; ++i) {
                        Vec2 v = randVec.rnd(  effectLightningLength + Mathf.random(  effectLightningLengthRand)).add(b).add(Tmp.v1.set(b.vel).scl(Fx.chainLightning.lifetime / 2.0F));
                        Fx.chainLightning.at(b.x, b.y, 12.0F, b.team.color, v.cpy());
                        WHFx.lightningHitSmall.at(v.x, v.y, 20.0F, b.team.color);
                    }
                }

            }

            public void init(Bullet b) {
                super.init(b);
                b.lifetime *= Mathf.randomSeed((long)b.id, 0.875F, 1.125F);
                  RsmokeEffect.at(b.x, b.y, b.team.color);
                  RshootEffect.at(b.x, b.y, b.rotation(), b.team.color);
            }

            public void drawTrail(Bullet b) {
                if (  trailLength > 0 && b.trail != null) {
                    float z = Draw.z();
                    Draw.z(z - 1.0E-4F);
                    b.trail.draw(  getColor(b),   trailWidth);
                    Draw.z(z);
                }

            }

            public void draw(Bullet b) {
                  drawTrail(b);
                Draw.color(Tmp.c1);
                Fill.circle(b.x, b.y,   size);
                float[] param = new float[]{9.0F, 28.0F, 1.0F, 9.0F, 22.0F, -1.25F, 12.0F, 16.0F, -0.45F};

                for(int i = 0; i < param.length / 3; ++i) {
                    int[] var4 = Mathf.signs;
                    int var5 = var4.length;

                    for(int var6 = 0; var6 < var5; ++var6) {
                        int j = var4[var6];
                        Drawf.tri(b.x, b.y, param[i * 3] * b.fout(), param[i * 3 + 1] * b.fout(), b.rotation() + 90.0F * (float)j + param[i * 3 + 2] * Time.time);
                    }
                }

                Draw.color(Color.black);
                Fill.circle(b.x, b.y,   size / 6.125F +   size / 3.0F * Mathf.curve(b.fout(), 0.1F, 0.35F));
                Drawf.light(b.x, b.y,   size * 6.85F, b.team.color, 0.7F);
            }

            public void despawned(Bullet b) {
                PositionLightning.createRandomRange(b, b.team, b,   randomGenerateRange, b.team.color, Mathf.chanceDelta((double)  randomLightningChance), 0.0F, 0,   boltWidth,   boltNum,   randomLightningNum, (hitPos) -> {
                    Damage.damage(b.team, hitPos.getX(), hitPos.getY(),   splashDamageRadius,   splashDamage * b.damageMultiplier(),   collidesAir,   collidesGround);
                    WHFx.lightningHitLarge.at(hitPos.getX(), hitPos.getY(), b.team.color);
                      liHitEffect.at(hitPos);

                    for(int j = 0; j <   lightning; ++j) {
                        Lightning.create(b, b.team.color,   lightningDamage < 0.0F ?   damage :   lightningDamage, b.x, b.y, b.rotation() + Mathf.range(  lightningCone / 2.0F) +   lightningAngle,   lightningLength + Mathf.random(  lightningLengthRand));
                    }

                      hitSound.at(hitPos, Mathf.random(0.9F, 1.1F));
                      hitModifier.get(hitPos);
                });
                if (  despawnHit) {
                      hit(b);
                } else {
                      createUnits(b, b.x, b.y);
                }

                if (!  fragOnHit) {
                      createFrags(b, b.x, b.y);
                }

                  despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);
                  despawnSound.at(b);
                Effect.shake(  despawnShake,   despawnShake, b);
            }

            public void hit(Bullet b, float x, float y) {
                  hitEffect.at(x, y, b.rotation(), b.team.color);
                  hitSound.at(x, y,   hitSoundPitch,   hitSoundVolume);
                Effect.shake(  hitShake,   hitShake, b);
                if (  fragOnHit) {
                      createFrags(b, x, y);
                }

                  createPuddles(b, x, y);
                  createIncend(b, x, y);
                  createUnits(b, x, y);
                if (  suppressionRange > 0.0F) {
                    Damage.applySuppression(b.team, b.x, b.y,   suppressionRange,   suppressionDuration, 0.0F,   suppressionEffectChance, new Vec2(b.x, b.y));
                }

                  createSplashDamage(b, x, y);

                for(int i = 0; i <   lightning; ++i) {
                    Lightning.create(b, b.team.color,   lightningDamage < 0.0F ?   damage :   lightningDamage, b.x, b.y, b.rotation() + Mathf.range(  lightningCone / 2.0F) +   lightningAngle,   lightningLength + Mathf.random(  lightningLengthRand));
                }

            }

            public void removed(Bullet b) {
                if (  trailLength > 0 && b.trail != null && b.trail.size() > 0) {
                    Fx.trailFade.at(b.x, b.y,   trailWidth, b.team.color, b.trail.copy());
                }

            }
        };
        vastBulletStrafeLaser = new StrafeLaserBulletType(0.0F, 300.0F) {
            {
                  strafeAngle = 0.0F;
            }

            public void init(Bullet b) {
                super.init(b);
                Sounds.laserblast.at(b);
            }

            public void hit(Bullet b, float x, float y) {
                super.hit(b, x, y);
                Entityc var5 = b.owner;
                if (var5 instanceof Unit) {
                    Unit from = (Unit)var5;
                    if (from.dead || !from.isAdded() || from.healthf() > 0.99F) {
                        return;
                    }

                    from.heal(  damage / 20.0F);
                    if (Vars.headless) {
                        return;
                    }

                    PositionLightning.createEffect(b, from, b.team.color, 2, Mathf.random(1.5F, 3.0F));
                }

            }

            public void draw(Bullet b) {
                Tmp.c1.set(b.team.color).lerp(Color.white, Mathf.absin(4.0F, 0.1F));
                super.draw(b);
                Draw.z(110.0F);
                float fout = b.fout(0.25F) * Mathf.curve(b.fin(), 0.0F, 0.125F);
                Draw.color(Tmp.c1);
                Fill.circle(b.x, b.y,   width / 1.225F * fout);
                Entityc var4 = b.owner;
                if (var4 instanceof Unit) {
                    Unit unit = (Unit)var4;
                    if (!unit.dead) {
                        Draw.z(100.0F);
                        Lines.stroke((  width / 3.0F + Mathf.absin(Time.time, 4.0F, 0.8F)) * fout);
                        Lines.line(b.x, b.y, unit.x, unit.y, false);
                    }
                }

                int[] var7 = Mathf.signs;
                int var9 = var7.length;

                for(int var5 = 0; var5 < var9; ++var5) {
                    int i = var7[var5];
                    Drawn.tri(b.x, b.y, 6.0F * fout, 10.0F + 50.0F * fout, Time.time * 1.5F + (float)(90 * i));
                    Drawn.tri(b.x, b.y, 6.0F * fout, 20.0F + 60.0F * fout, Time.time * -1.0F + (float)(90 * i));
                }

                Draw.z(110.001F);
                Draw.color(b.team.color, Color.white, 0.25F);
                Fill.circle(b.x, b.y,   width / 1.85F * fout);
                Draw.color(Color.black);
                Fill.circle(b.x, b.y,   width / 2.155F * fout);
                Draw.z(100.0F);
                Draw.reset();
                float rotation =   dataRot ? b.fdata : b.rotation() +   getRotation(b);
                float maxRangeFout =   maxRange * fout;
                float realLength = WHUtils.findLaserLength(b, rotation, maxRangeFout);
                Tmp.v1.trns(rotation, realLength);
                Tmp.v2.trns(rotation, 0.0F,   width / 2.0F * fout);
                Tmp.v3.setZero();
                if (realLength < maxRangeFout) {
                    Tmp.v3.set(Tmp.v2).scl((maxRangeFout - realLength) / maxRangeFout);
                }

                Draw.color(Tmp.c1);
                Tmp.v2.scl(0.9F);
                Tmp.v3.scl(0.9F);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
                if (realLength < maxRangeFout) {
                    Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
                }

                Tmp.v2.scl(1.2F);
                Tmp.v3.scl(1.2F);
                Draw.alpha(0.5F);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
                if (realLength < maxRangeFout) {
                    Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
                }

                Draw.alpha(1.0F);
                Draw.color(Color.black);
                Draw.z(Draw.z() + 0.01F);
                Tmp.v2.scl(0.5F);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + (Tmp.v1.x + Tmp.v3.x) / 3.0F, b.y + (Tmp.v1.y + Tmp.v3.y) / 3.0F, b.x + (Tmp.v1.x - Tmp.v3.x) / 3.0F, b.y + (Tmp.v1.y - Tmp.v3.y) / 3.0F);
                Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y,   width * 1.5F,   getColor(b), 0.7F);
                Draw.reset();
                Draw.z(Draw.z() - 0.01F);
            }
        };
        hyperBlast = new BasicBulletType(3.3F, 400.0F) {
            {
                  lifetime = 60.0F;
                  trailLength = 15;
                  drawSize = 250.0F;
                  drag = 0.0075F;
                  despawnEffect =   hitEffect = WHFx.lightningHitLarge(Pal.techBlue);
                  knockback = 12.0F;
                  width = 15.0F;
                  height = 37.0F;
                  splashDamageRadius = 40.0F;
                  splashDamage =   lightningDamage =   damage * 0.75F;
                  backColor =   lightColor =   lightningColor =   trailColor = Pal.techBlue;
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
        hyperBlastLinker = new LightningLinkerBulletType(5.0F, 220.0F) {
            {
                  effectLightningChance = 0.15F;
                  backColor =   trailColor =   lightColor =   lightningColor =   hitColor = Pal.techBlue;
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
                  splashDamage =   lightningDamage =   damage / 4.0F;
                  lifetime = 50.0F;
                  scaleLife = false;
                  despawnEffect = WHFx.lightningHitLarge(  hitColor);
                  hitEffect = new MultiEffect(new Effect[]{WHFx.hitSpark(  backColor, 65.0F, 22,   splashDamageRadius, 4.0F, 16.0F), WHFx.blast(  backColor,   splashDamageRadius / 2.0F)});
                  shootEffect = WHFx.hitSpark(  backColor, 45.0F, 12, 60.0F, 3.0F, 8.0F);
                  smokeEffect = WHFx.hugeSmokeGray;
            }
        };
        arc9000frag = new FlakBulletType(3.75F, 200.0F) {
            {
                  trailColor =   lightColor =   lightningColor =   backColor =   frontColor = Pal.techBlue;
                  trailLength = 14;
                  trailWidth = 2.7F;
                  trailRotation = true;
                  trailInterval = 3.0F;
                  trailEffect = WHFx.polyTrail(  backColor,   frontColor, 4.65F, 22.0F);
                  trailChance = 0.0F;
                  despawnEffect =   hitEffect = WHFx.techBlueExplosion;
                  knockback = 12.0F;
                  lifetime = 90.0F;
                  width = 17.0F;
                  height = 42.0F;
                  hittable = false;
                  collidesTiles = false;
                  splashDamageRadius = 60.0F;
                  splashDamage =   damage * 0.6F;
                  lightning = 3;
                  lightningLength = 8;
                  smokeEffect = Fx.shootBigSmoke2;
                  hitShake = 8.0F;
                  hitSound = Sounds.plasmaboom;
                  status = StatusEffects.shocked;
            }
        };
        arc9000 = new LightningLinkerBulletType(2.75F, 200.0F) {
            {
                  trailWidth = 4.5F;
                  trailLength = 66;
                  chargeEffect = new MultiEffect(new Effect[]{WHFx.techBlueCharge, WHFx.techBlueChargeBegin});
                  spreadEffect =   slopeEffect = Fx.none;
                  trailEffect = WHFx.hitSparkHuge;
                  trailInterval = 5.0F;
                  backColor =   trailColor =   hitColor =   lightColor =   lightningColor =   frontColor = Pal.techBlue;
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
                  intervalBullet =   fragBullet = WHBullets.arc9000frag;
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
                  despawnEffect = WHFx.circleOut(  hitColor,   splashDamageRadius * 1.5F);
                  hitEffect = WHFx.largeTechBlueHit;
                  shootEffect = WHFx.techBlueShootBig;
                  smokeEffect = WHFx.techBlueSmokeBig;
                  hitSpacing = 3.0F;
            }

            public void update(Bullet b) {
                super.update(b);
                if (b.timer(1, 6.0F)) {
                    for(int j = 0; j < 2; ++j) {
                        Drawn.randFadeLightningEffect(b.x, b.y, (float)Mathf.random(360), (float)Mathf.random(7, 12),   backColor, Mathf.chance(0.5));
                    }
                }

            }

            public void draw(Bullet b) {
                Draw.color(  backColor);
                Drawn.surround((long)b.id, b.x, b.y,   size * 1.45F, 14, 7.0F, 11.0F, (b.fin(WHInterp.parabola4Reversed) + 1.0F) / 2.0F * b.fout(0.1F));
                  drawTrail(b);
                Draw.color(  backColor);
                Fill.circle(b.x, b.y,   size);
                Draw.z(110.0001F);
                Draw.color(  frontColor);
                Fill.circle(b.x, b.y,   size * 0.62F);
                Draw.z(99.89F);
                Draw.color(  frontColor);
                Fill.circle(b.x, b.y,   size * 0.66F);
                Draw.z(100.0F);
                Drawf.light(b.x, b.y,   size * 1.85F,   backColor, 0.7F);
            }
        };
        arc9000hyper = new AccelBulletType(10.0F, 1000.0F) {
            {
                  drawSize = 1200.0F;
                  width =   height =   shrinkX =   shrinkY = 0.0F;
                  collides = false;
                  despawnHit = false;
                  collidesAir =   collidesGround =   collidesTiles = true;
                  splashDamage = 4000.0F;
                  velocityBegin = 6.0F;
                  velocityIncrease = -5.9F;
                  accelerateEnd = 0.75F;
                  accelerateBegin = 0.1F;
                  accelInterp = Interp.pow2;
                  trailInterp = Interp.pow10Out;
                  despawnSound = Sounds.plasmaboom;
                  hitSound = Sounds.explosionbig;
                  hitShake = 60.0F;
                  despawnShake = 100.0F;
                  lightning = 12;
                  lightningDamage = 2000.0F;
                  lightningLength = 50;
                  lightningLengthRand = 80;
                  fragBullets = 1;
                  fragBullet = WHBullets.arc9000;
                  fragVelocityMin = 0.4F;
                  fragVelocityMax = 0.6F;
                  fragLifeMin = 0.5F;
                  fragLifeMax = 0.7F;
                  trailWidth = 12.0F;
                  trailLength = 120;
                  ammoMultiplier = 1.0F;
                  hittable = false;
                  scaleLife = true;
                  splashDamageRadius = 400.0F;
                  hitColor =   lightColor =   lightningColor =   trailColor = Pal.techBlue;
                Effect effect = WHFx.crossBlast(  hitColor, 420.0F, 45.0F);
                effect.lifetime += 180.0F;
                  despawnEffect = WHFx.circleOut(  hitColor,   splashDamageRadius);
                  hitEffect = new MultiEffect(new Effect[]{WHFx.blast(  hitColor, 200.0F), (new Effect(180.0F, 600.0F, (e) -> {
                    float rad = 120.0F;
                    float f = (e.fin(Interp.pow10Out) + 8.0F) / 9.0F * Mathf.curve(Interp.slowFast.apply(e.fout(0.75F)), 0.0F, 0.85F);
                    Draw.alpha(0.9F * e.foutpowdown());
                    Draw.color(Color.white, e.color, e.fin() + 0.6F);
                    Fill.circle(e.x, e.y, rad * f);
                    e.scaled(45.0F, (i) -> {
                        Lines.stroke(7.0F * i.fout());
                        Lines.circle(i.x, i.y, rad * 3.0F * i.finpowdown());
                        Lines.circle(i.x, i.y, rad * 2.0F * i.finpowdown());
                    });
                    Draw.color(Color.white);
                    Fill.circle(e.x, e.y, rad * f * 0.75F);
                    Drawf.light(e.x, e.y, rad * f * 2.0F, Draw.getColor(), 0.7F);
                })).layer(110.001F), effect, new Effect(260.0F, 460.0F, (e) -> {
                    Draw.blend(Blending.additive);
                    Draw.z(114.2F);
                    float radius = e.fin(Interp.pow3Out) * 230.0F;
                    Fill.light(e.x, e.y, Lines.circleVertices(radius), radius, Color.clear, Tmp.c1.set(Pal.techBlue).a(e.fout(Interp.pow10Out)));
                    Draw.blend();
                })});
            }

            public void draw(Bullet b) {
                super.draw(b);
                Draw.color(Pal.techBlue, Color.white, b.fout() * 0.25F);
                float extend = Mathf.curve(b.fin(Interp.pow10Out), 0.075F, 1.0F);
                float chargeCircleFrontRad = 20.0F;
                float width = chargeCircleFrontRad * 1.2F;
                Fill.circle(b.x, b.y, width * (b.fout() + 4.0F) / 3.5F);
                float rotAngle = b.fdata;
                int[] var6 = Mathf.signs;
                int var7 = var6.length;

                int var8;
                int ix;
                for(var8 = 0; var8 < var7; ++var8) {
                    ix = var6[var8];
                    Drawn.tri(b.x, b.y, width * b.foutpowdown(), 200.0F + 570.0F * extend, rotAngle + (float)(90 * ix) - 45.0F);
                }

                var6 = Mathf.signs;
                var7 = var6.length;

                for(var8 = 0; var8 < var7; ++var8) {
                    ix = var6[var8];
                    Drawn.tri(b.x, b.y, width * b.foutpowdown(), 200.0F + 570.0F * extend, rotAngle + (float)(90 * ix) + 45.0F);
                }

                float cameraFin = (1.0F + 2.0F * Drawn.cameraDstScl(b.x, b.y, Vars.mobile ? 200.0F : 320.0F)) / 3.0F;
                float triWidth = b.fout() * chargeCircleFrontRad * cameraFin;
                int[] var14 = Mathf.signs;
                ix = var14.length;

                for(int var10 = 0; var10 < ix; ++var10) {
                    int i = var14[var10];
                    Fill.tri(b.x, b.y + triWidth, b.x, b.y - triWidth, b.x + (float)i * cameraFin * chargeCircleFrontRad * (23.0F + Mathf.absin(10.0F, 0.75F)) * (b.fout() * 1.25F + 1.0F), b.y);
                }

                float rad =   splashDamageRadius * b.fin(Interp.pow5Out) * Interp.circleOut.apply(b.fout(0.15F));
                Lines.stroke(8.0F * b.fin(Interp.pow2Out));
                Lines.circle(b.x, b.y, rad);
                Draw.color(Color.white);
                Fill.circle(b.x, b.y, width * (b.fout() + 4.0F) / 5.5F);
                Drawf.light(b.x, b.y, rad,   hitColor, 0.5F);
            }

            public void init(Bullet b) {
                super.init(b);
                b.fdata = Mathf.randomSeed((long)b.id, 90.0F);
            }

            public void update(Bullet b) {
                super.update(b);
                b.fdata += b.vel.len() / 3.0F;
            }

            public void despawned(Bullet b) {
                super.despawned(b);
                Angles.randLenVectors((long)b.id, 8,   splashDamageRadius / 1.25F, (x, y) -> {
                    float nowX = b.x + x;
                    float nowY = b.y + y;
                    Vec2 vec2 = new Vec2(nowX, nowY);
                    Team team = b.team;
                    float mul = b.damageMultiplier();
                    Time.run(Mathf.random(6.0F, 24.0F) + Mathf.sqrt(x * x + y * y) /   splashDamageRadius * 3.0F, () -> {
                        if (Mathf.chanceDelta(0.4000000059604645)) {
                              hitSound.at(vec2.x, vec2.y,   hitSoundPitch,   hitSoundVolume);
                        }

                          despawnSound.at(vec2);
                        Effect.shake(  hitShake,   hitShake, vec2);

                        int i;
                        for(i = 0; i <   lightning / 2; ++i) {
                            Lightning.create(team,   lightningColor,   lightningDamage, vec2.x, vec2.y, Mathf.random(360.0F),   lightningLength + Mathf.random(  lightningLengthRand));
                        }

                          hitEffect.at(vec2.x, vec2.y, 0.0F,   hitColor);
                          hitSound.at(vec2.x, vec2.y,   hitSoundPitch,   hitSoundVolume);
                        if (  fragBullet != null) {
                            for(i = 0; i <   fragBullets; ++i) {
                                  fragBullet.create((Entityc)team.cores().firstOpt(), team, vec2.x, vec2.y, (float)Mathf.random(360), Mathf.random(  fragVelocityMin,   fragVelocityMax), Mathf.random(  fragLifeMin,   fragLifeMax));
                            }
                        }

                        if (  splashDamageRadius > 0.0F && !b.absorbed) {
                            Damage.damage(team, vec2.x, vec2.y,   splashDamageRadius,   splashDamage * mul,   collidesAir,   collidesGround);
                            if (  status != StatusEffects.none) {
                                Damage.status(team, vec2.x, vec2.y,   splashDamageRadius,   status,   statusDuration,   collidesAir,   collidesGround);
                            }
                        }

                    });
                });
            }
        };
        AGFrag = new LightningLinkerBulletType() {
            {
                  effectLightningChance = 0.15F;
                  damage = 200.0F;
                  backColor =   trailColor =   lightColor =   lightningColor =   hitColor = Color.valueOf("FFC397FF");
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
                  splashDamage =   lightningDamage =   damage / 3.0F;
                  lifetime = 130.0F;
                  despawnEffect = WHFx.lightningHitLarge(  hitColor);
                  hitEffect = WHFx.sharpBlast(  hitColor,   frontColor, 35.0F,   splashDamageRadius * 1.25F);
                  shootEffect = WHFx.hitSpark(  backColor, 45.0F, 12, 60.0F, 3.0F, 8.0F);
                  smokeEffect = WHFx.hugeSmoke;
            }
        };
        tankAG7 = new EffectBulletType(480.0F) {
            {
                  hittable = false;
                  collides = false;
                  collidesTiles =   collidesAir =   collidesGround = true;
                  speed = 0.1F;
                  despawnHit = true;
                  keepVelocity = false;
                  splashDamageRadius = 480.0F;
                  splashDamage = 800.0F;
                  lightningDamage = 200.0F;
                  lightning = 36;
                  lightningLength = 60;
                  lightningLengthRand = 60;
                  hitShake =   despawnShake = 40.0F;
                  drawSize = 800.0F;
                  hitColor =   lightColor =   trailColor =   lightningColor = Color.valueOf("FFC397FF");
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
                  shootEffect = WHFx.lightningHitLarge(  hitColor);
                  hitEffect = WHFx.hitSpark(  hitColor, 240.0F, 220, 900.0F, 8.0F, 27.0F);
                  despawnEffect = WHFx.collapserBulletExplode;
            }

            public void despawned(Bullet b) {
                super.despawned(b);
                Vec2 vec = (new Vec2()).set(b);
                float damageMulti = b.damageMultiplier();
                Team team = b.team;

                for(int i = 0; (float)i <   splashDamageRadius / 28.0F; ++i) {
                    int finalI = i;
                    Time.run((float)i *   despawnEffect.lifetime / (  splashDamageRadius / 16.0F), () -> {
                        Damage.damage(team, vec.x, vec.y, (float)(8 * (finalI + 6)),   splashDamage * damageMulti, true);
                    });
                }

                float rad = 120.0F;
                float spacing = 2.5F;

                for(int k = 0; (float)k < (  despawnEffect.lifetime - WHFx.chainLightningFadeReversed.lifetime) / spacing; ++k) {
                    Time.run((float)k * spacing, () -> {
                        int[] var3 = Mathf.signs;
                        int var4 = var3.length;

                        for(int var5 = 0; var5 < var4; ++var5) {
                            int j = var3[var5];
                            Vec2 v = Tmp.v6.rnd(rad * 2.0F + Mathf.random(rad * 2.0F)).add(vec);
                            (j > 0 ? WHFx.chainLightningFade : WHFx.chainLightningFadeReversed).at(v.x, v.y, 12.0F,   hitColor, vec);
                        }

                    });
                }

            }

            public void update(Bullet b) {
                float rad = 120.0F;
                Effect.shake(8.0F * b.fin(), 6.0F, b);
                if (b.timer(1, 12.0F)) {
                    Seq<Teamc> entites = new Seq();
                    Team var10000 = b.team;
                    float var10001 = b.x;
                    float var10002 = b.y;
                    float var10003 = rad * 2.5F * (1.0F + b.fin()) / 2.0F;
                    Objects.requireNonNull(entites);
                    Units.nearbyEnemies(var10000, var10001, var10002, var10003, entites::add);
                    Units.nearbyBuildings(b.x, b.y, rad * 2.5F * (1.0F + b.fin()) / 2.0F, (ex) -> {
                        if (ex.team != b.team) {
                            entites.add(ex);
                        }

                    });
                    entites.shuffle();
                    entites.truncate(15);
                    Iterator var4 = entites.iterator();

                    while(var4.hasNext()) {
                        Teamc e = (Teamc)var4.next();
                        PositionLightning.create(b, b.team, b, e,   lightningColor, false,   lightningDamage, 5 + Mathf.random(5), 2.5F, 1, (p) -> {
                            WHFx.lightningHitSmall.at(p.getX(), p.getY(), 0.0F,   lightningColor);
                        });
                    }
                }

                if (b.lifetime() - b.time() > WHFx.chainLightningFadeReversed.lifetime) {
                    for(int i = 0; i < 2; ++i) {
                        if (Mathf.chanceDelta(0.2 * (double)Mathf.curve(b.fin(), 0.0F, 0.8F))) {
                            int[] var10 = Mathf.signs;
                            int var11 = var10.length;

                            for(int var6 = 0; var6 < var11; ++var6) {
                                int j = var10[var6];
                                Sounds.spark.at(b.x, b.y, 1.0F, 0.3F);
                                Vec2 v = Tmp.v6.rnd(rad / 2.0F + Mathf.random(rad * 2.0F) * (1.0F + Mathf.curve(b.fin(), 0.0F, 0.9F)) / 1.5F).add(b);
                                (j > 0 ? WHFx.chainLightningFade : WHFx.chainLightningFadeReversed).at(v.x, v.y, 12.0F,   hitColor, b);
                            }
                        }
                    }
                }

                if (b.fin() > 0.05F && Mathf.chanceDelta((double)(b.fin() * 0.3F + 0.02F))) {
                    WHSounds.blaster.at(b.x, b.y, 1.0F, 0.3F);
                    Tmp.v1.rnd(rad / 4.0F * b.fin());
                    WHFx.shuttleLerp.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v1.angle(),   hitColor, Mathf.random(rad, rad * 3.0F) * (Mathf.curve(b.fin(Interp.pow2In), 0.0F, 0.7F) + 2.0F) / 3.0F);
                }

            }

            public void draw(Bullet b) {
                float fin = Mathf.curve(b.fin(), 0.0F, 0.02F);
                float f = fin * Mathf.curve(b.fout(), 0.0F, 0.1F);
                float rad = 120.0F;
                float z = Draw.z();
                float circleF = (b.fout(Interp.pow2In) + 1.0F) / 2.0F;
                Draw.color(  hitColor);
                Lines.stroke(rad / 20.0F * b.fin());
                Lines.circle(b.x, b.y, rad * b.fout(Interp.pow3In));
                Lines.circle(b.x, b.y, b.fin(Interp.circleOut) * rad * 3.0F * Mathf.curve(b.fout(), 0.0F, 0.05F));
                Rand rand = WHFx.rand;
                rand.setSeed((long)b.id);

                for(int i = 0; i < (int)(rad / 3.0F); ++i) {
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
                Draw.color(  hitColor);
                Fill.circle(b.x, b.y, rad * fin * circleF / 2.0F);
                Draw.color(Color.valueOf("000000ff"));
                Fill.circle(b.x, b.y, rad * fin * circleF * 0.75F / 2.0F);
                Draw.z(99.9F);
                Draw.color(Color.valueOf("FFC397FF"));
                Fill.circle(b.x, b.y, rad * fin * circleF * 0.8F / 2.0F);
                Draw.z(z);
            }
        };
        collaspsePf = new LightningLinkerBulletType() {
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
                  lightningColor = Color.valueOf("788AD7FF");
                  buildingDamageMultiplier = 0.2F;
                  status = WHStatusEffects.palsy;
                  statusDuration = 300.0F;
                  lightningDamage = 65.0F;
                  lightning = 1;
                  lightningLength = 10;
                  lightningLengthRand = 10;
                  trailColor = Color.valueOf("788AD7FF");
                  despawnEffect = new MultiEffect(new Effect[]{WHFx.circleOut(130.0F,   splashDamageRadius, 3.0F), WHFx.smoothColorCircle(Color.valueOf("788AD7FF"),   splashDamageRadius, 130.0F)});
                  fragBullet = new DOTBulletType() {
                    {
                          DOTDamage =   damage = 40.0F;
                          DOTRadius = 12.0F;
                          radIncrease = 0.25F;
                          fx = WHFx.squSpark1;
                          lightningColor = Color.valueOf("788AD7FF");
                    }
                };
                  fragBullets = 1;
            }
        };
        collapseSp = new LightningLinkerBulletType() {
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
                  linkRange = 200.0F;
                  lightningColor = Color.valueOf("DBD58C");
                  buildingDamageMultiplier = 0.2F;
                  status = WHStatusEffects.palsy;
                  statusDuration = 480.0F;
                  lightningDamage = 65.0F;
                  lightning = 1;
                  lightningLength = 10;
                  lightningLengthRand = 10;
                  ammoMultiplier = 2.0F;
                  fragBullet = new DOTBulletType() {
                    {
                          DOTDamage =   damage = 70.0F;
                          DOTRadius = 12.0F;
                          radIncrease = 0.25F;
                          fx = WHFx.triSpark2;
                          lightningColor = Color.valueOf("DBD58C");
                    }
                };
                  fragBullets = 1;
                  despawnEffect = new MultiEffect(new Effect[]{WHFx.circleOut(130.0F,   splashDamageRadius, 3.0F), WHFx.smoothColorCircle(Color.valueOf("DBD58C"),   splashDamageRadius, 130.0F), WHFx.ScrossBlastArrow45, WHFx.subEffect(130.0F, 85.0F, 12, 30.0F, Interp.pow2Out, (i, x, y, rot, fin) -> {
                    float fout = Interp.pow2Out.apply(1.0F - fin);
                    float finpow = Interp.pow3Out.apply(fin);
                    Tmp.v1.trns(rot, 25.0F * finpow);
                    Draw.color(Color.valueOf("DBD58C"));
                    int[] var8 = Mathf.signs;
                    int var9 = var8.length;

                    for(int var10 = 0; var10 < var9; ++var10) {
                        int s = var8[var10];
                        Drawf.tri(x, y, 14.0F * fout, 30.0F * Mathf.curve(finpow, 0.0F, 0.3F) * WHFx.fout(fin, 0.15F), rot + (float)(s * 90));
                    }

                })});
            }
        };
        SK = new StrafeLaser(300.0F) {
            {
                strafeAngle = 0;
            }

            public void draw(Bullet b) {
                Tmp.c1.set(b.team.color).lerp(Color.white, Mathf.absin(4.0F, 0.1F));
                super.draw(b);
                Draw.z(110.0F);
                float fout = b.fout(0.25F) * Mathf.curve(b.fin(), 0.0F, 0.125F);
                Draw.color(Tmp.c1);
                Fill.circle(b.x, b.y, this.width / 1.225F * fout);
                for (int i : Mathf.signs) {
                    WHUtils.tri(b.x, b.y, 6.0F * fout, 10.0F + 50.0F * fout, Time.time * 1.5F + (float) (90 * i));
                    WHUtils.tri(b.x, b.y, 6.0F * fout, 20.0F + 60.0F * fout, Time.time * -1.0F + (float) (90 * i));
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
                if (realLength < maxRange) {
                    Tmp.v3.set(Tmp.v2).scl((maxRange - realLength) / maxRange);
                }

                Draw.color(Tmp.c1);
                Tmp.v2.scl(0.9F);
                Tmp.v3.scl(0.9F);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
                if (realLength < maxRange) {
                    Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v3.len());
                }

                Tmp.v2.scl(1.2F);
                Tmp.v3.scl(1.2F);
                Draw.alpha(0.5F);
                Fill.quad(b.x - Tmp.v2.x, b.y - Tmp.v2.y, b.x + Tmp.v2.x, b.y + Tmp.v2.y, b.x + Tmp.v1.x + Tmp.v3.x, b.y + Tmp.v1.y + Tmp.v3.y, b.x + Tmp.v1.x - Tmp.v3.x, b.y + Tmp.v1.y - Tmp.v3.y);
                if (realLength < maxRange) {
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
    }
    }
