package wh.gen;

import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ai.types.MissileAI;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.type.UnitType;
import mindustry.world.blocks.defense.Wall.WallBuild;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;
import wh.content.WHBulletsOther;
import wh.content.WHStatusEffects;
import wh.entities.bullet.ApproachBullet.AB;
import wh.net.packet.RevengeOrbitBulletPacket;
import wh.net.packet.RevengeOrbitCreatePacket;
import wh.util.WHUtils;

import static mindustry.io.TypeIO.*;
import static wh.util.WHUtils.rand;

public class RevengeUnit extends UnitEntity{

    public final float DAMAGE_REDUCE = 0.8f;
    public final float DAMAGE_REDUCE_Duration = 8 * 60f;
    public final float MAX_DAMAGE = 1000f;
    public final float ACCUMULATE_DAMAGE = 5000;

    public final float ABILITY_RELOAD = 15 * 60f;
    public final float BULLET_RECOVERY_TIME = 2.5f * 60f;
    public final float RECOVERY_HEALTH = 7000f;
    public final int MAX_BULLET = 5;
    public final float RANGE_CHECK = 120;

    public final float CHECK_RELOAD = 75;
    public final float CHECK_RANGE = 550;
    public final float ORBIT_COLLECT_INTERVAL = 10f;
    public final float ORBIT_COLLECT_RANGE = 320f;
    public float checkReload = CHECK_RELOAD;

    public float abilityTimer;
    public float rangeTimer;
    public float bulletRecoveryTimer;
    public float abilityDuration;
    public float drawSize;
    public float orbitCollectTimer;

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
        bullets.clear();
        bullets.add(WHBulletsOther.RevengeBullet1, WHBulletsOther.RevengeBullet2,
        WHBulletsOther.RevengeBullet3, WHBulletsOther.RevengeBullet4);
        orbitCollectTimer = 0f;
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
    }

    @Override
    public void update(){
        super.update();
        boolean server = !Vars.net.client();
        if (!server) {
            updateClientVisualState();
            updateOrbitBulletCache();
            surroundBullets.removeAll(e -> !isOrbitBulletCandidate(e));
            updateSurroundBulletsOrbit(false);
            return;
        }

        if (canShoot()) bulletRecoveryTimer += Time.delta * reloadMultiplier();

        rand.setSeed(id);

        checkReload -= Time.delta;
        rangeTimer -= Time.delta;
        abilityDuration -= Time.delta;

        updateOrbitBulletCache();
        surroundBullets.removeAll(e -> !isOrbitBulletCandidate(e));


        if(checkReload <= 0.0001f && surroundBullets.size > 0){
            checkReload = CHECK_RELOAD;
            Teamc en = Units.bestTarget(team, x, y, CHECK_RANGE,
            e -> !e.dead() && !(e instanceof TimedKillc),
            b -> !b.block.underBullets && b.maxHealth > 1000 && !(b instanceof WallBuild), UnitSorts.closest);

            if(en != null){
                int shots = Math.min(Mathf.random(1, 3), surroundBullets.size);
                for(int i = 0; i < shots && !surroundBullets.isEmpty(); i++){
                    int index = Mathf.random(surroundBullets.size - 1);
                    launchOrbitBulletAtIndex(index, en);
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

        if(accumulateDamage >= ACCUMULATE_DAMAGE && surroundBullets.size < MAX_BULLET * 3){
            accumulateDamage = 0;
            createBullet();
        }

        if(bulletRecoveryTimer > BULLET_RECOVERY_TIME && surroundBullets.size < MAX_BULLET){
            bulletRecoveryTimer = 0;
            createBullet();
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
                if(surroundBullets.isEmpty()) continue;
                int index = Mathf.random(surroundBullets.size - 1);
                launchOrbitBulletAtIndex(index, (Teamc) e);
            }
            apply(WHStatusEffects.energyAmplification, DAMAGE_REDUCE_Duration);
        }

        if(abilityDuration > 1){
            heal(maxHealth * Time.delta / 60f * 1.5f / 100f);
            drawSize = Mathf.lerpDelta(drawSize, 1f, 0.08f);
        }else drawSize = Mathf.lerpDelta(drawSize, 0f, 0.1f);

        updateSurroundBulletsOrbit(true);
    }

    public void updateClientVisualState() {
        abilityDuration = Math.max(abilityDuration - Time.delta, 0f);
        if (abilityDuration > 1f) {
            drawSize = Mathf.lerpDelta(drawSize, 1f, 0.08f);
        } else {
            drawSize = Mathf.lerpDelta(drawSize, 0f, 0.1f);
        }
    }

    public void updateSurroundBulletsOrbit(boolean authority) {
        if (surroundBullets.isEmpty()) return;

        for (int i = 0; i < surroundBullets.size; i++) {
            Bullet bullet = surroundBullets.get(i);
            if (!(bullet instanceof AB a)) continue;
            int ta = Mathf.randomSeed(a.id, 90, 150);
            float tg = Mathf.randomSeed(a.id, 360) + rotation;
            float r = Mathf.randomSeed(a.id, 0.7f, 1f);
            float angle = Time.time / 2 * r * (ta % 2 == 0 ? 1 : -1) + tg;
            float tx = WHUtils.ellipseXY(x, y, ta, ta / 4f, tg, angle, 0);
            float ty = WHUtils.ellipseXY(x, y, ta, ta / 4f, tg, angle, 1);

            WHUtils.movePoint(a, tx, ty, 0.1f * r);
            a.rotation(a.angleTo(tx, ty));
            a.initVel(a.rotation(), 0f);
            if (authority) {
                a.team(team);
                a.owner(this);
            }
            if (a.time > 30f) a.time = 30f;
        }
    }

    public void launchOrbitBulletAtIndex(int index, Teamc target) {
        if (index < 0 || index >= surroundBullets.size) return;
        Bullet bullet = surroundBullets.remove(index);
        if (!(bullet instanceof AB a)) return;
        int bulletId = bullet.id;

        boolean invalidTarget = !(target instanceof Healthc enemy) || enemy.dead() || !enemy.isValid();
        a.target = target;
        if (invalidTarget) a.find = true;
        float launchAngle = target == null ? bullet.rotation() : bullet.angleTo(target);
        a.initVel(launchAngle, bullet.type.speed);
        sendOrbitBulletLaunchPacket(bulletId, launchAngle, target == null ? -1 : target.id(), a.find);
    }

    public boolean applyOrbitLaunch(int bulletId, float launchAngle, int targetId, boolean forceFind) {
        Bullet bullet = null;
        for (int i = surroundBullets.size - 1; i >= 0; i--) {
            Bullet candidate = surroundBullets.get(i);
            if (candidate != null && candidate.id == bulletId) {
                bullet = candidate;
                surroundBullets.remove(i);
                break;
            }
        }
        if (bullet == null) {
            try {
                bullet = Groups.bullet.getByID(bulletId);
            } catch (RuntimeException ignored) {
                bullet = null;
            }
        }
        if (!(bullet instanceof AB a)) return false;
        surroundBullets.remove(bullet, true);
        Teamc resolvedTarget = null;
        if (targetId >= 0) {
            Entityc entity;
            try {
                entity = Groups.sync.getByID(targetId);
            } catch (RuntimeException ignored) {
                entity = null;
            }
            if (entity instanceof Teamc) resolvedTarget = (Teamc) entity;
        }

        a.target = resolvedTarget;
        a.find = forceFind || resolvedTarget == null || a.find;
        a.initVel(launchAngle, bullet.type.speed);
        return true;
    }

    public boolean applyOrbitLaunchEvent(int bulletId, float launchAngle, int targetId, boolean forceFind) {
        return applyOrbitLaunch(bulletId, launchAngle, targetId, forceFind);
    }

    public void updateOrbitBulletCache() {
        orbitCollectTimer -= Time.delta;
        if (orbitCollectTimer > 0f) return;
        orbitCollectTimer = ORBIT_COLLECT_INTERVAL;

        float range = ORBIT_COLLECT_RANGE + hitSize * 0.5f;
        Groups.bullet.intersect(x - range, y - range, range * 2f, range * 2f, this::addOrbitBullet);
    }

    public boolean applyOrbitBulletCreate(BulletType bulletType, Team bulletTeam, int bulletId, float bulletX, float bulletY, float fireAngle, float velocityScl, float aimX, float aimY) {
        if (!(bulletType.create(this, this, bulletTeam == null ? team : bulletTeam, bulletX, bulletY, fireAngle, -1f, velocityScl, 1f, null, null, aimX, aimY, null) instanceof AB bullet)) {
            return false;
        }
        bullet.id(bulletId);
        bullet.target = null;
        bullet.find = false;
        bullet.team(bulletTeam == null ? team : bulletTeam);
        bullet.owner(this);
        bullet.time = 0f;
        if (!surroundBullets.contains(bullet, true)) {
            surroundBullets.add(bullet);
        }
        return true;
    }

    public void sendOrbitBulletCreatePacket(Bullet bullet, float bulletX, float bulletY, float fireAngle, float velocityScl) {
        if (!Vars.net.server() || bullet == null) return;
        RevengeOrbitCreatePacket packet = new RevengeOrbitCreatePacket();
        packet.ownerId = id;
        packet.bulletId = bullet.id;
        packet.type = bullet.type;
        packet.team = bullet.team;
        packet.x = bulletX;
        packet.y = bulletY;
        packet.angle = fireAngle;
        packet.velocityScl = velocityScl;
        packet.aimX = aimX;
        packet.aimY = aimY;
        Vars.net.send(packet, true);
    }

    public void sendOrbitBulletLaunchPacket(int bulletId, float launchAngle, int targetId, boolean forceFind) {
        if (!Vars.net.server()) return;
        RevengeOrbitBulletPacket packet = new RevengeOrbitBulletPacket();
        packet.ownerId = id;
        packet.bulletId = bulletId;
        packet.launchAngle = launchAngle;
        packet.targetId = targetId;
        packet.forceFind = forceFind;
        Vars.net.send(packet, true);
    }

    public void createBullet(){
        if (Vars.net.client()) return;
        float
        bulletX = x + Angles.trnsx(rotation - 90, this.shootX, this.shootY),
        bulletY = y + Angles.trnsy(rotation - 90, this.shootX, this.shootY);
        BulletType b = bullets.random();
        float angle = rand.random(360f);
        float velocityScl = rand.random(0.7f, 1f);
        Bullet b1 = b.create(this, this, this.team, bulletX, bulletY, angle, -1f,
                velocityScl,
                1f, null, null, aimX, aimY, null);
        if (b1 != null) {
            surroundBullets.add(b1);
            sendOrbitBulletCreatePacket(b1, bulletX, bulletY, angle, velocityScl);
        }
    }

    public boolean matchesOrbitBulletOwner(Entityc owner) {
        if (owner == this) return true;
        return owner instanceof Unit unit && unit.id == id;
    }

    public boolean isOrbitBulletCandidate(Bullet bullet) {
        if (bullet == null || !bullet.isAdded()) return false;
        if (!(bullet instanceof AB a)) return false;
        if (bullet.team != team) return false;
        if (!bullets.contains(bullet.type, true)) return false;

        if (bullet.time > 45f) return false;
        if (a.target != null || a.find) return false;

        Entityc owner = bullet.owner();
        if (matchesOrbitBulletOwner(owner)) return true;
        if (!Vars.net.client() || owner != null) return false;

        float range = ORBIT_COLLECT_RANGE + hitSize * 0.9f;
        return Mathf.within(x, y, bullet.x, bullet.y, range);
    }

    public void addOrbitBullet(Bullet bullet) {
        if (!isOrbitBulletCandidate(bullet)) return;
        if (!surroundBullets.contains(bullet, true)) {
            surroundBullets.add(bullet);
        }
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
            Entityc entity = readEntity(read);
            if (entity instanceof Healthc) {
                enemies.add((Healthc) entity);
            }
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
            writeEntity(write, e);
        }
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);
        write.f(abilityDuration);
        write.f(drawSize);
        write.f(checkReload);
        write.f(bulletRecoveryTimer);
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);
        float syncAbilityDuration = read.f();
        float syncDrawSize = read.f();
        float syncCheckReload = read.f();
        float syncBulletRecoveryTimer = read.f();

        if (!isLocal()) {
            abilityDuration = syncAbilityDuration;
            drawSize = syncDrawSize;
            checkReload = syncCheckReload;
            bulletRecoveryTimer = syncBulletRecoveryTimer;
        }
    }
}
