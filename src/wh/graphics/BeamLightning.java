package wh.graphics;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import wh.content.WHFx;
import wh.core.WHSettings;
import wh.util.struct.Vec2Seq;

public final class BeamLightning {
    private static final Effect effect = new Effect(Float.MAX_VALUE, 1200f, e -> {
        if (!(e.data instanceof LightningData data)) return;
        e.lifetime = data.lifetime;
        if (!WHSettings.effectEnabled()) return;

        float progress = progress(e.time, data.growTime);
        float fadeIn = Mathf.curve(e.fin(), 0f, 0.2f);
        float thin = data.fadeTime <= 0.001f ? 1f :
                1f - Interp.smooth.apply(Mathf.clamp((e.time - data.growTime) / data.fadeTime));
        if (progress <= 0.001f || fadeIn <= 0.001f || thin <= 0.001f) return;

        Draw.z(Layer.bullet);
        drawFlash(data.points, progress, fadeIn, data.width * 2.7f * thin,
                data.color, data.fadePoints, Tmp.v3);
        drawFlash(data.points, progress, fadeIn, data.width * thin,
                data.color, data.fadePoints, Tmp.v3);
        drawFlash(data.points, progress, fadeIn, data.width * 0.42f * thin,
                Color.white, data.fadePoints, Tmp.v3);
        Vec2 start = data.points.firstTmp();
        Drawf.light(start.x, start.y, Tmp.v3.x, Tmp.v3.y,
                data.width * 1.25f * thin, data.color, 0.25f);
        Draw.reset();
    });

    private BeamLightning() {
    }

    public static void create(float startX, float startY, float endX, float endY, long seed,
                              Color color, float width, int segments, float randomDeviation,
                              float growTime, float fadeTime, float lifetime, float fadePoints) {
        createAndDraw(startX, startY, endX, endY, seed, color, width, segments, randomDeviation,
                growTime, fadeTime, lifetime, fadePoints);
    }

    public static Vec2Seq createAndDraw(float startX, float startY, float endX, float endY, long seed,
                                        Color color, float width, int segments, float randomDeviation,
                                        float growTime, float fadeTime, float lifetime, float fadePoints) {
        Vec2Seq points = createPath(startX, startY, endX, endY, seed, segments, randomDeviation);
        draw(points, color, width, growTime, fadeTime, lifetime, fadePoints);
        return points;
    }

    public static Vec2Seq createPath(float startX, float startY, float endX, float endY, long seed,
                                     int segments, float randomDeviation) {
        int segmentCount = Math.max(1, segments);
        Vec2Seq points = new Vec2Seq(segmentCount + 1);
        float angle = startX == endX && startY == endY ? 0f :
                Angles.angle(startX, startY, endX, endY) - 90f;
        Rand random = new Rand(seed);

        for (int i = 0; i <= segmentCount; i++) {
            float progress = i / (float) segmentCount;
            float offset = i == 0 || i == segmentCount ? 0f : random.range(randomDeviation);
            points.add(
                    Mathf.lerp(startX, endX, progress) + Angles.trnsx(angle, offset),
                    Mathf.lerp(startY, endY, progress) + Angles.trnsy(angle, offset)
            );
        }
        return points;
    }

    public static void draw(Vec2Seq points, Color color, float width,
                            float growTime, float fadeTime, float lifetime, float fadePoints) {
        if (points == null || points.size() < 2) return;

        float safeGrowTime = Math.max(0.01f, growTime);
        float safeLifetime = Math.max(0.01f, lifetime);
        float safeFadePoints = Math.max(0.001f, fadePoints);
        Vec2 start = points.firstTmp();
        Vec2 end = points.peekTmp();
        effect.clip = Math.max(1f, start.dst(end) * 2f);
        effect.at(start.x, start.y, 0f,
                new LightningData(points, color, width, safeGrowTime, fadeTime,
                        safeLifetime, safeFadePoints));
        createPathSparks(points, color, width);
    }

    public static float progress(float time, float growTime) {
        return growTime <= 0.001f ? 1f :
                Interp.pow2Out.apply(Mathf.clamp(time / growTime));
    }

    private static void createPathSparks(Vec2Seq points, Color color, float width) {
        if (!WHSettings.effectEnabled()) return;

        Rand random = new Rand();
        for (int i = 0; i < points.size() - 1; i++) {
            if (random.chance(0.1f)) {
                Vec2 point = points.setVec2(i, Tmp.v1);
                WHFx.lightningSpark.at(point.x, point.y,
                        random.random(2f + width, 4f + width), color);
            }
        }
    }

    private static void drawFlash(Vec2Seq points, float progress, float alpha, float stroke,
                                  Color color, float fadePoints, Vec2 end) {
        if (alpha <= 0.001f || stroke <= 0.001f) return;

        int segmentCount = points.size() - 1;
        float shown = Mathf.clamp(progress) * segmentCount;
        int complete = Math.min((int) shown, segmentCount);
        float partial = shown - complete;
        float fadeLength = Math.max(0.001f, fadePoints);

        for (int i = 0; i < complete; i++) {
            float tail = 1f - Mathf.clamp((shown - (i + 1f)) / fadeLength);
            float currentStroke = stroke * tail;
            if (currentStroke <= 0.001f) continue;

            Vec2 from = points.setVec2(i, Tmp.v1);
            Vec2 to = points.setVec2(i + 1, Tmp.v2);
            Draw.color(color);
            Lines.stroke(currentStroke);
            Lines.line(from.x, from.y, to.x, to.y, false);
            Fill.circle(from.x, from.y, currentStroke / 2f);
        }

        int current = complete;
        if (complete < segmentCount && partial > 0.001f) {
            Vec2 from = points.setVec2(current, Tmp.v1);
            Vec2 to = points.setVec2(current + 1, Tmp.v2);
            float endX = Mathf.lerp(from.x, to.x, partial);
            float endY = Mathf.lerp(from.y, to.y, partial);
            float tail = 1f - Mathf.clamp((shown - (complete + 1f)) / fadeLength);
            float currentStroke = stroke * tail;

            if (currentStroke > 0.001f) {
                Draw.color(color);
                Lines.stroke(currentStroke);
                Lines.line(from.x, from.y, endX, endY, false);
                Fill.circle(endX, endY, currentStroke * 0.5f);
            }
            end.set(endX, endY);
        } else {
            points.setVec2(current, end);
        }
    }

    private static class LightningData {
        final Vec2Seq points;
        final Color color;
        final float width;
        final float growTime;
        final float fadeTime;
        final float lifetime;
        final float fadePoints;

        LightningData(Vec2Seq points, Color color, float width, float growTime,
                      float fadeTime, float lifetime, float fadePoints) {
            this.points = points;
            this.color = color;
            this.width = width;
            this.growTime = growTime;
            this.fadeTime = fadeTime;
            this.lifetime = lifetime;
            this.fadePoints = fadePoints;
        }
    }
}