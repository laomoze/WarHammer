package wh.entities.world.Psy;

import arc.math.Mathf;
import mindustry.gen.Building;

public interface PsychicConsumer extends PsychicStorage {
    boolean psychicEnabled();

    float psychicOverload();

    void psychicOverload(float value);

    float psychicOverloadBlockScale();

    default float updatePsychicConsumer(Building building, float exposure, float passiveLoss, float overloadDecay,
                                        float healthLoss, float dangerThreshold, float dangerExponent) {
        float overload = psychicOverload();
        if (passiveLoss > 0f) {
            psychicModule().remove(passiveLoss / 60f * building.delta());
        }
        if (overload > epsilon) {
            exposure += building.delta() / 60f * Mathf.clamp(overload);
        } else {
            exposure = Mathf.approachDelta(exposure, 0f, 0.03f);
        }
        if (overload > dangerThreshold && healthLoss > 0f) {
            float severity = Mathf.pow(Math.max(overload - dangerThreshold, 0f), dangerExponent);
            building.damage(severity * (1f + exposure) * healthLoss / 60f * building.delta());
        }
        psychicOverload(Mathf.approachDelta(overload, 0f, overloadDecay));
        psychicModule().clamp(psychicCapacity());
        return exposure;
    }

    @Override
    default float getEnergyPotential() {
        return psychicStored();
    }

    @Override
    default float inputPotential() {
        return psychicSpace() > epsilon ? 0f : psychicStored();
    }

    @Override
    default boolean acceptEnergy(PsychicNetworkNode source) {
        return psychicEnabled() && psychicSpace() > epsilon;
    }

    @Override
    default float getEnergyNeed() {
        return acceptEnergy(null) ? psychicSpace() : 0f;
    }

    @Override
    default float drag() {
        return psychicOverload() * psychicOverloadBlockScale();
    }

    @Override
    default float energyTransferScale() {
        return Mathf.clamp(1f - psychicOverload() * 0.2f, 0.2f, 1f);
    }

    @Override
    default float handleEnergy(float amount) {
        return acceptEnergy(null) ? addPsychic(amount) : 0f;
    }

    @Override
    default void onEnergyOverload(float amount) {
        psychicOverload(Math.max(psychicOverload() + amount, 0f));
    }
}
