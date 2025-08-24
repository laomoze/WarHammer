package wh.content;

import arc.graphics.Color;
import mindustry.content.*;
import mindustry.type.*;
import wh.graphics.WHPal;

public final class WHLiquids{
    public static Liquid orePromethium, refinePromethium, liquidNitrogen, swageWater;

    private WHLiquids(){
    }

    public static void load(){

        orePromethium = new Liquid("ore-promethium", WHPal.OrePromethiumColor){{
            lightColor = WHPal.OrePromethiumColor;
            flammability = 0.6f;
            temperature = 0.6f;
            heatCapacity = 0.2f;
            explosiveness = 0.4f;
            viscosity = 1f;
            canStayOn.addAll(Liquids.water,swageWater);
        }};

        refinePromethium = new Liquid("refine-promethium", WHPal.RefinePromethiumColor){{
            lightColor = WHPal.RefinePromethiumColor;
            flammability = 0.5f;
            temperature = 0.6f;
            heatCapacity = 0.2f;
            viscosity = 0.5f;
            explosiveness = 0.5f;
            canStayOn.addAll(Liquids.water,swageWater);
        }};

        liquidNitrogen = new Liquid("liquid-nitrogen", Color.valueOf("97C9FFD0")){
            {
                lightColor = Color.valueOf("97C9FFD0");
                flammability = 0f;
                temperature = 0.1f;
                heatCapacity = 1.6f;
                explosiveness = 0f;
                viscosity = 0.3f;
                canStayOn.addAll(Liquids.water, Liquids.oil,swageWater);
            }
        };
        swageWater = new Liquid("liquid-swage-water", Color.valueOf("92E1ADFF")){
            {
                coolant = false;
                heatCapacity = 0.4f;
                effect = StatusEffects.wet;
                boilPoint = 0.5f;
                gasColor = Color.grays(0.9f);
                barColor = Liquids.cyanogen.color.cpy().lerp(Color.white, 0.5f);
                alwaysUnlocked = true;
            }
        };
    }
}
