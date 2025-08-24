package wh.entities.world.drawer;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import wh.content.WHFx;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class LargekilnDrawer extends DrawBlock {
    public Color lightingColor;
    public float updateEffectChance = 0.01f;

    public LargekilnDrawer(Color lightingColor) {
        this.lightingColor = lightingColor;
    }

    @Override
    public void draw(Building build) {
        super.draw(build);
        if (build.warmup() > 0f && lightingColor.a > 0.001f) {
            Block block = build.block;
            Draw.color(lightingColor);
            if (build.wasVisible && !state.isPaused() && Mathf.chance(updateEffectChance)) {
                Tmp.v1.rnd(1.8f * tilesize * block.size * 0.9f).add(build.tile);
                WHFx.chainLightningFade.at(build.x, build.y, 12f, lightingColor, Tmp.v1.cpy());
            }
            if (build.wasVisible && !state.isPaused() && Mathf.chance(updateEffectChance)) {
                Tmp.v1.rnd(1.5f * tilesize * block.size * 0.9f).add(build.tile);
                WHFx.chainLightningFadeReversed.at(build.x, build.y, 12f, lightingColor, Tmp.v1.cpy());
            }
        }
    }
}
