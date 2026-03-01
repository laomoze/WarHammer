package wh.entities.event.logic.actionLogic;

import arc.audio.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.world.blocks.logic.*;
import wh.content.*;
import wh.entities.event.ui.*;

import java.util.*;

/**
 * action 逻辑语句的公共工具方法。
 * 负责参数解析、资源映射、以及 UI 初始化辅助。
 */
public final class ActionLogicSupport{
    private static int overlayPulseToken = 0;

    private ActionLogicSupport(){
    }

    /** 将 Logic 变量安全转换为字符串值。 */
    public static String valueText(LVar value){
        if(value == null) return "";

        Object raw = value.obj();
        if(raw instanceof String text){
            return text.trim();
        }
        if(raw instanceof Team teamValue){
            return teamValue.name == null ? "" : teamValue.name.trim();
        }
        if(raw instanceof UnlockableContent content){
            return content.name == null ? "" : content.name.trim();
        }
        if(raw != null){
            return String.valueOf(raw).trim();
        }

        String name = value.name == null ? "" : value.name.trim();
        if(name.isEmpty()) return "";
        if(name.startsWith("___")){
            float num = value.numf();
            int asInt = Mathf.round(num);
            if(Math.abs(num - asInt) < 0.0001f){
                return String.valueOf(asInt);
            }
            return Strings.autoFixed(num, 3);
        }
        return name;
    }

    /** 支持脚本中的 [n] 转换为换行。 */
    public static String parseText(String raw){
        if(raw == null) return "";
        return raw.replace("[n]", "\n");
    }

    /** 解析 float，支持 @N 读取逻辑处理器下方 memory 单元。 */
    public static float parseFloat(String raw, float fallback, LExecutor exec){
        if(raw == null) return fallback;
        String token = raw.trim();
        if(token.isEmpty()) return fallback;

        if(token.startsWith("@") && exec != null && exec.build != null){
            try{
                int memoryIndex = Integer.parseInt(token.substring(1));
                Building memoryBuild = Vars.world.build(exec.build.tileX(), exec.build.tileY() - 1);
                if(memoryBuild instanceof MemoryBlock.MemoryBuild memory){
                    if(memoryIndex >= 0 && memoryIndex < memory.memory.length){
                        return (float)memory.memory[memoryIndex];
                    }
                }
            }catch(Exception ignored){
            }
        }

        try{
            return Float.parseFloat(token);
        }catch(Exception ignored){
            return fallback;
        }
    }

    /** 安全解析 int。 */
    public static int parseInt(String raw, int fallback){
        if(raw == null) return fallback;
        String token = raw.trim();
        if(token.isEmpty()) return fallback;
        try{
            return Integer.parseInt(token);
        }catch(Exception ignored){
            return fallback;
        }
    }

    /** 解析队伍字符串（名称或数字 ID）。 */
    public static Team parseTeam(String raw, Team fallback){
        if(raw == null) return fallback;
        String token = raw.trim();
        if(token.isEmpty()) return fallback;
        if(token.startsWith("@")) token = token.substring(1);
        token = token.toLowerCase(Locale.ROOT);

        switch(token){
            case "derelict":
                return Team.derelict;
            case "sharded":
                return Team.sharded;
            case "crux":
                return Team.crux;
            case "malis":
                return Team.malis;
            case "green":
                return Team.green;
            case "blue":
                return Team.blue;
            case "neoplastic":
                return Team.neoplastic;
            default:
                try{
                    return Team.get(Integer.parseInt(token));
                }catch(Exception ignored){
                    return fallback;
                }
        }
    }

    /** 解析单位类型（名称优先，数字 ID 兜底）。 */
    public static UnitType parseUnitType(String raw){
        if(raw == null) return UnitTypes.alpha;
        String token = raw.trim();
        if(token.isEmpty()) return UnitTypes.alpha;
        if(token.startsWith("@")) token = token.substring(1);

        UnitType byName = Vars.content.unit(token);
        if(byName != null) return byName;

        try{
            UnitType byId = Vars.content.unit(Integer.parseInt(token));
            return byId == null ? UnitTypes.alpha : byId;
        }catch(Exception ignored){
            return UnitTypes.alpha;
        }
    }

    /** 标记样式映射。 */
    public static MarkStyle markStyle(int style){
        return switch(style){
            case 1 -> MarkStyle.defaultNoLines;
            case 2 -> MarkStyle.defaultFixed;
            case 3 -> MarkStyle.signalShake;
            case 4 -> MarkStyle.iconRaid;
            default -> MarkStyle.defaultStyle;
        };
    }

    /** 预警图标映射。 */
    public static TextureRegion warningIcon(int icon){
        return switch(icon){
            case 0 -> WHContent.fleet;
            case 1 -> WHContent.objective;
            case 2 -> WHContent.fleet;
            default -> WHContent.objective;
        };
    }

    /** 预警音效映射。 */
    public static Sound warningSound(int soundID){
        return switch(soundID){
            case 0 -> WHSounds.alert2;
            case 1 -> WHSounds.alert2;
            case 2 -> Sounds.uiUnlock;
            case 3 -> Sounds.wind3;
            default -> Sounds.none;
        };
    }

    /** 远程打击子弹类型映射。 */
    public static BulletType raidBullet(int bulletTypeID){
        return switch(bulletTypeID){
            case 1 -> WHBullets.airRaiderBomb;
            case 2 -> WHBullets.airRaiderMissile;
            case 3 -> WHBullets.CycloneMissleLauncherMissile1;
            case 4 -> WHBullets.CycloneMissleLauncherMissile2;
            case 5 -> WHBullets.CycloneMissleLauncherMissile3;
            case 6 -> WHBullets.SSWordTiSteel;
            case 7 -> WHBullets.SSWordPyratite;
            case 8 -> WHBullets.SSWordSurgeAlloy;
            default -> WHBullets.airRaiderMissile;
        };
    }

    /** 保证过场 UI 已完成初始化。 */
    public static void ensureCutsceneUI(){
        if(!Vars.headless){
            ActionContext.cutsceneUI.ensureSetup();
        }
    }

    /**
     * 短暂压暗屏幕再恢复到原目标亮度。
     * 适合文本/提示出现时做过场感，不会长期锁死黑屏。
     */
    public static void pulseOverlay(float alpha, float holdTicks){
        if(Vars.headless) return;
        ensureCutsceneUI();

        float prev = ActionContext.cutsceneUI.targetOverlayAlpha;
        float peak = Math.max(prev, Mathf.clamp(alpha));
        ActionContext.cutsceneUI.targetOverlayAlpha = peak;

        int token = ++overlayPulseToken;
        Time.run(Math.max(1f, holdTicks), () -> {
            if(Vars.headless) return;
            if(token != overlayPulseToken) return;
            // 仅在没有更高优先级遮罩目标接管时才恢复。
            if(ActionContext.cutsceneUI.targetOverlayAlpha <= peak + 0.001f){
                ActionContext.cutsceneUI.targetOverlayAlpha = prev;
            }
        });
    }

    /**
     * UI 提示类语句的一次性门控：
     * 同一地图内，同一处理器同一行（可带额外内容哈希）只执行一次。
     * 返回 true 表示本次允许执行；false 表示已执行过，应跳过。
     */
    public static boolean allowUiShowOnce(LExecutor exec, String actionTag, String extra){
        // 兼容保留：一次性门控已移除，统一放行。
        return true;
    }

    /** 兼容保留：一次性门控已移除，无需重置。 */
    public static void resetUiShowOnceCache(){
    }

    /** token 安全化：为空时回落默认值，并将空白替换为下划线。 */
    public static String safeToken(String value, String fallback){
        if(value == null) return fallback;
        String out = value.trim();
        if(out.isEmpty()) return fallback;
        return out.replaceAll("\\s+", "_");
    }
}
