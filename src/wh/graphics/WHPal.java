package wh.graphics;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.graphics.*;

public final class WHPal {
    public static Color
            ancient = Items.surgeAlloy.color.cpy().lerp(Pal.accent, 0.115f),
            ancientHeat = Color.red.cpy().mul(1.075f),
            ancientLight = ancient.cpy().lerp(Color.white, 0.7f),
            ancientLightMid = ancient.cpy().lerp(Color.white, 0.4f),
            thurmixRed = Color.valueOf("#ff9492"),
            thurmixRedLight = Color.valueOf("#ffced0"),
            thurmixRedDark = thurmixRed.cpy().lerp(Color.black, 0.9f),

            rim = Color.valueOf("fffbbde1"),
            rim3 = Color.valueOf("dbeffe");

    private WHPal() {}
}
