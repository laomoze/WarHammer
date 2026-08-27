package wh.entities.world.entities.weapon;

import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectFloatMap;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.audio.SoundLoop;
import mindustry.entities.Mover;
import mindustry.entities.Predict;
import mindustry.entities.Sized;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.type.Weapon;
import wh.entities.world.entities.weapon.MarkWeapon.MarkWeaponMount;

import static mindustry.Vars.headless;

public class MulitShootContinueWeapon extends Weapon{
    public float shootRotateSpeed = -1f;
    public MulitShootContinueWeapon(String name){
        super(name);
        continuous = true;
        mountType = MultiShootMount::new;
    }

    @Override
    public void update(Unit unit, WeaponMount m){
        if (!(m instanceof MultiShootMount mount)) return;
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
                mount.target = findTarget(unit, mountX, mountY, bullet.range, bullet.collidesAir, bullet.collidesGround);
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
            float effectiveRotateSpeed = continuous && mount.bullet != null && mount.bullets.size > 0 && mount.bullets.any() && shootRotateSpeed >= 0f ? shootRotateSpeed : rotateSpeed;
            mount.rotation = Angles.moveToward(mount.rotation, mount.targetRotation, effectiveRotateSpeed * Time.delta);
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
            mount.bullets.removeAll(b -> b == null || !b.isAdded() || b.type == null || b.time >= b.lifetime || mount.bullet.type != bullet);
            for(Bullet bullet : mount.bullets){

                Vec2 pos = mount.shootPoints.get(bullet);
                float angleOffset = mount.shootAngles.get(bullet, 0f);
                if (pos == null) {
                    mount.shootAngles.remove(bullet, 0f);
                    continue;
                }
                float
                bx = mountX + Angles.trnsx(weaponRotation, this.shootX + pos.x, this.shootY + pos.y),
                by = mountY + Angles.trnsy(weaponRotation, this.shootX + pos.x, this.shootY + pos.y);
                bullet.set(bx, by);
                bullet.rotation(weaponRotation + 90f + angleOffset);

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
                Tmp.v1.trns(shootAngle + angleOffset, mount.lastLength = resultLength).add(bulletX, bulletY);

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
                    mount.shootAngles.remove(bullet, 0f);
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

        if (mount.shoot &&
                can &&
                !(bullet.killShooter && mount.totalShots > 0) &&
                (!alternate || wasFlipped == flipSprite) &&
                mount.warmup >= minWarmup &&
                unit.vel.len() >= minShootVelocity &&
                (mount.reload <= 0.0001f || (alwaysContinuous && mount.bullet == null)) &&
                (alwaysShooting || Angles.within(rotate ? mount.rotation : unit.rotation + baseRotation, mount.targetRotation, shootCone)) //has to be within the cone
        ){
            shoot(unit, mount, bulletX, bulletY, shootAngle);

            mount.reload = reload;
        }
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
        unit.apply(shootStatus, shootStatusDuration);

        if (shoot.firstShotDelay > 0) {
            mount.charging = true;
            shoot.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                if (delay > 0f) {
                    Time.run(delay - shoot.firstShotDelay, () -> {
                        Vec2 v1 = new Vec2().trns(rotation - 90f, xOffset, yOffset);
                        float fx = shootX + v1.x;
                        float fy = shootY + v1.y;
                        bullet.chargeEffect.at(fx, fy, rotation, unit);
                        chargeSound.at(fx, fy, Mathf.random(soundPitchMin, soundPitchMax));
                    });
                }
            }, () -> mount.barrelCounter++);
        }

        shoot.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
            //this is incremented immediately, as it is used for total bullet creation amount detection
            mount.totalShots++;
            int barrel = mount.barrelCounter;
            if (delay > 0f) {
                Time.run(delay, () -> {
                    //hack: make sure the barrel is the same as what it was when the bullet was queued to fire
                    int prev = mount.barrelCounter;
                    mount.barrelCounter = barrel;
                    bullet(unit, mount, xOffset, yOffset, angle, mover);
                    mount.barrelCounter = prev;
                });
            } else {
                bullet(unit, mount, xOffset, yOffset, angle, mover);
            }
        }, () -> mount.barrelCounter++);
    }

    @Override
    protected void bullet(Unit unit, WeaponMount m, float xOffset, float yOffset, float angleOffset, Mover mover){
        super.bullet(unit, m, xOffset, yOffset, angleOffset, mover);
        if (!(m instanceof MultiShootMount mount) || mount.bullet == null) return;
        mount.bullets.add(mount.bullet);
        mount.shootPoints.put(mount.bullet, new Vec2(xOffset, yOffset));
        mount.shootAngles.put(mount.bullet, angleOffset);
    }

    public static class MultiShootMount extends MarkWeaponMount {
        public final ObjectFloatMap<Bullet> shootAngles = new ObjectFloatMap<>();

        public MultiShootMount(Weapon weapon) {
            super(weapon);
        }
    }
}
