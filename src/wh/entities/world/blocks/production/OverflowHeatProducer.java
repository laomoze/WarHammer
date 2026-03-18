package wh.entities.world.blocks.production;

import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.blocks.heat.HeatProducer;

public class OverflowHeatProducer extends HeatProducer{
    public boolean ignoreItemFullness = false;
    public boolean dumpExtraItems = true;

    public OverflowHeatProducer(String name){
        super(name);
    }

    public class OverflowHeatProducerBuild extends HeatProducerBuild{
        @Override
        public boolean shouldConsume(){
            if(!ignoreItemFullness && outputItems != null){
                for(ItemStack output : outputItems){
                    if(items.get(output.item) + output.amount > itemCapacity){
                        return false;
                    }
                }
            }

            if(outputLiquids != null && !ignoreLiquidFullness){
                boolean allFull = true;
                for(LiquidStack output : outputLiquids){
                    if(liquids.get(output.liquid) >= liquidCapacity - 0.001f){
                        if(!dumpExtraLiquid){
                            return false;
                        }
                    }else{
                        allFull = false;
                    }
                }

                if(allFull){
                    return false;
                }
            }

            return enabled;
        }

        @Override
        public void craft(){
            consume();

            if(outputItems != null){
                for(ItemStack output : outputItems){
                    for(int i = 0; i < output.amount; i++){
                        // Keep crafting when item storage is full by dropping overflow outputs.
                        if(ignoreItemFullness && dumpExtraItems && items.get(output.item) >= itemCapacity){
                            continue;
                        }
                        offload(output.item);
                    }
                }
            }

            if(wasVisible){
                craftEffect.at(x, y);
            }
            progress %= 1f;
        }
    }
}
