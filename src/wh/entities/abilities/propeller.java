package wh.entities.abilities;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.environment.*;

import static wh.core.WarHammerMod.name;

public class Propeller extends Ability{
    public float px, py, length, speed;
    public String sprite;

    float rot = 0;

    public boolean drawWing = false;

    public static Effect wind = new Effect(30, e -> {
        Draw.z(Layer.debris);
        Draw.color(e.color);
        Fill.circle(e.x, e.y, e.fout() * 6 + 0.3f);
    });

    public Propeller(float px, float py, String sprite, float length, float speed){
        this.px = px;
        this.py = py;
        this.length = length;
        this.speed = speed;
        this.sprite = sprite;
    }

    public Propeller(float px, float py, float length, float speed){
        this.px = px;
        this.py = py;
        this.length = length;
        this.speed = speed;
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability.wh-utilities-propeller", px, py);
    }

    @Override
    public void update(Unit unit){
        if(unit.type != null && unit.type.naval && !unit.floorOn().isLiquid){
            unit.elevation = 1;
        }

        float realSpeed = unit.elevation * speed * Time.delta;
        float unitRot = unit.rotation - 90;
        rot += realSpeed;
        float out = unit.elevation * length;
        float x = unit.x + Angles.trnsx(unitRot, px, py) + Angles.trnsx(unitRot, 0, out);
        float y = unit.y + Angles.trnsy(unitRot, px, py) + Angles.trnsy(unitRot, 0, out);
        if(!unit.moving() && unit.isFlying()){
            Floor floor = Vars.world.floorWorld(x, y);
            if(floor != null) wind.at(x + Mathf.range(8), y + Mathf.range(8), floor.mapColor);
        }
    }

    @Override
    public void draw(Unit unit){

        float e = Math.max(unit.elevation, unit.type.shadowElevation);
        float unitRot = unit.rotation - 90;
        float out = unit.elevation * length;
        float x = unit.x + Angles.trnsx(unitRot, px, py) + Angles.trnsx(unitRot, 0, out);
        float y = unit.y + Angles.trnsy(unitRot, px, py) + Angles.trnsy(unitRot, 0, out);
        float z1 = Math.min(Layer.darkness, Layer.groundUnit - 1);
        float z2 = Math.max(z1, unit.elevation * Layer.flyingUnitLow);

        Draw.mixcol(Color.white, unit.hitTime);
        Draw.z(z2);
        if(drawWing) Draw.rect(Core.atlas.find(sprite), x, y, unit.rotation - 90);
        Draw.z(z2 - 1);
        Draw.rect(Core.atlas.find(name("wing")), x, y, unit.rotation + rot);

        Draw.z(z1);
        Draw.color(Pal.shadow);
        Draw.rect(Core.atlas.find(name("wing")), x + UnitType.shadowTX * e, y + UnitType.shadowTY * e, unit.rotation + rot * 2);
        Draw.mixcol();
        if(unit.isFlying() && drawWing){
            Draw.z(z1);
            Draw.color(Pal.shadow);
            Draw.rect(Core.atlas.find(sprite), x + UnitType.shadowTX * e, y + UnitType.shadowTY * e, unit.rotation - 90);
        }
        Draw.reset();
    }
}