package wh.entities.event.logic.actionLogic;

import arc.Events;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;

import static wh.entities.event.ui.ActionContext.cutsceneUI;

public class CutsceneControl {
    public boolean waiting = false;
    public float waitSpacing = 60f;
    public float waitTimer = 0f;

    public ActionBus mainBus;
    public Seq<ActionBus> subBuses = new Seq<>();
    public Queue<ActionBus> waitingBuses = new Queue<>();
    public ActionBus screenBus;
    public Queue<ActionBus> waitingScreenBuses = new Queue<>();

    public CutsceneControl() {
        Events.on(EventType.WorldLoadEvent.class, event -> clear());
    }

    public void update() {
        updateMainBus();
        updateScreenBus();
        updateWaiting();
        startNextMainBus();
        startNextScreenBus();
        updateSubBuses();
        if (!Vars.headless) cutsceneUI.update();
    }

    private void updateMainBus() {
        if (mainBus == null) return;

        mainBus.update();
        if (mainBus.complete()) {
            mainBus = null;
            waiting = true;
            if (!Vars.headless) cutsceneUI.resetMain();
        }
    }

    private void updateScreenBus() {
        if (screenBus == null) return;

        screenBus.update();
        if (screenBus.complete()) {
            screenBus = null;
            if (!Vars.headless && waitingScreenBuses.isEmpty()) cutsceneUI.resetScreen();
        }
    }

    private void updateWaiting() {
        if (!waiting) return;

        waitTimer += Time.delta;
        if (waitTimer >= waitSpacing) {
            waitTimer = 0f;
            waiting = false;
        }
    }

    private void startNextMainBus() {
        if (mainBus == null && !waiting && !waitingBuses.isEmpty()) {
            mainBus = waitingBuses.removeLast();
        }
    }

    private void startNextScreenBus() {
        if (screenBus == null && !waitingScreenBuses.isEmpty()) {
            screenBus = waitingScreenBuses.removeLast();
        }
    }

    private void updateSubBuses() {
        for (int i = 0; i < subBuses.size; ) {
            ActionBus bus = subBuses.get(i);
            bus.update();
            if (bus.complete()) subBuses.remove(i);
            else i++;
        }
    }

    public void clear() {
        waiting = false;
        waitTimer = 0f;
        if (mainBus != null) mainBus.skip();
        mainBus = null;
        waitingBuses.clear();
        if (screenBus != null) screenBus.skip();
        screenBus = null;
        waitingScreenBuses.clear();
        for (ActionBus bus : subBuses) bus.skip();
        subBuses.clear();
        if (!Vars.headless) cutsceneUI.reset();
    }

    public void addMainActionBus(ActionBus bus) {
        if (bus == null) return;
        if (mainBus == null) mainBus = bus;
        else waitingBuses.addFirst(bus);
    }

    public void addSubActionBus(ActionBus bus) {
        if (bus != null) subBuses.add(bus);
    }

    public void addScreenActionBus(ActionBus bus) {
        if (bus == null) return;
        if (screenBus == null) screenBus = bus;
        else waitingScreenBuses.addFirst(bus);
    }
}
