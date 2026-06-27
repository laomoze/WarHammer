package wh.entities.world.Psy;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.StatUnit;
import mindustry.world.modules.ItemModule;
import wh.content.WHStats;
import wh.core.WHSettings;
import wh.graphics.Drawn;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public class PsyStorageBlock extends StorageBlock {
    public DrawBlock drawer = new DrawDefault();
    public int linkRange = 18;
    public int coreCapacityBonus = 3000;
    public float psychicCapacity = 120f;
    public float psychicUse = 0.2f;
    public boolean requireCoreInRange = true;
    public float connectWarmupSpeed = 0.01f;
    public float connectFadeSpeed = 0.02f;
    public float coreCheckInterval = 20f;
    public float beamWidth = 2.2f;
    public float beamPulseScl = 6f;
    public float beamPulseMag = 0.22f;
    public int beamParticles = 5;
    public float beamParticleLife = 42f;
    public float beamParticleLength = 7f;
    public float beamParticleStroke = 0.8f;

    public PsyStorageBlock(String name) {
        super(name);
        update = true;
        sync = true;
        itemCapacity = 0;
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
                () -> WHPal.PsyColor,
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
        stats.add(WHStats.psychicFieldWidth, linkRange * 2f, StatUnit.blocks);
        stats.add(WHStats.psychicFieldHeight, linkRange * 2f, StatUnit.blocks);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return !requireCoreInRange || findCore(tile.x, tile.y, team) != null;
    }

    @Override
    public boolean isAccessible() {
        return true;
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
        Draw.color(Pal.accent);
        Draw.alpha(0.75f);
        Lines.stroke(1.2f);
        Lines.rect(worldX - linkRange * tilesize, worldY - linkRange * tilesize,
                linkRange * 2f * tilesize, linkRange * 2f * tilesize);
        Draw.reset();
    }

    public @Nullable CoreBuild findCore(int tileX, int tileY, Team team) {
        var cores = team.cores();
        CoreBuild closest = null;
        float best = Float.MAX_VALUE;
        for (CoreBuild core : cores) {
            if (core == null || !core.isAdded()) continue;
            if (Math.abs(core.tileX() - tileX) > linkRange || Math.abs(core.tileY() - tileY) > linkRange) continue;
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
        public final ItemModule emptyItems = new ItemModule();
        public @Nullable CoreBuild remoteCore;
        public int cachedCorePos = -1;
        public boolean capacityApplied;
        public float linkWarmup;
        public float coreCheckTime;
        public float lastCoreX, lastCoreY;
        public int lastCoreSize = 1;

        @Override
        public void draw() {
            drawer.draw(this);
            drawCoreLink();
        }

        @Override
        public void placed() {
            super.placed();
            updateCoreLink(true);
        }

        @Override
        public void pickedUp() {
            releaseCoreCapacity();
            remoteCore = null;
            linkedCore = null;
            items = emptyItems;
            emptyItems.clear();
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            updateCoreLink(true);
        }

        @Override
        public void updateTile() {
            updateCoreLink(false);
            updateLinkVisuals();

            if (hasOperationalCore() && psychicUse > 0f) {
                psychic.remove(psychicUse / 60f * delta());
            }

            if (!hasOperationalCore()) {
                emptyItems.clear();
            }
        }

        public void updateCoreLink() {
            updateCoreLink(false);
        }

        public void updateCoreLink(boolean forceSearch) {
            CoreBuild next = validCore(remoteCore) ? remoteCore : null;

            if (next == null && cachedCorePos != -1) {
                Building cached = Vars.world.build(cachedCorePos);
                next = cached instanceof CoreBuild core && validCore(core) ? core : null;
                if (next == null && cached != null) {
                    cachedCorePos = -1;
                }
            }

            if (next == null) {
                coreCheckTime += delta();
                if (forceSearch || coreCheckTime >= coreCheckInterval) {
                    coreCheckTime = 0f;
                    next = findCore(tile.x, tile.y, team);
                }
            } else {
                coreCheckTime = 0f;
            }

            if (next != remoteCore) {
                releaseCoreCapacity();
                remoteCore = next;
            }

            linkedCore = null;
            cachedCorePos = remoteCore == null ? -1 : remoteCore.pos();

            if (remoteCore == null) {
                items = emptyItems;
                emptyItems.clear();
                return;
            }

            items = remoteCore.items;
            lastCoreX = remoteCore.x;
            lastCoreY = remoteCore.y;
            lastCoreSize = remoteCore.block.size;

            if (!capacityApplied) {
                remoteCore.storageCapacity += coreCapacityBonus;
                capacityApplied = true;
            }
        }

        @Override
        public void onRemoved() {
            releaseCoreCapacity();
            remoteCore = null;
            linkedCore = null;
            items = emptyItems;
            emptyItems.clear();
            super.onRemoved();
        }

        @Override
        public int getMaximumAccepted(Item item) {
            return 0;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return false;
        }

        @Override
        public boolean allowDeposit() {
            return false;
        }

        @Override
        public boolean canUnload() {
            return true;
        }

        @Override
        public void handleItem(Building source, Item item) {
        }

        @Override
        public int removeStack(Item item, int amount) {
            if (!canAccessCoreItems()) {
                return 0;
            }
            return remoteCore.removeStack(item, amount);
        }


        @Override
        public void itemTaken(Item item) {
            if (remoteCore != null) {
                remoteCore.itemTaken(item);
            }
        }


        @Override
        public void display(Table table) {
            updateCoreLink(true);
            super.display(table);
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawLinkArea(x, y);
            if (remoteCore != null) {
                Drawf.square(remoteCore.x, remoteCore.y, remoteCore.block.size * tilesize / 2f + 3f, WHPal.PsyColor);
            }
        }

        @Override
        public boolean acceptEnergy(PsychicNetworkNode source) {
            return enabled && hasCoreLink() && psychic.amount() + 0.0001f < psychicCapacity;
        }

        @Override
        public boolean acceptsPsychicLinks() {
            return true;
        }

        @Override
        public float getEnergyNeed() {
            return hasCoreLink() ? Math.max(psychicCapacity - psychic.amount(), 0f) : 0f;
        }

        @Override
        public float handleEnergy(float amount) {
            return hasCoreLink() ? psychic.add(amount, psychicCapacity) : 0f;
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.itemCapacity) return remoteCore != null ? remoteCore.sense(sensor) : 0;
            return super.sense(sensor);
        }

        @Override
        public byte version() {
            return 3;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            psychic.write(write);
            TypeIO.writeBuilding(write, remoteCore);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            psychic.read(read);
            Building build = TypeIO.readBuilding(read);
            remoteCore = build instanceof CoreBuild core ? core : null;
            if (remoteCore != null) {
                cachedCorePos = remoteCore.pos();
            }

            linkedCore = null;
            items = remoteCore != null ? remoteCore.items : emptyItems;
            if (remoteCore == null) {
                emptyItems.clear();
            }
        }

        protected boolean hasCoreLink() {
            return validCore(remoteCore);
        }

        protected boolean validCore(@Nullable CoreBuild core) {
            return core != null && core.isAdded() && core.team == team
                    && Math.abs(core.tileX() - tile.x) <= linkRange
                    && Math.abs(core.tileY() - tile.y) <= linkRange;
        }

        protected boolean hasOperationalCore() {
            return hasCoreLink() && psychic.amount() > 0.0001f;
        }

        protected boolean canAccessCoreItems() {
            return hasCoreLink() && psychic.amount() > 0.0001f;
        }

        protected void releaseCoreCapacity() {
            if (remoteCore != null && capacityApplied) {
                remoteCore.storageCapacity = Math.max(remoteCore.storageCapacity - coreCapacityBonus, 0);
                capacityApplied = false;
            }
        }

        protected void updateLinkVisuals() {
            boolean linked = hasCoreLink();
            linkWarmup = Mathf.approachDelta(linkWarmup, linked ? 1f : 0f, linked ? connectWarmupSpeed : connectFadeSpeed);
        }

        protected void drawCoreLink() {
            if (linkWarmup <= 0.001f) return;

            float coreX, coreY;
            int coreSize;

            if (remoteCore != null) {
                coreX = remoteCore.x;
                coreY = remoteCore.y;
                coreSize = remoteCore.block.size;
                lastCoreX = coreX;
                lastCoreY = coreY;
                lastCoreSize = coreSize;
            } else {
                coreX = lastCoreX;
                coreY = lastCoreY;
                coreSize = lastCoreSize;
            }

            if (coreSize <= 0) return;

            float angle = Angles.angle(x, y, coreX, coreY);
            float fromLen = 0;
            float toLen = coreSize * tilesize * 0.45f;
            float x1 = x + Angles.trnsx(angle, fromLen);
            float y1 = y + Angles.trnsy(angle, fromLen);
            float x2 = coreX - Angles.trnsx(angle, toLen);
            float y2 = coreY - Angles.trnsy(angle, toLen);
            float pulse = 1f + Mathf.absin(beamPulseScl, beamPulseMag) * linkWarmup * 0.35f;

            Tmp.v1.set(x1, y1).lerp(x2, y2, linkWarmup);

            Draw.z(Layer.effect);
            Draw.color(team.color);
            Draw.alpha(linkWarmup * 0.5f);
            Drawn.basicLaser(x1, y1, Tmp.v1.x, Tmp.v1.y, beamWidth * 2 * pulse * (0.85f + linkWarmup * 0.35f), 1.1f);
            Draw.alpha(linkWarmup * 0.7f);
            Drawn.basicLaser(x1, y1, Tmp.v1.x, Tmp.v1.y, beamWidth * pulse * (0.85f + linkWarmup * 0.35f), 1f);
            Draw.reset();

            drawBeamParticles(x1, y1, x2, y2, angle);
        }

        protected void drawBeamParticles(float x1, float y1, float x2, float y2, float angle) {
            if (!WHSettings.effectEnabled() || linkWarmup <= 0.001f) return;

            float dst = Mathf.dst(x1, y1, x2, y2);
            if (dst <= beamParticleLength) return;

            int particles = Mathf.clamp(Mathf.ceil(beamParticles * linkWarmup), 1, beamParticles);
            Rand rand = new Rand((long) id * 7919L + cachedCorePos);

            Draw.z(Layer.effect);
            for (int i = 0; i < particles; i++) {
                float seed = rand.random(beamParticleLife);
                float side = rand.random(-1f, 1f) * block.size * tilesize * 0.18f;
                float fin = ((Time.time * (1f + i * 0.11f) + seed) % beamParticleLife) / beamParticleLife;
                float slope = Mathf.slope(fin);
                float cx, cy;
                float travel = Math.min(fin / Math.max(linkWarmup, 0.0001f), 1f);
                float len = beamParticleLength * (0.45f + slope * 0.9f) * linkWarmup;
                cx = Mathf.lerp(x1, x2, travel) + Angles.trnsx(angle + 90f, side);
                cy = Mathf.lerp(y1, y2, travel) + Angles.trnsy(angle + 90f, side);
                float hx = Angles.trnsx(angle, len / 2f);
                float hy = Angles.trnsy(angle, len / 2f);

                Draw.color(team.color);
                Draw.alpha((0.25f + slope * 0.6f) * linkWarmup);
                Lines.stroke(beamParticleStroke * (0.55f + slope * 0.65f) * linkWarmup);
                Lines.line(cx - hx, cy - hy, cx + hx, cy + hy, false);
            }
            Draw.reset();
            Lines.stroke(1f);
        }
    }
}
