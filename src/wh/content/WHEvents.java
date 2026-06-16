package wh.content;

import arc.Events;
import mindustry.entities.abilities.Ability;
import mindustry.game.EventType.BuildingBulletDestroyEvent;
import mindustry.game.EventType.UnitBulletDestroyEvent;
import mindustry.game.EventType.UnitDamageEvent;
import mindustry.game.EventType.UnitDestroyEvent;
import mindustry.gen.*;
import wh.entities.abilities.AllyDeathListener;
import wh.entities.abilities.BulletKillListener;
import wh.entities.world.Psy.PsychicDeathHarvesterBlock;
import wh.entities.world.Psy.PsychicFrontlineNodeBlock;
import wh.gen.RevengeUnit;

public class WHEvents{
    public static void load(){
        registerBulletKillEvents();
        registerAllyDeathEvents();
        registerPsychicBattleEvents();
    }

    public static void registerBulletKillEvents(){
        Events.on(UnitBulletDestroyEvent.class, e -> {
            handleBulletKill(e.bullet, e.unit);
        });

        Events.on(BuildingBulletDestroyEvent.class, e -> {
            handleBulletKill(e.bullet, e.build);
        });
    }

    public static void handleBulletKill(Bullet bullet, Healthc target){
        if(bullet == null || target == null){
            return;
        }

        if(bullet.owner instanceof RevengeUnit u){
            if(target.maxHealth() > u.RECOVERY_HEALTH &&
            target instanceof Teamc teamc &&
            teamc.team() != u.team &&
            u.surroundBullets.size < u.MAX_BULLET){
                u.createBullet();
            }
        }

        if(bullet.owner instanceof Unit unit){
            for(Ability ability : unit.abilities){
                if(ability instanceof BulletKillListener listener){
                    listener.onBulletKill(unit, target, bullet);
                }
            }
        }
    }

    public static void registerAllyDeathEvents(){
        Events.on(UnitDestroyEvent.class, e -> {
            handleAllyDeath(e.unit);
            PsychicDeathHarvesterBlock.handleUnitDeath(e.unit);
            PsychicFrontlineNodeBlock.handleUnitDeath(e.unit);
        });
    }

    public static void registerPsychicBattleEvents() {
        Events.on(UnitDamageEvent.class, e -> {
            PsychicFrontlineNodeBlock.handleUnitDamaged(e.unit, e.bullet);
        });
    }

    public static void handleAllyDeath(Unit deadAlly){
        if(deadAlly == null){
            return;
        }

        Groups.unit.each(unit -> {
            if(unit == deadAlly || unit.dead || !unit.isValid() || unit.team != deadAlly.team){
                return;
            }

            for(Ability ability : unit.abilities){
                if(ability instanceof AllyDeathListener listener){
                    listener.onAllyDeath(unit, deadAlly);
                }
            }
        });
    }
}
