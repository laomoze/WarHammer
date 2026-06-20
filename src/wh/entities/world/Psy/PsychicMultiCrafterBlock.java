package wh.entities.world.Psy;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.entities.world.blocks.production.MultiCrafter;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

public class PsychicMultiCrafterBlock extends MultiCrafter {
    public float psychicCapacity = 180f;
    public float passivePsychicLoss = 0f;
    public Color psychicColor = Color.valueOf("9f74ff");
    public Color overloadColor = Color.valueOf("ffb16a");
    public float overloadDecay = 0.03f;
    public float overloadBlockScale = 0.35f;
    public float overloadHealthLoss = 1.2f;
    public float overloadDangerThreshold = 0.35f;
    public float overloadDangerExponent = 2.2f;
    public boolean autoGenerateItemRecipes = false;
    public float autoCraftTime = 90f;
    public float autoPowerUse = 1.2f;
    public float autoPsychicBase = 4f;
    public float autoPsychicCostScale = 14f;

    public PsychicMultiCrafterBlock(String name) {
        super(name);
        sync = true;
    }

    @Override
    public void init() {
        if (autoGenerateItemRecipes && craftPlans.isEmpty()) {
            populateAutoRecipes();
        }
        super.init();
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
                () -> psychicColor,
                build::psychicFraction
        ));

        addBar("psychic-use", (PsychicMultiCrafterBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-use", Strings.autoFixed(build.currentPsychicUse(), 2)),
                () -> psychicColor,
                () -> {
                    float peak = peakPsychicUse();
                    return peak <= 0.0001f ? 0f : build.currentPsychicUse() / peak;
                }
        ));

    }

    protected String bundleFormat(String key, Object... args) {
        return Core.bundle != null && Core.bundle.has(key) ? Core.bundle.format(key, args) : key;
    }

    protected void populateAutoRecipes() {
        Seq<Item> items = Vars.content.items().select(item -> item != null && !item.hidden);
        items.sort(item -> item.id);

        for (Item item : items) {
            int amount = autoOutputAmount(item);
            float craftTime = autoCraftTimeFor(item, amount);
            float psychicCost = autoPsychicCostFor(item, amount);

            PsychicCraftPlan plan = new PsychicCraftPlan();
            plan.craftTime = craftTime;
            plan.outputItems = new ItemStack[]{new ItemStack(item, amount)};
            plan.craftEffect = mindustry.content.Fx.none;
            if (autoPowerUse > 0f) {
                plan.consumePower(autoPowerUse);
            }
            plan.psychicCost = psychicCost;
            craftPlans.add(plan);
        }
    }

    protected int autoOutputAmount(Item item) {
        float score = item.cost * 1.35f + item.hardness * 0.75f +
                item.explosiveness + item.flammability + item.radioactivity + item.charge;
        if (score <= 0.8f) return 8;
        if (score <= 1.5f) return 5;
        if (score <= 2.8f) return 3;
        if (score <= 4.2f) return 2;
        return 1;
    }

    protected float autoCraftTimeFor(Item item, int amount) {
        float score = item.cost * 1.2f + item.hardness * 0.5f + item.radioactivity * 0.4f + item.charge * 0.3f;
        return autoCraftTime + score * 18f + Math.max(0, 6 - amount) * 6f;
    }

    protected float autoPsychicCostFor(Item item, int amount) {
        float score = item.cost * 1.4f + item.hardness * 0.8f +
                item.explosiveness * 0.6f + item.flammability * 0.35f +
                item.radioactivity * 0.9f + item.charge * 0.55f;
        return autoPsychicBase + score * autoPsychicCostScale + Math.max(1, amount) * 0.8f;
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
                read.f(); // 跳过旧版遗留字段
            }
            overloadExposure = revision >= 4 ? Math.max(read.f(), 0f) : 0f;
        }
    }
}
