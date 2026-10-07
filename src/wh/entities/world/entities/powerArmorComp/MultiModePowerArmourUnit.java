package wh.entities.world.entities.powerArmorComp;

import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Interval;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.Sized;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.MechUnit;
import wh.gen.EntityRegister;

/**
 * 多模式单位的运行时状态
 * <p>
 * 流程：收刀 -> 拔刀 -> 待机 -> 攻击 -> 待机，或收刀中 -> 收刀
 * 服务端选择攻击并同步阶段和姿势，客户端只推进已同步的时间
 */
public class MultiModePowerArmourUnit extends MechUnit {

    public static final byte phaseSheathed = 0;
    public static final byte phaseDrawing = 1;
    public static final byte phaseReady = 2;
    public static final byte phaseAttacking = 3;
    public static final byte phaseSheathing = 4;
    public static final byte phaseRecovering = 5;

    /**
     * 当前攻击和连招步骤。
     */
    public int activeAttack = -1;
    public int activeStep;
    public byte attackPhase = phaseSheathed;
    public float actionProgress;
    public boolean attackFired;
    /**
     * 独立类需要在本地保存的通用绘制状态。
     */
    public float bodyMove;
    public Interval timer1 = new Interval(6);

    /**
     * 所有公共部件的缓存姿势数据。
     */
    private float phaseTime = 1f;
    private float readyWaitTime;
    private float readyAnimationTimer;
    private float[] poseValues = {};
    private boolean poseReady;
    private boolean attackUnavailable;
    private float attackDebugTime;
    private int attackDebugIndex = -1;
    private final MultiModePowerArmourUnitType.PoseTransform pose = new MultiModePowerArmourUnitType.PoseTransform();

    @Override
    public int classId() {
        return EntityRegister.getId(MultiModePowerArmourUnit.class);
    }

    public MultiModePowerArmourUnitType multiType() {
        return type instanceof MultiModePowerArmourUnitType multiType ? multiType : null;
    }

    public boolean canRunWeapon(int attackIndex) {
        MultiModePowerArmourUnitType multiType = multiType();
        MultiModePowerArmourUnitType.ComboAction attack = multiType == null ? null : multiType.attack(attackIndex);
        if (attack == null || !attack.canUse(this) || activeAttack != attackIndex
                || !multiType.isComboAttackRequested(this) || attackFired) return false;

        if (attack.shootProgress < 0f) return attackPhase == phaseReady;
        return attackPhase == phaseAttacking && actionProgress + Time.delta / phaseTime >= attack.shootProgress;
    }

    public boolean canUseAttack(MultiModePowerArmourUnitType.ComboAction attack) {
        if (attack == null || attack.weapon == null) return false;

        WeaponMount mount = null;
        for (WeaponMount candidate : mounts) {
            if (candidate.weapon == attack.weapon) {
                mount = candidate;
                break;
            }
        }
        if (mount == null) return false;
        MultiModePowerArmourUnitType.MultiModeWeapon weapon = (MultiModePowerArmourUnitType.MultiModeWeapon) mount.weapon;
        if (weapon.requireTarget) {
            weapon.ensureTarget(this, mount);
            if (mount.target == null) return false;
        }

        if (mount.target == null) return true;

        float mountX = x + Angles.trnsx(rotation - 90f, attack.weapon.x, attack.weapon.y);
        float mountY = y + Angles.trnsy(rotation - 90f, attack.weapon.x, attack.weapon.y);
        float range = attack.weapon.range() + Math.abs(attack.weapon.shootY);
        if (mount.target instanceof Sized sized) range += sized.hitSize() / 2f;
        boolean inRange = mount.target.within(mountX, mountY, range);
      /*  if (attack.weapon.requireTarget && (Time.time >= attackDebugTime || attackDebugIndex != attack.index)) {
            attackDebugTime = Time.time + 30f;
            attackDebugIndex = attack.index;
            Log.info("[MPsy][AttackDebug] unit=@ candidate=@ weapon=@ target=@ result=@ range=@ phase=@ active=@ step=@", id, attack.index, attack.weapon.name, mount.target.getClass().getSimpleName(), inRange ? "VALID" : "OUT_OF_RANGE", range, attackPhase, activeAttack, activeStep);
        }*/
        return inRange;
    }

    public void onModeShot(int attackIndex) {
        if (Vars.net.client() || attackIndex != activeAttack || attackFired) return;

        MultiModePowerArmourUnitType multiType = multiType();
        MultiModePowerArmourUnitType.ComboAction attack = activeAction(multiType);
        if (attack == null || !multiType.isComboAttackRequested(this)) return;
        if (attack.shootProgress >= 0f) {
            if (attackPhase != phaseAttacking || actionProgress + Time.delta / phaseTime < attack.shootProgress) return;
        } else if (attackPhase != phaseReady) {
            return;
        }

        readyWaitTime = 0f;
        attackUnavailable = false;
        if (attackPhase == phaseReady) startPhase(phaseAttacking, attack.actionTime);
        attackFired = true;
        activeStep = (activeStep + 1) % multiType.comboSteps.size;
    }

    @Override
    public void update() {
        super.update();
        updateBodyMove();
        updateComboState();
    }

    private void updateBodyMove() {
        if (mounts.length > 0 && vel().len2() > 0.01f) {
            bodyMove = Mathf.lerpDelta(bodyMove, 1f, 0.03f);
        } else {
            bodyMove = Mathf.lerpDelta(bodyMove, 0f, 0.05f);
        }
    }

    public void comboPose(int partIndex, MultiModePowerArmourUnitType.PoseTransform out) {
        MultiModePowerArmourUnitType multiType = multiType();
        if (multiType == null) {
            out.set(0f, 0f, 0f);
            return;
        }

        ensurePoseState(multiType);
        int poseIndex = partIndex * 3;
        pose.set(poseValues[poseIndex], poseValues[poseIndex + 1], poseValues[poseIndex + 2]);

        switch (attackPhase) {
            case phaseDrawing -> {
                multiType.interpolateAction(pose, multiType.drawKeyframes, multiType.drawEndPose(),
                        actionProgress, partIndex, false, multiType.drawInterp, out);
                return;
            }
            case phaseAttacking -> {
                MultiModePowerArmourUnitType.ComboAction attack = activeAction(multiType);
                if (attack != null) {
                    multiType.interpolateAction(pose, attack.keyframes, attack.endPose,
                            actionProgress, partIndex, attack.relative, attack.actionInterp, out);
                    return;
                }
            }
            case phaseReady -> {
                if (multiType.hasReadyAnimation()) {
                    multiType.interpolateReadyPose(pose,
                            multiType.readyAnimationInterp.apply(readyAnimationProgress(multiType)), partIndex, out);
                    return;
                }
            }
            case phaseSheathing -> {
                multiType.poseTransform(multiType.sheathedPose, partIndex, pose);
                multiType.interpolateAction(pose, multiType.drawKeyframes, multiType.drawEndPose(),
                        1f - actionProgress, partIndex, false, multiType.sheatheInterp, out);
                return;
            }
            case phaseRecovering -> {
                multiType.interpolatePose(pose, multiType.drawEndPose(),
                        multiType.cycleRecoveryInterp.apply(actionProgress), partIndex, out);
                return;
            }
        }

        out.set(pose.x, pose.y, pose.rotation);
    }

    public void attackPose(int partIndex, float progress, MultiModePowerArmourUnitType.PoseTransform out) {
        MultiModePowerArmourUnitType multiType = multiType();
        MultiModePowerArmourUnitType.ComboAction attack = activeAction(multiType);
        if (multiType == null || attack == null || attackPhase != phaseAttacking) {
            comboPose(partIndex, out);
            return;
        }

        ensurePoseState(multiType);
        int poseIndex = partIndex * 3;
        pose.set(poseValues[poseIndex], poseValues[poseIndex + 1], poseValues[poseIndex + 2]);
        multiType.interpolateAction(pose, attack.keyframes, attack.endPose, progress,
                partIndex, attack.relative, attack.actionInterp, out);
    }

    public MultiModePowerArmourUnitType.PoseTransform comboPose(int partIndex) {
        comboPose(partIndex, pose);
        return pose;
    }

    public float comboProgress() {
        return actionProgress;
    }

    public int renderWeaponIndex() {
        MultiModePowerArmourUnitType multiType = multiType();
        MultiModePowerArmourUnitType.ComboAction attack = activeAction(multiType);
        if (attack != null) {
            for (int index = 0; index < mounts.length; index++) {
                if (mounts[index].weapon == attack.weapon) return index;
            }
            return -1;
        }
        MultiModePowerArmourUnitType.ComboAction first = multiType == null ? null : multiType.attack(0);
        if (first != null) {
            for (int index = 0; index < mounts.length; index++) {
                if (mounts[index].weapon == first.weapon) return index;
            }
        }
        return -1;
    }

    private void updateComboState() {
        MultiModePowerArmourUnitType multiType = multiType();
        if (multiType == null || !multiType.hasCombo()) return;

        ensurePoseState(multiType);
        activeStep = Mathf.mod(activeStep, multiType.comboSteps.size);
        advanceReadyAnimation(multiType);

        if (Vars.net.client()) {
            updateClientComboProgress();
            return;
        }

        boolean attackRequested = multiType.isComboAttackRequested(this);
        if (!attackRequested) attackUnavailable = false;

        switch (attackPhase) {
            case phaseSheathed -> {
                if (attackRequested && !attackUnavailable) beginDrawing(multiType);
            }
            case phaseDrawing -> {
                if (advanceAction()) {
                    setPose(multiType, multiType.drawEndPose());
                    if (attackRequested) armNextAttack(multiType);
                    else beginSheathing(multiType);
                }
            }
            case phaseReady -> {
                if (!attackRequested) {
                    captureCurrentPose(multiType);
                    beginRecovery(multiType);
                } else if (!canRunActiveAttack(multiType)) {
                    armNextAttack(multiType);
                } else if (advanceReadyWait(multiType.attackStartTimeout)) {
                    attackUnavailable = true;
                    beginRecovery(multiType);
                }
            }
            case phaseAttacking -> {
                if (advanceAction()) {
                    MultiModePowerArmourUnitType.ComboAction attack = activeAction(multiType);
                    if (attack != null) commitAttackPose(multiType, attack);

                    if (attackFired && attackRequested && activeStep != 0) armNextAttack(multiType);
                    else beginRecovery(multiType);
                }
            }
            case phaseRecovering -> {
                if (advanceAction()) {
                    setPose(multiType, multiType.drawEndPose());
                    if (attackRequested && !attackUnavailable) armNextAttack(multiType);
                    else beginSheathing(multiType);
                }
            }
            case phaseSheathing -> {
                if (advanceAction()) resetSheathed(multiType);
            }
            default -> resetSheathed(multiType);
        }
    }

    private void updateClientComboProgress() {
        if (isTimedPhase()) {
            actionProgress = Mathf.clamp(actionProgress + Time.delta / phaseTime);
        }
    }

    private void advanceReadyAnimation(MultiModePowerArmourUnitType multiType) {
        if (attackPhase == phaseReady && multiType.hasReadyAnimation()) {
            readyAnimationTimer += Time.delta;
            if (readyAnimationTimer >= multiType.readyAnimationTime) {
                readyAnimationTimer = Mathf.mod(readyAnimationTimer, multiType.readyAnimationTime);
            }
        }
    }

    private float readyAnimationProgress(MultiModePowerArmourUnitType multiType) {
        return Mathf.clamp(readyAnimationTimer / multiType.readyAnimationTime);
    }

    private boolean advanceAction() {
        actionProgress = Mathf.clamp(actionProgress + Time.delta / phaseTime);
        if (actionProgress < 1f) return false;

        actionProgress = 1f;
        return true;
    }

    private boolean advanceReadyWait(float timeout) {
        if (timeout <= 0f) return false;
        readyWaitTime += Time.delta;
        return readyWaitTime >= timeout;
    }

    private void beginDrawing(MultiModePowerArmourUnitType multiType) {
        activeAttack = -1;
        activeStep = 0;
        setPose(multiType, multiType.sheathedPose);
        startPhase(phaseDrawing, multiType.drawTime);
    }

    private void armNextAttack(MultiModePowerArmourUnitType multiType) {
        MultiModePowerArmourUnitType.ComboAction attack = multiType.randomAttack(this, activeStep);
        if (attack == null) {
            beginSheathing(multiType);
            return;
        }

        activeAttack = attack.index;
        /* Log.info("[MPsy][AttackDebug] unit=@ SELECT attack=@ weapon=@ step=@ phase=@", id, attack.index, attack.weapon.name, activeStep, attackPhase);*/
        readyWaitTime = 0f;
        readyAnimationTimer = 0f;
        attackFired = false;
        actionProgress = 0f;
        if (attack.shootProgress >= 0f) {
            startPhase(phaseAttacking, attack.actionTime);
        } else {
            attackPhase = phaseReady;
        }
    }

    private void beginSheathing(MultiModePowerArmourUnitType multiType) {
        activeAttack = -1;
        setPose(multiType, multiType.drawEndPose());
        startPhase(phaseSheathing, multiType.sheatheTime);
    }

    private void beginRecovery(MultiModePowerArmourUnitType multiType) {
        activeAttack = -1;
        startPhase(phaseRecovering, multiType.cycleRecoveryTime);
    }

    private void resetSheathed(MultiModePowerArmourUnitType multiType) {
        activeAttack = -1;
        activeStep = 0;
        setPose(multiType, multiType.sheathedPose);
        actionProgress = 0f;
        attackFired = false;
        phaseTime = 1f;
        attackPhase = phaseSheathed;
    }

    private void ensurePoseState(MultiModePowerArmourUnitType multiType) {
        if (!poseReady || poseValues.length != multiType.posePartCount() * 3) {
            setPose(multiType, multiType.sheathedPose);
        }
    }

    private void setPose(MultiModePowerArmourUnitType multiType, MultiModePowerArmourUnitType.ComboPose comboPose) {
        // 将稀疏姿势数据复制到连续缓存，绘制时避免重复分配内存。
        int partCount = multiType.posePartCount();
        if (poseValues.length != partCount * 3) {
            poseValues = new float[partCount * 3];
        }

        for (int partIndex = 0; partIndex < partCount; partIndex++) {
            multiType.poseTransform(comboPose, partIndex, pose);
            int poseIndex = partIndex * 3;
            poseValues[poseIndex] = pose.x;
            poseValues[poseIndex + 1] = pose.y;
            poseValues[poseIndex + 2] = pose.rotation;
        }
        poseReady = true;
    }

    private void captureCurrentPose(MultiModePowerArmourUnitType multiType) {
        for (int partIndex = 0; partIndex < multiType.posePartCount(); partIndex++) {
            comboPose(partIndex, pose);
            int poseIndex = partIndex * 3;
            poseValues[poseIndex] = pose.x;
            poseValues[poseIndex + 1] = pose.y;
            poseValues[poseIndex + 2] = pose.rotation;
        }
    }

    private void commitAttackPose(MultiModePowerArmourUnitType multiType, MultiModePowerArmourUnitType.ComboAction attack) {
        for (int partIndex = 0; partIndex < multiType.posePartCount(); partIndex++) {
            int poseIndex = partIndex * 3;
            pose.set(poseValues[poseIndex], poseValues[poseIndex + 1], poseValues[poseIndex + 2]);
            multiType.interpolateAction(pose, attack.keyframes, attack.endPose, 1f,
                    partIndex, attack.relative, attack.actionInterp, pose);
            poseValues[poseIndex] = pose.x;
            poseValues[poseIndex + 1] = pose.y;
            poseValues[poseIndex + 2] = pose.rotation;
        }
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);
        write.i(activeAttack);
        write.i(activeStep);
        write.b(attackPhase);
        write.f(actionProgress);
        write.b(attackFired ? 1 : 0);
        write.f(readyAnimationTimer);
        MultiModePowerArmourUnitType multiType = multiType();
        if (multiType != null) ensurePoseState(multiType);
        write.i(poseValues.length);
        for (float value : poseValues) {
            write.f(value);
        }
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);
        activeAttack = read.i();
        activeStep = read.i();
        attackPhase = read.b();
        actionProgress = read.f();
        attackFired = read.bool();
        readyAnimationTimer = read.f();

        MultiModePowerArmourUnitType multiType = multiType();
        int poseValueCount = read.i();
        int expectedValueCount = multiType == null ? 0 : multiType.posePartCount() * 3;
        boolean validPose = multiType != null && poseValueCount == expectedValueCount;
        if (validPose && poseValues.length != poseValueCount) {
            poseValues = new float[poseValueCount];
        }
        for (int index = 0; index < poseValueCount; index++) {
            float value = read.f();
            if (validPose) poseValues[index] = value;
        }

        if (multiType != null) {
            activeStep = multiType.comboSteps.isEmpty() ? 0 : Mathf.mod(activeStep, multiType.comboSteps.size);
            phaseTime = segmentTime(multiType);
            if (!validPose) {
                poseReady = false;
                ensurePoseState(multiType);
            } else {
                poseReady = true;
            }
        }
    }

    private float segmentTime(MultiModePowerArmourUnitType multiType) {
        return switch (attackPhase) {
            case phaseDrawing -> Math.max(multiType.drawTime, 1f);
            case phaseAttacking -> {
                MultiModePowerArmourUnitType.ComboAction attack = activeAction(multiType);
                yield Math.max(attack == null ? 1f : attack.actionTime, 1f);
            }
            case phaseSheathing -> Math.max(multiType.sheatheTime, 1f);
            case phaseRecovering -> Math.max(multiType.cycleRecoveryTime, 1f);
            default -> 1f;
        };
    }

    private MultiModePowerArmourUnitType.ComboAction activeAction(MultiModePowerArmourUnitType multiType) {
        return multiType == null ? null : multiType.attack(activeAttack);
    }

    private boolean canRunActiveAttack(MultiModePowerArmourUnitType multiType) {
        MultiModePowerArmourUnitType.ComboAction attack = activeAction(multiType);
        return attack != null && attack.canUse(this);
    }

    private boolean isTimedPhase() {
        return attackPhase == phaseDrawing || attackPhase == phaseAttacking
                || attackPhase == phaseSheathing || attackPhase == phaseRecovering;
    }

    private void startPhase(byte phase, float duration) {
        actionProgress = 0f;
        phaseTime = Math.max(duration, 1f);
        attackPhase = phase;
    }
}
