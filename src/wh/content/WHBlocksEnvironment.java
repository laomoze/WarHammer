package wh.content;

import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import wh.entities.world.blocks.*;

public class WHBlocksEnvironment{
    public static Block road;
    //ore
    public static Block promethium, vibraniumOre, molybdenumOre;

    public static void load(){
        road = new Road("road-autotile");

        molybdenumOre = new OreBlock("molybdenum-ore", WHItems.molybdenum);

        vibraniumOre = new OreBlock("vibranium-ore", WHItems.vibranium){{
            variants = 4;
        }};

        promethium = new Floor("promethium"){{
            speedMultiplier = 0.2f;
            variants = 0;
            status = StatusEffects.tarred;
            statusDuration = 90f;
            liquidDrop = WHLiquids.orePromethium;
            isLiquid = true;
            cacheLayer = CacheLayer.tar;
            obstructsLight = true;
        }};
    }
}
