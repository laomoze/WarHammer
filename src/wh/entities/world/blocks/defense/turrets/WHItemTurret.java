package wh.entities.world.blocks.defense.turrets;

import arc.math.Mathf;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.Turret;
import wh.graphics.WHPal;

public class WHItemTurret extends ItemTurret{

    public WHItemTurret(String name){
        super(name);
        fogRadiusMultiplier = 0.35f;

        outlineColor = WHPal.Outline;
        outlineRadius = 3;
        squareSprite = false;
    }

    public static void intTurret(Turret turret){
        turret.fogRadiusMultiplier = 0.35f;

        turret.outlineColor = WHPal.Outline;
        turret.outlineRadius = 3;
        turret.squareSprite = false;

        turret.armor = 3 * turret.size;
        turret.researchCostMultiplier = Mathf.clamp(1.4f - 0.04f * turret.size * turret.size, 0.2f, 1.5f);
        turret.depositCooldown = turret.size * 0.5f + 1;
        turret.buildCostMultiplier = Mathf.clamp(4.5f - turret.size * 0.7f, 0.7f, 4);
        turret.scaledHealth = 4 * turret.size * turret.size + 40 + 2.5f * turret.size;
        float scaling = 1f;
        for(var stack : turret.requirements){
            scaling += stack.item.healthScaling;
        }
        turret.scaledHealth *= scaling;
    }

    @Override
    public void init(){
        intTurret(this);
        super.init();
    }

    @Override
    public void setStats(){
        super.setStats();
    }
}
