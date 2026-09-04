package wh.entities.bullet.laser;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Intersector;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import wh.content.WHFx;
import wh.core.WHSettings;
import wh.graphics.Drawn;
import wh.graphics.PositionLightning;
import wh.graphics.WHPal;
import wh.util.WHUtils;

import static wh.graphics.Drawn.rand;

public class LightningBeamBulletType extends ContinuousLaserBulletType {
    private static final int MAX_LINKED_BULLETS = 50;
    private static final float END_JITTER = 14f;
    private static final float SIDE_ANGLE = 90f;
    private static final Rect beamRect = new Rect();
    private static final Rect searchRect = new Rect();

    public Color[] colors = {WHPal.SkyBlueF.cpy(), WHPal.SkyBlueF.cpy().a(0.5f), WHPal.SkyBlueF.cpy().a(0.3f), Pal.coalBlack};

    public float growTime = 30;
    public float flashFadeInTime = 20f;
    public int segments = 14;

    private static final Effect lightningEffect = new Effect(60, 1200f, e -> {
        if (!(e.data instanceof LightningFlash flash)) return;
        if (!WHSettings.effectEnabled()) return;

        LightningBeamBulletType type = flash.type;
        float fadeStart = Math.max(type.growTime, type.flashFadeInTime);
        e.lifetime = fadeStart + type.fadeTime;

        float progress = Interp.pow2Out.apply(Mathf.clamp(e.time / type.growTime));
        float fadeIn = type.flashFadeInTime <= 0.001f ? 1f :
                Interp.pow2Out.apply(Mathf.clamp(e.time / type.flashFadeInTime));
        float thin = type.fadeTime <= 0.001f ? 1f :
                1f - Interp.smooth.apply(Mathf.clamp((e.time - fadeStart) / type.fadeTime));
        if (progress <= 0.001f || fadeIn <= 0.001f || thin <= 0.001f) return;

        Draw.z(Layer.bullet);
        type.drawFlash(flash.points, progress, fadeIn,
                type.lightningWidth * 2.7f * thin, type.lightningColor, Tmp.v3);
        type.drawFlash(flash.points, progress, fadeIn,
                type.lightningWidth * thin, type.lightningColor, Tmp.v3);
        type.drawFlash(flash.points, progress, fadeIn,
                type.lightningWidth * 0.42f * thin, Color.white, Tmp.v3);

        if (type.drawPositionLighting) {
            Drawf.light(flash.points[0], flash.points[1], Tmp.v3.x, Tmp.v3.y,
                    type.lightningWidth * 1.25f * thin, type.lightningColor, 0.25f);
        }
        Draw.reset();
    });

    public float beamShockRadius = 20;
    public float sideDamage = 100;
    public float sideDamageMultiplier = 0.15f;
    public float sideDamageInterval = 20f;
    public float linkDamage = 100f;
    public float beamLinkOffset = 6f;

    public float linkRange = 120f;
    public int linkAmounts = 5;
    public int extraLinkAmounts = 2;
    public float linkInterval = 30f;

    public float oscScl = 4f, oscMag = 0.15f;

    public float flashInterval = 20f;

    public float randomDeviation = 18f;
    public float lightningWidth = 1.5f;
    public boolean drawPositionLighting = true;

    private BeamLightningLinkerBulletType linkBulletType;

    public LightningBeamBulletType() {
        speed = 0f;
        collides = false;
        collidesAir = collidesGround = true;
        collidesTiles = true;
        hittable = false;
        absorbable = false;
        pierce = true;
        pierceCap = 1;
        removeAfterPierce = false;
        despawnEffect = Fx.none;
        shootEffect = smokeEffect = Fx.none;
        laserAbsorb = true;
        largeHit = true;
        status = StatusEffects.shocked;
        statusDuration = 30f;

        width = 13;
    }

    public LightningBeamBulletType(float damage) {
        this();
        this.damage = damage;
    }

    @Override
    public void init() {
        super.init();
        beamShockRadius = Math.max(0f, beamShockRadius);
        sideDamageInterval = Math.max(1f, sideDamageInterval);
        beamLinkOffset = Math.max(0f, beamLinkOffset);
        linkRange = Math.max(1f, linkRange);
        linkAmounts = Math.max(0, linkAmounts);
        linkInterval = Math.max(1f, linkInterval);
        growTime = Math.max(0.01f, growTime);
        flashFadeInTime = Math.max(0f, flashFadeInTime);
        segments = Math.max(1, segments);
        randomDeviation = Math.max(0f, randomDeviation);
        lightningWidth = Math.max(0.1f, lightningWidth);
        drawSize = Math.max(drawSize, length * 2f + beamShockRadius * 2f);
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        if (!(b instanceof LightningBeamBullet beam)) return;

        beam.baseDamage = b.damage;
        beam.damageScale = 1f;
        beam.linkedBullets.clear();
        updateBeamEnd(b, beam);
    }

    @Override
    public void update(Bullet b) {
        if (!(b instanceof LightningBeamBullet beam)) return;

        b.damage = beam.baseDamage * beam.damageScale;
        updateBeamEnd(b, beam);
        super.update(b);
        if (!b.isAdded()) return;
        setBeamEnd(b, beam.beamLength);

        if (b.timer(0, flashInterval)) emitLightning(b, beam);
        if (b.timer(3, sideDamageInterval)) damageAroundBeam(b);
        if (b.timer(4, damageInterval)) createHitChains(b);
        if (b.timer(5, 1f)) checkFriendlyBullets(b, beam);
    }

    @Override
    public float currentLength(Bullet b) {
        return b instanceof LightningBeamBullet beam ? beam.beamLength : length;
    }

    @Override
    public void draw(Bullet b) {
        if (!(b instanceof LightningBeamBullet beam) || beam.beamLength <= 0.1f) return;

        float fade = Mathf.curve(b.fin(Interp.pow2Out), 0f, 0.2f)
                * Mathf.curve(b.fout(Interp.smooth), 0f, 0.1f);
        float rotation = b.rotation();
        float endX = b.x + Angles.trnsx(rotation, beam.beamLength);
        float endY = b.y + Angles.trnsy(rotation, beam.beamLength);
        float pulse = 1f - oscMag + Mathf.absin(Time.time, oscScl, oscMag);
        float layerWidth = width * fade * pulse;

        for (int i = 0; i < colors.length; i++) {
            Color color = colors[i];
            float startHalf = layerWidth * 0.5f;
            float endHalf = startHalf * 0.75f;
            float startColor = Tmp.c1.set(color).a(color.a * fade).toFloatBits();
            float endColor = Tmp.c2.set(color).a(color.a * 0f * fade).toFloatBits();

            Draw.z(Layer.bullet - 0.001f + i * 0.0009f);
            Fill.quad(
                    b.x + Angles.trnsx(rotation + SIDE_ANGLE, startHalf),
                    b.y + Angles.trnsy(rotation + SIDE_ANGLE, startHalf), startColor,
                    b.x - Angles.trnsx(rotation + SIDE_ANGLE, startHalf),
                    b.y - Angles.trnsy(rotation + SIDE_ANGLE, startHalf), startColor,
                    endX - Angles.trnsx(rotation + SIDE_ANGLE, endHalf),
                    endY - Angles.trnsy(rotation + SIDE_ANGLE, endHalf), endColor,
                    endX + Angles.trnsx(rotation + SIDE_ANGLE, endHalf),
                    endY + Angles.trnsy(rotation + SIDE_ANGLE, endHalf), endColor
            );

            for (int sign : Mathf.signs) {
                Draw.color(startColor);
                Drawf.tri(b.x, b.y, startHalf * 1.5f,
                        startHalf * 4f, rotation + SIDE_ANGLE * sign);
            }

            layerWidth *= 0.7f;
        }

        Draw.reset();
        Drawf.light(b.x, b.y, endX, endY, width * 1.2f * fade, colors[0], 0.6f);
        Draw.reset();
    }

    private void updateBeamEnd(Bullet b, LightningBeamBullet beam) {
        beam.beamLength = Damage.findLength(b, length, laserAbsorb, pierceCap);
        setBeamEnd(b, beam.beamLength);
    }

    private void setBeamEnd(Bullet b, float beamLength) {
        b.aimX = b.x + Angles.trnsx(b.rotation(), beamLength);
        b.aimY = b.y + Angles.trnsy(b.rotation(), beamLength);
        b.fdata = beamLength;
    }

    private void emitLightning(Bullet b, LightningBeamBullet beam) {
        if (beam.beamLength <= 0.01f) return;

        rand.setSeed(b.id);
        float endAngle = rand.random(360f);
        float endDistance = rand.random(END_JITTER);
        float endX = beamEndX(b);
        float endY = beamEndY(b);
        Tmp.v1.set(endX + Angles.trnsx(endAngle, endDistance),
                endY + Angles.trnsy(endAngle, endDistance));
        LightningFlash flash = new LightningFlash(this, b.x, b.y,
                Tmp.v1.x, Tmp.v1.y, b.id + (long) beam.emission++ * 7919L);
        lightningEffect.at(b.x, b.y, b.rotation(), lightningColor, flash);
        createPathSparks(flash.points, b.id + (long) beam.emission++ * 7919L);
        if (hitEffect != null && hitEffect != Fx.none) {
            hitEffect.at(Tmp.v1.x, Tmp.v1.y, b.rotation(), hitColor);
        }
    }

    private void damageAroundBeam(Bullet b) {
        if (beamShockRadius <= 0.01f) return;

        setBeamRect(beamRect, b, linkRange + 8f);
        float amount = sideDamage < 0f ? b.damage * sideDamageMultiplier : sideDamage * b.damageMultiplier();
        if (amount <= 0f) return;

        Seq<Unit> hitUnits = new Seq<>();
        Units.nearbyEnemies(b.team, beamRect, unit -> {
            if (unit.dead || !unit.hittable()
                    || !unit.checkTarget(collidesAir, collidesGround)
                    || Intersector.distanceSegmentPoint(b.x, b.y, beamEndX(b), beamEndY(b), unit.x, unit.y)
                    > linkRange + unit.hitSize / 2f) return;
            hitUnits.add(unit);
        });

        hitUnits.shuffle();
        hitUnits.truncate(10);

        for (Unit unit : hitUnits) {
            unit.damage(amount);
            applyStatus(unit, b);
            setBeamPoint(b, unit.x, unit.y);
            PositionLightning.createEffect(Tmp.v1, unit, lightningColor, 2, PositionLightning.WIDTH);
            WHFx.lightningHitSmall.at(unit.x, unit.y, Math.max(4f, beamShockRadius), lightningColor);
        }
    }

    private void createPathSparks(float[] points, long seed) {
        if (!WHSettings.effectEnabled()) return;

        Rand random = new Rand(seed);
        float width = lightningWidth;
        for (int i = 0; i < points.length; i += 2) {
            if (random.chance(0.0855f) && i != points.length - 1) {
                WHFx.lightningSpark.at(points[i], points[i + 1],
                        random.random(2f + width, 4f + width), lightningColor);
            }
        }
    }

    private void createHitChains(Bullet b) {
        if (linkAmounts <= 0) return;

        setBeamRect(searchRect, b, beamShockRadius + 8f);
        Seq<Unit> hitUnits = new Seq<>();
        Units.nearbyEnemies(b.team, searchRect, unit -> {
            if (canShock(b, unit)) hitUnits.add(unit);
        });
        int count = Math.min(linkAmounts, hitUnits.size);
        for (int i = 0; i < count; i++) {
            Unit unit = hitUnits.get(i);
            setBeamPoint(b, unit.x, unit.y);
            createLink(b, Tmp.v1.x, Tmp.v1.y, unit,
                    Math.max(0f, linkDamage * b.damageMultiplier()));
        }
    }

    private void createLink(Bullet source, float fromX, float fromY, Unit target, float damage) {
        Tmp.v1.set(fromX, fromY);
        PositionLightning.create(source, source.team, Tmp.v1, target,
                source.type.lightningColor, false, damage, 0,
                PositionLightning.WIDTH, 2, point -> {
                    applyStatus(target, source);
                    if (source.type.hitEffect != null && source.type.hitEffect != Fx.none) {
                        source.type.hitEffect.at(point.getX(), point.getY(), 0f, source.type.hitColor);
                    }
                });
    }

    private void createChain(Bullet source, float x, float y) {
        if (extraLinkAmounts <= 0) return;

        float amount = Mathf.clamp((source.damage() + source.type.splashDamage) / 20 * Math.min(1, source.damageMultiplier()), 35, 150);
        Seq<Unit> targets = new Seq<>();
        Units.nearbyEnemies(source.team, x, y, linkRange, unit -> {
            if (!unit.dead && unit.hittable()
                    && unit.checkTarget(source.type.collidesAir, source.type.collidesGround)
                    && Mathf.dst2(x, y, unit.x, unit.y) <= linkRange * linkRange) {
                targets.add(unit);
            }
        });
        int count = Math.min(extraLinkAmounts, targets.size);
        for (int i = 0; i < count; i++) {
            createLink(source, x, y, targets.get(i), amount);
        }
    }

    private boolean canShock(Bullet beam, Unit unit) {
        float endX = beamEndX(beam);
        float endY = beamEndY(beam);
        return !unit.dead && unit.hittable()
                && unit.checkTarget(collidesAir, collidesGround)
                && Intersector.distanceSegmentPoint(beam.x, beam.y, endX, endY, unit.x, unit.y)
                <= beamShockRadius + unit.hitSize / 2f;
    }

    private void setBeamRect(Rect rect, Bullet beam, float margin) {
        float endX = beamEndX(beam);
        float endY = beamEndY(beam);
        rect.set(Math.min(beam.x, endX) - margin, Math.min(beam.y, endY) - margin,
                Math.abs(endX - beam.x) + margin * 2f,
                Math.abs(endY - beam.y) + margin * 2f);
    }

    private float beamEndX(Bullet beam) {
        return beam.x + Angles.trnsx(beam.rotation(), beam.fdata);
    }

    private float beamEndY(Bullet beam) {
        return beam.y + Angles.trnsy(beam.rotation(), beam.fdata);
    }

    private void setBeamPoint(Bullet beam, float x, float y) {
        float endX = beamEndX(beam);
        float endY = beamEndY(beam);
        float dx = endX - beam.x;
        float dy = endY - beam.y;
        float length2 = dx * dx + dy * dy;
        float progress = length2 <= 0.001f ? 0f :
                Mathf.clamp(((x - beam.x) * dx + (y - beam.y) * dy) / length2);
        float offset = Mathf.range(beamLinkOffset);
        float angle = beam.rotation() + 90f;
        Tmp.v1.set(beam.x + dx * progress + Angles.trnsx(angle, offset),
                beam.y + dy * progress + Angles.trnsy(angle, offset));
    }

    private void checkFriendlyBullets(Bullet beam, LightningBeamBullet data) {
        if (data.linkedBullets.size >= MAX_LINKED_BULLETS) return;

        setBeamRect(beamRect, beam, beamShockRadius + 8f);
        float endX = beamEndX(beam);
        float endY = beamEndY(beam);
        Groups.bullet.intersect(beamRect.x, beamRect.y, beamRect.width, beamRect.height, other -> {
            if (other == beam || other.team != beam.team || other.type == null || !other.type.collides
                    || !other.isAdded() || data.linkedBullets.contains(other)
                    || data.linkedBullets.size >= MAX_LINKED_BULLETS) return;

            float radius = beamShockRadius + Math.max(2f, other.hitSize());
            boolean near = Intersector.distanceSegmentPoint(
                    beam.x, beam.y, endX, endY, other.x, other.y) <= radius;
            boolean crossed = Intersector.intersectSegments(
                    beam.x, beam.y, endX, endY,
                    other.lastX(), other.lastY(), other.x, other.y, Tmp.v2);
            if (!near && !crossed) return;

            float linkX = crossed ? Tmp.v2.x : other.x;
            float linkY = crossed ? Tmp.v2.y : other.y;
            if (spawnLinkBullet(other, linkX, linkY)) data.linkedBullets.add(other);
        });
    }

    private boolean spawnLinkBullet(Bullet source, float x, float y) {
        if (linkBulletType == null) linkBulletType = new BeamLightningLinkerBulletType();
        Bullet linker = linkBulletType.create(source, source.shooter, source.team, x, y,
                0f, 0f, 0f, 1f, new LinkData(source), null, 0f, 0f, null);
        linkBulletType.hitColor = source.type.hitColor;
        return linker != null;
    }

    private void drawFlash(float[] points, float progress, float alpha, float stroke,
                           Color color, Vec2 end) {
        if (alpha <= 0.001f || stroke <= 0.001f) return;

        //[x1,y1,x2,y2...]
        int segmentCount = points.length / 2 - 1;
        float shown = Mathf.clamp(progress) * segmentCount;
        int complete = Math.min((int) shown, segmentCount);
        float partial = shown - complete;
        float fadePoints = 3f;


        for (int i = 0; i < complete; i++) {
            int point = i * 2;

            float tail = Mathf.clamp((shown - (i + 1f)) / fadePoints);
            float currentStroke = stroke * Mathf.lerp(0.2f, 1f, tail);

            Draw.color(color);
            Lines.stroke(currentStroke);
            /*  Draw.color(color, alpha * Mathf.lerp(0.2f, 1f, tail));*/
            Lines.line(points[point], points[point + 1],
                    points[point + 2], points[point + 3], false);

            Fill.circle(points[point], points[point + 1], currentStroke / 2);
        }


        int current = complete * 2;
        if (complete < segmentCount && partial > 0.001f) {
            float endX = Mathf.lerp(points[current], points[current + 2], partial);
            float endY = Mathf.lerp(points[current + 1], points[current + 3], partial);
            float tail = Mathf.clamp((shown - (complete + 1f)) / fadePoints);
            float currentStroke = stroke * Mathf.lerp(0.2f, 1f, tail);

            Draw.color(color);
            Lines.stroke(currentStroke);
            Lines.line(points[current], points[current + 1], endX, endY, false);
            Fill.circle(endX, endY, currentStroke * 0.5f);
            end.set(endX, endY);
        } else {
            end.set(points[current], points[current + 1]);
        }
    }

    private float[] createLightningPath(float startX, float startY,
                                        float endX, float endY, long seed) {
        float[] points = new float[(segments + 1) * 2];
        float angle = Angles.angle(startX, startY, endX, endY) - 90f;
        Rand random = new Rand(seed);

        for (int i = 0; i <= segments; i++) {
            float progress = i / (float) segments;
            float offset = i == 0 || i == segments ? 0f : random.range(randomDeviation);
            points[i * 2] = Mathf.lerp(startX, endX, progress) + Angles.trnsx(angle, offset);
            points[i * 2 + 1] = Mathf.lerp(startY, endY, progress) + Angles.trnsy(angle, offset);
        }
        return points;
    }

    private void applyStatus(Unit unit, Bullet source) {
        if (source.type.status != null && source.type.status != StatusEffects.none
                && source.type.statusDuration > 0f) {
            unit.apply(source.type.status, source.type.statusDuration);
        }
    }

    private class BeamLightningLinkerBulletType extends BasicBulletType {
        BeamLightningLinkerBulletType() {
            speed = 0f;
            lifetime = Float.MAX_VALUE;
            damage = 0f;
            collides = false;
            hittable = false;
            absorbable = false;
            shootEffect = smokeEffect = hitEffect = despawnEffect = Fx.none;
        }

        @Override
        public void update(Bullet b) {
            if (!(b.data instanceof LinkData data) || data.parent == null || !data.parent.isAdded()) {
                b.hit = true;
                b.remove();
                return;
            }

            Vec2 v = new Vec2().set(b);
            if (b.timer(1, 15) && Mathf.chanceDelta(0.2f)) {
                for (int j = 0; j < 2; ++j) {
                    rand.setSeed(b.id);
                    Drawn.randFadeLightningEffect(v.x, v.y, rand.random(20, 30), Mathf.random(7, 10), hitColor, Mathf.chance(0.5));
                }
            }

            Bullet parent = data.parent;
            b.set(parent);
            if (b.timer(0, linkInterval)) createChain(parent, b.x, b.y);
        }

        @Override
        public void draw(Bullet b) {
        }

        @Override
        public void drawLight(Bullet b) {
        }
    }

    private static class LinkData {
        final Bullet parent;

        LinkData(Bullet parent) {
            this.parent = parent;
        }
    }

    private static class LightningFlash {
        final LightningBeamBulletType type;
        final float[] points;

        LightningFlash(LightningBeamBulletType type, float startX, float startY,
                       float endX, float endY, long seed) {
            this.type = type;
            points = type.createLightningPath(startX, startY, endX, endY, seed);
        }
    }

    @Override
    public @Nullable Bullet create(@Nullable Entityc owner, @Nullable Entityc shooter, Team team,
                                   float x, float y, float angle, float damage, float velocityScl,
                                   float lifetimeScl, Object data, @Nullable Mover mover,
                                   float aimX, float aimY, @Nullable Teamc target) {
        LightningBeamBullet bullet = LightningBeamBullet.create();
        return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage,
                velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
    }

    public static class LightningBeamBullet extends Bullet {
        public float beamLength;
        public float baseDamage;
        public float damageScale = 1f;
        public int emission;
        public final Seq<Bullet> linkedBullets = new Seq<>();

        @Override
        public void reset() {
            super.reset();
            beamLength = baseDamage = 0f;
            damageScale = 1f;
            emission = 0;
            linkedBullets.clear();
        }

        public static LightningBeamBullet create() {
            return Pools.obtain(LightningBeamBullet.class, LightningBeamBullet::new);
        }
    }
}
