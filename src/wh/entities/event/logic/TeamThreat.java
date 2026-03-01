package wh.entities.event.logic;

import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.logic.*;

/**
 * NH-compat parser: teamthreat <team> <outThreat>
 */
public class TeamThreat extends LStatement{
    public String team = "@sharded";
    public String threat = "0";

    public TeamThreat(String[] tokens){
        if(tokens.length > 1) team = tokens[1];
        if(tokens.length > 2) threat = tokens[2];
    }

    public TeamThreat(){
    }

    @Override
    public void build(Table table){
        table.add("Team: ");
        fields(table, team, str -> team = str);
        table.add(" Out Threat: ");
        fields(table, threat, str -> threat = str);
    }

    @Override
    public boolean privileged(){
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder){
        return new TeamThreatInstruction(builder.var(team), builder.var(threat));
    }

    @Override
    public LCategory category(){
        return WHLogicStatements.autoTriggerCategory;
    }

    @Override
    public void write(StringBuilder builder){
        builder.append("teamthreat").append(" ").append(team).append(" ").append(threat);
    }

    public static class TeamThreatInstruction implements LExecutor.LInstruction{
        public LVar team;
        public LVar threat;

        public TeamThreatInstruction(LVar team, LVar threat){
            this.team = team;
            this.threat = threat;
        }

        @Override
        public void run(LExecutor exec){
            Team t = team.team();
            if(t == null || Vars.state == null || Vars.state.rules == null){
                threat.setnum(0f);
                return;
            }

            float unitCount = t.data() == null ? 0f : t.data().units.size;
            float coreCount = t.cores() == null ? 0f : t.cores().size;
            float buildCount = Groups.build.count(build -> build.team == t);

            // A simple, stable scalar for logic scripts that expect "threat" output.
            float score = unitCount + coreCount * 5f + buildCount * 0.05f;
            threat.setnum(Math.max(1f, score));
        }
    }
}
