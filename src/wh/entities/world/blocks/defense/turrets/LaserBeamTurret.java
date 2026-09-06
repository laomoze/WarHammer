package wh.entities.world.blocks.defense.turrets;

import arc.Core;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.consumers.ConsumeCoolant;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValues;
import wh.content.WHLiquids;
import wh.entities.bullet.laser.LaserBeamBulletType;
import wh.entities.bullet.laser.LightningBeamBulletType;
import wh.entities.bullet.laser.PointLaserBeamBulletType;

import static mindustry.Vars.tilesize;

public class LaserBeamTurret extends PowerTurret{
    public float firingMoveFract = 0.5f;
    public float shootDuration = 300;
    public Liquid cost = WHLiquids.refinePromethium;
    public float costMount = 90 / 60f;
    public LaserBeamTurret(String name){
        super(name);
        coolantMultiplier=2;

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
        private final ObjectMap<Bullet, Float> fadeLifetimes = new ObjectMap<>();

        public float lifeProgress() {
            if (bullets.isEmpty()) return 0f;
            BulletEntry entry = bullets.find(e -> e != null && e.bullet != null && e.bullet.type != null);
            if (entry == null) return 0f;
            return Mathf.clamp(entry.bullet.time / Math.max(entry.bullet.lifetime, 0.0001f), 0f, 1f);
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

            float bulletTimeScale = Math.max(timeScale, 0f);
            Float fadeLifetime = fadeLifetimes.get(entry.bullet);
            if (fadeLifetime != null) {
                entry.bullet.lifetime = fadeLifetime;
            } else {
                float targetLifetime = shootDuration / Math.max(bulletTimeScale, 0.0001f);
                if (entry.bullet.time >= targetLifetime) {
                    float fadeDuration = Math.max(bulletFadeTime(entry.bullet), Time.delta);
                    fadeLifetime = entry.bullet.time + fadeDuration;
                    fadeLifetimes.put(entry.bullet, fadeLifetime);
                    entry.bullet.lifetime = fadeLifetime;
                } else {
                    float fadeDuration = bulletFadeTime(entry.bullet);
                    entry.bullet.lifetime = targetLifetime + fadeDuration;
                }
            }

            if (entry.bullet instanceof PointLaserBeamBulletType.PointLaserBeamBullet beam) {
                beam.damageScale = bulletTimeScale;
                beam.damage = beam.baseDamage * beam.damageScale;
            } else if (entry.bullet instanceof LightningBeamBulletType.LightningBeamBullet beam) {
                beam.damageScale = bulletTimeScale;
                beam.damage = beam.baseDamage * beam.damageScale;
            } else if (entry.bullet.type instanceof ContinuousBulletType continuous) {
                float damageScale = continuous.timescaleDamage ? 1f : bulletTimeScale;
                entry.bullet.damage = entry.bullet.type.damage * damageScale * entry.bullet.damageMultiplier();
            } else {
                entry.bullet.damage = entry.bullet.type.damage * bulletTimeScale * entry.bullet.damageMultiplier();
            }
        }

        private float bulletFadeTime(Bullet bullet) {
            if (bullet.type instanceof PointLaserBeamBulletType beam) return Math.max(0f, beam.fadeTime);
            if (bullet.type instanceof LightningBeamBulletType beam) return Math.max(0f, beam.fadeTime);
            if (bullet.type instanceof LaserBeamBulletType beam) return Math.max(0f, beam.fadeTime);
            return 0f;
        }

        @Override
        public void updateTile(){
            super.updateTile();

            bullets.removeAll(b -> {
                boolean remove = b == null || b.bullet == null || !b.bullet.isAdded() || b.bullet.type == null || b.bullet.owner != this;
                if (remove && b != null && b.bullet != null) fadeLifetimes.remove(b.bullet);
                return remove;
            });

            if(bullets.any()){
                for(var entry : bullets){
                    updateBullet(entry);
                }
                curRecoil = 1;
                wasShooting = true;
                heat = 1f;
            } else if (reloadCounter > 0) {

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
            rotation = Angles.moveToward(rotation, targetRot, efficiency * rotateSpeed * delta() *
                    (bullets.any() ? firingRotationSpeedMultiplier() : 1f));
        }

        protected float firingRotationSpeedMultiplier() {
            return firingMoveFract;
        }

        @Override
        protected void handleBullet(@Nullable Bullet bullet, float offsetX, float offsetY, float angleOffset){
            if(bullet != null){
                float bulletTimeScale = Math.max(timeScale, 0.0001f);
                bullet.lifetime = shootDuration / bulletTimeScale + bulletFadeTime(bullet);
                bullet.time = 0f;
                fadeLifetimes.remove(bullet);
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
