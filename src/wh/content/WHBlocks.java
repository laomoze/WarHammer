//
package wh.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.DrawPart.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import wh.core.*;
import wh.entities.bullet.laser.*;
import wh.entities.world.blocks.defense.*;
import wh.entities.world.blocks.defense.turrets.*;
import wh.entities.world.blocks.distribution.*;
import wh.entities.world.blocks.effect.*;
import wh.entities.world.blocks.others.*;
import wh.entities.world.blocks.production.*;
import wh.entities.world.blocks.storage.*;
import wh.entities.world.blocks.unit.*;
import wh.entities.world.drawer.factory.*;
import wh.entities.world.drawer.part.*;
import wh.graphics.*;
import wh.ui.*;

import static arc.graphics.g2d.Draw.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;
import static mindustry.gen.Sounds.*;
import static mindustry.type.ItemStack.with;
import static wh.content.WHFx.*;
import static wh.graphics.Drawn.arcProcessFlip;
import static wh.graphics.WHPal.*;
import static wh.util.WHUtils.rand;

public final class WHBlocks{
    public static Block promethium;
    public static Block vibraniumOre;
    //factory
    public static Block
    siliconMixFurnace, atmosphericSeparator, largeKiln, T2PlastaniumCompressor, T2Electrolyzer, T2SporePress,
    t2MultiPress, T2CarbideCrucible, T2Cultivator, T2CryofluidMixer, T2PhaseSynthesizer, largeSurgeSmelter,

    titaniumSteelFurnace, T2TiSteelFurnace,
    ceramiteSteelFoundry, T2ceramiteSteelFoundry, crystalEngraver, laserEngraver,
    moSurgeSmelter, ceramiteRefinery, ADMill,
    //promethium
    promethiumRefinery, sealedPromethiumMill, LiquidNitrogenPlant, slagfurnace,

    scrapCrusher, scrapFurance, sandSeparator,
    heatSiliconSmelter, waterPurifier, T2WaterPurifier,
    pyratiteBlender, pyratiteSeparator,
    //heat
    combustionHeater, decayHeater, slagHeatMaker, promethiumHeater, smallHeatRouter, heatBelt, heatBridge, T2heatBridge,
    tungstenConverter, molybdenumConverter, vibraniumConverter;
    //drill
    public static Block electronicPneumaticDrill, MechanicalQuarry, heavyCuttingDrill, highEnergyDrill, SpecialCuttingDrill,
    strengthenOilExtractor, integratedCompressor, slagExtractor, promethiumExtractor, heavyExtractor;
    //liquid
    public static Block gravityPump, tiSteelPump, T2LiquidTank, armorLiquidTank, armorFluidJunction,
    armorFluidRouter, mixedFluidJunction, steelConduit, lightConduit,
    tiSteelBridgeConduit, T2BridgeConduit;
    //effect
    public static Block wrapProjector, wrapOverdrive, shelterDome, armoredVault,
    T2RepairTower, fortlessShield, strongholdCore, T1strongholdCore, T2strongholdCore, selectProjector;
    //distribution
    public static Block armorInvertedSorter, armorSorter, armorJunction, armorOverflowGate,
    armorUnderflowGate, armorRouter, steelDust,
    steelBridge, T2steelBridge,
    ceramiteConveyor, armorCoverStackBelt, stackBridge, steelUnloader, trackDriver;
    //power
    public static Block ventDistiller, turboGenerator, crackingGenerator, T2thermalGenerator,
    T2impactReactor, plaRector, promethiunmRector,
    Mbattery, Lbattery, MK3battery, TiNode, T2TiNode, SmallTiNode;

    //units
    public static Block airFactory, groundFactory, mechaFactory, tankFactory,
    t2Module, t3Module, t4Module, t5Module, t6Module, jumpBeacon, energyWarpGate, serpuloT6Assembler,
    t2PayloadMassDriver;
    //turrets
    public static Block
    //22
    Crush, AutoGun, Blaze,
    //33
    Lcarus, SSWord, Shard, Prevent, Deflection, Blade,
    //44
    Pyros, Ionize, Vortex, Viper, HeavyHammer, Flash, ArtilleryBeacon,
    //55
    RoaringFlame, Collapse, Colossus, CycloneMissleLauncher, Crumble, Sacrament,
    //66
    Hydra, Erase, Annihilate, Melta, Reckoning,
    //88,88+
    Hector, Mezoa;

    //TEST
    public static Block randomer, sb, sb3, sb4, sb5, sb6, sb7, sb8, sb9, sb10;


    private WHBlocks(){
    }

    public static void load(){
        promethium = new Floor("promethium");
        vibraniumOre = new OreBlock("vibranium-ore");

        atmosphericSeparator = new HeatCrafter("atmospheric-separator"){
            {

                requirements(Category.crafting, with(Items.graphite, 50, Items.silicon, 100, WHItems.titaniumSteel, 70));

                hasItems = false;
                hasPower = hasLiquids = true;
                size = 3;
                health = 900;
                craftTime = 60;
                liquidCapacity = 100;
                updateEffect = Fx.none;
                ambientSound = Sounds.loopExtract;
                ambientSoundVolume = 0.06f;
                consumePower(1.5f);
                heatRequirement = 10;
                maxEfficiency = 2;
                outputLiquid = new LiquidStack(Liquids.nitrogen, 20f / 60f);
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

        siliconMixFurnace = new GenericCrafter("silicon-mix-furnace"){
            {
                requirements(Category.crafting, with(Items.silicon, 80, Items.graphite, 100, Items.silicon, 50, WHItems.titaniumSteel, 50));

                size = 3;
                health = 900;
                craftTime = 90;
                itemCapacity = 60;
                hasPower = hasItems = true;
                consumePower(2f);
                outputItem = new ItemStack(Items.silicon, 8);
                consumeItems(with(Items.sand, 12, Items.graphite, 5));
                /*      consumeLiquid(Liquids.water, 0.1f);*/
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(){{
                    circleStroke = 1.5f;
                    flameRadiusScl = 2.5f;
                    flameRadiusMag = 0.2f;
                }}, new DrawDefault(),
                new DrawHeatOutput());
                ambientSound = loopSmelter;
                ambientSoundVolume = 0.11f;
                researchCostMultiplier = 0.5f;

            }
        };

        heatSiliconSmelter = new HeatCrafter("heat-silicon-smelter"){{
            requirements(Category.crafting, with(WHItems.molybdenumAlloy, 50, Items.carbide, 120, Items.pyratite, 30, Items.silicon, 200, WHItems.titaniumSteel, 100));
            size = 4;
            health = 2000;
            hasPower = hasItems = true;
            itemCapacity = 60;
            craftTime = 60;
            consumePower(1200 / 60f);
            consumeItems(with(Items.pyratite, 3, Items.graphite, 5, Items.metaglass, 5));
            outputItem = new ItemStack(Items.silicon, 18);
            maxEfficiency = 2f;
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

            requirements(Category.crafting, with(Items.tungsten, 100, Items.graphite, 80, WHItems.titaniumSteel, 40));
            size = 3;
            health = 1200;
            itemCapacity = 45;
            outputItem = new ItemStack(Items.carbide, 2);
            craftTime = 180;
            heatRequirement = 8;
            maxEfficiency = 3f;
            hasItems = hasPower = true;
            consumePower(300 / 60f);
            consumeItems(with(Items.graphite, 5, Items.tungsten, 3));
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawCrucibleFlame(), new DrawDefault(), new DrawHeatInput());
            researchCostMultiplier = 0.45f;
        }};

        largeKiln = new GenericCrafter("large-kiln"){
            {
                requirements(Category.crafting, with(Items.lead, 60, Items.graphite, 60, WHItems.titaniumSteel, 50));

                size = 3;
                health = 360;
                itemCapacity = 40;
                hasPower = hasItems = true;
                craftTime = 90;


                consumePower(180f / 60f);
                consumeItems(with(Items.sand, 5, Items.lead, 5));
                outputItem = new ItemStack(Items.metaglass, 8);
                drawer = new DrawMulti(new DrawDefault(), new DrawFlame(), new DrawGlowRegion(){{
                    color = Color.valueOf("ffc999");
                }}, new LargekilnDrawer(Color.valueOf("ffc999")));

                ambientSound = loopSmelter;
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
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault(), new T2PlastaniumCompresserDrawer(WHPal.ShootOrange), new DrawFade());
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

            requirements(Category.crafting, with(WHItems.titaniumSteel, 400, WHItems.resonantCrystal, 100, WHItems.protocolChip, 100, WHItems.refineCeramite, 100));
            size = 3;
            health = 850;
            hasPower = hasLiquids = hasItems = true;
            craftTime = 60;
            liquidCapacity = 300;
            itemCapacity = 30;
            consumePower(15f);
            consumeLiquid(Liquids.slag, 120 / 60f);
            results = with(
            WHItems.ceramite, 3,
            WHItems.refineCeramite, 2,
            Items.carbide, 4,
            Items.surgeAlloy, 4
            );
            drawer = new DrawMulti(new DrawRegion("-bottom"),
            new DrawLiquidTile(Liquids.slag),
            new DrawDefault(),
            new DrawFlame(Color.valueOf("FF8C7AFF")));
            ambientSound = loopPulse;
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

        T2TiSteelFurnace = new GenericCrafter("t2-ti-steel-furnace"){
            {
                Color color = WHPal.TiSteelColor;
                requirements(Category.crafting, with(Items.silicon, 100, Items.carbide, 70, WHItems.ceramite, 50, WHItems.titaniumSteel, 70));
                health = 700;
                hasItems = hasPower = true;
                craftTime = 60;
                itemCapacity = 40;
                size = 3;
                consumePower(5);
                consumeItems(with(Items.titanium, 6, Items.metaglass, 9));
                consumeLiquid(Liquids.water, 15 / 60f);
                outputItem = new ItemStack(WHItems.titaniumSteel, 6);
                drawer = new DrawMulti(new DrawDefault(), new DrawFlame(color));
                craftEffect = WHFx.square(TiSteelColor, 35f, 6, 26f, 5f);
                researchCostMultiplier = 0.45f;
            }
        };

        t2MultiPress = new GenericCrafter("t2-multi-press"){
            {
                requirements(Category.crafting, with(Items.thorium, 90, Items.graphite, 50, Items.plastanium, 50));
                health = 500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 64;
                size = 3;
                consumePower(5);
                consumeItems(with(Items.coal, 15));
                consumeLiquid(Liquids.water, 30 / 60f);
                outputItem = new ItemStack(Items.graphite, 12);
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

        sandSeparator = new HeatProducer("sand-separator"){
            {
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
                heatOutput = 2;
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
                new DrawDefault(), new DrawHeatOutput());
                craftEffect = Fx.smokeCloud;
                updateEffect = new Effect(20, e -> {
                    color(Pal.gray, Color.lightGray, e.fin());
                    randLenVectors(e.id, 6, 3f + e.fin() * 6f, (x, y) ->
                    Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45));
                });
                researchCostMultiplier = 0.5f;
            }
        };

        moSurgeSmelter = new HeatProducer("mo-surge-smelter"){
            {
                requirements(Category.crafting, with(Items.tungsten, 60, Items.plastanium, 50, Items.surgeAlloy, 30, WHItems.titaniumSteel, 70));
                health = 900;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 20;
                liquidCapacity = 80f;
                size = 3;
                consumePower(3);
                consumeItems(with(WHItems.molybdenum, 2, Items.carbide, 1, Items.surgeAlloy, 2));
                consumeLiquid(Liquids.slag, 12 / 60f);
                heatOutput = 2;
                outputItem = new ItemStack(WHItems.molybdenumAlloy, 2);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.slag), new DrawCircles(){{
                    color = Pal.lighterOrange.cpy().a(0.4f);
                    strokeMax = 2.5f;
                    radius = 10f;
                    amount = 3;
                }}, new DrawDefault(), new DrawHeatOutput());
                craftEffect = new RadialEffect(Fx.surgeCruciSmoke, 4, 90f, 7f);
                researchCostMultiplier = 0.45f;
            }
        };

        crystalEngraver = new GenericCrafter("crystal-engraver"){
            {
                requirements(Category.crafting, with(Items.metaglass, 50, Items.silicon, 40, WHItems.titaniumSteel, 40, WHItems.ceramite, 70));

                health = 1600;
                hasItems = hasPower = true;
                craftTime = 120;
                itemCapacity = 12;
                liquidCapacity = 120;
                size = 3;
                consumePower(5);
                consumeItems(with(WHItems.molybdenum, 3, Items.carbide, 1, Items.silicon, 6));
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
                }});
                craftEffect = WHFx.square(WHItems.resonantCrystal.color, 35f, 4, 16f, 3f);
                researchCostMultiplier = 0.45f;
            }
        };

        laserEngraver = new HeatProducer("laser-engraver"){
            {
                requirements(Category.crafting, with(Items.phaseFabric, 50, Items.carbide, 80, WHItems.resonantCrystal, 70, WHItems.titaniumSteel, 80));

                health = 2500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 20;
                liquidCapacity = 120;
                size = 4;
                consumePower(5);
                consumeItems(with(WHItems.resonantCrystal, 4, WHItems.molybdenumAlloy, 4, Items.phaseFabric, 6));
                outputItem = new ItemStack(WHItems.protocolChip, 3);
                heatOutput = 4;
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
                }}, new DrawDefault(), new DrawHeatOutput());
                consumeLiquid(WHLiquids.refinePromethium, 10 / 60f);
                craftEffect = new MultiEffect(WHFx.square(Liquids.slag.color, 35f, 8, 32, 4f),
                WHFx.diffuse(size, Liquids.slag.color, 60f));
                researchCostMultiplier = 0.45f;
            }
        };

        waterPurifier = new GenericCrafter("water-purifier"){
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
                requirements(Category.crafting, with(Items.silicon, 80, Items.tungsten, 50, WHItems.titaniumSteel, 50));
                health = 600;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 20;
                liquidCapacity = 120;
                size = 3;
                consumePower(4);
                consumeLiquid(Liquids.water, 0.5f);
                consumeItems(with(Items.titanium, 2));
                outputLiquid = new LiquidStack(Liquids.cryofluid, 31 / 60f);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.water),
                new DrawLiquidTile(Liquids.cryofluid){{
                    drawLiquidLight = true;
                }}, new DrawDefault());
                craftEffect = WHFx.square(Liquids.cryofluid.color, 35f, 4, 16f, 5f);
                researchCostMultiplier = 0.45f;
            }
        };

        T2PhaseSynthesizer = new GenericCrafter("t2-phase-synthesizer"){{

            requirements(Category.crafting, with(WHItems.titaniumSteel, 80, Items.silicon, 150, Items.carbide, 50, WHItems.ceramite, 50));
            health = 1000;
            hasItems = hasPower = hasLiquids = true;
            craftTime = 120;
            itemCapacity = 20;
            liquidCapacity = 120;
            size = 3;
            consumePower(5);
            consumeLiquid(Liquids.ozone, 30f / 60f);
            consumeItems(with(Items.thorium, 4, WHItems.oreSand, 10));
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
        promethiumRefinery = new GenericCrafter("promethium-refinery"){
            {

                requirements(Category.crafting, with(Items.phaseFabric, 20, Items.tungsten, 50, WHItems.ceramite, 20, WHItems.titaniumSteel, 50));

                health = 1200;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 20;
                liquidCapacity = 180;
                size = 3;
                consumePower(5);
                consumeItems(with(Items.phaseFabric, 4));
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
                new DrawDefault());
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

       /* scrapCrusher = new MultiCrafter("scrap-crusher"){{

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
        }};*/

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
            requirements(Category.crafting, with(WHItems.titaniumSteel, 120, WHItems.molybdenum, 180, Items.carbide, 120, WHItems.ceramite, 100));

            size = 4;
            craftTime = 120f;
            health = 1200;
            hasItems = hasPower = hasLiquids = true;
            liquidCapacity = 50;
            itemCapacity = 150;
            consumePower(8f);
            consumeItems(with(Items.copper, 30, Items.silicon, 25, WHItems.titaniumSteel, 10));
            consumeLiquid(WHLiquids.refinePromethium, 10 / 60f);
            outputItem = new ItemStack(Items.surgeAlloy, 12);
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
            liquidCapacity = 120;
            consumePower(4f);
            consumeLiquid(Liquids.nitrogen, 40 / 60f);
            consumeLiquid(Liquids.cryofluid, 30 / 60f);
            outputLiquid = new LiquidStack(WHLiquids.liquidNitrogen, 40);
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
                ambientSound = loopHum;
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
                ambientSound = loopSmelter;
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
            ambientSound = loopHum;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.slag), new DrawDefault(), new DrawHeatOutput());
            consumeLiquid(Liquids.slag, 1);
            heatOutput = 10f;
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
            ambientSound = loopHum;
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

        tungstenConverter = new HeatCrafter("tungsten-converter"){{
            requirements(Category.crafting, with(WHItems.titaniumSteel, 50, Items.plastanium, 60, Items.phaseFabric, 20));

            size = 2;
            craftTime = 60f;
            health = 400;
            hasPower = hasItems = true;
            itemCapacity = 20;
            consumePower(2f);
            consumeItem(Items.thorium, 2);
            outputItem = new ItemStack(Items.tungsten, 2);
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
            maxEfficiency = 2f;
            ambientSound = loopSmelter;
            updateEffect = WHFx.square(Items.tungsten.color, 20f, 4, 12, 5);
            researchCostMultiplier = 0.6f;
        }};

        molybdenumConverter = new GenericCrafter("molybdenum-converter"){{
            requirements(Category.crafting, with(WHItems.ceramite, 20, Items.carbide, 40, Items.phaseFabric, 50));

            size = 2;
            craftTime = 60f;
            health = 600;
            hasPower = hasItems = true;
            itemCapacity = 20;
            consumePower(4f);
            consumeItem(Items.plastanium, 1);
            outputItem = new ItemStack(WHItems.molybdenum, 2);
            drawer = new DrawMulti(new DrawDefault(),
            new DrawGlowRegion(){{
                alpha = 0.7f;
                color = Color.valueOf("F89661FF");
                glowIntensity = 0.3f;
                glowScale = 6f;
                rotateSpeed = 1.5f;

            }});
            ambientSound = loopSmelter;
            updateEffect = WHFx.square(WHItems.molybdenum.color, 20f, 4, 12, 5);
            researchCostMultiplier = 0.6f;
        }};

        vibraniumConverter = new HeatCrafter("vibranium-converter"){{
            requirements(Category.crafting, with(WHItems.molybdenumAlloy, 20, Items.carbide, 40, Items.phaseFabric, 90));

            size = 2;
            craftTime = 60f;
            health = 800;
            hasPower = hasItems = true;
            itemCapacity = 20;
            consumePower(4f);
            consumeItem(Items.carbide, 3);
            outputItem = new ItemStack(WHItems.vibranium, 1);
            drawer = new DrawMulti(new DrawDefault(),
            new DrawGlowRegion(){{
                alpha = 0.7f;
                color = Color.valueOf("F89661FF");
                glowIntensity = 0.3f;
                glowScale = 6f;
                rotateSpeed = 1.5f;

            }}, new DrawHeatInput());
            heatRequirement = 15f;
            maxEfficiency = 2f;
            ambientSound = loopSmelter;
            updateEffect = WHFx.square(WHItems.vibranium.color, 20f, 4, 12, 5);
            researchCostMultiplier = 0.6f;
        }};


        //陶钢
        ceramiteSteelFoundry = new GenericCrafter("ceramite-steel-foundry"){
            {
                Color color = CeramiteColor;
                requirements(Category.crafting, with(Items.lead, 100, Items.silicon, 50, Items.tungsten, 25, Items.plastanium, 25));
                health = 300;
                hasItems = hasPower = true;
                craftTime = 60;
                itemCapacity = 20;
                size = 2;
                consumePower(4);
                consumeItems(with(Items.tungsten, 3, Items.plastanium, 3));
                outputItem = new ItemStack(WHItems.ceramite, 2);
                drawer = new DrawMulti(new DrawDefault(), new DrawFlame(color));
                craftEffect = WHFx.square(CeramiteColor, 35f, 4, 16f, 4f);
            }
        };

        T2ceramiteSteelFoundry = new HeatProducer("t2-ceramite-steel-foundry"){
            {
                requirements(Category.crafting, with(Items.silicon, 150, Items.tungsten, 150, WHItems.ceramite, 50, WHItems.resonantCrystal, 50));
                health = 300;
                hasItems = hasPower = true;
                craftTime = 60;
                itemCapacity = 40;
                size = 4;
                consumePower(5);
                consumeItems(with(Items.carbide, 3, Items.plastanium, 5));
                consumeLiquid(Liquids.slag, 30 / 60f);
                outputItem = new ItemStack(WHItems.ceramite, 8);
                heatOutput = 5;
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.slag){{
                    alpha = 0.4f;
                }},
                new DrawRegion("-rotator", -4){{
                    spinSprite = true;
                }},
                new DrawRegion("-rotator", 4){{
                    spinSprite = true;
                }},
                new DrawRegion("-mid"), new DrawCrucibleFlame(),
                new DrawDefault(), new DrawHeatOutput());
                updateEffect = new MultiEffect(new RadialEffect(Fx.surgeCruciSmoke, 4, 90, 7),
                WHFx.square(CeramiteColor, 35f, 4, 16f, 4f));
                craftEffect = new MultiEffect(new RadialEffect(Fx.surgeCruciSmoke, 4, 45, 10f));
            }
        };

        ceramiteRefinery = new HeatProducer("ceramite-refinery"){
            {
                Color color = WHPal.RefineCeramiteColor;
                requirements(Category.crafting, with(WHItems.molybdenumAlloy, 50, Items.carbide, 60, Items.phaseFabric, 50));
                health = 1500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 180;
                itemCapacity = 30;
                size = 3;

                heatOutput = 3f;
                //wasteHeatOutput = 9f;
                consumePower(6);
                consumeItems(with(WHItems.molybdenumAlloy, 2, WHItems.ceramite, 3, Items.carbide, 2));
                consumeLiquid(WHLiquids.refinePromethium, 10 / 60f);
                outputItem = new ItemStack(WHItems.refineCeramite, 3);
                drawer = new DrawMulti(new DrawDefault(), new DrawHeatOutput(), new DrawFlame(color), new DrawGlowRegion(){{
                    color = Liquids.slag.color;
                    glowScale = 12f;
                }});
                craftEffect = new RadialEffect(Fx.surgeCruciSmoke, 4, 90f, 32 / 4f){{
                    rotationOffset = 45F;
                }};
                researchCostMultiplier = 0.6f;
            }
        };

        ADMill = new HeatProducer("admantium-mill"){
            {
                requirements(Category.crafting, with(Items.silicon, 200, WHItems.titaniumSteel, 50, WHItems.ceramite, 50, WHItems.refineCeramite, 30));

                hasItems = true;
                health = 600;
                size = 3;
                hasPower = true;
                hasLiquids = true;
                liquidCapacity = 40;
                itemCapacity = 20;
                craftTime = 180;

                heatOutput = 3f;
                consumePower(6f);
                consumeItems(with(WHItems.vibranium, 6, WHItems.refineCeramite, 3));
                consumeLiquid(WHLiquids.liquidNitrogen, 0.3f);
                outputItem = new ItemStack(WHItems.adamantium, 2);
                drawer = new DrawMulti(new DrawDefault(), new DrawHeatOutput(), new DrawFlame(Color.valueOf("FFEA96FF")), new AdmantiumMillDrawer(Items.surgeAlloy.color.cpy(), 7.5f));
                craftEffect = WHFx.hexagonSmoke(Items.surgeAlloy.color.cpy(), 45, 1f, 7.5f, 20f);
                researchCostMultiplier = 0.6f;
            }
        };
        //Drill
        electronicPneumaticDrill = new Drill("t2-pneumatic-drill"){{
            requirements(Category.production, with(Items.copper, 30, Items.silicon, 15, Items.graphite, 20));
            tier = 3;
            drillTime = 250;
            size = 2;
            consumePower(1);

            consumeLiquid(Liquids.water, 0.06f).boost();
        }};
        MechanicalQuarry = new Quarry("mechanical-quarry"){{

            requirements(Category.production, with(Items.lead, 50, Items.graphite, 30, WHItems.titaniumSteel, 30, Items.silicon, 60));

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
            consumePower(3f);
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
                requirements(Category.production, with(WHItems.protocolChip, 90, WHItems.resonantCrystal, 200, Items.silicon, 500, WHItems.ceramite, 150, WHItems.refineCeramite, 90));

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
                requirements(Category.production, with(WHItems.titaniumSteel, 80, Items.silicon, 200, Items.plastanium, 100, WHItems.ceramite, 80));

                health = 3200;
                drillTime = 500;
                size = 4;
                tier = 7;
                itemCapacity = 120;
                liquidCapacity = 80;
                mineOffset = -2;
                mineSize = 6;
                fogRadius = 2;
                drawRim = true;
                hasItems = hasPower = hasLiquids = true;
                consumePower(6);
                consumeLiquid(Liquids.cryofluid, 0.5f);
                allowedItems = Seq.with(
                Items.thorium, Items.tungsten, WHItems.molybdenum);

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
                requirements(Category.production, with(Items.graphite, 60, Items.silicon, 120, Items.tungsten, 80, WHItems.titaniumSteel, 50));
                health = 800;
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
                consumeLiquid(Liquids.cryofluid, 15 / 60f);
                consumePower(3);
                researchCostMultiplier = 0.6f;
            }
        };

        integratedCompressor = new AttributeCrafter("integrated-compressor"){
            {
                requirements(Category.crafting, with(WHItems.ceramite, 50, Items.graphite, 50, Items.plastanium, 50));
                health = 500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 64;
                liquidCapacity = 100;
                size = 3;
                consumePower(5);
                consumeLiquid(Liquids.water, 7.5f / 60f);
                outputItem = new ItemStack(Items.graphite, 6);
                attribute = Attribute.oil;
                drawer = new DrawMulti(new DrawDefault(),
                new DrawLiquidTile(Liquids.water){{
                    alpha = 0.2f;
                }},
                new DrawBlurSpin("-rotator", 6f){{
                    blurThresh = 0.01f;
                }},
                new DrawRegion("-rotator2", -3){{
                    spinSprite = true;
                }},
                new DrawRegion("-top")
                );
                craftEffect = Fx.pulverizeMedium;
            }
        };

        slagExtractor = new AttributeCrafter("slag-extractor"){
            {
                requirements(Category.production, with(Items.thorium, 100, Items.graphite, 100, Items.silicon, 150, WHItems.titaniumSteel, 50));

                health = 450;
                size = 3;
                hasItems = hasPower = hasLiquids = true;
                liquidCapacity = 180;
                updateEffect = Fx.redgeneratespark;
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidRegion(Liquids.slag),
                new DrawRegion("-rotator", 8.6f, true),
                new DrawDefault());
                consumePower(10);
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
                requirements(Category.production, with(Items.copper, 100, Items.silicon, 90, WHItems.titaniumSteel, 40));
                health = 400;
                size = 3;
                hasPower = true;
                hasLiquids = true;
                liquidCapacity = 50;
                itemCapacity = 10;
                pumpAmount = 30 / 60f;
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

        steelConduit = new TubeConduit("steel-conduit"){
            {
                requirements(Category.liquid, with(WHItems.titaniumSteel, 1, WHItems.ceramite, 1));
                health = 600;
                armor = 5;
                liquidCapacity = 80f;
                liquidPressure = 2.5f;
                drawCover = true;
                bridgeReplacement = WHBlocks.T2BridgeConduit;
                //重钢导管
            }
        };

        lightConduit = new TubeConduit("light-conduit"){
            {
                requirements(Category.liquid, with(Items.metaglass, 3, WHItems.titaniumSteel, 1));
                health = 250;
                liquidCapacity = 35f;
                liquidPressure = 2.5f;
                drawArrow = true;
                bridgeReplacement = WHBlocks.tiSteelBridgeConduit;
                //轻质导管
            }
        };

        T2LiquidTank = new LiquidRouter("t2-ti-steel-liquid-tank"){
            {
                requirements(Category.liquid, with(Items.tungsten, 50, Items.lead, 150, WHItems.titaniumSteel, 100, Items.plastanium, 100));
                health = 1500;
                size = 3;
                liquidCapacity = 5000;
                absorbLasers = true;
                researchCostMultiplier = 0.36f;
                //钛钢储液罐
            }
        };

        armorLiquidTank = new LiquidRouter("armor-liquid-tank"){
            {
                requirements(Category.liquid, with(Items.tungsten, 100, Items.lead, 150, WHItems.titaniumSteel, 100, WHItems.ceramite, 50));
                health = 3000;
                armor = 10;
                size = 3;
                liquidCapacity = 8000;
                absorbLasers = true;
                researchCostMultiplier = 0.36f;
                //钛钢储液罐
            }
        };

        armorFluidJunction = new LiquidJunction("armor-fluid-junction"){{
            requirements(Category.liquid, with(Items.graphite, 4, WHItems.titaniumSteel, 4, WHItems.molybdenum, 4));
            buildCostMultiplier = 3f;
            health = 450;
            armor = 5;
            ((TubeConduit)lightConduit).junctionReplacement = this;
            researchCostMultiplier = 1;
            solid = false;
            underBullets = true;
        }};

        mixedFluidJunction = new MixedFluidJunction("mixed-fluid-junction"){{
            requirements(Category.liquid, with(Items.graphite, 10, WHItems.titaniumSteel, 8, WHItems.molybdenumAlloy, 4));
            buildCostMultiplier = 2f;
            health = 450;
            armor = 5;
            displayedSpeed = 30f;
            ((TubeConduit)steelConduit).junctionReplacement = this;
            researchCostMultiplier = 1;
            solid = false;
            underBullets = true;
        }};

        armorFluidRouter = new LiquidRouter("armor-fluid-router"){{
            requirements(Category.liquid, with(Items.graphite, 4, WHItems.titaniumSteel, 4, WHItems.molybdenum, 4));
            liquidCapacity = 150f;
            liquidPadding = 3f / 4f;
            researchCostMultiplier = 3;
            underBullets = true;
            solid = false;
            health = 450;
            armor = 5;

            explosivenessScale = flammabilityScale = 40f / liquidCapacity;
        }};


        tiSteelBridgeConduit = new LiquidBridge("steel-bridge-conduit"){
            {
                requirements(Category.liquid, with(Items.lead, 20, Items.silicon, 25, WHItems.titaniumSteel, 10));
                health = 330;
                armor = 2;
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

        shelterDome = new ShelterDome("shelter-dome"){
            {
                requirements(Category.effect, with(Items.silicon, 200, WHItems.ceramite, 50, WHItems.resonantCrystal, 100));
                size = 4;
                range = 300;
                consumePower(1200 / 60f);
                researchCostMultiplier = 0.7f;
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
                consumePower(3600 / 60f);
                researchCostMultiplier = 0.7f;
                //区块化虚空盾
            }
        };


        strongholdCore = new FrontlineCoreBlock("s-core"){
            {
                requirements(Category.effect, with(Items.copper, 1000, Items.lead, 2000, Items.silicon, 1000, WHItems.titaniumSteel, 500));

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

        T1strongholdCore = new CoreBlock("m-core"){
            {
                requirements(Category.effect, with(Items.tungsten, 2000, Items.silicon, 2000, Items.surgeAlloy, 500, WHItems.titaniumSteel, 5000, WHItems.ceramite, 800));
                unitType = UnitTypes.evoke;
                armor = 35;
                health = 12000;
                itemCapacity = 15000;
                size = 5;
                unitCapModifier = 10;
                researchCostMultiplier = 0.8f;
                //大型据点核心
            }
        };

        T2strongholdCore = new CoreBlock("l-core"){
            {
                requirements(Category.effect, with(WHItems.molybdenum, 3000, Items.silicon, 8000, WHItems.molybdenumAlloy, 1000, WHItems.refineCeramite, 1000, WHItems.titaniumSteel, 5000));
                unitType = UnitTypes.evoke;
                armor = 35;
                health = 30000;
                itemCapacity = 40000;
                size = 6;
                unitCapModifier = 10;
                researchCostMultiplier = 0.8f;
                //大型据点核心
            }
        };

        armorInvertedSorter = new Sorter("armor-inverted-sorter"){{
            requirements(Category.distribution, with(Items.lead, 2, Items.tungsten, 2, WHItems.titaniumSteel, 1));
            buildCostMultiplier = 3f;
            health = 300;
            armor = 5;
            invert = true;
        }};

        armorSorter = new Sorter("armor-sorter"){{
            requirements(Category.distribution, with(Items.lead, 2, Items.tungsten, 2, WHItems.titaniumSteel, 1));
            buildCostMultiplier = 3f;
            health = 300;
            armor = 5;
        }};

        armorJunction = new Junction("armor-junction"){{
            requirements(Category.distribution, with(Items.graphite, 10, WHItems.titaniumSteel, 3));
            speed = 30;
            capacity = 10;
            displayedSpeed = 18.5f;
            health = 200;
            armor = 2;
            buildCostMultiplier = 6f;
        }};

        armorRouter = new Router("armor-router"){{
            requirements(Category.distribution, with(Items.graphite, 6, WHItems.titaniumSteel, 3));
            buildCostMultiplier = 2f;
            health = 200;
            armor = 2;
            speed = 12;
        }};

        armorOverflowGate = new OverflowDuct("armor-overflow-gate"){{
            requirements(Category.distribution, with(Items.graphite, 8, WHItems.titaniumSteel, 3));
            health = 300;
            armor = 2;
            speed = 2f;
            solid = false;
            researchCostMultiplier = 2f;
        }};

        armorUnderflowGate = new OverflowDuct("armor-underflow-gate"){{
            requirements(Category.distribution, with(Items.graphite, 8, WHItems.titaniumSteel, 3));
            health = 300;
            armor = 2;
            speed = 2f;
            solid = false;
            invert = true;
            researchCostMultiplier = 2f;
        }};

        steelDust = new CoverdConveyor("steel-dust"){{

            requirements(Category.distribution, with(WHItems.titaniumSteel, 2, Items.lead, 1, Items.silicon, 2));

            health = 150;
            underBullets = true;
            hasShadow = true;
            placeableLiquid = true;
            size = 1;
            bridgeReplacement = WHBlocks.steelBridge;
            junctionReplacement = Blocks.invertedSorter;
            speed = 30f / 138f;//why?
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
                requirements(Category.distribution, with(WHItems.titaniumSteel, 15, WHItems.sealedPromethium, 10, Items.phaseFabric, 10));

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

            requirements(Category.distribution, with(Items.lead, 5, Items.silicon, 4, WHItems.titaniumSteel, 2, WHItems.ceramite, 1));

            health = 550;
            size = 1;
            floating = false;
            update = true;
            hasItems = true;
            speed = 3.2f / 60f;
            itemCapacity = 40;
            researchCostMultiplier = 1;
            //陶钢打包带
        }};

        armorCoverStackBelt = new TubeStackConveyor("armor-cover-stack-belt"){
            {
                requirements(Category.distribution, with(Items.metaglass, 3, WHItems.titaniumSteel, 2, WHItems.ceramite, 2));
                health = 1000;
                size = 1;
                update = true;
                drawCover = true;
                floating = true;
                speed = 5 / 60f;
                itemCapacity = 40;
                researchCostMultiplier = 1;
                placeableLiquid = true;
            }
        };

        stackBridge = new StackBridge("packet-bridge"){
            {
                requirements(Category.distribution, with(WHItems.ceramite, 20, WHItems.sealedPromethium, 50, Items.phaseFabric, 15));

                health = 400;
                range = 10;
                arrowSpacing = 8;
                arrowOffset = 4f;
                arrowTimeScl = 12;
                bridgeWidth = 8;

                speed = 3.2f;
                itemCapacity = 40;

                researchCostMultiplier = 1;
            }

        };

        trackDriver = new MassDriver("track-driver"){
            {
                requirements(Category.distribution, with(WHItems.titaniumSteel, 120, Items.phaseFabric, 50, Items.carbide, 50, WHItems.ceramite, 50));

                health = 2800;
                size = 3;
                hasItems = true;
                itemCapacity = 300;
                minDistribute = 60;
                reload = 120;
                rotateSpeed = 2.5f;
                bulletSpeed = 8;
                shootEffect = Fx.instShoot;
                smokeEffect = WHFx.hugeSmokeGray;
                shootSound = shootLancer;
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
            ambientSound = loopHum;
            ambientSoundVolume = 0.06f;


            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.water), new DrawDefault(),
            new DrawBlurSpin("-rotator", 6f){{
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
                ambientSound = loopSmelter;
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

                ambientSound = loopSmelter;
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
                ambientSound = loopPulse;
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
                ambientSound = Sounds.loopFlux;
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
                explodeSound = explosionReactor;
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

        SmallTiNode = new PowerNode("composite-node"){{
            requirements(Category.power, with(Items.lead, 8, WHItems.titaniumSteel, 1, Items.silicon, 2));

            health = 150;
            size = 1;
            maxNodes = 12;
            laserRange = 9;
            laserScale = 0.4f;
            laserColor1 = Color.white;
            laserColor2 = WHPal.SkyBlue;
            researchCostMultiplier = 0.8f;
        }};

        airFactory = new UnitFactory("air-factory"){{
            requirements(Category.units, with(Items.lead, 100, Items.graphite, 35, Items.silicon, 120, WHItems.titaniumSteel, 25f));

            size = 3;
            plans = Seq.with(
            new UnitPlan(WHUnitTypes.air1, 60f * 30, with(Items.graphite, 50, Items.metaglass, 30, Items.silicon, 70)),
            new UnitPlan(WHUnitTypes.airB1, 60f * 40, with(Items.graphite, 50, Items.silicon, 40, WHItems.titaniumSteel, 15))
            );
            fogRadius = 3;
            consumePower(180 / 60f);
            researchCostMultiplier = 0.5f;
        }};

        groundFactory = new UnitFactory("ground-factory"){{
            requirements(Category.units, with(Items.graphite, 50, Items.lead, 150, Items.silicon, 100));
            plans = Seq.with(
            new UnitPlan(WHUnitTypes.M1, 60f * 20, with(Items.graphite, 30, Items.silicon, 30, Items.lead, 30))
            );
            size = 3;
            fogRadius = 3;
            consumePower(180 / 60f);
            researchCostMultiplier = 0.5f;
        }};

        mechaFactory = new UnitFactory("mecha-factory"){{
            requirements(Category.units, with(WHItems.titaniumSteel, 100, Items.tungsten, 120, Items.silicon, 100, Items.plastanium, 50));

            plans = Seq.with(
            new UnitPlan(WHUnitTypes.Mecha2, 60f * 60, with(WHItems.titaniumSteel, 50, Items.thorium, 100, Items.silicon, 150, Items.plastanium, 35))
            );

            size = 3;
            consumePower(240 / 6f);
            consumeLiquid(Liquids.hydrogen, 22.5f / 60f);
            researchCostMultiplier = 0.75f;
        }};

        tankFactory = new UnitFactory("tank-factory"){{
            requirements(Category.units, with(Items.thorium, 1000, Items.silicon, 1500, WHItems.titaniumSteel, 800, WHItems.ceramite, 300, WHItems.resonantCrystal, 200));

            size = 7;
            consumePower(30f);
            consumeLiquid(Liquids.nitrogen, 40 / 60f);
            consumeLiquid(WHLiquids.refinePromethium, 30 / 60f);
            createSound = Sounds.unitCreateBig;

            plans = Seq.with(
            new UnitPlan(WHUnitTypes.tank1, 60f * 150f, with(WHItems.titaniumSteel, 800, Items.carbide, 250, WHItems.ceramite, 400, WHItems.resonantCrystal, 100, WHItems.molybdenumAlloy, 100, Items.silicon, 800)),
            new UnitPlan(WHUnitTypes.tank1s, 60f * 150f, with(WHItems.titaniumSteel, 500, Items.carbide, 400, WHItems.ceramite, 400, WHItems.resonantCrystal, 100, WHItems.molybdenumAlloy, 150, Items.silicon, 800))
            );

            researchCostMultiplier = 0.75f;
        }};

        t2Module = new MultReconstructor("t2-modification-module"){{
            requirements(Category.units, with(WHItems.titaniumSteel, 80, Items.tungsten, 80, Items.silicon, 200, Items.plastanium, 50));

            size = 3;
            consumePower(8f);
            consumeLiquid(Liquids.hydrogen, 15f / 60f);

            addUpgrade(WHUnitTypes.M1, WHUnitTypes.M2, with(WHItems.titaniumSteel, 60, Items.thorium, 50, Items.silicon, 70));
            addUpgrade(WHUnitTypes.air1, WHUnitTypes.air2, with(WHItems.titaniumSteel, 70, Items.graphite, 80, Items.silicon, 40));
            addUpgrade(WHUnitTypes.airB1, WHUnitTypes.airB2, with(WHItems.titaniumSteel, 70, Items.tungsten, 50, Items.silicon, 70));

            constructTime = 60f * 20f;
            researchCostMultiplier = 0.75f;
        }};


        t3Module = new MultReconstructor("t3-modification-module"){{
            requirements(Category.units, with(Items.thorium, 300, WHItems.titaniumSteel, 200, Items.silicon, 500, WHItems.ceramite, 90, Items.phaseFabric, 150));

            size = 5;
            consumePower(15f);
            consumeLiquid(Liquids.nitrogen, 40 / 60f);

            addUpgrade(WHUnitTypes.M2, WHUnitTypes.M3, with(WHItems.titaniumSteel, 120, Items.tungsten, 150, Items.plastanium, 70, Items.silicon, 200));
            addUpgrade(WHUnitTypes.air2, WHUnitTypes.air3, with(WHItems.titaniumSteel, 120, WHItems.ceramite, 70, Items.silicon, 130));
            addUpgrade(WHUnitTypes.airB2, WHUnitTypes.airB3, with(WHItems.titaniumSteel, 120, Items.tungsten, 150, Items.plastanium, 50, Items.silicon, 130));

            addUpgrade(WHUnitTypes.Mecha2, WHUnitTypes.Mecha3, with(WHItems.titaniumSteel, 300, Items.phaseFabric, 150, WHItems.ceramite, 150, WHItems.sealedPromethium, 100, Items.silicon, 600));

            constructTime = 60f * 40f;
            researchCostMultiplier = 0.75f;
        }};

        t4Module = new MultReconstructor("t4-modification-module"){{
            requirements(Category.units, with(Items.thorium, 1000, Items.silicon, 1500, WHItems.titaniumSteel, 800, WHItems.ceramite, 300, WHItems.resonantCrystal, 400));

            size = 7;
            consumePower(30f);
            consumeLiquid(Liquids.nitrogen, 40 / 60f);
            consumeLiquid(WHLiquids.refinePromethium, 30 / 60f);
            createSound = Sounds.unitCreateBig;

            constructTime = 60f * 70f;

            addUpgrade(WHUnitTypes.M3, WHUnitTypes.M4A, with(WHItems.titaniumSteel, 500, Items.carbide, 200, WHItems.ceramite, 300, Items.silicon, 800));

            addUpgrade(WHUnitTypes.air3, WHUnitTypes.air4, with(WHItems.titaniumSteel, 300, Items.surgeAlloy, 300, Items.carbide, 300, WHItems.ceramite, 150, Items.silicon, 800));
            addUpgrade(WHUnitTypes.airB3, WHUnitTypes.airB4, with(WHItems.titaniumSteel, 400, Items.phaseFabric, 200, WHItems.ceramite, 150, Items.silicon, 1500));

            addUpgrade(WHUnitTypes.Mecha3, WHUnitTypes.Mecha4, with(WHItems.titaniumSteel, 300, WHItems.molybdenumAlloy, 250,
            WHItems.ceramite, 400, WHItems.resonantCrystal, 100, Items.silicon, 800));

            researchCostMultiplier = 0.75f;
        }};


        t5Module = new MultReconstructor("t5-modification-module"){{
            requirements(Category.units, with(WHItems.titaniumSteel, 1500, Items.silicon, 3000, Items.surgeAlloy, 1000, WHItems.ceramite, 800, WHItems.molybdenumAlloy, 300, WHItems.protocolChip, 150, WHItems.refineCeramite, 150));

            size = 9;
            consumePower(50f);
            consumeLiquid(WHLiquids.liquidNitrogen, 80 / 60f);

            constructTime = 60f * 90f;
            createSound = Sounds.unitCreateBig;

            addUpgrade(WHUnitTypes.M4A, WHUnitTypes.M5, with(WHItems.ceramite, 300, WHItems.resonantCrystal, 100,
            WHItems.molybdenumAlloy, 300, Items.silicon, 1500));

            addUpgrade(WHUnitTypes.air4, WHUnitTypes.air5, with(WHItems.ceramite, 400, WHItems.resonantCrystal, 200,
            WHItems.molybdenumAlloy, 300, Items.silicon, 1500));

            addUpgrade(WHUnitTypes.airB4, WHUnitTypes.airB5, with(
            WHItems.ceramite, 400, WHItems.protocolChip, 200, Items.silicon, 1500));

            addUpgrade(WHUnitTypes.tank1, WHUnitTypes.tank2, with(WHItems.ceramite, 400, WHItems.resonantCrystal, 200,
            WHItems.molybdenumAlloy, 350, Items.silicon, 1000));

            addUpgrade(WHUnitTypes.tank1s, WHUnitTypes.tank2s, with(WHItems.ceramite, 400, WHItems.resonantCrystal, 200,
            WHItems.molybdenumAlloy, 350, Items.silicon, 1000));

            addUpgrade(WHUnitTypes.Mecha4, WHUnitTypes.Mecha5, with(WHItems.ceramite, 200, WHItems.molybdenumAlloy, 400,
            WHItems.protocolChip, 200, Items.silicon, 1000));

            researchCostMultiplier = 0.5f;
        }};


        t6Module = new MultReconstructor("exterminate-reconstructor"){{
            requirements(Category.units, with(WHItems.titaniumSteel, 3000, WHItems.resonantCrystal, 1500, WHItems.ceramite, 1500, WHItems.protocolChip, 500, WHItems.refineCeramite, 1000));

            size = 11;
            consumePower(100f);
            consumeLiquid(WHLiquids.liquidNitrogen, 160 / 60f);

            constructTime = 60f * 60f * 2;
            createSound = Sounds.unitCreateBig;

            addUpgrade(WHUnitTypes.M5, WHUnitTypes.M6, with(WHItems.ceramite, 500, WHItems.protocolChip, 200, WHItems.sealedPromethium, 100,
            WHItems.refineCeramite, 200));

            addUpgrade(WHUnitTypes.air5, WHUnitTypes.air6, with(WHItems.ceramite, 500, WHItems.protocolChip, 200, WHItems.sealedPromethium, 100,
            WHItems.refineCeramite, 500));

            addUpgrade(WHUnitTypes.airB5, WHUnitTypes.airB6, with(WHItems.ceramite, 500, WHItems.resonantCrystal, 500, WHItems.protocolChip, 300,
            WHItems.refineCeramite, 500));

            addUpgrade(WHUnitTypes.tank2, WHUnitTypes.tank3, with(WHItems.ceramite, 1000, WHItems.molybdenumAlloy, 500, WHItems.protocolChip, 200,
            WHItems.refineCeramite, 600));

            addUpgrade(WHUnitTypes.tank2s, WHUnitTypes.tank3s, with(WHItems.ceramite, 500, WHItems.resonantCrystal, 400, WHItems.protocolChip, 300,
            WHItems.refineCeramite, 500));

            addUpgrade(WHUnitTypes.Mecha5, WHUnitTypes.Mecha6, with(WHItems.ceramite, 1000, WHItems.molybdenumAlloy, 500, WHItems.protocolChip, 400,
            WHItems.refineCeramite, 600));

            researchCostMultiplier = 0.5f;
        }};

        jumpBeacon = new UnitCallBlock("jump-beacon"){{
            requirements(Category.units, with(Items.silicon, 500, WHItems.titaniumSteel, 300, WHItems.ceramite, 200, WHItems.protocolChip, 100));

            health = 1000;
            range = 150;
            spawnRange = 50;
            size = 4;

            plans = Seq.with(
            new UnitPlan(WHUnitTypes.airB1, 60f * 40, false, with(Items.graphite, 50, Items.silicon, 40, WHItems.titaniumSteel, 15)),
            new UnitPlan(WHUnitTypes.airB2, 60f * 60, false, with(WHItems.titaniumSteel, 50, Items.graphite, 80, Items.silicon, 40)),
            new UnitPlan(WHUnitTypes.airB3, 60f * 80, false, with(WHItems.titaniumSteel, 200, Items.tungsten, 200, Items.plastanium, 100, Items.silicon, 200)),
            new UnitPlan(WHUnitTypes.airB4, 60f * 140, false, with(WHItems.titaniumSteel, 500, WHItems.ceramite, 400, WHItems.sealedPromethium, 100, Items.silicon, 2000)),

            new UnitPlan(WHUnitTypes.tankEn1, 60f * 150, true, with(WHItems.titaniumSteel, 500, WHItems.ceramite, 300, WHItems.resonantCrystal, 150, Items.silicon, 2000)),
            new UnitPlan(WHUnitTypes.tankEn2, 60f * 250, true, with(WHItems.titaniumSteel, 500, WHItems.ceramite, 400, WHItems.protocolChip, 200,
            WHItems.molybdenumAlloy, 400, Items.silicon, 2500)));

            drawBlock = b -> {
                Draw.z(Layer.effect);
                Draw.color(b.team.color.cpy());
                Lines.stroke(1.5f * b.warmup);
                arcProcessFlip(b.x, b.y, b.hitSize() * 0.8f * b.warmup * (1 - Mathf.sin(Time.time, 0.2f)), Time.time, 20);
                for(int i = 0; i < 3; i++){
                    float f = (Time.time - 100 / 3f * i) % 100 / 100;
                    Tmp.v1.trns(90 + 30, 40 * (1 - f)).add(b.x, b.y);
                    rand.setSeed(b.id);
                    Lines.stroke(f * fout(f, 0.9f) * (1.5f + Mathf.absin(Time.time, 8.0F, 1)) * b.warmup);
                    Lines.square(Tmp.v1.x, Tmp.v1.y, f * size * tilesize * 0.5f, (rand.random(60f) + Time.time / 3) % 360f);
                }
            };
            consumePower(1500 / 60f);

            drawer = new DrawMulti(
            new DrawCrucibleFlame(){{
                particleRad = 8;
            }},
            new DrawSoftParticles(){{
                alpha = 0.8f;
                particleRad = 10;
                particleSize = 7f;
            }},
            new DrawArcSmelt(){{
                drawCenter = false;
                midColor = flameColor = WHPal.ShootOrange;
                particleRad = 8;
                particleLen = 7f;
                particles = 15;
                particleLife = 60f;
            }},
            new DrawDefault()
            );
        }};

        energyWarpGate = new UnitCallBlock("energy-warp-gate"){{
            requirements(Category.units, with(WHItems.titaniumSteel, 2000, Items.carbide, 2000, WHItems.refineCeramite, 1500, WHItems.protocolChip, 1000));
            health = 4000;
            size = 6;
            range = 300;
            spawnRange = 150;
            plans = Seq.with(
            new UnitPlan(WHUnitTypes.air7, 60f * 60f * 7.5f, true, with(WHItems.titaniumSteel, 4000, WHItems.ceramite, 2000,
            WHItems.adamantium, 2000, WHItems.refineCeramite, 1000, WHItems.protocolChip, 1200)),
            new UnitPlan(WHUnitTypes.tankAG, 60f * 60f * 7.5f, true, with(WHItems.titaniumSteel, 4000, WHItems.ceramite, 3000,
            WHItems.adamantium, 2000, WHItems.refineCeramite, 1000, WHItems.protocolChip, 800)),
            new UnitPlan(WHUnitTypes.Mecha7, 60f * 60f * 7.5f, true, with(WHItems.titaniumSteel, 4000, WHItems.ceramite, 2500,
            WHItems.adamantium, 2500, WHItems.refineCeramite, 1000, WHItems.protocolChip, 800))
            );

            Effect t = new TrailEffect(60, 1000, 3, 13, 2)
            .trailUpdater((e, trail, x, y, w, len, index) -> {
                WHFx.rand.setSeed(e.id);
                float range = 100;
                float rand1 = WHFx.rand.random(0.5f, 1f);
                float rand2 = WHFx.rand.random(360);
                float cur = Mathf.curve(e.fin(), 0, 0.15f);
                Draw.z(Layer.effect - 0.001f);
                Angles.randLenVectors(e.id + index, 1, range * rand1 * e.fout() + 10, rand2 * e.fout(), 360, (x1, y1) -> {
                    trail.length = (int)(cur * len);
                    trail.update(x1 + x, y1 + y, w * e.fout());
                });
            });

            Effect t2 = new Effect(60, e -> {
                if(!(e.data instanceof Vec2 vec)) return;
                float len = vec.dst(e.x, e.y);
                WHFx.rand.setSeed(e.id);
                float rand1 = WHFx.rand.random(0.7f, 1f);
                Draw.z(Layer.effect - 0.001f);
                Draw.color(e.color);
                Angles.randLenVectors(e.id, 5, len * rand1 * e.fin(), 90f, 5, (x1, y1) -> {
                    Tmp.v1.trns(rand.random(360f), 8 * e.fin());
                    Fill.circle(e.x + Tmp.v1.x + x1, e.y + Tmp.v1.y + y1, 4 * e.fin() * fout(e.fin(), 0.9f));
                });
            });

            drawBlock = b -> {
                Draw.z(Layer.effect);
                Draw.color(b.team.color.cpy());
                rand.setSeed(b.id);
                Lines.stroke((1.5f + Mathf.absin(Time.time, 8.0F, 1)) * b.warmup);
                arcProcessFlip(b.x, b.y, b.hitSize() * 0.7f * (1 - b.warmup * Mathf.sin(Time.time, 0.8f)), Time.time, 20);
                Lines.square(b.x, b.y, size * tilesize * 0.7f * (1 - Mathf.sin(Time.time, 0.3f)), Time.time / 2 % 360f);
                Lines.square(b.x, b.y, size * tilesize * 0.3f * (1 - Mathf.sin(Time.time, 0.2f)), rand.random(180f) - Time.time / 4 % 360f);

                Tmp.v1.trns(90, 5 * tilesize * b.warmup);

                for(int m : Mathf.signs){
                    for(int i = 1; i <= 4; i++){
                        Tmp.v2.trns(180, m * 1.8f * tilesize + m * i * tilesize * 2 * b.warmup);
                        float f = (100f - (Time.time - 25f * i) % 100f) / 100f;
                        TextureRegion arrowRegion = WHContent.arrowRegion;
                        Draw.scl(1.5f);
                        Draw.rect(arrowRegion, Tmp.v1.x + Tmp.v2.x + b.x, Tmp.v1.y + Tmp.v2.y + b.y, arrowRegion.width * b.warmup * Draw.scl * f, arrowRegion.height * b.warmup * Draw.scl * f, 180f + 90 * m);
                    }
                    Tmp.v2.trns(90, m * 2f * tilesize);//上下对称
                    Lines.stroke((1.5f + Mathf.absin(Time.time, 8.0F, 1)) * b.warmup);
                    for(int m1 : Mathf.signs){
                        Tmp.v3.trns(180, m1 * 3f * tilesize * b.warmup);//左右对称
                        Lines.lineAngle(Tmp.v1.x + Tmp.v2.x + Tmp.v3.x + b.x, Tmp.v1.y + Tmp.v2.y + Tmp.v3.y + b.y, 180 * m, m1 * tilesize * 7f * b.warmup);
                    }
                }

                float s = 15f * b.warmup * (1 - Mathf.sin(Time.time, 20, 0.08f));
                Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, s);
                Draw.color(Pal.coalBlack);
                Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, s * 0.8f);
                if(Mathf.chanceDelta(0.02f) && b.warmup > 0.99f && !state.isPaused()){
                    t.at(b.x + Tmp.v1.x, b.y + Tmp.v1.y, 0, b.team.color.cpy());
                    t2.at(b.x, b.y, 0, b.team.color.cpy(), new Vec2().set(b).add(Tmp.v1));
                }
            };


            consumePower(8000 / 60f);

            drawer = new DrawMulti(
            new DrawCrucibleFlame(){{
                particleRad = 8;
            }},
            new DrawArcSmelt(){{
                drawCenter = false;
                midColor = flameColor = WHPal.ShootOrange;
                particleRad = 15;
                particleLen = 8f;
                particleLife = 90;
            }},
            new DrawDefault(),
            new DrawArcs(){{
                flameColor = WHPal.ShootOrange;
                midColor = ShootOrangeLight;
                arcLife = 90f;
                arcs = 15;
                arcRad = 25f;
            }}
            );
        }};

        t2PayloadMassDriver = new PayloadMassDriver2("t2-payload-mass-driver"){{
            requirements(Category.units, with(WHItems.ceramite, 100, WHItems.titaniumSteel, 150, WHItems.resonantCrystal, 80));
            size = 5;
            reload = 120f;
            chargeTime = 90f;
            range = 2500;
            maxPayloadSize = 4.5f;
            grabWidth = 11f;
            consumePower(15f);
        }};


        TiNode = new PowerNode("t2-composite-node"){
            {
                requirements(Category.power, with(Items.lead, 20, WHItems.titaniumSteel, 10, Items.silicon, 15));

                health = 400;
                size = 2;
                maxNodes = 18;
                laserRange = 18;
                laserScale = 0.4f;
                laserColor1 = Color.white;
                laserColor2 = WHPal.SkyBlue;
                researchCostMultiplier = 0.8f;
                //钢装甲节点
            }
        };


        T2TiNode = new CompositePoweNode("armor-power-tower"){
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
            shootSound = Sounds.shootCyclone;
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
            health = 1500;
            size = 2;
            range = 240;
            outlineColor = WHPal.Outline;
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
            shootSound = shootSpectre;
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

        Blaze = new ContinuousTurret("Blaze"){
            {
                requirements(Category.turret, with(Items.silicon, 100, Items.graphite, 100, WHItems.titaniumSteel, 50, Items.plastanium, 50));


                buildCostMultiplier = 8f;
                health = 2800;
                size = 3;
                float r = range = 200;
                outlineColor = WHPal.Outline;

                drawer = new DrawTurret(WarHammerMod.name("turret-"));

                scaleDamageEfficiency = true;
                shootSound = Sounds.none;
                loopSoundVolume = 1f;
                loopSound = Sounds.beamLustre;

                shootWarmupSpeed = 0.1f;
                shootCone = 360f;

                aimChangeSpeed = 5;
                rotateSpeed = 5;

                shootY = 16 / 4f;
                unitSort = UnitSorts.farthest;

                shootType = new PointLaserBulletType(){
                    {
                        damage = 150;
                        damageInterval = 6;
                        buildingDamageMultiplier = 0.3f;
                        trailColor = hitColor = WHPal.SkyBlue.cpy().lerp(Color.white, 0.3f);
                        trailLength = 10;
                        trailWidth = 2;
                        beamEffect = Fx.none;
                    }

                    public float width = 6;

                    @Override
                    public void update(Bullet b){
                        super.update(b);
                        if(b.owner instanceof BlazeBuild tu){
                            b.fdata = tu.charge;
                            b.damage = damage * b.fdata;
                        }
                        Color c = trailColor.cpy().lerp(ShootOrange, b.fdata);
                        if(b.timer(3, 10)){
                            Effect t = WHFx.hitSpark(c, 30, 3, 20, 1.3f, 5);
                            t.at(b.aimX, b.aimY, c);
                            if(b.fdata > 0.99f) t.at(b.x, b.y, c);
                        }
                    }

                    @Override
                    public void drawTrail(Bullet b){
                        if(trailLength > 0 && b.trail != null){
                            float z = Draw.z();
                            Draw.z(z - 0.0001f);
                            Color c = trailColor.cpy().lerp(ShootOrange, b.fdata);
                            b.trail.draw(c, trailWidth);
                            Draw.z(z);
                        }
                    }

                    @Override
                    public void draw(Bullet b){
                        if(b.owner instanceof BlazeBuild){
                            Color c = hitColor.cpy().lerp(ShootOrange, b.fdata);
                            Color[] colors = {c.a(0.3f), c.a(0.7f), c.a(1), Color.white};
                            float fadeTime = 8f;
                            float fout2 = b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f;
                            for(int i = 0; i < colors.length; i++){
                                Draw.color(colors[i]);
                                Drawn.basicLaser(b.x, b.y, b.aimX, b.aimY, width * fout2 * b.fslope() * (1 - i * 0.12f) * (1f - oscMag + Mathf.absin(Time.time, oscScl, oscMag)));
                            }

                            Draw.z(Layer.bullet);
                            Draw.color(c);
                            Lines.stroke(2 * fout2 * (1 + Mathf.sin(Time.time, 12, 0.3f)));
                            float num = 3;
                            float charge = b.fdata;
                            float phaseOffset = 360 / num;
                            rand.setSeed(b.id);
                            for(int i = 0; i < num; i++){
                                float a = phaseOffset * i + Time.time * 0.5f;
                                Tmp.v1.trns(a, (1 - charge) * width + width / num);
                                float lx =/* Mathf.lerp(b.x, b.aimX, charge)*/ +b.aimX + Tmp.v1.x,
                                ly = b.aimY + Tmp.v1.y;
                                float random = rand.random(0.5f, 1f);
                                Drawn.drawSine2Modifier(b.x + Tmp.v1.x, b.y + Tmp.v1.y, lx, ly,
                                Time.time * 0.7f * random, 8, 0.8f,
                                phaseOffset * Mathf.degreesToRadians, width * 3f * (1 - 0.6f * charge) * random,
                                /*b.dst(b.aimX,b.aimY)*/ r / 10, ((x1, y1) -> {
                                    Fill.circle(x1, y1, Lines.getStroke());
                                }));
                                Fill.circle(lx, ly, Lines.getStroke());
                            }
                        }
                        Draw.reset();
                    }
                };

                consumeLiquid(Liquids.hydrogen, 45 / 2f / 60f);
                consumePower(800 / 60f);
            }

            @Override
            public void init(){
                super.init();
                buildType = BlazeBuild::new;
            }

            public class BlazeBuild extends ContinuousTurretBuild{
                public float charge = 0;
                public final float warmupTime = 220;

                @Override
                protected void updateBullet(BulletEntry entry){
                    super.updateBullet(entry);
                    if(isShooting() && hasAmmo()){
                        charge = Mathf.approachDelta(charge, 1, 1 / warmupTime * timeScale);
                    }else charge = Mathf.approachDelta(charge, 0, 0.1f);
                    entry.bullet.fdata = charge;
                }

                @Override
                public void read(Reads read, byte revision){
                    super.read(read, revision);
                    charge = read.f();
                }

                @Override
                public void write(Writes write){
                    super.write(write);
                    write.f(charge);
                }
            }
        };

        Lcarus = new EnhancedPowerTurret("Lcarus"){{
            requirements(Category.turret, with(WHItems.titaniumSteel, 90, Items.silicon, 80, Items.metaglass, 80, Items.graphite, 60));

            buildCostMultiplier = 5f;
            health = 3000;
            size = 3;
            recoil = 2;
            liquidCapacity = 60;
            range = 280;
            shootCone = 20;

            shootSound = shootLancer;
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
            shootSound = shootMissile;
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
            shootSound = shootMissile;
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
            shootSound = shootSpectre;
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
                shootSound = shootArc;
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
            shootSound = shootTank;

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
                shootSound = shootLancer;
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
                        colors = new Color[]{WHPal.SkyBlue.cpy().a(0.4f).lerp(Pal.techBlue.cpy(), 0.3f),
                        WHPal.SkyBlue.cpy().a(0.6f).lerp(Pal.techBlue.cpy(), 0.3f), WHPal.SkyBlueF.lerp(Pal.techBlue.cpy(), 0.3f)};
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
                    lifetime = 290 / 4f;
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
                    lifetime = 290 / 4f;
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
                    lifetime = 290 / 4f;
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
                    lifetime = 290 / 4f;
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
                    lifetime = 290 / 4f;
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

            requirements(Category.turret, with(Items.tungsten, 150, Items.phaseFabric, 100, Items.carbide, 80, WHItems.ceramite, 80, WHItems.molybdenumAlloy, 90));

            size = 4;
            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;
            shoot.firstShotDelay = 120;
            reload = 360f;
            range = 400f;
            recoil = 4;
            liquidCapacity = 100;
            coolantMultiplier = 3.5f;
            shootSound = WHSounds.hugeShoot;
            chargeSound = chargeLancer;
            rotateSpeed = 2.5f;
            cooldownTime = 110;
            shootY = 57 / 4f;
            consumePower(1500 / 60f);
            coolant = consumeCoolant(30 / 60f);
            maxAmmo = 6;
            ammoPerShot = 4;
            drawer = new DrawMulti(new DrawTurret(WarHammerMod.name("turret-")){{
                parts.add(new RegionPart("-mid"){{
                    heatColor = WHPal.SkyBlueF.cpy();
                    layerOffset = -0.01f;
                }});
            }});
            ammo(
            Items.phaseFabric, WHBullets.IonizePhaseFabricBullet,
            WHItems.resonantCrystal, WHBullets.IonizeResonantCrystalBullet);

        }};

        Pyros = new HeatTurret("Pyros"){
            {
                requirements(Category.turret, with(WHItems.titaniumSteel, 200, WHItems.ceramite, 300, WHItems.resonantCrystal, 120, WHItems.molybdenumAlloy, 150));
                buildCostMultiplier = 5f;
                outlineColor = WHPal.Outline;
                outlineRadius = 3;
                armor = 4;
                health = 4000;
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
                shootSound = shootLancer;
                chargeSound = chargeLancer;
                heatColor = WHPal.Heat;
                cooldownTime = 110;
                shootY = 12;
                velocityRnd = 0.1f;
                shoot.firstShotDelay = 60f;

                consumeLiquid(Liquids.slag, 60 / 60f);
                consumePower(2000 / 60f);

                drawer = new DrawMulti(new DrawTurret(WarHammerMod.name("turret-")));
                shootType = WHBullets.PyrosBullet;
                enhancedBullet = WHBullets.PyrosBulletEnhance;
            }
        };

        RoaringFlame = new ContinuousLiquidTurret("Roaring-flame"){{

            requirements(Category.turret, with(WHItems.titaniumSteel, 200, WHItems.ceramite, 200, WHItems.molybdenumAlloy, 180, WHItems.sealedPromethium, 50));

            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;
            size = 5;
            health = 7000;
            float r = range = 240;
            squareSprite = false;
            rotateSpeed = 0.9f;
            shootWarmupSpeed = 0.06f;
            minWarmup = 0.88f;
            heatColor = WHPal.Heat;
            cooldownTime = 120f;
            ammoPerShot = 4;

            liquidCapacity = 50f;
            liquidConsumed = 25 / 60f;
            targetInterval = 10f;
            newTargetInterval = 30f;
            targetUnderBlocks = false;
            shootY = 73 / 4f;

            loopSound = Sounds.shootSublimate;
            shootSound = Sounds.none;
            loopSoundVolume = 1f;

            scaledHealth = 300;

            shoot = new ShootAlternate(){{
                firstShotDelay = 20f;
                barrels = 2;
                spread = 38 / 4f;
                shots = 2;
            }};

            drawer = new DrawMulti(new DrawTurret(WarHammerMod.name("turret-")){{
                parts.addAll(
                new RegionPart("-barrel"){{
                    mirror = false;
                    under = true;
                    moveY = -5f;
                    heatColor = Pal.turretHeat;
                    heatProgress = PartProgress.recoil;
                }},
                new RegionPart("-side1"){{
                    mirror = true;
                    under = true;
                    moveX = -12 / 4f;
                    moveY = -6 / 4f;
                    heatColor = Pal.turretHeat;
                    heatProgress = PartProgress.warmup;
                }},
                new RegionPart("-side2"){{
                    mirror = true;
                    under = true;
                    moveX = -12 / 4f;
                    moveY = -6 / 4f;
                    heatColor = Pal.turretHeat;
                    heatProgress = PartProgress.warmup;
                }}
                );
            }});

            ammo(
            Liquids.hydrogen, new ContinuousFlameBulletType(){{
                damage = 50;
                rangeChange = -24f;
                length = r + rangeChange;
                pierceCap = 2;
                pierceArmor = true;
                buildingDamageMultiplier = 0.1f;
                timescaleDamage = true;
                width = 3;

                colors = new Color[]{Color.valueOf("7A8EFFFF").a(0.55f), Color.valueOf("5E81FFFF").a(0.7f),
                Color.valueOf("3F83E0FF").a(0.8f), Color.valueOf("2EA1FFFF"), Color.white};

                flareColor = Color.valueOf("2EA1FFFF");

                lightColor = hitColor = flareColor;
            }},
            WHLiquids.orePromethium, new ContinuousFlameBulletType(){{
                damage = 70;
                length = r;
                knockback = 1f;
                pierceCap = 2;
                pierceArmor = true;
                buildingDamageMultiplier = 0.3f;
                timescaleDamage = true;
                width = 3;

                colors = new Color[]{Color.valueOf("FFB398FF").a(0.55f), Color.valueOf("E5976EFF").a(0.7f),
                Color.valueOf("D48A4DFF").a(0.8f), Color.valueOf("EB955EFF"), Color.white};

                flareColor = Color.valueOf("EB955EFF");

                lightColor = hitColor = flareColor;
            }},

            WHLiquids.refinePromethium, new LightingContinuousFlameBulletType(){{
                damage = 150f;
                rangeChange = 40f;
                length = r + rangeChange;
                knockback = 2f;
                pierceCap = 5;
                pierceArmor = true;
                buildingDamageMultiplier = 0.6f;
                timescaleDamage = true;
                width = 3;

                colors = new Color[]{Color.valueOf("FF6947FF").a(0.55f),
                Color.valueOf("FF8B37FF").a(0.7f), Color.valueOf("FEB938FF").a(0.8f),
                Color.valueOf("F6FF66FF"), Color.white};
                flareColor = Color.valueOf("F6FF66FF").lerp(Pal.slagOrange, 0.2f);

                lightColor = hitColor = flareColor;
            }});

            researchCostMultiplier = 0.5f;
        }};

        Collapse = new ShootMatchTurret("Collapse"){{

            requirements(Category.turret, with(WHItems.titaniumSteel, 200, WHItems.ceramite, 200, WHItems.molybdenumAlloy, 180, WHItems.sealedPromethium, 50));

            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;

            size = 5;
            health = 4500;
            canOverdrive = false;
            shootSound = shootMissilePlasmaShort;
            unitSort = UnitSorts.farthest;
            heatColor = WHPal.Heat;

            cooldownTime = 180f;
            shootY = 14f;
            shake = 1f;
            recoil = 4.5f;
            recoilTime = 180f;
            ammoPerShot = 4;
            range = 450;
            reload = 420;
            coolantMultiplier = 1.5f;

            rotateSpeed = 1.2f;

            shootWarmupSpeed = 0.08f;
            minWarmup = 0.85f;
            warmupMaintainTime = 480;
            moveWhileCharging = false;
            shootCone = 20f;
            trackingRange = range * 1.3f;

            consumePower(1200 / 60f);
            coolant = consumeCoolant(30 / 60f);

            shoot = new ShootPattern(){{
                firstShotDelay = 60f;
            }};
            drawer = new DrawTurret(WarHammerMod.name("turret-")){{
                parts.add(
                new RegionPart("-barrel"){{
                    under = true;
                    progress = PartProgress.recoil;
                    moveY = -8f;
                }},
                new CollapsePart(){{
                    x = 0;
                    y = 14;
                    radius = 5;
                    layer = Layer.effect;
                    particleLen = 16f;
                    progress = PartProgress.warmup;
                }});
            }};

            ammo(
            WHItems.resonantCrystal, WHBullets.CollapseResonantCrystal,
            WHItems.sealedPromethium, WHBullets.CollapseSealedPromethium
            );
        }};

        CycloneMissleLauncher = new ShootMatchTurret("Cyclone-missile-launcher"){{

            requirements(Category.turret, with(WHItems.titaniumSteel, 800, WHItems.resonantCrystal, 150, WHItems.ceramite, 400, WHItems.molybdenumAlloy, 300, WHItems.sealedPromethium, 150, WHItems.adamantium, 90));

            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;
            health = 8000;

            predictTarget = false;

            recoil = 3f;

            fogRadiusMultiplier = 0.4f;
            shootSound = shootScathe;

            minWarmup = 0.94f;
            newTargetInterval = 40f;
            unitSort = UnitSorts.strongest;
            shootWarmupSpeed = 0.03f;
            targetAir = false;
            targetUnderBlocks = false;

            shake = 6f;
            ammoPerShot = 20;
            maxAmmo = ammoPerShot * 3;
            size = 5;
            envEnabled |= Env.space;
            reload = 750f;
            range = 1100;
            shootCone = 1f;
            scaledHealth = 220;
            rotateSpeed = 0.9f;

            coolant = consumeCoolant(120 / 60f);
            consumeLiquid(WHLiquids.refinePromethium, 15 / 60f);
            consumePower(3600 / 60f);
            coolantMultiplier = 1f;
            moveWhileCharging = false;
            limitRange();

            shootY = 72 / 4f;

            shoot = new ShootBarrel(){{
                shots = 2;
                shotDelay = 18f;
                barrels = new float[]
                {-44 / 4f, 72 / 4f, 0f,
                44 / 4f, 72 / 4f, 0f};
            }};
            drawer = new DrawTurret(WarHammerMod.name("turret-")){{
                parts.add(
                new RegionPart("-move"){{
                    under = true;
                    progress = PartProgress.warmup;
                    moveY = -56 / 4f;
                }},
                new RegionPart("-missile-part"){{
                    x = -43 / 4f;
                    y = 29 / 4f - 15f;
                    progress = PartProgress.reload.curve(Interp.pow2In);

                    colorTo = new Color(1f, 1f, 1f, 0f);
                    color = Color.white;
                    mixColorTo = Pal.accent;
                    mixColor = new Color(1f, 1f, 1f, 0f);
                    outline = false;

                    layerOffset = -0.01f;

                    moves.add(new PartMove(PartProgress.warmup, 0f, 15f, 0f));
                }},
                new RegionPart("-missile-part"){{
                    x = 43 / 4f;
                    y = 29 / 4f - 15f;

                    progress = PartProgress.reload.curve(Interp.pow2In);

                    colorTo = new Color(1f, 1f, 1f, 0f);
                    color = Color.white;
                    mixColorTo = Pal.accent;
                    mixColor = new Color(1f, 1f, 1f, 0f);
                    outline = false;

                    layerOffset = -0.01f;

                    moves.add(new PartMove(PartProgress.warmup, 0f, 15f, 0f));
                }});
            }};

            ammo(
            Items.carbide, WHBullets.CycloneMissleLauncherMissile1,
            WHItems.sealedPromethium, WHBullets.CycloneMissleLauncherMissile2,
            WHItems.refineCeramite, WHBullets.CycloneMissleLauncherMissile3
            );

            shooter(Items.carbide, new ShootBarrel(){{
                shots = 4;
                shotDelay = 18f;
                barrels = new float[]
                {-44 / 4f, 72 / 4f, 0f,
                44 / 4f, 72 / 4f, 0f};
            }},
            WHItems.sealedPromethium, new ShootBarrel(){{
                shots = 1;
                barrels = new float[]
                {-44 / 4f, 72 / 4f, 0f,
                44 / 4f, 72 / 4f, 0f};
            }});

            researchCostMultiplier = 0.5f;
        }};

        Crumble = new ShootMatchTurret("Crumble"){{
            requirements(Category.turret, with(Items.carbide, 250, WHItems.molybdenumAlloy, 150, WHItems.ceramite, 200, WHItems.refineCeramite, 100));

            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;
            health = 9000;

            size = 5;
            range = 460;
            squareSprite = false;
            shootSound = Sounds.shootQuad;
            unitSort = UnitSorts.farthest;

            reload = 450;
            rotateSpeed = 1.2f;

            shootWarmupSpeed = 0.03f;
            minWarmup = 0.88f;
            shootY = 15;
            heatColor = WHPal.Heat;
            cooldownTime = 180;
            inaccuracy = 3;
            xRand = 1;
            recoil = 6;
            recoilTime = 160;
            velocityRnd = 0.12f;
            maxAmmo = 40;
            ammoPerShot = 8;

            fogRadiusMultiplier = 0.4f;
            liquidCapacity = 60;
            coolantMultiplier = 0.8f;
            consumePower(2400 / 60f);
            coolant = consumeCoolant(90 / 60f);

            shoot = new ShootMulti(
            new ShootAlternate(){{
                spread = 26 / 4f;
            }},
            new ShootAlternate(){{
                spread = 10 / 4f;
                shotDelay = 10;
                shots = 4;
            }}
            /*  new ShootHelix(12,0.2f, 4)*/
            );

            drawer = new DrawTurret(WarHammerMod.name("turret-")){{
                parts.addAll(
                new CrumblePart(){{
                    progress = PartProgress.warmup;
                    layer = Layer.effect;
                    y = -17;
                }}
                );
            }};

            ammo(
            WHItems.ceramite, WHBullets.CrumbleCeramiteBullet);

            researchCostMultiplier = 0.4f;
        }};


        Sacrament = new ShootMatchTurret("Sacrament"){{
            requirements(Category.turret, with(WHItems.ceramite, 500, WHItems.refineCeramite, 200
            , WHItems.resonantCrystal, 200, WHItems.protocolChip, 400));

            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;
            health = 7000;

            size = 5;
            range = 700;
            squareSprite = false;
            shootSound = shootForeshadow;
            unitSort = UnitSorts.strongest;

            reload = 300;
            rotateSpeed = 1.5f;

            shootY = 32 / 4f;
            heatColor = WHPal.Heat;
            cooldownTime = 180;
            recoil = 3;
            recoilTime = 180;
            maxAmmo = 45;
            ammoPerShot = 9;

            fogRadiusMultiplier = 0.4f;
            liquidCapacity = 120;
            coolantMultiplier = 0.8f;
            consumePower(2000 / 60f);
            coolant = consumeCoolant(90 / 60f);

            shoot.firstShotDelay = 60f;

            drawer = new DrawTurret(WarHammerMod.name("turret-")){{
                parts.add(
                new RegionPart("-barrel"){{
                    mirror = false;
                    under = true;
                    moveY = -8;
                    progress = PartProgress.recoil;
                    heatProgress = PartProgress.recoil;
                    heatColor = WHPal.Heat;
                }},
                new RegionPart("-side"){{
                    mirror = true;
                    x = 0;
                    y = 0;
                    moveRot = -15;
                    moveY = -1f;
                    moveX = 2f;
                    moves.add(new PartMove(PartProgress.recoil, 1, 0, -30));
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.recoil;
                    heatColor = WHPal.Heat;
                }});
            }};


            ammo(
            WHItems.sealedPromethium, WHBullets.SacramentSealedPromethium,
            WHItems.molybdenumAlloy, WHBullets.SacramentMolybdenumAlloy,
            WHItems.refineCeramite, WHBullets.SacramentRefineCeramite);

            researchCostMultiplier = 0.4f;

        }};

        Colossus = new ShootMatchTurret("Colossus"){{

            requirements(Category.turret, with(WHItems.titaniumSteel, 200, Items.carbide, 200, WHItems.ceramite, 250, WHItems.molybdenumAlloy, 100, WHItems.resonantCrystal, 80, WHItems.sealedPromethium, 50));
            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;

            health = 7500;
            size = 5;
            reload = 360;
            range = 560;
            shake = 2;
            recoil = 5;
            recoilTime = 120;
            rotateSpeed = 0.8f;
            targetAir = false;
            heatColor = Pal.turretHeat;
            cooldownTime = 200;

            ammoPerShot = 6;
            maxAmmo = ammoPerShot * 4;

            inaccuracy = 3;
            shootY = 11;
            shootSound = Sounds.shootSmite;

            shoot = new ShootMulti(
            new ShootPattern(){{
                firstShotDelay = 30;
            }},
            new ShootSpread(4, 3){{
                shotDelay = 12;
            }}
            );
            shootWarmupSpeed = 0.019f;
            minWarmup = 0.88f;
            warmupMaintainTime = 300;

            velocityRnd = 0.1f;
            coolantMultiplier = 0.7f;
            liquidCapacity = 60;
            consumePower(900 / 60f);
            coolant = consumeCoolant(45 / 60f);

            ammoEjectBack = 14;
            ammoUseEffect = new MultiEffect(
            new Effect(120, e -> {
                color(Pal.lightOrange, Pal.lightishGray, Pal.lightishGray, e.fin());
                alpha(e.fout(0.5f));
                float rot = Math.abs(e.rotation) - 90;

                float len = (4f + e.finpow() * 20);
                float lr = rot - 90 + Mathf.randomSeed(e.id + 1145, -15f * e.fin(), 15f * e.fin());

                Draw.rect(Core.atlas.find("casing"),
                e.x + trnsx(lr, len) + Mathf.randomSeedRange(e.id + 7, 3f * e.fin()),
                e.y + trnsy(lr, len) + Mathf.randomSeedRange(e.id + 8, 3f * e.fin()),
                5, 15, rot);


                e.scaled(30, a -> {
                    color(Pal.lighterOrange, Color.gray, a.fin());
                    randLenVectors(a.id, 4, 30, a.rotation - 180, 30f * a.fin(), (x, y) -> {
                        Fill.circle(a.x + x, a.y + y, a.fout() * 5f);
                    });
                });
            }));

            drawer = new DrawTurret(WarHammerMod.name("turret-")){{
                parts.add(
                new RegionPart("-b"){{
                    mirror = false;
                    under = true;
                    moveY = 3;
                    moves.add(new PartMove(PartProgress.recoil, 0, -6, 0));
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.recoil;
                    heatColor = WHPal.Heat;
                }},
                new RegionPart("-side-l"){{
                    mirror = false;
                    x = 0;
                    y = 0;
                    moveX = -2.4f;
                    moveY = -3;
                    moves.add(new PartMove(PartProgress.recoil, 0, 0, 0));
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.recoil;
                    heatColor = WHPal.Heat;
                }},
                new RegionPart("-side-r"){{
                    mirror = false;
                    x = 0;
                    y = 0;
                    moveX = 2.4f;
                    moveY = -3;
                    moves.add(new PartMove(PartProgress.recoil, 0, 0, 0));
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.recoil;
                    heatColor = WHPal.Heat;
                }});
            }};

            ammo(WHItems.ceramite, WHBullets.ColossusCeramite,
            WHItems.molybdenumAlloy, WHBullets.ColossusMolybdenumAlloy,
            WHItems.refineCeramite, WHBullets.ColossusRefineCeramite);

            shooter(
            WHItems.molybdenumAlloy, new ShootPattern(){{
                shots = 2;
                shotDelay = 30;
                firstShotDelay = 30f;
            }},
            WHItems.refineCeramite, new ShootPattern(){{
                firstShotDelay = 30f;
            }});

        }};

        Melta = new LaserBeamTurret("Melta"){{
            requirements(Category.turret, with(Items.surgeAlloy, 600, WHItems.titaniumSteel, 1000, WHItems.protocolChip, 300,
            WHItems.molybdenumAlloy, 500, WHItems.vibranium, 500, WHItems.refineCeramite, 250, WHItems.adamantium, 100));

            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;

            health = 16000;
            armor = 20;
            size = 6;
            reload = 380;
            float r = range = 42 * tilesize;
            /*shootSound = Sounds.railgun;*/
            shootSound = Sounds.shootMeltdown;
            loopSound = Sounds.beamMeltdown;
            unitSort = UnitSorts.strongest;
            shake = 5;
            recoil = 5;
            recoilTime = 30;
            rotateSpeed = 0.8f;


            newTargetInterval = 40f;
            shootWarmupSpeed = 0.07f;
            warmupMaintainTime = 120f;
            minWarmup = 0.88f;

            squareSprite = false;

            shoot.firstShotDelay = 35;
            heatColor = WHPal.Heat.cpy().lerp(Pal.meltdownHit, 0.5f);

            cooldownTime = 300;
            scaledHealth = 400;
            liquidCapacity = 60;
            shootY = 24;
            coolantMultiplier = 1.3f;
            consumePower(4000 / 60f);


            coolant = consume(new ConsumeCoolant(120 / 60f){{
                filter =
                liquid -> liquid.coolant && liquid != cost && (this.allowLiquid && !liquid.gas || this.allowGas && liquid.gas)
                && liquid.temperature <= maxTemp && liquid.flammability < maxFlammability && liquid.heatCapacity >= Liquids.cryofluid.heatCapacity;
            }});


            float d = shootDuration = 200;

            shootType = new LaserBeamBulletType(){
                {
                    lifetime = d;
                    damage = 250;
                    damageInterval = 6;
                    damageMult = 8;
                    length = r / 1.3f;
                    width = 25;
                    colors = new Color[]{Color.valueOf("ec745855"), Color.valueOf("ec7458aa"), Color.valueOf("ff9c5a"), Color.white};
                    hitEffect = Fx.hitMeltdown;
                    hitColor = Pal.meltdownHit;
                    status = StatusEffects.melting;
                    statusDuration = 60;
                    timescaleDamage = true;
                    pierceCap = 3;
                    incendAmount = 1;
                    incendSpread = 5;
                    incendChance = 0.4f;
                    buildingDamageMultiplier = 0.1f;
                    ammoMultiplier = 1;
                }

                public final BulletType create = new ArtilleryBulletType(0, 100){
                    {
                        splashDamage = damage;
                        splashDamageRadius = 56;
                        instantDisappear = true;
                        despawnEffect = hitEffect =
                        new MultiEffect(
                        WHFx.generalExplosion(30, Pal.meltdownHit, splashDamageRadius, 15, false),
                        WHFx.square(Pal.meltdownHit, 60, 10, splashDamageRadius, 5));
                        status = WHStatusEffects.melta;
                        statusDuration = 60;
                    }
                };

                @Override
                public void update(Bullet b){
                    super.update(b);
                    if(b.timer(2, damageInterval)){
                        WHFx.hitSpark(Pal.meltdownHit, 30, 8, 30, 1.4f, 8)
                        .at(b.x, b.y);
                    }
                }

                @Override
                public void hitEntity(Bullet b, Hitboxc entity, float health){
                    super.hitEntity(b, entity, health);
                    if(b.timer(2, damageInterval * 1.5f) && entity instanceof Healthc){
                        create.create(b.owner, b.team, entity.getX(), entity.getY(), 0, -1, 0, 1, null);
                    }
                }
            };
            drawer = new DrawTurret(WarHammerMod.name("turret-")){{
                parts.addAll(
                new MeltaPart(),
                new HaloPart(){{
                    sides = 4;
                    hollow = true;
                    y = -26;
                    shapes = 1;
                    stroke = 0;
                    strokeTo = 2f;
                    radius = radiusTo = 9;
                    rotateSpeed = 2;
                    shapeRotation = 45;
                    color = colorTo = Pal.meltdownHit;
                    layer = Layer.effect;
                }},
                new HaloPart(){{
                    sides = 4;
                    hollow = true;
                    y = -26;
                    shapes = 1;
                    stroke = 0;
                    strokeTo = 2f;
                    radius = radiusTo = 5;
                    rotateSpeed = -1;
                    shapeRotation = 45;
                    color = colorTo = Pal.meltdownHit;
                    layer = Layer.effect;
                }}
                );
            }};

            researchCostMultiplier = 0.4f;
        }};

        Reckoning = new ReckoningTurret("Reckoning"){{
            requirements(Category.turret, with(Items.carbide, 300, WHItems.titaniumSteel, 900, WHItems.protocolChip, 200,
            WHItems.molybdenumAlloy, 200, WHItems.vibranium, 300, WHItems.refineCeramite, 100));

            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;

            health = 18000;
            size = 6;
            armor = 30;

            shootSound = shootSpectre;
            loopSoundVolume = 0.05f;
            range = 47 * tilesize;

            shootCone = 12f;
            shake = 0.5f;
            inaccuracy = 5;
            xRand = 2;
            velocityRnd = 0.21f;
            squareSprite = false;
            rotateSpeed = 1.5f;
            reload = 60;
            ammoPerShot = 4;
            maxAmmo = ammoPerShot * 12;
            cooldownTime = 30f;

            liquidCapacity = 300;

            coolantMultiplier = 2;
            coolant = consumeCoolant(150 / 60f);

            shootY = 90 / 4f;
            recoil = 3;
            recoilTime = 60;

            shoot = new ShootMulti(
            new ShootAlternate(){{
                shots = 1;
                spread = 51 * 2 / 4f;
            }},
            new ShootPattern(){{
                shots = 2;
                shotDelay = 6;
            }}
            );

            ammo(
            Items.tungsten, WHBullets.ReckoningTungsten,
            WHItems.ceramite, WHBullets.ReckoningCeramite,
            WHItems.molybdenumAlloy, WHBullets.ReckoningMolybdenumAlloy,
            WHItems.sealedPromethium, WHBullets.ReckoningSealedPromethium
            );

            researchCostMultiplier = 0.4f;
        }};

        Hydra = new ShootMatchTurret("Hydra"){{
            requirements(Category.turret, with(Items.surgeAlloy, 500, WHItems.protocolChip, 200, WHItems.titaniumSteel, 600,
            WHItems.resonantCrystal, 200, WHItems.ceramite, 600, WHItems.vibranium, 300, WHItems.refineCeramite, 100));

            buildCostMultiplier = 5f;
            outlineColor = WHPal.Outline;
            outlineRadius = 3;

            health = 12000;
            size = 6;
            armor = 20;

            targetGround = false;
            shootSound = Sounds.shootSmite;
            loopSoundVolume = 0.05f;
            range = 58 * tilesize;

            shootCone = 20f;

            shake = 0.5f;
            inaccuracy = 3;
            velocityRnd = 0.21f;
            squareSprite = false;
            rotateSpeed = 2f;
            heatColor = WHPal.Heat.cpy().lerp(Color.sky, 0.7f);
            cooldownTime = 70;
            reload = 80;
            ammoPerShot = 6;
            maxAmmo = ammoPerShot * 12;

            liquidCapacity = 60;

            coolantMultiplier = 3;
            consumePower(2400 / 60f);
            coolant = consumeCoolant(60 / 60f);

            shootY = 0;
            recoil = 0;
            recoilTime = 24;

            shoot = new ShootBarrel(){{
                shots = 4;
                shotDelay = 13;
                barrels = new float[]{
                -56 / 4f, 80 / 4f, 0,
                -17 / 4f, 92 / 4f, 0,
                17 / 4f, 92 / 4f, 0,
                56 / 4f, 80 / 4f, 0
                };
            }};

            recoils = 4;
            drawer = new DrawTurret(WarHammerMod.name("turret-")){{
                for(int i = 0; i < recoils; i++){
                    int f = i;
                    int a = i + 1;
                    parts.addAll(
                    new RegionPart("-barrel-" + a){{
                        under = true;
                        moveY = -7;
                        heatProgress = progress = PartProgress.recoil.curve(Interp.smooth);
                        heatColor = WHPal.Heat.cpy().lerp(Pal.turretHeat.cpy(), 0.5f);
                        recoilIndex = f;
                    }});
                }
            }};

            ammo(
            Items.tungsten, WHBullets.HydraTungsten,
            WHItems.ceramite, WHBullets.HydraCeramite,
            WHItems.molybdenumAlloy, WHBullets.HydraMolybdenumAlloy,
            WHItems.refineCeramite, WHBullets.HydraRefineCeramite
            );
            ShootPattern ceShoot = shoot.copy();
            ceShoot.shots = 5;
            ShootPattern moShoot = shoot.copy();
            moShoot.shots = 2;
            ShootPattern reCShoot = shoot.copy();
            reCShoot.shots = 2;

            shooter(
            WHItems.ceramite, ceShoot,
            WHItems.molybdenumAlloy, moShoot,
            WHItems.refineCeramite, reCShoot
            );

            researchCostMultiplier = 0.4f;

        }};

        Annihilate = new PowerTurret("Annihilate"){
            {
                requirements(Category.turret, with(WHItems.titaniumSteel, 800, Items.surgeAlloy, 800, WHItems.vibranium, 150,
                WHItems.molybdenumAlloy, 500, WHItems.resonantCrystal, 200, WHItems.sealedPromethium, 400, WHItems.refineCeramite, 200));

                buildCostMultiplier = 5f;
                outlineColor = WHPal.Outline;
                outlineRadius = 3;

                health = 16000;
                size = 6;
                armor = 20;
                range = 66 * tilesize;
                reload = 540;
                shootY = 90 / 4f;
                rotateSpeed = 0.9f;
                recoil = 4;
                shake = 3;

                moveWhileCharging = true;

                minWarmup = 0.88f;
                shootWarmupSpeed = 0.02f;
                warmupMaintainTime = 300;
                cooldownTime = recoilTime = 180;

                liquidCapacity = 60;

                coolantMultiplier = 0.8f;
                consumePower(7000 / 60f);
                consumeLiquid(Liquids.hydrogen, 30 / 60f);
                coolant = consumeCoolant(90 / 60f);

                shoot.firstShotDelay = 60;
                shootSound = shootCorvus;
                soundPitchMin = 0.8f;
                soundPitchMax = 0.9f;

                drawer = new DrawTurret(WarHammerMod.name("turret-")){{
                    parts.addAll(
                    new DrawArrowSequence("-arrow"){{
                        x = y = 0;
                        layer = Layer.effect;
                        spacing = 16 / 4f;
                        arrows = 3;
                        progress = colorProgress = PartProgress.warmup.curve(Interp.smooth);
                        color = WHPal.SkyBlue.cpy().lerp(Color.white, 0.5f);
                        colorTo = WHPal.SkyBlueF.cpy().lerp(Color.white, 0.5f);
                    }});
                }};

                shootType = WHBullets.AnnihilateBullet;

                researchCostMultiplier = 0.4f;

            }

            @Override
            public void setStats(){
                super.setStats();
                stats.remove(Stat.ammo);
                stats.add(Stat.ammo, UIUtils.ammo(ObjectMap.of(this, shootType)));
            }
        };

        Erase = new ShootMatchTurret("Erase"){
            {
                requirements(Category.turret, with(WHItems.titaniumSteel, 800, Items.surgeAlloy, 800, WHItems.protocolChip, 300, WHItems.ceramite, 800,
                WHItems.molybdenumAlloy, 500, WHItems.resonantCrystal, 800, WHItems.adamantium, 200, WHItems.sealedPromethium, 300));
                buildCostMultiplier = 5f;
                outlineColor = WHPal.Outline;
                outlineRadius = 3;

                health = 17500;
                size = 6;
                reload = 630;
                range = 82 * tilesize;
                shake = 2;
                recoil = 5;
                rotateSpeed = 0.8f;
                targetAir = false;
                heatColor = Pal.turretHeat;
                recoilTime = cooldownTime = 200;

                ammoPerShot = 12;
                maxAmmo = ammoPerShot * 4;

                shootSound = shootTank;
                soundPitchMin = 0.8f;
                soundPitchMax = 1f;

                inaccuracy = 0.5f;
                shootY = 84 / 4f;
                shoot.firstShotDelay = 60;

                minWarmup = 0.95f;
                newTargetInterval = 40f;
                shootWarmupSpeed = 0.02f;
                warmupMaintainTime = 120f;

                velocityRnd = 0.1f;
                coolantMultiplier = 0.4f;
                liquidCapacity = 60;
                consumePower(4000 / 60f);
                coolant = consumeCoolant(120 / 60f);

                ammoEjectBack = 19;
                ammoUseEffect = new MultiEffect(
                new Effect(120, e -> {
                    color(Pal.lightOrange, Pal.lightishGray, Pal.lightishGray, e.fin());
                    alpha(e.fout(0.5f));
                    float rot = Math.abs(e.rotation) - 90;

                    float len = (4f + e.finpow() * 20);
                    float lr = rot - 90 + Mathf.randomSeed(e.id + 1145, -15f * e.fin(), 15f * e.fin());

                    Draw.rect(Core.atlas.find("casing"),
                    e.x + trnsx(lr, len) + Mathf.randomSeedRange(e.id + 7, 3f * e.fin()),
                    e.y + trnsy(lr, len) + Mathf.randomSeedRange(e.id + 8, 3f * e.fin()),
                    5, 20, rot);
                }));

                var haloProgress = PartProgress.warmup.delay(0.5f);
                float haloY = -15f, haloRotSpeed = 1f, haloRad = 16f;
                Color c = ShootOrange.cpy().lerp(Pal.slagOrange, 0.2f);

                drawer = new DrawTurret(WarHammerMod.name("turret-")){{
                    parts.addAll(
                    new RegionPart("-barrel"){{
                        mirror = false;
                        layerOffset = -0.0002f;
                        moveY = -8;
                        progress = heatProgress = PartProgress.recoil.curve(Interp.smooth);
                        heatColor = Pal.turretHeat.cpy().lerp(Pal.lighterOrange, 0.2f);
                    }},
                    new RegionPart("-side-barrel"){{
                        mirror = true;
                        under = true;
                        x = 0;
                        y = 0;
                        moveY = -5;
                        progress = heatProgress = PartProgress.recoil.curve(Interp.pow5In);
                        heatColor = WHPal.Heat.cpy().lerp(Pal.lighterOrange, 0.3f);
                    }},
                    new RegionPart("-side"){{
                        mirror = true;
                        under = true;
                        x = 0;
                        y = 0;
                        moveX = 1;
                        moveRot = 3;
                        moves.add(new PartMove(PartProgress.recoil, -moveX, -4, -10));
                        progress = PartProgress.warmup;
                        heatProgress = PartProgress.warmup;
                        heatColor = WHPal.Heat;
                    }},
                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.2f);
                        color = c;
                        circle = true;
                        hollow = true;
                        stroke = 0f;
                        strokeTo = 2f;
                        radius = 10f;
                        layer = Layer.effect;
                        y = haloY;
                        rotateSpeed = haloRotSpeed;
                    }},
                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.2f);
                        color = c;
                        hollow = true;
                        stroke = 0f;
                        strokeTo = 2;
                        radius = 8;
                        layer = Layer.effect;
                        y = haloY;
                        rotateSpeed = haloRotSpeed;
                    }},
                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.2f);
                        color = c;
                        circle = true;
                        hollow = true;
                        stroke = 0f;
                        strokeTo = 1.6f;
                        radius = 4f;
                        layer = Layer.effect;
                        y = haloY;
                        rotateSpeed = haloRotSpeed;
                    }},
                    //side
                    new HaloPart(){{
                        progress = PartProgress.warmup.blend(p -> Mathf.sin(12f, 1f) * p.warmup, 0.1f);
                        color = c;
                        layer = Layer.effect;
                        y = haloY;

                        haloRotation = 90f;
                        shapeMoveRot = 30;
                        shapes = 1;
                        mirror = true;
                        triLength = 0f;
                        triLengthTo = 23f;
                        haloRadius = haloRad;
                        tri = true;
                        radius = 6f;
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.blend(p -> Mathf.sin(12f, 1f) * p.warmup, 0.1f);
                        color = c;
                        layer = Layer.effect;
                        y = haloY;

                        haloRotation = 90f;
                        shapeMoveRot = 30;
                        shapes = 1;
                        mirror = true;
                        triLength = 0f;
                        triLengthTo = 7f;
                        haloRadius = haloRad;
                        tri = true;
                        radius = 6f;
                        shapeRotation = 180f;
                    }},
                    //sideTilt
                    new HaloPart(){{
                        progress = haloProgress;
                        color = c;
                        layer = Layer.effect;
                        y = haloY;

                        haloRotation = 35;
                        shapes = 2;
                        triLength = 0f;
                        triLengthTo = 20f;
                        haloRadius = haloRad;
                        tri = true;
                        radius = 4f;
                    }},
                    new HaloPart(){{
                        progress = haloProgress;
                        color = c;
                        layer = Layer.effect;
                        y = haloY;

                        haloRotation = 35;
                        shapes = 2;
                        triLength = 0f;
                        triLengthTo = 5f;
                        haloRadius = haloRad;
                        tri = true;
                        radius = 4f;
                        shapeRotation = 180f;
                    }},
                    //surround
                    new HaloPart(){{
                        progress = haloProgress;
                        color = c;
                        layer = Layer.effect;
                        y = haloY;
                        haloRotateSpeed = haloRotSpeed;

                        shapes = 4;
                        sides = 4;
                        radius = 0;
                        radiusTo = 4;
                        haloRotation = 45f;
                        haloRadius = haloRad - 3;
                    }}
                    );
                }};

                ammo(
                WHItems.molybdenumAlloy, WHBullets.EraseMolybdenumAlloy,
                WHItems.adamantium, WHBullets.EraseAdamantium);

                shooter(
                WHItems.adamantium, new ShootSpread(){{
                    firstShotDelay = 60;
                    shots = 2;
                    spread = 10;
                    shotDelay = 40;
                }});

                researchCostMultiplier = 0.4f;
            }
        };

        //Test


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
                flameColor = WHPal.ShootOrange;
                midColor = WHPal.ShootOrange.cpy().lerp(Color.white, 0.1f);
            }}, new DrawDefault());

            size = 3;
            consumePowerCond(6f, AirRaiderBuild::isCharging);
            consumeItem(WHItems.sealedPromethium, 4);
            itemCapacity = 16;
            health = 4500;

            triggeredEffect = new Effect(45f, e -> {
                Draw.color(WHPal.ShootOrange);
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
            shootSound = shootLancer;
            coolant = consumeCoolant(0.2f);

            consumePower(6f);

            shootType = new LightingContinuousLaserBullet(){{
                colors = new Color[]{Pal.lancerLaser.cpy().a(0.4f), Pal.lancerLaser, Color.white};
                //TODO merge
                chargeEffect = trailCharge(Pal.lancerLaser, 20, 2, 90, 3, 60).layer(Layer.effect);
                buildingDamageMultiplier = 0.25f;
                hitEffect = Fx.hitLancer;
                hitSize = 4;
                lifetime = 200;
                damage = 1145;
                drawSize = 400f;
                hitColor = Pal.lancerLaser;
                collidesAir = false;
                length = 173f;
                ammoMultiplier = 1f;
                pierceCap = 4;
            }};
        }};
        sb7 = new MultReconstructor("草泥马reconstructor"){{
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
            /*   proximityRange = 2;*/
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
                shootSound = Sounds.shootMeltdown;
                loopSound = Sounds.beamMeltdown;
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
                        despawnSound = Sounds.explosionArtilleryShock;

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

        //来自EU
        randomer = new Randomer("randomer1"){{
            requirements(Category.distribution, with(Items.silicon, 1));
            alwaysUnlocked = true;
            buildVisibility = BuildVisibility.sandboxOnly;
        }};

    }
}