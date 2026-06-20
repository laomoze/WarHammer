package wh.entities.world.Psy;

public interface PsychicNetworkNode {
    float epsilon = 0.0001f;

    default boolean acceptsPsychicLinks() {
        return acceptEnergy(null);
    }

    default boolean outputsPsychicLinks() {
        return outputEnergy();
    }

    default boolean acceptEnergy(PsychicNetworkNode source) {
        return false;
    }

    default boolean outputEnergy() {
        return false;
    }

    default float getEnergyNeed() {
        return 0f;
    }

    default float getEnergy() {
        return 0f;
    }

    default float getEnergyPotential() {
        return getEnergy();
    }

    default float inputPotential() {
        return getEnergyPotential();
    }

    default float outputPotential() {
        return getEnergyPotential();
    }

    default float resident() {
        return 0f;
    }

    default int energyPriority() {
        return 0;
    }

    default float energyTransferScale() {
        return 1f;
    }

    default float getEnergyPressure(PsychicNetworkNode next) {
        if (next == null || !outputEnergy() || !next.acceptEnergy(this)) return 0f;
        if (next.getEnergyNeed() <= epsilon) return 0f;
        return Math.max(outputPotential() - next.inputPotential(), 0f);
    }

    default float getEnergyMoveRate(PsychicNetworkNode next, float budget, float sourceAvailable, float targetSpace) {
        if (sourceAvailable <= epsilon || targetSpace <= epsilon) return 0f;

        float pressure = getEnergyPressure(next);
        if (pressure <= epsilon) return 0f;

        float usablePressure = pressure - next.resident();
        if (usablePressure <= epsilon) return 0f;

        float safeBudget = Math.max(budget, epsilon);
        float pressureScale = usablePressure / (usablePressure + 10f);
        float fullPressureScale = Math.min(usablePressure / 60f, 1f) * 0.25f;
        float transferScale = Math.max(energyTransferScale() * next.energyTransferScale(), 0f);
        float moved = safeBudget * Math.min(pressureScale + fullPressureScale, 1f) * transferScale;

        if (usablePressure > 60f) {
            float overflowPressure = usablePressure - 60f;
            moved += safeBudget * overflowPressure / 45f * transferScale;
        }

        return Math.min(Math.max(moved, 0f), Math.min(sourceAvailable, targetSpace));
    }

    default float getEnergyMoveRate(PsychicNetworkNode next, float budget, float efficiency) {
        if (next == null || efficiency <= epsilon) return 0f;
        return getEnergyMoveRate(next, budget, getEnergy(), next.getEnergyNeed() / efficiency);
    }

    default float handleEnergy(float amount) {
        return 0f;
    }

    default float removeEnergy(float amount) {
        return 0f;
    }

    default float moveEnergyTo(PsychicNetworkNode next, float budget, float efficiency) {
        if (next == null || efficiency <= epsilon) return 0f;

        float pressure = getEnergyPressure(next);
        float moved = getEnergyMoveRate(next, budget, efficiency);
        if (moved <= epsilon) return 0f;

        float removed = removeEnergy(moved);
        if (removed <= epsilon) return 0f;

        float delivered = next.handleEnergy(removed * efficiency);
        float actualMoved = delivered / Math.max(efficiency, epsilon);
        float refund = removed - actualMoved;
        if (refund > epsilon) handleEnergy(refund);

        if (actualMoved > epsilon) {
            energyMoved(next, actualMoved, false);
            next.energyMoved(this, delivered, true);

            float safeBudget = Math.max(budget, epsilon);
            float pressureScale = Math.min(Math.max(pressure / 60f, 0f), 2f);
            float overflowMoved = Math.max(actualMoved - safeBudget, 0f);
            float overflowScale = overflowMoved / safeBudget;
            onEnergyOverload(actualMoved * pressureScale * 0.0025f + overflowScale * 0.035f);
            next.onEnergyOverload(delivered * pressureScale * 0.0035f + overflowScale * 0.05f);
        }

        return actualMoved;
    }

    default void energyMoved(PsychicNetworkNode other, float amount, boolean incoming) {
    }

    default void onEnergyOverload(float amount) {
    }
}
