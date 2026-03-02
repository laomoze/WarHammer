//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.core;

import arc.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.net.*;
import wh.content.*;
import wh.entities.*;
import wh.entities.event.logic.*;
import wh.entities.event.objective.*;
import wh.entities.event.ui.*;
import wh.gen.*;
import wh.graphics.*;
import wh.net.packet.*;

public class WarHammerMod extends Mod {
    public static String ModName = "wh";

    public WarHammerMod() {
        Net.registerPacket(WarnHUDPacket::new);
        Net.registerPacket(AlertToastPacket::new);
        WHClassMap.load();
        WHSettings.load();
        Events.on(EventType.FileTreeInitEvent.class, (e) -> {
            if (!Vars.headless) {
                WHSounds.load();
                Core.app.post(() -> {
                    WHShaders.init();
                    MainRenderer.init();
                });
            }
        });

        // HUD tree is rebuilt after world load; remount objective panel with a short delay.
        Events.on(EventType.WorldLoadEvent.class, e -> {
            if(!Vars.headless){
                Core.app.post(() -> arc.util.Time.runTask(10f, WHObjectiveUI::init));
            }
        });

        // Keep objective panel alive if another UI rebuild removes it.
        Events.run(EventType.Trigger.update, () -> {
            if(!Vars.headless){
                ActionContext.cutsceneUI.update();
                WHObjectiveUI.ensureMounted();
            }
        });

        // Draw world-space cutscene markers (e.g. raid target icon + ring progress).
        Events.run(EventType.Trigger.draw, () -> {
            if(!Vars.headless){
                ActionContext.cutsceneUI.drawMarks();
            }
        });
    }

    public static String name(String add) {
        return ModName + "-" + add;
    }

    @Override
    public void loadContent() {
        super.loadContent();
        WorldRegister.load();
        EntityRegister.load();
        WHContent.loadPriority();

        WHItems.load();
        WHLiquids.load();
        WHStatusEffects.load();
        WHBulletsOther.load();
        WHBullets.load();
        WHUnitTypes.load();
        WHBlocksEnvironment.load();
        WHBlocks.load();

        WHPlanets.load();
        WHTechTree.load();

        WHOverride.load();
        WHEvents.load();
        WHAutoTriggerSetup.load();
        WHLogicStatements.load();
        WHObjectiveRegistry.load();

    }
}
