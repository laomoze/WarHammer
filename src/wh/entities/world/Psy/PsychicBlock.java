package wh.entities.world.Psy;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.Block;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public abstract class PsychicBlock extends Block {
    public float psychicCapacity = 60f;
    public float passivePsychicLoss = 0f;
    public float overloadDecay = 0.03f;
    public float disorderDecay = 0.02f;
    public float overloadBlockScale = 0.35f;
    public float disorderBiasScale = 0.2f;
    public float disorderPsychicLoss = 0.018f;
    public Color psychicColor = Color.valueOf("9f74ff");
    public Color overloadColor = Color.valueOf("ffb16a");
    public Color disorderColor = Color.valueOf("d065ff");
    public boolean acceptsPsychicLinks = true;
    public boolean outputsPsychicLinks = true;

    public PsychicBlock(String name) {
        super(name);
        update = true;
        solid = true;
        destructible = true;
        sync = true;
    }

    @Override
    public void setStats() {
        super.setStats();

        if (psychicCapacity > 0f) {
            PsychicStatValues.add(stats, WHStats.psychicCapacity, psychicCapacity, StatUnit.none);
        }

        if (passivePsychicLoss > 0f) {
            PsychicStatValues.add(stats, WHStats.psychicLoss, passivePsychicLoss, StatUnit.perSecond);
        }
    }

    @Override
    public void setBars() {
        super.setBars();

        if (psychicCapacity <= 0f) return;

        addBar("psychic", (PsychicBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-storage",
                        Strings.autoFixed(build.psychicStored(), 2),
                        Strings.autoFixed(psychicCapacity, 0)),
                () -> psychicColor,
                build::psychicFraction
        ));

        addBar("psychic-overload", (PsychicBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-overload", Strings.autoFixed(Mathf.clamp(build.overload) * 100f, 0)),
                () -> overloadColor,
                () -> Mathf.clamp(build.overload)
        ));

        addBar("psychic-disorder", (PsychicBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-disorder", Strings.autoFixed(Mathf.clamp(build.disorder) * 100f, 0)),
                () -> disorderColor,
                () -> Mathf.clamp(build.disorder)
        ));
    }

    protected String bundleFormat(String key, Object... args) {
        return Core.bundle != null && Core.bundle.has(key) ? Core.bundle.format(key, args) : key;
    }

    public class PsychicBuild extends Building implements PsychicNetworkNode {
        public final PsychicModule psychic = new PsychicModule();
        public float overload;
        public float disorder;
        public float networkStability;
        public float pressureBoost;

        public float psychicStored() {
            return psychic.amount();
        }

        public float psychicCapacity() {
            return PsychicBlock.this.psychicCapacity;
        }

        public float psychicFraction() {
            return psychic.fraction(psychicCapacity());
        }

        @Override
        public float getEnergyPotential() {
            return psychicStored();
        }

        @Override
        public float inputPotential() {
            return psychicStored() + disorder * PsychicBlock.this.disorderBiasScale;
        }

        @Override
        public float outputPotential() {
            return Math.max(psychicStored() - overload * PsychicBlock.this.overloadBlockScale, 0f) +
                    pressureBoost * psychicCapacity() * 0.15f;
        }

        public float psychicSpace() {
            return Math.max(psychicCapacity() - psychicStored(), 0f);
        }

        public float addPsychic(float amount) {
            return psychic.add(amount, psychicCapacity());
        }

        public float drainPsychic(float amount) {
            return psychic.remove(amount);
        }

        public boolean hasPsychic(float amount) {
            return psychic.has(amount);
        }

        public boolean consumePsychic(float amount) {
            if (!hasPsychic(amount)) return false;
            drainPsychic(amount);
            return true;
        }

        public void addPsychicOverload(float amount) {
            overload = Math.max(overload + amount, 0f);
        }

        public void addPsychicDisorder(float amount) {
            disorder = Math.max(disorder + amount, 0f);
        }

        public void addPsychicStability(float amount) {
            networkStability = Mathf.clamp(networkStability + amount);
        }

        public void addPsychicPressure(float amount) {
            pressureBoost = Mathf.clamp(pressureBoost + amount);
        }

        @Override
        public boolean acceptEnergy(PsychicNetworkNode source) {
            return PsychicBlock.this.acceptsPsychicLinks && enabled;
        }

        @Override
        public boolean outputEnergy() {
            return PsychicBlock.this.outputsPsychicLinks && enabled && psychicStored() > PsychicNetworkNode.epsilon;
        }

        @Override
        public float getEnergyNeed() {
            return acceptEnergy(null) ? psychicSpace() : 0f;
        }

        @Override
        public float getEnergy() {
            return outputEnergy() ? psychicStored() : 0f;
        }

        @Override
        public float resident() {
            return Math.max(overload * PsychicBlock.this.overloadBlockScale - networkStability * 0.25f, 0f);
        }

        @Override
        public float energyTransferScale() {
            return Mathf.clamp(1f - overload * 0.2f - disorder * 0.12f + networkStability * 0.2f + pressureBoost * 0.8f, 0.2f, 2f);
        }

        @Override
        public float handleEnergy(float amount) {
            return acceptEnergy(null) ? addPsychic(amount) : 0f;
        }

        @Override
        public float removeEnergy(float amount) {
            return outputEnergy() ? drainPsychic(amount) : 0f;
        }

        @Override
        public void energyMoved(PsychicNetworkNode other, float amount, boolean incoming) {
            if (amount <= PsychicNetworkNode.epsilon || psychicCapacity() <= PsychicNetworkNode.epsilon) return;
            addPsychicOverload(amount / psychicCapacity() * (incoming ? 0.08f : 0.05f));
        }

        @Override
        public void onEnergyOverload(float amount) {
            addPsychicOverload(amount);
        }

        @Override
        public void onEnergyDisorder(float amount) {
            addPsychicDisorder(amount);
        }

        protected void eachNearbyPsychicBuild(float rangeBlocks, Cons<PsychicBuild> cons) {
            float range = rangeBlocks * tilesize;
            float range2 = range * range;

            Groups.build.each(other -> {
                if (other == this || other.team != team || !other.isAdded()) return;
                if (!(other instanceof PsychicBlock.PsychicBuild build)) return;
                if (Mathf.dst2(x, y, other.x, other.y) > range2) return;
                cons.get(build);
            });
        }

        protected void updatePsychicState() {
            if (PsychicBlock.this.passivePsychicLoss > 0f) {
                psychic.remove(PsychicBlock.this.passivePsychicLoss / 60f * delta());
            }

            // 紊乱会让灵能慢慢漏掉，避免它只停留在 bar 上没有实际影响。
            if (disorder > PsychicNetworkNode.epsilon) {
                psychic.remove(disorder * PsychicBlock.this.disorderPsychicLoss / 60f * delta());
            }

            overload = Mathf.approachDelta(overload, 0f, PsychicBlock.this.overloadDecay);
            disorder = Mathf.approachDelta(disorder, 0f, PsychicBlock.this.disorderDecay);
            networkStability = Mathf.approachDelta(networkStability, 0f, 0.02f);
            pressureBoost = Mathf.approachDelta(pressureBoost, 0f, 0.035f);
            psychic.clamp(psychicCapacity());
        }

        @Override
        public void updateTile() {
            super.updateTile();
            updatePsychicState();
        }

        @Override
        public byte version() {
            return 2;
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

            if (revision >= 1) {
                psychic.read(read);
            } else {
                psychic.clear();
            }

            if (revision >= 2) {
                overload = Math.max(read.f(), 0f);
                disorder = Math.max(read.f(), 0f);
            } else {
                overload = 0f;
                disorder = 0f;
            }
        }
    }
}
