package wh.pipelinePlanet.passes;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.pipelinePlanet.core.*;

/**
 * 中文说明：科技网格阶段：铺设科技地板并替换部分墙体。
 */
public class TechGridPass implements GenPass{
    private final Block floor1;
    private final Block floor2;
    private final Block wall;
    private final int defaultSecSize;

    public TechGridPass(){
        this(Blocks.darkPanel3, Blocks.darkPanel4, Blocks.darkMetal, 20);
    }

    public TechGridPass(Block floor1, Block floor2, Block wall, int secSize){
        this.floor1 = floor1;
        this.floor2 = floor2;
        this.wall = wall;
        this.defaultSecSize = secSize;
    }

    @Override
    public String name(){
        return "TechGridPass";
    }

    @Override
    public void apply(GenContext ctx){
        if(!ctx.cfg.enableTechGrid) return;

        int secSize = Math.max(ctx.cfg.techGridCellSize > 0 ? ctx.cfg.techGridCellSize : defaultSecSize, 2);
        float thresholdA = ctx.cfg.techGridThresholdA;
        float thresholdB = ctx.cfg.techGridThresholdB;
        float wallChance = ctx.cfg.techGridWallChance;
        float innerOffset = ctx.cfg.techGridInnerOffset;

        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            Block block = tile.block();

            if(!floor.asFloor().hasSurface()) continue;

            int x = tile.x, y = tile.y;
            int mx = x % secSize, my = y % secSize;
            int sclx = x / secSize, scly = y / secSize;

            boolean onEdge = (mx == 0 || my == 0 || mx == secSize - 1 || my == secSize - 1);
            if(!onEdge) continue;

            if(noise(ctx, sclx, scly, 0.2f, 1f) > thresholdA && noise(ctx, sclx, scly + 999f, 200f, 1f) > thresholdB){
                if(ctx.rand.chance(noise(ctx, x + 0x231523f, y, 40f, 1f))){
                    Block selected = floor1;
                    if(Mathf.dst(mx, my, secSize / 2f, secSize / 2f) > secSize / 2f + innerOffset){
                        selected = floor2;
                    }
                    tile.setFloor(selected.asFloor());
                }

                if(block.solid && ctx.rand.chance(wallChance)){
                    tile.setBlock(wall);
                }
            }
        }
    }

    private float noise(GenContext ctx, float x, float y, double scl, double mag){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(ctx.seed, 1, 1, 1f / scl, v.x, v.y, v.z) * (float)mag;
    }
}
