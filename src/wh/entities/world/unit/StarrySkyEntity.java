package wh.entities.world.unit;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import wh.content.WHFx;
import wh.gen.EntityRegister;
import wh.graphics.Drawn;
import wh.graphics.WHPal;


public class StarrySkyEntity extends UnitEntity {

    public TextureRegion armorRegion;
    public float duration = 480f;
    public float delay = 480f;
    public float cooldown = 480f;
    public float cooldownTime = 480f;
    public float damageMultp = 0.3f;
    public float damageReduce = 0.6f;
    public float MaxDamageMultiplier = 8;
    protected float currentDamage,timer;
    protected boolean triggered,effectTriggered;
    protected float lastHealth;
    protected float lastDamageTime;
    public int damageMultiplier;
    public float actualDamage;
    private float warmup,size;
    public static final float DAMAGE_TIME = 6 * 60f;


    public int classId() {
        return EntityRegister.getId(StarrySkyEntity.class);
    }

    @Override
    public void setType(UnitType type) {
        super.setType(type);
    }

    @Override
    public void update() {
        super.update();

        if (cooldown > 0) {
            CooldownPhase();
        }

        if (triggered && cooldown <= 0) {
            if (timer > duration) {
                AbsorptionPhase();
            } else if (timer > 0) {
                ConversionPhase();
            } else if (timer <= 0) {
                cooldown = cooldownTime;
                triggered = false;
                currentDamage = 0f;
                actualDamage = 0f;
                damageMultiplier = 1;
            }
        }
        if (timer > duration && timer < delay + duration) {
            if (Mathf.chance(0.05)) {
                Tmp.v1.rnd(Mathf.random(80 + hitSize)).scl(-Mathf.random(1, 2)).add(x, y);
                WHFx.chainLightningFadeReversed.at(x, y, 12f, team.color.cpy(), Tmp.v1.cpy());
            }
        }

        if (triggered &&timer>0 &&damageMultiplier > 1&& Time.time - lastDamageTime <= DAMAGE_TIME) {
            warmup = Mathf.lerpDelta(warmup, 1, 0.08f);
        } else  {
            warmup = Mathf.lerpDelta(warmup, 0, 0.1f);
        }

        if (timer > duration && timer < duration + delay&& Time.time - lastDamageTime <= DAMAGE_TIME) {
            size = Mathf.lerpDelta(size, 1, 0.08f);
        } else {
            size = Mathf.lerpDelta(size, 0, 0.08f);
        }
    }

    private void CooldownPhase() {
        if (health < lastHealth || currentDamage > 1000) {
            triggered = true;
            timer = duration + delay;
        } else if (Time.time - lastDamageTime > DAMAGE_TIME && !(timer > duration || timer > 0)) {
            cooldown = cooldownTime;
            currentDamage = 0f;
            actualDamage = 0f;
            damageMultiplier = 1;
            triggered = false;
            effectTriggered = false;
        }
        lastHealth = health;
        if (triggered) cooldown = Math.max(cooldown - Time.delta, 0);
    }


    private void AbsorptionPhase() {
        timer -= Time.delta;
        currentDamage += actualDamage;
    }

    private void ConversionPhase() {
        timer -= Time.delta;
        if (timer <= duration) {
            float weaponCount = Math.max(type.weapons.size, 1);
            float damageFactor = damageMultp * currentDamage / (weaponCount * maxHealth * 0.1f);
            damageMultiplier = (int) Mathf.clamp(damageFactor, 1, MaxDamageMultiplier);
            this.statusDamageMultiplier(damageMultiplier);
        }
    }

    public void rawDamage(float amount) {
        boolean hadShields = shield > 1.0E-4F;
        if (Float.isNaN(health)) health = 0.0F;
        if (hadShields) {
            shieldAlpha = 1.0F;
        }
        float shieldDamage = Math.min(Math.max(shield, 0), amount);
        shield -= shieldDamage;
        hitTime = 1.0F;
        amount -= shieldDamage;
        if (amount > 0 && type.killable) {
            if (triggered && timer > duration && timer < delay + duration) {
                actualDamage += amount;
            }
            if (triggered && timer > 0) {
                amount *= (1 - damageReduce);
            }
            health -= amount;
            lastDamageTime = Time.time;
            if (health <= 0 && !dead) {
                kill();
            }
            if (hadShields && shield <= 1.0E-4F) {
                Fx.unitShieldBreak.at(x, y, 0, type.shieldColor(this), this);
            }
        }
    }

    @Override
    public void draw() {
        super.draw();

            Tmp.c1.set(team.color).lerp(Color.white, Mathf.absin(4f, 0.3f));
            float progress = 0;
            armorRegion = Core.atlas.find(this.type.name + "-energyArmor");
            float width = armorRegion.width * Draw.scl * size,
                    height = armorRegion.height * Draw.scl * size;

            if (Vars.renderer.animateShields) {
                Draw.z(Layer.shields + 0.01f);
                Draw.color(this.team.color.cpy());
                Draw.rect(armorRegion, x, y, width, height, this.rotation - 90);
            } else {
                Draw.z(Layer.shields);
                Draw.alpha(0.09f);
                Draw.rect(armorRegion, x, y, width, height, this.rotation - 90);
                Draw.alpha(0.4f);
                Draw.rect(armorRegion, x, y, width, height, this.rotation - 90);
            }

            if (timer > duration && timer < delay + duration) {
                Draw.color(Tmp.c1);
                progress = (timer - duration) / delay;
                if (Vars.renderer.animateShields) {
                    Draw.z(Layer.shields);
                    Lines.stroke((3f + Mathf.absin(10f, 0.55f)) * warmup * Mathf.curve(1 - progress, 0, 0.075f));
                } else {
                    Draw.z(Layer.shields + 0.01f);
                    Lines.stroke((3f + Mathf.absin(10f, 0.55f)) * warmup * Mathf.curve(1 - progress, 0, 0.075f));
                }
            } else if (timer > 0 && timer < delay && damageMultiplier > 1) {
                progress = timer / duration;
                if (Vars.renderer.animateShields) {
                    Draw.z(Layer.shields);
                    Lines.stroke((3f + Mathf.absin(15f, 0.8f)) * Mathf.curve(progress, 0, 0.1f));
                } else {
                    Draw.z(Layer.shields + 0.01f);
                    Lines.stroke((3f + Mathf.absin(15f, 0.8f)) * Mathf.curve(progress, 0, 0.1f));
                }
                Draw.color(Pal.remove.cpy().lerp(Pal.meltdownHit, 0.3f));
                if (!effectTriggered) {
                    Tmp.v1.add(x, y);
                    float ex = Tmp.v1.x, ey = Tmp.v1.y,
                            offsetX = Angles.trnsx(rotation, 36.25f), offsetY = Angles.trnsy(rotation, 4f);
                    WHFx.crossSpinBlast.at(ex - offsetX, ey + offsetY, rotation, team.color, self());
                    effectTriggered = true;
                }
            }
            Drawn.circlePercent(x, y, hitSize * 1.5f, progress, 0);
            Draw.reset();
    }


    @Override
    public void display(Table table) {
        type.display(this, table);
        Tmp.c1.set(team.color.cpy()).lerp(Color.white, Mathf.absin(4f, 0.3f));
        table.row();
        table.table(bars -> {
            bars.defaults().growX().pad(5).height(20f);
            bars.add(new Bar("bar.damagemulti", Tmp.c1, () -> (damageMultiplier - 1) / 14f));
            bars.row();
            bars.add(new Bar("bar.delay", Pal.accent, () ->
                    Mathf.clamp((timer - duration) / delay, 0f, 1f)
            ));
            bars.row();
            bars.add(new Bar("bar.cooldown", Pal.remove, () -> ((damageMultiplier == 1 && timer <= duration || cooldown == 0 ?
                    1 : (cooldown / cooldownTime)))));
        }).growX().padBottom(5);
    }

    @Override
    public void read(Reads read) {

        cooldown = read.f();
        timer = read.f();
        currentDamage = read.f();
        triggered = read.bool();
        damageMultiplier = read.i();
        actualDamage = read.f();

        super.read(read);
    }

    @Override
    public void write(Writes write) {
        write.f(cooldown);
        write.f(timer);
        write.f(currentDamage);
        write.bool(triggered);
        write.i(damageMultiplier);
        write.f(actualDamage);

        super.write(write);
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);
        write.f(cooldown);
        write.f(timer);
        write.f(currentDamage);
        write.bool(triggered);
        write.i(damageMultiplier);
        write.f(actualDamage);
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);
        if (!isLocal()) {
            timer = read.f();
            currentDamage = read.f();
            triggered = read.bool();
            damageMultiplier = read.i();
            actualDamage = read.f();
        } else {
            read.f();
            read.f();
            read.bool();
            read.i();
            read.f();
        }
    }

}