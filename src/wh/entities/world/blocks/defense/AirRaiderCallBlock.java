package wh.entities.world.blocks.defense;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.entities.*;
import wh.entities.world.entities.*;
import wh.gen.*;
import wh.graphics.*;
import wh.ui.*;

import static arc.Core.bundle;
import static mindustry.Vars.*;
import static mindustry.type.ItemStack.with;
import static wh.core.WarHammerMod.name;
import static wh.ui.UIUtils.LEN;

public class AirRaiderCallBlock extends Block{
    private static final String STRAFE_BUNDLE_KEY = "wh-strafe-mode";
    private static final String MISSILE_BUNDLE_KEY = "wh-missile-mode";
    private static final String BOMB_BUNDLE_KEY = "wh-bomb-mode";
    private static final String STRAFE_ICON_KEY = "strafe-mode";
    private static final String MISSILE_ICON_KEY = "missile-mode";
    private static final String BOMB_ICON_KEY = "bomb-mode";

    // 指令与轨迹计算阈值。
    private static final float COMMAND_WARMUP_THRESHOLD = 0.25f;
    private static final float TARGET_VALID_DISTANCE = 1f;
    private static final float PREVIEW_STRAFE_DISTANCE_TILES = 35f;
    private static final float PREVIEW_NON_STRAFE_DISTANCE_TILES = 75f;
    private static final float MIN_FALLBACK_STRAFE_DISTANCE_TILES = 6f;
    private static final float LAUNCH_MARK_ROTATION_OFFSET = 45f;
    private static final float STRAFE_SPAWN_MIN_TILES = 30f;
    private static final float STRAFE_SPAWN_MAX_TILES = 40f;
    private static final float NON_STRAFE_SPAWN_MIN_TILES = 60f;
    private static final float NON_STRAFE_SPAWN_MAX_TILES = 90f;

    public AttackMode defaultMode = null;
    public int maxMount = 5;
    public float range = 1500;
    public float starfRange = 600;
    public float spawnAngleSpread = 45f;
    public float spawnLateralSpread = 8f;
    public float spawnExitDistanceScale = 4f;
    public float commandTargetJitter = 10 * tilesize;
    public UnitType strafeUnit = WHUnitTypes.airRaiderS;
    public UnitType missileUnit = WHUnitTypes.airRaiderM;
    public UnitType bombUnit = WHUnitTypes.airRaiderB;
    public Seq<AttackModeUnitPlan> plans = new Seq<>();
    private final ObjectMap<AttackMode, AttackModeUnitPlan> planByMode = new ObjectMap<>();
    public StatusEntry statusEntry = new StatusEntry().set(StatusEffects.none, 0);

    public enum AttackMode{
        strafe, missile, bomb;

        public static AttackMode safeValueOf(int ordinal){
            if(ordinal < 0 || ordinal >= values().length){
                return null;
            }
            return values()[ordinal];
        }
    }

    public AirRaiderCallBlock(String name){
        super(name);
        size = 3;
        hasPower = true;
        hasItems = true;
        update = true;
        configurable = true;
        solid = true;
        itemCapacity = 50;

        config(Integer.class, (AirRaiderUnitBuild build, Integer planId) -> {
            if(planId >= 0 && planId < plans.size){
                build.setMode(plans.get(planId).mode);
            }
        });
        config(AttackMode.class, AirRaiderUnitBuild::setMode);

        consume(new ConsumeItemDynamic((AirRaiderUnitBuild e) -> {
            AttackModeUnitPlan currentPlan = planFor(e.currentMode);
            return currentPlan != null ? currentPlan.requirements : ItemStack.empty;
        }));

        // 默认计划直接在构造函数里注册。
        ensureDefaultUnits();
        plans.clear();
        planByMode.clear();
        addPlan(createPlan(strafeUnit, AttackMode.strafe, STRAFE_BUNDLE_KEY, STRAFE_ICON_KEY, 600f, WHItems.sealedPromethium, 10));
        addPlan(createPlan(missileUnit, AttackMode.missile, MISSILE_BUNDLE_KEY, MISSILE_ICON_KEY, 600f, WHItems.sealedPromethium, 8));
        addPlan(createPlan(bombUnit, AttackMode.bomb, BOMB_BUNDLE_KEY, BOMB_ICON_KEY, 600f, WHItems.sealedPromethium, 30));
    }

    @Override
    public void load(){
        super.load();
        initPlans();
    }

    public void initPlans(){
        // 资源系统未就绪时直接返回，避免内容早期初始化触发空指针。
        if(Core.atlas == null || Core.bundle == null) return;

        ensureDefaultUnits();
        rebuildPlans();
    }

    private void ensureDefaultUnits(){
        if(strafeUnit == null) strafeUnit = WHUnitTypes.airRaiderS;
        if(missileUnit == null) missileUnit = WHUnitTypes.airRaiderM;
        if(bombUnit == null) bombUnit = WHUnitTypes.airRaiderB;
    }

    private void rebuildPlans(){
        plans.clear();
        planByMode.clear();

        for(AttackMode mode : AttackMode.values()){
            AttackModeUnitPlan plan = createPlanForMode(mode);
            if(plan != null){
                addPlan(plan);
            }
        }
    }

    protected @Nullable AttackModeUnitPlan createPlanForMode(AttackMode mode){
        return switch(mode){
            case strafe -> createPlan(strafeUnit, mode, STRAFE_BUNDLE_KEY, STRAFE_ICON_KEY, 600f, WHItems.sealedPromethium, 10);
            case missile -> createPlan(missileUnit, mode, MISSILE_BUNDLE_KEY, MISSILE_ICON_KEY, 600f, WHItems.sealedPromethium, 8);
            case bomb -> createPlan(bombUnit, mode, BOMB_BUNDLE_KEY, BOMB_ICON_KEY, 600f, WHItems.sealedPromethium, 30);
        };
    }

    protected AttackModeUnitPlan createPlan(UnitType unit, AttackMode mode, String bundleKey, String iconKey, float time, Item costItem, int costAmount){
        Item safeItem = costItem == null ? WHItems.sealedPromethium : costItem;
        String planName = Core.bundle != null ? Core.bundle.get(bundleKey) : bundleKey;
        TextureRegion iconRegion = Core.atlas != null ? Core.atlas.find(name(iconKey)) : new TextureRegion();
        return new AttackModeUnitPlan(
        unit,
        planName,
        iconRegion,
        mode,
        Math.max(1f, time),
        with(safeItem, Math.max(0, costAmount))
        );
    }

    private void addPlan(AttackModeUnitPlan plan){
        plans.add(plan);
        planByMode.put(plan.mode, plan);
    }

    private void ensurePlansReady(){
        if(plans.isEmpty() || planByMode.isEmpty()){
            initPlans();
        }
    }

    public @Nullable AttackModeUnitPlan planFor(@Nullable AttackMode mode){
        ensurePlansReady();
        if(mode == null) return null;
        return planByMode.get(mode);
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("reload", (AirRaiderUnitBuild build) -> new Bar(
        () -> Core.bundle.get("bar.wh-reload"),
        () -> Pal.ammo,
        () -> {
            AttackModeUnitPlan currentPlan = planFor(build.currentMode);
            if(currentPlan == null) return 0f;
            return Mathf.clamp(build.reload / currentPlan.time);
        }
        ));
        addBar("mode", (AirRaiderUnitBuild build) -> new Bar(
        () -> {
            AttackModeUnitPlan currentPlan = planFor(build.currentMode);
            return currentPlan == null ? Core.bundle.get("none") : Core.bundle.get("bar.wh-mode") + currentPlan.name;
        },
        () -> Pal.accent,
        () -> build.currentMode == null ? 0f : 1f
        ));

        addBar("team-builds", (AirRaiderUnitBuild entity) -> new Bar(
        () -> {
            int count = WorldRegister.teamBlockCount(entity.team, this);
            return Core.bundle.get("bar.wh-amount") + count + " / " + maxMount;
        },
        () -> Pal.accent,
        () -> maxMount <= 0 ? 1f : Mathf.clamp((float)WorldRegister.teamBlockCount(entity.team, this) / maxMount)
        ));
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        if(team != Team.derelict && WorldRegister.teamBlockCount(team, this) >= maxMount){
            drawPlaceText("Maximum Placement Quantity Reached", tile.x, tile.y, false);
            return false;
        }
        return super.canPlaceOn(tile, team, rotation);
    }

    @Override
    public void setStats(){
        super.setStats();
        ensurePlansReady();
        plans.each(plan -> {
            stats.add(Stat.ammo, StatValues.items(plan.requirements));
            if(plan.unit.weapons.any()){
                stats.add(Stat.unitType, StatValues.content(plan.unit));
                stats.add(Stat.weapons, StatValues.weapons(plan.unit, plan.unit.weapons));
            }
        });
    }

    public static class AttackModeUnitPlan{
        public UnitType unit;
        public ItemStack[] requirements;
        public float time;
        public AttackMode mode;
        public String name;
        public TextureRegion icon;

        public AttackModeUnitPlan(UnitType unit, String name, TextureRegion icon, AttackMode mode, float time, ItemStack[] requirements){
            this.unit = unit;
            this.requirements = requirements;
            this.time = time;
            this.mode = mode;
            this.name = name;
            this.icon = icon;
        }

        public AttackModeUnitPlan(){
        }
    }

    public class AirRaiderUnitBuild extends Building implements Ranged{
        private static final long seedAngle = 0x9E3779B97F4A7C15L;
        private static final long seedPos = 0xD1B54A32D192ED03L;
        private static final long seedDist = 0x94D049BB133111EBL;

        public AttackMode currentMode = defaultMode;
        public boolean canSpawn;
        protected final Vec2 firstTarget = new Vec2().set(this);
        protected final Vec2 secondaryTarget = new Vec2().set(this);
        // Legacy alias for old code paths.
        protected final Vec2 SecondTarget = secondaryTarget;
        protected final Vec2 extendPos = new Vec2().set(this);

        // 运行时缓存，减少频繁创建临时对象。
        private final Seq<AirRaiderUnitBuild> participantBuffer = new Seq<>();
        private final Vec2 spawnPositionBuffer = new Vec2();

        public float delayTime = 240f;
        public float delay;
        public float reload;
        public float warmup;
        public float totalProgress;
        public float warmupSpeed = 0.07f;
        public float warmupFallSpeed = 0.1f;

        public Seq<Unit> spawnedUnits = new Seq<>();
        public int savedUnitsCount = 0;
        protected IntSeq readUnits = new IntSeq();

        @Override
        public float range(){
            return range;
        }

        @Override
        public void add(){
            if(!added){
                WorldRegister.ARBuilds.add(this);
                WorldRegister.registerBuild(this);
            }
            super.add();
        }

        @Override
        public void remove(){
            if(added){
                WorldRegister.ARBuilds.remove(this);
                WorldRegister.unregisterBuild(this);
            }
            markSpawnedUnitsRetreatOnRemove();
            super.remove();
        }

        /** 拆除建筑时让已生成空袭机立刻进入离场状态，避免悬空开火。 */
        private void markSpawnedUnitsRetreatOnRemove(){
            for(Unit unit : spawnedUnits){
                if(!(unit.controller() instanceof AirRaiderAI ai)) continue;

                ai.hasReachedEnd = true;
                for(var mount : unit.mounts){
                    mount.shoot = false;
                }
                if(unit instanceof AirRaiderUnitType u){
                    u.end = true;
                }
            }
        }

        private @Nullable AttackModeUnitPlan currentPlan(){
            return planFor(currentMode);
        }

        private boolean hasPrimaryTarget(){
            return firstTarget.dst(this) >= TARGET_VALID_DISTANCE;
        }

        private boolean hasSecondaryTarget(){
            return secondaryTarget.dst(firstTarget) >= TARGET_VALID_DISTANCE;
        }

        private boolean hasValidTargets(@Nullable AttackMode mode){
            if(mode == null) return false;
            if(mode == AttackMode.strafe){
                return hasPrimaryTarget() && hasSecondaryTarget();
            }
            return hasPrimaryTarget();
        }

        private boolean hasRequiredItems(@Nullable AttackModeUnitPlan plan){
            if(plan == null) return false;
            for(ItemStack stack : plan.requirements){
                if(items.get(stack.item) < stack.amount){
                    return false;
                }
            }
            return true;
        }

        private boolean canCommandByPlan(Vec2 target, @Nullable AttackModeUnitPlan plan){
            return plan != null && reload >= plan.time && warmup > COMMAND_WARMUP_THRESHOLD && within(target, range());
        }

        private boolean canCommandMode(Vec2 target, @Nullable AttackMode mode){
            return canCommandByPlan(target, planFor(mode));
        }

        private boolean isLaunchPending(){
            return canSpawn && delay < delayTime;
        }

        private boolean canIssueCommand(@Nullable AttackModeUnitPlan plan){
            return plan != null && !isLaunchPending() && hasRequiredItems(plan) && hasValidTargets(plan.mode) && canCommandByPlan(firstTarget, plan);
        }

        @Override
        public void updateTile(){
            restoreSpawnedUnitsAfterRead();
            updateWarmupAndProgress();
            chargeReload();
            updateLaunchState();
            pruneSpawnedUnits();
        }

        /** 读档后通过 id 重建单位引用，并刷新 AI 目标。 */
        private void restoreSpawnedUnitsAfterRead(){
            if(readUnits.isEmpty()) return;

            spawnedUnits.clear();
            for(int i = 0; i < readUnits.size; i++){
                Unit unit = Groups.unit.getByID(readUnits.get(i));
                if(unit != null){
                    spawnedUnits.addUnique(unit);
                }
            }
            readUnits.clear();
            selectUnitAI();
        }

        /** 有电升温、断电降温，同时累计总进度。 */
        private void updateWarmupAndProgress(){
            if(efficiency > 0f || power.status >= 0.99f){
                warmup = Mathf.lerpDelta(warmup, 1f, warmupSpeed);
                totalProgress += warmup * edelta();
            }else{
                warmup = Mathf.lerpDelta(warmup, 0f, warmupFallSpeed);
            }
        }

        private void chargeReload(){
            if(canSpawn) return;

            AttackModeUnitPlan plan = currentPlan();
            if(plan != null && shouldCharge()){
                reload = Math.min(plan.time, reload + edelta() * warmup);
            }
        }

        private void updateLaunchState(){
            if(!canSpawn) return;

            delay += Time.delta;
            if(delay >= delayTime){
                spawnRaiderUnit();
                selectUnitAI();
                delay = 0f;
            }
        }

        private void pruneSpawnedUnits(){
            for(int i = spawnedUnits.size - 1; i >= 0; i--){
                Unit unit = spawnedUnits.get(i);
                if(unit.dead || !unit.isAdded() || unit.team() != team || isRetreatingUnit(unit)){
                    spawnedUnits.remove(i);
                }
            }
        }

        private boolean isRetreatingUnit(Unit unit){
            if(unit instanceof AirRaiderUnitType airRaider && airRaider.end){
                return true;
            }
            return unit.controller() instanceof AirRaiderAI ai && ai.hasReachedEnd;
        }

        /** 将当前建筑的目标参数同步到已生成空袭单位的 AI。 */
        public void selectUnitAI(){
            if(currentMode == null) return;
            for(Unit unit : spawnedUnits){
                if(!(unit.controller() instanceof AirRaiderAI ai)) continue;
                if(isRetreatingUnit(unit)) continue;

                switch(currentMode){
                    case strafe -> ai.setStrafingPath(firstTarget, secondaryTarget, extendPos);
                    case bomb -> ai.setBombTarget(firstTarget, extendPos);
                    case missile -> ai.setMissileTarget(firstTarget, extendPos);
                }
            }
        }

        public boolean shouldCharge(){
            AttackModeUnitPlan currentPlan = currentPlan();
            return currentPlan != null && reload < currentPlan.time;
        }

        public boolean isCharging(){
            AttackModeUnitPlan currentPlan = currentPlan();
            return currentPlan != null && reload >= currentPlan.time;
        }

        // Keep misspelled old method for compatibility.
        public boolean Chargeing(){
            return isCharging();
        }

        public void setMode(AttackMode mode){
            currentMode = mode;
            secondaryTarget.set(this);
            onTargetsUpdated();
        }

        public void selectOnePosition(Table table){
            UIUtils.selectPos(table, pos -> {
                setPrimaryTargetFromTile(pos);
                onTargetsUpdated();
            });
        }

        public void selectTwoPosition(Table table){
            UIUtils.selectTwoPos(table, (point1, point2) -> {
                setPrimaryTargetFromTile(point1);
                setSecondaryTargetFromTile(point2);
                onTargetsUpdated();
            });
        }

        private void onTargetsUpdated(){
            updateExtendPosPreview();
            selectUnitAI();
        }

        private void setPrimaryTargetFromTile(Point2 pos){
            tileToWorldPos(pos, firstTarget);
            clampPosition(firstTarget, Tmp.v3.set(this), range());
        }

        private void setSecondaryTargetFromTile(Point2 pos){
            tileToWorldPos(pos, secondaryTarget);
            clampPosition(secondaryTarget, firstTarget, starfRange);
        }

        private void tileToWorldPos(Point2 pos, Vec2 out){
            out.set(
            pos.x * tilesize + tilesize / 2f,
            pos.y * tilesize + tilesize / 2f
            );
        }

        public Vec2 convertToWorldPos(Point2 pos){
            Vec2 worldPos = new Vec2();
            tileToWorldPos(pos, worldPos);
            clampPosition(worldPos, Tmp.v3.set(this), range());
            return worldPos;
        }

        public Vec2 convertToWorldPos2(Point2 pos){
            Vec2 worldPos = new Vec2();
            tileToWorldPos(pos, worldPos);
            clampPosition(worldPos, firstTarget, starfRange);
            return worldPos;
        }

        private void clampPosition(Vec2 position, Vec2 center, float maxDistance){
            if(position.dst(center) > maxDistance){
                position.sub(center).setLength(maxDistance).add(center);
            }
        }

        @Override
        public void buildConfiguration(Table table){
            control.input.selectedBlock();
            drawPlan(table);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            AttackModeUnitPlan currentPlan = currentPlan();
            return currentPlan != null && items.get(item) < getMaximumAccepted(item) &&
            Structs.contains(currentPlan.requirements, stack -> stack.item == item);
        }

        // Legacy entry, kept for compatibility with old call sites.
        public void DrawPlan(Table table){
            drawPlan(table);
        }

        public void drawPlan(Table table){
            ensurePlansReady();
            table.table(t -> {
                t.defaults().fill();

                for(AttackModeUnitPlan plan : plans){
                    addModeRow(t, plan.name, plan.icon, plan);
                    t.row();
                }

                t.button(Icon.modeAttack, Styles.defaulti, () -> {
                    AttackModeUnitPlan currentPlan = currentPlan();
                    if(canIssueCommand(currentPlan)){
                        consume();
                        commandAll(firstTarget, secondaryTarget);
                    }
                }).size(LEN).disabled(b -> !canIssueCommand(currentPlan()));
            });
        }

        private void addModeRow(Table parent, String name, TextureRegion icon, AttackModeUnitPlan plan){
            parent.table(t -> {
                t.background(Styles.black6);
                TextureRegionDrawable iconD = new TextureRegionDrawable(icon){
                    @Override
                    public void draw(float x, float y, float width, float height){
                        Draw.color(Tmp.c1.set(team.color).mul(Draw.getColor()).toFloatBits());
                        Draw.rect(region, x + width / 2f, y + height / 2f, width, height);
                    }
                };

                t.table(left -> left.button(name, iconD, LEN, () -> {
                    setMode(plan.mode);
                    selectTargetByMode(plan.mode, parent);
                }).size(LEN * 4f, LEN).left().disabled(b -> isLaunchPending()));

                t.table(req -> {
                    req.defaults().pad(2).left();
                    for(ItemStack stack : plan.requirements){
                        req.add(new ItemImageDynamic(stack.item, () -> stack.amount)).left();
                    }
                }).size(LEN * 1.5f, LEN);
            }).fill();
        }

        private void selectTargetByMode(AttackMode mode, Table table){
            if(mode == AttackMode.strafe){
                selectTwoPosition(table);
            }else{
                selectOnePosition(table);
            }
        }

        @Override
        public void drawConfigure(){
            super.drawConfigure();

            AttackMode mode = currentMode;
            collectCommandParticipants(participantBuffer, firstTarget, mode, false);
            drawParticipantLinks(participantBuffer);

            Drawf.dashCircle(x, y, range, team.color);

            if(hasPrimaryTarget()){
                Drawf.dashCircle(firstTarget.x, firstTarget.y, starfRange, Pal.accent);
                Drawn.posSquareLink(Pal.accent, 1, 2, true, x, y, firstTarget.x, firstTarget.y);
                Drawn.drawConnected(firstTarget.x, firstTarget.y, 10f, Pal.accent);
            }

            if(mode == AttackMode.strafe && hasSecondaryTarget()){
                Drawn.posSquareLink(Pal.accent, 1, 2, true, firstTarget.x, firstTarget.y, secondaryTarget.x, secondaryTarget.y);
                Drawn.drawConnected(secondaryTarget.x, secondaryTarget.y, 10f, Pal.accent);
                if(participantBuffer.any()){
                    Drawn.posSquareLink(Pal.heal, 1, 2, true, firstTarget.x, firstTarget.y, secondaryTarget.x, secondaryTarget.y);
                }
            }

            int participants = participantBuffer.size + ((mode != null && canCommandMode(firstTarget, mode)) ? 1 : 0);
            if(participants > 0 && hasPrimaryTarget()){
                Drawn.overlayText(bundle.format("wh-participants", participants),
                firstTarget.x, firstTarget.y, tilesize * 2f, Pal.accent, true);
            }
        }

        private void drawParticipantLinks(Seq<AirRaiderUnitBuild> participants){
            for(AirRaiderUnitBuild build : participants){
                Drawn.posSquareLink(Pal.gray, 3, 4, false, build.x, build.y, firstTarget.x, firstTarget.y);
                Draw.color(Pal.accent);
                Lines.stroke(2f);
                Lines.square(build.x, build.y, build.block.size * tilesize / 3f, LAUNCH_MARK_ROTATION_OFFSET + Time.time % 360f);
            }
            Draw.reset();

            for(AirRaiderUnitBuild build : participants){
                Drawn.posSquareLink(Pal.heal, 1, 2, false, build.x, build.y, firstTarget.x, firstTarget.y);
            }
        }

        public boolean canCommand(Vec2 target){
            return canCommandMode(target, currentMode);
        }

        private void collectCommandParticipants(Seq<AirRaiderUnitBuild> out, Vec2 target, @Nullable AttackMode mode, boolean allowSelf){
            out.clear();
            if(mode == null || !hasValidTargets(mode)) return;

            for(AirRaiderUnitBuild build : WorldRegister.ARBuilds){
                if(isCommandParticipant(build, target, mode, allowSelf)){
                    out.add(build);
                }
            }
        }

        private boolean isCommandParticipant(@Nullable AirRaiderUnitBuild build, Vec2 target, @Nullable AttackMode mode, boolean allowSelf){
            if(build == null || mode == null) return false;
            if(build.team != team) return false;
            if(!allowSelf && build == this) return false;
            return build.canCommandMode(target, mode);
        }

        private void setJitteredTarget(Vec2 out, Vec2 base, Rand random){
            out.set(base);
            if(commandTargetJitter > 0f){
                Tmp.v1.trns(random.random(360f), random.random(commandTargetJitter));
                out.add(Tmp.v1);
            }
        }

        private void applyCommand(AttackMode mode, Vec2 pos1, Vec2 pos2, long seed){
            AttackModeUnitPlan plan = planFor(mode);
            if(plan == null) return;

            Rand random = new Rand(seed);
            currentMode = mode;

            setJitteredTarget(firstTarget, pos1, random);
            clampPosition(firstTarget, Tmp.v3.set(this), range());

            if(mode == AttackMode.strafe){
                Vec2 secondBase = pos2.dst(pos1) < TARGET_VALID_DISTANCE ? pos1 : pos2;
                setJitteredTarget(secondaryTarget, secondBase, random);
                clampPosition(secondaryTarget, firstTarget, starfRange);

                if(secondaryTarget.dst(firstTarget) < TARGET_VALID_DISTANCE){
                    Tmp.v1.trns(baseSpawnAngle(mode), Math.min(starfRange, MIN_FALLBACK_STRAFE_DISTANCE_TILES * tilesize));
                    secondaryTarget.set(firstTarget).add(Tmp.v1);
                }
            }else{
                secondaryTarget.set(this);
            }

            canSpawn = true;
            delay = 0f;
            reload = Math.max(0f, reload - plan.time);
            updateExtendPosPreview();
        }

        public void commandAll(Vec2 pos1, Vec2 pos2){
            AttackMode mode = currentMode;
            if(mode == null || !hasValidTargets(mode)) return;

            collectCommandParticipants(participantBuffer, pos1, mode, true);

            int participants = 0;
            for(AirRaiderUnitBuild build : participantBuffer){
                long seed = (((long)id) << 32) ^ build.id ^ mode.ordinal();
                build.applyCommand(mode, pos1, pos2, seed);
                build.selectUnitAI();
                participants++;
            }
            if(participants > 0) lastAccessed(Iconc.modeAttack + "");

            if(!headless && participants > 0){
                AttackModeUnitPlan currentPlan = planFor(mode);
                if(currentPlan != null){
                    WHFx.warningRange(currentPlan.icon, delayTime * 1.5f, 80)
                    .at(pos1.x, pos1.y, 0f, team.color);
                }
                WHFx.attackWarningPos.at(pos1.x, pos1.y, delayTime, team.color, tile);
            }
        }

        public void lastAccessed(String lastAccessed){
            this.lastAccessed = lastAccessed;
        }

        public void SpawnRaiderUnit(){
            spawnRaiderUnit();
        }

        public void spawnRaiderUnit(){
            AttackModeUnitPlan plan = currentPlan();
            if(plan == null){
                canSpawn = false;
                return;
            }

            Unit raider = plan.unit.spawn(team, x, y);

            float spawnAngle = randomSpawnAngle(raider);
            float distanceTiles = spawnDistanceTiles(raider);
            randomSpawnPos(raider, spawnAngle, distanceTiles, spawnPositionBuffer);

            raider.set(spawnPositionBuffer);
            raider.rotation = spawnAngle;
            calculateEndPos(spawnAngle, distanceTiles);

            if(!Vars.net.client()) raider.add();
            raider.apply(StatusEffects.unmoving, Fx.unitSpawn.lifetime);
            raider.apply(statusEntry.effect, statusEntry.time);
            spawnedUnits.add(raider);

            canSpawn = false;
        }

        private long spawnSeed(Unit unit){
            long modeBits = currentMode == null ? 0L : (long)(currentMode.ordinal() + 1) * 131L;
            return (((long)id) << 32) ^ unit.id ^ modeBits;
        }

        private float spawnDistanceTiles(Unit unit){
            Rand random = new Rand(spawnSeed(unit) ^ seedDist);
            // 扫射和导弹统一出生距离区间，避免扫射模式“贴目标点出生”。
            return random.random(NON_STRAFE_SPAWN_MIN_TILES, NON_STRAFE_SPAWN_MAX_TILES);
        }

        private float baseSpawnAngle(@Nullable AttackMode mode){
            // 扫射始终按第一点->第二点连线确定朝向。
            if(mode == AttackMode.strafe){
                return firstTarget.angleTo(secondaryTarget);
            }
            if(hasPrimaryTarget()){
                return angleTo(firstTarget);
            }
            return rotation * 90f;
        }

        public float randomSpawnAngle(Unit unit){
            Rand random = new Rand(spawnSeed(unit) ^ seedAngle);
            return baseSpawnAngle(currentMode) + random.range(spawnAngleSpread);
        }

        public Vec2 randomSpawnPos(Unit unit){
            float spawnAngle = randomSpawnAngle(unit);
            Vec2 out = new Vec2();
            randomSpawnPos(unit, spawnAngle, spawnDistanceTiles(unit), out);
            return out;
        }

        private void randomSpawnPos(Unit unit, float spawnAngle, float spawnDistanceTiles, Vec2 out){
            Rand random = new Rand(spawnSeed(unit) ^ seedPos);
            // 扫射和导弹统一从建筑附近偏移生成。
            out.set(this);

            Tmp.v1.trns(spawnAngle, -spawnDistanceTiles * tilesize);
            Tmp.v2.trns(spawnAngle + 90f, random.range(spawnLateralSpread * tilesize));
            out.add(Tmp.v1).add(Tmp.v2);
        }

        public void calculateEndPos(Unit unit){
            calculateEndPos(baseSpawnAngle(currentMode), currentMode == AttackMode.strafe ? PREVIEW_STRAFE_DISTANCE_TILES : PREVIEW_NON_STRAFE_DISTANCE_TILES);
        }

        private void calculateEndPos(float spawnAngle, float spawnDistanceTiles){
            Vec2 pivot = (currentMode == AttackMode.strafe && hasSecondaryTarget()) ? secondaryTarget : firstTarget;
            float exitDistance = spawnDistanceTiles * tilesize * spawnExitDistanceScale;
            Tmp.v1.trns(spawnAngle, exitDistance);
            extendPos.set(pivot).add(Tmp.v1);
        }

        private void updateExtendPosPreview(){
            if(currentMode == null || !hasPrimaryTarget()){
                extendPos.set(this);
                return;
            }
            calculateEndPos(baseSpawnAngle(currentMode), currentMode == AttackMode.strafe ? PREVIEW_STRAFE_DISTANCE_TILES : PREVIEW_NON_STRAFE_DISTANCE_TILES);
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void read(Reads read, byte v){
            super.read(read, v);

            canSpawn = read.bool();
            delay = read.f();
            reload = read.f();
            warmup = read.f();
            totalProgress = read.f();

            int count = read.b() & 0xff;
            savedUnitsCount = read.i();

            readUnits.clear();
            for(int i = 0; i < count; i++){
                readUnits.add(read.i());
            }

            TypeIO.readVec2(read, firstTarget);
            TypeIO.readVec2(read, secondaryTarget);
            TypeIO.readVec2(read, extendPos);

            byte modeOrdinal = read.b();
            currentMode = AttackMode.safeValueOf(modeOrdinal);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(canSpawn);
            write.f(delay);
            write.f(reload);
            write.f(warmup);
            write.f(totalProgress);

            int count = Math.min(spawnedUnits.size, 255);
            write.b(count);
            write.i(spawnedUnits.size);
            for(int i = 0; i < count; i++){
                write.i(spawnedUnits.get(i).id);
            }

            TypeIO.writeVec2(write, firstTarget);
            TypeIO.writeVec2(write, secondaryTarget);
            TypeIO.writeVec2(write, extendPos);

            write.b(currentMode != null ? (byte)currentMode.ordinal() : (byte)-1);
        }
    }
}
