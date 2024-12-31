package wh.content;

import mindustry.type.*;
import wh.gen.*;
import wh.type.unit.*;

@SuppressWarnings("unused")
public final class WHUnitTypes {
    public static UnitType cMoon;

    private WHUnitTypes() {}

    public static void load() {
        cMoon = new NucleoidUnitType("c-moon") {{
            constructor = NucleoidUnit::create;
            addEngine(-57, -173, 0, 15, true);
            addEngine(-32, -164, 0, 15, true);
            addEngine(-8, -151, 0, 5, true);
            addEngine(-4, -151, 0, 5, true);
            addEngine(-1, -151, 0, 5, true);
        }};
    }
}
