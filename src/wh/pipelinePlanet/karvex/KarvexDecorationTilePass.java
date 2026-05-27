package wh.pipelinePlanet.karvex;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;
import wh.content.WHBlocksEnvironment;
import wh.pipelinePlanet.core.GenContext;
import wh.pipelinePlanet.core.TilePass;

/**
 * Karvex-specific decoration rules.
 * Keeps dark crystal clusters on dark-rock family only.
 */
public class KarvexDecorationTilePass implements TilePass{
    private final KarvexSurfaceProfile surfaceProfile;

    public KarvexDecorationTilePass(KarvexSurfaceProfile surfaceProfile){
        this.surfaceProfile = surfaceProfile;
    }

    @Override
    public String name(){
        return "KarvexDecorationTilePass";
    }

    @Override
    public void apply(GenContext ctx, int x, int y, Tile tile){
        if(tile.block() != Blocks.air) return;
        if(!tile.floor().hasSurface()) return;
        if(tile.floor().isLiquid) return;

        for(Point2 p : Geometry.d4){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.block() != Blocks.air){
                return;
            }
        }

        Block floor = tile.floor();

        // Dark-rock crystals only on dark-rock family, never on ore-shale or ore floors.
        if(isDarkRockFamily(floor)
                && WHBlocksEnvironment.darkRockCrystal != null
        && tile.overlay() == Blocks.air
                && !nearBlock(ctx, x, y, WHBlocksEnvironment.darkRockCrystal, 5)
        && ctx.rand.chance(0.0032f)){
            tile.setBlock(WHBlocksEnvironment.darkRockCrystal);
            return;
        }

        Block decoration = surfaceProfile.decorationForFloor(floor);
        if(decoration == null || decoration == Blocks.air) return;
        if (decoration == WHBlocksEnvironment.darkRockCrystal) return;

        float chance = 0.0085f;
        if(floor == WHBlocksEnvironment.quartzSand || floor == Blocks.crystalFloor){
            chance = 0.0023f;
        }else if(floor == WHBlocksEnvironment.chromiteStone || floor == WHBlocksEnvironment.radiationSand){
            chance = 0.0032f;
        }else if(isMetalFamily(floor)){
            chance = 0.0042f;
        }

        if(ctx.rand.chance(chance)){
            tile.setBlock(decoration);
        }
    }

    private boolean isDarkRockFamily(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.darkHotRock
                || floor == WHBlocksEnvironment.darkMagmaRock
                || floor == WHBlocksEnvironment.darkRockCraters;
    }

    private boolean isMetalFamily(Block floor){
        return floor == WHBlocksEnvironment.manganeseFloor
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteFloor
        || floor == WHBlocksEnvironment.chromiteFloorDark
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltFloor
        || floor == WHBlocksEnvironment.cobaltStone;
    }

    private boolean nearBlock(GenContext ctx, int x, int y, Block block, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                if(rx == 0 && ry == 0) continue;
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.block() == block){
                    return true;
                }
            }
        }
        return false;
    }
}
