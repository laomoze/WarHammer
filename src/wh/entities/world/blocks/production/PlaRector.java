package wh.entities.world.blocks.production;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.part.DrawPart;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.blocks.power.VariableReactor;
import mindustry.world.draw.DrawBlock;
import wh.content.WHFx;
import wh.entities.WHBaseEntity;
import wh.graphics.WHPal;
import wh.util.*;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class PlaRector extends VariableReactor {
    public static final float range = 36f;
    public static final float enemyRange = 15f * 8f;
public static final float lightingChance = 0.009f;
    public static final float bulletChance = 0.001f;
    public static Color Placolor = WHPal.SkyBlueF.cpy().lerp(WHPal.SkyBlue,0.3f);
    public float move=1.7f*tilesize;

    public PlaRector(String name) {
        super(name);
    }

    public static Effect spawnEffect = new Effect(60f, e -> {
        Draw.color(Placolor);
        Lines.stroke(e.fout() * 2f);
        Draw.color(Placolor);
        Lines.poly(e.x, e.y, 4, 8f + e.finpow() * range / 4, 45);
    });
    public static BulletType DrawnBullet = new BulletType() {
        {
            damage = 120f;
            speed = 3f;
            lifetime = 120f;
            hitEffect = WHFx.hitSpark(Placolor, 30f, 5, 12f, 1.5f, 9f);
            trailLength = 12;
            trailWidth = 1.5f;
            trailColor =Placolor;
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
        }

        @Override
        public void draw(Bullet b) {
            super.draw(b);
            Draw.color(Placolor);
            Drawf.tri(b.x, b.y, 4f, 8f, b.rotation());
            drawTrail(b);
            Draw.reset();

        }
    };

    public class LightninGeneratorBuild extends VariableReactorBuild {
        @Override
        public void draw() {
            super.draw();
            float bx = x, by = y;
            if (efficiency > 0) {
                if (wasVisible && !state.isPaused() && Mathf.chance(lightingChance/2)) {
                  /*  float range = block.size * tilesize / 2f;
                    float x1 = bx + Mathf.random(-range, range),
                            x2 = bx + Mathf.random(-range, range),
                            y1 = by + Mathf.random(-range, range),
                            y2 = by + Mathf.random(-range, range);
                    Fx.chainLightning.at(x1, y1, 0, WHPal.SkyBlueF.cpy().lerp(WHPal.SkyBlue, Mathf.absin(Time.time, 0.3f)), WHBaseEntity.pos(x2, y2));
*/
                    for (float mx : new float[]{move, -move}) {
                        for (float my : new float[]{move, -move}) {
                            Draw.z(Layer.effect);
                            Draw.alpha(warmup);
                            float x = bx + mx, y = by + my;
                            Fx.chainLightning.at(
                                    x, y, 0,
                                    WHPal.SkyBlueF.cpy().lerp(Pal.water, Mathf.absin(Time.time, 0.3f)),
                                    WHUtils.pos(bx, by)
                            );
                        }
                    }
                }
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();

            if (Mathf.chanceDelta(bulletChance * warmup)) {
                float random = Mathf.random(0f, 360f);
                for (int i = 0; i < 3; i++) {
                    DrawnBullet.create(this, x, y, 120 * i + random);
                }
            }
            if (Mathf.chanceDelta(effectChance / 2 * warmup)) {
                spawnEffect.at(
                        x + Mathf.range(block.size * 4),
                        y + Mathf.range(block.size * 4),
                        0f,
                        Placolor
                );
            }

        }
    }
}
