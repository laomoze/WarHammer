package wh.core;

import arc.*;
import arc.math.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;

public final class WHSettings{
    public static final String effectEnabledKey = "wh-effect-enabled";
    public static final String regularEffectScaleKey = "wh-regular-effect-scale";
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
            table.checkPref(effectEnabledKey, true);
            table.sliderPref(regularEffectScaleKey, 100, 25, 100, 5, i -> i + "%");
            table.checkPref(distortionEnabledKey, true);
            table.sliderPref(distortionStrengthKey, 100, 0, 100, 5, i -> i + "%");
        });
    }

    public static boolean effectEnabled(){
        return Core.settings.getBool(effectEnabledKey, true);
    }

    public static int regularEffectPercent(){
        return Mathf.clamp(Core.settings.getInt(regularEffectScaleKey, 100), 25, 100);
    }

    public static float regularEffectScale(){
        return regularEffectPercent() / 100f;
    }

    public static int detailCount(int fullValue, int minValue){
        if(fullValue <= 0) return 0;
        return Math.max(minValue, Mathf.ceil(fullValue * regularEffectScale()));
    }

    public static boolean distortionEnabled(){
        return Core.settings.getBool(distortionEnabledKey, true);
    }

    public static float distortionStrengthScale(){
        return Core.settings.getInt(distortionStrengthKey, 100) / 100f;
    }
}
