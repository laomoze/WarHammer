package wh.entities.world.entities;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.payloads.*;
import wh.gen.*;
import wh.gen.CarrierUnit.*;

import java.util.*;

public class CarrierUnitType extends WHUnitType{
    public static class Runway{
        public float x, y;
        public int capacity;
        // Local angle offset in degrees relative to carrier forward.
        // NaN means use carrier forward direction.
        public float angle = Float.NaN;
        // Whether this runway participates in payload layer drawing.
        public boolean drawPayloadLayer = true;

        public Runway(float x, float y, int capacity){
            this.x = x;
            this.y = y;
            this.capacity = Math.max(capacity, 1);
        }

        public Runway(float x, float y, int capacity, boolean drawPayloadLayer){
            this(x, y, capacity);
            this.drawPayloadLayer = drawPayloadLayer;
        }

        public Runway(float x, float y, int capacity, float angle){
            this(x, y, capacity);
            this.angle = angle;
        }

        public Runway(float x, float y, int capacity, float angle, boolean drawPayloadLayer){
            this(x, y, capacity, angle);
            this.drawPayloadLayer = drawPayloadLayer;
        }
    }

    public UnitType fighterType = UnitTypes.flare;
    public final Seq<Runway> runways = new Seq<>();

    // Legacy deck grid settings; used when no custom runways are configured.
    public int deckLanes = 2;
    public int deckRows = 5;
    public int trimRearSlots = 2;

    public int initialFighterCount = 8;
    public int maxDeployedFighters = 8;

    // Legacy deck grid positioning.
    public float deckLaneSpacing = 18f;
    public float deckRowSpacing = 16f;
    public float deckFrontOffset = 24f;

    // Runway controls.
    // <= 0 means use fighter hitSize for spacing.
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
    // If true, only one fighter per runway is allowed to approach for recovery at a time.
    public boolean oneByOneRecovery = true;
    // Return movement speed factor relative to fighter speed.
    public float recoveryMoveSpeedFactor = 1.05f;
    // Holding ring radius multiplier used while waiting for recovery permission.
    public float recoveryHoldRadius = 1.35f;
    // Rotation speed while aligning for queue entry.
    public float recoveryTurnRate = 12f;
    public float recoverRefitTime = 90f;
    // Heal cadence while fighter is stored on deck after recovery.
    public float recoverHealInterval = 45f;
    // Fraction of max health restored per recoverHealInterval tick.
    public float recoverHealFraction = 0.12f;
    public float idleReturnDelay = 6f * 60f;
    public float recallHealthf = 0.35f;
    public float maxFighterDistance = 360f;

    public float fighterOrbitRadius = 90f;
    public float fighterOrbitSmoothing = 70f;
    public float fighterAttackDistanceFactor = 0.8f;
    public float deckVisualSmoothing = 0.6f;
    // Queue visual movement multiplier; 1 = base speed, lower is slower.
    public float queueMoveSpeed = 0.1f;

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

    public CarrierUnitType runway(float x, float y, int capacity, boolean drawPayloadLayer){
        runways.add(new Runway(x, y, capacity, drawPayloadLayer));
        return this;
    }

    public CarrierUnitType runway(float x, float y, int capacity, float angle, boolean drawPayloadLayer){
        runways.add(new Runway(x, y, capacity, angle, drawPayloadLayer));
        return this;
    }

    public CarrierUnitType clearRunways(){
        runways.clear();
        return this;
    }

    public float slotSpacing(){
        float base = runwaySpacing > 0f ? runwaySpacing : (fighterType == null ? 10f : fighterType.hitSize * 1.4f);
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

        int lanes = Math.max(deckLanes, 1);
        int rows = Math.max(deckRows, 1);
        int trimmed = Math.max(trimRearSlots, 0);
        return Math.max(lanes * rows - trimmed, 1);
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

    @Override
    public <T extends Unit&Payloadc> void drawPayload(T unit){
        if(!(unit instanceof CarrierHostc carrier)) return;

        if(!unit.hasPayload()) return;

        float prev = Draw.z();
        Draw.z(prev - 0.02f);

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

        // Precompute slot/runway once per payload; select one construct target per runway.
        for(int i = 0; i < payloadCount; i++){
            Payload payload = payloads.get(i);
            if(payload == null) continue;

            int slot = carrier.deckSlotForPayload(payload);
            int runway = slot >= 0 ? runwayForDeckSlot(slot) : 0;
            runway = carrier.clampRunway(runway);
            payloadSlotCache[i] = slot;
            payloadRunwayCache[i] = runway;

            if(!(payload instanceof UnitPayload up) || up.unit == null) continue;
            if(!carrier.deckRefitShowsConstruct(up.unit.id)) continue;

            float remain = carrier.deckRefitRemaining(up.unit.id);
            if(remain <= 0.001f) continue;
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
            if(!runwayDrawPayloadLayer(runway)){
                continue;
            }

            if(payload instanceof UnitPayload up && up.unit != null){
                float remain = carrier.deckRefitRemaining(up.unit.id);
                if(carrier.deckRefitShowsConstruct(up.unit.id) && remain > 0.001f){
                    if(constructPayloadCache[runway] == payload){
                        float total = Math.max(Math.max(recoverRefitTime, 0.001f), remain);
                        float progress = Mathf.clamp(1f - remain / total, 0f, 1f);
                        carrier.recoveryPoint(runway, Tmp.v2);
                        carrier.runwayFrontPoint(runway, Tmp.v3);

                        if(invalidCarrierPoint(unit, Tmp.v2)){
                            carrier.deckSlotWorldVisual(payload, slot, Tmp.v2);
                        }
                        if(invalidCarrierPoint(unit, Tmp.v3)){
                            carrier.recoveryPoint(runway, Tmp.v3);
                        }
                        if(invalidCarrierPoint(unit, Tmp.v3)){
                            Tmp.v3.set(unit.x, unit.y);
                        }

                        float fx = Tmp.v2.x;
                        float fy = Tmp.v2.y;
                        float runwayRotation = unit.rotation();
                        if(Tmp.v2.dst2(Tmp.v3) > 0.0001f){
                            runwayRotation = Angles.angle(Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y);
                        }

                        Draw.alpha(progress);
                        float frot = runwayRotation - 90f;
                        TextureRegion ficon = up.unit.type.fullIcon;
                        if(ficon != null) Draw.rect(ficon, fx, fy, frot);
                        Draw.alpha(1f);
                    }
                    continue;
                }
            }

            carrier.deckSlotWorldVisual(payload, slot, Tmp.v1);
            float payloadRotation = unit.rotation();
            carrier.recoveryPoint(runway, Tmp.v2);
            carrier.runwayFrontPoint(runway, Tmp.v3);
            if(!invalidCarrierPoint(unit, Tmp.v2) && !invalidCarrierPoint(unit, Tmp.v3) && Tmp.v2.dst2(Tmp.v3) > 0.0001f){
                payloadRotation = Angles.angle(Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y);
            }
            payload.set(Tmp.v1.x, Tmp.v1.y, payloadRotation);
            payload.draw();
        }

        Draw.z(prev);
    }
}
