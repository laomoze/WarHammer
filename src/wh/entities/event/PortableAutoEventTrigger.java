package wh.entities.event;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.input.*;
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

    private static final Boolf<EventType.WorldLoadEvent> defaultAutoInstall = e -> Vars.state.isGame() && !Vars.state.isEditor();

    /** Auto install templates on world load when this condition returns true. */
    public static Boolf<EventType.WorldLoadEvent> autoInstall = defaultAutoInstall;

    /** Debug: bypass mode and sector checks. */
    public static boolean debugForceAnyMode = false;

    /** Debug: bypass spawn-point/wave/resource requirements and force fire helper usage. */
    public static boolean debugBypassMeet = false;

    /** Optional world marker (crosshair) shown on trigger. Default off. */
    public static boolean worldMarkEnabled = false;

    /** Fleet warning HUD mode. */
    public enum FleetWarnHudMode{
        legacy,
        centered,
        both
    }

    /** Keep legacy HUD and add a new centered HUD by default. */
    public static FleetWarnHudMode fleetWarnHudMode = FleetWarnHudMode.both;

    public static void setFleetWarnHudMode(FleetWarnHudMode mode){
        fleetWarnHudMode = mode == null ? FleetWarnHudMode.both : mode;
    }

    public static void init(){
        if(inited) return;
        inited = true;

        Events.on(EventType.WorldLoadEvent.class, e -> {
            syncActiveFromGroups();
            restoreTransientBindings();
            Boolf<EventType.WorldLoadEvent> installFilter = autoInstall == null ? defaultAutoInstall : autoInstall;
            if((debugForceAnyMode || installFilter.get(e)) && active.isEmpty()){
                installTemplates();
            }
        });

        Events.on(EventType.ResetEvent.class, e -> clearActive());
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
            case both:
                showFleetWarnHudLegacy(color, duration);
                showFleetWarnHudCentered(color, centeredText, duration);
                return;
            case legacy:
            default:
                showFleetWarnHudLegacy(color, duration);
        }
    }

    private static void showFleetWarnHudLegacy(Color color, float duration){
        Table warning = new Table(Tex.paneSolid);
        warning.margin(4f);
        warning.table(t2 -> {
            t2.image().growX().height(UIUtils.OFFSET / 2f).pad(UIUtils.OFFSET / 3f).padRight(-9f).color(color);
            t2.image(WHContent.fleet).size(UIUtils.LEN - UIUtils.OFFSET).color(color);
            t2.image().growX().height(UIUtils.OFFSET / 2f).pad(UIUtils.OFFSET / 3f).padLeft(-9f).color(color);
        }).growX().pad(UIUtils.OFFSET / 2f).fillY().row();
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
                centeredLabel.setColor(Color.white);
                tText.add(centeredLabel)
                .width(width * 0.72f).padTop(6f).center();
            }).growX().center().row();
        }

        warning.table(t3 -> t3.right().add("[lightgray]Left-click to skip[]").padRight(8f).padBottom(4f)).growX();

        Table container = Core.scene.table();
        container.touchable = Touchable.enabled;
        container.setFillParent(true);
        container.center().add(warning).width(width).height(height);
        InputListener skipListener = new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                if(button != KeyCode.mouseLeft) return false;
                container.clearActions();
                container.actions(
                Actions.fadeOut(0.15f, Interp.fade),
                Actions.remove()
                );
                return true;
            }
        };
        container.addListener(skipListener);

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

    static void showToastText(String text){
        if(Vars.headless || !hasText(text)) return;
        TextureRegionDrawable fl = new TextureRegionDrawable(WHContent.fleet);
        UIUtils.showToast(fl, text, Sounds.none);
    }
}
