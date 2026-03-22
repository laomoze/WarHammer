package wh.pipelinePlanet.passes;

import mindustry.content.*;
import mindustry.world.*;
import wh.pipelinePlanet.core.*;

/**
 * 中文说明：基础地形阶段：初始地板/墙体填充与底稿构建。
 */
public class BaseTerrainPass implements GenPass{
    @Override
    public String name(){
        return "BaseTerrainPass";
    }

    @Override
    public void apply(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.floor() == Blocks.air){
                tile.setFloor(Blocks.stone.asFloor());
            }

            if(!tile.block().isStatic()){
                tile.setBlock(Blocks.air);
            }
        }
    }
}
