package wh.entities.event.objective;

import arc.util.*;
import mindustry.game.*;

import java.util.*;

import static mindustry.Vars.state;

/**
 * One-shot timer objective that can be triggered by logic code.
 */
public class TriggerObjective extends MapObjectives.MapObjective{
    public @MapObjectives.Second float duration = 60f * 10f;
    public String timer = "event-timer";

    protected boolean triggered = false;
    protected float countup;

    public TriggerObjective(String timer){
        this.timer = timer;
    }

    public TriggerObjective(){
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

    public static TriggerObjective obtain(String timer){
        final TriggerObjective[] objective = {find(timer)};

        if(objective[0] == null){
            objective[0] = new TriggerObjective(timer);
            state.rules.objectives.all.add(objective[0]);
        }
        return objective[0];
    }

    public static TriggerObjective find(String timer){
        final TriggerObjective[] objective = {null};
        state.rules.objectives.each(mapObjective -> {
            if(mapObjective instanceof TriggerObjective triggerObjective && Objects.equals(triggerObjective.timer, timer)){
                objective[0] = triggerObjective;
            }
        });
        return objective[0];
    }
}
