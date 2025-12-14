//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.graphics;

import arc.Core;
import arc.files.Fi;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.Camera3D;
import arc.graphics.gl.Shader;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.graphics.Shaders.*;
import wh.WHVars;
import wh.entities.world.type.BetterPlanet;

import static arc.Core.files;
import static mindustry.Vars.*;

public class WHShaders{
    public static DepthShader depth;
    public static DepthAtmosphereShader depthAtmosphere;
    public static @Nullable HexagonalTextureShieldShader HexagonalShield;
    public static OutlineShader powerArea, powerDynamicArea;

    private WHShaders(){
    }

    public static void init(){
        depth = new DepthShader();
        depthAtmosphere = new DepthAtmosphereShader();
        powerArea = new OutlineShader() {
            @Override
            public float thick() {
                return 2f;
            }
        };
        powerDynamicArea = new OutlineShader() {
            public float thick() {
                return 2f * Interp.slope.apply(Time.time / 240f % 1f);
            }
        };
        try{
            HexagonalShield = new HexagonalTextureShieldShader();
        }catch(Throwable t){
            //don't load shield shader
            HexagonalShield = null;
            t.printStackTrace();
        }
    }


    public static Fi df(String name){
        return Vars.tree.get("shaders/" + name + ".frag");
    }

    public static Fi dv(String name){
        return Vars.tree.get("shaders/" + name + ".vert");
    }

    public static Fi mf(String name){
        return WHVars.internalTree.child("shaders/" + name + ".frag");
    }

    public static Fi mv(String name){
        return WHVars.internalTree.child("shaders/" + name + ".vert");
    }


    public static class OutlineShader extends LoadShader {
        public OutlineShader() {
            super("outliner", "screenspace");
        }

        @Override
        public void apply() {
            setUniformf("u_offset",
            Core.camera.position.x - Core.camera.width / 2,
            Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_dp", Scl.scl(1f));
            setUniformf("u_thick", thick());
            setUniformf("u_time", Time.time / Scl.scl(1f));
            setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
            setUniformf("u_texsize", Core.camera.width, Core.camera.height);
        }

        public float thick() {
            return 1f;
        }
    }

    public static class HexagonalTextureShieldShader extends LoadShader{
        public HexagonalTextureShieldShader(){
            super("hexagonalShield", "screenspace");
        }

        @Override
        public void apply(){
            setUniformf("u_dp", Scl.scl(1f));
            setUniformf("u_time", Time.time / Scl.scl(1f));

            setUniformf("u_offset",
            Core.camera.position.x - Core.camera.width / 2,
            Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_texsize", Core.camera.width, Core.camera.height);
            setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
        }
    }

    public static class VoidShield extends LoadShader{
        public VoidShield(){
            super("voidShield", "screenspace");
        }

        @Override
        public void apply(){
            setUniformf("u_dp", Scl.scl(1f));
            setUniformf("u_time", Time.time / Scl.scl(1f));
            setUniformf("u_offset",
            Core.camera.position.x - Core.camera.width / 2,
            Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_texsize", Core.camera.width, Core.camera.height);
            setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
        }
    }


    //这个想想还是删了吧
    public static final class DepthShader extends Shader{
        public Camera3D camera;

        private DepthShader(){
            super(WHShaders.mv("depth"), WHShaders.mf("depth"));
        }

        public void apply(){
            this.setUniformf("u_camPos", this.camera.position);
            this.setUniformf("u_camRange", this.camera.near, this.camera.far - this.camera.near);
        }
    }

    public static class DepthAtmosphereShader extends Shader{
        private static final Mat3D mat = new Mat3D();

        public Camera3D camera;
        public BetterPlanet planet;

        /**
         * The only instance of this class: {@link #depthAtmosphere}.
         */
        private DepthAtmosphereShader(){
            super(WHShaders.mv("depth-atmosphere"), WHShaders.mf("depth-atmosphere"));
        }


        @Override
        public void apply(){
            setUniformMatrix4("u_proj", camera.combined.val);
            setUniformMatrix4("u_trans", planet.getTransform(mat).val);

            setUniformf("u_camPos", camera.position);
            setUniformf("u_relCamPos", Tmp.v31.set(camera.position).sub(planet.position));
            setUniformf("u_camRange", camera.near, camera.far - camera.near);
            setUniformf("u_center", planet.position);
            setUniformf("u_light", planet.getLightNormal());
            setUniformf("u_color", planet.atmosphereColor.r, planet.atmosphereColor.g, planet.atmosphereColor.b);

            setUniformf("u_innerRadius", planet.radius + planet.atmosphereRadIn);
            setUniformf("u_outerRadius", planet.radius + planet.atmosphereRadOut);

            planet.buffer.getTexture().bind(0);
            setUniformi("u_topology", 0);
            setUniformf("u_viewport", Core.graphics.getWidth(), Core.graphics.getHeight());
        }
    }


    public static int MaxCont = 4;
    public static HoleShader holeShader;

    public static void createHoleShader(){
        if(MaxCont >= 512) return;

        MaxCont = Math.min(MaxCont * 2, 512);
        if(holeShader != null) holeShader.dispose();
        Shader.prependFragmentCode = "#define MAX_COUNT " + MaxCont + "\n";
        holeShader = new HoleShader();
        Shader.prependFragmentCode = "";
    }

    public static class HoleShader extends Shader{
        public float[] blackHoles;

        public HoleShader(){
            super(
            files.internal("shaders/screenspace.vert"),
            tree.get("shaders/TearingSpace.frag")
            );
        }

        public void apply(){
            this.setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2.0F, Core.camera.position.y - Core.camera.height / 2.0F);
            this.setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            this.setUniformi("u_blackholecount", blackHoles.length / 4);
            this.setUniform4fv("u_blackholes", blackHoles, 0, blackHoles.length);
        }
    }
}
