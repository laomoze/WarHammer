package wh.entities.world.entities.powerArmorComp;

import arc.func.Boolf;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.type.Weapon;
import wh.entities.world.entities.WHUnitType;

/**
 * 多模式单位定义：连招数据、姿势、公共部件和攻击绘制。
 * <p>
 * 流程：定义部件和姿势 -> 添加有序连招步骤 -> 运行时选择攻击
 * -> 绘制选中的武器和姿势 -> 推进或重置单位阶段。
 */
public class MultiModePowerArmourUnitType extends WHUnitType {

    private static final float shadowTX = -12f, shadowTY = -13f;

    private static final PoseOffset ZERO_OFFSET = new PoseOffset();
    private static final PoseTransform ZERO_TRANSFORM = new PoseTransform();

    /**
     * 有序连招步骤，每一步可以包含多个按权重选择的攻击。
     */
    public final Seq<ComboStep> comboSteps = new Seq<>();
    /**
     * 扁平化攻击列表，供单位运行逻辑和武器适配器查询。
     */
    public final Seq<ComboAction> comboActions = new Seq<>();
    /**
     * 所有连招姿势共用的部件。
     */
    public final Seq<DrawUnitPart> commonParts = new Seq<>();
    private final Seq<DrawUnitPart> poseParts = new Seq<>();
    private final Seq<ComboPose> poses = new Seq<>();
    private boolean posePartsReady;

    public final ComboPose sheathedPose;
    public final ComboPose readyPose;
    private ComboPose drawEndPose;
    public float drawTime = 14f;
    public float sheatheTime = 14f;
    public Interp drawInterp = Interp.pow2Out;
    public Interp sheatheInterp = Interp.pow2In;
    public float readyAnimationTime = 60f;
    public Interp readyAnimationInterp = Interp.linear;
    public float cycleRecoveryTime = 12f;
    public Interp cycleRecoveryInterp = Interp.smooth;
    public float attackStartTimeout = 180f;
    public Boolf<MultiModePowerArmourUnit> comboAttackRequest = MultiModePowerArmourUnit::isShooting;
    public final Seq<ComboKeyframe> drawKeyframes = new Seq<>();

    public MultiModePowerArmourUnitType(String name) {
        super(name);
        constructor = MultiModePowerArmourUnit::new;
        drawBody = false;
        shadowElevation = 0.25f;
        sheathedPose = newPose();
        readyPose = newPose();
    }

    public ComboStep addStep() {
        ComboStep step = new ComboStep(comboSteps.size);
        comboSteps.add(step);
        return step;
    }

    public ComboPose newPose() {
        ComboPose pose = new ComboPose(poses.size, this);
        poses.add(pose);
        return pose;
    }

    public MultiModePowerArmourUnitType drawKeyframe(float progress, ComboPose pose) {
        addKeyframe(drawKeyframes, progress, pose);
        return this;
    }

    public MultiModePowerArmourUnitType drawEndPose(ComboPose pose) {
        drawEndPose = pose;
        return this;
    }

    public ComboPose drawEndPose() {
        return drawEndPose == null ? readyPose : drawEndPose;
    }

    public boolean hasCombo() {
        return !comboSteps.isEmpty() && !comboActions.isEmpty();
    }

    public boolean hasReadyAnimation() {
        return readyAnimationTime > 0f && !readyPose.keyframes.isEmpty();
    }

    public boolean isComboAttackRequested(MultiModePowerArmourUnit unit) {
        return comboAttackRequest != null && comboAttackRequest.get(unit);
    }

    public ComboAction attack(int index) {
        return index >= 0 && index < comboActions.size ? comboActions.get(index) : null;
    }

    public ComboPose pose(int index) {
        return poses.get(validPoseIndex(index));
    }

    public int validPoseIndex(int index) {
        return index >= 0 && index < poses.size ? index : sheathedPose.index;
    }

    public PoseTransform poseTransform(ComboPose pose, int partIndex, PoseTransform out) {
        PoseOffset offset = offset(pose, partIndex);
        return out.set(offset.x, offset.y, offset.rotation);
    }

    public int firstComboWeaponIndex() {
        ComboAction attack = attack(0);
        return attack == null ? -1 : attack.weaponIndex;
    }

    public ComboAction randomAttack(int stepIndex) {
        return randomAttack(null, stepIndex);
    }

    public ComboAction randomAttack(MultiModePowerArmourUnit unit, int stepIndex) {
        if (stepIndex < 0 || stepIndex >= comboSteps.size) return null;

        ComboStep step = comboSteps.get(stepIndex);
        if (step.attacks.isEmpty()) return null;

        float totalWeight = 0f;
        for (ComboAction attack : step.attacks) {
            if (!attack.canUse(unit) || (unit != null && !unit.canUseAttack(attack))) continue;
            totalWeight += Math.max(attack.weight, 0f);
        }

        if (totalWeight <= 0f) return null;

        float value = Mathf.random(totalWeight);
        for (ComboAction attack : step.attacks) {
            if (!attack.canUse(unit) || (unit != null && !unit.canUseAttack(attack))) continue;
            value -= Math.max(attack.weight, 0f);
            if (value <= 0f) return attack;
        }
        return null;
    }

    public void interpolatePose(ComboPose start, ComboPose end, float progress, int partIndex, PoseTransform out) {
        PoseOffset from = offset(start, partIndex);
        PoseOffset to = offset(end, partIndex);
        out.set(Mathf.lerp(from.x, to.x, progress), Mathf.lerp(from.y, to.y, progress),
                Mathf.lerp(from.rotation, to.rotation, progress));
    }

    public void interpolatePose(PoseTransform start, ComboPose end, float progress, int partIndex, PoseTransform out) {
        PoseOffset to = offset(end, partIndex);
        out.set(Mathf.lerp(start.x, to.x, progress), Mathf.lerp(start.y, to.y, progress),
                Mathf.lerp(start.rotation, to.rotation, progress));
    }

    public void interpolateAction(PoseTransform start, Seq<ComboKeyframe> keyframes, ComboPose end, float progress,
                                  int partIndex, boolean relative, Interp interp, PoseTransform out) {
        float originX = relative ? start.x : 0f;
        float originY = relative ? start.y : 0f;
        float originRotation = relative ? start.rotation : 0f;
        float fromX = start.x;
        float fromY = start.y;
        float fromRotation = start.rotation;
        float fromProgress = 0f;
        float clampedProgress = Mathf.clamp(progress);

        for (ComboKeyframe keyframe : keyframes) {
            PoseOffset target = offset(keyframe.pose, partIndex);
            float targetX = originX + target.x;
            float targetY = originY + target.y;
            float targetRotation = originRotation + target.rotation;
            if (clampedProgress <= keyframe.progress) {
                float duration = Math.max(keyframe.progress - fromProgress, 0.001f);
                float segmentProgress = Mathf.clamp((clampedProgress - fromProgress) / duration);
                segmentProgress = interp == null ? segmentProgress : interp.apply(segmentProgress);
                out.set(Mathf.lerp(fromX, targetX, segmentProgress), Mathf.lerp(fromY, targetY, segmentProgress),
                        Mathf.lerp(fromRotation, targetRotation, segmentProgress));
                return;
            }
            fromX = targetX;
            fromY = targetY;
            fromRotation = targetRotation;
            fromProgress = keyframe.progress;
        }

        PoseOffset endOffset = offset(end, partIndex);
        float targetX = end == null ? fromX : originX + endOffset.x;
        float targetY = end == null ? fromY : originY + endOffset.y;
        float targetRotation = end == null ? fromRotation : originRotation + endOffset.rotation;
        float duration = Math.max(1f - fromProgress, 0.001f);
        float segmentProgress = Mathf.clamp((clampedProgress - fromProgress) / duration);
        segmentProgress = interp == null ? segmentProgress : interp.apply(segmentProgress);
        out.set(Mathf.lerp(fromX, targetX, segmentProgress), Mathf.lerp(fromY, targetY, segmentProgress),
                Mathf.lerp(fromRotation, targetRotation, segmentProgress));
    }

    public void interpolateReadyPose(PoseTransform base, float progress, int partIndex, PoseTransform out) {
        float fromX = 0f, fromY = 0f, fromRotation = 0f;
        float fromProgress = 0f;
        float clampedProgress = Mathf.clamp(progress);

        for (ComboKeyframe keyframe : readyPose.keyframes) {
            PoseOffset target = offset(keyframe.pose, partIndex);
            if (clampedProgress <= keyframe.progress) {
                float duration = Math.max(keyframe.progress - fromProgress, 0.001f);
                float segmentProgress = Mathf.clamp((clampedProgress - fromProgress) / duration);
                out.set(base.x + Mathf.lerp(fromX, target.x, segmentProgress),
                        base.y + Mathf.lerp(fromY, target.y, segmentProgress),
                        base.rotation + Mathf.lerp(fromRotation, target.rotation, segmentProgress));
                return;
            }
            fromX = target.x;
            fromY = target.y;
            fromRotation = target.rotation;
            fromProgress = keyframe.progress;
        }

        float duration = Math.max(1f - fromProgress, 0.001f);
        float segmentProgress = Mathf.clamp((clampedProgress - fromProgress) / duration);
        out.set(base.x + Mathf.lerp(fromX, 0f, segmentProgress),
                base.y + Mathf.lerp(fromY, 0f, segmentProgress),
                base.rotation + Mathf.lerp(fromRotation, 0f, segmentProgress));
    }

    private static PoseOffset offset(ComboPose pose, int partIndex) {
        if (pose == null) return ZERO_OFFSET;
        PoseOffset offset = pose.offset(partIndex);
        return offset == null ? ZERO_OFFSET : offset;
    }

    private static void addKeyframe(Seq<ComboKeyframe> keyframes, float progress, ComboPose pose) {
        float time = Mathf.clamp(progress);
        for (ComboKeyframe keyframe : keyframes) {
            if (keyframe.progress == time) {
                keyframe.pose.merge(pose);
                return;
            }
        }

        ComboKeyframe keyframe = new ComboKeyframe(time, pose);
        int index = keyframes.size;
        while (index > 0 && keyframes.get(index - 1).progress > keyframe.progress) {
            index--;
        }
        keyframes.insert(index, keyframe);
    }

    @Override
    public void load() {
        rebuildPoseParts();
        // 这里只加载公共部件，普通武器部件由其他绘制流程处理。
        super.load();
        for (DrawUnitPart part : commonParts) {
            part.turretShading = false;
            part.load(name);
        }
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        rebuildPoseParts();
        super.getRegionsToOutline(out);
        for (DrawUnitPart part : commonParts) {
            part.getOutlines(out);
        }
    }

    @Override
    public void drawWeapons(Unit unit) {
        ensurePoseParts();
        // 绘制当前选中的武器，并应用插值后的姿势。
        super.drawWeapons(unit);
        if (!(unit instanceof MultiModePowerArmourUnit multiUnit)) return;

        int weaponIndex = multiUnit.renderWeaponIndex();
        if (weaponIndex < 0 || weaponIndex >= multiUnit.mounts.length) return;

        WeaponMount mount = multiUnit.mounts[weaponIndex];
        Weapon weapon = mount.weapon;
        for (int index = 0; index < commonParts.size; index++) {
            DrawUnitPart part = commonParts.get(index);
            setPartParams(multiUnit, mount, weapon, index, multiUnit.activeAttack);

            applyColor(unit);
            part.draw(DrawUnitPart.params);
        }
        Draw.alpha(1f);
    }

    private void setPartParams(MultiModePowerArmourUnit unit, WeaponMount mount, Weapon weapon, int partIndex, int modeIndex) {
        float rotation = unit.rotation;
        float reload = weapon.reload <= 0f ? 0f : mount.reload / weapon.reload;
        PoseTransform transform = partIndex < 0 ? ZERO_TRANSFORM : unit.comboPose(partIndex);
        DrawUnitPart.params.set(unit, this, unit.bodyMove, mount.warmup, reload, mount.smoothReload, mount.heat,
                mount.heat, mount.recoil, mount.charge, unit.comboProgress(), unit.x, unit.y, rotation);
        DrawUnitPart.params.positionRotation = rotation;
        DrawUnitPart.params.motionX = transform.x;
        DrawUnitPart.params.motionY = transform.y;
        DrawUnitPart.params.motionRotation = transform.rotation;
        DrawUnitPart.params.sideMultiplier = 1;
        DrawUnitPart.params.modeIndex = modeIndex;
    }

    @Override
    public void drawShadow(Unit unit) {
        float elevation = Math.max(unit.elevation, shadowElevation);
        float shadowSize = hitSize * (1.35f + elevation * 0.2f);
        Drawf.shadow(unit.x + shadowTX * elevation, unit.y + shadowTY * elevation, shadowSize);
    }

    @Override
    public void drawCell(Unit unit) {
    }

    private ComboAction addAttack(ComboStep step, MultiModeWeapon weapon) {
        return addAttack(step, step.attacks.size, weapon);
    }

    private ComboAction addAttack(ComboStep step, int modeIndex, MultiModeWeapon weapon) {
        ComboAction attack = new ComboAction(weapon, comboActions.size, weapons.size, step.index, modeIndex);
        weapon.attackIndex = attack.index;
        comboActions.add(attack);
        step.attacks.add(attack);
        weapons.add(weapon);
        return attack;
    }

    public int posePartCount() {
        ensurePoseParts();
        return poseParts.size;
    }

    private int poseIndex(DrawUnitPart part) {
        if (part == null) return -1;
        ensurePoseParts();
        return part.comboIndex;
    }

    private void ensurePoseParts() {
        if (!posePartsReady) rebuildPoseParts();
    }

    private void rebuildPoseParts() {
        poseParts.clear();
        for (int index = 0; index < commonParts.size; index++) {
            registerPosePart(commonParts.get(index));
        }
        for (int index = 0; index < commonParts.size; index++) {
            registerPoseChildren(commonParts.get(index));
        }
        posePartsReady = true;
    }

    private void registerPosePart(DrawUnitPart part) {
        if (part == null || poseParts.contains(part)) return;

        part.comboIndex = poseParts.size;
        poseParts.add(part);
    }

    private void registerPoseChildren(DrawUnitPart part) {
        if (!(part instanceof UnitRegionPart regionPart)) return;

        for (DrawUnitPart child : regionPart.children) {
            registerPosePart(child);
            registerPoseChildren(child);
        }
    }

    public static class PoseTransform {
        public float x, y, rotation;

        public PoseTransform set(float x, float y, float rotation) {
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            return this;
        }

        public PoseTransform cpy() {
            return new PoseTransform().set(x, y, rotation);
        }
    }

    private static class PoseOffset {
        float x, y, rotation;

        PoseOffset set(float x, float y, float rotation) {
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            return this;
        }
    }

    public static class ComboPose {
        public final int index;
        private final MultiModePowerArmourUnitType owner;
        private final Seq<PoseOffset> offsets = new Seq<>();
        public final Seq<ComboKeyframe> keyframes = new Seq<>();

        private ComboPose(int index, MultiModePowerArmourUnitType owner) {
            this.index = index;
            this.owner = owner;
        }

        public ComboPose part(int partIndex, float x, float y, float rotation) {
            if (partIndex < 0) return this;
            while (offsets.size <= partIndex) {
                offsets.add((PoseOffset) null);
            }
            PoseOffset offset = offsets.get(partIndex);
            if (offset == null) {
                offset = new PoseOffset();
                offsets.set(partIndex, offset);
            }
            offset.set(x, y, rotation);
            return this;
        }

        public ComboPose parts(float x, float y, float rotation, int... partIndices) {
            for (int partIndex : partIndices) {
                part(partIndex, x, y, rotation);
            }
            return this;
        }

        public ComboPose part(DrawUnitPart part, float x, float y, float rotation) {
            return part(owner.poseIndex(part), x, y, rotation);
        }

        public ComboPose keyframe(float progress, ComboPose pose) {
            if (pose != null) addKeyframe(keyframes, progress, pose);
            return this;
        }

        public ComboPose merge(ComboPose other) {
            if (other == null) return this;

            for (int partIndex = 0; partIndex < other.offsets.size; partIndex++) {
                PoseOffset offset = other.offsets.get(partIndex);
                if (offset != null) part(partIndex, offset.x, offset.y, offset.rotation);
            }
            return this;
        }

        private PoseOffset offset(int partIndex) {
            return partIndex >= 0 && partIndex < offsets.size ? offsets.get(partIndex) : null;
        }
    }

    public class ComboStep {
        public final int index;
        public final Seq<ComboAction> attacks = new Seq<>();

        private ComboStep(int index) {
            this.index = index;
        }

        public ComboAction addAttack(MultiModeWeapon weapon) {
            return MultiModePowerArmourUnitType.this.addAttack(this, weapon);
        }

        public ComboAction addAttack(int modeIndex, MultiModeWeapon weapon) {
            return MultiModePowerArmourUnitType.this.addAttack(this, modeIndex, weapon);
        }
    }

    public static class ComboAction {
        public final MultiModeWeapon weapon;
        public final int index;
        public final int weaponIndex;
        public final int stepIndex;
        public final int modeIndex;
        public float weight = 1f;
        public float actionTime = 20f;
        public float shootProgress = -1f;
        public Interp actionInterp = Interp.smooth;
        public ComboPose endPose;
        public final Seq<ComboKeyframe> keyframes = new Seq<>();
        public Boolf<MultiModePowerArmourUnit> condition = unit -> true;
        public boolean relative;

        private ComboAction(MultiModeWeapon weapon, int index, int weaponIndex, int stepIndex, int modeIndex) {
            this.weapon = weapon;
            this.index = index;
            this.weaponIndex = weaponIndex;
            this.stepIndex = stepIndex;
            this.modeIndex = modeIndex;
        }

        public ComboAction weight(float weight) {
            this.weight = weight;
            return this;
        }

        public ComboAction actionTime(float actionTime) {
            this.actionTime = actionTime;
            return this;
        }

        public ComboAction shootProgress(float shootProgress) {
            this.shootProgress = Mathf.clamp(shootProgress);
            return this;
        }

        public ComboAction actionInterp(Interp actionInterp) {
            this.actionInterp = actionInterp == null ? Interp.linear : actionInterp;
            return this;
        }

        public ComboAction endPose(ComboPose endPose) {
            if (this.endPose == null) this.endPose = endPose;
            else this.endPose.merge(endPose);
            return this;
        }

        public ComboAction keyframe(float progress, ComboPose pose) {
            if (pose != null) addKeyframe(keyframes, progress, pose);
            return this;
        }

        public ComboAction relative() {
            relative = true;
            return this;
        }

        public ComboAction condition(Boolf<MultiModePowerArmourUnit> condition) {
            this.condition = condition == null ? unit -> true : condition;
            return this;
        }

        public boolean canUse(MultiModePowerArmourUnit unit) {
            return unit == null || condition == null || condition.get(unit);
        }
    }

    public static class ComboKeyframe {
        public final float progress;
        public final ComboPose pose;

        private ComboKeyframe(float progress, ComboPose pose) {
            this.progress = progress;
            this.pose = pose;
        }
    }

    public static class MultiModeWeapon extends Weapon {
        protected int attackIndex = -1;
        public boolean requireTarget = false;

        public MultiModeWeapon() {
            super(new String());
        }

        public MultiModeWeapon(String name) {
            super(name);
        }

        public boolean ensureTarget(Unit unit, WeaponMount mount) {
            if (bullet == null) return false;

            float mountX = unit.x + Angles.trnsx(unit.rotation - 90f, x, y);
            float mountY = unit.y + Angles.trnsy(unit.rotation - 90f, x, y);
            if (mount.target != null && !checkTarget(unit, mount.target, mountX, mountY, bullet.range)) {
                return true;
            }

            mount.target = findTarget(unit, mountX, mountY, bullet.range, bullet.collidesAir, bullet.collidesGround);
            return mount.target != null && !checkTarget(unit, mount.target, mountX, mountY, bullet.range);
        }

        @Override
        public void update(Unit unit, WeaponMount mount) {
            if (unit instanceof MultiModePowerArmourUnit multiUnit && attackIndex >= 0
                    && multiUnit.canRunWeapon(attackIndex)) {
                mount.reload = 0f;
                if (requireTarget) mount.shoot = mount.target != null;
            } else if (isBlocked(unit)) {
                updateWithoutShooting(unit, mount);
                return;
            }
            super.update(unit, mount);
        }

        private boolean isBlocked(Unit unit) {
            return unit instanceof MultiModePowerArmourUnit multiUnit
                    && attackIndex >= 0 && !multiUnit.canRunWeapon(attackIndex);
        }

        private void updateWithoutShooting(Unit unit, WeaponMount mount) {
            mount.shoot = false;
            super.update(unit, mount);
            mount.shoot = false;
        }

        @Override
        public void draw(Unit unit, WeaponMount mount) {
        }

        @Override
        protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
            if (unit instanceof MultiModePowerArmourUnit multiUnit && attackIndex >= 0) {
             /*   if(requireTarget){
                    Log.info("[MPsy][AttackDebug] FIRE unit=@ attack=@ weapon=@ target=@", unit.id, attackIndex, name, mount.target == null ? "null" : mount.target.getClass().getSimpleName());
                }*/
                multiUnit.onModeShot(attackIndex);
            }
            super.shoot(unit, mount, shootX, shootY, rotation);
        }
    }
}
