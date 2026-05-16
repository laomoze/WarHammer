package wh.entities.world.entities;

import arc.Core;
import arc.graphics.g2d.TextureRegion;

public class RevengeUnitType extends SuperHeavyUnitType {
    public TextureRegion armorRegion;

    public RevengeUnitType(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        armorRegion = Core.atlas == null ? null : Core.atlas.find(this.name + "-energyArmor");
    }
}
