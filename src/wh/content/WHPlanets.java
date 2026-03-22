package wh.content;

import arc.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import mindustry.world.*;
import wh.pipelinePlanet.karvex.*;

import static mindustry.content.Planets.sun;

public final class WHPlanets{
    public static Planet karvex;

    public static void load(){
        karvex = new Planet("karvex", sun, 1f, 2){{
            generator = new KarvexPlanetGenerator();

            Color radiation = colorOf(WHBlocksEnvironment.radiationWater, "4f8d86");
            Color promethium = colorOf(WHBlocksEnvironment.promethiumSand, "7a5b3a");
            Color darkRock = colorOf(WHBlocksEnvironment.darkRock, "3a3f48");
            Color chromite = colorOf(WHBlocksEnvironment.chromiteStone, "59616d");
            Color toxicYellow = Color.valueOf("d8c86e");
            Color toxicGreen = Color.valueOf("8da85f");
            Color hazeYellow = Color.valueOf("e2d882");

            meshLoader = () -> new HexMesh(this, 7);

            // Keep atmosphere/cloud layers clearly visible in planet view.
            cloudMeshLoader = () -> new MultiMesh(
            new HexSkyMesh(this, 6, 0.20f, 0.13f, 5, chromite.cpy().lerp(radiation, 0.24f).lerp(toxicGreen, 0.34f).a(0.30f), 2, 0.45f, 1.00f, 0.36f),
            new HexSkyMesh(this, 2, 0.44f, 0.14f, 5, toxicYellow.cpy().lerp(toxicGreen, 0.26f).a(0.34f), 2, 0.45f, 1.08f, 0.38f),
            new HexSkyMesh(this, 1, 0.60f, 0.16f, 5, hazeYellow.cpy().lerp(toxicGreen, 0.18f).a(0.30f), 2, 0.45f, 1.15f, 0.40f)
            );

            iconColor = darkRock.cpy().lerp(chromite, 0.25f).lerp(promethium, 0.08f);

            visible = true;
            tidalLock = false;
            accessible = true;
            alwaysUnlocked = true;
            allowLaunchLoadout = false;
            clearSectorOnLose = true;
            startSector = 15;
            allowLaunchSchematics = false;
            orbitTime = 210 * 60;

            hasAtmosphere = true;
            atmosphereRadIn = 0.028f;
            atmosphereRadOut = 0.34f;
            atmosphereColor = Color.valueOf("c9cf63");
            landCloudColor = Color.valueOf("a7c166").a(0.42f);

            clipRadius = 6.8f;
            camRadius = 0.28f;
            minZoom = 0.2f;
            maxZoom = 2.6f;
            allowWaves = false;
            allowSectorInvasion = false;
        }};
    }

    private static Color colorOf(Block block, String fallback){
        if(block == null) return Color.valueOf(fallback);
        Color color = block.mapColor;
        if(color == null) return Color.valueOf(fallback);
        if(color.r <= 0.01f && color.g <= 0.01f && color.b <= 0.01f){
            return Color.valueOf(fallback);
        }
        return color.cpy().a(1f);
    }
}
