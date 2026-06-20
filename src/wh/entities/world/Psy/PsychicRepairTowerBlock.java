package wh.entities.world.Psy;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Strings;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.Units;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.logic.Ranged;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.ui.PsychicBar;

import static mindustry.Vars.tilesize;

public class PsychicRepairTowerBlock extends Block {
    protected static final float refreshInterval = 6f;

    public DrawBlock drawer = new DrawDefault();
    public float range = 80f;
    public float healAmount = 1f;
    public float psychicCapacity = 120f;
    public float psychicUse = 0.4f;
    public float passivePsychicLoss = 0f;
    public float warmupSpeed = 0.08f;
    public Color circleColor = Pal.heal;
    public Color glowColor = Pal.heal.cpy().a(0.5f);
    public float circleSpeed = 120f, circleStroke = 3f, squareRad = 3f, squareSpinScl = 0.8f, glowMag = 0.5f, glowScl = 8f;
    public TextureRegion glow;

    public PsychicRepairTowerBlock(String name) {
        super(name);
        update = true;
        solid = true;
        sync = true;
        suppressable = true;
        flags = EnumSet.of(BlockFlag.repair);
        hasPower = true;
        drawArrow = false;
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
        glow = Core.atlas.find(name + "-glow", Core.atlas.find("repair-tower-glow", Core.atlas.find("error")));
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.repairSpeed, healAmount * 60f, StatUnit.perSecond);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("psychic", (PsychicRepairTowerBuild build) -> new PsychicBar(
                () -> Core.bundle.format("bar.wh-psychic-storage",
                        Strings.autoFixed(build.psychic.amount(), 2),
                        Strings.autoFixed(psychicCapacity, 0)),
                () -> Pal.sapBullet,
                () -> build.psychic.fraction(psychicCapacity)
        ));
        addBar("psychic-use", (PsychicRepairTowerBuild build) -> new PsychicBar(
                () -> Core.bundle.format("bar.wh-psychic-use", Strings.autoFixed(build.lastUse * 60f, 2)),
                () -> Pal.sapBullet,
                () -> psychicUse <= 0.0001f ? 0f : build.lastUse / psychicUse
        ));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.placing);
    }

    public class PsychicRepairTowerBuild extends Building implements Ranged, PsychicNetworkNode {
        public final PsychicModule psychic = new PsychicModule();
        public float refresh = Mathf.random(refreshInterval);
        public float warmup;
        public float totalProgress;
        public float lastUse;
        public Seq<Unit> targets = new Seq<>();

        @Override
        public void updateTile() {
            if (passivePsychicLoss > 0f) {
                psychic.remove(passivePsychicLoss / 60f * delta());
            }

            if (potentialEfficiency > 0 && (refresh += Time.delta) >= refreshInterval) {
                targets.clear();
                refresh = 0f;
                Units.nearby(team, x, y, range, u -> {
                    if (u.damaged()) targets.add(u);
                });
            }

            if (checkSuppression()) {
                warmup = 0f;
                lastUse = 0f;
                return;
            }

            boolean any = false;
            float used = 0f;
            if (efficiency > 0f) {
                float perTarget = psychicUse / Math.max(1, targets.size) * edelta();
                for (var target : targets) {
                    if (!target.damaged()) continue;
                    if (psychic.remove(perTarget) < perTarget * 0.999f) break;
                    target.heal(healAmount * edelta());
                    used += perTarget;
                    any = true;
                }
            }

            lastUse = Mathf.lerpDelta(lastUse, used / Math.max(delta(), 0.0001f), 0.18f);
            warmup = Mathf.lerpDelta(warmup, any ? efficiency : 0f, warmupSpeed);
            totalProgress += Time.delta / circleSpeed;
        }

        @Override
        public boolean shouldConsume() {
            return targets.size > 0;
        }

        @Override
        public void draw() {
            drawer.draw(this);

            if (warmup <= 0.001f) return;

            Draw.z(Layer.effect);
            float mod = totalProgress % 1f;
            Draw.color(circleColor);
            Lines.stroke(circleStroke * (1f - mod) * warmup);
            Lines.circle(x, y, range * mod);
            Draw.color(Pal.heal);
            Fill.square(x, y, squareRad * warmup, Time.time / squareSpinScl);
            Draw.reset();

            Drawf.additive(glow, glowColor, warmup * (1f - glowMag + Mathf.absin(Time.time, glowScl, glowMag)), x, y, 0f, Layer.blockAdditive);
        }

        @Override
        public void drawSelect() {
            Drawf.dashCircle(x, y, range, Pal.placing);
        }

        public float range() {
            return range;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        @Override
        public float progress() {
            return psychicUse <= 0.0001f ? 0f : Mathf.clamp(lastUse / psychicUse);
        }

        @Override
        public boolean acceptEnergy(PsychicNetworkNode source) {
            return enabled && psychic.amount() + 0.0001f < psychicCapacity;
        }

        @Override
        public float getEnergyNeed() {
            return Math.max(psychicCapacity - psychic.amount(), 0f);
        }

        @Override
        public float handleEnergy(float amount) {
            return psychic.add(amount, psychicCapacity);
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress) return warmup;
            return super.sense(sensor);
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            psychic.write(write);
            write.f(warmup);
            write.f(totalProgress);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            psychic.read(read);
            warmup = read.f();
            totalProgress = read.f();
        }
    }
}
