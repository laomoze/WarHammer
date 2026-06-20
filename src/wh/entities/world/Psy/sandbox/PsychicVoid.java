package wh.entities.world.Psy.sandbox;

import wh.entities.world.Psy.PsychicBlock;

public class PsychicVoid extends PsychicBlock {
    public PsychicVoid(String name) {
        super(name);
        psychicCapacity = 0f;
        acceptsPsychicLinks = true;
        outputsPsychicLinks = false;
        configurable = false;
        drawArrow = false;
    }

    public class PsychicVoidBuild extends PsychicBuild {
        @Override
        public float inputPotential() {
            return 0f;
        }

        @Override
        public float getEnergyNeed() {
            return Float.MAX_VALUE;
        }

        @Override
        public float handleEnergy(float amount) {
            return enabled ? Math.max(amount, 0f) : 0f;
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
