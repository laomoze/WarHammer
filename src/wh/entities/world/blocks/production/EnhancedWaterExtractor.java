package wh.entities.world.blocks.production;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.blocks.production.*;
import mindustry.world.meta.*;

public class EnhancedWaterExtractor extends SolidPump{
    public static final float extractorBoost = 1.5f;
    public static final Item extractorItem = Items.graphite;
    public static final float weUseTime = 120f;
    public EnhancedWaterExtractor(String name){
        super(name);
        consumeItem(extractorItem).boost();
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
