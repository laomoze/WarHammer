package wh.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.graphics.*;
import wh.content.*;
import wh.core.*;

import static arc.Core.*;
import static wh.graphics.WHShaders.convex;

public class MainRenderer{
    private final Seq<BlackHole> holes = new Seq<>();
    public static MainRenderer renderer;

    public FrameBuffer buffer = new FrameBuffer();
    public FrameBuffer buffer2 = new FrameBuffer();
    public int width, height;

    private static final float[][] initFloat = new float[512][];
    private static final float[][] initStrength = new float[512][];
    private static final Pool<BlackHole> holePool = Pools.get(BlackHole.class, BlackHole::new);
    private static boolean warnedConvexMissing = false;
    private static boolean warnedHoleMissing = false;

    protected MainRenderer(){
        if(!Vars.headless){
            WHShaders.createHoleShader();
            Events.run(EventType.Trigger.draw, this::draw);
        }
    }

    public void draw(){
        width = graphics.getWidth();
        height = graphics.getHeight();

        buffer2.resize(graphics.getWidth(), graphics.getHeight());
       /* drawShader(WHShaders.powerArea, WHContent.POWER_AREA,1);
        drawShader(WHShaders.powerDynamicArea, WHContent.POWER_DYNAMIC, 1);*/
        if(Vars.renderer.animateShields){
            drawShader(WHShaders.hexagonalShield, WHContent.HEXAGONAL_SHIELD, 1);
        }
        advancedDraw();
    }

    public static void init(){
        if(renderer == null) renderer = new MainRenderer();
        for(int i = 0; i < 512; i++){
            initFloat[i] = new float[i * 4];
            initStrength[i] = new float[i * 4];
        }
    }

    public void drawShader(Shader shader, float layer, float range){
        if(shader != null){
            Draw.drawRange(layer, range, () -> buffer2.begin(Color.clear), () -> {
                buffer2.end();
                buffer2.blit(shader);
            });
        }
    }

    public static void addShockCircle(float x, float y, float r, float lifetime){
        addShockCircle(x, y, r, lifetime, convex == null ? 0f : convex.strength);
    }

    public static void addShockCircle(float x, float y, float r, float lifetime, float strength){
        float scaledStrength = resolveDistortionStrength(strength);
        if(scaledStrength <= 0.0001f || convex == null) return;
        convex.add(x, y, r, lifetime, scaledStrength);
    }

    public static void addShockRect(float x, float y, float length, float width, float angle, float lifetime){
        addShockRect(x, y, length, width, angle, lifetime, convex == null ? 0f : convex.strength);
    }

    public static void addShockRect(float x, float y, float length, float width, float angle, float lifetime, float strength){
        float scaledStrength = resolveDistortionStrength(strength);
        if(scaledStrength <= 0.0001f || convex == null) return;
        convex.addRect(x, y, length, width, angle, lifetime, scaledStrength);
    }

    private static float resolveDistortionStrength(float baseStrength){
        if(Vars.headless || !WHSettings.distortionEnabled()) return 0f;
        float scale = WHSettings.distortionStrengthScale();
        if(scale <= 0.0001f) return 0f;

        if(convex == null){
            if(!warnedConvexMissing){
                warnedConvexMissing = true;
                Log.warn("Convex lens shader is null; WHShaders.init() may have failed.");
            }
            return 0f;
        }

        float scaledStrength = baseStrength * scale;
        return scaledStrength <= 0.0001f ? 0f : scaledStrength;
    }

    public static void addBlackHole(float x, float y, float inRadius, float outRadius, float alpha){
        addBlackHole(x, y, inRadius, outRadius, alpha, 1f);
    }

    public static void addBlackHole(float x, float y, float inRadius, float outRadius){
        addBlackHole(x, y, inRadius, outRadius, 1f, 1f);
    }

    public static void addBlackHole(float x, float y, float inRadius, float outRadius, float alpha, float strength){
        if(!Vars.headless && renderer != null){
            renderer.addHole(x, y, inRadius, outRadius, alpha, strength);
        }
    }

    private void advancedDraw(){
        if(settings.getBool("pixelate") || holes.size >= 512){
            holes.clear();
            return;
        }
        Draw.draw(Layer.floor - 8, () -> {
            buffer.resize(graphics.getWidth(), graphics.getHeight());
            if(!buffer.isBound()) buffer.begin();
        });

        Draw.draw(Layer.space + 16, () -> {
            int holeCount = holes.size;
            if(holeCount >= WHShaders.MaxCont) WHShaders.createHoleShader();

            //Keep black core circles inside the source buffer so post effects don't overwrite them.
            if(!buffer.isBound()) buffer.begin();
            if(buffer.isBound()) buffer.end();

            buffer2.resize(graphics.getWidth(), graphics.getHeight());
            buffer2.begin(Color.clear);
            if(WHShaders.holeShader != null){
                float[] blackholes = initFloat[holeCount];
                float[] strengths = initStrength[holeCount];

                for(int i = 0; i < holeCount; i++){
                    var hole = holes.get(i);
                    blackholes[i * 4] = hole.x;
                    blackholes[i * 4 + 1] = hole.y;
                    blackholes[i * 4 + 2] = hole.inRadius;
                    blackholes[i * 4 + 3] = hole.outRadius;
                    strengths[i * 4] = hole.strength;
                }

                WHShaders.holeShader.blackHoles = blackholes;
                WHShaders.holeShader.blackHoleStrengths = strengths;
                //Compose blackhole pass into buffer2 first, so convex can sample the already-distorted image.
                buffer.blit(WHShaders.holeShader);
            }else{
                if(!warnedHoleMissing){
                    warnedHoleMissing = true;
                    Log.warn("Black hole shader is null; falling back to screenspace pass.");
                }
                buffer.blit(Shaders.screenspace);
            }
            buffer2.end();

            if(convex != null && convex.hasAny()){
                convex.blitFrom(buffer2);
            }else{
                buffer2.blit(Shaders.screenspace);
            }

            //Draw black cores after post-processing to avoid donut-like rings from re-sampling.
            for(int i = 0; i < holeCount; i++){
                var hole = holes.get(i);
                Draw.color(Tmp.c2.set(Color.black).a(hole.alpha));
                Fill.circle(hole.x, hole.y, hole.inRadius * 1.5f);
                holePool.free(hole);
            }
            Draw.color();
            holes.clear();
        });
    }

    private void addHole(float x, float y, float inRadius, float outRadius, float alpha, float strength){
        if(inRadius > outRadius || outRadius <= 0) return;

        float safeStrength = Math.max(strength, 0.0001f);
        float scaledStrength = safeStrength * WHSettings.distortionStrengthScale();
        if(!WHSettings.distortionEnabled()) scaledStrength = 0f;

        holes.add(holePool.obtain().set(x, y, inRadius, outRadius, alpha, scaledStrength));
    }

    private static class BlackHole{
        float x, y, inRadius, outRadius, alpha, strength;

        public BlackHole set(float x, float y, float inRadius, float outRadius, float alpha, float strength){
            this.x = x;
            this.y = y;
            this.inRadius = inRadius;
            this.outRadius = outRadius;
            this.alpha = alpha;
            this.strength = strength;
            return this;
        }

        public BlackHole(){

        }
    }
}
