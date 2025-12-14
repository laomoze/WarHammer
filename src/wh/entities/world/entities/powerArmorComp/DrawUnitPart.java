package wh.entities.world.entities.powerArmorComp;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;

public abstract class DrawUnitPart{
    public static final UnitPartParams params = new UnitPartParams();

    /** If true, the layer is overridden to be under the weapon/turret itself. */
    public boolean under = false;
    /** For units, this is the index of the weapon this part gets its progress for. */
    public int weaponIndex = 0;
    /** Which recoil counter to use. < 0 to use base recoil. */
    public int recoilIndex = -1;

    public boolean turretShading;

    public abstract void draw(UnitPartParams params);

    public void load(String name){
    }

    public void getOutlines(Seq<TextureRegion> out){
    }

    /** Parameters for drawing a part in draw(). */
    public static class UnitPartParams{
        public PowerArmourUnit unit;
        public PowerArmourUnitType type;
        public float warmup, reload, smoothReload, heat, recoil, life, charge, actionTime;
        public float smoothHeat;
        public float x, y, rotation;
        public int sideOverride = -1, sideMultiplier = 1;

        public UnitPartParams set(PowerArmourUnit unit, PowerArmourUnitType type, float warmup, float reload, float smoothReload, float smoothHeat,
                                  float heat, float recoil, float charge, float actionTime, float x, float y, float rotation){

            this.unit = unit;
            this.type = type;

            this.warmup = warmup;
            this.reload = reload;
            this.heat = heat;
            this.recoil = recoil;
            this.smoothReload = smoothReload;
            this.smoothHeat = smoothHeat;
            this.charge = charge;
            this.actionTime = actionTime;
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.sideOverride = -1;
            this.life = 0f;
            this.sideMultiplier = 1;
            return this;
        }

        public UnitPartParams setRecoil(float recoils){
            this.recoil = recoils;
            return this;
        }
    }

    public static class UnitPartMove{
        public UnitPartProgress progress = UnitPartProgress.warmup;
        public float x, y, gx, gy, rot;

        public PowerArmourUnit unit;
        public PowerArmourUnitType type;

        public UnitPartMove(PowerArmourUnit unit, PowerArmourUnitType type, UnitPartProgress progress, float x, float y, float gx, float gy, float rot){
            this.progress = progress;
            this.x = x;
            this.y = y;
            this.gx = gx;
            this.gy = gy;
            this.rot = rot;
            this.unit = unit;
            this.type = type;
        }

        public UnitPartMove(UnitPartProgress progress, float x, float y, float gx, float gy, float rot){
            this.progress = progress;
            this.x = x;
            this.y = y;
            this.gx = gx;
            this.gy = gy;
            this.rot = rot;
        }

        public UnitPartMove(UnitPartProgress progress, float x, float y, float rot){
            this(progress, x, y, 0, 0, rot);
        }

        public UnitPartMove(){
        }
    }

    public interface UnitPartProgress{
        /** Reload of the weapon - 1 right after shooting, 0 when ready to fire */
        UnitPartProgress
        reload = p -> p.reload,
        /** Reload, but smoothed out, so there is no sudden jump between 0-1. */
        smoothReload = p -> p.smoothReload,
        /** Weapon warmup, 0 when not firing, 1 when actively shooting. Not equivalent to heat. */
        warmup = p -> p.warmup,
        /** Weapon charge, 0 when beginning to charge, 1 when finished */
        charge = p -> p.charge,
        /** Weapon recoil with no curve applied. */
        recoil = p -> p.recoil,
        /** Weapon heat, 1 when just fired, 0, when it has cooled down (duration depends on weapon) */
        heat = p -> p.heat,
        /** Lifetime fraction, 0 to 1. Only for missiles. */
        life = p -> p.life,
        /** Current unscaled value of Time.time. */
        time = p -> Time.time,

        one = p -> 1f,

        smoothHeat = p -> p.smoothHeat,

        actionTime = p -> p.actionTime;

        float get(UnitPartParams p);

        static UnitPartProgress constant(float value){
            return p -> value;
        }

        default float getClamp(UnitPartParams p){
            return getClamp(p, true);
        }

        default float getClamp(UnitPartParams p, boolean clamp){
            return clamp ? Mathf.clamp(get(p)) : get(p);
        }

        static UnitPartProgress moveSin(float moveScl){
            return p -> Mathf.sin(moveScl * Mathf.pi, 1 * p.unit.bodyMove);
        }

        default UnitPartProgress inv(){
            return p -> 1f - get(p);
        }

        default UnitPartProgress slope(){
            return p -> Mathf.slope(get(p));
        }

        default UnitPartProgress clamp(){
            return p -> Mathf.clamp(get(p));
        }

        default UnitPartProgress add(float amount){
            return p -> get(p) + amount;
        }

        default UnitPartProgress add(UnitPartProgress other){
            return p -> get(p) + other.get(p);
        }

        default UnitPartProgress delay(float amount){
            return p -> (get(p) - amount) / (1f - amount);
        }

        default UnitPartProgress curve(float offset, float duration){
            return p -> (get(p) - offset) / duration;
        }

        default UnitPartProgress sustain(float offset, float grow, float sustain){
            return p -> {
                float val = get(p) - offset;
                return Math.min(Math.max(val, 0f) / grow, (grow + sustain + grow - val) / grow);
            };
        }

        default UnitPartProgress shorten(float amount){
            return p -> get(p) / (1f - amount);
        }

        default UnitPartProgress compress(float start, float end){
            return p -> Mathf.curve(get(p), start, end);
        }

        default UnitPartProgress blend(UnitPartProgress other, float amount){
            return p -> Mathf.lerp(get(p), other.get(p), amount);
        }

        default UnitPartProgress mul(UnitPartProgress other){
            return p -> get(p) * other.get(p);
        }

        default UnitPartProgress mul(float amount){
            return p -> get(p) * amount;
        }

        default UnitPartProgress min(UnitPartProgress other){
            return p -> Math.min(get(p), other.get(p));
        }

        default UnitPartProgress sin(float offset, float scl, float mag){
            return p -> get(p) + Mathf.sin(Time.time + offset, scl, mag);
        }

        default UnitPartProgress sin(float scl, float mag){
            return p -> get(p) + Mathf.sin(scl, mag);
        }

        default UnitPartProgress absin(float scl, float mag){
            return p -> get(p) + Mathf.absin(scl, mag);
        }

        default UnitPartProgress mod(float amount){
            return p -> Mathf.mod(get(p), amount);
        }

        default UnitPartProgress loop(float time){
            return p -> Mathf.mod(get(p) / time, 1);
        }

        default UnitPartProgress apply(UnitPartProgress other, mindustry.entities.part.DrawPart.PartFunc func){
            return p -> func.get(get(p), other.get(p));
        }

        default UnitPartProgress curve(Interp interp){
            return p -> interp.apply(get(p));
        }
    }

    public interface PartFunc{
        float get(float a, float b);
    }
}
