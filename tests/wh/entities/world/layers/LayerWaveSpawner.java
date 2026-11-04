package wh.entities.world.layers;

import arc.func.Cons;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.ai.WaveSpawner;
import mindustry.game.EventType.TileOverlayChangeEvent;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

/**
 * Holds spawn positions and notifications for one layer.
 */
public final class LayerWaveSpawner implements LayerEvents.Listener {
    private final WaveSpawner spawner;
    private final Cons<Object> overlayHandler;

    private LayerWaveSpawner(WaveSpawner spawner, Cons<Object> overlayHandler) {
        this.spawner = spawner;
        this.overlayHandler = overlayHandler;
    }

    public static LayerWaveSpawner create(LayerEvents events) {
        EngineEventCapture capture = new EngineEventCapture(WorldLoadEvent.class, TileOverlayChangeEvent.class);
        ObjectMap<Object, Cons<Object>> handlers;
        WaveSpawner spawner;
        try {
            spawner = new WaveSpawner();
        } finally {
            handlers = capture.finish();
        }
        Vars.spawner = spawner;
        handlers.get(WorldLoadEvent.class).get(new WorldLoadEvent());
        LayerWaveSpawner layer = new LayerWaveSpawner(spawner, handlers.get(TileOverlayChangeEvent.class));
        events.add(layer);
        return layer;
    }

    public WaveSpawner spawner() {
        return spawner;
    }

    @Override
    public void overlayChanged(Tile tile, Floor previous, Floor overlay) {
        overlayHandler.get(new TileOverlayChangeEvent().set(tile, previous, overlay));
    }
}
