package wh.entities.abilities;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import wh.content.*;
import wh.graphics.*;

import static mindustry.Vars.tilesize;
import static wh.core.WarHammerMod.name;

public class EllipseForceFieldAbility extends Ability{

    public float longAxis, minorAxis;
    public float radius = 60f;
    public float regen, max, cooldown, restartRatio;
    public float rotation = 0f;
    public float armor = 1f;

    public float reflectChance = 0.1f;
    public boolean shader = false;
    public boolean ignoreBulletAbsorb = false;

    public boolean percentRegen = false;
    public float percentRegenAmount = 0.05f;

    public boolean damageReduction = false;
    public float damageReductionAmount = 0.1f;


    public boolean Regen = false;
    public float regenThreshold = 0.1f;
    protected boolean shouldRegen = false;
    protected boolean start = false;

    protected float alpha, size, lastShield, cooldownTimer = 0f;
    protected boolean wasBroken = true;

    private static Unit paramUnit;
    private static EllipseForceFieldAbility paramField;
    public static Effect absorbEffect = Fx.absorb;
    public static Effect reflectEffect = Fx.dynamicExplosion;
  /*  public Effect BreakEffect = new Effect(120f, e -> {
        Draw.color(e.color);
        Lines.stroke(e.fout() * 3f);
            float[] ellipseData = (float[]) e.data;
            float radiusX = ellipseData[0];
            float radiusY = ellipseData[1];
        Drawn.ellipseProcess(e.x, e.y, 100, e.rotation+rotation, radiusX, radiusY, 1);
    }).followParent(true);*/

    public EllipseForceFieldAbility(float width, float length, float regen, float max, float cooldown, float restartRatio){
        this.longAxis = width;
        this.minorAxis = length;
        this.regen = regen;
        this.max = max;
        this.cooldown = cooldown;
        this.restartRatio = restartRatio;
    }

    public EllipseForceFieldAbility(float width, float length, float regen, float max, float cooldown, float restartRatio, float armor){
        this.longAxis = width;
        this.minorAxis = length;
        this.regen = regen;
        this.max = max;
        this.cooldown = cooldown;
        this.restartRatio = restartRatio;
        this.armor = armor;
    }

    public EllipseForceFieldAbility(){
    }

   /* private static float calculateInEllipse(Bullet bullet, Unit unit, float a, float b, float XY){
        float realX = bullet.x() - unit.x, realY = bullet.y() - unit.y;
        float X = Mathf.cosDeg(bullet.x), Y = Mathf.sinDeg(bullet.y);
        float rotX = realX * X - realY * Y, rotY = realX * Y + realY * X;
        float dx = rotX / a, dy = rotY / b;
        return XY == 1 ? dx : dy;
    }*/

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

    private static final Cons<Bullet> ellipseShieldConsumer = bullet -> {
        if(bullet.team != paramUnit.team && (paramField.ignoreBulletAbsorb || bullet.type.absorbable) && paramUnit.shield > 0){
            float tx = calculateInEllipse(bullet, paramUnit, paramField.longAxis, paramField.minorAxis, 1);
            float ty = calculateInEllipse(bullet, paramUnit, paramField.longAxis, paramField.minorAxis, 0);

            if(tx * tx + ty * ty <= 1f){
                if(Mathf.chance(paramField.reflectChance)){
                    reflectBullet(bullet, paramUnit);
                }else{
                    absorbBullet(bullet, paramUnit);
                }
            }
        }
    };

    protected static void absorbBullet(Bullet bullet, Unit unit){
        absorbEffect.at(bullet);
        unit.shield -= (paramField.damageReduction ? 1 - paramField.damageReductionAmount : 1) *
        (Math.max(bullet.type().shieldDamage(bullet) - (bullet.type().pierceArmor ? 0 : paramField.armor), bullet.type().shieldDamage(bullet) * 0.1f));
        paramField.alpha = 1f;
        bullet.absorb();
    }

    protected static void reflectBullet(Bullet bullet, Unit unit){
        bullet.owner = unit;
        bullet.team = unit.team;
        bullet.time += 1f;
        bullet.rotation(bullet.rotation() - 180);
        reflectEffect.at(bullet.x, bullet.y, 0, unit.team.color);
    }


    protected void regenShield(Unit unit){
        if(unit.shield <= regenThreshold * max && start && !shouldRegen && !wasBroken){
            unit.shield = max;
            shouldRegen = true;
            start = false;
        }
    }

    @Override
    public void update(Unit unit){

        if(unit.shield <= 0f && !wasBroken){
            unit.shield = -0.1f;
            unit.shield -= cooldown * regen;
            cooldownTimer = cooldown;
           /* float[] ellipseData = {longAxis, radiusY};
            BreakEffect.at(unit.x, unit.y, unit.type.hitSize, unit.type.shieldColor(unit), ellipseData);*/
        }

        size = Mathf.lerpDelta(size, wasBroken ? 0 : 1, 0.08f);

        wasBroken = unit.shield <= 0f;

        if(wasBroken){
            shouldRegen = start = false;
        }

        if(cooldownTimer > 0){
            cooldownTimer -= Time.delta;
            if(cooldownTimer <= 0 && unit.shield < 0){
                unit.shield = max * restartRatio;
            }
        }

        if(unit.shield < max && cooldownTimer <= 0){
            unit.shield += Time.delta * regen;
            unit.shield += (percentRegen ? Time.delta * max * (percentRegenAmount / 60) : 0);
        }else if(unit.shield > max&&unit.shield < max*1.1f){
            start = true;
        }

        alpha = Math.max(alpha - Time.delta / 10f, 0f);

        if(unit.shield > 0 && cooldownTimer <= 0){
            paramUnit = unit;
            paramField = this;
            checkRadius(unit);
            if(Regen) regenShield(unit);
        }

        lastShield = Mathf.clamp(unit.shield, 0, max);
    }

    public void checkRadius(Unit unit){
        paramUnit = unit;
        paramField = this;
        Groups.bullet.intersect(unit.x - longAxis, unit.y - minorAxis, longAxis * 2f * size, minorAxis * 2f * size, ellipseShieldConsumer);
    }

    @Override
    public void display(Table t){
        super.display(t);
    }

    @Override
    public void draw(Unit unit){
        Draw.color(unit.type.shieldColor(unit), Color.white, Mathf.clamp(alpha));
        float Width = longAxis * size, Height = minorAxis * size;
        if(unit.shield < 0){
            Draw.color(unit.type.shieldColor(unit), Color.white, Mathf.clamp(alpha));
            Lines.stroke(2 + unit.hitSize * 0.03f + Mathf.absin(Time.time, 0.2f));
            float cooldownProgress = 1f - Math.abs((cooldownTimer / cooldown));
            Drawn.ellipseProcess(unit.x, unit.y, 365, unit.rotation + rotation, longAxis, minorAxis, cooldownProgress);
        }
        if(Vars.renderer.animateShields){
            Draw.z(Layer.shields + 0.001f * alpha);
            Draw.z(shader ? WHContent.HEXAGONAL_SHIELD + 0.001f * alpha : Layer.shields + 0.001f * alpha);
            Drawn.ellipse(unit.x, unit.y, 50, unit.rotation + rotation, Width, Height);
        }else{
            Draw.z(Layer.shields);
            Lines.stroke(1.5f);
            Draw.alpha(0.09f);
            Drawn.ellipse(unit.x, unit.y, 50, unit.rotation + rotation, Width, Height);
            Draw.alpha(1f);
            Lines.ellipse(unit.x, unit.y, 50, Width, Height, unit.rotation + rotation);
        }
        Draw.reset();
    }

    @Override
    public void displayBars(Unit unit, Table bars){
        /*if(fullAbsorb) bars.add(new Bar(Core.bundle.format("bar.wh-full-absorb"), Pal.accent, () -> fullAbsorbTimer / fullAbsorbDuration)).row();*/
        bars.add(new Bar("stat.shieldhealth", Pal.accent, () -> Mathf.clamp(unit.shield / max, 0, 1))).row();
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(Core.bundle.format("stat.wh-range", Strings.autoFixed(radius / tilesize, 2)));
        t.row();
        t.add(abilityStat("shield", Strings.autoFixed(max, 2)));
        t.row();
        if(damageReduction){
            t.row();
            t.add(Core.bundle.format("stat.wh-damage-reduction", Strings.autoFixed(damageReductionAmount * 100, 2) + "%"));
        }
        t.add(abilityStat("repairspeed", Strings.autoFixed(regen * 60f, 2)));
        t.row();
        if(percentRegen){
            t.add(Core.bundle.format("stat.wh-extra-percent-regen", Strings.autoFixed(percentRegenAmount * 60, 2) + "%"));
            t.row();
        }
        t.add(abilityStat("cooldown", Strings.autoFixed(cooldown / 60f, 2)));
        t.row();
        t.add(Core.bundle.format("stat.wh-reflect-chance", Strings.autoFixed(reflectChance * 100, 2) + "%"));
        t.row();
        if(Regen){
            t.add(Core.bundle.format("stat.wh-regen", Strings.autoFixed(regenThreshold*100,2) + "%"));
            t.row();
        }
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability." + name("ellipse-force-field-ability"));
    }
}