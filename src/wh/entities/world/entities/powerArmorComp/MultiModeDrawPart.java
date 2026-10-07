package wh.entities.world.entities.powerArmorComp;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.struct.LongMap;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.Effect;
import wh.content.WHFx;

public class MultiModeDrawPart extends DrawUnitPart {
    public interface ModeDrawer {
        void draw(UnitPartParams params, float progress, float alpha);
    }

    public static class ModeEffect {
        public Effect effect;
        public float x, y, width, height;
        public float effectRot, effectRandRot;
        public float interval;
        public float chance;
        public Color color = Color.white;
        public boolean useProgress = true;
        private final IntMap<Float> lastSpawnTimes = new IntMap<>();
        private final IntMap<Float> lastChanceTimes = new IntMap<>();

        public ModeEffect(Effect effect) {
            this.effect = effect;
        }

        private void update(UnitPartParams params, float progress) {
            if (effect == null || Vars.state.isPaused()) return;

            int unitId = params.unit.id;
            float now = Time.time;
            boolean spawn;
            if (interval > 0f) {
                float lastSpawn = lastSpawnTimes.get(unitId, Float.NEGATIVE_INFINITY);
                spawn = now - lastSpawn >= interval;
                if (spawn) lastSpawnTimes.put(unitId, now);
            } else {
                float lastChance = lastChanceTimes.get(unitId, Float.NEGATIVE_INFINITY);
                if (lastChance == now) return;
                lastChanceTimes.put(unitId, now);
                spawn = Mathf.chanceDelta(chance * (useProgress ? progress : 1f));
            }
            if (!spawn) return;

            float offsetRotation = params.positionRotation - 90f;
            float drawX = params.x + Angles.trnsx(offsetRotation, x, y);
            float drawY = params.y + Angles.trnsy(offsetRotation, x, y);
            float rotation = params.rotation + effectRot + Mathf.range(effectRandRot);
            drawX += Angles.trnsx(rotation, Mathf.range(width * 0.5f), Mathf.range(height * 0.5f));
            drawY += Angles.trnsy(rotation, Mathf.range(width * 0.5f), Mathf.range(height * 0.5f));
            effect.at(drawX, drawY, rotation, color);

        }
    }

    public float x, y, rotation;
    public UnitPartProgress progress = p -> Mathf.curve(p.actionTime, 0, 0.15f) * WHFx.fout(p.actionTime, 0.15f);
    public UnitPartProgress alpha = p -> WHFx.fout(p.actionTime, 0.15f);
    public boolean inheritRotation = true;
    public final IntMap<ModeDrawer> modes = new IntMap<>();
    public final LongMap<ModeDrawer> stepModes = new LongMap<>();
    public final IntMap<ModeEffect> effects = new IntMap<>();
    public final LongMap<ModeEffect> stepEffects = new LongMap<>();

    private final UnitPartParams modeParams = new UnitPartParams();

    public MultiModeDrawPart mode(int index, ModeDrawer drawer) {
        if (drawer != null) modes.put(index, drawer);
        return this;
    }

    /**
     * Registers a drawer for a specific combo step and the mode inside that step.
     */
    public MultiModeDrawPart mode(int stepIndex, int modeIndex, ModeDrawer drawer) {
        long key = stepModeKey(stepIndex, modeIndex);
        if (drawer == null) stepModes.remove(key);
        else stepModes.put(key, drawer);
        return this;
    }

    public MultiModeDrawPart progress(UnitPartProgress progress) {
        this.progress = progress == null ? p -> 0f : progress;
        return this;
    }

    public MultiModeDrawPart alpha(UnitPartProgress alpha) {
        this.alpha = alpha == null ? p -> 0f : alpha;
        return this;
    }

    public MultiModeDrawPart effect(int index, ModeEffect effect) {
        if (effect == null) effects.remove(index);
        else effects.put(index, effect);
        return this;
    }

    /**
     * Registers an effect for a specific combo step and the mode inside that step.
     */
    public MultiModeDrawPart effect(int stepIndex, int modeIndex, ModeEffect effect) {
        long key = stepModeKey(stepIndex, modeIndex);
        if (effect == null) stepEffects.remove(key);
        else stepEffects.put(key, effect);
        return this;
    }

    public MultiModeDrawPart effect(int index, Effect effect, float interval, float chance) {
        return effect(index, new ModeEffect(effect) {{
            this.interval = interval;
            this.chance = chance;
        }});
    }

    public MultiModeDrawPart effect(int stepIndex, int modeIndex, Effect effect, float interval, float chance) {
        return effect(stepIndex, modeIndex, new ModeEffect(effect) {{
            this.interval = interval;
            this.chance = chance;
        }});
    }

    @Override
    public void draw(UnitPartParams params) {
        if (!(params.unit instanceof MultiModePowerArmourUnit unit)) return;

        MultiModePowerArmourUnitType.ComboAction action = unit.multiType() == null
                ? null : unit.multiType().attack(unit.activeAttack);
        int globalIndex = unit.activeAttack;
        int stepIndex = action == null ? -1 : action.stepIndex;
        int modeIndex = action == null ? -1 : action.modeIndex;
        float modeProgress = Mathf.clamp(progress.get(params));
        float modeAlpha = Mathf.clamp(alpha.get(params));
        prepareParams(params);
        drawMode(stepIndex, modeIndex, globalIndex, modeParams, modeProgress, modeAlpha);

        ModeEffect effect = stepEffects.get(stepModeKey(stepIndex, modeIndex));
        if (effect == null) effect = effects.get(globalIndex);
        if (effect != null && unit.attackPhase == MultiModePowerArmourUnit.phaseAttacking) {
            effect.update(modeParams, modeProgress);
        }

        Draw.reset();
    }

    protected void drawMode(int mode, UnitPartParams params, float progress, float alpha) {
        ModeDrawer drawer = modes.get(mode);
        if (drawer != null) drawer.draw(params, progress, alpha);
    }

    protected void drawMode(int stepIndex, int modeIndex, int globalIndex, UnitPartParams params,
                            float progress, float alpha) {
        ModeDrawer drawer = stepModes.get(stepModeKey(stepIndex, modeIndex));
        if (drawer != null) {
            drawer.draw(params, progress, alpha);
        } else {
            drawMode(globalIndex, params, progress, alpha);
        }
    }

    private static long stepModeKey(int stepIndex, int modeIndex) {
        return ((long) stepIndex << 32) ^ (modeIndex & 0xffffffffL);
    }

    private void prepareParams(UnitPartParams params) {
        modeParams.set(params.unit, params.type, params.bodyMove, params.warmup, params.reload,
                params.smoothReload, params.smoothHeat, params.heat, params.recoil, params.charge,
                params.actionTime, params.x, params.y, inheritRotation ? params.rotation + rotation : rotation);
        modeParams.positionRotation = inheritRotation ? params.positionRotation + rotation : rotation;
        modeParams.motionX = params.motionX;
        modeParams.motionY = params.motionY;
        modeParams.motionRotation = params.motionRotation;
        modeParams.sideMultiplier = params.sideMultiplier;
        modeParams.sideOverride = params.sideOverride;
        modeParams.life = params.life;
        modeParams.modeIndex = params.modeIndex;

        if (x != 0f || y != 0f) {
            float offsetRotation = params.positionRotation - 90f;
            modeParams.x += Angles.trnsx(offsetRotation, x, y);
            modeParams.y += Angles.trnsy(offsetRotation, x, y);
        }
    }
}
