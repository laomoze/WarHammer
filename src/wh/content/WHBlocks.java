//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.content;

import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.entities.TargetPriority;
import mindustry.entities.effect.MultiEffect;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.heat.HeatConductor;
import mindustry.world.blocks.production.BurstDrill;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawHeatInput;
import mindustry.world.draw.DrawHeatOutput;
import mindustry.world.draw.DrawMulti;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import wh.entities.bullet.PositionLightningBulletType;
import wh.graphics.WHPal;
import wh.world.blocks.distribution.CoveredConveyor;
import wh.world.blocks.distribution.HeatBelt;
import wh.world.blocks.production.SpecificMineralDrill;

import static mindustry.type.ItemStack.with;

public final class WHBlocks {
    public static Block promethium;
    public static Block vibraniumOre;
    public static Block steelDust;
    //turret
    public static Block flash, collapse;
    //Drill
    public static Block sb, sb2;

    private WHBlocks() {
    }

    public static void load() {
        promethium = new Floor("promethium");
        vibraniumOre = new OreBlock("vibranium-ore");
        steelDust = new CoveredConveyor("steel-dust");
        flash = new PowerTurret("Flash") {
            {
                this.shootType = new PositionLightningBulletType(50.0F) {
                    {
                        this.maxRange = 300.0F;
                        this.rangeOverride = 300.0F;
                        this.lightningColor = WHPal.rim3;
                        this.lightningDamage = 50.0F;
                        this.lightning = 3;
                        this.lightningLength = 6;
                        this.lightningLengthRand = 6;
                        this.hitEffect = WHFx.lightningSpark;
                    }
                };
            }
        };
        collapse = new ItemTurret("Collapse") {
            {
                this.ammo(new Object[]{WHItems.sealedPromethium, WHBullets.collapseSp, Items.phaseFabric, WHBullets.collaspsePf});
            }
        };
        sb = new SpecificMineralDrill("傻逼"){{
            requirements(Category.production, with(Items.silicon, 1));
            drillTime = 60f;
            size = 4;
            hasPower = true;
            tier = 1145;
            drillEffect = new MultiEffect(Fx.mineImpact, Fx.drillSteam, Fx.mineImpactWave.wrap(Pal.redLight, 40f));
            shake = 4f;
            itemCapacity = 40;
            researchCostMultiplier = 0.5f;
            drillMultipliers.put(Items.beryllium, 2.5f);
            fogRadius = 4;

            consumePower(160f / 60f);
        }};
        sb2 = new HeatBelt("Heat-Belt"){{
            requirements(Category.crafting, with(Items.tungsten, 10, Items.graphite, 10));
            drawer = new DrawMulti(new DrawDefault(), new DrawHeatOutput(), new DrawHeatInput("-heat"));
            researchCostMultiplier = 10f;
            rotate = true;
            group = BlockGroup.heat;
            size = 1;
            regionRotated1 = 1;
        }};
    }
}
