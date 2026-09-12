package wh.entities.bullet.laser;

import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.gen.Bullet;
import wh.graphics.BeamLightning;
import wh.util.struct.Vec2Seq;

import static mindustry.entities.Damage.findPierceLength;

public class DirectedLightningBulletType extends LaserBulletType implements BeamLightningDamageBulletType.Source {
    public BeamLightningDamageBulletType lightningDamageBulletType;
    public float LightingAmounts = 2;
    public float lightingLife = 60;
    public float glowTime = 30, fadeTime = 10;
    public int segments = 15;
    public float fadePoints = 6f;

    public DirectedLightningBulletType() {

    }

    @Override
    public void draw(Bullet b) {
    }

    @Override
    public void init(Bullet b) {
        b.fdata = findPierceLength(b, pierceCap, laserAbsorb, length);
        createBeamLightning(b, b.damage);
    }

    protected void createBeamLightning(Bullet b, float sourceDamage) {
        if (b.fdata <= 0.01f) return;

        float damageAmount = sourceDamage * b.damageMultiplier();
        if (damageAmount <= 0f) return;

        if (lightningDamageBulletType == null) {
            lightningDamageBulletType = new BeamLightningDamageBulletType(this);
        }
        for (float i = 0; i <= LightingAmounts; i += 1) {
            Tmp.v4.trns(b.rotation(), b.fdata).add(b.x, b.y);
            Vec2Seq points = BeamLightning.createPath(
                    b.x, b.y, Tmp.v4.x, Tmp.v4.y, (long) (b.id + i), segments, width
            );
            lightningDamageBulletType.createBeam(
                    b, points, lightningColor, damageAmount, glowTime, fadeTime, lightingLife, 1.5f, fadePoints);
        }
    }

    @Override
    public BulletType bulletType() {
        return this;
    }
}
