package wh.entities.world.Psy.unused;

import arc.math.Mathf;
import arc.math.geom.Vec2;

final class PsychicFieldGrid {
    int worldWidth;
    int worldHeight;
    int gridWidth;
    int gridHeight;
    int sampleSpacing;

    void resize(int worldWidth, int worldHeight, int sampleSpacing) {
        this.worldWidth = Math.max(worldWidth, 0);
        this.worldHeight = Math.max(worldHeight, 0);
        this.sampleSpacing = Math.max(sampleSpacing, 1);
        gridWidth = Math.max(2, Mathf.ceil((float) this.worldWidth / this.sampleSpacing) + 1);
        gridHeight = Math.max(2, Mathf.ceil((float) this.worldHeight / this.sampleSpacing) + 1);
    }

    int size() {
        return gridWidth * gridHeight;
    }

    int clampTileX(int tileX) {
        return Mathf.clamp(tileX, 0, Math.max(worldWidth - 1, 0));
    }

    int clampTileY(int tileY) {
        return Mathf.clamp(tileY, 0, Math.max(worldHeight - 1, 0));
    }

    int tileXAt(int gx) {
        return Math.min(Math.max(worldWidth - 1, 0), gx * sampleSpacing);
    }

    int tileYAt(int gy) {
        return Math.min(Math.max(worldHeight - 1, 0), gy * sampleSpacing);
    }

    float valueAt(float[] values, int gx, int gy) {
        return values[gy * gridWidth + gx];
    }

    float valueBlend(float[] current, float[] target, float alpha, int gx, int gy) {
        float value = valueAt(current, gx, gy);
        if (target == null || alpha <= 0f) return value;
        return Mathf.lerp(value, valueAt(target, gx, gy), alpha);
    }

    float sample(float[] values, int tileX, int tileY) {
        return sampleBlend(values, null, 0f, tileX, tileY);
    }

    float sampleBlend(float[] current, float[] target, float alpha, int tileX, int tileY) {
        if (current == null || worldWidth <= 0 || worldHeight <= 0) return 0f;

        tileX = clampTileX(tileX);
        tileY = clampTileY(tileY);

        float gx = tileX / (float) sampleSpacing;
        float gy = tileY / (float) sampleSpacing;
        int x0 = Mathf.clamp((int) gx, 0, gridWidth - 1);
        int y0 = Mathf.clamp((int) gy, 0, gridHeight - 1);
        int x1 = Math.min(x0 + 1, gridWidth - 1);
        int y1 = Math.min(y0 + 1, gridHeight - 1);
        float tx = gx - x0;
        float ty = gy - y0;

        float v00 = valueBlend(current, target, alpha, x0, y0);
        float v10 = valueBlend(current, target, alpha, x1, y0);
        float v01 = valueBlend(current, target, alpha, x0, y1);
        float v11 = valueBlend(current, target, alpha, x1, y1);

        float top = Mathf.lerp(v00, v10, tx);
        float bottom = Mathf.lerp(v01, v11, tx);
        return Mathf.lerp(top, bottom, ty);
    }

    Vec2 gradient(float[] values, int tileX, int tileY, Vec2 out) {
        return gradientBlend(values, null, 0f, tileX, tileY, out);
    }

    Vec2 gradientBlend(float[] current, float[] target, float alpha, int tileX, int tileY, Vec2 out) {
        if (out == null) out = new Vec2();
        if (current == null || worldWidth <= 0 || worldHeight <= 0) return out.setZero();

        tileX = clampTileX(tileX);
        tileY = clampTileY(tileY);

        float gx = tileX / (float) sampleSpacing;
        float gy = tileY / (float) sampleSpacing;
        int x0 = Mathf.clamp((int) gx, 0, gridWidth - 1);
        int y0 = Mathf.clamp((int) gy, 0, gridHeight - 1);
        int x1 = Math.min(x0 + 1, gridWidth - 1);
        int y1 = Math.min(y0 + 1, gridHeight - 1);
        float tx = gx - x0;
        float ty = gy - y0;

        float v00 = valueBlend(current, target, alpha, x0, y0);
        float v10 = valueBlend(current, target, alpha, x1, y0);
        float v01 = valueBlend(current, target, alpha, x0, y1);
        float v11 = valueBlend(current, target, alpha, x1, y1);
        float invSpacing = 1f / sampleSpacing;

        float dx = Mathf.lerp(v10 - v00, v11 - v01, ty) * invSpacing;
        float dy = Mathf.lerp(v01 - v00, v11 - v10, tx) * invSpacing;
        return out.set(dx, dy);
    }
}
