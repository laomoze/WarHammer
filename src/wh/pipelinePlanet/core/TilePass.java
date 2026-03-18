package wh.pipelinePlanet.core;

import mindustry.world.*;

/**
 * 按单格执行的管线单元。
 * 实现时应避免在热点循环中产生额外分配。
 */
public interface TilePass{
    String name();

    void apply(GenContext ctx, int x, int y, Tile tile);
}
