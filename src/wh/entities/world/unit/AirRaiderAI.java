package wh.entities.world.unit;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.type.*;
import wh.entities.world.blocks.defense.*;
import wh.entities.world.blocks.defense.AirRaiderCallBlock.*;

import static arc.graphics.Gl.version;
import static mindustry.Vars.tilesize;

public class AirRaiderAI extends AssemblerAI{
    public enum Mode{bomb, strafe, missile}

    public Mode mode;
    public Vec2 targetPos = new Vec2();
    public Vec2 startPos = new Vec2(), endPos = new Vec2();
    public Vec2 extendPos = new Vec2();

    public boolean isStrafing = false;
    public boolean hasReachedEnd = false;

    protected float shootDuration = 0;
    protected float maxShootDuration = 20;

    protected float deathTimer;
    protected float fadeInTime = 90f;
    protected float deathDelay = 150f;

    @Override
    public void init(){
        super.init();
        if(unit instanceof AirRaiderUnitType u){
            u.fadeOutTime = deathDelay;
            u.fadeInTime = fadeInTime;
            u.end = false;
        }
    }

    public AirRaiderAI(Mode mode){
        super();
        this.mode = mode;
    }

    public void setBombTarget(Vec2 position, Vec2 run){
        mode = Mode.bomb;
        targetPos = position;
        extendPos = run;

    }

    public void setStrafingPath(Vec2 start, Vec2 end, Vec2 run){
        mode = Mode.strafe;
        startPos = start;
        endPos = end;
        extendPos = run;
    }

    public void setMissileTarget(Vec2 target, Vec2 run){
        mode = Mode.missile;
        targetPos = target;
        extendPos = run;
    }

    @Override
    public void afterRead(Unit unit){
        super.afterRead(unit);
        if(unit instanceof AirRaiderUnitType u){
            hasReachedEnd = u.aiHasReachedEnd;
            isStrafing = u.aiIsStrafing;

            endPos = u.aiEndPos;
            startPos = u.aiStartPos;
            targetPos = u.aiTargetPos;
            extendPos = u.aiExtendPos;
        }
    }

    @Override
    public void updateMovement(){
        switch(mode){
            case bomb -> {
                if(targetPos == null) return;

                unit.moveAt(vec.trns(unit.rotation, unit.speed()));

                if(!hasReachedEnd){
                    if(!unit.within(targetPos, 30f)){
                        vec.set(targetPos).sub(unit).setLength(unit.speed());
                        unit.moveAt(vec);
                        unit.lookAt(targetPos);
                    }else{
                        hasReachedEnd = true;
                        if(unit instanceof AirRaiderUnitType u){
                            u.end = true;
                        }
                    }
                }else{
                    vec.set(extendPos.cpy()).sub(unit).setLength(unit.speed());
                    unit.moveAt(vec);
                    unit.lookAt(extendPos.cpy());
                    deathTimer += Time.delta;
                    if(deathTimer >= deathDelay){
                        unit.kill();
                        deathTimer = 0f;
                    }
                }
            }

            case strafe -> {
                if(startPos == null || endPos == null){
                    return;
                }

                unit.moveAt(vec.trns(unit.rotation, unit.speed()));

                if(!isStrafing){
                    if(!unit.within(startPos, 50f)){
                        vec.set(startPos).sub(unit).setLength(unit.speed());
                        unit.moveAt(vec);
                        unit.lookAt(startPos);
                    }else{
                        isStrafing = true;
                        hasReachedEnd = false;
                    }
                }else if(!hasReachedEnd){
                    vec.set(endPos).sub(unit).setLength(unit.speed());
                    unit.moveAt(vec);
                    unit.lookAt(endPos);

                    if(unit.within(endPos, 50f)){
                        hasReachedEnd = true;
                        if(unit instanceof AirRaiderUnitType u){
                            u.end = true;
                        }
                    }
                }else{
                    vec.set(extendPos).sub(unit).setLength(unit.speed());
                    unit.moveAt(vec);
                    unit.lookAt(extendPos);
                    deathTimer += Time.delta;
                    if(deathTimer >= deathDelay){
                        unit.kill();
                        deathTimer = 0f;
                    }
                }
            }

            case missile -> {
                if(targetPos == null) return;

                unit.moveAt(vec.trns(unit.rotation, unit.speed()));

                if(!hasReachedEnd){
                    vec.set(targetPos).sub(unit).setLength(unit.speed());
                    unit.moveAt(vec);
                    unit.lookAt(targetPos);
                }else{
                    vec.set(extendPos).sub(unit).setLength(unit.speed());
                    unit.moveAt(vec);
                    unit.lookAt(extendPos);

                    deathTimer += Time.delta;
                    if(deathTimer >= deathDelay){
                        unit.kill();
                        deathTimer = 0f;
                    }
                }

            }
        }

        if(unit instanceof AirRaiderUnitType u){
            u.aiHasReachedEnd = hasReachedEnd;
            u.aiIsStrafing = isStrafing;

            if(!u.aiEndPos.epsilonEquals(endPos, 0.1f)) u.aiEndPos.set(endPos);
            if(!u.aiStartPos.epsilonEquals(startPos, 0.1f)) u.aiStartPos.set(startPos);
            if(!u.aiTargetPos.epsilonEquals(targetPos, 0.1f)) u.aiTargetPos.set(targetPos);
            if(!u.aiExtendPos.epsilonEquals(extendPos, 0.1f)) u.aiExtendPos.set(extendPos);
        }
    }

    @Override
    public void updateWeapons(){
        switch(mode){
            case strafe -> {

                for(var mount : unit.mounts){
                    Weapon weapon = mount.weapon;
                    float wrange = weapon.range();
                    mount.shoot = isStrafing && !hasReachedEnd;
                    mount.aimX = unit.x + unit.vel.x * wrange / tilesize;
                    mount.aimY = unit.y + unit.vel.y * wrange / tilesize;
                }
            }
            case bomb -> {
                if(targetPos == null) return;
                for(var mount : unit.mounts){
                    mount.shoot = hasReachedEnd;
                    mount.aimX = targetPos.x;
                    mount.aimY = targetPos.y;
                }
            }
            case missile -> {
                if(targetPos == null) return;
                for(var mount : unit.mounts){
                    Weapon weapon = mount.weapon;

                    if(weapon.bullet.speed > 0 && weapon.predictTarget){
                        Tmp.v2.set(Predict.intercept(unit, targetPos, weapon.bullet.speed));
                        mount.aimX = mount.weapon.x + Tmp.v2.x;
                        mount.aimY = mount.weapon.y + Tmp.v2.y;

                    }
                    mount.shoot = unit.within(targetPos, weapon.range());
                    if(mount.shoot){
                        shootDuration += Time.delta;
                    }
                }
                if(shootDuration >= maxShootDuration){
                    hasReachedEnd = true;
                    shootDuration = 0;
                    if(unit instanceof AirRaiderUnitType u) u.end = true;
                }
            }

            default -> super.updateWeapons();
        }
    }

}


