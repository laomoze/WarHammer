package wh.entities.world.layers;

import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.gen.Groups;
import mindustry.net.Net;
import mindustry.world.Tile;
import mindustry.world.Tiles;

import java.io.DataInput;
import java.io.IOException;
import java.util.Objects;

/**
 * Creates and restores a layer using the terrain of the main world.
 */
public final class LayerWorld {
    private LayerWorld() {
    }

    public static LayerContext create(String id, Tiles surface, GameState main, Net network) throws IOException {
        LayerArchive.validateId(id);
        Objects.requireNonNull(surface, "surface");
        LayerEvents events = new LayerEvents();
        Tiles tiles = LayerTile.createTiles(surface.width, surface.height, events);
        LayerContext layer = LayerContext.create(tiles, newGame(main), null, null, null, events);
        boolean completed = false;
        try {
            layer.run(() -> {
                Vars.world.setGenerating(true);
                for (int i = 0; i < surface.width * surface.height; i++) {
                    Tile source = surface.geti(i);
                    Tile target = tiles.geti(i);
                    target.setFloor(source.floor());
                    target.setOverlay(source.overlay());
                    if (source.build == null && source.block().isStatic()) target.setBlock(source.block());
                }
                resizeGroups();
                initialize(layer, events, id, network);
                Vars.world.setGenerating(false);
            });
            completed = true;
            return layer;
        } finally {
            if (!completed) layer.close();
        }
    }

    public static LayerContext read(String id, DataInput input, GameState main, Net network) throws IOException {
        LayerArchive.validateId(id);
        LayerEvents events = new LayerEvents();
        return LayerSnapshotCodec.read(input, newGame(main), null, null, null, events,
                layer -> initialize(layer, events, id, network));
    }

    private static void initialize(LayerContext layer, LayerEvents events, String id, Net network) {
        if (!(Vars.pathfinder instanceof LayerPathRouter)) {
            throw new IllegalStateException("The surface path router must be installed before creating layers");
        }
        if (network != null && network.server()) Vars.net = new LayerNet(network, id);
        Vars.avoidance.init();
        Vars.indexer = LayerIndexer.create(events).indexer();
        LayerWaveSpawner.create(events);
        LayerNativePathfinder.create(layer, events);
    }

    private static void resizeGroups() {
        Groups.resize(-Vars.finalWorldBounds, -Vars.finalWorldBounds,
                Vars.world.width() * Vars.tilesize + Vars.finalWorldBounds * 2,
                Vars.world.height() * Vars.tilesize + Vars.finalWorldBounds * 2);
    }

    private static GameState newGame(GameState main) {
        Objects.requireNonNull(main, "main");
        GameState game = new GameState();
        game.rules = main.rules;
        game.map = main.map;
        game.wave = main.wave;
        game.wavetime = main.wavetime;
        game.tick = main.tick;
        game.updateId = main.updateId;
        return game;
    }
}
