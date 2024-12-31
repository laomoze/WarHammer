package wh.content;

import mindustry.type.*;

@SuppressWarnings("unused")
public final class WHLiquids {
    public static Liquid orePromethium, refinePromethium, liquidNitrogen;

    private WHLiquids() {}

    public static void load() {
        orePromethium = new Liquid("ore-promethium");
        refinePromethium = new Liquid("refine-promethium");
        liquidNitrogen = new Liquid("liquid-nitrogen");
    }
}
