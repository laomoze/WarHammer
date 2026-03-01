package wh.entities.event.logic;

import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.*;
import mindustry.logic.*;
import mindustry.world.*;

/**
 * NH-compat parser: randspawn <seed> <outX> <outY>
 */
public class RandomSpawn extends LStatement{
    public String seed = "0";
    public String x = "sx";
    public String y = "sy";

    public RandomSpawn(String[] tokens){
        if(tokens.length > 1) seed = tokens[1];
        if(tokens.length > 2) x = tokens[2];
        if(tokens.length > 3) y = tokens[3];
    }

    public RandomSpawn(){
    }

    @Override
    public void build(Table table){
        table.add("Seed: ");
        fields(table, seed, str -> seed = str);
        table.add(" out: ");
        fields(table, x, str -> x = str);
        table.add(", ");
        fields(table, y, str -> y = str);
    }

    @Override
    public boolean privileged(){
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder){
        return new RandomSpawnInstruction(builder.var(seed), builder.var(x), builder.var(y));
    }

    @Override
    public LCategory category(){
        return WHLogicStatements.autoTriggerCategory;
    }

    @Override
    public void write(StringBuilder builder){
        builder.append("randspawn").append(" ").append(seed).append(" ").append(x).append(" ").append(y);
    }

    public static class RandomSpawnInstruction implements LExecutor.LInstruction{
        public LVar seed;
        public LVar x;
        public LVar y;

        public RandomSpawnInstruction(LVar seed, LVar x, LVar y){
            this.seed = seed;
            this.x = x;
            this.y = y;
        }

        @Override
        public void run(LExecutor exec){
            if(Vars.spawner == null){
                x.setnum(0);
                y.setnum(0);
                return;
            }

            Seq<Tile> spawns = Vars.spawner.getSpawns();
            if(spawns == null || spawns.isEmpty()){
                x.setnum(0);
                y.setnum(0);
                return;
            }

            Rand rand = new Rand(seed.numi());
            Tile tile = spawns.random(rand);
            x.setnum(tile.x);
            y.setnum(tile.y);
        }
    }
}
