package wh.entities.world.Psy;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.graphics.Drawn;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public class PsychicAttributeSourceBlock extends PsychicBlock {
    public Attribute attribute = Attribute.heat;
    public Attribute secondaryAttribute;
    public float secondaryScale = 0.35f;
    public float baseEfficiency = 0f;
    public float boostScale = 1f;
    public float maxBoost = 4f;
    public int attributeRadius = 1;
    public float generationRate = 3f;
    public float warmupSpeed = 0.05f;

    public PsychicAttributeSourceBlock(String name) {
        super(name);
        acceptsPsychicLinks = false;
        outputsPsychicLinks = true;
        configurable = false;
        drawArrow = false;
        buildType = PsychicAttributeSourceBuild::new;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, attributeRadius, StatUnit.blocks);
        PsychicStatValues.add(stats, WHStats.psychicProduction, generationRate * maxEfficiency(), StatUnit.perSecond);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-production", (PsychicAttributeSourceBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-production", Strings.autoFixed(build.productionRate, 2)),
                () -> psychicColor,
                () -> generationRate <= 0.0001f ? 0f : build.productionRate / Math.max(generationRate * maxEfficiency(), 0.0001f)
        ));

        addBar("psychic-efficiency", (PsychicAttributeSourceBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-efficiency", Strings.autoFixed(build.efficiencyScale * 100f, 0)),
                () -> psychicColor,
                () -> Mathf.clamp(build.efficiencyScale / Math.max(maxEfficiency(), 0.0001f))
        ));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        float worldX = x * tilesize + offset;
        float worldY = y * tilesize + offset;
        drawAttributeArea(worldX, worldY);
        float efficiency = efficiencyAt(x, y);
        drawPlaceText(
                bundleFormat("bar.wh-psychic-place-source",
                        Strings.autoFixed(efficiency * 100f, 0),
                        Strings.autoFixed(generationRate * efficiency, 2)),
                x, y, valid
        );
    }

    protected float maxEfficiency() {
        return Math.max(baseEfficiency + maxBoost * boostScale, 0f);
    }

    protected void drawAttributeArea(float worldX, float worldY) {
        float extent = (size * 0.5f + attributeRadius) * tilesize;
        Draw.z(Layer.overlayUI);
        Draw.color(Pal.accent);
        Draw.alpha(0.7f);
        Lines.stroke(1.2f);
        Lines.square(worldX, worldY, extent);
        Draw.reset();
    }

    protected float efficiencyAt(int tileX, int tileY) {
        float primary = sumAttribute(tileX, tileY, attribute);
        float secondary = secondaryAttribute == null ? 0f : sumAttribute(tileX, tileY, secondaryAttribute) * secondaryScale;
        float boost = Mathf.clamp(primary + secondary, 0f, maxBoost);
        return Math.max(baseEfficiency + boost * boostScale, 0f);
    }

    protected float sumAttribute(int tileX, int tileY, Attribute source) {
        if (source == null || Vars.world == null) return 0f;

        int footprint = Math.max((size - 1) / 2, 0);
        int minX = tileX - footprint - attributeRadius;
        int maxX = tileX + footprint + attributeRadius;
        int minY = tileY - footprint - attributeRadius;
        int maxY = tileY + footprint + attributeRadius;

        float sum = 0f;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Tile tile = Vars.world.tile(x, y);
                if (tile == null || tile.floor() == null) continue;
                sum += tile.floor().attributes.get(source);
            }
        }
        return sum;
    }

    public class PsychicAttributeSourceBuild extends PsychicBuild {
        public float efficiencyScale;
        public float productionRate;
        public float warmup;

        @Override
        public void updateTile() {
            super.updateTile();

            efficiencyScale = efficiencyAt(tile.x, tile.y);
            float produced = 0f;
            if (enabled && generationRate > 0f && efficiencyScale > 0f) {
                produced = addPsychic(generationRate * efficiencyScale / 60f * delta());
            }

            float actual = produced * 60f / Math.max(delta(), 0.0001f);
            productionRate = Mathf.lerpDelta(productionRate, actual, 0.16f);
            warmup = Mathf.approachDelta(warmup, actual > 0.001f ? 1f : 0f, warmupSpeed);
        }

        @Override
        public void draw() {
            super.draw();

            float stored = psychicFraction();
            float pulse = Mathf.absin(8f, 0.9f + warmup * 1.2f);
            float radius = block.size * tilesize * (0.34f + stored * 0.18f + warmup * 0.1f);

            Draw.z(Layer.effect);
            Draw.color(psychicColor, Color.white, 0.1f + warmup * 0.16f);
            Draw.alpha(0.14f + stored * 0.2f + warmup * 0.18f);
            Lines.stroke(1f + warmup * 1.4f);
            Lines.circle(x, y, radius + pulse * 0.16f);
            Fill.square(x, y, radius * 0.36f + pulse * 0.08f, 45f);
            Draw.reset();

            if (stored > 0.001f || warmup > 0.001f) {
                Drawf.light(x, y, radius * 2.6f, psychicColor, 0.18f + stored * 0.25f + warmup * 0.12f);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawAttributeArea(x, y);
            Drawn.overlayText(
                    bundleFormat("bar.wh-psychic-storage",
                            Strings.autoFixed(psychicStored(), 2),
                            Strings.autoFixed(psychicCapacity(), 0)) +
                            " | " + bundleFormat("bar.wh-psychic-efficiency", Strings.autoFixed(efficiencyScale * 100f, 0)) +
                            "\n" + bundleFormat("bar.wh-psychic-production", Strings.autoFixed(productionRate, 2)),
                    x, y, block.size * tilesize * 1.15f, psychicColor, false
            );
        }
    }
}
