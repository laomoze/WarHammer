package wh.entities.world.entities.powerArmorComp;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.environment.*;
import wh.entities.world.entities.*;

public class PowerArmourUnitType extends WHUnitType{

    public static final float shadowTX = -12, shadowTY = -13;
    private static final Vec2 legOffset = new Vec2();
    private static final Seq<UnitStance> tmpStances = new Seq<>();


    public TextureRegion handRegion, forearmRegion, headCellRegion, headRegion, bodyRegion, bodyCellRegion, shoulderRegion, shoulderCellRegion;

    public PowerArmourUnitType(String name){
        super(name);
        constructor = PowerArmourUnit::new;
        drawBody = false;
    }

    @Override
    public void load(){
        super.load();
        for(var weapon : weapons){
            if(weapon instanceof MainWeapon m){
                m.loadUnitName(name);
            }
        }
     /*   handRegion = Core.atlas.find(name + "-hand", name);
        forearmRegion = Core.atlas.find(name + "-forearm", name);

        headRegion = Core.atlas.find(name + "-head", name);
        bodyRegion = Core.atlas.find(name + "-body", name);
        shoulderRegion = Core.atlas.find(name + "-shoulder", name);

        headCellRegion = Core.atlas.find(name + "-head" + "-cell", name);
        bodyCellRegion = Core.atlas.find(name + "-body" + "-cell", name);
        shoulderCellRegion = Core.atlas.find(name + "-shoulder" + "-cell", name);*/
    }


    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        super.getRegionsToOutline(out);
        for(Weapon weapon : weapons){
            if(weapon instanceof MainWeapon m){
                for(var part : m.unitParts){
                    part.getOutlines(out);
                }
            }
        }
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);
       /* float scl = xscl;
        if(unit.inFogTo(Vars.player.team())) return;

        if(buildSpeed > 0f){
            unit.drawBuilding();
        }

        if(unit.mining()){
            drawMining(unit);
        }

        boolean isPayload = !unit.isAdded();

        Segmentc seg = unit instanceof Segmentc c ? c : null;
        float z =
        isPayload ? Draw.z() :
        //dead flying units are assumed to be falling, and to prevent weird clipping issue with the dark "fog", they always draw above it
        unit.elevation > 0.5f || (flying && unit.dead) ? (flyingLayer) :
        seg != null ? groundLayer + seg.segmentIndex() / 4000f * Mathf.sign(segmentLayerOrder) + (!segmentLayerOrder ? 0.01f : 0f) :
        groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);

        if(!isPayload && (unit.isFlying() || shadowElevation > 0)){
            Draw.z(Math.min(Layer.darkness, z - 1f));
            drawShadow(unit);
        }

        Draw.z(z - 0.02f);

        if(unit instanceof PowerArmourUnit m){
            *//*drawPowerArmourUnit(m, z);*//*
            drawPowerArmourBase(m);

            legOffset.trns(m.baseRotation(), 0f, Mathf.lerp(Mathf.sin(m.walkExtend(true), 2f / Mathf.PI, 1) * mechSideSway, 0f, unit.elevation));

            //front
            legOffset.add(Tmp.v1.trns(m.baseRotation() + 90, 0f, Mathf.lerp(Mathf.sin(m.walkExtend(true), 1f / Mathf.PI, 1) * mechFrontSway, 0f, unit.elevation)));

            unit.trns(legOffset.x, legOffset.y);
        }

        Draw.z(Math.min(z - 0.01f, Layer.bullet - 1f));

        if(unit instanceof Payloadc){
            drawPayload((Unit & Payloadc)unit);
        }

        if(drawSoftShadow) drawSoftShadow(unit);

        Draw.z(z);

        if(drawBody) drawOutline(unit);
        drawWeaponOutlines(unit);
        if(engineLayer > 0) Draw.z(engineLayer);
        if(trailLength > 0 && !naval && (unit.isFlying() || !useEngineElevation)){
            drawTrail(unit);
        }
        if(engines.size > 0) drawEngines(unit);
        Draw.z(z);
        if(drawCell && !(unit instanceof Crawlc)) drawCell(unit);
        Draw.scl(scl); //TODO this is a hack for neoplasm turrets
        drawWeapons(unit);
        if(drawItems) drawItems(unit);
        if(!isPayload){
            drawLight(unit);
        }

        if(unit.shieldAlpha > 0 && drawShields){
            drawShield(unit);
        }

        //TODO how/where do I draw under?
        if(parts.size > 0){
            for(int i = 0; i < parts.size; i++){
                var part = parts.get(i);

                WeaponMount mount = unit.mounts.length > part.weaponIndex ? unit.mounts[part.weaponIndex] : null;
                if(mount != null){
                    DrawPart.params.set(mount.warmup, mount.reload / mount.weapon.reload, mount.smoothReload, mount.heat, mount.recoil, mount.charge, unit.x, unit.y, unit.rotation);
                }else{
                    DrawPart.params.set(0f, 0f, 0f, 0f, 0f, 0f, unit.x, unit.y, unit.rotation);
                }

                if(unit instanceof Scaled s){
                    DrawPart.params.life = s.fin();
                }

                applyColor(unit);
                part.draw(DrawPart.params);
            }
        }

        if(!isPayload){
            for(Ability a : unit.abilities){
                Draw.reset();
                a.draw(unit);
            }
        }

        if(unit instanceof PowerArmourUnit m){
            m.trns(-legOffset.x, -legOffset.y);
        }

        Draw.reset();*/

    }


  /*  public void drawPowerArmourUnit(PowerArmourUnit unit, float layer){
        Draw.reset();

        Draw.mixcol(Color.white, unit.hitTime);

        float e = unit.elevation;

        float shoulderSin =Mathf.sin(Time.time, bodyScl* Mathf.PI, 1* unit.bodyMove);

        float extension = Mathf.lerp(unit.walkExtend(false), 0, e);
        float boostTrns = e * 2f;

        Floor floor = unit.isFlying() ? Blocks.air.asFloor() : unit.floorOn();

        if(floor.isLiquid){
            Draw.color(Color.white, floor.mapColor, 0.5f);
        }

        if(bodyRegion.found()){
            Draw.rect(bodyRegion,
            unit.x, unit.y,
            bodyRegion.width * bodyRegion.scl(),
            bodyRegion.height * bodyRegion.scl(),
            unit.rotation() - 90 + shoulderSin * bodySwayAngle* unit.bodyMove);
            drawCell(unit, bodyCellRegion, unit.x, unit.y,
            bodyCellRegion.width * bodyCellRegion.scl(),
            bodyCellRegion.height * bodyCellRegion.scl(),
            unit.rotation() - 90 + shoulderSin * bodySwayAngle*2* unit.bodyMove);
        }

        for(int i : Mathf.signs){
            float bodyOffsetX = Angles.trnsx(shoulderSin,bodySwayAngle * unit.bodyMove) ;
            float bodyOffsetY = Angles.trnsy(shoulderSin,bodySwayAngle * unit.bodyMove) ;
            float sx = unit.x + Angles.trnsx(unit.rotation() + shoulderSin * bodySwayAngle* unit.bodyMove, extension * i - boostTrns, -boostTrns * i) * shoulderSway * unit.bodyMove;
            float sy = unit.y + Angles.trnsy(unit.rotation() + shoulderSin * bodySwayAngle* unit.bodyMove, extension * i - boostTrns, -boostTrns * i) * shoulderSway * unit.bodyMove;
            Draw.rect(shoulderRegion,
            sx, sy,
            shoulderRegion.width * shoulderRegion.scl() * i,
            shoulderRegion.height * shoulderRegion.scl(),
            unit.rotation() - 90 + 35f * i * e + shoulderSin * bodySwayAngle * unit.bodyMove);

            drawCell(unit, shoulderCellRegion,
            sx, sy,
            shoulderCellRegion.width * shoulderCellRegion.scl() * i,
            shoulderCellRegion.height * shoulderCellRegion.scl(),
            unit.rotation() - 90 + 35f * i * e + shoulderSin * bodySwayAngle * unit.bodyMove);
        }

        Draw.z(layer + 0.01f);

        Draw.rect(headRegion, unit, unit.rotation() - 90);
        drawCell(unit, headCellRegion, unit.x, unit.y, headRegion.width * headRegion.scl(),
        headRegion.height * headRegion.scl(), unit.rotation() - 90);
    }*/

    public void drawPowerArmourBase(PowerArmourUnit unit){
        Draw.reset();

        float e = unit.elevation;

        float sin = Mathf.lerp(Mathf.sin(unit.walkExtend(true), 2f / Mathf.PI, 1f), 0f, e);
        float extension = Mathf.lerp(unit.walkExtend(false), 0, e);
        float boostTrns = e * 2f;

        Floor floor = unit.isFlying() ? Blocks.air.asFloor() : unit.floorOn();

        if(floor.isLiquid){
            Draw.color(Color.white, floor.mapColor, 0.5f);
        }

        for(int i : Mathf.signs){
            Draw.mixcol(Tmp.c1.set(mechLegColor).lerp(Color.white, Mathf.clamp(unit.hitTime)), Math.max(Math.max(0, i * extension / mechStride), unit.hitTime));

            Draw.rect(legRegion,
            unit.x + Angles.trnsx(unit.baseRotation(), extension * i - boostTrns, -boostTrns * i),
            unit.y + Angles.trnsy(unit.baseRotation(), extension * i - boostTrns, -boostTrns * i),
            legRegion.width * legRegion.scl() * i,
            legRegion.height * legRegion.scl() * (1 - Math.max(-sin * i, 0) * 0.5f),
            unit.baseRotation() - 90 + 35f * i * e);
        }

        Draw.mixcol(Color.white, unit.hitTime);

        if(unit.lastDrownFloor != null){
            Draw.color(Color.white, Tmp.c1.set(unit.lastDrownFloor.mapColor).mul(0.83f), unit.drownTime * 0.9f);
        }else{
            Draw.color(Color.white);
        }

        Draw.rect(baseRegion, unit, unit.baseRotation() - 90);

        Draw.mixcol();
    }


    @Override
    public void drawCell(Unit unit){
    }
}
