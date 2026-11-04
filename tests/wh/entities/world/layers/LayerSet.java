package wh.entities.world.layers;

import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.core.World;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * Owns the independent layers attached to one main world.
 */
public final class LayerSet implements AutoCloseable {
    private final World world;
    private final int width, height;
    private final LinkedHashMap<String, LayerContext> layers = new LinkedHashMap<>();
    private boolean closed;

    public LayerSet(int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Invalid layer dimensions");
        this.world = Objects.requireNonNull(Vars.world, "world");
        this.width = width;
        this.height = height;
    }

    public void add(String id, LayerContext layer) throws IOException {
        checkWorld();
        LayerArchive.validateId(id);
        if (layer == null || layers.containsKey(id) || layers.containsValue(layer))
            throw new IOException("Invalid or duplicate layer: " + id);
        checkDimensions(layer);
        layers.put(id, layer);
    }

    public LayerContext get(String id) {
        checkWorld();
        return layers.get(id);
    }

    public Map<String, LayerContext> layers() {
        checkWorld();
        return Collections.unmodifiableMap(layers);
    }

    public void update(GameState main) {
        checkWorld();
        for (LayerContext layer : layers.values()) LayerSimulation.update(layer, main);
    }

    public void write(DataOutput output) throws IOException {
        checkWorld();
        LayerArchive.write(output, layers);
    }

    public void read(DataInput input, LayerArchive.Loader loader) throws IOException {
        checkWorld();
        checkInactive(layers);
        IdentityHashMap<LayerContext, Boolean> seen = new IdentityHashMap<>();
        Map<String, LayerContext> incoming = LayerArchive.read(input, (id, stream) -> {
            LayerContext layer = loader.read(id, stream);
            if (layer == null || layers.containsValue(layer) || seen.containsKey(layer)) {
                throw new IOException("Loaded layers must have independent contexts");
            }
            try {
                checkDimensions(layer);
            } catch (IOException | RuntimeException failure) {
                try {
                    layer.close();
                } catch (RuntimeException closeFailure) {
                    failure.addSuppressed(closeFailure);
                }
                throw failure;
            }
            seen.put(layer, Boolean.TRUE);
            return layer;
        });
        Map<String, LayerContext> old = new LinkedHashMap<>(layers);
        layers.clear();
        layers.putAll(incoming);
        closeAll(old);
    }

    @Override
    public void close() {
        if (closed) return;
        checkInactive(layers);
        closed = true;
        Map<String, LayerContext> old = new LinkedHashMap<>(layers);
        layers.clear();
        closeAll(old);
    }

    private void checkDimensions(LayerContext layer) throws IOException {
        boolean matches = layer.call(() -> Vars.world.width() == width && Vars.world.height() == height);
        if (!matches) throw new IOException("Layer dimensions do not match the main world");
    }

    private void checkWorld() {
        if (closed || Vars.world != world) throw new IllegalStateException("Layer set belongs to an inactive world");
    }

    private static void closeAll(Map<String, LayerContext> values) {
        RuntimeException failure = null;
        for (LayerContext layer : values.values()) {
            try {
                layer.close();
            } catch (RuntimeException exception) {
                if (failure == null) failure = exception;
                else failure.addSuppressed(exception);
            }
        }
        if (failure != null) throw failure;
    }

    private static void checkInactive(Map<String, LayerContext> values) {
        for (LayerContext layer : values.values()) {
            if (layer.inUse()) throw new IllegalStateException("Cannot replace active layers");
        }
    }
}
