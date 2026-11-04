package wh.entities.world.layers;

import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;

import java.util.function.Supplier;

/**
 * Transfers items between directional ports belonging to different layers.
 */
public final class LayerItemLink {
    private LayerItemLink() {
    }

    /**
     * A null context denotes the currently active main world.
     */
    public static boolean transfer(LayerContext sourceLayer, int sourcePos, LayerContext targetLayer, int targetPos) {
        if (sourceLayer == targetLayer) throw new IllegalArgumentException("Ports must be in different layers");
        if ((sourceLayer == null || targetLayer == null) && LayerContext.insideLayer()) {
            throw new IllegalStateException("A main-world endpoint must be resolved from the main world");
        }
        Building source = in(sourceLayer, () -> Vars.world.build(sourcePos));
        if (!(source instanceof LayerItemEndpoint)) return false;
        LayerItemEndpoint from = (LayerItemEndpoint) source;
        Item item = from.peekLayerItem();
        if (item == null) return false;
        return in(targetLayer, () -> {
            Building target = Vars.world.build(targetPos);
            if (!(target instanceof LayerItemEndpoint)) return false;
            LayerItemEndpoint to = (LayerItemEndpoint) target;
            if (!to.acceptsLayerItem(source.team, item)) {
                return false;
            }
            from.removeLayerItem(item);
            to.receiveLayerItem(item);
            return true;
        });
    }

    private static <T> T in(LayerContext layer, Supplier<T> action) {
        return layer == null ? action.get() : layer.call(action);
    }
}
