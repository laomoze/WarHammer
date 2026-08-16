package wh.entities.world.entities.weapon;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.scene.ui.layout.Table;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.weapons.PointDefenseWeapon;
import mindustry.world.blocks.defense.turrets.TractorBeamTurret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

public class LaserPointDefenseWeapon extends PointDefenseWeapon{
    float reRotateTime = 180f;
    public float laserWidth = 0.75f;
    public float maintainTime = 10;

    public Boolean drawTri = true;
    public float triLength = 20f, triRotation = 0f, triRotationSpeed = 0f;

    public float shootSoundVolume = 0.8f;
    public float damage = 10f;
    public boolean useTeamColor = true;
    public float damageMultiplier = 5;
    public float damageInterval = 6f;

    public boolean removeRange = true;
    public float removeRangeRadius = 18;

    {
        shootSound = Sounds.beamLustre;
        predictTarget = false;
        autoTarget = true;
        controllable = false;
        rotate = true;
        rotateSpeed = 15;
        useAttackRange = false;
        targetInterval = targetSwitchInterval = 4;

        shootCone = 5;
        bullet = new BulletType(){{
            damage = 4;
            maxRange = 160;
            collidesGround = false;
        }};
    }

    public LaserPointDefenseWeapon(String name, float damage){
        super(name);
        this.damage = damage;
        mountType = LaserPointDefenseWeaponMount::new;
        shootCone = 10;
    }

    public LaserPointDefenseWeapon(String name){
        super(name);
        mountType = LaserPointDefenseWeaponMount::new;
        shootCone = 10;
    }

    public LaserPointDefenseWeapon(){
    }

    @Override
    public void init(){
        super.init();
        damageInterval = Math.max(damageInterval, 1f);
    }

    @Override
    public void addStats(UnitType u, Table t){
        if(damage > 0){
            t.row();
            t.add("[lightgray]" + Stat.damage.localized() + ": [white]" + (int)damage * 60 + " " + StatUnit.perSecond.localized());
        }
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
    }


    @Override
    public void update(Unit unit, WeaponMount m){

        LaserPointDefenseWeaponMount mount = (LaserPointDefenseWeaponMount)m;
        mount.maintain -= Time.delta;
        if(mount.any && mount.maintain > 0) mount.strength = Mathf.approachDelta(mount.strength, 1, 0.1f);
        if(!mount.any || mount.target == null || mount.maintain < 0){
            mount.strength = Mathf.lerpDelta(mount.strength, 0, 0.15f);
            if(mount.strength < 0.001f) mount.strength = 0;
            mount.maintain = 0;
        }

        if(mount.strength > 0.9f){
            mount.damageMul = Mathf.lerpDelta(1, damageMultiplier, 0.08f);
        }else if(mount.strength < 0.5f){
            mount.damageMul = Mathf.lerpDelta(mount.damageMul, 1, 0.1f);
        }

        float
        mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
        mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

        mount.mx = mountX;
        mount.my = mountY;

        if(mount.target != null){
            mount.reRotate = reRotateTime;
        }else{
            mount.reRotate = Math.max(mount.reRotate - Time.delta, 0f);
        }

        if(mount.target == null && !mount.shoot && !Angles.within(mount.rotation, mount.weapon.baseRotation, 0.01f) && mount.reRotate <= 0.001){
            mount.rotate = true;
            Tmp.v1.trns(unit.rotation + mount.weapon.baseRotation, 5f);
            mount.aimX = mountX + Tmp.v1.x;
            mount.aimY = mountY + Tmp.v1.y;
        }

        super.update(unit, m);

        if(mount.target != null && mount.shoot){
            mount.maintain = maintainTime;
        }

        if(!(m.target instanceof Bullet target)) return;

        mount.any = false;
        if(mount.maintain > 0 && Angles.within(rotate ? mount.rotation : unit.rotation + baseRotation, mount.targetRotation, shootCone)){
            mount.any = true;
            if(!headless){
                control.sound.loop(shootSound, mount, shootSoundVolume);
            }
            mount.lastX = target.x;
            mount.lastY = target.y;

            if (mount.damageTick.get(0, damageInterval)) {
                float bulletDamage = damage * mount.damageMul * unit.damageMultiplier() * state.rules.unitDamage(unit.team) / (60f / damageInterval);
                float splashDamage = bulletDamage * 0.1f;

                if (bulletDamage > 0f) {
                    if (target.damage() > bulletDamage) {
                        target.damage(target.damage() - bulletDamage);
                    } else {
                        target.remove();
                    }
                }

                if (removeRange && removeRangeRadius > 0f && splashDamage > 0f) {
                    Groups.bullet.intersect(mount.lastX - removeRangeRadius, mount.lastY - removeRangeRadius, removeRangeRadius * 2f, removeRangeRadius * 2f, b -> {
                        if (b.team != unit.team && b.type.hittable) {
                            if (b.damage() > splashDamage) {
                                b.damage(b.damage() - splashDamage);
                            } else {
                                b.remove();
                            }
                        }
                    });
                }
            }
        }

    }

    @Override
    public void draw(Unit unit, WeaponMount m){
        super.draw(unit, m);
        LaserPointDefenseWeaponMount mount = (LaserPointDefenseWeaponMount)m;
        Color c;
        if(useTeamColor){
            c = unit.team.color.cpy().lerp(Color.white, 0.2f);
        }else{
            c = color;
        }
        float z = Draw.z();
        if(mount.strength > 0.01f){
            Draw.z(Layer.bullet);
            float ang = mount.angleTo(mount.lastX, mount.lastY);

            Draw.mixcol(c, Mathf.absin(12f, 0.15f));

            TractorBeamTurret t = ((TractorBeamTurret)Blocks.parallax);

            Draw.color(c);
            Drawf.laser(t.laser, t.laserStart, t.laserEnd,
            mount.mx + Angles.trnsx(ang, shootY), mount.my + Angles.trnsy(ang, shootY),
            mount.lastX, mount.lastY, mount.strength * laserWidth);

            if(drawTri){
                for(int i = 0; i < 4; i++){
                    Drawf.tri(mount.mx + Angles.trnsx(ang, shootY), mount.my + Angles.trnsy(ang, shootY),
                    triLength / 6f * mount.strength, triLength * mount.strength, i * 90 + triRotation * mount.strength + triRotationSpeed * Time.time);
                }
                Draw.color();
                for(int i = 0; i < 4; i++){
                    Drawf.tri(mount.mx + Angles.trnsx(ang, shootY), mount.my + Angles.trnsy(ang, shootY),
                    triLength / 6f * mount.strength * 0.66f, triLength * mount.strength * 0.66f, i * 90 + triRotation * mount.strength + triRotationSpeed * Time.time);
                }
            }

            Draw.mixcol();
        }
        Draw.z(z);
        Draw.reset();
    }

    public static class LaserPointDefenseWeaponMount extends WeaponMount implements Position{
        public boolean any;
        public float lastX, lastY, strength;
        public float mx, my;
        public float reRotate;
        public float maintain;
        public float damageMul = 1f;
        public Interval damageTick = new Interval(1);

        public LaserPointDefenseWeaponMount(Weapon weapon){
            super(weapon);
        }

        @Override
        public float getX(){
            return mx;
        }

        @Override
        public float getY(){
            return my;
        }
    }
}
