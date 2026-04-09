package wh.gen.CarrierUnit;

import arc.util.*;
import mindustry.world.blocks.payloads.*;
import wh.entities.world.entities.*;

/**
 * Rearm + launch flow extracted from CarrierRuntime for readability.
 * Keep logic equivalent to runtime behavior.
 */
final class CarrierLaunchFlow{
    private static final float eps = 0.001f;

    private CarrierLaunchFlow(){
    }

    static void updateRearm(CarrierRuntime host, CarrierUnitType ctype){
        host.rebuildTargetRunwayCounts(ctype);
        boolean full = true;
        for(int runway = 0; runway < host.runwayCount(); runway++){
            int target = host.runwayTargetCount(runway);
            if(host.assignedFightersInRunway(runway) < target){
                full = false;
                break;
            }
        }

        if(full){
            host.regrouping = false;
            host.lossCount = 0;
            for(CarrierRuntime.RunwayLane lane : host.runwayLanes){
                if(lane != null){
                    lane.rearmReload = 0f;
                    lane.regroupDelayTimer = 0f;
                }
            }
            return;
        }

        int runways = host.runwayCount();
        for(int i = 0; i < runways; i++){
            CarrierRuntime.RunwayLane lane = host.runwayLane(i);
            lane.rearmReload = Math.max(lane.rearmReload - Time.delta, 0f);
        }

        for(int runway = 0; runway < runways; runway++){
            CarrierRuntime.RunwayLane lane = host.runwayLane(runway);
            if(lane.rearmReload > 0.001f) continue;

            int target = host.runwayTargetCount(runway);
            if(host.assignedFightersInRunway(runway) >= target) continue;
            if(lane.storedFighterCount >= host.runwayCapacity(runway)) continue;
            if(!host.runwayCanRearmNow(runway)) continue;
            if(host.runwayHasConstructingPayload(runway)) continue;

            host.createDeckFighter(ctype, runway);
            lane.rearmReload = Math.max(ctype.rearmInterval, 1f);
        }
    }

    static void updateLaunch(CarrierRuntime host, CarrierUnitType ctype){
        if(host.payloads().isEmpty()){
            if(host.runwayLanes != null){
                for(CarrierRuntime.RunwayLane lane : host.runwayLanes){
                    if(lane != null){
                        lane.launchReload = 0f;
                        lane.launchWaveActive = false;
                    }
                }
            }
            return;
        }

        for(int runway = 0; runway < host.runwayCount(); runway++){
            CarrierRuntime.RunwayLane lane = host.runwayLane(runway);
            lane.launchReload = Math.max(lane.launchReload - Time.delta, 0f);
            if(lane.launchReload > eps) continue;

            // Check full/healthy gate only when opening a launch wave.
            // Once opened, keep launching this runway's current queue instead of reverting to one-by-one.
            if(!lane.launchWaveActive){
                if(host.runwayLaunchBlocked(runway)) continue;
                if(!host.runwayReadyForLaunchWave(runway)) continue;
                lane.launchWaveActive = true;
            }

            if(host.runwayLaunchBlocked(runway)){
                // Keep wave active, wait until runway is free, then continue with interval pacing.
                continue;
            }

            boolean launched = launchOneFromRunway(host, runway, ctype);

            if(host.frontUnitPayloadInRunway(runway) == null || lane.storedFighterCount <= 0){
                lane.launchWaveActive = false;
                lane.launchReload = 0f;
                continue;
            }

            // Preserve classic per-fighter launch interval even during a wave.
            lane.launchReload = launchInterval(ctype);
            if(!launched){
                // Keep wave active and retry next interval when front payload becomes launchable.
                lane.launchWaveActive = true;
            }
        }
    }

    private static float launchInterval(CarrierUnitType ctype){
        return Math.max(ctype.launchInterval, 1f);
    }

    private static boolean launchOneFromRunway(CarrierRuntime host, int runway, CarrierUnitType ctype){
        UnitPayload payload = host.frontUnitPayloadInRunway(runway);
        if(payload == null) return false;
        if(!host.launchStateReady(payload, runway, ctype)) return false;

        int launchSlot = host.deckSlotForPayloadInternal(payload);
        if(!host.launchFighter(payload, runway, launchSlot, ctype)) return false;

        host.removeLaunchedPayloadFromDeck(runway, payload);
        return true;
    }
}
