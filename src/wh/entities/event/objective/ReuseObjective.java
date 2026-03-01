package wh.entities.event.objective;

import arc.util.*;
import mindustry.game.*;

import static mindustry.Vars.state;

/**
 * Repeat executor flag while a trigger flag is present.
 */
public class ReuseObjective extends MapObjectives.MapObjective{
    public @MapObjectives.Second float duration = 60f * 30f;
    public String trigger = "trigger";
    public String executor = "executor";

    protected float countup;

    public ReuseObjective(float duration, String trigger, String executor){
        this.duration = duration;
        this.trigger = trigger;
        this.executor = executor;
    }

    public ReuseObjective(){
    }

    @Override
    public boolean update(){
        if(countup <= duration){
            countup += Time.delta;
        }else{
            countup %= Math.max(1f, duration);
            state.rules.objectiveFlags.add(executor);
        }
        return false;
    }

    public float getCountup(){
        return countup;
    }

    @Override
    public boolean qualified(){
        return state.rules.objectiveFlags.contains(trigger);
    }
}
