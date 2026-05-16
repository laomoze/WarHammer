package wh.gen;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.units.WeaponMount;
import mindustry.io.TypeIO;
import mindustry.type.UnitType;
import wh.entities.world.entities.TankEn2UnitType;

public class TankEn2Unit extends HoverPayloadUnit{
    public WeaponMount m;
    public WeaponMount[] ms;

    private void ensureCoaxialMount() {
        if (ms != null && ms.length > 0 && ms[0] != null) {
            m = ms[0];
            return;
        }
        if (!(type instanceof TankEn2UnitType type1) || type1.coaxialWeapon == null) return;

        ms = new WeaponMount[1];
        ms[0] = new WeaponMount(type1.coaxialWeapon);
        m = ms[0];

        if (!Vars.headless && m.weapon != null) {
            m.weapon.load();
        }
    }

    @Override
    public int classId(){
        return EntityRegister.getId(TankEn2Unit.class);
    }

    @Override
    public void setType(UnitType type){
        super.setType(type);
        ensureCoaxialMount();
    }

    @Override
    public void update(){
        super.update();
        ensureCoaxialMount();

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
    }


    @Override
    public void afterRead(){
        super.afterRead();
    }

    @Override
    public void read(Reads read){
        super.read(read);
        ensureCoaxialMount();
        if (ms != null) {
            TypeIO.readMounts(read, this.ms);
        }
    }

    @Override
    public void write(Writes write){
        super.write(write);
        ensureCoaxialMount();
        TypeIO.writeMounts(write, this.ms == null ? new WeaponMount[0] : this.ms);
    }
}
