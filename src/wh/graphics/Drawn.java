//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.graphics;

import arc.Core;
import arc.func.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.*;
import arc.scene.ui.layout.Scl;
import arc.struct.*;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.Fonts;
import wh.content.WHFx;
import wh.math.WHInterp;
import wh.struct.Vec2Seq;
import wh.util.WHUtils;

import static arc.graphics.g2d.Lines.polyline;
import static mindustry.Vars.tilesize;

public final class Drawn{
    private static final FloatSeq points = new FloatSeq();
    public static final int[] oneArr = new int[]{1};
    public static final float sinScl = 1.0F;
    public static final float[] v = new float[6];
    public static final Rand rand = new Rand(0L);
    public static final Color bottomColor = Pal.gray;
    static final Vec3[] tmpV = new Vec3[4];
    static final Mat3D matT = new Mat3D();
    static final Vec2 v1 = new Vec2();
    static final Vec2 v2 = new Vec2();
    static final Vec2 v3 = new Vec2();
    static final Vec2 v6 = new Vec2();
    static final Color c1 = new Color();
    static final Color c2 = new Color();
    private static final float PERSPECTIVE_STRENGTH = 0.06f;


    private Drawn(){
    }


    public static void basicLaser(float x, float y, float x2, float y2, float stroke, float circleScl){
        Lines.stroke(stroke);
        Lines.line(x, y, x2, y2, false);
        Fill.circle(x, y, stroke * circleScl);
        Fill.circle(x2, y2, stroke * circleScl);
        Lines.stroke(1f);
    }

    public static void basicLaser(float x, float y, float x2, float y2, float stroke){
        basicLaser(x, y, x2, y2, stroke, 0.95f);
    }

    public static void teleportUnitNet(Unit before, float x, float y, float angle, Player player){
        if(!Vars.net.active() && !Vars.headless){
            before.set(x, y);
        }else{
            if(player != null){
                player.set(x, y);
                player.snapInterpolation();
                player.snapSync();
                player.lastUpdated = player.updateSpacing = 0L;
            }

            before.set(x, y);
            before.snapInterpolation();
            before.snapSync();
            before.updateSpacing = 0L;
            before.lastUpdated = 0L;
        }

        before.rotation = angle;
    }

    public static void circlePercent(float x, float y, float rad, float percent, float angle){
        float p = Mathf.clamp(percent);

        int sides = Lines.circleVertices(rad);

        float space = 360.0F / (float)sides;
        float len = 2 * rad * Mathf.sinDeg(space / 2);
        float hstep = Lines.getStroke() / 2.0F / Mathf.cosDeg(space / 2.0F);
        float r1 = rad - hstep;
        float r2 = rad + hstep;

        int i;

        for(i = 0; i < sides * p - 1; ++i){
            float a = space * (float)i + angle;
            float cos = Mathf.cosDeg(a);
            float sin = Mathf.sinDeg(a);
            float cos2 = Mathf.cosDeg(a + space);
            float sin2 = Mathf.sinDeg(a + space);
            Fill.quad(x + r1 * cos, y + r1 * sin, x + r1 * cos2, y + r1 * sin2, x + r2 * cos2, y + r2 * sin2, x + r2 * cos, y + r2 * sin);
        }

        float a = space * i + angle;
        float cos = Mathf.cosDeg(a);
        float sin = Mathf.sinDeg(a);
        float cos2 = Mathf.cosDeg(a + space);
        float sin2 = Mathf.sinDeg(a + space);
        float f = sides * p - i;
        v1.trns(a, 0, len * (f - 1));
        Fill.quad(x + r1 * cos, y + r1 * sin, x + r1 * cos2 + v1.x, y + r1 * sin2 + v1.y, x + r2 * cos2 + v1.x, y + r2 * sin2 + v1.y, x + r2 * cos, y + r2 * sin);
    }

    public static void fillCirclePercentFade(float centerX, float centerY, float x, float y, float rad, float percent, float angle, float aScl, float start, float end){
        float p = Mathf.clamp(percent);

        int sides = Lines.circleVertices(rad);

        float space = 360.0F / (float)sides;
        float len = 2 * rad * Mathf.sinDeg(space / 2);

        int i;

        v1.trns(angle, rad);

        for(i = 0; i < sides * p - 1; ++i){
            float a = space * (float)i + angle;
            float cos = Mathf.cosDeg(a);
            float sin = Mathf.sinDeg(a);
            float cos2 = Mathf.cosDeg(a + space);
            float sin2 = Mathf.sinDeg(a + space);
            Draw.alpha(Mathf.curve(i / (sides * p), start, end) * aScl);
            Fill.tri(x + rad * cos, y + rad * sin, x + rad * cos2, y + rad * sin2, centerX, centerY);
        }

        float a = space * i + angle;
        float cos = Mathf.cosDeg(a);
        float sin = Mathf.sinDeg(a);
        float cos2 = Mathf.cosDeg(a + space);
        float sin2 = Mathf.sinDeg(a + space);
        float f = sides * p - i;
        v1.trns(a, 0, len * (f - 1));
        Draw.alpha(aScl);
        Fill.tri(x + rad * cos, y + rad * sin, x + rad * cos2 + v1.x, y + rad * sin2 + v1.y, centerX, centerY);
    }

    public static void shockWave(float x, float y, float rad, float width, float percent, Color color){
        shockWave(x, y, rad, width, percent, 0, color.cpy().a(0.03f), color.cpy().a(0.8f));
    }

    public static void shockWave(float x, float y, float rad, float width, float percent, float angle, Color colorFrom, Color colorTo){
        float p = Mathf.clamp(percent);
        float currentRad = rad * p;
        float currentInnerRad = currentRad - width;

        int sides = Lines.circleVertices(currentRad);
        float space = 360.0F / sides;

        for(int i = 0; i < sides; i++){
            float a = space * i + angle;
            float cos = Mathf.cosDeg(a);
            float sin = Mathf.sinDeg(a);
            float cos2 = Mathf.cosDeg(a + space);
            float sin2 = Mathf.sinDeg(a + space);

            Color color1 = colorFrom.cpy();
            Color color2 = colorTo.cpy();

            float c1 = color1.toFloatBits(), c2 = color2.toFloatBits();

            Fill.quad(
            x + currentInnerRad * cos, y + currentInnerRad * sin, c1,
            x + currentRad * cos, y + currentRad * sin, c2,
            x + currentRad * cos2, y + currentRad * sin2, c2,
            x + currentInnerRad * cos2, y + currentInnerRad * sin2, c1
            );
        }
    }

    public static void circlePercentFlip(float x, float y, float rad, float in, float scl){
        float f = Mathf.cos(in % (scl * 3.0F), scl, 1.1F);
        circlePercent(x, y, rad, f > 0.0F ? f : -f, in + (float)(-90 * Mathf.sign(f)));
    }

    //相比于臭猫的arc,可以绘制多段弧线进度
    public static void arcProcess(float x, float y, float radius, float fraction, float rotation, int sides, float progress){
        int max = Mathf.ceil(sides * fraction);
        points.clear();
        float progressMax = Math.min(max, max * progress);

        for(int i = 0; i <= progressMax; i++){
            v6.trns((float)i / max * fraction * 360f + rotation, radius);
            float x1 = v6.x;
            float y1 = v6.y;

            v6.trns((float)(i + 1) / max * fraction * 360f + rotation, radius);

            points.add(x1 + x, y1 + y);
        }

        polyline(points, false);
    }

    public static void arcProcessFlip(float x, float y, float rad, float in, float scl){
        float f = Mathf.cos(in % (scl * 3.0F), scl, 1.1F);
        arcProcess(x, y, rad, 1, in + (float)(-90 * Mathf.sign(f)), 100, f > 0.0F ? f : -f);
    }

    public static void posSquareLink(Color color, float stroke, float size, boolean drawBottom, float x, float y, float x2, float y2){
        posSquareLink(color, stroke, size, drawBottom, v1.set(x, y), v2.set(x2, y2));
    }

    public static void posSquareLink(Color color, float stroke, float size, boolean drawBottom, Position from, Position to){
        posSquareLinkArr(color, stroke, size, drawBottom, false, from, to);
    }


    public static void posSquareLinkArr(Color color, float stroke, float size, boolean drawBottom, boolean linkLine, Position... pos){
        if(pos.length < 2 || (!linkLine && pos[0] == null)) return;

        for(int c : drawBottom ? Mathf.signs : oneArr){
            for(int i = 1; i < pos.length; i++){
                if(pos[i] == null) continue;
                Position p1 = pos[i - 1], p2 = pos[i];
                Lines.stroke(stroke + 1 - c, c == 1 ? color : bottomColor);
                if(linkLine){
                    if(p1 == null) continue;
                    Lines.line(p2.getX(), p2.getY(), p1.getX(), p1.getY());
                }else{
                    Lines.line(p2.getX(), p2.getY(), pos[0].getX(), pos[0].getY());
                }
                Draw.reset();
            }

            for(Position p : pos){
                if(p == null) continue;
                Draw.color(c == 1 ? color : bottomColor);
                Fill.square(p.getX(), p.getY(), size + 1 - c / 1.5f, 45);
                Draw.reset();
            }
        }
    }


    public static void randFadeLightningEffect(float x, float y, float range, float lightningPieceLength, Color color, boolean in){
        randFadeLightningEffectScl(x, y, range, 0.55F, 1.1F, lightningPieceLength, color, in);
    }

    public static void randFadeLightningEffectScl(float x, float y, float range, float sclMin, float sclMax, float lightningPieceLength, Color color, boolean in){
        v6.rnd(range).scl(Mathf.random(sclMin, sclMax)).add(x, y);
        (in ? WHFx.chainLightningFadeReversed : WHFx.chainLightningFade).at(x, y, lightningPieceLength, color, v6.cpy());
    }

    public static float cameraDstScl(float x, float y, float norDst){
        v6.set(Core.camera.position);
        float dst = Mathf.dst(x, y, v6.x, v6.y);
        return 1.0F - Mathf.clamp(dst / norDst);
    }

    public static float rotator_90(float in, float margin){
        return 90.0F * WHInterp.pow10.apply(Mathf.curve(in, margin, 1.0F - margin));
    }

    public static float rotator_90(){
        return 90.0F * Interp.pow5.apply(Mathf.curve(cycle_100(), 0.15F, 0.85F));
    }

    public static float rotator_120(float in, float margin){
        return 120.0F * WHInterp.pow10.apply(Mathf.curve(in, margin, 1.0F - margin));
    }

    public static float cycle(float phaseOffset, float T){
        return (Time.time + phaseOffset) % T / T;
    }

    public static float cycle(float in, float phaseOffset, float T){
        return (in + phaseOffset) % T / T;
    }

    public static float cycle_100(){
        return Time.time % 100.0F / 100.0F;
    }

    public static void surround(long id, float x, float y, float rad, int num, float innerSize, float outerSize, float interp){
        Rand rand = WHUtils.rand;

        rand.setSeed(id);
        for(int i = 0; i < num; i++){
            float len = rad * rand.random(0.75f, 1.5f);
            v1.trns(rand.random(360f) + rand.range(2f) * (1.5f - Mathf.curve(len, rad * 0.75f, rad * 1.5f)) * Time.time, len);
            float angle = v1.angle();
            v1.add(x, y);
            tri(v1.x, v1.y, ((interp + 1) * outerSize + rand.random(0, outerSize / 8)) * interp, (outerSize + 0.25f) * (Interp.exp5In.apply(interp)) / 2f, angle);
            tri(v1.x, v1.y, ((interp + 1) / 2 * innerSize + rand.random(0, innerSize / 8)) * interp, (innerSize + 0.5f) * (Interp.exp5In.apply(interp)), angle - 180);
        }
    }

    public static void drawConnected(float x, float y, float size, Color color){
        Draw.reset();
        float sin = Mathf.absin(Time.time, 8.0F, 1.25F);

        for(int i = 0; i < 4; ++i){
            float length = size / 2.0F + 3.0F + sin;
            Tmp.v1.trns((float)(i * 90), -length);
            Draw.color(Pal.gray);
            Draw.rect(Core.atlas.find("wh-linked-arrow-back"), x + Tmp.v1.x, y + Tmp.v1.y, (float)(i * 90));
            Draw.color(color);
            Draw.rect(Core.atlas.find("wh-linked-arrow"), x + Tmp.v1.x, y + Tmp.v1.y, (float)(i * 90));
        }

        Draw.reset();
    }

    public static void overlayText(String text, float x, float y, float offset, Color color, boolean underline){
        overlayText(Fonts.outline, text, x, y, offset, 1.0F, 0.25F, color, underline, false);
    }

    //Block，drawPlaceText原型
    public static void overlayText(Font font, String text, float x, float y, float offset, float offsetScl, float size, Color color, boolean underline, boolean align){
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(size / Scl.scl(1.0f));
        layout.setText(font, text);
        font.setColor(color);

        float dy = offset + 3.0F;
        font.draw(text, x, y + layout.height / (align ? 2 : 1) + (dy + 1.0F) * offsetScl, 1);
        --dy;

        if(underline){
            Lines.stroke(2.0F, Color.darkGray);
            Lines.line(x - layout.width / 2.0F - 2.0F, dy + y, x + layout.width / 2.0F + 1.5F, dy + y);
            Lines.stroke(1.0F, color);
            Lines.line(x - layout.width / 2.0F - 2.0F, dy + y, x + layout.width / 2.0F + 1.5F, dy + y);
            Draw.color();
        }

        font.setUseIntegerPositions(ints);
        font.setColor(Color.white);
        font.getData().setScale(1.0F);
        Draw.reset();
        Pools.free(layout);
    }

    public static void arrow(float x, float y, float width, float length, float backLength, float angle){
        float wx = Angles.trnsx(angle + 90.0F, width);
        float wy = Angles.trnsy(angle + 90.0F, width);
        float ox = Angles.trnsx(angle, backLength);
        float oy = Angles.trnsy(angle, backLength);
        float cx = Angles.trnsx(angle, length) + x;
        float cy = Angles.trnsy(angle, length) + y;
        Fill.tri(x + ox, y + oy, x - wx, y - wy, cx, cy);
        Fill.tri(x + ox, y + oy, x + wx, y + wy, cx, cy);
    }

    public static void tri(float x, float y, float width, float length, float angle){
        float wx = Angles.trnsx(angle + 90.0F, width);
        float wy = Angles.trnsy(angle + 90.0F, width);
        Fill.tri(
        x + wx, y + wy,
        x - wx, y - wy,
        Angles.trnsx(angle, length) + x, Angles.trnsy(angle, length) + y);
    }

    public static void fillOctagon(float x, float y, float range, float f, Color color){

        float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * range / 2 - 0.2f - f),
        w = Mathf.clamp(0.5f - f) * range;
        Draw.color(color);
        Draw.z(Layer.flyingUnitLow - 1);
        Draw.alpha(0.5f * Mathf.curve(f, 0, 0.4f));
        points.clear();
        for(int i = 0; i < 4; i++){
            points.add(x + Geometry.d4(i).x * r + Geometry.d4(i).y * w,
            y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
            if(f < 0.5f){
                points.add(x + Geometry.d4(i).x * r - Geometry.d4(i).y * w,
                y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
            }
        }
        Fill.poly(points);

        Draw.color(color.cpy().lerp(Color.white, 0.1f));
        Draw.alpha(1 - f);
        Lines.stroke(3.5f * Mathf.curve(1 - f, 0f, 0.3f));
        Lines.beginLine();
        for(int i = 0; i < 4; i++){
            Lines.linePoint(x + Geometry.d4(i).x * r + Geometry.d4(i).y * w, y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
            if(f < 0.5f)
                Lines.linePoint(x + Geometry.d4(i).x * r - Geometry.d4(i).y * w, y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
        }
        Lines.endLine(true);
    }

    public static void drawCornerTri(float x, float y, float rad, float cornerLen, float rotate, boolean line){
        drawCornerPoly(x, y, rad, cornerLen, 3, rotate, line);
    }

    public static void drawCornerPoly(float x, float y, float rad, float cornerLen, float sides, float rotate, boolean line){
        float step = 360 / sides;

        if(line) Lines.beginLine();
        for(int i = 0; i < sides; i++){
            v1.set(rad, 0).setAngle(step * i + rotate);
            v2.set(v1).rotate90(1).setLength(cornerLen);

            if(line){
                Lines.linePoint(x + v1.x - v2.x, y + v1.y - v2.y);
                Lines.linePoint(x + v1.x + v2.x, y + v1.y + v2.y);
            }else{
                Fill.tri(x, y,
                x + v1.x - v2.x, y + v1.y - v2.y,
                x + v1.x + v2.x, y + v1.y + v2.y
                );
            }
        }
        if(line) Lines.endLine(true);
    }

    public static void smoothSquareLine(float x, float y, float length, float width, Interp inp, float percent, float angle, boolean smoothCorner, Color color){
        float p = Mathf.clamp(percent);
        if(p >= 1f) return;

        int lines = smoothCorner ? 8 : 4;
        float segmentP = inp.apply(p) * lines;
        float halfLength = length / 2f;

        Vec2Seq corners = new Vec2Seq(lines);
        for(int i = 0; i < lines; i++){
            float cornerAngle = angle + i * (smoothCorner ? 45f : 90f);
            corners.add(
            x + Angles.trnsx(cornerAngle, halfLength),
            y + Angles.trnsy(cornerAngle, halfLength)
            );
        }

        for(int i = 0; i < lines; i++){

            float lineProgress = Mathf.clamp(segmentP - i);
            if(lineProgress <= 0f) continue;

            Vec2 start = corners.newVec2(i);
            Vec2 end = corners.newVec2((i + 1) % lines);

            if(smoothCorner){
                if(i % 2 == 0){
                    Vec2 actualEnd = new Vec2(
                    start.x + (end.x - start.x) * lineProgress,
                    start.y + (end.y - start.y) * lineProgress
                    );
                    Lines.stroke(width, color);
                    Lines.line(start.x, start.y, actualEnd.x, actualEnd.y);
                    Lines.stroke(width * 1 / 3f, color.cpy().lerp(Color.white, 0.2f));
                    Lines.line(start.x, start.y, actualEnd.x, actualEnd.y);
                }else{
                    float cornerAngle = angle + i * 45f + 90f;
                    Drawn.arcProcess(x, y, halfLength, 0.123f, cornerAngle, 70, lineProgress);
                    Lines.stroke(width * 1 / 3f, color.cpy().lerp(Color.white, 0.2f));
                    Drawn.arcProcess(x, y, halfLength, 0.123f, cornerAngle, 70, lineProgress);
                }
            }else{
                Vec2 actualEnd = new Vec2(
                start.x + (end.x - start.x) * lineProgress,
                start.y + (end.y - start.y) * lineProgress);
                Lines.stroke(width, color);
                Lines.line(start.x, start.y, actualEnd.x, actualEnd.y);
                Lines.stroke(width * 1 / 3f, color.cpy().lerp(Color.white, 0.2f));
                Lines.line(start.x, start.y, actualEnd.x, actualEnd.y);
            }
        }
    }


    public static void ellipse(float x, float y, int divisions, float rotation, float width, float length){
        points.clear();
        for(int i = 0; i < divisions; i++){
            float angle = 360f * i / (float)divisions;
            Tmp.v1.trnsExact(angle, width);
            point(Tmp.v1.x / width * length, Tmp.v1.y, x, y, rotation);
        }
        Fill.poly(points);
    }

    public static void ellipseProcess(float x, float y, int divisions, float rotation, float width, float length, float progress){
        points.clear();
        int progressMax = Math.min(divisions, Mathf.ceil(divisions * progress));
        for(int i = 0; i <= progressMax; i++){
            float angle = 360f * i / (float)divisions;
            Tmp.v1.trnsExact(angle, width);
            point(Tmp.v1.x / width * length, Tmp.v1.y, x, y, rotation);
        }
        polyline(points, false);
    }

    public static void superEllipseProgress(float x, float y, int divisions, float rotation, float width, float length, float progress){
        points.clear();
        int progressMax = Math.min(divisions, Mathf.ceil(divisions * progress));
        for(int i = 0; i <= progressMax; i++){
            float angle = 360f * i / (float)divisions;
            WHUtils.superEllipse(angle, width, length, 4, x, y, rotation, Tmp.v1);
            points.add(Tmp.v1.x, Tmp.v1.y);
        }
        polyline(points, false);
    }


    private static void point(float x, float y, float baseX, float baseY, float rotation){
        Tmp.v1.set(x, y).rotateRadExact(rotation * Mathf.degRad);
        points.add(Tmp.v1.x + baseX, Tmp.v1.y + baseY);
    }

    public static void drawBeam(Building build, int id, float x, float y, float rotation, float length, float strength, float beamWidth, Vec2 lastEnd, Vec2 offset, Color laserColor){
        rand.setSeed(build.id + id);
        float
        originX = x + Angles.trnsx(rotation, length),
        originY = y + Angles.trnsy(rotation, length);

        lastEnd.set(build).sub(originX, originY);
        lastEnd.setLength(Math.max(2f, lastEnd.len()));

        lastEnd.add(offset.trns(
        rand.random(360f) + Time.time / 2f,
        Mathf.sin(Time.time + rand.random(200f), 15f / Math.max(build.efficiency, 0.00001f), rand.random(0.2f * tilesize, 0.7f * tilesize))
        ).rotate(0));

        lastEnd.add(originX, originY);

        if(strength > 0.001f){
            Draw.alpha(strength);
            Lines.stroke(beamWidth * strength, laserColor);
            Lines.line(originX, originY, lastEnd.x, lastEnd.y);
            Fill.circle(lastEnd.x, lastEnd.y, 0.7f);
            Draw.color();
        }
    }

    public static void drawSine2Modifier(float x, float y, float x2, float y2, float in, float scale, float scaleSpeed, float scaleOffset, float mag, float wavelength, Floatc2 f){
        float dstTotal = Mathf.dst(x, y, x2, y2);
        float ang = Angles.angle(x, y, x2, y2);
        int dst = (int)(dstTotal / wavelength);

        Lines.beginLine();

        v1.trns(ang, 0, Mathf.sin(in + scale * scaleOffset, scale, mag)).add(x, y);
        Lines.linePoint(v1);
        f.get(v1.x, v1.y);

        for(int i = 1; i < dst; i++){
            v1.trns(ang, i * wavelength, Mathf.sin(in + scale * (scaleSpeed * i + scaleOffset), scale, mag)).add(x, y);
            Lines.linePoint(v1);
        }

        Lines.linePoint(x2, y2);
        //        Lines.linePoint(x2, y2);
        Lines.endLine(false);

    }

    //伪3d
    public static float toP3dCoord(float coord, float z3d, boolean isY){
        float cam = (isY ? Core.camera.position.y - 48f : Core.camera.position.x);
        return coord + (coord - cam) * z3d * PERSPECTIVE_STRENGTH;
    }

    //单面墙
    public static void wall(float x1, float y1, float x2, float y2,
                            float z3d, Color colorIn, Color colorOut, float z){
        float x1p = toP3dCoord(x1, z3d, false);
        float y1p = toP3dCoord(y1, z3d, true);
        float x2p = toP3dCoord(x2, z3d, false);
        float y2p = toP3dCoord(y2, z3d, true);

        Draw.z(z);
        Fill.quad(x1, y1, colorIn.toFloatBits(),
        x2, y2, colorIn.toFloatBits(),
        x2p, y2p, colorOut.toFloatBits(),
        x1p, y1p, colorOut.toFloatBits());
    }

    //立方体
    public static void room(float x, float y, float z3d,
                            float w, float h, Color colorIn, Color colorOut, float zBase){

        if(w < 0.0001f || h < 0.0001f) return;

        Tmp.c1.set(colorIn);
        Tmp.c2.set(colorOut);

        /* 4 堵墙，亮度微调 + Z 偏移 */
        wall(x - w / 2, y - h / 2, x + w / 2, y - h / 2, z3d,
        Tmp.c1.set(colorIn).mul(0.75f), Tmp.c2.set(colorOut).mul(0.75f),
        zBase + calcOff(0, x, y - h / 2));

        wall(x - w / 2, y + h / 2, x + w / 2, y + h / 2, z3d,
        Tmp.c1.set(colorIn).mul(1.2f), Tmp.c2.set(colorOut).mul(1.2f),
        zBase + calcOff(2, x, y + h / 2));

        wall(x - w / 2, y - h / 2, x - w / 2, y + h / 2, z3d,
        Tmp.c1.set(colorIn), Tmp.c2.set(colorOut),
        zBase + calcOff(3, x - w / 2, y));

        wall(x + w / 2, y - h / 2, x + w / 2, y + h / 2, z3d,
        Tmp.c1.set(colorIn), Tmp.c2.set(colorOut),
        zBase + calcOff(1, x + w / 2, y));
    }

    /* 立方体：纯色→透明 */
    public static void roomFade(float x, float y, float z3d,
                                float w, float h, Color color, float z){
        Tmp.c1.set(color);
        Tmp.c2.set(color).a(0f);
        room(x, y, z3d, w, h, Tmp.c1, Tmp.c2, z);
    }

    //圆柱
    public static void cylinder(float x, float y, float z3d,
                                float rad, Color colorIn, Color colorOut, float z){
        int sides = Lines.circleVertices(rad) * 2;
        float ang = 360f / sides;
        for(int i = 0; i < sides; i++){
            float a1 = ang * i;
            float a2 = ang * (i + 1);
            wall(x + Mathf.cosDeg(a1) * rad, y + Mathf.sinDeg(a1) * rad,
            x + Mathf.cosDeg(a2) * rad, y + Mathf.sinDeg(a2) * rad,
            z3d, colorIn, colorOut, z);
        }
    }

    /* 圆柱：纯色→透明 */
    public static void cylinderFade(float x, float y, float z3d,
                                    float rad, Color color, float z){
        Tmp.c1.set(color);
        Tmp.c2.set(color).a(0f);
        cylinder(x, y, z3d, rad, Tmp.c1, Tmp.c2, z);
    }

    //贴图3d
    public static void reg(float x, float y, float z3d,
                           TextureRegion region, float scale, float z){
        if(region == null) return;
        float w = region.width * 2f * scale / Vars.tilesize;
        float h = region.height * 2f * scale / Vars.tilesize;
        float xp = toP3dCoord(x, z3d, false);
        float yp = toP3dCoord(y, z3d, true);

        Draw.z(z);
        Draw.rect(region, xp, yp, w, h);
        Draw.z();
    }

    // 带底座的贴图
    public static void regRoom(float x, float y, float z3d,
                               TextureRegion region, float scale, float fixScale,
                               Color baseColor, float z){
        if(region == null) return;
        float w = region.width * 2f * scale / Vars.tilesize;
        float h = region.height * 2f * scale / Vars.tilesize;

        room(x, y, z3d, w, h, baseColor, baseColor, z - 1.85f); // 默认 Layer.power -1.85
        reg(x, y, z3d, region, scale * fixScale, z);
    }

    //根据方位返回 Z 偏移，避免 Z-fighting
    private static float calcOff(int index, float x, float y){
        boolean cx = x - Core.camera.position.x >= 0f;
        boolean cy = y - Core.camera.position.y + 48f >= 0f;
        if(cx && cy){
            switch(index){
                case 0:
                    return 0.0003f;
                case 1:
                    return 0.0001f;
                case 2:
                    return 0.0002f;
                case 3:
                    return 0.0004f;
            }
        }else if(!cx && cy){
            switch(index){
                case 0:
                    return 0.0003f;
                case 1:
                    return 0.0004f;
                case 2:
                    return 0.0002f;
                case 3:
                    return 0.0001f;
            }
        }else if(!cx && !cy){
            switch(index){
                case 0:
                    return 0.0002f;
                case 1:
                    return 0.0004f;
                case 2:
                    return 0.0003f;
                case 3:
                    return 0.0001f;
            }
        }else{
            switch(index){
                case 0:
                    return 0.0002f;
                case 1:
                    return 0.0001f;
                case 2:
                    return 0.0003f;
                case 3:
                    return 0.0004f;
            }
        }
        return 0;
    }

    public static void drawEnergyOrb(Vec2 pos, float fin, Color color){
        Draw.z(Layer.effect);

        float glowSize = 8f * fin + Mathf.absin(Time.time, 2f, 2f * fin);
        Draw.color(color, 0.3f);
        Fill.circle(pos.x, pos.y, glowSize);

        float coreSize = 4f * fin;
        Draw.color(color);
        Fill.circle(pos.x, pos.y, coreSize);

        Draw.color(Color.white, 0.6f);
        Fill.circle(pos.x, pos.y, coreSize * 0.6f);
    }

    public static void drawCurve(Seq<Vec2> points, Color color, float fin, float lifeProgress){
        Draw.z(Layer.effect);
        Lines.stroke(2f * fin * (1f - lifeProgress * 0.5f), color);

        for(int i = 0; i < points.size - 1; i++){
            Vec2 p1 = points.get(i);
            Vec2 p2 = points.get(i + 1);
            float dist = p1.dst(p2);
            float controlDist = dist * 0.1f;
            float angle = Angles.angle(p1.x, p1.y, p2.x, p2.y);

            Vec2 c1 = p1.cpy().lerp(p2, 0.3f).add(
            Angles.trnsx(angle + 135, Mathf.sin(Time.time / 600f + i) * controlDist * 2f),
            Angles.trnsy(angle + 135, Mathf.sin(Time.time / 600f + i) * controlDist * 2f)
            );
            Vec2 c2 = p1.cpy().lerp(p2, 0.7f).add(
            Angles.trnsx(angle - 135, Mathf.cos(Time.time / 600f + i) * controlDist * 2f),
            Angles.trnsy(angle - 135, Mathf.cos(Time.time / 600f + i) * controlDist * 2f)
            );

            Lines.curve(p1.x, p1.y, c1.x, c1.y, c2.x, c2.y, p2.x, p2.y, (int)(dist / 4f));
        }
    }

}
