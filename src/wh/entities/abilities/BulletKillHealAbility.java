package wh.entities.abilities;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.g2d.Lines;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.*;
import wh.content.WHStats;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static wh.core.WarHammerMod.name;

/** 子弹击杀目标后，为发射者回复生命值。 */
public class BulletKillHealAbility extends Ability implements BulletKillListener{
    public float healAmount = 200f;
    public float healPercent = 0.05f;
    public float minTargetMaxHealth = 0f;

    public Effect healEffect = new Effect(35, e -> {
        color(e.color);
        stroke(e.fout() * 3f);
        Lines.circle(e.x, e.y, 4f + e.finpow() * e.rotation);
    });
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

        unit.heal(amount);

        if(healEffect != Fx.none){
            healEffect.at(unit.x, unit.y, unit.hitSize * 2f, unit.team.color, unit);
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
            t.add(WHStats.format("wh-kill-heal-flat", Strings.autoFixed(healAmount, 2)));
        }
        if(healPercent > 0f){
            t.row();
            t.add(WHStats.format("wh-kill-heal-percent", Strings.autoFixed(healPercent * 100f, 2) + "%"));
        }
        if(minTargetMaxHealth > 0f){
            t.row();
            t.add(WHStats.format("wh-kill-heal-target-threshold", Strings.autoFixed(minTargetMaxHealth, 0)));
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
