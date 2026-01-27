package wh.content;

import arc.*;
import mindustry.game.EventType.*;
import wh.entities.world.entities.*;

public class WHEvents{
    public static void load(){
        RevengeUnitKillRecovery();
    }

    public static void RevengeUnitKillRecovery(){
        Events.on(UnitBulletDestroyEvent.class, e -> {
            if(e.bullet.owner instanceof RevengeUnit u){
                if(e.unit.type.health > u.RECOVERY_HEALTH && e.unit.team != u.team && u.surroundBullets.size < u.MAX_BULLET){
                    u.createBullet();
                }
            }
        });

        Events.on(BuildingBulletDestroyEvent.class, e -> {
            if(e.bullet.owner instanceof RevengeUnit u){
                if(e.build.block.health > u.RECOVERY_HEALTH && e.build.team != u.team && u.surroundBullets.size < u.MAX_BULLET){
                    u.createBullet();
                }
            }
        });
    }
}
