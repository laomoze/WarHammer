package wh.entities.world.drawer;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.draw.*;
import wh.entities.world.blocks.production.*;
import wh.entities.world.blocks.production.OverheatGenericCrafter.*;

public class DrawOverheatOutput extends DrawBlock{
    public TextureRegion heat;

    public Color heatColor = new Color(1f, 0.22f, 0.22f, 0.9f);
    public float heatPulse = 0.3f, heatPulseScl = 10f;

    public int rotOffset = 0;

    public DrawOverheatOutput(){}

    public DrawOverheatOutput(int rotOffset){
        this.rotOffset = rotOffset;
    }

    @Override
    public void draw(Building build){
        float rotdeg = (build.rotation + rotOffset) * 90;
        if(build instanceof OverheatGenericCrafterBuild heater && heater.heat > 0 ){
            Draw.z(Layer.blockAdditive+0.3f);
            Draw.blend(Blending.additive);
            Draw.color(heatColor, heater.heatFrac() * (heatColor.a * (1f - heatPulse + Mathf.absin(heatPulseScl, heatPulse))));
            if(heat.found()) Draw.rect(heat, build.x, build.y, rotdeg);
            Draw.blend();
            Draw.reset();
        }
    }

    @Override
    public void load(Block block){
        heat = Core.atlas.find(block.name + "-heat");
    }
}