//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.graphics;


import arc.graphics.*;
import mindustry.content.*;
import mindustry.graphics.*;

public final class WHPal {
    public static Color ancient;
    public static Color ancientHeat;
    public static Color ancientLight;
    public static Color ancientLightMid;
    public static Color thurmixRed;
    public static Color thurmixRedLight;
    public static Color thurmixRedDark;
    public static Color WHYellow;
    public static Color WHYellow2;
    public static Color ShootOrange;
    public static Color ShootOrangeLight;
    public static Color Outline;
    public static Color OutlineS;
    public static Color Heat;
    public static Color SkyBlue;
    public static Color SkyBlueF;
    public static Color TiSteelColor;
    public static Color CeramiteColor;
    public static Color RefineCeramiteColor;
    public static Color RefinePromethiumColor;
    public static Color OrePromethiumColor;
    public static Color molybdenumAlloyColor;
    public static Color resonantCrystalColor;

    private WHPal() {
    }

    static {
        Outline = Color.valueOf("383848");
        OutlineS = Color.valueOf("42424FFF");
        Heat = Color.valueOf("FF4040");

        ancient = Items.surgeAlloy.color.cpy().lerp(Pal.accent, 0.115F);
        ancientHeat = Color.red.cpy().mul(1.075F);
        ancientLight = ancient.cpy().lerp(Color.white, 0.7F);
        ancientLightMid = ancient.cpy().lerp(Color.white, 0.4F);

        thurmixRed = Color.valueOf("ff9492");
        thurmixRedLight = Color.valueOf("ffced0");
        thurmixRedDark = thurmixRed.cpy().lerp(Color.black, 0.9F);

        WHYellow = Color.valueOf("FFFBBDE1").lerp(Pal.slagOrange, 0.1f);
        WHYellow2 = Pal.accent.cpy().lerp(Color.white, 0.6F);
        ShootOrange = Pal.bulletYellowBack.cpy().lerp(Pal.slagOrange.cpy(), 0.25f).lerp(Color.orange.cpy(), 0.25f).lerp(Pal.coalBlack.cpy(), 0.05f);
        ShootOrangeLight = ShootOrange.cpy().lerp(Color.white.cpy(), 0.7F);

        SkyBlue=Color.valueOf("579ED4FF");
        SkyBlueF=Color.valueOf("DBFFFDFF");
        TiSteelColor=Color.valueOf("80D2F3FF");
        CeramiteColor=Color.valueOf("A3BF65FF");
        RefineCeramiteColor=Color.valueOf("EAE28AFF");
        RefinePromethiumColor= Color.valueOf("999CA8FF");
        OrePromethiumColor=Color.valueOf("676870FF");
        molybdenumAlloyColor=Color.valueOf("9FA5B4FF");
        resonantCrystalColor=Color.valueOf("AA7FFAFF");
    }
}
