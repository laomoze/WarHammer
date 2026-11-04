package wh.entities.world.layers;

import arc.func.Cons;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.ai.Pathfinder;
import mindustry.game.EventType.*;
import mindustry.world.Tile;

/**
 * Owns the vanilla flowfield pathfinder and forwards only its layer's events.
 */
public final class LayerNativePathfinder implements LayerEvents.Listener {
    private final LayerEvents events;
    private final ObjectMap<Object, Cons<Object>> handlers;

    private LayerNativePathfinder(LayerEvents events, ObjectMap<Object, Cons<Object>> handlers) {
        this.events = events;
        this.handlers = handlers;
    }

    /**
     * Initializes the pathfinder while the given layer is active.
     */
    public static LayerNativePathfinder create(LayerContext context, LayerEvents events) {
        EngineEventCapture capture = new EngineEventCapture(WorldLoadEvent.class, ResetEvent.class,
                TileChangeEvent.class, TilePreChangeEvent.class, Trigger.afterGameUpdate);
        Pathfinder pathfinder;
        ObjectMap<Object, Cons<Object>> handlers;
        try {
            pathfinder = new Pathfinder();
        } finally {
            handlers = capture.finish();
        }
        context.setPathfinder(pathfinder);
        Vars.controlPath = new LayerControlPathfinder(events);
        LayerNativePathfinder layer = new LayerNativePathfinder(events, handlers);
        context.onClose(layer::close);
        handlers.get(WorldLoadEvent.class).get(new WorldLoadEvent());
        events.add(layer);
        return layer;
    }

    @Override
    public void preChange(Tile tile) {
        handlers.get(TilePreChangeEvent.class).get(new TilePreChangeEvent().set(tile));
    }

    @Override
    public void changed(Tile tile) {
        handlers.get(TileChangeEvent.class).get(new TileChangeEvent().set(tile));
    }

    @Override
    public void afterUpdate() {
        handlers.get(Trigger.afterGameUpdate).get(null);
    }

    private void close() {
        events.remove(this);
        handlers.get(ResetEvent.class).get(new ResetEvent());
    }
}
