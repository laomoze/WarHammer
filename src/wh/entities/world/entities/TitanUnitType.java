package wh.entities.world.entities;

import mindustry.ai.types.CommandAI;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.gen.TimedKillc;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import wh.gen.TitanUnit;

public class TitanUnitType extends UnitType{
    public TitanUnitType(String name){
        super(name);
        constructor = TitanUnit::new;
    }

    public float longAxis = 500 / 4f, minorAxis = 400 / 4f;
    public float radius = 60f;
    public float regen = 1050 / 60f, max = 13000, cooldown = 60 * 60f, restartRatio = 0.4f;

    public float reflectChance = 0.15f;
    public boolean shader = true;
    public boolean ignoreBulletAbsorb = false;

    public boolean percentRegen = false;
    public float percentRegenAmount = 0.05f;

    public float damageMax = 2000;
    public float shieldDamageMaxPer = 2500;
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
        unit.elevation = flying ? 1f : 0;
        unit.heal();
        if(unit instanceof TimedKillc u){
            u.lifetime(lifetime);
        }
        return unit;
    }
}
