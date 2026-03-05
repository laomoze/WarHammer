package wh.net;

import arc.audio.*;
import arc.scene.style.*;
import mindustry.*;
import mindustry.gen.*;
import wh.content.*;
import wh.entities.event.logic.*;
import wh.net.packet.*;
import wh.ui.*;

import static mindustry.Vars.headless;

public class WHCall{
    private WHCall(){
    }

    public static void warnHudPacket(String timerName, String text, float time, float range, float sx, float sy, float tx, float ty){
        if(Vars.net.server() || !Vars.net.active()){
            DefaultRaid.clientAlertHud(timerName, text, time, range, sx, sy, tx, ty);
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
            case 1 -> new TextureRegionDrawable(WHContent.bombard);
            case 2 -> new TextureRegionDrawable(WHContent.fleet);
            case 3 -> new TextureRegionDrawable(WHContent.airborne);
            case 4 -> new TextureRegionDrawable(WHContent.strafeRegion);
            case 5 -> new TextureRegionDrawable(WHContent.missileRegion);
            case 6 -> new TextureRegionDrawable(WHContent.bombRegion);
            default -> new TextureRegionDrawable(WHContent.objective);
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
