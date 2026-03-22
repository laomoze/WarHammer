package wh.pipelinePlanet.karvex;

import mindustry.content.*;
import mindustry.game.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

import static mindustry.Vars.*;

/**
 * 中文说明：Karvex 收尾阶段：出生点落载、规则收束与残局清理。
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

        clearLiquidWalls(ctx);
        sanitizeArea(ctx, ctx.spawnRoom.x, ctx.spawnRoom.y, 15);
        for(RoomAnchor enemy : ctx.enemyRooms){
            sanitizeArea(ctx, enemy.x, enemy.y, 6);
        }

        placePlayerCore(ctx.spawnRoom.x, ctx.spawnRoom.y);
        restoreEnemySpawnOverlays(ctx);
    }

    private void clearLiquidWalls(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(!tile.floor().isLiquid) continue;

            if(tile.block() != Blocks.air){
                tile.setBlock(Blocks.air);
            }
            if(tile.overlay().needsSurface){
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void sanitizeArea(GenContext ctx, int cx, int cy, int radius){
        int r2 = radius * radius;

        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;

                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null) continue;

                tile.setOverlay(Blocks.air);
                tile.setBlock(Blocks.air);

                if(!tile.floor().hasSurface() || tile.floor().isLiquid){
                    tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 10).asFloor());
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

        if(tryPlaceLaunchLoadout(x, y)){
            return;
        }

        if(tryPlaceSchematic(Loadouts.basicShard, x, y, team)){
            addLaunchResources(x, y);
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

    private void restoreEnemySpawnOverlays(GenContext ctx){
        for(RoomAnchor enemy : ctx.enemyRooms){
            Tile tile = ctx.tiles.get(enemy.x, enemy.y);
            if(tile == null) continue;

            if(tile.block() != Blocks.air){
                tile.setBlock(Blocks.air);
            }
            if(tile.floor().isLiquid || !tile.floor().hasSurface()){
                tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 8).asFloor());
            }
            tile.setOverlay(Blocks.spawn);
        }
    }

    private Floor findNearbyLandFloor(GenContext ctx, int x, int y, int radius){
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

        return WHBlocksEnvironment.mineralSandstone.asFloor();
    }
}
