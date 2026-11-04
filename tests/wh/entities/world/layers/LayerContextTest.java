package wh.entities.world.layers;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.math.geom.Point2;
import arc.mock.*;
import arc.util.I18NBundle;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.core.ContentLoader;
import mindustry.core.GameState;
import mindustry.core.World;
import mindustry.entities.EntityCollisions;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.io.SaveIO;
import mindustry.io.SaveVersion;
import mindustry.net.Net;
import mindustry.net.Packet;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.world.Build;
import mindustry.world.Tiles;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.ConstructBlock.ConstructBuild;
import mindustry.world.blocks.distribution.Conveyor;
import mindustry.world.blocks.environment.AirBlock;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BuildVisibility;

import java.io.*;
import java.util.*;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class LayerContextTest {
    public static final class LargeTestPacket extends Packet {
        byte[] bytes = new byte[1450];

        @Override
        public void write(Writes output) {
            output.b(bytes);
        }
    }

    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        System.out.println("PASS " + message);
    }

    private static LayerContext layer(LayerEvents events) {
        Tiles tiles = LayerTile.createTiles(8, 8, events);
        GameState state = new GameState();
        state.rules.disableUnitCap = true;
        return LayerContext.create(tiles, state, null, null, null, events);
    }

    public static void main(String[] args) throws Exception {
        class QueuedApp extends MockApplication {
            final LinkedBlockingQueue<Runnable> posted = new LinkedBlockingQueue<>();

            @Override
            public void post(Runnable task) {
                posted.add(task);
            }
        }
        QueuedApp app = new QueuedApp();
        Core.app = app;
        Core.audio = new MockAudio();
        Core.files = new MockFiles();
        Core.graphics = new MockGraphics();
        Core.settings = new MockSettings();
        Core.bundle = I18NBundle.createEmptyBundle();
        Vars.headless = true;
        Vars.content = new ContentLoader();
        Vars.state = new GameState();
        Vars.state.rules.disableUnitCap = true;
        Vars.state.set(GameState.State.playing);
        Vars.net = new Net(null);
        Vars.world = new World();
        Vars.collisions = new EntityCollisions();
        Vars.mainExecutor = new AbstractExecutorService() {
            private boolean shutdown;

            @Override
            public void shutdown() {
                shutdown = true;
            }

            @Override
            public List<Runnable> shutdownNow() {
                shutdown = true;
                return Collections.emptyList();
            }

            @Override
            public boolean isShutdown() {
                return shutdown;
            }

            @Override
            public boolean isTerminated() {
                return shutdown;
            }

            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) {
                return shutdown;
            }

            @Override
            public void execute(Runnable task) {
                task.run();
            }
        };
        Groups.init();
        Blocks.air = new AirBlock("air");
        Blocks.air.init();
        Blocks.stone = new Floor("stone");
        Blocks.stone.init();
        Item item = new Item("layer-item", Color.orange);
        Conveyor belt = new Conveyor("layer-belt");
        belt.speed = 0.1f;
        belt.flags = belt.flags.with(BlockFlag.storage);
        belt.init();
        Conveyor large = new Conveyor("layer-large-belt");
        large.size = 2;
        large.init();
        UnitType unitType = new UnitType("layer-unit");
        unitType.constructor = UnitEntity::create;
        unitType.init();
        Time.setDeltaProvider(() -> 1f);

        World world = Vars.world;
        Tiles originalTiles = world.tiles;
        GameState originalState = Vars.state;
        var originalGroups = Groups.build;
        LayerEvents firstEvents = new LayerEvents(), secondEvents = new LayerEvents();
        LayerContext first = layer(firstEvents), second = layer(secondEvents);
        for (LayerContext layer : new LayerContext[]{first, second}) {
            layer.run(() -> {
                Vars.world.setGenerating(true);
                Groups.resize(0, 0, 64, 64);
                for (var tile : Vars.world.tiles) tile.setFloor((Floor) Blocks.stone);
                Vars.world.tile(2, 2).setBlock(belt, Team.sharded, 0);
                Vars.world.tile(3, 2).setBlock(belt, Team.sharded, 0);
                Vars.world.tile(5, 5).setBlock(large, Team.sharded, 1);
                Building input = Vars.world.build(2, 2), output = Vars.world.build(3, 2);
                input.onProximityUpdate();
                output.onProximityUpdate();
                input.handleItem(input, item);
            });
        }
        for (int i = 0; i < 30; i++) {
            Time.update();
            originalState.tick++;
            originalState.updateId++;
            LayerSimulation.update(first, originalState);
            LayerSimulation.update(second, originalState);
        }
        first.run(() -> check(Vars.world.build(3, 2).items.get(item) == 1, "first layer transports items"));
        second.run(() -> check(Vars.world.build(3, 2).items.get(item) == 1, "second layer transports items"));
        first.run(() -> check(Vars.state.updateId == originalState.updateId, "layer uses main frame clock"));
        originalState.set(GameState.State.paused);
        originalState.updateId++;
        LayerSimulation.update(first, originalState);
        first.run(() -> check(Vars.state.updateId != originalState.updateId, "paused world does not advance layer"));
        originalState.set(GameState.State.playing);
        int[] delayed = {0, 0};
        Tiles firstLayerTiles = first.call(() -> Vars.world.tiles);
        Tiles secondLayerTiles = second.call(() -> Vars.world.tiles);
        first.run(() -> Time.run(2f, () -> {
            check(Vars.world.tiles == firstLayerTiles, "first delayed task runs in its own layer");
            delayed[0]++;
        }));
        second.run(() -> Time.run(2f, () -> {
            check(Vars.world.tiles == secondLayerTiles, "second delayed task runs in its own layer");
            delayed[1]++;
        }));
        Time.update();
        Time.update();
        check(delayed[0] == 0 && delayed[1] == 0, "surface time cannot execute layer tasks");
        double mainTime = Time.getInternalTime();
        LayerSimulation.update(first, originalState);
        LayerSimulation.update(first, originalState);
        check(delayed[0] == 1 && delayed[1] == 0 && Time.getInternalTime() == mainTime,
                "one layer advances only its own tasks without advancing surface time");
        LayerSimulation.update(second, originalState);
        LayerSimulation.update(second, originalState);
        check(delayed[1] == 1, "second layer advances its own delayed task");
        int[] posted = {0};
        first.run(() -> Core.app.post(() -> {
            check(Vars.world.tiles == firstLayerTiles, "posted callback runs in its originating layer");
            posted[0]++;
        }));
        check(posted[0] == 0 && Core.app == app, "posted callback waits while surface context is restored");
        Runnable postedTask = app.posted.poll(2, TimeUnit.SECONDS);
        check(postedTask != null, "posted callback was queued");
        postedTask.run();
        check(posted[0] == 1 && Vars.world.tiles == originalTiles && Core.app == app, "posted callback restores surface afterwards");
        first.run(() -> Time.runTask(1f, () -> {
            check(Vars.world.tiles == firstLayerTiles, "timer callback runs in its originating layer");
            posted[0]++;
        }));
        Runnable timedTask = app.posted.poll(2, TimeUnit.SECONDS);
        check(timedTask != null, "timer callback was queued");
        timedTask.run();
        check(posted[0] == 2 && Vars.world.tiles == originalTiles, "timer callback restores surface afterwards");
        LayerContext retired = layer(new LayerEvents());
        retired.run(() -> Core.app.post(() -> posted[0]++));
        retired.close();
        Runnable retiredTask = app.posted.poll(2, TimeUnit.SECONDS);
        check(retiredTask != null, "retired layer callback was queued");
        retiredTask.run();
        check(posted[0] == 2 && Vars.world.tiles == originalTiles, "closed layer cannot execute a stale callback");
        first.run(() -> second.run(() -> {
            try {
                first.close();
                throw new AssertionError("closed a suspended layer");
            } catch (IllegalStateException expected) {
                check(true, "suspended layer cannot be closed");
            }
        }));
        first.run(() -> {
            Tiles firstTiles = Vars.world.tiles;
            var firstGroup = Groups.build;
            second.run(() -> check(Vars.world.tiles != firstTiles && Groups.build != firstGroup, "nested layer isolated"));
            check(Vars.world.tiles == firstTiles && Groups.build == firstGroup, "nested layer restored");
            first.run(() -> Vars.world.tileChanges = 31);
        });
        first.run(() -> check(Vars.world.tileChanges == 31, "same-layer nesting retains changes"));
        second.run(() -> check(Vars.world.tileChanges != 31, "world counters isolated"));
        first.run(() -> {
            Groups.resize(0, 0, 64, 64);
            var unit = unitType.create(Team.sharded);
            unit.set(24f, 32f);
            unit.health = 37f;
            unit.add();
        });
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        LayerSnapshotCodec.write(new DataOutputStream(bytes), first);
        GameState restoredState = new GameState();
        restoredState.rules.disableUnitCap = true;
        LayerContext restored = LayerSnapshotCodec.read(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())),
                restoredState, null, null, null, new LayerEvents());
        restored.run(() -> {
            check(Vars.world.build(3, 2).items.get(item) == 1, "snapshot restores belt cargo");
            check(Vars.world.build(5, 5) != null && Vars.world.build(5, 5) == Vars.world.build(6, 6), "snapshot restores multiblock occupancy");
            check(Groups.unit.size() == 1 && Groups.unit.first().health == 37f, "snapshot restores unit state");
        });
        try {
            byte[] truncated = java.util.Arrays.copyOf(bytes.toByteArray(), bytes.size() - 1);
            LayerSnapshotCodec.read(new DataInputStream(new ByteArrayInputStream(truncated)), restoredState, null, null, null, new LayerEvents());
            throw new AssertionError("truncated snapshot accepted");
        } catch (EOFException expected) {
            check(Vars.world == world && world.tiles == originalTiles && Groups.build == originalGroups, "truncated snapshot leaves outer world intact");
        }
        Map<String, LayerContext> archiveLayers = new LinkedHashMap<>();
        archiveLayers.put("wh:underground", first);
        archiveLayers.put("wh:other", second);
        ByteArrayOutputStream archiveBytes = new ByteArrayOutputStream();
        LayerArchive.write(new DataOutputStream(archiveBytes), archiveLayers);
        LayerArchive.Loader loader = (id, input) -> {
            GameState game = new GameState();
            game.rules.disableUnitCap = true;
            return LayerSnapshotCodec.read(input, game, null, null, null, new LayerEvents());
        };
        Map<String, LayerContext> loadedLayers = LayerArchive.read(new DataInputStream(new ByteArrayInputStream(archiveBytes.toByteArray())), loader);
        check(loadedLayers.size() == 2 && loadedLayers.containsKey("wh:underground") && loadedLayers.containsKey("wh:other"),
                "archive restores stable layer IDs");
        loadedLayers.get("wh:underground").run(() -> check(Groups.unit.size() == 1, "archive keeps entity state in its layer"));
        loadedLayers.get("wh:other").run(() -> check(Groups.unit.size() == 0, "archive keeps layers independent"));
        List<LayerContext> partiallyLoaded = new ArrayList<>();
        try {
            byte[] truncated = java.util.Arrays.copyOf(archiveBytes.toByteArray(), archiveBytes.size() - 1);
            LayerArchive.read(new DataInputStream(new ByteArrayInputStream(truncated)), (id, input) -> {
                LayerContext value = loader.read(id, input);
                partiallyLoaded.add(value);
                return value;
            });
            throw new AssertionError("truncated layer archive accepted");
        } catch (EOFException expected) {
            check(archiveLayers.size() == 2 && archiveLayers.get("wh:underground") == first,
                    "failed archive read does not replace existing layers");
            try {
                partiallyLoaded.get(0).run(() -> {
                });
                throw new AssertionError("partially loaded layer remains active");
            } catch (IllegalStateException closed) {
                check(true, "failed archive read closes partially loaded layers");
            }
        }
        LayerSet set = new LayerSet(8, 8);
        set.add("wh:underground", loadedLayers.get("wh:underground"));
        set.add("wh:other", loadedLayers.get("wh:other"));
        SaveVersion.addCustomChunk("wh-layer-test", new LayerSaveChunk(set, loader));
        ByteArrayOutputStream chunkBytes = new ByteArrayOutputStream();
        SaveIO.getSaveWriter().writeCustomChunks(new DataOutputStream(chunkBytes), true);
        check(chunkBytes.size() > 4, "layer chunk is included in initial network world data");
        SaveIO.getSaveWriter().readCustomChunks(new DataInputStream(new ByteArrayInputStream(chunkBytes.toByteArray())));
        check(set.layers().size() == 2 && set.get("wh:underground") != loadedLayers.get("wh:underground"),
                "vanilla custom chunk restores a fresh layer set");
        set.get("wh:underground").run(() -> check(Groups.unit.size() == 1, "custom chunk restores layer entities"));
        try {
            loadedLayers.get("wh:underground").run(() -> {
            });
            throw new AssertionError("replaced layer remains active");
        } catch (IllegalStateException expected) {
            check(true, "replacing layer set closes previous contexts");
        }
        set.close();
        check(first.call(() -> Vars.unitPhysics) != second.call(() -> Vars.unitPhysics), "unit physics worlds are separate");
        second.run(() -> {
            var unit = unitType.create(Team.sharded);
            unit.set(24f, 32f);
            unit.add();
        });
        for (int i = 0; i < 4; i++) {
            Time.update();
            originalState.updateId++;
            LayerSimulation.update(first, originalState);
            LayerSimulation.update(second, originalState);
        }
        first.run(() -> check(Math.abs(Groups.unit.first().x - 24f) < 0.01f, "first-layer unit ignores overlapping unit in another layer"));
        second.run(() -> check(Math.abs(Groups.unit.first().x - 24f) < 0.01f, "second-layer unit ignores overlapping unit in another layer"));
        int[] surfaceEvents = {0}, layerEvents = {0, 0, 0};
        Events.on(TileChangeEvent.class, event -> surfaceEvents[0]++);
        Events.on(TileFloorChangeEvent.class, event -> surfaceEvents[0]++);
        Events.on(TileOverlayChangeEvent.class, event -> surfaceEvents[0]++);
        secondEvents.add(new LayerEvents.Listener() {
            public void changed(mindustry.world.Tile tile) {
                layerEvents[0]++;
            }

            public void floorChanged(mindustry.world.Tile tile, Floor previous, Floor floor) {
                layerEvents[1]++;
            }

            public void overlayChanged(mindustry.world.Tile tile, Floor previous, Floor overlay) {
                layerEvents[2]++;
            }
        });
        Floor otherFloor = new Floor("other-floor");
        otherFloor.init();
        second.run(() -> {
            Vars.world.setGenerating(false);
            Vars.world.tile(2, 2).setAir();
            Vars.world.tile(2, 2).setFloor(otherFloor);
            Vars.world.tile(2, 2).setOverlay(otherFloor);
        });
        check(layerEvents[0] > 0 && layerEvents[1] == 1 && layerEvents[2] == 1, "layer receives block, floor and overlay events");
        check(surfaceEvents[0] == 0, "layer tile changes do not emit surface events");
        first.run(() -> check(Vars.world.build(2, 2) != null, "removal does not touch overlapping layer"));
        int[] globalWorldLoads = {0};
        Events.on(WorldLoadEvent.class, event -> globalWorldLoads[0]++);
        first.run(() -> {
            Vars.world.setGenerating(false);
            LayerIndexer local = LayerIndexer.create(firstEvents);
            Vars.indexer = local.indexer();
            check(local.indexer().getFlagged(Team.sharded, BlockFlag.storage).size == 2, "layer indexer initializes from its own tiles");
            Vars.world.tile(2, 2).setAir();
            check(local.indexer().getFlagged(Team.sharded, BlockFlag.storage).size == 1, "layer indexer tracks demolition");
        });
        check(globalWorldLoads[0] == 0 && surfaceEvents[0] == 0, "layer indexer does not broadcast main-world events");
        BasicBulletType bullet = new BasicBulletType(6f, 10f);
        bullet.hitEffect = Fx.none;
        bullet.despawnEffect = Fx.none;
        bullet.init();
        for (LayerContext layer : new LayerContext[]{first, second}) {
            layer.run(() -> Vars.world.tile(4, 1).setBlock(belt, Team.crux, 0));
        }
        float otherHealth = second.call(() -> Vars.world.build(4, 1).health);
        float targetHealth = first.call(() -> Vars.world.build(4, 1).health);
        first.run(() -> bullet.create(null, Team.sharded, 16f, 8f, 0f));
        for (int i = 0; i < 5; i++) {
            Time.update();
            originalState.updateId++;
            LayerSimulation.update(first, originalState);
            LayerSimulation.update(second, originalState);
        }
        first.run(() -> check(Vars.world.build(4, 1).health < targetHealth, "bullet damages target in its own layer"));
        second.run(() -> check(Vars.world.build(4, 1).health == otherHealth, "bullet cannot damage overlapping target in another layer"));
        int[] globalBuildEvents = {0}, localBuildEvents = {0};
        Events.on(BlockBuildBeginEvent.class, event -> globalBuildEvents[0]++);
        Events.on(BlockBuildEndEvent.class, event -> globalBuildEvents[0]++);
        firstEvents.on(BlockBuildBeginEvent.class, event -> localBuildEvents[0]++);
        firstEvents.on(BlockBuildEndEvent.class, event -> localBuildEvents[0]++);
        Vars.fogControl = new mindustry.game.FogControl();
        mindustry.ai.Pathfinder testPathfinder = new mindustry.ai.Pathfinder() {
            @Override
            public void updateTile(mindustry.world.Tile tile) {
            }
        };
        ConstructBlock construction = new ConstructBlock(1);
        construction.init();
        first.run(() -> {
            Vars.state.rules.editor = true;
            Vars.pathfinder = testPathfinder;
            var builder = unitType.create(Team.sharded);
            Build.beginPlace(builder, belt, Team.sharded, 1, 1, 1, null);
            check(Vars.world.build(1, 1) instanceof ConstructBuild, "placement starts vanilla construction");
            ConstructBuild pending = (ConstructBuild) Vars.world.build(1, 1);
            pending.construct(builder, null, 0.4f, null);
            check(pending.progress > 0f && pending.progress < 1f, "construction retains intermediate progress");
            try {
                ByteArrayOutputStream inProgress = new ByteArrayOutputStream();
                LayerSnapshotCodec.write(new DataOutputStream(inProgress), first);
                GameState loadedGame = new GameState();
                loadedGame.rules.disableUnitCap = true;
                LayerContext loaded = LayerSnapshotCodec.read(new DataInputStream(new ByteArrayInputStream(inProgress.toByteArray())),
                        loadedGame, null, testPathfinder, null, new LayerEvents());
                loaded.run(() -> {
                    Building build = Vars.world.build(1, 1);
                    check(build instanceof ConstructBuild && Math.abs(((ConstructBuild) build).progress - 0.4f) < 0.001f,
                            "snapshot restores construction progress");
                });
            } catch (IOException failure) {
                throw new UncheckedIOException(failure);
            }
            pending.construct(builder, null, 0.6f, null);
            check(Vars.world.build(1, 1) != null && Vars.world.build(1, 1).block == belt && Vars.world.build(1, 1).rotation == 1,
                    "construction completes with selected rotation");
            Build.beginBreak(builder, Team.sharded, 1, 1);
            check(Vars.world.build(1, 1) instanceof ConstructBuild, "demolition starts vanilla construction state");
            ConstructBuild breaking = (ConstructBuild) Vars.world.build(1, 1);
            breaking.deconstruct(builder, null, 0.4f);
            check(breaking.progress > 0f && breaking.progress < 1f, "demolition retains intermediate progress");
            breaking.deconstruct(builder, null, 0.6f);
            check(Vars.world.build(1, 1) == null, "demolition finishes in the selected layer");
        });
        check(globalBuildEvents[0] == 0 && localBuildEvents[0] == 4, "construction events stay in their layer");
        unitType.buildSpeed = 1f;
        belt.buildTime = 10f;
        belt.buildVisibility = BuildVisibility.shown;
        Team.sharded.rules().infiniteResources = true;
        var surfaceBuilder = unitType.create(Team.sharded);
        surfaceBuilder.set(8f, 8f);
        BuildPlan surfacePlan = new BuildPlan(6, 6, 0, belt);
        surfaceBuilder.plans().addLast(surfacePlan);
        LayerBuilder layerBuilder = new LayerBuilder(first);
        layerBuilder.run(surfaceBuilder, () -> surfaceBuilder.addBuild(new BuildPlan(1, 3, 2, belt)));
        layerBuilder.update(surfaceBuilder);
        first.run(() -> check(Vars.world.build(1, 3) instanceof ConstructBuild &&
                ((ConstructBuild) Vars.world.build(1, 3)).progress < 1f, "surface builder starts timed construction in selected layer"));
        check(surfaceBuilder.plans().size == 1 && surfaceBuilder.plans().first() == surfacePlan,
                "layer build queue does not replace the surface queue");
        for (int i = 0; i < 12; i++) layerBuilder.update(surfaceBuilder);
        first.run(() -> check(Vars.world.build(1, 3) != null && Vars.world.build(1, 3).block == belt &&
                Vars.world.build(1, 3).rotation == 2, "surface builder completes selected-layer plan"));
        check(surfaceBuilder.plans().size == 1 && surfaceBuilder.plans().first() == surfacePlan,
                "surface plan survives underground construction");
        layerBuilder.run(surfaceBuilder, () -> surfaceBuilder.addBuild(new BuildPlan(1, 3)));
        layerBuilder.update(surfaceBuilder);
        first.run(() -> check(Vars.world.build(1, 3) instanceof ConstructBuild &&
                ((ConstructBuild) Vars.world.build(1, 3)).progress > 0f, "surface builder starts timed demolition in selected layer"));
        for (int i = 0; i < 12; i++) layerBuilder.update(surfaceBuilder);
        first.run(() -> check(Vars.world.build(1, 3) == null, "surface builder completes selected-layer demolition"));
        check(surfaceBuilder.plans().size == 1 && surfaceBuilder.plans().first() == surfacePlan,
                "surface plan survives underground demolition");
        Team.sharded.rules().infiniteResources = false;
        belt.requirements = new ItemStack[]{new ItemStack(item, 2)};
        CoreBlock coreBlock = new CoreBlock("layer-supply-core");
        coreBlock.itemCapacity = 100;
        coreBlock.init();
        CoreBlock.CoreBuild supplyCore = second.call(() -> {
            Vars.world.tile(6, 4).setBlock(coreBlock, Team.sharded, 0);
            CoreBlock.CoreBuild core = (CoreBlock.CoreBuild) Vars.world.build(6, 4);
            core.storageCapacity = 100;
            core.items.add(item, 10);
            core.placed();
            return core;
        });
        layerBuilder.run(surfaceBuilder, () -> surfaceBuilder.addBuild(new BuildPlan(1, 4, 0, belt)));
        for (int i = 0; i < 12; i++) layerBuilder.update(surfaceBuilder, supplyCore);
        first.run(() -> check(Vars.world.build(1, 4) != null && Vars.world.build(1, 4).block == belt,
                "layer construction completes using surface core resources"));
        check(supplyCore.items.get(item) == 8, "layer construction consumes the correct surface inventory");
        first.run(() -> check(Team.sharded.cores().isEmpty(), "borrowed core does not remain in layer team state"));
        supplyCore.items.set(item, 0);
        layerBuilder.run(surfaceBuilder, () -> surfaceBuilder.addBuild(new BuildPlan(2, 4, 0, belt)));
        for (int i = 0; i < 3; i++) layerBuilder.update(surfaceBuilder, supplyCore);
        first.run(() -> check(Vars.world.build(2, 4) == null, "layer construction waits for missing resources"));
        supplyCore.items.add(item, 2);
        for (int i = 0; i < 12; i++) layerBuilder.update(surfaceBuilder, supplyCore);
        first.run(() -> check(Vars.world.build(2, 4) != null && Vars.world.build(2, 4).block == belt,
                "layer construction resumes after resources arrive"));
        check(supplyCore.items.get(item) == 0, "resumed construction consumes the supplied resources");
        layerBuilder.run(surfaceBuilder, () -> surfaceBuilder.addBuild(new BuildPlan(2, 4)));
        for (int i = 0; i < 12; i++) layerBuilder.update(surfaceBuilder, supplyCore);
        first.run(() -> check(Vars.world.build(2, 4) == null, "resource-backed demolition completes"));
        check(supplyCore.items.get(item) == Math.round(2 * originalState.rules.deconstructRefundMultiplier),
                "demolition refunds the correct amount to the surface core");
        LayerEvents pathEvents = new LayerEvents();
        LayerContext pathLayer = layer(pathEvents);
        StaticWall wall = new StaticWall("path-test-wall");
        wall.init();
        UnitType groundType = new UnitType("path-test-unit");
        groundType.constructor = MechUnit::create;
        groundType.init();
        pathLayer.run(() -> {
            Vars.world.setGenerating(true);
            for (var tile : Vars.world.tiles) tile.setFloor((Floor) Blocks.stone);
            Groups.resize(0, 0, 64, 64);
            Vars.world.setGenerating(false);
            Vars.world.tile(3, 0).setBlock(wall);
            Vars.world.tile(3, 1).setBlock(wall);
            var unit = groundType.create(Team.sharded);
            unit.set(8f, 8f);
            unit.add();
            LayerControlPathfinder pathfinder = new LayerControlPathfinder(pathEvents);
            Vars.controlPath = pathfinder;
            arc.math.geom.Vec2 destination = new arc.math.geom.Vec2(48f, 8f);
            int steps = 0;
            while (unit.tileX() != 6 || unit.tileY() != 1) {
                Vars.state.updateId++;
                var route = pathfinder.getPathPosition(unit, destination);
                check(route.move && route.next != null && route.next.block() != wall,
                        "layer route avoids walls at " + unit.tileX() + "," + unit.tileY() + " move=" + route.move + " unreachable=" + route.unreachable);
                unit.set(route.dest.x, route.dest.y);
                if (++steps > 30) throw new AssertionError("layer route did not reach destination");
            }
            for (int y = 0; y < 8; y++) Vars.world.tile(3, y).setBlock(wall);
            unit.set(8f, 8f);
            Vars.state.updateId++;
            var blocked = pathfinder.getPathPosition(unit, destination);
            check(blocked.unreachable && !blocked.move, "layer route detects a newly blocked path");
        });
        pathLayer.close();
        mindustry.ai.Pathfinder oldPathfinder = Vars.pathfinder;
        mindustry.ai.Pathfinder surfacePathfinder = new mindustry.ai.Pathfinder() {
            @Override
            public int get(int x, int y) {
                return 11;
            }
        };
        mindustry.ai.Pathfinder localPathfinder = new mindustry.ai.Pathfinder() {
            @Override
            public int get(int x, int y) {
                return 22;
            }
        };
        java.lang.reflect.Field grid = mindustry.ai.Pathfinder.class.getDeclaredField("tiles");
        grid.setAccessible(true);
        int[] surfaceGrid = {11};
        grid.set(surfacePathfinder, surfaceGrid);
        Vars.pathfinder = surfacePathfinder;
        LayerPathRouter router = LayerPathRouter.install();
        LayerEvents routedEvents = new LayerEvents();
        LayerContext routedLayer = LayerContext.create(LayerTile.createTiles(8, 8, routedEvents), new GameState(),
                null, localPathfinder, null, routedEvents);
        routedLayer.run(() -> {
            check(Vars.pathfinder == router && router.get(0, 0) == 22, "main-thread path queries use the active layer");
            AtomicReference<Integer> workerResult = new AtomicReference<>();
            Thread worker = new Thread(() -> workerResult.set(router.get(0, 0)));
            worker.start();
            try {
                worker.join();
            } catch (InterruptedException failure) {
                throw new RuntimeException(failure);
            }
            check(workerResult.get() == 11, "background path queries retain the surface pathfinder");
            try {
                check(grid.get(router) == surfaceGrid, "surface worker grid remains bound to surface data");
            } catch (IllegalAccessException failure) {
                throw new RuntimeException(failure);
            }
        });
        routedLayer.close();
        LayerEvents nativeEvents = new LayerEvents();
        GameState nativeGame = new GameState();
        nativeGame.rules.waveTeam = Team.sharded;
        LayerContext nativeLayer = LayerContext.create(LayerTile.createTiles(8, 8, nativeEvents), nativeGame,
                null, null, null, nativeEvents);
        nativeLayer.run(() -> {
            Vars.world.setGenerating(true);
            for (var tile : Vars.world.tiles) tile.setFloor((Floor) Blocks.stone);
            Vars.world.setGenerating(false);
            LayerWaveSpawner.create(nativeEvents);
            LayerNativePathfinder.create(nativeLayer, nativeEvents);
            int before = Vars.pathfinder.get(3, 3);
            Vars.world.tile(3, 3).setBlock(wall);
            check(Vars.pathfinder.get(3, 3) != before, "native layer flowfield tracks layer tile changes");
            check(surfaceGrid[0] == 11, "native layer path updates leave surface grid untouched");
        });
        nativeLayer.close();
        LayerContext createdLayer = LayerWorld.create("wh:factory-test", firstLayerTiles, originalState, Vars.net);
        createdLayer.run(() -> {
            check(Vars.world.tile(2, 2).floor() == firstLayerTiles.get(2, 2).floor(), "new layer copies surface terrain");
            check(Vars.world.build(3, 2) == null, "new layer does not copy surface buildings");
            Vars.world.tile(3, 2).setBlock(belt, Team.sharded, 1);
        });
        ByteArrayOutputStream worldBytes = new ByteArrayOutputStream();
        LayerSnapshotCodec.write(new DataOutputStream(worldBytes), createdLayer);
        LayerContext loadedWorld = LayerWorld.read("wh:factory-test",
                new DataInputStream(new ByteArrayInputStream(worldBytes.toByteArray())), originalState, Vars.net);
        loadedWorld.run(() -> {
            check(Vars.world.build(3, 2) != null && Vars.world.build(3, 2).rotation == 1, "restored layer initializes buildings and rotation");
            check(Vars.indexer != null && Vars.spawner != null && Vars.controlPath instanceof LayerControlPathfinder,
                    "restored layer initializes its own subsystems");
        });
        createdLayer.close();
        loadedWorld.close();
        router.close();
        check(Vars.pathfinder == surfacePathfinder, "path router restores the original surface pathfinder");
        Vars.pathfinder = oldPathfinder;
        LayerItemPort exporter = new LayerItemPort("layer-export-port", true);
        LayerItemPort importer = new LayerItemPort("layer-import-port", false);
        exporter.init();
        importer.init();
        originalState.rules.editor = false;
        LayerContext itemSource = layer(new LayerEvents());
        LayerContext itemTarget = layer(new LayerEvents());
        for (LayerContext itemLayer : new LayerContext[]{itemSource, itemTarget}) {
            itemLayer.run(() -> {
                Vars.world.setGenerating(true);
                Groups.resize(0, 0, 64, 64);
                for (var tile : Vars.world.tiles) tile.setFloor((Floor) Blocks.stone);
            });
        }
        itemSource.run(() -> {
            Vars.world.tile(2, 4).setBlock(belt, Team.sharded, 0);
            Vars.world.tile(3, 4).setBlock(exporter, Team.sharded, 0);
            Vars.world.build(2, 4).updateProximity();
            Vars.world.build(3, 4).updateProximity();
            Building conveyor = Vars.world.build(2, 4);
            conveyor.handleItem(conveyor, item);
        });
        itemTarget.run(() -> {
            Vars.world.tile(3, 4).setBlock(importer, Team.sharded, 0);
            Vars.world.tile(4, 4).setBlock(belt, Team.sharded, 0);
            Vars.world.tile(5, 4).setBlock(belt, Team.sharded, 0);
            Vars.world.build(3, 4).updateProximity();
            Vars.world.build(4, 4).updateProximity();
            Vars.world.build(5, 4).updateProximity();
        });
        int portPosition = Point2.pack(3, 4);
        for (int i = 0; i < 70; i++) {
            Time.update();
            originalState.updateId++;
            LayerSimulation.update(itemSource, originalState);
            LayerItemLink.transfer(itemSource, portPosition, itemTarget, portPosition);
            LayerSimulation.update(itemTarget, originalState);
        }
        itemTarget.run(() -> check(Vars.world.build(5, 4).items.get(item) == 1,
                "directional ports transfer conveyor items between layers"));
        itemSource.run(() -> Vars.world.build(3, 4).items.add(item, 1));
        itemTarget.run(() -> Vars.world.build(3, 4).items.add(item, importer.itemCapacity));
        check(!LayerItemLink.transfer(itemSource, portPosition, itemTarget, portPosition), "full destination rejects cross-layer transfer");
        itemSource.run(() -> check(Vars.world.build(3, 4).items.get(item) == 1, "rejected transfer preserves the source item"));
        Tiles mainTiles = new Tiles(8, 8);
        mainTiles.fill();
        boolean oldGenerating = Vars.world.isGenerating();
        Vars.world.tiles = mainTiles;
        Vars.world.setGenerating(true);
        for (var tile : mainTiles) tile.setFloor((Floor) Blocks.stone);
        Vars.world.tile(3, 4).setBlock(importer, Team.sharded, 0);
        check(LayerItemLink.transfer(itemSource, portPosition, null, portPosition), "layer port can send to the surface");
        check(Vars.world.build(3, 4).items.get(item) == 1, "surface receives an item from the layer");
        Vars.world.tile(3, 4).setBlock(exporter, Team.sharded, 0);
        Vars.world.build(3, 4).items.add(item, 1);
        itemTarget.run(() -> Vars.world.build(3, 4).items.clear());
        check(LayerItemLink.transfer(null, portPosition, itemTarget, portPosition), "surface port can send to a layer");
        itemTarget.run(() -> check(Vars.world.build(3, 4).items.get(item) == 1, "layer receives an item from the surface"));
        Vars.world.tile(3, 4).setAir();
        Vars.world.tiles = originalTiles;
        Vars.world.setGenerating(oldGenerating);
        LayerContext rotationLayer = layer(new LayerEvents());
        rotationLayer.run(() -> {
            Vars.world.setGenerating(true);
            for (var tile : Vars.world.tiles) tile.setFloor((Floor) Blocks.stone);
            for (int rotation = 0; rotation < 4; rotation++) {
                for (int dir = 0; dir < 4; dir++) {
                    Vars.world.tile(4 + arc.math.geom.Geometry.d4x[dir], 4 + arc.math.geom.Geometry.d4y[dir]).setAir();
                }
                int dx = arc.math.geom.Geometry.d4x[rotation], dy = arc.math.geom.Geometry.d4y[rotation];
                Vars.world.tile(4, 4).setBlock(exporter, Team.sharded, rotation);
                Vars.world.tile(4 - dx, 4 - dy).setBlock(belt, Team.sharded, rotation);
                Vars.world.tile(4 + dx, 4 + dy).setBlock(belt, Team.sharded, rotation);
                Building port = Vars.world.build(4, 4);
                check(port.acceptItem(Vars.world.build(4 - dx, 4 - dy), item) &&
                        !port.acceptItem(Vars.world.build(4 + dx, 4 + dy), item), "port accepts only from its back at rotation " + rotation);
            }
        });
        rotationLayer.close();
        itemSource.close();
        itemTarget.close();
        LayerContext shaftLayer = layer(new LayerEvents());
        shaftLayer.run(() -> {
            Vars.world.setGenerating(true);
            for (var tile : Vars.world.tiles) tile.setFloor((Floor) Blocks.stone);
            Vars.world.setGenerating(false);
        });
        Vars.world.tiles = mainTiles;
        Vars.world.setGenerating(true);
        LayerShaftBlock shaft = new LayerShaftBlock("layer-shaft-in", true, () -> shaftLayer, importer);
        shaft.init();
        check(shaft.canPlaceOn(Vars.world.tile(3, 4), Team.sharded, 0), "shaft can be placed above an empty layer tile");
        shaftLayer.run(() -> Vars.world.tile(3, 4).setBlock(wall));
        check(!shaft.canPlaceOn(Vars.world.tile(3, 4), Team.sharded, 0), "shaft rejects an occupied layer tile");
        shaftLayer.run(() -> Vars.world.tile(3, 4).setAir());
        Vars.world.tile(3, 4).setBlock(shaft, Team.sharded, 0);
        Building surfaceShaft = Vars.world.build(3, 4);
        surfaceShaft.items.add(item, 1);
        surfaceShaft.updateTile();
        shaftLayer.run(() -> check(Vars.world.build(3, 4) != null && Vars.world.build(3, 4).block == importer &&
                Vars.world.build(3, 4).items.get(item) == 1, "shaft creates its layer endpoint and transfers items"));
        surfaceShaft.rotation = 1;
        surfaceShaft.updateTile();
        shaftLayer.run(() -> check(Vars.world.build(3, 4).rotation == 1 && Vars.world.build(3, 4).items.get(item) == 1,
                "shaft rotation updates the endpoint without losing items"));
        Vars.world.setGenerating(false);
        surfaceShaft.onRemoved();
        shaftLayer.run(() -> check(Vars.world.build(3, 4) == null, "removing shaft removes its linked endpoint"));
        Vars.world.setGenerating(true);
        Vars.world.tile(3, 4).setAir();
        Vars.world.tiles = originalTiles;
        Vars.world.setGenerating(oldGenerating);
        shaftLayer.close();
        SetTileCallPacket packet = new SetTileCallPacket();
        second.run(() -> {
            packet.tile = Vars.world.tile(1, 1);
            packet.block = belt;
            packet.team = Team.sharded;
            packet.rotation = 2;
        });
        byte[] encoded = LayerPacketCodec.encode(packet);
        Vars.net.setClientLoaded(true);
        try {
            LayerPacketCodec.deliverClient(second, encoded, type -> false);
            throw new AssertionError("disallowed packet accepted");
        } catch (IOException expected) {
            check(true, "packet allowlist rejects unexpected packet types");
        }
        LayerPacketCodec.deliverClient(second, encoded, type -> type == SetTileCallPacket.class);
        second.run(() -> check(Vars.world.build(1, 1) != null && Vars.world.build(1, 1).rotation == 2,
                "vanilla packet resolves coordinates in selected layer"));
        first.run(() -> check(Vars.world.build(1, 1) == null, "layer packet does not mutate overlapping layer"));
        LayerRpcPacket envelope = new LayerRpcPacket("wh:underground", encoded);
        ByteArrayOutputStream wire = new ByteArrayOutputStream();
        envelope.write(new Writes(new DataOutputStream(wire)));
        LayerRpcPacket received = new LayerRpcPacket();
        received.read(new Reads(new DataInputStream(new ByteArrayInputStream(wire.toByteArray()))), wire.size());
        received.handled();
        LayerRpcPacket.setReceiver((id, payload) -> {
            if (!id.equals("wh:underground")) throw new IllegalArgumentException("unknown layer");
            try {
                LayerPacketCodec.deliverClient(first, payload, type -> type == SetTileCallPacket.class);
            } catch (IOException failure) {
                throw new UncheckedIOException(failure);
            }
        });
        check(!received.allow(true) && received.allow(false), "layer RPC only accepts server-to-client direction");
        received.handleClient();
        first.run(() -> check(Vars.world.build(1, 1) != null && Vars.world.build(1, 1).rotation == 2,
                "network envelope routes vanilla packet by layer ID"));
        LayerRpcPacket.setReceiver(null);
        class RecordingNet extends Net {
            Object last;
            List<LayerRpcPacket> sent = new ArrayList<>();
            boolean reliable;

            RecordingNet() {
                super(null);
            }

            @Override
            public boolean server() {
                return true;
            }

            @Override
            public boolean active() {
                return true;
            }

            @Override
            public boolean client() {
                return false;
            }

            @Override
            public void send(Object object, boolean reliable) {
                last = object;
                this.reliable = reliable;
                sent.add((LayerRpcPacket) object);
            }
        }
        RecordingNet transport = new RecordingNet();
        Net mainNet = Vars.net;
        LayerNet layerNet = new LayerNet(transport, "wh:underground");
        first.run(() -> {
            Vars.net = layerNet;
            Call.setTile(Vars.world.tile(1, 3), belt, Team.sharded, 3);
        });
        check(Vars.net == mainNet && transport.last instanceof LayerRpcPacket, "vanilla server broadcast is wrapped and outer net restored");
        LayerRpcPacket routed = (LayerRpcPacket) transport.last;
        check("wh:underground".equals(routed.layerId), "wrapped broadcast includes layer ID");
        LayerRpcPacket.setReceiver((id, payload) -> {
            try {
                LayerPacketCodec.deliverClient(second, payload, type -> type == SetTileCallPacket.class);
            } catch (IOException failure) {
                throw new UncheckedIOException(failure);
            }
        });
        routed.handleClient();
        second.run(() -> check(Vars.world.build(1, 3) != null && Vars.world.build(1, 3).rotation == 3,
                "wrapped vanilla broadcast applies to target layer"));
        first.run(() -> Vars.net = mainNet);
        LayerRpcPacket.setReceiver(null);
        byte[] largePayload = new byte[1450];
        for (int i = 0; i < largePayload.length; i++) largePayload[i] = (byte) i;
        LayerRpcPacket part0 = new LayerRpcPacket("wh:underground", 42, 0, 3, java.util.Arrays.copyOfRange(largePayload, 0, 600));
        LayerRpcPacket part1 = new LayerRpcPacket("wh:underground", 42, 1, 3, java.util.Arrays.copyOfRange(largePayload, 600, 1200));
        LayerRpcPacket part2 = new LayerRpcPacket("wh:underground", 42, 2, 3, java.util.Arrays.copyOfRange(largePayload, 1200, 1450));
        LayerRpcAssembler assembler = new LayerRpcAssembler();
        check(assembler.accept(part2, 1) == null && assembler.accept(part0, 2) == null && assembler.accept(part0, 3) == null,
                "fragment reassembly tolerates reordering and duplicates");
        check(java.util.Arrays.equals(assembler.accept(part1, 4), largePayload), "fragment reassembly restores full payload");
        check(assembler.accept(part1, 5) == null, "completed fragmented message is not replayed");
        LayerRpcAssembler expired = new LayerRpcAssembler();
        expired.accept(part0, 0);
        check(expired.accept(part1, 30001) == null && expired.accept(part2, 30002) == null,
                "incomplete fragments expire");
        Net.registerPacket(LargeTestPacket::new);
        LargeTestPacket largePacket = new LargeTestPacket();
        transport.sent.clear();
        layerNet.send(largePacket, true);
        check(transport.sent.size() == 3 && transport.reliable, "large vanilla packet is split into reliable fragments");
        LayerRpcAssembler actualFragments = new LayerRpcAssembler();
        byte[] reconstructed = null;
        for (LayerRpcPacket part : transport.sent) {
            byte[] result = actualFragments.accept(part, 5);
            if (result != null) reconstructed = result;
        }
        check(java.util.Arrays.equals(reconstructed, LayerPacketCodec.encode(largePacket)), "sent fragments preserve vanilla packet bytes");
        try {
            first.run(() -> {
                throw new IllegalArgumentException("expected");
            });
            throw new AssertionError("missing exception");
        } catch (IllegalArgumentException expected) {
            check(Vars.world == world && world.tiles == originalTiles && Vars.state == originalState && Groups.build == originalGroups,
                    "outer globals restored after exception");
        }
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                first.run(() -> {
                });
            } catch (Throwable caught) {
                failure.set(caught);
            }
        });
        thread.start();
        thread.join();
        check(failure.get() instanceof IllegalStateException, "cross-thread access rejected");
        Vars.world = new World();
        try {
            first.run(() -> {
            });
            throw new AssertionError("old layer accepted a new world");
        } catch (IllegalStateException expected) {
            check(Vars.world != world, "old layer rejected after world replacement");
        }
        Vars.world = world;
    }
}
