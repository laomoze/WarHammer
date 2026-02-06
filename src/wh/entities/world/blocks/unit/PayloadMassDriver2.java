package wh.entities.world.blocks.unit;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.payloads.*;

import static mindustry.Vars.world;

public class PayloadMassDriver2 extends PayloadMassDriver{

    public PayloadMassDriver2(String name){
        super(name);
    }

    public class PayloadMassDriver2Build extends PayloadDriverBuild{
        @Override
        public void draw(){
            float
            tx = x + Angles.trnsx(turretRotation + 180f, reloadCounter * knockback),
            ty = y + Angles.trnsy(turretRotation + 180f, reloadCounter * knockback), r = turretRotation - 90;

            Draw.rect(baseRegion, x, y);

            //draw input
            for(int i = 0; i < 4; i++){
                if(blends(i) && i != rotation){
                    Draw.rect(inRegion, x, y, (i * 90) - 180);
                }
            }

            Draw.rect(outRegion, x, y, rotdeg());

            if(payload != null){
                updatePayload();

                if(effectDelayTimer <= 0){
                    Draw.z(loaded ? Layer.blockOver + 0.2f : Layer.blockOver);
                    payload.draw();
                }
            }

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);

            Draw.z(Layer.turret);
            //Fix rotation
            Drawf.shadow(region, tx - (size / 2f), ty - (size / 2f), r);
            Tmp.v1.trns(turretRotation, 0, -(curSize / 2f - grabWidth));
            Tmp.v2.trns(turretRotation, -Math.max(curSize / 2f - grabHeight - length, 0f), 0f);
            float rx, ry, lx, ly;

            rx = tx + Tmp.v1.x + Tmp.v2.x;
            ry = ty + Tmp.v1.y + Tmp.v2.y;
            lx = tx - Tmp.v1.x + Tmp.v2.x;
            ly = ty - Tmp.v1.y + Tmp.v2.y;

            Draw.rect(capOutlineRegion, tx, ty, r);
            Draw.rect(leftOutlineRegion, lx, ly, r);
            Draw.rect(rightOutlineRegion, rx, ry, r);

            Draw.rect(leftRegion, lx, ly, r);
            Draw.rect(rightRegion, rx, ry, r);
            Draw.rect(capRegion, tx, ty, r);

            Draw.z(Layer.effect);

            if(charge > 0 && linkValid()){
                Building link = world.build(this.link);

                float fin = Interp.pow2Out.apply(charge / chargeTime), fout = 1f - fin, len = length * 1.8f, w = curSize / 2f + 7f * fout;
                Vec2 right = Tmp.v1.trns(turretRotation, len, w);
                Vec2 left = Tmp.v2.trns(turretRotation, len, -w);

                Lines.stroke(fin * 1.2f, Pal.accent);
                Lines.line(x + left.x, y + left.y, link.x - right.x, link.y - right.y);
                Lines.line(x + right.x, y + right.y, link.x - left.x, link.y - left.y);

                for(int i = 0; i < 4; i++){
                    Tmp.v3.set(x, y).lerp(link.x, link.y, 0.5f + (i - 2) * 0.1f);
                    Draw.scl(fin * 1.1f);
                    Draw.rect(arrow, Tmp.v3.x, Tmp.v3.y, turretRotation);
                    Draw.scl();
                }

                Draw.reset();
            }
        }
    }
}
