package wh.gen.CarrierUnit;

import arc.math.geom.Vec2;
import arc.util.Nullable;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.blocks.payloads.Payload;
import wh.entities.world.entities.CarrierUnitType;

/**
 * Carrier behavior contract used by fighter AI.
 * Any carrier-like unit can implement this interface.
 */
public interface CarrierHostc extends Teamc{
    CarrierUnitType carrierType();

    int clampRunway(int runway);

    boolean ownsFighter(Unit fighter);

    int fighterRunway(Unit fighter);

    float fighterSortieTime(Unit fighter);

    void runwayFrontPoint(int runway, Vec2 out);

    void launchExitPoint(int runway, Vec2 out);

    void recoveryPoint(int runway, Vec2 out);

    void runwayQueueInsertPoint(int runway, Vec2 out);

    boolean allowRecoveryApproach(Unit fighter);

    boolean tryRecoverFighter(Unit fighter);

    boolean shouldRecallFighter(Unit fighter);

    void releaseRecoveryClaim(Unit fighter);

    boolean focusPosition(Vec2 out);

    @Nullable
    Teamc lockedTarget();

    int deckSlotForPayload(Payload payload);

    void deckSlotWorldVisual(Payload payload, int slot, Vec2 out);

    float deckRefitRemaining(int fighterId);

    boolean deckRefitShowsConstruct(int fighterId);
}
