package wh.pipelinePlanet.passes;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.Vars;
import mindustry.world.*;
import wh.pipelinePlanet.core.*;

/**
 * 接近原版风格的地形扭曲 pass，思路对应 BasicGenerator.distort(scl, mag)。
 */
public class DistortPass implements GenPass{
    private final float scl;
    private final float mag;

    public DistortPass(float scl, float mag){
        this.scl = scl;
        this.mag = mag;
    }

    @Override
    public String name(){
        return "DistortPass(" + scl + "," + mag + ")";
    }

    @Override
    public void apply(GenContext ctx){
        int w = ctx.width(), h = ctx.height();
        short[] blocks = new short[w * h];
        short[] floors = new short[w * h];

        for(Tile tile : ctx.tiles){
            int x = tile.x, y = tile.y;
            int idx = y * w + x;

            float cx = x + noise(ctx, x - 155f, y - 200f, scl, mag) - mag / 2f;
            float cy = y + noise(ctx, x + 155f, y + 155f, scl, mag) - mag / 2f;

            int sx = Mathf.clamp((int)cx, 0, w - 1);
            int sy = Mathf.clamp((int)cy, 0, h - 1);
            Tile other = ctx.tiles.getn(sx, sy);

            blocks[idx] = other.block().id;
            floors[idx] = other.floor().id;
        }

        for(int i = 0; i < blocks.length; i++){
            Tile tile = ctx.tiles.geti(i);
            tile.setFloor(Vars.content.block(floors[i]).asFloor());
            tile.setBlock(Vars.content.block(blocks[i]));
        }
    }

    private float noise(GenContext ctx, float x, float y, double scl, double mag){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(ctx.seed, 1, 1f, 1f / scl, v.x, v.y, v.z) * (float)mag;
    }
}
