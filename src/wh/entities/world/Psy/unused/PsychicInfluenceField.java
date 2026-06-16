package wh.entities.world.Psy.unused;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.IntSeq;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;
import mindustry.world.Tile;

final class PsychicInfluenceField {
    private static final float zeroEpsilon = 0.0001f;
    private static final float localDecayAlpha = 0.01f;
    private static final float globalDecayAlpha = 0.012f;
    private static final float positiveLimit = 0.28f;
    private static final float negativeLimit = 0.32f;
    private static final float wavePulseAmount = 0.012f;
    private static final float wavePulseScale = 0.0012f;
    private static final float bossWavePulseAmount = 0.02f;

    private final PsychicFieldGrid grid = new PsychicFieldGrid();
    private final IntSeq active = new IntSeq(false, 16);
    private float[] values;
    private boolean[] activeMarks;
    private float globalOffset;

    void resize(int worldWidth, int worldHeight, int sampleSpacing) {
        grid.resize(worldWidth, worldHeight, sampleSpacing);
        values = new float[grid.size()];
        activeMarks = new boolean[grid.size()];
        active.clear();
        globalOffset = 0f;
    }

    void clear() {
        values = null;
        activeMarks = null;
        active.clear();
        globalOffset = 0f;
    }

    void update() {
        if (values == null) return;

        globalOffset = Mathf.lerpDelta(globalOffset, 0f, globalDecayAlpha);
        if (Math.abs(globalOffset) <= zeroEpsilon) {
            globalOffset = 0f;
        }

        for (int i = 0; i < active.size; ) {
            int index = active.items[i];
            float value = Mathf.lerpDelta(values[index], 0f, localDecayAlpha);
            if (Math.abs(value) <= zeroEpsilon) {
                values[index] = 0f;
                activeMarks[index] = false;
                active.items[i] = active.items[active.size - 1];
                active.size--;
                continue;
            }

            values[index] = value;
            i++;
        }
    }

    float sample(int tileX, int tileY) {
        return globalOffset + grid.sample(values, tileX, tileY);
    }

    float localSample(int tileX, int tileY) {
        return grid.sample(values, tileX, tileY);
    }

    Vec2 gradient(int tileX, int tileY, Vec2 out) {
        return grid.gradient(values, tileX, tileY, out);
    }

    void addWavePulse(int wave, int bossSpawns) {
        float scaledWave = Mathf.clamp(Mathf.sqrt(Math.max(wave, 0f)) * wavePulseScale, 0f, 0.08f);
        float scaledBoss = Mathf.clamp(Math.max(bossSpawns, 0) * bossWavePulseAmount, 0f, 0.18f);
        globalOffset = Mathf.clamp(globalOffset + wavePulseAmount + scaledWave + scaledBoss, -negativeLimit, positiveLimit);
    }

    void addUnitDeath(Unit unit) {
        if (unit == null) return;

        float health = Math.max(unit.maxHealth(), 1f);
        float amount = Mathf.clamp(0.006f + Mathf.sqrt(health) * 0.0018f, 0.006f, 0.07f);
        float radiusTiles = 1 + Mathf.sqrt(health) * 0.1f + unit.hitSize() / Vars.tilesize * 0.15f;
        if (unit.isBoss()) {
            amount = Math.min(positiveLimit, amount * 1.35f);
            radiusTiles *= 1.18f;
        }
        addWorld(unit.x, unit.y, radiusTiles * Vars.tilesize, amount);
    }

    void addBlockDestroy(Tile tile) {
        if (tile == null) return;
        Building build = tile.build;
        if (build == null || build.block == null) return;

        float health = Math.max(build.maxHealth(), 1f);
        float amount = Mathf.clamp(0.008f + Mathf.sqrt(health) * 0.0018f, 0.008f, 0.07f);
        float radiusTiles = 3f + build.block.size * 1.25f + Mathf.sqrt(health) * 0.08f;
        addWorld(build.x, build.y, radiusTiles * Vars.tilesize, amount);
    }

    void addGeneratorPressureExplode(Building build) {
        if (build == null || build.block == null) return;

        float health = Math.max(build.maxHealth(), 1f);
        float pressure = Math.max(build.block.size, 1f);
        float amount = Mathf.clamp(0.02f + Mathf.sqrt(health) * 0.0024f + pressure * 0.006f, 0.02f, 0.12f);
        float radiusTiles = 4f + build.block.size * 1.6f + Mathf.sqrt(health) * 0.08f;
        addWorld(build.x, build.y, radiusTiles * Vars.tilesize, amount);
    }

    void addBulletDestroy(Bullet bullet) {
        if (bullet == null || bullet.type == null) return;

        float directDamage = Math.max(bullet.type.damage, 0f);
        float splashDamage = Math.max(bullet.type.splashDamage, 0f);
        float lightningDamage = Math.max(bullet.type.lightningDamage, 0f);
        float peakDamage = Math.max(directDamage, Math.max(splashDamage * 0.7f, lightningDamage * 0.55f));
        if (peakDamage <= 0f) return;

        float radiusWorld = Math.max(
                bullet.type.splashDamageRadius > 0f ? bullet.type.splashDamageRadius : 0f,
                Math.max(bullet.type.hitSize * 3.4f, Vars.tilesize * 1.5f)
        );
        float amount = Mathf.clamp(0.004f + Mathf.sqrt(peakDamage) * 0.0014f, 0.004f, 0.05f);

        if (bullet.type.fragBullet != null) amount *= 1.1f;
        if (bullet.type.splashDamageRadius > 0f) amount *= 1.08f;

        addWorld(bullet.x, bullet.y, radiusWorld, amount);
    }

    void addWorld(float worldX, float worldY, float radiusWorld, float amount) {
        addTile(worldX / Vars.tilesize, worldY / Vars.tilesize, radiusWorld / Vars.tilesize, amount);
    }

    void drainWorld(float worldX, float worldY, float radiusWorld, float amount) {
        addWorld(worldX, worldY, radiusWorld, -Math.abs(amount));
    }

    float globalOffset() {
        return globalOffset;
    }

    float[] copyValues() {
        if (values == null) return null;
        return values.clone();
    }

    void restore(float globalOffset, float[] restoredValues) {
        if (values == null || restoredValues == null || restoredValues.length != values.length) return;

        this.globalOffset = Mathf.clamp(globalOffset, -negativeLimit, positiveLimit);
        System.arraycopy(restoredValues, 0, values, 0, values.length);

        active.clear();
        if (activeMarks != null) {
            for (int i = 0; i < activeMarks.length; i++) {
                activeMarks[i] = false;
            }
        }

        for (int i = 0; i < values.length; i++) {
            float value = Mathf.clamp(values[i], -negativeLimit, positiveLimit);
            values[i] = Math.abs(value) <= zeroEpsilon ? 0f : value;
            if (values[i] != 0f) {
                activate(i);
            }
        }
    }

    private void addTile(float tileX, float tileY, float radiusTiles, float amount) {
        if (values == null || amount == 0f) return;

        float radius = Math.max(radiusTiles, grid.sampleSpacing * 0.5f);
        float radius2 = radius * radius;
        float invRadius2 = 1f / radius2;

        int minGx = Mathf.clamp((int) Math.floor((tileX - radius) / grid.sampleSpacing), 0, grid.gridWidth - 1);
        int minGy = Mathf.clamp((int) Math.floor((tileY - radius) / grid.sampleSpacing), 0, grid.gridHeight - 1);
        int maxGx = Mathf.clamp((int) Math.floor((tileX + radius) / grid.sampleSpacing), 0, grid.gridWidth - 1);
        int maxGy = Mathf.clamp((int) Math.floor((tileY + radius) / grid.sampleSpacing), 0, grid.gridHeight - 1);

        for (int gy = minGy; gy <= maxGy; gy++) {
            float sampleY = grid.tileYAt(gy);
            float dy = sampleY - tileY;

            for (int gx = minGx; gx <= maxGx; gx++) {
                float sampleX = grid.tileXAt(gx);
                float dx = sampleX - tileX;
                float dist2 = dx * dx + dy * dy;
                if (dist2 >= radius2) continue;

                float falloff = 1f - dist2 * invRadius2;
                float delta = amount * falloff * falloff;
                int index = gy * grid.gridWidth + gx;
                float value = Mathf.clamp(values[index] + delta, -negativeLimit, positiveLimit);
                values[index] = Math.abs(value) <= zeroEpsilon ? 0f : value;
                if (values[index] != 0f) {
                    activate(index);
                }
            }
        }
    }

    private void activate(int index) {
        if (activeMarks == null || activeMarks[index]) return;
        activeMarks[index] = true;
        active.add(index);
    }
}
