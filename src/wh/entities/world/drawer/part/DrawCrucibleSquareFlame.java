package wh.entities.world.drawer.part;

import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.world.draw.DrawCrucibleFlame;

public class DrawCrucibleSquareFlame extends DrawCrucibleFlame {
    @Override
    public void draw(Building build) {
        if (build.warmup() > 0f && flameColor.a > 0.001f) {
            Lines.stroke(circleStroke * build.warmup());

            float si = Mathf.absin(flameRadiusScl, flameRadiusMag);
            float a = alpha * build.warmup();
            Draw.blend(Blending.additive);

            Draw.color(midColor, a);
            Fill.square(build.x + x, build.y + y, flameRad + si, 45);

            Draw.color(flameColor, a);
            Lines.square(build.x + x, build.y + y, (flameRad + circleSpace + si) * build.warmup(), 45);

            float base = (Time.time / particleLife);
            rand.setSeed(build.id);
            for (int i = 0; i < particles; i++) {
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = rand.random(360f) + (Time.time / rotateScl) % 360f;
                float len = particleRad * particleInterp.apply(fout);
                Draw.alpha(a * (1f - Mathf.curve(fin, 1f - fadeMargin)));
                Fill.square(
                        build.x + Angles.trnsx(angle, len) + x,
                        build.y + Angles.trnsy(angle, len) + y,
                        particleSize * fin * build.warmup(),
                        45
                );
            }

            Draw.blend();
            Draw.reset();
        }
    }
}
