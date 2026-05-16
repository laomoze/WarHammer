package wh.entities.world.blocks.distribution;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.Eachable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.StackConveyor;
import wh.util.WHUtils;

import static mindustry.Vars.*;

public class TubeStackConveyor extends StackConveyor{

    static final byte[][] tileMap = {
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
    public TextureRegion coverRegion;
    public TextureRegion[][] CoRegions;
    public float coverLength = 12f;
    public boolean drawCover = false;

    public TubeStackConveyor(String name){
        super(name);
    }

    @Override
    public void load() {
        super.load();
        CoRegions = new TextureRegion[5][8];

        topRegion = WHUtils.splitLayers2(name + "-top", 32, 512, 64);
        capRegion = new TextureRegion[]{topRegion[1][0], topRegion[1][1]};
        editorRegion = Core.atlas.find(name + "-full");
        for(int i = 0; i < 5; i++){
            for(int j = 0; j < 8; j++){
                CoRegions[i][j] = Core.atlas.find(name + "-" + i + "-" + j);
            }
        }
        coverRegion = Core.atlas.find(name + "-cover");
    }


    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        int[] bits = getTiling(plan, list);

        if(bits == null) return;

        TextureRegion region = CoRegions[bits[0]][0];
        Draw.rect(region, plan.drawx(), plan.drawy(), plan.rotation * 90);

        TextureRegion conveyor = topRegion[0][bits[0]];
        Draw.rect(conveyor, plan.drawx(), plan.drawy(), conveyor.width * bits[1] * conveyor.scl(), conveyor.height * bits[2] * conveyor.scl(), plan.rotation * 90);

        BuildPlan[] directionals = new BuildPlan[4];
        list.each(other -> {
            if(other.breaking || other == plan) return;

            int i = 0;
            for(Point2 point : Geometry.d4){
                int x = plan.x + point.x, y = plan.y + point.y;
                if(x >= other.x - (other.block.size - 1) / 2 && x <= other.x + (other.block.size / 2) && y >= other.y - (other.block.size - 1) / 2 && y <= other.y + (other.block.size / 2)){
                    if((other.block instanceof StackConveyor ? (plan.rotation == i || (other.rotation + 2) % 4 == i) : !noSideBlend && ((plan.rotation == i && other.block.acceptsItems) || (plan.rotation != i && other.block.outputsItems())))){
                        directionals[i] = other;
                    }
                }
                i++;
            }
        });

        int mask = 0;
        for(int i = 0; i < directionals.length; i++){
            if(directionals[i] != null){
                mask += (1 << i);
            }
        }
        mask |= (1 << plan.rotation);
        Draw.rect(topRegion[0][mask], plan.drawx(), plan.drawy(), 0);
        for(byte i : tileMap[mask]){
            if(directionals[i] == null || (directionals[i].block instanceof StackConveyor ? (directionals[i].rotation + 2) % 4 == plan.rotation : ((plan.rotation == i && !directionals[i].block.acceptsItems) || (plan.rotation != i && !directionals[i].block.outputsItems())))){
                int id = i == 0 || i == 3 ? 1 : 0;
                Draw.rect(capRegion[id], plan.drawx(), plan.drawy(), i == 0 || i == 2 ? 0 : -90);
            }
        }

    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{editorRegion};
    }


    @Override
    protected void initBuilding(){
        if(buildType == null) buildType = TubeStackConveyorBuild::new;
    }

    public class TubeStackConveyorBuild extends StackConveyorBuild{
        public int blendbits, blending;
        public boolean capped, backCapped = false;
        public int tiling = 0;
        public boolean shouldDrawCover;

        public int blendsclx = 1, blendscly = 1;

        public float heat;

        public boolean checkBuild(TubeStackConveyorBuild b){
            return b != null && b.block == this.block && b.rotation == rotation && b.shouldDrawCover;
        }

        public void updateCoverState() {
            if (!drawCover) {
                shouldDrawCover = false;
                return;
            }

            int maxCoverStep = Math.max(1, Mathf.round(coverLength));
            Point2 back = Geometry.d4(rotation + 2);
            boolean hasCover = false;

            // Continue cover chain only when there is already a support anchor maxCoverStep tiles behind.
            for (int r = 1; r <= maxCoverStep; r++) {
                Tile other = tile.nearby(back.x * r, back.y * r);
                if (other != null && other.build != this && other.build instanceof TubeStackConveyorBuild b && checkBuild(b)) {
                    if (r >= maxCoverStep) {
                        hasCover = true;
                        break;
                    }
                }
            }

            // If there is no previous support anchor, allow starting from capped/back-capped endpoints.
            if (!hasCover) {
                for (int r = 1; r <= maxCoverStep; r++) {
                    Tile backCap = tile.nearby(back.x * r, back.y * r);
                    if (backCap != null && backCap.build != this && backCap.build instanceof TubeStackConveyorBuild a
                            && a.block == this.block && a.rotation == rotation
                            && ((a.blendbits == 1 || a.blendbits == 3) || a.capped || a.backCapped)) {
                        if (r >= maxCoverStep) {
                            hasCover = true;
                            break;
                        }
                    }
                }
            }

            shouldDrawCover = hasCover;
        }

        @Override
        public void created(){
            super.created();
            updateCoverState();
        }

        @Override
        public void draw(){
            Draw.z(Layer.block + 0.001f);
            Draw.scl(1.017f, 1.017f);
            Draw.rect(topRegion[0][tiling], x, y, 0);
            byte[] placementId = tileMap[tiling];
            for(byte i : placementId){
                if(isEnd(i)){
                    int id = i == 0 || i == 3 ? 1 : 0;
                    Draw.rect(capRegion[id], x, y, i == 0 || i == 2 ? 0 : -90);
                }
            }
            Draw.scl();
            Draw.z(Layer.block + 0.02f);

            if(drawCover && shouldDrawCover && blendbits != 3 && blendbits != 1){
                for(byte i : placementId){
                    Draw.rect(coverRegion, x, y, i == 0 || i == 2 ? 0 : -90);
                }
            }
            Draw.scl();

            Draw.z(Layer.block - 0.2f);

            int frame = (int)((Time.time * speed * 6f * timeScale * efficiency) % 8f);
            Draw.rect(CoRegions[blendbits][frame], x, y, tilesize * blendsclx, tilesize * blendscly, rotation * 90);

            Tile from = world.tile(link);

            if(link == -1 || from == null || lastItem == null) return;

            int fromRot = from.build == null ? rotation : from.build.rotation;

            //offset
            Tmp.v1.set(from.worldx(), from.worldy());
            Tmp.v2.set(x, y);
            Tmp.v1.interpolate(Tmp.v2, 1f - cooldown, Interp.linear);

            //rotation
            float a = (fromRot % 4) * 90;
            float b = (rotation % 4) * 90;
            if((fromRot % 4) == 3 && (rotation % 4) == 0) a = -1 * 90;
            if((fromRot % 4) == 0 && (rotation % 4) == 3) a = 4 * 90;

            if(glowRegion.found()){
                Draw.z(Layer.blockAdditive + 0.01f);
            }
            Draw.z(Layer.block - 0.01f);
            //stack
            Draw.rect(stackRegion, Tmp.v1.x, Tmp.v1.y, Mathf.lerp(a, b, Interp.smooth.apply(1f - Mathf.clamp(cooldown * 2, 0f, 1f))));

            //item
            float size = itemSize * Mathf.lerp(Math.min((float)items.total() / itemCapacity, 1), 1f, 0.4f);
            Drawf.shadow(Tmp.v1.x, Tmp.v1.y, size * 1.2f);

            Draw.rect(lastItem.fullIcon, Tmp.v1.x, Tmp.v1.y, size, size, 0);
        }


        public boolean valid(int i){
            Building b = nearby(i);
            return b != null && (b instanceof TubeStackConveyorBuild ? (b.front() != null && b.front() == this) : b.block.acceptsItems || b.block.outputsItems());
        }

        public boolean isEnd(int i){
            Building b = nearby(i);
            return (!valid(i) && (b == null ? null : b.block) != block) || (b instanceof StackConveyorBuild && ((b.rotation + 2) % 4 == rotation || (b.front() != this && back() == b)));
        }

        @Override
        public void updateTile(){
            super.updateTile();
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            int[] bits = buildBlending(tile, rotation, null, true);
            blendbits = bits[0];
            blendsclx = bits[1];
            blendscly = bits[2];
            blending = bits[4];

            tiling = 0;

            for(int i = 0; i < 4; i++){
                Building other = nearby(i);

                if(other == null) continue;
                if(other.block instanceof StackConveyor && rotation == i || (other.rotation + 2) % 4 == i){
                    tiling |= (1 << i);
                }
            }
            tiling |= 1 << rotation;

            Building next = front(), prev = back();
            if(next instanceof TubeStackConveyorBuild a){
                capped = a.state == 2;
            }
            if(prev instanceof TubeStackConveyorBuild a){
                backCapped = a.state == 1;
            }
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            shouldDrawCover = read.bool();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(shouldDrawCover);
        }
    }
}
