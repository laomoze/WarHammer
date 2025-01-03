//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.gen;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import java.util.Iterator;
import mindustry.Vars;
import mindustry.ai.types.MissileAI;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.Sized;
import mindustry.entities.Units;
import mindustry.entities.units.UnitController;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import mindustry.gen.Healthc;
import mindustry.gen.Hitboxc;
import mindustry.gen.Player;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.gen.UnitEntity;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;
import mindustry.world.meta.BlockGroup;
import wh.content.WHBullets;
import wh.content.WHFx;
import wh.entities.WHUnitSorts;
import wh.graphics.Drawn;
import wh.math.WHInterp;
import wh.type.unit.PesterUnitType;
import wh.util.WHUtils;

public class PesterUnit extends UnitEntity implements Pesterc {
    public static final ObjectIntMap<Healthc> checked = new ObjectIntMap();
    public static Building tmpBuilding = null;
    public boolean isBoss = false;
    public Teamc bossTarget;
    public Teamc lastTarget;
    public transient Vec2 lastTargetPos = new Vec2();
    public float bossWeaponReload;
    public float bossWeaponWarmup;
    public float bossWeaponProgress;
    public float bossTargetShiftLerp = 0.0F;
    public float bossTargetSearchReload = 0.0F;
    public transient float bossWeaponReloadLast = 0.0F;
    public transient float bossWeaponReloadTarget = 0.0F;
    public float hatredCheckReload = 0.0F;
    public float salvoReload = 0.0F;
    public transient float salvoReloadLast = 0.0F;
    public transient float salvoReloadTarget = 0.0F;
    public ObjectFloatMap<Healthc> hatred = new ObjectFloatMap();
    public Seq<Healthc> nextTargets = new Seq();
    protected Trail[] trails = new Trail[0];

    protected PesterUnit() {
    }

    public int classId() {
        return EntityRegister.getId(PesterUnit.class);
    }

    public void setType(UnitType type) {
        super.setType(type);
        if (!Vars.net.active()) {
             lastTargetPos.set( x,  y);
        }

        if (!Vars.headless &&  trails.length != 4) {
             trails = new Trail[4];

            for(int i = 0; i <  trails.length; ++i) {
                 trails[i] = new Trail(type.trailLength);
            }
        }

        if (type instanceof PesterUnitType) {
            PesterUnitType pType = (PesterUnitType)type;
             bossTargetSearchReload = pType.checkBossReload;
             hatredCheckReload = pType.checkReload;
        }

    }

    public Healthc findOwner(Entityc ent) {
        Healthc target = null;

        for(int itr = 0; ent instanceof Bullet; ++itr) {
            Bullet bullet = (Bullet)ent;
            if (itr > 4) {
                break;
            }

            ent = bullet.owner();
            if (ent instanceof Unit) {
                Unit u = (Unit)ent;
                UnitController var8 = u.controller();
                if (!(var8 instanceof MissileAI)) {
                    target = u;
                    break;
                }

                MissileAI m = (MissileAI)var8;
                Unit o = m.shooter;
                if (!(o.controller() instanceof MissileAI)) {
                    target = o;
                    break;
                }

                target = o;
            } else if (ent instanceof Building) {
                Building build = (Building)ent;
                target = build;
                break;
            }
        }

        return (Healthc)target;
    }

    public void collision(Hitboxc other, float x, float y) {
        if (other instanceof Bullet) {
            Bullet bullet = (Bullet)other;
             controller.hit(bullet);
            if (bullet.team ==  team || bullet.type.damage + bullet.type.splashDamage + bullet.type.lightningDamage < 60.0F) {
                return;
            }

            Healthc target =  findOwner(bullet);
            if (target != null) {
                float v = Mathf.clamp(bullet.damage / bullet.type.damage, 0.75F, 1.25F) * (bullet.type.damage + bullet.type.splashDamage + bullet.type.lightningDamage);
                 hatred.increment(target, v, v);
            }
        }

    }

    public void update() {
         isBoss( hasEffect(StatusEffects.boss));
        super.update();
        UnitType var2 =  type;
        if (var2 instanceof PesterUnitType) {
            PesterUnitType pType = (PesterUnitType)var2;
             bossTargetSearchReload -= Time.delta;
            if ( bossTargetSearchReload < 0.0F &&  isBoss) {
                 bossTargetSearchReload = pType.checkBossReload;
                 bossTarget = Units.bestTarget( team,  x,  y, pType.bossWeaponRange, (ex) -> {
                    return true;
                }, (ex) -> {
                    return ex.block.group != BlockGroup.walls;
                }, WHUnitSorts.regionalHPMaximumAll);
            }

            if ( bossTarget != null) {
                if ( bossTargetShiftLerp <= 0.0075F &&  lastTarget ==  bossTarget) {
                     lastTargetPos.set( lastTarget.x(),  lastTarget.y());
                } else {
                    if ( bossTargetShiftLerp <= 0.0075F) {
                         bossTargetShiftLerp = 1.0F;
                    }

                     bossTargetShiftLerp = Mathf.lerpDelta( bossTargetShiftLerp, 0.0F, 0.075F);
                     lastTargetPos.lerp( bossTarget, 0.075F * Time.delta);
                }
            }

             lastTarget =  bossTarget;
            if ( lastTarget != null &&  lastTarget.isAdded()) {
                 bossWeaponWarmup = Mathf.lerpDelta( bossWeaponWarmup, 1.0F, 0.0075F);
                 bossWeaponProgress += Time.delta *  bossWeaponWarmup * (0.86F + Mathf.absin(37.0F, 1.0F) + Mathf.absin(77.0F, 1.0F)) * (0.9F -  bossWeaponReload / pType.bossReload * 0.7F);
                 bossWeaponReload += Time.delta *  bossWeaponWarmup;
            } else {
                if ( bossWeaponWarmup <= 0.0F) {
                     bossWeaponWarmup = 0.0F;
                     lastTargetPos.set(this);
                } else if ( bossWeaponWarmup < 0.35F) {
                     bossWeaponWarmup -= Time.delta / 3.0F;
                }

                 bossWeaponWarmup = Mathf.lerpDelta( bossWeaponWarmup, 0.0F, 0.0075F);
            }

            if ( bossWeaponReload > pType.bossReload) {
                 bossWeaponReload =  bossWeaponWarmup =  bossWeaponProgress = 0.0F;
                 shootBossTarget();
                 lastTargetPos.set( x,  y);
            }

            if ( hatred.size > 0) {
                 hatredCheckReload -= Time.delta;
            }

            if ( hatredCheckReload < 0.0F) {
                 hatredCheckReload = pType.checkReload;
                Groups.bullet.intersect( x - pType.checkRange,  y - pType.checkRange, pType.checkRange * 2.0F, pType.checkRange * 2.0F, (bullet) -> {
                    if (bullet.team !=  team) {
                        Healthc target =  findOwner(bullet);
                        if (target != null) {
                            float v = Mathf.clamp(bullet.damage / bullet.type.damage, 0.75F, 1.25F) * (bullet.type.damage + bullet.type.splashDamage + bullet.type.lightningDamage);
                             hatred.increment(target, v, v);
                        }
                    }

                });
                ObjectFloatMap.Entries var4 =  hatred.entries().iterator();

                label70:
                while(true) {
                    while(true) {
                        ObjectFloatMap.Entry e;
                        do {
                            if (!var4.hasNext()) {
                                break label70;
                            }

                            e = (ObjectFloatMap.Entry)var4.next();
                        } while(e.key == null);

                        if (((Healthc)e.key).isValid() &&  within((Position)e.key, pType.reflectRange) && ((Teamc)e.key).team() !=  team) {
                            if (e.value > pType.checkDamage) {
                                 nextTargets.add((Healthc)e.key);
                                e.value -= pType.checkDamage;
                            }
                        } else {
                             hatred.remove((Healthc)e.key, 0.0F);
                        }
                    }
                }
            }

            if ( nextTargets.any()) {
                 salvoReload += Time.delta * (1.0F + (float)Mathf.num( isBoss) *  reloadMultiplier);
                if ( salvoReload > pType.salvoReload) {
                     shootAtHatred();
                     salvoReload = 0.0F;
                }
            }
        }


    }
    public void shootBossTarget(){WHBullets.ncBlackHole.create(self(), team, lastTargetPos.x, lastTargetPos.y, 0, 1, 1, 1,  WHBullets.ncBlackHole.splashDamageRadius);
    }
    public void shootAtHatred() {
        Tmp.v1.trns(this.rotation, 2 * Vars.tilesize).add(this.x, this.y);
        Tmp.v1.y += 2 * Vars.tilesize; // 确保Y坐标的偏移也被考虑
        float ex = Tmp.v1.x;
        float ey = Tmp.v1.y;
        int itr = 0;
        Iterator var4 = nextTargets.iterator();

        while(var4.hasNext()) {
            Healthc hel = (Healthc)var4.next();
            if (hel.isValid()) {
                tmpBuilding = null;
                boolean found = World.raycast(World.toTile(ex), World.toTile(ey), World.toTile(hel.getX()), World.toTile(hel.getY()), (x, y) -> {
                    return (tmpBuilding = Vars.world.build(x, y)) != null && tmpBuilding.team != team && checked.get(tmpBuilding, 0) < 2;
                });
                Healthc t = found ? tmpBuilding : hel;
                int c = checked.increment(t, 0, 1);
                if (c <= 3) {
                    Time.run((float)itr * 2.0F, () -> {
                        shoot(t);
                    });
                    ++itr;
                }
            }
        }

        checked.clear();
        nextTargets.clear();
        if (!Vars.headless && itr > 0) {
            WHSounds.hugeShoot.at(ex, ey);
            WHFx.crossSpinBlast.at(ex, ey, 0.0F, team.color, this);
        }
        if (!Vars.headless &&  isBoss) {
            Rand rand = WHUtils.rand;

            for(int i = 0; i <  trails.length; ++i) {
                Trail trail =  trails[i];
                float scl = rand.random(0.75F, 1.5F) * (float)Mathf.sign((float)rand.range(1)) * (float)(i + 1) / 1.25F;
                float s = rand.random(0.75F, 1.25F);
                Tmp.v1.trns(Time.time * scl * rand.random(0.5F, 1.5F) + (float)i * 360.0F / (float) trails.length + (float)rand.random(360),  hitSize * (1.1F + 0.5F * (float)i) * 0.75F).add(this).add(Mathf.sinDeg(Time.time * scl * rand.random(0.75F, 1.25F) * s) *  hitSize * 0.75F * ((float)i * 0.125F + 1.0F) * rand.random(-1.5F, 1.5F), Mathf.cosDeg(Time.time * scl * rand.random(0.75F, 1.25F) * s) *  hitSize * 0.75F * ((float)i * 0.125F + 1.0F) * rand.random(-1.5F, 1.5F));
                trail.update(Tmp.v1.x, Tmp.v1.y, 1.0F + Mathf.absin(4.0F, 0.2F));
            }
        }

    }

    public void shoot(Healthc h) {
        if (Vars.state.isGame() && h.isValid()) {
            UnitType var3 =  type;
            if (var3 instanceof PesterUnitType) {
                PesterUnitType pType = (PesterUnitType)var3;
                Effect var10000 = pType.toBeBlastedEffect;
                float var10001 = h.getX();
                float var10002 = h.getY();
                float var10003;
                if (h instanceof Sized) {
                    Sized s = (Sized)h;
                    var10003 = s.hitSize();
                } else {
                    var10003 = 30.0F;
                }

                var10000.at(var10001, var10002, var10003,  team.color, h);
                Fx.chainLightning.at( x,  y, 0.0F,  team.color, h);
                Time.run(pType.shootDelay, () -> {
                    if (Vars.state.isGame() && h.isValid()) {
                        pType.hitterBullet.create(this,  team, h.getX(), h.getY(), 0.0F);
                         heal(500.0F);
                    }

                });
            }
        }

    }

    public void drawBossWeapon() {
        if ( bossWeaponWarmup > 0.01F) {
            UnitType var2 =  type;
            if (var2 instanceof PesterUnitType) {
                PesterUnitType pType = (PesterUnitType)var2;
                float fin =  bossWeaponReload / pType.bossReload;
                float fout = 1.0F - fin;
                float fadeS = Mathf.curve(fout, 0.0225F, 0.06F);
                float fadeS2 = Mathf.curve(fout, 0.09F, 0.185F);
                float fade =  bossWeaponWarmup * Mathf.curve(fout, 0.0F, 0.025F) * WHInterp.bounce5In.apply(fadeS);
                Tmp.v2.trns( bossWeaponProgress / 17.0F, Mathf.sin( bossWeaponProgress, 30.0F, 60.0F) * fout, Mathf.cos( bossWeaponProgress + 177.0F, 17.0F, 35.0F) * fout);
                Tmp.v3.set(Mathf.sin( bossWeaponProgress, 30.0F, 15.0F) * fout, Mathf.sin( bossWeaponProgress + 0.9424779F, 43.0F, 12.0F) * fout);
                float str = 3.5F * fade;
                float addtionRot = (-Drawn.rotator_120(Drawn.cycle( bossWeaponProgress, 45.0F, 490.0F), 0.24F) + Mathf.absin(33.0F, 220.0F)) * fadeS2 +  bossWeaponProgress;
                Tmp.v1.trns( bossWeaponProgress / 6.0F, fout * 160.0F, Mathf.absin( bossWeaponProgress, 288.0F, 33.0F)).scl(Mathf.curve(fout, 0.025F, 0.525F));
                Tmp.v4.set(Tmp.v1).add( lastTargetPos).add(Tmp.v2).add(Tmp.v3);
                Lines.stroke(str, Tmp.c1);
                Lines.poly(Tmp.v4.x, Tmp.v4.y, 3, 50.0F + 80.0F * fout, addtionRot);
                Lines.stroke(str * 3.0F, Color.black);
                Lines.spikes(Tmp.v4.x, Tmp.v4.y, 25.0F + 40.0F * fout, Lines.getStroke(), 3, addtionRot + 60.0F);
                Lines.stroke(str, Tmp.c1);
                Lines.line(Tmp.v4.x, Tmp.v4.y,  lastTargetPos.x,  lastTargetPos.y);
                Fill.circle(Tmp.v4.x, Tmp.v4.y, Lines.getStroke() * 1.8F);
                Tmp.v4.set(Tmp.v1).rotate(270.0F * fout +  bossWeaponProgress * 0.035F).add( lastTargetPos).add(Tmp.v5.set(Tmp.v2).lerp(Tmp.v3, Mathf.absin(8.0F, 1.0F)));
                Drawn.circlePercent(Tmp.v4.x, Tmp.v4.y, 200.0F - 60.0F * fin, fin * 1.035F, Time.time / 2.0F);
                Lines.line(Tmp.v4.x, Tmp.v4.y,  lastTargetPos.x,  lastTargetPos.y);
                Fill.circle(Tmp.v4.x, Tmp.v4.y, Lines.getStroke() * 1.8F);
                float fCurveOut = Mathf.curve(fout, 0.0F, 0.03F) * fadeS2;
                Tmp.v4.set(Tmp.v1).rotate(130.0F * fout +  bossWeaponProgress * 0.075F).add( lastTargetPos).add(Tmp.v5.set(Tmp.v3).lerp(Tmp.v2, Mathf.absin(12.0F, 2.0F) - 1.0F));
                Lines.spikes(Tmp.v4.x, Tmp.v4.y, 16.0F + 60.0F * fout, 32.0F * fout + 28.0F, 3, addtionRot + Mathf.absin(33.0F, 220.0F) * fCurveOut - Drawn.rotator_120(Drawn.cycle( bossWeaponProgress, 0.0F, 360.0F), 0.14F) * 2.0F * fCurveOut + Drawn.rotator_120(Drawn.cycle( bossWeaponProgress, 70.0F, 450.0F), 0.22F) * fCurveOut + 60.0F);
                Lines.line(Tmp.v4.x, Tmp.v4.y,  lastTargetPos.x,  lastTargetPos.y);
                Fill.circle(Tmp.v4.x, Tmp.v4.y, Lines.getStroke() * 1.8F);
                Tmp.v4.set( lastTargetPos).add(Mathf.sin(Time.time, 36.0F, 12.0F) * fout, Mathf.cos(Time.time, 36.0F, 12.0F) * fout);
                Lines.spikes(Tmp.v4.x, Tmp.v4.y, 12.0F + 40.0F * fout, 16.0F * fout + 8.0F, 4, 45.0F + Drawn.rotator_90());
                Fill.circle( lastTargetPos.x,  lastTargetPos.y, Lines.getStroke() * 5.0F);
                Draw.color(Color.black);
                Fill.circle( lastTargetPos.x,  lastTargetPos.y, Lines.getStroke() * 3.8F);
                Draw.color(Tmp.c1);
                int[] var10 = Mathf.signs;
                int var11 = var10.length;

                for(int var12 = 0; var12 < var11; ++var12) {
                    int i = var10[var12];
                    float d = (float)(220 * i) * fout + (float)(2 * i);
                    float phi = Mathf.absin(8.0F + (float)i * 2.0F, 12.0F) * fout;
                    Lines.lineAngle( lastTargetPos.x + d + 1.0F * (float)i,  lastTargetPos.y + phi, (float)(90 - i * 90), (float)(682 + i * 75) + 220.0F * fin);
                    Lines.lineAngleCenter( lastTargetPos.x + d,  lastTargetPos.y + phi, 45.0F, (float)(188 + i * 20) * fout + 80.0F);
                }

                Lines.stroke(str / 2.2F);
                Lines.spikes( lastTargetPos.x,  lastTargetPos.y, WHBullets.ncBlackHole.splashDamageRadius, 12.0F * fade, 30, Time.time * 0.38F);
            }
        }

    }

    public void draw() {
        super.draw();
        UnitType var2 =  type;
        if (var2 instanceof PesterUnitType) {
            PesterUnitType pType = (PesterUnitType)var2;
            Tmp.c1.set( team.color).lerp(Color.white, Mathf.absin(4.0F, 0.3F));
            Draw.reset();
            float z = Draw.z();
            Draw.z(109.999F);
             drawBossWeapon();
            Draw.color(Tmp.c1);
            if ( isBoss) {
                Tmp.v1.trns( rotation, - type.engineOffset).add( x,  y);
                float cameraFin = (1.0F + 2.0F * Drawn.cameraDstScl(Tmp.v1.x, Tmp.v1.y, Vars.mobile ? 200.0F : 320.0F)) / 3.0F;
                float triWidth =  hitSize * 0.033F * cameraFin;
                int[] var5 = Mathf.signs;
                int var6 = var5.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    int i = var5[var7];
                    Fill.tri(Tmp.v1.x, Tmp.v1.y + triWidth, Tmp.v1.x, Tmp.v1.y - triWidth, Tmp.v1.x + (float)i * cameraFin *  hitSize * (15.0F + Mathf.absin(12.0F, 3.0F)), Tmp.v1.y);
                }
            }

            Lines.stroke((3.0F + Mathf.absin(10.0F, 0.55F)) * Mathf.curve(1.0F -  salvoReload / pType.salvoReload, 0.0F, 0.075F));
            if ( salvoReload > 5.0F) {
                Drawn.circlePercent( x,  y,  hitSize * 1.35F,  salvoReload / pType.salvoReload, 0.0F);
            }

            Draw.z(100.0F);
            if ( isBoss) {
                for(int i = 0; i <  trails.length; ++i) {
                    Tmp.c1.set( team.color).mul(1.0F + (float)i * 0.005F).lerp(Color.white, 0.015F * (float)i + Mathf.absin(4.0F, 0.3F) + Mathf.clamp( hitTime) / 5.0F);
                     trails[i].drawCap(Tmp.c1,  type.trailScl);
                     trails[i].draw(Tmp.c1,  type.trailScl);
                }
            }

            Draw.z(z);
        }

    }

    public void writeSync(Writes write) {
        super.writeSync(write);
      write.f( salvoReload);
        write.f( bossWeaponReload);
    }

    public void readSync(Reads read) {
        super.readSync(read);
        if (! isLocal()) {
             salvoReloadLast =  salvoReload;
             salvoReloadTarget = read.f();
             bossWeaponReloadLast =  bossWeaponReload;
             bossWeaponReloadTarget = read.f();
        } else {
            read.f();
             salvoReloadLast =  salvoReload;
             salvoReloadTarget =  salvoReload;
            read.f();
             bossWeaponReloadLast =  bossWeaponReload;
             bossWeaponReloadTarget =  bossWeaponReload;
        }

    }

    public void snapSync() {
        super.snapSync();
         salvoReloadLast =  salvoReloadTarget;
         salvoReload =  salvoReloadTarget;
         bossWeaponReloadLast =  bossWeaponReloadTarget;
         bossWeaponReload =  bossWeaponReloadTarget;
    }

    public void snapInterpolation() {
        super.snapInterpolation();
         salvoReloadLast =  salvoReload;
         salvoReloadTarget =  salvoReload;
         bossWeaponReloadLast =  bossWeaponReload;
         bossWeaponReloadTarget =  bossWeaponReload;
    }

    public boolean isSyncHidden(Player player) {
        return  nextTargets.isEmpty() &&  hatred.isEmpty() && ! isShooting() &&  inFogTo(player.team());
    }

    public void rawDamage(float amount) {
        UnitType var3 =  type;
        if (var3 instanceof PesterUnitType) {
            PesterUnitType pType = (PesterUnitType)var3;
            super.rawDamage(pType.damageMultiplier * amount);
        }

    }

    public boolean isBoss() {
        return  isBoss;
    }

    public Teamc bossTarget() {
        return  bossTarget;
    }

    public Teamc lastTarget() {
        return  lastTarget;
    }

    public Vec2 lastTargetPos() {
        return  lastTargetPos;
    }

    public float bossWeaponReload() {
        return  bossWeaponReload;
    }

    public float bossWeaponWarmup() {
        return  bossWeaponWarmup;
    }

    public float bossWeaponProgress() {
        return  bossWeaponProgress;
    }

    public float bossTargetShiftLerp() {
        return  bossTargetShiftLerp;
    }

    public float bossTargetSearchReload() {
        return  bossTargetSearchReload;
    }

    public float bossWeaponReloadLast() {
        return  bossWeaponReloadLast;
    }

    public float bossWeaponReloadTarget() {
        return  bossWeaponReloadTarget;
    }

    public float hatredCheckReload() {
        return  hatredCheckReload;
    }

    public float salvoReload() {
        return  salvoReload;
    }

    public float salvoReloadLast() {
        return  salvoReloadLast;
    }

    public float salvoReloadTarget() {
        return  salvoReloadTarget;
    }

    public ObjectFloatMap<Healthc> hatred() {
        return  hatred;
    }

    public Seq<Healthc> nextTargets() {
        return  nextTargets;
    }

    public Trail[] trails() {
        return  trails;
    }

    public void isBoss(boolean value) {
         isBoss = value;
    }

    public void bossTarget(Teamc value) {
         bossTarget = value;
    }

    public void lastTarget(Teamc value) {
         lastTarget = value;
    }

    public void lastTargetPos(Vec2 value) {
         lastTargetPos = value;
    }

    public void bossWeaponReload(float value) {
         bossWeaponReload = value;
    }

    public void bossWeaponWarmup(float value) {
         bossWeaponWarmup = value;
    }

    public void bossWeaponProgress(float value) {
         bossWeaponProgress = value;
    }

    public void bossTargetShiftLerp(float value) {
         bossTargetShiftLerp = value;
    }

    public void bossTargetSearchReload(float value) {
         bossTargetSearchReload = value;
    }

    public void bossWeaponReloadLast(float value) {
         bossWeaponReloadLast = value;
    }

    public void bossWeaponReloadTarget(float value) {
         bossWeaponReloadTarget = value;
    }

    public void hatredCheckReload(float value) {
         hatredCheckReload = value;
    }

    public void salvoReload(float value) {
         salvoReload = value;
    }

    public void salvoReloadLast(float value) {
         salvoReloadLast = value;
    }

    public void salvoReloadTarget(float value) {
         salvoReloadTarget = value;
    }

    public void hatred(ObjectFloatMap<Healthc> value) {
         hatred = value;
    }

    public void nextTargets(Seq<Healthc> value) {
         nextTargets = value;
    }

    public void trails(Trail[] value) {
         trails = value;
    }

    public static PesterUnit create() {
        return new PesterUnit();
    }
}
