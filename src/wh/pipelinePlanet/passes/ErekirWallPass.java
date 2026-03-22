package wh.pipelinePlanet.passes;

import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.pipelinePlanet.core.*;

/**
 * 中文说明：Erekir 风格墙体修整阶段。
 */
public class ErekirWallPass implements GenPass{
    @Override
    public String name(){
        return "ErekirWallPass";
    }

    @Override
    public void apply(GenContext ctx){
        for(Tile tile : ctx.tiles){
            // Keep static walls on solid floor so liquid does not leak through wall regions.
            if(tile.block().isStatic()){
                ensureWallSupportFloor(ctx, tile);
            }

            // Erekir: denser regolith wall fields.
            if(tile.floor() == Blocks.regolith && noise(ctx, tile.x, tile.y, 3, 0.4f, 13f, 1f) > 0.59f){
                placeWall(ctx, tile, Blocks.regolithWall);
            }

            // Erekir: arkyic walls on static arkyic terrain.
            if((tile.floor() == Blocks.arkyciteFloor || tile.floor() == Blocks.arkyicStone) && tile.block().isStatic()){
                placeWall(ctx, tile, Blocks.arkyicWall);
            }

            // Preserve Erekir-like sealed outer fringe using local edge heuristics only.
            if(shouldForceEdgeWall(ctx, tile)){
                Block wall = tile.floor().asFloor().wall;
                placeWall(ctx, tile, (wall == null || wall == Blocks.air) ? Blocks.yellowStoneWall : wall);
            }
        }
    }

    private void placeWall(GenContext ctx, Tile tile, Block wall){
        ensureWallSupportFloor(ctx, tile);
        tile.setBlock(wall);
    }

    private void ensureWallSupportFloor(GenContext ctx, Tile tile){
        if(tile.floor().hasSurface() && !tile.floor().isLiquid) return;
        tile.setFloor(findNearbySupportFloor(ctx, tile.x, tile.y).asFloor());
    }

    private Block findNearbySupportFloor(GenContext ctx, int x, int y){
        // Prefer immediate neighbors to keep local floor style.
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(isValidSupportFloor(near)){
                return near.floor();
            }
        }

        // Fallback: expand search ring when nearby tiles are all liquid/invalid.
        for(int radius = 2; radius <= 3; radius++){
            for(int ox = -radius; ox <= radius; ox++){
                for(int oy = -radius; oy <= radius; oy++){
                    if(Math.abs(ox) != radius && Math.abs(oy) != radius) continue;
                    Tile near = ctx.tiles.get(x + ox, y + oy);
                    if(isValidSupportFloor(near)){
                        return near.floor();
                    }
                }
            }
        }

        return Blocks.yellowStone;
    }

    private boolean isValidSupportFloor(Tile tile){
        if(tile == null) return false;
        Block floor = tile.floor();
        return floor.asFloor().hasSurface() && !floor.asFloor().isLiquid;
    }

    private boolean shouldForceEdgeWall(GenContext ctx, Tile tile){
        int edge = distanceToMapEdge(tile.x, tile.y, ctx.width(), ctx.height());
        if(edge <= 1) return true;
        if(edge > 3) return false;

        int exposed = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
            if(near == null || !near.floor().hasSurface()){
                exposed++;
            }
        }
        if(exposed < 2) return false;

        return noise(ctx, tile.x + 97f, tile.y - 61f, 2, 0.65f, 21f, 1f) > -0.22f;
    }

    private int distanceToMapEdge(int x, int y, int w, int h){
        int dx = Math.min(x, w - 1 - x);
        int dy = Math.min(y, h - 1 - y);
        return Math.min(dx, dy);
    }

    private float noise(GenContext ctx, float x, float y, double octaves, double falloff, double scl, double mag){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(ctx.seed, octaves, falloff, 1f / scl, v.x, v.y, v.z) * (float)mag;
    }
}
