package wh.content;

import arc.audio.*;
import arc.files.*;
import arc.util.*;
import mindustry.*;

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
    /* missileShoot = new Sound(),
     missileShoot2 = new Sound(),
     rocket = new Sound(),*/
    energyShoot = new Sound(),
    LaserGatling = new Sound(),
    machineGunShoot = new Sound(),
    sniperShoot = new Sound(),
    /*  pistonLaser = new Sound(),*/
    rifleLaser = new Sound(),
    shootGunDouble = new Sound();

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
          /*  missileShoot = new Sound(ogg("DD1"));
            missileShoot2 = new Sound(ogg("lbp3SearchlightEdited"));
            rocket = new Sound(ogg("FJ"));*/
            energyShoot = new Sound(ogg("JG1"));
            LaserGatling = new Sound(ogg("trLaserGatling"));
            machineGunShoot = new Sound(ogg("mgsvGatling"));
            sniperShoot = new Sound(wav("sniper-shoot"));
            /*   pistonLaser = new Sound(wav("piston-laser"));*/
            rifleLaser = new Sound(wav("rifle-laser"));
            shootGunDouble = new Sound(ogg("shoot-gun-double"));
        }catch(Exception e){
            Log.err("Failed to load Sound.", e);
        }
    }

    static Fi ogg(String name){
        return Vars.tree.get("sounds/" + name + ".ogg");
    }

    static Fi wav(String name){
        return Vars.tree.get("sounds/" + name + ".wav");
    }

    static Fi mp3(String name){
        return Vars.tree.get("sounds/" + name + ".mp3");
    }
}
