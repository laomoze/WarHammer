package wh.entities.abilities;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.content.*;

import static wh.core.WarHammerMod.name;

/** 在修复场基础上补充自定义特效，并支持单位自身的缓慢自愈。 */
public class AdaptedHealAbility extends RepairFieldAbility{
    public AdaptedHealAbility(float amount, float reload, float range, Color applyColor){
        this(amount, reload, range);
        this.applyColor = applyColor;
    }

    public AdaptedHealAbility(float amount, float reload, float range){
        super(amount, reload, range);

        healEffect = WHFx.healReceiveCircle;
        activeEffect = WHFx.healSendCircle;
    }

    public AdaptedHealAbility modify(Cons<AdaptedHealAbility> modifier){
        modifier.get(this);
        return this;
    }

    // 修复特效的主色。
    public Color applyColor = Pal.heal;

    // 为 false 时，自愈会随 healthMultiplier 一起缩放。
    public boolean ignoreHealthMultiplier = true;
    // 每次自愈按最大生命值的比例恢复。
    public float selfHealAmount = 0.0005f;
    // 小于 0 表示关闭自愈等待机制。
    public float selfHealReloadTime = -1;

    // 用上一帧血量判断是否处于“最近没再挨打”的状态。
    protected float lastHealth = 0;
    protected float selfHealReload = 0;

    public void update(Unit unit) {
        // 这里的 data 用作修复场自身的冷却进度。
        data += Time.delta;

        if(data >= reload){
            wasHealed = false;

            Units.nearby(unit.team, unit.x, unit.y, range, other -> {
                if(other.damaged()){
                    healEffect.at(other.x, other.y, 0, applyColor, parentizeEffects ? other : null);
                    wasHealed = true;
                }
                other.heal(amount);
            });

            if(wasHealed){
                activeEffect.at(unit.x, unit.y, range, applyColor);
            }

            data = 0f;
        }

        if(selfHealReloadTime < 0)return;

        // 只有在没继续掉血时才累积自愈等待时间。
        if(lastHealth <= unit.health && unit.damaged()){
            selfHealReload += Time.delta;

            if(selfHealReload > selfHealReloadTime){
                unit.healFract(selfHealAmount / 60 * (ignoreHealthMultiplier ? 1 : 1 / unit.healthMultiplier));
            }
        }else{
            selfHealReload = 0;
        }

        lastHealth = unit.health;
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        if(selfHealReloadTime < 0)return;
        t.row();
        t.add(Core.bundle.format("stat.wh-self-heal-reload-time", Strings.autoFixed(selfHealReloadTime / 60, 2)));
        t.row();
        t.add(Core.bundle.format("stat.self-heal-amount", Strings.autoFixed(selfHealAmount, 2)));

    }

    @Override
    public String localized() {
        return Core.bundle.format("ability." + name("adapted-heal-ability"));
    }
}
