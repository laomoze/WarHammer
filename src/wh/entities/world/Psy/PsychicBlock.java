package wh.entities.world.Psy;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public abstract class PsychicBlock extends Block {
    public DrawBlock drawer = new DrawDefault();
    public float psychicCapacity = 60f;
    public float passivePsychicLoss = 0f;
    public float overloadDecay = 0.03f;
    public float overloadBlockScale = 0.35f;
    public float pressurePotentialScale = 0.12f;
    public float stabilityResidentRelief = 0.5f;
    public float overloadTransferPenalty = 0.8f;
    public float stabilityTransferBonus = 0.4f;
    public float pressureTransferBonus = 0.5f;
    public float minTransferScale = 0.25f;
    public float maxTransferScale = 1.75f;
    public float overloadHealthLoss = 1.2f;
    public float overloadDangerThreshold = 0.35f;
    public float overloadDangerExponent = 2.2f;
    public Color psychicColor = Color.valueOf("9f74ff");
    public Color overloadColor = Color.valueOf("ffb16a");
    public boolean acceptsPsychicLinks = true;
    public boolean outputsPsychicLinks = true;

    public PsychicBlock(String name) {
        super(name);
        update = true;
        solid = true;
        destructible = true;
        sync = true;
        squareSprite = false;
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
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
    }

    protected String bundleFormat(String key, Object... args) {
        return Core.bundle != null && Core.bundle.has(key) ? Core.bundle.format(key, args) : key;
    }

    public class PsychicBuild extends Building implements PsychicNetworkNode {
        public final PsychicModule psychic = new PsychicModule();
        public float overload;
        public float networkStability;
        public float pressureBoost;
        public float overloadExposure;
        public float activityTotalProgress;

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
            return psychicStored();
        }

        @Override
        public float outputPotential() {
            float usable = psychicStored() * Math.max(1f - overload * PsychicBlock.this.overloadBlockScale, 0f);
            float pressure = pressureBoost * psychicCapacity() * PsychicBlock.this.pressurePotentialScale;
            return usable + pressure;
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
        public boolean acceptsPsychicLinks() {
            return PsychicBlock.this.acceptsPsychicLinks;
        }

        @Override
        public boolean outputEnergy() {
            return PsychicBlock.this.outputsPsychicLinks && enabled && psychicStored() > PsychicNetworkNode.epsilon;
        }

        @Override
        public boolean outputsPsychicLinks() {
            return PsychicBlock.this.outputsPsychicLinks;
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
            float overloadLoad = overload * PsychicBlock.this.overloadBlockScale;
            float relief = networkStability * PsychicBlock.this.stabilityResidentRelief;
            return Math.max(overloadLoad * (1f - relief), 0f);
        }

        @Override
        public float energyTransferScale() {
            float scale = 1f;
            scale -= overload * PsychicBlock.this.overloadTransferPenalty;
            scale += networkStability * PsychicBlock.this.stabilityTransferBonus;
            scale += pressureBoost * PsychicBlock.this.pressureTransferBonus;
            return Mathf.clamp(scale, PsychicBlock.this.minTransferScale, PsychicBlock.this.maxTransferScale);
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

            // 过载会持续累积暴露值，避免它只停留在状态条上而没有实际影响。
            if (overload > PsychicNetworkNode.epsilon) {
                overloadExposure += delta() / 60f * Mathf.clamp(overload);
            } else {
                overloadExposure = Mathf.approachDelta(overloadExposure, 0f, 0.03f);
            }
            if (shouldTakeOverloadDamage() && overloadHealthLoss > 0f) {
                float severity = overloadDamageSeverity();
                float exposureScale = 1f + overloadExposure;
                damage(severity * exposureScale * PsychicBlock.this.overloadHealthLoss / 60f * delta());
            }

            overload = Mathf.approachDelta(overload, 0f, PsychicBlock.this.overloadDecay);
            networkStability = Mathf.approachDelta(networkStability, 0f, 0.02f);
            pressureBoost = Mathf.approachDelta(pressureBoost, 0f, 0.035f);
            psychic.clamp(psychicCapacity());
        }

        protected boolean shouldTakeOverloadDamage() {
            return overload > overloadDangerThreshold;
        }

        protected float overloadDamageSeverity() {
            return Mathf.pow(Math.max(overload - overloadDangerThreshold, 0f), overloadDangerExponent);
        }

        @Override
        public void updateTile() {
            super.updateTile();
            updatePsychicState();
            activityTotalProgress += warmup() * edelta();
        }

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public float warmup() {
            if (!enabled) return 0f;

            float activity = (outputEnergy() || getEnergyNeed() > PsychicNetworkNode.epsilon) ? Mathf.clamp(efficiency) : 0f;
            float stored = psychicCapacity() > PsychicNetworkNode.epsilon ? psychicFraction() : 0f;
            float state = Math.max(Mathf.clamp(overload), Math.max(Mathf.clamp(networkStability), Mathf.clamp(pressureBoost)));
            return Mathf.clamp(Math.max(activity, Math.max(stored, state)));
        }

        @Override
        public float totalProgress() {
            return activityTotalProgress;
        }

        @Override
        public float progress() {
            return warmup();
        }

        @Override
        public byte version() {
            return 4;
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
            if (revision < 1) {
                psychic.clear();
            } else {
                psychic.read(read);
            }

            overload = revision >= 2 ? Math.max(read.f(), 0f) : 0f;

            if (revision == 3) {
                read.f();
            }
            overloadExposure = revision >= 3 ? Math.max(read.f(), 0f) : 0f;
        }
    }
}
