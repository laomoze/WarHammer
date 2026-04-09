package wh.gen.CarrierUnit;

import arc.util.*;
import mindustry.gen.*;
import wh.entities.world.entities.*;

/**
 * Recovery/landing pickup flow extracted from CarrierRuntime for readability.
 * Behavior must stay equivalent to runtime methods.
 */
final class CarrierRecoveryFlow{
    private CarrierRecoveryFlow(){
    }

    static void updateRecovery(CarrierRuntime host, CarrierUnitType ctype){
        if(host.payloads().size >= host.deckSlotCount()) return;

        for(int runway = 0; runway < host.runwayCount(); runway++){
            CarrierRuntime.RunwayLane lane = host.runwayLane(runway);
            lane.recoverReload = Math.max(lane.recoverReload - Time.delta, 0f);
            if(lane.recoverReload > 0.001f) continue;
            lane.recoverReload = Math.max(ctype.recoverCheckInterval, 0.1f);

            if(host.payloads().size >= host.deckSlotCount()) break;
            if(lane.storedFighterCount >= host.runwayCapacity(runway)) continue;

            host.runwayQueueBackPoint(runway, Tmp.v1);
            host.runwayQueueInsertPoint(runway, Tmp.v2);
            if(host.invalidLaunchPoint(Tmp.v1)){
                Tmp.v1.set(Tmp.v2);
            }
            if(host.invalidLaunchPoint(Tmp.v2)){
                Tmp.v2.set(Tmp.v1);
            }

            float touchX = Tmp.v1.x, touchY = Tmp.v1.y;
            float spread = Tmp.v1.dst(Tmp.v2);
            float range = Math.max(
            Math.max(ctype.recoverRadius * 2.6f, ctype.landingApproachRadius * 2.4f),
            Math.max(84f, spread + ctype.recoverRadius * 1.8f)
            );
            int expectedRunway = host.clampRunway(runway);

            for(int i = lane.airborneUnits.size - 1; i >= 0; i--){
                Unit fighter = lane.airborneUnits.get(i);
                if(!host.canRecoverCandidate(fighter, ctype, expectedRunway, touchX, touchY, range)) continue;
                host.tryRecoverFighter(fighter);
            }
        }
    }
}
