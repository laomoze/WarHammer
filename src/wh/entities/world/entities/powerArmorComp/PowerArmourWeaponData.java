package wh.entities.world.entities.powerArmorComp;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.type.*;

public final class PowerArmourWeaponData{
    private static final ObjectMap<Weapon, PowerArmourWeaponData> map = new ObjectMap<>();

    public final Seq<DrawUnitPart> unitParts = new Seq<>(DrawUnitPart.class);
    public boolean melee;
    public float actionTime;
    public Interp actionInInterp = Interp.linear;
    public Interp actionOutInterp = Interp.linear;
    public float smoothHeatSpeed = 0.2f;

    private PowerArmourWeaponData(){
    }

    public static PowerArmourWeaponData of(Weapon weapon){
        PowerArmourWeaponData data = map.get(weapon);
        if(data == null){
            data = new PowerArmourWeaponData();
            map.put(weapon, data);
        }
        return data;
    }

    public static @Nullable PowerArmourWeaponData get(Weapon weapon){
        return map.get(weapon);
    }
}
