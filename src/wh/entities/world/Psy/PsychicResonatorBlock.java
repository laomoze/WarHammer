package wh.entities.world.Psy;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.Bits;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public class PsychicResonatorBlock extends PsychicBlock {
    public float range = 14f;
    public float baseProduction = 0.2f;
    public float factoryContribution = 0.55f;
    public float factoryWarmupWeight = 0.65f;
    public float factoryTimeScaleWeight = 0.2f;
    public float unitContribution = 0.3f;
    public float buffContribution = 0.16f;
    public float bossBonus = 1.75f;
    public float maxProduction = 12f;
    public float warmupSpeed = 0.05f;
    public float scanInterval = 20f;

    public PsychicResonatorBlock(String name) {
        super(name);
        acceptsPsychicLinks = false;
        outputsPsychicLinks = true;
        configurable = false;
        drawArrow = false;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, range, StatUnit.blocks);
        PsychicStatValues.add(stats, WHStats.psychicProduction, maxProduction, StatUnit.perSecond);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-production", (PsychicResonatorBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-production", Strings.autoFixed(build.productionRate, 2)),
                () -> psychicColor,
                () -> maxProduction <= 0.0001f ? 0f : build.productionRate / maxProduction
        ));

        addBar("psychic-resonance", (PsychicResonatorBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-resonance",
                        Strings.autoFixed(build.buildingScore, 2),
                        Strings.autoFixed(build.unitScore, 2)),
                () -> psychicColor,
                () -> Mathf.clamp((build.buildingScore + build.unitScore) / Math.max(maxProduction, 0.0001f))
        ));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        drawRange(x * tilesize + offset, y * tilesize + offset);
        drawPlaceText(bundleFormat("bar.wh-psychic-place-source",
                Strings.autoFixed(100f, 0),
                Strings.autoFixed(maxProduction, 2)), x, y, valid);
    }

    protected void drawRange(float worldX, float worldY) {
        Draw.z(Layer.overlayUI);
        Draw.color(Pal.accent);
        Draw.alpha(0.75f);
        Lines.stroke(1.2f);
        Lines.circle(worldX, worldY, range * tilesize);
        Draw.reset();
    }

    public class PsychicResonatorBuild extends PsychicBuild {
        public float productionRate;
        public float buildingScore;
        public float unitScore;
        public float warmup;
        public int lastTileChanges = -1;

        @Override
        public void updateTile() {
            super.updateTile();

            if (!enabled) {
                productionRate = Mathf.lerpDelta(productionRate, 0f, 0.16f);
                buildingScore = Mathf.lerpDelta(buildingScore, 0f, 0.16f);
                unitScore = Mathf.lerpDelta(unitScore, 0f, 0.16f);
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }

            if (lastTileChanges != Vars.world.tileChanges || timer(0, scanInterval)) {
                buildingScore = calculateBuildingScore();
                unitScore = calculateUnitScore();
                lastTileChanges = Vars.world.tileChanges;
            }

            float targetProduction = Mathf.clamp(baseProduction + buildingScore + unitScore, 0f, maxProduction);
            float produced = addPsychic(targetProduction / 60f * delta());
            float actualProduction = produced * 60f / Math.max(delta(), 0.0001f);

            productionRate = Mathf.lerpDelta(productionRate, actualProduction, 0.16f);
            warmup = Mathf.approachDelta(warmup, actualProduction > 0.001f ? 1f : 0f, warmupSpeed);
        }

        protected float calculateBuildingScore() {
            float rangeWorld = range * tilesize;
            final float[] total = {0f};

            Vars.indexer.eachBlock(team, x, y, rangeWorld, other -> other != this && other.isAdded(), other -> {
                float sizeFactor = Math.max(other.block.size, 1);
                float activity = Mathf.clamp(other.warmup() * factoryWarmupWeight + Math.max(other.timeScale() - 1f, 0f) * factoryTimeScaleWeight + 0.35f, 0.1f, 2f);
                float falloff = 1f - Mathf.dst(x, y, other.x, other.y) / rangeWorld;
                total[0] += sizeFactor * activity * Math.max(falloff, 0.15f) * factoryContribution;
            });

            return total[0];
        }

        protected float calculateUnitScore() {
            float rangeWorld = range * tilesize;
            final float[] total = {0f};

            Units.nearby(team, x, y, rangeWorld, unit -> {
                float volume = Math.max(unit.hitSize, 4f) / 8f;
                float buffed = 1f + countStatuses(unit.statusBits()) * buffContribution;
                if (unit.isBoss()) buffed *= bossBonus;
                float moving = 0.75f + Mathf.clamp(unit.vel.len() / Math.max(unit.speed(), 0.0001f), 0f, 1.5f) * 0.35f;
                float falloff = 1f - Mathf.dst(x, y, unit.x, unit.y) / rangeWorld;
                total[0] += volume * buffed * moving * Math.max(falloff, 0.2f) * unitContribution;
            });

            return total[0];
        }

        protected int countStatuses(Bits bits) {
            if (bits == null) return 0;
            int total = 0;
            for (int i = 0; i < Vars.content.statusEffects().size; i++) {
                if (bits.get(i)) total++;
            }
            return total;
        }

        @Override
        public void draw() {
            super.draw();
            float stored = psychicFraction();
            float radius = block.size * tilesize * (0.38f + stored * 0.2f + warmup * 0.12f);
            if (stored > 0.001f || warmup > 0.001f) {
                Drawf.light(x, y, radius * 2.7f, psychicColor, 0.2f + stored * 0.24f + warmup * 0.12f);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawRange(x, y);
            drawSelectText(
                    bundleFormat("bar.wh-psychic-storage",
                            Strings.autoFixed(psychicStored(), 2),
                            Strings.autoFixed(psychicCapacity(), 0)),
                    bundleFormat("bar.wh-psychic-production", Strings.autoFixed(productionRate, 2)),
                    bundleFormat("bar.wh-psychic-resonance",
                            Strings.autoFixed(buildingScore, 2),
                            Strings.autoFixed(unitScore, 2))
            );
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float progress() {
            return maxProduction <= 0.0001f ? 0f : Mathf.clamp(productionRate / maxProduction);
        }
    }
}
