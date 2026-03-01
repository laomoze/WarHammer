package wh.entities.event.logic.actionLogic;

import arc.*;
import arc.flabel.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.actions.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import wh.entities.*;
import wh.entities.event.logic.*;
import wh.entities.event.ui.*;
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
        public String fadeInSec = "0";
        public String fadeOutSec = "0";
        public String x = "0";
        public String y = "0";

        public CameraControlStatement(){
        }

        public CameraControlStatement(String[] tokens){
            run = tok(tokens, 1, run);
            if(tokens != null && tokens.length >= 8){
                seconds = tok(tokens, 2, seconds);
                fadeInSec = tok(tokens, 3, fadeInSec);
                fadeOutSec = tok(tokens, 4, fadeOutSec);
                x = tok(tokens, 5, x);
                y = tok(tokens, 6, y);
                out = tok(tokens, 7, out);
            }else{
                // 兼容旧格式：run sec x y out
                seconds = tok(tokens, 2, seconds);
                x = tok(tokens, 3, x);
                y = tok(tokens, 4, y);
                out = tok(tokens, 5, out);
            }
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 72f);
            fieldLabeled(table, " sec ", seconds, v -> seconds = v, 72f);
            table.row();
            fieldLabeled(table, "fadeIn ", fadeInSec, v -> fadeInSec = v, 72f);
            fieldLabeled(table, " fadeOut ", fadeOutSec, v -> fadeOutSec = v, 72f);
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
            final LVar vIn = builder.var(fadeInSec);
            final LVar vOut = builder.var(fadeOutSec);
            final LVar vx = builder.var(x);
            final LVar vy = builder.var(y);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                private final Vec2 start = new Vec2();
                private final Vec2 target = new Vec2();
                private boolean forcedCutscene = false;
                private float fadeInRatio = 0f;
                private float fadeOutRatio = 0f;

                @Override
                protected boolean begin(LExecutor exec){
                    float sec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vSec), 0f, exec));
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vIn), 0f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOut), 0f, exec));
                    target.set(
                    ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vx), 0f, exec),
                    ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vy), 0f, exec)
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

                    if(inSec + outSec > sec && sec > 0f){
                        float scale = sec / (inSec + outSec);
                        inSec *= scale;
                        outSec *= scale;
                    }
                    fadeInRatio = sec <= 0f ? 0f : Mathf.clamp(inSec / sec);
                    fadeOutRatio = sec <= 0f ? 0f : Mathf.clamp(outSec / sec);

                    startTimed(sec * Time.toSeconds);
                    return true;
                }

                @Override
                protected void update(LExecutor exec, float progress){
                    if(Vars.headless) return;

                    float eased = progress;
                    float tailStart = 1f - fadeOutRatio;
                    if(fadeInRatio > 0f && progress < fadeInRatio){
                        float local = progress / fadeInRatio;
                        eased = Interp.pow2In.apply(local) * fadeInRatio;
                    }else if(fadeOutRatio > 0f && progress > tailStart){
                        float local = (progress - tailStart) / fadeOutRatio;
                        eased = tailStart + Interp.pow2Out.apply(local) * fadeOutRatio;
                    }

                    Tmp.v1.set(start).lerp(target, Mathf.clamp(eased));
                    Vars.control.input.logicCamSpeed = 10f;
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
            writeCommon(builder, "wh-camera-control", seconds, fadeInSec, fadeOutSec, x, y);
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
                    float sec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vSec), 0f, exec));
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

    /** 幕布动画（0->1->0）。 */
    public static class CurtainDrawStatement extends BaseActionStatement{
        public CurtainDrawStatement(){
        }

        public CurtainDrawStatement(String[] tokens){
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
                    ActionLogicSupport.ensureCutsceneUI();
                    // 单条语句内完成 0->1->0 的一体化曲线。
                    startTimed(180f);
                    return true;
                }

                @Override
                protected void update(LExecutor exec, float progress){
                    if(Vars.headless) return;
                    float p = progress <= 0.5f ? progress * 2f : (1f - progress) * 2f;
                    ActionContext.cutsceneUI.curtainProgress = Interp.linear.apply(Mathf.clamp(p));
                }

                @Override
                protected void end(LExecutor exec){
                    if(!Vars.headless){
                        ActionContext.cutsceneUI.curtainProgress = 0f;
                    }
                }

                @Override
                protected void cancel(LExecutor exec){
                    if(!Vars.headless){
                        ActionContext.cutsceneUI.curtainProgress = 0f;
                    }
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-curtain");
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
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-curtain-fade", fadeInSec, holdSec, fadeOutSec);
        }
    }

    /** 信息面板：淡入 -> 停留 -> 淡出。 */
    public static class InfoFadeInStatement extends BaseActionStatement{
        public String fadeInSec = "1";
        public String holdSec = "1";
        public String fadeOutSec = "1";

        public InfoFadeInStatement(){
        }

        public InfoFadeInStatement(String[] tokens){
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
                    if(!ActionLogicSupport.allowUiShowOnce(exec, "wh-info-fade", "")){
                        return true;
                    }

                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vIn), 1f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vHold), 1f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOut), 1f, exec));
                    float totalSec = inSec + hold + outSec;

                    if(!Vars.headless){
                        ActionLogicSupport.ensureCutsceneUI();
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
            writeCommon(builder, "wh-info-fade", fadeInSec, holdSec, fadeOutSec);
        }
    }

    /** 设置信息面板文本。 */
    public static class InfoTextStatement extends BaseActionStatement{
        public String text = "";
        public String fadeInSec = "0.25";
        public String holdSec = "0.5";
        public String fadeOutSec = "0.25";
        public String overlayAlpha = "0";

        public InfoTextStatement(){
        }

        public InfoTextStatement(String[] tokens){
            run = tok(tokens, 1, run);
            if(tokens != null && tokens.length >= 8){
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
            fieldLabeled(table, " result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vt = builder.var(text);
            final LVar vIn = builder.var(fadeInSec);
            final LVar vHold = builder.var(holdSec);
            final LVar vOut = builder.var(fadeOutSec);
            final LVar vOverlay = builder.var(overlayAlpha);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean enableGlobalFade(){
                    return false;
                }

                @Override
                protected boolean begin(LExecutor exec){
                    if(Vars.headless) return true;
                    ActionLogicSupport.ensureCutsceneUI();
                    String val = ActionLogicSupport.parseText(ActionLogicSupport.valueText(vt));
                    if(!ActionLogicSupport.allowUiShowOnce(exec, "wh-info-text", val)){
                        return true;
                    }
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vIn), 0.25f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vHold), 0.5f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOut), 0.25f, exec));
                    float totalSec = inSec + hold + outSec;
                    float overlay = Mathf.clamp(ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOverlay), 0f, exec));

                    // 单条语句流程：淡入 -> 停留 -> 淡出。
                    ActionContext.cutsceneUI.infoLabel = new FLabel(val);
                    ActionContext.cutsceneUI.infoLabel.setStyle(Styles.outlineLabel);
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
            writeCommon(builder, "wh-info-text", text, fadeInSec, holdSec, fadeOutSec, overlayAlpha);
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
        public String unit = "@alpha";
        public String team = "@crux";
        public String x = "0";
        public String y = "0";
        public String angle = "90";
        public String delaySec = "0";
        public String inaccuracy = "0";

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
            out = tok(tokens, 9, out);
        }

        @Override
        public void build(Table table){
            fieldLabeled(table, "run ", run, v -> run = v, 64f);
            fieldLabeled(table, " unit ", unit, v -> unit = v, 110f);
            fieldLabeled(table, " team ", team, v -> team = v, 90f);
            table.row();
            fieldLabeled(table, "x ", x, v -> x = v, 82f);
            fieldLabeled(table, " y ", y, v -> y = v, 82f);
            fieldLabeled(table, " ang ", angle, v -> angle = v, 82f);
            table.row();
            fieldLabeled(table, "delay ", delaySec, v -> delaySec = v, 90f);
            fieldLabeled(table, " spread ", inaccuracy, v -> inaccuracy = v, 90f);
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
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean begin(LExecutor exec){
                    UnitType unitType = ActionLogicSupport.parseUnitType(ActionLogicSupport.valueText(vu));
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vt), Team.derelict);
                    float worldX = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vx), 0f, exec);
                    float worldY = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vy), 0f, exec);
                    float angleVal = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(va), 0f, exec);
                    float delay = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vd), 0f, exec) * Time.toSeconds;
                    float spread = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vi), 0f, exec);

                    Spawner spawner = new Spawner();
                    Tmp.v1.trns(Mathf.random(360f), Mathf.random(spread));
                    spawner.init(unitType, teamVal, new Vec2(worldX + Tmp.v1.x, worldY + Tmp.v1.y), angleVal, delay, false);
                    spawner.add();
                    return true;
                }
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-jump-in", unit, team, x, y, angle, delaySec, inaccuracy);
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

                    float worldX = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vx), 0f, exec);
                    float worldY = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vy), 0f, exec);
                    float r = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vrad), 80f, exec);
                    float t = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vsec), 3f, exec);
                    int styleId = ActionLogicSupport.parseInt(ActionLogicSupport.valueText(vst), 0);
                    Team teamVal = ActionLogicSupport.parseTeam(ActionLogicSupport.valueText(vteam), Team.derelict);

                    ActionContext.cutsceneUI.mark(worldX, worldY, r, t * Time.toSeconds, teamVal.color, ActionLogicSupport.markStyle(styleId));
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
            fieldLabeled(table, " team ", team, v -> team = v, 86f);
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
                    float sourceX = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vsx), 0f, exec);
                    float sourceY = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vsy), 0f, exec);
                    float targetX = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vdx), 0f, exec);
                    float targetY = ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vdy), 0f, exec);
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
                    if(!ActionLogicSupport.allowUiShowOnce(exec, "wh-signal-fade", "")){
                        return true;
                    }

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

        public SignalTextStatement(){
        }

        public SignalTextStatement(String[] tokens){
            run = tok(tokens, 1, run);
            if(tokens != null && tokens.length >= 8){
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
            fieldLabeled(table, " result ", out, v -> out = v, 120f);
        }

        @Override
        public LExecutor.LInstruction build(LAssembler builder){
            final LVar vr = builder.var(run);
            final LVar vt = builder.var(text);
            final LVar vIn = builder.var(fadeInSec);
            final LVar vHold = builder.var(holdSec);
            final LVar vOut = builder.var(fadeOutSec);
            final LVar vOverlay = builder.var(overlayAlpha);
            final LVar vo = builder.var(out);
            return new ActionInstruction(vr, vo){
                @Override
                protected boolean enableGlobalFade(){
                    return false;
                }

                @Override
                protected boolean begin(LExecutor exec){
                    if(Vars.headless) return true;
                    ActionLogicSupport.ensureCutsceneUI();
                    String val = ActionLogicSupport.parseText(ActionLogicSupport.valueText(vt));
                    if(!ActionLogicSupport.allowUiShowOnce(exec, "wh-signal-text", val)){
                        return true;
                    }
                    float inSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vIn), 0.5f, exec));
                    float hold = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vHold), 0.5f, exec));
                    float outSec = Math.max(0f, ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOut), 0.5f, exec));
                    float totalSec = inSec + hold + outSec;
                    float overlay = Mathf.clamp(ActionLogicSupport.parseFloat(ActionLogicSupport.valueText(vOverlay), 0f, exec));

                    // 单条语句流程：淡入 -> 停留 -> 淡出。
                    Sounds.uiChat.play();
                    ActionContext.cutsceneUI.textLabel = new FLabel(val);
                    ActionContext.cutsceneUI.textLabel.setStyle(Styles.outlineLabel);
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
            };
        }

        @Override
        public void write(StringBuilder builder){
            writeCommon(builder, "wh-signal-text", text, fadeInSec, holdSec, fadeOutSec, overlayAlpha);
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
            fieldLabeled(table, "run ", run, v -> run = v, 64f);
            fieldLabeled(table, " icon ", icon, v -> icon = v, 64f);
            fieldLabeled(table, " team ", team, v -> team = v, 90f);
            table.row();
            fieldLabeled(table, "text ", text, v -> text = v, 160f);
            fieldLabeled(table, " result ", out, v -> out = v, 90f);
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
                    if(!ActionLogicSupport.allowUiShowOnce(exec, "wh-warning-icon", iconId + "|" + message)){
                        return true;
                    }
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
                    if(!ActionLogicSupport.allowUiShowOnce(exec, "wh-warning-sound", allyID + "|" + enemyID + "|" + teamVal.id)){
                        return true;
                    }
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
        registerStatement("wh-curtain-fade", ActionStatements.CurtainFadeInStatement::new, ActionStatements.CurtainFadeInStatement::new);
        registerStatement("wh-info-fade", ActionStatements.InfoFadeInStatement::new, ActionStatements.InfoFadeInStatement::new);
        registerStatement("wh-info-text", ActionStatements.InfoTextStatement::new, ActionStatements.InfoTextStatement::new);
        registerStatement("wh-input-lock", ActionStatements.InputLockStatement::new, ActionStatements.InputLockStatement::new);
        registerStatement("wh-input-unlock", ActionStatements.InputUnlockStatement::new, ActionStatements.InputUnlockStatement::new);
        registerStatement("wh-jump-in", ActionStatements.JumpInStatement::new, ActionStatements.JumpInStatement::new);
        registerStatement("wh-mark-world", ActionStatements.MarkWorldStatement::new, ActionStatements.MarkWorldStatement::new);
        registerStatement("wh-raid", ActionStatements.RaidStatement::new, ActionStatements.RaidStatement::new);
        registerStatement("wh-signal-fade", ActionStatements.SignalCutInStatement::new, ActionStatements.SignalCutInStatement::new);
        registerStatement("wh-signal-text", ActionStatements.SignalTextStatement::new, ActionStatements.SignalTextStatement::new);
        registerStatement("wh-ui-hide", ActionStatements.UIHideStatement::new, ActionStatements.UIHideStatement::new);
        registerStatement("wh-ui-show", ActionStatements.UIShowStatement::new, ActionStatements.UIShowStatement::new);
        registerStatement("wh-wait", ActionStatements.WaitStatement::new, ActionStatements.WaitStatement::new);
        registerStatement("wh-warning-icon", ActionStatements.WarningIconStatement::new, ActionStatements.WarningIconStatement::new);
        registerStatement("wh-warning-sound", ActionStatements.WarningSoundStatement::new, ActionStatements.WarningSoundStatement::new);
    }
}

