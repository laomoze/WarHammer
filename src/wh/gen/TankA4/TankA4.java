package wh.gen.TankA4;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.gen.Player;
import mindustry.gen.TankUnit;
import mindustry.type.UnitType;
import wh.entities.world.entities.TankA4UnitType;
import wh.gen.EntityRegister;

public class TankA4 extends TankUnit {
    private static final float progressEpsilon = 0.001f;

    private @Nullable TankA4UnitType tankA7Type;
    private float baseHitSize = -1f;
    private float baseArmor = -1f;
    private transient boolean deploymentFxInitialized;
    private transient boolean lastDeployedState;
    private transient boolean deployCommandHandled;

    public boolean deployRequested;
    public float deployProgress;
    public float deployHoldRemaining;
    public float deployNoTargetTime;
    public float deployRedeployCooldown;

    public static TankA4 create() {
        return new TankA4();
    }

    @Override
    public int classId() {
        return EntityRegister.getId(TankA4.class);
    }

    @Override
    public void setType(UnitType type) {
        super.setType(type);
        tankA7Type = type instanceof TankA4UnitType t ? t : null;
        if (baseHitSize < 0f) baseHitSize = hitSize;
        if (baseArmor < 0f) baseArmor = armor;
    }

    public @Nullable TankA4UnitType tankType() {
        return tankA7Type;
    }

    public void requestDeployment(boolean deploy) {
        TankA4UnitType type = tankType();

        if (deploy) {
            if (!deployRequested && type != null) {
                deployHoldRemaining = type.deployHoldTime;
            }
            deployRequested = true;
            return;
        }

        if (deployHoldRemaining > progressEpsilon) {
            return;
        }

        deployRequested = deploy;
    }

    public void toggleDeployment() {
        deployRequested = !deployRequested;
    }

    public boolean isDeploymentRequested() {
        return deployRequested;
    }

    public boolean isDeployCommandHandled() {
        return deployCommandHandled;
    }

    public void setDeployCommandHandled(boolean handled) {
        deployCommandHandled = handled;
    }

    public boolean isDeployed() {
        return deployProgress >= 1f - progressEpsilon;
    }

    public boolean isInDeploymentTransition() {
        return deployProgress > progressEpsilon && deployProgress < 1f - progressEpsilon;
    }

    public float deploymentProgress() {
        return deployProgress;
    }

    public float deployNoTargetTime() {
        return deployNoTargetTime;
    }

    public void deployNoTargetTime(float time) {
        deployNoTargetTime = time;
    }

    public float deployRedeployCooldown() {
        return deployRedeployCooldown;
    }

    public void deployRedeployCooldown(float time) {
        deployRedeployCooldown = time;
    }

    public float deploymentMoveMultiplier() {
        TankA4UnitType type = tankType();
        float deployedMoveMultiplier = type == null ? 0f : type.deployedMoveMultiplier;
        return Mathf.lerp(1f, deployedMoveMultiplier, deployProgress);
    }

    @Override
    public boolean canShoot() {
        return super.canShoot() && !isInDeploymentTransition();
    }

    @Override
    public void update() {
        updateDeploymentProgress();
        updateDeploymentEffects();
        super.update();

        if (deployProgress > progressEpsilon) {
            float damping = Mathf.lerp(1f, 0.85f, deployProgress);
            vel.scl(damping);
        }

        deployHoldRemaining = Math.max(0f, deployHoldRemaining - Time.delta);
    }

    private void updateDeploymentProgress() {
        TankA4UnitType type = tankType();
        if (type == null) {
            deployProgress = 0f;
            deployRequested = false;
            if (baseHitSize >= 0f) hitSize = baseHitSize;
            if (baseArmor >= 0f) armor = baseArmor;
            return;
        }

        float target = deployRequested ? 1f : 0f;
        float duration = deployRequested ? type.deployTime : type.undeployTime;
        float rate = duration <= progressEpsilon ? 1f : 1f / duration;
        deployProgress = Mathf.approachDelta(deployProgress, target, rate);

        if (Mathf.equal(deployProgress, target, progressEpsilon)) {
            deployProgress = target;
        }

        if (baseHitSize < 0f) baseHitSize = hitSize;
        if (baseArmor < 0f) baseArmor = armor;
        hitSize = Mathf.lerp(baseHitSize, baseHitSize * type.deployHitSizeMultiplier, deployProgress);
        armor = Mathf.lerp(baseArmor, baseArmor * type.deployArmorMultiplier, deployProgress);

        if (deployProgress > 0.1f) this.apply(StatusEffects.unmoving, 60);
    }

    private void updateDeploymentEffects() {
        TankA4UnitType type = tankType();
        if (type == null) return;

        boolean deployed = isDeployed();
        if (!deploymentFxInitialized) {
            deploymentFxInitialized = true;
            lastDeployedState = deployed;
            return;
        }

        if (!Vars.net.client()) {
            if (deployProgress >= 0.25f && deployProgress <= 0.75f && type.deployEffect != null && Mathf.chanceDelta(0.08f)) {
                type.deployEffect.at(x, y, rotation, team.color);
            } else if (deployProgress > progressEpsilon && deployProgress < 0.25f && type.undeployEffect != null && Mathf.chanceDelta(0.05f)) {
                type.undeployEffect.at(x, y, rotation, team.color);
            }
        }

        if (!lastDeployedState && deployed) {
            if (!Vars.net.client() && type.deployFinishEffect != null) {
                type.deployFinishEffect.at(x, y, hitSize, team.color);
            }
            lastDeployedState = true;
        } else if (lastDeployedState && !deployed) {
            lastDeployedState = false;
        }
    }

    @Override
    public void moveAt(Vec2 vector, float acceleration) {
        float moveMul = deploymentMoveMultiplier();
        if (moveMul <= progressEpsilon) return;
        super.moveAt(Tmp.v1.set(vector).scl(moveMul), acceleration);
    }

    @Override
    public void approach(Vec2 vector) {
        float moveMul = deploymentMoveMultiplier();
        if (moveMul <= progressEpsilon) return;
        super.approach(Tmp.v1.set(vector).scl(moveMul));
    }

    @Override
    public void lookAt(float angle) {
        rotation = Angles.moveToward(rotation, angle, type.rotateSpeed * Time.delta * (1 - deployProgress) * speedMultiplier());
    }

    @Override
    public void draw() {
        super.draw();
        TankA4UnitType type = tankType();
        if (type != null && !Vars.headless) {
            type.drawDeployParts(this);
        }
    }

    @Override
    public float mass() {
        TankA4UnitType type = tankType();
        float baseMass = super.mass();
        if (type == null) return baseMass;
        return baseMass * Mathf.lerp(1f, type.deployMassMultiplier, deployProgress);
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        write.bool(deployRequested);
        write.f(deployProgress);
        write.f(deployHoldRemaining);
        write.f(deployNoTargetTime);
        write.f(deployRedeployCooldown);
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        deployRequested = read.bool();
        deployProgress = read.f();
        deployHoldRemaining = read.f();
        deployNoTargetTime = read.f();
        deployRedeployCooldown = read.f();
    }

    @Override
    public void afterRead() {
        super.afterRead();

        TankA4UnitType type = tankType();
        if (type != null && !(controller() instanceof Player)) {
            controller(type.createController(this));
        }

        deploymentFxInitialized = false;
        lastDeployedState = isDeployed();
        deployCommandHandled = false;
    }
}
