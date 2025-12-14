package wh.entities.world.entities.powerArmorComp;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.graphics.*;

import static mindustry.Vars.state;

public class DrawBladePart extends DrawUnitPart{
    public float layer, layerOffset = 10;
    public float x, y;
    public float actionX, actionY, actionRot;
    public float moveX, moveY, moveRot;
    public float rotation;
    public Color heatColor = Pal.accent;
    public UnitPartProgress progress = UnitPartProgress.warmup;
    public float rad = 0;
    public int trailLength = 10, trailWidth = 2;
    public Color color = Pal.accent;
    public UnitPartProgress actionProgress = UnitPartProgress.actionTime;

    public Interval interval = new Interval();

    public DrawBladePart(){
    }

    @Override
    public void draw(UnitPartParams params){

        PowerArmourUnit unit = params.unit;

        if(unit.BladeTrail == null){
            unit.BladeTrail = new Trail(trailLength);
        }
        if(layer > 0) Draw.z(layer);
        Draw.z(Draw.z() + layerOffset);

        float action = actionProgress.getClamp(params, false);
        float ax = actionX * action, ay = actionY * action, aRot = actionRot * action;

        float prog = progress.getClamp(params, true);
        float mx = moveX * prog + ax, my = moveY * prog + ay, mr = moveRot * prog + rotation + aRot;
        float rot = mr + params.rotation;

        Tmp.v1.set(x + mx, y + my).rotateRadExact((params.rotation - 90) * Mathf.degRad);

        float rx = Tmp.v1.x + params.x, ry = Tmp.v1.y + params.y;

        Tmp.v2.set(Tmp.v1).trnsExact(rot, rad).add(params.x, params.y);

        boolean in;
        if(unit.mounts[0] instanceof MainWeaponMount m){
            in = m.shouldAction;
            float progress = in ? action * (Math.abs(actionRot) / 360f) : 0;
            float w = trailWidth * action;


            if(!state.isPaused()) unit.BladeTrail.update(Tmp.v2.x, Tmp.v2.y);
            unit.BladeTrail.draw(color, w);
            unit.BladeTrail.drawCap(color, w);

           /* Draw.color(color);
            Drawn.fillCirclePercentFade(rx,ry,rx,ry, rad, progress, rot,
            1, 0.6f + 0.35f * Interp.pow2InInverse.apply(Mathf.curve(progress, 0, 0.8f)), 1f);*/
            Draw.reset();
        }
    }
}
