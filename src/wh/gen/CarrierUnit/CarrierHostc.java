package wh.gen.CarrierUnit;

import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.world.blocks.payloads.*;
import wh.entities.world.entities.*;

/**
 * Carrier behavior contract used by fighter AI.
 * Any carrier-like unit can implement this interface.
 */
public interface CarrierHostc extends Teamc{
    CarrierUnitType carrierType();

    int clampRunway(int runway);

    Vec2 runwayFrontPoint(int runway, Vec2 out);

    Vec2 launchExitPoint(int runway, Vec2 out);

    Vec2 recoveryPoint(int runway, Vec2 out);

    Vec2 runwayQueueInsertPoint(int runway, Vec2 out);

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
