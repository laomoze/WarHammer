package wh.entities.bullet.laser;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.pooling.*;
import arc.util.pooling.Pool.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.distribution.MassDriver.*;

public class DelayedPointBulletType extends BulletType{
    protected static float cdist = 0f;
    protected static Position result;
    public Color[] colors = {new Color(1, 1, 1, 0f).a(-5f), new Color(1, 1, 1, 1f), new Color(1, 1, 1, 1f)};
    protected static float lengthFalloff = 0.5f;
    public boolean square = false;
    public static Effect laser = new Effect(60f, 2000f, b -> {
        if(!(b.data instanceof DPBData data)) return;
        Position target = data.target;
        Color[] colors = data.c;
        boolean square = data.square;

        float tX = target.getX();
        float tY = target.getY();
        float cwidth = b.rotation;
        float compound = 1f;

        for(int i = 0; i < colors.length; i++){
            Color color = colors[i];
            Draw.color(color);
            Lines.stroke((cwidth *= lengthFalloff) * b.fout());
            Lines.line(b.x, b.y, tX, tY, false);

            if(square){
                float r = b.rotation + 45f;
                Fill.square(b.x, b.y, 1.25f * cwidth * b.fout(), r);
                Fill.square(tX, tY, 1.25f * cwidth * b.fout(), r);
            }else{
                Fill.circle(b.x, b.y, 1.25f * cwidth * b.fout());
                Fill.circle(tX, tY, 1.25f * cwidth * b.fout());
            }

            compound *= lengthFalloff;
        }
        Draw.reset();

        Drawf.light(b.x, b.y, tX, tY, cwidth * 1.4f * b.fout(), colors[0], 0.6f);
    }).followParent(false);
    public float errorCorrectionRadius = 12;
    public float width = 8f;
    public float trailSpacing = 10f;
    public float delayEffectLifeTime = 30f;

    public DelayedPointBulletType(){
        scaleLife = true;
        speed = 0.001f;
        collides = false;
        reflectable = false;
        keepVelocity = false;
        hittable = absorbable = false;
        despawnHit = false;
        setDefaults = false;
    }

    @Override
    public void init(){
        super.init();

        lifetime = range / speed;
        drawSize = range * 2f;
    }

    @Override
    public void init(Bullet b){

        float px = b.x + b.lifetime * b.vel.x,
        py = b.y + b.lifetime * b.vel.y,
        rot = b.rotation();

        cdist = 0f;
        result = null;

        Building absorber = Damage.findAbsorber(b.team, b.x, b.y, px, py);
        if(absorber != null){
            result = absorber;
            cdist = b.dst(absorber) - absorber.hitSize();
        }else{

            Units.nearbyEnemies(b.team, px - errorCorrectionRadius, py - errorCorrectionRadius, errorCorrectionRadius * 2f, errorCorrectionRadius * 2f, e -> {
                if(e.dead() || !e.checkTarget(collidesAir, collidesGround) || !e.hittable()) return;

                e.hitbox(Tmp.r1);
                if(!Tmp.r1.contains(px, py)) return;

                float dst = e.dst(px, py) - e.hitSize;
                if((result == null || dst < cdist)){
                    result = e;
                    cdist = dst;
                }
            });
        }
        if(result == null) result = new Vec2(px, py);

        DPBData data = Pools.obtain(DPBData.class, DPBData::new);
        data.target = result;
        data.c = colors;
        data.square = square;

        b.data = data;

        b.lifetime = delayEffectLifeTime;

        b.rotation(rot);

        super.init(b);
    }

    @Override
    public void draw(Bullet b){
        if(!(b.data instanceof DPBData data)) return;
        Position target = data.target;

        float tX = target.getX();
        float tY = target.getY();
        float cwidth = width / 1.4f * b.fout();
        float compound = 1f;

        for(int i = 0; i < colors.length; i++){
            /* Draw.color(Tmp.c1.set(hitColor).lerp(colors[i], i * 0.3f + 0.1f));*/
            Color color = colors[i];
            Draw.color(color);
            float s = (cwidth *= lengthFalloff) * (b.fout() * 2 + 1) / 3;
            Lines.stroke(s);
            Lines.line(b.x, b.y, tX, tY, false);

            if(square){
                float r = b.rotation() + 45f;
                Fill.square(b.x, b.y, 1.25f * cwidth * b.fout(), r);
                Fill.square(tX, tY, 1.25f * cwidth * b.fout(), r);
                Draw.color(color);
                Fill.square(Mathf.lerp(b.x, tX, b.fin()), Mathf.lerp(b.y, tY, b.fin()), 2 * (s + width / 8f) * (1 + b.fout()) * 0.5f * b.fin(), r);
            }else{
                Fill.circle(b.x, b.y, 1.5f * s);
                Fill.circle(tX, tY, 1.5f * s);
                Draw.color(color);
                Fill.circle(Mathf.lerp(b.x, tX, b.fin()), Mathf.lerp(b.y, tY, b.fin()), 2 * (s + width / 8f) * (1 + b.fout()) * 0.5f * b.fin());
            }

            compound *= lengthFalloff;
        }
        Draw.reset();

        Drawf.light(b.x, b.y, tX, tY, cwidth * 1.4f * b.fout(), colors[0], 0.6f);
    }

    @Override
    public void despawned(Bullet b){
        if(!(b.data instanceof DPBData data) || !b.isAdded()) return;
        Position target = data.target;

        float tX = target.getX();
        float tY = target.getY();

        float rot = b.rotation();
        if(trailChance > 0) Geometry.iterateLine(0f, b.x, b.y, tX, tY, trailSpacing, (x1, y1) -> {
            trailEffect.at(x1, y1, rot, trailColor);
        });

        laser.at(b.x, b.y, width, hitColor, target);
        b.set(tX, tY);

        if(target instanceof Hitboxc){
            hitEntity(b, (Hitboxc)target, 0);
            if(!despawnHit) hit(b, tX, tY);
        }else if(collidesTiles){
            Building build = Vars.world.buildWorld(tX, tY);
            if(build != null && build.team != b.team){
                build.collision(b);
                if(!despawnHit) hit(b, build.x, build.y);
            }
        }else if(despawnHit) hit(b, tX, tY);

        b.hit = true;
        super.despawned(b);
    }

    public static class DPBData implements Poolable{
        Position target;
        Color[] c;
        boolean square;

        @Override
        public void reset(){
            target = null;
            c = null;
            square = false;
        }
    }
}
