package wh.entities.world.blocks.production;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g2d.TextureAtlas.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import wh.entities.world.blocks.production.MultiCrafter.*;
import wh.ui.*;
import wh.util.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * MultiCrafter. You can freely choose to change the production formula.
 *
 * @author Eipusino
 * @see CraftPlan craftPlan type
 * @since 1.0.6
 */

public class MultiCrafterE extends Block {
    /** Formula table {@link Formula}. */
    public Seq<Formula> products = new Seq<>();
    /** If {@link MultiCrafterE#useBlockDrawer} is false, use the drawer in the recipe for the block. */
    public DrawBlock drawer = new DrawDefault();
    /** Do you want to use the {@link MultiCrafterE#drawer} inside the block itself. */
    public boolean useBlockDrawer = true;
    /**
     * Whether multiple liquid outputs require different directions,
     * please refer to {@link Formula#liquidOutputDirections} to determine the value of this parameter.
     */
    public boolean hasDoubleOutput = false;
    /** Automatically add bar to liquid. */
    public boolean autoAddBar = true;
    /** Is liquid suspension display used. */
    public boolean useLiquidTable = true;
    /** Background color of liquid suspension display. */
    public Color liquidTableBack = Pal.gray.cpy().a(0.5f);
    /** How many formulas can be displayed at most once. */
    public int maxList = 4;

    public MultiCrafterE(String name) {
        super(name);

        update = true;
        solid = true;
        hasItems = true;
        ambientSound = Sounds.machine;
        sync = true;
        ambientSoundVolume = 0.03f;
        flags = EnumSet.of(BlockFlag.factory);
        drawArrow = false;

        configurable = true;
        saveConfig = true;

        config(int[].class, (MultiCrafterBuildE tile, int[] ints) -> {
            if (ints.length != 2) return;

            tile.rotation = ints[0];

            if (products.size <= 0 || ints[1] == -1) tile.formula = null;
            tile.formula = products.get(ints[1]);
        });
    }

    @Override
    public void init() {
        for (Formula product : products) {
            product.owner = this;
            product.init();
            if (product.outputLiquids != null) {
                hasLiquids = true;
            }
            if (product.outputItems != null) {
                hasItems = true;
            }
            if (product.consPower != null) {
                hasPower = true;
                consume(new ConsumePowerDynamic(b -> b instanceof MultiCrafterBuildE tile ? tile.formulaPower() : 0));
            }
        }
        super.init();
        hasConsumers = products.any();
    }

    @Override
    public void setBars() {
        super.setBars();
        removeBar("items");
        removeBar("liquid");
        removeBar("power");
        if (consPower != null) {
            addBar("power", (MultiCrafterBuildE tile) -> new Bar(
                    () -> bundle.get("bar.hi-mti-power") + (tile.formulaPower() > 0.01f ? bundle.get("bar.hi-mti-power-need") : bundle.get("bar.hi-mti-power-no-need")),
                    () -> Pal.powerBar,
                    () -> Mathf.zero(consPower.requestedPower(tile)) && tile.power.graph.getPowerProduced() + tile.power.graph.getBatteryStored() > 0f ? 1f : tile.power.status)
            );
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.output, table -> {
            table.row();

            for (int i = 0; i < products.size; i++) {
                Formula p = products.get(i);
                int finalI = i + 1;
                table.table(Styles.grayPanel, info -> {
                    info.left().defaults().left();
                    info.add("[accent]formula[]" + finalI + ":").row();
                    Stats stat = new Stats();
                    stat.timePeriod = p.craftTime;
                    for (Consume c : p.consumers) {
                        c.display(stat);
                    }
                    if ((hasItems && itemCapacity > 0) || p.outputItems != null) {
                        stat.add(Stat.productionTime, p.craftTime / 60f, StatUnit.seconds);
                    }

                    if (p.outputItems != null) {
                        stat.add(Stat.output, StatValues.items(p.craftTime, p.outputItems));
                    }

                    if (p.outputLiquids != null) {
                        stat.add(Stat.output, StatValues.liquids(1f, p.outputLiquids));
                    }
                    info.table(st -> UIUtils.statTurnTable(stat, st)).pad(8).left();
                }).growX().left().pad(10);
                table.row();
            }
        });
    }

    @Override
    public void load() {
        super.load();
        if (useBlockDrawer) drawer.load(this);
        else for (Formula p : products) {
            p.drawer.load(this);
        }
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        if (useBlockDrawer) drawer.drawPlan(this, plan, list);
        else if (products.any()) products.get(0).drawer.drawPlan(this, plan, list);
        else super.drawPlanRegion(plan, list);
    }

    @Override
    protected TextureRegion[] icons() {
        return useBlockDrawer ? drawer.icons(this) : products.any() ? products.get(0).drawer.icons(this) : super.icons();
    }

    public class MultiCrafterBuildE extends Building {
        public @Nullable Formula formula = products.any() ? products.get(0) : null;
        public float progress;
        public float totalProgress;
        public float warmup;

        public int[] configs = {0, 0};
        public int lastRotation = -1;

        public TextureRegionDrawable[] rotationIcon = {Icon.right, Icon.up, Icon.left, Icon.down};

        @Override
        public void draw() {
            if (formula == null || useBlockDrawer) drawer.draw(this);
            else formula.drawer.draw(this);
        }

        @Override
        public void drawStatus() {
            if (block.enableDrawStatus && formula != null && formula.hasConsumers) {
                float multiplier = block.size > 1 ? 1 : 0.64f;
                float brcx = x + (float) (block.size * 8) / 2f - 8f * multiplier / 2f;
                float brcy = y - (float) (block.size * 8) / 2f + 8f * multiplier / 2f;
                Draw.z(71f);
                Draw.color(Pal.gray);
                Fill.square(brcx, brcy, 2.5f * multiplier, 45);
                Draw.color(status().color);
                Fill.square(brcx, brcy, 1.5f * multiplier, 45);
                Draw.color();
            }
        }

        public float warmupTarget() {
            return 1f;
        }

        public float formulaPower() {
            if (formula == null) return 0;
            ConsumePower consumePower = formula.consPower;
            if (consumePower == null) return 0;
            return consumePower.usage;
        }

        @Override
        public void updateTile() {
            if (lastRotation != rotation) {
                Fx.placeBlock.at(x, y, size);
                lastRotation = rotation;
            }

            if (formula == null) return;
            if (efficiency > 0) {
                progress += getProgressIncrease(formula.craftTime, formula);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), formula.warmupSpeed);

                if (formula.outputLiquids != null) {
                    float inc = getProgressIncrease(1f);
                    for (LiquidStack output : formula.outputLiquids) {
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                    }
                }

                if (wasVisible && Mathf.chanceDelta(formula.updateEffectChance)) {
                    formula.updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0f, formula.warmupSpeed);
            }

            totalProgress += warmup * Time.delta;

            if (progress >= 1f) {
                craft(formula);
            }

            dumpOutputs(formula);
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        public float getProgressIncrease(float baseTime, Formula formula) {
            if (formula.ignoreLiquidFullness) {
                return super.getProgressIncrease(baseTime);
            }

            float scaling = 1f, max = 1f;
            if (formula.outputLiquids != null) {
                max = 0f;
                for (LiquidStack s : formula.outputLiquids) {
                    float value = (liquidCapacity - liquids.get(s.liquid)) / (s.amount * edelta());
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }

            return super.getProgressIncrease(baseTime) * (formula.dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }

        public void craft(Formula formula) {
            consume();

            if (formula.outputItems != null) {
                for (ItemStack output : formula.outputItems) {
                    for (int i = 0; i < output.amount; i++) {
                        offload(output.item);
                    }
                }
            }

            if (wasVisible) {
                formula.craftEffect.at(x, y);
            }
            progress %= 1f;
        }

        public void dumpOutputs(Formula formula) {
            if (formula.outputItems != null && timer(timerDump, dumpTime / timeScale)) {
                for (ItemStack output : formula.outputItems) {
                    dump(output.item);
                }
            }

            if (formula.outputLiquids != null) {
                for (int i = 0; i < formula.outputLiquids.length; i++) {
                    int dir = formula.liquidOutputDirections.length > i ? formula.liquidOutputDirections[i] : -1;

                    dumpLiquid(formula.outputLiquids[i].liquid, 2f, dir);
                }
            }
        }

        @Override
        public boolean shouldConsume() {
            if (formula == null) return false;
            if (formula.outputItems != null) {
                for (ItemStack output : formula.outputItems) {
                    if (items.get(output.item) + output.amount > itemCapacity) {
                        return false;
                    }
                }
            }
            if (formula.outputLiquids != null && !formula.ignoreLiquidFullness) {
                boolean allFull = true;
                for (LiquidStack output : formula.outputLiquids) {
                    if (liquids.get(output.liquid) >= liquidCapacity - 0.001f) {
                        if (!formula.dumpExtraLiquid) {
                            return false;
                        }
                    } else {
                        allFull = false;
                    }
                }
                if (allFull) {
                    return false;
                }
            }
            return enabled;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            if (formula == null) return false;
            return formula.getConsumeItem(item) && items.get(item) < itemCapacity;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (formula == null) return false;
            return block.hasLiquids && formula.getConsumeLiquid(liquid);
        }

        @Override
        public void consume() {
            if (formula == null) return;
            Consume[] c = formula.consumers;

            for (Consume cons : c) {
                cons.trigger(this);
            }
        }

        public void displayConsumption(Table table) {
            if (formula == null) return;
            table.left();
            Formula[] lastFormula = {formula};
            table.table(t -> {
                table.update(() -> {
                    if (lastFormula[0] != formula) {
                        rebuild(t);
                        lastFormula[0] = formula;
                    }
                });
                rebuild(t);
            });
        }

        private void rebuild(Table table) {
            table.clear();
            Consume[] c = formula.consumers;
            for (Consume cons : c) {
                if (!cons.optional || !cons.booster) {
                    cons.build(this, table);
                }
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            if (formula == null || !useLiquidTable) return;

            if (formula.outputLiquids != null) {
                for (int i = 0; i < formula.outputLiquids.length; i++) {
                    int dir = formula.liquidOutputDirections.length > i ? formula.liquidOutputDirections[i] : -1;

                    if (dir != -1) {
                        Draw.rect(
                                formula.outputLiquids[i].liquid.fullIcon,
                                x + Geometry.d4x(dir + rotation) * (size * tilesize / 2f + 4),
                                y + Geometry.d4y(dir + rotation) * (size * tilesize / 2f + 4),
                                8f, 8f
                        );
                    }
                }
            }

            AtlasRegion canBar = atlas.find("wh-can");
            float width = 0, height = 32, pad = 4, tw = 32;
            for (int i = 0; i < formula.liquidFilter.size; i++) width += tw;
            if (formula.outputLiquids != null)
                for (int i = 0; i < formula.outputLiquids.length; i++) width += tw;
            if (width > 0) {
                Draw.color(liquidTableBack);
                float realW = width + pad * 2, realH = height + pad * 2;
                float realX = x + size / 2f * tilesize - realW / 2;
                float realY = y + size / 2f * tilesize;
                float vts = tw / 4f;
                float boPad = 1;
                Fill.rect(x, realY + realH / 2, realW, realH);
                for (Liquid l : content.liquids()) {
                    if (formula.getConsumeLiquid(l)) {
                        float ly = realY + pad;
                        Draw.color();
                        Draw.rect(l.uiIcon, realX, ly);
                        WHUtils.drawTiledFramesBar(vts, (height * liquids.get(l) / liquidCapacity), realX + vts / 2f, ly, l, 1);
                        Draw.color();
                        Draw.alpha(1);
                        Draw.rect(canBar, realX + vts, ly + height / 2f, vts + boPad, height + boPad);
                        Fonts.outline.draw(l.localizedName, realX, ly - 1, Color.white, 0.2f, false, Align.center);
                        realX += tw;
                    }
                }
                if (formula.outputLiquids != null) {
                    for (int i = 0; i < formula.outputLiquids.length; i++) {
                        LiquidStack ls = formula.outputLiquids[i];
                        float ly = realY + pad;
                        Draw.color();
                        Draw.rect(ls.liquid.uiIcon, realX, ly);
                        WHUtils.drawTiledFramesBar(vts, (height * liquids.get(ls.liquid) / liquidCapacity), realX + vts / 2f, ly, ls.liquid, 1);
                        Draw.color();
                        Draw.alpha(1);
                        Draw.rect(canBar, realX + vts / 2f + vts / 2f, ly + height / 2f, vts + 1, height + 2);
                        Fonts.outline.draw(ls.liquid.localizedName, realX, ly - 1, Color.white, 0.2f, false, Align.center);
                        realX += tw;
                    }
                }
            }
        }

        @Override
        public void displayBars(Table table) {
            super.displayBars(table);
            if (formula == null) return;
            Formula[] lastFormula = {formula};
            table.update(() -> {
                if (lastFormula[0] != formula) {
                    rebuildBar(table);
                    lastFormula[0] = formula;
                }
            });
            rebuildBar(table);
        }

        private void rebuildBar(Table table) {
            table.clear();

            for (Func<Building, Bar> buildingBarFunc : block.listBars()) {
                Bar result = buildingBarFunc.get(this);
                if (result != null) {
                    table.add(result).growX();
                    table.row();
                }
            }
            if (formula == null) return;
            if (formula.barMap.size > 0) {
                for (Func<Building, Bar> bar : formula.listBars()) {
                    Bar result = bar.get(this);
                    if (result == null) continue;
                    table.add(result).growX();
                    table.row();
                }
            }
        }

        @Override
        public boolean shouldAmbientSound() {
            return efficiency > 0;
        }

        public void updateConsumption() {
            if (formula == null) return;
            if (formula.hasConsumers && !cheating()) {
                if (!enabled) {
                    potentialEfficiency = efficiency = optionalEfficiency = 0;
                } else {
                    boolean update = shouldConsume() && productionValid();
                    float minEfficiency = 1f;
                    efficiency = optionalEfficiency = 1f;
                    Consume[] c = formula.nonOptionalConsumers;
                    int cl = c.length;

                    int i;
                    Consume cons;
                    for (i = 0; i < cl; i++) {
                        cons = c[i];
                        minEfficiency = Math.min(minEfficiency, cons.efficiency(this));
                    }

                    c = formula.optionalConsumers;
                    cl = c.length;

                    for (i = 0; i < cl; i++) {
                        cons = c[i];
                        optionalEfficiency = Math.min(optionalEfficiency, cons.efficiency(this));
                    }

                    efficiency = minEfficiency;
                    optionalEfficiency = Math.min(optionalEfficiency, minEfficiency);
                    potentialEfficiency = efficiency;
                    if (!update) {
                        efficiency = optionalEfficiency = 0f;
                    }

                    updateEfficiencyMultiplier();
                    if (update && efficiency > 0f) {
                        c = formula.updateConsumers;
                        cl = c.length;

                        for (i = 0; i < cl; i++) {
                            cons = c[i];
                            cons.update(this);
                        }
                    }
                }
            } else {
                potentialEfficiency = enabled && productionValid() ? 1f : 0f;
                efficiency = optionalEfficiency = shouldConsume() ? potentialEfficiency : 0f;
                updateEfficiencyMultiplier();
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            Table rtc = new Table();
            rtc.left().defaults().size(55);

            Table cont = new Table().top();
            cont.left().defaults().left().growX();

            Runnable rebuild = () -> {
                rtc.clearChildren();
                if (hasDoubleOutput) {
                    for (int i = 0; i < rotationIcon.length; i++) {
                        ImageButton button = new ImageButton();
                        int I = i;
                        button.table(img -> img.image(rotationIcon[I]).color(Color.white).size(40).pad(10f));
                        button.changed(() -> {
                            configs[0] = I;
                            configure(configs);
                        });
                        button.update(() -> button.setChecked(rotation == I));
                        button.setStyle(Styles.clearNoneTogglei);
                        rtc.add(button).tooltip(i * 90 + "angle");
                    }
                }

                cont.clearChildren();
                for (Formula f : products) {
                    ImageButton button = new ImageButton();
                    button.table(info -> {
                        info.left();
                        info.table(from -> {
                            Stats stat = new Stats();
                            stat.timePeriod = f.craftTime;
                            for (Consume c : f.consumers) {
                                c.display(stat);
                            }
                            UIUtils.statToTable(stat, from);
                        }).left().pad(6);
                        info.row();
                        info.table(to -> {
                            if (f.outputItems != null) {
                                StatValues.items(f.craftTime, f.outputItems).display(to);
                            }

                            if (f.outputLiquids != null) {
                                StatValues.liquids(1f, f.outputLiquids).display(to);
                            }
                        }).left().pad(6);
                    }).grow().left().pad(5);
                    button.setStyle(Styles.clearNoneTogglei);
                    button.changed(() -> {
                        configs[1] = products.indexOf(f);
                        configure(configs);
                    });
                    button.update(() -> button.setChecked(formula == f));
                    cont.add(button);
                    cont.row();
                }
            };

            rebuild.run();

            Table main = new Table().background(Styles.black6);

            main.add(rtc).left().row();

            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);

            if (block != null) {
                pane.setScrollYForce(block.selectScroll);
                pane.update(() -> block.selectScroll = pane.getScrollY());
            }

            pane.setOverscroll(false, false);
            main.add(pane).maxHeight(100 * maxList);
            table.top().add(main);
        }

        @Override
        public int[] config() {
            return configs;
        }

        @Override
        public void configure(Object value) {
            super.configure(value);
            deselect();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(progress);
            write.f(warmup);
            write.i(lastRotation);
            write.i(formula == null || !products.contains(formula) ? -1 : products.indexOf(formula));
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            progress = read.f();
            warmup = read.f();
            lastRotation = read.i();
            int i = read.i();
            formula = i == -1 ? null : products.get(i);
            configs[0] = rotation;
            configs[1] = i;
        }
    }

    public static class Formula {
        /** Array of consumers used by this block. Only populated after init(). */
        public Consume[] consumers = {}, optionalConsumers = {}, nonOptionalConsumers = {}, updateConsumers = {};
        /** Set to true if this block has any consumers in its array. */
        public boolean hasConsumers;
        /** The single power consumer, if applicable. */
        public @Nullable ConsumePower consPower;

        public float craftTime;

        public @Nullable ItemStack[] outputItems;
        public @Nullable LiquidStack[] outputLiquids;

        public int[] liquidOutputDirections = {-1};
        public boolean ignoreLiquidFullness = false;
        public boolean dumpExtraLiquid = true;

        public MultiCrafterE owner = null;

        public float warmupSpeed = 0.02f;

        public float updateEffectChance = 0.05f;
        public Effect updateEffect = Fx.none;
        public Effect craftEffect = Fx.none;
        public DrawBlock drawer = new DrawDefault();

        /** Consumption filters. */
        public ObjectMap<Item, Boolean> itemFilter = new ObjectMap<>();
        public ObjectMap<Liquid, Boolean> liquidFilter = new ObjectMap<>();

        /** List for building up consumption before init(). */
        protected Seq<Consume> consumeBuilder = new Seq<>();
        protected OrderedMap<String, Func<Building, Bar>> barMap = new OrderedMap<>();

        public void init() {
            consumers = consumeBuilder.toArray(Consume.class);
            optionalConsumers = consumeBuilder.select(consume -> consume.optional && !consume.ignore()).toArray(Consume.class);
            nonOptionalConsumers = consumeBuilder.select(consume -> !consume.optional && !consume.ignore()).toArray(Consume.class);
            updateConsumers = consumeBuilder.select(consume -> consume.update && !consume.ignore()).toArray(Consume.class);
            hasConsumers = consumers.length > 0;

            if (owner.autoAddBar) {
                if (liquidFilter.size > 0) {
                    for (Liquid l : liquidFilter.keys().toSeq()) {
                        addLiquidBar(l);
                    }
                }
                if (outputLiquids != null) {
                    for (LiquidStack l : outputLiquids) {
                        addLiquidBar(l.liquid);
                    }
                }
            }
        }

        public void setApply(UnlockableContent content) {
            if (content instanceof Item item) {
                itemFilter.put(item, true);
            }
            if (content instanceof Liquid liquid) {
                liquidFilter.put(liquid, true);
            }
        }

        public Iterable<Func<Building, Bar>> listBars() {
            return barMap.values();
        }

        public void addBar(String name, Func<Building, Bar> sup) {
            barMap.put(name, sup);
        }

        public void addLiquidBar(Liquid liq) {
            addBar("liquid-" + liq.name, entity -> !liq.unlockedNow() ? null : new Bar(
                    () -> liq.localizedName,
                    liq::barColor,
                    () -> entity.liquids.get(liq) / owner.liquidCapacity
            ));
        }

        public boolean getConsumeItem(Item item) {
            return itemFilter.containsKey(item) && itemFilter.get(item);
        }

        public boolean getConsumeLiquid(Liquid liquid) {
            return liquidFilter.containsKey(liquid) && liquidFilter.get(liquid);
        }

        @SuppressWarnings("unchecked")
        public <T extends Consume> T findConsumer(Boolf<Consume> filter) {
            return consumers.length == 0 ? (T) consumeBuilder.find(filter) : (T) Structs.find(consumers, filter);
        }

        public boolean hasConsumer(Consume cons) {
            return consumeBuilder.contains(cons);
        }

        public void removeConsumer(Consume cons) {
            if (consumers.length > 0) {
                throw new IllegalStateException("You can only remove consumers before init(). After init(), all consumers have already been initialized.");
            }
            consumeBuilder.remove(cons);
        }

        public void removeConsumers(Boolf<Consume> b) {
            consumeBuilder.removeAll(b);
            //the power was removed, unassign it
            if (!consumeBuilder.contains(c -> c instanceof ConsumePower)) {
                consPower = null;
            }
        }

        public void consumeLiquid(Liquid liquid, float amount) {
            setApply(liquid);
            consume(new ConsumeLiquid(liquid, amount));
        }

        public void consumeLiquids(LiquidStack... stacks) {
            for (LiquidStack s : stacks) setApply(s.liquid);
            consume(new ConsumeLiquids(stacks));
        }

        public void consumePower(float powerPerTick) {
            consume(new ConsumePower(powerPerTick, 0f, false));
        }

        public void consumeItem(Item item) {
            setApply(item);
            consumeItem(item, 1);
        }

        public void consumeItem(Item item, int amount) {
            setApply(item);
            consume(new ConsumeItems(new ItemStack[]{new ItemStack(item, amount)}));
        }

        public void consumeItems(Item copper, int i, ItemStack... items) {
            for (ItemStack s : items) setApply(s.item);
            consume(new ConsumeItems(items));
        }

        public <T extends Consume> void consume(T consume) {
            if (consume instanceof ConsumePower consumePower) {
                consumeBuilder.removeAll(b -> b instanceof ConsumePower);
                consPower = consumePower;
            }
            consumeBuilder.add(consume);
        }
    }
}
