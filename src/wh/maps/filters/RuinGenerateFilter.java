package wh.maps.filters;

import arc.math.Mathf;
import arc.struct.IntSeq;
import mindustry.content.Blocks;
import mindustry.gen.Iconc;
import mindustry.maps.filters.FilterOption;
import mindustry.maps.filters.GenerateFilter;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;

import java.lang.reflect.Field;

import static mindustry.maps.filters.FilterOption.oresFloorsOptional;
import static wh.maps.filters.WHFilterOptions.*;

public class RuinGenerateFilter extends GenerateFilter {
    private static final Field inputBufferField;

    static {
        Field field;
        try {
            field = GenerateInput.class.getDeclaredField("buffer");
            field.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            field = null;
        }
        inputBufferField = field;
    }

    public Block replaceOverlay = Blocks.metalTiles1;
    public RuinStep[] steps = RuinStep.defaults();

    public float scl = 40f, threshold = 0.5f, octaves = 3f, falloff = 0.5f, tilt = 0f;
    public float distortScl = 10f, distortMag = 5f;

    private transient IntSeq centers;
    private transient TileState[] computed;
    private transient boolean[] floorModified;
    private transient boolean[] blockModified;
    private transient boolean mapComputed;
    private transient int tilesLeft;

    @Override
    public FilterOption[] options() {
        return new FilterOption[]{
                new BlockOption("replace", () -> replaceOverlay, b -> replaceOverlay = b, oresFloorsOptional),
                new StepsEditOption(this),
                new SliderOption("scale", () -> scl, f -> scl = f, 1f, 500f),
                new SliderOption("threshold", () -> threshold, f -> threshold = f, 0f, 1f),
                new SliderOption("octaves", () -> octaves, f -> octaves = f, 1f, 10f),
                new SliderOption("falloff", () -> falloff, f -> falloff = f, 0f, 1f),
                new SliderOption("tilt", () -> tilt, f -> tilt = f, -4f, 4f),
                new SliderOption("distort-scale", () -> distortScl, f -> distortScl = f, 1f, 20f),
                new SliderOption("distort-mag", () -> distortMag, f -> distortMag = f, 0.1f, 10f)
        };
    }

    @Override
    public char icon() {
        return Iconc.blockStoneWall;
    }

    @Override
    public void apply(GenerateInput in) {
        if (!mapComputed) {
            rebuildCenters(in);
            computeMap(in);
            mapComputed = true;
            tilesLeft = in.width * in.height;
        }

        writeTile(in);

        if (--tilesLeft <= 0) {
            mapComputed = false;
        }
    }

    private void computeMap(GenerateInput in) {
        int width = in.width, height = in.height;
        int size = width * height;
        computed = new TileState[size];
        floorModified = new boolean[size];
        blockModified = new boolean[size];

        for (int i = 0; i < size; i++) {
            computed[i] = TileState.from(readTile(in, i % width, i / width));
        }

        TileState[] baseline = copyStates(computed);

        if (steps == null || steps.length == 0) return;

        for (RuinStep step : steps) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (!passesNoise(x, y, in)) continue;

                    int index = x + y * width;
                    for (int ci = 0; ci < centers.size; ci++) {
                        int centerIndex = centers.get(ci);
                        int cx = centerIndex % width, cy = centerIndex / width;
                        if (!step.matches(cx, cy, x, y)) continue;

                        applyStep(computed[index], baseline[index], step, index);
                    }
                }
            }
        }

        applyDistort(computed, baseline, in);
    }

    private void applyDistort(TileState[] state, TileState[] baseline, GenerateInput in) {
        int width = in.width, height = in.height;
        int size = width * height;
        TileState[] source = copyStates(state);
        TileState[] dest = copyStates(baseline);
        boolean[] newFloorModified = new boolean[size];
        boolean[] newBlockModified = new boolean[size];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = x + y * width;
                if (!floorModified[index] && !blockModified[index]) continue;

                in.x = x;
                in.y = y;
                int ox = sampleCoord(x, noise(in, distortScl, distortMag / 2f), width);
                int oy = sampleCoord(y, noise(1, in, distortScl, distortMag / 2f), height);
                int destIndex = ox + oy * width;

                if (floorModified[index]) {
                    dest[destIndex].floor = source[index].floor;
                    newFloorModified[destIndex] = true;
                }
                if (blockModified[index]) {
                    dest[destIndex].block = source[index].block;
                    newBlockModified[destIndex] = true;
                }
            }
        }

        System.arraycopy(dest, 0, state, 0, size);
        floorModified = newFloorModified;
        blockModified = newBlockModified;
    }

    private void writeTile(GenerateInput in) {
        int index = in.x + in.y * in.width;
        if (!floorModified[index] && !blockModified[index]) return;

        TileState state = computed[index];
        if (floorModified[index] && state.floor instanceof Floor floor) {
            in.floor = floor;
            in.overlay = !floor.hasSurface() && in.overlay.asFloor().needsSurface && in.overlay instanceof OreBlock ? Blocks.air : in.overlay;
        }

        if (blockModified[index]) {
            if (state.block == Blocks.air) {
                if (!in.block.synthetic()) in.block = Blocks.air;
            } else if (!in.block.synthetic() && !state.block.synthetic()) {
                in.block = state.block;
            }
        }
    }

    private void rebuildCenters(GenerateInput in) {
        if (centers == null) centers = new IntSeq();
        centers.clear();

        for (int x = 0; x < in.width; x++) {
            for (int y = 0; y < in.height; y++) {
                Tile tile = readTile(in, x, y);
                if (tile != null && tile.overlay() == replaceOverlay) {
                    centers.add(x + y * in.width);
                }
            }
        }
    }

    private static Tile readTile(GenerateInput in, int x, int y) {
        x = Mathf.clamp(x, 0, in.width - 1);
        y = Mathf.clamp(y, 0, in.height - 1);

        if (inputBufferField != null) {
            try {
                GenerateInput.TileProvider provider = (GenerateInput.TileProvider) inputBufferField.get(in);
                if (provider != null) return provider.get(x, y);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return null;
    }

    private void applyStep(TileState state, TileState baseline, RuinStep step, int index) {
        if (step.preservesFloor()) {
            state.floor = baseline.floor;
            floorModified[index] = false;
        } else if (step.hasFloor()) {
            state.floor = step.floor;
            floorModified[index] = true;
        }
        if (step.removesWall()) {
            state.block = Blocks.air;
            blockModified[index] = true;
        } else if (step.hasWall()) {
            state.block = step.wall;
            blockModified[index] = true;
        }
    }

    private static TileState[] copyStates(TileState[] states) {
        TileState[] copy = new TileState[states.length];
        for (int i = 0; i < states.length; i++) {
            copy[i] = states[i].copy();
        }
        return copy;
    }

    private static int sampleCoord(int coord, float offset, int size) {
        return Mathf.clamp((int) (coord + offset), 0, size - 1);
    }

    private boolean passesNoise(int x, int y, GenerateInput in) {
        return noise(x, y + x * tilt, scl, 1f, octaves, falloff) <= threshold;
    }

    private static class TileState {
        Block block, floor, overlay;

        static TileState from(Tile tile) {
            TileState state = new TileState();
            if (tile == null) return state;
            state.block = tile.block();
            state.floor = tile.floor();
            state.overlay = tile.overlay();
            return state;
        }

        TileState copy() {
            TileState state = new TileState();
            state.block = block;
            state.floor = floor;
            state.overlay = overlay;
            return state;
        }
    }
}

