package wh.content;

import mindustry.ai.UnitCommand;

public final class WHUnitCommands {
    public static UnitCommand deploy;

    private WHUnitCommands() {
    }

    public static void load() {
        deploy = new UnitCommand("deploy", "hammer", null) {{
            switchToMove = false;
            resetTarget = false;
        }};
    }
}
