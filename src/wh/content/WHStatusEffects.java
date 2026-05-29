package wh.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.units.StatusEntry;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;
import wh.graphics.Drawn;
import wh.graphics.WHPal;

import static arc.graphics.g2d.Draw.alpha;
import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Angles.randLenVectors;
import static wh.content.WHFx.rand;
public final class WHStatusEffects{
    public static StatusEffect
    powerEnhance1, powerEnhance2,
    powerReduce1, powerReduce2,
            rust, radiation, acidRain,
            assault, bless, energyAmplification, weaponCharge, protection,
    tear, armorFracture, rock,
            scare, mark,
    distort, forcesOfChaos, melta, palsy, plasma, plasmaFireBurn;
    private WHStatusEffects(){
    }
    public static void load(){
        powerEnhance1 = new StatusEffect("power-enhance"){{
            color = Color.lightGray.cpy().lerp(Pal.accent.cpy(), 0.2f);
            speedMultiplier = 1.3f;
            init(() -> opposite(WHStatusEffects.powerReduce1, WHStatusEffects.powerReduce2, StatusEffects.slow));
        }};
        powerEnhance2 = new StatusEffect("power-enhance-2"){{
            color = Color.lightGray.cpy().lerp(Pal.accent.cpy(), 0.4f);
            speedMultiplier = 1.5f;
            init(() -> opposite(WHStatusEffects.powerReduce1, WHStatusEffects.powerReduce2, StatusEffects.slow));
        }};
        powerReduce1 = new StatusEffect("power-damage"){{
            color = Color.lightGray.cpy().lerp(Team.crux.color.cpy(), 0.2f);
            speedMultiplier = 0.8f;
        }};
        powerReduce2 = new StatusEffect("power-damage-2"){{
            color = Color.lightGray.cpy().lerp(Team.crux.color.cpy(), 0.4f);
            speedMultiplier = 0.5f;
        }};

        assault = new StatusEffect("assault"){{
            color = Team.crux.color.cpy();
            healthMultiplier = 1.3f;
            speedMultiplier = 1.6f;
            reloadMultiplier = 1.3f;
            effectChance = 0.05f;
            effect = WHFx.square(30, Team.crux.color.cpy(), 4, 25, 5);
        }};
        bless = new StatusEffect("bless"){{
            color = Color.valueOf("F4EEADFF");
            healthMultiplier = 1.3f;
            speedMultiplier = 0.9f;
            damage = -900 / 60f;
            buildSpeedMultiplier = 1.1f;
            effectChance = 0.05f;
            parentizeEffect = true;
            applyEffect = effect = new Effect(120, e -> {
                Draw.color(Color.valueOf("F4EEADFF"));
                if(!(e.data instanceof Unit u)) return;
                rand.setSeed(e.id);
                Lines.stroke(2 * e.fout());
                Lines.circle(e.x, e.y, u.hitSize / 2 * rand.random(0.3f, 1f) * e.fin() + 10f);
            });
            init(() -> {
                opposite(StatusEffects.boss, WHStatusEffects.forcesOfChaos, WHStatusEffects.distort);
            });
        }};
        distort = new StatusEffect("distort"){{
            color = Pal.sapBullet.cpy();
            healthMultiplier = 2.5f;
            speedMultiplier = 0.8f;
            reloadMultiplier = 1.2f;
            effectChance = 0.1f;
            parentizeEffect = true;
            effect = new Effect(60, e -> {
                Draw.color(Pal.sapBullet);
                if(!(e.data instanceof Unit u)) return;
                rand.setSeed(e.id);
                Lines.stroke(2 * e.fout());
                Angles.randLenVectors(e.id, 2, u.hitSize / 2 * rand.random(0.3f, 1f) * e.fin() + 10f, (x, y) -> {
                    Lines.circle(e.x + x, e.y + y, 10);
                });
            });
        }};
        energyAmplification = new StatusEffect("energy-amplification"){{
            color = Pal.techBlue.cpy();
            reloadMultiplier = 1.2f;
            healthMultiplier = 1.2f;
            damageMultiplier = 1.2f;
            speedMultiplier = 1.1f;
            effectChance = 0.1f;
            parentizeEffect = true;
            effect = new Effect(90, e -> {
                if(!(e.data instanceof Unit u)) return;
                rand.setSeed(e.id);
                randLenVectors(e.id, 1, u.hitSize / 1.5f * rand.random(0.3f, 1f) * e.fin() + 10f, (x, y) -> {
                    color(Pal.techBlue.cpy());
                    Lines.stroke(2 * e.fout(Interp.pow2Out));
                    Lines.poly(e.x + x, e.y + y, 6, Mathf.clamp(u.hitSize / 10f, 5, 12f) * e.fout(Interp.pow2Out), 60);
                    Draw.color();
                });
                randLenVectors(e.id, 2, u.hitSize * rand.random(0.2f, 1.1f) + 10f, (x, y) -> {
                    color(Pal.techBlue.cpy());
                    Lines.stroke(2 * e.fout(Interp.pow2Out));
                    Tmp.v1.trns(90, Mathf.randomSeed(e.id, 20, 40) * e.fin()).add(e.x, e.y);
                    lineAngle(Tmp.v1.x + x, Tmp.v1.y + y, 90, e.finpow() * 10f * rand.random(0.5f, 1f));
                    Draw.color();
                });
            });
        }};

        weaponCharge = new StatusEffect("weapon-charge") {{
            color = Pal.techBlue.cpy();
            reloadMultiplier = 1.2f;
            damageMultiplier = 1.5f;
            speedMultiplier = 0.75f;
            effectChance = 0.1f;
            parentizeEffect = true;
            effect = new Effect(35f, e -> {
                color(e.color);
                rand.setSeed(e.id);
                randLenVectors(e.id, 2, 1f + e.fout() * 15 * rand.random(0.5f, 1), (x, y) -> {
                    Fill.square(e.x + x, e.y + y, e.fout() * 3 * Mathf.curve(e.fin(), 0, 0.25f) + 0.5f);
                });
            });
        }};

        protection = new StatusEffect("protection"){
            {
                color = Pal.accent.cpy().lerp(Pal.slagOrange, 0.3f);
                healthMultiplier = 2f;
                damage = -1000 / 60f;
            }

            @Override
            public void update(Unit unit, StatusEntry entry){
                super.update(unit, entry);
                unit.shield += damage * Time.delta;
            }
        };

        forcesOfChaos = new StatusEffect("forces-of-chaos"){{
            color = Team.crux.color.cpy();
            reloadMultiplier = 1.5f;
            healthMultiplier = 2f;
            damageMultiplier = 2f;
            speedMultiplier = 2f;
            effectChance = 0.1f;
            parentizeEffect = true;
            effect = new Effect(90, e -> {
                if(!(e.data instanceof Unit u)) return;
                rand.setSeed(e.id);
                randLenVectors(e.id, 1, u.hitSize / 2 * rand.random(0.3f, 1f) * e.fin() + 10f, (x, y) -> {
                    color(Team.crux.color.cpy());
                    Lines.stroke(2 * e.fout(Interp.pow2Out));
                    Lines.poly(e.x + x, e.y + y, 6, Mathf.clamp(u.hitSize / 10f, 5, 12f) * e.fout(Interp.pow2Out), 60);
                    Draw.color();
                });
            });
        }};
        melta = new StatusEffect("melta"){{
            color = WHPal.ShootOrangeLight;
            speedMultiplier = 0.8f;
            healthMultiplier = 0.8f;
            damage = 6;
            effect = Fx.melting;
            init(() -> {
                opposite(StatusEffects.wet, StatusEffects.freezing, WHStatusEffects.plasma);
                affinity(StatusEffects.tarred, (unit, result, time) -> {
                    unit.damagePierce(6);
                    Fx.burning.at(unit.x + Mathf.range(unit.bounds() / 2f), unit.y + Mathf.range(unit.bounds() / 2f));
                    result.set(StatusEffects.melting, Math.min(time + result.time, 200f));
                });
            });
        }};
        palsy = new StatusEffect("palsy"){{
            color = WHPal.WHYellow2;
            speedMultiplier = 0.5f;
            healthMultiplier = 0.9f;
            reloadMultiplier = 0.65f;
            effectChance = 0.1f;
            effect = WHFx.square(30, Pal.powerLight.cpy(), 3, 35, Mathf.random(5, 8));
            init(() -> {
                affinity(tear, (unit, status, time) -> {
                    if(Mathf.chance(0.155))
                        unit.damage(20);
                    Drawn.randFadeLightningEffect(unit.x + Mathf.range(unit.hitSize), unit.y + Mathf.range(unit.hitSize),
                    unit.hitSize * Mathf.random(1.4f, 2.2f) + 28f, 8f,
                    Tmp.c1.set(Pal.powerLight).mul(Mathf.random(0.16f) + 1f), false);
                });
            });
        }};
        tear = new StatusEffect("tear"){
            {
                color = WHItems.molybdenumAlloy.color.cpy();
                damage = 300 / 60f;
            }
            @Override
            public void update(Unit unit, StatusEntry entry){
                super.update(unit, entry);
                if(unit.shield > 0) unit.shield -= Mathf.clamp(unit.shield / unit.maxHealth, 1, 10) * 0.5f / 60f * Time.delta;
                if(Mathf.chanceDelta(0.05f) && unit.shield > 0){
                    Tmp.v1.rnd(Mathf.range(unit.type.hitSize / 2f));
                    WHFx.shuttle(40, color, color.cpy().lerp(Color.gray, 0.1f), true, 0, 0).
                    at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, Mathf.chanceDelta(0.5f) ? 45 : 135f, color, Mathf.range(unit.type.hitSize / 4f, unit.type.hitSize / 2f));
                }
            }
        };
        rock = new StatusEffect("rock"){
            {
                color = WHItems.molybdenumAlloy.color.cpy().lerp(Color.white, 0.3f);
                speedMultiplier = 0.8f;
                reloadMultiplier = 0.95f;
                dragMultiplier = 1.2f;
                buildSpeedMultiplier = 0.9f;
                effectChance = 0.05f;
                effect = WHFx.hitSpark(120, color, 5, 30, 1.5f, 10);
            }
        };
        armorFracture = new StatusEffect("armor-fracture"){
            {
                color = WHItems.molybdenumAlloy.color.cpy().lerp(Color.gray, 0.5f);
                intervalDamageTime = 20f;
                intervalDamage = 100;
                intervalDamagePierce = true;
            }

            final float amount = 3;

            @Override
            public void applied(Unit unit, float time, boolean extend){
                super.applied(unit, time, extend);
                if(extend) return;
                float baseArmor = unit.type.armor;
                if(baseArmor <= 0f) return;
                float minArmor = baseArmor / 2f;
                float currentArmor = unit.armorOverride >= 0f ? unit.armorOverride : baseArmor;
                float nextArmor = Math.max(minArmor, currentArmor - amount);
                unit.statusArmor(nextArmor);
            }
        };
        scare = new StatusEffect("scare"){{
            color = Pal.sap;
            speedMultiplier = 0.8f;
            reloadMultiplier = 0.7f;
            buildSpeedMultiplier = 0.3f;
            effect = Fx.sapped;
            effectChance = 0.1f;
            init(() -> opposite(bless));
        }};

        mark = new StatusEffect("mark") {{
            color = Pal.remove;
            effectChance = 0f;
        }};

        plasmaFireBurn = new StatusEffect("plasma-fire"){{
            color = WHPal.SkyBlue;
            damage = 300 / 60f;
            parentizeEffect = true;
            effect = new Effect(25.0F, (e) -> {
                if(!(e.data instanceof Unit u)) return;
                color(WHPal.SkyBlue, Color.gray, e.fin() * 0.75F);
                randLenVectors(e.id, 2, 2.0F + e.fin() * (7.0F + u.hitSize() * 0.2f), (x, y) -> {
                    Fill.poly(e.x + x, e.y + y, 6, 0.2F + e.fout() * (4f + u.hitSize() * 0.08f), 45);
                });
            });
            init(() -> {
                affinity(plasma, (unit, result, time) -> result.set(plasmaFireBurn, result.time + time));
            });
        }};
        plasma = new StatusEffect("plasma"){{
            color = WHPal.SkyBlue;
            damage = 500 / 60f;
            speedMultiplier = 0.8f;
            effect = new Effect(40, e -> {
                if(!(e.data instanceof Unit u)) return;
                color(WHPal.SkyBlueF, Pal.techBlue, e.fin() * 0.75f);
                rand.setSeed(e.id);
                Lines.stroke(Mathf.clamp(u.hitSize / 10, 2, 3.5f) * e.fout());
                randLenVectors(e.id, 2, 2f + e.fin() * rand.random(0.4f, 1f) * u.hitSize * 0.8f, (x, y) -> {
                    Lines.square(e.x + x, e.y + y, 0.2f + e.fout() * 1.5f, 45);
                });
            });
            init(() -> {
                affinity(palsy, (unit, status, time) -> {
                    unit.damagePierce(12f);
                    if(Mathf.chance(0.155))
                        WHFx.crossBlast(WHPal.SkyBlueF, Mathf.clamp(unit.hitSize / 5f, 8, 20), 0);
                });
            });
        }};

        rust = new StatusEffect("rust"){
            {
                color = WHLiquids.swageWater.color.cpy();
                damage = 90 / 60f;
                speedMultiplier = 0.9f;
                dragMultiplier = 0.95f;
                effectChance = 0.09f;
                transitionDamage = 14;
                effect = new Effect(80f, e -> {
                    color(WHLiquids.swageWater.color.cpy());
                    alpha(Mathf.clamp(e.fin() * 2f));
                    Fill.circle(e.x, e.y, e.fout());
                });
                init(() -> {
                    affinity(StatusEffects.shocked, (unit, result, time) -> {
                        unit.damage(transitionDamage);
                    });
                    opposite(StatusEffects.burning, StatusEffects.melting, palsy);
                });
            }
        };
        radiation = new StatusEffect("radiation"){
            {
                color = WHLiquids.swageWater.color.cpy().lerp(Items.plastanium.color, 0.35f);
                damage = 90 / 60f;
                speedMultiplier = 0.9f;
                dragMultiplier = 0.95f;
                effectChance = 0.09f;
                transitionDamage = 1000 / 60f;
                effect = new Effect(90, e -> {
                    if(!(e.data instanceof Unit u)) return;
                    rand.setSeed(e.id);
                    color(color, Color.white, e.fout() * 0.3F);
                    stroke(e.fout() * 1.5f);
                    randLenVectors(e.id, 5, e.finpow() * u.hitSize / 2 + rand.random(0.4f, 0.8f) * u.hitSize * 0.5f, e.rotation, 360.0F, (x, y) -> {
                        float ang = Mathf.angle(x, y);
                        lineAngle(e.x + x, e.y + y, ang, e.fout() * 10 * 0.85F + 10 * 0.15F);
                    });
                });
                init(() -> {
                    affinity(armorFracture, (unit, result, time) -> {
                        unit.damage(transitionDamage);
                    });
                    affinity(tear, (unit, result, time) -> {
                        unit.damage(transitionDamage);
                    });
                    opposite(forcesOfChaos, bless, palsy);
                });
            }
        };

        acidRain = new StatusEffect("acid-rain") {
            {
                color = WHLiquids.swageWater.color.cpy().lerp(Pal.coalBlack, 0.2f);
                speedMultiplier = 0.7f;
                dragMultiplier = 1.25f;
                effectChance = 0.09f;
                effect = new Effect(90, e -> {
                    if (!(e.data instanceof Unit u)) return;
                    rand.setSeed(e.id);
                    color(color, Color.white, e.fout() * 0.3F);
                    stroke(e.fout() * 1.5f);
                    randLenVectors(e.id, 5, e.finpow() * u.hitSize / 2 + rand.random(0.4f, 0.8f) * u.hitSize * 0.5f, e.rotation, 360.0F, (x, y) -> {
                        float ang = Mathf.angle(x, y);
                        lineAngle(e.x + x, e.y + y, ang, e.fout() * 10 * 0.85F + 10 * 0.15F);
                    });
                });
            }
        };
    }
}
