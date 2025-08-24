package wh.entities.world.blocks.defense.turrets;

import arc.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;
import wh.ui.*;

public class HeatTurret extends PowerTurret{
    public int[] heatRequirements = new int[]{60, 120, 240};

    public ShootPattern stage2Shoot = new ShootPattern();

    public HeatTurret(String name){
        super(name);
        heatRequirement = 15;
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
        if(entity.heatReq >= heatRequirements[2]){
            stage = 3;
        }else if(entity.heatReq >= heatRequirements[1]){
            stage = 2;
        }else if(entity.heatReq >= heatRequirements[0]){
            stage = 1;
        }else if(entity.heatReq <= heatRequirements[0]){
            stage = 0;
        }
        int finalStage = stage;
        float heatStageRequirement = stage == 0 ? heatRequirement : heatRequirements[stage - 1];
        return new Bar(() ->
        Core.bundle.format("bar.wh-heat-stage", finalStage, (int)(entity.heatReq / heatStageRequirement * 100)),
        () -> Pal.lightOrange,
        () -> entity.heatReq / heatStageRequirement);
    }

    public class HeatTurretBuild extends PowerTurretBuild{
        @Override
        public void updateEfficiencyMultiplier(){
            if(heatRequirement > 0){
                efficiency *= Math.min(Math.max(heatReq / heatRequirement, cheating() ? 1f : 0f), 1f);
            }
        }

        @Override
        public void update(){
            super.update();
        }

        @Override
        protected void shoot(BulletType type){
            float
            bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
            bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);

            ShootPattern finalShoot = heatReq > heatRequirements[1] ? stage2Shoot : shoot;

            if(finalShoot.firstShotDelay > 0){
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
                type.chargeEffect.at(bulletX, bulletY, rotation);
            }

            finalShoot.shoot(barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                queuedBullets++;
                int barrel = barrelCounter;

                if(delay > 0f){
                    Time.run(delay, () -> {
                        //hack: make sure the barrel is the same as what it was when the bullet was queued to fire
                        int prev = barrelCounter;
                        barrelCounter = barrel;
                        bullet(type, xOffset, yOffset, angle, mover);
                        barrelCounter = prev;
                    });
                }else{
                    bullet(type, xOffset, yOffset, angle, mover);
                }
            }, () -> barrelCounter++);

            if(consumeAmmoOnce){
                useAmmo();
            }
        }

        @Override
        public void draw(){
            super.draw();
        }
    }

    public static class HeatBulletType extends BasicBulletType{
        public float[] damageM=new float[]{1,2};
        @Override
        public void init(Bullet b){
            super.init(b);
            if(b.owner instanceof HeatTurretBuild build &&
            build.block instanceof HeatTurret heatTurret){
                if(build.heatReq < heatTurret.heatRequirement) b.fdata = 1f;
                if(build.heatReq >= heatTurret.heatRequirements[0])
                    b.fdata = 1+damageM[0] * Mathf.curve(build.heatReq / heatTurret.heatRequirements[0], 0, 1);
                if(build.heatReq >= heatTurret.heatRequirements[1])
                    b.fdata =1+damageM[0]+ damageM[1] * Mathf.curve(build.heatReq / heatTurret.heatRequirements[1], 0, 1);
                if(build.heatReq >= heatTurret.heatRequirements[2])
                    b.fdata =1+damageM[0]+ damageM[1]+ (Mathf.log(25,build.heatReq / heatTurret.heatRequirements[2])-1);
            }
        }
        @Override
        public void updateHoming(Bullet b){
            super.updateHoming(b);
            if(homingPower > 0.0001f && b.time >= homingDelay){
                if(followAimSpeed > 0f){
                    @Nullable Unit shooter = null;
                    if(b.owner instanceof Unit) shooter = (Unit)b.owner;
                    if(b.owner instanceof ControlBlock) shooter = ((ControlBlock)b.owner).unit();
                    if(shooter != null){
                        float angle = b.angleTo(shooter.aimX, shooter.aimY);
                        b.rotation(Angles.moveToward(b.rotation(), angle, followAimSpeed * Time.delta));
                    }
                }
            }
        }

        @Override
        public float damageMultiplier(Bullet b){
            super.damageMultiplier(b);
            return b.fdata;
        }
    }
}
