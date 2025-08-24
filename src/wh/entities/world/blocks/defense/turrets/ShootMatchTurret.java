package wh.entities.world.blocks.defense.turrets;

import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.meta.*;
import wh.ui.*;

public class ShootMatchTurret extends ItemTurret {
    public float lifeRnd = 0;
    public IntMap<ShootPattern> shooterMap = new IntMap<>();
    public boolean moreBarrelCharge = false;

    public ShootMatchTurret(String name) {
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.ammo);
        stats.add(Stat.ammo, UIUtils.ammo(ammoTypes));
    }
    /**
     * Should invoke after {@link ItemTurret#ammo(Object...)}
     */
    public void shooter(Object... objects) {
        ObjectMap<Item, ShootPattern> mapper = ObjectMap.of(objects);

        for (ObjectMap.Entry<Item, BulletType> entry : ammoTypes.entries()) {
            shooterMap.put(entry.value.id, mapper.get(entry.key, shoot));
        }
    }

    public class ShootMatchTurretBuild extends ItemTurretBuild {
        public ShootPattern getShooter(BulletType type) {
            ShootPattern s = shooterMap.get(type.id);
            return s == null ? shoot : s;
        }

        @Override
        protected void shoot(BulletType type) {
            float
            bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
            bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);

            ShootPattern shoot = getShooter(type);

            if(shoot.firstShotDelay > 0&& moreBarrelCharge){
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
                shoot.shoot(barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                    type.chargeEffect.at(
                    x + Angles.trnsx(rotation - 90, shootX + xOffset, shootY + yOffset),
                    y + Angles.trnsy(rotation - 90, shootX + xOffset, shootY + yOffset),
                    rotation
                    );
                }, () -> {});
            }

            if(shoot.firstShotDelay > 0&& !moreBarrelCharge){
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
                type.chargeEffect.at(bulletX, bulletY, rotation);
            }

            shoot.shoot(barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                queuedBullets++;
                if (delay > 0f) {
                    Time.run(delay, () -> bullet(type, xOffset, yOffset, angle, mover));
                } else {
                    bullet(type, xOffset, yOffset, angle, mover);
                }
            }, () -> barrelCounter++);

            if (consumeAmmoOnce) {
                useAmmo();
            }
        }

        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover) {
            queuedBullets--;

            if (dead || (!consumeAmmoOnce && !hasAmmo())) return;

            float
            xSpread = Mathf.range(xRand),
            bulletX = x + Angles.trnsx(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
            bulletY = y + Angles.trnsy(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
            shootAngle = rotation + angleOffset + Mathf.range(inaccuracy + type.inaccuracy);

            float lifeScl = type.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, targetPos.x, targetPos.y) / type.range, minRange / type.range, range() / type.range) : 1f;

            if (lifeRnd > 0) lifeScl += Mathf.range(lifeRnd);

            //TODO aimX / aimY for multi shot turrets?
            handleBullet(type.create(this, team, bulletX, bulletY, shootAngle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, mover, targetPos.x, targetPos.y), xOffset, yOffset, shootAngle - rotation);

            (shootEffect == null ? type.shootEffect : shootEffect).at(bulletX, bulletY, rotation + angleOffset, type.hitColor);
            (smokeEffect == null ? type.smokeEffect : smokeEffect).at(bulletX, bulletY, rotation + angleOffset, type.hitColor);
            shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));

            ammoUseEffect.at(
            x - Angles.trnsx(rotation, ammoEjectBack),
            y - Angles.trnsy(rotation, ammoEjectBack),
            rotation * Mathf.sign(xOffset)
            );

            if (shake > 0) {
                Effect.shake(shake, shake, this);
            }

            curRecoil = 1f;
            if (recoils > 0) {
                curRecoils[barrelCounter % recoils] = 1f;
            }
            heat = 1f;
            totalShots++;

            if (!consumeAmmoOnce) {
                useAmmo();
            }
        }
    }
}
//每次射击可以消耗多种物品的炮塔设计感觉有问题
/*public class ShootMatchTurret extends ItemTurret{
    public IntMap<ShootPattern> shooterMap = new IntMap<>();

    public ObjectMap<Item, Item> enhancerMap = new OrderedMap<>();
    public ObjectMap<Item, BulletType> ammoEnhancement = new OrderedMap<>();
    public ObjectMap<Item, ObjectMap<Item, BulletType>> ammoEnhancementsMap = new ObjectMap<>();

    public boolean enhanced = false;
    public int maxEnhanced = maxAmmo;

    public ShootMatchTurret(String name){
        super(name);
    }

    *//**
 * Should invoke after {@link ItemTurret#ammo(Object...)}
 *//*
    public void shooter(Object... objects){
        ObjectMap<Item, ShootPattern> mapper = ObjectMap.of(objects);
        for(ObjectMap.Entry<Item, BulletType> entry : ammoTypes.entries()){
            shooterMap.put(entry.value.id, mapper.get(entry.key, shoot));
        }
    }

    public void enhance(Item baseAmmo, Item enhancer, BulletType enhancedBullet, ShootPattern pattern){
        enhancerMap.put(baseAmmo, enhancer);
        ammoEnhancement.put(enhancer, enhancedBullet);
        ammoEnhancementsMap.put(baseAmmo, ammoEnhancement.copy());
        shooterMap.put(enhancedBullet.id, pattern);
    }



    @Override
    public void setBars(){
        super.setBars();
        if(enhanced){
            addBar("wh-enhancedAmmo", (ShootMatchTurretBuild entity) ->
            new Bar(
            () -> "enhancedAmmo",
            () -> Pal.ammo,
            () -> entity.enhancedAmmo() > 0 ? entity.enhancedAmmo() / (float)maxEnhanced : 0f
            ));
        }
    }

    @Override
    public void init(){
        super.init();
    }


    public class ShootMatchTurretBuild extends ItemTurretBuild{
        public int enhancedAmmo = 0;
        public @Nullable Item lastBase;
        public Seq<EnhanceItemEntry> enhanceAmmo = new Seq<>();

        public @Nullable Item currentBase(){
            return ammo.isEmpty() ? null : ((ItemEntry)ammo.peek()).item;
        }

        public float enhancedAmmo(){
            return enhancedAmmo;
        }

        @Override
        public void handleItem(Building source, Item item){
            super.handleItem(source, item);
            if(enhanced){
                Item base = currentBase();
                if(base != null
                && enhancerMap.get(base) == item
                && ammoEnhancementsMap.get(base).containsKey(item)){
                    BulletType type = ammoEnhancementsMap.get(base).get(item);
                    if(type == null) return;
                    enhancedAmmo += (int)type.ammoMultiplier;

                    //find ammo entry by type
                    for(int i = 0; i < enhanceAmmo.size; i++){
                        var entry = enhanceAmmo.get(i);
                        if(entry.item == item){
                            entry.amount += (int)type.ammoMultiplier;
                            enhanceAmmo.swap(i, enhanceAmmo.size - 1);
                            return;
                        }
                    }
                    enhanceAmmo.add(new EnhanceItemEntry(item, (int)type.ammoMultiplier));
                }
            }
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            for(int i = 0; i < amount; i++){
                handleItem(null, item);
            }
        }

        public BulletType useEnhancedAmmo(){
            if(enhanceAmmo.isEmpty()) return null;
            AmmoEntry entry = enhanceAmmo.peek();
            entry.amount -= ammoPerShot;
            if(entry.amount <= 0){
                enhanceAmmo.pop();
            }
            enhancedAmmo -= ammoPerShot;
            enhancedAmmo = Math.max(enhancedAmmo, 0);
            return entry.type();
        }

        *//** @return whether the turret has ammo. *//*
        public boolean hasEnhancedAmmo(){
            return enhanceAmmo.size > 0 && (enhanceAmmo.peek().amount >= ammoPerShot);
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            if(enhanced && currentBase() != null) enhanceAcceptStack(item, amount, source);
            return super.acceptStack(item, amount, source);
        }

        public int enhanceAcceptStack(Item item, int amount, Teamc source){
            BulletType type = ammoEnhancementsMap.get(currentBase()).get(item);
            if(type == null) return 0;
            return Math.min((int)((maxEnhanced - enhancedAmmo) / type.ammoMultiplier), amount);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return super.acceptItem(source, item) || (enhanced && currentBase() != null && item == enhancerMap.get(currentBase())
            && enhancedAmmo + ammoEnhancement.get(item).ammoMultiplier <= maxEnhanced);
        }

        public ShootPattern getShooter(BulletType type){
            ShootPattern s = shooterMap.get(type.id);
            return s == null ? shoot : s;
        }

        @Override
        protected void shoot(BulletType type){
            float
            bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
            bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);

            Item base = currentBase();
            BulletType finalType;

            if(hasEnhancedAmmo() && enhanceAmmo.peek().item == enhancerMap.get(base)){
                Item enhancer = enhancerMap.get(base);
                ObjectMap<Item, BulletType> map = ammoEnhancementsMap.get(base);
                finalType = map.get(enhancer);

            }else{
                finalType = type;
            }

            ShootPattern pattern = getShooter(finalType);
            if(pattern.firstShotDelay > 0){
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
                finalType.chargeEffect.at(bulletX, bulletY, rotation);
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
                if(enhanced) useEnhancedAmmo();
            }
        }


        @Override
        public void updateTile(){
            super.updateTile();
            if(enhanced){
            Item current = currentBase();
            if(current != lastBase){
                enhanceAmmo.clear();
                enhancedAmmo = 0;
                lastBase = current;
            }
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.b(enhanceAmmo.size);
            for(EnhanceItemEntry entry : enhanceAmmo){
                write.s(entry.item.id);
                write.s(entry.amount);
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            enhanceAmmo.clear();
            enhancedAmmo = 0;
            int amount = read.ub();
            for(int i = 0; i < amount; i++){
                Item item = Vars.content.item(revision < 2 ? read.ub() : read.s());
                short a = read.s();

                //only add ammo if this is a valid ammo type
                if(item != null && ammoEnhancement.containsKey(item)){
                    enhancedAmmo += a;
                    enhanceAmmo.add(new EnhanceItemEntry(item, a));
                }
            }
        }

        public class EnhanceItemEntry extends AmmoEntry{
            public Item item;

            EnhanceItemEntry(Item item, int amount){
                this.item = item;
                this.amount = amount;
            }

            @Override
            public BulletType type(){
                return ammoEnhancement.get(item);
            }

            @Override
            public String toString(){
                return "ItemEntry{" +
                "item=" + item +
                ", amount=" + amount +
                '}';
            }
        }
    }
}*/

