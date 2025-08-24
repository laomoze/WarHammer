//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.content;

import arc.graphics.Color;
import arc.struct.*;
import mindustry.type.Item;
import wh.graphics.WHPal;

public final class WHItems {
    public static Item titaniumSteel;
    public static Item imperium;
    public static Item vibranium;
    public static Item ceramite;
    public static Item refineCeramite;
    public static Item adamantium;
    public static Item sealedPromethium;
    public static Item molybdenum;
    public static Item molybdenumAlloy;
    public static Item oreSand;
    public static Item resonantCrystal;
    public static Item protocolChip;

    public static final Seq<Item> KellexItems = new Seq<>();

    private WHItems() {
    }

    public static void load() {
        imperium = new Item("imperium", Color.valueOf("FFFFFF")){{
            hidden = true;
            alwaysUnlocked = true;
        }};
        molybdenum = new Item("molybdenum", Color.valueOf(" DF90CAFF")){{
            hardness = 5;
            cost = 1.2f;
            healthScaling = 0.8f;
        }};
        molybdenumAlloy = new Item("molybdenum-alloy",WHPal.molybdenumAlloyColor){{
            hardness = 5;
            cost = 1.2f;
            healthScaling = 0.8f;
        }};
        oreSand = new Item("ore-sand", Color.valueOf("998165FF")){{
            hidden = true;
            alwaysUnlocked = true;
        }};
        resonantCrystal = new Item("resonant-crystal",WHPal.resonantCrystalColor){{
            cost = 1.1f;
            healthScaling = 0.8f;
        }};
        protocolChip = new Item("protocol-chip", Color.valueOf("FF6363FF")){{
            charge = 0.8f;
        }};
        titaniumSteel = new Item("titanium-steel", WHPal.TiSteelColor){{
            cost = 0.75f;
        }};
        vibranium = new Item("vibranium",Color.valueOf("85CBFFFF")){{
            cost=4.3f;
            hardness=7;
            healthScaling = 1.4f;
        }};

        ceramite = new Item("ceramite",WHPal.CeramiteColor){{
            cost = 1.8f;
            healthScaling = 1.4f;
        }};

        refineCeramite = new Item("refine-ceramite",WHPal.RefineCeramiteColor){{
            cost= 3.75f;
            charge = 0.8f;
            healthScaling = 1.8f;
        }};

        adamantium = new Item("adamantium", Color.valueOf("E3AE6FFF")){{
            cost = 6f;
            healthScaling = 2.1f;
        }};

        sealedPromethium = new Item("sealed-promethium",Color.valueOf("68FFFFFF")){{
            cost = 1f;
            radioactivity = 1.5f;
            flammability = 0.8f;
            explosiveness = 0.8f;
            healthScaling = 0.5f;
            charge = 0.8f;
            frames=12;
            transitionFrames=2;
            frameTime=1;
        }};
    }

}