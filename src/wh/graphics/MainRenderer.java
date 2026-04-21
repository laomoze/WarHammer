package wh.graphics;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Tmp;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Groups;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import wh.content.WHContent;
import wh.core.WHSettings;
import wh.entities.world.entities.CarrierUnitType;
import wh.gen.CarrierUnit.CarrierHostc;
import wh.gen.CarrierUnit.CarrierRuntime;
import wh.gen.CarrierUnit.UnitAI.CarrierFighterAI;

import static arc.Core.graphics;
import static arc.Core.settings;

public class MainRenderer{
    private final Seq<BlackHole> holes = new Seq<>();
    private final Vec2 hudQueue = new Vec2();
    private final Vec2 hudReverse = new Vec2();
    private final Vec2 hudFront = new Vec2();
    private final Vec2 hudLaunch = new Vec2();
    private final Vec2 hudTakeoffFrom = new Vec2();
    private final Vec2 hudTakeoffTo = new Vec2();
    private final Vec2 hudVel = new Vec2();
    private final Vec2 hudNose = new Vec2();
    public static MainRenderer renderer;

    public FrameBuffer buffer = new FrameBuffer();
    public FrameBuffer buffer2 = new FrameBuffer();
    public int width, height;

    private static final float[][] initFloat = new float[512][];
    private static final float[][] initStrength = new float[512][];
    private static final Pool<BlackHole> holePool = Pools.get(BlackHole.class, BlackHole::new);
    private static boolean warnedConvexMissing = false;
    private static boolean warnedRectMissing = false;
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
        drawCarrierDebugHud();
    }

    private void drawCarrierDebugHud(){
        if(Vars.headless || !WHSettings.carrierDebugHud()) return;
        if(Vars.state == null || !Vars.state.isGame()) return;

        Draw.z(Layer.flyingUnit + 2f);

        Groups.unit.each(fighter -> {
            if(!(fighter.controller() instanceof CarrierFighterAI ai)) return;

            CarrierHostc carrier = ai.carrierDebug();
            if(carrier == null) return;
            CarrierUnitType type = carrier.carrierType();
            if(type == null) return;

            int runway = carrier.clampRunway(ai.runwayIndex());
            carrier.recoveryPoint(runway, Tmp.v1);
            carrier.runwayFrontPoint(runway, hudFront);
            carrier.launchExitPoint(runway, hudLaunch);
            carrier.runwayQueueInsertPoint(runway, hudQueue);
            ai.debugTakeoffFrom(hudTakeoffFrom);
            ai.debugTakeoffTo(hudTakeoffTo);
            boolean showReverse = false;
            if(carrier instanceof CarrierRuntime runtime){
                runtime.recoveryReversePoint(runway, hudReverse);
                showReverse = Float.isFinite(hudReverse.x) && Float.isFinite(hudReverse.y);
            }

            float distTail = fighter.dst(Tmp.v1);
            float velLen = fighter.vel.len();
            float orbitAngleErr = ai.debugOrbitAngleError();
            String stage = ai.debugStage();
            String flags = "o" + (ai.debugInOrbitBand() ? "1" : "0") + " e" + (ai.debugInEntryWindow() ? "1" : "0") + " c" + (ai.debugClaimBlocked() ? "1" : "0");
            String fire = "t" + (ai.debugWeaponHasTarget() ? "1" : "0") +
                    " s" + (ai.debugWeaponAllowShoot() ? "1" : "0") +
                    " f" + (ai.debugWeaponAllowFire() ? "1" : "0");

            Color stateColor = ai.isLanding() ? Pal.accent : Pal.power;
            Drawn.overlayText(
                    "r" + runway + " " + stage + " rc " + ai.debugRecallReason() + " dT " + Strings.fixed(distTail, 0) + " p " + Strings.fixed(orbitAngleErr, 0) + " " + flags,
                    fighter.x, fighter.y + fighter.hitSize + 14f, 0f, stateColor, false
            );
            Drawn.overlayText(
                    "v " + Strings.fixed(velLen, 2) + " rot " + Strings.fixed(fighter.rotation, 0) + " tk " + Strings.fixed(ai.debugTakeoffTimer(), 1) +
                            " rg " + Strings.fixed(ai.debugRecallGraceTimer(), 1) + " nt " + Strings.fixed(ai.debugNoTargetTimer(), 1) + " " + fire,
                    fighter.x, fighter.y + fighter.hitSize + 6f, 0f, Color.white, false
            );

            Lines.stroke(1.2f, stateColor);
            Lines.line(fighter.x, fighter.y, Tmp.v1.x, Tmp.v1.y);
            Drawf.dashLine(Pal.heal, hudFront.x, hudFront.y, hudLaunch.x, hudLaunch.y);
            Draw.color(Pal.heal);
            Fill.circle(hudFront.x, hudFront.y, 2f);
            Fill.circle(hudLaunch.x, hudLaunch.y, 2.6f);

            if (ai.debugTakeoffTimer() > 0f) {
                Drawf.dashLine(Pal.place, hudTakeoffFrom.x, hudTakeoffFrom.y, hudTakeoffTo.x, hudTakeoffTo.y);
                Draw.color(Pal.place);
                Fill.circle(hudTakeoffFrom.x, hudTakeoffFrom.y, 1.8f);
                Fill.circle(hudTakeoffTo.x, hudTakeoffTo.y, 2.2f);
            }
            if(ai.isReturning() && showReverse){
                Drawf.dashLine(Pal.place, Tmp.v1.x, Tmp.v1.y, hudReverse.x, hudReverse.y);
                Draw.color(Pal.place);
                Fill.circle(hudReverse.x, hudReverse.y, 1.8f);
            }
            Draw.color(Color.scarlet);
            Fill.circle(Tmp.v1.x, Tmp.v1.y, 2.2f);
            Draw.reset();
        });
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
        addShockCircle(x, y, r, lifetime, WHShaders.convex == null ? 0f : WHShaders.convex.strength);
    }

    public static void addShockCircle(float x, float y, float r, float lifetime, float strength){
        float scaledStrength = resolveDistortionStrength(strength, WHShaders.convex != null, true);
        if(scaledStrength <= 0.0001f || WHShaders.convex == null) return;
        WHShaders.convex.add(x, y, r, lifetime, scaledStrength);
    }

    public static void addShockRect(float x, float y, float length, float width, float angle, float lifetime){
        addShockRect(x, y, length, width, angle, lifetime, WHShaders.convexRect == null ? 0f : WHShaders.convexRect.strength);
    }

    public static void addShockRect(float x, float y, float length, float width, float angle, float lifetime, float strength){
        float scaledStrength = resolveDistortionStrength(strength, WHShaders.convexRect != null, false);
        if(scaledStrength <= 0.0001f || WHShaders.convexRect == null) return;
        WHShaders.convexRect.addRect(x, y, length, width, angle, lifetime, scaledStrength);
    }

    private static float resolveDistortionStrength(float baseStrength, boolean shaderReady, boolean circle){
        if(Vars.headless || !WHSettings.distortionEnabled()) return 0f;
        float scale = WHSettings.distortionStrengthScale();
        if(scale <= 0.0001f) return 0f;

        if(!shaderReady){
            if(circle && !warnedConvexMissing){
                warnedConvexMissing = true;
                Log.warn("Convex lens shader is null; WHShaders.init() may have failed.");
            }else if(!circle && !warnedRectMissing){
                warnedRectMissing = true;
                Log.warn("Rect lens shader is null; WHShaders.init() may have failed.");
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
                buffer.blit(Shaders.screenspace);
            }
            buffer2.end();

            boolean hasCircleDistortion = WHShaders.convex != null && WHShaders.convex.hasAny();
            boolean hasRectDistortion = WHShaders.convexRect != null && WHShaders.convexRect.hasAny();

            if(hasCircleDistortion && hasRectDistortion){
                buffer.resize(graphics.getWidth(), graphics.getHeight());
                buffer.begin(Color.clear);
                WHShaders.convex.blitFrom(buffer2);
                buffer.end();
                WHShaders.convexRect.blitFrom(buffer);
            }else if(hasCircleDistortion){
                WHShaders.convex.blitFrom(buffer2);
            }else if(hasRectDistortion){
                WHShaders.convexRect.blitFrom(buffer2);
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
