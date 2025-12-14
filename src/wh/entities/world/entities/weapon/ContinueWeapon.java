package wh.entities.world.entities.weapon;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.audio.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import wh.entities.bullet.laser.*;
import wh.entities.bullet.laser.ChargePointLaser.*;
import wh.entities.world.entities.weapon.MarkWeapon.*;

import static mindustry.Vars.*;

public class ContinueWeapon extends Weapon{
    public ContinueWeapon(String name){
        super(name);
        continuous = true;
        mountType = MarkWeapon.MarkWeaponMount::new;
    }

    @Override
    public void update(Unit unit, WeaponMount m){
        if(!(m instanceof MarkWeaponMount mount)) return;
        //super.update(unit, m);

        float
        mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
        mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

        boolean can = unit.canShoot();
        float lastReload = mount.reload;
        mount.reload = Math.max(mount.reload - Time.delta * unit.reloadMultiplier, 0);
        mount.recoil = Mathf.approachDelta(mount.recoil, 0, unit.reloadMultiplier / recoilTime);
        if(recoils > 0){
            if(mount.recoils == null) mount.recoils = new float[recoils];
            for(int i = 0; i < recoils; i++){
                mount.recoils[i] = Mathf.approachDelta(mount.recoils[i], 0, unit.reloadMultiplier / recoilTime);
            }
        }
        mount.smoothReload = Mathf.lerpDelta(mount.smoothReload, mount.reload / reload, smoothReloadSpeed);
        mount.charge = mount.charging && shoot.firstShotDelay > 0 ? Mathf.approachDelta(mount.charge, 1, 1 / shoot.firstShotDelay) : 0;

        float warmupTarget = (can && mount.shoot) || (continuous && mount.bullet != null) || mount.charging ? 1f : 0f;
        if(linearWarmup){
            mount.warmup = Mathf.approachDelta(mount.warmup, warmupTarget, shootWarmupSpeed);
        }else{
            mount.warmup = Mathf.lerpDelta(mount.warmup, warmupTarget, shootWarmupSpeed);
        }

        //find a new target
        if(!controllable && autoTarget){
            if((mount.retarget -= Time.delta) <= 0f){
                mount.target = findTarget(unit, mountX, mountY, mount.bulletWhich.range, mount.bulletWhich.collidesAir, mount.bulletWhich.collidesGround);
                mount.retarget = mount.target == null ? targetInterval : targetSwitchInterval;
            }

            if(mount.target != null && checkTarget(unit, mount.target, mountX, mountY, bullet.range)){
                mount.target = null;
            }

            boolean shoot = false;

            if(mount.target != null){
                shoot = mount.target.within(mountX, mountY, bullet.range + Math.abs(shootY) + (mount.target instanceof Sized s ? s.hitSize() / 2f : 0f)) && can;

                if(predictTarget){
                    Vec2 to = Predict.intercept(unit, mount.target, bullet.speed);
                    mount.aimX = to.x;
                    mount.aimY = to.y;
                }else{
                    mount.aimX = mount.target.x();
                    mount.aimY = mount.target.y();
                }
            }

            mount.shoot = mount.rotate = shoot;

            //note that shooting state is not affected, as these cannot be controlled
            //logic will return shooting as false even if these return true, which is fine
        }

        //rotate if applicable
        if(rotate && (mount.rotate || mount.shoot) && can){
            float axisX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
            axisY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

            mount.targetRotation = Angles.angle(axisX, axisY, mount.aimX, mount.aimY) - unit.rotation;
            mount.rotation = Angles.moveToward(mount.rotation, mount.targetRotation, rotateSpeed * Time.delta);
            if(rotationLimit < 360){
                float dst = Angles.angleDist(mount.rotation, baseRotation);
                if(dst > rotationLimit / 2f){
                    mount.rotation = Angles.moveToward(mount.rotation, baseRotation, dst - rotationLimit / 2f);
                }
            }
        }else if(!rotate){
            mount.rotation = baseRotation;
            mount.targetRotation = unit.angleTo(mount.aimX, mount.aimY);
        }

        float
        weaponRotation = unit.rotation - 90 + (rotate ? mount.rotation : baseRotation),
        bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX, this.shootY),
        bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX, this.shootY),
        shootAngle = bulletRotation(unit, mount, bulletX, bulletY);

        if(alwaysShooting) mount.shoot = true;

        if(continuous && mount.bullet != null && mount.bullets.size > 0){
            mount.bullets.removeAll(b -> b == null || !b.isAdded() || b.type == null || b.time >= b.lifetime || mount.bullet.type != mount.bulletWhich);
            for(Bullet bullet : mount.bullets){

                Vec2 pos = mount.shootPoints.get(bullet);
                float
                bx = mountX + Angles.trnsx(weaponRotation, this.shootX + pos.x, this.shootY + pos.y),
                by = mountY + Angles.trnsy(weaponRotation, this.shootX + pos.x, this.shootY + pos.y);
                bullet.set(bx, by);
                bullet.rotation(weaponRotation + 90);

                mount.reload = reload;
                mount.recoil = 1f;

                unit.vel.add(Tmp.v1.trns(bullet.rotation() + 180f, bullet.type.recoil * Time.delta));
                if(shootSound != Sounds.none && !headless){
                    if(mount.sound == null) mount.sound = new SoundLoop(shootSound, 1f);
                    mount.sound.update(bulletX, bulletY, true);
                }

                //target length of laser
                float shootLength = Math.min(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY), range());
                //current length of laser
                float curLength = Mathf.dst(bulletX, bulletY, bullet.aimX, bullet.aimY);
                //resulting length of the bullet (smoothed)
                float resultLength = Mathf.approachDelta(curLength, shootLength, aimChangeSpeed);
                //actual aim end point based on length
                Tmp.v1.trns(shootAngle, mount.lastLength = resultLength).add(bulletX, bulletY);

                bullet.aimX = Tmp.v1.x;
                bullet.aimY = Tmp.v1.y;

                if(alwaysContinuous && mount.shoot){

                    bullet.time = bullet.lifetime * bullet.type.optimalLifeFract * mount.warmup;
                    bullet.keepAlive = true;

                    unit.apply(shootStatus, shootStatusDuration);
                }
                if(bullet.time >= bullet.lifetime){
                    mount.bullets.remove(bullet);
                    mount.shootPoints.remove(bullet);
                }
            }
        }else{
            //heat decreases when not firing
            mount.heat = Math.max(mount.heat - Time.delta * unit.reloadMultiplier / cooldownTime, 0);

            if(mount.sound != null){
                mount.sound.update(bulletX, bulletY, false);
            }
        }

        //flip weapon shoot side for alternating weapons
        boolean wasFlipped = mount.side;
        if(otherSide != -1 && alternate && mount.side == flipSprite && mount.reload <= reload / 2f && lastReload > reload / 2f){
            unit.mounts[otherSide].side = !unit.mounts[otherSide].side;
            mount.side = !mount.side;
        }

        //shoot if applicable
        if(mount.shoot && //must be shooting
        can && //must be able to shoot
        !(bullet.killShooter && mount.totalShots > 0) && //if the bullet kills the shooter, you should only ever be able to shoot once
        (!useAmmo || unit.ammo > 0 || !state.rules.unitAmmo || unit.team.rules().infiniteAmmo) && //check ammo
        (!alternate || wasFlipped == flipSprite) &&
        mount.warmup >= minWarmup && //must be warmed up
        unit.vel.len() >= minShootVelocity && //check velocity requirements
        (mount.reload <= 0.0001f || (alwaysContinuous && mount.bullet == null)) && //reload has to be 0, or it has to be an always-continuous weapon
        (alwaysShooting || Angles.within(rotate ? mount.rotation : unit.rotation + baseRotation, mount.targetRotation, shootCone)) //has to be within the cone
        ){
            shoot(unit, mount, bulletX, bulletY, shootAngle);

            mount.reload = reload;

            if(useAmmo){
                unit.ammo--;
                if(unit.ammo < 0) unit.ammo = 0;
            }
        }
    }

    @Override
    protected void bullet(Unit unit, WeaponMount m, float xOffset, float yOffset, float angleOffset, Mover mover){
        super.bullet(unit, m, xOffset, yOffset, angleOffset, mover);
        if(!(m instanceof MarkWeaponMount mount)) return;
        mount.bullets.add(mount.bullet);
        mount.shootPoints.put(mount.bullet, new Vec2(xOffset, yOffset));
    }
}
