package wh.entities.event.logic.actionLogic;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.content.UnitTypes;
import mindustry.core.World;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import wh.content.WHBullets;
import wh.content.WHContent;
import wh.content.WHSounds;
import wh.entities.event.ui.ActionContext;
import wh.entities.event.ui.MarkStyle;

import java.util.Locale;

/**
 * Action 逻辑公共工具类。
 * 负责参数解析、资源映射与过场 UI 辅助。
 */
public final class ActionLogicSupport{
    private static int overlayPulseToken = 0;

    private ActionLogicSupport(){
    }

    /** 将 Logic 变量安全转换为字符串。 */
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

        // Numeric logic variables (e.g. x/y) must resolve to their current value,
        // not the variable name, otherwise downstream parsers fall back unexpectedly.
        if(!value.isobj){
            return numericText(value.numf());
        }

        String name = value.name == null ? "" : value.name.trim();
        if(name.isEmpty()) return "";
        return name;
    }

    private static String numericText(float num){
        int asInt = Mathf.round(num);
        if(Math.abs(num - asInt) < 0.0001f){
            return String.valueOf(asInt);
        }
        return Strings.autoFixed(num, 3);
    }

    /** 支持脚本中的 `[n]` 转换为换行符。 */
    public static String parseText(String raw){
        if(raw == null) return "";
        return raw.replace("[n]", "\n");
    }

    /**
     * 解析浮点数；仅支持字面量。
     */
    public static float parseFloat(String raw, float fallback, LExecutor exec) {
        Float parsed = tryParseFloatToken(raw);
        return parsed == null ? fallback : parsed;
    }

    /** Parse float directly from a logic variable. */
    public static float parseFloat(LVar value, float fallback, LExecutor exec){
        if(value == null) return fallback;

        if(!value.isobj){
            return value.numf();
        }

        Object obj = value.obj();
        if(obj instanceof Number number){
            return number.floatValue();
        }
        if(obj instanceof Boolean bool){
            return bool ? 1f : 0f;
        }
        if(obj instanceof Team team){
            return team.id;
        }
        if(obj instanceof String text) {
            Float parsed = tryParseFloatToken(text);
            return parsed == null ? fallback : parsed;
        }
        if(obj != null){
            Float parsedObj = tryParseFloatToken(String.valueOf(obj).trim());
            return parsedObj == null ? fallback : parsedObj;
        }

        return fallback;
    }

    /** Parse tile coordinate and convert to world coordinate via World.unconv(). */
    public static float parseWorldCoord(String raw, float fallbackTile, LExecutor exec){
        return World.unconv(parseFloat(raw, fallbackTile, exec));
    }

    /** Parse tile coordinate from a logic variable and convert to world coordinate. */
    public static float parseWorldCoord(LVar value, float fallbackTile, LExecutor exec){
        return World.unconv(parseFloat(value, fallbackTile, exec));
    }

    /** Parse color token. Supports #RRGGBB / RRGGBB and common color names. */
    public static Color parseColor(String raw, Color fallback){
        Color fb = fallback == null ? Color.white : fallback;
        if(raw == null) return fb;

        String token = raw.trim();
        if(token.isEmpty()) return fb;

        if(token.startsWith("[#") && token.endsWith("]") && token.length() > 3){
            token = token.substring(2, token.length() - 1);
        }
        if(token.startsWith("#")){
            token = token.substring(1);
        }

        Color palColor = parsePalColor(token);
        if(palColor != null){
            return palColor;
        }

        String lower = token.toLowerCase(Locale.ROOT);
        switch(lower){
            case "white":
                return Color.white;
            case "black":
                return Color.black;
            case "red":
                return Color.red;
            case "green":
                return Color.green;
            case "blue":
                return Color.blue;
            case "yellow":
                return Color.yellow;
            case "orange":
                return Color.orange;
            case "pink":
                return Color.pink;
            case "gray":
            case "grey":
                return Color.gray;
            case "lightgray":
            case "lightgrey":
                return Color.lightGray;
            case "darkgray":
            case "darkgrey":
                return Color.darkGray;
            case "cyan":
                return Color.cyan;
            case "magenta":
                return Color.magenta;
            case "coral":
                return Color.coral;
            case "salmon":
                return Color.salmon;
            case "royal":
                return Color.royal;
            case "scarlet":
                return Color.scarlet;
            case "gold":
                return Color.gold;
            case "lime":
                return Color.lime;
            case "purple":
                return Color.purple;
            case "violet":
                return Color.violet;
            case "maroon":
                return Color.maroon;
            case "teal":
                return Color.teal;
            case "navy":
                return Color.navy;
            case "clear":
                return Color.clear;
            default:
                try{
                    return Color.valueOf(token);
                }catch(Exception ignored){
                    return fb;
                }
        }
    }

    /** Parse color by Mindustry Pal static field name. */
    private static Color parsePalColor(String token){
        if(token == null) return null;
        String key = token.trim();
        if(key.isEmpty()) return null;

        if(key.startsWith("pal.") || key.startsWith("Pal.")){
            key = key.substring(4);
        }else if(key.startsWith("pal_") || key.startsWith("Pal_")){
            key = key.substring(4);
        }
        key = key.replace('-', '_').replace(' ', '_');

        // Fast path: exact field name.
        try{
            java.lang.reflect.Field field = Pal.class.getField(key);
            Object value = field.get(null);
            if(value instanceof Color color){
                return new Color(color);
            }
        }catch(Exception ignored){
        }

        // Fallback: case-insensitive match.
        for(java.lang.reflect.Field field : Pal.class.getFields()){
            if(!Color.class.isAssignableFrom(field.getType())) continue;
            if(!field.getName().equalsIgnoreCase(key)) continue;
            try{
                Object value = field.get(null);
                if(value instanceof Color color){
                    return new Color(color);
                }
            }catch(Exception ignored){
            }
            break;
        }
        return null;
    }

    /** 安全解析 `int`，失败时返回默认值。 */
    public static int parseInt(String raw, int fallback){
        return parseInt(raw, fallback, null);
    }

    /** Parse int directly from a logic variable to avoid text round-trip. */
    public static int parseInt(LVar value, int fallback, LExecutor exec){
        float resolved = parseFloat(value, Float.NaN, exec);
        if(Float.isNaN(resolved) || Float.isInfinite(resolved)){
            return fallback;
        }
        return Mathf.round(resolved);
    }

    /** 安全解析 `int`。 */
    public static int parseInt(String raw, int fallback, LExecutor exec) {
        Float parsed = tryParseFloatToken(raw);
        if (parsed == null || Float.isNaN(parsed) || Float.isInfinite(parsed)) return fallback;
        return Mathf.round(parsed);
    }

    private static Float tryParseFloatToken(String token) {
        if (token == null) return null;
        String t = token.trim();
        if (t.isEmpty()) return null;
        if (t.equalsIgnoreCase("true")) return 1f;
        if (t.equalsIgnoreCase("false")) return 0f;
        try {
            return Float.parseFloat(t);
        }catch(Exception ignored) {
            return null;
        }
    }

    /** 解析队伍标识（名称或数字 ID）。 */
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

    /** 解析状态效果（名称或数字 ID）。 */
    public static StatusEffect parseStatusEffect(String raw, StatusEffect fallback){
        StatusEffect fb = fallback == null ? StatusEffects.none : fallback;
        if(raw == null) return fb;

        String token = raw.trim();
        if(token.isEmpty()) return fb;
        if(token.startsWith("@")) token = token.substring(1);

        String lower = token.toLowerCase(Locale.ROOT);
        if(lower.equals("none")) return StatusEffects.none;

        StatusEffect byName = Vars.content.statusEffect(token);
        if(byName != null) return byName;

        try{
            int id = Integer.parseInt(token);
            arc.struct.Seq<StatusEffect> all = Vars.content.statusEffects();
            if(all == null || id < 0 || id >= all.size) return fb;
            StatusEffect byId = all.get(id);
            return byId == null ? fb : byId;
        }catch(Exception ignored){
            return fb;
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
            case 1 -> WHContent.bombard;
            default -> WHContent.objective;
        };
    }

    /** 预警音效映射。 */
    public static Sound warningSound(int soundID){
        return switch(soundID){
            case 0 -> WHSounds.alert2;
            case 1 -> Sounds.uiUnlock;
            case 2 -> Sounds.wind3;
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
            default -> WHBullets.airRaiderMissile;
        };
    }

    /** 确保过场 UI 已完成初始化。 */
    public static void ensureCutsceneUI(){
        if(!Vars.headless){
            ActionContext.cutsceneUI.ensureSetup();
        }
    }

    /**
     * 短暂压暗屏幕后恢复到原目标亮度。
     * 适合文本/提示出现时的过场，不会长期锁死黑屏。
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
            // 仅在没有更高优先级遮罩接管时才恢复。
            if(ActionContext.cutsceneUI.targetOverlayAlpha <= peak + 0.001f){
                ActionContext.cutsceneUI.targetOverlayAlpha = prev;
            }
        });
    }

    /** token 安全化：为空时回落默认值，并将空白替换为下划线。 */
    public static String safeToken(String value, String fallback){
        if(value == null) return fallback;
        String out = value.trim();
        if(out.isEmpty()) return fallback;
        return out.replaceAll("\\s+", "_");
    }
}
