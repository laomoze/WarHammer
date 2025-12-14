package wh.entities.world.entities.weapon;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ai.types.*;
import mindustry.audio.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import wh.graphics.*;
import wh.ui.*;
import wh.util.*;

import static mindustry.Vars.*;

public class MarkWeapon extends Weapon{

    public float markChance = 0.3f;
    public BulletType markBullet;
    public ShootPattern markShoot = new ShootPattern();

    private @Nullable Healthc markedUnit = null;
    public float markReduce = 0f;
    public float markTime = 120;
    protected float markTimer;
    protected float effectTimer;

    public MarkWeapon(String name){
        super(name);
        mountType = MarkWeaponMount::new;
    }

    @Override
    public void init(){
        super.init();
    }

    @Override
    public void update(Unit unit, WeaponMount m){
        if(!(m instanceof MarkWeaponMount mount)) return;
        markTimer -= Time.delta;
        if(markTimer < 0){
            effectTimer = Mathf.approachDelta(effectTimer, 0, 0.15f);
        }else effectTimer = Mathf.approachDelta(effectTimer, 1, 0.1f);

        if(markedUnit != null && (markedUnit.dead() || markTimer < 0)){
            markedUnit = null;
            markTimer = -1;
        }
        //super.update(unit, m);

        float
        mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
        mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

        boolean can = unit.canShoot();
        float lastReload = mount.reload;
        mount.reload = Math.max(mount.reload - Time.delta * unit.reloadMultiplier, 0);
        mount.recoil = Mathf.approachDelta(mount.recoil, 0, unit.reloadMultiplier / recoilTime);
        if(recoils > 0){
            if(mount.recoils == null) mount.recoils = new float[recoils];
            for(int i = 0; i < recoils; i++){
                mount.recoils[i] = Mathf.approachDelta(mount.recoils[i], 0, unit.reloadMultiplier / recoilTime);
            }
        }
        mount.smoothReload = Mathf.lerpDelta(mount.smoothReload, mount.reload / reload, smoothReloadSpeed);
        mount.charge = mount.charging && shoot.firstShotDelay > 0 ? Mathf.approachDelta(mount.charge, 1, 1 / shoot.firstShotDelay) : 0;

        float warmupTarget = (can && mount.shoot) || (continuous && mount.bullet != null) || mount.charging ? 1f : 0f;
        if(linearWarmup){
            mount.warmup = Mathf.approachDelta(mount.warmup, warmupTarget, shootWarmupSpeed);
        }else{
            mount.warmup = Mathf.lerpDelta(mount.warmup, warmupTarget, shootWarmupSpeed);
        }

        //find a new target
        if(!controllable && autoTarget){
            if((mount.retarget -= Time.delta) <= 0f){
                mount.target = findTarget(unit, mountX, mountY, mount.bulletWhich.range, mount.bulletWhich.collidesAir, mount.bulletWhich.collidesGround);
                mount.retarget = mount.target == null ? targetInterval : targetSwitchInterval;
            }

            if(mount.target != null && checkTarget(unit, mount.target, mountX, mountY, bullet.range)){
                mount.target = null;
            }

            boolean shoot = false;

            if(mount.target != null){
                shoot = mount.target.within(mountX, mountY, bullet.range + Math.abs(shootY) + (mount.target instanceof Sized s ? s.hitSize() / 2f : 0f)) && can;

                if(predictTarget){
                    Vec2 to = Predict.intercept(unit, mount.target, bullet.speed);
                    mount.aimX = to.x;
                    mount.aimY = to.y;
                }else{
                    mount.aimX = mount.target.x();
                    mount.aimY = mount.target.y();
                }
            }

            mount.shoot = mount.rotate = shoot;

            //note that shooting state is not affected, as these cannot be controlled
            //logic will return shooting as false even if these return true, which is fine
        }

        //rotate if applicable
        if(rotate && (mount.rotate || mount.shoot) && can){
            float axisX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
            axisY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

            mount.targetRotation = Angles.angle(axisX, axisY, mount.aimX, mount.aimY) - unit.rotation;
            mount.rotation = Angles.moveToward(mount.rotation, mount.targetRotation, rotateSpeed * Time.delta);
            if(rotationLimit < 360){
                float dst = Angles.angleDist(mount.rotation, baseRotation);
                if(dst > rotationLimit / 2f){
                    mount.rotation = Angles.moveToward(mount.rotation, baseRotation, dst - rotationLimit / 2f);
                }
            }
        }else if(!rotate){
            mount.rotation = baseRotation;
            mount.targetRotation = unit.angleTo(mount.aimX, mount.aimY);
        }

        float
        weaponRotation = unit.rotation - 90 + (rotate ? mount.rotation : baseRotation),
        bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX, this.shootY),
        bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX, this.shootY),
        shootAngle = bulletRotation(unit, mount, bulletX, bulletY);

        if(alwaysShooting) mount.shoot = true;

        if(continuous && mount.bullet != null && mount.bullets.size > 0 && mount.bullet.type instanceof ContinuousBulletType){
            mount.bullets.removeAll(b -> b == null ||!b.isAdded() || b.type == null ||
            b.time >= b.lifetime || mount.bullet.type != mount.bulletWhich);
            for(Bullet bullet : mount.bullets){
                Vec2 pos = mount.shootPoints.get(bullet);
                float
                bx = mountX + Angles.trnsx(weaponRotation, this.shootX + pos.x, this.shootY + pos.y),
                by = mountY + Angles.trnsy(weaponRotation, this.shootX + pos.x, this.shootY + pos.y);
                bullet.set(bx, by);
                bullet.rotation(weaponRotation + 90);

                mount.reload = reload;
                mount.recoil = 1f;

                unit.vel.add(Tmp.v1.trns(bullet.rotation() + 180f, bullet.type.recoil * Time.delta));
                if(shootSound != Sounds.none && !headless){
                    if(mount.sound == null) mount.sound = new SoundLoop(shootSound, 1f);
                    mount.sound.update(bulletX, bulletY, true);
                }

                //target length of laser
                float shootLength = Math.min(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY), range());
                //current length of laser
                float curLength = Mathf.dst(bulletX, bulletY, bullet.aimX, bullet.aimY);
                //resulting length of the bullet (smoothed)
                float resultLength = Mathf.approachDelta(curLength, shootLength, aimChangeSpeed);
                //actual aim end point based on length
                Tmp.v1.trns(shootAngle, mount.lastLength = resultLength).add(bulletX, bulletY);

                bullet.aimX = Tmp.v1.x;
                bullet.aimY = Tmp.v1.y;

                if(alwaysContinuous && mount.shoot){
                    bullet.time = bullet.lifetime * bullet.type.optimalLifeFract * mount.warmup;
                    bullet.keepAlive = true;

                    unit.apply(shootStatus, shootStatusDuration);
                }
                if(bullet.time >= bullet.lifetime){
                    mount.bullets.remove(bullet);
                    mount.shootPoints.remove(bullet);
                }
            }
        }else{
            //heat decreases when not firing
            mount.heat = Math.max(mount.heat - Time.delta * unit.reloadMultiplier / cooldownTime, 0);

            if(mount.sound != null){
                mount.sound.update(bulletX, bulletY, false);
            }
        }

        //flip weapon shoot side for alternating weapons
        boolean wasFlipped = mount.side;
        if(otherSide != -1 && alternate && mount.side == flipSprite && mount.reload <= reload / 2f && lastReload > reload / 2f){
            unit.mounts[otherSide].side = !unit.mounts[otherSide].side;
            mount.side = !mount.side;
        }

        //shoot if applicable
        if(mount.shoot && //must be shooting
        can && //must be able to shoot
        !(bullet.killShooter && mount.totalShots > 0) && //if the bullet kills the shooter, you should only ever be able to shoot once
        (!useAmmo || unit.ammo > 0 || !state.rules.unitAmmo || unit.team.rules().infiniteAmmo) && //check ammo
        (!alternate || wasFlipped == flipSprite) &&
        mount.warmup >= minWarmup && //must be warmed up
        unit.vel.len() >= minShootVelocity && //check velocity requirements
        (mount.reload <= 0.0001f || (alwaysContinuous && mount.bullet == null)) && //reload has to be 0, or it has to be an always-continuous weapon
        (alwaysShooting || Angles.within(rotate ? mount.rotation : unit.rotation + baseRotation, mount.targetRotation, shootCone)) //has to be within the cone
        ){
            shoot(unit, mount, bulletX, bulletY, shootAngle);

            mount.reload = reload;

            if(useAmmo){
                unit.ammo--;
                if(unit.ammo < 0) unit.ammo = 0;
            }
        }
    }

    protected Teamc findTarget(Unit unit, float x, float y, float range, boolean air, boolean ground){
        if(markedUnit != null && markedUnit instanceof Teamc mark){
            return mark;
        }else return super.findTarget(unit, x, y, range, air, ground);
    }

    protected boolean checkTarget(Unit unit, Teamc target, float x, float y, float range){
        if(markedUnit != null && markedUnit instanceof Teamc mark){
            return Units.invalidateTarget(mark, unit.team, x, y, range + Math.abs(shootY));
        }else return super.checkTarget(unit, target, x, y, range);
    }

    protected Teamc findPlayerTarget(Unit unit, float x, float y, float range, boolean air, boolean ground){
        if(unit.controller() instanceof Player){
            float len = unit.dst(unit.aimX, unit.aimY);
            Tmp.v2.trns(unit.rotation, len).limit(range).add(x, y);
            float x1 = Tmp.v2.x, y1 = Tmp.v2.y;
            return Units.closestTarget(unit.team, x1, y1, 2 * tilesize,
            u -> u.checkTarget(air, ground), t -> ground && (unit.type.targetUnderBlocks || !t.block.underBullets));
        }
        return null;
    }

    @Override
    protected void shoot(Unit unit, WeaponMount m, float shootX, float shootY, float rotation){
        unit.apply(shootStatus, shootStatusDuration);
        if(!(m instanceof MarkWeaponMount mount)) return;
        mount.specialShoot = markShoot.copy();
        mount.specialBullet = markBullet.copy();

        if(controllable && unit.controller() instanceof Player){
            float
            mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
            mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);
            if(Mathf.chance(markChance) && markTimer < 0){
                mount.target = findPlayerTarget(unit, mountX, mountY, bullet.range, bullet.collidesAir, bullet.collidesGround);
                if(mount.target instanceof Healthc targetUnit){
                    markTimer = markTime;
                    markedUnit = targetUnit;
                }
            }
        }else{
            if(Mathf.chance(markChance) && markTimer < 0 && mount.target instanceof Healthc targetUnit){
                markedUnit = targetUnit;
                markTimer = markTime;
            }
        }

        if(markTimer > 0 && markedUnit != null && mount.specialShoot.shots > 1){
            if(mount.specialShoot.shots * markReduce > markTimer){
                mount.specialShoot = markShoot.copy();
                mount.specialShoot.shots = (int)(markTimer / markReduce) - 1;
            }else{
                mount.specialShoot = markShoot.copy();
            }
        }

        if(markTimer > 0 && markedUnit != null){
            mount.shootWhich = mount.specialShoot.copy();
            mount.bulletWhich = mount.specialBullet.copy();
        }else{
            mount.shootWhich = shoot.copy();
            mount.bulletWhich = bullet.copy();
        }

        if(mount.shootWhich.firstShotDelay > 0){
            mount.charging = true;
            chargeSound.at(shootX, shootY, Mathf.random(soundPitchMin, soundPitchMax));
            mount.bulletWhich.chargeEffect.at(shootX, shootY, rotation, bullet.keepVelocity || parentizeEffects ? unit : null);
        }


        mount.shootWhich.shoot(mount.barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
            //this is incremented immediately, as it is used for total bullet creation amount detection
            mount.totalShots++;
            int barrel = mount.barrelCounter;

            if(delay > 0f){
                Time.run(delay, () -> {
                    //hack: make sure the barrel is the same as what it was when the bullet was queued to fire
                    int prev = mount.barrelCounter;
                    mount.barrelCounter = barrel;
                    bullet(unit, mount, xOffset, yOffset, angle, mover);
                    mount.barrelCounter = prev;

                });
            }else{
                bullet(unit, mount, xOffset, yOffset, angle, mover);
            }
        }, () -> mount.barrelCounter++);
    }

    @Override
    protected void bullet(Unit unit, WeaponMount m, float xOffset, float yOffset, float angleOffset, Mover mover){
        if(!unit.isAdded()) return;
        if(!(m instanceof MarkWeaponMount mount)) return;
        mount.specialBullet = markBullet.copy();

        if(markTimer > 0 && markedUnit != null){
            mount.bulletWhich = mount.specialBullet.copy();
            markTimer -= markReduce;
        }else{
            mount.bulletWhich = bullet.copy();
        }

        mount.charging = false;
        float
        xSpread = Mathf.range(xRand),
        ySpread = Mathf.range(yRand),
        weaponRotation = unit.rotation - 90 + (rotate ? mount.rotation : baseRotation),
        mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
        mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y),
        bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread),
        bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX + xOffset + xSpread, this.shootY + yOffset + ySpread),
        shootAngle = bulletRotation(unit, mount, bulletX, bulletY) + angleOffset,
        lifeScl = mount.bulletWhich.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY) / mount.bulletWhich.range) : 1f,
        angle = shootAngle + Mathf.range(inaccuracy + mount.bulletWhich.inaccuracy);

        Entityc shooter = unit.controller() instanceof MissileAI ai ? ai.shooter : unit; //Pass the missile's shooter down to its bullets
        Bullet b = mount.bullet = mount.bulletWhich.create(unit, shooter, unit.team, bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd) + extraVelocity, lifeScl, null, mover, mount.aimX, mount.aimY, mount.target);
        handleBullet(unit, mount, mount.bullet);

        mount.bullets.add(mount.bullet);
        mount.shootPoints.put(b, new Vec2(xOffset, yOffset));

        if(!continuous){
            shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
        }

        ejectEffect.at(mountX, mountY, angle * Mathf.sign(this.x));
        mount.bulletWhich.shootEffect.at(bulletX, bulletY, angle, mount.bulletWhich.hitColor, unit);
        mount.bulletWhich.smokeEffect.at(bulletX, bulletY, angle, mount.bulletWhich.hitColor, unit);

        unit.vel.add(Tmp.v1.trns(shootAngle + 180f, mount.bulletWhich.recoil));
        Effect.shake(shake, shake, bulletX, bulletY);
        mount.recoil = 1f;
        if(recoils > 0){
            mount.recoils[mount.barrelCounter % recoils] = 1f;
        }
        mount.heat = 1f;
    }

    protected void handleBullet(Unit unit, WeaponMount mount, Bullet bullet){
        super.handleBullet(unit, mount, bullet);
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){
        super.draw(unit, mount);
        /*DrawMark(unit, mount);*/
    }

    public void DrawMark(Unit unit, WeaponMount mount){
        if(mount.target instanceof Sized sized && unit.controller() instanceof Player){
            float progress = Mathf.clamp(effectTimer);
            Tmp.v2.set(sized.getX(), sized.getY());
            Draw.color(unit.team.color);
            for(int l = 1; l < 5; ++l){
                float angle = (float)(90 * l);
                Tmp.v3.trns(angle + Time.time * 0.3f, 8 + Math.max(sized.hitSize() / 1.2f, 14) * (1 - Mathf.sin(Time.time, 12, 0.3f))).add(Tmp.v2);
                Drawn.tri(Tmp.v3.x, Tmp.v3.y, (2 + sized.hitSize() / 6) * progress, sized.hitSize() / 1.5f * progress, angle + Time.time * 0.3f - 180);
                Drawn.tri(Tmp.v3.x, Tmp.v3.y, (2 + sized.hitSize() / 6) * progress, sized.hitSize() / 6 * progress, angle + Time.time * 0.3f);
            }
            Lines.stroke(sized.hitSize() / 8);
            Drawn.arcProcess(Tmp.v2.x, Tmp.v2.y, sized.hitSize() / 1.5f, 1, 0, 90, progress);
        }
    }

    @Override
    public void addStats(UnitType u, Table t){
        super.addStats(u, t);
        t.row();
        t.add("[lightgray]" + new Stat("wh-mark-chance", StatCat.function).localized() + "[white]" + (int)(markChance * 100) + "%");
        t.row();
        t.add(Core.bundle.format("stat.wh-markBullet"));
        t.row();
        UIUtils.ammo(ObjectMap.of(u, markBullet)).display(t);
    }
    public static class MarkWeaponMount extends WeaponMount{
        public ShootPattern specialShoot;
        public BulletType specialBullet;
        public ShootPattern shootWhich;
        public BulletType bulletWhich;
        public Seq<Bullet> bullets = new Seq<>();
        public ObjectMap<Bullet, Vec2> shootPoints = new ObjectMap<>();

        public MarkWeaponMount(Weapon weapon){
            super(weapon);
        }
    }
}
