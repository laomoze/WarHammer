package wh.entities.abilities;

import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.world.meta.*;
import wh.content.*;

public class AccelerateReload extends Ability{

    public float maxMultiplier = 2f;
    public float resetTime = 120;

    public float increaseTime = 300;
    public float decreaseTime = 60;
    public boolean liner = true;

    protected float timer = 0;
    protected float reloadMultiplier = 1f;

    public AccelerateReload(){

    }

   /* @Override
    public void displayBars(Unit unit, Table bars){
        bars.add(new Bar("[lightgray]" + WHStats.maxBoostPercent.localized(), Pal.accent, () -> reloadMultiplier / maxMultiplier)).row();
    }*/

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

        float re = Mathf.clamp(reloadMultiplier, 1, maxMultiplier);
        if(timer <= resetTime){
            if(unit.isShooting){
                reloadMultiplier = Mathf.approachDelta(re, maxMultiplier, (liner ? 1 : re) / increaseTime);
                timer = 0;
            }else if(reloadMultiplier >= maxMultiplier){
                timer += Time.delta;
            }
        }else{
            reloadMultiplier = Mathf.approachDelta(reloadMultiplier, 1, (liner ? 1 : re) / decreaseTime);
            if(Mathf.equal(reloadMultiplier, 1, 0.01f)){
                timer = 0;
            }
        }

        unit.reloadMultiplier *= re;
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
