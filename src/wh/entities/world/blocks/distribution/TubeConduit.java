package wh.entities.world.blocks.distribution;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.Eachable;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.TargetPriority;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.blocks.liquid.Conduit;
import wh.util.WHUtils;

import static mindustry.Vars.renderer;
import static mindustry.type.Liquid.animationFrames;

public class TubeConduit extends Conduit{
    static final float rotatePad = 6, hpad = rotatePad / 2f / 4f;
    static final float[][] rotateOffsets = {{hpad, hpad}, {-hpad, hpad}, {-hpad, -hpad}, {hpad, -hpad}};

    public TubeConduit(String name){
        super(name);
        rotate = true;
        solid = false;
        floating = true;
        underBullets = true;
        conveyorPlacement = true;
        noUpdateDisabled = true;
        canOverdrive = false;
        noSideBlend = true;
        priority = TargetPriority.transport;
        botColor = Color.valueOf("393A40ff");
    }

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
    public TextureRegion[] arrowRegion = new TextureRegion[5];
    public float coverLength = 10f;
    public boolean drawCover = false;
    public boolean drawArrow = false;

    @Override
    public void load(){
        super.load();
        editorRegion = Core.atlas.find(name + "-full");
        coverRegion = Core.atlas.find(name + "-cover");

        topRegion = WHUtils.splitLayers2(name + "-top", 32, 512, 64);
        capRegion = new TextureRegion[]{topRegion[1][0], topRegion[1][1]};

        botRegions = new TextureRegion[5];
        for(int i = 0; i < 5; i++){
            botRegions[i] = Core.atlas.find("conduit-bottom-" + i);
        }
        for(int i = 0; i < 5; i++){
            arrowRegion[i] = Core.atlas.find(name + "-arrow-" + i);
        }

        rotateRegions = new TextureRegion[4][2][animationFrames];

        if(renderer != null){
            float pad = rotatePad;
            var frames = renderer.getFluidFrames();

            for(int rot = 0; rot < 4; rot++){
                for(int fluid = 0; fluid < 2; fluid++){
                    for(int frame = 0; frame < animationFrames; frame++){
                        TextureRegion base = frames[fluid][frame];
                        TextureRegion result = new TextureRegion();
                        result.set(base);

                        if(rot == 0){
                            result.setX(result.getX() + pad);
                            result.setHeight(result.height - pad);
                        }else if(rot == 1){
                            result.setWidth(result.width - pad);
                            result.setHeight(result.height - pad);
                        }else if(rot == 2){
                            result.setWidth(result.width - pad);
                            result.setY(result.getY() + pad);
                        }else{
                            result.setX(result.getX() + pad);
                            result.setY(result.getY() + pad);
                        }

                        rotateRegions[rot][fluid][frame] = result;
                    }
                }
            }
        }

    }

    @Override
    public void init(){
        super.init();
        for(int i = 0; i < 5; i++){
            arrowRegion[i] = Core.atlas.find(name + "-arrow-" + i);
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{editorRegion};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        int[] bits = getTiling(plan, list);

        if(bits == null) return;

        BuildPlan[] directionals = new BuildPlan[4];
        list.each(other -> {
            if(other.breaking || other == plan) return;

            int i = 0;
            for(Point2 point : Geometry.d4){
                int x = plan.x + point.x, y = plan.y + point.y;
                if(x >= other.x - (other.block.size - 1) / 2 && x <= other.x + (other.block.size / 2) && y >= other.y - (other.block.size - 1) / 2 && y <= other.y + (other.block.size / 2)){
                    if((other.block instanceof Conduit ? (plan.rotation == i || (other.rotation + 2) % 4 == i) : !noSideBlend && ((plan.rotation == i && other.block.hasLiquids) || (plan.rotation != i && other.block.outputsLiquid)))){
                        directionals[i] = other;
                    }
                }
                i++;
            }
        });

        int mask = 1 << plan.rotation;
        for(int rel = 0; rel < 4; rel++){
            if((bits[3] & (1 << rel)) != 0){
                mask |= 1 << Mathf.mod(plan.rotation - rel, 4);
            }
        }
        Draw.rect(topRegion[0][mask], plan.drawx(), plan.drawy(), 0);

        Draw.scl(bits[1], bits[2]);
        Draw.color(botColor);
        Draw.alpha(0.5f);
        Draw.rect(botRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.color();
        if(drawArrow) Draw.rect(arrowRegion[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.scl();

        for(byte i : tileMap[mask]){
            if(directionals[i] == null || (directionals[i].block instanceof Conduit ? (directionals[i].rotation + 2) % 4 == plan.rotation : ((plan.rotation == i && !directionals[i].block.hasLiquids) || (plan.rotation != i && !directionals[i].block.outputsLiquid)))){
                int id = i == 0 || i == 3 ? 1 : 0;
                Draw.rect(capRegion[id], plan.drawx(), plan.drawy(), i == 0 || i == 2 ? 0 : -90);
            }
        }
    }

    @Override
    protected void initBuilding(){
        if(buildType == null) buildType = TubeConduitBuild::new;
    }

    public class TubeConduitBuild extends ConduitBuild{
        public int tiling = 0;

        public boolean shouldDrawCover;

        @Override
        public void created(){
            super.created();
            if(!drawCover) return;
            boolean hasCover = false;
            for(int r = 1; r <= coverLength; r++){
                Tile other = tile.nearby(Geometry.d4(rotation + 2).x * r, Geometry.d4(rotation + 2).y * r);
                if(other != null && other.build != this && other.build instanceof TubeConduitBuild b && b.rotation == rotation
                && b.block.name.equals(block.name) && b.shouldDrawCover && !b.backCapped && !b.capped){
                    float dist = Math.max(Math.abs(b.tileX() - tile.x), Math.abs(b.tileY() - tile.y));
                    if(dist >= coverLength - 0.1f){
                        shouldDrawCover = true;
                        return;
                    }
                }
            }
            for(int r = 1; r <= coverLength; r++){
                Tile backCap = tile.nearby(Geometry.d4(rotation + 2).x * r, Geometry.d4(rotation + 2).y * r);
                if(!hasCover && (backCap != null && backCap.build != this && backCap.build instanceof TubeConduitBuild a
                && a.block.name.equals(block.name) && a.rotation == rotation && (a.capped || a.backCapped || a.blendbits == 1 || a.blendbits == 3))){
                    float dist = Math.max(Math.abs(a.tileX() - tile.x), Math.abs(a.tileY() - tile.y));
                    if(dist >= coverLength - 0.1f){
                        hasCover = true;
                    }
                }
            }
            shouldDrawCover = hasCover;
        }

        public boolean valid(int i){
            Building b = nearby(i);
            return b != null && (b instanceof TubeConduitBuild ?
            (b.front() != null && b.front() == this) : b.block.hasLiquids || b.block.outputsLiquid);
        }

        public boolean isEnd(int i){
            Building b = nearby(i);
            return (!valid(i) && (b == null ? null : b.block) != block) || (b instanceof ConduitBuild &&
            ((b.rotation + 2) % 4 == rotation || (b.front() != this && back() == b)));
        }

        @Override
        public void draw(){
            int r = this.rotation;
            //draw extra conduits facing this one for tiling purposes
            Draw.z(Layer.blockUnder);
            for(int i = 0; i < 4; i++){
                if((blending & (1 << i)) != 0){
                    int dir = r - i;
                    drawAt(x + Geometry.d4x(dir) * mindustry.Vars.tilesize * 0.75f, y + Geometry.d4y(dir) * mindustry.Vars.tilesize * 0.75f, 0, i == 0 ? r : dir, i != 0 ? SliceMode.bottom : SliceMode.top);
                }
            }

            Draw.z(Layer.block);

            float oldX = Draw.xscl, oldY = Draw.yscl;
            Draw.scl(1.017f, 1.017f);
            if(capped || backCapped){
                Draw.z(Layer.block - 0.003f);
                Draw.rect(topRegion[0][tiling], x, y, 0);
            }else{
                Draw.z(Layer.block + 0.001f);
                Draw.rect(topRegion[0][tiling], x, y, 0);
            }

            Draw.scl(oldX, oldY);

            Draw.z(Layer.block - 0.1f);
            Draw.scl(xscl, yscl);
            drawAt(x, y, blendbits, r, SliceMode.none);
            Draw.scl();

            byte[] placementId = tileMap[tiling];

            Draw.scl(1.01f, 1.01f);
            Draw.z(Layer.block + 0.002f);

            if(drawCover && shouldDrawCover && blendbits != 3 && blendbits != 1){
                for(byte i : placementId){
                    Draw.rect(coverRegion, x, y, i == 0 || i == 2 ? 0 : -90);
                }
            }
            Draw.scl();

            for(byte i : placementId){
                if(isEnd(i)){
                    int id = i == 0 || i == 3 ? 1 : 0;
                    Draw.rect(capRegion[id], x, y, i == 0 || i == 2 ? 0 : -90);
                }
            }
        }

        @Override
        protected void drawAt(float x, float y, int bits, int rotation, SliceMode slice){
            float angle = rotation * 90f;
            Draw.color(botColor);
            Draw.rect(sliced(botRegions[bits], slice), x, y, angle);

            int offset = yscl == -1 ? 3 : 0;

            int frame = liquids.current().getAnimationFrame();
            int gas = liquids.current().gas ? 1 : 0;
            float ox = 0f, oy = 0f;
            int wrapRot = (rotation + offset) % 4;
            TextureRegion liquidr = bits == 1 && padCorners ? rotateRegions[wrapRot][gas][frame] : renderer.fluidFrames[gas][frame];

            if(bits == 1 && padCorners){
                ox = rotateOffsets[wrapRot][0];
                oy = rotateOffsets[wrapRot][1];
            }

            //the drawing state machine sure was a great design choice with no downsides or hidden behavior!!!
            float sx = Draw.xscl, sy = Draw.yscl;
            Draw.scl(1f, 1f);
            Drawf.liquid(sliced(liquidr, slice), x + ox, y + oy, smoothLiquid, liquids.current().color.write(Tmp.c1).a(1f));
            Draw.scl(sx, sy);

            if(drawArrow){
                Draw.z(Layer.block);
                Draw.rect(sliced(arrowRegion[bits], slice), x, y, angle);
            }
        }

        @Override
        public void onProximityUpdate(){
            int[] bits = buildBlending(tile, rotation, null, true);
            blendbits = bits[0];
            xscl = bits[1];
            yscl = bits[2];
            blending = bits[4];

            int mask = 1 << rotation;

            // bits[3] 的 rel 位表示“相对当前朝向 rel 方向有连接”。
            // 需要把相对方向转回绝对方向：abs = mod(rotation - rel, 4)
            // 然后将该绝对方向对应的 bit OR 到 mask 里。
            for(int rel = 0; rel < 4; rel++){
                if((bits[3] & (1 << rel)) != 0){
                    mask |= 1 << Mathf.mod(rotation - rel, 4);
                }
            }
            // 例子：
            // rotation=1，且 bits[3] 只有 rel=1 连通（bits[3]=0b0010）：
            // abs = mod(1 - 1, 4) = 0
            // mask: 0b0010 | (1 << 0) = 0b0011 => tiling=3

            tiling = mask;
            Building next = front(), prev = back();
            capped = next == null || next.team != team || !next.block.hasLiquids;
            backCapped = blendbits == 0 && (prev == null || prev.team != team || !prev.block.hasLiquids);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(shouldDrawCover);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            shouldDrawCover = read.bool();
        }
    }
}
