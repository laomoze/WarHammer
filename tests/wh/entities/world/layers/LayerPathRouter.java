package wh.entities.world.layers;

import arc.Events;
import arc.func.Cons;
import mindustry.Vars;
import mindustry.ai.Pathfinder;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.world.Tile;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Keeps the surface path grid visible to vanilla background pathfinding.
 */
public final class LayerPathRouter extends Pathfinder implements AutoCloseable {
    private static final Field tileGrid = findTileGrid();
    private final Pathfinder surface;
    private final Cons<WorldLoadEvent> worldLoaded;

    private LayerPathRouter(Pathfinder surface) {
        this.surface = surface;
        worldLoaded = event -> copySurfaceGrid();
        copySurfaceGrid();
    }

    public static LayerPathRouter install() {
        Pathfinder surface = Objects.requireNonNull(Vars.pathfinder, "surface pathfinder");
        if (surface instanceof LayerPathRouter) throw new IllegalStateException("Path router already installed");
        EngineEventCapture capture = new EngineEventCapture(WorldLoadEvent.class, ResetEvent.class,
                TileChangeEvent.class, TilePreChangeEvent.class, Trigger.afterGameUpdate);
        LayerPathRouter router;
        try {
            router = new LayerPathRouter(surface);
        } finally {
            capture.finish();
        }
        Events.on(WorldLoadEvent.class, router.worldLoaded);
        Vars.pathfinder = router;
        return router;
    }

    private Pathfinder selected() {
        Pathfinder local = LayerContext.activePathfinder();
        if (local == null && LayerContext.insideLayer())
            throw new IllegalStateException("Layer pathfinder is not initialized");
        return local == null || local == this ? surface : local;
    }

    @Override
    public int packTile(Tile tile) {
        return selected().packTile(tile);
    }

    @Override
    public int get(int x, int y) {
        return selected().get(x, y);
    }

    @Override
    public void updateTile(Tile tile) {
        selected().updateTile(tile);
    }

    @Override
    public Flowfield getField(Team team, int costType, int fieldType) {
        return selected().getField(team, costType, fieldType);
    }

    @Override
    public Tile getTargetTile(Tile tile, Flowfield path) {
        return selected().getTargetTile(tile, path);
    }

    @Override
    public Tile getTargetTile(Tile tile, Flowfield path, boolean diagonals) {
        return selected().getTargetTile(tile, path, diagonals);
    }

    @Override
    public Tile getTargetTile(Tile tile, Flowfield path, boolean diagonals, int avoidanceId) {
        return selected().getTargetTile(tile, path, diagonals, avoidanceId);
    }

    @Override
    public void close() {
        Events.remove(WorldLoadEvent.class, worldLoaded);
        if (Vars.pathfinder == this) Vars.pathfinder = surface;
    }

    private void copySurfaceGrid() {
        try {
            tileGrid.set(this, tileGrid.get(surface));
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException("Cannot bind surface path grid", failure);
        }
    }

    private static Field findTileGrid() {
        try {
            Field field = Pathfinder.class.getDeclaredField("tiles");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Unsupported Pathfinder tile grid", failure);
        }
    }
}
