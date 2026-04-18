package wh.entities;

import arc.math.Mathf;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.gen.Unit;
import mindustry.world.meta.BlockGroup;

public final class WHUnitSorts {
    private static final float distanceWeight = 6400f;
    private static final float regionOffset = 68f;
    private static final float regionSize = 128f;
    private static final float regionRange = 128f;

    public static final Units.Sortf slowest = (u, x, y) -> u.speed() + Mathf.dst2(u.x, u.y, x, y) / distanceWeight;
    public static final Units.Sortf fastest = (u, x, y) -> -u.speed() + Mathf.dst2(u.x, u.y, x, y) / distanceWeight;
    public static final Units.Sortf regionalHPMaximumUnit = (u, x, y) -> -nearbyUnitHp(u);
    public static final Units.Sortf regionalHPMaximumBuilding = (u, x, y) -> -nearbyBuildingHp(u);
    public static final Units.Sortf regionalHPMaximumAll = (u, x, y) -> -(nearbyUnitHp(u) + nearbyBuildingHp(u));

    private static float nearbyUnitHp(Unit unit) {
        final float[] hp = {0f};
        Vars.state.teams.get(unit.team).tree().intersect(
                unit.x - regionOffset, unit.y - regionOffset, regionSize, regionSize,
                (Unit target) -> hp[0] += target.health + target.shield
        );
        return hp[0];
    }

    private static float nearbyBuildingHp(Unit unit) {
        final float[] hp = {0f};
        Vars.indexer.eachBlock(
                unit, regionRange,
                building -> building.block.group != BlockGroup.walls,
                building -> hp[0] += building.health
        );
        return hp[0];
    }

    private WHUnitSorts() {
    }
}
