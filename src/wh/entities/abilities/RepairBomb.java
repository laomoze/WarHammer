package wh.entities.abilities;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.graphics.*;

import static mindustry.Vars.*;
import static wh.core.WarHammerMod.name;

public class RepairBomb extends Ability{
    public BulletType deathBullet;
    public Object data;
    public float x, y;
    public boolean withRotate = false;
    public float reload, range, amount;
    public float scanTime = 90f;
    public float healPercent = 2f;
    public Color color = Pal.heal;

    public float layer = Layer.bullet - 0.001f, blinkScl = 20f, blinkSize = 0.1f;
    public float effectRadius = 5f, rotateSpeed = 0.5f;
    public float triLength = 15f, triWidth = 4f;

    public Effect healEffect = new Effect(scanTime, 2000, e -> {
        Rand rand = WHFx.rand;
        rand.setSeed(e.id);
        Draw.color(e.color);
        Interp scanInterp = Interp.pow5Out;

        float f = Interp.pow4Out.apply(Mathf.curve(e.fin(), 0, 0.3f));
        float stroke = Mathf.clamp(e.rotation / 80, 3, 8);

        Lines.stroke(2 * e.fout());
        Lines.circle(e.x, e.y, e.rotation * f / 8);

        Lines.stroke(stroke * e.fout() + 1 * e.fout(Interp.pow5In));
        Lines.circle(e.x, e.y, e.rotation * f);


        Lines.stroke(stroke * Mathf.curve(e.fin(), 0, 0.1f) * Mathf.curve(e.fout(), 0.05f, 0.15f));
        float angle = 360 * e.fin(scanInterp);
        Lines.lineAngle(e.x, e.y, angle, e.rotation * f - Lines.getStroke() / 2);
        Lines.stroke(stroke * Mathf.curve(e.fin(), 0, 0.1f) * e.fout(0.05f));

        Draw.z(Layer.bullet - 1);
        Drawn.fillCirclePercentFade(e.x, e.y, e.x, e.y, e.rotation * f, e.fin(scanInterp), 0, Mathf.curve(e.fout(), 0.2f, 0.25f) / 1.5f, 0.6f + 0.35f * Interp.pow2InInverse.apply(Mathf.curve(e.fin(), 0, 0.8f)), 1f);
        Draw.z(Layer.effect);

        Angles.randLenVectors(e.id, (int)(e.rotation / 40), e.rotation * 0.85f * f, angle, 0, (x, y) -> {
            Lines.lineAngle(e.x + x, e.y + y, angle, e.rotation * rand.random(0.05f, 0.15f) * e.fout(0.15f));
        });

        Draw.color(e.color);
        Lines.stroke(stroke * 1.25f * e.fout(0.2f));
        Fill.circle(e.x, e.y, Lines.getStroke());
        Draw.color(Color.white, e.color, e.fin());
        Fill.circle(e.x, e.y, Lines.getStroke() / 2);

        Drawf.light(e.x, e.y, e.rotation * f * 1.35f * e.fout(0.15f), e.color, 0.6f);
    });

    protected float timer = 0, damageTimer = 0;
    protected boolean healing = false, wasHeal = false;

    protected UnitType statUnit;

    public RepairBomb(float amount, float reload, float range){
        this.amount = amount;
        this.reload = reload;
        this.range = range;
    }

    @Override
    public void init(UnitType type){
        statUnit = type;
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);
        Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
        float rx = Tmp.v1.x, ry = Tmp.v1.y;
        float fin = Mathf.clamp(timer / reload, 0, 1);
        float orbRadius = effectRadius * (1f + Mathf.absin(blinkScl, blinkSize) * fin);
        float scale = 0.6f;
        Draw.z(layer);
        Fill.circle(rx, ry, orbRadius);
        Draw.color(color);
        Fill.circle(rx, ry, orbRadius);
        Draw.color(color.cpy().lerp(Color.white, 0.5f));
        Fill.circle(rx, ry, orbRadius * scale);

        Draw.color(color);
        for(int i : Mathf.signs){
            Drawf.tri(rx, ry, triWidth * fin, triLength * fin, Time.time + 90 * i);
        }
        Draw.color(color.cpy().lerp(Color.white, 0.5f));
        for(int i : Mathf.signs){
            Drawf.tri(rx, ry, triWidth * fin * scale, triLength * fin * scale, Time.time + 90 * i);
        }
        Draw.reset();
    }


    @Override
    public void update(Unit unit){
        super.update(unit);
        healing = false;
        Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
        float rx = Tmp.v1.x, ry = Tmp.v1.y;
        timer += Time.delta;
        damageTimer += Time.delta;
        if(damageTimer >= 10){
            damageTimer = 0;
            indexer.eachBlock(unit, range, Building::damaged, other -> healing = true);
        }
        timer += Time.delta;
        if(timer >= reload && healing){
            timer = 0;
            indexer.eachBlock(unit, range, Building::damaged, other -> {
                other.heal(other.maxHealth() * healPercent / 100f + amount);
                other.recentlyHealed();
                healEffect.at(rx, ry, range, color, unit);
                Fx.healBlockFull.at(other.x, other.y, other.block.size, color, other.block);
            });
        }
        wasHeal = healing;
    }

    @Override
    public void death(Unit unit){
        if(deathBullet == null) return;
        deathBullet.create(unit, unit.team, unit.x, unit.y, withRotate ? unit.rotation() : 0, -1, 1, 1, data);
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(Core.bundle.format("bullet.range", Strings.autoFixed(range / tilesize, 2)));
        t.row();
        t.add(abilityStat("repairspeed", Strings.autoFixed(amount * 60f / reload, 2)));
        t.row();
        if(deathBullet != null){
            t.add(Core.bundle.get(name("deathBullet"))).row();
            StatValues.ammo(ObjectMap.of(statUnit, deathBullet)).display(t);
        }
    }

    @Override
    public String localized(){
        return Core.bundle.get("ability." + name("RepairBomb"));
    }
}
