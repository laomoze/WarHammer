package wh.entities.world.Psy;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.ui.Styles;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;
import wh.content.WHStats;
import wh.entities.world.blocks.production.MultiCrafter;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicImage;
import wh.ui.PsychicStatValues;

public class PsychicMultiCrafterBlock extends MultiCrafter {
    public float psychicCapacity = 180f;
    public float passivePsychicLoss = 0f;
    public float overloadDecay = 0.03f;
    public float overloadBlockScale = 0.35f;
    public float overloadHealthLoss = 1.2f;
    public float overloadDangerThreshold = 0.35f;
    public float overloadDangerExponent = 2.2f;

    public PsychicMultiCrafterBlock(String name) {
        super(name);
        sync = true;
    }

    @Override
    public void setStats() {
        super.setStats();
        PsychicStatValues.add(stats, WHStats.psychicCapacity, psychicCapacity, StatUnit.none);

        float peakConsumption = 0f;
        for (CraftPlan plan : craftPlans) {
            peakConsumption = Math.max(peakConsumption, psychicUsePerSecond(plan));
        }
        if (peakConsumption > 0f) {
            PsychicStatValues.add(stats, WHStats.psychicConsumption, peakConsumption, StatUnit.perSecond);
        }
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic", (PsychicMultiCrafterBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-storage",
                        Strings.autoFixed(build.psychicStored(), 2),
                        Strings.autoFixed(psychicCapacity, 0)),
                () -> WHPal.PsyColor,
                build::psychicFraction
        ));

        addBar("psychic-use", (PsychicMultiCrafterBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-use", Strings.autoFixed(build.currentPsychicUse(), 2)),
                () -> WHPal.PsyColor,
                () -> {
                    float peak = peakPsychicUse();
                    return peak <= 0.0001f ? 0f : build.currentPsychicUse() / peak;
                }
        ));
    }

    protected String bundleFormat(String key, Object... args) {
        return Core.bundle != null && Core.bundle.has(key) ? Core.bundle.format(key, args) : key;
    }

    protected float psychicCost(CraftPlan plan) {
        return plan instanceof PsychicCraftPlan psychicPlan ? Math.max(psychicPlan.psychicCost, 0f) : 0f;
    }

    protected float psychicUsePerSecond(CraftPlan plan) {
        float cost = psychicCost(plan);
        return plan == null || plan.craftTime <= 0.0001f ? 0f : cost * 60f / plan.craftTime;
    }

    protected float peakPsychicUse() {
        float peak = 0f;
        for (CraftPlan plan : craftPlans) {
            peak = Math.max(peak, psychicUsePerSecond(plan));
        }
        return peak;
    }

    protected void buildPsychicRecipe(Table table, CraftPlan plan) {
        float cost = psychicCost(plan);

        table.left().defaults().left();
        table.table(flow -> {
            flow.left().defaults().left().padRight(6f);
            flow.add(new PsychicImage(cost)).size(40f).padRight(4f);
            flow.add(bundleFormat("ui.wh-psychic-cost-per-craft", Strings.autoFixed(cost, 2)));
            flow.add("[lightgray] ->[]").padLeft(2f).padRight(2f);

            if (plan.outputItems.length > 0) {
                StatValues.items(plan.craftTime, plan.outputItems).display(flow);
            }

            if (plan.outputLiquids.length > 0) {
                if (plan.outputItems.length > 0) flow.add("  ");
                StatValues.liquids(1f, plan.outputLiquids).display(flow);
            }
        }).left().growX();
        table.row();

        if (plan.craftTime > 0.0001f) {
            table.add(bundleFormat(
                    "ui.wh-psychic-recipe-detail",
                    Strings.autoFixed(plan.craftTime / 60f, 2),
                    Strings.autoFixed(psychicUsePerSecond(plan), 2)
            )).left();
            table.row();
        }
    }

    public static class PsychicCraftPlan extends CraftPlan {
        public float psychicCost;
    }

    public class PsychicMultiCrafterBuild extends MultiCrafterBuild implements PsychicNetworkNode {
        public final PsychicModule psychic = new PsychicModule();
        public float overload;
        public float overloadExposure;

        public float psychicStored() {
            return psychic.amount();
        }

        public float psychicFraction() {
            return psychic.fraction(psychicCapacity);
        }

        @Override
        public float getEnergyPotential() {
            return psychicStored();
        }

        @Override
        public float inputPotential() {
            return psychicStored();
        }

        public float psychicSpace() {
            return Math.max(psychicCapacity - psychicStored(), 0f);
        }

        public float addPsychic(float amount) {
            return psychic.add(amount, psychicCapacity);
        }

        public float drainPsychic(float amount) {
            return psychic.remove(amount);
        }

        public boolean hasPsychic(float amount) {
            return psychic.has(amount);
        }

        protected void updatePsychic() {
            if (passivePsychicLoss > 0f) {
                psychic.remove(passivePsychicLoss / 60f * delta());
            }
            if (overload > PsychicNetworkNode.epsilon) {
                overloadExposure += delta() / 60f * Mathf.clamp(overload);
            } else {
                overloadExposure = Mathf.approachDelta(overloadExposure, 0f, 0.03f);
            }
            if (overload > overloadDangerThreshold && overloadHealthLoss > 0f) {
                float severity = Mathf.pow(Math.max(overload - overloadDangerThreshold, 0f), overloadDangerExponent);
                float exposureScale = 1f + overloadExposure;
                damage(severity * exposureScale * overloadHealthLoss / 60f * delta());
            }
            overload = Mathf.approachDelta(overload, 0f, overloadDecay);
            psychic.clamp(psychicCapacity);
        }

        public float currentPsychicCost() {
            return PsychicMultiCrafterBlock.this.psychicCost(craftPlan);
        }

        public float currentPsychicUse() {
            return PsychicMultiCrafterBlock.this.psychicUsePerSecond(craftPlan);
        }

        @Override
        public boolean acceptEnergy(PsychicNetworkNode source) {
            return enabled && psychicSpace() > 0.0001f;
        }

        @Override
        public boolean outputEnergy() {
            return false;
        }

        @Override
        public float getEnergyNeed() {
            return acceptEnergy(null) ? psychicSpace() : 0f;
        }

        @Override
        public float getEnergy() {
            return 0f;
        }

        @Override
        public float resident() {
            return overload * overloadBlockScale;
        }

        @Override
        public float energyTransferScale() {
            return Mathf.clamp(1f - overload * 0.2f, 0.2f, 1f);
        }

        @Override
        public float handleEnergy(float amount) {
            return acceptEnergy(null) ? addPsychic(amount) : 0f;
        }

        @Override
        public float removeEnergy(float amount) {
            return 0f;
        }

        @Override
        public void energyMoved(PsychicNetworkNode other, float amount, boolean incoming) {
            if (incoming && amount > PsychicNetworkNode.epsilon) {
                overload += amount / Math.max(psychicCapacity, 1f) * 0.08f;
            }
        }

        @Override
        public void onEnergyOverload(float amount) {
            overload = Math.max(overload + amount, 0f);
        }

        @Override
        public void updateTile() {
            updatePsychic();
            super.updateTile();
        }

        @Override
        public boolean shouldConsume() {
            return super.shouldConsume() && hasPsychic(currentPsychicCost());
        }

        @Override
        public void craft(CraftPlan craftPlan) {
            float cost = PsychicMultiCrafterBlock.this.psychicCost(craftPlan);
            if (cost > 0f && !hasPsychic(cost)) return;

            if (cost > 0f) {
                drainPsychic(cost);
            }
            super.craft(craftPlan);
        }

        @Override
        public void buildConfiguration(Table table) {
            Table rtc = new Table();
            rtc.left().defaults().size(55);

            Table cont = new Table().top();
            cont.left().defaults().left().growX();

            Runnable rebuild = () -> {
                rtc.clearChildren();
                if (hasDoubleOutput) {
                    for (int i = 0; i < rotationIcon.length; i++) {
                        var button = new ImageButton();
                        int j = i;
                        button.table(img -> img.image(rotationIcon[j]).color(Color.white).size(40).pad(10f));
                        button.changed(() -> configure(new int[]{j, craftPlanIndex()}));
                        button.update(() -> button.setChecked(rotation == j));
                        button.setStyle(Styles.clearNoneTogglei);
                        rtc.add(button).tooltip(String.valueOf(i * 90));
                    }
                }

                cont.clearChildren();
                for (CraftPlan plan : craftPlans) {
                    var button = new ImageButton();
                    button.table(info -> {
                        info.left().defaults().left().growX();
                        buildPsychicRecipe(info, plan);

                        if (plan.hasConsumers) {
                            info.table(from -> {
                                from.left().defaults().left();
                                for (var cons : plan.consumers) {
                                    if (cons != plan.consPower) {
                                        cons.build(this, from);
                                    }
                                }
                            }).left().padTop(4f);
                            info.row();
                        }

                        if (plan.powerProduction > 0f || plan.heatOutput > 0f) {
                            info.table(extra -> {
                                extra.left().defaults().left().padRight(8f);
                                if (plan.powerProduction > 0f) {
                                    StatValues.number(plan.powerProduction * 60f, StatUnit.powerSecond).display(extra);
                                }
                                if (plan.heatOutput > 0f) {
                                    StatValues.number(plan.heatOutput, StatUnit.heatUnits).display(extra);
                                }
                            }).left().padTop(2f);
                        }
                    }).grow().left().pad(5f);

                    button.setStyle(Styles.clearNoneTogglei);
                    button.changed(() -> configure(new int[]{rotation, craftPlans.indexOf(plan)}));
                    button.update(() -> button.setChecked(this.craftPlan == plan));
                    cont.add(button).growX();
                    cont.row();
                }
            };

            rebuild.run();

            Table main = new Table().background(Styles.black6);
            main.add(rtc).left().row();

            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);

            if (block != null) {
                pane.setScrollYForce(block.selectScroll);
                pane.update(() -> block.selectScroll = pane.getScrollY());
            }

            pane.setOverscroll(false, false);
            main.add(pane).maxHeight(100 * maxList);
            table.top().add(main);
        }

        @Override
        public byte version() {
            return 5;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            psychic.write(write);
            write.f(overload);
            write.f(overloadExposure);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision < 2) {
                psychic.clear();
            } else {
                psychic.read(read);
            }

            overload = revision >= 3 ? Math.max(read.f(), 0f) : 0f;

            if (revision == 4) {
                read.f();
            }
            overloadExposure = revision >= 4 ? Math.max(read.f(), 0f) : 0f;
        }
    }
}


