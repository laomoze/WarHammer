package wh.entities.world.Psy;

import arc.Core;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.IntFloatMap;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.world.blocks.defense.RegenProjector;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.*;

public class PsychicRegenProjectorBlock extends RegenProjector {
    private static final IntFloatMap mendMap = new IntFloatMap();
    private static long lastUpdateFrame = -1L;

    public float psychicCapacity = 120f;
    public float psychicUse = 0.8f;
    public String fallbackRegionName = "wrap-projector";

    public PsychicRegenProjectorBlock(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();

        TextureRegion fallback = Core.atlas.find(fallbackRegionName, region);
        if (region == null || !region.found()) {
            region = fallback;
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        PsychicStatValues.add(stats, WHStats.psychicCapacity, psychicCapacity, StatUnit.none);
        PsychicStatValues.add(stats, WHStats.psychicConsumption, psychicUse, StatUnit.perSecond);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic", (PsychicRegenProjectorBuild build) -> new PsychicBar(
                () -> Core.bundle.format("bar.wh-psychic-storage",
                        Strings.autoFixed(build.psychicStored(), 2),
                        Strings.autoFixed(psychicCapacity, 0)),
                () -> WHPal.PsyColor,
                build::psychicFraction
        ));

        addBar("psychic-use", (PsychicRegenProjectorBuild build) -> new PsychicBar(
                () -> Core.bundle.format("bar.wh-psychic-use", Strings.autoFixed(build.lastUse * 60f, 2)),
                () -> WHPal.PsyColor,
                () -> psychicUse <= 0.0001f ? 0f : build.lastUse / psychicUse
        ));
    }

    public class PsychicRegenProjectorBuild extends RegenProjectorBuild implements PsychicNetworkNode {
        public final PsychicModule psychic = new PsychicModule();
        public float lastUse;

        public float psychicStored() {
            return psychic.amount();
        }

        public float psychicFraction() {
            return psychic.fraction(psychicCapacity);
        }

        @Override
        public void updateTile() {
            if (lastChange != world.tileChanges) {
                lastChange = world.tileChanges;
                updateTargets();
            }

            warmup = Mathf.approachDelta(warmup, didRegen ? 1f : 0f, 1f / 70f);
            totalTime += warmup * Time.delta;
            didRegen = false;
            anyTargets = false;

            psychic.clamp(psychicCapacity);

            if (checkSuppression()) {
                lastUse = 0f;
                return;
            }

            anyTargets = targets.contains(Building::damaged);

            float used = 0f;
            if (efficiency > 0f && anyTargets) {
                float required = psychicUse / 60f * edelta();

                if (required <= 0.0001f || psychic.remove(required) >= required * 0.999f) {
                    used = required;

                    if ((optionalTimer += edelta() * optionalEfficiency) >= optionalUseTime) {
                        consume();
                        optionalTimer = 0f;
                    }

                    float healAmount = Mathf.lerp(1f, optionalMultiplier, optionalEfficiency) * healPercent;

                    for (var build : targets) {
                        if (!build.damaged() || build.isHealSuppressed()) continue;

                        didRegen = true;

                        int pos = build.pos();
                        float value = mendMap.get(pos);
                        mendMap.put(pos, Math.min(Math.max(value, healAmount * edelta() * build.block.health / 100f), build.block.health - build.health));

                        if (value <= 0f && Mathf.chanceDelta(effectChance * build.block.size * build.block.size)) {
                            effect.at(build.x + Mathf.range(build.block.size * tilesize / 2f - 1f), build.y + Mathf.range(build.block.size * tilesize / 2f - 1f));
                        }
                    }
                }
            }

            lastUse = Mathf.lerpDelta(lastUse, used / Math.max(delta(), 0.0001f), 0.18f);

            if (lastUpdateFrame != state.updateId) {
                lastUpdateFrame = state.updateId;

                for (var entry : mendMap.entries()) {
                    var build = world.build(entry.key);
                    if (build != null) {
                        build.heal(entry.value);
                        build.recentlyHealed();
                    }
                }
                mendMap.clear();
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.dashSquare(baseColor, x, y, range * tilesize);
            for (var target : targets) {
                Drawf.selected(target, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f)));
            }
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
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            psychic.write(write);
            write.f(lastUse);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            psychic.read(read);
            lastUse = revision >= 1 ? read.f() : 0f;
        }
    }
}
