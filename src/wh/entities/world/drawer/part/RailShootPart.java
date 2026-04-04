package wh.entities.world.drawer.part;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;
import wh.core.*;

public class RailShootPart extends DrawPart{
    public float x, y, rotation;

    public float length = 120f;
    public float width = 3f;     // line stroke scale
    public float spacing = 30f;
    public float arrowWidthScl = 1.3f;
    public float arrowHeightScl = 1;
    public float lineGap = 3.4f;
    public float arrowWavePeriod = 16;
    public float arrowWaveTimeScl = 16f;
    public boolean arrowOffset = true;

    public float layer = Layer.effect;
    public float layerOffset = 0f;

    public float opacity = 1f;
    public Blending blending = Blending.normal;
    public Color color = Color.white;

    public PartProgress progress = PartProgress.warmup;
    public PartProgress alpha = PartProgress.warmup;

    public String arrowName;
    public String name;
    public TextureRegion arrowRegion;

    public RailShootPart(){
    }

    public RailShootPart(String arrowName){
        this.arrowName = arrowName;
    }

    @Override
    public void load(String blockName){
        super.load(blockName);
        String realName = this.name == null ? blockName + arrowName : this.name;
        if(arrowName != null){
            arrowRegion = Core.atlas.find(realName);
        }else arrowRegion = Core.atlas.find(WarHammerMod.name("jump-gate-arrow"));

    }

    @Override
    public void draw(PartParams params){
        float z = Draw.z();
        if(layer > 0f) Draw.z(layer);
        if(under && turretShading) Draw.z(z - 0.0001f);
        Draw.z(Draw.z() + layerOffset);

        float fin = Mathf.clamp(progress.get(params));

        float rot = params.rotation + rotation;

        Tmp.v1.trns(params.rotation - 90f, x, y);
        float rx = params.x + Tmp.v1.x;
        float ry = params.y + Tmp.v1.y;

        Draw.color(color);
        float baseAlpha = opacity * alpha.getClamp(params);
        Draw.alpha(baseAlpha);
        Draw.blend(blending);

        float step = Math.max(spacing, 0.001f);
        int arrows = Math.max(1, Mathf.ceil(length / step));

        for(int i = 0; i <= arrows; i++){
            float dist = (arrowOffset ? 0.5f * step : 0f) + i * step;
            Tmp.v2.trns(rot, dist);

            float seg = Mathf.clamp((fin * length - dist) / step);
            float f = Interp.pow3Out.apply(seg) * (0.6f * Mathf.curve(fin, 0f, 0.25f) + fin * 0.4f);

            float reveal = Interp.pow2Out.apply(Mathf.clamp(fin * (arrows + 1f) - i));
            float wave = Mathf.absin(i * arrowWaveTimeScl - Time.time, arrowWavePeriod, 1f);
            float arrowAlpha = baseAlpha * reveal * wave;

            Draw.alpha(arrowAlpha);

            Draw.rect(
            arrowRegion,
            rx + Tmp.v2.x, ry + Tmp.v2.y,
            arrowRegion.width * Draw.scl * f * arrowWidthScl,
            arrowRegion.height * Draw.scl * f * arrowHeightScl,
            rot - 90f
            );
        }

        Draw.alpha(baseAlpha);

        Tmp.v3.trns(rot, 0f, (lineGap - fin) * Vars.tilesize);

        if(width > 0){
            Lines.stroke(fin * width * 0.7f + 0.3f * width * Mathf.absin(Time.time, arrowWavePeriod, 1f));
            float lineLen = length * (0.5f + fin / 2f)
            * Mathf.curve(Interp.pow5Out.apply(fin), 0f, 0.2f);

            for(int sign : Mathf.signs){
                Lines.lineAngle(rx + Tmp.v3.x * sign, ry + Tmp.v3.y * sign, rot, lineLen);
            }
        }

        Draw.blend();
        Draw.reset();
        Draw.z(z);
    }
}
