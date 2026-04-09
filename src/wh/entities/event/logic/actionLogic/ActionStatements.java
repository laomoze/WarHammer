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
import arc.struct.*;
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

import static mindustry.Vars.tilesize;
import static wh.entities.event.logic.WHLogicStatements.registerStatement;

/**
 * 将事件动作拆分为独立逻辑语句。
 * 所有语句复用 ActionInstruction 生命周期。
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
            if(visible.isEmpty()){
                // 某些模式下 isHidden 可能把全部单位都过滤掉，回退到非 internal 列表。
                visible = units.select(u -> u != null && !u.internal);
            }
            if(visible.isEmpty()) return;
            final arc.struct.Seq<UnitType> pickUnits = visible;

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

                UIUtils.bindContentSearch(search, list, pickUnits, candidate -> {
                    list.button(b -> {
                        b.left();
                        TextureRegion iconRegion = candidate.fullIcon == null ? candidate.uiIcon : candidate.fullIcon;
                        Drawable icon = iconRegion != null && iconRegion.found() ? new TextureRegionDrawable(iconRegion) : Icon.units;
                        b.image(icon).size(18f).padRight(6f);
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

        protected void showStatusPickerCommon(Button button, arc.func.Cons<StatusEffect> setter){
            arc.struct.Seq<StatusEffect> statuses = Vars.content.statusEffects();
            if(statuses == null || statuses.isEmpty()) return;

            arc.struct.Seq<StatusEffect> visible = statuses.select(s -> s != null && !s.isHidden());
            if(visible.isEmpty()){
                visible = statuses.select(s -> s != null);
            }
            if(visible.isEmpty()) return;
            final arc.struct.Seq<StatusEffect> pickStatuses = visible;

            showSelectTable(button, (popup, hide) -> {
                popup.clearChildren();
                popup.margin(2f);

                Table root = new Table();
                root.left().top();

                TextField search = new TextField("");
                search.setMessageText("search status...");
                root.add(search).growX().height(34f).padBottom(4f).row();

                Table list = new Table();
                list.left().top();
                list.defaults().growX().pad(1f);

                UIUtils.bindContentSearch(search, list, pickStatuses, candidate -> {
                    list.button(b -> {
                        b.left();
                        TextureRegion iconRegion = candidate.fullIcon == null ? candidate.uiIcon : candidate.fullIcon;
                        Drawable icon = iconRegion != null && iconRegion.found() ? new TextureRegionDrawable(iconRegion) : Icon.units;
                        b.image(icon).size(18f).padRight(6f);
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
                private final Vec2 tmp = new Vec2();
                private boolean forcedCutscene = false;

                @Override
                protected boolean begin(LExecutor exec){
                    float sec = Math.max(0f, ActionLogicSupport.parseFloat(vSec, 0f, exec));
                    target.set(
                    ActionLogicSupport.parseWorldCoord(vx, 0f, exec),
                    ActionLogicSupport.parseWorldCoord(vy, 0f, exec)
                    );
                    if(sec <= 0f){
                        if(!Vars.headless){
                            forcedCutscene = !Vars.control.input.logicCutscene;
                            Vars.control.input.logicCutscene = true;
                            Vars.control.input.logicCamSpeed = 10f;
                            Vars.control.input.logicCamPan = target;
                            if(forcedCutscene){
                                Vars.control.input.logicCutscene = false;
                            }
                        }
                        return true;
                    }
                    if(!Vars.headless){
                        forcedCutscene = !Vars.control.input.logicCutscene;
                        Vars.control.input.logicCutscene = true;
                        start.set(Core.camera.position);
                    }

                    startTimed(sec * Time.toSeconds);
                    return true;
                }

                @Override
                protected void update(LExecutor exec, float progress){
                    if(Vars.headless) return;

                    tmp.set(start).lerp(target, Mathf.clamp(progress));
                    Vars.control.input.logicCamSpeed = 10f;
                    Vars.control.input.logicCamPan = tmp;
                }

                @Override
                protected void end(LExecutor exec){
                    if(!Vars.headless && forcedCutscene){
                        Vars.control.input.logicCutscene = false;
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
                private boolean forcedCutscene = false;

                @Override
                protected boolean begin(LExecutor exec){
                    float sec = Math.max(0f, ActionLogicSupport.parseFloat(vSec, 0f, exec));
                    if(sec <= 0f){
                        if(!Vars.headless){
                            forcedCutscene = !Vars.control.input.logicCutscene;
                            Vars.control.input.logicCutscene = true;
                            Vars.control.input.logicCamSpeed = 1000f;
                            Tmp.v1.set(Vars.player);
                            Vars.control.input.logicCamPan = Tmp.v1;
                            if(forcedCutscene){
                                Vars.control.input.logicCutscene = false;
                            }
                        }
                        return true;
                    }
                    if(!Vars.headless){
                        forcedCutscene = !Vars.control.input.logicCutscene;
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

                @Override
                protected void end(LExecutor exec){
                    if(!Vars.headless && forcedCutscene){
                        Vars.control.input.logicCutscene = false;
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
                        Vars.control.input.logicCutsceneZoom = ActionLogicSupport.parseFloat(vz, 1f, exec);
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
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(vIn, 1f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(vHold, 1f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(vOut, 1f, exec));

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
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(vIn, 1f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(vHold, 1f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(vOut, 1f, exec));

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
                    // 单条语句流程：淡入 -> 停留 -> 淡出。
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
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(vIn, 0.25f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(vHold, 0.5f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(vOut, 0.25f, exec));
                    float totalSec = inSec + hold + outSec;
                    float overlay = Mathf.clamp(ActionLogicSupport.parseFloat(vOverlay, 0f, exec));
                    Color fontColor = ActionLogicSupport.parseColor(ActionLogicSupport.valueText(vColor), Color.white);
                    float scale = Mathf.clamp(ActionLogicSupport.parseFloat(vScale, 1f, exec), 0.25f, 4f);

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
        public String delaySec = "5";
        public String inaccuracy = "0";
        public String shield = "-1";
        public String status = "@none";
        public String statusDuration = "0";
        public String spawnerCount = "1";
        public String spawnerIntervalSec = "0";

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
            shield = tokNum(tokens, 9, shield);
            status = tok(tokens, 10, status);
            statusDuration = tokNum(tokens, 11, statusDuration);
            spawnerCount = tokNum(tokens, 12, spawnerCount);
            spawnerIntervalSec = tokNum(tokens, 13, spawnerIntervalSec);
            out = tok(tokens, 14, out);
        }

        @Override
        public void build(Table table){
            rebuild(table);
        }

        private void rebuild(Table table){
            table.clearChildren();
            fieldLabeled(table, "run ", run, v -> run = v, 64f);
            fieldLabeled(table, " unit ", unit, v -> unit = v, 100f);
            TextButton unitPick = new TextButton("pick", Styles.logict);
            unitPick.clicked(() -> showUnitPickerCommon(unitPick, selected -> {
                unit = "@" + selected.name;
                rebuild(table);
            }));
            table.add(unitPick).size(64f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, "team ", team, v -> team = v, 100f);
            TextButton teamPick = new TextButton("pick", Styles.logict);
            teamPick.clicked(() -> showTeamPickerCommon(teamPick, selected -> {
                team = "@" + selected.name;
                rebuild(table);
            }));
            table.add(teamPick).size(64f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, "x ", x, v -> x = v, 82f);
            fieldLabeled(table, " y ", y, v -> y = v, 82f);
            fieldLabeled(table, " ang ", angle, v -> angle = v, 82f);
            table.row();
            fieldLabeled(table, "delay ", delaySec, v -> delaySec = v, 90f);
            fieldLabeled(table, " spread ", inaccuracy, v -> inaccuracy = v, 90f);
            table.row();
            fieldLabeled(table, " shield ", shield, v -> shield = v, 84f);
            fieldLabeled(table, " status ", status, v -> status = v, 110f);
            TextButton statusPick = new TextButton("pick", Styles.logict);
            statusPick.clicked(() -> showStatusPickerCommon(statusPick, selected -> {
                status = "@" + selected.name;
                rebuild(table);
            }));
            table.add(statusPick).size(64f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, " statusDuration ", statusDuration, v -> statusDuration = v, 84f);
            fieldLabeled(table, " count ", spawnerCount, v -> spawnerCount = v, 72f);
            fieldLabeled(table, " IntervalSec ", spawnerIntervalSec, v -> spawnerIntervalSec = v, 72f);
            table.row();
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
            final LVar vShield = builder.var(shield);
            final LVar vStatus = builder.var(status);
            final LVar vStatusDur = builder.var(statusDuration);
            final LVar vSpawnerCount = builder.var(spawnerCount);
            final LVar vSpawnerInterval = builder.var(spawnerIntervalSec);
            final LVar vo = builder.var(out);
            final String autoTimerKeyBase = "jumpin-" + Math.abs((unit + "|" + team + "|" + x + "|" + y + "|" + angle + "|" + shield + "|" + status + "|" + statusDuration + "|" + spawnerCount + "|" + spawnerIntervalSec).hashCode());
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    UnitType unitType = ActionLogicSupport.parseUnitType(ActionLogicSupport.valueText(vu));
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vt), Team.derelict);
                    float worldX = ActionLogicSupport.parseWorldCoord(vx, 0f, exec);
                    float worldY = ActionLogicSupport.parseWorldCoord(vy, 0f, exec);
                    float angleVal = ActionLogicSupport.parseFloat(va, 0f, exec);
                    float delay = ActionLogicSupport.parseFloat(vd, 0f, exec) * Time.toSeconds;
                    float spread = ActionLogicSupport.parseFloat(vi, 0f, exec);
                    float shieldVal = ActionLogicSupport.parseFloat(vShield, -1f, exec);
                    StatusEffect statusVal = ActionLogicSupport.parseStatusEffect(ActionLogicSupport.valueText(vStatus), null);
                    float statusDurVal = Math.max(0f, ActionLogicSupport.parseFloat(vStatusDur, 0f, exec));
                    int waves = Math.max(1, ActionLogicSupport.parseInt(vSpawnerCount, 1, exec));
                    float intervalSec = Math.max(0f, ActionLogicSupport.parseFloat(vSpawnerInterval, 0f, exec));
                    float intervalTicks = intervalSec * Time.toSeconds;

                    Runnable spawnOne = () -> {
                        Spawner spawner = new Spawner();
                        Tmp.v1.trns(Mathf.random(360f), Mathf.random(spread) * tilesize);
                        spawner.init(unitType, teamVal, new Vec2(worldX + Tmp.v1.x, worldY + Tmp.v1.y), angleVal, delay, false);
                        spawner.setShieldToApply(shieldVal);
                        spawner.setStatus(statusVal, statusDurVal);
                        spawner.add();
                    };

                    for(int i = 0; i < waves; i++){
                        float spawnDelayTicks = intervalTicks * i;
                        if(spawnDelayTicks <= 0f){
                            spawnOne.run();
                        }else{
                            Time.run(spawnDelayTicks, spawnOne);
                        }
                    }

                    String timerKey = autoTimerKeyBase;
                    if(exec != null && exec.build != null){
                        timerKey += "-" + exec.build.id;
                    }
                    float totalDurationSec = DEFAULT_TIMER_DURATION_SEC + Math.max(0, waves - 1) * intervalSec;
                    JumpInTriggerObjective.obtain(timerKey).trigger(totalDurationSec * Time.toSeconds, 0f);
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-jump-in", unit, team, x, y, angle, delaySec, inaccuracy, shield, status, statusDuration, spawnerCount, spawnerIntervalSec);
        }
    }

    /** 使用 AirborneSpawner 生成多单位跳入。 */

    /** `units` 支持逗号/分号/竖线/空白分隔。 */
    /** Spawn one unit using RiftSpawner. */
    public static class RiftSpawnerStatement extends BaseActionStatement{
        private static final float DEFAULT_TIMER_DURATION_SEC = 3f;

        public String unit = "@alpha";
        public String team = "@crux";
        public String x = "0";
        public String y = "0";
        public String angle = "90";
        public String delaySec = "10";
        public String inaccuracy = "10";
        public String shield = "-1";
        public String status = "@none";
        public String statusDuration = "0";
        public String spawnerCount = "1";
        public String spawnerIntervalSec = "2";

        public RiftSpawnerStatement(){
        }

        public RiftSpawnerStatement(String[] tokens){
            run = tok(tokens, 1, run);
            unit = tok(tokens, 2, unit);
            team = tok(tokens, 3, team);
            x = tok(tokens, 4, x);
            y = tok(tokens, 5, y);
            angle = tok(tokens, 6, angle);
            delaySec = tok(tokens, 7, delaySec);
            inaccuracy = tok(tokens, 8, inaccuracy);
            shield = tokNum(tokens, 9, shield);
            status = tok(tokens, 10, status);
            statusDuration = tokNum(tokens, 11, statusDuration);
            spawnerCount = tokNum(tokens, 12, spawnerCount);
            spawnerIntervalSec = tokNum(tokens, 13, spawnerIntervalSec);
            out = tok(tokens, 14, out);
        }

        @Override
        public void build(Table table){
            rebuild(table);
        }

        private void rebuild(Table table){
            table.clearChildren();
            fieldLabeled(table, "run ", run, v -> run = v, 64f);
            fieldLabeled(table, " unit ", unit, v -> unit = v, 100f);
            TextButton unitPick = new TextButton("pick", Styles.logict);
            unitPick.clicked(() -> showUnitPickerCommon(unitPick, selected -> {
                unit = "@" + selected.name;
                rebuild(table);
            }));
            table.add(unitPick).size(64f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, "team ", team, v -> team = v, 100f);
            TextButton teamPick = new TextButton("pick", Styles.logict);
            teamPick.clicked(() -> showTeamPickerCommon(teamPick, selected -> {
                team = "@" + selected.name;
                rebuild(table);
            }));
            table.add(teamPick).size(64f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, "x ", x, v -> x = v, 82f);
            fieldLabeled(table, " y ", y, v -> y = v, 82f);
            fieldLabeled(table, " ang ", angle, v -> angle = v, 82f);
            table.row();
            fieldLabeled(table, "delay ", delaySec, v -> delaySec = v, 90f);
            fieldLabeled(table, " spread ", inaccuracy, v -> inaccuracy = v, 90f);
            table.row();
            fieldLabeled(table, " shield ", shield, v -> shield = v, 84f);
            fieldLabeled(table, " status ", status, v -> status = v, 110f);
            TextButton statusPick = new TextButton("pick", Styles.logict);
            statusPick.clicked(() -> showStatusPickerCommon(statusPick, selected -> {
                status = "@" + selected.name;
                rebuild(table);
            }));
            table.add(statusPick).size(64f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, " statusDuration ", statusDuration, v -> statusDuration = v, 84f);
            fieldLabeled(table, " count ", spawnerCount, v -> spawnerCount = v, 72f);
            fieldLabeled(table, " IntervalSec ", spawnerIntervalSec, v -> spawnerIntervalSec = v, 72f);
            table.row();
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
            final LVar vShield = builder.var(shield);
            final LVar vStatus = builder.var(status);
            final LVar vStatusDur = builder.var(statusDuration);
            final LVar vSpawnerCount = builder.var(spawnerCount);
            final LVar vSpawnerInterval = builder.var(spawnerIntervalSec);
            final LVar vo = builder.var(out);
            final String autoTimerKeyBase = "riftspawner-" + Math.abs((unit + "|" + team + "|" + x + "|" + y + "|" + angle + "|" + shield + "|" + status + "|" + statusDuration + "|" + spawnerCount + "|" + spawnerIntervalSec).hashCode());
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    UnitType unitType = ActionLogicSupport.parseUnitType(ActionLogicSupport.valueText(vu));
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vt), Team.derelict);
                    float worldX = ActionLogicSupport.parseWorldCoord(vx, 0f, exec);
                    float worldY = ActionLogicSupport.parseWorldCoord(vy, 0f, exec);
                    float angleVal = ActionLogicSupport.parseFloat(va, 0f, exec);
                    float delay = ActionLogicSupport.parseFloat(vd, 0f, exec) * Time.toSeconds;
                    float spread = ActionLogicSupport.parseFloat(vi, 0f, exec);
                    float shieldVal = ActionLogicSupport.parseFloat(vShield, -1f, exec);
                    StatusEffect statusVal = ActionLogicSupport.parseStatusEffect(ActionLogicSupport.valueText(vStatus), null);
                    float statusDurVal = Math.max(0f, ActionLogicSupport.parseFloat(vStatusDur, 0f, exec));
                    int waves = Math.max(1, ActionLogicSupport.parseInt(vSpawnerCount, 1, exec));
                    float intervalSec = Math.max(0f, ActionLogicSupport.parseFloat(vSpawnerInterval, 0f, exec));
                    float intervalTicks = intervalSec * Time.toSeconds;

                    Runnable spawnOne = () -> {
                        RiftSpawner spawner = new RiftSpawner();
                        Tmp.v1.trns(Mathf.random(360f), Mathf.random(spread) * tilesize);
                        spawner.init(unitType, teamVal, new Vec2(worldX + Tmp.v1.x, worldY + Tmp.v1.y), angleVal, delay);
                        spawner.setShieldToApply(shieldVal);
                        spawner.setStatus(statusVal, statusDurVal);
                        spawner.add();
                    };

                    for(int i = 0; i < waves; i++){
                        float spawnDelayTicks = intervalTicks * i;
                        if(spawnDelayTicks <= 0f){
                            spawnOne.run();
                        }else{
                            Time.run(spawnDelayTicks, spawnOne);
                        }
                    }

                    String timerKey = autoTimerKeyBase;
                    if(exec != null && exec.build != null){
                        timerKey += "-" + exec.build.id;
                    }
                    float totalDurationSec = DEFAULT_TIMER_DURATION_SEC + Math.max(0, waves - 1) * intervalSec;
                    JumpInTriggerObjective.obtain(timerKey).trigger(totalDurationSec * Time.toSeconds, 0f);
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-rift-spawn", unit, team, x, y, angle, delaySec, inaccuracy, shield, status, statusDuration, spawnerCount, spawnerIntervalSec);
        }
    }

    public static class AirborneJumpInStatement extends BaseActionStatement{
        private static final float DEFAULT_TIMER_DURATION_SEC = 3f;
        private static final int MAX_AIRBORNE_UNITS = 4;

        public String units = "@alpha";
        public String team = "@crux";
        public String x = "0";
        public String y = "0";
        public String delaySec = "5";
        public String inaccuracy = "10";
        public String shield = "100";
        public String status = "@none";
        public String statusDuration = "120";
        public String spawnerCount = "3";
        public String spawnerIntervalSec = "3";

        public AirborneJumpInStatement(){
        }

        public AirborneJumpInStatement(String[] tokens){
            run = tok(tokens, 1, run);
            units = tok(tokens, 2, units);
            team = tok(tokens, 3, team);
            x = tok(tokens, 4, x);
            y = tok(tokens, 5, y);
            delaySec = tok(tokens, 6, delaySec);
            inaccuracy = tok(tokens, 7, inaccuracy);
            shield = tokNum(tokens, 8, shield);
            status = tok(tokens, 9, status);
            statusDuration = tokNum(tokens, 10, statusDuration);
            spawnerCount = tokNum(tokens, 11, spawnerCount);
            spawnerIntervalSec = tokNum(tokens, 12, spawnerIntervalSec);
            out = tok(tokens, 13, out);
        }

        @Override
        public void build(Table table){
            rebuild(table);
        }

        private void rebuild(Table table){
            table.clearChildren();

            int parsedCount = parseUnitList(units).size;
            fieldLabeled(table, "run ", run, v -> run = v, 64f);
            fieldLabeled(table, " units ", units, v -> units = v, 180f);
            TextButton unitPick = new TextButton("pick", Styles.logict);
            unitPick.clicked(() -> showUnitPickerCommon(unitPick, selected -> {
                String token = "@" + selected.name;
                String current = units == null ? "" : units.trim();
                units = current.isEmpty() ? token : current + "," + token;
                rebuild(table);
            }));
            table.add(unitPick).size(64f, 32f).padLeft(2f);
            TextButton unitDetail = new TextButton("detail", Styles.logict);
            unitDetail.clicked(this::showUnitDetailDialog);
            table.add(unitDetail).size(74f, 32f).padLeft(2f);
            TextButton unitCount = new TextButton("count " + parsedCount + "/" + MAX_AIRBORNE_UNITS, Styles.logict);
            unitCount.clicked(() -> rebuild(table));
            table.add(unitCount).size(106f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, "team ", team, v -> team = v, 100f);
            TextButton teamPick = new TextButton("pick", Styles.logict);
            teamPick.clicked(() -> showTeamPickerCommon(teamPick, selected -> {
                team = "@" + selected.name;
                rebuild(table);
            }));
            table.add(teamPick).size(64f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, "x ", x, v -> x = v, 82f);
            fieldLabeled(table, " y ", y, v -> y = v, 82f);
            fieldLabeled(table, "delay ", delaySec, v -> delaySec = v, 90f);
            table.row();
            fieldLabeled(table, " spread ", inaccuracy, v -> inaccuracy = v, 90f);
            fieldLabeled(table, " shield ", shield, v -> shield = v, 70);
            fieldLabeled(table, " status ", status, v -> status = v, 100);
            TextButton statusPick = new TextButton("pick", Styles.logict);
            statusPick.clicked(() -> showStatusPickerCommon(statusPick, selected -> {
                status = "@" + selected.name;
                rebuild(table);
            }));
            table.add(statusPick).size(64f, 32f).padLeft(2f);
            table.row();
            fieldLabeled(table, " statusDuration ", statusDuration, v -> statusDuration = v, 72f);
            fieldLabeled(table, " count ", spawnerCount, v -> spawnerCount = v, 72f);
            fieldLabeled(table, " IntervalSec ", spawnerIntervalSec, v -> spawnerIntervalSec = v, 72f);
            table.row();
            fieldLabeled(table, " result ", out, v -> out = v, 90f);
        }

        private arc.struct.Seq<UnitType> parseUnitList(String raw){
            arc.struct.Seq<UnitType> parsed = new arc.struct.Seq<>();
            String text = raw == null ? "" : raw.trim();
            if(!text.isEmpty()){
                String[] tokens = text.split("[,;|\\s]+");
                for(String token : tokens){
                    if(token == null) continue;
                    String trimmed = token.trim();
                    if(trimmed.isEmpty()) continue;
                    parsed.add(ActionLogicSupport.parseUnitType(trimmed));
                    if(parsed.size >= MAX_AIRBORNE_UNITS) break;
                }
            }
            if(parsed.isEmpty()){
                parsed.add(ActionLogicSupport.parseUnitType(text));
            }
            return parsed;
        }

        private int rawUnitCount(String raw){
            String text = raw == null ? "" : raw.trim();
            if(text.isEmpty()) return 0;
            int count = 0;
            String[] tokens = text.split("[,;|\\s]+");
            for(String token : tokens){
                if(token != null && !token.trim().isEmpty()) count++;
            }
            return count;
        }

        private void showUnitDetailDialog(){
            arc.struct.Seq<UnitType> parsed = parseUnitList(units);
            int rawCount = rawUnitCount(units);

            mindustry.ui.dialogs.BaseDialog dialog = new mindustry.ui.dialogs.BaseDialog("airborne units");
            dialog.cont.pane(root -> {
                root.left().defaults().left().pad(2f);

                String rawText = units == null || units.trim().isEmpty() ? "(empty)" : units.trim();
                root.add("raw: " + rawText).wrap().width(Vars.mobile ? 420f : 340f).left().row();
                root.add("parsed: " + parsed.size + "/" + MAX_AIRBORNE_UNITS).color(Color.lightGray).left().row();
                if(rawCount > MAX_AIRBORNE_UNITS){
                    root.add("truncated to first " + MAX_AIRBORNE_UNITS + " units").color(Pal.remove).left().row();
                }

                root.row();
                for(int i = 0; i < parsed.size; i++){
                    UnitType type = parsed.get(i);
                    if(type == null){
                        root.add((i + 1) + ". null").left().row();
                        continue;
                    }
                    String display = type.localizedName == null ? type.name : type.localizedName;
                    root.add((i + 1) + ". " + display + " (" + type.name + ")").wrap().width(Vars.mobile ? 420f : 340f).left().row();
                }
            }).width(Vars.mobile ? 460f : 380f).maxHeight(Vars.mobile ? 420f : 300f);

            dialog.buttons.defaults().size(130f, 54f);
            dialog.buttons.button("@ok", dialog::hide);
            dialog.show();
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vus = builder.var(units);
            final LVar vt = builder.var(team);
            final LVar vx = builder.var(x);
            final LVar vy = builder.var(y);
            final LVar vd = builder.var(delaySec);
            final LVar vi = builder.var(inaccuracy);
            final LVar vShield = builder.var(shield);
            final LVar vStatus = builder.var(status);
            final LVar vStatusDur = builder.var(statusDuration);
            final LVar vSpawnerCount = builder.var(spawnerCount);
            final LVar vSpawnerInterval = builder.var(spawnerIntervalSec);
            final LVar vo = builder.var(out);
            final String autoTimerKeyBase = "airborne-jumpin-" + Math.abs((units + "|" + team + "|" + x + "|" + y + "|" + shield + "|" + status + "|" + statusDuration + "|" + spawnerCount + "|" + spawnerIntervalSec).hashCode());

            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vt), Team.derelict);
                    float worldX = ActionLogicSupport.parseWorldCoord(vx, 0f, exec);
                    float worldY = ActionLogicSupport.parseWorldCoord(vy, 0f, exec);
                    float delay = ActionLogicSupport.parseFloat(vd, 0f, exec) * Time.toSeconds;
                    float spread = ActionLogicSupport.parseFloat(vi, 0f, exec);
                    float shieldVal = ActionLogicSupport.parseFloat(vShield, -1f, exec);
                    StatusEffect statusVal = ActionLogicSupport.parseStatusEffect(ActionLogicSupport.valueText(vStatus), null);
                    float statusDurVal = Math.max(0f, ActionLogicSupport.parseFloat(vStatusDur, 0f, exec));
                    int waves = Math.max(1, ActionLogicSupport.parseInt(vSpawnerCount, 1, exec));
                    float intervalSec = Math.max(0f, ActionLogicSupport.parseFloat(vSpawnerInterval, 0f, exec));
                    float intervalTicks = intervalSec * Time.toSeconds;

                    Seq<UnitType> types = parseUnitList(ActionLogicSupport.valueText(vus));
                    UnitType[] typeArray = new UnitType[Math.min(types.size, MAX_AIRBORNE_UNITS)];
                    for(int i = 0; i < typeArray.length; i++){
                        typeArray[i] = types.get(i);
                    }

                    Runnable spawnOne = () -> {
                        AirborneSpawner spawner = new AirborneSpawner();
                        Tmp.v1.trns(Mathf.random(360f), Mathf.random(spread * tilesize));
                        spawner.init(teamVal, new Vec2(worldX + Tmp.v1.x, worldY + Tmp.v1.y), 0f, delay, typeArray);
                        spawner.setShieldToApply(shieldVal);
                        spawner.setStatus(statusVal, statusDurVal);
                        spawner.add();
                    };

                    for(int i = 0; i < waves; i++){
                        float spawnDelayTicks = intervalTicks * i;
                        if(spawnDelayTicks <= 0f){
                            spawnOne.run();
                        }else{
                            Time.run(spawnDelayTicks, spawnOne);
                        }
                    }

                    String timerKey = autoTimerKeyBase;
                    if(exec != null && exec.build != null){
                        timerKey += "-" + exec.build.id;
                    }
                    float totalDurationSec = DEFAULT_TIMER_DURATION_SEC + Math.max(0, waves - 1) * intervalSec;
                    JumpInTriggerObjective.obtain(timerKey).trigger(totalDurationSec * Time.toSeconds, 0f);
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-airborne-in", units, team, x, y, delaySec, inaccuracy, shield, status, statusDuration, spawnerCount, spawnerIntervalSec);
        }
    }

    /** 在世界中显示标记。 */
    public static class MarkWorldStatement extends BaseActionStatement{
        public String x = "0";
        public String y = "0";
        public String radius = "40";
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

                    float worldX = ActionLogicSupport.parseWorldCoord(vx, 0f, exec);
                    float worldY = ActionLogicSupport.parseWorldCoord(vy, 0f, exec);
                    float r = ActionLogicSupport.parseFloat(vrad, 80f, exec);
                    float t = ActionLogicSupport.parseFloat(vsec, 3f, exec);
                    int styleId = ActionLogicSupport.parseInt(vst, 0, exec);
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vteam), Team.derelict);

                    ActionContext.cutsceneUI.mark(
                    worldX, worldY, r, t * Time.toSeconds,
                    teamVal.color, ActionLogicSupport.markStyle(styleId),
                    (mx, my, mradius, progress, pulse, lifeTicks, maxLifeTicks, tint, markStyle, icon) -> {
                        Draw.z(Layer.flyingUnit + 1);
                        Draw.blend(Blending.additive);
                        Draw.color(tint);
                        Draw.alpha(0.22f + progress * 0.14f);
                        Lines.stroke(2.2f);
                        Lines.circle(mx, my, mradius * pulse);
                        float fout = 1 - Mathf.curve(progress, 0.9f, 1f);
                        Draw.alpha(0.95f * fout);
                        Lines.stroke(3.2f);
                        Drawn.circlePercent(mx, my, mradius + 1.5f, progress, 90f);

                        if(icon != null){
                            Draw.color(tint);
                            Draw.alpha(0.98f * fout);
                            float size = Math.max(16f, mradius * 0.9f * fout);
                            Draw.rect(icon, mx, my, size, size);
                        }
                        Draw.blend(Blending.normal);
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
                    int bulletID = ActionLogicSupport.parseInt(vb, 0, exec);
                    float sourceX = ActionLogicSupport.parseWorldCoord(vsx, 0f, exec);
                    float sourceY = ActionLogicSupport.parseWorldCoord(vsy, 0f, exec);
                    float targetX = ActionLogicSupport.parseWorldCoord(vdx, 0f, exec);
                    float targetY = ActionLogicSupport.parseWorldCoord(vdy, 0f, exec);
                    float spread = ActionLogicSupport.parseFloat(vspread, 80f, exec);

                    Tmp.v1.trns(Mathf.random(360f), Mathf.random(spread) * tilesize);
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

    /** 通讯面板动画：淡入 -> 停留 -> 淡出。 */
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

                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(vIn, 1f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(vHold, 1f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(vOut, 1f, exec));
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
                    // 总时长：淡入 + 停留 + 淡出。
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
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(vIn, 0.5f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(vHold, 0.5f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(vOut, 0.5f, exec));
                    float totalSec = inSec + hold + outSec;
                    float overlay = Mathf.clamp(ActionLogicSupport.parseFloat(vOverlay, 0f, exec));
                    Color fontColor = ActionLogicSupport.parseColor(ActionLogicSupport.valueText(vColor), Color.white);
                    float scale = Mathf.clamp(ActionLogicSupport.parseFloat(vScale, 1f, exec), 0.25f, 4f);

                    // 单条语句流程：淡入 -> 停留 -> 淡出。
                    Sounds.uiChat.play();
                    ActionContext.cutsceneUI.textLabel = new FLabel(val);
                    // 使用默认 UI 字体以获得更好的中日韩字符覆盖。
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
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    if(!Vars.headless && Vars.ui != null && Vars.ui.hudfrag != null){
                        Vars.ui.hudfrag.shown = false;
                        Vars.control.input.config.forceHide();
                    }
                    return true;
                }
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
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    if(!Vars.headless && Vars.ui != null && Vars.ui.hudfrag != null){
                        Vars.ui.hudfrag.shown = true;
                    }
                    return true;
                }
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
                    float sec = Math.max(0f, ActionLogicSupport.parseFloat(vs, 0f, exec));
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
            final LVar vt = builder.var(team);
            final String textToken = text;
            final LVar vTextVar = textToken != null && textToken.startsWith("$") && textToken.length() > 1
            ? builder.var(textToken.substring(1))
            : null;
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){

                @Override
                protected boolean begin(LExecutor exec){
                    if(Vars.headless) return true;
                    int iconId = ActionLogicSupport.parseInt(vi, 0, exec);
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vt), Team.derelict);
                    String message = ActionLogicSupport.parseText(
                    vTextVar == null ? textToken : ActionLogicSupport.valueText(vTextVar)
                    );
                    TextureRegion region = ActionLogicSupport.warningIcon(iconId);
                    if(region != null){
                        UIUtils.showToast(
                        new TextureRegionDrawable(region),
                        "<< " + message + " >>",
                        Sounds.none,
                        teamVal == null ? Color.white : teamVal.color
                        );
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
                    int allyID = ActionLogicSupport.parseInt(va, 0, exec);
                    int enemyID = ActionLogicSupport.parseInt(ve, 0, exec);
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
        registerStatement("wh-rift-spawn", ActionStatements.RiftSpawnerStatement::new, ActionStatements.RiftSpawnerStatement::new);
        registerStatement("wh-airborne-in", ActionStatements.AirborneJumpInStatement::new, ActionStatements.AirborneJumpInStatement::new);
        registerStatement("wh-mark-world", ActionStatements.MarkWorldStatement::new, ActionStatements.MarkWorldStatement::new);
        registerStatement("wh-raid", ActionStatements.RaidStatement::new, ActionStatements.RaidStatement::new);
        /*   registerStatement("wh-signal-fade", ActionStatements.SignalCutInStatement::new, ActionStatements.SignalCutInStatement::new);*/
        registerStatement("wh-signal-text", ActionStatements.SignalTextStatement::new, ActionStatements.SignalTextStatement::new);
        registerStatement("wh-ui-hide", ActionStatements.UIHideStatement::new, ActionStatements.UIHideStatement::new);
        registerStatement("wh-ui-show", ActionStatements.UIShowStatement::new, ActionStatements.UIShowStatement::new);
        registerStatement("wh-wait", ActionStatements.WaitStatement::new, ActionStatements.WaitStatement::new);
        registerStatement("wh-warning-icon", ActionStatements.WarningIconStatement::new, ActionStatements.WarningIconStatement::new);
        /*   registerStatement("wh-warning-sound", ActionStatements.WarningSoundStatement::new, ActionStatements.WarningSoundStatement::new);*/
    }
}




