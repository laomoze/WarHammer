package wh.core;

import arc.Core;
import arc.Events;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;

public final class WHSettings{
    public static final String effectEnabledKey = "wh-effect-enabled";
    public static final String regularEffectScaleKey = "wh-regular-effect-scale";
    public static final String distortionEnabledKey = "wh-distortion-enabled";
    public static final String distortionStrengthKey = "wh-distortion-strength";
    public static final String carrierDebugHudKey = "wh-carrier-debug-hud";
    public static final String psychicDebugHudKey = "wh-psychic-debug-hud";
    public static final String fullTechCoverageKey = "wh-full-tech-coverage";
    private static final String categoryName = "WarHammer设置";

    private WHSettings(){
    }

    public static void load(){
        initDefaults();
        if (Vars.headless) {
            forceMultiplayerSettings();
        }
        Events.on(ClientLoadEvent.class, event -> Core.app.post(WHSettings::register));
    }

    private static void register(){
        if(Vars.headless || Vars.ui == null || Vars.ui.settings == null) return;

        Vars.ui.settings.addCategory(categoryName, Icon.settings, table -> {
            table.checkPref(effectEnabledKey, true);
            table.sliderPref(regularEffectScaleKey, 100, 25, 100, 5, i -> i + "%");
            table.checkPref(distortionEnabledKey, true);
            table.sliderPref(distortionStrengthKey, 100, 0, 100, 5, i -> i + "%");
            table.checkPref(carrierDebugHudKey, false);
            table.checkPref(psychicDebugHudKey, false);
            table.checkPref(fullTechCoverageKey, false);
        });
    }

    private static void initDefaults() {
        setDefault(effectEnabledKey, true);
        setDefault(regularEffectScaleKey, 100);
        setDefault(distortionEnabledKey, true);
        setDefault(distortionStrengthKey, 100);
        setDefault(carrierDebugHudKey, false);
        setDefault(psychicDebugHudKey, false);
        setDefault(fullTechCoverageKey, false);
    }

    private static void setDefault(String key, Object value) {
        if (!Core.settings.has(key)) {
            Core.settings.put(key, value);
        }
    }

    public static void forceMultiplayerSettings() {
        if (Vars.headless) {
            Core.settings.put(fullTechCoverageKey, true);
        }
    }

    public static String overrideStatus() {
        if (!Vars.headless) return "";
        StringBuilder builder = new StringBuilder();
        builder.append(fullTechCoverageKey).append(":true|");
        return builder.toString();
    }

    public static String mismatchedSettings(String status) {
        if (status == null || status.isEmpty()) return "";

        StringBuilder mismatched = new StringBuilder();
        for (String entry : status.split("\\|")) {
            if (entry.isEmpty()) continue;

            int split = entry.indexOf(':');
            if (split <= 0 || split >= entry.length() - 1) continue;

            String key = entry.substring(0, split);
            boolean requiredValue = Boolean.parseBoolean(entry.substring(split + 1));
            if (Core.settings.getBool(key, false) != requiredValue) {
                if (mismatched.length() > 0) {
                    mismatched.append("\n");
                }
                mismatched.append(settingDisplayName(key));
            }
        }
        return mismatched.toString();
    }

    private static String settingDisplayName(String key) {
        String bundleKey = "setting." + key + ".name";
        if (Core.bundle != null && Core.bundle.has(bundleKey)) {
            return Core.bundle.get(bundleKey);
        }
        return key;
    }

    public static boolean effectEnabled(){
        return Core.settings.getBool(effectEnabledKey, true);
    }

    public static int regularEffectPercent(){
        return Mathf.clamp(Core.settings.getInt(regularEffectScaleKey, 100), 10, 100);
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

    public static boolean carrierDebugHud(){
        return Core.settings.getBool(carrierDebugHudKey, false);
    }

    public static boolean psychicDebugHud() {
        return Core.settings.getBool(psychicDebugHudKey, false);
    }

    public static boolean fullTechCoverage() {
        return Core.settings.getBool(fullTechCoverageKey, false);
    }

    public static boolean converterEnabled() {
        return fullTechCoverage();
    }

    public static boolean laserDebugLengths() {
        return carrierDebugHud();
    }
}
