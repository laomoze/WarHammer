package wh.entities.world.entities;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.content.*;
import wh.graphics.*;

import static arc.Core.atlas;
import static mindustry.Vars.tilesize;

public class EnergyUnitType extends SuperHeavyUnitType{
    public float outerEyeScl = 0.25f;
    public float innerEyeScl = 0.18f;
    /*
     * [0] -> Length
     * [1] -> Arrow Offset
     * [2] -> Width
     * [3] -> Rotate Speed
     * [4] -> Origin Offset
     * */
    public float[][] rotator = {
            {75f, 0, 8.5f, 1.35f, 0.1f},
            {55f, 0, 6.5f, -1.7f, 0.1f},
            {25, 0, 13, 0.75f, 0.3f},
            {100f, 33.5f, 11, 0.75f, 0.7f},
            {60f, -20, 6f, -0.5f, 1.25f}
    };

    public float bodySize = 24f;
    public float effectTriggerLen = 40f;
    public float reload = 240;
    public float teleportMinRange = 180f;
    public float teleportRange = 400f;

    public Effect teleport = Fx.none;
    public Effect teleportTrans = Fx.none;
    public Effect slopeEffect = WHFx.boolSelector;

    public EnergyUnitType(String name) {
        super(name);
    }

    @Override
    public void init() {
        super.init();
        if (teleport == Fx.none)
            teleport = new Effect(45, 600, e -> {
                if (!(e.data instanceof Vec2 v)) return;

                float angle = Angles.angle(e.x, e.y, v.x, v.y);
                float dst = Mathf.dst(e.x, e.y, v.x, v.y);
                Rand rand = new Rand(e.id);
                Tmp.v1.set(v).sub(e.x, e.y).nor().scl(tilesize * 3f);
                Lines.stroke(Mathf.curve(e.fout(), 0, 0.3f) * 1.75f);
                Draw.color(e.color, Color.white, e.fout() * 0.75f);
                for (int i = 1; i < dst / tilesize / 3f; i++) {
                    for (int j = 0; j < (int) e.rotation / 3; j++) {
                        Tmp.v4.trns(angle, rand.random(e.rotation / 4, e.rotation / 2), rand.range(e.rotation));
                        Tmp.v3.set(Tmp.v2.set(Tmp.v1)).scl(i).add(Tmp.v2.scl(rand.range(0.5f))).add(Tmp.v4).add(e.x, e.y);
                        Lines.lineAngle(Tmp.v3.x, Tmp.v3.y, angle - 180, e.fout(Interp.pow2Out) * 18 + 8);
                    }
                }
            });
        if (teleportTrans == Fx.none)
            teleportTrans = new Effect(45, 600, e -> {
                if (!(e.data instanceof Vec2 v)) return;

                float angle = Angles.angle(e.x, e.y, v.x, v.y);
                float dst = Mathf.dst(e.x, e.y, v.x, v.y);
                Rand rand = new Rand(e.id);
                Tmp.v1.set(v).sub(e.x, e.y).nor().scl(tilesize * 3f);
                Lines.stroke(Mathf.curve(e.fout(), 0, 0.3f) * 1.75f);
                Draw.color(e.color, Color.white, e.fout() * 0.75f);
                for (int i = 1; i < dst / tilesize / 3f; i++) {
                    for (int j = 0; j < (int) e.rotation / 3; j++) {
                        Tmp.v4.trns(angle, rand.random(e.rotation / 4, e.rotation / 2), rand.range(e.rotation));
                        Tmp.v3.set(Tmp.v2.set(Tmp.v1)).scl(i).add(Tmp.v2.scl(rand.range(0.5f))).add(Tmp.v4).add(e.x, e.y);
                        Lines.lineAngle(Tmp.v3.x, Tmp.v3.y, angle - 180, e.fout(Interp.pow2Out) * 18 + 8);
                    }
                }
            });
        if (trailLength < 0) trailLength = (int) bodySize * 4;
        if (slopeEffect == WHFx.boolSelector) slopeEffect = new Effect(30, b -> {
            if (!(b.data instanceof Integer)) return;
            int i = b.data();
            Draw.color(b.color);
            Angles.randLenVectors(b.id, (int) (b.rotation / 8f), b.rotation / 4f + b.rotation * 2f * b.fin(), (x, y) -> Fill.circle(b.x + x, b.y + y, b.fout() * b.rotation / 2.25f));
            Lines.stroke((i < 0 ? b.fin(Interp.pow2InInverse) : b.fout(Interp.pow2Out)) * 2f);
            Lines.circle(b.x, b.y, (i > 0 ? (b.fin(Interp.pow2InInverse) + 0.5f) : b.fout(Interp.pow2Out)) * b.rotation);
        }).layer(Layer.bullet);

        engineSize = bodySize / 4;
        engineSize *= -1;
    }

    @Override
    public void load() {
        super.load();
        shadowRegion = uiIcon = fullIcon = Core.atlas.find("wh-jump-gate-pointer");
    }

    @Override
    public void draw(Unit unit) {
        super.draw(unit);
    }

    @Override
    public void drawBody(Unit unit) {
        float sizeF = 1 + Mathf.absin(4f, 0.1f);
        Draw.z(Layer.flyingUnitLow + 5f);
        Draw.color(Color.black);
        Fill.circle(unit.x, unit.y, bodySize * sizeF * 0.71f * unit.healthf());

        Drawf.light(unit.x, unit.y, unit.hitSize * 4.05f, unit.team.color, 0.68f);
        Draw.z(Layer.effect + 0.001f);
        Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) + Mathf.clamp(unit.hitTime) / 5f * 3f);
        Draw.alpha(0.65f);
        Fill.circle(unit.x, unit.y, bodySize * sizeF * 1.1f);
        Draw.alpha(1f);
        Fill.circle(unit.x, unit.y, bodySize * sizeF);

        for (float[] j : rotator) {
            for (int i : Mathf.signs) {
                float ang = Time.time * j[3] + 90 + 90 * i + Mathf.randomSeed(unit.id, 360);
                Tmp.v1.trns(ang, hitSize * j[4]).add(unit);
                Drawn.arrow(Tmp.v1.x, Tmp.v1.y, j[2], j[0], j[1], ang);
            }
        }

        Draw.color(Tmp.c1.set(unit.team.color).lerp(Color.white, 0.65f));
        Fill.circle(unit.x, unit.y, bodySize * sizeF * 0.75f * unit.healthf());
        Draw.color(Color.black);
        Fill.circle(unit.x, unit.y, bodySize * sizeF * 0.7f * unit.healthf());

        Draw.color(unit.team.color);
        Tmp.v1.set(unit.aimX, unit.aimY).sub(unit).nor().scl(bodySize * 0.15f);
        Fill.circle(Tmp.v1.x + unit.x, Tmp.v1.y + unit.y, bodySize * sizeF * outerEyeScl);
        Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) + 0.45f);
        Tmp.v1.setLength(bodySize * sizeF * (outerEyeScl - innerEyeScl));
        Fill.circle(Tmp.v1.x + unit.x, Tmp.v1.y + unit.y, bodySize * sizeF * innerEyeScl);
        Draw.reset();
    }

    @Override
    public void update(Unit unit) {
        super.update(unit);
        if (Mathf.chanceDelta(0.1)) for (int i : Mathf.signs)
            slopeEffect.at(unit.x + Mathf.range(bodySize), unit.y + Mathf.range(bodySize), bodySize, unit.team.color, i);
    }

    @Override
    public void drawCell(Unit unit) {}

    public void drawControl(Unit unit) {
        Draw.z(Layer.effect + 0.001f);
        Draw.color(unit.team.color, Color.white, Mathf.absin(4f, 0.3f) + Mathf.clamp(unit.hitTime) / 5f);
        for (int i = 0; i < 4; i++) {
            float rotation = Time.time * 1.5f + i * 90;
            Tmp.v1.trns(rotation, bodySize * 1.5f).add(unit);
            Draw.rect(atlas.find("wh-jump-gate-arrow"), Tmp.v1.x, Tmp.v1.y, rotation + 90);
        }
        Draw.reset();
    }

    @Override
    public void drawItems(Unit unit) {
        super.drawItems(unit);
    }

    @Override
    public void drawLight(Unit unit) {
        Drawf.light(unit.x, unit.y, bodySize * 3f, unit.team.color, lightOpacity);
    }

    @Override
    public void drawMech(Mechc mech) {}

    @Override
    public void drawOutline(Unit unit) {}

    @Override
    public void drawEngines(Unit unit) {}

    @Override
    public void drawTrail(Unit unit) {}

    @Override
    public <T extends Unit & Payloadc> void drawPayload(T unit) {}

    @Override
    public void drawShadow(Unit unit) {}

    @Override
    public void drawShield(Unit unit) {
        float alpha = unit.shieldAlpha();
        float radius = unit.hitSize() * 1.3f;
        Fill.light(unit.x, unit.y, Lines.circleVertices(radius), radius, Tmp.c1.set(Pal.shield), Tmp.c2.set(unit.team.color).a(0.7f).lerp(Color.white, Mathf.clamp(unit.hitTime() / 2f)).a(Pal.shield.a * alpha));
    }

    @Override
    public void drawSoftShadow(Unit unit) {}

    @Override
    public void drawWeapons(Unit unit) {}
}
