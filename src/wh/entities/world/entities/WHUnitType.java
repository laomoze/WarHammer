package wh.entities.world.entities;

import arc.util.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import mindustry.world.*;
import mindustry.world.blocks.units.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.entities.world.blocks.unit.*;
import wh.graphics.*;

import static mindustry.Vars.content;

public class WHUnitType extends UnitType{
    public WHUnitType(String name){
        super(name);
        outlineColor = WHPal.Outline;
        envDisabled = Env.space;
        ammoType = new ItemAmmoType(WHItems.titaniumSteel);
        researchCostMultiplier = 10;
    }

    @Override
    public @Nullable ItemStack[] getRequirements(@Nullable UnitType[] prevReturn, @Nullable float[] timeReturn){
        //find MultReconstructor
       /* var rec = (MultReconstructor)content.blocks().find(b -> b instanceof MultReconstructor re &&
        re.upgradeCosts.keys().toSeq().contains(u -> u[1] == this));*/

        var rec = (MultReconstructor)content.blocks().find(b -> {
            if(b instanceof MultReconstructor re){
                for(var entry : re.upgradeCosts.entries()){
                    UnitType[] recipe = entry.key;
                    if(recipe != null && recipe.length >= 2 && recipe[1] == this){
                        return true;
                    }
                }
            }
            return false;
        });

        if(rec != null){
            for(var entry : rec.upgradeCosts.entries()){
                UnitType[] recipe = entry.key;
                ItemStack[] recipeCost = entry.value;
                if(recipe != null && recipe.length >= 2 && recipe[1] == this){
                    if(prevReturn != null){
                        prevReturn[0] = recipe[0];
                    }
                    if(timeReturn != null){
                        timeReturn[0] = rec.constructTime;
                    }
                    return recipeCost;
                }
            }
        }else{
            //find a factory
            var factory = (UnitFactory)content.blocks().find(u -> u instanceof UnitFactory uf && uf.plans.contains(p -> p.unit == this));
            if(factory != null){

                var plan = factory.plans.find(p -> p.unit == this);
                if(timeReturn != null){
                    timeReturn[0] = plan.time;
                }
                return plan.requirements;
            }else{
                //find unitCallBlock
                var unitCallBlock = (UnitCallBlock2)content.blocks().find(u -> u instanceof UnitCallBlock2 uf && uf.plans.contains(p -> p.unit == this));
                if(unitCallBlock != null){

                    var plan = unitCallBlock.plans.find(p -> p.unit == this);
                    if(timeReturn != null){
                        timeReturn[0] = plan.time;
                    }
                    return plan.requirements;
                }else{
                    //find an assembler
                    var assembler = (UnitAssembler)content.blocks().find(u -> u instanceof UnitAssembler a && a.plans.contains(p -> p.unit == this));
                    if(assembler != null){
                        var plan = assembler.plans.find(p -> p.unit == this);

                        if(timeReturn != null){
                            timeReturn[0] = plan.time;
                        }
                        ItemSeq reqs = new ItemSeq();
                        for(var bstack : plan.requirements){
                            if(bstack.item instanceof Block block){
                                for(var stack : block.requirements){
                                    reqs.add(stack.item, stack.amount * bstack.amount);
                                }
                            }else if(bstack.item instanceof UnitType unit){
                                for(var stack : unit.getTotalRequirements()){
                                    reqs.add(stack.item, stack.amount * bstack.amount);
                                }
                            }
                        }
                        return reqs.toArray();
                    }
                }
            }
        }
        return null;
    }
}
