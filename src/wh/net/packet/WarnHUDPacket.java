package wh.net.packet;

import arc.util.io.*;
import mindustry.net.*;
import wh.entities.event.logic.*;
import wh.net.*;

public class WarnHUDPacket extends Packet{
    public String name;
    public String text;
    public float time;
    public float range;
    public float sourceX;
    public float sourceY;
    public float targetX;
    public float targetY;

    private byte[] data = NODATA;

    @Override
    public void write(Writes write){
        write.str(name);
        write.str(text);
        write.f(time);
        write.f(range);
        write.f(sourceX);
        write.f(sourceY);
        write.f(targetX);
        write.f(targetY);
    }

    @Override
    public void read(Reads read, int length){
        data = read.b(length);
    }

    @Override
    public void handled(){
        BAIS.setBytes(data);
        name = READ.str();
        time = READ.f();
        range = READ.f();
        sourceX = READ.f();
        sourceY = READ.f();
        targetX = READ.f();
        targetY = READ.f();
    }

    @Override
    public void handleClient(){
        DefaultRaid.clientAlertHud(name, text, time, range, sourceX, sourceY, targetX, targetY);
    }

    @Override
    public void handleServer(NetConnection con){
        if(con.player == null || con.kicked){
            return;
        }
        WHCall.warnHudPacket(name, text, time, range, sourceX, sourceY, targetX, targetY);
    }
}
