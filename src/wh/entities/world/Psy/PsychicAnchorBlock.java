package wh.entities.world.Psy;

import arc.math.Mathf;
import arc.util.Strings;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

public class PsychicAnchorBlock extends PsychicBlock {
    public float anchorRange = 10f;
    public float psychicUsePerSecond = 2.5f;
    public float stabilityPerSecond = 0.12f;
    public float disorderCleanPerSecond = 0.08f;
    public float overloadCleanPerSecond = 0.06f;

    public PsychicAnchorBlock(String name) {
        super(name);
        acceptsPsychicLinks = true;
        outputsPsychicLinks = false;
        buildType = PsychicAnchorBuild::new;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, anchorRange, StatUnit.blocks);
        PsychicStatValues.add(stats, WHStats.psychicConsumption, psychicUsePerSecond, StatUnit.perSecond);
        PsychicStatValues.add(stats, WHStats.psychicStability, stabilityPerSecond * 100f, StatUnit.percent);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-anchor", (PsychicAnchorBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-anchor", Strings.autoFixed(build.anchorRate * 100f, 0)),
                () -> psychicColor,
                () -> Mathf.clamp(build.anchorRate / Math.max(stabilityPerSecond * 4f, 0.0001f))
        ));
    }

    public class PsychicAnchorBuild extends PsychicBuild {
        public float anchorRate;

        @Override
        protected void updatePsychicState() {
            super.updatePsychicState();

            if (!enabled || psychicStored() <= PsychicNetworkNode.epsilon) {
                anchorRate = Mathf.lerpDelta(anchorRate, 0f, 0.1f);
                return;
            }

            float used = drainPsychic(psychicUsePerSecond / 60f * delta());
            float scale = used * 60f / Math.max(psychicUsePerSecond, PsychicNetworkNode.epsilon);
            if (scale <= PsychicNetworkNode.epsilon) {
                anchorRate = Mathf.lerpDelta(anchorRate, 0f, 0.1f);
                return;
            }

            float stabilityStep = stabilityPerSecond / 60f * delta() * scale;
            float disorderStep = disorderCleanPerSecond / 60f * delta() * scale;
            float overloadStep = overloadCleanPerSecond / 60f * delta() * scale;
            int[] affected = {1};

            applyAnchor(this, stabilityStep, disorderStep, overloadStep);
            eachNearbyPsychicBuild(anchorRange, other -> {
                applyAnchor(other, stabilityStep, disorderStep, overloadStep);
                affected[0]++;
            });

            anchorRate = Mathf.lerpDelta(anchorRate, stabilityPerSecond * scale * affected[0], 0.16f);
        }

        protected void applyAnchor(PsychicBlock.PsychicBuild other, float stabilityStep, float disorderStep, float overloadStep) {
            other.addPsychicStability(stabilityStep);
            other.disorder = Mathf.approachDelta(other.disorder, 0f, disorderStep);
            other.overload = Mathf.approachDelta(other.overload, 0f, overloadStep);
        }
    }
}
