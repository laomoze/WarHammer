package wh.entities.world.Psy;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.core.Renderer;
import mindustry.gen.Building;
import wh.core.WHSettings;
import wh.graphics.WHPal;
import wh.util.WHUtils;

final class PsychicBeamEffects {
    static void drawFlowParticles(PsychicNode node, PsychicNode.PsychicNodeBuild build, int direction,
                                  Building from, Building to, int fromDirection, int toDirection) {
        if (!WHSettings.effectEnabled() || build.beamEffectAlpha[direction] <= PsychicNetworkNode.epsilon) return;
        if (!build.resolvePrimaryBeamEndpoints(from, to, fromDirection, toDirection)) return;

        float rate = Math.max(build.beamFlowRate[direction], node.flowParticleMinRate);
        float pressure = Math.max(build.beamPotentialDiff[direction], node.flowParticleMinPotential);
        float amountScl = Mathf.clamp(rate / Math.max(node.transferRate, node.flowParticleMinRate));
        int particles = Mathf.clamp(Mathf.ceil(node.maxFlowParticles * amountScl), 1, node.maxFlowParticles);
        float alpha = Renderer.laserOpacity * Mathf.clamp(build.beamEffectAlpha[direction]) *
                Mathf.clamp(pressure / Math.max(node.flowParticleMinPotential * 2f, 1f));
        float angle = Angles.angle(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
        float dst = Tmp.v1.dst(Tmp.v2);
        if (dst <= node.flowParticleLen) return;

        Rand rand = WHUtils.rand((long) build.id() * 31L + direction * 9973L);

        for (int i = 0; i < particles; i++) {
            float offset = rand.random(node.flowParticleLife);
            float widthScl = rand.random(0.45f, 1.25f);
            float lenScl = rand.random(0.65f, 1.35f);
            float alphaScl = rand.random(0.55f, 1f);
            float fin = ((Time.time + offset) % node.flowParticleLife) / node.flowParticleLife;
            float slope = Interp.pow2Out.apply(Mathf.slope(fin));
            float len = node.flowParticleLen * lenScl * (0.45f + 0.55f * slope);
            float offsetLen = build.hitSize() / 2.5f * rand.random(-1f, 1f);
            float cx = Mathf.lerp(Tmp.v1.x, Tmp.v2.x, fin) + Angles.trnsx(angle + 90f, offsetLen);
            float cy = Mathf.lerp(Tmp.v1.y, Tmp.v2.y, fin) + Angles.trnsy(angle + 90f, offsetLen);
            float hx = Angles.trnsx(angle, len / 2f);
            float hy = Angles.trnsy(angle, len / 2f);

            Draw.color(Tmp.c1.set(WHPal.PsyColor).lerp(node.laserColor1, 0.35f + 0.25f * widthScl).a(alpha * alphaScl));
            Lines.stroke(node.flowParticleWidth * widthScl * slope);
            Lines.line(cx - hx, cy - hy, cx + hx, cy + hy, false);
        }
        Lines.stroke(1f);
    }

    private PsychicBeamEffects() {
    }
}
