package wh.entities.abilities;

import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.world.meta.*;
import wh.content.*;

public class AccelerateReload extends Ability{
    public float reloadMultiplier = 1f;
    public float maxMultiplier = 2f;
    public float resetTime = 120;

    public float increaseTime = 300;
    public float decreaseTime = 60;

    protected float timer;

    public AccelerateReload(){

    }

    public AccelerateReload(float maxMultiplier, float resetTime){
        this.maxMultiplier = maxMultiplier;
        this.resetTime = resetTime;
    }

    public AccelerateReload(float increaseTime, float decreaseTime, float maxMultiplier, float resetTime){
        this.increaseTime = increaseTime;
        this.decreaseTime = decreaseTime;
        this.maxMultiplier = maxMultiplier;
        this.resetTime = resetTime;
    }

    @Override
    public void update(Unit unit){
        super.update(unit);

        float increment = 1;
        if(unit.isShooting){
            increment = Mathf.approachDelta(increment, maxMultiplier, reloadMultiplier / increaseTime);
        }else{
            timer += Time.delta;
        }

        if(timer >= resetTime && !unit.isShooting){
            increment = Mathf.approachDelta(increment, 1, reloadMultiplier / decreaseTime);
            if(increment == 1) timer = 0;
        }

        unit.reloadMultiplier *= Mathf.clamp(increment, 1, maxMultiplier);
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + WHStats.increaseWhenShooting.localized() + ": [white]+" + Strings.autoFixed(increaseTime * 60 * 100, 0) + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + WHStats.decreaseNotShooting.localized() + ": [white]-" + Strings.autoFixed(decreaseTime * 60 * 100, 0) + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + WHStats.maxBoostPercent.localized() + ": [white]" + Strings.autoFixed(maxMultiplier * 100, 0) + "%");
        t.row();
        t.add("[lightgray]" + new Stat("wh-reset-time", StatCat.function).localized() + ": [white]" + Strings.autoFixed(resetTime / 60f, 0) + StatUnit.perSecond.localized());
    }
}
