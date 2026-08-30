package wh.entities.event.ui;

import wh.entities.event.logic.actionLogic.CutsceneControl;

/**
 * 事件动作的共享上下文。
 * 当前仅维护一个全局过场 UI 实例。
 */
public final class ActionContext{
    public static final CutsceneControl cutscene = new CutsceneControl();
    public static final WHCutsceneUI cutsceneUI = new WHCutsceneUI();

    private ActionContext(){
    }
}
