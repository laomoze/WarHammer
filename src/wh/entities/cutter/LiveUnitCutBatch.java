package wh.entities.cutter;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.GL20;
import arc.graphics.Texture;
import arc.graphics.g2d.Batch;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.Shader;
import arc.mock.MockGL20;
import mindustry.gen.Unit;

/**
 * 截获当前帧绘制并提交两侧切面。
 */
final class LiveUnitCutBatch extends Batch {
    private static final MockGL20 mockGl = new MockGL20();

    private final UnitSeveration composite = UnitSeveration.begin(0f, 0f);

    void captureAndDraw(Unit unit, Runnable draw, float x1, float y1, float x2, float y2,
                        float separation, float openingAngle) {
        Batch previousBatch = Core.batch;
        GL20 previousGl = Core.gl;
        boolean previousLegacyLines = Lines.useLegacyLine;
        Blending previousBlending = Draw.getBlend();
        float previousColor = Draw.getColorPacked();
        float previousMixColor = Draw.getMixColorPacked();
        float previousZ = Draw.z();

        composite.resetFrameCapture(unit.x, unit.y, unit.bounds());
        Core.batch = this;
        Core.gl = mockGl;
        Lines.useLegacyLine = true;
        setPackedColor(previousColor);
        setPackedMixColor(previousMixColor);
        setBlending(previousBlending);
        z(previousZ);

        try {
            draw.run();
        } finally {
            Lines.useLegacyLine = previousLegacyLines;
            Core.batch = previousBatch;
            Core.gl = previousGl;
            Draw.color(previousColor);
            Draw.mixcol(previousMixColor);
            Draw.blend(previousBlending);
            Draw.z(previousZ);
        }

        if (!composite.empty()) composite.drawClipped(x1, y1, x2, y2, separation, openingAngle);
    }

    protected void setMixColor(Color tint) {
    }

    protected void setMixColor(float r, float g, float b, float a) {
    }

    @Override
    protected void setPackedMixColor(float packedColor) {
    }

    @Override
    protected void draw(Texture texture, float[] spriteVertices, int offset, int count) {
    }

    @Override
    protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation) {
        if (composite == null || blending != Blending.normal || region == Core.atlas.white() || !region.found()) return;
        composite.addQuad(region, x, y, originX, originY, width, height, rotation, colorPacked, z);
    }

    @Override
    protected void flush() {
    }

    @Override
    protected void setShader(Shader shader, boolean apply) {
    }
}
