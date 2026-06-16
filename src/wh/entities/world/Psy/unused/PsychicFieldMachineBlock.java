package wh.entities.world.Psy.unused;

import arc.math.Mathf;
import arc.util.Strings;
import mindustry.ui.Bar;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;

import static mindustry.Vars.tilesize;

public abstract class PsychicFieldMachineBlock extends PsychicFieldBlock {
    public float minConcentration = 0.2f;
    public float baseOperation = 0.35f;
    public float concentrationOperation = 0.9f;
    public float fluxOperationScale = 0.45f;
    public float maxOperation = 1.6f;
    public float warmupSpeed = 0.045f;
    public float fieldInfluenceRadius = 6f;
    public float fieldInfluenceScale = 0.045f;

    public PsychicFieldMachineBlock(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(WHStats.psychicThreshold, minConcentration * 100f, StatUnit.percent);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-operation", (PsychicFieldMachineBuild build) -> new Bar(
                () -> operationLabel() + " " + Strings.autoFixed(build.operationRate, 2) + "/s",
                () -> psychicColor,
                () -> maxOperation <= 0.0001f ? 0f : build.operationRate / maxOperation
        ));
    }

    @Override
    protected String placeText(FieldSample sample) {
        return "Field " + Strings.autoFixed(sample.concentration, 3) +
                "  Flux " + Strings.autoFixed(sample.flux, 3) +
                "  " + operationLabel() + " " + Strings.autoFixed(operationAt(sample.concentration, sample.flux), 2) + "/s";
    }

    public float operationAt(float concentration, float flux) {
        float normalized = Mathf.clamp((concentration - minConcentration) / Math.max(1f - minConcentration, 0.0001f));
        float shaped = normalized * normalized;
        float fluxPart = Mathf.clamp(flux * 12f);
        return Math.min(maxOperation, baseOperation * shaped + concentrationOperation * normalized + fluxOperationScale * fluxPart);
    }

    protected float targetOperation(PsychicFieldMachineBuild build) {
        return operationAt(build.fieldConcentration, build.fieldFlux);
    }

    protected abstract String operationLabel();

    protected abstract float runOperation(PsychicFieldMachineBuild build, float amount);

    protected abstract void applyFieldInfluence(PsychicFieldMachineBuild build, float amount);

    public class PsychicFieldMachineBuild extends PsychicFieldBuild {
        public float operationRate;
        public float warmup;

        @Override
        public void updateTile() {
            super.updateTile();

            float moved = 0f;
            if (enabled) {
                float targetOperation = targetOperation(this);
                if (targetOperation > 0f) {
                    moved = runOperation(this, targetOperation / 60f * delta());
                    if (moved > 0.0001f && fieldInfluenceScale > 0f) {
                        applyFieldInfluence(this, moved);
                    }
                }
            }

            float actualOperation = delta() <= 0.0001f ? 0f : moved * 60f / delta();
            operationRate = Mathf.lerpDelta(operationRate, actualOperation, 0.16f);
            warmup = Mathf.approachDelta(warmup, actualOperation > 0.001f ? 1f : 0f, warmupSpeed);
        }

        public float fieldInfluenceRadiusWorld() {
            return fieldInfluenceRadius * tilesize;
        }
    }
}
