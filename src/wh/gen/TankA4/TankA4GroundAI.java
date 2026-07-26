package wh.gen.TankA4;

import arc.util.Time;
import mindustry.ai.types.GroundAI;
import mindustry.entities.Units;
import mindustry.gen.Healthc;
import mindustry.gen.Teamc;
import mindustry.gen.TimedKillc;
import mindustry.world.blocks.defense.turrets.Turret;
import wh.entities.WHUnitSorts;
import wh.entities.world.entities.TankA4UnitType;

public class TankA4GroundAI extends GroundAI {
    protected static final float undeployDelay = 60f * 20;
    protected static final float redeployDelay = 60f * 25f;

    @Override
    public void updateUnit() {
        if (unit instanceof TankA4 tank) {
            tank.deployRedeployCooldown(Math.max(0f, tank.deployRedeployCooldown() - Time.delta));
        }

        super.updateUnit();
        updateDeploymentIntent();
    }

    protected void updateDeploymentIntent() {
        if (!(unit instanceof TankA4 tank)) return;
        TankA4UnitType type = tank.tankType();
        if (type == null) return;
        if (tank.deploymentProgress() >= 0.9f && target != null) {
            unit.lookAt(target);
        }

        boolean hasDeployTarget = target instanceof Healthc h && h.maxHealth() > 10000;

        if (hasDeployTarget) {
            tank.deployNoTargetTime(0f);
            if (tank.deployRedeployCooldown() <= 0f) {
                tank.requestDeployment(true);
            }
            return;
        }

        tank.deployNoTargetTime(tank.deployNoTargetTime() + Time.delta);

        if (tank.isDeploymentRequested() && tank.deployNoTargetTime() < undeployDelay) {
            return;
        }

        if (tank.isDeploymentRequested()) {
            tank.requestDeployment(false);
            tank.deployRedeployCooldown(redeployDelay);
        }
    }

    @Override
    public Teamc target(float x, float y, float range, boolean air, boolean ground) {
        return Units.bestTarget(unit.team, unit.x, unit.y, unit.range() + 10f,
                e -> e.checkTarget(air, ground) && !(e instanceof TimedKillc),
                b -> b instanceof Turret.TurretBuild tu && tu.hasAmmo(),
                WHUnitSorts.slowest
        );
    }
}
