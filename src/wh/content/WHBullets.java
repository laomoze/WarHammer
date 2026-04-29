//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.ExplosionEffect;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.effect.WaveEffect;
import mindustry.entities.effect.WrapEffect;
import mindustry.entities.part.FlarePart;
import mindustry.entities.pattern.ShootAlternate;
import mindustry.entities.pattern.ShootBarrel;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.pattern.ShootSpread;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.type.StatusEffect;
import mindustry.world.blocks.ControlBlock;
import wh.entities.bullet.*;
import wh.entities.bullet.laser.DelayedPointBulletType;
import wh.entities.bullet.laser.LightingLaserBulletType;
import wh.entities.bullet.laser.LightningLinkerBulletType;
import wh.entities.bullet.laser.SizeDamageBullet;
import wh.entities.world.blocks.defense.turrets.HeatTurret.HeatBulletType;
import wh.entities.world.blocks.defense.turrets.ShootMatchTurret.ShootMatchTurretBuild;
import wh.gen.PlasmaFire;
import wh.graphics.*;
import wh.util.WHUtils;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static mindustry.gen.Sounds.*;
import static wh.content.WHBulletsOther.*;
import static wh.content.WHFx.*;
import static wh.core.WarHammerMod.name;
import static wh.graphics.WHPal.SkyBlueF;
import static wh.util.WHUtils.rand;

public class WHBullets{

    public static BulletType PlasmaFireBall;

    //unit
    public static BulletType hitter;
    public static BulletType ncBlackHole;
    public static BulletType nuBlackHole;
    //building
    //air-raid
    public static BulletType airRaiderMissile;
    public static BulletType airRaiderBomb;
    public static BulletType raidBulletType;

    //building-break
    public static BulletType sealedPromethiumMillBreak;
    public static BulletType warpBreak;
    public static BulletType plaBreak;

    public static BulletType RayBullet;

    public static BulletType SpikeBulletManganese;
    public static BulletType SpikeBulletChromium;
    public static BulletType SpikeBulletSilicon;
    public static BulletType SpikeBulletGraphite;

    //turret
    //22
    public static BulletType CrushBulletManganese;
    public static BulletType CrushBulletMetaGlass;
    public static BulletType CrushBulletPlastanium;

    public static BulletType AutoGunGraphite;
    public static BulletType AutoGunChromium;
    public static BulletType AutoGunCombustible;
    public static BulletType AutoGunArmorAlloy;

    //33
    public static BulletType LcarusBullet;
    public static BulletType LcarusBulletEnhanced;

    public static BulletType SSWordMnSteel;
    public static BulletType SSWordCombustible;
    public static BulletType SSWordArmorAlloy;
    public static BulletType SSWordPlastanium;

    public static BulletType ShardTungsten;
    public static BulletType ShardMolybdenumAlloy;
    public static BulletType ShardRefineCeramite;

    public static BulletType BladeMnSteel;
    public static BulletType BladePlastanium;
    public static BulletType BladeCarbide;
    public static BulletType BladeCeramite;

    //44
    public static BulletType PreventChromium;
    public static BulletType PreventUranium;
    public static BulletType PreventTungsten;
    public static BulletType PreventCarbide;
    public static BulletType PreventArmorAlloy;
    public static BulletType PreventCombustible;

    public static BulletType HeavyHammerUranium;
    public static BulletType HeavyHammerCeramite;
    public static BulletType HeavyHammerMolybdenumAlloy;

    public static BulletType IonizeEntanglementBullet;
    public static BulletType IonizeResonantCrystalBullet;

    public static BulletType ViperBullet;
    public static BulletType ViperBulletEnhance;

    public static BulletType PyrosBullet;
    public static BulletType PyrosBulletEnhance1;
    public static BulletType PyrosBulletEnhance1Main;
    public static BulletType PyrosBulletEnhance2;

    //55
    public static BulletType CollapseResonantCrystal;
    public static BulletType CollapseCulverCrystal;

    public static BulletType CycloneMissleLauncherMissile1;
    public static BulletType CycloneMissleLauncherMissile2;
    public static BulletType CycloneMissleLauncherMissile3;

    public static BulletType CrumbleCeramiteBullet;
    public static BulletType CrumbleCulverCrystalBullet;
    public static BulletType CrumbleSealedPromethiumBullet;

    public static BulletType SacramentSealedPromethium;
    public static BulletType SacramentMolybdenumAlloy;
    public static BulletType SacramentCulverCrystal;
    public static BulletType SacramentRefineCeramite;

    //66
    public static BulletType ColossusCeramite;
    public static BulletType ColossusCulverCrystal;
    public static BulletType ColossusMolybdenumAlloy;
    public static BulletType ColossusRefineCeramite;

    public static BulletType HydraTungsten;
    public static BulletType HydraUranium;
    public static BulletType HydraCeramite;
    public static BulletType HydraMolybdenumAlloy;
    public static BulletType HydraRefineCeramite;

    public static BulletType ReckoningTungsten;
    public static BulletType ReckoningCeramite;
    public static BulletType ReckoningMolybdenumAlloy;
    public static BulletType ReckoningSealedPromethium;
    public static BulletType ReckoningCulverCrystal;

    public static BulletType AnnihilateBullet;

    public static BulletType EraseMolybdenumAlloy;
    public static BulletType EraseRefineCeramite;
    public static BulletType EraseAdamantium;

    public static BulletType HectorAdamantium;

    private WHBullets(){
    }

    public static void load(){
        hitter = new EffectBulletType(15){
            {
                speed = 0;
                hittable = false;
                scaledSplashDamage = true;
                collidesTiles = collidesGround = collides = collidesAir = true;
                lightningDamage = 500;
                lightColor = lightningColor = trailColor = hitColor = WHPal.WHYellow;
                lightning = 5;
                lightningLength = 12;
                lightningLengthRand = 12;
                splashDamageRadius = 60.0F;
                hitShake = despawnShake = 20.0F;
                hitSound = despawnSound = explosionArtilleryShock;
                hitEffect = despawnEffect = Fx.none;
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);
            }

            @Override
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
                    Lightning.create(b, lightColor, lightningDamage < 0.0F ? damage : lightningDamage, b.x, b.y,
                    b.rotation() + Mathf.range(lightningCone / 2.0F) + lightningAngle,
                    lightningLength + Mathf.random(lightningLengthRand));
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

                /*WHFx.circleOut.at(b.x, b.y, fdata * 1.25f, b.team.color);*/
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


        PlasmaFireBall = new FireBulletType(1f, 80f){
            {
                colorFrom = colorMid = WHPal.SkyBlue;
                lifetime = 15;
                radius = 4.0F;
                trailEffect = WHFx.PlasmaFireBurn;
            }

            public void draw(Bullet b){
                Draw.color(colorFrom, colorMid, colorTo, b.fin());
                Fill.poly(b.x, b.y, 6, b.fout() * radius, 0);
                Draw.reset();
            }

            @Override
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
                linkRange = splashDamageRadius = 80f;
                effectLightningLength = 60;
                despawnEffect = WHFx.multipRings(110f, Pal.sapBullet, 60f, 3);
            }

            public final float bulletRange = 60f;

            @Override
            public void update(Bullet b){
                super.update(b);
                Vec2 v = new Vec2().set(b);
                if(b.timer(1, 12F)){
                    for(int j = 0; j < 2; ++j){
                        Drawn.randFadeLightningEffect(v.x, v.y, Mathf.random(100), Mathf.random(7, 12), backColor, Mathf.chance(0.5));
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
                Lines.circle(b.x, b.y, bulletRange);
            }
        };

        plaBreak = new LightningLinkerBulletType(){
            {
                speed = 0f;
                lifetime = 180f;
                frontColor = Pal.coalBlack;
                trailColor = backColor = hitColor = lightColor = lightningColor = SkyBlueF.cpy().lerp(Pal.techBlue, 0.3f);
                ;
                damage = 50f;
                absorbable = hittable = false;
                size = 30f;
                shrinkX = 1f;
                shrinkY = 1f;
                maxHit = 3;
                lightning = 1;
                lightningLength = 5;
                lightningLengthRand = 8;
                lightningDamage = 60;
                effectLingtning = 1;
                effectLightningChance = 0.08F;
                splashDamage = 3000 * 7f;
                linkLightingDamage = 120;
                linkRange = splashDamageRadius = 30 * 8f;
                effectLightningLength = 60;
                float r = splashDamageRadius * 0.55f;
                hitEffect = Fx.none;
                despawnEffect = new MultiEffect(new Effect(150, e -> {
                    float realLength = 800;
                    float baseLen = realLength * Mathf.curve(e.fin(Interp.pow2Out), 0, 0.1f);
                    float cwidth = 75;
                    Color[] colors = {hitColor.a(0.1f), hitColor.a(0.2f), hitColor.a(0.6f), Pal.coalBlack};
                    float lengthFalloff = 0.8f;

                    Tmp.v1.trns(90, baseLen).add(e.x, e.y);
                    Tmp.v2.trns(90, baseLen).add(e.x, e.y);
                    Draw.z(Layer.effect + 0.001f);
                    for(Color color : colors){
                        Draw.color(color);
                        Lines.stroke((cwidth *= lengthFalloff) * e.fout());
                        Fill.circle(e.x, e.y, (cwidth *= lengthFalloff) * e.fout());
                        /*  Fill.circle(Tmp.v2.x, Tmp.v2.y, (cwidth *= lengthFalloff) * e.fout());*/
                        Drawf.tri(Tmp.v2.x, Tmp.v2.y, Lines.getStroke(), cwidth, 90);
                        Lines.line(e.x, e.y, Tmp.v2.x, Tmp.v2.y, false);
                    }
                    Draw.reset();
                    Drawf.light(e.x, e.y, Tmp.v1.x, Tmp.v1.y, cwidth * 1.4f * e.fout(), hitColor, 0.6f);

                    e.scaled(120, i -> {
                        float intensity = 0.4f;
                        color(hitColor);
                        Fill.circle(i.x, i.y, intensity * r * i.fout(Interp.pow3Out));
                        float scl = 0.3f * r;
                        Rand rand = new Rand(i.id);
                        randLenVectors(i.id, 8, scl / 3, scl * (1.0F + i.fout(Interp.circleOut)) / 1.5f, (x, y) -> {
                            float angle = Mathf.angle(x, y);
                            float width = i.foutpowdown() * rand.random(scl / 6.0F, scl / 3.0F);
                            float length = rand.random(scl, scl * 2) * i.fout(Interp.circleOut);
                            color(hitColor);
                            Drawn.tri(i.x + x, i.y + y, width, scl / 3.0F * i.fout(Interp.circleOut), angle - 180);
                            Drawn.tri(i.x + x, i.y + y, width, length, angle);
                            color(Color.black);
                            width *= i.fout();
                            Drawn.tri(i.x + x, i.y + y, width / 2.0F, scl / 3.0F * i.fout(Interp.circleOut) * 0.9F * i.fout(), angle - 180);
                            Drawn.tri(i.x + x, i.y + y, width / 2.0F, length / 1.5F * i.fout(), angle);
                        });
                        color(Pal.coalBlack.cpy());
                        Fill.circle(i.x, i.y, intensity * r * 0.6f * i.fout(Interp.pow3Out));
                    });
                }),
                new Effect(120, e -> {
                    color(hitColor);
                    stroke(4f * e.fout());
                    float height = 200;
                    for(int i = 0; i < 3; i++){
                        float yOffset = (height * e.fin(Interp.pow3Out)) * (i) / 3f;
                        Tmp.v1.trns(90, yOffset);
                        Lines.ellipse(e.x, Tmp.v1.y + e.y, r / 2.7f * (1 - i / 3f + 0.2f), 1.25f, 0.95f, 0);
                    }
                }),
                WHFx.subEffect(120, 100, 15, 30, Interp.pow3Out, (id, x, y, rotation, fin) -> {
                    float height = 300f;
                    float yOffset = height * fin;
                    color(hitColor);
                    Tmp.v1.trns(90, yOffset);
                    rand.random(id);
                    float range = rand.random(0.5f, 2f), fout = 1 - fin;
                    stroke(3f * Mathf.curve(fin, 0, 0.2f) * WHFx.fout(fin, 0.85f));
                    randLenVectors(id, 1, r * rand.random(1) * fout, (a, b) -> {
                        lineAngle(a + x, Tmp.v1.y + b + y, 90, 12 * Interp.pow10Out.apply(fin) * range);
                    });
                    Tmp.v2.trns(90, height * fout);
                    randLenVectors(id, 1, r * rand.random(1) * fin, (c, d) -> {
                        lineAngle(x + c, Tmp.v2.y + d + y, 90, 12 * Interp.pow2Out.apply(fin) * range);
                    });
                }),
                WHFx.circleOut(150, hitColor, r * 2f),
                WHFx.hitSpark(150, hitColor, 30, r * 2, 2.5f, 18f),
                WHFx.trailHitSpark(60, hitColor, 20, r * 2, 2, 12f),
                WHFx.instRotation(120, hitColor, r * 2, 45, false));
            }

            public final Effect lineEffect = new Effect(70, e -> {
                rand.setSeed(e.id);
                float height = 200 * rand.random(0.15f, 1.25f);
                float yOffset = height * e.fin(Interp.smooth);
                color(hitColor);
                Tmp.v1.setZero();
                Tmp.v1.trns(90, yOffset);
                stroke(3.5f * Mathf.curve(e.fin(), 0, 0.1f) * WHFx.fout(e.fin(), 0.9f));
                for(int i = 0; i < 4; i++){
                    Tmp.v2.trns(rand.random(360f), splashDamageRadius * e.fout(Interp.smooth));
                    float angle = Mathf.lerp(Mathf.atan2(Tmp.v2.x + e.x, Tmp.v2.y + e.y), 90, e.fin(Interp.smooth));
                    lineAngle(Tmp.v1.x + Tmp.v2.x + e.x, Tmp.v1.y + Tmp.v2.y + e.y, angle, 15 * e.fin(Interp.pow5Out) * WHFx.fout(e.fin(), 0.9f) * range * rand.random(0.5f, 2f));
                }
            });

            public final Effect surroundEffect3 = new TrailEffect(60, 500, hitColor, hitColor, 1, 15, 2)
            .trailUpdater((e, trail, x, y, w, len, index) -> {
                WHFx.rand.setSeed(e.id);
                float range = splashDamageRadius;
                float cur = Mathf.curve(e.fin(), 0, 0.15f);
                Draw.z(Layer.effect);
                Angles.randLenVectors(e.id, 1, range * e.fout(), WHFx.rand.random(0.5f, 1f) * e.fout() * 60, 360f, (x1, y1) -> {
                    trail.length = (int)(cur * len);
                    trail.update(x1 + x, y1 + y, w * e.fout());
                });
            });

            @Override
            public void update(Bullet b){
                super.update(b);
                Vec2 v = new Vec2().set(b);
                if(b.timer(1, 12F)){
                    for(int j = 0; j < 2; ++j){
                        Drawn.randFadeLightningEffect(v.x, v.y, Mathf.random(100), Mathf.random(7, 12), backColor, Mathf.chance(0.5));
                    }
                }
                if(Mathf.chanceDelta(0.05 + 0.15f * b.fin())){
                    surroundEffect3.at(b);
                }
                if(!(b instanceof TrailBullet Interval)) return;
                for(int i = 0; i < 2; i++){
                    if(!Vars.headless){
                        if(Interval.trails[i] == null) Interval.trails[i] = new Trail(22);
                        Interval.trails[i].length = 22;
                    }
                    float dx = WHUtils.dx(b.x, 45, (b.time * (8 - (i % 2 == 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 180 * i),
                    dy = WHUtils.dy(b.y, 45, (b.time * (8 - (i % 2 != 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 180 * i);
                    if(!Vars.headless) Interval.trails[i].update(dx, dy, 2.4f);
                    if(Interval.vs[i] != null) Interval.vs[i].set(dx, dy);
                }
            }

            @Override
            public void draw(Bullet b){
                super.draw(b);
                Draw.color(backColor, b.fout());
                Lines.stroke(2);
                Lines.circle(b.x, b.y, linkRange);
                Draw.color(backColor);
                Draw.z(Layer.bullet - 0.0001f);
                rand.setSeed(b.id);
                float ran = rand.random(180, 360), ran2 = rand.random(90);
                float radio = Time.time + ran;
                for(int i : Mathf.signs){
                    Draw.color(backColor);
                    Drawf.tri(b.x, b.y, 14, 120 * b.fout() * (1 - Mathf.sin(radio, 20, 0.5f)), ran + i * Time.time * 0.5f % 360);
                    Draw.color();
                    Drawf.tri(b.x, b.y, 7, 60 * b.fout() * (1 - Mathf.sin(radio, 20, 0.5f)), ran + i * Time.time * 0.5f % 360);
                }
                for(int i : Mathf.signs){
                    Draw.color(backColor);
                    Drawf.tri(b.x, b.y, 12, 90 * b.fout() * (1 - Mathf.sin(radio, 20, 0.3f)), ran2 + i * Time.time * 0.5f % 360);
                    Draw.color();
                    Drawf.tri(b.x, b.y, 6, 45 * b.fout() * (1 - Mathf.sin(radio, 20, 0.3f)), ran2 + i * Time.time * 0.5f % 360);
                }
                Draw.reset();
                if(!(b instanceof TrailBullet Interval)) return;
                float z = Draw.z();
                Draw.z(z - 1e-4f);
                for(int i = 0; i < 2; i++){
                    if(Interval.trails[i] != null){
                        Interval.trails[i].draw(trailColor, 2.7f * b.fout(Interp.pow5Out));
                        Draw.color(trailColor);
                        Fill.circle(Interval.vs[i].x, Interval.vs[i].y, 2.7f * b.fout(Interp.pow5In));
                    }
                }
                Draw.z(z);
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);
                Vec2 vec = new Vec2().set(b);
                float rad = splashDamageRadius * 1.2f;
                float spacing = 8;
                float damageMulti = b.damageMultiplier();
                for(int k = 0; k < 5; k++){
                    Time.run(k * spacing, () -> {
                        for(int j : Mathf.signs){
                            Vec2 v = Tmp.v6.rnd(Mathf.random(rad * 1.2f)).add(vec);
                            (j > 0 ? WHFx.chainLightningFade : WHFx.chainLightningFadeReversed).at(v.x, v.y, 12, hitColor, vec);
                        }
                    });
                }
                rand.setSeed(b.id);
                for(int k = 0; k < 20; k++){
                    Time.run(k * 5, () -> {
                        lineEffect.at(vec.x, vec.y, rand.random(360), hitColor, null);
                    });
                }
            }


            @Override
            public void init(Bullet b){
                super.init(b);
                if(!(b instanceof TrailBullet Interval)) return;
                for(int i = 0; i < 2; i++){
                    Interval.vs[i] = new Vec2();
                }
            }

            @Override
            public @Nullable Bullet create(
            @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
            float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
            ){
                TrailBullet bullet = TrailBullet.create();

                for(int i = 0; i < 2; i++){
                    if(bullet.trails[i] != null){
                        bullet.trails[i].clear();
                    }
                }
                return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
            }
        };

        warpBreak = new BasicBulletType(0, 100.0F){
            {
                instantDisappear = true;
                status = StatusEffects.slow;
                statusDuration = 180f;
                hitShake = 8;
                hitSound = explosionQuad;
                hitSoundVolume = 3f;
                hitColor = lightColor = lightningColor = backColor = Pal.sapBullet;
                despawnEffect = hitEffect = new MultiEffect(
                new WrapEffect(Fx.dynamicSpikes, hitColor, 70),
                Fx.titanExplosion);
                lightning = 5;
                lightningLength = 8;
                lightningLengthRand = 8;
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
                hitSoundVolume = 4.0F;

            }
        };


        /*SK = new StrafeLaser(300.0F){
            {
                strafeAngle = 0;
            }

            @Override
            public void draw(Bullet b){
                super.draw(b);
                drawMultipleColor(b);
            }
        };*/

        airRaiderMissile = new MissileBulletType(){
            {
                width = 50;
                height = 60;
                sprite = name("large-missile");
                speed = 10;
                lifetime = 35;
                drag = -0.004f;
                homingDelay = 15;
                homingPower = 0.12f;
                homingRange = 30;
                trailLength = 10;
                trailWidth = 3;
                shrinkY = 0.5f;
                shrinkX = 0.5f;
                frontColor = WHPal.ShootOrangeLight;
                lightningColor = trailColor = hitColor = backColor = WHPal.ShootOrange;
                hitEffect = despawnEffect = new MultiEffect(
                WHFx.generalExplosion(60, hitColor, splashDamageRadius, 5, false),
                WHFx.line45Explosion(hitColor, hitColor, 10));

                trailEffect = new Effect(50, e -> {
                    Draw.color(hitColor);
                    Angles.randLenVectors(e.id, 1, -20 * e.finpow(), e.rotation, 80, (x, y) ->
                    Fill.square(e.x + x, e.y + y, 5 * e.foutpow(), Mathf.randomSeed(e.id, 360) + e.time));
                });
                hitSound = explosionQuad;
                hitShake = 1;
                shootEffect = WHFx.shootLineSmall(hitColor);
                smokeEffect = hugeSmokeGray;
                hittable = false;
                damage = 90;
                shieldDamageMultiplier = 5;
                splashDamageRadius = 32;
                splashDamage = 100;
                lightningDamage = 30;
                lightning = 3;
                lightningLength = 12;
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

                despawnSound = explosionQuad;
                hitSound = explosionArtilleryShock;
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
                hitColor = lightColor = lightningColor = trailColor = WHPal.ShootOrange;
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

        raidBulletType = new TrailFadeBulletType(){{
            speed = 7f;
            damage = 500;
            lifetime = 200f;

            lightColor = lightningColor = hitColor = trailColor = frontColor = backColor = Team.crux.color.cpy().lerp(WHPal.ShootOrange, 0.3f);

            trailEffect = Fx.vapor;
            trailParam = 6f;
            trailChance = 0.2f;
            trailInterval = 3;
            trailWidth = 5f;
            trailLength = 30;
            trailInterp = Interp.slope;

            splashDamage = damage;
            splashDamageRadius = 120;
            splashDamagePierce = false;
            scaledSplashDamage = true;

            hitBlinkTrail = despawnBlinkTrail = false;

            despawnHit = true;
            collides = false;

            shrinkY = shrinkX = 0.33f;
            width = 17f;
            height = 55f;

            despawnShake = hitShake = 12f;
            hitEffect = new MultiEffect(
            WHFx.square(200, hitColor, 20, splashDamageRadius + 80, 10),
            WHFx.lightningHitLarge,
            WHFx.hitSpark(130, hitColor, 85, splashDamageRadius * 1.5f, 2.2f, 10f),
            WHFx.subEffect(140, splashDamageRadius + 12, 33, 34f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
                float fout = Interp.pow2Out.apply(1 - fin);
                for(int s : Mathf.signs){
                    Drawf.tri(x, y, 12 * fout, 45 * Mathf.curve(fin, 0, 0.1f) * WHFx.fout(fin, 0.25f), rot + s * 90);
                }
            })));
            despawnEffect = WHFx.circleOut(145f, splashDamageRadius + 15f, 3f);
            shootEffect = instShoot(hitColor, hitColor);
            smokeEffect = WHFx.instShoot(hitColor, frontColor);

            despawnSound = hitSound = Sounds.explosion;
        }};

        //Turrets
        RayBullet = new CritBulletType(5, 30){
            {
                lifetime = 200 / speed;
                width = 2f;
                height = 8f;
                pierceCap = 3;
                bouncing = true;

                lightningColor = hitColor = backColor = trailColor = Pal.techBlue;
                frontColor = backColor.cpy().lerp(Color.white, 0.3f);
                hitEffect = despawnEffect = new MultiEffect(
                Fx.hitBulletColor, WHFx.square(60, hitColor, 4, 12, 4));

                intervalBullet = new LightningBulletType(){{
                    damage = 10;
                    collidesAir = true;
                    lightningColor = Pal.techBlue;
                    lightningLength = 3;
                    lightningLengthRand = 4;

                    //for visual stats only.
                    buildingDamageMultiplier = 0.25f;

                    lightningType = new BulletType(0.0001f, 0f){{
                        lifetime = Fx.lightning.lifetime;
                        hitEffect = Fx.hitLancer;
                        despawnEffect = Fx.none;
                        status = StatusEffects.shocked;
                        statusDuration = 10f;
                        hittable = false;
                        lightColor = Color.white;
                        buildingDamageMultiplier = 0.25f;
                    }};
                }};

                bulletInterval = 10f;
                lightningDamage = 12;
                lightning = 2;
                lightningLength = 2;
                lightningLengthRand = 8;
            }

            @Override
            public void draw(Bullet b){
                Draw.color(backColor);
                Lines.stroke(width);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height);
                Draw.color(frontColor);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height / 2f);
                Draw.reset();
            }
        };

        SpikeBulletManganese = new BasicBulletType(4.5f, 25){{
            ammoMultiplier = 2f;
            lifetime = 185 / speed;
            width = 7f;
            height = 14f;
            hitSize = 5f;
            trailLength = 4;
            trailWidth = width / 4.4f;

            hitColor = backColor = trailColor = WHItems.manganese.color;
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);
            hitEffect = despawnEffect = Fx.hitBulletColor;

        }};

        SpikeBulletChromium = new BasicBulletType(4.5f, 33){{
            ammoMultiplier = 2f;
            lifetime = 185 / speed;
            armorMultiplier = 0.1f;
            width = 7f;
            height = 14f;
            hitSize = 5f;
            trailLength = 4;
            trailWidth = width / 4.4f;

            hitColor = backColor = trailColor = WHItems.chromium.color;
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);
            hitEffect = despawnEffect = Fx.hitBulletColor;

        }};


        SpikeBulletSilicon = new BasicBulletType(5f, 20){{
            reloadMultiplier = 1.5f;
            ammoMultiplier = 4f;
            homingPower = 0.12f;
            lifetime = 185 / speed;
            width = 7f;
            height = 14f;
            hitSize = 5f;
            knockback = 1f;
            trailLength = 4;
            trailWidth = width / 4.4f;

            hitColor = backColor = trailColor = Pal.siliconAmmoBack;
            frontColor = Pal.siliconAmmoFront;
            hitEffect = despawnEffect = Fx.hitBulletColor;
        }};


        SpikeBulletGraphite = new BasicBulletType(4f, 45){{
            rangeChange = 12f;
            ammoMultiplier = 3f;
            armorMultiplier = 0.1f;
            reloadMultiplier = 0.8f;
            lifetime = 185 / speed;
            width = 7f;
            height = 14f;
            hitSize = 5f;
            knockback = 0.3f;
            trailLength = 4;
            trailWidth = width / 4.4f;

            hitEffect = despawnEffect = Fx.hitBulletColor;
            hitColor = backColor = trailColor = Items.graphite.color;
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);

            shootEffect = Fx.shootBigColor;
            smokeEffect = Fx.shootBigSmoke;
        }};

        CrushBulletManganese = new FlakBulletType(6f, 15){{
            shootPattern = new ShootPattern(){{
                shots = 2;
                shotDelay = 5f;
            }};
            ammoMultiplier = 2;
            lifetime = 180f / speed;
            width = 6;
            height = 8;
            trailWidth = width / 4.4f;
            trailLength = 4;
            backColor = hitColor = trailColor = WHPal.MnSteelColor;
            explodeRange = splashDamageRadius = 20;
            shieldDamageMultiplier = 0.5f;
            splashDamage = 20f;
            shootEffect = Fx.shootSmallColor;
            hitEffect = Fx.flakExplosion;
            hitEffect = new WrapEffect(Fx.flakExplosion, WHPal.MnSteelColor);
            collidesGround = true;
        }};

        CrushBulletMetaGlass = new FlakBulletType(5, 35){{
            reloadMultiplier = 0.8f;
            rangeChange = 1.5f * 8f;
            ammoMultiplier = 2;

            lifetime = 180f / speed;
            width = 9;
            height = 9;
            trailWidth = width / 4.4f;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.1f;
            splashDamage = 30f;
            explodeRange = splashDamageRadius = 28;
            shieldDamageMultiplier = 0.5f;
            backColor = hitColor = trailColor = Pal.glassAmmoBack;
            shootEffect = Fx.shootSmallColor;
            hitEffect = Fx.flakExplosion;

            collidesGround = true;

            fragBullet = new BasicBulletType(3f, 12, name("tall")){{
                width = 10f;
                height = 10f;
                shrinkY = 1f;
                lifetime = 20f;
                backColor = Pal.gray;
                frontColor = Color.white;
                despawnEffect = Fx.none;
                collidesGround = true;
            }};
        }};

        CrushBulletPlastanium = new FlakBulletType(6.5f, 30){{
            reloadMultiplier = 1.15f;
            ammoMultiplier = 3;
            lifetime = 180f / speed;
            width = 6;
            height = 10;
            trailWidth = 2f;
            trailLength = 5;
            trailSinScl = 12f;
            trailSinMag = 0.1f;
            hitColor = trailColor = backColor = Pal.plastaniumBack;
            frontColor = Pal.plastaniumFront;
            damage = splashDamage = 45f;
            explodeRange = splashDamageRadius = 28;
            shieldDamageMultiplier = 0.5f;
            shootEffect = Fx.shootSmallColor;
            hitEffect = new MultiEffect(Fx.plasticExplosion, Fx.shockwave);
            collidesGround = true;
            fragBullet = new BasicBulletType(2.5f, 10, "bullet"){{
                width = 10f;
                height = 12f;
                shrinkY = 1f;
                lifetime = 12f;
                backColor = Pal.plastaniumBack;
                frontColor = Pal.plastaniumFront;
                despawnEffect = Fx.none;
                collidesAir = false;
            }};
            fragBullets = 3;
        }};

        AutoGunGraphite = new CritBulletType(8f, 65){
            {
                reloadMultiplier = 0.8f;
                critChance = 0.1f;
                critMultiplier = 2f;
                lifetime = 240 / 8f;
                backColor = hitColor = trailColor = Items.graphite.color;
                width = 3;
                height = width * 4.5f;
                trailWidth = 2f;
                trailLength = 5;
                shootEffect = Fx.shootBig;
                smokeEffect = Fx.shootBigSmoke;
                trailChance = 0.1f;
                critEffect = WHFx.square(20, Items.graphite.color, 1, 10, 3f);
                despawnEffect = hitEffect = Fx.explosion;
            }

            @Override
            public void draw(Bullet b){
                Draw.color(backColor);
                Lines.stroke(width);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height);
                Draw.color(frontColor);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height / 2f);
                Draw.reset();
            }
        };

        AutoGunChromium = new CritBulletType(8.5f, 40){
            {
                ammoMultiplier = 2;
                critChance = 0.1f;
                critMultiplier = 2f;
                lifetime = 240 / 8.5f;
                backColor = hitColor = trailColor = WHItems.chromium.color;
                width = 3;
                height = width * 4.5f;
                trailWidth = 2f;
                trailLength = 5;
                trailSinScl = 12f;
                trailSinMag = 0.08f;
                homingDelay = 15;
                homingPower = 0.01f;
                homingRange = 20;
                splashDamageRadius = 20;
                splashDamage = 42;
                trailChance = 0.1f;
                critEffect = WHFx.square(20, Items.silicon.color, 1, 10, 3f);
                shootEffect = Fx.shootBig;
                smokeEffect = Fx.shootBigSmoke;
                despawnEffect = hitEffect = Fx.explosion;
            }

            @Override
            public void draw(Bullet b){
                Draw.color(backColor);
                Lines.stroke(width);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height);
                Draw.color(frontColor);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height / 2f);
                Draw.reset();
            }
        };

        AutoGunCombustible = new CritBulletType(8.5f, 70){
            {
                ammoMultiplier = 6f;
                reloadMultiplier = 2;
                rangeChange = 16f;
                critChance = 0.2f;
                critMultiplier = 1.5f;
                knockback = 0.8f;
                lifetime = 240 / 8.5f;
                backColor = hitColor = trailColor = Items.pyratite.color;
                width = 3;
                height = width * 4.5f;
                trailWidth = 2f;
                trailLength = 5;
                trailSinScl = 12f;
                trailSinMag = 0.08f;
                makeFire = true;
                status = StatusEffects.burning;
                statusDuration = 60;
                trailChance = 0.1f;
                critEffect = Fx.smoke;
                shootEffect = Fx.shootBig;
                smokeEffect = Fx.shootBigSmoke;
                despawnEffect = hitEffect = Fx.explosion;
            }

            @Override
            public void draw(Bullet b){
                Draw.color(backColor);
                Lines.stroke(width);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height);
                Draw.color(frontColor);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height / 2f);
                Draw.reset();
            }
        };
        AutoGunArmorAlloy = new CritBulletType(8.5f, 30){
            {
                ammoMultiplier = 4f;
                reloadMultiplier = 1.5f;
                critChance = 0.1f;
                critMultiplier = 2f;
                knockback = 0.8f;
                lifetime = 240 / 8.5f;
                backColor = hitColor = trailColor = Items.surgeAlloy.color.cpy().lerp(WHItems.manganeseSteel.color, 0.7f);
                width = 2;
                height = width * 5;
                trailWidth = 2f;
                trailLength = 5;
                trailSinScl = 12f;
                trailSinMag = 0.12f;
                status = StatusEffects.slow;
                statusDuration = 60;
                splashDamageRadius = 20;
                splashDamage = 55;
                trailChance = 0.1f;
                critEffect = WHFx.square(20, trailColor, 1, 10, 6f);
                shootEffect = Fx.shootBig;
                smokeEffect = Fx.shootBigSmoke;
                despawnEffect = hitEffect = Fx.explosion;
            }

            @Override
            public void draw(Bullet b){
                Draw.color(backColor);
                Lines.stroke(width);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height);
                Draw.color(frontColor);
                Lines.lineAngleCenter(b.x, b.y, b.rotation(), height / 2f);
                Draw.reset();
            }
        };

        LcarusBullet = new DelayedPointBulletType(){{
            colors = new Color[]{WHPal.SkyBlue.cpy().mul(1f, 1f, 1f, 0.4f), WHPal.SkyBlue.cpy(), Pal.lancerLaser};
            damage = 50;
            splashDamageRadius = 32;
            splashDamage = 80;
            width = 20;
            laser = Fx.none;
            delayEffectLifeTime = 28f;
            renderingDistortion = true;
            hitColor = WHPal.SkyBlue.cpy();
            hitEffect = despawnEffect = new MultiEffect(new ExplosionEffect(){{
                waveColor = sparkColor = WHPal.SkyBlue;
                waveRad = splashDamageRadius;
                waveStroke = 2f;
                waveLife = 15f;
                sparks = 10;
                sparkRad = 30;
                sparkLen = 8;
            }},
            WHFx.square(45, hitColor, 10, splashDamageRadius, 4)
            );
            shootEffect = new MultiEffect(WHFx.lineCircleOut(30, WHPal.SkyBlue, 13, 2f),
            WHFx.shootCircleSmall(WHPal.SkyBlue));
        }};

        LcarusBulletEnhanced = new DelayedPointBulletType(){{
            reloadMultiplier = 0.75f;
            colors = new Color[]{WHPal.ShootOrangeLight.cpy().a(0.3f), WHPal.ShootOrangeLight.cpy().a(0.7f), WHPal.ShootOrangeLight.cpy()};
            damage = 120;
            rangeChange = 16f;
            shieldDamageMultiplier = 2;
            splashDamageRadius = 40;
            splashDamage = 100;
            laser = Fx.none;
            width = 20;
            delayEffectLifeTime = 28f;
            renderingDistortion = true;
            hitColor = WHPal.ShootOrangeLight;
            hitEffect = WHFx.linePolyOut(60, hitColor, splashDamageRadius, 2, 4, 0);
            despawnEffect = new MultiEffect(new ExplosionEffect(){{
                waveColor = sparkColor = WHPal.ShootOrangeLight;
                waveRad = splashDamageRadius;
                waveLife = 15f;
                waveStroke = 2f;
                sparks = 15;
                sparkRad = 30;
                sparkLen = 8;
            }},
            WHFx.square(45, hitColor, 10, splashDamageRadius, 4)
            );
            shootEffect = new MultiEffect(WHFx.lineCircleOut(30, WHPal.ShootOrangeLight, 13, 2f),
            WHFx.shootCircleSmall(WHPal.ShootOrangeLight));
        }};

        SSWordMnSteel = new CritMissileBulletType(){{
            ammoMultiplier = 3;
            critChance = 0.1f;
            critMultiplier = 2f;
            speed = 8f;
            damage = 50;
            splashDamageRadius = 32;
            splashDamage = 45;
            lifetime = 420 / speed;
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
            hitColor = trailColor = backColor = WHItems.manganeseSteel.color.cpy();
            width = 18f;
            height = 60;
            hitSize = 10f;
            keepVelocity = false;

            smokeEffect = Fx.shootSmallFlame;
            shootEffect = new MultiEffect(Fx.shootBigColor, Fx.colorSparkBig);
            hitEffect = despawnEffect = new MultiEffect(
            new ExplosionEffect(){{
                lifetime = 50f;
                waveStroke = 5f;
                waveColor = sparkColor = hitColor;
                waveRad = 45f;
                smokeSize = 0f;
                smokeSizeBase = 0f;
                sparks = 10;
                sparkRad = 25f;
                sparkLen = 8f;
                sparkStroke = 3f;
            }},
            WHFx.hitSpark(30, WHItems.manganeseSteel.color, 8, 40, 1, 5));
            critEffect = WHFx.square(30, WHItems.manganeseSteel.color, 1, 10, 3f);
            flameWidth = 3f;
            flameLength = 16f;
            lengthOffset = 5;
            colors = new Color[]{WHItems.manganeseSteel.color.cpy().a(0.4f), WHItems.manganeseSteel.color.cpy().a(0.8f), Pal.lancerLaser};
        }};

        SSWordPlastanium = new CritMissileBulletType(){{
            ammoMultiplier = 3;
            reloadMultiplier = 1.5f;

            critChance = 0.1f;
            critMultiplier = 2f;
            speed = 8f;
            damage = 40;
            lifetime = 420 / speed;
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

            fragBullets = 6;
            fragBullet = new BasicBulletType(2.5f, 25, name("tall")){{
                splashDamageRadius = 32;
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
            hitEffect = despawnEffect = new MultiEffect(Fx.plasticExplosion,
            WHFx.hitSpark(30, Items.plastanium.color, 8, 40, 1, 5));
            critEffect = WHFx.square(30, Items.plastanium.color, 1, 10, 5f);
            flameWidth = 3f;
            flameLength = 16f;
            lengthOffset = 5;
            colors = new Color[]{Items.plastanium.color.cpy().a(0.4f), Items.plastanium.color.cpy().a(0.8f), Items.plastanium.color.cpy().lerp(Color.white, 0.8f)};
        }};

        SSWordCombustible = new CritMissileBulletType(){{
            ammoMultiplier = 5;
            critChance = 0.1f;
            critMultiplier = 2f;
            speed = 8f;
            damage = 70;
            splashDamageRadius = 50;
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
            hitColor = trailColor = backColor = WHItems.combustible.color.cpy();
            width = 18f;
            height = 60;
            hitSize = 10f;
            keepVelocity = false;
            makeFire = true;
            incendSpread = 10f;
            incendChance = 0.1f;
            smokeEffect = Fx.shootSmallFlame;
            shootEffect = new MultiEffect(Fx.shootBigColor, Fx.colorSparkBig);
            hitEffect = despawnEffect = new MultiEffect(Fx.flakExplosionBig,
            WHFx.hitSpark(30, Items.pyratite.color, 8, 40, 1, 5));
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

        SSWordArmorAlloy = new CritMissileBulletType(){{
            ammoMultiplier = 4;
            reloadMultiplier = 0.8f;

            critChance = 0.1f;
            critMultiplier = 2f;
            speed = 8f;
            damage = 100;
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
            lightningColor = hitColor = trailColor = backColor = WHItems.armorAlloy.color.cpy();
            width = 18f;
            height = 60;
            hitSize = 10f;
            keepVelocity = false;

            status = WHStatusEffects.rock;
            statusDuration = 60f;
            smokeEffect = Fx.shootSmallFlame;
            shootEffect = new MultiEffect(Fx.shootBigColor, Fx.colorSparkBig);
            hitEffect = despawnEffect = new MultiEffect(
            new ExplosionEffect(){{
                lifetime = 50f;
                waveStroke = 5f;
                waveColor = sparkColor = hitColor;
                waveRad = 45f;
                smokeSize = 0f;
                smokeSizeBase = 0f;
                sparks = 10;
                sparkRad = 25f;
                sparkLen = 8f;
                sparkStroke = 3f;
            }},
            WHFx.hitSpark(30, hitColor, 8, 40, 1, 5),
            WHFx.instHit(hitColor, true, 3, 18));
            critEffect = WHFx.square(30, hitColor, 1, 10, 3);
            flameWidth = 3f;
            flameLength = 16f;
            lengthOffset = 5;
            colors = new Color[]{hitColor.cpy().a(0.4f), hitColor.cpy().a(0.8f), hitColor.cpy().lerp(Color.white, 0.8f)};
        }};

        ShardTungsten = new CritBulletType(6, 70){{
            critChance = 0.1f;
            critMultiplier = 2f;
            damage = 60;
            lifetime = 200f / 6f;
            splashDamageRadius = 24f;
            splashDamage = 25f;
            buildingDamageMultiplier = 0.2f;
            pierce = true;
            pierceCap = 3;
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
            hitEffect = WHFx.hitSpark(20, Items.tungsten.color.cpy(), 3, 20, 1f, 5);
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
            critChance = 0.4f;
            critMultiplier = 2.5f;
            lifetime = 200f / 10f;
            splashDamageRadius = 24f;
            splashDamage = 50f;
            buildingDamageMultiplier = 0.2f;
            reloadMultiplier = 0.8f;
            pierce = true;
            pierceCap = 4;
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
            hitEffect = WHFx.hitSpark(20, WHItems.molybdenumAlloy.color.cpy(), 3, 20, 1f, 5);
            despawnEffect = new MultiEffect(WHFx.instBombSize(WHItems.molybdenumAlloy.color.cpy(), 4, 50), Fx.hitBulletColor, Fx.hitLancer);
        }};

        ShardRefineCeramite = new TrailFadeBulletType(6, 150){{

            rangeChange = 8 * 6f;
            lifetime = (200f + 48f) / 6f;
            splashDamageRadius = 24f;
            splashDamage = 35f;
            buildingDamageMultiplier = 0.2f;
            pierce = true;
            pierceCap = 6;
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
            hitEffect = WHFx.hitSpark(20, WHItems.refineCeramite.color.cpy(), 3, 20, 1f, 5);
            despawnEffect = new MultiEffect(WHFx.instHit(WHItems.refineCeramite.color.cpy(), true, 2, 18), Fx.hitBulletColor, Fx.hitLancer);

        }};

        BladeMnSteel = new CritBulletType(5, 6, "bullet"){{
            reloadMultiplier = 0.8f;
            ammoMultiplier = 2f;

            critChance = 0.1f;
            critMultiplier = 1.3f;

            shootEffect = Fx.shootSmallColor;
            width = 12f;
            height = width * 2;
            trailLength = 0;
            spin = 1.5f;
            lightningColor = backColor = trailColor = hitColor = WHItems.manganeseSteel.color.cpy();
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);
            hitEffect = new MultiEffect(
            WHFx.generalExplosion(30, hitColor, 20, 5, false),
            WHFx.hitSpark(30, hitColor, 8, 40, 1, 5)
            );

            critEffect = smokeTrail;

            fragBullet = new CritBulletType(3f, 12, "bullet"){{
                width = 5f;
                height = 12f;
                shrinkY = 1f;
                lifetime = 20f;
                lightningColor = backColor = trailColor = hitColor = WHItems.manganeseSteel.color.cpy();
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);
                despawnEffect = Fx.none;
            }};
            fragBullets = 4;
            despawnEffect = Fx.hitBulletColor;
        }};

        BladePlastanium = new CritBulletType(4f, 6, "bullet"){{
            reloadMultiplier = 1.5f;
            ammoMultiplier = 3f;

            critChance = 0.1f;
            critMultiplier = 1.3f;


            shootEffect = Fx.shootSmallColor;
            width = 12f;
            height = width * 2;
            trailLength = 0;
            spin = -1.5f;
            lightningColor = backColor = trailColor = hitColor = Pal.plastaniumBack.cpy();
            frontColor = Pal.plastaniumFront;
            hitEffect = new MultiEffect(
            WHFx.generalExplosion(30, hitColor, 40, 5, false),
            WHFx.hitSpark(30, hitColor, 8, 40, 1, 5)
            );
            splashDamage = 40f;
            splashDamageRadius = 40;

            critEffect = smokeTrail;

            fragBullet = new CritBulletType(2.5f, 20, "bullet"){{
                critMultiplier = 1.3f;
                critChance = 0.05f;

                width = 10f;
                height = 12f;
                shrinkY = 1f;
                lifetime = 15f;
                lightningColor = backColor = trailColor = hitColor = Pal.plastaniumBack.cpy();
                frontColor = Pal.plastaniumFront;
                despawnEffect = Fx.none;
            }};
            fragBullets = 4;
            despawnEffect = Fx.hitBulletColor;
        }};

        BladeCarbide = new CritBulletType(4f, 6, "bullet"){{
            ammoMultiplier = 4f;
            reloadMultiplier = 0.5f;

            critChance = 0.1f;
            critMultiplier = 1.3f;

            pierceCap = 2;

            shootEffect = Fx.shootBigColor;
            width = 12f;
            height = width * 2;
            trailLength = 0;
            spin = -1f;
            lightningColor = backColor = trailColor = hitColor = Color.valueOf("ab8ec5").cpy();
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);
            hitEffect = new MultiEffect(
            WHFx.generalExplosion(30, hitColor, 40, 5, false),
            WHFx.hitSpark(30, hitColor, 8, 40, 1, 5),
            WHFx.trailHitSpark(30, hitColor, 8, 40, 1.3f, 8)
            );
            splashDamage = 80;
            splashDamageRadius = 40;
            trailEffect = Fx.disperseTrail;
            trailRotation = true;
            trailInterval = 2f;

            critEffect = smokeTrail;

            fragBullet = new CritBulletType(6, 30, "bullet"){{
                critMultiplier = 1.3f;
                critChance = 0.05f;

                width = 11f;
                height = 14f;
                shrinkY = 1f;
                lifetime = 50 / speed;
                pierceCap = 2;
                lightningColor = backColor = trailColor = hitColor = Color.valueOf("ab8ec5").cpy();
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                trailEffect = Fx.disperseTrail;
                trailInterval = 2f;
                trailRotation = true;
                trailWidth = 1.8f;
                trailLength = 11;

                despawnEffect = Fx.hitBulletColor;
            }};
            fragBullets = 4;
            despawnEffect = Fx.hitBulletColor;
        }};

        BladeCeramite = new CritBulletType(4f, 50, "bullet"){{
            reloadMultiplier = 1.3f;
            ammoMultiplier = 3f;

            critChance = 0.1f;
            critMultiplier = 1.3f;

            shootEffect = Fx.shootSmallColor;
            width = 12f;
            height = width * 2;
            trailLength = 0;
            spin = 1f;
            lightningColor = backColor = trailColor = hitColor = WHItems.ceramite.color.cpy();
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);

            splashDamage = 80;
            splashDamageRadius = 50;

            hitEffect = new MultiEffect(
            WHFx.generalExplosion(10, hitColor, splashDamageRadius, 5, true),
            WHFx.hitSpark(30, hitColor, 8, splashDamageRadius, 1.5f, 8),
            WHFx.trailCircleHitSpark(30, hitColor, 8, splashDamageRadius, 1, 10),
            WHFx.square(60, hitColor, 10, splashDamageRadius, 4)
            );

            critEffect = smokeTrail;

            despawnEffect = Fx.hitBulletColor;
        }};

        PreventChromium = new CritBulletType(7, 100){{
            critChance = 0.15f;
            critMultiplier = 1.4f;
            buildingDamageMultiplier = 0.3f;
            armorMultiplier = 1.2f;

            lifetime = 316 / speed;
            hitColor = backColor = trailColor = WHItems.chromium.color.cpy();
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);

            width = 13f;
            height = width * 2f;
            trailWidth = width / 4.5f;
            trailLength = 5;

            pierceCap = 2;
            shootEffect = Fx.shootBigColor;
            smokeEffect = Fx.shootBigSmoke;
            critEffect = smokeTrail;
            trailEffect = WHFx.square(30, hitColor, 2, 10, 5);
            trailChance = 0.08f;
            hitEffect = despawnEffect = new MultiEffect(
            Fx.hitBulletColor,
            WHFx.square(30, hitColor, 6, 30, 4)
            );
        }};

        PreventTungsten = new CritBulletType(8, 150){{
            ammoMultiplier = 3;
            reloadMultiplier = 1.25f;
            armorMultiplier = 0.8f;

            critChance = 0.1f;
            critMultiplier = 1.4f;
            buildingDamageMultiplier = 0.3f;

            lifetime = 316 / speed;
            Color c = hitColor = backColor = trailColor = Items.tungsten.color.cpy();
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);

            width = 13f;
            height = width * 2f;
            trailWidth = width / 4.5f;
            trailLength = 5;

            pierceCap = 2;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootBigSmoke;

            status = WHStatusEffects.rock;
            statusDuration = 15f;

            critEffect = Fx.disperseTrail;
            trailRotation = true;
            hitEffect = despawnEffect = Fx.hitBulletColor;
        }};

        PreventUranium = new CritBulletType(8, 170){
            {
                ammoMultiplier = 4;
                armorMultiplier = 0.8f;

                critChance = 0.1f;
                critMultiplier = 1.4f;
                buildingDamageMultiplier = 0.3f;

                lifetime = 300 / speed;
                backColor = WHItems.uranium.color.cpy();
                Color c = hitColor = trailColor = backColor.cpy().lerp(Color.white, 0.3f);
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                width = 13f;
                height = width * 2f;
                trailWidth = width / 4.5f;
                trailLength = 5;

                pierceCap = 2;
                status = WHStatusEffects.radiation;
                statusDuration = 20f;
                shootEffect = Fx.shootBigColor;
                smokeEffect = Fx.shootBigSmoke;
                critEffect = smokeTrail;

            /*trailEffect = WHFx.sineTrail(c, 18, 1.6f, 7f, 0.55f, 24f);
            trailInterval = 0.6f;
            trailChance = 1f;
            trailRotation = true;*/

                fragOnHit = false;
                fragRandomSpread = 0f;
                fragBullets = 1;
                fragBullet = new ShrapnelBulletType(){{
                    damage = 40;
                    length = 50;
                    width = 12;
                    toColor = c;
                    pierceCap = 3;
                    serrations = 2;
                    serrationSpaceOffset = 10f;
                }};

                hitEffect = despawnEffect = new MultiEffect(
                Fx.hitBulletColor,
                WHFx.square(30, hitColor, 6, 30, 4));
            }

            @Override
            public void init(Bullet b){
                super.init(b);
                WHFx.sineTrail(30, hitColor, 18, 100, 1.6f, 5, 5)
                .at(b.x, b.y, b.rotation(), hitColor);
            }

        };

        PreventCarbide = new CritBulletType(12, 150){
            {
                reloadMultiplier = 0.5f;
                critChance = 0.1f;
                critMultiplier = 2f;
                buildingDamageMultiplier = 0.3f;
                armorMultiplier = 1.2f;

                lifetime = 316 / speed;
                Color c = hitColor = backColor = trailColor = Color.valueOf("ab8ec5");
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                width = 13f;
                height = width * 2f;
                trailWidth = width / 4.5f;
                trailLength = 5;
                pierceCap = 2;

                fragBullets = 3;
                fragRandomSpread = 120;
                fragBullet = new BasicBulletType(7, 100){{
                    armorMultiplier = 1.2f;
                    lifetime = 12f;
                    width = 11f;
                    height = 14f;
                    hitSize = 7f;
                    pierceCap = 2;
                    hitColor = backColor = trailColor = c;
                    frontColor = Color.white;
                    trailWidth = 1.8f;
                    trailLength = 5;
                    trailEffect = Fx.disperseTrail;
                    trailInterval = 2;
                    trailRotation = true;
                    hitEffect = despawnEffect = Fx.hitBulletColor;
                    buildingDamageMultiplier = 0.2f;
                }};

                trailRotation = true;
                shootEffect = Fx.shootBig;
                smokeEffect = Fx.shootBigSmoke;
                critEffect = Fx.disperseTrail;
                trailEffect = WHFx.square(30, hitColor, 1, 10, 3);
                trailChance = 0.8f;
                hitEffect = despawnEffect = Fx.hitBulletColor;
            }
        };

        PreventArmorAlloy = new CritBulletType(8, 200){{
            ammoMultiplier = 3;
            reloadMultiplier = 0.8f;
            armorMultiplier = 0.5f;

            critChance = 0.18f;
            critMultiplier = 1.4f;
            buildingDamageMultiplier = 0.3f;

            lifetime = 316 / speed;
            Color c = hitColor = backColor = trailColor = WHItems.armorAlloy.color.lerp(Color.white, 0.1f).cpy();
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);

            width = 13f;
            height = width * 2f;
            trailWidth = width / 4.5f;
            trailLength = 5;

            pierceCap = 3;
            status = WHStatusEffects.armorFracture;
            statusDuration = 240;
            shootEffect = Fx.shootBigColor;
            smokeEffect = Fx.shootBigSmoke;
            critEffect = WHFx.square(15, hitColor, 1, 10, 4);

            hitEffect = new MultiEffect(
            Fx.hitBulletColor,
            WHFx.instHit(hitColor, true, 3, 20)
            );

            trailEffect = WHFx.square(30, hitColor, 2, 15, 5);
            trailChance = 0.08f;

            despawnEffect = new MultiEffect(
            WHFx.hitSparkAng(60, Pal.bulletYellowBack, hitColor, 8, 30, 45, 2, 8f),
            WHFx.trailHitSpark(30, hitColor, 4, 40, 1.5f, 7f),
            WHFx.square(30, hitColor, 6, 30, 4)
            );
        }};

        PreventCombustible = new CritBulletType(8, 150){{
            ammoMultiplier = 5;
            armorMultiplier = 0.7f;

            critChance = 0.08f;
            critMultiplier = 1.2f;
            buildingDamageMultiplier = 0.3f;

            lifetime = 316 / speed;
            Color c = hitColor = backColor = trailColor = WHItems.combustible.color.cpy();
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);

            width = 13f;
            height = width * 2f;
            trailWidth = width / 4.5f;
            trailLength = 5;

            splashDamage = damage * 0.7f;
            splashDamageRadius = 40f;
            incendAmount = 2;
            incendChance = 0.3f;
            incendSpread = 24f;

            trailEffect = Fx.ballfire;
            trailRotation = true;
            trailChance = 0.8f;

            pierceCap = 2;
            status = StatusEffects.blasted;
            statusDuration = 60;
            shootEffect = Fx.shootBigColor;
            smokeEffect = Fx.shootBigSmoke;
            critEffect = smokeTrail;

            fragBullet = new FireBulletType(8, 50){{
                fireTrailChance = 0.01f;
                pierceCap = 2;
            }};
            fragBullets = 3;

            hitEffect = despawnEffect = new MultiEffect(
            Fx.hitBulletColor,
            WHFx.generalExplosion(20, hitColor, splashDamageRadius, 10, true),
            WHFx.trailCircleHitSpark(20, hitColor, 4, splashDamageRadius, 1.2f, 7f),
            WHFx.hitCircle(30, frontColor, backColor, 6, splashDamageRadius, 5)
            );
        }};


        HeavyHammerUranium = new CritMissileBulletType(){{
            reloadMultiplier = 1.2f;
            ammoMultiplier = 2f;

            critChance = 0.15f;
            critMultiplier = 1.5f;
            sprite = "shell";

            lengthOffset = 5;
            flameLength = 18f;
            flameWidth = 2f;
            despawnEffect = Fx.none;
            knockback = 3f;
            speed = 5f;
            height = 28f;
            width = 15f;

            damage = 300f;
            splashDamageRadius = 50;
            splashDamage = 500;
            collidesTiles = false;
            collides = false;
            collidesAir = false;
            scaleLife = true;

            scaledSplashDamage = true;
            hitSound = explosionTitan;

            backColor = WHItems.uranium.color.cpy();

            Color c = hitColor = trailColor = backColor.cpy().lerp(Color.white, 0.3f);
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);
            colors = new Color[]{c.cpy().a(0.4f), c.cpy().a(0.8f), c.cpy().lerp(Color.white, 0.8f)};
            hitEffect = new MultiEffect(
            WHFx.circleOut(c, splashDamageRadius * 1.5f),
            Fx.titanExplosion, Fx.titanSmoke,
            WHFx.hitSpark(90, hitColor, 20, splashDamageRadius, 1.5f, 12),
            WHFx.spreadOutSpark(240, splashDamageRadius * 1.5f, 20, 4, 30, 25, 4, Interp.pow2Out)
            );
            shootEffect = Fx.shootTitan;
            smokeEffect = Fx.shootSmokeTitan;

            status = WHStatusEffects.radiation;
            statusDuration = 90;

            trailLength = 32;
            trailWidth = 3.35f;
            trailSinScl = 2.5f;
            trailSinMag = 0.5f;
            trailEffect = Fx.disperseTrail;
            trailInterval = 2f;
            despawnShake = 7f;
            trailRotation = true;

            trailInterp = v -> Math.max(Mathf.slope(v), 0.8f);
            shrinkX = 0.2f;
            shrinkY = 0.1f;
            buildingDamageMultiplier = 0.5f;
            fragLifeMin = 1.5f;
            fragBullets = 3;
            fragBullet = new CritBulletType(1, 90, "shell"){{
                armorMultiplier = 1.2f;
                collidesAir = false;

                despawnShake = 3f;
                width = 8f;
                height = 12f;
                lifetime = 50f;
                knockback = 0.5f;
                shrinkY = 0.3f;
                splashDamageRadius = 24;
                splashDamage = 50f;
                scaledSplashDamage = true;
                backColor = WHItems.uranium.color.cpy();

                Color c = hitColor = trailColor = backColor.cpy().lerp(Color.white, 0.3f);
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);
                hitEffect = new MultiEffect(Fx.titanExplosionFrag, Fx.titanLightSmall, new WaveEffect(){{
                    lifetime = 15f;
                    strokeFrom = 1f;
                    sizeTo = 8f;
                }});
                despawnEffect = Fx.hitBulletColor;
                buildingDamageMultiplier = 0.3f;
            }};
        }};

        HeavyHammerCeramite = new CritMissileBulletType(){{
            ammoMultiplier = 2f;
            reloadMultiplier = 0.9f;

            critChance = 0.15f;
            critMultiplier = 1.2f;
            sprite = "shell";

            lengthOffset = 5;
            flameLength = 18f;
            flameWidth = 2f;
            despawnEffect = Fx.none;
            knockback = 3f;
            speed = 4f;
            height = 28f;
            width = 15f;

            damage = 200;
            splashDamageRadius = 70;
            splashDamage = 300;
            collidesTiles = false;
            collides = false;
            collidesAir = false;
            scaleLife = true;

            scaledSplashDamage = true;
            hitSound = explosionTitan;

            backColor = WHItems.ceramite.color.cpy();

            Color c = hitColor = trailColor = backColor.cpy();
            frontColor = backColor.cpy().lerp(Color.white, 0.5f);
            colors = new Color[]{c.cpy().a(0.4f), c.cpy().a(0.8f), c.cpy().lerp(Color.white, 0.8f)};
            hitEffect = new MultiEffect(
            WHFx.circleOut(c, splashDamageRadius), Fx.titanSmoke,
            WHFx.square(120, hitColor, 20, splashDamageRadius, 6),
            WHFx.trailCircleHitSpark(90, hitColor, 20, splashDamageRadius, 1.5f, 12),
            WHFx.generalExplosion(60, hitColor, splashDamageRadius, 10, true)
            );
            shootEffect = Fx.shootTitan;
            smokeEffect = Fx.shootSmokeTitan;

            trailLength = 32;
            trailWidth = 3.35f;
            trailSinScl = 2.5f;
            trailSinMag = 0.5f;
            trailEffect = smokeTrail;
            trailInterval = 2f;
            despawnShake = 7f;
            trailRotation = true;

            trailInterp = v -> Math.max(Mathf.slope(v), 0.8f);
            shrinkX = 0.2f;
            shrinkY = 0.1f;
            buildingDamageMultiplier = 0.5f;
            fragLifeMin = 1.5f;

            fragBullets = 6;
            fragBullet = new CritBulletType(1.5f, 100, "shell"){{
                armorMultiplier = 2;
                splashDamageRadius = 40;
                splashDamage = 50f;
                scaledSplashDamage = true;

                collidesAir = false;
                collidesTiles = false;
                despawnEffect = Fx.hitBulletColor;
                width = 8f;
                height = 12f;
                lifetime = 50f;
                knockback = 0.5f;

                hitColor = trailColor = backColor = WHItems.ceramite.color.cpy();
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);
                hitEffect = new MultiEffect(
                WHFx.square(40, hitColor, 6, splashDamageRadius, 4),
                WHFx.hitSpark(40, hitColor, 10, splashDamageRadius, 1.5f, 7),
                WHFx.generalExplosion(20, hitColor, splashDamageRadius, 10, false)
                );

                buildingDamageMultiplier = 0.3f;
                shrinkY = 0.3f;
            }};
        }};

        HeavyHammerMolybdenumAlloy = new ShieldBreakerType(5, 600, "missile-large", 300){{
            ammoMultiplier = 3f;
            reloadMultiplier = 1.2f;
            armorMultiplier = -1;

            despawnEffect = new MultiEffect(WHFx.instHit(WHItems.molybdenumAlloy.color.cpy(), true, 4, 30),
            WHFx.hitSpark(20, WHItems.molybdenumAlloy.color.cpy(), 5, 30, 1f, 8));
            hitEffect = Fx.titanExplosionSmall;
            knockback = 3f;
            drag = -0.03f;
            speed = 5f;
            rangeChange = 6 * 8f;
            lifetime = 40.06f;
            height = 40f;
            width = 15f;

            pierce = true;
            pierceCap = 2;
            collidesAir = false;
            backColor = hitColor = trailColor = WHItems.molybdenumAlloy.color.cpy();
            hitSound = explosionTitan;

            status = WHStatusEffects.tear;
            statusDuration = 90f;

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
            fragBullet = new CritBulletType(10f, 150, name("pierce")){{
                despawnEffect = hitEffect = new MultiEffect(hitSpark(20, WHItems.molybdenumAlloy.color.cpy(), 5, 30, 1f, 8),
                WHFx.instHit(WHItems.molybdenumAlloy.color.cpy(), true, 2, 10));
                critChance = 0.25f;
                critMultiplier = 3f;
                width = 15;
                height = 60f;
                lifetime = (50 + 6 * 8) / speed;
                trailRotation = true;
                pierceCap = 2;
                trailEffect = Fx.disperseTrail;
                trailChance = 0.8f;
                backColor = hitColor = trailColor = WHItems.molybdenumAlloy.color.cpy();
                trailLength = 8;
                trailWidth = 3;

                status = WHStatusEffects.tear;
                statusDuration = 20f;

                fragBullets = 1;
                fragRandomSpread = fragAngle = 0;
                fragBullet = new LaserBulletType(110){
                    {
                        pierceCap = 3;
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

       /* IonizeEntanglementBullet = new MultiTrailBulletType(){{
            sprite = name("energy-bullet");
            subTrailWidth = 2;

            float rad = 70;
            Color c = backColor = hitColor = trailColor = WHItems.entanglement.color.cpy();
            frontColor = backColor.cpy().lerp(Color.white, 0.7f);

            hitEffect = despawnEffect = new MultiEffect(
            WHFx.instHit(hitColor, true, 4, 50),
            WHFx.hitSpark(hitColor, 90, 15, rad, 1f, 8),
            WHFx.circleOut(60, rad, 2f),
            WHFx.smoothColorCircle(hitColor, rad, 130),
            WHFx.crossBlastArrow45(frontColor, backColor, 130, 15f, 40, rad, 0));

            knockback = 3f;
            drag = -0.03f;
            speed = 4f;
            lifetime = 46.8f;
            height = 40f;
            width = 10f;
            damage = 200;
            hittable = false;

            hitSound = explosionTitan;

            status = WHStatusEffects.scare;
            statusDuration = 30;

            chargeEffect = new MultiEffect(
            WHFx.genericChargeCircle(hitColor, 5, 30, 120),
            WHFx.trailCharge(hitColor, 20, 1.5f, 30, 3, 120)).followParent(true);

            trailLength = 10;
            trailWidth = 3f;
            trailInterval = 1f;
            trailEffect = WHFx.square(hitColor, 20f, 1, 25, 4);

            shootEffect = Fx.shootTitan;
            smokeEffect = Fx.shootSmokeTitan;
            trailRotation = true;

            trailInterp = v -> Math.max(Mathf.slope(v), 0.8f);
            fragBullets = 1;
            fragRandomSpread = fragAngle = 0;
            fragBullet = new DOTBulletType(){{
                lifetime = 120;
                damageInterval = 6;
                DOTDamage = damage = 150;
                DOTRadius = rad;
                radIncrease = 3;
                sprite = "large-orb";
                effect = WHStatusEffects.powerReduce1;
                statusDuration = 60f;
                hitColor = lightColor = lightningColor = c;
                fx = WHFx.square(hitColor, 20, 2, 15, 5);
                armorMultiplier = 0.8f;
            }};
        }

            @Override
            public void update(Bullet b){
                super.update(b);
                if(Mathf.chanceDelta(0.15f) && b.fin() < 0.7f){
                    WHFx.sineTrail(hitColor, 20, 100, 1.5f, 5, 5, 30)
                    .at(b.x, b.y, b.rotation(), hitColor);
                }
            }
        };*/
        IonizeEntanglementBullet = new CritBulletType(3, 150, "large-orb"){
            {
                critChance = 0.1f;
                critMultiplier = 2f;
                buildingDamageMultiplier = 0.3f;
                armorMultiplier = 1.2f;
                bouncing = true;
                hittable = false;

                lifetime = 400 / speed;
                weaveMag = 1;
                weaveScale = 5;
                Color c = backColor = hitColor = trailColor = WHItems.entanglement.color.cpy().lerp(Pal.techBlue, 0.5f);
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                chargeEffect = new MultiEffect(
                WHFx.genericChargeCircle(120, hitColor, 5, 30),
                WHFx.trailCharge(120, hitColor, 20, 1.5f, 30, 3),
                WHFx.trailCharge2(120, hitColor, 15, 1.5f, 60, 5),
                WHFx.trailCharge2(60, hitColor, 15, 1.5f, 40, 5).startDelay(60)).followParent(true);

                height = width = 18;
                pierceCap = 3;
                shrinkX = shrinkY = 0;

                intervalBullet = new LightningBulletType(){{
                    damage = 50;
                    lightningColor = c;
                    lightningLength = 5;
                    lightningLengthRand = 5;
                    buildingDamageMultiplier = 0.25f;
                }};

                bulletInterval = 3f;

                status = WHStatusEffects.plasma;
                statusDuration = 30;

                fragBullets = 4;
                fragBullet = new CritBulletType(3, 100){{
                    armorMultiplier = 2;
                    bouncing = true;

                    lifetime = 40;
                    width = 11f;
                    height = 14f;
                    hitSize = 7f;
                    pierceCap = 3;
                    lightningColor = hitColor = backColor = trailColor = c;
                    frontColor = backColor.cpy().lerp(Color.white, 0.5f);
                    trailWidth = 1.8f;
                    trailLength = 5;
                    trailEffect = WHFx.sineTrail(30, hitColor, 10, 50, 1.5f, 5, 2);

                    lightningDamage = 30;
                    lightningLength = 10;
                    lightning = 1;
                    lightningType = new CritBulletType(0.0001f, 0f){{
                        armorMultiplier = 0.5f;
                        lifetime = Fx.lightning.lifetime;
                        hitEffect = Fx.hitLancer;
                        despawnEffect = Fx.none;
                        status = StatusEffects.shocked;
                        statusDuration = 10f;
                        hittable = false;
                        lightningColor = lightColor = hitColor = WHItems.entanglement.color.cpy();
                        ;
                        buildingDamageMultiplier = 0.25f;
                    }};

                    trailInterval = 6;
                    trailRotation = true;
                    rotateSpeed = 2;
                    homingPower = 0.05f;
                    followAimSpeed = 2;
                    hitEffect = Fx.hitBulletColor;
                    despawnEffect = new MultiEffect(
                    WHFx.instRotation(60, hitColor, 30, 90, false),
                    WHFx.lineCircleOut(60, hitColor, 30, 2)
                    );
                    buildingDamageMultiplier = 0.2f;
                }};

                homingPower = 0.04f;
                trailRotation = true;
                shootEffect = Fx.shootBig;
                smokeEffect = Fx.shootBigSmoke;
                trailSpread = 8;
                trailEffect = new MultiEffect(
                WHFx.sineTrail(90, hitColor, 30, 150, 1.5f, 0.75f, 3),
                new Effect(50, e -> {
                    color(e.color);
                    Fill.circle(e.x, e.y, 3 * e.fout());
                }).layer(Layer.bullet - 0.001f)
                );
                trailChance = 0.11f;
                trailInterval = 6;
                hitEffect = despawnEffect = new MultiEffect(
                WHFx.instRotation(60, hitColor, 40, 90, false),
                WHFx.square(60, hitColor, 10, 40, 3),
                WHFx.generalExplosion(30, hitColor, 40, 10, true)
                );
            }

            @Override
            public void updateTrailEffects(Bullet b){
                if(b.fin() < 0.8f) super.updateTrailEffects(b);
            }
        };

        IonizeResonantCrystalBullet = new LightningLinkerBulletType(5, 200){{

            randomLightningChance = 0.1f;
            randomLightningNum = 1;
            linkLightingDamage = 100;
            hitSpacing = 5;
            maxHit = 2;
            linkRange = 80;
            scaleLife = despawnHit = false;

            sprite = name("energy-bullet");
            fragBullet = intervalBullet = WHBulletsOther.IonizeInterval;
            intervalBullets = 2;
            bulletInterval = 6;
            intervalDelay = 30;
            intervalRandomSpread = 0;
            intervalSpread = 180;
            collides = true;
            pierceCap = 4;
            fragOffsetMin = fragOffsetMax = 20;
            fragBullets = 4;
            fragSpread = 90;
            fragRandomSpread = 0;

            status = WHStatusEffects.plasma;
            statusDuration = 30;

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
            lightningLength = 7;
            lightningLengthRand = 6;
            splashDamage = 300;
            splashDamageRadius = 64;
            reloadMultiplier = 0.5f;
            lightningColor = trailColor = backColor = lightColor = hitColor = WHItems.resonantCrystal.color.cpy().lerp(Pal.techBlue, 0.7f);
            trailEffect = WHFx.square(20f, hitColor, 1, 25, 4);
            trailLength = 10;
            trailWidth = 3f;
            trailInterval = 1f;

            shootEffect = new MultiEffect(
            WHFx.instShoot(hitColor, hitColor),
            WHFx.shootLine(40, 20)
            );

            chargeEffect = new MultiEffect(
            WHFx.genericChargeCircle(120, hitColor, 5, 60),
            WHFx.trailCharge(120, hitColor, 20, 1.7f, 30, 3),
            WHFx.trailCharge2(120, hitColor, 15, 1.5f, 80, 5),
            WHFx.trailCharge2(60, hitColor, 15, 1.5f, 80, 5).startDelay(60));

            hitEffect = new MultiEffect(
            WHFx.hitSparkAng(30, Pal.lighterOrange, hitColor, 10, 30, 30, 2.2f, 10f),
            WHFx.hitSpark(60, hitColor, 15, 30, 2f, 8),
            WHFx.circleOut(60, 40, 2f),
            WHFx.generalExplosion(15, hitColor, 40, 0, false)
            );
            despawnEffect = new MultiEffect(
            WHFx.sharpBlast(120, hitColor, hitColor, 60),
            WHFx.circleOut(60, hitColor, 80).startDelay(15),
            WHFx.fillCircle(60, hitColor, 10, Interp.pow3Out));
        }};

        ViperBullet = new MultiBulletType(
        ViperBulletComp,
        ViperBulletMain,
        ViperBulletComp){
            {
                ammoMultiplier = 1;
                angleOffset = 15f;

                lightningColor = trailColor = lightColor = hitColor = WHPal.MnSteelColor.cpy().lerp(Pal.techBlue.cpy(), 0.2f);
                trailEffect = WHFx.square(20f, hitColor, 1, 25, 4);

                shootEffect = new MultiEffect(
                WHFx.instShoot(hitColor, hitColor));

                chargeEffect = new MultiEffect(
                WHFx.lineCircleIn(30, hitColor, 30, 2).startDelay(30),
                WHFx.genericChargeCircle(60, hitColor, 5, 60),
                WHFx.trailCharge(60, hitColor, 20, 1.7f, 30, 3),
                WHFx.trailCharge2(60, hitColor, 15, 1.5f, 80, 5)
                ).followParent(true);
            }

            @Override
            public Bullet create(Entityc owner, Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, Mover mover, float aimX, float aimY, Teamc target){
                Bullet last = null;
                float startOffset = -((bullets.length - 1f) * angleOffset) / 2f;

                for(int i = 0; i < repeat; i++){
                    for(int b = 0; b < bullets.length; b++){
                        BulletType bullet = bullets[b];
                        float shotAngle = angle + startOffset + b * angleOffset;
                        last = bullet.create(owner, shooter, team, x, y, shotAngle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
                    }
                }

                return last;
            }
        };


        ViperBulletEnhance = new MultiBulletType(
        ViperBulletComp,
        ViperBulletMain2,
        ViperBulletComp){
            {
                ammoMultiplier = 1;
                angleOffset = 15f;

                float length = 420;
                rangeChange = length - 360;

                lightningColor = trailColor = lightColor = hitColor = WHPal.MnSteelColor.cpy().lerp(Pal.techBlue.cpy(), 0.2f);
                trailEffect = WHFx.square(20f, hitColor, 1, 25, 4);

                shootEffect = new MultiEffect(
                WHFx.instShoot(hitColor, hitColor));

                chargeEffect = new MultiEffect(
                WHFx.lineCircleIn(30, hitColor, 30, 2).startDelay(30),
                WHFx.genericChargeCircle(60, hitColor, 5, 60),
                WHFx.trailCharge(60, hitColor, 20, 1.7f, 30, 3),
                WHFx.trailCharge2(60, hitColor, 15, 1.5f, 80, 5),
                WHFx.convergeSpinLines(60, hitColor, 3, 5
                , 200, 4, 15, 2)
                ).followParent(true);
            }

            @Override
            public Bullet create(Entityc owner, Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, Mover mover, float aimX, float aimY, Teamc target){
                Bullet last = null;
                float startOffset = -((bullets.length - 1f) * angleOffset) / 2f;

                for(int i = 0; i < repeat; i++){
                    for(int b = 0; b < bullets.length; b++){
                        BulletType bullet = bullets[b];
                        float shotAngle = angle + startOffset + b * angleOffset;
                        last = bullet.create(owner, shooter, team, x, y, shotAngle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
                    }
                }

                return last;
            }
        };

        PyrosBullet = new HeatBulletType(){
            {
                shootPattern = new ShootPattern(){{
                    firstShotDelay = 60f;
                    shots = 2;
                    shotDelay = 30f;
                }};

                sprite = "large-orb";
                hittable = false;
                armorMultiplier = 0.5f;
                damage = 100;
                speed = 3f;
                lifetime = 400 / 3f;
                splashDamage = 150;
                splashDamageRadius = 64;
                scaledSplashDamage = true;
                sticky = true;
                height = width = 25;
                shrinkX = shrinkY = 0;
                frontColor = backColor = lightningColor = lightColor =
                hitColor = trailColor = Pal.slagOrange.cpy().lerp(Pal.lightOrange, 0.25f);
                trailWidth = 4;
                trailSpread = 5;
                trailInterval = 1;
                trailChance = 0.3f;
                trailLength = 8;
                puddleAmount = 10;

                chargeEffect = new MultiEffect(
                WHFx.genericChargeCircle(60, hitColor, 6, 60),
                WHFx.lineCircleIn(30, hitColor, 50, 2f),
                WHFx.trailCharge2(60, hitColor, 12, 1.7f, 60, 10)
                ).rotWithParent(true);

                hitEffect = despawnEffect = new MultiEffect(
                WHFx.lineCircleOut(90, hitColor, splashDamageRadius, 2f),
                WHFx.hitSpark(120, hitColor, 15, splashDamageRadius, 1f, 8),
                WHFx.square(90, hitColor, 18, splashDamageRadius, 5),
                WHFx.fillCircle(90, hitColor, 10, Interp.pow3Out),
                WHFx.generalExplosion(30, hitColor, splashDamageRadius, 0, false));

                smokeEffect = Fx.shootBigSmoke;
                shootEffect = new MultiEffect(
                WHFx.shootLine(50, 45),
                Fx.shootBigColor
                );

                fragBullets = 5;
                fragLifeMin = 0.3f;
                fragLifeMax = 1f;
                fragVelocityMin = 0.3f;
                fragVelocityMax = 1f;
                fragBullet = WHBulletsOther.PyrosBulletFrag;
            }

            @Override
            public void updateTrailEffects(Bullet b){
                super.updateTrailEffects(b);
                if(Mathf.chanceDelta(0.05f) && b.fin() < 0.7)
                    WHFx.sineTrail(90, hitColor, 30, 150, 1.5f, 0.75f, 3).at(
                    b.x, b.y, b.rotation(), hitColor);
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(entity instanceof Unit u && u.type != null){
                    if(u.hasEffect(WHStatusEffects.armorFracture)){
                        float dmg = b.damage * (1 + Mathf.clamp(u.armor / 4, 1, 5));
                        u.damagePierce(dmg);
                        if(u.health > b.damage) u.health(u.health - b.damage);
                    }
                }
            }
        };

        PyrosBulletEnhance1 = new HeatBulletType(){
            {
              /*  shootPattern= new ShootHelix(){{
                    shots=1;
                    firstShotDelay=60f;
                    scl=3;mag=1.5f;
                }};*/

                reloadMultiplier = 0.75f;
                sprite = "large-orb";
                hittable = false;
                damage = 150;
                speed = 2.5f;
                lifetime = 400 / speed;
                splashDamage = 200;
                splashDamageRadius = 70;
                scaledSplashDamage = true;

                height = width = 30;
                shrinkX = shrinkY = 0;
                frontColor = backColor = lightningColor = lightColor =
                hitColor = trailColor = Pal.slagOrange.cpy().lerp(Pal.lightOrange, 0.25f);
                trailWidth = 4;
                trailSpread = 5;
                trailInterval = 1;
                trailChance = 0.3f;
                trailLength = 8;
                puddleAmount = 10;

                chargeEffect = new MultiEffect(
                WHFx.genericChargeCircle(60, hitColor, 6, 60),
                WHFx.lineCircleIn(30, hitColor, 50, 2f),
                WHFx.trailCharge2(60, hitColor, 12, 1.7f, 60, 10),
                WHFx.trailCharge2(30, hitColor, 12, 1.7f, 60, 10).startDelay(30)
                ).rotWithParent(true);

                hitEffect = despawnEffect = new MultiEffect(
                WHFx.lineCircleOut(90, hitColor, 50, 2f),
                WHFx.hitSpark(120, hitColor, 15, 40, 1f, 8),
                WHFx.square(90, hitColor, 18, 60, 5),
                WHFx.fillCircle(90, hitColor, 10, Interp.pow3Out),
                WHFx.subEffect(140, splashDamageRadius, 8, 34f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
                    Draw.color(hitColor);
                    float fout = Interp.pow2Out.apply(1 - fin);
                    for(int s : Mathf.signs){
                        Drawf.tri(x, y, 8 * fout, 17 * Mathf.curve(fin, 0, 0.1f) * WHFx.fout(fin, 0.25f), rot + s * 90);
                    }
                })));

                smokeEffect = Fx.shootBigSmoke;
                shootEffect = new MultiEffect(
                WHFx.shoot3DWave(60, hitColor, 92, 14),
                WHFx.shootLine(50, 45),
                Fx.shootBigColor
                );

                fragBullets = 2;
                fragLifeMin = 0.7f;
                fragLifeMax = 1.5f;
                fragVelocityMin = 0.3f;
                fragVelocityMax = 1f;
                fragBullet = WHBulletsOther.PyrosBulletFrag;
                intervalBullet = PyrosBulletInterval;
                bulletInterval = lifetime + 1;
            }

            @Override
            public void updateTrailEffects(Bullet b){
                super.updateTrailEffects(b);
                if(Mathf.chanceDelta(0.05f) && b.fin() < 0.7){
                    WHFx.sineTrail(60, hitColor, 30, 150, 1.5f, 0.75f, 3)
                    .at(b.x, b.y, b.rotation(), hitColor);
                }
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(entity instanceof Unit u && u.type != null){
                    if(intervalBullet != null){
                        rand.setSeed(b.id);
                        float ang = rand.random(360);
                        for(int i = 0; i < 4; i++){
                            Tmp.v1.trns(ang, u.hitSize + 10);
                            intervalBullet.create(b, b.team, b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v1.angle());
                        }
                    }
                    if(u.hasEffect(WHStatusEffects.armorFracture)){
                        float dmg = b.damage * (1 + Mathf.clamp(u.armor / 10, 1, 4));
                        u.damagePierce(dmg);
                    }
                }
            }

            @Override
            public void update(Bullet b){
                super.update(b);
                if(b.timer.get(3, 20)){
                    Damage.damage(b.team, b.x, b.y, splashDamageRadius, splashDamage / 2);
                    Damage.status(b.team, b.x, b.y, splashDamageRadius, WHStatusEffects.melta, 60, true, true);
                    Damage.status(b.team, b.x, b.y, splashDamageRadius, StatusEffects.melting, 60, true, true);
                    MainRenderer.addShockCircle(b.x, b.y, splashDamageRadius, 20, 0.5f);
                }
                if(Mathf.chanceDelta(0.15)){
                    WHFx.tentacleCorona(25, 20, 10, 1, 2, hitColor, Pal.lightOrange)
                    .at(b.x, b.y, b.rotation(), hitColor, b);
                }

                if(Mathf.chanceDelta(0.08f) && b.fin() > 0.1){
                    Tmp.v1.rnd(Mathf.random(20));
                    WHFx.sineTrail(90, hitColor, 20, 50, 1.5f, 2, 3)
                    .rotWithParent(true).at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, b.rotation(), hitColor, b);
                }
            }

            @Override
            public void updateBulletInterval(Bullet b){
                if(intervalBullet != null && b.time >= intervalDelay && b.timer.get(2, bulletInterval)){
                    float ang = b.rotation();
                    for(int i = 0; i < intervalBullets; i++){
                        Tmp.v1.trns(ang, -10);
                        intervalBullet.create(b, b.team, b.x + Tmp.v1.x, b.y + Tmp.v1.y,
                        ang + Mathf.range(intervalRandomSpread) + intervalAngle + ((i - (intervalBullets - 1f) / 2f) * intervalSpread),
                        -1, 1, 1, b);
                    }
                }
            }
        };

        PyrosBulletEnhance1Main = PyrosBulletEnhance1.copy();
        PyrosBulletEnhance1Main.intervalBullets = 2;
        PyrosBulletEnhance1Main.intervalBullet = PyrosBulletInterval;
        PyrosBulletEnhance1Main.intervalDelay = 30;
        PyrosBulletEnhance1Main.bulletInterval = 10;
        PyrosBulletEnhance1Main.intervalAngle = 180f;
        PyrosBulletEnhance1Main.damage = 600;
        PyrosBulletEnhance1Main.splashDamage = 600;
        PyrosBulletEnhance1Main.splashDamageRadius = 70;
        PyrosBulletEnhance1Main.fragBullets = 3;

        PyrosBulletEnhance2 = new MultiBulletType(
        PyrosBulletEnhance1Main,
        PyrosBulletComp1,
        PyrosBulletComp2){
            {
           /* reloadMultiplier=0.5f;
            shootPattern=new ShootPattern(){{
                firstShotDelay= 60f;
                shots=2;
                shotDelay=30f;
            }};*/

                lightningColor = lightColor =
                hitColor = trailColor = Pal.slagOrange.cpy().lerp(Pal.lightOrange, 0.25f);

                chargeEffect = new MultiEffect(
                WHFx.genericChargeCircle(60, hitColor, 6, 60),
                WHFx.lineCircleIn(30, hitColor, 50, 2f),
                WHFx.trailCharge2(60, hitColor, 12, 1.7f, 60, 10),
                WHFx.trailCharge(30, hitColor, 12, 1.7f, 70, 4).startDelay(30)
                ).rotWithParent(true);

                smokeEffect = Fx.shootBigSmoke;
                shootEffect = new MultiEffect(
                WHFx.shoot3DWave(60, hitColor, 70, 20),
                WHFx.shootLine(50, 45),
                Fx.shootBigColor
                );
            }

            @Override
            public void init(Bullet b){
                super.init(b);
                MainRenderer.addShockCircle(b.x, b.y, 60, 50, 0.3f);
            }
        };

        CollapseResonantCrystal = new DelayedPointBulletType(){
            {
                Color c = hitColor = trailColor = lightningColor = WHItems.culverCrystal.color.cpy();
                colors = new Color[]{hitColor.cpy().mul(1f, 1f, 1f, 0.4f), hitColor.cpy(), hitColor.cpy(), Pal.coalBlack};
                reflectable = hittable = false;
                damage = 1;
                splashDamageRadius = 10 * tilesize;
                splashDamage = 100F;
                buildingDamageMultiplier = 0.2F;

                width = 50;
                delayEffectLifeTime = 60f;
                square = true;

                fragBullets = 1;
                fragRandomSpread = 0;

                chargeEffect = new MultiEffect(
                WHFx.trailCharge2(60, c, 12, 1.3f, 80, 4),
                WHFx.genericChargeCircle(60, c, 6, 80),
                lineCircleIn(30, c, 50, 2f).startDelay(10)
                );

                shootEffect = new MultiEffect(WHFx.shootLine(20, 30), WHFx.instShoot(c, c));

                hitEffect = despawnEffect = new MultiEffect(
                WHFx.lineCircleOut(180, c, splashDamageRadius, 5f),
                new Effect(140, e -> {
                    Draw.z(Layer.effect);
                    Draw.color(c);
                    float progress = Mathf.curve(Interp.pow5Out.apply(e.fin()), 0, 0.2f) * WHFx.fout(e.fin(), 0.1f);
                    Drawn.surround(e.id, e.x, e.y, 35, 8, 5, 8, progress);
                    Fill.circle(e.x, e.y, 16 * progress);
                    Draw.color(Pal.coalBlack.cpy());
                    Fill.circle(e.x, e.y, 9 * progress);
                }),
                WHFx.hitSpark(30, c, 20, splashDamageRadius, 1, 6),
                /*   WHFx.circleLightning(WHPal.WHYellow.cpy(), 120, 20, 12, splashDamageRadius),*/
                WHFx.sharpBlast(90, c, c, 60));
                fragBullet = new DOTBulletType(){
                    {
                        DOTDamage = damage = 150;
                        damageInterval = 15;
                        DOTRadius = 12 * tilesize;
                        radIncrease = 0.5f;
                        status = WHStatusEffects.palsy;
                        statusDuration = 180f;
                        effectTimer = 2f;
                        fx = WHFx.square(60, c, 1, 0, 5);
                        lightningColor = c;
                    }
                };
            }
        };

        CollapseCulverCrystal = new TrailFadeBulletType(){
            {
                sprite = name("energy-bullet");
                Color c = hitColor = backColor = trailColor = lightningColor = WHItems.culverCrystal.color.cpy();
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);
                reflectable = hittable = false;
                damage = 1;
                speed = 6;
                lifetime = 137.93f;
                drag = 0.01f;
                splashDamageRadius = 16 * tilesize;
                splashDamage = 100F;
                buildingDamageMultiplier = 0.2F;

                trailLength = 30;
                trailWidth = 3;
                trailSinScl = 2.5f;
                trailSinMag = 0.1f;

                intervalBullets = 4;
                intervalAngle = 45;
                intervalSpread = 90;
                intervalBullet = WHBulletsOther.CollapseSealedPromethiumInterval;

                height = 45;
                width = 15;

                despawnBlinkTrail = true;
                tracers = 2;
                tracerStroke = 2;
                tracerSpacing = 8;
                tracerRandX = 4;
                tracerFadeOffset = 4;
                tracerStrokeOffset = 9;
                tracerUpdateSpacing = 2;

                fragBullets = 1;
                fragRandomSpread = 0;

                chargeEffect = new MultiEffect(
                WHFx.trailCharge(30, c, 12, 1.3f, 100, 3),
                WHFx.trailCharge2(60, c, 12, 1.3f, 100, 3),
                WHFx.genericChargeCircle(60, c, 6, 80),
                lineCircleIn(30, c, 50, 2f).startDelay(10)
                );

                shootEffect = new MultiEffect(
                WHFx.shootLine(20, 30), WHFx.instShoot(c, c));

                hitEffect = despawnEffect = new MultiEffect(
                WHFx.lineCircleOut(130, c, splashDamageRadius, 5f),
                new Effect(200 + 30, e -> {
                    Draw.z(Layer.effect);
                    Draw.color(c);
                    float progress = Mathf.curve(Interp.pow5Out.apply(e.fin()), 0, 0.2f) * WHFx.fout(e.fin(), 0.1f);
                    Drawn.surround(e.id, e.x, e.y, 35, 10, 6, 8, progress);
                    Fill.circle(e.x, e.y, 20 * progress);
                    Draw.color(Pal.coalBlack.cpy());
                    Fill.circle(e.x, e.y, 12.5f * progress);
                }),
                WHFx.hitSpark(30, c, 20, splashDamageRadius, 1, 6),
                WHFx.sharpBlast(120, c, c, 70),
                WHFx.crossBlastArrow45(120, c, c, 8f, 40, splashDamageRadius * 0.5f, splashDamageRadius));
                fragBullet = new DOTBulletType(){
                    {
                        lifetime = 200;
                        DOTDamage = damage = 400;
                        damageInterval = 15;
                        DOTRadius = 16 * tilesize;
                        radIncrease = 0.5f;
                        status = WHStatusEffects.palsy;
                        statusDuration = 400f;
                        effectTimer = 4f;
                        fx = WHFx.tri(60, c, 1, 0, 5);
                        lightningColor = c;
                        despawnEffect =
                        new MultiEffect(
                        WHFx.hitSpark(90, c, 20, DOTRadius, 1.5f, 10),
                        WHFx.square(90, c, 20, DOTRadius, 5),
                        WHFx.instRotation(90, c, DOTRadius, 45, false),
                        WHFx.generalExplosion(90, c, DOTRadius, 0, true));
                    }

                    @Override
                    public void despawned(Bullet b){
                        super.despawned(b);
                        Damage.damage(b.team, b.x, b.y, DOTRadius, 300, true, collidesAir, collidesGround, scaledSplashDamage, b);
                    }
                };
            }

            public final float checkRange = 12 * tilesize;
            public final Effect hitEff = WHFx.square(60, hitColor, 4, 20, 5);
            private final Seq<Healthc> all = new Seq<>();
            public final int maxTargets = 12;
            public final float attackInterval = 12;
            public final float damageOther = 80;
            public final float healMult = 1;
            public final float healPercent = 2f;

            @Override
            public void update(Bullet b){
                super.update(b);

                Vec2 v = new Vec2().set(b);
                if(b.timer(1, 20)){
                    for(int j = 0; j < 4; ++j){
                        rand.setSeed(b.id);
                        Drawn.randFadeLightningEffect(v.x, v.y, rand.random(30, 80), Mathf.random(7, 12), hitColor, Mathf.chance(0.5));
                    }
                }

                if(b.time > b.lifetime / 2 && b.timer(0, attackInterval)){
                    all.clear();
                    Units.nearbyEnemies(b.team, b.x, b.y, checkRange, other -> {
                        if(other.team != b.team && other.hittable()){
                            all.add(other);
                        }
                    });
                    Vars.indexer.allBuildings(b.x, b.y, checkRange, other -> {
                        if((b.team != Team.derelict || state.rules.coreCapture) && ((other.team != b.team && other.block.targetable) || other.damaged())){
                            all.add(other);
                        }
                    });

                    all.sort(h -> h.dst2(b.x, b.y));
                    int len = Math.min(all.size, maxTargets);

                    for(int i = 0; i < len; i++){
                        Healthc other = all.get(i);
                        var absorber = Damage.findAbsorber(b.team, b.x, b.y, other.getX(), other.getY());

                        if(((Teamc)other).team() == b.team){
                            if(other.damaged()){
                                other.heal(healPercent / 100f * other.maxHealth() * healMult);
                                healEffect.at(other);
                                hitEff.at(other.getX(), other.getY(), b.angleTo(other), hitColor);

                                if(other instanceof Building build){
                                    Fx.healBlockFull.at(b.x, b.y, 0f, hitColor, build.block);
                                }
                            }
                        }else{
                            if(absorber != null){
                                other = absorber;
                            }
                            if(other instanceof Building build){
                                build.damage(b.team, damageOther * damageMultiplier(b));
                            }else{
                                other.damage(damageOther * damageMultiplier(b));
                            }
                            if(other instanceof Statusc s){
                                s.apply(WHStatusEffects.palsy, 120);
                            }
                            hitEff.at(other.getX(), other.getY(), b.angleTo(other), hitColor);
                            PositionLightning.createEffect(b, other, hitColor, 2, Mathf.random(0.5f, 1.5f));
                        }
                    }
                }
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                if(!pierce || b.collided.size >= pierceCap) explode(b);
                super.hitEntity(b, entity, health);
            }

            @Override
            public void hit(Bullet b){
                explode(b);
                super.hit(b);
            }

            public void explode(Bullet b){
                if(!(b.owner instanceof ShootMatchTurretBuild tb)) return;
                for(int i = 0; i < intervalBullets; i++){
                    float angleOffset = i * intervalSpread - (intervalBullets - 1) * intervalSpread / 2f;
                    Position p2 = WHUtils.pos(b.x, b.y);

                    intervalBullet.create(tb, tb.team, tb.x, tb.y, tb.rotation + intervalAngle + angleOffset, -1, 1, 1, p2);
                }
            }

            @Override
            public void updateBulletInterval(Bullet b){
            }
        };

        CycloneMissleLauncherMissile1 = new CritMissileBulletType(){{
            shootPattern = new ShootBarrel(){{
                shots = 4;
                shotDelay = 18f;
                barrels = new float[]
                {-44 / 4f, 72 / 4f, 0f,
                44 / 4f, 72 / 4f, 0f};
            }};
            ammoMultiplier = 2;
            buildingDamageMultiplier = 0.1f;
            critChance = 0.18f;
            critMultiplier = 3f;
            sprite = name("Cyclone-missile-launcher-missile1");
            speed = 4.6f;
            drag = -0.01f;
            lifetime = 122f;
            trailLength = 18;
            width = 20f;
            height = 39f;
            shrinkX = shrinkY = 0;
            homingPower = 0.05f;
            homingRange = 200;
            homingDelay = 20f;
            followAimSpeed = 1;
            reflectable = false;

            flameWidth = 4f;
            flameLength = 20f;
            lengthOffset = 11;

            colors = new Color[]{Pal.redLight.cpy().a(0.4f), Pal.redLight.cpy().a(0.8f), Pal.redLight.cpy().lerp(Color.white, 0.8f)};
            hitColor = trailColor = Pal.redLight.cpy();

            drawMissile = true;
            loopSound = loopMissileTrail;
            loopSoundVolume = 0.6f;

            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootSmokeMissileColor;

            trailEffect = Fx.missileTrailSmokeSmall;
            trailInterval = 3f;

            damage = splashDamage = 700;
            scaledSplashDamage = true;
            splashDamageRadius = 80;

            despawnEffect = hitEffect = new MultiEffect(
            Fx.massiveExplosion, Fx.scatheExplosion, Fx.scatheLight, new WaveEffect(){{
                lifetime = 10f;
                strokeFrom = 4f;
                sizeTo = 150f;
            }});

            fragLifeMin = 0.1f;
            fragBullets = 7;
            fragBullet = new CritBulletType(3.4f, 90){{
                critMultiplier = 1.5f;
                buildingDamageMultiplier = 0.1f;
                drag = 0.02f;
                hitEffect = Fx.massiveExplosion;
                despawnEffect = Fx.scatheSlash;
                knockback = 0.8f;
                lifetime = 23f;
                width = height = 18f;
                collidesTiles = false;
                splashDamageRadius = 40f;
                splashDamage = 100f;
                backColor = trailColor = hitColor = Pal.redLight;
                frontColor = Color.white;
                smokeEffect = Fx.shootBigSmoke2;
                despawnShake = 7f;
                lightRadius = 30f;
                lightColor = Pal.redLight;
                lightOpacity = 0.5f;

                trailLength = 20;
                trailWidth = 3.5f;
                trailEffect = Fx.none;
            }};
        }};

        CycloneMissleLauncherMissile2 = new CritMissileBulletType(){{
            shootPattern = new ShootBarrel(){{
                shots = 1;
                barrels = new float[]
                {-44 / 4f, 72 / 4f, 0f,
                44 / 4f, 72 / 4f, 0f};
            }};

            buildingDamageMultiplier = 0.1f;
            critChance = 0.05f;
            critMultiplier = 3f;
            sprite = name("Cyclone-missile-launcher-missile2");
            speed = 4.6f;
            drag = -0.01f;
            lifetime = 122f;
            trailLength = 18;
            width = 20f;
            height = 39f;
            shrinkX = shrinkY = 0;
            homingPower = 0.05f;
            homingRange = 200;
            homingDelay = 20f;
            followAimSpeed = 1;
            reflectable = false;

            flameWidth = 4f;
            flameLength = 20f;
            lengthOffset = 11;

            Color c = WHItems.sealedPromethium.color.cpy();
            colors = new Color[]{c.a(0.4f), c.a(0.8f), c.lerp(Color.white, 0.8f)};
            hitColor = trailColor = c;

            drawMissile = true;
            loopSound = loopMissileTrail;
            loopSoundVolume = 0.6f;

            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootSmokeMissileColor;

            trailEffect = Fx.missileTrailSmokeSmall;
            trailInterval = 3f;

            damage = splashDamage = 800;
            splashDamageRadius = 80;

            despawnEffect = hitEffect = new MultiEffect(new WaveEffect(){{
                colorFrom = colorTo = c;
                lifetime = 180;
                strokeFrom = 4f;
                interp = Interp.pow5Out;
                sizeTo = 100;
            }});

            fragRandomSpread = 0;
            fragBullets = 1;
            fragBullet = WHBulletsOther.CycloneMissleLauncherMissile2Frag;
        }};

        CycloneMissleLauncherMissile3 = new CritMissileBulletType(){
            {
                reloadMultiplier = 0.75f;
                buildingDamageMultiplier = 0.1f;
                critChance = 0.25f;
                critMultiplier = 3f;
                sprite = name("Cyclone-missile-launcher-missile3");
                speed = 4.6f;
                drag = -0.01f;
                lifetime = 122f;
                trailLength = 18;
                width = 20f;
                height = 39f;
                shrinkX = shrinkY = 0;
                homingPower = 0.01f;
                homingRange = 200;
                homingDelay = 20f;
                followAimSpeed = 0.5f;
                reflectable = false;

                flameWidth = 4f;
                flameLength = 20f;
                lengthOffset = 11;

                colors = new Color[]{WHItems.refineCeramite.color.cpy().a(0.4f), WHItems.refineCeramite.color.cpy().a(0.8f), WHItems.refineCeramite.color.cpy().lerp(Color.white, 0.8f)};
                hitColor = trailColor = WHItems.refineCeramite.color.cpy();

                drawMissile = true;
                loopSound = loopMissileTrail;
                loopSoundVolume = 0.6f;

                shootEffect = Fx.shootBig;
                smokeEffect = Fx.shootSmokeMissileColor;

                trailRotation = true;
                trailEffect = WHFx.instTrail(WHItems.refineCeramite.color.cpy(), 40, false);
                trailInterval = 18f;

                damage = splashDamage = 1500;
                scaledSplashDamage = true;
                splashDamageRadius = 120;

                lightning = 5;
                lightningColor = WHItems.refineCeramite.color.cpy();
                lightningDamage = 50;
                lightningLength = 8;
                lightningLengthRand = 7;

                despawnEffect = hitEffect = new MultiEffect(
                WHFx.circleOut(hitColor, splashDamageRadius),
                WHFx.trailHitSpark(90, hitColor, 20, splashDamageRadius, 1.7f, 15),
                WHFx.hitSpark(90, hitColor, 15, 100, 1.5f, 14f),
                WHFx.blast(hitColor, splashDamageRadius),
                WHFx.instBombSize(hitColor, 4, splashDamageRadius),

                Fx.scatheLight, new WaveEffect(){{
                    colorFrom = colorTo = WHItems.refineCeramite.color.cpy();
                    lifetime = 10f;
                    strokeFrom = 4f;
                    sizeTo = 150f;
                }});

                fragLifeMin = 0.1f;
                fragBullets = 3;
                fragBullet = new CritBulletType(3.4f, 200){{
                    critMultiplier = 1.5f;
                    buildingDamageMultiplier = 0.1f;
                    drag = 0.02f;
                    hitEffect = Fx.massiveExplosion;
                    despawnEffect = Fx.scatheSlash;
                    knockback = 0.8f;
                    lifetime = 23f;
                    width = height = 18f;
                    collidesTiles = false;
                    splashDamageRadius = 40f;
                    splashDamage = 300;
                    backColor = trailColor = hitColor = WHItems.refineCeramite.color.cpy();
                    frontColor = Color.white;
                    smokeEffect = Fx.shootBigSmoke2;
                    despawnShake = 7f;
                    lightRadius = 30f;
                    lightColor = hitColor;
                    lightOpacity = 0.5f;

                    trailLength = 20;
                    trailWidth = 3.5f;
                    trailEffect = Fx.none;
                }};
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);
                PositionLightning.createRandomRange(b, b.team, b, splashDamageRadius, hitColor, Mathf.chanceDelta(0.5f),
                0, 0, PositionLightning.WIDTH, 0, 8, hitPos -> {
                    Damage.damage(b.team, hitPos.getX(), hitPos.getY(), splashDamageRadius / 5, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
                    WHFx.lightningHitLarge.at(hitPos.getX(), hitPos.getY(), hitColor);
                });
            }
        };

        CrumbleCeramiteBullet = new CritBulletType(){
            {
                shootPattern = new ShootAlternate(){{
                    spread = 20 / 2f;
                    shotDelay = 6;
                    shots = 8;
                }};

                ammoMultiplier = 2f;
                reloadMultiplier = 1.5f;

                critChance = 0.3f;
                critMultiplier = 1.5f;
                armorMultiplier = 0.3f;
                bouncing = true;

                damage = 200;
                splashDamage = damage;
                splashDamageRadius = 32;

                pierceCap = 3;
                knockback = 0.2f;
                speed = 6;
                width = 13f;
                height = width * 2.5f;
                shrinkY = 0.3f;
                sprite = name("energy-bullet");
                shootEffect = Fx.shootBig2;
                smokeEffect = Fx.shootSmokeDisperse;
                frontColor = Color.white;

                trailColor = backColor = hitColor = WHItems.ceramite.color.cpy();

                trailLength = 6;
                trailWidth = width / 4f;

                lifetime = 460 / speed;
                trailRotation = true;
                weaveMag = 1;
                weaveScale = 5f;

                trailInterval = 5f;
                trailChance = 0.2f;
                trailEffect = Fx.disperseTrail;

                hitEffect = despawnEffect = new MultiEffect(
                WHFx.instHit(hitColor, true, 2, 12),
                WHFx.instRotation(30, hitColor, splashDamageRadius * 1.2f, 0, false),
                WHFx.generalExplosion(15, hitColor, splashDamageRadius, 0, true),
                WHFx.trailCircleHitSpark(30, hitColor, 10, splashDamageRadius, 1.5f, 12f),
                WHFx.square(30, hitColor, 8, splashDamageRadius, 5));
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(!(entity instanceof Healthc)) return;
                if(entity instanceof Unit unit && unit.hasEffect(WHStatusEffects.palsy)){
                    unit.damagePierce(damage / 2);
                }
            }
        };

        CrumbleCulverCrystalBullet = new CritBulletType(){
            {
                shootPattern = new ShootAlternate(){{
                    spread = 20 / 2f;
                    shotDelay = 8;
                    shots = 6;
                }};

                ammoMultiplier = 2f;

                critChance = 0.3f;
                critMultiplier = 1.5f;
                armorMultiplier = 0.8f;
                bouncing = true;

                damage = 400;
                splashDamage = -1;
                splashDamageRadius = 32;
                status = WHStatusEffects.rock;
                statusDuration = 120f;

                pierceCap = 5;
                knockback = 0.2f;
                speed = 6;
                width = 15;
                height = width * 1.2f;
                shrinkY = 0.3f;
                sprite = name("tall");

                Color c = hitColor = backColor = trailColor = lightningColor = WHItems.culverCrystal.color.cpy();
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                shootEffect = new MultiEffect(
                WHFx.shoot3DWave(30, hitColor, 20, 6),
                WHFx.shootLine(50, 45),
                Fx.shootBigColor
                );
                smokeEffect = Fx.shootSmokeDisperse;

                trailLength = -1;
                lightning = 3;
                lightningDamage = 35;
                lightningLength = lightningLengthRand = 12;

                lifetime = 460 / speed;
                trailRotation = true;

                trailInterval = 5f;
                trailChance = 0.03f;
                trailEffect = new MultiEffect(
                WHFx.hitPoly(60, frontColor, backColor, 2, 20, 5, 5, 60)).rotWithParent(true);

                hitEffect = new MultiEffect(
                WHFx.trailHitSpark(30, hitColor, 10, splashDamageRadius, 1.5f, 12f),
                WHFx.square(30, hitColor, 8, splashDamageRadius, 5)
                );
                despawnEffect = new MultiEffect(
                WHFx.sharpBlast(30, hitColor, frontColor, splashDamageRadius),
                WHFx.linePolyOut(30, hitColor, splashDamageRadius, 2, 4, 0)
                );

                fragBullets = 2;
                fragBullet = new CritBulletType(3.4f, 80){
                    {
                        critMultiplier = 1.5f;
                        buildingDamageMultiplier = 0.1f;
                        drag = 0.02f;

                        knockback = 0.8f;
                        lifetime = 23f;
                        width = 10f;
                        height = width * 2f;
                        splashDamageRadius = 24f;
                        splashDamage = damage;
                        backColor = trailColor = hitColor = c;
                        frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.linePolyOut(30, hitColor, splashDamageRadius, 2, 4, 0),
                        WHFx.square(30, hitColor, 8, splashDamageRadius, 5)
                        );

                        lightRadius = 30f;
                        lightColor = hitColor;
                        lightOpacity = 0.5f;

                        trailLength = 0;
                    }

                    @Override
                    public void draw(Bullet b){
                        Draw.color(hitColor);
                        Draw.z(Layer.bullet);
                        rand.setSeed(b.id);
                        var rot = b.fin() * 300 * rand.random(1f, 2f);
                        Drawn.drawCrystal(
                        b.x, b.y, 12, 6, 8f, 0f, 0f, 0.4f, EFFECT_MASK,
                        Layer.bullet, rot, b.rotation(), Tmp.c1.set(hitColor).a(0.6f), hitColor
                        );
                    }
                };
            }

            @Override
            public void update(Bullet b){
                super.update(b);
                if(Mathf.chanceDelta(0.08f) && b.fin() > 0.1){
                    Tmp.v1.rnd(Mathf.random(20));
                    WHFx.sineTrail(90, hitColor, 15, 60, 1.5f, 2, 3)
                    .rotWithParent(true).at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, b.rotation(), hitColor, b);
                }
            }

            @Override
            public void createSplashDamage(Bullet b, float x, float y){
                super.createSplashDamage(b, x, y);
                if(status != StatusEffects.none){
                    Damage.status(b.team, x, y, splashDamageRadius, WHStatusEffects.scare, 30, collidesAir, collidesGround);
                    Damage.status(b.team, x, y, splashDamageRadius, StatusEffects.melting, 90, collidesAir, collidesGround);
                }
            }

            @Override
            public void draw(Bullet b){
                Draw.color(hitColor);
                Draw.z(Layer.bullet);
                rand.setSeed(b.id);
                var rot = b.fin() * 600 * rand.random(1f, 2f);
                Drawn.drawCrystal(
                b.x, b.y, 30f, 14f, 8f, 0f, 0f, 0.8f, EFFECT_MASK,
                Layer.bullet, rot, b.rotation(), Tmp.c1.set(hitColor).a(0.6f), hitColor
                );
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(!(entity instanceof Healthc)) return;
                if(entity instanceof Unit unit && unit.hasEffect(WHStatusEffects.palsy)){
                    unit.damagePierce(damage);
                }
            }
        };

        CrumbleSealedPromethiumBullet = new LightningLinkerBulletType(){
            {
                sprite = "large-orb";

                speed = 2;
                lifetime = 175.1f;
                drag = -0.003f;
                homingDelay = 33;
                homingPower = 0.08f;
                homingRange = 80;
                damage = 250;
                splashDamageRadius = 64;
                splashDamage = 150;
                status = WHStatusEffects.plasmaFireBurn;
                statusDuration = 60;
                shrinkY = 0;
                trailLength = 13;
                trailWidth = 32 / 4f;
                hitColor = lightningColor = backColor = frontColor = trailColor = WHPal.SkyBlue.cpy();
                width = height = 15;

                hitSound = explosionQuad;

                drawCircle = false;

                linkRange = randomGenerateRange = 120;
                randomLightningNum = 1;
                effectLightningChance = 0.1f;
                hitSpacing = 16;
                maxHit = 4;
                linkLightingDamage = 80;
                collides = true;
                scaleLife = false;

                weaveMag = 0.3f;
                weaveScale = 12f;
                weaveRandom = true;

                lightningDamage = 50;
                lightning = 2;
                lightningLength = 8;
                lightningLengthRand = 6;

                despawnEffect = hitEffect = new MultiEffect(
                WHFx.instRotation(60, hitColor, splashDamageRadius, 0, false),
                WHFx.hitSpark(60, hitColor, 20, splashDamageRadius, 1.5f, 12f),
                WHFx.square(60, hitColor, 20, splashDamageRadius, 6),
                WHFx.circleOut(60, splashDamageRadius, 3),
                WHFx.circleOut(60, splashDamageRadius / 2, 3)
                );

                shootEffect = new MultiEffect(
                new Effect(35, e -> {
                    Draw.color(hitColor);
                    Lines.stroke(2 * e.fout());
                    Lines.ellipse(30, e.x, e.y, 16 * e.fin(), 5, e.rotation + 90);
                }), WHFx.shootLine(8, 15));
            }

            @Override
            public void update(Bullet b){
                super.update(b);
                if(!(b instanceof TrailBullet Interval)) return;
                for(int i = 0; i < 2; i++){
                    if(!Vars.headless){
                        if(Interval.trails[i] == null) Interval.trails[i] = new Trail(22);
                        Interval.trails[i].length = 22;
                    }
                    float dx = WHUtils.dx(b.x, 5, (b.time * (8 - (i % 2 == 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 180 * i),
                    dy = WHUtils.dy(b.y, 5, (b.time * (8 - (i % 2 != 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 180 * i);
                    if(!Vars.headless) Interval.trails[i].update(dx, dy, trailInterp.apply(b.fin()) * (1 + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0)));
                    if(Interval.vs[i] != null) Interval.vs[i].set(dx, dy);
                }
            }

            @Override
            public void draw(Bullet b){
                super.draw(b);
                float vel = Math.max(0, b.vel.len() / speed);
                float out = b.time > b.lifetime - 12 ? (b.lifetime - b.time) / 12 : 1;

                Draw.color(trailColor);

                if(!(b instanceof TrailBullet Interval)) return;
                float z = Draw.z();
                Draw.z(z - 1e-4f);
                for(int i = 0; i < 2; i++){
                    if(Interval.trails[i] != null){
                        Interval.trails[i].draw(trailColor, trailWidth * (1 - vel) * out);
                    }
                    if(Interval.vs[i] != null){
                        Fill.circle(Interval.vs[i].x, Interval.vs[i].y, trailWidth * (1 - vel) * out);
                    }
                }
                Draw.z(z);
            }

            @Override
            public void drawTrail(Bullet b){
                if(trailLength > 0 && b.trail != null){
                    float z = Draw.z();
                    Draw.z(z - 1e-4f);
                    b.trail.draw(trailColor, 2.9f);
                    Draw.z(z);
                }
            }

            @Override
            public void init(Bullet b){
                super.init(b);
                if(!(b instanceof TrailBullet Interval)) return;
                for(int i = 0; i < 2; i++){
                    Interval.vs[i] = new Vec2();
                }
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(!(entity instanceof Healthc)) return;
                if(entity instanceof Unit unit && unit.hasEffect(WHStatusEffects.palsy)){
                    unit.damagePierce(damage);
                }
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);
                Vec2 vec = new Vec2().set(b);
                float rad = splashDamageRadius * 1.5f;
                float spacing = 5;
                float damageMulti = b.damageMultiplier();
                for(int k = 0; k < 5; k++){
                    int finalK = k;
                    Time.run(k * spacing, () -> {
                        for(int j : Mathf.signs){
                            Vec2 v = Tmp.v6.rnd(Mathf.random(rad * 1.2f)).add(vec);
                            (j > 0 ? WHFx.chainLightningFade : WHFx.chainLightningFadeReversed).at(v.x, v.y, 12, hitColor, vec);
                            Damage.damage(b.team, b.x, b.y, finalK < 3 ? splashDamageRadius / 2.5f : splashDamageRadius / 5 * finalK, splashDamage * damageMulti, true);
                        }
                    });
                }
            }

            @Override
            public @Nullable Bullet create(
            @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
            float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
            ){
                TrailBullet bullet = TrailBullet.create();

                for(int i = 0; i < 2; i++){
                    if(bullet.trails[i] != null){
                        bullet.trails[i].clear();
                    }
                }
                return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
            }
        };

        SacramentSealedPromethium = new RailBulletType(){
            {
                ammoMultiplier = 8f;

                hitColor = trailColor = WHPal.SkyBlue.cpy().lerp(Pal.techBlue, 0.2f).lerp(Color.white, 0.2f);
                shootEffect = WHFx.instShoot(WHPal.SkyBlue, WHPal.SkyBlueF);
                pierceEffect = hitEffect = WHFx.instHit(hitColor, false, 3, 40);
                smokeEffect = Fx.smokeCloud;

                damage = 1000;
                splashDamage = damage;
                splashDamageRadius = 80;
                buildingDamageMultiplier = 0.2f;
                pierceDamageFactor = 1;
                despawnEffect = new MultiEffect(
                WHFx.instRotation(60, hitColor, splashDamageRadius, 45, false),
                WHFx.generalExplosion(60, hitColor, splashDamageRadius, 0, true));

                pointEffect = new Effect(120, e -> {
                    color(WHPal.SkyBlueF);
                    Draw.z(Layer.effect);
                    rand.setSeed(e.id);
                    stroke(e.fout() * rand.random(0.7f, 1f) * 3.5f);
                    randLenVectors(e.id, 2, rand.random(0.5f, 1.2f) * 30f * e.finpow(), (x, y) -> {
                        Lines.lineAngleCenter(e.x + x, e.y + y, Mathf.angle(x, y) + rand.random(180) * e.fout(),
                        e.foutpowdown() * 15 * rand.random(0.7f, 1f));
                    });
                });

                pointEffectSpace = 20;

                length = 700;
                hitShake = 6f;


                lineEffect = new Effect(120, e -> {
                    if(!(e.data instanceof Vec2 v)) return;

                    Draw.z(Layer.effect);
                    color(e.color);
                    stroke(e.fout() * 2f);

                    Fx.rand.setSeed(e.id);
                    for(int i = 0; i < 7; i++){
                        Fx.v.trns(e.rotation, Fx.rand.random(8f, v.dst(e.x, e.y) - 8f));
                        Lines.lineAngleCenter(e.x + Fx.v.x, e.y + Fx.v.y,
                        e.rotation + e.finpow(), e.foutpowdown() * 20f * Fx.rand.random(0.5f, 1f) + 0.3f);
                    }

                    e.scaled(25, b -> {
                        float realLength = v.dst(e.x, e.y);

                        float baseLen = realLength * b.fin(Interp.pow2Out);
                        float cwidth = 30;
                        float compound = 1f;
                        Color[] colors = {hitColor.a(0.4f), hitColor.a(0.8f), hitColor, Color.white};
                        float lengthFalloff = 0.5f;

                        Tmp.v1.trns(e.rotation, baseLen).add(e.x, e.y);
                        Tmp.v2.trns(e.rotation, baseLen * b.fin(Interp.pow2Out)).add(e.x, e.y);

                        for(Color color : colors){
                            Draw.color(color);
                            Lines.stroke((cwidth *= lengthFalloff) * b.fout());

                            Lines.line(Tmp.v2.x, Tmp.v2.y, v.x, v.y, false);

                            Fill.circle(Tmp.v2.x, Tmp.v2.y, (cwidth *= lengthFalloff) * b.fout());
                            Drawf.tri(Tmp.v1.x, Tmp.v1.y, Lines.getStroke(), cwidth * 2f, e.rotation);

                            compound *= lengthFalloff;
                        }
                        Draw.reset();
                        Drawf.light(b.x, b.y, Tmp.v1.x, Tmp.v1.y, cwidth * 1.4f * b.fout(), colors[0], 0.6f);
                    });
                });
            }


            @Override
            public void init(Bullet b){
                super.init(b);
                float resultLen = b.fdata;

                Tmp.v1.trns(b.rotation(), resultLen).add(b);
                if(despawnHit){
                    hit(b, Tmp.v1.x, Tmp.v1.y);
                }else{
                    createUnits(b, Tmp.v1.x, Tmp.v1.y);
                }

                if(!fragOnHit){
                    createFrags(b, Tmp.v1.x, Tmp.v1.y);
                }

                despawnEffect.at(Tmp.v1.x, Tmp.v1.y, b.rotation(), hitColor);

                despawnSound.at(Tmp.v1, 1f + Mathf.range(hitSoundPitchRange));

                Effect.shake(despawnShake, despawnShake, Tmp.v1);
            }

            @Override
            public void despawned(Bullet b){
            }
        };

        SacramentMolybdenumAlloy = new RailBulletType(){
            {
                ammoMultiplier = 2f;
                rangeChange = 48;

                hitColor = trailColor = WHItems.molybdenumAlloy.color.cpy().lerp(Pal.techBlue, 0.2f).lerp(Color.white, 0.2f);
                shootEffect = WHFx.instShoot(hitColor, Color.white);
                pierceEffect = hitEffect = WHFx.instHit(hitColor, false, 3, 40);
                smokeEffect = Fx.smokeCloud;

                damage = 1500;
                splashDamage = damage * 0.4f;
                splashDamageRadius = 40;
                buildingDamageMultiplier = 0.2f;
                pierceDamageFactor = 1f;
                despawnEffect = new MultiEffect(
                WHFx.trailHitSpark(60, hitColor, 10, 100, 1.5f, 20),
                WHFx.instRotation(60, hitColor, splashDamageRadius * 2, 45, false),
                WHFx.square(60, hitColor, 10, splashDamageRadius, 6),
                WHFx.lineCircleOut(30, hitColor, splashDamageRadius * 1.5f, 2),
                WHFx.generalExplosion(30, hitColor, splashDamageRadius, 5, true));

                pointEffectSpace = 20;

                length = 700;
                hitShake = 6f;

                lineEffect = new MultiEffect(
                new Effect(120, e -> {
                    if(!(e.data instanceof Vec2 v)) return;

                    Draw.z(Layer.effect);
                    color(e.color);
                    stroke(e.fout() * 3f);

                    Fx.rand.setSeed(e.id);
                    for(int i = 0; i < 7; i++){
                        Fx.v.trns(e.rotation, Fx.rand.random(8f, v.dst(e.x, e.y) - 8f));
                        Lines.lineAngleCenter(e.x + Fx.v.x, e.y + Fx.v.y,
                        e.rotation + e.finpow(), e.foutpowdown() * 20f * Fx.rand.random(0.5f, 1f) + 0.3f);
                    }

                    e.scaled(30, b -> {
                        float realLength = v.dst(e.x, e.y);

                        float baseLen = realLength * b.fin(Interp.pow2Out);
                        float cwidth = 30;
                        float compound = 1f;
                        Color[] colors = {hitColor.a(0.4f), hitColor.a(0.8f), hitColor, Color.white};
                        float lengthFalloff = 0.5f;

                        Tmp.v1.trns(e.rotation, baseLen).add(e.x, e.y);
                        Tmp.v2.trns(e.rotation, baseLen * b.fin(Interp.pow2Out)).add(e.x, e.y);

                        for(Color color : colors){
                            Draw.color(color);
                            Lines.stroke((cwidth *= lengthFalloff) * b.fout());

                            Lines.line(Tmp.v2.x, Tmp.v2.y, v.x, v.y, false);

                            Fill.circle(Tmp.v2.x, Tmp.v2.y, (cwidth *= lengthFalloff) * b.fout());
                            Drawf.tri(Tmp.v1.x, Tmp.v1.y, Lines.getStroke(), cwidth * 2f, e.rotation);

                            compound *= lengthFalloff;
                        }
                        Draw.reset();
                        Drawf.light(b.x, b.y, Tmp.v1.x, Tmp.v1.y, cwidth * 1.4f * b.fout(), colors[0], 0.6f);
                    });

                })
                );
            }

            public final Effect moveTrailEffect = new Effect(25, e -> {
                if(!(e.data instanceof TrailEffectData data)) return;
                float resultLen = data.len;
                Trail trail = data.trail;
                Tmp.v1.trns(e.rotation, resultLen * e.fin()).add(e.x, e.y);
                Tmp.v2.trns(e.rotation, 0, Mathf.sin(e.fin() * resultLen + Mathf.randomSeed(e.id, 180f, 360f), resultLen / 8, 1) * 10f);
                float tx = Tmp.v1.x + Tmp.v2.x, ty = Tmp.v1.y + Tmp.v2.y;

                float size = 2.5f;
                if(!state.isPaused()) trail.update(tx, ty, size);
               /* Draw.color(e.color);
                Fill.circle(tx, ty, size * 2 * e.fout());*/
                trail.drawCap(e.color, size * e.fout());
                trail.draw(e.color, size * e.fout());
            });


            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(entity instanceof Unit unit){
                    float x = unit.x, y = unit.y, radius = Math.max(unit.hitSize * 5, 100);
                    Seq<Unit> units = new Seq<>();
                    Units.nearbyEnemies(b.team, x, y, radius, other -> {
                        if(other.team != b.team && other.hittable() && other != unit){
                            units.add(other);
                        }
                    });
                    units.sort(h -> -h.health());
                    units.truncate(3);

                    for(Unit other : units){
                        float angle = unit.angleTo(other);
                        if(mbc instanceof RailBulletType r){
                            r.length = unit.dst(other);
                            Tmp.v1.trns(angle, unit.hitSize + 10);
                            mbc.create(b, x + Tmp.v1.x, y + Tmp.v1.y, angle);
                        }
                    }
                }
            }

            @Override
            public void init(Bullet b){
                super.init(b);

                float resultLen = b.fdata;

                Tmp.v1.trns(b.rotation(), resultLen).add(b);
                if(despawnHit){
                    hit(b, Tmp.v1.x, Tmp.v1.y);
                }else{
                    createUnits(b, Tmp.v1.x, Tmp.v1.y);
                }

                if(!fragOnHit){
                    createFrags(b, Tmp.v1.x, Tmp.v1.y);
                }

                despawnEffect.at(Tmp.v1.x, Tmp.v1.y, b.rotation(), hitColor);

                despawnSound.at(Tmp.v1, 1f + Mathf.range(hitSoundPitchRange));

                for(int i = 0; i < 2; i++){
                    TrailEffectData data = TrailEffectData.create();
                    data.len = length;
                    data.trail = new Trail(12);
                    moveTrailEffect.at(b.x, b.y, b.rotation(), hitColor, data);
                }

                Effect.shake(despawnShake, despawnShake, Tmp.v1);
            }

            @Override
            public void despawned(Bullet b){
            }
        };

        SacramentCulverCrystal = new CritBulletType(8, 1200){
            {
                ammoMultiplier = 4f;

                critChance = 0.3f;
                critMultiplier = 1.5f;
                armorMultiplier = 0.5f;
                pierceCap = 5;

                knockback = 0.2f;
                width = 15;
                height = width * 1.2f;

                Color c = hitColor = backColor = trailColor = lightningColor = WHItems.culverCrystal.color.cpy();
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                shootEffect = new MultiEffect(
                new Effect(60, e -> {
                    float rad = 40 * e.fin(Interp.pow2Out);
                    float w = 8 * e.fout(Interp.pow3Out);
                    Drawn.pseudo3dRing(e.x, e.y, rad, w, 0.28f, e.rotation + 90f, Tmp.c1.set(frontColor).a(0.75f));
                }),
                WHFx.instShoot(hitColor, frontColor),
                WHFx.shootLine(50, 30),
                Fx.shootBigColor
                );
                smokeEffect = Fx.shootSmokeDisperse;

                trailLength = -1;
                lightning = 3;
                lightningDamage = 35;
                lightningLength = lightningLengthRand = 12;
                splashDamageRadius = 60;

                lifetime = 700 / speed;
                trailRotation = true;

                trailInterval = 5f;
                trailChance = 0.03f;
                trailEffect = new MultiEffect(
                new Effect(30, e -> {
                    for(int i = 0; i < 2; i++){
                        color(i == 0 ? backColor : frontColor);

                        float m = i == 0 ? 1f : 0.5f;

                        float rot = e.rotation + 180f;
                        float w = 15f * e.fout() * m;
                        Drawf.tri(e.x, e.y, w, (30f + Mathf.randomSeedRange(e.id, 15f)) * m, rot);
                        Drawf.tri(e.x, e.y, w, 10f * m, rot + 180f);
                    }

                    Drawf.light(e.x, e.y, 60f, frontColor, 0.6f * e.fout());
                }),
                WHFx.hitPoly(60, frontColor, backColor, 1, 20, 5, 5, 20)).rotWithParent(true);

                hitEffect = new MultiEffect(
                WHFx.instHit(hitColor, false, 3, 40),
                WHFx.trailHitSpark(30, hitColor, 10, splashDamageRadius, 1.5f, 12f),
                WHFx.square(30, hitColor, 8, splashDamageRadius, 5)
                );
                despawnEffect = new MultiEffect(
                WHFx.sharpBlast(30, hitColor, frontColor, splashDamageRadius),
                WHFx.linePolyOut(30, hitColor, splashDamageRadius, 2, 4, 0),
                WHFx.linePolyOut(30, hitColor, splashDamageRadius / 2, 2, 4, 0)
                );

                fragBullets = 3;
                fragBullet = new CritBulletType(3.4f, 120){
                    {
                        critMultiplier = 1.5f;
                        buildingDamageMultiplier = 0.1f;
                        drag = 0.02f;

                        knockback = 0.8f;
                        lifetime = 23f;
                        width = 10f;
                        height = width * 2f;
                        splashDamageRadius = 24f;
                        splashDamage = damage;
                        backColor = trailColor = hitColor = c;
                        frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.linePolyOut(30, hitColor, splashDamageRadius, 2, 4, 0),
                        WHFx.square(30, hitColor, 8, splashDamageRadius, 5)
                        );

                        lightRadius = 30f;
                        lightColor = hitColor;
                        lightOpacity = 0.5f;

                        trailLength = 0;
                    }

                    @Override
                    public void draw(Bullet b){
                        Draw.color(hitColor);
                        Draw.z(Layer.bullet);
                        rand.setSeed(b.id);
                        var rot = b.fin() * 300 * rand.random(1f, 2f);
                        Drawn.drawCrystal(
                        b.x, b.y, 12 * b.fout(), 6 * b.fout(), 8f, 0f, 0f, 0.4f, EFFECT_MASK,
                        Layer.bullet, rot, b.rotation(), Tmp.c1.set(hitColor).a(0.6f), hitColor
                        );
                    }
                };
            }

            @Override
            public void update(Bullet b){
                super.update(b);
                Vec2 v = new Vec2().set(b);
                if(b.timer(1, 20)){
                    for(int j = 0; j < 3; ++j){
                        rand.setSeed(b.id);
                        Drawn.randFadeLightningEffect(v.x, v.y, rand.random(40, 80), Mathf.random(7, 12), hitColor, Mathf.chance(0.5));
                    }
                }
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);
                Vec2 v = new Vec2().set(b);
                for(int j = 0; j < 6; ++j){
                    rand.setSeed(b.id);
                    Drawn.randFadeLightningEffect(v.x, v.y, rand.random(30, 80), Mathf.random(7, 12), hitColor, Mathf.chance(0.5));
                }
            }

            @Override
            public void createSplashDamage(Bullet b, float x, float y){
                super.createSplashDamage(b, x, y);
            }

            @Override
            public void draw(Bullet b){
                Draw.color(hitColor);
                Draw.z(Layer.bullet);
                rand.setSeed(b.id);
                var rot = b.fin() * 600 * rand.random(1f, 2f);
                Drawn.drawCrystal(
                b.x, b.y, 40, 20, 5f, 0f, 0f, 0.8f, EFFECT_MASK,
                Layer.bullet, rot, b.rotation(), Tmp.c1.set(hitColor).a(0.6f), hitColor
                );
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(entity instanceof Unit h){
                    float shield = Math.max(h.shield() * 0.05f, b.damage);
                    h.shield(h.shield() - shield);
                    b.damage(b.damage - damage / pierceCap);
                }
            }
        };


        SacramentRefineCeramite = new RailBulletType(){
            {
                ammoMultiplier = 2f;
                rangeChange = 16;

                hitColor = trailColor = WHItems.refineCeramite.color.cpy().lerp(Pal.techBlue, 0.2f).lerp(Color.white, 0.2f);
                shootEffect = WHFx.instShoot(hitColor, Color.white);
                pierceEffect = hitEffect = WHFx.instHit(hitColor, false, 3, 40);
                smokeEffect = Fx.smokeCloud;

                damage = 3800;
                splashDamage = damage * 0.2f;
                splashDamageRadius = 50;
                buildingDamageMultiplier = 0.2f;
                pierceDamageFactor = 0.5f;
                despawnEffect = new MultiEffect(
                WHFx.triSpread(120, hitColor, 5, 12, 100),
                WHFx.trailCircleHitSpark(120, hitColor, 10, 100, 1.4f, 20),
                WHFx.instRotation(60, hitColor, splashDamageRadius * 2, 45, false),
                WHFx.circleOut(60, hitColor, splashDamageRadius * 1.5f),
                WHFx.generalExplosion(60, hitColor, splashDamageRadius, 0, false));

                pointEffectSpace = 20;

                length = 700;
                hitShake = 6f;


                lifetime = 60f;

                lightningDamage = 70;
                lightningLength = 20;

                fragOnHit = false;
                fragBullets = 3;
                fragAngle = 0;
                fragSpread = 120;
                fragRandomSpread = 0;
                fragBullet = new ShrapnelBulletType(){{
                    length = damage = 120;
                    lifetime = 25f;
                    width = 25f;
                    Color c = hitColor = trailColor = WHItems.refineCeramite.color.cpy().lerp(Pal.techBlue, 0.2f).lerp(Color.white, 0.2f);
                    pierceCap = 3;
                    hitEffect = WHFx.square(40, hitColor, 6, 40, 6);
                    fromColor = c.cpy().lerp(Color.white, 0.5f);
                    toColor = c;
                    serrations = 3;
                    serrationSpaceOffset = 30;
                }};

                lineEffect = new MultiEffect(
                new Effect(120, e -> {
                    if(!(e.data instanceof Vec2 v)) return;

                    Draw.z(Layer.effect);
                    color(e.color);
                    stroke(e.fout() * 3f);

                    Fx.rand.setSeed(e.id);
                    for(int i = 0; i < 7; i++){
                        Fx.v.trns(e.rotation, Fx.rand.random(8f, v.dst(e.x, e.y) - 8f));
                        Lines.lineAngleCenter(e.x + Fx.v.x, e.y + Fx.v.y,
                        e.rotation + e.finpow(), e.foutpowdown() * 20f * Fx.rand.random(0.5f, 1f) + 0.3f);
                    }

                    e.scaled(25, b -> {
                        float realLength = v.dst(e.x, e.y);

                        float baseLen = realLength * b.fin(Interp.pow2Out);
                        float cwidth = 30;
                        float compound = 1f;
                        Color[] colors = {hitColor.a(0.4f), hitColor.a(0.8f), hitColor, Color.white};
                        float lengthFalloff = 0.5f;

                        Tmp.v1.trns(e.rotation, baseLen).add(e.x, e.y);
                        Tmp.v2.trns(e.rotation, baseLen * b.fin(Interp.pow2Out)).add(e.x, e.y);

                        for(Color color : colors){
                            Draw.color(color);
                            Lines.stroke((cwidth *= lengthFalloff) * b.fout());

                            Lines.line(Tmp.v2.x, Tmp.v2.y, v.x, v.y, false);

                            Fill.circle(Tmp.v2.x, Tmp.v2.y, (cwidth *= lengthFalloff) * b.fout());
                            Drawf.tri(Tmp.v1.x, Tmp.v1.y, Lines.getStroke(), cwidth * 2f, e.rotation);

                            compound *= lengthFalloff;
                        }
                        Draw.reset();
                        Drawf.light(b.x, b.y, Tmp.v1.x, Tmp.v1.y, cwidth * 1.4f * b.fout(), colors[0], 0.6f);
                    });

                }));
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(entity instanceof Unit h){
                    float shield = Math.max(h.shield() * 0.03f, 100f);
                    h.shield(h.shield() - shield);
                }

                float length = b.dst(entity);
                TrailEffectData data = TrailEffectData.create();
                data.len = length;
                data.trail = new Trail(12);

                moveTrailEffect.at(b.x, b.y, b.rotation(), hitColor, data);
            }

            public final Effect moveTrailEffect = new Effect(15, e -> {
                if(!(e.data instanceof TrailEffectData data)) return;
                float resultLen = data.len;
                Trail trail = data.trail;
                Tmp.v2.trns(e.rotation, resultLen).add(e.x, e.y);
                float tx = Tmp.v2.x, ty = Tmp.v2.y, dst = Mathf.dst(e.x, e.y, tx, ty);
                Tmp.v1.set(Tmp.v2).sub(e.x, e.y).nor();

                float normx = Tmp.v1.x, normy = Tmp.v1.y;
                float range = 50;
                int links = Mathf.ceil(dst / range);
                float spacing = dst / links;

                rand.setSeed(e.id);

                float[] ny = new float[links];
                float[] nx = new float[links];

                for(int i = 0; i < links; i++){
                    if(i == links - 1){
                        nx[i] = tx;
                        ny[i] = ty;
                    }else{
                        float len = (i + 1) * spacing;
                        Tmp.v1.setToRandomDirection(rand).scl(25);
                        nx[i] = e.x + normx * len + Tmp.v1.x;
                        ny[i] = e.y + normy * len + Tmp.v1.y;
                    }
                }
                float currentX = nx[0], currentY = ny[0];

                int tPos = (int)(e.fin() * (links - 1)), nPos;
                tPos = Math.min(tPos, links - 2);
                nPos = tPos + 1;

                float progress = (e.fin() * (links - 1)) - tPos;

                currentX = Mathf.lerp(nx[tPos], nx[nPos], progress);
                currentY = Mathf.lerp(ny[tPos], ny[nPos], progress);

                float size = 2.5f;

                if(!state.isPaused()) trail.update(currentX, currentY, size * e.fout());
                Draw.color(e.color);
                Fill.circle(currentX, currentY, size * 1.5f * e.fout());
                trail.draw(e.color, size);
                trail.drawCap(e.color, size);
            });


            @Override
            public void init(Bullet b){
                super.init(b);

                float resultLen = b.fdata;

                Tmp.v1.trns(b.rotation(), resultLen).add(b);
                if(despawnHit){
                    hit(b, Tmp.v1.x, Tmp.v1.y);
                }else{
                    createUnits(b, Tmp.v1.x, Tmp.v1.y);
                }

                if(!fragOnHit){
                    createFrags(b, Tmp.v1.x, Tmp.v1.y);
                }

                despawnEffect.at(Tmp.v1.x, Tmp.v1.y, b.rotation(), hitColor);

                despawnSound.at(Tmp.v1, 1f + Mathf.range(hitSoundPitchRange));

                float spacing = 10;
                Vec2 v = new Vec2().set(Tmp.v1);
                for(int k = 0; k < 5; k++){
                    Time.run(k * spacing, () -> {
                        for(int j : Mathf.signs){
                            Drawn.randFadeLightningEffect(v.x, v.y, splashDamageRadius * 1.5f, 12, hitColor, j > 0);
                        }
                        PositionLightning.createRange(b, v, b.team, 100, 3, hitColor, false, splashDamage, 0, 1.5f, 2,
                        p -> {
                            WHFx.generalExplosion(8, hitColor, 25, 2, false).at(p.getX(), p.getY());
                        });
                    });
                }

                TrailEffectData data = TrailEffectData.create();
                data.len = length;
                data.trail = new Trail(13);

                moveTrailEffect.at(b.x, b.y, b.rotation(), hitColor, data);

                Effect.shake(despawnShake, despawnShake, Tmp.v1);
            }

            @Override
            public void despawned(Bullet b){
            }
        };

        ColossusCulverCrystal = new ShieldBreakerType(6, 550, 800){
            {
                rangeChange = 40f;
                ammoMultiplier = 2;

                shootPattern = new ShootPattern(){{
                    firstShotDelay = 30f;
                }};

                splashDamage = damage;
                float sp = splashDamageRadius = 120;

                scaleLife = true;
                collidesTiles = false;
                collides = false;
                collidesAir = false;
                scaledSplashDamage = true;

                sprite = name("energy-bullet");
                Color c = hitColor = backColor = trailColor = lightningColor = WHItems.culverCrystal.color.cpy();
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                hitShake = 1f;
                despawnSound = hitSound = explosionArtilleryShockBig;

                shootEffect = new MultiEffect(Fx.shootTitan, WHFx.instShoot(hitColor, frontColor), WHFx.shootLine(20, 45));
                trailEffect = WHFx.hitPoly(60, frontColor, backColor, 2, 20, 5, 5, 360);
                trailInterval = 2f;

                shrinkX = 0.15f;
                shrinkY = 0.63f;
                shrinkInterp = Interp.slope;

                trailSinScl = 2.5f;
                trailSinMag = 0.15f;

                width = 25;
                height = 60;
                trailLength = 20;
                trailWidth = width / 5f;


                despawnEffect = hitEffect =
                new MultiEffect(
                new Effect(120, sp * 2.0F, (e) -> {
                    color(e.color, Color.white, e.fout() * 0.7F);
                    stroke(3 * e.fout());
                    circle(e.x, e.y, sp * Mathf.curve(e.fin(), 0, 0.15f) * WHFx.fout(e.fin(Interp.smooth), 0.25f));
                }),
                WHFx.instRotation(90, hitColor, splashDamageRadius, 45, false)
                );

                fragRandomSpread = 0;
                fragBullets = 1;
                fragBullet = new LightningLinkerBulletType(){
                    {
                        drawCircle = true;
                        frontColor = Pal.coalBlack;
                        backColor = c;
                        linkLightingDamage = 120;
                        linkRange = sp;
                        hitSpacing = 10;
                        hitColor = trailColor = lightningColor = c;

                        shieldDamageMultiplier = 5;

                        collidesAir = collidesTiles = collides = false;
                        scaleLife = false;

                        lightning = 1;
                        lightningLength = 8;
                        lightningDamage = 30;

                        lifetime = 120;

                        speed = 0;
                        despawnHit = false;
                        damage = 150;
                        splashDamageRadius = sp + 20;
                        splashDamage = 1500;
                        buildingDamageMultiplier = 0.1f;
                        despawnEffect = hitEffect =
                        new MultiEffect(
                        WHFx.instRotation(120, hitColor, splashDamageRadius, 45, false),
                        WHFx.generalExplosion(120, hitColor, splashDamageRadius, 10, true),
                        WHFx.circleOut(120, hitColor, splashDamageRadius),
                        WHFx.trailHitSpark(120, hitColor, 20, splashDamageRadius, 2, 14),
                        WHFx.square(120, hitColor, 20, splashDamageRadius, 5),
                        WHFx.crossBlastArrow45(120, hitColor, hitColor, 22, 60, splashDamageRadius * 0.6f, splashDamageRadius),
                        WHFx.spreadOutSpark(120, splashDamageRadius, 10, 5, 60, 30, 10, Interp.smooth)
                        );
                    }

                    public final float slowLinkRange = 160;
                    public final float slowLinkDuration = 30;
                    public StatusEffect slowLinkStatus = WHStatusEffects.powerReduce2;
                    public final float slowLinkInterval = 20;
                    public final float slowLinkAcquireChance = 0.22f;
                    public final int slowLinkMaxTargets = 8;
                    public final Seq<Unit> slowLinkCandidates = new Seq<>();

                    @SuppressWarnings("unchecked")
                    public Seq<Unit> slowLinks(Bullet b){
                        if(!(b.data instanceof Seq<?>)){
                            b.data = new Seq<Unit>();
                        }
                        return (Seq<Unit>)b.data;
                    }

                    public boolean validSlowTarget(Bullet b, Unit unit){
                        return unit != null && unit.isAdded() && !unit.dead &&
                        unit.team != b.team &&
                        unit.within(b.x, b.y, slowLinkRange) &&
                        unit.checkTarget(collidesAir, collidesGround);
                    }

                    public void drawSlowLink(Bullet b, Unit target, int index){
                        float tx = target.x, ty = target.y;
                        float dst = Mathf.dst(b.x, b.y, tx, ty);
                        if(dst <= 0.1f) return;

                        float angle = Angles.angle(b.x, b.y, tx, ty);
                        float life = b.fout();
                        long linkSeed = target.id;
                        float linkRand = Mathf.randomSeed(linkSeed, 0.85f, 1.5f);
                        float phase = Time.time / 2 * (0.16f + 0.03f * linkRand) + b.id * 0.17f + index * 0.73f + linkRand * 1.5f;
                        float side = (((target.id + index) & 1) == 0) ? 1f : -1f;
                        float sizeMag = Mathf.clamp(target.hitSize / 18f, 0.45f, 1.35f);
                        float baseWave = Mathf.clamp(dst * 0.18f + target.hitSize * 0.75f, 6f, 42f) * life * linkRand * sizeMag;
                        float chaosA = baseWave * 0.15f * Mathf.sin(phase * 2.4f + linkRand * 5f);
                        float chaosB = baseWave * 0.13f * Mathf.cos(phase * 2.6f - linkRand * 4f);
                        float waveA = baseWave * (0.82f + 0.18f * Mathf.sin(phase * 1.1f)) + chaosA;
                        float waveB = baseWave * 0.52f * (0.82f + 0.18f * Mathf.cos(phase * 1.3f)) + chaosB;
                        float coreA = waveA * 0.38f;
                        float coreB = waveB * 0.38f;
                        int segments = Math.max(10, (int)(dst / 4.8f));


                        float jitterMag = 0.02f + 0.013f * Mathf.clamp(target.hitSize / 30f, 0f, 1f);
                        float alongJitter1 = dst * jitterMag * Mathf.sin(phase * 1.9f + linkRand * 4f);
                        float alongJitter2 = dst * jitterMag * Mathf.cos(phase * 1.7f - linkRand * 4f);
                        float curve1 = 0.34f + (linkRand - 1f) * 0.1f;
                        float curve2 = 0.74f + (linkRand - 1f) * 0.12f;
                        float cx1 = b.x + Angles.trnsx(angle, dst * curve1 + alongJitter1) + Angles.trnsx(angle + 90f * side, waveA);
                        float cy1 = b.y + Angles.trnsy(angle, dst * curve1 + alongJitter1) + Angles.trnsy(angle + 90f * side, waveA);
                        float cx2 = b.x + Angles.trnsx(angle, dst * curve2 + alongJitter2) + Angles.trnsx(angle - 90f * side, waveB);
                        float cy2 = b.y + Angles.trnsy(angle, dst * curve2 + alongJitter2) + Angles.trnsy(angle - 90f * side, waveB);

                        float icx1 = b.x + Angles.trnsx(angle, dst * (curve1 - 0.04f)) + Angles.trnsx(angle + 90f * side, coreA);
                        float icy1 = b.y + Angles.trnsy(angle, dst * (curve1 - 0.04f)) + Angles.trnsy(angle + 90f * side, coreA);
                        float icx2 = b.x + Angles.trnsx(angle, dst * (curve2 + 0.04f)) + Angles.trnsx(angle - 90f * side, coreB);
                        float icy2 = b.y + Angles.trnsy(angle, dst * (curve2 + 0.04f)) + Angles.trnsy(angle - 90f * side, coreB);

                        float shimmer = 0.5f + 0.5f * Mathf.sin(phase * 2.2f);
                        float baseStroke = Mathf.clamp(target.hitSize / 6f, 1f, 4f);
                        float stroke = (baseStroke + baseStroke * shimmer * 0.55f + Mathf.absin(2f, 0.22f)) * life;

                        Draw.color(hitColor, Color.white, 0.25f + 0.75f * shimmer);
                        Lines.stroke(stroke);
                        Lines.curve(b.x, b.y, cx1, cy1, cx2, cy2, tx, ty, segments);

                        Draw.color(Color.white, hitColor, 0.45f + 0.4f * shimmer);
                        Lines.stroke(stroke * 0.45f);
                        Lines.curve(b.x, b.y, icx1, icy1, icx2, icy2, tx, ty, segments);

                        Fill.circle(tx, ty, stroke * life * (0.9f + 0.2f * shimmer));
                        Drawf.light(tx, ty, 12f * life * (0.8f + 0.2f * shimmer), hitColor, 0.5f + 0.15f * shimmer);
                        Draw.reset();
                    }

                    @Override
                    public void update(Bullet b){
                        super.update(b);
                        Vec2 v = new Vec2().set(b);
                        if(b.timer(1, 20)){
                            for(int j = 0; j < 3; ++j){
                                rand.setSeed(b.id);
                                Drawn.randFadeLightningEffect(v.x, v.y, rand.random(70, 120), Mathf.random(7, 12), hitColor, Mathf.chance(0.5));
                            }
                        }
                        if(Mathf.chanceDelta(0.15)){
                            WHFx.tentacleCorona(35, 30, 15, 1, 2, hitColor, Pal.lightOrange)
                            .at(b.x, b.y, b.rotation(), hitColor, b);
                        }

                        Seq<Unit> links = slowLinks(b);
                        links.remove(unit -> !validSlowTarget(b, unit));

                        if(b.timer(2, slowLinkInterval)){
                            slowLinkCandidates.clear();
                            float radius = slowLinkRange;
                            float diameter = radius * 2f;

                            Units.nearbyEnemies(b.team, b.x - radius, b.y - radius, diameter, diameter, unit -> {
                                if(!validSlowTarget(b, unit) || links.contains(unit)) return;
                                if(Mathf.chance(slowLinkAcquireChance)){
                                    slowLinkCandidates.add(unit);
                                }
                            });

                            while(links.size < slowLinkMaxTargets && !slowLinkCandidates.isEmpty()){
                                links.add(slowLinkCandidates.remove(Mathf.random(slowLinkCandidates.size - 1)));
                            }

                            for(Unit unit : links){
                                unit.apply(slowLinkStatus, slowLinkDuration);
                            }

                            for(Unit unit : links){
                                if(!validSlowTarget(b, unit)) continue;
                                unit.apply(slowLinkStatus, slowLinkDuration);
                                unit.damagePierce(300);
                            }
                        }
                    }

                    @Override
                    public void draw(Bullet b){
                        super.draw(b);
                        Draw.z(Layer.bullet - 0.001f);
                        if(!(b.data instanceof Seq<?>)) return;
                        @SuppressWarnings("unchecked")
                        Seq<Unit> links = (Seq<Unit>)b.data;
                        int index = 0;
                        for(Unit unit : links){
                            if(!validSlowTarget(b, unit)) continue;
                            drawSlowLink(b, unit, index++);
                        }
                    }

                    public final float crowdBonusPerUnit = 0.25f;
                    public final float crowdBonusMax = 4f;

                    @Override
                    public void despawned(Bullet b){
                        if(despawnHit){
                            hit(b, b.x, b.y, false);
                        }else{
                            createUnits(b, b.x, b.y);
                        }

                        despawnEffect.at(b.x, b.y, b.rotation(), hitColor);
                        despawnSound.at(b, 1f + Mathf.range(hitSoundPitchRange));

                        Effect.shake(despawnShake, despawnShake, b);
                    }

                    @Override
                    public void createSplashDamage(Bullet b, float x, float y){
                        if(splashDamageRadius <= 0f || b.absorbed) return;

                        float radius = splashDamageRadius;
                        float diameter = radius * 2f;
                        int[] targetCount = {0};

                        Units.nearbyEnemies(b.team, x - radius, y - radius, diameter, diameter, unit -> {
                            if(unit == null || !unit.isAdded() || unit.dead) return;
                            if(!unit.within(x, y, radius)) return;
                            if((unit.isFlying() && !collidesAir) || (!unit.isFlying() && !collidesGround)) return;
                            targetCount[0]++;
                        });

                        float extraTargets = Math.max(0, targetCount[0] - 1f);
                        float bonusMul = Math.min(crowdBonusMax, extraTargets * crowdBonusPerUnit);
                        float scaledSplash = splashDamage * (1f + Math.max(0f, bonusMul));

                        Damage.damage(b.team, x, y, splashDamageRadius, scaledSplash * b.damageMultiplier(),
                        splashDamagePierce, collidesAir, collidesGround, scaledSplashDamage, b);

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
                };
            }
        };

        ColossusCeramite = new ShieldBreakerType(4, 300, 100){{

            splashDamage = 300;
            splashDamageRadius = 150;

            scaleLife = true;
            collidesTiles = false;
            collides = false;
            collidesAir = false;
            scaledSplashDamage = true;

            hitShake = 1f;
            despawnSound = hitSound = explosionArtilleryShockBig;

            shootEffect = new MultiEffect(Fx.shootTitan, WHFx.shootLine(20, 45));
            trailEffect = Fx.vapor;
            trailInterval = 3f;

            shrinkX = 0.15f;
            shrinkY = 0.63f;
            shrinkInterp = Interp.slope;

            trailSinScl = 2.5f;
            trailSinMag = 0.15f;

            trailLength = 14;
            trailWidth = 14 / 5f;
            width = 16;
            height = 32;

            hitColor = lightningColor = backColor = trailColor = WHItems.ceramite.color.cpy();
            frontColor = WHItems.ceramite.color.cpy().lerp(Color.white, 0.5f);
            despawnEffect = hitEffect =
            new MultiEffect(
            WHFx.generalExplosion(120, hitColor, splashDamageRadius, 10, false),
            WHFx.hitSpark(45, Pal.missileYellowBack, 20, splashDamageRadius, 2, 12f),
            WHFx.trailCircleHitSpark(120, hitColor, 10, splashDamageRadius, 2, 10),
            Fx.titanSmokeSmall
            );

            fragLifeMax = 1.5f;
            fragLifeMin = 0.5f;
            fragVelocityMax = 2;

            fragBullets = 3;
            fragBullet = new CritBulletType(){{

                critMultiplier = 2f;
                critChance = 0.08f;

                lightning = 1;
                lightningLength = 8;
                lightningDamage = 30;
                drag = 0.016f;

                lifetime = 55;

                speed = 2;
                sprite = "shell";
                knockback = 1.5f;
                width = 8;
                height = 8;
                lightningColor = frontColor = hitColor = trailColor = backColor = WHItems.ceramite.color.cpy();
                trailLength = 12;
                trailWidth = 2;
                trailChance = 0.01f;
                damage = 100;
                splashDamageRadius = 64;
                splashDamage = 100;
                despawnEffect = hitEffect = new MultiEffect(
                WHFx.square(40, hitColor, 10, splashDamageRadius, 5),
                WHFx.generalExplosion(25, hitColor, splashDamageRadius, 10, false));
            }};

        }};

        ColossusMolybdenumAlloy = new ShieldBreakerType(8, 400, 150){{

            shootPattern = new ShootPattern(){{
                shots = 2;
                shotDelay = 30;
                firstShotDelay = 30f;
            }};

            sprite = name("pierce");
            Color moColor = WHItems.molybdenumAlloy.color.cpy();
            Color moColorDark = moColor.cpy().lerp(Pal.gray, 0.1f);
            pierce = true;
            pierceCap = 4;
            splashDamageRadius = 64;
            splashDamage = 100;

            rangeChange = 3 * 8f;

            drag = -0.03f;
            lifetime = 39.24f;

            hitShake = 1f;
            hitSound = Sounds.explosion;

            shootEffect =
            new MultiEffect(
            Fx.shootTitan, WHFx.shootLine(30, 45),
            WHFx.shootCircleSmall(moColor),
            WHFx.instShoot(moColor.lerp(Color.lightGray, 0.3f), moColor)
            );
            smokeEffect = Fx.shootBigSmoke2;
            trailEffect = WHFx.square(30, moColor, 1, 10, 5);
            trailInterval = 3f;
            trailChance = 0.1f;

            shrinkY = shrinkX = 0;
            trailSinScl = 2.5f;
            trailSinMag = 0.15f;

            trailLength = 14;
            trailWidth = 12 / 4.5f;
            width = 15;
            height = 36;

            hitColor = lightningColor = backColor = trailColor = moColor;
            frontColor = moColor.cpy().lerp(Color.white, 0.5f);
            despawnSound = hitSound = explosionDull;
            hitSoundVolume /= 2.2f;
            hitEffect =
            new MultiEffect(
            WHFx.generalExplosion(120, moColor, splashDamageRadius, 15, false),
            WHFx.hitSpark(45, moColor, 20, splashDamageRadius, 2, 6f),
            WHFx.hitSparkAng(45, Pal.missileYellowBack, moColor, 6, splashDamageRadius, 30, 2.2f, 12)
            );
            despawnEffect = new MultiEffect(
            WHFx.square(60, moColor, 15, 40, 5),
            WHFx.shuttle(60, moColorDark, frontColor, true, 45, 45),
            WHFx.shuttle(60, moColorDark, frontColor, true, 45, 45 + 90f).startDelay(15));

            fragRandomSpread = 0;

            fragBullets = 1;
            fragBullet = new SizeDamageBullet(){
                {
                    sizeDamageCreate.lightning = 1;
                    sizeDamageCreate.lightningDamage = 12;
                    sizeDamageCreate.lightningLength = 10;
                    damageInterp = Interp.pow2In;

                    lifetime = 20;
                    maxDamageMultiple = 6;
                    hitSizeDamage = 100;
                    maxHitSizeScale = 40f;
                    hitSizeLightingScale = 4;

                    hitColor = lightningColor = frontColor = trailColor = backColor = moColor;
                    trailLength = 12;
                    trailWidth = 2;
                    trailChance = 0.01f;
                    despawnEffect = hitEffect = WHFx.generalExplosion(60, moColor, splashDamageRadius, 10, false);
                }

                @Override
                public void draw(Bullet b){
                    //none
                }

                @Override
                public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
                    float size = Math.min(s.hitSize() / 3, 15);
                    if(Mathf.chance(0.32) || data.size < 8){
                        float sd = Mathf.random(size * 2f, size * 4);
                        WHFx.shuttle(60, moColorDark, frontColor, true, 1, 1)
                        .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), Mathf.chance(0.5) ? 45 : 135, moColor, sd);
                    }
                }
            };
        }};
        ColossusRefineCeramite = new CritBulletType(7f, 1000, name("pierce")){
            {
                rangeChange = 48;
                reloadMultiplier = 0.8f;
                shootPattern = new ShootPattern(){{
                    firstShotDelay = 30f;
                }};

                critMultiplier = 1.5f;
                critChance = 0.15f;

                backColor = trailColor = lightColor = lightningColor = hitColor = WHItems.refineCeramite.color.cpy();
                frontColor = WHItems.refineCeramite.color.cpy().lerp(Pal.accent, 0.5f);
                trailEffect = Fx.missileTrailSmokeSmall;
                trailParam = 6f;
                trailChance = 0.2f;
                trailInterval = 3;

                scaleLife = true;

                trailWidth = 5f;
                trailLength = 55;
                trailInterp = Interp.slope;

                lightning = 6;
                lightningLength = lightningLengthRand = 7;
                splashDamage = damage;
                buildingDamageMultiplier = 0.2f;
                lightningDamage = 50;
                splashDamageRadius = 120;
                scaledSplashDamage = true;
                despawnHit = true;
                /* collides = false;*/

                shrinkY = shrinkX = 0.33f;
                width = 17f;
                height = 55f;

                despawnShake = hitShake = 12f;

                hitEffect = new MultiEffect(
                WHFx.instRotation(120, hitColor, splashDamageRadius, 45, true),
                WHFx.square(200, hitColor, 20, splashDamageRadius, 10),
                WHFx.subEffect(140, splashDamageRadius + 12, 33, 34f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
                    float fout = Interp.pow2Out.apply(1 - fin);
                    for(int s : Mathf.signs){
                        Drawf.tri(x, y, 12 * fout, 45 * Mathf.curve(fin, 0, 0.1f) * WHFx.fout(fin, 0.25f), rot + s * 90);
                    }
                })));
                despawnEffect = WHFx.generalExplosion(120, hitColor, splashDamageRadius, 40, true);

                shootEffect = new MultiEffect(
                WHFx.shootLine(12, 30));
                smokeEffect = WHFx.instShoot(hitColor, frontColor);

                despawnSound = hitSound = explosionArtilleryShockBig;

                fragBullets = 8;
                fragBullet = new CritBulletType(2f, 220, "circle"){{

                    collidesTiles = false;
                    collides = false;
                    collidesAir = false;
                    scaleLife = true;

                    critMultiplier = 2;
                    buildingDamageMultiplier = 0.2f;
                    critChance = 0.1f;
                    width = height = 10f;
                    shrinkY = shrinkX = 0.7f;
                    backColor = trailColor = lightColor = lightningColor = hitColor = WHItems.refineCeramite.color.cpy();
                    frontColor = WHItems.refineCeramite.color.cpy().lerp(Pal.accent, 0.5f);
                    trailEffect = Fx.missileTrail;
                    trailParam = 3.5f;
                    splashDamage = damage;
                    splashDamageRadius = 40;

                    lifetime = 9f;

                    lightning = 2;
                    lightningLength = lightningLengthRand = 4;
                    lightningDamage = 30;

                    hitSoundVolume /= 2.2f;
                    despawnShake = hitShake = 4f;
                    despawnSound = hitSound = explosionDull;

                    trailWidth = 5f;
                    trailLength = 35;
                    trailInterp = Interp.slope;

                    despawnEffect = WHFx.blast(hitColor, 40f);
                    hitEffect = WHFx.generalExplosion(120, hitColor, splashDamageRadius * 1.5f, 40, false);
                    fragRandomSpread = 0;

                    fragBullets = 1;
                    fragBullet = new SizeDamageBullet(){
                        {
                            BulletType b = sizeDamageCreate;
                            b.lightning = 1;
                            b.lightningDamage = 20;
                            b.lightningLength = b.lightningLengthRand = 12;
                            hitSizeLightingScale = 1;

                            lifetime = 20;
                            maxDamageMultiple = 5;
                            hitSizeDamage = 60;
                            maxHitSizeScale = UnitTypes.vanquish.hitSize * 2;

                            hitSizeColor = hitColor = lightningColor = frontColor = trailColor = backColor = WHItems.refineCeramite.color.cpy();
                            trailLength = 12;
                            trailWidth = 2;
                            trailChance = 0.01f;
                            despawnEffect = hitEffect = WHFx.generalExplosion(60, WHItems.refineCeramite.color.cpy(), splashDamageRadius, 10, false);
                        }

                        @Override
                        public void draw(Bullet b){
                        }

                        @Override
                        public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
                            float size = Math.min(s.hitSize() / 3, 15);
                            if(Mathf.chance(0.32) || data.size < 8){
                                float sd = Mathf.random(size * 1.5f, size * 3);
                                WHFx.shuttle(60, hitColor, hitColor, false, 1, 1)
                                .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), Mathf.chance(0.5) ? 45 : 135, hitColor, sd);
                            }
                        }
                    };
                }};

                fragLifeMax = 5f;
                fragLifeMin = 1.5f;
                fragVelocityMax = 2f;
                fragVelocityMin = 0.35f;
            }

            public final SizeDamageBullet tearBullet = new SizeDamageBullet(){
                {
                    lifetime = 20;
                    maxDamageMultiple = 19;
                    hitSizeDamage = 200;
                    maxHitSizeScale = UnitTypes.conquer.hitSize * 2.5f;
                    damageInterp = Interp.pow3In;

                    hitSizeColor = hitColor = lightningColor = frontColor = trailColor = backColor = WHItems.refineCeramite.color.cpy();
                    despawnEffect = hitEffect = Fx.none;
                }

                @Override
                public void draw(Bullet b){
                }

                @Override
                public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
                    float size = Math.min(s.hitSize() / 2, maxHitSizeScale / 2);
                    if(Mathf.chance(0.32) || data.size < 8){
                        float sd = Mathf.random(size * 2, size * 3);
                        Color c = hitColor.cpy().lerp(Pal.accent, 0.5f);
                        WHFx.shuttle(120, c.cpy().lerp(Pal.coalBlack, 0.3f), c, true, 1, 1)
                        .at(s.getX(), s.getY(), 135, hitColor, sd);
                    }
                }
            };

            public final float shieldDamage = 3000;
            public final BulletType breakType = new EffectBulletType(3f){
                {
                    absorbable = true;
                    collides = false;
                    lifetime = 8f;
                    drawSize = 0;
                    hitColor = lightningColor = lightColor = trailColor = backColor = WHItems.refineCeramite.color.cpy();
                }

                @Override
                public void despawned(Bullet b){
                    WHFx.shuttle(60, hitColor, hitColor, false, 1, 1)
                    .at(b.x, b.y, 45 + 90, hitColor, b.damage / Vars.tilesize / 3);
                    /*  shuttleDark.at(b.x, b.y, 45, hitColor, b.damage / Vars.tilesize/4);*/
                    Effect.shake(b.damage / 100, b.damage / 100, b);
                }
            };

            @Override
            public void hit(Bullet b, float x, float y){
                super.hit(b, x, y);
                if(b.absorbed){
                    breakType.create(b, b.team, b.x, b.y, 0, shieldDamage, 0, 1, null);
                }
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc other, float initialHealth){
                super.hitEntity(b, other, initialHealth);
                if(!b.absorbed && other instanceof Healthc && other.hitSize() > UnitTypes.vanquish.hitSize){
                    tearBullet.create(b, b.team, other.getX(), other.getY(), 0, -1, 0, 1, null);
                }
            }
        };
        HydraTungsten = new CritBulletType(8.5f, 180){
            {
                shootPattern = new ShootBarrel(){{
                    shots = 4;
                    shotDelay = 13;
                    barrels = new float[]{
                    -56 / 4f, 80 / 4f, 0,
                    -17 / 4f, 92 / 4f, 0,
                    17 / 4f, 92 / 4f, 0,
                    56 / 4f, 80 / 4f, 0
                    };
                }};

                ammoMultiplier = 3f;
                reloadMultiplier = 1.5f;

                critChance = 0.3f;
                critMultiplier = 1.5f;

                pierceCap = 2;
                knockback = 0.2f;
                width = height = 16;
                spin = 1.5f;
                shrinkY = 0.3f;
                backSprite = "large-bomb-back";
                sprite = "mine-bullet";
                collidesGround = false;
                collidesTiles = false;
                shootEffect = Fx.shootBig2;
                smokeEffect = Fx.shootSmokeDisperse;
                frontColor = Color.white;

                backColor = hitColor = Color.sky.cpy();
                trailColor = Color.sky.cpy().lerp(Color.white, 0.3f);

                trailLength = 8;
                trailWidth = width / 4f;
                trailSinMag = 0.1f;
                trailSinScl = 12;

                lifetime = 58 * tilesize / speed;
                rotationOffset = 90f;
                trailRotation = true;

                trailInterval = 2f;
                trailChance = 0.15f;
                trailEffect = Fx.disperseTrail;

                hitEffect = despawnEffect = new MultiEffect(Fx.hitBulletColor, WHFx.square(30, hitColor, 4, 20, 4));
            }
        };

        HydraUranium = new CritBulletType(8, 200){
            {
                ammoMultiplier = 4;
                armorMultiplier = 0.8f;

                critChance = 0.1f;
                critMultiplier = 1.4f;
                buildingDamageMultiplier = 0.3f;
                collidesGround = false;

                lifetime = 58 * tilesize / speed;
                backColor = WHItems.uranium.color.cpy();
                Color c = hitColor = trailColor = backColor.cpy().lerp(Color.white, 0.3f);
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                width = 15;
                height = width * 3f;
                trailWidth = width / 4.5f;
                trailLength = 11;

                pierceCap = 2;
                armorMultiplier = 0.5f;

                status = WHStatusEffects.radiation;
                statusDuration = 20f;
                shootEffect = Fx.shootBigColor;
                smokeEffect = Fx.shootBigSmoke;
                critEffect = smokeTrail;

            /*trailEffect = WHFx.sineTrail(c, 18, 1.6f, 7f, 0.55f, 24f);
            trailInterval = 0.6f;
            trailChance = 1f;
            trailRotation = true;*/

                fragOnHit = false;
                fragRandomSpread = 0f;
                fragBullets = 1;
                fragBullet = new ShrapnelBulletType(){{
                    damage = 40;
                    length = 50;
                    width = 12;
                    toColor = c;
                    pierceCap = 3;
                    serrations = 2;
                    serrationSpaceOffset = 10f;
                }};

                hitEffect = despawnEffect = new MultiEffect(
                Fx.hitBulletColor,
                WHFx.square(30, hitColor, 6, 30, 4));
            }

            @Override
            public void init(Bullet b){
                super.init(b);
                WHFx.sineTrail(30, hitColor, 20, 120, 1.6f, 5, 5)
                .at(b.x, b.y, b.rotation(), hitColor);
            }

        };

        HydraCeramite = new CritBulletType(){
            {

                shootPattern = new ShootBarrel(){{
                    shots = 5;
                    shotDelay = 13;
                    barrels = new float[]{
                    -56 / 4f, 80 / 4f, 0,
                    -17 / 4f, 92 / 4f, 0,
                    17 / 4f, 92 / 4f, 0,
                    56 / 4f, 80 / 4f, 0
                    };
                }};

                rangeChange = 5 * tilesize;
                ammoMultiplier = 4f;
                armorMultiplier = 1.5f;

                critChance = 0.18f;
                critMultiplier = 1.5f;

                Color c = WHItems.ceramite.color.cpy();

                pierceCap = 2;
                damage = 280;
                splashDamage = 100;
                splashDamageRadius = 56;
                scaledSplashDamage = true;
                spin = 1.5f;

                knockback = 0.5f;

                speed = 3f;
                drag = -0.03f;
                lifetime = 58.52f;

                width = height = 16;
                shrinkY = 0.3f;
                backSprite = "large-bomb-back";
                sprite = "mine-bullet";
                collidesGround = false;
                collidesTiles = false;
                shootEffect = new MultiEffect(WHFx.shootLine(10, 30), Fx.shootBig2);
                smokeEffect = Fx.shootSmokeDisperse;
                frontColor = c.lerp(Color.white, 0.5f);
                backColor = trailColor = hitColor = c;
                trailLength = 8;
                trailWidth = width / 6f;
                trailSinMag = 0.1f;
                trailSinScl = 12;


                trailChance = 0.25f;
                trailInterval = 3f;
                trailEffect = WHFx.square(30, hitColor, 1, 10, 4);

                hitEffect = despawnEffect = new MultiEffect(
                WHFx.generalExplosion(15, hitColor, splashDamageRadius, 4, false),
                WHFx.square(30, hitColor, 4, 20, 4)
                );

                despawnSound = hitSound = Sounds.explosion;

                fragBullets = 3;
                fragBullet = new CritBulletType(){{
                    backColor = trailColor = hitColor = c;
                    despawnEffect = hitEffect = WHFx.square(30, hitColor, 4, splashDamageRadius, 4);
                    width = height = 10;
                    sprite = "circle";
                    trailWidth = 2f;
                    trailLength = 5;
                    critChance = 0.3f;
                    critMultiplier = 1.5f;
                    splashDamageRadius = 24;
                    splashDamage = damage = 50;
                    lifetime = 8;
                    speed = 5;
                    drag = 0.05f;
                }};
            }
        };

        HydraMolybdenumAlloy = new CritBulletType(){
            {
                shootPattern = new ShootBarrel(){{
                    shots = 3;
                    shotDelay = 20;
                    barrels = new float[]{
                    -56 / 4f, 80 / 4f, 0,
                    -17 / 4f, 92 / 4f, 0,
                    17 / 4f, 92 / 4f, 0,
                    56 / 4f, 80 / 4f, 0
                    };
                }};

                ammoMultiplier = 3f;
                reloadMultiplier = 0.85f;

                critChance = 0.12f;
                critMultiplier = 5;

                Color moColor = WHItems.molybdenumAlloy.color.cpy();
                Color moColorDark = moColor.cpy().lerp(Pal.gray, 0.1f);

                pierceArmor = true;
                pierceCap = 2;
                damage = 400;
                splashDamage = damage / 3;
                splashDamageRadius = 56;
                scaledSplashDamage = true;
                knockback = 1f;

                speed = 5f;
                drag = -0.025f;
                lifetime = 48.5f;
                width = 15;
                height = 45f;
                shrinkY = 0f;
                sprite = name("pierce");
                collidesGround = false;
                collidesTiles = false;
                shootEffect = new MultiEffect(WHFx.shootLine(10, 30), Fx.shootBig2);
                smokeEffect = Fx.shootSmokeDisperse;
                trailColor = hitColor = frontColor = moColor;
                backColor = moColorDark;

                trailLength = 12;
                trailWidth = width / 5f;
                trailSinMag = 0.1f;
                trailSinScl = 12;

                trailChance = 0.25f;
                trailInterval = 2f;
                trailEffect = WHFx.square(40, hitColor, 1, 15, 6);

                hitEffect = despawnEffect = new MultiEffect(
                WHFx.hitSparkAng(60, Pal.bulletYellowBack, hitColor, 4, splashDamageRadius / 2, 30, 2.2f, 12),
                WHFx.generalExplosion(60, hitColor, splashDamageRadius, 12, false),
                WHFx.square(60, hitColor, 12, splashDamageRadius, 8)
                );


                fragBullets = 1;
                fragBullet = new SizeDamageBullet(){
                    {
                        BulletType b = sizeDamageCreate;
                        b.collidesTiles = b.collidesGround = false;
                        collidesGround = false;
                        collidesTiles = false;

                        lifetime = 20;
                        maxDamageMultiple = 6f;
                        hitSizeDamage = 100;
                        maxHitSizeScale = UnitTypes.eclipse.hitSize;

                        hitSizeColor = hitColor = lightningColor = frontColor = trailColor = backColor = moColor;
                        trailLength = 12;
                        trailWidth = 2;
                        trailChance = 0.01f;
                        despawnEffect = hitEffect = Fx.none;
                    }

                    @Override
                    public void draw(Bullet b){
                    }

                    @Override
                    public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
                        float size = Math.min(s.hitSize() / 3, 10);
                        if(Mathf.chance(0.32) || data.size < 8){
                            float sd = Mathf.random(size * 1.5f, size * 3);
                            WHFx.hitSpark(60, Pal.blastAmmoBack, 4, splashDamageRadius / 2, Mathf.range(size / 12), Mathf.range(size / 4));
                            WHFx.shuttle(60, hitColor, hitColor, false, 1, 1)
                            .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), Mathf.chance(0.5) ? 45 : 135, hitColor, sd);
                        }
                    }
                };
            }
        };
        HydraRefineCeramite = new FlakBulletType(3f, 500){
            {
                shootPattern = new ShootBarrel(){{
                    shots = 2;
                    shotDelay = 20;
                    barrels = new float[]{
                    -56 / 4f, 80 / 4f, 0,
                    -17 / 4f, 92 / 4f, 0,
                    17 / 4f, 92 / 4f, 0,
                    56 / 4f, 80 / 4f, 0
                    };
                }};

                ammoMultiplier = 4f;
                reloadMultiplier = 0.6f;
                sprite = "missile-large";

                Color circleColor = WHItems.refineCeramite.color.cpy();
                Color circleColorDark = circleColor.cpy().lerp(Pal.gray, 0.15f);

                drag = -0.06f;
                /*lifetime = (58 * tilesize * 0.75f) / speed;*/
                lifetime = 35.6f;
                width = 12f;
                height = 22f;
                collidesGround = false;
                collidesTiles = false;

                hitSize = 7f;
                shootEffect = new MultiEffect(WHFx.shootLine(10, 30), Fx.shootBig2);

                smokeEffect = Fx.shootSmokeDisperse;
                hitColor = backColor = trailColor = lightningColor = circleColor;
                frontColor = circleColor.lerp(Color.white, 0.3f);
                trailWidth = 3f;
                trailLength = 12;
                hitEffect = despawnEffect = Fx.hitBulletColor;

                trailEffect = Fx.colorSpark;
                trailRotation = true;
                trailInterval = 3f;

                homingPower = 0.17f;
                homingDelay = 19f;
                homingRange = 160f;

                explodeRange = 100f;
                explodeDelay = 0f;

                flakInterval = 20f;
                despawnShake = 3f;

                bulletInterval = 6f;
                intervalBullet = new LightningBulletType(){{
                    lightningColor = circleColor;
                    lightningCone = 10f;
                    lightningLength = 8;
                    lightningLengthRand = 8;
                    damage = 50;
                }};

                fragRandomSpread = 0f;
                fragBullets = 1;
                fragBullet = new LightingLaserBulletType(200){{
                    colors = new Color[]{circleColor.cpy().a(0.4f), circleColor, Color.white};
                    buildingDamageMultiplier = 0.25f;
                    width = 19f;
                    hitColor = lightningColor = circleColor;
                    hitEffect = Fx.hitLancer;
                    sideAngle = 175f;
                    sideWidth = 1f;
                    sideLength = 40f;
                    lifetime = 22f;
                    drawSize = 400f;
                    length = 58 * tilesize * 0.25f;
                    collidesGround = false;
                    collidesTiles = false;
                    pierceCap = 2;
                    optimalLifeFract = 1f;

                    despawnHit = false;
                    fragOnHit = true;

                    fragRandomSpread = 0f;
                    fragBullets = 1;
                    fragBullet = new LaserBulletType(150){{
                        colors = new Color[]{circleColor.cpy().a(0.4f), circleColor, Color.white};
                        buildingDamageMultiplier = 0.25f;
                        width = 19f;
                        hitColor = lightningColor = circleColor;
                        hitEffect = Fx.hitLancer;
                        sideAngle = 175f;
                        sideWidth = 1f;
                        sideLength = 40f;
                        lifetime = 22f;
                        drawSize = 400f;
                        knockback = 4;
                        length = 58 * tilesize * 0.3f;
                        collidesGround = false;
                        collidesTiles = false;
                        pierceCap = 8;
                        optimalLifeFract = 1f;

                        fragBullets = 1;
                        fragBullet = new SizeDamageBullet(){
                            {
                                BulletType b = sizeDamageCreate;
                                b.lightning = 1;
                                b.lightningDamage = 20;
                                b.lightningLength = b.lightningLengthRand = 10;
                                b.collidesGround = b.collidesTiles = false;
                                b.lightningColor = circleColor;
                                b.shieldDamageMultiplier = 0.1f;
                                collidesGround = false;
                                collidesTiles = false;
                                hitSizeLightingScale = 4;
                                damageInterp = Interp.pow2In;

                                lifetime = 20;
                                maxDamageMultiple = 4;
                                hitSizeDamage = 100;
                                maxHitSizeScale = UnitTypes.eclipse.hitSize * 1.5f;

                                hitSizeColor = hitColor = lightningColor = frontColor = trailColor = backColor = circleColor;
                                trailLength = 12;
                                trailWidth = 2;
                                trailChance = 0.01f;
                                despawnEffect = hitEffect = WHFx.square(30, hitColor, 4, 20, 4);
                            }

                            @Override
                            public void draw(Bullet b){
                            }

                            @Override
                            public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
                                float size = Math.min(s.hitSize() / 3, 10);
                                if(Mathf.chance(0.32) || data.size < 8){
                                    float sd = Mathf.random(size * 1.5f, size * 3);
                                    WHFx.hitSpark(60, hitColor, 4, splashDamageRadius / 2, Mathf.range(size / 12), Mathf.range(size / 4));
                                    WHFx.shuttle(60, circleColorDark, hitColor, true, 1, 1)
                                    .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), Mathf.chance(0.5) ? 45 : 135, hitColor, sd);
                                }
                            }
                        };
                    }};

                    intervalBullets = 1;
                    fragSpread = fragRandomSpread = intervalRandomSpread = 0f;
                    bulletInterval = 20f;

                    splashDamage = 0f;
                    hitEffect = Fx.hitSquaresColor;
                }};
            }
        };

        ReckoningTungsten = new CritBulletType(12f, 120){{
            lifetime = 51 * tilesize / speed;
            hitSize = 10;
            width = 12f;
            height = 26f;
            shootEffect = Fx.shootBig;
            ammoMultiplier = 4f;
            pierceCap = 3;
            pierceBuilding = true;
            knockback = 3.5f;
            trailEffect = Fx.disperseTrail;
            trailRotation = true;
            trailInterval = 4f;
            backColor = hitColor = trailColor = Pal.tungstenShot;
            frontColor = WHPal.SkyBlueF;

            hitEffect = despawnEffect = new MultiEffect(WHFx.square(20, hitColor, 4, 30, 5), Fx.hitBulletColor);

        }};

        ReckoningCeramite = new CritBulletType(8, 100){{
            rangeChange = 24f;
            armorMultiplier = 1.3f;
            lifetime = 42.9f;//51 * tilesize;;
            drag = -0.008f;
            reloadMultiplier = 0.8f;
            ammoMultiplier = 4;
            hitSize = 10;
            splashDamage = damage / 2;
            splashDamageRadius = 36;
            scaledSplashDamage = true;
            width = 15;
            height = width * 2.5f;

            trailLength = 5;
            trailWidth = width / 6f;
            trailSinMag = 0.1f;
            trailSinScl = 12;
            pierceCap = 2;
            lightColor = backColor = hitColor = trailColor = WHItems.ceramite.color.cpy();
            frontColor = backColor.cpy().lerp(Color.white, 0.7f);
            trailEffect = WHFx.square(40, hitColor.cpy().lerp(Color.white, 0.3f), 2, 20, 3.5f);
            trailChance = 0.05f;
            knockback = 1f;

            shootEffect = new MultiEffect(Fx.shootTitan, WHFx.shootLine(20, 45));

            hitEffect = despawnEffect =
            new MultiEffect(WHFx.square(20, hitColor, 4, 30, 5),
            WHFx.generalExplosion(10, hitColor, splashDamageRadius * 1.3f, 5, false),
            WHFx.trailCircleHitSpark(25, hitColor, 3, splashDamageRadius * 2, 1.5f, 10));

            fragBullets = 3;
            fragBullet = new CritBulletType(5f, 80){{
                armorMultiplier = 1.3f;
                sprite = "missile-large";
                width = 5f;
                height = 7f;
                lifetime = 15;
                trailLength = 5;
                trailWidth = 1;
                hitSize = 4f;
                pierceCap = 3;
                pierce = true;
                pierceBuilding = true;
                hitColor = backColor = trailColor = WHItems.ceramite.color.cpy();
                frontColor = Color.white;
                trailWidth = 1.7f;
                trailLength = 3;
                drag = 0.01f;
                despawnEffect = hitEffect = Fx.hitBulletColor;
            }};

        }};


        ReckoningMolybdenumAlloy = new TrailFadeBulletType(5f, 400, name("pierce")){{
            Color moColor = WHItems.molybdenumAlloy.color.cpy();
            Color moColorDark = moColor.cpy().lerp(Pal.gray, 0.1f);
            splashDamageRadius = 64;
            splashDamage = 80;
            reloadMultiplier = 0.5f;
            ammoMultiplier = 8;

            tracers = 1;
            tracerStroke = 2;
            tracerFadeOffset = 4;
            tracerStrokeOffset = 7;
            tracerRandX = 5;
            tracerUpdateSpacing = 1.1f;
            addBeginPoint = false;
            hitBlinkTrail = false;
            despawnBlinkTrail = false;

            drag = -0.01f;
            lifetime = 60f;//51 * tilesize

            hitShake = 1f;
            hitSound = Sounds.explosion;

            status = WHStatusEffects.tear;
            statusDuration = 120f;

            shootEffect =
            new MultiEffect(
            Fx.shootTitan, WHFx.shootLine(30, 20),
            WHFx.shootCircleSmall(moColor)
            );
            trailEffect = WHFx.square(30, moColor, 1, 10, 5);
            trailChance = 0.15f;

            shrinkY = shrinkX = 0;
            trailSinScl = 2.5f;
            trailSinMag = 0.15f;

            trailLength = 14;
            trailWidth = 12 / 4.5f;
            width = 12;
            height = 32;

            hitColor = lightningColor = backColor = trailColor = moColor;
            frontColor = moColor.cpy().lerp(Color.white, 0.5f);
            despawnSound = hitSound = explosionDull;
            hitSoundVolume /= 2.2f;
            hitEffect =
            new MultiEffect(
            WHFx.hitSparkAng(30, Pal.bulletYellowBack, hitColor, 4, splashDamageRadius, 20, 2, 12),
            WHFx.generalExplosion(10, moColor, splashDamageRadius, 15, false),
            WHFx.trailHitSpark(25, hitColor, 3, splashDamageRadius, 1.5f, 10)
            );
            despawnEffect = new MultiEffect(
            WHFx.square(60, moColor, 15, 40, 5),
            WHFx.shuttle(60, moColorDark, frontColor, true, 30, 45),
            WHFx.shuttle(60, moColorDark, frontColor, true, 30, 45 + 90f).startDelay(15));

            fragRandomSpread = 0;
            fragBullets = 1;
            fragBullet = new SizeDamageBullet(){
                {
                    sizeDamageCreate.lightning = 1;
                    sizeDamageCreate.lightningDamage = 12;
                    sizeDamageCreate.lightningLength = 10;
                    sizeDamageCreate.lightningColor = moColor;
                    damageInterp = Interp.linear;

                    lifetime = 20;
                    maxDamageMultiple = 9;
                    hitSizeDamage = 100;
                    maxHitSizeScale = UnitTypes.oct.hitSize * 1.5f;
                    hitSizeLightingScale = 3;

                    hitColor = lightningColor = frontColor = trailColor = backColor = moColor;
                    trailLength = 12;
                    trailWidth = 2;
                    trailChance = 0.01f;
                    despawnEffect = hitEffect = Fx.none;
                }

                @Override
                public void draw(Bullet b){
                    //none
                }

                @Override
                public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
                    float size = Math.min(s.hitSize() / 3, 15);
                    if(Mathf.chance(0.35f)){
                        float sd = Mathf.random(size * 2f, size * 4);
                        WHFx.hitSparkAng(45, Pal.bulletYellowBack, hitColor, 4, splashDamageRadius, 20, 2, 12)
                        .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), b.rotation());
                        WHFx.shuttle(30, moColorDark, frontColor, true, 1, 1)
                        .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), Mathf.chance(0.5) ? 45 : 135, moColor, sd);
                    }
                }
            };
        }

            @Override
            public void update(Bullet b){
                super.update(b);
                if(b.fin() < 0.5f && Mathf.chanceDelta(0.04f)){
                    WHFx.tri(40, hitColor, 2, 20, 5f).at(b.x, b.y);
                }
            }
        };

        ReckoningCulverCrystal = new CritBulletType(7, 350){
            {
                ammoMultiplier = 8f;
                reloadMultiplier = 0.6f;

                critChance = 0.2f;
                critMultiplier = 1.25f;
                armorMultiplier = 0.7f;

                splashDamage = -1;
                splashDamageRadius = 32;
                status = WHStatusEffects.rock;
                statusDuration = 120f;

                pierceCap = 2;
                knockback = 0.2f;
                speed = 6;
                width = 15;
                height = width * 1.2f;
                shrinkY = 0.3f;
                sprite = name("tall");

                Color c = hitColor = backColor = trailColor = lightningColor = WHItems.culverCrystal.color.cpy();
                frontColor = backColor.cpy().lerp(Color.white, 0.5f);

                shootEffect = new MultiEffect(
                WHFx.shootLine(50, 30),
                Fx.shootBigColor
                );
                smokeEffect = Fx.shootSmokeDisperse;

                trailLength = -1;
                lightning = 1;
                lightningDamage = 35;
                lightningLength = lightningLengthRand = 6;

                lifetime = 51 * tilesize / speed;
                trailRotation = true;

                trailInterval = 20f;
                trailChance = 0.03f;
                trailEffect = new MultiEffect(
                WHFx.hitPoly(60, frontColor, backColor, 1, 20, 5, 5, 60)).rotWithParent(true);

                hitEffect = new MultiEffect(
                WHFx.hitSparkAng(30, Pal.bulletYellowBack, hitColor, 5, 30, 30, 1.8f, 12f),
                WHFx.square(30, hitColor, 8, splashDamageRadius, 5)
                );
                despawnEffect = new MultiEffect(
                WHFx.trailHitSpark(30, hitColor, 10, splashDamageRadius, 1.5f, 12f),
                WHFx.linePolyOut(30, hitColor, splashDamageRadius, 2, 4, 0)
                );

            }

            @Override
            public void update(Bullet b){
                super.update(b);
               /* if(Mathf.chanceDelta(0.08f) && b.fin() > 0.1){
                    Tmp.v1.rnd(Mathf.random(20));
                    WHFx.sineTrail(60, hitColor, 15, 60, 1.5f, 2, 3)
                    .rotWithParent(true).at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, b.rotation(), hitColor, b);
                }*/
            }

            @Override
            public void createSplashDamage(Bullet b, float x, float y){
                super.createSplashDamage(b, x, y);
            }

            @Override
            public void draw(Bullet b){
                Draw.color(hitColor);
                Draw.z(Layer.bullet);
                rand.setSeed(b.id);
                var rot = b.fin() * 600 * rand.random(1f, 2f);
                Drawn.drawCrystal(
                b.x, b.y, 40f * b.fout(), 10f * b.fout(), 8f, 0f, 0f, 0.8f, EFFECT_MASK,
                Layer.bullet, rot, b.rotation(), Tmp.c1.set(hitColor).a(0.6f), hitColor
                );
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
            }
        };

        ReckoningSealedPromethium = new MultiTrailBulletType(6, 200, name("pierce")){
            {
                Color color = WHPal.SkyBlue.cpy().lerp(Pal.techBlue, 0.3f).lerp(Color.sky, 0.3f);

                ammoMultiplier = 12;
                reloadMultiplier = 2;
                subTrails = 1;
                offset = 0.1f;
                width = 6f;
                height = width * 2f;
                trailLength = 16;
                trailWidth = width / 3f;
                rangeChange = 6 * tilesize;
                lifetime = (51 * tilesize + rangeChange) / speed;
                rotateSpeed = 0.008f;
                weaveScale = 20;
                weaveMag = 0.5f;
                homingDelay = 30;
                homingPower = 0.08f;

                pierceCap = 3;

                hitColor = lightningColor = frontColor = trailColor = backColor = color;

                trailEffect = WHFx.square(30, color, 2, 10, 4);
                trailChance = 0.18f;

                followAimSpeed = 1f;
                homingRange = 120f;

                splashDamage = 80f;
                splashDamageRadius = 30;

                hitEffect = despawnEffect = new MultiEffect(
                WHFx.generalExplosion(10, color, splashDamageRadius, 5, false),
                WHFx.square(20, hitColor, 10, splashDamageRadius, 6),
                WHFx.trailCircleHitSpark(40, hitColor, 3, splashDamageRadius * 2, 1.2f, 10)
                );
            }

            public final float angelRandOffset = 3f;

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
                    }else{
                        b.vel.rotate(Mathf.random(-angelRandOffset, angelRandOffset));
                    }
                }

                if(followAimSpeed > 0f && b.owner instanceof ControlBlock c){
                    Unit u = c.unit();
                    float angle = b.angleTo(u.aimX, u.aimY);
                    b.vel.setAngle(Angles.moveToward(b.vel.angle(), angle, followAimSpeed * Time.delta));
                }
            }


            @Override
            public void updateTrail(Bullet b){
                super.updateTrail(b);
                if(!headless && trailLength > 0){
                    if(b.trail == null){
                        b.trail = new Trail(trailLength);
                    }
                    b.trail.length = trailLength;
                    WHFx.rand.setSeed(b.id);
                    float r = WHFx.rand.random(0.5f, 1);
                    Tmp.v1.trns(360 * 5 * b.fin() * r, 2);
                    b.trail.update(b.x + Tmp.v1.x, b.y + Tmp.v1.y, trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
                }
            }
        };

        AnnihilateBullet = new TrailFadeBulletType(8, 1800, name("energy-bullet")){
            {
                Color f = WHPal.SkyBlueF.cpy().lerp(Color.sky, 0.3f);

                drag = -0.008f;
                lifetime = 51.2f;

                splashDamage = damage / 2;
                splashDamageRadius = 80;
                scaledSplashDamage = true;

                status = WHStatusEffects.plasma;
                statusDuration = 90;

                pierceCap = 2;

                width = 30;
                height = 50;
                shrinkX = shrinkY = 0;
                hitShake = 5;
                frontColor = f.cpy().lerp(Color.white, 0.15f);

                backColor = trailColor = hitColor = lightningColor = f;

                trailLength = 15;
                trailWidth = width / 5f;
                trailSinMag = 0.1f;
                trailSinScl = 12;

                trailChance = trailInterval = 0.5f;
                trailEffect = WHFx.square(30, f, 1, 18, 6);

                despawnBlinkTrail = false;
                tracers = 2;
                tracerStroke = 2;
                tracerSpacing = 8;
                tracerRandX = 6;
                tracerFadeOffset = 4;
                tracerStrokeOffset = 8;
                tracerUpdateSpacing = 0.9f;

                hitSound = despawnSound = explosionPlasmaSmall;
                hitSoundVolume = 0.08f;

                lightning = 3;
                lightningLengthRand = lightningLength = 12;
                lightningDamage = 80;

                chargeEffect = new MultiEffect(
                WHFx.genericChargeCircle(60, f, 10, 100).layer(Layer.effect),
                trailCharge(60, f, 15, 2, 90, 3).layer(Layer.effect),
                trailCharge(30, f, 15, 2, 90, 3).layer(Layer.effect),
                trailCharge2(60, f, 15, 2, 90, 3).layer(Layer.effect),
                trailCharge2(30, f, 15, 2, 90, 3).layer(Layer.effect),
                lineCircleIn(30, f, 80, 3).startDelay(30)
                );

                shootEffect = new MultiEffect(
                WHFx.shootLine(10, 30),
                WHFx.plasmaShoot(60, f, 20, 20)
                );

                despawnEffect = hitEffect = WHFx.instHit(f, true, 4, 60);

                fragBullets = 1;
                fragRandomSpread = 0;
                fragVelocityMax = fragVelocityMin = 0.1f;
                fragLifeMax = fragLifeMin = 1f;
                fragBullet = WHBulletsOther.AnnihilateFrag;
            }
        };

        EraseMolybdenumAlloy = new CritBulletType(8, 2500){
            {

                critMultiplier = 3;
                critChance = 0.2f;

                sprite = name("pierce");
                Color moColor = WHItems.molybdenumAlloy.color.cpy();
                Color moColorDark = moColor.cpy().lerp(Pal.gray, 0.1f);

                splashDamageRadius = 64;
                splashDamage = damage / 4;
                status = WHStatusEffects.tear;
                statusDuration = 120;

                pierceCap = 3;
                ammoMultiplier = 3;
                reloadMultiplier = 1.1f;
                drag = -0.03f;
                lifetime = 38.2f;

                hitShake = 4f;
                collidesAir = false;
                smokeEffect = new MultiEffect(Fx.shootBigSmoke2, Fx.shootSmokeDisperse);
                shootEffect =
                new MultiEffect(
                Fx.shootTitan, WHFx.shootLine(60, 30),
                WHFx.shootCircleSmall(moColor),
                WHFx.instShoot(moColor.lerp(Color.lightGray, 0.3f), moColor)
                );

                width = 17;
                height = 38;

                trailEffect =
                new MultiEffect(WHFx.square(30, moColor, 2, 15, 5),
                new Effect(30, e -> {
                    for(int i = 0; i < 2; i++){
                        color(i == 0 ? moColor : moColorDark);

                        float m = i == 0 ? 1f : 0.5f;

                        float rot = e.rotation + 180f;
                        float w = 15f * e.fout() * m;
                        Drawf.tri(e.x, e.y, w, (30f + Mathf.randomSeedRange(e.id, 15f)) * m, rot);
                        Drawf.tri(e.x, e.y, w, 10f * m, rot + 180f);
                    }
                    Drawf.light(e.x, e.y, 60f, moColorDark, 0.6f * e.fout());
                }));
                trailRotation = true;
                trailInterval = 1f;
                trailChance = 0.5f;

                shrinkY = shrinkX = 0;
                trailSinScl = 2.5f;
                trailSinMag = 0.15f;

                trailLength = 14;
                trailWidth = 17 / 5f;

                hitColor = lightningColor = backColor = trailColor = moColor;
                frontColor = moColor.cpy().lerp(Color.white, 0.5f);

                despawnSound = hitSound = explosionArtilleryShock;
                hitSoundVolume /= 2.2f;
                hitEffect =
                new MultiEffect(
                WHFx.instHit(moColor, true, 3, splashDamageRadius * 0.8f),
                WHFx.hitSpark(45, moColor, 20, splashDamageRadius, 2, 6f)
                );
                despawnEffect = new MultiEffect(
                WHFx.square(60, moColor, 15, 40, 5),
                WHFx.shuttle(60, moColorDark, frontColor, true, 60, 45),
                WHFx.shuttle(60, moColorDark, frontColor, true, 60, 45 + 90f),
                WHFx.shuttle(60, moColorDark, frontColor, true, 120, 90),
                WHFx.generalExplosion(120, moColor, splashDamageRadius, 30, false)
                );

                parts.addAll(
                new FlarePart(){{
                    color1 = moColor;
                    color2 = moColorDark;
                    sides = 2;
                    rotation = 25f;
                    radius = 0f;
                    radiusTo = 60;
                    stroke = 8f;
                    progress = PartProgress.life.slope().curve(Interp.pow2In);
                }}
                );

            }

            final Color moColor = WHItems.molybdenumAlloy.color.cpy();
            final Color moColorDark = moColor.cpy().lerp(Pal.gray, 0.1f);
            public final BulletType tearBullet = new SizeDamageBullet(){
                {
                    BulletType b = sizeDamageCreate;
                    b.lightning = 2;
                    b.lightningDamage = 30;
                    b.lightningLength = b.lightningLengthRand = 15;
                    b.lightningColor = b.hitColor = moColorDark;
                    damageInterp = Interp.pow2In;

                    lifetime = 20;
                    maxDamageMultiple = 15;
                    hitSizeDamage = 200;
                    maxHitSizeScale = UnitTypes.conquer.hitSize * 1.7f;
                    hitSizeLightingScale = 5;
                    splashDamageRadius = 26;

                    hitColor = lightningColor = frontColor = trailColor = backColor = moColor;
                    trailLength = 12;
                    trailWidth = 2;
                    trailChance = 0.01f;
                    despawnEffect = hitEffect = Fx.none;
                }

                @Override
                public void draw(Bullet b){
                    //none
                }

                @Override
                public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
                    float size = Math.min(s.hitSize() / 3, 15);
                    if(Mathf.chance(0.32) || data.size < 8){
                        float sd = Mathf.random(size * 2f, size * 4);
                        WHFx.shuttle(60, moColorDark, frontColor, true, 1, 1)
                        .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), 135, moColor, sd);
                        WHFx.shuttle(60, moColorDark, frontColor, true, 1, 1)
                        .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), 45, moColor, sd);
                        WHFx.lineCircleOut(20, moColor, s.hitSize(), 2f).at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size));
                    }
                }
            };

            @Override
            public void hitEntity(Bullet b, Hitboxc other, float initialHealth){
                super.hitEntity(b, other, initialHealth);
                float sd = Mathf.range(other.hitSize());
                if(other instanceof Healthc){
                    float size = 20;
                    float targetSize = other.hitSize();
                    int bulletCount = Math.min(8, (int)(targetSize / size));
                    if(bulletCount > 0){
                        for(int i = 0; i < bulletCount; i++){
                            tearBullet.create(b, b.team, other.getX() + sd, other.getY() + sd, 0, -1, 0, 1, null);
                        }
                    }
                }
            }
        };

        EraseAdamantium = new TrailFadeBulletType(8, 3000, name("pierce")){
            {
                shootPattern = new ShootSpread(){{
                    firstShotDelay = 60;
                    shots = 2;
                    spread = 10;
                    shotDelay = 40;
                }};

                reloadMultiplier = 0.8f;
                ammoMultiplier = 2f;

                float spr = splashDamageRadius = 120;
                splashDamage = damage;
                lightningDamage = 90;
                lightning = 3;
                lightningLength = lightningLengthRand = 13;

                Color c = WHItems.adamantium.color.cpy().lerp(Pal.redLight, 0.2f);
                knockback = 3f;
                height = width * 3f;
                width = 18;
                rangeChange = 3 * tilesize;
                lifetime = (82 * tilesize + rangeChange) / speed;


                collidesAir = false;
                scaleLife = true;
                scaledSplashDamage = true;
                backColor = hitColor = trailColor = lightningColor = c;
                frontColor = c.cpy().lerp(Color.white, 0.5f);

                hitEffect = new MultiEffect(WHFx.trailHitSpark(90, hitColor, 10, splashDamageRadius, 1.5f, 10),
                WHFx.square(90, hitColor, 10, splashDamageRadius, 5));
                despawnEffect = new MultiEffect(
                new Effect(90, e -> {
                    float intensity = 0.25f, size = splashDamageRadius;
                    color(c);
                    Fill.circle(e.x, e.y, intensity * size * e.fout(Interp.pow3Out));
                    float scl = 0.3f * size;
                    Rand rand = new Rand(e.id);
                    randLenVectors(e.id, 8, scl / 3, scl * (1.0F + e.fout(Interp.circleOut)) / 1.5f, (x, y) -> {
                        float angle = Mathf.angle(x, y);
                        float width = e.foutpowdown() * rand.random(scl / 6.0F, scl / 3.0F);
                        float length = rand.random(scl, scl * 2) * e.fout(Interp.circleOut);
                        color(c);
                        Drawn.tri(e.x + x, e.y + y, width, scl / 3.0F * e.fout(Interp.circleOut), angle - 180);
                        Drawn.tri(e.x + x, e.y + y, width, length, angle);
                        color(Pal.coalBlack.cpy());
                        width *= e.fout();
                        Drawn.tri(e.x + x, e.y + y, width / 2.0F, scl / 3.0F * e.fout(Interp.circleOut) * 0.9F * e.fout(), angle - 180);
                        Drawn.tri(e.x + x, e.y + y, width / 2.0F, length / 1.5F * e.fout(), angle);
                    });
                    color(Pal.coalBlack.cpy());
                    Fill.circle(e.x, e.y, intensity * size * 0.6f * e.fout(Interp.pow3Out));
                    Drawf.light(e.x, e.y, size, c, 0.8f * e.fout());
                }).layer(Layer.effect + 0.001f),
                WHFx.circleOut(90, c, splashDamageRadius * 1.2f),
                Fx.titanExplosionSmall);

                hitSound = explosionTitan;

                despawnBlinkTrail = true;
                tracers = 2;
                tracerStroke = 2;
                tracerSpacing = 8;
                tracerRandX = 4;
                tracerFadeOffset = 4;
                tracerStrokeOffset = 9;
                tracerUpdateSpacing = 2;

                trailLength = 15;
                trailWidth = 3.35f;
                trailSinScl = 2.5f;
                trailSinMag = 0.5f;
                trailEffect =
                new MultiEffect(
                Fx.vapor,
                new Effect(18, e -> {
                    for(int i = 0; i < 2; i++){
                        color(i == 0 ? c : c.lerp(Pal.redLight, 0.5f));

                        float m = i == 0 ? 1f : 0.5f;

                        float rot = e.rotation + 180f;
                        float w = 15f * e.fout() * m;
                        Drawf.tri(e.x, e.y, w, (30f + Mathf.randomSeedRange(e.id, 15f)) * m, rot);
                        Drawf.tri(e.x, e.y, w, 10f * m, rot + 180f);
                    }
                    Drawf.light(e.x, e.y, 60f, c, 0.6f * e.fout());
                }));
                trailInterval = 4f;
                despawnShake = 7f;

                shootEffect = Fx.shootTitan;
                smokeEffect = Fx.shootSmokeTitan;
                trailRotation = true;

                trailInterp = v -> Math.max(Mathf.slope(v), 0.8f);
                shrinkX = 0.2f;
                shrinkY = 0.1f;
                buildingDamageMultiplier = 0.5f;
                fragLifeMin = 1.5f;

                fragBullets = 6;
                fragBullet = new CritBulletType(3, 800, "circle"){{

                    collides = false;
                    height = width = 6f;
                    shrinkX = shrinkY = 0;
                    trailLength = 10;
                    trailWidth = width / 2f;

                    trailEffect = Fx.vapor;
                    trailChance = 0.15f;
                    trailInterval = 3f;

                    collidesAir = true;

                    lifetime = spr * 1.3f / speed;
                    knockback = 0.5f;
                    splashDamageRadius = 64f;
                    splashDamage = damage;
                    lightning = 3;
                    lightningDamage = 50;
                    lightningLength = lightningLengthRand = 10;
                    scaledSplashDamage = true;
                    pierceArmor = true;
                    lightningColor = frontColor = backColor = hitColor = c;
                    buildingDamageMultiplier = 0.3f;

                    fragBullets = 1;
                    fragBullet = new SizeDamageBullet(){
                        {
                            BulletType b = sizeDamageCreate;
                            b.lightning = 2;
                            b.lightningDamage = 30;
                            b.lightningLength = b.lightningLengthRand = 15;
                            b.lightningColor = b.hitColor = c;
                            damageInterp = Interp.pow2In;

                            lifetime = 20;
                            maxDamageMultiple = 5;
                            hitSizeDamage = 300;
                            maxHitSizeScale = UnitTypes.reign.hitSize * 1.5f;
                            hitSizeLightingScale = 5;
                            splashDamageRadius = 70;

                            hitColor = lightningColor = frontColor = trailColor = backColor = c;
                            trailLength = 12;
                            trailWidth = 2;
                            trailChance = 0.01f;
                            despawnEffect = hitEffect = Fx.none;
                        }

                        @Override
                        public void draw(Bullet b){
                            //none
                        }

                        @Override
                        public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
                            float size = Math.min(s.hitSize() / 3, 25);
                            if(Mathf.chance(0.32) || data.size < 8){
                                float sd = Mathf.random(size * 2f, size * 4);
                                WHFx.shuttle(60, c, c.cpy().lerp(Color.black, 0.7f), true, 1, 1)
                                .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), 135, c, sd);
                                WHFx.shuttle(60, c, c.cpy().lerp(Color.black, 0.7f), true, 1, 1)
                                .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), 45, c, sd);
                                WHFx.lineCircleOut(20, c, s.hitSize(), 2f).at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size));
                            }
                        }
                    };

                    despawnEffect = hitEffect = new MultiEffect(new Effect(30, 50f, e -> {
                        color(e.color);
                        stroke(e.fout() * 2f);
                        float circleRad = 6f + e.finpow() * splashDamageRadius;
                        Lines.circle(e.x, e.y, circleRad);

                        rand.setSeed(e.id);
                        for(int i = 0; i < 8; i++){
                            float angle = rand.random(360f);
                            float lenRand = rand.random(0.5f, 1f);
                            Tmp.v1.trns(angle, circleRad);

                            for(int s : Mathf.signs){
                                Drawf.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, e.foutpow() * 15f, e.fout() * 20f * lenRand + 6f, angle + 90f + s * 90f);
                            }
                        }
                    }), WHFx.instRotation(30, hitColor, splashDamageRadius * 1.5f, 45, false),
                    WHFx.generalExplosion(30, c, splashDamageRadius, 10, false),
                    Fx.titanExplosionFrag);
                }};
            }

            public final BulletType lb = new EffectBulletType(){
                {
                    lifetime = 80;
                    splashDamage = 500;
                }

                @Override
                public void update(Bullet b){
                    super.update(b);
                    Vec2 v = new Vec2().set(Tmp.v1);
                    if(b.timer(2, 8)){
                        PositionLightning.createRange(b, v, b.team, 130, 8, hitColor, false, splashDamage, 0, 1.5f, 2,
                        p -> {
                            WHFx.generalExplosion(20, hitColor, 30, 2, false).at(p.getX(), p.getY());
                        });
                    }
                }
            };

            @Override
            public void despawned(Bullet b){
                super.despawned(b);
                float spacing = 10;
                Vec2 v = new Vec2().set(b);
                lb.create(b, b.team, v.x, v.y, 0, 0, 0, 1, null);
                if(status != StatusEffects.none){
                    Damage.status(b.team, v.x, v.y, splashDamageRadius, WHStatusEffects.scare, 60, collidesAir, collidesGround);
                    Damage.status(b.team, v.x, v.y, splashDamageRadius, WHStatusEffects.tear, 60, collidesAir, collidesGround);
                    Damage.status(b.team, v.x, v.y, splashDamageRadius, WHStatusEffects.armorFracture, 120, collidesAir, collidesGround);
                }
                for(int k = 0; k < 5; k++){
                    int finalK = k;
                    Time.run(k * spacing, () -> {
                        for(int j : Mathf.signs){
                            Drawn.randFadeLightningEffect(v.x, v.y, splashDamageRadius * 1.5f, 12, hitColor, j > 0);
                            WHFx.triSpread(90, hitColor, 1, finalK * 8 + 5, splashDamageRadius * 1.5f).at(v.x, v.y);
                        }
                    });
                }
            }
        };

        HectorAdamantium = new CritBulletType(6f, 4000, name("pierce")){
            {
                critMultiplier = 1.8f;
                critChance = 0.3f;

                backColor = trailColor = lightColor = lightningColor = hitColor = WHItems.adamantium.color.cpy();
                frontColor = WHItems.adamantium.color.cpy().lerp(Pal.accent, 0.5f);
                lifetime = 51.6f;//720
                drag = -0.03f;

                trailWidth = 5f;
                trailLength = 20;
                trailInterp = Interp.slope;

                lightning = 12;
                lightningLength = lightningLengthRand = 7;
                splashDamage = damage;
                buildingDamageMultiplier = 0.2f;
                lightningDamage = 90;
                splashDamageRadius = 140;
                scaledSplashDamage = true;
                despawnHit = true;
                pierceCap = 2;
                /* collides = false;*/

                shrinkY = shrinkX = 0.33f;
                width = 30f;
                height = 75f;

                despawnShake = hitShake = 25f;

                trailEffect = new MultiEffect(
                WHFx.instTrail(hitColor, 40, false),
                Fx.missileTrailSmokeSmall);
                trailRotation = true;
                trailChance = 0.1f;
                trailInterval = 12;

                float a = 1.2f, life1 = 180f, life2 = 300f;
                hitEffect = new MultiEffect(
                WHFx.blast(hitColor, splashDamageRadius),
                WHFx.instRotation(life1, hitColor, splashDamageRadius, 45, true),
                WHFx.square(life1, hitColor, 60, splashDamageRadius * a, 10),
                WHFx.instHit(hitColor, false, 5, splashDamageRadius),
                WHFx.trailHitSpark(90, hitColor, 50, splashDamageRadius * a, 2, 20),
                WHFx.hitSpark(90, hitColor, 50, splashDamageRadius * a, 2, 14)
                );
                despawnEffect = new MultiEffect(
                WHFx.lineCircleIn(60, hitColor, splashDamageRadius * a, 3),
                WHFx.generalExplosion(life2, hitColor, splashDamageRadius * a, 40, true),
                WHFx.circleOut(life2, hitColor, splashDamageRadius * a),
                WHFx.shuttle(life1, backColor, frontColor, false, splashDamageRadius, 45),
                WHFx.shuttle(life1, backColor, frontColor, false, splashDamageRadius, 135),
                WHFx.subEffect(life2, splashDamageRadius * 2, 33, 34f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
                    float fout = Interp.pow2Out.apply(1 - fin);
                    for(int s : Mathf.signs){
                        Drawf.tri(x, y, 12 * fout, 45 * Mathf.curve(fin, 0, 0.1f) * WHFx.fout(fin, 0.25f), rot + s * 90);
                    }
                })
                )
                );

                shootEffect = new MultiEffect(
                WHFx.shoot3DWave(90, hitColor, 120, 30),
                WHFx.shootLine(12, 30),
                WHFx.instShoot(hitColor, frontColor));
                smokeEffect = new Effect(18f, e -> {
                    color(Pal.lightOrange, Color.lightGray, Color.gray, e.fin());

                    randLenVectors(e.id, 15, e.finpow() * 60, e.rotation, 90, (x, y) -> {
                        Fill.circle(e.x + x, e.y + y, e.fout() * 8 + 0.2f);
                    });
                });

                despawnSound = hitSound = explosionArtilleryShockBig;

                fragBullets = 7;
                fragLifeMax = 1.1f;
                fragVelocityMax = 1.1f;
                fragBullet = new CritBulletType(1.1f, 2000, name("pierce")){{
                    critMultiplier = 1.5f;
                    buildingDamageMultiplier = 0.2f;
                    critChance = 0.1f;
                    homingPower = 0.04f;

                    collides = false;
                    collidesGround = collidesAir = true;

                    width = 15f;
                    height = width * 2.5f;
                    shrinkY = shrinkX = 0.7f;
                    Color c = backColor = trailColor = lightColor = lightningColor = hitColor = WHItems.adamantium.color.cpy();
                    frontColor = hitColor.cpy().lerp(Pal.accent, 0.5f);

                    splashDamage = damage;
                    splashDamageRadius = 80;

                    lifetime = 20f;

                    lightning = 2;
                    lightningLength = lightningLengthRand = 4;
                    lightningDamage = 50;

                    hitSoundVolume /= 2.2f;
                    despawnShake = hitShake = 4f;
                    despawnSound = hitSound = explosionDull;

                    trailWidth = 5f;
                    trailLength = 15;
                    trailInterp = Interp.slope;

                    float a = 1.2f, life1 = 180f;
                    trailEffect = Fx.missileTrailSmoke;
                    trailParam = 3.5f;
                    trailInterval = 10f;

                    despawnEffect = new MultiEffect(
                    WHFx.lineCircleIn(30, hitColor, splashDamageRadius * a, 2),
                    WHFx.instRotation(life1, hitColor, splashDamageRadius, 45, false),
                    WHFx.circleOut(life1, hitColor, splashDamageRadius),
                    WHFx.generalExplosion(life1, hitColor, splashDamageRadius * a, 0, false),
                    WHFx.instHit(hitColor, false, 3, splashDamageRadius)
                    );
                    hitEffect = new MultiEffect(
                    WHFx.square(90, hitColor, 15, splashDamageRadius * a, 10),
                    WHFx.instHit(hitColor, false, 5, splashDamageRadius),
                    WHFx.trailHitSpark(90, hitColor, 15, splashDamageRadius * a, 1.3f, 15f),
                    WHFx.hitSpark(90, hitColor, 15, splashDamageRadius * a, 1.5f, 12f));

                    fragRandomSpread = 0;

                    fragBullets = 1;
                    fragBullet = new SizeDamageBullet(){
                        {
                            BulletType b = sizeDamageCreate;
                            b.lightning = 3;
                            b.lightningDamage = 30;
                            b.lightningLength = b.lightningLengthRand = 15;

                            hitSizeLightingScale = 10;
                            lifetime = 40;
                            hitSizeLightingScale = maxDamageMultiple = 9;
                            splashDamageRadius = 50f;
                            hitSizeDamage = 1000;
                            maxHitSizeScale = UnitTypes.eclipse.hitSize * 2.5f;

                            hitSizeColor = hitColor = lightningColor = frontColor = trailColor = backColor = c;
                            trailLength = 12;
                            trailWidth = 2;
                            trailChance = 0.01f;
                            despawnEffect = hitEffect = Fx.none;
                        }

                        @Override
                        public void draw(Bullet b){
                        }

                        @Override
                        public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
                            float size = Math.min(s.hitSize() / 4, 30);
                            if(Mathf.chance(0.32) || data.size < 8){
                                float sd = Mathf.random(size * 1.5f, size * 3);
                                WHFx.shuttle(60, hitColor, Color.black, true, 1, 1)
                                .at(s.getX() + Mathf.range(size), s.getY() + Mathf.range(size), Mathf.chance(0.5) ? 45 : 135, hitColor, sd);
                            }
                        }
                    };
                }};

                fragLifeMax = 5f;
                fragLifeMin = 1.5f;
                fragVelocityMax = 2f;
                fragVelocityMin = 0.35f;
            }

            public final SizeDamageBullet tearBullet = new SizeDamageBullet(){
                {
                    lifetime = 20;
                    maxDamageMultiple = 15;
                    hitSizeDamage = 200;
                    splashDamageRadius = 50f;
                    maxHitSizeScale = UnitTypes.conquer.hitSize * 1.5f;
                    damageInterp = Interp.pow3In;

                    hitSizeColor = hitColor = lightningColor = frontColor = trailColor = backColor = WHItems.adamantium.color.cpy();
                    despawnEffect = hitEffect = Fx.none;
                }

                @Override
                public void draw(Bullet b){
                }

                @Override
                public void dynamicHitEffect(Sized s, Seq<Sized> data, Bullet b){
                    float size = Math.min(s.hitSize() / 2, maxHitSizeScale / 2);
                    if(Mathf.chance(0.32) || data.size < 8){
                        float sd = Mathf.random(size * 2, size * 3);
                        Color c = hitColor.cpy().lerp(Pal.accent, 0.5f);
                        WHFx.shuttle(120, c.cpy().lerp(Pal.coalBlack, 0.3f), c, true, 1, 1)
                        .at(s.getX(), s.getY(), 135, hitColor, sd);
                    }
                }
            };

            public final float shieldDamage = 3000;
            public final BulletType breakType = new EffectBulletType(3f){
                {
                    absorbable = true;
                    collides = false;
                    lifetime = 8f;
                    drawSize = 0;
                    hitColor = lightningColor = lightColor = trailColor = backColor = WHItems.adamantium.color.cpy();
                }

                @Override
                public void despawned(Bullet b){
                    WHFx.shuttle(60, hitColor, hitColor, false, 1, 1)
                    .at(b.x, b.y, 45 + 90, hitColor, b.damage / Vars.tilesize / 3);
                    /*  shuttleDark.at(b.x, b.y, 45, hitColor, b.damage / Vars.tilesize/4);*/
                    Effect.shake(b.damage / 100, b.damage / 100, b);
                }
            };

            @Override
            public void hit(Bullet b, float x, float y){
                super.hit(b, x, y);
                if(b.absorbed){
                    breakType.create(b, b.team, b.x, b.y, 0, shieldDamage, 0, 1, null);
                }
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc other, float initialHealth){
                super.hitEntity(b, other, initialHealth);
                if(!b.absorbed && other instanceof Healthc && other.hitSize() > UnitTypes.vanquish.hitSize * 2.5f){
                    tearBullet.create(b, b.team, other.getX(), other.getY(), 0, -1, 0, 1, null);
                }
            }
        };
    }

    public static class TrailEffectData implements Pool.Poolable{
        public float len;
        public Trail trail;

        public static TrailEffectData create(){
            return Pools.obtain(TrailEffectData.class, TrailEffectData::new);
        }

        @Override
        public void reset(){
            len = 0;
            trail = null;
        }
    }
}
