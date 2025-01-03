//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.MoveEffectAbility;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.effect.ParticleEffect;
import mindustry.entities.effect.WaveEffect;
import mindustry.entities.part.RegionPart;
import mindustry.entities.part.ShapePart;
import mindustry.entities.pattern.*;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.weapons.PointDefenseWeapon;
import wh.entities.abilities.AdaptedHealAbility;
import wh.entities.abilities.ShockWaveAbility;
import wh.entities.bullet.*;
import wh.gen.NucleoidUnit;
import wh.gen.PesterUnit;
import wh.gen.WHSounds;
import wh.graphics.PositionLightning;
import wh.graphics.WHPal;
import wh.type.unit.NucleoidUnitType;
import wh.type.unit.PesterUnitType;
import wh.util.WHUtils;
import wh.util.WHUtils.EffectWrapper;

import static mindustry.content.Fx.*;
import static mindustry.content.Fx.none;
import static mindustry.gen.Sounds.*;
import static mindustry.gen.Sounds.shootBig;
import static wh.core.WarHammerMod.name;

public final class WHUnitTypes {
    public static UnitType cMoon, tankAG, M4, StarrySky;

    private WHUnitTypes() {
    }

    public static void load() {


        cMoon = new NucleoidUnitType("c-moon") {
            {
                constructor = NucleoidUnit::create;
                addEngine(-58.0F, -175.0F, 0.0F, 5.0F, true);
                addEngine(-53.0F, -175.0F, 0.0F, 5.0F, true);
                addEngine(-8.0F, -151.0F, 0.0F, 5.0F, true);
                addEngine(-4.0F, -151.0F, 0.0F, 5.0F, true);
                addEngine(-1.0F, -151.0F, 0.0F, 5.0F, true);
                abilities.add(new Ability() {
                    {
                        display = false;
                    }

                    public void death(Unit unit) {
                        Effect.shake(unit.hitSize / 10.0F, unit.hitSize / 8.0F, unit.x, unit.y);
                        WHFx.circleOut.at(unit.x, unit.y, unit.hitSize, unit.team.color);
                        WHFx.jumpTrailOut.at(unit.x, unit.y, unit.rotation, unit.team.color, unit.type);
                        WHSounds.jump.at(unit.x, unit.y, 1.0F, 3.0F);
                    }
                });
                abilities.add((new AdaptedHealAbility(3000.0F, 180.0F, 220.0F, healColor)).modify((a) -> a.selfHealReloadTime = 300.0F));
                abilities.add((new ShockWaveAbility(180.0F, 320.0F, 2000.0F, WHPal.pop)).status(StatusEffects.unmoving, 300.0F, StatusEffects.disarmed, 300.0F).modify((a) -> {
                    a.knockback = 400.0F;
                    a.shootEffect = new MultiEffect(WHFx.circleOut, WHFx.hitSpark(a.hitColor, 55.0F, 40, a.range + 30.0F, 3.0F, 8.0F), WHFx.crossBlastArrow45, WHFx.smoothColorCircle(WHPal.pop.cpy().a(0.3F), a.range, 60.0F));
                }));
                weapons.add(new Weapon("wh-c-moon-weapon3") {
                    {
                        x = 49.0F;
                        y = -61.5F;
                        mirror = true;
                        layerOffset = 0.15F;
                        rotate = true;
                        rotateSpeed = 1.2F;
                        reload = 300.0F;
                        shootY = 46.0F;
                        inaccuracy = 1.5F;
                        velocityRnd = 0.075F;
                        shootSound = Sounds.mediumCannon;
                        shoot = new ShootAlternate() {
                            {
                                shots = 2;
                                spread = 25.0F;
                                shotDelay = 15.0F;
                            }
                        };
                        bullet = new TrailFadeBulletType(20.0F, 1800.0F) {
                            {
                                lifetime = 55.0F;
                                trailLength = 90;
                                trailWidth = 3.6F;
                                tracers = 2;
                                tracerFadeOffset = 20;
                                keepVelocity = false;
                                tracerSpacing = 10.0F;
                                tracerUpdateSpacing *= 1.25F;
                                removeAfterPierce = false;
                                hitColor = backColor = lightColor = lightningColor = WHPal.pop;
                                trailColor = WHPal.pop;
                                frontColor = WHPal.pop2;
                                width = 18.0F;
                                height = 60.0F;
                                hitSound = Sounds.plasmaboom;
                                despawnShake = hitShake = 18.0F;
                                pierce = pierceArmor = true;
                                pierceCap = 3;
                                pierceBuilding = false;
                                lightning = 3;
                                lightningLength = 8;
                                lightningLengthRand = 8;
                                lightningDamage = 300.0F;
                                smokeEffect = EffectWrapper.wrap(WHFx.hitSparkHuge, hitColor);
                                shootEffect = WHFx.instShoot(backColor, frontColor);
                                despawnEffect = WHFx.lightningHitLarge;
                            }

                            public void createFrags(Bullet b, float x, float y) {
                                super.createFrags(b, x, y);
                                WHBullets.nuBlackHole.create(b, x, y, 0.0F);
                            }
                        };
                        parts.add(new RegionPart("-管l") {
                            {
                                outline = true;
                                mirror = false;
                                moveY = -8.0F;
                                under = true;
                                recoilIndex = 0;
                                heatProgress = PartProgress.recoil;
                                progress = PartProgress.recoil;
                            }
                        });
                        parts.add(new RegionPart("-管r") {
                            {
                                outline = true;
                                mirror = false;
                                moveY = -8.0F;
                                under = true;
                                recoilIndex = 1;
                                heatProgress = PartProgress.recoil;
                                progress = PartProgress.recoil;
                            }
                        });
                    }

                    protected Teamc findTarget(Unit unit, float x, float y, float range, boolean air, boolean ground) {
                        return Units.bestTarget(unit.team, x, y, range, (u) -> u.checkTarget(air, ground), (t) -> {
                            return ground;
                        }, UnitSorts.strongest);
                    }
                });
                weapons.add(new Weapon("wh-c-moon-weapon1") {
                    {
                        x = 39.0F;
                        y = 34.0F;
                        shootY = 16.0F;
                        reload = 15.0F;
                        rotate = true;
                        alternate = false;
                        rotateSpeed = 4.0F;
                        shake = 1.0F;
                        shootSound = Sounds.railgun;
                        soundPitchMin = 1.5F;
                        soundPitchMax = 1.5F;
                        heatColor = Color.valueOf("FF9A79FF");
                        shoot = new ShootAlternate() {
                            {
                                barrels = 2;
                                spread = 7.5F;
                            }
                        };
                        bullet = new ChainLightingBulletType(300) {
                            {
                                length = 600.0F;
                                hitColor = lightColor = lightningColor = WHPal.pop;
                                shootEffect = WHFx.hitSparkLarge;
                                hitEffect = WHFx.lightningHitSmall;
                                smokeEffect = WHFx.hugeSmokeGray;
                            }
                        };
                    }
                });
                weapons.add(new PointDefenseWeapon("wh-c-moon-weapon1") {
                    {
                        color = WHPal.pop;
                        display = false;
                        layerOffset = 0.01F;
                        rotate = true;
                        rotateSpeed = 3.0F;
                        mirror = true;
                        alternate = false;
                        x = -54.5F;
                        y = -118.5F;
                        reload = 6.0F;
                        targetInterval = 6.0F;
                        targetSwitchInterval = 6.0F;
                        beamEffect = Fx.chainLightning;
                        bullet = new BulletType() {
                            {
                                shootEffect = WHFx.shootLineSmall(color);
                                hitEffect = WHFx.lightningHitSmall;
                                hitColor = color;
                                maxRange = 600.0F;
                                damage = 150.0F;
                            }
                        };
                    }
                });
                weapons.add(new Weapon("wh-c-moon-weapon1") {
                    {
                        reload = 20.0F;
                        rotate = true;
                        rotateSpeed = 3.0F;
                        alternate = false;
                        x = -97.5F;
                        y = -73.25F;
                        shootY = 20.0F;
                        shoot = new ShootAlternate() {
                            {
                                barrels = 2;
                                spread = 7.5F;
                            }
                        };
                        bullet = new PositionLightningBulletType(200.0F) {
                            {
                                maxRange = 600.0F;
                                rangeOverride = 600.0F;
                                hitColor = lightColor = lightningColor = WHPal.pop;
                                shootEffect = WHFx.hitSparkLarge;
                                hitEffect = WHFx.lightningHitSmall;
                                smokeEffect = WHFx.hugeSmokeGray;
                                spawnBullets.add(new LaserBulletType(800.0F) {
                                    {
                                        status = WHStatusEffects.plasma;
                                        statusDuration = 80.0F;
                                        shootEffect = Fx.bigShockwave;
                                        sideAngle = 25.0F;
                                        sideWidth = 1.35F;
                                        sideLength = 90.0F;
                                        colors = new Color[]{Color.valueOf("FBE5BFFF"), Color.valueOf("FFFBBDE1"), Color.valueOf("FFFBBDE1")};
                                        width = 11.0F;
                                        length = 600.0F;
                                    }
                                });
                            }
                        };
                    }
                });
                weapons.add(new Weapon("wh-c-moon-weapon2") {
                    {
                        layerOffset = 0.01F;
                        reload = 100.0F;
                        recoil = 1.5F;
                        x = -63.75F;
                        y = 17.75F;
                        shootY = 16.75F;
                        rotate = true;
                        mirror = true;
                        rotateSpeed = 3.0F;
                        inaccuracy = 1.8F;
                        shootCone = 31.8F;
                        shootSound = Sounds.bolt;
                        alternate = false;
                        shake = 0.3F;
                        ejectEffect = Fx.casing3Double;
                        parts.add(new RegionPart("-管") {
                            {
                                progress = PartProgress.recoil;
                                mirror = false;
                                under = true;
                                x = 0.0F;
                                y = 0.0F;
                                moveY = -8.0F;
                            }
                        });
                        shoot = new ShootAlternate() {
                            {
                                barrels = 2;
                                spread = 10.5F;
                                shots = 6;
                                shotDelay = 15.0F;
                            }
                        };
                        bullet = new TrailFadeBulletType() {
                            {
                                damage = 800.0F;
                                tracerStroke -= 0.3f;
                                tracers = 1;
                                keepVelocity = true;

                                tracerSpacing = 10f;
                                tracerUpdateSpacing *= 1.25f;
                                pierce = true;
                                pierceCap = 3;
                                speed = 20.0F;
                                status = WHStatusEffects.palsy;
                                statusDuration = 400.0F;
                                buildingDamageMultiplier = 0.3F;
                                lightningDamage = 200.0F;
                                lightning = 2;
                                lightningLength = 9;
                                lightningLengthRand = 9;
                                lightColor = lightningColor = WHPal.pop;
                                lifetime = 47.0F;
                                width = 15.0F;
                                height = 30.0F;
                                frontColor = WHPal.pop;
                                backColor = WHPal.pop;
                                trailLength = 13;
                                trailWidth = 3.0F;
                                trailColor = Color.valueOf("FEEBB3");
                                shootEffect = shootBig2;
                                smokeEffect = new ParticleEffect() {
                                    {
                                        particles = 8;
                                        line = true;
                                        interp = Interp.fastSlow;
                                        sizeInterp = Interp.pow3In;
                                        lenFrom = 6.0F;
                                        lenTo = 0.0F;
                                        strokeFrom = 2.0F;
                                        strokeTo = 0.0F;
                                        length = 35.0F;
                                        baseLength = 0.0F;
                                        lifetime = 23.0F;
                                        colorFrom = colorTo = WHPal.pop;
                                        cone = 15.0F;
                                    }
                                };
                                hitEffect = new MultiEffect(new ParticleEffect() {
                                    {
                                        particles = 4;
                                        region = "wh-菱形";
                                        sizeFrom = 12.0F;
                                        sizeTo = 0.0F;
                                        length = 30.0F;
                                        baseLength = 0.0F;
                                        lifetime = 45.0F;
                                        colorFrom = colorTo = WHPal.pop;
                                    }
                                });
                                despawnEffect = new MultiEffect() {
                                    {
                                        new ParticleEffect() {
                                            {
                                                particles = 4;
                                                region = "wh-菱形";
                                                sizeFrom = 10.0F;
                                                sizeTo = 0.0F;
                                                length = 50.0F;
                                                baseLength = 0.0F;
                                                lifetime = 60.0F;
                                                colorFrom = colorTo = WHPal.pop;
                                            }
                                        };
                                        new ParticleEffect() {
                                            {
                                                particles = 12;
                                                strokeFrom = 2.0F;
                                                strokeTo = 0.0F;
                                                line = true;
                                                lenFrom = 20.0F;
                                                lenTo = 0.0F;
                                                length = 80.0F;
                                                baseLength = 10.0F;
                                                lifetime = 60.0F;
                                                colorFrom = colorTo = WHPal.pop;
                                            }
                                        };
                                        new WaveEffect() {
                                            {
                                                lifetime = 70.0F;
                                                sizeFrom = 0.0F;
                                                sizeTo = 70.0F;
                                                strokeFrom = 3.0F;
                                                strokeTo = 0.0F;
                                                colorFrom = colorTo = WHPal.pop;
                                            }
                                        };
                                        new WaveEffect() {
                                            {
                                                lifetime = 70.0F;
                                                sizeFrom = 40.0F;
                                                sizeTo = 70.0F;
                                                strokeFrom = 2.5F;
                                                strokeTo = 0.0F;
                                                colorFrom = colorTo = WHPal.pop;
                                            }
                                        };
                                    }
                                };
                            }
                        };
                    }
                });
                weapons.add(new Weapon("wh-c-moon-weapon2") {
                    {
                        layerOffset = 0.01F;
                        reload = 100.0F;
                        recoil = 1.5F;
                        x = -43.5F;
                        y = 91.0F;
                        shootY = 16.75F;
                        rotate = true;
                        mirror = true;
                        rotateSpeed = 3.0F;
                        inaccuracy = 1.8F;
                        shootCone = 31.8F;
                        shootSound = Sounds.bolt;
                        alternate = false;
                        shake = 0.3F;
                        ejectEffect = Fx.casing3Double;
                        parts.add(new RegionPart("-管") {
                            {
                                progress = PartProgress.recoil;
                                mirror = false;
                                under = true;
                                x = 0.0F;
                                y = 0.0F;
                                moveY = -8.0F;
                            }
                        });
                        shoot = new ShootAlternate() {
                            {
                                barrels = 2;
                                spread = 10.5F;
                                shots = 6;
                                shotDelay = 15.0F;
                            }
                        };
                        range = 280.0F;
                        reload = 150.0F;
                        shootY = 0.25F;
                        rotateSpeed = 3.5F;
                        recoil = 4.0F;
                        bullet = new TrailFadeBulletType() {
                            {
                                damage = 800.0F;
                                keepVelocity = false;
                                tracerSpacing = 10.0F;
                                tracerUpdateSpacing *= 1.25F;
                                removeAfterPierce = false;
                                pierce = true;
                                pierceCap = 3;
                                speed = 20.0F;
                                status = WHStatusEffects.palsy;
                                statusDuration = 400.0F;
                                buildingDamageMultiplier = 0.3F;
                                lightningDamage = 200.0F;
                                lightning = 2;
                                lightningLength = 9;
                                lightningLengthRand = 9;
                                lightningColor = WHPal.pop;
                                lifetime = 47.0F;
                                width = 15.0F;
                                height = 30.0F;
                                frontColor = WHPal.pop;
                                backColor = WHPal.pop;
                                trailLength = 13;
                                trailWidth = 3.0F;
                                trailColor = Color.valueOf("FEEBB3");
                                shootEffect = shootBig2;
                                smokeEffect = new ParticleEffect() {
                                    {
                                        particles = 8;
                                        line = true;
                                        interp = Interp.fastSlow;
                                        sizeInterp = Interp.pow3In;
                                        lenFrom = 6.0F;
                                        lenTo = 0.0F;
                                        strokeFrom = 2.0F;
                                        strokeTo = 0.0F;
                                        length = 35.0F;
                                        baseLength = 0.0F;
                                        lifetime = 23.0F;
                                        colorFrom = colorTo = WHPal.pop;
                                        cone = 15.0F;
                                    }
                                };
                                hitEffect = new MultiEffect(new ParticleEffect() {
                                    {
                                        particles = 4;
                                        region = "wh-菱形";
                                        sizeFrom = 12.0F;
                                        sizeTo = 0.0F;
                                        length = 30.0F;
                                        baseLength = 0.0F;
                                        lifetime = 45.0F;
                                        colorFrom = colorTo = WHPal.pop;
                                    }
                                });
                                despawnEffect = new MultiEffect() {
                                    {
                                        new ParticleEffect() {
                                            {
                                                particles = 4;
                                                region = "wh-菱形";
                                                sizeFrom = 10.0F;
                                                sizeTo = 0.0F;
                                                length = 50.0F;
                                                baseLength = 0.0F;
                                                lifetime = 60.0F;
                                                colorFrom = colorTo = WHPal.pop;
                                            }
                                        };
                                        new ParticleEffect() {
                                            {
                                                particles = 12;
                                                strokeFrom = 2.0F;
                                                strokeTo = 0.0F;
                                                line = true;
                                                lenFrom = 20.0F;
                                                lenTo = 0.0F;
                                                length = 80.0F;
                                                baseLength = 10.0F;
                                                lifetime = 60.0F;
                                                colorFrom = colorTo = WHPal.pop;
                                            }
                                        };
                                        new WaveEffect() {
                                            {
                                                lifetime = 70.0F;
                                                sizeFrom = 0.0F;
                                                sizeTo = 70.0F;
                                                strokeFrom = 3.0F;
                                                strokeTo = 0.0F;
                                                colorFrom = colorTo = WHPal.pop;
                                            }
                                        };
                                        new WaveEffect() {
                                            {
                                                lifetime = 70.0F;
                                                sizeFrom = 40.0F;
                                                sizeTo = 70.0F;
                                                strokeFrom = 2.5F;
                                                strokeTo = 0.0F;
                                                colorFrom = colorTo = WHPal.pop;
                                            }
                                        };
                                    }
                                };
                            }
                        };
                    }
                });
                weapons.add(new Weapon("wh-c-moon-weapon4") {
                    {
                        reload = 600.0F;
                        recoil = 1.5F;
                        x = 0.0F;
                        y = 0.0F;
                        range = 800.0F;
                        shootY = 16.75F;
                        rotate = false;
                        mirror = false;
                        rotateSpeed = 3.0F;
                        inaccuracy = 1.8F;
                        alternate = false;
                        shake = 0.3F;
                        ejectEffect = none;
                        shootCone = 30.0F;
                        shoot.firstShotDelay = 180.0F;
                        shootSound = WHSounds.hugeShoot;
                        shootStatus = StatusEffects.unmoving;
                        shootStatusDuration = 180.0F;
                        bullet = new BasicBulletType() {
                            {
                                parts.add(new ShapePart() {
                                    {
                                        color = Color.valueOf("000000ff");
                                        circle = true;
                                        radius = 23.0F;
                                        radiusTo = 30.0F;
                                        layer = 114.0F;
                                    }
                                }, new ShapePart() {
                                    {
                                        color = Color.valueOf("FFFBBDE1");
                                        circle = true;
                                        radius = 30.0F;
                                        radiusTo = 40.0F;
                                        layer = 110.0F;
                                    }
                                });
                                spin = -11.0F;
                                homingPower = 0.03F;
                                homingRange = 50.0F;
                                damage = 400.0F;
                                speed = 5.0F;
                                lifetime = 200.0F;
                                trailLength = 45;
                                trailWidth = 9.0F;
                                hittable = false;
                                absorbable = false;
                                trailColor = WHPal.pop;
                                trailInterval = 1.0F;
                                trailRotation = true;
                                trailEffect = new ParticleEffect() {
                                    {
                                        interp = Interp.pow10Out;
                                        line = true;
                                        lenFrom = 60.0F;
                                        lenTo = 0.0F;
                                        strokeFrom = 5.0F;
                                        strokeTo = 0.0F;
                                        length = -100.0F;
                                        baseLength = 0.0F;
                                        lifetime = 60.0F;
                                        colorFrom = Color.valueOf("FFFBBDE1");
                                        colorTo = Color.valueOf("FFFBBDE1");
                                        cone = 18.0F;
                                    }
                                };
                                hitEffect = despawnEffect = none;
                                shootEffect = none;
                                chargeEffect = new MultiEffect(new WaveEffect() {
                                    {
                                        lifetime = 99.0F;
                                        sizeFrom = 190.0F;
                                        sizeTo = 0.0F;
                                        strokeFrom = 0.0F;
                                        strokeTo = 10.0F;
                                        colorFrom = colorTo = WHPal.pop;
                                    }
                                }, new WaveEffect() {
                                    {
                                        startDelay = 49.0F;
                                        interp = Interp.circleIn;
                                        lifetime = 50.0F;
                                        sizeFrom = 190.0F;
                                        sizeTo = 0.0F;
                                        strokeFrom = 0.0F;
                                        strokeTo = 10.0F;
                                        colorFrom = colorTo = WHPal.pop;
                                    }
                                }, new Effect(180.0F, (e) -> {
                                    Draw.color(Tmp.c1);
                                    Fill.circle(e.x, e.y, 23.0F * e.finpow());
                                    float z = Draw.z();
                                    Draw.z(212.0F);
                                    Draw.color(Color.black);
                                    Fill.circle(e.x, e.y, 20.0F * e.finpow());
                                    Draw.z(z);
                                    Angles.randLenVectors(e.id, 16, 60.0F * e.foutpow(), Mathf.randomSeed(e.id, 360.0F), 360.0F, (x, y) -> {
                                        Draw.color(Tmp.c1);
                                        Fill.circle(e.x + x, e.y + y, 12.0F * e.foutpow());
                                        float zs = Draw.z();
                                        Draw.z(210.0F);
                                        Draw.color(Color.black);
                                        Fill.circle(e.x + x, e.y + y, 12.0F * e.foutpow());
                                        Draw.z(zs);
                                    });
                                }));
                                chargeSound = Sounds.lasercharge;
                                fragBullets = 1;
                                fragRandomSpread = 0.0F;
                                fragBullet = new BlackHoleBulletType() {
                                    {
                                        keepVelocity = false;
                                        inRad = 32.0F;
                                        outRad = drawSize = 88.0F;
                                        accColor = WHPal.pop;
                                        speed = 0.0F;
                                        lifetime = 150.0F;
                                        splashDamageRadius = 180.0F;
                                        splashDamage = 1500.0F;
                                        despawnEffect = hitEffect = new MultiEffect(WHFx.smoothColorCircle(WHPal.pop, splashDamageRadius * 1.25F, 95.0F), WHFx.circleOut(WHPal.pop, splashDamageRadius * 1.25F), WHFx.hitSparkLarge);
                                        fragBullets = 15;
                                        fragVelocityMax = 1.3F;
                                        fragVelocityMin = 0.5F;
                                        fragLifeMax = 1.3F;
                                        fragLifeMin = 0.4F;
                                        fragBullet = new LightningLinkerBulletType(3.5F, 800.0F) {
                                            {
                                                size = 12.0F;
                                                randomLightningNum = 2;
                                                lightning = 2;
                                                lightningLength = 10;
                                                lightningLengthRand = 3;
                                                effectLightningChance = 0.25F;
                                                lightningCone = 360.0F;
                                                lightningDamage = 200.0F;
                                                linkRange = 150.0F;
                                                lightningColor = WHPal.pop;
                                                drag = 0.04F;
                                                knockback = 0.8F;
                                                lifetime = 90.0F;
                                                splashDamageRadius = 64.0F;
                                                splashDamage = 800.0F;
                                                backColor = trailColor = hitColor = WHPal.pop;
                                                frontColor = WHPal.pop;
                                                despawnShake = 7.0F;
                                                lightRadius = 30.0F;
                                                lightColor = WHPal.pop;
                                                lightOpacity = 0.5F;
                                                trailLength = 20;
                                                trailWidth = 3.5F;
                                            }
                                        };
                                    }
                                };
                            }
                        };
                    }
                });
            }
        };
        tankAG = new UnitType("tankAG") {
            {
                constructor = UnitEntity::create;
                weapons.add(new Weapon("tankAG-weapon7") {
                    final float rangeWeapon = 650.0F;

                    {
                        reload = 1100.0F;
                        x = 0.0F;
                        shake = 0.0F;
                        rotate = true;
                        rotateSpeed = 10.0F;
                        mirror = false;
                        inaccuracy = 0.0F;
                        shootSound = Sounds.plasmadrop;
                        recoil = 0.0F;
                        bullet = new ArtilleryBulletType(10.0F, 150.0F) {
                            {
                                sprite = "wh-jump-arrow";
                                height = 65.0F;
                                width = 55.0F;
                                lifetime = 65.0F;
                                shrinkY = 0.0F;
                                frontColor = Color.valueOf("FFC397FF");
                                backColor = Color.valueOf("FFC397FF");
                                absorbable = false;
                                hittable = false;
                                despawnEffect = smokeEffect = none;
                                hitEffect = new MultiEffect(new ParticleEffect() {
                                    {
                                        particles = 1;
                                        region = "wh-jump";
                                        sizeFrom = 15.0F;
                                        sizeTo = 0.0F;
                                        length = 0.0F;
                                        baseLength = 0.0F;
                                        lifetime = 180.0F;
                                        colorFrom = Color.valueOf("FFC397FF");
                                        colorTo = Color.valueOf("FFC397FF00");
                                    }
                                }, new WaveEffect() {
                                    {
                                        lifetime = 60.0F;
                                        sizeFrom = 100.0F;
                                        sizeTo = 0.0F;
                                        strokeFrom = 4.0F;
                                        strokeTo = 1.0F;
                                        colorFrom = Color.valueOf("FFC397FF");
                                        colorTo = Color.valueOf("FFC397FF");
                                    }
                                });
                                trailColor = Color.valueOf("FFC397FF");
                                trailRotation = true;
                                trailWidth = 8.0F;
                                trailLength = 12;
                                targetInterval = 0.3F;
                                trailChance = 0.7F;
                                trailEffect = new MultiEffect(new Effect(60.0F, (e) -> {
                                    Draw.color(e.color);
                                    Fx.rand.setSeed(e.id);
                                    float fin = 1.0F - Mathf.curve(e.fout(), 0.0F, 0.85F);
                                    Tmp.v1.set((float) (Fx.rand.chance(0.5) ? 5 : -5) * (Fx.rand.chance(0.20000000298023224) ? 0.0F : fin), 0.0F).rotate(e.rotation - 90.0F);
                                    float exx = e.x + Tmp.v1.x;
                                    float ey = e.y + Tmp.v1.y;
                                    Draw.rect("wh-jump-arrow", exx, ey, 64.0F * e.fout(), 64.0F * e.fout(), e.rotation - 90.0F);
                                }));
                                shootEffect = Fx.bigShockwave;
                                fragRandomSpread = 1.0F;
                                fragVelocityMax = 1.0F;
                                fragVelocityMin = 1.0F;
                                fragLifeMin = 1.0F;
                                fragLifeMax = 1.0F;
                                fragAngle = 180.0F;
                                fragBullets = 1;
                                fragBullet = new PointBulletType() {
                                    {
                                        damage = 20.0F;
                                        lifetime = 125.0F;
                                        speed = 10.0F;
                                        trailEffect = none;
                                        hitEffect = none;
                                        despawnEffect = new MultiEffect(new ParticleEffect() {
                                            {
                                                particles = 1;
                                                sizeFrom = 0.0F;
                                                sizeTo = 30.0F;
                                                length = 0.0F;
                                                baseLength = 0.0F;
                                                lifetime = 20.0F;
                                                colorFrom = Color.valueOf("FFC397FF");
                                                colorTo = Color.valueOf("FFC397FF6E");
                                            }
                                        }, new WaveEffect() {
                                            {
                                                lifetime = 20.0F;
                                                sizeFrom = 60.0F;
                                                sizeTo = 0.0F;
                                                strokeFrom = 4.0F;
                                                strokeTo = 0.0F;
                                                colorFrom = Color.valueOf("FFC397FF");
                                                colorTo = Color.valueOf("FFC397FF");
                                            }
                                        });
                                        fragBullets = 1;
                                        fragAngle = 180.0F;
                                        fragRandomSpread = 1.0F;
                                        fragVelocityMax = 1.0F;
                                        fragVelocityMin = 1.0F;
                                        fragLifeMin = 1.0F;
                                        fragLifeMax = 1.0F;
                                        fragBullet = new BasicBulletType(15.0F, 1000.0F, "wh-大导弹") {
                                            {
                                                lifetime = 83.3F;
                                                splashDamageRadius = 150.0F;
                                                splashDamage = 200.0F;
                                                scaledSplashDamage = true;
                                                buildingDamageMultiplier = 1.25F;
                                                shrinkX = 0.5F;
                                                shrinkY = 0.5F;
                                                width = 55.0F;
                                                height = 200.0F;
                                                hitSize = 20.0F;
                                                trailLength = 40;
                                                trailWidth = 5.0F;
                                                trailColor = Color.valueOf("FFC397FF");
                                                trailChance = 0.8F;
                                                trailEffect = new ParticleEffect() {
                                                    {
                                                        particles = 4;
                                                        region = "wh-菱形";
                                                        sizeFrom = 7.0F;
                                                        sizeTo = 16.0F;
                                                        length = 42.0F;
                                                        baseLength = 0.0F;
                                                        lifetime = 33.0F;
                                                        colorFrom = Color.valueOf("FFC397FF");
                                                        colorTo = Color.valueOf("FFC397FF");
                                                    }
                                                };
                                                hitShake = 20.0F;
                                                collides = false;
                                                pierce = true;
                                                absorbable = false;
                                                hittable = false;
                                                pierceBuilding = true;
                                                backColor = Color.valueOf("FF5B5B");
                                                frontColor = Color.valueOf("EEC591");
                                                heatColor = Color.valueOf("EEC591");
                                                hitSound = Sounds.plasmaboom;
                                                hitEffect = new MultiEffect(new ParticleEffect() {
                                                    {
                                                        particles = 60;
                                                        line = true;
                                                        strokeFrom = 4.0F;
                                                        strokeTo = 0.0F;
                                                        lenFrom = 85.0F;
                                                        lenTo = 23.0F;
                                                        length = 203.0F;
                                                        baseLength = 20.0F;
                                                        lifetime = 30.0F;
                                                        colorFrom = Color.valueOf("FFC397FF");
                                                        colorTo = Color.valueOf("FFC397FF");
                                                    }
                                                }, new WaveEffect() {
                                                    {
                                                        lifetime = 15.0F;
                                                        sizeFrom = 0.0F;
                                                        sizeTo = 230.0F;
                                                        strokeFrom = 19.0F;
                                                        strokeTo = 0.0F;
                                                        colorFrom = Color.valueOf("FFC397FF");
                                                        colorTo = Color.valueOf("FFC397FF");
                                                    }
                                                });
                                                despawnEffect = none;
                                                fragBullets = 1;
                                                fragLifeMin = 1.0F;
                                                fragLifeMax = 1.0F;
                                                fragVelocityMin = 0.0F;
                                                fragVelocityMax = 0.0F;
                                                fragBullet = WHBullets.tankAG7;
                                            }
                                        };
                                    }
                                };
                            }
                        };
                    }

                    public void draw(Unit unit, WeaponMount mount) {
                        float z = Draw.z();
                        Tmp.v1.trns(unit.rotation, y);
                        float f = 1.0F - mount.reload / reload;
                        float rad = 12.0F;
                        float f1 = Mathf.curve(f, 0.4F, 1.0F);
                        Draw.z(100.0F);
                        Draw.color(heatColor);
                        TextureRegion arrowRegion = WHContent.arrowRegion;
                        Tmp.v6.set(mount.aimX, mount.aimY).sub(unit);
                        Tmp.v2.set(mount.aimX, mount.aimY).sub(unit).nor().scl(Math.min(Tmp.v6.len(), rangeWeapon)).add(unit);

                        for (int l = 0; l < 4; ++l) {
                            float angle = (float) (45 + 90 * l);

                            for (int i = 0; i < 3; ++i) {
                                Tmp.v3.trns(angle, (float) ((i - 4) * 8 + 8)).add(Tmp.v2);
                                float fS = (100.0F - (Time.time + (float) (25 * i)) % 100.0F) / 100.0F * f1 / 4.0F;
                                Draw.rect(arrowRegion, Tmp.v3.x, Tmp.v3.y, (float) arrowRegion.width * fS, (float) arrowRegion.height * fS, angle + 90.0F);
                            }
                        }

                        Lines.stroke((1.0F + Mathf.absin(Time.time + 4.0F, 8.0F, 1.5F)) * f1, heatColor);
                        Lines.square(Tmp.v2.x, Tmp.v2.y, 4.0F + Mathf.absin(8.0F, 4.0F), 45.0F);
                        Lines.stroke(rad / 2.5F * mount.heat, heatColor);
                        Lines.circle(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, rad * 2.0F * (1.0F - mount.heat));
                        Draw.color(heatColor);
                        Draw.z(z);
                    }
                });
                weapons.add(new Weapon("wh-tankAG-weapon1") {
                    {
                        x = -40.0F;
                        y = 33.5F;
                        shootY = 16.0F;
                        reload = 60.0F;
                        rotate = true;
                        alternate = true;
                        mirror = true;
                        rotateSpeed = 1.5F;
                        shootSound = Sounds.missile;
                        recoil = 2.0F;
                        shoot = new ShootSpread() {
                            {
                                spread = 1.2F;
                                shots = 2;
                                firstShotDelay = 30.0F;
                            }
                        };
                        bullet = new PositionLightningBulletType() {
                            {
                                maxRange = 550.0F;
                                rangeOverride = 550.0F;
                                lightningColor = Color.valueOf("FFA05C");
                                lightningDamage = 65.0F;
                                lightning = 2;
                                lightningLength = 4;
                                lightningLengthRand = 6;
                                hitEffect = WHFx.lightningSpark;
                                spawnBullets.add(new LaserBulletType() {
                                    {
                                        damage = 500.0F;
                                        length = 550.0F;
                                        width = 12.0F;
                                        sideAngle = 0.0F;
                                        sideWidth = 0.0F;
                                        sideLength = 0.0F;
                                        laserAbsorb = true;
                                        pierce = true;
                                        pierceBuilding = false;
                                        colors = new Color[]{Color.valueOf("FFA05C"), Color.valueOf("FFA05C"), Color.valueOf("FFA05C")};
                                        shootEffect = new ParticleEffect() {
                                            {
                                                particles = 1;
                                                sizeFrom = 12.0F;
                                                sizeTo = 0.0F;
                                                length = 0.0F;
                                                baseLength = 0.0F;
                                                lifetime = 15.0F;
                                                colorFrom = Color.valueOf("FFC397FF");
                                                colorTo = Color.valueOf("FFC397FF");
                                            }
                                        };
                                        smokeEffect = none;
                                    }
                                });
                            }
                        };
                    }
                });
                weapons.add(new Weapon("wh-tankAG-weapon2") {
                    {
                        shoot = new ShootAlternate() {
                            {
                                barrels = 2;
                                spread = 12.5F;
                                shots = 6;
                                shotDelay = 6.0F;
                            }
                        };
                        reload = 60.0F;
                        recoil = 3.0F;
                        x = 0.0F;
                        y = 19.0F;
                        shootY = 21.0F;
                        mirror = false;
                        rotateSpeed = 1.5F;
                        rotate = true;
                        inaccuracy = 8.0F;
                        shootSound = shootBig;
                        alternate = false;
                        ejectEffect = Fx.casing4;
                        shootCone = 15.0F;
                        bullet = new BasicBulletType() {
                            {
                                damage = 140.0F;
                                pierce = true;
                                pierceBuilding = false;
                                pierceArmor = true;
                                pierceCap = 5;
                                speed = 25.0F;
                                lifetime = 22.0F;
                                hitSound = Sounds.explosion;
                                shootEffect = shootBig2;
                                smokeEffect = shootBigSmoke;
                                trailLength = 4;
                                trailWidth = 3.3F;
                                trailColor = Color.valueOf("FFC397FF");
                                backColor = Color.valueOf("FFC397FF");
                                frontColor = Color.valueOf("FFFFFF");
                                width = 14.0F;
                                height = 33.0F;
                                hitEffect = new ParticleEffect() {
                                    {
                                        particles = 15;
                                        line = true;
                                        strokeFrom = 3.0F;
                                        strokeTo = 0.0F;
                                        lenFrom = 0.0F;
                                        lenTo = 11.0F;
                                        length = 60.0F;
                                        baseLength = 0.0F;
                                        lifetime = 10.0F;
                                        colorFrom = Color.valueOf("FFC397FF");
                                        colorTo = Color.valueOf("FFFFFF");
                                        cone = 60.0F;
                                    }
                                };
                                despawnEffect = Fx.flakExplosionBig;
                            }
                        };
                    }
                });
                weapons.add(new Weapon("wh-tankAG-weapon3") {
                    {
                        reload = 15.0F;
                        recoil = 2.0F;
                        recoilTime = 20.0F;
                        mirror = false;
                        x = 0.0F;
                        y = -21.0F;
                        rotate = true;
                        rotateSpeed = 0.8F;
                        shootCone = 20.0F;
                        xRand = 1.0F;
                        inaccuracy = 5.0F;
                        shootSound = laser;
                        shake = 1.0F;
                        layerOffset = 0.02F;
                        recoils = 2;
                        cooldownTime = 120.0F;
                        heatColor = Color.valueOf("FF4040");
                        shoot = new ShootBarrel() {
                            {
                                shots = 1;
                                barrels = new float[]{-24.0F, 62.0F, 0.0F, 24.0F, 62.0F, 0.0F};
                            }
                        };
                        ejectEffect = none;
                        velocityRnd = 0.06F;
                        parts.add(new RegionPart("-管l") {
                            {
                                y = -2.0F;
                                outline = true;
                                mirror = false;
                                moveY = -20.0F;
                                under = true;
                                recoilIndex = 0;
                                heatColor = Color.valueOf("FF4040");
                                heatProgress = PartProgress.recoil;
                                progress = PartProgress.recoil;
                            }
                        }, new RegionPart("-管r") {
                            {
                                y = -2.0F;
                                outline = true;
                                mirror = false;
                                moveY = -20.0F;
                                under = true;
                                recoilIndex = 1;
                                heatColor = Color.valueOf("FF4040");
                                heatProgress = PartProgress.recoil;
                                progress = PartProgress.recoil;
                            }
                        });
                        bullet = new BasicBulletType(10.3F, 900.0F) {
                            public final float hitChance;

                            {
                                keepVelocity = false;
                                sprite = "wh-透彻";
                                absorbable = false;
                                hittable = true;
                                pierce = true;
                                lightningColor = Color.valueOf("FFC397FF");
                                pierceArmor = true;
                                pierceBuilding = true;
                                pierceCap = 8;
                                splashDamageRadius = 48.0F;
                                splashDamage = 300.0F;
                                hitEffect = WHFx.sharpBlast(Color.valueOf("FFC397FF"), Color.valueOf("FFC397FF"), 35.0F, splashDamageRadius * 1.25F);
                                despawnEffect = WHFx.lightningHitLarge(Color.valueOf("FFC397FF"));
                                trailRotation = true;
                                hitSound = Sounds.plasmaboom;
                                trailLength = 12;
                                trailWidth = 5.0F;
                                trailColor = Color.valueOf("FFC397FF");
                                backColor = Color.valueOf("FFC397FF");
                                frontColor = Color.valueOf("FFFFFF");
                                drag = -0.07F;
                                width = 23.0F;
                                height = 75.0F;
                                hitSize = 25.0F;
                                lifetime = 25.5F;
                                hitChance = 0.2F;
                            }

                            public void despawned(Bullet b) {
                                Units.nearbyEnemies(b.team, b.x, b.y, splashDamageRadius, (u) -> {
                                    if (u.checkTarget(collidesAir, collidesGround) && u.type != null && u.targetable(b.team) && Mathf.chance(0.20000000298023224)) {
                                        float pDamage = splashDamage * 3.0F;
                                        if (u.health <= pDamage) {
                                            u.kill();
                                        } else {
                                            u.health -= pDamage;
                                        }
                                    }

                                });
                                super.despawned(b);
                            }
                        };
                    }
                });
            }
        };
        M4 = new UnitType("m4") {
            {
                constructor = MechUnit::create;
                canDrown = false;
                rotateSpeed = 1.1f;
                mechStepParticles = true;
                mechFrontSway = 1.3f;
                mechSideSway = 1.1f;
                mechLandShake = 0.1f;
                speed = 1f;
                weapons.add(new Weapon("wh-m4-weapon1") {
                    {
                        reload = 240f;
                        mirror = false;
                        shootY = 1f;
                        x = y = 0;
                        layerOffset = 0.002f;
                        shootCone = 30f;
                        shoot = new ShootBarrel() {{
                            shotDelay = 4.5f;
                            shots = 5;
                            barrels = new float[]
                                    {
                                            -5, 0, 0,
                                            0, 0, 0,
                                            5, 0, 0,
                                    };
                        }};
                        velocityRnd = 0.05f;
                        shootSound = WHSounds.launch;
                        bullet = new AccelBulletType(5f, 50) {
                            {
                                sprite = "wh-重型导弹";
                                width = 16f;
                                height = 16f;
                                shrinkY = 0f;
                                backColor = hitColor = lightColor = lightningColor = Color.valueOf("FFC397FF");
                                trailColor = Color.gray;
                                frontColor = Color.valueOf("FFC397FF");
                                homingPower = 0.08f;
                                homingDelay = 5f;
                                lifetime = 55f;
                                hitEffect = WHFx.instHit(backColor, 2, 10f);
                                despawnEffect = WHFx.shootCircleSmall(backColor);
                                lightning = 1;
                                lightningLength = 6;
                                lightningDamage = 40;
                                smokeEffect = Fx.shootPyraFlame;
                                shootEffect = WHFx.hugeSmokeGray;
                                trailColor = WHPal.pop2;
                                trailWidth = 2f;
                                trailLength = 5;

                            }
                        };
                    }

                    {
                        weapons.add(new Weapon("wh-m4-weapon2") {
                            {
                                x = -13;
                                y = 0;
                                reload = 10;
                                shootY = 10;
                                shootX = 0;
                                {
                                    shoot = new ShootAlternate() {
                                        {
                                            barrels = 2;
                                            spread = 4F;
                                            shots = 2;
                                        }
                                    };

                                    layerOffset = -0.001f;
                                    rotate = true;
                                    rotateSpeed = 0.7f;
                                    rotationLimit = 30;
                                    mirror = true;
                                    alternate = true;
                                    ejectEffect = none;
                                    recoil = 4;
                                    inaccuracy = 6;
                                    shootSound = shootBig;
                                    shootCone = 10;
                                    minWarmup = 0.95f;
                                    shootWarmupSpeed = 0.15f;
                                    bullet = new BasicBulletType() {
                                        {
                                            pierceCap = 3;
                                            absorbable = false;
                                            damage = 180;
                                            splashDamage = 100;
                                            splashDamageRadius = 32;
                                            speed = 20;
                                            hitShake = 1;
                                            lifetime = 10.5f;
                                            shootEffect = shootBig2;
                                            smokeEffect = shootBigSmoke;
                                            hitSound = Sounds.explosion;
                                            width = 9;
                                            height = 15;
                                            backColor = Color.valueOf("FFC397FF");

                                            frontColor = Color.valueOf("FFFFFF");

                                            trailLength = 18;
                                            trailWidth = 2;
                                            trailColor = Color.valueOf("FFC397FF");

                                            hitEffect = WHFx.shootCircleSmall(backColor);
                                            despawnEffect = blastExplosion;
                                        }

                                    };

                                }
                            }
                        });
                    }
                });
            }

        };
        StarrySky = new PesterUnitType("Starry-sky") {
            {
                constructor = PesterUnit::create;
                addEngine(-7, -107F, 0.0F, 4.0F, true);
                addEngine(-1, -107F, 0.0F, 4.0F, true);
                addEngine(-26, -83.25F, 0.0F, 5.0F, false);
                addEngine(-31, -83.25F, 0.0F, 5.0F, false);
                addEngine(9.25F, -107, 0.0F, 4.0F, false);
                addEngine(35, -113.25F, 0.0F, 4.0F, false);
                addEngine(40, -113.25F, 0.0F, 4.0F, false);
                addEngine(45, -113.25F, 0.0F, 4.0F, false);
                addEngine(50, -113.25F, 0.0F, 4.0F, false);
                engineOffset = 45.25f;
                engineSize = -1;
                abilities.add(new AdaptedHealAbility(1500, 900, hitSize * 2f, healColor).modify(a -> {
                    a.selfHealReloadTime = 480;
                    a.selfHealAmount /= 8;
                }));
                abilities.add(
                        new MoveEffectAbility() {{
                            minVelocity = 0;
                            rotateEffect = false;
                            effectParam = engineSize;
                            parentizeEffects = true;
                            teamColor = true;
                            display = false;

                            y = -engineOffset;
                            effect = new Effect(33, b -> {
                                Draw.color(b.color);
                                Angles.randLenVectors(b.id, (int) (b.rotation / 8f), b.rotation / 5f + b.rotation * 2f * b.fin(), (x, y) -> {
                                    Fill.circle(b.x + x, b.y + y, b.fout() * b.rotation / 2.25f);
                                });
                            });
                        }});
                class SKEngine extends UnitEngine {
                    final float triScl = 1;

                    public SKEngine(float x, float y, float radius, float rotation) {
                        super(x, y, radius, rotation);
                    }

                    public void draw(Unit unit) {
                        UnitType type = unit.type;
                        float scale = type.useEngineElevation ? unit.elevation : 1f;

                        if (scale <= 0.0001f) return;

                        float rot = unit.rotation - 90;

                        Color color = unit.team.color;

                        Tmp.v1.set(x, y).rotate(rot).add(unit);
                        float ex = Tmp.v1.x, ey = Tmp.v1.y;

                        Draw.color(color);
                        Fill.circle(
                                ex,
                                ey,
                                (radius + Mathf.absin(Time.time, 4f, radius / 4f)) * scale
                        );

                        float ang = Time.time * 1.5f;
                        for (int i : Mathf.signs) {
                            WHUtils.tri(ex, ey, radius / 3f * triScl, radius * 2.35f * triScl, ang + 90 * i);
                        }

                        ang *= -1.5f;
                        for (int i : Mathf.signs) {
                            WHUtils.tri(ex, ey, radius / 4f * triScl, radius * 1.85f * triScl, ang + 90 * i);
                        }

                        Draw.color(Color.white);
                        Fill.circle(
                                ex,
                                ey,
                                (radius + Mathf.absin(Time.time, 4f, radius / 4f)) * 0.785f * scale
                        );

                        Draw.color(Color.black);
                        Fill.circle(
                                ex,
                                ey,
                                (radius + Mathf.absin(Time.time, 4f, radius / 4f)) * 0.7f * scale
                        );

                    }
                }
                engines.add(new SKEngine(-36.25f, 4, 10f, -90f));
                engines.add(new AncientEngine(-36.25f, 4, 10f, -90f) {{
                    forceZ = Layer.flyingUnit - 1f;
                    alphaBase = alphaSclMin = 1;
                }});
                engineLayer = Layer.effect + 0.005f;

                weapons.add(new Weapon() {
                    {
                        shootCone = 30.0F;
                        predictTarget = false;
                        top = false;
                        mirror = false;
                        rotate = true;
                        x = -36.25f;
                        y = 4;
                        continuous = false;
                        rotateSpeed = 100.0F;
                        reload = 600.0F;

                        shake = 13.0F;
                        shootSound = Sounds.none;
                        bullet = new StrafeLaser(300.0F) {
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
                });

                weapons.add(new PointDefenseWeapon() {
                    {
                        beamEffect = Fx.chainLightning;
                        mirror = false;
                        x = -36.25f;
                        y = 5;
                        reload = 5f;
                        targetInterval = 10f;
                        targetSwitchInterval = 8f;
                        shootSound = laser;

                        bullet = new BulletType() {{
                            shootEffect = none;
                            hitEffect = WHFx.lightningHitSmall;
                            maxRange = 320f;
                            damage = 200f;
                        }};
                    }

                    @Override
                    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
                        if (!(mount.target instanceof Bullet)) return;

                        Bullet target = (Bullet) mount.target;
                        if (target.damage() > bullet.damage) {
                            target.damage(target.damage() - bullet.damage);
                        } else {
                            target.remove();
                        }

                        beamEffect.at(shootX, shootY, rotation, unit.team.color, new Vec2().set(target));
                        bullet.shootEffect.at(shootX, shootY, rotation, unit.team.color);
                        bullet.hitEffect.at(target.x, target.y, 12f, unit.team.color);
                        shootSound.at(shootX, shootY, Mathf.random(0.9f, 1.1f));
                        mount.recoil = 1f;
                        mount.heat = 1f;
                    }
                });

                weapons.add(new Weapon() {{
                    reload = 240f;
                    xRand = 10f;
                    shootY = 57f;
                    x = y = 0;
                    layerOffset = 0.002f;
                    mirror = rotate = false;
                    shoot = new ShootMulti(new ShootBarrel() {{
                        shots = 3;
                        barrels = new float[]
                                {
                                        45.5f, 0, 0,
                                        53f, 0, 0,
                                        68f, 0, 0,
                                };
                    }}, new ShootPattern() {{
                        shots = 15;
                        shotDelay = 4.5f;
                    }});

                    velocityRnd = 0.05f;
                    shootSound = WHSounds.launch;

                    bullet = new AccelBulletType(7f, 100) {
                        {
                            sprite = "wh-重型导弹";
                            width = 16f;
                            height = 16f;
                            shrinkY = 0f;
                            accelerateBegin = 0.1f;
                            accelerateEnd = 0.6f;

                            velocityBegin = 3f;
                            velocityIncrease = 11f;

                            backColor = hitColor = lightColor = lightningColor = WHPal.pop;
                            trailColor = Color.gray;
                            frontColor = WHPal.pop;
                            homingPower = 0.08f;
                            homingDelay = 5f;
                            lifetime = 120f;
                            hitEffect = WHFx.instHit(backColor, 2, 30f);
                            despawnEffect = WHFx.shootCircleSmall(backColor);
                            lightning = 1;
                            lightningLengthRand = 4;
                            lightningLength = 5;
                            lightningDamage = 90;
                            lightningColor = WHPal.pop;
                            smokeEffect = Fx.shootPyraFlame;
                            shootEffect = WHFx.hugeSmokeGray;
                            trailWidth = 2f;
                            trailLength = 8;
                        }

                        @Override
                        public void updateHoming(Bullet b) {
                            if (b.time >= this.homingDelay) {
                                if (b.owner instanceof Unit) {
                                    Unit u = (Unit) b.owner;
                                    if (u.isPlayer()) {
                                        Player p = u.getPlayer();
                                        b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(p.mouseX, p.mouseY), homingPower * Time.delta * 50.0F));
                                    } else super.updateHoming(b);
                                } else super.updateHoming(b);
                            }
                        }
                    };
                    baseRotation = -90;
                    minWarmup = 0.9f;
                    shootWarmupSpeed /= 3f;
                    shootCone = 360;
                    recoil = 2f;
                    parts.add(new RegionPart("wh-Starry-sky-weapon1") {
                        {
                            under = outline = true;
                            rotation = 90;
                        }
                    });

                }});
                weapons.add(new Weapon("wh-Starry-sky-weapon2") {
                    {
                        x = -41.5f;
                        y = -58.5f;
                        reload = 260;
                        recoil = 3;
                        shootCone = 20;
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 1;
                        shootY = 8.2f;
                        shootX = 0;
                        shootSound = WHSounds.dd1;
                        shoot = new ShootAlternate() {
                            {
                                shots = 2;
                                barrels = 2;
                                spread = 4F;
                                shotDelay = 20;
                            }
                        };
                        bullet = new TextureMissileType(1200f, "wh-Starry-sky-missile") {{
                            velocityBegin = 2f;
                            velocityIncrease = 10f;
                            absorbable = false;
                            scaledSplashDamage=true;
                            splashDamage = 600;
                            splashDamageRadius = 64f;
                            incendAmount = 2;
                            incendChance = 0.08f;
                            incendSpread = 24f;
                            makeFire = false;
                            lifetime += 50;
                            trailColor = WHPal.pop;
                            trailEffect = WHFx.trailToGray;
                            trailParam = 2f;
                            trailChance = 0.2f;
                            trailLength = 15;
                            trailWidth = 1.2f;

                            width = 15;
                            height = 20f;
                            lightning = 3;
                            lightningLengthRand = 4;
                            lightningLength = 6;
                            lightningDamage = 130;
                            backColor = hitColor = lightColor = lightningColor = WHPal.pop;
                            frontColor = WHPal.pop2;

                            smokeEffect = none;
                            shootEffect = none;
                            despawnEffect = hitEffect = new MultiEffect(WHFx.lightningHitLarge, WHFx.hitSparkHuge);


                            hitShake = despawnShake = 2f;
                            despawnSound = hitSound = Sounds.explosion;
                        }};
                        reload = 300f;
                        inaccuracy = 2f;
                        velocityRnd = 0.1f;
                        shake = 1.25f;
                        shootSound = WHSounds.launch;
                        shootCone = 8f;
                    }
                });
                weapons.add(new Weapon("wh-Starry-sky-weapon2") {
                    {
                        x = -4.8f;
                        y = 112.5f;
                        reload = 260;
                        recoil = 3;
                        shootCone = 20;
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 1;
                        shootY = 8.2f;
                        shootX = 0;
                        shootSound = WHSounds.dd1;
                        shoot = new ShootAlternate() {
                            {
                                shots = 2;
                                barrels = 2;
                                spread = 4F;
                                shotDelay = 20;
                            }
                        };
                        bullet = new TextureMissileType(1200f, "wh-Starry-sky-missile") {{
                            velocityBegin = 2f;
                            velocityIncrease = 10f;
                            absorbable = false;
                            scaledSplashDamage=true;
                            splashDamage = 500;
                            splashDamageRadius = 64f;
                            incendAmount = 2;
                            incendChance = 0.08f;
                            incendSpread = 24f;
                            makeFire = false;
                            lifetime += 40f;
                            trailColor = WHPal.pop;
                            trailEffect = WHFx.trailToGray;
                            trailParam = 2f;
                            trailChance = 0.2f;
                            trailLength = 15;
                            trailWidth = 1.2f;

                            width = 15;
                            height = 20f;

                            backColor = hitColor = lightColor = lightningColor = WHPal.pop;
                            frontColor = WHPal.pop2;

                            smokeEffect = none;
                            shootEffect = none;
                            despawnEffect = hitEffect = new MultiEffect(WHFx.lightningHitLarge, WHFx.hitSparkHuge);
                            lightning = 3;
                            lightningLengthRand = 4;
                            lightningLength = 8;
                            lightningDamage = 130;

                            hitShake = despawnShake = 2f;
                            despawnSound = hitSound = Sounds.explosion;
                        }};
                        reload = 300f;
                        inaccuracy = 2f;
                        velocityRnd = 0.1f;

                        shake = 1.25f;
                        shootSound = WHSounds.launch;
                        shootCone = 8f;
                    }
                });
                weapons.add(new Weapon("wh-Starry-sky-weapon2") {
                    {
                        x = -15f;
                        y = -1f;
                        reload = 260;
                        recoil = 3;
                        shootCone = 20;
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 1;
                        shootY = 8.2f;
                        shootX = 0;
                        shootSound = WHSounds.dd1;
                        shoot = new ShootAlternate() {
                            {
                                shots = 4;
                                barrels = 2;
                                spread = 4F;
                                shotDelay = 20;
                            }
                        };
                        bullet = new TextureMissileType(1200f, "wh-Starry-sky-missile") {{
                            velocityBegin = 2f;
                            velocityIncrease = 10f;
                            absorbable = false;
                            scaledSplashDamage=true;
                            splashDamage = 600;
                            splashDamageRadius = 64f;
                            incendAmount = 2;
                            incendChance = 0.08f;
                            incendSpread = 24f;
                            makeFire = false;
                            lifetime += 50f;
                            trailColor = WHPal.pop;
                            trailEffect = WHFx.trailToGray;
                            trailParam = 2f;
                            trailChance = 0.2f;
                            trailLength = 15;
                            trailWidth = 1.2f;

                            width = 15;
                            height = 20f;

                            backColor = hitColor = lightColor = lightningColor = WHPal.pop;
                            frontColor = WHPal.pop2;

                            smokeEffect = none;
                            shootEffect = none;
                            despawnEffect = hitEffect = new MultiEffect(WHFx.lightningHitLarge, WHFx.hitSparkHuge);
                            lightning = 3;
                            lightningLengthRand = 4;
                            lightningLength = 10;
                            lightningDamage = 130;
                            lightningColor = WHPal.pop;

                            hitShake = despawnShake = 2f;
                            despawnSound = hitSound = Sounds.explosion;
                        }};
                        reload = 300f;
                        inaccuracy = 2f;
                        velocityRnd = 0.1f;

                        shake = 1.25f;
                        shootSound = WHSounds.launch;
                        shootCone = 8f;
                    }
                });
                weapons.add(new Weapon("wh-Starry-sky-weapon3") {
                    {
                        layerOffset = 0.01F;
                        reload = 200.0F;
                        recoil = 1.5F;
                        x = 34F;
                        y = -19F;
                        shootY = 16.75F;
                        rotate = true;
                        mirror = false;
                        rotateSpeed = 0.6F;
                        inaccuracy = 1.8F;
                        shootCone = 31.8F;
                        shootSound = Sounds.bolt;
                        alternate = false;
                        shake = 0.3F;
                        ejectEffect = Fx.casing3Double;
                        parts.add(new RegionPart("-管") {
                            {
                                progress = PartProgress.recoil;
                                mirror = false;
                                under = true;
                                x = 0.0F;
                                y = 0.0F;
                                moveY = -8.0F;
                            }
                        });
                        shoot = new ShootAlternate() {
                            {
                                barrels = 2;
                                spread = 10.5F;
                                shots = 6;
                                shotDelay = 15.0F;
                            }
                        };
                        shootY = 0.25F;
                        recoil = 4.0F;
                        bullet = new TrailFadeBulletType() {
                            {
                                damage = 800.0F;
                                tracerUpdateSpacing *= 1.25F;
                                pierce = true;
                                pierceCap = 3;
                                speed = 12.0F;
                                tracerStroke -= 0.3f;
                                tracers = 1;
                                keepVelocity = true;

                                tracerSpacing = 10f;
                                tracerUpdateSpacing *= 1.25f;

                                status = WHStatusEffects.palsy;
                                statusDuration = 400.0F;
                                buildingDamageMultiplier = 0.3F;
                                lightningDamage = 100.0F;
                                lightning = 2;
                                lightningLength = 9;
                                lightningLengthRand = 3;
                                splashDamage = 400;
                                splashDamageRadius = 40f;
                                lightColor = lightningColor = WHPal.pop;
                                lifetime = 47.0F;
                                width = 15.0F;
                                height = 30.0F;
                                frontColor = WHPal.pop;
                                backColor = WHPal.pop;
                                trailLength = 13;
                                trailWidth = 3.0F;
                                trailColor = Color.valueOf("FEEBB3");
                                shootEffect = EffectWrapper.wrap(WHFx.shootLine(33, 28), hitColor);
                                smokeEffect = new ParticleEffect() {
                                    {
                                        particles = 8;
                                        line = true;
                                        interp = Interp.fastSlow;
                                        sizeInterp = Interp.pow3In;
                                        lenFrom = 6.0F;
                                        lenTo = 0.0F;
                                        strokeFrom = 2.0F;
                                        strokeTo = 0.0F;
                                        length = 35.0F;
                                        baseLength = 0.0F;
                                        lifetime = 23.0F;
                                        colorFrom = colorTo = WHPal.pop;
                                        cone = 15.0F;
                                    }
                                };
                                hitEffect = new MultiEffect(new ParticleEffect() {
                                    {
                                        particles = 4;
                                        region = "wh-菱形";
                                        sizeFrom = 12.0F;
                                        sizeTo = 0.0F;
                                        length = 30.0F;
                                        baseLength = 0.0F;
                                        lifetime = 45.0F;
                                        colorFrom = colorTo = WHPal.pop;
                                    }
                                });
                                despawnEffect = WHFx.crossBlast(WHPal.pop, splashDamageRadius * 1.2f, 45);
                            }

                        };
                    }
                });
                weapons.add(new Weapon("wh-Starry-sky-weapon4") {
                    {
                        x = 26.25f;
                        y = 27.2f;
                        reload = 80;
                        recoil = 3;
                        shootCone = 20;
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 1;
                        shootY = 8.2f;
                        shootX = 0;
                        shootSound = missile;
                        shoot = new ShootBarrel() {
                            {
                                shots = 6;
                                shotDelay = 4;
                                barrels = new float[]{0, 7, 0, -4, 7, 0, 4, 7, 0};
                            }
                        };
                        bullet = new MissileBulletType(5.5f, 70) {{
                            width = 12f;
                            height = 12f;
                            shrinkY = 0f;
                            drag = -0.01f;
                            backColor = trailColor = lightColor = lightningColor = hitColor = WHPal.pop;
                            frontColor = backColor.cpy().lerp(Color.white, 0.7f);
                            splashDamageRadius = 24;
                            splashDamage = 80;
                            despawnEffect = Fx.smoke;
                            hitEffect = WHFx.hitSpark;
                            lifetime = 50f;
                            lightningDamage = damage / 2;
                            lightning = 2;
                            lightningLength = 10;
                            inaccuracy = 5;
                            trailColor = WHPal.pop;
                            trailWidth = 1f;
                            trailLength = 7;
                        }};
                    }
                });
                weapons.add(new Weapon("wh-Starry-sky-weapon4") {
                    {
                        x = -47.5f;
                        y = -29.5f;
                        reload = 80;
                        recoil = 3;
                        shootCone = 20;
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 1;
                        shootY = 8.2f;
                        shootX = 0;
                        shootSound = missile;
                        shoot = new ShootBarrel() {
                            {
                                shots = 6;
                                shotDelay = 4;
                                barrels = new float[]{0, 7, 0, -4, 7, 0, 4, 7, 0};
                            }
                        };
                        bullet = new MissileBulletType(5.5f, 70) {{
                            width = 12f;
                            height = 12f;
                            shrinkY = 0f;
                            drag = -0.01f;
                            backColor = trailColor = lightColor = lightningColor = hitColor = WHPal.pop;
                            frontColor = backColor.cpy().lerp(Color.white, 0.7f);
                            splashDamageRadius = 24;
                            splashDamage = 80;
                            despawnEffect = Fx.smoke;
                            hitEffect = WHFx.hitSpark;
                            lifetime = 50f;
                            lightningDamage = damage / 2;
                            lightning = 2;
                            lightningLength = 10;
                            inaccuracy = 5;
                            trailColor = WHPal.pop;
                            trailWidth = 1f;
                            trailLength = 7;
                        }};
                    }
                });
                weapons.add(new Weapon("wh-Starry-sky-weapon5") {
                    {
                        reload = 25F;
                        rotate = true;
                        rotateSpeed = 2F;
                        mirror = false;
                        x = 17.75F;
                        y = -86.25F;
                        shootY = 20.0F;
                        shoot = new ShootAlternate() {
                            {
                                barrels = 2;
                                spread = 7.5F;
                            }
                        };
                        bullet = new PositionLightningBulletType(200) {
                            {
                                maxRange = 350F;
                                rangeOverride = 350F;
                                shootSound=laser;
                                hitColor = lightColor = lightningColor = WHPal.pop;
                                shootEffect = WHFx.hitSparkLarge;
                                hitEffect = WHFx.lightningHitSmall;
                                smokeEffect = WHFx.hugeSmokeGray;
                                spawnBullets.add(new LaserBulletType(300) {
                                    {
                                        sideAngle = 25.0F;
                                        sideWidth = 1.35F;
                                        sideLength = 30.0F;
                                        colors = new Color[]{Color.valueOf("FBE5BFFF"), Color.valueOf("FFFBBDE1"), Color.valueOf("FFFBBDE1")};
                                        width = 11.0F;
                                        length = 350F;
                                    }
                                });
                            }
                        };
                    }
                });

            }
        };
    }

}



