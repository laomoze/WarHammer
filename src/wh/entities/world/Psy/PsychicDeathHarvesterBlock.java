package wh.entities.world.Psy;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Strings;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.graphics.Drawn;
import wh.ui.PsychicBar;

import static mindustry.Vars.tilesize;

public class PsychicDeathHarvesterBlock extends PsychicBlock {
    public float deathRange = 12f;
    public float baseDeathGain = 1.6f;
    public float healthDeathScale = 0.055f;
    public float maxDeathGain = 14f;
    public float bossMultiplier = 1.35f;
    public float warmupSpeed = 0.05f;

    public PsychicDeathHarvesterBlock(String name) {
        super(name);
        acceptsPsychicLinks = false;
        outputsPsychicLinks = true;
        configurable = false;
        drawArrow = false;
        buildType = PsychicDeathHarvesterBuild::new;
    }

    public static void handleUnitDeath(Unit unit) {
        if (unit == null) return;

        Groups.build.each(build -> {
            if (build instanceof PsychicDeathHarvesterBuild harvester) {
                harvester.harvestUnitDeath(unit);
            }
        });
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, deathRange, StatUnit.blocks);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-death-gain", (PsychicDeathHarvesterBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-harvest", Strings.autoFixed(build.gainRate, 2)),
                () -> psychicColor,
                () -> Mathf.clamp(build.gainRate / Math.max(baseDeathGain * 3f, 0.0001f))
        ));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        float worldX = x * tilesize + offset;
        float worldY = y * tilesize + offset;
        drawDeathRange(worldX, worldY);
        drawPlaceText(bundleFormat("bar.wh-psychic-death-range", Strings.autoFixed(deathRange, 1)), x, y, valid);
    }

    protected void drawDeathRange(float worldX, float worldY) {
        Draw.z(Layer.overlayUI);
        Draw.color(Pal.heal);
        Draw.alpha(0.75f);
        Lines.stroke(1.2f);
        Lines.circle(worldX, worldY, deathRange * tilesize);
        Draw.reset();
    }

    public class PsychicDeathHarvesterBuild extends PsychicBuild {
        public float gainRate;
        public float warmup;
        public float gainedThisFrame;

        @Override
        public void updateTile() {
            super.updateTile();

            float actualGain = gainedThisFrame * 60f / Math.max(delta(), 0.0001f);
            gainRate = Mathf.lerpDelta(gainRate, actualGain, 0.18f);
            warmup = Mathf.approachDelta(warmup, actualGain > 0.001f ? 1f : 0f, warmupSpeed);
            gainedThisFrame = 0f;
        }

        public void harvestUnitDeath(Unit unit) {
            if (unit == null || !enabled || !isAdded()) return;
            if (unit.team == team) return;

            float rangeWorld = deathRange * tilesize;
            float dst2 = Mathf.dst2(x, y, unit.x, unit.y);
            if (dst2 > rangeWorld * rangeWorld) return;

            float falloff = 1f - dst2 / (rangeWorld * rangeWorld);
            float gain = deathGain(unit) * falloff * falloff;
            float accepted = addPsychic(gain);
            if (accepted > 0.0001f) {
                gainedThisFrame += accepted;
            }
        }

        protected float deathGain(Unit unit) {
            float gain = baseDeathGain + Mathf.sqrt(Math.max(unit.maxHealth(), 1f)) * healthDeathScale;
            if (unit.isBoss()) {
                gain *= bossMultiplier;
            }
            return Math.min(gain, maxDeathGain);
        }

        @Override
        public void draw() {
            super.draw();

            float stored = psychicFraction();
            float pulse = Mathf.absin(7f, 0.9f + warmup * 1.4f);
            float radius = block.size * tilesize * (0.42f + stored * 0.18f + warmup * 0.12f);

            Draw.z(Layer.effect);
            Draw.color(psychicColor, Color.white, 0.08f + warmup * 0.12f);
            Draw.alpha(0.14f + stored * 0.2f + warmup * 0.18f);
            Lines.stroke(1f + warmup * 1.3f);
            Lines.circle(x, y, radius + pulse * 0.3f);
            Lines.square(x, y, radius * 0.82f + pulse * 0.16f, 45f);
            Draw.reset();

            if (stored > 0.001f || warmup > 0.001f) {
                Drawf.light(x, y, radius * 2.4f, psychicColor, 0.2f + stored * 0.22f + warmup * 0.1f);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawDeathRange(x, y);
            Drawn.overlayText(
                    bundleFormat("bar.wh-psychic-storage",
                            Strings.autoFixed(psychicStored(), 2),
                            Strings.autoFixed(psychicCapacity(), 0)) +
                            " | " + bundleFormat("bar.wh-psychic-harvest", Strings.autoFixed(gainRate, 2)),
                    x, y, block.size * tilesize * 1.1f, psychicColor, false
            );
        }
    }
}
