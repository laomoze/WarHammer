package wh.gen;

import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.type.unit.*;

public interface Energyc extends Unitc {
    boolean teleportValid(EnergyUnitType eType);

    void teleport(float x, float y);

    Vec2 lastPos();

    float reloadValue();

    float lastHealth();

    Interval timer();

    Trail[] trails();

    void lastPos(Vec2 value);

    void reloadValue(float value);

    void lastHealth(float value);

    void timer(Interval value);

    void trails(Trail[] value);
}
