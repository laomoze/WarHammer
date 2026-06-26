package wh.entities.world.Psy;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.WindowedMean;
import arc.util.Strings;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHFx;
import wh.content.WHStats;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.player;
import static mindustry.Vars.tilesize;

public class PsychicRequiemBlock extends PsychicBlock {
    public float deathRange = 12f;
    public float baseDeathGain = 2;
    public float healthDeathScale = 0.1f;
    public float maxDeathGain = 20;
    public float bossMultiplier = 1.5f;
    public float warmupSpeed = 0.05f;
    public int gainRateWindow = 4;
    public float gainRateSampleInterval = 10f;

    public PsychicRequiemBlock(String name) {
        super(name);
        acceptsPsychicLinks = false;
        outputsPsychicLinks = true;
        configurable = false;
        drawArrow = false;
    }

    public static void handleUnitDeath(Unit unit) {
        if (unit == null) return;

        Groups.build.each(build -> {
            if (build instanceof PsychicRequiemBuild harvester) {
                harvester.harvestUnitDeath(unit);
            }
        });
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, deathRange, StatUnit.blocks);
        PsychicStatValues.add(stats, WHStats.psychicHarvest, maxDeathGain, StatUnit.perSecond);
        stats.add(WHStats.psychicHarvestRule, bundleFormat("stat.wh-psychic-harvest-rule-desc"));
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-death-gain", (PsychicRequiemBuild build) -> new PsychicBar(
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
        Team placeTeam = player == null ? Team.derelict : player.team();
        boolean overlap = overlapsAt(worldX, worldY, placeTeam, null);
        drawRange(worldX, worldY, !overlap);
        drawPlaceText(bundleFormat("bar.wh-psychic-death-range", Strings.autoFixed(deathRange, 1)), x, y, valid && !overlap);
        if (overlap) {
            drawPlaceText(bundleFormat("bar.wh-psychic-requiem-overlap"), x, y + 1, false);
        }
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (!super.canPlaceOn(tile, team, rotation)) return false;
        return !overlapsAt(tile.worldx() + offset, tile.worldy() + offset, team, null);
    }

    protected void drawRange(float worldX, float worldY, boolean valid) {
        float rangeWorld = deathRange * tilesize;
        Draw.alpha(0.75f);
        Drawf.dashRect(valid ? Pal.heal : Pal.remove, worldX - rangeWorld, worldY - rangeWorld, rangeWorld * 2f, rangeWorld * 2f);
        Draw.reset();
    }

    protected boolean overlapsAt(float worldX, float worldY, Team team, PsychicRequiemBuild self) {
        float rangeWorld = deathRange * tilesize;
        final boolean[] overlap = {false};
        Groups.build.each(build -> {
            if (overlap[0] || build.team != team || !(build instanceof PsychicRequiemBuild other) || !other.isAdded())
                return;
            if (self != null && other == self) return;

            float otherRange = other.block instanceof PsychicRequiemBlock block ? block.deathRange * tilesize : rangeWorld;
            if (Math.abs(other.x - worldX) < rangeWorld + otherRange && Math.abs(other.y - worldY) < rangeWorld + otherRange) {
                overlap[0] = true;
            }
        });
        return overlap[0];
    }

    public class PsychicRequiemBuild extends PsychicBuild {
        public float gainRate;
        public float warmup;
        public float gainedThisFrame;
        public float gainedWindow;
        public float gainedWindowTime;
        public transient WindowedMean gainRateMean;

        @Override
        public void updateTile() {
            super.updateTile();

            if (gainRateMean == null) {
                gainRateMean = new WindowedMean(Math.max(gainRateWindow, 1));
            }

            gainedWindow += gainedThisFrame;
            gainedWindowTime += delta();

            if (gainedWindowTime >= gainRateSampleInterval) {
                float sampledRate = gainedWindow * 60f / Math.max(gainedWindowTime, 0.0001f);
                gainRateMean.add(sampledRate);
                gainRate = gainRateMean.hasEnoughData() ? gainRateMean.mean() : sampledRate;
                gainedWindow = 0f;
                gainedWindowTime = 0f;
            } else if (!gainRateMean.hasEnoughData()) {
                gainRate = gainedWindow * 60f / Math.max(gainedWindowTime, 0.0001f);
            }

            if (efficiency > 0) {
                warmup = Mathf.approachDelta(warmup, psychicFraction() > 0.001f ? 1f : 0f, warmupSpeed);
            }
            gainedThisFrame = 0f;
        }

        public void harvestUnitDeath(Unit unit) {
            if (unit == null || !enabled || !isAdded()) return;
            if (unit.team == team) return;

            float rangeWorld = deathRange * tilesize;
            float dx = Math.abs(unit.x - x);
            float dy = Math.abs(unit.y - y);
            if (dx > rangeWorld || dy > rangeWorld) return;

            float gain = deathGain(unit);
            float accepted = addPsychic(gain);
            if (accepted > 0.0001f) {
                gainedThisFrame += accepted;
                WHFx.trailDeathSiphon(60, psychicColor, Math.max(unit.hitSize / 20, 2), (int) Mathf.clamp(unit.hitSize * 0.9f, 10f, 20))
                        .at(unit.x, unit.y, unit.hitSize, psychicColor, this);
            }
        }

        protected float deathGain(Unit unit) {
            float gain = baseDeathGain + Mathf.sqrt(Math.max(unit.maxHealth(), 1f)) * healthDeathScale;
            if (unit.isBoss() || unit.healthMultiplier > 2) {
                gain *= bossMultiplier;
            }
            return Math.min(gain, maxDeathGain);
        }

        @Override
        public void draw() {
            super.draw();

            float stored = psychicFraction();
            float radius = block.size * tilesize * (0.42f + stored * 0.18f + warmup * 0.12f);

            if (stored > 0.001f || warmup > 0.001f) {
                Drawf.light(x, y, radius * 2.4f, psychicColor, 0.2f + stored * 0.22f + warmup * 0.1f);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawRange(x, y, true);
            drawSelectText(
                    bundleFormat("bar.wh-psychic-storage",
                            Strings.autoFixed(psychicStored(), 2),
                            Strings.autoFixed(psychicCapacity(), 0)),
                    bundleFormat("bar.wh-psychic-harvest", Strings.autoFixed(gainRate, 2))
            );
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float progress() {
            return Mathf.clamp(gainRate / Math.max(baseDeathGain * 3f, 0.0001f));
        }
    }
}
