package wh.entities.world.blocks.production;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.part.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import wh.content.*;
import wh.graphics.*;

import static mindustry.Vars.*;

public class PlaRector extends VariableReactor{
    public static final float range = 36f;
    public static final float enemyRange = 15f * 8f;
    public static final float lightingChance = 0.009f;
    public static final float bulletChance = 0.001f;
    public static Color Placolor = WHPal.SkyBlueF.cpy().lerp(WHPal.SkyBlue, 0.3f);
    public float move = 1.7f * tilesize;

    public Seq<DrawPart> parts = new Seq<>(DrawPart.class);

    public PlaRector(String name){
        super(name);

        explosionPuddleAmount = explosionPuddles = 0;
        explodeEffect = Fx.none;
      /*  explosionDamage=3000 * 6;

        explosionRadius = 25;*/
        explosionMinWarmup = 0.5f;
        destroyBullet = WHBullets.plaBreak;
        explodeSound = Sounds.explosionReactor2;
    }

    public static Effect spawnEffect = new Effect(60f, e -> {
        Draw.color(Placolor);
        Lines.stroke(e.fout() * 2f);
        Draw.color(Placolor);
        Lines.poly(e.x, e.y, 4, 8f + e.finpow() * range / 4, 45);
    });
    public static BulletType DrawnBullet = new BulletType(){
        {
            damage = 120f;
            speed = 3f;
            lifetime = 120f;
            hitEffect = WHFx.hitSpark(Placolor, 30f, 5, 12f, 1.5f, 9f);
            trailLength = 12;
            trailWidth = 1.5f;
            trailColor = Placolor;
            despawnEffect = Fx.none;
        }

        @Override
        public void update(Bullet b){
            super.update(b);
            if(b.time > 18f){
                Teamc target = Units.closestTarget(b.team, b.x, b.y, enemyRange,
                unit -> (unit.isGrounded() && collidesGround) || (unit.isFlying() && collidesAir),
                t -> collidesGround
                );
                Position targetTo = target != null ? target : (Position)b.owner;
                float homingPower = target == null ? 0.08f : 0.5f;
                if(targetTo != null){
                    b.vel.setAngle(Mathf.slerpDelta(b.rotation() + 0.01f, b.angleTo(targetTo), homingPower));
                }
            }
        }

        @Override
        public void draw(Bullet b){
            super.draw(b);
            Draw.color(Placolor);
            Drawf.tri(b.x, b.y, 4f, 8f, b.rotation());
            drawTrail(b);
            Draw.reset();
        }
    };

    @Override
    public void load(){
        super.load();
        for(var part : parts){
            part.load(name);
        }
    }


    public class LightninGeneratorBuild extends VariableReactorBuild{
        @Override
        public void draw(){
            if(parts.size > 0){
                for(int i = 0; i < parts.size; i++){
                    var part = parts.get(i);
                    DrawPart.params.set(warmup(), 0f, 0f, 0f, 0f, 0f, x, y, rotation);
                    part.draw(DrawPart.params);
                }
            }
            super.draw();
        }

        @Override
        public void afterDestroyed(){
        }

        @Override
        public void createExplosion(){
            if(shouldExplode()){
                if(explosionDamage > 0){
                    Damage.damage(x, y, explosionRadius * tilesize, explosionDamage);
                }

                if(block.destroyBullet != null){
                    block.destroyBullet.create(this, block.destroyBulletSameTeam ? team : Team.derelict, x, y, Mathf.randomSeed(id(), 360.0F));
                }

                explodeEffect.at(this);
                explodeSound.at(this);

                if(explosionPuddleLiquid != null){
                    for(int i = 0; i < explosionPuddles; i++){
                        Tmp.v1.trns(Mathf.random(360f), Mathf.random(explosionPuddleRange));
                        Tile tile = world.tileWorld(x + Tmp.v1.x, y + Tmp.v1.y);
                        Puddles.deposit(tile, explosionPuddleLiquid, explosionPuddleAmount);
                    }
                }

                if(explosionShake > 0){
                    Effect.shake(explosionShake, explosionShakeDuration, this);
                }
            }
        }

        @Override
        public boolean shouldExplode(){
            return heat > maxHeat * 0.5f;
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(Mathf.chanceDelta(bulletChance * warmup)){
                float random = Mathf.random(0f, 360f);
                for(int i = 0; i < 3; i++){
                    DrawnBullet.create(this, x, y, 120 * i + random);
                }
            }
            if(Mathf.chanceDelta(effectChance / 2 * warmup)){
                spawnEffect.at(x + Mathf.range(block.size * 4), y + Mathf.range(block.size * 4), 0f, Placolor);
            }

            float bx = x, by = y;
            if(efficiency > 0){
                if(wasVisible && Mathf.chanceDelta(lightingChance)){
                    for(float mx : new float[]{move, -move}){
                        for(float my : new float[]{move, -move}){
                            Draw.z(Layer.effect);
                            Draw.alpha(warmup);
                            float x = bx + mx, y = by + my;
                            Vec2 v = new Vec2().set(bx, by);
                            Fx.chainLightning.at(x, y, 3, WHPal.SkyBlueF.cpy().lerp(Pal.water, Mathf.absin(Time.time, 0.3f)), v);
                        }
                    }
                }
            }

        }
    }
}
