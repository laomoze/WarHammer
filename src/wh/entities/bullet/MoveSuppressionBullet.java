package wh.entities.bullet;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.entities.world.blocks.defense.turrets.*;

public class MoveSuppressionBullet extends ContinuousBulletType{
    public float findRange;
    public float findAngle;
    public Color color = Pal.lighterOrange;
    public float fadeTime = 15f;
    public boolean air = true;
    public boolean ground = true;

    public boolean traction = false;

    public float force = 20, scaledForce;

    public MoveSuppressionBullet(float findAngle, float findRange){
        this(findAngle, findRange, 0.85f);
    }

    public MoveSuppressionBullet(float findAngle, float findRange, float scaledForce){
        this.findAngle = findAngle;
        this.findRange = findRange;
        this.scaledForce = scaledForce;
        speed = 0;
        damage = 0;
        collides = collidesTiles = collidesGround = collidesAir = false;
        keepVelocity = hittable = absorbable = false;
        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        smokeEffect = Fx.none;
        drawSize = findRange * 1.5f;
        maxRange = range = findRange;
    }

    public MoveSuppressionBullet(){
        this(45, 160);
    }

    @Override
    protected float calculateRange(){
        return findRange;
    }

    @Override
    public void update(Bullet b){
        float in = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f);

        Units.nearbyEnemies(b.team, b.x, b.y, findRange * in, other -> {
            if(other.team != b.team && other.hittable() &&
            Angles.within(b.rotation(), b.angleTo(other), (findAngle * in) / 2)
            && other.checkTarget(air, ground)){
                if(traction)
                    Tmp.v3.set(other).sub(b).nor().scl(scaledForce * (other.dst(b) / findRange) * in).limit(force);
                else Tmp.v3.set(b).sub(other).limit((force + (1f - other.dst(b) / findRange) * scaledForce) * in);
                other.impulseNet(Tmp.v3);
            }
        });

       /* if(suppressionRange > 0){
            Vars.indexer.allBuildings(b.x, b.y, findRange * in, other -> {
                if(other.team != b.team && Angles.within(b.rotation(), findRange * in, (findAngle * in) / 2)){
                    Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, new Vec2(b.x, b.y), suppressColor);
                }
            });
        }*/
    }

    @Override
    public void applyDamage(Bullet b){
    }

    @Override
    public void draw(Bullet b){
        float in = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f);
        float range = findRange * in;
        float angle = findAngle * in;
        Drawf.light(b.x, b.y, range / 0.8f, color, 0.8f);
        Draw.color(color);
        Draw.z(Layer.buildBeam);
        Draw.alpha(0.8f);
        Fill.circle(b.x, b.y, 4 * in);
        Fill.arc(b.x, b.y, range, angle / 360, b.rotation() - angle / 2);
        Draw.alpha(1);
    }
}
