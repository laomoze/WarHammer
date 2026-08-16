package wh.entities.world.entities;

import arc.audio.Sound;
import arc.func.Cons;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.gen.Unit;

public final class UnitCutter {
    private static final UnitCutBatch batch = new UnitCutBatch();
    private static final Rand rand = new Rand();

    private UnitCutter() {
    }

    public static void cut(Unit unit, Effect explosionEffect, float x1, float y1, float x2, float y2) {
        cut(unit, x1, y1, x2, y2, explosionEffect, null, null);
    }

    public static void cut(Unit unit, float x1, float y1, float x2, float y2, Effect explosionEffect, Sound sound, Cons<UnitSeveration> extraSetup) {
        Seq<UnitSeveration> pieces = capture(unit, explosionEffect, sound, extraSetup);
        for (UnitSeveration piece : pieces) {
            piece.cutWorld(x1, y1, x2, y2, null);
        }
    }

    public static void shatter(Unit unit, int splitCount) {
        shatter(unit, splitCount, null, null, null);
    }

    public static void shatter(Unit unit, int splitCount, Effect explosionEffect, Sound sound, Cons<UnitSeveration> extraSetup) {
        Seq<UnitSeveration> pieces = capture(unit, explosionEffect, sound, extraSetup);
        if (pieces.isEmpty()) return;

        rand.setSeed((((long) unit.id) << 32) ^ Float.floatToIntBits(unit.x * 17.13f + unit.y * 31.73f + Time.time));
        splitCount = Math.max(splitCount, 1);

        for (int i = 0; i < splitCount; i++) {
            UnitSeveration piece = pickPiece(pieces);
            if (piece == null) break;

            float angle = rand.random(360f);
            float offset = rand.range(piece.bounds * 0.22f);
            float length = Math.max(piece.bounds * 1.6f, 24f);

            Vec2 along = Tmp.v1.trns(angle, length);
            Vec2 normal = Tmp.v2.trns(angle + 90f, offset);
            float cx = piece.x + normal.x;
            float cy = piece.y + normal.y;

            Seq<UnitSeveration> created = piece.cutWorldResult(
                    cx - along.x, cy - along.y,
                    cx + along.x, cy + along.y,
                    shattered -> {
                        Tmp.v3.trns(angle + rand.range(70f), rand.random(0.3f, 2.6f));
                        shattered.vx += Tmp.v3.x;
                        shattered.vy += Tmp.v3.y;
                        shattered.vr += rand.range(8f);
                    });

            if (!created.isEmpty()) {
                pieces.remove(piece);
                pieces.addAll(created);
            }
        }
    }

    private static Seq<UnitSeveration> capture(Unit unit, Effect explosionEffect, Sound sound, Cons<UnitSeveration> extraSetup) {
        // 这里保存的是单位每个绘制贴图对应的初始碎片，不是单位本身。
        Seq<UnitSeveration> captured = new Seq<>();
        if (unit == null) return captured;

        // 为本次捕获到的所有碎片设置统一的消失特效和声音。
        batch.explosionEffect = explosionEffect;
        batch.sound = sound;

        // UnitCutBatch 每截获一次贴图绘制，都会创建一个 UnitSeveration，
        // 并在创建后调用这里，为它补上单位当前速度和调用方的额外配置。
        batch.cutHandler = severation -> {
            severation.vx += unit.vel.x;
            severation.vy += unit.vel.y;
            if (extraSetup != null) extraSetup.get(severation);
        };

        // 临时用 UnitCutBatch 替换 Core.batch 后执行 unit.draw()。
        // 此时不会真正绘制单位，而是把每次贴图绘制转换成 UnitSeveration 并收集起来。
        captured.addAll(batch.switchBatch(unit::draw));
        return captured;
    }

    private static UnitSeveration pickPiece(Seq<UnitSeveration> pieces) {
        // 用面积作为权重：面积更大的碎片更容易被下一刀选中，
        // 同时忽略已移除或过小的碎片，避免产生大量视觉上无意义的细屑。
        float totalArea = 0f;
        for (UnitSeveration piece : pieces) {
            if (piece != null && piece.added && piece.area >= 24f) {
                totalArea += piece.area;
            }
        }
        if (totalArea <= 0f) return null;

        // 在 [0, totalArea] 中随机取一点；按面积顺序累加时，
        // 哪块碎片的面积区间包含该点，就选中哪一块。
        float target = rand.random(totalArea);
        float accum = 0f;
        UnitSeveration fallback = null;
        for (UnitSeveration piece : pieces) {
            if (piece == null || !piece.added || piece.area < 24f) continue;

            // 正常情况下 target 一定会落入某个区间；fallback 用于浮点边界情况。
            fallback = piece;
            accum += piece.area;
            if (accum >= target) {
                return piece;
            }
        }
        return fallback;
    }
}
