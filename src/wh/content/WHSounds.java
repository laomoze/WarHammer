package wh.content;

import arc.audio.*;
import arc.files.*;
import arc.util.*;
import wh.*;

public final class WHSounds{
    public static Sound
    alert2 = new Sound(),
    launch = new Sound(),
    hugeBlast = new Sound(),
    hugeShoot = new Sound(),
    shock = new Sound(),
    jump = new Sound(),
    lightningShoot = new Sound(),
    blast = new Sound(),
    missileShoot = new Sound(),
    missileShoot2 = new Sound(),
    rocket = new Sound(),
    energyShoot = new Sound(),
    LaserGatling = new Sound(),
    machineGunShoot = new Sound();

    private WHSounds(){
    }

    public static void load(){
        try{
            alert2 = new Sound(ogg("alert2"));
            launch = new Sound(ogg("launch"));
            hugeBlast = new Sound(ogg("hugeBlast"));
            hugeShoot = new Sound(ogg("hugeShoot"));
            shock = new Sound(ogg("shock"));
            jump = new Sound(ogg("jump"));
            lightningShoot = new Sound(ogg("CT1"));
            blast = new Sound(ogg("DBZ1"));
            missileShoot = new Sound(ogg("DD1"));
            missileShoot2 = new Sound(ogg("lbp3SearchlightEdited"));
            rocket = new Sound(ogg("FJ"));
            energyShoot = new Sound(ogg("JG1"));
            LaserGatling = new Sound(ogg("trLaserGatling"));
            machineGunShoot = new Sound(ogg("mgsvGatling"));
        }catch(Exception e){
            Log.err("Failed to load Sound.", e);
        }
    }

    static Fi ogg(String name){
        return WHVars.internalTree.child("sounds/" + name + ".ogg");
    }

    static Fi mp3(String name){
        return WHVars.internalTree.child("sounds/" + name + ".mp3");
    }
}
