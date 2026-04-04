package wh.content;

import arc.*;
import mindustry.entities.abilities.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import wh.entities.abilities.*;
import wh.gen.*;

public class WHEvents{
    public static void load(){
        registerBulletKillEvents();
        registerAllyDeathEvents();
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
