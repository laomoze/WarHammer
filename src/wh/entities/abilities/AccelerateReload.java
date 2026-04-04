package wh.entities.abilities;

import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.world.meta.*;
import wh.content.*;

/** 持续射击时逐步提高装填倍率，停火足够久后再缓慢回落。 */
public class AccelerateReload extends Ability{

    // 最终可达到的装填倍率上限。
    public float maxMultiplier = 2f;
    // 停火后维持满增益的时间。
    public float resetTime = 120;

    // 射击时升到上限需要的时间。
    public float increaseTime = 300;
    // 增益开始回落后，退回 1 倍所需时间。
    public float decreaseTime = 60;
    public boolean liner = true;

    // 记录停火后的维持时间。
    protected float timer = 0;
    // 当前累积出的装填倍率。
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
            // 射击中持续叠加增益。
            if(unit.isShooting){
                reloadMultiplier = Mathf.approachDelta(re, maxMultiplier, (liner ? 1 : re) / increaseTime);
                timer = 0;
                // 到达上限后开始计入“维持时间”。
            }else if(reloadMultiplier >= maxMultiplier){
                timer += Time.delta;
            }
        }else{
            // 维持时间结束后，增益才开始衰减。
            reloadMultiplier = Mathf.approachDelta(reloadMultiplier, 1, (liner ? 1 : re) / decreaseTime);
            if(Mathf.equal(reloadMultiplier, 1, 0.01f)){
                timer = 0;
            }
        }

        unit.reloadMultiplier *= re;
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + WHStats.increaseWhenShooting.localized() + ": [white]+" + Strings.autoFixed(60f * 100f / increaseTime, 0) + "%" + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + WHStats.decreaseNotShooting.localized() + ": [white]-" + Strings.autoFixed(60f * 100f / decreaseTime, 0) + "%" + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + WHStats.maxBoostPercent.localized() + ": [white]" + Strings.autoFixed(maxMultiplier * 100, 0) + "%");
        t.row();
        t.add("[lightgray]" + new Stat("wh-maintain-time", StatCat.function).localized() + ": [white]" + Strings.autoFixed(resetTime / 60f, 0) + " " + StatUnit.seconds.localized());
    }
}
