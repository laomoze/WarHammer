package wh.entities.world.drawer.factory;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;
import mindustry.world.draw.DrawBlock;

public class DrawMultiTrail extends DrawBlock {
    private final TrailConfig[] configs;
    private final TrailUpdater updater;
    private final IntMap<Trail[]> buildingTrails = new IntMap<>();
    private final TrailPoint point = new TrailPoint();
    private final float layer;
    private final float activeWarmup;
    private final float cleanupInterval;
    private float cleanupTime;

    public DrawMultiTrail(TrailUpdater updater, TrailConfig... configs) {
        this(Layer.effect, 0.001f, 60f, updater, configs);
    }

    public DrawMultiTrail(float layer, float activeWarmup, float cleanupInterval, TrailUpdater updater, TrailConfig... configs) {
        this.layer = layer;
        this.activeWarmup = activeWarmup;
        this.cleanupInterval = cleanupInterval;
        this.updater = updater;
        this.configs = configs;
    }

    @Override
    public void draw(Building build) {
        if (configs == null || configs.length == 0 || updater == null) return;

        Trail[] trails = trails(build);
        boolean active = build.warmup() > activeWarmup;

        for (int i = 0; i < configs.length; i++) {
            TrailConfig config = configs[i];
            Trail trail = trails[i];

            point.set(build.x, build.y, 1f, active);
            updater.update(build, trail, i, point);

            if (point.active) {
                trail.length = config.length;
                trail.update(point.x, point.y, point.width);
            } else {
                trail.shorten();
            }

            if (trail.size() <= 0 || config.stroke <= 0f) continue;

            float z = Draw.z();
            Draw.z(config.layer >= 0f ? config.layer : layer);
            Draw.color(config.color);
            trail.draw(config.color, config.stroke);
            if (config.drawCap) {
                trail.drawCap(config.color, config.stroke);
            }
            Draw.z(z);
            Draw.reset();
        }

        cleanupTime += Time.delta;
        if (cleanupTime >= cleanupInterval) {
            cleanupTime = 0f;
            cleanup();
        }
    }

    private Trail[] trails(Building build) {
        Trail[] trails = buildingTrails.get(build.id);
        if (trails == null || trails.length != configs.length) {
            trails = new Trail[configs.length];
            for (int i = 0; i < configs.length; i++) {
                trails[i] = new Trail(configs[i].length);
            }
            buildingTrails.put(build.id, trails);
        }
        return trails;
    }

    private void cleanup() {
        var iterator = buildingTrails.iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Building build = Groups.build.getByID(entry.key);
            if (build == null || !build.isAdded()) {
                iterator.remove();
            }
        }
    }

    public interface TrailUpdater {
        void update(Building build, Trail trail, int index, TrailPoint point);
    }

    public static class TrailPoint {
        public float x, y, width;
        public boolean active;

        public TrailPoint set(float x, float y, float width, boolean active) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.active = active;
            return this;
        }
    }

    public static class TrailConfig {
        public final int length;
        public final float stroke;
        public final Color color;
        public final float layer;
        public final boolean drawCap;

        public TrailConfig(int length, float stroke, Color color) {
            this(length, stroke, color, -1f, false);
        }

        public TrailConfig(int length, float stroke, Color color, float layer, boolean drawCap) {
            this.length = Math.max(length, 1);
            this.stroke = Mathf.clamp(stroke, 0f, 999f);
            this.color = color == null ? Color.white : color;
            this.layer = layer;
            this.drawCap = drawCap;
        }
    }
}
