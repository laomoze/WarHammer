/*
package wh.entities.event;

import arc.*;
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
import wh.entities.*;
import wh.gen.*;
import wh.graphics.*;

import java.nio.*;

import static mindustry.Vars.*;
import static wh.util.WHUtils.rand;

public class RiftSpawner extends WHBaseEntity implements Syncc, Timedc, Rotc{
    public Team team = Team.derelict;
    public UnitType type = UnitTypes.alpha;
    public float time = 0f, lifetime;
    public float surviveTime, surviveLifetime = 3000f;
    public float rotation;

    public double flagToApply = Double.NaN;

    public StatusEntry statusEntry = new StatusEntry().set(StatusEffects.none, 0f);

    public Interval timer = new Interval();
    public float trailProgress = Mathf.random(360f);

    public long lastUpdated, updateSpacing;

    public SoundLoop soundLoop;
    public Unit toSpawn;
    public Vec2 commandPos = new Vec2(Float.NaN, Float.NaN);
    
    public final Seq<Trail> trails = Seq.with(new Trail(30), new Trail(50), new Trail(70));
    public float trailWidth = 2.5f;

    public static float spawnTime = 70f;

    public boolean airdrop;
    public float airDropTime = 110f;
    public boolean effectTriggered;

    @Override
    public float clipSize(){
        return drawSize + 500f;
    }

    public RiftSpawner init(UnitType type, Team team, Position pos, float rotation, float lifetime, boolean airdrop){
        this.type = type;
        this.airdrop = airdrop;
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

    @Override
    public void add(){
        super.add();
        Groups.sync.add(this);
        WHFx.spawnWave.at(x, y, drawSize * 1.1f, team.color);
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
                trailProgress += Time.delta * (0.75f + fin(Interp.pow2In) * 2.4f);

                rand.random(id());
                float random=rand.random(0.5f,1f);
                for(int i = 0; i < trails.size; i++){
                    Trail trail = trails.get(i);

                    float startRad = drawSize*random * (1.7f + i * 0.55f);
                    float endRad = drawSize*random * 0.18f;
                    float rad = Mathf.lerp(startRad, endRad, fin(Interp.pow2In));

                    float spin = trailProgress * (1.15f + i * 0.45f);
                    float ang = rotation + rand.random(360f) + spin + i * (360f / trails.size);

                    Tmp.v1.trns(ang, rad).add(x, y);

                    float widthMul = (fout(0.25f) * 2f + 1f) / 3f;
                    trail.update(Tmp.v1.x, Tmp.v1.y, widthMul);
                }
                updateTerminalFx();
            }

            if( time >= lifetime - airDropTime && !effectTriggered){
                jumpInRift.at(x, y, rotation(), team.color, type);
                effectTriggered = true;
            }
        }else{
            surviveTime += Time.delta;
        }

        if(surviveTime > surviveLifetime) remove();

        if(type.flying){
           */
/* if(time > lifetime - spawnTime + 1f && !effectTriggered){
                jumpIn();
                effectTriggered = true;
            }*//*

            if(time > lifetime){
                dump();
                effect();
                remove();
            }
        }else{
            if(time > lifetime){
                dump();
                effect();
                remove();
            }
        }
    }

    public static Effect jumpInRift = new Effect(RiftSpawner.spawnTime, e -> {
        if(!(e.data instanceof UnitType type)) return;
        TextureRegion region = type.fullIcon != null ? type.fullIcon : type.region;
        if(region == null) return;

        float enter = Interp.pow3Out.apply(e.fin());
        float unitW = Math.max(16f, type.hitSize * 2.15f);
        float unitH = unitW * (region.height / Math.max(region.width, 1f));
        Tmp.tr1.u

        float reveal = 0.25f + 0.75f * enter;
        float rw = unitW * reveal;

        // 从裂隙内部推出
        Tmp.v1.trns(e.rotation - 90f, -(1f - enter) * type.hitSize * 1.35f).add(e.x, e.y);

        Draw.z(Layer.flyingUnit + 0.02f);
        Draw.color();
        Draw.alpha(0.2f + 0.8f * enter);
        Draw.rect(region, Tmp.v1.x, Tmp.v1.y, rw, unitH, e.rotation - 90f);
    });

    public void jumpIn(){
        WHFx.jumpIn.at(x, y, rotation(), team.color, type);
    }

    public void effect(){
        Effect.shake(type.hitSize / 3f, type.hitSize / 4f, toSpawn);

        if(type.flying){
            WHFx.jumpTrail.at(toSpawn.x, toSpawn.y, rotation(), team.color, type);
        }else{
            toSpawn.apply(StatusEffects.slow, WHFx.jumpTrail.lifetime);
            Fx.unitSpawn.at(toSpawn.x, toSpawn.y, rotation(), type);
            WHFx.spawn.at(x, y, type.hitSize, team.color);
        }

        if(!headless){
            for(int i = 0; i < trails.size; i++){
                Trail trail = trails.get(i);
                Fx.trailFade.at(x, y, trailWidth, team.color, trail.copy());
            }
        }
    }

    public void dump(){
        toSpawn = type.create(team);
        toSpawn.set(x, y);
        toSpawn.rotation = rotation();

        if(!Double.isNaN(flagToApply)){
            toSpawn.flag(flagToApply);
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

        drawRiftBody(can);
        drawEnteringUnit(can);

        Draw.z(Layer.effect + 0.01f);
        if(can){
            trails.each(t -> {
                t.drawCap(team.color, trailWidth);
                t.draw(team.color, trailWidth);
            });

            Drawn.overlayText(
            Fonts.tech,
            String.valueOf(Mathf.ceil((lifetime - time) / 60f)),
            x, y, 0f, 0f, 0.25f, team.color, false, true
            );
        }else{
            Draw.z(Layer.effect);
            Draw.color(Pal.ammo);

            float s = Mathf.clamp(drawSize / 4f, 12f, 20f);
            Draw.rect(Icon.warning.getRegion(), x, y, s, s);
        }

        Draw.reset();
    }

    private void updateTerminalFx(){
        if(!type.flying) return;

        float enter = Mathf.clamp((time - (lifetime - spawnTime)) / Math.max(spawnTime, 1f));
        if(enter <= 0f || enter >= 1f) return;

        float eased = Interp.pow3Out.apply(enter);
        float unitW = Math.max(16f, drawSize * 2.15f);
        float rw = unitW * (0.25f + 0.75f * eased);

        float travel = (1f - eased) * drawSize * 1.35f;
        Tmp.v1.trns(rotation() - 90f, -travel);
        float ux = x + Tmp.v1.x;
        float uy = y + Tmp.v1.y;

        float span = Math.max(6f, drawSize);
        float localY = Mathf.range(span * 0.5f);
        float localX = rw * 0.5f;

        Tmp.v2.set(localX, localY).rotate(rotation() - 90f).add(ux, uy);
        if(Mathf.chanceDelta(0.08f))WHFx.square(team.color, 60f, 2, 20f, 8f).at(Tmp.v2.x, Tmp.v2.y);
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
    public void snapSync(){}

    @Override
    public void snapInterpolation(){}

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
    public void readSyncManual(FloatBuffer floatBuffer){}

    @Override
    public void writeSyncManual(FloatBuffer floatBuffer){}

    @Override
    public void afterSync(){}

    @Override
    public void handleSyncHidden(){}

    @Override
    public void interpolate(){}

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
*/
