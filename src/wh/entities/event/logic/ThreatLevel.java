package wh.entities.event.logic;

import arc.math.*;
import mindustry.game.*;

/**
 * Team threat estimation used by raid logic.
 * The score is simple and predictable:
 * - unit count
 * - core count * coreWeight
 */
public final class ThreatLevel{
    private static final float coreWeight = 1.5f;
    private static final float unitWeight = 0.75f;

    private ThreatLevel(){
    }

    public static int getTeamThreat(Team team){
        if(team == null) return 1;

        float unitCount = team.data() == null ? 0f : team.data().units.size;
        float coreCount = team.cores() == null ? 0f : team.cores().size;
        float score = Math.max(1f, unitCount * unitWeight + coreCount * coreWeight);
        return Mathf.clamp(Math.max(1, Mathf.round(score)), 1, 10);
    }

    public static float getThreatScale(Team team){
        return Mathf.sqrt(getTeamThreat(team));
    }
}
