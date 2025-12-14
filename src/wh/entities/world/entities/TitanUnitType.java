package wh.entities.world.entities;

import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;

public class TitanUnitType extends UnitType{
    public TitanUnitType(String name){
        super(name);
        constructor = TitanUnit::new;
    }

    public float longAxis = 500 / 4f, minorAxis = 400 / 4f;
    public float radius = 60f;
    public float regen = 1200 / 60f, max = 15000, cooldown = 60 * 60f, restartRatio = 0.4f;

    public float reflectChance = 0.15f;
    public boolean ignoreBulletAbsorb = false;

    public boolean percentRegen = false;
    public float percentRegenAmount = 0.05f;

    public float damageMax = 1300;
    public float shieldDamageMaxPer = 2000;
    public float accumulateDamage = 8000;
    public float fullAbsorbTime = 240;

    public float regenThreshold = 0.25f;

    public Effect absorbEffect = Fx.absorb;
    public Effect reflectEffect = Fx.dynamicExplosion;

    @Override
    public void init(){
        super.init();
    }

    @Override
    public Unit create(Team team){
        Unit unit = constructor.get();
        unit.team = team;
        unit.setType(this);
        if(unit.controller() instanceof CommandAI command && defaultCommand != null){
            command.command = defaultCommand;
        }
        if(unit instanceof TitanUnit t){
            t.forceShield = max;
        }
        for(var ability : unit.abilities){
            ability.created(unit);
        }
        unit.ammo = ammoCapacity; //fill up on ammo upon creation
        unit.elevation = flying ? 1f : 0;
        unit.heal();
        if(unit instanceof TimedKillc u){
            u.lifetime(lifetime);
        }
        return unit;
    }
}
