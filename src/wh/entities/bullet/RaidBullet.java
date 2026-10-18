package wh.entities.bullet;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.graphics.Trail;

import static mindustry.Vars.headless;

public class RaidBullet extends BasicBulletType {
    public Boolean randomTrail = false;
    public Boolean teamColor = false;
    public float tracerRandRange = 10f;
    public float tracerUpdateInterval = 3f;

    public RaidBullet() {
        speed = 8f;
        lifetime = 120f;

        splashDamageRadius = 80f;

        splashDamagePierce = true;
        collides = false;
        collidesGround = true;
        collideFloor = true;
        collidesAir = true;

        hittable = true;
        reflectable = false;
        absorbable = true;
        despawnHit = true;

        trailLength = 14;
        trailChance = 0.075f;
        trailParam = 6;
        drawSize = 120f;
        hitShake = despawnShake = 16f;

        shrinkX = shrinkY = 0;
        height = 75f;
        width = 20f;

        sprite = "missile-large";
        hitSound = Sounds.explosion;
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
    }

    @Override
    public void updateTrailEffects(Bullet b) {
        if (trailChance > 0) {
            if (Mathf.chanceDelta(trailChance)) {
                trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
            }
        }

        if (trailInterval > 0f) {
            if (b.timer(0, trailInterval)) {
                trailEffect.at(b.x, b.y, trailRotation ? b.rotation() : trailParam, b.team.color);
            }
        }
    }

    @Deprecated
    public void updateTrail(Bullet b) {
        if (!headless && trailLength > 0) {
            if (b.trail == null) {
                b.trail = new Trail(trailLength);
            }
            b.trail.length = trailLength;
            if (randomTrail && b.timer(1, tracerUpdateInterval)) {
                b.trail.update(
                        b.x + Mathf.random(-tracerRandRange, tracerRandRange),
                        b.y + Mathf.random(-tracerRandRange, tracerRandRange),
                        trailInterp.apply(b.fin())
                );
            } else {
                b.trail.update(b.x, b.y, trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
            }
        }
    }

    @Override
    public void draw(Bullet b) {
        super.draw(b);
        float shrink = shrinkInterp.apply(b.fout());
        float height = this.height * ((1f - shrinkY) + shrinkY * shrink);
        float width = this.width * ((1f - shrinkX) + shrinkX * shrink);
        float offset = -90 + (spin != 0 ? Mathf.randomSeed(b.id, 360f) + b.time * spin : 0f) + rotationOffset;

        Color mix = Tmp.c1.set(mixColorFrom).lerp(mixColorTo, b.fin());

        Draw.mixcol(mix, mix.a);

        if (backRegion.found()) {
            Draw.color(teamColor ? b.team().color.cpy() : backColor);
            Draw.rect(backRegion, b.x, b.y, width, height, b.rotation() + offset);
        }

        Draw.color(teamColor ? b.team().color.cpy().lerp(Color.white, 0.25f) : frontColor);
        Draw.rect(frontRegion, b.x, b.y, width, height, b.rotation() + offset);

        Draw.reset();
    }
}
