package wh.gen;

import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.io.*;
import mindustry.type.*;

public class TankEn2Unit extends HoverPayloadUnit{
    public WeaponMount m;
    public WeaponMount[] ms;


    @Override
    public int classId(){
        return EntityRegister.getId(TankEn2Unit.class);
    }

    @Override
    public void setType(UnitType type){
        super.setType(type);

        if((ms == null || ms.length == 0) && type instanceof TankEn2UnitType type1){
            this.ms = new WeaponMount[1];
            ms[0] = new WeaponMount(type1.coaxialWeapon);
            m = ms[0];
            m.weapon.load();
        }
    }

    @Override
    public void update(){
        super.update();

        if(mounts != null && mounts.length > 0 && m != null && ms != null){
            WeaponMount mainMount = mounts[0];
            m = ms[0];

            m.target = mainMount.target;
            m.rotation = mainMount.rotation;
            m.targetRotation = mainMount.targetRotation;
            m.shoot = mainMount.shoot;
            m.rotate = mainMount.rotate;
            m.aimX = mainMount.aimX;
            m.aimY = mainMount.aimY;

            m.weapon.update(this, m);
        }
    }

    @Override
    public void draw(){
        super.draw();
        if(m != null){
            m.weapon.draw(this, m);
            m.weapon.drawOutline(this, m);
        }
    }


    @Override
    public void afterRead(){
        super.afterRead();
    }

    @Override
    public void read(Reads read){
        super.read(read);
        TypeIO.readMounts(read, this.ms);
    }

    @Override
    public void write(Writes write){
        super.write(write);
        TypeIO.writeMounts(write, this.ms);
    }
}
