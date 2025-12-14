package wh.entities.world.drawer.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.*;
import mindustry.gen.Unit;
import mindustry.graphics.*;
import mindustry.type.UnitType;
import wh.content.WHFx;
import wh.util.WHUtils;

public class WHUnitEngine extends UnitType.UnitEngine{
    public boolean line = false;
    public float baseLength;
    public float maxLength;
    public int divisions = 14;
    public int startParticles = 10;
    public int particlesMultiple = 3;

    public WHUnitEngine(float x, float y, float length, float lengthMax, float width){
        super(x, y, width, -90);
        this.baseLength = length;
        this.maxLength = lengthMax;
    }


    //length, radius,pan,alpha
    public float[] lengthradiusPans = {
    1.25f, 1f, 0.32f, 0.5f,
    1.05f, 1f, 0.28f, 0.58f,
    0.8f, 0.9f, 0.22f, 0.66f,
    0.65f, 0.8f, 0.2f, 0.78f,
    0.5f, 0.7f, 0.15f, 0.84f,
    0.4f, 0.6f, 0.1f, 0.9f,
    0.3f, 0.2f, 0.05f, 1f,
    };

    @Override
    public void draw(Unit unit){
        UnitType type = unit.type;
        /* Draw.z(type.engineLayer > 0? type.engineLayer : Layer.effect);*/
        float sin = Mathf.absin(Time.time, 2f, 0.1f);
        float rot = unit.rotation - 90;
        Color color = type.engineColor == null ? unit.team.color : type.engineColor;

        Tmp.v1.trns(rot, x, y);
        float ex = Tmp.v1.x + unit.x, ey = Tmp.v1.y + unit.y;
        float realLength;
        realLength = baseLength + Math.abs((maxLength - baseLength) *
        Interp.smoother.apply(Mathf.clamp(unit.vel.len() / unit.type.speed, 0, 1)));

        for(int i = 0; i < lengthradiusPans.length / 4; i++){
            float alphaWave = Mathf.absin(4f, i * 0.55f) * 0.25f + 0.66f;
            float baseAlpha = lengthradiusPans[i * 4 + 3];
            Draw.color(color.cpy().a(baseAlpha * alphaWave));
            Drawf.flame(ex, ey,
            divisions,
            unit.rotation() - 180,
            realLength * lengthradiusPans[i * 4] * (1f - sin),
            radius * lengthradiusPans[i * 4 + 1] * (1f + sin),
            lengthradiusPans[i * 4 + 2]
            );
        }

        Draw.color(color.cpy().a((1-sin)));
        Drawf.flame(ex, ey,
                divisions * 2,
                unit.rotation() - 180,
                realLength * 0.35f* (1f - sin) ,
        radius * 2f * (1f + sin),
                0.3f);

        if(!unit.moving()) return;

        if(line){
            float particleLife = 74f;
            float particleLen = 7.5f;
            Rand rand = WHUtils.rand((long)(unit.id+99999+x+y));

            float progress = Mathf.clamp((realLength - baseLength) / (maxLength - baseLength), 0, 1);
            int particlesMult = (int)(1 + particlesMultiple * progress);

            float base = (Time.time / particleLife);
            for(int i = 0; i < startParticles * particlesMult; i++){
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin, fslope = WHFx.fslope(fin);
                float len = rand.random(particleLen * 0.7f, particleLen * 1.3f) * Mathf.curve(fin, 0.2f, 0.9f);
                float centerDeg = rand.random(Mathf.pi);
                Tmp.v2.trns(unit.rotation-180, Interp.pow3In.apply(fin) *  rand.random(0,realLength*1.3f ) + rand.range(11) - 8,
                (((rand.random(0, radius * 3) * (fout + 1) / 2 + 2) / (3 * fin / 7 + 1.3f) - 1) + rand.random(-radius * 1.2f, radius * 1.2f)) * Mathf.cos(centerDeg));
                float angle = Mathf.slerp(Tmp.v1.angle() , unit.rotation-180, Interp.pow2Out.apply(fin));
                Tmp.v2.add(ex, ey);

                Draw.color(color.cpy(), Color.white, fin * 0.7f);
                Lines.stroke(Mathf.curve(fslope, 0, 0.42f) * 1.4f * Mathf.curve(fin, 0, 0.6f));
                Lines.lineAngleCenter( Tmp.v2.x,  Tmp.v2.y, angle, len);

            }
        }
    }
}


