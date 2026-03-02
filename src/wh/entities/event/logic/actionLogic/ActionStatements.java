package wh.entities.event.logic.actionLogic;

import arc.*;
import arc.flabel.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.actions.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import wh.entities.*;
import wh.entities.event.logic.*;
import wh.entities.event.objective.*;
import wh.entities.event.ui.*;
import wh.graphics.*;
import wh.math.*;
import wh.ui.*;

import static wh.entities.event.logic.WHLogicStatements.registerStatement;

/**
 * 将事件动作拆分为独立逻辑语句。
 * 所有语句复用 ActionInstruction 生命周期，统一处理计时与 yield。
 */
public final class ActionStatements{
    private ActionStatements(){
    }

    /** 所有动作语句的公共父类。 */
    private abstract static class BaseActionStatement extends LStatement{
        public String run = "1";
        public String out = "result";

        /** 安全读取 token：越界或空字符串时回退默认值。 */
        protected static String tok(String[] tokens, int index, String fallback){
            if(tokens == null || index < 0 || index >= tokens.length) return fallback;
            String out = tokens[index];
            return out == null || out.trim().isEmpty() ? fallback : out;
        }

        /** 数值 token 兜底：非数字时回退默认值，避免历史脏值污染参数。 */
        protected static String tokNum(String[] tokens, int index, String fallback){
            String raw = tok(tokens, index, fallback).trim();
            try{
                Float.parseFloat(raw);
                return raw;
            }catch(Exception ignored){
                return fallback;
            }
        }

        /** 统一绘制“标签 + 输入框”。 */
        protected void fieldLabeled(Table table, String label, String value, arc.func.Cons<String> setter, float width){
            table.add(label);
            fields(table, value, setter).width(width);
        }

        protected void showTeamPickerCommon(Button button, arc.func.Cons<Team> setter){
            Team[] teams = Team.baseTeams;
            if(teams == null || teams.length == 0) return;

            showSelectTable(button, (table, hide) -> {
                table.clearChildren();
                table.margin(2f);

                Table root = new Table();
                root.left().top();
                root.defaults().growX().pad(1f);

                for(Team candidate : teams){
                    if(candidate == null) continue;
                    root.button(b -> {
                        b.left();
                        b.image().size(14f).color(candidate.color).padRight(6f);
                        Label name = new Label(candidate.name);
                        name.setFontScale(1.05f);
                        b.add(name).left().growX();
                    }, Styles.logicTogglet, () -> {
                        setter.get(candidate);
                        hide.run();
                    }).growX().height(34f);
                    root.row();
                }

                ScrollPane pane = new ScrollPane(root, Styles.smallPane);
                pane.setScrollingDisabled(true, false);
                pane.setFadeScrollBars(false);
                table.add(pane).width(Vars.mobile ? 300f : 270f).maxHeight(Vars.mobile ? 330f : 250f).left();
            });
        }

        protected void showUnitPickerCommon(Button button, arc.func.Cons<UnitType> setter){
            arc.struct.Seq<UnitType> units = Vars.content.units();
            if(units == null || units.isEmpty()) return;

            arc.struct.Seq<UnitType> visible = units.select(u -> u != null && !u.internal && !u.isHidden());

            showSelectTable(button, (popup, hide) -> {
                popup.clearChildren();
                popup.margin(2f);

                Table root = new Table();
                root.left().top();

                TextField search = new TextField("");
                search.setMessageText("search unit...");
                root.add(search).growX().height(34f).padBottom(4f).row();

                Table list = new Table();
                list.left().top();
                list.defaults().growX().pad(1f);

                UIUtils.bindContentSearch(search, list, visible, candidate -> {
                    list.button(b -> {
                        b.left();
                        b.image(candidate.fullIcon == null ? candidate.uiIcon : candidate.fullIcon).size(18f).padRight(6f);
                        String name = candidate.localizedName == null ? candidate.name : candidate.localizedName;
                        Label label = new Label(compactLabelText(name));
                        label.setFontScale(1.05f);
                        b.add(label).left().growX();
                    }, Styles.logicTogglet, () -> {
                        setter.get(candidate);
                        hide.run();
                    }).growX().height(34f);
                    list.row();
                });

                ScrollPane pane = new ScrollPane(list, Styles.smallPane);
                pane.setScrollingDisabled(true, false);
                pane.setFadeScrollBars(false);

                float paneWidth = Vars.mobile ? 300f : 270f;
                float paneHeight = Vars.mobile ? 330f : 250f;
                root.add(pane).width(paneWidth).maxHeight(paneHeight).left().row();

                popup.add(root).left();
            });
        }

        protected String compactLabelText(String text){
            if(text == null) return "";
            String out = text.trim();
            if(out.length() <= 20) return out;
            return out.substring(0, 19) + "...";
        }

        /** 通用写回格式：opcode run arg... out。 */
        protected void writeCommon(StringBuilder builder, String opcode, String... args){
            builder.append(opcode).append(" ");
            builder.append(ActionLogicSupport.safeToken(run, "1"));
            for(String arg : args){
                builder.append(" ").append(ActionLogicSupport.safeToken(arg, "_"));
            }
            builder.append(" ").append(ActionLogicSupport.safeToken(out, "result"));
        }

        @Override
        public boolean privileged(){
            // 允许在普通逻辑处理器中使用。
            return false;
        }

        @Override
        public LCategory category(){
            return WHLogicStatements.autoTriggerCategory;
        }
    }

    /** 相机平滑移动到目标世界坐标。 */
    public static class CameraControlStatement extends BaseActionStatement{
        public String seconds = "1";
        public String x = "0";
        public String y = "0";

        public CameraControlStatement(){
        }

        public CameraControlStatement(String[] tokens){
            run = tok(tokens, 1, run);
            seconds = tok(tokens, 2, seconds);
            x = tok(tokens, 3, x);
            y = tok(tokens, 4, y);
            out = outToken(tok(tokens, 5, out), out);
        }

        private String outToken(String value, String fallback){
            if(value == null) return fallback;
            String token = value.trim();
            if(token.isEmpty()) return fallback;
            try{
                Float.parseFloat(token);
                return fallback;
            }catch(Exception ignored){
                return value;
            }
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, " sec ", seconds, v -> seconds = v, 72f);
            table.row();
            fieldLabeled(table, "x ", x, v -> x = v, 110f);
            fieldLabeled(table, " y ", y, v -> y = v, 110f);
            table.row();
            fieldLabeled(table, "result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vSec = builder.var(seconds);
            final LVar vx = builder.var(x);
            final LVar vy = builder.var(y);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                private final Vec2 start = new Vec2();
                private final Vec2 target = new Vec2();
                private final Vec2 Tmp = new Vec2();
                @Override
                protected boolean begin(LExecutor exec){
                    float sec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vSec), 0f, exec));
                    target.set(
                    ActionLogicSupport.parseWorldCoord(ActionLogicSupport.valueText(vx), 0f, exec),
                    ActionLogicSupport.parseWorldCoord(ActionLogicSupport.valueText(vy), 0f, exec)
                    );
                    if(sec <= 0f){
                        if(!Vars.headless){
                            Vars.control.input.logicCutscene = true;
                            Vars.control.input.logicCamSpeed = 10f;
                            Vars.control.input.logicCamPan = target;
                        }
                        return true;
                    }
                    if(!Vars.headless){
                        Vars.control.input.logicCutscene = true;
                        start.set(Core.camera.position);
                    }

                    startTimed(sec * Time.toSeconds);
                    return true;
                }

                @Override
                protected void update(LExecutor exec, float progress){
                    if(Vars.headless) return;

                    Tmp.set(start).lerp(target, Mathf.clamp(progress));
                    Vars.control.input.logicCamSpeed = 10f;
                    Vars.control.input.logicCamPan = Tmp;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-camera-control", seconds, x, y);
        }
    }

    /** 相机平滑回到玩家位置。 */
    public static class CameraResetStatement extends BaseActionStatement{
        public String seconds = "1";

        public CameraResetStatement(){
        }

        public CameraResetStatement(String[] tokens){
            run = tok(tokens, 1, run);
            seconds = tok(tokens, 2, seconds);
            out = tok(tokens, 3, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, " sec ", seconds, v -> seconds = v, 72f);
            table.row();
            fieldLabeled(table, "result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vSec = builder.var(seconds);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    float sec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vSec), 0f, exec));
                    if(sec <= 0f){
                        if(!Vars.headless){
                            Vars.control.input.logicCutscene = true;
                            Vars.control.input.logicCamSpeed = 1000f;
                            Tmp.v1.set(Vars.player);
                            Vars.control.input.logicCamPan = Tmp.v1;
                        }
                        return true;
                    }
                    if(!Vars.headless){
                        Vars.control.input.logicCutscene = true;
                    }
                    startTimed(sec * Time.toSeconds);
                    return true;
                }

                @Override
                protected void update(LExecutor exec, float progress){
                    if(Vars.headless) return;
                    Tmp.v1.set(Core.camera.position).lerpDelta(Vars.player, progress);
                    Vars.control.input.logicCamSpeed = 1000f;
                    Vars.control.input.logicCamPan = Tmp.v1;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-camera-reset", seconds);
        }
    }

    /** 设置过场相机缩放倍数。 */
    public static class CameraZoomStatement extends BaseActionStatement{
        public String zoom = "1";

        public CameraZoomStatement(){
        }

        public CameraZoomStatement(String[] tokens){
            run = tok(tokens, 1, run);
            zoom = tok(tokens, 2, zoom);
            out = tok(tokens, 3, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, " zoom ", zoom, v -> zoom = v, 90f);
            table.row();
            fieldLabeled(table, "result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vz = builder.var(zoom);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    if(!Vars.headless){
                        Vars.control.input.logicCutsceneZoom = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vz), 1f, exec);
                    }
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-camera-zoom", zoom);
        }
    }


    /** 幕布动画：淡入 -> 停留 -> 淡出（黑边开合）。 */
    public static class CurtainDrawStatement extends BaseActionStatement{
        public String fadeInSec = "1";
        public String holdSec = "1";
        public String fadeOutSec = "1";

        public CurtainDrawStatement(){
        }

        public CurtainDrawStatement(String[] tokens){
            run = tok(tokens, 1, run);
            if(tokens != null && tokens.length >= 6){
                fadeInSec = tokNum(tokens, 2, fadeInSec);
                holdSec = tokNum(tokens, 3, holdSec);
                fadeOutSec = tokNum(tokens, 4, fadeOutSec);
                out = tok(tokens, 5, out);
            }else{
                // 兼容旧格式：run out
                out = tok(tokens, 2, out);
            }
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, " fadeIn ", fadeInSec, v -> fadeInSec = v, 72f);
            fieldLabeled(table, " hold ", holdSec, v -> holdSec = v, 72f);
            table.row();
            fieldLabeled(table, " fadeOut ", fadeOutSec, v -> fadeOutSec = v, 72f);
            fieldLabeled(table, " result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vIn = builder.var(fadeInSec);
            final LVar vHold = builder.var(holdSec);
            final LVar vOut = builder.var(fadeOutSec);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                private float inTicks = 0f;
                private float holdTicks = 0f;
                private float outTicks = 0f;
                private float totalTicks = 0f;

                @Override
                protected boolean begin(LExecutor exec){
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vIn), 1f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vHold), 1f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOut), 1f, exec));

                    inTicks = inSec * Time.toSeconds;
                    holdTicks = hold * Time.toSeconds;
                    outTicks = outSec * Time.toSeconds;
                    totalTicks = inTicks + holdTicks + outTicks;

                    if(!Vars.headless){
                        ActionLogicSupport.ensureCutsceneUI();
                        ActionContext.cutsceneUI.curtainProgress = 0f;
                    }

                    if(totalTicks <= 0f){
                        return true;
                    }

                    startTimed(totalTicks);
                    return true;
                }

                @Override
                protected void update(LExecutor exec, float progress){
                    if(Vars.headless) return;

                    float elapsed = progress * totalTicks;
                    float p;

                    if(inTicks > 0f && elapsed < inTicks){
                        p = elapsed / inTicks;
                    }else if(elapsed < inTicks + holdTicks){
                        p = 1f;
                    }else if(outTicks > 0f){
                        p = 1f - (elapsed - inTicks - holdTicks) / outTicks;
                    }else{
                        p = 0f;
                    }

                    ActionContext.cutsceneUI.curtainProgress = Mathf.clamp(p);
                }

                @Override
                protected void end(LExecutor exec){
                    if(!Vars.headless){
                        ActionContext.cutsceneUI.curtainProgress = 0f;
                    }
                }

                @Override
                protected void cancel(LExecutor exec){
                    end(exec);
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-curtain", fadeInSec, holdSec, fadeOutSec);
        }
    }

    /** 全屏幕布：淡入 -> 停留 -> 淡出。 */
    public static class CurtainFadeInStatement extends BaseActionStatement{
        public String fadeInSec = "1";
        public String holdSec = "1";
        public String fadeOutSec = "1";

        public CurtainFadeInStatement(){
        }

        public CurtainFadeInStatement(String[] tokens){
            run = tok(tokens, 1, run);
            if(tokens != null && tokens.length >= 6){
                fadeInSec = tok(tokens, 2, fadeInSec);
                holdSec = tok(tokens, 3, holdSec);
                fadeOutSec = tok(tokens, 4, fadeOutSec);
                out = tok(tokens, 5, out);
            }
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, " fadeIn ", fadeInSec, v -> fadeInSec = v, 72f);
            fieldLabeled(table, " hold ", holdSec, v -> holdSec = v, 72f);
            table.row();
            fieldLabeled(table, " fadeOut ", fadeOutSec, v -> fadeOutSec = v, 72f);
            fieldLabeled(table, " result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vIn = builder.var(fadeInSec);
            final LVar vHold = builder.var(holdSec);
            final LVar vOut = builder.var(fadeOutSec);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                private float inTicks = 0f;
                private float holdTicks = 0f;
                private float outTicks = 0f;
                private float totalTicks = 0f;

                @Override
                protected boolean begin(LExecutor exec){
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vIn), 1f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vHold), 1f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOut), 1f, exec));

                    inTicks = inSec * Time.toSeconds;
                    holdTicks = hold * Time.toSeconds;
                    outTicks = outSec * Time.toSeconds;
                    totalTicks = inTicks + holdTicks + outTicks;

                    if(!Vars.headless){
                        ActionLogicSupport.ensureCutsceneUI();
                        ActionContext.cutsceneUI.targetOverlayAlpha = 0f;
                        ActionContext.cutsceneUI.curtain.color.a = 0f;
                    }
                    if(totalTicks <= 0f){
                        return true;
                    }
                    // 单条语句完成：淡入 -> 停留 -> 淡出。
                    startTimed(totalTicks);
                    return true;
                }

                @Override
                protected void update(LExecutor exec, float progress){
                    if(Vars.headless) return;

                    float elapsed = progress * totalTicks;
                    float alpha = 0f;

                    if(inTicks > 0f && elapsed < inTicks){
                        alpha = Mathf.clamp(elapsed / inTicks);
                    }else if(elapsed < inTicks + holdTicks){
                        alpha = 1f;
                    }else if(outTicks > 0f){
                        float pOut = (elapsed - inTicks - holdTicks) / outTicks;
                        alpha = Mathf.clamp(1f - pOut);
                    }

                    ActionContext.cutsceneUI.targetOverlayAlpha = alpha;
                    ActionContext.cutsceneUI.curtain.color.a = alpha;
                }

                @Override
                protected void end(LExecutor exec){
                    if(Vars.headless) return;
                    ActionContext.cutsceneUI.targetOverlayAlpha = 0f;
                    ActionContext.cutsceneUI.curtain.color.a = 0f;
                }

                @Override
                protected void cancel(LExecutor exec){
                    end(exec);
                }

                @Override
                protected boolean enableGlobalFade(){
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-curtain-fade", fadeInSec, holdSec, fadeOutSec);
        }
    }

    /** 设置信息面板文本。 */
    public static class InfoTextStatement extends BaseActionStatement{
        public String text = "helloWorld";
        public String fadeInSec = "0.25";
        public String holdSec = "0.5";
        public String fadeOutSec = "0.25";
        public String overlayAlpha = "0";
        public String color = "ffffff";
        public String fontScale = "1";

        public InfoTextStatement(){
        }

        public InfoTextStatement(String[] tokens){
            run = tok(tokens, 1, run);
            if(tokens != null && tokens.length >= 10){
                text = tok(tokens, 2, text);
                fadeInSec = tokNum(tokens, 3, fadeInSec);
                holdSec = tokNum(tokens, 4, holdSec);
                fadeOutSec = tokNum(tokens, 5, fadeOutSec);
                overlayAlpha = tokNum(tokens, 6, overlayAlpha);
                color = tok(tokens, 7, color);
                fontScale = tokNum(tokens, 8, fontScale);
                out = tok(tokens, 9, out);
            }else if(tokens != null && tokens.length >= 8){
                text = tok(tokens, 2, text);
                fadeInSec = tokNum(tokens, 3, fadeInSec);
                holdSec = tokNum(tokens, 4, holdSec);
                fadeOutSec = tokNum(tokens, 5, fadeOutSec);
                overlayAlpha = tokNum(tokens, 6, overlayAlpha);
                out = tok(tokens, 7, out);
            }else{
                text = tok(tokens, 2, text);
                out = tok(tokens, 3, out);
            }
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, " text ", text, v -> text = v, 170f);
            table.row();
            fieldLabeled(table, " fadeIn ", fadeInSec, v -> fadeInSec = v, 72f);
            fieldLabeled(table, " hold ", holdSec, v -> holdSec = v, 72f);
            fieldLabeled(table, " fadeOut ", fadeOutSec, v -> fadeOutSec = v, 72f);
            table.row();
            fieldLabeled(table, " overlay ", overlayAlpha, v -> overlayAlpha = v, 72f);
            fieldLabeled(table, " color ", color, v -> color = v, 96f);
            fieldLabeled(table, " size ", fontScale, v -> fontScale = v, 72f);
            table.row();
            fieldLabeled(table, " result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final String textToken = text;
            final LVar vTextVar = textToken != null && textToken.startsWith("$") && textToken.length() > 1 ? builder.var(textToken.substring(1)) : null;
            final LVar vIn = builder.var(fadeInSec);
            final LVar vHold = builder.var(holdSec);
            final LVar vOut = builder.var(fadeOutSec);
            final LVar vOverlay = builder.var(overlayAlpha);
            final LVar vColor = builder.var(color);
            final LVar vScale = builder.var(fontScale);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){

                @Override
                protected boolean begin(LExecutor exec){
                    if(Vars.headless) return true;
                    ActionLogicSupport.ensureCutsceneUI();
                    String val = ActionLogicSupport.parseText(
                    vTextVar == null ? textToken : ActionLogicSupport.valueText(vTextVar));
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vIn), 0.25f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vHold), 0.5f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOut), 0.25f, exec));
                    float totalSec = inSec + hold + outSec;
                    float overlay = Mathf.clamp(ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOverlay), 0f, exec));
                    Color fontColor = ActionLogicSupport.parseColor(ActionLogicSupport.valueText(vColor), Color.white);
                    float scale = Mathf.clamp(ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vScale), 1f, exec), 0.25f, 4f);

                    // 单条语句流程：淡入 -> 停留 -> 淡出。
                    ActionContext.cutsceneUI.infoLabel = new FLabel(val);
                    ActionContext.cutsceneUI.infoLabel.setStyle(Styles.outlineLabel);
                    ActionContext.cutsceneUI.infoLabel.setColor(fontColor);
                    ActionContext.cutsceneUI.infoLabel.setFontScale(scale);
                    ActionContext.cutsceneUI.infoTable.clear();
                    ActionContext.cutsceneUI.infoTable.add(ActionContext.cutsceneUI.infoLabel);

                    ActionContext.cutsceneUI.infoTable.clearActions();
                    ActionContext.cutsceneUI.infoTable.color.a = 0f;
                    ActionContext.cutsceneUI.infoTable.actions(
                    Actions.sequence(
                    Actions.alpha(0f),
                    Actions.fadeIn(inSec, WHInterp.bounce5Out),
                    Actions.delay(hold),
                    Actions.fadeOut(outSec, Interp.pow2In)
                    )
                    );

                    if(overlay > 0.001f && totalSec > 0f){
                        ActionLogicSupport.pulseOverlay(overlay, totalSec * Time.toSeconds);
                    }
                    if(totalSec <= 0f){
                        return true;
                    }
                    startTimed(totalSec * Time.toSeconds);
                    return true;
                }

                @Override
                protected void end(LExecutor exec){
                    if(Vars.headless) return;
                    ActionContext.cutsceneUI.infoLabel = new FLabel("");
                    ActionContext.cutsceneUI.infoTable.clear();
                    ActionContext.cutsceneUI.infoTable.add(ActionContext.cutsceneUI.infoLabel);
                    ActionContext.cutsceneUI.infoTable.actions(Actions.alpha(0f));
                }

                @Override
                protected void cancel(LExecutor exec){
                    end(exec);
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-info-text", text, fadeInSec, holdSec, fadeOutSec, overlayAlpha, color, fontScale);
        }
    }

    /** 锁定玩家输入，进入过场控制。 */
    public static class InputLockStatement extends BaseActionStatement{
        public InputLockStatement(){
        }

        public InputLockStatement(String[] tokens){
            run = tok(tokens, 1, run);
            out = tok(tokens, 2, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, "result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    if(Vars.headless) return true;
                    ActionLogicSupport.ensureCutsceneUI();
                    ActionContext.cutsceneUI.controlOverride = true;
                    Vars.control.input.logicCamPan = Core.camera.position;
                    Vars.control.input.logicCutscene = true;
                    Vars.control.input.config.forceHide();
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-input-lock");
        }
    }

    /** 解锁玩家输入，退出过场控制。 */
    public static class InputUnlockStatement extends BaseActionStatement{
        public InputUnlockStatement(){
        }

        public InputUnlockStatement(String[] tokens){
            run = tok(tokens, 1, run);
            out = tok(tokens, 2, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, "result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    if(Vars.headless) return true;
                    ActionLogicSupport.ensureCutsceneUI();
                    ActionContext.cutsceneUI.controlOverride = false;
                    Vars.control.input.logicCutscene = false;
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-input-unlock");
        }
    }

    /** 生成一批跳入单位。 */
    public static class JumpInStatement extends BaseActionStatement{
        private static final float DEFAULT_TIMER_DURATION_SEC = 3f;

        public String unit = "@alpha";
        public String team = "@crux";
        public String x = "0";
        public String y = "0";
        public String angle = "90";
        public String delaySec = "0";
        public String inaccuracy = "0";
        public String timerDelaySec = "0";

        public JumpInStatement(){
        }

        public JumpInStatement(String[] tokens){
            run = tok(tokens, 1, run);
            unit = tok(tokens, 2, unit);
            team = tok(tokens, 3, team);
            x = tok(tokens, 4, x);
            y = tok(tokens, 5, y);
            angle = tok(tokens, 6, angle);
            delaySec = tok(tokens, 7, delaySec);
            inaccuracy = tok(tokens, 8, inaccuracy);
            timerDelaySec = tokNum(tokens, 9, timerDelaySec);
            out = tok(tokens, 10, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 64f);
            fieldLabeled(table, " unit ", unit, v -> unit = v, 100f);
            TextButton unitPick = new TextButton("pick", Styles.logict);
            unitPick.clicked(() -> showUnitPickerCommon(unitPick, selected -> unit = "@" + selected.name));
            table.add(unitPick).size(64f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, "team ", team, v -> team = v, 100f);
            TextButton teamPick = new TextButton("pick", Styles.logict);
            teamPick.clicked(() -> showTeamPickerCommon(teamPick, selected -> team = "@" + selected.name));
            table.add(teamPick).size(64f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, "x ", x, v -> x = v, 82f);
            fieldLabeled(table, " y ", y, v -> y = v, 82f);
            fieldLabeled(table, " ang ", angle, v -> angle = v, 82f);
            table.row();
            fieldLabeled(table, "delay ", delaySec, v -> delaySec = v, 90f);
            fieldLabeled(table, " spread ", inaccuracy, v -> inaccuracy = v, 90f);
            table.row();
            fieldLabeled(table, " tDelay ", timerDelaySec, v -> timerDelaySec = v, 84f);
            fieldLabeled(table, " result ", out, v -> out = v, 90f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vu = builder.var(unit);
            final LVar vt = builder.var(team);
            final LVar vx = builder.var(x);
            final LVar vy = builder.var(y);
            final LVar va = builder.var(angle);
            final LVar vd = builder.var(delaySec);
            final LVar vi = builder.var(inaccuracy);
            final LVar vTimerDelay = builder.var(timerDelaySec);
            final LVar vo = builder.var(out);
            final String autoTimerKeyBase = "jumpin-" + Math.abs((unit + "|" + team + "|" + x + "|" + y + "|" + angle).hashCode());
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    UnitType unitType = ActionLogicSupport.parseUnitType(ActionLogicSupport.valueText(vu));
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vt), Team.derelict);
                    float worldX = ActionLogicSupport.parseWorldCoord(ActionLogicSupport.valueText(vx), 0f, exec);
                    float worldY = ActionLogicSupport.parseWorldCoord(ActionLogicSupport.valueText(vy), 0f, exec);
                    float angleVal = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(va), 0f, exec);
                    float delay = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vd), 0f, exec) * Time.toSeconds;
                    float spread = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vi), 0f, exec);

                    Spawner spawner = new Spawner();
                    Tmp.v1.trns(Mathf.random(360f), Mathf.random(spread));
                    spawner.init(unitType, teamVal, new Vec2(worldX + Tmp.v1.x, worldY + Tmp.v1.y), angleVal, delay, false);
                    spawner.add();

                    float objectiveDelaySec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vTimerDelay), 0f, exec));
                    String timerKey = autoTimerKeyBase;
                    if(exec != null && exec.build != null){
                        timerKey += "-" + exec.build.id;
                    }
                    JumpInTriggerObjective.obtain(timerKey).trigger(DEFAULT_TIMER_DURATION_SEC * Time.toSeconds, objectiveDelaySec);
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-jump-in", unit, team, x, y, angle, delaySec, inaccuracy, timerDelaySec);
        }
    }

    /** 在世界中显示标记。 */
    public static class MarkWorldStatement extends BaseActionStatement{
        public String x = "0";
        public String y = "0";
        public String radius = "80";
        public String timeSec = "3";
        public String style = "0";
        public String team = "@sharded";

        public MarkWorldStatement(){
        }

        public MarkWorldStatement(String[] tokens){
            run = tok(tokens, 1, run);
            x = tok(tokens, 2, x);
            y = tok(tokens, 3, y);
            radius = tok(tokens, 4, radius);
            timeSec = tok(tokens, 5, timeSec);
            style = tok(tokens, 6, style);
            team = tok(tokens, 7, team);
            out = tok(tokens, 8, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 64f);
            fieldLabeled(table, " x ", x, v -> x = v, 82f);
            fieldLabeled(table, " y ", y, v -> y = v, 82f);
            table.row();
            fieldLabeled(table, "r ", radius, v -> radius = v, 82f);
            fieldLabeled(table, " sec ", timeSec, v -> timeSec = v, 82f);
            fieldLabeled(table, " style ", style, v -> style = v, 72f);
            table.row();
            fieldLabeled(table, "team ", team, v -> team = v, 96f);
            TextButton teamPick = new TextButton("pick", Styles.logict);
            teamPick.clicked(() -> showTeamPickerCommon(teamPick, selected -> {
                team = "@" + selected.name;
            }));
            table.add(teamPick).size(70f, 32f).padLeft(4f);
            fieldLabeled(table, " result ", out, v -> out = v, 96f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vx = builder.var(x);
            final LVar vy = builder.var(y);
            final LVar vrad = builder.var(radius);
            final LVar vsec = builder.var(timeSec);
            final LVar vst = builder.var(style);
            final LVar vteam = builder.var(team);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    if(Vars.headless) return true;
                    ActionLogicSupport.ensureCutsceneUI();

                    float worldX = ActionLogicSupport.parseWorldCoord(ActionLogicSupport.valueText(vx), 0f, exec);
                    float worldY = ActionLogicSupport.parseWorldCoord(ActionLogicSupport.valueText(vy), 0f, exec);
                    float r = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vrad), 80f, exec);
                    float t = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vsec), 3f, exec);
                    int styleId = ActionLogicSupport.parseInt(ActionLogicSupport.valueText(vst), 0);
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vteam), Team.derelict);

                    ActionContext.cutsceneUI.mark(
                    worldX, worldY, r, t * Time.toSeconds,
                    teamVal.color, ActionLogicSupport.markStyle(styleId),
                    (mx, my, mradius, progress, pulse, lifeTicks, maxLifeTicks, tint, markStyle, icon) -> {
                        Draw.z(Layer.flyingUnit + 1);
                        Draw.color(tint);
                        Draw.alpha(0.22f + progress * 0.14f);
                        Lines.stroke(2.2f);
                        Lines.circle(mx, my, mradius * pulse);

                        Draw.alpha(0.95f);
                        Lines.stroke(3.2f);
                        Drawn.circlePercent(mx, my, mradius + 1.5f, progress, 90f);

                        if(icon != null){
                            Draw.color(tint);
                            Draw.alpha(0.98f);
                            float size = Math.max(16f, mradius * 0.9f);
                            Draw.rect(icon, mx, my, size, size);
                        }
                    }
                    );
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-mark-world", x, y, radius, timeSec, style, team);
        }
    }

    /** 生成一次远程打击子弹。 */
    public static class RaidStatement extends BaseActionStatement{
        public String team = "@crux";
        public String bulletType = "0";
        public String srcX = "0";
        public String srcY = "0";
        public String dstX = "0";
        public String dstY = "0";
        public String inaccuracy = "80";

        public RaidStatement(){
        }

        public RaidStatement(String[] tokens){
            run = tok(tokens, 1, run);
            team = tok(tokens, 2, team);
            bulletType = tok(tokens, 3, bulletType);
            srcX = tok(tokens, 4, srcX);
            srcY = tok(tokens, 5, srcY);
            dstX = tok(tokens, 6, dstX);
            dstY = tok(tokens, 7, dstY);
            inaccuracy = tok(tokens, 8, inaccuracy);
            out = tok(tokens, 9, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 64f);
            fieldLabeled(table, " team ", team, v -> team = v, 78f);
            TextButton teamPick = new TextButton("pick", Styles.logict);
            teamPick.clicked(() -> showTeamPickerCommon(teamPick, selected -> team = "@" + selected.name));
            table.add(teamPick).size(64f, 32f).padLeft(4f);
            fieldLabeled(table, " type ", bulletType, v -> bulletType = v, 72f);
            table.row();
            fieldLabeled(table, "sx ", srcX, v -> srcX = v, 74f);
            fieldLabeled(table, " sy ", srcY, v -> srcY = v, 74f);
            fieldLabeled(table, " dx ", dstX, v -> dstX = v, 74f);
            fieldLabeled(table, " dy ", dstY, v -> dstY = v, 74f);
            table.row();
            fieldLabeled(table, "spread ", inaccuracy, v -> inaccuracy = v, 86f);
            fieldLabeled(table, " result ", out, v -> out = v, 86f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vt = builder.var(team);
            final LVar vb = builder.var(bulletType);
            final LVar vsx = builder.var(srcX);
            final LVar vsy = builder.var(srcY);
            final LVar vdx = builder.var(dstX);
            final LVar vdy = builder.var(dstY);
            final LVar vspread = builder.var(inaccuracy);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vt), Team.derelict);
                    int bulletID = ActionLogicSupport.parseInt(ActionLogicSupport.valueText(vb), 0);
                    float sourceX = ActionLogicSupport.parseWorldCoord(ActionLogicSupport.valueText(vsx), 0f, exec);
                    float sourceY = ActionLogicSupport.parseWorldCoord(ActionLogicSupport.valueText(vsy), 0f, exec);
                    float targetX = ActionLogicSupport.parseWorldCoord(ActionLogicSupport.valueText(vdx), 0f, exec);
                    float targetY = ActionLogicSupport.parseWorldCoord(ActionLogicSupport.valueText(vdy), 0f, exec);
                    float spread = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vspread), 80f, exec);

                    Tmp.v1.trns(Mathf.random(360f), Mathf.random(spread));
                    float dst = Mathf.dst(sourceX, sourceY, targetX, targetY);
                    float ang = Angles.angle(sourceX, sourceY, targetX, targetY);
                    BulletType type = ActionLogicSupport.raidBullet(bulletID);
                    if(type == null) return true;
                    float lifetimeScl = dst / Math.max(0.0001f, type.speed * type.lifetime);
                    Call.createBullet(type, teamVal, sourceX + Tmp.v1.x, sourceY + Tmp.v1.y, ang, -1, 1f, lifetimeScl);
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-raid", team, bulletType, srcX, srcY, dstX, dstY, inaccuracy);
        }
    }

    /** 通讯面板：淡入 -> 停留 -> 淡出。 */
    public static class SignalCutInStatement extends BaseActionStatement{
        public String fadeInSec = "1";
        public String holdSec = "1";
        public String fadeOutSec = "1";

        public SignalCutInStatement(){
        }

        public SignalCutInStatement(String[] tokens){
            run = tok(tokens, 1, run);
            if(tokens != null && tokens.length >= 6){
                fadeInSec = tok(tokens, 2, fadeInSec);
                holdSec = tok(tokens, 3, holdSec);
                fadeOutSec = tok(tokens, 4, fadeOutSec);
                out = tok(tokens, 5, out);
            }
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, " fadeIn ", fadeInSec, v -> fadeInSec = v, 72f);
            fieldLabeled(table, " hold ", holdSec, v -> holdSec = v, 72f);
            table.row();
            fieldLabeled(table, " fadeOut ", fadeOutSec, v -> fadeOutSec = v, 72f);
            fieldLabeled(table, " result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vIn = builder.var(fadeInSec);
            final LVar vHold = builder.var(holdSec);
            final LVar vOut = builder.var(fadeOutSec);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){

                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vIn), 1f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vHold), 1f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOut), 1f, exec));
                    float totalSec = inSec + hold + outSec;

                    if(!Vars.headless){
                        ActionLogicSupport.ensureCutsceneUI();
                        ActionContext.cutsceneUI.textTable.clearActions();
                        ActionContext.cutsceneUI.textTable.color.a = 0f;
                        ActionContext.cutsceneUI.textTable.actions(
                        Actions.sequence(
                        Actions.alpha(0f),
                        Actions.fadeIn(inSec, WHInterp.bounce5Out),
                        Actions.delay(hold),
                        Actions.fadeOut(outSec, Interp.pow2In)
                        )
                        );
                    }

                    if(totalSec <= 0f){
                        return true;
                    }
                    // 总时长：淡入 + 停留 + 淡出
                    startTimed(totalSec * Time.toSeconds);
                    return true;
                }

                @Override
                protected void end(LExecutor exec){
                    if(Vars.headless) return;
                    ActionContext.cutsceneUI.textLabel = new FLabel("");
                    ActionContext.cutsceneUI.textArea.clear();
                    ActionContext.cutsceneUI.textArea.add(ActionContext.cutsceneUI.textLabel).pad(4f, 32f, 4f, 32f);
                    ActionContext.cutsceneUI.textTable.actions(Actions.alpha(0f));
                }

                @Override
                protected void cancel(LExecutor exec){
                    end(exec);
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-signal-fade", fadeInSec, holdSec, fadeOutSec);
        }
    }

    /** 设置通讯面板文本。 */
    public static class SignalTextStatement extends BaseActionStatement{
        public String text = "";
        public String fadeInSec = "0.5";
        public String holdSec = "0.5";
        public String fadeOutSec = "0.5";
        public String overlayAlpha = "0";
        public String color = "white";
        public String fontScale = "1";

        public SignalTextStatement(){
        }

        public SignalTextStatement(String[] tokens){
            run = tok(tokens, 1, run);
            if(tokens != null && tokens.length >= 10){
                text = tok(tokens, 2, text);
                fadeInSec = tokNum(tokens, 3, fadeInSec);
                holdSec = tokNum(tokens, 4, holdSec);
                fadeOutSec = tokNum(tokens, 5, fadeOutSec);
                overlayAlpha = tokNum(tokens, 6, overlayAlpha);
                color = tok(tokens, 7, color);
                fontScale = tokNum(tokens, 8, fontScale);
                out = tok(tokens, 9, out);
            }else if(tokens != null && tokens.length >= 8){
                text = tok(tokens, 2, text);
                fadeInSec = tokNum(tokens, 3, fadeInSec);
                holdSec = tokNum(tokens, 4, holdSec);
                fadeOutSec = tokNum(tokens, 5, fadeOutSec);
                overlayAlpha = tokNum(tokens, 6, overlayAlpha);
                out = tok(tokens, 7, out);
            }else{
                text = tok(tokens, 2, text);
                out = tok(tokens, 3, out);
            }
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, " text ", text, v -> text = v, 180f);
            table.row();
            fieldLabeled(table, " fadeIn ", fadeInSec, v -> fadeInSec = v, 72f);
            fieldLabeled(table, " hold ", holdSec, v -> holdSec = v, 72f);
            fieldLabeled(table, " fadeOut ", fadeOutSec, v -> fadeOutSec = v, 72f);
            table.row();
            fieldLabeled(table, " overlay ", overlayAlpha, v -> overlayAlpha = v, 72f);
            fieldLabeled(table, " color ", color, v -> color = v, 96f);
            fieldLabeled(table, " size ", fontScale, v -> fontScale = v, 72f);
            table.row();
            fieldLabeled(table, " result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final String textToken = text;
            final LVar vTextVar = textToken != null && textToken.startsWith("$") && textToken.length() > 1
            ? builder.var(textToken.substring(1))
            : null;
            final LVar vIn = builder.var(fadeInSec);
            final LVar vHold = builder.var(holdSec);
            final LVar vOut = builder.var(fadeOutSec);
            final LVar vOverlay = builder.var(overlayAlpha);
            final LVar vColor = builder.var(color);
            final LVar vScale = builder.var(fontScale);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){

                @Override
                protected boolean begin(LExecutor exec){
                    if(Vars.headless) return true;
                    ActionLogicSupport.ensureCutsceneUI();
                    String val = ActionLogicSupport.parseText(
                    vTextVar == null ? textToken : ActionLogicSupport.valueText(vTextVar)
                    );
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vIn), 0.5f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vHold), 0.5f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOut), 0.5f, exec));
                    float totalSec = inSec + hold + outSec;
                    float overlay = Mathf.clamp(ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOverlay), 0f, exec));
                    Color fontColor = ActionLogicSupport.parseColor(ActionLogicSupport.valueText(vColor), Color.white);
                    float scale = Mathf.clamp(ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vScale), 1f, exec), 0.25f, 4f);

                    // 单条语句流程：淡入 -> 停留 -> 淡出。
                    Sounds.uiChat.play();
                    ActionContext.cutsceneUI.textLabel = new FLabel(val);
                    // Use default UI font for better CJK coverage.
                    ActionContext.cutsceneUI.textLabel.setStyle(Styles.defaultLabel);
                    ActionContext.cutsceneUI.textLabel.setColor(fontColor);
                    ActionContext.cutsceneUI.textLabel.setFontScale(scale);
                    ActionContext.cutsceneUI.textArea.clear();
                    ActionContext.cutsceneUI.textArea.add(ActionContext.cutsceneUI.textLabel).pad(4f, 32f, 4f, 32f);

                    ActionContext.cutsceneUI.textTable.clearActions();
                    ActionContext.cutsceneUI.textTable.color.a = 0f;
                    ActionContext.cutsceneUI.textTable.actions(
                    Actions.sequence(
                    Actions.alpha(0f),
                    Actions.fadeIn(inSec, WHInterp.bounce5Out),
                    Actions.delay(hold),
                    Actions.fadeOut(outSec, Interp.pow2In)
                    )
                    );

                    if(overlay > 0.001f && totalSec > 0f){
                        ActionLogicSupport.pulseOverlay(overlay, totalSec * Time.toSeconds);
                    }
                    if(totalSec <= 0f){
                        return true;
                    }
                    startTimed(totalSec * Time.toSeconds);
                    return true;
                }

                @Override
                protected void end(LExecutor exec){
                    if(Vars.headless) return;
                    ActionContext.cutsceneUI.textLabel = new FLabel("");
                    ActionContext.cutsceneUI.textArea.clear();
                    ActionContext.cutsceneUI.textArea.add(ActionContext.cutsceneUI.textLabel).pad(4f, 32f, 4f, 32f);
                    ActionContext.cutsceneUI.textTable.actions(Actions.alpha(0f));
                }

                @Override
                protected void cancel(LExecutor exec){
                    end(exec);
                }

                @Override
                protected boolean enableGlobalFade(){
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-signal-text", text, fadeInSec, holdSec, fadeOutSec, overlayAlpha, color, fontScale);
        }
    }

    /** 隐藏原版 HUD。 */
    public static class UIHideStatement extends BaseActionStatement{
        public UIHideStatement(){
        }

        public UIHideStatement(String[] tokens){
            run = tok(tokens, 1, run);
            out = tok(tokens, 2, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, "result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vo = builder.var(out);
            return exec -> {
                if(vr != null && vr.numi() == 0){
                    if(vo != null) vo.setnum(0f);
                    return;
                }
                if(!Vars.headless && Vars.ui != null && Vars.ui.hudfrag != null){
                    Vars.ui.hudfrag.shown = false;
                    Vars.control.input.config.forceHide();
                }
                if(vo != null) vo.setnum(1f);
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-ui-hide");
        }
    }

    /** 显示原版 HUD。 */
    public static class UIShowStatement extends BaseActionStatement{
        public UIShowStatement(){
        }

        public UIShowStatement(String[] tokens){
            run = tok(tokens, 1, run);
            out = tok(tokens, 2, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, "result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vo = builder.var(out);
            return exec -> {
                if(vr != null && vr.numi() == 0){
                    if(vo != null) vo.setnum(0f);
                    return;
                }
                if(!Vars.headless && Vars.ui != null && Vars.ui.hudfrag != null){
                    Vars.ui.hudfrag.shown = true;
                }
                if(vo != null) vo.setnum(1f);
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-ui-show");
        }
    }

    /** 纯等待语句。 */
    public static class WaitStatement extends BaseActionStatement{
        public String seconds = "1";

        public WaitStatement(){
        }

        public WaitStatement(String[] tokens){
            run = tok(tokens, 1, run);
            seconds = tok(tokens, 2, seconds);
            out = tok(tokens, 3, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, " sec ", seconds, v -> seconds = v, 72f);
            table.row();
            fieldLabeled(table, "result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vs = builder.var(seconds);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    float sec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vs), 0f, exec));
                    if(sec <= 0f){
                        return true;
                    }
                    startTimed(sec * Time.toSeconds);
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-wait", seconds);
        }
    }

    /** 显示预警图标与文字。 */
    public static class WarningIconStatement extends BaseActionStatement{
        public String icon = "0";
        public String team = "@sharded";
        public String text = "";

        public WarningIconStatement(){
        }

        public WarningIconStatement(String[] tokens){
            run = tok(tokens, 1, run);
            icon = tok(tokens, 2, icon);
            team = tok(tokens, 3, team);
            text = tok(tokens, 4, text);
            out = tok(tokens, 5, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 56f);
            fieldLabeled(table, " icon ", icon, v -> icon = v, 56f);
            table.row();
            fieldLabeled(table, "team ", team, v -> team = v, 78f);
            TextButton teamPick = new TextButton("pick", Styles.logict);
            teamPick.clicked(() -> showTeamPickerCommon(teamPick, selected -> team = "@" + selected.name));
            table.add(teamPick).size(64f, 32f).padLeft(4f);
            fieldLabeled(table, " text ", text, v -> text = v, 120f);
            table.row();
            fieldLabeled(table, "result ", out, v -> out = v, 84f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vi = builder.var(icon);
            final LVar vm = builder.var(text);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){

                @Override
                protected boolean begin(LExecutor exec){
                    if(Vars.headless) return true;
                    int iconId = ActionLogicSupport.parseInt(ActionLogicSupport.valueText(vi), 0);
                    String message = ActionLogicSupport.parseText(ActionLogicSupport.valueText(vm));
                    TextureRegion region = ActionLogicSupport.warningIcon(iconId);
                    if(region != null){
                        UIUtils.showToast(new TextureRegionDrawable(region), "<< " + message + " >>", Sounds.none);
                    }
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-warning-icon", icon, team, text);
        }
    }

    /** 播放预警音效（可区分友军/敌军）。 */
    public static class WarningSoundStatement extends BaseActionStatement{
        public String ally = "0";
        public String enemy = "0";
        public String team = "@sharded";

        public WarningSoundStatement(){
        }

        public WarningSoundStatement(String[] tokens){
            run = tok(tokens, 1, run);
            ally = tok(tokens, 2, ally);
            enemy = tok(tokens, 3, enemy);
            team = tok(tokens, 4, team);
            out = tok(tokens, 5, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 64f);
            fieldLabeled(table, " ally ", ally, v -> ally = v, 64f);
            fieldLabeled(table, " enemy ", enemy, v -> enemy = v, 64f);
            table.row();
            fieldLabeled(table, "team ", team, v -> team = v, 90f);
            TextButton teamPick = new TextButton("pick", Styles.logict);
            teamPick.clicked(() -> showTeamPickerCommon(teamPick, selected -> team = "@" + selected.name));
            table.add(teamPick).size(64f, 32f).padLeft(4f);
            fieldLabeled(table, " result ", out, v -> out = v, 90f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar va = builder.var(ally);
            final LVar ve = builder.var(enemy);
            final LVar vt = builder.var(team);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){

                @Override
                protected boolean begin(LExecutor exec){
                    if(Vars.headless) return true;
                    int allyID = ActionLogicSupport.parseInt(ActionLogicSupport.valueText(va), 0);
                    int enemyID = ActionLogicSupport.parseInt(ActionLogicSupport.valueText(ve), 0);
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vt), Team.derelict);
                    if(Vars.player.team() == teamVal){
                        ActionLogicSupport.warningSound(allyID).play();
                    }else{
                        ActionLogicSupport.warningSound(enemyID).play();
                    }
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-warning-sound", ally, enemy, team);
        }
    }

    public static void load(){
        registerStatement("wh-camera-control", ActionStatements.CameraControlStatement::new, ActionStatements.CameraControlStatement::new);
        registerStatement("wh-camera-reset", ActionStatements.CameraResetStatement::new, ActionStatements.CameraResetStatement::new);
        registerStatement("wh-camera-zoom", ActionStatements.CameraZoomStatement::new, ActionStatements.CameraZoomStatement::new);
        registerStatement("wh-curtain", ActionStatements.CurtainDrawStatement::new, ActionStatements.CurtainDrawStatement::new);
        /*   registerStatement("wh-curtain-fade", ActionStatements.CurtainFadeInStatement::new, ActionStatements.CurtainFadeInStatement::new);*/
        registerStatement("wh-info-text", ActionStatements.InfoTextStatement::new, ActionStatements.InfoTextStatement::new);
        registerStatement("wh-input-lock", ActionStatements.InputLockStatement::new, ActionStatements.InputLockStatement::new);
        registerStatement("wh-input-unlock", ActionStatements.InputUnlockStatement::new, ActionStatements.InputUnlockStatement::new);
        registerStatement("wh-jump-in", ActionStatements.JumpInStatement::new, ActionStatements.JumpInStatement::new);
        registerStatement("wh-mark-world", ActionStatements.MarkWorldStatement::new, ActionStatements.MarkWorldStatement::new);
        registerStatement("wh-raid", ActionStatements.RaidStatement::new, ActionStatements.RaidStatement::new);
        /*   registerStatement("wh-signal-fade", ActionStatements.SignalCutInStatement::new, ActionStatements.SignalCutInStatement::new);*/
        registerStatement("wh-signal-text", ActionStatements.SignalTextStatement::new, ActionStatements.SignalTextStatement::new);
        registerStatement("wh-ui-hide", ActionStatements.UIHideStatement::new, ActionStatements.UIHideStatement::new);
        registerStatement("wh-ui-show", ActionStatements.UIShowStatement::new, ActionStatements.UIShowStatement::new);
        registerStatement("wh-wait", ActionStatements.WaitStatement::new, ActionStatements.WaitStatement::new);
        registerStatement("wh-warning-icon", ActionStatements.WarningIconStatement::new, ActionStatements.WarningIconStatement::new);
        registerStatement("wh-warning-sound", ActionStatements.WarningSoundStatement::new, ActionStatements.WarningSoundStatement::new);
    }
}


