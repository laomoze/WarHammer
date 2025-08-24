package wh.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.*;

public class CritMissileBulletType extends CritBulletType{
    public CritMissileBulletType(){
        super();
    }

    public float[] lengthWidthPans = {
    1.12f, 1.3f, 0.32f,
    1f, 1f, 0.3f,
    0.8f, 0.9f, 0.2f,
    0.5f, 0.8f, 0.15f,
    0.25f, 0.7f, 0.1f,
    };
    public Color[] colors = new Color[]{Color.valueOf("465ab8").a(0.55f), Color.valueOf("66a6d2").a(0.7f), Color.valueOf("89e8b6").a(0.8f), Color.valueOf("cafcbe"), Color.white};
    public float flameWidth = 3.7f, oscScl = 5f, oscMag = 0.06f;
    public int divisions = 15;
    public float flameLength = 10f;
    public float lengthOffset = 10f;
    public Interp lengthInterp = Interp.slope;
    public boolean autoHoming = false;

    @Override
    public void draw(Bullet b){
        super.draw(b);
        float mult = b.fin(lengthInterp);
        float sin = Mathf.sin(Time.time, oscScl, oscMag);
        for(int i = 0; i < colors.length; i++){
            Draw.color(colors[i].write(Tmp.c1).mul(0.9f).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));
            Tmp.v1.trns(b.rotation(), lengthOffset);
            Drawf.flame(b.x-Tmp.v1.x, b.y -Tmp.v1.y, divisions, b.rotation()-180,
            flameLength * lengthWidthPans[i * 3] * (1f - sin),
            flameWidth * lengthWidthPans[i * 3 + 1] * mult * (1f + sin),
            lengthWidthPans[i * 3 + 2]
            );
        }
    }

    @Override
    public void init(Bullet b){
        super.init(b);
    }

    public void lookAt(float angle, Bullet b) {
        b.rotation(Angles.moveToward(b.rotation(), angle, homingPower * Time.delta));
    }

    public void lookAt(float x, float y, Bullet b) {
        lookAt(b.angleTo(x, y), b);
    }


    @Override
    public void updateHoming(Bullet b) {
        //autoHoming时候优先寻找目标，无目标时依然会跟随瞄准方向
        if (homingPower > 0.0001f && b.time >= homingDelay) {
            float realAimX = b.aimX < 0 ? b.data instanceof Position ? ((Position) b.data).getX() : b.x : b.aimX;
            float realAimY = b.aimY < 0 ? b.data instanceof Position ? ((Position) b.data).getY() : b.y : b.aimY;

            Teamc target;
            if (heals()) {
                target = Units.closestTarget(null, realAimX, realAimY, homingRange,
                e -> e.checkTarget(collidesAir, collidesGround) && e.team != b.team && !b.hasCollided(e.id),
                t -> collidesGround && (t.team != b.team || t.damaged()) && !b.hasCollided(t.id)
                );
            } else {
                if (b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team && collidesGround && !b.hasCollided(b.aimTile.build.id)) {
                    target = b.aimTile.build;
                } else {
                    target = Units.closestTarget(b.team, realAimX, realAimY, homingRange, e -> e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id), t -> collidesGround && !b.hasCollided(t.id));
                }
            }

            if(reflectable) return;
            if (target != null && autoHoming) {
                b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower * Time.delta));
            } else {
                @Nullable Unit shooter = null;
                if(b.owner instanceof Unit) shooter = (Unit)b.owner;
                if(b.owner instanceof ControlBlock) shooter = ((ControlBlock)b.owner).unit();
                if (shooter != null) {
                    //if(!Vars.net.client() || shooter.isPlayer()) lookAt(EUGet.pos(shooter.aimX, shooter.aimY), b);
                    if(shooter.isPlayer()) lookAt(shooter.aimX, shooter.aimY, b);
                    else {
                        if (b.data instanceof Position p)
                            lookAt(p.getX(), p.getY(), b);
                        else
                            lookAt(realAimX, realAimY, b);
                    }
                }
            }
        }
    }
    private static class CritMissileData {
        float aimX, aimY;
        Object parentData;
    }
}
