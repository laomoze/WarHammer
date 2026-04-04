package wh.entities.abilities;

import arc.*;
import arc.audio.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;

import static wh.core.WarHammerMod.name;

/** 子弹击杀目标后，为发射者回复生命值。 */
public class BulletKillHealAbility extends Ability implements BulletKillListener{
    public float healAmount = 200f;
    public float healPercent = 0.1f;
    public float minTargetMaxHealth = 0f;

    public Effect healEffect = Fx.healWaveDynamic;
    public Sound healSound = Sounds.none;

    public BulletKillHealAbility(){
    }

    public BulletKillHealAbility(float healAmount, float healPercent){
        this.healAmount = healAmount;
        this.healPercent = healPercent;
    }

    @Override
    public void onBulletKill(Unit unit, Healthc target, Bullet bullet){
        // 这里只处理“我方单位发出的子弹击杀了敌方目标”后的回复。
        if(unit == null || target == null || unit.dead()){
            return;
        }

        if(target.maxHealth() < minTargetMaxHealth){
            return;
        }

        if(target instanceof Teamc teamc && teamc.team() == unit.team){
            return;
        }

        float amount = healAmount + target.maxHealth() * healPercent;
        if(amount <= 0f){
            return;
        }

        // 回复直接加到单位身上，不改子弹本体，方便后续复用同一事件。
        unit.heal(amount);

        if(healEffect != Fx.none){
            healEffect.at(unit.x, unit.y, unit.hitSize, unit.team.color, unit);
        }

        if(healSound != Sounds.none){
            healSound.at(unit);
        }
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        if(healAmount > 0f){
            t.row();
            t.add(Core.bundle.format("stat.wh-kill-heal-flat", Strings.autoFixed(healAmount, 2)));
        }
        if(healPercent > 0f){
            t.row();
            t.add(Core.bundle.format("stat.wh-kill-heal-percent", Strings.autoFixed(healPercent * 100f, 2) + "%"));
        }
        if(minTargetMaxHealth > 0f){
            t.row();
            t.add(Core.bundle.format("stat.wh-kill-heal-target-threshold", Strings.autoFixed(minTargetMaxHealth, 0)));
        }
    }

    @Override
    public String localized(){
        return Core.bundle.get(getBundle());
    }

    @Override
    public String getBundle(){
        return "ability." + name("bullet-kill-heal-ability");
    }
}
