package wh.entities.world.Psy;

import arc.math.Mathf;
import arc.util.Strings;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public class PsychicGeneratorBlock extends PsychicBlock {
    public float craftTime = 90f;
    public float psychicPerCraft = 12f;
    public float powerPerSecond = 2f;
    public float range = 12f;
    public float warmupSpeed = 0.05f;
    public float fluctuationInterval = 180f;
    public float negativeTimeScale = 0.7f;
    public float positiveTimeScale = 1.25f;
    public float fluctuationDuration = 120f;

    public PsychicGeneratorBlock(String name) {
        super(name);
        acceptsPsychicLinks = false;
        outputsPsychicLinks = true;
        configurable = false;
        drawArrow = false;
        hasPower = true;
        outputsPower = true;
        connectedPower = true;
        canOverdrive = false;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.productionTime);
        stats.add(Stat.productionTime, craftTime / 60f, StatUnit.seconds);
        PsychicStatValues.add(stats, WHStats.psychicProduction, productionPerSecond(), StatUnit.perSecond);
        stats.add(Stat.basePowerGeneration, powerPerSecond * 60f, StatUnit.powerSecond);
        stats.add(Stat.range, range, StatUnit.blocks);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-production", (PsychicGeneratorBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-production", Strings.autoFixed(build.productionRate, 2)),
                () -> WHPal.PsyColor,
                () -> productionPerSecond() <= 0.0001f ? 0f : build.productionRate / productionPerSecond()
        ));

        addBar("psychic-craft", (PsychicGeneratorBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-craft", Strings.autoFixed(build.progressFraction() * 100f, 0)),
                () -> WHPal.PsyColor,
                build::progressFraction
        ));

        addBar("power-output", (PsychicGeneratorBuild build) -> new Bar(
                () -> bundleFormat("bar.poweroutput", Strings.autoFixed(build.getPowerProduction() * 60f, 2)),
                () -> Pal.powerBar,
                () -> powerPerSecond <= 0.0001f ? 0f : build.getPowerProduction() / powerPerSecond
        ));
    }

    protected float productionPerSecond() {
        return craftTime <= 0.0001f ? 0f : psychicPerCraft * 60f / craftTime;
    }

    public class PsychicGeneratorBuild extends PsychicBuild {
        public float progress;
        public float warmup;
        public float productionRate;
        public float producedThisFrame;
        public float fluctuationTimer;
        public boolean negativePulse;

        @Override
        public byte version() {
            return 5;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
            write.f(productionRate);
            write.f(fluctuationTimer);
            write.bool(negativePulse);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            if (revision >= 5) {
                progress = Math.max(read.f(), 0f);
                warmup = Mathf.clamp(read.f());
                productionRate = Math.max(read.f(), 0f);
                fluctuationTimer = Math.max(read.f(), 0f);
                negativePulse = read.bool();
            } else {
                progress = 0f;
                warmup = powerPerSecond > 0.0001f ? 1f : 0f;
                productionRate = 0f;
                fluctuationTimer = 0f;
                negativePulse = false;
            }

            producedThisFrame = 0f;
        }

        @Override
        public void updateTile() {
            super.updateTile();

            boolean canCraft = enabled && canConsume() && psychicPerCraft > 0.0001f
                    && psychicSpace() + 0.0001f >= psychicPerCraft && craftTime > 0.0001f;

            if (canCraft) {
                progress += edelta();
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed);
                fluctuationTimer += edelta();
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            if (progress >= craftTime && craftTime > 0.0001f && psychicPerCraft > 0.0001f
                    && enabled && canConsume() && psychicSpace() + 0.0001f >= psychicPerCraft) {
                consume();
                float produced = addPsychic(psychicPerCraft);
                producedThisFrame += produced;
                progress = 0;
            }

            if (canCraft && fluctuationTimer >= fluctuationInterval) {
                fluctuationTimer = 0f;
                negativePulse = !negativePulse;
                applyAreaFluctuation(negativePulse ? negativeTimeScale : positiveTimeScale, fluctuationDuration);
            }

            float actual = producedThisFrame * 60f / Math.max(delta(), 0.0001f);
            productionRate = Mathf.lerpDelta(productionRate, actual, 0.18f);
            producedThisFrame = 0f;
        }

        protected void applyAreaFluctuation(float targetScale, float duration) {
            float worldRange = range * tilesize;
            Vars.indexer.eachBlock(team, arc.util.Tmp.r1.setCentered(x, y, worldRange * 2f), other ->
                    other != this && other.isAdded() && other.block.update && other.block.canOverdrive, other -> {
                if (negativePulse) {
                    other.applySlowdown(Math.max(targetScale, 0.001f), duration);
                } else {
                    other.applyBoost(Math.max(targetScale, 0.001f), duration);
                }
            });
        }

        public float progressFraction() {
            return craftTime <= 0.0001f ? 0f : Mathf.clamp(progress / craftTime);
        }

        @Override
        public float getPowerProduction() {
            return enabled ? powerPerSecond * warmup : 0f;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float totalProgress() {
            return super.totalProgress();
        }

        @Override
        public float progress() {
            return progressFraction();
        }

        @Override
        public void draw() {
            super.draw();

            float stored = psychicFraction();
            float radius = block.size * tilesize * (0.32f + stored * 0.18f + warmup * 0.08f);

            if (stored > 0.001f || warmup > 0.001f) {
                Drawf.light(x, y, radius * 2.3f, WHPal.PsyColor, 0.18f + stored * 0.22f + warmup * 0.12f);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            float realRange = range * tilesize;
            Vars.indexer.eachBlock(this, realRange, other -> other.block.canOverdrive,
                    other -> Drawf.selected(other, Tmp.c1.set(WHPal.PsyColor).a(Mathf.absin(4f, 1f))));
            Drawf.dashCircle(x, y, realRange, WHPal.PsyColor);
            drawSelectText(
                    bundleFormat("bar.wh-psychic-storage",
                            Strings.autoFixed(psychicStored(), 2),
                            Strings.autoFixed(psychicCapacity(), 0)),
                    bundleFormat("bar.wh-psychic-production", Strings.autoFixed(productionRate, 2)),
                    bundleFormat("bar.wh-psychic-craft", Strings.autoFixed(progressFraction() * 100f, 0))
            );
        }
    }
}
