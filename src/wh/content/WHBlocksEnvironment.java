package wh.content;

import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.MultiPacker;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.environment.*;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BuildVisibility;
import wh.entities.world.blocks.Road;

import static mindustry.type.ItemStack.with;

public class WHBlocksEnvironment {
    public static final Attribute hasPromethium = Attribute.add("promethium");
    public static Block road;
    //ore
    public static Block promethium,
            graphiticOre, manganeseOre, chromiumOre, cobaltOre, uraniumOre, molybdenumOre, vibraniumOre,
            mineralSand, quartzSand, promethiumSand, oilMineralSand, radiationSand;
    //liquid
    public static Block radiationWater, radiationWaterDeep, effluent, effluentDeep,
            mineralSandEffluentWater, mineralSandRadiationWater, radiationSandWater,
            oilMineralSandWater;
    //Floor
    public static Block cementFloor, darkHotRock, darkMagmaRock, darkRock, gravel, darkMineralSandstone, darkMineralFloor, mineralSandFloor, chromiteStone, chromiteFloor, chromiteFloorDark, manganeseStone, manganeseFloor, cobaltStone, cobaltFloor,
            trachyte, oreShale, oreSalt, radiationCraters, radiationRockFloor,
            scorchedEarth, scorchedStone;
    //Wall
    public static Block mineralSandWall, quartzSandWall, cementWall, darkRockWall, darkMineralSandstoneWall, chromiteWall, manganeseWall, cobaltWall,
            oreShaleWall, oreSaltWall, radiationRockWall,
            scorchedEarthWall, trachyteWall, darkMeltaWall,
            abandonedWall, abandonedWallLarge, abandonedFactory, abandonedFactoryBreak;
    //MeltaFloor
    public static Block darkMetalFloor1, darkMetalFloor2, darkMetalFloor3, darkMetalFloor4, darkMetalFloor5, darkMetalFloor6,
            darkMetalFloorDamage,
            darkTile1, darkTile2, darkTile3, metalTile1, metalTile2, metalTile31, metalTile32, metalTile33,
            metalTile34, metalTile4, metalTile5, metalMesh;
    //AutoTileFloor
    public static Block cementTile1, cementTile2, cementTile3, cementTile4;
    //Vents
    public static Block quartzSandVent, cementVent, darkRockVent, radiationRockVent, scorchedEarthVent, chromiteVent, manganeseVent, cobaltVent;
    //Boulders/Props
    public static Block quartzSandBoulder, chromiteBoulder, cobaltBoulder, darkRockBoulder, manganeseBoulder, mineralSandFloorBoulder, radiationBoulder,
            darkMineralSandBoulder, scorchedEarthBoulder, darkStoneCrystalCluster, quartzCrystalCluster,
            chromiteBlock, cobaltBlock, darkMineralSandBlock, manganeseBlock, mineralSandBlock;


    public static void load() {
        road = new Road("road-autotile");

        graphiticOre = new OreBlock("graphitic-ore", Items.graphite) {{
            variants = 4;
        }};

        manganeseOre = new OreBlock("manganese-ore", WHItems.manganese) {{
            variants = 4;
        }};

        chromiumOre = new OreBlock("chromium-ore", WHItems.chromium);

        cobaltOre = new OreBlock("cobalt-ore", WHItems.cobalt) {{
            variants = 4;
        }};

        uraniumOre = new OreBlock("uranium-ore", WHItems.uranium) {{
            variants = 4;
        }};

        molybdenumOre = new OreBlock("molybdenum-ore", WHItems.molybdenum) {{
            variants = 3;
        }};

        vibraniumOre = new OreBlock("vibranium-ore", WHItems.vibranium) {{
            variants = 4;
        }};

        promethium = new Floor("promethium") {{
            speedMultiplier = 0.2f;
            variants = 0;
            status = StatusEffects.tarred;
            statusDuration = 90f;
            liquidDrop = WHLiquids.orePromethium;
            isLiquid = true;
            cacheLayer = CacheLayer.tar;
            obstructsLight = true;
        }};

        mineralSand = new Floor("mineral-sand") {
            {
                variants = 5;
                itemDrop = WHItems.oreSand;
                playerUnmineable = true;
                attributes.set(Attribute.oil, 0.5f);
                attributes.set(hasPromethium, 0.5f);
                hasColor = true;
            }

            @Override
            public void createIcons(MultiPacker packer) {
                super.createIcons(packer);
                mapColor.set(Color.valueOf("CDB99FF").lerp(WHItems.oreSand.color.cpy(), 0.35f)
                        .lerp(Color.gray.cpy(), 0.2f).rgba());
            }
        };

        quartzSand = new Floor("quartz-sand") {{
            variants = 4;
            itemDrop = Items.sand;
            playerUnmineable = true;
            attributes.set(Attribute.oil, 0.5f);
        }};

        oilMineralSand = new Floor("oil-mineral-sand") {{
            itemDrop = WHItems.oreSand;
            playerUnmineable = true;
            attributes.set(Attribute.oil, 2);
            attributes.set(hasPromethium, 0.5f);
        }};

        promethiumSand = new Floor("promethium-sand") {{
            itemDrop = WHItems.oreSand;
            playerUnmineable = true;
            attributes.set(Attribute.oil, 1f);
            attributes.set(hasPromethium, 1f);
        }};

        radiationSand = new Floor("radiation-sand") {{
            itemDrop = WHItems.oreSand;
            playerUnmineable = true;
            attributes.set(hasPromethium, 1f);
        }};


        radiationWaterDeep = new Floor("radio-water-deep") {{
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

        radiationWater = new Floor("radio-water") {{
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

        effluentDeep = new Floor("effluent-deep") {{
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

        effluent = new Floor("effluent") {{
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


        mineralSandEffluentWater = new ShallowLiquid("mineral-sand-swagewater") {{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
            mapColor.set(Color.valueOf("5e7087"));
        }};

        mineralSandRadiationWater = new ShallowLiquid("mineral-sand-radiowater") {{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
            mapColor.set(Color.valueOf("4f8497"));
        }};

        radiationSandWater = new ShallowLiquid("radiation-sandwater") {{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
            mapColor.set(Color.valueOf("6d89a1"));
        }};

        oilMineralSandWater = new ShallowLiquid("oil-mineral-sandwater") {{
            speedMultiplier = 0.8f;
            statusDuration = 50f;
            albedo = 0.9f;
            supportsOverlay = true;
            mapColor.set(Color.valueOf("6d89a1"));
        }};

        cementFloor = new Floor("cement-floor") {{
            variants = 5;
        }};

        darkRock = new Floor("dark-rock") {{
            attributes.set(Attribute.water, -0.25f);
            variants = 4;
        }};

        darkHotRock = new Floor("dark-hot-rock") {{
            attributes.set(Attribute.heat, 0.6f);
            attributes.set(Attribute.water, -0.5f);
            blendGroup = darkRock;

            emitLight = true;
            lightRadius = 30f;
            lightColor = Color.orange.cpy().lerp(Pal.slagOrange, 0.15f).a(0.15f);
        }};

        darkMagmaRock = new Floor("dark-magma-rock") {{
            attributes.set(Attribute.heat, 0.8f);
            attributes.set(Attribute.water, -0.75f);
            blendGroup = darkRock;

            emitLight = true;
            lightRadius = 50f;
            lightColor = Color.orange.cpy().lerp(Pal.slagOrange, 0.15f).a(0.3f);
        }};

        gravel = new Floor("gravel") {{
            variants = 3;
            attributes.set(Attribute.water, -0.2f);
        }};

        darkMineralSandstone = new Floor("dark-mineral-sandstone") {{
            itemDrop = WHItems.oreSand;
            playerUnmineable = true;
            attributes.set(Attribute.oil, 0.4f);
            attributes.set(hasPromethium, 0.4f);
        }

            @Override
            public void createIcons(MultiPacker packer) {
                super.createIcons(packer);
                mapColor.cpy().lerp(Pal.coalBlack, 0.5f);
            }
        };

        darkMineralFloor = new Floor("dark-mineral-floor") {{
            variants = 4;
            attributes.set(Attribute.oil, 0.35f);
            attributes.set(hasPromethium, 0.25f);
        }};

        mineralSandFloor = new Floor("mineral-sand-floor") {{
            variants = 5;
            attributes.set(Attribute.oil, 0.33f);
            attributes.set(hasPromethium, 0.22f);
        }};

        chromiteStone = new Floor("chromite-stone") {{
            variants = 3;
        }};

        chromiteFloor = new Floor("chromite-floor") {{
            variants = 5;
        }};

        chromiteFloorDark = new Floor("chromite-floor-dark") {{
            variants = 3;
        }};

        manganeseStone = new Floor("manganese-stone") {{
            variants = 3;
        }};

        manganeseFloor = new Floor("manganese-floor") {{
            variants = 4;
        }};

        cobaltStone = new Floor("cobalt-stone") {{
            variants = 3;
        }};

        cobaltFloor = new Floor("cobalt-floor") {{
            variants = 4;
        }};

        trachyte = new Floor("trachyte") {{
            variants = 5;
        }};

        oreShale = new Floor("ore-shale") {{
            variants = 4;
        }};

        oreSalt = new Floor("ore-salt") {{
            variants = 0;
            attributes.set(Attribute.water, 0.25f);
            attributes.set(Attribute.oil, 0.3f);
        }};

        radiationRockFloor = new Floor("radiation-rock-floor") {{
            attributes.set(Attribute.water, -1f);
        }};

        radiationCraters = new Floor("radiation-craters") {{
            attributes.set(Attribute.water, -1f);
            blendGroup = radiationRockFloor;
        }};

        scorchedEarth = new Floor("scorched-earth") {{
            attributes.set(Attribute.water, 0.3f);
            variants = 6;
        }};

        scorchedStone = new Floor("scorched-stone") {{
            attributes.set(Attribute.water, -1);
        }};


        quartzSandVent = new SteamVent("quartz-sand-vent") {{
            parent = blendGroup = quartzSand;
            attributes.set(Attribute.steam, 1f);
        }};

        cementVent = new SteamVent("cement-vent") {{
            parent = blendGroup = cementFloor;
            attributes.set(Attribute.steam, 1f);
        }};

        darkRockVent = new SteamVent("dark-rock-vent") {{
            parent = blendGroup = darkRock;
            attributes.set(Attribute.steam, 1f);
        }};

        chromiteVent = new SteamVent("chromite-vent") {{
            parent = blendGroup = chromiteStone;
            attributes.set(Attribute.steam, 1f);
        }};

        manganeseVent = new SteamVent("manganese-vent") {{
            parent = blendGroup = manganeseStone;
            attributes.set(Attribute.steam, 1f);
        }};

        cobaltVent = new SteamVent("cobalt-vent") {{
            parent = blendGroup = cobaltStone;
            attributes.set(Attribute.steam, 1f);
        }};

        radiationRockVent = new SteamVent("radiation-rock-vent") {{
            parent = blendGroup = radiationRockFloor;
            attributes.set(Attribute.steam, 1f);
        }};

        scorchedEarthVent = new SteamVent("scorched-earth-vent") {{
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

        mineralSandWall = new StaticWall("mineral-sand-wall") {{
            mineralSand.asFloor().wall = this;
        }};

        quartzSandWall = new StaticWall("quartz-sand-wall") {{
            quartzSand.asFloor().wall = this;
        }};

        cementWall = new StaticWall("cement-wall") {{
            cementFloor.asFloor().wall = this;
        }};

        darkRockWall = new StaticWall("dark-rock-wall") {{
            darkRock.asFloor().wall = darkMagmaRock.asFloor().wall = darkHotRock.asFloor().wall = this;
        }};

        darkMineralSandstoneWall = new StaticWall("dark-mineral-sandstone-wall") {{
            darkMineralSandstone.asFloor().wall = this;
        }};

        chromiteWall = new StaticWall("chromite-wall") {{
            variants = 3;
            chromiteStone.asFloor().wall = this;
        }};

        manganeseWall = new StaticWall("manganese-wall") {{
            variants = 3;
            manganeseStone.asFloor().wall = this;
        }};

        cobaltWall = new StaticWall("cobalt-wall") {{
            variants = 3;
            cobaltStone.asFloor().wall = this;
        }};

        oreShaleWall = new StaticWall("ore-shale-wall") {{
            variants = 2;
            oreShale.asFloor().wall = this;
        }};

        oreSaltWall = new StaticWall("ore-salt-wall") {{
            variants = 2;
            oreSalt.asFloor().wall = this;
        }};

        radiationRockWall = new StaticWall("radiation-rock-wall") {{
            radiationSand.asFloor().wall = radiationRockFloor.asFloor().wall = this;
        }};

        scorchedEarthWall = new StaticWall("scorched-earth-wall") {{
            scorchedStone.asFloor().wall = scorchedEarth.asFloor().wall = this;
        }};

      /*  titaniumCrystalWall = new StaticWall("titanium-crystal-wall"){{
            titaniumCrystalStone.asFloor().wall = titaniumCrystal.asFloor().wall = this;
        }};*/

        trachyteWall = new StaticWall("trachyte-wall") {{
            trachyte.asFloor().wall = this;
        }};

        abandonedWall = new Wall("abandoned-wall") {{
            health = 400;
            variants = 5;
            requirements(Category.defense, BuildVisibility.sandboxOnly, with(WHItems.manganese, 10, WHItems.chromium, 10));
        }};

        abandonedWallLarge = new Wall("abandoned-wall-large") {{
            size = 2;
            variants = 4;
            health = abandonedWall.health * 4;
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.mult(abandonedWall.requirements, 4));
        }};

        abandonedFactory = new Wall("abandoned-factory") {{
            size = 4;
            health = 5500;
            variants = 4;
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.mult(abandonedWall.requirements, 16));
        }};

        abandonedFactoryBreak = new Wall("abandoned-factory-break") {{
            size = 4;
            health = 5000;
            variants = 4;
            requirements(Category.defense, BuildVisibility.sandboxOnly, ItemStack.mult(abandonedWall.requirements, 14));
        }};

        darkMeltaWall = new StaticWall("dark-melta-wall") {{
            variants = 4;
        }};

        quartzSandBoulder = new Prop("quartz-sand-boulder") {{
            variants = 3;
            quartzSand.asFloor().decoration = this;
        }};

        chromiteBoulder = new Prop("chromite-boulder") {{
            variants = 3;
            chromiteStone.asFloor().decoration = this;
        }};

        cobaltBoulder = new Prop("cobalt-boulder") {{
            variants = 3;
            cobaltStone.asFloor().decoration = this;
        }};

        darkRockBoulder = new Prop("dark-rock-boulder") {{
            variants = 3;
            darkRock.asFloor().decoration = darkHotRock.asFloor().decoration = darkMagmaRock.asFloor().decoration = this;
        }};

        manganeseBoulder = new Prop("manganese-boulder") {{
            variants = 3;
            manganeseStone.asFloor().decoration = this;
        }};

        darkMineralSandBoulder = new Prop("dark-mineral-sand-boulder") {{
            variants = 2;
            darkMineralFloor.asFloor().decoration = darkMineralSandstone.asFloor().decoration = this;
        }};

        mineralSandFloorBoulder = new Prop("mineral-sand-floor-boulder") {{
            variants = 3;
            mineralSandFloor.asFloor().decoration = this;
        }};

        radiationBoulder = new Prop("radiation-boulder") {{
            variants = 2;
            radiationSand.asFloor().decoration = radiationRockFloor.asFloor().decoration = radiationCraters.asFloor().decoration = this;
        }};

        scorchedEarthBoulder = new Prop("scorched-earth-boulder") {{
            variants = 3;
            scorchedEarth.asFloor().decoration = scorchedStone.asFloor().decoration = this;
        }};

        darkStoneCrystalCluster = new TallBlock("dark-stone-crystal-cluster") {{
            variants = 3;
            customShadow = true;
            clipSize = 128f;
            oreShale.asFloor().decoration = this;
        }};

        quartzCrystalCluster = new TallBlock("quartz-crystal-cluster") {{
            variants = 3;
            customShadow = true;
            clipSize = 128f;
            quartzSand.asFloor().decoration = this;
        }};

        chromiteBlock = new TallBlock("chromite-block") {{
            variants = 2;
            customShadow = true;
            clipSize = 128f;
        }};

        cobaltBlock = new TallBlock("cobalt-block") {{
            variants = 2;
            customShadow = true;
            clipSize = 128f;
        }};

        darkMineralSandBlock = new TallBlock("dark-mineral-sand-block") {{
            variants = 2;
            customShadow = true;
            clipSize = 128f;
        }};

        manganeseBlock = new TallBlock("manganese-block") {{
            variants = 2;
            customShadow = true;
            clipSize = 128f;
        }};

        mineralSandBlock = new TallBlock("mineral-sand-block") {{
            variants = 2;
            customShadow = true;
            clipSize = 128f;
        }};

        darkMetalFloor1 = new Floor("dark-melta-floor", 0);

        darkMetalFloor2 = new Floor("dark-melta-floor-2", 0);

        darkMetalFloor3 = new Floor("dark-melta-floor-3", 0);

        darkMetalFloorDamage = new Floor("dark-melta-floor-damage", 3);

        darkMetalFloor4 = darkMetalFloorDamage;

        darkMetalFloor5 = new Floor("dark-melta-floor-2-damage", 3);

        darkMetalFloor6 = new Floor("dark-melta-floor-3-damage", 3);

        darkMetalFloor1.asFloor().wall = darkMeltaWall;


        darkTile1 = new Floor("dark-tile-1", 0);

        darkTile2 = new Floor("dark-tile-2", 0);

        darkTile3 = new Floor("dark-tile-3", 0);

      /*  metalTile1 = new Floor("metal-tile-1", 0);

        metalTile2 = new Floor("metal-tile-2", 0);

        metalTile31 = new Floor("metal-tile-3-1", 0);

        metalTile32 = new Floor("metal-tile-3-2", 0);

        metalTile33 = new Floor("metal-tile-3-3", 0);

        metalTile34 = new Floor("metal-tile-3-4", 0);*/

        metalTile4 = new Floor("metal-tile-4", 0);

        /* metalTile5 = new Floor("metal-tile-5", 0);*/

        metalMesh = new Floor("metal-mesh-autotile") {{
            autotile = true;
            drawEdgeOut = true;
            drawEdgeIn = false;
        }};

        cementTile1 = new Floor("cement-tile-autotile-1") {{
            autotile = true;
            drawEdgeOut = true;
            drawEdgeIn = false;
        }};

        cementTile2 = new Floor("cement-tile-autotile-2") {{
            autotile = true;
            drawEdgeOut = true;
            drawEdgeIn = false;
        }};

        cementTile3 = new Floor("cement-tile-autotile-3") {{
            autotile = true;
            drawEdgeOut = true;
            drawEdgeIn = false;
        }};

        cementTile4 = new Floor("cement-tile-autotile-4") {{
            autotile = true;
            drawEdgeOut = true;
            drawEdgeIn = false;
        }};

        ((ShallowLiquid) mineralSandEffluentWater).set(effluent, mineralSand);
        ((ShallowLiquid) mineralSandRadiationWater).set(radiationWater, mineralSand);
        ((ShallowLiquid) radiationSandWater).set(radiationWater, radiationSand);
        ((ShallowLiquid) oilMineralSandWater).set(effluent, oilMineralSand);
    }

    public static boolean isMineralCoreFloor(Block floor) {
        return floor == darkMineralFloor || floor == mineralSandFloor;
    }

    public static Block defaultMineralFloor() {
        return mineralSandFloor != null ? mineralSandFloor : darkMineralFloor;
    }
}
