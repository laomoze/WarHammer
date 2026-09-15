package wh.maps.filters;

import arc.math.Mathf;
import mindustry.content.Blocks;
import mindustry.world.Block;

import java.io.Serializable;

public class RuinStep implements Serializable {
    public float radius;
    public Block floor, wall;
    public boolean removeWall;
    public StepMode stepMode = StepMode.chebyshev;

    public RuinStep() {
    }

    public RuinStep(float radius, Block floor, Block wall) {
        this.radius = radius;
        this.floor = floor;
        this.wall = wall;
    }

    public RuinStep copy() {
        RuinStep step = new RuinStep(radius, floor, wall);
        step.removeWall = removeWall;
        step.stepMode = stepMode;
        if (step.wall == Blocks.removeWall) {
            step.wall = null;
            step.removeWall = true;
        }
        return step;
    }

    public boolean hasFloor() {
        return floor != null && floor != Blocks.removeWall;
    }

    public boolean preservesFloor() {
        return floor == Blocks.removeWall;
    }

    public boolean hasWall() {
        return wall != null && wall != Blocks.removeWall;
    }

    public boolean removesWall() {
        return removeWall || wall == Blocks.removeWall;
    }

    public boolean affectsBlock() {
        return removesWall() || hasWall();
    }

    public boolean matches(int cx, int cy, int x, int y) {
        float dx = x - cx, dy = y - cy;
        switch (stepMode) {
            case geometric:
                return Mathf.len(dx, dy) <= radius;
            case chebyshev:
                return Math.max(Math.abs(dx), Math.abs(dy)) <= radius;
            default:
                return Math.abs(dx) + Math.abs(dy) <= radius;
        }
    }

    public static RuinStep floor(float radius, Block floor) {
        return new RuinStep(radius, floor, null);
    }

    public static RuinStep wall(float radius, Block wall) {
        return new RuinStep(radius, null, wall);
    }

    public static RuinStep removeWall(float radius) {
        RuinStep step = new RuinStep(radius, Blocks.removeWall, null);
        step.removeWall = true;
        return step;
    }

    public static RuinStep[] defaults() {
        return new RuinStep[]{
                floor(6, Blocks.metalFloor),
                floor(1, Blocks.metalFloor3),
                wall(4, Blocks.metalWall2),
                removeWall(3)
        };
    }

    public enum StepMode {
        geometric,
        manhattan,
        chebyshev;

        public StepMode next() {
            StepMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }
}

