package wh.entities.abilities;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.Interval;
import arc.util.Strings;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import wh.content.WHStats;

import static mindustry.Vars.tilesize;
import static wh.core.WarHammerMod.name;

/** 周围友军数量越多，单位获得的团队伤害加成越高。 */
public class TeamCombatAbility extends Ability{
    public float range = 120f;
    public int minUnits = 3;
    public int maxUnits = 15;
    public float damageBoostPerUnit = 0.01f;
    public float interval = 15f;

    protected transient Interval timer = new Interval();
    // 最近一次扫描统计到的有效友军数。
    protected transient int countedUnits;
    // 结算后实际应用的团队伤害倍率。
    protected transient float damageMultiplier = 1f;

    public TeamCombatAbility(){
    }

    public TeamCombatAbility(float range, int minUnits, int maxUnits, float damageBoostPerUnit){
        this.range = range;
        this.minUnits = minUnits;
        this.maxUnits = maxUnits;
        this.damageBoostPerUnit = damageBoostPerUnit;
    }

    @Override
    public void update(Unit unit){
        // 固定间隔扫描我方团队单位，避免每帧都去遍历附近单位。
        if(timer.get(0, interval)){
            recalculate(unit);
        }

        unit.damageMultiplier *= damageMultiplier;
    }

    protected void recalculate(Unit unit){
        countedUnits = 0;

        Units.nearby(unit.team, unit.x - range, unit.y - range, range * 2f, range * 2f, other -> {
            // 团队作战只统计我方团队，不把自己算进去。
            if(other == unit || other.dead() || !other.isValid() || other.team != unit.team || !other.within(unit, range)){
                return;
            }

            // 满足“同类型”或“体型不大于自己”任一条件时，计入加成。
            if(other.type == unit.type || other.hitSize <= unit.hitSize + 0.001f){
                countedUnits++;
            }
        });

        int effectiveUnits = countedUnits >= minUnits ? Math.min(countedUnits, maxUnits) : 0;
        damageMultiplier = 1f + effectiveUnits * damageBoostPerUnit;
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(WHStats.format("wh-range", Strings.autoFixed(range / tilesize, 2) + " " + mindustry.world.meta.StatUnit.blocks.localized()));
        t.row();
        t.add(WHStats.format("wh-team-combat-threshold", minUnits));
        t.row();
        t.add(WHStats.format("wh-team-combat-max-count", maxUnits));
        t.row();
        t.add(WHStats.format("wh-team-combat-damage", Strings.autoFixed(damageBoostPerUnit * 100f, 2) + "%"));
    }

    @Override
    public TeamCombatAbility copy(){
        TeamCombatAbility out = (TeamCombatAbility)super.copy();
        out.timer = new Interval();
        out.countedUnits = 0;
        out.damageMultiplier = 1f;
        return out;
    }

    @Override
    public String localized(){
        return Core.bundle.get(getBundle());
    }

    @Override
    public String getBundle(){
        return "ability." + name("team-combat-ability");
    }
}
