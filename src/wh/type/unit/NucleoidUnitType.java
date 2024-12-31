package wh.type.unit;

import arc.util.*;

public class NucleoidUnitType extends AncientUnitType {
    public float maxDamagedPerSec = 30000;
    public float recentDamageResume = maxDamagedPerSec / 60f;

    public float maxOnceDamage = 3000;
    public float reinforcementsSpacing = Time.toMinutes * 2;

    public float mass = 8000000f;

    public NucleoidUnitType(String name) {
        super(name);
    }
}
