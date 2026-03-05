package wh.content;

import mindustry.game.*;
import wh.entities.event.mapmarker.*;
import wh.entities.event.objective.*;

/**
 * Registers custom objective types used by WARHAMMER event logic.
 */
public final class WHObjectiveRegistry{
    private static boolean loaded = false;

    private WHObjectiveRegistry(){
    }

    public static void load(){
        if(loaded) return;
        loaded = true;

        MapObjectives.registerMarker(RaidIndicator::new);
        MapObjectives.registerObjective(TriggerObjective::new);
        MapObjectives.registerObjective(JumpInTriggerObjective::new);
        MapObjectives.registerObjective(RaidEventObjective::new);
    }
}
