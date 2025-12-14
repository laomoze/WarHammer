package wh.entities.world.entities.powerArmorComp;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.audio.*;
import mindustry.entities.*;
import mindustry.entities.part.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

import static mindustry.Vars.*;

public class MainWeapon extends Weapon{
    public MainWeapon(String name){
        super(name);
    }

    {
        mountType = MainWeaponMount::new;
    }

    public float actionTime;
    public boolean melee = false;

    public Interp actionInInterp = Interp.linear;
    public Interp actionOutInterp = Interp.linear;

    public Seq<DrawUnitPart> unitParts = new Seq<>(DrawUnitPart.class);

/*    public boolean switchWeapon = false;
    public float switchSpeed = 0.1f;*/

    public float smoothHeatSpeed = 0.2f;

    /* public Seq<Weapon> secondaryWeapons = new Seq<>(Weapon.class);*/


    @Override
    public void init(){
        super.init();
       /* if(switchWeapon){
            for(Weapon w : secondaryWeapons){
                w.init();
            }
        }*/
        weaponRange();
    }

    public MainWeapon copy(){
        try{
            return (MainWeapon)clone();
        }catch(CloneNotSupportedException suck){
            throw new RuntimeException("very good language design", suck);
        }
    }

    public void loadUnitName(String name){
        for(var part : unitParts){
            part.turretShading = false;
            part.load(name);
        }
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){

        if(!(unit instanceof PowerArmourUnit unit1)) return;
        if(!(mount instanceof MainWeaponMount mount1)) return;
        if(!(unit1.type instanceof PowerArmourUnitType type)) return;

        //apply layer offset, roll it back at the end
        float z = Draw.z();
        Draw.z(z + layerOffset);

        float acTime, sHeat;
        /*if(melee && switchWeapon){
            acTime = mount1.actionInterpProgress * mount1.switchProgress;

            sHeat = mount1.smoothHeat * mount1.switchProgress* mount1.actionInterpProgress;

        }else*/
        if(melee){
            acTime = mount1.actionInterpProgress;
            sHeat = mount1.smoothHeat * mount1.actionInterpProgress;
        }else{
            acTime = 1;
            sHeat = mount1.smoothHeat;
        }


        float
        rotation = unit.rotation - 90,
        realRecoil = Mathf.pow(mount.recoil, recoilPow) * recoil,
        weaponRotation = rotation + (rotate ? mount.rotation : baseRotation);

        float
        wx = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, -realRecoil),
        wy = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, -realRecoil);

        if(shadow > 0){
            Drawf.shadow(wx, wy, shadow);
        }

        if(top){
            drawOutline(unit, mount);
        }

        if(parts.size > 0){
            DrawPart.params.set(mount.warmup, mount.reload / reload, mount.smoothReload, mount.heat, mount.recoil, mount.charge, wx, wy,
            weaponRotation + 90);
            DrawPart.params.sideMultiplier = flipSprite ? -1 : 1;

            for(int i = 0; i < parts.size; i++){
                var part = parts.get(i);
                DrawPart.params.setRecoil(part.recoilIndex >= 0 && mount.recoils != null ? mount.recoils[part.recoilIndex] : mount.recoil);
                if(part.under){
                    unit.type.applyColor(unit);
                    part.draw(DrawPart.params);
                }
            }
        }

        if(unitParts.size > 0){

            DrawUnitPart.params.set(unit1, type, mount.warmup, mount.reload / reload, mount.smoothReload, sHeat,
            mount.heat, mount.recoil, mount.charge, acTime, unit1.x, unit1.y, weaponRotation + 90);
            DrawUnitPart.params.sideMultiplier = flipSprite ? -1 : 1;

            for(int i = 0; i < unitParts.size; i++){
                var part = unitParts.get(i);
                DrawUnitPart.params.setRecoil(part.recoilIndex >= 0 && mount.recoils != null ? mount.recoils[part.recoilIndex] : mount.recoil);
                unit.type.applyColor(unit);
                part.draw(DrawUnitPart.params);
            }
        }

        float prev = Draw.xscl;

        Draw.xscl *= -Mathf.sign(flipSprite);

        //fix color
        unit.type.applyColor(unit);

        if(region.found()) Draw.rect(region, wx, wy, weaponRotation);

        if(cellRegion.found()){
            Draw.color(unit.type.cellColor(unit));
            Draw.rect(cellRegion, wx, wy, weaponRotation);
            Draw.color();
        }

        if(heatRegion.found() && mount.heat > 0){
            Draw.color(heatColor, mount.heat);
            Draw.blend(Blending.additive);
            Draw.rect(heatRegion, wx, wy, weaponRotation);
            Draw.blend();
            Draw.color();
        }

        Draw.xscl = prev;

        if(parts.size > 0){
            //TODO does it need an outline?
            for(int i = 0; i < parts.size; i++){
                var part = parts.get(i);
                DrawPart.params.setRecoil(part.recoilIndex >= 0 && mount.recoils != null ? mount.recoils[part.recoilIndex] : mount.recoil);
                if(!part.under){
                    unit.type.applyColor(unit);
                    part.draw(DrawPart.params);
                }
            }
        }

        Draw.xscl = 1f;

        Draw.z(z);
    }

    public float weaponRange(){
      /*  secondaryWeapons.sort(h -> range());
        if(switchWeapon){
            return Math.max(bullet.range, secondaryWeapons.peek().range());
        }*/
        return bullet.range;
    }

    @Override
    public float range(){
        return super.range();
    }

  /*  public void checkSwitchWeapon(Unit unit, MainWeaponMount mount, float range, float secondaryWeaponRange){
        boolean can = unit.canShoot();
        float
        mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
        mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

        if(!controllable && autoTarget && switchWeapon && melee){
            float searchRange = Math.max(range, secondaryWeaponRange);
            if((mount.retarget -= Time.delta) <= 0f){
                mount.target = findTarget(unit, mountX, mountY, searchRange, bullet.collidesAir, bullet.collidesGround);
                mount.retarget = mount.target == null ? targetInterval : targetSwitchInterval;
            }

            if(mount.target != null && checkTarget(unit, mount.target, mountX, mountY, searchRange)){
                mount.target = null;
            }

            boolean shoot = false;

            if(mount.target != null){

                shoot = mount.target.within(mountX, mountY, searchRange + Math.abs(shootY) + (mount.target instanceof Sized s ? s.hitSize() / 2f : 0f)) && can;

                float targetDistance = Mathf.dst(mountX, mountY, mount.target.x(), mount.target.y());

                boolean inMainWeaponRange = targetDistance <= range + (mount.target instanceof Sized s ? s.hitSize() / 2f : 0f);

                boolean inSecondaryWeaponRange = targetDistance <= secondaryWeaponRange + (mount.target instanceof Sized s ? s.hitSize() / 2f : 0f);

                if(inMainWeaponRange){
                    mount.switchWeapon = false;
                    shoot = can;
                }else if(inSecondaryWeaponRange && switchWeapon && !secondaryWeapons.isEmpty()){
                    mount.switchWeapon = true;
                    shoot = can;
                }else{
                    mount.switchWeapon = true;
                    shoot = false;
                }

                if(predictTarget){
                    Vec2 to = Predict.intercept(unit, mount.target, bullet);
                    mount.aimX = to.x;
                    mount.aimY = to.y;
                }else{
                    mount.aimX = mount.target.x();
                    mount.aimY = mount.target.y();
                }
            }else{
                mount.switchWeapon = true;
            }

            mount.shoot = mount.rotate = shoot;
        }
    }*/

    public void checkTarget(Unit unit, MainWeaponMount mount){
        boolean can = unit.canShoot();
        float
        mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
        mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

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
                    Vec2 to = Predict.intercept(unit, mount.target, bullet);
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
    }

    ;

    @Override
    public void update(Unit m, WeaponMount n){
        if(!(m instanceof PowerArmourUnit unit)) return;
        if(!(n instanceof MainWeaponMount mount)) return;

        boolean can = unit.canShoot();
        mount.reload = Math.max(mount.reload - Time.delta * unit.reloadMultiplier, 0);
        mount.charge = mount.charging && shoot.firstShotDelay > 0 ? Mathf.approachDelta(mount.charge, 1, 1 / shoot.firstShotDelay) : 0;

        mount.recoil = Mathf.approachDelta(mount.recoil, 0, unit.reloadMultiplier / recoilTime);

        mount.smoothReload = Mathf.lerpDelta(mount.smoothReload, mount.reload / reload, smoothReloadSpeed);

        float lastHeat = mount.heat;
        mount.smoothHeat = Mathf.lerpDelta(mount.smoothHeat, lastHeat, smoothHeatSpeed);

        boolean shouldShoot = (can && mount.shoot) || (continuous && mount.bullet != null) || mount.charging;
        float warmupTarget;
        if(melee){
            warmupTarget = shouldShoot && mount.reload <= 0.001f ? 1f : 0f;
        }else{
            warmupTarget = shouldShoot ? 1f : 0f;
        }
        if(linearWarmup){
            mount.warmup = Mathf.approachDelta(mount.warmup, warmupTarget, shootWarmupSpeed);
        }else{
            mount.warmup = Mathf.lerpDelta(mount.warmup, warmupTarget, shootWarmupSpeed);
        }

      /*  if(switchWeapon && secondaryWeapons != null){
            float range = weaponRange();
            checkSwitchWeapon(unit, mount, range(), range);
            mount.switchProgress = Mathf.approachDelta(mount.switchProgress, mount.switchWeapon ? 0 : 1, switchSpeed);


            if(mount.switchProgress < 0.001f){
                for(var weapon : secondaryWeapons){
                    weapon.update(unit, mount);
                }
            }else{
                updateThisWeapon(unit, mount);
            }
        }else{
            checkTarget(unit, mount);
            updateThisWeapon(unit, mount);
        }*/

        checkTarget(unit, mount);
        updateThisWeapon(unit, mount);
    }

    public void updateThisWeapon(Unit unit, MainWeaponMount mount){
        boolean can = unit.canShoot();
        float lastReload = mount.reload;

        float
        mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
        mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

        //rotate if applicable
        if(rotate && (mount.rotate || mount.shoot) && can){
            float
            axisX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
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

        //update continuous state
        if(continuous && mount.bullet != null){
            if(!mount.bullet.isAdded() || mount.bullet.time >= mount.bullet.lifetime || mount.bullet.type != bullet){
                mount.bullet = null;
            }else{
                mount.bullet.rotation(weaponRotation + 90);
                mount.bullet.set(bulletX, bulletY);
                mount.reload = reload;
                mount.recoil = 1f;
                unit.vel.add(Tmp.v1.trns(mount.bullet.rotation() + 180f, mount.bullet.type.recoil * Time.delta));
                if(shootSound != Sounds.none && !headless){
                    if(mount.sound == null) mount.sound = new SoundLoop(shootSound, 1f);
                    mount.sound.update(bulletX, bulletY, true);
                }

                //target length of laser
                float shootLength = Math.min(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY), range());
                //current length of laser
                float curLength = Mathf.dst(bulletX, bulletY, mount.bullet.aimX, mount.bullet.aimY);
                //resulting length of the bullet (smoothed)
                float resultLength = Mathf.approachDelta(curLength, shootLength, aimChangeSpeed);
                //actual aim end point based on length
                Tmp.v1.trns(shootAngle, mount.lastLength = resultLength).add(bulletX, bulletY);

                mount.bullet.aimX = Tmp.v1.x;
                mount.bullet.aimY = Tmp.v1.y;

                if(alwaysContinuous && mount.shoot){
                    mount.bullet.time = mount.bullet.lifetime * mount.bullet.type.optimalLifeFract * mount.warmup;
                    mount.bullet.keepAlive = true;

                    unit.apply(shootStatus, shootStatusDuration);
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

        float pg = Mathf.approachDelta(mount.actionProgress, mount.shouldAction ? 1 : 0, unit.reloadMultiplier / actionTime);

        Interp interp = mount.shouldAction ? actionInInterp : actionOutInterp;

        mount.actionProgress = pg;
        mount.actionProgress2 = Mathf.approachDelta(mount.actionProgress, mount.shouldAction ? 1 : 0, mount.shouldAction ? 0.2f : 0.03f);
        mount.actionInterpProgress = interp.apply(mount.actionProgress);

        if(mount.actionProgress >= 1f){
            mount.shouldAction = false;
        }
    }

    @Override
    protected void shoot(Unit m, WeaponMount n, float shootX, float shootY, float rotation){
        if(!(m instanceof PowerArmourUnit unit)) return;
        if(!(n instanceof MainWeaponMount mount)) return;
        unit.apply(shootStatus, shootStatusDuration);
        mount.reset = mount.shouldAction = true;

        if(shoot.firstShotDelay > 0){
            mount.charging = true;
            chargeSound.at(shootX, shootY, Mathf.random(soundPitchMin, soundPitchMax));
            bullet.chargeEffect.at(shootX, shootY, rotation, bullet.keepVelocity || parentizeEffects ? unit : null);
        }

        shoot.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
            //this is incremented immediately, as it is used for total bullet creation amount detection
            mount.totalShots++;
            int barrel = mount.barrelCounter;

            if(delay > 0f){
                Time.run(delay, () -> {
                    //hack: make sure the barrel is the same as what it was when the bullet was queued to fire
                    int prev = mount.barrelCounter;
                    mount.barrelCounter = barrel;
                    bullet(unit, mount, xOffset, yOffset, angle, mover);
                    mount.barrelCounter = prev;
                });
            }else{
                bullet(unit, mount, xOffset, yOffset, angle, mover);
            }
        }, () -> mount.barrelCounter++);

    }
}
