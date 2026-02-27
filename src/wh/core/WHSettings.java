package wh.core;

import arc.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;

public final class WHSettings{
    public static final String distortionEnabledKey = "wh-distortion-enabled";
    public static final String distortionStrengthKey = "wh-distortion-strength";
    private static final String categoryName = "WarHammer设置";

    private WHSettings(){
    }

    public static void load(){
        Events.on(ClientLoadEvent.class, event -> Core.app.post(WHSettings::register));
    }

    private static void register(){
        if(Vars.headless || Vars.ui == null || Vars.ui.settings == null) return;

        Vars.ui.settings.addCategory(categoryName, Icon.settings, table -> {
            table.checkPref(distortionEnabledKey, true);
            table.sliderPref(distortionStrengthKey, 100, 0, 100, 5, i -> i + "%");
        });
    }

    public static boolean distortionEnabled(){
        return Core.settings.getBool(distortionEnabledKey, true);
    }

    public static float distortionStrengthScale(){
        return Core.settings.getInt(distortionStrengthKey, 100) / 100f;
    }
}
