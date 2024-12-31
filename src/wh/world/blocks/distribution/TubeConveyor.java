package wh.world.blocks.distribution;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.distribution.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static wh.util.WHUtils.*;

/**
 * Compared to CoveredConverter, its upper layer texture has been changed to one that can have light and shadow effects.
 *
 * @author Eipusino
 */
public class TubeConveyor extends BeltConveyor {
    public static final float itemSpace = 0.4f;
    public static final int[][] tiles = new int[][]{
            {},
            {0, 2}, {1, 3}, {0, 1},
            {0, 2}, {0, 2}, {1, 2},
            {0, 1, 2}, {1, 3}, {0, 3},
            {1, 3}, {0, 1, 3}, {2, 3},
            {0, 2, 3}, {1, 2, 3}, {0, 1, 2, 3}
    };

    public TextureRegion[][] topRegion;
    public TextureRegion[] capRegion;
    public TextureRegion editorRegion;

    public TubeConveyor(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        topRegion = splitLayers(name + "-sheet", 32, 2);
        capRegion = new TextureRegion[]{topRegion[1][0], topRegion[1][1]};
        editorRegion = atlas.find(name + "-editor");
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{editorRegion};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        int[] bits = getTiling(plan, list);

        if (bits == null) return;

        TextureRegion conveyor = conveyorAtlas[0][bits[0]];
        Draw.rect(conveyor, plan.drawx(), plan.drawy(), conveyor.width * bits[1] * conveyor.scl(), conveyor.height * bits[2] * conveyor.scl(), plan.rotation * 90);

        BuildPlan[] directionals = new BuildPlan[4];
        list.each(other -> {
            if (other.breaking || other == plan) return;

            int i = 0;
            for (Point2 point : Geometry.d4) {
                int x = plan.x + point.x, y = plan.y + point.y;
                if (x >= other.x - (other.block.size - 1) / 2 && x <= other.x + (other.block.size / 2) && y >= other.y - (other.block.size - 1) / 2 && y <= other.y + (other.block.size / 2)) {
                    if ((other.block instanceof Conveyor ? (plan.rotation == i || (other.rotation + 2) % 4 == i) : !noSideBlend && ((plan.rotation == i && other.block.acceptsItems) || (plan.rotation != i && other.block.outputsItems())))) {
                        directionals[i] = other;
                    }
                }
                i++;
            }
        });

        int mask = 0;
        for (int i = 0; i < directionals.length; i++) {
            if (directionals[i] != null) {
                mask += (1 << i);
            }
        }
        mask |= (1 << plan.rotation);
        Draw.rect(topRegion[0][mask], plan.drawx(), plan.drawy(), 0);
        for (int i : tiles[mask]) {
            if (directionals[i] == null || (directionals[i].block instanceof Conveyor ? (directionals[i].rotation + 2) % 4 == plan.rotation : ((plan.rotation == i && !directionals[i].block.acceptsItems) || (plan.rotation != i && !directionals[i].block.outputsItems())))) {
                int id = i == 0 || i == 3 ? 1 : 0;
                Draw.rect(capRegion[id], plan.drawx(), plan.drawy(), i == 0 || i == 2 ? 0 : -90);
            }
        }
    }

    public class TubeConveyorBuild extends BeltConveyorBuild {
        public int tiling = 0;

        public boolean valid(int i) {
            Building b = nearby(i);
            return b != null && (b instanceof TubeConveyorBuild ? (b.front() != null && b.front() == this) : b.block.acceptsItems || b.block.outputsItems());
        }

        public boolean isEnd(int i) {
            Building b = nearby(i);
            return (!valid(i) && (b == null ? null : b.block) != this.block) || (b instanceof ConveyorBuild && ((b.rotation + 2) % 4 == rotation || (b.front() != this && back() == b)));
        }

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
                    Draw.rect(sliced(topRegion[0][1], i != 0 ? SliceMode.bottom : SliceMode.top), x + Geometry.d4x(dir) * tilesize * 0.75f, y + Geometry.d4y(dir) * tilesize * 0.75f, rot);
                }
            }

            Draw.z(Layer.block - 0.25f);

            Draw.rect(conveyorAtlas[frame][blendbits], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);

            Draw.z(Layer.block - 0.2f);
            float layer = Layer.block - 0.2f, wwidth = world.unitWidth(), wheight = world.unitHeight(), scaling = 0.01f;

            for (int i = 0; i < len; i++) {
                Item item = ids[i];
                Tmp.v1.trns(rotation * 90, tilesize, 0);
                Tmp.v2.trns(rotation * 90, -tilesize / 2f, xs[i] * tilesize / 2f);

                float ix = (x + Tmp.v1.x * ys[i] + Tmp.v2.x), iy = (y + Tmp.v1.y * ys[i] + Tmp.v2.y);

                //keep draw position deterministic.
                Draw.z(layer + (ix / wwidth + iy / wheight) * scaling);
                Draw.rect(item.fullIcon, ix, iy, itemSize, itemSize);
            }

            Draw.z(Layer.block - 0.15f);
            Draw.rect(topRegion[0][tiling], x, y, 0);
            int[] placementID = tiles[tiling];
            for (int i : placementID) {
                if (isEnd(i)) {
                    int id = i == 0 || i == 3 ? 1 : 0;
                    Draw.rect(capRegion[id], x, y, i == 0 || i == 2 ? 0 : -90);
                }
            }
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            if (isEnd(reverse(rotation)) && items.total() >= 2) return 0;
            if (isEnd(reverse(rotation)) && isEnd(rotation) && items.total() >= 1) return 0;
            return Math.min((int) (minitem / itemSpace), amount);
        }

        @Override
        public void drawCracks() {
            Draw.z(Layer.block);
            super.drawCracks();
        }

        @Override
        public void unitOn(Unit unit) {
            //There is a cover, can't slide on this thing.
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();

            tiling = 0;
            for (int i = 0; i < 4; i++) {
                Building otherBlock = nearby(i);
                if (otherBlock == null) continue;
                if ((otherBlock.block instanceof Conveyor ? (rotation == i || (otherBlock.rotation + 2) % 4 == i) : !noSideBlend && ((rotation == i && otherBlock.block.acceptsItems) || (rotation != i && otherBlock.block.outputsItems())))) {
                    tiling |= (1 << i);
                }
            }
            tiling |= 1 << rotation;
        }
    }
}
