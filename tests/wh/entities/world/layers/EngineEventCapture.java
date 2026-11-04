package wh.entities.world.layers;

import arc.func.Cons;
import arc.struct.ObjectMap;
import arc.struct.Seq;

/**
 * Detaches listeners registered by an engine object constructed for a layer.
 */
final class EngineEventCapture {
    private final Object[] keys;
    private final int[] sizes;
    private final ObjectMap<Object, Seq<Cons<?>>> registry;

    EngineEventCapture(Object... keys) {
        this.keys = keys;
        this.sizes = new int[keys.length];
        this.registry = LayerEvents.eventRegistry();
        for (int i = 0; i < keys.length; i++) {
            Seq<Cons<?>> listeners = registry.get(keys[i]);
            sizes[i] = listeners == null ? 0 : listeners.size;
        }
    }

    ObjectMap<Object, Cons<Object>> finish() {
        ObjectMap<Object, Cons<Object>> captured = new ObjectMap<>();
        boolean valid = true;
        for (int i = 0; i < keys.length; i++) {
            Seq<Cons<?>> listeners = registry.get(keys[i]);
            int added = listeners == null ? 0 : listeners.size - sizes[i];
            if (added != 1) valid = false;
            if (listeners != null) {
                while (listeners.size > sizes[i]) {
                    @SuppressWarnings("unchecked") Cons<Object> handler = (Cons<Object>) listeners.pop();
                    captured.put(keys[i], handler);
                }
            }
        }
        if (!valid) throw new IllegalStateException("Unsupported engine event registration");
        return captured;
    }
}
