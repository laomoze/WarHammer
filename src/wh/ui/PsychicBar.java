package wh.ui;

import arc.Core;
import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.ScissorStack;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Scl;
import arc.util.pooling.Pools;
import mindustry.gen.Tex;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;

public class PsychicBar extends Bar {
    private static final Rect scissor = new Rect();

    private final Prov<CharSequence> nameProv;
    private final Prov<Color> colorProv;
    private final Floatp fraction;
    private final Color blinkColor = new Color();

    private float value;
    private float lastValue;
    private float blink;

    public PsychicBar(Prov<CharSequence> name, Prov<Color> color, Floatp fraction) {
        super(name, color, fraction);
        this.nameProv = name;
        this.colorProv = color;
        this.fraction = fraction;
        this.lastValue = this.value = Mathf.clamp(fraction.get());
    }

    @Override
    public void draw() {
        if (fraction == null) return;

        float computed = Mathf.clamp(fraction.get());
        if (lastValue > computed) {
            blink = 1f;
            lastValue = computed;
        }

        if (Float.isNaN(lastValue) || Float.isInfinite(lastValue)) lastValue = 0f;
        if (Float.isNaN(value) || Float.isInfinite(value)) value = 0f;
        if (Float.isNaN(computed) || Float.isInfinite(computed)) computed = 0f;

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.15f);

        Drawable bar = Tex.bar;
        Draw.colorl(0.1f);
        Draw.alpha(parentAlpha);
        bar.draw(x, y, width, height);

        Color barColor = colorProv.get();
        blinkColor.set(barColor);
        Draw.color(barColor, blinkColor, blink);
        Draw.alpha(parentAlpha);

        Drawable top = Tex.barTop;
        float topWidth = width * value;
        float textureWidth = Core.atlas.find("bar-top").width;
        if (topWidth > textureWidth) {
            top.draw(x, y, topWidth, height);
        } else if (ScissorStack.push(scissor.set(x, y, topWidth, height))) {
            top.draw(x, y, textureWidth, height);
            ScissorStack.pop();
        }

        drawNameWithIcon(nameProv.get());
        Draw.reset();
    }

    private void drawNameWithIcon(CharSequence name) {
        Font font = Fonts.outline;
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        layout.setText(font, name);

        float iconSize = Math.max(height * 0.95f, Scl.scl(18f));
        float gap = Scl.scl(4f);
        float totalWidth = iconSize + gap + layout.width;
        float iconX = x + width / 2f - totalWidth / 2f + iconSize / 2f;
        float textX = iconX + iconSize / 2f + gap;
        float centerY = y + height / 2f;

        Draw.color(Color.white);
        Draw.alpha(parentAlpha);
        Draw.rect(PsychicImage.region(), iconX, centerY, iconSize, iconSize);

        font.setColor(1f, 1f, 1f, 1f);
        font.getCache().clear();
        font.getCache().addText(name, textX, centerY + layout.height / 2f + 1f);
        font.getCache().draw(parentAlpha);

        Pools.free(layout);
    }
}
