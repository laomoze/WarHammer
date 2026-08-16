//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.graphics;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.scene.ui.layout.Scl;
import arc.struct.FloatSeq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType.Trigger;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders.LoadShader;
import wh.WHVars;

import static arc.Core.files;
import static mindustry.Vars.state;
import static mindustry.Vars.tree;

public class WHShaders{
    public static @Nullable HexagonalTextureShieldShader hexagonalShield;
    public static @Nullable CMoonVoidShieldShader cMoonVoidShield;
    public static OutlineShader powerArea, powerDynamicArea;
    public static ConvexLensShader convex;
    public static RectLensShader convexRect;
    public static @Nullable PsychicTideShader psychicTide;
    public static RingShader ringShader;

    private WHShaders(){
    }

    public static void init(){
        try{
            powerArea = new OutlineShader(){
                @Override
                public float thick(){
                    return 2f;
                }
            };
        }catch(Throwable t){
            Log.err("Failed to load power area shader.", t);
        }

        powerDynamicArea = new OutlineShader(){
            public float thick(){
                return 2f * Interp.slope.apply(Time.time / 240f % 1f);
            }
        };
        convex = new ConvexLensShader();
        convexRect = new RectLensShader();
        hexagonalShield = new HexagonalTextureShieldShader();
        try {
            cMoonVoidShield = new CMoonVoidShieldShader();
        } catch (Throwable t) {
            cMoonVoidShield = null;
            Log.err("Failed to load c-moon void shield shader.", t);
        }
        ringShader = new RingShader();
        try {
            psychicTide = new PsychicTideShader();
        } catch (Throwable t) {
            psychicTide = null;
            Log.err("Failed to load psychic tide shader.", t);
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


    public static class RingShader extends Shader {
        public boolean emissive;
        public Vec3 lightDir = new Vec3();
        public Vec3 cameraPos = new Vec3();
        public Color ambientColor = Color.white.cpy();

        public RingShader() {
            super(WHShaders.mv("ring"), WHShaders.mf("ring"));
        }

        @Override
        public void apply() {
            setUniformf("u_lightdir", lightDir);
            setUniformf("u_campos", cameraPos);
            setUniformf("u_ambientColor", ambientColor.r, ambientColor.g, ambientColor.b);
            setUniformf("u_emissive", emissive ? 1f : 0f);
        }
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

    public static class CMoonVoidShieldShader extends LoadShader {
        public static final int maxShields = 256;
        public static final int shieldsPerPass = 16;
        private final float[] shields = new float[maxShields * 4];
        private final float[] states = new float[maxShields * 4];
        private final float[] alphas = new float[maxShields * 4];
        private final float[] colors = new float[maxShields * 4];
        private int count;
        private int batchOffset;
        private int batchCount;

        public CMoonVoidShieldShader() {
            super("voidShield2", "screenspace");
        }

        public void clear() {
            count = 0;
            batchOffset = 0;
            batchCount = 0;
        }

        public boolean add(float x, float y, float longAxis, float minorAxis, float rotation, float state, float stateProgress, float shieldFraction, float alpha, Color color) {
            if (count >= maxShields) return false;

            int offset = count++ * 4;
            shields[offset] = x;
            shields[offset + 1] = y;
            shields[offset + 2] = longAxis;
            shields[offset + 3] = minorAxis;
            states[offset] = state;
            states[offset + 1] = stateProgress;
            states[offset + 2] = shieldFraction;
            states[offset + 3] = rotation * Mathf.degRad;
            alphas[offset] = alpha;
            colors[offset] = color.r;
            colors[offset + 1] = color.g;
            colors[offset + 2] = color.b;
            colors[offset + 3] = color.a;
            return true;
        }

        public boolean hasShields() {
            return count > 0;
        }

        public int batchCount() {
            return Mathf.ceil(count / (float) shieldsPerPass);
        }

        public void setBatch(int batch) {
            batchOffset = batch * shieldsPerPass;
            batchCount = Math.min(shieldsPerPass, Math.max(0, count - batchOffset));
        }

        @Override
        public void apply() {
            float cameraWidth = Math.max(Core.camera.width, 0.0001f);
            float cameraHeight = Math.max(Core.camera.height, 0.0001f);
            setUniformf("u_time", Time.time);
            setUniformf("u_campos", Core.camera.position.x - cameraWidth / 2f, Core.camera.position.y - cameraHeight / 2f);
            setUniformf("u_resolution", cameraWidth, cameraHeight);
            setUniformi("u_voidshield_count", batchCount);
            setUniformi("u_voidshield_hit_count", 0);
            int offset = batchOffset * 4;
            int length = batchCount * 4;
            setUniform4fv("u_voidshields", shields, offset, length);
            setUniform4fv("u_voidshield_states", states, offset, length);
            setUniform4fv("u_voidshield_alpha", alphas, offset, length);
            setUniform4fv("u_voidshield_colors", colors, offset, length);
        }
    }

    public static class ConvexLensShader extends LoadShader{
        static final int max = 64;
        static final int size = 6;

        //x y radius life[1-0] lifetime strength
        protected FloatSeq data = new FloatSeq();
        protected FloatSeq uniforms = new FloatSeq();
        protected int lensesUniformLoc = Integer.MIN_VALUE;
        protected float snapLeft, snapBottom, snapWidth = 1f, snapHeight = 1f;
        protected boolean hasSnapshot = false;
        protected int replaceCursor = 0;

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

            Events.run(Trigger.draw, () -> {
                if(data.size <= 0) return;

                refreshCameraSnapshot();

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
            refreshCameraSnapshot();

            int count = Math.min(data.size / size, max);

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
            if(hasUniform("u_rect_count")){
                setUniformi("u_rect_count", 0);
            }

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
        }

        private void refreshCameraSnapshot(){
            if(Core.camera == null) return;

            snapWidth = Math.max(Core.camera.width, 0.0001f);
            snapHeight = Math.max(Core.camera.height, 0.0001f);
            snapLeft = Core.camera.position.x - snapWidth / 2f;
            snapBottom = Core.camera.position.y - snapHeight / 2f;
            hasSnapshot = true;
        }

        public void add(float x, float y, float radius, float lifetime){
            add(x, y, radius, lifetime, strength);
        }

        public void add(float x, float y, float radius, float lifetime, float strength){
            float safeRadius = Math.max(radius, 0.001f);
            float safeLifetime = Math.max(lifetime, 1f);
            float safeStrength = Math.max(strength, 0f);

            if(data.size / size >= max){
                var items = data.items;
                int offset = (replaceCursor++ % max) * size;
                items[offset] = x;
                items[offset + 1] = y;
                items[offset + 2] = safeRadius;
                items[offset + 3] = 1f;
                items[offset + 4] = safeLifetime;
                items[offset + 5] = safeStrength;
            }else{
                data.addAll(x, y, safeRadius, 1f, safeLifetime, safeStrength);
            }
        }

        public void clear(){
            data.size = 0;
            replaceCursor = 0;
        }

        public boolean hasAny(){
            return data.size > 0;
        }

        public void blitFrom(FrameBuffer source){
            if(source == null || !hasAny()) return;
            Draw.blend(Blending.disabled);
            source.blit(this);
            Draw.blend();
        }
    }

    public static class RectLensShader extends LoadShader{
        static final int rectMax = 64;
        static final int rectSize = 8;
        static final float minSize = 0.001f;
        static final float minStrength = 0.0001f;
        static final int idxX = 0, idxY = 1, idxLength = 2, idxWidth = 3, idxRotation = 4, idxLife = 5, idxLifetime = 6, idxStrength = 7;

        //x y length width rotation life[1-0] lifetime strength
        protected FloatSeq rectData = new FloatSeq();
        protected FloatSeq rectUniformA = new FloatSeq();
        protected FloatSeq rectUniformB = new FloatSeq();
        protected int rectAUniformLoc = Integer.MIN_VALUE;
        protected int rectBUniformLoc = Integer.MIN_VALUE;
        protected float snapLeft, snapBottom, snapWidth = 1f, snapHeight = 1f;
        protected boolean hasSnapshot = false;
        protected int replaceCursor = 0;

        public float lifetime = 20f;
        public float strength = 0.9f;

        public RectLensShader(){
            super("convexRect", "screenspace");

            Events.run(Trigger.update, () -> {
                if(state.isPaused()) return;
                if(state.isMenu()){
                    clear();
                    return;
                }

                var rectItems = rectData.items;
                for(int i = 0; i < rectData.size; i += rectSize){
                    rectItems[i + idxLife] -= Time.delta / rectItems[i + idxLifetime];

                    if (rectItems[i + idxLife] <= 0f) {
                        if(rectData.size > rectSize){
                            System.arraycopy(rectItems, rectData.size - rectSize, rectItems, i, rectSize);
                        }
                        rectData.size -= rectSize;
                        i -= rectSize;
                    }
                }
            });

            Events.run(Trigger.draw, () -> {
                if(rectData.size <= 0) return;

                refreshCameraSnapshot();
            });
        }

        @Override
        public void apply(){
            refreshCameraSnapshot();

            float screenW = Core.graphics.getWidth();
            float screenH = Core.graphics.getHeight();
            setUniformf("u_screen", screenW, screenH);

            float camWidth = hasSnapshot ? snapWidth : Math.max(Core.camera.width, 0.0001f);
            float camHeight = hasSnapshot ? snapHeight : Math.max(Core.camera.height, 0.0001f);
            float camLeft = hasSnapshot ? snapLeft : (Core.camera.position.x - camWidth / 2f);
            float camBottom = hasSnapshot ? snapBottom : (Core.camera.position.y - camHeight / 2f);
            float worldToScreenX = screenW / camWidth;
            float worldToScreenY = screenH / camHeight;

            rectUniformA.clear();
            rectUniformB.clear();
            int rectPacked = packVisibleRects(screenW, screenH, camLeft, camBottom, worldToScreenX, worldToScreenY);

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

        private int packVisibleRects(float screenW, float screenH, float camLeft, float camBottom, float worldToScreenX, float worldToScreenY) {
            int maxRects = Math.min(rectData.size / rectSize, rectMax);
            var rectItems = rectData.items;
            int packed = 0;

            for (int i = 0; i < maxRects; i++) {
                int offset = i * rectSize;
                float life = Mathf.clamp(rectItems[offset + idxLife]);
                float worldLen = rectItems[offset + idxLength];
                float worldWid = rectItems[offset + idxWidth];
                float localStrength = rectItems[offset + idxStrength] * life;

                if (worldLen <= minSize || worldWid <= minSize || localStrength <= minStrength) continue;

                float sx = (rectItems[offset + idxX] - camLeft) * worldToScreenX;
                float sy = (rectItems[offset + idxY] - camBottom) * worldToScreenY;
                float halfLen = worldLen * worldToScreenX * 0.5f;
                float halfWid = worldWid * worldToScreenY * 0.5f;

                float bound = Mathf.sqrt(halfLen * halfLen + halfWid * halfWid);
                if (sx + bound < 0f || sx - bound > screenW || sy + bound < 0f || sy - bound > screenH) continue;

                float rad = rectItems[offset + idxRotation] * Mathf.degRad;
                rectUniformA.add(sx, sy, halfLen, halfWid);
                rectUniformB.add(Mathf.cos(rad), Mathf.sin(rad), localStrength, 0f);

                packed++;
                if (packed >= rectMax) break;
            }

            return packed;
        }

        private void refreshCameraSnapshot(){
            if(Core.camera == null) return;

            snapWidth = Math.max(Core.camera.width, 0.0001f);
            snapHeight = Math.max(Core.camera.height, 0.0001f);
            snapLeft = Core.camera.position.x - snapWidth / 2f;
            snapBottom = Core.camera.position.y - snapHeight / 2f;
            hasSnapshot = true;
        }

        public void addRect(float x, float y, float length, float width, float rotation, float lifetime){
            addRect(x, y, length, width, rotation, lifetime, strength);
        }

        public void addRect(float x, float y, float length, float width, float rotation, float lifetime, float strength){
            float safeLength = Math.max(length, minSize);
            float safeWidth = Math.max(width, minSize);
            float safeLifetime = Math.max(lifetime, 1f);
            float safeStrength = Math.max(strength, 0f);

            if(rectData.size / rectSize >= rectMax){
                var items = rectData.items;
                int offset = (replaceCursor++ % rectMax) * rectSize;
                items[offset + idxX] = x;
                items[offset + idxY] = y;
                items[offset + idxLength] = safeLength;
                items[offset + idxWidth] = safeWidth;
                items[offset + idxRotation] = rotation;
                items[offset + idxLife] = 1f;
                items[offset + idxLifetime] = safeLifetime;
                items[offset + idxStrength] = safeStrength;
            }else{
                rectData.addAll(x, y, safeLength, safeWidth, rotation, 1f, safeLifetime, safeStrength);
            }
        }

        public void clear(){
            rectData.size = 0;
            replaceCursor = 0;
        }

        public boolean hasAny(){
            return rectData.size > 0;
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
        try{
            Shader.prependFragmentCode = "#define MAX_COUNT " + MaxCont + "\n";
            holeShader = new HoleShader();
        }catch(Throwable t){
            holeShader = null;
            Log.err("Failed to load black hole shader.", t);
        }finally{
            Shader.prependFragmentCode = "";
        }
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

    public static class PsychicTideShader extends Shader {
        public PsychicTideShader() {
            super(
                    files.internal("shaders/screenspace.vert"),
                    tree.get("shaders/psychicTide.frag")
            );
        }

        @Override
        public void apply() {
            float camWidth = Math.max(Core.camera.width, 0.0001f);
            float camHeight = Math.max(Core.camera.height, 0.0001f);
            setUniformf("u_time", Time.time);
            setUniformf("u_campos",
                    Core.camera.position.x - camWidth / 2f,
                    Core.camera.position.y - camHeight / 2f);
            setUniformf("u_resolution", camWidth, camHeight);
        }
    }
}
