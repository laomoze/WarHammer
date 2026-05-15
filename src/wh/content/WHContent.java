//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.content;

import arc.Core;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.ContentType;
import mindustry.graphics.Layer;
import wh.core.WarHammerMod;

import static wh.core.WarHammerMod.name;

public class WHContent extends Content{
    private static final TextureRegion emptyRegion = new TextureRegion(new Texture("sprites/error.png"));

    public static TextureRegion arrowRegion,
    pointerRegion,
    strafeRegion, missileRegion, bombRegion, annihilateArrow,
    bombard, fleet, objective, airborne,

    dropPod, dropPodTeam, dropPodSide1, dropPodSideTeam1, dropPodSide2, dropPodSideTeam2;

    public static final float HEXAGONAL_SHIELD = Layer.shields + 12f;
    public static final float VOID_SHIELD = Layer.shields + 9f;
    public static final float POWER_AREA = Layer.power + 3f;
    public static final float POWER_DYNAMIC = Layer.power + 4f;

    @Override
    public ContentType getContentType(){
        return ContentType.error;
    }

    public static TextureRegion safeRegion(TextureRegion region) {
        return region == null ? emptyRegion : region;
    }

    public static boolean hasRegion(TextureRegion region) {
        return region != null && region.width > 0f && region.height > 0f;
    }

    public static void loadPriority(){
        if (Vars.headless) return;
        if (Core.atlas == null) {
            return;
        }
        new WHContent().load();
    }

    public void load(){
        if (Core.atlas == null) return;

        arrowRegion = Core.atlas.find(WarHammerMod.name("jump-gate-arrow"));
        pointerRegion = Core.atlas.find(WarHammerMod.name("jump-gate-pointer"));
        strafeRegion = Core.atlas.find(name("strafe-mode"));
        missileRegion = Core.atlas.find(name("missile-mode"));
        bombRegion = Core.atlas.find(name("bomb-mode"));
        annihilateArrow = Core.atlas.find(name("Annihilate-arrow"));
        bombard = Core.atlas.find(name("bombard"));
        fleet = Core.atlas.find(name("fleet"));
        objective = Core.atlas.find(name("objective"));
        airborne = Core.atlas.find(name("airborne"));

        String dp = "space-marine-drop-pod";
        dropPod = Core.atlas.find(name(dp));
        dropPodTeam = Core.atlas.find(name(dp + "-team"));
        dropPodSide1 = Core.atlas.find(name(dp + "-side1"));
        dropPodSideTeam1 = Core.atlas.find(name(dp + "-side1-team"));
        dropPodSide2 = Core.atlas.find(name(dp + "-side2"));
        dropPodSideTeam2 = Core.atlas.find(name(dp + "-side2-team"));
    }
}
