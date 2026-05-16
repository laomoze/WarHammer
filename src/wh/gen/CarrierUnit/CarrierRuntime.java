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
        public int localSlot = -1;//单位占用的槽位编号�?
        public final Vec2 current = new Vec2();
        public final Vec2 target = new Vec2();

        /**
         * [001] ：将当前可视位置直接对齐到目标位置，立即结束过渡�?
         */
        public void snap(){
            current.set(target);
        }

        /**
         * [002] ：按给定插值系数平滑推�?payload 的当前可视位置�?
         */
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

        /**
         * [003] ：创建并初始化指定跑道的运行时状态容器�?
         */
        public RunwayLane(int runway){
            this.runway = runway;
        }
    }

    // 甲板是否已完成初始装填�?
    public boolean deckInitialized = false;
    // 已放飞战机的出击计时�?
    public IntFloatMap sortieTimers = new IntFloatMap();
    // 甲板战机的整�?回收冷却计时�?
    public IntFloatMap deckRefitTimers = new IntFloatMap();
    // 甲板战机的维修回血计时�?
    public transient IntFloatMap deckHealTimers = new IntFloatMap();
    // 各跑道的甲板、在空与回收占位状态�?
    public transient RunwayLane[] lanes = new RunwayLane[0];
    // 无法归入跑道队列的额�?payload�?
    public transient Seq<Payload> overflow = new Seq<>();
    // 甲板 payload 的队�?槽位过渡状态�?
    public transient ObjectMap<UnitPayload, RunwayPayloadState> payloadStates = new ObjectMap<>();
    // 甲板 payload 的视觉位置缓存�?
    public transient ObjectMap<Payload, Vec2> payloadVisuals = new ObjectMap<>();
    public transient Interval runtimeIntervals = new Interval(4);
    // 甲板队列�?payloads 是否需要重同步�?
    public transient boolean deckDirty = true;
    // 视觉锚点是否已建立�?
    public transient boolean visualAnchorValid = false;
    // 上一帧视觉锚点位�?朝向�?
    public transient float visualAnchorX = 0f;
    public transient float visualAnchorY = 0f;
    public transient float visualAnchorRot = 0f;

    private final transient Vec2 runwayScratch = new Vec2();
    private final transient IntSeq staleIdsScratch = new IntSeq();
    private final transient IntSet uniqueIdsScratch = new IntSet();
    private final transient ObjectSet<Payload> livePayloadsScratch = new ObjectSet<>();
    private final transient Seq<Payload> stalePayloadsScratch = new Seq<>();
    private final transient Seq<UnitPayload> staleStateScratch = new Seq<>();

    protected boolean authoritative() {
        return !Vars.net.client();
    }

    // ===== 生命周期与入�?=====

    /** [004] ：返回实体注�?ID，用于网络同步与序列化识别�?*/
    @Override
    public int classId(){
        return EntityRegister.getId(getClass());
    }

    /** [005] ：返回单位质量参数，供物理与碰撞系统使用�?*/
    @Override
    public float mass() {
        return 114514;
    }

    /** [006] ：切换单位类型后重置甲板状态、计时器与瞬态缓存�?*/
    @Override
    public void setType(UnitType type) {
        super.setType(type);
        deckInitialized = false;
        sortieTimers.clear();
        deckRefitTimers.clear();
        deckHealTimers.clear();
        resetTransientState();
        if (!Vars.net.client()) {
            trimPayloadsToDeckCapacity();
        } else {
            deckDirty = true;
        }
    }

    /** [007] ：每帧驱动航母核心流程：同步、维护、起降与可视化�?*/
    @Override
    public void update(){
        super.update();

        CarrierUnitType ctype = carrierType();
        if(ctype == null) return;
        boolean authoritative = !Vars.net.client();

        ensureLanes();
        if (authoritative) {
            trimPayloadsToDeckCapacity();
            if (!deckInitialized) {
                initializeDeck(ctype);
                deckInitialized = true;
            }
        }
        ensureDeckQueuesSynced();

        if (authoritative) {
            updateAirborneState();
            updateDeckMaintenance(ctype);
            updateRearm(ctype);
            if (runtimeIntervals.get(intervalClaimCleanup, 12f)) {
                cleanupStaleRecoveryClaims();
            }
            if (runtimeIntervals.get(intervalAirborneRescan, 35f)) {
                rescanAirborneFighters();
            }
            updateRunwayLaunchState(ctype);
        }

        updatePayloadVisuals(ctype);
        if (runtimeIntervals.get(intervalVisualCleanup, 30f)) {
            cleanupPayloadVisuals();
        }
    }

    // ===== 运行时状态与甲板队列 =====

    /** [008] ：清空所有瞬态缓存并恢复运行时标记到初始状态�?*/
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

    /** [009] ：确保跑道状态数组与当前跑道数量一致�?*/
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

    /**
     * [010] ：获取并返回指定跑道状态，含索引钳制�?
     */
    protected RunwayLane laneForRunway(int runway) {
        ensureLanes();
        return lanes[clampRunway(runway)];
    }

    /**
     * [011] ：在甲板为空时按跑道容量初始化舰载机�?
     */
    protected void initializeDeck(CarrierUnitType ctype) {
        ensureDeckQueuesSynced();
        if (!payloads.isEmpty() || !ctype.hasAnyFighterType()) return;

        for (int runway = 0; runway < runwayCount(); runway++) {
            for (int i = 0; i < runwayCapacity(runway); i++) {
                createDeckFighter(ctype, runway, false);
            }
        }
    }

    /**
     * [012] ：确�?lane.deck、overflow �?payloads 三者数据一致�?
     */
    protected void ensureDeckQueuesSynced() {
        ensureLanes();
        // 保证 lane.deck / overflow / payloads 三者始终对应，必要时重建甲板队列�?
        if (deckDirty || !deckQueuesMatchPayloadStorage()) {
            rebuildRunwayDecksFromPayloadStorage();
        }
    }

    /**
     * [013] ：检查当前跑道队列结构是否与 payload 存储一致�?
     */
    protected boolean deckQueuesMatchPayloadStorage() {
        int total = overflow.size;
        for (RunwayLane lane : lanes) {
            total += lane.deck.size;
        }
        if (total != payloads.size) return false;

        for (Payload payload : payloads) {
            if (overflow.contains(payload, true)) continue;
            if (payload instanceof UnitPayload up && isPayloadInAnyRunwayDeck(up)) continue;
            return false;
        }
        return true;
    }

    /**
     * [014] ：判断目�?payload 是否存在于任意跑道队列中�?
     */
    protected boolean isPayloadInAnyRunwayDeck(UnitPayload target) {
        for (RunwayLane lane : lanes) {
            for (UnitPayload payload : lane.deck) {
                if (payload == target) return true;
            }
        }
        return false;
    }

    /**
     * [015] ：根�?payload 存储重建各跑道队列与 overflow 分区�?
     */
    protected void rebuildRunwayDecksFromPayloadStorage() {
        ensureLanes();
        overflow.clear();
        for (RunwayLane lane : lanes) {
            lane.deck.clear();
        }

        int[] fill = new int[runwayCount()];
        for(Payload payload : payloads) {
            if (payload instanceof UnitPayload up && up.unit != null) {
                int runway = chooseRunwayForPayloadFighter(up.unit, fill);
                if (runway >= 0 && fill[runway] < runwayCapacity(runway)) {
                    bindFighter(up.unit, runway);
                    laneForRunway(runway).deck.addLast(up);
                    fill[runway]++;
                    continue;
                }
            }
            overflow.add(payload);
        }
        deckDirty = false;
    }

    /**
     * [016] ：为 payload 内战机选择可用跑道与槽位�?
     */
    protected int chooseRunwayForPayloadFighter(Unit fighter, int[] fill) {
        int runway = ownsFighter(fighter) ? fighterRunway(fighter) : 0;
        if (runway >= 0 && runway < fill.length && fill[runway] < runwayCapacity(runway)) {
            return runway;
        }
        for (int i = 0; i < fill.length; i++) {
            if (fill[i] < runwayCapacity(i)) return i;
        }
        return -1;
    }

    /**
     * [017] ：把跑道队列顺序回写�?payload 存储�?
     */
    protected void syncPayloadStorageFromRunwayDecks() {
        if (!authoritative()) return;
        // 按各跑道队列重新拼出 payloads，保持实际存储顺序与跑道队列一致�?
        payloads.clear();
        for (RunwayLane lane : lanes) {
            for (UnitPayload payload : lane.deck) {
                payloads.add(payload);
            }
        }
        payloads.addAll(overflow);
        deckDirty = false;
        trimPayloadsToDeckCapacity();
    }

    /** [018] ：裁剪超出甲板容量的 payload 并清理关联状态�?*/
    protected void trimPayloadsToDeckCapacity() {
        if (!authoritative()) return;
        while (payloads.size > deckSlotCount() && !payloads.isEmpty()) {
            Payload removed = payloads.pop();
            removePayloadFromDeckCollections(removed);
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

    /**
     * [019] ：从 overflow 与所有跑道队列中移除指定 payload�?
     */
    protected void removePayloadFromDeckCollections(Payload payload) {
        overflow.remove(payload, true);
        if (payload instanceof UnitPayload up) {
            for (RunwayLane lane : lanes) {
                removeUnitPayloadFromQueue(lane.deck, up);
            }
        }
    }

    /**
     * [020] ：从单个跑道队列中删除目�?UnitPayload�?
     */
    protected void removeUnitPayloadFromQueue(Queue<UnitPayload> queue, UnitPayload target) {
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

    // ===== 战机绑定与归�?=====

    /** [021] ：将战机绑定到当前航母和跑道，并切换为舰�?AI�?*/
    protected void bindFighter(Unit fighter, int runway) {
        if (fighter == null) return;

        int r = clampRunway(runway);
        fighter.team = team;
        // 把战机重新绑定到当前航母与指定跑道，并确保它使用舰载机专�?AI�?
        if (fighter instanceof CarrierFighterUnit data) {
            data.setCarrierBinding(id, r);
        }

        if (fighter.controller() instanceof CarrierBoundAIC ai) {
            ai.setCarrier(id).setRunway(r);
        } else {
            fighter.controller(new CarrierFighterAI(id, r));
        }
    }

    /** [022] ：清除战机的航母绑定标记�?AI 绑定参数�?*/
    protected void clearFighterBinding(Unit fighter) {
        if (fighter instanceof CarrierFighterUnit data) {
            data.clearCarrierBinding();
        }
        if (fighter.controller() instanceof CarrierFighterAI ai) {
            ai.setCarrier(-1).setRunway(0);
        }
    }

    /** [023] ：判断战机是否归属当前航母�?*/
    @Override
    public boolean ownsFighter(Unit fighter) {
        if (fighter == null) return false;
        if (fighter instanceof CarrierFighterUnit data) {
            return data.carrierId == id;
        }
        return fighter.controller() instanceof CarrierFighterAI ai && ai.carrierId() == id;
    }

    /** [024] ：获取战机当前绑定的跑道索引�?*/
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

    /** [025] ：读取战机当前出击累计时长�?*/
    @Override
    public float fighterSortieTime(Unit fighter) {
        return fighter == null ? 0f : sortieTimers.get(fighter.id, 0f);
    }

    // ===== 甲板槽位与跑道几�?=====

    /**
     * [026] ：统计指定跑道上甲板内已存放战机数量�?
     */
    public int storedFighterCountInRunway(int runway) {
        ensureDeckQueuesSynced();
        return laneForRunway(runway).deck.size;
    }

    /**
     * [027] ：统计指定跑道已分配战机总数（甲�?在空）�?
     */
    public int assignedFighterCountInRunway(int runway) {
        RunwayLane lane = laneForRunway(runway);
        return lane.deck.size + lane.airborne.size;
    }

    /**
     * [028] ：将跑道局部槽位转换为世界坐标�?
     */
    @Override
    public void deckSlotWorld(int runway, int localSlot, Vec2 out){
        if (out == null) return;

        CarrierUnitType ctype = carrierType();
        if (ctype == null) {
            out.set(x, y);
            return;
        }

        if (ctype.runways.any()) {
            int resolvedRunway = clampRunway(runway);
            int resolvedLocal = Mathf.clamp(localSlot, 0, Math.max(runwayCapacity(resolvedRunway) - 1, 0));
            runwayFrontPoint(resolvedRunway, out);
            float spacing = ctype.runwaySlotSpacing(resolvedRunway);
            Vec2 forward = runwayForwardVector(resolvedRunway, runwayScratch);
            out.sub(forward.x * resolvedLocal * spacing, forward.y * resolvedLocal * spacing);
            return;
        }

        out.set(x, y);
    }

    /**
     * [029] ：对外查�?payload 对应的跑道索引�?
     */
    @Override
    public int deckRunwayForPayload(Payload payload) {
        ensureDeckQueuesSynced();
        return deckRunwayForPayloadInternal(payload);
    }

    /**
     * [030] ：对外查�?payload 对应的跑道局部槽位�?
     */
    @Override
    public int deckLocalSlotForPayload(Payload payload) {
        ensureDeckQueuesSynced();
        return deckLocalSlotForPayloadInternal(payload);
    }

    protected int runwayForDeckIndex(int index) {
        if (index < 0) return -1;
        int remain = index;
        for (int runway = 0; runway < runwayCount(); runway++) {
            int cap = Math.max(runwayCapacity(runway), 1);
            if (remain < cap) {
                return runway;
            }
            remain -= cap;
        }
        return runwayCount() <= 0 ? -1 : runwayCount() - 1;
    }

    protected int localSlotForDeckIndex(int index) {
        if (index < 0) return -1;
        int remain = index;
        for (int runway = 0; runway < runwayCount(); runway++) {
            int cap = Math.max(runwayCapacity(runway), 1);
            if (remain < cap) {
                return remain;
            }
            remain -= cap;
        }
        return runwayCount() <= 0 ? -1 : Math.max(runwayCapacity(runwayCount() - 1) - 1, 0);
    }

    /**
     * [031] ：内部解�?payload 跑道索引，支持状态缓存回退�?
     */
    protected int deckRunwayForPayloadInternal(Payload payload) {
        if (payload == null || deckSlotCount() <= 0) return -1;
        if (overflow.contains(payload, true)) return -1;

        if (payload instanceof UnitPayload up) {
            RunwayPayloadState state = payloadStates.get(up);
            if (state != null && state.runway >= 0 && state.localSlot >= 0) {
                return clampRunway(state.runway);
            }
        }

        for (int runway = 0; runway < runwayCount(); runway++) {
            for (UnitPayload payloadInLane : laneForRunway(runway).deck) {
                if (payloadInLane == payload) {
                    return runway;
                }
            }
        }

        int linear = payloads.indexOf(payload, true);
        return runwayForDeckIndex(linear);
    }

    /** [032] ：内部解�?payload 跑道局部槽位，支持状态缓存回退�?*/
    protected int deckLocalSlotForPayloadInternal(Payload payload){
        if(payload == null || deckSlotCount() <= 0) return -1;
        if (overflow.contains(payload, true)) return -1;

        if (payload instanceof UnitPayload up) {
            RunwayPayloadState state = payloadStates.get(up);
            if (state != null && state.runway >= 0 && state.localSlot >= 0) {
                int runway = clampRunway(state.runway);
                return Mathf.clamp(state.localSlot, 0, Math.max(runwayCapacity(runway) - 1, 0));
            }
        }

        for (int runway = 0; runway < runwayCount(); runway++) {
            int localSlot = 0;
            for (UnitPayload payloadInLane : laneForRunway(runway).deck) {
                if (payloadInLane == payload) {
                    return localSlot;
                }
                localSlot++;
            }
        }

        int linear = payloads.indexOf(payload, true);
        return localSlotForDeckIndex(linear);
    }

    /**
     * [033] ：获�?payload 渲染坐标，优先使用视觉缓存�?
     */
    @Override
    public void deckSlotWorldVisual(Payload payload, int runway, int localSlot, Vec2 out) {
        if (out == null) return;

        if (runway < 0 || localSlot < 0) {
            out.set(x, y);
            return;
        }

        Vec2 visual = payloadVisuals.get(payload);
        if (visual != null) {
            out.set(visual);
        } else {
            deckSlotWorld(runway, localSlot, out);
        }
    }

    /** [032] ：返回战机整备剩余时间（绝对值）�?*/
    @Override
    public float deckRefitRemaining(int fighterId) {
        return fighterId < 0 ? 0f : Math.abs(deckRefitTimers.get(fighterId, 0f));
    }

    /** [033] ：判断整备计时是否处于“建造显示”阶段�?*/
    @Override
    public boolean deckRefitShowsConstruct(int fighterId) {
        return fighterId >= 0 && deckRefitTimers.get(fighterId, 0f) > eps;
    }

    /** [034] ：计算跑道前端世界坐标�?*/
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

        deckSlotWorld(runway, 0, out);
    }

    /** [035] ：计算跑道后端世界坐标�?*/
    public void runwayBackPoint(int runway, Vec2 out) {
        if (out == null) return;
        runwayFrontPoint(runway, out);
        Vec2 forward = runwayForwardVector(runway, runwayScratch);
        out.sub(forward.x * runwayDeckDepth(runway), forward.y * runwayDeckDepth(runway));
    }

    /** [036] ：计算跑道甲板纵向深度�?*/
    public float runwayDeckDepth(int runway){
        CarrierUnitType ctype = carrierType();
        if (ctype == null) return 0f;
        return Math.max(runwayCapacity(runway) - 1, 0) * ctype.runwaySlotSpacing(runway);
    }

    /** [037] ：计算并归一化跑道朝向向量�?*/
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

        if(out.len2() < 0.001f) {
            out.set(0f, 1f);
        }
        return out.nor();
    }

    /** [038] ：计算战机起飞离舰目标点�?*/
    @Override
    public void launchExitPoint(int runway, Vec2 out) {
        if (out == null) return;
        runwayFrontPoint(runway, out);
        Vec2 forward = runwayForwardVector(runway, runwayScratch);
        float offset = Math.max(carrierType() == null ? 0f : carrierType().runwayLaunchOffset(runway), 0f);
        out.add(forward.x * offset, forward.y * offset);
    }

    /** [039] ：计算战机回收触地点（入舰点）�?*/
    @Override
    public void recoveryPoint(int runway, Vec2 out) {
        if (out == null) return;

        CarrierUnitType ctype = carrierType();
        if (ctype == null) {
            out.set(this);
            return;
        }

        deckSlotWorld(runway, Math.max(runwayCapacity(runway) - 1, 0), out);
        Vec2 forward = runwayForwardVector(runway, runwayScratch);
        out.sub(forward.x * Math.abs(ctype.runwayRecoverOffset(runway)), forward.y * Math.abs(ctype.runwayRecoverOffset(runway)));
        if(invalidLaunchPoint(out)) {
            runwayBackPoint(runway, out);
        }
        if (invalidLaunchPoint(out)){
            out.set(x, y);
        }
    }

    /** [040] ：按默认距离计算回收反向等待点�?*/
    public void recoveryReversePoint(int runway, Vec2 out) {
        recoveryReversePoint(runway, 0f, out);
    }

    /** [041] ：按指定距离计算回收反向等待点�?*/
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
        if (Tmp.v3.len2() < 0.0001f){
            runwayForwardVector(runway, Tmp.v3).scl(-1f);
        } else {
            Tmp.v3.nor();
        }
        return out.set(Tmp.v1.x + Tmp.v3.x * len, Tmp.v1.y + Tmp.v3.y * len);
    }

    /** [042] ：计算回收排队插入点坐标�?*/
    @Override
    public void runwayQueueInsertPoint(int runway, Vec2 out) {
        CarrierUnitType ctype = carrierType();
        float distance = ctype == null ? 48f : Math.max(
                ctype.runwaySlotSpacing(runway) * 1.5f,
                Math.max(ctype.landingApproachRadius * 1.75f, ctype.recoverRadius * 2.1f)
        );
        recoveryReversePoint(runway, distance, out);
    }

    // ===== 目标选择 =====

    /** [043] ：计算指挥目标有效性检测范围�?*/
    protected float commandTargetCheckRange(@Nullable CarrierUnitType ctype) {
        return ctype == null ? Math.max(type.range, targetRangeBase) : Math.max(type.range, ctype.maxFighterDistance * targetRangeCarrierScale);
    }

    /** [044] ：计算武器挂载目标检测范围�?*/
    protected float mountTargetCheckRange(@Nullable CarrierUnitType ctype) {
        return ctype == null ? (Math.max(type.range, mountRangeFloor) + mountRangePadding) : Math.max(type.range, ctype.maxFighterDistance * targetRangeCarrierScale);
    }

    /** [045] ：计算主动索敌扫描范围�?*/
    protected float bestTargetScanRange(@Nullable CarrierUnitType ctype) {
        return ctype == null ? Math.max(type.range, targetRangeBase) : Math.max(type.range, ctype.maxFighterDistance);
    }

    /** [046] ：读取并验证指挥 AI 的攻击目标�?*/
    protected @Nullable Teamc commandAttackTarget(float checkRange) {
        if (!(controller() instanceof CommandAI ai)) return null;
        Teamc target = ai.attackTarget;
        return Units.invalidateTarget(target, this, checkRange) ? null : target;
    }

    /** [047] ：读取并验证武器挂载当前目标�?*/
    protected @Nullable Teamc mountAttackTarget(float checkRange) {
        if (mounts == null) return null;
        for (WeaponMount mount : mounts) {
            Teamc target = mount.target;
            if (!Units.invalidateTarget(target, this, checkRange)) return target;
        }
        return null;
    }

    /** [048] ：在给定范围内选择最优敌方目标�?*/
    protected @Nullable Teamc bestTargetInRange(float range) {
        return Units.bestTarget(team, x, y, range, u -> u.checkTarget(true, true), b -> true, UnitSorts.weakest);
    }

    /** [049] ：在指挥落点附近寻找焦点敌人或敌方建筑�?*/
    protected @Nullable Teamc focusEnemyNearCommandPos(CommandAI ai) {
        if (ai.targetPos == null || !Float.isFinite(ai.targetPos.x) || !Float.isFinite(ai.targetPos.y)) return null;
        float minFocus = Math.max(hitSize * 0.8f, focusMinDistance);
        if (ai.targetPos.within(x, y, minFocus)) return null;

        Teamc pointEnemy = Units.bestEnemy(team, ai.targetPos.x, ai.targetPos.y, focusEnemyProbeRange, u -> u.checkTarget(true, true), UnitSorts.weakest);
        if (pointEnemy != null) return pointEnemy;

        Building tile = Vars.world == null ? null : Vars.world.buildWorld(ai.targetPos.x, ai.targetPos.y);
        return tile != null && tile.team != team && tile.isValid() ? tile : null;
    }

    /** [050] ：给舰载机返回当前优先锁定目标�?*/
    @Override
    public @Nullable Teamc lockedTarget(){
        CarrierUnitType ctype = carrierType();
        Teamc attack = commandAttackTarget(commandTargetCheckRange(ctype));
        if (attack != null) return attack;

        Teamc mount = mountAttackTarget(mountTargetCheckRange(ctype));
        if (mount != null) return mount;

        return bestTargetInRange(bestTargetScanRange(ctype));
    }

    /** [051] ：给舰载机输出当前应聚焦的目标位置�?*/
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

    /** [052] ：判断战机是否满足返航回收条件�?*/
    @Override
    public boolean shouldRecallFighter(Unit fighter){
        CarrierUnitType ctype = carrierType();
        if(ctype == null || fighter == null) return true;
        if (ctype.sortieDuration > 0f && fighterSortieTime(fighter) >= ctype.sortieDuration) return true;
        if (fighter.healthf() <= Mathf.clamp(ctype.recallHealthf, 0f, 1f)) return true;
        return false;
    }

    // ===== 回收流程 =====

    /** [053] ：判断指定跑道当前是否具备回收条件�?*/
    protected boolean canRunwayRecoverToDeck(int runway) {
        return payloads.size < deckSlotCount() && storedFighterCountInRunway(runway) < runwayCapacity(runway);
    }

    /** [054] ：释放战机占用的跑道回收进场资格�?*/
    @Override
    public void releaseRecoveryClaim(Unit fighter) {
        if (!authoritative()) return;
        if (fighter == null) return;
        for (RunwayLane lane : lanes) {
            if (lane.recoveryClaim == fighter.id) {
                lane.recoveryClaim = -1;
            }
        }
    }

    /** [055] ：清理失效、越界或过期的回收占位声明�?*/
    protected void cleanupStaleRecoveryClaims() {
        if (!authoritative()) return;
        CarrierUnitType ctype = carrierType();
        if(ctype == null) return;
        // 未开启逐架回收时，不需要维护跑道“最终进场资格”，直接清空所�?claim�?
        if (!ctype.oneByOneRecovery) {
            for (RunwayLane lane : lanes) lane.recoveryClaim = -1;
            return;
        }

        for (RunwayLane lane : lanes) {
            int claim = lane.recoveryClaim;
            if(claim < 0) continue;

            Unit fighter = Groups.unit.getByID(claim);
            // 占位飞机不存在、已死亡、不再属于本航母、已经回收到甲板�?
            // 或其当前分配跑道已变化时，这�?claim 就应立即失效�?
            if (fighter == null || fighter.dead() || !ownsFighter(fighter) || isFighterStoredOnDeck(claim) || fighterRunway(fighter) != lane.runway) {
                lane.recoveryClaim = -1;
                continue;
            }
            // 只有仍由舰载�?AI 控制，且仍处于返�?降落流程中的飞机�?
            // 才允许继续保留这条跑道的最终进场资格�?
            if (!(fighter.controller() instanceof CarrierBoundAIC ai) || (!ai.isReturning() && !ai.isLanding())) {
                lane.recoveryClaim = -1;
                continue;
            }

            runwayQueueInsertPoint(lane.runway, Tmp.v1);
            float radius = Math.max(ctype.recoverRadius * 4f, ctype.landingApproachRadius * 3f);
            // 即便状态仍合法，只要飞机已经远离本跑道等待区，也释�?claim�?
            // 避免旧占位长期卡住后续回收�?
            if (!fighter.within(Tmp.v1, radius)) {
                lane.recoveryClaim = -1;
            }
        }
    }

    /** [056] ：判定战机是否允许进入最终回收进场阶段�?*/
    @Override
    public boolean allowRecoveryApproach(Unit fighter){
        if(fighter == null || fighter.dead()) return false;
        CarrierUnitType ctype = carrierType();
        if (ctype == null || !ownsFighter(fighter)) return false;

        int runway = fighterRunway(fighter);
        if (!canRunwayRecoverToDeck(runway)) return false;
        // 未开�?oneByOneRecovery 时，只要基础条件满足就允许直接进入最后进场�?
        if (!ctype.oneByOneRecovery) return true;
        if (!authoritative()) {
            int claim = laneForRunway(runway).recoveryClaim;
            return claim < 0 || claim == fighter.id;
        }

        // 开启逐架回收时，同一跑道同一时刻只允许一架飞机占用最终进场资格�?
        cleanupStaleRecoveryClaims();
        RunwayLane lane = laneForRunway(runway);
        // recoveryClaim < 0 表示当前无人占位�?
        // recoveryClaim == fighter.id 表示该资格本来就属于当前飞机，允许继续完成降落流程�?
        if (lane.recoveryClaim < 0 || lane.recoveryClaim == fighter.id) {
            lane.recoveryClaim = fighter.id;
            return true;
        }
        return false;
    }

    /** [057] ：计算回收触地判定半径�?*/
    protected float recoveryTouchdownRadius(Unit fighter, CarrierUnitType ctype, int runway) {
        if (fighter == null || ctype == null) return 0f;
        return Math.max(3.5f, Math.max(ctype.runwaySlotSpacing(runway) * 0.18f, fighter.hitSize * 0.4f));
    }

    /** [058] ：判断战机是否已进入回收触地点范围�?*/
    protected boolean isNearRecoveryTouchdown(Unit fighter, int runway, float radius) {
        if (fighter == null) return false;
        recoveryPoint(runway, Tmp.v4);
        return !invalidLaunchPoint(Tmp.v4) && fighter.within(Tmp.v4.x, Tmp.v4.y, radius);
    }

    /** [059] ：尝试把满足条件的战机回收到甲板�?*/
    @Override
    public boolean tryRecoverFighter(Unit fighter) {
        if (!authoritative()) return false;
        if (fighter == null || !fighter.isValid() || fighter.dead()) return false;
        CarrierUnitType ctype = carrierType();
        if (ctype == null || !ownsFighter(fighter)) return false;

        int runway = fighterRunway(fighter);
        if (!canRunwayRecoverToDeck(runway)) return false;
        if (!isNearRecoveryTouchdown(fighter, runway, recoveryTouchdownRadius(fighter, ctype, runway))) return false;
        // 战机 AI 会在真正接地时才允许回收；这里等于在航母侧再做一次最终确认，
        // 避免飞机还没完成最后进场就被提前收入甲板�?
        if (fighter.controller() instanceof CarrierBoundAIC ai && !ai.canRecoverNow()) return false;

        recoverFighterToDeck(fighter, runway);
        return true;
    }

    /**
     * [060] ：执行战机入舰回收并重置其在舰状态�?
     */
    protected void recoverFighterToDeck(Unit fighter, int runway) {
        if (!authoritative()) return;
        int oldId = fighter.id;
        // 将已接地战机从世界移回甲板，并重置其在舰整备相关状态�?
        if (fighter.isAdded()) {
            fighter.team.data().updateCount(fighter.type, 1);
        }
        fighter.remove();
        releaseRecoveryClaim(fighter);
        sortieTimers.remove(oldId, 0f);
        deckHealTimers.remove(oldId, 0f);
        deckRefitTimers.remove(oldId, 0f);

        float refit = Math.max(carrierType() == null ? 0f : carrierType().recoverRefitTime, 0f);

        for (RunwayLane lane : lanes) {
            if (lane.recoveryClaim == oldId) {
                lane.recoveryClaim = -1;
            }
            for (int i = lane.airborne.size - 1; i >= 0; i--) {
                if (lane.airborne.get(i) == oldId) {
                    lane.airborne.removeIndex(i);
                }
            }
        }

        // Keep the same ID while entering payload (world -> payload).
        // ID remapping is only needed when the fighter re-enters the world (payload -> world).
        bindFighter(fighter, runway);
        if (refit > eps) {
            deckRefitTimers.put(fighter.id, -refit);
        } else {
            deckRefitTimers.remove(fighter.id, 0f);
        }

        UnitPayload payload = new UnitPayload(fighter);
        laneForRunway(runway).deck.addLast(payload);
        seedPayloadVisualPosition(payload, runway, true);
        syncPayloadStorageFromRunwayDecks();

        Fx.unitPickup.at(fighter);
        Events.fire(new PickupEvent(this, fighter));
    }

    // ===== 在空追踪与甲板维�?=====

    /** [061] ：维护在空列表、纠正跑道归属并累计出击时间�?*/
    protected void updateAirborneState() {
        // 维护各跑�?airborne 列表：去重、剔除失效目标、修正跑道归属，并累�?sortie 时间�?
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
                if (fighter == null || fighter.dead() || isFighterStoredOnDeck(fighterId) || !ownsFighter(fighter)) {
                    lane.airborne.removeIndex(i);
                    sortieTimers.remove(fighterId, 0f);
                    continue;
                }

                int runway = fighterRunway(fighter);
                if (runway != lane.runway) {
                    lane.airborne.removeIndex(i);
                    addAirborneFighter(runway, fighterId);
                    continue;
                }

                bindFighter(fighter, runway);
                sortieTimers.put(fighterId, sortieTimers.get(fighterId, 0f) + Time.delta);
            }
        }
    }

    /**
     * [062] ：全局重扫在空战机，补全遗漏索引�?
     */
    protected void rescanAirborneFighters() {
        for (Unit fighter : Groups.unit) {
            if (fighter == this || !ownsFighter(fighter) || isFighterStoredOnDeck(fighter.id)) continue;
            addAirborneFighter(fighterRunway(fighter), fighter.id);
        }
    }

    /**
     * [063] ：向指定跑道在空列表去重添加战机 ID�?
     */
    protected void addAirborneFighter(int runway, int fighterId) {
        RunwayLane lane = laneForRunway(runway);
        for (int i = 0; i < lane.airborne.size; i++) {
            if (lane.airborne.get(i) == fighterId) return;
        }
        lane.airborne.add(fighterId);
    }

    /** [064] ：判断给定战机是否已存放在甲�?payload 中�?*/
    protected boolean isFighterStoredOnDeck(int fighterId) {
        if (fighterId < 0) return false;
        for (Payload payload : payloads) {
            if (payload instanceof UnitPayload up && up.unit != null && up.unit.id == fighterId) {
                return true;
            }
        }
        return false;
    }

    /** [065] ：推进甲板整�?维修计时并执行周期回血�?*/
    protected void updateDeckMaintenance(CarrierUnitType ctype){
        IntSet live = uniqueIdsScratch;
        live.clear();

        boolean allowHeal = ctype.recoverHealFraction > 0.0001f;
        float healInterval = allowHeal ? Math.max(ctype.recoverHealInterval, 1f) : 1f;
        float healFraction = allowHeal ? Mathf.clamp(ctype.recoverHealFraction, 0f, 1f) : 0f;

        // 处理甲板内战机的整备/维修计时，并定期清理失效计时器条目�?
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
            pruneTimerEntries(deckRefitTimers, live);
            pruneTimerEntries(deckHealTimers, live);
        }
    }

    /** [066] ：清理计时表中不再存活的条目�?*/
    protected void pruneTimerEntries(IntFloatMap map, IntSet live) {
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

    // ===== 补给与起飞调�?=====

    /** [067] ：按补给间隔为空缺跑道补充甲板战机�?*/
    protected void updateRearm(CarrierUnitType ctype) {
        // 当某条跑道无在空战机且甲板未满时，按间隔补充新的甲板战机�?
        for (int runway = 0; runway < runwayCount(); runway++) {
            RunwayLane lane = laneForRunway(runway);
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

    /** [068] ：创建一架甲板战机并加入对应跑道队列�?*/
    protected boolean createDeckFighter(CarrierUnitType ctype, int runway, boolean construct) {
        if (!authoritative()) return false;
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
        laneForRunway(r).deck.addLast(payload);
        seedPayloadVisualPosition(payload, r, construct);
        syncPayloadStorageFromRunwayDecks();
        return true;
    }

    /** [069] ：初始化�?payload 的视觉起始位置�?*/
    protected void seedPayloadVisualPosition(UnitPayload payload, int runway, boolean construct) {
        if (payload == null) return;
        Vec2 start = new Vec2();
        if (construct) {
            recoveryPoint(runway, start);
        } else {
            int localSlot = Math.max(laneForRunway(runway).deck.size - 1, 0);
            deckSlotWorld(runway, localSlot, start);
        }
        if (invalidLaunchPoint(start)) {
            runwayQueueInsertPoint(runway, start);
        }
        if (invalidLaunchPoint(start)) {
            start.set(x, y);
        }
        payloadVisuals.put(payload, start);
    }

    /** [070] ：调度各跑道起飞流程与发射节奏�?*/
    protected void updateRunwayLaunchState(CarrierUnitType ctype) {
        // 起飞调度：等待整条跑道准备就绪，再按发射间隔依次放飞队首战机�?
        for(int runway = 0; runway < runwayCount(); runway++){
            RunwayLane lane = laneForRunway(runway);
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

            UnitPayload payload = frontPayloadInRunway(runway);
            if (payload == null) {
                lane.launching = false;
                continue;
            }
            if (!launchStateReady(payload, runway, ctype)) continue;

            int localSlot = deckLocalSlotForPayloadInternal(payload);
            if (launchFighter(payload, runway, localSlot, ctype)) {
                removeLaunchedPayload(runway, payload);
                lane.launching = !lane.deck.isEmpty();
                lane.launchReload = lane.launching ? Math.max(ctype.launchInterval, 1f) : 0f;
            }
        }
    }

    /** [071] ：检查跑道内战机是否全部满足起飞前置条件�?*/
    protected boolean runwayStoredFightersReady(int runway) {
        CarrierUnitType ctype = carrierType();
        if (ctype == null) return false;
        if (laneForRunway(runway).deck.size < runwayCapacity(runway)) return false;

        for (UnitPayload payload : laneForRunway(runway).deck) {
            if (payload == null || payload.unit == null) return false;
            if (Math.abs(deckRefitTimers.get(payload.unit.id, 0f)) > eps) return false;
            if (ctype.launchRequireFullHealth && payload.unit.health < payload.unit.maxHealth - eps) return false;
        }
        return true;
    }

    /**
     * [072] ：判断跑道是否被回收流程阻塞起飞�?
     */
    protected boolean runwayLaunchBlocked(int runway) {
        RunwayLane lane = laneForRunway(runway);
        if (lane.recoveryClaim >= 0) return true;
        for (int i = 0; i < lane.airborne.size; i++) {
            Unit fighter = Groups.unit.getByID(lane.airborne.get(i));
            if (fighter != null && fighter.controller() instanceof CarrierBoundAIC ai && (ai.isReturning() || ai.isLanding())) {
                return true;
            }
        }
        return false;
    }

    /**
     * [073] ：获取跑道队首可用战�?payload�?
     */
    protected @Nullable UnitPayload frontPayloadInRunway(int runway) {
        Queue<UnitPayload> deck = laneForRunway(runway).deck;
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

    /** [074] ：判断队�?payload 是否达到可起飞状态�?*/
    protected boolean launchStateReady(UnitPayload payload, int runway, CarrierUnitType ctype){
        if(payload == null || ctype == null) return false;
        Queue<UnitPayload> deck = laneForRunway(runway).deck;
        if (deck.isEmpty() || deck.first() != payload) return false;

        RunwayPayloadState state = payloadStates.get(payload);
        if (state == null) return true;

        runwayFrontPoint(runway, Tmp.v4);
        float threshold = Math.max(3f, ctype.runwaySlotSpacing(runway) * 0.22f);
        return state.current.dst2(Tmp.v4) <= threshold * threshold * 5.76f;
    }

    /** [075] ：执行战机出舰起飞、速度设置与事件派发�?*/
    protected boolean launchFighter(UnitPayload payload, int runway, int launchLocalSlot, CarrierUnitType ctype) {
        Unit fighter = payload.unit;
        if (fighter == null || fighter.type == null) return false;

        runwayForwardVector(runway, Tmp.v3);
        resolveLaunchStartPoint(runway, launchLocalSlot, Tmp.v1);
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
        if(!fighter.isAdded()){
            fighter.team.data().updateCount(fighter.type, -1);
        }

        // Match vanilla payload drop semantics: remap to a fresh ID before re-entering world.
        // This prevents client removed-entity cache collisions when the same fighter is recovered
        // then relaunched repeatedly.
        int oldId = fighter.id;
        fighter.id = EntityGroup.nextId();
        if (oldId != fighter.id) {
            sortieTimers.remove(oldId, 0f);
            deckRefitTimers.remove(oldId, 0f);
            deckHealTimers.remove(oldId, 0f);
            for (RunwayLane lane : lanes) {
                if (lane == null) continue;
                if (lane.recoveryClaim == oldId) {
                    lane.recoveryClaim = -1;
                }
                for (int i = lane.airborne.size - 1; i >= 0; i--) {
                    if (lane.airborne.get(i) == oldId) {
                        lane.airborne.removeIndex(i);
                    }
                }
            }
        }

        bindFighter(fighter, runway);
        fighter.add();
        fighter.unloaded();
        fighter.vel.set(Tmp.v3).scl(launchVelocity);

        if(fighter.controller() instanceof CarrierBoundAIC ai) {
            ai.beginTakeoff(Tmp.v1, Tmp.v2, ctype.takeoffDuration, ctype.takeoffSpeedMultiplier);
        }
        if (ctype.takeoffEffect != null) {
            ctype.takeoffEffect.at(Tmp.v1.x, Tmp.v1.y, launchAngle);
        }
        Fx.unitDrop.at(Tmp.v1.x, Tmp.v1.y, launchAngle);

        addAirborneFighter(runway, fighter.id);
        sortieTimers.put(fighter.id, 0f);
        Events.fire(new PayloadDropEvent(this, fighter));
        return true;
    }

    /**
     * [076] ：解析起飞起点坐标并处理无效点回退�?
     */
    protected void resolveLaunchStartPoint(int runway, int launchLocalSlot, Vec2 out) {
        runwayFrontPoint(runway, out);
        if (invalidLaunchPoint(out) && launchLocalSlot >= 0) {
            deckSlotWorld(runway, launchLocalSlot, out);
        }
        if(invalidLaunchPoint(out)) {
            out.set(x, y);
        }
    }

    /**
     * [077] ：移除已起飞 payload 并清理其关联计时�?
     */
    protected void removeLaunchedPayload(int runway, UnitPayload payload) {
        if (!authoritative()) return;
        removeUnitPayloadFromQueue(laneForRunway(runway).deck, payload);
        payloads.remove(payload, true);
        payloadVisuals.remove(payload);
        payloadStates.remove(payload);
        if (payload.unit != null) {
            deckRefitTimers.remove(payload.unit.id, 0f);
            deckHealTimers.remove(payload.unit.id, 0f);
        }
        syncPayloadStorageFromRunwayDecks();
    }

    // ===== 视觉状态与插�?=====

    /**
     * [078] ：同步视觉锚点并对历史视觉点做位姿补偿�?
     */
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
        float drot = Angles.angleDist(visualAnchorRot, rotation);
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

    /**
     * [079] ：对单个视觉点应用平移与旋转变换�?
     */
    protected void transformVisualPoint(Vec2 point, float anchorX, float anchorY, float dx, float dy, float drot) {
        if (point == null) return;
        point.sub(anchorX, anchorY).rotate(drot).add(anchorX + dx, anchorY + dy);
    }

    /**
     * [080] ：计算两个角度之间的最短差值�?
     */
    protected float angleDelta(float from, float to) {
        float delta = to - from;
        while (delta <= -180f) delta += 360f;
        while (delta > 180f) delta -= 360f;
        return delta;
    }

    /** [081] ：获取或创建 payload 的视觉插值状态对象�?*/
    protected RunwayPayloadState getOrCreatePayloadState(UnitPayload payload, int runway, int localSlot) {
        RunwayPayloadState state = payloadStates.get(payload);
        if (state == null) {
            state = new RunwayPayloadState();
            state.runway = runway;
            state.localSlot = localSlot;
            deckSlotWorld(runway, localSlot, state.target);
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

    /** [082] ：逐帧更新甲板与溢�?payload 的视觉位置�?*/
    protected void updatePayloadVisuals(CarrierUnitType ctype) {
        updateVisualAnchor();
        float smooth = Mathf.clamp(ctype.deckVisualSmoothing, 0.02f, 0.95f);
        float queueSpeed = Mathf.clamp(ctype.queueMoveSpeed, 0.02f, 3f);
        float follow = Mathf.clamp(smooth * queueSpeed, 0.0025f, 0.18f);

        // 仅更新甲板上 payload 的视觉位置，让排�?回收过程看起来平滑连续�?
        for (int runway = 0; runway < runwayCount(); runway++) {
            int localSlot = 0;
            for (UnitPayload payload : laneForRunway(runway).deck) {
                if (payload == null) continue;
                RunwayPayloadState state = getOrCreatePayloadState(payload, runway, localSlot);
                state.runway = runway;
                state.localSlot = localSlot;
                deckSlotWorld(runway, localSlot, state.target);

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
                localSlot++;
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

    /** [083] ：清理已失效 payload 的视觉缓存与状态�?*/
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

    /** [084] ：移除航母时清理并销毁所有归属战机�?*/
    @Override
    public void remove() {
        // Only the server/authority should mutate ownership and kill fighters.
        if (!Vars.net.client()) {
            for (RunwayLane lane : lanes) {
                for (int i = 0; i < lane.airborne.size; i++) {
                    Unit fighter = Groups.unit.getByID(lane.airborne.get(i));
                    if (fighter != null && ownsFighter(fighter)) {
                        clearFighterBinding(fighter);
                        fighter.kill();
                    }
                }
            }

            for (Payload payload : payloads) {
                if (payload instanceof UnitPayload up && up.unit != null && ownsFighter(up.unit)) {
                    clearFighterBinding(up.unit);
                    up.unit.kill();
                }
            }
        }

        sortieTimers.clear();
        deckRefitTimers.clear();
        deckHealTimers.clear();
        resetTransientState();
        super.remove();
    }

    // ===== 状态序列化 =====

    /** [085] ：把 IntFloatMap 序列化写入数据流�?*/
    protected void writeIntFloatMap(Writes write, IntFloatMap map) {
        write.i(map.size);
        for (IntFloatMap.Entry entry : map) {
            write.i(entry.key);
            write.f(entry.value);
        }
    }

    /** [086] ：从数据流读取并反序列化 IntFloatMap�?*/
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

    /** [087] ：写出航母完整运行时状态�?*/
    protected void writeState(Writes write) {
        write.bool(deckInitialized);
        writeIntFloatMap(write, sortieTimers);
        writeIntFloatMap(write, deckRefitTimers);
        write.i(runwayCount());
        for (int runway = 0; runway < runwayCount(); runway++) {
            RunwayLane lane = laneForRunway(runway);
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

    /** [088] ：按完整模式读取运行时状态（默认入口）�?*/
    protected void readState(Reads read) {
        readState(read, true);
    }

    /** [089] ：按 full 标志读取并应用运行时状态�?*/
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
        deckDirty = true;
        if (!full) {
            overflow.clear();
            payloadStates.clear();
            payloadVisuals.clear();
            visualAnchorValid = false;
        }
        clearRemovedFlagsForStoredFighters();
    }

    protected void clearRemovedFlagsForStoredFighters() {
        if (!Vars.net.client() || Vars.netClient == null) return;
        for (Payload payload : payloads) {
            if (payload instanceof UnitPayload up && up.unit != null) {
                Vars.netClient.clearRemovedEntity(up.unit.id);
            }
        }
    }

    protected void clearRemovedFlagsForKnownFighters() {
        if (!Vars.net.client() || Vars.netClient == null) return;

        for (Payload payload : payloads) {
            if (payload instanceof UnitPayload up && up.unit != null) {
                Vars.netClient.clearRemovedEntity(up.unit.id);
            }
        }
        for (RunwayLane lane : lanes) {
            if (lane == null) continue;
            for (int i = 0; i < lane.airborne.size; i++) {
                Vars.netClient.clearRemovedEntity(lane.airborne.get(i));
            }
        }
        for (IntFloatMap.Entry entry : sortieTimers) {
            Vars.netClient.clearRemovedEntity(entry.key);
        }
        for (IntFloatMap.Entry entry : deckRefitTimers) {
            Vars.netClient.clearRemovedEntity(entry.key);
        }
    }

    /** [090] ：读取实体全量状态数据�?*/
    @Override
    public void read(Reads read) {
        super.read(read);
        readState(read);
    }

    /**
     * [091] ：写出实体全量状态数据�?
     */
    @Override
    public void write(Writes write) {
        super.write(write);
        writeState(write);
    }

    /** [092] ：读取联机同步帧状态�?*/
    @Override
    public void readSync(Reads read) {
        clearRemovedFlagsForKnownFighters();
        super.readSync(read);
        readState(read, false);
    }

    /** [093] ：写出联机同步帧状态�?*/
    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);
        writeState(write);
    }
}
