//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.content;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import mindustry.type.unit.*;
import mindustry.type.weapons.*;
import mindustry.world.meta.*;
import wh.entities.abilities.*;
import wh.entities.bullet.*;
import wh.entities.bullet.laser.*;
import wh.entities.world.drawer.part.*;
import wh.entities.world.entities.*;
import wh.entities.world.entities.AirRaiderAI.*;
import wh.entities.world.entities.AirRaiderUnitType.*;
import wh.entities.world.entities.powerArmorComp.*;
import wh.entities.world.entities.weapon.*;
import wh.gen.*;
import wh.graphics.*;
import wh.util.*;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static mindustry.content.Fx.none;
import static mindustry.content.Fx.*;
import static mindustry.content.StatusEffects.*;
import static mindustry.gen.Sounds.explosion;
import static mindustry.gen.Sounds.*;
import static wh.content.WHFx.rand;
import static wh.content.WHFx.*;
import static wh.core.WarHammerMod.name;
import static wh.graphics.Drawn.arcProcessFlip;

public final class WHUnitTypes{

    public static final byte
    ANCIENT_GROUND = 10, ANCIENT_AIR = 11,
    OTHERS = Byte.MIN_VALUE,
    GROUND_LINE_1 = 0, AIR_LINE_1 = 1, AIR_LINE_2 = 2, ENERGY_LINE_1 = 3, NAVY_LINE_1 = 6;

    public static UnitType
    //空军
    air8Enemy, air7Enemy,
    cMoon, revenge,
    air7,
    air6, air5, air4, air3, air2, air1,
    //空辅
    airB6, airB5, airB4, airB3, airB2, airB1,

    //空袭
    airRaiderS, airRaiderM, airRaiderB,
    //坦克
    tankAG,
    tank3s, tank2s, tank1s,
    tank3, tank2, tank1,
    //机甲
    Mecha7, Mecha6, Mecha5, Mecha4, Mecha3, Mecha2,
    //陆军
    M6, M5,
    M4A, M4B, M4C, M4D,
    M3, M2, M1,
    test, test2,
    //特种
    MEn1,
    tankEn2, tankEn1,
    airEn1, airEn2;
    //核心机

    //原版
    public static UnitType airS6, airSGreen6, mechaS6, mechaSGreen6, meshSPurple6, navyS6, navySGreen6;

    private WHUnitTypes(){
    }

    static{
        EntityMapping.nameMap.put("wh-c-moon", NucleoidUnit::new);
        EntityMapping.nameMap.put("wh-Starry-sky", StarrySkyEntity::new);
    }

    public static void load(){


        cMoon = new NucleoidUnitType("c-moon"){
            {
                outlineRadius = 3;
                outlineColor = Color.valueOf("36363CFF");
                speed = 0.5F;
                accel = 0.036F;
                drag = 0.032F;
                fogRadius = 90;
                lightRadius = 600;
                lowAltitude = true;
                healColor = Color.valueOf("FFFBBD80");
                range = 1000;
                flying = true;
                engineSize = 0;
                hitSize = 180;
                health = 900000;
                armor = 100;
                targetFlags = new BlockFlag[]{BlockFlag.turret, null};
                rotateSpeed = 0.25F;
                ammoCapacity = 100000;
                deathExplosionEffect = none;
                createWreck = false;
                addEngine(-58.0F, -175.0F, 0.0F, 5.0F, true);
                addEngine(-53.0F, -175.0F, 0.0F, 5.0F, true);
                addEngine(-8.0F, -151.0F, 0.0F, 5.0F, true);
                addEngine(-4.0F, -151.0F, 0.0F, 5.0F, true);
                addEngine(-1.0F, -151.0F, 0.0F, 5.0F, true);

                abilities.add(new Ability(){
                    {
                        display = false;
                    }

                    public void death(Unit unit){
                        Effect.shake(unit.hitSize / 10.0F, unit.hitSize / 8.0F, unit.x, unit.y);
                        WHFx.lineCircleOut(30, 30, 3).at(unit.x, unit.y, unit.hitSize, unit.team.color);
                        WHFx.jumpTrailOut.at(unit.x, unit.y, unit.rotation, unit.team.color, unit.type);
                        WHSounds.jump.at(unit.x, unit.y, 1.0F, 3.0F);
                    }
                });
                abilities.add((new AdaptedHealAbility(3000.0F, 180.0F, 220.0F, healColor)).modify((a) -> a.selfHealReloadTime = 300.0F));
                abilities.add((new ShockWaveAbility(180.0F, 320.0F, 2000.0F, WHPal.WHYellow)).status(StatusEffects.unmoving, 300.0F, StatusEffects.disarmed, 300.0F).modify((a) -> {
                    a.knockback = 400.0F;
                    a.shootEffect = new MultiEffect(WHFx.lineCircleOut(a.hitColor, 30, 30, 3), WHFx.hitSpark(a.hitColor, 55.0F, 40, a.range + 30.0F, 3.0F, 8.0F), WHFx.smoothColorCircle(WHPal.WHYellow.cpy().a(0.3F), a.range, 60.0F));
                }));
                weapons.add(new Weapon("wh-c-moon-weapon3"){
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
                        shootSound = shootRipple;
                        shoot = new ShootAlternate(){
                            {
                                shots = 2;
                                spread = 25.0F;
                                shotDelay = 15.0F;
                            }
                        };
                        bullet = new TrailFadeBulletType(20.0F, 1800.0F){
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
                                hitColor = backColor = lightColor = lightningColor = WHPal.WHYellow;
                                trailColor = WHPal.WHYellow;
                                frontColor = WHPal.WHYellow2;
                                width = 18.0F;
                                height = 60.0F;
                                hitSound = explosionReactor;
                                despawnShake = hitShake = 18.0F;
                                pierce = pierceArmor = true;
                                pierceCap = 3;
                                pierceBuilding = false;
                                lightning = 3;
                                lightningLength = 8;
                                lightningLengthRand = 8;
                                lightningDamage = 300.0F;
                                smokeEffect = new WrapEffect(WHFx.hitSparkHuge, hitColor);
                                shootEffect = WHFx.instShoot(backColor, frontColor);
                                despawnEffect = WHFx.lightningHitLarge;
                            }

                            public void createFrags(Bullet b, float x, float y){
                                super.createFrags(b, x, y);
                                WHBullets.nuBlackHole.create(b, x, y, 0.0F);
                            }
                        };

                        parts.add(new RegionPart("-管l"){
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
                        parts.add(new RegionPart("-管r"){
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

                    protected Teamc findTarget(Unit unit, float x, float y, float range, boolean air, boolean ground){
                        return Units.bestTarget(unit.team, x, y, range, (u) -> u.checkTarget(air, ground), (t) -> {
                            return ground;
                        }, UnitSorts.strongest);
                    }
                });
                weapons.add(new Weapon("wh-c-moon-weapon1"){
                    {
                        x = 39.0F;
                        y = 34.0F;
                        shootY = 16.0F;
                        reload = 15.0F;
                        rotate = true;
                        alternate = false;
                        rotateSpeed = 4.0F;
                        shake = 1.0F;
                        shootSound = shootOmura;
                        soundPitchMin = 1.5F;
                        soundPitchMax = 1.5F;
                        heatColor = Color.valueOf("FF9A79FF");
                        shoot = new ShootAlternate(){
                            {
                                barrels = 2;
                                spread = 7.5F;
                            }
                        };
                        bullet = new ChainLightingBulletType(300){
                            {
                                length = 600.0F;
                                hitColor = lightColor = lightningColor = WHPal.WHYellow;
                                shootEffect = WHFx.hitSparkLarge;
                                hitEffect = WHFx.lightningHitSmall;
                            }
                        };
                    }
                });
                weapons.add(new PointDefenseWeapon("wh-c-moon-weapon1"){
                    {
                        color = WHPal.WHYellow;
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
                        bullet = new BulletType(){
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
                weapons.add(new Weapon("wh-c-moon-weapon1"){
                    {
                        reload = 20.0F;
                        rotate = true;
                        rotateSpeed = 3.0F;
                        alternate = false;
                        x = -97.5F;
                        y = -73.25F;
                        shootY = 20.0F;
                        shoot = new ShootAlternate(){
                            {
                                barrels = 2;
                                spread = 7.5F;
                            }
                        };
                        bullet = new PositionLightningBulletType(200.0F){
                            {
                                maxRange = 600.0F;
                                rangeOverride = 600.0F;
                                hitColor = lightColor = lightningColor = WHPal.WHYellow;
                                shootEffect = WHFx.hitSparkLarge;
                                hitEffect = WHFx.lightningHitSmall;
                                spawnBullets.add(new LaserBulletType(800.0F){
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
                weapons.add(new Weapon("wh-c-moon-weapon2"){
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
                        shootSound = shootLaser;
                        alternate = false;
                        shake = 0.3F;
                        ejectEffect = Fx.casing3Double;
                        parts.add(new RegionPart("-管"){
                            {
                                progress = PartProgress.recoil;
                                mirror = false;
                                under = true;
                                x = 0.0F;
                                y = 0.0F;
                                moveY = -8.0F;
                            }
                        });
                        shoot = new ShootAlternate(){
                            {
                                barrels = 2;
                                spread = 10.5F;
                                shots = 6;
                                shotDelay = 15.0F;
                            }
                        };
                        bullet = new TrailFadeBulletType(){
                            {
                                damage = 800.0F;
                                tracerStroke -= 0.3f;
                                tracers = 1;
                                keepVelocity = true;
                                hitEffect = none;
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
                                lightColor = lightningColor = WHPal.WHYellow;
                                lifetime = 47.0F;
                                width = 15.0F;
                                height = 30.0F;
                                frontColor = WHPal.WHYellow;
                                backColor = WHPal.WHYellow;
                                trailLength = 13;
                                trailWidth = 3.0F;
                                trailColor = Color.valueOf("FEEBB3");
                                shootEffect = shootBig2;
                                smokeEffect = new ParticleEffect(){
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
                                        colorFrom = colorTo = WHPal.WHYellow;
                                        cone = 15.0F;
                                    }
                                };
                                despawnEffect = new MultiEffect(){
                                    {
                                        new ParticleEffect(){
                                            {
                                                particles = 4;
                                                region = "wh-菱形";
                                                sizeFrom = 10.0F;
                                                sizeTo = 0.0F;
                                                length = 50.0F;
                                                baseLength = 0.0F;
                                                lifetime = 60.0F;
                                                colorFrom = colorTo = WHPal.WHYellow;
                                            }
                                        };
                                        new ParticleEffect(){
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
                                                colorFrom = colorTo = WHPal.WHYellow;
                                            }
                                        };
                                        new WaveEffect(){
                                            {
                                                lifetime = 70.0F;
                                                sizeFrom = 0.0F;
                                                sizeTo = 70.0F;
                                                strokeFrom = 3.0F;
                                                strokeTo = 0.0F;
                                                colorFrom = colorTo = WHPal.WHYellow;
                                            }
                                        };
                                        new WaveEffect(){
                                            {
                                                lifetime = 70.0F;
                                                sizeFrom = 40.0F;
                                                sizeTo = 70.0F;
                                                strokeFrom = 2.5F;
                                                strokeTo = 0.0F;
                                                colorFrom = colorTo = WHPal.WHYellow;
                                            }
                                        };
                                    }
                                };
                            }
                        };
                    }
                });
                weapons.add(new Weapon("wh-c-moon-weapon2"){
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
                        shootSound = shootLaser;
                        alternate = false;
                        shake = 0.3F;
                        ejectEffect = Fx.casing3Double;
                        parts.add(new RegionPart("-管"){
                            {
                                progress = PartProgress.recoil;
                                mirror = false;
                                under = true;
                                x = 0.0F;
                                y = 0.0F;
                                moveY = -8.0F;
                            }
                        });
                        shoot = new ShootAlternate(){
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
                        bullet = new TrailFadeBulletType(){
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
                                lightningColor = WHPal.WHYellow;
                                lifetime = 47.0F;
                                width = 15.0F;
                                height = 30.0F;
                                frontColor = WHPal.WHYellow;
                                backColor = WHPal.WHYellow;
                                trailLength = 13;
                                trailWidth = 3.0F;
                                trailColor = Color.valueOf("FEEBB3");
                                shootEffect = shootBig2;
                                smokeEffect = new ParticleEffect(){
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
                                        colorFrom = colorTo = WHPal.WHYellow;
                                        cone = 15.0F;
                                    }
                                };
                                hitEffect = none;
                                despawnEffect = new MultiEffect(){
                                    {
                                        new ParticleEffect(){
                                            {
                                                particles = 4;
                                                region = "wh-菱形";
                                                sizeFrom = 10.0F;
                                                sizeTo = 0.0F;
                                                length = 50.0F;
                                                baseLength = 0.0F;
                                                lifetime = 60.0F;
                                                colorFrom = colorTo = WHPal.WHYellow;
                                            }
                                        };
                                        new ParticleEffect(){
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
                                                colorFrom = colorTo = WHPal.WHYellow;
                                            }
                                        };
                                        new WaveEffect(){
                                            {
                                                lifetime = 70.0F;
                                                sizeFrom = 0.0F;
                                                sizeTo = 70.0F;
                                                strokeFrom = 3.0F;
                                                strokeTo = 0.0F;
                                                colorFrom = colorTo = WHPal.WHYellow;
                                            }
                                        };
                                        new WaveEffect(){
                                            {
                                                lifetime = 70.0F;
                                                sizeFrom = 40.0F;
                                                sizeTo = 70.0F;
                                                strokeFrom = 2.5F;
                                                strokeTo = 0.0F;
                                                colorFrom = colorTo = WHPal.WHYellow;
                                            }
                                        };
                                    }
                                };
                            }
                        };
                    }
                });
                weapons.add(new Weapon("wh-c-moon-weapon4"){
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
                        bullet = new BasicBulletType(){
                            {
                                parts.add(new ShapePart(){
                                    {
                                        color = Color.valueOf("000000ff");
                                        circle = true;
                                        radius = 23.0F;
                                        radiusTo = 30.0F;
                                        layer = 114.0F;
                                    }
                                }, new ShapePart(){
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
                                trailColor = WHPal.WHYellow;
                                trailInterval = 1.0F;
                                trailRotation = true;
                                trailEffect = new ParticleEffect(){
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
                                chargeEffect = new MultiEffect(new WaveEffect(){
                                    {
                                        lifetime = 99.0F;
                                        sizeFrom = 190.0F;
                                        sizeTo = 0.0F;
                                        strokeFrom = 0.0F;
                                        strokeTo = 10.0F;
                                        colorFrom = colorTo = WHPal.WHYellow;
                                    }
                                }, new WaveEffect(){
                                    {
                                        startDelay = 49.0F;
                                        interp = Interp.circleIn;
                                        lifetime = 50.0F;
                                        sizeFrom = 190.0F;
                                        sizeTo = 0.0F;
                                        strokeFrom = 0.0F;
                                        strokeTo = 10.0F;
                                        colorFrom = colorTo = WHPal.WHYellow;
                                    }
                                }, new Effect(180.0F, (e) -> {
                                    Draw.color(WHPal.WHYellow);
                                    Fill.circle(e.x, e.y, 23.0F * e.finpow());
                                    float z = Draw.z();
                                    Draw.z(212.0F);
                                    Draw.color(Color.black);
                                    Fill.circle(e.x, e.y, 20.0F * e.finpow());
                                    Draw.z(z);
                                    Angles.randLenVectors(e.id, 16, 60.0F * e.foutpow(), Mathf.randomSeed(e.id, 360.0F), 360.0F, (x, y) -> {
                                        Draw.color(WHPal.WHYellow);
                                        Fill.circle(e.x + x, e.y + y, 12.0F * e.foutpow());
                                        float zs = Draw.z();
                                        Draw.z(210.0F);
                                        Draw.color(Color.black);
                                        Fill.circle(e.x + x, e.y + y, 12.0F * e.foutpow());
                                        Draw.z(zs);
                                    });
                                }));
                                chargeSound = chargeCorvus;
                                fragBullets = 1;
                                fragRandomSpread = 0.0F;
                                fragBullet = new BlackHoleBulletType(){
                                    {
                                        keepVelocity = false;
                                        inRad = 32.0F;
                                        outRad = drawSize = 88.0F;
                                        accColor = WHPal.WHYellow;
                                        speed = 0.0F;
                                        lifetime = 150.0F;
                                        splashDamageRadius = 180.0F;
                                        splashDamage = 1500.0F;
                                        despawnEffect = hitEffect = new MultiEffect(WHFx.smoothColorCircle(WHPal.WHYellow, splashDamageRadius * 1.25F, 95.0F), WHFx.circleOut(WHPal.WHYellow, splashDamageRadius * 1.25F), WHFx.hitSparkLarge);
                                        fragBullets = 15;
                                        fragVelocityMax = 1.3F;
                                        fragVelocityMin = 0.5F;
                                        fragLifeMax = 1.3F;
                                        fragLifeMin = 0.4F;
                                        fragBullet = new LightningLinkerBulletType(3.5F, 800.0F){
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
                                                lightningColor = WHPal.WHYellow;
                                                drag = 0.04F;
                                                knockback = 0.8F;
                                                lifetime = 90.0F;
                                                splashDamageRadius = 64.0F;
                                                splashDamage = 800.0F;
                                                backColor = trailColor = hitColor = WHPal.WHYellow;
                                                frontColor = WHPal.WHYellow;
                                                despawnShake = 7.0F;
                                                lightRadius = 30.0F;
                                                lightColor = WHPal.WHYellow;
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

        revenge = new SuperHeavyUnitType("revenge"){
            {
                constructor = RevengeUnit::new;

                speed = 0.5f;
                range = 600;
                rotateSpeed = 0.2f;

                health = 280 * 1000;
                armor = 40;
                hitSize = 100;
                healColor = WHPal.WHYellow;
                fogRadius = range * 0.3f;
                lightRadius = 600;

                ammoCapacity = 30000;
                flyingLayer = Layer.bullet - 0.1f;
                flying = true;

                targetFlags = new BlockFlag[]{BlockFlag.turret, null};

                engineOffset = 45.25f;
                engineSize = -1;

                for(int i = 0; i < 5; i++){
                    addEngine(-7 + i * 2, -107f, 0f, 3f, true);
                }

                for(int i = 0; i < 3; i++){
                    addEngine(7 + i, -107f, 0f, 1f, true);
                }

                for(int i = 0; i < 8; i++){
                    addEngine(-36 + i, -83.25f, 0f, 2, false);
                }

                for(int i = 0; i < 10; i++){
                    addEngine(35 + 1.5f * i, -113f, 2, 2, false);
                }

                abilities.add(new AdaptedHealAbility(2000, 10 * 60f, hitSize * 2f, healColor).modify(a -> {
                    a.selfHealReloadTime = a.reload;
                    a.selfHealAmount = 0.35f / 60f;
                }));

                class SKEngine extends UnitEngine{
                    final float triScl = 1;

                    public SKEngine(float x, float y, float radius, float rotation){
                        super(x, y, radius, rotation);
                    }

                    public void draw(Unit unit){
                        UnitType type = unit.type;
                        float scale = type.useEngineElevation ? unit.elevation : 1f;

                        if(scale <= 0.0001f) return;

                        float rot = unit.rotation - 90;

                        Color color = unit.team.color;

                        Tmp.v1.set(x, y).rotate(rot).add(unit);
                        float ex = Tmp.v1.x, ey = Tmp.v1.y;

                        Draw.color(color);
                        Fill.circle(ex, ey, (radius + Mathf.absin(Time.time, 4f, radius / 4f)) * scale
                        );

                        float ang = Time.time * 1.5f;
                        for(int i : Mathf.signs){
                            WHUtils.tri(ex, ey, radius / 3f * triScl, radius * 2.35f * triScl, ang + 90 * i);
                        }

                        ang *= -1.5f;
                        for(int i : Mathf.signs){
                            WHUtils.tri(ex, ey, radius / 4f * triScl, radius * 1.85f * triScl, ang + 90 * i);
                        }

                        Draw.color(Color.white);
                        Fill.circle(ex, ey, (radius + Mathf.absin(Time.time, 4f, radius / 4f)) * 0.785f * scale);

                        Draw.color(Color.black);
                        Fill.circle(ex, ey, (radius + Mathf.absin(Time.time, 4f, radius / 4f)) * 0.7f * scale);

                    }
                }
                engines.add(new SKEngine(-36.25f, 4, 10f, -90f));
                engines.add(new AncientEngine(-36.25f, 4, 10f, -90f){{
                    forceZ = Layer.flyingUnit - 1f;
                    alphaBase = alphaSclMin = 1;
                }});
                engineLayer = Layer.effect + 0.005f;

                weapons.add(new PointDefenseWeapon(){
                    {
                        beamEffect = Fx.chainLightning;
                        mirror = false;
                        x = -36.25f;
                        y = 5;
                        reload = 5f;
                        targetInterval = 10f;
                        targetSwitchInterval = 8f;
                        shootSound = shootLaser;

                        bullet = new BulletType(){{
                            shootEffect = none;
                            reflectable = absorbable = collides = false;
                            hitEffect = WHFx.lightningHitSmall;
                            maxRange = 320f;
                            damage = 200f;
                        }};
                    }

                    @Override
                    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
                        if(!(mount.target instanceof Bullet)) return;

                        Bullet target = (Bullet)mount.target;
                        if(target.damage() > bullet.damage){
                            target.damage(target.damage() - bullet.damage);
                        }else{
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

                weapons.add(new Weapon(name + "-weapon1"){{
                    reload = 240f;
                    shootY = -218 / 4f;
                    shootX = 224 / 4f;
                    x = y = 0;
                    mirror = rotate = false;
                    shootCone = 175f;
                    shoot = new ShootBarrel(){{
                        barrels = new float[]
                        {
                        0, 20f, -90f,
                        0, 10f, -90f,
                        0, 0, -90f,
                        0, -10f, -90f,
                        0, -20f, -90f
                        };
                        shots = 20;
                        shotDelay = 5;
                    }};

                    velocityRnd = 0.05f;
                    shootSound = WHSounds.rocket;

                    bullet = new CritMissileBulletType(7f, 90, name("large-missile")){
                        {
                            width = 20;
                            height = width * 1.5f;
                            shrinkY = 0f;
                            frontColor = WHPal.WHYellow2;
                            trailColor = backColor = hitColor = lightColor = lightningColor = WHPal.WHYellow;

                            homingPower = 0.08f;
                            homingDelay = 5f;
                            followAimSpeed = 6;

                            Color c = WHPal.WHYellow.cpy().lerp(Color.orange, 0.25f);
                            colors = new Color[]{c.a(0.55f), c.a(0.7f), c.a(0.8f), c, Color.white};

                            lifetime = 600 / speed;
                            hitEffect = WHFx.instHit(backColor, true, 2, 30f);
                            despawnEffect = WHFx.generalExplosion(10, hitColor, 24, 3, false);
                            smokeEffect = Fx.shootPyraFlame;
                            lightning = 2;
                            lightningLengthRand = 8;
                            lightningLength = 8;
                            lightningDamage = 35;

                            shieldDamageMultiplier = 1.5f;

                            trailWidth = 2f;
                            trailLength = 8;
                        }
                    };

                }});

                Weapon revengeMissile2 = new Weapon(name("small-cannon-3")){
                    {
                        reload = 240;
                        recoil = 3;
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 2;
                        shootY = 52 / 4f;
                        shootSound = WHSounds.missileShoot2;
                        shoot = new ShootAlternate(28 / 4f){
                            {
                                shots = 2;
                                shotDelay = 20;
                            }
                        };
                        bullet = new MultiTrailBulletType(8, 500, name("energy-bullet")){{

                            lifetime = 350 / speed;

                            width = 12;
                            height = width * 2.5f;
                            hitSize = 20f;

                            trailLength = 16;
                            trailWidth = 3;

                            keepVelocity = false;
                            absorbable = false;
                            trailColor = frontColor = backColor = lightColor = hitColor = lightningColor = WHPal.WHYellow;

                            splashDamageRadius = 56;
                            splashDamage = damage / 3;

                            buildingDamageMultiplier = 1.7f;
                            shieldDamageMultiplier = 0.7f;

                            shootEffect = WHFx.shootLine(30, 30);

                            hitEffect = new MultiEffect(
                            WHFx.trailHitSpark(hitColor, 30f, 5, splashDamageRadius, 2f, 10f),
                            WHFx.generalExplosion(10, hitColor, splashDamageRadius, 10, false));
                            despawnEffect = new MultiEffect(
                            WHFx.circleOut(hitColor, 30, splashDamageRadius),
                            WHFx.crossBlastArrow45(hitColor, hitColor, 60, 5, 40, splashDamageRadius * 0.5f, splashDamageRadius)
                            );

                            intervalBullets = 2;
                            intervalDelay = 5;
                            intervalBullet = new CritBulletType(3, 90){{
                                lightColor = backColor = trailColor = hitColor = WHPal.WHYellow;
                                lifetime = 30;
                                width = 8;
                                height = width * 2.5f;

                                rotateSpeed = 2;

                                hitEffect = despawnEffect = WHFx.generalExplosion(20, hitColor, 30, 3, true);

                                frontColor = Color.white;
                                smokeEffect = Fx.shootBigSmoke2;
                                despawnShake = 7f;
                                lightRadius = 30f;
                                lightOpacity = 0.5f;

                                trailLength = 6;
                                trailWidth = 3;
                                trailEffect = Fx.none;
                            }};

                            fragBullets = 6;
                            fragRandomSpread = 0;
                            fragLifeMax = fragLifeMin = 1;
                            fragVelocityMax = fragVelocityMin = 1;
                            fragSpread = 60;
                            fragOnAbsorb = false;
                            fragBullet = new CritBulletType(3.5f, 120){
                                {
                                    splashDamageRadius = 40f;
                                    splashDamage = damage;
                                    lightColor = backColor = trailColor = hitColor = WHPal.WHYellow;

                                    hitEffect = new MultiEffect(
                                    WHFx.hitSpark(hitColor, 30, 10, splashDamageRadius, 1.5f, 10),
                                    WHFx.circleOut(hitColor, 30, splashDamageRadius));
                                    despawnEffect = WHFx.instRotation(hitColor, 30, splashDamageRadius, 45, false);
                                    knockback = 0.8f;
                                    lifetime = 120 / speed;

                                    width = 12;
                                    height = width * 2.5f;

                                    rotateSpeed = 3;

                                    frontColor = Color.white;
                                    smokeEffect = Fx.shootBigSmoke2;
                                    lightRadius = 30f;
                                    lightOpacity = 0.5f;

                                    trailLength = 9;
                                    trailWidth = 2;
                                    trailEffect = Fx.none;
                                }

                                @Override
                                public void createFrags(Bullet b, float x, float y){
                                    if(fragBullet != null && (fragOnAbsorb || !b.absorbed) && !(b.frags >= pierceFragCap && pierceFragCap > 0)){
                                        for(int i = 0; i < fragBullets; i++){
                                            float len = Mathf.random(fragOffsetMin, fragOffsetMax);
                                            float a = b.rotation() + Mathf.randomSeed(b.id, 180) + fragAngle + fragSpread * i - (fragBullets - 1) * fragSpread / 2f;
                                            fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                                        }
                                        b.frags++;
                                    }
                                }
                            };

                        }};
                    }
                };

                weapons.add(new LaserPointDefenseWeapon(name("machine-gun-1")){{
                    x = -24 / 4f;
                    y = 442 / 4f;
                    mirror = false;
                    rotate = true;
                    rotateSpeed = 5;
                    shootY = 23 / 4f;
                    damage = 1000 / 60f;
                    laserWidth = 2;
                    color = WHPal.WHYellow;
                    useTeamColor = false;

                    bullet = new BulletType(){{
                        damage = 4;
                        maxRange = 400;
                        collidesGround = false;
                    }};
                }});

                weapons.add(new Weapon(name + "-weapon3"){
                    {
                        layerOffset = 0.01f;
                        reload = 300;

                        x = 136f / 4f;
                        y = -63 / 4f;

                        mirror = false;
                        rotate = true;
                        rotateSpeed = 1;
                        shootSound = Sounds.shootTank;
                        ejectEffect = Fx.casing3Double;
                        parts.add(new RegionPart("-barrel"){
                            {
                                progress = PartProgress.recoil;
                                mirror = false;
                                under = true;
                                x = y = 0f;
                                moveY = -8;
                            }
                        });
                        shoot = new ShootAlternate(){
                            {
                                barrels = 2;
                                spread = 40 / 4f;
                                shots = 4;
                                shotDelay = 10;
                            }
                        };

                        shootY = 64 / 4f;
                        recoil = 1.5F;

                        bullet = new TrailFadeBulletType(8, 600, name("pierce")){
                            {
                                tracerUpdateSpacing = 0.5f;
                                hitBlinkTrail = false;
                                despawnBlinkTrail = false;
                                tracers = 2;

                                buildingDamageMultiplier = 0.75f;
                                shieldDamageMultiplier = 1.25f;

                                lifetime = 450 / speed;

                                width = 18;
                                height = width * 2.5f;
                                hitSize = 20f;

                                trailLength = 12;
                                trailWidth = 4;

                                keepVelocity = false;
                                absorbable = false;
                                pierce = true;
                                trailColor = frontColor = backColor = lightColor = hitColor = lightningColor = WHPal.WHYellow;
                                pierceArmor = true;
                                pierceBuilding = true;
                                pierceCap = 2;

                                splashDamageRadius = 64;
                                splashDamage = damage / 3;

                                shootEffect = WHFx.instShoot(hitColor, hitColor);

                                hitEffect = new MultiEffect(
                                WHFx.trailHitSpark(hitColor, 30f, 5, splashDamageRadius, 2f, 10f),
                                WHFx.generalExplosion(10, hitColor, splashDamageRadius, 10, false));
                                despawnEffect = new MultiEffect(
                                WHFx.circleOut(hitColor, 30, splashDamageRadius),
                                WHFx.crossBlastArrow45(hitColor, hitColor, 60, 5, 40, splashDamageRadius * 0.5f, splashDamageRadius)
                                );

                                fragOnHit = false;
                                fragBullets = 3;
                                fragBullet = new CritBulletType(3.4f, 70, "missile-large"){{
                                    collides = false;
                                    collidesTiles = false;
                                    splashDamageRadius = 40f;
                                    splashDamage = 100f;
                                    lightColor = backColor = trailColor = hitColor = WHPal.WHYellow;
                                    drag = 0.02f;
                                    hitEffect = WHFx.circleOut(hitColor, 30, splashDamageRadius);
                                    despawnEffect = Fx.scatheSlash;
                                    knockback = 0.8f;
                                    lifetime = 23f;
                                    width = height = 15;

                                    frontColor = Color.white;
                                    smokeEffect = Fx.shootBigSmoke2;
                                    despawnShake = 7f;
                                    lightRadius = 30f;
                                    lightOpacity = 0.5f;

                                    trailLength = 20;
                                    trailWidth = 3.5f;
                                    trailEffect = Fx.none;
                                }};

                            }
                        };
                    }
                });

                Weapon revengeMissile = new Weapon(name("missile-launcher")){
                    {
                        reload = 90;
                        rotate = true;
                        mirror = false;
                        rotateSpeed = 1.5f;
                        shootSound = WHSounds.missileShoot;
                        inaccuracy = 6;
                        alternate = true;
                        shoot = new ShootAlternate(){{
                            barrels = 3;
                            spread = 17 / 4f;
                            shots = 3;
                            shotDelay = 4f;
                        }};
                        bullet = new CritMissileBulletType(6, 90, name("rocket")){
                            {
                                hittable = false;
                                pierceArmor = true;
                                width = 10;
                                height = width * 2.2f;
                                shrinkX = shrinkY = 0f;

                                splashDamage = damage;
                                splashDamageRadius = 30;

                                lengthOffset = width - 2;
                                flameWidth = 2.5f;
                                flameLength = 15f;

                                weaveMagOnce = true;
                                weaveMag = 0.4f;
                                weaveRandom = true;
                                drawMissile = true;
                                lightningColor = hitColor = trailColor = WHPal.WHYellow;

                                Color c = WHPal.WHYellow.cpy().lerp(Color.orange, 0.25f);
                                colors = new Color[]{c.a(0.55f), c.a(0.7f), c.a(0.8f), c, Color.white};

                                lifetime = 380 / speed;
                                damage = 80;
                                trailLength = 6;
                                trailWidth = 1.5f;
                                statusDuration = 60;
                                homingDelay = lifetime / 2f;
                                homingPower = 0.01f;
                                followAimSpeed = 2f;

                                despawnEffect = hitEffect = new MultiEffect(
                                WHFx.generalExplosion(10, hitColor, splashDamageRadius, 5, false),
                                WHFx.instHit(hitColor, true, 2, 30f)
                                );
                                smokeEffect = hugeSmoke;
                                shootEffect = shootBigColor;
                                lightningDamage = 40;
                                lightning = 2;
                                lightningLength = 12;
                            }
                        };
                    }
                };

                Weapon revengeLaser = new Weapon(name("small-cannon")){
                    {

                        shootY = 64 / 4f;
                        reload = 40;
                        rotate = true;
                        mirror = false;
                        rotateSpeed = 1.5F;
                        shootSound = shootMissile;
                        recoil = 2.0F;
                        shoot = new ShootAlternate(){
                            {
                                spread = 30 / 4f;
                                shots = 2;
                                shotDelay = 10;
                                firstShotDelay = 15f;
                            }
                        };

                        parentizeEffects = true;

                        bullet = new PositionLightningBulletType(300){
                            {
                                float r = maxRange = 275;
                                shootSound = shootLaser;
                                hitColor = lightColor = lightningColor = WHPal.WHYellow;
                                shootEffect = WHFx.shootLine(20, 20);
                                hitEffect = WHFx.square(hitColor, 40, 10, 40, 6);
                                spawnBullets.add(new LaserBulletType(250){
                                    {
                                        sideWidth = 2;
                                        sideLength = 20;
                                        Color c = WHPal.WHYellow.cpy().lerp(Color.orange, 0.1f);
                                        colors = new Color[]{c.a(0.3f), c.a(0.5f), c.a(0.7f), c, WHPal.WHYellow2};
                                        width = 12;
                                        length = r;
                                    }
                                });
                            }
                        };
                    }
                };

                weapons.addAll(
                copyAndMove(revengeMissile, 107 / 4f, 128 / 4f),
                copyAndMove(revengeMissile, -120 / 4f, -108 / 4f),
                copyAndMove(revengeMissile2, -59 / 4f, -13 / 4f),
                copyAndMove(revengeMissile2, -59 / 4f, 119 / 4f),
                copyAndMove(revengeLaser, -144 / 4f, -252 / 4f),
                copyAndMove(revengeLaser, 69 / 4f, -345 / 4f)

                );
            }
        };


        tankAG = new SuperHeavyUnitType("tankAG"){
            {
                constructor = ElevationMoveUnit::create;

                health = 210 * 1000f;
                speed = 0.8f;
                hitSize = 80;
                armor = 40;
                rotateSpeed = 0.6f;

                engineOffset = 18;
                engineSize = 7;

                lowAltitude = true;
                hovering = true;
                canDrown = false;
                squareShape = true;
                faceTarget = false;

                useEngineElevation = false;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                moveSound = Sounds.loopExtract;
                moveSoundVolume = 0.25f;
                moveSoundPitchMin = 0.7f;
                moveSoundPitchMax = 1.5f;

                abilities.add(
                new EllipseForceFieldAbility(80, 120, 15, 8000, 60 * 60, 0.3f, 30){{
                    Regen = true;
                }});
                abilities.add(new ShieldRegenFieldAbility(500, 5000, 180, 220));
                abilities.add(new AdaptedHealAbility(1000, 180, 220, WHPal.ShootOrangeLight));
                abilities.add(
                new ShockAbility(){{
                    range = 300;
                    reload = 60 * 5;
                    bulletDamage = 1000;
                }});

                for(float f : new float[]{0, 1}){
                    abilities.add(
                    new MoveEffectAbility(){{
                        minVelocity = f;
                        interval = 12 - f * 2;
                        parentizeEffects = true;
                        effect = new MultiEffect(
                        new ParticleEffect(){{
                            particles = 2;
                            sizeFrom = 16;
                            lifetime = 30;
                            sizeInterp = Interp.pow5In;
                            interp = Interp.pow5Out;
                            length = 70;
                            layer = 60;
                            colorFrom = Pal.gray.cpy().a(0.8f);
                            colorTo = Color.white.cpy().a(0.1f);
                        }},
                        new ParticleEffect(){{
                            particles = 3;
                            sizeFrom = 20;
                            lifetime = 30;
                            sizeInterp = Interp.pow5In;
                            interp = Interp.pow5Out;
                            length = 60;
                            layer = 60;
                            colorFrom = Pal.gray.cpy().a(0.8f);
                            colorTo = Color.white.cpy().a(0.1f);
                        }}
                        );
                    }});
                }
                parts.addAll(new HoverPart(){
                    {
                        layerOffset = -0.01f;
                        mirror = true;
                        x = 109 / 4f;
                        y = 229 / 4f;
                        radius = 35;
                        color = WHPal.ShootOrange;
                        phase = 100;
                        stroke = 6;
                    }
                });

                parts.addAll(new HoverPart(){
                    {
                        layerOffset = -0.01f;
                        mirror = true;
                        x = 168 / 4f;
                        y = -232 / 4f;
                        radius = 35;
                        color = WHPal.ShootOrange;
                        phase = 100;
                        stroke = 6;
                    }
                });

                for(int i = 0; i < 15; i++){
                    AncientEngine.addEngine(this, 67 / 4f + i * 2 / 4f, -270 / 4f, 0, 2, true);
                    AncientEngine.addEngine(this, 146 / 4f, 33 / 4f + i * 2 / 4f, 90, 2, true);
                }

                weapons.add(new Weapon(name("tankAG-doom-laser-weapon")){

                    {
                        reload = 2000;
                        x = y = 0;
                        rotate = true;
                        mirror = false;
                        inaccuracy = 0.0F;

                        shootWarmupSpeed = 0.008f;
                        minWarmup = 0.98f;

                        heatColor = WHPal.ShootOrange.cpy();

                        bullet = new DoomLaserBulletType(){{
                            lifetime = 500;
                            damage = 800;
                            splashDamage = 250;
                            damageInterval = 6;

                            buildingDamageMultiplier = 0.5f;

                            width = 40;
                            splashDamageRadius = width * 1.3f;
                            fixedAngle = 110;
                            sideAngle = 80f;
                            sideLength = 100;
                            triSize = 100;

                            speed = 0.45f;

                            trailLength = 20;

                            homingDelay = 120;
                            homingPower = 0.1f;

                            shake = 10;

                            Color c = trailColor = lightningColor = hitColor = WHPal.ShootOrange.cpy();
                            colors = new Color[]{c.a(0.4f), c.a(0.7f), c, Pal.coalBlack};

                            shootEffect = new MultiEffect(
                            WHFx.generalExplosion(120, hitColor, splashDamageRadius, 20, true),
                            WHFx.trailCircleHitSpark(hitColor, 120, 10, splashDamageRadius, 2, 15)
                            );

                            despawnEffect = hitEffect = new MultiEffect(
                            WHFx.hitCircle(hitColor, Color.lightGray, 15, 6, 80, 8),
                            WHFx.hitSpark(hitColor, 20, 6, 80, 1.5f, 10)
                            );
                        }};

                    }

                    @Override
                    public float range(){
                        return 300;
                    }

                    @Override
                    public void update(Unit unit, WeaponMount mount){
                        super.update(unit, mount);

                        boolean can = unit.canShoot();
                        float warmupTarget = (can && mount.shoot && mount.reload <= 0.0001f) ? 1f : 0f;
                        if(linearWarmup){
                            mount.warmup = Mathf.approachDelta(mount.warmup, warmupTarget, shootWarmupSpeed);
                        }else{
                            mount.warmup = Mathf.lerpDelta(mount.warmup, warmupTarget, shootWarmupSpeed);
                        }
                    }

                    @Override
                    protected void bullet(Unit unit, WeaponMount mount, float xOffset, float yOffset, float angleOffset, Mover mover){
                        if(!unit.isAdded()) return;

                        Tmp.v6.set(mount.aimX, mount.aimY).sub(unit);
                        Tmp.v1.set(mount.aimX, mount.aimY).sub(unit).nor().scl(Math.min(Tmp.v6.len(), range())).add(unit);

                        mount.charging = false;
                        float
                        bulletX = Tmp.v1.x,
                        bulletY = Tmp.v1.y,
                        shootAngle = bulletRotation(unit, mount, bulletX, bulletY) + angleOffset,
                        lifeScl = bullet.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY) / bullet.range) : 1f,
                        angle = shootAngle + Mathf.range(inaccuracy + bullet.inaccuracy);

                        Entityc shooter = unit.controller() instanceof MissileAI ai ? ai.shooter : unit; //Pass the missile's shooter down to its bullets
                        mount.bullet = bullet.create(unit, shooter, unit.team, bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd) + extraVelocity, lifeScl, null, mover, mount.aimX, mount.aimY, mount.target);
                        handleBullet(unit, mount, mount.bullet);

                        if(!continuous){
                            shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax), shootSoundVolume);
                        }else{
                            initialShootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax), shootSoundVolume);
                        }

                        bullet.shootEffect.at(bulletX, bulletY, angle, bullet.hitColor, unit);
                        bullet.smokeEffect.at(bulletX, bulletY, angle, bullet.hitColor, unit);

                        unit.vel.add(Tmp.v1.trns(shootAngle + 180f, bullet.recoil));
                        Effect.shake(shake, shake, bulletX, bulletY);
                        mount.recoil = 1f;
                        if(recoils > 0){
                            mount.recoils[mount.barrelCounter % recoils] = 1f;
                        }
                        mount.heat = 1f;
                    }

                    @Override
                    public void draw(Unit unit, WeaponMount mount){
                        float z = Draw.z();
                        Tmp.v1.trns(unit.rotation, y);
                        float f = 1f - mount.reload / reload;
                        float rad = 10;
                        float f1 = Mathf.curve(f, 0.4F, 1.0F);
                        Draw.z(Layer.bullet);
                        Draw.color(heatColor);

                        rand.setSeed(unit.id);

                        float range = 60;
                        float in = 1 - mount.warmup;

                        Tmp.v6.set(mount.aimX, mount.aimY).sub(unit);
                        Tmp.v2.set(mount.aimX, mount.aimY).sub(unit).nor().scl(Math.min(Tmp.v6.len(), range())).add(unit);
                        float angle1 = unit.angleTo(mount.aimX, mount.aimY) - 180 + rand.random(-20, 20);

                        Lines.stroke((2f + Mathf.absin(Time.time + 4.0F, 8.0F, 1.5F)) * f1 * mount.warmup, heatColor);

                        if(mount.reload <= 0.0001f){
                            for(int i = 1; i < 4; i++){
                                float cur = Mathf.clamp(1 - Interp.smooth.apply(Mathf.curve(3 * mount.warmup, i - 1, i)));
                                Tmp.v3.trns(angle1, (range * 3 * cur)).add(Tmp.v2);
                                float a = rand.random(0, 90), b = rand.random(20, 40);
                                float radius = (rad * 3 + (i / 3f) * 0.2f * range + i * 0.2f * range);
                                arc(Tmp.v3.x, Tmp.v3.y, radius, (b + i * 10) / 360f, a + Time.time % 360 * i);
                                arc(Tmp.v3.x, Tmp.v3.y, radius, (b + i * 10) / 360f, a - Time.time % 360 * i + 180);
                            }
                        }

                        for(int i = 0; i < 8; i++){
                            float angle = i * 360 / 8f + 360 * mount.warmup * f1;
                            Tmp.v4.trns(angle, (rad * 2.5f + 3 * Mathf.sin(Time.time, 0.3f) * f1) * mount.warmup + range * in)
                            .add(Tmp.v2);
                            Lines.lineAngle(Tmp.v4.x, Tmp.v4.y, angle, 7 * Mathf.absin(Time.time, 5 + i, 1));
                        }

                        Lines.stroke((2f + Mathf.absin(Time.time + 4.0F, 8.0F, 1.5F)) * f1, heatColor);

                        arcProcessFlip(Tmp.v2.x, Tmp.v2.y, rad * f1 + 5 * Mathf.sin(Time.time, 0.3f), Time.time, 20);

                        for(int i = 0; i < 4; i++){
                            float angle = i * 90 + Time.time % 360;
                            Tmp.v3.trns(angle, rad + 3 * Mathf.sin(Time.time, 0.3f) * f1 * mount.warmup).add(Tmp.v2);
                            Lines.lineAngle(Tmp.v3.x, Tmp.v3.y, angle, 8);
                        }

                        Draw.color(heatColor);
                        Draw.z(z);
                    }

                });

                weapons.add(new Weapon(name("small-cannon")){
                                {
                                    x = 140 / 4f;
                                    y = 117 / 4f;

                                    shootY = 64 / 4f;
                                    reload = 40;
                                    rotate = true;
                                    mirror = true;
                                    rotateSpeed = 1.5F;
                                    shootSound = shootMissile;
                                    recoil = 2.0F;
                                    shoot = new ShootAlternate(){
                                        {
                                            spread = 30 / 4f;
                                            shots = 2;
                                            shotDelay = 10;
                                            firstShotDelay = 15f;
                                        }
                                    };

                                    parentizeEffects = true;

                                    bullet = new LightingLaserBulletType(200){{
                                        recoil = 0.15f;
                                        Color color = hitColor = lightningColor = WHPal.ShootOrange.cpy();
                                        chargeEffect = WHFx.genericCharge(hitColor, 5, 30, shoot.firstShotDelay).followParent(true);
                                        lifetime = 30;
                                        length = 350;
                                        width = 25;
                                        pierceCap = 5;
                                        sideAngle = 90;
                                        sideLength = 15;
                                        colors = new Color[]{color.a(0.4f), color.a(0.7f), color, Color.white};
                                    }};
                                }

                                @Override
                                protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
                                    unit.apply(shootStatus, shootStatusDuration);

                                    if(shoot.firstShotDelay > 0){
                                        mount.charging = true;
                                        shoot.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                                            if(delay > 0f){
                                                Time.run(delay, () -> {
                                                    chargeSound.at(xOffset + shootX, yOffset + shootY, Mathf.random(soundPitchMin, soundPitchMax));
                                                    bullet.chargeEffect.at(xOffset + shootX, yOffset + shootY, rotation, unit);
                                                });
                                            }
                                        });
                                    }

                                    shoot.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                                        //this is incremented immediately, as it is used for total bullet creation amount detection
                                        mount.totalShots++;
                                        int barrel = mount.barrelCounter;
                                        if(delay > 0f){
                                            Time.run(delay, () -> {
                                                //hack: make sure the barrel is the same as what it was when the bullet was queued to fire
                                                int prev = mount.barrelCounter;
                                                mount.barrelCounter = barrel;
                                                bullet(unit, mount, xOffset, yOffset, angle, mover);
                                                mount.barrelCounter = prev;
                                            });
                                        }else{
                                            bullet(unit, mount, xOffset, yOffset, angle, mover);
                                        }
                                    }, () -> mount.barrelCounter++);
                                }
                            }
                );

                weapons.add(new Weapon(name("tankAG-weapon1")){
                    {
                        reload = 90;
                        recoil = 2;
                        x = 167 / 4f;
                        y = -106 / 4f;
                        shootY = 24 / 4f;

                        mirror = true;
                        rotateSpeed = 1;
                        rotate = true;

                        shootSound = shootLaser;

                        bullet = new MultiTrailBulletType(8, 500, name("pierce")){{

                            width = 12;
                            height = width * 2.5f;

                            lifetime = 420 / speed;//360
                            keepVelocity = false;
                            splashDamage = damage;
                            splashDamageRadius = 64f;
                            frontColor = WHPal.ShootOrangeLight;
                            lightningColor = lightColor = hitColor = trailColor = backColor = WHPal.ShootOrange;

                            lightning = 3;
                            lightningLength = 6;
                            lightningLengthRand = 8;
                            lightningDamage = 35f;

                            hittable = false;

                            offset = 1;
                            subTrails = 3;
                            trailLength = 12;
                            subTrailWidth = 1.5f;
                            trailWidth = width / 3f;

                            shootEffect = new MultiEffect(
                            shootBigColor,
                            WHFx.linePolyOut(hitColor, 30, 4, 2, 6, 0),
                            linePolyOut(hitColor, 15, 40, 2, 6, 0).startDelay(15f)
                            );

                            hitEffect = new MultiEffect(
                            WHFx.trailCircleHitSpark(hitColor, 30, 12, 70, 1.5f, 10f),
                            WHFx.hitPoly(hitColor, hitColor, 30, 10, 64, 5, 6, 45),
                            WHFx.generalExplosion(10, hitColor, splashDamageRadius * 0.45f, 6, true),
                            WHFx.instHit(hitColor, true, 3, splashDamageRadius)
                            );

                            despawnEffect = new MultiEffect(
                            WHFx.instRotation(hitColor, 60, splashDamageRadius * 1.5f, 45, true),
                            WHFx.generalExplosion(20, hitColor, splashDamageRadius, 6, false)
                            );

                            fragBullets = 5;
                            fragBullet = new CritBulletType(3, 150){{
                                width = height = 14;
                                shrinkX = shrinkY = 1;
                                trailLength = 5;
                                trailWidth = 2;
                                drag = 0.02f;
                                lifetime = 32;
                                splashDamage = damage;
                                splashDamageRadius = 40;
                                frontColor = lightningColor = lightColor = hitColor = trailColor = backColor = WHPal.ShootOrange;

                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.generalExplosion(10, hitColor, splashDamageRadius, 5, false),
                                WHFx.instRotation(hitColor, 30, splashDamageRadius * 0.8f, 90, false),
                                WHFx.hitPoly(hitColor, hitColor, 30, 5, splashDamageRadius, 4, 5, 45),
                                WHFx.trailCircleHitSpark(hitColor, 30, 5, splashDamageRadius, 1f, 6)
                                );
                            }};

                        }};

                    }
                });

                weapons.add(new Weapon(name("tankAG-weapon2")){
                    {
                        reload = 60;
                        recoil = 2;
                        x = 0;
                        y = 115 / 4f;
                        shootY = 77 / 4f;

                        shoot = new ShootAlternate(){
                            {
                                barrels = 2;
                                spread = 46 / 4f;
                                shots = 8;
                                shotDelay = 4;
                            }
                        };

                        mirror = false;
                        rotateSpeed = 2;
                        rotate = true;
                        inaccuracy = 3;

                        shootSound = shootSpectre;
                        ejectEffect = Fx.casing4;

                        bullet = new CritBulletType(10, 120){
                            {
                                width = 12;
                                height = width * 3;
                                spin = 2;

                                trailLength = 5;
                                trailWidth = 2.7f;

                                pierceCap = 2;
                                splashDamage = damage;
                                splashDamageRadius = 32;
                                lifetime = 320 / speed;
                                hitSound = explosion;
                                shootEffect = shootTitan;
                                smokeEffect = shootBigSmoke;
                                frontColor = WHPal.ShootOrangeLight;
                                hitColor = backColor = lightColor = trailColor = WHPal.ShootOrange;

                                hitEffect = new MultiEffect(
                                WHFx.trailHitSpark(hitColor, 30f, 4, 40, 1.5f, 10f)
                                );
                                despawnEffect = new MultiEffect(
                                WHFx.instHit(hitColor, true, 2, 40),
                                WHFx.generalExplosion(10, hitColor, 30, 10, true));
                            }
                        };
                    }
                });

                weapons.add(new Weapon(name("tankAG-weapon3")){
                    {
                        reload = 15;
                        recoil = 2;
                        recoilTime = 12;
                        mirror = false;
                        x = 0;
                        y = -114 / 4f;

                        shootY = 218 / 4f;

                        rotate = true;
                        rotateSpeed = 0.7f;
                        shootCone = 15;
                        xRand = 0.3f;
                        inaccuracy = 2;
                        shootSound = shootSmite;
                        shake = 1.5f;
                        layerOffset = 0.02F;
                        recoils = 2;
                        cooldownTime = 120;
                        shoot = new ShootAlternate(){{
                            barrels = 2;
                            spread = 106 / 4f;
                        }};

                        ejectEffect = none;
                        velocityRnd = 0.06f;
                        parts.add(new RegionPart("-barrel-l"){
                            {
                                moveY = -80 / 4f;
                                under = true;
                                recoilIndex = 0;
                                progress = heatProgress = PartProgress.recoil;
                            }
                        }, new RegionPart("-barrel-r"){
                            {
                                moveY = -80 / 4f;
                                recoilIndex = 1;
                                under = true;
                                progress = heatProgress = PartProgress.recoil;
                            }
                        });
                        bullet = new TrailFadeBulletType(20, 600, name("pierce")){
                            {
                                tracerUpdateSpacing = 0.3f;
                                hitBlinkTrail = false;
                                despawnBlinkTrail = false;
                                tracers = 1;

                                buildingDamageMultiplier = 0.75f;

                                lifetime = 520 / speed;//500

                                width = 18;
                                height = width * 2.5f;
                                hitSize = 20f;

                                trailLength = 12;
                                trailWidth = 4;

                                keepVelocity = false;
                                absorbable = false;
                                pierce = true;
                                trailColor = frontColor = backColor = lightColor = hitColor = lightningColor = WHPal.ShootOrange;
                                pierceArmor = true;
                                pierceBuilding = true;
                                pierceCap = 3;

                                splashDamageRadius = 56;
                                splashDamage = damage / 3;

                                shootEffect = WHFx.instShoot(hitColor, hitColor);

                                hitEffect = new MultiEffect(
                                WHFx.trailHitSpark(hitColor, 30f, 5, splashDamageRadius, 2f, 10f),
                                WHFx.generalExplosion(10, hitColor, splashDamageRadius, 10, false),
                                WHFx.sharpBlast(WHPal.ShootOrange, WHPal.ShootOrange, 30, splashDamageRadius * 0.7f));
                                despawnEffect = new MultiEffect(
                                WHFx.circleOut(hitColor, 30, splashDamageRadius),
                                WHFx.instRotation(hitColor, 30, splashDamageRadius + 20f, 45, false)
                                );

                            }
                        };
                    }
                });
            }
        };

        tankEn2 = new TankEn2UnitType("tankEn2"){
            @Override
            public void init(){
                super.init();
                naval = false;
                initPathType();
            }

            public void initPathType(){
                flowfieldPathType =
                Pathfinder.costHover;
                pathCost = ControlPathfinder.costHover;
                pathCostId = ControlPathfinder.costTypes.indexOf(pathCost);
            }

            {
                constructor = () -> new TankEn2Unit(){{
                    LandSpeedMultiplier = 1f;
                }};

                pickupUnits = true;
                hovering = true;
                squareShape = true;

                payloadCapacity = (3 * 3) * tilePayload;

                hitSize = 40;
                speed = 1f;
                rotateSpeed = 1.3f;
                health = 20000;
                armor = 24;
                itemCapacity = 0;
                floorMultiplier = 0.6f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new PowerAmmoType(4000);

                parts.addAll(
                new HoverPart(){{
                    x = -71 / 4f;
                    y = 104f / 4f;
                    radius = 80 / 4f;
                    stroke = 5;
                    color = Pal.slagOrange;
                    layerOffset = -0.001f;
                    mirror = true;
                    rotation = 45f;
                }},
                new HoverPart(){{
                    x = -71 / 4f;
                    y = -104f / 4f;
                    radius = 80 / 4f;
                    color = Pal.slagOrange;
                    stroke = 5;
                    layerOffset = -0.001f;
                    mirror = true;
                    rotation = 45f;
                }}
                );

                abilities.add(new EnergyFieldAbility(70, 90, 200){{
                    statusDuration = 60f * 6f;
                    maxTargets = 5;
                    healPercent = 0.5f;
                    color = WHPal.ShootOrange;
                    layer = Layer.groundUnit - 1f;
                    sameTypeHealMult = 0.5f;
                    effectRadius = 25;
                }});

                weapons.addAll(
                new Weapon(name("tankEn2-weapon1")){
                    {
                        layerOffset = 0.04f;
                        y = -12 / 4f;
                        x = 0;
                        reload = 150;
                        mirror = false;
                        recoil = 2f;
                        recoilTime = 60;
                        shootSound = shootLaser;
                        heatColor = WHPal.thurmixRed;

                        rotate = true;
                        rotateSpeed = 1.3f;

                        shootY = 163 / 4f;
                        shootX = 15 / 4f;

                        shoot.firstShotDelay = 30;

                        parts.addAll(
                        new RegionPart("-barrel"){{
                            under = true;
                            layerOffset = -0.001f;
                            x = 15 / 4f;
                            y = 107 / 4f;
                            heatProgress = progress = PartProgress.recoil;
                            heatColor = WHPal.thurmixRed;
                            moveY = -30 / 4f;
                        }});

                        parentizeEffects = true;

                        bullet = new CritBulletType(12, 1500){
                            {
                                pierceArmor = true;
                                lifetime = 380 / speed;
                                keepVelocity = false;
                                splashDamage = damage / 4f;
                                splashDamageRadius = 32;
                                pierceDamageFactor = 0.33f;
                                reflectable = absorbable = false;
                                width = 4;
                                height = 100;

                                shrinkX = shrinkY = 0;
                                frontColor = WHPal.SkyBlueF;
                                trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.SkyBlue;

                                chargeEffect = new MultiEffect(
                                WHFx.trailCharge(hitColor, 10, 2, 50, 3, shoot.firstShotDelay),
                                WHFx.genericChargeCircle(hitColor, 10, 50, shoot.firstShotDelay)
                                );

                                shootEffect = new MultiEffect(
                                WHFx.lineCircleOut(hitColor, 30, 30, 2),
                                WHFx.shootLine(10, 20),
                                Fx.shootBig);
                                smokeEffect = Fx.hitLaserColor;
                                trailChance = 0.5f;
                                trailEffect = WHFx.hitPoly(hitColor, hitColor, 30, 1, 15, 4, 6, 60);

                                hitEffect = new MultiEffect(
                                WHFx.instHit(hitColor, true, 2, splashDamageRadius),
                                WHFx.square(hitColor, 30, 10, splashDamageRadius, 6),
                                WHFx.lineCircleOut(hitColor, 30, splashDamageRadius, 2),
                                WHFx.hitSpark(hitColor, 30f, 10, splashDamageRadius, 1.4f, 10f)
                                );
                                despawnEffect = new MultiEffect(
                                WHFx.lineCircleOut(hitColor, 30, 60, 2),
                                WHFx.generalExplosion(12, hitColor, splashDamageRadius * 1.5f, 8, false),
                                WHFx.trailCircleHitSpark(hitColor, 90, 10, splashDamageRadius * 1.5f, 1.7f, 12)
                                );
                                hitSound = Sounds.none;
                            }

                            @Override
                            public void draw(Bullet b){
                                float inp = b.fin(Interp.slope);
                                Draw.color(backColor);
                                Lines.stroke(width);
                                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height * inp);
                                Draw.color(frontColor);
                                Lines.stroke(width);
                                Lines.lineAngleCenter(b.x, b.y, b.rotation(), (height / 2.5f) * inp);
                                Draw.reset();
                            }
                        };
                    }
                }
                );

                Weapon tankEn2Weapon2 = new Weapon(name("gun-mount")){
                    {
                        shootY = 22 / 4f;
                        reload = 90;
                        rotate = true;
                        rotateSpeed = 1.6f;
                        shootSound = shootTank;
                        recoil = 1;

                        mirror = true;

                        bullet = new BasicBulletType(8f, 120){{
                            sprite = "missile-large";
                            width = 9.5f;
                            height = 13f;
                            lifetime = 270 / speed;
                            hitSize = 6f;
                            shootEffect = Fx.shootTitan;
                            smokeEffect = Fx.shootSmokeTitan;
                            hitColor = backColor = trailColor = WHPal.ShootOrange;
                            frontColor = WHPal.ShootOrangeLight;
                            trailWidth = 3.1f;
                            trailLength = 8;
                            hitEffect = despawnEffect = Fx.blastExplosion;
                            splashDamageRadius = 20f;
                            splashDamage = 50f;

                            fragOnHit = false;
                            fragRandomSpread = 0f;
                            fragSpread = 10f;
                            fragBullets = 3;
                            fragVelocityMin = 1f;
                            despawnSound = explosionDull;

                            fragBullet = new CritBulletType(8f, 50){{
                                sprite = "missile-large";
                                width = 8f;
                                height = 12f;
                                lifetime = 15f;
                                hitSize = 4f;
                                hitColor = backColor = trailColor = WHPal.ShootOrange;
                                frontColor = WHPal.ShootOrangeLight;
                                trailWidth = 2.8f;
                                trailLength = 6;
                                hitEffect = despawnEffect = Fx.blastExplosion;
                                splashDamageRadius = 10f;
                                splashDamage = 20f;
                            }};
                        }};
                    }
                };

                weapons.addAll(
                copyAndMove(tankEn2Weapon2, 89 / 4f, -59 / 4f),
                copyAndMove(tankEn2Weapon2, 87 / 4f, 27 / 4f)
                );
            }
        };

        tankEn1 = new TankUnitType("tankEn1"){

            {
                constructor = TankUnit::create;

                hitSize = 40;
                speed = 1f;
                rotateSpeed = 1.5f;
                health = 20000;
                armor = 24;
                itemCapacity = 0;
                floorMultiplier = 0.6f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new PowerAmmoType(4000);

                float lx = 200 / 2f, ly = 250 / 2f;
                treadPullOffset = 8;
                treadFrames = 16;
                treadRects = new Rect[]{new Rect(11 - lx, 6 + 4 - ly, 51, 232)};

                researchCostMultiplier = 0.7f;

                abilities.addAll(new AccelerateReload(300, 60, 5, 150));

                weapons.addAll(
                new Weapon(name("tankEn1-weapon1")){{
                    layerOffset = 0.04f;
                    y = -19 / 4f;
                    x = 0;
                    reload = 30;
                    mirror = false;
                    ejectEffect = casing3Double;
                    recoil = 2f;
                    shootSound = WHSounds.machineGunShoot;
                    inaccuracy = 4f;
                    velocityRnd = 0.15f;
                    xRand = 0.3f;
                    recoils = 4;
                    heatColor = WHPal.thurmixRed;

                    rotate = true;
                    rotateSpeed = 2f;

                    shoot = new ShootAlternate(){{
                        barrels = 4;
                        spread = 8 / 4f;
                    }};

                    shootY = 120 / 4f;
                    shootX = 0;

                    float x1 = -14 / 4f, x2 = 0 / 4f, x3 = 14f / 4f,
                    move = 14f / 4f, y1 = 80 / 4f;

                    parts.addAll(
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.0012f;
                        x = x1;
                        y = y1;
                        heatProgress = progress = PartProgress.reload.curve(Interp.smooth).delay(0.2f);
                        heatColor = WHPal.thurmixRed;
                        moveX = move;
                        /*  moves.add(new PartMove(PartProgress.reload.mul(-0.5f).add(PartProgress.reload).mul(2), move, 0, 0));*/
                        recoilIndex = 0;
                    }},
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.001f;
                        x = x2;
                        y = y1;
                        heatProgress = progress = PartProgress.reload.curve(Interp.smooth).delay(0.1f);
                        heatColor = WHPal.thurmixRed;
                        moveX = move;
                        /* moves.add(new PartMove(PartProgress.reload.mul(-0.5f).add(PartProgress.reload).mul(2), move, 0, 0));*/
                        recoilIndex = 1;
                    }},
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.0014f;
                        x = x3;
                        y = y1;
                        heatProgress = progress = PartProgress.reload.curve(Interp.smooth).delay(0.2f);
                        heatColor = WHPal.thurmixRed;
                        moveX = -move;
                        /* moves.add(new PartMove(PartProgress.reload.mul(-0.5f).add(PartProgress.reload).mul(2),-2* move, 0, 0))*/
                        recoilIndex = 2;
                    }},
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.0013f;
                        x = x2;
                        y = y1;
                        heatProgress = progress = PartProgress.reload.curve(Interp.smooth).delay(0.1f);
                        heatColor = WHPal.thurmixRed;
                        moveX = -move;
                        /* moves.add(new PartMove(PartProgress.reload.mul(-0.5f).add(PartProgress.reload).mul(2),-2* move, 0, 0))*/
                        ;
                        recoilIndex = 3;
                    }}
                    );

                    bullet = new CritBulletType(16f, 140, name("pierce")){{
                        lifetime = 300 / speed;
                        keepVelocity = false;
                        splashDamageRadius = 40;
                        pierceCap = 3;
                        width = 10;
                        height = width * 3;
                        trailWidth = width / 3;
                        trailLength = 5;
                        shrinkX = shrinkY = 0;
                        frontColor = WHPal.ShootOrangeLight;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.ShootOrange;
                        mixColorFrom = mixColorTo = hitColor;
                        shootEffect = new MultiEffect(
                        WHFx.shootLine(10, 20),
                        Fx.shootBig);
                        smokeEffect = Fx.hitLaserColor;
                        trailChance = 0.5f;
                        trailEffect = WHFx.square(hitColor, 30, 2, 30, 4);
                        hitEffect = WHFx.square(hitColor, 35, 10, splashDamageRadius, 6);
                        despawnEffect = new MultiEffect(
                        WHFx.instHit(hitColor, true, 2, splashDamageRadius),
                        WHFx.generalExplosion(12, hitColor, 40, 8, false)
                        );
                        hitSound = Sounds.none;
                    }};
                }}
                );

                weapons.add(new Weapon(name("laser-weapon-1")){
                    {

                        y = 22 / 4f;
                        x = 73 / 4f;
                        reload = 80;
                        ejectEffect = Fx.none;
                        recoil = 2f;
                        shootSound = WHSounds.machineGunShoot;
                        shoot = new ShootAlternate(){{
                            shotDelay = 8;
                            shots = 2;
                            spread = 16f / 4f;
                        }};

                        rotate = true;
                        rotateSpeed = 1.5f;

                        shootY = 16 / 4f;

                        bullet = new RailBulletType(){{
                            length = 300;
                            damage = 80;
                            pointEffectSpace = 10f;
                            hitColor = WHPal.ShootOrange;
                            hitEffect = endEffect = Fx.hitBulletColor;
                            pierceDamageFactor = 0.95f;

                            smokeEffect = Fx.colorSpark;

                            endEffect = new Effect(14f, e -> {
                                color(e.color);
                                Drawf.tri(e.x, e.y, e.fout() * 1.5f, 5f, e.rotation);
                            });

                            shootEffect = new Effect(10, e -> {
                                color(e.color);
                                float w = 1.2f + 7 * e.fout();

                                Drawf.tri(e.x, e.y, w, 30f * e.fout(), e.rotation);
                                color(e.color);

                                for(int i : Mathf.signs){
                                    Drawf.tri(e.x, e.y, w * 0.9f, 18f * e.fout(), e.rotation + i * 90f);
                                }

                                Drawf.tri(e.x, e.y, w, 4f * e.fout(), e.rotation + 180f);
                            });

                            lineEffect = new Effect(20f, e -> {
                                if(!(e.data instanceof Vec2 v)) return;

                                color(e.color);
                                stroke(e.fout() * 0.9f + 1.2f);

                                Fx.rand.setSeed(e.id);
                                for(int i = 0; i < (length / pointEffectSpace) - 1; i++){
                                    Fx.v.trns(e.rotation, Fx.rand.random(8f, v.dst(e.x, e.y) - 8f));
                                    Lines.lineAngleCenter(e.x + Fx.v.x, e.y + Fx.v.y, e.rotation + e.finpow(), e.foutpowdown() * 20f * Fx.rand.random(0.5f, 1f) + 0.3f);
                                }

                                e.scaled(14f, b -> {
                                    stroke(b.fout() * 1.5f);
                                    color(e.color);
                                    Lines.line(e.x, e.y, v.x, v.y);
                                });
                            });
                        }};
                    }
                });

            }
        };

        tank3s = new TankUnitType("tank3s"){
            {
                constructor = TankUnit::create;

                hitSize = 72;
                speed = 0.9f;
                rotateSpeed = 1f;
                health = 90000;
                armor = 44;
                itemCapacity = 0;
                floorMultiplier = 0.6f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new PowerAmmoType(50000);

                ammoCapacity = 10000;

                float lx = 140, ly = 180;

                treadFrames = 20;

                treadRects = new Rect[]{new Rect(31 - lx, 20 - ly, 69, 320)};

                researchCostMultiplier = 0.5f;

                immunities.addAll(StatusEffects.melting, StatusEffects.burning, slow, unmoving, WHStatusEffects.rust);

                weapons.add(new Weapon(name("tank3s-weapon1")){

                    {
                        mirror = false;
                        shake = 4;
                        shootY = 40;
                        rotate = true;
                        rotateSpeed = 1.2f;
                        x = 0;
                        y = 0;
                        reload = 300;
                        shootStatusDuration = shoot.firstShotDelay = 60;
                        shootStatus = WHStatusEffects.powerReduce2;
                        chargeSound = chargeCorvus;
                        shootSound = shootMeltdown;
                        continuous = true;
                        cooldownTime = 200;
                        heatColor = WHPal.Heat;

                        parentizeEffects = true;

                        bullet = new LaserBeamBulletType(){{
                            damage = 175;
                            width = 25;
                            length = 100;
                            moveInterp = Interp.pow3Out;
                            extensionProportion = 330 / length;
                            damageInterval = 6;
                            damageMult = 4f;
                            lifetime = 180;

                            pierceCap = 3;

                            drawPositionLighting = true;
                            triCap = tri = true;
                            sideWidth = 2;
                            sideLength = 40;
                            moveSpeed = 0.05f;

                            Color c = lightningColor = hitColor = WHPal.ShootOrange.cpy();
                            colors = new Color[]{c.a(0.2f), c.a(0.5f), c.a(0.7f), c, Color.white};

                            chargeEffect = new MultiEffect(
                            WHFx.genericChargeCircle(hitColor, 8, 90, shoot.firstShotDelay + 1 - 20).startDelay(20).followParent(true),
                            WHFx.lineCircleIn(hitColor, shoot.firstShotDelay + 1 - 20, 40, 2).startDelay(20).followParent(true),
                            WHFx.genericCharge(hitColor, 8, 40, shoot.firstShotDelay + 1).followParent(true)
                            ).followParent(true);

                            hitEffect = new MultiEffect(
                            WHFx.hitCircle(hitColor, Color.lightGray, 15, 6, 40, 8),
                            WHFx.hitSpark(hitColor, 20, 6, 40, 1.5f, 10)
                            );
                        }};
                    }
                });
            }
        }

        ;

        tank2s = new TankUnitType("tank2s"){
            {

                constructor = TankUnit::create;

                aiController = () -> new GroundAI(){
                    @Override
                    public void faceTarget(){
                        if(unit.vel.len() < 1 && target != null){
                            unit.rotation = Mathf.approachDelta(unit.rotation, unit.angleTo(target.x(), target.y()), unit.type.rotateSpeed / 2 * Time.delta);
                        }
                    }
                };

                controller = u -> !playerControllable || (u.team.isAI() && !u.team.rules().rtsAi) ? aiController.get() :
                new CommandAI(){
                    @Override
                    public void faceTarget(){
                        if(unit.vel.len() < 1 && target != null){
                            unit.rotation = Mathf.approachDelta(unit.rotation, unit.angleTo(target.x(), target.y()), unit.type.rotateSpeed / 2 * Time.delta);
                        }
                    }
                };

                rotateMoveFirst = false;

                hitSize = 50;
                speed = 1.4f;
                rotateSpeed = 1.5f;
                health = 32000;
                armor = 30;
                itemCapacity = 0;
                floorMultiplier = 0.6f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new ItemAmmoType(WHItems.ceramite){{
                    ammoPerItem = 10;
                }};

                float lx = 110, ly = 175;

                treadFrames = 16;
                treadPullOffset = 8;
                treadRects = new Rect[]{new Rect(8 - lx, 43 - ly - treadPullOffset, 53, 272)};

                researchCostMultiplier = 0.7f;

                targetAir = false;

                range = 280;

                abilities.add(new PcShieldArcAbility(){{
                    whenShooting = false;
                    y = 25;
                    radius = 30;
                    max = 12000;
                    regen = 30;
                    cooldown = 1500;
                    angle = 100;
                    width = 6f;
                }});
                abilities.add(new MoveEffectAbility(){{
                    interval = 8;
                    minVelocity = 0;
                    parentizeEffects = rotateEffect = true;
                    display = false;
                    y = 19 / 4f;
                    x = -20 / 4f;
                    effect = new MultiEffect(new ParticleEffect(){
                        {
                            particles = 1;
                            sizeFrom = 4;
                            sizeTo = 0;
                            length = -30;
                            lifetime = 80;
                            cone = 150;
                            colorFrom = Color.valueOf("767676FF");
                            colorTo = Color.valueOf("767676FF");
                        }
                    },
                    new ParticleEffect(){
                        {
                            particles = 1;
                            sizeFrom = 8;
                            sizeTo = 0;
                            length = -30;
                            lifetime = 80;
                            cone = 150;
                            colorFrom = Color.valueOf("767676FF");
                            colorTo = Color.valueOf("767676FF");
                        }
                    });
                }});


                weapons.add(new Weapon(name("tank2s-weapon1")){
                    {
                        shootSound = shootTank;
                        shake = 4;
                        layerOffset = 0.001f;
                        heatColor = Pal.turretHeat;
                        cooldownTime = 60;
                        recoilTime = 80;
                        x = y = 0;
                        reload = 400;
                        mirror = false;
                        rotate = false;
                        shoot = new ShootBarrel(){
                            {
                                shots = 1;
                                barrels = new float[]{0f, 136 / 4f, 0f};
                            }
                        };

                        parts.add(new RegionPart("-barrel"){
                            {
                                layerOffset = -0.001f;
                                mirror = false;
                                heatProgress = PartProgress.recoil;
                                progress = PartProgress.recoil;
                                moveY = -7f;
                            }
                        });

                        bullet = new BasicBulletType(10, 800, "missile-large"){
                            {
                                width = 16;
                                height = 42;
                                lifetime = 300 / speed;
                                recoil = 2;

                                damage = 900;
                                collidesTiles = true;
                                collidesAir = false;
                                hittable = false;
                                absorbable = false;
                                frontColor = WHPal.ShootOrangeLight;
                                hitColor = trailColor = backColor = WHPal.ShootOrange;

                                scaledSplashDamage = true;
                                splashDamageRadius = 64;
                                splashDamage = 700;
                                buildingDamageMultiplier = 2;
                                hitSound = explosionMissile;
                                hitShake = 9;
                                shootEffect = new MultiEffect(
                                WHFx.shootLine(40, 30),
                                WHFx.lineCircleOut(hitColor, 30, 30, 2)
                                );
                                smokeEffect = none;
                                trailChance = 0.5f;
                                trailInterval = 1;
                                trailRotation = true;
                                trailEffect = tank3sMissileTrailSmoke;
                                despawnEffect = hitEffect = new MultiEffect(massiveExplosion,
                                WHFx.hitSpark(backColor, 45f, 15, 90f, 2f, 10f),
                                WHFx.lineCircleOut(backColor, 70f, 70, 2),
                                WHFx.sharpBlast(backColor, frontColor, 160f, 40f),
                                WHFx.generalExplosion(50, hitColor, splashDamageRadius, 10, false),
                                tank3sExplosionSmoke
                                );
                                fragBullets = 7;
                                fragBullet = new BasicBulletType(3, 100){
                                    {
                                        lifetime = 20;
                                        hitColor = frontColor = backColor = WHPal.ShootOrange;
                                        width = 16;
                                        height = 16;
                                        scaledSplashDamage = true;
                                        splashDamageRadius = 49;
                                        splashDamage = 200;
                                        buildingDamageMultiplier = 1.8f;
                                        hitSound = explosion;
                                        absorbable = false;
                                        sprite = "circle-bullet";
                                        shrinkY = 0;
                                        hitEffect = new Effect(60f, e -> {
                                            Fx.rand.setSeed(e.id);
                                            Draw.color(WHPal.ShootOrange, WHPal.ShootOrange, e.finpow());
                                            Lines.stroke(1.75f * e.fout());
                                            Lines.spikes(e.x, e.y, Fx.rand.random(10, 20) * e.finpow(), Fx.rand.random(1, 5) * e.foutpowdown() + Fx.rand.random(5, 8) * e.fin(), 4, 45);
                                            randLenVectors(e.id, 4, 4f + e.fin() * 8f, (x, y) -> {
                                                color(WHPal.ShootOrange, WHPal.ShootOrange, e.finpow());
                                                Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 6f, 45);

                                            });
                                        });
                                        despawnEffect = WHFx.generalExplosion(20, hitColor, splashDamageRadius, 5, false);
                                    }
                                };
                            }
                        };
                    }
                });

                weapons.add(new Weapon(name("missile-launcher")){
                    {
                        x = 35 / 4f;
                        y = 13 / 4f;
                        layerOffset = 0.002f;
                        reload = 45;
                        rotate = true;
                        rotateSpeed = 1.5f;
                        shootSound = WHSounds.missileShoot;
                        inaccuracy = 6;
                        alternate = true;
                        mirror = false;
                        shoot = new ShootAlternate(){{
                            barrels = 3;
                            spread = 17 / 4f;
                            shots = 3;
                            shotDelay = 6f;
                        }};

                        autoTarget = true;
                        controllable = false;
                        bullet = new CritMissileBulletType(6, 70, name("rocket")){
                            {
                                pierceArmor = true;
                                width = 10;
                                height = width * 2.2f;
                                shrinkX = shrinkY = 0f;

                                lengthOffset = width - 2;
                                flameWidth = 2.5f;
                                flameLength = 15f;

                                weaveMagOnce = true;
                                weaveMag = 0.4f;
                                weaveRandom = true;
                                drawMissile = true;
                                lightningColor = hitColor = trailColor = WHPal.ShootOrange;

                                Color c = WHPal.ShootOrange.cpy().lerp(Color.orange, 0.25f);
                                colors = new Color[]{c.a(0.55f), c.a(0.7f), c.a(0.8f), c, Color.white};

                                lifetime = 300 / speed;
                                damage = 80;
                                trailLength = 6;
                                trailWidth = 1.5f;
                                statusDuration = 60;
                                homingDelay = lifetime / 2f;
                                homingPower = 0.01f;
                                followAimSpeed = 0.1f;

                                despawnEffect = hitEffect = new MultiEffect(
                                WHFx.generalExplosion(10, hitColor, 25, 5, false),
                                WHFx.instHit(hitColor, true, 2, 30f)
                                );
                                shootEffect = shootBigColor;
                                lightningDamage = 30;
                                lightning = 2;
                                lightningLength = 12;
                            }
                        };
                    }
                });
            }
        };


        tank1s = new TankUnitType("tank1s"){
            {
                constructor = TankUnit::create;

                hitSize = 32;
                speed = 1.4f;
                rotateSpeed = 1.5f;
                health = 15000;
                armor = 20;
                itemCapacity = 0;
                floorMultiplier = 0.6f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new ItemAmmoType(WHItems.ceramite){{
                    ammoPerItem = 10;
                }};

                float lx = 94, ly = 112;

                treadFrames = 16;
                treadRects = new Rect[]{new Rect(17 - lx, 7 - ly, 45, 208)};

                researchCostMultiplier = 0.8f;

                abilities.addAll(new AccelerateReload(2, 180));

                BasicBulletType onp = new ArtilleryBulletType(0, 150, "circle-bullet"){
                    {

                        frontColor = WHPal.WHYellow2;
                        backColor = WHPal.ShootOrange;
                        shrinkX = 1;
                        shrinkY = 1;

                        lifetime = 15f;
                        hittable = absorbable = false;
                        scaledSplashDamage = true;
                        splashDamageRadius = 56;
                        splashDamage = 280;
                        hitShake = 3;
                        hitSound = explosionPlasmaSmall;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.hitSpark(WHPal.ShootOrange, 30f, 20, 60f, 2f, 10f),
                        WHFx.circleOut(WHPal.ShootOrange, 55f));
                    }
                };
                weapons.add(new Weapon(name("small-cannon")){
                    {
                        x = 23 / 4f;
                        y = -26 / 4f;
                        layerOffset = 0.001f;
                        shootY = 69 / 4f;
                        shoot = new ShootAlternate(){
                            {
                                barrels = 2;
                                spread = 28 / 4f;
                                shots = 2;
                                shotDelay = 6.0F;
                            }
                        };
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 1.2f;
                        cooldownTime = 150;
                        heatColor = WHPal.Heat;
                        recoil = 3;
                        reload = 200;
                        inaccuracy = 3;
                        velocityRnd = 0.1f;
                        bullet = new CritBulletType(15, 150, name("energy-bullet")){
                            {
                                damage = 330;
                                lifetime = 300 / speed;
                                width = 18;
                                height = 24;
                                hitSize = 30;
                                frontColor = WHPal.ShootOrangeLight;
                                hitColor = trailColor = backColor = WHPal.ShootOrange;
                                trailLength = 8;
                                trailWidth = 3.6f;
                                hitSound = explosion;
                                hitEffect = despawnEffect =
                                new MultiEffect(WHFx.hitSpark(hitColor, 30f, 20, 60f, 2f, 10f),
                                WHFx.circleOut(hitColor, 55f));

                                smokeEffect = smokeCloud;

                                fragBullet = onp;
                                fragBullets = 1;
                                fragRandomSpread = 0;
                                fragOnHit = true;
                            }
                        };
                    }
                });
                weapons.add(new Weapon(name("laser-weapon-1")){
                    {

                        y = 12 / 4f;
                        x = 62 / 4f;
                        reload = 60;
                        ejectEffect = Fx.none;
                        recoil = 2f;
                        shootSound = WHSounds.machineGunShoot;
                        shoot = new ShootAlternate(){{
                            shotDelay = 8;
                            shots = 4;
                            spread = 16f / 4f;
                        }};

                        rotate = true;
                        rotateSpeed = 1.5f;

                        shootY = 16 / 4f;

                        bullet = new RailBulletType(){{
                            length = 300;
                            damage = 70;
                            pointEffectSpace = 10f;
                            hitColor = WHPal.ShootOrange;
                            hitEffect = endEffect = Fx.hitBulletColor;
                            pierceDamageFactor = 0.95f;

                            smokeEffect = Fx.colorSpark;

                            endEffect = new Effect(14f, e -> {
                                color(e.color);
                                Drawf.tri(e.x, e.y, e.fout() * 1.5f, 5f, e.rotation);
                            });

                            shootEffect = new Effect(10, e -> {
                                color(e.color);
                                float w = 1.2f + 7 * e.fout();

                                Drawf.tri(e.x, e.y, w, 30f * e.fout(), e.rotation);
                                color(e.color);

                                for(int i : Mathf.signs){
                                    Drawf.tri(e.x, e.y, w * 0.9f, 18f * e.fout(), e.rotation + i * 90f);
                                }

                                Drawf.tri(e.x, e.y, w, 4f * e.fout(), e.rotation + 180f);
                            });

                            lineEffect = new Effect(20f, e -> {
                                if(!(e.data instanceof Vec2 v)) return;

                                color(e.color);
                                stroke(e.fout() * 0.9f + 1.2f);

                                Fx.rand.setSeed(e.id);
                                for(int i = 0; i < (length / pointEffectSpace) - 1; i++){
                                    Fx.v.trns(e.rotation, Fx.rand.random(8f, v.dst(e.x, e.y) - 8f));
                                    Lines.lineAngleCenter(e.x + Fx.v.x, e.y + Fx.v.y, e.rotation + e.finpow(), e.foutpowdown() * 20f * Fx.rand.random(0.5f, 1f) + 0.3f);
                                }

                                e.scaled(14f, b -> {
                                    stroke(b.fout() * 1.5f);
                                    color(e.color);
                                    Lines.line(e.x, e.y, v.x, v.y);
                                });
                            });
                        }};
                    }
                });
                weapons.add(new Weapon(name("machine-gun-1")){
                    {
                        reload = 10;
                        velocityRnd = 0.15f;
                        x = -20 / 4f;
                        y = 30 / 4f;
                        recoil = 2;
                        shootY = 32 / 4f;
                        mirror = false;
                        rotate = true;
                        rotationLimit = 90;
                        rotateSpeed = 1;
                        inaccuracy = 2;
                        shootSound = shootSpectre;
                        shootCone = 25;
                        bullet = new CritBulletType(){{
                            damage = 40;
                            width = 10;
                            height = 22;
                            speed = 25;
                            lifetime = 14f;
                            shrinkY = 0;
                            frontColor = WHPal.ShootOrangeLight;
                            trailColor = backColor = WHPal.ShootOrange;
                            trailLength = 3;
                            trailWidth = 2;
                            smokeEffect = Fx.shootBig;
                            hitEffect = despawnEffect = new MultiEffect(new Effect(30, e -> {
                                Draw.color(WHPal.ShootOrange);
                                Angles.randLenVectors(e.id, 1, e.finpow(), (x, y) -> {
                                    Fill.square(e.x, e.y, e.fout() * 4f, 45);
                                    Drawf.light(e.x + x, e.y + y, e.fout() * 8f, WHPal.ShootOrange, 0.7f);
                                });
                            }), WHFx.hitSpark(WHPal.ShootOrange, 20, 4, 25, 2, 8));
                        }};
                    }
                });
            }
        };

        tank3 = new TankUnitType("tank3"){
            {
                constructor = TankUnit::create;

                hitSize = 72;
                speed = 0.8f;
                rotateSpeed = 1f;
                health = 100000;
                armor = 40;
                itemCapacity = 0;
                floorMultiplier = 0.6f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new ItemAmmoType(WHItems.refineCeramite){{
                    ammoPerItem = 10;
                }};

                ammoCapacity = 10000;

                float lx = 150, ly = 205;

                treadFrames = 20;
                treadPullOffset = 20;
                treadRects = new Rect[]{new Rect(19 - lx, 20 - treadPullOffset / 2 - ly, 69, 380)};

                researchCostMultiplier = 0.5f;

                immunities.addAll(WHStatusEffects.palsy, WHStatusEffects.powerReduce2, unmoving);

                range = 430;

                weapons.add(new Weapon(name("tank3-weapon1")){
                    {
                        reload = 300;
                        x = 0;
                        y = -16 / 4f;
                        shootY = 200 / 4f;
                        cooldownTime = 180;
                        heatColor = WHPal.Heat;
                        layerOffset = 0.002f;
                        rotate = true;
                        rotateSpeed = 1;
                        recoil = 6;
                        mirror = false;
                        shootSound = shootTank;
                        shake = 6;
                        shoot.firstShotDelay = 30;

                        bullet = new CritBulletType(20, 1500, name("pierce")){
                            {
                                splashDamage = 900;
                                splashDamageRadius = 56;
                                buildingDamageMultiplier = 0.5f;
                                width = 30;
                                height = width * 2;
                                hitSize = 18;
                                lifetime = 450 / speed;
                                lightningColor = WHPal.ShootOrange;

                                lightningDamage = 50;
                                lightning = 2;
                                lightningLength = 10;

                                pierceBuilding = true;
                                pierceCap = 3;
                                hitShake = 5;

                                frontColor = WHPal.ShootOrangeLight;
                                trailColor = hitColor = backColor = WHPal.ShootOrange;
                                trailLength = 25;
                                trailWidth = width / 5f;
                                trailChance = 0.8f;
                                trailInterval = 3;
                                trailEffect = WHFx.square(hitColor, 30, 3, 45, 8);
                                hitEffect = WHFx.instHit(hitColor, true, 3, 40);
                                despawnEffect = new MultiEffect(WHFx.hitSpark(hitColor, 30f, 20, 60f, 2f, 10f),
                                WHFx.circleOut(hitColor, 70, 70),
                                WHFx.sharpBlast(hitColor, hitColor, 100f, 50f),
                                WHFx.subEffect(70, 70, 11, 30f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
                                    float fout = Interp.pow2Out.apply(1 - fin);
                                    Draw.color(hitColor);
                                    for(int s : Mathf.signs){
                                        Drawf.tri(x, y, 15 * fout, 25 * Mathf.curve(fin, 0, 0.1f) * WHFx.fout(fin, 0.25f), rot + s * 90);
                                    }
                                })));
                                hitSound = explosionArtillery;
                                shootEffect = new MultiEffect(
                                WHFx.generalExplosion(10, hitColor, 30, 5, false),
                                WHFx.shootLine(30, 30));
                                smokeEffect = smokeCloud;
                            }
                        };
                    }
                });
                weapons.add(new Weapon(name("tank3-weapon2")){
                    {
                        x = 108 / 4f;
                        y = 64 / 4f;
                        inaccuracy = 3;
                        shootY = 57 / 4f;
                        reload = 90;
                        rotate = true;
                        rotateSpeed = 2;
                        shootSound = shootLaser;
                        recoil = 1;
                        shoot.firstShotDelay = 30;
                        shoot.shots = 3;
                        shoot.shotDelay = 12;
                        bullet = new PositionLightningBulletType(100){
                            {
                                maxRange = 400;
                                hitColor = lightColor = lightningColor = WHPal.ShootOrange;
                                shootEffect = new WrapEffect(WHFx.hitSparkLarge, WHPal.ShootOrange);
                                hitEffect = WHFx.lightningHitSmall;
                                spawnBullets.add(new LaserBulletType(150){
                                    {
                                        pierceCap = 3;
                                        sideAngle = 90;
                                        sideWidth = 2;
                                        sideLength = 20;
                                        Color c = hitColor = lightColor = lightningColor = WHPal.ShootOrange.cpy();
                                        colors = new Color[]{c.a(0.3f), c.a(0.5f), c.a(0.7f), WHPal.ShootOrangeLight};
                                        width = 14;
                                        length = 400;
                                    }
                                });
                            }
                        };
                    }
                });
                weapons.add(new Weapon(name("machine-gun")){
                    {
                        x = -29 / 4f;
                        y = 90 / 4f;

                        shootY = 22 / 4f;
                        rotate = true;
                        mirror = false;
                        rotateSpeed = 1f;
                        rotationLimit = 60;
                        recoil = 2;

                        inaccuracy = 3.5f;
                        reload = 80;
                        cooldownTime = 45;
                        heatColor = WHPal.Heat;
                        shootCone = 15;
                        shootSound = shootSpectre;
                        shoot = new ShootAlternate(){
                            {
                                barrels = 3;
                                spread = 2;
                                shots = 9;
                                shotDelay = recoilTime = 6;
                            }
                        };

                        recoils = 3;
                        for(int i = 0; i < 3; i++){
                            int f = i;
                            parts.add(new RegionPart("-barrel-" + f){{
                                heatProgress = progress = PartProgress.recoil;
                                recoilIndex = f;
                                under = true;
                                moveY = -4f;
                            }});
                        }
                        bullet = new DelayedPointBulletType(){
                            {
                                maxRange = range = 300;
                                damage = 100;
                                splashDamage = damage * 0.7f;
                                splashDamageRadius = 40;
                                shootSound = shootLaser;
                                hitSound = WHSounds.blast;
                                delayEffectLifeTime = 10;
                                square = true;
                                width = 10;
                                Color c = hitColor = lightningColor = WHPal.ShootOrange.cpy();
                                colors = new Color[]{c.a(0.3f), c.a(0.5f), c.a(0.8f), WHPal.ShootOrangeLight};
                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.linePolyOut(hitColor, 10, splashDamageRadius, 3, 4, 0),
                                WHFx.hitSpark(hitColor, 30, 5, splashDamageRadius, 2, 8)
                                );
                            }
                        };
                    }
                });
                weapons.add(new Weapon(name("machine-gun-2")){
                    {
                        x = 30 / 4f;
                        y = 90 / 4f;
                        rotate = true;
                        rotateSpeed = 0.8f;
                        rotationLimit = 60;
                        mirror = false;
                        recoil = 2;
                        inaccuracy = 3;
                        reload = 240;
                        cooldownTime = 180;
                        heatColor = WHPal.Heat;
                        shootCone = 10;
                        shootSound = explosion;
                        bullet = new ArtilleryBulletType(8, 500, "circle-bullet"){{
                            lifetime = 50;
                            hitSize = 12;
                            width = height = 16;
                            hitColor = trailColor = frontColor = backColor = WHPal.ShootOrange;
                            trailLength = 10;
                            trailWidth = 3;
                            trailInterval = 1f;
                            trailEffect = WHFx.square(hitColor, 30, 2, 20, 5);
                            shootEffect = shootBig2;
                            splashDamageRadius = 64;
                            splashDamage = damage;
                            buildingDamageMultiplier = 1.5f;
                            hitEffect = despawnEffect = new MultiEffect(
                            WHFx.circleOut(hitColor, 60, splashDamageRadius),
                            WHFx.smoothColorCircle(hitColor, splashDamageRadius, 60),
                            WHFx.square(hitColor, 30, 20, splashDamageRadius, 5),
                            WHFx.hitSpark(hitColor, 60, 20, splashDamageRadius + 30, 1.5f, 10));
                            hitSound = explosionTitan;
                        }};
                    }
                });
            }
        };

        tank2 = new TankUnitType("tank2"){
            {
                constructor = TankUnit::create;

                hitSize = 56;
                speed = 1f;
                rotateSpeed = 1.3f;
                health = 30000;
                armor = 30;
                itemCapacity = 0;
                floorMultiplier = 0.6f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new ItemAmmoType(WHItems.ceramite);

                float lx = 130, ly = 160;

                treadFrames = 16;
                treadPullOffset = 8;
                treadRects = new Rect[]{new Rect(30 - lx, 24 + treadPullOffset - ly, 51, 264)};

                researchCostMultiplier = 0.6f;

                weapons.add(new Weapon(name("tank2-weapon1")){
                    {
                        layerOffset = 0.011f;
                        reload = 200;
                        x = 0;
                        y = -2 / 4f;
                        shootY = 140 / 4f;
                        cooldownTime = 100;
                        rotate = true;
                        top = true;
                        rotateSpeed = 0.8f;
                        recoil = 3;
                        recoilTime = 90f;
                        mirror = false;
                        inaccuracy = 0;
                        shootSound = shootRipple;
                        shake = 8;

                        shootWarmupSpeed = 0.2f;
                        minWarmup = 0.99f;

                        shoot = new ShootAlternate(){
                            {
                                barrels = 2;
                                shots = 2;
                                shotDelay = 8;
                                spread = 40 / 4f;
                            }
                        };

                        recoils = 2;

                        parts.addAll(
                        new RegionPart("-aim"){{
                            layerOffset = 0.001f;
                            y = 5 / 4f;
                            x = 19 / 4f;
                            children.addAll(
                            new AimLaserPart(){{
                                alpha = PartProgress.warmup.mul(0.5f).add(0.5f);
                                blending = Blending.additive;
                                length = 80;
                                width = 8 / 4f;
                            }}
                            );
                        }}
                        );

                        for(int i = 0; i < 2; i++){
                            int f = i;
                            parts.add(new RegionPart("-barrel-" + f){{
                                progress = PartProgress.recoil;
                                recoilIndex = f;
                                under = true;
                                moveY = -8f;
                            }});
                        }

                        bullet = new CritBulletType(20, 500, name("pierce")){{
                            splashDamage = damage * 0.35f;
                            splashDamageRadius = 55;
                            lifetime = 360 / speed;

                            width = 8;
                            height = width * 4f;

                            lightning = 3;
                            lightningDamage = 40;
                            lightningLength = 15;

                            shrinkY = 0;
                            frontColor = WHPal.ShootOrangeLight;
                            hitColor = lightningColor = trailColor = backColor = WHPal.ShootOrange;
                            trailLength = 10;
                            trailWidth = 2f;

                            pierceCap = 3;
                            pierceBuilding = true;
                            knockback = 8;

                            trailEffect = WHFx.square(hitColor, 20, 2, 20, 4);
                            trailChance = 0.25f;
                            trailInterval = 3;

                            hitEffect = new MultiEffect(
                            WHFx.square(hitColor, 20, 12, 50, 6),
                            WHFx.hitSpark(hitColor, 20, 15, 60f, 1f, 10f),
                            WHFx.lineCircleOut(hitColor, 20, 50, 3),
                            WHFx.instHit(hitColor, true, 5, 50)
                            );

                            despawnEffect = new MultiEffect(
                            WHFx.square(hitColor, 60, 12, 50, 6),
                            WHFx.hitSpark(hitColor, 60, 15, 60f, 1f, 10f),
                            WHFx.lineCircleOut(hitColor, 60, 50, 3),
                            WHFx.explosionSmokeEffect(hitColor, 60, 50, 10, 8)
                            );

                            shootEffect = new MultiEffect(
                            WHFx.shootLine(30, 30),
                            new WrapEffect(WHFx.lightningHitLarge, WHPal.ShootOrange),
                            WHFx.lineCircleOut(hitColor, 20, 30, 2)
                            );

                            hitSound = explosionPlasmaSmall;
                            fragLifeMin = 0.1f;
                            fragBullets = 2;
                            fragRandomSpread = 8;
                            fragBullet = new PointBulletType(){{
                                trailEffect = none;
                                despawnEffect = none;
                                hitEffect = new MultiEffect(WHFx.instHit(WHPal.ShootOrange, true, 3, 20),
                                WHFx.hitSpark(WHPal.ShootOrange, 30f, 30, 60f, 2f, 10f));
                                lightning = 1;
                                lightningDamage = 25;
                                lightningLength = 6;
                                lightningColor = WHPal.ShootOrange;
                                hitSound = shootLaser;
                                collides = false;
                                splashDamageRadius = 40;
                                splashDamage = damage = 100;
                                buildingDamageMultiplier = 1.5f;
                                lifetime = 10;
                                speed = 8;
                                fragBullets = 1;
                                fragRandomSpread = 8;
                                fragBullet = new PointBulletType(){
                                    {
                                        trailEffect = none;
                                        despawnEffect = bigShockwave;
                                        hitEffect = new MultiEffect(
                                        WHFx.hitSpark(WHPal.ShootOrange, 30f, 15, 20f, 2f, 10f),
                                        WHFx.instHit(WHPal.ShootOrange, true, 3, 15));
                                        lightning = 1;
                                        lightningDamage = 25;
                                        lightningLength = 10;
                                        lightningColor = WHPal.ShootOrange;
                                        hitSound = shootLaser;
                                        collides = false;
                                        splashDamage = damage = 130;
                                        buildingDamageMultiplier = 1.5f;
                                        lifetime = 10;
                                        speed = 2;
                                    }
                                };
                            }};
                        }};
                    }
                });
                weapons.add(new Weapon(name("machine-gun")){
                    {
                        x = 82 / 4f;
                        y = -16 / 4f;

                        layerOffset = 0.01f;

                        shootY = 22 / 4f;
                        rotate = true;
                        rotateSpeed = 1f;
                        rotationLimit = 180;
                        recoil = 2;

                        inaccuracy = 3.5f;
                        reload = 80;
                        cooldownTime = 45;
                        heatColor = WHPal.Heat;
                        shootCone = 15;
                        shootSound = shootSpectre;
                        shoot = new ShootAlternate(){
                            {
                                barrels = 3;
                                spread = 2;
                                shots = 6;
                                shotDelay = recoilTime = 10;
                            }
                        };

                        recoils = 3;
                        for(int i = 0; i < 3; i++){
                            int f = i;
                            parts.add(new RegionPart("-barrel-" + f){{
                                heatProgress = progress = PartProgress.recoil;
                                recoilIndex = f;
                                under = true;
                                moveY = -4f;
                            }});
                        }

                       /* bullet = new CritBulletType(15, 80){{
                            lifetime = 300 / speed;
                            hitSize = 8;
                            shootEffect = shootBig2;
                            smokeEffect = shootBigSmoke;
                            width = 8;
                            height = width * 2.2f;
                            splashDamageRadius = 40;
                            splashDamage = 50;
                            frontColor = WHPal.ShootOrangeLight;
                            trailWidth = 2;
                            trailLength = 5;
                            hitColor = trailColor = backColor = WHPal.ShootOrange;
                            hitEffect = despawnEffect = WHFx.generalExplosion(10, hitColor, splashDamageRadius, 10, false);
                        }};*/
                        bullet = new DelayedPointBulletType(){
                            {
                                maxRange = range = 300;
                                damage = 80;
                                splashDamage = damage * 0.7f;
                                splashDamageRadius = 40;
                                shootSound = shootLaser;
                                hitSound = WHSounds.blast;
                                delayEffectLifeTime = 15;
                                square = true;
                                width = 10;
                                Color c = hitColor = lightningColor = WHPal.ShootOrange.cpy();
                                colors = new Color[]{c.a(0.3f), c.a(0.5f), c.a(0.8f), WHPal.ShootOrangeLight};
                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.linePolyOut(hitColor, 10, splashDamageRadius, 3, 4, 0),
                                WHFx.hitSpark(hitColor, 30, 3, splashDamageRadius, 2, 8)
                                );
                            }
                        };
                    }
                });
            }
        };

        tank1 = new TankUnitType("tank1"){

            {
                constructor = TankUnit::create;

                hitSize = 32;
                speed = 1.1f;
                rotateSpeed = 1.5f;
                health = 15000;
                armor = 24;
                itemCapacity = 0;
                floorMultiplier = 0.6f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new PowerAmmoType(4000);

                float lx = 94f, ly = 128;
                treadPullOffset = 8;
                treadFrames = 16;
                treadRects = new Rect[]{new Rect(9 - lx, 29 - treadPullOffset - ly, 45, 208)};

                researchCostMultiplier = 0.6f;

                abilities.add(
                new ShieldRegenFieldAbility(100, 500, 60f * 3, 90));

                weapons.add(new Weapon(name("tank1-weapon1")){
                    {
                        x = 0;
                        y = -10 / 4f;
                        shootY = 122 / 4f;
                        layerOffset = 0.002f;
                        rotate = true;
                        rotateSpeed = 0.8f;
                        cooldownTime = 100;
                        heatColor = WHPal.Heat;
                        mirror = false;
                        inaccuracy = 0;
                        reload = 120;
                        shootSound = explosion;

                        bullet = new DelayedPointBulletType(){
                            {
                                Color c = hitColor = lightningColor = WHPal.ShootOrange.cpy();

                                delayEffectLifeTime = 10;
                                errorCorrectionRadius = 16;
                                maxRange = range = 320;
                                damage = 500;
                                splashDamage = damage * 0.7f;
                                splashDamageRadius = 40;
                                shootSound = shootLaser;
                                hitSound = WHSounds.blast;
                                smokeEffect = smokeCloud;
                                width = 25;
                                lightning = 3;
                                lightningLength = 8;
                                lightningDamage = 40f;
                                square = true;
                                trailChance = 1;
                                trailSpacing = 15;
                                trailEffect = WHFx.square(hitColor, 30, 2, 20, 6);

                                colors = new Color[]{c.a(0.3f), c.a(0.5f), c.a(0.8f), WHPal.ShootOrangeLight};
                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.trailCircleHitSpark(hitColor, 20, 4, 25, 2, 8),
                                WHFx.linePolyOut(hitColor, 10, splashDamageRadius, 3, 4, 0),
                                WHFx.hitSpark(hitColor, 30, 10, splashDamageRadius, 2, 8)
                                );
                            }
                        };
                    }
                });
                weapons.add(new Weapon(name("tank1-weapon")){
                    {
                        reload = 10;
                        velocityRnd = 0.15f;
                        x = 64 / 4f;
                        y = -16 / 4f;
                        recoil = 2;
                        shootY = 28 / 4f;
                        rotate = true;
                        rotateSpeed = 2;
                        inaccuracy = 3;
                        shootSound = shootSpectre;
                        bullet = new CritBulletType(12, 70){{
                            width = 6;
                            height = width * 2;
                            lifetime = 300 / speed;
                            shrinkY = 0;
                            frontColor = WHPal.WHYellow2;
                            trailColor = hitColor = backColor = WHPal.ShootOrange;
                            trailLength = 5;
                            trailWidth = width / 2.8f;
                            smokeEffect = Fx.shootBig;
                            hitEffect = despawnEffect = new MultiEffect(
                            WHFx.square(hitColor, 25, 4, 25, 4),
                            WHFx.hitSpark(WHPal.ShootOrange, 20, 4, 25, 2, 8));
                        }};
                    }
                });
            }
        }

        ;

        Mecha7 = new

        TitanUnitType("mecha7"){
            {
                constructor = TitanUnit::create;
                speed = 0.45f;
                hitSize = 80;
                rotateSpeed = 0.6f;
                health = 260 * 1000f;
                armor = 50;
                mechStepParticles = true;
                stepShake = 2;
                mechFrontSway = 1.5f;
                mechSideSway = 1.3F;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new ItemAmmoType(WHItems.refineCeramite);

               /* abilities.add(
                new EllipseForceFieldAbility(500 / 4f, 400 / 4f, 12, 10000, 90 * 60f, 0.3f, 30){{
                    shader = true;
                    Regen = true;
                    regenThreshold = 0.3f;
                    reflectChance = 0.2f;
                    percentRegen = true;
                    percentRegenAmount = 0.06f;
                    damageReduction = true;
                    damageReductionAmount = 0.25f;

                    fullAbsorb = true;
                    healthThresholds = new float[]{0.8f, 0.4f};
                    fullAbsorbDuration = 60 * 8f;
                }});*/


                weapons.add(new Weapon(name("mecha7-weapon1")){
                    {
                        reload = 200;
                        x = -185 / 4f;
                        y = 15f / 4f;

                        continuous = true;
                        alternate = false;

                        rotate = true;
                        top = false;
                        layerOffset = -0.001f;
                        rotateSpeed = 0.4f;
                        rotationLimit = 35;
                        recoil = 8;
                        recoilTime = 120;
                        shootCone = 20f;
                        inaccuracy = 0;
                        shootSound = beamMeltdown;
                        shake = 5;

                        shootY = 175 / 4f;

                        bullet = new LaserBeamBulletType(){{
                            damage = 150;
                            width = 28;
                            length = 100;
                            moveInterp = Interp.pow3Out;
                            extensionProportion = 450 / length;
                            damageInterval = 6;
                            damageMult = 4f;
                            lifetime = 180;

                            pierceCap = 3;

                            triCap = true;
                            drawPositionLighting = true;

                            rotate = true;
                            rotateAngle = 5;
                            moveSpeed = 0.05f;

                            Color c = lightningColor = hitColor = WHPal.ShootOrange.cpy();
                            colors = new Color[]{c.a(0.2f), c.a(0.5f), c.a(0.7f), c, Color.white};

                            hitEffect = new MultiEffect(
                            WHFx.generalExplosion(10, hitColor, 40, 2, false),
                            WHFx.hitCircle(hitColor, Color.lightGray, 15, 6, 40, 8),
                            WHFx.hitSpark(hitColor, 20, 6, 40, 1.5f, 10)
                            );
                        }};
                    }
                });

                weapons.add(new Weapon(name("mecha7-plasma")){
                    {
                        x = 165 / 4f;
                        y = -54 / 4f;
                        reload = 240;
                        rotate = true;
                        layerOffset = 0.001f;
                        rotateSpeed = 1.5f;
                        rotationLimit = 120;
                        recoil = 3;
                        recoilTime = 60;
                        cooldownTime = 120;
                        heatColor = WHPal.SkyBlue;

                        shootCone = 20f;
                        inaccuracy = 2;
                        shootSound = shootLaser;
                        shake = 2;

                        shootY = 100 / 4f;

                        shoot.firstShotDelay = 70;
                        parentizeEffects = true;

                        bullet = new TrailFadeBulletType(15, 1500, name("energy-bullet")){
                            {
                                lifetime = 450 / speed;

                                hitBlinkTrail = false;

                                width = 15;
                                height = width * 2.7f;

                                frontColor = WHPal.SkyBlueF;
                                lightColor = backColor = trailColor = hitColor = lightningColor = WHPal.SkyBlue;
                                lightning = 3;
                                lightningDamage = 50;
                                lightningLength = 10;
                                lightningLengthRand = 10;


                                chargeEffect = new MultiEffect(
                                WHFx.trailCharge(frontColor, 18, 2, 120, 2, shoot.firstShotDelay + 1).followParent(true),
                                WHFx.trailCharge(frontColor, 15, 2, 90, 2, shoot.firstShotDelay + 1).followParent(true),
                                WHFx.trailCharge(frontColor, 10, 2, 60, 2, shoot.firstShotDelay + 1).followParent(true),
                                WHFx.genericChargeCircle(frontColor, 8, splashDamageRadius * 0.5f, shoot.firstShotDelay + 1).followParent(true)
                                );

                                pierceArmor = true;

                                status = WHStatusEffects.plasma;
                                statusDuration = 150;

                                trailLength = 10;
                                trailWidth = width / 4f;
                                trailInterval = 1f;
                                trailEffect = new MultiEffect(
                                WHFx.hitPoly(hitColor, hitColor, 30, 3, 30, 7, 6, 60)
                                );

                                splashDamage = 800;
                                splashDamageRadius = 90;

                                hitEffect = new MultiEffect(
                                WHFx.instRotation(hitColor, 90, splashDamageRadius * 1.2f, 45, false),
                                WHFx.generalExplosion(45, hitColor, 60, 10, false),
                                WHFx.hitSpark(hitColor, 45, 15, 60, 1.5f, 10),
                                WHFx.hitPoly(hitColor, hitColor, 30, 3, 30, 7, 6, 60),
                                WHFx.trailCircleHitSpark(hitColor, 120, 12, splashDamageRadius / 2, 1.6f, 15)
                                );
                                despawnEffect = new MultiEffect(
                                WHFx.generalExplosion(90, hitColor, splashDamageRadius, 10, true),
                                WHFx.lineCircleOut(hitColor, 90, splashDamageRadius / 2, 4),
                                WHFx.trailCircleHitSpark(hitColor, 120, 20, splashDamageRadius * 2, 1.6f, 15),
                                WHFx.airAsh(90, splashDamageRadius, 20, 20, hitColor, 2, 2),
                                new Effect(90, e -> {
                                    color(hitColor);
                                    rand.setSeed(e.id);
                                    randLenVectors(e.id, (int)(rand.random(0.7f, 1) * 100), splashDamageRadius * 0.7f * Mathf.curve(e.fin(), 0, 0.08f),
                                    splashDamageRadius * 0.3f * rand.random(0.8f, 1.2f) * e.finpow(), (x, y) -> {
                                        Fill.circle(e.x + x, e.y + y, Mathf.curve(e.fin(), 0, 0.08f) * e.fout(Interp.pow10Out) * 4 * rand.random(0.7f, 1.6f));
                                    });
                                }));
                            }

                            @Override
                            public void hit(Bullet b){
                                super.hit(b);
                                float spacing = 10;
                                float damageMulti = b.damageMultiplier();
                                Vec2 v = new Vec2().set(b);
                                for(int k = 0; k < 5; k++){
                                    int finalK = k;
                                    Time.run(k * spacing, () -> {
                                        for(int j : Mathf.signs){
                                            Drawn.randFadeLightningEffect(v.x, v.y, splashDamageRadius * (1.5f), 12, hitColor, j > 0);
                                        }
                                        Damage.damage(b.team, v.x, v.y, splashDamageRadius / 5 * finalK, (splashDamage * damageMulti) / 5 * finalK, false);
                                    });
                                }
                            }

                            @Override
                            public void despawned(Bullet b){
                                super.despawned(b);
                            }
                        };
                    }
                });

                weapons.add(new Weapon(name("small-cannon-3")){
                    {
                        reload = 90;
                        x = 80 / 4f;
                        y = 22 / 4f;
                        rotate = true;
                        rotateSpeed = 1.2f;
                        recoil = 2;
                        recoilTime = 20;
                        shootCone = 20f;
                        rotationLimit = 35;
                        inaccuracy = 8;
                        velocityRnd = 0.1f;
                        cooldownTime = 30;
                        shootY = 51 / 4f;
                        shootSound = WHSounds.missileShoot2;

                        shoot = new ShootMulti(
                        new ShootAlternate(){{
                            barrels = 2;
                            spread = 28 / 4f;
                        }},
                        new ShootSpread(3, 6)
                        );
                        bullet = new CritBulletType(7, 120){
                            {

                                width = 15;
                                height = width * 2;
                                spin = 0.12f;
                                pierceArmor = true;

                                frontColor = WHPal.ShootOrangeLight;

                                backColor = lightningColor = hitColor = trailColor = WHPal.ShootOrange;

                                Color c = WHPal.ShootOrange.cpy().lerp(Color.orange, 0.25f);

                                range = 420;
                                lifetime = 420 / speed;
                                trailLength = 6;
                                trailWidth = 1.5f;

                                splashDamage = damage;
                                splashDamageRadius = 60;

                                pierceCap = 2;
                                pierceBuilding = true;
                                pierceArmor = true;

                                shootEffect = new MultiEffect(
                                WHFx.linePolyOut(hitColor, 20, 25, 2.5f, 6, 60),
                                shootBigColor);

                                despawnEffect = hitEffect = new MultiEffect(
                                /*   WHFx.sharpBlast(hitColor, frontColor, 50, splashDamageRadius),*/
                                WHFx.shuttle(hitColor, hitColor, 20, false, splashDamageRadius / 2, 0),
                                WHFx.instRotation(hitColor, 20, splashDamageRadius, 90, false),
                                WHFx.generalExplosion(20, hitColor, splashDamageRadius, 5, false),
                                WHFx.instHit(hitColor, true, 3, splashDamageRadius / 2)
                                );
                                shootEffect = shootBigColor;

                                fragBullets = 4;
                                fragAngle = 120;
                                fragBullet = new CritBulletType(4, 100){{
                                    lifetime = 20;
                                    width = 10;
                                    height = width * 2;
                                    hitSize = 10;
                                    splashDamage = damage;
                                    splashDamageRadius = 40;
                                    buildingDamageMultiplier = 2;
                                    frontColor = WHPal.ShootOrangeLight;

                                    backColor = lightningColor = hitColor = trailColor = WHPal.ShootOrange;
                                    despawnEffect = hitEffect = new MultiEffect(
                                    WHFx.linePolyOut(hitColor, 13f, splashDamageRadius, 2.5f, 6, 60),
                                    WHFx.trailHitSpark(hitColor, 13f, 5, 45, 1.5f, 10),
                                    WHFx.generalExplosion(8, hitColor, splashDamageRadius, 5, false)
                                    );
                                }};

                            }
                        };
                    }
                });

                weapons.add(new Weapon(name("small-cannon-2")){
                    {
                        reload = 10;
                        x = 0;
                        y = -49 / 4f;
                        shootY = 75 / 4f;
                        rotate = true;
                        rotateSpeed = 2f;
                        layerOffset = 0.0001f;
                        recoil = 2;
                        recoilTime = 45f;
                        mirror = false;
                        inaccuracy = 0;
                        shootSound = shootSpectre;
                        heatColor = WHPal.Heat;
                        cooldownTime = 10f;
                        shoot = new ShootAlternate(){{
                            barrels = 2;
                            spread = 36 / 4f;
                        }};
                        bullet = new CritBulletType(10, 120, name("pierce")){
                            {
                                width = 10;
                                height = width * 1.5f;
                                trailWidth = width / 3.5f;
                                trailLength = 10;

                                lifetime = 450 / speed;
                                splashDamageRadius = 40;
                                splashDamage = 100;
                                lightningDamage = 40;
                                lightning = 3;
                                lightningLength = 8;
                                lightningLengthRand = 6;

                                frontColor = WHPal.ShootOrangeLight;
                                hitColor = lightningColor = backColor = trailColor = WHPal.ShootOrange;


                                shootEffect = new MultiEffect(
                                shootBigColor,
                                WHFx.shootLineSmall(WHPal.ShootOrange)
                                );

                                trailChance = 0.5f;
                                trailEffect = WHFx.square(hitColor, 45, 2, 25, 6);

                                hitShake = 3;
                                hitSound = explosionPlasmaSmall;
                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.instHit(hitColor, true, 3, splashDamageRadius / 2),
                                WHFx.lineCircleIn(hitColor, 20, splashDamageRadius, 3),
                                explosionSmokeEffect(hitColor, 30, splashDamageRadius, 4, 10).startDelay(20f)
                                );
                            }
                        };
                    }
                });

            }
        };

        Mecha6 = new UnitType("mecha6"){
            {
                constructor = MechUnit::create;

                speed = 0.65f;
                hitSize = 50;
                rotateSpeed = 1.3f;
                health = 96000;
                armor = 32f;
                mechStepParticles = true;
                stepShake = 0.75f;
                drownTimeMultiplier = 0.5f;
                mechFrontSway = 1.5f;
                mechSideSway = 1;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new ItemAmmoType(WHItems.refineCeramite);
                range = 390;

                abilities.add(
                new EllipseForceFieldAbility(300 / 4f, 180 / 4f, 7, 5000, 70 * 60f, 0.3f, 22){{
                    reflectChance = 0.15f;
                    percentRegen = true;
                    percentRegenAmount = 0.06f;
                }},
                new ShockAbility(){{
                    bulletDamage = 400;
                    reload = 3 * 60f;
                    waveColor = WHPal.ShootOrange;
                }});

                weapons.add(new Weapon(name("mecha6-weapon1")){
                    {
                        reload = 180;
                        x = -122 / 4f;
                        y = -7f / 4f;
                        shootY = 120 / 4f;
                        rotate = true;
                        top = false;
                        layerOffset = -0.001f;
                        rotateSpeed = 0.5f;
                        recoil = 5;
                        recoilTime = 45;
                        shootCone = 20f;
                        rotationLimit = 35;
                        inaccuracy = 3;
                        shootSound = explosionTitan;
                        shake = 5;
                        bullet = new CritBulletType(15, 900, "missile-large"){
                            {
                                critChance = 0.2f;
                                width = 16;
                                height = width * 2;

                                shieldDamageMultiplier = 1.5f;
                                buildingDamageMultiplier = 2;

                                splashDamageRadius = 72;
                                splashDamage = damage;
                                trailWidth = 5;
                                trailLength = 15;

                                hitSize = 32;
                                lifetime = 408 / speed;
                                knockback = 5;
                                frontColor = WHPal.ShootOrangeLight;
                                hitColor = backColor = trailColor = WHPal.ShootOrange;
                                trailChance = 0.75f;
                                trailInterval = 1f;
                                trailEffect = WHFx.square(hitColor, 45, 2, 25, 6);
                                shrinkY = 0;
                                hitShake = 15;
                                despawnEffect = hitEffect = new MultiEffect(
                                WHFx.circleOut(hitColor, 20, splashDamageRadius),
                                WHFx.generalExplosion(20, hitColor, splashDamageRadius, 8, true),
                                lineCircleOut(hitColor, 30, 70, 4).startDelay(20f),
                                WHFx.trailHitSpark(hitColor, 120, 10, 120, 2, 15),
                                WHFx.hitSpark(hitColor, 120, 30, 120, 2f, 15),
                                WHFx.blast(hitColor, 60)
                                );
                                hitSound = explosionPlasmaSmall;
                                despawnSound = explosionTitan;
                                shootEffect = new MultiEffect(
                                shootBigColor,
                                WHFx.shootLine(45, 30).followParent(true),
                                WHFx.lineCircleOut(hitColor, 30, 30, 2));
                                fragBullets = 10;
                                fragBullet = new BasicBulletType(3.3f, 80, "circle"){
                                    {
                                        width = height = 9;

                                        trailWidth = 3;
                                        trailLength = 12;

                                        collides = false;
                                        lifetime = 30;
                                        drag = 0.06f;
                                        splashDamage = damage;
                                        splashDamageRadius = 40;
                                        buildingDamageMultiplier = 2;

                                        frontColor = WHPal.ShootOrangeLight;
                                        lightningColor = trailColor = hitColor = backColor = WHPal.ShootOrange;


                                        hitSound = explosionPlasmaSmall;
                                        despawnEffect = none;
                                        hitEffect = WHFx.generalExplosion(30, hitColor, 30, 8, false);
                                    }
                                };
                            }
                        };
                    }
                });

                weapons.add(new Weapon(name("small-cannon-2")){
                    {
                        reload = 90;
                        x = 0;
                        y = -9 / 4f;
                        shootY = 75 / 4f;
                        rotate = true;
                        rotateSpeed = 1f;
                        rotationLimit = 70f;
                        layerOffset = 0.0001f;
                        recoil = 2;
                        recoilTime = 45f;
                        mirror = false;
                        inaccuracy = 0;
                        shootSound = shootRipple;
                        heatColor = WHPal.Heat;
                        cooldownTime = 45f;
                        shoot = new ShootAlternate(){{
                            barrels = 2;
                            spread = 36 / 4f;
                            shots = 2;
                        }};
                        bullet = new TrailFadeBulletType(10, 300, name("pierce")){
                            {
                                width = 10;
                                height = width * 1.5f;
                                trailWidth = width / 3.5f;
                                trailLength = 5;

                                lifetime = 400 / speed;
                                splashDamageRadius = 40;
                                splashDamage = 100;
                                lightningDamage = 40;
                                lightning = 3;
                                lightningLength = 8;
                                lightningLengthRand = 6;

                                frontColor = WHPal.ShootOrangeLight;
                                hitColor = lightningColor = backColor = trailColor = WHPal.ShootOrange;

                                smokeEffect = WHFx.hugeSmokeGray;
                                shootEffect = new MultiEffect(
                                shootBigColor,
                                WHFx.shootLineSmall(WHPal.ShootOrange)
                                );

                                hitShake = 3;
                                hitSound = explosionPlasmaSmall;
                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.explosionSmokeEffect(hitColor, 60, splashDamageRadius, 10, 10),
                                new Effect(30, e -> {
                                    color(hitColor, e.fout() * 0.7F);
                                    randLenVectors(e.id, 3, 2f + 40f * e.finpow(), (x, y) -> {
                                        stroke(3f * e.fout());
                                        Lines.circle(e.x, e.y, 45f + e.finpow() * 25f);
                                    });

                                    randLenVectors(e.id, 2, 2f + 40f * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout(Interp.pow2Out) * Mathf.clamp(range / 15.0F, 3.0F, 14.0F)));

                                    color(hitColor);
                                    stroke(e.fout());

                                    randLenVectors(e.id + 1, 20, 15f + 23f * e.finpow(), (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 2f + e.fout() * 3f));

                                    Drawf.light(e.x, e.y, 70f, hitColor, 0.7f * e.fout());
                                }));
                            }
                        };
                        weapons.add(new Weapon(name("missile-launcher")){
                                        {
                                            x = 103 / 4f;
                                            y = -18 / 4f;
                                            reload = 25f;
                                            rotate = true;
                                            rotateSpeed = 1.5f;
                                            shootSound = WHSounds.missileShoot;
                                            inaccuracy = 6;
                                            alternate = true;
                                            shoot = new ShootAlternate(){{
                                                barrels = 3;
                                                spread = 17 / 4f;
                                                shots = 3;
                                                shotDelay = 6f;
                                            }};
                                            bullet = new CritMissileBulletType(6, 70, name("rocket")){
                                                {
                                                    pierceArmor = true;
                                                    width = 10;
                                                    height = width * 2.2f;
                                                    shrinkX = shrinkY = 0f;

                                                    lengthOffset = width - 2;
                                                    flameWidth = 2.5f;
                                                    flameLength = 15f;

                                                    weaveMagOnce = true;
                                                    weaveMag = 0.4f;
                                                    weaveRandom = true;
                                                    drawMissile = true;
                                                    lightningColor = hitColor = trailColor = WHPal.ShootOrange;

                                                    Color c = WHPal.ShootOrange.cpy().lerp(Color.orange, 0.25f);
                                                    colors = new Color[]{c.a(0.55f), c.a(0.7f), c.a(0.8f), c, Color.white};

                                                    lifetime = 380 / speed;
                                                    damage = 80;
                                                    trailLength = 6;
                                                    trailWidth = 1.5f;
                                                    statusDuration = 60;
                                                    homingDelay = lifetime / 2f;
                                                    homingPower = 0.01f;
                                                    followAimSpeed = 0.1f;

                                                    despawnEffect = hitEffect = new MultiEffect(
                                                    WHFx.generalExplosion(10, hitColor, 25, 5, false),
                                                    WHFx.instHit(hitColor, true, 2, 30f)
                                                    );
                                                    smokeEffect = hugeSmoke;
                                                    shootEffect = shootBigColor;
                                                    lightningDamage = 40;
                                                    lightning = 2;
                                                    lightningLength = 12;
                                                }
                                            };
                                        }
                                    }
                        );
                    }
                });
            }
        };

        Mecha5 = new UnitType("mecha5"){
            {
                constructor = MechUnit::create;

                speed = 0.8f;
                hitSize = 42;
                rotateSpeed = 1.65f;
                health = 35000;
                armor = 25f;
                mechStepParticles = true;
                stepShake = 0.75f;
                drownTimeMultiplier = 0.5f;
                mechFrontSway = 0.8f;
                mechSideSway = 0.5f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new ItemAmmoType(WHItems.molybdenumAlloy);
                range = 330;

                abilities.add(
                new EllipseForceFieldAbility(250 / 4f, 150 / 4f, 6, 3500, 30 * 60f, 0.5f, 18)
              /*  new ShockAbility(){{
                    bulletDamage = 400;
                    reload = 3 * 60f;
                    waveColor=WHPal.ShootOrange;
                }}*/);

                weapons.add(new Weapon(name("mecha5-weapon1")){
                                {
                                    reload = 20;
                                    x = 87 / 4f;
                                    y = 17 / 4f;
                                    shootY = 80 / 4f;
                                    layerOffset = -0.001f;
                                    rotationLimit = 35;
                                    rotateSpeed = 0.8f;
                                    rotate = true;
                                    shootCone = 20;
                                    shootSound = shootRipple;
                                    inaccuracy = 3;
                                    recoil = 6;
                                    bullet = new CritBulletType(12, 200, "missile-large"){
                                        {
                                            lifetime = 340 / speed;

                                            splashDamageRadius = 48;
                                            splashDamage = 100;
                                            lightning = 3;
                                            lightningDamage = 40;
                                            lightningLength = 6;
                                            lightningLengthRand = 5;
                                            frontColor = lightningColor = WHPal.ShootOrangeLight;

                                            width = 14;
                                            height = width * 2.5f;
                                            hitSound = explosion;
                                            hitShake = 3;
                                            hitColor = backColor = trailColor = WHPal.ShootOrange;
                                            trailParam = 2;
                                            trailWidth = 5;
                                            trailLength = 10;
                                            trailChance = 0.2f;
                                            trailInterval = 1;
                                            trailEffect = new Effect(50, e -> {
                                                Draw.color(e.color);
                                                Angles.randLenVectors(e.id, 1, -20 * e.finpow(), e.rotation, 80, (x, y) ->
                                                Fill.square(e.x + x, e.y + y, 5 * e.foutpow(), Mathf.randomSeed(e.id, 360) + e.time));
                                            });
                                            shootEffect = new MultiEffect(
                                            shootBigColor,
                                            shootSmokeSquare,
                                            WHFx.shootLine(40, 20).followParent(true)
                                            );

                                            hitEffect = new MultiEffect(
                                            WHFx.trailHitSpark(hitColor, 30, 10, 80, 1.5f, 12),
                                            WHFx.generalExplosion(15, hitColor, 80, 10, false),
                                            WHFx.lineCircleOut(hitColor, 30, 60, 3),
                                            WHFx.hitSpark(hitColor, 30, 15, 70, 2, 12),
                                            new Effect(30, e -> {
                                                Draw.color(WHPal.ShootOrange);
                                                randLenVectors(e.id, 4, 30f * e.finpow(), (x, y) -> {
                                                    Fill.square(e.x + x, e.y + y, 8f * e.fout(), 45);
                                                    Drawf.light(e.x + x, e.y + y, e.fout() * 6f, e.color, 0.7f);
                                                });
                                            }));

                                            fragBullets = 4;
                                            fragLifeMin = 0f;
                                            fragRandomSpread = 30f;

                                            fragBullet = new BasicBulletType(6, 60){{
                                                width = 10f;
                                                height = width * 2.5f;
                                                pierce = true;
                                                pierceCap = 3;
                                                trailWidth = 2f;
                                                trailLength = 5;

                                                frontColor = lightningColor = WHPal.ShootOrangeLight;
                                                hitColor = backColor = trailColor = WHPal.ShootOrange;

                                                trailEffect = WHFx.square(hitColor, 15, 2, 20, 6);
                                                targetInterval = 1;

                                                lifetime = 25f;

                                                splashDamage = 40;
                                                splashDamageRadius = 20;

                                                hitEffect = WHFx.generalExplosion(15, hitColor, splashDamageRadius, 5, false);
                                            }};
                                        }
                                    };
                                }
                            }
                );

                Weapon mecha5Laser = new Weapon(name("gun-mount")){
                    {
                        shootY = 22 / 4f;
                        reload = 110;
                        rotate = true;
                        rotateSpeed = 1.6f;
                        shootSound = shootLaser;
                        recoil = 1;
                        bullet = new LightingLaserBulletType(){
                            {
                                lifetime = 35f;
                                damage = 90;
                                width = 18;
                                length = 260;
                                sideAngle = 30;
                                sideWidth = 2;
                                sideLength = 15;
                                pierce = true;
                                pierceCap = 3;
                                hitColor = lightningColor = WHPal.ShootOrange;
                                Color c = WHPal.ShootOrange.cpy();
                                colors = new Color[]{c.a(0.4f), c.a(0.7f), c, Color.white};
                                hitEffect = WHFx.square(hitColor, 30, 5, 30, 6);
                            }
                        };
                    }
                };

                weapons.addAll(
                copyAndMove(mecha5Laser, 36 / 4f, 28 / 4f),
                copyAndMoveAnd(mecha5Laser, 57 / 4f, -21 / 4f, b -> b.reload = 90)
                );
            }
        };

        Mecha4 = new UnitType("mecha4"){
            {
                constructor = MechUnit::create;

                speed = 0.7f;
                hitSize = 32f;
                rotateSpeed = 1.4f;
                health = 22000;
                armor = 18f;
                mechFrontSway = 1f;
                ammoType = new ItemAmmoType(WHItems.sealedPromethium);

                stepShake = 0.15f;
                drownTimeMultiplier = 0.5f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                abilities.addAll(
                new PcShieldArcAbility(){{
                    whenShooting = false;
                    pushUnits = true;
                    x = -3f;
                    radius = 160 * 0.9f / 4f;
                    angleOffset = 90;
                    max = 2000;
                    regen = 5;
                    cooldown = 45 * 60f;
                    angle = 60;
                    width = 8f;
                    chanceDeflect = 0.15f;
                }},
                new PcShieldArcAbility(){{
                    whenShooting = false;
                    pushUnits = true;
                    x = 3f;
                    radius = 160 * 0.9f / 4f;
                    angleOffset = -90;
                    max = 2000;
                    regen = 5;
                    cooldown = 45 * 60f;
                    angle = 60;
                    width = 8f;
                    chanceDeflect = 0.15f;
                }},
                new PcShieldArcAbility(){{
                    y = -9f;
                    radius = 160 * 0.9f / 4f;
                    max = 3000;
                    regen = 7;
                    cooldown = 45 * 60f;
                    angle = 80;
                    width = 6f;
                    chanceDeflect = 0.15f;
                }});

                weapons.add(new Weapon(name("mecha4-weapon1")){
                    {
                        x = 36 / 4f;
                        y = -8 / 4f;
                        reload = 240;
                        cooldownTime = 100;
                        mirror = false;
                        shootY = 60 / 4f;
                        recoil = 5;
                        parentizeEffects = true;
                        rotate = true;
                        rotateSpeed = 0.8f;
                        rotationLimit = 90f;
                        shoot.firstShotDelay = 70f;
                        bullet = new BasicBulletType(4, 200, "large-orb"){{
                            collidesAir = hittable = false;
                            height = width = 36;
                            drag = -0.01f;
                            lifetime = 76.9f;//320
                            shrinkY = shrinkX = 0;
                            trailLength = 11;
                            trailWidth = 4;
                            frontColor = WHPal.SkyBlueF;
                            hitColor = trailColor = backColor = WHPal.SkyBlue;

                            splashDamageRadius = 80;
                            splashDamage = 300;
                            shieldDamageMultiplier = 4f;
                            buildingDamageMultiplier = 1.3f;

                            chargeEffect = new MultiEffect(
                            WHFx.trailCharge(frontColor, 12, 2, 100, 2, shoot.firstShotDelay + 1).followParent(true),
                            WHFx.genericChargeCircle(hitColor, 8, splashDamageRadius * 0.5f, shoot.firstShotDelay + 1).followParent(true)
                            );
                            trailEffect = WHFx.hitPoly(hitColor, hitColor, 60, 2, 20, 5, 6, 60f);
                            trailInterval = 2;
                            shootEffect = new MultiEffect(
                            Fx.shootBigColor, Fx.colorSparkBig);

                            hitShake = 4;
                            bulletInterval = 2;
                            intervalBullets = 1;
                            intervalRandomSpread = 360;
                            intervalSpread = 20;
                            intervalAngle = 0;
                            chargeSound = chargeLancer;
                            shootSound = shootMissilePlasma;
                            intervalBullet = new LightningBulletType(){
                                {
                                    damage = 36;
                                    lightningColor = WHPal.SkyBlue;
                                    lightningLength = 4;
                                    lightningLengthRand = 3;
                                    hitEffect = hitLancer;
                                    despawnEffect = none;
                                    lightningDamage = 45;
                                    lightning = 2;
                                    shieldDamageMultiplier = 4f;
                                }
                            };
                            hitEffect = new MultiEffect(
                            WHFx.generalExplosion(120, hitColor, splashDamageRadius, 20, true),
                            WHFx.instRotation(hitColor, 120, splashDamageRadius + 20, 45, true),
                            WHFx.circleOut(hitColor, 120, splashDamageRadius),
                            WHFx.trailCircleHitSpark(hitColor, 120, 20, splashDamageRadius * 1.5f, 2, 12)
                            /*new Effect(120F, (e) -> {
                                float rad = 22.5F;
                                rand.setSeed(e.id);
                                Draw.color(Color.white, WHPal.SkyBlue, e.fin() + 0.6F);
                                float circleRad = e.fin(Interp.circleOut) * rad * 4.0F;
                                Lines.stroke(6.0F * e.fout());
                                Lines.circle(e.x, e.y, circleRad);

                                for(int i = 0; i < 4; i++){
                                    Drawf.tri(e.x, e.y, 6f, 110 * e.fout(), i * 90);
                                }

                                color();
                                for(int i = 0; i < 4; i++){
                                    Drawf.tri(e.x, e.y, 3f, 70 * e.fout(), i * 90);
                                }

                                for(int i = 0; i < 8; ++i){
                                    Draw.color(Color.white, WHPal.SkyBlue, e.fin() + 0.6F);
                                    Tmp.v1.set(1.0F, 0.0F).setToRandomDirection(rand).scl(circleRad);
                                    Drawn.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 8.0F, circleRad / 6.0F) * e.fout(), rand.random(circleRad / 2, circleRad / 1.3f) * (1.0F + e.fin()) / 2.0F, Tmp.v1.angle() - 180.0F);
                                }
                                Draw.blend(Blending.additive);
                                Draw.z(110.1F);
                                Fill.light(e.x, e.y, Lines.circleVertices(circleRad), circleRad, Color.clear, Tmp.c1.set(WHPal.SkyBlue).a(e.fout(Interp.pow10Out)));
                                Drawf.light(e.x, e.y, rad * e.fout(Interp.circleOut) * 4.0F, WHPal.SkyBlue, 0.7F);
                                Draw.blend();
                            })*/);
                            fragBullets = 5;
                            fragLifeMin = 0.6f;
                            fragLifeMax = 1.5f;
                            fragVelocityMin = 0.6f;
                            fragVelocityMax = 1.5f;
                            fragRandomSpread = 90f;
                            fragBullet = new BasicBulletType(3, 80, "large-orb"){{
                                hitSound = explosionPlasmaSmall;
                                lifetime = 20;
                                splashDamagePierce = true;
                                splashDamageRadius = 40;
                                splashDamage = 100;
                                buildingDamageMultiplier = 0.6f;
                                lightningLength = 12;
                                lightningDamage = 50;
                                lightning = 1;
                                shieldDamageMultiplier = 3;
                                height = width = 16;
                                sprite = "large-orb";
                                shrinkY = 0;
                                hitShake = 4;
                                frontColor = WHPal.SkyBlueF;
                                lightningColor = hitColor = backColor = WHPal.SkyBlue;
                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.lineCircleOut(WHPal.SkyBlue, 40, 30, 3f),
                                WHFx.smoothColorCircle(WHPal.SkyBlue, 30, 40),
                                WHFx.blast(WHPal.SkyBlue, 40));
                            }};
                        }};
                    }
                });
                weapons.add(new Weapon(name("mecha4-weapon2")){
                    {
                        x = 72 / 4f;
                        y = 3 / 4f;
                        reload = 80;
                        shoot = new ShootMulti(new ShootAlternate(){{
                            barrels = 5;
                            spread = 1.2f;
                        }},
                        new ShootSpread(5, 2){{
                            shotDelay = 6;
                        }});
                        shootSound = shootCyclone;
                        shootY = (60 - 10) / 4f;
                        recoil = 5;
                        inaccuracy = 3;
                        rotate = false;
                        shootCone = 15f;
                        layerOffset = -0.001f;
                        xRand = 0.1f;
                        velocityRnd = 0.1f;
                        bullet = new CritBulletType(25, 90, name("pierce")){{
                            lifetime = 22;
                            drag = 0.06f;
                            speed = 25;
                            width = 15;
                            height = width * 2f;
                            frontColor = WHPal.ShootOrangeLight;
                            trailColor = hitColor = backColor = WHPal.ShootOrange;
                            trailWidth = width / 3;
                            trailLength = 6;
                            pierceCap = 2;
                            pierce = true;
                            pierceArmor = true;
                            pierceBuilding = pierceArmor = true;
                            shootEffect = new MultiEffect(
                            shootBigColor, WHFx.shootLine(20, 20)
                            );
                            smokeEffect = shootBigSmoke;
                            hitEffect = despawnEffect = new MultiEffect(
                            WHFx.square(hitColor, 30, 5, 30, 6),
                            hitBulletColor
                            );
                            fragBullets = 1;
                            fragBullet = new BasicBulletType(0, 70, name("square")){{
                                hitSound = explosionPlasmaSmall;
                                hittable = collidesAir = collides = false;
                                speed = 0;
                                lifetime = 30;
                                splashDamage = damage;
                                splashDamageRadius = 32;
                                shrinkY = shrinkX = 0;
                                width = height = 5;
                                frontColor = trailColor = hitColor = backColor = WHPal.ShootOrange;
                                trailEffect = WHFx.square(hitColor, 15, 2, 30, 4);
                                trailChance = 0.16f;
                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.generalExplosion(20, hitColor, splashDamageRadius, 10, false)
                                );
                            }};

                        }};
                    }
                });
            }
        };

        Mecha3 = new UnitType("mecha3"){
            {
                constructor = MechUnit::create;

                speed = 1f;
                hitSize = 26f;
                rotateSpeed = 2.3f;
                health = 8000;
                armor = 12f;
                mechFrontSway = 0.55f;
                drownTimeMultiplier = 0.5f;
                ammoType = new ItemAmmoType(WHItems.ceramite);

                immunities.addAll(StatusEffects.burning, WHStatusEffects.plasmaFireBurn);

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                abilities.add(new PcShieldArcAbility(){{
                    whenShooting = false;
                    y = -8;
                    radius = 30;
                    max = 4000;
                    regen = 4;
                    cooldown = 30 * 60f;
                    angle = 130;
                    width = 6f;
                    chanceDeflect = 0.15f;
                }});

                weapons.add(new MarkWeapon(name("mecha3-weapon1")){
                    {

                        x = 69 / 4f;
                        y = -1f / 4f;
                        reload = 90;
                        shoot = new ShootAlternate(){{
                            barrels = 2;
                            spread = 8 / 4f;
                            shots = 3;
                            shotDelay = 8;
                        }};
                        ShootAlternate shoot2 = (ShootAlternate)shoot.copy();
                        shoot2.shots = 5;
                        markShoot = shoot2;
                        markTime = 60;
                        markChance = 0.1f;

                        shootY = 60 / 4f;
                        recoil = 4;
                        recoilTime = 60f;
                        inaccuracy = 5;
                        shootSound = explosion;
                        rotate = false;
                        layerOffset = -0.0001f;
                        alternate = true;
                        xRand = 0.3f;
                        velocityRnd = 0.05f;
                        markBullet = bullet = new CritBulletType(8, 80, "circle-bullet"){
                            {
                                buildingDamageMultiplier = 2f;
                                makePlaFire = true;
                                plaFireChance = 0.08f;
                                lifetime = 260 / speed;

                                splashDamageRadius = 40;
                                splashDamage = 90;
                                frontColor = WHPal.SkyBlueF;
                                hitColor = trailColor = backColor = WHPal.SkyBlue;
                                shrinkX = shrinkY = 0;
                                trailLength = 7;
                                height = width = 7;
                                trailWidth = width / 2.2f;
                                status = WHStatusEffects.plasmaFireBurn;
                                statusDuration = 15;

                                trailEffect = WHFx.hitPoly(hitColor, backColor, 20, 2, 20, 5, 6, 60f);
                                trailChance = 0.18f;

                                despawnEffect = hitEffect = new MultiEffect(
                                WHFx.hitPoly(hitColor, backColor, 30, 5, 60, 6, 6, 60f),
                                WHFx.hitSpark(WHPal.SkyBlue, 45, 10, 60, 1, 6),
                                WHFx.generalExplosion(15f, hitColor, 25, 4, false),
                                WHFx.lineCircleOut(WHPal.SkyBlue, 15, 50, 2)
                                );
                                shootEffect = new MultiEffect(
                                WHFx.lineCircleOut(WHPal.SkyBlue, 16, 30, 2),
                                WHFx.hitPoly(hitColor, backColor, 10, 3, 20, 6, 6, 60f),
                                shootBigColor);
                                smokeEffect = shootBigSmoke;
                            }
                        };
                    }
                });
                weapons.add(new Weapon(name("machine-gun-2")){
                    {
                        x = 48 / 4f;
                        y = -23 / 4f;
                        reload = 14;
                        shootY = 37 / 4f;
                        rotate = true;
                        rotateSpeed = 1.2f;
                        heatColor = WHPal.SkyBlue;
                        cooldownTime = 30;

                        recoil = 1;
                        shootSound = shootFlame;
                        bullet = new BasicBulletType(8, 60){
                            {
                                hitSize = 10;
                                pierce = true;
                                pierceBuilding = true;
                                pierceCap = 3;
                                hittable = false;
                                status = WHStatusEffects.plasmaFireBurn;
                                statusDuration = 10;
                                lifetime = 180 / speed;

                                hitColor = trailColor = backColor = WHPal.SkyBlue;
                                width = 8;
                                height = width * 2.5f;

                                shootEffect = new Effect(32f, 80f, e -> {
                                    rand.setSeed(e.id);
                                    color(WHPal.SkyBlueF, WHPal.SkyBlue, Color.gray, e.fin());

                                    randLenVectors(e.id, 25, e.finpow() * (speed * lifetime - 10), e.rotation, 5, (x, y) -> {
                                        Fill.poly(e.x + x, e.y + y, 6, e.fout() * 4.3f * 0.85F, 60 + rand.random(360) * e.fin());
                                    });
                                });
                                despawnEffect = hitEffect = new Effect(12, e -> {
                                    color(hitColor);
                                    stroke(e.fout() * 2.2f);
                                    randLenVectors(e.id, 8, e.finpow() * 10f, e.rotation, 10f, (x, y) -> {
                                        Fill.poly(e.x + x, e.y + y, 6, e.fout(Interp.pow10In) * 4.3f * 0.85F, 60 + rand.random(360) * e.fin());
                                        lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.finpow() * 3f + 1f);
                                    });
                                });
                            }
                        };
                    }
                });
            }
        };

        Mecha2 = new UnitType("mecha2"){
            {
                constructor = LegsUnit::create;

                speed = 1.4f;
                drag = 0.1f;
                hitSize = 21f;
                rotateSpeed = 1.3f;
                health = 3000;
                armor = 8f;
                stepShake = 0f;

                legCount = 4;
                legLength = (60 + 59) / 4f * 0.7f;
                legGroupSize = 2;
                lockLegBase = true;
                legContinuousMove = true;
                legExtension = -3f;
                legBaseOffset = 7f;
                legMaxLength = 1.1f;
                legMinLength = 0.2f;
                legLengthScl = 0.95f;
                legForwardScl = 0.9f;

                legMoveSpace = 1f;
                hovering = true;

                shadowElevation = 0.2f;
                groundLayer = Layer.legUnit - 1f;
                drownTimeMultiplier = 0.5f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                weapons.add(new Weapon(name("mecha2-weapon1")){
                    {
                        x = -53 / 4f;
                        y = 7 / 4f;
                        shootY = 8.5f;
                        layerOffset = -0.001f;
                        mirror = false;
                        rotate = false;
                        rotationLimit = 45;
                        shootSound = shootMissile;
                        reload = 240;
                        cooldownTime = 180;
                        heatColor = WHPal.Heat;
                        shootCone = 10;
                        recoil = 5;
                        bullet = new BasicBulletType(4, 200, name("large-missile")){{
                            splashDamageRadius = 50;
                            splashDamage = 150;
                            lifetime = 62.1f;//300
                            status = blasted;
                            statusDuration = 60;
                            width = 25;
                            height = width * 3;
                            homingDelay = 16f;
                            homingPower = 0.003f;
                            followAimSpeed = 1f;
                            trailLength = 10;
                            trailWidth = 2.5f;
                            frontColor = WHPal.ShootOrangeLight;
                            backColor = hitColor = trailColor = WHPal.ShootOrange;
                            trailEffect = smoke;
                            hitShake = 1;
                            hitEffect = despawnEffect = new MultiEffect(
                            lineCircleOut(hitColor, 20, splashDamageRadius * 0.85f, 4).startDelay(5f),
                            lineCircleOut(hitColor, 20, splashDamageRadius * 0.85f, 2).startDelay(10f),
                            WHFx.hitSpark(hitColor, 20, 30, 50, 1, 10),
                            WHFx.generalExplosion(15, hitColor, splashDamageRadius * 0.85f, 10, true));
                            shootEffect = shootBigSmoke;
                            smokeEffect = Fx.shootSmokeTitan;
                        }};
                    }
                });
                weapons.add(new Weapon(name("mecha2-weapon2")){
                    {
                        reload = 45;
                        recoil = 3;
                        shoot.shots = 3;
                        shoot.shotDelay = 4;
                        x = 49 / 4f;
                        y = -2 / 4f;
                        shootY = 64 / 4f;
                        mirror = false;
                        rotate = false;
                        inaccuracy = 3;
                        shootSound = shootSpectre;
                        ejectEffect = casing4;
                        layerOffset = -0.0001f;
                        bullet = new CritBulletType(8, 80){
                            {
                                hitShake = 1;
                                lifetime = 220 / speed;
                                shootEffect = shootBig2;
                                smokeEffect = shootBigSmoke;
                                hitSound = explosion;
                                width = 6;
                                height = width * 2.5f;
                                frontColor = WHPal.ShootOrangeLight;
                                trailLength = 5;
                                trailWidth = width / 2.7f;
                                hitColor = backColor = trailColor = WHPal.ShootOrange;
                                hitEffect = despawnEffect = new MultiEffect(
                                hitBulletColor, hitSquaresColor
                                );
                            }
                        };
                    }
                });
            }
        };

        air7 = new SuperHeavyUnitType("air7"){
            {
                constructor = UnitTypes.eclipse.constructor;

                flying = true;
                speed = 0.85f;
                rotateSpeed = 0.5f;
                hitSize = 100;
                health = 190 * 1000;
                armor = 40;

                engineOffset = 310 / 4f;
                engineSize = 14;
                lowAltitude = true;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                for(int a = 0; a < 10; a++){
                    AncientEngine.addEngine(this, -193 / 4f - a * 3 / 4f, -27 / 4f, 0, 2, true);
                    AncientEngine.addEngine(this, -263 / 4f - a * 3 / 4f, -243 / 4f, 0, 2, true);
                }

                Weapon air7Laser1 = new Weapon(name("energy-cannon")){
                    {
                        rotate = true;
                        rotateSpeed = 1f;
                        reload = 240;
                        shootY = 30 / 4f;
                        shootSound = beamMeltdown;
                        alternate = true;

                        continuous = true;
                        bullet = new LightingContinuousLaserBullet(2800 / 10f){{

                            damageInterval = 6f;
                            pierceCap = 4;
                            hitColor = lightColor = WHPal.ShootOrange.cpy();

                            lifetime = 180;
                            length = 400;

                            width = 6;

                            colors = new Color[]{hitColor.cpy().a(0.4f), hitColor.cpy().a(0.6f), hitColor.cpy().a(0.8f), Pal.coalBlack};

                            smokeEffect = Fx.shootSmallSmoke;
                            despawnEffect = hitEffect = Fx.hitMeltdown;
                        }};
                    }
                };

                Weapon air7MainWeapon = new Weapon(){

                    @Override
                    public void draw(Unit unit, WeaponMount mount){
                        float z = Draw.z();

                        Tmp.v1.trns(unit.rotation, y);
                        float f = 1 - mount.reload / reload;
                        float rad = 12f;

                        float f1 = Mathf.curve(f, 0.4f, 1f);
                        Draw.z(Layer.bullet);
                        Draw.color(heatColor);

                        Tmp.v6.set(mount.aimX, mount.aimY).sub(unit);
                        Tmp.v2.set(mount.aimX, mount.aimY).sub(unit).nor().scl(Math.min(Tmp.v6.len(), range())).add(unit);

                        Lines.stroke((1.5f + Mathf.absin(Time.time + 4, 8f, 1.5f)) * f1, heatColor);
                        Lines.square(Tmp.v2.x, Tmp.v2.y, 4 + Mathf.absin(8f, 4f), 45);

                        rand.setSeed(unit.id);
                        for(int i = 1; i < 3; i++){
                            float a = rand.random(0, 90);
                            float radius = rad + (i / 2f) * rad;
                            float r = (Time.time % 360f) / i;
                            arc(Tmp.v2.x, Tmp.v2.y, radius * f, 0.1f + i / 15f, r + a);
                            arc(Tmp.v2.x, Tmp.v2.y, radius * f, 0.1f + i / 15f, r + a - 180);
                        }


                        for(int i : Mathf.signs){
                            for(int j : Mathf.signs){
                                Drawn.tri(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f1 * rad / 3f + Mathf.num(j > 0) * 2f * (f1 + 1) / 2, (rad * 3f + Mathf.num(j > 0) * 20f) * f1, j * Time.time + 90 * i);
                            }
                        }

                        Lines.stroke(rad / 2.5f * mount.heat, heatColor);
                        Lines.circle(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, rad * 2 * (1 - mount.heat));

                        Draw.color(heatColor);
                        Fill.circle(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f * rad);
                        Lines.stroke(f * 1.5f);
                        Drawn.circlePercentFlip(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f * rad + 5, Time.time, 20f);
                        Draw.color(Color.white);
                        Fill.circle(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, f * rad * 0.7f);

                        Draw.z(z);
                    }

                    public Effect ellipseHalo = new Effect(60, e -> {
                        Draw.color(heatColor);
                        rand.setSeed(e.id);
                        Draw.z(Layer.bullet + 0.001f);
                        Lines.stroke(e.fout(Interp.smooth) * 5f);
                        float random1 = rand.random(0.2f, 1) * e.fin(Interp.pow2Out);
                        float random2 = rand.random(360);
                        Lines.ellipse(e.x, e.y, 150, 0.2f * random1, 0.6f * random1, random2);
                    }).followParent(true);

                    @Override
                    public void update(Unit unit, WeaponMount mount){
                        super.update(unit, mount);
                        Tmp.v1.trns(unit.rotation, y);
                        if(Mathf.chanceDelta(0.03f) && mount.reload <= 0.01f){
                            ellipseHalo.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, mount.rotation, unit);
                        }
                    }

                    @Override
                    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
                        shootSound.at(shootX, shootY, Mathf.random(soundPitchMin, soundPitchMax));

                        BulletType ammo = bullet;
                        float lifeScl = ammo.scaleLife ? Mathf.clamp(Mathf.dst(shootX, shootY, mount.aimX, mount.aimY) / ammo.range) : 1f;

                        Tmp.v6.set(mount.aimX, mount.aimY).sub(unit);
                        Tmp.v1.set(mount.aimX, mount.aimY).sub(unit).nor().scl(Math.min(Tmp.v6.len(), range())).add(unit);

                        Bullet b = bullet.create(unit, unit.team, Tmp.v1.x, Tmp.v1.y, 0);
                        b.vel.setZero();
                        b.set(Tmp.v1);
                        unit.apply(shootStatus, shootStatusDuration);

                        if(Vars.headless) return;
                        Vec2 vec2 = new Vec2().trns(unit.rotation, y).add(unit);

                        for(int i = 0; i < 5; i++){
                            Time.run(i * 6f, () -> {
                                PositionLightning.createEffect(vec2, b, heatColor, 3, 2.5f);
                                WHFx.chainLightningFade.at(vec2.x, vec2.y, Mathf.random(8, 14), heatColor, b);
                            });
                        }

                        ammo.shootEffect.at(shootX, shootY, rotation);
                        ammo.smokeEffect.at(shootX, shootY, rotation);
                    }

                    {
                        y = 0;
                        x = 0;
                        shootY = 0;
                        shoot = new ShootPattern();
                        reload = 1500;
                        rotate = true;
                        top = false;
                        mirror = alternate = predictTarget = false;
                        heatColor = WHPal.ShootOrange;
                        shootSound = shootForeshadow;

                        float r = range = 360;

                        bullet = new EffectBulletType(360){

                            @Override
                            protected float calculateRange(){
                                return r;
                            }

                            @Override
                            public void despawned(Bullet b){
                                super.despawned(b);

                                Vec2 vec = new Vec2().set(b);

                                float damageMulti = b.damageMultiplier();
                                Team team = b.team;
                                for(int i = 0; i < 18; i++){
                                    int finalI = i;
                                    Time.run(i * (despawnEffect.lifetime - 120) / 15, () -> {
                                        Damage.damage(team, vec.x, vec.y, (splashDamageRadius / 15) * finalI, splashDamage * damageMulti, finalI < 10);
                                    });
                                }

                                Units.nearby(team, vec.x, vec.y, splashDamageRadius, u -> {
                                    u.heal(Math.max((1 - u.healthf()) / 7 * u.maxHealth(), 1500));
                                    u.apply(WHStatusEffects.powerEnhance1, 300f);
                                });

                                Units.nearbyEnemies(team, vec.x, vec.y, splashDamageRadius, u -> {
                                    u.apply(StatusEffects.melting, 300f);
                                    u.apply(WHStatusEffects.powerReduce2, 600f);
                                });

                                float rad = 120;
                                float spacing = 2.5f;

                                for(int k = 0; k < (despawnEffect.lifetime - WHFx.chainLightningFadeReversed.lifetime) / spacing; k++){
                                    Time.run(k * spacing, () -> {
                                        for(int j : Mathf.signs){
                                            Vec2 v = Tmp.v6.rnd(rad * 2 + Mathf.random(rad * 4)).add(vec);
                                            Drawn.randFadeLightningEffect(vec.x, vec.y, rad + Mathf.random(rad * 2), 12, hitColor, j
                                            > 0);
                                            /* (j > 0 ? WHFx.chainLightningFade : WHFx.chainLightningFadeReversed).at(v.x, v.y, 12f, hitColor, vec);*/
                                        }
                                    });
                                }
                            }

                            Effect surroundEffect1 = new Effect(90, 500, e -> {
                                Draw.color(e.color);
                                rand.setSeed(e.id);
                                float s = rand.random(1f) > 0.5f ? 1 : -1;
                                float rand1 = rand.random(1f);
                                float rand2 = rand.random(90f, 360f);
                                float range = 250;
                                float cur = Mathf.curve(e.fin(), 0, rand.random(0.2f, 0.3f));
                                Draw.z(Layer.effect);
                                Angles.randLenVectors(e.id, 1, rand1 * cur * range,
                                rand2 * e.fin() * s, range, rand1 * range / 32f, (x, y) -> {
                                    Fill.circle(e.x + x, e.y + y, e.fout(Interp.pow3Out) * 6 * cur);
                                });
                            });


                            Effect surroundEffect2 = new Effect(90, 500, e -> {
                                if(!(e.data instanceof Float data)) return;
                                rand.setSeed(e.id);
                                float rand1 = rand.random(0.5f, 1);
                                float rand2 = rand.random(-1, 1);
                                float range = 250;
                                float cur = Mathf.curve(e.fin(), 0, 0.15f);
                                Draw.z(Layer.effect);
                                Tmp.v1.trns(e.rotation + 15 * e.fout(Interp.pow2Out) + 30 * e.fout(Interp.smooth) + 5 * rand2 * e.fout(Interp.smooth), data * range * rand1 * e.fout(Interp.pow2Out));
                                float x = Tmp.v1.x, y = Tmp.v1.y;
                                Draw.color(e.color);
                                Fill.circle(e.x + x, e.y + y, e.fout(Interp.pow3Out) * 5 * cur * rand1);
                                Draw.color(Pal.coalBlack);
                                Fill.circle(e.x + x, e.y + y, e.fout(Interp.pow3Out) * 2.5f * cur * rand1);
                                Draw.z(Layer.bullet - 0.01f);
                                Draw.color(Pal.coalBlack);
                                Fill.circle(e.x + x, e.y + y, e.fout(Interp.pow3Out) * 3 * cur * rand1);

                            });

                            Effect surroundEffect3 = new TrailEffect(60, 500, WHPal.ShootOrange, WHPal.ShootOrange, 1, 15, 2)
                            .trailUpdater((e, trail, x, y, w, len, index) -> {
                                rand.setSeed(e.id);
                                float range = 300;
                                float rand1 = rand.random(0.5f, 1f);
                                float cur = Mathf.curve(e.fin(), 0, 0.15f);
                                Draw.z(Layer.effect);
                                Angles.randLenVectors(e.id, 1, range * rand1 * e.fout(), (x1, y1) -> {
                                    trail.length = (int)(cur * len);
                                    trail.update(x1 + x, y1 + y, w * e.fout());
                                });
                            });

                            Effect surroundEffect4 = new TrailEffect(60, 500, WHPal.ShootOrange, WHPal.ShootOrange, 1, 10, 2.5f)
                            .trailUpdater((e, trail, x, y, w, len, index) -> {
                                rand.setSeed(e.id);
                                float range = 300;
                                float rand1 = rand.random(0.5f, 1f);
                                float rand2 = rand.random(90f, 360f);
                                float cur = Mathf.curve(e.fin(), 0, 0.15f);
                                Draw.z(Layer.effect);
                                Angles.randLenVectors(e.id, 1, range * rand1 * e.fout(), rand2 * e.fout(), 360, (x1, y1) -> {
                                    trail.length = (int)(cur * len);
                                    trail.update(x1 + x, y1 + y, w * e.fout());
                                });
                            });

                            Effect surroundEffect5 = new TrailEffect(120, 500, WHPal.ShootOrange, WHPal.ShootOrange, 1, 12, 3)
                            .trailUpdater((e, trail, x, y, w, len, index) -> {
                                rand.setSeed(e.id);
                                float random = rand.random(0f, 0.5f);
                                float range = 300;
                                float cur = Mathf.curve(e.fin(), 0, 0.15f);
                                float
                                dx = WHUtils.dx(x, range * e.fout(Interp.pow2In) + random * range, e.time * 2f * rand.random(0.3f, 1.5f) + random * 360),
                                dy = WHUtils.dy(y, range * e.fout(Interp.pow2In) + random * range, e.time * 2f * rand.random(0.3f, 1.5f) + random * 360);
                                Draw.z(Layer.effect);
                                trail.length = (int)(cur * len);
                                trail.update(dx, dy, w * e.fout());
                            });

                            @Override
                            public void update(Bullet b){
                                updateBulletInterval(b);
                                float rad = 110;

                                Effect.shake(8 * b.fin(), 6, b);

                                if(b.timer(1, 6 + 10 * b.fout(Interp.pow2Out))){
                                    Seq<Teamc> entites = new Seq<>();

                                    Units.nearbyEnemies(b.team, b.x, b.y, rad * 2.5f * (1 + b.fin()) / 2, entites::add);

                                    Units.nearbyBuildings(b.x, b.y, rad * 2.5f * (1 + b.fin()) / 2, e -> {
                                        if(e.team != b.team) entites.add(e);
                                    });

                                    entites.shuffle();
                                    entites.truncate(10);

                                    for(Teamc e : entites){
                                        PositionLightning.create(b, b.team, b, e, lightningColor, false, lightningDamage,
                                        5 + Mathf.random(5), PositionLightning.WIDTH, 1, p -> {
                                            WHFx.lightningHitSmall.at(p.getX(), p.getY(), 0, lightningColor);
                                        });
                                    }
                                }

                                Vec2 vec = new Vec2().set(b);
                                Team team = b.team;
                                float damageMulti = b.damageMultiplier();
                                if(b.timer(2, b.lifetime / 10)){
                                    Damage.damage(team, vec.x, vec.y, splashDamageRadius * (0.1f + 0.5f * b.fin(Interp.pow2Out)), splashDamage * damageMulti, true);
                                }

                                if(b.lifetime() - b.time() > WHFx.chainLightningFadeReversed.lifetime)
                                    for(int i = 0; i < 2; i++){
                                        if(Mathf.chanceDelta(0.2 * Mathf.curve(b.fin(), 0, 0.8f))){
                                            for(int j : Mathf.signs){
                                                shootArc.at(b.x, b.y, 1f, 0.3f);
                                                Vec2 v = Tmp.v6.rnd(rad / 2 + Mathf.random(rad * 2) * (1 + Mathf.curve(b.fin(), 0, 0.9f)) / 1.5f).add(b);
                                                (j > 0 ? WHFx.chainLightningFade : WHFx.chainLightningFadeReversed).at(v.x, v.y, 12f, hitColor, b);
                                            }
                                        }
                                    }

                                if(b.fin() > 0.03f && Mathf.chanceDelta(b.fin() * 0.3f + 0.2 + 0.2 * Mathf.curve(b.fin(), 0.2f, 0.5f))){
                                    surroundEffect1.at(b.x, b.y, hitColor);
                                }

                                if(Mathf.chanceDelta(b.fin() * 0.15f + 0.2)){
                                    for(int i = 0; i < 4; i++){
                                        float angle = i * 90;
                                        surroundEffect2.at(b.x, b.y, angle, hitColor, Mathf.curve(b.fout(), 0, 0.15f));
                                    }
                                }

                                if(Mathf.chanceDelta(b.fin() * 0.2f + 0.1f + 0.25f * Mathf.curve(b.fin(), 0.5f, 1f))){
                                    surroundEffect3.at(b.x, b.y, hitColor);
                                }

                                if(b.fin() > 0.03f && Mathf.chanceDelta(b.fout() * 0.25f + 0.15f)){
                                    surroundEffect4.at(b.x, b.y, hitColor);
                                }

                                if(Mathf.chanceDelta(b.fin() * 0.1f + 0.02f + 0.3f * Mathf.curve(b.fin(), 0.5f, 1f))
                                && b.lifetime() - b.time() > surroundEffect5.lifetime){
                                    surroundEffect5.at(b.x, b.y, hitColor);
                                }

                                if(b.fin() > 0.7 && b.timer.get(3, b.lifetime * 0.12f)){
                                    Effect.shake(8, 15, b);
                                    WHFx.lineCircleIn(hitColor, 20, 400, 5).at(b.x, b.y, hitColor);
                                }

                                float fin = Mathf.curve(b.fin(), 0, 0.3f);
                                float f = fin * Mathf.curve(b.fout(), 0, 0.05f) * 3 * rad;

                                if(b.data instanceof Trail[] trail){
                                    for(int i = 0; i < 2; i++){
                                        if(trail[i] != null){
                                            float angle = i * 180 + b.time * 4;
                                            float tx = WHUtils.dx(b.x, f, angle);
                                            float ty = WHUtils.dy(b.y, f, angle);
                                            trail[i].update(tx, ty);
                                        }
                                    }
                                }
                            }

                            @Override
                            public void init(Bullet b){
                                super.init(b);
                                Trail[] trails = new Trail[2];
                                b.data = trails;
                                for(int i = 0; i < 2; i++){
                                    if(trails[i] == null){
                                        trails[i] = new Trail(16);
                                    }
                                }
                            }

                            @Override
                            public void draw(Bullet b){
                                float fin = Mathf.curve(b.fin(), 0, 0.02f);
                                float f = fin * Mathf.curve(b.fout(), 0f, 0.1f);
                                float rad = 110;

                                float z = Draw.z();

                                float circleF = (b.fout(Interp.pow2In) + 1) / 2;

                                Draw.color(hitColor);
                                Lines.stroke(rad / 20 * b.fin());
                                Lines.circle(b.x, b.y, rad * b.fout(Interp.pow3In));
                                /* Lines.circle(b.x, b.y, b.fin(Interp.circleOut) * rad * 3f * Mathf.curve(b.fout(), 0, 0.05f));*/

                                if(b.data instanceof Trail[] trail){
                                    for(int i = 0; i < 2; i++){
                                        if(trail[i] != null){
                                            trail[i].draw(hitColor, rad / 20 * b.fin());
                                            trail[i].drawCap(hitColor, rad / 20 * b.fin());
                                        }
                                    }
                                }

                                Rand rand = WHUtils.rand;
                                rand.setSeed(b.id);
                                Draw.color(hitColor);

                                for(int i = 0; i < (int)(rad / 5); i++){
                                    Tmp.v1.trns(rand.random(360f) + rand.range(1f) * rad / 5 * b.fin(Interp.pow2Out), rad / 2.05f * circleF + rand.random(rad * (1 + b.fin(Interp.circleOut)) / 1.8f));
                                    float angle = Tmp.v1.angle();
                                    Drawn.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, (b.fin() + 1) / 2 * 28 + rand.random(0, 8), rad / 16 * (b.fin(Interp.exp5In) + 0.25f), angle);
                                    Drawn.tri(b.x + Tmp.v1.x, b.y + Tmp.v1.y, (b.fin() + 1) / 2 * 12 + rand.random(0, 2), rad / 12 * (b.fin(Interp.exp5In) + 0.5f) / 1.2f, angle - 180);
                                }

                                Angles.randLenVectors(b.id + 1, (int)(rad / 4), rad / 4 * circleF, rad * (1 + b.fin(Interp.pow3Out)) / 3, (x, y) -> {
                                    float angle = Mathf.angle(x, y);
                                    Drawn.tri(b.x + x, b.y + y, rad / 8 * (1 + b.fout()) / 2.2f, (b.fout() * 3 + 1) / 3 * 25 + rand.random(4, 12) * (b.fout(Interp.circleOut) + 1) / 2, angle);
                                    Drawn.tri(b.x + x, b.y + y, rad / 8 * (1 + b.fout()) / 2.2f, (b.fout() * 3 + 1) / 3 * 9 + rand.random(0, 2) * (b.fin() + 1) / 2, angle - 180);
                                });

                                Drawf.light(b.x, b.y, rad * f * (b.fin() + 1) * 2, Draw.getColor(), 0.7f);

                                Draw.z(Layer.effect + 0.001f);
                                Draw.color(hitColor);
                                Fill.circle(b.x, b.y, rad * fin * circleF / 2f);
                                Draw.color(Pal.coalBlack);
                                Fill.circle(b.x, b.y, rad * fin * circleF * 0.75f / 2f);
                                Draw.z(Layer.bullet - 0.1f);
                                Draw.color(Pal.coalBlack);
                                Fill.circle(b.x, b.y, rad * fin * circleF * 0.8f / 2f);
                                Draw.z(z);
                            }

                            {
                                hittable = false;
                                collides = false;
                                collidesTiles = collidesAir = collidesGround = true;
                                speed = 0;

                                despawnHit = true;
                                keepVelocity = false;

                                splashDamageRadius = 300;
                                splashDamage = 350;

                                lightningDamage = 120;
                                lightning = 36;
                                lightningLength = 20;
                                lightningLengthRand = 20;

                                hitShake = despawnShake = 40f;
                                drawSize = clipSize = 800f;
                                hitColor = lightColor = trailColor = lightningColor = WHPal.ShootOrange;

                                fragBullets = 15;
                                fragBullet = WHBulletsOther.Air7MainWeaponFrag;
                                hitSound = explosionReactor;
                                hitSoundVolume = 4f;

                                fragLifeMax = 1.1f;
                                fragLifeMin = 0.7f;
                                fragVelocityMax = 1.2f;
                                fragVelocityMin = 0.4f;

                                bulletInterval = 15;
                                intervalBullets = 3;
                                intervalDelay = 100;
                                intervalBullet = new LightningBulletType(){{
                                    damage = 70;
                                    collidesAir = true;
                                    ammoMultiplier = 1f;
                                    lightningColor = WHPal.ShootOrange;
                                    lightningLength = 30;
                                    lightningLengthRand = 15;
                                }};

                                status = WHStatusEffects.melta;
                                statusDuration = 300f;

                                shootEffect = none;

                                hitEffect = WHFx.trailHitSpark(hitColor, 220, 100, splashDamageRadius, 3, 20);
                                despawnEffect = air7Explode;
                            }
                        };
                    }
                };

                weapons.add(new Weapon(name("small-cannon")){
                                {
                                    x = 124 / 4f;
                                    y = 108 / 4f;

                                    shootY = 64 / 4f;
                                    reload = 40;
                                    rotate = true;
                                    mirror = true;
                                    rotateSpeed = 1.5F;
                                    shootSound = shootMissile;
                                    recoil = 2.0F;
                                    shoot = new ShootAlternate(){
                                        {
                                            spread = 30 / 4f;
                                            shots = 2;
                                            shotDelay = 10;
                                            firstShotDelay = 15f;
                                        }
                                    };

                                    parentizeEffects = true;

                                    bullet = new LightingLaserBulletType(250){{
                                        recoil = 0.15f;
                                        Color color = hitColor = lightningColor = WHPal.ShootOrange.cpy();
                                        chargeEffect = WHFx.genericCharge(hitColor, 5, 30, shoot.firstShotDelay).followParent(true);
                                        lifetime = 30;
                                        length = 350;
                                        width = 30;
                                        pierceCap = 5;
                                        sideAngle = 90;
                                        sideLength = 15;
                                        colors = new Color[]{color.a(0.4f), color.a(0.7f), color, Color.white};
                                    }};
                                }

                                @Override
                                protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
                                    unit.apply(shootStatus, shootStatusDuration);

                                    if(shoot.firstShotDelay > 0){
                                        mount.charging = true;
                                        shoot.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                                            if(delay > 0f){
                                                Time.run(delay, () -> {
                                                    chargeSound.at(xOffset + shootX, yOffset + shootY, Mathf.random(soundPitchMin, soundPitchMax));
                                                    bullet.chargeEffect.at(xOffset + shootX, yOffset + shootY, rotation, unit);
                                                });
                                            }
                                        });
                                    }

                                    shoot.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                                        //this is incremented immediately, as it is used for total bullet creation amount detection
                                        mount.totalShots++;
                                        int barrel = mount.barrelCounter;
                                        if(delay > 0f){
                                            Time.run(delay, () -> {
                                                //hack: make sure the barrel is the same as what it was when the bullet was queued to fire
                                                int prev = mount.barrelCounter;
                                                mount.barrelCounter = barrel;
                                                bullet(unit, mount, xOffset, yOffset, angle, mover);
                                                mount.barrelCounter = prev;
                                            });
                                        }else{
                                            bullet(unit, mount, xOffset, yOffset, angle, mover);
                                        }
                                    }, () -> mount.barrelCounter++);
                                }
                            }
                );

                weapons.add(new LaserPointDefenseWeapon(name("gun-mount-2")){
                    {
                        x = 122 / 4f;
                        y = 230 / 4f;
                        shootY = 24 / 4f;
                        shootX = 0;
                        damage = 18f;
                        mirror = true;
                        bullet = new BulletType(){{
                            range = 300;
                        }};
                    }
                });

                weapons.add(new Weapon(name("tankAG-weapon1")){
                    {
                        x = 0 / 4f;
                        y = -156 / 4f;
                        reload = 240;
                        shootSound = shootAfflict;
                        velocityRnd = 0.12f;
                        mirror = false;
                        rotate = true;
                        rotateSpeed = 0.8f;
                        recoil = 2.0f;
                        shootY = 20 / 4f;
                        bullet = new LightningLinkerBulletType(5, 400){{
                            size = width = height = 18;
                            scaleLife = false;
                            collides = true;
                            drag = 0.01f;
                            lifetime = 160.13f;//400
                            keepVelocity = false;
                            frontColor = WHPal.ShootOrangeLight;
                            lightningColor = lightColor = hitColor = trailColor = backColor = WHPal.ShootOrange;
                            linkLightingDamage = 150;
                            lightning = 4;
                            lightningLength = 10;
                            lightningLengthRand = 12;
                            lightningDamage = 50;

                            hittable = false;

                            maxHit = 20;
                            linkRange = 200;
                            effectLightningChance = 0.07f;
                            randomGenerateRange = 100;
                            effectLightningLengthRand = 100;
                            effectLightningLength = 80;
                            trailEffect = WHFx.hitPoly(hitColor, hitColor, 20, 3, 15, 5, 8, 45);
                            trailChance = 0.4f;
                            trailInterval = 2.5f;

                            splashDamage = damage * 0.75f;
                            splashDamageRadius = 100;

                            shootEffect = new MultiEffect(
                            WHFx.linePolyOut(hitColor, 30, 4, 2, 6, 0),
                            linePolyOut(hitColor, 15, 4, 2, 6, 0).startDelay(15f)
                            );

                            hitEffect = despawnEffect = new MultiEffect(
                            WHFx.instRotation(hitColor, 60, splashDamageRadius + 20, 45, false),
                            WHFx.trailCircleHitSpark(hitColor, 60, 12, splashDamageRadius, 1.5f, 10f),
                            WHFx.hitPoly(hitColor, hitColor, 60, 10, splashDamageRadius, 5, 6, 45),
                            WHFx.generalExplosion(60, hitColor, splashDamageRadius, 6, false),
                            WHFx.circleOut(hitColor, 60, splashDamageRadius)
                            );

                            fragBullets = 3;
                            fragLifeMax = 0.7f;
                            fragLifeMin = 0.3f;
                            fragVelocityMax = 0.5f;

                            fragBullet = WHBulletsOther.Air7MainWeaponFrag;

                        }};
                    }
                });

                weapons.add(new Weapon(name("missile-launcher")){
                    {
                        x = 184 / 4f;
                        y = 64 / 4f;
                        reload = 90;
                        rotate = false;
                        baseRotation = -60;
                        layerOffset = -0.01f;
                        shootSound = WHSounds.missileShoot;
                        inaccuracy = 6;
                        alternate = false;
                        shoot = new ShootAlternate(){{
                            barrels = 3;
                            spread = 17 / 4f;
                            shots = 6;
                            shotDelay = 5f;
                        }};
                        bullet = new CritMissileBulletType(8, 100, name("large-missile")){
                            {
                                pierceArmor = true;
                                width = 20;
                                height = width * 2;
                                shrinkX = shrinkY = 0f;

                                shootCone = 180f;

                                lengthOffset = width - 2;
                                flameWidth = 2.5f;
                                flameLength = 15f;

                                lightningColor = hitColor = trailColor = WHPal.ShootOrange;

                                Color c = WHPal.ShootOrange.cpy().lerp(Color.orange, 0.25f);
                                colors = new Color[]{c.a(0.55f), c.a(0.7f), c.a(0.8f), c, Color.white};

                                lifetime = 400 / speed;
                                trailLength = 10;
                                trailWidth = 1.5f;
                                statusDuration = 60;
                                status = WHStatusEffects.melta;
                                homingDelay = 15;
                                homingPower = 0.1f;
                                followAimSpeed = 6;

                                splashDamage = 150;
                                splashDamageRadius = 56;

                                despawnEffect = hitEffect = new MultiEffect(
                                WHFx.generalExplosion(40, hitColor, splashDamageRadius, 8, false),
                                WHFx.instHit(hitColor, true, 2, splashDamageRadius * 0.75f),
                                WHFx.lineCircleOut(hitColor, 5, splashDamageRadius, 5)
                                );
                                smokeEffect = hugeSmoke;
                                shootEffect = shootBigColor;
                                lightningDamage = 40;
                                lightning = 2;
                                lightningLength = 12;
                            }
                        };
                    }
                });

                weapons.addAll(
                copyAndMove(air7Laser1, -207 / 4f, -167 / 4f),
                copyAndMove(air7Laser1, -117 / 4f, -89 / 4f),
                air7MainWeapon
                );
            }
        };

        air6 = new UnitType("air6"){
            {
                constructor = PayloadUnit::create;

                speed = 1.16f;
                accel = 0.07f;
                drag = 0.09f;
                rotateSpeed = 0.5f;
                flying = true;
                lowAltitude = false;
                health = 90000;
                engineSize = -1f;
                hitSize = 80;
                armor = 28;
                itemCapacity = 0;
                engineLayer = Layer.effect - 0.001f;

                immunities.addAll(WHStatusEffects.melta, WHStatusEffects.rust, StatusEffects.melting, StatusEffects.burning, StatusEffects.muddy);

                payloadCapacity = (7 * 7) * tilePayload;

                targetFlags = new BlockFlag[]{BlockFlag.reactor, BlockFlag.turret, BlockFlag.core, null};
                ammoType = new ItemAmmoType(WHItems.molybdenumAlloy);

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                for(int i : Mathf.signs){
                    engines.addAll(new WHUnitEngine(19 / 4f * i, -195 / 4f, 10, 40, 1.8f){{
                                       line = true;
                                   }},
                    new WHUnitEngine(49 / 4f * i, -195 / 4f, 8, 32, 1.8f));
                }
                for(int a = 0; a < 10; a++){
                    AncientEngine.addEngine(this, -122 / 4f - a * 3 / 4f, -131 / 4f, 0, 3, true);
                }

                abilities.add(new BoostAbility(2, 30){{
                    trailLength = 20;
                }});


                Weapon air6Laser = new Weapon(name("gun-mount")){{
                    {
                        rotate = true;
                        rotateSpeed = 0.8f;
                        reload = 200;
                        layerOffset = -0.1f;
                        shootY = 24 / 4f;
                        shootSound = beamMeltdown;
                        ;
                        continuous = true;
                        bullet = new LaserBeamBulletType(120){{

                            damageInterval = 6;
                            pierceCap = 3;

                            hitColor = lightColor = WHPal.ShootOrange.cpy();

                            lifetime = 80;
                            hitSize = 5;
                            length = 30;
                            extensionProportion = 9f;
                            moveInterp = Interp.pow3Out;
                            width = 12;
                            triCap = true;
                            moveSpeed = 0.015f;

                            colors = new Color[]{hitColor.cpy().a(0.4f), hitColor.cpy().a(0.6f), hitColor.cpy().a(0.8f), Color.white};

                            smokeEffect = Fx.shootSmallSmoke;
                            despawnEffect = hitEffect = Fx.hitMeltdown;
                        }};
                    }
                }};
                weapons.addAll(
                new Weapon(name(name + "weapon-1")){{
                    rotate = false;
                    x = 0;
                    reload = 240;
                    layerOffset = -0.1f;
                    shoot = new ShootBarrel(){{
                        shots = 10;
                        shotDelay = 120f / shots;
                        barrels = new float[]{
                        0, 12, 0,
                        40, -6, 0,
                        -40, -6, 0
                        };
                    }};
                    minShootVelocity = 0.4f;
                    ignoreRotation = true;
                    shootCone = 60;
                    shootSound = Sounds.shootMissileSmall;
                    xRand = 6;
                    bullet = new CritBulletType(0.7f, 350, "missile-large"){{
                        maxRange = 80f;

                        drag = 0.03f;
                        lifetime = 100;

                        width = 24;
                        height = 32;
                        hitShake = 12;
                        hitSound = explosionArtillery;
                        frontColor = Color.white;
                        trailColor = hitColor = backColor = WHPal.ShootOrange;
                        trailLength = 10;
                        trailWidth = 5;
                        collides = false;
                        collidesAir = false;
                        scaledSplashDamage = true;

                        shrinkY = shrinkX = 0.8f;

                        status = WHStatusEffects.melta;
                        statusDuration = 300;
                        incendAmount = 3;
                        incendSpread = 86;
                        incendChance = 0.25f;

                        splashDamage = damage;
                        splashDamageRadius = 65;
                        buildingDamageMultiplier = 1.4f;
                        shootEffect = smokeEffect = Fx.none;

                        despawnEffect = hitEffect = new MultiEffect(
                        WHFx.generalExplosion(30, hitColor, splashDamageRadius, 10, true),
                        WHFx.trailHitSpark(hitColor, 30, 15, splashDamageRadius, 1.7f, 12f),
                        WHFx.circleOut(hitColor, 30, splashDamageRadius),
                        WHFx.crossBlastArrow45(hitColor, hitColor, 30, 8, 35, splashDamageRadius * 0.5f, splashDamageRadius)
                        );
                        fragBullets = 5;
                        fragLifeMin = 0.1f;
                        fragBullet = new BasicBulletType(3.5f, 80, "missile-large"){{
                            drag = 0.02f;

                            knockback = 0.8f;
                            lifetime = 23f;
                            width = height = 18f;
                            collides = collidesTiles = false;
                            splashDamageRadius = 32f;
                            splashDamage = 80;
                            backColor = trailColor = hitColor = WHPal.ShootOrange;
                            frontColor = Color.white;
                            smokeEffect = Fx.shootBigSmoke2;
                            despawnShake = 7f;
                            lightRadius = 30f;

                            hitEffect = despawnEffect = new MultiEffect(
                            WHFx.generalExplosion(13, hitColor, splashDamageRadius, 4, false)
                            );

                            trailLength = 20;
                            trailWidth = 3.5f;
                        }};
                    }};
                }},
                copyAndMove(air6Laser, 61 / 4f, 45 / 4f),
                copyAndMoveAnd(air6Laser, 38 / 4f, 94 / 4f, b -> b.reload = 240f)
                );

                weapons.add(
                new Weapon(name("machine-gun-3")){{
                    y = -35 / 4f;
                    x = 118f / 4f;
                    reload = 90;
                    recoil = 2f;
                    shootSound = WHSounds.machineGunShoot;
                    inaccuracy = 4f;
                    velocityRnd = 0.15f;
                    xRand = 0.1f;
                    recoils = 3;

                    layerOffset = -1;

                    shoot = new ShootAlternate(){{
                        shots = 10;
                        barrels = 3;
                        spread = 10 / 4f;
                        shotDelay = 6;
                        recoilTime = shotDelay;
                    }};
                    rotate = true;
                    rotateSpeed = 0.8f;

                    shootY = 80f / 4f;
                    shootX = 4.1f / 4f;


                    float x1 = -15f / 4f, x2 = -5 / 4f, x3 = 5f / 4f,
                    move = 10f / 4f;

                    parts.addAll(
                    new RegionPart("-barrel"){{
                        layerOffset = -0.01f;
                        x = x1;
                        y = 52 / 4f;
                        heatProgress = progress = PartProgress.recoil;
                        heatColor = WHPal.thurmixRed;
                        moveX = move;
                        recoilIndex = 0;
                    }},
                    new RegionPart("-barrel"){{
                        layerOffset = -0.02f;
                        x = x2;
                        y = 52 / 4f;
                        heatProgress = progress = PartProgress.recoil;
                        heatColor = WHPal.thurmixRed;
                        moveX = move;
                        recoilIndex = 1;
                    }},
                    new RegionPart("-barrel"){{
                        layerOffset = -0.03f;
                        x = x3;
                        y = 52 / 4f;
                        heatProgress = progress = PartProgress.recoil;
                        heatColor = WHPal.thurmixRed;
                        moveX = -2 * move;
                        recoilIndex = 2;
                    }}
                    );

                    bullet = new CritBulletType(7f, 90){{
                        lifetime = 350 / speed;
                        keepVelocity = false;
                        splashDamage = 60;
                        splashDamageRadius = 4 * 8f;
                        width = 8;
                        height = width * 2.5f;
                        trailWidth = width / 3;
                        trailLength = 5;
                        frontColor = WHPal.ShootOrangeLight;
                        trailChance = 0.2f;

                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.ShootOrange;
                        shootEffect = new MultiEffect(
                        WHFx.shootLine(10, 20),
                        Fx.shootBig);
                        smokeEffect = shootBigSmoke;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.square(hitColor, 15, 4, 30, 5),
                        WHFx.generalExplosion(20, hitColor, 30, 8, false)
                        );
                    }};
                }}
                );

            }
        };


        air5 = new UnitType("air5"){
            {
                constructor = UnitTypes.eclipse.constructor;

                flying = true;
                speed = 1.5f;
                rotateSpeed = 1;
                accel = 0.04f;
                drag = 0.04f;
                hitSize = 50;
                health = 30000;
                armor = 20;

                engineSize = -1f;
                lowAltitude = false;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                for(int i : Mathf.signs){
                    engines.add(new WHUnitEngine(24 / 4f * i, -120 / 4f, 5, 30, 1.5f){{
                        line = true;
                    }});
                }
                for(int a = 0; a < 10; a++){
                    AncientEngine.addEngine(this, -112 / 4f - a * 2 / 4f, -120 / 4f, 0, 2, true);
                }

                abilities.add(new BoostAbility(2, 60){{
                    trailLength = 15;
                }});

                weapons.add(new Weapon(name(name + "-weapon-1")){
                    {
                        x = 80 / 4f;
                        y = 24 / 4f;
                        reload = 180;
                        rotate = false;
                        inaccuracy = 1.2f;
                        shootSound = WHSounds.energyShoot;
                        shootCone = 20f;
                        bullet = new TrailFadeBulletType(5, 200, name("pierce")){
                            {
                                splashDamageRadius = 56;
                                splashDamage = 180;
                                width = 15;
                                height = 35;
                                drag = -0.01f;
                                lifetime = 53f;//360
                                pierceCap = 2;
                                pierce = true;
                                pierceBuilding = true;
                                lightningDamage = 50;
                                lightning = 2;
                                lightningLength = 13;

                                hitBlinkTrail = false;
                                trailColor = lightColor = lightningColor = backColor = hitColor = WHPal.SkyBlue;

                                reflectable = false;
                                shrinkY = 0;
                                frontColor = WHPal.WHYellow2;
                                trailLength = 12;
                                trailWidth = 3f;

                                shootEffect = new MultiEffect(
                                WHFx.lineCircleOut(WHPal.SkyBlue, 20, 40, 2),
                                WHFx.shootLineSmall(WHPal.SkyBlue));
                                smokeEffect = WHFx.hugeSmokeGray;

                                hitEffect = new MultiEffect(
                                WHFx.generalExplosion(30, hitColor, splashDamageRadius, 5, false),
                                WHFx.hitSpark(WHPal.SkyBlue, 45, 14, 60, 1, 8),
                                WHFx.hitPoly(hitColor, hitColor, 30, 10, splashDamageRadius, 8, 6, 45)
                                );
                                despawnEffect = new MultiEffect(WHFx.blast(WHPal.SkyBlue, 50),
                                WHFx.trailCircleHitSpark(hitColor, 60, 18, splashDamageRadius * 1.4f, 1.8f, 10f),
                                WHFx.airAsh(60, splashDamageRadius * 0.9f, splashDamageRadius * 0.1f, splashDamageRadius * 0.2f, WHPal.SkyBlue, 1.7f, 3),
                                WHFx.instRotation(hitColor, 60, splashDamageRadius, 45, false)
                                );
                            }

                            @Override
                            public void hit(Bullet b, float x, float y){
                                super.hit(b, x, y);
                                PlasmaFire.createChance(b, 24, 0.01f);
                            }
                        };
                    }
                });

                weapons.add(new Weapon(name(name + "-weapon-2")){
                    {
                        x = 48 / 4f;
                        y = 88 / 4f;
                        reload = 40;
                        xRand = 1;
                        rotate = false;
                        shoot.shots = 6;
                        shoot.shotDelay = 5;
                        inaccuracy = 5f;
                        shootSound = shootMissile;
                        velocityRnd = 0.07f;
                        shootCone = 20f;

                        bullet = new CritBulletType(8, 50, "circle"){
                            {
                                critChance = 0.01f;
                                drag = -0.008f;
                                width = height = 6;
                                speed = 5;
                                lifetime = 48;//300

                                splashDamageRadius = 55;
                                splashDamage = 50;
                                knockback = 0.3f;

                                lightning = 2;
                                lightningLength = 8;
                                lightningLengthRand = 8;
                                lightningDamage = 25f;

                                reflectable = false;
                                trailLength = 5;
                                trailWidth = width / 2;
                                shrinkX = shrinkY = 0;

                                frontColor = WHPal.SkyBlueF;
                                lightningColor = lightColor = hitColor = trailColor = backColor = WHPal.SkyBlue;
                                shootEffect = shootBigColor;
                                smokeEffect = WHFx.hitCircle(hitColor, Color.lightGray, 20, 6, 25, 5f);

                                trailInterval = 3;
                                trailEffect = WHFx.hitPoly(hitColor, hitColor, 20, 2, 12, 4, 6, 45);

                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.line45Explosion(hitColor, hitColor, splashDamageRadius * 0.7f),
                                WHFx.hitPoly(hitColor, hitColor, 30, 7, 30, 4, 6, 45),
                                WHFx.generalExplosion(15, hitColor, splashDamageRadius, 6, false)
                                );

                                homingDelay = 15;
                                homingPower = 0.004f;
                                homingRange = 32f;

                            }
                        };
                    }
                });

                weapons.add(new Weapon(name(name + "-weapon-3")){
                    {
                        x = 112 / 4f;
                        y = -40 / 4f;
                        layerOffset = -0.01f;
                        reload = 240;
                        shootSound = shootMissile;
                        shootCone = 20f;
                        velocityRnd = 0.12f;
                        bullet = new LightningLinkerBulletType(4.3f, 200){{
                            size = width = height = 10;
                            scaleLife = false;
                            collides = true;
                            drag = 0.01f;
                            lifetime = 180.6f;//360
                            keepVelocity = false;
                            frontColor = WHPal.SkyBlueF;
                            lightningColor = lightColor = hitColor = trailColor = backColor = WHPal.SkyBlue;
                            linkLightingDamage = 110;
                            lightning = 3;
                            lightningLength = 6;
                            lightningLengthRand = 8;
                            lightningDamage = 35f;

                            hittable = false;

                            linkRange = 120;
                            effectLightningChance = 0.03f;
                            randomGenerateRange = 60f;
                            effectLightningLengthRand = 70;
                            effectLightningLength = 60;
                            trailEffect = WHFx.hitPoly(hitColor, hitColor, 20, 3, 15, 5, 8, 45);
                            trailChance = 0.23f;
                            trailInterval = 2.5f;

                            shootEffect = new MultiEffect(
                            WHFx.linePolyOut(hitColor, 30, 4, 2, 6, 0),
                            linePolyOut(hitColor, 15, 4, 2, 6, 0).startDelay(15f)
                            );

                            hitEffect = despawnEffect = new MultiEffect(
                            WHFx.trailCircleHitSpark(hitColor, 30, 12, 70, 1.5f, 10f),
                            WHFx.hitPoly(hitColor, hitColor, 30, 10, 64, 5, 6, 45),
                            WHFx.generalExplosion(30, hitColor, 64, 6, false)
                            );

                            fragBullets = 5;
                            fragBullet = new CritBulletType(1.5f, 120, "circle"){{
                                width = height = 6;
                                shrinkX = shrinkY = 0f;
                                trailLength = 5;
                                trailWidth = 2;
                                drag = 0.008f;
                                lifetime = 50;
                                splashDamage = damage;
                                splashDamageRadius = 60;
                                frontColor = WHPal.SkyBlueF;
                                lightningColor = lightColor = hitColor = trailColor = backColor = WHPal.SkyBlue;

                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.generalExplosion(10, hitColor, 25, 5, false),
                                WHFx.line45Explosion(hitColor, hitColor, splashDamageRadius * 0.7f),
                                WHFx.hitPoly(hitColor, hitColor, 20, 5, splashDamageRadius, 4, 5, 45),
                                WHFx.trailCircleHitSpark(hitColor, 20, 12, splashDamageRadius, 1f, 6)
                                );
                            }};

                        }};
                    }
                });
            }
        }

        ;
        air4 = new

        UnitType("air4"){
            {
                lowAltitude = false;
                constructor = UnitTypes.flare.constructor;
                speed = 1.8f;
                accel = 0.04f;
                drag = 0.04f;
                rotateSpeed = 1;
                immunities.addAll(StatusEffects.electrified, StatusEffects.slow);
                ammoType = new PowerAmmoType(6600);
                ammoCapacity = 3000;
                hitSize = 35;
                flying = true;
                health = 10000;
                armor = 15f;
                itemCapacity = 0;
                outlineRadius = 3;
                outlineColor = WHPal.Outline;
                engines.addAll(
                new UnitEngine(0, -95f / 4f, 5, 0),
                new WHUnitEngine(0, -90 / 4f, 0, 30, 2){{
                    line = true;
                }},
                new WHUnitEngine(62 / 4f, -60 / 4f, 0, 20, 2),
                new WHUnitEngine(-62 / 4f, -60 / 4f, 0, 20, 2f)
                );
                engineLayer = Layer.effect - 0.001f;

                abilities.add(new BoostAbility(2, 60){{
                    trailLength = 10;
                }});

                weapons.add(new Weapon(name(name + "-weapon-1")){
                    {
                        x = 32 / 4f;
                        y = 48 / 4f;
                        reload = 40;
                        rotate = false;

                        inaccuracy = 1.2f;
                        soundPitchMin = 0.5f;
                        soundPitchMax = 0.7f;
                        shootSound = WHSounds.machineGunShoot;
                        layerOffset = -0.01f;
                        alternate = false;
                        shoot = new ShootPattern(){{
                            shots = 6;
                            shotDelay = 5;
                        }};
                        bullet = new BasicBulletType(8, 50){{
                            width = 8;
                            height = 20;
                            lifetime = 270 / speed;
                            shrinkY = 0;
                            frontColor = WHPal.ShootOrangeLight;
                            hitColor = trailColor = backColor = WHPal.ShootOrange;
                            trailLength = 4;
                            trailWidth = width / 2.8f;
                            pierceCap = 2;
                            pierceBuilding = true;
                            shootEffect = WHFx.lineCircleOut(WHPal.ShootOrange, 10, 5, 2);
                            smokeEffect = shootSmallSmoke;
                            despawnEffect = hitEffect = new MultiEffect(
                            WHFx.square(WHPal.ShootOrangeLight, 15, 4, 30, 4),
                            WHFx.square(WHPal.ShootOrangeLight, 25, 2, 15, 6));

                        }};
                    }
                });
                weapons.add(new Weapon(name(name + "-weapon-2")){
                    {
                        x = -54 / 4f;
                        y = 32 / 4f;
                        reload = 70;
                        xRand = 1;
                        rotate = false;
                        shoot.shots = 4;
                        shoot.shotDelay = 6;
                        inaccuracy = 4;
                        shootSound = shootMissile;
                        velocityRnd = 0.1f;

                        bullet = new CritBulletType(8, 50, "circle"){
                            {
                                critChance = 0.01f;
                                makePlaFire = true;
                                plaFireChance = 0.05f;
                                drag = -0.008f;
                                width = height = 6;
                                speed = 5;
                                lifetime = 49.2f;//300

                                splashDamageRadius = 55;
                                splashDamage = 50;
                                knockback = 0.3f;

                                lightning = 2;
                                lightningLength = 8;
                                lightningLengthRand = 8;
                                lightningDamage = 25f;

                                reflectable = false;
                                trailLength = 5;
                                trailWidth = width / 2;
                                shrinkX = shrinkY = 0;

                                frontColor = WHPal.SkyBlueF;
                                lightColor = hitColor = trailColor = backColor = WHPal.SkyBlue;
                                shootEffect = shootBigColor;
                                smokeEffect = WHFx.hitCircle(hitColor, Color.lightGray, 20, 6, 25, 5f);

                                trailInterval = 3;
                                trailEffect = WHFx.hitPoly(hitColor, hitColor, 20, 2, 12, 4, 6, 45);

                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.hitPoly(hitColor, hitColor, 30, 7, 30, 4, 6, 45),
                                WHFx.generalExplosion(15, hitColor, splashDamageRadius, 6, false)
                                );

                                homingDelay = 15;
                                homingPower = 0.004f;
                                homingRange = 32f;

                            }
                        };
                    }
                });
                weapons.add(new Weapon(name(name + "-weapon-3")){
                    {
                        x = -86 / 4f;
                        y = 0;
                        reload = 240;
                        xRand = 1;
                        alternate = false;
                        mirror = false;
                        shoot = new ShootSpread(){{
                            spread = 10f;
                            shots = 4;
                            shotDelay = 8;
                        }};
                        inaccuracy = 5;
                        shootSound = shootMissileLong;
                        velocityRnd = 0.1f;
                        bullet = new CritMissileBulletType(6, 90, name("large-missile")){{

                            Color c = lightningColor = trailColor = backColor = hitColor = WHPal.ShootOrange;
                            frontColor = WHPal.ShootOrangeLight;
                            colors = new Color[]{c.a(0.4f), c.a(0.6f), c.a(0.8f), Color.white};

                            flameWidth = 1.3f;
                            flameLength = 15;

                            splashDamage = 60;
                            splashDamageRadius = 35;

                            width = 28;
                            height = width * 2;
                            lengthOffset = height / 3f;
                            lifetime = 300 / speed;
                            homingDelay = 20;
                            homingPower = 0.08f;
                            followAimSpeed = 1;

                            shrinkX = shrinkY = 0;
                            hitEffect = despawnEffect = new MultiEffect(
                            WHFx.line45Explosion(hitColor, hitColor, splashDamageRadius / 2),
                            WHFx.generalExplosion(20, hitColor, splashDamageRadius, 10, false)
                            );
                            hitSound = explosionPlasmaSmall;
                            hitShake = 5;
                            shootEffect = none;
                            smokeEffect = shootBigSmoke;

                            fragBullets = 1;
                            fragRandomSpread = 0;
                            fragLifeMax = fragLifeMin = 1;
                            fragBullet = new BasicBulletType(0, 80){{
                                hittable = reflectable = false;
                                lifetime = 20f;
                                splashDamage = damage;
                                splashDamageRadius = 45;
                                lightningColor = trailColor = backColor = hitColor = WHPal.ShootOrange;
                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.generalExplosion(14, hitColor, splashDamageRadius, 10, false)
                                );
                                fragBullets = 1;
                                fragRandomSpread = 0;
                                fragLifeMax = fragLifeMin = 1;
                                fragBullet = new BasicBulletType(0, 100){
                                    {
                                        hittable = reflectable = false;
                                        lifetime = 20f;
                                        splashDamage = damage;
                                        splashDamageRadius = 55;
                                        lightningColor = trailColor = backColor = hitColor = WHPal.ShootOrange;
                                        hitEffect = despawnEffect = new MultiEffect(
                                        WHFx.generalExplosion(14, hitColor, splashDamageRadius, 10, false)
                                        );
                                    }
                                };
                            }};

                        }};
                    }
                });
            }
        }

        ;

        air3 = new

        UnitType("air3"){
            {
                constructor = PayloadUnit::create;
                speed = 2f;
                drag = 0.06f;
                accel = 0.08f;
                rotateSpeed = 4f;
                flying = true;
                lowAltitude = false;
                health = 2400;
                armor = 10f;
                hitSize = 20;
                engineOffset = 7.5f;
                engineSize = 4.5f;
                trailLength = 6;
                pickupUnits = true;
                payloadCapacity = (3 * 3) * tilePayload;
                itemCapacity = 50;
                engineLayer = 110f;

                immunities.add(StatusEffects.unmoving);

                ammoType = new PowerAmmoType(3000);
                ammoCapacity = 150;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                abilities.add(new BoostAbility(1.6f, 90){{
                    trailLength = 10;
                }});

                weapons.add(new Weapon(name("gun-mount-2")){{
                    x = 38 / 4f;
                    y = 24 / 4f;
                    reload = 4f;
                    recoil = 1;
                    recoilTime = 20f;
                    shootCone = 10;
                    inaccuracy = 3f;
                    shootSound = WHSounds.energyShoot;
                    layerOffset = -0.01f;

                    bullet = new BasicBulletType(6, 45f, "shell"){{
                        shrinkY = 0f;
                        trailLength = 5;
                        trailWidth = 1f;
                        hitColor = trailColor = WHPal.ShootOrange;
                        frontColor = WHPal.ShootOrangeLight;
                        backColor = WHPal.ShootOrange;
                        width = 7;
                        height = width * 3;
                        lifetime = 210 / speed;

                        shootEffect = new MultiEffect(Fx.colorSpark, Fx.shootBig);

                        smokeEffect = shootSmallSmoke;

                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.square(hitColor, 25, 4, 25, 5),
                        WHFx.lineCircleOut(hitColor, 10, 25, 2)
                        );
                    }};
                }});

                weapons.add(new MarkWeapon(name + "weapon-2"){{
                    reload = 70f;
                    x = -7.5f;
                    y = 7f;
                    inaccuracy = 3f;
                    ejectEffect = Fx.casing2;
                    shootSound = shootMissile;


                    shoot = new ShootSpread(3, 15){{
                        shotDelay = 5;
                    }};

                    markShoot = new ShootPattern(){{
                        shots = 4;
                        shotDelay = 5;
                    }};
                    markTime = 30f;

                    bullet = new CritMissileBulletType(4f, 65f, "missile"){{

                        width = 10;
                        height = 16;
                        lengthOffset = width / 4f;
                        flameLength = 22;
                        flameWidth = 2f;
                        Color c = trailColor = backColor = hitColor = WHPal.ShootOrange.cpy().lerp(Pal.slagOrange, 0.3f);
                        frontColor = WHPal.ShootOrangeLight;
                        colors = new Color[]{c.a(0.4f), c.a(0.6f), c.a(0.8f), Color.white};
                        homingPower = 0.004f;
                        homingDelay = 30;
                        homingRange = 64f;
                        followAimSpeed = 1f;

                        splashDamageRadius = 20f;
                        splashDamage = 55f;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.square(hitColor, 30, 4, splashDamageRadius, 4f),
                        WHFx.hitSpark(hitColor, 20, 6, splashDamageRadius, 1, 4f),
                        WHFx.generalExplosion(10, hitColor, splashDamageRadius, 10, false)
                        );
                        shootEffect = new MultiEffect(
                        WHFx.hitCircle(hitColor, Color.lightGray, 12, 5, 15, 4f)
                        );
                        smokeEffect = Fx.none;
                        lifetime = 300 / speed;

                        weaveScale = 12f;
                        weaveMag = 1;
                    }};
                    BulletType markB = bullet.copy();
                    markB.fragBullets = 1;
                    markB.fragRandomSpread = 0f;
                    markB.fragBullet = new LaserBulletType(60){{
                        length = 55;
                        width = 12;
                        pierceCap = 3;
                        Color c = hitColor = WHPal.ShootOrange.cpy();
                        colors = new Color[]{c.a(0.4f), c.a(0.6f), c.a(0.8f), Color.white};
                        hitEffect = despawnEffect = WHFx.hitSpark(hitColor, 20, 8, 30, 1.2f, 8f);
                    }};
                    markBullet = markB;
                }});
            }
        }

        ;

        air2 = new

        UnitType("air2"){
            {
                constructor = UnitTypes.flare.constructor;
                lowAltitude = true;
                speed = 1.8f;
                accel = 0.08f;
                drag = 0.016f;
                hitSize = 11f;
                flying = true;
                health = 1000;
                armor = 6f;
                engineOffset = 45 / 4f;
                engineSize = 4;
                itemCapacity = 25;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new ItemAmmoType(Items.graphite);

                abilities.add(new BoostAbility(1.6f, 90){{
                    trailLength = 10;
                }});

                weapons.add(new MarkWeapon(name("gun-mount-2")){{
                    markShoot = new ShootSpread(3, 10){{
                        shotDelay = 10f;
                    }};
                    markChance = 0.1f;
                    markReduce = 0f;
                    x = 32 / 4f;
                    y = -16 / 4f;
                    rotate = true;
                    rotateSpeed = 0.8f;
                    reload = 90f;
                    recoil = 4f;
                    shake = 2f;
                    shootY = 20 / 4f;
                    ejectEffect = Fx.casing2;
                    shootSound = shootArtillery;
                    markBullet = bullet = new ArtilleryBulletType(4f, 80, "shell"){{
                        shootEffect = Fx.shootBig;
                        smokeEffect = shootBigSmoke;
                        knockback = 0.8f;
                        lifetime = 230 / speed;
                        width = 12f;
                        height = 20f;
                        collidesAir = collides = true;
                        collidesTiles = true;
                        trailLength = 5;
                        trailWidth = 2;
                        splashDamageRadius = 40f;
                        splashDamage = 45;
                        hitColor = backColor = Pal.bulletYellowBack;
                        frontColor = Pal.bulletYellow;
                        trailChance = 0.2f;
                        trailEffect = WHFx.hitCircle(hitColor, Color.lightGray, 10, 2, 10, 6f);
                        hitEffect = despawnEffect =
                        new MultiEffect(
                        WHFx.square(hitColor, 30, 4, splashDamageRadius, 4f),
                        WHFx.generalExplosion(10, hitColor, splashDamageRadius, 10, false));
                        fragBullets = 3;
                        fragLifeMax = 1.5f;
                        fragBullet = new BasicBulletType(4f, 30, "shell"){{
                            splashDamage = 20;
                            splashDamageRadius = 20f;
                            lifetime = 10;
                            width = 5f;
                            height = 10f;
                            hitEffect = despawnEffect = flakExplosion;
                        }};
                    }};
                }});
            }
        }

        ;

        air1 = new

        UnitType("air1"){
            {
                constructor = UnitTypes.flare.constructor;
                circleTarget = true;
                range = 80;
                lowAltitude = false;
                engineLayer = Layer.effect - 0.01f;

                aiController = () -> new FlyingAI(){
                    @Override
                    public void updateMovement(){
                        unloadPayloads();

                        if(target != null && unit.hasWeapons()){
                            if(unit.type.circleTarget){
                                circleAttack(120 + 100 * Mathf.absin(8, 1));
                            }else{
                                moveTo(target, unit.type.range * 0.8f);
                                unit.lookAt(target);
                            }
                        }

                        if(target == null && state.rules.waves && unit.team == state.rules.defaultTeam){
                            moveTo(getClosestSpawner(), state.rules.dropZoneRadius + 130f);
                        }
                    }
                };

                speed = 2.5f;
                accel = 0.08f;
                drag = 0.04f;
                flying = true;
                health = 350;
                armor = 4f;
                engineOffset = 28 / 4f;
                targetFlags = new BlockFlag[]{BlockFlag.generator, null};
                hitSize = 9;
                itemCapacity = 10;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                abilities.add(new BoostAbility(1.6f, 90){{
                    trailLength = 10;
                }});

                ammoType = new ItemAmmoType(Items.coal);

                immunities.add(StatusEffects.burning);

                weapons.add(new Weapon(name(name + "weapon-1")){{
                    x = 20 / 4f;
                    y = 16 / 4f;
                    top = false;
                    shootSound = shootFlame;
                    shootY = 2f;
                    reload = 8f;
                    recoil = 1f;
                    ejectEffect = Fx.none;
                    bullet = new BulletType(4, 30f){{
                        ammoMultiplier = 3f;
                        hitSize = 7f;
                        lifetime = 18;
                        pierce = true;
                        pierceBuilding = true;
                        pierceCap = 2;
                        statusDuration = 60f * 4;
                        shootEffect = Fx.shootSmallFlame;
                        hitEffect = Fx.hitFlameSmall;
                        despawnEffect = Fx.none;
                        status = StatusEffects.burning;
                        keepVelocity = false;
                        hittable = false;
                    }};
                }});
            }
        }

        ;


        airB1 = new

        UnitType("airB1"){
            {
                constructor = UnitTypes.mono.constructor;
                defaultCommand = UnitCommand.mineCommand;

                flying = true;
                drag = 0.06f;
                accel = 0.12f;
                speed = 1.8f;
                health = 400;
                engineSize = 1.8f;
                engineOffset = 25 / 4f;
                itemCapacity = 70;
                range = 50f;
                isEnemy = false;
                controlSelectGlobal = false;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new PowerAmmoType(900);

                mineTier = 3;
                mineSpeed = 3.5f;
            }
        }

        ;


        airB2 = new

        UnitType("airB2"){
            {
                researchCostMultiplier = 0.9f;
                constructor = UnitTypes.poly.constructor;
                defaultCommand = UnitCommand.rebuildCommand;

                flying = true;
                drag = 0.05f;
                speed = 2.6f;
                rotateSpeed = 15f;
                accel = 0.1f;
                range = 130f;
                health = 800;
                armor = 3f;
                buildSpeed = 1.2f;
                buildBeamOffset = mineBeamOffset = 18 / 4f;
                engineOffset = 6.5f;
                trailLength = 10;

                hitSize = 12f;
                lowAltitude = true;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new PowerAmmoType(1200);

                mineTier = 2;
                mineSpeed = 3f;
                mineItems = Seq.with(WHItems.kellexItems);

                abilities.add(
                new RepairFieldAbility(50, 60f * 8, 40){{
                    healEffect = WHFx.linePolyOut(WHPal.thurmixRed, 60, range, 3, 4, 0);
                    activeEffect = WHFx.linePolyOut(WHPal.thurmixRed, 60, 12f, 3, 4, 0);
                }}
                );

                weapons.add(new HealWeapon(name(name + "-weapon1")){{
                    top = false;
                    y = 0;
                    x = 31f / 4f;
                    reload = 12;
                    ejectEffect = Fx.none;
                    recoil = 2f;
                    shootSound = shootMissile;
                    velocityRnd = 0.15f;
                    inaccuracy = 3f;
                    alternate = true;
                    rotate = false;

                    shootY = 0;

                    bullet = new BasicBulletType(8f, 40){{
                        homingPower = 0.08f;
                        weaveMag = 0.3f;
                        weaveScale = 12;
                        lifetime = 220 / 8f;
                        keepVelocity = false;
                        width = 8;
                        height = width * 2;
                        trailWidth = 1.6f;
                        trailLength = 8;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.thurmixRed;
                        shootEffect = Fx.shootSmallColor;
                        smokeEffect = Fx.hitLaserColor;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.linePolyOut(hitColor, 30, 25, 3, 4, 0),
                        WHFx.square(WHPal.thurmixRed, 30, 4, 30, 4)
                        );
                        frontColor = Color.white;
                        hitSound = Sounds.none;

                        healPercent = 1.5f;
                        collidesTeam = true;
                    }};
                }});
            }
        }

        ;

        airB3 = new

        UnitType("airB3"){
            {
                researchCostMultiplier = 0.8f;
                constructor = UnitTypes.mega.constructor;
                defaultCommand = UnitCommand.repairCommand;

                mineTier = 3;
                mineSpeed = 4f;
                mineItems = Seq.with(WHItems.kellexItems);
                health = 1800;
                armor = 6f;
                speed = 2f;
                accel = 0.06f;
                drag = 0.017f;
                lowAltitude = true;
                flying = true;
                engineOffset = 36 / 4f;
                hitSize = 16.05f;
                engineSize = 7f;
                payloadCapacity = (2 * 2) * tilePayload;
                buildSpeed = 3.5f;
                isEnemy = false;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new PowerAmmoType(2200);

                weapons.addAll(
                new HealWeapon(name(name + "-weapon1")){{
                    top = false;
                    x = 0;
                    y = -20 / 4f;
                    reload = 60;
                    ejectEffect = Fx.none;
                    recoil = 2f;
                    shootSound = shootMissile;
                    velocityRnd = 0.12f;
                    inaccuracy = 2f;
                    alternate = true;
                    rotate = false;
                    mirror = false;
                    shootY = 0;

                    shoot = new ShootSpread(4, 90){
                        @Override
                        public void shoot(int totalShots, BulletHandler handler){
                            float angle = 45f;
                            for(int i = 0; i < shots; i++){
                                float angleOffset = i * spread - (shots - 1) * spread / 2f + angle;
                                handler.shoot(0, 0, angleOffset, firstShotDelay + shotDelay * i);
                            }
                        }

                        {
                            shotDelay = 5f;
                        }
                    };

                    bullet = new BasicBulletType(8f, 50){{

                        weaveMag = 0.5f;
                        weaveScale = 7;
                        lifetime = 240 / speed;
                        keepVelocity = false;
                        width = 8;
                        height = width * 2;
                        trailWidth = 1.6f;
                        trailLength = 8;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.thurmixRed;
                        shootEffect = new MultiEffect(
                        WHFx.shootLine(10, 15),
                        WHFx.square(WHPal.thurmixRedLight, 15f, 3, 20, 4)
                        );
                        smokeEffect = Fx.hitLaserColor;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.linePolyOut(hitColor, 30, 16, 3f, 4, 0),
                        WHFx.square(WHPal.thurmixRed, 30, 4, 30, 8)
                        );
                        frontColor = WHPal.thurmixRed;
                        hitSound = Sounds.none;

                        healPercent = 1f;
                        collidesTeam = true;
                        followAimSpeed = 12;
                        homingPower = 0.02f;
                        homingDelay = 3f;
                    }};
                }},
                new HealConeWeapon(name(name + "-weapon2")){{
                    x = 0f;
                    y = 46 / 8f;
                    shootY = 0;
                    shootCone = 30f;
                    rotate = true;
                    rotateSpeed = 0.5f;
                    bullet = new HealCone(){{
                        percentHeal = false;
                        healColor = WHPal.thurmixRed;
                        lifetime = 150;
                        healPercent = 0.0f;
                        healAmount = 80;
                        findAngle = 30f;
                        findRange = 100;
                    }};
                    reload = 300;
                    useAmmo = mirror = alternate = false;
                    continuous = true;
                    recoil = 0;
                }},
                new Weapon(name(name + "-weapon3")){{
                    shootSound = shootLaser;
                    reload = 140;
                    x = y = 0f;
                    shootY = 46 / 8f;
                    mirror = rotate = false;
                    shoot.firstShotDelay = 30;
                    parentizeEffects = true;
                    bullet = new LightingLaserBulletType(70){{
                        recoil = 0.3f;
                        chargeEffect = WHFx.genericCharge(WHPal.thurmixRed, 5, 30, 30).followParent(true);
                        lifetime = 45f;
                        healPercent = 3;
                        collidesTeam = true;
                        length = 150;
                        sideAngle = 140;
                        sideLength = 14f;
                        Color color = lightningColor = WHPal.thurmixRed.cpy();
                        colors = new Color[]{color.a(0.4f), color.a(0.7f), color, Color.white};
                    }};
                    shootStatus = StatusEffects.slow;
                    shootStatusDuration = shoot.firstShotDelay;
                }}
                );
            }
        }

        ;

        airB4 = new

        UnitType("airB4"){
            {
                researchCostMultiplier = 0.7f;
                constructor = UnitTypes.mega.constructor;
                payloadCapacity = (3 * 3) * tilePayload;

                flying = true;
                speed = 1.2f;
                rotateSpeed = 1.5f;
                accel = 0.05f;
                drag = 0.017f;
                health = 9000;
                armor = 13f;
                buildSpeed = 4f;
                buildBeamOffset = mineBeamOffset = 68 / 4f;
                engineSize = -1;

                hitSize = 40f;
                lowAltitude = true;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new PowerAmmoType(4000);

                float engineSize = 1.8f;

                for(int a = 0; a < 10; a++){
                    AncientEngine.addEngine(this, a * engineSize / 4f, (-126 - a * 0.5f) / 4f, 0, engineSize, true);
                }
                for(int b = 0; b < 5; b++){
                    AncientEngine.addEngine(this, -b * engineSize / 4f + 75f / 4f, (-33 - b * 0.5f) / 4f, 60, engineSize, true);
                }


                abilities.addAll(
                new Propeller(64 / 4f, -88 / 4f, 0, 2),
                new Propeller(-64 / 4f, -88 / 4f, 0, 2),
                new RepairFieldAbility(100, 60f * 8, 40){{
                    healEffect = WHFx.linePolyOut(WHPal.thurmixRed, 60, 12, 3, 4, 0);
                    activeEffect = WHFx.linePolyOut(WHPal.thurmixRed, 60, range, 3, 4, 0);
                }},
                new ShockAbility(){{
                    range = 100;
                    reload = 60 * 3;
                    bulletDamage = 120;
                    falloffCount = 8;
                    waveColor = WHPal.thurmixRed;
                }}
                );

                weapons.addAll(
                new HealWeapon(name("airB-weapon-mount-2")){{
                    y = -49f / 4f;
                    x = 48f / 4f;
                    reload = 15;
                    ejectEffect = Fx.none;
                    recoil = 2f;
                    shootSound = WHSounds.LaserGatling;
                    inaccuracy = 1f;
                    shoot.shots = 3;
                    shoot.shotDelay = 5;
                    shoot.firstShotDelay = 10;

                    rotate = true;
                    rotateSpeed = 1.5f;

                    shootY = 16 / 4f;

                    bullet = new BasicBulletType(6f, 50){{
                        homingPower = 0.08f;
                        weaveMag = 0.3f;
                        weaveScale = 12;
                        lifetime = 220 / speed;
                        keepVelocity = true;
                        width = 7;
                        height = width * 3.5f;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.thurmixRed;
                        shootEffect = Fx.shootSmallColor;
                        smokeEffect = Fx.hitLaserColor;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.square(WHPal.thurmixRed, 60, 5, 20, 4)
                        );
                        frontColor = Color.white;
                        hitSound = Sounds.none;

                        healPercent = 0.7f;
                        collidesTeam = true;
                    }};
                }},
                new Weapon(name("airB-weapon-mount-2")){{
                    y = 32f / 4f;
                    x = 32f / 4f;
                    reload = 30;
                    ejectEffect = Fx.none;
                    recoil = 2f;
                    shootSound = WHSounds.LaserGatling;
                    velocityRnd = 0.15f;
                    inaccuracy = 2f;

                    rotate = true;
                    rotateSpeed = 1.5f;

                    shootY = 16 / 4f;

                    bullet = new DelayedPointBulletType(){{
                        lifetime = 60;
                        damage = 100;
                        maxRange = range = 240f;
                        width = 16f;
                        square = true;

                        Color c = hitColor = lightningColor = WHPal.thurmixRed.cpy();
                        shootEffect = WHFx.hitSpark(c, 20, 10, 20, 1, 5f);
                        colors = new Color[]{c.a(0.3f), c.a(0.7f), c, Color.white};
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.linePolyOut(hitColor, 30, 15, 3, 4, 0)
                        );
                    }};
                }},
                new HealConeWeapon(name(name + "-weapon2")){{
                    x = 0f;
                    y = 68 / 4f;
                    shootY = 0;
                    shootCone = 30f;
                    rotate = true;
                    rotateSpeed = 0.5f;
                    bullet = new HealCone(){{
                        percentHeal = false;
                        healColor = WHPal.thurmixRed;
                        lifetime = 240;
                        healAmount = 180;
                        findAngle = 40f;
                        findRange = 130f;
                    }};
                    reload = 300;
                    useAmmo = mirror = alternate = false;
                    continuous = true;
                    recoil = 0;
                }},
                new Weapon(name("airB-cannon-mount")){{
                    y = -2f / 4f;
                    x = 0;
                    reload = 140f;
                    recoil = 2f;
                    rotate = true;
                    rotateSpeed = 0.5f;
                    mirror = false;
                    shootSound = shootMalign;
                    velocityRnd = 0.15f;
                    inaccuracy = 2f;
                    shoot = new ShootAlternate(20 / 4f){{
                        shots = 2;
                        firstShotDelay = 20;
                        shotDelay = 15f;
                    }};

                    shootY = 16 / 4f;

                    bullet = new MultiTrailBulletType(6, 120, name("pierce")){{
                        splashDamage = 100;
                        splashDamageRadius = 5 * 8f;
                        lifetime = 300 / speed;
                        keepVelocity = false;
                        width = 10;
                        height = width * 2.4f;
                        weaveMag = 0.3f;
                        weaveScale = 12;
                        subTrailWidth = trailWidth = 2.5f;
                        trailLength = 10;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.thurmixRed;
                        shootEffect = new MultiEffect(
                        Fx.shootSmallColor,
                        WHFx.hitSpark(WHPal.thurmixRed, 20, 5, 20, 1, 5f));
                        smokeEffect = Fx.hitLaserColor;
                        hitEffect = WHFx.square(WHPal.thurmixRed, 30, 4, 30, 4);
                        despawnEffect = new MultiEffect(
                        WHFx.sharpBlast(WHPal.thurmixRed, WHPal.thurmixRedDark, 40, 40),
                        WHFx.linePolyOut(hitColor, 30, 40, 4, 4, 0),
                        WHFx.square(WHPal.thurmixRed, 30, 7, 50, 4.5f)
                        );
                        trailChance = 0.1f;
                        trailEffect = WHFx.square(WHPal.thurmixRed, 30, 2, 10, 3f);
                        pierceCap = 3;
                        frontColor = Color.white;
                        hitSound = Sounds.none;
                    }};
                }}
                );
            }
        }

        ;

        airB5 = new

        UnitType("airB5"){
            {
                researchCostMultiplier = 0.6f;
                constructor = UnitTypes.poly.constructor;

                flying = true;
                speed = 0.8f;
                rotateSpeed = 0.9f;
                accel = 0.05f;
                drag = 0.017f;
                health = 26000;
                armor = 20;
                buildSpeed = 5f;
                buildBeamOffset = mineBeamOffset = 146 / 4f;
                engineOffset = 140f / 4f;
                engineSize = 7.8f;

                hitSize = 65f;
                lowAltitude = true;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new PowerAmmoType(10000);

                float engineSize = 2f;

                for(int a = 0; a < 5; a++){
                    AncientEngine.addEngine(this, a * engineSize / 4f + 136f / 4f, -64f / 4f, 0, engineSize, true);
                }
                for(int b = 0; b < 10; b++){
                    AncientEngine.addEngine(this, b * engineSize / 4f + 58f / 4f, -119f / 4f, 0, engineSize, true);
                }


                abilities.addAll(
                new ContinueEnergyFieldAbility(90, 130, 180, 200, 10, 5){{
                    y = 70 / 2f / 4f;
                    effectRadius = 10;
                    sectors = 6;
                    status = StatusEffects.none;
                    maxTargets = 2;
                    unitPierceCap = 5;
                    buildPierceCap = 2;
                    healPercent = 0.5f;
                    sameTypeHealMult = 0.6f;
                    color = WHPal.thurmixRed;
                }},
                new ShockAbility(){{
                    range = 200;
                    reload = 60 * 3;
                    bulletDamage = 250;
                    falloffCount = 8;
                    waveColor = WHPal.thurmixRed;
                }},
                new AdaptedHealAbility(100, 60f * 8, 150){{
                    selfHealReloadTime = 180;
                    selfHealAmount = 0.01f;
                    healEffect = WHFx.linePolyOut(WHPal.thurmixRed, 60, 12, 3, 4, 0);
                    activeEffect = WHFx.linePolyOut(WHPal.thurmixRed, 60, range, 3, 4, 0);
                }},
                new RepairBomb(500, 180, 150){{
                    y = 70 / 2f / 4f;
                    effectRadius = 0;
                    healPercent = 3;
                    triLength = 28;
                    triWidth = 12f;
                    color = WHPal.thurmixRed;
                }}
                );

                Weapon airBCannon = new Weapon(name("airB-cannon-mount")){{
                    y = -2f / 4f;
                    x = 0;
                    reload = 200;
                    recoil = 2f;
                    rotate = true;
                    rotateSpeed = 0.5f;
                    shootSound = shootMalign;
                    velocityRnd = 0.15f;
                    inaccuracy = 2f;
                    shoot = new ShootAlternate(20 / 4f){{
                        shots = 2;
                        firstShotDelay = 20;
                        shotDelay = 15f;
                    }};

                    shootY = 16 / 4f;

                    bullet = new MultiTrailBulletType(6, 200, name("pierce")){{
                        splashDamage = damage * 0.7f;
                        splashDamageRadius = 7 * 8f;
                        shieldDamageMultiplier = 2;
                        lifetime = 320 / speed;
                        keepVelocity = false;
                        width = 10;
                        height = width * 2.4f;
                        weaveMag = 0.3f;
                        weaveScale = 12;
                        subTrailWidth = trailWidth = 2.5f;
                        trailLength = 10;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.thurmixRed;
                        shootEffect = new MultiEffect(
                        Fx.shootSmallColor,
                        WHFx.hitSpark(WHPal.thurmixRed, 20, 5, 20, 1, 5f));
                        smokeEffect = Fx.hitLaserColor;
                        hitEffect = WHFx.square(WHPal.thurmixRed, 30, 4, 30, 4);
                        despawnEffect = new MultiEffect(
                        WHFx.sharpBlast(WHPal.thurmixRed, WHPal.thurmixRedDark, 40, 40),
                        WHFx.linePolyOut(hitColor, 30, 40, 4, 4, 0),
                        WHFx.square(WHPal.thurmixRed, 30, 7, 50, 4.5f)
                        );
                        trailChance = 0.1f;
                        trailEffect = WHFx.square(WHPal.thurmixRed, 30, 2, 10, 3f);
                        pierceCap = 2;
                        frontColor = Color.white;
                        hitSound = Sounds.none;
                    }};
                }};

                weapons.addAll(
                new HealWeapon(name("airB-weapon-mount-2")){{
                    y = 21 / 4f;
                    x = 112f / 4f;
                    reload = 60;
                    ejectEffect = Fx.none;
                    recoil = 2f;
                    shootSound = WHSounds.LaserGatling;
                    inaccuracy = 0f;
                    shoot = new ShootHelix(4, 1){{
                        shots = 3;
                        shotDelay = 10;
                    }};

                    rotate = true;
                    rotateSpeed = 1.5f;

                    shootY = 16 / 4f;

                    bullet = new BasicBulletType(6f, 70){{
                        homingPower = 0.08f;
                        lifetime = 250 / speed;
                        keepVelocity = true;
                        width = 7;
                        height = width * 3.5f;
                        trailWidth = 1.7f;
                        trailLength = 4;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.thurmixRed;
                        shootEffect = Fx.shootSmallColor;
                        smokeEffect = Fx.hitLaserColor;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.square(WHPal.thurmixRed, 60, 5, 20, 4)
                        );
                        frontColor = Color.white;
                        hitSound = Sounds.none;

                        healPercent = 0.7f;
                        collidesTeam = true;
                    }};
                }},
                new PointDefenseWeapon(name("airB-weapon-mount-2")){{
                    x = 52f / 4f;
                    y = -96f / 4f;
                    reload = 12;
                    targetInterval = 10f;
                    targetSwitchInterval = 15f;

                    bullet = new BulletType(){{
                        hitColor = WHPal.thurmixRed;
                        shootEffect = Fx.sparkShoot;
                        hitEffect = Fx.pointHit;
                        maxRange = 230;
                        damage = 120;
                    }};
                }},
                new Weapon(name("airB-weapon-mount-2")){{
                    y = 80f / 4f;
                    x = 64f / 4f;
                    ejectEffect = Fx.none;
                    recoil = 2f;
                    shootSound = WHSounds.LaserGatling;
                    velocityRnd = 0.15f;
                    inaccuracy = 2f;

                    shoot = new ShootSpread(2, 5){{
                        shotDelay = 10;
                    }};

                    rotate = true;
                    rotateSpeed = 1.5f;

                    shootY = 16 / 4f;

                    reload = 50f;


                    bullet = new BasicBulletType(4.5f, 100, "circle"){{
                        shieldDamageMultiplier = 2f;
                        pierceArmor = true;
                        shrinkY = shrinkX = 0;
                        splashDamage = 100;
                        splashDamageRadius = 4 * 8f;
                        followAimSpeed = 1.5f;
                        homingPower = 0.03f;
                        weaveMag = 0.1f;
                        weaveScale = 12;
                        lifetime = 260 / speed;
                        keepVelocity = true;
                        width = height = 10;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.thurmixRed;
                        shootEffect = new MultiEffect(
                        Fx.shootSmallColor,
                        WHFx.linePolyOut(WHPal.thurmixRed, 60, 8, 2, 4, 0),
                        WHFx.hitSpark(WHPal.thurmixRed, 20, 5, 20, 1, 5f));
                        trailRotation = true;
                        trailChance = 0.1f;
                        trailInterval = 5f;
                        trailEffect = WHFx.hitCircle(hitColor, hitColor, 10, 1, 5, 12);
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.linePolyOut(WHPal.thurmixRed, 60, 40, 3, 4, 0),
                        WHFx.square(WHPal.thurmixRed, 60, 8, 40, 5),
                        WHFx.generalExplosion(60, hitColor, 40, 10, false)
                        );
                    }};
                }},
                copyAndMove(airBCannon, 85f / 4f, -39f / 4f)
                );
            }
        }

        ;


        airB6 = new UnitType("airB6"){
            {
                researchCostMultiplier = 0.7f;
                constructor = UnitTypes.mega.constructor;
                payloadCapacity = (6 * 6) * tilePayload;

                flying = true;
                speed = 0.6f;
                rotateSpeed = 0.8f;
                accel = 0.05f;
                drag = 0.017f;
                health = 80000;
                armor = 25;
                engineLayer = Layer.flyingUnitLow - 1f;
                engineOffset = 225 / 4f;
                engineSize = 8;
                trailLength = 10;

                hitSize = 80f;
                lowAltitude = true;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new PowerAmmoType(30000);

                range = 400;

                parts.add(
                new RegionPart("-tail"){{
                    layerOffset = 0.13f;
                }}
                );

                float engineSize = 1.8f;

                for(int a = 0; a < 10; a++){
                    AncientEngine.addEngine(this, 173 / 4f + a * engineSize / 2, -104 / 4f, 0, engineSize, true);
                }
                engines.addAll(
                new WHUnitEngine(102 / 4f, -176 / 4f + 20, 0, 45, 37f / 8f){{
                    line = true;
                }},
                new WHUnitEngine(-102 / 4f, -176 / 4f + 20, 0, 45, 37f / 8f){{
                    line = true;
                }}
                );
                float r = 200;
                abilities.addAll(
                new BoostAbility(1.5f, 15f),
                new ShockAbility(){{
                    range = r;
                    reload = 60 * 4;
                    bulletDamage = 450;
                    falloffCount = 10;
                    waveColor = WHPal.thurmixRed;
                }},
                new AdaptedHealAbility(500, 60f * 6, r){{
                    selfHealReloadTime = 360;
                    selfHealAmount = 0.003f;
                    healEffect = WHFx.linePolyOut(WHPal.thurmixRed, 60, 12, 3, 4, 0);
                    activeEffect = WHFx.linePolyOut(WHPal.thurmixRed, 60, range, 3, 4, 0);
                }});

                weapons.addAll(
                new HealWeapon(name("airB-weapon-mount-1")){{
                    y = 125f / 4f;
                    x = 86f / 4f;
                    reload = 45;
                    layerOffset = -0.1f;
                    ejectEffect = Fx.none;
                    recoil = 2f;
                    shootSound = WHSounds.machineGunShoot;
                    inaccuracy = 3f;
                    shoot = new ShootAlternate(){{
                        shots = 6;
                        shotDelay = 4;
                        spread = 16f / 4f;
                    }};

                    rotate = true;
                    rotateSpeed = 28f / 4f;

                    shootY = 16 / 4f;

                    bullet = new BasicBulletType(6f, 60){{
                        pierceArmor = true;
                        lifetime = 280 / speed;
                        keepVelocity = false;
                        width = 7;
                        height = width * 3.5f;
                        trailWidth = 1.7f;
                        trailLength = 4;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.thurmixRed;
                        shootEffect = Fx.shootSmallColor;
                        smokeEffect = Fx.hitLaserColor;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.linePolyOut(hitColor, 20, 30, 3, 4, 0),
                        WHFx.square(WHPal.thurmixRed, 60, 5, 20, 4)
                        );
                        frontColor = Color.white;
                        hitSound = Sounds.none;

                        pierceCap = 2;
                        lightning = 2;
                        lightningLength = 5;
                        lightningDamage = 15;

                        healAmount = 100;
                        healPercent = 0.7f;
                        collidesTeam = true;
                    }};
                }},
                new Weapon(name("airB6-weapon2")){{
                    y = -51f / 4f;
                    x = 154f / 4f;
                    reload = 200;
                    recoil = 2f;
                    rotate = false;
                    shootCone = 15f;
                    shootSound = shootMissileSmall;
                    velocityRnd = 0.15f;
                    inaccuracy = 2f;
                    layerOffset = -0.1f;
                    shoot = new ShootPattern(){{
                        shots = 3;
                        firstShotDelay = 20;
                        shotDelay = 15f;
                    }};

                    shootY = 63f / 4f;
                    shootX = -4f / 4f;

                    bullet = new CritMissileBulletType(6, 200, "missile-large"){{

                        shieldDamageMultiplier = 1.4f;
                        critMultiplier = 1.4f;
                        splashDamage = damage * 2;
                        splashDamageRadius = 8 * 8f;
                        lifetime = 450 / speed;
                        keepVelocity = false;
                        width = 10;
                        height = width * 3;
                        lengthOffset = height / 3f - 8f;
                        flameWidth = 5;
                        flameLength = 15f;
                        trailWidth = 2.5f;
                        trailLength = 10;
                        lightningDamage = 50;
                        lightning = 2;
                        lightningLength = 6;
                        lightningLengthRand = 9;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.thurmixRed;
                        colors = new Color[]{hitColor.cpy().a(0.55f), hitColor.cpy().a(0.7f), hitColor.cpy().a(0.9f), hitColor.cpy().lerp(Color.white, 0.8f)};
                        shootEffect = new MultiEffect(
                        Fx.shootSmallColor,
                        WHFx.hitSpark(WHPal.thurmixRed, 20, 5, 20, 1, 5f));
                        smokeEffect = Fx.hitLaserColor;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.generalExplosion(70, hitColor, splashDamageRadius, 5, false),
                        WHFx.sharpBlast(hitColor, WHPal.thurmixRedDark, 40, 40),
                        WHFx.square(hitColor, 30, 7, 50, 4.5f)
                        );
                        trailChance = 0.12f;
                        trailEffect = WHFx.square(WHPal.thurmixRed, 30, 2, 10, 6f);
                        frontColor = Color.white;
                        hitSound = Sounds.none;
                        homingDelay = 12;
                        homingPower = 0.06f;
                        followAimSpeed = 1.5f;
                    }};
                }},
                new Weapon(name("airB6-weapon3")){{
                    y = -107f / 4f;
                    x = 0 / 4f;
                    reload = 300;
                    mirror = false;
                    recoil = 2f;
                    rotate = true;
                    rotationLimit = 30;
                    rotateSpeed = 0.5f;
                    shootCone = 15f;
                    shootSound = shootRipple;
                    velocityRnd = 0.03f;
                    layerOffset = 0.15f;
                    parentizeEffects = true;
                    recoilTime = 60f;

                    parts.addAll(
                    new RegionPart("-barrel1"){{
                        mirror = false;
                        under = true;
                        heatProgress = progress = PartProgress.recoil;
                        heatColor = WHPal.thurmixRed;
                        moveY = -6f;
                    }}
                    );

                    shoot.firstShotDelay = 40f;

                    shootY = 152f / 4f;

                    bullet = new TrailFadeBulletType(12, 800, name("pierce")){{

                        pierceCap = 2;
                        pierceBuilding = true;

                        shieldDamageMultiplier = 3f;

                        pierceArmor = true;

                        hitSize = 12f;
                        splashDamage = 400;
                        splashDamageRadius = 10 * 8f;
                        lifetime = 460 / speed;
                        keepVelocity = false;
                        width = 10;
                        height = width * 3;
                        trailWidth = 2;
                        trailLength = 12;
                        lightningDamage = 50;
                        lightning = 3;
                        lightningLength = 6;
                        lightningLengthRand = 9;
                        frontColor = WHPal.SkyBlue;
                        trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.SkyBlueF;
                        chargeEffect = new MultiEffect(
                        WHFx.genericCharge(frontColor, 8, 30, shoot.firstShotDelay)
                        );
                        shootEffect = new MultiEffect(
                        shootSmallColor,
                        WHFx.shootLine(30, 30),
                        WHFx.lineCircleOut(hitColor, 30, 20, 4));
                        smokeEffect = Fx.hitLaserColor;
                        despawnEffect = new MultiEffect(
                        WHFx.lineCircleOut(hitColor, 10, splashDamageRadius, 2.5f),
                        WHFx.hitPoly(hitColor, hitColor, 60, 12, splashDamageRadius, 10, 6, 60),
                        WHFx.explosionSmokeEffect(hitColor, 120, splashDamageRadius, 20, 12f),
                        WHFx.trailCircleHitSpark(hitColor, 120, 25, splashDamageRadius + 20, 1.5f, 10)
                        );
                        trailChance = 0.3f;
                        trailInterval = 3f;
                        hitEffect = WHFx.generalExplosion(90, hitColor, splashDamageRadius, 20, false);
                        /*  trailEffect = WHFx.hitCircle(hitColor, hitColor,30, 2, 10, 8f);*/
                        trailEffect = WHFx.square(hitColor, 30, 2, 10, 3f);
                        frontColor = Color.white;
                        hitSound = Sounds.none;
                    }};
                }},
                new Weapon(name("airB-weapon-mount-4")){{
                    y = -71f / 4f;
                    x = 51f / 4f;
                    reload = 90;
                    ejectEffect = Fx.none;
                    layerOffset = 0.005f;
                    recoil = 2f;
                    shootSound = WHSounds.machineGunShoot;
                    inaccuracy = 4f;
                    velocityRnd = 0.15f;
                    xRand = 0.3f;
                    recoils = 4;

                    shoot = new ShootAlternate(){{
                        shots = 10;
                        barrels = 4;
                        spread = 0.1f;
                        shotDelay = 6;
                        recoilTime = shotDelay;
                    }};

                    rotate = true;
                    rotateSpeed = 0.8f;

                    shootY = 80f / 4f;
                    shootX = -8f / 4f;

                    float x1 = -15f / 4f, x2 = -5 / 4f, x3 = 5f / 4f,
                    move = 10f / 4f, y1 = 52 / 4f;

                    parts.addAll(
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.0005f;
                        x = x1;
                        y = y1;
                        heatProgress = progress = PartProgress.recoil.mul(0).add(p -> Mathf.sin(p.recoil * Mathf.PI, 1, 1));
                        heatColor = WHPal.thurmixRed;
                        moveX = move;
                        recoilIndex = 0;
                    }},
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.001f;
                        x = x2;
                        y = y1;
                        heatProgress = progress = PartProgress.recoil.mul(0).add(p -> Mathf.sin(p.recoil * Mathf.PI, 1, 1));
                        heatColor = WHPal.thurmixRed;
                        moveX = move;
                        recoilIndex = 1;
                    }},
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.002f;
                        x = x3;
                        y = y1;
                        heatProgress = progress = PartProgress.recoil.mul(0).add(p -> Mathf.sin(p.recoil * Mathf.PI, 1, 1));
                        heatColor = WHPal.thurmixRed;
                        moveX = -move;
                        recoilIndex = 2;
                    }},
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.0015f;
                        x = x2;
                        y = y1;
                        heatProgress = progress = PartProgress.recoil.mul(0).add(p -> Mathf.sin(p.recoil * Mathf.PI, 1, 1));
                        heatColor = WHPal.thurmixRed;
                        moveX = -move;
                        recoilIndex = 3;
                    }}
                    );

                    bullet = new BasicBulletType(7f, 120, "circle"){{
                        lifetime = 420 / speed;
                        keepVelocity = false;
                        splashDamage = damage;
                        splashDamageRadius = 4 * 8f;
                        shieldDamageMultiplier = 1.5f;
                        width = height = 8;
                        trailWidth = width / 2.3f;
                        lightning = 1;
                        lightningDamage = 30;
                        lightningLength = 7;
                        lightningLengthRand = 7;
                        trailLength = 8;
                        shrinkX = shrinkY = 0;
                        trailColor = hitColor = frontColor = backColor = lightColor = lightningColor = WHPal.thurmixRed;
                        mixColorFrom = mixColorTo = hitColor;
                        shootEffect = new MultiEffect(
                        WHFx.shootLine(10, 20),
                        Fx.shootBig);
                        smokeEffect = Fx.hitLaserColor;
                        trailChance = 0.2f;
                        hitEffect = despawnEffect = new MultiEffect(
                        new Effect(10, e -> {
                            Draw.color(hitColor);
                            stroke(3f * e.fout());
                            Lines.circle(e.x, e.y, splashDamageRadius * e.fin() + 3f);
                        }),
                        WHFx.generalExplosion(40, hitColor, 40, 8, false)
                        );
                        frontColor = Color.white;
                        hitSound = Sounds.none;
                    }};
                }});

            }
        };

        M6 = new UnitType("m6"){
            {

                constructor = MechUnit::create;
                speed = 0.9f;
                hitSize = 30f;
                rotateSpeed = 1.65f;
                health = 68000;
                armor = 36;
                mechStepParticles = true;
                stepShake = 0.75f;
                drownTimeMultiplier = 1.6f;
                mechFrontSway = 1f;
                mechSideSway = 0.3f;
                ammoType = new ItemAmmoType(WHItems.ceramite);

                singleTarget = true;

                abilities.add(
                new EllipseForceFieldAbility(180 / 4f, 120 / 4f, 6f, 3500, 80 * 60f, 0.8f, 18){{
                    percentRegen = true;
                    percentRegenAmount = 0.03f;
                }},
                new ShockAbility(){{
                    range = 120;
                    reload = 60 * 6;
                    bulletDamage = 500;
                }},
                new ShieldRegenFieldAbility(130, 3000, 60f * 1.5f, 60f));

                outlineRadius = 3;
                mechLegColor = outlineColor = WHPal.Outline;

                weapons.add(
                new Weapon(name("m6-weapon1")){{
                    layerOffset = -0.001f;
                    y = 16 / 4f;
                    x = -92 / 4f;
                    mirror = false;
                    shootY = 53 / 4f;
                    reload = 125;
                    recoil = 5f;
                    recoilTime = 90;
                    shake = 2f;
                    ejectEffect = Fx.casing4;
                    shootSound = shootSmite;

                    shoot = new ShootPattern(){{
                        shots = 2;
                        firstShotDelay = 20;
                        shotDelay = 15;
                    }};

                    bullet = new TrailFadeBulletType(10, 800, name("energy-bullet")){
                        {
                            lifetime = 330 / speed;

                            hitBlinkTrail = false;

                            buildingDamageMultiplier = 1.5f;

                            width = 15;
                            height = width * 2.7f;

                            frontColor = WHPal.SkyBlueF;
                            lightColor = backColor = trailColor = hitColor = lightningColor = WHPal.SkyBlue;
                            lightning = 2;
                            lightningDamage = 60;
                            lightningLength = 10;
                            lightningLengthRand = 10;

                            shootEffect = new MultiEffect(
                            WHFx.plasmaShoot(hitColor, 30, 3, 20)
                            );

                            chargeEffect = new MultiEffect(
                            WHFx.trailCharge(frontColor, 10, 2, 40, 3, shoot.firstShotDelay + 1).followParent(true),
                            WHFx.genericChargeCircle(frontColor, 6, splashDamageRadius * 0.5f, shoot.firstShotDelay + 1).followParent(true)
                            );

                            status = WHStatusEffects.plasma;
                            statusDuration = 60;

                            trailLength = 10;
                            trailWidth = width / 4f;
                            trailInterval = 1f;
                            trailEffect = new MultiEffect(
                            WHFx.hitPoly(hitColor, hitColor, 30, 1, 15, 3, 6, 60)
                            );

                            splashDamage = damage / 2;
                            splashDamageRadius = 40;

                            hitEffect = new MultiEffect(
                            WHFx.generalExplosion(10, hitColor, 60, 10, false),
                            WHFx.hitSpark(hitColor, 45, 10, 60, 1.5f, 10),
                            WHFx.hitPoly(hitColor, hitColor, 30, 10, 30, 7, 6, 60)
                            );
                            despawnEffect = new MultiEffect(
                            WHFx.generalExplosion(20, hitColor, splashDamageRadius, 10, true),
                            WHFx.instRotation(hitColor, 60, splashDamageRadius * 1.2f, 0, false),
                            WHFx.circleOut(hitColor, 60, splashDamageRadius),
                            WHFx.lineCircleOut(hitColor, 60, splashDamageRadius / 2, 4),
                            WHFx.trailCircleHitSpark(hitColor, 90, 8, splashDamageRadius * 2, 1.6f, 15)
                            );
                        }
                    };
                }}
                );
                weapons.addAll(
                new Weapon(name("machine-gun-3")){{
                    layerOffset = -0.001f;
                    y = -12f / 4f;
                    x = 100f / 4f;
                    reload = 70;
                    mirror = false;
                    ejectEffect = casing3Double;
                    recoil = 2f;
                    shootSound = WHSounds.machineGunShoot;
                    inaccuracy = 4f;
                    velocityRnd = 0.15f;
                    xRand = 0.3f;
                    recoils = 4;

                    shoot = new ShootAlternate(){{
                        shots = 7;
                        barrels = 4;
                        spread = 3 / 4f;
                        shotDelay = 6;
                        recoilTime = shotDelay;
                    }};

                    shootY = 80f / 4f;
                    shootX = 2f / 4f;


                    float x1 = -15f / 4f, x2 = -5 / 4f, x3 = 5f / 4f,
                    move = 10f / 4f, y1 = 52 / 4f;

                    parts.addAll(
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.0012f;
                        x = x1;
                        y = y1;
                        heatProgress = progress = PartProgress.recoil;
                        heatColor = WHPal.thurmixRed;
                        moveX = move;
                        recoilIndex = 0;
                    }},
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.001f;
                        x = x2;
                        y = y1;
                        heatProgress = progress = PartProgress.recoil;
                        heatColor = WHPal.thurmixRed;
                        moveX = move;
                        recoilIndex = 1;
                    }},
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.0014f;
                        x = x3;
                        y = y1;
                        heatProgress = progress = PartProgress.recoil;
                        heatColor = WHPal.thurmixRed;
                        moveX = -move;
                        recoilIndex = 2;
                    }},
                    new RegionPart("-barrel"){{
                        under = true;
                        layerOffset = -0.0013f;
                        x = x2;
                        y = y1;
                        heatProgress = progress = PartProgress.recoil;
                        heatColor = WHPal.thurmixRed;
                        moveX = -move;
                        recoilIndex = 3;
                    }}
                    );

                    bullet = new CritBulletType(12f, 160, name("pierce")){
                        {
                            pierceArmor = true;
                            lifetime = 300 / speed;
                            keepVelocity = false;
                            splashDamage = damage * 0.75f;
                            pierceCap = 3;
                            pierceBuilding = true;
                            width = 10;
                            height = width * 3;
                            trailWidth = width / 3;
                            trailLength = 5;
                            shrinkX = shrinkY = 0;
                            frontColor = WHPal.ShootOrangeLight;
                            trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.ShootOrange;
                            mixColorFrom = mixColorTo = hitColor;
                            shootEffect = new MultiEffect(
                            WHFx.shootLine(10, 20),
                            Fx.shootBig);
                            smokeEffect = Fx.hitLaserColor;
                            trailChance = 0.5f;
                            trailEffect = WHFx.square(hitColor, 30, 2, 30, 4);
                            hitEffect = despawnEffect = new MultiEffect(
                            WHFx.instHit(hitColor, true, 2, splashDamageRadius),
                            WHFx.square(hitColor, 35, 10, splashDamageRadius, 6),
                            WHFx.generalExplosion(12, hitColor, 40, 8, false)
                            );
                            hitSound = Sounds.none;
                        }

                        @Override
                        public void hitEntity(Bullet b, Hitboxc other, float initialHealth){
                            super.hitEntity(b, other, initialHealth);
                            if(other instanceof Unit u){
                                if(u.hitSize > 72) u.damagePierce(damage * 0.6f);
                            }
                        }
                    };
                }}
                );
                weapons.add(
                new Weapon(name("laser-weapon-1")){
                    {
                        y = -23 / 4f;
                        x = 0;
                        reload = 40;
                        layerOffset = 0.001f;
                        ejectEffect = Fx.none;
                        recoil = 2f;
                        shootSound = WHSounds.machineGunShoot;
                        inaccuracy = 3f;
                        mirror = false;
                        shoot = new ShootAlternate(){{
                            shotDelay = 4;
                            shots = 7;
                            spread = 16f / 4f;
                        }};

                        rotate = true;
                        rotateSpeed = 1.5f;

                        shootY = 16 / 4f;

                        bullet = new CritBulletType(6f, 60){{
                            lifetime = 250 / speed;
                            keepVelocity = false;
                            width = 7;
                            height = width * 3.5f;
                            trailWidth = 1.7f;
                            trailLength = 3;
                            frontColor = WHPal.ShootOrangeLight;
                            trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.ShootOrange;
                            shootEffect = Fx.shootSmallColor;
                            smokeEffect = Fx.hitLaserColor;
                            hitEffect = despawnEffect = new MultiEffect(
                            WHFx.hitSpark(hitColor, 20, 5, 30, 1.2f, 9f),
                            WHFx.square(hitColor, 60, 5, 20, 4));

                            hitSound = Sounds.none;

                            pierceCap = 2;

                        }};
                    }
                }
                );
            }
        }

        ;

        M5 = new

        PowerArmourUnitType("m5"){
            {
                speed = 1.2f;
                hitSize = 40;
                rotateSpeed = 2;
                health = 28000;
                armor = 32;
                mechFrontSway = 0.08f;
                ammoType = new ItemAmmoType(WHItems.ceramite);

                mechStepParticles = true;
                stepShake = 0.15f;
                singleTarget = true;

                abilities.add(
                new EllipseForceFieldAbility(120 / 4f, 80 / 4f, 5, 2000, 70 * 60f, 0.8f, 15){{
                    percentRegen = true;
                    percentRegenAmount = 0.03f;
                }},
                new ShockAbility(){{
                    range = 90;
                    reload = 60 * 6;
                    bulletDamage = 300;
                }},
                new ShieldRegenFieldAbility(130, 2000, 60f * 1.5f, 60f));

                outlineRadius = 3;
                mechLegColor = outlineColor = WHPal.Outline;
                weapons.add(
                new MainWeapon(""){{
                    y = 0f;
                    x = 37 / 4f;
                    reload = 6;

                    shootY = 100 / 4f;

                    recoil = 5f;
                    shake = 2f;

                    mirror = false;
                    recoilTime = 15;

                    shootWarmupSpeed = 0.09f;
                    minWarmup = 0.99f;

                    inaccuracy = 3f;

                    shootSound = shootLaser;

                    float reY = -8f / 4f, reRot = -6f;
                    float bw = 8 / 4f, bh = 54 / 4f;

                    shoot = new ShootAlternate(){{
                        barrels = 3;
                        spread = bw;
                    }};
                    unitParts.addAll(
                    new UnitRegionPart("weapon2"){
                        {
                            x = 37 / 4f;
                            y = 31 / 4f;

                            moveX = 5 / 4f;
                            moveY = -12 / 4f;
                            moveRot = -70;

                            rotation = 70;
                            sinAngle = 10;
                            bodyScl = 12;
                            swingScl = 0.005f;
                            swingFront = false;

                            layerOffset = -0.01f;

                            moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                            children.addAll(
                            new UnitRegionPart("weapon2-barrel"){{
                                x = -bw;

                                sinAngle = 10;
                                bodyScl = 12;
                                swingScl = 0.005f;
                                swingFront = false;

                                layerOffset = -0.011f;

                                heatColor = WHPal.thurmixRed;

                                heatProgress = UnitPartProgress.recoil;
                                moves.add(new UnitPartMove(UnitPartProgress.reload.curve(Interp.smooth), 2 * bw, reY, 0));
                                moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                            }},
                            new UnitRegionPart("weapon2-barrel"){{
                                x = 0;

                                sinAngle = 10;
                                bodyScl = 12;
                                swingScl = 0.005f;
                                swingFront = false;

                                layerOffset = -0.012f;

                                heatColor = WHPal.thurmixRed;

                                heatProgress = UnitPartProgress.recoil;
                                moves.add(new UnitPartMove(UnitPartProgress.reload.curve(Interp.smooth), bw, reY, 0));
                                moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                            }},
                            new UnitRegionPart("weapon2-barrel"){{
                                x = bw;

                                sinAngle = 10;
                                bodyScl = 12;
                                swingScl = 0.005f;
                                swingFront = false;

                                layerOffset = -0.013f;

                                heatColor = WHPal.thurmixRed;

                                heatProgress = UnitPartProgress.recoil;
                                moves.add(new UnitPartMove(UnitPartProgress.reload.curve(Interp.smooth), -2 * bw, reY, 0));
                                moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                            }});
                        }
                    },

                    //l
                    new UnitRegionPart("forearm"){{
                        x = -52 / 4f;
                        y = 12 / 4f;

                        moveX = 50 / 4f;
                        moveY = 30 / 4f;
                        moveRot = -20;

                        rotation = -45;
                        sinAngle = 10;
                        bodyScl = 12;
                        swingScl = 0.005f;
                        swingFront = false;

                        layerOffset = -0.002f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("body"){{
                        x = y = 0;
                        moveRot = -30;
                        sinAngle = 4;
                        bodyScl = 5;
                        swingFront = false;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, 0, reRot));
                    }},

                    //left
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        moveX = 16 / 4f;
                        moveY = 10 / 4f;
                        moveRot = -30;
                        bodyScl = 12;
                        swingScl = 0.01f;
                        symmetry = true;
                        swingFront = false;
                        outlineLayerOffset = -0.01f;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, -8f / 4f, 0));
                    }},
                    //right
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        moveX = 5 / 4f;
                        moveY = -3 / 4f;
                        moveRot = -15;
                        bodyScl = 12;
                        swingScl = 0.01f;
                        swingFront = false;
                        outlineLayerOffset = -0.01f;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("head"){{
                        x = y = 0;
                        layerOffset = 0.04f;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.008f;
                        outline = false;
                        swingFront = false;
                    }}
                    );
                    bullet = new DelayedPointBulletType(){{
                        recoil = 0.08f;
                        delayEffectLifeTime = 15;
                        splashDamage = damage = 120;
                        splashDamageRadius = 24;
                        pierceArmor = true;
                        maxRange = range = 300;
                        width = 13f;
                        square = true;
                        Color c = lightningColor = Pal.slagOrange.cpy().lerp(Pal.orangeSpark, 0.4f).cpy();
                        colors = new Color[]{c.a(0.3f), c.a(0.7f), c, Color.white};
                    }};
                }}
                );
                weapons.add(
                new Weapon(name("m5-weapon1")){
                    {
                        x = 0f;
                        y = -24 / 4f;
                        reload = 150;
                        rotate = true;
                        mirror = false;
                        rotateSpeed = 1.5f;
                        shootSound = WHSounds.missileShoot;
                        inaccuracy = 6;
                        shootY = 15 / 4f;

                        minWarmup = 0.99f;
                        shootWarmupSpeed = 0.09f;

                        layerOffset = 0.1f;

                        parts.addAll(
                        new AimLaserPart(){{
                            layerOffset = 0.001f;
                            alpha = PartProgress.warmup.mul(0.7f).add(0.3f);
                            blending = Blending.additive;
                            length = 70;
                            width = 4 / 4f;
                            y = 11 / 4f;
                        }}
                        );
                        shoot =
                        new ShootMulti(
                        new ShootAlternate(){{
                            barrels = 2;
                            spread = 22 / 4f;
                            shots = 2;
                            shotDelay = 20;
                        }},
                        new ShootSpread(){{
                            spread = 15 / 4f;
                            shots = 6;
                            shotDelay = 3f;
                        }});
                        bullet = new CritMissileBulletType(6, 90, name("large-missile")){
                            {
                                pierceArmor = true;
                                width = 10;
                                height = width * 2.2f;
                                shrinkX = shrinkY = 0f;

                                lengthOffset = width - 2;
                                flameWidth = 2.5f;
                                flameLength = 15f;

                                weaveMagOnce = true;
                                weaveMag = 0.4f;
                                weaveRandom = true;
                                drawMissile = true;
                                lightningColor = hitColor = trailColor = WHPal.ShootOrange;

                                Color c = WHPal.ShootOrange.cpy().lerp(Color.orange, 0.25f);
                                colors = new Color[]{c.a(0.55f), c.a(0.7f), c.a(0.8f), c, Color.white};

                                lifetime = 350 / speed;
                                trailLength = 6;
                                trailWidth = 1.5f;
                                statusDuration = 60;
                                homingDelay = lifetime / 2f;
                                homingPower = 0.01f;
                                followAimSpeed = 0.1f;

                                despawnEffect = hitEffect = new MultiEffect(
                                WHFx.generalExplosion(10, hitColor, 25, 5, false),
                                WHFx.instHit(hitColor, true, 2, 30f)
                                );
                                smokeEffect = hugeSmoke;
                                shootEffect = shootBigColor;
                                lightningDamage = 40;
                                lightning = 2;
                                lightningLength = 12;
                            }
                        };
                    }
                }
                );
            }
        }

        ;

        M4A = new

        PowerArmourUnitType("m4A"){
            {
                speed = 1.5f;
                hitSize = 28f;
                rotateSpeed = 1.6f;
                health = 12000;
                armor = 28;
                mechFrontSway = 0.08f;
                ammoType = new ItemAmmoType(WHItems.ceramite);

                mechStepParticles = true;
                stepShake = 0.15f;
                singleTarget = true;

                abilities.add(
                new ShockAbility(){{
                    range = 60;
                    reload = 60 * 5;
                    bulletDamage = 200;
                }},
                new ShieldRegenFieldAbility(100, 1000, 60f * 1, 60f));

                outlineRadius = 3;
                mechLegColor = outlineColor = WHPal.Outline;

                weapons.add(
                new MainWeapon(""){{
                    y = 0f;
                    x = 33 / 4f;
                    reload = 15;

                    shootY = 70 / 4f;

                    recoil = 5f;
                    shake = 2f;

                    mirror = false;
                    cooldownTime = recoilTime = 30;

                    shootWarmupSpeed = 0.09f;
                    minWarmup = 0.99f;

                    shootSound = shootCyclone;

                    float reY = -8f / 4f, reRot = -6f;
                    unitParts.addAll(
                    new UnitRegionPart("weapon"){
                        {
                            x = 23 / 4f;
                            y = 32 / 4f;

                            moveX = 10 / 4f;
                            moveY = -8 / 4f;
                            moveRot = -65;

                            rotation = 70;
                            sinAngle = 10;
                            bodyScl = 12;
                            swingScl = 0.005f;
                            swingFront = false;

                            heatProgress = UnitPartProgress.recoil;

                            moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                        }
                    },

                    new UnitRegionPart("hand"){{
                        x = 23 / 4f;
                        y = 32 / 4f;

                        moveX = 10 / 4f;
                        moveY = 10 / 4f;
                        moveRot = -65;

                        rotation = 70;
                        sinAngle = 10;
                        bodyScl = 12;
                        swingScl = 0.005f;
                        swingFront = false;

                        layerOffset = -0.001f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("forearm"){{
                        x = -52 / 4f;
                        y = 12 / 4f;

                        moveX = 50 / 4f;
                        moveY = 30 / 4f;
                        moveRot = -20;

                        rotation = -45;
                        sinAngle = 10;
                        bodyScl = 12;
                        swingScl = 0.005f;
                        swingFront = false;

                        layerOffset = -0.002f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    //rightHand
                    new UnitRegionPart("hand"){{
                        x = -35 / 4f;
                        y = 28 / 4f;

                        moveX = 68 / 4f;
                        moveY = -10 / 4f;
                        moveRot = 40;

                        rotation = 30;
                        sinAngle = 10;
                        bodyScl = 12;
                        symmetry = true;
                        swingFront = false;

                        layerOffset = -0.001f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("body"){{
                        x = y = 0;
                        moveRot = -30;
                        sinAngle = 4;
                        bodyScl = 5;
                        swingFront = false;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, 0, reRot));
                    }},

                    //left
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        moveX = 16 / 4f;
                        moveY = 10 / 4f;
                        moveRot = -30;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.008f;
                        symmetry = true;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, -8f / 4f, 0));
                    }},
                    //right
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        moveX = 5 / 4f;
                        moveY = -9 / 4f;
                        moveRot = -15;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.008f;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("head"){{
                        x = y = 0;
                        layerOffset = 1;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.008f;
                        outline = false;
                        swingFront = false;
                    }}
                    );

                    bullet = new CritBulletType(12, 120, name("pierce")){{

                        splashDamage = damage;
                        splashDamageRadius = 32;
                        width = 12;
                        height = width * 2;
                        lifetime = 320 / speed;

                        pierceCap = 2;
                        pierceBuilding = true;

                        recoil = 0.3f;
                        knockback = 1.2f;

                        shrinkY = 0;
                        frontColor = WHPal.WHYellow2;
                        trailColor = hitColor = backColor = WHPal.ShootOrange;
                        trailLength = 5;
                        trailWidth = width / 4;
                        smokeEffect = shootSmallSmoke;

                        ejectEffect = casing4;

                        shootEffect = new MultiEffect(
                        Fx.shootBig,
                        WHFx.shootLine(30, 30)
                        );

                        trailChance = 0.3f;
                        trailInterval = 3;
                        trailEffect = WHFx.square(hitColor, 15, 2, 20, 6);

                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.generalExplosion(10, hitColor, splashDamageRadius, 12, false),
                        WHFx.square(hitColor, 10, 4, 25, 4),
                        WHFx.trailHitSpark(hitColor, 20, 5, splashDamageRadius, 1.5f, 10),
                        WHFx.hitSpark(WHPal.ShootOrange, 20, 4, 25, 2, 8));
                    }};
                }}
                );
            }
        }

        ;

        M4B = new

        PowerArmourUnitType("m4B"){
            {
                speed = 1.5f;
                hitSize = 28f;
                rotateSpeed = 1.6f;
                health = 12000;
                armor = 28;
                mechFrontSway = 0.08f;
                ammoType = new ItemAmmoType(WHItems.ceramite);

                mechStepParticles = true;
                stepShake = 0.15f;
                singleTarget = true;

                abilities.add(
                new ShockAbility(){{
                    range = 60;
                    reload = 60 * 5;
                    bulletDamage = 200;
                }},
                new ShieldRegenFieldAbility(100, 1000, 60f * 1, 60f));

                outlineRadius = 3;
                mechLegColor = outlineColor = WHPal.Outline;

                weapons.add(
                new MainWeapon(""){{
                    y = 0f;
                    x = 33 / 4f;
                    reload = 180;

                    layerOffset = 0.01f;

                    shootY = 70 / 4f;

                    recoil = 5f;
                    shake = 2f;

                    mirror = false;
                    cooldownTime = recoilTime = 120;

                    shootWarmupSpeed = 0.09f;
                    minWarmup = 0.99f;

                    shootSound = shootCyclone;

                    float reY = -13f / 4f, reRot = 0f;
                    unitParts.addAll(
                    new UnitRegionPart("weapon"){
                        {
                            x = 23 / 4f;
                            y = 32 / 4f;

                            moveX = 10 / 4f;
                            moveY = -8 / 4f;
                            moveRot = -65;

                            rotation = 70;
                            sinAngle = 3;
                            bodyScl = 12;
                            swingScl = 0.005f;
                            swingFront = false;

                            heatProgress = UnitPartProgress.recoil;

                            layerOffset = -0.02f;

                            moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                        }
                    },

                    new UnitRegionPart("hand"){{
                        x = -23 / 4f;
                        y = 32 / 4f;

                        moveX = 45 / 4f;
                        moveY = 10 / 4f;
                        moveRot = -20;

                        rotation = -70;
                        sinAngle = 3;
                        bodyScl = 12;
                        swingScl = 0.005f;
                        swingFront = false;

                        layerOffset = -0.01f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("forearm"){{
                        x = -52 / 4f;
                        y = 12 / 4f;

                        moveX = 32 / 4f;
                        moveY = 12 / 4f;
                        moveRot = -20;

                        rotation = -45;
                        sinAngle = 3;
                        bodyScl = 12;
                        swingScl = 0.005f;
                        swingFront = false;

                        layerOffset = -0.02f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    //rightHand
                    new UnitRegionPart("hand"){{
                        x = -35 / 4f;
                        y = 28 / 4f;

                        moveX = 68 / 4f;
                        moveY = -10 / 4f;
                        moveRot = 40;

                        rotation = 30;
                        sinAngle = 10;
                        bodyScl = 12;
                        swingScl = 0.005f;
                        symmetry = true;
                        swingFront = false;

                        layerOffset = -0.03f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("body"){{
                        x = y = 0;
                        moveRot = -30;
                        sinAngle = 4;
                        bodyScl = 5;
                        swingFront = false;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, 0, reRot));
                    }},

                    //left
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        moveX = 16 / 4f;
                        moveY = 10 / 4f;
                        moveRot = -30;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.008f;
                        symmetry = true;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    //right
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        moveX = 5 / 4f;
                        moveY = -9 / 4f;
                        moveRot = -15;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.008f;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("head"){{
                        x = y = 0;
                        layerOffset = 1;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.008f;
                        outline = false;
                        swingFront = false;
                    }}
                    );

                    continuous = true;

                    bullet = new LaserBeamBulletType(){{
                        damage = 60;
                        width = 12;
                        length = 50;
                        moveInterp = Interp.pow3Out;
                        extensionProportion = 190 / length;
                        damageInterval = 6;
                        damageMult = 4.5f;
                        lifetime = 120;

                        pierceCap = 3;

                        rotate = true;
                        rotateAngle = 3;
                        moveSpeed = 0.05f;

                        Color c = hitColor = WHPal.ShootOrange.cpy();
                        colors = new Color[]{c.a(0.2f), c.a(0.5f), c.a(0.7f), c, Color.white};

                        hitEffect = new MultiEffect(
                        WHFx.generalExplosion(10, hitColor, 40, 2, false),
                        WHFx.hitCircle(hitColor, Color.lightGray, 15, 6, 40, 8),
                        WHFx.hitSpark(hitColor, 20, 6, 40, 1.5f, 10)
                        );
                    }};
                }}
                );
            }
        }

        ;

        M4C = new

        PowerArmourUnitType("m4C"){
            {
                speed = 1.5f;
                hitSize = 28f;
                rotateSpeed = 1.6f;
                health = 12000;
                armor = 28;
                mechFrontSway = 0.08f;
                ammoType = new ItemAmmoType(WHItems.ceramite);

                mechStepParticles = true;
                stepShake = 0.15f;
                singleTarget = true;

                abilities.add(
                new ShockAbility(){{
                    range = 60;
                    reload = 60 * 5;
                    bulletDamage = 200;
                }},
                new ShieldRegenFieldAbility(100, 1000, 60f * 1, 60f));

                outlineRadius = 3;
                mechLegColor = outlineColor = WHPal.Outline;

                weapons.add(
                new MainWeapon(""){{
                    y = 0f;
                    x = 33 / 4f;
                    reload = 150;

                    shootY = 70 / 4f;

                    recoil = 5f;
                    shake = 2f;

                    mirror = false;
                    cooldownTime = recoilTime = 120;

                    shootWarmupSpeed = 0.09f;
                    minWarmup = 0.99f;

                    shootSound = shootCyclone;

                    float reY = -14f / 4f, reRot = -10f;
                    unitParts.addAll(
                    new UnitRegionPart("weapon"){
                        {
                            x = 23 / 4f;
                            y = 32 / 4f;

                            moveX = 10 / 4f;
                            moveY = -8 / 4f;
                            moveRot = -65;

                            rotation = 70;
                            sinAngle = 10;
                            bodyScl = 12;
                            swingScl = 0.005f;
                            swingFront = false;

                            heatProgress = UnitPartProgress.recoil.add(0.3f);
                            heatColor = WHPal.SkyBlueF;

                            moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                        }
                    },

                    new UnitRegionPart("hand"){{
                        x = 23 / 4f;
                        y = 32 / 4f;

                        moveX = 10 / 4f;
                        moveY = 10 / 4f;
                        moveRot = -65;

                        rotation = 70;
                        sinAngle = 10;
                        bodyScl = 12;
                        swingScl = 0.005f;
                        swingFront = false;

                        layerOffset = -0.001f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("forearm"){{
                        x = -52 / 4f;
                        y = 12 / 4f;

                        moveX = 50 / 4f;
                        moveY = 30 / 4f;
                        moveRot = -20;

                        rotation = -45;
                        sinAngle = 10;
                        bodyScl = 12;
                        swingScl = 0.005f;
                        swingFront = false;

                        layerOffset = -0.002f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    //rightHand
                    new UnitRegionPart("hand"){{
                        x = -35 / 4f;
                        y = 28 / 4f;

                        moveX = 68 / 4f;
                        moveY = -10 / 4f;
                        moveRot = 40;

                        rotation = 30;
                        sinAngle = 10;
                        bodyScl = 12;
                        symmetry = true;
                        swingFront = false;

                        layerOffset = -0.001f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("body"){{
                        x = y = 0;
                        moveRot = -30;
                        sinAngle = 4;
                        bodyScl = 5;
                        swingFront = false;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, 0, reRot));
                    }},

                    //left
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        moveX = 16 / 4f;
                        moveY = 10 / 4f;
                        moveRot = -30;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.008f;
                        symmetry = true;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, -8f / 4f, 0));
                    }},
                    //right
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        moveX = 5 / 4f;
                        moveY = -9 / 4f;
                        moveRot = -15;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.008f;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("head"){{
                        x = y = 0;
                        layerOffset = 1;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.008f;
                        outline = false;
                        swingFront = false;
                    }}
                    );

                    shoot.firstShotDelay = 30;

                    bullet = new TrailFadeBulletType(10, 1000, name("energy-bullet")){
                        {
                            lifetime = 330 / speed;

                            hitBlinkTrail = false;

                            buildingDamageMultiplier = 1.5f;

                            width = 15;
                            height = width * 2.7f;

                            frontColor = WHPal.SkyBlueF;
                            lightColor = backColor = trailColor = hitColor = lightningColor = WHPal.SkyBlue;
                            lightning = 3;
                            lightningDamage = 30;
                            lightningLength = 10;
                            lightningLengthRand = 10;

                            chargeEffect = new MultiEffect(
                            WHFx.trailCharge(frontColor, 10, 2, 40, 3, shoot.firstShotDelay + 1).followParent(true),
                            WHFx.genericChargeCircle(frontColor, 6, splashDamageRadius * 0.5f, shoot.firstShotDelay + 1).followParent(true)
                            );

                            pierceArmor = true;
                            pierceCap = 3;

                            status = WHStatusEffects.plasma;
                            statusDuration = 30;

                            trailLength = 10;
                            trailWidth = width / 4f;
                            trailInterval = 1f;
                            trailEffect = new MultiEffect(
                            WHFx.hitPoly(hitColor, hitColor, 30, 1, 15, 3, 6, 60)
                            );

                            splashDamage = damage / 2;
                            splashDamageRadius = 40;

                            hitEffect = new MultiEffect(
                            WHFx.generalExplosion(10, hitColor, 60, 10, false),
                            WHFx.hitSpark(hitColor, 45, 10, 60, 1.5f, 10),
                            WHFx.hitPoly(hitColor, hitColor, 30, 10, 30, 7, 6, 60)
                            );
                            despawnEffect = new MultiEffect(
                            WHFx.generalExplosion(60, hitColor, splashDamageRadius, 10, true),
                            WHFx.instRotation(hitColor, 60, splashDamageRadius * 1.2f, 45, false),
                            WHFx.circleOut(hitColor, 60, splashDamageRadius),
                            WHFx.lineCircleOut(hitColor, 60, splashDamageRadius / 2, 4),
                            WHFx.trailCircleHitSpark(hitColor, 60, 8, splashDamageRadius * 2, 1.6f, 15),
                            new Effect(60, e -> {
                                color(hitColor);
                                rand.setSeed(e.id);
                                randLenVectors(e.id, (int)(rand.random(0.7f, 1) * 100), splashDamageRadius * 0.7f * Mathf.curve(e.fin(), 0, 0.08f),
                                splashDamageRadius * 0.3f * rand.random(0.8f, 1.2f) * e.finpow(), (x, y) -> {
                                    Fill.circle(e.x + x, e.y + y, Mathf.curve(e.fin(), 0, 0.08f) * e.fout(Interp.pow10Out) * 4 * rand.random(0.7f, 1.6f));
                                });
                            }));
                        }
                    };
                }}
                );
            }
        }

        ;

        M4D = new

        PowerArmourUnitType("m4D"){
            {
                speed = 1.8f;
                hitSize = 22f;
                rotateSpeed = 1.6f;
                health = 15000;
                armor = 28;
                mechFrontSway = 0.08f;
                ammoType = new ItemAmmoType(WHItems.ceramite);

                mechStepParticles = true;
                stepShake = 0.15f;
                singleTarget = true;

                abilities.add(
                new EllipseForceFieldAbility(75 / 4f, 50 / 4f, 4, 1000, 40 * 60f, 0.8f, 15){{
                    percentRegen = true;
                    percentRegenAmount = 0.06f;
                }},
                new ShieldRegenFieldAbility(100, 1000, 60f * 1, 60f));

                outlineRadius = 3;
                mechLegColor = outlineColor = WHPal.Outline;

                weapons.add(
                new MainWeapon(""){{
                    y = 1f;
                    x = 10f;
                    reload = 45;

                    recoil = 5f;
                    shake = 2f;

                    mirror = false;
                    melee = true;
                    recoilTime = 45;
                    shootStatusDuration = shoot.firstShotDelay = actionTime = 20;
                    actionInInterp = Interp.pow2In;
                    actionOutInterp = Interp.pow2Out;

                    shootWarmupSpeed = 0.12f;
                    minWarmup = 0.95f;

                    shootStatus = slow;

                    unitParts.addAll(

                    new UnitRegionPart("weapon2"){
                        {
                            x = 55 / 4f;
                            y = -10 / 4f;

                            actionRot = -120;
                            actionX = 20 / 4f;
                            actionY = -20 / 4f;
                            actionGrowY = -0.1f;
                            actionGrowX = -0.3f;

                            moveX = 10 / 4f;
                            moveY = 26 / 4f;
                            moveRot = 40;

                            rotation = 10;
                            sinAngle = 10;
                            bodyScl = 9;
                            swingFront = true;

                            heatColor = WHPal.SkyBlueF;
                            heatProgress = UnitPartProgress.one.add(-0.5f).blend(UnitPartProgress.actionTime, 0.5f);
                        }
                    },

                    new DrawBladePart(){{
                        x = 55 / 4f;
                        y = -10 / 4f;

                        actionRot = -120;
                        actionX = 10 / 4f;
                        actionY = -20 / 4f;

                        moveX = 20 / 4f;
                        moveY = 26 / 4f;
                        moveRot = 40;

                        rotation = 10;

                        trailWidth = 4;
                        rad = 150 / 4f;
                        color = WHPal.SkyBlueF;
                    }},

                    new UnitRegionPart("hand"){{
                        x = 55 / 4f;
                        y = -10 / 4f;

                        actionRot = -120;
                        actionX = 10 / 4f;
                        actionY = -20 / 4f;
                        actionGrowY = -0.1f;
                        actionGrowX = -0.3f;

                        moveX = 20 / 4f;
                        moveY = 26 / 4f;
                        moveRot = 40;

                        rotation = 30;
                        sinAngle = 5;
                        bodyScl = 9;
                        swingFront = true;
                    }},
                    new UnitRegionPart("forearm"){{
                        x = 55 / 4f;
                        y = -40 / 4f;

                        actionRot = -70;
                        actionX = -30 / 4f;
                        actionY = 0;

                        moveX = 0;
                        moveY = 5 / 4f;
                        moveRot = -10;

                        rotation = 10;
                        sinAngle = 5;
                        bodyScl = 9;
                        swingFront = true;
                    }},
                    //rightHand
                    new UnitRegionPart("hand"){{
                        x = -55 / 4f;
                        y = -10 / 4f;

                        actionRot = -90;
                        actionX = 60 / 4f;
                        actionY = 60 / 4f;

                        moveX = -5 / 4f;
                        moveY = -5 / 4f;
                        moveRot = 20;

                        rotation = 30;
                        sinAngle = 5;
                        bodyScl = 9;
                        swingFront = true;
                        symmetry = true;
                    }},

                    new UnitRegionPart("body"){{
                        x = y = 0;
                        actionRot = -60;
                        moveRot = 25;
                        sinAngle = 6;
                        bodyScl = 5;
                        swingFront = false;
                    }},
                    //left
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        actionX = 8 / 4f;
                        actionY = 10 / 4f;
                        actionRot = -30;
                        moveRot = 10;
                        sinAngle = 8;
                        bodyScl = 5;
                        symmetry = true;
                        swingScl = 0.01f;
                    }},
                    //right
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        actionX = 5 / 4f;
                        actionY = -11 / 4f;
                        actionRot = -20;
                        moveRot = 5;
                        sinAngle = 8;
                        bodyScl = 5;
                        swingScl = 0.01f;
                    }},

                    new UnitRegionPart("head"){{
                        x = y = 0;
                        actionRot = 5;
                        layerOffset = 1;
                        sinAngle = 2;
                        bodyScl = 5;
                        outline = false;
                        swingFront = false;
                    }}
                    );

                    bullet = new CritBulletType(4, 1300){{
                        height = width = 0;
                        critChance = 0.2f;
                        splashDamage = damage / 2;
                        pierce = true;
                        splashDamageRadius = 24;
                        absorbable = hittable = collidesAir = false;
                        lifetime = 15;
                        lightningColor = hitColor = WHPal.SkyBlue;
                        smokeEffect = shootEffect = none;
                        hitEffect = new MultiEffect(
                        WHFx.generalExplosion(10, hitColor, splashDamageRadius, 5, false),
                        WHFx.shuttle(WHPal.SkyBlue, hitColor, 30, true, 30, 30));
                        //standard bullet damage is far too much for lightning
                    }};
                }}
                );
            }
        }

        ;

        M3 = new

        PowerArmourUnitType("m3"){
            {
                speed = 1;
                hitSize = 18f;
                rotateSpeed = 3f;
                targetAir = true;
                health = 3000;
                armor = 13f;
                mechFrontSway = 0.3f;
                ammoType = new PowerAmmoType(5000);

                outlineRadius = 3;
                mechLegColor = outlineColor = WHPal.Outline;

                weapons.add(
                new MainWeapon(""){{
                    y = 0f;
                    x = 23 / 4f;
                    reload = 90;

                    layerOffset = 0.01f;

                    shootY = 70 / 4f;

                    recoil = 5f;
                    shake = 1f;

                    mirror = false;
                    cooldownTime = recoilTime = 120;

                    shootWarmupSpeed = 0.09f;
                    minWarmup = 0.99f;

                    chargeSound = chargeLancer;
                    shootSound = shootLaser;

                    float reY = -5f / 4f, reRot = -10f;
                    unitParts.addAll(
                    new UnitRegionPart("weapon1"){
                        {
                            x = 16 / 4f;
                            y = 21 / 4f;

                            moveX = 7 / 4f;
                            moveY = -1 / 4f;
                            moveRot = -70;

                            rotation = 75;
                            sinAngle = 8;
                            bodyScl = 12;
                            swingScl = 0.005f;
                            swingFront = false;

                            moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                        }
                    },

                    new UnitRegionPart("hand"){{
                        x = -34 / 4f;
                        y = 5 / 4f;

                        moveX = 14 / 4f;
                        moveY = 26 / 4f;
                        moveRot = -20;

                        rotation = -40;
                        sinAngle = 8;
                        bodyScl = 12;
                        swingScl = 0.005f;
                        swingFront = false;

                        layerOffset = -0.001f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},

                    new UnitRegionPart("body"){{
                        x = y = 0;
                        moveRot = -20;
                        sinAngle = 4;
                        bodyScl = 5;
                        swingFront = false;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, 0, reRot));
                    }},

                    //left
                    new UnitRegionPart("shoulder"){{
                        outlineLayerOffset = -0.1f;
                        x = y = 0;
                        moveX = 3 / 4f;
                        moveY = 5 / 4f;
                        moveRot = -10;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.001f;
                        symmetry = true;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    //right
                    new UnitRegionPart("shoulder"){{
                        outlineLayerOffset = -0.1f;
                        x = y = 0;
                        moveX = 3 / 4f;
                        moveY = -3 / 4f;
                        moveRot = -15;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.001f;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("head"){{
                        x = y = 0;
                        layerOffset = 1;
                        sinAngle = 3;
                        bodyScl = 10;
                        swingScl = 0.001f;
                        outline = false;
                        swingFront = false;
                    }}
                    );

                    shoot.shots = 2;
                    shoot.shotDelay = 12;
                    shoot.firstShotDelay = 20;
                    parentizeEffects = true;

                    bullet = new LightingLaserBulletType(110){{
                        recoil = 0.15f;
                        Color color = hitColor = lightningColor = WHPal.SkyBlueF.cpy();
                        chargeEffect = WHFx.genericCharge(hitColor, 5, 30, 20).followParent(true);
                        lifetime = 30;
                        length = 250;
                        width = 7;
                        pierceCap = 5;
                        sideAngle = 90;
                        sideLength = 8;
                        colors = new Color[]{color.a(0.4f), color.a(0.7f), color, Color.white};
                    }};
                }}
                );
            }
        }

        ;

        M2 = new

        PowerArmourUnitType("m2"){
            {
                speed = 0.9f;
                hitSize = 12;
                rotateSpeed = 2f;
                targetAir = true;
                health = 1500;
                armor = 10f;
                mechFrontSway = 0.3f;
                ammoType = new PowerAmmoType(3000);

                outlineRadius = 3;
                mechLegColor = outlineColor = WHPal.Outline;

                weapons.add(
                new MainWeapon(""){{
                    y = 0f;
                    x = 23 / 4f;
                    reload = 10;

                    layerOffset = 0.01f;

                    shootY = 50 / 4f;

                    recoil = 5f;
                    shake = 1f;

                    mirror = false;
                    cooldownTime = recoilTime = 120;

                    shootWarmupSpeed = 0.09f;
                    minWarmup = 0.99f;

                    shootSound = shootLaser;

                    float reY = -5f / 4f, reRot = -10f;
                    unitParts.addAll(
                    new UnitRegionPart("weapon1"){
                        {
                            x = 16 / 4f;
                            y = 21 / 4f;

                            moveX = 7 / 4f;
                            moveY = -15 / 4f;
                            moveRot = -70;

                            rotation = 75;
                            sinAngle = 8;
                            bodyScl = 12;
                            swingScl = 0.005f;
                            swingFront = false;

                            moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                        }
                    },

                    new UnitRegionPart("hand"){{
                        x = -34 / 4f;
                        y = 5 / 4f;

                        moveX = 14 / 4f;
                        moveY = 11 / 4f;
                        moveRot = -20;

                        rotation = -40;
                        sinAngle = 8;
                        bodyScl = 12;
                        swingScl = 0.005f;
                        swingFront = false;

                        layerOffset = -0.001f;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},

                    new UnitRegionPart("body"){{
                        x = y = 0;
                        moveRot = -20;
                        sinAngle = 4;
                        bodyScl = 5;
                        swingFront = false;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, 0, reRot));
                    }},

                    //left
                    new UnitRegionPart("shoulder"){{
                        outlineLayerOffset = -0.1f;
                        x = y = 0;
                        moveX = 3 / 4f;
                        moveY = 5 / 4f;
                        moveRot = -10;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.001f;
                        symmetry = true;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    //right
                    new UnitRegionPart("shoulder"){{
                        outlineLayerOffset = -0.1f;
                        x = y = 0;
                        moveX = 3 / 4f;
                        moveY = -3 / 4f;
                        moveRot = -15;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.001f;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    new UnitRegionPart("head"){{
                        x = y = 0;
                        layerOffset = 1;
                        sinAngle = 3;
                        bodyScl = 10;
                        swingScl = 0.001f;
                        outline = false;
                        swingFront = false;
                    }}
                    );

              /*  shoot.shots=2;
                shoot.shotDelay=12;*/
                    shoot.firstShotDelay = 10;
                    parentizeEffects = true;

                    bullet = new RailBulletType(){{
                        length = 220;
                        damage = 75;
                        hitColor = Color.valueOf("feb380");
                        hitEffect = endEffect = Fx.hitBulletColor;
                        pierceDamageFactor = 0.25f;

                        smokeEffect = Fx.colorSpark;

                        endEffect = new Effect(14f, e -> {
                            color(e.color);
                            Drawf.tri(e.x, e.y, e.fout() * 1.5f, 5f, e.rotation);
                        });

                        shootEffect = new Effect(10, e -> {
                            color(e.color);
                            float w = 1.2f + 7 * e.fout();

                            Drawf.tri(e.x, e.y, w, 30f * e.fout(), e.rotation);
                            color(e.color);

                            for(int i : Mathf.signs){
                                Drawf.tri(e.x, e.y, w * 0.9f, 18f * e.fout(), e.rotation + i * 90f);
                            }

                            Drawf.tri(e.x, e.y, w, 4f * e.fout(), e.rotation + 180f);
                        });

                        lineEffect = new Effect(20f, e -> {
                            if(!(e.data instanceof Vec2 v)) return;

                            color(e.color);
                            stroke(e.fout() * 1.1f + 0.6f);

                            Fx.rand.setSeed(e.id);
                            for(int i = 0; i < 7; i++){
                                Fx.v.trns(e.rotation, Fx.rand.random(8f, v.dst(e.x, e.y) - 8f));
                                Lines.lineAngleCenter(e.x + Fx.v.x, e.y + Fx.v.y, e.rotation + e.finpow(), e.foutpowdown() * 20f * Fx.rand.random(0.5f, 1f) + 0.3f);
                            }

                            e.scaled(14f, b -> {
                                stroke(b.fout() * 1.5f);
                                color(e.color);
                                Lines.line(e.x, e.y, v.x, v.y);
                            });
                        });
                    }};

                }}
                );
            }
        }

        ;

        M1 = new

        PowerArmourUnitType("m1"){
            {
                speed = 0.7f;
                hitSize = 12;
                rotateSpeed = 2f;
                targetAir = true;
                health = 600;
                armor = 8;
                mechFrontSway = 0.3f;
                ammoType = new PowerAmmoType(1500);

                outlineRadius = 3;
                mechLegColor = outlineColor = WHPal.Outline;

                weapons.add(
                new MainWeapon(""){{
                    y = 0f;
                    x = 9 / 4f;
                    reload = 40;

                    layerOffset = 0.01f;

                    shootY = 25 / 4f;

                    recoil = 5f;
                    shake = 1f;

                    mirror = false;
                    cooldownTime = recoilTime = 20;

                    shootWarmupSpeed = 0.09f;
                    minWarmup = 0.99f;

                    shootSound = shootTank;

                    float reY = -3f / 4f, reRot = -5;
                    unitParts.addAll(
                    new UnitRegionPart("weapon1"){
                        {
                            x = 7 / 4f;
                            y = 12 / 4f;

                            moveX = 3 / 4f;
                            moveY = -8 / 4f;
                            moveRot = -70;

                            rotation = 75;
                            sinAngle = 8;
                            bodyScl = 12;
                            swingScl = 0.005f;
                            swingFront = false;

                            symmetry = true;

                            moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                        }
                    },

                    new UnitRegionPart("body"){{
                        x = y = 0;
                        sinAngle = 4;
                        bodyScl = 5;
                        swingFront = false;

                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, 0, reRot));
                    }},

                    //left
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        outlineLayerOffset = -0.1f;
                        moveX = 1 / 4f;
                        moveY = 3 / 4f;
                        moveRot = -40;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.001f;
                        symmetry = true;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }},
                    //right
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        outlineLayerOffset = -0.1f;
                        moveX = 3 / 4f;
                        moveY = -3 / 4f;
                        moveRot = -15;
                        sinAngle = 5;
                        bodyScl = 8;
                        swingScl = 0.001f;
                        moves.add(new UnitPartMove(UnitPartProgress.recoil.curve(Interp.smooth), 0, reY, 0));
                    }}
                    );

                    shoot.shots = 3;
                    shoot.shotDelay = 6;
                    shoot.firstShotDelay = 20;
                    parentizeEffects = true;

                    bullet = new CritBulletType(6, 20){{
                        sprite = "missile-large";
                        smokeEffect = Fx.shootBigSmoke;
                        shootEffect = Fx.shootBigColor;
                        width = 5f;
                        height = 7f;
                        lifetime = 175 / speed;
                        hitSize = 4f;
                        hitColor = backColor = trailColor = Color.valueOf("feb380");
                        frontColor = Color.white;
                        trailWidth = 1.7f;
                        trailLength = 5;
                        despawnEffect = hitEffect = Fx.hitBulletColor;
                    }};
                }}
                );
            }
        }

        ;

        MEn1 = new

        PowerArmourUnitType("mEn1"){
            {

                String n = name;
                speed = 1f;
                hitSize = 30;
                rotateSpeed = 1.65f;
                health = 30000;
                armor = 30;

                drownTimeMultiplier = 0.5f;

                outlineRadius = 3;
                outlineColor = WHPal.Outline;

                ammoType = new ItemAmmoType(WHItems.molybdenumAlloy);

                stepShake = 0.15f;
                singleTarget = true;

                abilities.add(new PcShieldArcAbility(){{
                    cooldown = 45 * 60f;
                    radius = 20;
                    width = 8f;
                    regen = 6;
                    chanceDeflect = 0.2f;
                    max = 5000;
                    angle = 100;
                    whenShooting = false;
                }});


                outlineRadius = 3;
                mechLegColor = outlineColor = WHPal.Outline;

                range = 300;

                MinRangeWeapon sWeapon = new MinRangeWeapon(name(n + "-weapon2")){{

                    reload = 3;
                    velocityRnd = 0.15f;
                    x = -68 / 4f;
                    y = 0f;
                    recoil = 2;
                    shootY = 64 / 4f;
                    inaccuracy = 3;
                    shootSound = shootSpectre;
                    bullet = new CritBulletType(15, 100){{
                        pierceCap = 2;
                        splashDamage = damage * 0.7f;
                        splashDamageRadius = 32;
                        width = 8;
                        height = width * 2;
                        lifetime = 300 / speed;
                        shrinkY = 0;
                        frontColor = WHPal.WHYellow2;
                        trailColor = hitColor = backColor = WHPal.ShootOrange;

                        trailLength = 4;
                        trailWidth = width / 4;

                        smokeEffect = Fx.shootBig;
                        shootEffect = WHFx.shootLine(15, 15);
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.generalExplosion(5, hitColor, splashDamageRadius, 5, false),
                        WHFx.square(hitColor, 25, 4, 25, 4),
                        WHFx.hitSpark(WHPal.ShootOrange, 20, 4, 25, 2, 8));
                    }};
                }};

                MinRangeWeapon sWeapon2 = new MinRangeWeapon(name(n + "-weapon3")){{

                    reload = 140;
                    x = 0;
                    y = 0f;
                    shoot = new ShootAlternate(){
                        {
                            firstShotDelay = 12;
                        }
                    };
                    recoil = 2;
                    shootX = -68 / 4f;
                    shootY = 80 / 4f;
                    shootSound = shootMeltdown;
                    loopSound = beamMeltdown;
                    ;
                    continuous = true;
                    alternate = true;
                    mirror = true;
                    bullet = new ContinuousLaserBulletType(){{
                        damage = 100;
                        width = 3;
                        length = 200;
                        damageInterval = 6;
                        lifetime = 90;
                        pierceCap = 3;
                        backLength = 10;
                        frontLength = 50;
                        Color c = hitColor = WHPal.ShootOrange.cpy();
                        colors = new Color[]{c.a(0.2f), c.a(0.5f), c.a(0.7f), c, Color.white};

                        hitEffect = new MultiEffect(
                        WHFx.hitCircle(hitColor, Color.lightGray, 15, 6, 40, 8)
                        );
                    }};
                }};

                weapons.add(
                new MainWeapon(""){{
                    reload = 70;

                    recoil = 5f;
                    shake = 2f;

                    shootY = shootX = 0;

                    controllable = false;
                    autoTarget = true;

                    mirror = false;
                    cooldownTime = recoilTime = 45;
                    shoot.firstShotDelay = actionTime = 20;
                    actionInInterp = Interp.pow2In;
                    actionOutInterp = Interp.pow2Out;

                    shootWarmupSpeed = 0.12f;
                    minWarmup = 0.95f;

                    melee = true;

                    unitParts.addAll(

                    new UnitRegionPart("hand"){{
                        x = 66 / 4f;
                        y = -10 / 4f;

                        actionRot = -10;
                        actionX = -5 / 4f;
                        actionY = -40 / 4f;

                        actionGrowY = actionGrowX = 0.1f;

                        rotation = 6f;

                        sinGrowX = -0.3f;

                        moveX = 2 / 4f;
                        moveY = 10 / 4f;

                        sinAngle = 3;
                        bodyScl = 9.5f;
                        swingFront = true;
                        moves.addAll(new UnitPartMove(UnitPartProgress.smoothHeat, 0, 100 / 4f, 24));
                    }},
                    //LeftHand
                    new UnitRegionPart("hand"){{
                        x = -66 / 4f;
                        y = -10 / 4f;

                        actionRot = -40;
                        actionX = 10 / 4f;
                        actionY = 50 / 4f;

                        actionGrowY = -0.1f;
                        actionGrowX = -0.2f;

                        rotation = -6f;

                        sinGrowX = -0.3f;

                        moveX = 2 / 4f;
                        moveY = 10 / 4f;

                        sinAngle = 3;
                        bodyScl = 9.5f;
                        swingFront = true;
                        symmetry = true;
                    }},

                    new UnitRegionPart("body"){{
                        x = y = 0;
                        actionRot = -60;
                        sinAngle = 2;
                        bodyScl = 5;
                        swingFront = false;
                    }},
                    //left
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        actionX = 6 / 4f;
                        actionY = 10 / 4f;
                        actionRot = -20;
                        sinAngle = 8;
                        bodyScl = 5;
                        symmetry = true;
                        swingScl = 0.01f;
                    }},
                    //right
                    new UnitRegionPart("shoulder"){{
                        x = y = 0;
                        actionX = -2 / 4f;
                        actionY = -15 / 4f;
                        actionRot = -15;
                        sinAngle = 8;
                        bodyScl = 5;
                        swingScl = 0.01f;
                    }},

                    new UnitRegionPart("head"){{
                        x = y = 0;
                        actionRot = 5;
                        layerOffset = 1;
                        sinAngle = 2;
                        bodyScl = 5;
                        outline = false;
                        swingFront = false;
                    }}
                    );
                    shootSound = explosionTitan;

                    bullet = new CritBulletType(4, 1300){{
                        height = width = 0;
                        critChance = 0.2f;
                        splashDamage = damage * 0.8f;
                        splashDamageRadius = 64;
                        absorbable = hittable = collidesAir = false;
                        lifetime = 15;
                        lightningColor = hitColor = WHPal.ShootOrange;
                        smokeEffect = shootEffect = none;
                        hitEffect = new MultiEffect(
                        WHFx.generalExplosion(60, hitColor, splashDamageRadius * 0.7f, 5, true),
                        WHFx.square(hitColor, 60, 10, splashDamageRadius * 0.7f, 6));
                    }};
                }}
                );
                weapons.addAll(
                sWeapon,
                sWeapon2
                );
            }
        };


        airS6 = new UnitType("air-s6"){
            {
                constructor = UnitTypes.eclipse.constructor;
                researchCostMultiplier = 0.1f;
                health = 68000;
                armor = 33;
                speed = 0.75f;
                drag = 0.05f;
                accel = 0.04f;
                flying = true;
                hitSize = 75;
                lowAltitude = true;
                rotateSpeed = 0.78f;
                engineOffset = 200 / 4f;
                engineSize = 16;
                outlineColor = WHPal.OutlineS;
                BulletType missileBullet = new CritMissileBulletType(){{
                    critChance = 0.1f;
                    critMultiplier = 3f;
                    drawTeamColor = true;

                    sprite = "missile";
                    flameLength = 18;
                    lifetime = 60;
                    speed = 6;
                    damage = 60;
                    splashDamage = damage / 2;
                    splashDamageRadius = 45f;
                    shootEffect = Fx.shootBig;
                    smokeEffect = Fx.shootBigSmoke;

                    lightningColor = trailColor = backColor = Pal.bulletYellowBack;
                    width = 18;
                    height = 28;
                    lengthOffset = height / 4;
                    trailLength = 12;
                    trailWidth = 2.5f;
                    lightningLength = 10;
                    lightning = 2;
                    lightningLengthRand = 3;
                    lightningDamage = 16;
                    homingPower = 0.08f;
                    homingDelay = 20;
                    homingRange = 64;
                    followAimSpeed = 2;
                    status = StatusEffects.blasted;
                    hitEffect = Fx.flakExplosionBig;
                }};
                Weapon missileWeapon = new Weapon(name("air-s6-missile-weapon1")){{
                    reload = 30;
                    inaccuracy = 6;
                    shoot.shotDelay = 3;
                    shoot.shots = 3;
                    shootY = 29 / 4f;
                    rotate = true;
                    rotateSpeed = 1.3f;
                    recoil = 2;
                    shake = 1;
                    shootSound = shootSpectre;
                    bullet = missileBullet;
                }};
                weapons.addAll(
                new Weapon(name("air-s6-missile-weapon1")){{
                    reload = 40;
                    x = 72 / 4f;
                    y = 62 / 4f;
                    shoot = new ShootSpread(3, 3){{
                        shotDelay = 7f;
                    }};
                    shootY = 29 / 4f;
                    rotate = true;
                    rotateSpeed = 1.3f;
                    recoil = 2;
                    shootSound = shootFuse;
                    bullet = new ShrapnelBulletType(){{
                        length = 270;
                        damage = 80;
                        pierceCap = 4;
                        absorbable = true;
                        toColor = Pal.bulletYellowBack;
                        shootEffect = smokeEffect = WHFx.hitSpark(Pal.bulletYellowBack, 30, 5, 25, 1.5f, 8);
                        serrations = 3;
                    }};
                }},

                copyAndMove(missileWeapon, 104 / 4f, -120 / 4f),

                new Weapon(name(name + "missile")){{
                    reload = 180;
                    x = 112 / 4f;
                    y = 70 / 4f;
                    shoot = new ShootAlternate(){{
                        shots = 4;
                        shotDelay = 15;
                    }};
                    shootY = shootX = 0;
                    rotate = false;
                    shake = 1;
                    shootSound = shootSmite;
                    bullet = new CritMissileBulletType(){{
                        hitColor = lightningColor = trailColor = backColor = Pal.slagOrange.cpy().lerp(Pal.bulletYellowBack, 0.5f);

                        critChance = 0.1f;
                        critMultiplier = 4f;
                        sprite = name("large-missile");
                        flameLength = 20;
                        speed = 2;
                        lifetime = 79.21f;
                        drag = -0.02f;
                        width = 40;
                        height = 80;
                        drawTeamColor = true;
                        damage = 120;
                        splashDamage = damage;
                        splashDamageRadius = 70;
                        shootEffect = new MultiEffect(
                        Fx.shootBig,
                        WHFx.hitCircle(Pal.lighterOrange, Color.lightGray, 30, 10, 30, 5),
                        WHFx.shootLine(8, 20)
                        );
                        smokeEffect = Fx.shootBigSmoke;

                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.instRotation(hitColor, 60, splashDamageRadius * 0.7f, 90, false),
                        /*WHFx.generalExplosion(60, Pal.lighterOrange, splashDamageRadius + 10, 15, true),*/
                        WHFx.trailHitSpark(hitColor, 100, 10, splashDamageRadius + 10, 1.5f, 12),
                        WHFx.circleOut(hitColor, splashDamageRadius * 0.7f)
                        );

                        trailLength = 10;
                        trailWidth = 2.5f;
                        weaveScale = 12f;
                        weaveMag = 0.2f;
                        homingPower = 0.08f;
                        homingDelay = 15;
                        homingRange = 100;
                        followAimSpeed = 2;
                    }};
                }},

                new Weapon(name("air-s6-weapon2")){{
                    x = 94 / 4f;
                    y = -32 / 4f;

                    layerOffset = 0.00001f;
                    rotate = true;
                    rotateSpeed = 0.7f;
                    rotationLimit = 120f;
                    shootY = 60 / 4f;
                    reload = 30;
                    alternate = false;
                    recoil = 2f;
                    shake = 2f;
                    ejectEffect = Fx.casing3;
                    shootSound = explosionMissile;
                    inaccuracy = 4f;

                    shoot = new ShootAlternate(){{
                        shots = 4;
                        shotDelay = 8;
                        spread = 14 / 4f;
                    }};

                    bullet = new CritBulletType(8f, 80){{
                        critChance = 0.1f;
                        critMultiplier = 2f;
                        hitColor = trailColor = backColor = Pal.bulletYellowBack;
                        width = 14f;
                        height = 28f;
                        lifetime = 320 / speed;
                        splashDamage = damage / 2;
                        splashDamageRadius = 50;
                        shootEffect = Fx.shootBig;
                        lightning = 2;
                        lightningLength = 6;
                        lightningColor = Pal.surge;
                        lightningDamage = 30;
                        pierceCap = 2;
                        bouncing = true;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.generalExplosion(20, hitColor, splashDamageRadius / 2, 5, false),
                        WHFx.instHit(hitColor, false, 4, 20)
                        );
                    }};
                }});
            }
        }

        ;

        airSGreen6 = new

        UnitType("air-g-s6"){
            {
                constructor = UnitTypes.oct.constructor;
                researchCostMultiplier = 0.1f;
                health = 60000;
                armor = 20;
                speed = 0.45f;
                drag = 0.05f;
                accel = 0.04f;
                flying = true;
                hitSize = 80;
                lowAltitude = true;
                rotateSpeed = 0.78f;
                engineOffset = 180 / 4f;
                engineSize = 16;
                outlineColor = WHPal.OutlineS;

                range = 270;

                ammoType = new PowerAmmoType(15000);

                setEnginesMirror(
                new UnitEngine(-64 / 4f, -160 / 4f, 14, 225)
                );
                float r = 180;
                abilities.addAll(
                new ShockAbility(){{
                    range = r;
                    reload = 300;
                    bulletDamage = 1000;
                    falloffCount = 10;
                    waveColor = Pal.heal;
                    waveEffect = WHFx.circleOut(waveColor, 120, range);
                }},
                new RepairBomb(800, 240, r){{
                    y = 122 / 2f / 4f;
                    effectRadius = 10;
                    healPercent = 6;
                    triLength = 40;
                    triWidth = 14f;
                    deathBullet = new BasicBulletType(){{
                        hitColor = Pal.heal;
                        instantDisappear = true;
                        damage = splashDamage = 600;
                        splashDamageRadius = 130;
                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.instBombSize(hitColor, 4, splashDamageRadius + 20),
                        WHFx.hitCircle(hitColor, Color.lightGray, 180, 15, splashDamageRadius, 16),
                        WHFx.generalExplosion(180, hitColor, splashDamageRadius, 10, true),
                        WHFx.circleOut(hitColor, 90, splashDamageRadius)
                        );
                    }};
                }}
                );
                abilities.add(new ContinueEnergyFieldAbility(85, 80, 240, r + 56, 12, 9){{
                    y = 122 / 2f / 4f;
                    effectRadius = 10;
                    sectors = 6;
                    status = StatusEffects.none;
                    maxTargets = 3;
                    unitPierceCap = 4;
                    buildPierceCap = 3;
                    healPercent = 0.7f;
                    sameTypeHealMult = 0.6f;
                }});
                Weapon mechaSGreen6D = new LaserPointDefenseWeapon(name("heal-weapon-mount")){{
                    x = 126 / 4f;
                    y = -18 / 4f;
                    mirror = true;
                    rotate = true;
                    rotateSpeed = 5;
                    shootY = 23 / 4f;
                    damage = 380 / 60f;
                    laserWidth = 1.5f;
                    color = Pal.heal;
                    useTeamColor = false;

                    bullet = new BulletType(){{
                        damage = 4;
                        maxRange = 220;
                        collidesGround = false;
                    }};
                }};

                weapons.addAll(
                new HealConeWeapon(name("heal-projection-weapon")){{
                    x = 112 / 4f;
                    y = 65 / 4f;
                    shootY = 8;
                    mirror = true;
                    bullet = new HealCone(){{
                        lifetime = 240;
                        healPercent = 2;
                    }};
                    reload = 300;
                    rotate = true;
                    rotateSpeed = 2;
                    alternate = false;
                    continuous = true;
                    recoil = 0;
                }},
                copyAndMove(mechaSGreen6D, 144 / 4f, 0f)
                );

                weapons.add(new HealWeapon(name("heal-missile-large")){{
                                reload = 60;
                                x = 137 / 4f;
                                y = -73 / 4f;

                                shootY = 32 / 4f;
                                shoot = new ShootAlternate(){{
                                    shots = 3;
                                    barrels = 3;
                                    spread = 18 / 4f;
                                    shotDelay = 10;
                                }};

                                shadow = 5f;

                                rotateSpeed = 1f;
                                rotate = true;
                                inaccuracy = 1f;
                                velocityRnd = 0.1f;
                                shootSound = shootMissile;

                                ejectEffect = Fx.none;
                                bullet = new FlakBulletType(5, 90){{
                                    sprite = "missile-large";
                                    //for targeting
                                    collidesGround = collidesAir = true;
                                    explodeRange = 40f;
                                    width = height = 12f;
                                    shrinkY = 0f;
                                    drag = 0.003f;
                                    homingRange = 60f;
                                    keepVelocity = false;
                                    lightRadius = 60f;
                                    lightOpacity = 0.7f;
                                    lightColor = Pal.heal;

                                    healAmount = 200;
                                    healPercent = 0.5f;

                                    splashDamageRadius = 64;
                                    splashDamage = 50;

                                    lifetime = 68.49f;
                                    backColor = Pal.heal;
                                    frontColor = Color.white;

                                    hitEffect = despawnEffect =
                                    new MultiEffect(
                                    WHFx.generalExplosion(60, Pal.heal, splashDamageRadius * 0.7f, 10, false),
                                    WHFx.hitSpark(Pal.heal, 60, 20, splashDamageRadius, 1.5f, 8)
                                    );
                                    weaveScale = 15f;
                                    weaveMag = 0.5f;

                                    trailColor = Pal.heal;
                                    trailWidth = 4.5f;
                                    trailLength = 29;

                                    fragBullets = 7;
                                    fragVelocityMin = 0.3f;

                                    fragBullet = new MissileBulletType(3.9f, 30){{
                                        homingPower = 0.2f;
                                        weaveMag = 4;
                                        weaveScale = 4;
                                        lifetime = 60f;
                                        keepVelocity = false;
                                        shootEffect = Fx.shootHeal;
                                        smokeEffect = Fx.hitLaser;
                                        splashDamage = damage;
                                        splashDamageRadius = 24;
                                        frontColor = Color.white;
                                        hitSound = Sounds.none;

                                        lightColor = Pal.heal;
                                        lightRadius = 40f;
                                        lightOpacity = 0.7f;

                                        trailColor = Pal.heal;
                                        trailWidth = 2.5f;
                                        trailLength = 20;
                                        trailChance = -1f;

                                        healPercent = 0.3f;
                                        healAmount = 50;

                                        collidesTeam = true;
                                        backColor = Pal.heal;

                                        homingDelay = 15;
                                        homingRange = 60f;
                                        followAimSpeed = 1.2f;
                                        hitEffect = despawnEffect = WHFx.generalExplosion(45, Pal.heal, 25, 3, false);
                                    }};
                                }};
                            }}
                );

            }
        };

        mechaS6 = new UnitType("mech-s6"){
            {
                constructor = MechUnit::create;
                researchCostMultiplier = 0.1f;
                hitSize = 48;
                canDrown = false;
                armor = 26;
                speed = 0.7f;
                rotateSpeed = 1.44f;
                baseRotateSpeed = 1.3f;
                health = 65000;
                mechSideSway = 0.5f;
                mechFrontSway = 2f;
                outlineColor = WHPal.OutlineS;
                range = 360 - 8;
                immunities.addAll(StatusEffects.disarmed, StatusEffects.unmoving);
                weapons.add(
                new MarkWeapon(name("mech-s6-weapon1")){{
                    reload = 30;
                    shake = 3;
                    recoil = 8;
                    recoilTime = 25;
                    x = 133 / 4f;
                    y = -1 / 4f;
                    shootY = 104 / 4f;
                    shootX = -11 / 4f;
                    xRand = 1;
                    rotate = false;
                    rotateSpeed = 0.4f;
                    rotationLimit = 25;
                    layerOffset = -0.00001f;
                    inaccuracy = 0.5f;
                    velocityRnd = 0.1f;
                    shootSound = shootSpectre;
                    ejectEffect = Fx.casing4;
                    shootCone = 15;
                    markChance = 0.2f;
                    markTime = 90;
                    markReduce = 20;
                    shoot.shots = 2;
                    shoot.shotDelay = 10;
                    markShoot = new ShootSpread(4, 5){{
                        shotDelay = 7f;
                    }};
                    markBullet = bullet = new CritBulletType(7, 110){{
                        critMultiplier = 2;
                        critChance = 0.15f;
                        layer = Layer.effect - 0.0001f;
                        parts.addAll(
                        new FlarePart(){{
                            progress = PartProgress.life;
                            radius = 0f;
                            radiusTo = 40f;
                            stroke = 5f;
                            rotation = 45f;
                            spinSpeed = 1;
                            color1 = Pal.bulletYellowBack;
                            followRotation = true;
                        }}
                        );
                        splashDamage = damage;
                        splashDamageRadius = 40;
                        pierce = true;
                        pierceCap = 5;
                        lifetime = 360 / speed;
                        hitSound = Sounds.explosion;
                        shootEffect = Fx.shootBig2;
                        smokeEffect = new MultiEffect(Fx.shootBigSmoke2, explosionSmokeEffect(Pal.bulletYellowBack, 60, 30, 5, 5).layer(Layer.effect));
                        hitColor = trailColor = backColor = Pal.bulletYellowBack;
                        width = 20;
                        height = 45;
                        trailLength = 16;
                        trailWidth = 14 / 4f;
                        hitEffect = new MultiEffect(
                        WHFx.hitSpark(hitColor, 60, 15, splashDamageRadius * 1.5f, 1.4f, 8));
                        despawnEffect = new MultiEffect(
                        WHFx.generalExplosion(60, hitColor, splashDamageRadius, 12, true),
                        WHFx.instRotation(hitColor, 60, splashDamageRadius + 10, 45, true));
                        bulletInterval = 12;
                        intervalBullets = 1;
                        intervalAngle = 0;
                        fragAngle = fragRandomSpread = 0;
                        fragBullets = 2;
                        fragVelocityMin = 0.7f;
                        fragVelocityMax = 1.5f;
                        fragBullet = intervalBullet = new BasicBulletType(3, 90){
                            {
                                hittable = false;
                                drag = 0.01f;
                                lifetime = 45;
                                instantDisappear = true;
                                splashDamage = damage;
                                splashDamageRadius = 56;
                                hitColor = trailColor = backColor = Pal.bulletYellowBack;
                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.lineCircleOut(hitColor, 25, splashDamageRadius / 2, 2),
                                WHFx.hitCircle(hitColor, hitColor, 30, 5, splashDamageRadius / 2.5f, 8)
                                );
                            }
                        };
                    }};
                }},
                new Weapon(name("s-cannon")){{
                    top = true;
                    layerOffset = 0.0001f;
                    x = 64 / 4f;
                    y = 32 / 4f;
                    shootY = 18 / 4f;
                    reload = 30f;
                    recoil = 2f;
                    shake = 1f;
                    rotate = true;
                    rotateSpeed = 1f;
                    shootSound = explosionMissile;
                    shoot.shots = 3;
                    shoot.shotDelay = 5;

                    bullet = new BasicBulletType(8f, 60){{
                        pierce = true;
                        pierceCap = 3;
                        width = 12;
                        height = width * 2;
                        lifetime = 220 / speed;
                        shootEffect = Fx.shootBig;
                        fragVelocityMin = 0.4f;
                        hitColor = trailColor = backColor = Pal.bulletYellowBack;

                        splashDamage = 45;
                        splashDamageRadius = 24f;
                        hitEffect = new Effect(30, (e) -> {
                            Draw.color(hitColor.cpy(), Color.gray.cpy(), e.fout() * 0.3f);
                            stroke(e.fout() * 1.4f);
                            randLenVectors(e.id, (int)(6 * Mathf.randomSeed(e.id, 1, 1.5f)), e.finpow() * 15, e.rotation, 45, (x, y) -> {
                                float ang = Mathf.angle(x, y);
                                lineAngle(e.x + x, e.y + y, ang, e.fout() * 6 * Mathf.randomSeed(e.id, 0.5f, 1.5f));
                            });
                        });
                        despawnEffect = flakExplosion;
                    }};
                }});
                weapons.add(new Weapon(name("projection-weapon")){
                    {
                        x = 92 / 4f;
                        y = -32 / 4f;
                        reload = 60f;
                        rotate = true;
                        rotateSpeed = 1f;
                        autoTarget = continuous = true;
                        controllable = false;
                        shootY = 4;
                        alternate = false;
                        shootCone = 15f;
                        shootSound = loopPulse;
                        ;
                        bullet = new MoveSuppressionBullet(20, 200, 0.9f){{
                            lifetime = 240f;
                            maxRange = findRange;
                            color = Pal.slagOrange.cpy().lerp(Color.white, 0.2f);
                        }};
                    }

                    @Override
                    protected Teamc findTarget(Unit unit, float x, float y, float range, boolean air, boolean ground){
                        return Units.closestEnemy(unit.team, x, y, range + Math.abs(shootY), u -> u.checkTarget(true, true));
                    }
                });
            }
        };


        mechaSGreen6 = new UnitType("mech-g-s6"){
            {
                constructor = LegsUnit::create;
                speed = 0.45f;
                rotateSpeed = 0.8f;
                hitSize = 50f;
                health = 58000;
                range = 500;
                fogRadius = range * 0.5f;
                faceTarget = true;
                armor = 20;

                legCount = 4;
                legSpeed = 0.6f;
                lockLegBase = legContinuousMove = true;
                rippleScale = 6.8f;
                legGroupSize = 2;
                legPairOffset = 1;
                legLength = 22f;
                legBaseOffset = 8f;
                legMoveSpace = 1f;
                legForwardScl = 0.58f;
                allowLegStep = hovering = true;
                shadowElevation = 0.3f;
                ammoType = new PowerAmmoType(20000);
                groundLayer = Layer.legUnit + 0.01f;

                outlineColor = WHPal.OutlineS;
                abilities.add(new PcShieldArcAbility(){{
                    pushUnits = true;
                    radius = 90;
                    angle = 120;
                    width = 12;
                    cooldown = 60 * 25;
                    max = 3000;
                    regen = 300 / 60f;
                    chanceDeflect = 0.2f;
                    y = 3;
                }});

                weapons.add(new Weapon("mech-g-s6-laser"){{
                    reload = 360;
                    mirror = false;
                    x = 0;
                    y = 0;
                    shootY = 77 / 4f;
                    parentizeEffects = true;

                    shootSound = shootCorvus;
                    chargeSound = chargeCorvus;
                    soundPitchMin = 0.75f;
                    soundPitchMax = 0.9f;
                    recoilTime = 285;
                    cooldownTime = 105;
                    continuous = true;
                    recoil = 0;
                    shake = 1f;
                    parts.addAll(
                    new ShapePart(){{
                        progress = PartProgress.recoil;
                        color = Color.white;
                        circle = true;
                        radius = 5f;
                        radiusTo = 0f;
                        layer = 114;
                        y = shootY;
                    }},
                    new ShapePart(){{
                        progress = PartProgress.recoil;
                        color = Pal.heal;
                        circle = true;
                        radius = 8;
                        radiusTo = 0;
                        layer = Layer.effect;
                        y = shootY;
                    }});

                    shootStatus = StatusEffects.unmoving;
                    shootStatusDuration = 400;
                    shoot.firstShotDelay = 100;

                    bullet = new LaserBeamBulletType(){
                        {
                            damageInterval = 6;
                            damage = 1400 / (60f / damageInterval);
                            rotate = true;
                            triCap = tri = true;
                            width = 27f;
                            sideLength = 3;
                            sideWidth = 20;
                            sideAngle = 0;
                            rotateAngle = 8;
                            moveSpeed = 0.07f;
                            moveInterp = Interp.pow10Out;
                            collidesTeam = true;
                            healPercent = 0.7f;

                            lifetime = 300;
                            maxRange = 500;
                            length = 10;
                            extensionProportion = maxRange / length;

                            hitColor = Pal.heal;
                            chargeEffect = new MultiEffect(
                            WHFx.genericChargeCircle(hitColor, 10, 120, 100).followParent(true),
                            WHFx.lineCircleIn(hitColor, 100, 120, 3).followParent(true),
                            WHFx.genericChargeCircle(hitColor, 10, 100, 80).followParent(true).startDelay(20),
                            WHFx.lineCircleIn(hitColor, 80, 100, 3).followParent(true).startDelay(20),
                            WHFx.genericChargeCircle(hitColor, 10, 100, 60).followParent(true).startDelay(40),
                            WHFx.lineCircleIn(hitColor, 60, 100, 3).followParent(true).startDelay(40)
                            );

                            colors = new Color[]{Pal.heal.cpy().a(0.4f), Pal.heal.cpy().a(0.7f), Pal.heal, Color.white};
                            pierceCap = 5;
                            hitEffect = WHFx.hitCircle(hitColor, hitColor, 60, 6, 25, 6);
                        }

                        @Override
                        public void update(Bullet b){
                            super.update(b);
                            if(b.timer(2, 8)){
                                WHFx.hitSpark(hitColor, 30, 8, 80, 1.6f, 6)
                                .at(b.x, b.y);
                            }
                        }
                    };
                }});

                Weapon mechaSGreen6D = new LaserPointDefenseWeapon(name("heal-weapon-mount")){{
                    x = 126 / 4f;
                    y = -18 / 4f;
                    mirror = true;
                    rotate = true;
                    rotateSpeed = 5;
                    shootY = 23 / 4f;
                    damage = 350 / 60f;
                    laserWidth = 1.5f;
                    color = Pal.heal;
                    useTeamColor = false;

                    bullet = new BulletType(){{
                        damage = 4;
                        maxRange = 220;
                        collidesGround = false;
                    }};
                }};


                Weapon mechaSGreen6B = new HealWeapon(name("heal-weapon-mount")){{
                    shootSound = Sounds.shootLaser;
                    reload = 60;
                    x = 55 / 4f;
                    y = 53 / 4f;
                    rotate = true;
                    rotateSpeed = 1.5f;
                    shootY = 23 / 4f;
                    shoot = new ShootAlternate(){{
                        shots = 4;
                        shotDelay = 8;
                    }};
                    bullet = new BasicBulletType(5f, 50){{
                        width = 8;
                        height = width * 2.2f;
                        trailLength = 8;
                        lifetime = 101.65f;
                        drag = 0.01f;

                        healPercent = 0.4f;
                        collidesTeam = true;
                        trailColor = hitColor = backColor = Pal.heal;
                        frontColor = Color.white;
                        shootEffect = new MultiEffect(
                        Fx.shootHeal,
                        WHFx.hitCircle(hitColor, hitColor, 30, 4, 12, 5),
                        WHFx.lineCircleOut(hitColor, 20, 18, 4)
                        );
                        despawnEffect = hitEffect = new MultiEffect(
                        WHFx.lineCircleOut(hitColor, 20, 18, 2),
                        WHFx.hitCircle(hitColor, hitColor, 20, 5, 18, 6)
                        );
                    }};
                }};

                weapons.addAll(
                new HealConeWeapon(name("heal-projection-weapon")){{
                    x = 80 / 4f;
                    y = -22 / 4f;
                    shootY = 8;
                    mirror = true;
                    bullet = new HealCone(){{
                        lifetime = 200;
                        healPercent = 1.8f;
                        healAmount = 900;
                    }};
                    reload = 300;
                    rotate = true;
                    rotateSpeed = 2;
                    alternate = false;
                    useAmmo = false;
                    continuous = true;
                    cooldownTime = 150;
                    recoil = 0;
                }},
                copy(mechaSGreen6D),
                copy(mechaSGreen6B)
                );
            }
        };

        meshSPurple6 = new UnitType("mech-p-s6"){
            {
                groundLayer = Layer.legUnit;
                constructor = LegsUnit::create;
                drag = 0.1f;
                speed = 0.42f;
                hitSize = 35.5f;
                health = 52000;
                outlineRadius = 5;
                rotateSpeed = 1.3f;
                legContinuousMove = true;
                legCount = 8;
                legMoveSpace = 0.76f;
                legPairOffset = 0.7f;
                legGroupSize = 2;
                legLength = 115f;
                legExtension = -8.25f;
                /*legBaseOffset = 0f;*/
                stepShake = 2.4f;
                legLengthScl = 1f;
                rippleScale = 2f;
                legSpeed = 0.2f;

                ammoType = new ItemAmmoType(Items.surgeAlloy, 4);
                ammoCapacity = 1000;

                legSplashDamage = 80f;
                legSplashRange = 40f;
                hovering = true;
                armor = 17f;
                allowLegStep = true;
                shadowElevation = 0.95f;

                outlineColor = WHPal.OutlineS;

                BulletType sapLaser = new LaserBulletType(){
                    {
                        damage = 115f;
                        sideWidth = 0f;
                        sideLength = 0f;
                        lightningSpacing = 17f;
                        lightningDelay = 0.12f;
                        lightningDamage = 10f;
                        lightningLength = 4;
                        lightningLengthRand = 2;
                        lightningAngleRand = 15f;
                        width = 42f;
                        length = 140f;
                        hitColor = lightningColor = Pal.sapBullet;
                        shootEffect = WHFx.hitSpark(hitColor, 60, 10, 30, 1.6f, 5);
                        colors = new Color[]{Pal.sapBulletBack.cpy().a(0.4f), Pal.sapBullet, Color.white};
                    }
                };

                weapons.add(new Weapon(name("sap-weapon-mount")){{
                    reload = 20;
                    mirror = true;
                    rotate = true;
                    rotateSpeed = 2;
                    x = 100 / 4f;
                    y = 8 / 4f;
                    shootCone = 15;
                    shootY = 28 / 4f;
                    shoot = new ShootSpread(){{
                        spread = 5f;
                        shots = 3;
                        shotDelay = 5;
                    }};
                    shootSound = explosion;
                    recoil = 3;
                    shake = 1f;
                    bullet = new SapBulletType(){{
                        sapStrength = 1f;
                        length = 250;
                        damage = 60;
                        pierceArmor = true;
                        shootEffect = Fx.shootSmall;
                        hitColor = color = Color.valueOf("bf92f9");
                        despawnEffect = Fx.none;
                        width = 0.8f;
                        lifetime = 30f;
                        knockback = -1f;
                    }};
                }});
                weapons.add(new ChargePointLaserrWeapon(name("sap-weapon-laser")){{
                    reload = 170f;
                    mirror = true;
                    alternate = false;
                    x = 63 / 4f;
                    y = -35 / 4f;
                    shootSound = shootSap;
                    shootY = 24 / 4f;
                    recoil = 2;
                    rotateSpeed = 2;
                    rotate = alwaysContinuous = continuous = true;
                    shake = 1f;
                    shoot.firstShotDelay = 30;
                    bullet = new ContinuousFlameBulletType(){
                        {
                            hitColor = Pal.sapBullet;
                            flareColor = hitColor;
                            damage = 700 / 10f;
                            damageInterval = 6;
                            length = 260;
                            hitEffect = WHFx.hitCircle(hitColor, hitColor, 40, 8, 20, 6);
                            drawSize = 420f;
                            width = 3.4f;
                            shake = 1f;
                            pierceCap = 3;
                            pierceArmor = false;
                            colors = new Color[]{
                            Pal.sapBulletBack.cpy().a(0.4f), Pal.sapBullet,
                            Color.valueOf("91a4ff"),
                            Pal.sapBulletBack.cpy().lerp(Color.white, 0.7f)};
                            despawnEffect = Fx.smokeCloud;
                            smokeEffect = Fx.none;
                            shootEffect = Fx.none;
                        }

                        @Override
                        public void hitEntity(Bullet b, Hitboxc entity, float health){
                            super.hitEntity(b, entity, health);
                            if(entity instanceof Healthc h && b.owner instanceof Healthc owner){
                                owner.heal(damage * 0.4f);
                            }
                        }

                        @Override
                        public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
                            super.hitTile(b, build, x, y, initialHealth, direct);
                            if(b.owner instanceof Healthc owner){
                                owner.heal(damage * 0.4f);
                            }
                        }
                    };
                }});

                weapons.add(new Weapon(name + "-cannon"){{
                    y = 0f;
                    x = 0f;
                    shootY = 64 / 4f;
                    mirror = false;
                    reload = 400;
                    shake = 10f;
                    shootSound = shootArtillery;
                    rotate = false;
                    float delay = shoot.firstShotDelay = 90;

                    bullet = new TrailFadeBulletType(5f, 300){{

                        sprite = name("cross-star");
                        spin = 2;
                        trailColor = backColor = lightningColor = Pal.sapBullet.cpy().lerp(Color.white, 0.3f);
                        frontColor = Color.white;
                        hitColor = lightColor = Pal.sap;
                        lightOpacity = 0.6f;

                        chargeEffect = new MultiEffect(
                        WHFx.genericChargeCircle(hitColor, 10, 120, delay).followParent(true),
                        WHFx.trailCharge(hitColor, 12, 2, 100, 2, delay).followParent(true),
                        WHFx.trailCharge(hitColor, 12, 1.5f, 70, 2, delay).followParent(true)
                        );

                        parentizeEffects = true;
                        hitEffect = new MultiEffect(
                        Fx.sapExplosion,
                        WHFx.trailHitSpark(hitColor, 90, 10, splashDamageRadius, 1.6f, 12),
                        WHFx.generalExplosion(60, hitColor, splashDamageRadius, 8, false));
                        knockback = 0.8f;
                        lifetime = 80f;
                        width = 55;
                        height = 55;
                        scaleLife = true;
                        ammoMultiplier = 4f;
                        scaledSplashDamage = true;
                        splashDamageRadius = 80f;
                        splashDamage = damage;
                        backColor = Pal.sapBulletBack;

                        lightning = 5;
                        lightningLength = 20;
                        lightningDamage = 50;
                        smokeEffect = Fx.shootBigSmoke2;
                        hitShake = 10f;
                        lightRadius = 40f;

                        status = StatusEffects.sapped;
                        statusDuration = 60f * 10;

                        despawnBlinkTrail = false;
                        tracers = 2;
                        tracerStroke = 2;
                        tracerSpacing = 8;
                        tracerRandX = 4;
                        tracerFadeOffset = 4;
                        tracerStrokeOffset = 9;
                        tracerUpdateSpacing = 0.8f;

                        trailLength = 18;
                        trailWidth = 3.5f;
                        trailSinScl = 4;
                        trailSinMag = 0.2f;

                        fragLifeMin = 1;
                        fragBullets = 6;

                        fragVelocityMin = fragVelocityMax = 1f;
                        fragRandomSpread = 0;
                        fragSpread = 360f / fragBullets;

                        fragBullet = new ArtilleryBulletType(1, 80){{
                            hitEffect = Fx.sapExplosion;
                            knockback = 0.8f;
                            lifetime = 25;
                            width = height = 20f;
                            collidesGround = collidesAir/*=collidesTiles =collides*/ = true;
                            splashDamageRadius = 48;
                            splashDamage = 80;
                            backColor = Pal.sapBulletBack;
                            frontColor = lightningColor = Pal.sapBullet;
                            lightning = 2;
                            lightningDamage = 20f;
                            lightningLength = 10;
                            smokeEffect = Fx.shootBigSmoke2;
                            hitShake = 5f;
                            lightRadius = 30f;
                            lightColor = Pal.sap;
                            lightOpacity = 0.5f;

                            status = StatusEffects.sapped;
                            statusDuration = 60f * 10;

                            fragLifeMin = 1;
                            fragBullets = 1;
                            fragVelocityMin = fragVelocityMax = 1f;

                            fragOffsetMax = fragOffsetMin = 0;
                            fragRandomSpread = 0;

                            fragBullet = new ArtilleryBulletType(1, 60){
                                {
                                    hitEffect = Fx.sapExplosion;
                                    knockback = 0.8f;
                                    lifetime = 25;
                                    width = height = 20f;
                                    collidesGround = collidesAir/*=collidesTiles =collides*/ = true;
                                    splashDamageRadius = 70f;
                                    splashDamage = 40;
                                    backColor = Pal.sapBulletBack;
                                    frontColor = lightningColor = Pal.sapBullet;
                                    lightning = 2;
                                    lightningLength = 5;
                                    smokeEffect = Fx.shootBigSmoke2;
                                    hitShake = 5f;
                                    lightRadius = 30f;
                                    lightColor = Pal.sap;
                                    lightOpacity = 0.5f;

                                    status = StatusEffects.sapped;
                                    statusDuration = 60f * 10;
                                }
                            };
                        }};
                    }};
                }});

            }
        };

        navyS6 = new UnitType("naval-s6"){

            {
                constructor = UnitTypes.omura.constructor;
                researchCostMultiplier = 0.1f;
                faceTarget = false;
                speed = 0.52f;
                rotateSpeed = 0.75f;
                hitSize = 80;
                trailLength = 160;
                waveTrailX = 25;
                waveTrailY = -46;
                trailScl = 5.4f;
                health = 68000;
                armor = 26;
                range = 400;

                immunities.addAll(StatusEffects.burning, StatusEffects.melting);

                outlineColor = WHPal.OutlineS;

                Weapon navyS6MissileLauncher = new Weapon(name("air-s6-missile-mount")){{
                    reload = 180;
                    shootY = 32 / 4f;
                    mirror = false;

                    shoot = new ShootMulti(
                    new ShootAlternate(){{
                        barrels = 3;
                        spread = 18 / 4f;
                        shots = 2;
                        shotDelay = 30;
                    }},
                    new ShootSpread(3, 30));

                    shootSound = shootMissile;
                    rotate = true;
                    rotateSpeed = 1;
                    inaccuracy = 2;
                    xRand = 0.4f;
                    bullet = new CritMissileBulletType(){{
                        hitColor = lightningColor = trailColor = backColor = Pal.slagOrange.cpy().lerp(Pal.bulletYellowBack.cpy(), 0.5f);

                        Color c = hitColor.cpy();
                        colors = new Color[]{c.a(0.55f), c.a(0.7f), c.a(0.8f), c, Color.white};

                        sprite = name("large-missile");
                        flameLength = 20;
                        flameWidth = 1.8f;
                        speed = 8;
                        lifetime = 420 / speed;
                        width = 30;
                        height = 60;
                        damage = 70;
                        splashDamage = 90;
                        splashDamageRadius = 56;
                        shootEffect = new MultiEffect(
                        Fx.shootBig,
                        WHFx.hitCircle(Pal.lighterOrange, Color.lightGray, 30, 10, 30, 5),
                        WHFx.shootLine(8, 20)
                        );
                        smokeEffect = Fx.shootBigSmoke;

                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.generalExplosion(60, Pal.lighterOrange, splashDamageRadius + 10, 15, false),
                        WHFx.trailHitSpark(hitColor, 100, 10, splashDamageRadius + 10, 1.5f, 12),
                        WHFx.circleOut(hitColor, splashDamageRadius * 0.7f)
                        );

                        trailLength = 10;
                        trailWidth = 2.5f;
                        weaveScale = 12f;
                        weaveMag = 0.2f;
                        homingPower = 0.08f;
                        homingDelay = 25;
                        homingRange = 80;
                        followAimSpeed = 1.4f;

                        fragBullets = 3;
                        fragVelocityMin = 0.5f;
                        fragVelocityMax = 1.5f;
                        fragBullet = new CritBulletType(2.8f, 60){{
                            critMultiplier = 1.6f;
                            width = 10;
                            height = 12;
                            shrinkY = 1;
                            lifetime = 15;
                            pierceBuilding = true;
                            pierce = true;
                            pierceCap = 2;
                            hitColor = lightningColor = trailColor = backColor = Pal.slagOrange.cpy().lerp(Pal.bulletYellowBack, 0.5f);
                            hitEffect = Fx.massiveExplosion;
                            despawnEffect = new Effect(40f, 160f, e -> {
                                Draw.color(e.color);
                                for(int s : Mathf.signs){
                                    Drawf.tri(e.x, e.y, e.fout() * 10f, e.foutpow() * 26 + 6f, e.rotation + s * 90f);
                                }
                            });
                        }};
                    }};
                }};
                Weapon navyS6Cannon = new MarkWeapon(name("naval-cannon")){{
                    reload = 150;
                    rotate = true;
                    rotateSpeed = 0.8f;
                    x = 0;
                    mirror = false;
                    inaccuracy = 3;
                    recoil = 3;
                    recoilTime = 90;
                    cooldownTime = 160;
                    shootSound = shootArtillery;
                    shootY = 70 / 4f;
                    shake = 4;
                    recoils = 2;
                    layerOffset = 0.1f;
                    for(int i = 0; i < 2; i++){
                        int f = i;
                        int h = i + 1;
                        parts.addAll(
                        new RegionPart("-barrel-" + h){{
                            progress = PartProgress.recoil;
                            heatColor = Pal.turretHeat;
                            recoilIndex = f;
                            under = true;
                            moveY = -4f;
                        }}
                        );
                    }
                    markTime = 90;
                    markReduce = 20;
                    markChance = 0.22f;
                    shoot = new ShootAlternate(){{
                        shots = 2;
                        shotDelay = 20;
                        spread = 17 / 4f;
                    }};
                    markShoot = new ShootMulti(
                    new ShootAlternate(){{
                        shots = 2;
                        barrels = 2;
                        spread = 17 / 4f;
                    }},
                    new ShootPattern(){{
                        shots = 2;
                        shotDelay = 30;
                    }}
                    );
                    markBullet = bullet = new TrailFadeBulletType(10, 180, name("pierce")){{
                        scaleLife = true;
                        shieldDamageMultiplier = 1.5f;
                        splashDamageRadius = 56f;
                        splashDamage = 150;

                        lifetime = 420 / speed;
                        pierce = true;
                        pierceCap = 3;

                        hitBlinkTrail = true;
                        despawnBlinkTrail = false;
                        tracers = 2;
                        tracerStroke = 2;
                        tracerSpacing = 8;
                        tracerRandX = 4;
                        tracerFadeOffset = 4;
                        tracerStrokeOffset = 9;
                        tracerUpdateSpacing = 0.8f;

                        trailLength = 12;
                        trailWidth = 3.5f;
                        trailSinScl = 4;
                        trailSinMag = 0.2f;

                        shrinkY = 0;
                        width = 16;
                        height = 45;
                        lightColor = hitColor = trailColor = backColor = Pal.bulletYellowBack;
                        frontColor = Color.white;
                        shootEffect = new MultiEffect(
                        WHFx.shootLine(36, 15f),
                        Fx.shootBig
                        );
                        smokeEffect = Fx.shootBigSmoke;

                        despawnEffect = new MultiEffect(
                        WHFx.trailHitSpark(hitColor, 90, 5, splashDamageRadius * 0.7f, 1.6f, 12),
                        WHFx.trailHitSpark(hitColor, 90, 8, splashDamageRadius, 1.6f, 12).startDelay(30),
                        WHFx.generalExplosion(60, hitColor, splashDamageRadius, 8, false),
                        WHFx.instHit(hitColor, true, 5, splashDamageRadius * 0.7f)
                        );

                        hitEffect = new MultiEffect(
                        WHFx.hitCircle(hitColor, hitColor, 30, 8, 40, 9),
                        WHFx.circleOut(15, 30, 2f),
                        WHFx.hitSpark(hitColor, 30, 8, 50, 1.6f, 9)
                        );
                    }};
                }};
                Weapon navyS6Laser1 = new Weapon(name("s-cannon")){{
                    shake = 2f;
                    shootY = 18 / 4f;
                    x = 18f;
                    y = 5f;
                    rotateSpeed = 2f;
                    reload = 70;
                    recoil = 4f;
                    shootSound = shootLaser;
                    shadow = 20f;
                    rotate = true;
                    bullet = new LightingLaserBulletType(){{
                        damage = 150;
                        sideAngle = 20f;
                        sideWidth = 1.5f;
                        sideLength = 80f;
                        width = 25f;
                        length = 230f;
                        shootEffect = Fx.shockwave;
                        Color c = lightningColor = Pal.slagOrange.cpy().lerp(Pal.orangeSpark, 0.4f).cpy();
                        colors = new Color[]{c.a(0.5f), c.a(0.8f), Color.white};
                    }};
                }};
                Weapon navyS6Laser2 = new Weapon(name("s-cannon")){{
                    shake = 2f;
                    shootY = 18 / 4f;
                    rotateSpeed = 1f;
                    reload = 50f;
                    recoil = 4f;
                    shootSound = shootLaser;
                    shadow = 20f;
                    rotate = true;

                    bullet = new DelayedPointBulletType(){{
                        delayEffectLifeTime = 15;
                        damage = 120;
                        maxRange = range = 280f;
                        width = 16f;
                        Color c = lightningColor = Pal.slagOrange.cpy().lerp(Pal.orangeSpark, 0.4f).cpy();
                        colors = new Color[]{c.a(0.3f), c.a(0.7f), c, Color.white};
                    }};
                }};
                weapons.addAll(
                new PointDefenseBulletWeapon(name("weapon-mount")){{
                    x = 96f / 4f;
                    y = -128 / 4f;
                    reload = 6f;
                    alternate = false;

                    targetInterval = 9f;
                    targetSwitchInterval = 12f;
                    recoil = 0.5f;

                    bullet = new InterceptorBulletType(10, 30){
                        {
                            lifetime = 26f;
                            width = 10;
                            height = 20;
                            trailColor = backColor = hitColor = Pal.bulletYellowBack;
                            trailWidth = 2f;
                            trailLength = 10;
                            hitEffect = despawnEffect = new MultiEffect(
                            WHFx.hitCircle(hitColor, hitColor, 30, 8, 25, 9),
                            WHFx.circleOut(18, 22, 2)
                            );
                        }
                    };
                }},
                copyAndMoveAnd(navyS6Cannon, 0, 52f / 4f, b -> b.layerOffset = 0.13f),
                copyAndMove(navyS6Cannon, 0, 132f / 4f),

                copyAndMove(navyS6Laser1, 80f / 4f, 0),
                copyAndMove(navyS6Laser2, 117f / 4f, -49f / 4f),
                copyAndMove(navyS6MissileLauncher, 0, -157 / 4f)
                );
            }
        };


        navySGreen6 = new UnitType("naval-s6-g"){

            @Override
            public void init(){
                super.init();
                naval = false;
                initPathType();
            }

            public void initPathType(){
                flowfieldPathType =
                Pathfinder.costHover;
                pathCost = ControlPathfinder.costHover;
                pathCostId = ControlPathfinder.costTypes.indexOf(pathCost);
            }

            {
                faceTarget = false;
                pickupUnits = true;
                hovering = true;
                constructor = HoverPayloadUnit::new;
                researchCostMultiplier = 0.1f;
                speed = 1.03f;
                rotateSpeed = 0.86f;
                immunities.addAll(StatusEffects.slow, StatusEffects.unmoving, StatusEffects.electrified);
                hitSize = 9 * 9;
                payloadCapacity = (6 * 6) * tilePayload;
                trailLength = 160;
                waveTrailX = 29.5f;
                waveTrailY = -40;
                trailScl = 5f;
                health = 75000;
                armor = 15;
                outlineColor = WHPal.OutlineS;
                range = 390;

                abilities.add(new MoveEffectAbility(){{
                    minVelocity = 0;
                    interval = 10;
                    effect = new MultiEffect(
                    new ParticleEffect(){{
                        particles = 2;
                        sizeFrom = 10;
                        lifetime = 30;
                        sizeInterp = Interp.pow5In;
                        interp = Interp.pow5Out;
                        length = 70;
                        layer = 60;
                        colorFrom = Pal.heal.cpy().a(0.8f);
                        colorTo = Color.white.cpy().a(0.1f);
                    }},
                    new ParticleEffect(){{
                        particles = 3;
                        sizeFrom = 6;
                        lifetime = 30;
                        sizeInterp = Interp.pow5In;
                        interp = Interp.pow5Out;
                        length = 60;
                        layer = 60;
                        colorFrom = Pal.heal.cpy().a(0.8f);
                        colorTo = Color.white.cpy().a(0.1f);
                    }}
                    );
                }});
                abilities.add(new MoveEffectAbility(){{
                    minVelocity = 1f;
                    interval = 8;
                    effect = new MultiEffect(
                    new ParticleEffect(){{
                        particles = 12;
                        sizeFrom = 8;
                        lifetime = 30;
                        sizeInterp = Interp.pow5In;
                        interp = Interp.pow5Out;
                        length = 45;
                        layer = 60;
                        colorFrom = Pal.heal.cpy().a(0.8f);
                        colorTo = Color.white.cpy().a(0.1f);
                    }},
                    new ParticleEffect(){{
                        particles = 2;
                        sizeFrom = 12;
                        lifetime = 30;
                        sizeInterp = Interp.pow5In;
                        interp = Interp.pow5Out;
                        length = 50;
                        layer = 60;
                        colorFrom = Pal.heal.cpy().a(0.8f);
                        colorTo = Color.white.cpy().a(0.1f);
                    }}
                    );
                }});

                parts.addAll(
                new HoverPart(){{
                    x = 97 / 4f;
                    y = -159 / 4f;
                    mirror = true;
                    sides = 18;
                    radius = 20f;
                    phase = 90f;
                    stroke = 4f;
                    layerOffset = -0.001f;
                    color = Pal.heal.cpy().lerp(Pal.coalBlack, 0.2f);
                }},
                new HoverPart(){{
                    x = 80 / 4f;
                    y = 160 / 4f;
                    mirror = true;
                    sides = 18;
                    radius = 20f;
                    phase = 90f;
                    stroke = 4f;
                    layerOffset = -0.001f;
                    color = Pal.heal.cpy().lerp(Pal.coalBlack, 0.2f);
                }});

                weapons.add(new ChargePointLaserrWeapon(name("naval-g-laser")){
                    {
                        x = 0;
                        y = -41 / 4f;
                        shootY = 78 / 4f;
                        shootX = 0;
                        parentizeEffects = true;
                        alwaysContinuous = true;
                        continuous = true;
                        reload = 180;
                        mirror = false;
                        shootSound = shootSap;
                        rotate = true;
                        rotateSpeed = 0.9f;
                        /* rotationLimit = 160;*/
                        top = true;
                        layerOffset = 0.0001f;
                        recoil = 0;
                        aimChangeSpeed = 1.6f;
                        shoot.firstShotDelay = 60f;
                        despawnCone = 40;
                        bullet = new ChargePointLaser(){{
                            chargeReload = 180;
                            maxRange = length = 408;
                            hitColor = Pal.heal.cpy();
                            beamEffectInterval = 10;
                            beamEffect = new MultiEffect(
                            WHFx.explosionSmokeEffect(hitColor, 90, 60, 4, 8));
                            despawnEffect = none;
                            hitEffect = WHFx.hitSpark(hitColor, 60, 10, 50, 1.5f, 8);
                            shootEffect = WHFx.lineCircleOut(hitColor, 20, 60, 2);
                            chargeEffect = new MultiEffect(
                            WHFx.genericChargeCircle(hitColor, 12, 56, 40).followParent(true),
                            WHFx.lineCircleIn(hitColor, 30, 60, 2).followParent(true),
                            WHFx.genericChargeCircle(hitColor, 8, 56, 60).followParent(true),
                            WHFx.lineCircleIn(hitColor, 60, 60, 2).followParent(true)).followParent(true);
                            trailInterval = 12;
                            lifetime = 20f;
                            splashDamage = damage = 350 / 10f;
                            buildingDamageMultiplier = 0.08f;
                            damageInterval = 6f;
                            splashDamageRadius = 6 * tilesize;
                            maxDamageMultiplier = 2.5f;
                            width = 7f;
                            trailLength = 10;
                        }};
                    }
                });

                Weapon navyGWeapons = new HealWeapon(name("heal-weapon-mount")){{
                    shootSound = Sounds.shootLaser;
                    reload = 60f;
                    x = 8f;
                    y = -6f;
                    rotate = true;
                    rotateSpeed = 2;
                    shootY = 23 / 4f;
                    shoot = new ShootMulti(
                    new ShootAlternate(){{
                        shots = 2;
                        shotDelay = 10;
                    }},
                    new ShootHelix(3, 0.8f, Mathf.PI)
                    );
                    bullet = new BasicBulletType(5f, 40){{
                        width = 10;
                        height = width * 1.8f;
                        trailLength = 8;
                        lifetime = 101.65f;
                        drag = 0.01f;

                        healPercent = 2f;
                        collidesTeam = true;
                        trailColor = hitColor = backColor = Pal.heal;
                        frontColor = Color.white;
                        shootEffect = WHFx.hitCircle(hitColor, hitColor, 30, 4, 12, 5);
                        despawnEffect = hitEffect = new MultiEffect(
                        WHFx.lineCircleOut(hitColor, 20, 18, 2),
                        WHFx.hitCircle(hitColor, hitColor, 20, 5, 18, 6)
                        );
                    }};
                }};

                weapons.add(
                new Weapon(name("emp-cannon")){{
                    rotate = true;

                    x = 100f / 4f;
                    y = 6f / 4f;

                    reload = 90f;
                    shake = 3f;
                    rotateSpeed = 2f;
                    shadow = 30f;
                    shootY = 72 / 4f;
                    recoil = 4f;
                    cooldownTime = reload - 10f;

                    shootSound = shootLaser;

                    shoot = new ShootAlternate(){{
                        shots = 2;
                        shotDelay = 5;
                    }};

                    bullet = new EmpBulletType(){{
                        float rad = 70;

                        sprite = name("energy-bullet");
                        width = 12;
                        height = 24;

                        scaleLife = true;
                        lightOpacity = 0.7f;
                        unitDamageScl = 1f;
                        healPercent = 12f;
                        timeIncrease = 2.5f;
                        timeDuration = 60f * 8f;
                        powerDamageScl = 2.5f;

                        inaccuracy = 3f;

                        damage = splashDamage = 90;
                        splashDamageRadius = rad;

                        hitColor = lightColor = Pal.heal;
                        lightRadius = 70f;
                        clipSize = 250f;
                        shootEffect = Fx.hitEmpSpark;
                        smokeEffect = Fx.shootBigSmoke2;

                        backColor = Pal.heal;
                        frontColor = Color.white;

                        shrinkY = 0f;
                        speed = 6f;
                        lifetime = 380 / speed;
                        trailLength = 10;
                        trailWidth = 2.5f;
                        weaveMag = 1f;
                        weaveScale = 5f;
                        trailColor = Pal.heal;
                        trailInterval = 7f;

                        hitShake = 4f;
                        trailRotation = true;
                        status = StatusEffects.electrified;
                        hitSound = explosionReactor;

                        trailEffect = new Effect(16f, e -> {
                            color(Pal.heal);
                            for(int s : Mathf.signs){
                                Drawf.tri(e.x, e.y, 4f, 15f * e.fslope(), e.rotation + 90f * s);
                            }
                        });

                        hitEffect = new MultiEffect(
                        WHFx.hitSpark(Pal.heal, 30, 20, rad, 1.2f, 8),
                        WHFx.explosionSmokeEffect(Pal.heal, 30, rad, 8, 12),
                        WHFx.instBombSize(Pal.heal, 4, rad * 1.5f),
                        new Effect(50f, 100f, e -> {

                            color(Pal.heal);
                            stroke(e.fout() * 3f);
                            Lines.circle(e.x, e.y, rad);

                            int points = 3;
                            float offset = Mathf.randomSeed(e.id, 360f);
                            for(int i = 0; i < points; i++){
                                float angle = i * 360f / points;
                                float ang = angle + offset * e.fin();
                                //for(int s : Mathf.zeroOne){
                                Drawf.tri(e.x + Angles.trnsx(ang, rad), e.y + Angles.trnsy(ang, rad), 6f, 50f * e.fout(), ang);
                                //}
                            }

                            Fill.circle(e.x, e.y, 12f * e.fout());
                            color();
                            Fill.circle(e.x, e.y, 6f * e.fout());
                            Drawf.light(e.x, e.y, rad * 1.6f, Pal.heal, e.fout());
                        }));
                    }};
                }},
                copyAndMove(navyGWeapons, 88 / 4f, 123f / 4f),
                copyAndMove(navyGWeapons, 116 / 4f, -82 / 4f)
                );
            }
        }

        ;

        airRaiderS = new

        UnitType(name("air-raider-s")){
            {
                constructor = AirRaiderUnitType::new;
                controller = u -> new

                AirRaiderAI(Mode.strafe);

                lifetime = 1500f;
                speed = 5;
                drag = 0.06f;
                accel = 0.08f;
                rotateSpeed = 1f;
                immunities.

                add(StatusEffects.unmoving);

                ammoType = new

                PowerAmmoType(3000);

                hitSize = 20f;
                flying = true;
                outlineColor = WHPal.Outline;
                engineLayer = 110;
                health = 1800;
                armor = 6;
                engineOffset = 7;
                engineSize = 4;
                trailLength = 15;
                faceTarget = true;
                hidden = true;
                allowedInPayloads = playerControllable = useUnitCap = logicControllable = false;

                fallEffect = fallEngineEffect = Fx.none;
                physics = false;
                bounded = false;

                weapons.

                add(new ARWeapon("wh-air4-weapon1"){
                    {
                        x = -14;
                        y = 5;
                        reload = 3.5f;
                        rotate = true;
                        rotationLimit = 12;
                        shootCone = 30;
                        inaccuracy = 1.5f;
                        rotateSpeed = 0.7f;
                        shootSound = WHSounds.energyShoot;
                        layerOffset = -0.01f;
                        top = false;
                        bullet = new BasicBulletType(45, 10){

                            {
                                shrinkY = 0;
                                frontColor = Color.white;
                                backColor = WHPal.ShootOrangeLight;
                                shootEffect = WHFx.lineCircleOut(WHPal.ShootOrangeLight, 6, 8, 3);
                                smokeEffect = Fx.none;
                                hitEffect = despawnEffect = new MultiEffect(
                                WHFx.blast(WHPal.ShootOrangeLight, 12)
                                );
                                trailLength = 5;
                                trailWidth = 1.5f;
                                trailColor = WHPal.ShootOrangeLight;
                                damage = 200;
                                width = 6;
                                height = 12;
                                speed = 10;
                                lifetime = 22;
                                buildingDamageMultiplier = 0.3f;
                            }
                        };
                    }
                });
            }
        };
        airRaiderM = new

        UnitType("air-raider-m"){
            {
                constructor = AirRaiderUnitType::new;
                controller = u -> new AirRaiderAI(Mode.missile){{
                    deathDelay = 360f;
                }};

                lifetime = 1500f;
                health = 20000;
                armor = 13;
                speed = 4;
                drag = 0.04f;
                accel = 0.08f;
                rotateSpeed = 0.3f;
                ammoType = new PowerAmmoType(3000);
                hitSize = 30f;
                flying = true;
                outlineRadius = 3;
                outlineColor = WHPal.Outline;
                engineLayer = 110;
                engineSize = 4;
                engineOffset = 30;
                trailLength = 20;
                faceTarget = true;
                hidden = true;
                lowAltitude = false;
                allowedInPayloads = playerControllable = useUnitCap = logicControllable = false;
                fallEffect = fallEngineEffect = Fx.none;
                physics = false;
                bounded = false;

                for(int i : Mathf.signs){
                    engines.add(new UnitEngine(-10 * i, -30, 4, -90f));
                }
                weapons.add(new ARWeapon("wh-air5-weapon2"){
                    {
                        x = 25;
                        y = -8;
                        layerOffset = -0.01f;
                        reload = 999999;
                        rotate = true;
                        rotateSpeed = 1;
                        rotationLimit = 60;
                        shootCone = 15;
                        xRand = 10;
                        alternate = false;
                        mirror = true;
                        shoot = new ShootPattern(){{
                            shots = 12;
                            shotDelay = 6;
                        }};
                        inaccuracy = 2f;
                        shootSound = Sounds.shootMissile;
                        velocityRnd = 0.3f;
                        bullet = WHBullets.airRaiderMissile;

                    }
                });
            }
        };

        airRaiderB = new

        UnitType("air-raider-b"){
            {
                constructor = AirRaiderUnitType::new;
                controller = u -> new AirRaiderAI(Mode.bomb){{
                    deathDelay = 360f;
                }};

                lifetime = 1500f;

                armor = 16f;
                health = 70000;
                speed = 3;
                rotateSpeed = 0.1f;
                accel = 0.05f;
                drag = 0.017f;
                lowAltitude = false;
                flying = true;
                outlineRadius = 3;
                outlineColor = WHPal.Outline;
                engineOffset = 40;
                engineSize = 10f;
                engineLayer = 110;
                trailLength = 45;
                faceTarget = false;
                hitSize = 50f;
                targetAir = false;

                fallEffect = fallEngineEffect = Fx.none;
                physics = false;
                bounded = false;

                ammoType = new ItemAmmoType(WHItems.ceramite);

                weapons.add(new ARWeapon(){{
                    x = y = 0f;
                    mirror = false;
                    ignoreRotation = true;
                    reload = 99999;

                    soundPitchMin = 1f;
                    shootSound = Sounds.shootMissileSmall;
                    ejectEffect = Fx.none;
                    bullet = WHBullets.airRaiderBomb;
                }});
            }
        };

        test = new

        UnitType("scepter"){
            {
                constructor = MechUnit::create;
                speed = 3.6f;
                hitSize = 20f;
                rotateSpeed = 2.1f;
                health = 500000;
                armor = 10f;
                mechFrontSway = 1f;
                flying = false;
                ammoType = new ItemAmmoType(Items.thorium);
          /*  abilities.add(
            new EllipseForceFieldAbility(100, 80, 2000/60f, 10000f, 60f * 3, 0.2f)
            {{shader=true;fullAbsorb=true;Regen=true;}});*/
                mechStepParticles = true;
                stepShake = 0.15f;
                singleTarget = true;
                drownTimeMultiplier = 4f;
                BulletType smallBullet = new BasicBulletType(3f, 10){{
                    width = 7f;
                    height = 9f;
                    lifetime = 50f;
                }};
                weapons.add(new LaserPointDefenseWeapon("reign-weapon", 3){{
                    y = 1f;
                    x = 21.5f;
                    layerOffset = -0.002f;
                    bullet = new BulletType(){{
                        damage = 4;
                        maxRange = 240;
                        collidesGround = false;
                    }};
                }});
                weapons.addAll(
                new MarkWeapon("reign-weapon"){{
                    layerOffset = -0.001f;
                    y = 1f;
                    x = 21.5f;
                    shootY = 11f;
                    reload = 60f;
                    recoil = 5f;
                    shake = 2f;
                    ejectEffect = Fx.casing4;
                    shootSound = explosionMissile;
                    rotate = true;
                    rotateSpeed = 1f;
                    rotationLimit = 60f;
                    shootCone = 10f;
                    markChance = 0.3f;

                    shoot = new ShootBarrel(){
                        {
                            shots = 3;
                            barrels = new float[]{
                            -15f, 5f, 0f,
                            0f, 5f, 0f,
                            15f, 5f, 0f,
                            };
                            shotDelay = 15f;
                        }
                    };

                    shoot.firstShotDelay = markShoot.firstShotDelay = Fx.greenLaserChargeSmall.lifetime - 1f;
                    continuous = true;
                    bullet = new LaserBeamBulletType(){{
                        pierceCap = 1;
                        moveInterp = Interp.pow10Out;
                        damage = 35f;
                        damageMult = 2f;
                        length = 10f;
                        extensionProportion = 25f;
                        hitEffect = Fx.hitMeltHeal;
                        drawSize = 420f;
                        lifetime = 200f;
                        shake = 1f;
                        despawnEffect = Fx.smokeCloud;
                        smokeEffect = Fx.none;

                        chargeEffect = Fx.greenLaserChargeSmall;

                        incendChance = 0.1f;
                        incendSpread = 5f;
                        incendAmount = 1;
                        colors = new Color[]{Pal.heal.cpy().a(.2f), Pal.heal.cpy().a(.5f), Pal.heal.cpy().mul(1.2f), Color.white};
                    }};

                    markBullet = new LaserBeamBulletType(){{
                        pierceCap = 1;
                        moveInterp = Interp.pow10Out;
                        damage = 90f;
                        damageMult = 5f;
                        length = 10f;
                        extensionProportion = 25f;
                        hitEffect = Fx.hitMeltHeal;
                        drawSize = 420f;
                        lifetime = 200f;
                        shake = 1f;
                        despawnEffect = Fx.smokeCloud;
                        smokeEffect = Fx.none;

                        chargeEffect = Fx.greenLaserChargeSmall;

                        incendChance = 0.1f;
                        incendSpread = 5f;
                        incendAmount = 1;

                        colors = new Color[]{Pal.slagOrange.cpy().a(.2f), Pal.slagOrange.cpy().a(.5f), Pal.slagOrange.cpy().mul(1.2f), Color.white};
                    }};

                }});
                weapons.add(new Weapon("mount-weapon"){{
                                x = 8.5f;
                                y = 6f;
                                reload = 30f;
                                rotate = true;
                                rotateSpeed = 5f;
                                alwaysContinuous = true;
                                alternate = false;
                                bullet = new MoveSuppressionBullet(30f, 180, 3){{
                                    color = Pal.slagOrange.cpy().lerp(Color.white, 0.2f);
                                }};

                            }},

                new Weapon("scepter-mount"){{
                    reload = 100;
                    x = 8.5f;
                    y = -7f;
                    rotate = true;
                    ejectEffect = Fx.casing1;
                    bullet = WHBulletsOther.RevengeBullet1;
                    rotateSpeed = 3f;
                }},

                new Weapon("scepter-mount"){{
                    reload = 200;
                    x = 8.5f;
                    y = -7f;
                    rotate = true;
                    ejectEffect = Fx.casing1;
                    bullet = WHBulletsOther.RevengeBullet2;
                    rotateSpeed = 3f;
                }},

                new Weapon("scepter-mount"){{
                    reload = 200;
                    x = 8.5f;
                    y = -7f;
                    rotate = true;
                    ejectEffect = Fx.casing1;
                    bullet = WHBulletsOther.RevengeBullet3;
                    rotateSpeed = 3f;
                }});

                weapons.add(new PointLaserWeapon(name("gun-mount-2")){
                    {
                        x = 122 / 4f;
                        y = 230 / 4f;
                        shootY = 24 / 4f;
                        shootX = 0;
                        parentizeEffects = true;
                        alwaysContinuous = true;
                        continuous = true;
                        reload = 180;
                        mirror = false;
                        shootSound = shootSap;
                        rotate = true;
                        rotateSpeed = 3;
                        top = true;
                        recoil = 0;

                        bullet = new LightingPointLaserBulletType(){{
                            maxRange = range = 280;
                            hitColor = WHPal.ShootOrange;
                            despawnEffect = none;
                            hitEffect = WHFx.hitSpark(hitColor, 60, 10, 50, 1.5f, 8);
                            shootEffect = WHFx.lineCircleOut(hitColor, 20, 60, 2);
                            trailInterval = 12;
                            lifetime = 20f;
                            buildingDamageMultiplier = 0;
                            damageInterval = 6f;
                            width = 7f;
                            trailLength = 10;
                        }};
                    }
                });

            }
        };

    }


    public static Weapon copyAnd(Weapon weapon, Cons<Weapon> modifier){
        Weapon n = weapon.copy();
        modifier.get(n);
        return n;
    }

    public static Weapon copy(Weapon weapon){
        return weapon.copy();
    }

    public static Weapon copyAndMove(Weapon weapon, float x, float y){
        Weapon n = weapon.copy();
        n.x = x;
        n.y = y;
        return n;
    }

    public static Weapon copyAndMoveAnd(Weapon weapon, float x, float y, Cons<Weapon> modifier){
        Weapon n = weapon.copy();
        n.x = x;
        n.y = y;
        modifier.get(n);
        return n;
    }

}



