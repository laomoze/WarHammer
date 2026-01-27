package wh.entities.world.blocks.production;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;
import wh.graphics.*;

import static mindustry.Vars.tilesize;
//unused
public class LightninGenerator extends NuclearReactor {
    public static final float range = 36f;
    public static final float enemyRange = 15f * 8f;
    public static final float pullPower = 22f * 60f;

    public static Effect Start = new Effect(30f, e -> {
        Draw.color(WHPal.SkyBlueF);
        Lines.stroke(3f * e.fout());
        Lines.circle(e.x, e.y, range * e.fout());
    });

    public static BulletType effectBullet = new BasicBulletType() {{
        collidesAir = true;
        lifetime = 6f;
        speed = 1f;
        splashDamageRadius = 56f;
        instantDisappear = true;
        splashDamage = 10f;
        hitShake = 3f;
        lightningColor = WHPal.SkyBlueF;
        lightningDamage = 3f;
        lightning = 5;
        lightningLength = 12;
        hitSound = Sounds.explosion;

        hitEffect = new Effect(60f, e -> {
            Draw.color(WHPal.SkyBlueF);
            Lines.stroke(e.fout() * 2f);
            Lines.circle(e.x, e.y, 4f + e.finpow() * splashDamageRadius);
            Draw.color(WHPal.SkyBlueF);
            for (int i = 0; i < 4; i++) {
                Drawf.tri(e.x, e.y, 6f, 36f * e.fout(), (i - e.fin()) * 90f);
            }
            Draw.color();
            for (int i = 0; i < 4; i++) {
                Drawf.tri(e.x, e.y, 3f, 16f * e.fout(), (i - e.fin()) * 90f);
            }
        });
    }};

    public static Effect tailEffect = new Effect(12f, e -> {
        Draw.color(WHPal.SkyBlueF);
        float time =e.data();
        Drawf.tri(e.x, e.y, 4f * e.fout(), 11f, e.rotation);
        Drawf.tri(e.x, e.y, 4f * e.fout(), (float) (15f *Math.min(1, time/ 8 * 0.8 + 0.2)), e.rotation - 180f);
    });

    public static BulletType effectBullet2 = new BulletType() {
        {
            damage = 22f;
            speed = 4f;
            lifetime = 120f;
            hitEffect = Fx.none;
            despawnEffect = Fx.none;

        }

        @Override
        public void update(Bullet b) {
            super.update(b);
            if (b.time > 18f) {
                Teamc target = Units.closestTarget(b.team, b.x, b.y, enemyRange,
                        unit -> (unit.isGrounded() && collidesGround) || (unit.isFlying() && collidesAir),
                        t -> collidesGround
                );
                Position targetTo = target != null ? target : (Position) b.owner;
                float homingPower = target == null ? 0.08f : 0.5f;
                if (targetTo != null) {
                    b.vel.setAngle(Mathf.slerpDelta(b.rotation() + 0.01f, b.angleTo(targetTo), homingPower));
                }
            }
            tailEffect.at(b.x, b.y, b.rotation(),b.time);
        }


        @Override
        public void draw(Bullet b) {
            super.draw(b);
            Draw.color(WHPal.SkyBlueF);
            Drawf.tri(b.x, b.y, 4f, 8f, b.rotation());
            Draw.reset();
        }
    };

    public LightninGenerator(String name) {
        super(name);

        size = 6;
        itemCapacity = 30;
        health = 2400;
        itemDuration = 186f;
        powerProduction = 15900f / 60f;
        hasItems = true;
        hasLiquids = true;
        heating = 0.04f;
        explosionRadius = 88;
        explosionDamage = 6000;
        coolantPower = 0.1f;

        liquidCapacity = 60f;


        requirements(Category.power, BuildVisibility.shown, ItemStack.with(
                Items.metaglass, 600,
                Items.graphite, 550,
                Items.silicon, 470,
                Items.surgeAlloy, 550

        ));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, enemyRange, Pal.accent);
    }


    public class LightninGeneratorBuild extends NuclearReactorBuild {
        public boolean working = false;
        public float consumeTimer = 0f;
        public float light = 0f;

        @Override
        public void updateTile() {
            if (items.total() == itemCapacity && !working) {
                Start.at(this);
                Units.nearby(null, x, y, range * 2, unit -> {
                    unit.impulse(Tmp.v3.set(unit).sub(x, y).nor().scl(-pullPower));
                });
                working = true;
            }
            if (items.total() < 1 && working) {
                working = false;
                consumeTimer = 0f;
            }



            int fuel = items.get(fuelItem);
            float fullness = fuel / itemCapacity;
            productionEfficiency = fullness;

            if (fuel > 0 && enabled && working) {
                heat += fullness * heating * Math.min(delta(), 4f);
                consumeTimer += getProgressIncrease(itemDuration);
                if (consumeTimer >= 1f) {

                    consume();
                    effectBullet.create(this, team, x + Mathf.range(size * 4), y + Mathf.range(size * 4), 0f);
                    float random = Mathf.random(0f, 360f);
                    for (int i = 0; i < 3; i++) {
                        effectBullet2.create(this, x, y, 120 * i + random);
                    }
                    consumeTimer %= 1f;
                }
            } else {
                productionEfficiency = 0f;
            }

            if(heat > 0){
                float maxUsed = Math.min(liquids.currentAmount(), heat / coolantPower);
                heat -= maxUsed * coolantPower;
                liquids.remove(liquids.current(), maxUsed);
            }

            if (heat > smokeThreshold) {
                float smoke = 1 + (heat - smokeThreshold) / (1 - smokeThreshold);
                if (Mathf.chance(smoke / 20 * delta())) {
                    Fx.reactorsmoke.at(x + Mathf.range(size * tilesize / 2), y + Mathf.range(size * tilesize / 2));
                }
            }

            heat = Mathf.clamp(heat);
            if (heat >= 0.999f) {
                Events.fire(EventType.Trigger.thoriumReactorOverheat);
                kill();
            }

            light = Mathf.lerpDelta(light, working ? 1f : 0f, 0.05f);
        }

        @Override
        public void draw() {
            super.draw();
            Draw.color();
            Draw.alpha(items.total() > 0 ? 1f : 0f);
            Draw.z(Layer.effect);
            Lines.stroke(3f);
            if (!working) {
                circlePercent(x, y, range, items.total() / itemCapacity, 135f);
            }
            Draw.alpha(light);
            Draw.rect(Core.atlas.find("btm-lightnin-generator-lights"), x, y);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.dashCircle(x, y, enemyRange, Pal.accent);
        }

        public  void circlePercent(float x, float y, float rad, float percent, float angle) {
            float p = Mathf.clamp(percent);
            int sides = Lines.circleVertices(rad);
            float space = 360f / sides;
            float len = 2 * rad * Mathf.sinDeg(space / 2);
            float hstep = Lines.getStroke() / 2 / Mathf.cosDeg(space / 2);
            float r1 = rad - hstep;
            float r2 = rad + hstep;
            int i;
            for (i = 0; i < sides * p - 1; ++i) {
                float a = space * i + angle;
                float cos = Mathf.cosDeg(a);
                float sin = Mathf.sinDeg(a);
                float cos2 = Mathf.cosDeg(a + space);
                float sin2 = Mathf.sinDeg(a + space);
                Fill.quad(x + r1 * cos, y + r1 * sin, x + r1 * cos2, y + r1 * sin2, x + r2 * cos2, y + r2 * sin2, x + r2 * cos, y + r2 * sin);
            }
            float a = space * i + angle;
            float cos = Mathf.cosDeg(a);
            float sin = Mathf.sinDeg(a);
            float cos2 = Mathf.cosDeg(a + space);
            float sin2 = Mathf.sinDeg(a + space);
            float f = sides * p - i;
            Vec2 vec = new Vec2();
            vec.trns(a, 0, len * (f - 1));
            Fill.quad(x + r1 * cos, y + r1 * sin, x + r1 * cos2 + vec.x, y + r1 * sin2 + vec.y, x + r2 * cos2 + vec.x, y + r2 * sin2 + vec.y, x + r2 * cos, y + r2 * sin);
        }

        @Override
        public void write(arc.util.io.Writes write) {
            super.write(write);
            write.bool(working);
            write.f(consumeTimer);
            write.f(light);
        }

        @Override
        public void read(arc.util.io.Reads read, byte revision) {
            super.read(read, revision);
            working = read.bool();
            consumeTimer = read.f();
            light = read.f();
        }
    }
}