package wh.entities.world.blocks.effect;

import arc.func.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.*;

public class BaseForceProjector extends ForceProjector {
    public BaseForceProjector(String name) {
        super(name);
    }
    public class BaseForceProjectorBuilding extends ForceBuild {
        // continuous repel tuning: larger values push units out faster
        protected float repelBase = 0.02f;
        protected float repelScale = 0.08f;
        protected float repelDepthCap = 6f;

        protected final Cons<Unit> unitConsumer = unit -> {
            //if this is positive, repel the unit; if it exceeds the unit radius * 2, it's inside the forcefield
            float overlapDst = (unit.hitSize / 2f + realRadius()) - unit.dst(this);

            if(overlapDst > 0f){
                // outward direction from projector center to unit
                Tmp.v1.set(unit).sub(this);
                if(Tmp.v1.len2() < 0.0001f){
                    Tmp.v1.trns(Mathf.random(360f), 1f);
                }

                float depthFactor = Mathf.clamp(overlapDst / (unit.hitSize / 2f + 4f), 0f, 1f);
                float push = Math.min(overlapDst, repelDepthCap) * (repelBase + depthFactor * repelScale) * Time.delta;
                unit.vel.add(Tmp.v1.nor().scl(push));

                if(Mathf.chanceDelta(0.15f)){
                    Fx.circleColorSpark.at(unit.x, unit.y, team.color);
                }
            }
        };

        @Override
        public void updateTile() {
            super.updateTile();
            deflectUnits();
        }

        public void deflectUnits(){
            float radius = realRadius();
            if(radius > 0 && !broken){
                paramEntity = this;
                Units.nearbyEnemies(team, x, y, radius + 10f, unitConsumer);
            }
        }
    }

}
