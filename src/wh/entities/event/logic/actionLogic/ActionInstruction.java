package wh.entities.event.logic.actionLogic;

import arc.math.*;
import arc.util.*;
import mindustry.logic.*;

/**
 * 动作语句的通用生命周期驱动器。
 * <p>
 * 边沿触发语义：
 * run 从 0 变为非 0 时触发；回到 0 后复位，可再次触发。
 */
public abstract class ActionInstruction implements LExecutor.LInstruction{
    private static final float GLOBAL_FADE_ALPHA = 0.22f;
    private static final float GLOBAL_FADE_HOLD_TICKS = 12f;

    protected final LVar run;
    protected final LVar out;

    private boolean armed = true;
    private boolean running = false;
    private float lifeTicks = 0f;
    private float maxTicks = 0f;

    protected ActionInstruction(LVar run, LVar out){
        this.run = run;
        this.out = out;
    }

    @Override
    public final void run(LExecutor exec){
        // run == 0：取消当前计时阶段并复位，等待下一次 0->1 边沿。
        if(run != null && run.numi() == 0){
            if(running){
                cancel(exec);
                stop();
            }
            armed = true;
            setOut(0f);
            return;
        }

        if(!armed && !running){
            setOut(0f);
            return;
        }

        if(!running){
            armed = false;
            // begin() 返回 false 说明启动失败：输出保持 0，并允许在首次成功前继续重试。
            if(!begin(exec)){
                setOut(0f);
                return;
            }
            if(enableGlobalFade()){
                ActionLogicSupport.pulseOverlay(globalFadeAlpha(), globalFadeHoldTicks());
            }
        }

        if(running){
            lifeTicks += Time.delta;
            update(exec, progress());

            if(lifeTicks >= maxTicks){
                end(exec);
                stop();
            }else if(exec != null){
                // 停留在当前语句，下一帧继续执行。
                exec.counter.numval--;
                exec.yield = true;
            }
        }

        setOut(1f);
    }

    protected abstract boolean begin(LExecutor exec);

    protected boolean enableGlobalFade(){
        return true;
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
