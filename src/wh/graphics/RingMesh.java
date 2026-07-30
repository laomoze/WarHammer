package wh.graphics;

import arc.graphics.*;
import arc.math.Mathf;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.util.Time;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.PlanetMesh;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;

public class RingMesh extends PlanetMesh {


    private static final VertexAttribute emissiveAttribute = new VertexAttribute(4, 5121, true, "a_emissive");
    public float innerRadius;
    public float outerRadius;
    public float rotationSpeed;
    public boolean emissive = true;

    public RingMesh(Planet planet, float innerRadius, float outerRadius, int segments) {
        this(planet, innerRadius, outerRadius, segments, Color.white, Color.white, 0f);
    }

    public RingMesh(Planet planet, float innerRadius, float outerRadius, int segments, Color color) {
        this(planet, innerRadius, outerRadius, segments, color, color, 0f);
    }

    public RingMesh(Planet planet, float innerRadius, float outerRadius, int segments, Color innerColor, Color outerColor, float rotationSpeed) {
        super(planet, buildMesh(innerRadius, outerRadius, segments, innerColor, outerColor), Shaders.planet);
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.rotationSpeed = rotationSpeed;
    }

    private static Mesh buildMesh(float innerRadius, float outerRadius, int segments, Color innerColor, Color outerColor) {
        int vertices = segments * 2;
        float[] data = new float[vertices * 8];
        short[] indices = new short[segments * 6];
        Color innerRingColor = innerColor == null ? Color.white : innerColor;
        Color outerRingColor = outerColor == null ? innerRingColor : outerColor;

        float innerVertexColor = innerRingColor.toFloatBits();
        float innerEmissive = innerRingColor.toFloatBits();
        float outerVertexColor = outerRingColor.toFloatBits();
        float outerEmissive = outerRingColor.toFloatBits();

        for (int i = 0; i < segments; i++) {
            float angle = i / (float) segments * Mathf.PI2;
            float cos = Mathf.cos(angle);
            float sin = Mathf.sin(angle);

            int offset = i * 16;

            writeVertex(data, offset, cos * innerRadius, 0f, sin * innerRadius, innerVertexColor, innerEmissive);

            writeVertex(data, offset + 8, cos * outerRadius, 0f, sin * outerRadius, outerVertexColor, outerEmissive);
        }

        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;

            int index = i * 6;

            short inner = (short) (i * 2);
            short outer = (short) (i * 2 + 1);
            short nextInner = (short) (next * 2);
            short nextOuter = (short) (next * 2 + 1);

            indices[index] = inner;
            indices[index + 1] = outer;
            indices[index + 2] = nextOuter;

            indices[index + 3] = inner;
            indices[index + 4] = nextOuter;
            indices[index + 5] = nextInner;
        }

        Mesh mesh = new Mesh(
                true,
                vertices,
                indices.length,
                VertexAttribute.position3, VertexAttribute.normal, VertexAttribute.color, emissiveAttribute);

        mesh.setVertices(data);
        mesh.setIndices(indices);

        return mesh;
    }

    private static void writeVertex(float[] data, int offset, float x, float y, float z, float color, float emissive) {
        data[offset] = x;
        data[offset + 1] = y;
        data[offset + 2] = z;
        data[offset + 3] = 0f;
        data[offset + 4] = 1f;
        data[offset + 5] = 0f;
        data[offset + 6] = color;
        data[offset + 7] = emissive;
    }

    @Override
    public void preRender(PlanetParams params) {
        if (WHShaders.ringShader != null) {
            shader = WHShaders.ringShader;
            WHShaders.ringShader.emissive = emissive;
            WHShaders.ringShader.ambientColor.set(planet.solarSystem.lightColor);
            WHShaders.ringShader.lightDir.set(planet.getLightNormal());
            WHShaders.ringShader.cameraPos.set(params.camPos).add(planet.position);
        }
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform) {
        Mat3D ringTransform = new Mat3D(transform);
        if (rotationSpeed != 0f) {
            ringTransform.rotate(Vec3.Y, Time.time * rotationSpeed);
        }

        Gl.disable(Gl.cullFace);
        preRender(params);
        super.render(params, projection, ringTransform);
        Blending.normal.apply();
        Gl.enable(Gl.cullFace);
    }

}
