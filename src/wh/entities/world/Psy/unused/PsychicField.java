package wh.entities.world.Psy.unused;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.FrameBuffer;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.game.EventType;
import mindustry.game.SpawnGroup;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;
import mindustry.maps.Map;
import wh.entities.world.Psy.PsychicDeathHarvesterBlock;
import wh.graphics.WHShaders;

import java.util.Arrays;

public final class PsychicField {
    private static final float overlayLayer = Layer.overlayUI - 1f;
    private static final float overlayLayerRange = 0.01f;
    private static final float valueEpsilon = 0.0001f;
    private static final float transitionComplete = 0.9999f;

    public static int sampleSpacing = 24;
    public static int influenceSpacing = 8;
    public static float rebuildInterval = 120;
    public static float timeScale = 0.001f;
    public static float approachScale = 1f;
    public static float overlayAlpha = 0.8f;
    public static float overlayThreshold = 0.04f;
    public static boolean overlayShaderEnabled = true;

    private static final Color overlayLowColor = Color.valueOf("29183d");
    private static final Color overlayMidColor = Color.valueOf("7b4dd8");
    private static final Color overlayHighColor = Color.valueOf("d9ccff").a(0.68f);
    private static final Color overlayEventHotColor = Color.valueOf("ff8ac8");
    private static final Color overlayEventColdColor = Color.valueOf("74d9ff");
    private static final Vec2 tmpGradient = new Vec2();
    private static final Vec2 tmpInfluenceGradient = new Vec2();
    private static final PsychicFieldGrid baseGrid = new PsychicFieldGrid();
    private static final PsychicInfluenceField influences = new PsychicInfluenceField();

    private static boolean loaded;
    private static boolean eventHooksBound;
    private static int seed;
    private static float fieldTime;
    private static float targetFieldTime;
    private static float rebuildTimer;
    private static float transition;
    private static float[] samples;
    private static float[] targets;
    private static FrameBuffer overlayBuffer;
    private static int overlayWidth = -1;
    private static int overlayHeight = -1;
    private static PsychicFieldState stateEntity;

    private PsychicField() {
    }

    public static void init() {
        bindEventHooks();
    }

    public static void loadWorld() {
        init();
        if (Vars.world == null || Vars.world.width() <= 0 || Vars.world.height() <= 0) {
            clear();
            return;
        }

        baseGrid.resize(Vars.world.width(), Vars.world.height(), sampleSpacing);
        seed = buildSeed();

        fieldTime = 0f;
        targetFieldTime = sampleStepTime();
        rebuildTimer = 0f;
        transition = 0f;
        samples = new float[baseGrid.size()];
        targets = new float[baseGrid.size()];
        influences.resize(baseGrid.worldWidth, baseGrid.worldHeight, Math.max(influenceSpacing, 1));
        loaded = true;
        PsychicFieldState state = stateEntity(true);
        if (!restoreSavedState(state)) {
            clearFieldArray(samples);
            clearFieldArray(targets);
        }
    }

    public static void clear() {
        loaded = false;
        seed = 0;
        fieldTime = 0f;
        targetFieldTime = 0f;
        rebuildTimer = 0f;
        transition = 0f;
        samples = null;
        targets = null;
        baseGrid.resize(0, 0, sampleSpacing);
        influences.clear();
        if (overlayBuffer != null) {
            overlayBuffer.dispose();
            overlayBuffer = null;
        }
        overlayWidth = -1;
        overlayHeight = -1;
        stateEntity = null;
    }

    public static void update() {
        if (!active()) return;

        float interval = Math.max(rebuildInterval, valueEpsilon);
        float approach = Math.max(approachScale, 0f) / interval;
        fieldTime += Time.delta * timeScale;
        rebuildTimer += Time.delta;
        influences.update();

        transition = approach > 0f ? Mathf.approachDelta(transition, 1f, approach) : 1f;

        while (rebuildTimer >= interval) {
            rebuildTimer -= interval;
            commitDisplayedSamples();
            targetFieldTime += sampleStepTime();
            rebuild(targets, targetFieldTime);
        }
    }

    public static boolean active() {
        return loaded && samples != null && targets != null;
    }

    public static int width() {
        return baseGrid.worldWidth;
    }

    public static int height() {
        return baseGrid.worldHeight;
    }

    public static int seed() {
        return seed;
    }

    public static float get(int tileX, int tileY) {
        if (!active()) return 0f;
        return clampFieldValue(sampleDisplayed(tileX, tileY));
    }

    public static float getWorld(float worldX, float worldY) {
        if (!active()) return 0f;
        return get((int) (worldX / Vars.tilesize), (int) (worldY / Vars.tilesize));
    }

    public static float getExact(int tileX, int tileY) {
        if (!active()) return 0f;
        tileX = baseGrid.clampTileX(tileX);
        tileY = baseGrid.clampTileY(tileY);
        return clampFieldValue(influences.sample(tileX, tileY));
    }

    public static float disturbance(int tileX, int tileY) {
        if (!active()) return 0f;
        tileX = baseGrid.clampTileX(tileX);
        tileY = baseGrid.clampTileY(tileY);
        return influences.sample(tileX, tileY);
    }

    public static float disturbanceWorld(float worldX, float worldY) {
        if (!active()) return 0f;
        return disturbance((int) (worldX / Vars.tilesize), (int) (worldY / Vars.tilesize));
    }

    public static Sample sample(int tileX, int tileY, Sample out) {
        if (out == null) out = new Sample();
        if (!active()) return out.clear();

        tileX = baseGrid.clampTileX(tileX);
        tileY = baseGrid.clampTileY(tileY);

        float baseValue = baseGrid.sampleBlend(samples, targets, transition, tileX, tileY);
        float disturbance = influences.sample(tileX, tileY);
        baseGrid.gradientBlend(samples, targets, transition, tileX, tileY, tmpGradient);
        influences.gradient(tileX, tileY, tmpInfluenceGradient);

        out.concentration = clampFieldValue(baseValue + disturbance);
        out.disturbance = disturbance;
        out.gradientX = tmpGradient.x + tmpInfluenceGradient.x;
        out.gradientY = tmpGradient.y + tmpInfluenceGradient.y;
        return out;
    }

    public static Sample sampleWorld(float worldX, float worldY, Sample out) {
        return sample((int) (worldX / Vars.tilesize), (int) (worldY / Vars.tilesize), out);
    }

    public static void addPulseWorld(float worldX, float worldY, float radiusWorld, float amount) {
        if (!active()) return;
        influences.addWorld(worldX, worldY, radiusWorld, amount);
    }

    public static void drainWorld(float worldX, float worldY, float radiusWorld, float amount) {
        if (!active()) return;
        influences.drainWorld(worldX, worldY, radiusWorld, amount);
    }

    public static Vec2 gradient(int tileX, int tileY, Vec2 out) {
        if (out == null) out = new Vec2();
        if (!active()) return out.setZero();

        tileX = baseGrid.clampTileX(tileX);
        tileY = baseGrid.clampTileY(tileY);
        baseGrid.gradientBlend(samples, targets, transition, tileX, tileY, out);
        influences.gradient(tileX, tileY, tmpInfluenceGradient);
        return out.add(tmpInfluenceGradient);
    }

    public static Vec2 gradientWorld(float worldX, float worldY, Vec2 out) {
        return gradient((int) (worldX / Vars.tilesize), (int) (worldY / Vars.tilesize), out);
    }

    public static float gradientLength(int tileX, int tileY) {
        return gradient(tileX, tileY, tmpGradient).len();
    }

    public static void drawOverlay() {
        if (!active()) return;

        if (overlayShaderEnabled && WHShaders.psychicTide != null && !Vars.headless) {
            drawOverlayWithShader();
        } else {
            drawOverlayRaw();
        }
    }

    private static void drawOverlayWithShader() {
        if (Vars.headless) return;

        ensureOverlayBuffer();
        if (overlayBuffer == null) {
            drawOverlayRaw();
            return;
        }

        Draw.drawRange(overlayLayer, overlayLayerRange, () -> {
            overlayBuffer.begin(Color.clear);
            Draw.proj(Core.camera);
        }, () -> {
            Draw.flush();
            overlayBuffer.end();
            Draw.proj(Core.camera);
            overlayBuffer.blit(WHShaders.psychicTide);
            Draw.proj(Core.camera);
        });
        Draw.draw(overlayLayer, PsychicField::drawOverlayTiles);
    }

    private static void drawOverlayRaw() {
        Draw.draw(overlayLayer, PsychicField::drawOverlayTiles);
    }

    private static void drawOverlayTiles() {
        if (!active()) return;

        float cellSize = baseGrid.sampleSpacing * Vars.tilesize;
        float worldMaxX = baseGrid.worldWidth * Vars.tilesize;
        float worldMaxY = baseGrid.worldHeight * Vars.tilesize;

        Core.camera.bounds(Tmp.r1);
        Tmp.r1.grow(cellSize);

        int minGx = Mathf.clamp((int) Math.floor(Tmp.r1.x / cellSize), 0, baseGrid.gridWidth - 2);
        int minGy = Mathf.clamp((int) Math.floor(Tmp.r1.y / cellSize), 0, baseGrid.gridHeight - 2);
        int maxGx = Mathf.clamp((int) Math.floor((Tmp.r1.x + Tmp.r1.width) / cellSize), 0, baseGrid.gridWidth - 2);
        int maxGy = Mathf.clamp((int) Math.floor((Tmp.r1.y + Tmp.r1.height) / cellSize), 0, baseGrid.gridHeight - 2);

        for (int gy = minGy; gy <= maxGy; gy++) {
            float y0 = Math.min(gy * cellSize, worldMaxY);
            float y1 = Math.min((gy + 1) * cellSize, worldMaxY);
            if (y1 <= y0) continue;

            for (int gx = minGx; gx <= maxGx; gx++) {
                float x0 = Math.min(gx * cellSize, worldMaxX);
                float x1 = Math.min((gx + 1) * cellSize, worldMaxX);
                if (x1 <= x0) continue;

                float v00 = sampleDisplayedAt(gx, gy);
                float v10 = sampleDisplayedAt(gx + 1, gy);
                float v01 = sampleDisplayedAt(gx, gy + 1);
                float v11 = sampleDisplayedAt(gx + 1, gy + 1);
                float d00 = disturbanceAt(gx, gy);
                float d10 = disturbanceAt(gx + 1, gy);
                float d01 = disturbanceAt(gx, gy + 1);
                float d11 = disturbanceAt(gx + 1, gy + 1);

                float a00 = visibleAlpha(v00, d00);
                float a10 = visibleAlpha(v10, d10);
                float a01 = visibleAlpha(v01, d01);
                float a11 = visibleAlpha(v11, d11);
                if (a00 <= 0.001f && a10 <= 0.001f && a01 <= 0.001f && a11 <= 0.001f) continue;

                float c00 = colorBits(v00, d00, a00);
                float c10 = colorBits(v10, d10, a10);
                float c11 = colorBits(v11, d11, a11);
                float c01 = colorBits(v01, d01, a01);

                Fill.quad(
                        x0, y0, c00,
                        x1, y0, c10,
                        x1, y1, c11,
                        x0, y1, c01
                );
            }
        }

        Draw.reset();
    }

    private static void rebuild(float[] target, float time) {
        clearFieldArray(target);
    }

    private static void clearFieldArray(float[] values) {
        if (values == null) return;
        Arrays.fill(values, 0f);
    }

    private static float sampleDisplayed(int tileX, int tileY) {
        return baseGrid.sampleBlend(samples, targets, transition, tileX, tileY) + influences.sample(tileX, tileY);
    }

    private static float sampleDisplayedAt(int gx, int gy) {
        return clampFieldValue(
                baseGrid.valueBlend(samples, targets, transition, gx, gy) +
                        influences.sample(baseGrid.tileXAt(gx), baseGrid.tileYAt(gy))
        );
    }

    private static float disturbanceAt(int gx, int gy) {
        return influences.localSample(baseGrid.tileXAt(gx), baseGrid.tileYAt(gy));
    }

    private static float visibleAlpha(float value, float disturbance) {
        float scaled = Mathf.clamp((value - overlayThreshold) / Math.max(1f - overlayThreshold, valueEpsilon));
        float shaped = Mathf.pow(scaled, 0.8f) * (0.45f + scaled * 0.55f);
        float dynamic = Mathf.pow(Mathf.clamp(Math.abs(disturbance) / 0.1f), 0.95f) * 0.18f;
        return Mathf.clamp(Math.max(shaped * overlayAlpha, dynamic));
    }

    private static float colorBits(float value, float disturbance, float alpha) {
        float glow = Mathf.clamp((value - overlayThreshold) / Math.max(1f - overlayThreshold, valueEpsilon));
        float midMix = Mathf.clamp(glow * 1.4f);
        float highMix = Mathf.clamp((glow - 0.3f) / 0.5f);
        float dynamicMix = Mathf.clamp(Math.abs(disturbance) / 0.12f);
        Color dynamicColor = disturbance >= 0f ? overlayEventHotColor : overlayEventColdColor;

        return Tmp.c1.set(overlayLowColor)
                .lerp(overlayMidColor, midMix)
                .lerp(overlayHighColor, highMix)
                .lerp(dynamicColor, dynamicMix * 0.28f)
                .mul(0.9f + glow * 0.12f)
                .a(alpha)
                .toFloatBits();
    }

    private static float sampleStepTime() {
        return Math.max(rebuildInterval, valueEpsilon) * timeScale;
    }

    private static void ensureOverlayBuffer() {
        if (overlayBuffer == null) {
            overlayBuffer = new FrameBuffer();
        }
        int width = Core.graphics.getWidth();
        int height = Core.graphics.getHeight();
        if (overlayWidth != width || overlayHeight != height) {
            overlayBuffer.resize(width, height);
            overlayWidth = width;
            overlayHeight = height;
        }
    }

    private static void commitDisplayedSamples() {
        if (samples == null || targets == null || transition <= 0f) {
            transition = 0f;
            return;
        }

        if (transition >= transitionComplete) {
            System.arraycopy(targets, 0, samples, 0, samples.length);
        } else {
            for (int i = 0; i < samples.length; i++) {
                samples[i] = Mathf.lerp(samples[i], targets[i], transition);
            }
        }

        transition = 0f;
    }

    private static int buildSeed() {
        Map map = Vars.state == null ? null : Vars.state.map;
        int mapHash = map == null ? 0 : map.name().hashCode();
        int sectorId = Vars.state != null && Vars.state.rules != null && Vars.state.rules.sector != null
                ? Vars.state.rules.sector.id
                : 0;

        int mixed = 0x9E3779B9;
        mixed ^= baseGrid.worldWidth * 73856093;
        mixed ^= baseGrid.worldHeight * 19349663;
        mixed ^= mapHash;
        mixed ^= sectorId * 83492791;
        return mixed;
    }

    private static float clampFieldValue(float value) {
        return Mathf.clamp(value);
    }

    private static void bindEventHooks() {
        if (eventHooksBound) return;
        eventHooksBound = true;

        Events.on(EventType.UnitDestroyEvent.class, e -> {
            if (!active() || e.unit == null) return;
            influences.addUnitDeath(e.unit);
            PsychicDeathHarvesterBlock.handleUnitDeath(e.unit);
        });

        Events.on(EventType.BlockDestroyEvent.class, e -> {
            if (!active() || e.tile == null) return;
            influences.addBlockDestroy(e.tile);
        });

        Events.on(EventType.UnitBulletDestroyEvent.class, e -> {
            if (!active() || e.bullet == null) return;
            influences.addBulletDestroy(e.bullet);
        });

        Events.on(EventType.BuildingBulletDestroyEvent.class, e -> {
            if (!active() || e.bullet == null) return;
            influences.addBulletDestroy(e.bullet);
        });

        Events.on(EventType.GeneratorPressureExplodeEvent.class, e -> {
            if (!active() || e.build == null) return;
            influences.addGeneratorPressureExplode(e.build);
        });

        Events.on(EventType.WaveEvent.class, e -> {
            if (!active()) return;
            influences.addWavePulse(Vars.state == null ? 0 : Vars.state.wave, currentWaveBossSpawns());
        });
    }

    private static int currentWaveBossSpawns() {
        if (Vars.state == null || Vars.state.rules == null || Vars.state.rules.spawns == null) return 0;

        int waveIndex = Math.max(Vars.state.wave - 1, 0);
        int bossSpawns = 0;
        for (SpawnGroup group : Vars.state.rules.spawns) {
            if (group == null || group.effect != StatusEffects.boss) continue;
            bossSpawns += Math.max(group.getSpawned(waveIndex), 0);
        }
        return bossSpawns;
    }

    private static boolean restoreSavedState(PsychicFieldState state) {
        if (state == null) return false;
        if (state.worldWidth != baseGrid.worldWidth || state.worldHeight != baseGrid.worldHeight) return false;
        if (state.sampleSpacing != baseGrid.sampleSpacing || state.influenceSpacing != Math.max(influenceSpacing, 1))
            return false;
        if (state.seed != seed) return false;
        if (state.samples == null || state.targets == null || state.influences == null) return false;
        if (state.samples.length != samples.length || state.targets.length != targets.length) return false;

        fieldTime = Math.max(state.fieldTime, 0f);
        targetFieldTime = Math.max(state.targetFieldTime, sampleStepTime());
        rebuildTimer = Mathf.clamp(state.rebuildTimer, 0f, Math.max(rebuildInterval, valueEpsilon));
        transition = Mathf.clamp(state.transition);
        System.arraycopy(state.samples, 0, samples, 0, samples.length);
        System.arraycopy(state.targets, 0, targets, 0, targets.length);
        influences.restore(state.globalOffset, state.influences);
        return true;
    }

    static void copyToStateEntity(PsychicFieldState state) {
        if (state == null || !active()) return;

        state.worldWidth = baseGrid.worldWidth;
        state.worldHeight = baseGrid.worldHeight;
        state.sampleSpacing = baseGrid.sampleSpacing;
        state.influenceSpacing = Math.max(influenceSpacing, 1);
        state.seed = seed;
        state.fieldTime = fieldTime;
        state.targetFieldTime = targetFieldTime;
        state.rebuildTimer = rebuildTimer;
        state.transition = transition;
        state.globalOffset = influences.globalOffset();
        state.samples = samples == null ? null : samples.clone();
        state.targets = targets == null ? null : targets.clone();
        state.influences = influences.copyValues();
    }

    static PsychicFieldState stateEntity() {
        return stateEntity;
    }

    static void attachStateEntity(PsychicFieldState entity) {
        stateEntity = entity;
    }

    private static PsychicFieldState stateEntity(boolean create) {
        if (stateEntity != null && stateEntity.isAdded()) return stateEntity;

        stateEntity = null;
        Groups.sync.each(entity -> {
            if (stateEntity == null && entity instanceof PsychicFieldState fieldState) {
                stateEntity = fieldState;
            }
        });

        if (stateEntity == null && create) {
            stateEntity = new PsychicFieldState();
            stateEntity.add();
        }

        return stateEntity;
    }

    public static class Sample {
        public float concentration;
        public float disturbance;
        public float gradientX;
        public float gradientY;

        public Sample clear() {
            concentration = 0f;
            disturbance = 0f;
            gradientX = 0f;
            gradientY = 0f;
            return this;
        }

        public float flux() {
            return Mathf.len(gradientX, gradientY);
        }
    }
}
