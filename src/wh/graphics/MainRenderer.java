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

import static arc.Core.*;
import static wh.graphics.WHShaders.convex;

public class MainRenderer{
    private final Seq<BlackHole> holes = new Seq<>();
    public static MainRenderer renderer;

    public FrameBuffer buffer = new FrameBuffer();
    public FrameBuffer buffer2 = new FrameBuffer();
    public int width, height;

    private static final float[][] initFloat = new float[512][];
    private static final Pool<BlackHole> holePool = Pools.get(BlackHole.class, BlackHole::new);
    private static boolean warnedConvexMissing = false;

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
        if(Vars.headless) return;
        if(convex == null){
            if(!warnedConvexMissing){
                warnedConvexMissing = true;
                Log.warn("Convex lens shader is null; WHShaders.init() may have failed.");
            }
            return;
        }
        convex.add(x, y, r, lifetime);
    }

    public static void addShockCircle(float x, float y, float r, float lifetime, float strength){
        if(Vars.headless) return;
        if(convex == null){
            if(!warnedConvexMissing){
                warnedConvexMissing = true;
                Log.warn("Convex lens shader is null; WHShaders.init() may have failed.");
            }
            return;
        }
        convex.add(x, y, r, lifetime, strength);
    }

   /* public static void addShockEsp(float x, float y, float r, float life,float rot){
        if(shockwave != null&&!Vars.headless) shockwave.addLensEllipse(x, y, r,r,rot,life,0.2f);
    }*/

    public static void addBlackHole(float x, float y, float inRadius, float outRadius, float alpha){
        if(!Vars.headless) renderer.addHole(x, y, inRadius, outRadius, alpha);
    }

    public static void addBlackHole(float x, float y, float inRadius, float outRadius){
        if(!Vars.headless) renderer.addHole(x, y, inRadius, outRadius, 1);
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
            if(buffer.isBound()) buffer.end();

            if(holes.size >= WHShaders.MaxCont) WHShaders.createHoleShader();

            float[] blackholes = initFloat[holes.size];

            for(int i = 0; i < holes.size; i++){
                var hole = holes.get(i);
                blackholes[i * 4] = hole.x;
                blackholes[i * 4 + 1] = hole.y;
                blackholes[i * 4 + 2] = hole.inRadius;
                blackholes[i * 4 + 3] = hole.outRadius;

                Draw.color(Tmp.c2.set(Color.black).a(hole.alpha));
                Fill.circle(hole.x, hole.y, hole.inRadius * 1.5f);
                Draw.color();

                holePool.free(hole);
            }
            WHShaders.holeShader.blackHoles = blackholes;
            buffer.blit(WHShaders.holeShader);

            if(!buffer.isBound()) buffer.begin();
            Draw.rect();
            if(buffer.isBound()) buffer.end();
            holes.clear();
        });
    }

    private void addHole(float x, float y, float inRadius, float outRadius, float alpha){
        if(inRadius > outRadius || outRadius <= 0) return;

        holes.add(holePool.obtain().set(x, y, inRadius, outRadius, alpha));
    }

    private static class BlackHole{
        float x, y, inRadius, outRadius, alpha;

        public BlackHole set(float x, float y, float inRadius, float outRadius, float alpha){
            this.x = x;
            this.y = y;
            this.inRadius = inRadius;
            this.outRadius = outRadius;
            this.alpha = alpha;
            return this;
        }

        public BlackHole(){

        }
    }
}
