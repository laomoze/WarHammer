package wh.pipelinePlanet.passes;

import mindustry.content.*;
import wh.pipelinePlanet.core.*;
import mindustry.world.*;

/**
 * 基础地形修正：确保地面与静态墙状态合法。
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
