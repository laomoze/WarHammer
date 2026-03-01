package wh.entities.event.logic;

import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.logic.*;
import mindustry.world.*;

/**
 * NH-compat parser: linetarget <team> <sx> <sy> <tx> <ty> <outX> <outY>
 */
public class LineTarget extends LStatement{
    public String team = "@sharded";
    public String sourceX = "0";
    public String sourceY = "0";
    public String targetX = "0";
    public String targetY = "0";
    public String outX = "0";
    public String outY = "0";

    public LineTarget(String[] tokens){
        if(tokens.length > 1) team = tokens[1];
        if(tokens.length > 2) sourceX = tokens[2];
        if(tokens.length > 3) sourceY = tokens[3];
        if(tokens.length > 4) targetX = tokens[4];
        if(tokens.length > 5) targetY = tokens[5];
        if(tokens.length > 6) outX = tokens[6];
        if(tokens.length > 7) outY = tokens[7];
    }

    public LineTarget(){
    }

    @Override
    public void build(Table table){
        table.table(t -> {
            t.add("Target Team: ");
            fields(t, team, str -> team = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add("Source Position: ");
            fields(t, sourceX, str -> sourceX = str);
            t.add(", ");
            fields(t, sourceY, str -> sourceY = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add("End Position: ");
            fields(t, targetX, str -> targetX = str);
            t.add(", ");
            fields(t, targetY, str -> targetY = str);
        }).left();

        table.row();

        table.table(t -> {
            t.add("Out Position: ");
            fields(t, outX, str -> outX = str);
            t.add(", ");
            fields(t, outY, str -> outY = str);
        }).left();
    }

    @Override
    public boolean privileged(){
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder){
        return new LineTargetInstruction(
        builder.var(team), builder.var(sourceX), builder.var(sourceY), builder.var(targetX), builder.var(targetY),
        builder.var(outX), builder.var(outY)
        );
    }

    @Override
    public LCategory category(){
        return WHLogicStatements.autoTriggerCategory;
    }

    @Override
    public void write(StringBuilder builder){
        builder.append("linetarget").append(" ").append(team).append(" ")
        .append(sourceX).append(" ").append(sourceY).append(" ")
        .append(targetX).append(" ").append(targetY).append(" ")
        .append(outX).append(" ").append(outY);
    }

    public static class LineTargetInstruction implements LExecutor.LInstruction{
        public LVar team;
        public LVar sourceX;
        public LVar sourceY;
        public LVar targetX;
        public LVar targetY;
        public LVar outX;
        public LVar outY;

        public LineTargetInstruction(LVar team, LVar sourceX, LVar sourceY, LVar targetX, LVar targetY, LVar outX, LVar outY){
            this.team = team;
            this.sourceX = sourceX;
            this.sourceY = sourceY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.outX = outX;
            this.outY = outY;
        }

        @Override
        public void run(LExecutor exec){
            Team t = team.team();
            if(t == null || Vars.world == null){
                outX.setnum(-1);
                outY.setnum(-1);
                return;
            }

            int sx = sourceX.numi();
            int sy = sourceY.numi();
            int tx = targetX.numi();
            int ty = targetY.numi();

            Seq<Tile> tiles = new Seq<>();
            Bresenham2.line(sx, sy, tx, ty, (x, y) -> {
                Tile tile = Vars.world.tile(x, y);
                if(tile != null){
                    tiles.add(tile);
                }
            });

            int ox = -1;
            int oy = -1;
            for(Tile tile : tiles){
                if(tile.team() == t){
                    ox = tile.x;
                    oy = tile.y;
                    break;
                }
            }

            outX.setnum(ox * 8);
            outY.setnum(oy * 8);
        }
    }
}
