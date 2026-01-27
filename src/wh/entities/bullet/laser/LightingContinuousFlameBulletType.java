package wh.entities.bullet.laser;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.graphics.*;

public class LightingContinuousFlameBulletType extends ContinuousFlameBulletType{
    public boolean drawLightning = false;
    public float waveLength = 8;

    public LightingContinuousFlameBulletType(){
        super();
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);
        float mult = b.fin(lengthInterp);
        float realLength = Damage.findLength(b, length * mult, laserAbsorb, pierceCap);
        if(drawLightning && lightningColor != null && b.timer.get(2, 5)){
            PositionLightning.createEffect(b, Tmp.v2.trns(b.rotation(), realLength).add(b), lightningColor, 2, Mathf.random(2, 3));
        }

        Draw.z(Layer.bullet + 0.001f);
        Tmp.v1.trns(Time.time * 0.2f, width / 2).add(b);
        Tmp.v2.trns(b.rotation(), realLength * 0.6f).add(b);
        Lines.stroke(width / 2);
        Draw.color(hitColor);
        Fill.circle(Tmp.v1.x, Tmp.v1.y, Lines.getStroke());
        Drawn.drawFinSine2Modifier(
        Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, 6f, Time.time * 0.4f, 0.6f, 0, width * 2, waveLength,
        ((x1, y1) -> {
            Fill.circle(x1, y1, Lines.getStroke());
        }));
    }
}
