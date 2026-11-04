package wh.entities.world.layers;

import mindustry.Vars;
import mindustry.ai.BlockIndexer;
import mindustry.ai.ControlPathfinder;
import mindustry.ai.Pathfinder;
import mindustry.core.GameState;
import mindustry.gen.Groups;
import mindustry.io.SaveIO;
import mindustry.io.SaveReadState;
import mindustry.io.SaveVersion;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.WorldContext;

import java.io.*;
import java.util.Objects;

/**
 * Encodes a layer inside a surrounding save or initial world stream.
 */
public final class LayerSnapshotCodec {
    private static final int magic = 0x57484c59;
    private static final int format = 1;
    private static final int maxBytes = 128 * 1024 * 1024;
    private static final int maxDimension = 8192;
    private static final int maxTiles = 16 * 1024 * 1024;

    private LayerSnapshotCodec() {
    }

    /**
     * Tile and content IDs are interpreted using the enclosing save's content mapping.
     */
    public static void write(DataOutput output, LayerContext layer) throws IOException {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(layer, "layer");
        SaveVersion writer = SaveIO.getSaveWriter();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            layer.run(() -> {
                try {
                    DataOutputStream data = new DataOutputStream(bytes);
                    writer.writeMap(data);
                    writer.writeEntities(data);
                    data.flush();
                } catch (IOException failure) {
                    throw new UncheckedIOException(failure);
                }
            });
        } catch (UncheckedIOException failure) {
            throw failure.getCause();
        }
        if (bytes.size() > maxBytes) throw new IOException("Layer snapshot is too large");
        output.writeInt(magic);
        output.writeInt(format);
        output.writeInt(writer.version);
        output.writeInt(bytes.size());
        output.write(bytes.toByteArray());
    }

    /**
     * Reads into a new context; the caller publishes it only after successful initialization.
     */
    public static LayerContext read(DataInput input, GameState game, BlockIndexer indexer,
                                    Pathfinder pathfinder, ControlPathfinder controlPath, LayerEvents events) throws IOException {
        return read(input, game, indexer, pathfinder, controlPath, events, context -> {
        });
    }

    public static LayerContext read(DataInput input, GameState game, BlockIndexer indexer,
                                    Pathfinder pathfinder, ControlPathfinder controlPath, LayerEvents events,
                                    Initializer initializer) throws IOException {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(events, "events");
        Objects.requireNonNull(initializer, "initializer");
        if (input.readInt() != magic) throw new IOException("Invalid layer snapshot");
        int version = input.readInt();
        if (version != format) throw new IOException("Unsupported layer format: " + version);
        int engineVersion = input.readInt();
        SaveVersion reader = SaveIO.getSaveWriter(engineVersion);
        if (reader == null) throw new IOException("Unsupported Mindustry save version: " + engineVersion);
        int size = input.readInt();
        if (size < 4 || size > maxBytes) throw new IOException("Invalid layer snapshot size: " + size);
        byte[] bytes = new byte[size];
        input.readFully(bytes);
        DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
        int width = data.readUnsignedShort(), height = data.readUnsignedShort();
        validateSize(width, height);
        LayerContext layer = LayerContext.create(new Tiles(1, 1), game, indexer, pathfinder, controlPath, events);
        boolean completed = false;
        try {
            layer.run(() -> {
                try {
                    Vars.world.setGenerating(true);
                    DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes));
                    SaveReadState state = new SaveReadState(new ReadContext(events));
                    reader.readMap(stream, state);
                    Vars.avoidance.init();
                    Groups.resize(-Vars.finalWorldBounds, -Vars.finalWorldBounds,
                            width * Vars.tilesize + Vars.finalWorldBounds * 2,
                            height * Vars.tilesize + Vars.finalWorldBounds * 2);
                    state.allBuildings.each(build -> build.updateProximity());
                    initializer.initialize(layer);
                    reader.readEntities(stream, state);
                    if (stream.available() != 0) throw new IOException("Trailing layer snapshot data");
                    Vars.world.setGenerating(false);
                } catch (IOException failure) {
                    throw new UncheckedIOException(failure);
                }
            });
            completed = true;
        } catch (UncheckedIOException failure) {
            throw failure.getCause();
        } finally {
            if (!completed) layer.close();
        }
        return layer;
    }

    public interface Initializer {
        void initialize(LayerContext layer) throws IOException;
    }

    private static void validateSize(int width, int height) throws IOException {
        if (width == 0 || height == 0 || width > maxDimension || height > maxDimension || (long) width * height > maxTiles) {
            throw new IOException("Invalid layer dimensions: " + width + "x" + height);
        }
    }

    private static final class ReadContext implements WorldContext {
        private final LayerEvents events;

        ReadContext(LayerEvents events) {
            this.events = events;
        }

        @Override
        public Tile tile(int index) {
            return Vars.world.tiles.geti(index);
        }

        @Override
        public void resize(int width, int height) {
            Vars.world.tiles = new Tiles(width, height);
        }

        @Override
        public Tile create(int x, int y, int floor, int overlay, int wall) {
            Tile tile = new LayerTile(x, y, floor, overlay, events);
            Vars.world.tiles.set(x, y, tile);
            return tile;
        }

        @Override
        public boolean isGenerating() {
            return true;
        }

        @Override
        public void begin() {
            throw new IllegalStateException("Layer loading must not start the main world lifecycle");
        }

        @Override
        public void end() {
            throw new IllegalStateException("Layer loading must not end the main world lifecycle");
        }
    }
}
