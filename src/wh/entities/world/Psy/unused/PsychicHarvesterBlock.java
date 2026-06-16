package wh.entities.world.Psy.unused;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.ui.Styles;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.graphics.Drawn;

import static mindustry.Vars.tilesize;

public class PsychicHarvesterBlock extends PsychicFieldMachineBlock {
    private static final Vec2 tmpGradient = new Vec2();

    public float fieldDrainScale = 0.045f;
    public float disturbanceWindow = 0.08f;

    public PsychicHarvesterBlock(String name) {
        super(name);

        acceptsPsychicLinks = true;
        outputsPsychicLinks = true;
        configurable = true;
        saveConfig = false;
        copyConfig = false;
        drawArrow = false;
        fullOverride = "power-node";
        buildType = PsychicHarvesterBuild::new;
    }

    @Override
    public void load() {
        super.load();
        if (!region.found() && fullIcon != null && fullIcon.found()) {
            region = fullIcon;
        }
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.add(WHStats.psychicHarvest, maxOperation, StatUnit.perSecond);
    }

    @Override
    protected String operationLabel() {
        return "Harvest";
    }

    @Override
    protected String placeText(FieldSample sample) {
        return "Disturb " + Strings.autoFixed(sample.disturbance, 3) +
                "  Flux " + Strings.autoFixed(sample.flux, 3) +
                "  " + operationLabel() + " " + Strings.autoFixed(harvestAt(sample.disturbance, sample.flux), 2) + "/s";
    }

    @Override
    protected float runOperation(PsychicFieldMachineBuild build, float amount) {
        return ((PsychicHarvesterBuild) build).addPsychic(amount);
    }

    @Override
    protected void applyFieldInfluence(PsychicFieldMachineBuild build, float amount) {
        PsychicHarvesterBuild harvester = (PsychicHarvesterBuild) build;
        PsychicField.drainWorld(harvester.x, harvester.y, harvester.fieldInfluenceRadiusWorld(), amount * fieldDrainScale);
    }

    @Override
    protected float targetOperation(PsychicFieldMachineBuild build) {
        return harvestAt(build.fieldDisturbance, build.fieldFlux);
    }

    public float harvestAt(float disturbance, float flux) {
        float positive = Math.max(disturbance, 0f);
        float normalized = Mathf.clamp((positive - minConcentration) / Math.max(disturbanceWindow, 0.0001f));
        float shaped = normalized * normalized;
        float fluxPart = Mathf.clamp(flux * 8f);
        return Math.min(maxOperation, baseOperation * shaped + concentrationOperation * normalized + fluxOperationScale * fluxPart);
    }

    public class PsychicHarvesterBuild extends PsychicFieldMachineBuild {

        @Override
        public void draw() {
            super.draw();

            float stored = psychicFraction();
            float pulse = Mathf.absin(8f, 1.5f + warmup * 1.8f);
            float radius = block.size * tilesize * (0.4f + stored * 0.2f + warmup * 0.08f);

            Draw.z(Layer.effect);
            Draw.color(psychicColor, Color.white, 0.08f + fieldConcentration * 0.25f);
            Draw.alpha(0.18f + stored * 0.22f + warmup * 0.18f);
            Lines.stroke(1f + warmup * 1.5f);
            Lines.circle(x, y, radius + pulse * 0.35f);
            Lines.circle(x, y, radius * 0.65f + pulse * 0.18f);

            tmpGradient.set(fieldGradientX, fieldGradientY);
            if (tmpGradient.len2() > 0.00001f) {
                tmpGradient.setLength(radius * 0.9f);
                Lines.line(x, y, x + tmpGradient.x, y + tmpGradient.y);
            }

            Draw.reset();

            if (stored > 0.001f || warmup > 0.001f) {
                Drawf.light(x, y, radius * 2.5f, psychicColor, 0.25f + stored * 0.35f + warmup * 0.1f);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawn.overlayText(
                    "Psi " + Strings.autoFixed(psychicStored(), 2) + "/" + Strings.autoFixed(psychicCapacity(), 0) +
                            " | Disturb " + Strings.autoFixed(fieldDisturbance, 3) +
                            " | Gain " + Strings.autoFixed(operationRate, 2) + "/s",
                    x, y, block.size * tilesize * 0.9f, psychicColor, false
            );
        }

        @Override
        public void buildConfiguration(Table table) {
            table.table(Styles.black6, t -> {
                t.defaults().left().pad(4f);
                t.add("@block.wh-warp-siphon.name").color(psychicColor).row();
                t.label(() -> "Stored psychic: " + Strings.autoFixed(psychicStored(), 3) + " / " + Strings.autoFixed(psychicCapacity(), 0)).row();
                t.label(() -> "Field area: " + Strings.autoFixed(fieldRangeX * 2f, 0) + " x " + Strings.autoFixed(fieldRangeY * 2f, 0)).row();
                t.label(() -> "Field disturbance: " + Strings.autoFixed(fieldDisturbance, 4)).row();
                t.label(() -> "Field peak: " + Strings.autoFixed(fieldPeak, 4)).row();
                t.label(() -> "Field flux: " + Strings.autoFixed(fieldFlux, 4)).row();
                t.label(() -> "Harvest rate: " + Strings.autoFixed(operationRate, 3) + "/s").row();
            });
        }
    }
}
