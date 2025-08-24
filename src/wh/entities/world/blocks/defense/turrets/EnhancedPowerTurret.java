package wh.entities.world.blocks.defense.turrets;

import arc.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.Item;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.ItemTurret.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.content;

public class EnhancedPowerTurret extends Turret{

    public @Nullable Item enhancerItem;
    public @Nullable BulletType enhancedBullet;
    public ShootPattern enhancedPattern;
    public int maxAmmo;
    public BulletType shootType;

    public EnhancedPowerTurret(String name){
        super(name);
        hasPower = true;
    }

    public void enhance(Item enhancer, BulletType bullet, ShootPattern pattern){
        this.enhancerItem = enhancer;
        this.enhancedBullet = bullet;
        this.enhancedPattern = pattern;
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("ammo", (PowerTurretBuild entity) ->
        new Bar(
        "stat.ammo", Pal.ammo,
        () -> (float)entity.enhancedAmmo / maxAmmo));
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(this, shootType)));
        if(enhancedBullet != null && enhancerItem != null) stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(enhancerItem, enhancedBullet)));
        stats.add(Stat.ammoCapacity, maxAmmo / ammoPerShot, StatUnit.shots);
    }


    public class PowerTurretBuild extends TurretBuild{
        public int enhancedAmmo;

        @Override
        public BulletType useAmmo(){
            if(enhancedAmmo > 0){
                enhancedAmmo -= ammoPerShot;
                enhancedAmmo = Math.max(enhancedAmmo, 0);
                return enhancedBullet != null ? enhancedBullet : shootType;
            }
            return shootType;
        }

        @Override
        public boolean hasAmmo(){
            if(enhancedAmmo > ammoPerShot) return power.status > 0.99F;
            return power.status > 0.99F;
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            for(int i = 0; i < amount; i++){
                handleItem(null, item);
            }
        }

        @Override
        public BulletType peekAmmo(){
            return enhancedAmmo > 0 && enhancedBullet != null ?
            enhancedBullet : shootType;
        }

        @Override
        public void handleItem(Building source, Item item){
            BulletType type = enhancedBullet;
            enhancedAmmo += (int)type.ammoMultiplier;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if(enhancedBullet == null || enhancerItem != item) return false;

            return enhancedAmmo + enhancedBullet.ammoMultiplier <= maxAmmo;
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            if(enhancedBullet == null || enhancerItem != item) return 0;
            return Math.min((int)((maxAmmo - enhancedAmmo) / enhancedBullet.ammoMultiplier), amount);
        }

        @Override
        public int removeStack(Item item, int amount){
            return 0;
        }

        public boolean hasEnhancedAmmo(){
            return enhancedAmmo >= ammoPerShot;
        }

        @Override
        protected void shoot(BulletType type){
            float
            bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
            bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);

            BulletType finalType;

            if(hasEnhancedAmmo()){
                finalType = enhancedBullet;
            }else{
                finalType = type;
            }

            ShootPattern pattern = enhancedAmmo > 0 && enhancedPattern != null ? enhancedPattern : shoot;

            if(pattern.firstShotDelay > 0){
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
                pattern.shoot(barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                    finalType.chargeEffect.at(
                    x + Angles.trnsx(rotation - 90, shootX + xOffset, shootY + yOffset),
                    y + Angles.trnsy(rotation - 90, shootX + xOffset, shootY + yOffset),
                    rotation
                    );
                }, () -> {
                    barrelCounter++;
                });
            }

            pattern.shoot(barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                queuedBullets++;
                if(delay > 0f){
                    Time.run(delay, () -> bullet(finalType, xOffset, yOffset, angle, mover));
                }else{
                    bullet(finalType, xOffset, yOffset, angle, mover);
                }
            }, () -> barrelCounter++);

            if(consumeAmmoOnce){
                useAmmo();
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();
            unit.ammo((float)unit.type().ammoCapacity * enhancedAmmo / maxAmmo);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(enhancedAmmo);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            enhancedAmmo = read.i();
        }
    }
}