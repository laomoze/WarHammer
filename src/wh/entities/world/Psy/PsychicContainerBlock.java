package wh.entities.world.Psy;

import arc.math.Mathf;
import mindustry.graphics.Drawf;
import wh.graphics.WHPal;

import static mindustry.Vars.tilesize;

public class PsychicContainerBlock extends PsychicBlock {
    public float warmupSpeed = 0.08f;
    public float glowRadius = 4;

    public PsychicContainerBlock(String name) {
        super(name);
        drawArrow = false;
    }

    public class PsychicContainerBuild extends PsychicBuild {
        public float warmup;

        @Override
        public float inputPotential() {
            return psychicStored() * 0.2f;
        }

        @Override
        public void updateTile() {
            super.updateTile();
            warmup = Mathf.approachDelta(warmup, psychicStored() > PsychicNetworkNode.epsilon ? 1f : 0f, warmupSpeed);
        }

        @Override
        public void draw() {
            super.draw();
            if (warmup <= 0.001f) return;
            float frac = psychicFraction();
            Drawf.light(x, y, block.size * tilesize * glowRadius, WHPal.PsyColor, 0.12f + frac * 0.22f);
        }

        @Override
        public float warmup() {
            return warmup;
        }
    }
}

