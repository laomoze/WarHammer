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
    public static Color pop;
    public static Color pop2;
    public static Color OR;
    public static Color ORL;
    public static Color Heat;

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
        pop = Color.valueOf("FFFBBDE1");
        pop2 = Color.valueOf("FFFFFF");
        OR = Color.valueOf("FFC397FF");
        ORL =Color.valueOf("FFA05C");
        Heat=Color.valueOf("FF4040");
    }
}
