package wh.entities.world.layers;

import arc.struct.Queue;
import arc.struct.Seq;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Unit;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Keeps a surface builder's plans and builder progress separate for one layer.
 */
public final class LayerBuilder {
    private static final Map<Class<?>, Fields> fields = new HashMap<>();

    private final LayerContext layer;
    private final Map<Unit, BuilderState> builders = new IdentityHashMap<>();

    public LayerBuilder(LayerContext layer) {
        this.layer = Objects.requireNonNull(layer, "layer");
    }

    public void run(Unit unit, Runnable action) {
        Objects.requireNonNull(action, "action");
        call(unit, () -> {
            action.run();
            return null;
        });
    }

    public <T> T call(Unit unit, Supplier<T> action) {
        Objects.requireNonNull(unit, "unit");
        Objects.requireNonNull(action, "action");
        return layer.call(() -> {
            BuilderState local = builders.computeIfAbsent(unit, ignored -> new BuilderState());
            if (unit.plans() == local.plans) return action.get();
            Fields access = fields.computeIfAbsent(unit.getClass(), Fields::new);
            BuilderState outer = access.capture(unit);
            try {
                access.install(unit, local);
                return action.get();
            } finally {
                try {
                    builders.put(unit, access.capture(unit));
                } finally {
                    access.install(unit, outer);
                }
            }
        });
    }

    public void update(Unit unit) {
        run(unit, unit::updateBuildLogic);
    }

    /**
     * Allows a surface builder to use its team's surface core while working in this layer.
     */
    public void update(Unit unit, CoreBuild surfaceCore) {
        run(unit, () -> {
            Seq<CoreBuild> cores = unit.team.data().cores;
            boolean borrowed = cores.isEmpty() && surfaceCore != null && surfaceCore.team == unit.team && surfaceCore.isValid();
            if (borrowed) cores.add(surfaceCore);
            try {
                unit.updateBuildLogic();
            } finally {
                if (borrowed) cores.remove(surfaceCore, true);
            }
        });
    }

    public void remove(Unit unit) {
        builders.remove(unit);
    }

    public void clear() {
        builders.clear();
    }

    private static final class BuilderState {
        Queue<BuildPlan> plans = new Queue<>(1);
        float counter, alpha;
        BuildPlan lastActive;
        int lastSize;
    }

    private static final class Fields {
        final Field counter, lastActive, lastSize;

        Fields(Class<?> type) {
            counter = find(type, "buildCounter");
            lastActive = find(type, "lastActive");
            lastSize = find(type, "lastSize");
        }

        BuilderState capture(Unit unit) {
            try {
                BuilderState value = new BuilderState();
                value.plans = unit.plans();
                value.counter = counter.getFloat(unit);
                value.lastActive = (BuildPlan) lastActive.get(unit);
                value.lastSize = lastSize.getInt(unit);
                value.alpha = unit.buildAlpha;
                return value;
            } catch (IllegalAccessException failure) {
                throw new IllegalStateException("Cannot capture builder state", failure);
            }
        }

        void install(Unit unit, BuilderState value) {
            try {
                unit.plans(value.plans);
                counter.setFloat(unit, value.counter);
                lastActive.set(unit, value.lastActive);
                lastSize.setInt(unit, value.lastSize);
                unit.buildAlpha = value.alpha;
            } catch (IllegalAccessException failure) {
                throw new IllegalStateException("Cannot install builder state", failure);
            }
        }

        private static Field find(Class<?> type, String name) {
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                try {
                    Field field = current.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                }
            }
            throw new IllegalStateException("Unsupported builder field: " + type.getName() + "." + name);
        }
    }
}
