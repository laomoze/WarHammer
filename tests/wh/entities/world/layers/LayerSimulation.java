package wh.entities.world.layers;

import arc.util.Time;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.gen.Groups;

import java.util.Objects;

/**
 * Advances one layer's entity groups using the main world's frame clock.
 */
public final class LayerSimulation {
    private LayerSimulation() {
    }

    public static void update(LayerContext layer, GameState main) {
        Objects.requireNonNull(layer, "layer");
        Objects.requireNonNull(main, "main");
        if (!main.isGame() || main.isPaused()) return;
        layer.run(() -> {
            GameState local = Vars.state;
            local.rules = main.rules;
            local.map = main.map;
            local.wave = main.wave;
            local.wavetime = main.wavetime;
            local.tick = main.tick;
            local.updateId = main.updateId;
            local.set(main.getState());
            local.envAttrs.clear();
            local.envAttrs.add(local.rules.attributes);
            Groups.weather.each(weather -> local.envAttrs.add(weather.weather.attrs, weather.opacity));

            Vars.unitPhysics.begin();
            Vars.avoidance.init();
            Vars.avoidance.begin();
            if (Vars.avoidance.shouldProcess()) Vars.avoidance.process();

            double mainTime = Time.getInternalTime();
            try {
                Time.setInternalTime(mainTime - Time.delta);
                Time.update();
            } finally {
                Time.setInternalTime(mainTime);
            }

            Groups.updatePooling();
            Groups.bullet.updatePhysics();
            Groups.unit.updatePhysics();
            Groups.player.update();
            Groups.effect.update();
            boolean editor = local.isEditor();
            if (!editor) Groups.all.update();
            if (editor) Groups.unit.update(unit -> unit.isPlayer() || unit.spawnedByCore);
            else Groups.unit.update();
            if (!editor) {
                Groups.powerGraph.update();
                Groups.build.update();
                Groups.bullet.update();
                Groups.bullet.collide();
            }
            layer.afterUpdate();
        });
    }
}
