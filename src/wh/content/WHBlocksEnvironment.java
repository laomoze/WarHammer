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
    public static Block promethium,
    manganeseOre, chromiumOre, cobaltOre, uraniumOre, molybdenumOre, vibraniumOre,
    mineralSand, quartzSand, promethiumSand, radiationSand;
    //liquid
    public static Block radiationWater, radiationWaterDeep, effluent, effluentDeep, mineralSandEffluentWater, mineralSandRadiationWater, radiationSandWater;
    //Floor
    public static Block apatite, cementFloor, darkHotRock, darkMagmaRock, darkRock, gravel, mineralSandstone, mineralFloor, mineralSandFloor, chromiteStone, chromiteFloor, chromiteFloorDark, manganeseStone, manganeseFloor, cobaltStone, cobaltFloor,
    trachyte, oreShale, oreSalt, radiationCraters, radiationRockFloor,
    scorchedEarth, scorchedStone;
    //Wall
    public static Block apatiteWall, cementWall, darkRockWall, mineralSandstoneWall, chromiteWall, manganeseWall, cobaltWall,
    oreShaleWall, oreSaltWall, quartzSandWall, radiationRockWall,
    scorchedEarthWall, trachyteWall;
    //MeltaFloor
    public static Block darkMetalFloor1, darkMetalFloor2, darkMetalFloor3, darkMetalFloor4, darkMetalFloor5, darkMetalFloor6,
    darkMetalFloorDamage,
    darkTile1, darkTile2, darkTile3, metalTile1, metalTile2, metalTile31, metalTile32, metalTile33,
    metalTile34, metalTile4, metalTile5, metalMesh;
    //AutoTileFloor
    public static Block cementTile1, cementTile2, cementTile3, cementTile4;
    //Vents
    public static Block apatiteVent, cementVent, darkRockVent, radiationRockVent, scorchedEarthVent;
    //Boulders/Props
    public static Block apatiteBoulder, chromiteBoulder, cobaltBoulder, darkRockBoulder, manganeseBoulder, mineralSandBoulder, mineralSandFloorBoulder, radiationBoulder,
    darkStoneCrystalCluster, quartzCrystalCluster;


    public static void load(){
        road = new Road("road-autotile");
        manganeseOre = new OreBlock("manganese-ore", WHItems.manganese){{
            variants = 4;
        }};

        chromiumOre = new OreBlock("chromium-ore", WHItems.chromium);

        cobaltOre = new OreBlock("cobalt-ore", WHItems.cobalt){{
            variants = 4;
        }};

        uraniumOre = new OreBlock("uranium-ore", WHItems.uranium){{
            variants = 4;
        }};

        molybdenumOre = new OreBlock("molybdenum-ore", WHItems.molybdenum){{
            variants = 3;
        }};

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
            variants = 5;
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
            itemDrop = WHItems.oreSand;
            playerUnmineable = true;
            attributes.set(Attribute.oil, 1.5f);
            attributes.set(hasPromethium, 1f);
        }};

        radiationSand = new Floor("radiation-sand"){{
            itemDrop = WHItems.oreSand;
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

        effluentDeep = new Floor("effluent-deep"){{
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

        effluent = new Floor("effluent"){{
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

        mineralSandEffluentWater = new ShallowLiquid("mineral-sand-swagewater"){{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
            mapColor.set(Color.valueOf("5e7087"));
        }};

        ((ShallowLiquid)mineralSandEffluentWater).set(effluent, mineralSand);

        mineralSandRadiationWater = new ShallowLiquid("mineral-sand-radiowater"){{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
            mapColor.set(Color.valueOf("4f8497"));
        }};

        ((ShallowLiquid)mineralSandRadiationWater).set(radiationWater, mineralSand);

        radiationSandWater = new ShallowLiquid("radiation-sandwater"){{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
            mapColor.set(Color.valueOf("6d89a1"));
        }};

        ((ShallowLiquid)radiationSandWater).set(radiationWater, radiationSand);

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

        gravel = new Floor("gravel"){{
            variants = 3;
            attributes.set(Attribute.water, -0.2f);
        }};

        mineralSandstone = new Floor("mineral-sandstone"){{
            itemDrop = WHItems.oreSand;
            playerUnmineable = true;
            attributes.set(Attribute.oil, 0.4f);
            attributes.set(hasPromethium, 0.4f);
        }};

        mineralFloor = new Floor("mineral-floor"){{
            variants = 4;
            attributes.set(Attribute.oil, 0.35f);
            attributes.set(hasPromethium, 0.25f);
        }};

        mineralSandFloor = new Floor("mineral-sand-floor"){{
            variants = 5;
            blendGroup = mineralFloor;
            attributes.set(Attribute.oil, 0.33f);
            attributes.set(hasPromethium, 0.22f);
        }};

        chromiteStone = new Floor("chromite-stone"){{
            variants = 3;
        }};

        chromiteFloor = new Floor("chromite-floor"){{
            variants = 5;
        }};

        chromiteFloorDark = new Floor("chromite-floor-dark"){{
            variants = 3;
            blendGroup = chromiteFloor;
        }};

        manganeseStone = new Floor("manganese-stone"){{
            variants = 3;
        }};

        manganeseFloor = new Floor("manganese-floor"){{
            variants = 4;
        }};

        cobaltStone = new Floor("cobalt-stone"){{
            variants = 3;
        }};

        cobaltFloor = new Floor("cobalt-floor"){{
            variants = 4;
        }};

        trachyte = new Floor("trachyte"){{
            variants = 5;
        }};

        oreShale = new Floor("ore-shale"){{
            variants = 4;
        }};

        oreSalt = new Floor("ore-salt"){{
            variants = 0;
            attributes.set(Attribute.water, 0.25f);
            attributes.set(Attribute.oil, 0.3f);
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
            variants = 6;
        }};

        scorchedStone = new Floor("scorched-stone"){{
            attributes.set(Attribute.water, -1);
        }};

        apatiteVent = new SteamVent("apatite-vent"){{
            parent = blendGroup = apatite;
            attributes.set(Attribute.steam, 1f);
        }};

        cementVent = new SteamVent("cement-vent"){{
            parent = blendGroup = cementFloor;
            attributes.set(Attribute.steam, 1f);
        }};

        darkRockVent = new SteamVent("dark-rock-vent"){{
            parent = blendGroup = darkRock;
            attributes.set(Attribute.steam, 1f);
        }};

        radiationRockVent = new SteamVent("radiation-rock-vent"){{
            parent = blendGroup = radiationRockFloor;
            attributes.set(Attribute.steam, 1f);
        }};

        scorchedEarthVent = new SteamVent("scorched-earth-vent"){{
            parent = blendGroup = scorchedEarth;
            attributes.set(Attribute.steam, 1f);
        }};

      /*  titaniumCrystal = new Floor("titanium-crystal"){{
            attributes.set(Attribute.oil, 0.8f);
            variants = 4;
            dragMultiplier = 0.8f;
        }};

        titaniumCrystalStone = new Floor("titanium-crystal-stone"){{
            attributes.set(Attribute.oil, 1f);
            dragMultiplier = 0.8f;
        }};*/

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
            mineralSandstone.asFloor().wall = this;
        }};

        chromiteWall = new StaticWall("chromite-wall"){{
            variants = 3;
            chromiteStone.asFloor().wall = this;
        }};

        manganeseWall = new StaticWall("manganese-wall"){{
            variants = 3;
            manganeseStone.asFloor().wall = this;
        }};

        cobaltWall = new StaticWall("cobalt-wall"){{
            variants = 3;
            cobaltStone.asFloor().wall = this;
        }};

        oreShaleWall = new StaticWall("ore-shale-wall"){{
            variants = 2;
            oreShale.asFloor().wall = this;
        }};

        oreSaltWall = new StaticWall("ore-salt-wall"){{
            variants = 2;
            oreSalt.asFloor().wall = this;
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

      /*  titaniumCrystalWall = new StaticWall("titanium-crystal-wall"){{
            titaniumCrystalStone.asFloor().wall = titaniumCrystal.asFloor().wall = this;
        }};*/

        trachyteWall = new StaticWall("trachyte-wall"){{
            trachyte.asFloor().wall = this;
        }};

        apatiteBoulder = new Prop("apatite-boulder"){{
            variants = 2;
            apatite.asFloor().decoration = this;
        }};

        chromiteBoulder = new Prop("chromite-boulder"){{
            variants = 3;
            chromiteStone.asFloor().decoration = this;
        }};

        cobaltBoulder = new Prop("cobalt-boulder"){{
            variants = 3;
            cobaltStone.asFloor().decoration = this;
        }};

        darkRockBoulder = new Prop("dark-rock-boulder"){{
            variants = 3;
            darkRock.asFloor().decoration = darkHotRock.asFloor().decoration = darkMagmaRock.asFloor().decoration = this;
        }};

        manganeseBoulder = new Prop("manganese-boulder"){{
            variants = 3;
            manganeseStone.asFloor().decoration = this;
        }};

        mineralSandBoulder = new Prop("mineral-sand-boulder"){{
            variants = 3;
            mineralSand.asFloor().decoration = mineralSandstone.asFloor().decoration = this;
        }};

        mineralSandFloorBoulder = new Prop("mineral-sand-floor-boulder"){{
            variants = 3;
            mineralSandFloor.asFloor().decoration = this;
        }};

        radiationBoulder = new Prop("radiation-boulder"){{
            variants = 2;
            radiationSand.asFloor().decoration = radiationRockFloor.asFloor().decoration = radiationCraters.asFloor().decoration = this;
        }};

        darkStoneCrystalCluster = new TallBlock("dark-stone-crystal-cluster"){{
            variants = 3;
            customShadow = true;
            clipSize = 128f;
            oreShale.asFloor().decoration = this;
        }};

        quartzCrystalCluster = new TallBlock("quartz-crystal-cluster"){{
            variants = 3;
            customShadow = true;
            clipSize = 128f;
            quartzSand.asFloor().decoration = this;
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

        metalTile5 = new Floor("metal-tile-5", 0);

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

    public static boolean isMineralCoreFloor(Block floor){
        return floor == mineralFloor || floor == mineralSandFloor;
    }

    public static Block defaultMineralFloor(){
        return mineralSandFloor != null ? mineralSandFloor : mineralFloor;
    }
}
