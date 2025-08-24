package wh.entities.world.drawer;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.gen.Building;
import mindustry.world.draw.DrawBlock;
import wh.util.WHUtils;

public class SealedPromethiumMillDrawer extends DrawBlock {
    public float warmup;

    @Override
    public void draw(Building build) {
        if (build.warmup() > 0f) {
            super.draw(build);
            if (Mathf.equal(warmup, 1, 0.0015F)) {
                warmup = 1f;
            } else {
                warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
            }

            Draw.reset();
            float sin = Mathf.absin(Time.time, 8f, build.block.size / 2f);
            Draw.color(build.team.color);
            Lines.stroke(warmup * (1 + sin *1.5f));
            Lines.spikes(build.x, build.y, sin * 3 + build.block.size/2f, warmup * (4 + sin), 4, 45 + WHUtils.rotator_90());

            Draw.reset();
        }
    }

}
