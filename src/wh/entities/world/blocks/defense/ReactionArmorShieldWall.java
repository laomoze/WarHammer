package wh.entities.world.blocks.defense;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class ReactionArmorShieldWall extends ReactionArmorWall{
    public float shieldHealth = 900f;
    public float breakCooldown = 60f * 10f;
    public float regenSpeed = 2f;

    public Color glowColor = Color.valueOf("ff7531").a(0.5f);
    public float glowMag = 0.6f, glowScl = 8f;

    public TextureRegion glowRegion;

    public Sound breakSound = Sounds.shieldBreak;
    public Sound hitSound = Sounds.shieldHit;
    public Effect absorbEffect = Fx.absorb;
    public float hitSoundVolume = 0.12f;
    public int sides = 4;
    public float shieldRotation = 45f;
    public float range = 2;

    protected static ReactionArmorShieldWall paramBlock;
    protected static ReactionArmorShieldWallBuild paramEntity;
    protected static final Cons<Bullet> shieldConsumer = bullet -> {
        if(bullet.team != paramEntity.team && bullet.type.absorbable && !bullet.absorbed &&
        Intersector.isInRegularPolygon(paramBlock.sides, paramEntity.x, paramEntity.y, paramEntity.realRadius(), paramBlock.shieldRotation, bullet.x, bullet.y)){

            bullet.absorb();
            paramBlock.hitSound.at(bullet.x, bullet.y, 1f + Mathf.range(0.1f), paramBlock.hitSoundVolume);
            paramBlock.absorbEffect.at(bullet);
            paramEntity.hit = 1f;
            paramEntity.shield -= bullet.type.shieldDamage(bullet);
        }
    };

    public ReactionArmorShieldWall(String name){
        super(name);
        update = true;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.shieldHealth, shieldHealth);
    }

    @Override
    public void load(){
        super.load();
        glowRegion = Core.atlas.find(name + "-glow");
    }

    public class ReactionArmorShieldWallBuild extends ReactionArmorWallBuild{
        public float shield = shieldHealth, shieldRadius = 0f;
        public float breakTimer;

        @Override
        public void draw(){
            Draw.rect(block.region, x, y);

            if(shieldRadius > 0){
                float radius = shieldRadius * tilesize * range;

                Draw.z(Layer.shields);

                Draw.color(team.color, Color.white, Mathf.clamp(hit));

                if(renderer.animateShields){
                    Fill.square(x, y, radius);
                }else{
                    Lines.stroke(1.5f);
                    Draw.alpha(0.09f + Mathf.clamp(0.08f * hit));
                    Fill.square(x, y, radius);
                    Draw.alpha(1f);
                    Lines.poly(x, y, 4, radius, 45f);
                    Draw.reset();
                }

                Draw.reset();

                Drawf.additive(glowRegion, glowColor, (1f - glowMag + Mathf.absin(glowScl, glowMag)) * shieldRadius, x, y, 0f, Layer.blockAdditive);
            }
        }

        public void deflectBullets(){
            float realRadius = realRadius();

            if(realRadius > 0 && !broken()){
                paramBlock = ReactionArmorShieldWall.this;
                paramEntity = this;
                Groups.bullet.intersect(x - realRadius, y - realRadius, realRadius * 2f, realRadius * 2f, shieldConsumer);
            }
        }

        public float realRadius(){
            return shieldRadius * tilesize * range;
        }

        @Override
        public void updateTile(){
            if(breakTimer > 0){
                breakTimer -= Time.delta;
            }else{
                //regen when not broken
                shield = Mathf.clamp(shield + regenSpeed * edelta(), 0f, shieldHealth);
            }

            if(hit > 0){
                hit -= Time.delta / 10f;
                hit = Math.max(hit, 0f);
            }

            shieldRadius = Mathf.lerpDelta(shieldRadius, broken() ? 0f : 1f, 0.12f);

            if(shield <= 0.00001f){
                shield = 0;
                breakTimer = breakCooldown;
                breakSound.at(x, y);
            }

            deflectBullets();
        }

        public boolean broken(){
            return breakTimer > 0 || !canConsume();
        }

        @Override
        public void pickedUp(){
            super.pickedUp();
            shieldRadius = 0f;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(shield);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            shield = read.f();
            if(shield > 0) shieldRadius = 1f;
        }
    }
}
