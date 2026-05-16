package wh.gen;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.Effect;
import mindustry.entities.units.WeaponMount;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.gen.UnitEntity;
import mindustry.io.TypeIO;

public class CarrierFighterUnit extends UnitEntity {
    public static final float dodgeChance = 0.25f;
    public static final float dodgeCooldown = 20;
    public static final float dodgeBoostDuration = 120;
    public static final float dodgeBoostMultiplier = 1.45f;
    public static final float dodgeBoostAccel = 0.22f;
    public static final float dodgeImpulseScale = 1.65f;
    public static final float dodgeIFrameDuration = 5f;

    public float dodgeCooldownTimer = 0f;
    public float dodgeBoostTimer = 0f;
    public float dodgeIFrameTimer = 0f;

    public int carrierId = -1;
    public int runway = 0;
    public boolean returning = false;
    public boolean landing = false;
    public float takeoffTimer = 0f;
    public float takeoffDuration = 1f;
    public float takeoffSpeedMultiplier = 1.2f;
    public float recallGraceTimer = 0f;
    public float landingTimer = 0f;
    public float landingDuration = 1f;
    public float landingAngle = 0f;
    public final Vec2 takeoffFrom = new Vec2();
    public final Vec2 takeoffTo = new Vec2();
    public final Vec2 landingTo = new Vec2();

    @Override
    public int classId() {
        return EntityRegister.getId(CarrierFighterUnit.class);
    }

    public CarrierFighterUnit setCarrierBinding(int carrierId, int runway) {
        this.carrierId = carrierId;
        this.runway = Math.max(runway, 0);
        return this;
    }

    public void clearCarrierBinding() {
        carrierId = -1;
        runway = 0;
        returning = false;
        landing = false;
        takeoffTimer = 0f;
        takeoffDuration = 1f;
        takeoffSpeedMultiplier = 1.2f;
        recallGraceTimer = 0f;
        landingTimer = 0f;
        landingDuration = 1f;
        landingAngle = 0f;
        takeoffFrom.setZero();
        takeoffTo.setZero();
        landingTo.setZero();
    }

    @Override
    public void update() {
        super.update();

        dodgeCooldownTimer = Math.max(0f, dodgeCooldownTimer - Time.delta);
        dodgeIFrameTimer = Math.max(0f, dodgeIFrameTimer - Time.delta);

        if (dodgeBoostTimer > 0f) {
            dodgeBoostTimer = Math.max(0f, dodgeBoostTimer - Time.delta);

            float boostedMaxSpeed = Math.max(type.speed * dodgeBoostMultiplier, type.speed + 0.1f);
            vel.add(Tmp.v2.trns(rotation, dodgeBoostAccel * Time.delta));
            vel.limit(boostedMaxSpeed);
        }
    }

    @Override
    public void collision(Hitboxc other, float x, float y) {
        if (other instanceof Bullet bullet && shouldDodge(bullet)) {
            performDodge(bullet);
            dodgeIFrameTimer = Math.max(dodgeIFrameTimer, dodgeIFrameDuration);
            return;
        }
        super.collision(other, x, y);
    }

    @Override
    public void rawDamage(float amount) {
        if (dodgeIFrameTimer > 0f) {
            amount *= 0.1f;
        }
        super.rawDamage(amount);
    }

    @Override
    public float mass() {
        return this.hitSize * (float) Math.PI * 50;
    }

    @Override
    public boolean targetable(Team targeter) {
        return hasOffensiveWeapon() && super.targetable(targeter);
    }

    @Override
    public boolean checkTarget(boolean targetAir, boolean targetGround) {
        return hasOffensiveWeapon() && super.checkTarget(targetAir, targetGround);
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        writeCarrierState(write);
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        readCarrierState(read);
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);
        writeCarrierState(write);
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);
        readCarrierState(read);
    }

    protected boolean shouldDodge(Bullet bullet) {
        if (bullet == null || bullet.team == team) return false;
        if (bullet.type == null || !bullet.type.hittable) return false;
        if (dodgeCooldownTimer > 0f) return false;
        return Mathf.chance(dodgeChance);
    }

    protected void performDodge(Bullet bullet) {
        Vec2 dodgeDir = Tmp.v1.set(bullet.vel);
        if (dodgeDir.len2() < 0.0001f) {
            dodgeDir.trns(rotation, 1f);
        }

        dodgeDir.rotate(Mathf.chance(0.5f) ? 90f : -90f).nor();
        float impulse = Math.max(type.speed * dodgeImpulseScale, 0.9f);
        vel.add(dodgeDir.scl(impulse));

        dodgeCooldownTimer = dodgeCooldown;
        dodgeBoostTimer = Math.max(dodgeBoostTimer, dodgeBoostDuration);
        Effect.shake(0.6f, 0.8f, x, y);
    }

    protected boolean hasOffensiveWeapon() {
        if (mounts == null || mounts.length == 0) return false;
        for (WeaponMount mount : mounts) {
            if (mount == null || mount.weapon == null) continue;
            if (mount.weapon.noAttack) continue;
            if (mount.weapon.bullet == null) continue;
            return true;
        }
        return false;
    }

    protected void writeCarrierState(Writes write) {
        write.i(carrierId);
        write.i(runway);
        write.bool(returning);
        write.bool(landing);
        write.f(takeoffTimer);
        write.f(takeoffDuration);
        write.f(takeoffSpeedMultiplier);
        write.f(recallGraceTimer);
        write.f(landingTimer);
        write.f(landingDuration);
        write.f(landingAngle);
        TypeIO.writeVec2(write, takeoffFrom);
        TypeIO.writeVec2(write, takeoffTo);
        TypeIO.writeVec2(write, landingTo);
    }

    protected void readCarrierState(Reads read) {
        carrierId = read.i();
        runway = Math.max(read.i(), 0);
        returning = read.bool();
        landing = read.bool();
        takeoffTimer = Math.max(read.f(), 0f);
        takeoffDuration = Math.max(read.f(), 1f);
        takeoffSpeedMultiplier = Math.max(read.f(), 1f);
        recallGraceTimer = Math.max(read.f(), 0f);
        landingTimer = Math.max(read.f(), 0f);
        landingDuration = Math.max(read.f(), 1f);
        landingAngle = read.f();
        TypeIO.readVec2(read, takeoffFrom);
        TypeIO.readVec2(read, takeoffTo);
        TypeIO.readVec2(read, landingTo);
    }
}
