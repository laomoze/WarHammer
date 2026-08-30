package wh.entities.event.logic.actionLogic;

import arc.util.Time;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import wh.entities.event.ui.ActionContext;

public abstract class ActionInstruction implements LExecutor.LInstruction {
    protected final LVar run;
    protected final LVar out;
    private boolean lastRunHigh;
    private RuntimeAction runtimeAction;
    private LExecutor owner;

    protected ActionInstruction(LVar run, LVar out) {
        this.run = run;
        this.out = out;
    }

    @Override
    public final void run(LExecutor exec) {
        if (!isRunHigh()) {
            if (runtimeAction != null && !runtimeAction.complete()) runtimeAction.skip();
            runtimeAction = null;
            owner = null;
            lastRunHigh = false;
            setOut(0f);
            return;
        }

        if (!lastRunHigh) {
            lastRunHigh = true;
            owner = exec;
            runtimeAction = new RuntimeAction();
            ActionBus bus = new ActionBus();
            bus.add(runtimeAction);
            if (useScreenBus()) {
                ActionContext.cutscene.addScreenActionBus(bus);
            } else if (useSubBus()) {
                ActionContext.cutscene.addSubActionBus(bus);
            } else {
                ActionContext.cutscene.addMainActionBus(bus);
            }
            setOut(1f);
        }
    }

    private boolean isRunHigh() {
        return run == null || run.numi() != 0;
    }

    protected boolean useSubBus() {
        return false;
    }

    protected boolean useScreenBus() {
        return false;
    }

    private final class RuntimeAction extends Action {
        private boolean started;

        @Override
        public void begin() {
            started = true;
            if (cancelled) return;
            if (!ActionInstruction.this.begin(owner)) {
                lifeTimer = duration;
                return;
            }
            if (enableGlobalFade()) {
                ActionLogicSupport.pulseOverlay(globalFadeAlpha(), globalFadeHoldTicks());
            }
        }

        @Override
        public void update() {
            if (lifeTimer >= duration) return;
            lifeTimer += Time.delta;
            ActionInstruction.this.update(owner, progress());
        }

        @Override
        public void end() {
            if (started && !cancelled) ActionInstruction.this.end(owner);
            setOut(0f);
        }

        @Override
        public void skip() {
            if (started && !cancelled) ActionInstruction.this.cancel(owner);
            super.skip();
            setOut(0f);
        }
    }

    protected abstract boolean begin(LExecutor exec);

    protected boolean enableGlobalFade() {
        return false;
    }

    protected float globalFadeAlpha() {
        return 0.22f;
    }

    protected float globalFadeHoldTicks() {
        return 12f;
    }

    protected void update(LExecutor exec, float progress) {
    }

    protected void end(LExecutor exec) {
    }

    protected void cancel(LExecutor exec) {
        end(exec);
    }

    protected final void startTimed(float durationTicks) {
        if (runtimeAction == null) return;
        runtimeAction.duration = Math.max(0f, durationTicks);
        runtimeAction.lifeTimer = 0f;
    }

    protected final void stop() {
        if (runtimeAction != null) runtimeAction.lifeTimer = runtimeAction.duration;
    }

    protected final boolean running() {
        return runtimeAction != null && !runtimeAction.complete();
    }

    protected final float progress() {
        return runtimeAction == null ? 1f : runtimeAction.progress();
    }

    protected final float maxTicks() {
        return runtimeAction == null ? 0f : runtimeAction.duration;
    }

    protected final void setOut(float value) {
        if (out != null) out.setnum(value);
    }
}
