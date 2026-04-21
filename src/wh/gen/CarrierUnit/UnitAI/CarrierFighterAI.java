package wh.gen.CarrierUnit.UnitAI;

import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.ai.types.FlyingAI;
import mindustry.ai.types.GroundAI;
import mindustry.entities.Predict;
import mindustry.entities.Sized;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.units.UnitController;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.type.Weapon;
import wh.entities.world.entities.CarrierUnitType;
import wh.gen.CarrierFighterUnit;
import wh.gen.CarrierUnit.CarrierHostc;

public class CarrierFighterAI extends FlyingAI implements CarrierBoundAIC {
    private static final float eps = 0.001f;
    private static final float landingAngleThreshold = 14f;

    protected int carrierId = -1;
    protected int runwayIndex = 0;
    protected boolean returning;
    protected boolean approachingRecovery;

    protected float takeoffTimer = 0f;
    protected float takeoffDuration = 1f;
    protected float takeoffSpeedMultiplier = 1.2f;
    protected float takeoffLiftStart = 0.6f;
    protected float landingTimer = 0f;
    protected float landingDuration = 1f;
    protected float landingAngle = 0f;
    protected final Vec2 takeoffFrom = new Vec2();
    protected final Vec2 takeoffTo = new Vec2();
    protected final Vec2 takeoffDir = new Vec2();
    protected final Vec2 takeoffMove = new Vec2();
    protected final Vec2 landingTo = new Vec2();
    protected final Vec2 recoveryMove = new Vec2();
    protected final Vec2 focusPoint = new Vec2();
    protected float takeoffCruiseSpeed = 0f;
    protected float recallGraceTimer = 0f;
    protected float noTargetTimer = 0f;
    protected float missingCarrierTimer = 0f;

    protected float debugOrbitRadius = 0f;
    protected float debugOrbitBand = 0f;
    protected float debugOrbitAngleError = 0f;
    protected boolean debugInOrbitBand = false;
    protected boolean debugInEntryWindow = false;
    protected boolean debugClaimBlocked = false;
    protected boolean debugWeaponHasTarget = false;
    protected boolean debugWeaponAllowShoot = false;
    protected boolean debugWeaponAllowFire = false;
    protected @Nullable Teamc debugWeaponTarget;
    protected String debugRecallReason = "none";

    public CarrierFighterAI() {
    }

    public CarrierFighterAI(int carrierId) {
        this(carrierId, 0);
    }

    public CarrierFighterAI(int carrierId, int runwayIndex) {
        this.carrierId = carrierId;
        this.runwayIndex = Math.max(runwayIndex, 0);
    }

    public CarrierFighterAI(Unit unit) {
    }

    public int carrierId() {
        CarrierFighterUnit data = carrierData();
        return data == null ? carrierId : data.carrierId;
    }

    @Override
    public CarrierFighterAI setCarrier(int carrierId) {
        this.carrierId = carrierId;
        pushStateToUnit();
        return this;
    }

    @Override
    public CarrierFighterAI setRunway(int runwayIndex) {
        this.runwayIndex = Math.max(runwayIndex, 0);
        pushStateToUnit();
        return this;
    }

    @Override
    public CarrierFighterAI beginTakeoff(Vec2 from, Vec2 to, float duration, float speedMultiplier) {
        CarrierHostc carrier = carrier();

        if (from != null) {
            takeoffFrom.set(from);
        } else if (carrier != null) {
            carrier.runwayFrontPoint(runwayIndex, takeoffFrom);
        } else if (unit != null) {
            takeoffFrom.set(unit.x, unit.y);
        } else {
            takeoffFrom.setZero();
        }

        if (to != null) {
            takeoffTo.set(to);
        } else if (carrier != null) {
            carrier.launchExitPoint(runwayIndex, takeoffTo);
        } else if (unit != null) {
            Tmp.v1.trns(unit.rotation, Math.max(unit.hitSize * 2f, 28f));
            takeoffTo.set(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y);
        } else {
            takeoffTo.setZero();
        }

        sanitizeTakeoffPoints(carrier);
        takeoffDuration = Math.max(duration, 1f);
        takeoffTimer = takeoffDuration;
        takeoffSpeedMultiplier = Math.max(speedMultiplier, 1f);
        noTargetTimer = 0f;
        resetRecoveryState(true);
        landingDuration = 1f;
        recallGraceTimer = Math.max(90f, takeoffDuration * 0.6f);

        refreshTakeoffDirection(carrier);
        float base = unit == null ? 1f : Math.max(prefSpeed(), Math.max(unit.type == null ? 0f : unit.type.speed, 0.1f));
        takeoffCruiseSpeed = Math.max(base * takeoffSpeedMultiplier, 1f);

        if (unit != null) {
            unit.vel.set(takeoffDir).scl(takeoffCruiseSpeed * 0.35f);
            unit.rotation = takeoffDir.angle();
            if (unit.type.flying) {
                unit.elevation = Math.max(unit.elevation, 0.32f);
            }
        }

        pushStateToUnit();
        return this;
    }

    public boolean isLanding() {
        return approachingRecovery;
    }

    @Override
    public boolean canRecoverNow() {
        if (unit == null || !returning || !approachingRecovery) return false;
        return landingTimer <= Math.max(Time.delta * 1.5f, 1f);
    }

    @Override
    public boolean isReturning() {
        return returning;
    }

    public int runwayIndex() {
        return carrierData() == null ? runwayIndex : Math.max(carrierData().runway, 0);
    }

    public @Nullable CarrierHostc carrierDebug() {
        return carrier();
    }

    public float debugOrbitRadius() {
        return debugOrbitRadius;
    }

    public float debugOrbitBand() {
        return debugOrbitBand;
    }

    public float debugOrbitAngleError() {
        return debugOrbitAngleError;
    }

    public boolean debugInOrbitBand() {
        return debugInOrbitBand;
    }

    public boolean debugInEntryWindow() {
        return debugInEntryWindow;
    }

    public boolean debugClaimBlocked() {
        return debugClaimBlocked;
    }

    public String debugStage() {
        if (takeoffTimer > eps) return "takeoff";
        if (returning) {
            if (landingTimer > eps) return "landing";
            if (approachingRecovery) return "final";
            return "return";
        }
        return "combat";
    }

    public float debugTakeoffTimer() {
        return takeoffTimer;
    }

    public float debugRecallGraceTimer() {
        return recallGraceTimer;
    }

    public float debugNoTargetTimer() {
        return noTargetTimer;
    }

    public String debugRecallReason() {
        return debugRecallReason;
    }

    public boolean debugWeaponHasTarget() {
        return debugWeaponHasTarget;
    }

    public boolean debugWeaponAllowShoot() {
        return debugWeaponAllowShoot;
    }

    public boolean debugWeaponAllowFire() {
        return debugWeaponAllowFire;
    }

    public @Nullable Teamc debugWeaponTarget() {
        return debugWeaponTarget;
    }

    public void debugTakeoffFrom(Vec2 out) {
        if (out != null) out.set(takeoffFrom);
    }

    public void debugTakeoffTo(Vec2 out) {
        if (out != null) out.set(takeoffTo);
    }

    @Override
    public void afterRead(Unit unit) {
        super.afterRead(unit);
        pullStateFromUnit();
    }

    @Override
    public void updateMovement() {
        pullStateFromUnit();

        CarrierHostc carrier = carrier();
        CarrierUnitType type = carrier == null ? null : carrier.carrierType();
        if (carrier == null || type == null) {
            if (waitForCarrier()) return;
            if (switchToDefaultController()) return;
            fallbackMovement();
            return;
        }

        // 这是战机移动主状态机：先处理与航母的上下文同步，
        // 再按“起飞 -> 返航/回收 -> 常规战斗机动”的优先级逐层更新。
        prepareCarrierContext(carrier);
        if (updateTakeoff(carrier)) return;
        if (updateRecall(carrier, type)) return;
        updateCombatMovement(carrier, type);

        pushStateToUnit();
    }

    @Override
    public void updateWeapons() {
        pullStateFromUnit();

        CarrierHostc carrier = carrier();
        CarrierUnitType type = carrier == null ? null : carrier.carrierType();
        resetWeaponDebug();

        if (carrier == null || type == null) {
            if (carrierId >= 0) {
                stopShooting();
                return;
            }
            if (switchToDefaultController()) return;
            super.updateWeapons();
            return;
        }

        if (takeoffTimer > eps || returning) {
            stopShooting();
            return;
        }

        Teamc engage = acquireEngageTarget(carrier, type);
        if (engage == null) {
            clearWeaponTargets();
            return;
        }

        target = engage;
        debugWeaponHasTarget = true;
        debugWeaponTarget = engage;
        unit.isShooting = false;
        float rotation = unit.rotation - 90f;
        boolean allowShoot = shouldShoot();
        boolean allowFire = shouldFire();
        debugWeaponAllowShoot = allowShoot;
        debugWeaponAllowFire = allowFire;

        for (WeaponMount mount : unit.mounts) {
            updateWeaponMount(carrier, type, mount, engage, rotation, allowShoot, allowFire);
        }
    }

    protected boolean waitForCarrier() {
        if (carrierId < 0) return false;

        missingCarrierTimer += Time.delta;
        if (missingCarrierTimer >= 180f) return false;

        stopShooting();
        unit.vel.scl(0.96f);
        pushStateToUnit();
        return true;
    }

    protected void prepareCarrierContext(CarrierHostc carrier) {
        missingCarrierTimer = 0f;
        runwayIndex = carrier.clampRunway(runwayIndex);
        if (unit.team != carrier.team()) {
            unit.team = carrier.team();
        }

        Tmp.v4.set(unit.x, unit.y);
        if (!invalidCarrierPoint(Tmp.v4, carrier)) return;

        carrier.runwayFrontPoint(runwayIndex, Tmp.v4);
        if (invalidCarrierPoint(Tmp.v4, carrier)) {
            Tmp.v4.set(carrier.x(), carrier.y());
        }
        unit.set(Tmp.v4.x, Tmp.v4.y);
        unit.vel.setZero();
    }

    protected boolean updateTakeoff(CarrierHostc carrier) {
        if (takeoffTimer <= eps) return false;

        debugRecallReason = "takeoff";
        sanitizeTakeoffPoints(carrier);
        refreshTakeoffDirection(carrier);

        float linear = Mathf.clamp(1f - takeoffTimer / takeoffDuration, 0f, 1f);
        float progress = Interp.pow2Out.apply(linear);
        float baseSpeed = Math.max(takeoffCruiseSpeed, Math.max(prefSpeed(), 0.1f));
        float speed = baseSpeed * Mathf.lerp(0.35f, 1f, progress);
        takeoffMove.set(takeoffDir).scl(speed);
        unit.moveAt(takeoffMove);
        unit.vel.set(takeoffMove);
        unit.lookAt(unit.x + takeoffDir.x * 12f, unit.y + takeoffDir.y * 12f);

        if (unit.type.flying) {
            float liftProgress = Mathf.curve(linear, takeoffLiftStart, 1f);
            float elev = Mathf.clamp(Interp.sineOut.apply(liftProgress), 0f, 1f);
            unit.elevation = Math.max(elev, 0.3f);
        }

        takeoffTimer = Math.max(0f, takeoffTimer - Time.delta);
        if (takeoffTimer <= eps) {
            takeoffTimer = 0f;
            if (unit.type.flying) {
                unit.elevation = 1f;
            }
            unit.vel.set(takeoffDir).scl(Math.max(baseSpeed, 0.1f));
        }

        // 起飞阶段由定时器驱动：逐步修正起飞点/方向、平滑抬升高度，
        // 并在计时结束时把飞机切回正常巡航速度。
        pushStateToUnit();
        return true;
    }

    protected boolean updateRecall(CarrierHostc carrier, CarrierUnitType type) {
        recallGraceTimer = Math.max(0f, recallGraceTimer - Time.delta);
        if (!returning && recallGraceTimer <= eps) {
            String reason = recallReasonFromCarrier(carrier, type);
            if (reason != null) {
                returning = true;
                debugRecallReason = reason;
            }
        }
        if (!returning) return false;

        // 返航阶段负责从“是否该召回”过渡到真正的回收机动；
        // 一旦触发返航，就把后续路径、排队、进场和着舰都交给 updateReturn()。
        updateReturn(carrier, type);
        pushStateToUnit();
        return true;
    }

    protected void updateCombatMovement(CarrierHostc carrier, CarrierUnitType type) {
        resetRecoveryState(false);
        debugRecallReason = "none";
        carrier.releaseRecoveryClaim(unit);

        // 非返航状态下执行常规战斗逻辑：优先追击目标，其次围绕航母焦点机动，
        // 都没有时就在航母周围巡航，同时主动释放回收 claim，避免占着跑道资格不放。
        Teamc engage = acquireEngageTarget(carrier, type);
        if (engage != null) {
            noTargetTimer = 0f;
            moveToEngage(engage, type);
        } else if (carrier.focusPosition(focusPoint)) {
            noTargetTimer = 0f;
            moveToFocus(type);
        } else {
            noTargetTimer += Time.delta;
            orbitAroundCarrier(carrier, type);
        }

    }

    protected void moveToEngage(Teamc engage, CarrierUnitType type) {
        target = engage;
        if (unit.hasWeapons()) {
            float attackDistance = Math.max(24f, unit.range() * Mathf.clamp(type.fighterAttackDistanceFactor, 0.25f, 1f));
            float circleLength = unit.type.circleTargetRadius > eps ? unit.type.circleTargetRadius : attackDistance;
            circleAttack(Math.max(circleLength, 24f));
            unit.lookAt(engage);
            return;
        }

        float attackDistance = Math.max(24f, unit.range() * Mathf.clamp(type.fighterAttackDistanceFactor, 0.25f, 1f));
        moveTo(engage, attackDistance, type.fighterOrbitSmoothing);
        unit.lookAt(engage);
    }

    protected void moveToFocus(CarrierUnitType type) {
        target = null;
        float focusRadius = Math.max(unit.hitSize * 4f, Math.max(type.fighterOrbitRadius * 0.4f, 40f));
        circlePoint(focusPoint, orbitRadius(focusRadius, Math.max(unit.hitSize, 6f), 120f));
        unit.lookAt(focusPoint);
    }

    protected void orbitAroundCarrier(CarrierHostc carrier, CarrierUnitType type) {
        target = null;
        float orbitRadius = Math.max(type.fighterOrbitRadius * 0.8f, 60f) + unit.hitSize;
        orbitRadius = orbitRadius(orbitRadius, Math.max(unit.hitSize * 1.2f, 7f), 150f);
        circlePoint(carrier, orbitRadius);
        unit.lookAt(carrier);
    }

    protected float orbitRadius(float base, float amplitude, float timeScale) {
        float seed = Mathf.randomSeed(unit.id * 61L + runwayIndex * 17L, 0f, 360f);
        float drift = Mathf.sin(Time.time + seed, timeScale, amplitude * 0.5f);
        return Math.max(unit.hitSize * 1.5f, base + drift);
    }

    protected void resetWeaponDebug() {
        debugWeaponHasTarget = false;
        debugWeaponAllowShoot = false;
        debugWeaponAllowFire = false;
        debugWeaponTarget = null;
    }

    protected void clearWeaponTargets() {
        target = null;
        unit.isShooting = false;
        for (WeaponMount mount : unit.mounts) {
            mount.target = null;
            mount.rotate = false;
            mount.shoot = false;
        }
    }

    protected void updateWeaponMount(CarrierHostc carrier, CarrierUnitType type, WeaponMount mount, Teamc engage, float rotation, boolean allowShoot, boolean allowFire) {
        Weapon weapon = mount.weapon;
        if (weapon == null || weapon.noAttack || weapon.bullet == null) return;
        if (!weapon.aiControllable) {
            mount.rotate = false;
            mount.shoot = false;
            return;
        }

        float mountX = unit.x + Angles.trnsx(rotation, weapon.x, weapon.y);
        float mountY = unit.y + Angles.trnsy(rotation, weapon.x, weapon.y);
        Teamc mountTarget = resolveWeaponTarget(carrier, type, weapon, engage, mountX, mountY);
        mount.target = mountTarget;
        if (mountTarget == null) {
            mount.rotate = false;
            mount.shoot = false;
            return;
        }

        Vec2 intercept = Predict.intercept(unit, mountTarget, weapon.bullet);
        if (intercept == null) {
            Tmp.v2.set(mountTarget);
            intercept = Tmp.v2;
        }
        mount.aimX = intercept.x;
        mount.aimY = intercept.y;

        boolean shoot = allowShoot &&
                mountTarget.within(mountX, mountY, weapon.range() + (mountTarget instanceof Sized sized ? sized.hitSize() / 2f : 0f));
        mount.rotate = shoot;
        mount.shoot = allowFire && shoot;
        unit.isShooting |= mount.shoot;

        if (shoot) {
            unit.aimX = mount.aimX;
            unit.aimY = mount.aimY;
        }
    }

    protected @Nullable Teamc resolveWeaponTarget(CarrierHostc carrier, CarrierUnitType type, Weapon weapon, @Nullable Teamc engage, float x, float y) {
        if (weapon == null || weapon.bullet == null) return null;

        float range = Math.max(weapon.range(), 1f);
        boolean air = weapon.bullet.collidesAir;
        boolean groundUnits = weapon.bullet.collidesGround;
        boolean buildings = weapon.bullet.collidesGround || weapon.bullet.collidesTiles;

        if (weaponTargetUsable(engage, range + 80f, air, groundUnits, buildings) && targetWithinCarrierRange(carrier, engage, type)) {
            return engage;
        }

        Teamc local = Units.bestTarget(
                unit.team, x, y, Math.max(range * 1.1f, 80f),
                u -> u.checkTarget(air, groundUnits),
                b -> buildings,
                UnitSorts.closest
        );
        return targetWithinCarrierRange(carrier, local, type) ? local : null;
    }

    protected boolean weaponTargetUsable(@Nullable Teamc target, float range, boolean air, boolean groundUnits, boolean buildings) {
        return target != null &&
                weaponTargetAccepts(target, air, groundUnits, buildings) &&
                !Units.invalidateTarget(target, unit.team, unit.x, unit.y, range);
    }

    protected boolean weaponTargetAccepts(@Nullable Teamc target, boolean air, boolean groundUnits, boolean buildings) {
        if (target == null) return false;
        if (target instanceof Unit u) {
            return u.checkTarget(air, groundUnits);
        }
        if (target instanceof Building) {
            return buildings;
        }
        return buildings;
    }

    protected @Nullable CarrierFighterUnit carrierData() {
        return unit instanceof CarrierFighterUnit data ? data : null;
    }

    protected void pullStateFromUnit() {
        CarrierFighterUnit data = carrierData();
        if (data == null) return;

        carrierId = data.carrierId;
        runwayIndex = Math.max(data.runway, 0);
        returning = data.returning;
        approachingRecovery = data.landing;
        takeoffTimer = Math.max(data.takeoffTimer, 0f);
        takeoffDuration = Math.max(data.takeoffDuration, 1f);
        takeoffSpeedMultiplier = Math.max(data.takeoffSpeedMultiplier, 1f);
        recallGraceTimer = Math.max(data.recallGraceTimer, 0f);
        landingTimer = Math.max(data.landingTimer, 0f);
        landingDuration = Math.max(data.landingDuration, 1f);
        landingAngle = data.landingAngle;
        takeoffFrom.set(data.takeoffFrom);
        takeoffTo.set(data.takeoffTo);
        landingTo.set(data.landingTo);
    }

    protected void pushStateToUnit() {
        CarrierFighterUnit data = carrierData();
        if (data == null) return;

        data.carrierId = carrierId;
        data.runway = runwayIndex;
        data.returning = returning;
        data.landing = approachingRecovery;
        data.takeoffTimer = takeoffTimer;
        data.takeoffDuration = takeoffDuration;
        data.takeoffSpeedMultiplier = takeoffSpeedMultiplier;
        data.recallGraceTimer = recallGraceTimer;
        data.landingTimer = landingTimer;
        data.landingDuration = landingDuration;
        data.landingAngle = landingAngle;
        data.takeoffFrom.set(takeoffFrom);
        data.takeoffTo.set(takeoffTo);
        data.landingTo.set(landingTo);
    }

    protected void resetRecoveryState(boolean clearReturning) {
        if (clearReturning) {
            returning = false;
        }
        approachingRecovery = false;
        landingTimer = 0f;
    }

    protected void fallbackMovement() {
        resetRecoveryState(true);
        super.updateMovement();
    }

    protected boolean switchToDefaultController() {
        if (unit == null || unit.type == null) return false;

        UnitController next = unit.type.createController(unit);
        if (next == null || next instanceof CarrierBoundAIC) {
            next = unit.type.flying ? new FlyingAI() : new GroundAI();
        }

        CarrierFighterUnit data = carrierData();
        if (data != null) {
            data.clearCarrierBinding();
        }
        carrierId = -1;
        unit.controller(next);
        return true;
    }

    protected @Nullable Teamc acquireEngageTarget(CarrierHostc carrier, CarrierUnitType type) {
        boolean targetAir = unit.type == null || unit.type.targetAir;
        boolean targetGround = unit.type != null && unit.type.targetGround;
        float lockRange = lockRangeForEngage(type);
        Teamc lock = carrier.lockedTarget();
        if (!targetUsable(lock, lockRange, targetAir, targetGround)) {
            lock = null;
        }
        if (!targetWithinCarrierRange(carrier, lock, type)) {
            lock = null;
        }

        float localRange = Math.max(Math.max(unit.range() * 2f, type.fighterOrbitRadius * 1.25f), 220f);
        Teamc local = Units.bestTarget(
                unit.team, unit.x, unit.y, localRange,
                u -> u.checkTarget(targetAir, targetGround),
                b -> targetGround,
                UnitSorts.closest
        );
        if (!targetWithinCarrierRange(carrier, local, type)) {
            local = null;
        }

        if (local == null) return lock;
        if (lock == null) return local;
        return unit.dst2(local) + 80f * 80f < unit.dst2(lock) ? local : lock;
    }

    protected float lockRangeForEngage(CarrierUnitType type) {
        return Math.max(unit.range() + 120f, type.maxFighterDistance * 1.7f);
    }

    protected boolean targetModeAccepts(@Nullable Teamc target, boolean targetAir, boolean targetGround) {
        if (target == null) return false;
        if (target instanceof Unit u) {
            return u.checkTarget(targetAir, targetGround);
        }
        if (target instanceof Building) {
            return targetGround;
        }
        return targetGround;
    }

    protected boolean targetUsable(@Nullable Teamc target, float range, boolean targetAir, boolean targetGround) {
        return target != null &&
                targetModeAccepts(target, targetAir, targetGround) &&
                !Units.invalidateTarget(target, unit.team, unit.x, unit.y, range);
    }

    protected @Nullable String recallReasonFromCarrier(CarrierHostc carrier, CarrierUnitType type) {
        if (type == null || carrier == null || unit == null) return "invalid";
        if (type.sortieDuration > 0f && carrier.fighterSortieTime(unit) >= type.sortieDuration) return "sortie";
        if (unit.healthf() <= Mathf.clamp(type.recallHealthf, 0f, 1f)) return "hp";
        return null;
    }

    protected boolean targetWithinCarrierRange(CarrierHostc carrier, @Nullable Teamc target, CarrierUnitType type) {
        if (target == null || carrier == null || type == null) return false;
        float range = Math.max(type.maxFighterDistance * 1.1f, Math.max(unit.range(), type.fighterOrbitRadius));
        return target.within(carrier, range);
    }

    protected boolean invalidCarrierPoint(Vec2 point, @Nullable CarrierHostc carrier) {
        if (point == null || !Float.isFinite(point.x) || !Float.isFinite(point.y)) return true;
        return carrier != null && point.within(0f, 0f, 8f) && !carrier.within(0f, 0f, 80f);
    }

    protected float recoverySpeed(CarrierUnitType type) {
        return Math.max(prefSpeed() * Mathf.clamp(type.recoveryMoveSpeedFactor, 0.6f, 1.6f), prefSpeed() * 0.95f);
    }

    protected float landingApproachRadius(CarrierUnitType type) {
        return Math.max(type.landingApproachRadius, unit.hitSize * 1.6f);
    }

    protected void integrateVelocityFromPosition(float oldX, float oldY, @Nullable Vec2 fallbackDir, float fallbackSpeed) {
        if (Time.delta > 0.001f) {
            unit.vel.set((unit.x - oldX) / Time.delta, (unit.y - oldY) / Time.delta);
        } else if (fallbackDir != null) {
            unit.vel.set(fallbackDir).scl(fallbackSpeed);
        } else {
            unit.vel.setZero();
        }
    }

    protected void moveAtSpeed(Position target, float speed) {
        if (target == null) return;
        recoveryMove.set(target).sub(unit);
        if (recoveryMove.len2() < 0.0001f) return;
        recoveryMove.setLength(speed);
        unit.approach(recoveryMove);
    }

    protected void approachElevation(float target, float amount) {
        if (unit.type.flying) {
            unit.elevation = Mathf.approachDelta(unit.elevation, target, amount);
        }
    }

    protected void sanitizeTakeoffPoints(@Nullable CarrierHostc carrier) {
        if (unit == null) return;

        if (carrier != null && invalidCarrierPoint(takeoffFrom, carrier)) {
            carrier.runwayFrontPoint(runwayIndex, takeoffFrom);
        }
        if (invalidCarrierPoint(takeoffFrom, carrier)) {
            takeoffFrom.set(unit.x, unit.y);
        }

        boolean invalidTo = invalidCarrierPoint(takeoffTo, carrier) || takeoffTo.dst2(takeoffFrom) < 1f;
        if (carrier != null && invalidTo) {
            carrier.launchExitPoint(runwayIndex, takeoffTo);
            invalidTo = invalidCarrierPoint(takeoffTo, carrier) || takeoffTo.dst2(takeoffFrom) < 1f;
        }

        if (invalidTo) {
            Tmp.v2.set(takeoffDir);
            if (Tmp.v2.len2() < 0.0001f) {
                Tmp.v2.trns(unit.rotation, Math.max(unit.hitSize * 2f, 28f));
            } else {
                Tmp.v2.setLength(Math.max(unit.hitSize * 2f, 28f));
            }
            takeoffTo.set(takeoffFrom).add(Tmp.v2);
        }
    }

    protected void refreshTakeoffDirection(@Nullable CarrierHostc carrier) {
        takeoffDir.set(takeoffTo).sub(takeoffFrom);
        if (takeoffDir.len2() < 0.0001f && carrier != null) {
            carrier.runwayFrontPoint(runwayIndex, Tmp.v1);
            carrier.launchExitPoint(runwayIndex, Tmp.v2);
            takeoffDir.set(Tmp.v2).sub(Tmp.v1);
        }
        if (takeoffDir.len2() < 0.0001f) {
            Tmp.v2.trns(unit.rotation, 1f);
            takeoffDir.set(Tmp.v2);
        }
        if (takeoffDir.len2() < 0.0001f) {
            takeoffDir.set(0f, 1f);
        }
        takeoffDir.nor();
    }

    protected void beginLanding(CarrierHostc carrier, CarrierUnitType type, Vec2 point, float runwayAngle) {
        landingDuration = Math.max(type.landingDuration, 8f);
        landingTimer = landingDuration;
        landingAngle = runwayAngle;
        landingTo.set(point);
        approachingRecovery = true;
        unit.vel.scl(0.2f);
    }

    protected void updateLanding(CarrierHostc carrier, CarrierUnitType type) {
        if (landingTimer <= eps) return;

        carrier.recoveryPoint(runwayIndex, Tmp.v4);
        if (!invalidCarrierPoint(Tmp.v4, carrier)) {
            landingTo.set(Tmp.v4);
            carrier.runwayFrontPoint(runwayIndex, Tmp.v2);
            if (!invalidCarrierPoint(Tmp.v2, carrier)) {
                landingAngle = Angles.angle(landingTo.x, landingTo.y, Tmp.v2.x, Tmp.v2.y);
            }
        }

        float duration = Math.max(landingDuration, 1f);
        float linear = Mathf.clamp(1f - landingTimer / duration, 0f, 1f);
        float progress = Interp.sineOut.apply(linear);
        float remaining = Math.max(landingTimer, Time.delta);
        float distance = unit.dst(landingTo);
        float minSpeed = Math.max(prefSpeed() * 0.22f, 0.8f);
        float maxSpeed = Math.max(recoverySpeed(type) * 1.2f, prefSpeed());
        float desiredSpeed = Mathf.clamp(distance / remaining, minSpeed, maxSpeed);
        moveAtSpeed(landingTo, desiredSpeed);

        float turnRate = Math.max(type.recoveryTurnRate, 1f);
        float noseToTouch = unit.angleTo(landingTo);
        float align = Mathf.curve(progress, 0.3f, 1f);
        float blend = Mathf.lerp(20f, 120f, align);
        float targetAngle = Angles.moveToward(noseToTouch, landingAngle, blend);
        unit.rotation = Angles.moveToward(unit.rotation, targetAngle, turnRate * Mathf.lerp(3.6f, 5.6f, align) * Time.delta);

        if (unit.type.flying) {
            float down = Mathf.lerp(0.04f, 0.16f, progress);
            unit.elevation = Mathf.approachDelta(unit.elevation, 0.03f, down);
        }

        float touchdownRadius = recoveryTouchdownRadius(type);
        if (unit.within(landingTo.x, landingTo.y, touchdownRadius)) {
            unit.rotation = landingAngle;
            landingTimer = 0f;
        } else {
            landingTimer = Math.max(landingTimer - Time.delta, 0f);
        }

        // 正式降落阶段由 landingTimer 驱动：持续朝接地点减速、对正跑道并降低高度；
        // 一旦进入接地半径，就把战机钉到回收点并交给航母执行实际回收。
        if (landingTimer <= eps) {
            unit.set(landingTo.x, landingTo.y);
            unit.vel.setZero();
            if (unit.type.flying) {
                unit.elevation = 0.03f;
            }
            carrier.tryRecoverFighter(unit);
        }
    }

    protected void updateReturnOrbit(CarrierHostc carrier, float orbitRadius, float elevation, float elevationRate) {
        target = carrier;
        circlePoint(Tmp.v2, orbitRadius);
        approachElevation(elevation, elevationRate);
    }

    protected void joinReturnOrbit(float orbitRadius, float elevation, float elevationRate) {
        target = carrier();
        approachReturnOrbit(Tmp.v1, orbitRadius);
        approachElevation(elevation, elevationRate);
    }

    protected void circlePoint(Position point, float circleLength) {
        circlePoint(point, circleLength, vec);
        if (vec.isNaN() || vec.isInfinite() || vec.isZero()) return;
        unit.movePref(vec);
    }

    protected void circlePoint(Position point, float circleLength, Vec2 out) {
        if (point == null) return;

        out.set(point).sub(unit);
        float dst = out.len();
        if (dst <= 0.001f) {
            out.trns(unit.rotation, prefSpeed());
            return;
        }

        if (dst < circleLength) {
            out.rotate((circleLength - dst) / Math.max(circleLength, 0.001f) * 180f);
        }

        if (unit.type.omniMovement && unit.vel.len2() > 0.0001f) {
            out.setAngle(Angles.moveToward(unit.vel().angle(), out.angle(), 8f));
        }

        out.setLength(prefSpeed());
    }

    protected void approachReturnOrbit(Position point, float orbitRadius) {
        if (point == null) return;
        circlePoint(point, orbitRadius);
        unit.rotation = Angles.moveToward(unit.rotation, vec.angle(), Math.max(unit.type.rotateSpeed * 2f, 4f) * Time.delta);
    }

   /* protected void approachReturnOrbit(Position point, float orbitRadius) {
        if (point == null) return;
        if (unit.dst(point) > orbitRadius) {
            moveAtSpeed(point, prefSpeed());
            unit.rotation = Angles.moveToward(unit.rotation, unit.angleTo(point), Math.max(unit.type.rotateSpeed * 2f, 4f) * Time.delta);
        } else {
            circlePoint(point, orbitRadius);
        }
    }*/

    protected void enterRecoveryPoint(Position point, float speed, float turnRate, float elevation, float elevationRate) {
        if (point == null) return;

        moveAtSpeed(point, speed);
        unit.rotation = Angles.moveToward(unit.rotation, unit.angleTo(point), turnRate * 3.2f * Time.delta);
        approachElevation(elevation, elevationRate);
    }

    protected float blendedRecoveryAngle(float noseToRecovery, float runwayAngle, float distToRecovery, float finalAlignRadius, float landingStart) {
        float span = Math.max(finalAlignRadius - landingStart, 0.001f);
        float progress = 1f - Mathf.clamp((distToRecovery - landingStart) / span, 0f, 1f);
        progress = Interp.sine.apply(progress);
        return Angles.moveToward(noseToRecovery, runwayAngle, Angles.angleDist(noseToRecovery, runwayAngle) * progress);
    }

    protected boolean holdForRecoveryClaim(CarrierHostc carrier, float orbitRadius) {
        // 尝试申请该跑道的“最终进场资格”。
        // allowRecoveryApproach() 返回 true 表示当前跑道已分配给自己（或本来就由自己占用），
        // 此时无需继续等待；若返回 false，则撤销 approachingRecovery，并回到外圈盘旋排队。
        approachingRecovery = true;
        if (carrier.allowRecoveryApproach(unit)) return false;

        approachingRecovery = false;
        updateReturnOrbit(carrier, orbitRadius, 1f, 0.05f);
        return true;
    }

    protected void updateCircleStyleReturnLoiter(CarrierHostc carrier, float orbitRadius) {
        target = carrier;
        circlePoint(Tmp.v1, orbitRadius);
        approachElevation(0.55f, 0.06f);
    }

    protected float recoveryTouchdownRadius(CarrierUnitType type) {
        if (type == null) return Math.max(unit.hitSize * 0.4f, 3.5f);
        return Math.max(3.5f, Math.max(type.slotSpacing() * 0.18f, unit.hitSize * 0.4f));
    }

    protected void updateRunwayPoints(CarrierHostc carrier, Vec2 hold, Vec2 touch, Vec2 front) {
        carrier.runwayQueueInsertPoint(runwayIndex, hold);
        carrier.recoveryPoint(runwayIndex, touch);
        carrier.runwayFrontPoint(runwayIndex, front);

        if (invalidCarrierPoint(hold, carrier)) hold.set(touch);
        if (invalidCarrierPoint(touch, carrier)) touch.set(hold);
        if (invalidCarrierPoint(touch, carrier)) touch.set(carrier.x(), carrier.y());
        if (invalidCarrierPoint(front, carrier)) front.set(carrier.x(), carrier.y());
    }

    protected void updateReturn(CarrierHostc carrier, CarrierUnitType type) {
        // 返航阶段会围绕 3 个关键点运作：
        // hold: 进场等待点；touch: 真正接地/回收点；front: 跑道前向参考点。
        updateRunwayPoints(carrier, Tmp.v1, Tmp.v2, Tmp.v4);

        // 用回收点 -> 跑道前点确定跑道朝向，后续会让机头逐步对齐这个方向。
        float runwayAngle = Angles.angle(Tmp.v2.x, Tmp.v2.y, Tmp.v4.x, Tmp.v4.y);
        float approach = landingApproachRadius(type);
        float turnRate = Math.max(type.recoveryTurnRate, 1f);
        // landingStart: 从“普通返航移动”切到“最后对正准备降落”的距离阈值。
        float landingStart = Math.max(type.recoverRadius * 0.52f, Math.max(approach * 0.48f, unit.hitSize * 1.05f));
        float speed = recoverySpeed(type);
        // orbitRadius: 没法立即进场时的盘旋半径；会根据机体 id 加一点抖动，避免所有飞机重叠。
        float radiusBase = Math.max(
                Mathf.dst(Tmp.v2.x, Tmp.v2.y, Tmp.v1.x, Tmp.v1.y),
                Math.max(type.recoverRadius * 1.7f, approach * 1.2f)
        );
        float radiusJitter = Mathf.randomSeed(unit.id, -approach * 0.35f, approach * 0.35f);
        float orbitRadius = Math.max(unit.hitSize * 4f, radiusBase + radiusJitter);
        float orbitBand = Math.max(unit.hitSize * 1.5f, approach * 0.45f);

        // 已经进入正式降落动画时，后续逻辑全部交给 updateLanding()。
        if (landingTimer > eps) {
            updateLanding(carrier, type);
            return;
        }

        float distToRecovery = unit.dst(Tmp.v2);
        float noseToRecovery = unit.angleTo(Tmp.v2);
        // orbitEntryAngle: 理想入场切线角；orbitPhaseAngle: 当前飞机相对回收点所在相位角。
        float orbitEntryAngle = Angles.angle(Tmp.v2.x, Tmp.v2.y, Tmp.v1.x, Tmp.v1.y);
        float orbitPhaseAngle = Angles.angle(Tmp.v2.x, Tmp.v2.y, unit.x, unit.y);
        float orbitAngleError = Angles.angleDist(orbitPhaseAngle, orbitEntryAngle);
        // inOrbitBand: 是否已经进入合适的盘旋半径带。
        boolean inOrbitBand = Math.abs(unit.dst(Tmp.v2) - orbitRadius) <= orbitBand;
        // inEntryWindow: 当前相位角是否适合从盘旋切入回收航线。
        boolean inEntryWindow = orbitAngleError <= 58f;
        // nearRecovery: 是否已经接近回收区域，可以尝试从盘旋过渡到进场。
        boolean nearRecovery = distToRecovery <= Math.max(orbitRadius + orbitBand * 1.5f, approach * 1.35f);
        float finalAlignRadius = Math.max(landingStart * 1.4f, unit.hitSize * 2.8f);
        // commitRecovery: 一旦足够靠近，就强制进入最后对正流程，避免来回犹豫。
        boolean commitRecovery = distToRecovery <= finalAlignRadius;

        debugOrbitRadius = orbitRadius;
        debugOrbitBand = orbitBand;
        debugOrbitAngleError = orbitAngleError;
        debugInOrbitBand = inOrbitBand;
        debugInEntryWindow = inEntryWindow;
        debugClaimBlocked = false;

        // approachingRecovery 表示已经拿到回收资格并进入最后对正；
        // commitRecovery 表示距离已经近到必须直接准备接地。
        if (approachingRecovery || commitRecovery) {
            // oneByOneRecovery 开启时，同一跑道同一时刻可能只允许 1 架飞机最后进场。
            if (!approachingRecovery && holdForRecoveryClaim(carrier, orbitRadius)) {
                debugClaimBlocked = true;
                return;
            }

            approachingRecovery = true;
            // targetAngle 会在“朝回收点机头方向”和“跑道方向”之间渐进混合，
            // 这样飞机不会突然瞬间掰正，而是越接近越贴近跑道方向。
            float targetAngle = blendedRecoveryAngle(noseToRecovery, runwayAngle, distToRecovery, finalAlignRadius, landingStart);
            float targetRunwayError = Angles.angleDist(targetAngle, runwayAngle);
            float alignSpan = Math.max(finalAlignRadius - landingStart, 0.001f);
            float alignProgress = 1f - Mathf.clamp((distToRecovery - landingStart) / alignSpan, 0f, 1f);
            alignProgress = Interp.sine.apply(alignProgress);
            moveAtSpeed(Tmp.v2, speed);
            unit.rotation = Angles.moveToward(unit.rotation, targetAngle, turnRate * Mathf.lerp(3.4f, 5.6f, alignProgress) * Time.delta);
            approachElevation(0.24f, 0.08f);

            // 进入足够近的距离，并且朝向已经和跑道基本对齐后，才真正开始降落收尾。
            if (distToRecovery <= landingStart && targetRunwayError <= landingAngleThreshold) {
                beginLanding(carrier, type, Tmp.v2, runwayAngle);
            }
            return;
        }

        // 已经接近回收区且处在合适切入窗口时，直接朝回收点推进。
        if (nearRecovery && inEntryWindow) {
            target = carrier;
            enterRecoveryPoint(Tmp.v2, speed, turnRate, 0.38f, 0.07f);
            return;
        }

        // 否则先并入返航盘旋轨道；进轨后就在外圈持续盘旋等待下一次切入机会。
        if (!inOrbitBand) {
            joinReturnOrbit(orbitRadius, 0.62f, 0.06f);
        } else {
            updateCircleStyleReturnLoiter(carrier, orbitRadius);
        }
    }

    protected CarrierHostc carrier() {
        int hostId = carrierId();
        if (hostId < 0) return null;

        Unit owner = Groups.unit.getByID(hostId);
        if (!(owner instanceof CarrierHostc carrier)) return null;
        return owner.isAdded() && !owner.dead() ? carrier : null;
    }
}
