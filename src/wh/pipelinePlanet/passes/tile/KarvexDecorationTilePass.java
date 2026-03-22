package wh.pipelinePlanet.passes.tile;

import arc.math.geom.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.karvex.*;

/**
 * 中文说明：Karvex 地表装饰放置阶段。
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

        // Match vanilla behavior: skip decoration next to walls.
        for(Point2 point : Geometry.d4){
            Tile near = ctx.tiles.get(x + point.x, y + point.y);
            if(near != null && near.block() != Blocks.air){
                return;
            }
        }

        Block floor = tile.floor();
        if(isCrystalDecorationFloor(floor)){
            float clusterChance = floor == Blocks.crystalFloor ? 0.020f : 0.014f;
            if(ctx.rand.chance(clusterChance)
            && !nearBlock(ctx, x, y, Blocks.crystalCluster, 3)
            && !nearBlock(ctx, x, y, Blocks.vibrantCrystalCluster, 3)){
                tile.setBlock(floor == Blocks.crystalFloor && ctx.rand.chance(0.35f) ? Blocks.vibrantCrystalCluster : Blocks.crystalCluster);
                return;
            }
        }else if(floor == WHBlocksEnvironment.darkRock && nearCrystalField(ctx, x, y, 2)){
            if(ctx.rand.chance(0.0045f)
            && !nearBlock(ctx, x, y, Blocks.crystalCluster, 4)
            && !nearBlock(ctx, x, y, Blocks.vibrantCrystalCluster, 4)){
                tile.setBlock(ctx.rand.chance(0.2f) ? Blocks.vibrantCrystalCluster : Blocks.crystalCluster);
                return;
            }
        }

        float chance = 0.01f;

        // Keep crystal/chromite-family floors slightly sparser than other surfaces.
        if(floor == WHBlocksEnvironment.quartzSand || floor == Blocks.crystalFloor){
            chance = 0.0025f;
        }else if(floor == WHBlocksEnvironment.chromiteStone || floor == WHBlocksEnvironment.radiationSand
        || floor == Blocks.ferricCraters || floor == Blocks.denseRedStone){
            chance = 0.0035f;
        }

        Block decoration = surfaceProfile.decorationForFloor(floor);
        if(decoration == null || decoration == Blocks.air) return;

        if(ctx.rand.chance(chance)){
            tile.setBlock(decoration);
        }
    }

    private boolean isCrystalDecorationFloor(Block floor){
        return floor == Blocks.crystallineStone || floor == Blocks.crystalFloor;
    }

    private boolean nearCrystalField(GenContext ctx, int x, int y, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && isCrystalDecorationFloor(near.floor())){
                    return true;
                }
            }
        }
        return false;
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
