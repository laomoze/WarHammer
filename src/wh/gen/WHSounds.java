package wh.gen;

import arc.audio.*;
import arc.files.*;
import arc.util.*;
import wh.*;

public final class WHSounds {
    public static Sound
            alert2 = new Sound(),
            launch = new Sound(),
            hugeBlast = new Sound(),
            hugeShoot = new Sound(),
            blaster = new Sound(),
            shock = new Sound(),
            jump = new Sound(),
            ct1 = new Sound(),
            dbz1 = new Sound(),
            dd1 = new Sound(),
            fj = new Sound(),
            jg1 = new Sound();

    private WHSounds() {}

    public static void load() {
        try {
            alert2 = new Sound(ogg("alert2"));
            launch = new Sound(ogg("launch"));
            hugeBlast = new Sound(ogg("hugeBlast"));
            hugeShoot = new Sound(ogg("hugeShoot"));
            blaster = new Sound(ogg("blaster"));
            shock = new Sound(ogg("shock"));
            jump = new Sound(ogg("jump"));
            ct1 = new Sound(ogg("CT1"));
            dbz1 = new Sound(ogg("DBZ1"));
            dd1  = new Sound(ogg("DD1"));
            fj  = new Sound(ogg("FJ"));
            jg1  = new Sound(ogg("JG1"));
        } catch (Exception e) {
            Log.err("Failed to load Sound.", e);
        }
    }

    static Fi ogg(String name) {
        return WHVars.internalTree.child("sounds/" + name + ".ogg");
    }

    static Fi mp3(String name) {
        return WHVars.internalTree.child("sounds/" + name + ".mp3");
    }
}
