//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.entities.bullet.laser;

import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import mindustry.gen.Hitboxc;
import mindustry.gen.Sounds;
import wh.content.WHFx;
import wh.graphics.PositionLightning;

public class PositionLightningBulletType extends BulletType {
    public int boltNum = 2;
    public float hitEffectRotation = 12f;

    public PositionLightningBulletType() {
        speed = 0f;
        scaleLife = true;
        hitShake = 2f;
        hitSound = Sounds.spark;
        absorbable = keepVelocity = false;
        instantDisappear = true;
        collides = false;
        collidesAir = collidesGround = true;
        hitEffect = shootEffect = smokeEffect = WHFx.boolSelector;
        despawnEffect = Fx.none;
    }

    public PositionLightningBulletType(float damage) {
        this.damage = damage;

        speed = 0f;
        scaleLife = true;
        hitShake = 2f;
        hitSound = Sounds.spark;
        absorbable = keepVelocity = false;
        instantDisappear = true;
        collides = false;
        collidesAir = collidesGround = true;
        hitEffect = shootEffect = smokeEffect = WHFx.boolSelector;
        despawnEffect = Fx.none;
    }

    @Override
    public void init() {
        super.init();
        drawSize = Math.max(drawSize, maxRange * 2);
        if (hitEffect == WHFx.boolSelector) hitEffect = WHFx.lightningHitLarge(lightningColor);
        if (smokeEffect == WHFx.boolSelector) smokeEffect = Fx.shootBigSmoke;
        if (shootEffect == WHFx.boolSelector) shootEffect = WHFx.shootLineSmall(lightningColor);
    }

    public float range() {
        return maxRange;
    }

    @Override
    public void init(Bullet b) {
        float length = b.lifetime * range() / lifetime;

        Healthc target = Damage.linecast(b, b.x, b.y, b.rotation(), length + 4f);
        b.data = target;

        if (target instanceof Hitboxc hit) {
            b.collision(hit, hit.x(), hit.y());
        } else if (target instanceof Building tile) {
            if (tile.collide(b)) {
                tile.collision(b);
                hit(b, tile.x, tile.y);
            }
        }
        PositionLightning.createLength(b, b.team, b, length, b.rotation(), lightningColor, true, 0, 0, PositionLightning.WIDTH, boltNum, p -> {
            hitEffect.at(p.getX(), p.getY(), hitEffectRotation, hitColor);
            Effect.shake(hitShake, hitShake, p);
        });
        super.init(b);
    }

    @Override
    public void despawned(Bullet b) {
        despawnEffect.at(b.x, b.y, b.rotation(), lightningColor);
    }

    @Override
    public void hit(Bullet b) {
    }

    @Override
    public void hit(Bullet b, float x, float y) {
    }

    @Override
    public void draw(Bullet b) {
    }

    @Override
    public void drawLight(Bullet b) {
    }
}
