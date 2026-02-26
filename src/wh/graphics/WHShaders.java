//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import mindustry.graphics.Shaders.*;
import wh.*;

import static arc.Core.files;
import static mindustry.Vars.*;

public class WHShaders{
    public static @Nullable HexagonalTextureShieldShader hexagonalShield;
    public static OutlineShader powerArea, powerDynamicArea;
    public static ConvexLensShader convex;

    private WHShaders(){
    }

    public static void init(){
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
        convex = new ConvexLensShader();
        try{
            hexagonalShield = new HexagonalTextureShieldShader();
        }catch(Throwable t){
            //don't load shield shader
            hexagonalShield = null;
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

    public static class ConvexLensShader extends LoadShader{
        static final int max = 64;
        static final int size = 6;

        //x y radius life[1-0] lifetime strength
        protected FloatSeq data = new FloatSeq();
        protected FloatSeq uniforms = new FloatSeq();
        protected boolean hadAny = false;
        protected FrameBuffer buffer = new FrameBuffer();
        protected int lensesUniformLoc = Integer.MIN_VALUE;
        protected boolean warnedMissingLensUniform = false;

        public float lifetime = 20f;
        public float strength = 0.9f;
        public boolean debugDraw = true;
        public boolean debugForceVisible = false;

        public ConvexLensShader(){
            super("convex", "screenspace");

            Events.run(Trigger.update, () -> {
                if(state.isPaused()) return;
                if(state.isMenu()){
                    clear();
                    return;
                }

                var items = data.items;
                for(int i = 0; i < data.size; i += size){
                    //decrease lifetime
                    items[i + 3] -= Time.delta / items[i + 4];

                    if(items[i + 3] <= 0f){
                        if(data.size > size){
                            System.arraycopy(items, data.size - size, items, i, size);
                        }
                        data.size -= size;
                        i -= size;
                    }
                }
            });

            Events.run(Trigger.preDraw, () -> {
                hadAny = data.size > 0;
                if(hadAny){
                    buffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
                    buffer.begin(Color.clear);
                }
            });

            Events.run(Trigger.postDraw, () -> {
                if(hadAny){
                    buffer.end();
                    // Replace with distorted capture directly; alpha blending can hide the effect.
                    Draw.blend(Blending.disabled);
                    buffer.blit(this);
                    Draw.blend();
                }
            });

            Events.run(Trigger.draw, () -> {
                if(debugDraw){
                    Draw.proj(Core.camera);
                    Draw.z(Layer.effect + 1f);
                    Draw.color(Pal.remove);
                    Lines.stroke(1.2f);

                    var items = data.items;
                    for(int i = 0; i < data.size; i += size){
                        float fin = 1f - items[i + 3];
                        float pulse = Mathf.pow(Mathf.sin(Mathf.clamp(fin) * Mathf.pi), 0.55f);
                        float currentRadius = items[i + 2] * pulse;

                        Fill.circle(items[i], items[i + 1], 4f);
                        if(currentRadius > 0.5f){
                            Lines.circle(items[i], items[i + 1], currentRadius);
                        }
                    }

                    Draw.color();
                }
            });

        }

        @Override
        public void apply(){
            int count = Math.min(data.size / size, max);

            setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            setUniformf("u_campos",
            Core.camera.position.x - Core.camera.width / 2f,
            Core.camera.position.y - Core.camera.height / 2f
            );
            setUniformi("u_lens_count", count);
            setUniformi("u_debug_force", debugForceVisible && count > 0 ? 1 : 0);

            if(count <= 0) return;

            uniforms.clear();

            var items = data.items;
            for(int i = 0; i < count; i++){
                int offset = i * size;
                float fin = 1f - items[offset + 3];
                float pulse = Mathf.pow(Mathf.sin(Mathf.clamp(fin) * Mathf.pi), 0.55f);

                uniforms.add(
                items[offset], items[offset + 1],
                items[offset + 2] * pulse,
                items[offset + 5] * pulse
                );
            }

            if(lensesUniformLoc == Integer.MIN_VALUE){
                lensesUniformLoc = getUniformLocation("u_lenses");
                if(lensesUniformLoc < 0){
                    lensesUniformLoc = getUniformLocation("u_lenses[0]");
                }
            }

            if(lensesUniformLoc >= 0){
                setUniform4fv(lensesUniformLoc, uniforms.items, 0, uniforms.size);
            }else if(debugDraw && !warnedMissingLensUniform){
                warnedMissingLensUniform = true;
                Log.warn("Convex lens uniform array not found: u_lenses / u_lenses[0]");
            }
        }

        public void add(float x, float y, float radius, float lifetime){
            add(x, y, radius, lifetime, strength);
        }

        public void add(float x, float y, float radius, float lifetime, float strength){
            float safeRadius = Math.max(radius, 0.001f);
            float safeLifetime = Math.max(lifetime, 1f);
            float safeStrength = Math.max(strength, 0f);

            //replace first entry
            if(data.size / size >= max){
                var items = data.items;
                items[0] = x;
                items[1] = y;
                items[2] = safeRadius;
                items[3] = 1f;
                items[4] = safeLifetime;
                items[5] = safeStrength;
            }else{
                data.addAll(x, y, safeRadius, 1f, safeLifetime, safeStrength);
            }
        }

        public void clear(){
            data.size = 0;
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
