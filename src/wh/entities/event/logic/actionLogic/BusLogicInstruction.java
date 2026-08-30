package wh.entities.event.logic.actionLogic;

import mindustry.logic.LExecutor;
import wh.entities.event.ui.ActionContext;

/**
 * Runs a Logic instruction as an independent ActionBus action.
 */
public abstract class BusLogicInstruction implements LExecutor.LInstruction {
    private boolean scheduled;
    private LExecutor executor;

    @Override
    public final void run(LExecutor exec) {
        if (scheduled || !canStart(exec)) return;

        scheduled = true;
        executor = exec;
        ActionBus bus = new ActionBus();
        bus.add(new RuntimeAction());
        ActionContext.cutscene.addSubActionBus(bus);
    }

    protected boolean canStart(LExecutor exec) {
        return canStart();
    }

    protected boolean canStart() {
        return true;
    }

    protected final LExecutor executor() {
        return executor;
    }

    /**
     * Returning false pauses the active action without completing it.
     */
    protected boolean canUpdate() {
        return true;
    }

    protected void beginAction() {
    }

    /**
     * Return true when the action reached its normal end.
     */
    protected abstract boolean updateAction();

    protected void endAction() {
    }

    protected void cancelAction() {
    }

    private final class RuntimeAction extends Action {
        private boolean finished;

        @Override
        public void begin() {
            beginAction();
        }

        @Override
        public void update() {
            if (!canUpdate()) return;
            finished = updateAction();
        }

        @Override
        public boolean complete() {
            return finished;
        }

        @Override
        public void end() {
            if (!cancelled) endAction();
            scheduled = false;
            executor = null;
        }

        @Override
        public void skip() {
            if (!cancelled) cancelAction();
            super.skip();
            scheduled = false;
            executor = null;
        }
    }
}
