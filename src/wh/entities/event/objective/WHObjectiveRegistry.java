package wh.entities.event.objective;

import mindustry.game.*;

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

        MapObjectives.registerObjective(ReuseObjective::new);
        MapObjectives.registerObjective(TriggerObjective::new);
        MapObjectives.registerObjective(RaidEventObjective::new);
    }
}
