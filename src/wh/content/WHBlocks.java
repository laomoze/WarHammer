//
package wh.content;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.defense.RegenProjector;
import mindustry.world.blocks.defense.ShieldWall;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.blocks.distribution.MassDriver;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.heat.HeatConductor;
import mindustry.world.blocks.heat.HeatProducer;
import mindustry.world.blocks.liquid.Conduit;
import mindustry.world.blocks.liquid.LiquidBridge;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import wh.core.*;
import wh.entities.bullet.*;
import wh.entities.bullet.laser.*;
import wh.entities.world.blocks.defense.*;
import wh.entities.world.blocks.defense.turrets.*;
import wh.entities.world.blocks.distribution.*;
import wh.entities.world.blocks.effect.*;
import wh.entities.world.blocks.production.*;
import wh.entities.world.blocks.storage.*;
import wh.entities.world.blocks.unit.*;
import wh.graphics.WHPal;
import wh.entities.world.drawer.*;

import static arc.graphics.g2d.Draw.color;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;
import static wh.graphics.WHPal.CeramiteColor;
import static wh.graphics.WHPal.TiSteelColor;

public final class WHBlocks{
    public static Block promethium;
    public static Block vibraniumOre;
    //factory
    public static Block ADMill, T2TiSteelFurnace, titaniumSteelFurnace,
    ceramiteSteelFoundry, ceramiteRefinery,
    promethiumRefinery, sealedPromethiumMill,
    siliconMixFurnace, atmosphericSeparator, T2CarbideCrucible, largeKiln, T2PlastaniumCompressor, T2Electrolyzer, T2SporePress,
    T2Cultivator, T2CryofluidMixer, T2PhaseSynthesizer, largeSurgeSmelter,
    LiquidNitrogenPlant, slagfurnace, scrapCrusher, scrapFurance, tungstenConverter, t2MultiPress,
    heatSiliconSmelter, crystalEngraver, moSurgeSmelter, sandSeparator, laserEngraver, WaterPurifier, T2WaterPurifier,
    pyratiteBlender, pyratiteSeparator,
    combustionHeater, decayHeater, slagHeatMaker, promethiumHeater, smallHeatRouter, heatBelt, heatBridge, T2heatBridge;
    //drill
    public static Block MechanicalQuarry, heavyCuttingDrill, highEnergyDrill, SpecialCuttingDrill,
    strengthenOilExtractor, slagExtractor, promethiumExtractor, heavyExtractor;
    //liquid
    public static Block gravityPump, tiSteelPump, T2LiquidTank, tiSteelConduit,
    tiSteelBridgeConduit, T2BridgeConduit;
    //effect
    public static Block wrapProjector, wrapOverdrive, armoredVault,
    T2RepairTower, fortlessShield, strongholdCore, T2strongholdCore, selectProjector;
    //distribution
    public static Block steelDust, steelBridge, T2steelBridge, ceramiteConveyor, steelUnloader, trackDriver;
    //power
    public static Block ventDistiller, turboGenerator, crackingGenerator, T2thermalGenerator, T2impactReactor, plaRector, promethiunmRector,
    Mbattery, Lbattery, MK3battery, TiNode, T2TiNode;

    //units

    //turrets
    public static Block
    //22
    Crush, AutoGun,
    //33
    Lcarus, SSWord, Shard, Prevent, Deflection,
    //44
    Pyros, Ionize, Vortex, Viper, HeavyHammer, Flash, ArtilleryBeacon,
    //55
    Collapse, Colossus, Crumble, CycloneMissleLauncher,
    //66
    Hydra, Erase, Annihilate, Melta,
    //88,88+
    Hector, Mezoa;

    //TEST
    public static Block sb, sb3, sb4, sb5, sb6, sb7, sb8, sb9, sb10, sb11,
    sb12, sb13, sb14, sb15, sb16, sb17, sb18, sb19, sb20, sb21,
    sb22, sb23, sb24, sb25, sb26, sb27, sb28, sb29, sb30;


    private WHBlocks(){
    }

    public static void load(){
        promethium = new Floor("promethium");
        vibraniumOre = new OreBlock("vibranium-ore");

        atmosphericSeparator = new HeatCrafter("atmospheric-separator"){
            {

                requirements(Category.crafting, with(Items.lead, 80, Items.metaglass, 80, Items.silicon, 150, WHItems.titaniumSteel, 70));

                hasItems = false;
                hasPower = hasLiquids = true;
                size = 3;
                health = 900;
                craftTime = 60;
                liquidCapacity = 100;
                updateEffect = Fx.none;
                ambientSound = Sounds.extractLoop;
                ambientSoundVolume = 0.06f;
                consumePower(1.5f);
                heatRequirement = 10;
                outputLiquid = new LiquidStack(Liquids.nitrogen, 18f / 60f);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.nitrogen, 4.1f),
                new DrawParticles(){{
                    color = Color.valueOf("d4f0ff");
                    alpha = 0.6f;
                    particleSize = 4f;
                    particles = 10;
                    particleRad = 12f;
                    particleLife = 140f;
                }},
                new DrawDefault(), new DrawHeatInput());
            }
        };

        siliconMixFurnace = new OverheatGenericCrafter("silicon-mix-furnace"){
            {
                requirements(Category.crafting, with(Items.silicon, 80, Items.graphite, 100, Items.silicon, 50, WHItems.titaniumSteel, 50));

                overloadThreshold = 0.7f;
                overloadEfficiency = 0.25f;
                wasteHeatOutput = 1f;

                size = 3;
                health = 900;
                craftTime = 120;
                itemCapacity = 60;
                hasPower = hasItems = true;
                consumePower(5f);
                outputItem = new ItemStack(Items.silicon, 8);
                consumeItems(with(Items.sand, 12, Items.graphite, 5));
                consumeLiquid(Liquids.water, 0.1f);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(){{
                    circleStroke = 1.5f;
                    flameRadiusScl = 2.5f;
                    flameRadiusMag = 0.2f;
                }}, new DrawDefault(),
                new DrawOverheatOutput());
                ambientSound = Sounds.smelter;
                ambientSoundVolume = 0.11f;
                researchCostMultiplier = 0.5f;

            }
        };

        heatSiliconSmelter = new HeatCrafter("heat-silicon-smelter"){{
            requirements(Category.crafting, with(WHItems.molybdenumAlloy, 30, Items.carbide, 40, Items.pyratite, 30, Items.silicon, 100, WHItems.titaniumSteel, 80));
            size = 4;
            health = 2000;
            hasPower = hasItems = true;
            itemCapacity = 60;
            craftTime = 60;
            consumePower(1200 / 60f);
            consumeItems(with(Items.pyratite, 3, Items.graphite, 5, Items.sand, 20));
            outputItem = new ItemStack(Items.silicon, 18);
            maxEfficiency = 1f;
            heatRequirement = 25;
            drawer = new DrawMulti(new DrawRegion("-bottom"),
            new DrawCrucibleFlame(){{
                flameRad = 4.5f;
                circleSpace = 3;
                circleStroke = 0.6f;
                flameRadiusScl = 16;
                flameRadiusMag = 3;
                particleLife = 107;
                particleRad = 16;
                particleSize = 2.68f;
                rotateScl = 1.7f;
            }},
            new DrawArcSmelt(){{
                particleLen = 6f;
                particleRad = 13f;
                particleStroke = 1.6f;
                flameRad = 3f;
                circleSpace = 2.5f;
            }},
            new DrawDefault(),
            new DrawHeatInput());
            researchCostMultiplier = 0.45f;
        }};

        T2CarbideCrucible = new HeatCrafter("t2-carbide-crucible"){{

            requirements(Category.crafting, with(Items.copper, 120, Items.thorium, 80, Items.silicon, 80, WHItems.titaniumSteel, 80));
            size = 3;
            health = 800;
            itemCapacity = 24;
            outputItem = new ItemStack(Items.carbide, 3);
            craftTime = 180;
            heatRequirement = 15;
            hasItems = hasPower = true;
            consumePower(4.5f);
            consumeItems(with(Items.graphite, 3, Items.tungsten, 5));
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawCrucibleFlame(), new DrawDefault(), new DrawHeatInput());
            researchCostMultiplier = 0.45f;
        }};

        largeKiln = new OverheatGenericCrafter("large-kiln"){
            {
                requirements(Category.crafting, with(Items.graphite, 60, WHItems.titaniumSteel, 50, Items.plastanium, 100));

                size = 3;
                health = 360;
                itemCapacity = 40;
                hasPower = hasItems = true;
                craftTime = 90;

                overloadThreshold = 0.7f;
                overloadEfficiency = 0.2f;
                wasteHeatOutput = 1f;

                consumePower(180f / 60f);
                consumeItems(with(Items.sand, 5, Items.lead, 5));
                outputItem = new ItemStack(Items.metaglass, 8);
                drawer = new DrawMulti(new DrawDefault(), new DrawFlame(), new DrawGlowRegion(){{
                    color = Color.valueOf("ffc999");
                }}, new DrawOverheatOutput(), new LargekilnDrawer(Color.valueOf("ffc999")));

                ambientSound = Sounds.smelter;
                ambientSoundVolume = 0.11f;
                researchCostMultiplier = 0.8f;
            }
        };
        T2PlastaniumCompressor = new GenericCrafter("t2-plastanium-compressor"){
            {

                requirements(Category.crafting, with(Items.lead, 300, Items.titanium, 200, Items.graphite, 150, WHItems.titaniumSteel, 200));
                size = 3;
                health = 650;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                liquidCapacity = 120;
                itemCapacity = 30;
                consumePower(10f);
                consumeLiquid(Liquids.oil, 1);
                consumeItems(with(Items.titanium, 8));
                outputItem = new ItemStack(Items.plastanium, 5);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault(), new T2PlastaniumCompresserDrawer(WHPal.ORL), new DrawFade());
                craftEffect = Fx.formsmoke;
                updateEffect = Fx.plasticburn;
                researchCostMultiplier = 0.6f;
            }
        };
        T2Electrolyzer = new GenericCrafter("t2-electrolyzer"){
            {
                requirements(Category.crafting, with(Items.lead, 120, Items.silicon, 100, Items.graphite, 80, WHItems.titaniumSteel, 40));

                size = 3;
                health = 750;
                hasPower = hasLiquids = true;
                craftTime = 60;
                liquidCapacity = 60;
                itemCapacity = 0;

                rotate = true;
                invertFlip = true;
                consumePower(4f);
                consumeLiquid(Liquids.water, 1f);
                outputLiquids = LiquidStack.with(Liquids.ozone, 30f / 60, Liquids.hydrogen, 45f / 60);
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.water, 2f),
                new DrawBubbles(Color.valueOf("7693e3")){{
                    sides = 10;
                    recurrence = 3f;
                    spread = 6;
                    radius = 1.5f;
                    amount = 20;
                }},
                new DrawRegion(),
                new DrawLiquidOutputs(),
                new DrawGlowRegion(){{
                    alpha = 0.7f;
                    color = Color.valueOf("c4bdf3");
                    glowIntensity = 0.3f;
                    glowScale = 6f;
                }}
                );
                regionRotated1 = 3;
                liquidOutputDirections = new int[]{2, 4};
            }
        };

        slagfurnace = new Separator("slag-furnace"){{

            requirements(Category.crafting, with(Items.graphite, 150, WHItems.titaniumSteel, 400, Items.surgeAlloy, 200, WHItems.ceramite, 200, WHItems.refineCeramite, 80));
            size = 3;
            health = 850;
            hasPower = hasLiquids = hasItems = true;
            craftTime = 60;
            liquidCapacity = 120;
            itemCapacity = 30;
            consumePower(15f);
            consumeLiquid(Liquids.slag, 120 / 60f);
            results = with(
            WHItems.ceramite, 3,
            WHItems.titaniumSteel, 5,
            WHItems.refineCeramite, 2,
            Items.surgeAlloy, 4,
            Items.phaseFabric, 4
            );
            drawer = new DrawMulti(new DrawRegion("-bottom"),
            new DrawLiquidTile(Liquids.slag),
            new DrawDefault(),
            new DrawFlame(Color.valueOf("FF8C7AFF")));
            ambientSound = Sounds.pulse;
            ambientSoundVolume = 0.3f;
            researchCostMultiplier = 0.6f;

        }};

        //钛钢
        titaniumSteelFurnace = new GenericCrafter("titanium-steel-furnace"){
            {
                Color color = WHPal.TiSteelColor;
                requirements(Category.crafting, with(Items.lead, 30, Items.copper, 20, Items.silicon, 30));

                health = 150;
                hasItems = hasPower = true;
                craftTime = 60;
                itemCapacity = 10;
                size = 2;
                drawer = new DrawMulti(new DrawDefault(), new DrawFlame(color));
                consumePower(1);
                consumeItems(with(Items.titanium, 1, Items.metaglass, 2));
                craftEffect = WHFx.square(TiSteelColor, 35f, 4, 16f, 4f);
                outputItem = new ItemStack(WHItems.titaniumSteel, 1);
                researchCostMultiplier = 0.2f;
            }
        };

        T2TiSteelFurnace = new OverheatGenericCrafter("t2-ti-steel-furnace"){
            {

                overloadThreshold = 0.6f;
                overloadEfficiency = 0.3f;
                wasteHeatOutput = 1f;

                Color color = WHPal.TiSteelColor;
                requirements(Category.crafting, with(Items.silicon, 100, Items.carbide, 30, WHItems.ceramite, 50, WHItems.titaniumSteel, 70));
                health = 700;
                hasItems = hasPower = true;
                craftTime = 60;
                itemCapacity = 40;
                size = 3;
                consumePower(5);
                consumeItems(with(Items.titanium, 6, Items.metaglass, 9));
                consumeLiquid(Liquids.water, 15 / 60f);
                outputItem = new ItemStack(WHItems.titaniumSteel, 6);
                drawer = new DrawMulti(new DrawDefault(), new DrawOverheatOutput(), new DrawFlame(color));
                craftEffect = WHFx.square(TiSteelColor, 35f, 6, 26f, 5f);
                researchCostMultiplier = 0.45f;
            }
        };

        t2MultiPress = new GenericCrafter("t2-multi-press"){
            {
                requirements(Category.crafting, with(Items.thorium, 50, Items.graphite, 50, Items.plastanium, 50, WHItems.titaniumSteel, 30));
                health = 500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 30;
                size = 3;
                consumePower(5);
                consumeItems(with(Items.coal, 8));
                consumeLiquid(Liquids.water, 30 / 60f);
                outputItem = new ItemStack(Items.graphite, 6);
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.water),
                new DrawPistons(){{
                    angleOffset = 45f;
                    sinMag = 3f;
                    sinScl = 5f;
                    sides = 4;
                    sideOffset = Mathf.PI / 2f;
                }}, new DrawDefault());
                craftEffect = Fx.pulverizeMedium;
            }
        };

        sandSeparator = new OverheatGenericCrafter("sand-separator"){
            {
                overloadThreshold = 0.6f;
                overloadEfficiency = 0.3f;
                wasteHeatOutput = 1f;

                requirements(Category.crafting, with(Items.copper, 80, Items.lead, 60, Items.graphite, 40, WHItems.titaniumSteel, 20));
                health = 300;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 60;
                liquidCapacity = 90;
                size = 3;
                dumpExtraLiquid = true;
                ignoreLiquidFullness = true;
                consumePower(6);
                consumeItems(with(WHItems.oreSand, 20, Items.graphite, 1));
                outputItem = new ItemStack(Items.sand, 15);
                outputLiquid = new LiquidStack(Liquids.slag, 30 / 60f);
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.slag),
                new DrawRegion("-rotator", -4){{
                    x = -13 / 4f;
                    y = 13 / 4f;
                    spinSprite = true;
                }},
                new DrawRegion("-rotator", 4){{
                    x = 13 / 4f;
                    y = 13 / 4f;
                    spinSprite = true;
                }},
                new DrawRegion("-rotator", -4){{
                    x = 13 / 4f;
                    y = -13 / 4f;
                    spinSprite = true;
                }},
                new DrawRegion("-rotator", 4){{
                    x = -13 / 4f;
                    y = -13 / 4f;
                    spinSprite = true;
                }},
                new DrawDefault(), new DrawOverheatOutput());
                craftEffect = Fx.smokeCloud;
                updateEffect = new Effect(20, e -> {
                    color(Pal.gray, Color.lightGray, e.fin());
                    randLenVectors(e.id, 6, 3f + e.fin() * 6f, (x, y) ->
                    Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45));
                });
                researchCostMultiplier = 0.5f;
            }
        };

        moSurgeSmelter = new OverheatGenericCrafter("mo-surge-smelter"){
            {
                overloadThreshold = 0.5f;
                overloadEfficiency = 2f;
                wasteHeatOutput = 1.5f;

                requirements(Category.crafting, with(Items.tungsten, 60, Items.plastanium, 50, Items.surgeAlloy, 30, WHItems.titaniumSteel, 70));
                health = 900;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 90;
                itemCapacity = 12;
                liquidCapacity = 80f;
                size = 3;
                consumePower(3);
                consumeItems(with(WHItems.molybdenum, 3, Items.surgeAlloy, 1));
                consumeLiquid(Liquids.slag, 12 / 60f);
                outputItem = new ItemStack(WHItems.molybdenumAlloy, 2);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.slag), new DrawCircles(){{
                    color = WHPal.OR.a(0.4f);
                    strokeMax = 2.5f;
                    radius = 10f;
                    amount = 3;
                }}, new DrawDefault(), new DrawOverheatOutput());
                craftEffect = new RadialEffect(Fx.surgeCruciSmoke, 4, 90f, 7f);
                researchCostMultiplier = 0.45f;
            }
        };

        crystalEngraver = new OverheatGenericCrafter("crystal-engraver"){
            {
                requirements(Category.crafting, with(Items.silicon, 40, WHItems.titaniumSteel, 40, WHItems.molybdenum, 30, Items.tungsten, 40));

                overloadThreshold = 0.5f;
                overloadEfficiency = 0.5f;
                wasteHeatOutput = 1.5f;

                health = 1600;
                hasItems = hasPower = true;
                craftTime = 120;
                itemCapacity = 12;
                liquidCapacity = 120;
                size = 3;
                consumePower(5);
                consumeItems(with(WHItems.molybdenum, 2, Items.metaglass, 2, Items.silicon, 3));
                outputItem = new ItemStack(WHItems.resonantCrystal, 3);
                drawer = new DrawMulti(new EngraverDraw(){{
                    rotate = true;
                }}, new DrawArcSmelt(), new DrawDefault(),
                new EngraverDraw(){{
                    backLength = 2f;
                    frontLength = 1.5f;
                    width = 5.3f;
                    time = 120;
                    lengthMag = 0.6f;
                }}, new DrawOverheatOutput());
                craftEffect = WHFx.square(WHItems.resonantCrystal.color, 35f, 4, 16f, 3f);
                researchCostMultiplier = 0.45f;
            }
        };

        laserEngraver = new OverheatGenericCrafter("laser-engraver"){
            {
                requirements(Category.crafting, with(Items.thorium, 50, Items.carbide, 80, WHItems.resonantCrystal, 70, WHItems.molybdenumAlloy, 80, WHItems.titaniumSteel, 40));

                overloadThreshold = 0.3f;
                overloadEfficiency = 0.5f;
                wasteHeatOutput = 4f;

                health = 2500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 180;
                itemCapacity = 20;
                liquidCapacity = 120;
                size = 4;
                consumePower(5);
                consumeItems(with(Items.silicon, 6, WHItems.molybdenumAlloy, 4, Items.carbide, 3));
                outputItem = new ItemStack(WHItems.protocolChip, 3);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawCircles(){{
                    color = Pal.accent;
                    radius = 15f;
                    timeScl = 300f;
                    amount = 2;
                }}, new DrawArcs(){{
                    drawCenter = false;
                    flameColor = Pal.accent;
                    arcRad = 16f;
                    arcLife = 70f;
                }},
                new DrawArcSmelt(){{
                    flameRad = 3.2f;
                    particleRad = 15f;
                    particleLen = 3.5f;
                }}, new DrawDefault(), new DrawOverheatOutput());
                consumeLiquid(WHLiquids.refinePromethium, 10 / 60f);
                craftEffect = new MultiEffect(WHFx.square(Liquids.slag.color, 35f, 8, 32, 4f),
                WHFx.diffuse(size, Liquids.slag.color, 60f));
                researchCostMultiplier = 0.45f;
            }
        };

        WaterPurifier = new GenericCrafter("water-purifier"){
            {
                requirements(Category.crafting, with(Items.silicon, 40, WHItems.titaniumSteel, 80, Items.graphite, 50));
                health = 600;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 12;
                liquidCapacity = 300;
                size = 3;
                consumePower(8);
                consumeItems(with(WHItems.oreSand, 2, Items.graphite, 2));
                consumeLiquid(WHLiquids.swageWater, 100 / 60f);
                outputLiquid = new LiquidStack(Liquids.water, 70 / 60f);
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(WHLiquids.swageWater),
                new DrawLiquidTile(Liquids.water),
                new DrawBubbles(WHLiquids.swageWater.color.cpy().lerp(Liquids.water.color.cpy(), 0.5f)){{
                    sides = 10;
                    recurrence = 3f;
                    spread = 6;
                    radius = 1.5f;
                    amount = 20;
                }}, new DrawCircles(){{
                    color = WHLiquids.swageWater.color.cpy().lerp(Liquids.water.color.cpy(), 0.5f);
                    amount = 3;
                    strokeMax = 1.5f;
                }},
                new DrawDefault(),
                new DrawRegion("-rotator"){{
                    rotateSpeed = 2;
                    spinSprite = true;
                }},
                new DrawRegion("-top"),
                new DrawGlowRegion(){{
                    alpha = 0.7f;
                    color = Color.valueOf("c4bdf3");
                    glowIntensity = 0.3f;
                    glowScale = 6f;
                }});
            }
        };

        T2WaterPurifier = new GenericCrafter("t2-water-purifier"){
            {
                requirements(Category.crafting, with(Items.silicon, 80, Items.carbide, 30, WHItems.molybdenum, 100, WHItems.titaniumSteel, 40, Items.graphite, 50));
                health = 1400;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 30;
                liquidCapacity = 600;
                size = 4;
                consumePower(15);
                consumeItems(with(WHItems.oreSand, 1, Items.plastanium, 1, Items.graphite, 2));
                consumeLiquid(WHLiquids.swageWater, 400 / 60f);
                outputLiquid = new LiquidStack(Liquids.water, 340 / 60f);
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(WHLiquids.swageWater),
                new DrawLiquidTile(Liquids.water),
                new DrawBubbles(WHLiquids.swageWater.color.cpy().lerp(Liquids.water.color.cpy(), 0.3f)){{
                    sides = 10;
                    recurrence = 3f;
                    spread = 6;
                    radius = 1.5f;
                    amount = 20;
                }}, new DrawCircles(){{
                    color = WHLiquids.swageWater.color.cpy().lerp(Liquids.water.color.cpy(), 0.3f);
                    amount = 3;
                    strokeMax = 1.5f;
                }}, new DrawCrucibleFlame(){{
                    flameColor = WHLiquids.swageWater.color.cpy().lerp(Liquids.water.color.cpy(), 0.3f);
                    particleRad = 15f;
                }}, new DrawDefault());
                updateEffect = new RadialEffect(Fx.steamCoolSmoke, 4, 90f, 55 / 4f){{
                    rotationOffset = 45F;
                }};
                updateEffectSpread = 0;
            }
        };

        pyratiteBlender = new GenericCrafter("pyratite-blender"){
            {
                requirements(Category.crafting, with(Items.copper, 80, Items.silicon, 40, Items.graphite, 50, WHItems.titaniumSteel, 50));

                hasItems = true;
                hasPower = true;
                consumePower(90 / 60f);
                consumeItems(with(Items.coal, 3, WHItems.oreSand, 5));
                consumeLiquid(Liquids.hydrogen, 10 / 60f);
                outputItem = new ItemStack(Items.pyratite, 3);

                updateEffect = new ExplosionEffect();
                updateEffectChance = 0.02f;

                size = 3;
                craftTime = 90;
                itemCapacity = 20;
                health = 600;
            }
        };

        pyratiteSeparator = new GenericCrafter("pyratite-separator"){
            {
                requirements(Category.crafting, with(WHItems.ceramite, 20, Items.phaseFabric, 30, WHItems.molybdenum, 50, WHItems.titaniumSteel, 40));
                health = 1500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 180;
                itemCapacity = 60;
                liquidCapacity = 60;
                size = 3;
                ignoreLiquidFullness = true;
                consumeLiquid(Liquids.ozone, 10 / 60f);
                consumeItems(with(WHItems.oreSand, 20, Items.graphite, 4));
                outputLiquid = new LiquidStack(Liquids.slag, 1.5f);
                outputItem = new ItemStack(Items.pyratite, 6);
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.slag),
                new DrawLiquidTile(Liquids.ozone){{
                    alpha = 0.2f;
                }},
                new DrawCrucibleFlame(){{
                    particleRad = 12f;
                }},
                new DrawRegion("-rotator1"){{
                    rotateSpeed = 1.2f;
                }},
                new DrawRegion("-rotator2"){{
                    rotateSpeed = -0.6f;
                }},
                new DrawDefault()
                );
            }
        };


        T2SporePress = new GenericCrafter("t2-spore-press"){{

            requirements(Category.crafting, with(Items.graphite, 30, Items.silicon, 60, Items.plastanium, 30, WHItems.titaniumSteel, 70));
            health = 700;
            hasItems = hasPower = hasLiquids = true;
            craftTime = 60;
            itemCapacity = 40;
            liquidCapacity = 120;
            size = 3;
            consumePower(2);
            consumeItems(with(Items.sporePod, 9));
            outputLiquid = new LiquidStack(Liquids.oil, 1);
            drawer = new DrawMulti(
            new DrawRegion("-bottom"),
            new DrawRegion("-rotator", 4){{
                spinSprite = true;
            }},
            new DrawDefault());
            new DrawLiquidRegion(Liquids.oil);

            researchCostMultiplier = 0.45f;
        }};

        T2Cultivator = new AttributeCrafter("t2-cultivator"){{
            requirements(Category.crafting, with(Items.copper, 90, Items.silicon, 60, WHItems.titaniumSteel, 50));
            health = 400;
            hasItems = hasPower = hasLiquids = true;
            craftTime = 120;
            itemCapacity = 20;
            liquidCapacity = 120;
            size = 3;
            consumePower(5);
            consumeLiquid(Liquids.water, 30f / 60f);
            outputItem = new ItemStack(Items.sporePod, 6);

            craftEffect = Fx.none;
            envRequired |= Env.spores;
            attribute = Attribute.spores;
            legacyReadWarmup = true;
            drawer = new DrawMulti(
            new DrawDefault(),
            new DrawCultivator(){{
                radius = 4f;
            }},
            new DrawRegion("-top")
            );
            maxBoost = 2f;
            researchCostMultiplier = 0.45f;
        }};

        T2CryofluidMixer = new GenericCrafter("t2-cryofluid-mixer"){
            {
                requirements(Category.crafting, with(Items.silicon, 80, WHItems.ceramite, 20, WHItems.titaniumSteel, 50));
                health = 600;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 20;
                liquidCapacity = 120;
                size = 3;
                consumePower(4);
                consumeLiquid(Liquids.water, 0.5f);
                outputLiquid = new LiquidStack(Liquids.cryofluid, 0.65f);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.water),
                new DrawLiquidTile(Liquids.cryofluid){{
                    drawLiquidLight = true;
                }}, new DrawDefault());
                craftEffect = WHFx.square(Liquids.cryofluid.color, 35f, 4, 16f, 5f);
                researchCostMultiplier = 0.45f;
            }
        };

        T2PhaseSynthesizer = new GenericCrafter("t2-phase-synthesizer"){{

            requirements(Category.crafting, with(Items.silicon, 120, Items.carbide, 20, WHItems.ceramite, 50, WHItems.ceramite, 20, WHItems.titaniumSteel, 100));
            health = 1000;
            hasItems = hasPower = hasLiquids = true;
            craftTime = 120;
            itemCapacity = 20;
            liquidCapacity = 120;
            size = 3;
            consumePower(5);
            consumeLiquid(Liquids.ozone, 30f / 60f);
            consumeItems(with(Items.thorium, 4, Items.sand, 10));
            outputItem = new ItemStack(Items.phaseFabric, 5);
            drawer = new DrawMulti(new DrawRegion("-bottom"),
            new DrawLiquidTile(Liquids.ozone){{
                drawLiquidLight = true;
                alpha = 0.8f;
            }},
            new PhaseWeave(),
            new DrawMultiWeave(){{
                glowColor = new Color(1f, 0.4f, 0.4f, 0.4f);
            }},
            new DrawDefault());
            craftEffect = WHFx.square(Items.phaseFabric.color, 35f, 8, 16f, 5f);
            researchCostMultiplier = 0.45f;
        }};
        //钷素
        promethiumRefinery = new OverheatGenericCrafter("promethium-refinery"){
            {

                requirements(Category.crafting, with(Items.phaseFabric, 20, Items.tungsten, 50, WHItems.ceramite, 20, WHItems.titaniumSteel, 50));

                overloadThreshold = 0.5f;
                overloadEfficiency = 0.25f;
                wasteHeatOutput = 2f;

                health = 1200;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 20;
                liquidCapacity = 180;
                size = 3;
                consumePower(5);
                consumeItems(with(Items.phaseFabric, 3));
                consumeLiquid(Liquids.oil, 45f / 60f);
                consumeLiquid(WHLiquids.orePromethium, 15 / 60f);
                outputLiquid = new LiquidStack(WHLiquids.refinePromethium, 31f / 60f);
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(WHLiquids.orePromethium),
                new DrawLiquidTile(WHLiquids.refinePromethium),
                new DrawGlowRegion(){{
                    color = WHPal.RefinePromethiumColor;
                    rotateSpeed = 3f;
                    glowIntensity = 0.5f;
                    alpha = 0.6f;
                }},
                new DrawDefault(),
                new DrawOverheatOutput());
                craftEffect = WHFx.square(WHLiquids.refinePromethium.color, 35f, 4, 16f, 5f);
                researchCostMultiplier = 0.6f;
            }
        };

        sealedPromethiumMill = new GenericCrafter("sealed-promethium-mill"){{

            requirements(Category.crafting, with(Items.tungsten, 150, Items.plastanium, 100, WHItems.ceramite, 60, Items.phaseFabric, 80));

            health = 2000;
            hasItems = hasPower = hasLiquids = true;
            craftTime = 120;
            itemCapacity = 40;
            liquidCapacity = 120;
            size = 4;
            consumePower(15);
            consumeLiquid(WHLiquids.refinePromethium, 10f / 60f);
            consumeItems(with(Items.phaseFabric, 5, WHItems.ceramite, 2));
            outputItems = with(WHItems.sealedPromethium, 3);
            drawer = new DrawMulti(new DrawRegion("-bottom"),
            new DrawLiquidTile(WHLiquids.refinePromethium){{
                alpha = 0.5f;
            }},
            new SealedPromethiumMillDrawer(),
            new DrawRegion("-mid"),
            new DrawArcs(){{
                flameRad = 2f;
                midColor = flameColor = Pal.sapBullet;
                arcs = 8;
                arcRange = size * 2;
                arcLife = 60f;
            }},
            new DrawDefault(),
            new DrawArcSmelt(){{
                drawCenter = false;
                midColor = flameColor = Pal.sapBullet;
                particleRad = 40f;
                particleLen = 7f;
                particleLife = 60f;

            }},
            new DrawGlowRegion(Layer.blockAdditive + 0.1f){{
                color = Pal.sapBullet;
                glowIntensity = 1f;
                glowScale = 6f;
            }}
            );
            craftEffect = new MultiEffect(new WrapEffect(WHFx.circleOut(40, 40f, 5), Pal.sapBullet),
            new WrapEffect(WHFx.circleOut(30, 40f, 5), Pal.sapBullet).startDelay(40));

            destroyBullet = WHBullets.sealedPromethiumMillBreak;
            researchCostMultiplier = 0.6f;
        }};

        scrapCrusher = new MultiCrafter("scrap-crusher"){{

            requirements(Category.crafting, with(Items.silicon, 50, Items.lead, 50, WHItems.titaniumSteel, 30, Items.graphite, 80));

            health = 400;
            hasItems = hasPower = true;
            hasLiquids = false;
            itemCapacity = 20;
            outputsLiquid = false;
            size = 2;
            useBlockDrawer = true;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault());
            craftPlans.add(
            new CraftPlan(){{
                craftTime = 60f;
                consumeItem(Items.scrap, 3);
                outputItems = with(Items.sand, 4);
                consumePower(100 / 60f);
                craftEffect = new MultiEffect(WHFx.square(Items.scrap.color, 35f, 4, 16f, 5f),
                WHFx.arcSmelt(Items.copper.color, 13f, 6, 60f));
            }}
            , new CraftPlan(){{
                craftTime = 20f;
                consumeItem(Items.copper, 1);
                outputItems = with(Items.scrap, 2);
                consumePower(100 / 60f);
                craftEffect = new MultiEffect(WHFx.square(Items.copper.color, 35f, 4, 16f, 5f),
                WHFx.arcSmelt(Items.copper.color, 13f, 6, 60f));
            }}
            , new CraftPlan(){{
                craftTime = 30f;
                consumeItem(Items.lead, 1);
                outputItems = with(Items.sand, 2);
                consumePower(100 / 60f);
                craftEffect = new MultiEffect(WHFx.square(Items.lead.color, 35f, 4, 16f, 5f),
                WHFx.arcSmelt(Items.lead.color, 13f, 6, 60f));
            }}, new CraftPlan(){{
                craftTime = 30f;
                consumeItem(Items.titanium, 1);
                outputItems = with(Items.sand, 3);
                consumePower(100 / 60f);
                craftEffect = new MultiEffect(WHFx.square(Items.titanium.color, 35f, 4, 16f, 5f),
                WHFx.arcSmelt(Items.titanium.color, 13f, 6, 60f));
            }});
        }};

        scrapFurance = new GenericCrafter("scrap-furance"){{
            requirements(Category.crafting, with(WHItems.titaniumSteel, 100, Items.lead, 120, Items.graphite, 120));
            health = 800;
            outputLiquid = new LiquidStack(Liquids.slag, 1);
            size = 2;
            craftTime = 10f;
            hasLiquids = hasPower = true;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.slag), new DrawDefault());

            consumePower(3f);
            consumeItem(Items.scrap, 4);
            researchCostMultiplier = 0.6f;
        }};

        largeSurgeSmelter = new GenericCrafter("large-surge-smelter"){{
            requirements(Category.crafting, with(WHItems.titaniumSteel, 120, WHItems.molybdenum, 180, Items.carbide, 80, WHItems.ceramite, 50));

            outputItem = new ItemStack(Items.surgeAlloy, 10);
            size = 4;
            craftTime = 120f;
            health = 1200;
            hasItems = hasPower = hasLiquids = true;
            liquidCapacity = 50;
            itemCapacity = 50;
            consumePower(8f);
            consumeItems(with(Items.copper, 20, Items.silicon, 16, WHItems.titaniumSteel, 16));
            consumeLiquid(WHLiquids.refinePromethium, 10 / 60f);
            drawer = new DrawMulti(new DrawDefault(),
                   /* new DrawFlame() {{
                        flameRadius = 0;
                        flameRadiusIn = 0;
                        flameRadiusMag = 0;
                        flameRadiusInMag = 0;
                        flameColor = Color.valueOf("F2E770");
                    }},*/
            new DrawGlowRegion(){{
                color = Color.valueOf("FFDEB5FF");
                layer = Layer.effect;
                glowIntensity = 0.5f;
            }});
            updateEffect = WHFx.hexagonSpread(Items.surgeAlloy.color, 10f, 20f);
            craftEffect = WHFx.hexagonSmoke(Items.surgeAlloy.color, 30f, 1.2f, 10, 20f);
            researchCostMultiplier = 0.6f;
        }};

        LiquidNitrogenPlant = new GenericCrafter("Liquid-nitrogen-plant"){{

            requirements(Category.crafting, with(WHItems.titaniumSteel, 80, Items.plastanium, 60, Items.carbide, 40, WHItems.ceramite, 40));

            size = 2;
            craftTime = 60f;
            health = 550;
            hasPower = hasLiquids = true;
            liquidCapacity = 80;
            consumePower(4f);
            consumeLiquid(Liquids.nitrogen, 36 / 60f);
            consumeLiquid(Liquids.cryofluid, 18 / 60f);
            outputLiquid = new LiquidStack(WHLiquids.liquidNitrogen, 0.6f);
            drawer = new DrawMulti(new DrawRegion(){{
                suffix = "-bottom";
            }}, new DrawLiquidTile(){{
                drawLiquid = Liquids.cryofluid;
            }}, new DrawLiquidTile(){{
                drawLiquid = WHLiquids.liquidNitrogen;
            }},
            new DrawDefault());
            updateEffect = WHFx.square(Liquids.nitrogen.color, 20f, 4, 12, 5);
            researchCostMultiplier = 0.6f;
        }};

        tungstenConverter = new HeatCrafter("tungsten-converter"){{
            requirements(Category.crafting, with(WHItems.titaniumSteel, 50, Items.plastanium, 60, Items.phaseFabric, 20));

            size = 2;
            craftTime = 90f;
            health = 400;
            hasPower = hasItems = true;
            itemCapacity = 20;
            consumePower(2f);
            consumeItem(Items.thorium, 1);
            outputItem = new ItemStack(Items.tungsten, 1);
            drawer = new DrawMulti(new DrawDefault(),
            new DrawGlowRegion(){{
                alpha = 0.7f;
                color = Color.valueOf("F89661FF");
                glowIntensity = 0.3f;
                glowScale = 6f;
                rotateSpeed = 1.5f;

            }},
            new DrawHeatInput());
            heatRequirement = 8f;
            ambientSound = Sounds.smelter;
            updateEffect = WHFx.square(Items.tungsten.color, 20f, 4, 12, 5);
            researchCostMultiplier = 0.6f;
        }};
        //热量
        combustionHeater = new FlammabilityHeatProducer("combustion-heater"){
            {
                requirements(Category.crafting, with(WHItems.titaniumSteel, 50, Items.lead, 60, Items.silicon, 40));
                size = 2;
                craftTime = 45f;
                health = 200;
                hasItems = true;
                itemCapacity = 20;
                consume(new ConsumeItemFlammable(1));
                heatOutput = 1.5f;
                drawer = new DrawMulti(new DrawDefault(),
                new DrawHeatOutput(),
                new DrawHeatInput(){{
                    suffix = "-heat";
                }});
                ;
                drawer = new DrawMulti(new DrawDefault(),
                new DrawHeatOutput(),
                new DrawHeatInput(){{
                    suffix = "-heat";
                }});
                ambientSound = Sounds.hum;
                ambientSoundVolume = 0.002f;
                researchCostMultiplier = 0.6f;
            }
        };

        decayHeater = new HeatProducer("decay-heater"){
            {
                requirements(Category.crafting, with(WHItems.resonantCrystal, 50, Items.plastanium, 60, Items.carbide, 50));
                size = 3;
                craftTime = 240f;
                health = 1000;
                hasItems = true;
                itemCapacity = 20;
                consumeItem(Items.thorium, 2);
                heatOutput = 5f;
                drawer = new DrawMulti(new DrawDefault(),
                new DrawHeatOutput(),
                new DrawHeatInput(){{
                    suffix = "-heat";
                }});
                ambientSound = Sounds.smelter;
                ambientSoundVolume = 0.002f;
                researchCostMultiplier = 0.7f;
            }
        };

        slagHeatMaker = new HeatProducer("slag-heat-maker"){{
            requirements(Category.crafting, with(WHItems.titaniumSteel, 30, Items.graphite, 30, Items.silicon, 30, Items.tungsten, 30));

            size = 3;
            itemCapacity = 0;
            liquidCapacity = 120f;
            rotateDraw = false;
            regionRotated1 = 1;
            ambientSound = Sounds.hum;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.slag), new DrawDefault(), new DrawHeatOutput());
            consumeLiquid(Liquids.slag, 1);
            heatOutput = 8f;
        }};

        promethiumHeater = new HeatProducer("promethium-heater"){{
            requirements(Category.crafting, with(Items.thorium, 220, Items.silicon, 150, Items.carbide, 100, WHItems.ceramite, 100, WHItems.titaniumSteel, 100));

            size = 3;
            craftTime = 4f;
            health = 1000;
            hasItems = false;
            hasLiquids = hasPower = true;
            liquidCapacity = 80;
            consumeLiquid(WHLiquids.refinePromethium, 15 / 60f);
            heatOutput = 50f;
            drawer = new DrawMulti(new DrawRegion("-bottom"),
            new DrawLiquidTile(WHLiquids.refinePromethium),
            new DrawDefault(),
            new DrawHeatOutput(),
            new DrawHeatInput(){{
                suffix = "-heat";
            }});
            ambientSound = Sounds.hum;
            ambientSoundVolume = 0.002f;
            researchCostMultiplier = 0.8f;
        }};

        smallHeatRouter = new HeatConductor("small-heat-router"){
            {

                requirements(Category.distribution, with(Items.copper, 15, Items.graphite, 10, Items.titanium, 10));

                researchCostMultiplier = 2f;

                group = BlockGroup.heat;
                size = 1;
                drawer = new DrawMulti(new DrawDefault(), new DrawRegion("-glow"));
                regionRotated1 = 1;
                splitHeat = true;
            }

        };

        heatBelt = new HeatBelt("Heat-Belt"){
            {
                requirements(Category.distribution, with(Items.lead, 7, Items.graphite, 2, WHItems.titaniumSteel, 2));
                researchCostMultiplier = 10f;
                rotate = true;
                hasPower = false;
                group = BlockGroup.heat;
                size = 1;
                regionRotated1 = 1;
            }
        };

        heatBridge = new HeatDirectionBridge("heat-bridge"){

            {
                requirements(Category.distribution, with(Items.silicon, 10, Items.lead, 15, Items.graphite, 10));
                size = 1;
                range = 4;
                health = 100;
                lost = 0.03f;
                hasPower = false;
                researchCostMultiplier = 2f;
                regionRotated1 = 1;
                pulse = true;
            }
        };

        T2heatBridge = new HeatDirectionBridge("t2-heat-bridge"){
            {
                requirements(Category.distribution, with(WHItems.resonantCrystal, 10, Items.tungsten, 15, WHItems.titaniumSteel, 10));
                size = 1;
                range = 10;
                health = 200;
                lost = 0.01f;
                researchCostMultiplier = 2f;
                regionRotated1 = 1;
                consumePower(1f);
                hasPower = true;
                pulse = true;
            }
        };
//陶钢
        ceramiteSteelFoundry = new OverheatGenericCrafter("ceramite-steel-foundry"){
            {

                Color color = CeramiteColor;
                requirements(Category.crafting, with(Items.lead, 50, Items.silicon, 50, Items.tungsten, 25, Items.plastanium, 25));
                health = 300;
                hasItems = hasPower = true;
                craftTime = 60;
                itemCapacity = 20;
                size = 2;
                overloadThreshold = 0.6f;
                overloadEfficiency = 0.4f;
                consumePower(4);
                consumeItems(with(Items.tungsten, 4, Items.plastanium, 4));
                outputItem = new ItemStack(WHItems.ceramite, 2);
                drawer = new DrawMulti(new DrawDefault(), new DrawOverheatOutput(), new DrawFlame(color));
                craftEffect = WHFx.square(CeramiteColor, 35f, 4, 16f, 4f);
            }
        };

        ceramiteRefinery = new OverheatGenericCrafter("ceramite-refinery"){
            {
                Color color = WHPal.RefineCeramiteColor;
                requirements(Category.crafting, with(Items.surgeAlloy, 50, Items.carbide, 60, WHItems.ceramite, 50, Items.phaseFabric, 50));
                health = 1500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 30;
                size = 3;
                overloadThreshold = 0.5f;
                overloadEfficiency = 0.3f;
                wasteHeatOutput = 3f;
                consumePower(6);
                consumeItems(with(WHItems.molybdenumAlloy, 2, WHItems.ceramite, 3, Items.carbide, 2));
                consumeLiquid(WHLiquids.refinePromethium, 10 / 60f);
                outputItem = new ItemStack(WHItems.refineCeramite, 3);
                drawer = new DrawMulti(new DrawDefault(), new DrawOverheatOutput(), new DrawFlame(color), new DrawGlowRegion(){{
                    color = Liquids.slag.color;
                    glowScale = 12f;
                }});
                craftEffect = new RadialEffect(Fx.surgeCruciSmoke, 4, 90f, 32 / 4f){{
                    rotationOffset = 45F;
                }};
                researchCostMultiplier = 0.6f;
            }
        };

        ADMill = new
        OverheatGenericCrafter("admantium-mill"){
            {
                requirements(Category.crafting, with(Items.silicon, 200, WHItems.titaniumSteel, 50, WHItems.ceramite, 50, WHItems.refineCeramite, 30));

                hasItems = true;
                health = 600;
                size = 3;
                hasPower = true;
                hasLiquids = true;
                liquidCapacity = 40;
                itemCapacity = 20;
                craftTime = 120;
                overloadThreshold = 0.5f;
                overloadEfficiency = 0.5f;
                wasteHeatOutput = 3f;
                consumePower(6f);
                consumeItems(with(WHItems.vibranium, 6, WHItems.refineCeramite, 3));
                consumeLiquid(WHLiquids.liquidNitrogen, 0.3f);
                outputItem = new ItemStack(WHItems.adamantium, 2);
                drawer = new DrawMulti(new DrawDefault(), new DrawOverheatOutput(), new DrawFlame(Color.valueOf("FFEA96FF")), new AdmantiumMillDrawer(Items.surgeAlloy.color.cpy(), 7.5f));
                craftEffect = WHFx.hexagonSmoke(Items.surgeAlloy.color.cpy(), 45, 1f, 7.5f, 20f);
                researchCostMultiplier = 0.6f;
            }
        };
        //Drill
        MechanicalQuarry = new Quarry("mechanical-quarry"){{

            requirements(Category.production, with(Items.graphite, 50, Items.metaglass, 30, Items.silicon, 60));

            health = 300;
            size = 3;
            regionRotated1 = 1;
            itemCapacity = 100;
            acceptsItems = true;

            areaSize = 11;
            liquidBoostIntensity = 1.5f;
            mineTime = 400f;

            tier = 3;

            drawDrill = true;
            deploySpeed = 0.015f;
            deployInterp = new Interp.PowOut(4);
            deployInterpInverse = new Interp.PowIn(4);
            drillMoveSpeed = 0.07f;
            consumePower(2.5f);
            consumeLiquid(Liquids.water, 7.5f / 60f).boost();

        }};
        heavyCuttingDrill = new BurstDrill("heavy-cutting-drill"){
            {
                requirements(Category.production, with(Items.copper, 100, Items.lead, 100, Items.graphite, 100, Items.silicon, 200, WHItems.titaniumSteel, 100));

                health = 2900;
                size = 4;
                tier = 3;
                arrowOffset = 0;
                arrowSpacing = 0;
                arrows = 1;
                itemCapacity = 100;
                liquidCapacity = 50;
                glowColor = Color.valueOf("FEE984FF");
                fogRadius = 1;
                drawRim = true;
                hasItems = hasPower = hasLiquids = true;
                consumePower(7);
                consumeLiquid(Liquids.water, 15f / 60f);
                drillTime = 45;
                drillEffect = new MultiEffect(Fx.mineImpact, Fx.drillSteam, Fx.mineImpactWave.wrap(Pal.redLight, 40f));
                researchCostMultiplier = 0.6f;

            }
        };

        highEnergyDrill = new BurstDrill("high-energy-drill"){
            {
                requirements(Category.production, with(Items.tungsten, 300, Items.graphite, 300, Items.silicon, 500, WHItems.ceramite, 150, WHItems.refineCeramite, 100));

                health = 4000;
                size = 5;
                tier = 15;
                arrowOffset = 2;
                arrowSpacing = 5;
                arrows = 2;
                itemCapacity = 120;
                liquidCapacity = 80;
                glowColor = Items.surgeAlloy.color;
                fogRadius = 2;
                drawRim = true;
                hasItems = hasPower = hasLiquids = true;
                consumePower(15);
                consumeLiquid(Liquids.cryofluid, 0.3f);
                drillTime = 45;
                drillEffect = new MultiEffect(Fx.mineImpact, Fx.drillSteam,
                new WrapEffect(Fx.dynamicSpikes, Items.surgeAlloy.color, 30f),
                new WrapEffect(Fx.mineImpactWave, Items.surgeAlloy.color, 45f));
                researchCostMultiplier = 0.6f;

            }
        };

        SpecialCuttingDrill = new SpecialDrill("heavy-steel-laser-drill"){
            {
                requirements(Category.production, with(Items.tungsten, 100, Items.silicon, 200, Items.plastanium, 100, WHItems.ceramite, 100));

                health = 3200;
                drillTime = 600;
                size = 4;
                tier = 7;
                itemCapacity = 120;
                liquidCapacity = 80;
                mineOffset = -2;
                mineSize = 6;
                fogRadius = 2;
                drawRim = true;
                hasItems = hasPower = hasLiquids = true;
                consumePower(8);
                consumeLiquid(Liquids.cryofluid, 0.3f);
                allowedItems = Seq.with(
                Items.thorium,
                Items.tungsten,
                WHItems.molybdenum
                );

                drillTime = 80f;
                drillEffect = new MultiEffect(Fx.drillSteam);
                updateEffect = new Effect(180, 100, e -> {
                    float fadeTime = 60f;
                    float fout = Mathf.clamp(e.time > e.lifetime - fadeTime ?
                    1f - (e.time - (e.lifetime - fadeTime)) / fadeTime : 1f, 0, 1);
                    float fade = Interp.pow2Out.apply(fout) * e.fin(Interp.pow5In);
                    Draw.color(WHPal.SkyBlue.cpy());
                    Lines.stroke(fade * 10.0F);
                    Lines.square(e.x, e.y, 32 * e.fin(Interp.pow5In), 90f);
                });

                researchCostMultiplier = 0.6f;

            }
        };
        strengthenOilExtractor = new Fracker("strengthen-oil-extractor"){
            {
                requirements(Category.production, with(Items.silicon, 80, Items.graphite, 90, WHItems.titaniumSteel, 120, Items.plastanium, 60));
                health = 800;
                size = 3;
                floating = true;
                hasPower = true;
                hasLiquids = true;
                liquidCapacity = 60;
                requirements(Category.production, with(Items.silicon, 100, Items.graphite, 100, WHItems.titaniumSteel, 100, Items.plastanium, 100));
                health = 400;
                size = 3;
                floating = true;
                hasPower = true;
                hasLiquids = true;
                liquidCapacity = 60;
                pumpAmount = 0.15f;
                rotateSpeed = 1.3f;
                result = Liquids.oil;
                baseEfficiency = 2;
                attribute = Attribute.oil;
                consumeLiquid(Liquids.cryofluid, 0.3f);
                consumePower(3);
                researchCostMultiplier = 0.6f;
            }
        };

        slagExtractor = new AttributeCrafter("slag-extractor"){
            {
                requirements(Category.production, with(Items.thorium, 100, Items.graphite, 100, Items.silicon, 200, WHItems.titaniumSteel, 150));

                health = 450;
                size = 3;
                hasItems = hasPower = hasLiquids = true;
                liquidCapacity = 180;
                updateEffect = Fx.redgeneratespark;
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidRegion(Liquids.slag),
                new DrawRegion("-rotator", 8.6f * (1 - Mathf.absin(Time.time, 10, 0.3f)), true),
                new DrawDefault());
                consumePower(8);
                outputLiquid = new LiquidStack(Liquids.slag, 1f);
                craftTime = 60f;
                consumeItems(new ItemStack(WHItems.titaniumSteel, 1));
                baseEfficiency = 0.3f;
                maxBoost = 6;
                boostScale = 0.334f;
                minEfficiency = -1;
                attribute = Attribute.heat;
                researchCostMultiplier = 0.45f;
            }
        };

        promethiumExtractor = new Fracker("promethium-extractor"){
            {
                requirements(Category.production, with(Items.silicon, 90, Items.graphite, 120, WHItems.titaniumSteel, 90, Items.thorium, 80));

                health = 550;
                size = 3;
                hasPower = true;
                liquidCapacity = 60;
                pumpAmount = 15 / 60f;
                rotateSpeed = 1.3f;
                result = WHLiquids.orePromethium;
                attribute = Attribute.oil;
                consumePower(1.5f);
                itemUseTime = 60f;
                consumeItems(new ItemStack(WHItems.oreSand, 2));
                consumeLiquid(Liquids.water, 0.3f);
                updateEffect = Fx.pulverize;
                updateEffectChance = 0.05f;
                researchCostMultiplier = 0.36f;
                //抽钷机
            }
        };

        heavyExtractor = new EnhancedWaterExtractor("heavy-extractor"){

            {
                requirements(Category.production, with(Items.copper, 60, Items.graphite, 60, Items.silicon, 40, WHItems.titaniumSteel, 40));
                health = 400;
                size = 3;
                hasPower = true;
                hasLiquids = true;
                liquidCapacity = 50;
                itemCapacity = 10;
                pumpAmount = 0.3f;
                rotateSpeed = 1.3f;
                result = Liquids.water;
                attribute = Attribute.water;
                consumePower(7f);
                researchCostMultiplier = 0.5f;
                //硬化抽水机
            }
        };

        gravityPump = new Pump("gravity-pump"){
            {
                requirements(Category.liquid, with(Items.metaglass, 300, WHItems.titaniumSteel, 150, Items.plastanium, 130, Items.surgeAlloy, 50, WHItems.ceramite, 50));
                health = 1400;
                size = 4;
                drawer = new DrawMulti(new DrawPumpLiquid(), new DrawDefault());
                squareSprite = false;
                liquidCapacity = 800;
                hasLiquids = hasPower = true;
                pumpAmount = 0.36f;
                consumePower(12);
                researchCostMultiplier = 0.36f;
                //重力泵
            }
        };


        tiSteelPump = new Pump("titanium-steel-pump"){
            {
                requirements(Category.liquid, with(Items.copper, 50, WHItems.titaniumSteel, 30, Items.silicon, 50));
                health = 320;
                size = 2;
                drawer = new DrawMulti(new DrawPumpLiquid(), new DrawDefault());
                squareSprite = false;
                liquidCapacity = 120;
                hasLiquids = hasPower = true;
                pumpAmount = 0.32f;
                consumePower(1);
                researchCostMultiplier = 0.45f;
                //钛钢泵
            }
        };

        T2LiquidTank = new LiquidRouter("t2-titanium-steel-liquid-tank"){
            {
                requirements(Category.liquid, with(Items.lead, 150, WHItems.titaniumSteel, 100, Items.metaglass, 150, Items.plastanium, 100));
                health = 3000;
                size = 3;
                liquidCapacity = 7500;
                absorbLasers = true;
                researchCostMultiplier = 0.36f;
                //钛钢储液罐
            }
        };

        tiSteelBridgeConduit = new LiquidBridge("steel-bridge-conduit"){
            {
                requirements(Category.liquid, with(Items.lead, 20, Items.silicon, 25, WHItems.titaniumSteel, 10));
                health = 130;
                rotate = false;
                range = 8;
                liquidCapacity = 55f;
                arrowSpacing = 7;
                arrowOffset = 3.5f;
                arrowTimeScl = 12;
                bridgeWidth = 8;
                //钛钢导管桥
            }
        };

        tiSteelConduit = new Conduit("steel-conduit"){
            {
                requirements(Category.liquid, with(Items.titanium, 2, Items.metaglass, 3, WHItems.titaniumSteel, 1));
                health = 250;
                liquidCapacity = 55f;
                liquidPressure = 2.5f;
                bridgeReplacement = WHBlocks.tiSteelBridgeConduit;
                //钢导管
            }
        };

        T2BridgeConduit = new LiquidBridge("low-resistance-conduit"){
            {
                requirements(Category.liquid, with(Items.metaglass, 15, WHItems.resonantCrystal, 10, WHItems.titaniumSteel, 20, Items.phaseFabric, 10));
                health = 230;
                rotate = false;
                range = 18;
                pulse = true;
                liquidCapacity = 120;
                consumePower(0.5f);
                arrowSpacing = 7;
                arrowOffset = 3.5f;
                arrowTimeScl = 12;
                bridgeWidth = 8;
                //低阻导管桥
            }
        };

        wrapProjector = new RegenProjector("wrap-projector"){
            {
                requirements(Category.effect, with(Items.plastanium, 100, Items.silicon, 200, WHItems.titaniumSteel, 100, Items.carbide, 100, WHItems.sealedPromethium, 50));

                health = 1500;
                size = 3;
                armor = 6;
                canOverdrive = false;
                healPercent = 2 / 60f;
                squareSprite = true;
                baseColor = Pal.sapBullet;
                drawer = new DrawMulti(new DrawLiquidTile(Liquids.nitrogen), new DrawGlowRegion(){{
                    color = Pal.sapBullet;
                }}, new DrawPulseShape(){{
                    square = false;
                    color = Pal.sapBullet.cpy();
                }}, new DrawShape(){{
                    color = Pal.sapBullet.cpy();
                    sides = 4;
                    radius = 4f;
                    useWarmupRadius = true;
                }}, new DrawRegion("-cap"),
                new DrawDefault());
                effect = new MultiEffect(new ParticleEffect(){
                    {
                        particles = 3;
                        length = 12;
                        lifetime = 96;
                        sizeFrom = 4;
                        sizeTo = 0;
                        colorFrom = Pal.sapBullet.cpy().lerp(Pal.sapBulletBack, 0.5f);
                        colorTo = Pal.sapBullet.cpy();
                    }
                }
                );
                destroyBullet = WHBullets.warpBreak;
                range = 50;
                hasLiquids = true;
                consumePower(15);
                consumeLiquid(Liquids.nitrogen, 0.25f);
                researchCostMultiplier = 0.7f;
                //亚空间修复仪
            }
        };

        wrapOverdrive = new SelectOverdriveProjector("wrap-overdrive"){
            {
                requirements(Category.effect, with(Items.silicon, 250, WHItems.titaniumSteel, 250, Items.surgeAlloy, 150, Items.phaseFabric, 80, WHItems.sealedPromethium, 50));

                health = 100;
                size = 3;
                range = 300;
                phaseRangeBoost = 50;
                speedBoostPhase = 1;
                speedBoost = 1.7f;
                baseColor = Pal.sapBulletBack;
                phaseColor = Pal.sapBullet;
                hasBoost = true;
                useTime = 240;
                status = new StatusEffect[]{StatusEffects.overclock};
                boostStatus = new StatusEffect[]{WHStatusEffects.assault};
                squareSprite = false;
                consumePower(6);
                consumeLiquid(WHLiquids.refinePromethium, 0.1f);
                consumeItems(with(WHItems.sealedPromethium, 1)).boost();
                destroyBullet = WHBullets.warpBreak;
                researchCostMultiplier = 0.7f;
                //亚空间超速仪
                //通过亚空间能量扭曲部分时空，拉近现在与未来的帷幕来实现超速[red]摧毁会发生能量泄露
            }
        };

        armoredVault = new StorageBlock("armored-vault"){
            {
                requirements(Category.effect, with(Items.silicon, 2800, WHItems.titaniumSteel, 1500, Items.plastanium, 1000, WHItems.ceramite, 800));

                health = 2800;
                size = 3;
                itemCapacity = 30000;
                researchCostMultiplier = 0.6f;
                category = Category.effect;
                armor = 12;
                //装甲仓库
            }
        };
        T2RepairTower = new RepairTower("energy-repair-tower"){
            {
                requirements(Category.effect, with(Items.plastanium, 400, WHItems.titaniumSteel, 300, WHItems.ceramite, 300, WHItems.sealedPromethium, 200));

                health = 1500;
                size = 3;
                liquidCapacity = 200;
                range = 220;
                healAmount = 20;
                circleSpeed = 75;
                circleStroke = 8;
                squareRad = 8;
                squareSpinScl = 1.2f;
                glowMag = 0.3f;
                glowScl = 12f;
                consumePower(40);
                consumeLiquid(Liquids.ozone, 0.75f);
                researchCostMultiplier = 0.6f;
                //纳米修复塔
            }
        };

        fortlessShield = new BaseForceProjector("fortless-level-void-shield"){
            {
                requirements(Category.effect, with(Items.tungsten, 1500, WHItems.titaniumSteel, 1500, WHItems.ceramite, 650, WHItems.refineCeramite, 300));
                health = 8500;
                size = 5;
                radius = 300;
                sides = 24;
                canOverdrive = false;
                shieldHealth = 16000;
                phaseRadiusBoost = 0;
                phaseShieldBoost = 10000;
                cooldownNormal = 12;
                cooldownLiquid = 1.5f;
                cooldownBrokenBase = 12;
                coolantConsumption = 20 / 60f;
                liquidCapacity = 150;
                itemCapacity = 20;
                consumePower(40);
                researchCostMultiplier = 0.7f;
                //区块化虚空盾
            }
        };


        strongholdCore = new FrontlineCoreBlock("stronghold-core"){
            {
                requirements(Category.effect, buildVisibility = BuildVisibility.shown, with(Items.copper, 1000, Items.lead, 2000, Items.silicon, 1000, WHItems.titaniumSteel, 500));

                unitType = UnitTypes.evoke;
                armor = 22;
                health = 5000;
                itemCapacity = 2000;
                size = 4;
                unitCapModifier = 10;
                researchCostMultiplier = 0.7f;
                //据点核心

            }
        };

        T2strongholdCore = new FrontlineCoreBlock("large-stronghold-core"){
            {
                requirements(Category.effect, buildVisibility = BuildVisibility.shown, with(Items.thorium, 4300, Items.silicon, 8000, Items.plastanium, 3000, Items.surgeAlloy, 1500, WHItems.titaniumSteel, 5000, WHItems.ceramite, 800));
                unitType = UnitTypes.evoke;
                armor = 35;
                health = 30000;
                itemCapacity = 20000;
                size = 6;
                unitCapModifier = 10;
                researchCostMultiplier = 0.8f;
                //大型据点核心
            }
        };

        steelDust = new CoverdConveyor("steel-dust"){{

            requirements(Category.distribution, with(WHItems.titaniumSteel, 2, Items.lead, 1, Items.silicon, 2));

            health = 150;
            underBullets = true;
            hasShadow = true;
            placeableLiquid = true;
            size = 1;
            bridgeReplacement = WHBlocks.steelBridge;
            junctionReplacement = Blocks.invertedSorter;
            speed = 0.218f;
            displayedSpeed = 30;
            hasItems = true;
            itemCapacity = 2;
            researchCostMultiplier = 1;
            //钢质导轨
        }};

        steelBridge = new ItemBridge("steel-bridge-conveyor"){{
            {
                requirements(Category.distribution, with(Items.lead, 10, Items.silicon, 15, WHItems.titaniumSteel, 6, Items.metaglass, 10));

                health = 230;
                range = 8;
                transportTime = 3;
                arrowSpacing = 5;
                arrowOffset = 2.5f;
                arrowTimeScl = 12;
                bridgeWidth = 8;
                researchCostMultiplier = 1;
                //钢质带桥
            }
        }};

        T2steelBridge = new ItemBridge("low-resistance-bridge"){{
            {
                requirements(Category.distribution, with(WHItems.titaniumSteel, 20, WHItems.sealedPromethium, 15, Items.phaseFabric, 15));

                health = 400;
                range = 25;
                transportTime = 1.5f;
                arrowSpacing = 8;
                arrowOffset = 4f;
                arrowTimeScl = 12;
                bridgeWidth = 8;
                consumePower(0.5f);
                researchCostMultiplier = 1;
                //低阻带桥
            }
        }};

        steelUnloader = new UnloaderF("titanium-steel-unloader"){{

            requirements(Category.distribution, with(Items.lead, 20, Items.silicon, 15, WHItems.titaniumSteel, 30, Items.metaglass, 20));
            size = 1;
            update = true;
            hasItems = true;
            health = 300;
            speed = 1.5f;
            researchCostMultiplier = 1;
            //钢质卸载器
        }};


        ceramiteConveyor = new BetterStackConvyor("ceramite-conveyor"){{

            requirements(Category.distribution, with(Items.lead, 5, Items.silicon, 4, WHItems.titaniumSteel, 3, WHItems.ceramite, 2));

            health = 550;
            size = 1;
            update = true;
            hasItems = true;
            speed = 0.12f;
            itemCapacity = 35;
            researchCostMultiplier = 1;
            //陶钢打包带
        }};

        trackDriver = new MassDriver("track-driver"){
            {
                requirements(Category.distribution, with(Items.plastanium, 120, WHItems.titaniumSteel, 120, Items.carbide, 80, WHItems.ceramite, 50));

                health = 2800;
                size = 4;
                hasItems = true;
                itemCapacity = 300;
                minDistribute = 60;
                reload = 120;
                rotateSpeed = 2.5f;
                bulletSpeed = 8;
                shootEffect = Fx.instShoot;
                smokeEffect = WHFx.hugeSmokeGray;
                shootSound = Sounds.laser;
                range = 600;
                consumePower(13);
                researchCostMultiplier = 0.6f;
                //轨道驱动器
            }
        };

        ventDistiller = new ThermalGenerator("vent-distiller"){{
            requirements(Category.production, with(Items.graphite, 50, Items.silicon, 150, Items.plastanium, 50, WHItems.titaniumSteel, 40));

            attribute = Attribute.steam;
            group = BlockGroup.liquids;
            displayEfficiencyScale = 1f / 9f;
            minEfficiency = 9f - 0.0001f;
            powerProduction = (900f / 60f) / 9f;
            displayEfficiency = false;
            generateEffect = Fx.turbinegenerate;
            effectChance = 0.04f;
            size = 3;
            ambientSound = Sounds.hum;
            ambientSoundVolume = 0.06f;


            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.water), new DrawDefault(), new DrawBlurSpin("-rotator", 6f){{
                blurThresh = 0.01f;
            }},
            new DrawRegion("-top"));

            hasLiquids = true;
            outputLiquid = new LiquidStack(Liquids.water, 60f / 60f / 9f);
            liquidCapacity = 120f;
            fogRadius = 3;

        }};

        turboGenerator = new ConsumeGenerator("turbo-generator"){
            {
                requirements(Category.power, with(Items.thorium, 50, Items.metaglass, 100, Items.silicon, 250, WHItems.resonantCrystal, 30, WHItems.titaniumSteel, 50));
                size = 3;
                health = 500;
                hasItems = hasLiquids = true;
                itemDuration = 20f;
                consumeLiquid(Liquids.water, 0.3f);
                itemCapacity = 30;
                liquidCapacity = 150;
                powerProduction = 1100 / 60f;
                effectChance = 0.08f;
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.water),
                new DrawRegion("-rotator1", 0.6f * 9f){{
                    spinSprite = true;
                }},
                new DrawRegion("-rotator2", -0.6f * 3f){{
                    spinSprite = true;
                }},
                new DrawDefault());
                generateEffect = Fx.generatespark;
                consume(new ConsumeItemFlammable(1.2f));
                ambientSound = Sounds.smelter;
                ambientSoundVolume = 0.06f;
                researchCostMultiplier = 0.8f;
                //蒸汽发电机
            }
        };

        crackingGenerator = new ConsumeGenerator("cracking-generator"){
            {
                requirements(Category.power, with(Items.silicon, 100, Items.graphite, 100, Items.carbide, 20, WHItems.titaniumSteel, 70, WHItems.ceramite, 20));
                powerProduction = 2000 / 60f;

                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.oil),
                new DrawLiquidTile(Liquids.hydrogen),
                new DrawRegion("-mid"),
                new DrawPistons(){{
                    sinMag = 4.5f;
                    sinScl = 8f;
                    sides = 4;
                    angleOffset = 45f;
                    sideOffset = Mathf.PI / 2f;
                }},
                new DrawDefault(),
                new DrawGlowRegion(){{
                    suffix = "-heat";
                    alpha = 1f;
                    glowScale = 5f;
                    color = Pal.slagOrange;
                }});

                consumeLiquids(LiquidStack.with(Liquids.oil, 30f / 60f, Liquids.hydrogen, 30f / 60f));
                size = 3;

                liquidCapacity = 30f * 5;

                outputLiquid = new LiquidStack(Liquids.water, 30f / 60f);

                generateEffect = Fx.none;

                ambientSound = Sounds.smelter;
                ambientSoundVolume = 0.06f;

                researchCostMultiplier = 0.4f;
            }
        };

        T2thermalGenerator = new ThermalGenerator("t2-geothermal-generator"){
            {
                requirements(Category.power, with(Items.silicon, 120, WHItems.titaniumSteel, 70, Items.plastanium, 50, Items.tungsten, 50, WHItems.resonantCrystal, 50));

                size = 3;
                health = 1400;
                powerProduction = 4f;
                generateEffect = Fx.redgeneratespark;
                effectChance = 0.011f;
                drawer = new DrawMulti(
                new DrawDefault(),
                new DrawFade(){{
                    scale = 12f;
                }});
                researchCostMultiplier = 0.8f;
                //地热发电炉
            }
        };

        T2impactReactor = new ImpactReactor("detonation-reactor"){
            {
                requirements(Category.power, with(Items.lead, 2000, WHItems.titaniumSteel, 1500, Items.tungsten, 800, WHItems.vibranium, 600, WHItems.refineCeramite, 350));

                size = 4;
                health = 10000;
                liquidCapacity = 600;
                itemCapacity = 110;
                hasItems = true;
                hasLiquids = true;
                outputsPower = true;
                powerProduction = 750;
                itemDuration = 1.5f * 60f * 60f;
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawPlasma(){{
                    plasma1 = WHPal.SkyBlue;
                    plasma2 = WHPal.SkyBlueF;
                }},
                new DrawLiquidRegion(WHLiquids.refinePromethium),
                new DrawDefault()
                );
                consumePower(15);
                consumeItem(WHItems.sealedPromethium, 50);
                consumeLiquid(WHLiquids.refinePromethium, 10 / 60f);
                ambientSound = Sounds.pulse;
                ambientSoundVolume = 0.1f;
                researchCostMultiplier = 0.8f;
                //爆破放射反应堆
            }
        };


        plaRector = new PlaRector("plasma-reactor"){
            {
                requirements(Category.power, with(Items.silicon, 4000, Items.carbide, 2000, WHItems.titaniumSteel, 4000, WHItems.refineCeramite, 1600, WHItems.adamantium, 800, WHItems.sealedPromethium, 800));

                health = 20000;
                size = 5;
                ambientSound = Sounds.flux;
                ambientSoundVolume = 0.13f;
                effectChance = 0.05f;
                explosionMinWarmup = 0.8f;
                explosionRadius = 20;
                hasItems = true;
                hasLiquids = true;
                itemCapacity = 120;
                liquidCapacity = 1200;
                consumeLiquid(Liquids.cryofluid, 0.5f);
                consumeLiquid(WHLiquids.refinePromethium, 0.5f);
                powerProduction = 2000;
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(WHLiquids.refinePromethium),
                new DrawSoftParticles(){{
                    alpha = 0.35f;
                    particleRad = 16f;
                    particleSize = 7f;
                    particleLife = 120f;
                    particles = 15;
                    color = WHPal.SkyBlue;
                    color2 = WHPal.SkyBlueF;
                }},
                new DrawRegion("-mid"),
                new DrawDefault(),
                new DrawHeatInput(),
                new DrawGlowRegion("-ventglow"){{
                    color = Color.valueOf("32603a");
                }}
                );
                researchCostMultiplier = 0.6f;
                //等离子反应堆
                //直接利用地核中的等离子体用来发热，不冷却会发生剧烈爆炸
                //[red]地 核 抽 取 机
            }
        };

        promethiunmRector = new NuclearReactor("promethium-reactor"){
            {
                requirements(Category.power, with(Items.lead, 200, Items.silicon, 200, Items.carbide, 30, WHItems.titaniumSteel, 110, WHItems.resonantCrystal, 50, WHItems.molybdenumAlloy, 20));

                health = 3500;
                size = 3;
                liquidCapacity = 80;
                itemCapacity = 40;
                hasItems = hasLiquids = outputsPower = true;
                powerProduction = 4000 / 60f;
                itemDuration = 200;
                explosionRadius = 18;
                fuelItem = Items.thorium;
                heating = 0.01f;

                consumeItem(Items.thorium);
                consumeLiquid(WHLiquids.refinePromethium, 5 / 60f).update(false);

                explodeEffect = WHFx.promethiunmRectorExplosion;
                explodeSound = Sounds.explosionbig;
                researchCostMultiplier = 0.8f;
                //钷素反应堆
            }
        };

        Mbattery = new Battery("middle-battery"){

            {
                requirements(Category.power, with(Items.lead, 120, Items.silicon, 110, WHItems.titaniumSteel, 50));
                health = 250;
                size = 2;
                emptyLightColor = Color.valueOf("5757C1FF");
                fullLightColor = Color.valueOf("50D499FF");
                consumePowerBuffered(50000);
                baseExplosiveness = 2f;
                researchCostMultiplier = 0.8f;
                //小型储能点
                //优化原先大型电池结构，单位面积储存电量更大
            }
        };

        Lbattery = new Battery("large-battery"){
            {
                requirements(Category.power, with(Items.lead, 400, WHItems.titaniumSteel, 400, WHItems.refineCeramite, 50, WHItems.sealedPromethium, 30));
                health = 2900;
                size = 4;
                drawer = new DrawMulti(new DrawDefault(), new DrawPower(), new DrawRegion("-top"));
                consumePowerBuffered(635000);
                baseExplosiveness = 15f;
                researchCostMultiplier = 0.8f;
                //大型储能点
            }
        };

        MK3battery = new ShieldWall("MK3-reinforced-battery"){
            {

                requirements(Category.power, with(Items.silicon, 800, WHItems.titaniumSteel, 400, WHItems.refineCeramite, 400, WHItems.sealedPromethium, 200));

                health = 3800;
                size = 4;
                armor = 18;
                regenSpeed = 15;
                shieldHealth = 2500;
                breakCooldown = 1500;
                conductivePower = true;
                hasPower = true;
                outputsPower = true;
                consumesPower = true;
                canOverdrive = false;
                consumePowerBuffered(900000);
                baseExplosiveness = 10f;
                researchCostMultiplier = 0.8f;
                destroyBullet = WHBullets.warpBreak.copy();
                destroyBullet.hitColor = lightColor = lightningColor = WHPal.WHYellow;
                //MK3强化电池
            }
        };


        TiNode = new CompositePoweNode("titanium-steel-node"){
            {
                requirements(Category.power, with(Items.lead, 20, WHItems.titaniumSteel, 10, Items.silicon, 15));

                health = 400;
                size = 2;
                maxNodes = 16;
                laserRange = 18;
                laserScale = 0.4f;
                laserColor1 = Color.white;
                laserColor2 = WHPal.SkyBlue;
                researchCostMultiplier = 0.8f;

                //钢装甲节点
            }
        };


        T2TiNode = new CompositePoweNode("large-titanium-steel-node"){
            {
                requirements(Category.power, with(WHItems.titaniumSteel, 20, WHItems.resonantCrystal, 20, WHItems.molybdenumAlloy, 20));

                health = 850;
                size = 3;
                maxNodes = 5;
                laserRange = 10;
                laserScale = 0.6f;
                laserColor1 = Color.white;
                laserColor2 = WHPal.SkyBlue;
                schematicPriority = -15;

                researchCostMultiplier = 0.8f;
                //装甲电力塔
            }
        };

        selectProjector = new SelectForceProjector("select-projector"){{
            requirements(Category.effect, with(Items.silicon, 300, WHItems.resonantCrystal, 100, WHItems.refineCeramite, 100, WHItems.titaniumSteel, 500, WHItems.protocolChip, 100, WHItems.sealedPromethium, 100));
            size = 4;
            OneTileShieldHealth = 250f;
            cooldownNormal = 1.5f;
            cooldownLiquid = 1.2f;
            cooldownBrokenBase = 0.5f;
            range = 800f;

            itemConsumer = consumeItem(WHItems.sealedPromethium).boost();
            consumePower(100f);
        }};

        Crush = new ItemTurret("Crush"){{

            requirements(Category.turret, with(Items.copper, 80, Items.lead, 80));

            buildCostMultiplier = 8f;
            health = 800;
            size = 2;
            range = 180;
            reload = 20;
            maxAmmo = 30;
            inaccuracy = 6;
            xRand = 0.1f;
            heatColor = Pal.turretHeat;
            recoilTime = 10;
            recoil = 0.5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;
            shootCone = 30f;
            shootSound = Sounds.shootSnap;
            liquidCapacity = 25;
            coolantMultiplier = 4;
            ammoPerShot = 2;
            coolant = consumeCoolant(0.2f);
            drawer = new DrawTurret(WarHammerMod.name("turret-"));
            ammo(
            Items.lead, WHBullets.CrushBulletLead,
            Items.metaglass, WHBullets.CrushBulletMetaGlass,
            WHItems.titaniumSteel, WHBullets.CrushBulletTiSteel
            );
            alwaysUnlocked = true;
            researchCostMultiplier = 0.6f;
        }};

        AutoGun = new ItemTurret("Auto-gun"){{

            requirements(Category.turret, with(Items.copper, 80, Items.graphite, 50, Items.silicon, 50, WHItems.titaniumSteel, 50));

            buildCostMultiplier = 8f;
            health = 1200;
            size = 2;
            range = 240;
            reload = 130;
            ammoPerShot = 2;
            inaccuracy = 3;
            xRand = 0.1f;
            maxAmmo = 16;
            recoil = 1f;
            heatColor = WHPal.Heat;
            ammoUseEffect = Fx.casing3;
            shoot = new ShootAlternate(){{
                barrels = 2;
                spread = 7.5f;
                shots = 4;
                shotDelay = 10;
            }};
            shootSound = Sounds.shootBig;
            liquidCapacity = 25;
            coolantMultiplier = 4;
            coolant = consumeCoolant(0.2f);
            drawer = new DrawTurret(WarHammerMod.name("turret-"));
            ammo(
            Items.graphite, WHBullets.AutoGunGraphite,
            Items.silicon, WHBullets.AutoGunSilicon,
            Items.pyratite, WHBullets.AutoGunPyratite,
            WHItems.titaniumSteel, WHBullets.AutoGunTiSteel
            );
            alwaysUnlocked = true;
            researchCostMultiplier = 0.6f;
        }};

        Lcarus = new EnhancedPowerTurret("Lcarus"){{
            requirements(Category.turret, with(WHItems.titaniumSteel, 90, Items.silicon, 80, Items.metaglass, 80, Items.graphite, 60));

            buildCostMultiplier = 5f;
            health = 1800;
            size = 3;
            recoil = 2;
            liquidCapacity = 60;
            range = 280;
            shootCone = 20;

            shootSound = Sounds.laser;
            reload = 60f;
            drawer = new DrawTurret(WarHammerMod.name("turret-"));
            coolantMultiplier = 4;
            coolant = consumeCoolant(0.3f);
            consumePower(9f);
            shootType = WHBullets.LcarusBullet;
            ammoPerShot = 2;
            maxAmmo = 10;
            enhance(WHItems.resonantCrystal, WHBullets.LcarusBulletEnhanced, new ShootAlternate(){{
                barrels = 2;
                spread = 5f;
                shots = 2;
            }});
            researchCostMultiplier = 0.6f;

        }};

        SSWord = new ShootMatchTurret("S-sword"){{
            requirements(Category.turret, with(WHItems.titaniumSteel, 80, Items.silicon, 150, Items.plastanium, 80, Items.phaseFabric, 20));

            buildCostMultiplier = 5f;
            size = 3;
            health = 2500;
            range = 440;
            shootSound = Sounds.missile;
            reload = 180f;
            fogRadiusMultiplier = 0.35f;
            maxAmmo = 50;
            ammoPerShot = 4;
            recoil = 2;
            cooldownTime = 120;
            squareSprite = false;
            rotateSpeed = 2f;
            heatColor = Pal.turretHeat;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;
            drawer = new DrawTurret(WarHammerMod.name("turret-"));
            shoot = new ShootBarrel(){{
                shots = 6;
                shotDelay = 4;
                barrels = new float[]{
                -29 / 4f, 36 / 4f, 0,
                -10 / 4f, 42 / 4f, 0,
                29 / 4f, 36 / 8f, 0,
                10 / 4f, 42 / 4f, 0,
                };
            }};
            shootY = 0f;
            drawMinRange = true;
            minRange = 80;
            liquidCapacity = 30;
            coolantMultiplier = 4f;
            coolant = consumeCoolant(15 / 60f);
            inaccuracy = 9;
            shootCone = 30;
            velocityRnd = 0.03f;

            ammo(
            WHItems.titaniumSteel, WHBullets.SSWordTiSteel,
            Items.plastanium, WHBullets.SSWordPlastanium,
            Items.pyratite, WHBullets.SSWordPyratite,
            Items.surgeAlloy, WHBullets.SSWordSurgeAlloy
            );

            researchCostMultiplier = 0.5f;

        }};

        Shard = new ShootMatchTurret("Shard"){{
            requirements(Category.turret, with(Items.tungsten, 120, Items.plastanium, 60, WHItems.resonantCrystal, 25, WHItems.molybdenumAlloy, 25));

            buildCostMultiplier = 5f;
            health = 3000;
            armor = 5;
            size = 3;
            reload = 180f;
            range = 200f;
            maxAmmo = 20;
            ammoPerShot = 4;
            recoilTime = 30f;
            recoil = 3f;
            shootY = 9f;
            cooldownTime = 160f;
            heatColor = Pal.turretHeat;
            shootSound = Sounds.shotgun;
            ammoUseEffect = Fx.casing2Double;
            xRand = 0.2f;
            inaccuracy = 3f;
            rotateSpeed = 3f;
            velocityRnd = 0.1f;

            drawer = new DrawTurret(WarHammerMod.name("turret-")){{
                parts.add(new RegionPart("-part1"){{
                    mirror = false;
                    heatColor = Pal.turretHeat;
                    ;
                    progress = PartProgress.reload;
                    moveY = 1.5f;
                }});
            }};

            coolant = consumeCoolant(30 / 60f);
            coolantMultiplier = 3f;

            shoot = new ShootMulti(
            new ShootAlternate(8.6f),
            new ShootHelix(6, 0.5f){{
                shotDelay = 4;
                shots = 4;
                offset = Mathf.PI * 0.25f;
            }}
            );
            ammo(
            Items.tungsten, WHBullets.ShardTungsten,
            WHItems.molybdenumAlloy, WHBullets.ShardMolybdenumAlloy,
            WHItems.refineCeramite, WHBullets.ShardRefineCeramite);

            shooter(WHItems.molybdenumAlloy,
            new ShootMulti(new ShootAlternate(8.6f),
            new ShootSpread(8, 10f){{
                shotDelay = 4;
                firstShotDelay = 60f;
            }}));
            shooter(WHItems.refineCeramite,
            new ShootMulti(new ShootAlternate(8.6f),
            new ShootSpread(6, 8f)));

            researchCostMultiplier = 0.55f;

        }};

        Prevent = new ItemTurret("Prevent"){{
            requirements(Category.turret, with(Items.thorium, 50, WHItems.titaniumSteel, 100, Items.graphite, 150, WHItems.resonantCrystal, 20));

            buildCostMultiplier = 4f;
            health = 1800;
            armor = 5;
            size = 3;
            reload = 10f;
            range = 280f;
            maxAmmo = 40;
            shootY = 45 / 4f;
            shoot = new ShootAlternate(){{
                barrels = 2;
                spread = 32 / 4f;
            }};
            recoilTime = 25f;
            recoil = 2.88f;
            coolantMultiplier = 3f;
            liquidCapacity = 60f;
            shootSound = Sounds.shootBig;
            ammoUseEffect = Fx.casing3Double;
            squareSprite = false;
            inaccuracy = 2f;
            drawer = new DrawTurret(WarHammerMod.name("turret-"));

            coolant = consumeCoolant(15 / 60f);
            ammo(
            Items.pyratite, WHBullets.PreventPyratite,
            Items.thorium, WHBullets.PreventThorium,
            Items.tungsten, WHBullets.PreventTungsten,
            Items.carbide, WHBullets.PreventCarbide
            );
        }};

        Deflection = new BulletDefenseTurret("Deflection"){{

            requirements(Category.turret, with(WHItems.titaniumSteel, 500, Items.carbide, 200, WHItems.ceramite, 200, WHItems.refineCeramite, 100, WHItems.sealedPromethium, 50));

            buildCostMultiplier = 3f;
            size = 4;
            health = 5300;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;
            shootWarmupSpeed = 0.1f;
            minWarmup = 0.8f;
            warmupMaintainTime = 120f;
            drawer = new DrawTurret(WarHammerMod.name("turret-"));

            researchCostMultiplier = 0.6f;


        }};


        Flash = new SpeedupTurret("Flash"){
            {
                requirements(Category.turret, with(WHItems.titaniumSteel, 180, WHItems.resonantCrystal, 60, Items.carbide, 100, WHItems.ceramite, 150));
                buildCostMultiplier = 3f;
                armor = 5;
                health = 2500;
                outlineColor = WHPal.Outline;
                outlineRadius = 3;
                maxSpeedupScl = 1.5f;
                size = 4;
                inaccuracy = 3;
                recoil = 2;
                liquidCapacity = 60;
                canOverdrive = true;
                recoilTime = 60;
                reload = 55;
                rotateSpeed = 4;
                range = 320;
                xRand = 0.2f;
                shootY = 10;
                shootSound = Sounds.spark;
                heatColor = WHPal.Heat.cpy().lerp(WHPal.SkyBlue, 0.5f);

                cooldownTime = 150;
                velocityRnd = 0.1f;

                consumePower(900 / 60f);
                coolant = consumeCoolant(18 / 60f);

                drawer = new DrawMulti(new DrawTurret(WarHammerMod.name("turret-")){{
                    parts.add(new RegionPart("-side"){{
                        mirror = true;
                        layerOffset = -0.01f;
                        moveY = -0.3f;
                        moveX = 2f * 4f / 3f;
                        moveRot = -30;
                        heatColor = WHPal.Heat.cpy().lerp(WHPal.SkyBlue, 0.5f);
                        heatProgress = PartProgress.heat;
                    }});
                }});
                shootType = new ChainLightingBulletType(90f){
                    {
                        maxHit = 2;
                        chainRange = 50f;
                        maxRange = length = 320;
                        hitColor = lightningColor = Pal.lancerLaser;
                        lightningDamage = 50;
                        lightning = 3;
                        lightningLength = 6;
                        lightningLengthRand = 6;
                        hitEffect = WHFx.lightningSpark;
                    }
                };

                researchCostMultiplier = 0.5f;
            }
        };

        HeavyHammer = new ShootMatchTurret("Heavy-hammer"){{
            requirements(Category.turret, with(Items.silicon, 200, Items.plastanium, 100, Items.thorium, 300, WHItems.titaniumSteel, 120, WHItems.molybdenumAlloy, 50));

            buildCostMultiplier = 3f;
            health = 3500;
            size = 4;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;
            reload = 200;
            range = 380;
            recoil = 4;
            rotateSpeed = 1;
            targetAir = false;
            heatColor = WHPal.Heat;
            cooldownTime = 120;
            squareSprite = false;
            coolantMultiplier = 3.5f;
            ammoPerShot = 8;
            maxAmmo = 40;
            shootSound = Sounds.mediumCannon;

            shootWarmupSpeed = 0.07f;
            warmupMaintainTime = 120f;

            drawer = new DrawMulti(new DrawTurret(WarHammerMod.name("turret-")){{
                parts.add(new RegionPart("-side"){{
                    mirror = true;
                    layer = Layer.turret - 0.01f;
                    moveY = -0.5f;
                    moveX = 2f * 4f / 2f;
                    moveRot = -30;
                    heatColor = WHPal.Heat.cpy();
                    heatProgress = PartProgress.warmup;
                    progress = PartProgress.warmup;
                }});
                parts.add(new RegionPart("-back"){{
                    mirror = false;
                    moveY = -3f;
                    heatColor = WHPal.Heat.cpy();
                    progress = PartProgress.recoil.curve(Interp.pow2In);
                }});
            }});

            coolant = consumeCoolant(18 / 60f);
            coolantMultiplier = 3.75f;

            ammo(Items.thorium, WHBullets.HeavyHammerThorium,
            WHItems.molybdenumAlloy, WHBullets.HeavyHammerMolybdenumAlloy);

            researchCostMultiplier = 0.5f;
        }};

        Viper = new PowerTurret("Viper"){
            {
                requirements(Category.turret, with(WHItems.titaniumSteel, 100, WHItems.resonantCrystal, 100, WHItems.ceramite, 100, WHItems.molybdenumAlloy, 50));
                health = 3000;
                size = 4;
                buildCostMultiplier = 5f;
                outlineColor = WHPal.Outline;
                outlineRadius = 3;
                reload = 180;
                range = 300;
                recoil = 4;
                liquidCapacity = 100;
                coolantMultiplier = 2.5f;
                shootSound = Sounds.laser;
                heatColor = WHPal.TiSteelColor.cpy().lerp(WHPal.Heat, 0.5f);
                cooldownTime = 110;
                shootY = 14;
                consumePower(1500 / 60f);
                coolant = consumeCoolant(30 / 60f);
                drawer = new DrawMulti(new DrawTurret(WarHammerMod.name("turret-")){{
                    parts.add(new RegionPart("-light"){{
                        layer = 110;
                        colorTo = WHPal.SkyBlueF;
                        color = WHPal.TiSteelColor.cpy();
                        progress = PartProgress.heat;
                        outline = false;
                    }});
                }});
                shoot = new ShootMulti(new ShootAlternate(8),
                new ShootSpread(3, 3));
                shootType = new LightingLaserBulletType(){
                    {
                        damage = 180;
                        length = 300;
                        width = 15;
                        sideWidth = 2;
                        sideLength = 8;
                        pierce = true;
                        pierceCap = 5;
                        lightningColor = hitColor = WHPal.TiSteelColor;
                        shootEffect = WHFx.shootLine(6, 30);
                        colors = new Color[]{WHPal.SkyBlue.cpy().a(0.4f).lerp(Pal.techBlue.cpy(),0.3f),
                        WHPal.SkyBlue.cpy().a(0.6f).lerp(Pal.techBlue.cpy(),0.3f), WHPal.SkyBlueF.lerp(Pal.techBlue.cpy(),0.3f)};
                    }

                    @Override
                    public void hitEntity(Bullet b, Hitboxc entity, float health){
                        int hitCount = b.data instanceof Integer ? (int)b.data : 0;
                        float damageMultiplier = hitCount < 3 ? 1.5f : 1f;
                        b.damage *= damageMultiplier;

                        super.hitEntity(b, entity, health);

                        b.damage /= damageMultiplier;
                        b.data = hitCount + 1;
                    }
                };
                researchCostMultiplier = 0.5f;
            }
        };

        Vortex = new LiquidTurret("Vortex"){
            {
                requirements(Category.turret, with(Items.metaglass, 150, Items.plastanium, 100, WHItems.ceramite, 50, Items.tungsten, 100));

                size = 4;
                buildCostMultiplier = 5f;
                outlineColor = WHPal.Outline;
                outlineRadius = 3;
                reload = 2f;
                shoot.shots = 3;
                velocityRnd = 0.1f;
                inaccuracy = 4f;
                recoil = 1f;
                shootCone = 45f;
                liquidCapacity = 120f;
                shootEffect = Fx.shootLiquid;
                range = 290f;
                scaledHealth = 250;
                health = 3000;

                ammo(
                WHLiquids.swageWater, new LiquidBulletType(WHLiquids.swageWater){{
                    lifetime = 290/4f;
                    speed = 4f;
                    knockback = 1.7f;
                    puddleSize = 8f;
                    orbSize = 4f;
                    drag = 0.001f;
                    ammoMultiplier = 0.4f;
                    statusDuration = 60f * 4f;
                    damage = 0.2f;
                    layer = Layer.bullet - 2f;
                    status = StatusEffects.slow;
                }},
                Liquids.water, new LiquidBulletType(Liquids.water){{
                    lifetime = 290/4f;
                    speed = 4f;
                    knockback = 1.7f;
                    puddleSize = 8f;
                    orbSize = 4f;
                    drag = 0.001f;
                    ammoMultiplier = 0.4f;
                    statusDuration = 60f * 4f;
                    damage = 0.2f;
                    layer = Layer.bullet - 2f;
                }},
                Liquids.slag, new LiquidBulletType(Liquids.slag){{
                    lifetime = 290/4f;
                    speed = 4f;
                    knockback = 1.3f;
                    puddleSize = 8f;
                    orbSize = 4f;
                    damage = 4.75f;
                    drag = 0.001f;
                    ammoMultiplier = 0.4f;
                    statusDuration = 60f * 4f;
                }},
                Liquids.cryofluid, new LiquidBulletType(Liquids.cryofluid){{
                    lifetime = 290/4f;
                    speed = 4f;
                    knockback = 1.3f;
                    puddleSize = 8f;
                    orbSize = 4f;
                    drag = 0.001f;
                    ammoMultiplier = 0.4f;
                    statusDuration = 60f * 4f;
                    damage = 0.2f;
                }},
                Liquids.oil, new LiquidBulletType(Liquids.oil){{
                    lifetime = 290/4f;
                    speed = 4f;
                    knockback = 1.3f;
                    puddleSize = 8f;
                    orbSize = 4f;
                    drag = 0.001f;
                    ammoMultiplier = 0.4f;
                    statusDuration = 60f * 4f;
                    damage = 0.2f;
                    layer = Layer.bullet - 2f;
                }}
                );

                drawer = new DrawMulti(new DrawTurret(WarHammerMod.name("turret-")));
                flags = EnumSet.of(BlockFlag.turret, BlockFlag.extinguisher);
            }
        };

        Ionize = new ShootMatchTurret("Ionize"){{

            requirements(Category.turret, with( Items.tungsten, 150, Items.phaseFabric,100,Items.carbide, 80,WHItems.ceramite,80,WHItems.molybdenumAlloy,90));

            size = 4;
            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;
            shoot.firstShotDelay=120;
            reload = 360f;
            range = 400f;
            recoil = 4;
            liquidCapacity = 100;
            coolantMultiplier = 3.5f;
            shootSound = WHSounds.hugeShoot;
            chargeSound = Sounds.lasercharge;
            rotateSpeed=2.5f;
            cooldownTime = 110;
            shootY = 57/4f;
            consumePower(1500 / 60f);
            coolant = consumeCoolant(30 / 60f);
            maxAmmo=6;
            ammoPerShot=4;
            drawer = new DrawMulti(new DrawTurret(WarHammerMod.name("turret-")){{
                parts.add(new RegionPart("-mid"){{
                    heatColor = WHPal.SkyBlueF.cpy();
                    layerOffset = -0.01f;
                }});
            }});
            ammo(
            Items.phaseFabric, WHBullets.IonizePhaseFabricBullet,
            WHItems.resonantCrystal,WHBullets.IonizeResonantCrystalBullet);

        }};

        Pyros=new HeatTurret("Pyros"){
            {
                requirements(Category.turret, with(WHItems.titaniumSteel, 150,WHItems.ceramite,80, WHItems.resonantCrystal, 80, WHItems.molybdenumAlloy, 80));
                buildCostMultiplier = 5f;
                outlineColor = WHPal.Outline;
                outlineRadius = 3;
                armor = 4;health = 4000;
                size = 4;
                unitSort = UnitSorts.strongest;
                inaccuracy = 0;
                recoil = 5;
                liquidCapacity = 100;
                recoilTime = 60;
                reload = 240;
                rotateSpeed = 2f;
                range = 400;
                coolantMultiplier = 1.8f;
                shootSound = Sounds.laser;
                heatColor = WHPal.Heat;
                cooldownTime = 110;
                shootY = 12;
                velocityRnd = 0.1f;

                drawer = new DrawMulti(new DrawTurret(WarHammerMod.name("turret-")));
                shootType=WHBullets.air5Missile;
            }
        };

        //Test

        UnitCallBlock airDrop = new UnitCallBlock("air-drop-unit"){
            {
                addSets(
                new UnitSet(UnitTypes.poly, new byte[]{WHUnitTypes.OTHERS, 2}, 45 * 60f, false,
                with(Items.lead, 30, Items.copper, 60, Items.graphite, 45, Items.silicon, 30)
                ),
                new UnitSet(WHUnitTypes.tank1, new byte[]{WHUnitTypes.AIR_LINE_2, 1}, 15 * 60f, true,
                with(Items.silicon, 16, Items.copper, 30)
                ),
                new UnitSet(WHUnitTypes.tank1s, new byte[]{WHUnitTypes.AIR_LINE_1, 1}, 15 * 60f, true,
                with(Items.titanium, 30, Items.silicon, 15)
                ),
                new UnitSet(WHUnitTypes.air4, new byte[]{WHUnitTypes.AIR_LINE_1, 2}, 30 * 60f, false,
                with(Items.titanium, 60, Items.silicon, 45, Items.graphite, 30)
                ),
                new UnitSet(WHUnitTypes.air5, new byte[]{WHUnitTypes.GROUND_LINE_1, 1}, 20 * 60f, false,
                with(Items.lead, 15, Items.silicon, 10, Items.copper, 10)
                ),
                new UnitSet(UnitTypes.antumbra, new byte[]{WHUnitTypes.GROUND_LINE_1, 2}, 35 * 60f, false,
                with(Items.lead, 30, Items.titanium, 60, Items.graphite, 45, Items.silicon, 30)
                ));
            }
        };

        UnitCallBlock2 airDrop2 = new UnitCallBlock2("air-drop-unit2"){
            {
                requirements(Category.units, with(Items.lead, 100, Items.titanium, 75, Items.silicon, 125));
                plans = Seq.with(
                new UnitPlan(UnitTypes.poly, 10 * 60, false,
                with(Items.lead, 30, Items.copper, 60, Items.graphite, 45, Items.silicon, 30)
                ),
                new UnitPlan(UnitTypes.flare, 10 * 60, false,
                with(Items.lead, 30, Items.copper, 60, Items.graphite, 45, Items.silicon, 30)
                ),
                new UnitPlan(UnitTypes.anthicus, 10 * 60, false,
                with(Items.lead, 30, Items.copper, 60, Items.graphite, 45, Items.silicon, 30)
                ),
                new UnitPlan(UnitTypes.sei, 10 * 60, false,
                with(Items.lead, 30, Items.copper, 60, Items.graphite, 45, Items.silicon, 30)
                ),
                new UnitPlan(UnitTypes.omura, 10 * 60, false,
                with(Items.lead, 30, Items.copper, 60, Items.graphite, 45, Items.silicon, 30)
                )
                );

                consumePower(100f);
            }
        };



        AirRaiderCallBlock test = new AirRaiderCallBlock("tactical-command-center"){{
            requirements(Category.turret, with(WHItems.titaniumSteel, 500, Items.carbide, 200, WHItems.ceramite, 200, WHItems.refineCeramite, 100, WHItems.sealedPromethium, 50));

            size = 4;

            consumePower(100f);
        }};

        AirRaider airRaider = new AirRaider("air-raider"){{
            requirements(Category.turret, with(WHItems.titaniumSteel, 500, Items.carbide, 200, WHItems.ceramite, 200, WHItems.refineCeramite, 100, WHItems.sealedPromethium, 50));

            shoot = new ShootSummon(0, 0, 120, 0){{
                shots = 4;
                shotDelay = 8f;
            }};

            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawCrucibleFlame(){{
                alpha = 0.375f;
                particles = 20;
                particleSize = 2.6f;
                particleRad = 7f;
                flameColor = WHPal.OR;
                midColor = WHPal.ORL.cpy().lerp(Color.white, 0.1f);
            }}, new DrawDefault());

            size = 3;
            consumePowerCond(6f, AirRaiderBuild::isCharging);
            consumeItem(WHItems.sealedPromethium, 4);
            itemCapacity = 16;
            health = 4500;

            triggeredEffect = new Effect(45f, e -> {
                Draw.color(WHPal.OR);
                Lines.stroke(e.fout() * 2f);
                Lines.square(e.x, e.y, size * tilesize / 2f + tilesize * 1.5f * e.fin(Interp.pow2In));
            });

            bullet = WHBullets.airRaiderMissile;
        }};

        TestShaderBlock testShaderBlock = new TestShaderBlock("test-shader-block"){{
            requirements(Category.effect, with(Items.lead, 100, Items.titanium, 75, Items.silicon, 125));
            size = 3;
            phaseRadiusBoost = 80f;
            radius = 101.7f;
            shieldHealth = 750f;
            cooldownNormal = 1.5f;
            cooldownLiquid = 1.2f;
            cooldownBrokenBase = 0.35f;

            itemConsumer = consumeItem(Items.phaseFabric).boost();
            consumePower(4f);
        }};


        sb6 = new PowerTurret("lancer"){{
            requirements(Category.turret, with(Items.copper, 60, Items.lead, 70, Items.silicon, 60, Items.titanium, 30));
            range = 165f;

            shoot.firstShotDelay = 40f;

            recoil = 2f;
            reload = 80f;
            shake = 2f;
            shootEffect = Fx.lancerLaserShoot;
            smokeEffect = Fx.none;
            heatColor = Color.red;
            size = 2;
            scaledHealth = 280;
            targetAir = false;
            moveWhileCharging = false;
            accurateDelay = false;
            shootSound = Sounds.laser;
            coolant = consumeCoolant(0.2f);

            consumePower(6f);

            shootType = new LaserBulletType(140){{
                colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
                //TODO merge
                chargeEffect = new MultiEffect(WHFx.TrailCharge(Pal.accent, 20f, 2.5f, 30, 3, 120));

                buildingDamageMultiplier = 0.25f;
                hitEffect = Fx.hitLancer;
                hitSize = 4;
                lifetime = 16f;
                drawSize = 400f;
                collidesAir = false;
                length = 173f;
                ammoMultiplier = 1f;
                pierceCap = 4;
            }};
        }};
        sb7 = new MultipleConsumerReconstructor("草泥马reconstructor"){{
            requirements(Category.units, with(Items.copper, 200, Items.lead, 120, Items.silicon, 90));

            size = 3;
            consumePower(3f);

            constructTime = 60f * 10f;

            addUpgrade(UnitTypes.dagger, UnitTypes.mace,
            new ItemStack(Items.silicon, 10),
            new ItemStack(Items.metaglass, 20)
            );

            addUpgrade(UnitTypes.mace, UnitTypes.fortress,
            new ItemStack(Items.silicon, 20),
            new ItemStack(Items.titanium, 30)
            );

            addUpgrade(UnitTypes.flare, UnitTypes.horizon,
            new ItemStack(Items.silicon, 20),
            new ItemStack(Items.lead, 30)
            );

            addUpgrade(UnitTypes.mono, UnitTypes.poly,
            new ItemStack(Items.silicon, 20),
            new ItemStack(Items.graphite, 30)
            );
        }};

        sb7 = new OverheatGenericCrafter("过热工厂"){{
            requirements(Category.crafting, with(Items.copper, 200, Items.lead, 120, Items.silicon, 90));

            craftEffect = Fx.pulverizeMedium;
            outputItem = new ItemStack(Items.graphite, 1);
            craftTime = 90f;
            size = 2;
            proximityRange = 2;
            hasItems = true;
            placeEffect = Fx.rotateBlock;

            consumeItem(Items.coal, 2);

        }};

        sb10 = new OverheatBooster("过热助推器"){
            {
                requirements(Category.crafting, with(Items.copper, 200, Items.lead, 120, Items.silicon, 90));
                size = 2;
                dymamicHeat = 2;
                heatReduceMax = 1f;
                hasItems = true;
                placeEffect = Fx.rotateBlock;
                consumeLiquid(Liquids.cryofluid, 0.5f);
                heatRequirement = 15f;
                boostRange = 20f;

            }
        };

        Collapse = new

        ItemTurret("Collapse"){
            {
                ammo(WHItems.sealedPromethium, WHBullets.collapseSp, Items.phaseFabric, WHBullets.collaspsePf);
            }
        }
        ;


        sb3 = new

        PowerTurret("激光"){
            {
                requirements(Category.turret, with(Items.copper, 1200, Items.lead, 350, Items.graphite, 300, Items.surgeAlloy, 325, Items.silicon, 325));
                shootEffect = Fx.shootBigSmoke2;
                shootCone = 40f;
                recoil = 4f;
                size = 4;
                shake = 2f;
                range = 500f;
                reload = 500f;
                shootSound = Sounds.laserbig;
                loopSound = Sounds.beam;
                loopSoundVolume = 2f;
                envEnabled |= Env.space;

                shootType = new DoomLaserBulletType(){{
                    splashDamage = damage = 1000f;
                    splashDamageRadius = width;
                    lifetime = 60f * 6f;
                    sideLength = 100f;
                    trailLength = 120;
                    width = 13f;
                    oscScl = 12f;
                    oscMag = 0.4f;
                }};

                scaledHealth = 200;
                coolant = consumeCoolant(0.5f);
                consumePower(17f);
            }
        }

        ;
        sb4 = new

        LaserBeamTurret("laserBeam"){
            {
                requirements(Category.turret, with(Items.copper, 1200, Items.lead, 350, Items.graphite, 300, Items.surgeAlloy, 325, Items.silicon, 325));
                shootEffect = Fx.shootBigSmoke2;
                shootCone = 40f;
                recoil = 4f;
                size = 4;
                shake = 2f;
                range = 300f;
                reload = 500f;
                coolantMultiplier=5f;
                shootSound = Sounds.laserbig;
                loopSound = Sounds.beam;
                loopSoundVolume = 2f;
                envEnabled |= Env.space;

                shootType = new LaserBeamBulletType(100){{
                    colors = new Color[]{Pal.slagOrange.cpy().a(0.4f), Pal.slagOrange, Color.white};
                    lightColor = lightningColor = Pal.slagOrange;
                    width = 15f;
                    shootDuration=lifetime = 300f;
                    length = 300f;
                    hitColor = Pal.slagOrange;
                    drawPositionLighting = false;
                }};
                scaledHealth = 200;
                coolant = consumeCoolant(0.5f);
                consumePower(17f);
            }

        };

        sb5 = new

        LaserTurret("AccelLaser"){
            {
                requirements(Category.turret, with(Items.copper, 1200, Items.lead, 350, Items.graphite, 300, Items.surgeAlloy, 325, Items.silicon, 325));
                shootEffect = Fx.shootBigSmoke2;
                shootCone = 40f;
                recoil = 4f;
                size = 4;
                shake = 2f;
                range = 300f;
                reload = 500f;
                coolantMultiplier=2f;
                shootSound = Sounds.laserbig;
                loopSound = Sounds.beam;
                loopSoundVolume = 2f;
                envEnabled |= Env.space;

                shootType = new AcceleratingLaserBulletType(100){{
                    colors = new Color[]{Pal.heal.cpy().a(0.4f), Pal.heal, Color.white};
                    lightColor = lightningColor = Pal.heal;
                    width = 15f;
                    maxLength = 500f;
                    hitColor = Pal.heal;
                }};
                scaledHealth = 200;
                coolant = consumeCoolant(0.5f);
                consumePower(17f);
            }
        };


        sb6 = new

        PowerTurret("laser3"){
            {
                requirements(Category.turret, with(Items.copper, 1200, Items.lead, 350, Items.graphite, 300, Items.surgeAlloy, 325, Items.silicon, 325));
                shootEffect = WHFx.circleLightning(Pal.heal.cpy(), 180, 15, 15, 100);
                shootCone = 40f;
                recoil = 4f;
                size = 4;
                shake = 2f;
                range = 350;
                reload = 180;
                shootSound = Sounds.laserbig;
                loopSound = Sounds.beam;
                loopSoundVolume = 2f;
                envEnabled |= Env.space;
                shootType = new BasicBulletType(){
                    {
                        shootEffect = new MultiEffect(Fx.shootTitan, new WaveEffect(){{
                            colorTo = Pal.sapBulletBack;
                            sizeTo = 26f;
                            lifetime = 14f;
                            strokeFrom = 4f;
                        }});
                        smokeEffect = Fx.shootSmokeTitan;
                        hitColor = Pal.sapBullet;
                        despawnSound = Sounds.spark;

                        sprite = "large-orb";
                        trailEffect = Fx.missileTrail;
                        trailInterval = 3f;
                        trailParam = 4f;
                        speed = 3f;
                        damage = 75f;
                        lifetime = 60f;
                        width = height = 15f;
                        backColor = Pal.sapBulletBack;
                        frontColor = Pal.sapBullet;
                        shrinkX = shrinkY = 0f;
                        trailColor = Pal.sapBulletBack;
                        trailLength = 12;
                        trailWidth = 2.2f;
                        despawnEffect = hitEffect = new ExplosionEffect(){{
                            waveColor = Pal.sapBullet;
                            smokeColor = Color.gray;
                            sparkColor = Pal.sap;
                            waveStroke = 4f;
                            waveRad = 40f;
                        }};
                        pierceCap = 3;

                        lightningColor = Pal.sapBullet;
                        lightningDamage = 17;
                        lightning = 8;
                        lightningLength = 2;
                        lightningLengthRand = 8;

                        fragBullets = 1;
                        fragBullet = new LightningChainBulletType(){{
                            speed = 0f;
                            damage = 75f;
                            lifetime = 150f;
                            width = height = 0;
                            shrinkX = shrinkY = 0f;
                            trailColor = Pal.sapBulletBack;
                            collidesAir = collidesGround = collides = false;
                            hitColor = chainColor = Pal.sapBullet;

                            fragBullets = 4;
                            fragVelocityMax = 1;
                            fragVelocityMin = 1;
                            fragBullet = new BasicBulletType(8f, 80){{
                                drag = 0.08f;
                                hitSize = 5;
                                width = 16f;
                                lifetime = 150f;
                                height = 23f;
                                shootEffect = Fx.shootBig;
                                pierceCap = 2;
                                pierceBuilding = true;
                                knockback = 0.7f;

                                backColor = hitColor = trailColor = Pal.thoriumAmmoBack;
                                frontColor = Pal.thoriumAmmoFront;
                            }};
                        }};
                        scaledHealth = 200;
                        coolant = consumeCoolant(0.5f);
                        consumePower(17f);
                    }
                };
            }
        };

    }
}