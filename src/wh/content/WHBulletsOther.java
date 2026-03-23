package wh.content;

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
import mindustry.entities.effect.*;
import mindustry.entities.pattern.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.entities.bullet.*;
import wh.entities.bullet.laser.*;
import wh.entities.world.blocks.defense.turrets.HeatTurret.*;
import wh.graphics.*;
import wh.util.*;

import java.util.*;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static mindustry.Vars.state;
import static mindustry.gen.Sounds.explosionArtilleryShock;
import static wh.core.WarHammerMod.name;
import static wh.util.WHUtils.rand;

public class WHBulletsOther{
    public static BulletType IonizeInterval;
    public static BulletType ViperBulletMain;
    public static BulletType ViperBulletMain2;
    public static BulletType ViperBulletComp;
    public static BulletType ViperBulletFrag;
    public static BulletType PyrosBulletInterval;
    public static BulletType PyrosBulletComp1;
    public static BulletType PyrosBulletComp2;
    public static BulletType PyrosBulletFrag;
    public static BulletType CollapseSealedPromethiumInterval;
    public static BulletType CycloneMissleLauncherMissile2Frag;
    public static BulletType mbc;
    public static BulletType AnnihilateFrag;
    public static BulletType Air7MainWeaponFrag;

    public static BulletType RevengeBullet1;
    public static BulletType RevengeBullet2;
    public static BulletType RevengeBullet3;
    public static BulletType RevengeBullet4;


    public static void load(){
        IonizeInterval = new BulletType(){
            {
                buildingDamageMultiplier = 0.2f;
                lifetime = 180;
                speed = 2.5f;
                damage = 90;
                keepVelocity = false;
                lightningColor = trailColor = lightColor = hitColor = WHItems.resonantCrystal.color.cpy().lerp(Pal.techBlue, 0.7f);
                trailWidth = 2;
                trailLength = 18;
                splashDamage = 50;
                splashDamageRadius = 32;
                despawnEffect = hitEffect = WHFx.square(60, hitColor, 4, 30, 3);
                buildingDamageMultiplier = 0.2f;
                homingRange = 100;
                lightningDamage = 50;
                lightning = 2;
                lightningLength = 5;
            }

            @Override
            public void update(Bullet b){
                super.update(b);
                if(!(b instanceof TrailBullet Interval)) return;
                Teamc target = Units.closestTarget(b.team, b.x, b.y, homingRange,
                e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                t -> t != null && collidesGround && !b.hasCollided(t.id));
                if(b.time < 30 || target == null){
                    b.initVel(b.rotation(), speed * 0.4f * Math.max(0, 1 - b.fin() * 3));
                }else{
                    b.initVel(b.angleTo(target), speed);
                }
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
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(entity instanceof Unit u && u.type != null){
                    float dmg = b.damage * (1 + Mathf.clamp(u.type.armor / 10f, 1, 3));
                    u.damagePierce(dmg);
                }
            }

            @Override
            public void draw(Bullet b){
                super.draw(b);
                float vel = Math.max(0, b.vel.len() / speed);
                float out = b.time > b.lifetime - 12 ? (b.lifetime - b.time) / 12 : 1;

                Draw.color(trailColor);
                Drawf.tri(b.x, b.y, 3.5f, 6.5f * vel, b.rotation());
                Fill.circle(b.x, b.y, 4 * (1 - vel) * out);

                if(!(b instanceof TrailBullet Interval)) return;
                float z = Draw.z();
                Draw.z(z - 1e-4f);
                for(int i = 0; i < 2; i++){
                    if(Interval.trails[i] != null){
                        Interval.trails[i].draw(trailColor, 1.2f * (1 - vel) * out);
                    }
                    if(Interval.vs[i] != null){
                        Draw.color(trailColor);
                        Fill.circle(Interval.vs[i].x, Interval.vs[i].y, 1.2f * (1 - vel) * out);
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
            public boolean testCollision(Bullet bullet, Building tile){
                return bullet.time > 30 && super.testCollision(bullet, tile);
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

        ViperBulletFrag = new ShrapnelBulletType(){{
            length = 100;
            damage = 80;
            armorMultiplier = 0.5f;
            toColor = WHPal.MnSteelColor.cpy().lerp(Pal.techBlue.cpy(), 0.2f);
            shootEffect = smokeEffect = Fx.thoriumShoot;
        }};

        ViperBulletMain = new LightingLaserBulletType(){
            {
                damage = 500;
                length = 345;
                width = 50;
                sideWidth = 0;
                sideLength = 0;
                lifetime = 35;
                collidesAir = false;
                pierce = true;
                pierceCap = 3;
                lightningColor = hitColor = WHPal.MnSteelColor.cpy().lerp(Pal.techBlue.cpy(), 0.2f);
                shootEffect = new MultiEffect(
                WHFx.shootLine(6, 30));
                colors = new Color[]{hitColor.cpy().a(0.4f),
                hitColor.cpy().a(0.6f), hitColor};

                fragBullets = 3;
                fragBullet = ViperBulletFrag;
                fragSpread = 120;
                fragOnDespawn = false;
                fragRandomSpread = 0;
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                int hitCount = b.data instanceof Integer ? (int)b.data : 0;
                float damageMultiplier = hitCount < 3 ? 1.5f : 1f;
                b.damage *= damageMultiplier;

                super.hitEntity(b, entity, health);

                b.damage /= damageMultiplier;
                b.data = hitCount + 1;
            }
        };

        ViperBulletMain2 = new LightingLaserBulletType(){
            {
                renderingDistortion = true;
                damage = 600;
                length = 420;
                rangeChange = length - 360;
                reloadMultiplier = 0.7f;
                width = 60;
                sideWidth = 0;
                sideLength = 0;
                lifetime = 60;
                collidesAir = false;
                largeHit = true;
                pierceCap = 5;
                lightningColor = hitColor = WHPal.MnSteelColor.cpy().lerp(Pal.techBlue.cpy(), 0.2f);
                shootEffect = new MultiEffect(
                WHFx.shootLine(6, 30));
                colors = new Color[]{hitColor.cpy().a(0.4f),
                hitColor.cpy().a(0.6f), hitColor};

                fragBullets = 3;
                fragBullet = ViperBulletFrag;
                fragSpread = 120;
                fragOnDespawn = false;
                fragRandomSpread = 0;

                lightningSpacing = 60;
                lightningDelay = 5f;
                intervalBullet = new CritBulletType(0, 0){{
                    instantDisappear = true;
                    armorMultiplier = 0.5f;
                    splashDamage = 250;
                    hittable = absorbable = reflectable = false;
                    splashDamageRadius = 40;

                    lightningColor = hitColor = WHPal.MnSteelColor.cpy().lerp(Pal.techBlue.cpy(), 0.2f);
                    collidesTiles = collidesAir = false;
                    despawnEffect = new MultiEffect(
                    WHFx.square(60, hitColor, 10, splashDamageRadius, 5),
                    WHFx.hitSpark(60, hitColor, 15, splashDamageRadius, 1.5f, 15),
                    WHFx.generalExplosion(30, hitColor, splashDamageRadius, 5, false));
                }};
            }

            @Override
            public void init(Bullet b){
                float resultLength = Damage.collideLaser(b, length, largeHit, laserAbsorb, pierceCap), rot = b.rotation();
                PositionLightning.createEffect(b, b.fdata * 0.95f, b.rotation(), lightningColor, 2, Mathf.random(2, 3));

                laserEffect.at(b.x, b.y, rot, resultLength * 0.75f);

                if(lightningSpacing > 0){
                    int idx = 0;
                    for(float i = 0; i <= resultLength; i += lightningSpacing){
                        float cx = b.x + Angles.trnsx(rot, i),
                        cy = b.y + Angles.trnsy(rot, i);
                        int f = idx++;
                        Time.run(f * lightningDelay, () -> {
                            if(b.isAdded() && b.type == this && b != null){
                                intervalBullet.create(b, cx, cy, b.rotation());
                            }
                        });
                    }
                }
                addRectPathDistortion(b, b.rotation());
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                int hitCount = b.data instanceof Integer ? (int)b.data : 0;
                float damageMultiplier = hitCount < 3 ? 1.5f : 1f;
                b.damage *= damageMultiplier;

                super.hitEntity(b, entity, health);

                b.damage /= damageMultiplier;
                b.data = hitCount + 1;
            }
        };

        ViperBulletComp = new LaserBulletType(){
            {
                damage = 300;
                length = 200;
                width = 40;
                sideWidth = 0;
                sideLength = 0;
                collidesAir = false;
                pierce = true;
                largeHit = true;
                lifetime = 20;
                pierceCap = 3;
                lightningColor = hitColor = WHPal.MnSteelColor.cpy().lerp(Pal.techBlue.cpy(), 0.2f);
                shootEffect = new MultiEffect(
                WHFx.shootLine(6, 30)
                );
                colors = new Color[]{hitColor.cpy().a(0.4f),
                hitColor.cpy().a(0.6f), hitColor};
            }
        };

        PyrosBulletFrag = new HeatBulletType(){
            {
                buildingDamageMultiplier = 0.1f;
                sprite = "circle-bullet";
                frontColor = backColor = lightningColor = lightColor =
                hitColor = trailColor = Pal.slagOrange.cpy().lerp(Pal.lightOrange, 0.25f);

                status = WHStatusEffects.melta;
                statusDuration = 25f;
                width = height = 12f;

                trailWidth = 1.5f;
                trailLength = 5;

                shrinkX = shrinkY = 0f;
                lifetime = 18;
                speed = 2;
                damage = 100;
                splashDamage = 100;
                splashDamageRadius = 32f;
                puddleAmount = 8;
                puddleLiquid = Liquids.slag;
                armorMultiplier = 0.5f;
                hitEffect = despawnEffect = new MultiEffect(
                WHFx.hitCircle(30, hitColor, Pal.lightishGray, 5, splashDamageRadius, 7),
                WHFx.generalExplosion(30, hitColor, splashDamageRadius, 0, false));
            }
        };
        PyrosBulletInterval = new HeatBulletType(){
            public final float before = 30;
            public final float rotSpeed = 5f;

            {
                sprite = "circle-bullet";
                buildingDamageMultiplier = 0.1f;
                lifetime = 70;
                speed = 3f;
                drag = 0.05f;
                damage = 80;
                frontColor = backColor = lightningColor = lightColor =
                hitColor = trailColor = Pal.slagOrange.cpy().lerp(Pal.lightOrange, 0.25f);
                width = height = 5;
                trailLength = 30;
                trailWidth = 2;
                shrinkY = shrinkX = 0;
                reflectable = false;
                pierceCap = 5;

                status = WHStatusEffects.melta;
                statusDuration = 10;
                homingDelay = 20;
                homingRange = 48;

                hitEffect = despawnEffect = new MultiEffect(
                WHFx.hitSparkAng(60, hitColor, Pal.lightishGray, 8, 30, 20, 1.5f, 10),
                WHFx.square(60, hitColor, 8, 20, 4));
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(entity instanceof Unit u && u.type != null){
                    float dmg = b.damage * (1 + Mathf.clamp(u.hitSize / 4, 1, 3));
                    u.damagePierce(dmg);
                }
            }

            @Override
            public void updateHoming(Bullet b){
                Position target = null;
                if(b.data instanceof Position pos){
                    target = pos;
                }else if(b.owner instanceof HeatTurretBuild h){
                    target = h.target;
                }
                if(target == null || target == b) return;

                b.initVel(b.rotation(), b.time < before ? speed * (1 - b.time / (before + 10)) : speed * b.fin());
                if(b.time > homingDelay){
                    b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), rotSpeed * Time.delta));
                }
            }
        };

        PyrosBulletComp1 = new HeatBulletType(){
            {
                weaveRandom = true;
                weaveMag = 6;
                weaveScale = 4.5f;

                sprite = "large-orb";
                damage = 150;
                speed = 2.5f;
                lifetime = 400 / 3f;
                hittable = false;
                splashDamage = 80;
                splashDamageRadius = 50;
                scaledSplashDamage = true;

                frontColor = backColor = lightningColor = lightColor =
                hitColor = trailColor = Pal.slagOrange.cpy().lerp(Pal.lightOrange, 0.25f);

                sticky = true;
                width = height = 10f;
                shrinkX = shrinkY = 0;

                trailWidth = 4;
                trailSpread = 5;
                trailChance = 0.3f;
                trailLength = 8;

                hitEffect = despawnEffect = new MultiEffect(
                WHFx.generalExplosion(20, hitColor, splashDamageRadius, 5, true)
                );
                fragBullets = 1;
                fragBullet = WHBulletsOther.PyrosBulletFrag;
            }

            @Override
            public void updateWeaving(Bullet b){
                if(weaveMag != 0){
                    float sign = (weaveRandom ? -1 : 1f);
                    b.vel.rotateRadExact((float)Math.sin((b.time + Math.PI * weaveScale / 2f) / weaveScale) * weaveMag * sign * Time.delta * Mathf.degRad);
                }

                if(rotateSpeed != 0){
                    b.vel.rotate(rotateSpeed * Time.delta);
                }
            }
        };

        PyrosBulletComp2 = PyrosBulletComp1.copy();
        PyrosBulletComp2.weaveRandom = false;

        CollapseSealedPromethiumInterval = new BasicBulletType(){
            public final float before = 12;
            public final float rotSpeed = 5f;

            {
                sprite = name("pierce");
                buildingDamageMultiplier = 0.1f;
                lifetime = 450 / 3f;
                speed = 6f;
                drag = -0.03f;
                frontColor = Color.white;
                Color c = hitColor = trailColor = lightningColor = WHItems.culverCrystal.color.cpy();
                width = 15;
                height = 25;
                trailLength = 30;
                trailWidth = 15 / 4f;
                shrinkY = shrinkX = 0;
                absorbable = reflectable = false;

                damage = 150;
                pierceArmor = true;

                status = WHStatusEffects.palsy;
                statusDuration = 60;
                homingDelay = 15;
                homingRange = 48;
                homingPower = 0.05f;

                hitEffect = despawnEffect = WHFx.square(60, c, 8, 20, 4);
            }

            @Override
            public void updateHoming(Bullet b){
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

                if(b.time > homingDelay && target != null){
                    b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), rotSpeed * Time.delta));
                }
                if(!(b.data instanceof Position pos)) return;
                b.initVel(b.rotation(), b.time < before ? speed * (1 - b.time / (before + 10)) : speed * b.fin());
                if(target != null) return;
                if(b.time > homingDelay) b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(pos), rotSpeed * Time.delta));
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

                if(!(b instanceof TrailBullet Interval)) return;
                float z = Draw.z();
                Draw.z(z - 1e-4f);
                if(Interval.trails[0] != null){
                    Interval.trails[0].draw(trailColor, 2.5f * (1 - vel) * out);
                }
                if(Interval.vs[0] != null){
                    Draw.color(trailColor);
                    Fill.circle(Interval.vs[0].x, Interval.vs[0].y, 2.5f * (1 - vel) * out);
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
                Interval.vs[0] = new Vec2();

            }

            @Override
            public @Nullable Bullet create(
            @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
            float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
            ){
                TrailBullet bullet = TrailBullet.create();

                if(bullet.trails[0] != null){
                    bullet.trails[0].clear();
                }
                return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
            }
        };

        CycloneMissleLauncherMissile2Frag = new BlackHoleBulletType(){
            {

                trailColor = hitColor = WHItems.sealedPromethium.color.cpy();

                reloadMultiplier = 1 / 3f;
                pierceArmor = true;
                damage = 3000f / (240 / 10f);
                damageInterval = 10;

                splashDamage = 1200;
                splashDamageRadius = 150;

                buildingDamageMultiplier = 0.1f;
                inRad = 40;
                outRad = 150;
                despawnEffect = new MultiEffect(
                WHFx.square(60, WHItems.sealedPromethium.color.cpy(), 30, 100, 4),
                WHFx.circleOut(WHItems.sealedPromethium.color.cpy(), 130),
                WHFx.hitSpark(60, WHItems.sealedPromethium.color.cpy(), 30, 100, 1.5f, 9f),
                WHFx.subEffect(180, 100, 8, 45f, Interp.pow2Out, ((i, x, y, rot, fin) -> {
                    Draw.color(hitColor);
                    float fout = Interp.pow2Out.apply(1 - fin);
                    for(int s : Mathf.signs){
                        Drawf.tri(x, y, 15 * fout, 30 * Mathf.curve(fin, 0, 0.1f) * WHFx.fout(fin, 0.25f), rot + s * 90);
                    }
                })));
                lifetime = 240;
                despawnShake = 7f;
                hitColor = lightColor = WHItems.sealedPromethium.color.cpy();
                lightOpacity = 0.5f;


                bulletInterval = 5;
                intervalBullets = 1;

                fragBullets = 20;
                fragLifeMax = 1.2f;
                fragLifeMin = 1f;
                fragVelocityMax = 1.4f;

                fragBullet = intervalBullet = new FlakBulletType(3f, 70f){{
                    sprite = "missile-large";

                    lifetime = 60f;
                    width = 8f;
                    height = 15f;

                    hitSize = 7f;
                    shootEffect = Fx.shootSmokeSquareBig;
                    smokeEffect = Fx.shootSmokeDisperse;
                    ammoMultiplier = 1;
                    hitColor = backColor = trailColor = lightningColor = WHItems.sealedPromethium.color.cpy();
                    frontColor = Color.white;
                    trailWidth = 3f;
                    trailLength = 12;
                    hitEffect = despawnEffect = Fx.hitBulletColor;
                    buildingDamageMultiplier = 0.3f;

                    trailEffect = Fx.colorSpark;
                    trailRotation = true;
                    trailInterval = 3f;

                    homingPower = 0.1f;
                    homingDelay = 19f;
                    homingRange = 160f;

                    explodeRange = 50;
                    explodeDelay = 0f;

                    flakInterval = 20f;
                    despawnShake = 3f;

                    fragBullet = new LaserBulletType(120){{
                        colors = new Color[]{WHItems.sealedPromethium.color.cpy().a(0.4f), WHItems.sealedPromethium.color.cpy(), Color.white};
                        buildingDamageMultiplier = 0.1f;
                        width = 19f;
                        hitEffect = Fx.hitLancer;
                        sideAngle = 175f;
                        sideWidth = 1f;
                        sideLength = 40f;
                        lifetime = 22f;
                        drawSize = 400f;
                        length = 60f;
                        pierceCap = 6;
                        pierceArmor = true;
                    }};

                    fragBullets = 1;
                    fragSpread = fragRandomSpread = intervalRandomSpread = 0f;

                    splashDamage = 30f;
                    hitEffect = Fx.hitSquaresColor;
                    collidesGround = true;
                }};
            }

            @Override
            public void draw(Bullet b){
                super.draw(b);
                Draw.color(hitColor);
                Lines.stroke(4f * b.fout());
                Drawn.surround(b.id, b.x, b.y, outRad / 1.5f, 12, 8, 12, Mathf.curve(b.fin(), 0, 0.2f) * WHFx.fout(b.fin(), 0.15f));
                Lines.circle(b.x, b.y, outRad * Mathf.curve(b.fin(), 0, 0.2f) * WHFx.fout(b.fin(), 0.15f));
            }

            @Override
            public void hit(Bullet b, float x, float y){
                super.hit(b, x, y);
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);
                PositionLightning.createRandomRange(b, b.team, b, splashDamageRadius, hitColor, Mathf.chanceDelta(0.5f),
                0, 0, PositionLightning.WIDTH, 0, 4, hitPos -> {
                    Damage.damage(b.team, hitPos.getX(), hitPos.getY(), splashDamageRadius / 5, splashDamage * b.damageMultiplier(), collidesAir, collidesGround);
                    WHFx.lightningHitLarge.at(hitPos.getX(), hitPos.getY(), hitColor);
                });

                createSplashDamage(b, b.x, b.y);
                Vec2 vec = new Vec2().set(b);
                float rad = 120;
                float spacing = 10;
                for(int k = 0; k < 5; k++){
                    Time.run(k * spacing, () -> {
                        for(int j : Mathf.signs){
                            Vec2 v = Tmp.v6.rnd(Mathf.random(rad * 1.2f)).add(vec);
                            (j > 0 ? WHFx.chainLightningFade : WHFx.chainLightningFadeReversed).at(v.x, v.y, 12, hitColor, vec);
                        }
                    });
                }
            }
        };

        AnnihilateFrag = new DOTBulletType(){
            {
                Color f = WHPal.SkyBlueF.cpy().lerp(Color.sky, 0.3f);
                Color b = WHPal.SkyBlue.cpy().lerp(Color.sky, 0.12f);

                splashDamage = 1200;
                DOTDamage = 250;
                splashDamageRadius = DOTRadius = 140;

                scaledSplashDamage = true;

                damageInterval = 6;
                lifetime = 240;
                speed = 0f;
                collides = collidesTiles = false;
                collidesAir = collidesGround = true;
                effect = status = WHStatusEffects.plasma;
                statusDuration = 60;

                effectTimer = 4;
                hitColor = trailColor = lightningColor = f;
                trailWidth = 5;
                fx = WHFx.square(60, f, 1, 10, 4);
                despawnEffect = new MultiEffect(
                WHFx.generalExplosion(30, f, DOTRadius * 1.3f, 30, true),
                WHFx.hitSpark(60, f, 40, DOTRadius, 1.2f, 8),
                WHFx.sharpBlast(180, f, b, DOTRadius * 0.7f),
                WHFx.trailCircleHitSpark(150, f, 20, DOTRadius * 1.5f, 2.5f, 40).layer(Layer.effect),
                WHFx.plasmaBlast(180, f, 30, DOTRadius)
                );
                intervalBullets = 2;
                bulletInterval = 5;
                intervalDelay = 20;
                intervalBullet = new LightningBulletType(){{
                    lightningColor = f;
                    lightningCone = 15f;
                    lightningLength = 10;
                    lightningLengthRand = 10;
                    damage = 60;
                    shieldDamageMultiplier = 0.1f;
                }};
            }

            @Override
            public void despawned(Bullet b){
                super.despawned(b);
                if(!(b instanceof TrailBullet a)) return;
                createSplashDamage(b, b.x, b.y);
                PositionLightning.createRandomRange(b, b.team, b, DOTRadius * 1.2f, hitColor, Mathf.chanceDelta(0.5f),
                0, 0, PositionLightning.WIDTH, 0, 12, hitPos -> {
                    Damage.damage(b.team, hitPos.getX(), hitPos.getY(), DOTRadius / 5, DOTDamage * b.damageMultiplier(), collidesAir, collidesGround);
                    WHFx.generalExplosion(30, hitColor, 10, 5, false).at(hitPos.getX(), hitPos.getY());
                });

                float spacing = 10, rad = DOTRadius / 2;
                Vec2 vec = new Vec2().set(b);
                for(int k = 0; k < 8; k++){
                    Time.run(k * spacing, () -> {
                        for(int j : Mathf.signs){
                            Vec2 v = Tmp.v6.rnd(rad + Mathf.random(rad * 1.2f)).add(vec);
                            (j > 0 ? WHFx.chainLightningFade : WHFx.chainLightningFadeReversed).at(v.x, v.y, 12f, hitColor, vec);
                        }
                    });
                }
                if(a.surround.size > 0){
                    for(Bullet e : a.surround){
                        e.remove();
                    }
                }
            }

            @Override
            public void drawBullet(Bullet b){
            }

            @Override
            public void init(Bullet b){
                super.init(b);
                if(!(b instanceof TrailBullet Interval)) return;
                for(int i = 0; i < 4; i++){
                    Interval.vs[i] = new Vec2();
                }
            }

            @Override
            public void update(Bullet b){
                super.update(b);
                if(!(b instanceof TrailBullet)) return;
                updateTrail(b);
                updateSurroundBullet(b);

                if(b.fin() > 0.3f && Mathf.chanceDelta(b.fin() * 0.08f + 0.02)){
                    Drawn.randFadeLightningEffect(b.x, b.y, DOTRadius * 1.5f, 15, hitColor, true);
                }

               /* if(b.fin() > 0.05f && Mathf.chanceDelta(b.fin() * 0.06f + 0.03f)){
                    Tmp.v1.rnd(DOTRadius / 4 * b.fin());
                    WHFx.shuttleLerp.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, Tmp.v1.angle(), hitColor,
                    Mathf.random(DOTRadius / 2.5f, DOTRadius / 1.8f) * (Mathf.curve(b.fin(Interp.pow2In), 0, 0.7f) + 2) / 3);
                }*/
            }

            @Override
            public void updateTrail(Bullet b){
                super.updateTrail(b);
                if(!(b instanceof TrailBullet Interval)) return;
                for(int i = 0; i < 4; i++){
                    if(!Vars.headless){
                        if(Interval.trails[i] == null) Interval.trails[i] = new Trail(22);
                        Interval.trails[i].length = 22;
                    }

                    Trail trail = Interval.trails[i];
                    Rand rand = new Rand(b.id + i);

                    float scl = rand.random(0.5f, 1f) * Mathf.sign(rand.range(1)) * (i + 1) / 1.5f;
                    float s = rand.random(0.5f, 1.1f);

                    Tmp.v1.trns(
                    Time.time * scl * rand.random(0.5f, 1.5f) + i * 360f / rand.random(360),
                    DOTRadius / 2 * (1.1f + 0.5f * i) * 0.75f
                    ).add(b).add(
                    Mathf.sinDeg(Time.time * scl * rand.random(0.75f, 1.25f) * s) * DOTRadius / 3 * (i * 0.125f + 1) * rand.random(-1.5f, 1.5f),
                    Mathf.cosDeg(Time.time * scl * rand.random(0.75f, 1.25f) * s) * DOTRadius / 3 * (i * 0.125f + 1) * rand.random(-1.5f, 1.5f)
                    );


                    if(!Vars.headless){
                        trail.update(Tmp.v1.x, Tmp.v1.y, trailInterp.apply(b.fin()) * (1 + (trailSinMag > 0 ? Mathf.absin(Time.time, 4, 0.2f) : 0)));
                        Interval.vs[i].set(Tmp.v1);
                    }
                }
            }

            @Override
            public void draw(Bullet b){
                super.draw(b);
                float fade = Mathf.curve(b.fin(), 0, 0.1f) * WHFx.fout(b.fin(Interp.pow2In), 0.15f);
                float in = Mathf.curve(b.fin(Interp.smooth), 0, 0.1f);
                Draw.color(hitColor);
                Drawn.surround(b.id, b.x, b.y, 50, 8, 8, 10, in);
                Fill.circle(b.x, b.y, 30 * in);
                Lines.stroke(3f * fade);
                Lines.circle(b.x, b.y, DOTRadius * fade);
                Draw.color(Pal.coalBlack.cpy());
                Draw.z(Layer.effect + 0.0001f);
                Fill.circle(b.x, b.y, (30 / 2f + 1.5f) * in);
                Draw.reset();
                float out = Mathf.curve(b.fin(), 0, 0.12f) * WHFx.fout(b.fin(), 0.15f);
                if(!(b instanceof TrailBullet Interval)) return;

                for(int i = 0; i < 4; i++){
                    if(!Vars.headless){
                        Tmp.c1.set(hitColor).mul(1 + i * 0.005f).lerp(Color.white, 0.015f * i + Mathf.absin(4f, 0.3f));
                        if(Interval.trails[i] != null){
                            Interval.trails[i].drawCap(Tmp.c1, trailWidth * out);
                            Interval.trails[i].draw(Tmp.c1, trailWidth * out);
                        }
                        if(Interval.vs[i] != null){
                            Draw.color(Tmp.c1);
                            for(int a = 0; a < 4; a++){
                                Drawf.tri(Interval.vs[i].x, Interval.vs[i].y,
                                trailWidth * 1.1f * out, trailWidth * 3.2f * out, a * 90);
                            }
                            Draw.color(Pal.coalBlack.cpy());
                            for(int a = 0; a < 4; a++){
                                Drawf.tri(Interval.vs[i].x, Interval.vs[i].y,
                                trailWidth * 0.2f * out, trailWidth * 0.2f * 3 * out, a * 90);
                            }
                        }
                    }
                }
                Draw.reset();

               /* Draw.color(Pal.remove);
                Draw.z(Layer.effect+6);
                Lines.stroke(3);
                Lines.circle(b.x, b.y, b.fdata);
                Draw.reset();*/

            }

            public final BulletType surroundBullet = new BasicBulletType(){
                {
                    Color f = WHPal.SkyBlueF.cpy().lerp(Color.sky, 0.3f);

                    sprite = name("cross-star");
                    spin = 2;

                    width = height = 24;
                    speed = 2f;

                    reflectable = false;
                    splashDamageRadius = 6 * 8;
                    scaledSplashDamage = true;
                    damage = splashDamage = 200;
                    pierceArmor = true;
                    homingRange = DOTRadius;
                    homingDelay = 18;
                    homingPower = 0.08f;

                    hitColor = trailColor = backColor = f;

                    trailWidth = 2;
                    trailLength = 9;

                    lifetime = 120;

                    buildingDamageMultiplier = 0.2f;

                    despawnEffect = new MultiEffect(
                    WHFx.square(60, f, 8, 30, 5),
                    WHFx.lineCircleOut(60, f, 30, 2));
                    hitSound = despawnSound = Sounds.explosion;
                }

                @Override
                public void update(Bullet b){
                    updateTrail(b);
                    if(b.time < homingDelay) return;
                    updateHoming(b);
                    Teamc target = Units.closestTarget(b.team, b.x, b.y, splashDamageRadius / 4f,
                    e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                    t -> t != null && collidesGround && !b.hasCollided(t.id));

                    if(target != null) b.remove();
                }

                @Override
                public void updateHoming(Bullet b){
                    Teamc target;
                    //home in on allies if possible
                    target = Units.closestTarget(b.team, b.x, b.y, homingRange,
                    e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                    t -> t != null && collidesGround && !b.hasCollided(t.id));

                    Teamc dateTarget = null;
                    if(b.data instanceof Teamc t) dateTarget = t;

                    if(target != null){
                        b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta * 50f));
                    }else if(dateTarget != null){
                        b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(dateTarget), homingPower * Time.delta * 50f));
                    }
                }

                @Override
                public void despawned(Bullet b){
                    despawnSound.at(b);
                    createSplashDamage(b, b.x, b.y);
                    despawnEffect.at(b.x, b.y, splashDamageRadius, b.team.color);

                }
            };

            public final float surroundInterval = 10;

            public void updateSurroundBullet(Bullet b){
                if(!(b instanceof TrailBullet a)) return;
                if(b.timer.get(3, surroundInterval)){
                    a.surround.add(surroundBullet.create(b, b.team, b.x, b.y, b.rotation()));
                }

                if(a.surround.size > 0){
                    for(Bullet e : a.surround){
                        int ta = Mathf.randomSeed(e.id, 80, 150);
                        float tg = Mathf.randomSeed(e.id, 360);
                        float angle = b.time * 2 * (ta % 2 == 0 ? 1 : -1) + tg;
                        float tx = WHUtils.ellipseXY(b.x, b.y, ta, ta / 2.5f, tg, angle, 0);
                        float ty = WHUtils.ellipseXY(b.x, b.y, ta, ta / 2.5f, tg, angle, 1);
                        WHUtils.movePoint(e, tx, ty, 0.1f);
                        e.rotation(e.angleTo(tx, ty));
                        e.initVel(e.rotation(), 0);
                        e.time = 0;

                        Teamc target = Units.closestTarget(b.team, b.x, b.y, DOTRadius * 2,
                        en -> en != null && en.checkTarget(collidesAir, collidesGround),
                        Objects::nonNull);

                        if(target != null){
                            e.data = target;
                            e.initVel(e.angleTo(target), 3);
                            a.surround.remove(e);
                        }
                    }
                }
            }

            @Override
            public @Nullable Bullet create(
            @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
            float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
            ){
                TrailBullet bullet = TrailBullet.create();

                for(int i = 0; i < 4; i++){
                    if(bullet.trails[i] != null){
                        bullet.trails[i].clear();
                    }
                }
                bullet.surround.clear();
                return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
            }
        };

        mbc = new RailBulletType(){
            {
                hitColor = trailColor = WHItems.molybdenumAlloy.color.cpy().lerp(Pal.techBlue, 0.2f).lerp(Color.white, 0.2f);
                shootEffect = WHFx.instShoot(hitColor, Color.white);
                pierceEffect = hitEffect = WHFx.instHit(hitColor, false, 3, 20);
                smokeEffect = Fx.smokeCloud;

                damage = 200;
                splashDamageRadius = 24;
                splashDamage = 80;
                buildingDamageMultiplier = 0.2f;
                pierceDamageFactor = 1f;

                pointEffectSpace = 20;

                length = 120;
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

            public Effect moveTrailEffect = new Effect(25, e -> {
                if(!(e.data instanceof TrailEffectData data)) return;
                float resultLen = data.len;
                Trail trail = data.trail;
                Tmp.v1.trns(e.rotation, resultLen * e.fin()).add(e.x, e.y);
                Tmp.v2.trns(e.rotation, 0, Mathf.sin(e.fin() * resultLen + Mathf.randomSeed(e.id, 180f, 360f), resultLen / 8, 1) * 10f);
                float tx = Tmp.v1.x + Tmp.v2.x, ty = Tmp.v1.y + Tmp.v2.y;

                float size = 2.5f;
                if(!state.isPaused()) trail.update(tx, ty, size);
                Draw.color(e.color);
                Fill.circle(tx, ty, size * 2 * e.fout());
                trail.drawCap(e.color, size * e.fout());
                trail.draw(e.color, size * e.fout());
            });

            public class TrailEffectData{
                public float len;
                public Trail trail;

                public TrailEffectData(float length, Trail trail){
                    this.len = length;
                    this.trail = trail;
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

                despawnEffect.at(Tmp.v1.x, Tmp.v1.y, b.rotation(), hitColor);

                despawnSound.at(Tmp.v1, 1f + Mathf.range(hitSoundPitchRange));

                for(int i = 0; i < 2; i++){
                    TrailEffectData data = new TrailEffectData(b.fdata, new Trail(12));
                    moveTrailEffect.at(b.x, b.y, b.rotation(), hitColor, data);
                }

                Effect.shake(despawnShake, despawnShake, Tmp.v1);
            }

            @Override
            public void hitEntity(Bullet b, Hitboxc entity, float health){
                super.hitEntity(b, entity, health);
                if(entity instanceof Unit h){
                    float shield = Math.max(h.shield() * 0.01f, 100f);
                    h.shield(h.shield() - shield);
                }
            }

            @Override
            public void despawned(Bullet b){
            }
        };

        Air7MainWeaponFrag = new LightningLinkerBulletType(){
            {
                effectLightningChance = 0.1f;
                damage = 200;
                linkLightingDamage = 120;
                backColor = trailColor = lightColor = lightningColor = hitColor = WHPal.ShootOrange;
                size = 10;
                frontColor = WHPal.ShootOrangeLight;
                range = 400;
                spreadEffect = Fx.none;
                trailWidth = 4;
                trailLength = 20;
                speed = 4f;
                linkRange = 180f;
                maxHit = 10;
                drag = 0.007f;
                hitSound = explosionArtilleryShock;
                splashDamageRadius = 60.0F;
                splashDamage = lightningDamage = damage / 3.0F;
                lifetime = 200;
                despawnEffect = Fx.none;
                hitEffect = new MultiEffect(
                WHFx.generalExplosion(30, hitColor, splashDamageRadius, 10, true),
                WHFx.sharpBlast(35.0F, hitColor, frontColor, splashDamageRadius));
                shootEffect = WHFx.hitSpark(45.0F, backColor, 12, 60.0F, 3.0F, 8.0F);
                smokeEffect = WHFx.hugeSmoke;
            }
        };

        RevengeBullet1 = new ApproachBullet(){
            {
                damage = splashDamage = 1;

                lifetime = 300;

                color = hitColor = WHPal.WHYellow;
                despawnEffect = new MultiEffect(
                WHFx.square(90, hitColor, 10, 60, 8)
                );

                trailEffect = WHFx.hitCircle(60, hitColor, hitColor, 2, 15, 5).layer(Layer.bullet - 0.001f);
                trailChance = 0.5f;

                shootEffect = WHFx.hitSpark(20, hitColor, 4, 30, 1, 8);

                reload = 300 / 3f - 5f;

                homingDelay = 60;
                homingRange = 300;
                shootType = new ShootHelix(4, 0.5f){{
                    shots = 3;
                    shotDelay = 9f;
                }};

                speed = 3;

                bulletType = new CritBulletType(4, 100){
                    {
                        hitColor = trailColor = frontColor = backColor = WHPal.WHYellow;
                        lifetime = 300 / speed;
                        width = 10;
                        height = width * 2;
                        trailWidth = 1.5f;
                        trailLength = 10;

                        splashDamage = damage;
                        pierceCap = 3;
                        pierceBuilding = true;
                        splashDamageRadius = 40;

                        shootEffect = WHFx.hitSpark(20, hitColor, 4, 30, 1, 8);

                        despawnEffect = hitEffect = new MultiEffect(
                        WHFx.generalExplosion(10, hitColor, splashDamageRadius, 0, false),
                        WHFx.square(60, hitColor, 10, 60, 8),
                        WHFx.trailHitSpark(15, hitColor, 3, splashDamageRadius, 1.5f, 12)
                        );
                    }
                };
            }
        };

        RevengeBullet2 = new ApproachBullet(){
            {
                damage = splashDamage = 1;

                color = hitColor = WHPal.WHYellow;
                despawnEffect = new MultiEffect(
                WHFx.square(90, hitColor, 10, 60, 8)
                );

                trailEffect = WHFx.hitCircle(60, hitColor, hitColor, 2, 15, 5).layer(Layer.bullet - 0.001f);
                trailChance = 0.5f;

                shootEffect = WHFx.hitSpark(20, hitColor, 4, 30, 1, 8);

                reload = lifetime / 3f - 5f;

                homingDelay = 60;
                homingRange = 300;

                shootType = new ShootMulti(
                new ShootPattern(){{
                    shots = 2;
                    shotDelay = 8f;
                }},
                new ShootSpread(4, 90){
                    @Override
                    public void shoot(int totalShots, BulletHandler handler, @Nullable Runnable barrelIncrementer){
                        for(int i = 0; i < shots; i++){
                            float angleOffset = i * spread - (shots - 1) * spread / 2f;
                            handler.shoot(0, 0, angleOffset + 45f, firstShotDelay + shotDelay * i);
                        }
                    }
                }
                );

                trailAmount = 4;
                trailWidth = 1.5f;
                trailUpdate = b -> {
                    for(int i = 0; i < trailAmount; i++){
                        if(!Vars.headless){
                            if(b.trails[i] == null) b.trails[i] = new Trail(12);
                            b.trails[i].length = 12;
                            rand.setSeed(b.id);

                            float a = 13 * (1 + 0.4f * i), ang = rand.random(90);

                            float dx = WHUtils.ellipseXY(b.x, b.y, a, a / 2, ang, (Time.time / 2 * rand.random(0.7f, 1) * (8 - (i % 2 == 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 90 * i, 0),
                            dy = WHUtils.ellipseXY(b.x, b.y, a, a / 2, ang, (Time.time / 2 * rand.random(0.7f, 1) * (8 - (i % 2 == 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 90 * i, 1);
                            if(!Vars.headless) b.trails[i].update(dx, dy, trailInterp.apply(b.fin()) * trailWidth);
                        }
                    }
                };

                speed = 3;

                bulletType = new CritBulletType(8, 150, name("pierce")){
                    {
                        hitColor = trailColor = frontColor = backColor = WHPal.WHYellow;
                        lifetime = 400 / speed;
                        width = 15;
                        height = width * 2;
                        trailWidth = 2;
                        trailLength = 10;

                        splashDamage = damage;
                        pierceArmor = true;
                        splashDamageRadius = 54;

                        homingPower = 0.1f;
                        homingRange = 100f;
                        homingDelay = 3;
                        followAimSpeed = 10;

                        shootEffect = WHFx.hitSpark(10, hitColor, 4, 30, 1, 8);

                        despawnEffect = hitEffect = new MultiEffect(
                        WHFx.generalExplosion(10, hitColor, splashDamageRadius, 8, false),
                        WHFx.square(60, hitColor, 10, 60, 8),
                        WHFx.trailHitSpark(10, hitColor, 10, splashDamageRadius, 1.5f, 8)
                        );
                    }
                };
            }
        };

        RevengeBullet3 = new ApproachBullet(){
            {
                damage = splashDamage = 1;

                color = hitColor = WHPal.WHYellow;
                despawnEffect = new MultiEffect(
                WHFx.square(60, hitColor, 10, 30, 6),
                WHFx.generalExplosion(8, hitColor, 20, 30, false)
                );

                trailEffect = WHFx.hitCircle(60, hitColor, hitColor, 2, 15, 5).layer(Layer.bullet - 0.001f);
                trailChance = 0.5f;

                shootEffect = WHFx.hitSpark(20, hitColor, 4, 30, 1, 8);

                reload = lifetime;
                rotateSpeed = 5;

                homingDelay = 60;
                homingRange = 250;

                speed = 3;

                shootY = 20;

                trailAmount = 3;
                trailWidth = 2;
                trailUpdate = b -> {
                    for(int i = 0; i < trailAmount - 1; i++){
                        if(!Vars.headless){
                            if(b.trails[i] == null) b.trails[i] = new Trail(15);
                            b.trails[i].length = 15;
                            rand.setSeed(b.id);

                            float dx = WHUtils.dx(b.x, 20, (Time.time / 2 * rand.random(0.7f, 1) * (8 - (i % 2 == 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 180 * i),
                            dy = WHUtils.dy(b.y, 20, (Time.time / 2 * rand.random(0.7f, 1) * (8 - (i % 2 != 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 180 * i);
                            if(!Vars.headless) b.trails[i].update(dx, dy, trailInterp.apply(b.fin()) * trailWidth);
                        }
                    }
                    if(!Vars.headless){
                        if(b.trails[2] == null) b.trails[2] = new Trail(10);
                        b.trails[2].length = 10;
                        rand.setSeed(b.id);

                        float a = 13 * (1 - Mathf.absin(8, 0.3f)), ang = rand.random(90);

                        float dx = WHUtils.ellipseXY(b.x, b.y, a, a / 2, ang, (Time.time / 2 * rand.random(0.7f, 1)) + Mathf.randomSeed(b.id, 360) + 45, 0),
                        dy = WHUtils.ellipseXY(b.x, b.y, a, a / 2, ang, (Time.time / 2 * rand.random(0.7f, 1)) + Mathf.randomSeed(b.id, 360) + 45, 1);
                        if(!Vars.headless) b.trails[2].update(dx, dy, trailInterp.apply(b.fin()) * trailWidth);
                    }
                };

                bulletType = new LightingContinuousLaserBullet(2000 / 10f){{

                    pierceArmor = true;
                    damageInterval = 6f;
                    pierceCap = 4;
                    hitColor = lightColor = WHPal.WHYellow.cpy();

                    lifetime = 350;
                    length = 250;

                    width = 6;

                    rings = 2;
                    ringSpacing = 20f;

                    colors = new Color[]{hitColor.cpy().a(0.4f), hitColor.cpy().a(0.6f), hitColor.cpy().a(0.8f), Pal.coalBlack};

                    smokeEffect = Fx.shootSmallSmoke;
                    despawnEffect = hitEffect = Fx.hitMeltdown;
                }};
            }
        };

        RevengeBullet4 = new ApproachBullet(){
            {
                damage = splashDamage = 500;
                splashDamageRadius = 80;
                lifetime = 300;

                color = hitColor = WHPal.WHYellow;
                despawnEffect = new MultiEffect(
                WHFx.generalExplosion(60, hitColor, splashDamageRadius, 10, false),
                WHFx.sharpBlast(60, hitColor, hitColor, splashDamageRadius),
                WHFx.circleOut(60, hitColor, splashDamageRadius)
                );

                trailEffect = WHFx.hitCircle(60, hitColor, hitColor, 2, 15, 5).layer(Layer.bullet - 0.001f);
                trailChance = 0.5f;

                shootEffect = WHFx.hitSpark(20, hitColor, 4, 30, 1, 8);

                reload = 300 / 2f - 5f;

                homingDelay = 60;
                homingRange = 250;

                speed = 2.5f;

                trailAmount = 3;
                trailWidth = 2;
                trailUpdate = b -> {
                    for(int i = 0; i < trailAmount; i++){
                        if(!Vars.headless){
                            if(b.trails[i] == null) b.trails[i] = new Trail(12);
                            b.trails[i].length = 12;
                            rand.setSeed(b.id);

                            float a = 15 * (1 + 0.4f * i), ang = rand.random(90);

                            float dx = WHUtils.ellipseXY(b.x, b.y, a, a / 2, ang, (Time.time / 2 * rand.random(0.7f, 1) * (8 - (i % 2 == 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 90 * i, 0),
                            dy = WHUtils.ellipseXY(b.x, b.y, a, a / 2, ang, (Time.time / 2 * rand.random(0.7f, 1) * (8 - (i % 2 == 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 90 * i, 1);
                            if(!Vars.headless) b.trails[i].update(dx, dy, trailInterp.apply(b.fin()) * trailWidth);
                        }
                    }
                };

                bulletType = new MultiTrailBulletType(6, 300, name("energy-bullet")){{

                    width = 9;
                    height = width * 2.5f;

                    lifetime = 380 / speed;//360
                    keepVelocity = false;
                    splashDamage = damage;
                    splashDamageRadius = 64f;
                    frontColor = WHPal.ShootOrangeLight;
                    lightningColor = lightColor = hitColor = trailColor = backColor = WHPal.WHYellow;

                    lightning = 3;
                    lightningLength = 6;
                    lightningLengthRand = 8;
                    lightningDamage = 50;

                    hittable = false;

                    offset = 1;
                    subTrails = 3;
                    trailLength = 20;
                    subTrailWidth = 1.5f;
                    trailWidth = width / 3f;

                    shootEffect = new MultiEffect(
                    WHFx.instShoot(hitColor, hitColor)
                    );

                    hitEffect = new MultiEffect(
                    WHFx.trailCircleHitSpark(30, hitColor, 12, 70, 1.5f, 10f),
                    WHFx.hitPoly(30, hitColor, hitColor, 10, 64, 5, 6, 45),
                    WHFx.generalExplosion(10, hitColor, splashDamageRadius * 0.45f, 6, true),
                    WHFx.instHit(hitColor, true, 3, splashDamageRadius)
                    );

                    despawnEffect = new MultiEffect(
                    WHFx.circleOut(60, hitColor, splashDamageRadius),
                    WHFx.instRotation(60, hitColor, splashDamageRadius * 1.5f, 45, true),
                    WHFx.hitSpark(20, hitColor, 10, splashDamageRadius, 1.5f, 10)
                    );

                    fragBullets = 3;
                    fragBullet = new CritBulletType(3, 150, name("cross-star")){{
                        width = height = 14;
                        shrinkX = shrinkY = 1;
                        trailLength = 5;
                        trailWidth = 2;
                        drag = 0.02f;
                        lifetime = 32;
                        splashDamage = damage;
                        splashDamageRadius = 40;
                        spin = 3;
                        frontColor = lightningColor = lightColor = hitColor = trailColor = backColor = WHPal.WHYellow;

                        hitEffect = despawnEffect = new MultiEffect(
                        WHFx.generalExplosion(10, hitColor, splashDamageRadius, 5, false),
                        WHFx.circleOut(30, hitColor, splashDamageRadius),
                        WHFx.instRotation(30, hitColor, splashDamageRadius * 0.8f, 90, false),
                        WHFx.hitPoly(30, hitColor, hitColor, 5, splashDamageRadius, 4, 5, 45),
                        WHFx.trailCircleHitSpark(30, hitColor, 5, splashDamageRadius, 1f, 6)
                        );
                    }};

                }};
            }
        };
    }


    public static class TrailBullet extends Bullet{
        @Nullable
        public Trail[] trails = new Trail[6];

        public Seq<Bullet> surround = new Seq<>();

        public Vec2[] vs = new Vec2[6];

        public static TrailBullet create(){
            return Pools.obtain(TrailBullet.class, TrailBullet::new);
        }
    }
}
