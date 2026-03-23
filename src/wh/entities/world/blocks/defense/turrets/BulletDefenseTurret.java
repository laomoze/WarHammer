package wh.entities.world.blocks.defense.turrets;

import arc.audio.*;
import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.core.*;
import wh.entities.world.drawer.part.*;
import wh.graphics.*;

public class BulletDefenseTurret extends Turret {

    /*TextureRegion baseRegion;
    TextureRegion region;*/
    public final int timerTarget = timers++;
    public float retargetTime = 8f;
    public Color color = Pal.missileYellowBack;
    public float checkRange = 12f;
    public float shootLength = 13f;

    public BulletType interceptor = new InterceptorBulletType() {
        {
            width = 8F;
            height = 14F;
            collidesTeam = false;
            hitSize = 20f;
            speed = 20f;
            damage = 300f;
            lifetime = 17.5f;
            hittable = false;
            absorbable = false;
            trailLength = 3;
            trailWidth = width / 4.5f;
            hitColor=trailColor = Pal.missileYellowBack;
            hitEffect = despawnEffect = WHFx.generalExplosion(30, hitColor, 20, 3, false);
        }
       @Override
        public void update(Bullet b) {
            super.update(b);
            Groups.bullet.intersect(b.x - checkRange, b.y - checkRange, checkRange * 2, checkRange * 2, bullet -> {
                if(isTargetableBullet(b.team, bullet)){
                    if (bullet.damage > b.damage) {
                       // b.remove();
                        bullet.damage((bullet.damage() - damage) * 0.85f);
                        hitEffect.at(bullet.x, bullet.y, hitColor);
                    } else {
                        b.remove();
                        bullet.remove();
                        hitEffect.at(bullet.x, bullet.y, hitColor);
                    }
                }
            });
        }
    };

    public Effect shootEffect = Fx.sparkShoot;
    public Sound shootSound = Sounds.shootDisperse;

    public BulletDefenseTurret(String name) {
        super(name);
        rotateSpeed = 10f;
        reload = 6f;
        range = 350f;
        scaledHealth = 500;
        hasPower = true;
        outlineColor = WHPal.Outline;
        outlineRadius = 3;
        consumePower(1800 / 60f);
        float aimLength = 48f;
        clipSize = aimLength * 2f;
        drawer = new DrawTurret(WarHammerMod.name("turret-")){
            {
                parts.add(new AimLaserPart() {{
                    alpha = PartProgress.warmup.mul(0.5f).add(1f);
                    blending = Blending.additive;
                    length = aimLength;
                    y = 24 / 4f;
                    x = 31 / 4f;
                }});
            }
        };
    }

    protected boolean isTargetableBullet(Team ownTeam, @Nullable Bullet bullet){
        return bullet != null
        && bullet.isAdded()
        && bullet.team != ownTeam
        && bullet.type() != null && bullet.type().hittable;
    }


    @Override
    public void load(){
        super.load();
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.reload, 60f / reload, StatUnit.perSecond);
    }

    @Override
    public void init(){
        WHItemTurret.intTurret(this);
        super.init();
    }

    public class BulletDefenseTurretBuild extends TurretBuild {
        public @Nullable Bullet bulletTarget;

        @Override
        public void updateTile() {

            if(timer(timerTarget, retargetTime)){
                bulletTarget = Groups.bullet.intersect(x - range, y - range, range * 2, range * 2)
                .min(b -> isTargetableBullet(team, b), b -> b.dst2(this));
            }

            if(bulletTarget != null){
                warmupHold = 60f;
            }

            if(bulletTarget != null && !bulletTarget.isAdded()){
                bulletTarget = null;
            }

            target = bulletTarget;

            boolean canShoot = canConsume() && (bulletTarget != null || warmupHold > 0);
            float warmupTarget = canShoot ? 1f : 0f;
            if (warmupHold > 0) {
                warmupHold -= Time.delta;
            }

            shootWarmup = Mathf.lerpDelta(shootWarmup, warmupTarget, warmupTarget > 0 ? 0.06f : 0.1f);
            if(coolant != null){
                updateCooling();
            }
            if(isTargetableBullet(team, bulletTarget) && bulletTarget.within(this, range)){
                float dest = angleTo(bulletTarget);
                rotation = Angles.moveToward(rotation, dest, Math.max(rotateSpeed * edelta(), 0.5f));
                reloadCounter += edelta();
                Tmp.v1.trns(rotation, shootLength);
                if (Angles.within(rotation, dest, Math.max(shootCone,10)) && reloadCounter >= reload) {
                    interceptor.create(this, team, x + Tmp.v1.x, y + Tmp.v1.y, rotation, interceptor.damage, 1f, 1f, bulletTarget);
                    shootEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation, color);
                    shootSound.at(x + Tmp.v1.x, y + Tmp.v1.y, Mathf.random(0.9f, 1.1f));
                    reloadCounter = 0;
                  /*  realDamage = bulletDamage * state.rules.blockDamage(team);*/
                }
            }
        }

        @Override
        public boolean shouldConsume() {
            return enabled && bulletTarget != null;
        }

        @Override
        public boolean hasAmmo(){
            return true;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(rotation);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            rotation = read.f();
        }
    }
}
