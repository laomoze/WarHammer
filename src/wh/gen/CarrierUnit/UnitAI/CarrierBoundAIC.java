package wh.gen.CarrierUnit.UnitAI;

import arc.math.geom.Vec2;

/**
 * Common carrier-flight contract used by carrier runtime.
 * Any AI that supports carrier launch/recovery can implement this.
 */
public interface CarrierBoundAIC{
    CarrierBoundAIC setCarrier(int carrierId);

    CarrierBoundAIC setRunway(int runwayIndex);

    CarrierBoundAIC beginTakeoff(Vec2 from, Vec2 to, float duration, float speedMultiplier);

    boolean isLanding();

    boolean isReturning();

    boolean canRecoverNow();
}
