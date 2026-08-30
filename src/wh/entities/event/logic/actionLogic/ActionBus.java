package wh.entities.event.logic.actionLogic;

import arc.Core;

import static wh.entities.event.ui.ActionContext.cutsceneUI;

public class ActionBus extends TimeQueue<Action> {
    public boolean skipping = false;

    public void skip() {
        skipping = true;
        if (current != null) current.skip();
        while (!queue.isEmpty()) queue.removeLast().skip();
        skipping = false;
        current = null;
        queue.clear();
        if (!mindustry.Vars.headless && Core.app != null) {
            Core.app.post(() -> cutsceneUI.controlOverride = false);
        }
    }

    public void skipCurrent() {
        if (current != null) {
            current.skip();
            super.skipCurrent();
        }
    }
}
