package wh.entities.event.objective;

import arc.util.*;
import mindustry.game.*;

import java.util.*;

import static mindustry.Vars.state;

/**
 * Timer objective for jump-in events. No world/minimap marker is attached.
 */
public class JumpInTriggerObjective extends MapObjectives.MapObjective{
    public @MapObjectives.Second float duration = 60f * 10f;
    public String timer = "jumpin-timer";
    /** optional start delay (seconds) before the timer actually begins. */
    public @MapObjectives.Second float delay = 0f;

    protected boolean triggered = false;
    protected float countup;
    protected float delayTicks;

    public JumpInTriggerObjective(String timer){
        this.timer = timer;
    }

    public JumpInTriggerObjective(){
    }

    @Override
    public boolean update(){
        if(!triggered) return false;

        if(delayTicks > 0f){
            delayTicks -= Time.delta;
            return false;
        }

        if(countup <= duration){
            countup += Time.delta;
        }else{
            triggered = false;
        }
        return false;
    }

    public void trigger(float duration){
        trigger(duration, 0f);
    }

    public void trigger(float duration, float delaySec){
        this.duration = Math.max(1f, duration);
        this.delay = Math.max(0f, delaySec);
        countup = 0f;
        triggered = true;
        delayTicks = this.delay * Time.toSeconds;
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

    public static JumpInTriggerObjective obtain(String timer){
        final JumpInTriggerObjective[] objective = {find(timer)};

        if(objective[0] == null){
            objective[0] = new JumpInTriggerObjective(timer);
            state.rules.objectives.all.add(objective[0]);
        }else{
            objective[0].timer = timer;
        }
        return objective[0];
    }

    public static JumpInTriggerObjective find(String timer){
        final JumpInTriggerObjective[] objective = {null};
        state.rules.objectives.each(mapObjective -> {
            if(mapObjective instanceof JumpInTriggerObjective jumpObjective && Objects.equals(jumpObjective.timer, timer)){
                objective[0] = jumpObjective;
            }
        });
        return objective[0];
    }
}
