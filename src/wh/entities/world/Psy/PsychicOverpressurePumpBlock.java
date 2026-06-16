package wh.entities.world.Psy;

import arc.math.Mathf;
import arc.util.Strings;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

public class PsychicOverpressurePumpBlock extends PsychicBlock {
    public float pumpRange = 10f;
    public float psychicUsePerSecond = 3.5f;
    public float pressureBoostPerSecond = 0.18f;
    public float selfOverloadPerSecond = 0.08f;
    public float selfDisorderPerSecond = 0.04f;

    public PsychicOverpressurePumpBlock(String name) {
        super(name);
        acceptsPsychicLinks = true;
        outputsPsychicLinks = false;
        buildType = PsychicOverpressurePumpBuild::new;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, pumpRange, StatUnit.blocks);
        PsychicStatValues.add(stats, WHStats.psychicConsumption, psychicUsePerSecond, StatUnit.perSecond);
        PsychicStatValues.add(stats, WHStats.psychicPressureBoost, pressureBoostPerSecond * 100f, StatUnit.percent);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-pressure", (PsychicOverpressurePumpBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-pressure", Strings.autoFixed(build.pumpRate * 100f, 0)),
                () -> overloadColor,
                () -> Mathf.clamp(build.pumpRate / Math.max(pressureBoostPerSecond * 4f, 0.0001f))
        ));
    }

    public class PsychicOverpressurePumpBuild extends PsychicBuild {
        public float pumpRate;

        @Override
        protected void updatePsychicState() {
            super.updatePsychicState();

            if (!enabled || psychicStored() <= PsychicNetworkNode.epsilon) {
                pumpRate = Mathf.lerpDelta(pumpRate, 0f, 0.1f);
                return;
            }

            float used = drainPsychic(psychicUsePerSecond / 60f * delta());
            float scale = used * 60f / Math.max(psychicUsePerSecond, PsychicNetworkNode.epsilon);
            if (scale <= PsychicNetworkNode.epsilon) {
                pumpRate = Mathf.lerpDelta(pumpRate, 0f, 0.1f);
                return;
            }

            float pressureStep = pressureBoostPerSecond / 60f * delta() * scale;
            int[] affected = {1};

            addPsychicPressure(pressureStep);
            addPsychicOverload(selfOverloadPerSecond / 60f * delta() * scale);
            addPsychicDisorder(selfDisorderPerSecond / 60f * delta() * scale);

            eachNearbyPsychicBuild(pumpRange, other -> {
                other.addPsychicPressure(pressureStep);
                affected[0]++;
            });

            pumpRate = Mathf.lerpDelta(pumpRate, pressureBoostPerSecond * scale * affected[0], 0.16f);
        }
    }
}
