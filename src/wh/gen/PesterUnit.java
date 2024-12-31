package wh.gen;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.entities.*;
import wh.graphics.*;
import wh.math.*;
import wh.type.unit.*;
import wh.util.*;

import static mindustry.Vars.*;

public class PesterUnit extends UnitEntity implements Pesterc {
    public static final ObjectIntMap<Healthc> checked = new ObjectIntMap<>();

    public static Building tmpBuilding = null;

    public boolean isBoss = false;

    public Teamc bossTarget;
    public Teamc lastTarget;
    public transient Vec2 lastTargetPos = new Vec2();

    public float bossWeaponReload;
    public float bossWeaponWarmup;
    public float bossWeaponProgress;
    public float bossTargetShiftLerp = 0f;
    public float bossTargetSearchReload = 0f;

    public transient float bossWeaponReloadLast = 0f;
    public transient float bossWeaponReloadTarget = 0f;

    public float hatredCheckReload = 0f;

    public float salvoReload = 0f;
    public transient float salvoReloadLast = 0f;
    public transient float salvoReloadTarget = 0f;

    public ObjectFloatMap<Healthc> hatred = new ObjectFloatMap<>();
    public Seq<Healthc> nextTargets = new Seq<>();

    protected Trail[] trails = {};

    protected PesterUnit() {}

    @Override
    public int classId() {
        return EntityRegister.getId(PesterUnit.class);
    }

    @Override
    public void setType(UnitType type) {
        super.setType(type);
        if (!net.active()) lastTargetPos.set(x, y);

        if (!headless && trails.length != 4) {
            trails = new Trail[4];
            for (int i = 0; i < trails.length; i++) {
                trails[i] = new Trail(type.trailLength);
            }
        }
        if (type instanceof PesterUnitType pType) {
            bossTargetSearchReload = pType.checkBossReload;
            hatredCheckReload = pType.checkReload;
        }
    }

    @Override
    public Healthc findOwner(Entityc ent) {
        Healthc target = null;

        int itr = 0;
        while (ent instanceof Bullet bullet) {
            if (itr > 4) break;

            ent = bullet.owner();

            if (ent instanceof Unit u) {
                if (u.controller() instanceof MissileAI m) {
                    Unit o = m.shooter;
                    if (o.controller() instanceof MissileAI) {
                        target = o;
                    } else {
                        target = o;
                        break;
                    }
                } else {
                    target = u;
                    break;
                }
            } else if (ent instanceof Building build) {
                target = build;
                break;
            }

            itr++;
        }

        return target;
    }

    @Override
    public void collision(Hitboxc other, float x, float y) {
        if (other instanceof Bullet bullet) {
            controller.hit(bullet);

            if (bullet.team == team || bullet.type.damage + bullet.type.splashDamage + bullet.type.lightningDamage < 60)
                return;

            //Target the source of the bullet;
            Healthc target = findOwner(bullet);

            if (target != null) {
                float v = Mathf.clamp(bullet.damage / bullet.type.damage, 0.75f, 1.25f) * (bullet.type.damage + bullet.type.splashDamage + bullet.type.lightningDamage);
                hatred.increment(target, v, v);
            }
        }
    }

    @Override
    public void update() {
        isBoss(hasEffect(StatusEffects.boss));

        super.update();

        if (type instanceof PesterUnitType pType) {
            bossTargetSearchReload -= Time.delta;
            if (bossTargetSearchReload < 0 && isBoss) {
                bossTargetSearchReload = pType.checkBossReload;

                bossTarget = Units.bestTarget(team, x, y, pType.bossWeaponRange, e -> true, e -> !(e.block.group == BlockGroup.walls), WHUnitSorts.regionalHPMaximumAll);
            }

            if (bossTarget != null) {
                if (bossTargetShiftLerp <= 0.0075f && lastTarget == bossTarget) {
                    lastTargetPos.set(lastTarget.x(), lastTarget.y());
                } else {
                    if (bossTargetShiftLerp <= 0.0075f) bossTargetShiftLerp = 1f;
                    bossTargetShiftLerp = Mathf.lerpDelta(bossTargetShiftLerp, 0f, 0.075f);
                    lastTargetPos.lerp(bossTarget, 0.075f * Time.delta);
                }
            }

            lastTarget = bossTarget;

            if (lastTarget != null && lastTarget.isAdded()) {
                bossWeaponWarmup = Mathf.lerpDelta(bossWeaponWarmup, 1, 0.0075f);
                bossWeaponProgress += Time.delta * bossWeaponWarmup * (0.86f + Mathf.absin(37f, 1f) + Mathf.absin(77f, 1f)) * (0.9f - (bossWeaponReload / pType.bossReload) * 0.7f);
                bossWeaponReload += Time.delta * bossWeaponWarmup;
            } else {
                if (bossWeaponWarmup <= 0) {
                    bossWeaponWarmup = 0;
                    lastTargetPos.set(this);
                } else if (bossWeaponWarmup < 0.35f) {
                    bossWeaponWarmup -= Time.delta / 3f;
                }
                bossWeaponWarmup = Mathf.lerpDelta(bossWeaponWarmup, 0, 0.0075f);
            }

            if (bossWeaponReload > pType.bossReload) {
                bossWeaponReload = bossWeaponWarmup = bossWeaponProgress = 0;
                shootBossTarget();
                lastTargetPos.set(x, y);
            }

            if (hatred.size > 0) hatredCheckReload -= Time.delta;
            if (hatredCheckReload < 0) {
                hatredCheckReload = pType.checkReload;

                Groups.bullet.intersect(x - pType.checkRange, y - pType.checkRange, pType.checkRange * 2, pType.checkRange * 2, bullet -> {
                    if (bullet.team != team) {
                        Healthc target = findOwner(bullet);

                        if (target != null) {
                            float v = Mathf.clamp(bullet.damage / bullet.type.damage, 0.75f, 1.25f) * (bullet.type.damage + bullet.type.splashDamage + bullet.type.lightningDamage);
                            hatred.increment(target, v, v);
                        }
                    }
                });

                for (ObjectFloatMap.Entry<Healthc> e : hatred.entries()) {
                    //??Why this happens??
                    if (e.key == null) continue;

                    if (!e.key.isValid() || !within(e.key, pType.reflectRange) || ((Teamc) e.key).team() == team) {
                        hatred.remove(e.key, 0);
                        continue;
                    }


                    if (e.value > pType.checkDamage) {
                        nextTargets.add(e.key);
                        e.value -= pType.checkDamage;
                    }
                }
            }

            if (nextTargets.any()) {
                salvoReload += Time.delta * (1 + Mathf.num(isBoss) * reloadMultiplier);

                if (salvoReload > pType.salvoReload) {
                    shootAtHatred();
                    salvoReload = 0;
                }
            }
        }
    }

    @Override
    public void shootBossTarget() {
        WHBullets.ncBlackHole.create(this, team, lastTargetPos.x, lastTargetPos.y, 0, 1, 1, 1, WHBullets.ncBlackHole.splashDamageRadius);
    }

    @Override
    public void shootAtHatred() {
        Tmp.v1.trns(rotation, -type.engineOffset).add(x, y);

        float ex = Tmp.v1.x, ey = Tmp.v1.y;

        int itr = 0;
        for (Healthc hel : nextTargets) {
            if (!hel.isValid()) continue;

            tmpBuilding = null;

            boolean found = World.raycast(World.toTile(ex), World.toTile(ey), World.toTile(hel.getX()), World.toTile(hel.getY()),
                    (x, y) -> (tmpBuilding = world.build(x, y)) != null && tmpBuilding.team != team && checked.get(tmpBuilding, 0) < 2);

            Healthc t = found ? tmpBuilding : hel;
            int c = checked.increment(t, 0, 1);
            if (c <= 3) {
                Time.run(itr * 2f, () -> shoot(t));
                itr++;
            }
        }
        checked.clear();

        nextTargets.clear();

        if (!headless && itr > 0) {
            WHSounds.hugeShoot.at(ex, ey);
            WHFx.crossSpinBlast.at(ex, ey, 0, team.color, this);
        }

        if (!headless && isBoss) {
            Rand rand = WHUtils.rand;
            for (int i = 0; i < trails.length; i++) {
                Trail trail = trails[i];

                float scl = rand.random(0.75f, 1.5f) * Mathf.sign(rand.range(1)) * (i + 1) / 1.25f;
                float s = rand.random(0.75f, 1.25f);

                Tmp.v1.trns(
                        Time.time * scl * rand.random(0.5f, 1.5f) + i * 360f / trails.length + rand.random(360),
                        hitSize * (1.1f + 0.5f * i) * 0.75f
                ).add(this).add(
                        Mathf.sinDeg(Time.time * scl * rand.random(0.75f, 1.25f) * s) * hitSize * 0.75f * (i * 0.125f + 1) * rand.random(-1.5f, 1.5f),
                        Mathf.cosDeg(Time.time * scl * rand.random(0.75f, 1.25f) * s) * hitSize * 0.75f * (i * 0.125f + 1) * rand.random(-1.5f, 1.5f)
                );
                trail.update(Tmp.v1.x, Tmp.v1.y, 1 + Mathf.absin(4f, 0.2f));
            }
        }
    }

    @Override
    public void shoot(Healthc h) {
        if (state.isGame() && h.isValid() && type instanceof PesterUnitType pType) {
            pType.toBeBlastedEffect.at(h.getX(), h.getY(), h instanceof Sized s ? s.hitSize() : 30f, team.color, h);
            Fx.chainLightning.at(x, y, 0, team.color, h);

            Time.run(pType.shootDelay, () -> {
                if (state.isGame() && h.isValid()) {
                    pType.hitterBullet.create(this, team, h.getX(), h.getY(), 0);
                    heal(500);
                }
            });
        }
    }

    @Override
    public void drawBossWeapon() {
        if (bossWeaponWarmup > 0.01f && type instanceof PesterUnitType pType) {
            float fin = bossWeaponReload / pType.bossReload, fout = 1 - fin;
            float fadeS = Mathf.curve(fout, 0.0225f, 0.06f);
            float fadeS2 = Mathf.curve(fout, 0.09f, 0.185f);
            float fade = bossWeaponWarmup * Mathf.curve(fout, 0, 0.025f) * WHInterp.bounce5In.apply(fadeS);

            Tmp.v2.trns(bossWeaponProgress / 17f, Mathf.sin(bossWeaponProgress, 30f, 60f) * fout, Mathf.cos(bossWeaponProgress + 177f, 17f, 35f) * fout);
            Tmp.v3.set(Mathf.sin(bossWeaponProgress, 30, 15) * fout, Mathf.sin(bossWeaponProgress + Mathf.pi * 0.3f, 43, 12) * fout);

            float str = 3.5f * fade;

            float addtionRot = (-Drawn.rotator_120(Drawn.cycle(bossWeaponProgress, 45, 490f), 0.24f) + Mathf.absin(33f, 220f)) * fadeS2 + bossWeaponProgress;

            Tmp.v1.trns(bossWeaponProgress / 6f, fout * 160f, Mathf.absin(bossWeaponProgress, 288, 33)).scl(Mathf.curve(fout, 0.025f, 0.525f));
            Tmp.v4.set(Tmp.v1).add(lastTargetPos).add(Tmp.v2).add(Tmp.v3);

            //Draw tri aim
            Lines.stroke(str, Tmp.c1);
            Lines.poly(Tmp.v4.x, Tmp.v4.y, 3, 50f + 80f * fout, addtionRot);

            Lines.stroke(str * 3, Color.black);
            Lines.spikes(Tmp.v4.x, Tmp.v4.y, 25f + 40f * fout, Lines.getStroke(), 3, addtionRot + 60);

            Lines.stroke(str, Tmp.c1);
            Lines.line(Tmp.v4.x, Tmp.v4.y, lastTargetPos.x, lastTargetPos.y);
            Fill.circle(Tmp.v4.x, Tmp.v4.y, Lines.getStroke() * 1.8f);

            Tmp.v4.set(Tmp.v1).rotate(270f * fout + bossWeaponProgress * 0.035f).add(lastTargetPos).add(Tmp.v5.set(Tmp.v2).lerp(Tmp.v3, Mathf.absin(8f, 1f)));
            Drawn.circlePercent(Tmp.v4.x, Tmp.v4.y, 200f - 60f * fin, fin * 1.035f, Time.time / 2f);
            Lines.line(Tmp.v4.x, Tmp.v4.y, lastTargetPos.x, lastTargetPos.y);
            Fill.circle(Tmp.v4.x, Tmp.v4.y, Lines.getStroke() * 1.8f);

            float fCurveOut = Mathf.curve(fout, 0, 0.03f) * fadeS2;

            Tmp.v4.set(Tmp.v1).rotate(130f * fout + bossWeaponProgress * 0.075f).add(lastTargetPos).add(Tmp.v5.set(Tmp.v3).lerp(Tmp.v2, Mathf.absin(12f, 2f) - 1f));
            Lines.spikes(Tmp.v4.x, Tmp.v4.y, 16 + 60f * fout, 32 * fout + 28, 3, addtionRot + Mathf.absin(33f, 220f) * fCurveOut
                    - Drawn.rotator_120(Drawn.cycle(bossWeaponProgress, 0, 360f), 0.14f) * 2 * fCurveOut
                    + Drawn.rotator_120(Drawn.cycle(bossWeaponProgress, 70, 450f), 0.22f) * fCurveOut + 60
            );

            Lines.line(Tmp.v4.x, Tmp.v4.y, lastTargetPos.x, lastTargetPos.y);
            Fill.circle(Tmp.v4.x, Tmp.v4.y, Lines.getStroke() * 1.8f);

            Tmp.v4.set(lastTargetPos).add(Mathf.sin(Time.time, 36, 12) * fout, Mathf.cos(Time.time, 36, 12) * fout);
            Lines.spikes(Tmp.v4.x, Tmp.v4.y, 12 + 40 * fout, 16 * fout + 8, 4, 45 + Drawn.rotator_90());

            Fill.circle(lastTargetPos.x, lastTargetPos.y, Lines.getStroke() * 5f);
            Draw.color(Color.black);
            Fill.circle(lastTargetPos.x, lastTargetPos.y, Lines.getStroke() * 3.8f);

            Draw.color(Tmp.c1);

            for (int i : Mathf.signs) {
                float d = 220 * i * fout + 2 * i;
                float phi = Mathf.absin(8 + i * 2f, 12f) * fout;
                Lines.lineAngle(lastTargetPos.x + d + 1f * i, lastTargetPos.y + phi, 90 - i * 90, (682 + i * 75) + 220 * fin);
                Lines.lineAngleCenter(lastTargetPos.x + d, lastTargetPos.y + phi, 45, (188 + i * 20) * fout + 80);
            }

            Lines.stroke(str / 2.2f);
            Lines.spikes(lastTargetPos.x, lastTargetPos.y, WHBullets.ncBlackHole.splashDamageRadius, 12 * fade, 30, Time.time * 0.38f);
        }
    }

    @Override
    public void draw() {
        super.draw();

        if (type instanceof PesterUnitType pType) {
            Tmp.c1.set(team.color).lerp(Color.white, Mathf.absin(4f, 0.3f));

            Draw.reset();

            float z = Draw.z();

            Draw.z(Layer.effect - 0.001f);

            drawBossWeapon();

            Draw.color(Tmp.c1);

            if (isBoss) {
                Tmp.v1.trns(rotation, -type.engineOffset).add(x, y);

                float cameraFin = (1 + 2 * Drawn.cameraDstScl(Tmp.v1.x, Tmp.v1.y, mobile ? 200 : 320)) / 3f;
                float triWidth = hitSize * 0.033f * cameraFin;

                for (int i : Mathf.signs) {
                    Fill.tri(Tmp.v1.x, Tmp.v1.y + triWidth, Tmp.v1.x, Tmp.v1.y - triWidth, Tmp.v1.x + i * cameraFin * hitSize * (15 + Mathf.absin(12f, 3f)), Tmp.v1.y);
                }
            }

            Lines.stroke((3f + Mathf.absin(10f, 0.55f)) * Mathf.curve(1 - salvoReload / pType.salvoReload, 0, 0.075f));
            if (salvoReload > 5f) Drawn.circlePercent(x, y, hitSize * 1.35f, salvoReload / pType.salvoReload, 0);

            Draw.z(Layer.bullet);

            if (isBoss) {
                for (int i = 0; i < trails.length; i++) {
                    Tmp.c1.set(team.color).mul(1 + i * 0.005f).lerp(Color.white, 0.015f * i + Mathf.absin(4f, 0.3f) + Mathf.clamp(hitTime) / 5f);
                    trails[i].drawCap(Tmp.c1, type.trailScl);
                    trails[i].draw(Tmp.c1, type.trailScl);
                }
            }

            Draw.z(z);
        }
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);
        write.f(salvoReload);
        write.f(bossWeaponReload);
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);

        if (!isLocal()) {
            salvoReloadLast = salvoReload;
            salvoReloadTarget = read.f();
            bossWeaponReloadLast = bossWeaponReload;
            bossWeaponReloadTarget = read.f();
        } else {
            read.f();
            salvoReloadLast = salvoReload;
            salvoReloadTarget = salvoReload;
            read.f();
            bossWeaponReloadLast = bossWeaponReload;
            bossWeaponReloadTarget = bossWeaponReload;
        }
    }

    @Override
    public void snapSync() {
        super.snapSync();
        salvoReloadLast = salvoReloadTarget;
        salvoReload = salvoReloadTarget;
        bossWeaponReloadLast = bossWeaponReloadTarget;
        bossWeaponReload = bossWeaponReloadTarget;
    }

    @Override
    public void snapInterpolation() {
        super.snapInterpolation();
        salvoReloadLast = salvoReload;
        salvoReloadTarget = salvoReload;
        bossWeaponReloadLast = bossWeaponReload;
        bossWeaponReloadTarget = bossWeaponReload;
    }

    @Override
    public boolean isSyncHidden(Player player) {
        return nextTargets.isEmpty() && hatred.isEmpty() && !isShooting() && inFogTo(player.team());
    }

    @Override
    public void rawDamage(float amount) {
        if (type instanceof PesterUnitType pType){
            super.rawDamage(pType.damageMultiplier * amount);
        }
    }

    @Override
    public boolean isBoss() {
        return isBoss;
    }

    @Override
    public Teamc bossTarget() {
        return bossTarget;
    }

    @Override
    public Teamc lastTarget() {
        return lastTarget;
    }

    @Override
    public Vec2 lastTargetPos() {
        return lastTargetPos;
    }

    @Override
    public float bossWeaponReload() {
        return bossWeaponReload;
    }

    @Override
    public float bossWeaponWarmup() {
        return bossWeaponWarmup;
    }

    @Override
    public float bossWeaponProgress() {
        return bossWeaponProgress;
    }

    @Override
    public float bossTargetShiftLerp() {
        return bossTargetShiftLerp;
    }

    @Override
    public float bossTargetSearchReload() {
        return bossTargetSearchReload;
    }

    @Override
    public float bossWeaponReloadLast() {
        return bossWeaponReloadLast;
    }

    @Override
    public float bossWeaponReloadTarget() {
        return bossWeaponReloadTarget;
    }

    @Override
    public float hatredCheckReload() {
        return hatredCheckReload;
    }

    @Override
    public float salvoReload() {
        return salvoReload;
    }

    @Override
    public float salvoReloadLast() {
        return salvoReloadLast;
    }

    @Override
    public float salvoReloadTarget() {
        return salvoReloadTarget;
    }

    @Override
    public ObjectFloatMap<Healthc> hatred() {
        return hatred;
    }

    @Override
    public Seq<Healthc> nextTargets() {
        return nextTargets;
    }

    @Override
    public Trail[] trails() {
        return trails;
    }

    @Override
    public void isBoss(boolean value) {
        isBoss = value;
    }

    @Override
    public void bossTarget(Teamc value) {
        bossTarget = value;
    }

    @Override
    public void lastTarget(Teamc value) {
        lastTarget = value;
    }

    @Override
    public void lastTargetPos(Vec2 value) {
        lastTargetPos = value;
    }

    @Override
    public void bossWeaponReload(float value) {
        bossWeaponReload = value;
    }

    @Override
    public void bossWeaponWarmup(float value) {
        bossWeaponWarmup = value;
    }

    @Override
    public void bossWeaponProgress(float value) {
        bossWeaponProgress = value;
    }

    @Override
    public void bossTargetShiftLerp(float value) {
        bossTargetShiftLerp = value;
    }

    @Override
    public void bossTargetSearchReload(float value) {
        bossTargetSearchReload = value;
    }

    @Override
    public void bossWeaponReloadLast(float value) {
        bossWeaponReloadLast = value;
    }

    @Override
    public void bossWeaponReloadTarget(float value) {
        bossWeaponReloadTarget = value;
    }

    @Override
    public void hatredCheckReload(float value) {
        hatredCheckReload = value;
    }

    @Override
    public void salvoReload(float value) {
        salvoReload = value;
    }

    @Override
    public void salvoReloadLast(float value) {
        salvoReloadLast = value;
    }

    @Override
    public void salvoReloadTarget(float value) {
        salvoReloadTarget = value;
    }

    @Override
    public void hatred(ObjectFloatMap<Healthc> value) {
        hatred = value;
    }

    @Override
    public void nextTargets(Seq<Healthc> value) {
        nextTargets = value;
    }

    @Override
    public void trails(Trail[] value) {
        trails = value;
    }

    public static PesterUnit create() {
        return new PesterUnit();
    }
}
