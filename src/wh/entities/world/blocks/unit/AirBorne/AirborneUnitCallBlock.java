package wh.entities.world.blocks.unit.AirBorne;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.struct.IntIntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.*;
import mindustry.world.modules.ItemModule;
import wh.entities.AirborneSpawner;
import wh.entities.WorldRegister;
import wh.ui.ItemImageDynamic;
import wh.ui.UIUtils;

import static mindustry.Vars.tilesize;

public class AirborneUnitCallBlock extends Block{
    public boolean useCoreItems = true;
    public final int GROUP_AMOUNT = 4;

    public Seq<UnitSpacePlan> plans = new Seq<>();

    public float reload = 60f * 45f;
    public float dropLifetime = 180;
    public float range = 600;
    public float spawnRadius = 90;

    public int maxSpawnCount = 3;
    public int maxBoostItemsPerUnit = 20;
    public int buffThresholdItems = 24;
    public int maxSpawnersPerWave = 12;
    public int capacityRecountInterval = 60;
    public float spawnerInterval = 120;

    public Item boostItem = Items.phaseFabric;
    public float shieldPerBoostItem = 20f;
    public StatusEffect boostStatus = StatusEffects.none;
    public float boostStatusDuration = 60f * 10f;

    protected int[] capacities = {};
    protected int[] unitSpaces = {};

    public AirborneUnitCallBlock(String name){
        super(name);
        update = true;
        sync = true;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        logicConfigurable = true;
        copyConfig = true;

        acceptsItems = true;
        hasPower = true;
        hasItems = true;
        separateItemCapacity = true;
        itemCapacity = 120;
        unloadable = true;
        solid = true;
        commandable = true;
        envEnabled = Env.any;
        group = BlockGroup.units;

        config(Integer.class, AirborneUnitCallBuild::setPlan);
        config(UnitType.class, (AirborneUnitCallBuild build, UnitType type) -> {
            int next = plans.indexOf(p -> p.unit == type);
            build.setPlan(next);
        });
        config(String.class, AirborneUnitCallBuild::applyGroupConfig);
        config(Boolean.class, (AirborneUnitCallBuild build, Boolean confirmDeploy) -> {
            if(Boolean.TRUE.equals(confirmDeploy)){
                build.executeConfirmDeploy();
            }
        });
        config(Point2.class, (AirborneUnitCallBuild build, Point2 data) -> build.setAmountAndBoost(data.x, data.y));
        config(Vec2.class, (AirborneUnitCallBuild build, Vec2 pos) -> build.setSpawnPos(pos));
        configClear((AirborneUnitCallBuild build) -> build.commandPos = null);
    }

    public void addPlan(UnitType unit, int space, ItemStack... requirements){
        plans.add(new UnitSpacePlan(unit, space, requirements));
    }

    public int maxCapacityLimit(){
        return Math.max(1, maxSpawnCount * GROUP_AMOUNT);
    }

    @Override
    public void init(){
        super.init();

        capacities = new int[Vars.content.items().size];
        for(UnitSpacePlan plan : plans){
            for(ItemStack stack : plan.requirements){
                int max = Math.max(1, stack.amount) * Math.max(1, maxSpawnCount);
                capacities[stack.item.id] = Math.max(capacities[stack.item.id], max);
            }
        }
        if(boostItem != null){
            capacities[boostItem.id] = Math.max(capacities[boostItem.id], Math.max(1, maxBoostItemsPerUnit) * Math.max(1, maxSpawnCount));
        }

        unitSpaces = new int[Vars.content.units().size];
        for(int i = 0; i < unitSpaces.length; i++){
            unitSpaces[i] = 1;
        }
        for(UnitSpacePlan plan : plans){
            if(plan.unit == null) continue;
            int id = plan.unit.id;
            if(id >= 0 && id < unitSpaces.length){
                unitSpaces[id] = Math.max(unitSpaces[id], Math.max(1, plan.space));
            }
        }

        clipSize = Math.max(clipSize, range + spawnRadius + 32f);
    }

    @Override
    public void getPlanConfigs(Seq<UnlockableContent> options){
        for(UnitSpacePlan plan : plans){
            if(!plan.unit.isBanned()){
                options.add(plan.unit);
            }
        }
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.reload, reload / 60f, StatUnit.seconds);
        stats.add(Stat.maxUnits, maxCapacityLimit(), StatUnit.none);
        stats.add(Stat.booster, table -> {
            table.left();
            table.add("[lightgray]" + Core.bundle.get("wh-airborne-amount") + ": " + maxSpawnCount + " x " + GROUP_AMOUNT + " = " + maxCapacityLimit()).left().row();
            table.add("[lightgray]" + Core.bundle.get("wh-airborne-spawn-interval") + ": " +
            Strings.autoFixed(spawnerInterval / 60f, 2) + "s").left().row();
            if(boostItem != null){
                table.add(Core.bundle.get("wh-airborne-boost") + ": " + boostItem.localizedName).left().row();
                table.add("[lightgray]+" + shieldPerBoostItem + " shield / item, " +
                Core.bundle.get("wh-airborne-buff-threshold") + ": " + buffThresholdItems).left().row();
                table.add("[lightgray]" + Core.bundle.get("wh-airborne-max-boost") + ": " + maxBoostItemsPerUnit).left().row();
                if(boostStatus != null && boostStatus != StatusEffects.none){
                    table.add("[lightgray]buff: " + boostStatus.localizedName +
                    " (" + Strings.autoFixed(boostStatusDuration / 60f, 1) + "s)").left();
                }else{
                    table.add("[lightgray]buff: none").left();
                }
            }
        });
        stats.add(Stat.output, table -> {
            table.left();
            table.defaults().left().growX().pad(4f);
            table.row();
            for(UnitSpacePlan plan : plans){
                table.table(Styles.grayPanel, panel -> {
                    panel.left().top();
                    panel.defaults().left().top();
                    panel.margin(6f);
                    panel.image(plan.unit.uiIcon).size(40f).pad(6f).top();
                    panel.table(info -> {
                        info.left();
                        info.add(plan.unit.localizedName).left().row();
                        info.add("[lightgray]" + Core.bundle.get("wh-airborne-space") + ": " + plan.space).left();
                        info.row();
                        int maxCount = Math.max(1, (maxSpawnCount * GROUP_AMOUNT) / Math.max(1, plan.space));
                        info.add("[lightgray]" + Core.bundle.get("wh-airborne-max-count") + ": " + maxCount).left();
                    }).growX().left().top().pad(4f);
                    panel.table(cost -> {
                        cost.left();
                        for(ItemStack stack : plan.requirements){
                            cost.add(StatValues.stack(stack.item, stack.amount, true)).pad(3f);
                        }
                        if(boostItem != null){
                            cost.add(StatValues.stack(boostItem, 1, true)).pad(3f);
                        }
                    }).left().top().padRight(6f).padLeft(8f);
                }).left().growX().fillX().minWidth(460f).pad(4f);
                table.row();
            }
        });
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("progress", (AirborneUnitCallBuild build) -> new Bar("bar.progress", Pal.ammo, build::progress));
        addBar("capacity", (AirborneUnitCallBuild build) -> new Bar(
        () -> Core.bundle.get("bar.wh-capacity") + ": " + build.usedCapacity + "/" + build.capacityLimit(),
        () -> Pal.techBlue,
        () -> build.capacityLimit() <= 0 ? 0f : Mathf.clamp((float)build.usedCapacity / build.capacityLimit())
        ));
       /* addBar("units", (AirborneUnitCallBuild build) -> new Bar(
        () -> build.unit() == null ? "[lightgray]" + Iconc.cancel :
        Core.bundle.format("bar.unitcap",
        Fonts.getUnicodeStr(build.unit().name),
        build.team.data().countType(build.unit()),
        build.unit().useUnitCap ? Units.getStringCap(build.team) : "infinite"
        ),
        () -> Pal.power,
        () -> build.unit() == null ? 0f : (build.unit().useUnitCap ? ((float)build.team.data().countType(build.unit()) / Units.getCap(build.team)) : 1f)
        ));*/
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        if(WorldRegister.blockCount(this) > 0){
            drawPlaceText(Core.bundle.get("wh-airborne-only-one"), tile.x, tile.y, false);
            return false;
        }
        return super.canPlaceOn(tile, team, rotation);
    }

    public static class UnitSpacePlan{
        public UnitType unit;
        public int space;
        public ItemStack[] requirements;

        public UnitSpacePlan(UnitType unit, int space, ItemStack[] requirements){
            this.unit = unit;
            this.space = Math.max(1, space);
            this.requirements = requirements == null ? ItemStack.empty : requirements;
        }
    }

    public class AirborneUnitCallBuild extends Building{
        public Vec2 spawnPos = new Vec2();
        public @Nullable Vec2 commandPos;

        public float warmup;
        public float buildProgress;
        // 进度达到 100% 后，是否需要玩家手动点击“确定”才执行投放。
        public boolean needConfirmDeploy = true;

        public int currentPlan = -1;
        public Seq<IntSeq> activeGroups = new Seq<>();
        public Seq<IntSeq> pendingGroups = new Seq<>();
        public int editingGroup = 0;
        public int spawnAmount = 1;
        public int boostItemsPerUnit = 0;
        public int pendingSpawnAmount = 1;
        public int pendingBoostItemsPerUnit = 0;

        public int activeCapacity;
        public int usedCapacity;
        public long spawnFlag;
        public @Nullable Image dragGhost;

        protected final Seq<PendingCapacity> pendingCaps = new Seq<>();
        protected final Interval timer = new Interval();

        @Override
        public void add(){
            if(!added){
                WorldRegister.registerBuild(this);
            }
            super.add();
        }

        @Override
        public void remove(){
            if(added){
                WorldRegister.unregisterBuild(this);
            }
            super.remove();
        }

        @Override
        public void created(){
            super.created();
            if(currentPlan == -1){
                currentPlan = defaultPlan();
            }
            ensureGroupRows(activeGroups, spawnAmount);
            if(activeGroups.get(0).isEmpty()){
                activeGroups.get(0).add(currentPlan);
            }
            for(int i = 1; i < activeGroups.size; i++){
                if(activeGroups.get(i).isEmpty()){
                    activeGroups.get(i).add(activeGroups.get(0).get(0));
                }
            }
            copyGroups(activeGroups, pendingGroups);
            pendingSpawnAmount = spawnAmount;
            pendingBoostItemsPerUnit = boostItemsPerUnit;
            editingGroup = Mathf.clamp(editingGroup, 0, Math.max(0, pendingGroups.size - 1));
            if(spawnPos == null || spawnPos.isNaN()){
                spawnPos = new Vec2(x, y);
            }
            clampSpawnPos();
            if(spawnFlag == 0L){
                spawnFlag = composeSpawnFlag();
            }
        }

        public int defaultPlan(){
            int next = plans.indexOf(p -> p.unit.unlockedNow() && !p.unit.isBanned());
            if(next == -1) next = plans.indexOf(p -> !p.unit.isBanned());
            if(next == -1 && !plans.isEmpty()) next = 0;
            return next;
        }

        public UnitSpacePlan plan(){
            if(currentPlan < 0 || currentPlan >= plans.size) return null;
            return plans.get(currentPlan);
        }

        public UnitType unit(){
            UnitSpacePlan p = plan();
            return p == null ? null : p.unit;
        }

        public void setPlan(int next){
            if(isPlanLocked()) return;

            if(next < 0 || next >= plans.size){
                currentPlan = defaultPlan();
            }else{
                currentPlan = next;
            }
            ensureGroupRows(activeGroups, spawnAmount);
            for(int i = 0; i < activeGroups.size; i++){
                IntSeq group = activeGroups.get(i);
                group.clear();
                group.add(currentPlan);
            }
            copyGroups(activeGroups, pendingGroups);
            pendingSpawnAmount = spawnAmount;
            pendingBoostItemsPerUnit = boostItemsPerUnit;
            editingGroup = 0;
            buildProgress = 0f;
            needConfirmDeploy = false;
        }

        public void applyGroupConfig(String data){
            if(isPlanLocked()) return;
            int targetAmount = spawnAmount;
            int targetBoost = boostItemsPerUnit;
            String groupData = data;

            if(data != null){
                int split = data.indexOf('|');
                if(split >= 0){
                    String header = data.substring(0, split).trim();
                    groupData = data.substring(split + 1);
                    String[] values = header.split(",");
                    if(values.length > 0){
                        targetAmount = Mathf.clamp(Strings.parseInt(values[0], spawnAmount), 1, Math.max(1, maxSpawnCount));
                    }
                    if(values.length > 1){
                        targetBoost = Mathf.clamp(Strings.parseInt(values[1], boostItemsPerUnit), 0, Math.max(0, maxBoostItemsPerUnit));
                    }
                }
            }

            spawnAmount = targetAmount;
            boostItemsPerUnit = targetBoost;

            Seq<IntSeq> parsed = parseGroups(groupData);
            activeGroups.clear();
            if(parsed.isEmpty()){
                // Allow applying an intentionally empty draft like "1,0|".
                ensureGroupRows(activeGroups, spawnAmount);
            }else{
                for(IntSeq group : parsed){
                    IntSeq copy = new IntSeq();
                    copy.addAll(group);
                    activeGroups.add(copy);
                }
            }
            int parsedRows = activeGroups.size;
            ensureGroupRows(activeGroups, spawnAmount);
            if(spawnAmount > parsedRows){
                seedNewGroups(activeGroups, parsedRows, spawnAmount);
            }
            syncCurrentPlanFromActive();
            copyGroups(activeGroups, pendingGroups);
            pendingSpawnAmount = spawnAmount;
            pendingBoostItemsPerUnit = boostItemsPerUnit;
            editingGroup = Mathf.clamp(editingGroup, 0, Math.max(0, pendingGroups.size - 1));
            buildProgress = 0f;
            needConfirmDeploy = false;
        }

        public Seq<IntSeq> parseGroups(String data){
            return AirborneGroupCodec.parseGroups(data, plans.size, GROUP_AMOUNT);
        }

        public void ensureGroupRows(Seq<IntSeq> groups, int rows){
            AirborneGroupCodec.ensureRows(groups, rows, plans.size, GROUP_AMOUNT);
        }

        public void copyGroups(Seq<IntSeq> source, Seq<IntSeq> target){
            AirborneGroupCodec.copyGroups(source, target);
        }

        public void seedNewGroups(Seq<IntSeq> groups, int oldRows, int newRows){
            ensureGroupRows(groups, newRows);
            if(newRows <= oldRows) return;

            IntSeq template = null;
            for(int i = 0; i < Math.min(oldRows, groups.size); i++){
                IntSeq candidate = groups.get(i);
                if(candidate != null && !candidate.isEmpty()){
                    template = candidate;
                    break;
                }
            }

            int fallback = (currentPlan >= 0 && currentPlan < plans.size) ? currentPlan : defaultPlan();

            for(int g = oldRows; g < newRows && g < groups.size; g++){
                IntSeq target = groups.get(g);
                if(target == null || !target.isEmpty()) continue;

                if(template != null){
                    int used = 0;
                    for(int i = 0; i < template.size; i++){
                        int idx = template.get(i);
                        if(idx < 0 || idx >= plans.size) continue;
                        int span = Math.max(1, plans.get(idx).space);
                        if(used + span > GROUP_AMOUNT) break;
                        target.add(idx);
                        used += span;
                        if(used >= GROUP_AMOUNT) break;
                    }
                }

                if(target.isEmpty() && fallback >= 0){
                    target.add(fallback);
                }
            }
        }

        public void setAmountAndBoost(int amount, int boost){
            int oldAmount = spawnAmount;
            spawnAmount = Mathf.clamp(amount, 1, Math.max(1, maxSpawnCount));
            boostItemsPerUnit = Mathf.clamp(boost, 0, Math.max(0, maxBoostItemsPerUnit));
            seedNewGroups(activeGroups, oldAmount, spawnAmount);
            if(activeGroups.isEmpty()){
                activeGroups.add(new IntSeq());
            }
            syncCurrentPlanFromActive();
            copyGroups(activeGroups, pendingGroups);
            pendingSpawnAmount = spawnAmount;
            pendingBoostItemsPerUnit = boostItemsPerUnit;
            ensureGroupRows(pendingGroups, pendingSpawnAmount);
            editingGroup = Mathf.clamp(editingGroup, 0, Math.max(0, pendingGroups.size - 1));
            buildProgress = 0f;
            needConfirmDeploy = false;
        }

        public int capacityLimit(){
            // 容量上限固定为方块设计上限，避免重置召唤数量后出现 9/4 这类误导。
            return Math.max(1, maxCapacityLimit());
        }

        public int configuredCapacity(Seq<IntSeq> groups, int groupAmount){
            return AirborneGroupLogic.configuredCapacity(groups, groupAmount, plans, GROUP_AMOUNT, -1);
        }

        public void setSpawnPos(Vec2 pos){
            if(pos == null) return;
            spawnPos.set(pos);
            clampSpawnPos();
        }

        @Override
        public Vec2 getCommandPosition(){
            return commandPos;
        }

        @Override
        public void onCommand(Vec2 target){
            if(target == null){
                commandPos = null;
            }else{
                commandPos = target.cpy();
            }
        }

        public boolean hasCommandPos(){
            return commandPos != null && !commandPos.isNaN();
        }

        @Override
        public Object config(){
            // 序列化当前已生效(active)配置，用于配置同步与保存。
            return activeConfigToString();
        }

        @Override
        public Object senseObject(LAccess sensor){
            if(sensor == LAccess.config){
                return unit();
            }
            return super.senseObject(sensor);
        }

        @Override
        public int getMaximumAccepted(Item item){
            if(item.id < 0 || item.id >= capacities.length) return 0;
            return capacities[item.id];
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if(useCoreItems && team.data().hasCore()) return false;
            return itemNeeded(item) && items.get(item) < getMaximumAccepted(item);
        }

        public boolean itemNeeded(Item item){
            if(boostItem == item) return true;
            for(UnitSpacePlan p : plans){
                for(ItemStack stack : p.requirements){
                    if(stack.item == item) return true;
                }
            }
            return false;
        }

        public ItemModule realItems(){
            return useCoreItems && team.data().hasCore() ? team.core().items : items;
        }

        @Override
        public void updateTile(){
            super.updateTile();
            clampSpawnPos();
            normalizeCurrentPlan();
            validateGroups();
            updateDisplayedCapacity();
            updateBuildProgressState();
        }

        public void validateGroups(){
            pendingSpawnAmount = Mathf.clamp(pendingSpawnAmount, 1, Math.max(1, maxSpawnCount));
            pendingBoostItemsPerUnit = Mathf.clamp(pendingBoostItemsPerUnit, 0, Math.max(0, maxBoostItemsPerUnit));
            ensureGroupRows(activeGroups, spawnAmount);
            ensureGroupRows(pendingGroups, pendingSpawnAmount);

            if(activeGroups.isEmpty()){
                activeGroups.add(new IntSeq());
            }
            syncCurrentPlanFromActive();

            // Keep pending as a real draft; do not force-fill group 1 slot 1.
            ensurePendingDraft();
            editingGroup = Mathf.clamp(editingGroup, 0, Math.max(0, pendingSpawnAmount - 1));
        }

        private void normalizeCurrentPlan(){
            // keep -1 as a valid "no active unit selected" state
            if(currentPlan < -1 || currentPlan >= plans.size){
                currentPlan = defaultPlan();
            }
        }

        private void ensurePendingDraft(){
            // Initialize pending draft from active groups only when pending is empty.
            if(pendingGroups.isEmpty()){
                copyGroups(activeGroups, pendingGroups);
            }
        }

        private void syncCurrentPlanFromActive(){
            int next = -1;
            for(IntSeq group : activeGroups){
                if(group == null || group.isEmpty()) continue;
                int idx = group.get(0);
                if(idx >= 0 && idx < plans.size){
                    next = idx;
                    break;
                }
            }
            currentPlan = next;
        }

        private void updateDisplayedCapacity(){
            updatePendingCapacity();
            // Capacity display follows the pending draft while editing.
            activeCapacity = 0;
            usedCapacity = configuredCapacity(pendingGroups, pendingSpawnAmount);
        }

        private void updateBuildProgressState(){
            float targetWarmup = efficiency > 0.001f && power.status > 0.5f ? 1f : 0f;
            warmup = Mathf.lerpDelta(warmup, targetWarmup, targetWarmup > 0f ? 0.02f : 0.08f);
            if(currentPlan == -1 || plan() == null){
                buildProgress = 0f;
                return;
            }
            // When waiting for deploy confirmation, keep progress at 100%.
            if(needConfirmDeploy){
                buildProgress = 1f;
                return;
            }
            if(warmup > 0.999f){
                buildProgress += edelta() / Math.max(1f, reload);
            }
            // Clamp at 100%, then switch to confirm-deploy state.
            if(buildProgress >= 1f){
                buildProgress = 1f;
                needConfirmDeploy = true;
            }
            buildProgress = Mathf.clamp(buildProgress, 0f, 1f);
        }

        public boolean isPlanLocked(){
            return buildProgress > 0.001f && buildProgress < 0.999f;
        }

        public int spawnWave(int request){
            if(activeGroups.isEmpty()) return 0;
            IntSeq firstGroup = firstConfiguredGroup();
            int fallback = fallbackPlan(firstGroup);
            int requestGroups = Math.max(1, request);
            int scheduledWaveCount = 0;
            int reservedUnitCap = 0;
            int spawned = 0;
            for(int groupIndex = 0; groupIndex < requestGroups; groupIndex++){
                IntSeq group = configuredGroupAt(groupIndex);
                if(!hasConfiguredLead(group, firstGroup, fallback)){
                    continue;
                }
                PreparedGroupSpawn prepared = prepareGroupSpawn(group, firstGroup, fallback, reservedUnitCap, scheduledWaveCount);
                if(prepared == null) break;
                spawned += dispatchPreparedGroup(prepared, scheduledWaveCount);
                scheduledWaveCount++;
                reservedUnitCap += prepared.capReserved;
            }
            return spawned;
        }

        private @Nullable IntSeq firstConfiguredGroup(){
            return activeGroups.isEmpty() ? null : activeGroups.first();
        }

        private int fallbackPlan(@Nullable IntSeq firstGroup){
            return (firstGroup != null && !firstGroup.isEmpty()) ? firstGroup.get(0) : -1;
        }

        private @Nullable IntSeq configuredGroupAt(int groupIndex){
            return groupIndex < activeGroups.size ? activeGroups.get(groupIndex) : null;
        }

        private int dispatchPreparedGroup(PreparedGroupSpawn prepared, int waveIndex){
            consumeGroupItems(prepared.requests);
            scheduleGroupSpawn(prepared, waveIndex);
            addPendingCapacity(prepared.space);
            return prepared.units;
        }

        public IntIntMap collectRequests(Seq<IntSeq> groups, int requestGroups){
            return AirborneGroupLogic.collectRequests(groups, requestGroups, plans, GROUP_AMOUNT, -1);
        }

        private boolean hasConfiguredLead(@Nullable IntSeq group, @Nullable IntSeq firstGroup, int fallback){
            for(int slot = 0; slot < GROUP_AMOUNT; slot++){
                int idx = slotLeadPlan(group, slot, firstGroup, fallback);
                if(idx >= 0 && idx < plans.size){
                    return true;
                }
            }
            return false;
        }

        public int slotPlan(@Nullable IntSeq group, int slot, @Nullable IntSeq firstGroup, int fallback){
            return AirborneGroupLogic.slotPlan(group, slot, firstGroup, fallback, plans, GROUP_AMOUNT);
        }

        public int slotLeadPlan(@Nullable IntSeq group, int slot, @Nullable IntSeq firstGroup, int fallback){
            return AirborneGroupLogic.slotLeadPlan(group, slot, firstGroup, fallback, plans, GROUP_AMOUNT);
        }

        public float unitCostMultiplier(){
            return Vars.state == null || Vars.state.rules == null ? 1f : Vars.state.rules.unitCost(team);
        }

        public int maxAffordableByItems(UnitSpacePlan plan, int wanted){
            return maxAffordableByItems(plan, wanted, boostItemsPerUnit);
        }

        public int maxAffordableByItems(UnitSpacePlan plan, int wanted, int boostPerUnit){
            ItemModule module = realItems();
            float unitCost = unitCostMultiplier();

            int out = wanted;
            for(ItemStack stack : plan.requirements){
                int perUnit = Mathf.ceil(stack.amount * unitCost);
                if(perUnit <= 0) continue;
                out = Math.min(out, module.get(stack.item) / perUnit);
                if(out <= 0) return 0;
            }

            if(boostItem != null && boostPerUnit > 0){
                out = Math.min(out, module.get(boostItem) / boostPerUnit);
            }

            return Math.max(0, out);
        }

        // 按单位上限估算：当前最多还能生成多少该单位。
        public int maxAffordableByUnitCap(UnitType unitType, int wanted){
            return maxAffordableByUnitCap(unitType, wanted, 0);
        }

        public int maxAffordableByUnitCap(UnitType unitType, int wanted, int reservedUnits){
            if(unitType == null || wanted <= 0) return 0;
            if(!Units.canCreate(team, unitType)) return 0;
            if(!unitType.useUnitCap) return wanted;
            if(Vars.state == null || Vars.state.rules == null) return 0;
            if(team == Vars.state.rules.waveTeam) return wanted;

            int left = Math.max(0, team.data().unitCap - team.data().unitCount - Math.max(0, reservedUnits));
            return Math.min(wanted, left);
        }

        public boolean canAffordPlan(UnitSpacePlan plan, int amount){
            return canAffordPlan(plan, amount, boostItemsPerUnit);
        }

        public boolean canAffordPlan(UnitSpacePlan plan, int amount, int boostPerUnit){
            return maxAffordableByItems(plan, Math.max(1, amount), boostPerUnit) >= Math.max(1, amount);
        }

        public void consumeItems(UnitSpacePlan plan, int amount){
            ItemModule module = realItems();
            float unitCost = unitCostMultiplier();

            for(ItemStack stack : plan.requirements){
                int perUnit = Mathf.ceil(stack.amount * unitCost);
                if(perUnit <= 0) continue;
                module.remove(stack.item, perUnit * amount);
            }

            if(boostItem != null && boostItemsPerUnit > 0){
                module.remove(boostItem, boostItemsPerUnit * amount);
            }
        }

        public void consumeGroupItems(IntIntMap requests){
            for(IntIntMap.Entry entry : requests){
                if(entry.key < 0 || entry.key >= plans.size || entry.value <= 0) continue;
                consumeItems(plans.get(entry.key), entry.value);
            }
        }

        public @Nullable PreparedGroupSpawn prepareGroupSpawn(@Nullable IntSeq group, @Nullable IntSeq firstGroup, int fallback, int reservedUnitCap, int scheduledWaveCount){
            if(scheduledWaveCount >= maxSpawnersPerWave) return null;

            Seq<UnitType> loadout = new Seq<>();
            IntIntMap requests = new IntIntMap();
            int space = 0;
            int capReserved = 0;
            int pendingUsed = pendingCapacity();

            for(int slot = 0; slot < GROUP_AMOUNT; slot++){
                int idx = slotLeadPlan(group, slot, firstGroup, fallback);
                if(idx < 0 || idx >= plans.size) continue;

                UnitSpacePlan plan = plans.get(idx);
                if(plan.unit == null || plan.unit.isBanned()) return null;
                if(!Units.canCreate(team, plan.unit)) return null;

                loadout.add(plan.unit);
                requests.put(idx, requests.get(idx, 0) + 1);
                space += Math.max(1, plan.space);
                if(plan.unit.useUnitCap){
                    capReserved++;
                }
            }

            if(loadout.isEmpty()) return null;
            if(space > Math.max(0, capacityLimit() - pendingUsed)) return null;
            if(capReserved > 0 && remainingUnitCap() - reservedUnitCap < capReserved) return null;

            for(IntIntMap.Entry entry : requests){
                if(entry.key < 0 || entry.key >= plans.size || entry.value <= 0) continue;
                if(maxAffordableByItems(plans.get(entry.key), entry.value, pendingBoostItemsPerUnit) < entry.value){
                    return null;
                }
            }

            return new PreparedGroupSpawn(loadout.toArray(UnitType.class), requests, space, loadout.size, capReserved);
        }

        public void scheduleGroupSpawn(PreparedGroupSpawn prepared, int waveIndex){
            float extraShieldPerUnit = extraShieldPerUnit();
            boolean applyBuff = shouldApplyBuff(prepared.units);

            Runnable spawnTask = () -> {
                if(Vars.net.client()) return;

                Vec2 dropPos = randomDropPos();
                float rotation = hasCommandPos() ? Angles.angle(dropPos.x, dropPos.y, commandPos.x, commandPos.y) : Angles.angle(x, y, dropPos.x, dropPos.y);

                AirborneSpawner spawner = new AirborneSpawner();
                spawner.init(team, dropPos, rotation, dropLifetime, prepared.loadout);
                spawner.setFlagToApply(spawnFlag);
                if(extraShieldPerUnit > 0f){
                    spawner.setShieldToApply(extraShieldPerUnit);
                }
                if(applyBuff && boostStatus != null && boostStatus != StatusEffects.none){
                    spawner.setStatus(boostStatus, boostStatusDuration);
                }
                if(hasCommandPos()){
                    spawner.commandPos.set(commandPos);
                }
                spawner.add();
            };

            if(spawnerInterval > 0.001f){
                Time.run(spawnerInterval * waveIndex, spawnTask);
            }else{
                spawnTask.run();
            }
        }

        public float extraShieldPerUnit(){
            return extraShieldPerUnit(boostItemsPerUnit);
        }

        public float extraShieldPerUnit(int boostPerUnit){
            if(boostItem == null || boostPerUnit <= 0 || shieldPerBoostItem <= 0f) return 0f;
            return boostPerUnit * shieldPerBoostItem;
        }

        public boolean shouldApplyBuff(int spawnedUnits){
            return shouldApplyBuff(spawnedUnits, boostItemsPerUnit);
        }

        public boolean shouldApplyBuff(int spawnedUnits, int boostPerUnit){
            if(boostItem == null || boostPerUnit <= 0) return false;
            return boostPerUnit * spawnedUnits >= buffThresholdItems;
        }

        public Vec2 randomDropPos(){
            Vec2 center = spawnPos == null ? new Vec2(x, y) : spawnPos;
            Tmp.v1.trns(Mathf.random(360f), Mathf.random(Math.max(0f, spawnRadius)));
            float wx = Mathf.clamp(center.x + Tmp.v1.x, 0f, Vars.world.unitWidth());
            float wy = Mathf.clamp(center.y + Tmp.v1.y, 0f, Vars.world.unitHeight());
            return new Vec2(wx, wy);
        }

        public void clampSpawnPos(){
            if(spawnPos == null){
                spawnPos = new Vec2(x, y);
                return;
            }
            if(spawnPos.isNaN()){
                spawnPos.set(x, y);
            }
            if(spawnPos.dst(this) > range){
                spawnPos.sub(this).setLength(range).add(this);
            }
        }

        public void recountActiveCapacity(){
            int previousActive = activeCapacity;
            int sum = 0;
            for(Unit unit : Groups.unit){
                if(unit == null || unit.dead || !unit.isAdded()) continue;
                if(unit.team != team) continue;
                if(Double.doubleToLongBits(unit.flag()) != spawnFlag) continue;
                int space = 1;
                int unitId = unit.type == null ? -1 : unit.type.id;
                if(unitId >= 0 && unitId < unitSpaces.length){
                    space = unitSpaces[unitId];
                }
                sum += Math.max(1, space);
            }
            activeCapacity = sum;
            reconcilePendingCapacity(previousActive, activeCapacity);
        }

        /**
         * 当场上已落地单位增加时，从 pending 预留中扣减对应容量，
         * 避免 active + pending 对同一批单位重复统计。
         */
        public void reconcilePendingCapacity(int previousActive, int currentActive){
            int increased = currentActive - previousActive;
            if(increased <= 0 || pendingCaps.isEmpty()) return;

            for(int i = 0; i < pendingCaps.size && increased > 0; ){
                PendingCapacity pending = pendingCaps.get(i);
                if(pending.amount <= 0){
                    pendingCaps.remove(i);
                    continue;
                }

                int consumed = Math.min(increased, pending.amount);
                pending.amount -= consumed;
                increased -= consumed;

                if(pending.amount <= 0){
                    pendingCaps.remove(i);
                }else{
                    i++;
                }
            }
        }

        public void addPendingCapacity(int amount){
            PendingCapacity p = new PendingCapacity();
            p.amount = Math.max(0, amount);
            p.expire = Time.time + dropLifetime + 40f;
            pendingCaps.add(p);
        }

        public void updatePendingCapacity(){
            for(int i = pendingCaps.size - 1; i >= 0; i--){
                PendingCapacity p = pendingCaps.get(i);
                if(p.amount <= 0 || Time.time >= p.expire){
                    pendingCaps.remove(i);
                }
            }
        }

        public int pendingCapacity(){
            int sum = 0;
            for(PendingCapacity p : pendingCaps){
                sum += p.amount;
            }
            return sum;
        }

        public long composeSpawnFlag(){
            long base = 0x4152424fL;
            long xPart = ((long)tileX() & 0xffffL) << 32;
            long yPart = ((long)tileY() & 0xffffL) << 16;
            long idPart = id & 0xffffL;
            return base ^ xPart ^ yPart ^ idPart;
        }

        @Override
        public void drawConfigure(){
            super.drawConfigure();

            Drawf.dashCircle(x, y, range, team.color);

            Vec2 target = spawnPos == null ? new Vec2(x, y) : spawnPos;
            Drawf.dashCircle(target.x, target.y, spawnRadius, team.color);

            Draw.color(Pal.gray);
            Lines.stroke(3f);
            Lines.line(x, y, target.x, target.y);
            Draw.color(team.color);
            Lines.stroke(1f);
            Lines.line(x, y, target.x, target.y);
            Drawf.square(target.x, target.y, 8f, 45f);

            if(hasCommandPos()){
                Drawf.square(commandPos.x, commandPos.y, 8f, 45f);
            }
            Draw.reset();
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            Drawf.dashCircle(x, y, range, team.color);
        }

        @Override
        public void draw(){
            super.draw();

            if(warmup > 0.01f){
                Draw.z(mindustry.graphics.Layer.effect);
                Draw.color(team.color, Color.white, Mathf.absin(Time.time, 10f, 0.3f));
                Lines.stroke(1.5f * warmup);
                Lines.square(x, y, block.size * tilesize * 0.5f + 4f, Time.time);
                Draw.reset();
            }
        }

        public IntSeq pendingGroup(int groupIndex){
            ensureGroupRows(pendingGroups, pendingSpawnAmount);
            int idx = Mathf.clamp(groupIndex, 0, Math.max(0, pendingGroups.size - 1));
            return pendingGroups.get(idx);
        }

        public void rebuildGroupsPanel(Table groupsPanel){
            groupsPanel.clearChildren();
            groupsPanel.defaults().growX().pad(2f);
            for(int g = 0; g < pendingSpawnAmount; g++){
                int groupIndex = g;
                Table row = new Table(Styles.grayPanel);
                row.defaults().pad(2f);
                row.button(Core.bundle.get("wh-airborne-group-name") + "-" + (groupIndex + 1), Styles.togglet, () -> editingGroup = groupIndex)
                .update(b -> b.setChecked(groupIndex == editingGroup))
                .size(90f, 40f);

                for(int slot = 0; slot < GROUP_AMOUNT; slot++){
                    int finalSlot = slot;
                    Button slotButton = new Button(Styles.black3);
                    slotButton.update(() -> {
                        IntSeq group = pendingGroup(groupIndex);
                        slotButton.clearChildren();
                        int entry = entryAtVisualSlot(group, finalSlot);
                        boolean lead = isLeadVisualSlot(group, finalSlot);
                        if(entry >= 0 && lead){
                            int idx = group.get(entry);
                            slotButton.image(plans.get(idx).unit.uiIcon).size(32f).scaling(Scaling.fit);
                        }else if(entry >= 0){
                            slotButton.image(Icon.lock).size(13f).color(Pal.gray);
                        }else{
                            slotButton.image(Icon.cancel).size(14f).color(Pal.gray);
                        }
                    });
                    slotButton.clicked(() -> {
                        IntSeq selectedGroup = pendingGroup(groupIndex);
                        if(entryAtVisualSlot(selectedGroup, finalSlot) >= 0){
                            removePendingAt(groupIndex, finalSlot);
                        }
                    });
                    slotButton.addListener(new InputListener(){
                        private boolean dragged = false;
                        private UnitType draggingType;

                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                            dragged = false;
                            draggingType = null;
                            IntSeq selectedGroup = pendingGroup(groupIndex);
                            int entry = entryAtVisualSlot(selectedGroup, finalSlot);
                            if(isPlanLocked() || entry < 0) return false;
                            int idx = selectedGroup.get(entry);
                            draggingType = idx >= 0 && idx < plans.size ? plans.get(idx).unit : null;
                            return true;
                        }

                        @Override
                        public void touchDragged(InputEvent event, float x, float y, int pointer){
                            if(dragged){
                                Vec2 stagePos = slotButton.localToStageCoordinates(Tmp.v1.set(x, y));
                                moveDragGhost(stagePos.x, stagePos.y);
                                return;
                            }
                            dragged = true;
                            Vec2 stagePos = slotButton.localToStageCoordinates(Tmp.v1.set(x, y));
                            startDragGhost(draggingType, stagePos.x, stagePos.y);
                        }

                        @Override
                        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button){
                            if(!dragged || isPlanLocked()){
                                clearDragGhost();
                                draggingType = null;
                                return;
                            }
                            IntSeq group = pendingGroup(groupIndex);
                            if(entryAtVisualSlot(group, finalSlot) < 0){
                                clearDragGhost();
                                draggingType = null;
                                return;
                            }

                            Vec2 stagePos = slotButton.localToStageCoordinates(Tmp.v1.set(x, y));
                            moveDragGhost(stagePos.x, stagePos.y);
                            Vec2 local = row.stageToLocalCoordinates(stagePos);
                            boolean outside = local.x < 0f || local.y < 0f || local.x > row.getWidth() || local.y > row.getHeight();
                            if(outside){
                                removePendingAt(groupIndex, finalSlot);
                            }
                            clearDragGhost();
                            draggingType = null;
                        }
                    });
                    row.add(slotButton).size(42f);
                }
                groupsPanel.add(row).growX().row();
            }
        }

        public int groupUsedSlots(IntSeq group){
            return AirborneGroupLogic.groupUsedSlots(group, plans, GROUP_AMOUNT);
        }

        public int entryAtVisualSlot(IntSeq group, int slot){
            return AirborneGroupLogic.entryAtVisualSlot(group, slot, plans, GROUP_AMOUNT);
        }

        public boolean isLeadVisualSlot(IntSeq group, int slot){
            return AirborneGroupLogic.isLeadVisualSlot(group, slot, plans, GROUP_AMOUNT);
        }

        public void appendPendingUnit(int groupIndex, int planIndex){
            if(isPlanLocked()) return;
            if(planIndex < 0 || planIndex >= plans.size) return;
            IntSeq group = pendingGroup(groupIndex);
            UnitSpacePlan targetPlan = plans.get(planIndex);
            int space = Math.max(1, targetPlan.space);
            if(groupUsedSlots(group) + space > GROUP_AMOUNT) return;
            int requiredAfterAppend = pendingUnitCount(planIndex) + 1;
            if(!canAffordPlan(targetPlan, requiredAfterAppend, pendingBoostItemsPerUnit)) return;
            group.add(planIndex);
        }

        public void removePendingAt(int groupIndex, int visualSlot){
            if(isPlanLocked()) return;
            IntSeq group = pendingGroup(groupIndex);
            int entry = entryAtVisualSlot(group, visualSlot);
            if(entry < 0 || entry >= group.size) return;
            group.removeIndex(entry);
        }

        public void startDragGhost(UnitType type, float stageX, float stageY){
            clearDragGhost();
            if(type == null) return;
            Image ghost = new Image(type.uiIcon);
            ghost.setSize(34f, 34f);
            ghost.setOrigin(ghost.getWidth() / 2f, ghost.getHeight() / 2f);
            ghost.setColor(1f, 1f, 1f, 0.9f);
            ghost.touchable = arc.scene.event.Touchable.disabled;
            ghost.setPosition(stageX - ghost.getWidth() / 2f, stageY - ghost.getHeight() / 2f);
            Core.scene.root.addChild(ghost);
            dragGhost = ghost;
        }

        public void moveDragGhost(float stageX, float stageY){
            if(dragGhost == null) return;
            dragGhost.setPosition(stageX - dragGhost.getWidth() / 2f, stageY - dragGhost.getHeight() / 2f);
        }

        public void clearDragGhost(){
            if(dragGhost != null){
                dragGhost.remove();
                dragGhost = null;
            }
        }

        public String pendingGroupsToString(){
            return AirborneGroupCodec.groupsToString(pendingGroups, pendingSpawnAmount, plans.size, GROUP_AMOUNT);
        }

        public String activeGroupsToString(){
            return AirborneGroupCodec.groupsToString(activeGroups, spawnAmount, plans.size, GROUP_AMOUNT);
        }

        // 当前已生效(active)配置字符串格式：数量,强化|分组数据
        public String activeConfigToString(){
            return spawnAmount + "," + boostItemsPerUnit + "|" + activeGroupsToString();
        }

        // 当前待生效(pending)配置字符串格式：数量,强化|分组数据
        public String pendingConfigToString(){
            return pendingSpawnAmount + "," + pendingBoostItemsPerUnit + "|" + pendingGroupsToString();
        }

        // “确定”按钮逻辑：
        // 1) 有改动时，先把 pending 应用到 active（不立即投放）；
        // 2) 无改动且处于待确认状态时，执行投放。
        public void onConfirmPressed(){
            if(isPlanLocked()) return;
            String pendingBlocked = pendingBlockedReason();
            if(pendingBlocked != null){
                showConfirmBlocked(pendingBlocked);
                return;
            }

            // 有改动时先应用配置，避免旧配置被直接投放。
            if(!pendingEqualsActive()){
                configure(pendingConfigToString());
                return;
            }

            // 尚未进入待确认阶段时，不执行投放。
            if(!needConfirmDeploy) return;

            String blockedReason = spawnBlockedReason();
            if(blockedReason != null){
                showConfirmBlocked(blockedReason);
                return;
            }

            // 使用 configure 同步“确认投放”到服务端，避免联机时仅本地执行。
            configure(Boolean.TRUE);
        }

        public boolean isFirstPendingGroupEmpty(){
            ensureGroupRows(pendingGroups, pendingSpawnAmount);
            return pendingGroups.isEmpty() || pendingGroups.get(0).isEmpty();
        }

        // 真正执行投放（由 config(Boolean) 触发）。
        public void executeConfirmDeploy(){
            if(isPlanLocked() || !needConfirmDeploy) return;

            String blockedReason = spawnBlockedReason();
            if(blockedReason != null){
                showConfirmBlocked(blockedReason);
                return;
            }

            int spawned = spawnWave(spawnAmount);
            if(spawned > 0){
                resetSpawnAmountAfterDeploy();
                buildProgress = 0f;
                needConfirmDeploy = false;
            }else{
                buildProgress = 1f;
                needConfirmDeploy = true;
            }
        }

        // 成功投放后保持当前编组模板，仅同步 pending 草稿。
        public void resetSpawnAmountAfterDeploy(){
            ensureGroupRows(activeGroups, Math.max(1, spawnAmount));
            if(activeGroups.isEmpty()){
                activeGroups.add(new IntSeq());
            }
            syncCurrentPlanFromActive();

            copyGroups(activeGroups, pendingGroups);
            pendingSpawnAmount = spawnAmount;
            pendingBoostItemsPerUnit = boostItemsPerUnit;
            ensureGroupRows(pendingGroups, pendingSpawnAmount);
            editingGroup = Mathf.clamp(editingGroup, 0, Math.max(0, pendingGroups.size - 1));
        }

        public @Nullable String pendingBlockedReason(){
            return blockedReasonFor(pendingGroups, pendingSpawnAmount, pendingBoostItemsPerUnit, true);
        }

        public @Nullable String spawnBlockedReason(){
            return blockedReasonFor(activeGroups, spawnAmount, boostItemsPerUnit, false);
        }

        private @Nullable String blockedReasonFor(Seq<IntSeq> groups, int groupAmount, int boostPerUnit, boolean requireFirstGroup){
            int amount = Mathf.clamp(groupAmount, 1, Math.max(1, maxSpawnCount));
            ensureGroupRows(groups, amount);
            if(groups.isEmpty()){
                return Core.bundle.get("wh-airborne-fail-no-unit");
            }
            if(requireFirstGroup && groups.get(0).isEmpty()){
                return Core.bundle.get("wh-airborne-fail-no-unit");
            }

            IntIntMap requests = collectRequests(groups, amount);
            if(requests.isEmpty()){
                return Core.bundle.get("wh-airborne-fail-no-unit");
            }

            int capLimit = capacityLimit();
            int pendingUsed = pendingCapacity();
            int remainingCapacity = Math.max(0, capLimit - pendingUsed);
            int requiredCapacity = configuredCapacity(groups, amount);
            if(remainingCapacity <= 0 || requiredCapacity > remainingCapacity){
                return Core.bundle.format("wh-airborne-fail-capacity", pendingUsed, capLimit);
            }

            int requiredUnitCap = 0;
            for(IntIntMap.Entry entry : requests){
                if(entry.key < 0 || entry.key >= plans.size || entry.value <= 0) continue;
                UnitSpacePlan requestPlan = plans.get(entry.key);
                if(requestPlan.unit != null && requestPlan.unit.useUnitCap){
                    requiredUnitCap += entry.value;
                }
            }
            if(requiredUnitCap > 0 && remainingUnitCap() < requiredUnitCap){
                return Core.bundle.get("wh-airborne-fail-unitcap");
            }

            boolean itemBlocked = false;
            boolean unitCapBlocked = false;
            for(IntIntMap.Entry entry : requests){
                if(entry.key < 0 || entry.key >= plans.size || entry.value <= 0) continue;
                UnitSpacePlan requestPlan = plans.get(entry.key);
                if(requestPlan.unit == null || requestPlan.unit.isBanned()) continue;

                if(!Units.canCreate(team, requestPlan.unit)){
                    unitCapBlocked = true;
                    continue;
                }

                int byItems = maxAffordableByItems(requestPlan, entry.value, boostPerUnit);
                if(byItems <= 0){
                    itemBlocked = true;
                }
            }

            if(itemBlocked){
                return Core.bundle.get("wh-airborne-fail-items");
            }
            if(unitCapBlocked){
                return Core.bundle.get("wh-airborne-fail-unitcap");
            }
            return itemBlocked || unitCapBlocked ? Core.bundle.get("wh-airborne-fail-generic") : null;
        }

        public void showConfirmBlocked(String message){
            if(message == null || message.isEmpty()) return;
            if(Vars.headless || Vars.ui == null) return;
            Vars.ui.showInfoToast(message, 2.8f);
        }

        public int remainingUnitCap(){
            if(Vars.state == null || Vars.state.rules == null) return 0;
            if(team == Vars.state.rules.waveTeam) return Integer.MAX_VALUE;
            return Math.max(0, team.data().unitCap - team.data().unitCount);
        }

        @Override
        public void buildConfiguration(Table table){
            // 首次打开配置面板时，用 active 初始化 pending 草稿。
            // 后续所有操作仅修改 pending，直到点击“确定”。
            if(pendingGroups.isEmpty()){
                copyGroups(activeGroups, pendingGroups);
                pendingSpawnAmount = spawnAmount;
                pendingBoostItemsPerUnit = boostItemsPerUnit;
            }
            pendingSpawnAmount = Mathf.clamp(pendingSpawnAmount, 1, Math.max(1, maxSpawnCount));
            pendingBoostItemsPerUnit = Mathf.clamp(pendingBoostItemsPerUnit, 0, Math.max(0, maxBoostItemsPerUnit));
            ensureGroupRows(pendingGroups, pendingSpawnAmount);
            editingGroup = Mathf.clamp(editingGroup, 0, Math.max(0, pendingSpawnAmount - 1));
            final float panelWidth = 500f;
            final float contentWidth = panelWidth - 8f;

            table.top();
            table.defaults().pad(4f);
            Table root = new Table(Tex.paneSolid);
            root.center();
            root.defaults().growX().pad(3f);
            Table groupsPanel = new Table(Styles.black6);
            rebuildGroupsPanel(groupsPanel);

            root.update(() -> {
                ensureGroupRows(pendingGroups, pendingSpawnAmount);
                editingGroup = Mathf.clamp(editingGroup, 0, Math.max(0, pendingSpawnAmount - 1));
                if(groupsPanel.getChildren().size != pendingSpawnAmount){
                    rebuildGroupsPanel(groupsPanel);
                }
            });

            root.label(() -> Core.bundle.get("wh-airborne-group") + ": " + (editingGroup + 1) + "/" + pendingSpawnAmount).left().padLeft(6f).row();
            root.add(groupsPanel).width(contentWidth).minWidth(contentWidth).maxWidth(contentWidth).padLeft(6f).padRight(6f).row();
            root.add("@wh-airborne-drag-remove").left().padLeft(6f).row();

            root.image().size(contentWidth, 4f).color(Pal.accent).padTop(4f).padBottom(6f).row();

            Table list = new Table();
            list.defaults().growX().pad(2f);
            Button.ButtonStyle unitSelectStyle = new Button.ButtonStyle(Styles.black8, Styles.black8, Styles.black8);
            unitSelectStyle.over = Styles.black8;
            unitSelectStyle.checked = Styles.black8;
            unitSelectStyle.disabled = Styles.black8;

            for(int i = 0; i < plans.size; i++){
                UnitSpacePlan plan = plans.get(i);
                int index = i;
                list.button(button -> {
                    button.table(in -> in.add(planDisplay(index, plan)).growX().fillX()).growX();
                    button.update(() -> {
                        IntSeq selectedGroup = pendingGroup(editingGroup);
                        int used = groupUsedSlots(selectedGroup);
                        int need = Math.max(1, plan.space);
                        boolean hasSpace = used + need <= GROUP_AMOUNT;
                        int requiredAfterAppend = pendingUnitCount(index) + 1;
                        boolean affordableAfterAppend = canAffordPlan(plan, requiredAfterAppend, pendingBoostItemsPerUnit);
                        button.setChecked(selectedGroup.contains(index));
                        button.setDisabled(isPlanLocked() || !hasSpace || !affordableAfterAppend);
                    });
                }, unitSelectStyle, () -> appendPendingUnit(editingGroup, index)).expandX().fillX().margin(0f).pad(4f);
                list.row();
            }

            ScrollPane listPane = new ScrollPane(list);
            listPane.setScrollingDisabled(true, false);
            listPane.setFadeScrollBars(false);
            listPane.setOverscroll(false, false);
            root.add(listPane).width(contentWidth).minWidth(contentWidth).maxWidth(contentWidth).maxHeight(300f).padRight(2f).row();

            root.label(() -> Core.bundle.get("wh-airborne-amount") + ": " + pendingSpawnAmount).left().padLeft(6f).row();
            root.slider(1f, maxSpawnCount, 1f, pendingSpawnAmount, value -> {
                int next = Mathf.round(value);
                if(next != pendingSpawnAmount){
                    int oldAmount = pendingSpawnAmount;
                    pendingSpawnAmount = Mathf.clamp(next, 1, Math.max(1, maxSpawnCount));
                    seedNewGroups(pendingGroups, oldAmount, pendingSpawnAmount);
                    editingGroup = Mathf.clamp(editingGroup, 0, Math.max(0, pendingSpawnAmount - 1));
                    rebuildGroupsPanel(groupsPanel);
                }
            }).growX().padLeft(6f).padRight(6f).row();

            root.table(actions -> {
                actions.defaults().growX().fillX().height(40f).pad(2f);
                actions.button(Core.bundle.get("wh-airborne-detail"), Icon.info, this::openDetailDialog).growX().fillX().row();
                actions.button(Core.bundle.get("wh-airborne-select-pos"), Icon.move, () -> UIUtils.selectPos(root, pos -> {
                    Vec2 worldPos = new Vec2(
                    pos.x * tilesize + tilesize / 2f,
                    pos.y * tilesize + tilesize / 2f
                    );
                    if(worldPos.dst(this) > range){
                        worldPos.sub(this).setLength(range).add(this);
                    }
                    configure(worldPos);
                })).growX().fillX().disabled(b -> isPlanLocked()).row();

                actions.button("@back", Icon.left, () -> {
                    IntSeq selectedGroup = pendingGroup(editingGroup);
                    if(!selectedGroup.isEmpty()){
                        selectedGroup.removeIndex(selectedGroup.size - 1);
                    }
                }).growX().fillX().disabled(b -> isPlanLocked() || pendingGroup(editingGroup).isEmpty()).row();

                actions.button("@clear", Icon.cancel, () -> pendingGroup(editingGroup).clear())
                .growX().fillX().disabled(b -> isPlanLocked() || pendingGroup(editingGroup).isEmpty()).row();

                actions.button("@ok", Icon.ok, this::onConfirmPressed)
                .growX().fillX()
                .disabled(b -> isPlanLocked() || pendingBlockedReason() != null);
            }).width(contentWidth).minWidth(contentWidth).maxWidth(contentWidth).padTop(2f).center().row();

            table.add(root)
            .width(panelWidth).minWidth(panelWidth).maxWidth(panelWidth)
            .center().row();
        }

        // 打开独立“详细信息”弹窗，避免配置面板过于拥挤。
        public void openDetailDialog(){
            final float dialogWidth = 860f;
            final float contentWidth = dialogWidth - 30f;

            BaseDialog dialog = new BaseDialog(Core.bundle.get("wh-airborne-detail"));
            dialog.cont.clear();
            dialog.cont.top();

            Table detailContent = createDetailContent(contentWidth);
            dialog.cont.add(detailContent)
            .width(contentWidth + 10f).minWidth(contentWidth + 10f).maxWidth(contentWidth + 10f)
            .center().top().pad(8f).row();

            dialog.addCloseButton();
            dialog.show();
        }

        // 构建详细信息面板（已选单位、资源消耗、状态信息）。
        public Table createDetailContent(float contentWidth){
            Table detailContent = new Table();
            detailContent.left().top();
            detailContent.defaults().growX().padTop(4f).padBottom(4f);

            detailContent.table(selectedTable -> {
                selectedTable.background(Styles.black6);
                selectedTable.left().top();
                selectedTable.margin(8f);
                selectedTable.defaults().left().growX();
                selectedTable.add("[accent]Selected Units").left().padBottom(4f).row();
                selectedTable.table(selectedList -> {
                    selectedList.left();
                    selectedList.defaults().left().growX().pad(2f);
                    selectedList.update(() -> {
                        selectedList.clearChildren();
                        boolean any = false;
                        for(int i = 0; i < plans.size; i++){
                            int selectedCount = pendingUnitCount(i);
                            if(selectedCount <= 0) continue;
                            any = true;
                            UnitSpacePlan selectedPlan = plans.get(i);
                            selectedList.table(Styles.grayPanel, card -> {
                                card.left().margin(5f);
                                card.image(selectedPlan.unit.uiIcon).size(34f).padRight(8f).scaling(Scaling.fit);
                                card.table(info -> {
                                    info.left();
                                    info.add("[white]" + selectedPlan.unit.localizedName + " [accent]" + selectedCount +
                                    "[lightgray]/[white]" + maxPlanDisplayCount(selectedPlan)).left().row();
                                    info.add("[lightgray]" + Core.bundle.get("wh-airborne-space") + ": [white]" + selectedPlan.space).left();
                                }).growX().left();
                            }).growX().left().row();
                        }
                        if(!any){
                            selectedList.add("[lightgray]-").left();
                        }
                    });
                }).growX().row();
            }).width(contentWidth).minWidth(contentWidth).maxWidth(contentWidth).left().row();

            detailContent.table(detailTable -> {
                detailTable.background(Styles.black6);
                detailTable.left().top();
                detailTable.margin(8f);
                detailTable.defaults().left().growX();
                detailTable.add("[accent]" + Core.bundle.get("wh-airborne-detail")).left().padBottom(4f).row();
                Table detailList = new Table();
                detailList.left();
                detailList.defaults().left().growX().padTop(2f).padBottom(2f);
                detailList.update(() -> {
                    detailList.clearChildren();
                    ItemModule module = realItems();
                    float unitCost = unitCostMultiplier();
                    for(int i = 0; i < plans.size; i++){
                        UnitSpacePlan detailPlan = plans.get(i);
                        int detailIndex = i;
                        detailList.table(Styles.grayPanel, row -> {
                            row.left().top();
                            row.margin(6f);
                            row.defaults().left().top().pad(2f);
                            int unitsInGroup = pendingUnitCount(detailIndex);
                            int boostForPlan = pendingBoostItemsPerUnit * unitsInGroup;
                            int needForBuff = Math.max(0, buffThresholdItems - boostForPlan);
                            String inGroupColor = unitsInGroup > 0 ? "[accent]" : "[lightgray]";

                            row.image(detailPlan.unit.uiIcon).size(38f).pad(3f).scaling(Scaling.fit);
                            row.table(info -> {
                                info.left();
                                info.defaults().left().padBottom(2f);
                                info.add("[white]" + detailPlan.unit.localizedName + "  " +
                                inGroupColor + Core.bundle.get("wh-airborne-in-group") + ": " + unitsInGroup).left().row();
                                info.add("[lightgray]" + Core.bundle.get("wh-airborne-space") + ": [white]" + detailPlan.space +
                                "   [lightgray]" + Core.bundle.get("wh-airborne-max-count") + ": [white]" + maxPlanDisplayCount(detailPlan)).left().row();
                                if(boostItem != null){
                                    String buffState = hasBoostStatusConfigured() && unitsInGroup > 0 && boostForPlan >= buffThresholdItems ? "[accent]active" : "[scarlet]inactive";
                                    info.add("[lightgray]buff: [white]" + boostStatusName() +
                                    "  [lightgray]state: " + buffState).left().row();
                                    info.add("[lightgray]boost: [white]" + boostForPlan +
                                    "  [lightgray]need: [white]" + needForBuff + " " + boostItem.localizedName).left();
                                }
                            }).growX().left().top().pad(2f);
                            row.table(req -> {
                                req.left().top();
                                req.defaults().pad(1.5f);
                                for(ItemStack stack : detailPlan.requirements){
                                    int amount = Mathf.ceil(stack.amount * unitCost);
                                    int finalAmount = amount;
                                    req.add(new ItemImageDynamic(stack.item, () -> finalAmount, module));
                                }
                                if(boostItem != null && pendingBoostItemsPerUnit > 0){
                                    int boostForThisPlan = pendingBoostItemsPerUnit * unitsInGroup;
                                    if(boostForThisPlan > 0){
                                        int finalBoost = boostForThisPlan;
                                        req.add(new ItemImageDynamic(boostItem, () -> finalBoost, module));
                                    }
                                }
                            }).left().top().padLeft(10f);
                        }).growX().left();
                        detailList.row();
                    }
                });

                ScrollPane detailListPane = new ScrollPane(detailList);
                detailListPane.setScrollingDisabled(true, false);
                detailListPane.setFadeScrollBars(false);
                detailListPane.setOverscroll(false, false);
                float detailListHeight = Math.min(Core.graphics.getHeight() * 0.5f, 420f);
                detailTable.add(detailListPane)
                .width(contentWidth - 16f).minWidth(contentWidth - 16f).maxWidth(contentWidth - 16f)
                .maxHeight(detailListHeight)
                .left().row();
            }).width(contentWidth).minWidth(contentWidth).maxWidth(contentWidth).left().row();

            if(boostItem != null && maxBoostItemsPerUnit > 0){
                detailContent.table(boostTable -> {
                    boostTable.background(Styles.black6);
                    boostTable.left();
                    boostTable.margin(8f);
                    boostTable.defaults().left().growX().padTop(2f).padBottom(2f);
                    boostTable.add("[accent]" + Core.bundle.get("wh-airborne-boost")).left().row();
                    boostTable.stack(
                    new UIUtils.PlanBackBar(
                    () -> pendingBoostCost() > 0 && realItems().get(boostItem) < pendingBoostCost() ? Pal.remove : Pal.shield,
                    () -> pendingBoostItemsPerUnit + "/" + maxBoostItemsPerUnit +
                    " x" + pendingTotalUnits() + "=" + pendingBoostCost() +
                    "  +" + Strings.autoFixed(extraShieldPerUnit(pendingBoostItemsPerUnit), 0) + " shield/unit",
                    () -> maxBoostItemsPerUnit <= 0 ? 0f : Mathf.clamp((float)pendingBoostItemsPerUnit / maxBoostItemsPerUnit)
                    ),
                    new Table(icon -> {
                        icon.left();
                        icon.image(boostItem.uiIcon).size(24f).padLeft(8f).padTop(5f).padBottom(5f).scaling(Scaling.fit);
                    })
                    ).height(34f).width(contentWidth - 12f).minWidth(contentWidth - 12f).maxWidth(contentWidth - 12f).fillX().left().row();

                    boostTable.slider(0f, maxBoostItemsPerUnit, 1f, pendingBoostItemsPerUnit, value -> {
                        int next = Mathf.round(value);
                        if(next != pendingBoostItemsPerUnit){
                            pendingBoostItemsPerUnit = Mathf.clamp(next, 0, Math.max(0, maxBoostItemsPerUnit));
                        }
                    }).width(contentWidth - 12f).minWidth(contentWidth - 12f).maxWidth(contentWidth - 12f).left().row();

                    boostTable.label(() -> {
                        String statusText = boostStatus == null || boostStatus == StatusEffects.none ? "none" : boostStatus.localizedName;
                        return "[white]Buff: [accent]" + statusText + "[lightgray]  " +
                        Strings.autoFixed(boostStatusDuration / 60f, 1) + "s";
                    }).left().row();

                    boostTable.label(() -> "[white]" + Core.bundle.get("wh-airborne-buff-threshold") +
                    ": [accent]" + pendingBoostCost() + "[lightgray]/[white]" + buffThresholdItems +
                    "  [lightgray](+" + Strings.autoFixed(extraShieldPerUnit(pendingBoostItemsPerUnit), 0) + " shield/unit)").left().row();

                    boostTable.label(() -> "[white]Need Buff: [accent]" + pendingBuffMissingItems() + "[lightgray] " +
                    (boostItem == null ? "item" : boostItem.localizedName) +
                    (pendingBoostItemsPerUnit > 0 ? "  [white]Need Units: [accent]" + pendingUnitsForBuffTrigger() : "  [scarlet](set boost/unit > 0)")
                    ).left();
                }).width(contentWidth).minWidth(contentWidth).maxWidth(contentWidth).left().row();
            }

            detailContent.table(costTable -> {
                costTable.background(Styles.black6);
                costTable.left();
                costTable.margin(8f);
                costTable.defaults().left().pad(2f);
                costTable.add("[accent]" + Core.bundle.get("wh-airborne-cost")).left().row();
                costTable.table(costItems -> {
                    costItems.left();
                    costItems.update(() -> {
                        costItems.clearChildren();
                        IntIntMap costs = pendingCostMap();
                        ItemModule module = realItems();
                        int shown = 0;
                        for(Item item : Vars.content.items()){
                            int needed = costs.get(item.id, 0);
                            if(needed <= 0) continue;
                            int finalNeeded = needed;
                            costItems.add(new ItemImageDynamic(item, () -> finalNeeded, module)).pad(2f);
                            if(++shown % 6 == 0){
                                costItems.row();
                            }
                        }
                        if(shown == 0){
                            costItems.add("[lightgray]-").left();
                        }
                    });
                }).growX().row();
            }).width(contentWidth).minWidth(contentWidth).maxWidth(contentWidth).left().row();

            detailContent.table(stateTable -> {
                stateTable.background(Styles.black6);
                stateTable.left();
                stateTable.margin(8f);
                stateTable.defaults().left().growX().padTop(2f).padBottom(2f);
                stateTable.table(lockTable -> {
                    lockTable.left();
                    lockTable.update(() -> {
                        lockTable.clearChildren();
                        if(isPlanLocked()){
                            lockTable.add("[scarlet]" + Core.bundle.get("wh-airborne-plan-lock")).left();
                        }
                    });
                }).left().growX().row();
                stateTable.label(() -> "[white]" + Core.bundle.get("bar.progress") + ": [accent]" + Strings.autoFixed(buildProgress * 100f, 0) + "%").left().growX().row();
                stateTable.label(() -> "[white]" + Core.bundle.get("bar.wh-capacity") + ": [accent]" + usedCapacity + "[lightgray]/[white]" + capacityLimit()).left().growX().row();
                stateTable.label(() -> "[white]" + Core.bundle.get("wh-airborne-in-group") + ": [accent]" + pendingTotalUnits()).left().growX().row();
                stateTable.label(() -> "[white]" + Core.bundle.get("wh-airborne-spawn-interval") + ": [accent]" + Strings.autoFixed(spawnerInterval / 60f, 2) + "s").left().growX().row();
                if(boostItem != null){
                    stateTable.table(boostState -> {
                        boostState.left();
                        boostState.add("[white]" + Core.bundle.get("wh-airborne-boost") + ": ").left().padRight(6f);
                        boostState.add(new ItemImageDynamic(boostItem, () -> pendingBoostCost(), realItems())).left().padRight(8f);
                        boostState.label(() -> "[lightgray]" + pendingBoostItemsPerUnit + "/unit  [white]" +
                        Core.bundle.get("wh-airborne-total") + ": [accent]" + pendingBoostCost()).left();
                    }).left().growX().row();
                    stateTable.label(() -> "[white]" + Core.bundle.get("wh-airborne-bonus") + ": [accent]+" + Strings.autoFixed(extraShieldPerUnit(pendingBoostItemsPerUnit), 0) + " shield/unit").left().growX().row();
                    stateTable.label(() -> {
                        String statusText = boostStatus == null || boostStatus == StatusEffects.none ? "none" : boostStatus.localizedName;
                        String activeText = pendingBuffActive() ? "[accent]active" : "[lightgray]inactive";
                        return "[white]Buff State: " + activeText +
                        "[lightgray]  (" + pendingBoostCost() + "/" + buffThresholdItems + ")" +
                        "  [white]Status: [accent]" + statusText +
                        "[lightgray]  " + Strings.autoFixed(boostStatusDuration / 60f, 1) + "s";
                    }).left().growX().row();
                    stateTable.label(() -> "[white]Need Buff: [accent]" + pendingBuffMissingItems() + "[lightgray] " +
                    boostItem.localizedName +
                    (pendingBoostItemsPerUnit > 0 ? "  [white]Need Units: [accent]" + pendingUnitsForBuffTrigger() : "  [scarlet](set boost/unit > 0)")
                    ).left().growX();
                }
            }).width(contentWidth).minWidth(contentWidth).maxWidth(contentWidth).left().row();

            return detailContent;
        }

        public boolean pendingEqualsActive(){
            if(pendingSpawnAmount != spawnAmount) return false;
            if(pendingBoostItemsPerUnit != boostItemsPerUnit) return false;
            return pendingGroupsEqualActive();
        }

        public boolean pendingGroupsEqualActive(){
            ensureGroupRows(activeGroups, spawnAmount);
            ensureGroupRows(pendingGroups, pendingSpawnAmount);
            if(pendingSpawnAmount != spawnAmount) return false;
            if(pendingGroups.size != activeGroups.size) return false;
            for(int g = 0; g < pendingGroups.size; g++){
                IntSeq p = pendingGroups.get(g), a = activeGroups.get(g);
                if(p.size != a.size) return false;
                for(int i = 0; i < p.size; i++){
                    if(p.get(i) != a.get(i)) return false;
                }
            }
            return true;
        }

        public Stack planDisplay(int planIndex, UnitSpacePlan plan){
            return new Stack(
            new UIUtils.PlanBackBar(
            () -> planRowColor(planIndex, plan),
            () -> (hasPlanItemShortage(planIndex, plan) ? "[scarlet]" : "[white]") +
            plan.unit.localizedName + "[lightgray]  " + pendingUnitCount(planIndex) + "/" + maxPlanDisplayCount(plan),
            () -> Mathf.clamp((float)pendingUnitCount(planIndex) / Math.max(1f, maxPlanDisplayCount(plan)))
            ),
            new Table(icon -> icon.left().add(new Image(plan.unit.uiIcon)).size(46f).padLeft(8f).padTop(6f).padBottom(6f).scaling(Scaling.fit)),
            new Table(right -> {
                right.right();
                right.label(() -> (hasPlanItemShortage(planIndex, plan) ? "[scarlet]" : "[lightgray]") + Core.bundle.get("wh-airborne-space") + ": " + plan.space).padRight(6f);
            })
            );
        }

        // 判断按当前计划数量是否存在物资不足。
        public boolean hasPlanItemShortage(int planIndex, UnitSpacePlan plan){
            int required = Math.max(1, pendingUnitCount(planIndex));
            return !canAffordPlan(plan, required, pendingBoostItemsPerUnit);
        }

        public int pendingUnitCount(int planIndex){
            return pendingRequests().get(planIndex, 0);
        }

        public IntIntMap pendingRequests(){
            ensureGroupRows(pendingGroups, pendingSpawnAmount);
            return collectRequests(pendingGroups, pendingSpawnAmount);
        }

        public int pendingTotalUnits(){
            int total = 0;
            for(IntIntMap.Entry entry : pendingRequests()){
                total += entry.value;
            }
            return total;
        }

        public int pendingBoostCost(){
            if(boostItem == null || pendingBoostItemsPerUnit <= 0) return 0;
            return pendingBoostItemsPerUnit * pendingTotalUnits();
        }

        public boolean hasBoostStatusConfigured(){
            return boostStatus != null && boostStatus != StatusEffects.none;
        }

        public String boostStatusName(){
            return hasBoostStatusConfigured() ? boostStatus.localizedName : "none";
        }

        public int pendingBuffMissingItems(){
            if(buffThresholdItems <= 0) return 0;
            return Math.max(0, buffThresholdItems - pendingBoostCost());
        }

        public boolean pendingBuffActive(){
            return shouldApplyBuff(pendingTotalUnits(), pendingBoostItemsPerUnit);
        }

        public int pendingUnitsForBuffTrigger(){
            if(boostItem == null || pendingBoostItemsPerUnit <= 0) return 0;
            if(buffThresholdItems <= 0) return 0;
            return Mathf.ceil((float)buffThresholdItems / pendingBoostItemsPerUnit);
        }

        public IntIntMap pendingCostMap(){
            IntIntMap costs = new IntIntMap();
            IntIntMap requests = pendingRequests();
            float unitCost = unitCostMultiplier();

            for(IntIntMap.Entry entry : requests){
                if(entry.key < 0 || entry.key >= plans.size || entry.value <= 0) continue;
                UnitSpacePlan requestPlan = plans.get(entry.key);
                for(ItemStack stack : requestPlan.requirements){
                    int perUnit = Mathf.ceil(stack.amount * unitCost);
                    if(perUnit <= 0) continue;
                    int total = perUnit * entry.value;
                    costs.put(stack.item.id, costs.get(stack.item.id, 0) + total);
                }
            }

            if(boostItem != null && pendingBoostItemsPerUnit > 0){
                int boostCost = pendingBoostCost();
                if(boostCost > 0){
                    costs.put(boostItem.id, costs.get(boostItem.id, 0) + boostCost);
                }
            }

            return costs;
        }

        public int maxGroupDisplayCount(){
            return Math.max(1, maxSpawnCount * GROUP_AMOUNT);
        }

        public int maxPlanDisplayCount(UnitSpacePlan plan){
            return Math.max(1, maxGroupDisplayCount() / Math.max(1, plan.space));
        }

        public Color planRowColor(int planIndex, UnitSpacePlan plan){
            IntSeq selectedGroup = pendingGroup(editingGroup);
            int used = groupUsedSlots(selectedGroup);
            int need = Math.max(1, plan.space);
            boolean selected = selectedGroup.contains(planIndex);
            boolean hasSpace = selected || (used + need <= GROUP_AMOUNT);
            boolean affordable = !hasPlanItemShortage(planIndex, plan);

            if(!affordable) return Pal.remove;
            if(!hasSpace) return Pal.gray;
            return selected ? Pal.techBlue : Pal.accent;
        }

        @Override
        public float warmup(){
            return warmup;
        }

        @Override
        public float progress(){
            return buildProgress;
        }

        @Override
        public byte version(){
            return 4;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(warmup);
            write.f(buildProgress);
            write.bool(needConfirmDeploy);
            write.s(currentPlan);
            write.s(spawnAmount);
            write.s(boostItemsPerUnit);
            TypeIO.writeVec2(write, spawnPos);
            TypeIO.writeVecNullable(write, commandPos);
            write.i(activeCapacity);
            write.i(usedCapacity);
            write.l(spawnFlag);
            write.s(pendingCaps.size);
            for(PendingCapacity p : pendingCaps){
                write.f(Math.max(0f, p.expire - Time.time));
                write.i(p.amount);
            }
            ensureGroupRows(activeGroups, spawnAmount);
            write.b(activeGroups.size);
            for(IntSeq group : activeGroups){
                write.b(group.size);
                for(int i = 0; i < group.size; i++){
                    write.s(group.get(i));
                }
            }
        }

        @Override
        public void read(Reads read, byte revision){
            boolean preservePendingDraft = !pendingEqualsActive();
            int pendingAmountSnapshot = pendingSpawnAmount;
            int pendingBoostSnapshot = pendingBoostItemsPerUnit;
            int pendingEditGroupSnapshot = editingGroup;
            Seq<IntSeq> pendingSnapshot = new Seq<>();
            copyGroups(pendingGroups, pendingSnapshot);

            super.read(read, revision);
            warmup = read.f();
            buildProgress = read.f();
            needConfirmDeploy = revision >= 4 ? read.bool() : buildProgress >= 0.999f;
            currentPlan = read.s();
            spawnAmount = read.s();
            boostItemsPerUnit = read.s();
            spawnPos = TypeIO.readVec2(read);
            commandPos = TypeIO.readVecNullable(read);
            activeCapacity = read.i();
            usedCapacity = read.i();
            spawnFlag = read.l();
            readPendingCapacities(read);
            readActiveGroups(read, revision);
            restoreLoadedState();
            restorePendingDraftAfterRead(
                    preservePendingDraft,
                    pendingAmountSnapshot,
                    pendingBoostSnapshot,
                    pendingEditGroupSnapshot,
                    pendingSnapshot
            );
        }

        private void readPendingCapacities(Reads read){
            pendingCaps.clear();
            int pending = read.s();
            for(int i = 0; i < pending; i++){
                PendingCapacity p = new PendingCapacity();
                p.expire = Time.time + read.f();
                p.amount = read.i();
                pendingCaps.add(p);
            }
        }

        private void readActiveGroups(Reads read, byte revision){
            activeGroups.clear();
            pendingGroups.clear();
            if(revision >= 3){
                int rows = read.b();
                for(int r = 0; r < rows; r++){
                    activeGroups.add(readGroup(read));
                }
                return;
            }
            if(revision >= 2){
                activeGroups.add(readGroup(read));
            }
        }

        private IntSeq readGroup(Reads read){
            IntSeq group = new IntSeq();
            int size = read.b();
            for(int i = 0; i < size; i++){
                int idx = read.s();
                if(idx >= 0 && idx < plans.size && group.size < GROUP_AMOUNT){
                    group.add(idx);
                }
            }
            return group;
        }

        private void restoreLoadedState(){
            if(spawnPos == null) spawnPos = new Vec2(x, y);
            if(spawnFlag == 0L) spawnFlag = composeSpawnFlag();
            spawnAmount = Mathf.clamp(spawnAmount, 1, Math.max(1, maxSpawnCount));
            boostItemsPerUnit = Mathf.clamp(boostItemsPerUnit, 0, Math.max(0, maxBoostItemsPerUnit));
            normalizeCurrentPlan();
            ensureGroupRows(activeGroups, spawnAmount);
            if(activeGroups.isEmpty()){
                activeGroups.add(new IntSeq());
            }
            syncCurrentPlanFromActive();
            clampSpawnPos();
            // Loaded saves may keep confirm flag; reset progress to rebuild safely.
            if (needConfirmDeploy) {
                buildProgress = 0f;
            }
        }

        private void syncPendingFromActive() {
            copyGroups(activeGroups, pendingGroups);
            pendingSpawnAmount = spawnAmount;
            pendingBoostItemsPerUnit = boostItemsPerUnit;
            editingGroup = Mathf.clamp(editingGroup, 0, Math.max(0, pendingSpawnAmount - 1));
        }

        private void restorePendingDraftAfterRead(boolean preservePendingDraft, int pendingAmountSnapshot, int pendingBoostSnapshot, int pendingEditGroupSnapshot, Seq<IntSeq> pendingSnapshot) {
            if (!preservePendingDraft) {
                syncPendingFromActive();
                return;
            }

            pendingSpawnAmount = Mathf.clamp(pendingAmountSnapshot, 1, Math.max(1, maxSpawnCount));
            pendingBoostItemsPerUnit = Mathf.clamp(pendingBoostSnapshot, 0, Math.max(0, maxBoostItemsPerUnit));
            copyGroups(pendingSnapshot, pendingGroups);
            ensureGroupRows(pendingGroups, pendingSpawnAmount);
            ensurePendingDraft();
            editingGroup = Mathf.clamp(pendingEditGroupSnapshot, 0, Math.max(0, pendingSpawnAmount - 1));

            if (pendingEqualsActive()) {
                syncPendingFromActive();
            }
        }
    }

    public static class PendingCapacity{
        public float expire;
        public int amount;
    }

    public static class PreparedGroupSpawn{
        public final UnitType[] loadout;
        public final IntIntMap requests;
        public final int space;
        public final int units;
        public final int capReserved;

        public PreparedGroupSpawn(UnitType[] loadout, IntIntMap requests, int space, int units, int capReserved){
            this.loadout = loadout;
            this.requests = requests;
            this.space = space;
            this.units = units;
            this.capReserved = capReserved;
        }
    }
}

