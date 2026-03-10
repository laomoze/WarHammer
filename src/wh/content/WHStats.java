package wh.content;

import mindustry.world.meta.*;

public class WHStats{
    public static final Stat
    baseHeatProduction =new Stat("wh-base-heat-production"),
    overloadHeatProduction =new Stat("wh-overload-heat-production"),
    heatLoss = new Stat("wh-heat-loss"),
    overloadThreshold = new Stat("wh-overload-threshold"),
    overloadEfficiency = new Stat("wh-overload-efficiency"),
    heatReduceMax=new Stat("wh-heat-reduce-max"),

    increaseWhenShooting = new Stat("wh-increase-when-shooting", StatCat.function),
    decreaseNotShooting = new Stat("wh-decrease-not-shooting", StatCat.function),
    maxBoostPercent = new Stat("wh-max-boost-percent", StatCat.function),
    reactionArmorTriggerHits = new Stat("wh-reaction-armor-trigger-hits", StatCat.function),
    reactionArmorLayers = new Stat("wh-reaction-armor-layers", StatCat.function),
    sharedDamageReduction = new Stat("wh-shared-damage-reduction", StatCat.function),

    payloadIsBuildRate = new Stat("wh-payload-is-build-rate", StatCat.function);
}
