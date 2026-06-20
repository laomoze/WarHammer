package wh.entities.world.Psy.sandbox;

import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.entities.world.Psy.PsychicBlock;
import wh.entities.world.Psy.PsychicNetworkNode;
import wh.ui.PsychicStatValues;

public class PsychicSource extends PsychicBlock {
    public float generationRate = 1000f;

    public PsychicSource(String name) {
        super(name);
        psychicCapacity = 0f;
        acceptsPsychicLinks = false;
        outputsPsychicLinks = true;
        configurable = false;
        drawArrow = false;
    }

    @Override
    public void setStats() {
        super.setStats();
        PsychicStatValues.add(stats, WHStats.psychicProduction, generationRate, StatUnit.perSecond);
    }

    public class PsychicSourceBuild extends PsychicBuild {
        @Override
        public boolean outputEnergy() {
            return enabled;
        }

        @Override
        public float getEnergy() {
            return enabled ? Float.MAX_VALUE : 0f;
        }

        @Override
        public float outputPotential() {
            return enabled ? generationRate : 0f;
        }

        @Override
        public float removeEnergy(float amount) {
            return enabled ? Math.max(amount, 0f) : 0f;
        }

        @Override
        public void energyMoved(PsychicNetworkNode other, float amount, boolean incoming) {
        }

        @Override
        protected void updatePsychicState() {
            psychic.clear();
            overload = 0f;
            networkStability = 0f;
            pressureBoost = 0f;
        }
    }
}
