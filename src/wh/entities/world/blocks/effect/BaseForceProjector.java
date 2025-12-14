package wh.entities.world.blocks.effect;

import arc.func.Cons;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.ForceProjector;

public class BaseForceProjector extends ForceProjector {
    public BaseForceProjector(String name) {
        super(name);
    }
    public class BaseForceProjectorBuilding extends ForceBuild {
        protected final Cons<Unit> unitConsumer = unit -> {
            //if this is positive, repel the unit; if it exceeds the unit radius * 2, it's inside the forcefield
            float overlapDst = (unit.hitSize/2 + realRadius()) - unit.dst(this);

            if (overlapDst > 0) {
                {
                    //stop
                    unit.vel.setZero();
                    //get out
                    unit.move(Tmp.v1.set(unit).sub(this).setLength(overlapDst + 0.01f));
                    if (Mathf.chanceDelta(0.15f * Time.delta)) {
                        Fx.circleColorSpark.at(unit.x, unit.y, team.color);
                    }
                }
            }
        };
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
