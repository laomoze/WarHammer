package wh.entities.world.Psy;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import mindustry.Vars;
import mindustry.gen.Building;

final class PsychicLinkScanner {
    static final int ignore = 0, block = 1, accept = 2;

    interface Candidate {
        int classify(Building building);
    }

    static Building find(int direction, int fixed, int axisMin, int axisMax, float axisCenter,
                         int range, Candidate candidate) {
        Point2 dir = Geometry.d4[direction];
        boolean horizontal = direction == 0 || direction == 2;
        boolean[] blocked = new boolean[Math.max(0, axisMax - axisMin + 1)];

        for (int distance = 1; distance <= range; distance++) {
            Building best = null;
            float bestAxisDistance = Float.MAX_VALUE;
            boolean anyOpenLane = false;

            for (int axis = axisMin; axis <= axisMax; axis++) {
                int index = axis - axisMin;
                if (blocked[index]) continue;
                anyOpenLane = true;

                int tileX = horizontal ? fixed + dir.x * distance : axis;
                int tileY = horizontal ? axis : fixed + dir.y * distance;
                Building other = Vars.world.build(tileX, tileY);
                if (other == null) continue;

                int result = other.isInsulated() ? block : candidate.classify(other);
                if (result == ignore) continue;
                blocked[index] = true;
                if (result != accept) continue;

                float axisDistance = horizontal ? Math.abs(other.y - axisCenter) : Math.abs(other.x - axisCenter);
                if (best == null || axisDistance < bestAxisDistance) {
                    best = other;
                    bestAxisDistance = axisDistance;
                }
            }

            if (best != null) return best;
            if (!anyOpenLane) break;
        }
        return null;
    }

    private PsychicLinkScanner() {
    }
}
