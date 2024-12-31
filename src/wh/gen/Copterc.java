package wh.gen;

import mindustry.gen.*;
import wh.type.unit.CopterUnitType.*;

public interface Copterc extends Unitc {
    RotorMount[] rotors();

    float rotorSpeedScl();

    void rotors(RotorMount[] value);

    void rotorSpeedScl(float value);
}
