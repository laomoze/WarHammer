package wh.content;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import wh.entities.*;
import wh.entities.event.*;

public class WHAutoTriggerSetup{
    /** Set true for dev testing: bypass mode/filter checks and spawn fallback without enemy spawn points. */
    public static boolean debugAnyMode = false;

    public static void load(){
        PortableAutoEventTrigger.init();
        WHAutoTriggerLogicBridge.init();
        applyDebugMode(debugAnyMode);
        registerTemplates();
    }

    private static void registerTemplates(){
        registerGlobalChaosTrigger();
    }

    private static void registerGlobalChaosTrigger(){
        Trigger globalChaos = new Trigger()
        .id("global-chaos")
        .allowModes(true, true, false, false)
        .allowNonSectorMaps(true)
        .rulesFilter(r -> r != null && !r.infiniteResources && r.mode() != Gamemode.sandbox && r.mode() != Gamemode.pvp)
        .checkSpacing(300f)
        .spacing(60 * 60f, 100)
        .teamToSpawn(() -> Vars.state.rules.waveTeam)
        .minWave(0)
        .spawn(WHUnitTypes.air5, 2, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.air4, 2, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.air3, 5, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.air2, 10, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawnShape(200, 180, 90, 180f)
        .useFleetWarnHUD(true, PortableAutoEventTrigger.FleetWarnHudMode.centered, 3)
        .chatText("[scarlet]Enemy fleet incoming[]", 120f);

        PortableAutoEventTrigger.registerTemplate(globalChaos);
    }

    public static void applyDebugMode(boolean enabled){
        debugAnyMode = enabled;
        PortableAutoEventTrigger.enableDebugAll(enabled);
        Log.info("[WH][AutoTrigger] debugAnyMode=@", enabled);
    }

    /** Force install templates and fire once immediately on the current map. */
    public static int debugInstallAndFireNow(){
        applyDebugMode(true);
        int fired = PortableAutoEventTrigger.debugInstallAndFireNow();
        Log.info("[WH][AutoTrigger] debugInstallAndFireNow fired @ trigger(s)", fired);
        return fired;
    }

    public static void spawnByWHSpawnerAtEnemySpawn(PortableAutoEventTrigger.SpawnContext sctx){
        if(Vars.spawner == null || !Vars.state.hasSpawns()){
            if(debugAnyMode || PortableAutoEventTrigger.debugBypassMeet){
                spawnAroundCenter(sctx);
            }
            return;
        }

        var spawns = Vars.spawner.getSpawns();
        if(spawns == null || spawns.isEmpty()){
            if(debugAnyMode || PortableAutoEventTrigger.debugBypassMeet){
                spawnAroundCenter(sctx);
            }
            return;
        }

        Team enemy = Vars.state.rules.waveTeam;
        float cx = Vars.world.width() * Vars.tilesize * 0.5f;
        float cy = Vars.world.height() * Vars.tilesize * 0.5f;

        for(int i = 0; i < sctx.amount; i++){
            float delay = sctx.warmup + i * sctx.eachDelay;

            Time.run(delay, () -> {
                var spawn = spawns.random();
                float sx = spawn.getX();
                float sy = spawn.getY();

                float px = sx + Mathf.range(sctx.range);
                float py = sy + Mathf.range(sctx.range);

                float rot = Angles.angle(px, py, cx, cy);

                Spawner sp = new Spawner()
                .init(sctx.type, enemy, new Vec2(px, py), rot, sctx.warmup, false)
                .setStatus(sctx.status, sctx.statusDuration)
                .setFlagToApply(sctx.flag);

                sp.add();
            });
        }
    }

    private static void spawnAroundCenter(PortableAutoEventTrigger.SpawnContext sctx){
        Team enemy = Vars.state.rules.waveTeam;
        float cx = Vars.world.width() * Vars.tilesize * 0.5f;
        float cy = Vars.world.height() * Vars.tilesize * 0.5f;

        for(int i = 0; i < sctx.amount; i++){
            float delay = sctx.warmup + i * sctx.eachDelay;

            Time.run(delay, () -> {
                float radius = Math.max(8f, sctx.range);
                float px = cx + Mathf.range(radius);
                float py = cy + Mathf.range(radius);
                float rot = Angles.angle(px, py, cx, cy);

                Spawner sp = new Spawner()
                .init(sctx.type, enemy, new Vec2(px, py), rot, 12f, true)
                .setStatus(sctx.status, sctx.statusDuration)
                .setFlagToApply(sctx.flag);

                sp.add();
            });
        }
    }
}
