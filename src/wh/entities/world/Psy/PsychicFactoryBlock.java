package wh.entities.world.Psy;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Strings;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.graphics.Drawn;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public class PsychicFactoryBlock extends PsychicBlock {
    public float craftTime = 90f;
    public float psychicPerCraft = 12f;
    public float warmupSpeed = 0.05f;

    public PsychicFactoryBlock(String name) {
        super(name);
        acceptsPsychicLinks = false;
        outputsPsychicLinks = true;
        configurable = false;
        drawArrow = false;
        buildType = PsychicFactoryBuild::new;
    }

    @Override
    public void setStats() {
        super.setStats();
        PsychicStatValues.add(stats, WHStats.psychicProduction, productionPerSecond(), StatUnit.perSecond);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-production", (PsychicFactoryBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-production", Strings.autoFixed(build.productionRate, 2)),
                () -> psychicColor,
                () -> productionPerSecond() <= 0.0001f ? 0f : build.productionRate / productionPerSecond()
        ));

        addBar("psychic-craft", (PsychicFactoryBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-craft", Strings.autoFixed(build.progressFraction() * 100f, 0)),
                () -> psychicColor,
                build::progressFraction
        ));
    }

    protected float productionPerSecond() {
        return craftTime <= 0.0001f ? 0f : psychicPerCraft * 60f / craftTime;
    }

    public class PsychicFactoryBuild extends PsychicBuild {
        public float progress;
        public float warmup;
        public float productionRate;
        public float producedThisFrame;

        @Override
        public void updateTile() {
            super.updateTile();

            boolean canCraft = enabled && canConsume() && psychicSpace() + 0.0001f >= psychicPerCraft && craftTime > 0.0001f;
            if (canCraft) {
                progress += edelta();
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed);
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            while (progress >= craftTime && enabled && canConsume() && psychicSpace() + 0.0001f >= psychicPerCraft) {
                consume();
                float produced = addPsychic(psychicPerCraft);
                producedThisFrame += produced;
                progress -= craftTime;
            }

            float actual = producedThisFrame * 60f / Math.max(delta(), 0.0001f);
            productionRate = Mathf.lerpDelta(productionRate, actual, 0.18f);
            producedThisFrame = 0f;
        }

        public float progressFraction() {
            return craftTime <= 0.0001f ? 0f : Mathf.clamp(progress / craftTime);
        }

        @Override
        public void draw() {
            super.draw();

            float stored = psychicFraction();
            float pulse = Mathf.absin(6f, 0.7f + warmup * 1.1f);
            float radius = block.size * tilesize * (0.32f + stored * 0.18f + warmup * 0.08f);

            Draw.z(Layer.effect);
            Draw.color(psychicColor, Color.white, 0.12f + warmup * 0.14f);
            Draw.alpha(0.16f + stored * 0.2f + warmup * 0.14f);
            Lines.stroke(1f + warmup * 1.3f);
            Lines.square(x, y, radius + pulse * 0.18f, 45f);
            Fill.square(x, y, radius * 0.42f + pulse * 0.1f, 45f);
            Draw.reset();

            if (stored > 0.001f || warmup > 0.001f) {
                Drawf.light(x, y, radius * 2.3f, psychicColor, 0.18f + stored * 0.22f + warmup * 0.12f);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawn.overlayText(
                    bundleFormat("bar.wh-psychic-storage",
                            Strings.autoFixed(psychicStored(), 2),
                            Strings.autoFixed(psychicCapacity(), 0)) +
                            " | " + bundleFormat("bar.wh-psychic-production", Strings.autoFixed(productionRate, 2)) +
                            "\n" + bundleFormat("bar.wh-psychic-craft", Strings.autoFixed(progressFraction() * 100f, 0)),
                    x, y, block.size * tilesize * 1.15f, psychicColor, false
            );
        }
    }
}
