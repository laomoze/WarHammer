package wh.entities.world.blocks.defense;

import arc.func.Cons;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.util.Time;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.world.blocks.defense.ForceProjector;
import wh.entities.bullet.laser.ShieldDam;

public class WHForceProjector extends ForceProjector {
    public int timer = timers++;

    public WHForceProjector(String name) {
        super(name);
    }

    protected static WHForceProjector paramBlock;
    protected static ForceBuild paramEntity;

    protected static final Cons<Bullet> shieldConsumer = bullet -> {
        if (bullet.team != paramEntity.team && !bullet.absorbed && Intersector.isInRegularPolygon(paramBlock.sides, paramEntity.x, paramEntity.y, paramEntity.realRadius(), paramBlock.shieldRotation, bullet.x, bullet.y)) {
            if (bullet.type instanceof ShieldDam shieldDam && shieldDam.canDamageShield()) {
                paramEntity.hit = 1f;
                if (paramEntity.timer(paramBlock.timer, 60)) {
                    paramBlock.hitSound.at(bullet.x, bullet.y, 1f + Mathf.range(0.1f), paramBlock.hitSoundVolume);
                }
                shieldDam.setCanCreateSplashDamage(bullet, false);
                if (bullet.type instanceof ContinuousBulletType c) {
                    paramEntity.buildup += (bullet.damage * (60 / c.damageInterval)) / 60 * shieldDam.shieldDamageMultiplier() * Time.delta;
                } else {
                    paramEntity.buildup += bullet.damage * shieldDam.shieldDamageMultiplier();
                }
                return;
            }

            if (!bullet.type.absorbable) return;

            bullet.absorb();
            paramBlock.hitSound.at(bullet.x, bullet.y, 1f + Mathf.range(0.1f), paramBlock.hitSoundVolume);
            paramBlock.absorbEffect.at(bullet);
            paramEntity.hit = 1f;
            paramEntity.buildup += bullet.type.shieldDamage(bullet);

        }
    };

    public class WHForceBuild extends ForceBuild {
        @Override
        public void updateTile() {
            super.updateTile();
        }

        @Override
        public void deflectBullets() {
            float realRadius = realRadius();

            if (realRadius > 0 && !broken) {
                paramBlock = WHForceProjector.this;
                paramEntity = this;
                Groups.bullet.intersect(x - realRadius, y - realRadius, realRadius * 2f, realRadius * 2f, shieldConsumer);
            }
        }
    }
}
