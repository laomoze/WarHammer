package wh.entities;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.ai.types.*;
import mindustry.audio.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.ui.*;
import wh.content.*;
import wh.gen.*;
import wh.graphics.*;

import java.nio.*;

import static mindustry.Vars.headless;
import static wh.util.WHUtils.rand;

public class RiftSpawner extends WHBaseEntity implements Syncc, Timedc, Rotc{
    public Team team = Team.derelict;
    public UnitType type = UnitTypes.alpha;
    public float lifetime = 600;
    public float riftTime = 240;
    public float surviveTime, surviveLifetime = 3000f;
    public float rotation;

    public double flagToApply = Double.NaN;
    /** Negative value means keep UnitType default shield. */
    public float shieldToApply = -1f;
    /** Forward offset for jump-out icon, as a multiple of hitSize. */
    public float emergeForward = 0.25f;
    /** Initial velocity multiplier based on unit type speed. */
    public float spawnInitialSpeed = 0.7f;

    public StatusEntry statusEntry = new StatusEntry().set(StatusEffects.none, 0);

    public Effect riftEffect = Fx.none;

    public Interval timer = new Interval();
    public float trailProgress = Mathf.random(360f);
    public float trailProgress2 = Mathf.random(360f);

    public long lastUpdated, updateSpacing;

    public SoundLoop soundLoop;
    public Unit toSpawn;
    public Vec2 commandPos = new Vec2(Float.NaN, Float.NaN);

    public final Seq<Trail> trails = Seq.with(new Trail(30), new Trail(50), new Trail(70));
    public final Seq<Trail> trails2 = Seq.with(new Trail(40), new Trail(50), new Trail(70));
    public float trailWidth = 3;

    private boolean effectTriggered = false;
    private final progress1 pg1 = new progress1();
    private final progress2 pg2 = new progress2();
    private float time = 0;

    @Override
    public float clipSize(){
        return drawSize + 500f;
    }

    public RiftSpawner init(UnitType type, Team team, Position pos, float rotation, float lifetime){
        this.type = type;
        this.lifetime = lifetime;
        this.rotation = rotation;
        this.team = team;
        this.drawSize = type.hitSize;
        this.trailWidth = Mathf.clamp(drawSize / 15f, 1.25f, 4f);
        set(pos);
        return this;
    }

    public RiftSpawner setStatus(StatusEffect status, float statusDuration){
        statusEntry.effect = status;
        statusEntry.time = statusDuration;
        return this;
    }

    public RiftSpawner setFlagToApply(double flagToApply){
        this.flagToApply = flagToApply;
        return this;
    }

    public RiftSpawner setFlagToApply(long flagToApply){
        this.flagToApply = Double.longBitsToDouble(flagToApply);
        return this;
    }

    public RiftSpawner setShieldToApply(float shieldToApply){
        this.shieldToApply = shieldToApply;
        return this;
    }

    public RiftSpawner setEmergeForward(float emergeForward){
        this.emergeForward = emergeForward;
        return this;
    }

    public RiftSpawner setSpawnInitialSpeed(float spawnInitialSpeed){
        this.spawnInitialSpeed = spawnInitialSpeed;
        return this;
    }

    @Override
    public void add(){
        super.add();
        Groups.sync.add(this);
        WHFx.spawnWave.at(x, y, drawSize * 1.1f, team.color);
    }

    public class progress1 implements Scaled{
        @Override
        public float fin(){
            return preRiftProgress();
        }
    }

    public class progress2 implements Scaled{
        @Override
        public float fin(){
            return riftProgress();
        }
    }

    @Override
    public void remove(){
        super.remove();
        effectTriggered = false;
        Groups.sync.remove(this);

        if(Vars.net.client()){
            Vars.netClient.addRemovedEntity(id());
        }

        if(soundLoop != null) soundLoop.update(x, y, false);
    }

    @Override
    public void update(){
        if(canCreate()){
            time += Time.delta;
            surviveTime = 0f;

            if(!headless){
                if(time < lifetime - riftTime){
                    trailProgress += Time.delta * (0.75f + pg1.fin(Interp.pow4In) * 2.4f);
                    for(int i = 0; i < trails.size; i++){
                        Trail trail = trails.get(i);
                        Tmp.v1.trns(trailProgress * (i + 1) * 1.5f + i * 360f / trails.size + Mathf.randomSeed(id, 360),
                        ((pg1.fin() + 1) / 2 * drawSize * (1 + 0.5f * i) + Mathf.sinDeg(trailProgress * (1 + 0.5f * i)) * drawSize / 2) * (pg1.fout(Interp.pow3) * 7 + 1) / 8, pg1.fin(Interp.swing) * pg1.fout(Interp.swingOut) * drawSize / 3 * fout()).add(this);
                        trail.update(Tmp.v1.x, Tmp.v1.y, (pg1.fout(0.25f) * 2 + 1) / 3);
                    }
                }else{
                    trailProgress2 += Time.delta * (0.75f + pg2.fin(Interp.pow2In) * 2.4f);
                    for(int i = 0; i < trails2.size; i++){
                        Trail trail = trails2.get(i);
                        float offset = (float)360 / trails2.size * i;
                        Tmp.v1.trns(trailProgress2 + offset, pg2.fout(Interp.smooth) * drawSize * (1 + 0.5f * i) + Mathf.sinDeg(trailProgress * (1 + 0.5f * i)) * drawSize / 2).add(this);
                        trail.update(Tmp.v1.x, Tmp.v1.y, (pg2.fout(0.25f) * 2 + 1) / 3);
                    }
                }
            }
            if(pg1.fin() >= 1f && Mathf.chanceDelta(0.3f)){
                createEffects();
            }
            if(pg2.fin() > 0.9f && !effectTriggered){
                dump();
                effect();
                effectTriggered = true;
            }
            if(time > lifetime) remove();
        }else{
            surviveTime += Time.delta;
        }
        if(surviveTime > surviveLifetime) remove();
    }

    public void drawJumPOut(float progress, float alpha){
        if(type.fullIcon == null && type.region == null) return;
        Draw.z(Layer.flyingUnit + 0.001f);
        TextureRegion region = type.fullIcon != null ? type.fullIcon : type.region;
        progress = Mathf.clamp(Interp.pow2Out.apply(progress));
        alpha = Mathf.clamp(alpha);
        if(alpha <= 0f || progress <= 0f) return;
        float drawRot = rotation() - 90f;

        float topV = region.v, bottomV = region.v2;
        float currentBottomV = Mathf.lerp(topV, bottomV, progress);

        Tmp.tr1.set(region);
        Tmp.tr1.set(region.u, topV, region.u2, currentBottomV);
        float offset = jumpOutForwardOffset(progress, region);
        Tmp.v2.trns(rotation(), offset);
        float drawX = x + Tmp.v2.x, drawY = y + Tmp.v2.y;

        float lineLen = Mathf.clamp(type.hitSize * 1.6f, 12f, type.hitSize * 2.4f);
        Tmp.v3.trns(drawRot, -lineLen / 2f, 0f);
        float lx1 = x + Tmp.v3.x, ly1 = y + Tmp.v3.y;
        Tmp.v3.trns(drawRot, lineLen / 2f, 0f);
        float lx2 = x + Tmp.v3.x, ly2 = y + Tmp.v3.y;

        Draw.color(1f, 1f, 1f, alpha);
        Draw.rect(Tmp.tr1, drawX, drawY, drawRot);
        Draw.z(WHFx.EFFECT_MASK);
        Draw.color(team.color);
        Lines.stroke((3f + Mathf.absin(6, 1) * 3f) * (1f - progress));
        Lines.line(lx1, ly1, lx2, ly2);
        Draw.reset();
        Draw.z(Layer.flyingUnit + 0.1f);
        Draw.alpha(0.7f);
        int curveCount = Mathf.clamp((int)(type.hitSize / 12f), 3, 6);
        for(int i = 0; i < curveCount; i++){
            Draw.color(team.color);
            drawJumpOutCurve(progress, i * 1145, lx1, ly1, lx2, ly2, drawX, drawY);
        }
        Draw.reset();
    }

    public void createEffects(){
        rand.setSeed(id);
        float lineLen = Mathf.clamp(type.hitSize * 1.6f, 12f, type.hitSize * 2.4f);
        Tmp.v3.trns(rotation() - 90f, Mathf.random(-lineLen / 2f, lineLen / 2f), 0f);
        WHFx.square(team.color.cpy(), 180, (int)Mathf.clamp(type.hitSize / 20, 1, 4),
        Mathf.clamp(type.hitSize / 4f, 20, 40), Mathf.clamp(type.hitSize / 10f, 2, 5))
        .layer(WHFx.EFFECT_MASK + 0.0001f).at(x + Tmp.v3.x, y + Tmp.v3.y, rotation());
    }

    private float jumpOutForwardOffset(float drawProgress, TextureRegion region){
        drawProgress = Mathf.clamp(drawProgress);
        float fullH = region.height * Draw.scl;
        float currentH = fullH * drawProgress;
        float extraPush = fullH * emergeForward * Mathf.curve(drawProgress, 0.9f, 1f);
        return currentH * 0.5f + extraPush;
    }

    private void drawJumpOutCurve(float progress, int index, float lx1, float ly1, float lx2, float ly2, float x2, float y2){
        rand.setSeed(id + index);
        float waveTime = Time.time / 2f;
        float jitterScl = 0.2f + 0.8f * Interp.pow2Out.apply(1f - progress);

        float lineT = rand.random(0.12f, 0.88f);
        float startX = Mathf.lerp(lx1, lx2, lineT);
        float startY = Mathf.lerp(ly1, ly2, lineT);
        float tailAngle = Angles.angle(lx1, ly1, lx2, ly2);

        float endAngle = rotation() + rand.range(180f) + Mathf.sinDeg(waveTime * 3.6f + rand.random(360f)) * 14f;
        Tmp.v4.trns(endAngle, rand.random(0.5f * type.hitSize * jitterScl));
        Tmp.v5.trns(tailAngle + 90, rand.random(0.5f * type.hitSize * progress));
        float endX = x2 + Tmp.v4.x + Tmp.v5.x;
        float endY = y2 + Tmp.v4.y + Tmp.v5.y;

        float dir = Angles.angle(startX, startY, endX, endY);
        float dist = Mathf.dst(startX, startY, endX, endY);
        float side = rand.chance(0.5f) ? 1f : -1f;
        float sideWave = Mathf.clamp(type.hitSize * 0.38f, 4f, type.hitSize * 0.9f)
        * jitterScl * (0.7f + 0.3f * Mathf.sinDeg(waveTime * 3.2f + rand.random(360f)));

        float cx1 = startX + Angles.trnsx(dir, dist * 0.36f) + Angles.trnsx(dir + 90f * side, sideWave);
        float cy1 = startY + Angles.trnsy(dir, dist * 0.36f) + Angles.trnsy(dir + 90f * side, sideWave);
        float cx2 = startX + Angles.trnsx(dir, dist * 0.78f) + Angles.trnsx(dir - 90f * side, sideWave * 0.6f);
        float cy2 = startY + Angles.trnsy(dir, dist * 0.78f) + Angles.trnsy(dir - 90f * side, sideWave * 0.6f);

        Lines.stroke(Mathf.clamp(type.hitSize / 16f, 1.5f, 3) * Mathf.curve(1f - progress, 0, 0.1f));
        Lines.curve(startX, startY, cx1, cy1, cx2, cy2, endX, endY, Math.max(6, (int)(dist / 5f)));

        float ringRadius = Mathf.clamp(type.hitSize * 0.12f, 2.5f, 4)
        * (0.8f + 0.2f * Mathf.sinDeg(waveTime * 8f + index));
        Fill.circle(endX, endY, ringRadius * Mathf.curve(1f - progress, 0, 0.1f));
        Draw.reset();
    }

    private float preRiftProgress(){
        return Mathf.clamp(time / Math.max(lifetime - riftTime, 1f));
    }

    private float riftProgress(){
        return Mathf.clamp((time - (lifetime - riftTime)) / Math.max(riftTime, 1f));
    }

    public void drawRift(float scl, Color color, float z, float alpha, int index, float progress, boolean blend){
        progress = Mathf.clamp(progress);
        if(progress <= 0f) return;
        rand.setSeed(id + index);

        float halfHit = type.hitSize * 0.5f * scl;
        float sides = 7 + rand.random(4);
        float step = 360f / Math.max(sides, 1);
        float rot = rotation + rand.random(360f) + Time.time * 0.3f;

        Draw.z(z);
        Draw.color(color, Mathf.clamp(alpha));
        Fill.polyBegin();
        for(int p = 0; p < sides; p++){
            float pointJitter = rand.random(0.8f, 1.25f);
            float wave = Mathf.absin(Time.time * rand.random(1, 2), rand.random(6, 10), pointJitter);

            float radius = (halfHit + halfHit * Mathf.clamp(wave * pointJitter)) * progress;
            float ang = rot + p * step;
            Fill.polyPoint(x + Angles.trnsx(ang, radius), y + Angles.trnsy(ang, radius));
        }
        Fill.polyEnd();
        Draw.reset();
    }

    public void drawRiftLine(float scl, Color color, float z, float alpha, int index, float progress){
        progress = Mathf.clamp(progress);
        alpha = Mathf.clamp(alpha);
        if(progress <= 0f || alpha <= 0f) return;
        rand.setSeed(id + index);

        float halfHit = type.hitSize * 0.5f * scl;
        int sides = 7 + rand.random(4);
        float step = 360f / Math.max(sides, 1);
        float rot = rotation + rand.random(360f) + Time.time * 0.3f;

        Draw.z(z);
        Draw.color(color, alpha);
        Draw.blend(Blending.additive);
        Lines.stroke(Mathf.clamp(type.hitSize / 10, 1.5f, 3.5f) * progress);
        Lines.beginLine();
        for(int p = 0; p < sides; p++){
            float pointJitter = rand.random(0.8f, 1.25f);
            float wave = Mathf.absin(Time.time * rand.random(1, 2), rand.random(6, 10), pointJitter);
            float radius = (halfHit + halfHit * Mathf.clamp(wave * pointJitter)) * progress;
            float ang = rot + p * step;
            Lines.linePoint(x + Angles.trnsx(ang, radius), y + Angles.trnsy(ang, radius));
        }
        Draw.blend(Blending.normal);
        Lines.endLine(true);
        Draw.reset();
    }

    public void effect(){
        Effect.shake(type.hitSize / 3f, type.hitSize / 4f, toSpawn);
        toSpawn.apply(StatusEffects.slow, WHFx.jumpTrail.lifetime);
        Fx.unitSpawn.at(toSpawn.x, toSpawn.y, rotation(), type);
        WHFx.spawn.at(toSpawn.x, toSpawn.y, type.hitSize, team.color);
        /*   if(type.flying)WHFx.jumpTrail.at(toSpawn.x, toSpawn.y, rotation(),team.color, type);*/
    }

    public void dump(){
        toSpawn = type.create(team);
        TextureRegion region = type.fullIcon != null ? type.fullIcon : type.region;
        float drawProgress = Mathf.clamp(Interp.pow2Out.apply(riftProgress()));
        float offset = region == null ? 0f : jumpOutForwardOffset(drawProgress, region);
        Tmp.v1.trns(rotation(), offset);
        toSpawn.set(Tmp.v1.x + x, Tmp.v1.y + y);
        toSpawn.rotation = rotation();
        float initialSpeed = Math.max(0f, spawnInitialSpeed) * Math.max(0.1f, toSpawn.type.speed);
        if(initialSpeed > 0f){
            toSpawn.vel.add(Tmp.v3.trns(rotation(), initialSpeed));
        }

        if(!Double.isNaN(flagToApply)){
            toSpawn.flag(flagToApply);
        }
        if(shieldToApply >= 0f){
            toSpawn.shield = shieldToApply;
        }
        if(!Vars.net.client()) toSpawn.add();

        toSpawn.apply(StatusEffects.unmoving, 120f);
        toSpawn.apply(statusEntry.effect, statusEntry.time);

        if(commandPos != null && !commandPos.isNaN()){
            if(toSpawn.isCommandable()){
                toSpawn.command().commandPosition(commandPos);
            }else{
                CommandAI ai = new CommandAI();
                ai.commandPosition(commandPos);
                toSpawn.controller(ai);
            }
        }

        Events.fire(new EventType.UnitCreateEvent(toSpawn, null));
    }

    public boolean canCreate(){
        return Units.canCreate(team, type) || team == Vars.state.rules.waveTeam;
    }

    @Override
    public void draw(){
        boolean can = canCreate();

        Drawf.light(x, y, clipSize() * fout(), team.color, 0.72f);
        if(can){
            float riftFin = pg2.fin(Interp.pow10Out);
            float fadeOut = 1 - Mathf.curve(time / lifetime, 0.9f, 1);

            for(int i = 1; i < 5; i++){
                drawRift((1f + 0.35f * i) * fadeOut, team.color,
                WHFx.EFFECT_BOTTOM - 0.0002f - 0.01f * i, 0.08f * (5 - i) * fadeOut, i * 1000, riftFin, false);
                drawRiftLine((0.99f + 0.35f * i) * fadeOut, team.color,
                Layer.effect - 0.001f, 0.18f * (5 - i) * fadeOut, i * 1000, riftFin);
            }
            drawRift(1.16f * fadeOut, team.color, WHFx.EFFECT_BOTTOM, fadeOut, 0, riftFin, false);
            drawRift(1.1f * fadeOut, Pal.coalBlack, WHFx.EFFECT_BOTTOM, fadeOut, 0, riftFin, false);

            drawRift(0.55f * fadeOut, team.color, WHFx.EFFECT_BOTTOM + 0.0001f, fadeOut, 1, riftFin, false);
            drawRift(0.5f * fadeOut, Pal.coalBlack, WHFx.EFFECT_BOTTOM + 0.0001f, fadeOut, 1, riftFin, false);

            drawJumPOut(pg2.fin(), 1);

            Draw.z(WHFx.EFFECT_MASK);
            Drawf.light(x, y, type.hitSize * 3 * fadeOut, team.color, 0.72f);
            trails.each(t -> {
                t.drawCap(team.color, trailWidth * pg1.fout(Interp.pow5Out));
                t.draw(team.color, trailWidth * pg1.fout(Interp.pow5Out));
            });

            if(time >= lifetime - riftTime) trails2.each(t -> {
                t.drawCap(team.color, trailWidth * pg2.fout(Interp.pow5Out));
                t.draw(team.color, trailWidth * pg2.fout(Interp.pow5Out));
            });

            Drawn.overlayText(
            Fonts.tech,
            String.valueOf(Mathf.ceil(Mathf.clamp((lifetime - riftTime) - time, 0, lifetime - riftTime) / 60f)),
            x, y, 0f, 0f, Mathf.clamp(type.hitSize / 12, 0.15f, 0.6f), team.color, false, true
            );
        }else{
            Draw.z(Layer.effect);
            Draw.color(Pal.ammo);

            float s = Mathf.clamp(drawSize / 4f, 12f, 20f);
            Draw.rect(Icon.warning.getRegion(), x, y, s, s);
        }
        Draw.reset();
    }


    @Override
    public void write(Writes write){
        super.write(write);
        write.bool(effectTriggered);
        write.f(lifetime);
        write.f(time);
        write.f(rotation);
        write.f(surviveTime);
        write.d(flagToApply);
        TypeIO.writeUnitType(write, type);
        TypeIO.writeTeam(write, team);
        TypeIO.writeStatus(write, statusEntry);
        TypeIO.writeVec2(write, commandPos);
    }

    @Override
    public void read(Reads read){
        super.read(read);
        effectTriggered = read.bool();
        lifetime = read.f();
        time = read.f();
        rotation = read.f();
        surviveTime = read.f();
        flagToApply = read.d();

        type = TypeIO.readUnitType(read);
        team = TypeIO.readTeam(read);
        statusEntry = TypeIO.readStatus(read);
        commandPos = TypeIO.readVec2(read);

        afterRead();
    }

    @Override
    public boolean serialize(){
        return true;
    }

    @Override
    public int classId(){
        return EntityRegister.getId(getClass());
    }

    @Override
    public void snapSync(){
    }

    @Override
    public void snapInterpolation(){
    }

    @Override
    public void readSync(Reads read){
        x = read.f();
        y = read.f();
        effectTriggered = read.bool();
        lifetime = read.f();
        time = read.f();
        rotation = read.f();
        surviveTime = read.f();

        type = TypeIO.readUnitType(read);
        team = TypeIO.readTeam(read);
        if(commandPos != null) commandPos = TypeIO.readVec2(read);
        else commandPos = new Vec2(Float.NaN, Float.NaN);

        afterSync();
    }

    @Override
    public void writeSync(Writes write){
        write.f(x);
        write.f(y);
        write.bool(effectTriggered);
        write.f(lifetime);
        write.f(time);
        write.f(rotation);
        write.f(surviveTime);

        TypeIO.writeUnitType(write, type);
        TypeIO.writeTeam(write, team);
        TypeIO.writeVec2(write, commandPos);
    }

    @Override
    public void readSyncManual(FloatBuffer floatBuffer){
    }

    @Override
    public void writeSyncManual(FloatBuffer floatBuffer){
    }

    @Override
    public void afterSync(){
    }

    @Override
    public void handleSyncHidden(){
    }

    @Override
    public void interpolate(){
    }

    @Override
    public boolean isSyncHidden(Player player){
        return false;
    }

    @Override
    public long lastUpdated(){
        return lastUpdated;
    }

    @Override
    public void lastUpdated(long l){
        lastUpdated = l;
    }

    @Override
    public long updateSpacing(){
        return updateSpacing;
    }

    @Override
    public void updateSpacing(long l){
        updateSpacing = l;
    }

    @Override
    public float fin(){
        return time / lifetime;
    }

    @Override
    public float time(){
        return time;
    }

    @Override
    public void time(float v){
        time = v;
    }

    @Override
    public float lifetime(){
        return lifetime;
    }

    @Override
    public void lifetime(float v){
        lifetime = v;
    }

    @Override
    public float rotation(){
        return rotation;
    }

    @Override
    public void rotation(float v){
        rotation = v;
    }

    @Override
    public Building buildOn(){
        return Vars.world.buildWorld(x, y);
    }

    public void setType(UnitType type){
        this.type = type;
    }
}

