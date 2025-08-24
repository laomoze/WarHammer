package wh.entities.abilities;

import arc.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;

import static wh.core.WarHammerMod.name;

public class DodgeAbility extends Ability{
    // 闪避冷却时间(秒)
    public float cooldown = 60f;
    public float chance = 0.1f;
    public Effect dodgeEffect = Fx.none;

    protected float timer = 0f;
    protected boolean dodging = false;


    @Override
    public void update(Unit unit){
        super.update(unit);
        if(timer <= cooldown)dodging = false;
        if(!dodging){
            timer += Time.delta;
        }
        if(timer > cooldown){
            Groups.bullet.intersect(unit.x - (unit.hitSize + 8), unit.y - (unit.hitSize + 8), unit.hitSize * 2, unit.hitSize * 2, bullet -> {
                if(bullet.team != unit.team && bullet.type.collides &&
                bullet.type.damage > 0 && bullet.within(unit, 10f)){
                    if(Mathf.random() < chance){
                        bullet.collides(unit);
                        bullet.remove();
                        dodging = true;
                        dodgeEffect.at(unit.x, unit.y, unit.rotation,DodgeAbilityData.size(unit, bullet.type.damage));
                        timer = 0f;
                    }
                }
            });
        }
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);
    }
    public static class DodgeAbilityData{
        public Unit unit;
        public float damage;
        public static DodgeAbilityData size(Unit unit, float damage) {
            DodgeAbilityData data = new DodgeAbilityData();
            data.unit = unit;
            data.damage = damage;
            return data;
        }
    }

    @Override
    public String localized() {
        return Core.bundle.format("ability." + name("better-arc-field-ability"));
    }

}
