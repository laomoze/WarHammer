package wh.content;

import arc.struct.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.Objectives.*;
import mindustry.type.*;

import static mindustry.content.TechTree.*;

public final class KarvexTeachTree{
    public static TechNode context = null;

    private KarvexTeachTree(){
    }

    public static void load(){
        if(WHPlanets.karvex == null) return;

        applyKarvexPlanetToVanillaContent();
        applyKarvexUnlockTuning();

        WHPlanets.karvex.techTree = nodeRoot("[yellow]IMPERIUM", WHItems.imperium, () -> {
        });

        context = WHPlanets.karvex.techTree;
        buildItemBranch();
        buildLogisticsBranch();
        buildLiquidBranch();
        buildProductionBranch();
        buildHeatBranch();
        buildPayloadBranch();
        buildLogicBranch();
        buildPowerBranch();
        buildDefenseBranch();
        buildUnitBranch();
        context = null;

        extendSerpuloTechTree();
    }

    private static void applyKarvexUnlockTuning(){
        // Spike should not be pre-unlocked at campaign start.
        if(WHBlocks.Spike != null){
            WHBlocks.Spike.alwaysUnlocked = false;
        }
    }

    private static void applyKarvexPlanetToVanillaContent(){
        addPlanetTab(Items.coal);
        addPlanetTab(Items.graphite);
        addPlanetTab(Items.tungsten);
        addPlanetTab(Items.silicon);
        addPlanetTab(Items.metaglass);
        addPlanetTab(Items.carbide);
        addPlanetTab(Items.plastanium);
        addPlanetTab(Liquids.water);
        addPlanetTab(Liquids.oil);
        addPlanetTab(Liquids.slag);
        addPlanetTab(Liquids.nitrogen);
        addPlanetTab(Liquids.cryofluid);
    }

    private static void addPlanetTab(UnlockableContent content){
        if(content == null || WHPlanets.karvex == null) return;
        content.shownPlanets.add(WHPlanets.karvex);
        content.databaseTabs.add(WHPlanets.karvex);
    }

    private static void buildItemBranch(){
        // ores
        nodeProduce(WHItems.manganese, () -> {
            nodeProduce(WHItems.oreSand, () -> {
                nodeProduce(Items.coal, () -> {
                    nodeProduce(Items.graphite, () -> {
                        nodeProduce(Items.silicon, () -> {
                            nodeProduce(Items.metaglass, () -> {
                            });
                        });
                    });
                });
            });

            nodeProduce(WHItems.chromium, () -> {
                nodeProduce(WHItems.cobalt, () -> {
                    nodeProduce(WHItems.uranium, () -> {
                        nodeProduce(Items.tungsten, () -> {
                            nodeProduce(WHItems.molybdenum, () -> {
                                nodeProduce(WHItems.vibranium, () -> {
                                });
                            });
                        });
                    });
                });
            });
        });

        // synthetic line: keep manganeseSteel gate minimal.
        nodeProduce(WHItems.manganeseSteel, () -> {
            nodeProduce(Items.plastanium, () -> {
                nodeProduce(Items.carbide, () -> {
                    nodeProduce(WHItems.combustible, () -> {
                        nodeProduce(WHItems.culverCrystal, () -> {
                        });
                    });

                    nodeProduce(WHItems.cobaltNitride, () -> {
                        nodeProduce(WHItems.armorAlloy, () -> {
                            nodeProduce(WHItems.entanglement, () -> {
                                nodeProduce(WHItems.resonantCrystal, () -> {
                                    nodeProduce(WHItems.protocolChip, () -> {
                                        nodeProduce(WHItems.sealedPromethium, () -> {
                                        });
                                    });
                                });
                            });
                        });

                        nodeProduce(WHItems.ceramite, () -> {
                            nodeProduce(WHItems.molybdenumAlloy, () -> {
                                nodeProduce(WHItems.refineCeramite, () -> {
                                    nodeProduce(WHItems.adamantium, () -> {
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });

        // liquids
        nodeProduce(WHLiquids.swageWater, () -> {
            nodeProduce(Liquids.water, () -> {
                nodeProduce(Liquids.oil, () -> {
                    nodeProduce(WHLiquids.orePromethium, () -> {
                        nodeProduce(WHLiquids.refinePromethium, () -> {
                        });
                    });
                });

                nodeProduce(Liquids.nitrogen, () -> {
                    nodeProduce(WHLiquids.liquidNitrogen, () -> {
                    });
                    nodeProduce(Liquids.cryofluid, () -> {
                    });
                });

                nodeProduce(Liquids.slag, () -> {
                });
            });
        });
    }

    private static void extendSerpuloTechTree(){
        if(Planets.serpulo == null || Planets.serpulo.techTree == null) return;

        // Add mod assembler after Serpulo reconstructor chain.
        if(WHBlocks.serpuloT6Assembler != null && !hasChildNode(Blocks.tetrativeReconstructor, WHBlocks.serpuloT6Assembler)){
            vanillaNode(Blocks.tetrativeReconstructor, () -> node(WHBlocks.serpuloT6Assembler, () -> {
            }));
        }

        // Add mod S6 units behind each vanilla T5 line.
        addSerpuloT6UnitAfter(UnitTypes.eclipse, WHUnitTypes.airS6);
        addSerpuloT6UnitAfter(UnitTypes.oct, WHUnitTypes.airSGreen6);
        addSerpuloT6UnitAfter(UnitTypes.reign, WHUnitTypes.mechaS6);
        addSerpuloT6UnitAfter(UnitTypes.corvus, WHUnitTypes.mechaSGreen6);
        addSerpuloT6UnitAfter(UnitTypes.toxopid, WHUnitTypes.meshSPurple6);
        addSerpuloT6UnitAfter(UnitTypes.omura, WHUnitTypes.navyS6);
        addSerpuloT6UnitAfter(UnitTypes.navanax, WHUnitTypes.navySGreen6);
    }

    private static void addSerpuloT6UnitAfter(UnitType parent, UnitType child){
        if(parent == null || child == null || hasChildNode(parent, child)) return;

        vanillaNode(parent, () -> {
            if(WHBlocks.serpuloT6Assembler == null){
                node(child, () -> {
                });
            }else{
                node(child, Seq.with(new Research(WHBlocks.serpuloT6Assembler)), () -> {
                });
            }
        });
    }

    private static boolean hasChildNode(UnlockableContent parent, UnlockableContent child){
        TechNode parentNode = TechTree.all.find(t -> t.content == parent);
        return parentNode != null && parentNode.children.contains(n -> n.content == child);
    }

    private static void buildLogisticsBranch(){
        node(WHBlocks.basicDust, () -> {
            node(WHBlocks.armorJunction, () -> {
                node(WHBlocks.armorRouter, () -> {
                    node(WHBlocks.armorSorter, () -> node(WHBlocks.armorInvertedSorter, () -> {
                    }));
                    node(WHBlocks.armorOverflowGate, () -> node(WHBlocks.armorUnderflowGate, () -> {
                    }));
                });
            });

            node(WHBlocks.basicBridge, () -> {
                node(WHBlocks.lowResistanceBridge, () -> {
                });
                node(WHBlocks.steelDust, () -> {
                    node(WHBlocks.trackDriverPoint, () -> {
                        node(WHBlocks.trackDriver, () -> {
                        });
                    });
                    node(WHBlocks.ceramiteConveyor, () -> {
                        node(WHBlocks.armorCoverStackBelt, () -> {
                            node(WHBlocks.stackBridge, () -> {
                            });
                        });
                    });
                });

            });

            node(WHBlocks.steelUnloader, () -> {
                node(WHBlocks.armoredContainer, () -> {
                    node(WHBlocks.armoredVault, () -> {
                    });
                });
                node(WHBlocks.landingPad, () -> {
                });
                node(WHBlocks.launchPad, () -> {
                });
            });
        });
    }

    private static void buildLiquidBranch(){
        node(WHBlocks.lightConduit, () -> {
            node(WHBlocks.armorFluidRouter, () -> {
                node(WHBlocks.armorFluidJunction, () -> {
                    node(WHBlocks.steelBridgeConduit, () -> {
                        node(WHBlocks.lowResistanceConduit, () -> {
                        });
                    });
                    node(WHBlocks.basicPump, () -> {
                        node(WHBlocks.steelPump, () -> node(WHBlocks.gravityPump, () -> {
                        }));
                    });
                });
            });

            node(WHBlocks.steelConduit, () -> {
                node(WHBlocks.mixedFluidJunction, () -> {
                });
                node(WHBlocks.T2LiquidTank, () -> node(WHBlocks.armorLiquidTank, () -> {
                }));
            });
        });
    }

    private static void buildHeatBranch(){
        node(WHBlocks.combustionHeater, () -> {
            node(WHBlocks.slagHeatMaker, () -> {
                node(WHBlocks.decayHeater, () -> {
                    node(WHBlocks.promethiumHeater, () -> {
                    });
                });
            });

            node(WHBlocks.smallHeatRouter, () -> {
                node(WHBlocks.heatBelt, () -> {
                    node(WHBlocks.heatBridge, () -> {
                        node(WHBlocks.T2heatBridge, () -> {
                        });
                    });
                });
            });

            // requested: converters are not part of the Karvex tech tree.
            // tungstenConverter / molybdenumConverter / vibraniumConverter
        });
    }

    private static void buildPayloadBranch(){
        node(WHBlocks.armorPayloadConveyor, () -> {
            node(WHBlocks.armorPayloadRouter, () -> {
            });
            node(WHBlocks.t2PayloadMassDriver, () -> {
            });
        });
    }

    private static void buildLogicBranch(){
        node(WHBlocks.holographyMessage, () -> {
            node(WHBlocks.switchBlock, () -> {
                node(WHBlocks.logicDisplay, () -> {
                    node(WHBlocks.canvas, () -> {
                    });
                });

                node(WHBlocks.memoryCell, () -> {
                    node(WHBlocks.memoryBank, () -> {
                    });
                });

                node(WHBlocks.juniorProcessor, () -> {
                    node(WHBlocks.instructionProcessor, () -> {
                    });
                });
            });
        });
    }

    private static void buildProductionBranch(){
        node(WHBlocks.electronicPneumaticDrill, () -> {
            // mining line
            node(WHBlocks.MechanicalQuarry, () -> {
                node(WHBlocks.lavaDrill, () -> {
                    node(WHBlocks.heavyCuttingDrill, () -> {
                        node(WHBlocks.SpecialCuttingDrill, () -> {
                            node(WHBlocks.highEnergyDrill, () -> {
                            });
                        });
                    });
                });
            });

            node(WHBlocks.heavyExtractor, () -> {
                node(WHBlocks.promethiumExtractor, () -> {
                    node(WHBlocks.slagExtractor, () -> {
                    });
                });
            });

            node(WHBlocks.strengthenOilExtractor, () -> {
                node(WHBlocks.integratedCompressor, () -> {
                });
            });

            node(WHBlocks.multiPress, () -> {
                node(WHBlocks.siliconMixFurnace, () -> {
                    node(WHBlocks.manganeseSteelFurnace, () -> {
                        node(WHBlocks.arcKiln, () -> {
                            node(WHBlocks.plastaniumCompressor, () -> {
                                node(WHBlocks.carbideCrucible, () -> {
                                    node(WHBlocks.cobaltNitrideChamber, () -> {
                                        node(WHBlocks.entanglementSynthesizer, () -> {
                                            node(WHBlocks.crystalEngraver, () -> {
                                            });
                                        });
                                        node(WHBlocks.heatSiliconSmelter, () -> {
                                            node(WHBlocks.laserEngraver, () -> {
                                            });
                                        });
                                        node(WHBlocks.T2ManganeseSteelFurnace, () -> {
                                            node(WHBlocks.T2ceramiteSteelFoundry, () -> {
                                            });
                                        });
                                    });
                                });
                            });

                            node(WHBlocks.sandSeparator, () -> {
                                node(WHBlocks.T2sandSeparator, () -> {
                                    node(WHBlocks.slagfurnace, () -> {
                                    });
                                });
                            });

                            node(WHBlocks.waterPurifier, () -> {
                                node(WHBlocks.T2WaterPurifier, () -> {
                                    node(WHBlocks.cryofluidMixer, () -> {
                                        node(WHBlocks.LiquidNitrogenPlant, () -> {
                                        });
                                    });
                                });
                            });
                        });
                    });
                });

                node(WHBlocks.scrapFurance, () -> {
                    node(WHBlocks.heatIncinerator, () -> {
                    });
                });

                node(WHBlocks.electrolyzer, () -> {
                    node(WHBlocks.cultivator, () -> {
                        node(WHBlocks.sporePress, () -> {
                            node(WHBlocks.coalCentrifuge, () -> {
                                node(WHBlocks.petroleumConverter, () -> {
                                });
                            });
                        });
                    });
                    node(WHBlocks.atmosphericSeparator, () -> {
                    });
                });

                node(WHBlocks.armorCompressor, () -> {
                    node(WHBlocks.moSurgeSmelter, () -> {
                    });
                    node(WHBlocks.largeArmorSmelter, () -> {
                    });
                });

                node(WHBlocks.ceramiteSteelFoundry, () -> {
                    node(WHBlocks.ceramiteRefinery, () -> {
                        node(WHBlocks.ADMill, () -> {
                        });
                    });
                });

                node(WHBlocks.combustibleCrafter, () -> {
                    node(WHBlocks.combustibleSeparator, () -> {
                    });
                    node(WHBlocks.pressureReactionChamber, () -> {
                    });
                });

                node(WHBlocks.promethiumRefinery, () -> {
                    node(WHBlocks.sealedPromethiumMill, () -> {
                    });
                });


            });
        });
    }

    private static void buildPowerBranch(){
        node(WHBlocks.powerNode, () -> {
            node(WHBlocks.t2PowerNode, () -> {
                node(WHBlocks.compositeNode, () -> node(WHBlocks.armorPowerTower, () -> {
                }));
            });

            node(WHBlocks.smallBattery, () -> {
                node(WHBlocks.midBattery, () -> node(WHBlocks.largeBattery, () -> {
                }));
            });
        });

        node(WHBlocks.ventDistiller, () -> {
            node(WHBlocks.oxidationGenerator, () -> {
                node(WHBlocks.turboGenerator, () -> node(WHBlocks.crackingGenerator, () -> {
                }));
            });
        });

        node(WHBlocks.combustionGenerator, () -> {
            node(WHBlocks.T2thermalGenerator, () -> {
            });
            node(WHBlocks.smallPromethiumReactor, () -> {
                node(WHBlocks.decayGenerator, () -> {
                });
                node(WHBlocks.promethiunmRector, () -> {
                    node(WHBlocks.T2impactReactor, () -> {
                        node(WHBlocks.plaRector, () -> {
                        });
                    });
                });
            });
        });
    }

    private static void buildDefenseBranch(){
        node(WHBlocks.Spike, () -> {


            // wall and support branch (vanilla-style defense structure)
            node(WHBlocks.primarySteelWall, () -> {
                node(WHBlocks.largePrimarySteelWall, () -> {
                    node(WHBlocks.improvedSteelWall, () -> {
                    });
                    node(WHBlocks.largeImprovedSteelWall, () -> {
                        node(WHBlocks.heavySteelWall, () -> {
                            node(WHBlocks.largeHeavySteelWall, () -> node(WHBlocks.heavySteelDoor, () -> {
                            }));
                        });
                    });

                    node(WHBlocks.ceramiteWall, () -> {
                        node(WHBlocks.largeCeramiteWall, () -> node(WHBlocks.ceramiteDoor, () -> {
                        }));
                        node(WHBlocks.refineCeramiteWall, () -> {
                            node(WHBlocks.largeRefineCeramiteWall, () -> {
                                node(WHBlocks.promethiumChargeWall, () -> node(WHBlocks.denseExplosionProofWall, () -> {
                                }));
                            });
                        });
                    });
                });
            });

            node(WHBlocks.wrapProjector, () -> {
                // wrapOverdrive is enemy-only; keep it out of player tree.
                if(WHBlocks.selectProjector != null){
                    node(WHBlocks.selectProjector, () -> {
                    });
                }
                node(WHBlocks.shelterDome, () -> {
                });
                node(WHBlocks.repairTower, () -> {
                    node(WHBlocks.voidShield, () -> node(WHBlocks.ionShield, () -> {
                    }));
                });
            });

            node(WHBlocks.strongholdCore, () -> {
                node(WHBlocks.T2strongholdCore, () -> node(WHBlocks.T3strongholdCore, () -> {
                }));
            });

            node(WHBlocks.Crush, () -> {
                node(WHBlocks.AutoGun, () -> {
                    node(WHBlocks.SSWord, () -> {
                        node(WHBlocks.Blade, () -> {
                            node(WHBlocks.Prevent, () ->
                            node(WHBlocks.Crumble, () -> {
                                node(WHBlocks.Collapse, () -> {
                                });
                                node(WHBlocks.Reckoning, () -> {
                                });
                            }));
                            node(WHBlocks.Deflection, () -> {
                            });
                            node(WHBlocks.RoaringFlame, () -> {
                                node(WHBlocks.Melta, () -> {
                                });
                            });
                        });

                        node(WHBlocks.Vortex, () -> {
                        });
                        node(WHBlocks.HeavyHammer, () -> {
                            node(WHBlocks.Colossus, () -> {
                                node(WHBlocks.CycloneMissleLauncher, () -> node(WHBlocks.Erase, () -> {
                                }));
                                node(WHBlocks.Hydra, () -> {
                                });
                            });
                        });

                    });
                });
            });


            node(WHBlocks.Ray, () -> {
                node(WHBlocks.Blaze, () -> {
                    node(WHBlocks.Flash, () -> {
                    });
                });
                node(WHBlocks.Lcarus, () -> {

                    node(WHBlocks.Viper, () -> {
                        node(WHBlocks.Pyros, () -> {
                        });
                    });
                    node(WHBlocks.Ionize, () -> {
                        node(WHBlocks.Sacrament, () -> node(WHBlocks.Annihilate, () -> {
                        }));
                    });
                });

            });
        });
    }

    private static void buildUnitBranch(){
        node(WHBlocks.groundFactory, () -> {
            // ground line
            node(WHUnitTypes.M1, () -> {
                node(WHUnitTypes.M2, () -> {
                    node(WHUnitTypes.M3, () -> {
                        node(WHUnitTypes.M4A, () -> {
                            node(WHUnitTypes.M5, () -> {
                                node(WHUnitTypes.M6, () -> {
                                });
                            });
                        });
                        node(WHUnitTypes.M4B, () -> {
                        });
                        node(WHUnitTypes.M4C, () -> {
                        });
                        node(WHUnitTypes.M4D, () -> {
                        });
                    });
                });
            });

            node(WHBlocks.airFactory, () -> {
                // air line A
                node(WHUnitTypes.air1, () -> {
                    node(WHUnitTypes.air2, () -> {
                        node(WHUnitTypes.air3, () -> {
                            node(WHUnitTypes.air4, () -> {
                                node(WHUnitTypes.air5, () -> {
                                    node(WHUnitTypes.air6, () -> {
                                    });
                                });
                            });
                        });
                    });
                });

                // air line B
                node(WHUnitTypes.airB1, () -> {
                    node(WHUnitTypes.airB2, () -> {
                        node(WHUnitTypes.airB3, () -> {
                            node(WHUnitTypes.airB4, () -> {
                                node(WHUnitTypes.airB5, () -> {
                                    node(WHUnitTypes.airB6, () -> {
                                    });
                                });
                            });
                        });
                    });
                });
            });

            // mechaFactory must be sibling of t2Module
            node(WHBlocks.mechaFactory, () -> {
                node(WHUnitTypes.Mecha2, () -> {
                    node(WHUnitTypes.Mecha3, () -> {
                        node(WHUnitTypes.Mecha4, () -> {
                            node(WHUnitTypes.Mecha5, () -> {
                                node(WHUnitTypes.Mecha6, () -> {
                                });
                            });
                        });
                    });
                });
            });

            node(WHBlocks.t2Module, () -> {
                node(WHBlocks.t3Module, () -> {
                    node(WHBlocks.tankFactory, () -> {
                        node(WHUnitTypes.tank1, () -> {
                            node(WHUnitTypes.tank2, () -> {
                                node(WHUnitTypes.tank3, () -> {
                                });
                            });
                        });

                        node(WHUnitTypes.tank1s, () -> {
                            node(WHUnitTypes.tank2s, () -> {
                                node(WHUnitTypes.tank3s, () -> {
                                });
                            });
                        });
                    });
                    node(WHBlocks.t4Module, () -> {
                        node(WHBlocks.t5Module, () -> {
                            node(WHBlocks.jumpBeacon, () -> {
                                node(WHUnitTypes.tankEn1, () -> {
                                    node(WHUnitTypes.tankEn2, () -> {
                                    });
                                });
                            });
                            node(WHBlocks.t6Module, () -> {
                                node(WHBlocks.airborneDeploymentBeacon, () -> {
                                    node(WHUnitTypes.MEn1, () -> {
                                    });
                                });

                                node(WHBlocks.energyWarpGate, () -> {
                                    node(WHUnitTypes.air7, () -> {
                                    });
                                    node(WHUnitTypes.tankAG, () -> {
                                    });
                                    node(WHUnitTypes.Mecha7, () -> {
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    public static void vanillaNode(UnlockableContent content, Runnable children){
        context = TechTree.all.find(t -> t.content == content);
        children.run();
    }

    public static void removeNode(UnlockableContent content){
        context = TechTree.all.find(t -> t.content == content);
        if(context != null){
            context.remove();
        }
    }

    public static void node(UnlockableContent content, Runnable children){
        node(content, content.researchRequirements(), children);
    }

    public static void node(UnlockableContent content, ItemStack[] requirements, Runnable children){
        node(content, requirements, null, children);
    }

    public static void node(UnlockableContent content, ItemStack[] requirements, Seq<Objective> objectives, Runnable children){
        TechNode node = new TechNode(context, content, requirements);
        if(objectives != null) node.objectives.addAll(objectives);

        TechNode prev = context;
        context = node;
        children.run();
        context = prev;
    }

    public static void node(UnlockableContent content, Seq<Objective> objectives, Runnable children){
        node(content, content.researchRequirements(), objectives, children);
    }

    public static void nodeProduce(UnlockableContent content, Seq<Objective> objectives, Runnable children){
        node(content, content.researchRequirements(), objectives.add(new Produce(content)), children);
    }

    public static void nodeProduce(UnlockableContent content, Runnable children){
        nodeProduce(content, new Seq<>(), children);
    }
}
