package wh.entities.world.drawer.factory;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.draw.DrawBlock;

public class DrawOrb extends DrawBlock {
    public float x, y;
    public float layer = Layer.effect;

    public Color color = Color.white;
    public Color particleColor = Color.white;

    public float orbRadius = 6f;
    public float orbMidScl = 0.5f;
    public float orbSinScl = 8f;
    public float orbSinMag = 1f;
    public float stroke = 2f;

    public int particles = 12;
    public float particleLife = 90f;
    public float particleSize = 2f;
    public float particleLen = 7f;
    public float rotateScl = 3f;
    public Interp particleInterp = f -> Interp.circleOut.apply(Interp.slope.apply(f));

    public long seedOffset = 0L;
    public float activeWarmup = 0.001f;

    protected final Rand rand = new Rand();

    @Override
    public void draw(Building build) {
        boolean active = build.warmup() > activeWarmup;
        if (!active) return;

        Draw.z(layer);

        float rad = orbRadius + Mathf.absin(orbSinScl, orbSinMag);
        Tmp.v1.set(x, y).rotate(build.rotation * 90f - 90f).add(build.x, build.y);
        float rx = Tmp.v1.x, ry = Tmp.v1.y;

        float base = Time.time / particleLife;
        rand.setSeed(build.id + seedOffset + hashCode());
        Draw.color(particleColor);
        for (int i = 0; i < particles; i++) {
            float fin = (rand.random(1f) + base) % 1f;
            float fout = 1f - fin;
            float angle = rand.random(360f) + (Time.time / rotateScl + build.rotation * 90f) % 360f;
            float len = particleLen * particleInterp.apply(fout);
            Fill.circle(
                    rx + Angles.trnsx(angle, len),
                    ry + Angles.trnsy(angle, len),
                    particleSize * Mathf.slope(fin) * build.warmup()
            );
        }

        Lines.stroke(stroke);
        Draw.color(color);
        Lines.circle(rx, ry, rad);
        Fill.circle(rx, ry, rad * orbMidScl);
        Draw.reset();
    }
}
