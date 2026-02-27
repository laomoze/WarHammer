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
        static final int rectMax = 64;
        static final int rectSize = 8;

        //x y radius life[1-0] lifetime strength
        protected FloatSeq data = new FloatSeq();
        protected FloatSeq uniforms = new FloatSeq();
        //x y length width rotation life[1-0] lifetime strength
        protected FloatSeq rectData = new FloatSeq();
        protected FloatSeq rectUniformA = new FloatSeq();
        protected FloatSeq rectUniformB = new FloatSeq();
        protected int lensesUniformLoc = Integer.MIN_VALUE;
        protected int rectAUniformLoc = Integer.MIN_VALUE;
        protected int rectBUniformLoc = Integer.MIN_VALUE;
        protected float snapLeft, snapBottom, snapWidth = 1f, snapHeight = 1f;
        protected boolean hasSnapshot = false;

        public float lifetime = 20f;
        public float strength = 0.9f;
        public boolean debugDraw = false;

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

                var rectItems = rectData.items;
                for(int i = 0; i < rectData.size; i += rectSize){
                    rectItems[i + 5] -= Time.delta / rectItems[i + 6];

                    if(rectItems[i + 5] <= 0f){
                        if(rectData.size > rectSize){
                            System.arraycopy(rectItems, rectData.size - rectSize, rectItems, i, rectSize);
                        }
                        rectData.size -= rectSize;
                        i -= rectSize;
                    }
                }
            });

            Events.run(Trigger.draw, () -> {
                if(data.size <= 0) return;

                if(Core.camera != null){
                    snapWidth = Math.max(Core.camera.width, 0.0001f);
                    snapHeight = Math.max(Core.camera.height, 0.0001f);
                    snapLeft = Core.camera.position.x - snapWidth / 2f;
                    snapBottom = Core.camera.position.y - snapHeight / 2f;
                    hasSnapshot = true;
                }

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
            int rectCount = Math.min(rectData.size / rectSize, rectMax);

            float screenW = Core.graphics.getWidth();
            float screenH = Core.graphics.getHeight();
            setUniformf("u_screen", screenW, screenH);

            float camWidth = hasSnapshot ? snapWidth : Math.max(Core.camera.width, 0.0001f);
            float camHeight = hasSnapshot ? snapHeight : Math.max(Core.camera.height, 0.0001f);
            float camLeft = hasSnapshot ? snapLeft : (Core.camera.position.x - camWidth / 2f);
            float camBottom = hasSnapshot ? snapBottom : (Core.camera.position.y - camHeight / 2f);
            float invCamWidth = screenW / camWidth;
            float invCamHeight = screenH / camHeight;

            uniforms.clear();
            int packed = 0;

            if(count > 0){
                var items = data.items;
                for(int i = 0; i < count; i++){
                    int offset = i * size;
                    float fin = 1f - items[offset + 3];
                    float pulse = Mathf.pow(Mathf.sin(Mathf.clamp(fin) * Mathf.pi), 0.55f);
                    float worldRadius = items[offset + 2] * pulse;
                    float localStrength = items[offset + 5] * pulse;

                    if(worldRadius <= 0.001f || localStrength <= 0.0001f) continue;

                    float sx = (items[offset] - camLeft) * invCamWidth;
                    float sy = (items[offset + 1] - camBottom) * invCamHeight;
                    float sr = worldRadius * invCamWidth;

                    //Skip lenses entirely outside the screen.
                    if(sx + sr < 0f || sx - sr > screenW || sy + sr < 0f || sy - sr > screenH) continue;

                    uniforms.add(
                    sx, sy,
                    sr,
                    localStrength
                    );
                    packed++;

                    if(packed >= max) break;
                }
            }

            setUniformi("u_lens_count", packed);

            if(packed > 0){
                if(lensesUniformLoc == Integer.MIN_VALUE){
                    lensesUniformLoc = getUniformLocation("u_lenses");
                    if(lensesUniformLoc < 0){
                        lensesUniformLoc = getUniformLocation("u_lenses[0]");
                    }
                }

                if(lensesUniformLoc >= 0){
                    setUniform4fv(lensesUniformLoc, uniforms.items, 0, uniforms.size);
                }else{
                    setUniformi("u_lens_count", 0);
                }
            }

            rectUniformA.clear();
            rectUniformB.clear();
            int rectPacked = 0;

            if(rectCount > 0){
                var rectItems = rectData.items;
                for(int i = 0; i < rectCount; i++){
                    int offset = i * rectSize;
                    float life = Mathf.clamp(rectItems[offset + 5]);
                    float worldLen = rectItems[offset + 2];
                    float worldWid = rectItems[offset + 3];
                    float localStrength = rectItems[offset + 7] * life;

                    if(worldLen <= 0.001f || worldWid <= 0.001f || localStrength <= 0.0001f) continue;

                    float sx = (rectItems[offset] - camLeft) * invCamWidth;
                    float sy = (rectItems[offset + 1] - camBottom) * invCamHeight;
                    float halfLen = worldLen * invCamWidth * 0.5f;
                    float halfWid = worldWid * invCamHeight * 0.5f;

                    float bound = Mathf.sqrt(halfLen * halfLen + halfWid * halfWid);
                    if(sx + bound < 0f || sx - bound > screenW || sy + bound < 0f || sy - bound > screenH) continue;

                    float rad = rectItems[offset + 4] * Mathf.degRad;
                    float cos = Mathf.cos(rad), sin = Mathf.sin(rad);

                    rectUniformA.add(
                    sx, sy,
                    halfLen,
                    halfWid
                    );
                    rectUniformB.add(
                    cos, sin,
                    localStrength,
                    0f
                    );

                    rectPacked++;
                    if(rectPacked >= rectMax) break;
                }
            }

            setUniformi("u_rect_count", rectPacked);
            if(rectPacked <= 0) return;

            if(rectAUniformLoc == Integer.MIN_VALUE){
                rectAUniformLoc = getUniformLocation("u_rectsA");
                if(rectAUniformLoc < 0){
                    rectAUniformLoc = getUniformLocation("u_rectsA[0]");
                }
            }

            if(rectBUniformLoc == Integer.MIN_VALUE){
                rectBUniformLoc = getUniformLocation("u_rectsB");
                if(rectBUniformLoc < 0){
                    rectBUniformLoc = getUniformLocation("u_rectsB[0]");
                }
            }

            if(rectAUniformLoc >= 0 && rectBUniformLoc >= 0){
                setUniform4fv(rectAUniformLoc, rectUniformA.items, 0, rectUniformA.size);
                setUniform4fv(rectBUniformLoc, rectUniformB.items, 0, rectUniformB.size);
            }else{
                setUniformi("u_rect_count", 0);
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

        public void addRect(float x, float y, float length, float width, float rotation, float lifetime){
            addRect(x, y, length, width, rotation, lifetime, strength);
        }

        public void addRect(float x, float y, float length, float width, float rotation, float lifetime, float strength){
            float safeLength = Math.max(length, 0.001f);
            float safeWidth = Math.max(width, 0.001f);
            float safeLifetime = Math.max(lifetime, 1f);
            float safeStrength = Math.max(strength, 0f);

            if(rectData.size / rectSize >= rectMax){
                var items = rectData.items;
                items[0] = x;
                items[1] = y;
                items[2] = safeLength;
                items[3] = safeWidth;
                items[4] = rotation;
                items[5] = 1f;
                items[6] = safeLifetime;
                items[7] = safeStrength;
            }else{
                rectData.addAll(x, y, safeLength, safeWidth, rotation, 1f, safeLifetime, safeStrength);
            }
        }

        public void clear(){
            data.size = 0;
            rectData.size = 0;
        }

        public boolean hasAny(){
            return data.size > 0 || rectData.size > 0;
        }

        public void blitFrom(FrameBuffer source){
            if(source == null || !hasAny()) return;
            Draw.blend(Blending.disabled);
            source.blit(this);
            Draw.blend();
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
        public float[] blackHoleStrengths;

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
            this.setUniform4fv("u_blackholeStrengths", blackHoleStrengths, 0, blackHoleStrengths.length);
        }
    }
}
