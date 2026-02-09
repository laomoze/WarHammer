package wh.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.meta.*;
import wh.entities.world.blocks.*;

public class WHBlocksEnvironment{
    public static final Attribute hasPromethium = Attribute.add("promethium");
    public static Block road;
    //ore
    public static Block promethium, vibraniumOre, molybdenumOre,
    mineralSand, quartzSand, promethiumSand, radiationSand;
    //liquid
    public static Block radiationWater, radiationWaterDeep, swageWater, swageWaterDeep, mineralSandSwageWater, mineralSandRadiationWater;
    //Floor
    public static Block apatite, cementFloor, darkHotRock, darkMagmaRock, darkRock, mineralSandstone,
    parasiticTrachyte, trachyte, purpleStone, radiationCraters, radiationRockFloor,
    scorchedEarth, scorchedStone, titaniumCrystal, titaniumCrystalStone;
    //Wall
    public static Block apatiteWall, cementWall, darkRockWall, mineralSandstoneWall,
    purpleStoneWall, quartzSandWall, radiationRockWall,
    scorchedEarthWall, titaniumCrystalWall, trachyteWall;
    //MeltaFloor
    public static Block darkMetalFloor1, darkMetalFloor2, darkMetalFloor3, darkMetalFloor4, darkMetalFloor5, darkMetalFloor6,
    darkMetalFloorDamage,
    darkTile1, darkTile2, darkTile3, metalTile1, metalTile2, metalTile31, metalTile32, metalTile33,
    metalTile34, metalTile4, metalTile5, metalMesh;
    //AutoTileFloor
    public static Block cementTile1, cementTile2, cementTile3, cementTile4;

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

        mineralSand = new Floor("mineral-sand"){{
            itemDrop = WHItems.oreSand;
            playerUnmineable = true;
            attributes.set(Attribute.oil, 0.5f);
            attributes.set(hasPromethium, 0.5f);
        }};

        quartzSand = new Floor("quartz-sand"){{
            itemDrop = Items.sand;
            playerUnmineable = true;
            attributes.set(Attribute.oil, 0.5f);
        }};

        promethiumSand = new Floor("promethium-sand"){{
            itemDrop = Items.sand;
            playerUnmineable = true;
            attributes.set(Attribute.oil, 1.5f);
            attributes.set(hasPromethium, 1f);
        }};

        radiationSand = new Floor("radiation-sand"){{
            itemDrop = Items.sand;
            playerUnmineable = true;
            attributes.set(hasPromethium, 1f);
        }};


        radiationWaterDeep = new Floor("radio-water-deep"){{
            speedMultiplier = 0.2f;
            variants = 0;
            liquidDrop = WHLiquids.swageWater;
            liquidMultiplier = 1.5f;
            isLiquid = true;
            status = WHStatusEffects.rust;
            statusDuration = 120f;
            drownTime = 200f;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        radiationWater = new Floor("radio-water"){{
            speedMultiplier = 0.5f;
            variants = 0;
            status = WHStatusEffects.rust;
            statusDuration = 90f;
            liquidDrop = WHLiquids.swageWater;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        swageWaterDeep = new Floor("swage-water-deep"){{
            speedMultiplier = 0.2f;
            variants = 0;
            liquidDrop = WHLiquids.swageWater;
            liquidMultiplier = 1.5f;
            isLiquid = true;
            status = WHStatusEffects.rust;
            statusDuration = 120f;
            drownTime = 200f;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        swageWater = new Floor("swage-water"){{
            speedMultiplier = 0.5f;
            variants = 0;
            status = WHStatusEffects.rust;
            statusDuration = 90f;
            liquidDrop = WHLiquids.swageWater;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        mineralSandSwageWater = new ShallowLiquid("mineral-sand-water"){{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        ((ShallowLiquid)mineralSandSwageWater).set(swageWater, mineralSand);

        mineralSandRadiationWater = new ShallowLiquid("mineral-sand-water2"){{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        ((ShallowLiquid)mineralSandRadiationWater).set(radiationWater, mineralSand);

        apatite = new Floor("apatite"){{
            attributes.set(Attribute.water, 0.5f);
            variants = 4;
        }};

        cementFloor = new Floor("cement-floor"){{
            variants = 5;
        }};

        darkRock = new Floor("dark-rock"){{
            attributes.set(Attribute.water, -0.25f);
            variants = 4;
        }};

        darkHotRock = new Floor("dark-hot-rock"){{
            attributes.set(Attribute.heat, 0.6f);
            attributes.set(Attribute.water, -0.5f);
            blendGroup = darkRock;

            emitLight = true;
            lightRadius = 30f;
            lightColor = Color.orange.cpy().lerp(Pal.slagOrange, 0.15f).a(0.15f);
        }};

        darkMagmaRock = new Floor("dark-magma-rock"){{
            attributes.set(Attribute.heat, 0.8f);
            attributes.set(Attribute.water, -0.75f);
            blendGroup = darkRock;

            emitLight = true;
            lightRadius = 50f;
            lightColor = Color.orange.cpy().lerp(Pal.slagOrange, 0.15f).a(0.3f);
        }};

        mineralSandstone = new Floor("mineral-sandstone"){{
            itemDrop = WHItems.oreSand;
            playerUnmineable = true;
            attributes.set(Attribute.oil, 0.4f);
            attributes.set(hasPromethium, 0.4f);
        }};

        parasiticTrachyte = new Floor("parasitic-trachyte"){{
            dragMultiplier = 1.1f;
            speedMultiplier = 0.85f;
            attributes.set(Attribute.water, 0.3f);
        }};

        trachyte = new Floor("trachyte");

        purpleStone = new Floor("purple-stone"){{
            /*attributes.set(Attribute.water, -1f);*/
            variants = 4;
        }};

        radiationRockFloor = new Floor("radiation-rock-floor"){{
            attributes.set(Attribute.water, -1f);
        }};

        radiationCraters = new Floor("radiation-craters"){{
            attributes.set(Attribute.water, -1f);
            blendGroup = radiationRockFloor;
        }};

        scorchedEarth = new Floor("scorched-earth"){{
            attributes.set(Attribute.water, 0.3f);
            variants = 5;
        }};

        scorchedStone = new Floor("scorched-stone"){{
            attributes.set(Attribute.water, -1);
        }};

        titaniumCrystal = new Floor("titanium-crystal"){{
            attributes.set(Attribute.oil, 0.8f);
            variants = 4;
            dragMultiplier = 0.8f;
        }};

        titaniumCrystalStone = new Floor("titanium-crystal-stone"){{
            attributes.set(Attribute.oil, 1f);
            dragMultiplier = 0.8f;
        }};

        apatiteWall = new StaticWall("apatite-wall"){{
            apatite.asFloor().wall = this;
        }};

        cementWall = new StaticWall("cement-wall"){{
            cementFloor.asFloor().wall = this;
        }};

        darkRockWall = new StaticWall("dark-rock-wall"){{
            darkRock.asFloor().wall = darkMagmaRock.asFloor().wall = darkHotRock.asFloor().wall = this;
        }};

        mineralSandstoneWall = new StaticWall("mineral-sandstone-wall"){{
            quartzSand.asFloor().wall = this;
        }};

        purpleStoneWall = new StaticWall("purple-stone-wall"){{
            purpleStone.asFloor().wall = this;
        }};

        quartzSandWall = new StaticWall("quartz-sand-wall"){{
            quartzSand.asFloor().wall = this;
        }};

        radiationRockWall = new StaticWall("radiation-rock-wall"){{
            radiationSand.asFloor().wall = radiationRockFloor.asFloor().wall = this;
        }};

        scorchedEarthWall = new StaticWall("scorched-earth-wall"){{
            scorchedStone.asFloor().wall = scorchedEarth.asFloor().wall = this;
        }};

        titaniumCrystalWall = new StaticWall("titanium-crystal-wall"){{
            titaniumCrystalStone.asFloor().wall = titaniumCrystal.asFloor().wall = this;
        }};

        trachyteWall = new StaticWall("trachyte-wall"){{
            trachyte.asFloor().wall = parasiticTrachyte.asFloor().wall = this;
        }};


        darkTile1 = new Floor("dark-tile-1", 0);

        darkTile2 = new Floor("dark-tile-2", 0);

        darkTile3 = new Floor("dark-tile-3", 0);

        metalTile1 = new Floor("metal-tile-1", 0);

        metalTile2 = new Floor("metal-tile-2", 0);

        metalTile31 = new Floor("metal-tile-3-1", 0);

        metalTile32 = new Floor("metal-tile-3-2", 0);

        metalTile33 = new Floor("metal-tile-3-3", 0);

        metalTile34 = new Floor("metal-tile-3-4", 0);

        metalTile4 = new Floor("metal-tile-4", 0);

      /*  metalTile5= new Floor("metal-tile-5", 0){{

        }};*/

        metalMesh = new Floor("metal-mesh", 2);


        cementTile1 = new Floor("cement-tile-autotile-1"){{
            autotile = true;
            drawEdgeOut = false;
            drawEdgeIn = false;
        }};

        cementTile2 = new Floor("cement-tile-autotile-2"){{
            autotile = true;
            drawEdgeOut = false;
            drawEdgeIn = false;
        }};

        cementTile3 = new Floor("cement-tile-autotile-3"){{
            autotile = true;
            drawEdgeOut = false;
            drawEdgeIn = false;
        }};

        cementTile4 = new Floor("cement-tile-autotile-4"){{
            autotile = true;
            drawEdgeOut = false;
            drawEdgeIn = false;
        }};
    }
}
