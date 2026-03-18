//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.content;

import arc.graphics.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.type.*;
import wh.graphics.*;

public final class WHItems{
    public static Item imperium;
    //ore
    public static Item
    oreSand, manganese, chromium, cobalt, uranium, molybdenum, vibranium;

    public static Item
    manganeseSteel, combustible,
    armorAlloy, entanglement,
    ceramite, cobaltNitride,
    resonantCrystal, culverCrystal,
    molybdenumAlloy, refineCeramite, protocolChip, sealedPromethium, adamantium;

    public static final Seq<Item> kellexItems = new Seq<>();


    private WHItems(){
    }

    public static void load(){

        imperium = new Item("imperium", Color.valueOf("FFFFFF")){{
            hidden = true;
            hardness = 114514;
            alwaysUnlocked = true;
        }};

        oreSand = new Item("ore-sand", Color.valueOf("998165FF")){{
            hidden = true;
            hardness = 1;
            alwaysUnlocked = true;
        }};

        manganese = new Item("manganese", Color.valueOf("947C9EFF")){{
            hardness = 2;
            cost = 0.7f;
            alwaysUnlocked = true;
            healthScaling = 0.06f;
        }};

        chromium = new Item("chromium", Color.valueOf("DC9B94FF")){{
            hardness = 3;
            cost = 0.8f;
            flammability = 0.3f;
            healthScaling = 0.08f;
        }};

        cobalt = new Item("cobalt", Color.valueOf("A9C0CDFF")){{
            hardness = 4;
            cost = 0.9f;
            healthScaling = 0.1f;
        }};

        uranium = new Item("uranium", Color.valueOf("6A9A4DFF").lerp(Pal.plastaniumFront, 0.1f)){{
            hardness = 5;
            explosiveness = 0.2f;
            radioactivity = 1.4f;
            cost = 1.3f;
            healthScaling = 0.2f;
        }};

        molybdenum = new Item("molybdenum", Color.valueOf(" DF90CAFF")){{
            hardness = 6;
            cost = 1.4f;
            healthScaling = 0.8f;
        }};

        vibranium = new Item("vibranium", Color.valueOf("85CBFFFF")){{
            cost = 4f;
            hardness = 7;
            healthScaling = 1.4f;
        }};

        manganeseSteel = new Item("manganese-steel", WHPal.MnSteelColor){{
            cost = 1f;
            healthScaling = 0.12f;
        }};

        combustible = new Item("combustible", Color.valueOf("EC8776FF")){{
            flammability = 1.5f;
            explosiveness = 0.4f;
            cost = 0.05f;
        }};

        armorAlloy = new Item("armor-alloy", Color.valueOf("8693AEFF")){{
            cost = 1.6f;
            healthScaling = 0.3f;
        }};

        cobaltNitride = new Item("cobalt-nitride", Color.valueOf("938197FF")){{
            cost = 1.4f;
            charge = 0.5f;
            healthScaling = 0.2f;
        }};

        ceramite = new Item("ceramite", WHPal.CeramiteColor){{
            cost = 1.6f;
            healthScaling = 0.4f;
        }};

        entanglement = new Item("entanglement", Color.valueOf("8C67ADFF")){{
            cost = 1.5f;
            healthScaling = 0.18f;
            charge = 1;
            radioactivity = 0.8f;
            explosiveness = 0.2f;
        }};

        culverCrystal = new Item("culver-crystal", Color.valueOf("DE5750FF")){{
            cost = 1.4f;
            flammability = 0.2f;
            explosiveness = 0.5f;
            charge = 0.3f;
            healthScaling = 0.5f;
        }};

        refineCeramite = new Item("refine-ceramite", WHPal.RefineCeramiteColor){{
            cost = 3.2f;
            charge = 0.8f;
            healthScaling = 1.8f;
        }};

        molybdenumAlloy = new Item("molybdenum-alloy", WHPal.molybdenumAlloyColor){{
            hardness = 5;
            cost = 1.8f;
            healthScaling = 1f;
        }};

        resonantCrystal = new Item("resonant-crystal", WHPal.resonantCrystalColor){{
            cost = 1.3f;
            charge = 0.3f;
            healthScaling = 0.55f;
        }};

        protocolChip = new Item("protocol-chip", Color.valueOf("FF6363FF")){{
            cost = 2.5f;
            charge = 0.8f;
            frames = 28;
            transitionFrames = 2;
            frameTime = 1;
            healthScaling = 0.8f;
        }};

        adamantium = new Item("adamantium", Color.valueOf("E3AE6FFF")){{
            cost = 6f;
            healthScaling = 2.1f;
        }};

        sealedPromethium = new Item("sealed-promethium", Color.valueOf("68FFFFFF")){{
            cost = 1.5f;
            radioactivity = 1.5f;
            flammability = 0.8f;
            explosiveness = 0.8f;
            healthScaling = 0.3f;
            charge = 0.8f;
            frames = 12;
            transitionFrames = 2;
            frameTime = 1;
        }};

        kellexItems.addAll(oreSand, molybdenum, Items.tungsten, Items.copper, Items.lead, Items.titanium, Items.thorium);
    }
}