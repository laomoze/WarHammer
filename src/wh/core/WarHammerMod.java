//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.core;

import arc.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.mod.*;
import wh.content.*;
import wh.entities.*;
import wh.entities.event.logic.*;
import wh.gen.*;
import wh.graphics.*;

public class WarHammerMod extends Mod {
    public static String ModName = "wh";

    public WarHammerMod() {
        WHClassMap.load();
        Events.on(EventType.FileTreeInitEvent.class, (e) -> {
            if (!Vars.headless) {
                WHSounds.load();
                Core.app.post(() -> {
                    WHShaders.init();
                    MainRenderer.init();
                });
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

        WHEvents.load();
        WHAutoTriggerSetup.load();
        WHLogicStatements.load();
        WHOverride.load();

    }
}
