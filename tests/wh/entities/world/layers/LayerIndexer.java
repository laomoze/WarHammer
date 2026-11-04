package wh.entities.world.layers;

import arc.func.Cons;
import arc.struct.ObjectMap;
import mindustry.ai.BlockIndexer;
import mindustry.game.EventType.TileChangeEvent;
import mindustry.game.EventType.TileFloorChangeEvent;
import mindustry.game.EventType.TilePreChangeEvent;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

/**
 * Adapts one vanilla block indexer's listeners to one layer.
 */
public final class LayerIndexer implements LayerEvents.Listener {
    private static final Object[] eventTypes = {
            TilePreChangeEvent.class, TileChangeEvent.class, TileFloorChangeEvent.class, WorldLoadEvent.class
    };

    private final BlockIndexer indexer;
    private final ObjectMap<Object, Cons<Object>> handlers;

    private LayerIndexer(BlockIndexer indexer, ObjectMap<Object, Cons<Object>> handlers) {
        this.indexer = indexer;
        this.handlers = handlers;
    }

    /**
     * Must be called while the layer context is active on the game thread.
     */
    public static LayerIndexer create(LayerEvents events) {
        EngineEventCapture capture = new EngineEventCapture(eventTypes);
        ObjectMap<Object, Cons<Object>> captured;
        BlockIndexer indexer;
        try {
            indexer = new BlockIndexer();
        } finally {
            captured = capture.finish();
        }
        LayerIndexer adapter = new LayerIndexer(indexer, captured);
        adapter.handlers.get(WorldLoadEvent.class).get(new WorldLoadEvent());
        events.add(adapter);
        return adapter;
    }

    public BlockIndexer indexer() {
        return indexer;
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
    public void floorChanged(Tile tile, Floor previous, Floor floor) {
        handlers.get(TileFloorChangeEvent.class).get(new TileFloorChangeEvent().set(tile, previous, floor));
    }

}
