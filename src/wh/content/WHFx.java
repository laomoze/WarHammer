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
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.*;
import arc.struct.IntMap;
import arc.util.*;

import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Effect.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Unit;
import mindustry.graphics.*;
import mindustry.type.UnitType;
import mindustry.type.UnitType.*;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.SteamVent;
import wh.entities.abilities.PcShieldArcAbility;
import wh.entities.world.drawer.part.*;
import wh.graphics.*;
import wh.struct.Vec2Seq;
import wh.util.WHUtils;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static mindustry.Vars.tilesize;
import static wh.entities.Spawner.delaySpawnTime;

public final class WHFx{
    public static final float EFFECT_MASK = Layer.effect + 0.0001f;
    public static final float EFFECT_BOTTOM = Layer.bullet - 0.11f;
    private static final Vec2 v = new Vec2();
    public static final float lightningAlign = 0.5F;
    public static final Rand rand = new Rand();
    public static final Rand rand1 = new Rand();
    public static final Rand rand2 = new Rand();
    public static final Vec2 v7 = new Vec2();
    public static final Vec2 v8 = new Vec2();
    public static final Vec2 v9 = new Vec2();
    public static final int[] oneArr = new int[]{1};

    public static Effect energyUnitBlast;
    public static Effect circle;
    public static Effect circleOut;
    public static Effect circleOutQuick;
    public static Effect circleOutLong;
    public static Effect circleSplash;

    public static Effect missileShoot;

    public static Effect sapPlasmaShoot;
    public static Effect coloredHitSmall;
    public static Effect hugeTrail;

    public static Effect crossBlastArrow45;
    public static Effect ScrossBlastArrow45;
    public static Effect crossBlast_45;
    public static Effect crossSpinBlast;

    public static Effect skyTrail;
    public static Effect trailToGray;
    public static Effect trailFromWhite;

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

    //保留
    public static Effect spawnWave;
    public static Effect spawnGround;

    public static Effect hugeSmokeLong;
    public static Effect hugeSmokeGray;
    public static Effect hugeSmoke;
    public static Effect shuttle;
    public static Effect shuttleDark;
    public static Effect shuttleLerp;
    public static Effect lightningHitSmall;
    public static Effect lightningHitLarge;
    public static Effect lightningFade;

    public static Effect groundRise;
    public static Effect squSpark1;
    public static Effect triSpark2;
    public static Effect hitSparkHuge;
    public static Effect hitSparkLarge;
    public static Effect hitSpark;

    public static Effect square45_4_45;
    public static Effect square45_6_45;
    public static Effect square45_6_45_Charge;
    public static Effect square45_8_45;

    public static Effect lightningSpark;
    public static Effect chainLightningFade;
    public static Effect chainLightningFadeReversed;
    public static Effect posLightning;

    public static Effect attackWarningRange;
    public static Effect attackWarningPos;

    public static Effect tank3sMissileTrailSmoke;
    public static Effect tank3sExplosionSmoke;
    public static Effect TankAG7BulletExplode;
    public static Effect promethiunmRectorExplosion;
    public static Effect PlasmaFireBurn;

    public static Effect spawn;
    public static Effect jumpTrail;
    public static Effect jumpIn;
    public static Effect jumpTrailOut;
    public static Effect airDropOut;
    public static Effect AirDropLandDust;
    public static Effect groundEffect;
    public static Effect BeamLaserGroundEffect;

    private WHFx(){
    }


    public interface EffectParam{
        void draw(long var1, float var3, float var4, float var5, float var6);
    }

    public static float fslope(float fin){
        return (0.5f - Math.abs(fin - 0.5f)) * 2f;
    }


    public static Effect boolSelector = new Effect(0, 0, e -> {
    });

    public static float fout(float fin, float margin){
        return fin >= 1 - margin ? 1 - (fin - (1 - margin)) / margin : 1;
    }


    public static Effect polyTrail(Color fromColor, Color toColor, float size, float lifetime){
        return new Effect(lifetime, size * 2.0F, (e) -> {
            color(fromColor, toColor, e.fin());
            Fill.poly(e.x, e.y, 6, size * e.fout(), e.rotation);
            Drawf.light(e.x, e.y, e.fout() * size, fromColor, 0.7F);
        });
    }

    public static Effect square(Color color, float lifetime, int num, float range, float size){
        return new Effect(lifetime, (e) -> {
            color(color);
            rand.setSeed(e.id);
            randLenVectors(e.id, num, range * e.finpow(), (x, y) -> {
                float s = e.fout(Interp.pow3In) * (size + rand.range(size / 3.0F));
                Fill.square(e.x + x, e.y + y, s, 45.0F);
                Drawf.light(e.x + x, e.y + y, s * 2.25F, color, 0.7F);
            });
        });
    }

    public static Effect tri(Color color, float lifetime, int num, float range, float size){
        return new Effect(lifetime, (e) -> {
            color(color);
            rand.setSeed(e.id);
            randLenVectors(e.id, num, range * e.finpow(), (x, y) -> {
                float s = e.fout(Interp.pow3In) * (size + rand.range(size / 3.0F));
                float randN = rand.random(180f);
                Fill.poly(e.x + x, e.y + y, 3, e.fout() * 8.0F * rand.random(0.8F, 1.2F),
                e.rotation + randN * e.fin());
                Drawf.light(e.x + x, e.y + y, s * 2.25F, color, 0.7F);
            });
        });
    }

    public static Effect hitSpark(Color color, float lifetime, int num, float range, float stroke, float length){
        return new Effect(lifetime, (e) -> {
            color(color, Color.white, e.fout() * 0.3F);
            stroke(e.fout() * stroke);
            randLenVectors(e.id, num, e.finpow() * range, e.rotation, 360.0F, (x, y) -> {
                float ang = Mathf.angle(x, y);
                lineAngle(e.x + x, e.y + y, ang, e.fout() * length * 0.85F + length * 0.15F);
            });
        });
    }

    public static Effect hitCircle(Color color, Color to, float lifetime, int num, float range, float size){
        return new Effect(lifetime, (e) -> {
            color(color, to, e.fout() * 0.3F);
            randLenVectors(e.id, num, e.finpow() * range, e.rotation, 360.0F, (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * size * 0.85F);
            });
        });
    }

    public static Effect hitLine(Color from, Color to, float range, float angleRange, float lineLength){
        return new Effect(37f, e -> {
            rand.setSeed(e.id);

            int num = Mathf.clamp((int)range / 6, 6, 20);
            float thick = Mathf.clamp(0.75f, 1, lineLength / 8 * rand.random(0.5f, 1f));
            color(from, to, e.fin());

            WHUtils.randLenVectors(e.id, num, 4 + (range * 1.2f) * e.fin(), range * 0.15f * e.fin(), e.rotation, angleRange, (x, y) -> {
                stroke(thick * e.fout(0.32f));
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), (e.fslope() + e.fin()) * 0.5f * (lineLength * rand.random(0.1f, 0.3f) + rand.random(2f)) + rand.random(2f));
                Drawf.light(e.x + x, e.y + y, e.fslope() * (range * 0.5f + 14f) + 3, e.color, 0.7f);
            });
        });
    }

    public static Effect hitPoly(Color color, Color to, float lifetime, int num, float range, float size, int sides, float rotation){
        return new Effect(lifetime, (e) -> {
            rand.setSeed(e.id);
            float rot = rand.random(rotation * 1.5f);
            color(color, to, e.fout() * 0.3F);
            randLenVectors(e.id, num, e.finpow() * range, e.rotation, 360.0F, (x, y) -> {
                Fill.poly(e.x + x, e.y + y, sides, e.fout() * size * 0.85F, (rot + rotation) * Mathf.curve(e.fin(), 0, 0.5f));
            });
        });
    }

    public static TrailEffect trailHitSpark(Color color, float lifetime, int num, float range, float stroke, float length){
        return new TrailEffect(lifetime, range * 2, color, color, num, (int)length, stroke)
        .trailUpdater((e, trail, x, y, width, len, index) -> {
            long id = e.id + index * 45;
            rand.setSeed(e.id + id);
            randLenVectors(e.id + id, 1, e.fin(Interp.pow3Out) * range, e.rotation, 360, (x1, y1) -> {
                trail.length = (int)(len * WHFx.fout(e.fin(), 0.06f));
                trail.update(x + x1, y + y1, width * e.fout());
            });
        }).drawTri(true);
    }

    public static TrailEffect trailCircleHitSpark(Color color, float lifetime, int num, float range, float stroke, float length){
        return new TrailEffect(lifetime, range * 2, color, color, num, (int)length, stroke)
        .trailUpdater((e, trail, x, y, width, len, index) -> {
            long id = e.id + index * 45L;
            rand.setSeed(e.id + id);
            randLenVectors(e.id + id, 1, e.fin(Interp.pow3Out) * range, e.rotation, 360, (x1, y1) -> {
                trail.length = (int)(len * WHFx.fout(e.fin(), 0.06f));
                trail.update(x + x1, y + y1, width * e.fout());
            });
        });
    }


    public static Effect smoothColorRect(Color out, float rad, float lifetime){
        return new Effect(lifetime, rad * 2, e -> {
            blend(Blending.additive);
            float radius = e.fin(Interp.pow3Out) * rad;
            Fill.light(e.x, e.y, 4, radius, 45f, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out)));
            color(out);
            stroke(radius * 2.0F + 0.2F);
            Lines.square(e.x, e.y, radius, 90f);
            blend();
        }).layer(Layer.effect + 0.15f);
    }

    public static Effect smoothColorCircle(Color out, float rad, float lifetime){
        return (new Effect(lifetime, rad * 2.0F, (e) -> {
            blend(Blending.additive);
            float radius = e.fin(Interp.pow3Out) * rad;
            Fill.light(e.x, e.y, circleVertices(radius), radius, Color.clear, Tmp.c1.set(out).a(e.fout(Interp.pow5Out)));
            Drawf.light(e.x, e.y, radius * 1.3F, out, 0.7F * e.fout(0.23F));
            blend();
        })).layer(110.15F);
    }

    public static Effect instTrail(Color color, float angle, boolean random){
        return new Effect(30, e -> {
            for(int j : angle == 0 ? oneArr : Mathf.signs){
                for(int i = 0; i < 2; ++i){
                    color(i == 0 ? color : color.cpy().lerp(Color.white, 0.15f));
                    float m = i == 0 ? 1 : 0.5f;
                    float rot = e.rotation + 180;
                    float w = 10 * e.fout() * m;
                    Drawn.tri(e.x, e.y, w, 30 + (random ? Mathf.randomSeedRange(e.id, 15) : 8) * m, rot + j * angle);
                    if(angle == 0) Drawn.tri(e.x, e.y, w, 10.0F * m, rot + 180.0F + j * angle);
                    else Fill.circle(e.x, e.y, w / 2f);
                }
            }
        }).followParent(true);
    }

    public static Effect lightningHitLarge(Color color, float range, int amount, float size){
        return new Effect(50.0F, 180.0F, (e) -> {
            color(color);
            Drawf.light(e.x, e.y, e.fout() * 90.0F, color, 0.7F);
            e.scaled(25.0F, (t) -> {
                stroke(3.0F * t.fout());
                circle(e.x, e.y, 3.0F + t.fin(Interp.pow3Out) * range);
            });
            Fill.circle(e.x, e.y, e.fout() * 8.0F);
            randLenVectors(e.id + 1, amount / 2, 1.0F + range * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * size);
            });
            color(Color.gray, 0.8f);
            randLenVectors(e.id, amount, 2 + range * 0.7f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * size + 0.5F);
            });
        });
    }

    public static Effect explosionSmokeEffect(Color color, float lifetime, float range, int amount, float size){
        return new Effect(lifetime, 180.0F, (e) -> {
            color(color);
            Drawf.light(e.x, e.y, e.fout() * 90.0F, color, 0.7F);
            Fill.circle(e.x, e.y, e.fout() * 8.0F);
            randLenVectors(e.id + 1, amount / 2, 1.0F + range * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * size);
            });
            color(Color.gray, 0.8f);
            randLenVectors(e.id, amount, 2 + range * 0.7f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * size + 0.5F);
            });
        });
    }

    public static Effect shootLineSmall(Color color){
        return new Effect(37.0F, (e) -> {
            color(color, Color.white, e.fout() * 0.7F);
            randLenVectors(e.id, 4, 8.0F + 32.0F * e.fin(), e.rotation, 22.0F, (x, y) -> {
                stroke(1.25F * e.fout(0.2F));
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 6.0F + 3.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 13.0F + 3.0F, color, 0.7F);
            });
        });
    }

    public static Effect shootCircleSmall(Color color){
        return new Effect(30, e -> {
            color(color, Color.white, e.fout() * 0.75f);
            rand.setSeed(e.id);
            randLenVectors(e.id, 3, 3 + 23 * e.fin(), e.rotation, 22, (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * rand.random(1.5f, 3.2f));
                Drawf.light(e.x + x, e.y + y, e.fout() * 4.5f, color, 0.7f);
            });
        });
    }

    public static Effect shuttle(Color color, Color bottomColor, float lifetime, Boolean drawBottom, float length, float rotation){
        return new Effect(lifetime, length * 1.5f, (e) -> {
            float len, ang;
            if(e.data instanceof Float){
                len = (Float)e.data;
                ang = e.rotation;
            }else{
                len = length;
                ang = rotation;
            }

            color(color);
            Drawf.light(e.x, e.y, e.fout() * len, color, 0.7f);

            float fout = e.fout(Interp.exp10Out);
            z(drawBottom ? EFFECT_BOTTOM : Layer.effect);
            for(int i : Mathf.signs){
                Drawn.tri(e.x, e.y, len / 14 * fout * (Mathf.absin(0.8f, 0.07f) + 1),
                len * 2 * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f),
                ang + i * 90);
            }

            z(drawBottom ? EFFECT_MASK : Layer.effect);
            color(bottomColor);
            if(drawBottom) for(int i : Mathf.signs){
                Drawn.tri(e.x, e.y, (len * 0.7f) / 14 * fout * (Mathf.absin(0.8f, 0.07f) + 1),
                len * 2 * 0.7f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f),
                ang + i * 90);
            }
        }).layer(Layer.effect - 1f);
    }

    public static Effect instRotation(Color color, float lifetime, float size, float rotateAngle, boolean rotate){
        return new Effect(lifetime, size * 1.6f, e -> {
            rand.setSeed(e.id);
            float ang = rotate ? rand.random(0, 180f) * e.fin(Interp.smooth) + rotateAngle : rotateAngle;
            Draw.color(color);
            for(int i = 0; i < 4; i++){
                Drawf.tri(e.x, e.y, size / 15f, size * e.fout(), i * 90 + ang);
            }

            Draw.color();
            float scale = 0.4f;
            for(int i = 0; i < 4; i++){
                Drawf.tri(e.x, e.y, scale * size / 15f, scale * size * e.fout(), i * 90 + ang);
            }

            Drawf.light(e.x, e.y, size * 1.1f, color, e.fout());
        });
    }

    public static Effect instBombSize(Color color, int num, float size){
        return new Effect(22.0F, size * 1.5F, (e) -> {
            color(color);
            stroke(e.fout() * 4);
            circle(e.x, e.y, 4 + e.finpow() * size / 4);
            Drawf.light(e.x, e.y, e.fout() * size, color, 0.7f);

            int i;
            for(i = 0; i < num; ++i){
                Drawn.tri(e.x, e.y, size / 12, size * e.fout(), (float)(i * 90 + 45));
            }

            color();
            for(i = 0; i < num; ++i){
                Drawn.tri(e.x, e.y, size / 26, size / 2.5f * e.fout(), (float)(i * 90 + 45));
            }

        });
    }


    public static Effect instHit(Color color, boolean square, int num, float size){
        return new Effect(20, size * 1.5F, (e) -> {
            rand.setSeed(e.id);

            for(int i = 0; i < 2; ++i){
                color(i == 0 ? color : color.cpy().lerp(Color.white, 0.25f));
                float m = i == 0 ? 1.0f : 0.5f;

                for(int j = 0; j < num; ++j){
                    float rot = e.rotation + rand.range(size);
                    float w = 15 * e.fout() * m;
                    Drawn.tri(e.x, e.y, w, (size + rand.range(size * 0.6f)) * m, rot);
                    Drawn.tri(e.x, e.y, w, size * 0.3F * m, rot + 180);
                }
            }

            e.scaled(12, (c) -> {
                color(color.cpy().lerp(Color.white, 0.25F));
                stroke(c.fout() * 2.0F + 0.2F);
                circle(e.x, e.y, c.fin() * size * 1.1F);
            });

            if(square) e.scaled(18, (c) -> {
                color(color);
                randLenVectors(e.id, (int)size, 8.0F + e.fin() * size * 1.25f, e.rotation, 60.0f, (x, y) -> {
                    Fill.square(e.x + x, e.y + y, c.fout() * 3, 45);
                });
            });

            Drawf.light(e.x, e.y, e.fout() * size, color, 0.7F);
        });
    }

    public static Effect blast(Color color, float range){
        float lifetime = Mathf.clamp(range * 1.5F, 90.0F, 600.0F);
        return new Effect(lifetime, range * 2.5F, (e) -> {
            color(color);
            Drawf.light(e.x, e.y, e.fout() * range, color, 0.7F);
            e.scaled(lifetime / 3.0F, (t) -> {
                stroke(3.0F * t.fout());
                circle(e.x, e.y, 8.0F + t.fin(Interp.circleOut) * range * 1.35F);
            });
            e.scaled(lifetime / 2.0F, (t) -> {
                Fill.circle(t.x, t.y, t.fout() * 8.0F);
                randLenVectors(t.id + 1, (int)(range / 13.0F), 2.0F + range * 0.75F * t.finpow(), (x, y) -> {
                    Fill.circle(t.x + x, t.y + y, t.fout(Interp.pow2Out) * Mathf.clamp(range / 15.0F, 3.0F, 14.0F));
                    Drawf.light(t.x + x, t.y + y, t.fout(Interp.pow2Out) * Mathf.clamp(range / 15.0F, 3.0F, 14.0F), color, 0.5F);
                });
            });
            z(99.999F);
            color(Color.gray);
            alpha(0.85F);
            float intensity = Mathf.clamp(range / 10.0F, 5.0F, 25.0F);

            for(int i = 0; i < 4; ++i){
                rand.setSeed(((long)e.id << 1) + i);
                float lenScl = rand.random(0.4F, 1.0F);
                int fi = i;
                e.scaled(e.lifetime * lenScl, (eIn) -> {
                    randLenVectors(eIn.id + fi - 1, eIn.fin(Interp.pow10Out), (int)(intensity / 2.5F), 8.0F * intensity, (x, y, in, out) -> {
                        float fout = eIn.fout(Interp.pow5Out) * rand.random(0.5F, 1.0F);
                        Fill.circle(eIn.x + x, eIn.y + y, fout * (2.0F + intensity) * 1.8F);
                    });
                });
            }

        });
    }

    public static Effect sharpBlast(Color colorExternal, Color colorInternal, float lifetime, float range){
        return new Effect(lifetime, range * 2.0F, (e) -> {
            randLenVectors(e.id, (int)Mathf.clamp(range / 8.0F, 4.0F, 18.0F), range / 8.0F, range * (1.0F + e.fout(Interp.pow2OutInverse)) / 2.0F, (x, y) -> {
                float angle = Mathf.angle(x, y);
                float width = e.foutpowdown() * rand.random(range / 6.0F, range / 3.0F) / 2.0F * e.fout();
                rand.setSeed(e.id);
                float length = rand.random(range / 2.0F, range * 1.1F) * e.fout();
                color(colorExternal);
                Drawn.tri(e.x + x, e.y + y, width, range / 3.0F * e.fout(Interp.pow2In), angle - 180.0F);
                Drawn.tri(e.x + x, e.y + y, width, length, angle);
                color(colorInternal);
                width *= e.fout();
                Drawn.tri(e.x + x, e.y + y, width / 2.0F, range / 3.0F * e.fout(Interp.pow2In) * 0.9F * e.fout(), angle - 180.0F);
                Drawn.tri(e.x + x, e.y + y, width / 2.0F, length / 1.5F * e.fout(), angle);
            });
        });
    }

    public static Effect crossBlastArrow45(Color color, Color to, float lifetime, float with, float length, float arrowStartLen, float range){
        return new Effect(lifetime, (e) -> {
            color(color, to, e.fout());
            Drawf.light(e.x, e.y, e.fout() * 70.0F, e.color, 0.7F);
            e.scaled(lifetime / 3, (ix) -> {
                stroke(1.35F * ix.fout());
                circle(e.x, e.y, range * ix.finpow());
            });
            rand.setSeed(e.id);
            float randL = rand.random(length);
            float f = Mathf.curve(e.fin(), 0, 0.08f);

            for(int i = 0; i < 4; ++i){
                Tmp.v1.trns(45 + i * 90, arrowStartLen);
                Drawn.arrow(e.x + Tmp.v1.x, e.y + Tmp.v1.y,
                with * (e.fout() * 3f + 1) / 4 * e.fout(Interp.pow3In),
                (length + randL) * f * e.fout(Interp.pow3),
                -randL / 6f * f, (float)(i * 90 + 45));
            }
        });
    }

    public static Effect crossBlast(Color color, float size, float rotate){
        return new Effect(Mathf.clamp(size / 3.0F, 35.0F, 240.0F), size * 2.0F, (e) -> {
            color(color, Color.white, e.fout() * 0.55F);
            Drawf.light(e.x, e.y, e.fout() * size, color, 0.7F);
            e.scaled(10.0F, (ix) -> {
                stroke(1.35F * ix.fout());
                circle(e.x, e.y, size * 0.7F * ix.finpow());
            });
            rand.setSeed(e.id);
            float sizeDiv = size / 1.5F;
            float randL = rand.random(sizeDiv);

            for(int i = 0; i < 4; ++i){
                Drawn.tri(e.x, e.y, size / 20.0F * (e.fout() * 3.0F + 1.0F) / 4.0F * (e.fout(Interp.pow3In) + 0.5F) / 1.5F, (sizeDiv + randL) * Mathf.curve(e.fin(), 0.0F, 0.05F) * e.fout(Interp.pow3), (float)(i * 90) + rotate);
            }

        });
    }

    public static Effect plasmaShoot(Color color, float lifetime, float size, int amount){
        return new Effect(lifetime, e -> {
            rand.setSeed(e.id);
            color(color);
            randLenVectors(e.id, amount, size * 0.85f * Mathf.curve(e.fin(), 0, 0.08f), size * 0.15f * rand.random(0.5f, 1.3f) * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, Mathf.curve(e.fin(), 0, 0.08f) * e.fout() * 5 * rand.random(0.5f, 1.3f));
            });
        });
    }

    public static Effect plasmaBlast(float lifetime, Color color, float size, int amount){
        return new Effect(lifetime, e -> {

            color(color);
            e.scaled(lifetime * 0.7f, a -> {
                rand.setSeed(a.id);
                randLenVectors(a.id + 999, amount * 3, size * 0.7f * Mathf.curve(a.fin(), 0, 0.08f), size * 0.3f * rand.random(0.8f, 1.2f) * a.finpow(), (x, y) -> {
                    Fill.circle(a.x + x, a.y + y, Mathf.curve(a.fin(), 0, 0.08f) * a.fout(Interp.pow10Out) * 4 * rand.random(0.7f, 1.6f));
                });
            });

            rand.setSeed(e.id);

            randLenVectors(e.id + 114514, amount, size * rand.random(0.4f, 1) * e.fin(), (x, y) -> {
                float fout = e.fout(Interp.pow5Out);
                Fill.circle(e.x + x, e.y + y, 12 * rand.random(0.7f, 1f) * fout);
            });

          /*  e.scaled(lifetime * 0.6f, i -> {
                randLenVectors(i.id, amount, 1f + Mathf.randomSeed(i.id, 0.5f, 1) * size * 0.85f * i.finpow(), (x, y) -> {
                    Fill.poly(i.x + x, i.y + y, 6, rand.random(0.5f, 1f) * size / 10 * i.fout(), rand.random(360) * i.fout() + 60f);
                });
            });*/
            e.scaled(lifetime, i -> {
                float intensity = 0.25f;
                color(color);
                Fill.circle(i.x, i.y, intensity * size * i.fout(Interp.pow3Out));
                float scl = 0.3f * size;
                Rand rand = new Rand(i.id);
                randLenVectors(i.id, 8, scl / 3, scl * (1.0F + i.fout(Interp.circleOut)) / 1.5f, (x, y) -> {
                    float angle = Mathf.angle(x, y);
                    float width = i.foutpowdown() * rand.random(scl / 6.0F, scl / 3.0F);
                    float length = rand.random(scl, scl * 2) * i.fout(Interp.circleOut);
                    color(color);
                    Drawn.tri(i.x + x, i.y + y, width, scl / 3.0F * i.fout(Interp.circleOut), angle - 180);
                    Drawn.tri(i.x + x, i.y + y, width, length, angle);
                    color(Color.black);
                    width *= i.fout();
                    Drawn.tri(i.x + x, i.y + y, width / 2.0F, scl / 3.0F * i.fout(Interp.circleOut) * 0.9F * i.fout(), angle - 180);
                    Drawn.tri(i.x + x, i.y + y, width / 2.0F, length / 1.5F * i.fout(), angle);
                });
                color(Pal.coalBlack.cpy());
                Fill.circle(i.x, i.y, intensity * size * 0.6f * i.fout(Interp.pow3Out));
            });
            Drawf.light(e.x, e.y, size, color, 0.8f * e.fout());
        });
    }


    public static Effect generalExplosion(float lifetime, Color color, float size, int amount, boolean smooth){
        return new MultiEffect(
        new Effect(lifetime * 2.5f, e -> {
            color(color);

            e.scaled(lifetime, i -> {
                stroke(3f * i.fout(Interp.pow2Out));
                if(!smooth) circle(e.x, e.y, 3f + i.fin(Interp.smooth) * size);
                if(smooth) Drawn.shockWave(i.x, i.y, size, size * 0.65f * i.fout(Interp.pow2Out), i.fin(Interp.pow3Out), color);
            });

            color(Color.gray);

            randLenVectors(e.id, amount / 2, 2f + size * 0.85f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * (Math.max(size / 8, 6) + 0.5f));
            });

            Draw.color(Pal.lighterOrange, Color.gray.cpy(), e.fout());
            e.scaled(lifetime, e1 -> {
                stroke(2 * e1.fout());
                randLenVectors(e1.id + 1, (int)(amount * 1.5f), 1f + Mathf.randomSeed(e1.id, 0.5f, 1) * size * 0.85f * e1.finpow(), (x, y) -> {
                    lineAngle(e1.x + x, e1.y + y, Mathf.angle(x, y), e1.fout() * Math.max(size / 8, 5) * Mathf.randomSeed(e1.id, 0.3f, 1.2f));

                });
                Drawf.light(e1.x, e1.y, size * 1.5f, Pal.lighterOrange, 0.8f * e1.fout());
            });

        })
        /*new TrailEffect(lifetime, size * 2, Pal.lighterOrange, Color.gray, amount, 20, 1.5f)
        .trailUpdater((e, trail, x, y, width, len, index) -> {
            long id = (long)(e.id + index * 999999L + Mathf.randomSeed(e.id, 99999));
            rand.setSeed(e.id + id);
            randLenVectors(e.id + id, 1, e.finpow() * size, e.rotation, 360, (x1, y1) -> {
                trail.length = (int)(e.fout() * len * Mathf.randomSeed(e.id + id, 0.7f, 2f));
                trail.update(x + x1, y + y1, Mathf.randomSeed(e.id, 0.5f, 1) * width * e.fout());
            });
        }).drawTri(true).layer(Layer.effect)*/
        );
    }

    public static Effect line45Explosion(Color from, Color to, float size){
        return new Effect(60f, e -> {
            rand.setSeed(e.id);
            color(from, to, e.finpow());
            stroke(Mathf.clamp(Math.max(1, size / 20), 1, 3) * e.fout());
            spikes(e.x, e.y, rand.random(size, size * 1.2f) * e.finpow(), rand.random(size * 0.08f) * e.foutpowdown() + rand.random(size * 0.15f, size * 0.5f) * e.fin(), 4, 45);
            randLenVectors(e.id, 4, 4f + e.fin() * 8f, (x, y) -> {
                color(from, to, e.finpow());
                Fill.square(e.x + x, e.y + y, 0.5f + e.fout() * 6f, 45);
            });
        });
    }

    public static Effect airAsh(float lifetime, float range, float start, float pin, Color color, float width,
                                int amount){
/*         new MultiEffect(
        new Effect(lifetime, e -> {
            float fee = e.time < e.lifetime / 2 ? e.fin() * 2 : e.fout() * 2;
            for(int a : Mathf.signs){
                for(int i = 0; i < amount; i++){
                    float dx = WHUtils.dx(e.x, range * e.fin() + start, (e.time * 8 + i) * a + Mathf.randomSeed(e.id, -10, 10)),
                    dy = WHUtils.dy(e.y, range * e.fin() + start, (e.time * 8 + i) * a + Mathf.randomSeed(e.id, -10, 10));
                    color(color);
                    Fill.circle(dx, dy, (width * i / amount + 0.2f) * fee);
                }
            }
        }),
        new Effect(lifetime, e -> {
            float fee = e.time < e.lifetime / 2 ? e.fin() * 2 : e.fout() * 2;
            for(int a : Mathf.signs){
                for(int i = 0; i < amount; i++){
                    float dx = WHUtils.dx(e.x, (range - pin) * e.fin() + start, (e.time * 8 + i) * a + Mathf.randomSeed(e.id, -10, 10) + 120),
                    dy = WHUtils.dy(e.y, (range - pin) * e.fin() + start, (e.time * 8 + i) * a + Mathf.randomSeed(e.id, -10, 10) + 120);
                    color(color);
                    Fill.circle(dx, dy, (width * i / amount + 0.2f) * fee);
                }
            }
        }),
        new Effect(lifetime, e -> {
            float fee = e.time < e.lifetime / 2 ? e.fin() * 2 : e.fout() * 2;
            for(int a : Mathf.signs){
                for(int i = 0; i < amount; i++){
                    float dx = WHUtils.dx(e.x, (range - pin * 2) * e.fin() + start, (e.time * 8 + i) * a + Mathf.randomSeed(e.id, -10, 10) + 240),
                    dy = WHUtils.dy(e.y, (range - pin * 2) * e.fin() + start, (e.time * 8 + i) * a + Mathf.randomSeed(e.id, -10, 10) + 240);
                    color(color);
                    Fill.circle(dx, dy, (width * i / amount + 0.2f) * fee);
                }
            }
        })
        )*/
        ;
        return new MultiEffect(
        new TrailEffect(lifetime, range * 2, color, color, amount, 20, width)
        .trailUpdater((e, trail, x, y, w, len, index) -> {
            rand.setSeed(e.id);
            color(color);
            float fee = e.time < e.lifetime / 2 ? e.fin() * 2 : e.fout() * 2;
            Draw.z(Layer.bullet + 0.01f);
            float random = rand.random(0f, 0.5f), offset = ((float)index / amount) * 360, r = Mathf.sign(rand.random(0, 1f) > 0.5f);
            float
            dx = WHUtils.dx(x, range * e.fin(Interp.smooth) + start, r * e.time * 6 + offset + random * 90),
            dy = WHUtils.dy(y, range * e.fin(Interp.smooth) + start, r * e.time * 6 + offset + random * 90);
            trail.length = (int)(fee * len);
            trail.update(dx, dy, w * fee);
        }),
        new TrailEffect(lifetime, range * 2, color, color, amount, 15, width)
        .trailUpdater((e, trail, x, y, w, len, index) -> {
            rand.setSeed(e.id);
            color(color);
            float fee = e.time < e.lifetime / 2 ? e.fin() * 2 : e.fout() * 2;
            Draw.z(Layer.bullet + 0.01f);
            float random = rand.random(0f, 0.5f), offset = ((float)index / amount) * 360, r = Mathf.sign(rand.random(0, 1f) > 0.5f);
            float
            dx = WHUtils.dx(x, (range - pin) * e.fin(Interp.smooth) + start, r * e.time * 4 + offset + random * 90 + 120),
            dy = WHUtils.dy(y, (range - pin) * e.fin(Interp.smooth) + start, r * e.time * 4 + offset + random * 90 + 120);
            trail.length = (int)(fee * len);
            trail.update(dx, dy, w * fee);
        }),
        new TrailEffect(lifetime, range * 2, color, color, amount, 20, width)
        .trailUpdater((e, trail, x, y, w, len, index) -> {
            rand.setSeed(e.id);
            color(color);
            float fee = e.time < e.lifetime / 2 ? e.fin() * 2 : e.fout() * 2;
            Draw.z(Layer.bullet + 0.01f);
            float random = rand.random(0f, 0.5f), offset = ((float)index / amount) * 360, r = Mathf.sign(rand.random(0, 1f) > 0.5f);
            float
            dx = WHUtils.dx(x, (range - pin * 2) * e.fin(Interp.smooth) + start, r * e.time * 2 + offset + random * 90 + 240),
            dy = WHUtils.dy(y, (range - pin * 2) * e.fin(Interp.smooth) + start, r * e.time * 2 + offset + random * 90 + 240);
            trail.length = (int)(fee * len);
            trail.update(dx, dy, w * fee);
        }));
    }


    public static Effect instShoot(Color color, Color colorInner){
        return new Effect(24, e -> {
            e.scaled(10, (b) -> {
                color(Color.white, color, b.fin());
                stroke(b.fout() * 3.0F + 0.2F);
                circle(b.x, b.y, b.fin() * 50.0F);
            });
            color(color);

            for(int i : Mathf.signs){
                Drawn.tri(e.x, e.y, 8 * e.fout(), 85, e.rotation + 90 * i);
                Drawn.tri(e.x, e.y, 8 * e.fout(), 50, 90 + 90 * i);
            }

            color(colorInner);

            for(int i : Mathf.signs){
                Drawn.tri(e.x, e.y, 5 * e.fout(), 48, e.rotation + 90 * i);
                Drawn.tri(e.x, e.y, 5 * e.fout(), 29, 90 + 90 * i);
            }
        });
    }


    public static Effect subEffect(float lifetime, float radius, int num, float childLifetime, Interp spreadOutInterp, EffectParam drawer){
        return new Effect(lifetime, radius * 2f, e -> {
            rand.setSeed(e.id);
            float finT = e.lifetime * e.fin(spreadOutInterp);

            for(int s = 0; s < num; s++){
                float sBegin = rand.random(e.lifetime - childLifetime);
                float
                fin = (finT - sBegin) / childLifetime;

                if(fin < 0 || fin > 1) continue;

                float
                fout = 1 - fin;

                rand2.setSeed(e.id + s);
                float theta = rand2.random(0f, Mathf.PI2);
                v.set(Mathf.cos(theta), Mathf.sin(theta)).scl(radius * sBegin / (e.lifetime - childLifetime));

                Tmp.c1.set(e.color).lerp(Color.white, fout * 0.7f);
                color(Tmp.c1);
                drawer.draw(e.id + s + 9999, e.x + v.x, e.y + v.y, Mathf.radiansToDegrees * theta, fin);
            }
        });
    }

    public static Effect multipRings(Color color, float radius, float amount, float baseLifetime){
        return new MultiEffect(
        new Effect(baseLifetime, radius * 2f, e -> {
            color(color, color, e.fout(Interp.pow10Out));
            stroke(2f);
            float height = 150f;
            for(int i = 0; i < amount; i++){
                float yOffset = height / amount * i * e.fin(Interp.pow10In);
                randLenVectors(e.id, 1, yOffset, 90, 0, (x, y) -> {
                    circle(x + e.x, y + e.y, radius * e.fout());
                });
            }
        }),
        WHFx.subEffect(240, radius * 2f, 12, 60f, Interp.pow3Out, (id, x, y, rotation, fin) -> {
            float height = 150f;
            float yOffset = height * fin;
            Tmp.v1.trns(90, yOffset);
            randLenVectors(id, 1, radius * fin, (a, b) -> {
                stroke(2f);
                lineAngle(a + x, Tmp.v1.y + b + y, 90, 25 * Interp.pow10Out.apply(fin) * Mathf.randomSeed(id, 2.5f));
            });
            Tmp.v2.trns(90, height * (1 - fin));
            randLenVectors(id, 2, radius * fin, (c, d) -> {
                stroke(2f);
                lineAngle(x + c, Tmp.v2.y + d + y, 90, 12 * Interp.pow2Out.apply(fin) * Mathf.randomSeed(id + 100, 2.5f));
            });
        }));
    }

    public static Effect arcSmelt(Color color, float radius, float amount, float Lifetime){
        return new Effect(Lifetime, radius * 2f, e -> {
            float flameRadiusScl = 3f, flameRadiusMag = 0.3f, circleStroke = 1.5f;
            blend(Blending.additive);
            color(color, color, e.fin());

            stroke(circleStroke * e.fout(Interp.pow2Out));
            circle(e.x, e.y, radius / 3);
            color(color);
            float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
            Fill.circle(e.x, e.y, 1.8f * e.finpow() + si);

            blend();
            reset();
            rand2.setSeed(e.id);
            e.scaled(Lifetime, (e2) -> {
                for(int s = 0; s < amount; s++){
                    float timeOffset = s * Lifetime * 2 / amount;
                    float progress = (e2.time - timeOffset) / Lifetime;
                    if(progress < 0 || progress > 1) return;

                    randLenVectors(e2.id + s, 1, radius * e.foutpow(), (x, y) -> {
                        Tmp.v1.trns(Mathf.angle(x, y), 1 - progress);
                        color(color);
                        stroke(e2.fout(Interp.pow2Out) * 1.5f);
                        lineAngle(e2.x + Tmp.v1.x + x, e2.y + Tmp.v1.y + y, Mathf.angle(x, y), 5 * e2.fout(Interp.pow2Out) * Mathf.randomSeed(e2.id, 1.5f));

                    });
                }
            });

        });
    }

    public static Effect hexagonSpread(Color color, float sizeMin, float sizeMax){
        return new Effect(20f, sizeMax * 2f, e -> {
            color(Color.white, color, e.fin() + 0.15f);
            if(e.id % 2 == 0){
                stroke(1.5f * e.fout(Interp.pow3Out));
                poly(e.x, e.y, 6, Mathf.randomSeed(e.id, sizeMin, sizeMax) * e.fin(Interp.pow2Out) + 3, 60);
            }else{
                Fill.square(e.x, e.y, Mathf.randomSeed(e.id, sizeMin * 0.5f, sizeMin * 0.8f) * e.fout(Interp.pow2Out), 45);
            }
        });
    }

    public static Effect hexagonSmoke(Color color, float lifetime, float stroke, float size, float range){
        {
            return new Effect(lifetime, e -> {
                rand.setSeed(e.id);
                randLenVectors(e.id, 5, range + e.fin() * 18f, (x, y) -> {
                    color(color);
                    stroke(stroke);
                    poly(e.x + x, e.y + y, 6, size * e.fout(Interp.pow2Out), 60);
                    color();
                });
            });
        }
    }


    public static Effect hexagonWave(Color color, float lifetime, float stroke, Interp fin, float size){

        return new Effect(lifetime, e -> {
            z(Layer.effect);
            rand.setSeed(e.id);
            color(color);
            stroke(stroke * e.fout(Interp.pow2Out));
            poly(e.x, e.y, 6, size * e.fin(fin), 60);
            color();
        });
    }

    public static Effect linePolyOut(Color color, float lifetime, float size, float stroke, int sides, float angle){
        return new Effect(lifetime, (e) -> {
            color(color);
            stroke(e.fout() * stroke);
            poly(e.x, e.y, sides, size * e.fin(Interp.pow3Out), angle);
        });
    }


    public static Effect lineCircleOut(Color color, float lifetime, float size, float stroke){
        return new Effect(lifetime, (e) -> {
            color(color);
            stroke(e.fout() * stroke);
            circle(e.x, e.y, e.fin(Interp.pow2Out) * size);
        });
    }

    public static Effect lineCircleIn(Color color, float lifetime, float size, float stroke){
        return new Effect(lifetime, (e) -> {
            color(color);
            stroke(e.fin() * stroke);
            circle(e.x, e.y, e.fout(Interp.pow2Out) * size);
        });
    }

    public static Effect circleOut(float lifetime, float radius, float thick){
        return new Effect(lifetime, radius * 2.0F, (e) -> {
            color(e.color, Color.white, e.fout() * 0.7F);
            stroke(thick * e.fout());
            circle(e.x, e.y, radius * e.fin(Interp.pow3Out));
        });
    }

    public static Effect circleOut(float lifetime, Color color, float range){
        return new Effect(lifetime, range * 1.5F, (e) -> {
            rand.setSeed(e.id);
            color(Color.white, color.cpy(), e.fin() + 0.6F);
            float circleRad = e.fin(Interp.circleOut) * range;
            stroke(Mathf.clamp(range / 24.0F, 4.0F, 20.0F) * e.fout());
            circle(e.x, e.y, circleRad);

            for(int i = 0; (float)i < Mathf.clamp(range / 12.0F, 9.0F, 60.0F); ++i){
                Tmp.v1.set(1, 0).setToRandomDirection(rand).scl(circleRad);
                Drawn.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16.0F, circleRad / 12.0F) * e.fout(),
                rand.random(circleRad / 4.0F, circleRad / 1.5F) * (1.0F + e.fin()) / 2.0F, Tmp.v1.angle() - 180.0F);
            }
        });
    }

    public static Effect circleOut(Color color, float range){
        return new Effect(Mathf.clamp(range / 2.0F, 45.0F, 360.0F), range * 1.5F, (e) -> {
            rand.setSeed(e.id);
            color(Color.white, color, e.fin() + 0.6F);
            float circleRad = e.fin(Interp.circleOut) * range;
            stroke(Mathf.clamp(range / 24.0F, 4.0F, 20.0F) * e.fout());
            circle(e.x, e.y, circleRad);

            for(int i = 0; (float)i < Mathf.clamp(range / 12.0F, 9.0F, 60.0F); ++i){
                Tmp.v1.set(1, 0).setToRandomDirection(rand).scl(circleRad);
                Drawn.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16.0F, circleRad / 12.0F) * e.fout(),
                rand.random(circleRad / 4.0F, circleRad / 1.5F) * (1.0F + e.fin()) / 2.0F, Tmp.v1.angle() - 180.0F);
            }

        });
    }

    public static Effect spreadOutSpark(float lifetime, float radius, int sparks, int sparkSpikes, float sparkLifetime, float sparkSize, float sparkLength, Interp spreadOutInterp){
        return new Effect(lifetime, radius * 2f, e -> {
            rand.setSeed(e.id);
            float finT = e.lifetime * e.fin(spreadOutInterp);

            for(int s = 0; s < sparks; s++){
                float sBegin = rand.random(e.lifetime - sparkLifetime);
                float
                fin = (finT - sBegin) / sparkLifetime;

                if(fin < 0 || fin > 1) continue;


                float
                fout = 1 - fin,
                fslope = (0.5f - Math.abs(fin - 0.5f)) * 2f;

                rand2.setSeed(e.id + s);
                v.setToRandomDirection(rand2).scl(radius * sBegin / (e.lifetime - sparkLifetime));

                Tmp.c1.set(e.color).lerp(Color.white, fout * 0.7f);
                color(Tmp.c1);
                stroke(1.2f * Mathf.curve(fout, 0, 0.22f));
                randLenVectors(e.id + s + 1, sparkSpikes, sparkSize * fin, (x, y) -> {
                    lineAngle(e.x + x + v.x, e.y + y + v.y, Mathf.angle(x, y), fslope * sparkLength + 2);
                    Drawf.light(e.x + x + v.x, e.y + y + v.y, fin * sparkLength * fslope * 1.3f, Tmp.c1, 0.7f);
                });
            }
        });
    }

    public static Effect railShoot(Color color, float length, float width, float lifetime, float spacing){
        return new Effect(lifetime, length * 2f, e -> {
            TextureRegion arrowRegion = WHContent.arrowRegion;

            color(color);

            float railF = Mathf.curve(e.fin(Interp.pow2Out), 0f, 0.25f) * Mathf.curve(e.fout(Interp.pow4Out), 0f, 0.3f) * e.fin();

            for(int i = 0; i <= length / spacing; i++){
                Tmp.v1.trns(e.rotation, i * spacing);
                float f = Interp.pow3Out.apply(Mathf.clamp((e.fin() * length - i * spacing) / spacing)) * (0.6f + railF * 0.4f);
                Draw.rect(arrowRegion, e.x + Tmp.v1.x, e.y + Tmp.v1.y, arrowRegion.width * scl * f, arrowRegion.height * scl * f, e.rotation - 90);
            }

            Tmp.v1.trns(e.rotation, 0f, (2 - railF) * tilesize * 1.4f);

            stroke(railF * 2f);
            for(int i : Mathf.signs){
                lineAngle(e.x + Tmp.v1.x * i, e.y + Tmp.v1.y * i, e.rotation, length * (0.75f + railF / 4f) * Mathf.curve(e.fout(Interp.pow5Out), 0f, 0.1f));
            }
        }).followParent(true);
    }

    public static Effect shootLine(float range, float angleRange){
        return new Effect(37f, e -> {
            rand.setSeed(e.id);

            int num = Mathf.clamp((int)range / 6, 6, 20);
            float thick = Mathf.clamp(0.75f, 1, range / 22f * rand.random(0.5f, 1f));
            color(e.color, Color.white, e.fout() * 0.7f);

            WHUtils.randLenVectors(e.id, num, 4 + (range * 1.2f) * e.fin(), range * 0.15f * e.fin(), e.rotation, angleRange, (x, y) -> {
                stroke(thick * e.fout(0.32f));
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), (e.fslope() + e.fin()) * 0.5f * (range * rand.random(0.1f, 0.3f) + rand.random(2f)) + rand.random(2f));
                Drawf.light(e.x + x, e.y + y, e.fslope() * (range * 0.5f + 14f) + 3, e.color, 0.7f);
            });
        });
    }


    public static Effect genericCharge(Color color, float size, float range, float lifetime){
        return new Effect(lifetime, e -> {
            color(color);
            stroke(size / 7f * e.fin());
            randLenVectors(e.id, 15, 3f + 60f * e.fout(), e.rotation, range, (x, y) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * size * 0.48f + size / 4f);
                Drawf.light(e.x + x, e.y + y, e.fout(0.25f) * size, color, 0.7f);
            });
            color(color);
            Fill.circle(e.x, e.y, size * Interp.pow3Out.apply(e.fin()));

        });
    }

    public static Effect genericChargeCircle(Color color, float size, float range, float lifetime){
        return new Effect(lifetime, e -> {
            z(Layer.effect - 0.001f);
            color(color);
            stroke(size / 7f * e.fin());
            rand.setSeed(e.id);
            randLenVectors(e.id, rand.random(2, 5) + rand.random(0, 5) + 3, 3f + range * e.fout() * rand.random(0.6f, 1), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fin() * size * 0.35f);
                color(color.cpy().lerp(Color.white, 0.7f));
                Fill.circle(e.x + x, e.y + y, e.fin() * size * 0.23f);
            });
            z(Layer.effect);
            color(color);
            Fill.circle(e.x, e.y, size * Interp.pow3Out.apply(e.fin()));
            color(color.cpy().lerp(Color.white, 0.7f));
            Fill.circle(e.x, e.y, size * 0.7f * Interp.pow3Out.apply(e.fin()));
        });
    }

    public static Effect arcShieldBreak = new Effect(40, e -> {
        stroke(3 * e.fout(), e.color);
        if(e.data instanceof Unit u){
            PcShieldArcAbility ab = (PcShieldArcAbility)Structs.find(u.abilities, a -> a instanceof PcShieldArcAbility);
            if(ab != null){
                Vec2 pos = Tmp.v1.set(ab.x, ab.y).rotate(u.rotation - 90f).add(u);
                arc(pos.x, pos.y, ab.radius + ab.width / 2, ab.angle / 360f, u.rotation + ab.angleOffset - ab.angle / 2f);
                arc(pos.x, pos.y, ab.radius - ab.width / 2, ab.angle / 360f, u.rotation + ab.angleOffset - ab.angle / 2f);
                for(int i : Mathf.signs){
                    float
                    px = pos.x + Angles.trnsx(u.rotation + ab.angleOffset - ab.angle / 2f * i, ab.radius + ab.width / 2),
                    py = pos.y + Angles.trnsy(u.rotation + ab.angleOffset - ab.angle / 2f * i, ab.radius + ab.width / 2),
                    px1 = pos.x + Angles.trnsx(u.rotation + ab.angleOffset - ab.angle / 2f * i, ab.radius - ab.width / 2),
                    py1 = pos.y + Angles.trnsy(u.rotation + ab.angleOffset - ab.angle / 2f * i, ab.radius - ab.width / 2);
                    line(px, py, px1, py1);
                }
            }
        }
    });

    public static Effect ExplosionSlash(Color color, float size, float range, float lifetime){
        float r = rand.random(0.5f, 1);
        return new Effect(lifetime * r, range * 2, e -> {
            color(color);
            randLenVectors(e.id, (int)Mathf.clamp(range / 8.0F, 4.0F, 18.0F), range / 8.0F, e.rotation, 360, (x, y) -> {
                for(int s : Mathf.signs){
                    Drawf.tri(e.x + x, e.y + y, e.fout() * size / 2, e.foutpow() * size * 2.5f + 6f, Mathf.angle(x, y) + s * 90f);
                }
            });
        });
    }


    public static Effect trailCharge(Color color, float length, float width, float range, int amount, float lifetime){
       /* return new Effect(lifetime, e -> {
            float fee = e.time < e.lifetime / 2 ? e.fin() * 2 : e.fout() * 2;
            z(Layer.bullet + 0.01f);
            for(int a = 1; a < amount + 1; ++a){
                rand.setSeed(e.id);
                float random = rand.random(0f, 0.5f);
                for(int i = 0; i < length; i++){
                    float
                    dx = WHUtils.dx(e.x, range * e.fout(Interp.pow2In) + random * range, (e.time * 4 + i) * a + random * 360f + Mathf.randomSeed(e.id, -10, 10)),
                    dy = WHUtils.dy(e.y, range * e.fout(Interp.pow2In) + random * range, (e.time * 4 + i) * a + random * 360f + Mathf.randomSeed(e.id, -10, 10));
                    color(color);
                    Fill.circle(dx, dy, (size * i / length) * fee);
                }
            }
        });*/
        return new TrailEffect(lifetime, 1000, color, color, amount, (int)length, width)
        .trailUpdater((e, trail, x, y, w, len, index) -> {
            rand.setSeed(e.id + index);
            float fee = e.time < e.lifetime / 2 ? e.fin() * 2 : e.fout() * 2;
            z(Layer.bullet + 0.01f);
            float random = rand.random(0f, 0.5f), offset = ((float)index / amount) * 360;
            float
            dx = WHUtils.dx(x, range * e.fout(Interp.pow2In) + random * range, e.time * 4 * rand.random(0.8f, 1.5f) + offset + random * 90),
            dy = WHUtils.dy(y, range * e.fout(Interp.pow2In) + random * range, e.time * 4 * rand.random(0.8f, 1.5f) + offset + random * 90);
            trail.length = (int)(fee * len);
            trail.update(dx, dy, w * fee);
        });
    }

    public static Effect fillCircle(Color color, float size, float lifetime, Interp inp){
        return new Effect(lifetime, e -> {
            z(Layer.effect);
            color(color);
            Fill.circle(e.x, e.y, size * e.fout(inp));
            color(Color.white);
            Fill.circle(e.x, e.y, size * 0.5f * e.fout(inp));
        });
    }

    //好几把怪
    public static Effect circleLightning(Color color, float lifetime, float lightingLifetime, int points, float size){
        Effect[] effects = new Effect[Mathf.ceil(lifetime / lightingLifetime)];
        for(int a = 0; a < effects.length; a++){
            float sizeOffset = Mathf.curve((float)a / effects.length, 0, 1f);
            float timeOffset = a * lightingLifetime;
            effects[a] = new Effect(lightingLifetime, 300f, e -> {
                float angleStep = 360f / points;
                stroke(2.5f * e.fout());
                color(color, color.cpy().lerp(Color.white, 0.3f), e.fout());
                beginLine();
                rand.setSeed(e.id + 99999);

                float firstX = 0, firstY = 0;
                for(int i = 0; i <= points; i++){
                    float angle = i * angleStep;
                    float nx = e.x + Angles.trnsx(angle, size * sizeOffset);
                    float ny = e.y + Angles.trnsy(angle, size * sizeOffset);

                    if(i == 0){
                        firstX = nx;
                        firstY = ny;
                    }

                    if(i > 0){
                        Tmp.v1.setToRandomDirection(rand).scl(size * 0.2f);
                        linePoint(nx + Tmp.v1.x, ny + Tmp.v1.y);
                    }else{
                        linePoint(nx, ny);
                    }
                }
                linePoint(firstX, firstY);
                endLine();
            }).startDelay(timeOffset);
        }
        return new MultiEffect(effects).followParent(false).rotWithParent(false);
    }


    public static Effect warningRange(TextureRegion region, float lifetime, float range){
        return new Effect(lifetime, 2000f, e -> {
            if(region == null) return;
            rand.setSeed(e.id);
            color(e.color);
            float fin = Mathf.curve(e.fin(Interp.pow2Out), 0, 0.3f);
            float fout = e.fout(Interp.pow10Out);
            stroke(4 * fin * Mathf.curve(fout, 0.9f, 1));
            for(int i = 1; i < 5; i++){
                float a = rand.random(0, 90), b = rand.random(20, 40);
                arc(e.x, e.y, range + Interp.pow5In.apply(i / 4f) * 0.2f * range + i * 0.15f * range * fout, (b + i * 10) / 360f, a + Time.time % 360 * i);
                arc(e.x, e.y, range + Interp.pow5In.apply(i / 4f) * 0.2f * range + i * 0.15f * range * fout, (b + i * 10) / 360f, a - Time.time % 360 * i + 180);
            }
            alpha(1 * fin * fout);
            stroke(6 * fin * Mathf.curve(fout, 0.5f, 1));
            TextureRegion arrowRegion = WHContent.arrowRegion;
            for(int i = 0; i < 4; i++){
                float ran = 360 * e.fout(Interp.pow3In);
                float angle = i * 90;
                Tmp.v1.trns(angle + ran, range + range * e.fout(Interp.pow2Out)).add(e.x, e.y);
                Draw.rect(arrowRegion, Tmp.v1.x, Tmp.v1.y, fin * range, fin * range, angle + ran + 90);
            }
            for(int i = 1; i < 4; i++){
                float progress = Mathf.curve(e.fin(), (i - 1) / 3f, i / 3f);
                float angle = 120f * i;
                Drawn.arcProcess(e.x, e.y, range * 0.7f, 0.2f, angle, 150, progress);
            }
            Drawn.circlePercentFlip(e.x, e.y, range + Mathf.sin(Time.time, 0.3f), Time.time, 20f);
            Draw.rect(region, e.x, e.y, 0.9f * range, 0.9f * range, 0);
        });
    }

    public static Effect diffuse(int size, Color color, float life){
        return new Effect(life, e -> {
            float f = e.fout();
            if(f < 1e-4f) return;
            float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * tilesize / 2f - f - 0.2f), w = Mathf.clamp(0.5f - f) * size * tilesize;
            stroke(3f * f, color);
            beginLine();
            for(int i = 0; i < 4; i++){
                linePoint(e.x + Geometry.d4(i).x * r + Geometry.d4(i).y * w, e.y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
                if(f < 0.5f)
                    linePoint(e.x + Geometry.d4(i).x * r - Geometry.d4(i).y * w, e.y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
            }
            endLine(true);
        });
    }

    static{
        hitSpark = new Effect(45.0F, (e) -> {
            color(e.color, Color.white, e.fout() * 0.3F);
            stroke(e.fout() * 1.6F);
            rand.setSeed(e.id);
            randLenVectors(e.id, 8, e.finpow() * 20.0F, (x, y) -> {
                float ang = Mathf.angle(x, y);
                lineAngle(e.x + x, e.y + y, ang, e.fout() * rand.random(1.95F, 4.25F) + 1.0F);
            });
        });
        hitSparkLarge = new Effect(40.0F, (e) -> {
            color(e.color, Color.white, e.fout() * 0.3F);
            stroke(e.fout() * 1.6F);
            rand.setSeed(e.id);
            randLenVectors(e.id, 18, e.finpow() * 27.0F, (x, y) -> {
                float ang = Mathf.angle(x, y);
                lineAngle(e.x + x, e.y + y, ang, e.fout() * (float)rand.random(4, 8) + 2.0F);
            });
        });
        hitSparkHuge = new Effect(70.0F, (e) -> {
            color(e.color, Color.white, e.fout() * 0.3F);
            stroke(e.fout() * 1.6F);
            rand.setSeed(e.id);
            randLenVectors(e.id, 26, e.finpow() * 65.0F, (x, y) -> {
                float ang = Mathf.angle(x, y);
                lineAngle(e.x + x, e.y + y, ang, e.fout() * (float)rand.random(6, 9) + 3.0F);
            });
        });

        lightningSpark = new Effect(Fx.chainLightning.lifetime, (e) -> {
            color(Color.white, e.color, e.fin() + 0.25F);
            stroke(0.65F + e.fout());
            randLenVectors(e.id, 3, e.fin() * e.rotation + 6.0F, (x, y) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 4.0F + 2.0F);
            });
            Fill.circle(e.x, e.y, 2.5F * e.fout());
        });
        TankAG7BulletExplode = (new Effect(300.0F, 1600.0F, (e) -> {
            float rad = 120.0F;
            rand.setSeed(e.id);
            color(Color.white, e.color, e.fin() + 0.6F);
            float circleRad = e.fin(Interp.circleOut) * rad * 4.0F;
            stroke(12.0F * e.fout());
            circle(e.x, e.y, circleRad);

            for(int i = 0; i < 24; ++i){
                Tmp.v1.set(1.0F, 0.0F).setToRandomDirection(rand).scl(circleRad);
                Drawn.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, rand.random(circleRad / 16.0F, circleRad / 12.0F) * e.fout(), rand.random(circleRad / 4.0F, circleRad / 1.5F) * (1.0F + e.fin()) / 2.0F, Tmp.v1.angle() - 180.0F);
            }

            blend(Blending.additive);
            z(110.1F);
            Fill.light(e.x, e.y, circleVertices(circleRad), circleRad, Color.clear, Tmp.c1.set(getColor()).a(e.fout(Interp.pow10Out)));
            blend();
            z(110.0F);
            e.scaled(120.0F, (ix) -> {
                color(Color.white, ix.color, ix.fin() + 0.4F);
                Fill.circle(ix.x, ix.y, rad * ix.fout());
                stroke(18.0F * ix.fout());
                circle(ix.x, ix.y, ix.fin(Interp.circleOut) * rad * 1.1F);
                randLenVectors(ix.id, 40, rad / 3.0F, rad * ix.fin(Interp.pow2Out), (x, y) -> {
                    lineAngle(ix.x + x, ix.y + y, Mathf.angle(x, y), ix.fslope() * 25.0F + 10.0F);
                });
                randLenVectors(ix.id, (int)(rad / 4.0F), rad / 6.0F, rad * (1.0F + ix.fout(Interp.circleOut)) / 1.5F, (x, y) -> {
                    float angle = Mathf.angle(x, y);
                    float width = ix.foutpowdown() * rand.random(rad / 6.0F, rad / 3.0F);
                    float length = rand.random(rad / 2.0F, rad * 5.0F) * ix.fout(Interp.circleOut);
                    color(ix.color);
                    Drawn.tri(ix.x + x, ix.y + y, width, rad / 3.0F * ix.fout(Interp.circleOut), angle - 180.0F);
                    Drawn.tri(ix.x + x, ix.y + y, width, length, angle);
                    color(Color.black);
                    width *= ix.fout();
                    Drawn.tri(ix.x + x, ix.y + y, width / 2.0F, rad / 3.0F * ix.fout(Interp.circleOut) * 0.9F * ix.fout(), angle - 180.0F);
                    Drawn.tri(ix.x + x, ix.y + y, width / 2.0F, length / 1.5F * ix.fout(), angle);
                });
                color(Color.black);
                Fill.circle(ix.x, ix.y, rad * ix.fout() * 0.75F);
            });
            Drawf.light(e.x, e.y, rad * e.fout(Interp.circleOut) * 4.0F, e.color, 0.7F);
        })).layer(Layer.effect + 0.001f);


        posLightning = (new Effect(PositionLightning.lifetime, 1500, e -> {
            if(!(e.data instanceof Vec2Seq)) return;
            Vec2Seq lines = e.data();

            color(e.color, Color.white, e.fout() * 0.6f);

            stroke(e.rotation * e.fout());

            Fill.circle(lines.firstTmp().x, lines.firstTmp().y, getStroke() / 2f);

            for(int i = 0; i < lines.size() - 1; i++){
                Vec2 cur = lines.setVec2(i, Tmp.v1);
                Vec2 next = lines.setVec2(i + 1, Tmp.v2);

                line(cur.x, cur.y, next.x, next.y, false);
                Fill.circle(next.x, next.y, getStroke() / 2f);
            }
        })).layer(Layer.effect - 0.001f);

        attackWarningPos = new Effect(120f, 2000f, e -> {
            if(!(e.data instanceof Position)) return;

            e.lifetime = e.rotation;
            Position pos = e.data();

            color(e.color);
            TextureRegion arrowRegion = WHContent.arrowRegion;
            float scl = Mathf.curve(e.fout(), 0f, 0.1f);
            stroke(2 * scl);
            line(pos.getX(), pos.getY(), e.x, e.y);
            Fill.circle(pos.getX(), pos.getY(), getStroke());
            Fill.circle(e.x, e.y, getStroke());
            Tmp.v1.set(e.x, e.y).sub(pos).scl(e.fin(Interp.pow2In)).add(pos);
            Draw.rect(arrowRegion, Tmp.v1.x, Tmp.v1.y, arrowRegion.width * scl * Draw.scl, arrowRegion.height * scl * Draw.scl, pos.angleTo(e.x, e.y) - 90f);
        });

        attackWarningRange = new Effect(240f, 2000f, e -> {
            color(e.color);
            stroke(2 * e.fout());
            circle(e.x, e.y, e.rotation);

            for(float i = 0.75f; i < 1.5f; i += 0.25f){
                spikes(e.x, e.y, e.rotation / i, e.rotation / 10f, 4, e.time);
                spikes(e.x, e.y, e.rotation / i / 1.5f, e.rotation / 12f, 4, -e.time * 1.25f);
            }

            TextureRegion arrowRegion = WHContent.arrowRegion;
            float scl = Mathf.curve(e.fout(), 0f, 0.1f);

            for(int l = 0; l < 4; l++){
                float angle = 90 * l;
                float regSize = e.rotation / 150f;
                for(int i = 0; i < 4; i++){
                    Tmp.v1.trns(angle, (i - 4) * tilesize * e.rotation / tilesize / 4);
                    float f = (100 - (Time.time - 25 * i) % 100) / 100;

                    Draw.rect(arrowRegion, e.x + Tmp.v1.x, e.y + Tmp.v1.y, arrowRegion.width * regSize * f * scl, arrowRegion.height * regSize * f * scl, angle - 90);
                }
            }

        });


        circleOut = new Effect(60.0F, 500.0F, (e) -> {
            stroke(2.5F * e.fout(), e.color);
            circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
        });
        circleOutQuick = new Effect(30.0F, 500.0F, (e) -> {
            stroke(2.5F * e.fout(), e.color);
            circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
        });
        circleOutLong = new Effect(120.0F, 500.0F, (e) -> {
            stroke(2.5F * e.fout(), e.color);
            circle(e.x, e.y, e.rotation * e.fin(Interp.pow3Out));
        });
        circleSplash = new Effect(26.0F, (e) -> {
            color(Color.white, e.color, e.fin() + 0.15F);
            randLenVectors(e.id, 4, 3.0F + 23.0F * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 3.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 3.5F, e.color, 0.7F);
            });
        });
        spawnWave = new Effect(60f, e -> {
            stroke(3 * e.fout(), e.color);
            circle(e.x, e.y, e.rotation * e.finpow());
        });
        spawnGround = new Effect(60f, e -> {
            color(e.color, Pal.gray, e.fin());
            randLenVectors(e.id, (int)(e.rotation * 1.35f), e.rotation * tilesize / 1.125f * e.fin(), (x, y) -> Fill.square(e.x + x, e.y + y, e.rotation * e.fout(), 45));
        });
        missileShoot = new Effect(130.0F, 300.0F, (e) -> {
            color(e.color);
            alpha(0.67F * e.fout(0.9F));
            rand.setSeed(e.id);

            for(int i = 0; i < 35; ++i){
                v9.trns(e.rotation + 180.0F + rand.range(21.0F), rand.random(e.finpow() * 90.0F)).add(rand.range(3.0F), rand.range(3.0F));
                e.scaled(e.lifetime * rand.random(0.2F, 1.0F), (b) -> {
                    Fill.circle(e.x + v9.x, e.y + v9.y, b.fout() * 9.0F + 0.3F);
                });
            }
        });
        shuttle = new Effect(70f, 800f, e -> {
            if(!(e.data instanceof Float)) return;
            float len = e.data();

            color(e.color, Color.white, e.fout() * 0.3f);
            stroke(e.fout() * 2.2F);

            randLenVectors(e.id, (int)Mathf.clamp(len / 12, 10, 40), e.finpow() * len, e.rotation, 360f, (x, y) -> {
                float ang = Mathf.angle(x, y);
                lineAngle(e.x + x, e.y + y, ang, e.fout() * len * 0.15f + len * 0.025f);
            });

            float fout = e.fout(Interp.exp10Out);
            for(int i : Mathf.signs){
                Drawn.tri(e.x, e.y, len / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
            }
        });

        shuttleDark = new Effect(70f, 800f, e -> {
            if(!(e.data instanceof Float)) return;
            float len = e.data();

            color(e.color, Color.white, e.fout() * 0.3f);
            stroke(e.fout() * 2.2F);

            randLenVectors(e.id, (int)Mathf.clamp(len / 12, 10, 40), e.finpow() * len, e.rotation, 360f, (x, y) -> {
                float ang = Mathf.angle(x, y);
                lineAngle(e.x + x, e.y + y, ang, e.fout() * len * 0.15f + len * 0.025f);
            });

            float fout = e.fout(Interp.exp10Out);
            for(int i : Mathf.signs){
                Drawn.tri(e.x, e.y, len / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
            }

            float len1 = len * 0.66f;
            z(EFFECT_MASK);
            color(Color.black);
            for(int i : Mathf.signs){
                Drawn.tri(e.x, e.y, len1 / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len1 * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
            }

            z(EFFECT_BOTTOM);
            for(int i : Mathf.signs){
                Drawn.tri(e.x, e.y, len1 / 17f * fout * (Mathf.absin(0.8f, 0.07f) + 1), len1 * 3f * Interp.swingOut.apply(Mathf.curve(e.fin(), 0, 0.7f)) * (Mathf.absin(0.8f, 0.12f) + 1) * e.fout(0.2f), e.rotation + 90 + i * 90);
            }
        }).layer(Layer.effect - 1f);

        shuttleLerp = new Effect(180f, 800f, e -> {
            if(!(e.data instanceof Float)) return;
            float f = Mathf.curve(e.fin(Interp.pow5In), 0f, 0.07f) * Mathf.curve(e.fout(), 0f, 0.4f);
            float len = e.data();

            color(e.color);
            v.trns(e.rotation - 90, (len + Mathf.randomSeed(e.id, 0, len)) * e.fin(Interp.circleOut));
            for(int i : Mathf.signs)
                Drawn.tri(e.x + v.x, e.y + v.y, Mathf.clamp(len / 8, 8, 25) * (f + e.fout(0.2f) * 2f) / 3.5f, len * 1.75f * e.fin(Interp.circleOut), e.rotation + 90 + i * 90);
        });
        lightningHitSmall = new Effect(Fx.chainLightning.lifetime, (e) -> {
            color(Color.white, e.color, e.fin() + 0.25F);
            e.scaled(7.0F, (s) -> {
                stroke(0.5F + s.fout());
                circle(e.x, e.y, s.fin() * (e.rotation + 12.0F));
            });
            stroke(0.75F + e.fout());
            randLenVectors(e.id, 6, e.fin() * e.rotation + 7.0F, (x, y) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 4.0F + 2.0F);
            });
            Fill.circle(e.x, e.y, 2.5F * e.fout());
        });
        lightningHitLarge = new Effect(50, 180, (e) -> {
            color(e.color);
            Drawf.light(e.x, e.y, e.fout() * 90.0F, e.color, 0.7F);
            e.scaled(25.0F, (t) -> {
                stroke(3.0F * t.fout());
                circle(e.x, e.y, 3.0F + t.fin(Interp.pow3Out) * 60);
            });
            Fill.circle(e.x, e.y, e.fout() * 8.0F);
            randLenVectors((e.id + 1), 4, 1.0F + 60.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 5.0F);
            });
            color(Color.gray);
            randLenVectors(e.id, 8, 2.0F + 30.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F);
            });
        });
        lightningFade = (new Effect(PositionLightning.lifetime, 1200.0f, e -> {
            if(!(e.data instanceof Vec2Seq)) return;
            Vec2Seq points = e.data();

            e.lifetime = points.size() < 2 ? 0 : 1000;
            int strokeOffset = (int)e.rotation;


            if(points.size() > strokeOffset + 1 && strokeOffset > 0 && points.size() > 2){
                points.removeRange(0, points.size() - strokeOffset - 1);
            }

            if(!state.isPaused() && points.any()){
                points.remove(0);
            }

            if(points.size() < 2) return;

            Vec2 data = points.peekTmp(); //x -> stroke, y -> fadeOffset;
            float stroke = data.x;
            float fadeOffset = data.y;

            color(e.color);
            for(int i = 1; i < points.size() - 1; i++){
//				Draw.alpha(Mathf.clamp((float)(i + fadeOffset - e.time) / points.size()));
                stroke(Mathf.clamp((i + fadeOffset / 2f) / points.size() * (strokeOffset - (points.size() - i)) / strokeOffset) * stroke);
                Vec2 from = points.setVec2(i - 1, Tmp.v1);
                Vec2 to = points.setVec2(i, Tmp.v2);
                line(from.x, from.y, to.x, to.y, false);
                Fill.circle(from.x, from.y, getStroke() / 2);
            }

            Vec2 last = points.tmpVec2(points.size() - 2);
            Fill.circle(last.x, last.y, getStroke() / 2);
        })).layer(Layer.effect - 0.001f);

        sapPlasmaShoot = new Effect(25.0F, (e) -> {
            color(Color.white, Pal.sapBullet, e.fin());
            randLenVectors(e.id, 13, e.finpow() * 20.0F, e.rotation, 23.0F, (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 5.0F);
                Fill.circle(e.x + x / 1.2F, e.y + y / 1.2F, e.fout() * 3.0F);
            });
        });
        coloredHitSmall = new Effect(14.0F, (e) -> {
            color(Color.white, e.color, e.fin());
            e.scaled(7.0F, (s) -> {
                stroke(0.5F + s.fout());
                Fill.circle(e.x, e.y, s.fin() * 5.0F);
            });
            stroke(0.5F + e.fout());
            randLenVectors(e.id, 5, e.fin() * 15.0F, (x, y) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 3.0F + 1.0F);
            });
        });
        hugeTrail = new Effect(40.0F, (e) -> {
            color(e.color);
            alpha(e.fout(0.85F) * 0.85F);
            randLenVectors(e.id, 6, 2.0F + e.rotation * 5.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout(Interp.pow3Out) * e.rotation);
            });
        });
        hugeSmokeGray = new Effect(40.0F, (e) -> {
            color(Color.gray, Color.darkGray, e.fin());
            randLenVectors(e.id, 6, 2.0F + 19.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout() * 2.0F);
            });
            e.scaled(25.0F, (i) -> {
                randLenVectors(e.id, 6, 2.0F + 19.0F * i.finpow(), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, i.fout() * 4.0F);
                });
            });
        });
        hugeSmoke = new Effect(40.0F, (e) -> {
            color(e.color);
            randLenVectors(e.id, 6, 2.0F + 19.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout() * 2.0F);
            });
            e.scaled(25.0F, (i) -> {
                randLenVectors(e.id, 6, 2.0F + 19.0F * i.finpow(), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, i.fout() * 4.0F);
                });
            });
        });
        hugeSmokeLong = new Effect(120.0F, (e) -> {
            color(e.color);
            randLenVectors(e.id, 6, 2.0F + 19.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x / 2.0F, e.y + y / 2.0F, e.fout() * 2.0F);
            });
            e.scaled(25.0F, (i) -> {
                randLenVectors(e.id, 6, 2.0F + 19.0F * i.finpow(), (x, y) -> {
                    Fill.circle(e.x + x, e.y + y, i.fout() * 4.0F);
                });
            });
        });
        square45_4_45 = new Effect(45.0F, (e) -> {
            color(e.color);
            randLenVectors(e.id, 5, 20.0F * e.finpow(), (x, y) -> {
                Fill.square(e.x + x, e.y + y, 4.0F * e.fout(), 45.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 6.0F, e.color, 0.7F);
            });
        });
        square45_6_45 = new Effect(45.0F, (e) -> {
            color(e.color, Color.white, e.fout() * 0.6F);
            randLenVectors(e.id, 6, 27.0F * e.finpow(), (x, y) -> {
                Fill.square(e.x + x, e.y + y, 5.0F * e.fout(), 45.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 9.0F, e.color, 0.7F);
            });
        });
        square45_6_45_Charge = new Effect(90.0F, (e) -> {
            color(e.color, Color.white, e.fin() * 0.6F);
            randLenVectors(e.id, 12, 60.0F * e.fout(Interp.pow4Out), (x, y) -> {
                Fill.square(e.x + x, e.y + y, 5.0F * e.fin(), 45.0F);
                Drawf.light(e.x + x, e.y + y, e.fin() * 9.0F, e.color, 0.7F);
            });
            stroke(2.0F * e.fin());
            circle(e.x, e.y, 80.0F * e.fout(Interp.pow5Out));
        });
        square45_8_45 = new Effect(45.0F, (e) -> {
            color(e.color, Color.white, e.fout() * 0.6F);
            randLenVectors(e.id, 7, 34.0F * e.finpow(), (x, y) -> {
                Fill.square(e.x + x, e.y + y, 8.0F * e.fout(), 45.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 12.0F, e.color, 0.7F);
            });
        });
        crossBlastArrow45 = new Effect(65.0F, 140.0F, (e) -> {
            color(e.color, Color.white, e.fout() * 0.55F);
            Drawf.light(e.x, e.y, e.fout() * 70.0F, e.color, 0.7F);
            e.scaled(10.0F, (ix) -> {
                stroke(1.35F * ix.fout());
                circle(e.x, e.y, 49.0F * ix.finpow());
            });
            rand.setSeed(e.id);
            float sizeDiv = 138.0F;
            float randL = rand.random(sizeDiv);
            float f = Mathf.curve(e.fin(), 0.0F, 0.05F);

            for(int i = 0; i < 4; ++i){
                Tmp.v1.trns((float)(45 + i * 90), 66.0F);
                Drawn.arrow(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 27.5F * (e.fout() * 3.0F + 1.0F) / 4.0F * e.fout(Interp.pow3In), (sizeDiv + randL) * f * e.fout(Interp.pow3), -randL / 6.0F * f, (float)(i * 90 + 45));
            }

        });
        ScrossBlastArrow45 = new Effect(80.0F, 50.0F, (e) -> {
            color(e.color, Color.white, e.fout() * 0.55F);
            Drawf.light(e.x, e.y, e.fout() * 70.0F, e.color, 0.7F);
            e.scaled(10.0F, (ix) -> {
                stroke(1.1F * ix.fout());
                circle(e.x, e.y, 78.0F * ix.finpow());
            });
            rand.setSeed(e.id);
            float sizeDiv = 60.0F;
            float randL = rand.random(sizeDiv);
            float f = Mathf.curve(e.fin(), 0.0F, 0.05F);

            for(int i = 0; i < 4; ++i){
                Tmp.v1.trns((float)(45 + i * 90), 66.0F);
                Drawn.arrow(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 10.0F * (e.fout() * 3.0F + 1.0F) / 4.0F * e.fout(Interp.pow3In),
                (sizeDiv + randL) * f * e.fout(Interp.pow3), -randL / 6.0F * f, (float)(i * 90 + 45));
            }

        });
        crossBlast_45 = new Effect(35.0F, 140.0F, (e) -> {
            color(e.color, Color.white, e.fout() * 0.55F);
            Drawf.light(e.x, e.y, e.fout() * 70.0F, e.color, 0.7F);
            e.scaled(10.0F, (ix) -> {
                stroke(1.35F * ix.fout());
                Fill.circle(e.x, e.y, 55.0F * ix.finpow());
            });
            rand.setSeed(e.id);
            float sizeDiv = 60.0F;
            float randL = rand.random(sizeDiv);

            for(int i = 0; i < 4; ++i){
                Drawn.tri(e.x, e.y, 5.85F * (e.fout() * 3.0F + 1.0F) / 4.0F * (e.fout(Interp.pow3In) + 0.5F) / 1.5F, (sizeDiv + randL) * Mathf.curve(e.fin(), 0.0F, 0.05F) * e.fout(Interp.pow3), (float)(i * 90 + 45));
            }

        });

        crossSpinBlast = (new Effect(150.0F, 600.0F, (e) -> {
            float scl = 1.0F;
            color(e.color, Color.white, e.fout() * 0.25F);
            float extend = Mathf.curve(e.fin(), 0.0F, 0.015F) * scl;
            float rot = e.fout(Interp.pow3In);

            for(int i : Mathf.signs){
                Drawn.tri(e.x, e.y, 9.0F * e.foutpowdown() * scl, 100.0F + 300.0F * extend, e.rotation + 180 * rot + (float)(90 * i) + 45.0F);
                Drawn.tri(e.x, e.y, 9.0F * e.foutpowdown() * scl, 100.0F + 300.0F * extend, e.rotation + 180 * rot + (float)(90 * i) - 45.0F);
            }

            for(int i : Mathf.signs){
                Drawn.tri(e.x, e.y, 6.0F * e.foutpowdown() * scl, 40.0F + 120.0F * extend, e.rotation + 270 * rot + (float)(90 * i) + 45.0F);
                Drawn.tri(e.x, e.y, 6.0F * e.foutpowdown() * scl, 40.0F + 120.0F * extend, e.rotation + 270 * rot + (float)(90 * i) - 45.0F);
            }
        })).followParent(true).layer(100.0F);


        PlasmaFireBurn = (new Effect(25.0F, (e) -> {
            color(WHPal.SkyBlue, Color.gray, e.fin() * 0.75F);
            randLenVectors(e.id, 2, 2.0F + e.fin() * 7.0F, (x, y) -> {
                Fill.poly(e.x + x, e.y + y, 6, 0.2F + e.fout() * 4f, 45);
            });
        }));

        skyTrail = new Effect(22.0F, (e) -> {
            color(Pal.techBlue, Pal.gray, e.fin() * 0.6F);
            rand.setSeed(e.id);
            randLenVectors(e.id, 3, e.finpow() * 13.0F, e.rotation - 180.0F, 30.0F, (x, y) -> {
                Fill.poly(e.x + x, e.y + y, 6, (float)rand.random(2, 4) * e.foutpow(), e.rotation);
            });
        });
        trailToGray = new Effect(50.0F, (e) -> {
            color(e.color, Color.gray, e.fin());
            randLenVectors(e.id, 2, 8.0F * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.rotation * e.fout());
            });
        });
        trailFromWhite = new Effect(50.0F, (e) -> {
            color(e.color, Color.white, e.fout() * 0.35F);
            randLenVectors(e.id, 2, 8.0F * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.rotation * e.fout());
            });
        });


        chainLightningFade = new Effect(220f, 500f, e -> {
            if(!(e.data instanceof Position)) return;
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
            for(i = 0; i < (int)(links * fin); i++){
                if(i == links - 1){
                    nx = tx;
                    ny = ty;
                }else{
                    float len = (i + 1) * spacing;
                    Tmp.v1.setToRandomDirection(rand).scl(range / 2f);
                    nx = e.x + normx * len + Tmp.v1.x;
                    ny = e.y + normy * len + Tmp.v1.y;
                }

                linePoint(nx, ny);
            }

            if(i < links){
                float f = Mathf.clamp(fin * links % 1);
                float len = (i + 1) * spacing;
                Tmp.v1.setToRandomDirection(rand).scl(range / 2f);
                Tmp.v2.set(nx, ny);
                if(i == links - 1) Tmp.v2.lerp(tx, ty, f);
                else Tmp.v2.lerp(e.x + (normx * len + Tmp.v1.x), e.y + (normy * len + Tmp.v1.y), f);

                linePoint(Tmp.v2.x, Tmp.v2.y);
                Fill.circle(Tmp.v2.x, Tmp.v2.y, getStroke() / 2);
            }

            endLine();
        }).followParent(false);


        /**{@link EffectContainer} as Target */
        chainLightningFadeReversed = new Effect(220f, 500f, e -> {
            if(!(e.data instanceof Position)) return;
            Position p = e.data();
            float tx = e.x, ty = e.y, dst = Mathf.dst(p.getX(), p.getY(), tx, ty);
            Tmp.v1.set(e.x, e.y).sub(p).nor();

            e.lifetime = dst * 0.3f;
            float normx = Tmp.v1.x, normy = Tmp.v1.y;
            float range = e.rotation;
            int links = Mathf.ceil(dst / range);
            float spacing = dst / links;

            stroke(2.5f * Mathf.curve(e.fout(), 0, 0.7f));
            color(e.color, Color.white, e.fout() * 0.6f);

            beginLine();

            Fill.circle(p.getX(), p.getY(), getStroke() / 2);
            linePoint(p);

            rand.setSeed(e.id);

            float fin = Mathf.curve(e.fin(), 0, lightningAlign);
            int i;
            float nx = p.getX(), ny = p.getY();
            for(i = 0; i < (int)(links * fin); i++){
                if(i == links - 1){
                    nx = tx;
                    ny = ty;
                }else{
                    float len = (i + 1) * spacing;
                    Tmp.v1.setToRandomDirection(rand).scl(range / 2f);
                    nx = p.getX() + normx * len + Tmp.v1.x;
                    ny = p.getY() + normy * len + Tmp.v1.y;
                }

                linePoint(nx, ny);
            }

            if(i < links){
                float f = Mathf.clamp(fin * links % 1);
                float len = (i + 1) * spacing;
                Tmp.v1.setToRandomDirection(rand).scl(range / 2f);
                Tmp.v2.set(nx, ny);
                if(i == links - 1) Tmp.v2.lerp(tx, ty, f);
                else Tmp.v2.lerp(p.getX() + (normx * len + Tmp.v1.x), p.getY() + (normy * len + Tmp.v1.y), f);

                linePoint(Tmp.v2.x, Tmp.v2.y);
                Fill.circle(Tmp.v2.x, Tmp.v2.y, getStroke() / 2);
            }

            endLine();
        }).followParent(false);
        techBlueCircleSplash = new Effect(26.0F, (e) -> {
            color(Pal.techBlue);
            randLenVectors(e.id, 4, 3.0F + 23.0F * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 3.0F);
                Drawf.light(e.x + x, e.y + y, e.fout() * 3.5F, Pal.techBlue, 0.7F);
            });
        });
        techBlueExplosion = new Effect(40.0F, (e) -> {
            color(Pal.techBlue);
            e.scaled(20.0F, (i) -> {
                stroke(3.0F * i.foutpow());
                circle(e.x, e.y, 3.0F + i.finpow() * 80.0F);
            });
            stroke(e.fout());
            randLenVectors((e.id + 1), 8, 1.0F + 60.0F * e.finpow(), (x, y) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1.0F + e.fout() * 3.0F);
            });
            color(Color.gray);
            randLenVectors(e.id, 5, 2.0F + 70.0F * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 4.0F + 0.5F);
            });
            Drawf.light(e.x, e.y, e.fout(Interp.pow2Out) * 100.0F, Pal.techBlue, 0.7F);
        });
        techBlueCharge = new Effect(130.0F, (e) -> {
            rand.setSeed(e.id);
            randLenVectors(e.id, 12, 140.0F * e.fout(Interp.pow3Out), (x, y) -> {
                color(Pal.techBlue);
                float rad = rand.random(9.0F, 18.0F);
                float scl = rand.random(0.6F, 1.0F);
                float dx = e.x + scl * x;
                float dy = e.y + scl * y;
                Fill.circle(dx, dy, e.fin() * rad);
                color(Pal.techBlue);
                z(110.0001F);
                Fill.circle(dx, dy, e.fin() * rad / 1.8F);
                z(99.89F);
                Fill.circle(dx, dy, e.fin() * rad / 1.8F);
                z(110.0F);
                Drawf.light(dx, dy, e.fin() * rad * 1.5F, Pal.techBlue, 0.7F);
            });
        });
        techBlueChargeBegin = new Effect(130.0F, (e) -> {
            color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fin() * 32.0F);
            stroke(e.fin() * 3.7F);
            circle(e.x, e.y, e.fout() * 80.0F);
            z(110.0001F);
            color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fin() * 20.0F);
            z(99.89F);
            color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fin() * 22.0F);
            Drawf.light(e.x, e.y, e.fin() * 35.0F, Pal.techBlue, 0.7F);
        });
        largeTechBlueHitCircle = new Effect(20.0F, (e) -> {
            color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 44.0F);
            randLenVectors(e.id, 5, 60.0F * e.fin(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 8.0F);
            });
            color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 30.0F);
            Drawf.light(e.x, e.y, e.fout() * 55.0F, Pal.techBlue, 0.7F);
        });
        largeTechBlueHit = (new Effect(50.0F, (e) -> {
            color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 44.0F);
            stroke(e.fout() * 3.2F);
            circle(e.x, e.y, e.fin() * 80.0F);
            stroke(e.fout() * 2.5F);
            circle(e.x, e.y, e.fin() * 50.0F);
            stroke(e.fout() * 3.2F);
            randLenVectors(e.id, 30, 18.0F + 80.0F * e.fin(), (x, y) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fslope() * 14.0F + 5.0F);
            });
            z(110.0001F);
            color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 30.0F);
            Drawf.light(e.x, e.y, e.fout() * 80.0F, Pal.techBlue, 0.7F);
            z(99.89F);
            Fill.circle(e.x, e.y, e.fout() * 31.0F);
            z(109.9999F);
        })).layer(109.9999F);
        mediumTechBlueHit = new Effect(23.0F, (e) -> {
            color(Pal.techBlue);
            stroke(e.fout() * 2.8F);
            circle(e.x, e.y, e.fin() * 60.0F);
            stroke(e.fout() * 2.12F);
            circle(e.x, e.y, e.fin() * 35.0F);
            stroke(e.fout() * 2.25F);
            randLenVectors(e.id, 9, 7.0F + 60.0F * e.finpow(), (x, y) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4.0F + e.fout() * 12.0F);
            });
            Fill.circle(e.x, e.y, e.fout() * 22.0F);
            color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 14.0F);
            Drawf.light(e.x, e.y, e.fout() * 80.0F, Pal.techBlue, 0.7F);
        });
        techBlueSmokeBig = new Effect(30.0F, (e) -> {
            color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 32.0F);
            color(Pal.techBlue);
            Fill.circle(e.x, e.y, e.fout() * 20.0F);
            Drawf.light(e.x, e.y, e.fout() * 36.0F, Pal.techBlue, 0.7F);
        });
        techBlueShootBig = new Effect(40.0F, 100.0F, (e) -> {
            color(Pal.techBlue);
            stroke(e.fout() * 3.7F);
            circle(e.x, e.y, e.fin() * 100.0F + 15.0F);
            stroke(e.fout() * 2.5F);
            circle(e.x, e.y, e.fin() * 60.0F + 15.0F);
            randLenVectors(e.id, 15, 7.0F + 60.0F * e.finpow(), (x, y) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 4.0F + e.fout() * 16.0F);
            });
            Drawf.light(e.x, e.y, e.fout() * 120.0F, Pal.techBlue, 0.7F);
        });
        healReceiveCircle = new Effect(11.0F, (e) -> {
            color(e.color);
            stroke(e.fout() * 1.667F);
            circle(e.x, e.y, 2.0F + e.finpow() * 7.0F);
        });
        healSendCircle = new Effect(22.0F, (e) -> {
            color(e.color);
            stroke(e.fout() * 2.0F);
            circle(e.x, e.y, e.finpow() * e.rotation);
        });


        jumpTrailOut = new Effect(120f, 200, e -> {
            if(!(e.data instanceof UnitType)) return;
            UnitType type = e.data();
            color(type.engineColor == null ? e.color : type.engineColor);

            if(type.engineLayer > 0) z(type.engineLayer);
            else z((type.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) - 0.001f);

            Tmp.v2.trns(e.rotation, 2300);

            for(int index = 0; index < type.engines.size; index++){
                UnitEngine engine = type.engines.get(index);

                if(Angles.angleDist(engine.rotation, -90) > 75) return;
                float ang = Mathf.slerp(engine.rotation, -90, 0.75f);

                //noinspection SuspiciousNameCombination
                Tmp.v1.trns(e.rotation, engine.y, -engine.x).add(Tmp.v2);

                rand.setSeed(e.id);
                e.scaled(80, i -> {
                    Drawn.tri(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 3f * i.fout(Interp.slowFast), 2300 + rand.range(120), i.rotation + ang - 90);
                    Fill.circle(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 3f * i.fout(Interp.slowFast));
                });

                randLenVectors(e.id + index, 42, 2330, e.rotation + ang - 90, 0f, (x, y) ->
                lineAngle(e.x + x + Tmp.v1.x, e.y + y + Tmp.v1.y, Mathf.angle(x, y), e.fout() * 60));
            }
        });

        groundRise = (new Effect(30.0F, (e) -> {
            Tile t = world.tileWorld(e.x, e.y);
            if(t != null){
                Floor f = t.floor();
                if(!(f instanceof SteamVent)){
                    TextureRegion region = f.variantRegions[Mathf.randomSeed(t.pos(), 0, Math.max(0, f.variantRegions.length - 1))];
                    float x = t.drawx();
                    float y = t.drawy() + e.rotation * e.fout();
                    z(z() - (float)t.y / (float)world.height() / 1000.0F);

                    for(int i = 0; i < region.width; ++i){
                        PixmapRegion image = Core.atlas.getPixmap(region);
                        float c1 = Tmp.c1.set(image.get(i, 0)).toFloatBits();
                        float c2 = Tmp.c2.set(Tmp.c1).lerp(Color.black, e.fout() / 4.0F).toFloatBits();
                        float px = x - (float)region.width / 4.0F / 2.0F + (float)i / 4.0F;
                        float py = y - (float)region.height / 4.0F / 2.0F;
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
            color(Color.valueOf("788AD7FF"), Color.white, e.fin());
            randLenVectors(e.id, 3, 3.0F + 24.0F * e.fin(), 5.0F, (x, y) -> {
                float randN = rand.random(120.0F);
                Fill.poly(e.x + x, e.y + y, 4, e.fout() * 8.0F * rand.random(0.8F, 1.2F), e.rotation + randN * e.fin());
            });
        });
        triSpark2 = new Effect(26.0F, (e) -> {
            rand.setSeed(e.id);
            color(Color.valueOf("DBD58C"), Color.white, e.fin());
            randLenVectors(e.id, 3, 3.0F + 24.0F * e.fin(), 5.0F, (x, y) -> {
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
                randLenVectors(e.id, e.fin(Interp.pow10Out), (int)(intensity * 2), 10f * intensity, (x, y, in, out) -> {
                    float fout = e.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                    float rad = 4 * intensity * fout;
                    Fill.circle(e.x + x, e.y + y, rad);
                    Drawf.light(e.x + x, e.y + y, rad * 2.5f, b.color, 0.5f);
                });
            });
        }).layer(Layer.bullet - 0.5f);

        tank3sExplosionSmoke = new Effect(180f, 300f, b -> {
            float intensity = 2f;
            color(b.color, 0.7f);
            for(int i = 0; i < 4; i++){
                rand.setSeed(b.id * 2 + i);
                float lenScl = rand.random(0.5f, 1f);
                int fi = i;
                b.scaled(b.lifetime * lenScl, e -> {
                    randLenVectors(e.id + fi - 1, e.fin(Interp.pow10Out), (int)(intensity), 15f * intensity, (x, y, in, out) -> {
                        float fout = e.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                        float rad = fout * ((2f + intensity) * 2.35f);

                        Fill.circle(e.x + x, e.y + y, rad);
                        Drawf.light(e.x + x, e.y + y, rad * 2.5f, b.color, 0.5f);
                    });
                });
            }
        });

        promethiunmRectorExplosion = new Effect(30, 500f, b -> {
            float intensity = 8f;
            float baseLifetime = 25f + intensity * 11f;
            b.lifetime = 50f + intensity * 65f;
            Color color = Color.valueOf("DEDEDE70");

            color(color);
            alpha(0.7f);
            for(int i = 0; i < 3; i++){
                rand.setSeed(b.id * 2 + i);
                float lenScl = rand.random(0.4f, 1f);
                int fi = i;
                b.scaled(b.lifetime * lenScl, e -> {
                    randLenVectors(e.id + fi - 1, e.fin(Interp.pow10Out), (int)(2.9f * intensity), 22f * intensity, (x, y, in, out) -> {
                        float fout = e.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                        float rad = fout * ((2f + intensity) * 2.35f);

                        Fill.circle(e.x + x, e.y + y, rad);
                        Drawf.light(e.x + x, e.y + y, rad * 2.5f, Pal.reactorPurple, 0.5f);
                    });
                });
            }

            b.scaled(200 + intensity * 2f, i -> {
                color(Pal.lighterOrange, color, i.fout(Interp.pow3In));
                stroke((2f * i.fout()));
                stroke((3.1f + intensity / 5f) * i.fout());
                float rad = 180 * i.fin(Interp.circleOut);
                circle(i.x, i.y, rad);
                Drawf.light(i.x, i.y, i.fin() * 14f * 2f * intensity, Color.white, 0.9f * i.fout());
                rand.setSeed(i.id);
                for(int a = 0; (float)a < Mathf.clamp(rad / 12.0F, 9.0F, 60.0F); ++a){
                    Tmp.v1.set(1.0F, 0.0F).setToRandomDirection(rand).scl(rad);
                    Drawn.tri(i.x + Tmp.v1.x, i.y + Tmp.v1.y, rand.random(rad / 16f, rad / 12f) * i.fout(),
                    rand.random(rad / 4.0F, rad / 1.5F) * (1.0F + i.fin()) / 2.0F, Tmp.v1.angle() - 180.0F);
                }
            });

            b.scaled(baseLifetime, e -> {
                color(Pal.lighterOrange, Pal.gray, e.fout(Interp.pow3In));
                z(Layer.effect + 0.001f);
                randLenVectors(e.id + 1, e.finpow() + 0.001f, (int)(8 * intensity), 28f * intensity, (x, y, in, out) -> {
                    lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + out * 4 * (4f + intensity));
                    Drawf.light(e.x + x, e.y + y, (out * 4 * (3f + intensity)) * 3.5f, getColor(), 0.8f);
                });
            });
        });

        spawn = new Effect(100f, e -> {
            TextureRegion pointerRegion = WHContent.pointerRegion;

            color(e.color);

            for(int j = 1; j <= 3; j++){
                for(int i = 0; i < 4; i++){
                    float length = e.rotation * 3f + tilesize;
                    float x = Angles.trnsx(i * 90, -length), y = Angles.trnsy(i * 90, -length);
                    e.scaled(30 * j, k -> {
                        float signSize = e.rotation / tilesize / 3f * scl * k.fout();
                        Draw.rect(pointerRegion, e.x + x * k.finpow(), e.y + y * k.finpow(), pointerRegion.width * signSize, pointerRegion.height * signSize, Angles.angle(x, y) - 90);
                        Drawf.light(e.x + x, e.y + y, e.fout() * signSize * pointerRegion.height, e.color, 0.7f);
                    });
                }
            }
        });

        jumpTrail = new Effect(120f, 5000, e -> {
            if(!(e.data instanceof UnitType)) return;

            UnitType type = e.data();

            color(type.engineColor == null ? e.color : type.engineColor);

            if(type.engineLayer > 0) z(type.engineLayer);
            else Draw.z(type.flyingLayer > 110 ? Layer.effect : type.flyingLayer + 1);
            /*z((type.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) - 0.001f);*/

            for(int index = 0; index < type.engines.size; index++){

                UnitEngine engine = type.engines.get(index);

                /*  if(Angles.angleDist(engine.rotation, -90) > 75) return;*/
                float ang = Mathf.slerp(engine.rotation, -90, 1);

                //noinspection SuspiciousNameCombination
                Tmp.v1.trns(e.rotation, engine.y, -engine.x);

                Drawn.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, engine.radius * 1.5f * e.fout(Interp.slowFast),
                1500 * engine.radius / (type.engineSize + 4), e.rotation + ang - 90);
                Fill.circle(e.x + Tmp.v1.x, e.y + Tmp.v1.y, engine.radius * 1.5f * e.fout(Interp.slowFast));

                e.scaled(30, i -> {
                    Drawn.tri(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 1.5f * i.fout(Interp.slowFast),
                    1500 * engine.radius / (type.engineSize + 4) * e.fout(), i.rotation + ang - 90);
                    Fill.circle(i.x + Tmp.v1.x, i.y + Tmp.v1.y, engine.radius * 1.5f * i.fout(Interp.slowFast));
                });

                randLenVectors(e.id + index, 22, 400 * engine.radius / (type.engineSize + 4),
                e.rotation + ang - 90, 0f, (x, y) -> lineAngle(e.x + x + Tmp.v1.x, e.y + y + Tmp.v1.y, Mathf.angle(x, y), e.fout() * 60));
            }

            color();
            alpha(e.fout(Interp.pow3In));
            mixcol(e.color, 1);
            Draw.z(type.flyingLayer > 110 ? Layer.effect : type.flyingLayer - 1);
            Draw.rect(type.fullIcon, e.x, e.y, type.fullIcon.width * e.fout(Interp.pow2Out) * scl * 1.2f, type.fullIcon.height * e.fout(Interp.pow2Out) * scl * 1.2f, e.rotation - 90f);
            reset();
        });

        jumpIn = new Effect(delaySpawnTime, e -> {
            if(!(e.data instanceof UnitType)) return;

            UnitType type = e.data();

            Tmp.v1.trns(e.rotation, -1500).add(e.x, e.y);
            Tmp.v2.trns(e.rotation, 1500 * e.fin()).add(Tmp.v1);

            alpha(0.5f + 0.5f * e.fin());
            Draw.z(type.flyingLayer > 110 ? Layer.effect : type.flyingLayer + 1);

            color(type.engineColor == null ? e.color : type.engineColor);

            for(int index = 0; index < type.engines.size; index++){
                UnitEngine engine = type.engines.get(index);

                /*   if(Angles.angleDist(engine.rotation, -90) > 75) return;*/
                float ang = Mathf.slerp(engine.rotation, -90, 1);

                //noinspection SuspiciousNameCombination
                Tmp.v3.trns(e.rotation, engine.y, -engine.x).add(Tmp.v2);

                Drawn.tri(Tmp.v3.x, Tmp.v3.y, engine.radius * 1.5f,
                1500 * e.fin() * engine.radius / (type.engineSize + 4), e.rotation + ang - 90);
                Fill.circle(Tmp.v3.x, Tmp.v3.y, engine.radius * 1.5f * e.fout(Interp.slowFast));
            }

            color();
            alpha(0.5f + 0.5f * e.fin());
            Draw.z(Layer.flyingUnit + 0.01f);
            Draw.rect(type.fullIcon, Tmp.v2.x, Tmp.v2.y, type.fullIcon.width * scl * 1.2f, type.fullIcon.height * scl * 1.2f, e.rotation - 90f);
        });

        airDropOut = new Effect(111f, e -> {
            if(!(e.data instanceof UnitType)) return;
            UnitType type = e.data();

            float fin = e.fin(Interp.pow4In) * Mathf.curve(e.fin(), 0, 0.13f);
            float rotation = e.rotation - 90f + e.fout(Interp.smooth) * (Mathf.randomSeedRange(e.id, 25f));
            float fout = e.fout(Interp.pow2In);
            float scale = fout * 1.1f + 1f;

            scale *= type.fullIcon.scl();
            float rw = type.fullIcon.width * scale, rh = type.fullIcon.height * scale;

            Drawf.shadow(e.x, e.y, type.hitSize, 1);

            z(Layer.effect + 0.001f);
            Draw.alpha(0.4f + 0.6f * e.fin());
            Fill.light(e.x, e.y, 10, (10f + type.hitSize / 2) * e.fout(), Tmp.c2.set(Pal.engine).a(fout), Tmp.c1.set(Pal.engine).a(0f));
            for(int i = 0; i < 4; i++){
                z(Layer.flyingUnit - 1);
                Tmp.v2.trns((i) * 90 + rotation + 45, type.hitSize * scale + 20f);
                float rad = type.hitSize * e.fout();
                float scl = Mathf.absin(Time.time, 2f, type.hitSize / 8f) * e.fout();
                color(type.engineColor == null ? e.color : type.engineColor);
                Fill.circle(e.x + Tmp.v2.x, e.y + Tmp.v2.y, rad * 0.4f + scl);
                color(type.engineColorInner);
                Fill.circle(e.x + Tmp.v2.x, e.y + Tmp.v2.y, rad * 0.2f + scl);
            }

            z(Layer.weather - 1);
            color();

            Draw.rect(type.fullIcon, e.x, e.y, rw, rh, rotation);


            Tmp.v1.trns(225f, fout * 300f);
            z(Layer.flyingUnit + 1);
            color(0, 0, 0, 0.5f * fin);
            Draw.rect(type.fullIcon, e.x + Tmp.v1.x, e.y + Tmp.v1.y, rw, rh, rotation);

            z(Layer.weather - 1);
            color(Color.gray, 0.8f * e.fout());

            reset();
        });

        AirDropLandDust = new Effect(70f, e -> {
            color(e.color, e.fout(0.1f));
            rand.setSeed(e.id);
            Tmp.v1.trns(e.rotation, e.finpow() * 100 * rand.random(0.2f, 1f));
            Fill.circle(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 8f * rand.random(0.6f, 1f) * e.fout(0.2f));
        }).layer(Layer.groundUnit + 1f);

        groundEffect = new Effect(150f, e -> {
            z(Layer.block);
            color(e.color, e.fout());
            rand.setSeed(e.id);
            float range = rand.random(0.2f, 1f);
            Fill.circle(e.x + 8 * range, e.y, 20 * range * e.fin(Interp.pow3Out));
        });

        BeamLaserGroundEffect = new Effect(90, e -> {
            z(Layer.effect);
            rand.setSeed(e.id);
            float range = e.data();
            float radius = range * rand.random(0.6f, 1f) * 0.1f;
            randLenVectors(e.id, (int)(3f * rand.random(1f)), -range * rand.random(0.3f), -60, radius, (x, y) -> {
                color(e.color, e.color.cpy().lerp(Color.red, 0.1f).lerp(Pal.meltdownHit, 0.1f), e.fout());
                float spiralAngle = e.time * 0.6f;
                float spiralRadius = e.finpow() * 25 * rand.random(0.3f, 1f);
                Tmp.v1.trns(e.rotation + spiralAngle * (rand.random(1) > 0.5f ? 1 : -1), spiralRadius);
                Tmp.v2.setToRandomDirection(rand).scl(5f * e.fout());

                Fill.circle(
                e.x + Tmp.v1.x + Tmp.v2.x,
                e.y + Tmp.v1.y + Tmp.v2.y,
                6f * rand.random(0.6f, 1f) * e.fout(Interp.pow2Out)
                );

                color(Color.black);
                Fill.circle(
                e.x + Tmp.v1.x + Tmp.v2.x,
                e.y + Tmp.v1.y + Tmp.v2.y,
                3.5f * rand.random(0.6f, 1f) * e.fout(Interp.pow2Out)
                );
                color(e.color, e.color.cpy().lerp(Color.red, 0.1f).lerp(Pal.meltdownHit, 0.1f), e.fout());
                stroke(e.fout(Interp.pow2Out) * 1.5f);
                float rot = e.rotation + rand.range(30f) + 180f;
                v.trns(rot, rand.random(e.fin() * 15f));
                lineAngle(e.x + v.x, e.y + v.y, rot, 8 * e.fout(Interp.pow2Out) * Mathf.randomSeed(e.id + 999, 1.5f));
                randLenVectors(e.id, e.fin(), (int)(5f * rand.random(1f)), -30f, (x1, y1, fin, fout) -> {
                    z(Layer.effect + 0.001f);
                    color(Color.gray);
                    alpha((0.5f - Math.abs(fin - 0.5f)) * 2f);
                    Fill.circle(e.x + x1, e.y + y1, 0.5f + fout * 12f);
                });
            });
        });
    }

}
