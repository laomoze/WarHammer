package wh.entities.event.logic.actionLogic;

import arc.math.*;
import arc.util.*;
import mindustry.logic.*;

/**
 * Action 逻辑语句的通用生命周期驱动器。
 *
 * 边沿触发语义：
 * - run: 0 -> 非 0 时触发 begin()
 * - run 持续非 0 时：若动作仍在计时则继续执行；若已结束则不会重复触发
 * - run 回到 0 时：复位，允许下一次重新触发
 */
public abstract class ActionInstruction implements LExecutor.LInstruction{
    private static final float GLOBAL_FADE_ALPHA = 0.22f;
    private static final float GLOBAL_FADE_HOLD_TICKS = 12f;

    protected final LVar run;
    protected final LVar out;

    private boolean lastRunHigh = false;
    private boolean running = false;   // 是否处于计时执行状态
    private float lifeTicks = 0f;      // 已执行的 tick
    private float maxTicks = 0f;       // 总持续 tick

    protected ActionInstruction(LVar run, LVar out){
        this.run = run;
        this.out = out;
    }

    @Override
    public final void run(LExecutor exec){
        if(!isRunHigh()){
            onRunLow(exec);
            return;
        }

        boolean risingEdge = isRisingEdge();

        // run 保持高电平且动作已结束时，不重复触发。
        if(!risingEdge && !running){
            setOut(0f);
            return;
        }

        if(risingEdge){
            if(!tryBegin(exec)){
                setOut(0f);
                return;
            }

            // begin() 未进入计时态，视为瞬时动作，直接成功。
            if(!running){
                setOut(1f);
                return;
            }
        }

        tick(exec);
        setOut(1f);
    }

    private boolean isRunHigh(){
        return run == null || run.numi() != 0;
    }

    private boolean isRisingEdge(){
        boolean rising = !lastRunHigh;
        lastRunHigh = true;
        return rising;
    }

    private void onRunLow(LExecutor exec){
        if(running){
            cancel(exec);
            stop();
        }

        lastRunHigh = false;
        setOut(0f);
    }

    private boolean tryBegin(LExecutor exec){
        if(!begin(exec)){
            return false;
        }

        if(enableGlobalFade()){
            ActionLogicSupport.pulseOverlay(globalFadeAlpha(), globalFadeHoldTicks());
        }
        return true;
    }

    private void tick(LExecutor exec){
        lifeTicks += Time.delta;
        update(exec, progress());

        if(lifeTicks >= maxTicks){
            end(exec);
            stop();
            return;
        }

        holdCurrentInstruction(exec);
    }

    private static void holdCurrentInstruction(LExecutor exec){
        if(exec == null) return;

        // 让逻辑处理器停留在当前语句，下一帧继续执行本语句。
        exec.counter.numval--;
        exec.yield = true;
    }

    protected abstract boolean begin(LExecutor exec);

    protected boolean enableGlobalFade(){
        return false;
    }

    protected float globalFadeAlpha(){
        return GLOBAL_FADE_ALPHA;
    }

    protected float globalFadeHoldTicks(){
        return GLOBAL_FADE_HOLD_TICKS;
    }

    protected void update(LExecutor exec, float progress){
    }

    protected void end(LExecutor exec){
    }

    protected void cancel(LExecutor exec){
        end(exec);
    }

    protected final void startTimed(float durationTicks){
        if(durationTicks <= 0f){
            stop();
            return;
        }

        running = true;
        lifeTicks = 0f;
        maxTicks = durationTicks;
    }

    protected final void stop(){
        running = false;
        lifeTicks = 0f;
        maxTicks = 0f;
    }

    protected final boolean running(){
        return running;
    }

    protected final float progress(){
        return maxTicks <= 0f ? 1f : Mathf.clamp(lifeTicks / maxTicks);
    }

    protected final float maxTicks(){
        return maxTicks;
    }

    protected final void setOut(float value){
        if(out != null){
            out.setnum(value);
        }
    }
}
