package wh.entities.event;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
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
import wh.ui.*;

/**
 * Entity-based portable auto trigger.
 *
 * <p>Compared with manager + rules.tags persistence, this class serializes runtime state
 * directly via Mindustry entity save/load.
 *
 * <p>Note: this class depends on EntityRegister class mapping. If you copy this into another mod,
 * register {@link Trigger} in that mod's entity mapping table.
 */
public final class PortableAutoEventTrigger{
    private PortableAutoEventTrigger(){
    }

    private static boolean inited = false;
    private static final Seq<Trigger> templates = new Seq<>();
    private static final Seq<Trigger> active = new Seq<>();
    private static final Vec2 markScreenTmp = new Vec2();
    private static final Color markPulseTmp = new Color();
    private static final Color markShadowTmp = new Color();

    /** Matches AutoEventTrigger time scaling idea. */
    public static float timeScale = 1f;

    /** Auto install templates on world load when this condition returns true. */
    public static Boolf<EventType.WorldLoadEvent> autoInstall = e -> Vars.state.isGame() && !Vars.state.isEditor();

    public static void init(){
        if(inited) return;
        inited = true;

        Events.on(EventType.WorldLoadEvent.class, e -> {
            if(autoInstall.get(e) && active.isEmpty()){
                installTemplates();
            }
        });

        Events.on(EventType.ResetEvent.class, e -> clearActive());
    }

    public static void registerTemplate(Trigger trigger){
        if(trigger == null) return;
        templates.add(trigger.copyTemplate());
    }

    public static void clearTemplates(){
        templates.clear();
    }

    public static void clearActive(){
        for(int i = active.size - 1; i >= 0; i--){
            active.get(i).remove();
        }
        active.clear();
    }

    public static Seq<Trigger> active(){
        return active;
    }

    public static void installTemplates(){
        clearActive();
        for(int i = 0; i < templates.size; i++){
            Trigger runtime = templates.get(i).copyRuntime(i + 1);
            if(runtime != null){
                runtime.add();
            }
        }
    }

    public static void addRuntime(Trigger trigger){
        if(trigger == null) return;
        Trigger runtime = trigger.copyRuntime(active.size + 1);
        if(runtime != null){
            runtime.add();
        }
    }

    private static void onEntityAdded(Trigger trigger){
        if(trigger.added && !active.contains(trigger, true)){
            active.add(trigger);
        }
    }

    private static void onEntityRemoved(Trigger trigger){
        active.remove(trigger, true);
    }

    public static class Trigger implements Entityc, Cloneable{
        // identity/runtime seed
        public String id = "portable-auto-trigger";
        private int runtimeIndex = 1;

        // requirements
        public Prov<Team> checkTeam = () -> Vars.state.rules.defaultTeam;
        public Boolf<Team> extraCondition = t -> true;
        public Seq<Requirement<Item>> requiredItems = new Seq<>();
        public Seq<Requirement<UnitType>> requiredUnits = new Seq<>();
        public Seq<Requirement<Block>> requiredBuildings = new Seq<>();
        public int minTriggerWave = 0;

        // mode/map filters
        public boolean allowCampaign = true;
        public boolean allowCustom = true;
        public boolean allowPvp = false;
        public boolean allowEditor = false;
        public boolean allowNonSectorMaps = true;
        public Seq<String> allowedSectorPresets = new Seq<>();
        public Boolf<Rules> rulesFilter = r -> true;

        // timing
        public float spacingBase = 120f * Time.toSeconds;
        public float spacingRand = 120f * Time.toSeconds;
        public float checkSpacing = 120f;
        public boolean disposable = false;
        public boolean triggerAfterAdd = false;
        public boolean removeIfCaptured = true;

        // optional built-in UI
        public boolean warnHudEnabled = true;
        public boolean useFleetWarnHudStyle = false;
        public float fleetWarnHudDuration = 2.5f;
        public Sound fleetWarnSound = WHSounds.alert2;
        public String hudText = "";
        public float hudToastDuration = 2.5f;
        public String triggerToastText = "";
        public float triggerToastDuration = 2.5f;
        public String chatText = "";
        /** NH cutscene text duration (ticks). */
        public float chatDuration = 120f;
        public boolean markOnTrigger = false;
        public Color markColor = Color.valueOf("ff7b69");
        public float markRadius = 24f;
        public float markLifetime = 180f;

        // optional spawn section (can be handled by custom spawnerInvoker)
        public Prov<Team> spawnTeam = () -> Vars.state.rules.waveTeam;
        public Func<Team, Vec2> spawnPosition = checkedTeam -> {
            CoreBlock.CoreBuild core = checkedTeam.core();
            if(core != null) return new Vec2(core.x, core.y);
            return new Vec2(Vars.world.unitWidth() / 2f, Vars.world.unitHeight() / 2f);
        };
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
         * If set, this is used for spawning instead of default UnitType.spawn fallback.
         * You can bridge your own Spawner implementation here.
         */
        public Cons<SpawnContext> spawnerInvoker = null;

        // callbacks
        public Cons<TriggerContext> onTrigger = ctx -> {
        };
        public Cons<TriggerContext> onClientTrigger = ctx -> {
        };

        // runtime
        public float reload = 0f;
        public float spacing = 0f;
        public float checkTimer = 0f;

        public boolean added;
        public transient int entityId = 0;
        private final Rand rand = new Rand();

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

        public Trigger triggerAfterAdd(boolean triggerAfterAdd){
            this.triggerAfterAdd = triggerAfterAdd;
            return this;
        }

        public Trigger removeIfCaptured(boolean remove){
            this.removeIfCaptured = remove;
            return this;
        }

        public Trigger warnHudEnabled(boolean enabled){
            this.warnHudEnabled = enabled;
            return this;
        }

        public Trigger useFleetWarnHUD(boolean enabled){
            this.useFleetWarnHudStyle = enabled;
            return this;
        }

        public Trigger useFleetWarnHUD(boolean enabled, float duration){
            this.useFleetWarnHudStyle = enabled;
            this.fleetWarnHudDuration = duration;
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
            for(int i = 0; i < units.length; i += 2){
                spawnUnits.put((UnitType)units[i], ((Number)units[i + 1]).intValue());
            }
            return this;
        }

        public Trigger spawn(UnitType type, int amount){
            spawnUnits.put(type, amount);
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
            if(Vars.state.isEditor()) return allowEditor;
            if(Vars.state.rules.pvp) return allowPvp;
            if(Vars.state.isCampaign()) return allowCampaign;
            return allowCustom;
        }

        private boolean allowedInCurrentSector(){
            if(rulesFilter != null && !rulesFilter.get(Vars.state.rules)) return false;

            if(allowedSectorPresets.isEmpty()) return true;
            if(!Vars.state.hasSector() || Vars.state.rules.sector == null || Vars.state.rules.sector.preset == null){
                return allowNonSectorMaps;
            }

            SectorPreset preset = Vars.state.rules.sector.preset;
            return allowedSectorPresets.contains(preset.name);
        }

        private boolean meet(Team team){
            if(requireEnemySpawnPoint && !hasEnemySpawnPoint()) return false;
            if(Vars.state.rules.waves && Vars.state.wave < minTriggerWave) return false;
            if(!team.active()) return false;

            CoreBlock.CoreBuild core = team.core();
            if(core == null) return false;

            for(int i = 0; i < requiredItems.size; i++){
                Requirement<Item> req = requiredItems.get(i);
                if(core.items.get(req.type) < req.amount) return false;
            }

            for(int i = 0; i < requiredUnits.size; i++){
                Requirement<UnitType> req = requiredUnits.get(i);
                if(team.data().countType(req.type) < req.amount) return false;
            }

            if(!requiredBuildings.isEmpty()){
                ObjectIntMap<Block> countByBlock = new ObjectIntMap<>();
                Groups.build.each(b -> {
                    if(b.team == team){
                        countByBlock.increment(b.block, 0, 1);
                    }
                });

                for(int i = 0; i < requiredBuildings.size; i++){
                    Requirement<Block> req = requiredBuildings.get(i);
                    if(countByBlock.get(req.type, 0) < req.amount) return false;
                }
            }

            return extraCondition.get(team);
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

            if(!Vars.headless){
                if(warnHudEnabled){
                    if(useFleetWarnHudStyle){
                        showFleetWarnHud(ctx, fleetWarnHudDuration, fleetWarnSound);
                    }else if(hudText != null && !hudText.isEmpty()){
                        showInfoCompat(hudText, hudToastDuration);
                    }
                }
                if(triggerToastText != null && !triggerToastText.isEmpty()){
                    String coord = Float.isNaN(x) || Float.isNaN(y) ? "" : " [" + (int)(x / 8f) + ", " + (int)(y / 8f) + "]";
                    showInfoCompat(triggerToastText + coord, triggerToastDuration);
                }
                if(chatText != null && !chatText.isEmpty()){
                    postChatCompat(chatText, chatDuration);
                }
                if(markOnTrigger && !Float.isNaN(x) && !Float.isNaN(y)){
                    markCompat(x, y, markRadius, markLifetime, markColor);
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
            if(disposable) remove();
        }

        private void spawnByConfig(TriggerContext ctx){
            if(spawnUnits.isEmpty()) return;
            if(ctx.teamSpawn == null || Float.isNaN(ctx.x) || Float.isNaN(ctx.y)) return;

            for(ObjectIntMap.Entry<UnitType> entry : spawnUnits.entries()){
                UnitType type = entry.key;
                int amount = Math.max(0, entry.value);
                int capLeft = Units.getCap(ctx.teamSpawn) - ctx.teamSpawn.data().countType(type);
                int realAmount = Math.min(amount, capLeft);
                if(realAmount <= 0) continue;

                SpawnContext sctx = new SpawnContext(
                ctx,
                type,
                realAmount,
                spawnRange,
                spawnWarmup,
                spawnEachDelay,
                spawnAngle,
                spawnStatus,
                spawnStatusDuration,
                spawnFlag
                );

                if(spawnerInvoker != null){
                    spawnerInvoker.get(sctx);
                }else{
                    defaultSpawn(sctx);
                }
            }
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
                    if(sctx.status != null && sctx.status != StatusEffects.none){
                        unit.apply(sctx.status, sctx.statusDuration);
                    }
                    if(!Double.isNaN(sctx.flag)){
                        unit.flag(sctx.flag);
                    }
                });
            }
        }

        private Trigger copyTemplate(){
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
            out.triggerAfterAdd = triggerAfterAdd;
            out.removeIfCaptured = removeIfCaptured;
            out.warnHudEnabled = warnHudEnabled;
            out.useFleetWarnHudStyle = useFleetWarnHudStyle;
            out.fleetWarnHudDuration = fleetWarnHudDuration;
            out.fleetWarnSound = fleetWarnSound == null ? WHSounds.alert2 : fleetWarnSound;
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
            out.spawnRange = spawnRange;
            out.spawnWarmup = spawnWarmup;
            out.spawnEachDelay = spawnEachDelay;
            out.spawnAngle = spawnAngle;
            out.spawnStatus = spawnStatus;
            out.spawnStatusDuration = spawnStatusDuration;
            out.spawnFlag = spawnFlag;
            out.requireEnemySpawnPoint = requireEnemySpawnPoint;
            out.spawnerInvoker = spawnerInvoker;
            out.onTrigger = onTrigger;
            out.onClientTrigger = onClientTrigger;
            return out;
        }

        private Trigger copyRuntime(int index){
            Trigger out = copyTemplate();
            out.runtimeIndex = index;
            out.resetRuntime();

            if(out.triggerAfterAdd){
                Team team = out.resolveCheckTeam();
                if(team == null){
                    return null;
                }
                out.fire(team);
                if(out.disposable) return null;
            }

            return out;
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
                spawnPosition = checkedTeam -> {
                    CoreBlock.CoreBuild core = checkedTeam.core();
                    if(core != null) return new Vec2(core.x, core.y);
                    return new Vec2(Vars.world.unitWidth() / 2f, Vars.world.unitHeight() / 2f);
                };
            }
            if(spawnStatus == null) spawnStatus = StatusEffects.none;
            if(markColor == null) markColor = Color.valueOf("ff7b69");
            if(fleetWarnSound == null) fleetWarnSound = WHSounds.alert2;
            if(onTrigger == null) onTrigger = ctx -> {
            };
            if(onClientTrigger == null) onClientTrigger = ctx -> {
            };
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
            triggerAfterAdd = read.bool();
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
            rulesFilter = r -> true;
            extraCondition = t -> true;
            onTrigger = ctx -> {
            };
            onClientTrigger = ctx -> {
            };
            fleetWarnSound = WHSounds.alert2;

            resetTransientHandlers();
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
            write.bool(triggerAfterAdd);
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

        private static void validatePairs(Object... pairs){
            if(pairs == null || (pairs.length & 1) != 0){
                throw new IllegalArgumentException("Pairs length must be even.");
            }
        }
    }

    public static class TriggerContext{
        public final Trigger trigger;
        public final Team teamChecked;
        public final Team teamSpawn;
        public final float x;
        public final float y;

        public TriggerContext(Trigger trigger, Team teamChecked, Team teamSpawn, float x, float y){
            this.trigger = trigger;
            this.teamChecked = teamChecked;
            this.teamSpawn = teamSpawn;
            this.x = x;
            this.y = y;
        }
    }

    public static class SpawnContext{
        public final TriggerContext ctx;
        public final UnitType type;
        public final int amount;
        public final float range;
        public final float warmup;
        public final float eachDelay;
        public final float angle;
        public final StatusEffect status;
        public final float statusDuration;
        public final double flag;

        public SpawnContext(TriggerContext ctx, UnitType type, int amount, float range, float warmup, float eachDelay, float angle, StatusEffect status, float statusDuration, double flag){
            this.ctx = ctx;
            this.type = type;
            this.amount = amount;
            this.range = range;
            this.warmup = warmup;
            this.eachDelay = eachDelay;
            this.angle = angle;
            this.status = status;
            this.statusDuration = statusDuration;
            this.flag = flag;
        }
    }

    public static class Requirement<T>{
        public final T type;
        public final int amount;

        public Requirement(T type, int amount){
            this.type = type;
            this.amount = amount;
        }
    }

    private static <T> Seq<Requirement<T>> copyReqs(Seq<Requirement<T>> source){
        Seq<Requirement<T>> out = new Seq<>(source.size);
        for(int i = 0; i < source.size; i++){
            Requirement<T> req = source.get(i);
            out.add(new Requirement<>(req.type, req.amount));
        }
        return out;
    }

    private static ObjectIntMap<UnitType> copySpawner(ObjectIntMap<UnitType> source){
        ObjectIntMap<UnitType> out = new ObjectIntMap<>(source.size);
        for(ObjectIntMap.Entry<UnitType> entry : source.entries()){
            out.put(entry.key, entry.value);
        }
        return out;
    }

    private static void showInlineChatHud(String text, float duration){
        if(text == null || text.isEmpty()) return;
        if(Vars.headless || Core.scene == null || Core.scene.root == null) return;

        Table body = new Table(Tex.paneSolid);
        body.touchable = Touchable.disabled;
        body.margin(8f);
        body.add(text).color(Color.white).wrap().width(Math.min(Core.graphics.getWidth() * 0.7f, 680f)).pad(4f);

        Table container = Core.scene.table();
        container.touchable = Touchable.disabled;
        container.bottom().add(body).padBottom(Vars.mobile ? 90f : 56f);
        container.actions(
        Actions.alpha(0f),
        Actions.fadeIn(0.16f),
        Actions.delay(Math.max(0.6f, duration / 60f)),
        Actions.fadeOut(0.24f),
        Actions.remove()
        );
    }

    private static void showInlineWorldMark(float x, float y, float radius, float lifetime, Color color){
        if(Float.isNaN(x) || Float.isNaN(y)) return;
        if(Vars.headless || Core.scene == null || Core.scene.root == null) return;

        Core.scene.root.addChild(new BuiltinWorldMark(x, y, radius, lifetime, color));
    }

    private static class BuiltinWorldMark extends Table{
        final float worldX;
        final float worldY;
        final float radius;
        final float lifetime;
        final Color color = new Color();
        float life = 0f;

        BuiltinWorldMark(float x, float y, float radius, float lifetime, Color color){
            this.worldX = x;
            this.worldY = y;
            this.radius = Math.max(8f, radius);
            this.lifetime = Math.max(30f, lifetime);
            this.color.set(color == null ? Color.valueOf("ff7b69") : color);

            touchable = Touchable.disabled;
            setFillParent(true);
        }

        @Override
        public void act(float delta){
            super.act(delta);
            life += Time.delta;
            if(Vars.state == null || Vars.state.isMenu() || life > lifetime){
                remove();
            }
        }

        @Override
        public void draw(){
            super.draw();
            if(Vars.headless || Core.camera == null) return;

            Vec2 pos = markScreenTmp.set(Core.camera.project(worldX, worldY));

            float w = Core.graphics.getWidth();
            float h = Core.graphics.getHeight();
            float xPad = w * 0.05f;
            float yPad = h * 0.05f;

            boolean out = pos.x < xPad || pos.y < yPad || pos.x > w - xPad || pos.y > h - yPad;
            if(out){
                pos.x = Mathf.clamp(pos.x, xPad, w - xPad);
                pos.y = Mathf.clamp(pos.y, yPad, h - yPad);
            }

            float size = radius * (Vars.renderer == null ? 1f : Vars.renderer.getDisplayScale());
            float rotate = 45f + 90f * ((life / 120f) % 1f);

            Color pulse = markPulseTmp.set(color).lerp(Color.white, Mathf.absin(life, 6f, 0.35f)).a(parentAlpha);
            Color shadow = markShadowTmp.set(0.15f, 0.15f, 0.15f, pulse.a);

            Lines.stroke(7f, shadow);
            Lines.square(pos.x, pos.y, size + 3f, rotate);
            Lines.stroke(2.4f, pulse);
            Lines.square(pos.x, pos.y, size + 3f, rotate);

            Lines.stroke(7f, shadow);
            for(int i : Mathf.signs){
                Lines.line(Math.max(0f, i) * w, pos.y, pos.x + size * i * 1.8f, pos.y);
                Lines.line(pos.x, Math.max(0f, i) * h, pos.x, pos.y + size * i * 1.8f);
            }

            Lines.stroke(2.4f, pulse);
            for(int i : Mathf.signs){
                Lines.line(Math.max(0f, i) * w, pos.y, pos.x + size * i * 1.8f, pos.y);
                Lines.line(pos.x, Math.max(0f, i) * h, pos.x, pos.y + size * i * 1.8f);
            }

            if(out){
                Lines.stroke(2.6f, pulse);
                Lines.spikes(pos.x, pos.y, size * 1.8f, size * 0.55f, 4, 45f);
            }

            Draw.reset();
        }
    }

    private static void showFleetWarnHud(TriggerContext ctx, float duration, Sound sound){
        if(Vars.headless || Vars.player == null || Core.scene == null || Vars.state.isMenu()) return;

        Team team = ctx.teamSpawn != null ? ctx.teamSpawn : ctx.teamChecked;
        if(team == null) return;

        Color color = team.color;
        if(team != Vars.player.team() && sound != null){
            sound.play();
        }

        String alertText = "Fleet Alert";
        try{
            String fromBundle = Core.bundle.get("wh.event.fleet-alert");
            if(fromBundle != null && !fromBundle.isEmpty()){
                alertText = fromBundle;
            }
        }catch(Throwable ignored){
        }

        Table warning = new Table(Tex.paneSolid);
        warning.margin(4f);
        warning.table(t2 -> {
            t2.image().growX().height(UIUtils.OFFSET / 2f).pad(UIUtils.OFFSET / 3f).padRight(-9f).color(color);
            t2.image(WHContent.fleet).size(UIUtils.LEN - UIUtils.OFFSET).color(color);
            t2.image().growX().height(UIUtils.OFFSET / 2f).pad(UIUtils.OFFSET / 3f).padLeft(-9f).color(color);
        }).growX().pad(UIUtils.OFFSET / 2f).fillY().row();

        String finalAlertText = alertText;
        warning.table(l -> l.add("<< " + finalAlertText + " >>").color(color).padBottom(4f).row()).growX().fillY();
        warning.pack();

        Table container = Core.scene.table();
        container.top().add(warning).padTop(Vars.mobile ? 60f : 24f);
        container.setTranslation(0f, warning.getPrefHeight());
        container.actions(
        Actions.translateBy(0f, -warning.getPrefHeight(), 0.25f, Interp.fade),
        Actions.delay(Math.max(0.1f, duration)),
        Actions.run(() -> container.actions(
        Actions.translateBy(0f, warning.getPrefHeight(), 0.25f, Interp.fade),
        Actions.remove()
        ))
        );
    }

    private static void showInfoCompat(String text, float duration){
        if(Vars.headless) return;
        TextureRegionDrawable fl = new TextureRegionDrawable(WHContent.fleet);
        UIUtils.showToast(fl, text, Sounds.none);
    }

    private static void postChatCompat(String text, float duration){
        if(Vars.headless) return;
        showInlineChatHud(text, duration);
    }

    private static void markCompat(float x, float y, float radius, float lifetime, Color color){
        if(Vars.headless) return;
        showInlineWorldMark(x, y, radius, lifetime, color);
    }
}
