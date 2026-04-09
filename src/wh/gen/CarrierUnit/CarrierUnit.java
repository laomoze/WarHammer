package wh.gen.CarrierUnit;

import arc.math.*;
import arc.math.geom.*;
import mindustry.gen.*;
import wh.entities.world.entities.*;

/**
 * Shared carrier unit foundation: type/runway sizing, carrier flag codec and generic point validity checks.
 */
public class CarrierUnit extends PayloadUnit{
    protected static final double fighterCarrierFlagBase = -2_000_000_000d;
    protected static final int flagRunwayBits = 16;
    protected static final long flagRunwayMask = (1L << flagRunwayBits) - 1L;

    public CarrierUnitType carrierType(){
        return type instanceof CarrierUnitType ? (CarrierUnitType)type : null;
    }

    public int deckSlotCount(){
        CarrierUnitType ctype = carrierType();
        return ctype == null ? 0 : Math.max(ctype.deckCapacity(), 1);
    }

    public int runwayCount(){
        CarrierUnitType ctype = carrierType();
        if(ctype == null || !ctype.runways.any()) return 1;
        return Math.max(ctype.runways.size, 1);
    }

    public int clampRunway(int runway){
        return Mathf.clamp(runway, 0, runwayCount() - 1);
    }

    public int runwayCapacity(int runway){
        CarrierUnitType ctype = carrierType();
        if(ctype == null || !ctype.runways.any()) return deckSlotCount();
        return Math.max(ctype.runways.get(clampRunway(runway)).capacity, 1);
    }

    public int runwayFirstSlot(int runway){
        CarrierUnitType ctype = carrierType();
        if(ctype == null || !ctype.runways.any()) return 0;

        int r = clampRunway(runway);
        int index = 0;
        for(int i = 0; i < r; i++){
            index += runwayCapacity(i);
        }
        return index;
    }

    public int runwayLastSlot(int runway){
        int r = clampRunway(runway);
        return runwayFirstSlot(r) + Math.max(runwayCapacity(r) - 1, 0);
    }

    public static double encodeCarrierFlag(int carrierId, int runway){
        long packed = ((long)Math.max(carrierId, 0) << flagRunwayBits) | (runway & flagRunwayMask);
        return fighterCarrierFlagBase - packed;
    }

    public static double encodeCarrierFlag(int carrierId){
        return encodeCarrierFlag(carrierId, 0);
    }

    protected static long decodePackedFlag(double flag){
        double raw = fighterCarrierFlagBase - flag;
        long packed = Math.round(raw);
        if(Math.abs(raw - packed) > 0.001) return -1L;
        return packed;
    }

    public static int decodeCarrierFlag(double flag){
        long packed = decodePackedFlag(flag);
        if(packed < 0) return -1;

        // Backward compatibility: old format stored only carrier ID.
        if(packed < (1L << flagRunwayBits)){
            return (int)packed;
        }
        return (int)(packed >>> flagRunwayBits);
    }

    public static int decodeRunwayFlag(double flag){
        long packed = decodePackedFlag(flag);
        if(packed < 0) return -1;

        // Backward compatibility: old format had no runway.
        if(packed < (1L << flagRunwayBits)){
            return 0;
        }
        return (int)(packed & flagRunwayMask);
    }

    public int fighterRunway(Unit fighter){
        if(fighter == null) return 0;
        return clampRunway(decodeRunwayFlag(fighter.flag()));
    }

    protected boolean validLaunchPoint(Vec2 point){
        if(point == null) return false;
        return Float.isFinite(point.x) && Float.isFinite(point.y);
    }

    protected boolean invalidLaunchPoint(Vec2 point){
        return !validLaunchPoint(point) || suspiciousOriginPoint(point);
    }

    protected boolean suspiciousOriginPoint(Vec2 point){
        if(point == null) return true;
        // If the carrier is not near world origin, launch points near origin are almost certainly invalid.
        return point.within(0f, 0f, 8f) && !within(0f, 0f, 80f);
    }
}

