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

/** 统计周围敌军数量，并按人数提高近距离作战伤害。 */
public class CloseCombatAbility extends Ability{
    public float range = 90f;
    public int maxEnemies = 8;
    public float damageBoostPerEnemy = 0.01f;
    public float interval = 15f;

    protected transient Interval timer = new Interval();
    // 最近一次扫描统计到的敌军数。
    protected transient int enemyCount;
    // 扫描后实际施加给单位的伤害倍率。
    protected transient float damageMultiplier = 1f;

    public CloseCombatAbility(){
    }

    public CloseCombatAbility(float range, int maxEnemies, float damageBoostPerEnemy){
        this.range = range;
        this.maxEnemies = maxEnemies;
        this.damageBoostPerEnemy = damageBoostPerEnemy;
    }

    @Override
    public void update(Unit unit){
        // 固定间隔扫描敌方团队单位，减少高频查询开销。
        if(timer.get(0, interval)){
            recalculate(unit);
        }

        unit.damageMultiplier *= damageMultiplier;
    }

    protected void recalculate(Unit unit){
        enemyCount = 0;

        Units.nearbyEnemies(unit.team, unit.x, unit.y, range, other -> {
            // 这里只统计敌方团队单位，不把友军算进去。
            if(other.dead() || !other.isValid() || other.team == unit.team){
                return;
            }

            enemyCount++;
        });

        Units.nearbyBuildings(unit.x, unit.y, range, build -> {
            if(build.dead() || !build.isValid() || build.team == unit.team){
                return;
            }

            enemyCount++;
        });

        int effectiveEnemies = Math.min(enemyCount, maxEnemies);
        damageMultiplier = 1f + effectiveEnemies * damageBoostPerEnemy;
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(WHStats.format("wh-range", Strings.autoFixed(range / tilesize, 2) + " " + mindustry.world.meta.StatUnit.blocks.localized()));
        t.row();
        t.add(WHStats.format("wh-close-combat-max-count", maxEnemies));
        t.row();
        t.add(WHStats.format("wh-close-combat-damage", Strings.autoFixed(damageBoostPerEnemy * 100f, 2) + "%"));
    }

    @Override
    public CloseCombatAbility copy(){
        CloseCombatAbility out = (CloseCombatAbility)super.copy();
        out.timer = new Interval();
        out.enemyCount = 0;
        out.damageMultiplier = 1f;
        return out;
    }

    @Override
    public String localized(){
        return Core.bundle.get(getBundle());
    }

    @Override
    public String getBundle(){
        return "ability." + name("close-combat-ability");
    }
}
