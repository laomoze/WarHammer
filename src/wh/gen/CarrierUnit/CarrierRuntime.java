package wh.gen.CarrierUnit;

import arc.Events;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.*;
import arc.util.Interval;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ai.types.CommandAI;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.EntityGroup;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.game.EventType.PayloadDropEvent;
import mindustry.game.EventType.PickupEvent;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;
import wh.entities.world.entities.CarrierUnitType;
import wh.gen.CarrierUnit.UnitAI.CarrierBoundAIC;
import wh.gen.CarrierUnit.UnitAI.CarrierFighterAI;
import wh.gen.EntityRegister;

@SuppressWarnings("unchecked")
public class CarrierRuntime extends CarrierUnit implements CarrierHostc{
    private static final float eps = 0.001f;
    private static final int intervalClaimCleanup = 0;
    private static final int intervalVisualCleanup = 1;
    private static final int intervalQueueSyncCheck = 2;
    private static final int intervalDeckTimerPrune = 3;

    public static class RunwayPayloadState{
        public int runway = 0;
        public int slot = -1;
        public boolean seeded = false;
        public final Vec2 current = new Vec2();
        public final Vec2 target = new Vec2();

        public void snap(){
            current.set(target);
        }

        public void update(float lerp){
            float amount = Mathf.clamp(lerp, 0f, 1f);
            current.x = Mathf.lerpDelta(current.x, target.x, amount);
            current.y = Mathf.lerpDelta(current.y, target.y, amount);
        }
    }

    public static class RunwayLane{
        public int runway = 0;
        public Queue<UnitPayload> queue = new Queue<>();
        public final Seq<RunwayPayloadState> states = new Seq<>();
        public final Seq<Unit> airborneUnits = new Seq<>();
        public float launchReload = 0f;
        public float recoverReload = 0f;
        public float rearmReload = 0f;
        public float regroupDelayTimer = 0f;
        public boolean launchWaveActive = false;
        public int storedFighterCount = 0;
        public int assignedFighterCount = 0;
        public boolean allAirborneRecovering = true;
        public boolean launchBlockedByActiveFighter = false;

        public RunwayLane(int runway){
            this.runway = runway;
        }

        public void resetTiming(){
            launchReload = 0f;
            recoverReload = 0f;
            rearmReload = 0f;
            regroupDelayTimer = 0f;
            launchWaveActive = false;
        }

        public void resetFrameState(int storedCount){
            airborneUnits.clear();
            storedFighterCount = storedCount;
            assignedFighterCount = storedCount;
            allAirborneRecovering = true;
            launchBlockedByActiveFighter = false;
        }

        public void trackAirborne(Unit fighter, boolean recovering, boolean launchBlocked){
            airborneUnits.add(fighter);
            assignedFighterCount++;
            if(!recovering){
                allAirborneRecovering = false;
            }
            if(launchBlocked){
                launchBlockedByActiveFighter = true;
            }
        }
    }

    public IntSeq activeFighters = new IntSeq();
    public IntFloatMap sortieElapsed = new IntFloatMap();
    public IntSeq targetRunwayCounts = new IntSeq();
    public IntFloatMap deckRefitTimers = new IntFloatMap();
    public transient IntFloatMap deckHealPulseTimers = new IntFloatMap();

    public int targetFighterCount = -1;
    public int lossCount = 0;

    public boolean deckInitialized = false;
    public boolean regrouping = false;

    public transient ObjectMap<Payload, Vec2> payloadVisualPos = new ObjectMap<>();
    public transient Queue<UnitPayload>[] runwayQueues = new Queue[0];
    public transient RunwayLane[] runwayLanes = new RunwayLane[0];
    public transient ObjectMap<UnitPayload, RunwayPayloadState> runwayPayloadStates = new ObjectMap<>();
    public transient Seq<Payload> queueOverflow = new Seq<>();
    public transient int[] runwayRecoveryClaims = new int[0];
    private transient Vec2 runwayPointScratch;
    private transient IntSet deckQueuedIdsScratch;
    private transient IntSeq deckStaleIdsScratch;
    private transient Interval runtimeIntervals = new Interval(4);
    private transient boolean runwayQueuesDirty = true;
    private transient ObjectSet<Payload> payloadLiveScratch;
    private transient Seq<Payload> payloadStalePayloadScratch;
    private transient Seq<UnitPayload> payloadStaleStateScratch;

    @Override
    public int classId(){
        return EntityRegister.getId(getClass());
    }

    @Override
    public void setType(mindustry.type.UnitType type){
        super.setType(type);

        activeFighters.clear();
        sortieElapsed.clear();
        targetRunwayCounts.clear();
        deckRefitTimers.clear();
        deckHealPulseTimers.clear();

        targetFighterCount = -1;
        lossCount = 0;

        deckInitialized = false;
        regrouping = false;
        resetTransientRunwayState();

        rebuildDeckSlots();
        trimPayloadToDeck();
    }

    @Override
    public void update(){
        super.update();
        boolean server = !Vars.net.client();

        CarrierUnitType ctype = carrierType();
        if(ctype == null) return;

        if(!deckInitialized){
            initDeck(ctype);
            deckInitialized = true;
        }

        ensureRunwayQueueSync(ctype);
        ensureRunwayRecoveryClaims();
        if(runtimeIntervals().get(intervalClaimCleanup, 20f)){
            cleanupRunwayRecoveryClaims();
        }

        if (!server) {
            updatePayloadVisuals(ctype);
            return;
        }

        if(regrouping && runwayLanes != null){
            for(RunwayLane lane : runwayLanes){
                if(lane != null && lane.regroupDelayTimer > 0f){
                    lane.regroupDelayTimer = Math.max(0f, lane.regroupDelayTimer - Time.delta);
                }
            }
        }

        // 运行顺序：先同步状态，再处理回收/补编，最后处理发射。
        refreshRunwayFlightState(ctype);
        updateDeckMaintenance(ctype);
        updateRecovery(ctype);
        updateRearm(ctype);
        updatePayloadVisuals(ctype);
        updateLaunch(ctype);
    }

    public int storedFighterCountInRunway(int runway){
        int r = clampRunway(runway);
        if(runwayQueues != null && runwayQueues.length == runwayCount() && runwayQueues.length > r && runwayQueues[r] != null){
            return runwayQueues[r].size;
        }

        int count = 0;
        for(Payload payload : payloads){
            if(payloadRunway(payload, r) == r){
                count++;
            }
        }
        return count;
    }

    public int computeTargetFighterCount(CarrierUnitType ctype){
        int cap = Math.max(Math.min(deckSlotCount(), ctype.deckCapacity()), 1);
        // 当前策略：目标机库数量始终维持满编。
        targetFighterCount = cap;
        return cap;
    }

    public int targetFighterCountInRunway(int runway){
        int r = clampRunway(runway);
        return targetRunwayCounts.size > r ? targetRunwayCounts.get(r) : 0;
    }

    protected Vec2 runwayPointScratch(){
        if(runwayPointScratch == null){
            runwayPointScratch = new Vec2();
        }
        return runwayPointScratch;
    }

    protected IntSet deckQueuedIdsScratch(){
        if(deckQueuedIdsScratch == null){
            deckQueuedIdsScratch = new IntSet();
        }else{
            deckQueuedIdsScratch.clear();
        }
        return deckQueuedIdsScratch;
    }

    protected IntSeq deckStaleIdsScratch(){
        if(deckStaleIdsScratch == null){
            deckStaleIdsScratch = new IntSeq();
        }else{
            deckStaleIdsScratch.clear();
        }
        return deckStaleIdsScratch;
    }

    protected Interval runtimeIntervals(){
        if(runtimeIntervals == null){
            runtimeIntervals = new Interval(4);
        }
        return runtimeIntervals;
    }

    protected void markRunwayQueuesDirty(){
        runwayQueuesDirty = true;
    }

    protected ObjectSet<Payload> payloadLiveScratch(){
        if(payloadLiveScratch == null){
            payloadLiveScratch = new ObjectSet<>();
        }else{
            payloadLiveScratch.clear();
        }
        return payloadLiveScratch;
    }

    protected Seq<Payload> payloadStalePayloadScratch(){
        if(payloadStalePayloadScratch == null){
            payloadStalePayloadScratch = new Seq<>();
        }else{
            payloadStalePayloadScratch.clear();
        }
        return payloadStalePayloadScratch;
    }

    protected Seq<UnitPayload> payloadStaleStateScratch(){
        if(payloadStaleStateScratch == null){
            payloadStaleStateScratch = new Seq<>();
        }else{
            payloadStaleStateScratch.clear();
        }
        return payloadStaleStateScratch;
    }

    protected int runwayTargetCount(int runway){
        return Math.min(targetFighterCountInRunway(runway), runwayCapacity(runway));
    }

    protected void rebuildTargetRunwayCounts(CarrierUnitType ctype){
        int runways = runwayCount();
        if(targetRunwayCounts.size != runways){
            targetRunwayCounts.clear();
            for(int i = 0; i < runways; i++) targetRunwayCounts.add(0);
        }else{
            for(int i = 0; i < runways; i++) targetRunwayCounts.set(i, 0);
        }

        int remain = computeTargetFighterCount(ctype);
        while(remain > 0){
            boolean progressed = false;
            for(int i = 0; i < runways && remain > 0; i++){
                int cur = targetRunwayCounts.get(i);
                int cap = runwayCapacity(i);
                if(cur < cap){
                    targetRunwayCounts.set(i, cur + 1);
                    remain--;
                    progressed = true;
                }
            }
            if(!progressed) break;
        }
    }

    public void deckSlotWorld(int slot, Vec2 out){
        if(out == null) return;

        CarrierUnitType ctype = carrierType();
        int total = deckSlotCount();
        if(ctype == null || total <= 0){
            out.set(x, y);
            return;
        }

        int index = Mathf.clamp(slot, 0, total - 1);
        float localX;
        float localY;

        if(ctype.runways.any()){
            float spacing = ctype.slotSpacing();
            int remain = index;
            int runways = ctype.runways.size;

            CarrierUnitType.Runway resolved = null;
            int localIndex = 0;
            for(int i = 0; i < runways; i++){
                CarrierUnitType.Runway def = ctype.runways.get(i);
                int cap = Math.max(def.capacity, 1);
                if(remain < cap){
                    resolved = def;
                    localIndex = remain;
                    break;
                }
                remain -= cap;
            }

            if(resolved == null){
                resolved = ctype.runways.peek();
                localIndex = Math.max(Math.max(resolved.capacity, 1) - 1, 0);
            }

            localX = resolved.x;
            localY = resolved.y - localIndex * spacing;
        }else{
            int lanes = Math.max(ctype.deckLanes, 1);
            float laneCenter = (lanes - 1f) * 0.5f;
            int row = index / lanes;
            int lane = index % lanes;

            localX = (lane - laneCenter) * ctype.deckLaneSpacing;
            localY = ctype.deckFrontOffset - row * ctype.deckRowSpacing;
        }

        float rot = rotation - 90f;
        out.set(
        Angles.trnsx(rot, localX, localY) + x,
        Angles.trnsy(rot, localX, localY) + y
        );
    }

    public void deckSlotWorldVisual(Payload payload, int slot, Vec2 out){
        if(out == null) return;

        if(slot < 0){
            Tmp.v1.set(x, y);
        }else{
            deckSlotWorld(slot, Tmp.v1);
        }

        if(payload == null){
            out.set(Tmp.v1);
            return;
        }

        if(payloadVisualPos == null){
            payloadVisualPos = new ObjectMap<>();
        }

        Vec2 visual = payloadVisualPos.get(payload);
        if(visual == null){
            visual = new Vec2(Tmp.v1);
            payloadVisualPos.put(payload, visual);
        }

        out.set(visual);
    }

    protected int deckSlotForPayloadInternal(Payload payload){
        if(payload == null || deckSlotCount() <= 0) return -1;
        if(queueOverflow != null && queueOverflow.contains(payload, true)) return -1;

        if(payload instanceof UnitPayload up && runwayPayloadStates != null){
            RunwayPayloadState state = runwayPayloadStates.get(up);
            if(state != null && state.slot >= 0 && state.slot < deckSlotCount()){
                return state.slot;
            }
        }

        if(runwayQueues != null && runwayQueues.length == runwayCount()){
            for(int runway = 0; runway < runwayQueues.length; runway++){
                Queue<UnitPayload> queue = runwayQueues[runway];
                if(queue == null || queue.isEmpty()) continue;

                int localIndex = 0;
                for(UnitPayload queued : queue){
                    if(queued == payload){
                        int slot = runwayFirstSlot(runway) + localIndex;
                        return Mathf.clamp(slot, 0, deckSlotCount() - 1);
                    }
                    localIndex++;
                }
            }
        }

        int linear = payloads.indexOf(payload, true);
        if(linear < 0) return -1;
        return Mathf.clamp(linear, 0, deckSlotCount() - 1);
    }

    public int deckSlotForPayload(Payload payload){
        CarrierUnitType ctype = carrierType();
        if(ctype != null){
            ensureRunwayQueueSync(ctype);
        }
        return deckSlotForPayloadInternal(payload);
    }

    @Override
    public float deckRefitRemaining(int fighterId){
        if(fighterId < 0) return 0f;
        return Math.abs(deckRefitTimers.get(fighterId, 0f));
    }

    @Override
    public boolean deckRefitShowsConstruct(int fighterId){
        return fighterId >= 0 && deckRefitTimers.get(fighterId, 0f) > 0.001f;
    }

    public Vec2 runwayFrontPoint(int runway, Vec2 out){
        if(out == null) out = Tmp.v1;

        CarrierUnitType ctype = carrierType();
        if(ctype == null){
            return out.set(this);
        }

        if(ctype.runways.any()){
            CarrierUnitType.Runway def = ctype.runways.get(clampRunway(runway));
            float rot = rotation - 90f;
            return out.set(
            Angles.trnsx(rot, def.x, def.y) + x,
            Angles.trnsy(rot, def.x, def.y) + y
            );
        }

        deckSlotWorld(runwayFirstSlot(runway), out);
        return out;
    }

    public Vec2 runwayBackPoint(int runway, Vec2 out){
        if(out == null) out = Tmp.v1;

        CarrierUnitType ctype = carrierType();
        if(ctype == null){
            return out.set(this);
        }

        runwayFrontPoint(runway, out);
        float frontX = out.x, frontY = out.y;
        Vec2 forward = runwayForwardVector(runway, runwayPointScratch());
        float depth = runwayDeckDepth(runway);
        return out.set(frontX - forward.x * depth, frontY - forward.y * depth);
    }

    public Vec2 runwayQueueBackPoint(int runway, Vec2 out){
        return recoveryPoint(runway, out);
    }

    public Vec2 runwayQueueInsertPoint(int runway, Vec2 out){
        CarrierUnitType ctype = carrierType();
        float distance = ctype == null ? 48f : Math.max(
        ctype.slotSpacing() * 1.5f,
        Math.max(ctype.landingApproachRadius * 1.75f, ctype.recoverRadius * 2.1f)
        );
        return recoveryReversePoint(runway, distance, out);
    }

    public float runwayDeckDepth(int runway){
        CarrierUnitType ctype = carrierType();
        if(ctype == null) return 0f;
        return Math.max(runwayCapacity(runway) - 1, 0) * ctype.slotSpacing();
    }

    public Vec2 runwayForwardVector(int runway, Vec2 out){
        if(out == null) out = Tmp.v1;

        CarrierUnitType ctype = carrierType();
        if(ctype != null && ctype.runways.any()){
            CarrierUnitType.Runway def = ctype.runways.get(clampRunway(runway));
            float baseRot = rotation - 90f;
            float angle = Float.isNaN(def.angle) ? baseRot : baseRot + def.angle;
            out.set(Angles.trnsx(angle, 0f, 1f), Angles.trnsy(angle, 0f, 1f));
        }else{
            float angle = rotation - 90f;
            out.set(Angles.trnsx(angle, 0f, 1f), Angles.trnsy(angle, 0f, 1f));
        }

        if(out.len2() < 0.001f){
            Tmp.v2.trns(rotation - 90f, 1f);
            out.set(Tmp.v2);
        }

        return out.nor();
    }

    public Vec2 launchExitPoint(int runway, Vec2 out){
        if(out == null) out = Tmp.v1;

        CarrierUnitType ctype = carrierType();
        if(ctype == null){
            return out.set(this);
        }

        runwayFrontPoint(runway, out);
        float frontX = out.x, frontY = out.y;
        Vec2 forward = runwayForwardVector(runway, runwayPointScratch());
        float forwardOffset = Math.max(ctype.launchForwardOffset, 0f);
        return out.set(frontX + forward.x * forwardOffset, frontY + forward.y * forwardOffset);
    }

    public Vec2 recoveryPoint(int runway, Vec2 out){
        if(out == null) out = Tmp.v1;

        CarrierUnitType ctype = carrierType();
        if(ctype == null){
            return out.set(this);
        }

        int slot = runwayLastSlot(runway);
        deckSlotWorld(slot, out);
        Vec2 forward = runwayForwardVector(runway, runwayPointScratch());
        float rearOffset = Math.abs(ctype.recoverRearOffset);
        out.sub(forward.x * rearOffset, forward.y * rearOffset);

        if(invalidLaunchPoint(out)){
            runwayBackPoint(runway, out);
        }
        if(invalidLaunchPoint(out)){
            out.set(x, y);
        }
        return out;
    }

    public Vec2 recoveryReversePoint(int runway, Vec2 out){
        return recoveryReversePoint(runway, 0f, out);
    }

    public Vec2 recoveryReversePoint(int runway, float distance, Vec2 out){
        if(out == null) out = Tmp.v1;

        recoveryPoint(runway, Tmp.v1);
        runwayFrontPoint(runway, Tmp.v2);

        if(invalidLaunchPoint(Tmp.v1)){
            return out.set(x, y);
        }

        float len = Math.max(distance, 0f);
        if(len <= 0.001f){
            CarrierUnitType ctype = carrierType();
            if(ctype == null){
                len = 48f;
            }else{
                len = Math.max(48f, Math.max(ctype.landingApproachRadius * 2.35f, ctype.recoverRadius * 2.7f));
            }
        }

        Tmp.v3.set(Tmp.v1).sub(Tmp.v2);
        if(Tmp.v3.len2() < 0.0001f){
            runwayForwardVector(runway, Tmp.v3).scl(-1f);
        }else{
            Tmp.v3.nor();
        }

        return out.set(Tmp.v1.x + Tmp.v3.x * len, Tmp.v1.y + Tmp.v3.y * len);
    }

    public @Nullable Teamc lockedTarget(){
        if(controller() instanceof CommandAI ai){
            Teamc attack = ai.attackTarget;
            float checkRange = carrierType() == null ? Math.max(type.range, 300f) : Math.max(type.range, carrierType().maxFighterDistance * 1.7f);
            if(!Units.invalidateTarget(attack, this, checkRange)){
                return attack;
            }
        }

        if(mounts != null){
            float checkRange = carrierType() == null ? (Math.max(type.range, 60f) + 240f) : Math.max(type.range, carrierType().maxFighterDistance * 1.7f);
            for(int i = 0; i < mounts.length; i++){
                Teamc target = mounts[i].target;
                if(!Units.invalidateTarget(target, this, checkRange)){
                    return target;
                }
            }
        }

        CarrierUnitType ctype = carrierType();
        float range = ctype == null ? Math.max(type.range, 300f) : Math.max(type.range, ctype.maxFighterDistance);
        return Units.bestTarget(team, x, y, range, u -> u.checkTarget(true, true), b -> true, UnitSorts.weakest);
    }

    public boolean focusPosition(Vec2 out){
        if(out == null) return false;

        CarrierUnitType ctype = carrierType();
        float checkRange = ctype == null ? Math.max(type.range, 300f) : Math.max(type.range, ctype.maxFighterDistance * 1.7f);

        if(controller() instanceof CommandAI ai){
            Teamc attack = ai.attackTarget;
            if(!Units.invalidateTarget(attack, this, checkRange)){
                out.set(attack);
                return true;
            }

            if(ai.targetPos != null && Float.isFinite(ai.targetPos.x) && Float.isFinite(ai.targetPos.y)){
                float minFocus = Math.max(hitSize * 0.8f, 28f);
                if(!ai.targetPos.within(x, y, minFocus)){
                    Teamc pointEnemy = Units.bestEnemy(
                            team,
                            ai.targetPos.x,
                            ai.targetPos.y,
                            40, u -> u.checkTarget(true, true), UnitSorts.weakest);
                    if (pointEnemy == null) {
                        Building tile = Vars.world == null ? null : Vars.world.buildWorld(ai.targetPos.x, ai.targetPos.y);
                        if (tile != null && tile.team != team && tile.isValid()) {
                            pointEnemy = tile;
                        }
                    }
                    if (pointEnemy != null) {
                        out.set(pointEnemy);
                        return true;
                    }
                }
            }
        }

        if(mounts != null){
            for(int i = 0; i < mounts.length; i++){
                Teamc target = mounts[i].target;
                if(!Units.invalidateTarget(target, this, checkRange)){
                    out.set(target);
                    return true;
                }
            }
        }

        Teamc lock = lockedTarget();
        if(lock != null){
            out.set(lock);
            return true;
        }

        return false;
    }

    public boolean shouldRecallFighter(Unit fighter){
        CarrierUnitType ctype = carrierType();
        if(ctype == null || fighter == null) return true;
        if(fighter.controller() instanceof CarrierBoundAIC ai && ai.isReturning()) return true;

        boolean recall =
        (ctype.sortieDuration > 0f && sortieElapsed.get(fighter.id, 0f) >= ctype.sortieDuration) ||
        fighter.healthf() <= Mathf.clamp(ctype.recallHealthf, 0f, 1f) ||
        fighter.dst(this) > ctype.maxFighterDistance;

        return recall;
    }

    public boolean tryRecoverFighter(Unit fighter){
        if(fighter == null || !fighter.isValid() || fighter.dead()) return false;

        CarrierUnitType ctype = carrierType();
        if(ctype == null) return false;
        if(!isBoundFighter(fighter)) return false;

        int runway = fighterRunway(fighter);
        // 必须在触地点附近且甲板仍有容量时，才允许回收入队。
        if(!nearRecoveryTouchdown(fighter, runway, recoveryTouchdownRadius(fighter, ctype))) return false;
        if(storedFighterCountInRunway(runway) >= runwayCapacity(runway)) return false;
        if(payloads.size >= deckSlotCount()) return false;

        releaseRecoveryClaim(fighter);
        removeActiveFighter(fighter.id);
        recoverFighterToQueue(fighter, runway);
        trimPayloadToDeck();
        return true;
    }

    protected boolean isBoundFighter(Unit fighter){
        return fighter != null && decodeCarrierFlag(fighter.flag()) == id;
    }

    protected void bindFighter(Unit fighter, int runway){
        int r = clampRunway(runway);
        fighter.flag(encodeCarrierFlag(id, r));

        CarrierBoundAIC ai;
        if(fighter.controller() instanceof CarrierBoundAIC){
            ai = (CarrierBoundAIC)fighter.controller();
            ai.setCarrier(id).setRunway(r);
        }else{
            CarrierFighterAI newAi = new CarrierFighterAI(id, r);
            ai = newAi;
            fighter.controller(newAi);
        }
    }

    protected int payloadRunway(Payload payload, int fallback){
        int r = fallback;
        if(payload instanceof UnitPayload up && up.unit != null){
            r = decodeRunwayFlag(up.unit.flag());
        }
        return clampRunway(r < 0 ? fallback : r);
    }

    protected int findRunwayWithSpace(int[] counts){
        for(int i = 0; i < counts.length; i++){
            if(counts[i] < runwayCapacity(i)){
                return i;
            }
        }
        return -1;
    }

    protected void normalizeDeckOrdering(CarrierUnitType ctype){
        if(ctype == null) return;
        rebuildRunwayQueuesFromPayloads(ctype);
    }

    protected void startRegroup(CarrierUnitType ctype){
        if(ctype == null) return;

        regrouping = false;
        ensureRunwayLanes();
        for(RunwayLane lane : runwayLanes){
            if(lane != null){
                lane.resetTiming();
            }
        }
    }

    protected void initDeck(CarrierUnitType ctype){
        rebuildDeckSlots();
        trimPayloadToDeck();
        computeTargetFighterCount(ctype);
        rebuildTargetRunwayCounts(ctype);
        if(!payloads.isEmpty() || ctype.fighterType == null){
            normalizeDeckOrdering(ctype);
            return;
        }

        for(int runway = 0; runway < runwayCount(); runway++){
            int count = runwayTargetCount(runway);
            for(int i = 0; i < count; i++){
                createDeckFighter(ctype, runway);
            }
        }
    }

    protected void createDeckFighter(CarrierUnitType ctype, int runway){
        if(ctype == null || ctype.fighterType == null) return;

        int r = clampRunway(runway);
        if(storedFighterCountInRunway(r) >= runwayCapacity(r)) return;
        if(payloads.size >= deckSlotCount()) return;

        Unit fighter = ctype.fighterType.create(team);
        fighter.team.data().updateCount(fighter.type, 1);
        bindFighter(fighter, r);
        boolean replacementFromLoss = regrouping || lossCount > 0;
        float refit = replacementFromLoss ? Math.max(ctype.recoverRefitTime, 0f) : 0f;
        if(refit > 0.001f){
            deckRefitTimers.put(fighter.id, refit);
        }else{
            deckRefitTimers.remove(fighter.id, 0f);
            deckHealPulseTimers.remove(fighter.id, 0f);
        }

        UnitPayload created = new UnitPayload(fighter);
        runwayQueue(r).addLast(created);
        noteDeckFighterCreated(r);
        recoveryPoint(r, Tmp.v1);
        if(invalidLaunchPoint(Tmp.v1)){
            runwayQueueBackPoint(r, Tmp.v1);
        }
        if(invalidLaunchPoint(Tmp.v1)){
            runwayQueueInsertPoint(r, Tmp.v1);
        }
        if(!invalidLaunchPoint(Tmp.v1)){
            if(payloadVisualPos == null){
                payloadVisualPos = new ObjectMap<>();
            }
            payloadVisualPos.put(created, new Vec2(Tmp.v1));
        }
        syncPayloadsFromRunwayQueues();
    }

    protected void rebuildDeckSlots(){
    }

    protected void trimPayloadToDeck(){
        boolean removedAny = false;
        while(payloads.size > deckSlotCount() && !payloads.isEmpty()){
            Payload removed = payloads.pop();
            payloadVisualPos.remove(removed);
            if(queueOverflow != null){
                queueOverflow.remove(removed, true);
            }
            if(removed instanceof UnitPayload up){
                removeUnitPayloadFromAllQueues(up);
                runwayPayloadStates.remove(up);
                if(up.unit != null){
                    deckRefitTimers.remove(up.unit.id, 0f);
                    deckHealPulseTimers.remove(up.unit.id, 0f);
                }
            }
            removed.remove();
            removedAny = true;
        }
        if(removedAny){
            markRunwayQueuesDirty();
        }
    }

    protected void resetTransientRunwayState(){
        if(payloadVisualPos == null){
            payloadVisualPos = new ObjectMap<>();
        }else{
            payloadVisualPos.clear();
        }

        runwayQueues = new Queue[0];
        runwayLanes = new RunwayLane[0];
        runwayPayloadStates.clear();

        if(queueOverflow == null){
            queueOverflow = new Seq<>();
        }else{
            queueOverflow.clear();
        }

        runwayRecoveryClaims = new int[0];
        if(deckQueuedIdsScratch != null) deckQueuedIdsScratch.clear();
        if(deckStaleIdsScratch != null) deckStaleIdsScratch.clear();
        runtimeIntervals = new Interval(4);
        runwayQueuesDirty = true;
        if(payloadLiveScratch != null) payloadLiveScratch.clear();
        if(payloadStalePayloadScratch != null) payloadStalePayloadScratch.clear();
        if(payloadStaleStateScratch != null) payloadStaleStateScratch.clear();
    }

    protected void ensureRunwayQueues(){
        int runways = runwayCount();
        if(runwayQueues != null && runwayQueues.length == runways){
            for(int i = 0; i < runways; i++){
                if(runwayQueues[i] == null) runwayQueues[i] = new Queue<>();
            }
            if(queueOverflow == null) queueOverflow = new Seq<>();
            ensureRunwayLanes();
            return;
        }

        runwayQueues = new Queue[runways];
        for(int i = 0; i < runways; i++){
            runwayQueues[i] = new Queue<>();
        }
        runwayLanes = new RunwayLane[runways];
        runwayPayloadStates.clear();
        if(queueOverflow == null) queueOverflow = new Seq<>();
        else queueOverflow.clear();
        ensureRunwayLanes();
        markRunwayQueuesDirty();
    }

    protected void ensureRunwayLanes(){
        int runways = runwayCount();
        if(runwayLanes == null || runwayLanes.length != runways){
            runwayLanes = new RunwayLane[runways];
        }

        for(int i = 0; i < runways; i++){
            RunwayLane lane = runwayLanes[i];
            if(lane == null){
                lane = new RunwayLane(i);
                runwayLanes[i] = lane;
                lane.resetTiming();
            }

            lane.runway = i;
            if(runwayQueues != null && runwayQueues.length > i && runwayQueues[i] != null){
                lane.queue = runwayQueues[i];
            }else if(lane.queue == null){
                lane.queue = new Queue<>();
            }
        }
    }

    protected void ensureRunwayRecoveryClaims(){
        int runways = runwayCount();
        if(runwayRecoveryClaims != null && runwayRecoveryClaims.length == runways) return;

        int[] old = runwayRecoveryClaims;
        runwayRecoveryClaims = new int[runways];
        for(int i = 0; i < runways; i++){
            runwayRecoveryClaims[i] = -1;
        }
        if(old != null){
            int copy = Math.min(old.length, runways);
            for(int i = 0; i < copy; i++){
                runwayRecoveryClaims[i] = old[i];
            }
        }
    }

    protected void cleanupRunwayRecoveryClaims(){
        ensureRunwayRecoveryClaims();
        CarrierUnitType ctype = carrierType();
        for(int i = 0; i < runwayRecoveryClaims.length; i++){
            int claim = runwayRecoveryClaims[i];
            if(claim < 0) continue;

            Unit holder = Groups.unit.getByID(claim);
            boolean recovering = holder != null && holder.controller() instanceof CarrierBoundAIC ai && (ai.isReturning() || ai.isLanding());
            boolean staleDistance = false;
            if(holder != null && ctype != null){
                recoveryPoint(i, Tmp.v1);
                if(invalidLaunchPoint(Tmp.v1)){
                    runwayQueueInsertPoint(i, Tmp.v1);
                }
                float readyRadius = Math.max(ctype.recoverRadius * 1.25f, Math.max(ctype.landingApproachRadius, holder.hitSize * 2f));
                float staleRadius = Math.max(readyRadius * 2.6f, Math.max(ctype.recoverRadius * 3.8f, ctype.landingApproachRadius * 3.2f));
                staleDistance = !holder.within(Tmp.v1, staleRadius);
            }

            if(holder == null || !holder.isAdded() || holder.dead() || !isBoundFighter(holder) || fighterRunway(holder) != i || fighterStoredOnDeck(claim) || !recovering || staleDistance){
                runwayRecoveryClaims[i] = -1;
            }
        }
    }

    protected void releaseRunwayRecoveryClaimById(int fighterId){
        if(fighterId < 0 || runwayRecoveryClaims == null) return;
        for(int i = 0; i < runwayRecoveryClaims.length; i++){
            if(runwayRecoveryClaims[i] == fighterId){
                runwayRecoveryClaims[i] = -1;
            }
        }
    }

    public void releaseRecoveryClaim(Unit fighter){
        if(fighter == null) return;
        releaseRunwayRecoveryClaimById(fighter.id);
    }

    public boolean allowRecoveryApproach(Unit fighter){
        if(fighter == null || fighter.dead()) return false;
        CarrierUnitType ctype = carrierType();
        if(ctype == null) return false;
        if(!isBoundFighter(fighter)) return false;

        int runway = fighterRunway(fighter);
        if(!ctype.oneByOneRecovery){
            return true;
        }

        // 周期清理回收资格锁，避免陈旧占位。
        if(runtimeIntervals().get(intervalClaimCleanup, 6f)){
            cleanupRunwayRecoveryClaims();
        }
        recoveryPoint(runway, Tmp.v1);
        if(invalidLaunchPoint(Tmp.v1)){
            runwayQueueInsertPoint(runway, Tmp.v1);
        }
        float readyRadius = Math.max(ctype.recoverRadius * 1.25f, Math.max(ctype.landingApproachRadius, fighter.hitSize * 2f));
        float claimRadius = Math.max(readyRadius * 2.2f, Math.max(ctype.recoverRadius * 3f, ctype.landingApproachRadius * 2.4f));
        if(fighter.within(Tmp.v1, readyRadius)){
            runwayRecoveryClaims[runway] = fighter.id;
            return true;
        }

        int claim = runwayRecoveryClaims[runway];
        if(claim == fighter.id){
            if(!fighter.within(Tmp.v1, claimRadius * 1.8f)){
                runwayRecoveryClaims[runway] = -1;
                claim = -1;
            }else{
                return true;
            }
        }

        Unit holder = claim < 0 ? null : Groups.unit.getByID(claim);
        if(holder != null){
            boolean holderValid = holder.isAdded() && !holder.dead() && isBoundFighter(holder) && fighterRunway(holder) == runway;
            boolean holderRecovering = holder.controller() instanceof CarrierBoundAIC ai && (ai.isReturning() || ai.isLanding());
            boolean holderNear = holder.within(Tmp.v1, claimRadius * 1.8f);
            if(!holderValid || !holderRecovering || !holderNear){
                runwayRecoveryClaims[runway] = -1;
                claim = -1;
                holder = null;
            }
        }

        if(holder != null){
            float holderDst2 = holder.dst2(Tmp.v1);
            float fighterDst2 = fighter.dst2(Tmp.v1);
            if(fighterDst2 + claimRadius * claimRadius * 0.2f < holderDst2){
                runwayRecoveryClaims[runway] = fighter.id;
                return true;
            }
        }

        if(claim < 0){
            // 无锁时直接抢占，保证等待中的战机能进入回收流程。
            runwayRecoveryClaims[runway] = fighter.id;
            return true;
        }
        return false;
    }

    protected void clearRunwayQueues(){
        ensureRunwayQueues();
        for(int i = 0; i < runwayQueues.length; i++){
            runwayQueues[i].clear();
        }
        if(runwayLanes != null){
            for(int i = 0; i < runwayLanes.length; i++){
                if(runwayLanes[i] != null){
                    runwayLanes[i].states.clear();
                }
            }
        }
        runwayPayloadStates.clear();
        queueOverflow.clear();
        markRunwayQueuesDirty();
    }

    protected Queue<UnitPayload> runwayQueue(int runway){
        ensureRunwayQueues();
        return runwayQueues[clampRunway(runway)];
    }

    protected RunwayLane runwayLane(int runway){
        ensureRunwayQueues();
        ensureRunwayLanes();
        return runwayLanes[clampRunway(runway)];
    }

    protected boolean queueContainsPayload(Queue<UnitPayload> queue, Payload payload){
        if(queue == null || payload == null || queue.isEmpty()) return false;
        for(UnitPayload queued : queue){
            if(queued == payload){
                return true;
            }
        }
        return false;
    }

    protected boolean runwayQueuesSynced(){
        int runways = runwayCount();
        if(runwayQueues == null || runwayQueues.length != runways || queueOverflow == null){
            return false;
        }

        int total = queueOverflow.size;
        for(int i = 0; i < runways; i++){
            if(runwayQueues[i] == null) return false;
            total += runwayQueues[i].size;
        }

        if(total != payloads.size){
            return false;
        }

        for(Payload payload : payloads){
            if(queueOverflow.contains(payload, true)){
                continue;
            }
            if(!queueContainsPayloadForAnyRunway(payload)){
                return false;
            }
        }

        return true;
    }

    protected boolean queueContainsPayloadForAnyRunway(Payload payload){
        if(runwayQueues == null || payload == null) return false;
        for(int i = 0; i < runwayQueues.length; i++){
            if(queueContainsPayload(runwayQueues[i], payload)){
                return true;
            }
        }
        return false;
    }

    protected void ensureRunwayQueueSync(CarrierUnitType ctype){
        if(ctype == null) return;

        int runways = runwayCount();
        if(runwayQueues == null || runwayQueues.length != runways || queueOverflow == null){
            rebuildRunwayQueuesFromPayloads(ctype);
            runwayQueuesDirty = false;
            return;
        }

        if(!runwayQueuesDirty && !runtimeIntervals().get(intervalQueueSyncCheck, 20f)){
            return;
        }

        if(runwayQueuesDirty || !runwayQueuesSynced()){
            rebuildRunwayQueuesFromPayloads(ctype);
        }
        runwayQueuesDirty = false;
    }

    protected void removeUnitPayloadFromQueue(Queue<UnitPayload> queue, UnitPayload target){
        if(queue == null || target == null || queue.isEmpty()) return;

        Queue<UnitPayload> rebuilt = new Queue<>(queue.size);
        while(!queue.isEmpty()){
            UnitPayload current = queue.removeFirst();
            if(current != target){
                rebuilt.addLast(current);
            }
        }
        while(!rebuilt.isEmpty()){
            queue.addLast(rebuilt.removeFirst());
        }
    }

    protected void removeUnitPayloadFromAllQueues(UnitPayload target){
        if(target == null || runwayQueues == null) return;
        for(int i = 0; i < runwayQueues.length; i++){
            removeUnitPayloadFromQueue(runwayQueues[i], target);
        }
    }

    protected void rebuildRunwayQueuesFromPayloads(CarrierUnitType ctype){
        clearRunwayQueues();

        int runways = runwayCount();
        int[] counts = new int[runways];

        for(Payload payload : payloads){
            int runway = 0;

            if(payload instanceof UnitPayload up && up.unit != null){
                long packed = decodePackedFlag(up.unit.flag());
                boolean legacyNoRunway = packed >= 0L && packed < (1L << flagRunwayBits);

                if(legacyNoRunway){
                    runway = findRunwayWithSpace(counts);
                }else{
                    int decoded = decodeRunwayFlag(up.unit.flag());
                    runway = decoded >= 0 && decoded < runways ? decoded : findRunwayWithSpace(counts);
                }

                if(runway < 0){
                    queueOverflow.add(payload);
                    continue;
                }

                if(counts[runway] >= runwayCapacity(runway)){
                    int alt = findRunwayWithSpace(counts);
                    if(alt < 0){
                        queueOverflow.add(payload);
                        continue;
                    }
                    runway = alt;
                }

                bindFighter(up.unit, runway);
                if(!deckRefitTimers.containsKey(up.unit.id)){
                    deckRefitTimers.put(up.unit.id, 0f);
                }

                runwayQueues[runway].addLast(up);
                counts[runway]++;
            }else{
                queueOverflow.add(payload);
            }
        }

        syncPayloadsFromRunwayQueues();
    }

    protected void syncPayloadsFromRunwayQueues(){
        ensureRunwayQueues();

        payloads.clear();
        for(int i = 0; i < runwayQueues.length; i++){
            for(UnitPayload up : runwayQueues[i]){
                payloads.add(up);
            }
        }
        payloads.addAll(queueOverflow);
        runwayQueuesDirty = false;

        int keep = deckSlotCount();
        if(keep <= 0) return;

        while(payloads.size > keep){
            Payload removed = payloads.pop();
            payloadVisualPos.remove(removed);
            queueOverflow.remove(removed, true);
            if(removed instanceof UnitPayload up){
                removeUnitPayloadFromAllQueues(up);
                runwayPayloadStates.remove(up);
                if(up.unit != null){
                    deckRefitTimers.remove(up.unit.id, 0f);
                    deckHealPulseTimers.remove(up.unit.id, 0f);
                }
            }
            removed.remove();
        }
    }

    protected void updateDeckMaintenance(CarrierUnitType ctype){
        if(payloads.isEmpty()){
            deckRefitTimers.clear();
            deckHealPulseTimers.clear();
            return;
        }

        IntSet queued = deckQueuedIdsScratch();
        boolean allowHeal = ctype != null && ctype.recoverHealFraction > 0.0001f;
        float healInterval = allowHeal ? Math.max(ctype.recoverHealInterval, 1f) : 1f;
        float healFraction = allowHeal ? Mathf.clamp(ctype.recoverHealFraction, 0f, 1f) : 0f;

        for(Payload payload : payloads){
            if(!(payload instanceof UnitPayload up) || up.unit == null) continue;

            Unit fighter = up.unit;
            queued.add(fighter.id);

            if(allowHeal){
                updateDeckHealPulse(fighter, healInterval, healFraction);
            }
        }

        if(!allowHeal){
            deckHealPulseTimers.clear();
        }

        decayAndPruneDeckRefitTimers(queued);
        if(runtimeIntervals().get(intervalDeckTimerPrune, 25f)){
            pruneDeckTimerMap(deckHealPulseTimers, queued);
        }
    }

    protected void updateDeckHealPulse(Unit fighter, float healInterval, float healFraction){
        if(fighter.health >= fighter.maxHealth - 0.001f){
            deckHealPulseTimers.remove(fighter.id, 0f);
            return;
        }

        float timer = deckHealPulseTimers.get(fighter.id, healInterval) - Time.delta;
        if(timer <= 0f){
            fighter.heal(Math.max(fighter.maxHealth * healFraction, 1f));
            timer = healInterval;
        }
        deckHealPulseTimers.put(fighter.id, timer);
    }

    protected void decayAndPruneDeckRefitTimers(IntSet queued){
        if(deckRefitTimers.size <= 0) return;

        IntSeq stale = deckStaleIdsScratch();
        for(IntFloatMap.Entry entry : deckRefitTimers){
            int fighterId = entry.key;
            if(!queued.contains(fighterId)){
                stale.add(fighterId);
                continue;
            }

            float current = Math.abs(entry.value);
            float next = Math.max(0f, current - Time.delta);
            if(next <= 0.001f){
                stale.add(fighterId);
            }else{
                float sign = entry.value >= 0f ? 1f : -1f;
                deckRefitTimers.put(fighterId, sign * next);
            }
        }

        for(int i = 0; i < stale.size; i++){
            deckRefitTimers.remove(stale.get(i), 0f);
        }
    }

    protected void pruneDeckTimerMap(IntFloatMap timers, IntSet queued){
        if(timers == null || timers.size <= 0) return;

        IntSeq stale = deckStaleIdsScratch();
        for(IntFloatMap.Entry entry : timers){
            if(!queued.contains(entry.key)){
                stale.add(entry.key);
            }
        }

        for(int i = 0; i < stale.size; i++){
            timers.remove(stale.get(i), 0f);
        }
    }

    protected boolean fighterStoredOnDeck(int fighterId){
        if(fighterId < 0) return false;
        for(Payload payload : payloads){
            if(payload instanceof UnitPayload up && up.unit != null && up.unit.id == fighterId){
                return true;
            }
        }
        return false;
    }

    protected boolean canBeginLanding(Unit fighter){
        if(fighter == null || !fighter.isAdded() || fighter.dead() || !isBoundFighter(fighter)){
            return false;
        }

        int runway = fighterRunway(fighter);
        if(payloads.size >= deckSlotCount()) return false;
        if(storedFighterCountInRunway(runway) >= runwayCapacity(runway)) return false;
        return true;
    }

    protected float recoveryTouchdownRadius(Unit fighter, CarrierUnitType ctype){
        if(fighter == null || ctype == null) return 0f;
        return Math.max(3.5f, Math.max(ctype.slotSpacing() * 0.18f, fighter.hitSize * 0.4f));
    }

    protected boolean nearRecoveryTouchdown(Unit fighter, int runway, float radius){
        if(fighter == null) return false;
        recoveryPoint(runway, Tmp.v4);
        if(invalidLaunchPoint(Tmp.v4)) return false;
        return fighter.within(Tmp.v4.x, Tmp.v4.y, radius);
    }

    protected boolean canRecoverCandidate(Unit fighter, CarrierUnitType ctype, int runway, float touchX, float touchY, float range){
        if(payloads.size >= deckSlotCount()) return false;
        if(fighter == this || !fighter.within(touchX, touchY, range)) return false;
        if(!isBoundFighter(fighter)) return false;
        if(fighterRunway(fighter) != runway) return false;
        if(ctype.fighterType != null && fighter.type != ctype.fighterType) return false;
        float touchdownRadius = recoveryTouchdownRadius(fighter, ctype);
        if(fighter.controller() instanceof CarrierBoundAIC ai){
            if(!ai.canRecoverNow()) return false;
            if(!fighter.within(touchX, touchY, touchdownRadius)) return false;
        }else{
            // 非舰载 AI 兜底：仅在极近距离允许直接回收。
            if(!fighter.within(touchX, touchY, touchdownRadius)) return false;
        }
        return canBeginLanding(fighter);
    }

    protected RunwayPayloadState payloadState(UnitPayload payload){
        if(payload == null) return null;
        if(runwayPayloadStates == null){
            runwayPayloadStates = new ObjectMap<>();
        }

        RunwayPayloadState state = runwayPayloadStates.get(payload);
        if(state == null){
            state = new RunwayPayloadState();
            runwayPayloadStates.put(payload, state);
        }
        return state;
    }

    protected void updatePayloadVisuals(CarrierUnitType ctype){
        if(payloadVisualPos == null){
            payloadVisualPos = new ObjectMap<>();
        }
        if(runwayPayloadStates == null){
            runwayPayloadStates = new ObjectMap<>();
        }

        ensureRunwayLanes();

        float smooth = Mathf.clamp(ctype.deckVisualSmoothing, 0.02f, 0.95f);
        float queueSpeed = Mathf.clamp(ctype.queueMoveSpeed, 0.02f, 3f);
        float follow = Mathf.clamp(smooth * queueSpeed, 0.0025f, 0.18f);
        int slotCount = deckSlotCount();

        for(int runway = 0; runway < runwayCount(); runway++){
            RunwayLane lane = runwayLane(runway);
            lane.states.clear();

            int localIndex = 0;
            for(UnitPayload payload : lane.queue){
                if(payload == null){
                    continue;
                }

                RunwayPayloadState state = payloadState(payload);
                if(state == null){
                    continue;
                }

                int previousRunway = state.runway;
                state.runway = runway;
                state.slot = slotCount <= 0 ? -1 : Mathf.clamp(runwayFirstSlot(runway) + localIndex, 0, slotCount - 1);

                if(state.slot < 0){
                    state.target.set(x, y);
                }else{
                    deckSlotWorld(state.slot, state.target);
                }

                boolean constructing = payload.unit != null && deckRefitTimers.get(payload.unit.id, 0f) > eps;
                if(constructing){
                    recoveryPoint(runway, state.current);
                    if(invalidLaunchPoint(state.current)){
                        runwayQueueBackPoint(runway, state.current);
                    }
                    if(invalidLaunchPoint(state.current)){
                        state.current.set(state.target);
                    }
                    state.seeded = true;
                }else if(!state.seeded){
                    Vec2 seededVisual = payloadVisualPos.get(payload);
                    if(seededVisual != null && !invalidLaunchPoint(seededVisual)){
                        state.current.set(seededVisual);
                    }else{
                        recoveryPoint(runway, state.current);
                    }
                    if(invalidLaunchPoint(state.current)){
                        runwayQueueBackPoint(runway, state.current);
                    }
                    if(invalidLaunchPoint(state.current)){
                        runwayQueueInsertPoint(runway, state.current);
                    }
                    if(invalidLaunchPoint(state.current)){
                        state.current.set(state.target);
                    }
                    state.seeded = true;
                }else if(previousRunway != runway){
                    state.current.set(state.target);
                }else if(invalidLaunchPoint(state.current)){
                    state.snap();
                }else{
                    state.update(follow);
                }

                Vec2 visual = payloadVisualPos.get(payload);
                if(visual == null){
                    payloadVisualPos.put(payload, new Vec2(state.current));
                }else{
                    visual.set(state.current);
                }

                lane.states.add(state);
                localIndex++;
            }
        }

        for(Payload payload : queueOverflow){
            if(payload == null){
                continue;
            }
            if(payload instanceof UnitPayload up){
                runwayPayloadStates.remove(up);
            }
            Vec2 visual = payloadVisualPos.get(payload);
            if(visual == null){
                payloadVisualPos.put(payload, new Vec2(x, y));
            }else{
                visual.x = Mathf.lerpDelta(visual.x, x, follow);
                visual.y = Mathf.lerpDelta(visual.y, y, follow);
            }
        }

        if(runtimeIntervals().get(intervalVisualCleanup, 30f)){
            cleanupPayloadVisualCaches();
        }
    }

    protected void cleanupPayloadVisualCaches(){
        ObjectSet<Payload> livePayloads = payloadLiveScratch();
        for(Payload payload : payloads){
            if(payload != null){
                livePayloads.add(payload);
            }
        }

        Seq<Payload> stalePayloads = payloadStalePayloadScratch();
        for(ObjectMap.Entry<Payload, Vec2> entry : payloadVisualPos){
            if(!livePayloads.contains(entry.key)){
                stalePayloads.add(entry.key);
            }
        }
        for(Payload payload : stalePayloads){
            payloadVisualPos.remove(payload);
        }

        Seq<UnitPayload> staleStates = payloadStaleStateScratch();
        for(ObjectMap.Entry<UnitPayload, RunwayPayloadState> entry : runwayPayloadStates){
            if(entry.key == null || !livePayloads.contains(entry.key)){
                staleStates.add(entry.key);
            }
        }
        for(UnitPayload payload : staleStates){
            runwayPayloadStates.remove(payload);
        }
    }

    protected void refreshRunwayFlightState(CarrierUnitType ctype){
        ensureRunwayLanes();
        for(int runway = 0; runway < runwayCount(); runway++){
            runwayLane(runway).resetFrameState(storedFighterCountInRunway(runway));
        }

        for(int i = activeFighters.size - 1; i >= 0; i--){
            int fighterId = activeFighters.get(i);
            boolean duplicate = false;
            for(int j = i - 1; j >= 0; j--){
                if(activeFighters.get(j) == fighterId){
                    duplicate = true;
                    break;
                }
            }
            if(duplicate){
                activeFighters.removeIndex(i);
                continue;
            }
            Unit fighter = Groups.unit.getByID(fighterId);

            if(fighter == null || !fighter.isAdded() || fighterStoredOnDeck(fighterId)){
                removeActiveFighterAt(i, fighterId);
                continue;
            }

            if(fighter.dead()){
                removeActiveFighterAt(i, fighterId);

                lossCount++;
                startRegroup(ctype);
                continue;
            }

            if(!isBoundFighter(fighter)){
                removeActiveFighterAt(i, fighterId);
                continue;
            }

            bindFighter(fighter, fighterRunway(fighter));
            sortieElapsed.put(fighterId, sortieElapsed.get(fighterId, 0f) + Time.delta);

            int runway = fighterRunway(fighter);
            RunwayLane lane = runwayLane(runway);
            CarrierBoundAIC ai = fighter.controller() instanceof CarrierBoundAIC carrierAi ? carrierAi : null;
            boolean recovering = ai != null && (ai.isReturning() || ai.isLanding());
            boolean launchBlocked = ai != null && ai.isLanding();
            lane.trackAirborne(fighter, recovering, launchBlocked);
        }
    }

    protected void refreshRunwayAirborneFlags(RunwayLane lane){
        lane.allAirborneRecovering = true;
        lane.launchBlockedByActiveFighter = false;

        for(Unit fighter : lane.airborneUnits){
            CarrierBoundAIC ai = fighter.controller() instanceof CarrierBoundAIC carrierAi ? carrierAi : null;
            boolean recovering = ai != null && (ai.isReturning() || ai.isLanding());
            boolean launchBlocked = ai != null && ai.isLanding();
            if(!recovering){
                lane.allAirborneRecovering = false;
            }
            if(launchBlocked){
                lane.launchBlockedByActiveFighter = true;
            }
        }
    }

    protected void noteDeckFighterCreated(int runway){
        RunwayLane lane = runwayLane(runway);
        lane.storedFighterCount++;
        lane.assignedFighterCount++;
    }

    protected void noteDeckFighterLaunched(int runway){
        RunwayLane lane = runwayLane(runway);
        lane.storedFighterCount = Math.max(lane.storedFighterCount - 1, 0);
    }

    protected void noteAirborneFighterRecovered(Unit fighter, int runway){
        RunwayLane lane = runwayLane(runway);
        lane.airborneUnits.remove(fighter, true);
        lane.storedFighterCount++;
        refreshRunwayAirborneFlags(lane);
    }

    protected void updateRecovery(CarrierUnitType ctype){
        CarrierRecoveryFlow.updateRecovery(this, ctype);
    }

    protected int assignedFightersInRunway(int runway){
        return runwayLane(runway).assignedFighterCount;
    }

    protected int airborneFightersInRunway(int runway){
        return runwayLane(runway).airborneUnits.size;
    }

    protected boolean runwayCanRearmNow(int runway){
        RunwayLane lane = runwayLane(runway);
        if(lane.storedFighterCount <= 0) return true;

        int airborne = airborneFightersInRunway(runway);
        if(airborne <= 0) return true;

        return lane.allAirborneRecovering;
    }

    protected boolean runwayHasConstructingPayload(int runway){
        Queue<UnitPayload> queue = runwayQueue(runway);
        if(queue == null || queue.isEmpty()) return false;

        for(UnitPayload payload : queue){
            if(payload == null || payload.unit == null) continue;
            if(deckRefitTimers.get(payload.unit.id, 0f) > eps){
                return true;
            }
        }
        return false;
    }

    protected boolean runwayStoredFightersAllHealthy(int runway){
        CarrierUnitType ctype = carrierType();
        if(ctype == null || !ctype.launchRequireFullHealth){
            return true;
        }

        Queue<UnitPayload> queue = runwayQueue(runway);
        if(queue == null || queue.isEmpty()){
            return false;
        }

        for(UnitPayload payload : queue){
            if(payload == null || payload.unit == null) continue;

            Unit fighter = payload.unit;
            float refit = Math.abs(deckRefitTimers.get(fighter.id, 0f));
            if(refit > 0.001f){
                return false;
            }
            if(fighter.health < fighter.maxHealth - 0.001f){
                return false;
            }
        }
        return true;
    }

    protected void updateRearm(CarrierUnitType ctype){
        CarrierLaunchFlow.updateRearm(this, ctype);
    }

    protected boolean runwayReadyForLaunchWave(int runway){
        RunwayLane lane = runwayLane(runway);
        return lane.storedFighterCount >= runwayCapacity(runway) && runwayStoredFightersAllHealthy(runway);
    }

    protected void updateLaunch(CarrierUnitType ctype){
        CarrierLaunchFlow.updateLaunch(this, ctype);
    }

    protected boolean launchOneFromRunway(int runway, CarrierUnitType ctype){
        UnitPayload payload = frontUnitPayloadInRunway(runway);
        if(payload == null) return false;
        if(!launchStateReady(payload, runway, ctype)) return false;

        int launchSlot = deckSlotForPayloadInternal(payload);
        if(!launchFighter(payload, runway, launchSlot, ctype)) return false;

        removeLaunchedPayloadFromDeck(runway, payload);
        return true;
    }

    protected void removeLaunchedPayloadFromDeck(int runway, UnitPayload payload){
        Queue<UnitPayload> queue = runwayQueue(runway);
        if(!queue.isEmpty() && queue.first() == payload){
            queue.removeFirst();
        }else{
            removeUnitPayloadFromQueue(queue, payload);
        }
        if(payload.unit != null){
            deckRefitTimers.remove(payload.unit.id, 0f);
            deckHealPulseTimers.remove(payload.unit.id, 0f);
        }
        payloads.remove(payload, true);
        payloadVisualPos.remove(payload);
        runwayPayloadStates.remove(payload);
        noteDeckFighterLaunched(runway);
        syncPayloadsFromRunwayQueues();
    }

    protected @Nullable UnitPayload frontUnitPayloadInRunway(int runway){
        int r = clampRunway(runway);
        Queue<UnitPayload> queue = runwayQueue(r);
        if(queue.isEmpty()) return null;

        while(!queue.isEmpty()){
            UnitPayload up = queue.first();
            if(up == null || up.unit == null){
                queue.removeFirst();
            }else{
                break;
            }
        }
        if(queue.isEmpty()) return null;

        UnitPayload front = queue.first();
        if(front.unit != null){
            float refit = Math.abs(deckRefitTimers.get(front.unit.id, 0f));
            if(refit > 0.001f){
                return null;
            }
        }
        return front;
    }

    protected boolean launchStateReady(UnitPayload payload, int runway, CarrierUnitType ctype){
        if(payload == null || ctype == null) return false;

        Queue<UnitPayload> queue = runwayQueue(runway);
        if(queue == null || queue.isEmpty() || queue.first() != payload){
            return false;
        }

        RunwayPayloadState state = runwayPayloadStates == null ? null : runwayPayloadStates.get(payload);
        if(state == null || !state.seeded){
            // 队列重排后视觉状态可能尚未种子化，允许队头先发射。
            return true;
        }

        runwayFrontPoint(runway, Tmp.v4);
        float threshold = Math.max(3f, ctype.slotSpacing() * 0.22f);
        float threshold2 = threshold * threshold;
        if(state.current.dst2(Tmp.v4) <= threshold2){
            return true;
        }

        // 航母移动时视觉可能滞后，放宽到 target 点避免发射卡死。
        float movingThreshold = threshold * 2.4f;
        return state.target.dst2(Tmp.v4) <= movingThreshold * movingThreshold;
    }

    protected boolean runwayLaunchBlocked(int runway){
        return runwayLane(runway).launchBlockedByActiveFighter;
    }

    protected boolean launchFighter(UnitPayload payload, int runway, int launchSlot, CarrierUnitType ctype){
        Unit fighter = payload.unit;
        if(fighter == null || fighter.type == null) return false;

        int r = clampRunway(runway);
        resolveRunwayForwardVector(r, Tmp.v3);
        resolveLaunchStartPoint(r, launchSlot, Tmp.v1);
        Tmp.v4.set(Tmp.v1);
        Tmp.v1.mulAdd(Tmp.v3, launchSpawnNudge(fighter));
        resolveLaunchEndPoint(fighter, ctype, Tmp.v4, Tmp.v3, Tmp.v2);

        float launchAngle = Tmp.v3.angle();
        float launchVelocity = Math.max(fighter.type.speed * Math.max(ctype.takeoffSpeedMultiplier, 1f) * 0.6f, 1.2f);

        if(!fighter.type.flying && !fighter.canPass(World.toTile(Tmp.v1.x), World.toTile(Tmp.v1.y))){
            return false;
        }

        fighter.set(Tmp.v1.x, Tmp.v1.y);
        fighter.rotation(launchAngle);
        if(fighter.type.flying){
            // 生成瞬间保持最低抬升，减少与航母模型碰撞。
            fighter.elevation = Math.max(fighter.elevation, 0.22f);
        }
        fighter.id = EntityGroup.nextId();
        if(!fighter.isAdded()){
            fighter.team.data().updateCount(fighter.type, -1);
        }

        bindFighter(fighter, r);
        fighter.add();
        fighter.unloaded();

        sanitizeSpawnedFighterPosition(fighter, r, Tmp.v4);
        // 用最终生成位置作为特效与起飞起点，避免视觉错位。
        Tmp.v1.set(fighter.x, fighter.y);

        fighter.rotation(launchAngle);
        fighter.vel.set(Tmp.v3).scl(launchVelocity);

        Fx.unitDrop.at(Tmp.v1.x, Tmp.v1.y, launchAngle);

        if(fighter.controller() instanceof CarrierBoundAIC ai){
            ai.beginTakeoff(Tmp.v1, Tmp.v2, ctype.takeoffDuration, ctype.takeoffSpeedMultiplier);
        }

        if(ctype.takeoffEffect != null){
            ctype.takeoffEffect.at(Tmp.v1.x, Tmp.v1.y, launchAngle);
        }

        activeFighters.add(fighter.id);
        sortieElapsed.put(fighter.id, 0f);

        Events.fire(new PayloadDropEvent(this, fighter));
        return true;
    }

    protected void resolveRunwayForwardVector(int runway, Vec2 out){
        runwayForwardVector(clampRunway(runway), out);
        if(out.len2() < 0.001f){
            out.trns(rotation - 90f, 1f);
        }
        if(out.len2() < 0.001f){
            out.set(0f, 1f);
        }
        out.nor();
    }

    protected void resolveLaunchStartPoint(int runway, int launchSlot, Vec2 out){
        int r = clampRunway(runway);
        runwayFrontPoint(r, out);

        if(invalidLaunchPoint(out) && launchSlot >= 0 && launchSlot < deckSlotCount()){
            deckSlotWorld(launchSlot, out);
        }
        if(invalidLaunchPoint(out)){
            runwayFrontPoint(r, out);
        }
        if(invalidLaunchPoint(out)){
            out.set(x, y);
        }
    }

    protected float launchSpawnNudge(Unit fighter){
        return 0f;
    }

    protected float launchTravelDistance(Unit fighter, CarrierUnitType ctype){
        return Math.max(Math.max(ctype.launchForwardOffset, 0f), Math.max(fighter.hitSize * 2.8f, 36f));
    }

    protected void resolveLaunchEndPoint(Unit fighter, CarrierUnitType ctype, Vec2 launchStart, Vec2 launchDir, Vec2 out){
        out.set(launchStart).mulAdd(launchDir, launchTravelDistance(fighter, ctype));
        if(invalidLaunchPoint(out)){
            out.set(launchStart).add(launchDir);
        }
    }

    protected void sanitizeSpawnedFighterPosition(Unit fighter, int runway, Vec2 launchStart){
        Tmp.v4.set(fighter.x, fighter.y);
        if(!invalidLaunchPoint(Tmp.v4)) return;

        runwayFrontPoint(clampRunway(runway), Tmp.v4);
        if(invalidLaunchPoint(Tmp.v4)){
            Tmp.v4.set(x, y);
        }
        fighter.set(Tmp.v4.x, Tmp.v4.y);
        launchStart.set(Tmp.v4);
    }

    protected void recoverFighterToQueue(Unit fighter, int runway){
        int r = clampRunway(runway);

        if(fighter.isAdded()){
            fighter.team.data().updateCount(fighter.type, 1);
        }
        fighter.remove();
        bindFighter(fighter, r);
        deckHealPulseTimers.remove(fighter.id, 0f);
        float refit = Math.max(carrierType() == null ? 0f : carrierType().recoverRefitTime, 0f);
        if(refit > 0.001f){
            // 负值表示“有整备时间但不显示建造投影”。
            deckRefitTimers.put(fighter.id, -refit);
        }else{
            deckRefitTimers.remove(fighter.id, 0f);
        }

        UnitPayload recovered = new UnitPayload(fighter);
        runwayQueue(r).addLast(recovered);
        noteAirborneFighterRecovered(fighter, r);
        // 回收首帧放在尾点，再由视觉插值并入队列。
        recoveryPoint(r, Tmp.v1);
        if(invalidLaunchPoint(Tmp.v1)){
            runwayQueueBackPoint(r, Tmp.v1);
        }
        if(invalidLaunchPoint(Tmp.v1)){
            runwayQueueInsertPoint(r, Tmp.v1);
        }
        if(!invalidLaunchPoint(Tmp.v1)){
            if(payloadVisualPos == null){
                payloadVisualPos = new ObjectMap<>();
            }
            payloadVisualPos.put(recovered, new Vec2(Tmp.v1));
        }
        syncPayloadsFromRunwayQueues();
        runwayLane(r).rearmReload = 0f;

        Fx.unitPickup.at(fighter);
        if(Vars.netClient != null){
            Vars.netClient.clearRemovedEntity(fighter.id);
        }
        Events.fire(new PickupEvent(this, fighter));
    }

    protected void removeActiveFighter(int fighterId){
        releaseRunwayRecoveryClaimById(fighterId);
        for(int i = activeFighters.size - 1; i >= 0; i--){
            if(activeFighters.get(i) == fighterId){
                activeFighters.removeIndex(i);
            }
        }
        sortieElapsed.remove(fighterId, 0f);
    }

    protected void removeActiveFighterAt(int index, int fighterId){
        releaseRunwayRecoveryClaimById(fighterId);
        activeFighters.removeIndex(index);
        sortieElapsed.remove(fighterId, 0f);
    }

    protected void killCarrierFighterOnHostRemoved(Unit fighter){
        if(fighter == null || fighter.dead()) return;
        fighter.flag(0d);
        fighter.kill();
    }

    @Override
    public void remove(){
        for(int i = activeFighters.size - 1; i >= 0; i--){
            Unit fighter = Groups.unit.getByID(activeFighters.get(i));
            if(fighter != null && isBoundFighter(fighter)){
                killCarrierFighterOnHostRemoved(fighter);
            }
        }

        for(Payload payload : payloads){
            if(payload instanceof UnitPayload up && up.unit != null && isBoundFighter(up.unit)){
                killCarrierFighterOnHostRemoved(up.unit);
            }
        }
        activeFighters.clear();
        sortieElapsed.clear();
        deckRefitTimers.clear();
        deckHealPulseTimers.clear();
        resetTransientRunwayState();
        super.remove();
    }

    protected void applyLegacyRunwayTiming(boolean launchWave, float launchReload, float recoverReload, float rearmReload, float regroupDelay, int launchRunway){
        ensureRunwayLanes();
        for(RunwayLane lane : runwayLanes){
            if(lane == null) continue;
            lane.launchReload = 0f;
            lane.recoverReload = Math.max(recoverReload, 0f);
            lane.rearmReload = Math.max(rearmReload, 0f);
            lane.regroupDelayTimer = Math.max(regroupDelay, 0f);
            lane.launchWaveActive = false;
        }

        if(runwayLanes.length > 0){
            RunwayLane lane = runwayLane(launchRunway);
            lane.launchReload = Math.max(launchReload, 0f);
            lane.launchWaveActive = launchWave;
        }
    }

    protected boolean legacyLaunchWaveActive(){
        if(runwayLanes == null) return false;
        for(RunwayLane lane : runwayLanes){
            if(lane != null && lane.launchWaveActive){
                return true;
            }
        }
        return false;
    }

    protected float legacyMinRecoverReload(){
        if(runwayLanes == null || runwayLanes.length == 0) return 0f;
        float min = Float.MAX_VALUE;
        for(RunwayLane lane : runwayLanes){
            if(lane == null) continue;
            min = Math.min(min, lane.recoverReload);
        }
        return min == Float.MAX_VALUE ? 0f : min;
    }

    protected float legacyLaunchReload(){
        if(runwayLanes == null || runwayLanes.length == 0) return 0f;
        for(RunwayLane lane : runwayLanes){
            if(lane != null && lane.launchWaveActive){
                return lane.launchReload;
            }
        }
        return 0f;
    }

    protected float legacyMinRearmReload(){
        if(runwayLanes == null || runwayLanes.length == 0) return 0f;
        float min = Float.MAX_VALUE;
        for(RunwayLane lane : runwayLanes){
            if(lane == null) continue;
            min = Math.min(min, lane.rearmReload);
        }
        return min == Float.MAX_VALUE ? 0f : min;
    }

    protected float legacyMaxRegroupDelay(){
        if(runwayLanes == null || runwayLanes.length == 0) return 0f;
        float max = 0f;
        for(RunwayLane lane : runwayLanes){
            if(lane == null) continue;
            max = Math.max(max, lane.regroupDelayTimer);
        }
        return max;
    }

    protected int legacyLaunchRunwayCursor(){
        if(runwayLanes == null || runwayLanes.length == 0) return 0;
        for(RunwayLane lane : runwayLanes){
            if(lane != null && lane.launchWaveActive){
                return lane.runway;
            }
        }
        return 0;
    }

    @Override
    public void read(Reads read){
        super.read(read);
        deckInitialized = read.bool();
        regrouping = read.bool();
        boolean legacyLaunchWave = read.bool();
        float legacyLaunchReload = read.f();
        float legacyRecoverReload = read.f();
        float legacyRearmReload = read.f();
        float legacyRegroupDelay = read.f();
        targetFighterCount = read.i();
        lossCount = read.i();
        int legacyLaunchRunwayCursor = read.i();
        read.i();

        activeFighters.clear();
        int activeSize = read.i();
        for(int i = 0; i < activeSize; i++){
            activeFighters.add(read.i());
        }

        sortieElapsed.clear();
        int sortieSize = read.i();
        for(int i = 0; i < sortieSize; i++){
            int fighterId = read.i();
            float elapsed = read.f();
            sortieElapsed.put(fighterId, elapsed);
        }

        targetRunwayCounts.clear();
        int runwaySize = read.i();
        for(int i = 0; i < runwaySize; i++){
            targetRunwayCounts.add(read.i());
        }

        deckRefitTimers.clear();
        deckHealPulseTimers.clear();
        int refitSize = read.i();
        for(int i = 0; i < refitSize; i++){
            int fighterId = read.i();
            float remain = read.f();
            if(Math.abs(remain) > 0.001f){
                deckRefitTimers.put(fighterId, remain);
            }
        }

        rebuildDeckSlots();
        trimPayloadToDeck();
        resetTransientRunwayState();

        CarrierUnitType ctype = carrierType();
        if(ctype != null){
            rebuildTargetRunwayCounts(ctype);
            normalizeDeckOrdering(ctype);
            applyLegacyRunwayTiming(legacyLaunchWave, legacyLaunchReload, legacyRecoverReload, legacyRearmReload, legacyRegroupDelay, legacyLaunchRunwayCursor);
        }
    }

    @Override
    public void write(Writes write){
        super.write(write);
        write.bool(deckInitialized);
        write.bool(regrouping);
        write.bool(legacyLaunchWaveActive());
        write.f(legacyLaunchReload());
        write.f(legacyMinRecoverReload());
        write.f(legacyMinRearmReload());
        write.f(legacyMaxRegroupDelay());
        write.i(targetFighterCount);
        write.i(lossCount);
        write.i(legacyLaunchRunwayCursor());
        write.i(0);

        write.i(activeFighters.size);
        for(int i = 0; i < activeFighters.size; i++){
            write.i(activeFighters.get(i));
        }

        write.i(sortieElapsed.size);
        for(IntFloatMap.Entry entry : sortieElapsed){
            write.i(entry.key);
            write.f(entry.value);
        }

        write.i(targetRunwayCounts.size);
        for(int i = 0; i < targetRunwayCounts.size; i++){
            write.i(targetRunwayCounts.get(i));
        }

        write.i(deckRefitTimers.size);
        for(IntFloatMap.Entry entry : deckRefitTimers){
            write.i(entry.key);
            write.f(entry.value);
        }
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);

        write.bool(deckInitialized);
        write.bool(regrouping);
        write.i(targetFighterCount);
        write.i(lossCount);

        write.s(Math.min(activeFighters.size, 32767));
        for (int i = 0; i < activeFighters.size && i < 32767; i++) {
            write.i(activeFighters.get(i));
        }

        write.b(Math.min(targetRunwayCounts.size, 255));
        for (int i = 0; i < targetRunwayCounts.size && i < 255; i++) {
            write.i(targetRunwayCounts.get(i));
        }

        write.s(Math.min(deckRefitTimers.size, 32767));
        int refitWritten = 0;
        for (IntFloatMap.Entry entry : deckRefitTimers) {
            if (refitWritten >= 32767) break;
            write.i(entry.key);
            write.f(entry.value);
            refitWritten++;
        }
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);

        boolean syncDeckInitialized = read.bool();
        boolean syncRegrouping = read.bool();
        int syncTargetFighterCount = read.i();
        int syncLossCount = read.i();

        IntSeq syncActiveFighters = new IntSeq();
        int activeSize = read.us();
        for (int i = 0; i < activeSize; i++) {
            syncActiveFighters.add(read.i());
        }

        IntSeq syncTargetRunwayCounts = new IntSeq();
        int runwaySize = read.ub();
        for (int i = 0; i < runwaySize; i++) {
            syncTargetRunwayCounts.add(read.i());
        }

        IntFloatMap syncDeckRefitTimers = new IntFloatMap();
        int refitSize = read.us();
        for (int i = 0; i < refitSize; i++) {
            int fighterId = read.i();
            float remain = read.f();
            if (Math.abs(remain) > 0.001f) {
                syncDeckRefitTimers.put(fighterId, remain);
            }
        }

        if (!isLocal()) {
            deckInitialized = syncDeckInitialized;
            regrouping = syncRegrouping;
            targetFighterCount = syncTargetFighterCount;
            lossCount = syncLossCount;

            activeFighters.clear();
            activeFighters.addAll(syncActiveFighters);

            targetRunwayCounts.clear();
            targetRunwayCounts.addAll(syncTargetRunwayCounts);

            deckRefitTimers.clear();
            for (IntFloatMap.Entry entry : syncDeckRefitTimers) {
                deckRefitTimers.put(entry.key, entry.value);
            }

            deckHealPulseTimers.clear();
        }
    }
}
