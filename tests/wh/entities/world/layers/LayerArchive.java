package wh.entities.world.layers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Versioned collection of layer snapshots, suitable for a custom save chunk.
 */
public final class LayerArchive {
    private static final int magic = 0x57484c41;
    private static final int version = 1;
    private static final int maxLayers = 64;
    private static final int maxIdLength = 128;

    private LayerArchive() {
    }

    public static void write(DataOutput output, Map<String, LayerContext> layers) throws IOException {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(layers, "layers");
        if (layers.size() > maxLayers) throw new IOException("Too many world layers");
        for (Map.Entry<String, LayerContext> entry : layers.entrySet()) {
            validateId(entry.getKey());
            if (entry.getValue() == null) throw new IOException("Missing layer context: " + entry.getKey());
        }
        output.writeInt(magic);
        output.writeInt(version);
        output.writeInt(layers.size());
        for (Map.Entry<String, LayerContext> entry : layers.entrySet()) {
            output.writeUTF(entry.getKey());
            LayerSnapshotCodec.write(output, entry.getValue());
        }
    }

    /**
     * Builds a new map; callers can publish it only after the whole archive has loaded.
     */
    public static Map<String, LayerContext> read(DataInput input, Loader loader) throws IOException {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(loader, "loader");
        if (input.readInt() != magic) throw new IOException("Invalid layer archive");
        int foundVersion = input.readInt();
        if (foundVersion != version) throw new IOException("Unsupported layer archive: " + foundVersion);
        int count = input.readInt();
        if (count < 0 || count > maxLayers) throw new IOException("Invalid layer count: " + count);
        Map<String, LayerContext> loaded = new LinkedHashMap<>();
        try {
            for (int i = 0; i < count; i++) {
                String id = input.readUTF();
                validateId(id);
                if (loaded.containsKey(id)) throw new IOException("Duplicate layer ID: " + id);
                LayerContext layer = loader.read(id, input);
                if (layer == null) throw new IOException("Layer loader returned null: " + id);
                loaded.put(id, layer);
            }
            return loaded;
        } catch (IOException | RuntimeException | Error failure) {
            for (LayerContext layer : loaded.values()) {
                try {
                    layer.close();
                } catch (RuntimeException closeFailure) {
                    failure.addSuppressed(closeFailure);
                }
            }
            throw failure;
        }
    }

    static void validateId(String id) throws IOException {
        if (id == null || id.isEmpty() || id.length() > maxIdLength) {
            throw new IOException("Invalid layer ID");
        }
    }

    public interface Loader {
        LayerContext read(String id, DataInput input) throws IOException;
    }
}
