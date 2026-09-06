package wh.entities.bullet.laser;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import wh.graphics.BeamLightning;
import wh.graphics.MainRenderer;
import wh.graphics.PositionLightning;
import wh.util.struct.Vec2Seq;

public class LightingLaserBulletType extends LaserBulletType implements BeamLightningDamageBulletType.Source {
    public Color lightningColor = Pal.lancerLaser;
    public BeamLightningDamageBulletType lightningDamageBulletType;

    public boolean renderingDistortion = false;
    public float pathDistortionRectWidthScale = 1.5f;
    public float pathDistortionRectStrength = 0.3f;

    public LightingLaserBulletType(float damage) {
        super(damage);
    }

    public LightingLaserBulletType() {
    }

    public void addRectPathDistortion(Bullet b, float rot) {
        if (renderingDistortion) {
            float rectLength = b.fdata;
            float rectWidth = Math.max(width * pathDistortionRectWidthScale, 1f);

            Tmp.v2.trns(rot, -width);
            Tmp.v3.trns(rot, rectLength * 0.5f + width / 2);
            MainRenderer.addShockRect(
                    b.x + Tmp.v2.x + Tmp.v3.x, b.y + Tmp.v2.y + Tmp.v3.y,
                    rectLength, rectWidth, rot, Math.max(lifetime, 60), pathDistortionRectStrength);
        }
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        PositionLightning.createEffect(b, b.fdata * 0.95f, b.rotation(), lightningColor, 3, Mathf.random(2, 3));
        addRectPathDistortion(b, b.rotation());
    }

    protected void createBeamLightning(Bullet b) {
        createBeamLightning(b, b.damage, 2);
    }

    protected void createBeamLightning(Bullet b, float sourceDamage, float amount) {
        if (b.fdata <= 0.01f) return;

        float damageAmount = sourceDamage * b.damageMultiplier();
        if (damageAmount <= 0f) return;

        if (lightningDamageBulletType == null) {
            lightningDamageBulletType = new BeamLightningDamageBulletType(this);
        }
        for (float i = 0; i <= amount; i += 1) {
            Tmp.v4.trns(b.rotation(), b.fdata).add(b.x, b.y);
            Vec2Seq points = BeamLightning.createPath(
                    b.x, b.y, Tmp.v4.x, Tmp.v4.y, (long) (b.id + i), 15, width * 1.2f
            );
            lightningDamageBulletType.createBeam(
                    b, points, lightningColor, damageAmount, 15f, 12f, 40f, 1.5f, 5f);
        }
    }

    @Override
    public void removed(Bullet b) {
        if (b.frags == 0 && fragOnDespawn && fragBullet != null) {
            createFrags(b, b.x, b.y);
        }
    }

    @Override
    public BulletType bulletType() {
        return this;
    }
}
