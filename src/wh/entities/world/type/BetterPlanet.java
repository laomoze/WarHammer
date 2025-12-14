package wh.entities.world.type;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Gl;
import arc.graphics.Mesh;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g3d.Camera3D;
import arc.graphics.gl.FrameBuffer;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.util.Nullable;
import arc.util.Tmp;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.GenericMesh;
import mindustry.graphics.g3d.HexMesher;
import mindustry.graphics.g3d.MeshBuilder;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;
import wh.graphics.WHShaders;

import static mindustry.Vars.headless;
import static mindustry.Vars.renderer;

/**
 * Just a regular planet, but with a fixed atmosphere shader at the little cost of performance.
 * @author Eipusino
 * @since 1.0.6
 */
public class BetterPlanet extends Planet{
    public @Nullable FrameBuffer buffer;

    public BetterPlanet(String name, Planet parent, float radius){
        super(name, parent, radius);
    }

    public BetterPlanet(String name, Planet parent, float radius, int sectorSize){
        super(name, parent, radius, sectorSize);
    }

    @Override
    public void load(){
        if(!headless){
            mesh = meshLoader.get();
            cloudMesh = cloudMeshLoader.get();
            if(grid != null) gridMesh = gridMeshLoader.get();

            buffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight(), true);
            buffer.getTexture().setFilter(TextureFilter.nearest);
        }
    }


    @Override
    public void drawAtmosphere(Mesh atmosphere, Camera3D cam){
        Gl.depthMask(false);
        Blending.additive.apply();

        var shader = WHShaders.depthAtmosphere;
        shader.camera = cam;
        shader.planet = this;
        shader.bind();
        shader.apply();
        atmosphere.render(shader, Gl.triangles);

        Blending.normal.apply();
        Gl.depthMask(true);
    }

    public class AtmosphereHexMesh implements GenericMesh{
        protected Mesh mesh;

        public AtmosphereHexMesh(HexMesher mesher, int divisions){
            mesh = MeshBuilder.buildHex(mesher, divisions, radius, 0.2f);
        }

        public AtmosphereHexMesh(int divisions){
            this(generator, divisions);
        }

        @Override
        public void render(PlanetParams params, Mat3D projection, Mat3D transform){
            if(params.alwaysDrawAtmosphere || Core.settings.getBool("atmosphere")){
                var depth = WHShaders.depth;
                buffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
                buffer.begin(Tmp.c1.set(0xffffff00));
                Blending.disabled.apply();

                depth.camera = renderer.planets.cam;
                depth.bind();
                depth.setUniformMatrix4("u_proj", projection.val);
                depth.setUniformMatrix4("u_trans", transform.val);
                depth.apply();
                mesh.render(depth, Gl.triangles);

                Blending.normal.apply();
                buffer.end();
            }

            var shader = Shaders.planet;
            shader.planet = BetterPlanet.this;
            shader.lightDir.set(solarSystem.position).sub(position).rotate(Vec3.Y, getRotation()).nor();
            shader.ambientColor.set(solarSystem.lightColor);
            shader.bind();
            shader.setUniformMatrix4("u_proj", projection.val);
            shader.setUniformMatrix4("u_trans", transform.val);
            shader.apply();
            mesh.render(shader, Gl.triangles);
        }

        @Override
        public void dispose(){
            mesh.dispose();
        }
    }
}