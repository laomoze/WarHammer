package wh.content;

import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;

public class WHStats{
    public static final Stat
    baseHeatProduction =new Stat("wh-base-heat-production"),
    overloadHeatProduction =new Stat("wh-overload-heat-production"),
    heatLoss = new Stat("wh-heat-loss"),
    overloadThreshold = new Stat("wh-overload-threshold"),
    overloadEfficiency = new Stat("wh-overload-efficiency"),
    heatReduceMax=new Stat("wh-heat-reduce-max"),
            psychicCapacity = new Stat("wh-psychic-capacity"),
            psychicHarvest = new Stat("wh-psychic-harvest", StatCat.function),
            psychicProduction = new Stat("wh-psychic-production", StatCat.function),
            psychicConsumption = new Stat("wh-psychic-consumption", StatCat.function),
            psychicSuppression = new Stat("wh-psychic-suppression", StatCat.function),
            psychicLoss = new Stat("wh-psychic-loss", StatCat.function),
            psychicThreshold = new Stat("wh-psychic-threshold", StatCat.function),
            psychicFieldWidth = new Stat("wh-psychic-field-width", StatCat.function),
            psychicFieldHeight = new Stat("wh-psychic-field-height", StatCat.function),
            psychicTransferRate = new Stat("wh-psychic-transfer-rate", StatCat.function),
            psychicLinkRange = new Stat("wh-psychic-link-range", StatCat.function),
            psychicCoverageRange = new Stat("wh-psychic-coverage-range", StatCat.function),
            psychicCoverageCount = new Stat("wh-psychic-coverage-count", StatCat.function),
            psychicFullFlowThreshold = new Stat("wh-psychic-full-flow-threshold", StatCat.function),

    increaseWhenShooting = new Stat("wh-increase-when-shooting", StatCat.function),
    decreaseNotShooting = new Stat("wh-decrease-not-shooting", StatCat.function),
    maxBoostPercent = new Stat("wh-max-boost-percent", StatCat.function),
    reactionArmorTriggerHits = new Stat("wh-reaction-armor-trigger-hits", StatCat.function),
    reactionArmorLayers = new Stat("wh-reaction-armor-layers", StatCat.function),
    sharedDamageReduction = new Stat("wh-shared-damage-reduction", StatCat.function),

    payloadIsBuildRate = new Stat("wh-payload-is-build-rate", StatCat.function);

    public static String format(Stat stat, Object value) {
        return stat.localized() + ": [white]" + value;
    }

    public static String format(String name, Object value) {
        return new Stat(name, StatCat.function).localized() + ": [white]" + value;
    }
}
