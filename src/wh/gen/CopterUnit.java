package wh.gen;

import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import wh.type.unit.*;
import wh.type.unit.CopterUnitType.*;

import static mindustry.Vars.*;

public class CopterUnit extends UnitEntity implements Copterc {
    protected transient RotorMount[] rotors = {};
    protected transient float rotorSpeedScl = 1f;

    protected CopterUnit() {}

    @Override
    public int classId() {
        return EntityRegister.getId(CopterUnit.class);
    }

    @Override
    public void add() {
        if (added) return;
        index__all = Groups.all.addIndex(this);
        index__unit = Groups.unit.addIndex(this);
        index__sync = Groups.sync.addIndex(this);
        index__draw = Groups.draw.addIndex(this);

        added = true;

        updateLastPosition();

        team.data().updateCount(type, 1);

        //check if over unit cap
        if (type.useUnitCap && count() > cap() && !spawnedByCore && !dead && !state.rules.editor) {
            Call.unitCapDeath(this);
            team.data().updateCount(type, -1);
        }

        if (type instanceof CopterUnitType cType) {
            rotors = new RotorMount[cType.rotors.size];

            for (int i = 0; i < rotors.length; i++) {
                Rotor rotor = cType.rotors.get(i);
                rotors[i] = new RotorMount(rotor);
                rotors[i].rotorRot = rotor.rotOffset;
                rotors[i].rotorShadeRot = rotor.rotOffset;
            }
        }
    }

    @Override
    public void update() {
        super.update();
        if (type instanceof CopterUnitType cType) {
            if (dead || health < 0f) {
                if (!net.client() || isLocal()) rotation += cType.fallRotateSpeed * Mathf.signs[id % 2] * Time.delta;

                rotorSpeedScl = Mathf.lerpDelta(rotorSpeedScl, 0f, cType.rotorDeathSlowdown);
            } else {
                rotorSpeedScl = Mathf.lerpDelta(rotorSpeedScl, 1f, cType.rotorDeathSlowdown);
            }

            for (RotorMount rotor : rotors) {
                rotor.rotorRot += rotor.rotor.speed * rotorSpeedScl * Time.delta;
                rotor.rotorRot %= 360f;

                rotor.rotorShadeRot += rotor.rotor.shadeSpeed * Time.delta;
                rotor.rotorShadeRot %= 360f;
            }
        }
    }

    @Override
    public RotorMount[] rotors() {
        return rotors;
    }

    @Override
    public float rotorSpeedScl() {
        return rotorSpeedScl;
    }

    @Override
    public void rotors(RotorMount[] value) {
        rotors = value;
    }

    @Override
    public void rotorSpeedScl(float value) {
        rotorSpeedScl = value;
    }

    public static CopterUnit create() {
        return new CopterUnit();
    }
}
