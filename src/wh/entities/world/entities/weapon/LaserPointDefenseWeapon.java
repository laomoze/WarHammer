package wh.entities.world.entities.weapon;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.weapons.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.meta.*;

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

    public boolean removeRange = true;
    public float removeRangeRadius = 18;
    public float damageInterval = 6;

    protected Interval damageTimer = new Interval();

    {
        shootSound = Sounds.tractorbeam;
        predictTarget = false;
        autoTarget = true;
        controllable = false;
        rotate = true;
        rotateSpeed = 15;
        useAmmo = false;
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
    }

    @Override
    public void addStats(UnitType u, Table t){
        if(damage > 0){
            t.row();
            t.add("[lightgray]" + Stat.damage.localized() + ": [white]" + (int)damage * (60f / damageInterval) + " " + StatUnit.perSecond.localized());
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
            float bulletDamage = damage * mount.damageMul * unit.damageMultiplier() * state.rules.unitDamage(unit.team);
            if(target.damage() > bulletDamage){
                if(damageTimer.get(damageInterval)){
                    target.damage(target.damage() - bulletDamage);
                }
            }else{
                target.remove();
            }
            if(removeRange && removeRangeRadius > 0){
                Groups.bullet.intersect(mount.lastX - removeRangeRadius, mount.lastY - removeRangeRadius, removeRangeRadius * 2, removeRangeRadius * 2, b -> {
                    if(b.team != unit.team && b.type.hittable){
                        if(damageTimer.get(damageInterval)){
                            b.damage(b.damage() - bulletDamage * 0.1f);
                        }
                        if(target.damage() < bulletDamage * 0.1f) b.remove();
                    }
                });
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
        public float damageMul;

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
