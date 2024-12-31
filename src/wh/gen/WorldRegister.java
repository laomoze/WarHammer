package wh.gen;

import arc.*;
import arc.struct.*;
import mindustry.core.*;
import mindustry.game.EventType.*;
import wh.world.blocks.defense.CommandableBlock.*;

import static arc.Core.*;

public final class WorldRegister {
    public static final Seq<Runnable> afterLoad = new Seq<>();

    public static final Seq<CommandableBlockBuild> commandableBuilds = new Seq<>();

    public static boolean worldLoaded = false;

    /** WorldRegister should not be instantiated. */
    private WorldRegister() {}

    public static void postAfterLoad(Runnable runnable) {
        if (worldLoaded) afterLoad.add(runnable);
    }

    public static void load() {
        Events.on(ResetEvent.class, e -> {
            commandableBuilds.clear();

            worldLoaded = true;
        });

        Events.on(WorldLoadEvent.class, e -> {
            app.post(() -> {
                worldLoaded = false;
            });
        });

        Events.on(StateChangeEvent.class, e -> {
            if (e.to == GameState.State.menu) {
                worldLoaded = true;
            }
        });
    }
}
