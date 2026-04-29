package wh.content;

import arc.audio.Sound;
import arc.files.Fi;
import arc.util.Log;
import mindustry.Vars;

public final class WHSounds{
    public static Sound
    alert2 = new Sound(),
    launch = new Sound(),
    hugeBlast = new Sound(),
    jump = new Sound(),
    blast = new Sound(),
    energyShoot = new Sound(),
    /*  LaserGatling = new Sound(),*/
    machineGunShoot = new Sound(),
    sniperShoot = new Sound(),
            laser2 = new Sound(),
            laser3 = new Sound(),
            laser4 = new Sound(),
            laser5 = new Sound(),
            largeBeam = new Sound(),
            abyssalGlareLoop = new Sound(),
            cryoflamerLoop = new Sound(),
            heavyAdjudicatorFire01 = new Sound(),
            highIntensLaserLoop = new Sound(),
            hypervelDriverFire01 = new Sound(),
            kineticBlaster01 = new Sound(),
            pulseLaserFire01 = new Sound(),
            voltaicCannonFire01 = new Sound(),
            rifleLaser = new Sound();
    /*  shootGunDouble = new Sound();*/

    private WHSounds(){
    }

    public static void load(){
        try{
            alert2 = new Sound(ogg("alert2"));
            launch = new Sound(ogg("launch"));
            hugeBlast = new Sound(ogg("hugeBlast"));
            jump = new Sound(ogg("jump"));
            blast = new Sound(ogg("DBZ1"));
            energyShoot = new Sound(ogg("JG1"));
            /*  LaserGatling = new Sound(ogg("trLaserGatling"));*/
            machineGunShoot = new Sound(ogg("mgsvGatling"));
            sniperShoot = new Sound(wav("sniper-shoot"));
            laser2 = new Sound(ogg("laser2"));
            laser3 = new Sound(ogg("laser3"));
            laser4 = new Sound(ogg("laser4"));
            laser5 = new Sound(ogg("laser5"));
            largeBeam = new Sound(ogg("largeBeam"));
            abyssalGlareLoop = new Sound(ogg("abyssal_glare_loop"));
            cryoflamerLoop = new Sound(ogg("cryoflamer_loop"));
            heavyAdjudicatorFire01 = new Sound(ogg("heavy_adjudicator_fire_01"));
            highIntensLaserLoop = new Sound(ogg("high_intens_laser_loop"));
            hypervelDriverFire01 = new Sound(ogg("hypervel_driver_fire_01"));
            kineticBlaster01 = new Sound(ogg("kinetic_blaster_01"));
            pulseLaserFire01 = new Sound(ogg("pulse_laser_fire_01"));
            voltaicCannonFire01 = new Sound(ogg("voltaic_cannon_fire_01"));
            rifleLaser = new Sound(wav("rifle-laser"));
            /*    shootGunDouble = new Sound(ogg("shoot-gun-double"));*/
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
