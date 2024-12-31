package wh.world.blocks.production;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.utils.*;
import arc.struct.*;
import arc.struct.EnumSet;
import arc.util.*;
import arc.util.io.*;
import arc.util.serialization.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.entities.effect.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import wh.ui.*;
import wh.ui.ItemImage;
import wh.world.consumers.*;

import java.lang.reflect.*;
import java.util.*;

import static mindustry.Vars.*;

@SuppressWarnings("unused")
public class MultiCrafterD extends PayloadBlock {
    public boolean hasHeat = false;
    public boolean hasPayloads = false;

    public float powerCapacity = 0f;
    /** maximum payloads this block can carry */
    public int payloadCapacity = 1;

    public float itemCapacityMultiplier = 1f;
    public float fluidCapacityMultiplier = 1f;
    public float powerCapacityMultiplier = 1f;
    public float payloadCapacityMultiplier = 2f;
    /*
    [ ==> Seq
      { ==> ObjectMap
        // String ==> Any --- Value may be a Seq, Map or String
        input:{
          // String ==> Any --- Value may be a Seq<String>, Seq<Map>, String or Map
          items:["mod-id-item/1","mod-id-item2/1"],
          fluids:["mod-id-liquid/10.5","mod-id-gas/10"]
          power: 3 pre tick
        },
        output:{
          items:["mod-id-item/1","mod-id-item2/1"],
          fluids:["mod-id-liquid/10.5","mod-id-gas/10"]
          heat: 10
        },
        craftTime: 120
      }
    ]
     */
    /**
     * For Json and Javascript to configure.
     */
    public Object recipes;
    /**
     * The resolved recipes.
     */
    @Nullable
    public Seq<Recipe> resolvedRecipes = null;
    /**
     * For Json and Javascript to configure.
     */
    public String menu = "transform";
    /**
     * The resolved menu.
     */
    @Nullable
    public RecipeSwitchStyle switchStyle = null;
    public Effect craftEffect = Fx.none;
    public Effect updateEffect = Fx.none;
    public Effect changeRecipeEffect = Fx.rotateBlock;
    public int[] fluidOutputDirections = {-1};
    public float updateEffectChance = 0.04f;
    public float warmupSpeed = 0.019f;
    /**
     * Whether stop production when the fluid is full.
     * Turn off this to ignore fluid output, for instance, the fluid is only by-product.
     */
    public boolean ignoreLiquidFullness = false;

    /**
     * If true, the crafter with multiple fluid outputs will dump excess,
     * when there's still space for at least one fluid type.
     */
    public boolean dumpExtraFluid = true;
    public DrawBlock drawer = new DrawDefault();

    protected boolean isOutputItem = false;
    protected boolean isConsumeItem = false;
    protected boolean isOutputFluid = false;
    protected boolean isConsumeFluid = false;
    protected boolean isOutputPower = false;
    protected boolean isConsumePower = false;
    protected boolean isOutputHeat = false;
    protected boolean isConsumeHeat = false;
    protected boolean isOutputPayload = false;
    protected boolean isConsumePayload = false;
    /**
     * What color of heat for recipe selector.
     */
    public Color heatColor = new Color(1f, 0.22f, 0.22f, 0.8f);
    /**
     * For {@linkplain HeatConsumer},
     * it's used to display something of block or initialize the recipe index.
     */
    public int defaultRecipeIndex = 0;
    /**
     * For {@linkplain HeatConsumer},
     * after heat meets this requirement, excess heat will be scaled by this number.
     */
    public float overheatScale = 1f;
    /**
     * For {@linkplain HeatConsumer},
     * maximum possible efficiency after overheat.
     */
    public float maxEfficiency = 1f;
    /**
     * For {@linkplain HeatBlock}
     */
    public float warmupRate = 0.15f;
    /**
     * Whether to show name tooltip in {@link MultiCrafterBuildD#buildStats(Table)}
     */
    protected boolean showNameTooltip = false;

    public MultiCrafterD(String name) {
        super(name);
        update = true;
        solid = true;
        sync = true;
        flags = EnumSet.of(BlockFlag.factory);
        ambientSound = Sounds.machine;
        configurable = true;
        saveConfig = true;
        ambientSoundVolume = 0.03f;
        config(Integer.class, MultiCrafterBuildD::setCurRecipeIndexFromRemote);
        Log.info("MultiCrafter[" + this.name + "] loaded.");
    }

    @Override
    public void init() {
        hasItems = false;
        hasLiquids = false;
        hasPower = false;
        hasHeat = false;
        hasPayloads = false;
        outputsPower = false;
        outputsPayload = false;

        final MultiCrafterParser parser = new MultiCrafterParser();
        // if the recipe is already set in another way, don't analyze it again.
        if (resolvedRecipes == null && recipes != null) resolvedRecipes = parser.parse(this, recipes);
        if (resolvedRecipes == null || resolvedRecipes.isEmpty())
            throw new ArcRuntimeException(MultiCrafterParser.genName(this) + " has no recipe! It's perhaps because all recipes didn't find items, fluids or payloads they need. Check your `last_log.txt` to obtain more information.");
        if (switchStyle == null) switchStyle = RecipeSwitchStyle.get(menu);
        decorateRecipes();
        setupBlockByRecipes();
        defaultRecipeIndex = Mathf.clamp(defaultRecipeIndex, 0, resolvedRecipes.size - 1);
        recipes = null; // free the recipe Seq, it's useless now.
        setupConsumers();
        super.init();
    }

    @Nullable
    protected static Table hoveredInfo;

    public class MultiCrafterBuildD extends PayloadBlockBuild<Payload> implements HeatBlock, HeatConsumer {
        /**
         * For {@linkplain HeatConsumer}, only enabled when the multicrafter requires heat input
         */
        public float[] sideHeat = new float[4];
        /**
         * For {@linkplain HeatConsumer} and {@linkplain HeatBlock},
         * only enabled when the multicrafter requires heat as input or can output heat.
         * Serialized
         */
        public float heat = 0f;
        /**
         * Serialized
         */
        public float craftingTime;
        public float totalProgress;
        /**
         * Serialized
         */
        public float warmup;
        /**
         * Serialized
         */
        public int curRecipeIndex = defaultRecipeIndex;

        public PayloadSeq payloads = new PayloadSeq();
        public @Nullable Vec2 commandPos;

        public void setCurRecipeIndexFromRemote(int index) {
            int newIndex = Mathf.clamp(index, 0, resolvedRecipes.size - 1);
            if (newIndex != curRecipeIndex) {
                curRecipeIndex = newIndex;
                createEffect(changeRecipeEffect);
                craftingTime = 0f;
                if (!Vars.headless) rebuildHoveredInfo();
            }
        }

        public Recipe getCurRecipe() {
            // Prevent out of bound
            curRecipeIndex = Mathf.clamp(curRecipeIndex, 0, resolvedRecipes.size - 1);
            return resolvedRecipes.get(curRecipeIndex);
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return hasItems &&
                    getCurRecipe().input.itemsUnique.contains(item) &&
                    items.get(item) < getMaximumAccepted(item);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            return hasLiquids &&
                    getCurRecipe().input.fluidsUnique.contains(liquid) &&
                    liquids.get(liquid) < liquidCapacity;
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return hasPayloads && this.payload == null &&
                    getCurRecipe().input.payloadsUnique.contains(payload.content()) &&
                    payloads.get(payload.content()) < payloadCapacity;
        }

        @Override
        public PayloadSeq getPayloads() {
            return this.payloads;
        }

        public void yeetPayload(Payload payload) {
            payloads.add(payload.content(), 1);
        }

        @Override
        public Vec2 getCommandPosition() {
            if (getCurRecipe().isOutputPayload())
                return this.commandPos;
            else return null;
        }

        @Override
        public void onCommand(Vec2 target) {
            if (getCurRecipe().isOutputPayload())
                this.commandPos = target;
        }

        @Override
        public float edelta() {
            Recipe cur = getCurRecipe();
            if (cur.input.power > 0f) return this.efficiency *
                    Mathf.clamp(getCurPowerStore() / cur.input.power) *
                    this.delta();
            else return this.efficiency * this.delta();
        }

        @Override
        public void updateTile() {
            Recipe cur = getCurRecipe();
            float craftTimeNeed = cur.craftTime;
            // As HeatConsumer
            if (cur.isConsumeHeat()) heat = calculateHeat(sideHeat);
            if (cur.isOutputHeat()) {
                float heatOutput = cur.output.heat;
                heat = Mathf.approachDelta(heat, heatOutput * efficiency, warmupRate * edelta());
            }
            // cool down
            if (efficiency > 0 && (!hasPower || getCurPowerStore() >= cur.input.power)) {
                // if <= 0, instantly produced
                if (craftTimeNeed > 0f) craftingTime += edelta();
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);
                if (hasPower) {
                    float powerChange = (cur.output.power - cur.input.power) * delta();
                    if (!Mathf.zero(powerChange))
                        setCurPowerStore((getCurPowerStore() + powerChange));
                }

                //continuously output fluid based on efficiency
                if (cur.isOutputFluid()) {
                    float increment = getProgressIncrease(1f);
                    for (LiquidStack output : cur.output.fluids) {
                        Liquid fluid = output.liquid;
                        handleLiquid(this, fluid, Math.min(output.amount * increment, liquidCapacity - liquids.get(fluid)));
                    }
                }
                // particle fx
                if (wasVisible && Mathf.chanceDelta(updateEffectChance))
                    updateEffect.at(x + Mathf.range(size * 4f), y + Mathf.range(size * 4));
            } else warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            totalProgress += warmup * Time.delta;

            if (moveInPayload()) {
                yeetPayload(payload);
                payload = null;
            }

            if (craftTimeNeed <= 0f) {
                if (efficiency > 0f)
                    craft();
            } else if (craftingTime >= craftTimeNeed)
                craft();

            updateBars();
            dumpOutputs();
        }

        public void updateBars() {
            barMap.clear();
            setBars();
        }

        @Override
        public boolean shouldConsume() {
            Recipe cur = getCurRecipe();
            if (hasItems) for (ItemStack output : cur.output.items)
                if (items.get(output.item) + output.amount > itemCapacity)
                    return false;

            if (hasLiquids) if (cur.isOutputFluid() && !ignoreLiquidFullness) {
                boolean allFull = true;
                for (LiquidStack output : cur.output.fluids)
                    if (liquids.get(output.liquid) >= liquidCapacity - 0.001f) {
                        if (!dumpExtraFluid) return false;
                    } else
                        allFull = false; //if there's still space left, it's not full for all fluids

                //if there is no space left for any fluid, it can't reproduce
                if (allFull) return false;
            }
            if (hasPayloads) for (PayloadStack output : cur.output.payloads)
                if (payloads.get(output.item) + output.amount > payloadCapacity)
                    return false;
            return enabled;
        }

        public void craft() {
            consume();
            Recipe cur = getCurRecipe();
            if (cur.isOutputItem())
                for (ItemStack output : cur.output.items) for (int i = 0; i < output.amount; i++) offload(output.item);

            if (wasVisible) createCraftEffect();
            if (cur.craftTime > 0f)
                craftingTime %= cur.craftTime;
            else
                craftingTime = 0f;
        }

        public void createCraftEffect() {
            Recipe cur = getCurRecipe();
            Effect curFx = cur.craftEffect;
            Effect fx = curFx != Fx.none ? curFx : craftEffect;
            createEffect(fx);
        }

        public void dumpOutputs() {
            Recipe cur = getCurRecipe();
            if (timer(timerDump, dumpTime / timeScale)) {
                if (cur.isOutputItem())
                    for (ItemStack output : cur.output.items) dump(output.item);

                //TODO fix infinite output
                if (cur.isOutputPayload()) {
                    for (PayloadStack output : cur.output.payloads) {
                        Payload payloadOutput = null;
                        if (output.item instanceof Block)
                            payloadOutput = new BuildPayload((Block) output.item, this.team);
                        else if (output.item instanceof UnitType)
                            payloadOutput = new UnitPayload(((UnitType) output.item).create(this.team));

                        if (payloadOutput != null)
                            dumpPayload(payloadOutput);
                    }
                }
            }

            if (cur.isOutputFluid()) {
                LiquidStack[] fluids = cur.output.fluids;
                for (int i = 0; i < fluids.length; i++) {
                    int dir = fluidOutputDirections.length > i ? fluidOutputDirections[i] : -1;
                    dumpLiquid(fluids[i].liquid, 2f, dir);
                }
            }
        }

        /**
         * As {@linkplain HeatBlock}
         */
        @Override
        public float heat() {
            return heat;
        }

        /**
         * As {@linkplain HeatBlock}
         */
        @Override
        public float heatFrac() {
            Recipe cur = getCurRecipe();
            if (isOutputHeat && cur.isOutputHeat()) return heat / cur.output.heat;
            else if (isConsumeHeat && cur.isConsumeHeat()) return heat / cur.input.heat;
            return 0f;
        }

        /**
         * As {@linkplain HeatConsumer}
         * Only for visual effects
         */
        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        /**
         * As {@linkplain HeatConsumer}
         * Only for visual effects
         */
        @Override
        public float heatRequirement() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) return cur.input.heat;
            return 0f;
        }

        @Override
        public float calculateHeat(float[] sideHeat) {
            Point2[] edges = this.block.getEdges();
            for (Point2 edge : edges) {
                Building build = this.nearby(edge.x, edge.y);
                if (build != null && build.team == this.team && build instanceof HeatBlock heater) {
                    // Only calculate heat if the block is a heater or a multicrafter heat output
                    if (heater instanceof MultiCrafterBuildD multi) {
                        if (multi.getCurRecipe().isOutputHeat())
                            return this.calculateHeat(sideHeat, null);
                    } else return this.calculateHeat(sideHeat, null);
                }
            }

            return 0.0f;
        }

        @Override
        public float getPowerProduction() {
            Recipe cur = getCurRecipe();

            if (isOutputPower && cur.isOutputPower()) return cur.output.power * efficiency;
            else return 0f;
        }

        @Override
        public void buildConfiguration(Table table) {
            switchStyle.build(MultiCrafterD.this, this, table);
        }

        public float getCurPowerStore() {
            if (power == null) return 0f;
            return power.status * powerCapacity;
        }

        public void setCurPowerStore(float powerStore) {
            if (power == null) return;
            power.status = Mathf.clamp(powerStore / powerCapacity);
        }

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public Object config() {
            return curRecipeIndex;
        }

        @Override
        public boolean shouldAmbientSound() {
            return efficiency > 0;
        }

        @Override
        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress) return progress();
            if (sensor == LAccess.heat) return warmup();
            //attempt to prevent wild total fluid fluctuation, at least for crafter
            //if(sensor == LAccess.totalLiquids && outputLiquid != null) return liquids.get(outputLiquid.liquid);
            return super.sense(sensor);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(craftingTime);
            write.f(warmup);
            write.i(curRecipeIndex);
            write.f(heat);

            //TODO Fix save corruption
            if(getCurRecipe().isConsumePayload())
                payloads.write(write);
            if (getCurRecipe().isOutputPayload())
                TypeIO.writeVecNullable(write, commandPos);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            craftingTime = read.f();
            warmup = read.f();
            curRecipeIndex = Mathf.clamp(read.i(), 0, resolvedRecipes.size - 1);
            heat = read.f();

            //TODO Fix save corruption
            if(getCurRecipe().isConsumePayload())
                payloads.read(read);
            if (revision >= 1 && getCurRecipe().isOutputPayload())
                commandPos = TypeIO.readVecNullable(read);
        }

        public float warmupTarget() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) return Mathf.clamp(heat / cur.input.heat);
            else return 1f;
        }

        @Override
        public void updateEfficiencyMultiplier() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) {
                efficiency *= efficiencyScale();
                potentialEfficiency *= efficiencyScale();
            }
        }

        public float efficiencyScale() {
            Recipe cur = getCurRecipe();
            // When As HeatConsumer
            if (isConsumeHeat && cur.isConsumeHeat()) {
                float heatRequirement = cur.input.heat;
                float over = Math.max(heat - heatRequirement, 0f);
                return Math.min(Mathf.clamp(heat / heatRequirement) + over / heatRequirement * overheatScale, maxEfficiency);
            } else return 1f;
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float progress() {
            Recipe cur = getCurRecipe();
            return Mathf.clamp(cur.craftTime > 0f ? craftingTime / cur.craftTime : 1f);
        }

        @Override
        public float totalProgress(){
            return totalProgress;
        }

        @Override
        public void display(Table table) {
            super.display(table);
            hoveredInfo = table;
        }

        public void rebuildHoveredInfo() {
            try {
                Table info = hoveredInfo;
                if (info != null) {
                    info.clear();
                    display(info);
                }
            } catch (Exception ignored) {
                // Maybe null pointer or cast exception
            }
        }

        public void createEffect(Effect effect) {
            if (effect == Fx.none) return;
            if (effect == Fx.placeBlock) effect.at(x, y, block.size);
            else if (effect == Fx.coreBuildBlock) effect.at(x, y, 0f, block);
            else if (effect == Fx.upgradeCore) effect.at(x, y, 0f, block);
            else if (effect == Fx.upgradeCoreBloom) effect.at(x, y, block.size);
            else if (effect == Fx.rotateBlock) effect.at(x, y, block.size);
            else effect.at(x, y, 0, this);
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.output, t -> {
            showNameTooltip = true;
            buildStats(t);
            showNameTooltip = false;
        });
    }

    public void buildStats(Table stat) {
        stat.row();
        for (Recipe recipe : resolvedRecipes) {
            Table t = new Table();
            t.background(Tex.whiteui);
            t.setColor(Pal.darkestGray);
            // Input
            buildIOEntry(t, recipe, true);
            // Time
            Table time = new Table();
            final float[] duration = {0f};
            float visualCraftTime = recipe.craftTime;
            time.update(() -> {
                duration[0] += Time.delta;
                if (duration[0] > visualCraftTime) duration[0] = 0f;
            });
            String craftTime = recipe.craftTime == 0 ? "0" : String.format("%.2f", recipe.craftTime / 60f);
            Cell<Bar> barCell = time.add(new Bar(() -> craftTime,
                            () -> Pal.accent,
                            () -> Interp.smooth.apply(duration[0] / visualCraftTime)))
                    .height(45f);
            barCell.width(Vars.mobile ? 220f : 250f);
            Cell<Table> timeCell = t.add(time).pad(12f);
            if (showNameTooltip)
                timeCell.tooltip(Stat.productionTime.localized() + ": " + craftTime + " " + StatUnit.seconds.localized());
            // Output
            buildIOEntry(t, recipe, false);
            stat.add(t).pad(10f).grow();
            stat.row();
        }
        stat.row();
        stat.defaults().grow();
    }

    protected void buildIOEntry(Table table, Recipe recipe, boolean isInput) {
        Table t = new Table();
        if (isInput) t.left();
        else t.right();
        Table mat = new Table();
        IOEntry entry = isInput ? recipe.input : recipe.output;
        int i = 0;
        for (ItemStack stack : entry.items) {
            Cell<ItemImage> iconCell = mat.add(new ItemImage(stack.item.uiIcon, stack.amount))
                    .pad(2f);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (showNameTooltip)
                iconCell.tooltip(stack.item.localizedName);
            if (i != 0 && i % 2 == 0) mat.row();
            i++;
        }
        for (LiquidStack stack : entry.fluids) {
            Cell<FluidImage> iconCell = mat.add(new FluidImage(stack.liquid.uiIcon, stack.amount * 60f))
                    .pad(2f);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (showNameTooltip)
                iconCell.tooltip(stack.liquid.localizedName);
            if (i != 0 && i % 2 == 0) mat.row();
            i++;
        }
        // No redundant ui
        // Power
        if (entry.power > 0f) {
            Cell<PowerImage> iconCell = mat.add(new PowerImage(entry.power * 60f))
                    .pad(2f);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (showNameTooltip)
                iconCell.tooltip(entry.power + " " + StatUnit.powerSecond.localized());
            i++;
            if (i != 0 && i % 2 == 0) mat.row();
        }
        //Heat
        if (entry.heat > 0f) {
            Cell<HeatImage> iconCell = mat.add(new HeatImage(entry.heat))
                    .pad(2f);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (showNameTooltip)
                iconCell.tooltip(entry.heat + " " + StatUnit.heatUnits.localized());
            i++;
            if (i != 0 && i % 2 == 0) mat.row();
        }
        for (PayloadStack stack : entry.payloads) {
            Cell<PayloadImage> iconCell = mat.add(new PayloadImage(stack.item.uiIcon, stack.amount))
                    .pad(2f);
            if (showNameTooltip)
                iconCell.tooltip(stack.item.localizedName);
            if (isInput) iconCell.left();
            else iconCell.right();
            if (i != 0 && i % 2 == 0) mat.row();
            i++;
        }
        Cell<Table> matCell = t.add(mat);
        if (isInput) matCell.left();
        else matCell.right();
        Cell<Table> tCell = table.add(t).pad(12f).fill();
        tCell.width(Vars.mobile ? 90f : 120f);
    }

    @Override
    public void setBars() {
        super.setBars();

        if (hasPower)
            addBar("power", (MultiCrafterBuildD b) -> new Bar(
                    b.getCurRecipe().isOutputPower() ? Core.bundle.format("bar.poweroutput", Strings.fixed(b.getPowerProduction() * 60f * b.timeScale(), 1)) : "bar.power",
                    Pal.powerBar,
                    () -> b.efficiency
            ));
        if (isConsumeHeat || isOutputHeat)
            addBar("heat", (MultiCrafterBuildD b) -> new Bar(
                    b.getCurRecipe().isConsumeHeat() ? Core.bundle.format("bar.heatpercent", (int) (b.heat + 0.01f), (int) (b.efficiencyScale() * 100 + 0.01f)) : "bar.heat",
                    Pal.lightOrange,
                    b::heatFrac
            ));
        addBar("progress", (MultiCrafterBuildD b) -> new Bar(
                "bar.loadprogress",
                Pal.accent,
                b::progress
        ));
    }

    @Override
    public boolean rotatedOutput(int x, int y) {
        return false;
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
    public boolean outputsItems() {
        return isOutputItem;
    }

    @Override
    public void drawOverlay(float x, float y, int rotation) {
        Recipe firstRecipe = resolvedRecipes.get(defaultRecipeIndex);
        LiquidStack[] fluids = firstRecipe.output.fluids;
        for (int i = 0; i < fluids.length; i++) {
            int dir = fluidOutputDirections.length > i ? fluidOutputDirections[i] : -1;

            if (dir != -1) Draw.rect(
                    fluids[i].liquid.fullIcon,
                    x + Geometry.d4x(dir + rotation) * (size * tilesize / 2f + 4),
                    y + Geometry.d4y(dir + rotation) * (size * tilesize / 2f + 4),
                    8f, 8f
            );
        }
    }

    protected void decorateRecipes() {
        resolvedRecipes.shrink();
        for (Recipe recipe : resolvedRecipes)
            recipe.cacheUnique();
    }

    protected void setupBlockByRecipes() {
        int maxItemAmount = 0;
        float maxFluidAmount = 0f;
        float maxPower = 0f;
        float maxHeat = 0f;
        int maxPayloadAmount = 0;

        for (Recipe recipe : resolvedRecipes) {
            hasItems |= recipe.hasItems();
            hasLiquids |= recipe.hasFluids();
            conductivePower = hasPower |= recipe.hasPower();
            hasHeat |= recipe.hasHeat();
            hasPayloads |= recipe.hasPayloads();

            maxItemAmount = Math.max(recipe.maxItemAmount(), maxItemAmount);
            maxFluidAmount = Math.max(recipe.maxFluidAmount(), maxFluidAmount);
            maxPower = Math.max(recipe.maxPower(), maxPower);
            maxHeat = Math.max(recipe.maxHeat(), maxHeat);
            maxPayloadAmount = Math.max(recipe.maxPayloadAmount(), maxPayloadAmount);

            isOutputItem |= recipe.isOutputItem();
            acceptsItems =   isConsumeItem |= recipe.isConsumeItem();
            outputsLiquid =  isOutputFluid |= recipe.isOutputFluid();
            isConsumeFluid |= recipe.isConsumeFluid();
            outputsPower =   isOutputPower |= recipe.isOutputPower();
            consumesPower =  isConsumePower |= recipe.isConsumePower();
            isOutputHeat |= recipe.isOutputHeat();
            isConsumeHeat |= recipe.isConsumeHeat();
            outputsPayload = isOutputPayload |= recipe.isOutputPayload();
            acceptsPayload = isConsumePayload |= recipe.isConsumePayload();
        }

        itemCapacity = Math.max((int) (maxItemAmount * itemCapacityMultiplier), itemCapacity);
        liquidCapacity = Math.max((int) (maxFluidAmount * 60f * fluidCapacityMultiplier), liquidCapacity);
        powerCapacity = Math.max(maxPower * 60f * powerCapacityMultiplier, powerCapacity);
        payloadCapacity = Math.max((int) (maxPayloadAmount * payloadCapacityMultiplier), payloadCapacity);
        if (isOutputHeat) {
            rotate = true;
            rotateDraw = false;
            canOverdrive = false;
            drawArrow = true;
        }
    }

    protected void setupConsumers() {
        if (isConsumeItem) consume(new ConsumeItemDynamic(
                (MultiCrafterBuildD b) -> b.getCurRecipe().input.items
        ));
        if (isConsumeFluid) consume(new ConsumeFluidDynamic(
                (MultiCrafterBuildD b) -> b.getCurRecipe().input.fluids
        ));
        if (isConsumePower) consume(new ConsumePowerDynamic(b ->
                ((MultiCrafterBuildD) b).getCurRecipe().input.power
        ));
        if (isConsumePayload) consume(new CustomConsumePayloadDynamic(
                (MultiCrafterBuildD b) -> b.getCurRecipe().input.payloads
        ));
    }

    public static class MultiCrafterParser {
        private static final String[] inputAlias = {"input", "in", "i"};
        private static final String[] outputAlias = {"output", "out", "o"};

        private String curBlock = "";
        private int recipeIndex = 0;

        private final Seq<String> errors = new Seq<>();
        private final Seq<String> warnings = new Seq<>();

        @SuppressWarnings({"rawtypes"})
        public Seq<Recipe> parse(Block meta, Object o) {
            curBlock = genName(meta);
            try {
                o = ParserUtils.parseJsonToObject(o);
            } catch (Exception e) {
                error("Can't convert Seq in preprocess " + o, e);
                o = Collections.emptyList();
            }
            Seq<Recipe> recipes = new Seq<>(Recipe.class);
            recipeIndex = 0;
            if (o instanceof List all) { // A list of recipe
                for (Object recipeMapObj : all) {
                    Map recipeMap = (Map) recipeMapObj;
                    parseRecipe(recipeMap, recipes);
                    recipeIndex++;
                }
            } else if (o instanceof Map recipeMap) { // Only one recipe
                parseRecipe(recipeMap, recipes);
            } else {
                throw new RecipeParserException("Unsupported recipe list from <" + o + ">");
            }
            return recipes;
        }

        private static final Prov<TextureRegion> NotFound = () -> Icon.cancel.getRegion();

        @SuppressWarnings("rawtypes")
        private void parseRecipe(Map recipeMap, Seq<Recipe> to) {
            try {
                Recipe recipe = new Recipe();
                // parse input
                Object inputsRaw = findValueByAlias(recipeMap, inputAlias);
                if (inputsRaw == null) {
                    warn("Recipe has no input, please ensure it's expected.");
                }
                recipe.input = parseIOEntry("input", inputsRaw);
                // parse output
                Object outputsRaw = findValueByAlias(recipeMap, outputAlias);
                if (outputsRaw == null) {
                    warn("Recipe has no output, please ensure it's expected");
                }
                recipe.output = parseIOEntry("output", outputsRaw);
                // parse craft time
                Object craftTimeObj = recipeMap.get("craftTime");
                recipe.craftTime = ParserUtils.parseFloat(craftTimeObj);
                // parse icon
                Object iconObj = recipeMap.get("icon");
                if (iconObj instanceof String) {
                    recipe.icon = findIcon((String) iconObj);
                }
                Object iconColorObj = recipeMap.get("iconColor");
                if (iconColorObj instanceof String) {
                    recipe.iconColor = Color.valueOf((String) iconColorObj);
                }
                // parse fx
                Object fxObj = recipeMap.get("craftEffect");
                Effect fx = parseFx(fxObj);
                if (fx != null) {
                    recipe.craftEffect = fx;
                }
                // Check empty
                if (recipe.input.isEmpty() && recipe.output.isEmpty()) {
                    warn("Recipe is completely empty.");
                }
                to.add(recipe);
            } catch (Exception e) {
                error("Can't load a recipe", e);
            }
        }

        @SuppressWarnings({"rawtypes"})
        private IOEntry parseIOEntry(String meta, @Nullable Object ioEntry) {
            IOEntry res = new IOEntry();
            if (ioEntry == null) {
                return res;
            } else if (ioEntry instanceof Map ioRawMap) {
            /*
                input/output:{
                  items:[],
                  fluids:[],
                  power:0,
                  heat:0,
                  payloads:[],
                  icon: Icon.power,
                  iconColor: "#FFFFFF"
                }
             */
                // Items
                Object items = ioRawMap.get("items");
                if (items != null) {
                    if (items instanceof List) { // ["mod-id-item/1","mod-id-item2"]
                        parseItems((List) items, res);
                    } else if (items instanceof String) {
                        parseItemPair((String) items, res);
                    } else if (items instanceof Map) {
                        parseItemMap((Map) items, res);
                    } else
                        throw new RecipeParserException("Unsupported type of items at " + meta + " from <" + items + ">");
                }
                // Fluids
                Object fluids = ioRawMap.get("fluids");
                if (fluids != null) {
                    if (fluids instanceof List) { // ["mod-id-fluid/1","mod-id-fluid2"]
                        parseFluids((List) fluids, res);
                    } else if (fluids instanceof String) {
                        parseFluidPair((String) fluids, res);
                    } else if (fluids instanceof Map) {
                        parseFluidMap((Map) fluids, res);
                    } else
                        throw new RecipeParserException("Unsupported type of fluids at " + meta + " from <" + fluids + ">");
                }
                // Power
                Object powerObj = ioRawMap.get("power");
                res.power = ParserUtils.parseFloat(powerObj);
                // Heat
                Object heatObj = ioRawMap.get("heat");
                res.heat = ParserUtils.parseFloat(heatObj);
                // Payloads
                Object payloads = ioRawMap.get("payloads");
                if (payloads != null) {
                    if (payloads instanceof List) { // ["mod-id-payload/1","mod-id-payload2"]
                        parsePayloads((List) payloads, res);
                    } else if (payloads instanceof String) {
                        parsePayloadPair((String) payloads, res);
                    } else if (payloads instanceof Map) {
                        parsePayloadMap((Map) payloads, res);
                    } else
                        throw new RecipeParserException("Unsupported type of payloads at " + meta + " from <" + payloads + ">");
                }

                // Icon
                Object iconObj = ioRawMap.get("icon");
                if (iconObj instanceof String) {
                    res.icon = findIcon((String) iconObj);
                }
                Object iconColorObj = ioRawMap.get("iconColor");
                if (iconColorObj instanceof String) {
                    res.iconColor = Color.valueOf((String) iconColorObj);
                }
            } else if (ioEntry instanceof List) {
            /*
              input/output: []
             */
                for (Object content : (List) ioEntry) {
                    if (content instanceof String) {
                        parseAnyPair((String) content, res);
                    } else if (content instanceof Map) {
                        parseAnyMap((Map) content, res);
                    } else {
                        throw new RecipeParserException("Unsupported type of content at " + meta + " from <" + content + ">");
                    }
                }
            } else if (ioEntry instanceof String) {
            /*
                input/output : "item/1"
             */
                parseAnyPair((String) ioEntry, res);
            } else {
                throw new RecipeParserException("Unsupported type of " + meta + " <" + ioEntry + ">");
            }
            return res;
        }

        @SuppressWarnings("rawtypes")
        private void parseItems(List items, IOEntry res) {
            for (Object entryRaw : items) {
                if (entryRaw instanceof String) { // if the input is String as "mod-id-item/1"
                    parseItemPair((String) entryRaw, res);
                } else if (entryRaw instanceof Map) {
                    // if the input is Map as { item : "copper", amount : 1 }
                    parseItemMap((Map) entryRaw, res);
                } else {
                    error("Unsupported type of items <" + entryRaw + ">, so skip them");
                }
            }
        }

        private void parseItemPair(String pair, IOEntry res) {
            try {
                String[] id2Amount = pair.split("/");
                if (id2Amount.length != 1 && id2Amount.length != 2) {
                    error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                    return;
                }
                String itemID = id2Amount[0];
                Item item = ContentResolver.findItem(itemID);
                if (item == null) {
                    error("<" + itemID + "> doesn't exist in all items, so skip this");
                    return;
                }
                ItemStack entry = new ItemStack();
                entry.item = item;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Integer.parseInt(amountStr);// throw NumberFormatException
                } else {
                    entry.amount = 1;
                }
                res.items = ParserUtils.addItemStack(res.items, entry);
            } catch (Exception e) {
                error("Can't parse an item from <" + pair + ">, so skip it", e);
            }
        }

        @SuppressWarnings("rawtypes")
        private void parseFluids(List fluids, IOEntry res) {
            for (Object entryRaw : fluids) {
                if (entryRaw instanceof String) { // if the input is String as "mod-id-fluid/1"
                    parseFluidPair((String) entryRaw, res);
                } else if (entryRaw instanceof Map) {
                    // if the input is Map as { fluid : "water", amount : 1.2 }
                    parseFluidMap((Map) entryRaw, res);
                } else {
                    error("Unsupported type of fluids <" + entryRaw + ">, so skip them");
                }
            }
        }

        private void parseFluidPair(String pair, IOEntry res) {
            try {
                String[] id2Amount = pair.split("/");
                if (id2Amount.length != 1 && id2Amount.length != 2) {
                    error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                    return;
                }
                String fluidID = id2Amount[0];
                Liquid fluid = ContentResolver.findFluid(fluidID);
                if (fluid == null) {
                    error("<" + fluidID + "> doesn't exist in all fluids, so skip this");
                    return;
                }
                LiquidStack entry = new LiquidStack(Liquids.water, 0f);
                entry.liquid = fluid;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Float.parseFloat(amountStr);// throw NumberFormatException
                } else {
                    entry.amount = 1f;
                }
                res.fluids = ParserUtils.addLiquidStack(res.fluids, entry);
            } catch (Exception e) {
                error("Can't parse a fluid from <" + pair + ">, so skip it", e);
            }
        }

        @SuppressWarnings("rawtypes")
        private void parsePayloads(List payloads, IOEntry res) {
            for (Object entryRaw : payloads) {
                if (entryRaw instanceof String) { // if the input is String as "mod-id-payload/1"
                    parsePayloadPair((String) entryRaw, res);
                } else if (entryRaw instanceof Map) {
                    // if the input is Map as { payload : "router", amount : 1 }
                    parsePayloadMap((Map) entryRaw, res);
                } else {
                    error("Unsupported type of items <" + entryRaw + ">, so skip them");
                }
            }
        }

        private void parsePayloadPair(String pair, IOEntry res) {
            try {
                String[] id2Amount = pair.split("/");
                if (id2Amount.length != 1 && id2Amount.length != 2) {
                    error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                    return;
                }
                String payloadID = id2Amount[0];
                UnlockableContent payload = ContentResolver.findPayload(payloadID);
                if (payload == null) {
                    error("<" + payloadID + "> doesn't exist in all payloads, so skip this");
                    return;
                }
                PayloadStack entry = new PayloadStack();
                entry.item = payload;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Integer.parseInt(amountStr);// throw NumberFormatException
                } else {
                    entry.amount = 1;
                }
                res.payloads = ParserUtils.addPayloadStack(res.payloads, entry);
            } catch (Exception e) {
                error("Can't parse an item from <" + pair + ">, so skip it", e);
            }
        }

        /**
         * @param pair "mod-id-item/1" or "mod-id-gas"
         */
        private void parseAnyPair(String pair, IOEntry res) {
            try {
                String[] id2Amount = pair.split("/");
                if (id2Amount.length != 1 && id2Amount.length != 2) {
                    error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                    return;
                }
                String id = id2Amount[0];
                // Find in item
                Item item = ContentResolver.findItem(id);
                if (item != null) {
                    ItemStack entry = new ItemStack(Items.copper, 1);
                    entry.item = item;
                    if (id2Amount.length == 2) {
                        String amountStr = id2Amount[1];
                        entry.amount = Integer.parseInt(amountStr);// throw NumberFormatException
                    }
                    res.items = ParserUtils.addItemStack(res.items, entry);
                    return;
                }
                // Find in fluid
                Liquid fluid = ContentResolver.findFluid(id);
                if (fluid != null) {
                    LiquidStack entry = new LiquidStack(Liquids.water, 1f);
                    entry.liquid = fluid;
                    if (id2Amount.length == 2) {
                        String amountStr = id2Amount[1];
                        entry.amount = Float.parseFloat(amountStr);// throw NumberFormatException
                    }
                    res.fluids = ParserUtils.addLiquidStack(res.fluids, entry);
                    return;
                }
                // Find in payload
                UnlockableContent payload = ContentResolver.findPayload(id);
                if (payload != null) {
                    PayloadStack entry = new PayloadStack(Blocks.router, 1);
                    entry.item = payload;
                    if (id2Amount.length == 2) {
                        String amountStr = id2Amount[1];
                        entry.amount = Integer.parseInt(amountStr);// throw NumberFormatException
                    }
                    res.payloads = ParserUtils.addPayloadStack(res.payloads, entry);
                    return;
                }
                error("Can't find the corresponding item, fluid or payload from this <" + pair + ">, so skip it");
            } catch (Exception e) {
                error("Can't parse this uncertain <" + pair + ">, so skip it", e);
            }
        }

        @SuppressWarnings("rawtypes")
        private void parseAnyMap(Map map, IOEntry res) {
            try {
                Object itemRaw = map.get("item");
                if (itemRaw instanceof String) {
                    Item item = ContentResolver.findItem((String) itemRaw);
                    if (item != null) {
                        ItemStack entry = new ItemStack();
                        entry.item = item;
                        Object amountRaw = map.get("amount");
                        entry.amount = ParserUtils.parseInt(amountRaw);
                        res.items = ParserUtils.addItemStack(res.items, entry);
                        return;
                    }
                }
                Object fluidRaw = map.get("fluid");
                if (fluidRaw instanceof String) {
                    Liquid fluid = ContentResolver.findFluid((String) fluidRaw);
                    if (fluid != null) {
                        LiquidStack entry = new LiquidStack(Liquids.water, 0f);
                        entry.liquid = fluid;
                        Object amountRaw = map.get("amount");
                        entry.amount = ParserUtils.parseFloat(amountRaw);
                        res.fluids = ParserUtils.addLiquidStack(res.fluids, entry);
                        return;
                    }
                }
                Object payloadRaw = map.get("payload");
                if (payloadRaw instanceof String) {
                    UnlockableContent payload = ContentResolver.findPayload((String) payloadRaw);
                    if (payload != null) {
                        PayloadStack entry = new PayloadStack();
                        entry.item = payload;
                        Object amountRaw = map.get("amount");
                        entry.amount = ParserUtils.parseInt(amountRaw);
                        res.payloads = ParserUtils.addPayloadStack(res.payloads, entry);
                        return;
                    }
                }
                error("Can't find the corresponding item, fluid or payload from <" + map + ">, so skip it");
            } catch (Exception e) {
                error("Can't parse this uncertain <" + map + ">, so skip it", e);
            }
        }

        @SuppressWarnings("rawtypes")
        private void parseItemMap(Map map, IOEntry res) {
            try {
                ItemStack entry = new ItemStack();
                Object itemID = map.get("item");
                if (itemID instanceof String) {
                    Item item = ContentResolver.findItem((String) itemID);
                    if (item == null) {
                        error("<" + itemID + "> doesn't exist in all items, so skip this");
                        return;
                    }
                    entry.item = item;
                } else {
                    error("Can't recognize a fluid from <" + map + ">");
                    return;
                }
                int amount = ParserUtils.parseInt(map.get("amount"));
                entry.amount = amount;
                if (amount <= 0) {
                    error("Item amount is +" + amount + " <= 0, so reset as 1");
                    entry.amount = 1;
                }
                res.items = ParserUtils.addItemStack(res.items, entry);
            } catch (Exception e) {
                error("Can't parse an item <" + map + ">, so skip it", e);
            }
        }

        @SuppressWarnings("rawtypes")
        private void parseFluidMap(Map map, IOEntry res) {
            try {
                LiquidStack entry = new LiquidStack(Liquids.water, 0f);
                Object fluidID = map.get("fluid");
                if (fluidID instanceof String) {
                    Liquid fluid = ContentResolver.findFluid((String) fluidID);
                    if (fluid == null) {
                        error(fluidID + " doesn't exist in all fluids, so skip this");
                        return;
                    }
                    entry.liquid = fluid;
                } else {
                    error("Can't recognize an item from <" + map + ">");
                    return;
                }
                float amount = ParserUtils.parseFloat(map.get("amount"));
                entry.amount = amount;
                if (amount <= 0f) {
                    error("Fluids amount is +" + amount + " <= 0, so reset as 1.0f");
                    entry.amount = 1f;
                }
                res.fluids = ParserUtils.addLiquidStack(res.fluids, entry);
            } catch (Exception e) {
                error("Can't parse <" + map + ">, so skip it", e);
            }
        }

        @SuppressWarnings("rawtypes")
        private void parsePayloadMap(Map map, IOEntry res) {
            try {
                PayloadStack entry = new PayloadStack();
                Object payloadID = map.get("payload");
                if (payloadID instanceof String) {
                    UnlockableContent payload = ContentResolver.findPayload((String) payloadID);
                    if (payload == null) {
                        error("<" + payloadID + "> doesn't exist in all payloads, so skip this");
                        return;
                    }
                    entry.item = payload;
                } else {
                    error("Can't recognize a fluid from <" + map + ">");
                    return;
                }
                int amount = ParserUtils.parseInt(map.get("amount"));
                entry.amount = amount;
                if (amount <= 0) {
                    error("Payload amount is +" + amount + " <= 0, so reset as 1");
                    entry.amount = 1;
                }
                res.payloads = ParserUtils.addPayloadStack(res.payloads, entry);
            } catch (Exception e) {
                error("Can't parse an item <" + map + ">, so skip it", e);
            }
        }


        private void error(String content) {
            error(content, null);
        }

        private void error(String content, @Nullable Throwable e) {
            String message = buildRecipeIndexInfo() + content;
            errors.add(message);
            if (e == null) {
                Log.err(message);
            } else {
                Log.err(message, e);
            }
        }

        private void warn(String content) {
            String message = buildRecipeIndexInfo() + content;
            warnings.add(message);
            Log.warn(message);
        }

        private String buildRecipeIndexInfo() {
            return "[" + curBlock + "](at recipe " + recipeIndex + ")\n";
        }

        public static String genName(Block meta) {
            if (meta.localizedName.equals(meta.name)) {
                return meta.name;
            } else {
                return meta.localizedName + "(" + meta.name + ")";
            }
        }

        private Prov<TextureRegion> findIcon(String name) {
            Prov<TextureRegion> icon = ContentResolver.findIcon(name);
            if (icon == null) {
                error("Icon <" + name + "> not found, so use a cross instead.");
                icon = NotFound;
            }
            return icon;
        }

        private static final Effect[] EffectType = new Effect[0];

        private static Effect composeMultiFx(List<String> names) {
            ArrayList<Effect> all = new ArrayList<>();
            for (String name : names) {
                Effect fx = ContentResolver.findFx(name);
                if (fx != null) all.add(fx);
            }
            return new MultiEffect(all.toArray(EffectType));
        }

        @SuppressWarnings("unchecked")
        @Nullable
        private static Effect parseFx(Object obj) {
            if (obj instanceof String) return ContentResolver.findFx((String) obj);
            else if (obj instanceof List) {
                return composeMultiFx((List<String>) obj);
            } else {
                return null;
            }
        }

        @SuppressWarnings("rawtypes")
        @Nullable
        private static Object findValueByAlias(Map map, String... aliases) {
            for (String alias : aliases) {
                Object tried = map.get(alias);
                if (tried != null) {
                    return tried;
                }
            }
            return null;
        }
    }

    public static class IOEntry {
        public ItemStack[] items = ItemStack.empty;
        public LiquidStack[] fluids = LiquidStack.empty;
        public float power = 0f;
        public float heat = 0f;
        public PayloadStack[] payloads = {}; // Equivalent of empty

        public ObjectSet<Item> itemsUnique = new ObjectSet<>();
        public ObjectSet<Liquid> fluidsUnique = new ObjectSet<>();
        public ObjectSet<UnlockableContent> payloadsUnique = new ObjectSet<>();
        @Nullable
        public Prov<TextureRegion> icon;
        @Nullable
        public Color iconColor;

        public IOEntry() {}

        public void cacheUnique() {
            for (ItemStack item : items) {
                itemsUnique.add(item.item);
            }
            for (LiquidStack fluid : fluids) {
                fluidsUnique.add(fluid.liquid);
            }
            for (PayloadStack payload : payloads) {
                // "item" can be any UnlockableContent
                payloadsUnique.add(payload.item);
            }
        }

        public boolean isEmpty() {
            return items.length == 0 && fluids.length == 0
                    && power <= 0f && heat <= 0f && payloads.length == 0;
        }

        public int maxItemAmount() {
            int max = 0;
            for (ItemStack item : items) {
                max = Math.max(item.amount, max);
            }
            return max;
        }

        public float maxFluidAmount() {
            float max = 0;
            for (LiquidStack fluid : fluids) {
                max = Math.max(fluid.amount, max);
            }
            return max;
        }

        public int maxPayloadAmount() {
            int max = 0;
            for (PayloadStack payload : payloads) {
                max = Math.max(payload.amount, max);
            }
            return max;
        }

        @Override
        public String toString() {
            return "IOEntry{" +
                    "items=" + items +
                    "fluids=" + fluids +
                    "power=" + power +
                    "heat=" + heat +
                    "payloads=" + payloads +
                    "}";
        }
    }

    public static abstract class RecipeSwitchStyle {
        public static HashMap<String, RecipeSwitchStyle> all = new HashMap<>();

        public static RecipeSwitchStyle get(@Nullable String name) {
            if (name == null) return transform;
            RecipeSwitchStyle inMap = all.get(name.toLowerCase());
            if (inMap == null) return transform;
            else return inMap;
        }

        public RecipeSwitchStyle(String name) {
            all.put(name.toLowerCase(), this);
        }

        public abstract void build(MultiCrafterD b, MultiCrafterBuildD c, Table table);

        public static Image getDefaultIcon(MultiCrafterD b, MultiCrafterBuildD c, IOEntry entry) {
            if (entry.icon != null) {
                Image img = new Image(entry.icon.get());
                if (entry.iconColor != null)
                    img.setColor(entry.iconColor);
                return img;
            }
            ItemStack[] items = entry.items;
            LiquidStack[] fluids = entry.fluids;
            boolean outputPower = entry.power > 0f;
            boolean outputHeat = entry.heat > 0f;
            PayloadStack[] payloads = entry.payloads;
            if (items.length > 0) {
                return new Image(items[0].item.uiIcon);
            } else if (fluids.length > 0) {
                return new Image(fluids[0].liquid.uiIcon);
            } else if (outputPower) {
                Image img = new Image(Icon.power.getRegion());
                img.setColor(Pal.power);
                return img;
            } else if (outputHeat) {
                Image img = new Image(Icon.waves.getRegion());
                img.setColor(b.heatColor);
                return img;
            } else if (payloads.length > 0) {
                return new Image(payloads[0].item.uiIcon);
            }
            return new Image(Icon.cancel.getRegion());
        }

        public static RecipeSwitchStyle simple = new RecipeSwitchStyle("simple") {
            @Override
            public void build(MultiCrafterD b, MultiCrafterBuildD c, Table table) {
                Table t = new Table();
                t.background(Tex.whiteui);
                t.setColor(Pal.darkerGray);
                for (int i = 0; i < b.resolvedRecipes.size; i++) {
                    Recipe recipe = b.resolvedRecipes.get(i);
                    int finalI = i;
                    ImageButton button = new ImageButton(Styles.clearTogglei);
                    Image img;
                    if (recipe.icon != null) {
                        img = new Image(recipe.icon.get());
                        if (recipe.iconColor != null)
                            img.setColor(recipe.iconColor);
                    } else {
                        img = getDefaultIcon(b, c, recipe.output);
                    }
                    button.replaceImage(img);
                    button.getImageCell().scaling(Scaling.fit).size(Vars.iconLarge);
                    button.changed(() -> c.configure(finalI));
                    button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                    t.add(button).grow().margin(10f);
                    if (i != 0 && i % 3 == 0) {
                        t.row();
                    }
                }
                table.add(t).grow();
            }
        };

        public static RecipeSwitchStyle number = new RecipeSwitchStyle("number") {
            @Override
            public void build(MultiCrafterD b, MultiCrafterBuildD c, Table table) {
                Table t = new Table();
                for (int i = 0; i < b.resolvedRecipes.size; i++) {
                    Recipe recipe = b.resolvedRecipes.get(i);
                    int finalI = i;
                    TextButton button = Elem.newButton("" + i, Styles.togglet,
                            () -> c.configure(finalI));
                    if (recipe.iconColor != null)
                        button.setColor(recipe.iconColor);
                    button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                    t.add(button).size(50f);
                    if (i != 0 && i % 3 == 0) {
                        t.row();
                    }
                }
                table.add(t).grow();
            }
        };
        public static RecipeSwitchStyle transform = new RecipeSwitchStyle("transform") {
            @Override
            public void build(MultiCrafterD b, MultiCrafterBuildD c, Table table) {
                Table t = new Table();
                for (int i = 0; i < b.resolvedRecipes.size; i++) {
                    if (i != 0 && i % 2 == 0) {
                        t.row();
                    }
                    Recipe recipe = b.resolvedRecipes.get(i);
                    int finalI = i;
                    ImageButton button = new ImageButton(Styles.clearTogglei);
                    Table bt = new Table();
                    Image in = getDefaultIcon(b, c, recipe.input);
                    bt.add(in).pad(6f);
                    bt.image(Icon.right).pad(6f);
                    Image out = getDefaultIcon(b, c, recipe.output);
                    bt.add(out).pad(6f);
                    button.replaceImage(bt);
                    button.changed(() -> c.configure(finalI));
                    button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                    t.add(button).grow().pad(8f).margin(10f);
                }
                table.add(t).grow();
            }
        };

        public static RecipeSwitchStyle detailed = new RecipeSwitchStyle("detailed") {
            @Override
            public void build(MultiCrafterD b, MultiCrafterBuildD c, Table table) {
                for (int i = 0; i < b.resolvedRecipes.size; i++) {
                    Recipe recipe = b.resolvedRecipes.get(i);
                    Table t = new Table();
                    t.background(Tex.whiteui);
                    t.setColor(Pal.darkestGray);
                    b.buildIOEntry(t, recipe, true);
                    t.image(Icon.right);
                    b.buildIOEntry(t, recipe, false);
                    int finalI = i;
                    ImageButton button = new ImageButton(Styles.clearTogglei);
                    button.changed(() -> c.configure(finalI));
                    button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                    button.replaceImage(t);
                    table.add(button).pad(5f).margin(10f).grow();
                    table.row();
                }
            }
        };
    }

    public static class RecipeParserException extends RuntimeException{
        public RecipeParserException() {
            super();
        }

        public RecipeParserException(String message) {
            super(message);
        }

        public RecipeParserException(String message, Throwable cause) {
            super(message, cause);
        }

        public RecipeParserException(Throwable cause) {
            super(cause);
        }

        protected RecipeParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    public static class Recipe {
        public IOEntry input;
        public IOEntry output;
        public float craftTime = 0f;
        @Nullable
        public Prov<TextureRegion> icon;
        @Nullable
        public Color iconColor;

        public Effect craftEffect = Fx.none;

        public Recipe() {}

        public void cacheUnique() {
            input.cacheUnique();
            output.cacheUnique();
        }

        public boolean isConsumeItem() {
            return input.items.length > 0;
        }

        public boolean isOutputItem() {
            return output.items.length > 0;
        }

        public boolean isConsumeFluid() {
            return input.fluids.length > 0;
        }

        public boolean isOutputFluid() {
            return output.fluids.length > 0;
        }

        public boolean isConsumePower() {
            return input.power > 0f;
        }

        public boolean isOutputPower() {
            return output.power > 0f;
        }

        public boolean isConsumeHeat() {
            return input.heat > 0f;
        }

        public boolean isOutputHeat() {
            return output.heat > 0f;
        }

        public boolean isConsumePayload() {
            return input.payloads.length > 0;
        }

        public boolean isOutputPayload() {
            return output.payloads.length > 0;
        }

        public boolean hasItems() {
            return isConsumeItem() || isOutputItem();
        }

        public boolean hasFluids() {
            return isConsumeFluid() || isOutputFluid();
        }

        public boolean hasPower() {
            return isConsumePower() || isOutputPower();
        }

        public boolean hasHeat() {
            return isConsumeHeat() || isOutputHeat();
        }

        public boolean hasPayloads() {
            return isConsumePayload() || isOutputPayload();
        }

        public int maxItemAmount() {
            return Math.max(input.maxItemAmount(), output.maxItemAmount());
        }

        public float maxFluidAmount() {
            return Math.max(input.maxFluidAmount(), output.maxFluidAmount());
        }

        public float maxPower() {
            return Math.max(input.power, output.power);
        }

        public float maxHeat() {
            return Math.max(input.heat, output.heat);
        }

        public int maxPayloadAmount() {
            return Math.max(input.maxPayloadAmount(), output.maxPayloadAmount());
        }

        @Override
        public String toString() {
            return "Recipe{" +
                    "input=" + input +
                    "output=" + output +
                    "craftTime" + craftTime +
                    "}";
        }
    }

    public static class ContentResolver {
        @Nullable
        public static Effect findFx(String name) {
            Object effect = ParserUtils.field(Fx.class, name);
            if (effect instanceof Effect) return (Effect) effect;
            else return null;
        }


        @Nullable
        public static Item findItem(String id) {
            for (Item item : Vars.content.items())
                if (id.equals(item.name)) return item;// prevent null pointer
            return null;
        }

        @Nullable
        public static Liquid findFluid(String id) {
            for (Liquid fluid : Vars.content.liquids())
                if (id.equals(fluid.name)) return fluid;// prevent null pointer
            return null;
        }

        @Nullable
        public static Block findBlock(String id) {
            for (Block block : Vars.content.blocks())
                if (id.equals(block.name)) return block; // prevent null pointer
            return null;
        }

        @Nullable
        public static UnitType findUnit(String id) {
            for (UnitType unit : Vars.content.units())
                if (id.equals(unit.name)) return unit; // prevent null pointer
            return null;
        }

        @Nullable
        public static UnlockableContent findPayload(String id) {
            UnitType unit = findUnit(id);
            if (unit != null) return unit;
            return findBlock(id);
        }


        /**
         * Supported name pattern:
         * <ul>
         *     <li> "Icon.xxx" from {@link Icon}
         *     <li> "copper", "water", "router" or "mono"
         * </ul>
         */
        @Nullable
        public static Prov<TextureRegion> findIcon(String name) {
            if (name.startsWith("Icon.") && name.length() > 5) {
                try {
                    String fieldName = name.substring(5);
                    Field field = Icon.class.getField(fieldName.contains("-") ? ParserUtils.kebab2camel(fieldName) : fieldName);
                    Object icon = field.get(null);
                    TextureRegion tr = ((TextureRegionDrawable) icon).getRegion();
                    return () -> tr;
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return null;
                }
            } else {
                Item item = findItem(name);
                if (item != null) return () -> item.uiIcon;
                Liquid fluid = findFluid(name);
                if (fluid != null) return () -> fluid.uiIcon;
                UnlockableContent payload = findPayload(name);
                if (payload != null) return () -> payload.uiIcon;
                TextureRegion tr = Core.atlas.find(name);
                if (tr.found()) return () -> tr;
            }
            return null;
        }
    }

    public static class ParserUtils {
        public static float parseFloat(@Nullable Object floatObj) {
            if (floatObj == null) return 0f;
            if (floatObj instanceof Number) {
                return ((Number) floatObj).floatValue();
            }
            try {
                return Float.parseFloat((String) floatObj);
            } catch (Exception e) {
                return 0f;
            }
        }

        public static int parseInt(@Nullable Object intObj) {
            if (intObj == null) return 0;
            if (intObj instanceof Number) {
                return ((Number) intObj).intValue();
            }
            try {
                return Integer.parseInt((String) intObj);
            } catch (Exception e) {
                return 0;
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        public static Object parseJsonToObject(Object o) {
            if (o instanceof Seq seq) {
                ArrayList list = new ArrayList(seq.size);
                for (Object e : new Seq.SeqIterable<>(seq)) {
                    list.add(parseJsonToObject(e));
                }
                return list;
            } else if (o instanceof ObjectMap objMap) {
                HashMap map = new HashMap();
                for (ObjectMap.Entry<Object, Object> entry : new ObjectMap.Entries<Object, Object>(objMap)) {
                    map.put(entry.key, parseJsonToObject(entry.value));
                }
                return map;
            } else if (o instanceof JsonValue) {
                return convert((JsonValue) o);
            }
            return o;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Nullable
        private static Object convert(JsonValue j) {
            JsonValue.ValueType type = j.type();
            switch (type) {
                case object: {
                    HashMap map = new HashMap();
                    for (JsonValue cur = j.child; cur != null; cur = cur.next) {
                        map.put(cur.name, convert(cur));
                    }
                    return map;
                }
                case array: {
                    ArrayList list = new ArrayList();
                    for (JsonValue cur = j.child; cur != null; cur = cur.next) {
                        list.add(convert(cur));
                    }
                    return list;
                }
                case stringValue:
                    return j.asString();
                case doubleValue:
                    return j.asDouble();
                case longValue:
                    return j.asLong();
                case booleanValue:
                    return j.asBoolean();
                case nullValue:
                    return null;
            }
            return Collections.emptyMap();
        }

        public static String kebab2camel(String kebab) {
            StringBuilder sb = new StringBuilder();
            boolean hyphen = false;
            for (int i = 0; i < kebab.length(); i++) {
                char c = kebab.charAt(i);
                if (c == '-') {
                    hyphen = true;
                } else {
                    if (hyphen) {
                        sb.append(Character.toUpperCase(c));
                        hyphen = false;
                    } else {
                        if (i == 0) sb.append(Character.toLowerCase(c));
                        else sb.append(c);
                    }
                }
            }
            return sb.toString();
        }

        /**
         * Gets a field from a static class by name, throwing a descriptive exception if not found.
         */
        public static Object field(Class<?> type, String name) {
            try {
                Object b = type.getField(name).get(null);
                if (b == null) throw new IllegalArgumentException(type.getSimpleName() + ": not found: '" + name + "'");
                return b;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static ItemStack[] addItemStack(ItemStack[] stackArray, ItemStack stack) {
            ArrayList<ItemStack> newItemStack = new ArrayList<>(Arrays.asList(stackArray));
            newItemStack.add(stack);
            return newItemStack.toArray(stackArray);
        }

        public static LiquidStack[] addLiquidStack(LiquidStack[] stackArray, LiquidStack stack) {
            ArrayList<LiquidStack> newLiquidStack = new ArrayList<>(Arrays.asList(stackArray));
            newLiquidStack.add(stack);
            return newLiquidStack.toArray(stackArray);
        }

        public static PayloadStack[] addPayloadStack(PayloadStack[] stackArray, PayloadStack stack) {
            ArrayList<PayloadStack> newPayloadStack = new ArrayList<>(Arrays.asList(stackArray));
            newPayloadStack.add(stack);
            return newPayloadStack.toArray(stackArray);
        }
    }
}
