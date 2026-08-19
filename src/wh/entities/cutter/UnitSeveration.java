package wh.entities.cutter;

import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import wh.graphics.WHShaders;

/**
 * 动态单位切割的当前帧几何容器。
 *
 * 每帧截获 unit.draw() 发出的普通贴图四边形，展开为三角形后按切线裁成两侧。
 * 该类不再创建或更新独立碎块实体，所有姿态均直接跟随原版单位本帧状态。
 */
final class UnitSeveration {
    private static final float epsilon = 0.00001f;
    private static final FloatSeq positive = new FloatSeq(24);
    private static final FloatSeq negative = new FloatSeq(24);
    private static final float[] drawVertices = new float[24];

    private final Seq<CutTri> tris = new Seq<>();
    private final Seq<CutTri> spareTris = new Seq<>();

    private float x;
    private float y;
    private float bounds;

    static UnitSeveration begin(float x, float y) {
        UnitSeveration result = new UnitSeveration();
        result.x = x;
        result.y = y;
        return result;
    }

    /**
     * 清空上一帧几何并复用三角形对象，避免动态捕获产生持续 GC。
     */
    void resetFrameCapture(float x, float y, float bounds) {
        if (!tris.isEmpty()) {
            spareTris.addAll(tris);
            tris.clear();
        }
        this.x = x;
        this.y = y;
        this.bounds = bounds;
    }

    boolean empty() {
        return tris.isEmpty();
    }

    /**
     * 将一次 Draw.rect 展开为两块带 UV 的世界坐标三角形。
     */
    void addQuad(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
                 float rotation, float color, float z) {
        float cos = Mathf.cosDeg(rotation);
        float sin = Mathf.sinDeg(rotation);
        float baseX = x + originX;
        float baseY = y + originY;

        float x0 = -originX * cos + originY * sin + baseX;
        float y0 = -originX * sin - originY * cos + baseY;
        float x1 = (width - originX) * cos + originY * sin + baseX;
        float y1 = (width - originX) * sin - originY * cos + baseY;
        float x2 = (width - originX) * cos - (height - originY) * sin + baseX;
        float y2 = (width - originX) * sin + (height - originY) * cos + baseY;
        float x3 = -originX * cos - (height - originY) * sin + baseX;
        float y3 = -originX * sin + (height - originY) * cos + baseY;

        addWorldTriangle(region.texture, color, z,
                x0, y0, region.u, region.v2,
                x1, y1, region.u2, region.v2,
                x2, y2, region.u2, region.v);
        addWorldTriangle(region.texture, color, z,
                x0, y0, region.u, region.v2,
                x2, y2, region.u2, region.v,
                x3, y3, region.u, region.v);
    }

    /**
     * 按当前帧捕获的贴图裁出两侧，并施加法线分离和小幅张开。
     */
    void drawClipped(float worldX1, float worldY1, float worldX2, float worldY2,
                     float separation, float openingAngle) {
        float length = Mathf.dst(worldX1, worldY1, worldX2, worldY2);
        if (Mathf.zero(length)) return;

        float normalX = -(worldY2 - worldY1) / length * separation;
        float normalY = (worldX2 - worldX1) / length * separation;
        drawClippedSide(worldX1, worldY1, worldX2, worldY2, true, positive,
                normalX, normalY, openingAngle);
        drawClippedSide(worldX1, worldY1, worldX2, worldY2, false, negative,
                -normalX, -normalY, -openingAngle);
    }

    /**
     * 同侧三角形共用一次 Shader 参数；每个三角形仍放入自己的 z 排序回调，
     * 以兼容 Arc 在排序开启时只能在 Draw.draw 回调中切换 Shader 的限制。
     */
    private void drawClippedSide(float x1, float y1, float x2, float y2, boolean keepPositive,
                                 FloatSeq polygon, float offsetX, float offsetY, float openingAngle) {
        final boolean useCutShader = WHShaders.alphaCut != null;
        final float angleCos = Mathf.cosDeg(openingAngle);
        final float angleSin = Mathf.sinDeg(openingAngle);
        final float transformedX1 = transformX(x1, y1, angleCos, angleSin, offsetX);
        final float transformedY1 = transformY(x1, y1, angleCos, angleSin, offsetY);
        final float transformedX2 = transformX(x2, y2, angleCos, angleSin, offsetX);
        final float transformedY2 = transformY(x2, y2, angleCos, angleSin, offsetY);
        final float cutWidth = Mathf.clamp(bounds * 0.025f, 1.5f, 5f);

        for (CutTri tri : tris) {
            Draw.draw(tri.z, () -> {
                clip(tri, x1, y1, x2, y2, keepPositive, polygon);
                if (polygon.size < 12) return;

                if (useCutShader) {
                    WHShaders.alphaCut.setCutEdge(true, transformedX1, transformedY1,
                            transformedX2 - transformedX1, transformedY2 - transformedY1,
                            cutWidth, 1f, 0f, 0f, 1f, 1f);
                    Draw.shader(WHShaders.alphaCut);
                }
                try {
                    drawClippedPolygon(tri, polygon, offsetX, offsetY, angleCos, angleSin);
                } finally {
                    if (useCutShader) Draw.shader();
                }
            });
        }
    }

    private void drawClippedPolygon(CutTri tri, FloatSeq polygon, float offsetX, float offsetY,
                                    float angleCos, float angleSin) {
        for (int index = 4; index < polygon.size - 4; index += 4) {
            writeClippedVertex(0, tri, polygon, 0, offsetX, offsetY, angleCos, angleSin);
            writeClippedVertex(1, tri, polygon, index, offsetX, offsetY, angleCos, angleSin);
            writeClippedVertex(2, tri, polygon, index + 4, offsetX, offsetY, angleCos, angleSin);
            writeClippedVertex(3, tri, polygon, index + 4, offsetX, offsetY, angleCos, angleSin);
            Draw.vert(tri.texture, drawVertices, 0, drawVertices.length);
        }
    }

    private void addWorldTriangle(Texture texture, float color, float z,
                                  float x1, float y1, float u1, float v1,
                                  float x2, float y2, float u2, float v2,
                                  float x3, float y3, float u3, float v3) {
        CutTri tri = obtainTri(texture, color, z);
        setVertex(tri.vertices, 0, x1, y1, u1, v1);
        setVertex(tri.vertices, 4, x2, y2, u2, v2);
        setVertex(tri.vertices, 8, x3, y3, u3, v3);
        if (triangleArea(tri.vertices) > epsilon) {
            tris.add(tri);
        } else {
            spareTris.add(tri);
        }
    }

    private void writeClippedVertex(int target, CutTri tri, FloatSeq polygon, int source,
                                    float offsetX, float offsetY, float angleCos, float angleSin) {
        int offset = target * 6;
        float relativeX = polygon.items[source] - x;
        float relativeY = polygon.items[source + 1] - y;
        drawVertices[offset] = relativeX * angleCos - relativeY * angleSin + x + offsetX;
        drawVertices[offset + 1] = relativeX * angleSin + relativeY * angleCos + y + offsetY;
        drawVertices[offset + 2] = tri.color;
        drawVertices[offset + 3] = polygon.items[source + 2];
        drawVertices[offset + 4] = polygon.items[source + 3];
        drawVertices[offset + 5] = Color.clearFloatBits;
    }

    private float transformX(float worldX, float worldY, float cos, float sin, float offsetX) {
        float relativeX = worldX - x;
        float relativeY = worldY - y;
        return relativeX * cos - relativeY * sin + x + offsetX;
    }

    private float transformY(float worldX, float worldY, float cos, float sin, float offsetY) {
        float relativeX = worldX - x;
        float relativeY = worldY - y;
        return relativeX * sin + relativeY * cos + y + offsetY;
    }

    private static void clip(CutTri tri, float x1, float y1, float x2, float y2,
                             boolean keepPositive, FloatSeq output) {
        output.clear();
        int previous = 8;
        float previousCross = lineCross(x1, y1, x2, y2, tri.vertices[previous], tri.vertices[previous + 1]);
        boolean previousInside = keepPositive ? previousCross >= -epsilon : previousCross <= epsilon;

        for (int current = 0; current < tri.vertices.length; current += 4) {
            float currentCross = lineCross(x1, y1, x2, y2, tri.vertices[current], tri.vertices[current + 1]);
            boolean currentInside = keepPositive ? currentCross >= -epsilon : currentCross <= epsilon;
            if (currentInside != previousInside) {
                float progress = Mathf.clamp(previousCross / (previousCross - currentCross));
                addInterpolated(output, tri.vertices, previous, current, progress);
            }
            if (currentInside) {
                add(output, tri.vertices[current], tri.vertices[current + 1],
                        tri.vertices[current + 2], tri.vertices[current + 3]);
            }
            previous = current;
            previousCross = currentCross;
            previousInside = currentInside;
        }
    }

    private static void addInterpolated(FloatSeq output, float[] input, int from, int to, float progress) {
        add(output,
                input[from] + (input[to] - input[from]) * progress,
                input[from + 1] + (input[to + 1] - input[from + 1]) * progress,
                input[from + 2] + (input[to + 2] - input[from + 2]) * progress,
                input[from + 3] + (input[to + 3] - input[from + 3]) * progress);
    }

    private static void add(FloatSeq output, float x, float y, float u, float v) {
        output.add(x);
        output.add(y);
        output.add(u);
        output.add(v);
    }

    private static float triangleArea(float[] vertices) {
        return Math.abs((vertices[4] - vertices[0]) * (vertices[9] - vertices[1])
                - (vertices[8] - vertices[0]) * (vertices[5] - vertices[1])) / 2f;
    }

    private static float lineCross(float x1, float y1, float x2, float y2, float px, float py) {
        return (x2 - x1) * (py - y1) - (y2 - y1) * (px - x1);
    }

    private static void setVertex(float[] vertices, int offset, float x, float y, float u, float v) {
        vertices[offset] = x;
        vertices[offset + 1] = y;
        vertices[offset + 2] = u;
        vertices[offset + 3] = v;
    }

    private CutTri obtainTri(Texture texture, float color, float z) {
        CutTri tri = spareTris.isEmpty() ? new CutTri() : spareTris.pop();
        tri.texture = texture;
        tri.color = color;
        tri.z = z;
        return tri;
    }

    private static class CutTri {
        final float[] vertices = new float[12];
        Texture texture;
        float color;
        float z;
    }
}
