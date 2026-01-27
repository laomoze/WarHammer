package wh.entities.world.entities;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.defense.Wall.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import wh.content.*;
import wh.entities.bullet.ApproachBullet.*;
import wh.gen.*;
import wh.util.*;

import static mindustry.io.TypeIO.*;
import static wh.util.WHUtils.rand;

public class RevengeUnit extends UnitEntity{

    public final float DAMAGE_REDUCE = 0.8f;
    public final float DAMAGE_REDUCE_Duration = 8 * 60f;
    public final float MAX_DAMAGE = 1000f;
    public final float ACCUMULATE_DAMAGE = 4000;

    public final float ABILITY_RELOAD = 15 * 60f;
    public final float BULLET_RECOVERY_TIME = 2.5f * 60f;
    public final float RECOVERY_HEALTH = 7000f;
    public final int MAX_BULLET = 5;
    public final float RANGE_CHECK = 120;

    public final float CHECK_RELOAD = 75;
    public final float CHECK_RANGE = 450;
    public float checkReload = CHECK_RELOAD;

    public float abilityTimer;
    public float rangeTimer;
    public float bulletRecoveryTimer;
    public float abilityDuration;
    public float drawSize;

    public float accumulateDamage;

    public float shootX = -36.25f, shootY = 4f;

    public Seq<Healthc> enemies = new Seq<>();

    public Seq<Bullet> surroundBullets = new Seq<>();
    public final Seq<BulletType> bullets = new Seq<>(BulletType.class);

    public TextureRegion armorRegion;

    public ObjectFloatMap<Healthc> hatred = new ObjectFloatMap<>();

    @Override
    public int classId(){
        return EntityRegister.getId(RevengeUnit.class);
    }

    @Override
    public void setType(UnitType type){
        super.setType(type);
        armorRegion = Core.atlas.find(this.type.name + "-energyArmor");
        bullets.add(WHBulletsOther.RevengeBullet1, WHBulletsOther.RevengeBullet2,
        WHBulletsOther.RevengeBullet3, WHBulletsOther.RevengeBullet4);
    }

    public Healthc findOwner(Entityc ent){
        Healthc target = null;

        if(ent instanceof Bullet b){
            Entityc owner = b.owner();
            if(owner instanceof Unit u){
                if(u.controller() instanceof MissileAI ai){
                    target = ai.shooter;
                }else{
                    target = u;
                }
            }else if(owner instanceof Building building){
                target = building;
            }
        }

        return target;
    }

    @Override
    public void rawDamage(float amount){
        boolean hadShields = shield > 0.0001f;

        if(Float.isNaN(health)) health = 0f;

        if(hadShields){
            shieldAlpha = 1f;
        }

        float reduce = abilityDuration > 0 ? DAMAGE_REDUCE : 1f;

        accumulateDamage += Damage.applyArmor(amount, armor) / healthMultiplier / Vars.state.rules.unitHealth(team);

        float shieldDamage = Math.min(Math.min(Math.max(shield, 0), amount), MAX_DAMAGE);
        shield -= shieldDamage * reduce;
        hitTime = 1f;
        amount -= shieldDamage;

        if(amount > 0 && type.killable){
            health -= Math.min(amount, MAX_DAMAGE) * reduce;
            if(health <= 0 && !dead){
                kill();
            }

            if(hadShields && shield <= 0.0001f){
                Fx.unitShieldBreak.at(x, y, 0, type.shieldColor(self()), this);
            }
        }
    }

    @Override
    public void draw(){
        super.draw();

        float width = armorRegion.width * Draw.scl * drawSize,
        height = armorRegion.height * Draw.scl * drawSize;

        if(Vars.renderer.animateShields){
            Draw.z(Layer.shields + 0.01f);
            Draw.color(Tmp.c1.set(team.color.cpy()).lerp(Color.white, Mathf.absin(4f, 0.3f)));
            Draw.rect(armorRegion, x, y, width, height, this.rotation - 90);
        }else{
            Draw.z(Layer.shields);
            Draw.color(Tmp.c1.set(team.color.cpy()).lerp(Color.white, Mathf.absin(4f, 0.3f)));
            Draw.alpha(0.5f);
            Draw.rect(armorRegion, x, y, width, height, this.rotation - 90);
        }
    }

    @Override
    public void update(){
        super.update();
        if(canShoot()) bulletRecoveryTimer += Time.delta * reloadMultiplier();

        rand.setSeed(id);

        checkReload -= Time.delta;
        rangeTimer -= Time.delta;
        abilityDuration -= Time.delta;

        surroundBullets.removeAll(e -> e == null || !e.isAdded());


        if(checkReload <= 0.0001f && surroundBullets.size > 0){
            checkReload = CHECK_RELOAD;
            Teamc en = Units.bestTarget(team, x, y, CHECK_RANGE,
            e -> !e.dead() && !(e instanceof TimedKillc),
            b -> !b.block.underBullets && b.maxHealth > 1000 && !(b instanceof WallBuild), UnitSorts.closest);

            Bullet c = surroundBullets.random();
            if(en != null){
                if(c instanceof AB a){
                    a.target = en;
                    Healthc enemy = (Healthc)en;
                    if(enemy.dead() || enemy.isValid()) a.find = true;
                    a.initVel(c.angleTo(en), c.type.speed);
                    surroundBullets.remove(c);
                }
            }
           /* Units.nearbyEnemies(team, x, y, CHECK_RANGE, other -> {
                Bullet c = surroundBullets.random();
                if(c instanceof AB a){
                    a.find = true;
                    a.initVel(c.angleTo(other), c.type.speed);
                }
                surroundBullets.remove(c);
            });*/
        }

        if(accumulateDamage >= ACCUMULATE_DAMAGE){
            accumulateDamage = 0;
            createBullet();
        }

        if(bulletRecoveryTimer > BULLET_RECOVERY_TIME && surroundBullets.size < MAX_BULLET){
            bulletRecoveryTimer = 0;
            createBullet();
            if(Mathf.chanceDelta(0.05f)) createBullet();
        }

        enemies.removeAll(e -> e == null || e.dead() || e.dst(this) > CHECK_RANGE || !e.isAdded() || e.maxHealth() <= RECOVERY_HEALTH);

        if(rangeTimer <= 0.0001f){
            rangeTimer = RANGE_CHECK;
            Units.nearbyEnemies(team, x, y, CHECK_RANGE, other -> {
                if(other.maxHealth() > RECOVERY_HEALTH) enemies.add(other);
            });
            Vars.indexer.allBuildings(x, y, CHECK_RANGE, other -> {
                if(other.team != this.team && other.block.targetable && other.block.underBullets
                && other.maxHealth() > RECOVERY_HEALTH && other instanceof TurretBuild){
                    enemies.add(other);
                }
            });
            enemies.sort(u -> -u.health());
            enemies.truncate(MAX_BULLET);
        }

        if(enemies.size > MAX_BULLET - 1){
            abilityTimer -= Time.delta;
        }

        if(abilityTimer <= 0.0001f && enemies.any() && surroundBullets.size > 0){
            abilityTimer = ABILITY_RELOAD;
            abilityDuration = DAMAGE_REDUCE_Duration;
            for(int i = 0; i < enemies.size; i++){
                Healthc e = enemies.get(i);
               /* Time.run(i * checkReload, () -> {
                    createBullet();
                    Bullet bu = surroundBullets.random();
                    if(bu instanceof AB a){
                        a.target = (Teamc)e;
                        if(e==null||e.dead())a.find = true;
                        a.initVel(bu.angleTo(e), bu.type.speed);
                        surroundBullets.remove(bu);
                    }
                });*/
                createBullet();
                Bullet bu = surroundBullets.random();
                if(bu instanceof AB a){
                    a.target = (Teamc)e;
                    if(e == null || e.dead()) a.find = true;
                    a.initVel(bu.angleTo(e), bu.type.speed);
                    surroundBullets.remove(bu);
                }
            }
            apply(WHStatusEffects.energyAmplification, DAMAGE_REDUCE_Duration);
        }

        if(abilityDuration > 1){
            heal(maxHealth * Time.delta / 60f * 1.5f / 100f);
            drawSize = Mathf.lerpDelta(drawSize, 1f, 0.08f);
        }else drawSize = Mathf.lerpDelta(drawSize, 0f, 0.1f);

        if(surroundBullets.size > 0){
            for(int i = 0; i < surroundBullets.size; i++){
                Bullet bullet = surroundBullets.get(i);
                if(bullet instanceof AB a){
                    int ta = Mathf.randomSeed(a.id, 90, 150);
                    float tg = Mathf.randomSeed(a.id, 360) + rotation;
                    float r = Mathf.randomSeed(a.id, 0.7f, 1f);
                    float angle = Time.time / 2 * r * (ta % 2 == 0 ? 1 : -1) + tg;
                    float tx = WHUtils.ellipseXY(x, y, ta, ta / 4f, tg, angle, 0);
                    float ty = WHUtils.ellipseXY(x, y, ta, ta / 4f, tg, angle, 1);
                    /*  a.layer = angle%360f > 180 ? -0.3f : 0.1f;*/
                    WHUtils.movePoint(a, tx, ty, 0.1f * r);
                    a.rotation(a.angleTo(tx, ty));
                    a.initVel(a.rotation(), 0);
                    a.team(team);
                    a.owner(this);
                    if(a.time > 30) a.time = 30;
                }
            }
        }
    }

    public void createBullet(){
        float
        bulletX = x + Angles.trnsx(rotation - 90, this.shootX, this.shootY),
        bulletY = y + Angles.trnsy(rotation - 90, this.shootX, this.shootY);
        BulletType b = bullets.random();
        Bullet b1 = b.create(this, this, this.team, bulletX, bulletY, rand.random(360), -1f,
        rand.random(0.7f, 1),
        1, null, null, aimX, aimY, null);
        surroundBullets.add(b1);
    }

    @Override
    public void destroy(){
        super.destroy();
        for(Bullet bu : surroundBullets){
            if(bu instanceof AB a){
                a.find = true;
                a.initVel(Mathf.randomSeed(bu.id, 360), bu.type.speed);
            }
        }
       /* Units.nearbyEnemies(team, x, y, CHECK_RANGE, other -> {
            if(other.maxHealth() > RECOVERY_HEALTH) enemies.add(other);
        });
        Vars.indexer.allBuildings(x, y, CHECK_RANGE, other -> {
            if(other.team != this.team && other.block.targetable && other.block.underBullets
            && other.maxHealth() > RECOVERY_HEALTH && other instanceof TurretBuild){
                enemies.add(other);
            }
        });*/
    }

    @Override
    public void read(Reads read){
        super.read(read);
        abilityTimer = read.f();
        bulletRecoveryTimer = read.f();
        checkReload = read.f();
        shootX = read.f();
        shootY = read.f();
        abilityDuration = read.f();
        drawSize = read.f();
        accumulateDamage = read.f();

        int size = read.i();
        surroundBullets.clear();

        for(int i = 0; i < size; i++){
            BulletType bulletType = readBulletType(read);
            float
            bulletX = x + Angles.trnsx(rotation - 90, this.shootX, this.shootY),
            bulletY = y + Angles.trnsy(rotation - 90, this.shootX, this.shootY);
            Bullet bullet = bulletType.create(this, this, this.team, bulletX, bulletY, rand.random(360), -1f,
            rand.random(0.7f, 1),
            1, null, null, aimX, aimY, null);
            surroundBullets.add(bullet);
        }

        int enemySize = read.i();
        enemies.clear();
        for(int i = 0; i < enemySize; i++){
            Healthc e = readUnit(read);
            enemies.add(e);
        }
    }

    @Override
    public void afterReadAll(){
        super.afterReadAll();
    }

    @Override
    public void write(Writes write){
        super.write(write);
        write.f(abilityTimer);
        write.f(bulletRecoveryTimer);
        write.f(checkReload);
        write.f(shootX);
        write.f(shootY);
        write.f(abilityDuration);
        write.f(drawSize);
        write.f(accumulateDamage);

        write.i(surroundBullets.size);

        for(Bullet bullet : surroundBullets){
            writeBulletType(write, bullet.type);
        }

        write.i(enemies.size);
        for(Healthc e : enemies){
            writeUnit(write, (Unit)e);
        }
    }
}
