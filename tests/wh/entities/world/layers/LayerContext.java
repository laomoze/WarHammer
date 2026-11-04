package wh.entities.world.layers;

import arc.Application;
import arc.Core;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.pooling.Pool;
import mindustry.Vars;
import mindustry.ai.BlockIndexer;
import mindustry.ai.ControlPathfinder;
import mindustry.ai.Pathfinder;
import mindustry.ai.WaveSpawner;
import mindustry.async.AvoidanceProcess;
import mindustry.async.PhysicsProcess;
import mindustry.core.GameState;
import mindustry.core.World;
import mindustry.entities.EntityGroup;
import mindustry.gen.*;
import mindustry.net.Net;
import mindustry.world.Tiles;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A scoped view of the engine globals belonging to one world layer.
 */
public final class LayerContext {
    private static final Field freeQueue = findFreeQueue();
    private static final Field timeRemoval = findField(Time.class, "removal");
    private static Thread owner;
    private static LayerContext active;

    private Snapshot state;
    private final LayerEvents events;
    private final Application application;
    private Pathfinder layerPathfinder;
    private boolean closed;
    private int depth;
    private final Seq<Runnable> closeHandlers = new Seq<>();

    private LayerContext(Snapshot state, LayerEvents events, Pathfinder layerPathfinder) {
        this.state = state;
        this.events = events;
        this.layerPathfinder = layerPathfinder;
        this.application = LayerApplication.create(state.application, this);
    }

    /**
     * Must be called on the game thread after the main world and entity groups exist.
     */
    public static LayerContext create(Tiles tiles, GameState game, BlockIndexer indexer,
                                      Pathfinder pathfinder, ControlPathfinder controlPath, LayerEvents events) {
        checkThread();
        Objects.requireNonNull(tiles, "tiles");
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(events, "events");
        Snapshot previous = Snapshot.capture();
        try {
            Groups.init();
            setFreeQueue(new Seq<Pool.Poolable>());
            Time.setRuns(new Seq<Time.DelayRun>());
            setTimeRemoval(new Seq<Time.DelayRun>());
            Vars.world.tiles = tiles;
            Vars.world.setGenerating(false);
            Vars.world.tileChanges = 0;
            Vars.world.floorChanges = 0;
            Vars.state = game;
            Vars.indexer = indexer;
            Vars.pathfinder = previous.pathfinder instanceof LayerPathRouter ? previous.pathfinder : pathfinder;
            Vars.controlPath = controlPath;
            Vars.unitPhysics = new PhysicsProcess();
            Vars.avoidance = new AvoidanceProcess();
            return new LayerContext(Snapshot.capture(), events, pathfinder);
        } finally {
            previous.install();
        }
    }

    public void run(Runnable action) {
        Objects.requireNonNull(action, "action");
        call(() -> {
            action.run();
            return null;
        });
    }

    public <T> T call(Supplier<T> action) {
        checkThread();
        if (closed) throw new IllegalStateException("Layer context is closed");
        Objects.requireNonNull(action, "action");
        Snapshot previous = Snapshot.capture();
        if (previous.world != state.world) {
            throw new IllegalStateException("Layer belongs to a different world instance");
        }
        LayerContext previousLayer = active;
        if (previousLayer != null) previousLayer.state = previous;
        LayerEvents.Scope scope = null;
        boolean entered = false;
        try {
            state.install();
            if (previous.pathfinder instanceof LayerPathRouter) Vars.pathfinder = previous.pathfinder;
            Core.app = application;
            active = this;
            depth++;
            entered = true;
            scope = events.enter();
            return action.get();
        } finally {
            try {
                try {
                    if (scope != null) scope.close();
                } finally {
                    if (entered) depth--;
                    state = Snapshot.capture();
                }
            } finally {
                active = previousLayer;
                (previousLayer == null ? previous : previousLayer.state).install();
            }
        }
    }

    /**
     * Prevents queued callbacks from re-entering a layer after its world is unloaded.
     */
    public void close() {
        checkThread();
        if (depth != 0) throw new IllegalStateException("Cannot close an active layer");
        if (closed) return;
        try {
            if (Vars.world == state.world) {
                run(() -> {
                    for (Runnable handler : closeHandlers) handler.run();
                    closeHandlers.clear();
                    Groups.clear();
                    Groups.updatePooling();
                    Vars.unitPhysics.reset();
                    Vars.avoidance.reset();
                });
            }
        } finally {
            closed = true;
            state.timeRuns.clear();
        }
    }

    void runPosted(Runnable action) {
        checkThread();
        if (!closed && Vars.world == state.world) run(action);
    }

    boolean inUse() {
        return depth != 0;
    }

    static Pathfinder activePathfinder() {
        return Thread.currentThread() == owner && active != null ? active.layerPathfinder : null;
    }

    static boolean insideLayer() {
        return Thread.currentThread() == owner && active != null;
    }

    void setPathfinder(Pathfinder pathfinder) {
        if (active != this) throw new IllegalStateException("Layer must be active");
        layerPathfinder = Objects.requireNonNull(pathfinder, "pathfinder");
        if (!(Vars.pathfinder instanceof LayerPathRouter)) Vars.pathfinder = pathfinder;
    }

    void onClose(Runnable handler) {
        if (active != this) throw new IllegalStateException("Layer must be active");
        closeHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    void afterUpdate() {
        if (active != this) throw new IllegalStateException("Layer must be active");
        events.afterUpdate();
    }

    private static void checkThread() {
        Thread current = Thread.currentThread();
        synchronized (LayerContext.class) {
            if (owner == null) owner = current;
            if (owner != current) throw new IllegalStateException("Layer contexts must run on the game thread");
        }
    }

    private static Field findFreeQueue() {
        return findField(Groups.class, "freeQueue");
    }

    private static Field findField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Unsupported engine field: " + owner.getName() + "." + name, failure);
        }
    }

    private static Object getTimeRemoval() {
        try {
            return timeRemoval.get(null);
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private static void setTimeRemoval(Object queue) {
        try {
            timeRemoval.set(null, queue);
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private static Object getFreeQueue() {
        try {
            return freeQueue.get(null);
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private static void setFreeQueue(Object queue) {
        try {
            freeQueue.set(null, queue);
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException(failure);
        }
    }

    private static final class Snapshot {
        final World world;
        final Tiles tiles;
        final GameState game;
        final BlockIndexer indexer;
        final Pathfinder pathfinder;
        final ControlPathfinder controlPath;
        final Net net;
        final PhysicsProcess unitPhysics;
        final AvoidanceProcess avoidance;
        final WaveSpawner spawner;
        final boolean generating;
        final int tileChanges, floorChanges;
        final EntityGroup<Entityc> all;
        final EntityGroup<Building> build;
        final EntityGroup<Bullet> bullet;
        final EntityGroup<Drawc> draw;
        final EntityGroup<EffectState> effect;
        final EntityGroup<Player> player;
        final EntityGroup<PowerGraphUpdaterc> powerGraph;
        final EntityGroup<Syncc> sync;
        final EntityGroup<mindustry.gen.Unit> unit;
        final EntityGroup<WeatherState> weather;
        final Object queue;
        final Seq<Time.DelayRun> timeRuns;
        final Object timeRemovalQueue;
        final Application application;

        private Snapshot() {
            world = Objects.requireNonNull(Vars.world, "world");
            tiles = world.tiles;
            game = Vars.state;
            indexer = Vars.indexer;
            pathfinder = Vars.pathfinder;
            controlPath = Vars.controlPath;
            net = Vars.net;
            unitPhysics = Vars.unitPhysics;
            avoidance = Vars.avoidance;
            spawner = Vars.spawner;
            generating = world.isGenerating();
            tileChanges = world.tileChanges;
            floorChanges = world.floorChanges;
            all = Groups.all;
            build = Groups.build;
            bullet = Groups.bullet;
            draw = Groups.draw;
            effect = Groups.effect;
            player = Groups.player;
            powerGraph = Groups.powerGraph;
            sync = Groups.sync;
            unit = Groups.unit;
            weather = Groups.weather;
            queue = getFreeQueue();
            timeRuns = Time.getRuns();
            timeRemovalQueue = getTimeRemoval();
            application = Core.app;
        }

        static Snapshot capture() {
            return new Snapshot();
        }

        void install() {
            Vars.world = world;
            world.tiles = tiles;
            world.setGenerating(generating);
            world.tileChanges = tileChanges;
            world.floorChanges = floorChanges;
            Vars.state = game;
            Vars.indexer = indexer;
            Vars.pathfinder = pathfinder;
            Vars.controlPath = controlPath;
            Vars.net = net;
            Vars.unitPhysics = unitPhysics;
            Vars.avoidance = avoidance;
            Vars.spawner = spawner;
            Groups.all = all;
            Groups.build = build;
            Groups.bullet = bullet;
            Groups.draw = draw;
            Groups.effect = effect;
            Groups.player = player;
            Groups.powerGraph = powerGraph;
            Groups.sync = sync;
            Groups.unit = unit;
            Groups.weather = weather;
            setFreeQueue(queue);
            Time.setRuns(timeRuns);
            setTimeRemoval(timeRemovalQueue);
            Core.app = application;
        }
    }
}
