package wh.gen;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.PayloadUnit;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import wh.entities.world.entities.CMoonUnitType;
import wh.graphics.CMoonVoidShieldRenderer;

public class CMoonUnit extends PayloadUnit {
    private static final float shieldEpsilon = 0.0001f;
    private static final float bodyCollisionLengthMultiplier = 2.2f;

    public float voidShield;
    public float voidShieldAlpha = 0.35f;
    public float overloadTimer;
    public float recoveryTimer;
    public float voidShieldDamageBudget;

    private int nearbySupportCount;
    private CMoonUnitType cMoonType;

    public static CMoonUnit create() {
        return new CMoonUnit();
    }

    @Override
    public void setType(UnitType type) {
        super.setType(type);
        cMoonType = type instanceof CMoonUnitType moonType ? moonType : null;
        if (cMoonType != null && !isAdded()) {
            voidShield = cMoonType.voidShieldCapacity;
            voidShieldDamageBudget = cMoonType.voidShieldDamagePerSecond;
        }
    }

    @Override
    public void add() {
        super.add();
        CMoonVoidShieldRenderer.add(this);
    }

    @Override
    public void remove() {
        CMoonVoidShieldRenderer.remove(this);
        super.remove();
    }

    @Override
    public int classId() {
        return EntityRegister.getId(CMoonUnit.class);
    }

    @Override
    public void hitbox(Rect out) {
        if (cMoonType == null) {
            super.hitbox(out);
        } else {
            out.setCentered(x, y, hitSize, hitSize * bodyCollisionLengthMultiplier);
        }
    }

    public float shieldLongAxis() {
        return cMoonType == null ? 0f : cMoonType.voidShieldLongAxis;
    }

    public float shieldMinorAxis() {
        return cMoonType == null ? 0f : cMoonType.voidShieldMinorAxis;
    }

    public float shieldRotation() {
        return rotation + (cMoonType == null ? 0f : cMoonType.voidShieldRotationOffset);
    }

    private float bodyCollisionLongAxis() {
        return hitSize * bodyCollisionLengthMultiplier * 0.5f;
    }

    private float bodyCollisionMinorAxis() {
        return hitSize * 0.5f;
    }

    public Color voidShieldColor() {
        return cMoonType == null ? Color.white : cMoonType.voidShieldColor;
    }

    public static boolean inEllipse(float pointX, float pointY, Unit unit, float longAxis, float minorAxis) {
        float ellipseRotation = unit instanceof CMoonUnit moon ? moon.shieldRotation() : unit.rotation;
        return inEllipse(pointX, pointY, unit.x, unit.y, ellipseRotation, longAxis, minorAxis);
    }

    private static boolean inEllipse(float pointX, float pointY, float centerX, float centerY, float ellipseRotation, float longAxis, float minorAxis) {
        if (longAxis <= 0f || minorAxis <= 0f) return false;

        float offsetX = pointX - centerX;
        float offsetY = pointY - centerY;
        float cos = Mathf.cosDeg(ellipseRotation);
        float sin = Mathf.sinDeg(ellipseRotation);
        float localX = offsetX * cos + offsetY * sin;
        float localY = -offsetX * sin + offsetY * cos;
        float normalizedX = localX / longAxis;
        float normalizedY = localY / minorAxis;
        return normalizedX * normalizedX + normalizedY * normalizedY <= 1f;
    }

    private final Cons<Bullet> shieldBulletConsumer = bullet -> {
        if (bullet.team != team && bullet.type.collides && bullet.type.absorbable && inEllipse(bullet.x, bullet.y, this, shieldLongAxis(), shieldMinorAxis())) {
            absorbBullet(bullet);
        }
    };

    private final Cons<Unit> supportUnitConsumer = unit -> {
        if (unit != this && !unit.dead && unit.team == team && unit.maxHealth >= cMoonType.lastStandHealth
                && Mathf.dst2(x, y, unit.x, unit.y) <= cMoonType.lastStandRange * cMoonType.lastStandRange) {
            nearbySupportCount++;
        }
    };

    private final Cons<Unit> ellipseCollisionConsumer = unit -> {
        if (unit == this || unit.dead || !unit.isAdded() || !unit.isFlying() || unit.hitSize() <= 0f) return;
        float offsetX = unit.x - x;
        float offsetY = unit.y - y;
        if (Mathf.zero(offsetX) && Mathf.zero(offsetY)) offsetY = 1f;

        float distance = Mathf.len(offsetX, offsetY);
        float ellipseRadius = ellipseRadiusAt(Mathf.angle(offsetX, offsetY), bodyCollisionLongAxis(), bodyCollisionMinorAxis(), rotation) + unit.hitSize() * 0.5f;
        if (distance >= ellipseRadius) return;

        float push = (ellipseRadius - distance) * 0.35f;
        unit.move(offsetX / Math.max(distance, 0.001f) * push, offsetY / Math.max(distance, 0.001f) * push);
    };

    private static float ellipseRadiusAt(float worldAngle, float longAxis, float minorAxis, float ellipseRotation) {
        float angle = (worldAngle - ellipseRotation) * Mathf.degRad;
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        return longAxis * minorAxis / (float) Math.sqrt(
                minorAxis * minorAxis * cos * cos + longAxis * longAxis * sin * sin);
    }

    private boolean isAuthority() {
        return !Vars.net.client() || isLocal();
    }

    private boolean shieldActive() {
        return voidShield > shieldEpsilon && overloadTimer <= 0f && recoveryTimer <= 0f;
    }

    private float voidShieldCost(float damage) {
        return damage > cMoonType.voidShieldMediumDamage ? cMoonType.voidShieldLargeCost
                : damage >= cMoonType.voidShieldFreeDamage ? cMoonType.voidShieldMediumCost
                : cMoonType.voidShieldFreeCost;
    }

    private void absorbBullet(Bullet bullet) {
        float damage = bullet.damage();
        float shieldCost = voidShieldCost(damage);

        bullet.absorb();
        Fx.absorb.at(bullet.x, bullet.y, bullet.rotation(), team.color);
        voidShieldAlpha = 1f;

        if (shieldCost <= 0f) return;

        voidShield = Math.max(0f, voidShield - shieldCost);
        if (voidShield <= shieldEpsilon) {
            overloadVoidShield();
        }
    }

    private void overloadVoidShield() {
        overloadTimer = cMoonType.voidShieldOverloadDuration;
        recoveryTimer = 0f;
    }

    @Override
    public void rawDamage(float amount) {
        if (cMoonType == null) {
            super.rawDamage(amount);
            return;
        }

        boolean hadVoidShield = shieldActive();
        boolean hadShield = shield > shieldEpsilon;
        if (Float.isNaN(health)) health = 0f;

        if (hadVoidShield && amount < cMoonType.voidShieldFreeDamage && cMoonType.voidShieldFreeCost <= 0f) {
            voidShieldAlpha = 1f;
            return;
        }

        if (hadVoidShield) {
            float voidDamage = Math.min(amount, Math.min(voidShield, voidShieldDamageBudget));
            if (voidDamage > 0f) {
                voidShield -= voidDamage;
                voidShieldDamageBudget -= voidDamage;
                amount -= voidDamage;
                voidShieldAlpha = 1f;
            }

            if (voidShield <= shieldEpsilon) {
                voidShield = 0f;
                overloadVoidShield();
            }
        }

        if (amount > 0f && shield > shieldEpsilon) {
            float shieldDamage = Math.min(shield, amount);
            shield -= shieldDamage;
            amount -= shieldDamage;
            shieldAlpha = 1f;

            if (shield <= shieldEpsilon) {
                Fx.unitShieldBreak.at(x, y, 0f, type.shieldColor(self()), this);
            }
        }

        if (amount > 0f && type.killable) {
            health -= amount;
            if (health <= 0f && !dead) kill();
        }
    }

    private boolean hasLastStandSupport() {
        nearbySupportCount = 0;
        float range = cMoonType.lastStandRange;
        Groups.unit.intersect(x - range, y - range, range * 2f, range * 2f, supportUnitConsumer);
        return nearbySupportCount >= cMoonType.lastStandUnits;
    }

    @Override
    public void kill() {
        if (!dead && hasLastStandSupport()) {
            health = Math.max(1f, health);
            return;
        }
        super.kill();
    }

    @Override
    public void update() {
        super.update();

        voidShieldAlpha = Mathf.approachDelta(voidShieldAlpha, shieldActive() ? 0.35f : 0f, 0.025f);
        if (!isAuthority()) return;

        if (overloadTimer > 0f) {
            overloadTimer = Math.max(0f, overloadTimer - Time.delta);
            if (overloadTimer <= 0f) recoveryTimer = cMoonType.voidShieldRecoveryDuration;
        } else if (recoveryTimer > 0f) {
            recoveryTimer = Math.max(0f, recoveryTimer - Time.delta);
        } else {
            voidShield = Math.min(cMoonType.voidShieldCapacity, voidShield + cMoonType.voidShieldRegen / 60f * Time.delta);
        }

        voidShieldDamageBudget = Math.min(cMoonType.voidShieldDamagePerSecond,
                voidShieldDamageBudget + cMoonType.voidShieldDamagePerSecond / 60f * Time.delta);

        if (shieldActive()) {
            float longAxis = shieldLongAxis();
            Groups.bullet.intersect(x - longAxis, y - longAxis, longAxis * 2f, longAxis * 2f, shieldBulletConsumer);
        }

        float longAxis = bodyCollisionLongAxis();
        Groups.unit.intersect(x - longAxis, y - longAxis, longAxis * 2f, longAxis * 2f, ellipseCollisionConsumer);
    }

    public int shieldRenderState() {
        return overloadTimer > 0f ? 1 : recoveryTimer > 0f ? 2 : 0;
    }

    public float shieldStateProgress() {
        if (overloadTimer > 0f) return 1f - overloadTimer / cMoonType.voidShieldOverloadDuration;
        if (recoveryTimer > 0f) return 1f - recoveryTimer / cMoonType.voidShieldRecoveryDuration;
        return 0f;
    }

    public float shieldFraction() {
        return voidShield / cMoonType.voidShieldCapacity;
    }

    @Override
    public void display(Table table) {
        super.display(table);
        table.row();
        table.table(bars -> {
            bars.defaults().growX().pad(5f).height(20f);
            bars.add(new Bar(() -> Core.bundle.get("stat.shieldhealth"), this::voidShieldColor, this::shieldFraction));
        }).growX().padBottom(5f);
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        write.f(voidShield);
        write.f(voidShieldAlpha);
        write.f(overloadTimer);
        write.f(recoveryTimer);
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        voidShield = read.f();
        voidShieldAlpha = read.f();
        overloadTimer = read.f();
        recoveryTimer = read.f();
    }
}
