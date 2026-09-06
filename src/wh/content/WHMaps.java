package wh.content;

import mindustry.type.SectorPreset;

public class WHMaps {
    public static SectorPreset abandonedWarehouse, rustedRiverbed, reservoirPass, sewageStation;

    public static void load() {
        abandonedWarehouse = new SectorPreset("1-1", WHPlanets.karvex, 15) {{
            alwaysUnlocked = true;
            difficulty = 1;
            captureWave = 16;
        }};

        rustedRiverbed = new SectorPreset("1-2", WHPlanets.karvex, 175) {{
            difficulty = 2;
            captureWave = 22;
        }};

        reservoirPass = new SectorPreset("1-3", WHPlanets.karvex, 219) {{
            difficulty = 2;
        }};

        sewageStation = new SectorPreset("1-4", WHPlanets.karvex, 220) {{
            difficulty = 3;
            captureWave = 37;
        }};
    }
}
