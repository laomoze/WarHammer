package wh.content;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.graphics.*;
import wh.util.*;

import static wh.content.WHFx.lightningFade;

public class WHBulletsOther{
    public static BulletType IonizeInterval;
    public static void load(){
    IonizeInterval = new BulletType(){{
        buildingDamageMultiplier = 0.2f;
        lifetime = 120;
        speed = 3;
        damage = 90;
        keepVelocity = false;
        lightningColor=trailColor = hitColor= WHItems.resonantCrystal.color.cpy();
        trailWidth = 2;trailLength = 18;
        splashDamage = 50;
        splashDamageRadius = 32;
        despawnEffect=hitEffect = WHFx.square(WHItems.resonantCrystal.color.cpy(), 60, 4, 30, 3);
        buildingDamageMultiplier = 0.2f;
        homingRange = 100;
        lightningDamage=50;
        lightning = 2;
        lightningLength = 5;
    }
        @Override
        public void update(Bullet b) {
            super.update(b);
            if(!(b instanceof InI Interval)) return;
            Teamc target = Units.closestTarget(b.team, b.x, b.y, homingRange,
            e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
            t -> t != null && collidesGround && !b.hasCollided(t.id));
            if(b.time < 30 || target == null){
                b.initVel(b.rotation(), speed * 0.4f * Math.max(0, 1 - b.fin() * 3));
            } else {
                b.initVel(b.angleTo(target), speed);
            }
            for(int i = 0; i < 2; i++){
                if(!Vars.headless) {
                    if (Interval.trails[i] == null) Interval.trails[i] = new Trail(22);
                    Interval.trails[i].length = 22;
                }
                float dx = WHUtils.dx(b.x, 5, (b.time * (8 - (i % 2 == 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 180 * i),
                dy = WHUtils.dy(b.y, 5, (b.time * (8 - (i % 2 != 0 ? 0.6f : 0))) + Mathf.randomSeed(b.id, 360) + 180 * i);
                if(!Vars.headless) Interval.trails[i].update(dx, dy, trailInterp.apply(b.fin()) * (1 + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0)));
                if(Interval.vs[i] != null) Interval.vs[i].set(dx, dy);
            }
        }

        @Override
        public void hitEntity(Bullet b, Hitboxc entity, float health){
            super.hitEntity(b, entity, health);
            if(entity instanceof Unit u && u.type != null) {
                float dmg = b.damage * (1 +Mathf.clamp( u.type.armor/10f,1,3));
                u.damagePierce(dmg);
                }
        }

        @Override
        public void draw(Bullet b) {
            super.draw(b);
            float vel = Math.max(0, b.vel.len()/speed);
            float out = b.time > b.lifetime - 12 ?  (b.lifetime - b.time)/12 : 1;

            Draw.color(trailColor);
            Drawf.tri(b.x, b.y, 3.5f, 6.5f * vel, b.rotation());
            Fill.circle(b.x, b.y, 4 * (1 - vel) * out);

            if(!(b instanceof InI Interval)) return;
            float z = Draw.z();
            Draw.z(z - 1e-4f);
            for(int i = 0; i < 2; i++){
                if ( Interval.trails[i] != null) {
                    Interval.trails[i].draw( trailColor , 1.2f * (1 - vel) * out);
                }
                if(Interval.vs[i] != null){
                    Draw.color(trailColor);
                    Fill.circle(Interval.vs[i].x, Interval.vs[i]. y, 1.2f * (1 - vel) * out);
                }
            }
            Draw.z(z);
        }

        @Override
        public void drawTrail(Bullet b) {
            if(trailLength > 0 && b.trail != null){
                float z = Draw.z();
                Draw.z(z - 1e-4f);
                b.trail.draw(trailColor, 2.9f);
                Draw.z(z);
            }
        }

        @Override
        public void init(Bullet b) {
            super.init(b);
            if(!(b instanceof InI Interval)) return;
            for(int i = 0; i < 2; i++){
                Interval.vs[i] = new Vec2();
            }
        }

        @Override
        public boolean testCollision(Bullet bullet, Building tile) {
            return bullet.time > 30 && super.testCollision(bullet, tile);
        }

        @Override
        public @Nullable Bullet create(
        @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
        float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
        ){
            InI bullet = InI.create();

            for(int i = 0; i < 2; i++){
                if (bullet.trails[i] != null) {
                    bullet.trails[i].clear();
                }
            }
            return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
        }
    };
}
public static class InI extends Bullet{
    @Nullable
    public Trail[] trails = new Trail[6];

    public Vec2[] vs = new Vec2[6];

    public static InI create() {
        return Pools.obtain(InI.class, InI::new);
    }
    }
}
