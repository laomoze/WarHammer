//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.entities.bullet;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.bullet.*;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import wh.content.WHFx;
import wh.graphics.PositionLightning;
import wh.struct.Vec2Seq;

public class TrailFadeBulletType extends BasicBulletType{
    protected static final Vec2 v1 = new Vec2();
    protected static final Vec2 v2 = new Vec2();
    protected static final Vec2 v3 = new Vec2();
    protected static final Rand rand = new Rand();
    public int tracers;
    public int tracerFadeOffset;
    public int tracerStrokeOffset;
    public float tracerStroke;
    public float tracerSpacing;
    public float tracerRandX;
    public float tracerUpdateSpacing;
    public boolean addBeginPoint;
    public boolean hitBlinkTrail;
    public boolean despawnBlinkTrail;

    public TrailFadeBulletType() {
         tracers = 2;
         tracerFadeOffset = 10;
         tracerStrokeOffset = 15;
         tracerStroke = 3.0F;
         tracerSpacing = 8.0F;
         tracerRandX = 6.0F;
         tracerUpdateSpacing = 0.3F;
         addBeginPoint = false;
         hitBlinkTrail = true;
         despawnBlinkTrail = false;
    }

    public TrailFadeBulletType(float speed, float damage, String bulletSprite) {
        super(speed, damage, bulletSprite);
         tracers = 2;
         tracerFadeOffset = 10;
         tracerStrokeOffset = 15;
         tracerStroke = 3.0F;
         tracerSpacing = 8.0F;
         tracerRandX = 6.0F;
         tracerUpdateSpacing = 0.3F;
         addBeginPoint = false;
         hitBlinkTrail = true;
         despawnBlinkTrail = false;
         impact = true;
    }

    public TrailFadeBulletType(float speed, float damage) {
        this(speed, damage, "bullet");
    }

    @Override
    public void despawned(Bullet b){
        if(!Vars.headless && (b.data instanceof Vec2Seq[])){
            Vec2Seq[] pointsArr = (Vec2Seq[])b.data();
            for(Vec2Seq points : pointsArr){
                points.add(b.x, b.y);
                if(despawnBlinkTrail || (b.absorbed && hitBlinkTrail)){
                    PositionLightning.createBoltEffect(hitColor, tracerStroke * 2f, points);
                    Vec2 v = points.firstTmp();
                    WHFx.lightningHitSmall.at(v.x, v.y, hitColor);
                }else{
                    points.add(tracerStroke, tracerFadeOffset);
                    WHFx.lightningFade.at(b.x, b.y, tracerStrokeOffset, hitColor, points);
                }
            }

            b.data = null;
        }

        super.despawned(b);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health){
        super.hitEntity(b, entity, health);

        hit(b);
    }

    @Override
    public void hit(Bullet b){
        super.hit(b);

        if(Vars.headless || !(b.data instanceof Vec2Seq[]))return;
        Vec2Seq[] pointsArr = (Vec2Seq[])b.data();
        for(Vec2Seq points : pointsArr){
            points.add(b.x, b.y);
            if(hitBlinkTrail){
                PositionLightning.createBoltEffect(hitColor, tracerStroke * 2f, points);
                Vec2 v = points.firstTmp();
                WHFx.lightningHitSmall.at(v.x, v.y, hitColor);
            }else{
                points.add(tracerStroke, tracerFadeOffset);
                WHFx.lightningFade.at(b.x, b.y, tracerStrokeOffset, hitColor, points);
            }
        }

        b.data = null;
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        if(Vars.headless && trailLength > 0)return;
        Vec2Seq[] points = new Vec2Seq[tracers];
        for(int i = 0; i < tracers; i++){
            Vec2Seq p = new Vec2Seq();
            if(addBeginPoint)p.add(b.x, b.y);
            points[i] = p;
        }
        b.data = points;
    }



    @Override
    public void update(Bullet b){
        super.update(b);
        if(!Vars.headless && b.timer(2, tracerUpdateSpacing)){
            if(!(b.data instanceof Vec2Seq[]))return;
            Vec2Seq[] points = (Vec2Seq[])b.data();
            for(Vec2Seq seq : points){
                v2.trns(b.rotation(), 0, rand.range(tracerRandX));
                v1.setToRandomDirection(rand).scl(tracerSpacing);
                seq.add(v3.set(b.x, b.y).add(v1).add(v2));
            }
        }
    }

    @Override
    public void drawTrail(Bullet b){
        super.drawTrail(b);

        if((b.data instanceof Vec2Seq[])){
            Vec2Seq[] pointsArr = (Vec2Seq[])b.data();
            for(Vec2Seq points : pointsArr){
                if(points.size() < 2)return;
                Draw.color(hitColor);
                for(int i = 1; i < points.size(); i++){
//					Draw.alpha(((float)(i + fadeOffset) / points.size));
                    Lines.stroke(Mathf.clamp((i + tracerFadeOffset / 2f) / points.size() * (tracerStrokeOffset - (points.size() - i)) / tracerStrokeOffset) * tracerStroke);
                    Vec2 from = points.setVec2(i - 1, Tmp.v1);
                    Vec2 to = points.setVec2(i, Tmp.v2);
                    Lines.line(from.x, from.y, to.x, to.y, false);
                    Fill.circle(from.x, from.y, Lines.getStroke() / 2);
                }

                Fill.circle(points.peekTmp().x, points.peekTmp().y, tracerStroke);
            }
        }
    }
}
