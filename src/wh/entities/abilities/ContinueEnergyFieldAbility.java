package wh.entities.abilities;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import wh.graphics.*;

import static mindustry.Vars.*;
import static wh.core.WarHammerMod.name;

public class ContinueEnergyFieldAbility extends EnergyFieldAbility{
    private static final Seq<Healthc> all = new Seq<>();
    public int shots, buildPierceCap = 4, unitPierceCap = 2;
    public float shotsDelay, delay = 20;
    public float stroke = 1.3f;
    public float unitDamage;
    public Color color = Pal.heal;

    protected float timer, curStroke;
    protected boolean anyNearby = false, pierceCap;

    public ContinueEnergyFieldAbility(float damage, float unitDamage, float reload, float range, int shots, float shotsDelay){
        super(damage, reload, range);
        this.shots = shots;
        this.shotsDelay = shotsDelay;
        this.unitDamage = unitDamage;
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(Core.bundle.format("stat.wh-shots", shots));
        t.row();
        t.add(Core.bundle.format("stat.wh-unitDamage", unitDamage));
    }

    @Override
    public void init(UnitType type){
        super.init(type);
        if(buildPierceCap > 0 || unitPierceCap > 0){
            pierceCap = true;
        }
    }

    @Override
    public void draw(Unit unit){
        Draw.z(layer);
        Draw.color(color);
        Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
        float rx = Tmp.v1.x, ry = Tmp.v1.y;
        float orbRadius = effectRadius * (1f + Mathf.absin(blinkScl, blinkSize));

        Fill.circle(rx, ry, orbRadius);
        Draw.color();
        Fill.circle(rx, ry, orbRadius / 2f);

        Lines.stroke((stroke + Mathf.absin(blinkScl, 0.7f)), color);

        for(int i = 0; i < sectors; i++){
            float rot = unit.rotation + i * 360f / sectors - Time.time * rotateSpeed;
            Lines.arc(rx, ry, orbRadius + 3f, sectorRad, rot);
        }

        Lines.stroke(Lines.getStroke() * curStroke);

        if(curStroke > 0){
            for(int i = 0; i < sectors; i++){
                float rot = unit.rotation + i * 360f / sectors + Time.time * rotateSpeed;
                Lines.arc(rx, ry, range, sectorRad, rot);
            }
        }

        Drawf.light(rx, ry, range * 1.5f, color, curStroke * 0.8f);

        Draw.reset();
    }

    @Override
    public void update(Unit unit){
        timer += Time.delta;

        curStroke = Mathf.lerpDelta(curStroke, anyNearby ? 1 : 0, 0.09f);

        if(timer >= reload && (!useAmmo || unit.ammo > 0 || !state.rules.unitAmmo)){
            Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
            float rx = Tmp.v1.x, ry = Tmp.v1.y;
            anyNearby = false;

            all.clear();

            if(hitUnits){
                Units.nearby(null, rx, ry, range, other -> {
                    if(other != unit && other.checkTarget(targetAir, targetGround) && other.targetable(unit.team) && (other.team != unit.team || other.damaged())){
                        all.add(other);
                    }
                });
            }

            if(hitBuildings && targetGround){
                Units.nearbyBuildings(rx, ry, range, b -> {
                    if((b.team != Team.derelict || state.rules.coreCapture) && ((b.team != unit.team && b.block.targetable) || b.damaged()) && !b.block.privileged){
                        all.add(b);
                    }
                });
            }

            all.sort(h -> {
                boolean sameTeam;
                if(h instanceof Building b){
                    sameTeam = b.team() == unit.team;
                }else{
                    sameTeam = ((Teamc)h).team() == unit.team;
                }
                boolean isDamaged = h.damaged();

                float baseValue = -h.dst2(rx, ry) * 10 + h.maxHealth();

                if(sameTeam && isDamaged){
                    return h.dst2(rx, ry) + h.health() * 20 - 11451419f;
                }else{
                    return baseValue;
                }
            });

            Seq<Healthc> localAll = all.copy();

            int len = Math.min(all.size, maxTargets);
            for(int i = 0; i < len; i++){
                int finalI = i;
                Time.run(delay * i, () -> {
                    for(int a = 0; a < shots; a++){
                        if(unit.dead()){
                            timer = 0;
                            return;
                        }
                        Time.run(shotsDelay * a, () -> {
                            Healthc other = localAll.get(finalI);

                            Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);

                            //lightning gets absorbed by plastanium
                            var absorber = Damage.findAbsorber(unit.team, rx, ry, other.getX(), other.getY());
                            if(absorber != null){
                                other = absorber;
                            }

                            if(((Teamc)other).team() == unit.team){
                                if(other.damaged()){
                                    Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
                                    anyNearby = true;
                                    float healMult = (other instanceof Unit u && u.type == unit.type) ? sameTypeHealMult : 1f;
                                    other.heal(healPercent / 100f * other.maxHealth() * healMult);
                                    healEffect.at(other);
                                    PositionLightning.createEffect(Tmp.v1, other, color, 2, Mathf.randomSeed(unit.id, 1.5f, 2.5f));
                                    hitEffect.at(rx, ry, unit.angleTo(other), color);

                                    if(other instanceof Building b){
                                        Fx.healBlockFull.at(b.x, b.y, 0f, color, b.block);
                                    }
                                }
                            }else{
                                Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
                                anyNearby = true;
                                if(other instanceof Building b){
                                    PositionLightning.createEffect(Tmp.v1, b, color, 2, Mathf.randomSeed(unit.id, 1.5f, 2.5f));
                                    b.damage(unit.team, damage * state.rules.unitDamage(unit.team));
                                }else{
                                    PositionLightning.createEffect(Tmp.v1, other, color, 1, Mathf.randomSeed(unit.id, 1.5f, 2.5f));
                                    other.damage(damage * state.rules.unitDamage(unit.team));
                                }

                                if(pierceCap && !unit.dead && other.within(rx, ry, range * 1.4f) && (!other.dead() && other.isValid())){
                                    tmpHealthc.clear();
                                    iterateHealthc(other, unit, rx, ry);
                                    int pierce = other instanceof Building ? buildPierceCap : unitPierceCap;

                                    int iterateLen = Math.min(tmpHealthc.size, pierce);
                                    for(int c = 0; c < iterateLen; c++){
                                        Healthc iterateHealthc = tmpHealthc.get(c);
                                        if(!iterateHealthc.dead()){
                                            if(iterateHealthc instanceof Building building){
                                                //damageEffect.at(rx, ry, 0f, color, other);
                                                PositionLightning.createEffect(Tmp.v1, other, color, 1, Mathf.randomSeed(unit.id, 1.5f, 2.5f));
                                                hitEffect.at(building.x(), building.y(), unit.angleTo(building), color);
                                                building.damage(unit.team, damage * state.rules.unitDamage(unit.team));
                                            }else if(unit.isValid()){
                                                Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
                                                //damageEffect.at(rx, ry, 0f, color, other);
                                                PositionLightning.createEffect(Tmp.v1, other, color, 1, Mathf.randomSeed(unit.id, 1.5f, 2.5f));
                                                hitEffect.at(iterateHealthc.x(), iterateHealthc.y(), unit.angleTo(iterateHealthc), color);
                                                iterateHealthc.damage(unitDamage * state.rules.unitDamage(unit.team));
                                            }
                                        }
                                    }
                                }

                                if(other instanceof Statusc s){
                                    s.apply(status, statusDuration);
                                }
                                hitEffect.at(other.x(), other.y(), unit.angleTo(other), color);
                                hitEffect.at(rx, ry, unit.angleTo(other), color);
                            }
                        });
                    }
                });
            }
            if(anyNearby){
                shootSound.at(unit);

                if(useAmmo && state.rules.unitAmmo){
                    unit.ammo--;
                }
            }
            timer = 0;
        }
    }

    private static final Seq<Healthc> tmpHealthc = new Seq<>();
    private static final Rect rect = new Rect();
    private static final Rect hitrect = new Rect();

    public void iterateHealthc(Healthc other, Unit unit, float x1, float y1){

        if(other instanceof Building building){
            World.raycast(World.toTile(x1), World.toTile(y1), World.toTile(other.getX()), World.toTile(other.getY()), (x, y) -> {
                Building tmpBuilding = world.build(x, y);
                if(tmpBuilding != null && tmpBuilding.team != unit.team && tmpBuilding != building){
                    tmpHealthc.add(tmpBuilding);
                }
                return false;
            });
        }

        float x2 = other.getX() - x1, y2 = other.getY() - y1, expand = Vars.tilesize;
        rect.setPosition(x1, y1).setSize(x2, y2).normalize().grow(expand * 2, unit.dst(other));
        Units.nearbyEnemies(unit.team, rect, u -> {
            u.hitbox(hitrect);
            if(u.checkTarget(unit.type.targetAir, unit.type.targetGround) && u.hittable() && u.isValid() && !u.dead
            && Intersector.intersectSegmentRectangle(x1, y1, other.getX(), other.getY(), hitrect)){
                tmpHealthc.add(u);
            }
        });

        tmpHealthc.sort(b1 -> -b1.dst2(x1, y1));
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability." + name("ContinueEnergyFieldAbility"));
    }
}
