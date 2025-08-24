package wh.content;

import mindustry.content.*;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.ConsumeItems;

import static mindustry.type.ItemStack.with;

public class WHOverride {
    private WHOverride() {
    }

    public static void load() {


        ((GenericCrafter) Blocks.multiPress).outputItem = new

                ItemStack(Items.graphite, 4);
        Blocks.multiPress.

                removeConsumer(Blocks.multiPress.findConsumer(c -> c instanceof ConsumeItems));
        Blocks.multiPress.

                consumePower(2);
        Blocks.multiPress.

                consumeItems(new ItemStack(Items.coal, 4));
        Blocks.multiPress.

                consumeLiquid(Liquids.water, 7.5f / 60f);
        Blocks.multiPress.

                requirements(Category.crafting, with(Items.silicon, 25, Items.lead, 80, Items.graphite, 50, WHItems.titaniumSteel, 50));
        Blocks.multiPress.itemCapacity = 30;

        UnitTypes.scepter.weapons.get(0).layerOffset=-0.005f;
        UnitTypes.scepter.weapons.get(0).rotateSpeed = 1;
        UnitTypes.scepter.weapons.get(0).rotationLimit = 20;
        UnitTypes.scepter.weapons.get(0).shootCone = 30;
        UnitTypes.scepter.weapons.get(0).shoot.shots = 1;
        UnitTypes.scepter.weapons.get(0).shoot.shotDelay = 0f;
        UnitTypes.scepter.weapons.get(0).inaccuracy=6f;
        UnitTypes.scepter.weapons.get(0).reload=6f;
        UnitTypes.reign.weapons.get(0).layerOffset=-0.007f;
        UnitTypes.reign.weapons.get(0).shoot.shots = 3;
        UnitTypes.reign.weapons.get(0).shoot.shotDelay = 4f;
        UnitTypes.reign.weapons.get(0).reload=17f;
        UnitTypes.fortress.weapons.get(0).layerOffset=-0.005f;

    }
}
