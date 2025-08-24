//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.graphics;


import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.graphics.Pal;

public final class WHPal {
    public static Color ancient;
    public static Color ancientHeat;
    public static Color ancientLight;
    public static Color ancientLightMid;
    public static Color thurmixRed;
    public static Color thurmixRedLight;
    public static Color thurmixRedDark;
    public static Color rim;
    public static Color rim3;
    public static Color WHYellow;
    public static Color WHYellow2;
    public static Color OR;
    public static Color ORL;
    public static Color Outline;
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
        ancient = Items.surgeAlloy.color.cpy().lerp(Pal.accent, 0.115F);
        ancientHeat = Color.red.cpy().mul(1.075F);
        ancientLight = ancient.cpy().lerp(Color.white, 0.7F);
        ancientLightMid = ancient.cpy().lerp(Color.white, 0.4F);
        thurmixRed = Color.valueOf("#ff9492");
        thurmixRedLight = Color.valueOf("#ffced0");
        thurmixRedDark = thurmixRed.cpy().lerp(Color.black, 0.9F);
        rim = Color.valueOf("fffbbde1");
        rim3 = Color.valueOf("dbeffe");

        WHYellow = Color.valueOf("FFFBBDE1");
        WHYellow2 = Color.valueOf("FFFFFF");
        OR = Color.valueOf("FFC397FF");
        ORL =Color.valueOf("FFA05C");
        Heat=Color.valueOf("FF4040");
        Outline=Color.valueOf("383848");

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
