package wh.pipelinePlanet.core;

import arc.math.geom.*;
import mindustry.content.*;
import mindustry.maps.generators.*;
import mindustry.world.*;

/**
 * 中文说明：可组合行星生成器基类，负责装配并驱动 pass 流水线。
 */
public abstract class PipelinePlanetGenerator extends PlanetGenerator{
    protected final GenConfig config = new GenConfig();
    protected final PassRunner runner = new PassRunner();
    private boolean pipelineReady = false;

    protected void configurePipeline(PassRunner runner){
        // 基类默认不注入 pass；由子类自行定义完整流程。
    }

    protected final void ensurePipeline(){
        if(pipelineReady) return;
        configurePipeline(runner);
        pipelineReady = true;
    }

    @Override
    protected void genTile(Vec3 position, TileGen tile){
        tile.floor = Blocks.stone;
        tile.overlay = Blocks.air;
        tile.block = tile.floor.asFloor().wall;
    }

    @Override
    protected void generate(){
        ensurePipeline();
        if(tiles == null || sector == null) return;
        runner.run(new GenContext(tiles, sector, rand, config, seed, baseSeed));
    }
}
