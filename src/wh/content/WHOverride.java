package wh.content;

import mindustry.content.*;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.ConsumeItems;

import static mindustry.type.ItemStack.with;
import static wh.core.WarHammerMod.name;

public class WHOverride{
    private WHOverride(){
    }

    public static void load(){
        ((GenericCrafter)Blocks.multiPress).outputItem = new ItemStack(Items.graphite, 4);
        Blocks.multiPress.removeConsumer(Blocks.multiPress.findConsumer(c -> c instanceof ConsumeItems));
        Blocks.multiPress.consumePower(2);
        Blocks.multiPress.consumeItems(new ItemStack(Items.coal, 4));
        Blocks.multiPress.consumeLiquid(Liquids.water, 7.5f / 60f);
        Blocks.multiPress.requirements(Category.crafting, with(Items.silicon, 25, Items.lead, 80, Items.graphite, 50, WHItems.titaniumSteel, 50));
        Blocks.multiPress.itemCapacity = 30;

        Weapon scepterWeapon1 = UnitTypes.scepter.weapons.get(0);
        scepterWeapon1.layerOffset = -0.001f;
        scepterWeapon1.top = false;
        scepterWeapon1.rotateSpeed = 1;
        scepterWeapon1.rotationLimit = 20;
        scepterWeapon1.shootCone = 30;
        scepterWeapon1.shoot.shots = 1;
        scepterWeapon1.shoot.shotDelay = 0f;
        scepterWeapon1.inaccuracy = 6f;
        scepterWeapon1.reload = 6f;
        Weapon reignWeapon1 = UnitTypes.reign.weapons.get(0);
        scepterWeapon1.top = false;
        reignWeapon1.layerOffset = -0.001f;
        reignWeapon1.shoot.shots = 3;
        reignWeapon1.shoot.shotDelay = 4f;
        reignWeapon1.reload = 20f;
        UnitTypes.fortress.weapons.get(0).layerOffset = -0.001f;
    }
}
