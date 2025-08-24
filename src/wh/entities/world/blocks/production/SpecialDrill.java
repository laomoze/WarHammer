package wh.entities.world.blocks.production;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.scene.ui.Image;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class SpecialDrill extends Block {

    public int mineSize = 5;
    public int mineOffset = 0;
    public float hardnessDrillMultiplier = 50f;

    protected final ObjectIntMap<Item> oreCount = new ObjectIntMap<>();
    protected final Seq<Item> itemArray = new Seq<>();

    /**
     * Maximum tier of blocks this drill can mine.
     */
    public int tier;
    /**
     * Base time to drill one ore, in frames.
     */
    public float drillTime = 300;
    /**
     * How many times faster the drill will progress when boosted by liquid.
     */
    public float liquidBoostIntensity = 1.6f;
    /**
     * Speed at which the drill speeds up.
     */
    public float warmupSpeed = 0.015f;
    /**
     * Special exemption item that this drill can't mine.
     */
    public @Nullable Item blockedItem;

    //return variables for countOre
    protected @Nullable Item returnItem;
    protected int returnCount;

    /**
     * Whether to draw the item this drill is mining.
     */
    public boolean drawMineItem = true;
    /**
     * Effect played when an item is produced. This is colored.
     */
    public Effect drillEffect = Fx.mine;
    /**
     * Drill effect randomness. Block size by default.
     */
    public float drillEffectRnd = -1f;
    /**
     * Chance of displaying the effect. Useful for extremely fast drills.
     */
    public float drillEffectChance = 1f;
    /**
     * Speed the drill bit rotates at.
     */
    public float rotateSpeed = 1.5f;
    /**
     * Effect randomly played while drilling.
     */
    public Effect updateEffect = Fx.pulverizeSmall;
    /**
     * Chance the update effect will appear.
     */
    public float updateEffectChance = 0.005f;

    /**
     * Multipliers of drill speed for each item. Defaults to 1.
     */
    public ObjectFloatMap<Item> drillMultipliers = new ObjectFloatMap<>();

    public @Nullable Seq<Item> allowedItems;

    public boolean drawRim = false;
    public boolean drawSpinSprite = true;
    public Color heatColor = Color.valueOf("ff5512");
    public TextureRegion rotatorRegion, topRegion;

    public SpecialDrill(String name) {
        super(name);
        update = true;
        solid = true;
        group = BlockGroup.drills;
        hasLiquids = true;
        liquidCapacity = 5f;
        hasItems = true;
        ambientSound = Sounds.drill;
        ambientSoundVolume = 0.018f;
    }

    @Override
    public void init() {
        super.init();
        if (drillEffectRnd < 0) drillEffectRnd = size;
    }

    @Override
    public void load() {
        super.load();
        //public TextureRegion rimRegion, rotatorRegion, topRegion;

        rotatorRegion = Core.atlas.find(name + "-rotator");
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list) {
        if (!plan.worldContext) return;
        Tile tile = plan.tile();
        if (tile == null) return;
        if (returnItem == null || !drawMineItem) return;
        countOre(tile);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("drillspeed", (SDDrillBuild e) ->
                new Bar(() -> Core.bundle.format("bar.drillspeed", Strings.fixed(e.lastDrillSpeed * 60 * e.timeScale(), 2)), () -> Pal.ammo, () -> e.warmup));
    }

    public Item getDrop(Tile tile) {
        return tile.drop();
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {

        if (isMultiblock()) {
            for (Tile other : OreTiles(tile, mineSize)) {
                if (canMine(other)) {
                    return true;
                }
            }
            return false;
        } else {
            return canMine(tile);
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashRect(valid ? Pal.accent : Pal.remove, getRect(Tmp.r1, x * tilesize + 4, y * tilesize + 4));

        Tile tile = world.tile(x, y);
        if (tile == null) return;

        countOre(tile);

        if (returnItem != null) {
            if (allowedItems != null && !allowedItems.contains(returnItem)) {
                drawPlaceText(Core.bundle.get("bar.drillnotallowed"), x, y, false);
                return;
            }
            float width = drawPlaceText(Core.bundle.formatFloat("bar.drillspeed",  mineSize*mineSize /( getDrillTime(returnItem)/60f), 2), x, y, valid);
            float dx = x * tilesize + offset - width / 2f, dy = y * tilesize + offset + size * tilesize / 2f, s = iconSmall / 4f;
            Draw.mixcol(Color.darkGray, 1f);
            Draw.rect(returnItem.fullIcon, dx, dy - 1, s, s);
            Draw.reset();
            Draw.rect(returnItem.fullIcon, dx, dy, s, s);
        } else {
            Tile to = tile.getLinkedTilesAs(this, tempTiles).find(t -> t.drop() != null && (t.drop().hardness > tier || t.drop() == blockedItem));
            Item item = to == null ? null : to.drop();
            if (item != null) {
                drawPlaceText(Core.bundle.get("bar.drilltierreq"), x, y, valid);
            }
        }
    }

    public float getDrillTime(Item item) {
        return (drillTime + hardnessDrillMultiplier * item.hardness) / drillMultipliers.get(item, 1f);
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.add(Stat.output, table -> {
            table.row();
            table.add(Core.bundle.format("stat-designated-minerals")).left().top();
            for (Item item : allowedItems) {
                table.add(item.localizedName).color(item.color).padLeft(5).padRight(5);
                Image itemImage = new Image(item.fullIcon);
                table.add(itemImage).size(32);
                table.draw();

            }
        });

        stats.add(Stat.drillTier, StatValues.drillables(drillTime, hardnessDrillMultiplier, size, drillMultipliers, b -> b instanceof Floor f && !f.wallOre && f.itemDrop != null &&
                f.itemDrop.hardness <= tier && f.itemDrop != blockedItem && allowedItems.contains(f.itemDrop) && (indexer.isBlockPresent(f) || state.isMenu())));

        stats.add(Stat.drillSpeed,   mineSize * mineSize/(60f / drillTime), StatUnit.itemsSecond);

        if (liquidBoostIntensity != 1 && findConsumer(f -> f instanceof ConsumeLiquidBase) instanceof ConsumeLiquidBase consBase) {
            stats.remove(Stat.booster);
            stats.add(Stat.booster,
                    StatValues.speedBoosters("{0}" + StatUnit.timesSpeed.localized(),
                            consBase.amount,
                            liquidBoostIntensity * liquidBoostIntensity, false, this::consumesLiquid)
            );
        }
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region, rotatorRegion, topRegion};
    }

    public Seq<Tile> OreTiles(Tile tile, int oreSize) {
        Seq<Tile> tiles = new Seq<>();
        for (int dx = 0; dx < oreSize; dx++) {
            for (int dy = 0; dy < oreSize; dy++) {
                Tile other = world.tile(tile.x + dx + mineOffset, tile.y + dy + mineOffset);
                if (other != null) {
                    Item drops = other.drop();
                    if (drops != null && drops.hardness <= tier && drops != blockedItem) {
                        tiles.add(other);
                    }
                }
            }
        }
        return tiles;
    }

    public Rect getRect(Rect rect, float x, float y) {
        rect.setCentered(x, y, mineSize * tilesize);
        return rect;
    }

    public boolean canMine(Tile tile) {
        if (tile == null || tile.block().isStatic()) return false;
        Item drops = tile.drop();
        return drops != null && drops.hardness <= tier && drops != blockedItem && (allowedItems == null || allowedItems.contains(drops));
    }

    protected void countOre(Tile tile) {
        returnItem = null;
        returnCount = 0;

        oreCount.clear();
        itemArray.clear();

        for (Tile other : OreTiles(tile, mineSize)) {
            if (canMine(other)) {
                oreCount.increment(getDrop(other), 0, 1);
            }
        }

        for (Item item : oreCount.keys()) {
            itemArray.add(item);
        }

        itemArray.sort((item1, item2) -> {
            int type = Boolean.compare(!item1.lowPriority, !item2.lowPriority);
            if (type != 0) return type;
            int amounts = Integer.compare(oreCount.get(item1, 0), oreCount.get(item2, 0));
            if (amounts != 0) return amounts;
            return Integer.compare(item1.id, item2.id);
        });

        if (itemArray.size == 0) {
            return;
        }

        returnItem = itemArray.peek();
        returnCount = oreCount.get(itemArray.peek(), 0);
    }


    public class SDDrillBuild extends Building {
        public float progress;
        public float warmup;
        public float timeDrilled;
        public float lastDrillSpeed;

        public int dominantItems;
        public Item dominantItem;

        @Override
        public boolean shouldConsume() {
            return items.total() < itemCapacity && enabled;
        }

        @Override
        public boolean shouldAmbientSound() {
            return efficiency > 0.01f && items.total() < itemCapacity;
        }

        @Override
        public float ambientVolume() {
            return efficiency * (size * size) / 4f;
        }

        @Override
        public void drawSelect() {
            Drawf.dashRect(Tmp.c1.set(Pal.accent), getRect(Tmp.r1, x, y));

            if (dominantItem != null) {
                float dx = x - size * tilesize / 2f, dy = y + size * tilesize / 2f, s = iconSmall / 4f;
                Draw.mixcol(Color.darkGray, 1f);
                Draw.rect(dominantItem.fullIcon, dx, dy - 1, s, s);
                Draw.reset();
                Draw.rect(dominantItem.fullIcon, dx, dy, s, s);
            }
        }

        @Override
        public void pickedUp() {
            dominantItem = null;
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();

            countOre(tile);
            dominantItem = returnItem;
            dominantItems = returnCount;
        }

        @Override
        public Object senseObject(LAccess sensor) {
            if (sensor == LAccess.firstItem) return dominantItem;
            return super.senseObject(sensor);
        }

        @Override
        public void updateTile() {

            if (dominantItem == null) {
                return;
            }

            if (timer(timerDump, dumpTime)) {
                dump(dominantItem != null && items.has(dominantItem) ? dominantItem : null);
            }

            timeDrilled += warmup * delta();

            float delay = getDrillTime(dominantItem);

            if (items.total() <= itemCapacity - dominantItems && dominantItems > 0 && efficiency > 0) {
                float speed = efficiency * Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency);
                lastDrillSpeed = (speed * dominantItems * warmup) / delay;
                warmup = Mathf.approachDelta(warmup, speed, warmupSpeed);
                progress += delta() * speed * warmup;

                if (Mathf.chanceDelta(updateEffectChance * warmup))
                    updateEffect.at(x, y);

            } else {
                lastDrillSpeed = 0f;
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }


            if (dominantItems > 0 && progress >= delay && items.total() < itemCapacity) {
                for (int i = 0; i < dominantItems; i++) {
                    offload(dominantItem);
                }
                progress %= delay;

                if (wasVisible && Mathf.chanceDelta(drillEffectChance * warmup))
                    drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
            }

        }

        @Override
        public float progress() {
            return dominantItem == null ? 0f : Mathf.clamp(progress / getDrillTime(dominantItem));
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress && dominantItem != null) return progress;
            return super.sense(sensor);
        }

        @Override
        public void drawCracks() {
        }

        public void drawDefaultCracks() {
            super.drawCracks();
        }

        @Override
        public void draw() {
            float s = 0.3f;
            float ts = 0.6f;

            Draw.rect(region, x, y);
            Draw.z(Layer.blockCracks);
            drawDefaultCracks();

            Draw.z(Layer.blockAfterCracks);
            if (drawRim) {
                Draw.color(heatColor);
                Draw.alpha(warmup * ts * (1f - s + Mathf.absin(Time.time, 3f, s)));
                Draw.blend(Blending.additive);
                Lines.stroke(1.4f*(1-Mathf.absin(Time.time, 3f, s)));
                Lines.poly(x, y, 4, 8, -timeDrilled * rotateSpeed / 8);
                Draw.blend();
                Draw.color();
            }

            if (drawSpinSprite) {
                Drawf.spinSprite(rotatorRegion, x, y, timeDrilled * rotateSpeed);
            } else {
                Draw.rect(rotatorRegion, x, y, timeDrilled * rotateSpeed);
            }

            Draw.rect(topRegion, x, y);
        }

        @Override
        public byte version() {
            return 1;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 1) {
                progress = read.f();
                warmup = read.f();
            }
        }
    }

}
