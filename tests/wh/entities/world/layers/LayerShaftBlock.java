package wh.entities.world.layers;

import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.world.Tile;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Surface end of a paired, directional item connection to another layer.
 */
public class LayerShaftBlock extends LayerItemPort {
    private final Supplier<LayerContext> targetLayer;
    private final LayerItemPort targetPort;

    public LayerShaftBlock(String name, boolean exports, Supplier<LayerContext> targetLayer, LayerItemPort targetPort) {
        super(name, exports);
        this.targetLayer = Objects.requireNonNull(targetLayer, "targetLayer");
        this.targetPort = Objects.requireNonNull(targetPort, "targetPort");
        if (targetPort.exports == exports)
            throw new IllegalArgumentException("Shaft endpoints must have opposite directions");
        buildType = LayerShaftBuild::new;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (!super.canPlaceOn(tile, team, rotation)) return false;
        LayerContext layer = targetLayer.get();
        if (layer == null) return false;
        return layer.call(() -> {
            Tile target = Vars.world.tile(tile.x, tile.y);
            return target != null && (target.block() == Blocks.air ||
                    (target.block() == targetPort && target.team() == team));
        });
    }

    public class LayerShaftBuild extends LayerItemPortBuild {
        @Override
        public void updateTile() {
            super.updateTile();
            if (Vars.net.client()) return;
            LayerContext layer = targetLayer.get();
            if (layer == null || !ensureTarget(layer)) return;
            int position = pos();
            if (exports) LayerItemLink.transfer(null, position, layer, position);
            else LayerItemLink.transfer(layer, position, null, position);
        }

        private boolean ensureTarget(LayerContext layer) {
            return layer.call(() -> {
                Tile target = Vars.world.tile(tile.x, tile.y);
                if (target == null) return false;
                if (target.block() == Blocks.air) {
                    Call.setTile(target, targetPort, team, rotation);
                }
                Building linked = target.build;
                if (linked == null || linked.block != targetPort || linked.team != team) return false;
                if (linked.rotation != rotation) {
                    linked.rotation = rotation;
                    linked.updateProximity();
                }
                return true;
            });
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
            if (Vars.world.isGenerating() || Vars.net.client()) return;
            LayerContext layer = targetLayer.get();
            if (layer == null) return;
            layer.run(() -> {
                Tile target = Vars.world.tile(tile.x, tile.y);
                if (target != null && target.block() == targetPort && target.team() == team) {
                    Call.removeTile(target);
                }
            });
        }
    }
}
