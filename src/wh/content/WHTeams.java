package wh.content;

import arc.graphics.Color;
import mindustry.game.Team;

public class WHTeams extends Team {
    protected WHTeams(int id, String name, Color color) {
        super(id, name, color);
    }

    public static WHTeams mankind, chaos;

    public static void load() {
        mankind = new WHTeams(7, "team-mankind", Color.valueOf("FFB375FF"));
        chaos = new WHTeams(8, "team-chaos", Color.valueOf("BF3F2EFF"));
    }
}
