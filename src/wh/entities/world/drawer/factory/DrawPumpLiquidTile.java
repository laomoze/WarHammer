package wh.entities.world.drawer.factory;

import mindustry.gen.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.production.Pump.*;
import mindustry.world.draw.*;

public class DrawPumpLiquidTile extends DrawLiquidTile{

    public DrawPumpLiquidTile(){
    }

    public DrawPumpLiquidTile(float padding){
        this.padding = padding;
    }

    @Override
    public void draw(Building build){
        if(!(build instanceof PumpBuild pump) || pump.liquidDrop == null) return;

        LiquidBlock.drawTiledFrames(
        build.block.size,
        build.x,
        build.y,
        padLeft,
        padRight,
        padTop,
        padBottom,
        pump.liquidDrop,
        build.liquids.get(pump.liquidDrop) / build.block.liquidCapacity * alpha
        );
    }
}
