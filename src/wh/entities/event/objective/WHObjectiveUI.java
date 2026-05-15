package wh.entities.event.objective;

import arc.Core;
import arc.func.Boolp;
import arc.func.Floatp;
import arc.func.Intp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.core.UI;
import mindustry.game.MapObjectives;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import wh.content.WHContent;

import static mindustry.Vars.*;

/**
 * 独立 objective 面板，挂载在原版 waves HUD 下方。
 */
public final class WHObjectiveUI{
    public static final float maxWidth = 65f * 5f + 4f;
    private static final String panelName = "wh-objective-panel";

    private static Table hudOverlay, hudWaves, hudStatusTable;
    private static Element infoTable;

    private WHObjectiveUI(){
    }

    /**
     * 是否已经挂载到当前 HUD。
     */
    public static boolean mounted(){
        if(headless || ui == null || ui.hudGroup == null) return false;
        return ui.hudGroup.find(panelName) != null;
    }

    /**
     * HUD 被重建后自动补挂。
     */
    public static void ensureMounted(){
        if(headless) return;
        if(state == null || !state.isGame()) return;
        if(!mounted()){
            init();
        }
    }

    public static void init(){
        if(headless) return;
        if(state == null || !state.isGame()) return;

        try{
            getReferences();
            if(hudWaves == null || hudStatusTable == null) return;

            removeOldPanel();
            preProcess();

            buildObjectiveTable();

            postProcess();
        }catch(Throwable t){
            Log.err(t);
        }
    }

    /**
     * 安全获取 HUD 节点，避免 ClassCastException。
     */
    private static void getReferences(){
        hudOverlay = null;
        hudWaves = null;
        hudStatusTable = null;
        infoTable = null;

        if(ui == null || ui.hudGroup == null) return;

        Element overlay = ui.hudGroup.find("overlaymarker");
        if(!(overlay instanceof Table overlayTable)) return;
        hudOverlay = overlayTable;

        Element wavesEditorElement = hudOverlay.find("waves/editor");
        if(!(wavesEditorElement instanceof Group wavesEditor)) return;

        Element waves = wavesEditor.find("waves");
        if(!(waves instanceof Table wavesTable)) return;
        hudWaves = wavesTable;

        Element status = hudWaves.find("statustable");
        if(status instanceof Table statusTable){
            hudStatusTable = statusTable;
        }
    }
    private static void removeOldPanel(){
        if(hudWaves == null) return;
        Table old = hudWaves.find(panelName);
        if(old != null){
            old.remove();
        }
    }

    private static void preProcess(){
        if(hudWaves == null) return;
        infoTable = hudWaves.find("infotable");
        if(infoTable != null){
            infoTable.remove();
        }
    }

    private static void postProcess(){
        if(hudWaves == null) return;
        if(infoTable != null){
            hudWaves.add(infoTable).width(maxWidth).left();
        }
    }

    public static void buildObjectiveTable(){
        if(hudWaves == null) return;
        hudWaves.top().left();

        Table panel = new Table(Tex.buttonEdge4, t -> {
            Table infoT = new Table();
            infoT.touchable = Touchable.childrenOnly;
            final Object[] lastObjectiveSource = {null};
            final int[] lastObjectiveCount = {-1};

            ImageButton button = new ImageButton(Icon.downOpen, Styles.clearNonei);
            button.clicked(() -> {
                if(button.isChecked()){
                    button.getStyle().imageUp = Icon.upOpen;
                    rebuildObjectiveList(infoT);
                    Object source = state == null || state.rules == null || state.rules.objectives == null ? null : state.rules.objectives.all;
                    lastObjectiveSource[0] = source;
                    lastObjectiveCount[0] = source == null ? 0 : state.rules.objectives.all.size;
                    infoT.exited(() -> Core.scene.unfocus(infoT));
                }else{
                    button.getStyle().imageUp = Icon.downOpen;
                    Core.scene.unfocus(infoT);
                }
            });

            button.update(() -> {
                if(state.isMenu()){
                    button.setChecked(false);
                }
            });
            infoT.update(() -> {
                if(!button.isChecked()) return;
                if(state == null || state.rules == null || state.rules.objectives == null) return;

                Object source = state.rules.objectives.all;
                int size = state.rules.objectives.all.size;
                if(source != lastObjectiveSource[0] || size != lastObjectiveCount[0]){
                    rebuildObjectiveList(infoT);
                    lastObjectiveSource[0] = source;
                    lastObjectiveCount[0] = size;
                }
            });

            t.table(bl -> {
                bl.table(table -> table.label(() -> {
                    int activeCount = activeObjectiveCount();
                    return activeCount == 0 ? "[lightgray]No Objective[]" : activeCount + " Objective(s)";
                }).maxWidth(maxWidth - 40f).pad(8f, 16f, 8f, 0f).row()).growX().height(50f).marginLeft(10f);
                bl.add(button).size(50f).padLeft(10f);
            }).growX().fillY().margin(4f).padBottom(4f);

            t.row().collapser(infoT, true, button::isChecked).growX().get().setDuration(0.22f);
        });

        panel.name = panelName;
        panel.top().left();
        hudWaves.row().add(panel).left().top().margin(10f).growX().row();
    }

    private static void rebuildObjectiveList(Table infoT){
        infoT.clear();
        infoT.table().padTop(4f);

        ScrollPane pane = infoT.pane(Styles.smallPane, i -> {
            i.top().left();
            i.defaults().growX().fillX().row();
            for(MapObjectives.MapObjective mapObjective : state.rules.objectives.all){
                Table objective = getObjectiveTable(mapObjective);
                i.row().collapser(objective, true, () -> isObjectiveActive(mapObjective))
                .growX()
                .fillX()
                .get()
                .setDuration(0.22f);
            }
        }).grow().maxHeight(getHeight() / 2f).get();

        pane.name = "pane";
        pane.setFadeScrollBars(true);
        pane.setForceScroll(false, true);
    }

    public static Table getObjectiveTable(MapObjectives.MapObjective e){
        return new Table(t -> {
            t.top().left();
            t.defaults().growX().fillX().pad(6f).padBottom(6f);

            if(e instanceof MapObjectives.ResearchObjective){
                MapObjectives.ResearchObjective obj = (MapObjectives.ResearchObjective)e;
                t.add(objectiveTable(
                obj.content.fullIcon,
                () -> Mathf.num(obj.isCompleted()),
                () -> 1,
                () -> "Research:",
                obj::isCompleted
                ));
            }

            if(e instanceof MapObjectives.ItemObjective){
                MapObjectives.ItemObjective obj = (MapObjectives.ItemObjective)e;
                t.add(objectiveTable(
                obj.item.fullIcon,
                () -> state.rules.defaultTeam.items().get(obj.item),
                () -> obj.amount,
                () -> "Obtain:",
                obj::isCompleted
                ));
            }

            if(e instanceof MapObjectives.CoreItemObjective){
                MapObjectives.CoreItemObjective obj = (MapObjectives.CoreItemObjective)e;
                t.add(objectiveTable(
                obj.item.fullIcon,
                () -> state.stats.coreItemCount.get(obj.item),
                () -> obj.amount,
                () -> "Collect:",
                obj::isCompleted
                ));
            }

            if(e instanceof MapObjectives.BuildCountObjective){
                MapObjectives.BuildCountObjective obj = (MapObjectives.BuildCountObjective)e;
                t.add(objectiveTable(
                obj.block.fullIcon,
                () -> state.stats.placedBlockCount.get(obj.block, 0),
                () -> obj.count,
                () -> "Build:",
                obj::isCompleted
                ));
            }

            if(e instanceof MapObjectives.UnitCountObjective){
                MapObjectives.UnitCountObjective obj = (MapObjectives.UnitCountObjective)e;
                t.add(objectiveTable(
                obj.unit.fullIcon,
                () -> state.rules.defaultTeam.data().countType(obj.unit),
                () -> obj.count,
                () -> "Build:",
                obj::isCompleted
                ));
            }

            if(e instanceof MapObjectives.DestroyUnitsObjective){
                MapObjectives.DestroyUnitsObjective obj = (MapObjectives.DestroyUnitsObjective)e;
                t.add(objectiveTable(
                Icon.units.getRegion(),
                () -> state.stats.enemyUnitsDestroyed,
                () -> obj.count,
                () -> "Destroy:",
                obj::isCompleted
                ));
            }

            if(e instanceof MapObjectives.TimerObjective){
                MapObjectives.TimerObjective obj = (MapObjectives.TimerObjective)e;
                Floatp countup = () -> Reflect.get(obj, "countup");
                Floatp realTime = () -> obj.duration * state.rules.objectiveTimerMultiplier;
                t.add(objectiveTable(
                Icon.refresh.getRegion(),
                () -> (int)countup.get(),
                () -> (int)realTime.get(),
                () -> UI.formatTime(countup.get()) + "/" + UI.formatTime(realTime.get()),
                obj::isCompleted,
                false
                ));
            }

            if(e instanceof TriggerObjective){
                TriggerObjective obj = (TriggerObjective)e;
                Floatp countup = obj::getCountup;
                Floatp realTime = () -> obj.duration;
                t.add(objectiveTable(
                Icon.refresh.getRegion(),
                () -> (int)countup.get(),
                () -> (int)realTime.get(),
                () -> UI.formatTime(countup.get()) + "/" + UI.formatTime(realTime.get()),
                () -> countup.get() >= realTime.get(),
                false
                ));
            }

            if(e instanceof JumpInTriggerObjective){
                JumpInTriggerObjective obj = (JumpInTriggerObjective)e;
                Floatp countup = obj::getCountup;
                Floatp realTime = () -> obj.duration;
                t.add(objectiveTable(
                WHContent.fleet,
                () -> (int)countup.get(),
                () -> (int)realTime.get(),
                () -> UI.formatTime(countup.get()) + "/" + UI.formatTime(realTime.get()),
                () -> countup.get() >= realTime.get(),
                false
                ));
            }

            if(e instanceof RaidEventObjective){
                RaidEventObjective obj = (RaidEventObjective)e;
                Floatp countup = obj::getCountup;
                Floatp realTime = () -> obj.duration;
                t.add(objectiveTable(
                WHContent.bombard,
                () -> (int)countup.get(),
                () -> (int)realTime.get(),
                () -> UI.formatTime(countup.get()) + "/" + UI.formatTime(realTime.get()),
                () -> countup.get() >= realTime.get(),
                false
                ));
            }

            if(e instanceof MapObjectives.DestroyBlockObjective){
                MapObjectives.DestroyBlockObjective obj = (MapObjectives.DestroyBlockObjective)e;
                t.add(objectiveTable(
                obj.block.fullIcon,
                () -> Mathf.num(obj.isCompleted()),
                () -> 1,
                () -> "Destroy:" + obj.block.localizedName,
                obj::isCompleted
                ));
            }

            if(e instanceof MapObjectives.DestroyBlocksObjective){
                MapObjectives.DestroyBlocksObjective obj = (MapObjectives.DestroyBlocksObjective)e;
                t.add(objectiveTable(
                obj.block.fullIcon,
                obj::progress,
                () -> obj.positions.length,
                () -> "Destroy:" + obj.block.localizedName,
                obj::isCompleted
                ));
            }

            if(e instanceof MapObjectives.CommandModeObjective){
                MapObjectives.CommandModeObjective obj = (MapObjectives.CommandModeObjective)e;
                t.add(objectiveTable(
                Icon.units.getRegion(),
                () -> Mathf.num(obj.isCompleted()),
                () -> 1,
                obj::text,
                obj::isCompleted
                ));
            }

            if(e instanceof MapObjectives.FlagObjective){
                MapObjectives.FlagObjective obj = (MapObjectives.FlagObjective)e;
                t.add(objectiveTable(
                Icon.info.getRegion(),
                () -> Mathf.num(obj.isCompleted()),
                () -> 1,
                obj::text,
                obj::isCompleted
                ));
            }

            if(e instanceof MapObjectives.DestroyCoreObjective){
                MapObjectives.DestroyCoreObjective obj = (MapObjectives.DestroyCoreObjective)e;
                t.add(objectiveTable(
                Icon.effect.getRegion(),
                () -> Mathf.num(obj.isCompleted()),
                () -> 1,
                obj::text,
                obj::isCompleted
                ));
            }
        });
    }

    public static Stack objectiveTable(TextureRegion region, Intp value, Intp target, Prov<CharSequence> info, Boolp checked, boolean appendProgress){
        Bar bar = new Bar(
                () -> "",
        () -> checked.get() ? Pal.heal : Pal.accent,
        () -> {
            int tar = Math.max(1, target.get());
            return Mathf.clamp((float)value.get() / (float)tar);
        }
        );

        Table barLayer = new Table(table -> table.add(bar).height(40f).expandX().fillX());

        Table contentLayer = new Table(content -> {
            content.left();
            content.image(WHContent.safeRegion(region)).size(30f).padTop(4f).padBottom(4f).padLeft(30f).padRight(8f);

            Label infoLabel = content.label(() -> buildInfoText(info)).left().growX().padRight(8f).get();
            infoLabel.setWrap(false);
            infoLabel.setEllipsis(true);
            infoLabel.setAlignment(Align.left);

            content.label(() -> appendProgress ? buildProgressText(value, target) : "").right().padRight(10f);
        });

        Table signLayer = new Table(sign -> {
            Image status = sign.image(Icon.cancel).size(16f).expandX().left().padLeft(10f).get();
            sign.update(() -> {
                boolean done = checked.get();
                status.setDrawable(done ? Icon.ok : Icon.cancel);
                status.setColor(done ? Pal.heal : Color.lightGray);
            });
        });

        return new Stack(barLayer, contentLayer, signLayer);
    }

    public static Stack objectiveTable(TextureRegion region, Intp value, Intp target, Prov<CharSequence> info, Boolp checked){
        return objectiveTable(region, value, target, info, checked, true);
    }

    private static CharSequence buildInfoText(Prov<CharSequence> info) {
        if (info == null) return "";
        CharSequence text = info.get();
        return text == null ? "" : text;
    }

    private static String buildProgressText(Intp value, Intp target) {
        return Math.max(0, value.get()) + "/" + Math.max(0, target.get());
    }

    public static float getHeight(){
        return Core.graphics.getHeight();
    }

    private static int activeObjectiveCount(){
        if(state == null || state.rules == null || state.rules.objectives == null) return 0;
        int active = 0;
        for(MapObjectives.MapObjective objective : state.rules.objectives.all){
            if(isObjectiveActive(objective)){
                active++;
            }
        }
        return active;
    }

    private static boolean isObjectiveActive(MapObjectives.MapObjective objective){
        return objective != null && objective.qualified() && !objective.hidden && !objective.isCompleted();
    }
}
