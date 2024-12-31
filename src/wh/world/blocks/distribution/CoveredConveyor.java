package wh.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static wh.util.WHUtils.*;

/**
 * Compared to the original conveyor belt, there is an additional sprites on top of the item layer.
 *
 * @author Eipusino
 */
public class CoveredConveyor extends BeltConveyor {
    public TextureRegion inputRegion, outputRegion;

    public CoveredConveyor(String name) {
        super(name);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void load() {
        super.load();
        inputRegion = atlas.find(name + "-cover-in");
        outputRegion = atlas.find(name + "-cover-out");
    }

    public class CoveredConveyorBuild extends BeltConveyorBuild {
        public boolean backCap, leftCap, rightCap, frontCap;

        @Override
        public void draw() {
            int frame = enabled && clogHeat <= 0.5f ? (int) (((Time.time * speed * 8f * timeScale * efficiency)) % 4) : 0;

            //draw extra conveyors facing this one for non-square tiling purposes
            Draw.z(Layer.blockUnder);
            for (int i = 0; i < 4; i++) {
                if ((blending & (1 << i)) != 0) {
                    int dir = rotation - i;
                    float rot = i == 0 ? rotation * 90 : (dir) * 90;

                    Draw.rect(sliced(conveyorAtlas[frame][0], i != 0 ? SliceMode.bottom : SliceMode.top), x + Geometry.d4x(dir) * tilesize * 0.75f, y + Geometry.d4y(dir) * tilesize * 0.75f, rot);
                }
            }

            Draw.z(Layer.block - 0.2f);

            Draw.rect(conveyorAtlas[frame][blendbits], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);

            Draw.z(Layer.block - 0.1f);
            float layer = Layer.block - 0.1f, wwidth = world.unitWidth(), wheight = world.unitHeight(), scaling = 0.01f;

            for (int i = 0; i < len; i++) {
                Item item = ids[i];
                Tmp.v1.trns(rotation * 90, tilesize, 0);
                Tmp.v2.trns(rotation * 90, -tilesize / 2f, xs[i] * tilesize / 2f);

                float
                        ix = (x + Tmp.v1.x * ys[i] + Tmp.v2.x),
                        iy = (y + Tmp.v1.y * ys[i] + Tmp.v2.y);

                //keep draw position deterministic.
                Draw.z(layer + (ix / wwidth + iy / wheight) * scaling);
                Draw.rect(item.fullIcon, ix, iy, itemSize, itemSize);
            }

            Draw.z(Layer.block - 0.08f);
            Draw.rect(edgeAtlas[(tile.x + tile.y) % 2][blendbits], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);

            if (frontCap) Draw.rect(outputRegion, x, y, rotdeg());
            if (!backCap) Draw.rect(inputRegion, x, y, rotdeg());
            if (leftCap) Draw.rect(inputRegion, x, y, rotdeg() - 90f);
            if (rightCap) Draw.rect(inputRegion, x, y, rotdeg() + 90f);
        }

        @Override
        public void unitOn(Unit unit) {
            //There is a cover, can't slide on this thing.
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();

            frontCap = nextc == null || block != nextc.block;

            Building backB = back();
            backCap = blendbits == 1 || blendbits == 4 || relativeDirection(backB, this) == 0 && backB.block == block;

            Building leftB = left();
            leftCap = blendbits != 0 && relativeDirection(leftB, this) == 0 && leftB.block != block;

            Building rightB = right();
            rightCap = blendbits != 0 && relativeDirection(rightB, this) == 0 && rightB.block != block;
        }
    }
}
