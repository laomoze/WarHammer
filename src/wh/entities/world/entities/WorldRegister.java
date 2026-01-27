
package wh.entities.world.entities;

import arc.*;
import arc.struct.*;
import mindustry.game.*;
import wh.entities.world.blocks.defense.AirRaiderCallBlock.*;
import wh.entities.world.blocks.defense.CommandableBlock.*;

public class WorldRegister {
    public static final Seq<Runnable> afterLoad = new Seq<>();
    public static final Seq<CommandableBlockBuild> commandableBuilds = new Seq<>();
    public static final Seq<AirRaiderUnitBuild> ARBuilds = new Seq<>();

    public static boolean worldLoaded = false;

    private WorldRegister() {
    }


    public static void clear() {
        commandableBuilds.clear();
        ARBuilds.clear();
    }

    public static void load() {
        Events.on(EventType.ResetEvent.class, (e) -> {
            WorldRegister.clear();
            worldLoaded = true;
        });
    }
}
