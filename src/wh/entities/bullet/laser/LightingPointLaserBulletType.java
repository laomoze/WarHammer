package wh.entities.bullet.laser;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.graphics.*;
import wh.util.*;

import static mindustry.Vars.state;
import static wh.util.WHUtils.rand;

public class LightingPointLaserBulletType extends PointLaserBulletType{
    public float width = 20f;
    public boolean drawLightning = false;
    /*  public  Color[] colors = {c.a(0.3f), c.a(0.7f), c.a(1), Pal.coalBlack};*/

    @Override
    public void draw(Bullet b){
        if(!(b instanceof PLBullet plBullet)) return;
        if(b.timer(3, 10) && !state.isPaused() && drawLightning){
            PositionLightning.createEffect(b, Tmp.v1.set(b.aimX, b.aimY), hitColor, 1, Mathf.random(1, 2));
        }

        float fadeTime = 8f;
        float fout2 = b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f;

        /*for(int i = 0; i < colors.length; i++){
            Draw.color(colors[i]);
            Drawn.basicLaser(b.x, b.y, b.aimX, b.aimY, width * fout2 * (1 - i * 0.12f) * (1f - oscMag + Mathf.absin(Time.time, oscScl, oscMag)));
        }*/

        Draw.z(Layer.bullet);
        Draw.color(hitColor);
        Lines.stroke(fout2 * plBullet.chargeTimer * width / 3 * (1 + Mathf.sin(Time.time, 12, 0.3f)));
        float num = 4;
        float phaseOffset = 360 / num;
        rand.setSeed(b.id);
        for(int i = 0; i < num; i++){
            float a = phaseOffset * i + Time.time * 0.5f;
            Tmp.v1.trns(a, width / 3.5f);
            float len = b.dst(b.aimX, b.aimY);
            float lx = Mathf.lerp(b.x, b.aimX, plBullet.chargeTimer) + Tmp.v1.x,
            ly = Mathf.lerp(b.y, b.aimY, plBullet.chargeTimer) + Tmp.v1.y;
            float random = rand.random(0.5f, 1f);
            Drawn.drawSine2Modifier(b.x + Tmp.v1.x, b.y + Tmp.v1.y, lx, ly,
            Time.time * 0.7f * random, 8, 0.8f,
            phaseOffset * random * Mathf.degreesToRadians, width - width * 0.6f * plBullet.chargeTimer * random,
            len / 5, ((x1, y1) -> {
                Fill.circle(x1, y1, Lines.getStroke());
            }));
            Fill.circle(lx, ly, Lines.getStroke());
        }

        Draw.reset();

    }

    @Override
    public void update(Bullet b){
        if(!(b instanceof PLBullet plBullet)) return;
        if(plBullet.chargeTimer >= 0.99f) super.update(b);
    }

    @Override
    public @Nullable Bullet create(
    @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
    float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
    ){
        PLBullet bullet = PLBullet.create();
        return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
    }

    public static class PLBullet extends Bullet{
        public float chargeTimer;

        @Override
        public void reset(){
            super.reset();
            chargeTimer = 0;
        }

        public static PLBullet create(){
            return Pools.obtain(PLBullet.class, PLBullet::new);
        }
    }
}
