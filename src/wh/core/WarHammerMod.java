//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.core;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import wh.content.*;
import wh.entities.world.entities.*;
import wh.gen.*;
import wh.graphics.*;
import wh.ui.dialogs.*;

import java.util.*;

public class WarHammerMod extends Mod {
    public static String ModName = "wh";

    public WarHammerMod() {
        WHClassMap.load();
        Events.on(EventType.FileTreeInitEvent.class, (e) -> {
            if (!Vars.headless) {
                WHSounds.load();
                Core.app.post(WHShaders::init);
            }
        });
    }

    public static String name(String add) {
        return ModName + "-" + add;
    }


    @Override
    public void init() {
        MainRenderer.init();
        WHResearchDialog dialog = new WHResearchDialog();
        ResearchDialog research = Vars.ui.research;
        research.shown(() -> {
            dialog.show();
            Objects.requireNonNull(research);
            Time.runTask(1.0F, research::hide);
        });
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
        WHOverride.load();

    }
}
