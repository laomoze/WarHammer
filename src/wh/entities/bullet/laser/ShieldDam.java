package wh.entities.bullet.laser;

import mindustry.gen.Bullet;

public interface ShieldDam {
    boolean canDamageShield();

    default void setCanCreateSplashDamage(Bullet bullet, boolean canCreateSplashDamage) {
    }

    default float shieldDamageMultiplier() {
        return 1f;
    }
}
