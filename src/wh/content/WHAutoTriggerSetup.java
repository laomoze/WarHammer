package wh.content;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import wh.entities.*;
import wh.entities.event.*;
import wh.entities.event.PortableAutoEventTrigger.*;

public class WHAutoTriggerSetup{
    /** 调试开关：开启后放宽触发条件，便于本地测试。 */
    public static boolean debugAnyMode = false;

    public static void load(){
        PortableAutoEventTrigger.init();

        PortableAutoEventTrigger.clearTemplates();
        applyDebugMode(debugAnyMode);
        /* registerTemplates();*/
    }

    private static void registerTemplates(){
        registerGlobalChaosTrigger();
    }

    private static void registerGlobalChaosTrigger(){
        Trigger globalChaos = new Trigger()
        .id("test1")
        .allowModes(true, true, false, false)
        .allowNonSectorMaps(true)
        .rulesFilter(r -> false)
        .requireEnemySpawnPoint(false)
        .checkSpacing(300f)
        .spacing(60 * 60f, 100)
        .teamToSpawn(() -> Vars.state.rules.waveTeam)
        .minWave(0)
        .spawn(WHUnitTypes.airA5, 2, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.airA4, 2, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.airA3, 5, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.airA2, 10, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.tankA1, 2, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.tankB1, 2, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        // Fallback: even if per-unit invoker mappings are lost after load, keep custom spawn path.
        .spawnerInvoker(WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawnShape(200, 180, 90, 180f)
        .useFleetWarnHUD(true, PortableAutoEventTrigger.FleetWarnHudMode.centered, 3)
        .chatText("[scarlet]Enemy fleet incoming[]", 120f);

        PortableAutoEventTrigger.registerTemplate(globalChaos);

        Trigger globalChaos2 = new Trigger()
        .id("test2")
        .allowModes(true, true, false, false)
        .allowNonSectorMaps(true)
        .rulesFilter(r -> r != null && !r.infiniteResources && r.mode() != Gamemode.sandbox && r.mode() != Gamemode.pvp)
        .checkSpacing(300f)
        .spacing(60 * 60f, 100)
        .teamToSpawn(() -> Vars.state.rules.waveTeam)
        .minWave(0)
        .spawn(WHUnitTypes.mechaS6, 1, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.Mecha6, 1, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.tankA2, 3, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.tankA1, 3, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawn(WHUnitTypes.tankB1, 3, WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        // Fallback: even if per-unit invoker mappings are lost after load, keep custom spawn path.
        .spawnerInvoker(WHAutoTriggerSetup::spawnByWHSpawnerAtEnemySpawn)
        .spawnShape(200, 180, 90, 180f)
        .useFleetWarnHUD(true, FleetWarnHudMode.legacy, 3)
        .chatText("[scarlet]Enemy fleet incoming[]", 120f);

        /* PortableAutoEventTrigger.registerTemplate(globalChaos2);*/
    }

    /** One-click cleanup for persisted Trigger entities from old saves. */
    public static int purgeLegacyTriggerEntities(){
        int removed = PortableAutoEventTrigger.purgeLegacyTriggerEntities();
        Log.info("[WH][AutoTrigger] purged @ persisted trigger entity(ies)", removed);
        return removed;
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
        if(sctx == null || sctx.type == null) return;
        if(Vars.spawner == null || !Vars.state.hasSpawns()){
            spawnAroundCenter(sctx);
            return;
        }

        var spawns = Vars.spawner.getSpawns();
        if(spawns == null || spawns.isEmpty()){
            spawnAroundCenter(sctx);
            return;
        }
        spawnAtEnemySpawns(sctx);
    }

    private static void spawnAtEnemySpawns(PortableAutoEventTrigger.SpawnContext sctx){
        var spawns = Vars.spawner.getSpawns();
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
                .setShieldToApply(sctx.shield)
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
                .setShieldToApply(sctx.shield)
                .setStatus(sctx.status, sctx.statusDuration)
                .setFlagToApply(sctx.flag);

                sp.add();
            });
        }
    }
}
