package wh.entities.world.layers;

import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;

/**
 * A directional item endpoint. Export ports accept from behind; import ports output ahead.
 */
public class LayerItemPort extends Block {
    public final boolean exports;
    public float transferTime = 1f;

    public LayerItemPort(String name, boolean exports) {
        super(name);
        this.exports = exports;
        update = true;
        rotate = true;
        hasItems = true;
        sync = true;
        itemCapacity = 4;
        group = BlockGroup.transportation;
        unloadable = false;
    }

    @Override
    public boolean outputsItems() {
        return !exports;
    }

    public class LayerItemPortBuild extends Building implements LayerItemEndpoint {
        private float progress;

        @Override
        public boolean acceptItem(Building source, Item item) {
            return exports && source != null && source.team == team && source == back() && items.total() < itemCapacity;
        }

        @Override
        public void updateTile() {
            if (exports || !items.any()) return;
            progress += delta();
            if (progress >= transferTime) {
                Item item = items.first();
                if (item != null && moveForward(item)) {
                    items.remove(item, 1);
                    progress %= Math.max(transferTime, 0.001f);
                } else {
                    progress = Math.max(transferTime, 0f);
                }
            }
        }

        @Override
        public Item peekLayerItem() {
            return exports && items.any() ? items.first() : null;
        }

        @Override
        public boolean acceptsLayerItem(Team sourceTeam, Item item) {
            return !exports && sourceTeam == team && items.total() < itemCapacity;
        }

        @Override
        public void removeLayerItem(Item item) {
            items.remove(item, 1);
        }

        @Override
        public void receiveLayerItem(Item item) {
            items.add(item, 1);
        }
    }
}
