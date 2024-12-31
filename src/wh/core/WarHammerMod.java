package wh.core;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import wh.content.*;
import wh.gen.*;
import wh.graphics.*;
import wh.ui.dialogs.*;

import java.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class WarHammerMod extends Mod {
    public WarHammerMod() {
        WHClassMap.load();

        Events.on(FileTreeInitEvent.class, e -> {
            if (!headless) {
                WHSounds.load();
                app.post(WHShaders::init);
            }
        });
    }

    @Override
    public void init() {
        //Replace the original technology tree
        WHResearchDialog dialog = new WHResearchDialog();
        ResearchDialog research = ui.research;
        research.shown(() -> {
            dialog.show();
            Objects.requireNonNull(research);
            Time.runTask(1f, research::hide);
        });
    }
}
