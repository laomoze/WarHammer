package wh.pipelinePlanet.core;

import mindustry.world.*;

/**
 * 中文说明：逐格处理接口，用于封装对单个 tile 的轻量规则。
 */
public interface TilePass{
    String name();

    void apply(GenContext ctx, int x, int y, Tile tile);
}
