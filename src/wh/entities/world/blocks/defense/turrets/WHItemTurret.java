package wh.entities.world.blocks.defense.turrets;

import arc.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import wh.graphics.*;
import wh.ui.*;

public class WHItemTurret extends ItemTurret{
    public @Nullable String special;

    public WHItemTurret(String name){
        super(name);
        fogRadiusMultiplier = 0.35f;

        outlineColor = WHPal.Outline;
        outlineRadius = 3;
        squareSprite = false;
    }

    public static void intTurret(Turret turret){
        turret.armor = 3 * turret.size;
        turret.researchCostMultiplier = Mathf.clamp(1.4f - 0.04f * turret.size * turret.size, 0.2f, 1.5f);
        turret.depositCooldown = turret.size * 0.5f + 1;
        turret.buildCostMultiplier = Mathf.clamp(4.5f - turret.size * 0.7f, 0.7f, 4);
        turret.scaledHealth = 10 * turret.size + 40;
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
        String specialText = special;
        String specialKey = "block." + name + ".special";
        String bundleSpecial = Core.bundle.has(specialKey) ? Core.bundle.get(specialKey) : null;
        if(bundleSpecial != null && !bundleSpecial.isEmpty() && !isMissingBundleText(bundleSpecial)){
            specialText = bundleSpecial;
        }
        final String finalSpecialText = specialText;
        if(finalSpecialText != null && !finalSpecialText.isEmpty() && !finalSpecialText.equals("_") && !isMissingBundleText(finalSpecialText)){
            stats.add(Stat.abilities, t -> {
                t.row();
                t.add("[gray]" + (unlocked() ? finalSpecialText : Iconc.lock + " " + Core.bundle.get("unlock.incampaign")))
                .pad(6).padTop(4).width(400f).wrap().fillX();
            });
        }
        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, UIUtils.ammo(ammoTypes));
    }

    private static boolean isMissingBundleText(String text){
        return text.startsWith("???") && text.endsWith("???");
    }
}
