package wh.pipelinePlanet.karvex;

import arc.math.Mathf;
import arc.math.geom.Point2;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.SteamVent;
import mindustry.world.meta.Attribute;
import wh.content.WHBlocksEnvironment;
import wh.pipelinePlanet.core.GenContext;
import wh.pipelinePlanet.core.GenPass;

/**
 * Places geothermal vents on coherent floor regions.
 */
public class KarvexVentPass implements GenPass{
    private static final int VENT_SPACING = 9;

    @Override
    public String name(){
        return "KarvexVentPass";
    }

    @Override
    public void apply(GenContext ctx){
        int area = ctx.width() * ctx.height();
        int target = Math.max(4, area / 88000);
        int placed = 0;

        for(Tile tile : ctx.tiles){
            if(placed >= target) break;
            if(!ctx.rand.chance(0.0007f)) continue;

            Block vent = ventFor(tile.floor());
            if(vent == null) continue;
            if(!canPlaceVent(ctx, tile.x, tile.y, vent)) continue;
            if(hasNearbyVent(ctx, tile.x, tile.y, VENT_SPACING)) continue;

            placeVent(ctx, tile.x, tile.y, vent);
            placed++;
        }

        int attempts = 0;
        while(placed < target && attempts++ < 3){
            for(Tile tile : ctx.tiles){
                if(placed >= target) break;
                if(!ctx.rand.chance(0.00042f * (1f + attempts))) continue;

                Block vent = ventFor(tile.floor());
                if(vent == null) continue;
                if(!canPlaceVent(ctx, tile.x, tile.y, vent)) continue;
                if(hasNearbyVent(ctx, tile.x, tile.y, VENT_SPACING)) continue;

                placeVent(ctx, tile.x, tile.y, vent);
                placed++;
            }
        }
    }

    private Block ventFor(Block floor){
        if(floor == WHBlocksEnvironment.scorchedEarth || floor == WHBlocksEnvironment.scorchedStone){
            return WHBlocksEnvironment.scorchedEarthVent;
        }
        if(floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.chromiteFloor
        || floor == WHBlocksEnvironment.chromiteFloorDark){
            return WHBlocksEnvironment.chromiteVent;
        }
        if(floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.manganeseFloor){
            return WHBlocksEnvironment.manganeseVent;
        }
        if(floor == WHBlocksEnvironment.cobaltStone
        || floor == WHBlocksEnvironment.cobaltFloor){
            return WHBlocksEnvironment.cobaltVent;
        }
        if(floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.darkHotRock
                || floor == WHBlocksEnvironment.darkMagmaRock
                || floor == WHBlocksEnvironment.darkRockCraters) {
            return WHBlocksEnvironment.darkRockVent;
        }
        if(floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters){
            return WHBlocksEnvironment.radiationRockVent;
        }
        if (floor == WHBlocksEnvironment.quartzSand) {
            return WHBlocksEnvironment.quartzSandVent;
        }
        if (floor == WHBlocksEnvironment.apatiteCoarse) {
            return WHBlocksEnvironment.apatiteVent;
        }
        return null;
    }

    private boolean canPlaceVent(GenContext ctx, int x, int y, Block vent){
        if(ctx.spawnRoom != null && Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + 9f)){
            return false;
        }

        Tile center = ctx.tiles.get(x, y);
        if(center == null || center.block() != Blocks.air) return false;
        if(!center.floor().hasSurface() || center.floor().isLiquid) return false;
        if(center.floor().attributes.get(Attribute.steam) != 0f) return false;

        Floor parent = vent.asFloor();
        if(parent == null) return false;

        for(Point2 off : SteamVent.offsets){
            Tile tile = ctx.tiles.get(x + off.x + 1, y + off.y + 1);
            if(tile == null) return false;
            if(tile.block().solid) return false;
            if(tile.floor().isLiquid) return false;
            if(tile.floor().attributes.get(Attribute.steam) != 0f) return false;
        }
        return true;
    }

    private boolean hasNearbyVent(GenContext ctx, int x, int y, int spacing){
        for(int ox = -spacing; ox <= spacing; ox++){
            for(int oy = -spacing; oy <= spacing; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near == null) continue;
                if(near.floor().attributes.get(Attribute.steam) > 0f){
                    return true;
                }
            }
        }
        return false;
    }

    private void placeVent(GenContext ctx, int x, int y, Block vent){
        for(Point2 off : SteamVent.offsets){
            Tile tile = ctx.tiles.get(x + off.x + 1, y + off.y + 1);
            if(tile != null){
                tile.setFloor(vent.asFloor());
                tile.setBlock(Blocks.air);
                tile.setOverlay(Blocks.air);
            }
        }
    }
}
