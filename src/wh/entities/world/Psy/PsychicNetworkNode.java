package wh.entities.world.Psy;

public interface PsychicNetworkNode {
    float epsilon = 0.0001f;

    /**
     * 是否愿意从 source 接收能量。
     */
    default boolean acceptEnergy(PsychicNetworkNode source) {
        return false;
    }

    /**
     * 是否有能量可以向外输出。
     */
    default boolean outputEnergy() {
        return false;
    }

    /**
     * 当前还能接收多少能量。
     */
    default float getEnergyNeed() {
        return 0f;
    }

    /**
     * 当前可以拿出来传输的能量。
     */
    default float getEnergy() {
        return 0f;
    }

    /**
     * 用于显示或默认计算的能势。
     */
    default float getEnergyPotential() {
        return getEnergy();
    }

    /**
     * 作为接收端时的能势，越高越不容易被灌入。
     */
    default float inputPotential() {
        return getEnergyPotential();
    }

    /**
     * 作为输出端时的能势，越高越容易向外流。
     */
    default float outputPotential() {
        return getEnergyPotential();
    }

    /**
     * 传输阻力 ，用来吃掉一部分小势差。
     */
    default float resident() {
        return 0f;
    }

    /**
     * 多个目标同时可传输时的优先级。
     */
    default int energyPriority() {
        return 0;
    }

    /**
     * 传输倍率，过载、稳定、增压都在这里统一影响流速。
     */
    default float energyTransferScale() {
        return 1f;
    }

    /**
     * 获取本方块对目标方块的能势差，高往低流。
     */
    default float getEnergyPressure(PsychicNetworkNode next) {
        if (next == null || !outputEnergy() || !next.acceptEnergy(this)) return 0f;
        if (next.getEnergyNeed() <= epsilon) return 0f;
        return Math.max(outputPotential() - next.inputPotential(), 0f);
    }

    /**
     * 获取本帧最多能向目标移动多少能量。
     */
    default float getEnergyMoveRate(PsychicNetworkNode next, float budget, float sourceAvailable, float targetSpace) {
        if (sourceAvailable <= epsilon || targetSpace <= epsilon) return 0f;

        float pressure = getEnergyPressure(next);
        if (pressure <= epsilon) return 0f;

        // 灵能按“势差水压”流动：低势差也会流，高势差接近传输上限。
        float usablePressure = pressure - next.resident();
        if (usablePressure <= epsilon) return 0f;

        float safeBudget = Math.max(budget, epsilon);
        float pressureScale = usablePressure / (usablePressure + 10f);
        float fullPressureScale = Math.min(usablePressure / 60f, 1f) * 0.25f;
        float transferScale = Math.max(energyTransferScale() * next.energyTransferScale(), 0f);
        float moved = safeBudget * Math.min(pressureScale + fullPressureScale, 1f) * transferScale;

        // transferRate 现在是安全流速，不再是硬上限；高压差可以超速，但会显著增加过载。
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

    /**
     * 实际收取能量，返回真正收下的量。
     */
    default float handleEnergy(float amount) {
        return 0f;
    }

    /**
     * 实际移除能量，返回真正扣掉的量。
     */
    default float removeEnergy(float amount) {
        return 0f;
    }

    /**
     * 从本方块向 next 传输能量。
     */
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

            // 高压差传输会让两端发热；超出安全流速的部分会额外堆积过载。
            float safeBudget = Math.max(budget, epsilon);
            float pressureScale = Math.min(Math.max(pressure / 60f, 0f), 2f);
            float overflowMoved = Math.max(actualMoved - safeBudget, 0f);
            float overflowScale = overflowMoved / safeBudget;
            float lost = actualMoved * Math.max(1f - Math.min(efficiency, 1f), 0f);
            onEnergyOverload(actualMoved * pressureScale * 0.0025f + overflowScale * 0.035f);
            next.onEnergyOverload(delivered * pressureScale * 0.0035f + overflowScale * 0.05f);
            if (overflowScale > epsilon) {
                float turbulence = overflowScale * Math.max(pressureScale, 0.25f);
                onEnergyDisorder(turbulence * 0.004f);
                next.onEnergyDisorder(turbulence * 0.006f);
            }
            if (lost > epsilon) {
                onEnergyDisorder(lost * (0.004f + overflowScale * 0.003f));
                next.onEnergyDisorder(lost * (0.01f + overflowScale * 0.005f));
            }
        }

        return actualMoved;
    }

    /**
     * 有能量流动后的回调，incoming 表示这次是不是流入本方块。
     */
    default void energyMoved(PsychicNetworkNode other, float amount, boolean incoming) {
    }

    default void onEnergyOverload(float amount) {
    }

    default void onEnergyDisorder(float amount) {
    }
}
