package wh.entities.world.blocks.production;

import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.ConsumeItemFilter;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawHeatOutput;
import mindustry.world.draw.DrawMulti;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class FlammabilityHeatProducer extends GenericCrafter {
    public float heatOutput = 10f;
    public float warmupRate = 0.15f;
    public @Nullable ConsumeItemFilter filterItem;
    public @Nullable ConsumeLiquidFilter filterLiquid;


    public FlammabilityHeatProducer(String name){
        super(name);

        drawer = new DrawMulti(new DrawDefault(), new DrawHeatOutput());
        rotateDraw = false;
        rotate = true;
        canOverdrive = false;
        drawArrow = true;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.output, heatOutput, StatUnit.heatUnits);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("heat", (HeatProducerBuild entity) -> new Bar("bar.heat", Pal.lightOrange, () ->
                Math.min(entity.heat / heatOutput, 1f))
        );
    }

    @Override
    public void init(){
        filterItem = findConsumer(c -> c instanceof ConsumeItemFilter);
        filterLiquid = findConsumer(c -> c instanceof ConsumeLiquidFilter);
        super.init();
    }

    public class HeatProducerBuild extends GenericCrafterBuild implements HeatBlock {
        public float heat;
        public float efficiencyMultiplier = 1f;
        @Override
        public void updateEfficiencyMultiplier(){
            efficiencyMultiplier = 1f;
            if(filterItem != null){
                float m = (float) Math.pow( filterItem.efficiencyMultiplier(this),2)+0.3f;
                if(m > 0) efficiencyMultiplier *= m;
            }
            if(filterLiquid != null){
                float m = (float) Math.pow(filterLiquid.efficiencyMultiplier(this),2)+0.3f;
                if(m > 0) efficiencyMultiplier *= m;
            }
            if (filterItem != null && filterItem.efficiencyMultiplier(this) <= 0) {
                efficiencyMultiplier = 0f;
            }
            if (filterLiquid != null && filterLiquid.efficiencyMultiplier(this) <= 0) {
                efficiencyMultiplier = 0f;
            }
            super.updateEfficiencyMultiplier();
        }

        @Override
        public void updateTile(){
            super.updateTile();
            updateEfficiencyMultiplier();

            heat = Mathf.approachDelta(heat, heatOutput* efficiencyMultiplier , warmupRate * delta());
        }

        @Override
        public float heatFrac(){
            return heat / (heatOutput * efficiencyMultiplier);
        }

        @Override
        public float heat(){
            return heat;
        }



        @Override
        public void write(Writes write){
            super.write(write);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            heat = read.f();
        }
    }
}
