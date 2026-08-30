package wh.entities.event.logic.actionLogic;

import arc.math.Mathf;
import arc.util.Time;

public abstract class Action implements TimeQueue.Timed {
    public float lifeTimer;
    public float duration;
    public boolean cancelled;

    @Override
    public void update() {
        if (lifeTimer < duration) {
            lifeTimer += Time.delta;
            act();
        }
    }

    @Override
    public boolean complete() {
        return lifeTimer >= duration;
    }

    @Override
    public void begin() {
    }

    @Override
    public void end() {
    }

    public void act() {
    }

    public float progress() {
        return duration <= 0f ? 1f : Mathf.clamp(lifeTimer / duration);
    }

    @Override
    public void skip() {
        cancelled = true;
        lifeTimer = duration;
    }
}
