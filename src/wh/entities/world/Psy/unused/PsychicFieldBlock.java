package wh.entities.world.Psy.unused;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Strings;
import mindustry.graphics.Layer;
import mindustry.ui.Bar;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.entities.world.Psy.PsychicBlock;

import static mindustry.Vars.tilesize;

public abstract class PsychicFieldBlock extends PsychicBlock {
    private static final FieldSample tmpSample = new FieldSample();
    private static final PsychicField.Sample tmpFieldState = new PsychicField.Sample();

    public float fieldRangeX = 6f;
    public float fieldRangeY = 6f;
    public int fieldSampleSpacing = 2;

    public PsychicFieldBlock(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(WHStats.psychicFieldWidth, fieldRangeX * 2f, StatUnit.blocks);
        stats.add(WHStats.psychicFieldHeight, fieldRangeY * 2f, StatUnit.blocks);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-field", (PsychicFieldBuild build) -> new Bar(
                () -> bundleFormat("bar.wh-psychic-field", Strings.autoFixed(build.fieldConcentration, 3)),
                () -> psychicColor,
                () -> build.fieldConcentration
        ));

        addBar("psychic-flux", (PsychicFieldBuild build) -> new Bar(
                () -> bundleFormat("bar.wh-psychic-flux", Strings.autoFixed(build.fieldFlux, 3)),
                () -> psychicColor,
                () -> Mathf.clamp(build.fieldFlux * 12f)
        ));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        float worldX = placeWorldX(x);
        float worldY = placeWorldY(y);
        FieldSample sample = sampleFieldWorld(worldX, worldY, tmpSample);

        drawFieldRange(worldX, worldY);
        drawPlaceText(placeText(sample), x, y, valid);
    }

    protected String placeText(FieldSample sample) {
        return bundleFormat("bar.wh-psychic-field-sample",
                Strings.autoFixed(sample.concentration, 3),
                Strings.autoFixed(sample.flux, 3));
    }

    protected float placeWorldX(int tileX) {
        return tileX * tilesize + offset;
    }

    protected float placeWorldY(int tileY) {
        return tileY * tilesize + offset;
    }

    protected void drawFieldRange(float worldX, float worldY) {
        float width = fieldRangeX * 2f * tilesize;
        float height = fieldRangeY * 2f * tilesize;

        Draw.z(Layer.overlayUI);
        Draw.color(psychicColor);
        Draw.alpha(0.7f);
        Lines.stroke(1.2f);
        Lines.rect(worldX - width / 2f, worldY - height / 2f, width, height);
        Draw.reset();
    }

    protected FieldSample sampleFieldWorld(float worldX, float worldY, FieldSample out) {
        if (out == null) out = new FieldSample();
        out.clear();

        if (!PsychicField.active()) return out;

        int rangeX = Math.max(1, Mathf.ceil(fieldRangeX));
        int rangeY = Math.max(1, Mathf.ceil(fieldRangeY));
        int spacing = Math.max(1, fieldSampleSpacing);

        for (int ty = -rangeY; ty <= rangeY; ty += spacing) {
            for (int tx = -rangeX; tx <= rangeX; tx += spacing) {
                float sampleX = worldX + tx * tilesize;
                float sampleY = worldY + ty * tilesize;
                float weight = 1f;

                PsychicField.Sample state = PsychicField.sampleWorld(sampleX, sampleY, tmpFieldState);
                float concentration = state.concentration;
                float flux = state.flux();

                out.concentration += concentration * weight;
                out.disturbance += state.disturbance * weight;
                out.flux += flux * weight;
                out.gradientX += state.gradientX * weight;
                out.gradientY += state.gradientY * weight;
                out.peak = Math.max(out.peak, concentration);
                out.weight += weight;
                out.samples++;
            }
        }

        if (out.weight <= 0.0001f) {
            PsychicField.Sample state = PsychicField.sampleWorld(worldX, worldY, tmpFieldState);
            out.concentration = state.concentration;
            out.disturbance = state.disturbance;
            out.flux = state.flux();
            out.gradientX = state.gradientX;
            out.gradientY = state.gradientY;
            out.peak = out.concentration;
            out.weight = 1f;
            out.samples = 1;
            return out;
        }

        out.concentration /= out.weight;
        out.disturbance /= out.weight;
        out.flux /= out.weight;
        out.gradientX /= out.weight;
        out.gradientY /= out.weight;
        return out;
    }

    public static class FieldSample {
        public float concentration;
        public float disturbance;
        public float flux;
        public float peak;
        public float gradientX;
        public float gradientY;
        public float weight;
        public int samples;

        public FieldSample clear() {
            concentration = 0f;
            disturbance = 0f;
            flux = 0f;
            peak = 0f;
            gradientX = 0f;
            gradientY = 0f;
            weight = 0f;
            samples = 0;
            return this;
        }
    }

    public class PsychicFieldBuild extends PsychicBuild {
        public float fieldConcentration;
        public float fieldDisturbance;
        public float fieldFlux;
        public float fieldPeak;
        public float fieldGradientX;
        public float fieldGradientY;
        protected transient FieldSample sampledField = new FieldSample();

        protected void updateFieldSample() {
            if (sampledField == null) sampledField = new FieldSample();
            FieldSample sample = sampleFieldWorld(x, y, sampledField);
            fieldConcentration = sample.concentration;
            fieldDisturbance = sample.disturbance;
            fieldFlux = sample.flux;
            fieldPeak = sample.peak;
            fieldGradientX = sample.gradientX;
            fieldGradientY = sample.gradientY;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            updateFieldSample();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawFieldRange(x, y);
        }
    }
}
