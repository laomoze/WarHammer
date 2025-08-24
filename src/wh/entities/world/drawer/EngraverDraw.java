package wh.entities.world.drawer;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.draw.*;


import static wh.graphics.Drawn.*;

public class EngraverDraw extends DrawBlock{
    public float laserLength = 9f;
    public float rot = 45f;
    public Color[] colors = {Pal.accent.cpy().a(0.4f), Pal.accent.cpy(), Color.white.cpy()};
    public float width = 4, strokeFrom = 1.2f, strokeTo = 0.5f, pointyScaling = 0.75f;
    public float backLength = 3f, frontLength = 5f;
    public float oscScl = 3f, oscMag = 0.4f;
    public int divisions = 10;
    public int time = 150;
    public float lengthMag=0.4f;
    public boolean rotate = false;
    private static float rotator_90(int time){return 90 * Interp.pow5.apply(cycle(0,time));}

    @Override
    public void draw(Building build){
        if(build.warmup() > 0f){
            for(int i = 0; i < colors.length; i++){
                for(int a = 0; a < 4; a++){
                    float angle = 360f / 4;
                    float sin1 =1- Mathf.absin(Time.time+Mathf.pi/2*a*5,20, lengthMag);
                    Tmp.v1.trns(a * angle + rot-180+(rotate?rotator_90(time):0) , laserLength);
                    float rx = build.x + Tmp.v1.x, ry = build.y + Tmp.v1.y;
                    float baseLen = 2+build.dst(rx, ry) / 1.3f * build.warmup()*sin1;
                    float rotation= angle * a+rot+(rotate?rotator_90(time):0);
                    Draw.color(Tmp.c1.set(colors[i]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));
                    Draw.alpha(0.6f);

                    float colorFin = i / (float)(colors.length - 1);
                    float baseStroke = Mathf.lerp(strokeFrom, strokeTo, colorFin);
                    float stroke = (width + Mathf.absin(Time.time, oscScl, oscMag)) * baseStroke * (build.efficiency > 0 ? build.efficiency / 2 : build.warmup());
                    float ellipseLenScl = Mathf.lerp(1 - i / (float)(colors.length), 1f, pointyScaling);

                    Lines.stroke(stroke);
                    Lines.lineAngle(rx, ry, rotation, (baseLen - frontLength*build.warmup()), false);

                    //back ellipse
                    Drawf.flameFront(rx, ry, divisions, rotation + 180f, backLength, stroke / 2f);
                    //front ellipse
                    Tmp.v3.trnsExact(rotation, (baseLen - frontLength*build.warmup()));
                    Drawf.flameFront(rx + Tmp.v3.x, ry + Tmp.v3.y, divisions, rotation, frontLength * ellipseLenScl * build.warmup(), stroke / 2f);
                }
                Draw.reset();
            }
        }
    }
}
