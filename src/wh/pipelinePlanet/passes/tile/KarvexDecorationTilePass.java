package wh.pipelinePlanet.passes.tile;

import arc.math.geom.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.karvex.*;

/**
 * Karvex 装饰 pass。
 * 使用地表映射表中的 dec 规则，而不是纯随机装饰。
 */
public class KarvexDecorationTilePass implements TilePass{
    private final KarvexSurfaceTable surface;

    public KarvexDecorationTilePass(KarvexSurfaceTable surface){
        this.surface = surface;
    }

    @Override
    public String name(){
        return "KarvexDecorationTilePass";
    }

    @Override
    public void apply(GenContext ctx, int x, int y, Tile tile){
        if(tile.block() != Blocks.air) return;
        if(!tile.floor().hasSurface()) return;

        // 与原版一致：四邻有实体块时不放装饰，避免贴墙重叠。
        for(Point2 point : Geometry.d4){
            Tile near = ctx.tiles.get(x + point.x, y + point.y);
            if(near != null && near.block() != Blocks.air){
                return;
            }
        }

        float chance = 0.01f;
        Block floor = tile.floor();

        // 降低晶体/铬矿类装饰密度，减少视觉噪声。
        if(floor == WHBlocksEnvironment.quartzSand || floor == Blocks.crystalFloor){
            chance = 0.0025f;
        }else if(floor == WHBlocksEnvironment.chromiteStone || floor == WHBlocksEnvironment.radiationSand
        || floor == Blocks.ferricCraters || floor == Blocks.denseRedStone){
            chance = 0.0035f;
        }

        if(ctx.rand.chance(chance)){
            tile.setBlock(surface.decorationFor(tile.floor()));
        }
    }
}
