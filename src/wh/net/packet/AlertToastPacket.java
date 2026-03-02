package wh.net.packet;

import arc.util.io.*;
import mindustry.net.*;
import wh.net.*;
import wh.ui.*;

public class AlertToastPacket extends Packet{
    public int iconID;
    public int soundID;
    public String text;

    private byte[] data = NODATA;

    @Override
    public void write(Writes write){
        write.str(text);
        write.i(iconID);
        write.i(soundID);
    }

    @Override
    public void read(Reads read, int length){
        data = read.b(length);
    }

    @Override
    public void handled(){
        BAIS.setBytes(data);
        text = READ.str();
        iconID = READ.i();
        soundID = READ.i();
    }

    @Override
    public void handleClient(){
        UIUtils.showToast(WHCall.getDrawable(iconID), text, WHCall.getSound(soundID));
    }

    @Override
    public void handleServer(NetConnection con){
        if(con.player == null || con.kicked){
            return;
        }
        WHCall.alertToastTable(iconID, soundID, text);
    }
}
