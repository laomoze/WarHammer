package wh.entities.world.entities;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.entities.Effect;
import mindustry.gen.Payloadc;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;
import wh.gen.CarrierPayloadUnit;
import wh.gen.CarrierUnit.CarrierHostc;

import java.util.Arrays;

public class CarrierUnitType extends WHUnitType{
    private static final float refitEps = 0.001f;
    private static final float directionEps2 = 0.0001f;

    public static class Runway{
        // 跑道锚点相对航母中心点的本地偏移。
        public float x, y;
        // 该跑道最多可容纳的机位数。
        public int capacity;
        // 该跑道的专属舰载机类型；为空时回退到 CarrierUnitType.fighterType。
        public UnitType fighterType;
        // 相对航母朝向的角度偏移（度）。NaN 表示直接使用航母前向。
        public float angle = Float.NaN;
        // 跑道机位间距覆盖值；<=0 表示沿用 CarrierUnitType.slotSpacing()。
        public float spacing = -1f;
        // 跑道起飞前推覆盖值；NaN 表示沿用 CarrierUnitType.launchForwardOffset。
        public float launchForwardOffset = Float.NaN;
        // 跑道回收后移覆盖值；NaN 表示沿用 CarrierUnitType.recoverRearOffset。
        public float recoverRearOffset = Float.NaN;
        // 是否在载荷层绘制该跑道上的 payload；false 时仅参与逻辑，不渲染模型。
        public boolean drawPayloadLayer = true;
        public float payloadLayerOffset = Float.NaN;

        public Runway(float x, float y, int capacity){
            this.x = x;
            this.y = y;
            this.capacity = Math.max(capacity, 1);
        }

        public Runway(float x, float y, int capacity, boolean drawPayloadLayer){
            this(x, y, capacity);
            this.drawPayloadLayer = drawPayloadLayer;
        }

        public Runway(float x, float y, int capacity, boolean drawPayloadLayer, float payloadLayerOffset) {
            this(x, y, capacity, drawPayloadLayer);
            this.payloadLayerOffset = payloadLayerOffset;
        }

        public Runway(float x, float y, int capacity, UnitType fighterType) {
            this(x, y, capacity);
            this.fighterType = fighterType;
        }

        public Runway(float x, float y, int capacity, float angle){
            this(x, y, capacity);
            this.angle = angle;
        }

        public Runway(float x, float y, int capacity, float angle, UnitType fighterType) {
            this(x, y, capacity, angle);
            this.fighterType = fighterType;
        }

        public Runway(float x, float y, int capacity, UnitType fighterType, boolean drawPayloadLayer) {
            this(x, y, capacity, drawPayloadLayer);
            this.fighterType = fighterType;
        }

        public Runway(float x, float y, int capacity, float angle, boolean drawPayloadLayer){
            this(x, y, capacity, angle);
            this.drawPayloadLayer = drawPayloadLayer;
        }

        public Runway(float x, float y, int capacity, float angle, boolean drawPayloadLayer, float payloadLayerOffset) {
            this(x, y, capacity, angle, drawPayloadLayer);
            this.payloadLayerOffset = payloadLayerOffset;
        }

        public Runway(float x, float y, int capacity, float angle, UnitType fighterType, boolean drawPayloadLayer) {
            this(x, y, capacity, angle, drawPayloadLayer);
            this.fighterType = fighterType;
        }

        public Runway(float x, float y, int capacity, float angle, UnitType fighterType, boolean drawPayloadLayer, float payloadLayerOffset) {
            this(x, y, capacity, angle, fighterType, drawPayloadLayer);
            this.payloadLayerOffset = payloadLayerOffset;
        }

        public Runway(float x, float y, int capacity, UnitType fighterType, float launchForwardOffset, float recoverRearOffset, boolean drawPayloadLayer) {
            this(x, y, capacity, drawPayloadLayer);
            this.fighterType = fighterType;
            this.launchForwardOffset = launchForwardOffset;
            this.recoverRearOffset = recoverRearOffset;
        }
    }

    public UnitType fighterType = UnitTypes.flare;
    public final Seq<Runway> runways = new Seq<>();

    public int initialFighterCount = 8;
    public int maxDeployedFighters = 8;

    // 跑道控制参数。
    // <= 0 时使用舰载机 hitSize 作为机位间距基准。
    public float runwaySpacing = -1f;
    public float runwaySpacingMultiplier = 1f;

    public float launchForwardOffset = 42f;
    public float recoverRearOffset = -42f;
    public float recoverRadius = 24f;

    public float launchInterval = 45f;
    // 跑道发射前是否要求该跑道甲板单位全部满血。
    public boolean launchRequireFullHealth = true;
    public float recoverCheckInterval = 10f;
    public float sortieDuration = 20f * 60f;
    public float regroupDelayOnLoss = 5f * 60f;
    public float rearmInterval = 2f * 60f;
    public boolean regroupOnAnyLoss = true;
    public float takeoffDuration = 45f;
    public float takeoffSpeedMultiplier = 1.25f;
    public Effect takeoffEffect = Fx.unitSpawn;
    public float landingDuration = 50f;
    public float landingApproachRadius = 22f;
    // 为 true 时，每条跑道同一时刻仅允许 1 架战机进入回收进近。
    public boolean oneByOneRecovery = true;
    // 回收返航移动速度系数（相对战机基础速度）。
    public float recoveryMoveSpeedFactor = 1.05f;
    // 等待回收许可时的盘旋半径倍率。
    public float recoveryHoldRadius = 1.35f;
    // 对齐入队方向时的转向速度。
    public float recoveryTurnRate = 2f;
    public float recoverRefitTime = 90f;
    // 回收后战机停放在甲板上的治疗周期。
    public float recoverHealInterval = 45f;
    // 每个 recoverHealInterval 恢复的最大生命值比例。
    public float recoverHealFraction = 0.12f;
    public float idleReturnDelay = 4f * 60f;
    public float recallHealthf = 0.15f;
    public float maxFighterDistance = 360f;

    public float fighterOrbitRadius = 90f;
    public float fighterOrbitSmoothing = 70f;
    public float fighterAttackDistanceFactor = 0.8f;
    public float deckVisualSmoothing = 0.6f;
    // 队列视觉插值速度倍率；1 为基础速度，越小越慢。
    public float queueMoveSpeed = 0.1f;
    // 载荷绘制层偏移（相对 unit 当前 Draw.z()）；负值会压到单位主体下方。
    public float payloadLayerOffset = 0.1f;

    public CarrierUnitType(String name){
        super(name);
        constructor = CarrierPayloadUnit::new;
    }

    public CarrierUnitType runway(float x, float y, int capacity){
        runways.add(new Runway(x, y, capacity));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, float angle){
        runways.add(new Runway(x, y, capacity, angle));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, UnitType fighterType) {
        runways.add(new Runway(x, y, capacity, fighterType));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, float angle, UnitType fighterType) {
        runways.add(new Runway(x, y, capacity, angle, fighterType));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, boolean drawPayloadLayer){
        runways.add(new Runway(x, y, capacity, drawPayloadLayer));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, boolean drawPayloadLayer, float payloadLayerOffset) {
        runways.add(new Runway(x, y, capacity, drawPayloadLayer, payloadLayerOffset));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, UnitType fighterType, float launchForwardOffset, float recoverRearOffset, boolean drawPayloadLayer) {
        runways.add(new Runway(x, y, capacity, fighterType, launchForwardOffset, recoverRearOffset, drawPayloadLayer));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, UnitType fighterType, boolean drawPayloadLayer) {
        runways.add(new Runway(x, y, capacity, fighterType, drawPayloadLayer));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, float angle, boolean drawPayloadLayer){
        runways.add(new Runway(x, y, capacity, angle, drawPayloadLayer));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, float angle, boolean drawPayloadLayer, float payloadLayerOffset) {
        runways.add(new Runway(x, y, capacity, angle, drawPayloadLayer, payloadLayerOffset));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, float angle, UnitType fighterType, boolean drawPayloadLayer) {
        runways.add(new Runway(x, y, capacity, angle, fighterType, drawPayloadLayer));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, float angle, UnitType fighterType, boolean drawPayloadLayer, float payloadLayerOffset) {
        runways.add(new Runway(x, y, capacity, angle, fighterType, drawPayloadLayer, payloadLayerOffset));
        return this;
    }

    public CarrierUnitType runwayFighter(int runway, UnitType fighterType) {
        if (!runways.any()) return this;
        int idx = Mathf.clamp(runway, 0, runways.size - 1);
        Runway def = runways.get(idx);
        if (def != null) {
            def.fighterType = fighterType;
        }
        return this;
    }

    public CarrierUnitType setRunwaySpacing(int runway, float spacing) {
        Runway def = runwayDef(runway);
        if (def != null) {
            def.spacing = spacing;
        }
        return this;
    }

    public CarrierUnitType setRunwayLaunchOffset(int runway, float offset) {
        Runway def = runwayDef(runway);
        if (def != null) {
            def.launchForwardOffset = offset;
        }
        return this;
    }

    public CarrierUnitType setRunwayRecoverOffset(int runway, float offset) {
        Runway def = runwayDef(runway);
        if (def != null) {
            def.recoverRearOffset = offset;
        }
        return this;
    }

    public CarrierUnitType setRunwayPayloadLayerOffset(int runway, float offset) {
        Runway def = runwayDef(runway);
        if (def != null) {
            def.payloadLayerOffset = offset;
        }
        return this;
    }

    public CarrierUnitType clearRunways(){
        runways.clear();
        return this;
    }

    protected Runway runwayDef(int runway) {
        if (!runways.any()) return null;
        int idx = Mathf.clamp(runway, 0, runways.size - 1);
        return runways.get(idx);
    }

    public UnitType runwayFighterType(int runway) {
        Runway def = runwayDef(runway);
        if (def != null && def.fighterType != null) {
            return def.fighterType;
        }
        return fighterType;
    }

    public float runwaySlotSpacing(int runway) {
        Runway def = runwayDef(runway);
        if (def != null && def.spacing > 0f) {
            return def.spacing;
        }
        return slotSpacing();
    }

    public float runwayLaunchOffset(int runway) {
        Runway def = runwayDef(runway);
        if (def != null && Float.isFinite(def.launchForwardOffset)) {
            return def.launchForwardOffset;
        }
        return launchForwardOffset;
    }

    public float runwayRecoverOffset(int runway) {
        Runway def = runwayDef(runway);
        if (def != null && Float.isFinite(def.recoverRearOffset)) {
            return def.recoverRearOffset;
        }
        return recoverRearOffset;
    }

    public float runwayPayloadLayerOffset(int runway) {
        Runway def = runwayDef(runway);
        if (def != null && Float.isFinite(def.payloadLayerOffset)) {
            return def.payloadLayerOffset;
        }
        return payloadLayerOffset;
    }

    public boolean hasAnyFighterType() {
        if (fighterType != null) return true;
        for (Runway runway : runways) {
            if (runway != null && runway.fighterType != null) {
                return true;
            }
        }
        return false;
    }

    public float slotSpacing(){
        UnitType spacingType = fighterType;
        if (spacingType == null && runways.any()) {
            float maxHit = -1f;
            for (int i = 0; i < runways.size; i++) {
                UnitType type = runwayFighterType(i);
                if (type != null && type.hitSize > maxHit) {
                    maxHit = type.hitSize;
                    spacingType = type;
                }
            }
        }

        float base = runwaySpacing > 0f ? runwaySpacing : (spacingType == null ? 10f : spacingType.hitSize * 1.5f);
        return Math.max(2f, base * Mathf.clamp(runwaySpacingMultiplier, 0.2f, 5f));
    }

    public int deckCapacity(){
        if(runways.any()){
            int count = 0;
            for(Runway runway : runways){
                count += Math.max(runway.capacity, 1);
            }
            return Math.max(count, 1);
        }
        return 0;
    }

    protected int runwayCountForDeck(){
        return runways.any() ? Math.max(runways.size, 1) : 1;
    }

    protected int runwayForDeckSlot(int slot){
        if(!runways.any() || slot < 0) return 0;

        int cursor = 0;
        for(int i = 0; i < runways.size; i++){
            cursor += Math.max(runways.get(i).capacity, 1);
            if(slot < cursor){
                return i;
            }
        }
        return runways.size - 1;
    }

    protected boolean runwayDrawPayloadLayer(int runway){
        if(!runways.any()) return true;
        int idx = Mathf.clamp(runway, 0, runways.size - 1);
        Runway def = runways.get(idx);
        return def == null || def.drawPayloadLayer;
    }

    protected boolean invalidCarrierPoint(Unit unit, Vec2 point){
        if(point == null || !Float.isFinite(point.x) || !Float.isFinite(point.y)) return true;
        return point.within(0f, 0f, 8f) && !unit.within(0f, 0f, 80f);
    }

    protected boolean shouldDrawConstructPreview(CarrierHostc carrier, UnitPayload payload) {
        if (payload == null || payload.unit == null) return false;
        if (!carrier.deckRefitShowsConstruct(payload.unit.id)) return false;
        return carrier.deckRefitRemaining(payload.unit.id) > refitEps;
    }

    protected float resolvePayloadRotation(Unit unit, Vec2 from, Vec2 to) {
        return resolvePayloadRotation(unit, from, to, unit.rotation());
    }

    protected float resolvePayloadRotation(Unit unit, Vec2 from, Vec2 to, float fallbackRotation) {
        if (!invalidCarrierPoint(unit, from) && !invalidCarrierPoint(unit, to) && from.dst2(to) > directionEps2) {
            return Angles.angle(from.x, from.y, to.x, to.y);
        }
        return fallbackRotation;
    }

    protected float runwayPayloadRotation(Unit unit, CarrierHostc carrier, int runway) {
        carrier.recoveryPoint(runway, Tmp.v4);
        carrier.runwayFrontPoint(runway, Tmp.v5);
        return resolvePayloadRotation(unit, Tmp.v4, Tmp.v5, unit.rotation() - 90f);
    }

    protected void drawRunwayConstructPreview(Unit unit, CarrierHostc carrier, Payload payload, UnitPayload up, int slot, int runway, float remain) {
        float total = Math.max(Math.max(recoverRefitTime, refitEps), remain);
        float progress = Mathf.clamp(1f - remain / total, 0f, 1f);

        carrier.deckSlotWorldVisual(payload, slot, Tmp.v2);
        if (invalidCarrierPoint(unit, Tmp.v2)) {
            carrier.recoveryPoint(runway, Tmp.v2);
        }
        if (invalidCarrierPoint(unit, Tmp.v2)) {
            Tmp.v2.set(unit.x, unit.y);
        }
        float rot = runwayPayloadRotation(unit, carrier, runway) - 90f;
        TextureRegion icon = up.unit.type.fullIcon;
        if (icon != null) {
            Draw.alpha(progress);
            Draw.rect(icon, Tmp.v2.x, Tmp.v2.y, rot);
            Draw.alpha(1f);
        }
    }

    protected void drawRunwayPayload(Unit unit, CarrierHostc carrier, Payload payload, int slot, int runway) {
        carrier.deckSlotWorldVisual(payload, slot, Tmp.v1);
        if (invalidCarrierPoint(unit, Tmp.v1)) {
            carrier.recoveryPoint(runway, Tmp.v1);
        }
        if (invalidCarrierPoint(unit, Tmp.v1)) {
            Tmp.v1.set(unit.x, unit.y);
        }

        float payloadRotation = runwayPayloadRotation(unit, carrier, runway);
        payload.set(Tmp.v1.x, Tmp.v1.y, payloadRotation);
        payload.draw();
    }

    @Override
    public <T extends Unit&Payloadc> void drawPayload(T unit){
        if(!(unit instanceof CarrierHostc carrier)) return;
        if(!unit.hasPayload()) return;

        float prev = Draw.z();

        Seq<Payload> payloads = unit.payloads();
        int payloadCount = payloads.size;
        int runwayCount = runwayCountForDeck();
        Payload[] constructPayloadCache = new Payload[Math.max(runwayCount, 1)];
        int[] constructSlotCache = new int[Math.max(runwayCount, 1)];
        int[] payloadSlotCache = new int[Math.max(payloadCount, 1)];
        int[] payloadRunwayCache = new int[Math.max(payloadCount, 1)];
        Arrays.fill(constructSlotCache, -1);
        Arrays.fill(payloadSlotCache, -1);
        Arrays.fill(payloadRunwayCache, 0);

        // 一次性缓存 payload 的 slot/runway，同时选出每条跑道唯一构建投影目标。
        for(int i = 0; i < payloadCount; i++){
            Payload payload = payloads.get(i);
            if(payload == null) continue;

            int slot = carrier.deckSlotForPayload(payload);
            int runway = slot >= 0 ? runwayForDeckSlot(slot) : 0;
            runway = carrier.clampRunway(runway);
            payloadSlotCache[i] = slot;
            payloadRunwayCache[i] = runway;

            if(!(payload instanceof UnitPayload up) || up.unit == null) continue;
            if (!shouldDrawConstructPreview(carrier, up)) continue;

            if(slot >= 0 && slot >= constructSlotCache[runway]){
                constructSlotCache[runway] = slot;
                constructPayloadCache[runway] = payload;
            }else if(slot < 0 && constructPayloadCache[runway] == null){
                constructPayloadCache[runway] = payload;
            }
        }

        for(int i = 0; i < payloadCount; i++){
            Payload payload = payloads.get(i);
            if(payload == null) continue;

            int slot = payloadSlotCache[i];
            int runway = payloadRunwayCache[i];
            if (!runwayDrawPayloadLayer(runway)) continue;

            Draw.z(prev + runwayPayloadLayerOffset(runway));

            if(payload instanceof UnitPayload up && up.unit != null){
                if (shouldDrawConstructPreview(carrier, up)) {
                    if(constructPayloadCache[runway] == payload){
                        float remain = carrier.deckRefitRemaining(up.unit.id);
                        drawRunwayConstructPreview(unit, carrier, payload, up, slot, runway, remain);
                    }
                    continue;
                }
            }

            drawRunwayPayload(unit, carrier, payload, slot, runway);
        }

        Draw.z(prev);
    }
}
