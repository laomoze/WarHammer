package wh.entities.world.entities.powerArmorComp;

import mindustry.entities.units.*;
import mindustry.type.*;

public class MainWeaponMount extends WeaponMount{
    public MainWeaponMount(Weapon weapon){
        super(weapon);
    }

    public float smoothHeat;
    public boolean shouldAction = false;
    public boolean reset = false;
    public float actionProgress;
    public float actionProgress2;
    public float actionInterpProgress;

    public boolean switchWeapon = false;
    public float switchProgress;


}
