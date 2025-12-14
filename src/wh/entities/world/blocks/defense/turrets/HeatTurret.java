package wh.entities.world.blocks.defense.turrets;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import wh.graphics.*;
import wh.ui.*;

import static mindustry.Vars.*;

public class HeatTurret extends PowerTurret{
    public int[] heatRequirements = new int[]{75, 150, 200};
    public @Nullable BulletType enhancedBullet;

    public HeatTurret(String name){
        super(name);
        heatRequirement = 25;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, UIUtils.ammo(ObjectMap.of(this, shootType)));
    }

    @Override
    public void setBars(){
        super.setBars();
        if(heatRequirement > 0){
            addBar("heat", this::get);
        }
    }

    private Bar get(HeatTurretBuild entity){
        int stage = 0;
        for(int i = 0; i<heatRequirements.length - 1; i++){
            if(entity.heatReq >= heatRequirements[i]){
                stage = i + 1;
            }else{
                break;
            }
        }
        stage = Math.min(stage, heatRequirements.length - 1);
        int finalStage = stage;
        float heatStageRequirement = stage == 0 ? heatRequirements[0] : heatRequirements[stage] ;
        return new Bar(() ->
        Core.bundle.format("bar.wh-heat-stage", finalStage+1, (int)((entity.heatReq / heatStageRequirement) * 100)),
        () -> Pal.lightOrange,
        () -> entity.heatReq / heatStageRequirement);
    }

    public class HeatTurretBuild extends PowerTurretBuild{
        public float warmup = 0;

        @Override
        public void updateEfficiencyMultiplier(){
            if(heatRequirement > 0){
                efficiency *= Math.min(Math.max(heatReq / heatRequirement, cheating() ? 1f : 0f), 1f);
            }
        }


        @Override
        public void updateTile(){
            super.updateTile();
            if(heatReq > heatRequirements[1]){
                warmup = Mathf.lerpDelta(warmup, 1, 0.08f);
            }else{
                warmup = Mathf.lerpDelta(warmup, 0, 0.1f);
            }
        }

        @Override
        protected void shoot(BulletType type){
            float
            bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
            bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);


            if(enhancedBullet != null && heatReq > heatRequirements[1]){
                type = enhancedBullet;
            }
            if(shoot.firstShotDelay > 0){
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
                type.chargeEffect.at(bulletX, bulletY, rotation);
            }

            BulletType finalType = type;
            shoot.shoot(barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                queuedBullets++;
                int barrel = barrelCounter;

                if(delay > 0f){
                    Time.run(delay, () -> {
                        //hack: make sure the barrel is the same as what it was when the bullet was queued to fire
                        int prev = barrelCounter;
                        barrelCounter = barrel;
                        bullet(finalType, xOffset, yOffset, angle, mover);
                        barrelCounter = prev;
                    });
                }else{
                    bullet(finalType, xOffset, yOffset, angle, mover);
                }
            }, () -> barrelCounter++);

            if(consumeAmmoOnce){
                useAmmo();
            }
        }

        @Override
        public void draw(){
            super.draw();
            Draw.z(Layer.effect + 0.001f);
            Tmp.v1.set(x, y).trns(rotation, -18);
            Draw.color(Pal.lighterOrange);
            Fill.circle(x + Tmp.v1.x, y + Tmp.v1.y, 8 * warmup);
            Draw.color(Pal.coalBlack);
            Fill.circle(x + Tmp.v1.x, y + Tmp.v1.y, 5 * warmup);
            Draw.color(Pal.lighterOrange);
            Drawn.surround(id, x + Tmp.v1.x, y + Tmp.v1.y, 13 * warmup, 5, 3, 5, warmup);
        }
    }

    public static class HeatBulletType extends BasicBulletType{
        public float[] damageM = new float[]{1f, 2f, 2f};
        public int extraFrag = 8;

        public HeatBulletType(){
            puddleLiquid = Liquids.slag;
            puddleAmount = 5f;
            puddles = 3;
            puddleRange = 32f;
        }

        @Override
        public void init(Bullet b){
            super.init(b);
            if(b.owner instanceof HeatTurretBuild build &&
            build.block instanceof HeatTurret heatTurret){
                b.fdata = 1f;
                for(int i = 0; i < heatTurret.heatRequirements.length - 1; i++){
                    if(build.heatReq >= heatTurret.heatRequirements[i]){
                        float ratio = Mathf.curve(build.heatReq / heatTurret.heatRequirements[i], 0, 1);
                        b.fdata += damageM[i] * ratio;
                    }
                }
                if(build.heatReq >= heatTurret.heatRequirements[heatTurret.heatRequirements.length - 1]){
                    b.fdata += (int)Mathf.log(5, build.heatReq / (float)heatTurret.heatRequirements[heatTurret.heatRequirements.length - 1]);
                }
            }
            b.damage = damage * b.fdata;
        }

        @Override
        public void createSplashDamage(Bullet b, float x, float y){
            if(splashDamageRadius > 0 && !b.absorbed){
                Damage.damage(b.team, x, y, splashDamageRadius, splashDamage * b.damageMultiplier() * b.fdata, splashDamagePierce, collidesAir, collidesGround, scaledSplashDamage, b);

                if(status != StatusEffects.none){
                    Damage.status(b.team, x, y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
                }

                if(heals()){
                    indexer.eachBlock(b.team, x, y, splashDamageRadius, Building::damaged, other -> {
                        healEffect.at(other.x, other.y, 0f, healColor, other.block);
                        other.heal(healPercent / 100f * other.maxHealth() + healAmount);
                    });
                }

                if(makeFire){
                    indexer.eachBlock(null, x, y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
                }
            }
        }

        @Override
        public void createFrags(Bullet b, float x, float y){
            if(fragBullet != null && (fragOnAbsorb || !b.absorbed) && !(b.frags >= pierceFragCap && pierceFragCap > 0)){

                for(int i = 0; i < fragBullets; i++){
                    float len = Mathf.random(fragOffsetMin, fragOffsetMax);
                    float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + fragSpread * i - (fragBullets - 1) * fragSpread / 2f;
                    Bullet f = fragBullet.create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                    if(f.type instanceof HeatBulletType) f.fdata = b.fdata;
                    f.owner = b.owner;
                }
                b.frags++;
            }
        }

        @Override
        public void hitEntity(Bullet b, Hitboxc entity, float health){
            super.hitEntity(b, entity, health);
            if(fragBullet != null && (fragOnAbsorb || !b.absorbed) && !(b.frags >= pierceFragCap && pierceFragCap > 0)){
                int splitCount = (int)Mathf.curve((entity.hitSize() / 8f), 0, 1) * extraFrag;
                for(int i = 0; i < splitCount; i++){
                    float len = Mathf.random(fragOffsetMin, fragOffsetMax);
                    float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + fragSpread * i - (fragBullets - 1) * fragSpread / 2f;
                    Bullet f = fragBullet.create(b, b.x + Angles.trnsx(a, len), b.y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
                    if(f.type instanceof HeatBulletType) f.fdata = b.fdata;
                }
            }
        }

        @Override
        public void removed(Bullet b){
            createPuddles(b, b.x, b.y);
            super.removed(b);
        }

        @Override
        public void update(Bullet b){
            super.update(b);

        }

     /*   public float damageMultiplier(Bullet b){
            if(b.owner instanceof HeatTurretBuild) return state.rules.blockDamage(b.team) * b.fdata;
            return super.damageMultiplier(b);
        }*/
    }
}
