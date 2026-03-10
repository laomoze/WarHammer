package wh.entities.world.blocks.production;

import mindustry.type.*;
import mindustry.world.blocks.production.*;
import mindustry.world.meta.*;

public class EnhancedWaterExtractor extends Fracker{
    public static final float extractorBoost = 1.5f;
    public Item extractorItem;
    public static final float weUseTime = 240f;
    public EnhancedWaterExtractor(String name){
        super(name);
    }

    @Override
    public void setStats() {
        stats.timePeriod = weUseTime;
        super.setStats();
        stats.add(Stat.boostEffect, extractorBoost, StatUnit.timesSpeed);
    }

    public class EnhancedWaterExtractorBuild extends SolidPumpBuild {
        public float timer = 0f;

        @Override
        public void updateTile() {
            efficiency *= items.get(extractorItem) > 0 ? extractorBoost : 1;
            super.updateTile();

            if(efficiency > 0){
                timer += power.status * delta();
            }
            if(timer >= weUseTime){
                consume();
                timer -= weUseTime;
            }
        }
    }
}
