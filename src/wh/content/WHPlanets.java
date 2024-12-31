package wh.content;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import wh.maps.*;
import wh.maps.ColorPass.*;
import wh.maps.HeightPass.*;
import wh.maps.planets.*;
import wh.type.*;

import static mindustry.content.Planets.*;

@SuppressWarnings("unused")
public final class WHPlanets {
    public static Planet kellex;

    public static void load() {
        kellex = new BetterPlanet("kellex", sun, 1, 2) {{
            generator = new KellexPlanetGenerator() {{
                baseHeight = 0f;
                baseColor = Blocks.stone.mapColor;
                heights.add(new NoiseHeight() {{
                    offset.set(1000, 0, 0);
                    octaves = 8;
                    persistence = 0.55;
                    magnitude = 1.15f;
                    heightOffset = -0.5f;
                }});
                heights.add(new ClampHeight(0f, 0.95f));
                Mathf.rand.setSeed(8);
                Seq<HeightPass> mountains = new Seq<>();
                for (int i = 0; i < 3; i++) {
                    mountains.add(new DotHeight() {{
                        dir.setToRandomDirection().y = Mathf.random(1f, 5f);
                        min = -1f;
                        max = 1f;
                        magnitude = 0.13f;
                        interp = Interp.exp10In;
                    }});
                }
                heights.add(new MultiHeight(mountains, MultiHeight.MixType.max, MultiHeight.Operation.add));
                heights.add(new ClampHeight(0f, 0.75f));
                heights.add(new ClampHeight(0f, 0.76f));
                colors.addAll(
                        new NoiseColorPass() {{
                            scale = 1.5;
                            persistence = 0.5;
                            octaves = 3;
                            magnitude = 1.2f;
                            min = 0f;
                            max = 0.5f;
                            out = Blocks.grass.mapColor;
                            offset.set(1500f, 300f, -500f);
                        }},
                        new NoiseColorPass() {{
                            seed = 5;
                            scale = 1.5;
                            persistence = 0.3;
                            octaves = 5;
                            magnitude = 1.2f;
                            min = 0.1f;
                            max = 0.4f;
                            out = Blocks.stone.mapColor;
                            offset.set(1500f, 300f, -500f);
                        }},
                        new NoiseColorPass() {{
                            seed = 5;
                            scale = 1.5;
                            persistence = 0.3;
                            octaves = 5;
                            magnitude = 0.95f;
                            min = 0.1f;
                            max = 0.4f;
                            out = Blocks.stone.mapColor;
                            offset.set(1500f, 300f, -500f);
                        }},
                        new NoiseColorPass() {{
                            seed = 3;
                            scale = 1.5;
                            persistence = 0.5;
                            octaves = 7;
                            magnitude = 1.2f;
                            min = 0.1f;
                            max = 0.4f;
                            out = Blocks.stone.mapColor;
                            offset.set(1500f, 300f, -500f);
                        }},
                        new NoiseColorPass() {{
                            seed = 5;
                            scale = 1.5;
                            persistence = 0.5;
                            octaves = 7;
                            magnitude = 1.2f;
                            min = 0.1f;
                            max = 0.4f;
                            out = Blocks.stone.mapColor;
                            offset.set(1500f, 300f, -500f);
                        }},
                        new NoiseColorPass() {{
                            seed = 8;
                            scale = 1.5;
                            persistence = 0.5;
                            octaves = 7;
                            magnitude = 1.2f;
                            min = 0.1f;
                            max = 0.4f;
                            out = Blocks.water.mapColor;
                            offset.set(1500f, 300f, -500f);
                        }}
                );
                for(int i = 0; i < 5; i++) {
                    colors.add(new SphereColorPass(new Vec3().setToRandomDirection(), 0.06f, Blocks.stone.mapColor));
                }
                colors.add(
                        new FlatColorPass() {{
                            min = 0;
                            max = 0.07f;
                            out = Blocks.water.mapColor;
                        }},
                        new FlatColorPass() {{
                            min = 0.3f;
                            max = 0.5f;
                            out = Blocks.stone.mapColor;
                        }},
                        new FlatColorPass() {{
                            max = 1f;
                            min = 0.52f;
                            out = Blocks.ice.mapColor;
                        }}
                );
            }};
            meshLoader = () -> new MultiMesh(
                    new NoiseMesh(this, 0, 5, 0.94f, 1, 0.0001f, 0.0001f, 1f, Color.valueOf("404066FF"), Color.valueOf("404066FF"), 1, 1, 1, 1),
                    new NoiseMesh(this, 0, 6, 0.94f, 4, 0.9f, 0.7f, 1f, Color.valueOf("B17C4EFF"), Color.valueOf("3E3E4AFF"), 1, 1, 1.8f, 1),
                    new NoiseMesh(this, 0, 6, 0.895f, 4, 0.9f, 0.7f, 1.5f, Color.valueOf("505D4DFF"), Color.valueOf("3B5029FF"), 4, 1, 1.8f, 1),
                    new NoiseMesh(this, 0, 6, 0.83f, 4, 0.9f, 0.7f, 2.2f, Color.valueOf("CCC7C2FF"), Color.valueOf("525252"), 4, 1, 0.7f, 1),
                    new NoiseMesh(this, 0, 6, 0.74f, 4, 0.9f, 0.7f, 3.3f, Color.valueOf("B5AB94FF"), Color.valueOf("D4B38CFF"), 4, 1, 1.8f, 1));
            cloudMeshLoader = () -> new MultiMesh(
                    new HexSkyMesh(this, 0, 3, 0.1f, 6, Color.valueOf("FFF2DD88"), 3, 0.3f, 1f, 0.43f),
                    new HexSkyMesh(this, 0, 2, 0.11f, 6, Color.valueOf("FADDB1BB"), 3, 0.5f, 0.9f, 0.43f),
                    new HexSkyMesh(this, 0, -2, 0.034f, 5, Color.valueOf("403673FF"), 1, 0.2f, 0.2f, 0.4f)
            );
            visible = true;
            tidalLock = false;
            accessible = true;
            alwaysUnlocked = true;
            allowLaunchLoadout = false;
            clearSectorOnLose = true;
            bloom = true;
            startSector = 15;
            allowLaunchSchematics = false;
            orbitRadius = 50;
            orbitTime = 240 * 60;
            rotateTime = 1 * 60f;
            atmosphereRadIn = 0.05f;
            atmosphereRadOut = 0.5f;
            allowWaves = false;
            allowWaveSimulation = false;
            allowSectorInvasion = false;
        }};
    }
}
