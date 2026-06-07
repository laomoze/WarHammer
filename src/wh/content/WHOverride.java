package wh.content;

import arc.math.Interp;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.Weapon;
import wh.graphics.WHPal;

import static mindustry.Vars.tilesize;

public class WHOverride {
    private WHOverride() {
    }

    public static void load() {
      /*  ((GenericCrafter)Blocks.multiPress).outputItem = new ItemStack(Items.graphite, 4);
        Blocks.multiPress.removeConsumer(Blocks.multiPress.findConsumer(c -> c instanceof ConsumeItems));
        Blocks.multiPress.consumePower(2);
        Blocks.multiPress.consumeItems(new ItemStack(Items.coal, 4));
        Blocks.multiPress.consumeLiquid(Liquids.water, 7.5f / 60f);
        Blocks.multiPress.requirements(Category.crafting, with(Items.silicon, 25, Items.lead, 80, Items.graphite, 50, WHItems.titaniumSteel, 50));
        Blocks.multiPress.itemCapacity = 30;*/

        Blocks.darksand.attributes.set(WHBlocksEnvironment.hasPromethium, 1f);
        Blocks.sand.attributes.set(WHBlocksEnvironment.hasPromethium, 0.5f);

        Weapon scepterWeapon1 = UnitTypes.scepter.weapons.get(0);
        scepterWeapon1.x = 63 / 4f;
        scepterWeapon1.y = -2 / 4f;
        scepterWeapon1.layerOffset = 0.0001f;
        scepterWeapon1.top = true;
        scepterWeapon1.rotateSpeed = 1;
        scepterWeapon1.rotationLimit = 20;
        scepterWeapon1.shootCone = 30;
        scepterWeapon1.shoot.shots = 1;
        scepterWeapon1.shoot.shotDelay = 0f;
        scepterWeapon1.inaccuracy = 6f;
        scepterWeapon1.reload = 8f;

        UnitTypes.scepter.outlineColor = WHPal.OutlineS;
        UnitTypes.scepter.weapons.removeAll(b -> b.name.equals("scepter-mount"));

        BulletType smallBullet = new BasicBulletType(12f, 20) {{
            width = 4.5f;
            height = 35f;
            lifetime = (26f * tilesize) / 12f;
            shrinkX = 0.6f;
            shrinkY = 0f;
            shrinkInterp = Interp.slope;

            trailChance = 10f / 60f;
            trailColor = Pal.bulletYellowBack;
            trailEffect = Fx.bulletSparkSmokeTrailSmall;
            trailSpread = 12f;
            shootEffect = Fx.shootScepterSecondary;
            hitEffect = Fx.hitScepterSecondary;
        }};

        UnitTypes.scepter.weapons.add(
                new Weapon("mount-weapon") {{
                    reload = 12;
                    x = 29 / 4f;
                    y = 10 / 4f;
                    rotate = true;
                    ejectEffect = Fx.casing1;
                    bullet = smallBullet;
                    shootSound = Sounds.shootScepterSecondary;
                    rotateSpeed = 3f;
                }},
                new Weapon("mount-weapon") {{
                    reload = 15f;
                    x = 33 / 4f;
                    y = -26 / 4f;
                    rotate = true;
                    ejectEffect = Fx.casing1;
                    bullet = smallBullet;
                    shootSound = Sounds.shootScepterSecondary;
                    rotateSpeed = 3f;
                }});

        Weapon reignWeapon1 = UnitTypes.reign.weapons.get(0);
        reignWeapon1.x = 86 / 4f;
        reignWeapon1.y = 1 / 4f;
        reignWeapon1.shoot.shots = 3;
        reignWeapon1.shoot.shotDelay = 4f;
        reignWeapon1.reload = 15f;

        Weapon fortressWeapon1 = UnitTypes.fortress.weapons.get(0);
        fortressWeapon1.x = 37 / 4f;
        fortressWeapon1.y = 3 / 4f;

        Weapon quasarWeapon1 = UnitTypes.quasar.weapons.get(0);
        quasarWeapon1.x = 25 / 4f;
        quasarWeapon1.y = 0 / 4f;

        Items.graphite.hardness = 2;
    }
}
