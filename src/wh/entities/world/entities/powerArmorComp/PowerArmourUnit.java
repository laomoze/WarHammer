package wh.entities.world.entities.powerArmorComp;

import arc.math.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.gen.*;

public class PowerArmourUnit extends MechUnit{

    public float bodyMove = 0f;
    public Trail BladeTrail;

    @Override
    public int classId(){
        return EntityRegister.getId(PowerArmourUnit.class);
    }

    @Override
    public void update(){
        super.update();
        WeaponMount firstMount = mounts[0];
        boolean MainMount = firstMount instanceof MainWeaponMount m && !(m.shouldAction && m.warmup > 0.01 && m.recoil > 0.01);
        if(Math.abs(vel().len()) > 0.1f && MainMount){
            bodyMove = Mathf.lerpDelta(bodyMove, 1f, 0.03f);
        }else{
            bodyMove = Mathf.lerpDelta(bodyMove, 0f, 0.05f);
        }
    }
}
