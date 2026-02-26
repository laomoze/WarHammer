package wh.entities.event.logic;

import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.*;
import wh.content.*;
import wh.entities.*;

public class WHRaidUnitStatement extends LStatement{
    // trigger flag from `setflag "<flag>" true`
    public String flag = "raid-executor";
    public String alertTime = "10";
    public String raidTime = "5";
    public String team = "@crux";
    // unit id: 0/invalid = fallback WHUnitTypes.air4
    public String unit = "0";
    public String count = "8";
    // spawn spread in tiles around spawn point
    public String spread = "8";
    // extra conditions
    public String checkTeam = "@sharded";
    public String minWave = "0";
    public String needItem = "-1";
    public String needItemAmount = "0";
    public String needBlock = "-1";
    public String needBlockAmount = "0";

    public WHRaidUnitStatement(){
    }

    public WHRaidUnitStatement(String[] tokens){
        if(tokens.length > 1) flag = tokens[1];
        if(tokens.length > 2) alertTime = tokens[2];
        if(tokens.length > 3) raidTime = tokens[3];
        if(tokens.length > 4) team = tokens[4];
        if(tokens.length > 5) unit = tokens[5];
        if(tokens.length > 6) count = tokens[6];
        if(tokens.length > 7) spread = tokens[7];
        if(tokens.length > 8) checkTeam = tokens[8];
        if(tokens.length > 9) minWave = tokens[9];
        if(tokens.length > 10) needItem = tokens[10];
        if(tokens.length > 11) needItemAmount = tokens[11];
        if(tokens.length > 12) needBlock = tokens[12];
        if(tokens.length > 13) needBlockAmount = tokens[13];
    }

    @Override
    public void build(Table table){
        float width = 240f;

        table.table(t -> {
            t.add("Trigger Flag: ");
            fields(t, flag, value -> flag = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Alert Time(s): ");
            fields(t, alertTime, value -> alertTime = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Raid Time(s): ");
            fields(t, raidTime, value -> raidTime = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Spawn Team: ");
            fields(t, team, value -> team = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Unit Id: ");
            fields(t, unit, value -> unit = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Raid Count: ");
            fields(t, count, value -> count = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Spread(tiles): ");
            fields(t, spread, value -> spread = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Check Team: ");
            fields(t, checkTeam, value -> checkTeam = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Min Wave: ");
            fields(t, minWave, value -> minWave = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Need Item Id: ");
            fields(t, needItem, value -> needItem = value).width(110f);
            t.add(" Amount: ");
            fields(t, needItemAmount, value -> needItemAmount = value).width(110f);
        }).left().row();

        table.table(t -> {
            t.add("Need Block Id: ");
            fields(t, needBlock, value -> needBlock = value).width(110f);
            t.add(" Amount: ");
            fields(t, needBlockAmount, value -> needBlockAmount = value).width(110f);
        }).left();
    }

    @Override
    public LCategory category(){
        return WHLogicStatements.autoTriggerCategory;
    }

    @Override
    public boolean privileged(){
        return true;
    }

    @Override
    public void write(StringBuilder builder){
        builder.append("wh-raid-unit ");
        builder.append(flag).append(" ");
        builder.append(alertTime).append(" ");
        builder.append(raidTime).append(" ");
        builder.append(team).append(" ");
        builder.append(unit).append(" ");
        builder.append(count).append(" ");
        builder.append(spread).append(" ");
        builder.append(checkTeam).append(" ");
        builder.append(minWave).append(" ");
        builder.append(needItem).append(" ");
        builder.append(needItemAmount).append(" ");
        builder.append(needBlock).append(" ");
        builder.append(needBlockAmount);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder){
        return new WHRaidUnitInstruction(
        builder.var(flag),
        builder.var(alertTime),
        builder.var(raidTime),
        builder.var(team),
        builder.var(unit),
        builder.var(count),
        builder.var(spread),
        builder.var(checkTeam),
        builder.var(minWave),
        builder.var(needItem),
        builder.var(needItemAmount),
        builder.var(needBlock),
        builder.var(needBlockAmount)
        );
    }

    public static class WHRaidUnitInstruction implements LExecutor.LInstruction{
        public LVar flag;
        public LVar alertTime;
        public LVar raidTime;
        public LVar team;
        public LVar unit;
        public LVar count;
        public LVar spread;
        public LVar checkTeam;
        public LVar minWave;
        public LVar needItem;
        public LVar needItemAmount;
        public LVar needBlock;
        public LVar needBlockAmount;

        public int raidCounter = 0;
        public float curTime = 0f;

        private final Vec2 pos = new Vec2();
        private final Vec2 target = new Vec2();

        public WHRaidUnitInstruction(
        LVar flag, LVar alertTime, LVar raidTime, LVar team, LVar unit, LVar count, LVar spread,
        LVar checkTeam, LVar minWave, LVar needItem, LVar needItemAmount, LVar needBlock, LVar needBlockAmount
        ){
            this.flag = flag;
            this.alertTime = alertTime;
            this.raidTime = raidTime;
            this.team = team;
            this.unit = unit;
            this.count = count;
            this.spread = spread;
            this.checkTeam = checkTeam;
            this.minWave = minWave;
            this.needItem = needItem;
            this.needItemAmount = needItemAmount;
            this.needBlock = needBlock;
            this.needBlockAmount = needBlockAmount;
        }

        @Override
        public void run(LExecutor exec){
            if(Vars.net.client() || Vars.state == null || Vars.state.rules == null || Vars.world == null){
                return;
            }

            if(!Vars.state.rules.objectiveFlags.contains(flag.name)){
                exec.counter.numval--;
                exec.yield = true;
                return;
            }

            if(curTime <= 0.0001f && !meetConditions()){
                exec.counter.numval--;
                exec.yield = true;
                return;
            }

            float alert = Math.max(0f, alertTime.numf());
            float raid = Math.max(0.001f, raidTime.numf());
            float total = alert + raid;

            if(curTime >= total){
                reset();
                return;
            }

            exec.counter.numval--;
            exec.yield = true;
            curTime += Time.delta / 60f;

            if(curTime <= alert) return;

            int totalCount = Math.max(0, count.numi());
            float raidTimer = curTime - alert;
            int raidCount = Mathf.round((raidTimer / raid) * totalCount);
            int delta = Math.max(0, raidCount - raidCounter);
            raidCounter = raidCount;

            for(int i = 0; i < delta; i++){
                spawnOne();
            }
        }

        private void reset(){
            curTime = 0f;
            raidCounter = 0;
            Vars.state.rules.objectiveFlags.remove(flag.name);
        }

        private boolean meetConditions(){
            Team conditionTeam = resolveConditionTeam();

            int minWaveRequired = Math.max(0, minWave.numi());
            if(minWaveRequired > 0 && Vars.state.wave < minWaveRequired){
                return false;
            }

            int itemAmountRequired = Math.max(0, needItemAmount.numi());
            if(itemAmountRequired > 0){
                Item item = resolveItem(needItem.numi());
                Building core = conditionTeam.core();
                if(item == null || core == null || core.items == null){
                    return false;
                }
                if(core.items.get(item) < itemAmountRequired){
                    return false;
                }
            }

            int blockAmountRequired = Math.max(0, needBlockAmount.numi());
            if(blockAmountRequired > 0){
                Block block = resolveBlock(needBlock.numi());
                if(block == null){
                    return false;
                }

                int[] amount = {0};
                Groups.build.each(build -> {
                    if(build.team == conditionTeam && build.block == block){
                        amount[0]++;
                    }
                });

                if(amount[0] < blockAmountRequired){
                    return false;
                }
            }

            return true;
        }

        private void spawnOne(){
            UnitType unitType = resolveUnitType(unit.numi());
            Team spawnTeam = resolveTeam();
            float spreadWorld = Math.max(0f, spread.numf()) * Vars.tilesize;

            if(!pickSpawnPosition(spreadWorld, pos)){
                return;
            }
            resolveTarget(target, spawnTeam);

            float rotation = Angles.angle(pos.x, pos.y, target.x, target.y);

            new Spawner()
            .init(unitType, spawnTeam, pos, rotation, 12f, false)
            .add();
        }

        private UnitType resolveUnitType(int rawId){
            if(rawId >= 10000){
                UnitType mapped = Vars.content.unit(rawId - 10000);
                if(mapped != null) return mapped;
            }

            UnitType unitById = Vars.content.unit(rawId);
            if(unitById != null) return unitById;

            return WHUnitTypes.air4;
        }

        private Team resolveTeam(){
            Team resolved = team.team();
            if(resolved != null) return resolved;
            return Vars.state.rules.waveTeam;
        }

        private Team resolveConditionTeam(){
            Team resolved = checkTeam.team();
            if(resolved != null) return resolved;
            return Vars.state.rules.defaultTeam;
        }

        private Item resolveItem(int rawId){
            if(rawId >= 10000){
                Item mapped = Vars.content.item(rawId - 10000);
                if(mapped != null) return mapped;
            }
            return Vars.content.item(rawId);
        }

        private Block resolveBlock(int rawId){
            if(rawId >= 10000){
                Block mapped = Vars.content.block(rawId - 10000);
                if(mapped != null) return mapped;
            }
            return Vars.content.block(rawId);
        }

        private boolean pickSpawnPosition(float spreadWorld, Vec2 out){
            if(Vars.spawner != null && Vars.state.hasSpawns()){
                Seq<Tile> spawns = Vars.spawner.getSpawns();
                if(spawns != null && !spawns.isEmpty()){
                    Tile tile = spawns.random();
                    out.set(tile.worldx(), tile.worldy());
                    out.x += Mathf.range(spreadWorld);
                    out.y += Mathf.range(spreadWorld);
                    return true;
                }
            }
            return false;
        }

        private void resolveTarget(Vec2 out, Team spawnTeam){
            Team enemy = spawnTeam == Vars.state.rules.defaultTeam ? Vars.state.rules.waveTeam : Vars.state.rules.defaultTeam;
            Building core = enemy.core();
            if(core != null){
                out.set(core.x, core.y);
            }else{
                out.set(Vars.world.width() * Vars.tilesize * 0.5f, Vars.world.height() * Vars.tilesize * 0.5f);
            }
        }
    }
}
