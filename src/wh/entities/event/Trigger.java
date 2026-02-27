package wh.entities.event;

import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;
import wh.content.*;
import wh.gen.*;

import static wh.entities.event.PortableAutoEventTrigger.*;

public class Trigger implements Entityc, Cloneable{
    // 标识与运行时种子
    public String id = "portable-auto-trigger";
    private int runtimeIndex = 1;

    // 触发条件需求
    public Prov<Team> checkTeam = () -> Vars.state.rules.defaultTeam;
    public Boolf<Team> extraCondition = t -> true;
    public Seq<Requirement<Item>> requiredItems = new Seq<>();
    public Seq<Requirement<UnitType>> requiredUnits = new Seq<>();
    public Seq<Requirement<Block>> requiredBuildings = new Seq<>();
    public int minTriggerWave = 0;

    // 模式与地图过滤
    public boolean allowCampaign = true;
    public boolean allowCustom = true;
    public boolean allowPvp = false;
    public boolean allowEditor = false;
    public boolean allowNonSectorMaps = true;
    public Seq<String> allowedSectorPresets = new Seq<>();
    public Boolf<Rules> rulesFilter = r -> true;

    // 计时参数
    public float spacingBase = 120f * Time.toSeconds;
    public float spacingRand = 120f * Time.toSeconds;
    public float checkSpacing = 120f;
    public boolean disposable = false;
    public boolean removeIfCaptured = true;

    // 可选内置 UI
    public boolean warnHudEnabled = true;
    public boolean useFleetWarnHudStyle = false;
    public float fleetWarnHudDuration = 2.5f;
    public Sound fleetWarnSound = WHSounds.alert2;
    /** 单个触发器的 HUD 样式覆盖；为 null 时使用全局默认 {@link PortableAutoEventTrigger#fleetWarnHudMode}。 */
    public FleetWarnHudMode fleetWarnHudModeOverride = null;
    public String hudText = "";
    public float hudToastDuration = 2.5f;
    public String triggerToastText = "";
    public float triggerToastDuration = 2.5f;
    public String chatText = "";
    /** NH 过场文本显示时长（tick）。 */
    public float chatDuration = 120f;
    public boolean markOnTrigger = false;
    public Color markColor = Color.valueOf("ff7b69");
    public float markRadius = 24f;
    public float markLifetime = 180f;

    // 可选刷怪配置（可由自定义 spawnerInvoker 接管）
    public Prov<Team> spawnTeam = () -> Vars.state.rules.waveTeam;
    public Func<Team, Vec2> spawnPosition = Trigger::resolveDefaultSpawnPosition;
    public ObjectIntMap<UnitType> spawnUnits = new ObjectIntMap<>();
    public float spawnRange = 48f;
    public float spawnWarmup = 50f;
    public float spawnEachDelay = 15f;
    public float spawnAngle = 0f;
    public StatusEffect spawnStatus = StatusEffects.none;
    public float spawnStatusDuration = 10f * Time.toSeconds;
    public double spawnFlag = Double.NaN;
    public boolean requireEnemySpawnPoint = true;

    /**
     * 设置后将优先使用该回调刷怪，而不是默认 UnitType.spawn 兜底逻辑。
     * 可在此桥接你自己的 Spawner 实现。
     */
    public Cons<SpawnContext> spawnerInvoker = null;
    /** 单位级 spawner 桥接；对某 UnitType 设置后，优先级高于 {@link #spawnerInvoker}。 */
    public ObjectMap<UnitType, Cons<SpawnContext>> unitSpawnerInvokers = new ObjectMap<>();
    /** 单位级覆盖参数（如护盾、状态）。 */
    public ObjectMap<UnitType, SpawnUnitConfig> spawnUnitConfigs = new ObjectMap<>();

    // 回调
    public Cons<TriggerContext> onTrigger = ctx -> {
    };
    public Cons<TriggerContext> onClientTrigger = ctx -> {
    };

    // 运行时状态
    public float reload = 0f;
    public float spacing = 0f;
    public float checkTimer = 0f;

    public boolean added;
    public transient int entityId = 0;
    private final Rand rand = new Rand();
    /** 建筑需求检查复用的临时计数表，避免每次检查分配对象。 */
    private transient final ObjectIntMap<Block> buildingCountScratch = new ObjectIntMap<>();
    /** 聚合后的建筑需求数量（处理重复方块需求）。 */
    private transient final ObjectIntMap<Block> buildingNeedScratch = new ObjectIntMap<>();

    public Trigger(){
        resetTransientHandlers();
    }

    public Trigger id(String id){
        this.id = id;
        return this;
    }

    public Trigger teamToCheck(Prov<Team> team){
        this.checkTeam = team;
        return this;
    }

    public Trigger extraCondition(Boolf<Team> condition){
        this.extraCondition = condition;
        return this;
    }

    public Trigger minWave(int wave){
        this.minTriggerWave = wave;
        return this;
    }

    public Trigger allowModes(boolean campaign, boolean custom, boolean pvp, boolean editor){
        this.allowCampaign = campaign;
        this.allowCustom = custom;
        this.allowPvp = pvp;
        this.allowEditor = editor;
        return this;
    }

    public Trigger allowNonSectorMaps(boolean allow){
        this.allowNonSectorMaps = allow;
        return this;
    }

    public Trigger forSectorPresets(String... presetNames){
        allowedSectorPresets.clear();
        if(presetNames != null){
            for(String name : presetNames){
                if(name != null && !name.isEmpty()){
                    allowedSectorPresets.add(name);
                }
            }
        }
        return this;
    }

    public Trigger rulesFilter(Boolf<Rules> filter){
        this.rulesFilter = filter == null ? r -> true : filter;
        return this;
    }

    public Trigger spacing(float base, float randRange){
        this.spacingBase = base;
        this.spacingRand = randRange;
        return this;
    }

    public Trigger checkSpacing(float spacing){
        this.checkSpacing = spacing;
        return this;
    }

    public Trigger disposable(boolean disposable){
        this.disposable = disposable;
        return this;
    }

    /** 一次性触发器的简写。 */
    public Trigger disposable(){
        return disposable(true);
    }

    /** 条件满足触发一次后自移除。 */
    public Trigger triggerOnce(){
        return disposable(true);
    }

    /** 条件满足刷怪一次后自移除。 */
    public Trigger spawnOnce(){
        return disposable(true);
    }

    public Trigger removeIfCaptured(boolean remove){
        this.removeIfCaptured = remove;
        return this;
    }

    public Trigger warnHudEnabled(boolean enabled){
        this.warnHudEnabled = enabled;
        return this;
    }

    /** 开启/关闭该触发器的舰队预警 HUD。 */
    public Trigger useFleetWarnHUD(boolean enabled){
        this.useFleetWarnHudStyle = enabled;
        return this;
    }

    /** 开启舰队预警 HUD，并覆盖该触发器的显示时长。 */
    public Trigger useFleetWarnHUD(boolean enabled, float duration){
        return useFleetWarnHUD(enabled, fleetWarnHudModeOverride, duration);
    }

    /** 开启舰队预警 HUD，并覆盖该触发器的 HUD 模式。 */
    public Trigger useFleetWarnHUD(boolean enabled, FleetWarnHudMode mode){
        return useFleetWarnHUD(enabled, mode, fleetWarnHudDuration);
    }

    /** 开启舰队预警 HUD，并同时覆盖 HUD 模式与显示时长。 */
    public Trigger useFleetWarnHUD(boolean enabled, FleetWarnHudMode mode, float duration){
        this.useFleetWarnHudStyle = enabled;
        this.fleetWarnHudModeOverride = mode;
        this.fleetWarnHudDuration = duration;
        return this;
    }

    public Trigger fleetWarnHudMode(FleetWarnHudMode mode){
        this.fleetWarnHudModeOverride = mode;
        return this;
    }

    public Trigger useGlobalFleetWarnHudMode(){
        this.fleetWarnHudModeOverride = null;
        return this;
    }

    public Trigger fleetWarnSound(Sound sound){
        this.fleetWarnSound = sound == null ? WHSounds.alert2 : sound;
        return this;
    }

    public Trigger hudText(String text, float duration){
        this.hudText = text;
        this.hudToastDuration = duration;
        return this;
    }

    public Trigger triggerToast(String text, float duration){
        this.triggerToastText = text;
        this.triggerToastDuration = duration;
        return this;
    }

    public Trigger chatText(String text, float duration){
        this.chatText = text;
        this.chatDuration = duration;
        return this;
    }

    public Trigger showTriggerMark(Color color, float radius, float lifetime){
        this.markOnTrigger = true;
        this.markColor = color == null ? Color.valueOf("ff7b69") : color;
        this.markRadius = radius;
        this.markLifetime = lifetime;
        return this;
    }

    public Trigger requireItems(Object... items){
        validatePairs(items);
        requiredItems.clear();
        for(int i = 0; i < items.length; i += 2){
            requiredItems.add(new Requirement<>((Item)items[i], ((Number)items[i + 1]).intValue()));
        }
        return this;
    }

    public Trigger requireUnits(Object... units){
        validatePairs(units);
        requiredUnits.clear();
        for(int i = 0; i < units.length; i += 2){
            requiredUnits.add(new Requirement<>((UnitType)units[i], ((Number)units[i + 1]).intValue()));
        }
        return this;
    }

    public Trigger requireBuildings(Object... blocks){
        validatePairs(blocks);
        requiredBuildings.clear();
        for(int i = 0; i < blocks.length; i += 2){
            requiredBuildings.add(new Requirement<>((Block)blocks[i], ((Number)blocks[i + 1]).intValue()));
        }
        return this;
    }

    public Trigger teamToSpawn(Prov<Team> team){
        this.spawnTeam = team;
        return this;
    }

    public Trigger spawnPos(Func<Team, Vec2> provider){
        this.spawnPosition = provider;
        return this;
    }

    public Trigger spawn(Object... units){
        validatePairs(units);
        spawnUnits.clear();
        if(unitSpawnerInvokers != null) unitSpawnerInvokers.clear();
        if(spawnUnitConfigs != null) spawnUnitConfigs.clear();
        for(int i = 0; i < units.length; i += 2){
            spawnUnits.put((UnitType)units[i], ((Number)units[i + 1]).intValue());
        }
        return this;
    }

    public Trigger spawn(UnitType type, int amount){
        spawnUnits.put(type, amount);
        return this;
    }

    public Trigger spawn(UnitType type, int amount, Cons<SpawnContext> invoker){
        spawnUnits.put(type, amount);
        if(type != null){
            if(unitSpawnerInvokers == null) unitSpawnerInvokers = new ObjectMap<>();
            if(invoker == null){
                unitSpawnerInvokers.remove(type);
            }else{
                unitSpawnerInvokers.put(type, invoker);
            }
        }
        return this;
    }

    /**
     * 新增/更新一个单位条目并指定固定护盾值。
     * 护盾值小于 0 表示沿用单位默认护盾。
     */
    public Trigger spawn(UnitType type, int amount, float shield, Cons<SpawnContext> invoker){
        spawn(type, amount, invoker);
        SpawnUnitConfig config = ensureSpawnUnitConfig(type);
        if(config != null){
            config.shield = shield;
        }
        return this;
    }

    /**
     * 新增/更新一个单位条目，并指定固定护盾和单位级状态覆盖。
     * 状态时长默认使用 {@link #spawnStatusDuration}。
     */
    public Trigger spawn(UnitType type, int amount, float shield, StatusEffect status, Cons<SpawnContext> invoker){
        return spawn(type, amount, shield, status, spawnStatusDuration, invoker);
    }

    /**
     * 新增/更新一个单位条目，并指定固定护盾与单位级状态覆盖（显式时长）。
     */
    public Trigger spawn(UnitType type, int amount, float shield, StatusEffect status, float statusDuration, Cons<SpawnContext> invoker){
        spawn(type, amount, invoker);
        SpawnUnitConfig config = ensureSpawnUnitConfig(type);
        if(config != null){
            config.shield = shield;
            config.status = status == null ? StatusEffects.none : status;
            config.statusDuration = Math.max(0f, statusDuration);
        }
        return this;
    }

    public Trigger spawnShape(float range, float warmup, float eachDelay, float angle){
        this.spawnRange = range;
        this.spawnWarmup = warmup;
        this.spawnEachDelay = eachDelay;
        this.spawnAngle = angle;
        return this;
    }

    public Trigger spawnStatus(StatusEffect effect, float duration){
        this.spawnStatus = effect;
        this.spawnStatusDuration = duration;
        return this;
    }

    public Trigger spawnFlag(long bits){
        this.spawnFlag = Double.longBitsToDouble(bits);
        return this;
    }

    public Trigger requireEnemySpawnPoint(boolean require){
        this.requireEnemySpawnPoint = require;
        return this;
    }

    public Trigger spawnerInvoker(Cons<SpawnContext> invoker){
        this.spawnerInvoker = invoker;
        return this;
    }

    public Trigger spawnerInvoker(UnitType type, Cons<SpawnContext> invoker){
        if(type == null) return this;
        if(unitSpawnerInvokers == null) unitSpawnerInvokers = new ObjectMap<>();
        if(invoker == null){
            unitSpawnerInvokers.remove(type);
        }else{
            unitSpawnerInvokers.put(type, invoker);
        }
        return this;
    }

    public Trigger onTrigger(Cons<TriggerContext> callback){
        this.onTrigger = callback;
        return this;
    }

    public Trigger onClientTrigger(Cons<TriggerContext> callback){
        this.onClientTrigger = callback;
        return this;
    }

    @Override
    public void remove(){
        if(!added) return;
        Groups.all.remove(this);
        added = false;
        onEntityRemoved(this);
    }

    @Override
    public void add(){
        if(Vars.net.client() || added) return;
        if(entityId == 0) entityId = EntityGroup.nextId();
        if(spacing <= 0f) spacing = nextSpacing();
        Groups.all.add(this);
        added = true;
        onEntityAdded(this);
    }

    @Override
    public void update(){
        // 世界加载后的同步（清理/重装）会延迟一个 tick，避免此窗口期触发旧实例。
        if(PortableAutoEventTrigger.isWorldLoadSyncPending()) return;

        reload += Time.delta * timeScale;
        checkTimer += Time.delta;

        if(reload < spacing || checkTimer < checkSpacing) return;
        checkTimer = 0f;

        if(!allowedInCurrentMode()) return;
        if(!allowedInCurrentSector()) return;

        Team team = resolveCheckTeam();
        if(team == null || team.cores().isEmpty()){
            remove();
            return;
        }

        if(removeIfCaptured && Vars.state.hasSector() && Vars.state.rules.sector != null && Vars.state.rules.sector.isCaptured()){
            remove();
            return;
        }

        if(meet(team)){
            fire(team);
        }
    }

    private Team resolveCheckTeam(){
        Team fallback = Vars.state != null && Vars.state.rules != null ? Vars.state.rules.defaultTeam : Team.sharded;
        try{
            Team team = checkTeam == null ? null : checkTeam.get();
            return team == null ? fallback : team;
        }catch(Throwable ignored){
            return fallback;
        }
    }

    private Team resolveSpawnTeam(){
        Team fallback = Vars.state != null && Vars.state.rules != null ? Vars.state.rules.waveTeam : Team.crux;
        try{
            Team team = spawnTeam == null ? null : spawnTeam.get();
            return team == null ? fallback : team;
        }catch(Throwable ignored){
            return fallback;
        }
    }

    private boolean allowedInCurrentMode(){
        if(debugForceAnyMode) return true;
        if(Vars.state.isEditor()) return allowEditor;
        if(Vars.state.rules.pvp) return allowPvp;
        if(Vars.state.isCampaign()) return allowCampaign;
        return allowCustom;
    }

    private boolean allowedInCurrentSector(){
        if(debugForceAnyMode) return true;
        if(rulesFilter != null && !rulesFilter.get(Vars.state.rules)) return false;

        if(allowedSectorPresets.isEmpty()) return true;
        if(!Vars.state.hasSector() || Vars.state.rules.sector == null || Vars.state.rules.sector.preset == null){
            return allowNonSectorMaps;
        }

        SectorPreset preset = Vars.state.rules.sector.preset;
        return allowedSectorPresets.contains(preset.name);
    }

    private boolean meet(Team team){
        if(debugBypassMeet) return true;
        if(requireEnemySpawnPoint && !hasEnemySpawnPoint()) return false;
        if(Vars.state.rules.waves && Vars.state.wave < minTriggerWave) return false;
        if(!team.active()) return false;

        CoreBlock.CoreBuild core = team.core();
        if(core == null) return false;
        Teams.TeamData teamData = team.data();

        for(int i = 0; i < requiredItems.size; i++){
            Requirement<Item> req = requiredItems.get(i);
            if(core.items.get(req.type) < req.amount) return false;
        }

        for(int i = 0; i < requiredUnits.size; i++){
            Requirement<UnitType> req = requiredUnits.get(i);
            if(teamData.countType(req.type) < req.amount) return false;
        }

        if(!requiredBuildings.isEmpty()){
            if(!meetBuildingRequirements(team)) return false;
        }

        return extraCondition.get(team);
    }

    private boolean meetBuildingRequirements(Team team){
        buildingNeedScratch.clear();
        for(int i = 0; i < requiredBuildings.size; i++){
            Requirement<Block> req = requiredBuildings.get(i);
            if(req == null || req.type == null || req.amount <= 0) continue;
            buildingNeedScratch.increment(req.type, 0, req.amount);
        }

        if(buildingNeedScratch.isEmpty()) return true;

        if(buildingNeedScratch.size == 1){
            ObjectIntMap.Entry<Block> single = buildingNeedScratch.entries().next();
            int count = Groups.build.count(b -> b.team == team && b.block == single.key);
            return count >= single.value;
        }

        buildingCountScratch.clear();
        Groups.build.each(b -> {
            if(b.team != team) return;
            int need = buildingNeedScratch.get(b.block, 0);
            if(need <= 0) return;

            int now = buildingCountScratch.get(b.block, 0);
            if(now < need){
                buildingCountScratch.put(b.block, now + 1);
            }
        });

        for(ObjectIntMap.Entry<Block> entry : buildingNeedScratch.entries()){
            if(buildingCountScratch.get(entry.key, 0) < entry.value) return false;
        }
        return true;
    }

    private boolean hasEnemySpawnPoint(){
        return Vars.state != null && Vars.state.hasSpawns() && Vars.spawner != null && Vars.spawner.getFirstSpawn() != null;
    }

    private void fire(Team checkedTeam){
        Team toSpawnTeam = resolveSpawnTeam();
        Vec2 spawnPos = spawnPosition == null ? null : spawnPosition.get(checkedTeam);
        float x = spawnPos == null ? Float.NaN : spawnPos.x;
        float y = spawnPos == null ? Float.NaN : spawnPos.y;

        TriggerContext ctx = new TriggerContext(this, checkedTeam, toSpawnTeam, x, y);
        FleetWarnHudMode warnMode = resolveFleetWarnHudMode();
        String centeredHudText = resolveCenteredHudText(warnMode);
        boolean centeredTextEmbedded = false;

        if(!Vars.headless){
            if(warnHudEnabled){
                if(useFleetWarnHudStyle){
                    showFleetWarnHud(ctx, fleetWarnHudDuration, fleetWarnSound, warnMode, centeredHudText);
                    centeredTextEmbedded = hasText(centeredHudText);
                }else if(hasText(hudText)){
                    showToastText(hudText);
                }
            }
            if(hasText(triggerToastText)){
                String coord = Float.isNaN(x) || Float.isNaN(y) ? "" : " [" + (int)(x / 8f) + ", " + (int)(y / 8f) + "]";
                showToastText(triggerToastText + coord);
            }
            if(hasText(chatText) && !centeredTextEmbedded){
                showInlineChatHud(chatText, chatDuration);
            }
            if(worldMarkEnabled && markOnTrigger && !Float.isNaN(x) && !Float.isNaN(y)){
                showInlineWorldMark(x, y, markRadius, markLifetime, markColor);
            }
            if(onClientTrigger != null){
                onClientTrigger.get(ctx);
            }
        }

        spawnByConfig(ctx);

        if(onTrigger != null){
            onTrigger.get(ctx);
        }

        reload = 0f;
        spacing = nextSpacing();
        if(disposable){
            markOneShotTemplateFired(id);
            remove();
        }
    }

    boolean debugFireNow(){
        Team team = resolveCheckTeam();
        if(team == null || team.cores().isEmpty()) return false;
        fire(team);
        return true;
    }

    private void spawnByConfig(TriggerContext ctx){
        if(spawnUnits.isEmpty()) return;
        if(ctx.teamSpawn == null || Float.isNaN(ctx.x) || Float.isNaN(ctx.y)) return;

        for(ObjectIntMap.Entry<UnitType> entry : spawnUnits.entries()){
            UnitType type = entry.key;
            int realAmount = resolveSpawnAmount(ctx, type, entry.value);
            if(realAmount <= 0) continue;

            SpawnUnitConfig config = spawnUnitConfigs == null ? null : spawnUnitConfigs.get(type);
            float unitShield = config == null ? -1f : config.shield;
            StatusEffect unitStatus = config == null || config.status == null ? spawnStatus : config.status;
            float unitStatusDuration = config == null || config.statusDuration < 0f ? spawnStatusDuration : config.statusDuration;
            if(unitStatus == null) unitStatus = StatusEffects.none;

            SpawnContext sctx = new SpawnContext(
            ctx,
            type,
            realAmount,
            spawnRange,
            spawnWarmup,
            spawnEachDelay,
            spawnAngle,
            unitShield,
            unitStatus,
            unitStatusDuration,
            spawnFlag
            );

            Cons<SpawnContext> invoker = resolveSpawnInvoker(type);
            if(debugForceAnyMode || debugBypassMeet){
                Log.info(
                "[WH][AutoTrigger][debug] spawn path=@ trigger=@ unit=@ amount=@",
                invoker != null ? "custom" : "default",
                id,
                type == null ? "null" : type.name,
                realAmount
                );
            }

            if(invoker != null){
                invoker.get(sctx);
            }else{
                defaultSpawn(sctx);
            }
        }
    }

    private int resolveSpawnAmount(TriggerContext ctx, UnitType type, int configuredAmount){
        int requestedAmount = Math.max(0, configuredAmount);
        int capLeft = Units.getCap(ctx.teamSpawn) - ctx.teamSpawn.data().countType(type);
        return Math.min(requestedAmount, capLeft);
    }

    private Cons<SpawnContext> resolveSpawnInvoker(UnitType type){
        if(unitSpawnerInvokers != null){
            Cons<SpawnContext> perUnitInvoker = unitSpawnerInvokers.get(type);
            if(perUnitInvoker != null){
                return perUnitInvoker;
            }
        }
        return spawnerInvoker;
    }

    private FleetWarnHudMode resolveFleetWarnHudMode(){
        return fleetWarnHudModeOverride == null ? fleetWarnHudMode : fleetWarnHudModeOverride;
    }

    private String resolveCenteredHudText(FleetWarnHudMode mode){
        if(mode != FleetWarnHudMode.centered) return "";
        if(hasText(hudText)) return hudText;
        if(hasText(chatText)) return chatText;
        return "";
    }

    private static void defaultSpawn(SpawnContext sctx){
        for(int i = 0; i < sctx.amount; i++){
            final float delay = sctx.warmup + i * sctx.eachDelay;
            Time.run(delay, () -> {
                float ux = sctx.ctx.x + Mathf.range(sctx.range);
                float uy = sctx.ctx.y + Mathf.range(sctx.range);

                Unit unit = sctx.type.spawn(sctx.ctx.teamSpawn, ux, uy);
                if(unit == null) return;

                unit.rotation(sctx.angle);
                if(sctx.shield >= 0f){
                    unit.shield = sctx.shield;
                }
                if(sctx.status != null && sctx.status != StatusEffects.none){
                    unit.apply(sctx.status, sctx.statusDuration);
                }
                if(!Double.isNaN(sctx.flag)){
                    unit.flag(sctx.flag);
                }
            });
        }
    }

    Trigger copyTemplate(){
        Trigger out = new Trigger();
        out.id = id;
        out.checkTeam = checkTeam;
        out.extraCondition = extraCondition;
        out.requiredItems = copyReqs(requiredItems);
        out.requiredUnits = copyReqs(requiredUnits);
        out.requiredBuildings = copyReqs(requiredBuildings);
        out.minTriggerWave = minTriggerWave;
        out.allowCampaign = allowCampaign;
        out.allowCustom = allowCustom;
        out.allowPvp = allowPvp;
        out.allowEditor = allowEditor;
        out.allowNonSectorMaps = allowNonSectorMaps;
        out.allowedSectorPresets = allowedSectorPresets.copy();
        out.rulesFilter = rulesFilter;
        out.spacingBase = spacingBase;
        out.spacingRand = spacingRand;
        out.checkSpacing = checkSpacing;
        out.disposable = disposable;
        out.removeIfCaptured = removeIfCaptured;
        out.warnHudEnabled = warnHudEnabled;
        out.useFleetWarnHudStyle = useFleetWarnHudStyle;
        out.fleetWarnHudDuration = fleetWarnHudDuration;
        out.fleetWarnSound = fleetWarnSound == null ? WHSounds.alert2 : fleetWarnSound;
        out.fleetWarnHudModeOverride = fleetWarnHudModeOverride;
        out.hudText = hudText;
        out.hudToastDuration = hudToastDuration;
        out.triggerToastText = triggerToastText;
        out.triggerToastDuration = triggerToastDuration;
        out.chatText = chatText;
        out.chatDuration = chatDuration;
        out.markOnTrigger = markOnTrigger;
        out.markColor = markColor == null ? Color.valueOf("ff7b69") : markColor.cpy();
        out.markRadius = markRadius;
        out.markLifetime = markLifetime;
        out.spawnTeam = spawnTeam;
        out.spawnPosition = spawnPosition;
        out.spawnUnits = copySpawner(spawnUnits);
        out.spawnUnitConfigs = copySpawnUnitConfigs(spawnUnitConfigs);
        out.spawnRange = spawnRange;
        out.spawnWarmup = spawnWarmup;
        out.spawnEachDelay = spawnEachDelay;
        out.spawnAngle = spawnAngle;
        out.spawnStatus = spawnStatus;
        out.spawnStatusDuration = spawnStatusDuration;
        out.spawnFlag = spawnFlag;
        out.requireEnemySpawnPoint = requireEnemySpawnPoint;
        out.spawnerInvoker = spawnerInvoker;
        out.unitSpawnerInvokers = copyUnitSpawnerInvokers(unitSpawnerInvokers);
        out.onTrigger = onTrigger;
        out.onClientTrigger = onClientTrigger;
        return out;
    }

    Trigger copyRuntime(int index){
        Trigger out = copyTemplate();
        out.runtimeIndex = index;
        out.resetRuntime();

        return out;
    }

    void rebindTransientFromTemplate(Trigger template){
        if(template == null) return;

        // 保持当前运行中的触发器配置与最新模板定义一致。
        requiredItems = copyReqs(template.requiredItems);
        requiredUnits = copyReqs(template.requiredUnits);
        requiredBuildings = copyReqs(template.requiredBuildings);
        minTriggerWave = template.minTriggerWave;

        allowCampaign = template.allowCampaign;
        allowCustom = template.allowCustom;
        allowPvp = template.allowPvp;
        allowEditor = template.allowEditor;
        allowNonSectorMaps = template.allowNonSectorMaps;
        allowedSectorPresets = template.allowedSectorPresets.copy();

        spacingBase = template.spacingBase;
        spacingRand = template.spacingRand;
        checkSpacing = template.checkSpacing;
        disposable = template.disposable;
        removeIfCaptured = template.removeIfCaptured;

        warnHudEnabled = template.warnHudEnabled;
        useFleetWarnHudStyle = template.useFleetWarnHudStyle;
        fleetWarnHudDuration = template.fleetWarnHudDuration;
        hudText = template.hudText;
        hudToastDuration = template.hudToastDuration;
        triggerToastText = template.triggerToastText;
        triggerToastDuration = template.triggerToastDuration;
        chatText = template.chatText;
        chatDuration = template.chatDuration;
        markOnTrigger = template.markOnTrigger;
        markColor = template.markColor == null ? Color.valueOf("ff7b69") : template.markColor.cpy();
        markRadius = template.markRadius;
        markLifetime = template.markLifetime;

        spawnUnits = copySpawner(template.spawnUnits);
        spawnUnitConfigs = copySpawnUnitConfigs(template.spawnUnitConfigs);
        spawnRange = template.spawnRange;
        spawnWarmup = template.spawnWarmup;
        spawnEachDelay = template.spawnEachDelay;
        spawnAngle = template.spawnAngle;
        spawnStatus = template.spawnStatus;
        spawnStatusDuration = template.spawnStatusDuration;
        spawnFlag = template.spawnFlag;
        requireEnemySpawnPoint = template.requireEnemySpawnPoint;

        checkTeam = template.checkTeam;
        extraCondition = template.extraCondition;
        rulesFilter = template.rulesFilter;
        spawnTeam = template.spawnTeam;
        spawnPosition = template.spawnPosition;
        spawnerInvoker = template.spawnerInvoker;
        unitSpawnerInvokers = copyUnitSpawnerInvokers(template.unitSpawnerInvokers);
        onTrigger = template.onTrigger;
        onClientTrigger = template.onClientTrigger;
        fleetWarnSound = template.fleetWarnSound == null ? WHSounds.alert2 : template.fleetWarnSound;
        fleetWarnHudModeOverride = template.fleetWarnHudModeOverride;

        resetTransientHandlers();
    }

    private void resetRuntime(){
        reload = 0f;
        checkTimer = 0f;
        spacing = nextSpacing();
    }

    private float nextSpacing(){
        long seed = ((long)id.hashCode() << 32) ^ runtimeIndex;
        rand.setSeed(seed + (long)reload + (long)Time.time);
        return spacingBase + rand.random(Math.max(0f, spacingRand));
    }

    private void resetTransientHandlers(){
        if(checkTeam == null) checkTeam = () -> Vars.state.rules.defaultTeam;
        if(extraCondition == null) extraCondition = t -> true;
        if(rulesFilter == null) rulesFilter = r -> true;
        if(spawnTeam == null) spawnTeam = () -> Vars.state.rules.waveTeam;
        if(spawnPosition == null){
            spawnPosition = Trigger::resolveDefaultSpawnPosition;
        }
        if(spawnStatus == null) spawnStatus = StatusEffects.none;
        if(markColor == null) markColor = Color.valueOf("ff7b69");
        if(fleetWarnSound == null) fleetWarnSound = WHSounds.alert2;
        if(unitSpawnerInvokers == null) unitSpawnerInvokers = new ObjectMap<>();
        if(spawnUnitConfigs == null) spawnUnitConfigs = new ObjectMap<>();
        if(onTrigger == null) onTrigger = ctx -> {
        };
        if(onClientTrigger == null) onClientTrigger = ctx -> {
        };
    }

    private static Vec2 resolveDefaultSpawnPosition(Team checkedTeam){
        if(Vars.state != null && Vars.spawner != null && Vars.state.hasSpawns()){
            Seq<Tile> spawns = Vars.spawner.getSpawns();
            if(spawns != null && !spawns.isEmpty()){
                Tile spawn = spawns.random();
                return new Vec2(spawn.getX(), spawn.getY());
            }
        }

        CoreBlock.CoreBuild core = checkedTeam == null ? null : checkedTeam.core();
        if(core != null) return new Vec2(core.x, core.y);

        return new Vec2(Vars.world.unitWidth() * 0.5f, Vars.world.unitHeight() * 0.5f);
    }

    @Override
    public void read(Reads read){
        runtimeIndex = read.i();
        id = read.str();

        Team serializedCheckTeam = TypeIO.readTeam(read);
        Team serializedSpawnTeam = TypeIO.readTeam(read);
        checkTeam = () -> serializedCheckTeam;
        spawnTeam = () -> serializedSpawnTeam;

        minTriggerWave = read.i();

        allowCampaign = read.bool();
        allowCustom = read.bool();
        allowPvp = read.bool();
        allowEditor = read.bool();
        allowNonSectorMaps = read.bool();

        int presetCount = read.i();
        allowedSectorPresets = new Seq<>(presetCount);
        for(int i = 0; i < presetCount; i++){
            String preset = read.str();
            if(preset != null && !preset.isEmpty()) allowedSectorPresets.add(preset);
        }

        spacingBase = read.f();
        spacingRand = read.f();
        checkSpacing = read.f();
        disposable = read.bool();
        read.bool(); // legacy triggerAfterAdd, kept for old save compatibility
        removeIfCaptured = read.bool();

        warnHudEnabled = read.bool();
        useFleetWarnHudStyle = read.bool();
        fleetWarnHudDuration = read.f();
        hudText = read.str();
        hudToastDuration = read.f();
        triggerToastText = read.str();
        triggerToastDuration = read.f();
        chatText = read.str();
        chatDuration = read.f();
        markOnTrigger = read.bool();
        markColor = new Color(read.f(), read.f(), read.f(), read.f());
        markRadius = read.f();
        markLifetime = read.f();

        spawnRange = read.f();
        spawnWarmup = read.f();
        spawnEachDelay = read.f();
        spawnAngle = read.f();
        int statusId = read.i();
        StatusEffect effect = statusId < 0 ? null : Vars.content.getByID(ContentType.status, statusId);
        spawnStatus = effect == null ? StatusEffects.none : effect;
        spawnStatusDuration = read.f();
        spawnFlag = read.d();
        try{
            requireEnemySpawnPoint = read.bool();
        }catch(Throwable ignored){
            requireEnemySpawnPoint = true;
        }

        int reqItems = read.i();
        requiredItems = new Seq<>(reqItems);
        for(int i = 0; i < reqItems; i++){
            Item item = TypeIO.readItem(read);
            int amount = read.i();
            if(item != null) requiredItems.add(new Requirement<>(item, amount));
        }

        int reqUnits = read.i();
        requiredUnits = new Seq<>(reqUnits);
        for(int i = 0; i < reqUnits; i++){
            UnitType unit = TypeIO.readUnitType(read);
            int amount = read.i();
            if(unit != null) requiredUnits.add(new Requirement<>(unit, amount));
        }

        int reqBuildings = read.i();
        requiredBuildings = new Seq<>(reqBuildings);
        for(int i = 0; i < reqBuildings; i++){
            Block block = TypeIO.readBlock(read);
            int amount = read.i();
            if(block != null) requiredBuildings.add(new Requirement<>(block, amount));
        }

        int spawnCount = read.i();
        spawnUnits = new ObjectIntMap<>(spawnCount);
        for(int i = 0; i < spawnCount; i++){
            UnitType type = TypeIO.readUnitType(read);
            int amount = read.i();
            if(type != null) spawnUnits.put(type, amount);
        }

        reload = read.f();
        spacing = read.f();
        checkTimer = read.f();

        spawnerInvoker = null;
        unitSpawnerInvokers = new ObjectMap<>();
        rulesFilter = r -> true;
        extraCondition = t -> true;
        onTrigger = ctx -> {
        };
        onClientTrigger = ctx -> {
        };
        fleetWarnSound = WHSounds.alert2;
        fleetWarnHudModeOverride = null;

        resetTransientHandlers();
        Trigger template = PortableAutoEventTrigger.findTemplateById(id);
        if(template != null){
            rebindTransientFromTemplate(template);
        }
    }

    @Override
    public void write(Writes write){
        write.i(runtimeIndex);
        write.str(id == null ? "" : id);

        TypeIO.writeTeam(write, resolveCheckTeam());
        TypeIO.writeTeam(write, resolveSpawnTeam());

        write.i(minTriggerWave);

        write.bool(allowCampaign);
        write.bool(allowCustom);
        write.bool(allowPvp);
        write.bool(allowEditor);
        write.bool(allowNonSectorMaps);

        write.i(allowedSectorPresets.size);
        for(int i = 0; i < allowedSectorPresets.size; i++){
            String preset = allowedSectorPresets.get(i);
            write.str(preset == null ? "" : preset);
        }

        write.f(spacingBase);
        write.f(spacingRand);
        write.f(checkSpacing);
        write.bool(disposable);
        write.bool(false); // legacy triggerAfterAdd slot
        write.bool(removeIfCaptured);

        write.bool(warnHudEnabled);
        write.bool(useFleetWarnHudStyle);
        write.f(fleetWarnHudDuration);
        write.str(hudText == null ? "" : hudText);
        write.f(hudToastDuration);
        write.str(triggerToastText == null ? "" : triggerToastText);
        write.f(triggerToastDuration);
        write.str(chatText == null ? "" : chatText);
        write.f(chatDuration);
        write.bool(markOnTrigger);
        Color c = markColor == null ? Color.valueOf("ff7b69") : markColor;
        write.f(c.r);
        write.f(c.g);
        write.f(c.b);
        write.f(c.a);
        write.f(markRadius);
        write.f(markLifetime);

        write.f(spawnRange);
        write.f(spawnWarmup);
        write.f(spawnEachDelay);
        write.f(spawnAngle);
        write.i(spawnStatus == null ? -1 : spawnStatus.id);
        write.f(spawnStatusDuration);
        write.d(spawnFlag);
        write.bool(requireEnemySpawnPoint);

        write.i(requiredItems.size);
        for(int i = 0; i < requiredItems.size; i++){
            Requirement<Item> req = requiredItems.get(i);
            TypeIO.writeItem(write, req.type);
            write.i(req.amount);
        }

        write.i(requiredUnits.size);
        for(int i = 0; i < requiredUnits.size; i++){
            Requirement<UnitType> req = requiredUnits.get(i);
            TypeIO.writeUnitType(write, req.type);
            write.i(req.amount);
        }

        write.i(requiredBuildings.size);
        for(int i = 0; i < requiredBuildings.size; i++){
            Requirement<Block> req = requiredBuildings.get(i);
            TypeIO.writeBlock(write, req.type);
            write.i(req.amount);
        }

        write.i(spawnUnits.size);
        for(ObjectIntMap.Entry<UnitType> entry : spawnUnits.entries()){
            TypeIO.writeUnitType(write, entry.key);
            write.i(entry.value);
        }

        write.f(reload);
        write.f(spacing);
        write.f(checkTimer);
    }

    @Override
    public boolean isLocal(){
        return false;
    }

    @Override
    public boolean isRemote(){
        return true;
    }

    @Override
    public <T extends Entityc> T self(){
        return (T)this;
    }

    @Override
    public <T> T as(){
        return (T)this;
    }

    @Override
    public boolean isAdded(){
        return added;
    }

    @Override
    public boolean serialize(){
        return true;
    }

    @Override
    public int classId(){
        return EntityRegister.getId(Trigger.class);
    }

    @Override
    public void afterRead(){
        resetTransientHandlers();
        Trigger template = PortableAutoEventTrigger.findTemplateById(id);
        if(template != null){
            rebindTransientFromTemplate(template);
        }
    }

    @Override
    public void afterReadAll(){

    }

    @Override
    public void beforeWrite(){

    }

    @Override
    public int id(){
        return entityId;
    }

    @Override
    public void id(int id){
        this.entityId = id;
    }

    @Override
    public String toString(){
        return "PortableAutoEventTrigger{" + "entityId=" + entityId + ", id='" + id + '\'' + '}';
    }

    private SpawnUnitConfig ensureSpawnUnitConfig(UnitType type){
        if(type == null) return null;
        if(spawnUnitConfigs == null) spawnUnitConfigs = new ObjectMap<>();

        SpawnUnitConfig config = spawnUnitConfigs.get(type);
        if(config == null){
            config = new SpawnUnitConfig();
            spawnUnitConfigs.put(type, config);
        }
        return config;
    }

    private static ObjectMap<UnitType, SpawnUnitConfig> copySpawnUnitConfigs(ObjectMap<UnitType, SpawnUnitConfig> source){
        ObjectMap<UnitType, SpawnUnitConfig> out = new ObjectMap<>();
        if(source == null || source.isEmpty()) return out;

        for(ObjectMap.Entry<UnitType, SpawnUnitConfig> entry : source.entries()){
            if(entry == null || entry.key == null || entry.value == null) continue;
            out.put(entry.key, entry.value.copy());
        }
        return out;
    }

    private static void validatePairs(Object... pairs){
        if(pairs == null || (pairs.length & 1) != 0){
            throw new IllegalArgumentException("Pairs length must be even.");
        }
    }

    public static class SpawnUnitConfig{
        /** 小于 0 表示沿用 UnitType 默认护盾值。 */
        public float shield = -1f;
        /** 为 null 表示使用触发器级 spawnStatus。 */
        public StatusEffect status = null;
        /** 小于 0 表示使用触发器级 spawnStatusDuration。 */
        public float statusDuration = -1f;

        public SpawnUnitConfig copy(){
            SpawnUnitConfig out = new SpawnUnitConfig();
            out.shield = shield;
            out.status = status;
            out.statusDuration = statusDuration;
            return out;
        }
    }

    @Override
    public Trigger clone(){
        try{
            return (Trigger)super.clone();
        }catch(CloneNotSupportedException e){
            throw new AssertionError();
        }
    }
}

