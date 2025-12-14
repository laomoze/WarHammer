//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.core;

import arc.Core;
import arc.Events;
import arc.util.Time;
import java.util.Objects;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.ResearchDialog;
import wh.content.*;
import wh.gen.EntityRegister;
import wh.content.WHSounds;
import wh.entities.world.entities.WorldRegister;
import wh.graphics.MainRenderer;
import wh.graphics.WHShaders;
import wh.ui.dialogs.WHResearchDialog;

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
        WHOverride.load();

        WHPlanets.load();
        WHTechTree.load();

    }
}
