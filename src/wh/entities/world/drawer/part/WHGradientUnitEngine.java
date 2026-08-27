package wh.entities.world.drawer.part;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import wh.content.WHFx;
import wh.core.WHSettings;
import wh.graphics.Drawn;
import wh.util.WHUtils;

public class WHGradientUnitEngine extends WHUnitEngine {
    public int layers = 8;
    public int gradientSteps = 13;

    public float lengthFrom = 1.25f;
    public float lengthTo = 0.3f;
    public float radiusFrom = 1f;
    public float radiusTo = 0.2f;
    public float panFrom = 0.32f;
    public float panTo = 0.05f;
    public float layerAlphaFrom = 0.5f;
    public float layerAlphaTo = 1;
    public float flameAlphaFrom = 1f;
    public float flameAlphaTo = 0f;
    public float baseWidth = 0.7f;
    public float widthCurve = 0.78f;
    public Color gradientColorFrom = Color.valueOf("e9c2cd");
    public Color gradientColorTo = Color.valueOf("493b90");

    public float particleLen = 12;
    public float particleStoke = 3;
    public float particleLayerOffset = 0.001f;

    public Interp layerInterpolation = Interp.pow2Out;
    public Interp gradientInterpolation = Interp.smooth;

    public WHGradientUnitEngine(float x, float y, float length, float lengthMax, float width) {
        super(x, y, length, lengthMax, width);
    }

    @Override
    public void draw(Unit unit) {
        float sin = Mathf.absin(Time.time, 2f, 0.1f);
        float rotation = unit.rotation - 90f;
        UnitType type = unit.type;
        float scale = type.useEngineElevation ? unit.elevation : 1f;

        if (scale <= 0.0001f) return;
        /*  Draw.z(type.engineLayer > 0? type.engineLayer : Layer.effect);*/
        Color color = unit.type.engineColor == null ? unit.team.color : unit.type.engineColor;

        Tmp.v1.trns(rotation, x, y);
        float ex = Tmp.v1.x + unit.x;
        float ey = Tmp.v1.y + unit.y;

        float realLength = baseLength + Math.abs((maxLength - baseLength) *
                Interp.smooth.apply(Mathf.clamp(unit.vel.len() / unit.type.speed, 0f, 1f)));

        Draw.blend(Blending.additive);
        drawGradientFlame(ex, ey, unit.rotation - 180f,
                realLength * (1f - sin) * scale, radius * (1f + sin), color);
        Draw.blend();

        if (!unit.moving()) return;

        if (line && WHSettings.effectEnabled()) {

            float particleLife = 74f;
            Rand rand = WHUtils.rand((long) (unit.id + 99999 + x + y));

            float progress = Mathf.clamp((realLength - baseLength) / (maxLength - baseLength), 0f, 1f);
            int particlesMult = (int) (1 + particlesMultiple * progress);

            float base = Time.time / particleLife;
            for (int i = 0; i < startParticles * particlesMult; i++) {
                float fin = (rand.random(1f) + base) % 1f;
                float fout = 1f - fin;
                float fslope = WHFx.fslope(fin);
                float len = rand.random(particleLen * 0.7f, particleLen * 1.3f) * Mathf.curve(fin, 0.2f, 0.9f);
                float centerDeg = rand.random(Mathf.pi);
                Tmp.v2.trns(unit.rotation - 180,
                        Interp.pow3In.apply(fin) * rand.random(0f, realLength * 1.3f) + rand.range(11) - 8,
                        (((rand.random(0f, radius * 3f) * (fout + 1f) / 2f + 2f) /
                                (3f * fin / 7f + 1.3f) - 1f) + rand.random(-radius * 0.9f, radius * 0.9f)) * Mathf.cos(centerDeg));
                float angle = Mathf.slerp(Tmp.v1.angle(), unit.rotation - 180, Interp.pow2Out.apply(fin));
                Tmp.v2.add(ex, ey);
                Draw.blend(Blending.additive);
                if (gradientColorFrom != null && gradientColorTo != null) {
                    Draw.color(gradientColorFrom, gradientColorTo.cpy().a(0.4f).lerp(Color.white, 0.35f), gradientInterpolation.apply(fin * 0.7f));
                } else {
                    Draw.color(color.cpy(), Color.white, fin * 0.7f);
                }

                float w = Mathf.curve(fslope, 0f, 0.42f) * particleStoke * Mathf.curve(fin, 0f, 0.6f);
                Drawn.tri(Tmp.v2.x, Tmp.v2.y, w, len / 2, angle);
                Drawn.tri(Tmp.v2.x, Tmp.v2.y, w, len / 2, angle - 180);

                Draw.blend();
            }

            Draw.reset();
        }
    }

    public void drawGradientFlame(float x, float y, float rotation, float length, float radius, Color color) {
        if (layers <= 0 || gradientSteps <= 0 || length <= 0f || radius <= 0f) return;

        float directionX = Mathf.cosDeg(rotation);
        float directionY = Mathf.sinDeg(rotation);
        float sideX = -directionY;
        float sideY = directionX;


        for (int layer = 0; layer < layers; layer++) {
            float progress = layers == 1 ? 0f : layer / (float) (layers - 1);
            float interpolation = layerInterpolation.apply(progress);
            float layerLength = length * Mathf.lerp(lengthFrom, lengthTo, interpolation);
            float layerRadius = radius * Mathf.lerp(radiusFrom, radiusTo, interpolation);
            float layerPan = Mathf.lerp(panFrom, panTo, interpolation);
            float layerAlpha = Mathf.lerp(layerAlphaFrom, layerAlphaTo, interpolation);

            drawLayer(x, y, directionX, directionY, sideX, sideY, layerLength, layerRadius,
                    layerPan, layerAlpha, color);
        }
    }

    private void drawLayer(float x, float y, float directionX, float directionY, float sideX, float sideY,
                           float length, float radius, float pan, float layerAlpha, Color color) {
        for (int step = 0; step < gradientSteps; step++) {
            float from = step / (float) gradientSteps;
            float to = (step + 1f) / gradientSteps;
            drawStrip(x, y, directionX, directionY, sideX, sideY, length, radius, pan,
                    layerAlpha, color, from, to);
        }
    }

    private void drawStrip(float x, float y, float directionX, float directionY, float sideX, float sideY,
                           float length, float radius, float pan, float layerAlpha, Color color,
                           float from, float to) {
        float fromDistance = length * from;
        float toDistance = length * to;
        float fromRadius = radius * widthAt(from, pan);
        float toRadius = radius * widthAt(to, pan);
        float fromColor = colorBits(gradientColorAt(color, from), layerAlpha * gradientAlphaAt(from));
        float toColor = colorBits(gradientColorAt(color, to), layerAlpha * gradientAlphaAt(to));

        Fill.quad(
                x + directionX * fromDistance + sideX * fromRadius,
                y + directionY * fromDistance + sideY * fromRadius,
                fromColor,
                x + directionX * toDistance + sideX * toRadius,
                y + directionY * toDistance + sideY * toRadius,
                toColor,
                x + directionX * toDistance - sideX * toRadius,
                y + directionY * toDistance - sideY * toRadius,
                toColor,
                x + directionX * fromDistance - sideX * fromRadius,
                y + directionY * fromDistance - sideY * fromRadius,
                fromColor
        );
    }

    private float widthAt(float progress, float pan) {
        float exponent = Mathf.lerp(widthCurve * 0.8f, widthCurve * 1.2f, Mathf.clamp(pan));
        return Mathf.lerp(baseWidth, 0f, Mathf.pow(progress, exponent));
    }

    private float gradientAlphaAt(float progress) {
        return Mathf.lerp(flameAlphaFrom, flameAlphaTo, gradientInterpolation.apply(progress));
    }

    private Color gradientColorAt(Color engineColor, float progress) {
        Color from = gradientColorFrom == null ? engineColor : gradientColorFrom;
        Color to = gradientColorTo == null ? engineColor : gradientColorTo;
        return Tmp.c2.set(from).lerp(to, gradientInterpolation.apply(progress));
    }

    private float colorBits(Color color, float alpha) {
        return Tmp.c1.set(color).a(color.a * Mathf.clamp(alpha)).toFloatBits();
    }
}
