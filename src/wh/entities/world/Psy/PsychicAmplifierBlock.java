package wh.entities.world.Psy;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Strings;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public class PsychicAmplifierBlock extends PsychicBlock {
    public float range = 12f;
    public float psychicUse = 0.9f;
    public float boost = 1.5f;
    public float overloadPerSecond = 0.08f;
    public float warmupSpeed = 0.06f;

    public PsychicAmplifierBlock(String name) {
        super(name);
        acceptsPsychicLinks = true;
        outputsPsychicLinks = false;
        drawArrow = false;
        configurable = false;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, range, StatUnit.blocks);
        PsychicStatValues.add(stats, WHStats.psychicPressureBoost, boost, StatUnit.none);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("psychic-use", (PsychicAmplifierBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-use", Strings.autoFixed(build.useRate, 2)),
                () -> WHPal.PsyColor,
                () -> psychicUse <= 0.0001f ? 0f : build.useRate / psychicUse
        ));
    }

    public class PsychicAmplifierBuild extends PsychicBuild {
        public float warmup;
        public float useRate;

        @Override
        public void updateTile() {
            super.updateTile();

            boolean active = updateConsumeRecipe(enabled && hasPsychic(psychicUse / 60f * delta()));
            if (active) {
                float used = drainPsychic(psychicUse / 60f * delta());
                useRate = Mathf.lerpDelta(useRate, used * 60f / Math.max(delta(), 0.0001f), 0.16f);
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed);
                addPsychicOverload(overloadPerSecond / 60f * delta());
                eachNearbyPsychicBuild(range, other -> {
                    if (other != this) other.addPsychicPressure((boost - 1f) * warmup * 0.1f);
                });
            } else {
                useRate = Mathf.lerpDelta(useRate, 0f, 0.16f);
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }
        }

        @Override
        public void draw() {
            super.draw();
            if (warmup <= 0.001f) return;
            Draw.z(Layer.effect);
            Draw.color(WHPal.PsyColor, Color.white, 0.15f + warmup * 0.15f);
            Draw.alpha(0.15f + warmup * 0.2f);
            Lines.stroke(1.2f + warmup);
            Lines.circle(x, y, range * tilesize * (0.55f + 0.08f * Mathf.absin(6f, 1f)));
            Draw.reset();
            Drawf.light(x, y, range * tilesize * 0.9f, WHPal.PsyColor, 0.15f + warmup * 0.18f);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.dashCircle(x, y, range * tilesize, Pal.accent);
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float progress() {
            return psychicUse <= 0.0001f ? 0f : Mathf.clamp(useRate / psychicUse);
        }
    }
}

