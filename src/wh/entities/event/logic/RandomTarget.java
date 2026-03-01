package wh.entities.event.logic;

import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.world.meta.*;

/**
 * NH-compat parser:
 * randtarget <team> <seed> <outX> <outY> <turretW> <generatorW> <factoryW> <coreW>
 */
public class RandomTarget extends LStatement{
    public String team = "@sharded";
    public String seed = "0";
    public String x = "tx";
    public String y = "ty";
    public String turretW = "0";
    public String generatorW = "0";
    public String factoryW = "0";
    public String coreW = "1";

    public RandomTarget(String[] tokens){
        if(tokens.length > 1) team = tokens[1];
        if(tokens.length > 2) seed = tokens[2];
        if(tokens.length > 3) x = tokens[3];
        if(tokens.length > 4) y = tokens[4];
        if(tokens.length > 5) turretW = tokens[5];
        if(tokens.length > 6) generatorW = tokens[6];
        if(tokens.length > 7) factoryW = tokens[7];
        if(tokens.length > 8) coreW = tokens[8];
    }

    public RandomTarget(){
    }

    @Override
    public void build(Table table){
        table.table(t -> {
            t.add("Team: ");
            fields(t, team, str -> team = str);
            t.add(" Seed: ");
            fields(t, seed, str -> seed = str);
            t.add(" out: ");
            fields(t, x, str -> x = str);
            t.add(", ");
            fields(t, y, str -> y = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add("Turret Weight: ");
            fields(t, turretW, str -> turretW = str);
            t.add(" Generator Weight: ");
            fields(t, generatorW, str -> generatorW = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add("Factory Weight: ");
            fields(t, factoryW, str -> factoryW = str);
            t.add(" Core Weight: ");
            fields(t, coreW, str -> coreW = str);
        }).left();
    }

    @Override
    public boolean privileged(){
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder){
        return new RandomTargetInstruction(
        builder.var(team), builder.var(seed), builder.var(x), builder.var(y),
        builder.var(turretW), builder.var(generatorW), builder.var(factoryW), builder.var(coreW)
        );
    }

    @Override
    public LCategory category(){
        return WHLogicStatements.autoTriggerCategory;
    }

    @Override
    public void write(StringBuilder builder){
        builder.append("randtarget").append(" ").append(team).append(" ").append(seed).append(" ")
        .append(x).append(" ").append(y).append(" ")
        .append(turretW).append(" ").append(generatorW).append(" ").append(factoryW).append(" ").append(coreW);
    }

    public static class RandomTargetInstruction implements LExecutor.LInstruction{
        public LVar team;
        public LVar seed;
        public LVar x;
        public LVar y;
        public LVar wTurret;
        public LVar wGenerator;
        public LVar wFactory;
        public LVar wCore;

        public RandomTargetInstruction(
        LVar team, LVar seed, LVar x, LVar y, LVar wTurret, LVar wGenerator, LVar wFactory, LVar wCore
        ){
            this.team = team;
            this.seed = seed;
            this.x = x;
            this.y = y;
            this.wTurret = wTurret;
            this.wGenerator = wGenerator;
            this.wFactory = wFactory;
            this.wCore = wCore;
        }

        @Override
        public void run(LExecutor exec){
            Team targetTeam = team.team();
            if(targetTeam == null || Vars.world == null){
                return;
            }

            Rand rand = new Rand(seed.numi());
            float wx = rand.random(0f, Vars.world.unitWidth());
            float wy = rand.random(0f, Vars.world.unitHeight());

            BlockFlag flag = chooseFlag(rand, wTurret.numf(), wGenerator.numf(), wFactory.numf(), wCore.numf());
            Building building = Geometry.findClosest(wx, wy, Vars.indexer.getEnemy(targetTeam, flag));
            if(building == null){
                building = targetTeam.core();
            }
            if(building == null){
                return;
            }

            x.setnum(building.tileX());
            y.setnum(building.tileY());
        }

        private BlockFlag chooseFlag(Rand rand, float turret, float generator, float factory, float core){
            float wt = Math.max(0f, turret);
            float wg = Math.max(0f, generator);
            float wf = Math.max(0f, factory);
            float wc = Math.max(0f, core);
            float sum = wt + wg + wf + wc;
            if(sum <= 0.0001f) return BlockFlag.core;

            float value = rand.random(sum);
            if((value -= wt) <= 0f) return BlockFlag.turret;
            if((value -= wg) <= 0f) return BlockFlag.generator;
            if((value -= wf) <= 0f) return BlockFlag.factory;
            return BlockFlag.core;
        }
    }
}
