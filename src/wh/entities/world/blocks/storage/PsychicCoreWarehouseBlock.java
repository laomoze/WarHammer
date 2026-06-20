package wh.entities.world.blocks.storage;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.entities.world.Psy.PsychicModule;
import wh.entities.world.Psy.PsychicNetworkNode;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public class PsychicCoreWarehouseBlock extends StorageBlock {
    public DrawBlock drawer = new DrawDefault();
    public int linkRangeX = 18;
    public int linkRangeY = 10;
    public int coreCapacityBonus = 6000;
    public float psychicCapacity = 120f;
    public float passivePsychicLoss = 0f;
    public float psychicUse = 0.2f;
    public boolean requireCoreInRange = true;

    public PsychicCoreWarehouseBlock(String name) {
        super(name);
        update = true;
        sync = true;
        buildType = PsychicCoreWarehouseBuild::new;
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
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
    public void setBars() {
        super.setBars();
        addBar("psychic", (PsychicCoreWarehouseBuild build) -> new PsychicBar(
                () -> Core.bundle.format("bar.wh-psychic-storage",
                        Strings.autoFixed(build.psychic.amount(), 2),
                        Strings.autoFixed(psychicCapacity, 0)),
                () -> Pal.sapBullet,
                () -> build.psychic.fraction(psychicCapacity)
        ));
    }

    @Override
    public void setStats() {
        super.setStats();
        PsychicStatValues.add(stats, WHStats.psychicCapacity, psychicCapacity, StatUnit.none);
        if (psychicUse > 0f) {
            PsychicStatValues.add(stats, WHStats.psychicConsumption, psychicUse, StatUnit.perSecond);
        }
        stats.add(WHStats.psychicFieldWidth, linkRangeX * 2f, StatUnit.blocks);
        stats.add(WHStats.psychicFieldHeight, linkRangeY * 2f, StatUnit.blocks);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return !requireCoreInRange || findCore(tile.x, tile.y, team) != null;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        drawLinkArea(x * tilesize + offset, y * tilesize + offset);
        if (requireCoreInRange && findCore(x, y, Vars.player.team()) == null) {
            drawPlaceText(Core.bundle.get("bar.corereq"), x, y, false);
        }
    }

    public void drawLinkArea(float worldX, float worldY) {
        Draw.z(Layer.overlayUI);
        Draw.color(Pal.placing);
        Draw.alpha(0.75f);
        Lines.stroke(1.2f);
        Lines.rect(worldX - linkRangeX * tilesize, worldY - linkRangeY * tilesize,
                linkRangeX * 2f * tilesize, linkRangeY * 2f * tilesize);
        Draw.reset();
    }

    public @Nullable CoreBlock.CoreBuild findCore(int tileX, int tileY, Team team) {
        Rect rect = new Rect(
                (tileX - linkRangeX) * tilesize,
                (tileY - linkRangeY) * tilesize,
                linkRangeX * 2f * tilesize,
                linkRangeY * 2f * tilesize
        );
        Seq<CoreBlock.CoreBuild> cores = team.cores();
        CoreBlock.CoreBuild closest = null;
        float best = Float.MAX_VALUE;
        for (CoreBlock.CoreBuild core : cores) {
            if (core == null || !core.isAdded()) continue;
            if (!rect.contains(core.x, core.y)) continue;
            float dst = Mathf.dst2(tileX, tileY, core.tileX(), core.tileY());
            if (dst < best) {
                best = dst;
                closest = core;
            }
        }
        return closest;
    }

    public class PsychicCoreWarehouseBuild extends StorageBuild implements PsychicNetworkNode {
        public final PsychicModule psychic = new PsychicModule();
        public @Nullable CoreBlock.CoreBuild remoteCore;
        public int cachedCorePos = -1;
        public boolean capacityApplied;

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void placed() {
            super.placed();
            updateCoreLink();
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            updateCoreLink();
        }

        @Override
        public void updateTile() {
            updateCoreLink();
            if (passivePsychicLoss > 0f) {
                psychic.remove(passivePsychicLoss / 60f * delta());
            }
            if (remoteCore != null && psychicUse > 0f) {
                psychic.remove(psychicUse / 60f * delta());
            }
        }

        public void updateCoreLink() {
            CoreBlock.CoreBuild next = findCore(tile.x, tile.y, team);
            if (next != remoteCore) {
                if (remoteCore != null && capacityApplied) {
                    remoteCore.storageCapacity = Math.max(remoteCore.storageCapacity - coreCapacityBonus, 0);
                    capacityApplied = false;
                }
                remoteCore = next;
                cachedCorePos = next == null ? -1 : next.pos();
            }
            linkedCore = remoteCore;
            if (remoteCore != null) {
                items = remoteCore.items;
                if (!capacityApplied) {
                    remoteCore.storageCapacity += coreCapacityBonus;
                    capacityApplied = true;
                }
            }
        }

        @Override
        public void onRemoved() {
            if (remoteCore != null && capacityApplied) {
                remoteCore.storageCapacity = Math.max(remoteCore.storageCapacity - coreCapacityBonus, 0);
                capacityApplied = false;
            }
            super.onRemoved();
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return remoteCore != null ? remoteCore.getMaximumAccepted(item) : super.getMaximumAccepted(item);
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (remoteCore != null) {
                return psychic.amount() > 0.0001f && remoteCore.acceptItem(source, item);
            }
            return super.acceptItem(source, item);
        }

        @Override
        public void handleItem(Building source, Item item) {
            if (remoteCore != null) {
                remoteCore.handleItem(source, item);
                return;
            }
            super.handleItem(source, item);
        }

        @Override
        public int removeStack(Item item, int amount) {
            if (remoteCore != null && psychic.amount() <= 0.0001f) {
                return 0;
            }
            int removed = super.removeStack(item, amount);
            return removed;
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawLinkArea(x, y);
            if (remoteCore != null) {
                Drawf.square(remoteCore.x, remoteCore.y, remoteCore.block.size * tilesize / 2f + 3f, Pal.accent);
            }
        }

        @Override
        public boolean acceptEnergy(PsychicNetworkNode source) {
            return enabled && psychic.amount() + 0.0001f < psychicCapacity;
        }

        @Override
        public boolean acceptsPsychicLinks() {
            return true;
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
            if (sensor == LAccess.itemCapacity && remoteCore != null) return remoteCore.sense(sensor);
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
            write.i(remoteCore == null ? -1 : remoteCore.pos());
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            psychic.read(read);
            int pos = read.i();
            Building build = pos == -1 ? null : Vars.world.build(pos);
            remoteCore = build instanceof CoreBlock.CoreBuild core ? core : null;
            linkedCore = remoteCore;
        }
    }
}
