package wh.entities.bullet.laser;

import arc.graphics.Color;
import arc.math.Angles;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import wh.graphics.BeamLightning;
import wh.util.WHUtils;
import wh.util.struct.Vec2Seq;

public class BeamLightningDamageBulletType extends BulletType {
    private final Source source;
    private boolean largeHit;

    public BeamLightningDamageBulletType(Source source) {
        this.source = source;
        speed = 0f;
        collides = false;
        hittable = false;
        absorbable = false;
        keepVelocity = false;
        despawnEffect = shootEffect = smokeEffect = Fx.none;
    }

    public Bullet createByAngle(Bullet source, float startX, float startY,
                                float angle, float length, float damage,
                                float growTime, float fadeTime, float lifetime,
                                float width, int segments, float randomDeviation,
                                float fadePoints) {
        if (source == null) return null;

        Vec2Seq points = BeamLightning.createPath(
                startX,
                startY,
                startX + Angles.trnsx(angle, length),
                startY + Angles.trnsy(angle, length),
                source.id, Math.max(1, segments),
                Math.max(0f, randomDeviation)
        );
        return createBeam(source, points, this.source.bulletType().hitColor, damage,
                growTime, fadeTime, lifetime, width, fadePoints);
    }

    public Bullet createBeam(Bullet source, Vec2Seq points, Color lightningColor, float damage, float growTime, float fadeTime,
                             float lifetime, float width, float fadePoints
    ) {
        if (source == null || points == null || points.size() < 2 || damage <= 0f) return null;

        float safeGrowTime = Math.max(0.01f, growTime);
        float safeFadeTime = Math.max(0f, fadeTime);
        float safeLifetime = lifetime > 0.01f
                ? Math.max(lifetime, safeGrowTime + safeFadeTime)
                : safeGrowTime + safeFadeTime;

        BeamLightning.draw(points, lightningColor,
                Math.max(0.1f, width), safeGrowTime, safeFadeTime, safeLifetime,
                Math.max(0.001f, fadePoints));
        return createDamageBullet(source, points, safeGrowTime, safeLifetime, damage);
    }

    public Bullet create(Bullet source, Vec2Seq points, float growTime, float lifetime, float damage) {
        Color lightningColor = this.source.bulletType().lightningColor;
        return createBeam(source, points, lightningColor, damage,
                growTime, lifetime / 2, lifetime, 1.5f, 3f);
    }

    private Bullet createDamageBullet(Bullet source, Vec2Seq points, float growTime, float lifetime, float damage) {
        if (source == null || points == null || points.size() < 2 || damage <= 0f) return null;

        syncSourceProperties();
        this.lifetime = Math.max(0.01f, lifetime);

        BeamLightningDamageBullet bullet = BeamLightningDamageBullet.create();
        bullet.points = points;
        bullet.growTime = Math.max(0.01f, growTime);
        bullet.nextSegment = 0;
        return WHUtils.anyOtherCreate(bullet, this, source.shooter, source, source.team,
                source.x, source.y, source.rotation(), damage, 0f, 1f,
                null, null, -1f, -1f, null);
    }

    @Override
    public void update(Bullet b) {
        if (!(b instanceof BeamLightningDamageBullet data) || data.points == null) {
            b.remove();
            return;
        }

        syncSourceProperties();
        int segmentCount = data.points.size() - 1;
        int complete = Math.min((int) (BeamLightning.progress(b.time, data.growTime) * segmentCount), segmentCount);
        while (data.nextSegment < complete) {
            Vec2 from = data.points.setVec2(data.nextSegment, Tmp.v1);
            Vec2 to = data.points.setVec2(data.nextSegment + 1, Tmp.v2);
            float length = from.dst(to);
            if (length > 0.001f) {
                Damage.collideLine(b, b.team, from.x, from.y,
                        Angles.angle(from.x, from.y, to.x, to.y), length, largeHit, false);
            }
            data.nextSegment++;
        }
    }

    @Override
    public void draw(Bullet b) {
    }

    @Override
    public void drawLight(Bullet b) {
    }

    private void syncSourceProperties() {
        BulletType sourceType = source.bulletType();
        collides = sourceType.collides;
        collidesAir = sourceType.collidesAir;
        collidesGround = sourceType.collidesGround;
        collidesTiles = sourceType.collidesTiles;
        collidesTeam = sourceType.collidesTeam;
        largeHit = source.largeHit();
        buildingDamageMultiplier = sourceType.buildingDamageMultiplier;
        shieldDamageMultiplier = sourceType.shieldDamageMultiplier;
        pierce = sourceType.pierce;
        pierceBuilding = sourceType.pierceBuilding;
        pierceCap = sourceType.pierceCap;
        removeAfterPierce = sourceType.removeAfterPierce;
        status = sourceType.status;
        statusDuration = sourceType.statusDuration;
        hitEffect = sourceType.hitEffect;
        hitColor = sourceType.hitColor;
    }

    public interface Source {
        BulletType bulletType();

        default boolean largeHit() {
            return false;
        }
    }

    public static class BeamLightningDamageBullet extends Bullet {
        public Vec2Seq points;
        public float growTime;
        public int nextSegment;

        @Override
        public void reset() {
            super.reset();
            points = null;
            growTime = 0f;
            nextSegment = 0;
        }

        public static BeamLightningDamageBullet create() {
            return Pools.obtain(BeamLightningDamageBullet.class, BeamLightningDamageBullet::new);
        }
    }
}