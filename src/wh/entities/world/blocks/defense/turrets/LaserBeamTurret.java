package wh.entities.world.blocks.defense.turrets;

import arc.Core;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Nullable;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.consumers.ConsumeCoolant;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValues;
import wh.content.WHLiquids;

import static mindustry.Vars.tilesize;

public class LaserBeamTurret extends PowerTurret{
    public float firingMoveFract = 0.5f;
    public float shootDuration = 300;
    public Liquid cost = WHLiquids.refinePromethium;
    public float costMount = 90 / 60f;
    public LaserBeamTurret(String name){
        super(name);
        coolantMultiplier=2;

        consume(new ConsumeLiquid(cost, costMount){
            @Override
            public void update(Building build){
                if(build instanceof LaserBeamTurretBuild c){
                    if(c.wasShooting){
                        super.update(build);
                    }
                }
            }
        });
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.booster);

        if(coolant != null){
            stats.add(Stat.input, StatValues.boosters(reload, coolant.amount, coolantMultiplier, false, liquid -> consumesLiquid(liquid) && liquid != cost));
        }
    }

    @Override
    public void init(){
        WHItemTurret.intTurret(this);
        super.init();
        if(coolant == null){
            coolant = findConsumer(c -> c instanceof ConsumeCoolant b && b.filter != cost);
        }
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("duration", (LaserBeamTurretBuild entity) ->
        new Bar(
        () -> Core.bundle.format("bar.wh-duration",entity.lifeProgress()),
        () -> Pal.accent,
                entity::lifeProgress));
    }

    public class LaserBeamTurretBuild extends PowerTurretBuild{
        public Seq<BulletEntry> bullets = new Seq<>();

        public float lifeProgress() {
            if (shootDuration <= 0.0001f || bullets.isEmpty()) return 0f;
            BulletEntry entry = bullets.find(e -> e != null && e.bullet != null && e.bullet.type != null);
            if (entry == null) return 0f;
            return Mathf.clamp(1f - entry.life / shootDuration, 0f, 1f);
        }

        @Override
        protected void updateCooling(){
            //do nothing, cooling is irrelevant here
        }
        protected void updateBullet(BulletEntry entry){
            if (entry == null || entry.bullet == null || entry.bullet.type == null) return;

            float
            bulletX = x + Angles.trnsx(rotation - 90, shootX + entry.x, shootY + entry.y),
            bulletY = y + Angles.trnsy(rotation - 90, shootX + entry.x, shootY + entry.y),
            angle = rotation + entry.rotation;
            entry.bullet.set(bulletX, bulletY);
            entry.bullet.rotation(angle);
            entry.bullet.owner = this;

            float effScale = Mathf.clamp(efficiency, 0.00001f, 1f);
            entry.life -= delta() / effScale;

            if(entry.bullet.type instanceof ContinuousBulletType){
                entry.bullet.lifetime = shootDuration;
                //drive beam growth/fade by turret-managed elapsed time and keep bullet alive manually
                float elapsed = Mathf.clamp(shootDuration - entry.life, 0f, Math.max(0f, entry.bullet.lifetime - 0.001f));
                entry.bullet.time = elapsed;
                entry.bullet.keepAlive = true;
            }else {
                entry.bullet.lifetime = (entry.bullet.type.lifetime * entry.bullet.type.optimalLifeFract);
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();

            bullets.removeAll(b -> b == null || b.bullet == null || !b.bullet.isAdded() || b.bullet.type == null || b.life <= 0f || b.bullet.owner != this);

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
            if (bullets.any()) {
                return lifeProgress();
            }
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
