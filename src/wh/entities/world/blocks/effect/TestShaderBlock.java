package wh.entities.world.blocks.effect;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.*;
import wh.content.*;

import static mindustry.Vars.renderer;

public class TestShaderBlock extends ForceProjector {
    public TestShaderBlock(String name) {
        super(name);
}

public class TestShaderBlockBuild extends ForceBuild {
    @Override
    public void drawShield(){
        if(!broken){
            float radius = realRadius();

            if(radius > 0.001f){
                Draw.color(team.color, Color.white, Mathf.clamp(hit));

                if(renderer.animateShields){
                    Draw.z(WHContent.HEXAGONAL_SHIELD+0.1f + 0.001f * hit);
                    Fill.poly(x, y, sides, radius, shieldRotation);
                }else{
                    Draw.z(WHContent.HEXAGONAL_SHIELD);
                    Lines.stroke(1.5f);
                    Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
                    Fill.poly(x, y, sides, radius, shieldRotation);
                    Draw.alpha(1f);
                    Lines.poly(x, y, sides, radius, shieldRotation);
                    Draw.reset();
                }
            }
        }

        Draw.reset();
    }
}

}
