//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.content;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.FloatSeq;
import arc.struct.IntMap;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pool;

import java.util.Objects;

import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.SteamVent;
import wh.entities.abilities.PcShieldArcAbility;
import wh.entities.bullet.SlowLaserBulletType;
import wh.graphics.Drawn;
import wh.graphics.PositionLightning;
import wh.struct.Vec2Seq;
import wh.util.WHUtils;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;
import static mindustry.content.Fx.rand;

public final class WHFx {
    public static final float EFFECT_MASK = 110.0001F;
    public static final float EFFECT_BOTTOM = 99.89F;
    private static final Vec2 v = new Vec2();
    public static final float lightningAlign = 0.5F;
    public static final Rand rand = new Rand();
    public static final Rand rand1 = new Rand();
    public static final Rand rand2 = new Rand();
    public static final Vec2 v7 = new Vec2();
    public static final Vec2 v8 = new Vec2();
    public static final Vec2 v9 = new Vec2();
    public static final IntMap<Effect> same = new IntMap();
    public static final int[] oneArr = new int[]{1};
    public static Effect lightningSpark;
    public static Effect TankAG7BulletExplode;
    public static Effect posLightning;
    public static Effect attackWarningRange;
    public static Effect attackWarningPos;
    public static Effect energyUnitBlast;
    public static Effect circle;
    public static Effect circleOut;
    public static Effect circleOutQuick;
    public static Effect circleOutLong;
    public static Effect circleSplash;
    public static Effect missileShoot;
    public static Effect shuttle;
    public static Effect shuttleDark;
    public static Effect shuttleLerp;
    public static Effect lightningHitSmall;
    public static Effect lightningHitLarge;
    public static Effect lightningFade;
    public static Effect sapPlasmaShoot;
    public static Effect coloredHitSmall;
    public static Effect hugeTrail;
    public static Effect hugeSmokeGray;
    public static Effect hugeSmoke;
    public static Effect hugeSmokeLong;
    public static Effect square45_4_45;
    public static Effect square45_6_45;
    public static Effect square45_6_45_Charge;
    public static Effect square45_8_45;
    public static Effect crossBlastArrow45;
    public static Effect ScrossBlastArrow45;
    public static Effect crossBlast_45;
    public static Effect crossSpinBlast;
    public static Effect ultFireBurn;
    public static Effect skyTrail;
    public static Effect trailToGray;
    public static Effect trailFromWhite;
    public static Effect chainLightningFade;
    public static Effect chainLightningFadeReversed;
    public static Effect techBlueCircleSplash;
    public static Effect techBlueExplosion;
    public static Effect techBlueCharge;
    public static Effect techBlueChargeBegin;
    public static Effect largeTechBlueHitCircle;
    public static Effect largeTechBlueHit;
    public static Effect mediumTechBlueHit;
    public static Effect techBlueSmokeBig;
    public static Effect techBlueShootBig;
    public static Effect healReceiveCircle;
    public static Effect healSendCircle;
    public static Effect jumpTrailOut;
    public static Effect AccretionDiskEffect;
    public static Effect groundRise;
    public static Effect squSpark1;
    public static Effect triSpark2;
    public static Effect tank3sMissileTrailSmoke;
    public static Effect tank3sExplosionSmoke;
    private WHFx() {
    }
    public interface EffectParam {
        void draw(long var1, float var3, float var4, float var5, float var6);
    }

    public static class ateData implements Pool.Poolable {
        public float width;
        public int length;
        public float inRad;
        public float outRad;
        public float speed;
        public transient Trail trail;
        public Bullet owner;
        public boolean out = false;

        public ateData() {
        }

        public void reset() {
            this.width = 0.0F;
            this.length = 0;
            this.inRad = this.outRad = this.speed = 0.0F;
            this.trail = null;
            this.owner = null;
            this.out = false;
        }
    }
    public static Effect boolSelector = new Effect(0, 0, e -> {});
    public static Effect hitSpark = new Effect(45.0F, (e) -> {
        Draw.color(e.color, Color.white, e.fout() * 0.3F);
        Lines.stroke(e.fout() * 1.6F);
        rand.setSeed(e.id);
        Angles.randLenVectors(e.id, 8, e.finpow() * 20.0F, (x, y) -> {
            float ang = Mathf.angle(x, y);
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * rand.random(1.95F, 4.25F) + 1.0F);
        });
    });

    public static Effect hitSparkLarge = new Effect(40.0F, (e) -> {
        Draw.color(e.color, Color.white, e.fout() * 0.3F);
        Lines.stroke(e.fout() * 1.6F);
        rand.setSeed(e.id);
        Angles.randLenVectors(e.id, 18, e.finpow() * 27.0F, (x, y) -> {
            float ang = Mathf.angle(x, y);
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * (float) rand.random(4, 8) + 2.0F);
        });
    });
    public static Effect hitSparkHuge = new Effect(70.0F, (e) -> {
        Draw.color(e.color, Color.white, e.fout() * 0.3F);
        Lines.stroke(e.fout() * 1.6F);
        rand.setSeed(e.id);
        Angles.randLenVectors(e.id, 26, e.finpow() * 65.0F, (x, y) -> {
            float ang = Mathf.angle(x, y);
            Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * (float) rand.random(6, 9) + 3.0F);
        });
    });
    public static float fout(float fin, float margin) {
        return fin >= 1.0F - margin ? 1.0F - (fin - (1.0F - margin)) / margin : 1.0F;
    }
    public static Effect polyTrail(Color fromColor, Color toColor, float size, float lifetime) {
        return new Effect(lifetime, size * 2.0F, (e) -> {
            Draw.color(fromColor, toColor, e.fin());
            //ill.poly(e.x, e.y, 6, size * e.fout(), e.rotation);：这行代码使用 Fill 类的 poly 方法绘制一个多边形。
            // e.x 和 e.y 是 Effect 对象的当前位置，6 是多边形的边数，size * e.fout() 是多边形的大小，e.rotation 是多边形的旋转角度。
            Fill.poly(e.x, e.y, 6, size * e.fout(), e.rotation);
            //Drawf.light(e.x, e.y, e.fout() * size, fromColor, 0.7F);：这行代码在 Effect 对象的当前位置绘制一个光源。
            // e.fout() * size 是光源的大小，fromColor 是光源的颜色，0.7F 是光源的强度。
            Drawf.light(e.x, e.y, e.fout() * size, fromColor, 0.7F);
        });
    }

    public static Effect hitSpark(Color color, float lifetime, int num, float range, float stroke, float length) {
        return new Effect(lifetime, (e) -> {
            Draw.color(color, Color.white, e.fout() * 0.3F);
            Lines.stroke(e.fout() * stroke);
            Angles.randLenVectors(e.id, num, e.finpow() * range, e.rotation, 360.0F, (x, y) -> {
                float ang = Mathf.angle(x, y);
                Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * length * 0.85F + length * 0.15F);
            });
        });
    }

    public static Effect instBomb(Color color) {
        return get("instBomb", color, instBombSize(color, 4, 80.0F));
    }

    public static Effect instBombSize(Color color, int num, float size) {
        return new Effect(22.0F, size * 1.5F, (e) -> {
            Draw.color(color);
            Lines.stroke(e.fout() * 4.0F);
            Lines.circle(e.x, e.y, 4.0F + e.finpow() * size / 4.0F);
            Drawf.light(e.x, e.y, e.fout() * size, color, 0.7F);

            int i;
            for (i = 0; i < num; ++i) {
                //这两个循环分别绘制了 num 个三角形，每个三角形的大小和旋转角度都不同。
                Drawn.tri(e.x, e.y, size / 12.0F, size * e.fout(), (float) (i * 90 + 45));
            }

            Draw.color();
            for (i = 0; i < num; ++i) {
                Drawn.tri(e.x, e.y, size / 26.0F, size / 2.5F * e.fout(), (float) (i * 90 + 45));
            }

        });
    }

    public static Effect instHit(Color color) {
        return get("instHit", color, instHit(color, 5, 50.0F));
    }

    public static Effect instHit(Color color, int num, float size) {
        return new Effect(20.0F, size * 1.5F, (e) -> {
            rand.setSeed(e.id);

            for (int i = 0; i < 2; ++i) {
                Draw.color(i == 0 ? color : color.cpy().lerp(Color.white, 0.25F));
                float m = i == 0 ? 1.0F : 0.5F;

                for (int j = 0; j < num; ++j) {
                    float rot = e.rotation + rand.range(size);
                    float w = 15.0F * e.fout() * m;
                    Drawn.tri(e.x, e.y, w, (size + rand.range(size * 0.6F)) * m, rot);
                    Drawn.tri(e.x, e.y, w, size * 0.3F * m, rot + 180.0F);
                }
            }

            e.scaled(12.0F, (c) -> {
                Draw.color(color.cpy().lerp(Color.white, 0.25F));
                Lines.stroke(c.fout() * 2.0F + 0.2F);
                Lines.circle(e.x, e.y, c.fin() * size * 1.1F);
            });
            e.scaled(18.0F, (c) -> {
                Draw.color(color);
                Angles.randLenVectors(e.id, 25, 8.0F + e.fin() * size * 1.25F, e.rotation, 60.0F, (x, y) -> {
                    Fill.square(e.x + x, e.y + y, c.fout() * 3.0F, 45.0F);
                });
            });
            Drawf.light(e.x, e.y, e.fout() * size, color, 0.7F);
        });
    }

    public static Effect smoothColorRect(Color out, float rad, float lifetime) {
        return (new Effect(lifetime, rad * 2.0F, (e) -> {
            Draw.blend(Blending.additive);
            float radius = e.fin(Interp.pow3Out) * rad;
            Fill.light(e.x, e.y, 4, radius, 45.0F, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out)));
            Draw.blend();
        })).layer(110.15F);
    }

    public static Effect smoothColorCircle(Color out, float rad, float lifetime) {
        return (new Effect(lifetime, rad * 2.0F, (e) -> {
            Draw.blend(Blending.additive);
            float radius = e.fin(Interp.pow3Out) * rad;
            Fill.light(e.x, e.y, Lines.circleVertices(radius), radius, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out)));
            Drawf.light(e.x, e.y, radius * 1.3F, out, 0.7F * e.fout(0.23F));
            Draw.blend();
        })).layer(110.15F);
    }

    public static Effect smoothColorCircle(Color out, float rad, float lifetime, float alpha) {
        return (new Effect(lifetime, rad * 2.0F, (e) -> {
            Draw.blend(Blending.additive);
            float radius = e.fin(Interp.pow3Out) * rad;
            Fill.light(e.x, e.y, Lines.circleVertices(radius), radius, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out) * alpha));
            Drawf.light(e.x, e.y, radius * 1.3F, out, 0.7F * e.fout(0.23F));
            Draw.blend();
        })).layer(110.15F);
    }

    public static Effect instTrail(Color color, float angle, boolean random) {
        return new Effect(30.0F, (e) -> {
            int[] var4 = angle == 0.0F ? oneArr : Mathf.signs;
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                int j = var4[var6];

                for (int i = 0; i < 2; ++i) {
                    Draw.color(i == 0 ? color : color.cpy().lerp(Color.white, 0.15F));
                    float m = i == 0 ? 1.0F : 0.5F;
                    float rot = e.rotation + 180.0F;
                    float w = 10.0F * e.fout() * m;
                    Drawn.tri(e.x, e.y, w, 30.0F + (random ? Mathf.randomSeedRange(e.id, 15.0F) : 8.0F) * m, rot + (float) j * angle);
                    if (angle == 0.0F) {
                        Drawn.tri(e.x, e.y, w, 10.0F * m, rot + 180.0F + (float) j * angle);
                    } else {
                        Fill.circle(e.x, e.y, w / 2.0F);
                    }
                }
            }

        });
    }

    public static Effect shootCircleSmall(Color color) {
        return get("shootCircleSmall", color, new Effect(30, e -> {
            color(color, Color.white, e.fout() * 0.75f);
            rand.setSeed(e.id);
            randLenVectors(e.id, 3, 3 + 23 * e.fin(), e.rotation, 22, (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * rand.random(1.5f, 3.2f));
                Drawf.light(e.x + x, e.y + y, e.fout() * 4.5f, color, 0.7f);
            });
        }));
    }

    public static Effect get(String m, Color c, Effect effect) {
        int hash = Objects.hash(new Object[]{m, c});
        Effect or = (Effect) same.get(hash);
        if (or == null) {
            same.put(hash, effect);
        }

        return or == null ? effect : or;
    }

    public static Effect lightningHitLarge(Color color) {
        return get("lightningHitLarge", color, new Effect(50.0F, 180.0F, (e) -> {
            Draw.color(color);
            Drawf.light(e.x, e.y, e.fout() * 90.0F, color, 0.7F);
            e.scaled(25.0F, (t) -> {
                Lines.stroke(3.0F * t.fout());
                Lines.circle(e.x, e.y, 3.0F + t.fin(Interp.pow3Out) * 80.0F);
            });
            Fill.circle(e.x, e.y, e.fout() * 8.0F);
            Angles.randLenVectors(e.id + 1, 4, 1.0F + 60.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 5.0F);
            });
            Draw.color(Color.gray);
            Angles.randLenVectors(e.id, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F);
            });
        }));
    }

    public static Effect shootLineSmall(Color color) {
        return get("shootLineSmall", color, new Effect(37.0F, (e) -> {
            Draw.color(color, Color.white, e.fout() * 0.7F);
            Angles.randLenVectors(e.id, 4, 8.0F + 32.0F * e.fin(), e.rotation, 22.0F, (x, y) -> {
                Lines.stroke(1.25F * e.fout(0.2F));
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 6.0F + 3.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 13.0F + 3.0F, color, 0.7F);
            });
        }));
    }

    public static Effect blast(Color color, float range) {
        float lifetime = Mathf.clamp(range * 1.5F, 90.0F, 600.0F);
        return new Effect(lifetime, range * 2.5F, (e) -> {
            Draw.color(color);
            Drawf.light(e.x, e.y, e.fout() * range, color, 0.7F);
            e.scaled(lifetime / 3.0F, (t) -> {
                Lines.stroke(3.0F * t.fout());
                Lines.circle(e.x, e.y, 8.0F + t.fin(Interp.circleOut) * range * 1.35F);
            });
            e.scaled(lifetime / 2.0F, (t) -> {
                Fill.circle(t.x, t.y, t.fout() * 8.0F);
                Angles.randLenVectors(t.id + 1, (int) (range / 13.0F), 2.0F + range * 0.75F * t.finpow(), (x, y) -> {
                    Fill.circle(t.x + x, t.y + y, t.fout(Interp.pow2Out) * Mathf.clamp(range / 15.0F, 3.0F, 14.0F));
                    Drawf.light(t.x + x, t.y + y, t.fout(Interp.pow2Out) * Mathf.clamp(range / 15.0F, 3.0F, 14.0F), color, 0.5F);
                });
            });
            Draw.z(99.999F);
            Draw.color(Color.gray);
            Draw.alpha(0.85F);
            float intensity = Mathf.clamp(range / 10.0F, 5.0F, 25.0F);

            for (int i = 0; i < 4; ++i) {
                rand.setSeed((e.id << 1) + i);
                float lenScl = rand.random(0.4F, 1.0F);
                int fi = i;
                e.scaled(e.lifetime * lenScl, (eIn) -> {
                    Angles.randLenVectors(eIn.id + fi - 1, eIn.fin(Interp.pow10Out), (int) (intensity / 2.5F), 8.0F * intensity, (x, y, in, out) -> {
                        float fout = eIn.fout(Interp.pow5Out) * rand.random(0.5F, 1.0F);
                        Fill.circle(eIn.x + x, eIn.y + y, fout * (2.0F + intensity) * 1.8F);
                    });
                });
            }

        });
    }

    public static Effect square(Color color, float lifetime, int num, float range, float size) {
        return new Effect(lifetime, (e) -> {
            Draw.color(color);
            rand.setSeed(e.id);
            Angles.randLenVectors(e.id, num, range * e.finpow(), (x, y) -> {
                float s = e.fout(Interp.pow3In) * (size + rand.range(size / 3.0F));
                Fill.square(e.x + x, e.y + y, s, 45.0F);
                Drawf.light(e.x + x, e.y + y, s * 2.25F, color, 0.7F);
            });
        });
    }

    public static Effect instShoot(Color color, Color colorInner) {
        return new Effect(24.0F, (e) -> {
            e.scaled(10.0F, (b) -> {
                Draw.color(Color.white, color, b.fin());
                Lines.stroke(b.fout() * 3.0F + 0.2F);
                Lines.circle(b.x, b.y, b.fin() * 50.0F);
            });
            Draw.color(color);
            int[] var3 = Mathf.signs;
            int var4 = var3.length;

            int var5;
            int i;
            for (var5 = 0; var5 < var4; ++var5) {
                i = var3[var5];
                Drawn.tri(e.x, e.y, 8.0F * e.fout(), 85.0F, e.rotation + 90.0F * (float) i);
                Drawn.tri(e.x, e.y, 8.0F * e.fout(), 50.0F, 90.0F + 90.0F * (float) i);
            }

            Draw.color(colorInner);
            var3 = Mathf.signs;
            var4 = var3.length;

            for (var5 = 0; var5 < var4; ++var5) {
                i = var3[var5];
                Drawn.tri(e.x, e.y, 5.0F * e.fout(), 48.0F, e.rotation + 90.0F * (float) i);
                Drawn.tri(e.x, e.y, 5.0F * e.fout(), 29.0F, 90.0F + 90.0F * (float) i);
            }

        });
    }

    public static Effect sharpBlast(Color colorExternal, Color colorInternal, float lifetime, float range) {
        return new Effect(lifetime, range * 2.0F, (e) -> {
            Angles.randLenVectors(e.id, (int) Mathf.clamp(range / 8.0F, 4.0F, 18.0F), range / 8.0F, range * (1.0F + e.fout(Interp.pow2OutInverse)) / 2.0F, (x, y) -> {
                float angle = Mathf.angle(x, y);
                float width = e.foutpowdown() * rand.random(range / 6.0F, range / 3.0F) / 2.0F * e.fout();
                rand.setSeed(e.id);
                float length = rand.random(range / 2.0F, range * 1.1F) * e.fout();
                Draw.color(colorExternal);
                Drawn.tri(e.x + x, e.y + y, width, range / 3.0F * e.fout(Interp.pow2In), angle - 180.0F);
                Drawn.tri(e.x + x, e.y + y, width, length, angle);
                Draw.color(colorInternal);
                width *= e.fout();
                Drawn.tri(e.x + x, e.y + y, width / 2.0F, range / 3.0F * e.fout(Interp.pow2In) * 0.9F * e.fout(), angle - 180.0F);
                Drawn.tri(e.x + x, e.y + y, width / 2.0F, length / 1.5F * e.fout(), angle);
            });
        });
    }

    public static Effect subEffect(float lifetime, float radius, int num, float childLifetime, Interp spreadOutInterp, EffectParam effect) {
        return new Effect(lifetime, radius * 2.0F, (e) -> {
            rand.setSeed(e.id);
            float finT = e.lifetime * e.fin(spreadOutInterp);

            for (int s = 0; s < num; ++s) {
                float sBegin = rand.random(e.lifetime - childLifetime);
                float fin = (finT - sBegin) / childLifetime;
                if (!(fin < 0.0F) && !(fin > 1.0F)) {
                    float fout = 1.0F - fin;
                    rand2.setSeed(e.id + s);
                    float theta = rand2.random(0.0F, 6.2831855F);
                    v9.set(Mathf.cos(theta), Mathf.sin(theta)).scl(radius * sBegin / (e.lifetime - childLifetime));
                    Tmp.c1.set(e.color).lerp(Color.white, fout * 0.7F);
                    Draw.color(Tmp.c1);
                    effect.draw((e.id + s) + 9999L, e.x + v9.x, e.y + v9.y, 57.295776F * theta, fin);
                }
            }

        });
    }

    public static Effect lineCircleOut(Color color, float lifetime, float size, float stroke) {
        return new Effect(lifetime, (e) -> {
            Draw.color(color);
            Lines.stroke(e.fout() * stroke);
            Lines.circle(e.x, e.y, e.fin(Interp.pow3Out) * size);
        });
    }

    public static Effect circleOut(float lifetime, float radius, float thick) {
        return new Effect(lifetime, radius * 2.0F, (e) -> {
            Draw.color(e.color, Color.white, e.fout() * 0.7F);
            Lines.stroke(thick * e.fout());
            Lines.circle(e.x, e.y, radius * e.fin(Interp.pow3Out));
        });
    }

    public static Effect circleOut(Color color, float range) {
        return new Effect(Mathf.clamp(range / 2.0F, 45.0F, 360.0F), range * 1.5F, (e) -> {
            rand.setSeed(e.id);
            Draw.color(Color.white, color, e.fin() + 0.6F);
            float circleRad = e.fin(Interp.circleOut) * range;
            Lines.stroke(Mathf.clamp(range / 24.0F, 4.0F, 20.0F) * e.fout());
            Lines.circle(e.x, e.y, circleRad);

            for (int i = 0; (float) i < Mathf.clamp(range / 12.0F, 9.0F, 60.0F); ++i) {
                Tmp.v1.set(1.0F, 0.0F).setToRandomDirection(rand).scl(circleRad);
                Drawn.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16.0F, circleRad / 12.0F) * e.fout(), rand.random(circleRad / 4.0F, circleRad / 1.5F) * (1.0F + e.fin()) / 2.0F, Tmp.v1.angle() - 180.0F);
            }

        });
    }

    public static Effect crossBlast(Color color) {
        return get("crossBlast", color, crossBlast(color, 72.0F));
    }

    public static Effect crossBlast(Color color, float size) {
        return crossBlast(color, size, 0.0F);
    }

    public static Effect crossBlast(Color color, float size, float rotate) {
        return new Effect(Mathf.clamp(size / 3.0F, 35.0F, 240.0F), size * 2.0F, (e) -> {
            Draw.color(color, Color.white, e.fout() * 0.55F);
            Drawf.light(e.x, e.y, e.fout() * size, color, 0.7F);
            e.scaled(10.0F, (ix) -> {
                Lines.stroke(1.35F * ix.fout());
                Lines.circle(e.x, e.y, size * 0.7F * ix.finpow());
            });
            rand.setSeed(e.id);
            float sizeDiv = size / 1.5F;
            float randL = rand.random(sizeDiv);

            for (int i = 0; i < 4; ++i) {
                Drawn.tri(e.x, e.y, size / 20.0F * (e.fout() * 3.0F + 1.0F) / 4.0F * (e.fout(Interp.pow3In) + 0.5F) / 1.5F, (sizeDiv + randL) * Mathf.curve(e.fin(), 0.0F, 0.05F) * e.fout(Interp.pow3), (float) (i * 90) + rotate);
            }

        });
    }

    public static float dx(float px, float r, float angle) {
        return px + r * (float) Math.cos((double) angle * Math.PI / 180.0);
    }

    public static float dy(float py, float r, float angle) {
        return py + r * (float) Math.sin((double) angle * Math.PI / 180.0);
    }

    public static Effect aimEffect(float lifetime, Color color, float width, float length, float spacing) {
        return new Effect(lifetime, length, (e) -> {
            Draw.color(color);
            TextureRegion region = Core.atlas.find("wh-aim-shoot");
            float track = Mathf.curve(e.fin(Interp.pow2Out), 0.0F, 0.25F) * Mathf.curve(e.fout(Interp.pow4Out), 0.0F, 0.3F) * e.fin();

            for (int i = 0; (float) i <= length / spacing; ++i) {
                Tmp.v1.trns(e.rotation, (float) i * spacing);
                float f = Interp.pow3Out.apply(Mathf.clamp((e.fin() * length - (float) i * spacing) / spacing)) * (0.6F + track * 0.4F);
                Draw.rect(region, e.x + Tmp.v1.x, e.y + Tmp.v1.y, 155.0F * Draw.scl * f, 155.0F * Draw.scl * f, e.rotation - 90.0F);
            }

            Tmp.v1.trns(e.rotation, 0.0F, (2.0F - track) * 8.0F * width);
            Lines.stroke(track * 2.0F);
            int[] var11 = Mathf.signs;
            int var12 = var11.length;

            for (int var9 = 0; var9 < var12; ++var9) {
                int ix = var11[var9];
                Lines.lineAngle(e.x + Tmp.v1.x * (float) ix, e.y + Tmp.v1.y * (float) ix, e.rotation, length * (0.75F + track / 4.0F) * Mathf.curve(e.fout(Interp.pow5Out), 0.0F, 0.1F));
            }

        });
    }

    public static Effect spreadOutSpark(float lifetime, float radius, int sparks, int sparkSpikes, float sparkLifetime, float sparkSize, float sparkLength, Interp spreadOutInterp) {
        return new Effect(lifetime, radius * 2.0F, (e) -> {
            rand.setSeed(e.id);
            float finT = e.lifetime * e.fin(spreadOutInterp);

            for (int s = 0; s < sparks; ++s) {
                float sBegin = rand.random(e.lifetime - sparkLifetime);
                float fin = (finT - sBegin) / sparkLifetime;
                if (!(fin < 0.0F) && !(fin > 1.0F)) {
                    float fout = 1.0F - fin;
                    float fslope = (0.5F - Math.abs(fin - 0.5F)) * 2.0F;
                    rand2.setSeed(e.id + s);
                    v.setToRandomDirection(rand2).scl(radius * sBegin / (e.lifetime - sparkLifetime));
                    Tmp.c1.set(e.color).lerp(Color.white, fout * 0.7F);
                    Draw.color(Tmp.c1);
                    Lines.stroke(1.2F * Mathf.curve(fout, 0.0F, 0.22F));
                    Angles.randLenVectors(e.id + s + 1, sparkSpikes, sparkSize * fin, (x, y) -> {
                        Lines.lineAngle(e.x + x + v.x, e.y + y + v.y, Mathf.angle(x, y), fslope * sparkLength + 2.0F);
                        Drawf.light(e.x + x + v.x, e.y + y + v.y, fin * sparkLength * fslope * 1.3F, Tmp.c1, 0.7F);
                    });
                }
            }

        });
    }
    public static Effect shootLine(float size, float angleRange) {
        int num = Mathf.clamp((int) size / 6, 6, 20);
        float thick = Mathf.clamp(0.75f, 2f, size / 22f);

        return new Effect(37f, e -> {
            color(e.color, Color.white, e.fout() * 0.7f);
            rand.setSeed(e.id);
            WHUtils.randLenVectors(e.id, num, 4 + (size * 1.2f) * e.fin(), size * 0.15f * e.fin(), e.rotation, angleRange, (x, y) -> {
                Lines.stroke(thick * e.fout(0.32f));
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), (e.fslope() + e.fin()) * 0.5f * (size * rand.random(0.15f, 0.5f) + rand.random(2f)) + rand.random(2f));
                Drawf.light(e.x + x, e.y + y, e.fslope() * (size * 0.5f + 14f) + 3, e.color, 0.7f);
            });
        });
    }
    public static Effect genericCharge(Color color, float size, float range, float lifetime){
        return new Effect(lifetime, e -> {
            color(color);
            Lines.stroke(size / 7f * e.fin());
            randLenVectors(e.id, 15, 3f + 60f * e.fout(), e.rotation, range, (x, y) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * size + size / 4f);
                Drawf.light(e.x + x, e.y + y, e.fout(0.25f) * size, color, 0.7f);
            });

            Fill.circle(e.x, e.y, size * 0.48f * Interp.pow3Out.apply(e.fin()));

        });
    }
    public static Effect arcShieldBreak = new Effect(40, e -> {
        Lines.stroke(3 * e.fout(), e.color);
        if(e.data instanceof Unit u){
            PcShieldArcAbility ab = (PcShieldArcAbility) Structs.find(u.abilities, a -> a instanceof PcShieldArcAbility);
            if(ab != null){
                Vec2 pos = Tmp.v1.set(ab.x, ab.y).rotate(u.rotation - 90f).add(u);
                Lines.arc(pos.x, pos.y, ab.radius + ab.width/2, ab.angle / 360f, u.rotation + ab.angleOffset - ab.angle / 2f);
                Lines.arc(pos.x, pos.y, ab.radius - ab.width/2, ab.angle / 360f, u.rotation + ab.angleOffset - ab.angle / 2f);
                for(int i : Mathf.signs){
                    float
                            px = pos.x + Angles.trnsx(u.rotation + ab.angleOffset - ab.angle / 2f * i, ab.radius + ab.width / 2),
                            py = pos.y + Angles.trnsy(u.rotation + ab.angleOffset - ab.angle / 2f * i, ab.radius + ab.width / 2),
                            px1 = pos.x + Angles.trnsx(u.rotation + ab.angleOffset - ab.angle / 2f * i, ab.radius - ab.width / 2),
                            py1 = pos.y + Angles.trnsy(u.rotation + ab.angleOffset - ab.angle / 2f * i, ab.radius - ab.width / 2);
                    Lines.line(px, py, px1, py1);
                }
            }
        }
    });
    public static Effect ExplosionSlash(Color color,float size, float range, float lifetime) {
        return new Effect(lifetime, range*2, e -> {
            Draw.color(color);
            Angles.randLenVectors(e.id, (int) Mathf.clamp(range / 8.0F, 4.0F, 18.0F), range / 8.0F, range * (1.0F + e.fout(Interp.pow2OutInverse)) / 2.0F, (x, y) -> {
                for (int s : Mathf.signs) {
                    Drawf.tri(e.x, e.y, e.fout() * size/2, e.foutpow() * size * 2.5f + 6f, e.rotation + s * 90f);
                }
            });
        });
    }
    static {
        lightningSpark = new Effect(Fx.chainLightning.lifetime, (e) -> {
            Draw.color(Color.white, e.color, e.fin() + 0.25F);
            Lines.stroke(0.65F + e.fout());
            Angles.randLenVectors(e.id, 3, e.fin() * e.rotation + 6.0F, (x, y) -> {
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 4.0F + 2.0F);
            });
            Fill.circle(e.x, e.y, 2.5F * e.fout());
        });
        TankAG7BulletExplode = (new Effect(300.0F, 1600.0F, (e) -> {
            float rad = 120.0F;
            rand.setSeed(e.id);
            Draw.color(Color.white, e.color, e.fin() + 0.6F);
            float circleRad = e.fin(Interp.circleOut) * rad * 4.0F;
            Lines.stroke(12.0F * e.fout());
            Lines.circle(e.x, e.y, circleRad);

            for (int i = 0; i < 24; ++i) {
                Tmp.v1.set(1.0F, 0.0F).setToRandomDirection(rand).scl(circleRad);
                Drawn.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16.0F, circleRad / 12.0F) * e.fout(), rand.random(circleRad / 4.0F, circleRad / 1.5F) * (1.0F + e.fin()) / 2.0F, Tmp.v1.angle() - 180.0F);
            }

            Draw.blend(Blending.additive);
            Draw.z(110.1F);
            Fill.light(e.x, e.y, Lines.circleVertices(circleRad), circleRad, Color.clear, Tmp.c1.set(Draw.getColor()).a(e.fout(Interp.pow10Out)));
            Draw.blend();
            Draw.z(110.0F);
            e.scaled(120.0F, (ix) -> {
                Draw.color(Color.white, ix.color, ix.fin() + 0.4F);
                Fill.circle(ix.x, ix.y, rad * ix.fout());
                Lines.stroke(18.0F * ix.fout());
                Lines.circle(ix.x, ix.y, ix.fin(Interp.circleOut) * rad * 1.1F);
                Angles.randLenVectors(ix.id, 40, rad / 3.0F, rad * ix.fin(Interp.pow2Out), (x, y) -> {
                    Lines.lineAngle(ix.x + x, ix.y + y, Mathf.angle(x, y), ix.fslope() * 25.0F + 10.0F);
                });
                Angles.randLenVectors(ix.id, (int) (rad / 4.0F), rad / 6.0F, rad * (1.0F + ix.fout(Interp.circleOut)) / 1.5F, (x, y) -> {
                    float angle = Mathf.angle(x, y);
                    float width = ix.foutpowdown() * rand.random(rad / 6.0F, rad / 3.0F);
                    float length = rand.random(rad / 2.0F, rad * 5.0F) * ix.fout(Interp.circleOut);
                    Draw.color(ix.color);
                    Drawn.tri(ix.x + x, ix.y + y, width, rad / 3.0F * ix.fout(Interp.circleOut), angle - 180.0F);
                    Drawn.tri(ix.x + x, ix.y + y, width, length, angle);
                    Draw.color(Color.black);
                    width *= ix.fout();
                    Drawn.tri(ix.x + x, ix.y + y, width / 2.0F, rad / 3.0F * ix.fout(Interp.circleOut) * 0.9F * ix.fout(), angle - 180.0F);
                    Drawn.tri(ix.x + x, ix.y + y, width / 2.0F, length / 1.5F * ix.fout(), angle);
                });
                Draw.color(Color.black);
                Fill.circle(ix.x, ix.y, rad * ix.fout() * 0.75F);
            });
            Drawf.light(e.x, e.y, rad * e.fout(Interp.circleOut) * 4.0F, e.color, 0.7F);
        })).layer(110.001F);


        posLightning = (new Effect(PositionLightning.lifetime, 1200.0F, (e) -> {
            Object patt6351$temp = e.data;
            if (patt6351$temp instanceof Vec2Seq) {
                Vec2Seq v = (Vec2Seq) patt6351$temp;
                Draw.color(e.color, Color.white, e.fout() * 0.6F);
                Lines.stroke(e.rotation * e.fout());
                Fill.circle(v.firstTmp().x, v.firstTmp().y, Lines.getStroke() / 2.0F);

                for (int i = 0; i < v.size() - 1; ++i) {
                    Vec2 cur = v.setVec2(i, Tmp.v1);
                    Vec2 next = v.setVec2(i + 1, Tmp.v2);
                    Lines.line(cur.x, cur.y, next.x, next.y, false);
                    Fill.circle(next.x, next.y, Lines.getStroke() / 2.0F);
                }

            }
        })).layer(109.999F);
        attackWarningRange = new Effect(120.0F, 2000.0F, (e) -> {
            Draw.color(e.color);
            Lines.stroke(2.0F * e.fout());
            Lines.circle(e.x, e.y, e.rotation);

            for (float ix = 0.75F; ix < 1.5F; ix += 0.25F) {
                Lines.spikes(e.x, e.y, e.rotation / ix, e.rotation / 10.0F, 4, e.time);
                Lines.spikes(e.x, e.y, e.rotation / ix / 1.5F, e.rotation / 12.0F, 4, -e.time * 1.25F);
            }

            TextureRegion arrowRegion = Core.atlas.find("wh-jump-gate-arrow");
            float scl = Mathf.curve(e.fout(), 0.0F, 0.1F);

            for (int l = 0; l < 4; ++l) {
                float angle = (float) (90 * l);
                float regSize = e.rotation / 150.0F;

                for (int i = 0; i < 4; ++i) {
                    Tmp.v1.trns(angle, (float) ((i - 4) * 8) * e.rotation / 8.0F / 4.0F);
                    float f = (100.0F - (Time.time - (float) (25 * i)) % 100.0F) / 100.0F;
                    Draw.rect(arrowRegion, e.x + Tmp.v1.x, e.y + Tmp.v1.y, (float) arrowRegion.width * regSize * f * scl, (float) arrowRegion.height * regSize * f * scl, angle - 90.0F);
                }
            }

        });
        attackWarningPos = new Effect(120.0F, 2000.0F, (e) -> {
            Object patt8300$temp = e.data;
            if (patt8300$temp instanceof Position) {
                Position p = (Position) patt8300$temp;
                e.lifetime = e.rotation;
                Draw.color(e.color);
                TextureAtlas.AtlasRegion arrowRegion = Core.atlas.find("wh-jump-gate-arrow");
                float scl = Mathf.curve(e.fout(), 0.0F, 0.1F);
                Lines.stroke(2.0F * scl);
                Lines.line(p.getX(), p.getY(), e.x, e.y);
                Fill.circle(p.getX(), p.getY(), Lines.getStroke());
                Fill.circle(e.x, e.y, Lines.getStroke());
                Tmp.v1.set(e.x, e.y).sub(p).scl(e.fin(Interp.pow2In)).add(p);
                Draw.rect(arrowRegion, Tmp.v1.x, Tmp.v1.y, (float) arrowRegion.width * scl * Draw.scl, (float) arrowRegion.height * scl * Draw.scl, p.angleTo(e.x, e.y) - 90.0F);
            }
        });
        energyUnitBlast = (new Effect(150.0F, 1600.0F, (e) -> {
            float rad = e.rotation;
            rand.setSeed(e.id);
            Draw.color(Color.white, e.color, e.fin() / 5.0F + 0.6F);
            float circleRad = e.fin(Interp.circleOut) * rad;
            Lines.stroke(12.0F * e.fout());
            Lines.circle(e.x, e.y, circleRad);
            e.scaled(120.0F, (i) -> {
                Fill.circle(i.x, i.y, rad * i.fout() / 2.0F);
                Lines.stroke(18.0F * i.fout());
                Lines.circle(i.x, i.y, i.fin(Interp.circleOut) * rad * 1.2F);
                Angles.randLenVectors(i.id, (int) (rad / 4.0F), rad / 6.0F, rad * (1.0F + i.fout(Interp.circleOut)) / 2.0F, (x, y) -> {
                    float angle = Mathf.angle(x, y);
                    float width = i.foutpowdown() * rand.random(rad / 8.0F, rad / 10.0F);
                    float length = rand.random(rad / 2.0F, rad) * i.fout(Interp.circleOut);
                    Draw.color(i.color);
                    Drawn.tri(i.x + x, i.y + y, width, rad / 8.0F * i.fout(Interp.circleOut), angle - 180.0F);
                    Drawn.tri(i.x + x, i.y + y, width, length, angle);
                    Draw.color(Color.black);
                    width *= i.fout();
                    Drawn.tri(i.x + x, i.y + y, width / 2.0F, rad / 8.0F * i.fout(Interp.circleOut) * 0.9F * i.fout(), angle - 180.0F);
                    Drawn.tri(i.x + x, i.y + y, width / 2.0F, length / 1.5F * i.fout(), angle);
                });
                Draw.color(Color.black);
                Fill.circle(i.x, i.y, rad * i.fout() * 0.375F);
            });
            Drawf.light(e.x, e.y, rad * e.fout() * 4.0F * Mathf.curve(e.fin(), 0.0F, 0.05F), e.color, 0.7F);
        })).layer(110.001F);
        circle = new Effect(25.0F, (e) -> {
            Draw.color(e.color, Color.white, e.fout() * 0.65F);
            Lines.stroke(Mathf.clamp(e.rotation / 18.0F, 2.0F, 6.0F) * e.fout());
            Lines.circle(e.x, e.y, e.rotation * e.finpow());
        });
        circleOut = new Effect(60.0F, 500.0F, (e) -> {
            Lines.stroke(2.5F * e.fout(), e.color);
            Lines.circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
        });
        circleOutQuick = new Effect(30.0F, 500.0F, (e) -> {
            Lines.stroke(2.5F * e.fout(), e.color);
            Lines.circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
        });
        circleOutLong = new Effect(120.0F, 500.0F, (e) -> {
            Lines.stroke(2.5F * e.fout(), e.color);
            Lines.circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
        });
        circleSplash = new Effect(26.0F, (e) -> {
            Draw.color(Color.white, e.color, e.fin() + 0.15F);
            Angles.randLenVectors(e.id, 4, 3.0F + 23.0F * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 3.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 3.5F, e.color, 0.7F);
            });
        });
        missileShoot = new Effect(130.0F, 300.0F, (e) -> {
            Draw.color(e.color);
            Draw.alpha(0.67F * e.fout(0.9F));
            rand.setSeed(e.id);

            for (int i = 0; i < 35; ++i) {
                v9.trns(e.rotation + 180.0F + rand.range(21.0F), rand.random(e.finpow() * 90.0F)).add(rand.range(3.0F), rand.range(3.0F));
                e.scaled(e.lifetime * rand.random(0.2F, 1.0F), (b) -> {
                    Fill.circle(e.x + v9.x, e.y + v9.y, b.fout() * 9.0F + 0.3F);
                });
            }

        });
        shuttle = new Effect(70.0F, 800.0F, (e) -> {
            Object patt12867$temp = e.data;
            if (patt12867$temp instanceof Float) {
                Float len = (Float) patt12867$temp;
                Draw.color(e.color, Color.white, e.fout() * 0.3F);
                Lines.stroke(e.fout() * 2.2F);
                Angles.randLenVectors(e.id, (int) Mathf.clamp(len / 12.0F, 10.0F, 40.0F), e.finpow() * len, e.rotation, 360.0F, (x, y) -> {
                    float ang = Mathf.angle(x, y);
                    Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * len * 0.15F + len * 0.025F);
                });
                float fout = e.fout(Interp.exp10Out);
                int[] var3 = Mathf.signs;
                int var4 = var3.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    int i = var3[var5];
                    Drawn.tri(e.x, e.y, len / 17.0F * fout * (Mathf.absin(0.8F, 0.07F) + 1.0F), len * 3.0F * Interp.swingOut.apply(Mathf.curve(e.fin(), 0.0F, 0.7F)) * (Mathf.absin(0.8F, 0.12F) + 1.0F) * e.fout(0.2F), e.rotation + 90.0F + (float) (i * 90));
                }

            }
        });
        shuttleDark = (new Effect(70.0F, 800.0F, (e) -> {
            Object patt13777$temp = e.data;
            if (patt13777$temp instanceof Float) {
                Float len = (Float) patt13777$temp;
                Draw.color(e.color, Color.white, e.fout() * 0.3F);
                Lines.stroke(e.fout() * 2.2F);
                Angles.randLenVectors(e.id, (int) Mathf.clamp(len / 12.0F, 10.0F, 40.0F), e.finpow() * len, e.rotation, 360.0F, (x, y) -> {
                    float ang = Mathf.angle(x, y);
                    Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * len * 0.15F + len * 0.025F);
                });
                float fout = e.fout(Interp.exp10Out);
                int[] var3 = Mathf.signs;
                int var4 = var3.length;

                int var5;
                int i;
                for (var5 = 0; var5 < var4; ++var5) {
                    i = var3[var5];
                    Drawn.tri(e.x, e.y, len / 17.0F * fout * (Mathf.absin(0.8F, 0.07F) + 1.0F), len * 3.0F * Interp.swingOut.apply(Mathf.curve(e.fin(), 0.0F, 0.7F)) * (Mathf.absin(0.8F, 0.12F) + 1.0F) * e.fout(0.2F), e.rotation + 90.0F + (float) (i * 90));
                }

                float len1 = len * 0.66F;
                Draw.z(110.0001F);
                Draw.color(Color.black);
                int[] var10 = Mathf.signs;
                var5 = var10.length;

                int ix;
                for (i = 0; i < var5; ++i) {
                    ix = var10[i];
                    Drawn.tri(e.x, e.y, len1 / 17.0F * fout * (Mathf.absin(0.8F, 0.07F) + 1.0F), len1 * 3.0F * Interp.swingOut.apply(Mathf.curve(e.fin(), 0.0F, 0.7F)) * (Mathf.absin(0.8F, 0.12F) + 1.0F) * e.fout(0.2F), e.rotation + 90.0F + (float) (ix * 90));
                }

                Draw.z(99.89F);
                var10 = Mathf.signs;
                var5 = var10.length;

                for (i = 0; i < var5; ++i) {
                    ix = var10[i];
                    Drawn.tri(e.x, e.y, len1 / 17.0F * fout * (Mathf.absin(0.8F, 0.07F) + 1.0F), len1 * 3.0F * Interp.swingOut.apply(Mathf.curve(e.fin(), 0.0F, 0.7F)) * (Mathf.absin(0.8F, 0.12F) + 1.0F) * e.fout(0.2F), e.rotation + 90.0F + (float) (ix * 90));
                }

            }
        })).layer(109.0F);
        shuttleLerp = new Effect(180.0F, 800.0F, (e) -> {
            Object patt15476$temp = e.data;
            if (patt15476$temp instanceof Float) {
                Float len = (Float) patt15476$temp;
                float f = Mathf.curve(e.fin(Interp.pow5In), 0.0F, 0.07F) * Mathf.curve(e.fout(), 0.0F, 0.4F);
                Draw.color(e.color);
                v7.trns(e.rotation - 90.0F, (len + Mathf.randomSeed(e.id, 0.0F, len)) * e.fin(Interp.circleOut));
                int[] var3 = Mathf.signs;
                int var4 = var3.length;

                for (int var5 = 0; var5 < var4; ++var5) {
                    int i = var3[var5];
                    Drawn.tri(e.x + v7.x, e.y + v7.y, Mathf.clamp(len / 8.0F, 8.0F, 25.0F) * (f + e.fout(0.2F) * 2.0F) / 3.5F, len * 1.75F * e.fin(Interp.circleOut), e.rotation + 90.0F + (float) (i * 90));
                }

            }
        });
        lightningHitSmall = new Effect(Fx.chainLightning.lifetime, (e) -> {
            Draw.color(Color.white, e.color, e.fin() + 0.25F);
            e.scaled(7.0F, (s) -> {
                Lines.stroke(0.5F + s.fout());
                Lines.circle(e.x, e.y, s.fin() * (e.rotation + 12.0F));
            });
            Lines.stroke(0.75F + e.fout());
            Angles.randLenVectors(e.id, 6, e.fin() * e.rotation + 7.0F, (x, y) -> {
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 4.0F + 2.0F);
            });
            Fill.circle(e.x, e.y, 2.5F * e.fout());
        });
        lightningHitLarge = new Effect(50.0F, 180.0F, (e) -> {
            Draw.color(e.color);
            Drawf.light(e.x, e.y, e.fout() * 90.0F, e.color, 0.7F);
            e.scaled(25.0F, (t) -> {
                Lines.stroke(3.0F * t.fout());
                Lines.circle(e.x, e.y, 3.0F + t.fin(Interp.pow3Out) * 80.0F);
            });
            Fill.circle(e.x, e.y, e.fout() * 8.0F);
            Angles.randLenVectors((e.id + 1), 4, 1.0F + 60.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 5.0F);
            });
            Draw.color(Color.gray);
            Angles.randLenVectors(e.id, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F);
            });
        });
        lightningFade = (new Effect(PositionLightning.lifetime, 1200.0F, (e) -> {
            Object patt17525$temp = e.data;
            if (patt17525$temp instanceof Vec2Seq) {
                Vec2Seq v = (Vec2Seq) patt17525$temp;
                e.lifetime = v.size() < 2 ? 0.0F : 1000.0F;
                int strokeOffset = (int) e.rotation;
                if (v.size() > strokeOffset + 1 && strokeOffset > 0 && v.size() > 2) {
                    v.removeRange(0, v.size() - strokeOffset - 1);
                }

                if (!Vars.state.isPaused() && v.any()) {
                    v.remove(0);
                }

                if (v.size() >= 2) {
                    Vec2 data = v.peekTmp();
                    float stroke = data.x;
                    float fadeOffset = data.y;
                    Draw.color(e.color);

                    for (int i = 1; i < v.size() - 1; ++i) {
                        Lines.stroke(Mathf.clamp(((float) i + fadeOffset / 2.0F) / (float) v.size() * (float) (strokeOffset - (v.size() - i)) / (float) strokeOffset) * stroke);
                        Vec2 from = v.setVec2(i - 1, Tmp.v1);
                        Vec2 to = v.setVec2(i, Tmp.v2);
                        Lines.line(from.x, from.y, to.x, to.y, false);
                        Fill.circle(from.x, from.y, Lines.getStroke() / 2.0F);
                    }

                    Vec2 last = v.tmpVec2(v.size() - 2);
                    Fill.circle(last.x, last.y, Lines.getStroke() / 2.0F);
                }
            }
        })).layer(109.999F);
        sapPlasmaShoot = new Effect(25.0F, (e) -> {
            Draw.color(Color.white, Pal.sapBullet, e.fin());
            Angles.randLenVectors(e.id, 13, e.finpow() * 20.0F, e.rotation, 23.0F, (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 5.0F);
                Fill.circle(e.x + x / 1.2F, e.y + y / 1.2F, e.fout() * 3.0F);
            });
        });
        coloredHitSmall = new Effect(14.0F, (e) -> {
            Draw.color(Color.white, e.color, e.fin());
            e.scaled(7.0F, (s) -> {
                Lines.stroke(0.5F + s.fout());
                Fill.circle(e.x, e.y, s.fin() * 5.0F);
            });
            Lines.stroke(0.5F + e.fout());
            Angles.randLenVectors(e.id, 5, e.fin() * 15.0F, (x, y) -> {
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 3.0F + 1.0F);
            });
        });
        hugeTrail = new Effect(40.0F, (e) -> {
            Draw.color(e.color);
            Draw.alpha(e.fout(0.85F) * 0.85F);
            Angles.randLenVectors(e.id, 6, 2.0F + e.rotation * 5.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout(Interp.pow3Out) * e.rotation);
            });
        });
        hugeSmokeGray = new Effect(40.0F, (e) -> {
            Draw.color(Color.gray, Color.darkGray, e.fin());
            Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout() * 2.0F);
            });
            e.scaled(25.0F, (i) -> {
                Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * i.finpow(), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, i.fout() * 4.0F);
                });
            });
        });
        hugeSmoke = new Effect(40.0F, (e) -> {
            Draw.color(e.color);
            Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout() * 2.0F);
            });
            e.scaled(25.0F, (i) -> {
                Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * i.finpow(), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, i.fout() * 4.0F);
                });
            });
        });
        hugeSmokeLong = new Effect(120.0F, (e) -> {
            Draw.color(e.color);
            Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout() * 2.0F);
            });
            e.scaled(25.0F, (i) -> {
                Angles.randLenVectors(e.id, 6, 2.0F + 19.0F * i.finpow(), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, i.fout() * 4.0F);
                });
            });
        });
        square45_4_45 = new Effect(45.0F, (e) -> {
            Draw.color(e.color);
            Angles.randLenVectors(e.id, 5, 20.0F * e.finpow(), (x, y) -> {
                Fill.square(e.x + x, e.y + y, 4.0F * e.fout(), 45.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 6.0F, e.color, 0.7F);
            });
        });
        square45_6_45 = new Effect(45.0F, (e) -> {
            Draw.color(e.color, Color.white, e.fout() * 0.6F);
            Angles.randLenVectors(e.id, 6, 27.0F * e.finpow(), (x, y) -> {
                Fill.square(e.x + x, e.y + y, 5.0F * e.fout(), 45.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 9.0F, e.color, 0.7F);
            });
        });
        square45_6_45_Charge = new Effect(90.0F, (e) -> {
            Draw.color(e.color, Color.white, e.fin() * 0.6F);
            Angles.randLenVectors(e.id, 12, 60.0F * e.fout(Interp.pow4Out), (x, y) -> {
                Fill.square(e.x + x, e.y + y, 5.0F * e.fin(), 45.0F);
                Drawf.light(e.x + x, e.y + y, e.fin() * 9.0F, e.color, 0.7F);
            });
            Lines.stroke(2.0F * e.fin());
            Lines.circle(e.x, e.y, 80.0F * e.fout(Interp.pow5Out));
        });
        square45_8_45 = new Effect(45.0F, (e) -> {
            Draw.color(e.color, Color.white, e.fout() * 0.6F);
            Angles.randLenVectors(e.id, 7, 34.0F * e.finpow(), (x, y) -> {
                Fill.square(e.x + x, e.y + y, 8.0F * e.fout(), 45.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 12.0F, e.color, 0.7F);
            });
        });
        crossBlastArrow45 = new Effect(65.0F, 140.0F, (e) -> {
            Draw.color(e.color, Color.white, e.fout() * 0.55F);
            Drawf.light(e.x, e.y, e.fout() * 70.0F, e.color, 0.7F);
            e.scaled(10.0F, (ix) -> {
                Lines.stroke(1.35F * ix.fout());
                Lines.circle(e.x, e.y, 49.0F * ix.finpow());
            });
            rand.setSeed(e.id);
            float sizeDiv = 138.0F;
            float randL = rand.random(sizeDiv);
            float f = Mathf.curve(e.fin(), 0.0F, 0.05F);

            for (int i = 0; i < 4; ++i) {
                Tmp.v1.trns((float) (45 + i * 90), 66.0F);
                Drawn.arrow(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 27.5F * (e.fout() * 3.0F + 1.0F) / 4.0F * e.fout(Interp.pow3In), (sizeDiv + randL) * f * e.fout(Interp.pow3), -randL / 6.0F * f, (float) (i * 90 + 45));
            }

        });
        ScrossBlastArrow45 = new Effect(80.0F, 50.0F, (e) -> {
            Draw.color(e.color, Color.white, e.fout() * 0.55F);
            Drawf.light(e.x, e.y, e.fout() * 70.0F, e.color, 0.7F);
            e.scaled(10.0F, (ix) -> {
                Lines.stroke(1.1F * ix.fout());
                Lines.circle(e.x, e.y, 78.0F * ix.finpow());
            });
            rand.setSeed(e.id);
            float sizeDiv = 60.0F;
            float randL = rand.random(sizeDiv);
            float f = Mathf.curve(e.fin(), 0.0F, 0.05F);

            for (int i = 0; i < 4; ++i) {
                Tmp.v1.trns((float) (45 + i * 90), 66.0F);
                Drawn.arrow(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 10.0F * (e.fout() * 3.0F + 1.0F) / 4.0F * e.fout(Interp.pow3In), (sizeDiv + randL) * f * e.fout(Interp.pow3), -randL / 6.0F * f, (float) (i * 90 + 45));
            }

        });
        crossBlast_45 = new Effect(35.0F, 140.0F, (e) -> {
            Draw.color(e.color, Color.white, e.fout() * 0.55F);
            Drawf.light(e.x, e.y, e.fout() * 70.0F, e.color, 0.7F);
            e.scaled(10.0F, (ix) -> {
                Lines.stroke(1.35F * ix.fout());
                Fill.circle(e.x, e.y, 55.0F * ix.finpow());
            });
            rand.setSeed(e.id);
            float sizeDiv = 60.0F;
            float randL = rand.random(sizeDiv);

            for (int i = 0; i < 4; ++i) {
                Drawn.tri(e.x, e.y, 5.85F * (e.fout() * 3.0F + 1.0F) / 4.0F * (e.fout(Interp.pow3In) + 0.5F) / 1.5F, (sizeDiv + randL) * Mathf.curve(e.fin(), 0.0F, 0.05F) * e.fout(Interp.pow3), (float) (i * 90 + 45));
            }

        });
        crossSpinBlast = (new Effect(150.0F, 600.0F, (e) -> {
            float scl = 1.0F;
            Draw.color(e.color, Color.white, e.fout() * 0.25F);
            float extend = Mathf.curve(e.fin(), 0.0F, 0.015F) * scl;
            float rot = e.fout(Interp.pow3In);
            int[] var4 = Mathf.signs;
            int var5 = var4.length;

            int var6;
            int i;
            for (var6 = 0; var6 < var5; ++var6) {
                i = var4[var6];
                Drawn.tri(e.x, e.y, 9.0F * e.foutpowdown() * scl, 100.0F + 300.0F * extend, e.rotation + 180.0F * rot + (float) (90 * i) + 45.0F);
                Drawn.tri(e.x, e.y, 9.0F * e.foutpowdown() * scl, 100.0F + 300.0F * extend, e.rotation + 180.0F * rot + (float) (90 * i) - 45.0F);
            }

            var4 = Mathf.signs;
            var5 = var4.length;

            for (var6 = 0; var6 < var5; ++var6) {
                i = var4[var6];
                Drawn.tri(e.x, e.y, 6.0F * e.foutpowdown() * scl, 40.0F + 120.0F * extend, e.rotation + 270.0F * rot + (float) (90 * i) + 45.0F);
                Drawn.tri(e.x, e.y, 6.0F * e.foutpowdown() * scl, 40.0F + 120.0F * extend, e.rotation + 270.0F * rot + (float) (90 * i) - 45.0F);
            }

        })).followParent(true).layer(100.0F);
        ultFireBurn = (new Effect(25.0F, (e) -> {
            Draw.color(Pal.techBlue, Color.gray, e.fin() * 0.75F);
            Angles.randLenVectors(e.id, 2, 2.0F + e.fin() * 7.0F, (x, y) -> {
                Fill.square(e.x + x, e.y + y, 0.2F + e.fout() * 1.5F, 45.0F);
            });
        })).layer(101.0F);
        skyTrail = new Effect(22.0F, (e) -> {
            Draw.color(Pal.techBlue, Pal.gray, e.fin() * 0.6F);
            rand.setSeed(e.id);
            Angles.randLenVectors(e.id, 3, e.finpow() * 13.0F, e.rotation - 180.0F, 30.0F, (x, y) -> {
                Fill.poly(e.x + x, e.y + y, 6, (float) rand.random(2, 4) * e.foutpow(), e.rotation);
            });
        });
        trailToGray = new Effect(50.0F, (e) -> {
            Draw.color(e.color, Color.gray, e.fin());
            Angles.randLenVectors(e.id, 2, 8.0F * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.rotation * e.fout());
            });
        });
        trailFromWhite = new Effect(50.0F, (e) -> {
            Draw.color(e.color, Color.white, e.fout() * 0.35F);
            Angles.randLenVectors(e.id, 2, 8.0F * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.rotation * e.fout());
            });
        });


        chainLightningFade = new Effect(220f, 500f, e -> {
            if (!(e.data instanceof Position)) return;
            Position p = e.data();
            float tx = p.getX(), ty = p.getY(), dst = Mathf.dst(e.x, e.y, tx, ty);
            Tmp.v1.set(p).sub(e.x, e.y).nor();

            e.lifetime = dst * 0.3f;
            float normx = Tmp.v1.x, normy = Tmp.v1.y;
            float range = e.rotation;
            int links = Mathf.ceil(dst / range);
            float spacing = dst / links;

            stroke(2.5f * Mathf.curve(e.fout(), 0, 0.7f));
            color(e.color, Color.white, e.fout() * 0.6f);

            beginLine();

            Fill.circle(e.x, e.y, getStroke() / 2);
            linePoint(e.x, e.y);

            rand.setSeed(e.id);

            float fin = Mathf.curve(e.fin(), 0, lightningAlign);
            int i;
            float nx = e.x, ny = e.y;
            for (i = 0; i < (int) (links * fin); i++) {
                if (i == links - 1) {
                    nx = tx;
                    ny = ty;
                } else {
                    float len = (i + 1) * spacing;
                    Tmp.v1.setToRandomDirection(rand).scl(range / 2f);
                    nx = e.x + normx * len + Tmp.v1.x;
                    ny = e.y + normy * len + Tmp.v1.y;
                }

                linePoint(nx, ny);
            }

            if (i < links) {
                float f = Mathf.clamp(fin * links % 1);
                float len = (i + 1) * spacing;
                Tmp.v1.setToRandomDirection(rand).scl(range / 2f);
                Tmp.v2.set(nx, ny);
                if (i == links - 1) Tmp.v2.lerp(tx, ty, f);
                else Tmp.v2.lerp(e.x + (normx * len + Tmp.v1.x), e.y + (normy * len + Tmp.v1.y), f);

                linePoint(Tmp.v2.x, Tmp.v2.y);
                Fill.circle(Tmp.v2.x, Tmp.v2.y, getStroke() / 2);
            }

            endLine();
        }).followParent(false);


        /**{@link Effect.EffectContainer} as Target */
        chainLightningFadeReversed = new Effect(220f, 500f, e -> {
            if (!(e.data instanceof Position)) return;
            Position p = e.data();
            float tx = e.x, ty = e.y, dst = Mathf.dst(p.getX(), p.getY(), tx, ty);
            Tmp.v1.set(e.x, e.y).sub(p).nor();

            e.lifetime = dst * 0.3f;
            float normx = Tmp.v1.x, normy = Tmp.v1.y;
            float range = e.rotation;
            int links = Mathf.ceil(dst / range);
            float spacing = dst / links;

            Lines.stroke(2.5f * Mathf.curve(e.fout(), 0, 0.7f));
            color(e.color, Color.white, e.fout() * 0.6f);

            Lines.beginLine();

            Fill.circle(p.getX(), p.getY(), Lines.getStroke() / 2);
            Lines.linePoint(p);

            rand.setSeed(e.id);

            float fin = Mathf.curve(e.fin(), 0, lightningAlign);
            int i;
            float nx = p.getX(), ny = p.getY();
            for (i = 0; i < (int) (links * fin); i++) {
                if (i == links - 1) {
                    nx = tx;
                    ny = ty;
                } else {
                    float len = (i + 1) * spacing;
                    Tmp.v1.setToRandomDirection(rand).scl(range / 2f);
                    nx = p.getX() + normx * len + Tmp.v1.x;
                    ny = p.getY() + normy * len + Tmp.v1.y;
                }

                linePoint(nx, ny);
            }

            if (i < links) {
                float f = Mathf.clamp(fin * links % 1);
                float len = (i + 1) * spacing;
                Tmp.v1.setToRandomDirection(rand).scl(range / 2f);
                Tmp.v2.set(nx, ny);
                if (i == links - 1) Tmp.v2.lerp(tx, ty, f);
                else Tmp.v2.lerp(p.getX() + (normx * len + Tmp.v1.x), p.getY() + (normy * len + Tmp.v1.y), f);

                linePoint(Tmp.v2.x, Tmp.v2.y);
                Fill.circle(Tmp.v2.x, Tmp.v2.y, getStroke() / 2);
            }

            Lines.endLine();
        }).followParent(false);


        techBlueCircleSplash = new Effect(26.0F, (e) -> {
            Draw.color(Pal.techBlue);
            Angles.randLenVectors(e.id, 4, 3.0F + 23.0F * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 3.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 3.5F, Pal.techBlue, 0.7F);
            });
        });
        techBlueExplosion = new Effect(40.0F, (e) -> {
            Draw.color(Pal.techBlue);
            e.scaled(20.0F, (i) -> {
                Lines.stroke(3.0F * i.foutpow());
                Lines.circle(e.x, e.y, 3.0F + i.finpow() * 80.0F);
            });
            Lines.stroke(e.fout());
            Angles.randLenVectors((e.id + 1), 8, 1.0F + 60.0F * e.finpow(), (x, y) -> {
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1.0F + e.fout() * 3.0F);
            });
            Draw.color(Color.gray);
            Angles.randLenVectors(e.id, 5, 2.0F + 70.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F);
            });
            Drawf.light(e.x, e.y, e.fout(Interp.pow2Out) * 100.0F, Pal.techBlue, 0.7F);
        });
        techBlueCharge = new Effect(130.0F, (e) -> {
            rand.setSeed(e.id);
            Angles.randLenVectors(e.id, 12, 140.0F * e.fout(Interp.pow3Out), (x, y) -> {
                Draw.color(Pal.techBlue);
                float rad = rand.random(9.0F, 18.0F);
                float scl = rand.random(0.6F, 1.0F);
                float dx = e.x + scl * x;
                float dy = e.y + scl * y;
                Fill.circle(dx, dy, e.fin() * rad);
                Draw.color(Pal.techBlue);
                Draw.z(110.0001F);
                Fill.circle(dx, dy, e.fin() * rad / 1.8F);
                Draw.z(99.89F);
                Fill.circle(dx, dy, e.fin() * rad / 1.8F);
                Draw.z(110.0F);
                Drawf.light(dx, dy, e.fin() * rad * 1.5F, Pal.techBlue, 0.7F);
            });
        });
        techBlueChargeBegin = new Effect(130.0F, (e) -> {
            Draw.color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fin() * 32.0F);
            Lines.stroke(e.fin() * 3.7F);
            Lines.circle(e.x, e.y, e.fout() * 80.0F);
            Draw.z(110.0001F);
            Draw.color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fin() * 20.0F);
            Draw.z(99.89F);
            Draw.color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fin() * 22.0F);
            Drawf.light(e.x, e.y, e.fin() * 35.0F, Pal.techBlue, 0.7F);
        });
        largeTechBlueHitCircle = new Effect(20.0F, (e) -> {
            Draw.color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 44.0F);
            Angles.randLenVectors(e.id, 5, 60.0F * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 8.0F);
            });
            Draw.color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 30.0F);
            Drawf.light(e.x, e.y, e.fout() * 55.0F, Pal.techBlue, 0.7F);
        });
        largeTechBlueHit = (new Effect(50.0F, (e) -> {
            Draw.color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 44.0F);
            Lines.stroke(e.fout() * 3.2F);
            Lines.circle(e.x, e.y, e.fin() * 80.0F);
            Lines.stroke(e.fout() * 2.5F);
            Lines.circle(e.x, e.y, e.fin() * 50.0F);
            Lines.stroke(e.fout() * 3.2F);
            Angles.randLenVectors(e.id, 30, 18.0F + 80.0F * e.fin(), (x, y) -> {
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14.0F + 5.0F);
            });
            Draw.z(110.0001F);
            Draw.color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 30.0F);
            Drawf.light(e.x, e.y, e.fout() * 80.0F, Pal.techBlue, 0.7F);
            Draw.z(99.89F);
            Fill.circle(e.x, e.y, e.fout() * 31.0F);
            Draw.z(109.9999F);
        })).layer(109.9999F);
        mediumTechBlueHit = new Effect(23.0F, (e) -> {
            Draw.color(Pal.techBlue);
            Lines.stroke(e.fout() * 2.8F);
            Lines.circle(e.x, e.y, e.fin() * 60.0F);
            Lines.stroke(e.fout() * 2.12F);
            Lines.circle(e.x, e.y, e.fin() * 35.0F);
            Lines.stroke(e.fout() * 2.25F);
            Angles.randLenVectors(e.id, 9, 7.0F + 60.0F * e.finpow(), (x, y) -> {
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4.0F + e.fout() * 12.0F);
            });
            Fill.circle(e.x, e.y, e.fout() * 22.0F);
            Draw.color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 14.0F);
            Drawf.light(e.x, e.y, e.fout() * 80.0F, Pal.techBlue, 0.7F);
        });
        techBlueSmokeBig = new Effect(30.0F, (e) -> {
            Draw.color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 32.0F);
            Draw.color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 20.0F);
            Drawf.light(e.x, e.y, e.fout() * 36.0F, Pal.techBlue, 0.7F);
        });
        techBlueShootBig = new Effect(40.0F, 100.0F, (e) -> {
            Draw.color(Pal.techBlue);
            Lines.stroke(e.fout() * 3.7F);
            Lines.circle(e.x, e.y, e.fin() * 100.0F + 15.0F);
            Lines.stroke(e.fout() * 2.5F);
            Lines.circle(e.x, e.y, e.fin() * 60.0F + 15.0F);
            Angles.randLenVectors(e.id, 15, 7.0F + 60.0F * e.finpow(), (x, y) -> {
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4.0F + e.fout() * 16.0F);
            });
            Drawf.light(e.x, e.y, e.fout() * 120.0F, Pal.techBlue, 0.7F);
        });
        healReceiveCircle = new Effect(11.0F, (e) -> {
            Draw.color(e.color);
            Lines.stroke(e.fout() * 1.667F);
            Lines.circle(e.x, e.y, 2.0F + e.finpow() * 7.0F);
        });
        healSendCircle = new Effect(22.0F, (e) -> {
            Draw.color(e.color);
            Lines.stroke(e.fout() * 2.0F);
            Lines.circle(e.x, e.y, e.finpow() * e.rotation);
        });
        jumpTrailOut = new Effect(120.0F, 200.0F, (e) -> {
            Object patt38666$temp = e.data;
            if (patt38666$temp instanceof UnitType) {
                UnitType type = (UnitType) patt38666$temp;
                Draw.color(type.engineColor == null ? e.color : type.engineColor);
                if (type.engineLayer > 0.0F) {
                    Draw.z(type.engineLayer);
                } else {
                    Draw.z((type.lowAltitude ? 90.0F : 115.0F) - 0.001F);
                }

                Tmp.v2.trns(e.rotation, 2300.0F);

                for (int index = 0; index < type.engines.size; ++index) {
                    UnitType.UnitEngine engine = (UnitType.UnitEngine) type.engines.get(index);
                    if (Angles.angleDist(engine.rotation, -90.0F) > 75.0F) {
                        return;
                    }

                    float ang = Mathf.slerp(engine.rotation, -90.0F, 0.75F);
                    Tmp.v1.trns(e.rotation, engine.y, -engine.x).add(Tmp.v2);
                    rand.setSeed(e.id);
                    e.scaled(80.0F, (i) -> {
                        Drawn.tri(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 3.0F * i.fout(Interp.slowFast), (float) (2300 + rand.range(120)), i.rotation + ang - 90.0F);
                        Fill.circle(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 3.0F * i.fout(Interp.slowFast));
                    });
                    Angles.randLenVectors(e.id + index, 42, 2330.0F, e.rotation + ang - 90.0F, 0.0F, (x, y) -> {
                        Lines.lineAngle(e.x + x + Tmp.v1.x, e.y + y + Tmp.v1.y, Mathf.angle(x, y), e.fout() * 60.0F);
                    });
                }

            }
        });
        AccretionDiskEffect = new Effect(60.0F, (e) -> {
            if (!Vars.headless) {
                Object patt55787$temp = e.data;
                if (patt55787$temp instanceof ateData) {
                    ateData data = (ateData) patt55787$temp;
                    if (data.owner != null) {
                        float fin = data.out ? e.finpow() : e.foutpow();
                        float fout = data.out ? e.foutpow() : e.finpow();
                        float start = Mathf.randomSeed(e.id, 360.0F);
                        Bullet b = data.owner;
                        float ioRad = data.outRad - (data.outRad - data.inRad) * fin;
                        float rad = data.speed * e.time * 8.0F;
                        float dx = dx(b.x, ioRad, start - rad);
                        float dy = dy(b.y, ioRad, start - rad);
                        if (data.trail == null) {
                            data.trail = new Trail(data.length);
                        }

                        float dzin = data.out && e.time > e.lifetime - 10.0F ? Interp.pow2Out.apply((e.lifetime - e.time) / 10.0F) : fin;
                        data.trail.length = data.length;
                        if (!Vars.state.isPaused()) {
                            data.trail.update(dx, dy, 2.0F);
                        }

                        float z = Draw.z();
                        Draw.z(110.0F - 19.0F * fout);
                        data.trail.draw(Tmp.c3.set(e.color).shiftValue(-e.color.value() * fout), data.width * dzin);
                        Draw.z(z);
                    }
                }
            }

        });
        groundRise = (new Effect(30.0F, (e) -> {
            Tile t = Vars.world.tileWorld(e.x, e.y);
            if (t != null) {
                Floor f = t.floor();
                if (!(f instanceof SteamVent)) {
                    TextureRegion region = f.variantRegions[Mathf.randomSeed(t.pos(), 0, Math.max(0, f.variantRegions.length - 1))];
                    float x = t.drawx();
                    float y = t.drawy() + e.rotation * e.fout();
                    Draw.z(Draw.z() - (float) t.y / (float) Vars.world.height() / 1000.0F);

                    for (int i = 0; i < region.width; ++i) {
                        PixmapRegion image = Core.atlas.getPixmap(region);
                        float c1 = Tmp.c1.set(image.get(i, 0)).toFloatBits();
                        float c2 = Tmp.c2.set(Tmp.c1).lerp(Color.black, e.fout() / 4.0F).toFloatBits();
                        float px = x - (float) region.width / 4.0F / 2.0F + (float) i / 4.0F;
                        float py = y - (float) region.height / 4.0F / 2.0F;
                        float by = py - e.rotation * e.fout();
                        float p = 0.125F;
                        Fill.quad(px - p, by, c2, px - p, py, c1, px + p, py, c1, px + p, by, c2);
                    }

                    Draw.rect(region, x, y);
                }
            }
        })).layer(0.01F);
        squSpark1 = new Effect(26.0F, (e) -> {
            rand.setSeed(e.id);
            Draw.color(Color.valueOf("788AD7FF"), Color.white, e.fin());
            Angles.randLenVectors(e.id, 3, 3.0F + 24.0F * e.fin(), 5.0F, (x, y) -> {
                float randN = rand.random(120.0F);
                Fill.poly(e.x + x, e.y + y, 4, e.fout() * 8.0F * rand.random(0.8F, 1.2F), e.rotation + randN * e.fin());
            });
        });
        triSpark2 = new Effect(26.0F, (e) -> {
            rand.setSeed(e.id);
            Draw.color(Color.valueOf("DBD58C"), Color.white, e.fin());
            Angles.randLenVectors(e.id, 3, 3.0F + 24.0F * e.fin(), 5.0F, (x, y) -> {
                float randN = rand.random(120.0F);
                Fill.poly(e.x + x, e.y + y, 3, e.fout() * 8.0F * rand.random(0.8F, 1.2F), e.rotation + randN * e.fin());
            });
        });
        tank3sMissileTrailSmoke = new Effect(180f, 300f, b -> {
            float intensity = 2f;
            color(b.color, 0.4f);
                rand.setSeed(b.id);
                float lenScl = rand.random(0.5f, 1f);
                b.scaled(b.lifetime * lenScl, e -> {
                    randLenVectors(e.id , e.fin(Interp.pow10Out), (int)(intensity*2), 10f * intensity, (x, y, in, out) -> {
                        float fout = e.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                        float rad = 4*intensity*fout;
                        Fill.circle(e.x + x, e.y + y, rad);
                        Drawf.light(e.x + x, e.y + y, rad * 2.5f, b.color, 0.5f);
                    });
                });
        }).layer(Layer.bullet - 0.5f);

        tank3sExplosionSmoke = new Effect(180f, 300f, b -> {
            float intensity = 2f;
            color(b.color, 0.7f);
            for(int i = 0; i < 4; i++){
                rand.setSeed(b.id*2 + i);
                float lenScl = rand.random(0.5f, 1f);
                int fi = i;
                b.scaled(b.lifetime * lenScl, e -> {
                    randLenVectors(e.id + fi - 1, e.fin(Interp.pow10Out), (int)( intensity), 15f * intensity, (x, y, in, out) -> {
                        float fout = e.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                        float rad = fout * ((2f + intensity) * 2.35f);

                        Fill.circle(e.x + x, e.y + y, rad);
                        Drawf.light(e.x + x, e.y + y, rad * 2.5f, b.color, 0.5f);
                    });
                });
            }
        });


    }
}
