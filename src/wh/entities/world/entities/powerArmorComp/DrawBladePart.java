package wh.entities.world.entities.powerArmorComp;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.gen.MechUnit;
import mindustry.graphics.Pal;
import wh.graphics.Drawn;

public class DrawBladePart extends DrawUnitPart{
    public float layer, layerOffset = 10;
    public float x, y;
    public float actionX, actionY, actionRot;
    public float moveX, moveY, moveRot;
    public float rotation;
    public Color heatColor = Pal.accent;
    public UnitPartProgress progress = UnitPartProgress.warmup;
    public float rad = 0;
    public float fanRadius;
    public int fanRotationPartIndex = -1;
    public float fanRotationOffset;
    public float fanAlpha = 0.35f;
    public Color color = Pal.accent;
    public UnitPartProgress actionProgress = UnitPartProgress.actionTime;
    private final MultiModePowerArmourUnitType.PoseTransform fanRotationPose = new MultiModePowerArmourUnitType.PoseTransform();

    public DrawBladePart(){
    }

    @Override
    public void draw(UnitPartParams params){

        MechUnit unit = params.unit;

        if(layer > 0) Draw.z(layer);
        Draw.z(Draw.z() + layerOffset);

        float action = actionProgress.getClamp(params, false);
        calculateBladeTip(params, action);

        if (unit.mounts.length > 0 && fanVisible(unit)) {
            float fanProgress = Mathf.clamp(action);
            float centerX = params.x + Tmp.v1.x;
            float centerY = params.y + Tmp.v1.y;

            float fanRad = fanRadius > 0f ? fanRadius : rad;
            if (fanRad > 0f && fanProgress > 0f) {
                float startAngle = fanDirection(params, 0f);
                float currentAngle = fanDirection(params, fanProgress);
                float sweep = signedAngleDelta(startAngle, currentAngle);
                boolean positiveSweep = sweep >= 0f;
                float drawStart = positiveSweep ? startAngle : currentAngle;
                float fadeStart = positiveSweep ? 0f : 1f;
                float fadeEnd = positiveSweep ? 1f : 0f;
                if (Math.abs(sweep) > 0.1f) {
                    Draw.color(color);
                    Drawn.fillCirclePercentFade(centerX, centerY, centerX, centerY, fanRad,
                            Math.abs(sweep) / 360f, drawStart, fanAlpha, fadeStart, fadeEnd);
                }
            }

            Draw.reset();
        }
    }

    private float calculateBladeTip(UnitPartParams params, float action) {
        float ax = actionX * action, ay = actionY * action, aRot = actionRot * action;
        float prog = progress.getClamp(params, true);
        float mx = moveX * prog + ax + params.motionX;
        float my = moveY * prog + ay + params.motionY;
        float mr = moveRot * prog + rotation + aRot + params.motionRotation;
        float rot = mr + params.rotation;

        Tmp.v1.set(x + mx, y + my).rotateRadExact((params.rotation - 90f) * Mathf.degRad);
        Tmp.v2.set(Tmp.v1).trnsExact(rot, rad).add(params.x, params.y);
        return rot;
    }

    private float fanDirection(UnitPartParams params, float action) {
        float moveProgress = progress.getClamp(params, true);
        float direction = params.rotation + rotation + moveRot * moveProgress + fanRotationOffset;
        if (fanRotationPartIndex >= 0 && params.unit instanceof MultiModePowerArmourUnit multiUnit) {
            multiUnit.attackPose(fanRotationPartIndex, action, fanRotationPose);
            direction += fanRotationPose.rotation;
        }
        return direction;
    }

    private float signedAngleDelta(float from, float to) {
        return Mathf.mod(to - from + 180f, 360f) - 180f;
    }

    private boolean fanVisible(MechUnit unit) {
        if (!(unit instanceof MultiModePowerArmourUnit multiUnit)) return true;
        return multiUnit.attackPhase == MultiModePowerArmourUnit.phaseAttacking;
    }
}
