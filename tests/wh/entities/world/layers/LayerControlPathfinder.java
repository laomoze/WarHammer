package wh.entities.world.layers;

import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.ai.Astar;
import mindustry.ai.ControlPathfinder;
import mindustry.core.World;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

/**
 * Main-thread paths for units controlled inside one layer.
 */
public final class LayerControlPathfinder extends ControlPathfinder implements LayerEvents.Listener {
    private static final int maxSearchesPerFrame = 2;
    private final ObjectMap<Unit, Path> paths = new ObjectMap<>();
    private final Seq<Unit> expired = new Seq<>();
    private final PathfindResult result = new PathfindResult();
    private long revision, frame = Long.MIN_VALUE;
    private int searches;

    public LayerControlPathfinder(LayerEvents events) {
        events.add(this);
    }

    @Override
    public void preChange(Tile tile) {
        revision++;
    }

    @Override
    public void floorChanged(Tile tile, Floor previous, Floor floor) {
        revision++;
    }

    @Override
    public void overlayChanged(Tile tile, Floor previous, Floor overlay) {
        revision++;
    }

    @Override
    public void updateTile(Tile tile) {
        revision++;
    }

    @Override
    public void updateSingleTile(Tile tile) {
        revision++;
    }

    @Override
    public PathfindResult getPathPosition(Unit unit, Vec2 destination) {
        return getPathPosition(unit, destination, destination);
    }

    @Override
    public PathfindResult getPathPosition(Unit unit, Vec2 destination, Vec2 mainDestination) {
        result.move = false;
        result.unreachable = false;
        result.next = null;
        result.dest.setZero();
        int tx = World.toTile(destination.x), ty = World.toTile(destination.y);
        Tile start = unit.tileOn(), end = Vars.world.tile(tx, ty);
        if (start == null || end == null || !Vars.world.isInMapArea(tx, ty)) {
            result.unreachable = true;
            return result;
        }
        if (unit.isFlying() || start == end) {
            result.dest.set(destination);
            result.move = true;
            return result;
        }
        if (frame != Vars.state.updateId) {
            frame = Vars.state.updateId;
            searches = 0;
            if (frame % 300 == 0) {
                expired.clear();
                paths.each((key, value) -> {
                    if (!key.isAdded()) expired.add(key);
                });
                for (Unit stale : expired) paths.remove(stale);
            }
        }
        Path path = paths.get(unit);
        if (path == null || path.destination != end.pos() || path.revision != revision || !path.contains(start)) {
            if (searches >= maxSearchesPerFrame) return result;
            searches++;
            boolean avoidDanger = unit.type.canDrown && !start.dangerous();
            Seq<Tile> found = Astar.pathfind(start, end, tile -> 1f,
                    tile -> unit.canPass(tile.x, tile.y) && (!avoidDanger || !tile.dangerous()));
            path = new Path(start, end, revision, found.copy());
            paths.put(unit, path);
        }
        Tile next = path.next(start);
        if (next == null) {
            result.unreachable = true;
        } else {
            result.next = next;
            result.dest.set(next.worldx(), next.worldy());
            result.move = true;
        }
        return result;
    }

    private static final class Path {
        final int start, destination;
        final long revision;
        final Seq<Tile> tiles;

        Path(Tile start, Tile destination, long revision, Seq<Tile> tiles) {
            this.start = start.pos();
            this.destination = destination.pos();
            this.revision = revision;
            this.tiles = tiles;
        }

        boolean contains(Tile tile) {
            return tile.pos() == start || tiles.contains(tile, true);
        }

        Tile next(Tile tile) {
            if (tile.pos() == start) return tiles.isEmpty() ? null : tiles.first();
            int index = tiles.indexOf(tile, true);
            return index < 0 || index + 1 >= tiles.size ? null : tiles.get(index + 1);
        }
    }
}
