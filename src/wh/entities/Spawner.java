package wh.entities;

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
import mindustry.world.blocks.environment.*;
import wh.content.*;
import wh.gen.*;
import wh.graphics.*;
import wh.util.*;

import java.nio.*;

import static mindustry.Vars.*;

public class Spawner extends WHBaseEntity implements Syncc, Timedc, Rotc{
    public Team team = Team.derelict;
    public UnitType type = UnitTypes.alpha;
    public float time = 0, lifetime;
    public float surviveTime, surviveLifetime = 3000f;
    public float rotation;

    public double flagToApply = Double.NaN;

    public StatusEntry statusEntry = new StatusEntry().set(StatusEffects.none, 0);

    public Interval timer = new Interval();

    public float trailProgress = Mathf.random(360);

    public long lastUpdated, updateSpacing;

    public SoundLoop soundLoop;
    public Unit toSpawn;
    public Vec2 commandPos = new Vec2(Float.NaN, Float.NaN);

    public final Seq<Trail> trails = Seq.with(new Trail(30), new Trail(50), new Trail(70));
    public float trailWidth = 3f;

    public static float delaySpawnTime = 12f;

    public boolean airdrop;
    public float airDropTime = 110f;
    public boolean effectTriggered;

    @Override
    public float clipSize(){
        return drawSize + 500;
    }

    public Spawner init(UnitType type, Team team, Position pos, float rotation, float lifetime, boolean airdrop){
        this.type = type;
        this.airdrop = airdrop;
        this.lifetime = lifetime;
        this.rotation = rotation;
        this.team = team;
        this.drawSize = type.hitSize;
        trailWidth = Mathf.clamp(drawSize / 15f, 1.25f, 4f);
        set(pos);

        return this;
    }

    public Spawner setStatus(StatusEffect status, float statusDuration){
        statusEntry.effect = status;
        statusEntry.time = statusDuration;

        return this;
    }

    public Spawner setFlagToApply(double flagToApply){
        this.flagToApply = flagToApply;
        return this;
    }

    public Spawner setFlagToApply(long flagToApply){
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
            surviveTime = 0;
            if(!headless){
                trailProgress += Time.delta * (0.45f + fin(Interp.pow3In) * 2f);
                for(int i = 0; i < trails.size; i++){

                  /*  float x= ((fin() + 1) / 2 * drawSize * (1 + 0.5f * i) + Mathf.sinDeg(trailProgress * (1 + 0.5f * i)) * drawSize / 2) * (fout(Interp.pow3) * 7 + 1) / 8,
                    y = (fout(Interp.swing) * fout(Interp.swingOut) * drawSize / 3 * fout()) + this.y;
                    Tmp.v1.trns(trailProgress * (i + 1) * 1.5f + i * 360f / trails.size + Mathf.randomSeed(id, 360),
                    ((fin() + 1) / 2 * drawSize * (1 + 0.5f * i) + Mathf.sinDeg(trailProgress * (1 + 0.5f * i)) * drawSize / 2) * (fout(Interp.pow3) * 7 + 1) / 8,
                    fin(Interp.swing) * fout(Interp.swingOut) * drawSize / 3 * fout()).add(this);*/

                    Trail trail = trails.get(i);
                    float angle = (i + 1) * 360f / trails.size + Mathf.randomSeed(id, 360);

                    WHUtils.superEllipse((360 + angle) * fin(Interp.smooth), ((2 + 0.4f * (i + 1)) * drawSize / 4 * (i + 1)) * fout() * Mathf.curve(fin(), 0, 0.05f), x, y,
                    45, Tmp.v1);

                    trail.update(Tmp.v1.x, Tmp.v1.y, (fout(0.25f) * 2 + 1) / 3);
                }
            }
            Floor floor = Vars.world.floorWorld(x, y);
            if(floor != null && Mathf.chance(0.2f) && airdrop && time >= lifetime - airDropTime){
                WHFx.AirDropLandDust.at(x, y, Mathf.range(360f), floor.mapColor.cpy().mul(1f + Mathf.range(0.25f)));
            }
            if(airdrop && !type.flying && time >= lifetime - airDropTime && !effectTriggered){
                WHFx.airDropOut.at(x, y, rotation(), team.color, type);
                effectTriggered = true;
            }
        }else surviveTime += Time.delta;

        if(surviveTime > surviveLifetime) remove();

        if(type.flying){
            if(time > lifetime - delaySpawnTime + 1f && !effectTriggered){
                jumpIn();
                effectTriggered = true;
            }
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
        toSpawn.apply(StatusEffects.unmoving, Fx.unitSpawn.lifetime);
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
        if(type.health > 8000 && team != Vars.player.team()) ;
        TextureRegion pointerRegion = WHContent.pointerRegion, arrowRegion = WHContent.arrowRegion;

        Drawf.light(x, y, clipSize() * fout(), team.color, 0.7f);
        Draw.z(Layer.effect - 1f);

        boolean can = canCreate();

        float regSize = WHUtils.regSize(type);
        Draw.color(can ? team.color : Tmp.c1.set(team.color).lerp(Pal.ammo, Mathf.absin(Time.time * Drawn.sinScl, 8f, 0.3f) + 0.1f));

        for(int i = -4; i <= 4; i++){
            if(i == 0) continue;
            Tmp.v1.trns(rotation, i * tilesize * 2);
            float f = (100 - (Time.time - 12.5f * i) % 100) / 100;
            Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, arrowRegion.width * (regSize / 2f + Draw.scl) * f, arrowRegion.height * (regSize / 2f + Draw.scl) * f, rotation() - 90);
        }

        if(can){
            trails.each(t -> {
                t.drawCap(team.color, trailWidth);
                t.draw(team.color, trailWidth);
            });
        }

        if(can)
            Drawn.overlayText(Fonts.tech, String.valueOf(Mathf.ceil((lifetime - time) / 60f)), x, y, 0, 0, 0.25f, team.color, false, true);
        else{
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
