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
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

public class PsychicUnitFactory extends UnitFactory {
    public float psychicCapacity = 120f;
    public float passivePsychicLoss = 0f;
    public Color overloadColor = Color.valueOf("ffb16a");
    public float overloadDecay = 0.03f;
    public float overloadBlockScale = 0.35f;
    public float overloadHealthLoss = 1.2f;
    public float overloadDangerThreshold = 0.35f;
    public float overloadDangerExponent = 2.2f;

    public PsychicUnitFactory(String name) {
        super(name);
        sync = true;
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
                () -> WHPal.PsyColor,
                build::psychicFraction
        ));

        addBar("psychic-use", (PsychicUnitFactoryBuild build) -> new PsychicBar(
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
            return psychicSpace() > PsychicNetworkNode.epsilon ? 0f : psychicStored();
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
        public float drag() {
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
            return 7;
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
            if (revision < 4) {
                psychic.clear();
            } else {
                psychic.read(read);
            }

            overload = revision >= 5 ? Math.max(read.f(), 0f) : 0f;

            if (revision == 6) {
                read.f(); // 跳过旧版遗留字段
            }
            overloadExposure = revision >= 6 ? Math.max(read.f(), 0f) : 0f;
        }
    }
}


