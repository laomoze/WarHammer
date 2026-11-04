package wh.entities.world.layers;

import mindustry.io.SaveFileReader.CustomChunk;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

/**
 * Stores layers in saves and in the initial world data sent to a joining client.
 */
public final class LayerSaveChunk implements CustomChunk {
    private final LayerSet layers;
    private final LayerArchive.Loader loader;

    public LayerSaveChunk(LayerSet layers, LayerArchive.Loader loader) {
        this.layers = Objects.requireNonNull(layers, "layers");
        this.loader = Objects.requireNonNull(loader, "loader");
    }

    @Override
    public void write(DataOutput output) throws IOException {
        layers.write(output);
    }

    @Override
    public void read(DataInput input) throws IOException {
        layers.read(input, loader);
    }

    @Override
    public boolean writeNet() {
        return true;
    }
}
