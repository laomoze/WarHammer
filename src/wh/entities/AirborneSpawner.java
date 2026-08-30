package wh.entities;

import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ai.types.CommandAI;
import mindustry.audio.SoundLoop;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.units.StatusEntry;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.io.TypeIO;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.world.blocks.environment.Floor;
import wh.content.WHContent;
import wh.content.WHFx;
import wh.content.WHUnitTypes;
import wh.gen.EntityRegister;
import wh.util.WHUtils;

import java.nio.FloatBuffer;

import static arc.graphics.g2d.Draw.z;
import static mindustry.Vars.tilesize;
import static wh.content.WHFx.rand;

public class AirborneSpawner extends WHBaseEntity implements Syncc, Timedc, Rotc{
    public Team team = Team.derelict;
    public UnitType type = UnitTypes.alpha;
    public final Seq<UnitType> spawnSeq = Seq.with(WHUnitTypes.M4A, WHUnitTypes.M4B, WHUnitTypes.M4C, WHUnitTypes.M4D);
    public final Seq<Unit> spawnedUnits = new Seq<>();
    public float lifetime = 80;
    public float surviveTime, surviveLifetime = 300;
    public float rotation = 0;
    public float size = 6;
    public final int MAX_SPAWN_COUNT = 4;

    public double flagToApply = Double.NaN;
    /** Negative value means keep UnitType default shield. */
    public float shieldToApply = -1f;

    public StatusEntry statusEntry = new StatusEntry().set(StatusEffects.none, 0);

    public Interval timer = new Interval();

    public long lastUpdated, updateSpacing;

    public SoundLoop soundLoop;
    public Unit toSpawn;
    public Vec2 commandPos = new Vec2(Float.NaN, Float.NaN);
    public @Nullable Cons<Unit> onSpawned;

    public float landingProgress1 = 0.7f, landingProgress2 = 0.85f;

    public float dropStartHeight = 800;
    public float dropImpactDamage = 1500;
    public Effect dropImpactDamageEffect = new MultiEffect(Fx.titanExplosionLarge, Fx.titanSmoke);
    public float dropImpactRadius = 72f;
    public float podSizeScale = 1.3f;

    // top right / bottom left thruster visuals
    public float thrusterLength = 53 / 4f, thrusterOffset = 10;
    public float thrusterSpacing = 14f;
    public float thrusterSize = 8;

    public TextureRegion dropPodRegion = WHContent.dropPod;
    public TextureRegion dropPodTeamRegion = WHContent.dropPodTeam;
    public TextureRegion thruster1Region = WHContent.dropPodSide1;
    public TextureRegion thruster1RegionTeam = WHContent.dropPodSideTeam1;
    public TextureRegion thruster2Region = WHContent.dropPodSide2;
    public TextureRegion thruster2RegionTeam = WHContent.dropPodSideTeam2;

    private boolean hasDropImpact = false;
    private boolean hasCreatUnits = false;
    private final DropFlame flame = new DropFlame();
    private float time = 0;

    @Override
    public float clipSize(){
        return drawSize + 1000;
    }

    public AirborneSpawner init(Team team, Position pos, float rotation, float lifetime, UnitType... type){
        this.lifetime = lifetime;
        this.rotation = rotation;
        this.team = team;
        setSpawnSeq(type);
        spawnedUnits.clear();
        hasDropImpact = false;
        hasCreatUnits = false;
        time = 0f;
        surviveTime = 0f;
        toSpawn = null;
        onSpawned = null;
        set(pos);

        return this;
    }

    public AirborneSpawner init(Team team, Position pos, float lifetime, UnitType... type){
        this.lifetime = lifetime;
        this.team = team;
        setSpawnSeq(type);
        spawnedUnits.clear();
        hasDropImpact = false;
        hasCreatUnits = false;
        time = 0f;
        surviveTime = 0f;
        toSpawn = null;
        onSpawned = null;
        set(pos);

        return this;
    }

    public AirborneSpawner init(Team team, Position pos, float lifetime){
        this.lifetime = lifetime;
        this.team = team;
        setSpawnSeq(type);
        spawnedUnits.clear();
        hasDropImpact = false;
        hasCreatUnits = false;
        time = 0f;
        surviveTime = 0f;
        toSpawn = null;
        onSpawned = null;
        set(pos);

        return this;
    }

    public AirborneSpawner setSpawnSeq(UnitType... units){
        spawnSeq.clear();
        if(units != null){
            for(UnitType unit : units){
                if(unit != null) spawnSeq.add(unit);
            }
        }
        if(spawnSeq.isEmpty()){
            spawnSeq.add(type == null ? UnitTypes.alpha : type);
        }
        spawnSeq.truncate(MAX_SPAWN_COUNT);
        return this;
    }

    public AirborneSpawner setStatus(StatusEffect status, float statusDuration){
        statusEntry.effect = status;
        statusEntry.time = statusDuration;

        return this;
    }

    public AirborneSpawner setFlagToApply(double flagToApply){
        this.flagToApply = flagToApply;
        return this;
    }

    public AirborneSpawner setFlagToApply(long flagToApply){
        this.flagToApply = Double.longBitsToDouble(flagToApply);
        return this;
    }

    public AirborneSpawner setShieldToApply(float shieldToApply){
        this.shieldToApply = shieldToApply;
        return this;
    }

    /**
     * Optional callback when this spawner creates a unit.
     */
    public AirborneSpawner onSpawned(@Nullable arc.func.Cons<Unit> callback) {
        this.onSpawned = callback;
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
        spawnedUnits.clear();
        onSpawned = null;
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
            updateDropPod();
        }else surviveTime += Time.delta;

        if(surviveTime > surviveLifetime) remove();
    }

    private void updateDropPod(){
        float progress = Mathf.clamp(fin(), 0f, 1f);
        Floor floor = Vars.world.floorWorld(x, y);

        if(floor != null && Mathf.chanceDelta(0.15f) && progress <= landingProgress1){
            WHFx.AirDropLandDust.at(x, y, Mathf.range(360f), floor.mapColor.cpy().mul(1f + Mathf.range(0.25f)));
        }

        if(Mathf.chanceDelta(0.25) && progress <= landingProgress1){
            float descend = Interp.pow3Out.apply(Mathf.curve(fin(), 0f, landingProgress1));
            float f = 1 - descend;
            float podY = f * dropStartHeight;
            rand.setSeed(id);
            Tmp.v1.trns(rand.random(360), rand.random(3, 10));
            WHFx.hugeSmokeGray.at(x + Tmp.v1.x, y + Tmp.v1.y + podY, Mathf.random(360f), 1f + Mathf.range(0.25f));
        }

        if(progress >= landingProgress1 && !hasDropImpact){
            triggerDropImpact();
        }

        if(progress >= landingProgress2 && !hasCreatUnits){
            spawnFromThrusters();
        }

        if(time > lifetime){
            remove();
        }
    }

    private void triggerDropImpact(){
        hasDropImpact = true;
        dropImpactDamageEffect.at(x, y, team.color);
        Damage.damage(team, x, y, dropImpactRadius, dropImpactDamage, false, false, true);
        Effect.shake(Math.max(drawSize / 2f, 6f), Math.max(drawSize / 3f, 4f), this);
        WHFx.spawn.at(x, y, Math.max(drawSize, dropImpactRadius * 0.5f), team.color);
    }

    @Override
    public void draw(){
        drawDropPod();
        Draw.reset();
    }

    private void drawDropPod(){

        rand.setSeed(id);
        float descend = Interp.pow3Out.apply(Mathf.curve(fin(), 0f, landingProgress1));
        float f = 1 - descend;
        float podY = f * dropStartHeight;
        float deploy = Mathf.curve(fin(), landingProgress1, landingProgress2);
        float alphaOut = (1 - Mathf.curve(fin(), landingProgress2, 1f)) * (!canCreate() ? surviveTime / surviveLifetime : 1);
        float rot = fout() * rand.random(45);

        Draw.z(Layer.blockUnder);
        Drawf.shadow(x, y, size * tilesize, descend);
        float strength = f * (0.95f + Mathf.absin(2f, 0.1f));
        float offset = (size - 4) * 2f;

        Draw.z(Layer.effect);
        for(int i = 0; i < 4; i++){
            Tmp.v1.trns(i * 90 + rot + 45, 1f);
            Tmp.v1.setLength((size * tilesize / 2f) + strength * 2f + offset);
            Draw.color(team.color);
            Fill.circle(Tmp.v1.x + x, Tmp.v1.y + podY + y, thrusterSize * strength);

            Tmp.v1.setLength((size * tilesize / 2f) + strength * 0.5f + offset);
            Draw.color(Color.white);
            Fill.circle(Tmp.v1.x + x, Tmp.v1.y + podY + y, thrusterSize / 2 * strength);
        }

        Tmp.v2.trns(90, podY);

        Drawf.light(x + Tmp.v3.x, y + Tmp.v3.y, drawSize * 2 * Math.max(fout(), 0.25f), team.color, 0.7f);

        Tmp.v3.trns(225f, f * 200);
        z(Layer.flyingUnit + 1);
        Draw.color(0, 0, 0, 0.5f * descend);
        Draw.rect(dropPodRegion, x + Tmp.v3.x, y + Tmp.v3.y, rot);

        Draw.z(Layer.weather - 0.01f);

        if(deploy > 0f){
            Draw.color();
            drawThrusters(1 - deploy, alphaOut, podY, rot);
        }

        Draw.color();
        Draw.alpha(alphaOut);
        Drawf.spinSprite(dropPodRegion, x, y + Tmp.v2.y, rot - 90f);

        if(dropPodTeamRegion.found()){
            Draw.color(team.color);
            Draw.alpha(alphaOut);
            Drawf.spinSprite(dropPodTeamRegion, x, y + Tmp.v2.y, rot - 90f);
        }

        flame.y = podY;
        flame.draw(this);
    }

    public void drawThrusters(float frame, float alpha, float podY, float rot){
        float length = -thrusterLength * frame;
        for(int i = 0; i < 4; i++){
            var reg = i >= 2 ? thruster2Region : thruster1Region;
            var regTeam = i >= 2 ? thruster2RegionTeam : thruster1RegionTeam;
            float dx = Geometry.d4x[i] * length, dy = Geometry.d4y[i] * length;
            Draw.alpha(alpha);
            Draw.rect(reg, x + dx, y + podY + dy, i * 90 + rot);
            Draw.color(team.color);
            Draw.alpha(alpha);
            Draw.rect(regTeam, x + dx, y + podY + dy, i * 90 + rot);
            Draw.reset();
        }
    }

    public class DropFlame{
        public float baseLength = 0;
        public float maxLength = 40;
        public int divisions = 12;
        public int startParticles = 5;
        public int particlesMultiple = 3;
        float radius = 4;
        public float x, y;

        //length, radius,pan,alpha
        public float[] lengthradiusPans = {
        1.25f, 1f, 0.32f, 0.5f,
        1.05f, 1f, 0.28f, 0.58f,
        0.8f, 0.9f, 0.22f, 0.66f,
        0.65f, 0.8f, 0.2f, 0.78f,
        0.5f, 0.7f, 0.15f, 0.84f,
        0.4f, 0.6f, 0.1f, 0.9f,
        0.3f, 0.2f, 0.05f, 1f,
        };

        public void draw(AirborneSpawner drop){
            float sin = Mathf.absin(Time.time, 2f, 0.1f);
            Color color = team.color;
            float baseRotation = 90;
            float ex = drop.x + x, ey = drop.y + y;
            float realLength;
            realLength = (baseLength + Math.abs((maxLength - baseLength)) * Mathf.curve(fin(), 0, 0.25f)) *
            (1 - Mathf.curve(fin(), landingProgress1, landingProgress2 - 0.1f));

            for(int i = 0; i < lengthradiusPans.length / 4; i++){
                float alphaWave = Mathf.absin(4f, i * 0.55f) * 0.25f + 0.66f;
                float baseAlpha = lengthradiusPans[i * 4 + 3];
                Draw.color(color.cpy().a(baseAlpha * alphaWave));
                Drawf.flame(ex, ey,
                divisions,
                baseRotation,
                realLength * lengthradiusPans[i * 4] * (1f - sin),
                radius * lengthradiusPans[i * 4 + 1] * (1f + sin),
                lengthradiusPans[i * 4 + 2]
                );
            }

            float particleLife = 120;
            float particleLen = 7.5f;
            Rand rand = WHUtils.rand((long)(drop.id + 99999 + x + y));

            float progress = Mathf.clamp((realLength - baseLength) / (maxLength - baseLength), 0, 1);
            int particlesMult = (int)(1 + particlesMultiple * progress);

            float base = (Time.time / particleLife);
            for(int i = 0; i < startParticles * particlesMult; i++){
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin, fslope = WHFx.fslope(fin);
                float len = rand.random(particleLen * 0.7f, particleLen * 1.3f) * Mathf.curve(fin, 0.2f, 0.9f);
                float centerDeg = rand.random(Mathf.pi);
                Tmp.v2.trns(baseRotation, Interp.pow3In.apply(fin) * rand.random(0, realLength) + rand.range(11),
                (((rand.random(0, radius) * (fout + 1) / 2 + 2) / (3 * fin / 7 + 1.3f) - 1) + rand.random(-radius, radius)) * Mathf.cos(centerDeg));
                float angle = Mathf.slerp(baseRotation, baseRotation - centerDeg * 5f, Interp.pow2Out.apply(fin));
                Tmp.v2.add(ex, ey);

                Draw.color(color.cpy(), Color.white, fin * 0.7f);
                Lines.stroke(Mathf.curve(fslope, 0, 0.42f) * 1.4f * Mathf.curve(fin, 0, 0.6f));
                Lines.lineAngleCenter(Tmp.v2.x, Tmp.v2.y, angle, len);

            }
        }
    }

    private void spawnFromThrusters(){
        hasCreatUnits = true;
        spawnedUnits.clear();

        for(int i = 0; i < spawnSeq.size; i++){
            UnitType unitType = spawnSeq.get(i);
            if(!canCreateType(unitType)) continue;

            Vec2 spawn = new Vec2();
            Vec2 spawnPos = thrusterSpawnPosition(i, spawnSeq.size, spawn);
            WHUtils.snapToSpawnPosition(unitType, spawnPos.x, spawnPos.y, Math.max(tilesize * 3f, unitType.hitSize), spawnPos);
            Unit spawned = spawnUnit(unitType, spawnPos.x, spawnPos.y, rotation());

            spawnedUnits.add(spawned);
            Fx.unitSpawn.at(spawned.x, spawned.y, rotation(), unitType);
            WHFx.spawn.at(spawned.x, spawned.y, unitType.hitSize, team.color);
        }
    }

    private Vec2 thrusterSpawnPosition(int index, int total, Vec2 out){
        int side = index & 1;
        float sideAngle = rotation() + (side == 0 ? 45f : 225f);
        float distance = thrusterOffset + thrusterLength;

        out.trns(sideAngle, distance).add(x, y);

        float perSide = (total + 1f) / 2f;
        float lane = index / 2f;
        float laneCenter = (perSide - 1f) * 0.5f;
        float laneOffset = (lane - laneCenter) * thrusterSpacing;
        Tmp.v2.trns(rotation() + 90f, laneOffset);
        out.add(Tmp.v2);
        return out;
    }

    private Unit spawnUnit(UnitType unitType, float spawnX, float spawnY, float spawnRotation){
        Unit spawned = unitType.create(team);
        spawned.set(spawnX, spawnY);
        spawned.rotation = spawnRotation;
        if(shieldToApply >= 0f){
            spawned.shield = shieldToApply;
        }
        if(!Double.isNaN(flagToApply)){
            spawned.flag(flagToApply);
        }
        if(!Vars.net.client()) spawned.add();
        spawned.apply(StatusEffects.unmoving, 120f);
        spawned.apply(statusEntry.effect, statusEntry.time);
        if(commandPos != null && !commandPos.isNaN()){
            if(spawned.isCommandable()){
                spawned.command().commandPosition(commandPos);
            }else{
                CommandAI ai = new CommandAI();
                ai.commandPosition(commandPos);
                spawned.controller(ai);
            }
        }

        if (onSpawned != null) {
            onSpawned.get(spawned);
        }

        toSpawn = spawned;
        Events.fire(new EventType.UnitCreateEvent(spawned, null));
        return spawned;
    }

    public boolean canCreate(){
        if(Vars.state == null || Vars.state.rules == null) return false;
        if(team == Vars.state.rules.waveTeam) return true;
        if(spawnSeq.isEmpty()) return Units.canCreate(team, type);

        for(UnitType unitType : spawnSeq){
            if(unitType != null && Units.canCreate(team, unitType)){
                return true;
            }
        }
        return false;
    }

    private boolean canCreateType(UnitType unitType){
        if(unitType == null || Vars.state == null || Vars.state.rules == null) return false;
        return team == Vars.state.rules.waveTeam || Units.canCreate(team, unitType);
    }

    @Override
    public void write(Writes write){
        super.write(write);
        write.bool(hasDropImpact);
        write.bool(hasCreatUnits);
        write.f(lifetime);
        write.f(time);
        write.f(rotation);
        write.f(surviveTime);
        write.d(flagToApply);
        TypeIO.writeUnitType(write, type);
        TypeIO.writeTeam(write, team);
        TypeIO.writeStatus(write, statusEntry);

        TypeIO.writeVec2(write, commandPos);
        write.f(dropStartHeight);
        write.f(dropImpactDamage);
        write.f(dropImpactRadius);
        write.f(podSizeScale);
        write.f(thrusterLength);
        write.f(thrusterOffset);
        write.f(thrusterSpacing);
        write.i(spawnSeq.size);
        for(UnitType unitType : spawnSeq){
            TypeIO.writeUnitType(write, unitType);
        }
    }

    @Override
    public void read(Reads read){
        super.read(read);
        hasDropImpact = read.bool();
        hasCreatUnits = read.bool();
        lifetime = read.f();
        time = read.f();
        rotation = read.f();
        surviveTime = read.f();
        flagToApply = read.d();

        type = TypeIO.readUnitType(read);
        team = TypeIO.readTeam(read);
        statusEntry = TypeIO.readStatus(read);

        commandPos = TypeIO.readVec2(read);
        dropStartHeight = read.f();
        dropImpactDamage = read.f();
        dropImpactRadius = read.f();
        podSizeScale = read.f();
        thrusterLength = read.f();
        thrusterOffset = read.f();
        thrusterSpacing = read.f();
        spawnSeq.clear();
        int size = read.i();
        for(int i = 0; i < size; i++){
            UnitType unitType = TypeIO.readUnitType(read);
            if(unitType != null) spawnSeq.add(unitType);
        }
        if(spawnSeq.isEmpty() && type != null){
            spawnSeq.add(type);
        }
        refreshPrimaryType();

        afterRead();
    }

    @Override
    public boolean serialize(){
        return true;
    }

    @Override
    public int classId(){
        return EntityRegister.getId(AirborneSpawner.class);
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
        hasDropImpact = read.bool();
        hasCreatUnits = read.bool();
        read.bool(); // discard old trigger flag slot
        lifetime = read.f();
        time = read.f();
        rotation = read.f();
        surviveTime = read.f();

        type = TypeIO.readUnitType(read);
        team = TypeIO.readTeam(read);
        spawnSeq.clear();
        int size = read.i();
        for(int i = 0; i < size; i++){
            UnitType unitType = TypeIO.readUnitType(read);
            if(unitType != null) spawnSeq.add(unitType);
        }
        if(spawnSeq.isEmpty() && type != null){
            spawnSeq.add(type);
        }
        Vec2 syncPos = TypeIO.readVec2(read);
        commandPos = syncPos == null ? new Vec2(Float.NaN, Float.NaN) : syncPos;
        refreshPrimaryType();

        afterSync();
    }

    @Override
    public void writeSync(Writes write){
        write.f(x);
        write.f(y);
        write.bool(hasDropImpact);
        write.bool(hasCreatUnits);
        write.bool(false); // padding for old trigger flag slot
        write.f(lifetime);
        write.f(time);
        write.f(rotation);
        write.f(surviveTime);

        TypeIO.writeUnitType(write, type);
        TypeIO.writeTeam(write, team);
        write.i(spawnSeq.size);
        for(UnitType unitType : spawnSeq){
            TypeIO.writeUnitType(write, unitType);
        }
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
    public boolean isSyncHidden(Team team) {
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

    private void refreshPrimaryType(){
        if(spawnSeq.isEmpty()){
            type = type == null ? UnitTypes.alpha : type;
            spawnSeq.add(type);
            return;
        }

        UnitType first = spawnSeq.first();
        if(first == null){
            type = type == null ? UnitTypes.alpha : type;
            spawnSeq.set(0, type);
        }else{
            type = first;
        }
    }
}
