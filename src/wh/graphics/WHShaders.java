package wh.graphics;

import arc.files.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import wh.type.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static wh.WHVars.*;

public final class WHShaders {
    public static DepthShader depth;
    public static DepthAtmosphereShader depthAtmosphere;

    private WHShaders() {}

    public static void init() {
        depth = new DepthShader();
        depthAtmosphere = new DepthAtmosphereShader();
    }

    /**
     * Resolves shader files from this mod via {@link Vars#tree}.
     *
     * @param name The shader file name, e.g. {@code my-shader.frag}.
     * @return The shader file, located inside {@code shaders/}.
     */
    public static Fi df(String name) {
        return tree.get("shaders/" + name + ".frag");
    }

    public static Fi dv(String name) {
        return tree.get("shaders/" + name + ".vert");
    }

    public static Fi mf(String name) {
        return internalTree.child("shaders/" + name + ".frag");
    }

    public static Fi mv(String name) {
        return internalTree.child("shaders/" + name + ".vert");
    }

    /**
     * An atmosphere shader that incorporates the planet shape in a form of depth texture. Better quality, but at the little
     * cost of performance.
     */
    public static final class DepthAtmosphereShader extends Shader {
        private static final Mat3D mat = new Mat3D();

        public Camera3D camera;
        public BetterPlanet planet;

        /** This class only requires one instance. Please use {@link WHShaders#depthAtmosphere}. */
        private DepthAtmosphereShader() {
            super(mv("depth-atmosphere"), mf("depth-atmosphere"));
        }

        @Override
        public void apply() {
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

            planet.depthBuffer.getTexture().bind(0);
            setUniformi("u_topology", 0);
            setUniformf("u_viewport", graphics.getWidth(), graphics.getHeight());
        }
    }

    /** Specialized mesh shader to capture fragment depths. */
    public static final class DepthShader extends Shader {
        public Camera3D camera;

        /** This class only requires one instance. Please use {@link WHShaders#depth}. */
        private DepthShader() {
            super(mv("depth"), mf("depth"));
        }

        @Override
        public void apply() {
            setUniformf("u_camPos", camera.position);
            setUniformf("u_camRange", camera.near, camera.far - camera.near);
        }
    }
}
