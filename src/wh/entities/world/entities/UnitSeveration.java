package wh.entities.world.entities;

import arc.audio.Sound;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Intersector;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import wh.entities.WHBaseEntity;
import wh.graphics.WHShaders;

import static arc.math.geom.Geometry.triangleArea;

/**
 * 单位贴图的一块可切割、可飞散的碎片。
 */
public class UnitSeveration extends WHBaseEntity {
    // 切三角形时复用的临时容器，避免频繁分配。
    private static final FloatSeq intersections = new FloatSeq();
    private static final FloatSeq splitSide1 = new FloatSeq();
    private static final FloatSeq splitSide2 = new FloatSeq();
    private static final Seq<CutTri> returnTri = new Seq<>();
    private static final Seq<CutTri> tmpTris = new Seq<>();
    private static final Seq<CutTri> tmpUncutTris = new Seq<>();
    private static final Seq<UnitSeveration> tmpCuts = new Seq<>();
    private static final Vec2 tmpVec = new Vec2();
    private static final Vec2 tmpVec2 = new Vec2();
    private static final float[] tmpVerts = new float[24];
    private static final float minArea = 16f;

    // 碎片当前由哪些局部坐标三角形组成。
    public final Seq<CutTri> tris = new Seq<>();
    public final TextureRegion region = new TextureRegion();

    public float bounds, area; // 用于裁剪范围、最小碎片判断和特效大小。
    public float centerX, centerY;
    public float rotation;
    public float width, height;

    public float color = Color.whiteFloatBits;
    public float z = Layer.flyingUnit;
    public Effect explosionEffect = Fx.none;
    public Sound explosionSound = Sounds.none;

    public float time;
    public float lifetime = 3f * 60f;
    public float vx, vy, vr;
    public float drag = 0.05f;

    public static UnitSeveration generate(TextureRegion region, float x, float y, float width, float height, float rotation) {
        UnitSeveration severation = new UnitSeveration();
        severation.region.set(region);
        severation.width = width;
        severation.height = height;
        severation.rotation = rotation;
        severation.x = x;
        severation.y = y;
        severation.drawSize = Math.max(Math.abs(width), Math.abs(height));

        // 初始整张矩形贴图由两个三角形组成，后续切割只操作这些三角形。
        for (int i = 0; i < 2; i++) {
            CutTri tri = new CutTri();
            float[] p = tri.pos;
            p[0] = i == 0 ? 0f : 1f;
            p[1] = i == 0 ? 0f : 1f;
            p[2] = 1f;
            p[3] = 0f;
            p[4] = 0f;
            p[5] = 1f;
            severation.tris.add(tri);
        }

        severation.updateBounds();
        severation.add();
        return severation;
    }

    public void cutWorld(float x1, float y1, float x2, float y2, Cons<UnitSeveration> force) {
        cutWorldResult(x1, y1, x2, y2, force);
    }

    public Seq<UnitSeveration> cutWorldResult(float x1, float y1, float x2, float y2, Cons<UnitSeveration> force) {
        Seq<UnitSeveration> result = new Seq<>();
        if (!added || area < minArea) return result;

        if (force == null) {
            // 默认让两半沿切线两侧分开，并附加相反方向的旋转。
            float fx1 = x1, fy1 = y1, fx2 = x2, fy2 = y2;
            force = cut -> {
                Vec2 nearest = Intersector.nearestSegmentPoint(fx1, fy1, fx2, fy2, cut.x, cut.y, tmpVec2);
                int side = lineSide(fx1, fy1, fx2, fy2, cut.x, cut.y);
                float dst = nearest.dst(cut.x, cut.y);
                nearest.sub(cut.x, cut.y).scl(-0.125f).limit(3f);
                cut.vx /= 1.5f;
                cut.vy /= 1.5f;
                cut.vr /= 1.5f;
                cut.vx += nearest.x;
                cut.vy += nearest.y;
                cut.vr += (-5f * side) / (1f + dst / 5f);
            };
        }

        // 将世界坐标切线转换为碎片的归一化局部坐标。
        tmpVec.set(x1, y1).sub(x, y).rotate(-rotation);
        float lx1 = tmpVec.x / width + centerX;
        float ly1 = tmpVec.y / height + centerY;
        tmpVec.set(x2, y2).sub(x, y).rotate(-rotation);
        float lx2 = tmpVec.x / width + centerX;
        float ly2 = tmpVec.y / height + centerY;

        Seq<UnitSeveration> cuts = cut(lx1, ly1, lx2, ly2);
        if (cuts.isEmpty()) return result;

        for (UnitSeveration cut : cuts) {
            cut.explosionEffect = explosionEffect;
            cut.explosionSound = explosionSound;

            // 子碎片中心不同，需要换算回正确的世界坐标。
            float dx = cut.centerX - centerX;
            float dy = cut.centerY - centerY;
            tmpVec.set(dx, dy).scl(width, height).rotate(rotation).add(x, y);

            cut.rotation = rotation;
            cut.x = tmpVec.x;
            cut.y = tmpVec.y;
            cut.vx += vx;
            cut.vy += vy;

            force.get(cut);
            cut.add();
            result.add(cut);
        }

        // 原碎片已被两个新碎片替代。
        remove();
        return result;
    }

    @Override
    public void update() {
        // 碎片按速度飞行，并通过阻力逐渐减速。
        x += vx * Time.delta;
        y += vy * Time.delta;
        rotation += vr * Time.delta;

        vx *= 1f - drag * Time.delta;
        vy *= 1f - drag * Time.delta;
        vr *= 1f - drag * Time.delta;

        if (time >= lifetime) {
            // 生命周期结束时播放配置的特效和声音。
            float size = Mathf.sqrt(area / 4f);
            if (explosionEffect != Fx.none) explosionEffect.at(x, y, size);
            if (explosionSound != Sounds.none) {
                explosionSound.at(x, y, Mathf.random(0.9f, 1.1f), Mathf.clamp(size / 1.1f));
            }
            remove();
        }

        time += Time.delta * (area < minArea ? 2f : 1f);
    }

    @Override
    public void draw() {
        float sin = Mathf.sinDeg(rotation);
        float cos = Mathf.cosDeg(rotation);
        float packedColor = color;
        float mixColor = Color.clearFloatBits;
        float previousZ = Draw.z();
        boolean useShader = WHShaders.alphaCut != null;

        // 使用 alphaCut，避免裁成三角形后透明边缘出现异常。
        if (useShader) {
            Draw.flush();
            Draw.shader(WHShaders.alphaCut);
        }

        for (CutTri tri : tris) {
            float[] pos = tri.pos;
            int vertI = 0;

            // Draw.vert 需要四个顶点；最后一个顶点重复三角形的第一个顶点。
            for (int i = 0; i < 8; i += 2) {
                int idx = Math.min(i, 4);
                float vx = (pos[idx] - centerX) * width;
                float vy = (pos[idx + 1] - centerY) * height;
                float tx = (vx * cos - vy * sin) + x;
                float ty = (vx * sin + vy * cos) + y;

                tmpVerts[vertI] = tx;
                tmpVerts[vertI + 1] = ty;
                tmpVerts[vertI + 2] = packedColor;
                tmpVerts[vertI + 3] = Mathf.lerp(region.u, region.u2, pos[idx]);
                tmpVerts[vertI + 4] = Mathf.lerp(region.v2, region.v, pos[idx + 1]);
                tmpVerts[vertI + 5] = mixColor;
                vertI += 6;
            }

            Draw.z(z);
            Draw.vert(region.texture, tmpVerts, 0, 24);
        }

        if (useShader) {
            Draw.flush();
            Draw.shader();
        }

        Draw.z(previousZ);
        Draw.color();
    }

    @Override
    public float clipSize() {
        return bounds * 2f;
    }

    public void hitbox(Rect out) {
        out.setCentered(x, y, bounds);
    }

    @Override
    public boolean serialize() {
        return false;
    }

    private Seq<UnitSeveration> cut(float x1, float y1, float x2, float y2) {
        tmpTris.clear();
        tmpUncutTris.clear();
        tmpCuts.clear();
        boolean hasCut = false;

        // 先拆分被切线穿过的三角形，未被穿过的三角形稍后归到同一侧。
        for (CutTri tri : tris) {
            Seq<CutTri> split = tri.cut(x1, y1, x2, y2);
            if (!split.isEmpty()) {
                hasCut = true;
                tmpTris.addAll(split);
            } else {
                tmpUncutTris.add(tri);
            }
        }

        if (!hasCut) return tmpCuts;

        // 切线两侧各生成一个新的碎片。
        UnitSeveration sideA = new UnitSeveration();
        UnitSeveration sideB = new UnitSeveration();

        sideA.region.set(region);
        sideB.region.set(region);
        sideA.width = sideB.width = width;
        sideA.height = sideB.height = height;
        sideA.z = sideB.z = z;
        sideA.time = sideB.time = time / 2f;
        sideA.color = sideB.color = color;
        sideA.drawSize = sideB.drawSize = drawSize;
        sideA.explosionEffect = sideB.explosionEffect = explosionEffect;
        sideA.explosionSound = sideB.explosionSound = explosionSound;
        sideA.lifetime = 3f * 60f + Mathf.range(15f);
        sideB.lifetime = 3f * 60f + Mathf.range(15f);

        for (CutTri tri : tmpTris) {
            if (tri.side == 0) {
                sideA.tris.add(tri);
            } else {
                sideB.tris.add(tri);
            }
        }

        // 完整三角形按其所在切线一侧归类。
        for (CutTri tri : tmpUncutTris) {
            float[] ps = tri.pos;
            int side = 0;
            for (int i = 0; i < 6; i += 2) {
                side += lineSide(x1, y1, x2, y2, ps[i], ps[i + 1]);
            }

            if (side >= 0) {
                sideA.tris.add(tri);
            } else {
                sideB.tris.add(tri);
            }
        }

        sideA.updateBounds();
        sideB.updateBounds();

        // 丢弃过小的块，防止生成细小碎屑。
        if (sideA.area >= minArea) tmpCuts.add(sideA);
        if (sideB.area >= minArea) tmpCuts.add(sideB);
        return tmpCuts;
    }

    private void updateBounds() {
        // 从所有局部顶点重新计算中心、面积和包围尺寸。
        float maxW = 0f, minW = 1f;
        float maxH = 0f, minH = 1f;
        float sumX = 0f, sumY = 0f;
        int count = 0;
        area = 0f;

        for (CutTri tri : tris) {
            for (int i = 0; i < tri.pos.length; i += 2) {
                float px = tri.pos[i];
                float py = tri.pos[i + 1];
                maxW = Math.max(px, maxW);
                minW = Math.min(px, minW);
                maxH = Math.max(py, maxH);
                minH = Math.min(py, minH);
                sumX += px;
                sumY += py;
                count++;
            }

            float[] p = tri.pos;
            area += triangleArea(p[0] * width, p[1] * height, p[2] * width, p[3] * height, p[4] * width, p[5] * height);
        }

        centerX = count == 0 ? 0.5f : sumX / count;
        centerY = count == 0 ? 0.5f : sumY / count;
        bounds = Math.max((maxW - minW) * Math.abs(width), (maxH - minH) * Math.abs(height));
    }

    private static int lineSide(float x1, float y1, float x2, float y2, float px, float py) {
        // 用叉积判断点在有向切线的左侧、右侧还是线上。
        float cross = (x2 - x1) * (py - y1) - (y2 - y1) * (px - x1);
        return cross > 0f ? 1 : (cross < 0f ? -1 : 0);
    }

    static class CutTri {
        // 三个顶点的归一化局部坐标：x1, y1, x2, y2, x3, y3。
        final float[] pos = new float[6];
        int side;

        Seq<CutTri> cut(float x1, float y1, float x2, float y2) {
            intersections.clear();
            splitSide1.clear();
            splitSide2.clear();
            returnTri.clear();

            // 收集切线与三角形三条边的交点。
            for (int i = 0; i < 3; i++) {
                int i1 = i * 2;
                int i2 = ((i + 1) % 3) * 2;

                float lx1 = pos[i1], ly1 = pos[i1 + 1];
                float lx2 = pos[i2], ly2 = pos[i2 + 1];
                if (Intersector.intersectSegments(lx1, ly1, lx2, ly2, x1, y1, x2, y2, tmpVec)) {
                    intersections.add(tmpVec.x, tmpVec.y);
                }
            }

            // 只有恰好穿过两条边时，才算有效切割。
            if (intersections.size != 4) return returnTri;

            int within = 0;
            for (int i = 0; i < 6; i += 2) {
                float sx = pos[i], sy = pos[i + 1];
                int side = lineSide(x1, y1, x2, y2, sx, sy);
                if (side >= 0) {
                    splitSide1.add(sx, sy);
                } else {
                    splitSide2.add(sx, sy);
                }
                if (side == 0) within++;
            }

            if (splitSide1.isEmpty() || splitSide2.isEmpty() || within >= 2) return returnTri;

            // 分别拼出切线两侧的多边形，再拆回三角形。
            for (int s = 0; s < 2; s++) {
                FloatSeq side = s == 0 ? splitSide1 : splitSide2;

                if (side.size <= 2) {
                    side.add(intersections.items[0], intersections.items[1]);
                    side.add(intersections.items[2], intersections.items[3]);
                } else {
                    int last = side.size - 2;
                    float fx = side.items[0], fy = side.items[1];
                    float lx = side.items[last], ly = side.items[last + 1];
                    float px1 = intersections.items[0], py1 = intersections.items[1];
                    float px2 = intersections.items[2], py2 = intersections.items[3];
                    int bias = 0;

                    for (int i = 0; i < 4; i += 2) {
                        float dx = intersections.items[i], dy = intersections.items[i + 1];
                        boolean intersect1 = Intersector.intersectSegments(lx, ly, dx, dy, fx, fy, px1, py1, null);
                        boolean intersect2 = Intersector.intersectSegments(lx, ly, dx, dy, fx, fy, px2, py2, null);
                        if ((dx != px1 || dy != py1) && intersect1) bias++;
                        if ((dx != px2 || dy != py2) && intersect2) bias--;
                    }

                    if (bias >= 0) {
                        side.add(px1, py1);
                        side.add(px2, py2);
                    } else {
                        side.add(px2, py2);
                        side.add(px1, py1);
                    }
                }

                if (side.size <= 6) {
                    float[] items = side.items;
                    addTriangle(items[0], items[1], items[2], items[3], items[4], items[5], s);
                } else {
                    float[][] tris = triangulate(side.items, side.size / 2);
                    for (float[] tri : tris) {
                        addTriangle(tri[0], tri[1], tri[2], tri[3], tri[4], tri[5], s);
                    }
                }
            }

            return returnTri;
        }

        private void addTriangle(float x1, float y1, float x2, float y2, float x3, float y3, int side) {
            // 忽略面积为零的退化三角形。
            float area = triangleArea(x1, y1, x2, y2, x3, y3);
            if (area <= 0f) return;

            CutTri tri = new CutTri();
            tri.side = side;
            tri.pos[0] = x1;
            tri.pos[1] = y1;
            tri.pos[2] = x2;
            tri.pos[3] = y2;
            tri.pos[4] = x3;
            tri.pos[5] = y3;
            returnTri.add(tri);
        }

        private float[][] triangulate(float[] arr, int size) {
            // 用扇形三角化将凸多边形拆分为三角形。
            float[][] ret = new float[size - 2][6];
            for (int i = 0; i < size - 2; i++) {
                float[] tri = ret[i];
                int id = i * 2;
                tri[0] = arr[0];
                tri[1] = arr[1];
                tri[2] = arr[id + 2];
                tri[3] = arr[id + 3];
                tri[4] = arr[id + 4];
                tri[5] = arr[id + 5];
            }
            return ret;
        }
    }
}
