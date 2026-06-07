package wh.entities.world.blocks.production;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.FrameBuffer;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.*;
import wh.util.WorldDef;

import static arc.Core.atlas;
import static arc.Core.graphics;
import static arc.graphics.g2d.Draw.color;
import static arc.math.Mathf.clamp;
import static arc.math.Mathf.rand;
import static mindustry.Vars.*;

public class Quarry extends Block {
    public TextureRegion sideRegion1;
    public TextureRegion sideRegion2;
    public TextureRegion locatorRegion;
    public TextureRegion armRegion;
    public TextureRegion drillRegion;
    public TextureRegion frameRegion;
    public TextureRegion topRegion;

    public float mineTime;
    public float liquidBoostIntensity;
    public int areaSize;
    public boolean drawDrill = true;
    public Color areaColor = Pal.accent;
    public Color boostColor = Color.sky.cpy().mul(0.87f);
    public Interp deployInterp;
    public Interp deployInterpInverse;

    public float drillMoveSpeed;
    public float deploySpeed;
    public float drillMargin = 20f;
    public float elevation = 8f;

    public int tier;
    public float hardnessDrillMultiplier = 50f;
    public float rescanInterval = 30;

    protected float fulls = areaSize * tilesize / 2f;

    /**
     * Multipliers of drill speed for each item. Defaults to 1.
     */
    public ObjectFloatMap<Item> drillMultipliers = new ObjectFloatMap<>();

    private FrameBuffer shadow = null;


    Effect quarryDrillEffect = new Effect(60, e -> {
        color(e.color, Color.gray, e.fin() * 3 >= 1 ? 1 : e.fin() * 3);
        Draw.rect(atlas.find("large-orb"), e.x + Angles.trnsx(e.rotation, e.fin(Interp.pow5Out) * 20), e.y + Angles.trnsy(e.rotation, e.fin(Interp.pow5Out) * 20), e.foutpow() * 8f + 2, e.foutpow() * 8f + 2);
    });
    public Effect drillEffect = new MultiEffect(quarryDrillEffect, Fx.mine);

    public Quarry(String name) {
        super(name);
        update = true;
        solid = true;
        rotate = true;
        flags = EnumSet.of(BlockFlag.drill);
        group = BlockGroup.drills;
        hasItems = true;
        hasLiquids = true;
        acceptsItems = true;
        liquidCapacity = 5f;
        hasPower = true;
        //drills work in space I guess
        envEnabled |= Env.space;
        updateInUnits = false;
        quickRotate = false;
        ambientSoundVolume = 0.05f;
        ambientSound = Sounds.loopMineBeam;
    }

    @Override
    public void init() {
        updateClipRadius((size + areaSize + 1) * 8);
        super.init();
        fulls = areaSize * tilesize / 2f;
    }

    @Override
    public void load() {
        super.load();
        sideRegion1 = Core.atlas.find(name + "-side1");
        sideRegion2 = Core.atlas.find(name + "-side2");
        locatorRegion = Core.atlas.find(name + "-locator");
        armRegion = Core.atlas.find(name + "-arm");
        drillRegion = Core.atlas.find(name + "-drill");
        frameRegion = Core.atlas.find(name + "-frame");
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region, topRegion};
    }

    public Rect getRect(Rect rect, float x, float y, int rotation) {
        rect.setCentered(x, y, areaSize * tilesize);
        float len = tilesize * (areaSize + size) / 2f;

        rect.x += Geometry.d4x(rotation) * len;
        rect.y += Geometry.d4y(rotation) * len;

        return rect;
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        int mx = (int) (x + Geometry.d4x(rotation) * (areaSize / 2f - (areaSize % 2) / 2f - sizeOffset * 2) - areaSize / 2f + (areaSize % 2));
        int my = (int) (y + Geometry.d4y(rotation) * (areaSize / 2f - (areaSize % 2) / 2f - sizeOffset * 2) - areaSize / 2f + (areaSize % 2));

        Seq<Tile> tiles = WorldDef.getAreaTile(new Vec2(mx - 1, my - 1), areaSize, areaSize);

        Seq<Item> items = getDropList(tiles);

        Seq<Item> itemList = WorldDef.listItem(items);

        drawDrillText(itemList, items, x, y, valid);
        x *= tilesize;
        y *= tilesize;
        x += (int) offset;
        y += (int) offset;

        Rect rect = getRect(Tmp.r1, x, y, rotation);

        Drawf.dashRect(valid ? Pal.accent : Pal.remove, rect);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {


        int mx = (int) (tile.x + Geometry.d4x(rotation) * (areaSize / 2f - (areaSize % 2) / 2f - sizeOffset * 2) - areaSize / 2f + (areaSize % 2));
        int my = (int) (tile.y + Geometry.d4y(rotation) * (areaSize / 2f - (areaSize % 2) / 2f - sizeOffset * 2) - areaSize / 2f + (areaSize % 2));

        Seq<Tile> tiles = WorldDef.getAreaTile(new Vec2(mx - 1, my - 1), areaSize, areaSize);

        Seq<Item> items = getDropList(tiles);

        Seq<Item> itemList = WorldDef.listItem(items);

        Rect rect = getRect(Tmp.r1, tile.worldx() + offset, tile.worldy() + offset, rotation);


        boolean Overlap = indexer.eachBlock(team, rect, b -> {
            if (!(b instanceof QuarryBuild)) return false;
            Rect existingRect = getRect(Tmp.r2, b.x, b.y, b.rotation);
            return existingRect.overlaps(rect);
        }, b -> {
        });

        boolean WorkingPlaceOverlap = indexer.getFlagged(team, BlockFlag.drill).contains(b -> {
            if (b.block instanceof Quarry) {
                Rect otherRect = getRect(Tmp.r2, b.x, b.y, b.rotation);
                return otherRect.overlaps(rect) && !otherRect.equals(rect);
            }
            return false;
        });

        return !itemList.isEmpty() && !WorkingPlaceOverlap && !Overlap;
    }

    public void drawText(Item item, int count, int x, int y, boolean valid, int layer) {
        if (item != null) {
            float width = drawPlaceText(Core.bundle.formatFloat("bar.drillspeed", count / (getDrillTime(item) / 60f), 2), x, y + layer, valid);
            float dx = x * tilesize + offset - width / 2f - 4f, dy = y * tilesize + offset + size * tilesize / 2f + 5 + layer * 8, s = iconSmall / 4f;
            Draw.mixcol(Color.darkGray, 1f);
            Draw.rect(item.fullIcon, dx, dy - 1, s, s);
            Draw.reset();
            Draw.rect(item.fullIcon, dx, dy, s, s);
        }
    }

    public int countOre(Item target, Seq<Item> array) {
        int count = 0;
        for (Item item : array) {
            if (item == target) {
                count += 1;
            }
        }
        return count;
    }

    public void drawDrillText(Seq<Item> array, Seq<Item> all, int x, int y, boolean valid) {
        int layer = 0;
        int count;
        for (Item item : array) {
            count = countOre(item, all);
            drawText(item, count, x, y, valid, layer);
            if (item != null) {
                layer += 1;
            }
        }
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(plan.rotation >= 2 ? sideRegion2 : sideRegion1, plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.rect(topRegion, plan.drawx(), plan.drawy(),plan.rotation * 90);
    }

    public Item getDrop(Tile tile) {
        return tile.overlay().itemDrop;
    }

    public Seq<Item> getDropList(Seq<Tile> tiles) {
        Seq<Item> items = new Seq<>();
        tiles.each(tile -> {
            if (tile != null && tile.block() == Blocks.air && getDrop(tile) != null && getDrop(tile).hardness <= tier)
                items.add(getDrop(tile));
            else items.add((Item) null);
        });
        return items;
    }

    @Override
    public boolean rotatedOutput(int x, int y) {
        return false;
    }

    public Vec2 getMiningArea(float x, float y, int rotation) {
        float len = tilesize * (areaSize + size) / 2f;
        float mineX = x + Geometry.d4x(rotation) * len, mineY = y + Geometry.d4y(rotation) * len;
        return Tmp.v4.set(mineX, mineY);
    }

    public float getDrillTime(Item item) {
        return (mineTime + hardnessDrillMultiplier * item.hardness) / drillMultipliers.get(item, 1f);
    }

    public float getItemCycleMultiplier(Item item) {
        float drillTime = getDrillTime(item);
        if (drillTime <= 0.0001f) return 0f;
        return mineTime / drillTime;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.drillSpeed, 60f / mineTime * areaSize * areaSize / 4, StatUnit.itemsSecond);
        if (liquidBoostIntensity != 1) {
            stats.add(Stat.boostEffect, liquidBoostIntensity, StatUnit.timesSpeed);
        }
    }

    @Override
    public void setBars() {
        super.setBars();
    }


    public class QuarryBuild extends Building {
        public int lastChange = -2;
        public float progress;
        public float deployProgress;
        //float mx = 0, mxS = 0, mxP = 0, mxM = 0, mxR = 0, my = 0, myS = 0, myP = 0, myM = 0, myR = 0, mN = -2 * fulls, mL = -2 * fulls;
        public float warmup;
        Color fullColor;
        public float boostWarmup;

        public boolean deploying;
        public boolean empty;
        Seq<Tile> tiles = new Seq<>();
        Seq<Item> itemsArray = new Seq<>();

        Seq<Item> itemList = new Seq<>();
        public final Interval rescan = new Interval(1);

        public float mx = 0, my = 0, drillX = 0, drillY = 0, targetX = 0, targetY = 0;

        public float deployProgress1, deployProgress2, deployProgress3, deployProgress4;

        Interp inp;

        @Override
        public void updateTile() {
            inp = deploying ? deployInterp : deployInterpInverse;

            boostWarmup = Mathf.lerpDelta(boostWarmup, optionalEfficiency, 0.1f);

            warmup = Mathf.lerpDelta(warmup, efficiency, 0.1f);

            Vec2 mineCentre = getMiningArea(x, y, rotation);
            mx = mineCentre.x;
            my = mineCentre.y;

            Vec2 MiningPos = new Vec2(World.conv(mx - fulls), World.conv(my - fulls));

            if (tiles.isEmpty() || lastChange != Vars.world.tileChanges || rescan.get(0, rescanInterval)) {
                refreshMiningState(MiningPos);
                lastChange = Vars.world.tileChanges;
            }

            fullColor = Tmp.c1.set(areaColor).lerp(boostColor, boostWarmup);
            float speed = 0;
            if (items.total() < itemCapacity && efficiency > 0) {
                speed = Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency) * efficiency;
            }
            if (!empty && progress >= mineTime && items.total() < itemCapacity && deployProgress >= 4) {
                progress = 0;
                Item tileItem;
                for (int ix = 0; ix < areaSize; ix++) {
                    for (int iy = 0; iy < areaSize; iy++) {
                        float dx = ix * tilesize + mx - fulls + tilesize / 2f;
                        float dy = iy * tilesize + my - fulls + tilesize / 2f;
                        tileItem = tileAt(ix, iy);
                        if (tileItem != null) {
                            if (items.total() < itemCapacity) {
                                float mul = getItemCycleMultiplier(tileItem);
                                int extra = Mathf.floor(mul);
                                float frac = mul - extra;

                                for (int n = 0; n < extra; n++) {
                                    if (items.total() < itemCapacity) {
                                        produceOre(tileItem, dx, dy);
                                    }
                                }

                                if (items.total() < itemCapacity && Mathf.chance(frac * 100f)) {
                                    produceOre(tileItem, dx, dy);
                                }
                            }
                        }
                    }
                }
            }
            if (deployProgress >= 4 && Mathf.random(1) > 0.3 && efficiency > 0 && items.total() < itemCapacity) {
                drillEffect.at(drillX + mx, drillY + my, rand.random(0, 360f), fullColor);
            }
            if (!empty) {

                if (efficiency > 0) {
                    if (deployProgress <= 4) {
                        this.deployProgress += delta() * deploySpeed * efficiency;
                        deploying = true;
                    } else progress += delta() * speed;
                } else if (deployProgress > 0 && Mathf.zero(drillX, 0.01f) && Mathf.zero(drillY, 0.01f) && items.total() < itemCapacity) {
                    this.deployProgress -= delta() * deploySpeed;
                    deploying = false;
                }

                if (items.total() >= itemCapacity || efficiency <= 0) {
                    drillX = Mathf.lerpDelta(drillX, 0, 0.07f);
                    drillY = Mathf.lerpDelta(drillY, 0, 0.07f);
                    //move to center
                    if(Mathf.zero(drillX, 0.01f) && Mathf.zero(drillY, 0.01f)){
                        this.deployProgress -= delta() * deploySpeed;
                        deploying = false;
                    }
                } else {
                    if (Mathf.equal(drillX, targetX) && Mathf.equal(drillY, targetY)) {
                        targetX = Mathf.randomSeed(id + (long) Time.time, (float) -areaSize / 2 * tilesize + drillMargin, (float) areaSize / 2 * tilesize - drillMargin);
                        targetY = Mathf.randomSeed(id / 2 + (long) Time.time, (float) -areaSize / 2 * tilesize + drillMargin, (float) areaSize / 2 * tilesize - drillMargin);
                    }//随机位置
                    if (deployProgress >= 4 && efficiency > 0) {
                        drillX = Mathf.approachDelta(drillX, targetX, drillMoveSpeed);
                        drillY = Mathf.approachDelta(drillY, targetY, drillMoveSpeed);
                    }
                }

            } else this.deployProgress = 0;

            dumpOutputs();
        }

        public void dumpOutputs() {
            if (!empty && timer(timerDump, dumpTime / timeScale)) {
                for (Item output : itemList) {
                    dump(output);
                }
            }
        }

        private void produceOre(Item tileItem, float dx, float dy) {
            Fx.itemTransfer.at(dx + Mathf.range(1f), dy + Mathf.range(1f), 0, tileItem.color, new Vec2(drillX + mx, drillY + my));
            Fx.itemTransfer.at(drillX + Mathf.range(1f) + mx, drillY + Mathf.range(1f) + my, 0, tileItem.color, new Vec2(x, y));
            offload(tileItem);
        }

        public void drawDrill(float x, float y, float mx, float my, float layer) {
            Draw.z(layer - 1f);
            Lines.stroke(armRegion.height / 4f);
            if (deployProgress < 2) {
                Draw.alpha(Mathf.clamp(deployProgress, 0, 1));
                for (Point2 p : Geometry.d8edge) {
                    Draw.rect(locatorRegion,
                            x + deployProgress1 * (mx - x + fulls * p.x),
                            y + deployProgress1 * (my - y + fulls * p.y)
                    );
                }
                //Draw arm
                Draw.z(layer - 1.15f);
                if (deployProgress > 2) {
                    for (int i = 0; i < Geometry.d8edge.length; i++) {
                        Point2 p1 = Geometry.d8edge(i);
                        Point2 p2 = Geometry.d8edge(i + 1);
                        Lines.line(armRegion,
                                mx + fulls * p1.x, my + fulls * p1.y,
                                mx + fulls * p2.x, my + fulls * p2.y,
                                false
                        );
                    }
                } else if (deployProgress > 1) {
                    for (Point2 p : Geometry.d8edge) {
                        for (int d : Mathf.zeroOne) {
                            Lines.line(armRegion,
                                    mx + fulls * p.x,
                                    my + fulls * p.y,
                                    deployProgress2 * -(-d + 1) * fulls * p.x + mx + fulls * p.x,
                                    deployProgress2 * -d * fulls * p.y + my + fulls * p.y,
                                    false
                            );
                        }
                    }
                }
                //draw across arm
                Draw.z(layer - 1.25f);
            } else {
                Draw.rect(frameRegion, mx, my);
            }
            if (deployProgress > 3) {
                /*Lines.line(armRegion,
                        drillX * 0 + mx + fulls * 1,
                        drillY * 1 + my + fulls * 0,
                        drillX * 0 + mx + fulls * 1,
                        drillY * 1 + my + fulls * 0,
                        false
                );*/
                Lines.line(armRegion,
                        mx + drillX, my + fulls,
                        mx + drillX, my - fulls,
                        false);
                Lines.line(armRegion,
                        mx + fulls, my + drillY,
                        mx - fulls, my + drillY,
                        false);
            } else if (deployProgress > 2) {
                for (int d : Mathf.zeroOne) {
                    for (int i : Mathf.signs) {
                        Lines.line(armRegion,
                                drillX * d + mx + fulls * i * (-d + 1),
                                drillY * (-d + 1) + my + fulls * i * d,
                                drillX * d + mx + i * (-d + 1) * (-deployProgress3 * fulls) + fulls * i * (-d + 1),
                                drillY * (-d + 1) + my + i * d * (-deployProgress3 * fulls) + fulls * i * d,
                                false
                        );
                    }
                }
            }
            if (deployProgress > 1) {
                Draw.alpha(Mathf.clamp(deployProgress, 0, 1));
                Draw.rect(drillRegion, drillX + mx, drillY + my);
            } else {
                Draw.alpha(Mathf.clamp(deployProgress, 0, 1));
                Draw.rect(drillRegion, x + deployProgress1 * (mx - x), y + deployProgress1 * (my - y));
            }

            Draw.reset();
        }

        @Override
        public void draw() {
            Draw.rect(region, x, y);
            Draw.rect(topRegion, x, y, rotdeg());
            Draw.rect(rotation >= 2 ? sideRegion2 : sideRegion1, x, y, rotdeg());

            if (!drawDrill) return;
            if(inp == null){
                inp = deployInterp != null ? deployInterp : Interp.linear;
            }
            //(0-1), (1-2), (2-3), (3-4)
            if (deployProgress >= 0 && deployProgress <= 1) deployProgress1 = inp.apply(clamp(deployProgress - 0));
            else if (deployProgress >= 1 && deployProgress <= 2) deployProgress2 = inp.apply(clamp(deployProgress - 1));
            else if (deployProgress >= 2 && deployProgress <= 3) deployProgress3 = inp.apply(clamp(deployProgress - 2));
            else if (deployProgress >= 3 && deployProgress <= 4) deployProgress4 = inp.apply(clamp(deployProgress - 3));
            drawDrill(x, y, mx, my, Layer.flyingUnitLow - 0.05f);
            Draw.draw(Layer.floor + 2, () -> {

                if (shadow == null) {
                    shadow = new FrameBuffer(graphics.getWidth(), graphics.getHeight());
                }
                shadow.resize(graphics.getWidth(), graphics.getHeight());

                shadow.begin(Color.clear);
                drawDrill(x - elevation, y - elevation, mx - elevation, my - elevation, Layer.floor + 2);
                shadow.end();

                Draw.z(Layer.blockOver);
                Draw.color(Pal.shadow, 0.25f);
                Draw.rect(Draw.wrap(shadow.getTexture()), Core.camera.position, Core.camera.width, -Core.camera.height);
                Draw.color();
            });

            Draw.reset();
            //draw ore stroke
            Draw.z(Layer.blockOver);

            Lines.stroke(0.6f);
            Item tileItem;
            for (int ix = 0; ix < areaSize; ix++) {
                for (int iy = 0; iy < areaSize; iy++) {
                    float dx = ix * 8 + mx - fulls + 4;
                    float dy = iy * 8 + my - fulls + 4;
                    tileItem = tileAt(ix, iy);
                    if (tileItem != null) {
                        for (int i = 0; i <= 1; i++) {

                            color(tileItem.color);

                            Drawf.light(dx, dy, 5, tileItem.color, 50);
                            float rot = i * 360f / 2 - Time.time * 1.1f;
                            Draw.alpha(warmup);
                            Lines.arc(dx, dy, 4f, 0.3f, rot);
                        }
                    }
                }
            }
        }

        private Item tileAt(int ix, int iy){
            int index = ix * areaSize + iy;
            if(index < 0 || index >= itemsArray.size) return null;
            return itemsArray.get(index);
        }

        private void refreshMiningState(Vec2 miningPos) {
            tiles = WorldDef.getAreaTile(miningPos, areaSize, areaSize);
            itemsArray = getDropList(tiles);
            itemList = WorldDef.listItem(itemsArray);
            empty = itemList.isEmpty();
        }

        private Seq<Item> getTopOres() {
            Seq<Item> sorted = itemList.copy();
            sorted.sort((a, b) -> {
                int countCompare = Integer.compare(countOre(b, itemsArray), countOre(a, itemsArray));
                if (countCompare != 0) return countCompare;
                return Integer.compare(a.id, b.id);
            });
            return sorted;
        }

        public Item getTopOre(int index) {
            Seq<Item> sorted = getTopOres();
            if (index < 0 || index >= sorted.size || index >= 3) return null;
            return sorted.get(index);
        }

        public float getTopOreSpeedPerSecond(int index) {
            Item item = getTopOre(index);
            if (item == null) return 0f;
            int count = countOre(item, itemsArray);
            float drillTime = getDrillTime(item);
            if (count <= 0 || drillTime <= 0.0001f) return 0f;
            float speedScale = Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency) * efficiency;
            return (60f / drillTime) * count * speedScale;
        }

        public String getTopOreSpeedText(int index) {
            Item item = getTopOre(index);
            if (item == null) return "-";
            return item.localizedName + " " + Strings.fixed(getTopOreSpeedPerSecond(index), 2) + "/s";
        }

        public float getTopOreSpeedProgress(int index) {
            float top = getTopOreSpeedPerSecond(0);
            if (top <= 0.0001f) return 0f;
            return Mathf.clamp(getTopOreSpeedPerSecond(index) / top);
        }

        @Override
        public void displayBars(Table table) {
            int[] lastCount = {Math.min(getTopOres().size, 3)};
            rebuildBars(table);
            table.update(() -> {
                int count = Math.min(getTopOres().size, 3);
                if (count != lastCount[0]) {
                    rebuildBars(table);
                    lastCount[0] = count;
                }
            });
        }

        private void rebuildBars(Table table) {
            table.clear();

            for (var bar : block.listBars()) {
                Bar base = bar.get(this);
                if (base != null) {
                    table.add(base).growX();
                    table.row();
                }
            }

            int maxBars = Math.min(getTopOres().size, 3);
            for (int i = 0; i < maxBars; i++) {
                final int index = i;
                Item item = getTopOre(index);
                if (item == null) continue;
                table.add(new Bar(
                        () -> getTopOreSpeedText(index),
                        () -> {
                            Item current = getTopOre(index);
                            return current == null ? Pal.gray : current.color;
                        },
                        () -> getTopOreSpeedProgress(index)
                )).growX();
                table.row();
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            Drawf.dashRect(Pal.accent, getRect(Tmp.r1, x, y, rotation));
        }

        @Override
        public boolean shouldConsume() {
            return items.total() < itemCapacity && enabled && !empty;
        }

        @Override
        public byte version() {
            return 2;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(deployProgress);
            write.f(progress);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 2) {
                deployProgress = read.f();
            }
            if (revision >= 1) {
                progress = read.f();
                warmup = read.f();
            }
        }
    }
}

