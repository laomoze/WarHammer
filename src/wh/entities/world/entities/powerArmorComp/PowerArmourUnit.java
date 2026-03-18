package wh.entities.world.entities.powerArmorComp;

import arc.math.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.gen.*;

public class PowerArmourUnit extends MechUnit{

    public float bodyMove = 0f;
    public Trail BladeTrail;
    // 每个武器挂点对应一份动画状态（按 mounts 下标一一对应）
    private WeaponAnimState[] weaponAnimStates = new WeaponAnimState[0];

    public @Nullable WeaponAnimState weaponAnimState(int index){
        return index >= 0 && index < weaponAnimStates.length ? weaponAnimStates[index] : null;
    }

    @Override
    public int classId(){
        return EntityRegister.getId(PowerArmourUnit.class);
    }

    @Override
    public void update(){
        super.update();

        // 先刷新所有挂点的动画状态，供后续 bodyMove/绘制读取
        updateWeaponAnimStates();

        if(mounts.length == 0){
            bodyMove = Mathf.lerpDelta(bodyMove, 0f, 0.05f);
            return;
        }

        boolean bodyMoveReady = isBodyMoveReady();

        if(vel().len2() > 0.01f && bodyMoveReady){
            bodyMove = Mathf.lerpDelta(bodyMove, 1f, 0.03f);
        }else{
            bodyMove = Mathf.lerpDelta(bodyMove, 0f, 0.05f);
        }
    }

    private int bodyMoveLockWeaponIndex(){
        // >=0: 仅检查指定挂点；-1: 任意挂点触发近战动作都锁身
        if(type instanceof PowerArmourUnitType powerType){
            return powerType.bodyMoveLockWeaponIndex;
        }
        return 0;
    }

    private boolean isBodyMoveReady(){
        int lockIndex = bodyMoveLockWeaponIndex();

        if(lockIndex >= 0){
            return !isMountActionBlocking(lockIndex);
        }

        for(int i = 0; i < mounts.length; i++){
            if(isMountActionBlocking(i)) return false;
        }
        return true;
    }

    private boolean isMountActionBlocking(int mountIndex){
        if(mountIndex < 0 || mountIndex >= mounts.length) return false;

        WeaponMount mount = mounts[mountIndex];
        PowerArmourWeaponData data = PowerArmourWeaponData.get(mount.weapon);
        WeaponAnimState state = weaponAnimState(mountIndex);
        // 仅“近战型”武器在动作前冲阶段才会阻断 bodyMove
        return data != null && data.melee && state != null &&
        state.shouldAction && mount.warmup > 0.01f && mount.recoil > 0.01f;
    }

    private void updateWeaponAnimStates(){
        // 动态对齐数组长度：武器数量变化时保留旧状态并补齐新槽位
        if(weaponAnimStates.length != mounts.length){
            WeaponAnimState[] next = new WeaponAnimState[mounts.length];
            for(int i = 0; i < next.length; i++){
                next[i] = i < weaponAnimStates.length && weaponAnimStates[i] != null ? weaponAnimStates[i] : new WeaponAnimState();
            }
            weaponAnimStates = next;
        }

        for(int i = 0; i < mounts.length; i++){
            WeaponMount mount = mounts[i];
            WeaponAnimState state = weaponAnimStates[i];
            PowerArmourWeaponData data = PowerArmourWeaponData.get(mount.weapon);
            if(data == null){
                // 无扩展配置：只更新基线值，避免下一帧误判“刚开火”
                state.lastReload = mount.reload;
                state.lastHeat = mount.heat;
                continue;
            }

            // reload 从小跳大通常表示“新一轮射击已触发”
            if(mount.reload > state.lastReload + 0.001f && mount.heat >= state.lastHeat){
                state.shouldAction = true;
            }

            state.smoothHeat = Mathf.lerpDelta(state.smoothHeat, mount.heat, data.smoothHeatSpeed);

            // 未显式配置 actionTime 时，回退到 firstShotDelay
            float actionTime = data.actionTime > 0f ? data.actionTime : mount.weapon.shoot.firstShotDelay;
            if(actionTime <= 0f) actionTime = 1f;

            float target = state.shouldAction ? 1f : 0f;
            state.actionProgress = Mathf.approachDelta(state.actionProgress, target, reloadMultiplier / actionTime);
            // 备用的慢速进度通道（目前主要保留兼容）
            state.actionProgress2 = Mathf.approachDelta(state.actionProgress2, target, state.shouldAction ? 0.2f : 0.03f);

            // 前冲/回收分别可配置不同插值曲线
            Interp interp = state.shouldAction ? data.actionInInterp : data.actionOutInterp;
            state.actionInterpProgress = interp.apply(state.actionProgress);

            // 前冲到顶后自动切回回收阶段
            if(state.actionProgress >= 1f){
                state.shouldAction = false;
            }

            state.lastReload = mount.reload;
            state.lastHeat = mount.heat;
        }
    }

    public static class WeaponAnimState{
        // 平滑热量（给部件动画使用）
        public float smoothHeat;
        // 是否处于动作前冲阶段
        public boolean shouldAction;
        // 主动作进度：0->1 前冲，1->0 回收
        public float actionProgress;
        // 备用进度通道（保留兼容）
        public float actionProgress2;
        // 插值后进度（用于更自然的动作曲线）
        public float actionInterpProgress;

        // 上一帧基线值，用于检测“是否刚开火”
        private float lastReload;
        private float lastHeat;
    }
}
