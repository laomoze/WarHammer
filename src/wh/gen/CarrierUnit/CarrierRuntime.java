package wh.gen.CarrierUnit;

import arc.Events;
import arc.math.Angles;
import arc.math.Interp;
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
import mindustry.entities.units.WeaponMount;
import mindustry.game.EventType.PayloadDropEvent;
import mindustry.game.EventType.PickupEvent;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;
import wh.entities.world.entities.CarrierUnitType;
import wh.gen.CarrierFighterUnit;
import wh.gen.CarrierUnit.UnitAI.CarrierBoundAIC;
import wh.gen.CarrierUnit.UnitAI.CarrierFighterAI;
import wh.gen.EntityRegister;

public class CarrierRuntime extends CarrierUnit implements CarrierHostc{
    private static final float eps = 0.001f;
    private static final float targetRangeBase = 300f;
    private static final float targetRangeCarrierScale = 1.7f;
    private static final float mountRangeFloor = 60f;
    private static final float mountRangePadding = 240f;
    private static final float focusEnemyProbeRange = 40f;
    private static final float focusMinDistance = 28f;
    private static final int intervalClaimCleanup = 0;
    private static final int intervalAirborneRescan = 1;
    private static final int intervalVisualCleanup = 2;
    private static final int intervalDeckPrune = 3;

    public static class RunwayPayloadState{
        public int runway = 0;
        public int slot = -1;//单位占用的槽位编号。
        public final Vec2 current = new Vec2();
        public final Vec2 target = new Vec2();

        public void snap(){
            current.set(target);
        }

        public void update(float alpha) {
            float amount = Mathf.clamp(alpha, 0f, 1f);
            current.x = Mathf.lerpDelta(current.x, target.x, amount);
            current.y = Mathf.lerpDelta(current.y, target.y, amount);
        }
    }

    public static class RunwayLane{
        public int runway;
        public final Queue<UnitPayload> deck = new Queue<>();
        public final IntSeq airborne = new IntSeq();
        public float launchReload = 0f;
        public float rearmReload = 0f;
        public int recoveryClaim = -1;
        public boolean launching = false;

        public RunwayLane(int runway){
            this.runway = runway;
        }
    }

    // 甲板是否已完成初始装填。
    public boolean deckInitialized = false;
    // 已放飞战机的出击计时。
    public IntFloatMap sortieTimers = new IntFloatMap();
    // 甲板战机的整备/回收冷却计时。
    public IntFloatMap deckRefitTimers = new IntFloatMap();
    // 甲板战机的维修回血计时。
    public transient IntFloatMap deckHealTimers = new IntFloatMap();
    // 各跑道的甲板、在空与回收占位状态。
    public transient RunwayLane[] lanes = new RunwayLane[0];
    // 无法归入跑道队列的额外 payload。
    public transient Seq<Payload> overflow = new Seq<>();
    // 甲板 payload 的队列/槽位过渡状态。
    public transient ObjectMap<UnitPayload, RunwayPayloadState> payloadStates = new ObjectMap<>();
    // 甲板 payload 的视觉位置缓存。
    public transient ObjectMap<Payload, Vec2> payloadVisuals = new ObjectMap<>();
    public transient Interval runtimeIntervals = new Interval(4);
    // 甲板队列与 payloads 是否需要重同步。
    public transient boolean deckDirty = true;
    // 视觉锚点是否已建立。
    public transient boolean visualAnchorValid = false;
    // 上一帧视觉锚点位置/朝向。
    public transient float visualAnchorX = 0f;
    public transient float visualAnchorY = 0f;
    public transient float visualAnchorRot = 0f;

    private final transient Vec2 runwayScratch = new Vec2();
    private final transient IntSeq staleIdsScratch = new IntSeq();
    private final transient IntSet uniqueIdsScratch = new IntSet();
    private final transient ObjectSet<Payload> livePayloadsScratch = new ObjectSet<>();
    private final transient Seq<Payload> stalePayloadsScratch = new Seq<>();
    private final transient Seq<UnitPayload> staleStateScratch = new Seq<>();

    @Override
    public int classId(){
        return EntityRegister.getId(getClass());
    }

    @Override
    public float mass() {
        return 114514;
    }

    @Override
    public void setType(UnitType type) {
        super.setType(type);
        deckInitialized = false;
        sortieTimers.clear();
        deckRefitTimers.clear();
        deckHealTimers.clear();
        resetTransientState();
        trimPayloadToDeck();
    }

    @Override
    public void update(){
        super.update();

        CarrierUnitType ctype = carrierType();
        if(ctype == null) return;

        // 主运行入口：维护跑道/甲板状态，处理在舰、回收、补充、起飞与可视化同步。
        ensureLanes();
        trimPayloadToDeck();
        if(!deckInitialized){
            initDeck(ctype);
            deckInitialized = true;
        }
        ensureDeckSync();

        if (!Vars.net.client()) {
            updateAirborneState();
            updateDeckMaintenance(ctype);
            updateRearm(ctype);
            if (runtimeIntervals.get(intervalClaimCleanup, 12f)) {
                cleanupRecoveryClaims();
            }
            if (runtimeIntervals.get(intervalAirborneRescan, 35f)) {
                rescanAirborneFighters();
            }
            updateLaunch(ctype);
        }

        updatePayloadVisuals(ctype);
        if (runtimeIntervals.get(intervalVisualCleanup, 30f)) {
            cleanupPayloadVisuals();
        }
    }

    protected void resetTransientState() {
        deckHealTimers.clear();
        lanes = new RunwayLane[0];
        overflow.clear();
        payloadStates.clear();
        payloadVisuals.clear();
        runtimeIntervals = new Interval(4);
        deckDirty = true;
        visualAnchorValid = false;
        visualAnchorX = 0f;
        visualAnchorY = 0f;
        visualAnchorRot = 0f;
        staleIdsScratch.clear();
        uniqueIdsScratch.clear();
        livePayloadsScratch.clear();
        stalePayloadsScratch.clear();
        staleStateScratch.clear();
    }

    protected void ensureLanes() {
        int runways = runwayCount();
        if (lanes.length != runways) {
            RunwayLane[] rebuilt = new RunwayLane[runways];
            for (int i = 0; i < runways; i++) {
                rebuilt[i] = i < lanes.length && lanes[i] != null ? lanes[i] : new RunwayLane(i);
                rebuilt[i].runway = i;
            }
            lanes = rebuilt;
            deckDirty = true;
        }
    }

    protected RunwayLane lane(int runway) {
        ensureLanes();
        return lanes[clampRunway(runway)];
    }

    protected void initDeck(CarrierUnitType ctype) {
        ensureDeckSync();
        if (!payloads.isEmpty() || !ctype.hasAnyFighterType()) return;

        for (int runway = 0; runway < runwayCount(); runway++) {
            for (int i = 0; i < runwayCapacity(runway); i++) {
                createDeckFighter(ctype, runway, false);
            }
        }
    }

    protected void ensureDeckSync() {
        ensureLanes();
        // 保证 lane.deck / overflow / payloads 三者始终对应，必要时重建甲板队列。
        if (deckDirty || !deckQueuesMatchPayloads()) {
            rebuildDeckFromPayloads();
        }
    }

    protected boolean deckQueuesMatchPayloads() {
        int total = overflow.size;
        for (RunwayLane lane : lanes) {
            total += lane.deck.size;
        }
        if (total != payloads.size) return false;

        for (Payload payload : payloads) {
            if (overflow.contains(payload, true)) continue;
            if (payload instanceof UnitPayload up && queueContains(up)) continue;
            return false;
        }
        return true;
    }

    protected boolean queueContains(UnitPayload target) {
        for (RunwayLane lane : lanes) {
            for (UnitPayload payload : lane.deck) {
                if (payload == target) return true;
            }
        }
        return false;
    }

    protected void rebuildDeckFromPayloads() {
        ensureLanes();
        overflow.clear();
        for (RunwayLane lane : lanes) {
            lane.deck.clear();
        }

        int[] fill = new int[runwayCount()];
        for(Payload payload : payloads){
            if (payload instanceof UnitPayload up && up.unit != null) {
                int runway = resolvePayloadRunway(up.unit, fill);
                if (runway >= 0 && fill[runway] < runwayCapacity(runway)) {
                    bindFighter(up.unit, runway);
                    lane(runway).deck.addLast(up);
                    fill[runway]++;
                    continue;
                }
            }
            overflow.add(payload);
        }
        deckDirty = false;
    }

    protected int resolvePayloadRunway(Unit fighter, int[] fill) {
        int runway = ownsFighter(fighter) ? fighterRunway(fighter) : 0;
        if (runway >= 0 && runway < fill.length && fill[runway] < runwayCapacity(runway)) {
            return runway;
        }
        for (int i = 0; i < fill.length; i++) {
            if (fill[i] < runwayCapacity(i)) return i;
        }
        return -1;
    }

    protected void syncPayloadsFromLanes() {
        // 按各跑道队列重新拼出 payloads，保持实际存储顺序与跑道队列一致。
        payloads.clear();
        for (RunwayLane lane : lanes) {
            for (UnitPayload payload : lane.deck) {
                payloads.add(payload);
            }
        }
        payloads.addAll(overflow);
        deckDirty = false;
        trimPayloadToDeck();
    }

    protected void trimPayloadToDeck() {
        while (payloads.size > deckSlotCount() && !payloads.isEmpty()) {
            Payload removed = payloads.pop();
            removePayloadEverywhere(removed);
            payloadVisuals.remove(removed);
            if (removed instanceof UnitPayload up) {
                payloadStates.remove(up);
                if (up.unit != null) {
                    deckRefitTimers.remove(up.unit.id, 0f);
                    deckHealTimers.remove(up.unit.id, 0f);
                }
            }
            removed.remove();
            deckDirty = true;
        }
    }

    protected void removePayloadEverywhere(Payload payload) {
        overflow.remove(payload, true);
        if (payload instanceof UnitPayload up) {
            for (RunwayLane lane : lanes) {
                removeUnitPayload(lane.deck, up);
            }
        }
    }

    protected void removeUnitPayload(Queue<UnitPayload> queue, UnitPayload target) {
        if (queue.isEmpty()) return;
        Queue<UnitPayload> rebuilt = new Queue<>();
        while (!queue.isEmpty()) {
            UnitPayload current = queue.removeFirst();
            if (current != target) {
                rebuilt.addLast(current);
            }
        }
        while (!rebuilt.isEmpty()) {
            queue.addLast(rebuilt.removeFirst());
        }
    }

    protected void bindFighter(Unit fighter, int runway) {
        if (fighter == null) return;

        int r = clampRunway(runway);
        fighter.team = team;
        // 把战机重新绑定到当前航母与指定跑道，并确保它使用舰载机专用 AI。
        if (fighter instanceof CarrierFighterUnit data) {
            data.setCarrierBinding(id, r);
        }

        if (fighter.controller() instanceof CarrierBoundAIC ai) {
            ai.setCarrier(id).setRunway(r);
        }else{
            fighter.controller(new CarrierFighterAI(id, r));
        }
    }

    protected void clearFighterBinding(Unit fighter) {
        if (fighter instanceof CarrierFighterUnit data) {
            data.clearCarrierBinding();
        }
        if (fighter.controller() instanceof CarrierFighterAI ai) {
            ai.setCarrier(-1).setRunway(0);
        }
    }

    @Override
    public boolean ownsFighter(Unit fighter) {
        if (fighter == null) return false;
        if (fighter instanceof CarrierFighterUnit data) {
            return data.carrierId == id;
        }
        return fighter.controller() instanceof CarrierFighterAI ai && ai.carrierId() == id;
    }

    @Override
    public int fighterRunway(Unit fighter) {
        if (fighter instanceof CarrierFighterUnit data && data.carrierId == id) {
            return clampRunway(data.runway);
        }
        if (fighter.controller() instanceof CarrierFighterAI ai && ai.carrierId() == id) {
            return clampRunway(ai.runwayIndex());
        }
        return 0;
    }

    @Override
    public float fighterSortieTime(Unit fighter) {
        return fighter == null ? 0f : sortieTimers.get(fighter.id, 0f);
    }

    public int storedFighterCountInRunway(int runway) {
        ensureDeckSync();
        return lane(runway).deck.size;
    }

    public int assignedFighterCountInRunway(int runway) {
        RunwayLane lane = lane(runway);
        return lane.deck.size + lane.airborne.size;
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
        if(ctype.runways.any()){
            int remain = index;
            CarrierUnitType.Runway resolved = null;
            int runway = 0;
            int local = 0;
            for (int i = 0; i < ctype.runways.size; i++) {
                CarrierUnitType.Runway def = ctype.runways.get(i);
                int cap = Math.max(def.capacity, 1);
                if(remain < cap){
                    resolved = def;
                    runway = i;
                    local = remain;
                    break;
                }
                remain -= cap;
            }

            if(resolved == null){
                resolved = ctype.runways.peek();
                runway = Math.max(ctype.runways.size - 1, 0);
                local = Math.max(Math.max(resolved.capacity, 1) - 1, 0);
            }

            runwayFrontPoint(runway, out);
            float spacing = ctype.runwaySlotSpacing(runway);
            Vec2 forward = runwayForwardVector(runway, runwayScratch);
            out.sub(forward.x * local * spacing, forward.y * local * spacing);
            return;
        }

        out.set(x, y);
    }

    @Override
    public int deckSlotForPayload(Payload payload) {
        ensureDeckSync();
        return deckSlotForPayloadInternal(payload);
    }

    protected int deckSlotForPayloadInternal(Payload payload){
        if(payload == null || deckSlotCount() <= 0) return -1;
        if (overflow.contains(payload, true)) return -1;

        if (payload instanceof UnitPayload up) {
            RunwayPayloadState state = payloadStates.get(up);
            if(state != null && state.slot >= 0 && state.slot < deckSlotCount()){
                return state.slot;
            }
        }

        for (int runway = 0; runway < runwayCount(); runway++) {
            int local = 0;
            for (UnitPayload payloadInLane : lane(runway).deck) {
                if (payloadInLane == payload) {
                    return runwayFirstSlot(runway) + local;
                }
                local++;
            }
        }

        int linear = payloads.indexOf(payload, true);
        return linear < 0 ? -1 : Mathf.clamp(linear, 0, deckSlotCount() - 1);
    }

    @Override
    public void deckSlotWorldVisual(Payload payload, int slot, Vec2 out) {
        if (out == null) return;

        if (slot < 0) {
            out.set(x, y);
            return;
        }

        Vec2 visual = payloadVisuals.get(payload);
        if (visual != null) {
            out.set(visual);
        } else {
            deckSlotWorld(slot, out);
        }
    }

    @Override
    public float deckRefitRemaining(int fighterId){
        return fighterId < 0 ? 0f : Math.abs(deckRefitTimers.get(fighterId, 0f));
    }

    @Override
    public boolean deckRefitShowsConstruct(int fighterId){
        return fighterId >= 0 && deckRefitTimers.get(fighterId, 0f) > eps;
    }

    @Override
    public void runwayFrontPoint(int runway, Vec2 out) {
        if (out == null) return;

        CarrierUnitType ctype = carrierType();
        if(ctype == null){
            out.set(this);
            return;
        }

        if(ctype.runways.any()){
            CarrierUnitType.Runway def = ctype.runways.get(clampRunway(runway));
            float rot = rotation - 90f;
            out.set(Angles.trnsx(rot, def.x, def.y) + x, Angles.trnsy(rot, def.x, def.y) + y);
            return;
        }

        deckSlotWorld(runwayFirstSlot(runway), out);
    }

    public void runwayBackPoint(int runway, Vec2 out) {
        if (out == null) return;
        runwayFrontPoint(runway, out);
        Vec2 forward = runwayForwardVector(runway, runwayScratch);
        out.sub(forward.x * runwayDeckDepth(runway), forward.y * runwayDeckDepth(runway));
    }

    public float runwayDeckDepth(int runway){
        CarrierUnitType ctype = carrierType();
        if(ctype == null) return 0f;
        return Math.max(runwayCapacity(runway) - 1, 0) * ctype.runwaySlotSpacing(runway);
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
            out.trns(rotation - 90f, 1f);
        }

        if(out.len2() < 0.001f){
            out.set(0f, 1f);
        }
        return out.nor();
    }

    @Override
    public void launchExitPoint(int runway, Vec2 out) {
        if (out == null) return;
        runwayFrontPoint(runway, out);
        Vec2 forward = runwayForwardVector(runway, runwayScratch);
        float offset = Math.max(carrierType() == null ? 0f : carrierType().runwayLaunchOffset(runway), 0f);
        out.add(forward.x * offset, forward.y * offset);
    }

    @Override
    public void recoveryPoint(int runway, Vec2 out) {
        if (out == null) return;

        CarrierUnitType ctype = carrierType();
        if(ctype == null){
            out.set(this);
            return;
        }

        deckSlotWorld(runwayLastSlot(runway), out);
        Vec2 forward = runwayForwardVector(runway, runwayScratch);
        out.sub(forward.x * Math.abs(ctype.runwayRecoverOffset(runway)), forward.y * Math.abs(ctype.runwayRecoverOffset(runway)));
        if(invalidLaunchPoint(out)){
            runwayBackPoint(runway, out);
        }
        if(invalidLaunchPoint(out)){
            out.set(x, y);
        }
    }

    public void recoveryReversePoint(int runway, Vec2 out) {
        recoveryReversePoint(runway, 0f, out);
    }

    public Vec2 recoveryReversePoint(int runway, float distance, Vec2 out){
        if(out == null) out = Tmp.v1;

        recoveryPoint(runway, Tmp.v1);
        runwayFrontPoint(runway, Tmp.v2);
        if (invalidLaunchPoint(Tmp.v1)) return out.set(x, y);

        float len = Math.max(distance, 0f);
        if(len <= 0.001f){
            CarrierUnitType ctype = carrierType();
            len = ctype == null ? 48f : Math.max(48f, Math.max(ctype.landingApproachRadius * 2.35f, ctype.recoverRadius * 2.7f));
        }

        Tmp.v3.set(Tmp.v1).sub(Tmp.v2);
        if(Tmp.v3.len2() < 0.0001f){
            runwayForwardVector(runway, Tmp.v3).scl(-1f);
        }else{
            Tmp.v3.nor();
        }
        return out.set(Tmp.v1.x + Tmp.v3.x * len, Tmp.v1.y + Tmp.v3.y * len);
    }

    @Override
    public void runwayQueueInsertPoint(int runway, Vec2 out) {
        CarrierUnitType ctype = carrierType();
        float distance = ctype == null ? 48f : Math.max(
                ctype.runwaySlotSpacing(runway) * 1.5f,
                Math.max(ctype.landingApproachRadius * 1.75f, ctype.recoverRadius * 2.1f)
        );
        recoveryReversePoint(runway, distance, out);
    }

    protected float commandTargetCheckRange(@Nullable CarrierUnitType ctype) {
        return ctype == null ? Math.max(type.range, targetRangeBase) : Math.max(type.range, ctype.maxFighterDistance * targetRangeCarrierScale);
    }

    protected float mountTargetCheckRange(@Nullable CarrierUnitType ctype) {
        return ctype == null ? (Math.max(type.range, mountRangeFloor) + mountRangePadding) : Math.max(type.range, ctype.maxFighterDistance * targetRangeCarrierScale);
    }

    protected float bestTargetScanRange(@Nullable CarrierUnitType ctype) {
        return ctype == null ? Math.max(type.range, targetRangeBase) : Math.max(type.range, ctype.maxFighterDistance);
    }

    protected @Nullable Teamc commandAttackTarget(float checkRange) {
        if (!(controller() instanceof CommandAI ai)) return null;
        Teamc target = ai.attackTarget;
        return Units.invalidateTarget(target, this, checkRange) ? null : target;
    }

    protected @Nullable Teamc mountAttackTarget(float checkRange) {
        if (mounts == null) return null;
        for (WeaponMount mount : mounts) {
            Teamc target = mount.target;
            if (!Units.invalidateTarget(target, this, checkRange)) return target;
        }
        return null;
    }

    protected @Nullable Teamc bestTargetInRange(float range) {
        return Units.bestTarget(team, x, y, range, u -> u.checkTarget(true, true), b -> true, UnitSorts.weakest);
    }

    protected @Nullable Teamc focusEnemyNearCommandPos(CommandAI ai) {
        if (ai.targetPos == null || !Float.isFinite(ai.targetPos.x) || !Float.isFinite(ai.targetPos.y)) return null;
        float minFocus = Math.max(hitSize * 0.8f, focusMinDistance);
        if (ai.targetPos.within(x, y, minFocus)) return null;

        Teamc pointEnemy = Units.bestEnemy(team, ai.targetPos.x, ai.targetPos.y, focusEnemyProbeRange, u -> u.checkTarget(true, true), UnitSorts.weakest);
        if (pointEnemy != null) return pointEnemy;

        Building tile = Vars.world == null ? null : Vars.world.buildWorld(ai.targetPos.x, ai.targetPos.y);
        return tile != null && tile.team != team && tile.isValid() ? tile : null;
    }

    @Override
    public @Nullable Teamc lockedTarget(){
        CarrierUnitType ctype = carrierType();
        Teamc attack = commandAttackTarget(commandTargetCheckRange(ctype));
        if (attack != null) return attack;

        Teamc mount = mountAttackTarget(mountTargetCheckRange(ctype));
        if (mount != null) return mount;

        return bestTargetInRange(bestTargetScanRange(ctype));
    }

    @Override
    public boolean focusPosition(Vec2 out){
        if(out == null) return false;

        CarrierUnitType ctype = carrierType();
        float focusRange = commandTargetCheckRange(ctype);
        if(controller() instanceof CommandAI ai){
            Teamc attack = ai.attackTarget;
            if (!Units.invalidateTarget(attack, this, focusRange)) {
                out.set(attack);
                return true;
            }

            Teamc pointEnemy = focusEnemyNearCommandPos(ai);
            if (pointEnemy != null) {
                out.set(pointEnemy);
                return true;
            }
        }

        Teamc mount = mountAttackTarget(focusRange);
        if (mount != null) {
            out.set(mount);
            return true;
        }

        Teamc scan = bestTargetInRange(bestTargetScanRange(ctype));
        if (scan != null) {
            out.set(scan);
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRecallFighter(Unit fighter){
        CarrierUnitType ctype = carrierType();
        if(ctype == null || fighter == null) return true;
        if (ctype.sortieDuration > 0f && fighterSortieTime(fighter) >= ctype.sortieDuration) return true;
        if (fighter.healthf() <= Mathf.clamp(ctype.recallHealthf, 0f, 1f)) return true;
        return false;
    }

    protected boolean canRecoverToDeck(int runway) {
        return payloads.size < deckSlotCount() && storedFighterCountInRunway(runway) < runwayCapacity(runway);
    }

    @Override
    public void releaseRecoveryClaim(Unit fighter) {
        if (fighter == null) return;
        for (RunwayLane lane : lanes) {
            if (lane.recoveryClaim == fighter.id) {
                lane.recoveryClaim = -1;
            }
        }
    }

    protected void cleanupRecoveryClaims() {
        CarrierUnitType ctype = carrierType();
        if(ctype == null) return;
        // 未开启逐架回收时，不需要维护跑道“最终进场资格”，直接清空所有 claim。
        if (!ctype.oneByOneRecovery) {
            for (RunwayLane lane : lanes) lane.recoveryClaim = -1;
            return;
        }

        for (RunwayLane lane : lanes) {
            int claim = lane.recoveryClaim;
            if(claim < 0) continue;

            Unit fighter = Groups.unit.getByID(claim);
            // 占位飞机不存在、已死亡、不再属于本航母、已经回收到甲板，
            // 或其当前分配跑道已变化时，这个 claim 就应立即失效。
            if (fighter == null || fighter.dead() || !ownsFighter(fighter) || fighterStoredOnDeck(claim) || fighterRunway(fighter) != lane.runway) {
                lane.recoveryClaim = -1;
                continue;
            }
            // 只有仍由舰载机 AI 控制，且仍处于返航/降落流程中的飞机，
            // 才允许继续保留这条跑道的最终进场资格。
            if (!(fighter.controller() instanceof CarrierBoundAIC ai) || (!ai.isReturning() && !ai.isLanding())) {
                lane.recoveryClaim = -1;
                continue;
            }

            runwayQueueInsertPoint(lane.runway, Tmp.v1);
            float radius = Math.max(ctype.recoverRadius * 4f, ctype.landingApproachRadius * 3f);
            // 即便状态仍合法，只要飞机已经远离本跑道等待区，也释放 claim，
            // 避免旧占位长期卡住后续回收。
            if (!fighter.within(Tmp.v1, radius)) {
                lane.recoveryClaim = -1;
            }
        }
    }

    @Override
    public boolean allowRecoveryApproach(Unit fighter){
        if(fighter == null || fighter.dead()) return false;
        CarrierUnitType ctype = carrierType();
        if (ctype == null || !ownsFighter(fighter)) return false;

        int runway = fighterRunway(fighter);
        if (!canRecoverToDeck(runway)) return false;
        // 未开启 oneByOneRecovery 时，只要基础条件满足就允许直接进入最后进场。
        if (!ctype.oneByOneRecovery) return true;

        // 开启逐架回收时，同一跑道同一时刻只允许一架飞机占用最终进场资格。
        cleanupRecoveryClaims();
        RunwayLane lane = lane(runway);
        // recoveryClaim < 0 表示当前无人占位；
        // recoveryClaim == fighter.id 表示该资格本来就属于当前飞机，允许继续完成降落流程。
        if (lane.recoveryClaim < 0 || lane.recoveryClaim == fighter.id) {
            lane.recoveryClaim = fighter.id;
            return true;
        }
        return false;
    }

    protected float recoveryTouchdownRadius(Unit fighter, CarrierUnitType ctype, int runway) {
        if (fighter == null || ctype == null) return 0f;
        return Math.max(3.5f, Math.max(ctype.runwaySlotSpacing(runway) * 0.18f, fighter.hitSize * 0.4f));
    }

    protected boolean nearRecoveryTouchdown(Unit fighter, int runway, float radius) {
        if (fighter == null) return false;
        recoveryPoint(runway, Tmp.v4);
        return !invalidLaunchPoint(Tmp.v4) && fighter.within(Tmp.v4.x, Tmp.v4.y, radius);
    }

    @Override
    public boolean tryRecoverFighter(Unit fighter) {
        if (fighter == null || !fighter.isValid() || fighter.dead()) return false;
        CarrierUnitType ctype = carrierType();
        if (ctype == null || !ownsFighter(fighter)) return false;

        int runway = fighterRunway(fighter);
        if (!canRecoverToDeck(runway)) return false;
        if (!nearRecoveryTouchdown(fighter, runway, recoveryTouchdownRadius(fighter, ctype, runway))) return false;
        // 战机 AI 会在真正接地时才允许回收；这里等于在航母侧再做一次最终确认，
        // 避免飞机还没完成最后进场就被提前收入甲板。
        if (fighter.controller() instanceof CarrierBoundAIC ai && !ai.canRecoverNow()) return false;

        recoverFighterToDeck(fighter, runway);
        return true;
    }

    protected void updateAirborneState() {
        // 维护各跑道 airborne 列表：去重、剔除失效目标、修正跑道归属，并累计 sortie 时间。
        for (RunwayLane lane : lanes) {
            uniqueIdsScratch.clear();
            for (int i = lane.airborne.size - 1; i >= 0; i--) {
                int fighterId = lane.airborne.get(i);
                if (uniqueIdsScratch.contains(fighterId)) {
                    lane.airborne.removeIndex(i);
                    continue;
                }
                uniqueIdsScratch.add(fighterId);

                Unit fighter = Groups.unit.getByID(fighterId);
                if (fighter == null || fighter.dead() || fighterStoredOnDeck(fighterId) || !ownsFighter(fighter)) {
                    lane.airborne.removeIndex(i);
                    sortieTimers.remove(fighterId, 0f);
                    continue;
                }

                int runway = fighterRunway(fighter);
                if (runway != lane.runway) {
                    lane.airborne.removeIndex(i);
                    addAirborne(runway, fighterId);
                    continue;
                }

                bindFighter(fighter, runway);
                sortieTimers.put(fighterId, sortieTimers.get(fighterId, 0f) + Time.delta);
            }
        }
    }

    protected void rescanAirborneFighters() {
        for (Unit fighter : Groups.unit) {
            if (fighter == this || !ownsFighter(fighter) || fighterStoredOnDeck(fighter.id)) continue;
            addAirborne(fighterRunway(fighter), fighter.id);
        }
    }

    protected void addAirborne(int runway, int fighterId) {
        RunwayLane lane = lane(runway);
        for (int i = 0; i < lane.airborne.size; i++) {
            if (lane.airborne.get(i) == fighterId) return;
        }
        lane.airborne.add(fighterId);
    }

    protected boolean fighterStoredOnDeck(int fighterId) {
        if (fighterId < 0) return false;
        for (Payload payload : payloads) {
            if (payload instanceof UnitPayload up && up.unit != null && up.unit.id == fighterId) {
                return true;
            }
        }
        return false;
    }

    protected void updateDeckMaintenance(CarrierUnitType ctype){
        IntSet live = uniqueIdsScratch;
        live.clear();

        boolean allowHeal = ctype.recoverHealFraction > 0.0001f;
        float healInterval = allowHeal ? Math.max(ctype.recoverHealInterval, 1f) : 1f;
        float healFraction = allowHeal ? Mathf.clamp(ctype.recoverHealFraction, 0f, 1f) : 0f;

        // 处理甲板内战机的整备/维修计时，并定期清理失效计时器条目。
        for(Payload payload : payloads){
            if(!(payload instanceof UnitPayload up) || up.unit == null) continue;
            Unit fighter = up.unit;
            live.add(fighter.id);

            float refit = Math.abs(deckRefitTimers.get(fighter.id, 0f));
            if (refit > eps) {
                float next = Math.max(0f, refit - Time.delta);
                float sign = deckRefitTimers.get(fighter.id, 0f) >= 0f ? 1f : -1f;
                if (next <= eps) {
                    deckRefitTimers.remove(fighter.id, 0f);
                } else {
                    deckRefitTimers.put(fighter.id, sign * next);
                }
            }

            if (!allowHeal || fighter.health >= fighter.maxHealth - eps || Math.abs(deckRefitTimers.get(fighter.id, 0f)) > eps) {
                deckHealTimers.remove(fighter.id, 0f);
                continue;
            }

            float timer = deckHealTimers.get(fighter.id, healInterval) - Time.delta;
            if (timer <= 0f) {
                fighter.heal(Math.max(fighter.maxHealth * healFraction, 1f));
                timer = healInterval;
            }
            deckHealTimers.put(fighter.id, timer);
        }

        if (runtimeIntervals.get(intervalDeckPrune, 25f)) {
            pruneTimerMap(deckRefitTimers, live);
            pruneTimerMap(deckHealTimers, live);
        }
    }

    protected void pruneTimerMap(IntFloatMap map, IntSet live) {
        staleIdsScratch.clear();
        for (IntFloatMap.Entry entry : map) {
            if (!live.contains(entry.key)) {
                staleIdsScratch.add(entry.key);
            }
        }
        for (int i = 0; i < staleIdsScratch.size; i++) {
            map.remove(staleIdsScratch.get(i), 0f);
        }
    }

    protected void updateRearm(CarrierUnitType ctype) {
        // 当某条跑道无在空战机且甲板未满时，按间隔补充新的甲板战机。
        for (int runway = 0; runway < runwayCount(); runway++) {
            RunwayLane lane = lane(runway);
            lane.rearmReload = Math.max(lane.rearmReload - Time.delta, 0f);
            if (lane.airborne.size > 0) continue;
            if (lane.deck.size >= runwayCapacity(runway)) continue;
            if (payloads.size >= deckSlotCount()) continue;
            if (lane.rearmReload > eps) continue;
            if (createDeckFighter(ctype, runway, true)) {
                lane.rearmReload = Math.max(ctype.rearmInterval, 1f);
            }
        }
    }

    protected boolean createDeckFighter(CarrierUnitType ctype, int runway, boolean construct) {
        int r = clampRunway(runway);
        UnitType fighterType = ctype.runwayFighterType(r);
        if (fighterType == null) return false;
        if (storedFighterCountInRunway(r) >= runwayCapacity(r) || payloads.size >= deckSlotCount()) return false;

        Unit fighter = fighterType.create(team);
        fighter.team.data().updateCount(fighter.type, 1);
        bindFighter(fighter, r);

        float refit = construct ? Math.max(ctype.recoverRefitTime, 0f) : 0f;
        if (refit > eps) {
            deckRefitTimers.put(fighter.id, refit);
        } else {
            deckRefitTimers.remove(fighter.id, 0f);
        }
        deckHealTimers.remove(fighter.id, 0f);

        UnitPayload payload = new UnitPayload(fighter);
        lane(r).deck.addLast(payload);
        seedPayloadVisual(payload, r, construct);
        syncPayloadsFromLanes();
        return true;
    }

    protected void seedPayloadVisual(UnitPayload payload, int runway, boolean construct) {
        if (payload == null) return;
        Vec2 start = new Vec2();
        if (construct) {
            recoveryPoint(runway, start);
        }else{
            int slot = runwayFirstSlot(runway) + Math.max(lane(runway).deck.size - 1, 0);
            deckSlotWorld(slot, start);
        }
        if (invalidLaunchPoint(start)) {
            runwayQueueInsertPoint(runway, start);
        }
        if (invalidLaunchPoint(start)) {
            start.set(x, y);
        }
        payloadVisuals.put(payload, start);
    }

    protected void updateVisualAnchor() {
        if (!visualAnchorValid) {
            visualAnchorValid = true;
            visualAnchorX = x;
            visualAnchorY = y;
            visualAnchorRot = rotation;
            return;
        }

        float dx = x - visualAnchorX;
        float dy = y - visualAnchorY;
        float drot = angleDelta(visualAnchorRot, rotation);
        boolean moved = Math.abs(dx) > eps || Math.abs(dy) > eps;
        boolean rotated = Math.abs(drot) > 0.001f;

        if (moved || rotated) {
            for (ObjectMap.Entry<Payload, Vec2> entry : payloadVisuals) {
                transformVisualPoint(entry.value, visualAnchorX, visualAnchorY, dx, dy, drot);
            }
            for (ObjectMap.Entry<UnitPayload, RunwayPayloadState> entry : payloadStates) {
                RunwayPayloadState state = entry.value;
                if (state != null) {
                    transformVisualPoint(state.current, visualAnchorX, visualAnchorY, dx, dy, drot);
                }
            }
        }

        visualAnchorX = x;
        visualAnchorY = y;
        visualAnchorRot = rotation;
    }

    protected void transformVisualPoint(Vec2 point, float anchorX, float anchorY, float dx, float dy, float drot) {
        if (point == null) return;
        point.sub(anchorX, anchorY).rotate(drot).add(anchorX + dx, anchorY + dy);
    }

    protected float angleDelta(float from, float to) {
        float delta = to - from;
        while (delta <= -180f) delta += 360f;
        while (delta > 180f) delta -= 360f;
        return delta;
    }

    protected void updateLaunch(CarrierUnitType ctype) {
        // 起飞调度：等待整条跑道准备就绪，再按发射间隔依次放飞队首战机。
        for(int runway = 0; runway < runwayCount(); runway++){
            RunwayLane lane = lane(runway);
            lane.launchReload = Math.max(lane.launchReload - Time.delta, 0f);

            if (!lane.launching) {
                lane.launching = lane.deck.size >= runwayCapacity(runway) && runwayStoredFightersReady(runway);
            }
            if (!lane.launching) continue;
            if (lane.deck.isEmpty()) {
                lane.launching = false;
                lane.launchReload = 0f;
                continue;
            }
            if (runwayLaunchBlocked(runway) || lane.launchReload > eps) continue;

            UnitPayload payload = frontUnitPayload(runway);
            if(payload == null){
                lane.launching = false;
                continue;
            }
            if (!launchStateReady(payload, runway, ctype)) continue;

            int slot = deckSlotForPayloadInternal(payload);
            if (launchFighter(payload, runway, slot, ctype)) {
                removeLaunchedPayload(runway, payload);
                lane.launching = !lane.deck.isEmpty();
                lane.launchReload = lane.launching ? Math.max(ctype.launchInterval, 1f) : 0f;
            }
        }
    }

    protected boolean runwayStoredFightersReady(int runway) {
        CarrierUnitType ctype = carrierType();
        if (ctype == null) return false;
        if (lane(runway).deck.size < runwayCapacity(runway)) return false;

        for (UnitPayload payload : lane(runway).deck) {
            if (payload == null || payload.unit == null) return false;
            if (Math.abs(deckRefitTimers.get(payload.unit.id, 0f)) > eps) return false;
            if (ctype.launchRequireFullHealth && payload.unit.health < payload.unit.maxHealth - eps) return false;
        }
        return true;
    }

    protected boolean runwayLaunchBlocked(int runway) {
        RunwayLane lane = lane(runway);
        if (lane.recoveryClaim >= 0) return true;
        for (int i = 0; i < lane.airborne.size; i++) {
            Unit fighter = Groups.unit.getByID(lane.airborne.get(i));
            if (fighter != null && fighter.controller() instanceof CarrierBoundAIC ai && (ai.isReturning() || ai.isLanding())) {
                return true;
            }
        }
        return false;
    }

    protected @Nullable UnitPayload frontUnitPayload(int runway) {
        Queue<UnitPayload> deck = lane(runway).deck;
        while (!deck.isEmpty()) {
            UnitPayload payload = deck.first();
            if (payload == null || payload.unit == null) {
                deck.removeFirst();
                continue;
            }
            return payload;
        }
        return null;
    }

    protected boolean launchStateReady(UnitPayload payload, int runway, CarrierUnitType ctype){
        if(payload == null || ctype == null) return false;
        Queue<UnitPayload> deck = lane(runway).deck;
        if (deck.isEmpty() || deck.first() != payload) return false;

        RunwayPayloadState state = payloadStates.get(payload);
        if (state == null) return true;

        runwayFrontPoint(runway, Tmp.v4);
        float threshold = Math.max(3f, ctype.runwaySlotSpacing(runway) * 0.22f);
        return state.current.dst2(Tmp.v4) <= threshold * threshold * 5.76f;
    }

    protected boolean launchFighter(UnitPayload payload, int runway, int launchSlot, CarrierUnitType ctype){
        Unit fighter = payload.unit;
        if(fighter == null || fighter.type == null) return false;

        runwayForwardVector(runway, Tmp.v3);
        resolveLaunchStartPoint(runway, launchSlot, Tmp.v1);
        launchExitPoint(runway, Tmp.v2);
        if (invalidLaunchPoint(Tmp.v2) || Tmp.v2.dst2(Tmp.v1) < 1f) {
            Tmp.v2.set(Tmp.v1).mulAdd(Tmp.v3, Math.max(fighter.hitSize * 2.8f, 36f));
        }

        float launchAngle = Angles.angle(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
        float launchVelocity = Math.max(fighter.type.speed * Math.max(ctype.takeoffSpeedMultiplier, 1f) * 0.6f, 1.2f);
        if (!fighter.type.flying && !fighter.canPass(World.toTile(Tmp.v1.x), World.toTile(Tmp.v1.y))) return false;

        fighter.set(Tmp.v1.x, Tmp.v1.y);
        if (fighter.trail != null) fighter.trail.clear();
        fighter.rotation(launchAngle);
        if(fighter.type.flying){
            fighter.elevation = Math.max(fighter.elevation, 0.32f);
        }
        fighter.id = EntityGroup.nextId();
        if(!fighter.isAdded()){
            fighter.team.data().updateCount(fighter.type, -1);
        }

        bindFighter(fighter, runway);
        fighter.add();
        fighter.unloaded();
        fighter.vel.set(Tmp.v3).scl(launchVelocity);

        if(fighter.controller() instanceof CarrierBoundAIC ai){
            ai.beginTakeoff(Tmp.v1, Tmp.v2, ctype.takeoffDuration, ctype.takeoffSpeedMultiplier);
        }
        if(ctype.takeoffEffect != null){
            ctype.takeoffEffect.at(Tmp.v1.x, Tmp.v1.y, launchAngle);
        }
        Fx.unitDrop.at(Tmp.v1.x, Tmp.v1.y, launchAngle);

        addAirborne(runway, fighter.id);
        sortieTimers.put(fighter.id, 0f);
        Events.fire(new PayloadDropEvent(this, fighter));
        return true;
    }

    protected void resolveLaunchStartPoint(int runway, int launchSlot, Vec2 out){
        runwayFrontPoint(runway, out);
        if(invalidLaunchPoint(out) && launchSlot >= 0 && launchSlot < deckSlotCount()){
            deckSlotWorld(launchSlot, out);
        }
        if(invalidLaunchPoint(out)){
            out.set(x, y);
        }
    }

    protected void removeLaunchedPayload(int runway, UnitPayload payload) {
        removeUnitPayload(lane(runway).deck, payload);
        payloads.remove(payload, true);
        payloadVisuals.remove(payload);
        payloadStates.remove(payload);
        if (payload.unit != null) {
            deckRefitTimers.remove(payload.unit.id, 0f);
            deckHealTimers.remove(payload.unit.id, 0f);
        }
        syncPayloadsFromLanes();
    }

    protected void recoverFighterToDeck(Unit fighter, int runway) {
        // 将已接地战机从世界移回甲板，并重置其在舰整备相关状态。
        if(fighter.isAdded()){
            fighter.team.data().updateCount(fighter.type, 1);
        }
        fighter.remove();
        bindFighter(fighter, runway);
        releaseRecoveryClaim(fighter);
        sortieTimers.remove(fighter.id, 0f);
        deckHealTimers.remove(fighter.id, 0f);

        float refit = Math.max(carrierType() == null ? 0f : carrierType().recoverRefitTime, 0f);
        if (refit > eps) {
            deckRefitTimers.put(fighter.id, -refit);
        }else{
            deckRefitTimers.remove(fighter.id, 0f);
        }

        for (RunwayLane lane : lanes) {
            for (int i = lane.airborne.size - 1; i >= 0; i--) {
                if (lane.airborne.get(i) == fighter.id) {
                    lane.airborne.removeIndex(i);
                }
            }
        }

        UnitPayload payload = new UnitPayload(fighter);
        lane(runway).deck.addLast(payload);
        seedPayloadVisual(payload, runway, true);
        syncPayloadsFromLanes();

        Fx.unitPickup.at(fighter);
        if(Vars.netClient != null){
            Vars.netClient.clearRemovedEntity(fighter.id);
        }
        Events.fire(new PickupEvent(this, fighter));
    }

    protected RunwayPayloadState payloadState(UnitPayload payload, int runway, int slot) {
        RunwayPayloadState state = payloadStates.get(payload);
        if (state == null) {
            state = new RunwayPayloadState();
            state.runway = runway;
            state.slot = slot;
            deckSlotWorld(slot, state.target);
            Vec2 visual = payloadVisuals.get(payload);
            if (visual != null) {
                state.current.set(visual);
            } else if (payload.unit != null && deckRefitShowsConstruct(payload.unit.id)) {
                recoveryPoint(runway, state.current);
            } else {
                state.current.set(state.target);
            }
            if (invalidLaunchPoint(state.current)) state.current.set(state.target);
            payloadStates.put(payload, state);
        }
        return state;
    }

    protected void updatePayloadVisuals(CarrierUnitType ctype) {
        updateVisualAnchor();
        float smooth = Mathf.clamp(ctype.deckVisualSmoothing, 0.02f, 0.95f);
        float queueSpeed = Mathf.clamp(ctype.queueMoveSpeed, 0.02f, 3f);
        float follow = Mathf.clamp(smooth * queueSpeed, 0.0025f, 0.18f);

        // 仅更新甲板上 payload 的视觉位置，让排队/回收过程看起来平滑连续。
        for (int runway = 0; runway < runwayCount(); runway++) {
            int local = 0;
            for (UnitPayload payload : lane(runway).deck) {
                if (payload == null) continue;
                int slot = runwayFirstSlot(runway) + local;
                RunwayPayloadState state = payloadState(payload, runway, slot);
                state.runway = runway;
                state.slot = slot;
                deckSlotWorld(slot, state.target);

                boolean constructing = payload.unit != null && deckRefitShowsConstruct(payload.unit.id);
                if (constructing) {
                    recoveryPoint(runway, Tmp.v4);
                    if (!invalidLaunchPoint(Tmp.v4)) {
                        float remaining = deckRefitRemaining(payload.unit.id);
                        float total = Math.max(ctype.recoverRefitTime, 1f);
                        float progress = 1f - Mathf.clamp(remaining / total, 0f, 1f);
                        progress = Interp.sineOut.apply(progress);
                        state.current.set(Tmp.v4).lerp(state.target, progress);
                    } else if (invalidLaunchPoint(state.current)) {
                        state.snap();
                    } else {
                        state.update(follow);
                    }
                } else if (invalidLaunchPoint(state.current)) {
                    state.snap();
                } else {
                    state.update(follow);
                }

                Vec2 visual = payloadVisuals.get(payload);
                if (visual == null) {
                    payloadVisuals.put(payload, new Vec2(state.current));
                } else {
                    visual.set(state.current);
                }
                local++;
            }
        }

        for (Payload payload : overflow) {
            if (payload instanceof UnitPayload up) {
                payloadStates.remove(up);
            }
            Vec2 visual = payloadVisuals.get(payload);
            if (visual == null) {
                payloadVisuals.put(payload, new Vec2(x, y));
            } else {
                visual.x = Mathf.lerpDelta(visual.x, x, follow);
                visual.y = Mathf.lerpDelta(visual.y, y, follow);
            }
        }
    }

    protected void cleanupPayloadVisuals() {
        livePayloadsScratch.clear();
        for (Payload payload : payloads) {
            if (payload != null) livePayloadsScratch.add(payload);
        }

        stalePayloadsScratch.clear();
        for (ObjectMap.Entry<Payload, Vec2> entry : payloadVisuals) {
            if (!livePayloadsScratch.contains(entry.key)) {
                stalePayloadsScratch.add(entry.key);
            }
        }
        for (Payload payload : stalePayloadsScratch) {
            payloadVisuals.remove(payload);
        }

        staleStateScratch.clear();
        for (ObjectMap.Entry<UnitPayload, RunwayPayloadState> entry : payloadStates) {
            if (entry.key == null || !livePayloadsScratch.contains(entry.key)) {
                staleStateScratch.add(entry.key);
            }
        }
        for (UnitPayload payload : staleStateScratch) {
            payloadStates.remove(payload);
        }
    }

    @Override
    public void remove(){
        for (RunwayLane lane : lanes) {
            for (int i = 0; i < lane.airborne.size; i++) {
                Unit fighter = Groups.unit.getByID(lane.airborne.get(i));
                if (fighter != null && ownsFighter(fighter)) {
                    clearFighterBinding(fighter);
                    fighter.kill();
                }
            }
        }

        for(Payload payload : payloads){
            if (payload instanceof UnitPayload up && up.unit != null && ownsFighter(up.unit)) {
                clearFighterBinding(up.unit);
                up.unit.kill();
            }
        }

        sortieTimers.clear();
        deckRefitTimers.clear();
        deckHealTimers.clear();
        resetTransientState();
        super.remove();
    }

    protected void writeIntFloatMap(Writes write, IntFloatMap map) {
        write.i(map.size);
        for (IntFloatMap.Entry entry : map) {
            write.i(entry.key);
            write.f(entry.value);
        }
    }

    protected void readIntFloatMap(Reads read, IntFloatMap map) {
        map.clear();
        int size = read.i();
        for (int i = 0; i < size; i++) {
            int key = read.i();
            float value = read.f();
            if (Math.abs(value) > eps) {
                map.put(key, value);
            }
        }
    }

    protected void writeState(Writes write) {
        write.bool(deckInitialized);
        writeIntFloatMap(write, sortieTimers);
        writeIntFloatMap(write, deckRefitTimers);
        write.i(runwayCount());
        for (int runway = 0; runway < runwayCount(); runway++) {
            RunwayLane lane = lane(runway);
            write.f(lane.launchReload);
            write.f(lane.rearmReload);
            write.bool(lane.launching);
            write.i(lane.recoveryClaim);
            write.i(lane.airborne.size);
            for (int i = 0; i < lane.airborne.size; i++) {
                write.i(lane.airborne.get(i));
            }
        }
    }

    protected void readState(Reads read) {
        readState(read, true);
    }

    protected void readState(Reads read, boolean full) {
        deckInitialized = read.bool();
        readIntFloatMap(read, sortieTimers);
        readIntFloatMap(read, deckRefitTimers);
        deckHealTimers.clear();
        if (full) {
            resetTransientState();
        }
        ensureLanes();
        for (RunwayLane lane : lanes) {
            lane.airborne.clear();
            lane.launchReload = 0f;
            lane.rearmReload = 0f;
            lane.launching = false;
            lane.recoveryClaim = -1;
        }

        int runways = read.i();
        for (int runway = 0; runway < runways; runway++) {
            float launchReload = read.f();
            float rearmReload = read.f();
            boolean launching = read.bool();
            int claim = read.i();
            int airborne = read.i();

            RunwayLane lane = runway < lanes.length ? lanes[runway] : null;
            if (lane != null) {
                lane.launchReload = Math.max(launchReload, 0f);
                lane.rearmReload = Math.max(rearmReload, 0f);
                lane.launching = launching;
                lane.recoveryClaim = claim;
                lane.airborne.clear();
            }

            for (int i = 0; i < airborne; i++) {
                int fighterId = read.i();
                if (lane != null) {
                    lane.airborne.add(fighterId);
                }
            }
        }
        if (full) {
            deckDirty = true;
        }
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        readState(read);
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        writeState(write);
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);
        readState(read, false);
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);
        writeState(write);
    }
}
