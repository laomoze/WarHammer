package wh.entities.world.entities;

import arc.Core;
import arc.audio.Sound;
import arc.func.Cons;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.GL20;
import arc.graphics.Texture;
import arc.graphics.g2d.Batch;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.entities.Effect;

public class UnitCutBatch extends Batch {
    public Effect explosionEffect;
    public Cons<UnitSeveration> cutHandler;
    public Sound sound;
    private static final Seq<UnitSeveration> returnEntities = new Seq<>();

    public Seq<UnitSeveration> switchBatch(Runnable run) {
        Batch last = Core.batch;
        GL20 lastGl = Core.gl;

        Core.batch = this;
        Lines.useLegacyLine = true;
        returnEntities.clear();

        try {
            run.run();
        } finally {
            Lines.useLegacyLine = false;
            Core.batch = last;
            Core.gl = lastGl;
            explosionEffect = null;
            cutHandler = null;
            sound = null;
        }

        return returnEntities;
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
        if (blending != Blending.normal || region == Core.atlas.white() || !region.found()) {
            return;
        }

        float midX = width / 2f;
        float midY = height / 2f;

        float cos = Mathf.cosDeg(rotation);
        float sin = Mathf.sinDeg(rotation);
        float dx = midX - originX;
        float dy = midY - originY;

        float bx = (cos * dx - sin * dy) + (x + originX);
        float by = (sin * dx + cos * dy) + (y + originY);

        UnitSeveration severation = UnitSeveration.generate(region, bx, by, width, height, rotation);
        severation.color = colorPacked;
        severation.z = z;
        if (sound != null) severation.explosionSound = sound;
        if (explosionEffect != null) severation.explosionEffect = explosionEffect;
        if (cutHandler != null) cutHandler.get(severation);
        returnEntities.add(severation);
    }

    @Override
    protected void flush() {
    }

    @Override
    protected void setShader(Shader shader, boolean apply) {
    }
}
