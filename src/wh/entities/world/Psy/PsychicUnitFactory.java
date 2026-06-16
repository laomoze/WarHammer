package wh.entities.world.Psy;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

public class PsychicUnitFactory extends UnitFactory {
    public float psychicCapacity = 120f;
    public float passivePsychicLoss = 0f;
    public Color psychicColor = Color.valueOf("9f74ff");
    public Color overloadColor = Color.valueOf("ffb16a");
    public Color disorderColor = Color.valueOf("d065ff");
    public float overloadDecay = 0.03f;
    public float disorderDecay = 0.02f;
    public float overloadBlockScale = 0.35f;
    public float disorderBiasScale = 0.2f;
    public float disorderPsychicLoss = 0.018f;

    public PsychicUnitFactory(String name) {
        super(name);
        sync = true;
        buildType = PsychicUnitFactoryBuild::new;
    }

    @Override
    public void setStats() {
        super.setStats();
        if (!usesPsychic()) return;

        PsychicStatValues.add(stats, WHStats.psychicCapacity, psychicCapacity, StatUnit.none);

        float peakConsumption = 0f;
        for (UnitPlan plan : plans) {
            peakConsumption = Math.max(peakConsumption, psychicUsePerSecond(plan));
        }
        if (peakConsumption > 0f) {
            PsychicStatValues.add(stats, WHStats.psychicConsumption, peakConsumption, StatUnit.perSecond);
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        if (!usesPsychic()) return;

        addBar("psychic", (PsychicUnitFactoryBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-storage",
                        Strings.autoFixed(build.psychicStored(), 2),
                        Strings.autoFixed(psychicCapacity, 0)),
                () -> psychicColor,
                build::psychicFraction
        ));

        addBar("psychic-use", (PsychicUnitFactoryBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-use", Strings.autoFixed(build.currentPsychicUse(), 2)),
                () -> psychicColor,
                () -> {
                    float peak = peakPsychicUse();
                    return peak <= 0.0001f ? 0f : build.currentPsychicUse() / peak;
                }
        ));

        addBar("psychic-overload", (PsychicUnitFactoryBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-overload", Strings.autoFixed(Mathf.clamp(build.overload) * 100f, 0)),
                () -> overloadColor,
                () -> Mathf.clamp(build.overload)
        ));

        addBar("psychic-disorder", (PsychicUnitFactoryBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-disorder", Strings.autoFixed(Mathf.clamp(build.disorder) * 100f, 0)),
                () -> disorderColor,
                () -> Mathf.clamp(build.disorder)
        ));
    }

    protected String bundleFormat(String key, Object... args) {
        return Core.bundle != null && Core.bundle.has(key) ? Core.bundle.format(key, args) : key;
    }

    protected boolean usesPsychic() {
        return psychicCapacity > 0.0001f || peakPsychicUse() > 0.0001f;
    }

    protected float planPsychicCost(UnitPlan plan) {
        return plan instanceof PsychicUnitPlan psychicPlan ? Math.max(psychicPlan.psychicCost, 0f) : 0f;
    }

    protected float psychicUsePerSecond(UnitPlan plan) {
        float cost = planPsychicCost(plan);
        return plan == null || plan.time <= 0.0001f ? 0f : cost * 60f / plan.time;
    }

    protected float peakPsychicUse() {
        float peak = 0f;
        for (UnitPlan plan : plans) {
            peak = Math.max(peak, psychicUsePerSecond(plan));
        }
        return peak;
    }

    public static class PsychicUnitPlan extends UnitPlan {
        public float psychicCost;

        public PsychicUnitPlan(UnitType unit, float time, float psychicCost, ItemStack[] requirements) {
            super(unit, time, requirements);
            this.psychicCost = psychicCost;
        }
    }

    public class PsychicUnitFactoryBuild extends UnitFactoryBuild implements PsychicNetworkNode {
        public final PsychicModule psychic = new PsychicModule();
        public float overload;
        public float disorder;

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
            return psychicStored() + disorder * disorderBiasScale;
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
            if (disorder > PsychicNetworkNode.epsilon) {
                psychic.remove(disorder * disorderPsychicLoss / 60f * delta());
            }
            overload = Mathf.approachDelta(overload, 0f, overloadDecay);
            disorder = Mathf.approachDelta(disorder, 0f, disorderDecay);
            psychic.clamp(psychicCapacity);
        }

        protected UnitPlan currentPlan() {
            return currentPlan < 0 || currentPlan >= plans.size ? null : plans.get(this.currentPlan);
        }

        public float currentPsychicCost() {
            return planPsychicCost(currentPlan());
        }

        public float currentPsychicUse() {
            return psychicUsePerSecond(currentPlan());
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
            return Mathf.clamp(1f - overload * 0.2f - disorder * 0.12f, 0.2f, 1f);
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
        public void onEnergyDisorder(float amount) {
            disorder = Math.max(disorder + amount, 0f);
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
        public void consume() {
            float cost = currentPsychicCost();
            if (cost > 0f && !hasPsychic(cost)) return;

            super.consume();
            if (cost > 0f) {
                drainPsychic(cost);
            }
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
            write.f(disorder);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 4) {
                psychic.read(read);
            } else {
                psychic.clear();
            }

            if (revision >= 5) {
                overload = Math.max(read.f(), 0f);
                disorder = Math.max(read.f(), 0f);
            } else {
                overload = 0f;
                disorder = 0f;
            }
        }
    }
}
