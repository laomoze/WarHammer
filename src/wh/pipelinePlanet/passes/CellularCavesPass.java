package wh.pipelinePlanet.passes;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.pipelinePlanet.core.*;

/**
 * 与原版对齐的细胞自动机 pass，对应 BasicGenerator.cells(iterations, 16, 16, 3)。
 */
public class CellularCavesPass implements GenPass{
    private final int iterations;
    private final int birthLimit;
    private final int deathLimit;
    private final int radius;

    public CellularCavesPass(){
        this(4, 16, 16, 3);
    }

    public CellularCavesPass(int iterations){
        this(iterations, 16, 16, 3);
    }

    public CellularCavesPass(int iterations, int birthLimit, int deathLimit, int radius){
        this.iterations = Math.max(1, iterations);
        this.birthLimit = birthLimit;
        this.deathLimit = deathLimit;
        this.radius = Math.max(1, radius);
    }

    @Override
    public String name(){
        return "CellularCavesPass";
    }

    @Override
    public void apply(GenContext ctx){
        GridBits read = new GridBits(ctx.width(), ctx.height());
        GridBits write = new GridBits(ctx.width(), ctx.height());

        for(Tile tile : ctx.tiles){
            read.set(tile.x, tile.y, !tile.block().isAir());
        }

        for(int i = 0; i < iterations; i++){
            for(Tile tile : ctx.tiles){
                int x = tile.x, y = tile.y;
                int alive = 0;

                for(int cx = -radius; cx <= radius; cx++){
                    for(int cy = -radius; cy <= radius; cy++){
                        if((cx == 0 && cy == 0) || !Mathf.within(cx, cy, radius)) continue;
                        if(!Structs.inBounds(x + cx, y + cy, ctx.width(), ctx.height()) || read.get(x + cx, y + cy)){
                            alive++;
                        }
                    }
                }

                if(read.get(x, y)){
                    write.set(x, y, alive >= deathLimit);
                }else{
                    write.set(x, y, alive > birthLimit);
                }
            }

            read.set(write);
        }

        for(Tile tile : ctx.tiles){
            if(read.get(tile.x, tile.y)){
                Block wall = tile.floor().asFloor().wall;
                tile.setBlock(wall == null ? Blocks.stoneWall : wall);
            }else{
                tile.setBlock(Blocks.air);
            }
        }
    }
}
