package wh.content;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import wh.entities.*;
import wh.entities.event.*;

public class WHAutoTriggerSetup{
    public static void load(){
        PortableAutoEventTrigger.init();

        PortableAutoEventTrigger.registerTemplate(
        new PortableAutoEventTrigger.Trigger()
        .id("global-chaos")
        .allowModes(true, true, true, false)
        .allowNonSectorMaps(true)
        .rulesFilter(r -> true)
        .checkSpacing(300f)                 // 每5秒检查一次
        .spacing(30f * 60f, 10)             // 每30秒可触发一次（你自己改）
        .teamToSpawn(() -> Vars.state.rules.waveTeam)
        .minWave(0)
        .spawn(WHUnitTypes.air4, 2)
        .spawnShape(80f, 45f, 10f, 180f)    // range/warmup/delay
        .spawnerInvoker(WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .useFleetWarnHUD(true, 2.5f)
        .chatText("[scarlet]Enemy fleet incoming[]", 120f)
        .showTriggerMark(Color.valueOf("ff7b69"), 24f, 180f)
        );
    }

    public static void spawnByWHSpawnerAtEnemySpawn(PortableAutoEventTrigger.SpawnContext sctx){
        if(Vars.spawner == null || !Vars.state.hasSpawns()) return;
        var spawns = Vars.spawner.getSpawns();
        if(spawns == null || spawns.isEmpty()) return;

        Team enemy = Vars.state.rules.waveTeam;
        float cx = Vars.world.width() * Vars.tilesize * 0.5f;
        float cy = Vars.world.height() * Vars.tilesize * 0.5f;

        for(int i = 0; i < sctx.amount; i++){
            float delay = sctx.warmup + i * sctx.eachDelay;

            Time.run(delay, () -> {
                var spawn = spawns.random(); // 多个出生点随机
                float sx = spawn.getX();
                float sy = spawn.getY();

                float px = sx + Mathf.range(sctx.range); // 出生点附近
                float py = sy + Mathf.range(sctx.range);

                float rot = Angles.angle(px, py, cx, cy); // 朝向地图中心（你给的逻辑）

                Spawner sp = new Spawner()
                .init(sctx.type, enemy, new Vec2(px, py), rot, 12f, true)
                .setStatus(sctx.status, sctx.statusDuration)
                .setFlagToApply(sctx.flag);

                sp.add();
            });
        }
    }

}
