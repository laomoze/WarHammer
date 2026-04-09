package wh.gen;

import arc.math.geom.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import wh.entities.world.entities.*;

public class AirRaiderAI extends AssemblerAI{
    public enum Mode{bomb, strafe, missile}

    private static final float ZERO_EPSILON = 0.001f;
    private static final float BOMB_ARRIVE_RANGE = 30f;
    private static final float STRAFE_START_ARRIVE_RANGE = 50f;
    private static final float STRAFE_END_ARRIVE_RANGE = 50f;
    private static final float MISSILE_ARRIVE_RANGE = 35f;
    private static final float EXTEND_ARRIVE_RANGE = 8f;
    private static final float AI_SYNC_EPSILON = 0.1f;

    public Mode mode;
    public final Vec2 targetPos = new Vec2();
    public final Vec2 startPos = new Vec2();
    public final Vec2 endPos = new Vec2();
    public final Vec2 extendPos = new Vec2();

    public boolean isStrafing = false;
    public boolean hasReachedEnd = false;

    protected float shootDuration = 0f;
    protected float maxShootDuration = 20f;

    protected float deathTimer = 0f;
    protected float fadeInTime = 90f;
    protected float deathDelay = 150f;

    public AirRaiderAI(Mode mode){
        super();
        this.mode = mode;
    }

    @Override
    public void init(){
        super.init();
        syncFadeStateToUnitType();
    }

    public void setBombTarget(Vec2 position, Vec2 run){
        if(isRetreating()) return;
        resetRuntimeState();
        mode = Mode.bomb;
        targetPos.set(position);
        extendPos.set(run);
        startPos.set(position);
        endPos.set(position);
    }

    public void setStrafingPath(Vec2 start, Vec2 end, Vec2 run){
        if(isRetreating()) return;
        resetRuntimeState();
        mode = Mode.strafe;
        startPos.set(start);
        endPos.set(end);
        targetPos.set(end);
        extendPos.set(run);
    }

    public void setMissileTarget(Vec2 target, Vec2 run){
        if(isRetreating()) return;
        resetRuntimeState();
        mode = Mode.missile;
        targetPos.set(target);
        startPos.set(target);
        endPos.set(target);
        extendPos.set(run);
    }

    @Override
    public void afterRead(Unit unit){
        super.afterRead(unit);
        if(unit instanceof AirRaiderUnitType u){
            hasReachedEnd = u.aiHasReachedEnd;
            isStrafing = u.aiIsStrafing;
            endPos.set(u.aiEndPos);
            startPos.set(u.aiStartPos);
            targetPos.set(u.aiTargetPos);
            extendPos.set(u.aiExtendPos);
        }
        syncFadeStateToUnitType();
    }

    /** 新指令下发时重置临时状态，避免上一个任务残留影响当前模式。 */
    private void resetRuntimeState(){
        hasReachedEnd = false;
        isStrafing = false;
        shootDuration = 0f;
        deathTimer = 0f;
    }

    /** AI 内部死亡/渐隐配置同步到单位实体，保证表现一致。 */
    private void syncFadeStateToUnitType(){
        if(unit instanceof AirRaiderUnitType u){
            u.fadeOutTime = deathDelay;
            u.fadeInTime = fadeInTime;
            u.end = hasReachedEnd;
        }
    }

    private boolean isRetreating(){
        if(hasReachedEnd) return true;
        return unit instanceof AirRaiderUnitType u && u.end;
    }

    private boolean moveTo(Vec2 target, float arriveRange){
        if(unit.within(target, arriveRange)){
            return true;
        }

        vec.set(target).sub(unit);
        if(vec.isZero(ZERO_EPSILON)){
            return true;
        }

        vec.setLength(unit.speed());
        unit.moveAt(vec);
        unit.lookAt(target);
        return false;
    }

    private void markReachedEnd(){
        hasReachedEnd = true;
        if(unit instanceof AirRaiderUnitType u){
            u.end = true;
        }
    }

    private void moveToExtendAndDie(){
        moveTo(extendPos, EXTEND_ARRIVE_RANGE);
        deathTimer += Time.delta;
        if(deathTimer >= deathDelay){
            unit.kill();
            deathTimer = 0f;
        }
    }

    private void syncToUnitType(){
        // 通过 Unit 字段做 AI 状态镜像，保证联机/读档恢复稳定。
        if(unit instanceof AirRaiderUnitType u){
            u.aiHasReachedEnd = hasReachedEnd;
            u.aiIsStrafing = isStrafing;
            if(!u.aiEndPos.epsilonEquals(endPos, AI_SYNC_EPSILON)) u.aiEndPos.set(endPos);
            if(!u.aiStartPos.epsilonEquals(startPos, AI_SYNC_EPSILON)) u.aiStartPos.set(startPos);
            if(!u.aiTargetPos.epsilonEquals(targetPos, AI_SYNC_EPSILON)) u.aiTargetPos.set(targetPos);
            if(!u.aiExtendPos.epsilonEquals(extendPos, AI_SYNC_EPSILON)) u.aiExtendPos.set(extendPos);
        }
    }

    private void updateBombMovement(){
        if(!hasReachedEnd){
            if(moveTo(targetPos, BOMB_ARRIVE_RANGE)){
                markReachedEnd();
            }
        }else{
            moveToExtendAndDie();
        }
    }

    private void updateStrafeMovement(){
        if(!isStrafing){
            if(moveTo(startPos, STRAFE_START_ARRIVE_RANGE)){
                isStrafing = true;
                hasReachedEnd = false;
                deathTimer = 0f;
            }
        }else if(!hasReachedEnd){
            if(moveTo(endPos, STRAFE_END_ARRIVE_RANGE)){
                markReachedEnd();
            }
        }else{
            moveToExtendAndDie();
        }
    }

    private void updateMissileMovement(){
        if(!hasReachedEnd){
            moveTo(targetPos, MISSILE_ARRIVE_RANGE);
        }else{
            moveToExtendAndDie();
        }
    }

    private void aimForwardByVelocity(WeaponMount mount, float range){
        if(unit.vel.isZero(ZERO_EPSILON)){
            Tmp.v1.trns(unit.rotation, range);
        }else{
            Tmp.v1.set(unit.vel).nor().scl(range);
        }

        mount.aimX = unit.x + Tmp.v1.x;
        mount.aimY = unit.y + Tmp.v1.y;
    }

    private void updateStrafeWeapons(){
        for(var mount : unit.mounts){
            Weapon weapon = mount.weapon;
            mount.shoot = isStrafing && !hasReachedEnd;
            aimForwardByVelocity(mount, weapon.range());
        }
    }

    private void aimAt(WeaponMount mount, Vec2 target){
        mount.aimX = target.x;
        mount.aimY = target.y;
    }

    private void updateBombWeapons(){
        for(var mount : unit.mounts){
            mount.shoot = hasReachedEnd;
            aimAt(mount, targetPos);
        }
    }

    private void updateMissileWeapons(){
        boolean anyShooting = false;

        for(var mount : unit.mounts){
            Weapon weapon = mount.weapon;

            Vec2 aim = weapon.bullet.speed > 0f && weapon.predictTarget ?
            Predict.intercept(unit, targetPos, weapon.bullet.speed) : targetPos;

            if(aim != null){
                aimAt(mount, aim);
            }else{
                aimAt(mount, targetPos);
            }

            mount.shoot = !hasReachedEnd && unit.within(targetPos, weapon.range());
            anyShooting |= mount.shoot;
        }

        shootDuration = anyShooting ? shootDuration + Time.delta : 0f;
        if(shootDuration >= maxShootDuration){
            markReachedEnd();
            shootDuration = 0f;
        }
    }

    @Override
    public void updateMovement(){
        if(mode == null){
            syncFadeStateToUnitType();
            syncToUnitType();
            return;
        }

        switch(mode){
            case bomb -> updateBombMovement();
            case strafe -> updateStrafeMovement();
            case missile -> updateMissileMovement();
        }
        syncFadeStateToUnitType();
        syncToUnitType();
    }

    @Override
    public void updateWeapons(){
        if(mode == null){
            super.updateWeapons();
            return;
        }

        switch(mode){
            case strafe -> updateStrafeWeapons();
            case bomb -> updateBombWeapons();
            case missile -> updateMissileWeapons();
        }

        // 导弹模式可能在武器阶段切换到离场，立即同步一次状态。
        syncFadeStateToUnitType();
        syncToUnitType();
    }
}
