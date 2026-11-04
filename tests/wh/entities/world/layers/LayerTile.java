package wh.entities.world.layers;

import mindustry.Vars;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;

import java.util.Objects;

/**
 * A tile that routes changes to its own layer rather than the main world.
 */
public final class LayerTile extends Tile {
    private final LayerEvents events;

    public LayerTile(int x, int y, LayerEvents events) {
        super(x, y);
        this.events = Objects.requireNonNull(events, "events");
    }

    LayerTile(int x, int y, int floor, int overlay, LayerEvents events) {
        this(x, y, events);
        this.floor = Vars.content.block(floor).asFloor();
        this.overlay = Vars.content.block(overlay).asFloor();
    }

    public static Tiles createTiles(int width, int height, LayerEvents events) {
        Tiles tiles = new Tiles(width, height);
        for (int i = 0; i < width * height; i++) {
            tiles.seti(i, new LayerTile(i % width, i / width, events));
        }
        return tiles;
    }

    @Override
    protected void firePreChanged() {
        if (!Vars.world.isGenerating()) events.preChange(this);
    }

    @Override
    protected void fireChanged() {
        if (!Vars.world.isGenerating()) events.changed(this);
    }

    @Override
    public void setFloor(Floor type) {
        if (floor == type) return;
        Floor previous = floor;
        floor = type;
        if (build != null) build.onProximityUpdate();
        if (!Vars.world.isGenerating()) {
            if (Vars.pathfinder != null && !Vars.state.isEditor()) Vars.pathfinder.updateTile(this);
            events.floorChanged(this, previous, type);
        }
        if (floor != previous) floor.floorChanged(this);
    }

    @Override
    public void setOverlay(Block block) {
        if (overlay == block) return;
        Floor previous = overlay;
        overlay = block.asFloor();
        if (!Vars.world.isGenerating()) {
            events.overlayChanged(this, previous, overlay);
            if (build != null) build.onProximityUpdate();
        }
    }

    @Override
    public void recache() {
    }

    @Override
    public void recacheWall() {
    }
}
