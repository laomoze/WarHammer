package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;

/**
 * 中文说明：Karvex 喷口与热区生成阶段。
 */
public class KarvexVentPass implements GenPass{
    private static final int ventSpacing = 9;

    @Override
    public String name(){
        return "KarvexVentPass";
    }

    @Override
    public void apply(GenContext ctx){
        int area = ctx.width() * ctx.height();
        int minVents = Math.max(2, area / 90000);
        int placed = 0;

        for(Tile tile : ctx.tiles){
            if(!ctx.rand.chance(0.00055f)) continue;

            Block vent = ventFor(tile.floor());
            if(vent == null) continue;
            if(!canPlaceVent(ctx, tile.x, tile.y, vent)) continue;
            if(hasNearbyVent(ctx, tile.x, tile.y, ventSpacing)) continue;

            placeVent(ctx, tile.x, tile.y, vent);
            placed++;
        }

        int attempts = 0;
        while(placed < minVents && attempts++ < 3){
            for(Tile tile : ctx.tiles){
                if(placed >= minVents) break;
                if(!ctx.rand.chance(0.00034f * (1 + attempts))) continue;

                Block vent = ventFor(tile.floor());
                if(vent == null) continue;
                if(!canPlaceVent(ctx, tile.x, tile.y, vent)) continue;
                if(hasNearbyVent(ctx, tile.x, tile.y, ventSpacing)) continue;

                placeVent(ctx, tile.x, tile.y, vent);
                placed++;
            }
        }
    }

    private Block ventFor(Block floor){
        if(floor == WHBlocksEnvironment.apatite){
            return WHBlocksEnvironment.apatiteVent;
        }else if(floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltStone){
            return WHBlocksEnvironment.darkRockVent;
        }else if(floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters){
            return WHBlocksEnvironment.radiationRockVent;
        }
        return null;
    }

    private boolean canPlaceVent(GenContext ctx, int x, int y, Block vent){
        if(ctx.spawnRoom != null && Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + 8f)){
            return false;
        }

        Tile center = ctx.tiles.get(x, y);
        if(center == null || center.block() != Blocks.air) return false;
        if(isGeothermalFloor(center.floor())) return false;

        int radius = 1;
        int matchingBase = 0;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near == null || near.block().solid) return false;
                if(near.floor().isLiquid || !near.floor().hasSurface()) return false;
                if(near.floor().attributes.get(Attribute.steam) != 0f) return false;
                if(isGeothermalFloor(near.floor())) return false;

                Block expected = ventFor(near.floor());
                if(expected != null && expected != vent){
                    return false;
                }

                if(expected == vent){
                    matchingBase++;
                }
            }
        }

        // Avoid too many vents from tiny accidental patches.
        return matchingBase >= 5;
    }

    private void placeVent(GenContext ctx, int x, int y, Block vent){
        for(Point2 offset : SteamVent.offsets){
            Tile other = ctx.tiles.get(x + offset.x + 1, y + offset.y + 1);
            if(other == null) continue;
            if(other.block().solid) continue;
            other.setFloor(vent.asFloor());
        }
    }

    private boolean isGeothermalFloor(Block floor){
        return floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock
        || floor == Blocks.slag;
    }

    private boolean hasNearbyVent(GenContext ctx, int x, int y, int radius){
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > radius * radius) continue;
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near == null) continue;
                if(near.floor().attributes.get(Attribute.steam) != 0f){
                    return true;
                }
            }
        }
        return false;
    }
}
