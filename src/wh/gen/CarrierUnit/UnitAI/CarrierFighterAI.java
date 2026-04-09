package wh.gen.CarrierUnit.UnitAI;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import wh.entities.world.entities.*;
import wh.gen.*;
import wh.gen.CarrierUnit.*;

public class CarrierFighterAI extends FlyingAI implements CarrierBoundAIC{
    private static final float eps = 0.001f;
    private static final float landingAngleThreshold = 14f;
    private static final int intervalMoveRetarget = 0;
    private static final int intervalWeaponRetarget = 1;

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
    protected final Vec2 landingTo = new Vec2();
    protected final Vec2 recoveryMove = new Vec2();
    protected final Vec2 focusPoint = new Vec2();
    protected float takeoffCruiseSpeed = 0f;

    protected float noTargetTimer = 0f;
    protected float debugOrbitRadius = 0f;
    protected float debugOrbitBand = 0f;
    protected float debugOrbitAngleError = 0f;
    protected boolean debugInOrbitBand = false;
    protected boolean debugInEntryWindow = false;
    protected boolean debugClaimBlocked = false;
    protected transient Interval aiIntervals = new Interval(2);
    protected @Nullable Teamc cachedEngageTarget;

    public CarrierFighterAI(){
    }

    public CarrierFighterAI(int carrierId){
        this(carrierId, 0);
    }

    public CarrierFighterAI(int carrierId, int runwayIndex){
        this.carrierId = carrierId;
        this.runwayIndex = Math.max(runwayIndex, 0);
    }

    public CarrierFighterAI setCarrier(int carrierId){
        this.carrierId = carrierId;
        return this;
    }

    public CarrierFighterAI setRunway(int runwayIndex){
        this.runwayIndex = Math.max(runwayIndex, 0);
        return this;
    }

    public CarrierFighterAI beginTakeoff(float duration, float speedMultiplier){
        CarrierHostc carrier = unit == null ? null : carrier();
        if(carrier != null){
            carrier.runwayFrontPoint(runwayIndex, takeoffFrom);
            carrier.launchExitPoint(runwayIndex, takeoffTo);
        }else if(unit != null){
            takeoffFrom.set(unit.x, unit.y);
            Tmp.v1.trns(unit.rotation, Math.max(unit.hitSize * 2f, 28f));
            takeoffTo.set(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y);
        }else{
            takeoffFrom.setZero();
            takeoffTo.setZero();
        }

        return beginTakeoff(takeoffFrom, takeoffTo, duration, speedMultiplier);
    }

    public CarrierFighterAI beginTakeoff(Vec2 from, Vec2 to, float duration, float speedMultiplier){
        CarrierHostc carrier = unit == null ? null : carrier();

        if(from != null){
            takeoffFrom.set(from);
        }else if(carrier != null){
            carrier.runwayFrontPoint(runwayIndex, takeoffFrom);
        }else if(unit != null){
            takeoffFrom.set(unit.x, unit.y);
        }else{
            takeoffFrom.setZero();
        }

        if(to != null){
            takeoffTo.set(to);
        }else if(carrier != null){
            carrier.launchExitPoint(runwayIndex, takeoffTo);
        }else if(unit != null){
            Tmp.v1.trns(unit.rotation, Math.max(unit.hitSize * 2f, 28f));
            takeoffTo.set(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y);
        }else{
            takeoffTo.setZero();
        }

        sanitizeTakeoffPoints(carrier);

        takeoffDuration = Math.max(duration, 1f);
        takeoffTimer = takeoffDuration;
        takeoffSpeedMultiplier = Math.max(speedMultiplier, 1f);
        noTargetTimer = 0f;
        cachedEngageTarget = null;
        resetRecoveryState(true);
        landingDuration = 1f;

        refreshTakeoffDirection(carrier);

        float base = unit == null ? 1f : Math.max(prefSpeed(), Math.max(unit.type == null ? 0f : unit.type.speed, 0.1f));
        takeoffCruiseSpeed = Math.max(base * takeoffSpeedMultiplier, 1f);

        if(unit != null){
            unit.vel.set(takeoffDir).scl(takeoffCruiseSpeed * 0.35f);
            unit.rotation = takeoffDir.angle();
            if(unit.type.flying){
                // Prevent touching the deck in the first takeoff frames (which causes recoil-like shove).
                unit.elevation = Math.max(unit.elevation, 0.22f);
            }
        }
        return this;
    }

    public boolean isTakingOff(){
        return takeoffTimer > 0.001f;
    }

    public boolean isLanding(){
        return approachingRecovery;
    }

    public boolean canRecoverNow(){
        if(unit == null || !returning || landingTimer <= eps) return false;
        // Do not allow server recovery before landing phase reaches touchdown.
        return landingTimer <= Math.max(Time.delta * 1.5f, 1f);
    }

    public boolean isReturning(){
        return returning;
    }

    public int runwayIndex(){
        return runwayIndex;
    }

    public @Nullable CarrierHostc carrierDebug(){
        return carrier();
    }

    public float debugOrbitRadius(){
        return debugOrbitRadius;
    }

    public float debugOrbitBand(){
        return debugOrbitBand;
    }

    public float debugOrbitAngleError(){
        return debugOrbitAngleError;
    }

    public boolean debugInOrbitBand(){
        return debugInOrbitBand;
    }

    public boolean debugInEntryWindow(){
        return debugInEntryWindow;
    }

    public boolean debugClaimBlocked(){
        return debugClaimBlocked;
    }

    public String debugStage(){
        if(takeoffTimer > eps) return "takeoff";
        if(returning){
            if(landingTimer > eps) return "landing";
            if(approachingRecovery) return "final";
            return "return";
        }
        return "combat";
    }

    protected void resetRecoveryState(boolean clearReturning){
        if(clearReturning){
            returning = false;
        }
        approachingRecovery = false;
        landingTimer = 0f;
    }

    protected void fallbackMovement(){
        resetRecoveryState(true);
        super.updateMovement();
    }

    protected boolean switchToDefaultController(){
        if(unit == null || unit.type == null) return false;

        UnitController next = unit.type.createController(unit);
        if(next == null || next == this || next instanceof CarrierBoundAIC){
            next = unit.type.flying ? new FlyingAI() : new GroundAI();
        }
        if(next == this || next == null) return false;

        unit.flag(0d);
        unit.controller(next);
        return true;
    }

    protected Teamc acquireEngageTarget(CarrierHostc carrier, CarrierUnitType type){
        Teamc lock = carrier.lockedTarget();
        if(lock == null) return null;
        float lockRange = Math.max(unit.range() + 120f, type.maxFighterDistance * 1.7f);
        return Units.invalidateTarget(lock, unit.team, unit.x, unit.y, lockRange) ? null : lock;
    }

    protected Teamc resolveWeaponEngageTarget(CarrierHostc carrier, CarrierUnitType type){
        Teamc current = validatedCachedEngageTarget(type);
        if(current != null && !aiIntervals().get(intervalWeaponRetarget, 6f)){
            return current;
        }

        Teamc next = acquireEngageTarget(carrier, type);
        if(next != null || current == null){
            current = next;
        }
        cachedEngageTarget = current;
        return current;
    }

    protected Interval aiIntervals(){
        if(aiIntervals == null){
            aiIntervals = new Interval(2);
        }
        return aiIntervals;
    }

    protected float lockRangeForEngage(CarrierUnitType type){
        return Math.max(unit.range() + 120f, type.maxFighterDistance * 1.7f);
    }

    protected Teamc validatedCachedEngageTarget(CarrierUnitType type){
        Teamc current = cachedEngageTarget;
        if(current == null) return null;
        float lockRange = lockRangeForEngage(type);
        if(Units.invalidateTarget(current, unit.team, unit.x, unit.y, lockRange)){
            cachedEngageTarget = null;
            return null;
        }
        return current;
    }

    protected Teamc resolveMovementEngageTarget(CarrierHostc carrier, CarrierUnitType type){
        Teamc current = validatedCachedEngageTarget(type);
        if(current != null && !aiIntervals().get(intervalMoveRetarget, 8f)){
            return current;
        }

        Teamc next = acquireEngageTarget(carrier, type);
        if(next != null || current == null){
            current = next;
        }
        cachedEngageTarget = current;
        return current;
    }

    protected boolean invalidCarrierPoint(Vec2 point, @Nullable CarrierHostc carrier){
        if(point == null || !Float.isFinite(point.x) || !Float.isFinite(point.y)) return true;
        return carrier != null && point.within(0f, 0f, 8f) && !carrier.within(0f, 0f, 80f);
    }

    protected float recoverySpeed(CarrierUnitType type){
        return Math.max(prefSpeed() * Mathf.clamp(type.recoveryMoveSpeedFactor, 0.6f, 1.6f), prefSpeed() * 0.95f);
    }

    protected float landingApproachRadius(CarrierUnitType type){
        return Math.max(type.landingApproachRadius, unit.hitSize * 1.6f);
    }

    protected void integrateVelocityFromPosition(float oldX, float oldY, @Nullable Vec2 fallbackDir, float fallbackSpeed){
        if(Time.delta > 0.001f){
            unit.vel.set((unit.x - oldX) / Time.delta, (unit.y - oldY) / Time.delta);
        }else if(fallbackDir != null){
            unit.vel.set(fallbackDir).scl(fallbackSpeed);
        }else{
            unit.vel.setZero();
        }
    }

    protected void moveAtSpeed(Position target, float speed){
        if(target == null) return;

        recoveryMove.set(target).sub(unit);
        if(recoveryMove.len2() < 0.0001f) return;

        recoveryMove.setLength(speed);
        unit.approach(recoveryMove);
    }

    protected void approachElevation(float target, float amount){
        if(unit.type.flying){
            unit.elevation = Mathf.approachDelta(unit.elevation, target, amount);
        }
    }

    protected void sanitizeTakeoffPoints(@Nullable CarrierHostc carrier){
        if(unit == null) return;

        if(carrier != null && invalidCarrierPoint(takeoffFrom, carrier)){
            carrier.runwayFrontPoint(runwayIndex, takeoffFrom);
        }
        if(invalidCarrierPoint(takeoffFrom, carrier)){
            takeoffFrom.set(unit.x, unit.y);
        }

        boolean invalidTo = invalidCarrierPoint(takeoffTo, carrier) || takeoffTo.dst2(takeoffFrom) < 1f;
        if(carrier != null && invalidTo){
            carrier.launchExitPoint(runwayIndex, takeoffTo);
            invalidTo = invalidCarrierPoint(takeoffTo, carrier) || takeoffTo.dst2(takeoffFrom) < 1f;
        }

        if(invalidTo){
            Tmp.v2.set(takeoffDir);
            if(Tmp.v2.len2() < 0.0001f){
                Tmp.v2.trns(unit.rotation, Math.max(unit.hitSize * 2f, 28f));
            }else{
                Tmp.v2.setLength(Math.max(unit.hitSize * 2f, 28f));
            }
            takeoffTo.set(takeoffFrom).add(Tmp.v2);
        }

        if(takeoffFrom.isZero(0.01f) && !unit.within(0f, 0f, 6f)){
            takeoffFrom.set(unit.x, unit.y);
            if(takeoffTo.isZero(0.01f)){
                Tmp.v2.trns(unit.rotation, Math.max(unit.hitSize * 2f, 28f));
                takeoffTo.set(takeoffFrom).add(Tmp.v2);
            }
        }
    }

    protected void refreshTakeoffDirection(@Nullable CarrierHostc carrier){
        takeoffDir.set(takeoffTo).sub(takeoffFrom);
        if(takeoffDir.len2() < 0.0001f && carrier != null){
            carrier.runwayFrontPoint(runwayIndex, Tmp.v1);
            carrier.launchExitPoint(runwayIndex, Tmp.v2);
            takeoffDir.set(Tmp.v2).sub(Tmp.v1);
        }
        if(takeoffDir.len2() < 0.0001f){
            if(unit != null){
                Tmp.v2.trns(unit.rotation, 1f);
                takeoffDir.set(Tmp.v2);
            }else{
                takeoffDir.set(0f, 1f);
            }
        }
        if(takeoffDir.len2() < 0.0001f){
            takeoffDir.set(0f, 1f);
        }
        takeoffDir.nor();
    }

    protected void beginLanding(CarrierHostc carrier, CarrierUnitType type, Vec2 point, float runwayAngle){
        landingDuration = Math.max(type.landingDuration, 8f);
        landingTimer = landingDuration;
        landingAngle = runwayAngle;
        landingTo.set(point);
        approachingRecovery = true;
        unit.vel.scl(0.2f);
    }

    protected void updateLanding(CarrierHostc carrier, CarrierUnitType type){
        if(landingTimer <= eps) return;

        // Track moving/rotating carrier touchdown each frame to avoid stale landing points.
        carrier.recoveryPoint(runwayIndex, Tmp.v4);
        if(!invalidCarrierPoint(Tmp.v4, carrier)){
            landingTo.set(Tmp.v4);
            carrier.runwayFrontPoint(runwayIndex, Tmp.v2);
            if(!invalidCarrierPoint(Tmp.v2, carrier)){
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

        if(unit.type.flying){
            float down = Mathf.lerp(0.04f, 0.16f, progress);
            unit.elevation = Mathf.approachDelta(unit.elevation, 0.03f, down);
        }

        float touchdownRadius = recoveryTouchdownRadius(type);
        if(unit.within(landingTo.x, landingTo.y, touchdownRadius)){
            unit.rotation = landingAngle;
            landingTimer = 0f;
        }else{
            landingTimer = Math.max(landingTimer - Time.delta, 0f);
        }
        if(landingTimer <= eps){
            unit.set(landingTo.x, landingTo.y);
            unit.vel.setZero();
            if(unit.type.flying){
                unit.elevation = 0.03f;
            }

            if(carrier.tryRecoverFighter(unit)){
                return;
            }
        }
    }

    protected void updateReturnOrbit(CarrierHostc carrier, float orbitRadius, float elevation, float elevationRate){
        target = carrier;
        circlePoint(Tmp.v2, orbitRadius);
        approachElevation(elevation, elevationRate);
    }

    protected void joinReturnOrbit(float orbitRadius, float elevation, float elevationRate){
        target = carrier();
        approachReturnOrbit(Tmp.v2, orbitRadius);
        approachElevation(elevation, elevationRate);
    }

    protected void circlePoint(Position point, float circleLength){
        circlePoint(point, circleLength, vec);
        if(vec.isNaN() || vec.isInfinite() || vec.isZero()) return;
        unit.movePref(vec);
    }

    protected void circlePoint(Position point, float circleLength, Vec2 out){
        if(point == null) return;

        out.set(point).sub(unit);
        float dst = out.len();

        if(dst <= 0.001f){
            out.trns(unit.rotation, prefSpeed());
            return;
        }

        if(dst < circleLength){
            out.rotate((circleLength - dst) / Math.max(circleLength, 0.001f) * 180f);
        }

        if(unit.type.omniMovement && unit.vel.len2() > 0.0001f){
            out.setAngle(Angles.moveToward(unit.vel().angle(), out.angle(), 8f));
        }

        out.setLength(prefSpeed());
    }

    protected void approachReturnOrbit(Position point, float orbitRadius){
        if(point == null) return;
        float joinThreshold = orbitRadius;
        if(unit.dst(point) > joinThreshold){
            moveAtSpeed(point, prefSpeed());
            unit.rotation = Angles.moveToward(unit.rotation, unit.angleTo(point), Math.max(unit.type.rotateSpeed * 2f, 4f) * Time.delta);
        }else{
            circlePoint(point, orbitRadius);
        }
    }

    protected void enterRecoveryPoint(Position point, float speed, float turnRate, float elevation, float elevationRate){
        if(point == null) return;

        moveAtSpeed(point, speed);
        unit.rotation = Angles.moveToward(unit.rotation, unit.angleTo(point), turnRate * 3.2f * Time.delta);
        approachElevation(elevation, elevationRate);
    }

    protected float blendedRecoveryAngle(float noseToRecovery, float runwayAngle, float distToRecovery, float finalAlignRadius, float landingStart){
        float span = Math.max(finalAlignRadius - landingStart, 0.001f);
        float progress = 1f - Mathf.clamp((distToRecovery - landingStart) / span, 0f, 1f);
        progress = Interp.sine.apply(progress);
        return Angles.moveToward(noseToRecovery, runwayAngle, Angles.angleDist(noseToRecovery, runwayAngle) * progress);
    }

    protected boolean holdForRecoveryClaim(CarrierHostc carrier, float orbitRadius){
        // 申请跑道回收 claim。没拿到时保持外圈等待，避免多机抢同一跑道。
        approachingRecovery = true;
        if(carrier.allowRecoveryApproach(unit)) return false;

        approachingRecovery = false;
        updateReturnOrbit(carrier, orbitRadius, 1f, 0.05f);
        return true;
    }

    protected void updateCircleStyleReturnLoiter(CarrierHostc carrier, float orbitRadius){
        updateReturnOrbit(carrier, orbitRadius, 0.55f, 0.06f);
    }

    protected float recoveryTouchdownRadius(CarrierUnitType type){
        if(type == null) return Math.max(unit.hitSize * 0.4f, 3.5f);
        return Math.max(3.5f, Math.max(type.slotSpacing() * 0.18f, unit.hitSize * 0.4f));
    }

    protected void updateRunwayPoints(CarrierHostc carrier, Vec2 hold, Vec2 touch, Vec2 front){
        carrier.runwayQueueInsertPoint(runwayIndex, hold);
        carrier.recoveryPoint(runwayIndex, touch);
        carrier.runwayFrontPoint(runwayIndex, front);

        if(invalidCarrierPoint(hold, carrier)){
            hold.set(touch);
        }
        if(invalidCarrierPoint(touch, carrier)){
            touch.set(hold);
        }
        if(invalidCarrierPoint(touch, carrier)){
            touch.set(carrier.x(), carrier.y());
        }
        if(invalidCarrierPoint(front, carrier)){
            front.set(carrier.x(), carrier.y());
        }
    }

    protected void updateReturn(CarrierHostc carrier, CarrierUnitType type){
        // 返航主流程：
        // 1) 外圈等待 -> 2) 获得 claim 后对正跑道 -> 3) 落地触发 carrier.tryRecoverFighter()
        updateRunwayPoints(carrier, Tmp.v1, Tmp.v2, Tmp.v4);

        float runwayAngle = Angles.angle(Tmp.v2.x, Tmp.v2.y, Tmp.v4.x, Tmp.v4.y);
        float approach = landingApproachRadius(type);
        float turnRate = Math.max(type.recoveryTurnRate, 1f);
        float landingStart = Math.max(type.recoverRadius * 0.52f, Math.max(approach * 0.48f, unit.hitSize * 1.05f));
        float speed = recoverySpeed(type);
        float radiusBase = Math.max(
        Mathf.dst(Tmp.v2.x, Tmp.v2.y, Tmp.v1.x, Tmp.v1.y),
        Math.max(type.recoverRadius * 1.7f, approach * 1.2f)
        );
        float radiusJitter = Mathf.randomSeed(unit.id, -approach * 0.35f, approach * 0.35f);
        float orbitRadius = Math.max(unit.hitSize * 4f, radiusBase + radiusJitter);
        float orbitBand = Math.max(unit.hitSize * 1.5f, approach * 0.45f);

        if(landingTimer > eps){
            updateLanding(carrier, type);
            return;
        }

        float distToRecovery = unit.dst(Tmp.v2);
        float noseToRecovery = unit.angleTo(Tmp.v2);
        float orbitEntryAngle = Angles.angle(Tmp.v2.x, Tmp.v2.y, Tmp.v1.x, Tmp.v1.y);
        float orbitPhaseAngle = Angles.angle(Tmp.v2.x, Tmp.v2.y, unit.x, unit.y);
        float orbitAngleError = Angles.angleDist(orbitPhaseAngle, orbitEntryAngle);
        boolean inOrbitBand = Math.abs(unit.dst(Tmp.v2) - orbitRadius) <= orbitBand;
        boolean inEntryWindow = orbitAngleError <= 58f;
        boolean nearRecovery = distToRecovery <= Math.max(orbitRadius + orbitBand * 1.5f, approach * 1.35f);
        float finalAlignRadius = Math.max(landingStart * 1.4f, unit.hitSize * 2.8f);
        boolean commitRecovery = distToRecovery <= finalAlignRadius;

        debugOrbitRadius = orbitRadius;
        debugOrbitBand = orbitBand;
        debugOrbitAngleError = orbitAngleError;
        debugInOrbitBand = inOrbitBand;
        debugInEntryWindow = inEntryWindow;
        debugClaimBlocked = false;

        if(approachingRecovery || commitRecovery){
            if(!approachingRecovery && holdForRecoveryClaim(carrier, orbitRadius)){
                debugClaimBlocked = true;
                return;
            }

            approachingRecovery = true;
            float targetAngle = blendedRecoveryAngle(noseToRecovery, runwayAngle, distToRecovery, finalAlignRadius, landingStart);
            float targetRunwayError = Angles.angleDist(targetAngle, runwayAngle);
            float alignSpan = Math.max(finalAlignRadius - landingStart, 0.001f);
            float alignProgress = 1f - Mathf.clamp((distToRecovery - landingStart) / alignSpan, 0f, 1f);
            alignProgress = Interp.sine.apply(alignProgress);
            moveAtSpeed(Tmp.v2, speed);
            unit.rotation = Angles.moveToward(unit.rotation, targetAngle, turnRate * Mathf.lerp(3.4f, 5.6f, alignProgress) * Time.delta);
            approachElevation(0.24f, 0.08f);

            if(distToRecovery <= landingStart && targetRunwayError <= landingAngleThreshold){
                beginLanding(carrier, type, Tmp.v2, runwayAngle);
            }
            return;
        }

        // Do not start runway alignment as soon as the fighter crosses the entry line.
        // First fly straight into T, then only at close range switch to final.
        // 中文说明：先飞到回收触点附近，再切换最终进场，减少提前转向导致的抖动/切角。
        if(nearRecovery && inEntryWindow){
            target = carrier;
            enterRecoveryPoint(Tmp.v2, speed, turnRate, 0.38f, 0.07f);
            return;
        }

        if(!inOrbitBand){
            joinReturnOrbit(orbitRadius, 0.62f, 0.06f);
        }else{
            updateCircleStyleReturnLoiter(carrier, orbitRadius);
        }
    }

    @Override
    public void updateMovement(){
        CarrierHostc carrier = carrier();
        CarrierUnitType type = carrier == null ? null : carrier.carrierType();
        if(carrier == null || type == null){
            // Carrier missing or invalid: restore this fighter to its type default controller.
            if(switchToDefaultController()) return;
            fallbackMovement();
            return;
        }

        runwayIndex = carrier.clampRunway(runwayIndex);

        if(unit.team != carrier.team()){
            unit.team = carrier.team();
        }

        Tmp.v4.set(unit.x, unit.y);
        if(invalidCarrierPoint(Tmp.v4, carrier)){
            carrier.runwayFrontPoint(runwayIndex, Tmp.v4);
            if(invalidCarrierPoint(Tmp.v4, carrier)){
                Tmp.v4.set(carrier.x(), carrier.y());
            }
            unit.set(Tmp.v4.x, Tmp.v4.y);
            unit.vel.setZero();
        }

        if(takeoffTimer > 0f){
            sanitizeTakeoffPoints(carrier);
            refreshTakeoffDirection(carrier);

            float linear = Mathf.clamp(1f - takeoffTimer / takeoffDuration, 0f, 1f);
            float progress = Interp.pow2Out.apply(linear);
            float baseSpeed = Math.max(takeoffCruiseSpeed, Math.max(prefSpeed(), 0.1f));
            float speed = baseSpeed * Mathf.lerp(0.35f, 1f, progress);
            float ox = unit.x, oy = unit.y;
            unit.set(
            unit.x + takeoffDir.x * speed * Time.delta,
            unit.y + takeoffDir.y * speed * Time.delta
            );
            integrateVelocityFromPosition(ox, oy, takeoffDir, speed);
            unit.lookAt(unit.x + takeoffDir.x * 12f, unit.y + takeoffDir.y * 12f);

            if(unit.type.flying){
                float liftProgress = Mathf.curve(linear, takeoffLiftStart, 1f);
                float elev = Mathf.clamp(Interp.sineOut.apply(liftProgress), 0f, 1f);
                // Keep a small floor so the fighter doesn't scrape the carrier during takeoff.
                unit.elevation = Math.max(elev, 0.16f);
            }

            takeoffTimer = Math.max(0f, takeoffTimer - Time.delta);
            if(takeoffTimer <= 0f){
                takeoffTimer = 0f;
                if(unit.type.flying){
                    unit.elevation = 1f;
                }
                unit.vel.set(takeoffDir).scl(Math.max(baseSpeed, 0.1f));
            }
            return;
        }

        if(!returning){
            returning = carrier.shouldRecallFighter(unit);
        }
        if(returning){
            cachedEngageTarget = null;
            updateReturn(carrier, type);
            return;
        }

        resetRecoveryState(false);
        carrier.releaseRecoveryClaim(unit);

        Teamc pursue = resolveMovementEngageTarget(carrier, type);
        boolean hasCombatTarget = pursue != null;
        boolean hasFocusPoint = false;
        if(pursue != null){
            noTargetTimer = 0f;
            target = pursue;
            if(unit.hasWeapons() && unit.type.circleTarget){
                circleAttack(Math.max(unit.type.circleTargetRadius, 24f));
            }else{
                float attackDistance = Math.max(24f, unit.range() * Mathf.clamp(type.fighterAttackDistanceFactor, 0.25f, 1f));
                moveTo(pursue, attackDistance, type.fighterOrbitSmoothing);
                unit.lookAt(pursue);
            }
        }else if(carrier.focusPosition(focusPoint)){
            hasFocusPoint = true;
            target = null;
            float focusRadius = Math.max(unit.hitSize * 4f, Math.max(type.fighterOrbitRadius * 0.4f, 40f));
            float focusAngle = (Time.time * 0.8f + unit.id * 23f) % 360f;
            Tmp.v1.trns(focusAngle, focusRadius).add(focusPoint);
            moveTo(Tmp.v1, Math.max(unit.hitSize * 0.5f, 8f), type.fighterOrbitSmoothing, false, null, true);
            unit.lookAt(focusPoint);
        }else{
            target = null;
            float orbitRadius = Math.max(type.fighterOrbitRadius * 0.8f, 60f) + (unit.id % 3) * (unit.hitSize * 0.9f);
            float orbitSpeed = 0.6f;
            float orbitAngle = (Time.time * orbitSpeed + (unit.id * 23f)) % 360f;
            Tmp.v1.trns(orbitAngle, orbitRadius).add(carrier.x(), carrier.y());
            moveTo(Tmp.v1, Math.max(unit.hitSize * 0.5f, 8f), type.fighterOrbitSmoothing, false, null, true);
            unit.lookAt(Tmp.v1);
        }

        if(hasCombatTarget){
            noTargetTimer = 0f;
        }else{
            // Keep return timer running when there is no real combat target.
            // Nearby "focus" points should not block idle recall forever.
            float farFocus = Math.max(type.fighterOrbitRadius * 1.25f, 120f);
            if(!hasFocusPoint || !focusPoint.within(carrier.x(), carrier.y(), farFocus)){
                noTargetTimer += Time.delta;
            }else{
                noTargetTimer = 0f;
            }
        }

        if(type.idleReturnDelay > 0f && noTargetTimer >= type.idleReturnDelay){
            returning = true;
        }
        if(unit.dst(carrier) > type.maxFighterDistance * 1.35f){
            returning = true;
        }
    }

    @Override
    public void afterRead(Unit unit){
        super.afterRead(unit);
        if(carrierId < 0){
            carrierId = CarrierPayloadUnit.decodeCarrierFlag(unit.flag());
        }
        runwayIndex = Math.max(CarrierPayloadUnit.decodeRunwayFlag(unit.flag()), 0);
    }

    protected CarrierHostc carrier(){
        if(carrierId < 0){
            carrierId = CarrierPayloadUnit.decodeCarrierFlag(unit.flag());
        }
        if(carrierId < 0) return null;

        Unit owner = Groups.unit.getByID(carrierId);
        if(!(owner instanceof CarrierHostc carrier)) return null;
        return owner.isAdded() && !owner.dead() ? carrier : null;
    }


    @Override
    public void updateWeapons(){
        CarrierHostc carrier = carrier();
        CarrierUnitType type = carrier == null ? null : carrier.carrierType();
        if(carrier == null || type == null){
            if(switchToDefaultController()) return;
            super.updateWeapons();
            return;
        }

        if(takeoffTimer > 0f || returning){
            stopShooting();
            return;
        }

        Teamc engage = resolveWeaponEngageTarget(carrier, type);
        if(engage == null){
            target = null;
            unit.isShooting = false;
            for(WeaponMount mount : unit.mounts){
                mount.target = null;
                mount.rotate = false;
                mount.shoot = false;
            }
            return;
        }

        target = engage;
        unit.isShooting = false;
        float rotation = unit.rotation - 90f;
        boolean allowShoot = shouldShoot();
        boolean allowFire = shouldFire();

        for(WeaponMount mount : unit.mounts){
            Weapon weapon = mount.weapon;
            if(!weapon.controllable || weapon.noAttack){
                continue;
            }
            if(!weapon.aiControllable){
                mount.rotate = false;
                mount.shoot = false;
                continue;
            }

            float mountX = unit.x + Angles.trnsx(rotation, weapon.x, weapon.y);
            float mountY = unit.y + Angles.trnsy(rotation, weapon.x, weapon.y);

            mount.target = engage;

            Vec2 to = Predict.intercept(unit, engage, weapon.bullet);
            if(to == null){
                Tmp.v2.set(engage);
                to = Tmp.v2;
            }

            mount.aimX = to.x;
            mount.aimY = to.y;

            boolean shoot = allowShoot &&
            engage.within(mountX, mountY, weapon.range() + (engage instanceof Sized s ? s.hitSize() / 2f : 0f));
            mount.shoot = mount.rotate = shoot;
            if(!allowFire){
                mount.shoot = false;
            }

            unit.isShooting |= mount.shoot;
            if(shoot){
                unit.aimX = mount.aimX;
                unit.aimY = mount.aimY;
            }
        }
    }
}
