package wh.entities.world.entities.weapon;

import arc.math.Mathf;
import arc.struct.IntMap;
import arc.util.Time;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.Weapon;

public class ShotBulletWeapon extends Weapon {
    public final IntMap<BulletType> shotBullets = new IntMap<>();

    public ShotBulletWeapon(String name) {
        super(name);
        mountType = ShotBulletMount::new;
    }

    public ShotBulletWeapon shotBullet(int shot, BulletType bullet) {
        if (shot < 1) return this;

        if (bullet == null) {
            shotBullets.remove(shot);
        } else {
            shotBullets.put(shot, bullet);
        }
        return this;
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
        ShotBulletMount shotMount = (ShotBulletMount) mount;
        shotMount.shotIndex = 0;

        unit.apply(shootStatus, shootStatusDuration);

        if (shoot.firstShotDelay > 0f) {
            mount.charging = true;
            chargeSound.at(shootX, shootY, Mathf.random(soundPitchMin, soundPitchMax));
            bullet.chargeEffect.at(shootX, shootY, rotation, bullet.keepVelocity || parentizeEffects ? unit : null);
        }

        shoot.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
            mount.totalShots++;
            int barrel = mount.barrelCounter;
            int shot = ++shotMount.shotIndex;

            Runnable fire = () -> {
                int previousBarrel = mount.barrelCounter;
                mount.barrelCounter = barrel;
                bullet(unit, mount, xOffset, yOffset, angle, mover, shot);
                mount.barrelCounter = previousBarrel;
            };

            if (delay > 0f) {
                Time.run(delay, fire);
            } else {
                fire.run();
            }
        }, () -> mount.barrelCounter++);
    }

    protected void bullet(Unit unit, WeaponMount mount, float xOffset, float yOffset, float angleOffset, Mover mover, int shot) {
        BulletType defaultBullet = bullet;
        BulletType shotBullet = shotBullets.get(shot);

        if (shotBullet != null) {
            bullet = shotBullet;
        }

        try {
            super.bullet(unit, mount, xOffset, yOffset, angleOffset, mover);
        } finally {
            bullet = defaultBullet;
        }
    }

    public static class ShotBulletMount extends WeaponMount {
        public int shotIndex;

        public ShotBulletMount(Weapon weapon) {
            super(weapon);
        }
    }
}
