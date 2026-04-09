package wh.gen;

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
import wh.entities.world.entities.*;
import wh.graphics.*;

public class TitanUnit extends MechUnit{
    private static final float shieldEpsilon = 0.001f;

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

    private TitanUnitType titanType;

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
        titanType = type instanceof TitanUnitType t ? t : null;
        if(titanType != null){
            shader = titanType.shader;
        }
    }

    private float longAxis(){
        return titanType != null ? titanType.longAxis : 0f;
    }

    private float minorAxis(){
        return titanType != null ? titanType.minorAxis : 0f;
    }

    private float regen(){
        return titanType != null ? titanType.regen : 0f;
    }

    private float maxShield(){
        return titanType != null ? titanType.max : 0f;
    }

    private float cooldown(){
        return titanType != null ? titanType.cooldown : 0f;
    }

    private float restartRatio(){
        return titanType != null ? titanType.restartRatio : 0f;
    }

    private float reflectChance(){
        return titanType != null ? titanType.reflectChance : 0f;
    }

    private boolean ignoreBulletAbsorb(){
        return titanType != null && titanType.ignoreBulletAbsorb;
    }

    private boolean percentRegen(){
        return titanType != null && titanType.percentRegen;
    }

    private float percentRegenAmount(){
        return titanType != null ? titanType.percentRegenAmount : 0f;
    }

    private float damageMax(){
        return titanType != null ? titanType.damageMax : 0f;
    }

    private float accumulateDamage(){
        return titanType != null ? titanType.accumulateDamage : 0f;
    }

    private float shieldDamageMaxPer(){
        return titanType != null ? titanType.shieldDamageMaxPer : 0f;
    }

    private float damageMaxPerTick(){
        return shieldDamageMaxPer() / 60f;
    }

    private float fullAbsorbTime(){
        return titanType != null ? titanType.fullAbsorbTime : 0f;
    }

    private float regenThreshold(){
        return titanType != null ? titanType.regenThreshold : 0f;
    }

    private Effect absorbEffect(){
        return titanType != null && titanType.absorbEffect != null ? titanType.absorbEffect : Fx.absorb;
    }

    private Effect reflectEffect(){
        return titanType != null && titanType.reflectEffect != null ? titanType.reflectEffect : Fx.dynamicExplosion;
    }

    public static boolean inEllipse(Bullet bullet, Unit unit, float axisX, float axisY){
        if(axisX <= 0f || axisY <= 0f) return false;

        float realX = bullet.x() - unit.x, realY = bullet.y() - unit.y;

        float unitRotation = unit.rotation - 90f;
        float cosRot = Mathf.cosDeg(unitRotation);
        float sinRot = Mathf.sinDeg(unitRotation);

        float rotX = realX * cosRot - realY * sinRot;
        float rotY = realX * sinRot + realY * cosRot;

        float dx = rotX / axisX, dy = rotY / axisY;
        return dx * dx + dy * dy <= 1f;
    }

    public final Cons<Bullet> ellipseShieldConsumer = bullet -> {
        if(bullet.team != this.team && (ignoreBulletAbsorb() || bullet.type.absorbable) && bullet.type.collides){
            float axisX = longAxis() * size, axisY = minorAxis() * size;
            if(inEllipse(bullet, this, axisX, axisY)){
                if(fullAbsorbTimer <= shieldEpsilon){
                    if(bullet.type.reflectable && Mathf.chance(reflectChance())){
                        reflectBullet(bullet, this);
                    }else{
                        absorbBullet(bullet, this);
                    }
                }else{
                    // Full absorb mode nullifies all incoming bullets.
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
        absorbEffect().at(bullet);
        /* damage(bullet.type().shieldDamage(bullet));*/
        shieldDamage(bullet);
        bullet.absorb();
        unit.alpha = 1f;
    }

    public void reflectBullet(Bullet bullet, TitanUnit unit){
        bullet.owner = unit;
        bullet.team = unit.team;
        bullet.time += 1f;
        bullet.rotation(bullet.rotation() - 180f);
        unit.alpha = 1f;
        reflectEffect().at(bullet.x, bullet.y, 0f, unit.team.color);
    }

    public void checkRadius(TitanUnit unit){
        float axisX = longAxis() * size, axisY = minorAxis() * size;
        if(axisX <= 0f || axisY <= 0f) return;
        Groups.bullet.intersect(unit.x - axisX, unit.y - axisY, axisX * 2f, axisY * 2f, ellipseShieldConsumer);
    }

    public void maintainShield(TitanUnit unit){
        unit.forceShield = Math.max(Integer.MAX_VALUE * 0.9f, unit.forceShield);
    }

    public void regenShield(TitanUnit unit){
        float max = maxShield();
        if(max <= 0f) return;

        if(forceShield >= max && regenCount <= regenMount && !hasRegenerated && !shouldRegen){
            shouldRegen = true;
        }

        if(forceShield < regenThreshold() * max && !hasRegenerated && shouldRegen && !wasBroken){
            lastForceShield = forceShield = max;
            shouldRegen = false;
            regenCount++;

            fullAbsorb = true;
            fullAbsorbTimer = fullAbsorbTime() * 0.7f;
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
            float max = maxShield();
            float limitAmount = Math.min(amount, damageMax());
            float missingShieldRatio = max <= 0f ? 1f : Mathf.clamp((max - forceShield) / max, 0f, 1f);
            float effectiveArmor = pierceArmor ? armor * missingShieldRatio : 0f;
            float actualDamage = Damage.applyArmor(limitAmount, effectiveArmor);
            float amount2 = Math.min(actualDamage, damageRecent);

            forceShield -= amount2;
            damageRecent -= amount2;
            if(fullAbsorbTimer <= shieldEpsilon) damageCount += amount2;
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

        float limitAmount = Math.min(amount, damageMax());

        if(hadVoidShields){

            float voidShieldsDamage = Math.min(forceShield, limitAmount);
            float finalDamage = Math.min(voidShieldsDamage, damageRecent);
            forceShield -= finalDamage;

            amount -= finalDamage;
            damageRecent -= finalDamage;

            if(fullAbsorbTimer <= shieldEpsilon) damageCount += Math.abs(finalDamage);
        }

        if(amount > 0f && hadShields && !hadVoidShields){
            float shieldDamage = Math.min(Math.max(shield, 0f), limitAmount);
            shield -= shieldDamage;
            hitTime = 1f;
            amount -= shieldDamage;
        }
        if(amount > 0f && !hadShields && !hadVoidShields && type.killable){
            health -= limitAmount;
            if(health <= 0f && !dead){
                kill();
            }
        }

        if(hadShields && shield <= 0.0001f || hadVoidShields && forceShield <= 0.0001f){
            Fx.unitShieldBreak.at(x, y, 0f, type.shieldColor(self()), this);
        }
    }

    @Override
    public void update(){
        super.update();

        float max = maxShield();
        float cooldown = cooldown();

        fullAbsorbTimer = Math.max(fullAbsorbTimer - Time.delta, 0f);
        fullAbsorbReloadTimer = Math.max(fullAbsorbReloadTimer - Time.delta, 0f);

        damageRecent = Math.min(damageRecent + damageMaxPerTick() * Time.delta, shieldDamageMaxPer());

        damageCountTimer += Time.delta;
        if(damageCountTimer > 60f){
            damageCountTimer = 0f;
            damageCount = 0f;
        }

        if(forceShield <= shieldEpsilon && !wasBroken){
            forceShield = 0f;
            cooldownTimer = cooldown;
            wasBroken = true;
        }

        size = Mathf.lerpDelta(size, wasBroken ? 0f : 1f, 0.08f);

        if(wasBroken) hasRegenerated = false;

        if(cooldownTimer > 0f){
            cooldownTimer -= Time.delta;
            if(cooldownTimer <= 0f && forceShield <= shieldEpsilon){
                forceShield = max * restartRatio();
                wasBroken = false;
            }
        }

        if(forceShield < max && cooldownTimer <= 0f){
            if(fullAbsorbTimer <= shieldEpsilon){
                forceShield += Time.delta * regen();
            }
            if(percentRegen()){
                forceShield += Time.delta * max * (percentRegenAmount() / 60f);
            }
            forceShield = fullAbsorbTimer > shieldEpsilon ? forceShield : Math.min(forceShield, max);
            wasBroken = false;
        }

        alpha = Math.max(alpha - Time.delta / 10f, 0f);

        if(fullAbsorbTimer <= shieldEpsilon && !fullAbsorb) lastForceShield = forceShield;

        regenShield(this);

        if(damageCount > accumulateDamage() && fullAbsorbTimer <= shieldEpsilon && !fullAbsorb && fullAbsorbReloadTimer <= shieldEpsilon){
            fullAbsorb = true;
            fullAbsorbTimer = fullAbsorbTime();
            maintainShield(this);
        }

        if(fullAbsorbTimer <= shieldEpsilon && fullAbsorb){
            fullAbsorbReloadTimer = fullAbsorbReload;
            fullAbsorbTimer = 0f;
            forceShield = lastForceShield;

            fullAbsorb = false;
        }

        if(forceShield > shieldEpsilon && cooldownTimer <= shieldEpsilon){
            checkRadius(this);
        }
    }


    @Override
    public void draw(){
        super.draw();
        Draw.color(Tmp.c1.set(type.shieldColor(this)), Color.white, Mathf.clamp(alpha));
        float width = longAxis() * size, height = minorAxis() * size;
        boolean hadVoidShields = forceShield > 0.0001f;
        if(!hadVoidShields){
            Draw.color(type.shieldColor(this), Color.white, Mathf.clamp(alpha));
            Lines.stroke(2f + hitSize * 0.03f + Mathf.absin(Time.time, 0.2f));
            float maxCooldown = cooldown();
            float cooldownProgress = maxCooldown <= 0f ? 1f : 1f - Math.abs(cooldownTimer / maxCooldown);
            Drawn.ellipseProcess(x, y, 365, rotateAngle + rotation, longAxis(), minorAxis(), cooldownProgress);
        }
        if(Vars.renderer.animateShields){
            Draw.z(Layer.shields + 0.001f * alpha);
            Draw.z(shader ? WHContent.HEXAGONAL_SHIELD + 0.001f * alpha : Layer.shields + 0.001f * alpha);
            Drawn.ellipse(x, y, 50, rotateAngle + rotation, width, height);
        }else{
            Draw.z(Layer.shields);
            Lines.stroke(1.5f);
            Draw.alpha(0.09f);
            Drawn.ellipse(x, y, 50, rotateAngle + rotation, width, height);
            Draw.alpha(1f);
            Lines.ellipse(x, y, 50, width, height, rotateAngle + rotation);
        }

        Draw.reset();
    }


    @Override
    public void display(Table t){
        super.display(t);
        t.row();
        t.table(bars -> {
            bars.defaults().growX().pad(5).height(20f);
            bars.add(new Bar(Core.bundle.format("bar.wh-full-absorb"), Pal.accent, () -> {
                float fullTime = fullAbsorbTime();
                return fullTime <= 0f ? 0f : fullAbsorbTimer / fullTime;
            }));
            bars.row();
            bars.add(new Bar("stat.shieldhealth", Pal.accent, () -> {
                float max = maxShield();
                return max <= 0f ? 0f : Mathf.clamp(forceShield / max, 0f, 1f);
            }));
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

        // Keep legacy stream layout for save compatibility.
        read.f(); // LONG_AXIS
        read.f(); // MINOR_AXIS
        read.f(); // RADIUS
        read.f(); // REGEN
        read.f(); // MAX
        read.f(); // COOLDOWN
        read.f(); // RESTART_RATIO
        read.f(); // REFLECT_CHANCE
        read.bool(); // SHADER
        read.bool(); // PERCENT_REGEN
        read.f(); // PERCENT_REGEN_AMOUNT
        read.f(); // DAMAGE_MAX
        read.f(); // ACCUMULATE_DAMAGE
        read.f(); // SHIELD_DAMAGE_MAX_PER
        read.f(); // DAMAGE_MAX_PER_TICK
        read.f(); // FULL_ABSORB_TIME
        read.f(); // REGEN_THRESHOLD
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

        // Keep legacy stream layout for save compatibility.
        write.f(longAxis());
        write.f(minorAxis());
        write.f((longAxis() + minorAxis()) * 0.5f);
        write.f(regen());
        write.f(maxShield());
        write.f(cooldown());
        write.f(restartRatio());
        write.f(reflectChance());
        write.bool(shader);
        write.bool(percentRegen());
        write.f(percentRegenAmount());
        write.f(damageMax());
        write.f(accumulateDamage());
        write.f(shieldDamageMaxPer());
        write.f(damageMaxPerTick());
        write.f(fullAbsorbTime());
        write.f(regenThreshold());
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

            // Keep legacy stream layout for sync compatibility.
            read.f(); // LONG_AXIS
            read.f(); // MINOR_AXIS
            read.f(); // RADIUS
            read.f(); // REGEN
            read.f(); // MAX
            read.f(); // COOLDOWN
            read.f(); // RESTART_RATIO
            read.f(); // REFLECT_CHANCE
            read.bool(); // SHADER
            read.bool(); // PERCENT_REGEN
            read.f(); // PERCENT_REGEN_AMOUNT
            read.f(); // DAMAGE_MAX
            read.f(); // ACCUMULATE_DAMAGE
            read.f(); // SHIELD_DAMAGE_MAX_PER
            read.f(); // DAMAGE_MAX_PER_TICK
            read.f(); // FULL_ABSORB_TIME
            read.f(); // REGEN_THRESHOLD
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

            // Keep legacy stream layout for sync compatibility.
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

        // Keep legacy stream layout for sync compatibility.
        write.f(longAxis());
        write.f(minorAxis());
        write.f((longAxis() + minorAxis()) * 0.5f);
        write.f(regen());
        write.f(maxShield());
        write.f(cooldown());
        write.f(restartRatio());
        write.f(reflectChance());
        write.bool(shader);
        write.bool(percentRegen());
        write.f(percentRegenAmount());
        write.f(damageMax());
        write.f(accumulateDamage());
        write.f(shieldDamageMaxPer());
        write.f(damageMaxPerTick());
        write.f(fullAbsorbTime());
        write.f(regenThreshold());
    }
}
