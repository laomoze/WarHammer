package wh.entities.world.blocks.defense.turrets;

import arc.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import wh.entities.bullet.laser.*;

import static mindustry.Vars.tilesize;

public class LaserBeamTurret extends PowerTurret{
    public float firingMoveFract = 0.5f;
    public float shootDuration = 300;
    public LaserBeamTurret(String name){
        super(name);
        coolantMultiplier=2;
    }
    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.booster);
        if(coolant != null){
            stats.add(Stat.input, StatValues.boosters(reload, coolant.amount, coolantMultiplier, false, this::consumesLiquid));
        }
    }

    @Override
    public void init(){
        super.init();

        if(coolant == null){
            coolant = findConsumer(c -> c instanceof ConsumeLiquidBase);
        }
    }

    @Override
    public void setBars(){
        super.setBars();
      /*  addBar("duration", (LaserBeamTurretBuild entity) ->
        new Bar(
        () -> Core.bundle.format("bar.wh-duration",entity.lifeProgress()),
        () -> Pal.accent,
        entity::lifeProgress));*/
    }

    public class LaserBeamTurretBuild extends PowerTurretBuild{
        public Seq<BulletEntry> bullets = new Seq<>();

   /*   public float lifeProgress(){
          if(bullets.isEmpty()) return 0f;
          BulletEntry entry = bullets.first();
          return entry == null ? 0f : Mathf.clamp(entry.bullet.time / entry.bullet.lifetime, 0f, 1f);
      }*/

        @Override
        protected void updateCooling(){
            //do nothing, cooling is irrelevant here
        }
        protected void updateBullet(BulletEntry entry){
            float
            bulletX = x + Angles.trnsx(rotation - 90, shootX + entry.x, shootY + entry.y),
            bulletY = y + Angles.trnsy(rotation - 90, shootX + entry.x, shootY + entry.y),
            angle = rotation + entry.rotation;
            entry.bullet.set(bulletX, bulletY);
            entry.bullet.rotation(angle);
            entry.bullet.owner = this;
            if(entry.bullet.type instanceof ContinuousBulletType){
                entry.bullet.lifetime = shootDuration;
            }else {
                entry.bullet.time = entry.bullet.type.lifetime * entry.bullet.type.optimalLifeFract;
            }
            /* entry.bullet.keepAlive = true;*///臭猫害得我绘制不出子弹
            entry.life -= delta()/ Mathf.clamp(Math.max(efficiency, 0.00001f),0,1);
        }

        @Override
        public void updateTile(){
            super.updateTile();

            bullets.removeAll(b -> !b.bullet.isAdded() || b.bullet.type == null || b.life <= 0f || b.bullet.owner != this);

            if(bullets.any()){
                for(var entry : bullets){
                    updateBullet(entry);
                }
                wasShooting = true;
                heat = 1f;
                curRecoil = 1f;
            }if(reloadCounter > 0){

                if(coolant != null){
                    //TODO does not handle multi liquid req?
                    Liquid liquid = liquids.current();
                    float maxUsed = coolant.amount;
                    float used = (cheating() ? maxUsed : Math.min(liquids.get(liquid), maxUsed)) * edelta();
                    reloadCounter -= used * liquid.heatCapacity * coolantMultiplier;
                    liquids.remove(liquid, used);

                    if(Mathf.chance(0.06 * used)){
                        coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                    }
                }else{
                    reloadCounter -= edelta();
                }
            }
        }

        @Override
        public float progress(){
            return 1f - Mathf.clamp(reloadCounter / reload);
        }

        @Override
        protected void updateReload(){
            //updated in updateTile() depending on coolant
        }

        @Override
        protected void updateShooting(){
            if(bullets.any()){
                return;
            }

            if(reloadCounter <= 0 && efficiency > 0 && !charging() && shootWarmup >= minWarmup){
                BulletType type = peekAmmo();

                shoot(type);

                reloadCounter = reload;
            }
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, efficiency * rotateSpeed * delta() * (bullets.any() ? firingMoveFract : 1f));
        }

        @Override
        protected void handleBullet(@Nullable Bullet bullet, float offsetX, float offsetY, float angleOffset){
            if(bullet != null){
                bullets.add(new BulletEntry(bullet, offsetX, offsetY, angleOffset, shootDuration));
            }
        }

        @Override
        public float activeSoundVolume(){
            return 1f;
        }

        @Override
        public boolean shouldActiveSound(){
            return bullets.any();
        }
    }
}
