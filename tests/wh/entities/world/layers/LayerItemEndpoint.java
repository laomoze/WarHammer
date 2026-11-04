package wh.entities.world.layers;

import mindustry.game.Team;
import mindustry.type.Item;

/**
 * Implemented by buildings that exchange items with a different world layer.
 */
public interface LayerItemEndpoint {
    Item peekLayerItem();

    boolean acceptsLayerItem(Team sourceTeam, Item item);

    void removeLayerItem(Item item);

    void receiveLayerItem(Item item);
}
