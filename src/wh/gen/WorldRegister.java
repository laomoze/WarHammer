//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.gen;

import arc.Core;
import arc.Events;
import arc.struct.*;
import mindustry.core.GameState.State;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import wh.entities.world.blocks.defense.*;
import wh.entities.world.blocks.defense.AirRaiderCallBlock.*;
import wh.entities.world.blocks.defense.CommandableBlock.*;
import wh.graphics.*;

import static wh.graphics.MainRenderer.renderer;

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
