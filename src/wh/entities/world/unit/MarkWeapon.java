package wh.entities.world.unit;

import arc.math.*;
import arc.scene.ui.layout.Table;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.ShootAlternate;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.type.*;

public class MarkWeapon extends Weapon{

    public float markChance = 0.3f;
    public BulletType markBullet;
    public ShootPattern markShoot = new ShootAlternate(5f);
    public MarkWeapon(String name){
        super(name);
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
        unit.apply(shootStatus, shootStatusDuration);

        ShootPattern specialShoot ;
        BulletType specialBullet ;
        if(Mathf.chance(markChance)){
            specialBullet = markBullet != null ? markBullet : bullet;
            specialShoot = markShoot != null ? markShoot : shoot;
        }else {
            specialShoot = shoot;
            specialBullet = bullet;
        }

        if(specialShoot.firstShotDelay > 0){
            mount.charging = true;
            chargeSound.at(shootX, shootY, Mathf.random(soundPitchMin, soundPitchMax));
            specialBullet.chargeEffect.at(shootX, shootY, rotation, specialBullet.keepVelocity || parentizeEffects ? unit : null);
        }

        specialShoot.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
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

    @Override
    protected void bullet(Unit unit, WeaponMount mount, float xOffset, float yOffset, float angleOffset, Mover mover){
        if(!unit.isAdded()) return;

        BulletType specialBullet ;
        if(Mathf.chance(markChance)){
            specialBullet = markBullet != null ? markBullet : bullet;
        }else {
            specialBullet = bullet;
        }

        mount.charging = false;
        float
        xSpread = Mathf.range(xRand),
        ySpread = Mathf.range(yRand),
        weaponRotation = unit.rotation - 90 + (rotate ? mount.rotation : baseRotation),
        mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
        mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y),
        bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread),
        bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread),
        shootAngle = bulletRotation(unit, mount, bulletX, bulletY) + angleOffset,
        lifeScl = specialBullet.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY) / specialBullet.range) : 1f,
        angle = shootAngle + Mathf.range(inaccuracy + specialBullet.inaccuracy);

        Entityc shooter = unit.controller() instanceof MissileAI ai ? ai.shooter : unit; //Pass the missile's shooter down to its bullets
        mount.bullet = specialBullet.create(unit, shooter, unit.team, bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd) + extraVelocity, lifeScl, null, mover, mount.aimX, mount.aimY, mount.target);
        handleBullet(unit, mount, mount.bullet);

        if(!continuous){
            shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
        }

        ejectEffect.at(mountX, mountY, angle * Mathf.sign(this.x));
        specialBullet.shootEffect.at(bulletX, bulletY, angle, specialBullet.hitColor, unit);
        specialBullet.smokeEffect.at(bulletX, bulletY, angle, specialBullet.hitColor, unit);

        unit.vel.add(Tmp.v1.trns(shootAngle + 180f, specialBullet.recoil));
        Effect.shake(shake, shake, bulletX, bulletY);
        mount.recoil = 1f;
        if(recoils > 0){
            mount.recoils[mount.barrelCounter % recoils] = 1f;
        }
        mount.heat = 1f;
    }

    @Override
    public void addStats(UnitType u, Table t){
        super.addStats(u, t);
        t.row();
        t.add("[lightgray]wh-markChance: [white]" + (int)(markChance * 100) + "%");
        t.row();
        t.add("[lightgray]wh-markBullet: [white]" + (markBullet != null ? markBullet : "none"));
    }

}