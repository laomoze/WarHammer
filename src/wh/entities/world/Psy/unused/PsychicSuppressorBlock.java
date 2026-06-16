package wh.entities.world.Psy.unused;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.ui.Styles;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.graphics.Drawn;

import static mindustry.Vars.tilesize;

public class PsychicSuppressorBlock extends PsychicFieldMachineBlock {
    public float suppressFieldScale = 1f;

    public PsychicSuppressorBlock(String name) {
        super(name);
        acceptsPsychicLinks = false;
        outputsPsychicLinks = false;
        buildType = PsychicSuppressorBuild::new;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(WHStats.psychicSuppression, maxOperation, StatUnit.perSecond);
    }

    @Override
    protected String operationLabel() {
        return "Suppress";
    }

    @Override
    protected float runOperation(PsychicFieldMachineBuild build, float amount) {
        return amount;
    }

    @Override
    protected void applyFieldInfluence(PsychicFieldMachineBuild build, float amount) {
        PsychicSuppressorBuild suppressor = (PsychicSuppressorBuild) build;
        PsychicField.drainWorld(suppressor.x, suppressor.y, fieldInfluenceRadius * tilesize, amount * suppressFieldScale);
    }

    public class PsychicSuppressorBuild extends PsychicFieldMachineBuild {
        @Override
        public void draw() {
            super.draw();

            float field = arc.math.Mathf.clamp(fieldConcentration + Math.abs(fieldDisturbance) * 0.8f);
            float pulse = warmup <= 0.001f ? 0f : arc.math.Mathf.absin(7f, 1.1f + warmup * 1.2f);
            float radius = block.size * tilesize * (0.42f + field * 0.16f + warmup * 0.1f);

            Draw.z(Layer.effect);
            Draw.color(psychicColor, Color.white, 0.08f + warmup * 0.08f);
            Draw.alpha(0.12f + field * 0.14f + warmup * 0.2f);
            Lines.stroke(1.1f + warmup * 1.6f);
            Lines.square(x, y, radius + pulse * 0.35f, 45f);
            Lines.square(x, y, radius * 0.68f + pulse * 0.18f, 45f);
            Draw.reset();

            if (field > 0.001f || warmup > 0.001f) {
                Drawf.light(x, y, radius * 2.3f, psychicColor, 0.16f + field * 0.18f + warmup * 0.12f);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawn.overlayText(
                    "Field " + Strings.autoFixed(fieldConcentration, 3) +
                            " | Disturb " + Strings.autoFixed(fieldDisturbance, 3) +
                            " | Suppress " + Strings.autoFixed(operationRate, 2) + "/s",
                    x, y, block.size * tilesize * 0.9f, psychicColor, false
            );
        }

        @Override
        public void buildConfiguration(Table table) {
            table.table(Styles.black6, t -> {
                t.defaults().left().pad(4f);
                t.add(block.localizedName).color(psychicColor).row();
                t.label(() -> "Field concentration: " + Strings.autoFixed(fieldConcentration, 4)).row();
                t.label(() -> "Field disturbance: " + Strings.autoFixed(fieldDisturbance, 4)).row();
                t.label(() -> "Field flux: " + Strings.autoFixed(fieldFlux, 4)).row();
                t.label(() -> "Suppression rate: " + Strings.autoFixed(operationRate, 3) + "/s").row();
            });
        }
    }
}
