package wh.pipelinePlanet.core;

import arc.struct.*;
import mindustry.world.*;

/**
 * 中文说明：把多个 TilePass 组合成一个 GenPass 阶段。
 */
public class TilePassStage implements GenPass{
    private final Seq<TilePass> tilePasses = new Seq<>();
    private final String stageName;

    public TilePassStage(String stageName){
        this.stageName = stageName;
    }

    public TilePassStage add(TilePass pass){
        tilePasses.add(pass);
        return this;
    }

    public Seq<TilePass> passes(){
        return tilePasses;
    }

    @Override
    public String name(){
        return stageName;
    }

    @Override
    public void apply(GenContext ctx){
        if(tilePasses.isEmpty()) return;

        for(Tile tile : ctx.tiles){
            int x = tile.x, y = tile.y;
            for(int i = 0; i < tilePasses.size; i++){
                tilePasses.get(i).apply(ctx, x, y, tile);
            }
        }
    }
}
