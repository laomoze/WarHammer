package wh.entities.bullet.laser;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.bullet.PointLaserBulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.blocks.environment.Floor;
import wh.util.WHUtils;

import static mindustry.Vars.state;

public class PointLaserBeamBulletType extends PointLaserBulletType {
    public Color[] colors = {Pal.lancerLaser.cpy().a(0.35f), Pal.lancerLaser.cpy().a(0.7f), Color.white};
    public float length = 99999;
    public float extensionSpeed = 12f;
    public float width = 15f;
    public float lengthFalloff = 0.8f;
    public float oscScl = 2f, oscMag = 0.15f;
    public float fadeTime = 10f;
    public float sideAngle = 90f;
    public boolean drawPositionLighting = false;

    public PointLaserBeamBulletType() {
        speed = 0f;
        collides = false;
        hittable = false;
        absorbable = false;
        despawnEffect = Fx.none;
        shootEffect = smokeEffect = Fx.none;
        laserAbsorb = true;
        range = length;
        drawSize = 1000f;
    }

    public PointLaserBeamBulletType(float damage) {
        this();
        this.damage = damage;
    }

    @Override
    protected float calculateRange() {
        return Math.max(length, rangeOverride > 0f ? rangeOverride : 0f);
    }

    @Override
    public void init() {
        super.init();
        range = calculateRange();
        drawSize = Math.max(drawSize, length * 2f);
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        if (!(b instanceof PointLaserBeamBullet data)) return;

        data.startX = b.x;
        data.startY = b.y;
        float aimLength = b.aimX < 0f || b.aimY < 0f ? 0f : Mathf.dst(data.startX, data.startY, b.aimX, b.aimY);
        data.startLength = length > 0f ? Math.min(length, aimLength) : aimLength;
        data.currentLength = data.startLength;
        data.baseDamage = b.damage;
        data.damageScale = 1f;
        data.blocked = false;
        if (data.startLength <= 0f) {
            b.aimX = data.startX;
            b.aimY = data.startY;
        }
        b.fdata = 0f;
    }

    @Override
    public float continuousDamage() {
        return (damage + splashDamage) / damageInterval * 60f;
    }

    @Override
    public float damageMultiplier(Bullet b) {
        return super.damageMultiplier(b) * (b instanceof PointLaserBeamBullet data ? data.damageScale : 1f);
    }

    @Override
    public void update(Bullet b) {
        if (!(b instanceof PointLaserBeamBullet data)) return;

        data.startX = b.x;
        data.startY = b.y;
        float rotation = b.rotation();
        float wantedLength = data.currentLength + Math.max(extensionSpeed, 0f) * Time.delta;

        float blockedLength = findAbsorberLength(b, data.startX, data.startY, rotation, wantedLength);
        data.blocked = blockedLength < wantedLength - 0.001f;
        data.currentLength = Math.min(wantedLength, blockedLength);

        float endX = data.startX + Angles.trnsx(rotation, data.currentLength);
        float endY = data.startY + Angles.trnsy(rotation, data.currentLength);
        b.aimX = endX;
        b.aimY = endY;
        b.fdata = data.currentLength;

        updateTrail(b);
        updateTrailEffects(b);
        updateBulletInterval(b);

        b.damage = data.baseDamage * data.damageScale;

        if (b.keepAlive && b.timer.get(0, damageInterval)) {
            Damage.collidePoint(b, b.team, hitEffect, endX, endY);
            createSplashDamage(b, endX, endY);
        }

        Floor floor = Vars.world.floorWorld(endX, endY);
        if (b.keepAlive && b.timer.get(1, beamEffectInterval)) {
            if (floor != null)
                beamEffect.at(endX, endY, b.rotation(), floor.mapColor.cpy().lerp(Color.black, 0.35f));
        }

        if (shake > 0f) Effect.shake(shake, shake, b);
    }

    @Override
    public void draw(Bullet b) {
        if (!(b instanceof PointLaserBeamBullet data)) return;

        float rotation = b.rotation();
        float endX = data.startX + Angles.trnsx(rotation, data.currentLength);
        float endY = data.startY + Angles.trnsy(rotation, data.currentLength);
        float fade = Mathf.clamp(b.time > b.lifetime - fadeTime ?
                1f - (b.time - (b.lifetime - fadeTime)) / fadeTime : 1f);
        float pulse = 1f - oscMag + Mathf.absin(Time.time, oscScl, oscMag);
        float layerWidth = width;

        for (int i = 0; i < colors.length; i++) {
            Color color = colors[i];
            float startWidth = layerWidth * fade * pulse;
            float startHalf = startWidth * 0.5f;
            float endWidth = startHalf * 0.75f;
            float startColor = Tmp.c1.set(color).a(color.a * fade * pulse).toFloatBits();
            float endColor = Tmp.c2.set(color).a(0.4f).toFloatBits();

            Draw.z(Layer.bullet - 0.001f + i * 0.0009f);
            Fill.quad(
                    data.startX + Angles.trnsx(rotation + 90f, startHalf),
                    data.startY + Angles.trnsy(rotation + 90f, startHalf),
                    startColor,
                    data.startX - Angles.trnsx(rotation + 90f, startHalf),
                    data.startY - Angles.trnsy(rotation + 90f, startHalf),
                    startColor,
                    endX - Angles.trnsx(rotation + 90f, endWidth),
                    endY - Angles.trnsy(rotation + 90f, endWidth),
                    endColor,
                    endX + Angles.trnsx(rotation + 90f, endWidth),
                    endY + Angles.trnsy(rotation + 90f, endWidth),
                    endColor
            );

            Draw.color(startColor);
            Fill.circle(data.startX, data.startY, startWidth * 1.2f);
            Draw.color(endColor);
            Fill.circle(endX, endY, endWidth * 1.2f);

            layerWidth *= lengthFalloff;
        }

        Draw.reset();

        if (drawPositionLighting && data.currentLength > 0.1f && !state.isPaused()) {
            Drawf.light(data.startX, data.startY, endX, endY, width * 1.2f * b.fout(), colors[0], 0.6f);
        }
        Draw.reset();
    }

    @Override
    public @Nullable Bullet create(
            @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle,
            float damage, float velocityScl, float lifetimeScl, Object data, @Nullable Mover mover,
            float aimX, float aimY, @Nullable Teamc target
    ) {
        PointLaserBeamBullet bullet = PointLaserBeamBullet.create();
        return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl,
                lifetimeScl, data, mover, aimX, aimY, target);
    }

    private float findAbsorberLength(Bullet b, float startX, float startY, float rotation, float maxLength) {
        if (!laserAbsorb || maxLength <= 0f) return maxLength;

        float endX = startX + Angles.trnsx(rotation, maxLength);
        float endY = startY + Angles.trnsy(rotation, maxLength);
        var absorber = Damage.findAbsorber(b.team, startX, startY, endX, endY);
        if (absorber == null) return maxLength;

        absorber.hitbox(Tmp.r1);
        float dx = Angles.trnsx(rotation, 1f);
        float dy = Angles.trnsy(rotation, 1f);
        float near = -Float.MAX_VALUE;
        float far = Float.MAX_VALUE;

        if (Math.abs(dx) < 0.000001f) {
            if (startX < Tmp.r1.x || startX > Tmp.r1.x + Tmp.r1.width) return maxLength;
        } else {
            float tx1 = (Tmp.r1.x - startX) / dx;
            float tx2 = (Tmp.r1.x + Tmp.r1.width - startX) / dx;
            near = Math.max(near, Math.min(tx1, tx2));
            far = Math.min(far, Math.max(tx1, tx2));
        }

        if (Math.abs(dy) < 0.000001f) {
            if (startY < Tmp.r1.y || startY > Tmp.r1.y + Tmp.r1.height) return maxLength;
        } else {
            float ty1 = (Tmp.r1.y - startY) / dy;
            float ty2 = (Tmp.r1.y + Tmp.r1.height - startY) / dy;
            near = Math.max(near, Math.min(ty1, ty2));
            far = Math.min(far, Math.max(ty1, ty2));
        }

        if (near > far || far < 0f) return maxLength;
        return Mathf.clamp(Math.max(0f, near), 0f, maxLength);
    }

    public static class PointLaserBeamBullet extends Bullet {
        public float startX, startY, startLength, currentLength, baseDamage, damageScale;
        public boolean blocked;

        @Override
        public void reset() {
            super.reset();
            startX = startY = startLength = currentLength = baseDamage = 0f;
            damageScale = 1f;
            blocked = false;
        }

        public static PointLaserBeamBullet create() {
            return Pools.obtain(PointLaserBeamBullet.class, PointLaserBeamBullet::new);
        }
    }
}
