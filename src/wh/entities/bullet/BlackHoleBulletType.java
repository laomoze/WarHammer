package wh.entities.bullet;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.unit.*;
import wh.content.*;
import wh.graphics.*;
import wh.util.*;

import static mindustry.Vars.*;

/** @author guiy **/
public class BlackHoleBulletType extends BulletType{
    public float inRad, outRad;
    public int minLength = 13, midLength = 18, maxLength = 25;
    public float minWidth = 1, maxWidth = 4;
    public float minSpeed = 0.5f, midSpeed = 0.7f, maxSpeed = 1.8f;

    public float impulse = 6.67f * 1e-4f;

    public boolean damageUnits = true;
    public float damageInterval = 12;

    //吸积盘颜色
    public boolean useTeamColor = false;
    public Color accColor = WHItems.sealedPromethium.color.cpy();
    //吸积盘基数
    public int amount = 1;
    public float fadeTime = 72;
    public float maxAbsorbBulletDamage = 200;
    public float maxAbs = 16f;


    public BlackHoleBulletType(){
        speed = 0f;
        damage = 0;
        lifetime = 300;
        keepVelocity = collides = collidesGround = collidesAir = absorbable = hittable = false;

        despawnEffect = healEffect = Fx.none;
        hitEffect = WHFx.lightningHitSmall;
    }

    @Override
    public void draw(Bullet b){
        float in = b.time <= b.lifetime - fadeTime ? Math.min(b.time / 60f, 1) : (b.lifetime - b.time) / fadeTime;
        in = Interp.fastSlow.apply(in);
        MainRenderer.addBlackHole(b.x, b.y, inRad * 0.5f * in, outRad * 1.3f * in, Math.min(1, in + 0.1f));

        super.draw(b);
    }

    @Override
    public void drawLight(Bullet b){
        //no light
    }

    @Override
    public void update(Bullet b){
        updateBulletInterval(b);
        float in = b.time <= b.lifetime - fadeTime ? Math.min(b.time / 60f, 1) : (b.lifetime - b.time) / fadeTime;
        in = Interp.fastSlow.apply(in);

        float finalIn = in;
        Units.nearbyEnemies(b.team, b.x, b.y, outRad * 2, e -> {
            if(e != null && e.targetable(b.team)){
                float p = F(b.x, b.y, e.x, e.y, impulse * Math.abs(e.mass() + 1f), outRad * 2);
                if(e.type instanceof MissileUnitType) p = F(b.x, b.y, e.x, e.y, impulse * 2000, outRad * 2);
                e.impulseNet(Tmp.v3.set(e).sub(b).nor().scl(-p * Time.delta * finalIn));
                e.vel.limit(5);
            }
        });
        if(damageUnits && b.timer(3, damageInterval))
            Units.nearbyEnemies(b.team, b.x, b.y, inRad*1.3f, e -> {
                if(e != null && e.targetable(b.team) && e.team != b.team){
                    hitEffect.at(e.x, e.y, 0, hitColor);
                    if(pierceArmor){
                        e.damagePierce(damage * b.damageMultiplier());
                    }else{
                        e.damage(damage * b.damageMultiplier());
                    }
                }
            });

       /* Groups.bullet.intersect(b.x - outRad * 2, b.y - outRad * 2, outRad * 4, outRad * 4, bullet -> {
            if(bullet != null && bullet.within(b, outRad * 2) && bullet.team != b.team && bullet.type != null && (bullet.type.absorbable || bullet.type.hittable)){
                float p = F(b.x, b.y, bullet.x, bullet.y, impulse * 10, outRad * 2);
                Vec2 v = Tmp.v4.set(bullet).sub(b).nor().scl(-p * Time.delta * finalIn);
                bullet.vel.add(v.x, v.y);
                bullet.vel.limit(5);
                if(bullet.within(b, inRad)) {
                    bullet.damage =0;
                    bullet.remove();
                }
            }
        });*/


        if(!Vars.headless && (Core.settings != null) && b.time <= b.lifetime - fadeTime && Mathf.randomBoolean(0.5f))
            for(int i = 0; i < amount; i++){
                ateData data = ateData.create();
                float outRDI = i % 2 == 0 ? outRad * 1.2f : outRad;
                data.width = Mathf.random(minWidth, maxWidth) * in;
                data.inRad = inRad * 0.9f * in;
                data.outRad = Math.max(data.inRad, Mathf.random(inRad * 1.1f, outRDI) * in);
                data.speed = data.outRad > inRad * 1.5f ? Mathf.random(minSpeed, midSpeed) : Mathf.random(midSpeed * 2f, maxSpeed);
                data.length = data.speed < midSpeed ? Mathf.random(midLength, maxLength) : Mathf.random(minLength, midLength);
                data.owner = b;
                if(i % 2 == 0) data.out = true;
                AccretionDiskEffect.at(b.x, b.y, 0, useTeamColor ? (b.team != null ? b.team.color.cpy() : accColor) : accColor, data);
            }

        if(!(b instanceof BlackHole a)) return;
        Groups.bullet.intersect(b.x - outRad, b.y - outRad, outRad * 2, outRad * 2, bullet -> {
            if(bullet.type != null && bullet.team != b.team && b.within(bullet, outRad) &&
            ((bullet.type.absorbable || bullet.type.hittable) && bullet.type.reflectable) &&
            bullet.damage < maxAbsorbBulletDamage && a.enemyBullets.size < maxAbs){
                a.enemyBullets.addUnique(bullet);
            }
        });

        a.enemyBullets.removeAll(e -> e == null || !e.isAdded());

        if(a.enemyBullets.size > 0){
            for(Bullet e : a.enemyBullets){
                int ta = Mathf.randomSeed(e.id, 80, 150);
                float tg = Mathf.randomSeed(e.id, 360);
                float angle = b.time * 2 * (ta % 2 == 0 ? 1 : -1) + tg;
                float tx = WHUtils.ellipseXY(b.x, b.y, ta, ta / 2.5f, tg, angle, 0);
                float ty = WHUtils.ellipseXY(b.x, b.y, ta, ta / 2.5f, tg, angle, 1);
                WHUtils.movePoint(e, tx, ty, 0.1f);
                e.rotation(e.angleTo(tx, ty));
                e.initVel(e.rotation(), 0);
                e.team(b.team);
                e.owner(b.owner);
                e.time = e.lifetime - 5;
            }
        }
    }

    public float F(float x, float y, float tx, float ty, float G, float r){
        float dst = Mathf.dst(x, y, tx, ty);
        float ptr = 1 - dst / Mathf.pow(r, 2);
        return G * ptr;
    }

    @Override
    public void despawned(Bullet b){
        super.despawned(b);
        if(!(b instanceof BlackHole a)) return;
        if(a.enemyBullets.size > 0){
            for(Bullet e : a.enemyBullets){
                if(e.type != null)
                    e.initVel(e.rotation(), e.type.speed);
            }
        }
    }

    @Override
    public @Nullable Bullet create(
    @Nullable Entityc owner, @Nullable Entityc shooter, Team team, float x, float y, float angle, float damage, float velocityScl,
    float lifetimeScl, Object data, @Nullable Mover mover, float aimX, float aimY, @Nullable Teamc target
    ){
        BlackHole bullet = BlackHole.create();
        bullet.enemyBullets.clear();

        return WHUtils.anyOtherCreate(bullet, this, shooter, owner, team, x, y, angle, damage, velocityScl, lifetimeScl, data, mover, aimX, aimY, target);
    }


    public static class BlackHole extends Bullet{
        public Seq<Bullet> enemyBullets = new Seq<>();

        public static BlackHole create(){
            return Pools.obtain(BlackHole.class, BlackHole::new);
        }
    }

    public static class ateData implements Pool.Poolable{
        public float width;
        public int length;
        public float inRad, outRad, speed;

        public transient Trail trail;

        public Bullet owner;

        public boolean out = false;

        public static ateData create(){
            return Pools.obtain(ateData.class, ateData::new);
        }

        @Override
        public void reset(){
            width = 0;
            length = 0;
            inRad = outRad = speed = 0;
            trail = null;
            owner = null;
            out = false;
        }
    }

    Effect AccretionDiskEffect = new Effect(60, e -> {
        if(headless || !(e.data instanceof ateData data) || data.owner == null) return;

        float fin = data.out ? e.finpow() : e.foutpow();
        float fout = data.out ? e.foutpow() : e.finpow();
        //float fout = 1 - fin;

        float start = Mathf.randomSeed(e.id, 360f);
        var b = data.owner;

        float ioRad = data.outRad - (data.outRad - data.inRad) * fin;
        float rad = data.speed * e.time * 6;
        float dx = WHUtils.dx(b.x, ioRad, start - rad),
        dy = WHUtils.dy(b.y, ioRad, start - rad);

        if(data.trail == null) data.trail = new Trail(data.length);
        float dzin = data.out && e.time > e.lifetime - 10 ? Interp.pow2Out.apply((e.lifetime - e.time) / 10) : fin;
        data.trail.length = data.length;
        //data.trail.length = (int) (data.length * dzin);

        if(!state.isPaused()) data.trail.update(dx, dy, 2.5f);

        float z = Draw.z();
        Draw.z(Layer.effect - 19 * fout);
        //Draw.z(Layer.max - 1);
        data.trail.draw(Tmp.c3.set(e.color).shiftValue(-e.color.value() * fout), data.width * dzin);
        data.trail.drawCap(Tmp.c3.set(e.color).shiftValue(-e.color.value() * fout), data.width * dzin);
        //data.trail.draw(e.color, data.width);
        Draw.z(z);
    });
}
