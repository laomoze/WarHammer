package wh.entities.world.Psy;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import mindustry.world.consumers.Consume;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import wh.content.WHStats;
import wh.entities.world.blocks.production.MultiCrafter;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicImage;
import wh.ui.PsychicStatValues;
import wh.ui.UIUtils;

public class PsychicMultiCrafterBlock extends MultiCrafter {
    protected static final float statsPsychicWidth = 124f;
    protected static final float configPsychicWidth = 116f;
    protected static final float recipeTextScale = 1.18f;

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
        stats.remove(Stat.output);
        stats.add(Stat.output, table -> {
            table.row();

            for (CraftPlan plan : craftPlans) {
                float cost = psychicCost(plan);

                table.table(Styles.grayPanel, info -> {
                    info.left().defaults().left();

                    info.table(line -> {
                        line.defaults().padBottom(2f);
                        buildPsychicOutputLine(line, plan, cost, 24f, statsPsychicWidth, 8f);
                    }).left().pad(9f);
                    info.row();

                    info.table(detail -> buildPsychicDetailLine(detail, plan))
                            .left().padLeft(8f).padBottom(6f);
                    info.row();

                    Stats stat = new Stats();
                    stat.timePeriod = plan.craftTime;
                    if (plan.hasConsumers) {
                        for (Consume c : plan.consumers) {
                            c.display(stat);
                        }
                    }
                    if (plan.heatRequirement > 0f) {
                        stat.add(Stat.input, plan.heatRequirement, StatUnit.heatUnits);
                        stat.add(Stat.maxEfficiency, (int) (plan.maxHeatEfficiency * 100f), StatUnit.percent);
                    }
                    if (plan.heatOutput > 0f) {
                        stat.add(Stat.output, plan.heatOutput, StatUnit.heatUnits);
                    }
                    if (!stat.toMap().isEmpty()) {
                        info.table(t -> UIUtils.statTurnTable(stat, t)).left().pad(6f);
                    }
                }).growX().left().pad(10f);
                table.row();
            }
        });

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

    protected void buildPsychicCostLine(Table table, float cost, float iconSize) {
        table.left().defaults().left();
        table.add(new Image(PsychicImage.region())).size(iconSize).scaling(Scaling.fit).padRight(2f);
        addOutline(table, "[accent]" + Strings.autoFixed(cost, 2) + "[]/[lightgray]次[]").left();
    }

    protected void buildPsychicOutputLine(Table table, CraftPlan plan, float cost, float iconSize, float psychicWidth, float gap) {
        table.left().defaults().left();
        table.table(costs -> buildPsychicCostLine(costs, cost, iconSize)).width(psychicWidth).left();
        table.add("->").color(Color.lightGray).padLeft(gap).padRight(gap);
        table.table(outputs -> {
            outputs.left().defaults().left();
            boolean hasOutput = false;

            for (ItemStack stack : plan.outputItems) {
                if (hasOutput) outputs.row();
                buildItemOutput(outputs, stack, plan.craftTime);
                hasOutput = true;
            }

            for (LiquidStack stack : plan.outputLiquids) {
                if (hasOutput) outputs.row();
                buildLiquidOutput(outputs, stack);
                hasOutput = true;
            }
        }).left().growX();
    }

    protected void buildPsychicDetailLine(Table table, CraftPlan plan) {
        table.left().defaults().left().padRight(10f);
        addOutline(table, "[lightgray]" + Core.bundle.get("stat.productiontime") + "[] " + Strings.autoFixed(plan.craftTime / 60f, 2) + " " + StatUnit.seconds.localized()).left();
        addOutline(table, "[lightgray]" + WHStats.psychicConsumption.localized() + "[] " + Strings.autoFixed(psychicUsePerSecond(plan), 2) + "/秒").left();
    }

    protected void buildItemOutput(Table table, ItemStack stack, float craftTime) {
        float perSecond = craftTime <= 0.0001f ? 0f : stack.amount * 60f / craftTime;
        table.left().defaults().left();
        table.image(stack.item.fullIcon).size(22f).scaling(Scaling.fit).padRight(5f);
        addOutline(table, stack.item.localizedName).left().padRight(6f);
        addOutline(table, "[lightgray]" + stack.amount + "[]").left().padRight(4f);
        addOutline(table, "[lightgray]" + Strings.autoFixed(perSecond, 3) + "/秒[]").left();
    }

    protected void buildLiquidOutput(Table table, LiquidStack stack) {
        table.left().defaults().left();
        table.image(stack.liquid.fullIcon).size(22f).scaling(Scaling.fit).padRight(5f);
        addOutline(table, stack.liquid.localizedName).left().padRight(6f);
        addOutline(table, "[lightgray]" + Strings.autoFixed(stack.amount, 3) + "/秒[]").left();
    }

    protected arc.scene.ui.layout.Cell<?> addOutline(Table table, String text) {
        var label = table.add(text).style(Styles.outlineLabel).get();
        label.setFontScale(recipeTextScale);
        return table.getCell(label);
    }

    protected void buildPsychicRecipe(Table table, CraftPlan plan) {
        float cost = psychicCost(plan);

        table.left().defaults().left();
        table.table(line -> {
            line.defaults().padBottom(2f);
            buildPsychicOutputLine(line, plan, cost, 22f, configPsychicWidth, 6f);
        }).left().growX().padBottom(2f);
        table.row();

        table.table(detail -> buildPsychicDetailLine(detail, plan)).left().padTop(2f);
        table.row();
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
            Table rotateTable = new Table();
            rotateTable.left().defaults().size(40f).padRight(2f);

            Table cont = new Table().top();
            cont.left().defaults().growX().padBottom(2f);

            Runnable rebuild = () -> {
                rotateTable.clearChildren();
                if (hasDoubleOutput) {
                    for (int i = 0; i < rotationIcon.length; i++) {
                        var button = new ImageButton();
                        int j = i;
                        button.table(img -> img.image(rotationIcon[j]).color(Color.white).size(22f).pad(6f));
                        button.changed(() -> configure(new int[]{j, craftPlanIndex()}));
                        button.update(() -> button.setChecked(rotation == j));
                        button.setStyle(Styles.clearNoneTogglei);
                        rotateTable.add(button).tooltip(String.valueOf(i * 90));
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
                            }).left().padTop(2f);
                        }
                    }).grow().left().pad(4f);

                    button.setStyle(Styles.clearNoneTogglei);
                    button.changed(() -> configure(new int[]{rotation, craftPlans.indexOf(plan)}));
                    button.update(() -> button.setChecked(this.craftPlan == plan));
                    cont.add(button).growX();
                    cont.row();
                }
            };

            rebuild.run();

            Table main = new Table().background(Styles.black6);
            if (hasDoubleOutput) {
                main.table(Styles.black3, head -> {
                    head.left().defaults().left();
                    head.add(bundleFormat("ui.wh-output-direction-label")).padRight(10f);
                    head.add(rotateTable).left();
                }).growX().pad(4f).row();
            }

            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);

            if (block != null) {
                pane.setScrollYForce(block.selectScroll);
                pane.update(() -> block.selectScroll = pane.getScrollY());
            }

            pane.setOverscroll(false, false);
            main.add(pane).growX().maxHeight(62 * maxList).pad(4f);
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
