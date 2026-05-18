//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.core;

import arc.Core;
import arc.Events;
import arc.func.Prov;
import arc.scene.ui.Button;
import arc.scene.ui.Label;
import arc.util.Align;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.io.JsonIO;
import mindustry.maps.Maps;
import mindustry.maps.filters.GenerateFilter;
import mindustry.mod.Mod;
import mindustry.net.Net;
import mindustry.ui.Styles;
import mindustry.ui.WarningBar;
import mindustry.ui.dialogs.BaseDialog;
import wh.content.*;
import wh.entities.WorldRegister;
import wh.entities.event.logic.WHLogicStatements;
import wh.entities.event.objective.WHObjectiveUI;
import wh.entities.event.ui.ActionContext;
import wh.gen.EntityRegister;
import wh.graphics.MainRenderer;
import wh.graphics.WHShaders;
import wh.maps.filters.WhTechFilter;
import wh.net.packet.*;

import java.util.Arrays;

public class WarHammerMod extends Mod {
    public static String ModName = "wh";
    private static final String qqGroupId = "316481519";
    private static final String qqGroupWebLink = "https://qun.qq.com/";
    private static final String qqButtonName = "wh-moddetail-qq-button";
    private static final String settingsButtonName = "wh-moddetail-settings-button";
    private static final String forcedSettingPacketName = "wh-forced-setting-check";
    private static final long modDetailPollIntervalMs = 160L;
    private static long nextModDetailPollMs = 0L;
    private static BaseDialog lastSeenDialog = null;

    public WarHammerMod() {
        Net.registerPacket(WarnHUDPacket::new);
        Net.registerPacket(AlertToastPacket::new);
        Net.registerPacket(GeminiSpecialBulletPacket::new);
        Net.registerPacket(RevengeOrbitCreatePacket::new);
        Net.registerPacket(RevengeOrbitBulletPacket::new);
        /* WHClassMap.load();*/
        WHSettings.load();
        setupMultiplayerSettingSync();
        registerEditorGenerateFilters();
        Events.on(EventType.FileTreeInitEvent.class, (e) -> {
            if (!Vars.headless) {
                WHSounds.load();
                Core.app.post(() -> {
                    WHShaders.init();
                    MainRenderer.init();
                    // HUD tree is rebuilt after world load; remount objective panel with a short delay.
                    Time.runTask(10f, WHObjectiveUI::init);
                });
            }
        });

        // If a cutscene/UI action hid vanilla HUD and flow exited early, recover on map enter.
        Events.on(EventType.WorldLoadEvent.class, e -> {
            if(!Vars.headless){
                restoreVanillaHud();
            }
        });

        // Keep objective panel alive if another UI rebuild removes it.
        Events.run(EventType.Trigger.update, () -> {
            if(!Vars.headless){
                ActionContext.cutsceneUI.update();
                WHObjectiveUI.ensureMounted();
                pollModDetailInjection();
            }
        });

        Events.run(Trigger.draw, () -> {
            if(!Vars.headless){
                ActionContext.cutsceneUI.drawMarks();
            }
        });
    }

    private static void setupMultiplayerSettingSync() {
        Events.on(EventType.PlayerConnect.class, event -> {
            WHSettings.forceMultiplayerSettings();
            if (WHSettings.fullTechCoverage()) {
                KarvexTeachTree.forceFullTechCoverage();
            }
            Call.clientPacketReliable(forcedSettingPacketName, WHSettings.overrideStatus());
        });

        if (!Vars.headless) {
            Vars.netClient.addPacketHandler(forcedSettingPacketName, WarHammerMod::handleForcedSettingPacket);
        }
    }

    private static void handleForcedSettingPacket(String status) {
        try {
            String mismatched = WHSettings.mismatchedSettings(status);
            if (mismatched.isEmpty()) return;

            if (Vars.ui != null) {
                Vars.ui.showInfo(bundleFormat(
                        "wh.settings.multiplayer.require",
                        "当前房间要求启用以下设置：\n{0}\n请在模组设置里开启后重连。",
                        mismatched
                ));
            }
            Vars.net.disconnect();
        } catch (Throwable t) {
            Log.err(t);
            Vars.net.disconnect();
        }
    }

    public static String name(String add) {
        return ModName + "-" + add;
    }

    private static void restoreVanillaHud(){
        if(Vars.ui != null && Vars.ui.hudfrag != null){
            Vars.ui.hudfrag.shown = true;
        }
    }

    private static void pollModDetailInjection(){
        if(Vars.ui == null || Core.scene == null) return;

        long now = Time.millis();
        if(now < nextModDetailPollMs) return;
        nextModDetailPollMs = now + modDetailPollIntervalMs;

        if(!(Core.scene.getDialog() instanceof BaseDialog dialog)){
            lastSeenDialog = null;
            return;
        }

        if(dialog == lastSeenDialog) return;
        lastSeenDialog = dialog;
        injectModDetailButtons(dialog);
    }

    private static void injectModDetailButtons(BaseDialog dialog){
        if(dialog.buttons == null || dialog.title == null) return;

        String title = Strings.stripColors(String.valueOf(dialog.title.getText())).trim();
        if(title.isEmpty()) return;
        if(!title.equalsIgnoreCase("WarHammer") && !title.equalsIgnoreCase("wh")) return;

        if(dialog.buttons.find(qqButtonName) == null){
            Button button = dialog.buttons.button(bundle("wh.moddetail.button.qq", "QQ群"), Icon.link, WarHammerMod::openQQGroup).size(210f, 64f).get();
            button.name = qqButtonName;
        }

        if(dialog.buttons.find(settingsButtonName) == null){
            Button button = dialog.buttons.button(bundle("wh.moddetail.button.settings", "模组设定"), Icon.settings, WarHammerMod::openModSettings).size(210f, 64f).get();
            button.name = settingsButtonName;
        }
    }

    private static void openQQGroup(){
        if(Vars.ui == null) return;
        Core.app.setClipboardText(qqGroupId);
        if(!Core.app.openURI(bundle("wh.moddetail.qq.link", qqGroupWebLink))){
            Core.app.setClipboardText(qqGroupId);
            Vars.ui.showInfoFade(bundleFormat("wh.moddetail.qq.copied", "无法直接打开QQ群链接，已复制群号: {0}", qqGroupId));
            return;
        }
        Vars.ui.showInfoFade(bundleFormat("wh.moddetail.qq.opened", "已打开QQ群链接，并复制群号: {0}", qqGroupId));
    }

    private static void openModSettings(){
        if(Vars.ui == null) return;

        BaseDialog dialog = new BaseDialog(bundle("wh.moddetail.settings.dialogTitle", "模组设定"));
        dialog.addCloseButton();

        dialog.cont.margin(16f);
        dialog.cont.table(head -> {
            head.top();
            head.add(new WarningBar()).growX().height(18f).row();
            Label titleLabel = head.add(bundle("wh.moddetail.settings.title", "[[ HUD::MOD SETTINGS ]]"))
            .style(Styles.techLabel).growX().center().padTop(2f).get();
            titleLabel.setFontScale(2f);
            titleLabel.setAlignment(Align.center);
            head.row();
            head.add(bundle("wh.moddetail.settings.subtitle", "滚木"))
            .style(Styles.outlineLabel).growX().center().padBottom(2f).row();
            head.add(new WarningBar()).growX().height(18f).row();
        }).growX().maxWidth(980f).padBottom(8f).row();

        dialog.cont.pane(Styles.smallPane, t -> {
            t.top().left();
            t.defaults().growX().left();

            t.add(bundle("wh.moddetail.settings.text", "[lightgray]设定内容加载中...[]"))
            .wrap()
            .left();
        }).grow().maxWidth(980f).maxHeight(640f);

        dialog.show();
        Vars.ui.showInfoFade(bundle("wh.moddetail.settings.hint", "已打开模组设定"));
    }

    private static String bundle(String key, String fallback){
        if(Core.bundle != null && Core.bundle.has(key)){
            return Core.bundle.get(key);
        }
        return fallback;
    }

    private static String bundleFormat(String key, String fallback, Object... args){
        if(Core.bundle != null && Core.bundle.has(key)){
            return Core.bundle.format(key, args);
        }
        String out = fallback;
        for(int i = 0; i < args.length; i++){
            out = out.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return out;
    }

    private static void registerEditorGenerateFilters() {
        if (Vars.headless) return;

        try {
            JsonIO.classTag("whTech", WhTechFilter.class);

            for (Prov<GenerateFilter> provider : Maps.allFilterTypes) {
                if (provider == null) continue;
                try {
                    if (provider.get() instanceof WhTechFilter) {
                        return;
                    }
                } catch (Throwable ignored) {
                }
            }

            Prov<GenerateFilter>[] appended = Arrays.copyOf(Maps.allFilterTypes, Maps.allFilterTypes.length + 1);
            appended[appended.length - 1] = WhTechFilter::new;
            Maps.allFilterTypes = appended;
        } catch (Throwable t) {
            Log.err(t);
        }
    }

    @Override
    public void loadContent() {
        super.loadContent();
        WorldRegister.load();
        EntityRegister.load();
        if (!Vars.headless) {
            WHContent.loadPriority();
        }

        WHItems.load();
        WHLiquids.load();
        WHStatusEffects.load();
        WHWeathers.load();
        WHTeams.load();
        WHBulletsOther.load();
        WHBullets.load();
        WHUnitTypes.load();
        WHBlocksEnvironment.load();
        WHBlocks.load();

        WHEvents.load();
        WHAutoTriggerSetup.load();
        WHLogicStatements.load();
        WHObjectiveRegistry.load();

        WHPlanets.load();
        KarvexTeachTree.load();
        WHOverride.load();
    }
}
