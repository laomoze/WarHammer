package wh.entities.world.Psy;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Strings;
import arc.util.Time;
import mindustry.entities.Units;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.content.WHStatusEffects;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public class PsychicBeaconBlock extends PsychicBlock {
    public float range = 10f;
    public float psychicUse = 0.7f;
    public float boost = 1.18f;
    public float statusDuration = 12f;
    public float warmupSpeed = 0.05f;
    public StatusEffect statusEffects = WHStatusEffects.assault;

    public PsychicBeaconBlock(String name) {
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
        PsychicStatValues.add(stats, WHStats.psychicStability, boost, StatUnit.none);
        PsychicStatValues.add(stats, WHStats.psychicConsumption, psychicUse, StatUnit.perSecond);
        stats.add(WHStats.psychicCoverageRange, range, StatUnit.blocks);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("psychic-use", (PsychicBeaconBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-use", Strings.autoFixed(build.useRate, 2)),
                () -> WHPal.PsyColor,
                () -> psychicUse <= 0.0001f ? 0f : build.useRate / psychicUse
        ));
    }

    public class PsychicBeaconBuild extends PsychicBuild {
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
                Units.nearby(team, x, y, range * tilesize, unit -> unit.apply(statusEffects, statusDuration));
                eachNearbyPsychicBuild(range, other -> {
                    if (other != this) {
                        other.addPsychicStability((boost - 1f) * 0.08f);
                        other.addPsychicPressure((boost - 1f) * 0.04f);
                    }
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
            Draw.color(WHPal.PsyColor, Color.white, 0.1f + warmup * 0.15f);
            Draw.alpha(0.14f + warmup * 0.2f);
            Lines.stroke(1f + warmup);
            Lines.square(x, y, range * tilesize * 0.45f, Time.time * 0.8f);
            Draw.reset();
            Drawf.light(x, y, range * tilesize * 0.7f, WHPal.PsyColor, 0.14f + warmup * 0.16f);
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

