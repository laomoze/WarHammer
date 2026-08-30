package wh.entities.bullet.laser;

import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.ForceFieldAbility;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.blocks.defense.ForceProjector;
import wh.entities.abilities.EllipseForceFieldAbility;
import wh.graphics.Drawn;

/**
 * A colliding bullet with a synchronized, ricocheting laser path.
 */
public class RicochetLaserBulletType extends BasicBulletType {
    public Color laserGlowColor = Pal.lancerLaser.cpy().mul(1f, 1f, 1f, 0.4f);
    public Color laserColor = Pal.lancerLaser;
    public Color laserCoreColor = Color.white;
    public float laserWidth = 7f;
    public float laserCoreWidth = 2f;
    public float laserGlowWidth = 1.8f;
    public float fadeInTime = 6f;
    public float fadeOutTime = 8f;
    public float headLength = 14f;
    public int maxBounces = 3;
    public float maxLength = -1f;
    public float bounceOffset = 1f;
    public float forceFieldSearchRange = 512f;
    public boolean bounceOffForceFields = true;
    public boolean bounceOnPierceCap = true;

    public RicochetLaserBulletType(float speed, float damage) {
        super(speed, damage);

        width = 6f;
        height = 8f;
        trailLength = -1;
        pierce = true;
        pierceBuilding = true;
        pierceCap = -1;
        removeAfterPierce = false;
        despawnEffect = Fx.none;
        absorbable = hittable = false;
    }

    public RicochetLaserBulletType(float damage) {
        this(5f, damage);
    }

    public RicochetLaserBulletType() {
        this(5f, 1f);
    }

    @Override
    public void init() {
        super.init();
        drawSize = Math.max(drawSize, speed * lifetime * 2f);
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        b.fdata = 0f;
        b.data = new LaserPath(b.originX, b.originY);
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        if (b.isAdded() && bounceOffForceFields) {
            checkForceFields(b);
        }
        if (b.isAdded() && maxLength > 0f) {
            LaserPath path = path(b);
            if (path != null && pathLength(b, path) >= maxLength) {
                destroy(b);
            }
        }
    }

    @Override
    public void draw(Bullet b) {
        LaserPath path = path(b);
        if (path == null) return;

        float fade = fade(b);
        drawPath(b, path, laserGlowColor, laserWidth * laserGlowWidth, fade);
        drawPath(b, path, laserColor, laserWidth, fade);
        drawPath(b, path, laserCoreColor, laserCoreWidth, fade);

        float headWidth = Math.max(laserWidth, 0f) * fade;
        if (headWidth > 0f) {
            Draw.color(laserColor, laserColor.a * fade);
            Fill.circle(b.x, b.y, headWidth * 0.6f);
            Drawn.tri(b.x, b.y, headWidth / 2f, headLength * fade, b.rotation());
        }

        if (laserCoreWidth > 0f) {
            Draw.color(laserCoreColor, laserCoreColor.a * fade);
            Fill.circle(b.x, b.y, laserCoreWidth * fade * 0.7f);
        }

        Draw.reset();
    }

    private void drawPath(Bullet b, LaserPath path, Color color, float width, float fade) {
        if (width <= 0f || path.points.isEmpty()) return;

        Draw.color(color, color.a * fade);
        Lines.stroke(width * fade);

        Vec2 previous = path.points.first();
        Fill.circle(previous.x, previous.y, width * fade * 0.5f);

        for (int i = 1; i < path.points.size; i++) {
            Vec2 point = path.points.get(i);
            Lines.line(previous.x, previous.y, point.x, point.y, false);
            Fill.circle(point.x, point.y, width * fade * 0.5f);
            previous = point;
        }

        Lines.line(previous.x, previous.y, b.x, b.y, false);
        Fill.circle(b.x, b.y, width * fade * 0.5f);
    }

    @Override
    public void drawLight(Bullet b) {
        LaserPath path = path(b);
        float fade = fade(b);
        if (path == null || fade <= 0f || laserWidth <= 0f || path.points.isEmpty()) return;

        Vec2 previous = path.points.first();
        for (int i = 1; i < path.points.size; i++) {
            Vec2 point = path.points.get(i);
            Drawf.light(previous.x, previous.y, point.x, point.y, laserWidth * 2f * fade, laserColor, 0.35f * fade);
            previous = point;
        }
        Drawf.light(previous.x, previous.y, b.x, b.y, laserWidth * 2f * fade, laserColor, 0.35f * fade);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        super.hitEntity(b, entity, health);
        if (!b.isAdded() || !bounceOnPierceCap || pierceCap < 1) return;

        LaserPath path = path(b);
        if (path == null || b.collided.size < pierceCap) return;

        ricochet(b, b.x, b.y, entity.x(), entity.y(), false);
        if (b.isAdded()) {
            b.collided.clear();
        }
    }

    private void checkForceFields(Bullet b) {
        LaserPath path = path(b);
        if (path == null) return;

        path.forceFieldId = -1;
        float range = Math.max(forceFieldSearchRange, b.hitSize + 8f);
        Cons<Building> buildingConsumer = build -> {
            if (path.forceFieldId != -1 || !(build instanceof ForceProjector.ForceBuild force)) return;
            if (force.team == b.team || force.broken || force.realRadius() <= 1f) return;

            if (!findShieldContact(b, force.x(), force.y(), force.realRadius(), Tmp.v2)) return;

            path.forceFieldId = force.id();
            force.hit = 1f;
            force.buildup += shieldDamage(b);

            ForceProjector projector = (ForceProjector) force.block;
            projector.absorbEffect.at(Tmp.v2.x, Tmp.v2.y);
            projector.hitSound.at(Tmp.v2.x, Tmp.v2.y, 1f + Mathf.range(0.1f), projector.hitSoundVolume);
            ricochet(b, Tmp.v2.x, Tmp.v2.y, force.x(), force.y(), false);
        };

        if (Groups.build.useTree()) {
            Groups.build.intersect(b.x - range, b.y - range, range * 2f, range * 2f, buildingConsumer);
        } else {
            Groups.build.each(buildingConsumer);
        }

        if (!b.isAdded() || path.forceFieldId != -1) return;

        Cons<Unit> unitConsumer = unit -> {
            if (path.forceFieldId != -1 || !unit.isAdded() || unit.dead || unit.team == b.team || unit.shield <= 0f)
                return;

            for (Ability ability : unit.abilities) {
                if (ability instanceof ForceFieldAbility field) {
                    if (!findShieldContact(b, unit.x, unit.y, field.radius, Tmp.v2)) continue;

                    path.forceFieldId = unit.id;
                    unit.shield -= shieldDamage(b);
                    Fx.absorb.at(Tmp.v2.x, Tmp.v2.y);
                    field.hitSound.at(Tmp.v2.x, Tmp.v2.y, 1f + Mathf.range(0.1f), field.hitSoundVolume);
                    ricochet(b, Tmp.v2.x, Tmp.v2.y, unit.x, unit.y, false);
                    break;
                }

                if (ability instanceof EllipseForceFieldAbility field) {
                    if (!findEllipseShieldContact(b, unit, field, Tmp.v2)) continue;

                    path.forceFieldId = unit.id;
                    unit.shield -= shieldDamage(b);
                    EllipseForceFieldAbility.absorbEffect.at(Tmp.v2.x, Tmp.v2.y);
                    ricochet(b, Tmp.v2.x, Tmp.v2.y, unit.x, unit.y, false);
                    break;
                }
            }
        };

        if (Groups.unit.useTree()) {
            Groups.unit.intersect(b.x - range, b.y - range, range * 2f, range * 2f, unitConsumer);
        } else {
            Groups.unit.each(unitConsumer);
        }
    }

    private boolean findShieldContact(Bullet b, float centerX, float centerY, float radius, Vec2 out) {
        float startX = b.lastX, startY = b.lastY;
        float deltaX = b.x - startX, deltaY = b.y - startY;
        float radius2 = radius * radius;
        float offsetX = startX - centerX, offsetY = startY - centerY;

        if (offsetX * offsetX + offsetY * offsetY <= radius2) return false;

        float a = deltaX * deltaX + deltaY * deltaY;
        if (a <= 0.0001f) return false;

        float dot = offsetX * deltaX + offsetY * deltaY;
        float c = offsetX * offsetX + offsetY * offsetY - radius2;
        float discriminant = dot * dot - a * c;
        if (discriminant < 0f) return false;

        float root = Mathf.sqrt(discriminant);
        float first = (-dot - root) / a;
        float second = (-dot + root) / a;
        float hit = first >= 0f && first <= 1f ? first : second;
        if (hit < 0f || hit > 1f) return false;

        out.set(startX + deltaX * hit, startY + deltaY * hit);
        return true;
    }

    private boolean findEllipseShieldContact(Bullet b, Unit unit, EllipseForceFieldAbility field, Vec2 out) {
        float startX = b.lastX - unit.x, startY = b.lastY - unit.y;
        float deltaX = b.x - b.lastX, deltaY = b.y - b.lastY;
        float angle = unit.rotation + field.rotation - 90f;
        float cos = Mathf.cosDeg(angle), sin = Mathf.sinDeg(angle);
        float localStartX = startX * cos - startY * sin;
        float localStartY = startX * sin + startY * cos;
        float localDeltaX = deltaX * cos - deltaY * sin;
        float localDeltaY = deltaX * sin + deltaY * cos;
        float axisX = Math.max(field.longAxis, 0.001f);
        float axisY = Math.max(field.minorAxis, 0.001f);
        float normalizedStartX = localStartX / axisX;
        float normalizedStartY = localStartY / axisY;

        if (normalizedStartX * normalizedStartX + normalizedStartY * normalizedStartY <= 1f) return false;

        float normalizedDeltaX = localDeltaX / axisX;
        float normalizedDeltaY = localDeltaY / axisY;
        float a = normalizedDeltaX * normalizedDeltaX + normalizedDeltaY * normalizedDeltaY;
        if (a <= 0.0001f) return false;

        float bTerm = 2f * (normalizedStartX * normalizedDeltaX + normalizedStartY * normalizedDeltaY);
        float c = normalizedStartX * normalizedStartX + normalizedStartY * normalizedStartY - 1f;
        float discriminant = bTerm * bTerm - 4f * a * c;
        if (discriminant < 0f) return false;

        float root = Mathf.sqrt(discriminant);
        float first = (-bTerm - root) / (2f * a);
        float second = (-bTerm + root) / (2f * a);
        float hit = first >= 0f && first <= 1f ? first : second;
        if (hit < 0f || hit > 1f) return false;

        out.set(b.lastX + deltaX * hit, b.lastY + deltaY * hit);
        return true;
    }

    private float fade(Bullet b) {
        float fadeIn = fadeInTime <= 0f ? 1f : Mathf.clamp(b.time / fadeInTime);
        float fadeOut = fadeOutTime <= 0f ? 1f : Mathf.clamp((b.lifetime - b.time) / fadeOutTime);
        return Interp.pow2In.apply(fadeIn) * Interp.pow2Out.apply(fadeOut);
    }

    private LaserPath path(Bullet b) {
        return b.data instanceof LaserPath path ? path : null;
    }

    private void recordBounce(Bullet b, float x, float y) {
        LaserPath path = path(b);
        if (path == null || path.points.isEmpty()) return;

        Vec2 last = path.points.peek();
        if (last.dst2(x, y) > 0.01f) {
            path.points.add(new Vec2(x, y));
        }
    }

    private void ricochet(Bullet b, float hitX, float hitY, float centerX, float centerY, boolean tile) {
        int bounceCount = (int) b.fdata;
        if (maxBounces >= 0 && bounceCount >= maxBounces) {
            destroy(b);
            return;
        }

        recordBounce(b, hitX, hitY);

        Tmp.v1.set(hitX - centerX, hitY - centerY);
        if (tile || Tmp.v1.len2() < 0.0001f) {
            Tmp.v1.set(b.lastX - centerX, b.lastY - centerY);
            if (Math.abs(Tmp.v1.x) >= Math.abs(Tmp.v1.y)) {
                Tmp.v1.set(Mathf.sign(Tmp.v1.x), 0f);
            } else {
                Tmp.v1.set(0f, Mathf.sign(Tmp.v1.y));
            }
        }

        if (Tmp.v1.len2() < 0.0001f) {
            Tmp.v1.set(-b.vel.x, -b.vel.y).nor();
        } else {
            Tmp.v1.nor();
        }

        float dot = b.vel.x * Tmp.v1.x + b.vel.y * Tmp.v1.y;
        float reflectedX = b.vel.x - 2f * dot * Tmp.v1.x;
        float reflectedY = b.vel.y - 2f * dot * Tmp.v1.y;
        b.vel.set(reflectedX, reflectedY);
        b.rotation(b.vel.angle());
        b.set(hitX + Tmp.v1.x * bounceOffset, hitY + Tmp.v1.y * bounceOffset);
        b.fdata = bounceCount + 1f;
        if (maxBounces >= 0 && (int) b.fdata >= maxBounces) {
            destroy(b);
        }
    }

    private void destroy(Bullet b) {
        b.hit = true;
        b.remove();
    }

    private float pathLength(Bullet b, LaserPath path) {
        if (path.points.isEmpty()) return 0f;

        float length = 0f;
        Vec2 previous = path.points.first();
        for (int i = 1; i < path.points.size; i++) {
            Vec2 point = path.points.get(i);
            length += previous.dst(point);
            previous = point;
        }
        return length + previous.dst(b.x, b.y);
    }

    public static class LaserPath {
        public final Seq<Vec2> points = new Seq<>();
        public int forceFieldId = -1;

        public LaserPath(float x, float y) {
            points.add(new Vec2(x, y));
        }
    }
}
