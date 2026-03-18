package wh.pipelinePlanet.karvex;

import arc.math.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

import static mindustry.Vars.*;

/**
 * Final map fixes after all terrain/resource passes.
 * Ensures player core exists and enemy spawn overlays remain visible.
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

        sanitizeSpawnArea(ctx);
        placePlayerCore(ctx.spawnRoom.x, ctx.spawnRoom.y);
        restoreEnemySpawnOverlays(ctx);
    }

    private void placePlayerCore(int x, int y){
        Team team = state != null && state.rules != null && state.rules.defaultTeam != null ? state.rules.defaultTeam : Team.sharded;
        Schematic loadout = preferredLoadout != null ? preferredLoadout : universe.getLastLoadout();
        boolean placed = false;
        boolean resourcesApplied = false;

        try{
            if(loadout != null){
                Schematics.placeLoadout(loadout, x, y, team);
                placed = true;
            }
        }catch(Throwable ignored){
            placed = false;
        }

        try{
            if(!placed){
                Schematics.placeLaunchLoadout(x, y);
                placed = true;
                resourcesApplied = true;
            }
        }catch(Throwable ignored){
            if(!placed){
                Schematics.placeLoadout(Loadouts.basicShard, x, y, team);
                placed = true;
            }
        }

        if(placed && !resourcesApplied){
            Tile center = world == null ? null : world.tile(x, y);
            if(center != null && center.build != null && universe != null){
                center.build.items.add(universe.getLaunchResources());
            }
        }
    }

    private void sanitizeSpawnArea(GenContext ctx){
        int radius = Math.max(5, ctx.spawnRoom.radius + 3);
        int r2 = radius * radius;

        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;

                Tile tile = ctx.tiles.get(ctx.spawnRoom.x + ox, ctx.spawnRoom.y + oy);
                if(tile == null) continue;

                tile.setOverlay(Blocks.air);
                tile.setBlock(Blocks.air);

                if(!tile.floor().hasSurface() || tile.floor().isLiquid){
                    tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 8).asFloor());
                }
            }
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
                tile.setFloor(findNearbyLandFloor(ctx, tile.x, tile.y, 7).asFloor());
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
