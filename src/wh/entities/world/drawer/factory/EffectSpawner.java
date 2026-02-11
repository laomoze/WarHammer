package wh.entities.world.drawer.factory;

import arc.graphics.*;
import arc.math.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.draw.*;

import static arc.math.Mathf.random;
import static arc.util.Tmp.*;

public class EffectSpawner extends DrawBlock{
    public float x, y, width, height, rotation;
    public boolean mirror = false;

    public float effectChance = 0.1f, effectRot, effectRandRot;
    public Effect effect = Fx.sparkShoot;
    public Color effectColor = Color.white;


    @Override
    public void draw(Building build){

        if(Vars.state.isPaused()) return;

        for(int i = 0; i < (mirror ? 2 : 1); i++){
            if(!Vars.state.isPaused() && Mathf.chanceDelta(effectChance * build.warmup())){
                float sign = (i == 0 ? 1f : -1f), rot = build.rotation + (rotation * sign);
                v1.set(x * sign, y).rotate(build.rotation - 90).add(build.x, build.y);
                v1.add(v2.set(random(-height * 0.5f, height * 0.5f), random(-width * 0.5f, width * 0.5f)).rotate(rot));

                effect.at(v1.x, v1.y, rot + (effectRot * sign) + random(-effectRandRot, effectRandRot), effectColor);
            }
        }
    }
}
