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
import mindustry.graphics.Shaders.*;
import wh.*;

import static arc.Core.files;
import static mindustry.Vars.*;

public class WHShaders{
    public static @Nullable HexagonalTextureShieldShader hexagonalShield;
    public static OutlineShader powerArea, powerDynamicArea;
    public static ShockwaveShader shockwave;

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
        try{
            hexagonalShield = new HexagonalTextureShieldShader();
        }catch(Throwable t){
            //don't load shield shader
            hexagonalShield = null;
            t.printStackTrace();
        }
        shockwave = new ShockwaveShader();
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

    public static class ShockwaveShader extends LoadShader{
        static final int max = 64;
        static final int size = 5;

        //x y radius life[1-0] lifetime
        protected FloatSeq data = new FloatSeq();
        protected FloatSeq uniforms = new FloatSeq();
        protected boolean hadAny = false;
        protected FrameBuffer buffer = new FrameBuffer();

        public float lifetime = 20f;

        public ShockwaveShader(){
            super("shockwave", "screenspace");

            Events.run(Trigger.update, () -> {
                if(state.isPaused()) return;
                if(state.isMenu()){
                    data.size = 0;
                    return;
                }

                var items = data.items;
                for(int i = 0; i < data.size; i += size){
                    //decrease lifetime
                    items[i + 3] -= Time.delta / items[i + 4];

                    if(items[i + 3] <= 0f){
                        //swap with head.
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
                    Draw.blend(Blending.disabled);
                    buffer.blit(this);
                    Draw.blend();
                }
            });
        }

        @Override
        public void apply(){
            int count = data.size / size;

            setUniformi("u_shockwave_count", count);
            if(count > 0){
                setUniformf("u_resolution", Core.camera.width, Core.camera.height);
                setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2f, Core.camera.position.y - Core.camera.height / 2f);

                uniforms.clear();

                var items = data.items;
                for(int i = 0; i < count; i++){
                    int offset = i * size;

                    uniforms.add(
                    items[offset], items[offset + 1], //xy
                    items[offset + 2] * (1f - items[offset + 3]), //radius * time
                    items[offset + 3] //time
                    //lifetime ignored
                    );
                }

                setUniform4fv("u_shockwaves", uniforms.items, 0, uniforms.size);
            }
        }

        public void add(float x, float y, float radius){
            add(x, y, radius, 20f);
        }

        public void add(float x, float y, float radius, float lifetime){
            //replace first entry
            if(data.size / size >= max){
                var items = data.items;
                items[0] = x;
                items[1] = y;
                items[2] = radius;
                items[3] = 1f;
                items[4] = lifetime;
            }else{
                data.addAll(x, y, radius, 1f, lifetime);
            }
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
