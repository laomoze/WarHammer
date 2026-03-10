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
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.payloads.*;
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
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;
import static mindustry.gen.Sounds.*;
import static mindustry.type.ItemStack.with;
import static wh.content.WHFx.*;
import static wh.graphics.Drawn.arcProcessFlip;
import static wh.graphics.WHPal.*;
import static wh.util.WHUtils.rand;

public final class WHBlocks{
    public final static float FACTORY_PAD_33 = 3;
    //factory
    public static Block
    scrapCrusher,
    manganeseSteelFurnace,
    arcKiln, multiPress, siliconMixFurnace, sandSeparator, scrapFurance,

    plastaniumCompressor, atmosphericSeparator,
    electrolyzer, coalCentrifuge, sporePress, cultivator,
    carbideCrucible, waterPurifier,

    T2sandSeparator, cobaltNitrideChamber, petroleumConverter,
    armorCompressor, LiquidNitrogenPlant,
    ceramiteSteelFoundry, cryofluidMixer, combustibleCrafter,
    entanglementSynthesizer, T2ManganeseSteelFurnace, promethiumRefinery,

    heatSiliconSmelter, T2WaterPurifier, combustibleSeparator,
    crystalEngraver, pressureReactionChamber,

    moSurgeSmelter, largeArmorSmelter, sealedPromethiumMill,

    T2ceramiteSteelFoundry, laserEngraver,

    ceramiteRefinery, slagfurnace, ADMill,
    //heat
    combustionHeater, slagHeatMaker, decayHeater, promethiumHeater,
    smallHeatRouter, heatBelt, heatBridge, T2heatBridge,
    tungstenConverter, molybdenumConverter, vibraniumConverter;
    //drill
    public static Block
    electronicPneumaticDrill, MechanicalQuarry, lavaDrill,
    heavyCuttingDrill, SpecialCuttingDrill, highEnergyDrill,
    heavyExtractor, strengthenOilExtractor,
    promethiumExtractor, slagExtractor, integratedCompressor;
    //liquid
    public static Block lightConduit, steelConduit, armorFluidRouter,
    armorFluidJunction, mixedFluidJunction,
    steelBridgeConduit, lowResistanceConduit,
    basicPump, steelPump, gravityPump,
    T2LiquidTank, armorLiquidTank;

    //distribution
    public static Block
    basicDust, steelDust,
    armorInvertedSorter, armorSorter, armorJunction, armorOverflowGate,
    armorUnderflowGate, armorRouter,
    basicBridge, lowResistanceBridge,
    ceramiteConveyor, armorCoverStackBelt, stackBridge,
    steelUnloader, trackDriver;

    //logic
    public static Block
    holographyMessage, switchBlock, juniorProcessor, instructionProcessor, logicDisplay,
    memoryCell, memoryBank, canvas;

    //power
    public static Block
    powerNode, t2PowerNode, compositeNode, armorPowerTower,
    ventDistiller, oxidationGenerator, turboGenerator,
    crackingGenerator, T2thermalGenerator,
    T2impactReactor, promethiunmRector, plaRector,

    smallBattery, smallBatteryRebel,
    midBattery, midBatteryRebel,
    largeBattery, largeBatteryRebel;

    //effect
    public static Block
    armoredVault, armoredContainer,
    wrapProjector, wrapOverdrive, shelterDome,
    repairTower, voidShield, ionShield,
    selectProjector,
    strongholdCore, T2strongholdCore, T3strongholdCore;

    //units
    public static Block airFactory, groundFactory, mechaFactory, tankFactory,
    t2Module, t3Module, t4Module, t5Module, t6Module, jumpBeacon, energyWarpGate,
    t2PayloadMassDriver,
    armorPayloadConveyor, armorPayloadRouter,
    serpuloT6Assembler;

    //walls
    public static Block primarySteelWall, largePrimarySteelWall, heavySteelWall, largeHeavySteelWall, heavySteelDoor,
    ceramiteWall, largeCeramiteWall, ceramiteDoor, refineCeramiteWall, largeRefineCeramiteWall, promethiumChargeWall,
    denseExplosionProofWall;

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
    public static Block randomer, sb1, sb2, sb3, sb6, sb7, sb10;


    private WHBlocks(){
    }

    public static void load(){
        scrapFurance = new GenericCrafter("scrap-furance"){{
            requirements(Category.crafting, with(WHItems.manganeseSteel, 30, WHItems.chromium, 40, Items.plastanium, 30));
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

        manganeseSteelFurnace = new GenericCrafter("manganese-steel-furnace"){
            {
                Color color = WHPal.MnSteelColor;
                requirements(Category.crafting, with(WHItems.manganese, 40));

                health = 300;
                hasItems = hasPower = true;
                craftTime = 60;
                itemCapacity = 10;
                size = 2;
                drawer = new DrawMulti(new DrawDefault(), new DrawFlame(color));
                consumePower(1);
                consumeItems(with(WHItems.manganese, 2, WHItems.chromium, 2));
                craftEffect = WHFx.square(MnSteelColor, 35f, 4, 16f, 4f);
                outputItem = new ItemStack(WHItems.manganeseSteel, 1);
                researchCostMultiplier = 0.2f;
            }
        };

        arcKiln = new GenericCrafter("arc-kiln"){
            {
                requirements(Category.crafting, with(WHItems.manganese, 30, Items.graphite, 30, Items.silicon, 40));

                size = 3;
                health = 360;
                itemCapacity = 40;
                hasPower = hasItems = true;
                craftTime = 120;

                consumePower(180f / 60f);
                consumeItems(with(WHItems.oreSand, 8, WHItems.manganese, 6));
                outputItem = new ItemStack(Items.metaglass, 10);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawFlame(){{
                    flameRadius = 0;
                }},
                new DrawArcSmelt(), new DrawDefault(),
                new DrawGlowRegion(){{
                    color = Color.valueOf("ffc999");
                    glowIntensity = 1f;
                    glowScale = 12;
                }},
                new LargekilnDrawer(Color.valueOf("ffc999")));

                ambientSound = loopSmelter;
                ambientSoundVolume = 0.11f;
                researchCostMultiplier = 0.8f;
            }
        };

        multiPress = new GenericCrafter("multi-press"){
            {
                requirements(Category.crafting, with(WHItems.manganese, 50, WHItems.chromium, 30));
                health = 500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 64;
                size = 3;
                consumePower(4);
                consumeItems(with(Items.coal, 10));
                consumeLiquid(WHLiquids.swageWater, 15 / 60f);
                outputItem = new ItemStack(Items.graphite, 8);
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(WHLiquids.swageWater),
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

        siliconMixFurnace = new GenericCrafter("silicon-mix-furnace"){
            {
                requirements(Category.crafting, with(WHItems.manganese, 40, WHItems.chromium, 20, Items.graphite, 30));

                size = 3;
                health = 600;
                craftTime = 120;
                itemCapacity = 60;
                hasPower = hasItems = true;
                consumePower(2f);
                outputItem = new ItemStack(Items.silicon, 8);
                consumeItems(with(Items.sand, 8, Items.graphite, 4));
                squareSprite = false;
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawArcSmelt(){{
                    circleStroke = 1.5f;
                    flameRadiusScl = 2.5f;
                    flameRadiusMag = 0.2f;
                }}, new DrawDefault());
                ambientSound = loopSmelter;
                ambientSoundVolume = 0.11f;
                researchCostMultiplier = 0.5f;
            }
        };

        sandSeparator = new GenericCrafter("sand-separator"){
            {
                requirements(Category.crafting, with(WHItems.chromium, 40, Items.graphite, 40, WHItems.manganeseSteel, 20));
                health = 300;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                liquidCapacity = itemCapacity = 40;
                size = 2;
                dumpExtraLiquid = true;
                ignoreLiquidFullness = true;
                consumePower(2);
                consumeItems(with(WHItems.oreSand, 6));
                outputItem = new ItemStack(Items.sand, 4);
                outputLiquid = new LiquidStack(Liquids.slag, 10 / 60f);
                drawer = new DrawMulti(new DrawDefault(),
                new DrawRegion("-rotator", -4){{
                    spinSprite = true;
                }},
                new DrawGlowRegion("-glow"){{
                    color = Pal.slagOrange.cpy().lerp(Pal.turretHeat, 0.3f);
                }},
                new DrawRegion("-top"));
                craftEffect = Fx.smokeCloud;
                updateEffect = new Effect(20, e -> {
                    color(Pal.gray, Color.lightGray, e.fin());
                    randLenVectors(e.id, 6, 3f + e.fin() * 6f, (x, y) ->
                    Fill.square(e.x + x, e.y + y, e.fout() * 2f, 45));
                });
                researchCostMultiplier = 0.5f;
            }
        };

        plastaniumCompressor = new GenericCrafter("plastanium-compressor"){
            {

                requirements(Category.crafting, with(WHItems.chromium, 80, Items.graphite, 120, WHItems.manganeseSteel, 110));
                size = 3;
                health = 650;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                liquidCapacity = 120;
                itemCapacity = 30;
                consumePower(10f);
                consumeLiquid(Liquids.oil, 1);
                consumeItems(with(WHItems.chromium, 8));
                outputItem = new ItemStack(Items.plastanium, 5);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawDefault(), new T2PlastaniumCompresserDrawer(WHPal.ShootOrange), new DrawFade());
                craftEffect = Fx.formsmoke;
                updateEffect = Fx.plasticburn;
                researchCostMultiplier = 0.6f;
            }
        };


        atmosphericSeparator = new HeatCrafter("atmospheric-separator"){
            {

                requirements(Category.crafting, with(Items.graphite, 50, Items.silicon, 100, WHItems.manganeseSteel, 70));

                hasItems = false;
                hasPower = hasLiquids = true;
                size = 3;
                health = 900;
                craftTime = 60;
                liquidCapacity = 100;
                updateEffect = Fx.none;
                ambientSound = Sounds.loopExtract;
                ambientSoundVolume = 0.06f;
                consumePower(2);
                heatRequirement = 8;
                maxEfficiency = 1;
                outputLiquid = new LiquidStack(Liquids.nitrogen, 20f / 60f);
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.nitrogen, FACTORY_PAD_33),
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


        electrolyzer = new GenericCrafter("electrolyzer"){
            {
                requirements(Category.crafting, with(WHItems.cobalt, 50, Items.metaglass, 40, Items.graphite, 40, WHItems.manganeseSteel, 30));

                size = 3;
                health = 750;
                hasPower = hasLiquids = true;
                craftTime = 60;
                liquidCapacity = 60;
                itemCapacity = 0;

                rotate = true;
                invertFlip = true;
                consumePower(2f);
                consumeLiquid(WHLiquids.swageWater, 1f);
                outputLiquids = LiquidStack.with(Liquids.ozone, 30f / 60, Liquids.hydrogen, 45f / 60);
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(WHLiquids.swageWater, FACTORY_PAD_33){{
                    alpha = 0.6f;
                }},
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

        coalCentrifuge = new GenericCrafter("coal-centrifuge"){{
            requirements(Category.crafting, with(WHItems.cobalt, 20, Items.graphite, 40, WHItems.molybdenumAlloy, 20));
            craftEffect = Fx.coalSmeltsmoke;
            outputItem = new ItemStack(Items.coal, 2);
            craftTime = 30f;
            size = 2;
            hasPower = hasItems = hasLiquids = true;

            drawer = new DrawMulti(
            new DrawDefault(),
            new DrawRegion("-rotator"){{
                rotateSpeed = -1.5f;
                spinSprite = true;
            }},
            new DrawRegion("-top"),
            new DrawGlowRegion(){{
                alpha = 0.7f;
                color = Color.valueOf("DEF3A9FF");
                glowIntensity = 0.3f;
                glowScale = 6f;
            }});

            consumeLiquid(Liquids.oil, 0.2f);
            consumePower(1);
        }};

        sporePress = new GenericCrafter("spore-press"){{

            requirements(Category.crafting, with(Items.silicon, 90, Items.plastanium, 40, WHItems.manganeseSteel, 30));
            health = 700;
            hasItems = hasPower = hasLiquids = true;
            craftTime = 60;
            itemCapacity = 40;
            liquidCapacity = 120;
            size = 3;
            consumePower(4);
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

        cultivator = new AttributeCrafter("cultivator"){{
            requirements(Category.crafting, with(WHItems.cobaltNitride, 40, Items.metaglass, 100, WHItems.manganeseSteel, 50));
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

        carbideCrucible = new HeatCrafter("carbide-crucible"){{

            requirements(Category.crafting, with(WHItems.chromium, 100, Items.graphite, 80, WHItems.manganeseSteel, 50));
            size = 3;
            health = 1200;
            itemCapacity = 30;
            craftTime = 120;
            heatRequirement = 8;
            maxEfficiency = 3f;
            hasItems = hasPower = true;
            consumePower(300 / 60f);
            consumeItems(with(Items.graphite, 4, Items.tungsten, 2));
            outputItem = new ItemStack(Items.carbide, 2);
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawCrucibleFlame(), new DrawDefault(), new DrawHeatInput());
            researchCostMultiplier = 0.45f;
        }};

        waterPurifier = new GenericCrafter("water-purifier"){
            {
                requirements(Category.crafting, with(WHItems.manganeseSteel, 50, WHItems.cobalt, 80, Items.plastanium, 50));
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

        armorCompressor = new GenericCrafter("armor-compressor"){{
            requirements(Category.crafting, with(WHItems.uranium, 50, WHItems.manganeseSteel, 50, WHItems.cobaltNitride, 50));

            size = 3;
            craftTime = 120f;
            health = 1200;
            hasItems = hasPower = true;
            itemCapacity = 20;
            consumePower(8f);
            consumeItems(with(WHItems.chromium, 2, Items.metaglass, 2, Items.carbide, 1));
            outputItem = new ItemStack(WHItems.armorAlloy, 2);
            drawer = new DrawMulti(new DrawDefault(),
            new DrawFlame(Items.surgeAlloy.color)
         /*   new DrawGlowRegion(){{
                color = Color.valueOf("FFDEB5FF");
                layer = Layer.effect;
                glowIntensity = 0.7f;
            }}*/);
            updateEffect = WHFx.hexagonSpread(Items.surgeAlloy.color, 5, 12f);
            craftEffect = WHFx.hexagonSmoke(Items.surgeAlloy.color, 30f, 1.2f, 10, 20f);
            researchCostMultiplier = 0.6f;
        }};

        ceramiteSteelFoundry = new GenericCrafter("ceramite-steel-foundry"){
            {
                Color color = CeramiteColor;
                requirements(Category.crafting, with(WHItems.chromium, 80, WHItems.uranium, 30));
                health = 300;
                hasItems = hasPower = true;
                craftTime = 120;
                itemCapacity = 20;
                size = 2;
                consumePower(4);
                consumeItems(with(Items.plastanium, 3, WHItems.cobalt, 3, Items.tungsten, 3));
                outputItem = new ItemStack(WHItems.ceramite, 2);
                drawer = new DrawMulti(new DrawDefault(), new DrawFlame(color));
                craftEffect = WHFx.square(CeramiteColor, 35f, 4, 16f, 4f);
            }
        };

        T2sandSeparator = new HeatProducer("large-sand-separator"){
            {
                requirements(Category.crafting, with(Items.graphite, 80, WHItems.manganeseSteel, 40, WHItems.ceramite, 50));
                health = 800;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 60;
                liquidCapacity = 90;
                size = 3;
                dumpExtraLiquid = true;
                ignoreLiquidFullness = true;
                consumePower(4);
                heatOutput = 2;
                consumeItems(with(WHItems.oreSand, 15));
                outputItem = new ItemStack(Items.sand, 12);
                outputLiquid = new LiquidStack(Liquids.slag, 40 / 60f);
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


        LiquidNitrogenPlant = new GenericCrafter("Liquid-nitrogen-plant"){{

            requirements(Category.crafting, with(WHItems.armorAlloy, 80, Items.plastanium, 60, WHItems.ceramite, 40));

            size = 2;
            craftTime = 60f;
            health = 550;
            hasPower = hasLiquids = true;
            liquidCapacity = 120;
            consumePower(4f);
            consumeLiquid(Liquids.nitrogen, 20 / 60f);
            consumeLiquid(Liquids.cryofluid, 30 / 60f);
            outputLiquid = new LiquidStack(WHLiquids.liquidNitrogen, 40 / 60f);
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

        cobaltNitrideChamber = new GenericCrafter("cobalt-nitride-chamber"){
            {
                requirements(Category.crafting, with(WHItems.cobalt, 40, Items.plastanium, 40, Items.metaglass, 40, WHItems.manganeseSteel, 40));
                size = 3;
                health = 750;
                hasPower = hasLiquids = true;
                craftTime = 120;
                liquidCapacity = 60;
                itemCapacity = 15;
                consumePower(4);
                consumeLiquid(Liquids.nitrogen, 20f / 4f / 60f);
                consumeItems(with(WHItems.cobalt, 3, Items.silicon, 2, WHItems.manganese, 2));
                outputItems = with(WHItems.cobaltNitride, 2);
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.nitrogen),
                new DrawArcSmelt(){{
                    flameColor = midColor = Liquids.nitrogen.color.cpy().lerp(Pal.techBlue, 0.3f);
                    flameRad = 2.5f;
                    circleSpace = 3;
                }},
                new DrawParticles(){{
                    color = Color.valueOf("d4f0ff");
                    alpha = 0.6f;
                    particleSize = 4f;
                    particles = 6;
                    particleRad = 12f;
                    particleLife = 140f;
                }},
                new DrawDefault()
                );
            }
        };

        petroleumConverter = new GenericCrafter("petroleum-converter"){{

            requirements(Category.crafting, with(Items.metaglass, 40, Items.plastanium, 60, WHItems.cobaltNitride, 20));

            size = 2;
            craftTime = 300;
            health = 550;
            hasPower = hasLiquids = true;
            liquidCapacity = 120;
            consumePower(2f);
            consumeLiquid(Liquids.oil, 30 / 60f);
            consumeItems(with(WHItems.combustible, 1));
            outputLiquid = new LiquidStack(WHLiquids.orePromethium, 20 / 60f);
            drawer = new DrawMulti(new DrawRegion(){{
                suffix = "-bottom";
            }}, new DrawLiquidTile(Liquids.oil){{
                alpha = 0.4f;
            }}, new DrawLiquidTile(WHLiquids.orePromethium){{
                alpha = 0.7f;
            }},
            new DrawBubbles(WHLiquids.orePromethium.color.cpy()){{
                sides = 10;
                recurrence = 3f;
                spread = 3;
                radius = 1.1f;
                amount = 10;
            }},
            new DrawDefault(),
            new DrawGlowRegion(){{
                alpha = 0.7f;
                color = Color.valueOf("c4bdf3");
                glowIntensity = 0.3f;
                glowScale = 4f;
            }});
            updateEffect = WHFx.square(WHLiquids.orePromethium.color, 20f, 4, 12, 5);
            researchCostMultiplier = 0.6f;
        }};

        T2ManganeseSteelFurnace = new GenericCrafter("t2-manganese-steel-furnace"){
            {
                Color color = WHPal.MnSteelColor;
                requirements(Category.crafting, with(Items.silicon, 100, WHItems.ceramite, 70, WHItems.manganeseSteel, 70));
                health = 700;
                hasItems = hasPower = true;
                craftTime = 60;
                itemCapacity = 40;
                size = 3;
                consumePower(5);
                consumeItems(with(WHItems.manganese, 6, WHItems.chromium, 3, Items.metaglass, 2));
                consumeLiquid(Liquids.water, 6 / 60f);
                outputItem = new ItemStack(WHItems.manganeseSteel, 4);
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.water, FACTORY_PAD_33){{
                    alpha = 0.75f;
                }},
                new DrawDefault(),
                new DrawCrucibleFlame(),
                new DrawRegion("-mid"),
                new DrawFlame(color){{
                    flameRadius = 6;
                }});
                craftEffect = WHFx.square(MnSteelColor, 35f, 6, 26f, 5f);
                researchCostMultiplier = 0.5f;
            }
        };


        cryofluidMixer = new GenericCrafter("cryofluid-mixer"){
            {
                requirements(Category.crafting, with(Items.tungsten, 80, WHItems.manganeseSteel, 50, WHItems.cobaltNitride, 40));
                health = 600;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 20;
                liquidCapacity = 120;
                size = 3;
                consumePower(4);
                consumeLiquid(Liquids.water, 0.5f);
                consumeItems(with(WHItems.cobalt, 2));
                outputLiquid = new LiquidStack(Liquids.cryofluid, 31 / 60f);
                drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.water),
                new DrawLiquidTile(Liquids.cryofluid){{
                    drawLiquidLight = true;
                }}, new DrawDefault());
                craftEffect = WHFx.square(Liquids.cryofluid.color, 35f, 4, 16f, 5f);
                researchCostMultiplier = 0.45f;
            }
        };

        combustibleCrafter = new GenericCrafter("combustible-crafter"){
            {
                requirements(Category.crafting, with(WHItems.chromium, 80, Items.carbide, 50, WHItems.manganeseSteel, 50));

                size = 3;
                craftTime = 120;
                itemCapacity = 20;
                health = 600;
                hasPower = hasLiquids = hasItems = true;
                consumePower(90 / 60f);
                consumeItems(with(WHItems.chromium, 2, WHItems.oreSand, 3));
                consumeLiquid(Liquids.hydrogen, 45 / 3f / 60f);
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawRegion("-rotator"){{
                    rotateSpeed = 2;
                    rotateDraw = true;
                }}, new DrawDefault());
                outputItem = new ItemStack(WHItems.combustible, 1);

                updateEffect = new ExplosionEffect();
                updateEffectChance = 0.02f;
            }
        };

        entanglementSynthesizer = new GenericCrafter("entanglement-synthesizer"){{

            requirements(Category.crafting, with(Items.plastanium, 80, WHItems.uranium, 80, WHItems.cobaltNitride, 60));
            health = 1000;
            hasItems = hasPower = hasLiquids = true;
            craftTime = 120;
            itemCapacity = 30;
            liquidCapacity = 120;
            squareSprite = false;
            size = 3;
            consumePower(5);
            consumeLiquid(Liquids.ozone, 30f / 2 / 60f);
            consumeItems(with(WHItems.uranium, 4, Items.metaglass, 5, Items.sand, 4));
            outputItem = new ItemStack(WHItems.entanglement, 2);
            drawer = new DrawMulti(new DrawRegion("-bottom"),
            new DrawLiquidTile(Liquids.ozone, FACTORY_PAD_33){{
                drawLiquidLight = true;
                alpha = 0.8f;
            }},
            new DrawParticles(){{
                particleSize = 5;
                particles = 15;
                particleRad = 9f;
                color = WHItems.entanglement.color.cpy().lerp(Pal.accent, 0.15f);
            }},
            new DrawCircles(){{
                color = WHItems.entanglement.color.cpy();
                amount = 3;
                radius = 10;
                strokeMax = 1.2f;
            }},
            new DrawRegion("-mid"),
            new PhaseWeave(){{
                color = WHItems.entanglement.color.cpy();
            }},
            new DrawMultiWeave(){{
                glowColor = new Color(1f, 0.4f, 0.4f, 0.4f);
            }},
            new DrawDefault());
            craftEffect = WHFx.square(WHItems.entanglement.color.cpy(), 35f, 8, 16f, 5f);
            researchCostMultiplier = 0.45f;
        }};

        promethiumRefinery = new GenericCrafter("promethium-refinery"){
            {
                requirements(Category.crafting, with(WHItems.cobalt, 50, WHItems.ceramite, 40, WHItems.armorAlloy, 40));

                health = 1200;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 20;
                liquidCapacity = 120;
                squareSprite = false;
                size = 3;
                consumePower(3);
                consumeItems(with(WHItems.entanglement, 1, WHItems.cobaltNitride, 1));
                consumeLiquid(WHLiquids.orePromethium, 20 / 60f);
                outputLiquid = new LiquidStack(WHLiquids.refinePromethium, 31f / 60f);
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(WHLiquids.orePromethium, FACTORY_PAD_33),
                new DrawLiquidTile(WHLiquids.refinePromethium, FACTORY_PAD_33),
                new DrawRegion("-mid"),
                new DrawRegion("-rotator"){{
                    rotateDraw = true;
                    rotateSpeed = 1;
                }},
                new DrawParticles(){{
                    particleSize = 5;
                    particles = 15;
                    particleRad = 9f;
                    color = WHPal.RefinePromethiumColor.cpy().lerp(Pal.accent, 0.15f);
                }},
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


        heatSiliconSmelter = new HeatCrafter("heat-silicon-smelter"){{
            requirements(Category.crafting, with(WHItems.armorAlloy, 70, WHItems.combustible, 30, WHItems.cobaltNitride, 90, WHItems.ceramite, 50));
            size = 4;
            health = 2000;
            hasPower = hasItems = true;
            itemCapacity = 60;
            craftTime = 60;
            consumePower(1200 / 60f);
            consumeItems(with(WHItems.combustible, 1, Items.graphite, 5, Items.sand, 12));
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


        T2WaterPurifier = new GenericCrafter("t2-water-purifier"){
            {
                requirements(Category.crafting, with(Items.carbide, 80, WHItems.armorAlloy, 50, WHItems.manganeseSteel, 40, Items.graphite, 50));
                health = 1400;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 30;
                liquidCapacity = 600;
                size = 4;
                consumePower(15);
                consumeItems(with(Items.plastanium, 2, Items.graphite, 2));
                consumeLiquid(WHLiquids.swageWater, 250 / 60f);
                outputLiquid = new LiquidStack(Liquids.water, 200 / 60f);
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

        combustibleSeparator = new GenericCrafter("combustible-separator"){
            {
                requirements(Category.crafting, with(WHItems.molybdenum, 80, WHItems.combustible, 30, WHItems.ceramite, 50, WHItems.armorAlloy, 80));
                health = 2000;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 60;
                liquidCapacity = 60;
                size = 4;
                ignoreLiquidFullness = true;
                consumeLiquid(Liquids.slag, 90 / 60f);
                consumeItems(with(WHItems.cobaltNitride, 2, WHItems.chromium, 4));
                outputItem = new ItemStack(WHItems.combustible, 4);
                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.slag){{
                    alpha = 0.2f;
                }},
                new DrawCircles(){{
                    color = Pal.slagOrange.cpy().lerp(Pal.accent, 0.5f).lerp(Pal.coalBlack, 0.1f);
                    amount = 3;
                    strokeMax = 1.5f;
                }},
                new DrawParticles(){{
                    particleSize = 6;
                    particles = 15;
                    particleRad = 9f;
                    color = Pal.slagOrange.cpy().lerp(Pal.accent, 0.5f).lerp(Pal.coalBlack, 0.1f).a(0.7f);
                }},
                new DrawDefault()
                );

                craftEffect = WHFx.square(WHItems.combustible.color, 20, 3, 20, 3f);
            }
        };

        crystalEngraver = new GenericCrafter("crystal-engraver"){
            {
                requirements(Category.crafting, with(WHItems.cobaltNitride, 50, WHItems.molybdenum, 70, WHItems.ceramite, 70));

                health = 900;
                hasItems = hasPower = true;
                craftTime = 90;
                itemCapacity = 12;
                liquidCapacity = 120;
                size = 3;
                consumePower(5);
                consumeItems(with(WHItems.molybdenum, 3, Items.metaglass, 3, WHItems.cobaltNitride, 1));
                outputItem = new ItemStack(WHItems.resonantCrystal, 3);
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new EngraverDraw(){{
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

        pressureReactionChamber = new GenericCrafter("pressure-reaction-chamber"){
            {
                requirements(Category.crafting, with(WHItems.cobaltNitride, 50, WHItems.armorAlloy, 90, WHItems.entanglement, 30));

                health = 900;
                hasItems = hasPower = true;
                craftTime = 120;
                itemCapacity = 12;
                liquidCapacity = 120;
                size = 3;
                consumePower(5);
                consumeLiquids(LiquidStack.with(Liquids.nitrogen, 10 / 60f));
                consumeItems(with(WHItems.combustible, 2, WHItems.resonantCrystal, 2));
                outputItem = new ItemStack(WHItems.culverCrystal, 1);
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawParticles(){{
                    particleSize = 6;
                    particles = 15;
                    particleRad = 9f;
                    color = WHItems.culverCrystal.color.cpy().a(0.6f);
                }},
                new DrawArcs(){{
                    flameRad = 2f;
                    circleSpace = 3f;
                    circleStroke = 2f;
                    arcRad = 8;
                    arcPoints = 5;
                    arcs = 10;
                    flameColor = midColor = WHItems.culverCrystal.color.cpy().a(0.6f);
                }},
                new DrawArcSmelt(){{
                    drawCenter = false;
                    flameColor = midColor = WHItems.culverCrystal.color.cpy().a(0.6f);
                }},
                new DrawLiquidTile(Liquids.nitrogen, 48 / 4f),
                new DrawDefault());
                craftEffect = new MultiEffect(WHFx.square(WHItems.culverCrystal.color, 35f, 4, 16f, 3f),
                WHFx.diffuse(3, WHItems.culverCrystal.color, 60));
                researchCostMultiplier = 0.45f;
            }
        };

        moSurgeSmelter = new HeatProducer("mo-surge-smelter"){
            {
                requirements(Category.crafting, with(Items.carbide, 60, WHItems.entanglement, 40, WHItems.armorAlloy, 50, WHItems.manganeseSteel, 70));
                health = 900;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 20;
                liquidCapacity = 80f;
                size = 3;
                consumePower(3);
                consumeItems(with(WHItems.molybdenum, 2, WHItems.armorAlloy, 2));
                consumeLiquid(Liquids.slag, 20 / 60f);
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

        largeArmorSmelter = new GenericCrafter("large-armor-smelter"){{
            requirements(Category.crafting, with(WHItems.armorAlloy, 120, WHItems.resonantCrystal, 50, WHItems.molybdenum, 180, WHItems.ceramite, 60));

            size = 4;
            craftTime = 120f;
            health = 1200;
            hasItems = hasPower = hasLiquids = true;
            liquidCapacity = 50;
            itemCapacity = 75;
            consumePower(8f);
            consumeItems(with(WHItems.chromium, 10, Items.carbide, 4, WHItems.manganeseSteel, 6));
            consumeLiquid(WHLiquids.refinePromethium, 10 / 60f);
            outputItem = new ItemStack(WHItems.armorAlloy, 6);
            drawer = new DrawMulti(new DrawDefault(),
            new DrawFlame(Color.valueOf("FFDEB5FF")){{
                flameRadius = flameRadiusIn = flameRadiusMag = 0;
            }}
           /* new DrawGlowRegion(){{
                color = Color.valueOf("FFDEB5FF");
                layer = Layer.effect;
                glowIntensity = 0.7f;
            }}*/);
            updateEffect = WHFx.hexagonSpread(Items.surgeAlloy.color, 10f, 20f);
            craftEffect = WHFx.hexagonSmoke(Items.surgeAlloy.color, 30f, 1.2f, 10, 20f);
            researchCostMultiplier = 0.6f;
        }};

        sealedPromethiumMill = new GenericCrafter("sealed-promethium-mill"){{

            requirements(Category.crafting, with(Items.carbide, 90, WHItems.ceramite, 100, WHItems.entanglement, 40, WHItems.armorAlloy, 30));

            health = 2000;
            hasItems = hasPower = hasLiquids = true;
            craftTime = 120;
            itemCapacity = 15;
            liquidCapacity = 120;
            size = 4;
            consumePower(15);
            consumeLiquid(WHLiquids.refinePromethium, 10f / 60f);
            consumeItems(with(WHItems.entanglement, 1, WHItems.ceramite, 2, WHItems.combustible, 1));
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
                arcPoints = 6;
                arcRange = size * 1.8f;
                arcLife = 60f;
            }},
            new DrawDefault(),
            new DrawArcSmelt(){{
                drawCenter = false;
                midColor = flameColor = Pal.sapBullet;
                particleRad = 40f;
                particleLen = 7f;
                particleLife = 60f;
            }}
           /* new DrawGlowRegion(Layer.blockAdditive + 0.1f){{
                color = Pal.sapBullet;
                glowIntensity = 1f;
                glowScale = 6f;
            }}*/
            );
            craftEffect = new MultiEffect(new WrapEffect(WHFx.circleOut(40, 40f, 5), Pal.sapBullet),
            new WrapEffect(WHFx.circleOut(30, 40f, 5), Pal.sapBullet).startDelay(40));

            destroyBullet = WHBullets.sealedPromethiumMillBreak;
            researchCostMultiplier = 0.6f;
        }};

        laserEngraver = new HeatCrafter("laser-engraver"){
            {
                requirements(Category.crafting, with(WHItems.entanglement, 50, WHItems.resonantCrystal, 70, WHItems.molybdenumAlloy, 50));

                health = 2500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 20;
                liquidCapacity = 120;
                size = 4;
                consumePower(5);
                consumeItems(with(WHItems.resonantCrystal, 3, WHItems.molybdenumAlloy, 2));
                consumeLiquid(WHLiquids.refinePromethium, 10 / 60f);
                outputItem = new ItemStack(WHItems.protocolChip, 3);
                /*heatOutput = 4;*/
                heatRequirement = 12;
                maxEfficiency = 2;
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
                }}, new DrawDefault(),
                new DrawHeatInput()
                /* new DrawHeatOutput()*/);

                craftEffect = new MultiEffect(WHFx.square(Liquids.slag.color, 35f, 8, 32, 4f),
                WHFx.diffuse(size, Liquids.slag.color, 60f));
                researchCostMultiplier = 0.45f;
            }
        };

        ceramiteRefinery = new HeatProducer("ceramite-refinery"){
            {
                Color color = WHPal.RefineCeramiteColor;
                requirements(Category.crafting, with(WHItems.molybdenumAlloy, 80, WHItems.entanglement, 50, WHItems.ceramite, 50));
                health = 1500;
                hasItems = hasPower = hasLiquids = true;
                craftTime = 120;
                itemCapacity = 30;
                size = 3;

                heatOutput = 3f;
                //wasteHeatOutput = 9f;
                consumePower(6);
                consumeItems(with(WHItems.molybdenumAlloy, 2, WHItems.ceramite, 3));
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

        slagfurnace = new Separator("slag-furnace"){{

            requirements(Category.crafting, with(WHItems.manganeseSteel, 400, WHItems.resonantCrystal, 100, WHItems.protocolChip, 100, WHItems.refineCeramite, 100));
            size = 3;
            health = 850;
            hasPower = hasLiquids = hasItems = true;
            craftTime = 90;
            liquidCapacity = 300;
            itemCapacity = 30;
            consumePower(15f);
            consumeLiquid(Liquids.slag, 120 / 60f);
            results = with(
            WHItems.ceramite, 2,
            WHItems.refineCeramite, 1,
            WHItems.armorAlloy, 2,
            WHItems.cobaltNitride, 1,
            WHItems.manganeseSteel, 4
            );
            drawer = new DrawMulti(new DrawRegion("-bottom"),
            new DrawLiquidTile(Liquids.slag),
            new DrawDefault(),
            new DrawFlame(Color.valueOf("FF8C7AFF")));
            ambientSound = loopPulse;
            ambientSoundVolume = 0.3f;
            researchCostMultiplier = 0.6f;

        }};

        ADMill = new HeatProducer("admantium-mill"){
            {
                requirements(Category.crafting, with(WHItems.molybdenumAlloy, 50, WHItems.protocolChip, 50, WHItems.refineCeramite, 30));

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


       /* scrapCrusher = new MultiCrafter("scrap-crusher"){{

            requirements(Category.crafting, with(Items.silicon, 50, WHItems.manganese, 50, WHItems.manganeseSteel, 30, Items.graphite, 80));

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
                WHFx.arcSmelt(WHItems.manganese.color, 13f, 6, 60f));
            }}
            , new CraftPlan(){{
                craftTime = 20f;
                consumeItem(WHItems.manganese, 1);
                outputItems = with(Items.scrap, 2);
                consumePower(100 / 60f);
                craftEffect = new MultiEffect(WHFx.square(WHItems.manganese.color, 35f, 4, 16f, 5f),
                WHFx.arcSmelt(WHItems.manganese.color, 13f, 6, 60f));
            }}
            , new CraftPlan(){{
                craftTime = 30f;
                consumeItem(WHItems.manganese, 1);
                outputItems = with(Items.sand, 2);
                consumePower(100 / 60f);
                craftEffect = new MultiEffect(WHFx.square(WHItems.manganese.color, 35f, 4, 16f, 5f),
                WHFx.arcSmelt(WHItems.manganese.color, 13f, 6, 60f));
            }}, new CraftPlan(){{
                craftTime = 30f;
                consumeItem(Items.titanium, 1);
                outputItems = with(Items.sand, 3);
                consumePower(100 / 60f);
                craftEffect = new MultiEffect(WHFx.square(Items.titanium.color, 35f, 4, 16f, 5f),
                WHFx.arcSmelt(Items.titanium.color, 13f, 6, 60f));
            }});
        }};*/

        combustionHeater = new FlammabilityHeatProducer("combustion-heater"){
            {
                requirements(Category.crafting, with(WHItems.manganese, 60, Items.silicon, 40));
                size = 2;
                craftTime = 60;
                health = 200;
                hasItems = true;
                itemCapacity = 20;
                consume(new ConsumeItemFlammable(0.9f));
                heatOutput = 1.5f;
                drawer = new DrawMulti(new DrawDefault(),
                new DrawHeatOutput(),
                new DrawHeatInput(){{
                    suffix = "-heat";
                }});
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

        slagHeatMaker = new HeatProducer("slag-heat-maker"){{
            requirements(Category.crafting, with(WHItems.chromium, 30, Items.graphite, 50, WHItems.manganeseSteel, 30, WHItems.cobaltNitride, 30));

            size = 3;
            itemCapacity = 0;
            liquidCapacity = 300;
            rotateDraw = false;
            regionRotated1 = 1;
            ambientSound = loopHum;
            drawer = new DrawMulti(new DrawRegion("-bottom"), new DrawLiquidTile(Liquids.slag), new DrawDefault(), new DrawHeatOutput());
            consumeLiquid(Liquids.slag, 1);
            heatOutput = 6;
        }};

        decayHeater = new HeatProducerReactor("decay-heater"){
            {
                requirements(Category.crafting, with(WHItems.armorAlloy, 50, WHItems.ceramite, 50, WHItems.uranium, 50, WHItems.cobaltNitride, 50));
                size = 3;
                itemDuration = 240f;
                health = 1800;
                hasItems = true;
                liquidCapacity = 120;
                itemCapacity = 10;
                workHeatOutput = 10;
                consumeItem(WHItems.uranium, 2);
                fuelItem = WHItems.uranium;
                consumeLiquid(Liquids.water, 20 / 60f).update(false);
                drawer = new DrawMulti(
                new DrawCrucibleFlame(),
                new DrawDefault(),
                new DrawHeatOutput());
                ambientSound = loopSmelter;
                ambientSoundVolume = 0.002f;
                researchCostMultiplier = 0.7f;
            }
        };


        promethiumHeater = new HeatProducer("promethium-heater"){{
            requirements(Category.crafting, with(WHItems.molybdenumAlloy, 100, WHItems.culverCrystal, 50, WHItems.cobaltNitride, 50));

            size = 3;
            craftTime = 4f;
            health = 1000;
            hasItems = false;
            hasLiquids = hasPower = true;
            liquidCapacity = 80;
            consumePower(2);
            consumeLiquid(WHLiquids.refinePromethium, 20 / 60f);
            heatOutput = 40f;
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

        heatBelt = new HeatBelt("Heat-Belt"){
            {
                requirements(Category.distribution, with(WHItems.cobalt, 2, Items.graphite, 2, WHItems.manganeseSteel, 2));
                researchCostMultiplier = 10f;
                rotate = true;
                hasPower = false;
                group = BlockGroup.heat;
                size = 1;
                regionRotated1 = 1;
            }
        };

        smallHeatRouter = new ConfigurableHeatRouter("small-heat-router"){
            {

                requirements(Category.distribution, with(WHItems.manganese, 15, WHItems.cobalt, 10, Items.graphite, 10));

                researchCostMultiplier = 2f;

                group = BlockGroup.heat;
                size = 1;
                drawer = new DrawMulti(new DrawDefault(), new DrawHeatOutput(-1, false),
                new DrawHeatOutput(), new DrawHeatOutput(1, false), new DrawHeatInput("-heat"));
                regionRotated1 = 1;
                splitHeat = true;
            }

        };

        heatBridge = new HeatDirectionBridge("heat-bridge"){

            {
                requirements(Category.distribution, with(WHItems.cobaltNitride, 3, WHItems.manganeseSteel, 5, Items.graphite, 10));
                size = 1;
                range = 4;
                health = 100;
                lost = 0.02f;
                hasPower = false;
                researchCostMultiplier = 2f;
                regionRotated1 = 1;
                pulse = true;
            }
        };

        T2heatBridge = new HeatDirectionBridge("t2-heat-bridge"){
            {
                requirements(Category.distribution, with(WHItems.resonantCrystal, 10, WHItems.armorAlloy, 5, WHItems.manganeseSteel, 10));
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
            requirements(Category.crafting, with(WHItems.manganeseSteel, 50, Items.plastanium, 60, Items.phaseFabric, 20));

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


        T2ceramiteSteelFoundry = new HeatProducer("t2-ceramite-steel-foundry"){
            {
                requirements(Category.crafting, with(WHItems.molybdenumAlloy, 50, WHItems.cobaltNitride, 50, WHItems.resonantCrystal, 50));
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
                updateEffect = new MultiEffect(WHFx.square(CeramiteColor, 35f, 4, 16f, 4f));
                craftEffect = new MultiEffect(
                new RadialEffect(Fx.surgeCruciSmoke, 4, 90, 7),
                new RadialEffect(Fx.surgeCruciSmoke, 4, 90, 13f){{
                    rotationOffset = 45f;
                }});
            }
        };


        //Drill
        electronicPneumaticDrill = new Drill("electric-drill"){{
            requirements(Category.production, with(WHItems.manganese, 20));
            tier = 3;
            drillTime = 180;
            size = 2;
            liquidCapacity = 30f;
            consumePower(0.5f);

            consumeLiquid(WHLiquids.swageWater, 5 / 60f).boost();
        }};
        MechanicalQuarry = new Quarry("mechanical-quarry"){{

            requirements(Category.production, with(Items.graphite, 30, WHItems.manganeseSteel, 30, Items.silicon, 60));

            health = 300;
            size = 3;
            regionRotated1 = 1;
            itemCapacity = 100;
            acceptsItems = true;

            areaSize = 11;
            liquidBoostIntensity = 1.5f;
            mineTime = 450;

            tier = 4;

            drawDrill = true;
            deploySpeed = 0.015f;
            deployInterp = new Interp.PowOut(4);
            deployInterpInverse = new Interp.PowIn(4);
            drillMoveSpeed = 0.07f;
            consumePower(1);
            consumeLiquid(WHLiquids.swageWater, 10 / 60f).boost();

        }};

        lavaDrill = new Drill("lava-drill"){{
            requirements(Category.production, with(WHItems.manganese, 70, WHItems.uranium, 70, WHItems.armorAlloy, 25, WHItems.cobaltNitride, 25));
            drillTime = 320;
            size = 4;
            drawRim = true;
            heatColor = Color.valueOf("ff5512").a(0.5f);
            hasPower = true;
            squareSprite = false;
            tier = 5;
            updateEffect = Fx.pulverizeRed;
            updateEffectChance = 0.03f;
            drillEffect = Fx.mineHuge;
            rotateSpeed = 6f;
            warmupSpeed = 0.01f;
            liquidCapacity = 60;
            itemCapacity = 50;

            liquidBoostIntensity = 2;

            consumePower(3f);
            consumeLiquid(Liquids.slag, 10 / 60f).boost();
        }};

        heavyCuttingDrill = new BurstDrill("heavy-cutting-drill"){
            {
                requirements(Category.production, with(Items.plastanium, 50, Items.silicon, 100, WHItems.manganeseSteel, 100, WHItems.cobaltNitride, 30));

                health = 2900;
                size = 4;
                tier = 6;
                arrowOffset = 0;
                arrowSpacing = 13 / 4f;
                arrows = 2;
                itemCapacity = 100;
                liquidCapacity = 50;
                glowColor = Color.valueOf("FEE984FF");
                fogRadius = 1;
                squareSprite = false;
                drawRim = true;
                hasItems = hasPower = hasLiquids = true;
                consumePower(5);
                consumeLiquid(Liquids.water, 20 / 60f);
                drillTime = 100;
                drillEffect = new MultiEffect(Fx.mineImpact, Fx.drillSteam, Fx.mineImpactWave.wrap(Pal.redLight, 40f));
                researchCostMultiplier = 0.6f;
            }
        };


        SpecialCuttingDrill = new SpecialDrill("heavy-steel-laser-drill"){
            {
                requirements(Category.production, with(WHItems.molybdenumAlloy, 50, WHItems.protocolChip, 50, WHItems.entanglement, 40));

                health = 3200;
                drillTime = 550;
                size = 4;
                tier = 7;
                itemCapacity = 120;
                liquidCapacity = 80;
                mineOffset = -2;
                mineSize = 6;
                fogRadius = 2;
                drawRim = true;
                hasItems = hasPower = hasLiquids = true;
                liquidBoostIntensity = 2f;
                consumePower(7);
                consumeLiquid(Liquids.cryofluid, 15 / 60f);
                allowedItems = Seq.with(
                WHItems.uranium, Items.tungsten, WHItems.molybdenum);

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

        highEnergyDrill = new BurstDrill("high-energy-drill"){
            {
                requirements(Category.production, with(WHItems.armorAlloy, 100, WHItems.culverCrystal, 50, WHItems.protocolChip, 90, WHItems.resonantCrystal, 100, WHItems.refineCeramite, 90));

                health = 4000;
                size = 5;
                tier = 20;
                arrowOffset = 2;
                arrowSpacing = 13 / 4f;
                arrows = 2;
                itemCapacity = 120;
                liquidCapacity = 80;
                glowColor = Items.surgeAlloy.color;
                fogRadius = 2;
                drawRim = true;
                squareSprite = false;
                hasItems = hasPower = hasLiquids = true;
                consumePower(15);
                consumeLiquid(Liquids.cryofluid, 0.3f);

                drillTime = 60;
                drillEffect = new MultiEffect(Fx.mineImpact, Fx.drillSteam,
                new WrapEffect(Fx.dynamicSpikes, Items.surgeAlloy.color, 30f),
                new WrapEffect(Fx.mineImpactWave, Items.surgeAlloy.color, 45f));
                researchCostMultiplier = 0.6f;

            }
        };

        heavyExtractor = new EnhancedWaterExtractor("heavy-extractor"){
            {
                requirements(Category.production, with(WHItems.chromium, 50, Items.silicon, 80, WHItems.cobaltNitride, 20));
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

                extractorItem = Items.plastanium;
                itemUseTime = 240;
                consumeItems(with(Items.graphite, 1));
                consumeItems(with(extractorItem, 1)).boost();
                consumePower(7f);
                researchCostMultiplier = 0.5f;
            }
        };

        strengthenOilExtractor = new Fracker("strengthen-oil-extractor"){
            {
                requirements(Category.production, with(WHItems.manganese, 60, Items.silicon, 120, Items.plastanium, 50, WHItems.manganeseSteel, 50));
                health = 900;
                size = 3;
                hasItems = floating = hasPower = hasLiquids = true;
                itemCapacity = 15;
                liquidCapacity = 60;
                pumpAmount = 18 / 60f;
                rotateSpeed = 1.3f;
                result = Liquids.oil;
                baseEfficiency = 0.5f;
                attribute = Attribute.oil;
                itemUseTime = 120;
                consumeItems(with(Items.graphite, 2));
                consumeLiquid(WHLiquids.swageWater, 30 / 60f);
                consumePower(3);
                researchCostMultiplier = 0.6f;
            }
        };

        promethiumExtractor = new Fracker("promethium-extractor"){
            {
                requirements(Category.production, with(WHItems.armorAlloy, 30, Items.graphite, 120, WHItems.cobaltNitride, 30, WHItems.manganeseSteel, 40));

                health = 550;
                size = 3;
                hasPower = true;
                liquidCapacity = 60;
                pumpAmount = 20 / 60f;
                rotateSpeed = 1.3f;
                result = WHLiquids.orePromethium;
                /*  attribute = Attribute.oil;*/
                baseEfficiency = 0.3f;
                attribute = WHBlocksEnvironment.hasPromethium;
                consumePower(4);
                itemUseTime = 300;
                consumeItems(new ItemStack(WHItems.combustible, 1));
                consumeLiquid(WHLiquids.swageWater, 30 / 60f);
                updateEffect = Fx.pulverize;
                updateEffectChance = 0.05f;
                researchCostMultiplier = 0.36f;
            }
        };

        integratedCompressor = new AttributeCrafter("integrated-compressor"){
            {
                requirements(Category.production, with(WHItems.cobaltNitride, 50, WHItems.ceramite, 50, WHItems.resonantCrystal, 30));
                hasItems = hasPower = hasLiquids = true;
                craftTime = 60;
                itemCapacity = 64;
                liquidCapacity = 100;
                size = 3;
                consumePower(7);
                envRequired |= Env.groundOil;
                attribute = Attribute.oil;
                baseEfficiency = 0f;
                consumeLiquid(Liquids.water, 20 / 60f);
                maxBoost = 2;
                boostScale = 1 / 9f;
                outputItem = new ItemStack(Items.graphite, 3);

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
                updateEffect = Fx.pulverizeMedium;
                craftEffect = WHFx.square(Pal.slagOrange, 60, 4, 20, 4);
            }
        };

        slagExtractor = new AttributeCrafter("slag-extractor"){
            {
                requirements(Category.production, with(WHItems.chromium, 70, WHItems.armorAlloy, 60, Items.silicon, 100, WHItems.manganeseSteel, 50));
                size = 3;
                hasItems = hasPower = hasLiquids = true;
                liquidCapacity = 180;
                updateEffect = Fx.redgeneratespark;
                drawer =
                new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.slag),
                new DrawDefault(),
                new DrawRegion("-rotator", 8.6f, true),
                new DrawRegion("-light"),
                new DrawRegion("-top"));

                craftTime = 60f;
                outputLiquid = new LiquidStack(Liquids.slag, 1f);

                consumePower(10);
                consumeItems(with(Items.tungsten, 2));

                baseEfficiency = 0.1f;
                maxBoost = 6;
                boostScale = 0.25f;
                attribute = Attribute.heat;
                researchCostMultiplier = 0.45f;
            }
        };

        //liquid
        lightConduit = new TubeConduit("light-conduit"){
            {
                requirements(Category.liquid, with(Items.metaglass, 2, WHItems.manganese, 3));
                liquidCapacity = 40f;
                liquidPressure = 2.5f;
                drawArrow = true;
            }
        };

        steelConduit = new TubeConduit("steel-conduit"){
            {
                requirements(Category.liquid, with(Items.metaglass, 3, WHItems.manganeseSteel, 1, WHItems.ceramite, 1));
                health = 600;
                armor = 5;
                liquidCapacity = 80f;
                liquidPressure = 3f;
                drawCover = true;
            }
        };

        armorFluidRouter = new LiquidRouter("armor-fluid-router"){{
            requirements(Category.liquid, with(WHItems.chromium, 4, Items.metaglass, 3, WHItems.manganeseSteel, 2));
            liquidCapacity = 150f;
            liquidPadding = 3f / 4f;
            researchCostMultiplier = 3;
            underBullets = true;
            solid = false;
            health = 300;
            armor = 5;

            explosivenessScale = flammabilityScale = 40f / liquidCapacity;
        }};

        armorFluidJunction = new LiquidJunction("armor-fluid-junction"){{
            requirements(Category.liquid, with(WHItems.chromium, 4, Items.metaglass, 4, WHItems.manganeseSteel, 4));
            buildCostMultiplier = 3f;
            health = 450;
            armor = 5;
            ((TubeConduit)lightConduit).junctionReplacement = this;
            researchCostMultiplier = 1;
            solid = false;
            underBullets = true;
        }};

        mixedFluidJunction = new MixedFluidJunction("mixed-fluid-junction"){{
            requirements(Category.liquid, with(WHItems.molybdenum, 10, Items.plastanium, 4, WHItems.armorAlloy, 4));
            buildCostMultiplier = 2f;
            health = 500;
            armor = 5;
            displayedSpeed = 30f;
            ((TubeConduit)steelConduit).junctionReplacement = this;
            researchCostMultiplier = 1;
            solid = false;
            underBullets = true;
        }};

        steelBridgeConduit = new LiquidBridge("steel-bridge-conduit"){
            {
                requirements(Category.liquid, with(WHItems.manganese, 10, WHItems.chromium, 5, Items.metaglass, 5));
                armor = 2;
                rotate = false;
                range = 7;
                liquidCapacity = 50;
                transportTime = 1.5f;
                arrowSpacing = 7;
                arrowOffset = 3.5f;
                arrowTimeScl = 12;
                bridgeWidth = 8;
                hasPower = false;
                ((TubeConduit)lightConduit).bridgeReplacement = this;
            }
        };

        lowResistanceConduit = new LiquidBridge("low-resistance-conduit"){
            {
                requirements(Category.liquid, with(Items.metaglass, 15, WHItems.manganeseSteel, 5, WHItems.resonantCrystal, 5, WHItems.cobaltNitride, 5));
                rotate = false;
                range = 18;
                pulse = true;
                liquidCapacity = 100;
                transportTime = 1.5f;
                arrowSpacing = 7;
                arrowOffset = 3.5f;
                arrowTimeScl = 12;
                bridgeWidth = 8;
                hasPower = true;
                consumePower(0.5f);
                ((TubeConduit)steelConduit).bridgeReplacement = this;
            }
        };

        basicPump = new Pump("basic-pump"){
            {
                requirements(Category.liquid, with(WHItems.manganese, 10, WHItems.chromium, 10, Items.metaglass, 20));
                size = 1;
                squareSprite = false;
                drawer = new DrawMulti(new DrawRegion("-liquid"), new DrawPumpLiquidTile(4 / 8f), new DrawDefault());
                liquidCapacity = 40;
                hasLiquids = hasPower = true;
                pumpAmount = 20 / 60f;
                researchCostMultiplier = 0.45f;
            }
        };

        steelPump = new Pump("steel-pump"){
            {
                requirements(Category.liquid, with(Items.metaglass, 50, WHItems.armorAlloy, 30, WHItems.manganeseSteel, 30, Items.silicon, 50));
                health = 320;
                size = 2;

                squareSprite = false;
                liquidCapacity = 200;
                hasLiquids = hasPower = true;
                pumpAmount = 100f / 4f / 60f;
                consumePower(1);
                researchCostMultiplier = 0.45f;
            }
        };

        gravityPump = new Pump("gravity-pump"){
            {
                requirements(Category.liquid, with(Items.metaglass, 300, WHItems.manganeseSteel, 150,
                WHItems.cobaltNitride, 80, WHItems.ceramite, 80, WHItems.entanglement, 50));

                health = 1400;
                size = 4;
                drawer = new DrawMulti(new DrawRegion("-liquid"), new DrawPumpLiquidTile(16 / 8f), new DrawDefault());
                squareSprite = false;
                liquidCapacity = 800;
                hasLiquids = hasPower = true;
                pumpAmount = 600 / 60f / 16f;
                consumePower(1000.001f / 60f);
                researchCostMultiplier = 0.36f;
            }
        };


        T2LiquidTank = new LiquidRouter("steel-liquid-tank"){
            {
                requirements(Category.liquid, with(WHItems.manganese, 150, WHItems.manganeseSteel, 100, Items.plastanium, 80));
                size = 3;
                liquidCapacity = 6000;
                absorbLasers = true;
                researchCostMultiplier = 0.36f;
            }
        };

        armorLiquidTank = new LiquidRouter("armor-liquid-tank"){
            {
                requirements(Category.liquid, with(WHItems.manganese, 150, WHItems.manganeseSteel, 100, Items.plastanium, 80, WHItems.ceramite, 50));
                armor = 10;
                size = 3;
                liquidCapacity = 8000;
                absorbLasers = true;
                squareSprite = false;
                researchCostMultiplier = 0.36f;
            }
        };


        //distribution
        basicDust = new CoverdConveyor("basic-dust"){{

            requirements(Category.distribution, with(WHItems.manganese, 1));

            underBullets = true;
            hasShadow = true;
            size = 1;
            speed = 15f / 138f;//why?
            displayedSpeed = 15;
            hasItems = true;
            itemCapacity = 2;
            researchCostMultiplier = 1;
        }};

        steelDust = new CoverdConveyor("steel-dust"){{

            requirements(Category.distribution, with(WHItems.cobalt, 2, WHItems.manganeseSteel, 2, Items.plastanium, 1));

            health = 200;
            underBullets = true;
            hasShadow = true;
            placeableLiquid = true;
            size = 1;

            speed = 30f / 138f;//why?
            displayedSpeed = 30;
            hasItems = true;
            itemCapacity = 2;
            researchCostMultiplier = 1;
            //钢质导轨
        }};

        armorJunction = new Junction("armor-junction"){{
            requirements(Category.distribution, with(Items.graphite, 5, WHItems.manganese, 4));
            speed = 45;
            capacity = 10;
            displayedSpeed = 18.5f;
            health = 200;
            armor = 2;
            buildCostMultiplier = 6f;
        }};

        armorInvertedSorter = new Sorter("armor-inverted-sorter"){{
            requirements(Category.distribution, with(WHItems.manganeseSteel, 2, Items.silicon, 5));
            buildCostMultiplier = 3f;
            armor = 5;
            invert = true;
        }};

        armorSorter = new Sorter("armor-sorter"){{
            requirements(Category.distribution, with(WHItems.manganeseSteel, 2, Items.silicon, 5));
            buildCostMultiplier = 3f;
            armor = 5;
        }};

        armorRouter = new Router("armor-router"){{
            requirements(Category.distribution, with(WHItems.manganese, 4, Items.graphite, 4));
            buildCostMultiplier = 2f;
            armor = 2;
            speed = 12;
        }};

        armorOverflowGate = new OverflowDuct("armor-overflow-gate"){{
            requirements(Category.distribution, with(WHItems.manganese, 4, WHItems.chromium, 2, Items.graphite, 4));
            armor = 2;
            speed = 2f;
            solid = false;
            researchCostMultiplier = 2f;
        }};

        armorUnderflowGate = new OverflowDuct("armor-underflow-gate"){{
            requirements(Category.distribution, with(WHItems.manganese, 4, WHItems.chromium, 2, Items.graphite, 4));
            armor = 2;
            speed = 2f;
            solid = false;
            invert = true;
            researchCostMultiplier = 2f;
        }};


        basicBridge = new ItemBridge("basic-bridge"){{
            {
                requirements(Category.distribution, with(WHItems.chromium, 10, Items.silicon, 15));

                range = 7;
                transportTime = 60 / 15f;
                arrowSpacing = 5;
                arrowOffset = 2.5f;
                arrowTimeScl = 12;
                bridgeWidth = 8;
                hasPower = false;
                ((CoverdConveyor)basicDust).bridgeReplacement = this;
                ((CoverdConveyor)basicDust).junctionReplacement = armorJunction;
                researchCostMultiplier = 1;
            }
        }};

        lowResistanceBridge = new ItemBridge("low-resistance-bridge"){{
            {
                requirements(Category.distribution, with(WHItems.manganeseSteel, 10, WHItems.resonantCrystal, 5, WHItems.cobaltNitride, 5));

                health = 400;
                range = 25;
                transportTime = 1.5f;
                arrowSpacing = 8;
                arrowOffset = 4f;
                arrowTimeScl = 12;
                bridgeWidth = 8;
                consumePower(0.5f);
                ((CoverdConveyor)steelDust).bridgeReplacement = this;
                ((CoverdConveyor)steelDust).junctionReplacement = armorJunction;
                researchCostMultiplier = 1;
            }
        }};


        ceramiteConveyor = new BetterStackConvyor("ceramite-conveyor"){{

            requirements(Category.distribution, with(WHItems.manganeseSteel, 2, WHItems.ceramite, 1));

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
                requirements(Category.distribution, with(WHItems.molybdenumAlloy, 1, WHItems.manganeseSteel, 4, WHItems.ceramite, 1));
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
                requirements(Category.distribution, with(WHItems.ceramite, 5, WHItems.resonantCrystal, 10, WHItems.entanglement, 10));

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

        steelUnloader = new UnloaderF("steel-unloader"){{

            requirements(Category.distribution, with(Items.carbide, 15, WHItems.manganeseSteel, 20));
            size = 1;
            update = true;
            hasItems = true;
            health = 300;
            speed = 1.5f;
            researchCostMultiplier = 1;
        }};

        trackDriver = new MassDriver("track-driver"){
            {
                requirements(Category.distribution, with(WHItems.manganeseSteel, 120, WHItems.ceramite, 50, WHItems.entanglement, 30));

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
            }
        };

        //logic
        holographyMessage = new MessageBlock("holography-message"){{
            requirements(Category.logic, with(WHItems.cobalt, 5, Items.silicon, 5));
        }};

        switchBlock = new SwitchBlock("switch"){{
            requirements(Category.logic, with(WHItems.cobalt, 5, Items.graphite, 5, Items.silicon, 5));
        }};

        juniorProcessor = new LogicBlock("junior-processor"){{
            requirements(Category.logic, with(WHItems.cobalt, 10, WHItems.manganeseSteel, 50, Items.silicon, 50));

            instructionsPerTick = 4;
            range = 8 * 15;
            size = 1;
        }};

        instructionProcessor = new LogicBlock("instruction-processor"){{
            requirements(Category.logic, with(WHItems.manganeseSteel, 50, WHItems.molybdenumAlloy, 50, WHItems.resonantCrystal, 50));

            instructionsPerTick = 20;
            range = 8 * 30;
            size = 2;
        }};

        memoryCell = new MemoryBlock("memory-cell"){{
            requirements(Category.logic, with(WHItems.cobalt, 30, Items.graphite, 30, Items.silicon, 30));

            memoryCapacity = 64;
        }};

        memoryBank = new MemoryBlock("memory-bank"){{
            requirements(Category.logic, with(WHItems.cobalt, 60, Items.silicon, 80, WHItems.entanglement, 30));

            memoryCapacity = 512;
            size = 2;
        }};

        logicDisplay = new LogicDisplay("logic-display"){{
            requirements(Category.logic, with(WHItems.manganeseSteel, 20, Items.silicon, 50, Items.metaglass, 50, WHItems.cobaltNitride, 20));

            displaySize = 80;

            size = 3;
        }};

        canvas = new CanvasBlock("canvas"){{
            requirements(Category.logic, BuildVisibility.shown, with(WHItems.cobalt, 30, Items.silicon, 30, WHItems.manganeseSteel, 30));

            canvasSize = 12;
            padding = 7f / 4f * 2f;

            size = 2;
        }};


        //power

        powerNode = new PowerNode("power-node"){{
            requirements(Category.power, with(WHItems.manganese, 2, WHItems.chromium, 3));

            health = 150;
            size = 1;
            maxNodes = 10;
            laserRange = 9;
            laserScale = 0.4f;
            researchCostMultiplier = 0.8f;
        }};

        t2PowerNode = new PowerNode("t2-power-node"){
            {
                requirements(Category.power, with(WHItems.cobalt, 10, WHItems.manganeseSteel, 5, Items.silicon, 15));

                health = 400;
                size = 2;
                maxNodes = 15;
                laserRange = 18;
                laserScale = 0.4f;
                researchCostMultiplier = 0.8f;
            }
        };

        compositeNode = new PowerNode("composite-node"){
            {
                requirements(Category.power, with(WHItems.ceramite, 10, WHItems.cobaltNitride, 5, Items.silicon, 15));

                health = 800;
                armor = 5;
                size = 2;
                maxNodes = 20;
                laserRange = 24;
                consumePowerBuffered(15 * 1000f);
                laserScale = 0.4f;
                researchCostMultiplier = 0.8f;
            }
        };

        armorPowerTower = new PowerNode("armor-power-tower"){
            {
                requirements(Category.power, with(WHItems.molybdenumAlloy, 10, WHItems.resonantCrystal, 15, WHItems.entanglement, 10));

                health = 1500;
                size = 3;
                armor = 5;
                maxNodes = 3;
                laserRange = 80;
                laserScale = 0.8f;
                schematicPriority = -20;
                researchCostMultiplier = 0.8f;
            }
        };

        ventDistiller = new ThermalGenerator("vent-distiller"){{
            requirements(Category.production, with(WHItems.manganese, 70, Items.graphite, 50));

            attribute = Attribute.steam;
            group = BlockGroup.liquids;
            displayEfficiencyScale = 1f / 9f;
            minEfficiency = 9f - 0.0001f;
            powerProduction = (750.00001f / 60f) / 9f;
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

        oxidationGenerator = new ConsumeGenerator("oxidation-generator"){{
            requirements(Category.power, with(WHItems.cobalt, 50, Items.graphite, 50, Items.silicon, 30));
            powerProduction = 450.0001f / 60f;

            size = 2;
            drawer = new DrawMulti(
            new DrawRegion("-bottom"),
            new DrawLiquidTile(Liquids.ozone, 5 / 4f),
            new DrawRegion("-mid"),
            new DrawCrucibleFlame(){{
                flameRad = 0.5f;
                circleSpace = circleStroke = 1f;
                particles = 12;
                particleRad = 6f;
            }},
            new DrawDefault(),
            new DrawGlowRegion(){{
                alpha = 1f;
                glowScale = 5f;
                color = Color.valueOf("c967b099");
            }});
            generateEffect = Fx.none;
            itemDuration = 120f;
            liquidCapacity = 20f * 3;
            consumeLiquids(LiquidStack.with(Liquids.ozone, 5 / 60f));
            itemDurationMultipliers.put(WHItems.chromium, 4);
            itemDurationMultipliers.put(WHItems.combustible, 6);
            itemDurationMultipliers.put(WHItems.sealedPromethium, 12);
            consume(new ConsumeItemFlammable());

            ambientSound = Sounds.loopSmelter;
            ambientSoundVolume = 0.06f;
        }};

        turboGenerator = new ConsumeGenerator("turbo-generator"){
            {
                requirements(Category.power, with(WHItems.cobalt, 80, Items.metaglass, 100, WHItems.ceramite, 80, WHItems.cobaltNitride, 50));
                size = 3;
                health = 1000;
                hasItems = hasLiquids = true;
                itemDuration = 20;
                consumeLiquid(Liquids.water, 40 / 60f);
                itemCapacity = 15;
                liquidCapacity = 150;
                powerProduction = 1500.0001f / 60f;
                effectChance = 0.08f;
                drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawLiquidTile(Liquids.water),
                new DrawRegion("-mid"),
                new DrawParticles(){{
                    color = Liquids.water.color.cpy().lerp(Pal.gray, 0.8f);
                    particleSize = 5;
                    particles = 20;
                    particleRad = 9f;
                    particleLife = 90;
                }},
                new DrawRegion("-rotator1", 0.6f * 9f){{
                    spinSprite = true;
                }},
                new DrawRegion("-rotator2", -0.6f * 3f){{
                    spinSprite = true;
                }},
                new DrawDefault(),
                new DrawGlowRegion(){{
                    alpha = 1f;
                    glowScale = 5f;
                    color = Pal.slagOrange;
                    ;
                }});
                generateEffect = Fx.generatespark;
                consume(new ConsumeItemFlammable(0.8f));
                itemDurationMultipliers.put(WHItems.chromium, 3);
                itemDurationMultipliers.put(WHItems.combustible, 6);
                itemDurationMultipliers.put(WHItems.sealedPromethium, 15);
                ambientSound = loopSmelter;
                ambientSoundVolume = 0.06f;
                researchCostMultiplier = 0.8f;
            }
        };

        crackingGenerator = new ConsumeGenerator("cracking-generator"){
            {
                requirements(Category.power,
                with(WHItems.cobalt, 100, Items.silicon, 100, WHItems.manganeseSteel, 70, WHItems.ceramite, 80, WHItems.resonantCrystal, 50));
                powerProduction = 2500.001f / 60f;

                drawer = new DrawMulti(new DrawRegion("-bottom"),
                new DrawLiquidTile(WHLiquids.orePromethium),
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

                consumeLiquids(LiquidStack.with(WHLiquids.orePromethium, 20 / 60f, Liquids.hydrogen, 15f / 60f));
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
                requirements(Category.power, with(Items.silicon, 120, WHItems.armorAlloy, 70, Items.plastanium, 50));

                size = 3;
                health = 1400;
                powerProduction = 5.5f;
                generateEffect = Fx.redgeneratespark;
                effectChance = 0.011f;
                drawer = new DrawMulti(
                new DrawDefault(),
                new DrawFade(){{
                    scale = 15f;
                }});
                researchCostMultiplier = 0.8f;
            }
        };

        promethiunmRector = new NuclearReactor("promethium-reactor"){
            {
                requirements(Category.power, with(WHItems.chromium, 100, WHItems.uranium, 100, Items.silicon, 200, WHItems.resonantCrystal, 50, WHItems.molybdenumAlloy, 50));

                health = 3500;
                size = 3;
                liquidCapacity = 80;
                itemCapacity = 10;
                hasItems = hasLiquids = outputsPower = true;
                powerProduction = 5500 / 60f + 0.0001f;
                heatOutput = 0;
                itemDuration = 200;
                explosionRadius = 28;
                explosionDamage = 3000 * 4;
                fuelItem = WHItems.uranium;
                heating = 0.015f;

                consumeItem(fuelItem);
                consumeLiquid(WHLiquids.refinePromethium, 5 / 60f).update(false);

                explodeEffect = WHFx.promethiunmRectorExplosion;
                explodeSound = explosionReactor;
                researchCostMultiplier = 0.8f;
                //钷素反应堆
            }
        };

        T2impactReactor = new ImpactReactor("detonation-reactor"){
            {
                requirements(Category.power, with(WHItems.cobalt, 1500, WHItems.manganeseSteel, 500,
                WHItems.molybdenumAlloy, 200, WHItems.sealedPromethium, 100));

                size = 4;
                health = 10000;
                armor = 10;
                liquidCapacity = 240;
                itemCapacity = 40;
                hasItems = true;
                hasLiquids = true;
                outputsPower = true;
                powerProduction = 40 * 1000 / 60f + 0.0001f;
                itemDuration = 5 * 60f;
                warmupSpeed = 0.0014f;
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
                consumeItems(with(WHItems.sealedPromethium, 1, WHItems.culverCrystal, 2));
                consumeLiquid(WHLiquids.refinePromethium, 60 / 60f);
                ambientSound = loopPulse;
                ambientSoundVolume = 0.1f;
                explosionShake = 8f;
                explosionShakeDuration = 20f;
                explosionRadius = 20;
                explosionDamage = 3000 * 4;
                explosionMinWarmup = 0.85f;
                float r = explosionRadius / 2f * tilesize;
                explodeEffect = new MultiEffect(
                WHFx.generalExplosion(120, ShootOrange, r * 1.5f, 10, false),
                WHFx.hitSpark(ShootOrange, 120, 30, r * 2, 2.5f, 11f),
                WHFx.trailCircleHitSpark(ShootOrange, 60, 20, r * 2, 2, 12f),
                WHFx.multipRings(ShootOrange, r * 1.5f, 3, 70),
                WHFx.circleOut(ShootOrange, 120, r * 2f),
                WHFx.subEffect(200, r * 1.5f, 8, 50, Interp.pow2In, (id, x, y, rot, fin) -> {
                    Draw.color(ShootOrange);
                    blend(Blending.additive);
                    float radius = Interp.pow3Out.apply(fin) * 50;
                    float fout = 1 - fin;
                    Fill.light(x, y, circleVertices(radius), radius, Color.clear, Tmp.c1.set(ShootOrange).a(Interp.pow3Out.apply(fout)));
                    Drawf.light(x, y, radius * 1.3F, ShootOrange, 0.7F * WHFx.fout(fin, 0.5f));
                    blend();

                    Lines.stroke(2.5f * Interp.pow10Out.apply(fout));
                    Lines.circle(x, y, radius);
                    rand.setSeed(id);
                    randLenVectors(id, 15, radius * rand.random(0.1f, 1.2f), (x1, y1) -> {
                        float ang = Mathf.angle(x1, y1);
                        Lines.stroke(2f * fin);
                        lineAngle(x + x1, y + y1, ang, fout * rand.random(0.35f, 1.25f) * 12f);
                    });
                }));
                explodeSound = Sounds.explosionReactor2;

                researchCostMultiplier = 0.8f;
            }
        };


        plaRector = new PlaRector("plasma-reactor"){
            {
                requirements(Category.power, with(WHItems.cobalt, 2000, Items.silicon, 4000, WHItems.cobaltNitride, 800,
                WHItems.molybdenumAlloy, 500, WHItems.refineCeramite, 500, WHItems.sealedPromethium, 800));

                health = 20000;
                size = 5;
                ambientSound = Sounds.loopFlux;
                ambientSoundVolume = 0.13f;
                effectChance = 0.05f;

                hasItems = true;
                hasLiquids = true;
                itemCapacity = 120;
                liquidCapacity = 120 * 10f;
                consumeLiquid(WHLiquids.liquidNitrogen, 80 / 60f);
                consumeLiquid(WHLiquids.refinePromethium, 90 / 60f);
                powerProduction = 150 * 1000f / 60f + 0.0001f;
                maxHeat = 120f;
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
                new DrawBubbles(){{
                    spread = 1;
                    recurrence = 20;
                    radius = amount = 10;
                }},
                new DrawArcs(){{
                    flameColor = midColor = SkyBlueF.cpy().a(0);
                }},
                new DrawArcs(){{
                    flameColor = midColor = SkyBlueF;
                    arcs = 5;
                    flameRad = 2f;
                }},
                new DrawDefault(),
                new DrawHeatInput(),
                new DrawGlowRegion("-ventglow"){{
                    color = Color.valueOf("32603a");
                }});


                researchCostMultiplier = 0.6f;
            }
        };

        smallBattery = new Battery("small-battery"){
            {
                requirements(Category.power, with(WHItems.manganese, 50, WHItems.cobalt, 20, Items.silicon, 30));
                health = 300;
                size = 2;
                consumePowerBuffered(20 * 1000f);
                emptyLightColor = Pal.coalBlack;
                baseExplosiveness = 2f;
                researchCostMultiplier = 0.8f;
            }
        };

        smallBatteryRebel = new Battery("small-battery-rebel"){
            {
                requirements(Category.power, BuildVisibility.editorOnly, with(WHItems.manganese, 50, WHItems.cobalt, 20, Items.silicon, 30));
                health = 300;
                size = 2;
                consumePowerBuffered(20 * 1000f);
                emptyLightColor = Pal.coalBlack;
                fullLightColor = Color.valueOf("F86060FF");
                baseExplosiveness = 2f;
                researchCostMultiplier = 0.8f;
            }
        };

        midBattery = new Battery("mid-battery"){
            {
                requirements(Category.power, with(WHItems.manganese, 200, WHItems.cobalt, 200,
                WHItems.cobaltNitride, 50, WHItems.armorAlloy, 30));
                health = 2000;
                size = 3;
                consumePowerBuffered(60 * 1000f);
                emptyLightColor = Pal.coalBlack;
                baseExplosiveness = 8f;
                researchCostMultiplier = 0.8f;
            }
        };

        midBatteryRebel = new Battery("mid-battery-rebel"){
            {
                requirements(Category.power, BuildVisibility.editorOnly, with(WHItems.manganese, 200, WHItems.cobalt, 200,
                WHItems.cobaltNitride, 50, WHItems.armorAlloy, 30));
                health = 2000;
                size = 3;
                consumePowerBuffered(60 * 1000f);
                emptyLightColor = Pal.coalBlack;
                fullLightColor = Color.valueOf("F86060FF");
                baseExplosiveness = 8f;
            }
        };

        largeBattery = new Battery("large-battery"){
            {
                requirements(Category.power, with(WHItems.manganeseSteel, 200, WHItems.cobalt, 400,
                WHItems.molybdenumAlloy, 50, WHItems.entanglement, 100));
                health = 4500;
                size = 4;
                consumePowerBuffered(130 * 1000f);
                emptyLightColor = Pal.coalBlack;
                baseExplosiveness = 14f;
                researchCostMultiplier = 0.8f;
            }
        };

        largeBatteryRebel = new Battery("large-battery-rebel"){
            {
                requirements(Category.power, BuildVisibility.editorOnly,
                with(WHItems.manganeseSteel, 200, WHItems.cobalt, 400,
                WHItems.molybdenumAlloy, 50, WHItems.entanglement, 100));

                health = 4500;
                size = 4;
                consumePowerBuffered(130 * 1000f);
                emptyLightColor = Pal.coalBlack;
                fullLightColor = Color.valueOf("F86060FF");
                baseExplosiveness = 14f;
                researchCostMultiplier = 0.8f;
            }
        };

       /* MK3battery = new ShieldWall("MK3-reinforced-battery"){
            {

                requirements(Category.power, with(Items.silicon, 800, WHItems.manganeseSteel, 400, WHItems.refineCeramite, 400, WHItems.sealedPromethium, 200));

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
        };*/

        //effect
        armoredContainer = new StorageBlock("armored-container"){
            {
                requirements(Category.effect, with(WHItems.cobalt, 150, WHItems.manganeseSteel, 100, Items.plastanium, 100));

                health = 1000;
                size = 2;
                itemCapacity = 1000;
                researchCostMultiplier = 0.3f;
                category = Category.effect;
                armor = 6;
            }
        };

        armoredVault = new StorageBlock("armored-vault"){
            {
                requirements(Category.effect, with(WHItems.cobalt, 500, Items.silicon, 1000, WHItems.armorAlloy, 500, WHItems.ceramite, 500));

                health = 3600;
                size = 3;
                itemCapacity = 10 * 1000;
                researchCostMultiplier = 0.3f;
                category = Category.effect;
                armor = 12;
            }
        };
        wrapProjector = new RegenProjector("wrap-projector"){
            {
                requirements(Category.effect, with(Items.plastanium, 100, Items.silicon, 200, WHItems.manganeseSteel, 100, Items.carbide, 100, WHItems.sealedPromethium, 50));

                health = 1500;
                size = 3;
                armor = 6;
                canOverdrive = false;
                healPercent = 2 / 60f;
                squareSprite = true;
                baseColor = Pal.sapBullet;
                drawer = new DrawMulti(
                new DrawLiquidTile(Liquids.nitrogen, FACTORY_PAD_33),
                new DrawGlowRegion(){{
                    color = Pal.sapBullet;
                }}, new DrawPulseShape(){{
                    square = false;
                    color = Pal.sapBullet.cpy();
                }}, new DrawShape(){{
                    color = Pal.sapBullet.cpy();
                    sides = 4;
                    radius = 4f;
                    useWarmupRadius = true;
                }},
                new DrawDefault(),
                new DrawWarmupRegion());
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
                /*   destroyBullet = WHBullets.warpBreak;*/
                range = 50;
                hasLiquids = true;
                consumePower(15);
                consumeLiquid(Liquids.nitrogen, 0.25f);
                researchCostMultiplier = 0.7f;
            }
        };

        wrapOverdrive = new SelectOverdriveProjector("wrap-overdrive"){
            {
                requirements(Category.effect, BuildVisibility.sandboxOnly, with(Items.silicon, 250, WHItems.manganeseSteel, 250, Items.surgeAlloy, 150, Items.phaseFabric, 80, WHItems.sealedPromethium, 50));

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
                status = new StatusEffect[]{WHStatusEffects.powerReduce2};
                boostStatus = new StatusEffect[]{WHStatusEffects.assault};
                squareSprite = false;
                consumePower(10);
                consumeItems(with(WHItems.sealedPromethium, 1)).boost();
                destroyBullet = WHBullets.warpBreak;
                researchCostMultiplier = 0.7f;
            }
        };

        shelterDome = new ShelterDome("shelter-dome"){
            {
                requirements(Category.effect, with(WHItems.cobaltNitride, 50, WHItems.ceramite, 100, WHItems.resonantCrystal, 50));
                size = 4;
                range = 360;
                consumePower(1200 / 60f);
                researchCostMultiplier = 0.7f;
            }
        };


        repairTower = new RepairTower("energy-repair-tower"){
            {
                requirements(Category.effect, with(Items.plastanium, 400, WHItems.ceramite, 300, WHItems.resonantCrystal, 200));

                health = 1500;
                size = 3;
                liquidCapacity = 200;
                range = 220;
                healAmount = 800 / 60f;
                circleSpeed = 75;
                circleStroke = 8;
                squareRad = 8;
                squareSpinScl = 1.2f;
                glowMag = 0.3f;
                glowScl = 12f;
                consumePower(1500 / 60f);
                consumeLiquid(WHLiquids.refinePromethium, 10f);
                researchCostMultiplier = 0.6f;
            }
        };

        voidShield = new BaseForceProjector("fortless-level-void-shield"){
            {
                requirements(Category.effect, with(
                WHItems.molybdenum, 1500, WHItems.ceramite, 500, WHItems.molybdenumAlloy, 300, WHItems.entanglement, 100));

                health = 5000;
                size = 5;
                radius = 320;
                sides = 30;
                canOverdrive = false;
                shieldHealth = 16000;
                phaseRadiusBoost = 6;
                phaseShieldBoost = 10000;
                cooldownNormal = 1000 / 60f;
                cooldownBrokenBase = 600 / 60f;
                coolantConsumption = 25 / 60f;
                cooldownLiquid = 1.1f;
                liquidCapacity = 150;
                itemCapacity = 20;
                phaseUseTime = 240f;
                itemConsumer = consumeItem(WHItems.sealedPromethium).boost();
                consumePower(4400 / 60f);
                researchCostMultiplier = 0.7f;
            }
        };

        ionShield = new ForceProjector("ion-shield"){{
            requirements(Category.effect, with(WHItems.cobalt, 150, WHItems.armorAlloy, 50, Items.silicon, 150));

            size = 3;
            sides = 20;
            shieldHealth = 2500;
            phaseShieldBoost = 1500;
            radius = 150;
            phaseRadiusBoost = 80f;
            cooldownNormal = shieldHealth / 15f / 60f;
            cooldownBrokenBase = shieldHealth / 20 / 60f;
            cooldownLiquid = 1.1f;
            phaseUseTime = 240f;
            coolantConsumption = 10 / 60f;
            itemConsumer = consumeItem(WHItems.entanglement).boost();

            consumePower(4f);
        }};

        selectProjector = new SelectForceProjector("select-projector"){{
            requirements(Category.effect, with(WHItems.resonantCrystal, 100, WHItems.molybdenumAlloy, 100, WHItems.sealedPromethium, 100));
            size = 4;
            OneTileShieldHealth = 250f;
            phaseShieldBoost = 250;
            cooldownNormal = 1.5f;
            cooldownLiquid = 1.2f;
            cooldownBrokenBase = 0.5f;
            range = 800f;

            itemConsumer = consumeItem(WHItems.sealedPromethium).boost();
            consumePower(100f);
        }};


        strongholdCore = new FrontlineCoreBlock("s-core"){
            {
                requirements(Category.effect, with(WHItems.manganeseSteel, 1000, Items.silicon, 1000, WHItems.cobaltNitride, 200));

                unitType = WHUnitTypes.reborn;
                armor = 22;
                health = 5000;
                itemCapacity = 2000;
                size = 4;
                unitCapModifier = 10;
                researchCostMultiplier = 0.3f;
            }
        };

        T2strongholdCore = new CoreBlock("m-core"){
            {
                requirements(Category.effect, with(WHItems.cobalt, 1000, WHItems.uranium, 1000, Items.silicon, 2000, WHItems.armorAlloy, 300, WHItems.ceramite, 500));
                unitType = WHUnitTypes.recovery;
                armor = 35;
                health = 12000;
                itemCapacity = 15000;
                size = 5;
                unitCapModifier = 15;
                researchCostMultiplier = 0.3f;
            }
        };

        T3strongholdCore = new CoreBlock("l-core"){
            {
                requirements(Category.effect, with(WHItems.molybdenum, 3000, WHItems.manganeseSteel, 5000, Items.silicon, 8000
                , WHItems.refineCeramite, 1000, WHItems.protocolChip, 500));
                unitType = WHUnitTypes.restore;
                armor = 35;
                health = 30000;
                itemCapacity = 40000;
                size = 6;
                unitCapModifier = 15;
                researchCostMultiplier = 0.3f;
            }
        };

        //units
        airFactory = new UnitFactory("air-factory"){{
            requirements(Category.units, with(Items.graphite, 35, Items.silicon, 120, WHItems.manganeseSteel, 25f));

            size = 3;
            plans = Seq.with(
            new UnitPlan(WHUnitTypes.air1, 60f * 30, with(Items.graphite, 50, Items.metaglass, 30, Items.silicon, 70)),
            new UnitPlan(WHUnitTypes.airB1, 60f * 40, with(Items.graphite, 50, Items.silicon, 40, WHItems.manganeseSteel, 15))
            );
            fogRadius = 3;
            consumePower(180 / 60f);
            researchCostMultiplier = 0.5f;
        }};

        groundFactory = new UnitFactory("ground-factory"){{
            requirements(Category.units, with(Items.graphite, 50, WHItems.manganese, 150, Items.silicon, 100));
            plans = Seq.with(
            new UnitPlan(WHUnitTypes.M1, 60f * 20, with(Items.graphite, 30, Items.silicon, 30, WHItems.chromium, 30))
            );
            size = 3;
            fogRadius = 3;
            consumePower(180 / 60f);
            researchCostMultiplier = 0.5f;
        }};

        mechaFactory = new UnitFactory("mecha-factory"){{
            requirements(Category.units, with(WHItems.manganeseSteel, 100, Items.tungsten, 120, Items.silicon, 100, Items.plastanium, 50));

            plans = Seq.with(
            new UnitPlan(WHUnitTypes.Mecha2, 60f * 60, with(WHItems.manganeseSteel, 50, WHItems.uranium, 100, Items.silicon, 150, Items.plastanium, 35))
            );

            size = 3;
            consumePower(240 / 6f);
            consumeLiquid(Liquids.hydrogen, 22.5f / 60f);
            researchCostMultiplier = 0.75f;
        }};

        tankFactory = new UnitFactory("tank-factory"){{
            requirements(Category.units, with(WHItems.molybdenum, 1000, Items.silicon, 1500, WHItems.manganeseSteel, 800, WHItems.ceramite, 300, WHItems.resonantCrystal, 200));

            size = 7;
            consumePower(30f);
            consumeLiquid(Liquids.nitrogen, 40 / 60f);
            consumeLiquid(WHLiquids.refinePromethium, 30 / 60f);
            createSound = Sounds.unitCreateBig;

            plans = Seq.with(
            new UnitPlan(WHUnitTypes.tank1, 60f * 150f, with(WHItems.manganeseSteel, 300, WHItems.ceramite, 400, WHItems.molybdenumAlloy, 150, Items.silicon, 800)),
            new UnitPlan(WHUnitTypes.tank1s, 60f * 150f, with(WHItems.manganeseSteel, 600, WHItems.ceramite, 400, WHItems.molybdenumAlloy, 150, Items.silicon, 800))
            );

            researchCostMultiplier = 0.75f;
        }};

        t2Module = new MultReconstructor("t2-modification-module"){{
            requirements(Category.units, with(WHItems.manganeseSteel, 80, Items.tungsten, 80, Items.silicon, 200, Items.plastanium, 50));

            size = 3;
            consumePower(8f);
            consumeLiquid(Liquids.hydrogen, 15f / 60f);

            addUpgrade(WHUnitTypes.M1, WHUnitTypes.M2, with(WHItems.manganeseSteel, 60, WHItems.cobalt, 50, Items.silicon, 70));
            addUpgrade(WHUnitTypes.air1, WHUnitTypes.air2, with(WHItems.manganeseSteel, 70, Items.graphite, 80, Items.silicon, 40));
            addUpgrade(WHUnitTypes.airB1, WHUnitTypes.airB2, with(WHItems.manganeseSteel, 70, WHItems.uranium, 50, Items.silicon, 70));

            constructTime = 60f * 20f;
            researchCostMultiplier = 0.75f;
        }};


        t3Module = new MultReconstructor("t3-modification-module"){{
            requirements(Category.units, with(WHItems.uranium, 300, WHItems.manganeseSteel, 200, Items.silicon, 500, WHItems.ceramite, 90, WHItems.cobaltNitride, 100));

            size = 5;
            consumePower(15f);
            consumeLiquid(Liquids.nitrogen, 40 / 60f);

            addUpgrade(WHUnitTypes.M2, WHUnitTypes.M3, with(WHItems.manganeseSteel, 90, WHItems.uranium, 150, Items.plastanium, 70, Items.silicon, 200));
            addUpgrade(WHUnitTypes.air2, WHUnitTypes.air3, with(WHItems.manganeseSteel, 120, WHItems.ceramite, 70, Items.silicon, 130));
            addUpgrade(WHUnitTypes.airB2, WHUnitTypes.airB3, with(WHItems.manganeseSteel, 120, WHItems.uranium, 150, Items.plastanium, 80, Items.silicon, 130));

            addUpgrade(WHUnitTypes.Mecha2, WHUnitTypes.Mecha3, with(WHItems.armorAlloy, 200, WHItems.ceramite, 150, WHItems.combustible, 100, Items.silicon, 600));

            constructTime = 60f * 40f;
            researchCostMultiplier = 0.75f;
        }};

        t4Module = new MultReconstructor("t4-modification-module"){{
            requirements(Category.units, with(WHItems.uranium, 1000, Items.silicon, 1500, WHItems.cobaltNitride, 200, WHItems.armorAlloy, 300,
            WHItems.ceramite, 300, WHItems.resonantCrystal, 300));

            size = 7;
            consumePower(30f);
            consumeLiquid(Liquids.nitrogen, 40 / 60f);
            consumeLiquid(WHLiquids.refinePromethium, 30 / 60f);
            createSound = Sounds.unitCreateBig;

            constructTime = 60f * 70f;

            addUpgrade(WHUnitTypes.M3, WHUnitTypes.M4A, with(WHItems.armorAlloy, 200, WHItems.ceramite, 300, Items.silicon, 800));

            addUpgrade(WHUnitTypes.air3, WHUnitTypes.air4, with(WHItems.armorAlloy, 300, WHItems.ceramite, 300, Items.silicon, 800));
            addUpgrade(WHUnitTypes.airB3, WHUnitTypes.airB4, with(WHItems.armorAlloy, 300, WHItems.ceramite, 150, Items.silicon, 1100));

            addUpgrade(WHUnitTypes.Mecha3, WHUnitTypes.Mecha4, with(WHItems.molybdenumAlloy, 250,
            WHItems.ceramite, 400, WHItems.resonantCrystal, 100, Items.silicon, 800));

            researchCostMultiplier = 0.75f;
        }};


        t5Module = new MultReconstructor("t5-modification-module"){{
            requirements(Category.units, with(WHItems.armorAlloy, 1000, Items.silicon, 3000, WHItems.cobaltNitride, 600,
            WHItems.ceramite, 800, WHItems.molybdenumAlloy, 400, WHItems.entanglement, 200));

            size = 9;
            consumePower(50f);
            consumeLiquid(WHLiquids.liquidNitrogen, 80 / 60f);

            constructTime = 60f * 90f;
            createSound = Sounds.unitCreateBig;

            addUpgrade(WHUnitTypes.M4A, WHUnitTypes.M5, with(WHItems.ceramite, 500, WHItems.entanglement, 100,
            WHItems.protocolChip, 150, Items.silicon, 1200));

            addUpgrade(WHUnitTypes.air4, WHUnitTypes.air5, with(WHItems.ceramite, 500, WHItems.entanglement, 200,
            WHItems.protocolChip, 150, Items.silicon, 1200));

            addUpgrade(WHUnitTypes.airB4, WHUnitTypes.airB5, with(WHItems.ceramite, 400, WHItems.entanglement, 150,
            WHItems.protocolChip, 150, Items.silicon, 1200));

            addUpgrade(WHUnitTypes.tank1, WHUnitTypes.tank2, with(WHItems.ceramite, 800, WHItems.entanglement, 200,
            WHItems.molybdenumAlloy, 350, Items.silicon, 1200));

            addUpgrade(WHUnitTypes.tank1s, WHUnitTypes.tank2s, with(WHItems.ceramite, 800, WHItems.entanglement, 200,
            WHItems.molybdenumAlloy, 350, Items.silicon, 1200));

            addUpgrade(WHUnitTypes.Mecha4, WHUnitTypes.Mecha5, with(WHItems.ceramite, 500, WHItems.molybdenumAlloy, 200,
            WHItems.protocolChip, 100, Items.silicon, 1200));

            researchCostMultiplier = 0.5f;
        }};


        t6Module = new MultReconstructor("exterminate-reconstructor"){{
            requirements(Category.units, with(WHItems.armorAlloy, 1500, WHItems.cobaltNitride, 1000, WHItems.ceramite, 2000,
            WHItems.protocolChip, 500, WHItems.refineCeramite, 1000, WHItems.culverCrystal, 500));

            size = 11;
            consumePower(100f);
            consumeLiquid(WHLiquids.liquidNitrogen, 160 / 60f);

            constructTime = 60f * 60f * 2;
            createSound = Sounds.unitCreateBig;

            addUpgrade(WHUnitTypes.M5, WHUnitTypes.M6, with(WHItems.protocolChip, 200, WHItems.sealedPromethium, 200,
            WHItems.refineCeramite, 300));

            addUpgrade(WHUnitTypes.air5, WHUnitTypes.air6, with(WHItems.protocolChip, 200, WHItems.culverCrystal, 200, WHItems.sealedPromethium, 150,
            WHItems.refineCeramite, 400));

            addUpgrade(WHUnitTypes.airB5, WHUnitTypes.airB6, with(WHItems.protocolChip, 200, WHItems.protocolChip, 300,
            WHItems.refineCeramite, 500));

            addUpgrade(WHUnitTypes.tank2, WHUnitTypes.tank3, with(WHItems.protocolChip, 200, WHItems.sealedPromethium, 100,
            WHItems.refineCeramite, 600));

            addUpgrade(WHUnitTypes.tank2s, WHUnitTypes.tank3s, with(WHItems.protocolChip, 300, WHItems.culverCrystal, 200,
            WHItems.sealedPromethium, 200, WHItems.refineCeramite, 600));

            addUpgrade(WHUnitTypes.Mecha5, WHUnitTypes.Mecha6, with(WHItems.protocolChip, 400, WHItems.sealedPromethium, 200,
            WHItems.culverCrystal, 150, WHItems.refineCeramite, 600));

            researchCostMultiplier = 0.5f;
        }};

        jumpBeacon = new UnitCallBlock("jump-beacon"){{
            requirements(Category.units, with(Items.silicon, 500, WHItems.manganeseSteel, 300, WHItems.cobaltNitride, 500, WHItems.ceramite, 200, WHItems.molybdenumAlloy, 200));

            health = 1000;
            range = 150;
            spawnRange = 50;
            size = 4;

            plans = Seq.with(
            new UnitPlan(WHUnitTypes.airB1, 60f * 40, false, with(Items.graphite, 50, Items.silicon, 40, WHItems.manganeseSteel, 15)),
            new UnitPlan(WHUnitTypes.airB2, 60f * 60, false, with(WHItems.manganeseSteel, 50, Items.graphite, 80, Items.silicon, 40)),
            new UnitPlan(WHUnitTypes.airB3, 60f * 80, false, with(WHItems.manganeseSteel, 200, Items.tungsten, 200, Items.plastanium, 100, Items.silicon, 200)),
            new UnitPlan(WHUnitTypes.airB4, 60f * 140, false, with(WHItems.manganeseSteel, 500, WHItems.ceramite, 400, WHItems.sealedPromethium, 100, Items.silicon, 2000)),

            new UnitPlan(WHUnitTypes.tankEn1, 60f * 150, true, with(WHItems.manganeseSteel, 500, WHItems.ceramite, 300, WHItems.resonantCrystal, 150, Items.silicon, 2000)),
            new UnitPlan(WHUnitTypes.tankEn2, 60f * 250, true, with(WHItems.manganeseSteel, 500, WHItems.ceramite, 400, WHItems.protocolChip, 200,
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
            requirements(Category.units, with(WHItems.molybdenumAlloy, 2000, WHItems.cobaltNitride, 2500, WHItems.refineCeramite, 1500, WHItems.protocolChip, 1000));
            health = 4000;
            size = 6;
            range = 300;
            spawnRange = 150;
            plans = Seq.with(
            new UnitPlan(WHUnitTypes.air7, 60f * 60f * 7.5f, true, with(WHItems.armorAlloy, 4000, WHItems.refineCeramite, 3000,
            WHItems.adamantium, 2000, WHItems.protocolChip, 1200)),
            new UnitPlan(WHUnitTypes.tankAG, 60f * 60f * 7.5f, true, with(WHItems.armorAlloy, 4000, WHItems.refineCeramite, 3000,
            WHItems.adamantium, 2000, WHItems.protocolChip, 800)),
            new UnitPlan(WHUnitTypes.Mecha7, 60f * 60f * 7.5f, true, with(WHItems.armorAlloy, 4000, WHItems.refineCeramite, 3000,
            WHItems.adamantium, 2000, WHItems.protocolChip, 800))
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
                Fill.circle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, s * 0.65f);
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
            requirements(Category.units, with(WHItems.ceramite, 100, WHItems.manganeseSteel, 150, WHItems.resonantCrystal, 80));
            size = 5;
            reload = 120f;
            chargeTime = 90f;
            range = 2500;
            maxPayloadSize = 4.5f;
            grabWidth = 11f;
            consumePower(15f);
        }};

        armorPayloadConveyor = new PayloadConveyor("armor-payload-conveyor"){{
            requirements(Category.units, with(WHItems.manganeseSteel, 10, Items.graphite, 10));
            moveTime = 35f;
            canOverdrive = false;
            health = 800;
            researchCostMultiplier = 3f;
            underBullets = true;
        }};

        armorPayloadRouter = new PayloadRouter("armor-payload-router"){{
            requirements(Category.units, with(WHItems.manganeseSteel, 10, Items.graphite, 10));
            moveTime = 35f;
            health = 800;
            canOverdrive = false;
            researchCostMultiplier = 3f;
            underBullets = true;
        }};


        serpuloT6Assembler = new ConfigurableUnitAssembler("t6-assembler"){{
            requirements(Category.units, with(Items.silicon, 6000, Items.thorium, 3000, Items.plastanium, 1500, Items.phaseFabric, 1500, Items.surgeAlloy, 1500));
            size = 8;
            droneType = WHUnitTypes.t6AssemblyDrone;
            plans.addAll(
            new AssemblerUnitPlan(WHUnitTypes.airS6, 60f * 240f, PayloadStack.list(UnitTypes.eclipse, 1)){{
                itemReq = with(Items.silicon, 2400, Items.plastanium, 2000, Items.surgeAlloy, 1000, Items.phaseFabric, 1000);
            }},
            new AssemblerUnitPlan(WHUnitTypes.airSGreen6, 60f * 240f, PayloadStack.list(UnitTypes.oct, 1)){{
                itemReq = with(Items.silicon, 2000, Items.plastanium, 1500, Items.surgeAlloy, 1000, Items.phaseFabric, 1400);
            }},
            new AssemblerUnitPlan(WHUnitTypes.mechaS6, 60f * 240f, PayloadStack.list(UnitTypes.reign, 1)){{
                itemReq = with(Items.silicon, 2000, Items.plastanium, 1500, Items.surgeAlloy, 1500, Items.phaseFabric, 1000);
            }},
            new AssemblerUnitPlan(WHUnitTypes.mechaSGreen6, 60f * 240f, PayloadStack.list(UnitTypes.corvus, 1)){{
                itemReq = with(Items.silicon, 2000, Items.plastanium, 1500, Items.surgeAlloy, 1200, Items.phaseFabric, 1200);
            }},
            new AssemblerUnitPlan(WHUnitTypes.meshSPurple6, 60f * 240f, PayloadStack.list(UnitTypes.toxopid, 1)){{
                itemReq = with(Items.silicon, 2000, Items.plastanium, 1300, Items.surgeAlloy, 1300, Items.phaseFabric, 1300);
            }},
            new AssemblerUnitPlan(WHUnitTypes.navyS6, 60f * 150f, PayloadStack.list(UnitTypes.omura, 1)){{
                itemReq = with(Items.silicon, 2000, Items.plastanium, 1600, Items.surgeAlloy, 1600, Items.phaseFabric, 1300);
            }},
            new AssemblerUnitPlan(WHUnitTypes.navySGreen6, 60f * 240f, PayloadStack.list(UnitTypes.navanax, 1)){{
                itemReq = with(Items.silicon, 2000, Items.plastanium, 1600, Items.surgeAlloy, 1400, Items.phaseFabric, 1400);
            }}
            );
            areaSize = 16;
            researchCostMultiplier = 0.4f;

            consumePower(3000 / 60f);
            consumeLiquid(Liquids.cryofluid, 360 / 60f);
        }};


        //Walls
        primarySteelWall = new Wall("primary-steel-wall"){{
            requirements(Category.defense, with(WHItems.manganese, 6, WHItems.manganeseSteel, 4));
            health = 500;
            researchCostMultiplier = 0.25f;
        }};

        largePrimarySteelWall = new Wall("large-primary-steel-wall"){{
            requirements(Category.defense, ItemStack.mult(primarySteelWall.requirements, 4));
            health = 500 * 4;
            size = 2;

            researchCostMultiplier = 0.3f;
        }};

        heavySteelWall = new ReactionArmorWall("heavy-steel-wall"){{
            requirements(Category.defense, with(WHItems.molybdenumAlloy, 10, Items.carbide, 15));
            health = 3000;
            frequency = 20;
            armor = 15;
            immunityAccount = 2;
            chanceDeflect = 30;

            researchCostMultiplier = 0.6f;
        }};

        largeHeavySteelWall = new ReactionArmorWall("large-heavy-steel-wall"){{
            requirements(Category.defense, with(WHItems.molybdenumAlloy, 40, Items.carbide, 60, Items.surgeAlloy, 20));
            health = 3000 * 4 + 2000;
            size = 2;
            armor = 20;
            frequency = 20;
            immunityAccount = 2;
            chanceDeflect = 30;

            researchCostMultiplier = 0.6f;
        }};

        heavySteelDoor = new AutoDoor("heavy-steel-door"){{
            requirements(Category.defense, with(Items.silicon, 50, WHItems.ceramite, 40, Items.carbide, 40, WHItems.molybdenum, 50));
            health = 2500 * 4 + 1500;
            size = 2;
            armor = 22;
            chanceDeflect = 30;

            researchCostMultiplier = 0.6f;
        }};

        ceramiteWall = new ReactionArmorWall("ceramite-wall"){{
            requirements(Category.defense, with(WHItems.ceramite, 10, Items.tungsten, 10));
            health = 1800;
            armor = 6;
            insulated = true;
            frequency = 25;
            immunityAccount = 2;

            researchCostMultiplier = 0.6f;
        }};

        largeCeramiteWall = new ReactionArmorWall("large-ceramite-wall"){{
            requirements(Category.defense, with(WHItems.ceramite, 40, Items.tungsten, 40, WHItems.manganeseSteel, 15));
            health = 8000;
            armor = 10;
            size = 2;
            insulated = true;
            frequency = 25;
            immunityAccount = 2;

            researchCostMultiplier = 0.6f;
        }};

        ceramiteDoor = new AutoDoor("ceramite-door"){{
            requirements(Category.defense, with(Items.plastanium, 30, WHItems.ceramite, 40, Items.tungsten, 40, WHItems.manganeseSteel, 15));
            health = 7500;
            size = 2;
            armor = 12;
            insulated = true;

            researchCostMultiplier = 0.6f;
        }};

        refineCeramiteWall = new ReactionArmorWall("refine-ceramite-wall"){{
            requirements(Category.defense, with(WHItems.refineCeramite, 10, WHItems.molybdenumAlloy, 5));
            health = 5500;
            armor = 8;
            insulated = true;
            frequency = 20;
            immunityAccount = 2;
            shareDamage = true;
            maxShareStep = 1;
            lightningChance = 0.1f;
            lightningLength = 12;
            lightningDamage = 75;
            lightningColor = WHItems.refineCeramite.color.cpy();

            researchCostMultiplier = 0.8f;
        }};

        largeRefineCeramiteWall = new ReactionArmorWall("large-refine-ceramite-wall"){{
            requirements(Category.defense, with(WHItems.refineCeramite, 40, WHItems.molybdenumAlloy, 25));
            health = 24000;
            size = 2;
            armor = 15;
            insulated = true;
            frequency = 20;
            immunityAccount = 3;
            shareDamage = true;
            maxShareStep = 2;
            lightningChance = 0.1f;
            lightningLength = 18;
            lightningDamage = 75;
            lightningColor = WHItems.refineCeramite.color.cpy();

            researchCostMultiplier = 0.8f;
        }};

        promethiumChargeWall = new ReactionArmorShieldWall("promethium-charge-wall"){{
            requirements(Category.defense, with(WHItems.refineCeramite, 70, WHItems.sealedPromethium, 60, WHItems.protocolChip, 50));
            health = 13000;
            size = 2;
            frequency = 13;
            immunityAccount = 3;
            shareDamage = false;
            shieldHealth = 8000;
            breakCooldown = 60 * 15f;
            regenSpeed = 800 / 60f;
            chanceDeflect = 75f;

            outputsPower = false;
            hasPower = true;
            consumesPower = true;
            conductivePower = true;

            consumePower(100 / 60f);

            researchCostMultiplier = 0.8f;
        }};

        denseExplosionProofWall = new ReactionArmorShieldWall("dense-explosion-proof-wall"){{
            requirements(Category.defense, with(WHItems.adamantium, 80, WHItems.refineCeramite, 120, WHItems.sealedPromethium, 60, WHItems.protocolChip, 20));
            health = 28000;
            size = 2;
            frequency = 15;
            immunityAccount = 2;
            shareDamage = true;
            maxShareStep = 2;
            shieldHealth = 12000;
            breakCooldown = 60 * 20f;
            regenSpeed = 1200 / 60f;
            /*insulated = true;*/

            outputsPower = false;
            hasPower = true;
            consumesPower = true;
            conductivePower = true;

            consumePower(180 / 60f);

            researchCostMultiplier = 0.8f;
        }};

        //Turrets
        Crush = new ItemTurret("Crush"){{

            requirements(Category.turret, with(WHItems.manganese, 30, WHItems.chromium, 20));

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
            WHItems.manganese, WHBullets.CrushBulletLead,
            Items.metaglass, WHBullets.CrushBulletMetaGlass,
            WHItems.manganeseSteel, WHBullets.CrushBulletTiSteel
            );
            alwaysUnlocked = true;
            researchCostMultiplier = 0.6f;
        }};

        AutoGun = new ItemTurret("Auto-gun"){{

            requirements(Category.turret, with(WHItems.manganese, 80, Items.graphite, 50, Items.silicon, 50, WHItems.manganeseSteel, 50));

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
            WHItems.manganeseSteel, WHBullets.AutoGunTiSteel
            );
            alwaysUnlocked = true;
            researchCostMultiplier = 0.6f;
        }};

        Blaze = new ContinuousTurret("Blaze"){
            {
                requirements(Category.turret, with(Items.silicon, 100, Items.graphite, 100, WHItems.manganeseSteel, 50, Items.plastanium, 50));


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
                                -Time.time * 0.7f * random, 8, 0.8f,
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
            requirements(Category.turret, with(WHItems.manganeseSteel, 90, Items.silicon, 80, Items.metaglass, 80, Items.graphite, 60));

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
            requirements(Category.turret, with(WHItems.manganeseSteel, 80, Items.silicon, 150, Items.plastanium, 80, Items.phaseFabric, 20));

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
            WHItems.manganeseSteel, WHBullets.SSWordTiSteel,
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
            requirements(Category.turret, with(Items.thorium, 50, WHItems.manganeseSteel, 100, Items.graphite, 150, WHItems.resonantCrystal, 20));

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

            requirements(Category.turret, with(WHItems.manganeseSteel, 500, Items.carbide, 200, WHItems.ceramite, 200, WHItems.refineCeramite, 100, WHItems.sealedPromethium, 50));

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
                requirements(Category.turret, with(WHItems.manganeseSteel, 180, WHItems.resonantCrystal, 60, Items.carbide, 100, WHItems.ceramite, 150));
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
            requirements(Category.turret, with(Items.silicon, 200, Items.plastanium, 100, Items.thorium, 300, WHItems.manganeseSteel, 120, WHItems.molybdenumAlloy, 50));

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
                requirements(Category.turret, with(WHItems.manganeseSteel, 100, WHItems.resonantCrystal, 100, WHItems.ceramite, 100, WHItems.molybdenumAlloy, 50));
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
                heatColor = WHPal.MnSteelColor.cpy().lerp(WHPal.Heat, 0.5f);
                cooldownTime = 110;
                shootY = 14;
                consumePower(1500 / 60f);
                coolant = consumeCoolant(30 / 60f);
                drawer = new DrawMulti(new DrawTurret(WarHammerMod.name("turret-")){{
                    parts.add(new RegionPart("-light"){{
                        layer = 110;
                        colorTo = WHPal.SkyBlueF;
                        color = WHPal.MnSteelColor.cpy();
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
                        lightningColor = hitColor = WHPal.MnSteelColor;
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
                requirements(Category.turret, with(WHItems.manganeseSteel, 200, WHItems.ceramite, 300, WHItems.resonantCrystal, 120, WHItems.molybdenumAlloy, 150));
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

            requirements(Category.turret, with(WHItems.manganeseSteel, 200, WHItems.ceramite, 200, WHItems.molybdenumAlloy, 180, WHItems.sealedPromethium, 50));

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

            requirements(Category.turret, with(WHItems.manganeseSteel, 200, WHItems.ceramite, 200, WHItems.molybdenumAlloy, 180, WHItems.sealedPromethium, 50));

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

            requirements(Category.turret, with(WHItems.manganeseSteel, 800, WHItems.resonantCrystal, 150, WHItems.ceramite, 400, WHItems.molybdenumAlloy, 300, WHItems.sealedPromethium, 150, WHItems.adamantium, 90));

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

            requirements(Category.turret, with(WHItems.manganeseSteel, 200, Items.carbide, 200, WHItems.ceramite, 250, WHItems.molybdenumAlloy, 100, WHItems.resonantCrystal, 80, WHItems.sealedPromethium, 50));
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
            requirements(Category.turret, with(Items.surgeAlloy, 600, WHItems.manganeseSteel, 1000, WHItems.protocolChip, 300,
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
            requirements(Category.turret, with(Items.carbide, 300, WHItems.manganeseSteel, 900, WHItems.protocolChip, 200,
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
            requirements(Category.turret, with(Items.surgeAlloy, 500, WHItems.protocolChip, 200, WHItems.manganeseSteel, 600,
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
                requirements(Category.turret, with(WHItems.manganeseSteel, 800, Items.surgeAlloy, 800, WHItems.vibranium, 150,
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
                requirements(Category.turret, with(WHItems.manganeseSteel, 800, Items.surgeAlloy, 800, WHItems.protocolChip, 300, WHItems.ceramite, 800,
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
            requirements(Category.turret, with(WHItems.manganeseSteel, 500, Items.carbide, 200, WHItems.ceramite, 200, WHItems.refineCeramite, 100, WHItems.sealedPromethium, 50));
            buildVisibility = BuildVisibility.sandboxOnly;
            size = 4;

            consumePower(100f);
        }};

        AirRaider airRaider = new AirRaider("air-raider"){{
            requirements(Category.turret, with(WHItems.manganeseSteel, 500, Items.carbide, 200, WHItems.ceramite, 200, WHItems.refineCeramite, 100, WHItems.sealedPromethium, 50));
            buildVisibility = BuildVisibility.sandboxOnly;
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
            requirements(Category.effect, with(WHItems.manganese, 100, Items.titanium, 75, Items.silicon, 125));
            buildVisibility = BuildVisibility.sandboxOnly;
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
            requirements(Category.turret, with(WHItems.manganese, 60, WHItems.manganese, 70, Items.silicon, 60, Items.titanium, 30));
            buildVisibility = BuildVisibility.sandboxOnly;
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

        sb7 = new OverheatGenericCrafter("过热工厂"){{
            requirements(Category.crafting, with(WHItems.manganese, 200, WHItems.manganese, 120, Items.silicon, 90));
            buildVisibility = BuildVisibility.sandboxOnly;
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
                requirements(Category.crafting, with(WHItems.manganese, 200, WHItems.manganese, 120, Items.silicon, 90));
                buildVisibility = BuildVisibility.sandboxOnly;
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

        //来自EU
        randomer = new Randomer("randomer1"){{
            requirements(Category.distribution, with(Items.silicon, 1));
            alwaysUnlocked = true;
            buildVisibility = BuildVisibility.sandboxOnly;
        }};


        sb2 = new ChainDrill("electric-drill2"){{
            requirements(Category.production, BuildVisibility.sandboxOnly, with(WHItems.armorAlloy, 114514));
            tier = 3;
            drillTime = 200;
            size = 2;
            consumePower(0.5f);
            liquidCapacity = 50f;

            consumeLiquid(WHLiquids.swageWater, 10 / 60f).boost();
        }};

    }
}
