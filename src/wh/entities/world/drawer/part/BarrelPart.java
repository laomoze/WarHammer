package wh.entities.world.drawer.part;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;

public class BarrelPart extends RegionPart{
    public Color heatColor = Pal.turretHeat.cpy();
    public float intervalWidth = 3f, intervalHeight = 3f;
    public int barrelCount = 4;
    public float layerOffset = -0.001f;
    public PartProgress progress = PartProgress.reload;

    @Override
    public void load(String name){
        super.load(name);
    }

    public BarrelPart(String suffix){
        this.suffix = suffix;
    }

    public BarrelPart(){
    }

    @Override
    public void draw(PartParams params){
        float prog = progress.getClamp(params, clampProgress);
        float mx = moveX * prog, my = moveY * prog, mr = moveRot * prog + rotation;
        int len = mirror && params.sideOverride == -1 ? 2 : 1;

        float time = /*(Time.time / 8 % 360f) **/ Interp.smooth.apply(prog);

        for(int s = 0; s < len; s++){
            Vec2 v = Tmp.v1;
            int a = params.sideOverride == -1 ? s : params.sideOverride;
            float sign = (a == 0 ? 1 : -1) * params.sideMultiplier;
            Tmp.v1.set((x + mx) * sign, y + my).rotateRadExact((params.rotation - 90) * Mathf.degRad);
            float
            rx = params.x + Tmp.v1.x,
            ry = params.y + Tmp.v1.y,
            rot = mr * sign + params.rotation - 90;

            for(int i = 0; i < barrelCount; i++){
                var region = drawRegion ? regions[Math.min(i, regions.length - 1)] : null;
                float angle = 360f / barrelCount;
                v.trns(params.rotation - 90f, intervalWidth * Mathf.cosDeg(time - angle * i), 0 /*intervalHeight * Mathf.sinDeg(time - angle * i)*/);
                Draw.z(Draw.z() + layerOffset * barrelCount - Mathf.sinDeg(time - angle * i) / 1000f);

                if(region != null){
                    Draw.rect(region, rx + v.x, ry + v.y, rot);
                }
                if(heat.found()){
                    float hprog = heatProgress.getClamp(params, clampProgress);
                    heatColor.write(Tmp.c1).a(hprog * heatColor.a);
                    Drawf.additive(heat, Tmp.c1, 1f, rx + v.x, ry + v.y, rot, turretShading ? turretHeatLayer : Draw.z() + heatLayerOffset, originX, originY);
                    if(heatLight) Drawf.light(rx + v.x, ry + v.y, light.found() ? light : heat, rot, Tmp.c1, heatLightOpacity * hprog);
                }
            }
        }
    }
}