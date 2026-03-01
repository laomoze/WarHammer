package wh.entities.event.objective;

import arc.util.*;
import mindustry.game.*;

import java.util.*;

import static mindustry.Vars.state;

/**
 * Dedicated raid-event timer objective.
 */
public class RaidEventObjective extends MapObjectives.MapObjective{
    public @MapObjectives.Second float duration = 60f * 10f;
    public String key = "raid-event";

    protected boolean triggered = false;
    protected float countup;

    public RaidEventObjective(String key){
        this.key = key;
    }

    public RaidEventObjective(){
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
        this.duration = Math.max(1f, duration);
        countup = 0f;
        triggered = true;
    }

    public void finish(){
        countup = duration;
        triggered = false;
    }

    public float getCountup(){
        return countup;
    }

    @Override
    public boolean qualified(){
        return triggered;
    }

    public static RaidEventObjective obtain(String key){
        final RaidEventObjective[] objective = {null};
        state.rules.objectives.each(mapObjective -> {
            if(mapObjective instanceof RaidEventObjective raidObjective && Objects.equals(raidObjective.key, key)){
                objective[0] = raidObjective;
            }
        });

        if(objective[0] == null){
            objective[0] = new RaidEventObjective(key);
            state.rules.objectives.all.add(objective[0]);
        }
        return objective[0];
    }
}
