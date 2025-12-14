package wh.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import wh.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.math.Angles.randLenVectors;
import static wh.content.WHFx.rand;

public final class WHStatusEffects{
    public static StatusEffect
    plasmaFireBurn, powerEnhance1, powerEnhance2, powerReduce1, powerReduce2,
    assault, bless, distort, rust,
    energyAmplification, forcesOfChaos, melta, palsy, plasma, tear;

    private WHStatusEffects(){
    }

    public static void load(){
        powerEnhance1 = new StatusEffect("power-enhance"){{
            color = Color.lightGray.cpy();
            speedMultiplier = 1.3f;
            init(() -> opposite(WHStatusEffects.powerReduce1, WHStatusEffects.powerReduce2));
        }};
        powerEnhance2 = new StatusEffect("power-enhance-2"){{
            color = Color.lightGray.cpy();
            speedMultiplier = 1.5f;
            init(() -> opposite(WHStatusEffects.powerReduce1, WHStatusEffects.powerReduce2));
        }};
        powerReduce1 = new StatusEffect("power-damage"){{
            color = Color.lightGray.cpy();
            speedMultiplier = 0.8f;
        }};
        powerReduce2 = new StatusEffect("power-damage-2"){{
            color = Color.lightGray.cpy();
            speedMultiplier = 0.5f;
        }};



        assault = new StatusEffect("assault"){{
            color = Team.crux.color.cpy();
            healthMultiplier = 1.3f;
            speedMultiplier = 1.6f;
            reloadMultiplier = 1.3f;
            effectChance = 0.1f;
            effect = WHFx.square(Team.crux.color.cpy(), 30, 4, 25, 5);
        }};
        bless = new StatusEffect("bless"){{
            color = Color.valueOf("F4EEADFF");
            healthMultiplier = 1.5f;
            speedMultiplier = 0.9f;
            damage = 900 / 60f;
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
            reloadMultiplier = 1.5f;
            healthMultiplier = 1.4f;
            dragMultiplier = 1.3f;
            damageMultiplier = 1.2f;
            speedMultiplier = 1.1f;
            effectChance = 0.1f;
            parentizeEffect = true;
            effect = new Effect(90, e -> {
                if(!(e.data instanceof Unit u)) return;
                rand.setSeed(e.id);
                randLenVectors(e.id, 1, u.hitSize / 2 * rand.random(0.3f, 1f) * e.fin() + 10f, (x, y) -> {
                    color(Pal.techBlue.cpy());
                    Lines.stroke(2 * e.fout(Interp.pow2Out));
                    Lines.poly(e.x + x, e.y + y, 6, Mathf.clamp(u.hitSize / 10f, 5, 12f) * e.fout(Interp.pow2Out), 60);
                    Draw.color();
                });
            });
        }};
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
            effect = WHFx.square(Pal.powerLight.cpy(), 30, 3, 35, Mathf.random(5, 8));
            init(() -> {
                affinity(StatusEffects.electrified, (unit, status, time) -> {
                    if(Mathf.chance(0.155))
                        unit.damage(20);
                    Drawn.randFadeLightningEffect(unit.x + Mathf.range(unit.hitSize), unit.y + Mathf.range(unit.hitSize),
                    unit.hitSize * Mathf.random(1.4f, 2.2f) + 28f, 8f,
                    Tmp.c1.set(Pal.powerLight).mul(Mathf.random(0.16f) + 1f), false);
                });
            });
        }};

        tear = new StatusEffect("tear"){{
            color = WHItems.molybdenumAlloy.color.cpy();
            damage = 300 / 60f;
            speedMultiplier = 0.9f;
            dragMultiplier = 1.1f;
            effect =
            WHFx.shuttle(color, color.cpy().lerp(Color.gray, 0.1f), 40, true, Mathf.random(20, 40), Mathf.chance(0.5f) ? 45f : 135f);
        }};

        plasmaFireBurn = new StatusEffect("plasma-fire"){{
            color = WHPal.SkyBlue;
            damage = 200 / 60f;
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
    }
}
