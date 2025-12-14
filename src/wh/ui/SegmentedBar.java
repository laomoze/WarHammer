package wh.ui;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.util.pooling.*;
import mindustry.gen.*;
import mindustry.ui.*;

public class SegmentedBar extends Bar{
    public float segmentMount = 8;
    public float segmentSpacing = 3;

    private static Rect scissor = new Rect();

    private Floatp fraction;
    private CharSequence name = "";
    private float value, lastValue, blink, outlineRadius;
    private Color blinkColor = new Color(), outlineColor = new Color();

    public SegmentedBar(){
        super();
    }

    public SegmentedBar(Prov<CharSequence> name, float segmentMount, Prov<Color> color, Floatp fraction){
        this.fraction = fraction;
        this.segmentMount = segmentMount;
        setColor(color.get());
        lastValue = value = Mathf.clamp(fraction.get());
        update(() -> {
            this.name = name.get();
            this.blinkColor.set(color.get());
            setColor(color.get());
        });
    }

    @Override
    public void draw(){
        if(fraction == null) return;

        float computed = Mathf.clamp(fraction.get());


        if(lastValue > computed){
            blink = 1f;
            lastValue = computed;
        }

        if(Float.isNaN(lastValue)) lastValue = 0;
        if(Float.isInfinite(lastValue)) lastValue = 1f;
        if(Float.isNaN(value)) value = 0;
        if(Float.isInfinite(value)) value = 1f;
        if(Float.isNaN(computed)) computed = 0;
        if(Float.isInfinite(computed)) computed = 1f;

        blink = Mathf.lerpDelta(blink, 0f, 0.2f);
        value = Mathf.lerpDelta(value, computed, 0.15f);

        Drawable bar = Tex.bar;

        if(outlineRadius > 0){
            Draw.color(outlineColor);
            bar.draw(x - outlineRadius, y - outlineRadius, width + outlineRadius * 2, height + outlineRadius * 2);
        }

        Draw.colorl(0.1f);
        Draw.alpha(parentAlpha);
        bar.draw(x, y, width, height);
        Draw.color(color, blinkColor, blink);
        Draw.alpha(parentAlpha);

       /* Drawable top = Tex.barTop;
        float topWidth = width * value;

        if(topWidth > Core.atlas.find("bar-top").width){
            top.draw(x, y, topWidth, height);
        }else{
            if(ScissorStack.push(scissor.set(x, y, topWidth, height))){
                top.draw(x, y, Core.atlas.find("bar-top").width, height);
                ScissorStack.pop();
            }
        }*/
        Drawable top = Tex.barTop;
        float totalGapWidth = segmentSpacing * (segmentMount - 1);
        float segmentWidth = (width - totalGapWidth) / segmentMount;

        int filledSegments = (int)(value * segmentMount);
        float partialFill = (value * segmentMount) - filledSegments;


        for(int i = 0; i < filledSegments; i++){
            float segmentX = x + i * (segmentWidth + segmentSpacing);
            top.draw(segmentX + segmentWidth / 2, y, segmentWidth, height);
        }

        if(partialFill > 0 && filledSegments < segmentMount){
            float segmentX = x + filledSegments * (segmentWidth + segmentSpacing);
            float partialWidth = segmentWidth * partialFill;

            if(partialWidth > Core.atlas.find("bar-top").width){
                top.draw(segmentX + segmentWidth / 2, y, segmentWidth, height);
            }else{
                if(ScissorStack.push(scissor.set(segmentX, y, partialWidth, height))){
                    top.draw(x, y, segmentX + Core.atlas.find("bar-top").width, height);
                    ScissorStack.pop();
                }
            }
        }

        Draw.color();

        Font font = Fonts.outline;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, name);

        font.setColor(1f, 1f, 1f, 1f);
        font.getCache().clear();
        font.getCache().addText(name, x + width / 2f - lay.width / 2f, y + height / 2f + lay.height / 2f + 1);
        font.getCache().draw(parentAlpha);

        Pools.free(lay);
    }

}
