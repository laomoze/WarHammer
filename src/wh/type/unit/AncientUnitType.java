package wh.type.unit;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.Seq;
import arc.util.*;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import wh.content.*;
import wh.graphics.*;
import wh.world.meta.*;

import static arc.Core.*;
import static mindustry.core.Version.type;

@SuppressWarnings("unused")
public class AncientUnitType extends UnitType {
    public float damageMultiplier = 1f;

    public AncientUnitType(String name) {
        super(name);

        outlineColor = Pal.darkOutline;
        healColor = WHPal.ancientLightMid;
        lightColor = WHPal.ancientLightMid;

        ammoType = new ItemAmmoType(WHItems.ceramite);
    }

    @Override
    public void setStats() {
        super.setStats();
        if (damageMultiplier < 1f) {
            stats.add(WHStat.damageReduction, bundle.format("bar.wh-damage-reduction", Strings.autoFixed((1f - damageMultiplier) * 100, 2)));
        }
    }

    public void addEngine(float x, float y, float relativeRot, float rad, boolean flipAdd) {
        if (flipAdd) {
            for (int i : Mathf.signs) {
                engines.add(new AncientEngine(x * i, y, rad, -90 + relativeRot * i, 0));
                engines.add(new AncientEngine(x * i, y, rad * 1.85f, -90 + relativeRot * i, Mathf.random(2f)).a(0.3f));
            }
        } else {
            engines.add(new AncientEngine(x, y, rad, -90 + relativeRot, 0));
            engines.add(new AncientEngine(x, y, rad * 1.85f, -90 + relativeRot, Mathf.random(2f)).a(0.3f));
        }
    }

    public static class AncientEngine extends UnitEngine {
        //public Color engineColor;
        public float forceZ = -1;
        public float phaseOffset = Mathf.random(5);
        public float alphaBase = 0.8f;
        public float scl = 0.825f;
        public float sizeSclPlus = 0.4f;
        public float sizeSclMin = 0.95f;
        public float alphaSclMin = 0.88f;

        public AncientEngine(float x, float y, float radius, float rotation) {
            super(x, y, radius, rotation);
        }

        public AncientEngine(float x, float y, float radius, float rotation, float phaseOffset) {
            this(x, y, radius, rotation);
            this.phaseOffset = phaseOffset;
        }

        public AncientEngine(float x, float y, float radius, float rotation, float alphaBase, float sizeSclPlus, float sizeSclMin) {
            this(x, y, radius, rotation);
            this.alphaBase = alphaBase;
            this.sizeSclPlus = sizeSclPlus;
            this.sizeSclMin = sizeSclMin;
        }

        public AncientEngine a(float f) {
            this.alphaBase = f;
            return this;
        }

        @Override
        public void draw(Unit unit) {
            UnitType type = unit.type;
            if (unit.vel.len2() > 0.001f) {
                float z = Draw.z();

                if (forceZ > 0) Draw.z(forceZ);

                float rot = unit.rotation - 90.0F;
                Color c = type.engineColor == null ? unit.team.color : type.engineColor;
                Tmp.v1.set(x, y).rotate(rot).add(unit.x, unit.y);
                float ex = Tmp.v1.x;
                float ey = Tmp.v1.y;
                float rad = Mathf.curve(unit.vel.len2(), 0.001f, type.speed * type.speed) * radius * (sizeSclMin + Mathf.absin(Time.time + phaseOffset, scl, sizeSclPlus));
                float a = alphaBase * (alphaSclMin + Mathf.absin(Time.time * 1.3f - phaseOffset, scl, 0.13f));

                Draw.blend(Blending.additive);
                Draw.alpha(a);
                Tmp.c2.set(c).a(a);
                Fill.light(ex, ey, Lines.circleVertices(rad), rad, Tmp.c2, Color.clear);
                Tmp.c1.set(Tmp.c2).lerp(Color.white, Mathf.absin(Time.time * 1.1f + phaseOffset, scl * 1.12512f, 0.14f));
                Draw.color(Tmp.c1);
                Draw.alpha(a * 0.5f);
                Fill.light(ex, ey, Lines.circleVertices(rad), rad * 1.125f, Tmp.c1, Color.clear);

                Drawf.light(ex, ey, rad * 1.9f, Tmp.c1, a);

                Draw.reset();
                Draw.blend();

                Draw.z(z);
            }
        }
    }


    public boolean immuniseAll = true;
    public void init(){
        super.init();

        if(immuniseAll){
            immunise(this);
        }}
    public static Seq<StatusEffect> statuses;

    public static void immunise(UnitType type){
        if(statuses == null){
            statuses = Vars.content.statusEffects().copy();
            statuses.retainAll(s -> s.disarm || s.damage > 0 || s.healthMultiplier * s.reloadMultiplier * s.buildSpeedMultiplier * s.speedMultiplier < 1);

            statuses.remove(StatusEffects.overclock);
            statuses.remove(StatusEffects.overdrive);
            statuses.remove(WHStatusEffects.assault);
            statuses.remove(WHStatusEffects.bless);
            statuses.remove(WHStatusEffects.plasma);
            statuses.add(StatusEffects.wet);
            statuses.add(StatusEffects.unmoving);
        }

        type.immunities.addAll(statuses);
    }
}
