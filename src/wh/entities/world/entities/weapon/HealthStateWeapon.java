package wh.entities.world.entities.weapon;

import arc.Core;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import wh.content.WHStats;
import wh.ui.UIUtils;

/**
 * 低血武器状态机：
 * 1) 可在低于阈值时切换到另一套 bullet + shoot；
 * 2) 可设置“仅在低于阈值时允许开火”；
 * 3) 保留默认 Weapon 行为，不改动其它武器类。
 */
public class HealthStateWeapon extends Weapon {
    /**
     * 低于该血量百分比时切换到 lowHealthBullet/lowHealthShoot；<0 表示禁用。
     */
    public float switchBulletHealthf = -1f;
    /**
     * 低于该血量百分比才允许开火；<0 表示不限制。
     */
    public float shootRequireHealthf = -1f;
    /**
     * 低血状态使用的子弹。
     */
    public BulletType lowHealthBullet;
    /**
     * 低血状态使用的射击模式。
     */
    public ShootPattern lowHealthShoot = new ShootPattern();

    public HealthStateWeapon(String name) {
        super(name);
        mountType = HealthStateMount::new;
    }

    public boolean thresholdEnabled(float healthf) {
        return healthf >= 0f && healthf <= 1f;
    }

    public boolean inLowHealthState(Unit unit) {
        return unit != null && lowHealthBullet != null && thresholdEnabled(switchBulletHealthf) && unit.healthf() <= switchBulletHealthf;
    }

    public boolean allowFireByHealth(Unit unit) {
        return unit != null && (!thresholdEnabled(shootRequireHealthf) || unit.healthf() <= shootRequireHealthf);
    }

    public boolean hasConfiguredLowHealthShoot() {
        if (lowHealthShoot == null) return false;
        // keep compatibility with direct field assignments like lowHealthShoot.firstShotDelay = ...
        if (lowHealthShoot.getClass() != ShootPattern.class) return true;
        return lowHealthShoot.firstShotDelay > 0f || lowHealthShoot.shotDelay > 0f || lowHealthShoot.shots != 1;
    }

    @Override
    public void update(Unit unit, WeaponMount m) {
        HealthStateMount mount = m instanceof HealthStateMount hm ? hm : null;
        BulletType prevBullet = bullet;
        ShootPattern prevShoot = shoot;

        if (mount != null) {
            mount.lowHealthActive = inLowHealthState(unit);
            mount.healthShootAllowed = allowFireByHealth(unit);
            mount.shootWhich = mount.lowHealthActive && hasConfiguredLowHealthShoot() ? lowHealthShoot : shoot;
            mount.bulletWhich = mount.lowHealthActive && lowHealthBullet != null ? lowHealthBullet : bullet;
        }

        try {
            // super.update 的目标选择/持续子弹逻辑依赖 weapon.bullet/shoot。
            if (mount != null) {
                if (mount.bulletWhich != null) bullet = mount.bulletWhich;
                if (mount.shootWhich != null) shoot = mount.shootWhich;
            }
            super.update(unit, m);
        } finally {
            if (mount != null) {
                bullet = prevBullet;
                shoot = prevShoot;
            }
        }

        if (mount != null && !mount.healthShootAllowed && continuous && mount.bullet != null) {
            mount.bullet.keepAlive = false;
        }
    }

    @Override
    protected void shoot(Unit unit, WeaponMount m, float shootX, float shootY, float rotation) {
        if (!(m instanceof HealthStateMount mount)) return;
        if (!mount.healthShootAllowed) return;

        unit.apply(shootStatus, shootStatusDuration);

        ShootPattern pattern = mount.shootWhich != null ? mount.shootWhich : shoot;
        BulletType useBullet = mount.bulletWhich != null ? mount.bulletWhich : bullet;
        if (pattern == null || useBullet == null) return;

        float baseDelay = resolveBaseFirstShotDelay(pattern);
        boolean hasDelayed = baseDelay > 0.001f;

        if (hasDelayed) {
            mount.charging = true;
            if (pattern instanceof ShootMulti) {
                pattern.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                    if (delay + 0.001f < baseDelay) return;
                    Runnable chargeTask = () -> {
                        if (!unit.isAdded()) return;
                        Vec2 v1 = new Vec2().trns(rotation - 90f, xOffset, yOffset);
                        float fx = shootX + v1.x;
                        float fy = shootY + v1.y;
                        chargeSound.at(fx, fy, Mathf.random(soundPitchMin, soundPitchMax));
                        useBullet.chargeEffect.at(fx, fy, rotation, parentizeEffects ? unit : null);
                    };
                    chargeTask.run();
                }, () -> mount.barrelCounter++);
            }
        } else {
            pattern.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                if (delay + 0.001f < baseDelay) return;
                float shotDelay = Math.max(delay - baseDelay, 0f);
                Runnable chargeTask = () -> {
                    if (!unit.isAdded()) return;
                    Vec2 v1 = new Vec2().trns(rotation - 90f, xOffset, yOffset);
                    float fx = shootX + v1.x;
                    float fy = shootY + v1.y;
                    chargeSound.at(fx, fy, Mathf.random(soundPitchMin, soundPitchMax));
                    useBullet.chargeEffect.at(fx, fy, rotation, parentizeEffects ? unit : null);
                };
                if (shotDelay > 0f) {
                    Time.run(shotDelay, chargeTask);
                } else {
                    chargeTask.run();
                }
            }, () -> mount.barrelCounter++);
        }

        pattern.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
            mount.totalShots++;
            int barrel = mount.barrelCounter;
            float shotDelay = Math.max(delay - baseDelay, 0f);

            Runnable fireTask = () -> {
                if (!unit.isAdded()) return;
                int prev = mount.barrelCounter;
                mount.barrelCounter = barrel;
                bullet(unit, mount, xOffset, yOffset, angle, mover);
                mount.barrelCounter = prev;
            };

            if (shotDelay > 0f) {
                Time.run(shotDelay, fireTask);
            } else {
                fireTask.run();
            }
        }, () -> mount.barrelCounter++);
    }

    protected float resolveBaseFirstShotDelay(ShootPattern pattern) {
        if (pattern == null) return 0f;
        if (pattern instanceof ShootMulti multi) {
            return multi.source == null ? 0f : Math.max(multi.source.firstShotDelay, 0f);
        }
        return Math.max(pattern.firstShotDelay, 0f);
    }

    @Override
    protected void bullet(Unit unit, WeaponMount m, float xOffset, float yOffset, float angleOffset, Mover mover) {
        if (!unit.isAdded()) return;
        if (!(m instanceof HealthStateMount mount)) return;

        BulletType useBullet = mount.bulletWhich != null ? mount.bulletWhich : bullet;
        if (useBullet == null) return;

        mount.charging = false;
        float xSpread = Mathf.range(xRand), ySpread = Mathf.range(yRand);
        float weaponRotation = unit.rotation - 90f + (rotate ? mount.rotation : baseRotation);
        float mountX = unit.x + Angles.trnsx(unit.rotation - 90f, x, y);
        float mountY = unit.y + Angles.trnsy(unit.rotation - 90f, x, y);
        float bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread);
        float bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread);
        float shootAngle = bulletRotation(unit, mount, bulletX, bulletY) + angleOffset;
        float lifeScl = useBullet.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY) / useBullet.range) : 1f;
        float angle = shootAngle + Mathf.range(inaccuracy + useBullet.inaccuracy);

        Entityc shooter = unit.controller() instanceof mindustry.ai.types.MissileAI ai ? ai.shooter : unit;
        Bullet b = mount.bullet = useBullet.create(
                unit, shooter, unit.team, bulletX, bulletY, angle,
                -1f, (1f - velocityRnd) + Mathf.random(velocityRnd) + extraVelocity,
                lifeScl, null, mover, mount.aimX, mount.aimY, mount.target
        );
        handleBullet(unit, mount, b);

        if (!continuous) {
            shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
        }

        ejectEffect.at(mountX, mountY, angle * Mathf.sign(this.x));
        useBullet.shootEffect.at(bulletX, bulletY, angle, useBullet.hitColor, unit);
        useBullet.smokeEffect.at(bulletX, bulletY, angle, useBullet.hitColor, unit);

        unit.vel.add(Tmp.v1.trns(shootAngle + 180f, useBullet.recoil));
        Effect.shake(shake, shake, bulletX, bulletY);
        mount.recoil = 1f;
        if (recoils > 0) {
            mount.recoils[mount.barrelCounter % recoils] = 1f;
        }
        mount.heat = 1f;
    }

    @Override
    public void addStats(UnitType u, Table t) {
        super.addStats(u, t);

        if (thresholdEnabled(shootRequireHealthf)) {
            t.row();
            t.add(WHStats.format("wh-health-enable-fire", Strings.autoFixed(shootRequireHealthf * 100f, 1) + "%"));
        }

        if (thresholdEnabled(switchBulletHealthf) && lowHealthBullet != null) {
            t.row();
            t.add(WHStats.format("wh-health-switch-bullet", Strings.autoFixed(switchBulletHealthf * 100f, 1) + "%"));
            t.row();
            t.add(Core.bundle.get("stat.wh-health-switch-bullet-list"));
            t.row();
            UIUtils.ammo(ObjectMap.of(u, lowHealthBullet)).display(t);
        }
    }

    public static class HealthStateMount extends WeaponMount {
        public ShootPattern shootWhich;
        public BulletType bulletWhich;
        public boolean lowHealthActive;
        public boolean healthShootAllowed = true;

        public HealthStateMount(Weapon weapon) {
            super(weapon);
        }
    }
}
