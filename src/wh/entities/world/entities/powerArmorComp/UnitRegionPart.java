package wh.entities.world.entities.powerArmorComp;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class UnitRegionPart extends DrawUnitPart{
    protected UnitPartParams childParam = new UnitPartParams();

    /** Appended to unit/weapon/block name and drawn. */
    public String suffix = "";
    /** Overrides suffix if set. */
    public @Nullable String name;
    public TextureRegion heat, light;
    public TextureRegion[] regions = {};
    public TextureRegion[] outlines = {};
    public TextureRegion[] cellRegions = {};


    public boolean symmetry = false;
    /** If true, parts are mirrored across the turret. Requires -1 and -2 regions. */
    public boolean mirror = false;
    /** If true, an outline is drawn under the part. */
    public boolean outline = true;
    /** If true, this part has an outline created 'in-place'. Currently vanilla only, do not use this! */
    public boolean replaceOutline = false;
    /** If true, the base + outline regions are drawn. Set to false for heat-only regions. */
    public boolean drawRegion = true;
    /** If true, the heat region produces light. */
    public boolean heatLight = false;
    /** Whether to clamp progress to (0-1). If false, allows usage of interps that go past the range, but may have unwanted visual bugs depending on values. */
    public boolean clampProgress = true;
    /** Progress function for determining position/rotation. */
    public UnitPartProgress progress = UnitPartProgress.warmup;
    /** Progress function for scaling. */
    public UnitPartProgress growProgress = UnitPartProgress.warmup;
    /** Progress function for heat alpha. */
    public UnitPartProgress heatProgress = UnitPartProgress.heat;
    public Blending blending = Blending.normal;
    public float layer = -1, layerOffset, heatLayerOffset = 1f, turretHeatLayer = Layer.turretHeat;
    public float outlineLayerOffset = 0.01f;
    //note that origin DOES NOT AFFECT child parts
    public float x, y, xScl = 1f, yScl = 1f, rotation, originX, originY;
    public float moveX, moveY, growX, growY, moveRot;
    public float heatLightOpacity = 0.3f;
    public @Nullable Color color, colorTo, mixColor, mixColorTo;
    public Color heatColor = Pal.turretHeat.cpy();
    public Seq<DrawUnitPart> children = new Seq<>();
    public Seq<UnitPartMove> moves = new Seq<>();

    public float bodyScl = 5, swingScl = 0.02f;
    public UnitPartProgress moveSinProgress = UnitPartProgress.moveSin(bodyScl);
    public UnitPartProgress actionProgress = UnitPartProgress.actionTime;
    public boolean swingFront = true;
    public float sinAngle = 4;
    public float sinGrowX, sinGrowY;
    public float actionX, actionY, actionRot, actionGrowX, actionGrowY;

    public float endX, endY, endRot, forearmLen;

    public Interval interval = new Interval(1);


    public UnitRegionPart(String region){
        this.suffix = region;
    }

    public UnitRegionPart(String region, Blending blending, Color color){
        this.suffix = region;
        this.blending = blending;
        this.color = color;
        outline = false;
    }

    public UnitRegionPart(){
    }

    @Override
    public void draw(UnitPartParams params){

        PowerArmourUnit unit = params.unit;
        PowerArmourUnitType type = params.type;

        float z = Draw.z();
        if(layer > 0) Draw.z(layer);
        //TODO 'under' should not be special cased like this...
        if(under) Draw.z(z - 0.0001f);
        Draw.z(Draw.z() + 0.01f + layerOffset);

        float prevZ = Draw.z();
        float action = actionProgress.getClamp(params, false);
        float ax = actionX * action, ay = actionY * action, aRot = actionRot * action;
        float agx = actionGrowX * action, agy = actionGrowY * action;


        float prog = progress.getClamp(params, clampProgress), sclProg = growProgress.getClamp(params, clampProgress);
        float mx = moveX * prog + ax, my = moveY * prog + ay, mr = moveRot * prog + rotation + aRot,
        gx = growX * sclProg + agx, gy = growY * sclProg + agy;

        if(moves.size > 0){
            for(int i = 0; i < moves.size; i++){
                var move = moves.get(i);
                float p = move.progress.getClamp(params, clampProgress);
                mx += move.x * p;
                my += move.y * p;
                mr += move.rot * p;
                gx += move.gx * p;
                gy += move.gy * p;
            }
        }

        int len = mirror && params.sideOverride == -1 ? 2 : 1;
        float preXscl = Draw.xscl, preYscl = Draw.yscl;
        Draw.xscl *= xScl + gx;
        Draw.yscl *= yScl + gy;

        for(int s = 0; s < len; s++){
            //use specific side if necessary
            int i = params.sideOverride == -1 ? s : params.sideOverride;

            //can be null
            var region = drawRegion && regions.length > 0 ? regions[Math.min(i, regions.length - 1)] : null;
            var cellRegion = drawRegion && cellRegions.length > 0 ? cellRegions[Math.min(i, cellRegions.length - 1)] : null;
            float sign = (i == 0 ? 1 : -1) * params.sideMultiplier;
            Tmp.v1.set((x + mx) * sign, y + my).rotateRadExact((params.rotation - 90) * Mathf.degRad);

            Draw.xscl *= sign;

            if(originX != 0f || originY != 0f){
                //correct for offset caused by origin shift
                Tmp.v1.sub(Tmp.v2.set(-originX * Draw.xscl, -originY * Draw.yscl).rotate(params.rotation - 90f).add(originX * Draw.xscl, originY * Draw.yscl));
            }

            float r = symmetry ? 1f : -1f;
            float e = unit.elevation;
            float extension = Mathf.lerp(unit.walkExtend(false), 0, e);
            float boostTrns = e * 2f;
            float swingX = swingFront ? extension * r - boostTrns : 0, swingY = swingFront ? -boostTrns * r : 0;
            float moveSin = moveSinProgress.getClamp(params, false);
            float sx = Angles.trnsx(params.rotation + mr + moveSin * sinAngle * unit.bodyMove, swingX, swingY) * sinAngle * unit.bodyMove * swingScl;
            float sy = Angles.trnsy(params.rotation + mr + moveSin * sinAngle * unit.bodyMove, swingX, swingY) * sinAngle * unit.bodyMove * swingScl;
            float sGrow = (1 - Math.max(-unit.bodyMove * i, 0) * 0.5f) * swingScl;

            float rx, ry, rot;
            rx = params.x + sx + Tmp.v1.x;
            ry = params.y + sy + Tmp.v1.y;
            rot = mr * sign + params.rotation - 90 + moveSin * sinAngle * unit.bodyMove;

            Draw.xscl *= xScl + sinGrowX * sGrow;
            Draw.yscl *= yScl + sinGrowY * sGrow;

            if(outline && drawRegion){
                TextureRegion outline = outlines[Math.min(i, regions.length - 1)];
                Draw.z(prevZ + outlineLayerOffset);
                rect(outline, rx, ry, rot);
                Draw.z(prevZ);
            }

            if(drawRegion && region != null){
                if(color != null && colorTo != null){
                    Draw.color(color, colorTo, prog);
                }else if(color != null){
                    Draw.color(color);
                }

                if(mixColor != null && mixColorTo != null){
                    Draw.mixcol(mixColor, mixColorTo, prog);
                }else if(mixColor != null){
                    Draw.mixcol(mixColor, mixColor.a);
                }

                Draw.blend(blending);
                Draw.z(Draw.z() + 0.01f + layerOffset);
                rect(region, rx, ry, rot);

                if(cellRegion != null){
                    drawCell(unit, cellRegion, rx, ry, 1, 1, rot);
                }

                Draw.blend();
                if(color != null) Draw.color();
            }

            if(heat.found()){
                float hprog = heatProgress.getClamp(params, clampProgress);
                heatColor.write(Tmp.c1).a(hprog * heatColor.a);
                Drawf.additive(heat, Tmp.c1, 1f, rx, ry, rot, turretShading ? turretHeatLayer : Draw.z() + heatLayerOffset, originX, originY);
                if(heatLight) Drawf.light(rx, ry, light.found() ? light : heat, rot, Tmp.c1, heatLightOpacity * hprog);
            }

            Draw.xscl *= sign;
        }

        Draw.color();
        Draw.mixcol();

        Draw.z(z);

        //draw child, if applicable - only at the end
        //TODO lots of copy-paste here
        if(children.size > 0){
            for(int s = 0; s < len; s++){
                int i = (params.sideOverride == -1 ? s : params.sideOverride);
                float sign = (i == 1 ? -1 : 1) * params.sideMultiplier;
                Tmp.v1.set((x + mx) * sign, y + my).rotateRadExact((params.rotation - 90) * Mathf.degRad);

                float chargeTime, smothHeat;
                if(unit.mounts[0] instanceof MainWeaponMount m && m.weapon instanceof MainWeapon mw && mw.melee){
                    chargeTime = m.actionProgress;
                    smothHeat = m.smoothHeat;
                }else{
                    chargeTime = 1;
                    smothHeat = 1;
                }
                childParam.set(unit, type, params.warmup, params.reload, params.smoothReload, params.heat, params.recoil, params.charge, smothHeat, chargeTime, params.x + Tmp.v1.x, params.y + Tmp.v1.y, mr * sign + params.rotation);
                childParam.sideMultiplier = params.sideMultiplier;
                childParam.life = params.life;
                childParam.sideOverride = i;
                for(var child : children){
                    child.draw(childParam);
                }
            }
        }

        Draw.scl(preXscl, preYscl);
    }

    public void drawCell(Unit unit, TextureRegion cellRegion, float x, float y, float width, float height, float rotation){
        if(cellRegion.found()){
            unit.type.applyColor(unit);

         /*   float
            cellSclX = cellRegion.width *  (cellRegion.scl()  + gx) * r,
            cellSclY = cellRegion.height * (cellRegion.scl() +  gy);*/

            float r = symmetry ? 1f : -1f;
            Draw.color(unit.type.cellColor(unit));
            /* Draw.rect(cellRegion, x, y, width, height, rotation);*/
            float w = cellRegion.width * cellRegion.scl() * Draw.xscl * r, h = cellRegion.height * cellRegion.scl() * Draw.yscl;
            Draw.rect(cellRegion, x, y, w, h, w / 2f + originX * Draw.xscl, h / 2f + originY * Draw.yscl, rotation);
            Draw.reset();
        }
    }

    void rect(TextureRegion region, float x, float y, float rotation){
        float r = symmetry ? 1f : -1f;
        float w = region.width * region.scl() * Draw.xscl * r, h = region.height * region.scl() * Draw.yscl;
        Draw.rect(region, x, y, w, h, w / 2f + originX * Draw.xscl, h / 2f + originY * Draw.yscl, rotation);
    }

    @Override
    public void load(String name){
        String realName = this.name == null ? name + "-" + suffix : this.name;

        if(drawRegion){
            if(mirror && turretShading){
                regions = new TextureRegion[]{
                Core.atlas.find(realName + "-r"),
                Core.atlas.find(realName + "-l")
                };

                outlines = new TextureRegion[]{
                Core.atlas.find(realName + "-r-outline"),
                Core.atlas.find(realName + "-l-outline")};
            }else{
                cellRegions = new TextureRegion[]{Core.atlas.find(realName + "-cell")};
                regions = new TextureRegion[]{Core.atlas.find(realName)};
                outlines = new TextureRegion[]{Core.atlas.find(realName + "-outline")};
            }
        }

        heat = Core.atlas.find(realName + "-heat");
        light = Core.atlas.find(realName + "-light");
        for(var child : children){
            child.turretShading = turretShading;
            child.load(name);
        }
    }

    @Override
    public void getOutlines(Seq<TextureRegion> out){
        if(outline && drawRegion){
            out.addAll(regions);
        }
        for(var child : children){
            child.getOutlines(out);
        }
    }
}
