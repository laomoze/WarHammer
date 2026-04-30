package wh.content;

import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.type.Weapon;

public class WHOverride{
    private WHOverride(){
    }

    public static void load(){
      /*  ((GenericCrafter)Blocks.multiPress).outputItem = new ItemStack(Items.graphite, 4);
        Blocks.multiPress.removeConsumer(Blocks.multiPress.findConsumer(c -> c instanceof ConsumeItems));
        Blocks.multiPress.consumePower(2);
        Blocks.multiPress.consumeItems(new ItemStack(Items.coal, 4));
        Blocks.multiPress.consumeLiquid(Liquids.water, 7.5f / 60f);
        Blocks.multiPress.requirements(Category.crafting, with(Items.silicon, 25, Items.lead, 80, Items.graphite, 50, WHItems.titaniumSteel, 50));
        Blocks.multiPress.itemCapacity = 30;*/

        Blocks.darksand.attributes.set(WHBlocksEnvironment.hasPromethium, 0.5f);
        Blocks.sand.attributes.set(WHBlocksEnvironment.hasPromethium, 0.5f);

        Weapon scepterWeapon1 = UnitTypes.scepter.weapons.get(0);
        scepterWeapon1.layerOffset = -0.001f;
        scepterWeapon1.top = false;
        scepterWeapon1.rotateSpeed = 1;
        scepterWeapon1.rotationLimit = 20;
        scepterWeapon1.shootCone = 30;
        scepterWeapon1.shoot.shots = 1;
        scepterWeapon1.shoot.shotDelay = 0f;
        scepterWeapon1.inaccuracy = 6f;
        scepterWeapon1.reload = 8f;
        Weapon reignWeapon1 = UnitTypes.reign.weapons.get(0);
        scepterWeapon1.top = false;
        reignWeapon1.layerOffset = -0.001f;
        reignWeapon1.shoot.shots = 3;
        reignWeapon1.shoot.shotDelay = 4f;
        reignWeapon1.reload = 15f;

        Weapon fortressWeapon1 = UnitTypes.fortress.weapons.get(0);
        fortressWeapon1.x = 37 / 4f;
        fortressWeapon1.y = 3 / 4f;

        Weapon quasarWeapon1 = UnitTypes.quasar.weapons.get(0);
        quasarWeapon1.x = 29 / 4f;
        quasarWeapon1.y = -4 / 4f;

        Items.graphite.hardness = 2;
    }
}
