package wh.entities.event.objective;

import arc.util.*;
import mindustry.game.*;
import wh.entities.event.mapmarker.*;

import java.util.*;

import static mindustry.Vars.state;

/**
 * Dedicated raid-event timer objective (same timer semantics as TriggerObjective).
 */
public class RaidEventObjective extends MapObjectives.MapObjective{
    public @MapObjectives.Second float duration = 60f * 10f;
    public String key = "raid-event";

    protected boolean triggered = false;
    protected float countup;

    public RaidEventObjective(String key){
        this.key = key;
        ensureRaidIndicator();
    }

    public RaidEventObjective(){
        ensureRaidIndicator();
    }

    @Override
    public boolean update(){
        if(!triggered) return false;

        if(countup <= duration){
            countup += Time.delta;
        }else{
            triggered = false;
        }
        return false;
    }

    public void trigger(float duration){
        ensureRaidIndicator();
        this.duration = Math.max(1f, duration);
        countup = 0f;
        triggered = true;
        resetMarker();
    }

    public void trigger(float duration, float delaySec){
        trigger(duration);
    }

    public void finish(){
        countup = duration;
        triggered = false;
        resetMarker();
    }

    public float getCountup(){
        return countup;
    }

    @Override
    public boolean qualified(){
        return triggered;
    }

    public static RaidEventObjective obtain(String key){
        final RaidEventObjective[] objective = {find(key)};

        if(objective[0] == null){
            objective[0] = new RaidEventObjective(key);
            state.rules.objectives.all.add(objective[0]);
        }else{
            objective[0].key = key;
            objective[0].ensureRaidIndicator();
        }
        return objective[0];
    }

    public static RaidEventObjective find(String key){
        final RaidEventObjective[] objective = {null};
        state.rules.objectives.each(mapObjective -> {
            if(mapObjective instanceof RaidEventObjective raidObjective && Objects.equals(raidObjective.key, key)){
                objective[0] = raidObjective;
            }
        });
        return objective[0];
    }

    public RaidIndicator raidIndicator(){
        if(markers == null || markers.length == 0) return null;
        for(MapObjectives.ObjectiveMarker marker : markers){
            if(marker instanceof RaidIndicator raidIndicator){
                return raidIndicator;
            }
        }
        return null;
    }

    private void resetMarker(){
        RaidIndicator marker = raidIndicator();
        if(marker != null){
            marker.clear();
        }
    }

    private void ensureRaidIndicator(){
        RaidIndicator existing = raidIndicator();
        if(existing == null){
            existing = new RaidIndicator(key);
        }
        existing.timerName = key;
        existing.minimap = true;
        existing.world = true;

        // Keep only the raid indicator to avoid duplicate icons.
        markers = new MapObjectives.ObjectiveMarker[]{existing};
    }
}
