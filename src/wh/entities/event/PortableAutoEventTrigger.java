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
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import wh.content.*;
import wh.ui.*;

/**
 * 基于实体序列化的便携式自动触发器。
 *
 * <p>相比 manager + rules.tags 的持久化方案，本类直接依赖 Mindustry 的实体存档/读档机制
 * 序列化运行时状态。
 *
 * <p>注意：本类依赖 EntityRegister 的实体映射表。
 * 若复制到其他模组，请在该模组的实体映射中注册 {@link Trigger}。
 */

/**
 * Trigger 模板调度器（管理器）。
 * <p>
 * 主要负责：
 * - 注册触发器模板
 * - 地图加载后安装/同步运行时触发器
 * - 处理旧存档残留触发器与调试开关
 */
public final class PortableAutoEventTrigger{
    private PortableAutoEventTrigger(){
    }

    private static boolean inited = false;
    private static boolean worldLoadSyncPending = false;
    private static final Seq<Trigger> templates = new Seq<>();
    private static final Seq<Trigger> active = new Seq<>();
    private static final Vec2 markScreenTmp = new Vec2();
    private static final Color markPulseTmp = new Color();
    private static final Color markShadowTmp = new Color();

    /** 与 AutoEventTrigger 保持一致的时间缩放因子。 */
    public static float timeScale = 1f;

    private static final Boolf<EventType.WorldLoadEvent> defaultAutoInstall = e -> Vars.state.isGame() && !Vars.state.isEditor();

    /** 地图加载时，条件满足则自动安装模板。 */
    public static Boolf<EventType.WorldLoadEvent> autoInstall = defaultAutoInstall;
    /** 每次地图加载时是否清理存档中的 Trigger 实体。 */
    public static boolean purgeOnWorldLoad = true;
    /** 启用后仅在模板签名发生变化时才清理存档 Trigger。 */
    public static boolean purgeOnlyWhenTemplateChanged = true;
    /** 一次性模板已触发标记的 rules.tags 前缀。 */
    public static final String oneShotFiredTagPrefix = "wh-pat-fired:";
    /** 当前世界模板签名保存到 rules.tags 时使用的键。 */
    public static final String templateSignatureTag = "wh-pat-template-signature";

    /** 调试开关：绕过模式与扇区检查。 */
    public static boolean debugForceAnyMode = false;

    /** 调试开关：绕过刷怪点/波次/资源条件，允许强制触发辅助逻辑。 */
    public static boolean debugBypassMeet = false;

    /** 可选世界标记（十字准星），默认关闭。 */
    public static boolean worldMarkEnabled = false;

    /** 舰队预警 HUD 显示模式。 */
    public enum FleetWarnHudMode{
        legacy,
        centered
    }

    /** 全局默认使用居中 HUD。 */
    public static FleetWarnHudMode fleetWarnHudMode = FleetWarnHudMode.centered;

    public static void setFleetWarnHudMode(FleetWarnHudMode mode){
        fleetWarnHudMode = mode == null ? FleetWarnHudMode.centered : mode;
    }

    public static void init(){
        if(inited) return;
        inited = true;

        Events.on(EventType.WorldLoadEvent.class, e -> {
            worldLoadSyncPending = true;
            // 延迟 5 tick，确保反序列化实体先加入 Groups，再执行清理/同步。
            Time.run(5f, () -> finishWorldLoadSync(e));
        });

        Events.on(EventType.ResetEvent.class, e -> {
            worldLoadSyncPending = false;
            clearActive();
        });
    }

    public static void registerTemplate(Trigger trigger){
        if(trigger == null) return;
        if(trigger.id != null){
            for(int i = 0; i < templates.size; i++){
                Trigger existing = templates.get(i);
                if(existing != null && trigger.id.equals(existing.id)){
                    Log.warn("[WH][AutoTrigger] duplicate template id skipped: @ (use unique id per template)", trigger.id);
                    return;
                }
            }
        }
        templates.add(trigger.copyTemplate());
    }

    public static void clearTemplates(){
        templates.clear();
    }

    public static void setDebugForceAnyMode(boolean enabled){
        debugForceAnyMode = enabled;
    }

    public static void setDebugBypassMeet(boolean enabled){
        debugBypassMeet = enabled;
    }

    public static void enableDebugAll(boolean enabled){
        debugForceAnyMode = enabled;
        debugBypassMeet = enabled;
        autoInstall = enabled ? e -> Vars.state != null && !Vars.state.isMenu() : defaultAutoInstall;
    }

    public static int debugInstallAndFireNow(){
        if(Vars.state == null || Vars.state.isMenu()) return 0;
        installTemplates();

        int fired = 0;
        Seq<Trigger> snapshot = active.copy();
        for(int i = 0; i < snapshot.size; i++){
            Trigger trigger = snapshot.get(i);
            if(trigger != null && trigger.debugFireNow()){
                fired++;
            }
        }
        return fired;
    }

    public static void clearActive(){
        for(int i = active.size - 1; i >= 0; i--){
            active.get(i).remove();
        }
        active.clear();
    }

    /**
     * 清理当前世界存档中所有持久化 Trigger 实体。
     * 适用于旧存档仍残留过期运行时 Trigger 的场景。
     * @return 被移除的 Trigger 实体数量
     */
    public static int purgeLegacyTriggerEntities(){
        Seq<Trigger> toRemove = new Seq<>();
        Groups.all.each(e -> {
            if(e instanceof Trigger){
                toRemove.add((Trigger)e);
            }
        });

        int removed = 0;
        for(int i = 0; i < toRemove.size; i++){
            Trigger trigger = toRemove.get(i);
            if(trigger == null) continue;
            boolean wasAdded = trigger.added;
            trigger.remove();
            if(wasAdded) removed++;
        }

        active.clear();
        return removed;
    }

    public static Seq<Trigger> active(){
        return active;
    }

    public static void installTemplates(){
        clearActive();
        for(int i = 0; i < templates.size; i++){
            Trigger template = templates.get(i);
            if(shouldSkipTemplate(template)) continue;

            Trigger runtime = template.copyRuntime(i + 1);
            if(runtime != null){
                runtime.add();
            }
        }
    }

    public static void addRuntime(Trigger trigger){
        if(trigger == null) return;
        if(shouldSkipTemplate(trigger)) return;

        Trigger runtime = trigger.copyRuntime(active.size + 1);
        if(runtime != null){
            runtime.add();
        }
    }

    static void onEntityAdded(Trigger trigger){
        if(trigger.added && !active.contains(trigger, true)){
            active.add(trigger);
        }
    }

    static void onEntityRemoved(Trigger trigger){
        active.remove(trigger, true);
    }

    private static void syncActiveFromGroups(){
        active.clear();
        Groups.all.each(e -> {
            if(e instanceof Trigger){
                Trigger t = (Trigger)e;
                if(t.added && !active.contains(t, true)){
                    active.add(t);
                }
            }
        });
    }

    private static boolean shouldAutoInstall(EventType.WorldLoadEvent event){
        Boolf<EventType.WorldLoadEvent> installFilter = autoInstall == null ? defaultAutoInstall : autoInstall;
        return debugForceAnyMode || installFilter.get(event);
    }

    static boolean isWorldLoadSyncPending(){
        return worldLoadSyncPending;
    }

    private static boolean hasInstallableTemplates(){
        for(int i = 0; i < templates.size; i++){
            if(!shouldSkipTemplate(templates.get(i))) return true;
        }
        return false;
    }

    private static boolean shouldSkipTemplate(Trigger template){
        if(template == null) return true;
        return template.disposable && isOneShotTemplateFired(template.id);
    }

    private static void restoreTransientBindings(){
        if(active.isEmpty() || templates.isEmpty()) return;

        for(int i = 0; i < active.size; i++){
            Trigger runtime = active.get(i);
            if(runtime == null) continue;

            Trigger template = findTemplateById(runtime.id);
            if(template != null){
                runtime.rebindTransientFromTemplate(template);
            }
        }
    }

    /**
     * 世界加载后的触发器同步主流程：
     * 1. 先把存档里反序列化出来的 Trigger 与 templates 做一次“瞬态回调”重绑；
     * 2. 再判断模板集合是否变化，若变化则清掉旧持久化 Trigger；
     * 3. 最后仅在当前 active 为空时安装模板，避免重复叠加。
     */
    private static void finishWorldLoadSync(EventType.WorldLoadEvent event){
        try{
            resyncActiveTriggers();

            SyncResult sync = syncPersistedTriggersByTemplate();
            logWorldLoadSync(sync);

            if(shouldInstallAfterWorldLoad(event)){
                installTemplates();
            }
        }finally{
            worldLoadSyncPending = false;
        }
    }

    private static void resyncActiveTriggers(){
        // 从 Groups 抽取当前世界中的 Trigger，并恢复非序列化字段（回调 / invoker 等）。
        syncActiveFromGroups();
        restoreTransientBindings();
    }

    private static SyncResult syncPersistedTriggersByTemplate(){
        // 不需要清理：保留现有运行时 Trigger，仅刷新一次模板签名到 rules.tags。
        if(!(purgeOnWorldLoad && shouldPurgePersistedByTemplateChange())){
            persistTemplateSignature();
            return SyncResult.notChanged();
        }

        SyncResult delta = reconcileActiveWithTemplates();
        persistTemplateSignature();
        return delta;
    }

    private static void persistTemplateSignature(){
        // 用于调试和后续兼容：把“当前模板签名”写回存档标签。
        if(Vars.state == null || Vars.state.rules == null || Vars.state.rules.tags == null) return;
        Vars.state.rules.tags.put(templateSignatureTag, currentTemplateSignature());
    }

    private static void logWorldLoadSync(SyncResult sync){
        if(!(debugForceAnyMode || debugBypassMeet)) return;
        Log.info(
        "[WH][AutoTrigger][debug] world load sync: changed=@ removed=@ added=@ active=@",
        sync.templateChanged,
        sync.removedCount,
        sync.addedCount,
        active.size
        );
    }

    private static boolean shouldInstallAfterWorldLoad(EventType.WorldLoadEvent event){
        return shouldAutoInstall(event) && active.isEmpty() && hasInstallableTemplates();
    }

    private static boolean shouldPurgePersistedByTemplateChange(){
        // 总开关：直接禁用“世界加载时清理”逻辑。
        if(!purgeOnWorldLoad) return false;
        // 关闭“仅在变化时清理”后，保持旧行为：每次都清。
        if(!purgeOnlyWhenTemplateChanged) return true;
        // 世界里本来就没有持久化 Trigger，无需清理。
        if(active.isEmpty()) return false;

        // 用稳定签名比较模板与当前 active：
        // - 只比较触发器 ID 列表
        // - 先排序后拼串，避免顺序差异导致误判
        String current = currentTemplateSignature();
        String existing = currentActiveSignature();
        boolean changed = !current.equals(existing);

        if(changed && (debugForceAnyMode || debugBypassMeet)){
            Log.info("[WH][AutoTrigger][debug] signature mismatch: template=@ active=@", current, existing);
        }

        persistTemplateSignature();
        return changed;
    }

    private static String currentTemplateSignature(){
        return buildSortedSignature(collectTemplateSignatureKeys());
    }

    private static String currentActiveSignature(){
        return buildSortedSignature(collectActiveSignatureKeys());
    }

    private static Seq<String> collectTemplateSignatureKeys(){
        Seq<String> parts = new Seq<>();
        for(int i = 0; i < templates.size; i++){
            Trigger template = templates.get(i);
            // 一次性触发器若已触发，就不参与“是否需要重装”的签名比较。
            if(shouldSkipTemplate(template)) continue;
            addSignatureKey(parts, template);
        }
        return parts;
    }

    private static Seq<String> collectActiveSignatureKeys(){
        Seq<String> parts = new Seq<>(active.size);
        for(int i = 0; i < active.size; i++){
            // active 中可能有空对象或无 ID 对象，addSignatureKey 会自动过滤。
            addSignatureKey(parts, active.get(i));
        }
        return parts;
    }

    private static void addSignatureKey(Seq<String> parts, Trigger trigger){
        String key = triggerSignatureKey(trigger);
        if(key != null){
            parts.add(key);
        }
    }

    private static String triggerSignatureKey(Trigger t){
        if(t == null || t.id == null || t.id.isEmpty()) return null;
        return t.id;
    }

    private static String buildSortedSignature(Seq<String> parts){
        if(parts == null || parts.isEmpty()) return "none";

        Seq<String> normalized = normalizeSignatureKeys(parts);
        if(normalized.isEmpty()) return "none";
        normalized.sort();

        // 统一签名格式示例：size=2;test1;test2;
        // 这样日志里能直接看出“数量 + 成员”差异。
        StringBuilder builder = new StringBuilder(normalized.size * 32);
        builder.append("size=").append(normalized.size).append(';');
        for(int i = 0; i < normalized.size; i++){
            builder.append(normalized.get(i)).append(';');
        }
        return builder.toString();
    }

    private static Seq<String> normalizeSignatureKeys(Seq<String> parts){
        // 过滤 null/空串，避免无效 ID 污染签名。
        Seq<String> normalized = new Seq<>(parts.size);
        for(int i = 0; i < parts.size; i++){
            String part = parts.get(i);
            if(part == null || part.isEmpty()) continue;
            normalized.add(part);
        }
        return normalized;
    }

    /**
     * 按模板对当前 active 做增量对齐：
     * 1) 保留并重绑仍存在于模板中的 Trigger；
     * 2) 删除模板已不存在（或应跳过）的 Trigger；
     * 3) 补上 active 中缺失的模板 Trigger。
     */
    private static SyncResult reconcileActiveWithTemplates(){
        // 步骤 A：先收集“本次允许存在”的模板（去掉应跳过/无 ID/重复 ID 的模板）。
        ObjectMap<String, Trigger> installableTemplateById = new ObjectMap<>();
        Seq<Trigger> installableTemplates = new Seq<>();
        for(int i = 0; i < templates.size; i++){
            Trigger template = templates.get(i);
            if(shouldSkipTemplate(template)) continue;
            if(template == null || template.id == null || template.id.isEmpty()) continue;
            if(installableTemplateById.containsKey(template.id)) continue;
            installableTemplateById.put(template.id, template);
            installableTemplates.add(template);
        }

        // 步骤 B：遍历当前 active 快照，保留可复用实例并删除过期/重复实例。
        // keptIds 记录“已经保留”的 runtime id，避免同 id 多实例残留。
        int removedCount = 0;
        ObjectSet<String> keptIds = new ObjectSet<>();
        Seq<Trigger> snapshot = active.copy();
        for(int i = 0; i < snapshot.size; i++){
            Trigger runtime = snapshot.get(i);
            if(runtime == null) continue;

            String runtimeId = runtime.id;
            Trigger template = runtimeId == null ? null : installableTemplateById.get(runtimeId);
            boolean duplicated = runtimeId != null && keptIds.contains(runtimeId);
            if(template == null || duplicated){
                // template == null: 当前 runtime 在新模板里已不存在（或不该安装）-> 删除
                // duplicated == true: 同 id 已保留过一个实例 -> 删除重复项
                boolean wasAdded = runtime.added;
                runtime.remove();
                if(wasAdded) removedCount++;
                continue;
            }

            // 复用实例：把非序列化回调/过滤器/自定义刷怪器等从模板重新绑定回来。
            runtime.rebindTransientFromTemplate(template);
            keptIds.add(runtimeId);
        }

        // 删除后先重扫一次 active，避免后续补装时 size/index 使用旧数据。
        syncActiveFromGroups();

        // 步骤 C：把“模板里有但 active 里没有”的实例补齐。
        int addedCount = 0;
        int nextIndex = active.size + 1;
        for(int i = 0; i < installableTemplates.size; i++){
            Trigger template = installableTemplates.get(i);
            if(template == null || keptIds.contains(template.id)) continue;

            Trigger runtime = template.copyRuntime(nextIndex++);
            if(runtime == null) continue;

            runtime.add();
            keptIds.add(template.id);
            addedCount++;
        }

        // 最后统一走一次完整同步：刷新 active 列表 + 兜底重绑。
        resyncActiveTriggers();
        return SyncResult.changed(removedCount, addedCount);
    }

    /** 仅用于“世界加载同步”调试日志输出，避免主流程传一堆零散变量。 */
    private static class SyncResult{
        final boolean templateChanged;
        final int removedCount;
        final int addedCount;

        private SyncResult(boolean templateChanged, int removedCount, int addedCount){
            this.templateChanged = templateChanged;
            this.removedCount = removedCount;
            this.addedCount = addedCount;
        }

        static SyncResult notChanged(){
            return new SyncResult(false, 0, 0);
        }

        static SyncResult changed(int removedCount, int addedCount){
            return new SyncResult(true, removedCount, addedCount);
        }
    }

    private static String oneShotFiredTagKey(String id){
        return oneShotFiredTagPrefix + (id == null ? "" : id);
    }

    static boolean isOneShotTemplateFired(String id){
        if(id == null || id.isEmpty()) return false;
        if(Vars.state == null || Vars.state.rules == null || Vars.state.rules.tags == null) return false;
        return "true".equals(Vars.state.rules.tags.get(oneShotFiredTagKey(id), "false"));
    }

    static void markOneShotTemplateFired(String id){
        if(id == null || id.isEmpty()) return;
        if(Vars.state == null || Vars.state.rules == null || Vars.state.rules.tags == null) return;
        Vars.state.rules.tags.put(oneShotFiredTagKey(id), "true");
    }

    static Trigger findTemplateById(String id){
        if(id == null || id.isEmpty()) return null;

        for(int i = 0; i < templates.size; i++){
            Trigger template = templates.get(i);
            if(template != null && id.equals(template.id)){
                return template;
            }
        }
        return null;
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
        /** 单位级护盾覆盖值；小于 0 表示沿用单位默认护盾。 */
        public final float shield;
        public final StatusEffect status;
        public final float statusDuration;
        public final double flag;

        public SpawnContext(TriggerContext ctx, UnitType type, int amount, float range, float warmup, float eachDelay, float angle, float shield, StatusEffect status, float statusDuration, double flag){
            this.ctx = ctx;
            this.type = type;
            this.amount = amount;
            this.range = range;
            this.warmup = warmup;
            this.eachDelay = eachDelay;
            this.angle = angle;
            this.shield = shield;
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

    static <T> Seq<Requirement<T>> copyReqs(Seq<Requirement<T>> source){
        Seq<Requirement<T>> out = new Seq<>(source.size);
        for(int i = 0; i < source.size; i++){
            Requirement<T> req = source.get(i);
            out.add(new Requirement<>(req.type, req.amount));
        }
        return out;
    }

    static ObjectIntMap<UnitType> copySpawner(ObjectIntMap<UnitType> source){
        ObjectIntMap<UnitType> out = new ObjectIntMap<>(source.size);
        for(ObjectIntMap.Entry<UnitType> entry : source.entries()){
            out.put(entry.key, entry.value);
        }
        return out;
    }

    static ObjectMap<UnitType, Cons<SpawnContext>> copyUnitSpawnerInvokers(ObjectMap<UnitType, Cons<SpawnContext>> source){
        ObjectMap<UnitType, Cons<SpawnContext>> out = new ObjectMap<>();
        if(source == null || source.isEmpty()) return out;

        for(ObjectMap.Entry<UnitType, Cons<SpawnContext>> entry : source.entries()){
            if(entry != null && entry.key != null && entry.value != null){
                out.put(entry.key, entry.value);
            }
        }
        return out;
    }

    static void showInlineChatHud(String text, float duration){
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

    static void showInlineWorldMark(float x, float y, float radius, float lifetime, Color color){
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

    static void showFleetWarnHud(TriggerContext ctx, float duration, Sound sound, FleetWarnHudMode mode, String centeredText){
        if(Vars.headless || Vars.player == null || Core.scene == null || Vars.state.isMenu()) return;

        Team team = ctx.teamSpawn != null ? ctx.teamSpawn : ctx.teamChecked;
        if(team == null) return;

        Color color = team.color;
        if(team != Vars.player.team() && sound != null){
            sound.play();
        }

        FleetWarnHudMode renderMode = mode == null ? fleetWarnHudMode : mode;
        switch(renderMode){
            case centered:
                showFleetWarnHudCentered(color, centeredText, duration);
                return;
            case legacy:
            default:
                showFleetWarnHudLegacy(color, duration, centeredText);
        }
    }

    public static void showFleetWarnHudNow(Team team, float duration, FleetWarnHudMode mode, String text){
        if(team == null) return;
        TriggerContext ctx = new TriggerContext(null, team, team, 0f, 0f);
        showFleetWarnHud(ctx, Math.max(0.1f, duration), WHSounds.alert2, mode, text == null ? "" : text);
    }

    private static void showFleetWarnHudLegacy(Color color, float duration, String text){
        float width = Core.graphics.getWidth();
        float bannerWidth = Math.min(width * 0.78f, 620f);

        Table warning = new Table(Tex.paneSolid);
        warning.margin(4f);
        warning.table(t2 -> {
            t2.defaults().growY();
            t2.image().growX().height(Math.max(4f, UIUtils.OFFSET / 2f)).pad(UIUtils.OFFSET / 3f).padRight(-9f).color(color);
            t2.image(WHContent.fleet).size(UIUtils.LEN - UIUtils.OFFSET).color(color);
            t2.image().growX().height(Math.max(4f, UIUtils.OFFSET / 2f)).pad(UIUtils.OFFSET / 3f).padLeft(-9f).color(color);
        }).width(bannerWidth).growX().pad(UIUtils.OFFSET / 2f).fillY().row();

        if(hasText(text)){
            String legacyText = text.trim();
            if(!(legacyText.startsWith("<<") && legacyText.endsWith(">>"))){
                legacyText = "<< " + legacyText + " >>";
            }
            Label legacyLabel = new Label(legacyText);
            legacyLabel.setWrap(true);
            legacyLabel.setAlignment(Align.center);
            applyTrigBlink(legacyLabel, color);
            warning.add(legacyLabel).growX().padTop(4f).padBottom(2f).center().row();
        }

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

    private static void showFleetWarnHudCentered(Color color, String text, float duration){
        float width = Core.graphics.getWidth();
        float height = Core.graphics.getHeight() * 0.22f;

        Table warning = new Table(Tex.paneSolid);
        warning.touchable = Touchable.enabled;
        warning.margin(8f);
        warning.table(t2 -> {
            t2.defaults().growY();
            t2.image().growX().height(Math.max(4f, height * 0.06f)).padRight(-10f).color(color);
            t2.image(WHContent.fleet).size(Math.min(height * 0.68f, 140f)).color(color);
            t2.image().growX().height(Math.max(4f, height * 0.06f)).padLeft(-10f).color(color);
        }).growX().growY().row();

        if(hasText(text)){
            String formattedCenteredText = text.trim();
            if(!(formattedCenteredText.startsWith("<<") && formattedCenteredText.endsWith(">>"))){
                formattedCenteredText = "<< " + formattedCenteredText + " >>";
            }
            final String centeredAlertText = formattedCenteredText;
            warning.table(tText -> {
                tText.center();
                Label centeredLabel = new Label(centeredAlertText);
                centeredLabel.setWrap(true);
                centeredLabel.setAlignment(Align.center);
                applyTrigBlink(centeredLabel, color);
                tText.add(centeredLabel)
                .width(width * 0.72f).padTop(6f).center();
            }).growX().center().row();
        }

        Label skipHint = new Label("Left click to skip");
        skipHint.setAlignment(Align.center);
        skipHint.setFontScale(0.9f);
        skipHint.setColor(color);
        warning.add(skipHint).growX().padTop(4f).padBottom(2f).center().row();

        Table container = Core.scene.table();
        container.touchable = Touchable.enabled;
        container.setFillParent(true);
        container.center().add(warning).width(width).height(height);

        Runnable dismiss = () -> {
            container.clearActions();
            container.actions(
            Actions.fadeOut(0.22f, Interp.pow2Out),
            Actions.remove()
            );
        };
        warning.clicked(dismiss);

        container.actions(
        Actions.alpha(0f),
        Actions.fadeIn(0.28f, Interp.pow2In),
        Actions.delay(Math.max(0.1f, duration)),
        Actions.fadeOut(0.36f, Interp.pow2Out),
        Actions.remove()
        );
    }

    static boolean hasText(String text){
        return text != null && !text.isEmpty();
    }

    private static void applyTrigBlink(Label label, Color base){
        if(label == null) return;
        label.update(() -> {
            Color baseColor = Color.white.cpy().lerp(base, Mathf.absin(Time.time, 6f, 0.45f));
            float alpha = 0.55f + Mathf.absin(Time.time, 6f, 0.45f);
            label.setColor(baseColor.r, baseColor.g, baseColor.b, Mathf.clamp(alpha));
        });
    }

    static void showToastText(String text){
        if(Vars.headless || !hasText(text)) return;
        TextureRegionDrawable fl = new TextureRegionDrawable(WHContent.fleet);
        UIUtils.showToast(fl, text, Sounds.none);
    }
}
