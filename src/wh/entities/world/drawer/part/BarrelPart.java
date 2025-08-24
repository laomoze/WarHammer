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
    public float barWidth = 3f, barHeight = 3f;
    public int barrelCount = 4;
    public float layerOffset = -0.001f;

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

        float time = (Time.time / 5 % 360f) * Interp.smooth.apply(prog);

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
                Draw.z(Draw.z() + layerOffset);
                v.trns(params.rotation - 90f, barWidth * Mathf.cosDeg(time - angle * i), barHeight * Mathf.sinDeg(time - angle * i));
                Draw.z(Draw.z() + layerOffset - Mathf.sinDeg(time - angle * i) / 1000f);

                if(region != null){
                    Draw.rect(region, rx + v.x, ry + v.y, rot);
                }
                if(heat.found()){
                    float hprog = heatProgress.getClamp(params, clampProgress);
                    heatColor.write(Tmp.c1).a(hprog * heatColor.a);
                    Drawf.additive(heat, Tmp.c1, 1f,rx+ v.x, ry +v.y, rot, turretShading ? turretHeatLayer : Draw.z() + heatLayerOffset, originX, originY);
                    if(heatLight) Drawf.light(rx+ v.x, ry +v.y, light.found() ? light : heat, rot, Tmp.c1, heatLightOpacity * hprog);
                }
            }

        }
    }
}
//nnd再也不造json石了
//中右下
                     /*   parts.add(new RegionPart("-barrel"){


                                      {
                                          recoilIndex = 0;
                                          layerOffset = -0.01f;
                                          recoilTime = 0f;
                                          recoil = 0f;
                                          y = 17f;
                                          x = 0.75f;
                                          moveX = 1.35f;
                                          moveY = -2F;
                                          color = Color.valueOf("82848CFF");
                                          colorTo = Color.valueOf("4A4B53FF");
                                          heatColor = Color.valueOf("FF774280");
                                          heatProgress = PartProgress.warmup

                                          ;
                                          progress = PartProgress.heat;
                                          turretHeatLayer = 50;
                                          xScl = 1.1f;
                                          yScl = 1.3f;
                                          moves.add(new PartMove(PartProgress.heat, 0, 0f, 0, -0.03f, 0f));

                                      }
                                  }
                        );
                        parts.add(new RegionPart("-barrel"){

                                      {
                                          recoilIndex = 0;
                                          layerOffset = -0.01f;
                                          recoil = 0f;
                                          recoilTime = 5f;
                                          y = 17f;
                                          x = 1.875f;
                                          moveX = 1.1f;
                                          moveY = -2F;
                                          color = Color.valueOf("4A4B53FF");
                                          colorTo = Color.valueOf("82848CFF");
                                          heatColor = Color.valueOf("FF774280");
                                          heatProgress = PartProgress.warmup

                                          ;
                                          progress = PartProgress.heat;
                                          turretHeatLayer = 50;
                                          xScl = 1f;
                                          yScl = 1.3f;
                                          moves.add(new PartMove(PartProgress.heat, 0, 0f, 0, -0.03f, 0f));

                                      }
                                  }
                        );

                        //中左下
                        parts.add(new RegionPart("-barrel"){

                                      {
                                          recoilIndex = 1;
                                          layerOffset = -0.01f;
                                          recoilTime = 0f;
                                          recoil = 0f;
                                          y = 17f;
                                          x = -1.875f;
                                          moveX = 1.125f;
                                          moveY = -2F;
                                          color = Color.valueOf("4A4B53FF");
                                          colorTo = Color.valueOf("82848CFF");
                                          heatColor = Color.valueOf("FF774280");
                                          heatProgress = PartProgress.warmup

                                          ;
                                          progress = PartProgress.heat;
                                          turretHeatLayer = 50;
                                          xScl = 1f;
                                          yScl = 1.3f;
                                          moves.add(new PartMove(PartProgress.heat, 0, 0f, 0, -0.03f, 0f));

                                      }
                                  }
                        );
                        parts.add(new RegionPart("-barrel"){

                                      {
                                          recoilIndex = 1;
                                          layerOffset = -0.01f;
                                          recoilTime = 0f;
                                          recoil = 0f;
                                          y = 17f;
                                          x = -0.75f;
                                          moveX = 1.275f;
                                          moveY = -2F;
                                          color = Color.valueOf("4A4B53FF");
                                          colorTo = Color.valueOf("4A4B53FF");
                                          heatColor = Color.valueOf("FF774280");
                                          heatProgress = PartProgress.warmup;
                                          progress = PartProgress.heat;
                                          turretHeatLayer = 50;
                                          xScl = 1f;
                                          yScl = 1.3f;
                                          moves.add(new PartMove(PartProgress.heat, 0, 0f, 0, -0.03f, 0f));

                                      }
                                  }
                        );

                        //左
                        parts.add(new RegionPart("-barrel"){

                                      {
                                          recoilIndex = 2;
                                          layerOffset = -0.01f;
                                          recoilTime = 0f;
                                          recoil = 0f;
                                          y = 17f;
                                          x = -3.125f;
                                          moveX = 1.125f;
                                          moveY = -2F;
                                          color = Color.valueOf("4A4B53FF");
                                          colorTo = Color.valueOf("4A4B53FF");
                                          heatColor = Color.valueOf("FF774280");
                                          heatProgress = PartProgress.warmup;
                                          progress = PartProgress.heat;
                                          turretHeatLayer = 50;
                                          xScl = 1f;
                                          yScl = 1.3f;
                                          moves.add(new PartMove(PartProgress.heat, 0, 0f, 0, -0.03f, 0f));

                                      }
                                  }
                        );
                        parts.add(new RegionPart("-barrel"){

                                      {
                                          recoilIndex = 2;
                                          layerOffset = -0.01f;
                                          recoilTime = 0f;
                                          recoil = 0f;
                                          y = 17f;
                                          x = -1.875f;
                                          moveX = 1.125f;
                                          moveY = -2F;
                                          color = Color.valueOf("82848CFF");
                                          colorTo = Color.valueOf("4A4B53FF");
                                          heatColor = Color.valueOf("FF774280");
                                          heatProgress = PartProgress.warmup;
                                          progress = PartProgress.heat;
                                          turretHeatLayer = 50;
                                          xScl = 1f;
                                          yScl = 1.3f;
                                          moves.add(new PartMove(PartProgress.heat, 0, 0f, 0, -0.03f, 0f));

                                      }
                                  }

                        );


                        //中
                        parts.add(new RegionPart("-barrel"){

                                      {
                                          recoilIndex = 3;
                                          layerOffset = -0.01f;
                                          recoilTime = 0f;
                                          recoil = 0f;
                                          y = 17f;
                                          x = -0.75f;
                                          moveX = -2.5f;
                                          moveY = -2F;
                                          color = Color.valueOf("565761FF");
                                          colorTo = Color.valueOf("4A4B53FF");
                                          heatColor = Color.valueOf("FF774280");
                                          heatProgress = PartProgress.warmup;
                                          progress = PartProgress.heat;
                                          turretHeatLayer = 50;
                                          xScl = 1.1f;
                                          yScl = 1.3f;
                                          moves.add(new PartMove(PartProgress.heat, 0, 0f, 0, -0.05f, 0f));

                                      }
                                  }
                        );
                        parts.add(new RegionPart("-barrel"){

                                      {
                                          recoilIndex = 3;
                                          layerOffset = -0.01f;
                                          recoilTime = 0f;
                                          recoil = 0f;
                                          y = 17f;
                                          x = 0.75f;
                                          moveX = -2.5f;
                                          moveY = -2F;
                                          color = Color.valueOf("989AA4FF");
                                          colorTo = Color.valueOf("82848CFF");
                                          heatColor = Color.valueOf("FF774280");
                                          heatProgress = PartProgress.warmup;
                                          progress = PartProgress.heat;
                                          turretHeatLayer = 50;
                                          xScl = 1.1f;
                                          yScl = 1.3f;
                                          moves.add(new PartMove(PartProgress.heat, 0, 0f, 0, -0.03f, 0f));

                                      }
                                  }
                        );
                        //右
                        parts.add(new RegionPart("-barrel"){

                                      {
                                          recoilIndex = 4;
                                          layerOffset = -0.01f;
                                          recoilTime = 0f;
                                          recoil = 0f;
                                          y = 17f;
                                          x = 1.875f;
                                          moveX = -1.5f;
                                          moveY = -2F;
                                          color = Color.valueOf("565761FF");
                                          colorTo = Color.valueOf("4A4B53FF");
                                          heatColor = Color.valueOf("FF774280");
                                          heatProgress = PartProgress.warmup;
                                          progress = PartProgress.heat;
                                          turretHeatLayer = 50;
                                          xScl = 1f;
                                          yScl = 1.3f;
                                          moves.add(new PartMove(PartProgress.heat, 0, 0f, 0, -0.03f, 0f));

                                      }
                                  }
                        );
                        parts.add(new RegionPart("-barrel"){

                                      {
                                          recoilIndex = 4;
                                          layerOffset = -0.01f;
                                          recoilTime = 0f;
                                          recoil = 0f;
                                          y = 17f;
                                          x = 3.125f;
                                          moveX = -2.5f;
                                          moveY = -2F;
                                          color = Color.valueOf("989AA4FF");
                                          colorTo = Color.valueOf("82848CFF");
                                          heatColor = Color.valueOf("FF774280");
                                          heatProgress = PartProgress.warmup;
                                          progress = PartProgress.heat;
                                          turretHeatLayer = 50;
                                          xScl = 1f;
                                          yScl = 1.3f;
                                          moves.add(new PartMove(PartProgress.heat, 0, 0f, 0, -0.03f, 0f));

                                      }
                                  }
                        );*/
