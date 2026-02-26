package wh.entities.world.drawer.factory;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import wh.graphics.*;

import static mindustry.Vars.*;

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
            Vec2 tile = new Vec2().set(build.tile);
            if(!Vars.headless && !state.isPaused() && Mathf.chanceDelta(updateEffectChance)){
                Drawn.randFadeLightningEffectScl(tile.x, tile.y, 1.8f * tilesize * block.size * 0.9f,
                0.55F, 1.1F, 12f, lightingColor, false);
                Drawn.randFadeLightningEffectScl(tile.x, tile.y, 1.8f * tilesize * block.size * 0.9f,
                0.55F, 1.1F, 12f, lightingColor, true);
            }
        }
    }
}
