package wh.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import mindustry.type.*;
import wh.graphics.*;
import wh.struct.*;

import java.util.*;

import static arc.Core.*;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;

@SuppressWarnings("unused")
public final class WHFx {
    public static final float EFFECT_MASK = Layer.effect + 0.0001f;
    public static final float EFFECT_BOTTOM = Layer.bullet - 0.11f;

    public static final float lightningAlign = 0.5f;

    public static final Rand rand = new Rand(), rand1 = new Rand(), rand2 = new Rand();
    public static final Vec2 v7 = new Vec2(), v8 = new Vec2(), v9 = new Vec2();

    public static final IntMap<Effect> same = new IntMap<>();

    public static final int[] oneArr = {1};

    private static int integer;

    public static Effect
            boolSelector = new Effect(0, 0, e -> {}),
            hitSpark = new Effect(45, e -> {
                Draw.color(e.color, Color.white, e.fout() * 0.3f);
                Lines.stroke(e.fout() * 1.6f);

                rand.setSeed(e.id);
                Angles.randLenVectors(e.id, 8, e.finpow() * 20f, (x, y) -> {
                    float ang = Mathf.angle(x, y);
                    Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * rand.random(1.95f, 4.25f) + 1f);
                });
            }),
            hitSparkLarge = new Effect(40, e -> {
                Draw.color(e.color, Color.white, e.fout() * 0.3f);
                Lines.stroke(e.fout() * 1.6f);

                rand.setSeed(e.id);
                Angles.randLenVectors(e.id, 18, e.finpow() * 27f, (x, y) -> {
                    float ang = Mathf.angle(x, y);
                    Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * rand.random(4, 8) + 2f);
                });
            }),
            hitSparkHuge = new Effect(70, e -> {
                Draw.color(e.color, Color.white, e.fout() * 0.3f);
                Lines.stroke(e.fout() * 1.6f);

                rand.setSeed(e.id);
                Angles.randLenVectors(e.id, 26, e.finpow() * 65f, (x, y) -> {
                    float ang = Mathf.angle(x, y);
                    Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * rand.random(6, 9) + 3f);
                });
            }),
            lightningSpark = new Effect(Fx.chainLightning.lifetime, e -> {
                Draw.color(Color.white, e.color, e.fin() + 0.25f);

                Lines.stroke(0.65f + e.fout());

                Angles.randLenVectors(e.id, 3, e.fin() * e.rotation + 6f, (x, y) -> Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 4 + 2f));

                Fill.circle(e.x, e.y, 2.5f * e.fout());
            }),
            collapserBulletExplode = new Effect(300F, 1600f, e -> {
                float rad = 150f;
                rand.setSeed(e.id);

                Draw.color(Color.white, e.color, e.fin() + 0.6f);
                float circleRad = e.fin(Interp.circleOut) * rad * 4f;
                Lines.stroke(12 * e.fout());
                Lines.circle(e.x, e.y, circleRad);
                for (int i = 0; i < 24; i++) {
                    Tmp.v1.set(1, 0).setToRandomDirection(rand).scl(circleRad);
                    Drawn.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16, circleRad / 12) * e.fout(), rand.random(circleRad / 4, circleRad / 1.5f) * (1 + e.fin()) / 2, Tmp.v1.angle() - 180);
                }

                Draw.blend(Blending.additive);
                Draw.z(Layer.effect + 0.1f);

                Fill.light(e.x, e.y, Lines.circleVertices(circleRad), circleRad, Color.clear, Tmp.c1.set(Draw.getColor()).a(e.fout(Interp.pow10Out)));
                Draw.blend();
                Draw.z(Layer.effect);

                e.scaled(120f, i -> {
                    Draw.color(Color.white, i.color, i.fin() + 0.4f);
                    Fill.circle(i.x, i.y, rad * i.fout());
                    Lines.stroke(18 * i.fout());
                    Lines.circle(i.x, i.y, i.fin(Interp.circleOut) * rad * 1.2f);
                    Angles.randLenVectors(i.id, 40, rad / 3, rad * i.fin(Interp.pow2Out), (x, y) -> {
                        Lines.lineAngle(i.x + x, i.y + y, Mathf.angle(x, y), i.fslope() * 25 + 10);
                    });

                    Angles.randLenVectors(i.id, (int) (rad / 4), rad / 6, rad * (1 + i.fout(Interp.circleOut)) / 1.5f, (x, y) -> {
                        float angle = Mathf.angle(x, y);
                        float width = i.foutpowdown() * rand.random(rad / 6, rad / 3);
                        float length = rand.random(rad / 2, rad * 5) * i.fout(Interp.circleOut);

                        Draw.color(i.color);
                        Drawn.tri(i.x + x, i.y + y, width, rad / 3 * i.fout(Interp.circleOut), angle - 180);
                        Drawn.tri(i.x + x, i.y + y, width, length, angle);

                        Draw.color(Color.black);

                        width *= i.fout();

                        Drawn.tri(i.x + x, i.y + y, width / 2, rad / 3 * i.fout(Interp.circleOut) * 0.9f * i.fout(), angle - 180);
                        Drawn.tri(i.x + x, i.y + y, width / 2, length / 1.5f * i.fout(), angle);
                    });

                    Draw.color(Color.black);
                    Fill.circle(i.x, i.y, rad * i.fout() * 0.75f);
                });

                Drawf.light(e.x, e.y, rad * e.fout(Interp.circleOut) * 4f, e.color, 0.7f);
            }).layer(Layer.effect + 0.001f),
            posLightning = (new Effect(PositionLightning.lifetime, 1200f, e -> {
                if (!(e.data instanceof Vec2Seq v)) return;

                Draw.color(e.color, Color.white, e.fout() * 0.6f);

                Lines.stroke(e.rotation * e.fout());

                Fill.circle(v.firstTmp().x, v.firstTmp().y, Lines.getStroke() / 2f);

                for (int i = 0; i < v.size() - 1; i++) {
                    Vec2 cur = v.setVec2(i, Tmp.v1);
                    Vec2 next = v.setVec2(i + 1, Tmp.v2);

                    Lines.line(cur.x, cur.y, next.x, next.y, false);
                    Fill.circle(next.x, next.y, Lines.getStroke() / 2f);
                }
            })).layer(Layer.effect - 0.001f),
            attackWarningRange = new Effect(120f, 2000f, e -> {
                Draw.color(e.color);
                Lines.stroke(2 * e.fout());
                Lines.circle(e.x, e.y, e.rotation);

                for (float i = 0.75f; i < 1.5f; i += 0.25f) {
                    Lines.spikes(e.x, e.y, e.rotation / i, e.rotation / 10f, 4, e.time);
                    Lines.spikes(e.x, e.y, e.rotation / i / 1.5f, e.rotation / 12f, 4, -e.time * 1.25f);
                }

                TextureRegion arrowRegion = atlas.find("wh-jump-gate-arrow");
                float scl = Mathf.curve(e.fout(), 0f, 0.1f);

                for (int l = 0; l < 4; l++) {
                    float angle = 90 * l;
                    float regSize = e.rotation / 150f;
                    for (int i = 0; i < 4; i++) {
                        Tmp.v1.trns(angle, (i - 4) * tilesize * e.rotation / tilesize / 4);
                        float f = (100 - (Time.time - 25 * i) % 100) / 100;

                        Draw.rect(arrowRegion, e.x + Tmp.v1.x, e.y + Tmp.v1.y, arrowRegion.width * regSize * f * scl, arrowRegion.height * regSize * f * scl, angle - 90);
                    }
                }
            }),
            attackWarningPos = new Effect(120f, 2000f, e -> {
                if (!(e.data instanceof Position p)) return;

                e.lifetime = e.rotation;

                Draw.color(e.color);
                TextureRegion arrowRegion = atlas.find("wh-jump-gate-arrow");
                float scl = Mathf.curve(e.fout(), 0f, 0.1f);
                Lines.stroke(2 * scl);
                Lines.line(p.getX(), p.getY(), e.x, e.y);
                Fill.circle(p.getX(), p.getY(), Lines.getStroke());
                Fill.circle(e.x, e.y, Lines.getStroke());
                Tmp.v1.set(e.x, e.y).sub(p).scl(e.fin(Interp.pow2In)).add(p);
                Draw.rect(arrowRegion, Tmp.v1.x, Tmp.v1.y, arrowRegion.width * scl * Draw.scl, arrowRegion.height * scl * Draw.scl, p.angleTo(e.x, e.y) - 90f);
            }),
            energyUnitBlast = new Effect(150f, 1600f, e -> {
                float rad = e.rotation;
                rand.setSeed(e.id);

                Draw.color(Color.white, e.color, e.fin() / 5 + 0.6f);
                float circleRad = e.fin(Interp.circleOut) * rad;
                Lines.stroke(12 * e.fout());
                Lines.circle(e.x, e.y, circleRad);

                e.scaled(120f, i -> {
                    Fill.circle(i.x, i.y, rad * i.fout() / 2);
                    Lines.stroke(18 * i.fout());
                    Lines.circle(i.x, i.y, i.fin(Interp.circleOut) * rad * 1.2f);

                    Angles.randLenVectors(i.id, (int) (rad / 4), rad / 6, rad * (1 + i.fout(Interp.circleOut)) / 2f, (x, y) -> {
                        float angle = Mathf.angle(x, y);
                        float width = i.foutpowdown() * rand.random(rad / 8, rad / 10);
                        float length = rand.random(rad / 2, rad) * i.fout(Interp.circleOut);

                        Draw.color(i.color);
                        Drawn.tri(i.x + x, i.y + y, width, rad / 8 * i.fout(Interp.circleOut), angle - 180);
                        Drawn.tri(i.x + x, i.y + y, width, length, angle);

                        Draw.color(Color.black);

                        width *= i.fout();

                        Drawn.tri(i.x + x, i.y + y, width / 2, rad / 8 * i.fout(Interp.circleOut) * 0.9f * i.fout(), angle - 180);
                        Drawn.tri(i.x + x, i.y + y, width / 2, length / 1.5f * i.fout(), angle);
                    });

                    Draw.color(Color.black);
                    Fill.circle(i.x, i.y, rad * i.fout() * 0.375f);
                });

                Drawf.light(e.x, e.y, rad * e.fout() * 4f * Mathf.curve(e.fin(), 0f, 0.05f), e.color, 0.7f);
            }).layer(Layer.effect + 0.001f),
            circle = new Effect(25f, e -> {
                Draw.color(e.color, Color.white, e.fout() * 0.65f);
                Lines.stroke(Mathf.clamp(e.rotation / 18f, 2, 6) * e.fout());
                Lines.circle(e.x, e.y, e.rotation * e.finpow());
            }),
            circleOut = new Effect(60f, 500f, e -> {
                Lines.stroke(2.5f * e.fout(), e.color);
                Lines.circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
            }),
            circleOutQuick = new Effect(30f, 500f, e -> {
                Lines.stroke(2.5f * e.fout(), e.color);
                Lines.circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
            }),
            circleOutLong = new Effect(120f, 500f, e -> {
                Lines.stroke(2.5f * e.fout(), e.color);
                Lines.circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
            }),
            circleSplash = new Effect(26f, e -> {
                Draw.color(Color.white, e.color, e.fin() + 0.15f);
                Angles.randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
                    Drawf.light(e.x + x, e.y + y, e.fout() * 3.5f, e.color, 0.7f);
                });
            }),
            missileShoot = new Effect(130f, 300f, e -> {
                Draw.color(e.color);
                Draw.alpha(0.67f * e.fout(0.9f));
                rand.setSeed(e.id);
                for (int i = 0; i < 35; i++) {
                    v9.trns(e.rotation + 180f + rand.range(21f), rand.random(e.finpow() * 90f)).add(rand.range(3f), rand.range(3f));
                    e.scaled(e.lifetime * rand.random(0.2f, 1f), b -> {
                        Fill.circle(e.x + v9.x, e.y + v9.y, b.fout() * 9f + 0.3f);
                    });
                }
            }),
            shuttle = new Effect(70f, 800f, e -> {
                if (!(e.data instanceof Float len)) return;

                Draw.color(e.color, Color.white, e.fout() * 0.3f);
                Lines.stroke(e.fout() * 2.2F);

                Angles.randLenVectors(e.id, (int) Mathf.clamp(len / 12, 10, 40), e.finpow() * len, e.rotation, 360f, (x, y) -> {
                    float ang = Mathf.angle(x, y);
                    Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * len * 0.15f + len * 0.025f);
                });

                float fout = e.fout(Interp.exp10Out);
                for (int i : Mathf.signs) {
                    Drawn.tri(e.x, e.y, len / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
                }
            }),
            shuttleDark = new Effect(70f, 800f, e -> {
                if (!(e.data instanceof Float len)) return;

                Draw.color(e.color, Color.white, e.fout() * 0.3f);
                Lines.stroke(e.fout() * 2.2F);

                Angles.randLenVectors(e.id, (int) Mathf.clamp(len / 12, 10, 40), e.finpow() * len, e.rotation, 360f, (x, y) -> {
                    float ang = Mathf.angle(x, y);
                    Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * len * 0.15f + len * 0.025f);
                });

                float fout = e.fout(Interp.exp10Out);
                for (int i : Mathf.signs) {
                    Drawn.tri(e.x, e.y, len / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
                }

                float len1 = len * 0.66f;
                Draw.z(EFFECT_MASK);
                Draw.color(Color.black);
                for (int i : Mathf.signs) {
                    Drawn.tri(e.x, e.y, len1 / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len1 * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
                }

                Draw.z(EFFECT_BOTTOM);
                for (int i : Mathf.signs) {
                    Drawn.tri(e.x, e.y, len1 / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len1 * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
                }
            }).layer(Layer.effect - 1f),
            shuttleLerp = new Effect(180f, 800f, e -> {
                if (!(e.data instanceof Float len)) return;
                float f = Mathf.curve(e.fin(Interp.pow5In), 0f, 0.07f) * Mathf.curve(e.fout(), 0f, 0.4f);

                Draw.color(e.color);
                v7.trns(e.rotation - 90, (len + Mathf.randomSeed(e.id, 0, len)) * e.fin(Interp.circleOut));
                for (int i : Mathf.signs)
                    Drawn.tri(e.x + v7.x, e.y + v7.y, Mathf.clamp(len / 8, 8, 25) * (f + e.fout(0.2f) * 2f) / 3.5f, len * 1.75f * e.fin(Interp.circleOut), e.rotation + 90 + i * 90);
            }),
            lightningHitSmall = new Effect(Fx.chainLightning.lifetime, e -> {
                Draw.color(Color.white, e.color, e.fin() + 0.25f);

                e.scaled(7f, s -> {
                    Lines.stroke(0.5f + s.fout());
                    Lines.circle(e.x, e.y, s.fin() * (e.rotation + 12f));
                });

                Lines.stroke(0.75f + e.fout());

                Angles.randLenVectors(e.id, 6, e.fin() * e.rotation + 7f, (x, y) -> {
                    Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 4 + 2f);
                });

                Fill.circle(e.x, e.y, 2.5f * e.fout());
            }),
            lightningHitLarge = new Effect(50f, 180f, e -> {
                Draw.color(e.color);
                Drawf.light(e.x, e.y, e.fout() * 90f, e.color, 0.7f);
                e.scaled(25f, t -> {
                    Lines.stroke(3f * t.fout());
                    Lines.circle(e.x, e.y, 3f + t.fin(Interp.pow3Out) * 80f);
                });
                Fill.circle(e.x, e.y, e.fout() * 8f);
                Angles.randLenVectors(e.id + 1, 4, 1f + 60f * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 5f));

                Draw.color(Color.gray);
                Angles.randLenVectors(e.id, 8, 2f + 30f * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4f + 0.5f));
            }),
            lightningFade = (new Effect(PositionLightning.lifetime, 1200f, e -> {
                if (!(e.data instanceof Vec2Seq v)) return;

                e.lifetime = v.size() < 2 ? 0 : 1000;
                int strokeOffset = (int) e.rotation;

                if (v.size() > strokeOffset + 1 && strokeOffset > 0 && v.size() > 2) {
                    v.removeRange(0, v.size() - strokeOffset - 1);
                }

                if (!state.isPaused() && v.any()) {
                    v.remove(0);
                }

                if (v.size() < 2) return;

                Vec2 data = v.peekTmp();
                float stroke = data.x;
                float fadeOffset = data.y;

                Draw.color(e.color);
                for (int i = 1; i < v.size() - 1; i++) {
                    Lines.stroke(Mathf.clamp((i + fadeOffset / 2f) / v.size() * (strokeOffset - (v.size() - i)) / strokeOffset) * stroke);
                    Vec2 from = v.setVec2(i - 1, Tmp.v1);
                    Vec2 to = v.setVec2(i, Tmp.v2);
                    Lines.line(from.x, from.y, to.x, to.y, false);
                    Fill.circle(from.x, from.y, Lines.getStroke() / 2);
                }

                Vec2 last = v.tmpVec2(v.size() - 2);
                Fill.circle(last.x, last.y, Lines.getStroke() / 2);
            })).layer(Layer.effect - 0.001f),
            sapPlasmaShoot = new Effect(25f, e -> {
                Draw.color(Color.white, Pal.sapBullet, e.fin());
                Angles.randLenVectors(e.id, 13, e.finpow() * 20f, e.rotation, 23f, (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, e.fout() * 5f);
                    Fill.circle(e.x + x / 1.2f, e.y + y / 1.2f, e.fout() * 3f);
                });
            }),
            coloredHitSmall = new Effect(14f, e -> {
                Draw.color(Color.white, e.color, e.fin());
                e.scaled(7f, s -> {
                    Lines.stroke(0.5f + s.fout());
                    Fill.circle(e.x, e.y, s.fin() * 5f);
                });

                Lines.stroke(0.5f + e.fout());
                Angles.randLenVectors(e.id, 5, e.fin() * 15f, (x, y) -> Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 3f + 1f));
            }),
            hugeTrail = new Effect(40f, e -> {
                Draw.color(e.color);
                Draw.alpha(e.fout(0.85f) * 0.85f);
                Angles.randLenVectors(e.id, 6, 2f + e.rotation * 5f * e.finpow(), (x, y) -> {
                    Fill.circle(e.x + x / 2f, e.y + y / 2f, e.fout(Interp.pow3Out) * e.rotation);
                });
            }),
            hugeSmokeGray = new Effect(40f, e -> {
                Draw.color(Color.gray, Color.darkGray, e.fin());
                Angles.randLenVectors(e.id, 6, 2f + 19f * e.finpow(), (x, y) -> Fill.circle(e.x + x / 2f, e.y + y / 2f, e.fout() * 2f));
                e.scaled(25f, i -> Angles.randLenVectors(e.id, 6, 2f + 19f * i.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, i.fout() * 4f)));
            }),
            hugeSmoke = new Effect(40f, e -> {
                Draw.color(e.color);
                Angles.randLenVectors(e.id, 6, 2f + 19f * e.finpow(), (x, y) -> Fill.circle(e.x + x / 2f, e.y + y / 2f, e.fout() * 2f));
                e.scaled(25f, i -> Angles.randLenVectors(e.id, 6, 2f + 19f * i.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, i.fout() * 4f)));
            }),
            hugeSmokeLong = new Effect(120f, e -> {
                Draw.color(e.color);
                Angles.randLenVectors(e.id, 6, 2f + 19f * e.finpow(), (x, y) -> Fill.circle(e.x + x / 2f, e.y + y / 2f, e.fout() * 2f));
                e.scaled(25f, i -> Angles.randLenVectors(e.id, 6, 2f + 19f * i.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, i.fout() * 4f)));
            }),
            square45_4_45 = new Effect(45f, e -> {
                Draw.color(e.color);
                Angles.randLenVectors(e.id, 5, 20f * e.finpow(), (x, y) -> {
                    Fill.square(e.x + x, e.y + y, 4f * e.fout(), 45);
                    Drawf.light(e.x + x, e.y + y, e.fout() * 6f, e.color, 0.7f);
                });
            }),
            square45_6_45 = new Effect(45f, e -> {
                Draw.color(e.color, Color.white, e.fout() * 0.6f);
                Angles.randLenVectors(e.id, 6, 27f * e.finpow(), (x, y) -> {
                    Fill.square(e.x + x, e.y + y, 5f * e.fout(), 45);
                    Drawf.light(e.x + x, e.y + y, e.fout() * 9F, e.color, 0.7f);
                });
            }),
            square45_6_45_Charge = new Effect(90f, e -> {
                Draw.color(e.color, Color.white, e.fin() * 0.6f);
                Angles.randLenVectors(e.id, 12, 60 * e.fout(Interp.pow4Out), (x, y) -> {
                    Fill.square(e.x + x, e.y + y, 5f * e.fin(), 45);
                    Drawf.light(e.x + x, e.y + y, e.fin() * 9f, e.color, 0.7f);
                });

                Lines.stroke(2f * e.fin());
                Lines.circle(e.x, e.y, 80 * e.fout(Interp.pow5Out));
            }),
            square45_8_45 = new Effect(45f, e -> {
                Draw.color(e.color, Color.white, e.fout() * 0.6f);
                Angles.randLenVectors(e.id, 7, 34f * e.finpow(), (x, y) -> {
                    Fill.square(e.x + x, e.y + y, 8f * e.fout(), 45);
                    Drawf.light(e.x + x, e.y + y, e.fout() * 12f, e.color, 0.7f);
                });
            }),
            crossBlastArrow45 = new Effect(65, 140, e -> {
                color(e.color, Color.white, e.fout() * 0.55f);
                Drawf.light(e.x, e.y, e.fout() * 70, e.color, 0.7f);

                e.scaled(10f, i -> {
                    stroke(1.35f * i.fout());
                    circle(e.x, e.y, 49 * i.finpow());
                });

                rand.setSeed(e.id);
                float sizeDiv = 138;
                float randL = rand.random(sizeDiv);

                float f = Mathf.curve(e.fin(), 0, 0.05f);

                for(int i = 0; i < 4; i++){
                    Tmp.v1.trns(45 + i * 90, 66);
                    Drawn.arrow(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 27.5f * (e.fout() * 3f + 1) / 4 * e.fout(Interp.pow3In), (sizeDiv + randL) * f * e.fout(Interp.pow3), -randL / 6f * f, i * 90 + 45);
                }
            }),
            crossBlast_45 = new Effect(35, 140, e -> {
                Draw.color(e.color, Color.white, e.fout() * 0.55f);
                Drawf.light(e.x, e.y, e.fout() * 70, e.color, 0.7f);

                e.scaled(10f, i -> {
                    Lines.stroke(1.35f * i.fout());
                    Fill.circle(e.x, e.y, 55 * i.finpow());
                });

                rand.setSeed(e.id);
                float sizeDiv = 60;
                float randL = rand.random(sizeDiv);

                for (int i = 0; i < 4; i++) {
                    Drawn.tri(e.x, e.y, 5.85f * (e.fout() * 3f + 1) / 4 * (e.fout(Interp.pow3In) + 0.5f) / 1.5f, (sizeDiv + randL) * Mathf.curve(e.fin(), 0, 0.05f) * e.fout(Interp.pow3), i * 90 + 45);
                }
            }),
            crossSpinBlast = (new Effect(150f, 600f, e -> {
                float scl = 1f;
                Draw.color(e.color, Color.white, e.fout() * 0.25f);
                float extend = Mathf.curve(e.fin(), 0f, 0.015f) * scl;
                float rot = e.fout(Interp.pow3In);

                for (int i : Mathf.signs) {
                    Drawn.tri(e.x, e.y, 9f * e.foutpowdown() * scl, 100f + 300f * extend, e.rotation + 180f * rot + (float) (90 * i) + 45f);
                    Drawn.tri(e.x, e.y, 9f * e.foutpowdown() * scl, 100f + 300f * extend, e.rotation + 180f * rot + (float) (90 * i) - 45f);
                }

                for (int i : Mathf.signs) {
                    Drawn.tri(e.x, e.y, 6f * e.foutpowdown() * scl, 40f + 120f * extend, e.rotation + 270f * rot + (float) (90 * i) + 45f);
                    Drawn.tri(e.x, e.y, 6f * e.foutpowdown() * scl, 40f + 120f * extend, e.rotation + 270f * rot + (float) (90 * i) - 45f);
                }
            })).followParent(true).layer(100f),
            ultFireBurn = new Effect(25f, e -> {
                Draw.color(Pal.techBlue, Color.gray, e.fin() * 0.75f);

                Angles.randLenVectors(e.id, 2, 2f + e.fin() * 7f, (x, y) -> {
                    Fill.square(e.x + x, e.y + y, 0.2f + e.fout() * 1.5f, 45);
                });
            }).layer(Layer.bullet + 1),
            skyTrail = new Effect(22, e -> {
                Draw.color(Pal.techBlue, Pal.gray, e.fin() * 0.6f);

                rand.setSeed(e.id);
                Angles.randLenVectors(e.id, 3, e.finpow() * 13, e.rotation - 180, 30f, (x, y) -> {
                    Fill.poly(e.x + x, e.y + y, 6, rand.random(2, 4) * e.foutpow(), e.rotation);
                });
            }),
            trailToGray = new Effect(50f, e -> {
                Draw.color(e.color, Color.gray, e.fin());
                Angles.randLenVectors(e.id, 2, tilesize * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.rotation * e.fout()));
            }),
            trailFromWhite = new Effect(50f, e -> {
                Draw.color(e.color, Color.white, e.fout() * 0.35f);
                Angles.randLenVectors(e.id, 2, tilesize * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.rotation * e.fout()));
            }),
            chainLightningFade = new Effect(220f, 500f, e -> {
                if (!(e.data instanceof Position p)) return;
                float tx = p.getX(), ty = p.getY(), dst = Mathf.dst(e.x, e.y, tx, ty);
                Tmp.v1.set(p).sub(e.x, e.y).nor();

                e.lifetime = dst * 0.3f;
                float normx = Tmp.v1.x, normy = Tmp.v1.y;
                float range = e.rotation;
                int links = Mathf.ceil(dst / range);
                float spacing = dst / links;

                Lines.stroke(2.5f * Mathf.curve(e.fout(), 0, 0.7f));
                Draw.color(e.color, Color.white, e.fout() * 0.6f);

                Lines.beginLine();

                Fill.circle(e.x, e.y, Lines.getStroke() / 2);
                Lines.linePoint(e.x, e.y);

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

                    Lines.linePoint(nx, ny);
                }

                if (i < links) {
                    float f = Mathf.clamp(fin * links % 1);
                    float len = (i + 1) * spacing;
                    Tmp.v1.setToRandomDirection(rand).scl(range / 2f);
                    Tmp.v2.set(nx, ny);
                    if (i == links - 1) Tmp.v2.lerp(tx, ty, f);
                    else Tmp.v2.lerp(e.x + (normx * len + Tmp.v1.x), e.y + (normy * len + Tmp.v1.y), f);

                    Lines.linePoint(Tmp.v2.x, Tmp.v2.y);
                    Fill.circle(Tmp.v2.x, Tmp.v2.y, Lines.getStroke() / 2);
                }

                Lines.endLine();
            }).followParent(false),
            chainLightningFadeReversed = new Effect(220f, 500f, e -> {
                if (!(e.data instanceof Position p)) return;

                float tx = e.x, ty = e.y, dst = Mathf.dst(p.getX(), p.getY(), tx, ty);
                Tmp.v1.set(e.x, e.y).sub(p).nor();

                e.lifetime = dst * 0.3f;
                float normx = Tmp.v1.x, normy = Tmp.v1.y;
                float range = e.rotation;
                int links = Mathf.ceil(dst / range);
                float spacing = dst / links;

                Lines.stroke(2.5f * Mathf.curve(e.fout(), 0, 0.7f));
                Draw.color(e.color, Color.white, e.fout() * 0.6f);

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

                    Lines.linePoint(nx, ny);
                }

                if (i < links) {
                    float f = Mathf.clamp(fin * links % 1);
                    float len = (i + 1) * spacing;
                    Tmp.v1.setToRandomDirection(rand).scl(range / 2f);
                    Tmp.v2.set(nx, ny);
                    if (i == links - 1) Tmp.v2.lerp(tx, ty, f);
                    else Tmp.v2.lerp(p.getX() + (normx * len + Tmp.v1.x), p.getY() + (normy * len + Tmp.v1.y), f);

                    Lines.linePoint(Tmp.v2.x, Tmp.v2.y);
                    Fill.circle(Tmp.v2.x, Tmp.v2.y, Lines.getStroke() / 2);
                }

                Lines.endLine();
            }).followParent(false),
            techBlueCircleSplash = new Effect(26f, e -> {
                Draw.color(Pal.techBlue);
                Angles.randLenVectors(e.id, 4, 3 + 23 * e.fin(), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, e.fout() * 3f);
                    Drawf.light(e.x + x, e.y + y, e.fout() * 3.5f, Pal.techBlue, 0.7f);
                });
            }),
            techBlueExplosion = new Effect(40, e -> {
                Draw.color(Pal.techBlue);
                e.scaled(20, i -> {
                    Lines.stroke(3f * i.foutpow());
                    Lines.circle(e.x, e.y, 3f + i.finpow() * 80f);
                });

                Lines.stroke(e.fout());
                Angles.randLenVectors(e.id + 1, 8, 1f + 60f * e.finpow(), (x, y) -> Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + e.fout() * 3f));

                Draw.color(Color.gray);

                Angles.randLenVectors(e.id, 5, 2f + 70 * e.finpow(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 4f + 0.5f));

                Drawf.light(e.x, e.y, e.fout(Interp.pow2Out) * 100f, Pal.techBlue, 0.7f);
            }),
            techBlueCharge = new Effect(130f, e -> {
                rand.setSeed(e.id);
                Angles.randLenVectors(e.id, 12, 140f * e.fout(Interp.pow3Out), (x, y) -> {
                    Draw.color(Pal.techBlue);
                    float rad = rand.random(9f, 18f);
                    float scl = rand.random(0.6f, 1f);
                    float dx = e.x + scl * x, dy = e.y + scl * y;
                    Fill.circle(dx, dy, e.fin() * rad);
                    Draw.color(Pal.techBlue);
                    Draw.z(EFFECT_MASK);
                    Fill.circle(dx, dy, e.fin() * rad / 1.8f);
                    Draw.z(EFFECT_BOTTOM);
                    Fill.circle(dx, dy, e.fin() * rad / 1.8f);
                    Draw.z(Layer.effect);
                    Drawf.light(dx, dy, e.fin() * rad * 1.5f, Pal.techBlue, 0.7f);
                });
            }),
            techBlueChargeBegin = new Effect(130f, e -> {
                Draw.color(Pal.techBlue);
                Fill.circle(e.x, e.y, e.fin() * 32);
                Lines.stroke(e.fin() * 3.7f);
                Lines.circle(e.x, e.y, e.fout() * 80);
                Draw.z(EFFECT_MASK);
                Draw.color(Pal.techBlue);
                Fill.circle(e.x, e.y, e.fin() * 20);

                Draw.z(EFFECT_BOTTOM);
                Draw.color(Pal.techBlue);
                Fill.circle(e.x, e.y, e.fin() * 22);
                Drawf.light(e.x, e.y, e.fin() * 35f, Pal.techBlue, 0.7f);
            }),
            largeTechBlueHitCircle = new Effect(20f, e -> {
                Draw.color(Pal.techBlue);
                Fill.circle(e.x, e.y, e.fout() * 44);
                Angles.randLenVectors(e.id, 5, 60f * e.fin(), (x, y) -> Fill.circle(e.x + x, e.y + y, e.fout() * 8));
                Draw.color(Pal.techBlue);
                Fill.circle(e.x, e.y, e.fout() * 30);
                Drawf.light(e.x, e.y, e.fout() * 55f, Pal.techBlue, 0.7f);
            }),
            largeTechBlueHit = new Effect(50, e -> {
                Draw.color(Pal.techBlue);
                Fill.circle(e.x, e.y, e.fout() * 44);
                Lines.stroke(e.fout() * 3.2f);
                Lines.circle(e.x, e.y, e.fin() * 80);
                Lines.stroke(e.fout() * 2.5f);
                Lines.circle(e.x, e.y, e.fin() * 50);
                Lines.stroke(e.fout() * 3.2f);
                Angles.randLenVectors(e.id, 30, 18 + 80 * e.fin(), (x, y) -> {
                    Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14 + 5);
                });

                Draw.z(EFFECT_MASK);
                Draw.color(Pal.techBlue);
                Fill.circle(e.x, e.y, e.fout() * 30);
                Drawf.light(e.x, e.y, e.fout() * 80f, Pal.techBlue, 0.7f);

                Draw.z(EFFECT_BOTTOM);
                Fill.circle(e.x, e.y, e.fout() * 31);
                Draw.z(Layer.effect - 0.0001f);
            }).layer(Layer.effect - 0.0001f),
            mediumTechBlueHit = new Effect(23, e -> {
                Draw.color(Pal.techBlue);
                Lines.stroke(e.fout() * 2.8f);
                Lines.circle(e.x, e.y, e.fin() * 60);
                Lines.stroke(e.fout() * 2.12f);
                Lines.circle(e.x, e.y, e.fin() * 35);

                Lines.stroke(e.fout() * 2.25f);
                Angles.randLenVectors(e.id, 9, 7f + 60f * e.finpow(), (x, y) -> Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f + e.fout() * 12f));

                Fill.circle(e.x, e.y, e.fout() * 22);
                Draw.color(Pal.techBlue);
                Fill.circle(e.x, e.y, e.fout() * 14);
                Drawf.light(e.x, e.y, e.fout() * 80f, Pal.techBlue, 0.7f);
            }),
            techBlueSmokeBig = new Effect(30f, e -> {
                Draw.color(Pal.techBlue);
                Fill.circle(e.x, e.y, e.fout() * 32);
                Draw.color(Pal.techBlue);
                Fill.circle(e.x, e.y, e.fout() * 20);
                Drawf.light(e.x, e.y, e.fout() * 36f, Pal.techBlue, 0.7f);
            }),
            techBlueShootBig = new Effect(40f, 100, e -> {
                Draw.color(Pal.techBlue);
                Lines.stroke(e.fout() * 3.7f);
                Lines.circle(e.x, e.y, e.fin() * 100 + 15);
                Lines.stroke(e.fout() * 2.5f);
                Lines.circle(e.x, e.y, e.fin() * 60 + 15);
                Angles.randLenVectors(e.id, 15, 7f + 60f * e.finpow(), (x, y) -> Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4f + e.fout() * 16f));
                Drawf.light(e.x, e.y, e.fout() * 120f, Pal.techBlue, 0.7f);
            }),
            healReceiveCircle = new Effect(11f, e -> {
                Draw.color(e.color);
                Lines.stroke(e.fout() * 1.667f);
                Lines.circle(e.x, e.y, 2f + e.finpow() * 7f);
            }),
            healSendCircle = new Effect(22f, e -> {
                Draw.color(e.color);
                Lines.stroke(e.fout() * 2f);
                Lines.circle(e.x, e.y, e.finpow() * e.rotation);
            }),
            jumpTrailOut = new Effect(120f, 200, e -> {
                if (!(e.data instanceof UnitType type))return;
                color(type.engineColor == null ? e.color : type.engineColor);

                if(type.engineLayer > 0)Draw.z(type.engineLayer);
                else Draw.z((type.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) - 0.001f);

                Tmp.v2.trns(e.rotation, 2300);

                for(int index = 0; index < type.engines.size; index++){
                    UnitType.UnitEngine engine = type.engines.get(index);

                    if(Angles.angleDist(engine.rotation, -90) > 75)return;
                    float ang = Mathf.slerp(engine.rotation, -90, 0.75f);

                    //noinspection SuspiciousNameCombination
                    Tmp.v1.trns(e.rotation, engine.y, -engine.x).add(Tmp.v2);

                    rand.setSeed(e.id);
                    e.scaled(80, i -> {
                        Drawn.tri(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 3f * i.fout(Interp.slowFast), 2300 + rand.range(120), i.rotation + ang - 90);
                        Fill.circle(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 3f * i.fout(Interp.slowFast));
                    });

                    randLenVectors(e.id + index, 42, 2330, e.rotation + ang - 90, 0f, (x, y) -> lineAngle(e.x + x + Tmp.v1.x, e.y + y + Tmp.v1.y, Mathf.angle(x, y), e.fout() * 60));
                }
            });

    /** WHFx should not be instantiated. */
    private WHFx() {}

    public static float fout(float fin, float margin) {
        if (fin >= 1f - margin) {
            return 1f - (fin - (1f - margin)) / margin;
        } else {
            return 1f;
        }
    }

    public static Effect polyTrail(Color fromColor, Color toColor, float size, float lifetime) {
        return new Effect(lifetime, size * 2, e -> {
            Draw.color(fromColor, toColor, e.fin());
            Fill.poly(e.x, e.y, 6, size * e.fout(), e.rotation);
            Drawf.light(e.x, e.y, e.fout() * size, fromColor, 0.7f);
        });
    }

    public static Effect hitSpark(Color color, float lifetime, int num, float range, float stroke, float length) {
        return new Effect(lifetime, e -> {
            Draw.color(color, Color.white, e.fout() * 0.3f);
            Lines.stroke(e.fout() * stroke);

            Angles.randLenVectors(e.id, num, e.finpow() * range, e.rotation, 360f, (x, y) -> {
                float ang = Mathf.angle(x, y);
                Lines.lineAngle(e.x + x, e.y + y, ang, e.fout() * length * 0.85f + length * 0.15f);
            });
        });
    }

    public static Effect instBomb(Color color){
        return get("instBomb", color, instBombSize(color, 4, 80f));
    }

    public static Effect instBombSize(Color color, int num, float size){
        return new Effect(22.0F, size * 1.5f, e -> {
            Draw.color(color);
            Lines.stroke(e.fout() * 4.0F);
            Lines.circle(e.x, e.y, 4.0F + e.finpow() * size / 4f);
            Drawf.light(e.x, e.y, e.fout() * size, color, 0.7f);

            int i;
            for(i = 0; i < num; ++i) {
                Drawn.tri(e.x, e.y, size / 12f, size * e.fout(), (float)(i * 90 + 45));
            }

            Draw.color();

            for(i = 0; i < num; ++i) {
                Drawn.tri(e.x, e.y, size / 26f, size / 2.5f * e.fout(), (float)(i * 90 + 45));
            }
        });
    }

    public static Effect instHit(Color color){return get("instHit", color, instHit(color, 5, 50)); }

    public static Effect instHit(Color color, int num, float size){
        return new Effect(20.0F, size * 1.5f, e -> {
            rand.setSeed(e.id);

            for(int i = 0; i < 2; ++i) {
                Draw.color(i == 0 ? color : color.cpy().lerp(Color.white, 0.25f));
                float m = i == 0 ? 1.0F : 0.5F;


                for(int j = 0; j < num; ++j) {
                    float rot = e.rotation + rand.range(size);
                    float w = 15.0F * e.fout() * m;
                    Drawn.tri(e.x, e.y, w, (size + rand.range( size * 0.6f)) * m, rot);
                    Drawn.tri(e.x, e.y, w, size * 0.3f * m, rot + 180.0F);
                }
            }

            e.scaled(12.0F, (c) -> {
                Draw.color(color.cpy().lerp(Color.white, 0.25f));
                Lines.stroke(c.fout() * 2.0F + 0.2F);
                Lines.circle(e.x, e.y, c.fin() * size * 1.1f);
            });

            e.scaled(18.0F, (c) -> {
                Draw.color(color);
                Angles.randLenVectors(e.id, 25, 8.0F + e.fin() * size * 1.25f, e.rotation, 60.0F, (x, y) -> {
                    Fill.square(e.x + x, e.y + y, c.fout() * 3.0F, 45.0F);
                });
            });

            Drawf.light(e.x, e.y, e.fout() * size, color, 0.7f);
        });
    }

    public static Effect smoothColorRect(Color out, float rad, float lifetime){
        return new Effect(lifetime, rad * 2, e -> {
            Draw.blend(Blending.additive);
            float radius = e.fin(Interp.pow3Out) * rad;
            Fill.light(e.x, e.y, 4, radius, 45f, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out)));
            Draw.blend();
        }).layer(Layer.effect + 0.15f);
    }

    public static Effect smoothColorCircle(Color out, float rad, float lifetime){
        return new Effect(lifetime, rad * 2, e -> {
            Draw.blend(Blending.additive);
            float radius = e.fin(Interp.pow3Out) * rad;
            Fill.light(e.x, e.y, circleVertices(radius), radius, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out)));
            Drawf.light(e.x, e.y, radius * 1.3f, out, 0.7f * e.fout(0.23f));
            Draw.blend();
        }).layer(Layer.effect + 0.15f);
    }

    public static Effect smoothColorCircle(Color out, float rad, float lifetime, float alpha){
        return new Effect(lifetime, rad * 2, e -> {
            Draw.blend(Blending.additive);
            float radius = e.fin(Interp.pow3Out) * rad;
            Fill.light(e.x, e.y, circleVertices(radius), radius, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out) * alpha));
            Drawf.light(e.x, e.y, radius * 1.3f, out, 0.7f * e.fout(0.23f));
            Draw.blend();
        }).layer(Layer.effect + 0.15f);
    }

    public static Effect instTrail(Color color, float angle, boolean random){
        return new Effect(30.0F, e -> {
            for(int j : angle == 0 ? oneArr: Mathf.signs){
                for(int i = 0; i < 2; ++i) {
                    Draw.color(i == 0 ? color : color.cpy().lerp(Color.white, 0.15f));
                    float m = i == 0 ? 1.0F : 0.5F;
                    float rot = e.rotation + 180.0F;
                    float w = 10.0F * e.fout() * m;
                    Drawn.tri(e.x, e.y, w, 30.0F + (random ? Mathf.randomSeedRange(e.id, 15.0F) : 8) * m, rot + j * angle);
                    if(angle == 0)Drawn.tri(e.x, e.y, w, 10.0F * m, rot + 180.0F + j * angle);
                    else  Fill.circle(e.x, e.y, w / 2f);
                }
            }
        });
    }

    public static Effect get(String m, Color c, Effect effect) {
        int hash = Objects.hash(m, c);
        Effect or = same.get(hash);
        if (or == null) same.put(hash, effect);
        return or == null ? effect : or;
    }

    public static Effect lightningHitLarge(Color color) {
        return get("lightningHitLarge", color, new Effect(50f, 180f, e -> {
            Draw.color(color);
            Drawf.light(e.x, e.y, e.fout() * 90f, color, 0.7f);
            e.scaled(25f, t -> {
                Lines.stroke(3f * t.fout());
                Lines.circle(e.x, e.y, 3f + t.fin(Interp.pow3Out) * 80f);
            });
            Fill.circle(e.x, e.y, e.fout() * 8f);
            Angles.randLenVectors(e.id + 1, 4, 1f + 60f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 5f);
            });

            Draw.color(Color.gray);
            Angles.randLenVectors(e.id, 8, 2f + 30f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 4f + 0.5f);
            });
        }));
    }

    public static Effect shootLineSmall(Color color) {
        return get("shootLineSmall", color, new Effect(37f, e -> {
            Draw.color(color, Color.white, e.fout() * 0.7f);
            Angles.randLenVectors(e.id, 4, 8 + 32 * e.fin(), e.rotation, 22F, (x, y) -> {
                Lines.stroke(1.25f * e.fout(0.2f));
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 6f + 3);
                Drawf.light(e.x + x, e.y + y, e.fout() * 13f + 3, color, 0.7f);
            });
        }));
    }

    public static Effect blast(Color color, float range) {
        float lifetime = Mathf.clamp(range * 1.5f, 90f, 600f);
        return new Effect(lifetime, range * 2.5f, e -> {
            Draw.color(color);
            Drawf.light(e.x, e.y, e.fout() * range, color, 0.7f);

            e.scaled(lifetime / 3, t -> {
                Lines.stroke(3f * t.fout());
                Lines.circle(e.x, e.y, 8f + t.fin(Interp.circleOut) * range * 1.35f);
            });

            e.scaled(lifetime / 2, t -> {
                Fill.circle(t.x, t.y, t.fout() * 8f);
                Angles.randLenVectors(t.id + 1, (int) (range / 13), 2 + range * 0.75f * t.finpow(), (x, y) -> {
                    Fill.circle(t.x + x, t.y + y, t.fout(Interp.pow2Out) * Mathf.clamp(range / 15f, 3f, 14f));
                    Drawf.light(t.x + x, t.y + y, t.fout(Interp.pow2Out) * Mathf.clamp(range / 15f, 3f, 14f), color, 0.5f);
                });
            });

            Draw.z(Layer.bullet - 0.001f);
            Draw.color(Color.gray);
            Draw.alpha(0.85f);
            float intensity = Mathf.clamp(range / 10f, 5f, 25f);
            for (int i = 0; i < 4; i++) {
                rand.setSeed(((long) e.id << 1) + i);
                float lenScl = rand.random(0.4f, 1f);
                int fi = i;
                e.scaled(e.lifetime * lenScl, eIn -> Angles.randLenVectors(eIn.id + fi - 1, eIn.fin(Interp.pow10Out), (int) (intensity / 2.5f), 8f * intensity, (x, y, in, out) -> {
                    float fout = eIn.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                    Fill.circle(eIn.x + x, eIn.y + y, fout * ((2f + intensity) * 1.8f));
                }));
            }
        });
    }

    public static Effect square(Color color, float lifetime, int num, float range, float size) {
        return new Effect(lifetime, e -> {
            Draw.color(color);
            rand.setSeed(e.id);
            Angles.randLenVectors(e.id, num, range * e.finpow(), (x, y) -> {
                float s = e.fout(Interp.pow3In) * (size + rand.range(size / 3f));
                Fill.square(e.x + x, e.y + y, s, 45);
                Drawf.light(e.x + x, e.y + y, s * 2.25f, color, 0.7f);
            });
        });
    }

    public static Effect instShoot(Color color, Color colorInner) {
        return new Effect(24f, e -> {
            e.scaled(10f, (b) -> {
                Draw.color(Color.white, color, b.fin());
                Lines.stroke(b.fout() * 3f + 0.2f);
                Lines.circle(b.x, b.y, b.fin() * 50f);
            });
            Draw.color(color);

            for (int i : Mathf.signs) {
                Drawn.tri(e.x, e.y, 8f * e.fout(), 85f, e.rotation + 90f * i);
                Drawn.tri(e.x, e.y, 8f * e.fout(), 50f, 90 + 90f * i);
            }

            Draw.color(colorInner);

            for (int i : Mathf.signs) {
                Drawn.tri(e.x, e.y, 5f * e.fout(), 48f, e.rotation + 90f * i);
                Drawn.tri(e.x, e.y, 5f * e.fout(), 29f, 90 + 90f * i);
            }
        });
    }

    public static Effect sharpBlast(Color colorExternal, Color colorInternal, float lifetime, float range) {
        return new Effect(lifetime, range * 2, e -> {
            Angles.randLenVectors(e.id, (int) Mathf.clamp(range / 8, 4, 18), range / 8, range * (1 + e.fout(Interp.pow2OutInverse)) / 2f, (x, y) -> {
                float angle = Mathf.angle(x, y);
                float width = e.foutpowdown() * rand.random(range / 6, range / 3) / 2 * e.fout();

                rand.setSeed(e.id);
                float length = rand.random(range / 2, range * 1.1f) * e.fout();

                Draw.color(colorExternal);
                Drawn.tri(e.x + x, e.y + y, width, range / 3 * e.fout(Interp.pow2In), angle - 180);
                Drawn.tri(e.x + x, e.y + y, width, length, angle);

                Draw.color(colorInternal);

                width *= e.fout();

                Drawn.tri(e.x + x, e.y + y, width / 2, range / 3 * e.fout(Interp.pow2In) * 0.9f * e.fout(), angle - 180);
                Drawn.tri(e.x + x, e.y + y, width / 2, length / 1.5f * e.fout(), angle);
            });
        });
    }

    public static Effect subEffect(float lifetime, float radius, int num, float childLifetime, Interp spreadOutInterp, EffectParam effect) {
        return new Effect(lifetime, radius * 2f, e -> {
            rand.setSeed(e.id);
            float finT = e.lifetime * e.fin(spreadOutInterp);

            for (int s = 0; s < num; s++) {
                float sBegin = rand.random(e.lifetime - childLifetime);
                float fin = (finT - sBegin) / childLifetime;

                if (fin < 0 || fin > 1) continue;

                float fout = 1 - fin;

                rand2.setSeed(e.id + s);
                float theta = rand2.random(0f, Mathf.PI2);
                v9.set(Mathf.cos(theta), Mathf.sin(theta)).scl(radius * sBegin / (e.lifetime - childLifetime));

                Tmp.c1.set(e.color).lerp(Color.white, fout * 0.7f);
                Draw.color(Tmp.c1);
                effect.draw(e.id + s + 9999l, e.x + v9.x, e.y + v9.y, Mathf.radiansToDegrees * theta, fin);
            }
        });
    }

    public static Effect lineCircleOut(Color color, float lifetime, float size, float stroke) {
        return new Effect(lifetime, e -> {
            Draw.color(color);
            Lines.stroke(e.fout() * stroke);
            Lines.circle(e.x, e.y, e.fin(Interp.pow3Out) * size);
        });
    }

    public static Effect circleOut(float lifetime, float radius, float thick) {
        return new Effect(lifetime, radius * 2f, e -> {
            Draw.color(e.color, Color.white, e.fout() * 0.7f);
            Lines.stroke(thick * e.fout());
            Lines.circle(e.x, e.y, radius * e.fin(Interp.pow3Out));
        });
    }

    public static Effect circleOut(Color color, float range) {
        return new Effect(Mathf.clamp(range / 2, 45f, 360f), range * 1.5f, e -> {
            rand.setSeed(e.id);

            Draw.color(Color.white, color, e.fin() + 0.6f);
            float circleRad = e.fin(Interp.circleOut) * range;
            Lines.stroke(Mathf.clamp(range / 24, 4, 20) * e.fout());
            Lines.circle(e.x, e.y, circleRad);
            for (int i = 0; i < Mathf.clamp(range / 12, 9, 60); i++) {
                Tmp.v1.set(1, 0).setToRandomDirection(rand).scl(circleRad);
                Drawn.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16, circleRad / 12) * e.fout(), rand.random(circleRad / 4, circleRad / 1.5f) * (1 + e.fin()) / 2, Tmp.v1.angle() - 180);
            }
        });
    }

    public static Effect crossBlast(Color color) {
        return get("crossBlast", color, crossBlast(color, 72));
    }

    public static Effect crossBlast(Color color, float size) {
        return crossBlast(color, size, 0);
    }

    public static Effect crossBlast(Color color, float size, float rotate) {
        return new Effect(Mathf.clamp(size / 3f, 35f, 240f), size * 2, e -> {
            Draw.color(color, Color.white, e.fout() * 0.55f);
            Drawf.light(e.x, e.y, e.fout() * size, color, 0.7f);
            e.scaled(10f, i -> {
                Lines.stroke(1.35f * i.fout());
                Lines.circle(e.x, e.y, size * 0.7f * i.finpow());
            });
            rand.setSeed(e.id);
            float sizeDiv = size / 1.5f;
            float randL = rand.random(sizeDiv);
            for (int i = 0; i < 4; i++) {
                Drawn.tri(e.x, e.y, size / 20 * (e.fout() * 3f + 1) / 4 * (e.fout(Interp.pow3In) + 0.5f) / 1.5f, (sizeDiv + randL) * Mathf.curve(e.fin(), 0, 0.05f) * e.fout(Interp.pow3), i * 90 + rotate);
            }
        });
    }

    public interface EffectParam {
        void draw(long id, float x, float y, float rot, float fin);
    }
}
