package wh.entities.world.layers;

import arc.Events;
import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.game.EventType.*;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Tile notifications scoped to a single layer.
 */
public final class LayerEvents {
    private static final Class<?>[] scopedTypes = {
            BlockBuildBeginEvent.class, BlockBuildEndEvent.class, BuildRotateEvent.class, BuildSelectEvent.class,
            BlockDestroyEvent.class, BuildingBulletDestroyEvent.class, UnitDestroyEvent.class, UnitDrownEvent.class,
            StateChangeEvent.class
    };
    private final Seq<Listener> listeners = new Seq<>();
    private final ObjectMap<Class<?>, Seq<Cons<?>>> scoped = new ObjectMap<>();

    public LayerEvents() {
        for (Class<?> type : scopedTypes) scoped.put(type, new Seq<>(Cons.class));
    }

    public <T> void on(Class<T> type, Cons<T> listener) {
        Seq<Cons<?>> handlers = scoped.get(type);
        if (handlers == null) throw new IllegalArgumentException("Event is not layer-scoped: " + type.getName());
        handlers.add(Objects.requireNonNull(listener, "listener"));
    }

    Scope enter() {
        ObjectMap<Object, Seq<Cons<?>>> registry = eventRegistry();
        Seq<Seq<Cons<?>>> previous = new Seq<>();
        for (int i = 0; i < scopedTypes.length; i++) {
            previous.add(registry.put(scopedTypes[i], scoped.get(scopedTypes[i])));
        }
        return () -> {
            for (int i = 0; i < scopedTypes.length; i++) {
                if (previous.get(i) == null) registry.remove(scopedTypes[i]);
                else registry.put(scopedTypes[i], previous.get(i));
            }
        };
    }

    @SuppressWarnings("unchecked")
    static ObjectMap<Object, Seq<Cons<?>>> eventRegistry() {
        try {
            Field field = Events.class.getDeclaredField("events");
            field.setAccessible(true);
            return (ObjectMap<Object, Seq<Cons<?>>>) field.get(null);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Unsupported Arc event registry", failure);
        }
    }

    interface Scope extends AutoCloseable {
        @Override
        void close();
    }

    public void add(Listener listener) {
        listeners.addUnique(Objects.requireNonNull(listener, "listener"));
    }

    public void remove(Listener listener) {
        listeners.remove(listener, true);
    }

    void preChange(Tile tile) {
        for (Listener listener : listeners.copy()) listener.preChange(tile);
    }

    void changed(Tile tile) {
        for (Listener listener : listeners.copy()) listener.changed(tile);
    }

    void floorChanged(Tile tile, Floor previous, Floor floor) {
        for (Listener listener : listeners.copy()) listener.floorChanged(tile, previous, floor);
    }

    void overlayChanged(Tile tile, Floor previous, Floor overlay) {
        for (Listener listener : listeners.copy()) listener.overlayChanged(tile, previous, overlay);
    }

    void afterUpdate() {
        for (Listener listener : listeners.copy()) listener.afterUpdate();
    }

    public interface Listener {
        default void preChange(Tile tile) {
        }

        default void changed(Tile tile) {
        }

        default void floorChanged(Tile tile, Floor previous, Floor floor) {
        }

        default void overlayChanged(Tile tile, Floor previous, Floor overlay) {
        }

        default void afterUpdate() {
        }
    }
}
