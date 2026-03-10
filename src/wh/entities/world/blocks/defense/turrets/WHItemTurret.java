package wh.entities.world.blocks.defense.turrets;

import arc.*;
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
    }

    @Override
    public void init(){
        super.init();
        armor = 3 * size;
    }

    @Override
    public void setStats(){
        super.setStats();
        String specialText = special;
        String specialKey = "block." + name + ".special";
        String bundleSpecial = Core.bundle.get(specialKey);
        if(bundleSpecial != null && !bundleSpecial.equals(specialKey)){
            specialText = bundleSpecial;
        }
        final String finalSpecialText = specialText;
        if(finalSpecialText != null && !finalSpecialText.isEmpty() && !finalSpecialText.equals("_")){
            stats.add(Stat.abilities, t -> {
                t.row();
                t.add("[gray]" + (unlocked() ? finalSpecialText : Iconc.lock + " " + Core.bundle.get("unlock.incampaign")))
                .pad(6).padTop(4).width(400f).wrap().fillX();
            });
        }
        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, UIUtils.ammo(ammoTypes));
    }
}
