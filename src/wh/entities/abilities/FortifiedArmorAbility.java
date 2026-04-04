package wh.entities.abilities;

import arc.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

import static wh.core.WarHammerMod.name;

/** 提供额外生命层与强化装甲，并在满足条件后重新获得这层防护。 */
public class FortifiedArmorAbility extends Ability{
    // 额外生命层占基础生命的比例。
    public float maxHealthBonus = 0.25f;
    // 额外生命层存在时的装甲倍率。
    public float armorMultiplier = 2f;
    // 血量低于该阈值后才允许后续恢复装甲层。
    public float restoreTrigger = 0.2f;
    // 回到该血量比例后重新获得装甲层。
    public float restoreHealth = 0.99f;
    public float interval = 15f;

    public Effect armorBreakEffect = Fx.unitShieldBreak;
    public Effect armorRestoreEffect = Fx.shieldApply;

    protected transient Interval timer = new Interval();
    protected transient boolean initialized;
    // armored 表示额外生命层是否仍在，canRestore 表示是否允许重新获得这层防护。
    protected transient boolean armored;
    protected transient boolean canRestore;
    protected transient float lastHealth = -1f;

    public FortifiedArmorAbility(){
    }

    public FortifiedArmorAbility(float maxHealthBonus, float armorMultiplier){
        this.maxHealthBonus = maxHealthBonus;
        this.armorMultiplier = armorMultiplier;
    }

    @Override
    public void created(Unit unit){
        // 单位创建时直接获得完整额外生命层和强化装甲。
        initialize(unit, true);
        applyArmor(unit);
    }

    @Override
    public void update(Unit unit){
        if(!initialized){
            initialize(unit, false);
        }

        if(timer.get(0, interval)){
            updateState(unit);
        }

        // 额外生命层存在时不允许回血把这层补回来。
        preventBonusLayerHealing(unit);
        applyArmor(unit);
        lastHealth = unit.health;
    }

    @Override
    public void displayBars(Unit unit, Table bars){
        // 这一条只显示“额外装甲层”本身的量，不拿恢复进度混在一起。
        bars.add(new Bar(
        () -> Core.bundle.format("bar.wh-fortified-armor-layer",
        Mathf.round(currentBonusHealth(unit)),
        Mathf.round(bonusHealth(unit))),
        () -> Pal.shield,
        () -> layerFraction(unit)
        )).row();

        // 装甲层失去后，单独再给一条恢复进度，避免和装甲层数值混淆。
        if(!armored && canRestore){
            bars.add(new Bar(
            () -> Core.bundle.format("bar.wh-fortified-armor-recover", Mathf.round(restoreFraction(unit) * 100f)),
            () -> Pal.heal,
            () -> restoreFraction(unit)
            )).row();
        }
    }

    protected void initialize(Unit unit, boolean fillHealth){
        float baseHealth = baseMaxHealth(unit);

        if(fillHealth){
            armored = true;
            canRestore = false;
            unit.maxHealth = totalMaxHealth(unit);
            unit.health = unit.maxHealth;
            applyArmor(unit);
            lastHealth = unit.health;
            initialized = true;
            return;
        }

        armored = unit.maxHealth > baseHealth + 0.001f || unit.health > baseHealth + 0.001f;
        canRestore = !armored && unit.health / Math.max(baseHealth, 0.001f) <= restoreTrigger;
        syncMaxHealth(unit);
        lastHealth = unit.health;
        initialized = true;
    }

    protected void updateState(Unit unit){
        boolean wasArmored = armored;

        // 额外生命层打空后，最大生命立即恢复成原值。
        if(armored && unit.health <= baseMaxHealth(unit) + 0.001f){
            setArmored(unit, false);
        }

        // 装甲丢失后，血量跌到阈值以下才允许后续恢复。
        if(unit.healthf() <= restoreTrigger){
            canRestore = true;
        }

        // 恢复到接近满血时，重新获得额外生命层和强化装甲。
        if(!armored && canRestore && unit.healthf() >= restoreHealth){
            setArmored(unit, true);
            canRestore = false;
        }

        if(wasArmored && !armored && armorBreakEffect != Fx.none){
            armorBreakEffect.at(unit.x, unit.y, unit.hitSize, unit.team.color, unit);
        }

        if(!wasArmored && armored && armorRestoreEffect != Fx.none){
            armorRestoreEffect.at(unit.x, unit.y, unit.hitSize, unit.team.color, unit);
        }
    }

    protected void preventBonusLayerHealing(Unit unit){
        float baseHealth = baseMaxHealth(unit);
        boolean bonusLayerActive = armored && unit.maxHealth > baseHealth + 0.001f && unit.health > baseHealth + 0.001f;

        if(bonusLayerActive && lastHealth >= 0f && unit.health > lastHealth + 0.001f){
            unit.health = lastHealth;
        }
    }

    protected void applyArmor(Unit unit){
        float armorBase = unit.armorOverride >= 0f ? unit.armorOverride : unit.type.armor;
        if(armored && armorBase > 0f){
            unit.armorOverride = armorBase * armorMultiplier;
        }
    }

    protected float baseMaxHealth(Unit unit){
        // 原始生命上限始终使用单位类型本身的数据。
        return unit.type.health;
    }

    protected float totalMaxHealth(Unit unit){
        // 强化装甲开启时才拥有额外最大生命。
        return baseMaxHealth(unit) * (1f + maxHealthBonus);
    }

    protected float bonusHealth(Unit unit){
        return totalMaxHealth(unit) - baseMaxHealth(unit);
    }

    protected float currentBonusHealth(Unit unit){
        return Mathf.clamp(unit.health - baseMaxHealth(unit), 0f, bonusHealth(unit));
    }

    protected float layerFraction(Unit unit){
        float extra = bonusHealth(unit);
        if(extra <= 0f){
            return armored ? 1f : 0f;
        }
        return Mathf.clamp(currentBonusHealth(unit) / extra);
    }

    protected float restoreFraction(Unit unit){
        if(restoreHealth <= 0f){
            return 1f;
        }
        return Mathf.clamp(unit.healthf() / restoreHealth);
    }

    protected void setArmored(Unit unit, boolean value){
        if(armored == value){
            syncMaxHealth(unit);
            unit.heal();
            return;
        }

        armored = value;

        if(armored){
            unit.maxHealth = totalMaxHealth(unit);
            unit.health = Math.min(unit.maxHealth, unit.health + bonusHealth(unit));
        }else{
            unit.maxHealth = baseMaxHealth(unit);
            unit.health = Math.min(unit.health, unit.maxHealth);
        }

        lastHealth = unit.health;
    }

    protected void syncMaxHealth(Unit unit){
        float target = armored ? totalMaxHealth(unit) : baseMaxHealth(unit);
        if(Math.abs(unit.maxHealth - target) > 0.001f){
            unit.maxHealth = target;
            unit.health = Math.min(unit.health, unit.maxHealth);
        }
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(Core.bundle.format("stat.wh-max-health-bonus", Strings.autoFixed(maxHealthBonus * 100f, 2) + "%"));
        t.row();
        t.add(Core.bundle.format("stat.wh-armor-multiplier", Strings.autoFixed(armorMultiplier, 2)));
        t.row();
        t.add(Core.bundle.format("stat.wh-restore-health-threshold", Strings.autoFixed(restoreTrigger * 100f, 2) + "%"));
      /*  t.row();
        t.add(Core.bundle.format("stat.wh-restore-health-required", Strings.autoFixed(restoreHealth * 100f, 2) + "%"));*/
    }

    @Override
    public FortifiedArmorAbility copy(){
        FortifiedArmorAbility out = (FortifiedArmorAbility)super.copy();
        out.timer = new Interval();
        out.initialized = false;
        out.armored = false;
        out.canRestore = false;
        out.lastHealth = -1f;
        return out;
    }

    @Override
    public String localized(){
        return Core.bundle.get(getBundle());
    }

    @Override
    public String getBundle(){
        return "ability." + name("fortified-armor-ability");
    }
}
