package wh.pipelinePlanet.karvex;

import arc.math.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.world.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

import static mindustry.Vars.*;

/**
 * Finalizes core placement, spawn markers and rules.
 */
public class KarvexFinalizePass implements GenPass{
    private final Schematic preferredLoadout;

    public KarvexFinalizePass(){
        this(null);
    }

    public KarvexFinalizePass(Schematic preferredLoadout){
        this.preferredLoadout = preferredLoadout;
    }

    @Override
    public String name(){
        return "KarvexFinalizePass";
    }

    @Override
    public void apply(GenContext ctx){
        if(ctx.spawnRoom == null) return;

        sanitizeCoreZones(ctx);
        placePlayerCore(ctx.spawnRoom.x, ctx.spawnRoom.y);
        restoreEnemySpawns(ctx);
        applyRules(ctx);
    }

    private void sanitizeCoreZones(GenContext ctx){
        sanitizeArea(ctx, ctx.spawnRoom.x, ctx.spawnRoom.y, 15);
        for(int i = 0; i < ctx.enemyRooms.size; i++){
            RoomAnchor enemy = ctx.enemyRooms.get(i);
            sanitizeArea(ctx, enemy.x, enemy.y, 8);
        }
    }

    private void sanitizeArea(GenContext ctx, int cx, int cy, int radius){
        int r2 = radius * radius;

        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;

                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null) continue;

                tile.setBlock(Blocks.air);
                tile.setOverlay(Blocks.air);

                if(!tile.floor().hasSurface() || tile.floor().isLiquid){
                    tile.setFloor(findNearbyLand(ctx, tile.x, tile.y, 10).asFloor());
                }
            }
        }
    }

    private void placePlayerCore(int x, int y){
        Team team = defaultTeam();

        if(tryPlacePreferredLoadout(x, y, team)){
            addLaunchResources(x, y);
            return;
        }

        if(tryPlaceLaunchLoadout(x, y)) return;

        if(tryPlaceSchematic(Loadouts.basicShard, x, y, team)){
            addLaunchResources(x, y);
        }
    }

    private void restoreEnemySpawns(GenContext ctx){
        for(int i = 0; i < ctx.enemyRooms.size; i++){
            RoomAnchor enemy = ctx.enemyRooms.get(i);
            Tile tile = ctx.tiles.get(enemy.x, enemy.y);
            if(tile == null) continue;

            tile.setBlock(Blocks.air);
            if(tile.floor().isLiquid || !tile.floor().hasSurface()){
                tile.setFloor(findNearbyLand(ctx, tile.x, tile.y, 8).asFloor());
            }
            tile.setOverlay(Blocks.spawn);
        }
    }

    private void applyRules(GenContext ctx){
        if(state == null || state.rules == null) return;

        state.rules.env = ctx.sector.planet.defaultEnv;
        state.rules.placeRangeCheck = true;
        state.rules.enemyCoreBuildRadius = 600f;

        float difficulty = Mathf.clamp(ctx.sector.threat);
        if(ctx.sector.hasEnemyBase()){
            state.rules.attackMode = true;
            state.rules.waves = true;
            state.rules.showSpawns = true;
            state.rules.spawns = Waves.generate(difficulty, new Rand(ctx.sector.id), true, true, false);
        }else{
            state.rules.attackMode = false;
            state.rules.waves = true;
            state.rules.winWave = 10 + 5 * (int)Math.max(difficulty * 10f, 1f);
            state.rules.waveSpacing = Mathf.lerp(60f * 65f * 2f, 60f * 60f, Math.max(difficulty - 0.4f, 0f));
        }
    }

    private Team defaultTeam(){
        return state != null && state.rules != null && state.rules.defaultTeam != null ? state.rules.defaultTeam : Team.sharded;
    }

    private boolean tryPlacePreferredLoadout(int x, int y, Team team){
        Schematic loadout = preferredLoadout;
        if(loadout == null && universe != null){
            loadout = universe.getLastLoadout();
        }
        return tryPlaceSchematic(loadout, x, y, team);
    }

    private boolean tryPlaceLaunchLoadout(int x, int y){
        try{
            Schematics.placeLaunchLoadout(x, y);
            return true;
        }catch(Throwable ignored){
            return false;
        }
    }

    private boolean tryPlaceSchematic(Schematic loadout, int x, int y, Team team){
        if(loadout == null) return false;
        try{
            Schematics.placeLoadout(loadout, x, y, team);
            return true;
        }catch(Throwable ignored){
            return false;
        }
    }

    private void addLaunchResources(int x, int y){
        if(universe == null || world == null) return;

        Tile center = world.tile(x, y);
        if(center != null && center.build != null){
            center.build.items.add(universe.getLaunchResources());
        }
    }

    private Block findNearbyLand(GenContext ctx, int x, int y, int radius){
        for(int r = 1; r <= radius; r++){
            for(int ox = -r; ox <= r; ox++){
                for(int oy = -r; oy <= r; oy++){
                    Tile near = ctx.tiles.get(x + ox, y + oy);
                    if(near == null) continue;
                    if(near.floor().hasSurface() && !near.floor().isLiquid){
                        return near.floor();
                    }
                }
            }
        }
        return WHBlocksEnvironment.defaultMineralFloor();
    }
}
