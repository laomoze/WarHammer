package wh.entities.world.entities;

import arc.math.*;
import arc.struct.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import wh.content.*;
import wh.entities.world.drawer.part.*;
import wh.graphics.*;

public class SuperHeavyUnitType extends WHUnitType{
    public boolean immuniseAll = true;
    public SuperHeavyUnitType(String name){
        super(name);

        outlineRadius = 3;
        outlineColor = WHPal.Outline;

        healColor = WHPal.WHYellow;
        lightColor = WHPal.WHYellow;

        ammoType = new ItemAmmoType(WHItems.ceramite);
    }

    @Override
    public void setStats() {
        super.setStats();
       /* if (damageMultiplier < 1f) {
            stats.add( new Stat("wh-damage-reduction"), bundle.format("bar.wh-damage-reduction", Strings.autoFixed((1f - damageMultiplier) * 100, 2)));
        }*/
    }


    public void addEngine(float x, float y, float relativeRot, float rad, boolean flipAdd) {
        if (flipAdd) {
            for (int i : Mathf.signs) {
                engines.add(new AncientEngine(x * i, y, rad, -90 + relativeRot * i, 0));
                engines.add(new AncientEngine(x * i, y, rad * 1.85f, -90 + relativeRot * i, Mathf.random(2f)).a(0.3f));
            }
        } else {
            engines.add(new AncientEngine(x, y, rad, -90 + relativeRot, 0));
            engines.add(new AncientEngine(x, y, rad * 1.85f, -90 + relativeRot, Mathf.random(2f)).a(0.3f));
        }
    }


    public void init(){
        super.init();

        if(immuniseAll){
            immunise(this);
        }}
    public static Seq<StatusEffect> statuses;

    public static void immunise(UnitType type){
        if(statuses == null){
            statuses = Vars.content.statusEffects().copy();
            statuses.retainAll(s -> s.disarm || s.damage > 0 || s.healthMultiplier * s.reloadMultiplier * s.buildSpeedMultiplier * s.speedMultiplier < 1);

            statuses.remove(StatusEffects.overclock);
            statuses.remove(StatusEffects.overdrive);
            statuses.remove(WHStatusEffects.assault);
            statuses.remove(WHStatusEffects.bless);
            statuses.remove(WHStatusEffects.plasma);
            statuses.add(StatusEffects.wet);
            /* statuses.add(StatusEffects.unmoving);*/
        }
        type.immunities.addAll(statuses);
    }
}
