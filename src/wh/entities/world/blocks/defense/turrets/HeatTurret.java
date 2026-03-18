package wh.entities.world.blocks.defense.turrets;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.graphics.*;
import wh.ui.*;

import static mindustry.Vars.indexer;

public class HeatTurret extends PowerTurret{
    public IntSeq heatRequirements = new IntSeq();
    public Seq<BulletType> stageEnhancedBullets = new Seq<>();

    public HeatTurret(String name){
        super(name);
        heatRequirement = 20;
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
        int stage = stageFor(entity.heatReq);
        float heatStageRequirement = stageRequirement(stage);
        float damageMul = heatDamageMultiplier(entity.heatReq);
        return new Bar(() ->
        Core.bundle.format("bar.wh-heat-stage", stage + 1, (int)((entity.heatReq / heatStageRequirement) * 100)) +
        "  x" + Strings.autoFixed(damageMul, 2),
        () -> Pal.lightOrange,
        () -> Mathf.clamp(entity.heatReq / heatStageRequirement));
    }

    private int unlockedEnhancedStages(float heatReq){
        int enhancedStageCount = stageEnhancedBullets.size;
        if(enhancedStageCount <= 0 || heatRequirements.isEmpty()) return 0;

        int stage = -1;
        int usableStages = Math.min(enhancedStageCount, heatRequirements.size);
        for(int i = 0; i < usableStages; i++){
            if(heatReq >= heatRequirements.get(i)){
                stage = i;
            }else{
                break;
            }
        }
        return Mathf.clamp(stage + 1, 0, enhancedStageCount);
    }

    private float heatDamageMultiplier(float heatReq){
        return 1f + unlockedEnhancedStages(heatReq);
    }

    public void init(){
        WHItemTurret.intTurret(this);
        super.init();
    }

    private int stageFor(float heatReq){
        int stage = 0;
        int maxStage = Math.max(heatRequirements.size - 1, 0);
        for(int i = 0; i < maxStage; i++){
            if(heatReq >= heatRequirements.get(i)){
                stage = i + 1;
            }else{
                break;
            }
        }
        return Math.min(stage, maxStage);
    }

    private float stageRequirement(int stage){
        if(heatRequirements.isEmpty()) return 1f;
        int idx = Mathf.clamp(stage, 0, heatRequirements.size - 1);
        return heatRequirements.get(idx);
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
            warmup = Mathf.lerpDelta(warmup, isEnhancedHeat() ? 1f : 0f, isEnhancedHeat() ? 0.08f : 0.1f);

            Tmp.v1.trns(rotation, -18);
            float cx = x + Tmp.v1.x, cy = y + Tmp.v1.y;
            if(warmup > 0.9f && Mathf.chanceDelta(0.08)){
                WHFx.tentacleCorona(50f, 15, 8, 1, 1f, Pal.lighterOrange, Pal.lightOrange)
                .rotWithParent(true)
                .at(cx, cy, rotation, Pal.lighterOrange, this);
            }
        }

        @Override
        protected void shoot(BulletType type){
            float
            bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
            bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);

            BulletType stageBullet = currentStageBullet();
            if(stageBullet != null){
                type = stageBullet;
            }

            if(shoot.firstShotDelay > 0){
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
                type.chargeEffect.at(bulletX, bulletY, rotation, type.hitColor);
            }

            BulletType finalType = type;

            ShootPattern pattern = finalType.shootPattern != null ? finalType.shootPattern : shoot;

            pattern.shoot(barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
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

        private int reachedHeatStage(){
            int stage = -1;
            for(int i = 0; i < heatRequirements.size; i++){
                if(heatReq >= heatRequirements.get(i)){
                    stage = i;
                }else{
                    break;
                }
            }
            return stage;
        }

        private @Nullable BulletType currentStageBullet(){
            int stage = reachedHeatStage();
            if(stage < 0) return null;

            if(stageEnhancedBullets.size > 0){
                int idx = Mathf.clamp(stage, 0, stageEnhancedBullets.size - 1);
                return stageEnhancedBullets.get(idx);
            }

            return null;
        }

        private boolean isEnhancedHeat(){
            if(heatRequirements.isEmpty()) return false;
            return heatReq >= heatRequirements.peek() - 1;
        }

        @Override
        public void draw(){
            super.draw();
            Tmp.v1.trns(rotation, -18);
            float cx = x + Tmp.v1.x, cy = y + Tmp.v1.y;
            Draw.z(Layer.effect + 0.0014f);
            Draw.color(Pal.lighterOrange);
            Fill.circle(cx, cy, 7f * warmup);
            Draw.color(Pal.coalBlack);
            Fill.circle(cx, cy, 4f * warmup);
            Draw.color(Pal.lighterOrange);
            Drawn.surround(id, cx, cy, 13 * warmup, 3, 3, 5, warmup);
        }
    }

    public static class HeatBulletType extends BasicBulletType{
        public int extraFrag = 5;

        public HeatBulletType(){
            puddleLiquid = Liquids.slag;
            puddleAmount = 5f;
            puddles = 6;
            puddleRange = 32f;
        }

        @Override
        public void init(Bullet b){
            super.init(b);
            if(b.owner instanceof HeatTurretBuild build && build.block instanceof HeatTurret heatTurret){
                b.fdata = heatFactor(build, heatTurret);
            }
            b.damage = damage * b.fdata;
        }

        private float heatFactor(HeatTurretBuild build, HeatTurret heatTurret){
            return heatTurret.heatDamageMultiplier(build.heatReq);
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
                    Bullet f = createFragBullet(b, x, y, i);
                    f.owner = b.owner;
                }
                b.frags++;
            }
        }

        @Override
        public void hitEntity(Bullet b, Hitboxc entity, float health){
            super.hitEntity(b, entity, health);
            if(fragBullet != null && (fragOnAbsorb || !b.absorbed) && !(b.frags >= pierceFragCap && pierceFragCap > 0)){
                int splitCount = (int)Mathf.clamp((entity.hitSize() / 8f), 0, 1) * extraFrag;
                for(int i = 0; i < splitCount; i++){
                    createFragBullet(b, b.x, b.y, i);
                }
            }
        }

        private Bullet createFragBullet(Bullet source, float x, float y, int index){
            float len = Mathf.random(fragOffsetMin, fragOffsetMax);
            float angle = source.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + fragSpread * index - (fragBullets - 1) * fragSpread / 2f;
            Bullet frag = fragBullet.create(source, x + Angles.trnsx(angle, len), y + Angles.trnsy(angle, len), angle,
            Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
            if(frag.type instanceof HeatBulletType) frag.fdata = source.fdata;
            return frag;
        }

        @Override
        public void removed(Bullet b){
            createPuddles(b, b.x, b.y);
            super.removed(b);
        }

     /*   public float damageMultiplier(Bullet b){
            if(b.owner instanceof HeatTurretBuild) return state.rules.blockDamage(b.team) * b.fdata;
            return super.damageMultiplier(b);
        }*/
    }
}
