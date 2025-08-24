package wh.entities.world.blocks.defense.turrets;

import arc.audio.Sound;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.InterceptorBulletType;
import mindustry.entities.effect.*;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.core.*;
import wh.entities.world.drawer.part.AimLaserPart;

public class BulletDefenseTurret extends Turret {

    /*TextureRegion baseRegion;
    TextureRegion region;*/
    public final int timerTarget = timers++;
    public float retargetTime = 8f;
    public Color color = Pal.missileYellowBack;
    public float checkRange = 15f;
    public float realDamage;
    public float shootLength = 13f;
    public DrawTurret drawer;

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
            hitEffect = Fx.hitLancer;
            despawnEffect = Fx.hitBulletSmall;
            trailLength = 3;
            trailWidth = 8/2.6f;
            hitColor=trailColor = Pal.missileYellowBack;
        }
       @Override
        public void update(Bullet b) {
            super.update(b);
            Groups.bullet.intersect(b.x - checkRange, b.y - checkRange, checkRange * 2, checkRange * 2, bullet -> {
                if (bullet.team != b.team && bullet.type().hittable) {
                    if (bullet.damage > b.damage) {
                       // b.remove();
                        bullet.damage((bullet.damage() - realDamage)*0.9f);
                        new WrapEffect(Fx.hitLancer, Pal.missileYellowBack).at(bullet.x, bullet.y);
                    } else {
                        b.remove();
                        bullet.remove();
                        new WrapEffect(Fx.hitLancer, Pal.missileYellowBack).at(bullet.x, bullet.y);
                    }
                }
            });
        }
    };

    public Effect shootEffect = Fx.sparkShoot;
    public Sound shootSound = Sounds.lasershoot;

    public BulletDefenseTurret(String name) {
        super(name);
        rotateSpeed = 10f;
        reload = 6f;
        range = 350f;
        scaledHealth = 500;
        hasPower = true;
        consumePower(30f);
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

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public void load(){
        super.load();

        drawer.load(this);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.reload, 60f / reload, StatUnit.perSecond);
    }

    public class BulletDefenseTurretBuild extends TurretBuild {
        public @Nullable Bullet target;

        @Override
        public void updateTile() {

            if(target != null && !target.isAdded()){
                target = null;
            }
            boolean canShoot = canConsume() && (target != null || warmupHold > 0);
            boolean shootTimeout = reloadCounter >= reload * 10;
            float warmupTarget = canShoot && !shootTimeout ? 1f : 0f;

            if (target != null) {
                warmupHold = 60f;
            }
            if (warmupHold > 0) {
                warmupHold -= Time.delta;
            }

            shootWarmup = Mathf.lerpDelta(shootWarmup, warmupTarget, warmupTarget > 0 ? 0.06f : 0.1f);

            if (timer(timerTarget, retargetTime)) {
                target = Groups.bullet.intersect(x - range, y - range, range * 2, range * 2)
                        .min(b -> b.team != team && b.type().hittable, b -> b.dst2(this));
            }
            if(coolant != null){
                updateCooling();
            }
            if (target != null && target.team != team && target.type().hittable) {
                float dest = angleTo(target);
                rotation = Angles.moveToward(rotation, dest, Math.max(rotateSpeed * edelta(), 0.5f));
                reloadCounter += edelta();
                Tmp.v1.trns(rotation, shootLength);
                if (Angles.within(rotation, dest, Math.max(shootCone,10)) && reloadCounter >= reload) {
                    interceptor.create(this, team, x + Tmp.v1.x, y + Tmp.v1.y, rotation, 100f, 1f, 1f, target);
                    shootEffect.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation, color);
                    shootSound.at(x + Tmp.v1.x, y + Tmp.v1.y, Mathf.random(0.9f, 1.1f));
                    reloadCounter = 0;
                  /*  realDamage = bulletDamage * state.rules.blockDamage(team);*/
                }
            }
        }


        @Override
        public boolean canConsume() {
            return power.status > 0.99f && super.canConsume();
        }

        @Override
        public boolean shouldConsume() {
            return super.shouldConsume() && target != null;
        }

        @Override
        public void draw(){
            drawer.draw(this);
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
