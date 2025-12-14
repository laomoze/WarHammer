package wh.entities.world.entities;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import wh.content.*;
import wh.gen.*;
import wh.graphics.*;

public class TitanUnit extends MechUnit{
    public boolean shader = true;
    public boolean shouldRegen = false;
    public int regenMount = 3;
    public int regenCount = 0;
    public boolean hasRegenerated = false;
    public boolean fullAbsorb = false;

    public float forceShield = 0f;
    public float lastForceShield = 0f;

    public float alpha;
    public float size;
    public float cooldownTimer = 0f;
    public boolean wasBroken;

    public float damageRecent;
    public float damageCount;
    public float damageCountTimer;
    public float fullAbsorbTimer;
    public float fullAbsorbReload = 300f;
    public float fullAbsorbReloadTimer;

    public float rotateAngle = 0f;

    public float LONG_AXIS;
    public float MINOR_AXIS;
    public float RADIUS;
    public float REGEN;
    public float MAX;
    public float COOLDOWN;
    public float RESTART_RATIO;
    public float REFLECT_CHANCE;
    public boolean SHADER;
    public boolean PERCENT_REGEN;
    public float PERCENT_REGEN_AMOUNT;
    public float DAMAGE_MAX;
    public float ACCUMULATE_DAMAGE;
    public float SHIELD_DAMAGE_MAX_PER;
    public float DAMAGE_MAX_PER_TICK;

    public float FULL_ABSORB_TIME;
    public float REGEN_THRESHOLD;

    public Effect ABSORB_EFFECT;
    public Effect REFLECT_EFFECT;

    public static TitanUnit create(){
        return new TitanUnit();
    }

    @Override
    public int classId(){
        return EntityRegister.getId(TitanUnit.class);
    }

    @Override
    public void setType(UnitType type){
        super.setType(type);
        if(type instanceof TitanUnitType t){
            LONG_AXIS = t.longAxis;
            MINOR_AXIS = t.minorAxis;
            RADIUS = t.radius;
            REGEN = t.regen;
            MAX = t.max;
            COOLDOWN = t.cooldown;
            RESTART_RATIO = t.restartRatio;
            REFLECT_CHANCE = t.reflectChance;

            SHADER = t.ignoreBulletAbsorb;
            PERCENT_REGEN = t.percentRegen;
            PERCENT_REGEN_AMOUNT = t.percentRegenAmount;

            DAMAGE_MAX = t.damageMax;
            SHIELD_DAMAGE_MAX_PER = t.shieldDamageMaxPer;
            DAMAGE_MAX_PER_TICK = SHIELD_DAMAGE_MAX_PER / 60f;

            ACCUMULATE_DAMAGE = t.accumulateDamage;
            REGEN_THRESHOLD = t.regenThreshold;
            FULL_ABSORB_TIME = t.fullAbsorbTime;

            ABSORB_EFFECT = t.absorbEffect;
            REFLECT_EFFECT = t.reflectEffect;
        }

    }

    public static float calculateInEllipse(Bullet bullet, Unit unit, float a, float b, float XY){
        float realX = bullet.x() - unit.x, realY = bullet.y() - unit.y;

        float unitRotation = unit.rotation - 90;
        float cosRot = Mathf.cosDeg(unitRotation);
        float sinRot = Mathf.sinDeg(unitRotation);

        float rotX = realX * cosRot - realY * sinRot;
        float rotY = realX * sinRot + realY * cosRot;

        float dx = rotX / a, dy = rotY / b;

        return XY == 1 ? dx : dy;
    }

    public final Cons<Bullet> ellipseShieldConsumer = bullet -> {
        if(bullet.team != this.team && bullet.type.absorbable && bullet.type.collides){
            float tx = calculateInEllipse(bullet, this, LONG_AXIS, MINOR_AXIS, 1);
            float ty = calculateInEllipse(bullet, this, LONG_AXIS, MINOR_AXIS, 0);

            if(tx * tx + ty * ty <= 1f){

                if(fullAbsorbTimer <= 0.001f){
                    absorbBullet(bullet, this);
                }else if(Mathf.chance(REFLECT_CHANCE) && bullet.type.reflectable && fullAbsorbTimer <= 0.001f){
                    reflectBullet(bullet, this);
                }else if(fullAbsorbTimer > 1f){
                    bullet.damage = 1f;
                    bullet.type.splashDamage = 0f;
                    bullet.type.splashDamageRadius = -1f;
                    bullet.absorb();
                    alpha = 1f;
                }
            }
        }
    };


    public void absorbBullet(Bullet bullet, TitanUnit unit){
        ABSORB_EFFECT.at(bullet);
        /* damage(bullet.type().shieldDamage(bullet));*/
        shieldDamage(bullet);
        bullet.absorb();
        unit.alpha = 1f;
    }

    public void reflectBullet(Bullet bullet, TitanUnit unit){
        bullet.owner = unit;
        bullet.team = unit.team;
        bullet.time += 1f;
        bullet.rotation(bullet.rotation() - 180);
        unit.alpha = 1f;
        REFLECT_EFFECT.at(bullet.x, bullet.y, 0, unit.team.color);
    }

    public void checkRadius(TitanUnit unit){
        Groups.bullet.intersect(unit.x - LONG_AXIS, unit.y - MINOR_AXIS, LONG_AXIS * 2f * size, MINOR_AXIS * 2f * size,
        ellipseShieldConsumer);
    }

    public void maintainShield(TitanUnit unit){
        unit.forceShield = Math.max(Integer.MAX_VALUE * 0.9f, unit.forceShield);
    }

    public void regenShield(TitanUnit unit){
        if(forceShield >= MAX && regenCount <= regenMount && !hasRegenerated && !shouldRegen){
            shouldRegen = true;
        }

        if(forceShield < REGEN_THRESHOLD * MAX && !hasRegenerated && shouldRegen && !wasBroken){
            lastForceShield = forceShield = MAX;
            shouldRegen = false;
            regenCount++;

            fullAbsorb = true;
            fullAbsorbTimer = FULL_ABSORB_TIME * 0.7f;
            maintainShield(this);
        }
        if(regenCount > regenMount){
            hasRegenerated = true;
            shouldRegen = false;
            regenCount = 0;
        }
    }

    public void shieldDamage(Bullet bullet){
        shieldDamage(bullet.type().shieldDamage(bullet), bullet.type().pierceArmor);
    }

    public void shieldDamage(float amount, boolean pierceArmor){
        boolean hadVoidShields = forceShield > 0.0001f;
        if(hadVoidShields){
            float limitAmount = Math.min(amount, DAMAGE_MAX);
            float actualDamage = Damage.applyArmor(limitAmount, pierceArmor ? armor * Mathf.clamp((MAX - forceShield) / MAX, 0f, 1f) : 0);
            float amount2 = Math.min(actualDamage, damageRecent);

            forceShield = forceShield - amount2;
            if(fullAbsorbTimer <= 0.001f) damageCount += amount2;
        }
    }

    @Override
    public void rawDamage(float amount){
        boolean hadShields = shield > 0.0001f;
        boolean hadVoidShields = forceShield > 0.0001f;

        if(Float.isNaN(health)) health = 0f;

        if(hadShields || hadVoidShields){
            shieldAlpha = 1f;
        }

        float limitAmount = Math.min(amount, DAMAGE_MAX);

        if(hadVoidShields){

            float voidShieldsDamage = Math.min(forceShield, limitAmount);
            float finalDamage = Math.min(voidShieldsDamage, damageRecent);
            forceShield -= finalDamage;

            amount -= finalDamage;
            damageRecent -= finalDamage;

            if(fullAbsorbTimer <= 0.001f) damageCount += Math.abs(finalDamage);
        }

        if(amount > 0 && hadShields && !hadVoidShields){
            float shieldDamage = Math.min(Math.max(shield, 0), limitAmount);
            shield -= shieldDamage;
            hitTime = 1f;
            amount -= shieldDamage;
        }
        if(amount > 0 && !hadShields && !hadVoidShields && type.killable){
            health -= limitAmount;
            if(health <= 0 && !dead){
                kill();
            }
        }

        if(hadShields && shield <= 0.0001f || hadVoidShields && forceShield <= 0.0001f){
            Fx.unitShieldBreak.at(x, y, 0, type.shieldColor(self()), this);
        }
    }

    public Interval interval = new Interval();

    @Override
    public void update(){
        super.update();

        fullAbsorbTimer = Math.max(fullAbsorbTimer - Time.delta, 0f);
        fullAbsorbReloadTimer = Math.max(fullAbsorbReloadTimer - Time.delta, 0f);

        damageRecent = Math.min(damageRecent + DAMAGE_MAX_PER_TICK * Time.delta, SHIELD_DAMAGE_MAX_PER);

        damageCountTimer += Time.delta;
        if(damageCountTimer > 60f){
            damageCountTimer = 0f;
            damageCount = 0f;
        }

        if(forceShield <= 0.001f && !wasBroken){
            forceShield = 0f;
            cooldownTimer = COOLDOWN;
            wasBroken = true;
        }

        size = Mathf.lerpDelta(size, wasBroken ? 0 : 1, 0.08f);

        if(wasBroken) hasRegenerated = false;

        if(cooldownTimer > 0){
            cooldownTimer -= Time.delta;
            if(cooldownTimer <= 0 && forceShield < 0){
                forceShield = MAX * RESTART_RATIO;
                wasBroken = false;
            }
        }

        if(forceShield < MAX && cooldownTimer <= 0){
            if(fullAbsorbTimer <= 0.001f){
                forceShield += Time.delta * REGEN;
            }
            if(PERCENT_REGEN){
                forceShield += Time.delta * MAX * (PERCENT_REGEN_AMOUNT / 60f);
            }
            forceShield = fullAbsorbTimer > 0.0001f ? forceShield : Math.min(forceShield, MAX);
            wasBroken = false;
        }

        alpha = Math.max(alpha - Time.delta / 10f, 0f);

        //

        if(fullAbsorbTimer <= 0.001f && !fullAbsorb) lastForceShield = forceShield;

        regenShield(this);

        if(damageCount > ACCUMULATE_DAMAGE && fullAbsorbTimer <= 0.001f && !fullAbsorb && fullAbsorbReloadTimer <= 0.001f){
            fullAbsorb = true;
            fullAbsorbTimer = FULL_ABSORB_TIME;
            maintainShield(this);
        }

        if(fullAbsorbTimer <= 0.001f && fullAbsorb){
            fullAbsorbReloadTimer = fullAbsorbReload;
            fullAbsorbTimer = 0f;
            forceShield = lastForceShield;

            fullAbsorb = false;
        }

        if(forceShield > 0.001f && cooldownTimer <= 0.001f){
            checkRadius(this);
        }
    }


    @Override
    public void draw(){
        super.draw();
        Draw.color(this.type.shieldColor(this).cpy(), Color.white, Mathf.clamp(alpha));
        float Width = LONG_AXIS * size, Height = MINOR_AXIS * size;
        boolean hadVoidShields = forceShield > 0.0001f;
        if(!hadVoidShields){
            Draw.color(this.type.shieldColor(this), Color.white, Mathf.clamp(alpha));
            Lines.stroke(2 + hitSize * 0.03f + Mathf.absin(Time.time, 0.2f));
            float cooldownProgress = 1f - Math.abs((cooldownTimer / COOLDOWN));
            Drawn.ellipseProcess(x, y, 365, rotateAngle + rotation, LONG_AXIS, MINOR_AXIS, cooldownProgress);
        }
        if(Vars.renderer.animateShields){
            Draw.z(Layer.shields + 0.001f * alpha);
            Draw.z(shader ? WHContent.HEXAGONAL_SHIELD + 0.001f * alpha : Layer.shields + 0.001f * alpha);
            Drawn.ellipse(x, y, 50, rotateAngle + rotation, Width, Height);
        }else{
            Draw.z(Layer.shields);
            Lines.stroke(1.5f);
            Draw.alpha(0.09f);
            Drawn.ellipse(x, y, 50, rotateAngle + rotation, Width, Height);
            Draw.alpha(1f);
            Lines.ellipse(x, y, 50, Width, Height, rotateAngle + rotation);
        }

        Draw.reset();
    }


    @Override
    public void display(Table t){
        super.display(t);
        t.row();
        t.table(bars -> {
            bars.defaults().growX().pad(5).height(20f);
            bars.add(new Bar(Core.bundle.format("bar.wh-full-absorb"), Pal.accent, () -> fullAbsorbTimer / FULL_ABSORB_TIME));
            bars.row();
            bars.add(new Bar("stat.shieldhealth", Pal.accent, () -> Mathf.clamp(forceShield / MAX, 0, 1)));
        }).growX().padBottom(5);
    }

    @Override
    public void read(Reads read){
        super.read(read);
        fullAbsorbTimer = read.f();
        fullAbsorb = read.bool();
        fullAbsorbReloadTimer = read.f();
        fullAbsorbReload = read.f();
        lastForceShield = read.f();
        forceShield = read.f();
        damageRecent = read.f();
        damageCount = read.f();
        damageCountTimer = read.f();
        cooldownTimer = read.f();
        alpha = read.f();
        size = read.f();
        shouldRegen = read.bool();
        hasRegenerated = read.bool();
        regenMount = read.i();
        regenCount = read.i();
        wasBroken = read.bool();

        LONG_AXIS = read.f();
        MINOR_AXIS = read.f();
        RADIUS = read.f();
        REGEN = read.f();
        MAX = read.f();
        COOLDOWN = read.f();
        RESTART_RATIO = read.f();
        REFLECT_CHANCE = read.f();
        SHADER = read.bool();
        PERCENT_REGEN = read.bool();
        PERCENT_REGEN_AMOUNT = read.f();
        DAMAGE_MAX = read.f();
        ACCUMULATE_DAMAGE = read.f();
        SHIELD_DAMAGE_MAX_PER = read.f();
        DAMAGE_MAX_PER_TICK = read.f();
        FULL_ABSORB_TIME = read.f();
        REGEN_THRESHOLD = read.f();

    }

    @Override
    public void write(Writes write){
        super.write(write);
        write.f(fullAbsorbTimer);
        write.bool(fullAbsorb);
        write.f(fullAbsorbReloadTimer);
        write.f(fullAbsorbReload);
        write.f(lastForceShield);
        write.f(forceShield);
        write.f(damageRecent);
        write.f(damageCount);
        write.f(damageCountTimer);
        write.f(cooldownTimer);
        write.f(alpha);
        write.f(size);
        write.bool(shouldRegen);
        write.bool(hasRegenerated);
        write.i(regenMount);
        write.i(regenCount);
        write.bool(wasBroken);

        write.f(LONG_AXIS);
        write.f(MINOR_AXIS);
        write.f(RADIUS);
        write.f(REGEN);
        write.f(MAX);
        write.f(COOLDOWN);
        write.f(RESTART_RATIO);
        write.f(REFLECT_CHANCE);
        write.bool(SHADER);
        write.bool(PERCENT_REGEN);
        write.f(PERCENT_REGEN_AMOUNT);
        write.f(DAMAGE_MAX);
        write.f(ACCUMULATE_DAMAGE);
        write.f(SHIELD_DAMAGE_MAX_PER);
        write.f(DAMAGE_MAX_PER_TICK);
        write.f(FULL_ABSORB_TIME);
        write.f(REGEN_THRESHOLD);
    }

    @Override
    public void readSync(Reads read){
        super.readSync(read);
        if(!isLocal()){
            forceShield = read.f();
            fullAbsorbTimer = read.f();
            fullAbsorb = read.bool();
            fullAbsorbReloadTimer = read.f();
            fullAbsorbReload = read.f();
            lastForceShield = read.f();
            damageRecent = read.f();
            damageCount = read.f();
            damageCountTimer = read.f();
            cooldownTimer = read.f();
            alpha = read.f();
            size = read.f();
            shouldRegen = read.bool();
            hasRegenerated = read.bool();
            regenMount = read.i();
            regenCount = read.i();
            wasBroken = read.bool();

            LONG_AXIS = read.f();
            MINOR_AXIS = read.f();
            RADIUS = read.f();
            REGEN = read.f();
            MAX = read.f();
            COOLDOWN = read.f();
            RESTART_RATIO = read.f();
            REFLECT_CHANCE = read.f();
            SHADER = read.bool();
            PERCENT_REGEN = read.bool();
            PERCENT_REGEN_AMOUNT = read.f();
            DAMAGE_MAX = read.f();
            ACCUMULATE_DAMAGE = read.f();
            SHIELD_DAMAGE_MAX_PER = read.f();
            DAMAGE_MAX_PER_TICK = read.f();
            FULL_ABSORB_TIME = read.f();
            REGEN_THRESHOLD = read.f();

        }else{

            read.f();
            read.f();
            read.bool();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.bool();
            read.bool();
            read.i();
            read.i();
            read.bool();

            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.bool();
            read.bool();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
            read.f();
        }
    }

    @Override
    public void writeSync(Writes write){
        super.writeSync(write);
        write.f(forceShield);
        write.f(fullAbsorbTimer);
        write.bool(fullAbsorb);
        write.f(fullAbsorbReloadTimer);
        write.f(fullAbsorbReload);
        write.f(lastForceShield);
        write.f(damageRecent);
        write.f(damageCount);
        write.f(damageCountTimer);
        write.f(cooldownTimer);
        write.f(alpha);
        write.f(size);
        write.bool(shouldRegen);
        write.bool(hasRegenerated);
        write.i(regenMount);
        write.i(regenCount);
        write.bool(wasBroken);

        write.f(LONG_AXIS);
        write.f(MINOR_AXIS);
        write.f(RADIUS);
        write.f(REGEN);
        write.f(MAX);
        write.f(COOLDOWN);
        write.f(RESTART_RATIO);
        write.f(REFLECT_CHANCE);
        write.bool(SHADER);
        write.bool(PERCENT_REGEN);
        write.f(PERCENT_REGEN_AMOUNT);
        write.f(DAMAGE_MAX);
        write.f(ACCUMULATE_DAMAGE);
        write.f(SHIELD_DAMAGE_MAX_PER);
        write.f(DAMAGE_MAX_PER_TICK);
        write.f(FULL_ABSORB_TIME);
        write.f(REGEN_THRESHOLD);
    }
}
