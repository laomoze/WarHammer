package wh.entities.world.blocks.others;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.core.World;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Posc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.logic.Ranged;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.Env;
import wh.content.WHStatusEffects;

import static mindustry.Vars.*;

public class SearchlightBlock extends Block {
    public final int timerTarget = timers++;
    public static final float logicControlCooldown = 60f * 2f;

    public float range = 220f;
    public float targetInterval = 12f;
    public float rotateSpeed = 0.8f;

    public float patrolIntervalMin = 8f * 60f;
    public float patrolIntervalMax = 15f * 60f;
    public float patrolStepMin = 120;
    public float patrolStepMax = 180f;
    public float patrolPauseMin = 4 * 60f;
    public float patrolPauseMax = 6 * 60f;

    public float beamLength = 220f;
    public float beamStart = 6f;
    public float beamFarWidth = 110f;
    public float beamAngle = 10f;
    public int beamRays = 12;
    public int beamSegments = 10;
    public float beamFalloff = 1.7f;
    public float markDuration = 20f;
    public float markAngleTolerance = 2f;

    public float warmupInLerp = 0.08f;
    public float warmupOutLerp = 0.02f;

    public float rotationOffset = -90;
    public Color beamColor = Color.valueOf("c3b4757f");
    public float lightOpacity = 0.85f;
    public boolean targetAir = true;
    public boolean targetGround = true;

    public TextureRegion topRegion;
    public TextureRegion topOutlineRegion;
    public TextureRegion lightRegion;

    public SearchlightBlock(String name) {
        super(name);

        hasPower = true;
        update = true;
        rotate = true;
        solid = true;
        configurable = true;
        saveConfig = true;
        envEnabled |= Env.space;
        canOverdrive = false;

        config(Integer.class, (SearchlightBuild build, Integer value) -> build.color = value);
    }

    @Override
    public void init() {
        clipSize = Math.max(clipSize, beamLength * 2f + beamFarWidth + size * tilesize);
        lightClipSize = Math.max(lightClipSize, clipSize * 1.2f);
        emitLight = true;
        super.init();
    }

    @Override
    public void load() {
        super.load();
        topRegion = Core.atlas.find(name + "-top", region);
        topOutlineRegion = Core.atlas.find(name + "-top-outline", topRegion);
        lightRegion = Core.atlas.find(name + "-light");
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        TextureRegion region = Core.atlas.find(name + "-top");
        if (region.found()) {
            out.add(region);
        }
    }

    @Override
    protected TextureRegion[] icons() {
        if (topRegion != null && topRegion.found()) {
            return new TextureRegion[]{region, topRegion};
        }
        return new TextureRegion[]{region};
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.placing);
    }

    @Override
    public int minimapColor(Tile tile) {
        var build = (SearchlightBuild) tile.build;
        return build == null ? 0 : build.color | 0xff;
    }

    @Override
    public void drawDefaultPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        TextureRegion reg = getPlanRegion(plan, list);
        float a = Draw.getColorAlpha();

        Draw.rect(reg, plan.drawx(), plan.drawy());
        Draw.rect(topRegion, plan.drawx(), plan.drawy(), rotate ? plan.rotation * 90f - 90f : 0f);
        Draw.rect(topOutlineRegion, plan.drawx(), plan.drawy(), rotate ? plan.rotation * 90f - 90f : 0f);

        if (plan.worldContext && player != null && teamRegion != null && teamRegion.found()) {
            if (teamRegions[player.team().id] == teamRegion) Draw.color(player.team().color, a);
            Draw.rect(teamRegions[player.team().id], plan.drawx(), plan.drawy());
            Draw.color(1f, 1f, 1f, a);
        }

        drawPlanConfig(plan, list);
    }

    public class SearchlightBuild extends Building implements Ranged {
        public float turretRotation;
        public float targetRotation;
        public float patrolTimer, pauseTimer, smoothTime, logicControlTime = -1f;
        public float logicAimX, logicAimY;
        public int color = beamColor.rgba();
        public boolean logicShooting = false;
        public Unit target;
        private boolean setupDone;


        @Override
        public void updateTile() {
            float warmupTarget = enabled ? Mathf.clamp(efficiency) : 0f;
            float warmupLerp = warmupTarget > smoothTime ? warmupInLerp : warmupOutLerp;
            smoothTime = Mathf.lerpDelta(smoothTime, warmupTarget, warmupLerp);
            if (!setupDone) {
                turretRotation = rotdeg();
                targetRotation = turretRotation;
                logicAimX = x;
                logicAimY = y;
                patrolTimer = Mathf.random(patrolIntervalMin, patrolIntervalMax);
                pauseTimer = 0f;
                setupDone = true;
            }

            if (logicControlTime > 0f) {
                logicControlTime -= Time.delta;
            }

            if (timer(timerTarget, targetInterval)) {
                target = Units.bestEnemy(team, x, y, range, this::canTarget, UnitSorts.closest);
            }

            if (Units.invalidateTarget(target, team, x, y, range)) {
                target = null;
            }

            if (logicControlled() && logicShooting) {
                targetRotation = Angles.angle(x, y, logicAimX, logicAimY);
                pauseTimer = 0f;
                patrolTimer = Mathf.random(patrolIntervalMin, patrolIntervalMax);
            } else if (target != null) {
                targetRotation = angleTo(target);
                pauseTimer = 0f;
                patrolTimer = Mathf.random(patrolIntervalMin, patrolIntervalMax);
            } else {
                if (Angles.within(turretRotation, targetRotation, 2f)) {
                    if (pauseTimer <= 0f) {
                        pauseTimer = Mathf.random(patrolPauseMin, patrolPauseMax);
                    } else {
                        pauseTimer -= delta();
                        if (pauseTimer <= 0f) {
                            pickNextPatrolRotation();
                        }
                    }
                } else {
                    pauseTimer = 0f;
                    patrolTimer -= delta();
                    if (patrolTimer <= 0f) {
                        // Failsafe: if blocked too long, pick another patrol angle.
                        pickNextPatrolRotation();
                    }
                }
            }

            if (efficiency > 0.001f && enabled) {
                turretRotation = Angles.moveToward(turretRotation, targetRotation, rotateSpeed * edelta());
            }

            if (beamStrength() > 0.01f && isIlluminating(target)) {
                target.apply(WHStatusEffects.mark, markDuration);
            }
        }

        private boolean logicControlled() {
            return logicControlTime > 0f;
        }

        private boolean canTarget(Unit unit) {
            if (unit == null || unit.dead()) return false;
            if (unit.isGrounded()) return targetGround;
            return targetAir;
        }

        private void pickNextPatrolRotation() {
            float sign = Mathf.random(1f) < 0.5f ? -1f : 1f;
            float angleStep = Mathf.random(patrolStepMin, patrolStepMax) * sign;
            targetRotation = Mathf.mod(turretRotation + angleStep, 360f);
            patrolTimer = Mathf.random(patrolIntervalMin, patrolIntervalMax);
            pauseTimer = 0f;
        }

        private float beamStrength() {
            return Mathf.clamp(smoothTime);
        }

        private boolean isIlluminating(Unit unit) {
            if (unit == null || !unit.isAdded() || unit.dead()) return false;
            float angle = beamAngle / 2f + markAngleTolerance;
            float dst = beamLength + unit.hitSize / 2f;
            return unit.within(this, dst) && Angles.within(turretRotation, angleTo(unit), angle);
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);

            float strength = beamStrength();
            if (strength > 0.001f) {
                drawBeam(strength);
            }

            Draw.z(Layer.turret);
            if (topOutlineRegion.found()) {
                Draw.rect(topOutlineRegion, x, y, turretRotation + rotationOffset);
            }
            Draw.rect(topRegion, x, y, turretRotation + rotationOffset);
        }

        private void drawBeam(float strength) {
            float drawRot = turretRotation + rotationOffset;
            Color drawColor = Tmp.c1.set(color);
            float sx = x + Angles.trnsx(turretRotation, beamStart);
            float sy = y + Angles.trnsy(turretRotation, beamStart);
            float angleFrac = beamAngle / 360f;
            float length = beamLength * smoothTime;

            Draw.z(Layer.blockAdditive - 0.01f);
            Draw.color(drawColor.r, drawColor.g, drawColor.b, 1f);
            Draw.blend(Blending.additive);

            // Radial gradient fill: near brighter, far dimmer.
            int segs = Math.max(2, beamSegments);
            for (int i = 1; i <= segs; i++) {
                float frac = i / (float) segs;
                float fall = Mathf.pow(1f - frac, beamFalloff);
                Draw.alpha(strength * (0.11f / segs + 0.08f * fall / segs));
                Fill.arc(sx, sy, length * frac, angleFrac, turretRotation - beamAngle / 2f);
            }

            Fill.circle(sx, sy, length * 0.08f);

            if (lightRegion.found()) {
                Draw.alpha(strength * 0.95f);
                Draw.rect(lightRegion, x, y, drawRot);
            }
            Draw.blend();
            Draw.color();
        }


        @Override
        public void drawLight() {
            float strength = beamStrength();
            if (strength <= 0.001f) return;

            float sx = x + Angles.trnsx(turretRotation, beamStart);
            float sy = y + Angles.trnsy(turretRotation, beamStart);
            Color drawColor = Tmp.c1.set(color).a(1f);
            float length = beamLength * smoothTime;

            // Stack multiple light rays into a fan-shaped beam.
            int rays = Math.max(3, beamRays);
            int segs = Math.max(2, beamSegments);
            float half = beamAngle / 2f;
            float stroke = beamFarWidth * 0.14f * smoothTime;
            for (int i = 0; i < rays; i++) {
                float t = i / (float) (rays - 1);
                float ang = turretRotation - half + beamAngle * t;
                for (int s = 0; s < segs; s++) {
                    float p0 = s / (float) segs;
                    float p1 = (s + 1f) / segs;

                    float x0 = sx + Angles.trnsx(ang, length * p0);
                    float y0 = sy + Angles.trnsy(ang, length * p0);
                    float x1 = sx + Angles.trnsx(ang, length * p1);
                    float y1 = sy + Angles.trnsy(ang, length * p1);

                    float segStroke = stroke * (1f - 0.25f * p0);
                    Drawf.light(x0, y0, x1, y1, segStroke, drawColor, lightOpacity * 0.2f * strength);
                }
            }

            float cex = sx + Angles.trnsx(turretRotation, length);
            float cey = sy + Angles.trnsy(turretRotation, length);
            Drawf.light(sx, sy, 50 * smoothTime, drawColor, lightOpacity * 0.2f * strength);
            Drawf.light(sx, sy, cex, cey, beamFarWidth * 0.12f * smoothTime, drawColor, lightOpacity * 0.2f * strength);
        }

        @Override
        public void configured(Unit unit, Object value) {
            super.configured(unit, value);
            if (!headless) renderer.minimap.update(tile);
        }

        @Override
        public void control(LAccess type, double p1, double p2, double p3, double p4) {
            if (type == LAccess.color) {
                color = Tmp.c1.fromDouble(p1).rgba8888();
                if (!headless) renderer.minimap.update(tile);
            } else if (type == LAccess.shoot) {
                logicAimX = World.unconv((float) p1);
                logicAimY = World.unconv((float) p2);
                logicShooting = !Mathf.zero((float) p3);
                logicControlTime = logicControlCooldown;
            }
            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public void control(LAccess type, Object p1, double p2, double p3, double p4) {
            if (type == LAccess.shootp) {
                if (p1 instanceof Posc pos) {
                    logicAimX = pos.getX();
                    logicAimY = pos.getY();
                }
                logicShooting = !Mathf.zero((float) p2);
                logicControlTime = logicControlCooldown;
            }
            super.control(type, p1, p2, p3, p4);
        }

        @Override
        public double sense(LAccess sensor) {
            return switch (sensor) {
                case color -> Tmp.c1.set(color).toDoubleBits();
                case rotation -> turretRotation;
                case shootX -> World.conv(logicAimX);
                case shootY -> World.conv(logicAimY);
                case shooting -> logicControlled() && logicShooting ? 1d : 0d;
                default -> super.sense(sensor);
            };
        }

        @Override
        public void buildConfiguration(Table table) {
            table.button(Icon.pencil, Styles.cleari, () -> {
                ui.picker.show(Tmp.c1.set(color).a(0.5f), false, res -> configure(res.rgba()));
                deselect();
            }).size(40f);
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (this == other) {
                deselect();
                return false;
            }
            return true;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.dashCircle(x, y, range, team.color);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(turretRotation);
            write.f(targetRotation);
            write.f(patrolTimer);
            write.f(pauseTimer);
            write.i(color);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            turretRotation = read.f();
            targetRotation = read.f();
            patrolTimer = read.f();
            if (revision >= 2) {
                pauseTimer = read.f();
            } else {
                pauseTimer = 0f;
            }
            if (revision >= 1) {
                color = read.i();
            } else {
                color = beamColor.rgba();
            }
            setupDone = true;
        }

        @Override
        public Integer config() {
            return color;
        }

        @Override
        public byte version() {
            return 2;
        }

        @Override
        public float range() {
            return range;
        }
    }
}
