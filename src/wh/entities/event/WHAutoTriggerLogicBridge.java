package wh.entities.event;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.blocks.logic.*;

/**
 * MessageBlock command bridge for PortableAutoEventTrigger.
 *
 * <p>Usage from logic processor:
 * print "@wh-at install" / "@wh-at fire" / "@wh-at debug on"
 * then printflush to a MessageBlock linked by a world processor.
 */
public final class WHAutoTriggerLogicBridge{
    public static final String prefix = "@wh-at";
    public static float scanSpacing = 15f;

    private static final IntMap<String> lastTextByBuilding = new IntMap<>();
    private static final IntSet allowedMessageIds = new IntSet();
    private static final IntSet deniedMessageIds = new IntSet();
    private static boolean inited = false;
    private static float scanReload = 0f;

    private WHAutoTriggerLogicBridge(){
    }

    public static void init(){
        if(inited) return;
        inited = true;

        Events.on(EventType.WorldLoadEvent.class, e -> {
            lastTextByBuilding.clear();
            allowedMessageIds.clear();
            deniedMessageIds.clear();
            scanReload = 0f;
        });

        Events.on(EventType.ResetEvent.class, e -> {
            lastTextByBuilding.clear();
            allowedMessageIds.clear();
            deniedMessageIds.clear();
            scanReload = 0f;
        });

        Events.run(EventType.Trigger.update, WHAutoTriggerLogicBridge::update);
    }

    private static void update(){
        if(Vars.state == null || !Vars.state.isGame() || Vars.state.isMenu() || Vars.net.client()) return;

        scanReload += Time.delta;
        if(scanReload < scanSpacing) return;
        scanReload = 0f;

        refreshAllowedMessageIds();
        if(allowedMessageIds.isEmpty()) return;

        Groups.build.each(build -> {
            if(!(build instanceof MessageBlock.MessageBuild)) return;

            MessageBlock.MessageBuild messageBuild = (MessageBlock.MessageBuild)build;
            if(!allowedMessageIds.contains(messageBuild.id)) return;
            if(deniedMessageIds.contains(messageBuild.id)) return;

            String text = messageBuild.message == null ? "" : messageBuild.message.toString().trim();
            if(text.isEmpty()) return;

            String last = lastTextByBuilding.get(messageBuild.id);
            if(text.equals(last)) return;
            lastTextByBuilding.put(messageBuild.id, text);

            executeCommand(text);
        });
    }

    private static void refreshAllowedMessageIds(){
        allowedMessageIds.clear();
        deniedMessageIds.clear();

        Groups.build.each(build -> {
            if(!(build instanceof LogicBlock.LogicBuild)) return;
            boolean worldProcessor = build.block == Blocks.worldProcessor;

            LogicBlock.LogicBuild processor = (LogicBlock.LogicBuild)build;
            if(processor.executor != null && processor.executor.links != null){
                for(int i = 0; i < processor.executor.links.length; i++){
                    if(processor.executor.links[i] instanceof MessageBlock.MessageBuild){
                        if(worldProcessor){
                            allowedMessageIds.add(processor.executor.links[i].id);
                        }else{
                            deniedMessageIds.add(processor.executor.links[i].id);
                        }
                    }
                }
            }

            for(int i = 0; i < processor.links.size; i++){
                LogicBlock.LogicLink link = processor.links.get(i);
                if(link == null) continue;

                if(Vars.world == null) continue;
                if(Vars.world.build(link.x, link.y) instanceof MessageBlock.MessageBuild linkedMessage){
                    if(worldProcessor){
                        allowedMessageIds.add(linkedMessage.id);
                    }else{
                        deniedMessageIds.add(linkedMessage.id);
                    }
                }
            }
        });
    }

    /**
     * Runs one command line. Returns true when the command prefix matched.
     */
    public static boolean executeCommand(String text){
        if(text == null) return false;

        String line = text.replace('\n', ' ').trim();
        if(line.isEmpty() || !line.regionMatches(true, 0, prefix, 0, prefix.length())){
            return false;
        }

        String body = line.substring(prefix.length()).trim();
        if(body.isEmpty()){
            printHelp();
            return true;
        }

        String[] args = body.split("\\s+");
        if(args.length == 0){
            printHelp();
            return true;
        }

        String cmd = args[0].toLowerCase();
        switch(cmd){
            case "install":
            case "add":
                PortableAutoEventTrigger.installTemplates();
                Log.info("[WH][AutoTrigger][Logic] Installed templates. active=@", PortableAutoEventTrigger.active().size);
                return true;
            case "fire":
                int fired = PortableAutoEventTrigger.debugInstallAndFireNow();
                Log.info("[WH][AutoTrigger][Logic] Forced fire. fired=@", fired);
                return true;
            case "clear":
                PortableAutoEventTrigger.clearActive();
                Log.info("[WH][AutoTrigger][Logic] Cleared active triggers.");
                return true;
            case "debug":
                if(args.length < 2){
                    Log.info("[WH][AutoTrigger][Logic] debug is @.", PortableAutoEventTrigger.debugForceAnyMode ? "on" : "off");
                    return true;
                }
                boolean debug = parseBoolean(args[1], PortableAutoEventTrigger.debugForceAnyMode);
                PortableAutoEventTrigger.enableDebugAll(debug);
                Log.info("[WH][AutoTrigger][Logic] debug set to @.", debug);
                return true;
            case "timescale":
            case "scale":
                if(args.length < 2){
                    Log.info("[WH][AutoTrigger][Logic] timescale=@", PortableAutoEventTrigger.timeScale);
                    return true;
                }
                try{
                    float value = Float.parseFloat(args[1]);
                    PortableAutoEventTrigger.timeScale = value;
                    Log.info("[WH][AutoTrigger][Logic] timescale set to @.", value);
                }catch(NumberFormatException ex){
                    Log.info("[WH][AutoTrigger][Logic] invalid timescale: @", args[1]);
                }
                return true;
            case "status":
                Log.info(
                "[WH][AutoTrigger][Logic] active=@ debugAnyMode=@ debugBypassMeet=@ timescale=@",
                PortableAutoEventTrigger.active().size,
                PortableAutoEventTrigger.debugForceAnyMode,
                PortableAutoEventTrigger.debugBypassMeet,
                PortableAutoEventTrigger.timeScale
                );
                return true;
            case "help":
            default:
                printHelp();
                return true;
        }
    }

    private static boolean parseBoolean(String value, boolean fallback){
        if(value == null) return fallback;
        String v = value.trim().toLowerCase();
        if(v.equals("1") || v.equals("true") || v.equals("on") || v.equals("yes")) return true;
        if(v.equals("0") || v.equals("false") || v.equals("off") || v.equals("no")) return false;
        return fallback;
    }

    private static void printHelp(){
        Log.info("[WH][AutoTrigger][Logic] commands: @ install|fire|clear|debug on/off|timescale <f>|status (world-processor linked message only)", prefix);
    }
}
