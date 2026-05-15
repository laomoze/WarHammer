package wh.net;

import arc.audio.Sound;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import mindustry.Vars;
import mindustry.gen.Sounds;
import wh.content.WHContent;
import wh.content.WHSounds;
import wh.entities.event.logic.DefaultRaids;
import wh.net.packet.AlertToastPacket;
import wh.net.packet.WarnHUDPacket;
import wh.ui.UIUtils;

import static mindustry.Vars.headless;

public class WHCall{
    private WHCall(){
    }

    public static void warnHudPacket(String timerName, String text, float time, float range, float sx, float sy, float tx, float ty){
        if(Vars.net.server() || !Vars.net.active()){
            DefaultRaids.clientAlertHud(timerName, text, time, range, sx, sy, tx, ty);
        }

        if(Vars.net.server()){
            WarnHUDPacket packet = new WarnHUDPacket();
            packet.name = timerName == null ? "" : timerName;
            packet.text = text == null ? "" : text;
            packet.time = time;
            packet.range = range;
            packet.sourceX = sx;
            packet.sourceY = sy;
            packet.targetX = tx;
            packet.targetY = ty;
            Vars.net.send(packet, false);
        }
    }

    public static void alertToastTable(int iconID, int soundID, String text){
        String toastText = text == null ? "" : text;

        if(Vars.net.server() || !Vars.net.active()){
            if(headless) return;
            UIUtils.showToast(getDrawable(iconID), toastText, getSound(soundID));
        }

        if(Vars.net.server()){
            AlertToastPacket packet = new AlertToastPacket();
            packet.text = toastText;
            packet.iconID = iconID;
            packet.soundID = soundID;
            Vars.net.send(packet, false);
        }
    }

    public static Drawable getDrawable(int id){
        return switch(id){
            case 0 -> new TextureRegionDrawable(WHContent.safeRegion(WHContent.objective));
            case 1 -> new TextureRegionDrawable(WHContent.safeRegion(WHContent.fleet));
            case 2 -> new TextureRegionDrawable(WHContent.safeRegion(WHContent.airborne));
            case 3 -> new TextureRegionDrawable(WHContent.safeRegion(WHContent.strafeRegion));
            case 4 -> new TextureRegionDrawable(WHContent.safeRegion(WHContent.missileRegion));
            case 5 -> new TextureRegionDrawable(WHContent.safeRegion(WHContent.bombRegion));
            default -> new TextureRegionDrawable(WHContent.safeRegion(WHContent.bombard));
        };
    }

    public static Sound getSound(int id){
        return switch(id){
            case -1 -> Sounds.none;
            case 1 -> WHSounds.alert2;
            default -> Sounds.uiUnlock;
        };
    }
}
