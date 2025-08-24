package wh.entities.world.drawer;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.DrawBlock;
import static mindustry.Vars.tilesize;

public class T2PlastaniumCompresserDrawer extends DrawBlock{
    public Color allowColor;

    public T2PlastaniumCompresserDrawer(Color allowColor) {
        this.allowColor = allowColor;
    }
    @Override
    public void draw(Building build) {
        super.draw(build);
        GenericCrafter block = (GenericCrafter) build.block;
        GenericCrafter.GenericCrafterBuild b = (GenericCrafter.GenericCrafterBuild) build;
        realDraw(block, b);
    }

    public void realDraw(GenericCrafter block, GenericCrafter.GenericCrafterBuild build){

        if (build.warmup() > 0f && allowColor.a > 0.01f) {
            Draw.color(build.team.color);

            float sin = Mathf.absin(Time.time, 8f, block.size / 2f);

            Draw.z(Layer.bullet + 1f);
            Draw.color(allowColor);
            float length = tilesize * block.size / 4f + sin;

            TextureRegion region = Core.atlas.find("wh-plastanium-allow");
            for (int i = 0; i < 4; i++) {
                Tmp.v1.trns(i * 90, -length);
                float alpha = Mathf.lerp(0f, 1f, build.warmup());
                if (build.warmup() == 0f) {
                    alpha *= Mathf.lerp(1f, 0f, Time.time / 60f);
                }
                Draw.color(allowColor.r, allowColor.g, allowColor.b, alpha);
                Draw.rect(region, build.x + Tmp.v1.x, build.y + Tmp.v1.y, region.width  * Draw.scl, region.height  * Draw.scl, i*90);
            }
            Draw.reset();

        }}
}

