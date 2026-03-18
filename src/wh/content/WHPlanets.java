package wh.content;

import arc.graphics.*;
import mindustry.content.*;
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

            meshLoader = () -> new HexMesh(this, 7);

            // Visible but not overblown halo.
            cloudMeshLoader = () -> new MultiMesh(
            new HexSkyMesh(this, 6, 0.20f, 0.13f, 5, chromite.cpy().lerp(radiation, 0.18f).a(0.09f), 2, 0.45f, 1.00f, 0.36f),
            new HexSkyMesh(this, 2, 0.44f, 0.14f, 5, Color.valueOf("a7bfd9").a(0.11f), 2, 0.45f, 1.08f, 0.38f),
            new HexSkyMesh(this, 1, 0.60f, 0.16f, 5, Color.valueOf("c8d8e6").a(0.10f), 2, 0.45f, 1.15f, 0.40f)
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

            // Dimmer atmosphere to avoid washed-out planet preview.
            atmosphereRadIn = 0.015f;
            atmosphereRadOut = 0.20f;
            atmosphereColor = Color.valueOf("8fa4bb");
            landCloudColor = Color.valueOf("96a9be").a(0.16f);

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