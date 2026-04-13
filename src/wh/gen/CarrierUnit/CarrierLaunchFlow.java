package wh.gen.CarrierUnit;

import arc.util.Time;
import mindustry.world.blocks.payloads.UnitPayload;
import wh.entities.world.entities.CarrierUnitType;

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

            // 仅在开启一轮发射波时检查“满编/满血”门限。
            // 一旦开启，优先把当前跑道队列打完，不再退回逐个判定。
            if(!lane.launchWaveActive){
                if(host.runwayLaunchBlocked(runway)) continue;
                if(!host.runwayReadyForLaunchWave(runway)) continue;
                lane.launchWaveActive = true;
            }

            if(host.runwayLaunchBlocked(runway)){
                // 波次保持激活，等待跑道解锁后按间隔继续发射。
                continue;
            }

            boolean launched = launchOneFromRunway(host, runway, ctype);

            if(host.frontUnitPayloadInRunway(runway) == null || lane.storedFighterCount <= 0){
                lane.launchWaveActive = false;
                lane.launchReload = 0f;
                continue;
            }

            // 即使在波次中，也保持“每架战机一次间隔”的节奏。
            lane.launchReload = launchInterval(ctype);
            if(!launched){
                // 本轮未发出时保持波次，下一次间隔再重试队头载荷。
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
