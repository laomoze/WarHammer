package wh.entities.world.blocks.distribution;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.IntSeq;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.entities.TargetPriority;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.ItemBridge;
import mindustry.world.blocks.distribution.ItemBridge.*;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.*;

import static arc.math.geom.Mat3D.rot;
import static mindustry.Vars.*;

public class HeatDirectionBridge extends Block {
    private static BuildPlan otherReq;

    public int range = 10;
    public float lost = 0.003f;
    public float visualMaxHeat = 15f;
    public DrawBlock drawer = new DrawDefault();
    public boolean splitHeat = false;
    public TextureRegion endRegion, bridgeRegion, arrowRegion, dirRegion;

    public boolean fadeIn = true;
    public boolean pulse = false;
    public float arrowSpacing = 4f, arrowOffset = 2f, arrowPeriod = 0.4f;
    public float arrowTimeScl = 6.2f;
    public float bridgeWidth = 8f;

    //for autolink
    public @Nullable HeatDirectionBridge.HeatDirectionBridgeBuild lastBuild;

    public HeatDirectionBridge(String name) {
        super(name);

        rotate = true;
        update = true;
        solid = true;
        underBullets = true;
        hasPower = true;
        itemCapacity = 10;
        configurable = true;
        hasItems = false;
        group = BlockGroup.transportation;
        noUpdateDisabled = true;
        allowDiagonal = false;
        copyConfig = false;

        //disabled as to not be annoying
        allowConfigInventory = false;
        priority = TargetPriority.transport;

        config(Point2.class, (HeatDirectionBridgeBuild tile, Point2 point) -> tile.link = Point2.pack(point.x + tile.tileX(), point.y + tile.tileY()));
        config(Integer.class, (HeatDirectionBridgeBuild tile, Integer point) -> tile.link = point);
    }

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list) {
        otherReq = null;
        list.each(other -> {
            if (other.block == this && plan != other && plan.config instanceof Point2 p && p.equals(other.x - plan.x, other.y - plan.y)) {
                otherReq = other;
            }
        });

        if (otherReq != null) {
            drawBridge(plan, otherReq.drawx(), otherReq.drawy(), 0);
        }
    }

    public void drawBridge(BuildPlan req, float ox, float oy, float flip) {
        if (Mathf.zero(Renderer.bridgeOpacity)) return;
        Draw.alpha(Renderer.bridgeOpacity);

        Lines.stroke(bridgeWidth);

        Tmp.v1.set(ox, oy).sub(req.drawx(), req.drawy()).setLength(tilesize / 2f);

        Lines.line(
                bridgeRegion,
                req.drawx() + Tmp.v1.x,
                req.drawy() + Tmp.v1.y,
                ox - Tmp.v1.x,
                oy - Tmp.v1.y, false
        );

        Draw.rect(arrowRegion, (req.drawx() + ox) / 2f, (req.drawy() + oy) / 2f,
                Angles.angle(req.drawx(), req.drawy(), ox, oy) + flip);

        Draw.reset();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        int dx = Geometry.d4x(rotation), dy = Geometry.d4y(rotation);
        Tile link = findLink(x, y);

        int length = range;

        Drawf.dashLine(Pal.placing,
                x * tilesize + dx * (tilesize / 2f + 2),
                y * tilesize + dy * (tilesize / 2f + 2),
                x * tilesize + dx * (length) * tilesize,
                y * tilesize + dy * (length) * tilesize
        );

        Draw.reset();
        Draw.color(Pal.placing);
        Lines.stroke(1f);
        if (link != null && Math.abs(link.x - x) + Math.abs(link.y - y) > 1) {
            int rot = link.absoluteRelativeTo(x, y);
            float w = (link.x == x ? tilesize : Math.abs(link.x - x) * tilesize - tilesize);
            float h = (link.y == y ? tilesize : Math.abs(link.y - y) * tilesize - tilesize);
            Lines.rect((x + link.x) / 2f * tilesize - w / 2f, (y + link.y) / 2f * tilesize - h / 2f, w, h);

            Draw.rect(Core.atlas.find("bridge-arrow"), link.x * tilesize + Geometry.d4(rot).x * tilesize, link.y * tilesize + Geometry.d4(rot).y * tilesize, link.absoluteRelativeTo(x, y) * 90);
        }
        Draw.reset();
    }

    public boolean linkValid(Tile tile, Tile other) {
        return linkValid(tile, other, true);
    }

    public boolean linkValid(Tile tile, Tile other, boolean checkDouble) {
        if (other == null || tile == null || !positionsValid(tile.x, tile.y, other.x, other.y)) return false;

        return ((other.block() == tile.block() && tile.block() == this) || (!(tile.block() instanceof HeatDirectionBridge) && other.block() == this))
                && (other.team() == tile.team() || tile.block() != this)
                && (!checkDouble || ((HeatDirectionBridge.HeatDirectionBridgeBuild) other.build).link != tile.pos());
    }

    public boolean positionsValid(int x1, int y1, int x2, int y2) {
        if (x1 == x2) {
            return Math.abs(y1 - y2) <= range;
        } else if (y1 == y2) {
            return Math.abs(x1 - x2) <= range;
        } else {
            return false;
        }
    }

    public Tile findLink(int x, int y) {
        Tile tile = world.tile(x, y);
        if (tile != null && tile.build != null && lastBuild != null && linkValid(tile, lastBuild.tile) && lastBuild.tile != tile && lastBuild.link == -1) {
            if (tile.build.rotation == lastBuild.rotation) {
                return lastBuild.tile;
            }
        }
        return null;
    }


    @Override
    public void setBars() {
        super.setBars();
        addBar("heat", (HeatDirectionBridgeBuild entity) -> new Bar(() -> Core.bundle.format("bar.heatamount", (int) (entity.heat + 0.001f)), () -> Pal.lightOrange, () -> entity.heat / visualMaxHeat));
    }

    @Override
    public void load() {
        super.load();

        endRegion = Core.atlas.find(name + "-end");
        dirRegion = Core.atlas.find(name + "-dir");
        bridgeRegion = Core.atlas.find(name + "-bridge");
        arrowRegion = Core.atlas.find(name + "-arrow");
        drawer.load(this);
    }

    @Override
    public void init() {
        super.init();
        updateClipRadius((range + 0.5f) * tilesize);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return new TextureRegion[]{region, dirRegion};
    }

    @Override
    public void handlePlacementLine(Seq<BuildPlan> plans) {
        for (int i = 0; i < plans.size - 1; i++) {
            var cur = plans.get(i);
            var next = plans.get(i + 1);
            if (positionsValid(cur.x, cur.y, next.x, next.y) && cur.rotation == next.rotation) {
                cur.config = new Point2(next.x - cur.x, next.y - cur.y);
            }
        }
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation) {
        Placement.calculateNodes(points, this, rotation, (point, other) -> Math.max(Math.abs(point.x - other.x), Math.abs(point.y - other.y)) <= range);
    }

    public class HeatDirectionBridgeBuild extends Building implements HeatBlock, HeatConsumer {
        public float warmup;
        public float time = -8f, timeSpeed;
        public int link = -1;
        public Seq<Building> owners = new Seq<>();
        public IntSeq incoming = new IntSeq(false, 4);

        public float heat = 0f;
        public float[] sideHeat = new float[4];
        public IntSet cameFrom = new IntSet();
        public long lastHeatUpdate = -1;

        @Override
        public void pickedUp() {
            link = -1;
        }


        @Override
        public void draw() {
            Draw.rect(block.region, x, y);
            Draw.rect(dirRegion, x, y, rotdeg());

            Draw.z(Layer.power);

            Tile other = world.tile(link);
            if (!linkValid(tile, other)) return;

            if (Mathf.zero(Renderer.bridgeOpacity)) return;

            int i = relativeTo(other.x, other.y);

            if (pulse) {
                Draw.color(Color.white, Color.black, Mathf.absin(Time.time, 6f, 0.07f));
            }

            float warmup = hasPower ? this.warmup : 1f;

            Draw.alpha((fadeIn ? Math.max(warmup, 0.9f) : 1f) * Renderer.bridgeOpacity);

            Draw.rect(endRegion, x, y, i * 90 + 90);
            Draw.rect(endRegion, other.drawx(), other.drawy(), i * 90 + 270);

            Lines.stroke(bridgeWidth);

            Tmp.v1.set(x, y).sub(other.worldx(), other.worldy()).setLength(tilesize / 2f).scl(-1f);

            Lines.line(bridgeRegion,
                    x + Tmp.v1.x,
                    y + Tmp.v1.y,
                    other.worldx() - Tmp.v1.x,
                    other.worldy() - Tmp.v1.y, false);

            int dist = Math.max(Math.abs(other.x - tile.x), Math.abs(other.y - tile.y)) - 1;

            Draw.color();

            int arrows = (int) (dist * tilesize / arrowSpacing), dx = Geometry.d4x(i), dy = Geometry.d4y(i);

            for (int a = 0; a < arrows; a++) {
                Draw.alpha(Mathf.absin(a - time / arrowTimeScl, arrowPeriod, 1f) * warmup * Renderer.bridgeOpacity);
                Draw.rect(arrowRegion,
                        x + dx * (tilesize / 2f + a * arrowSpacing + arrowOffset),
                        y + dy * (tilesize / 2f + a * arrowSpacing + arrowOffset),
                        i * 90f);
            }
            Draw.reset();
        }

        @Override
        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public void playerPlaced(Object config) {

            super.playerPlaced(config);

            Tile link = findLink(tile.x, tile.y);
            if (linkValid(tile, link) && this.link != link.pos() && !proximity.contains(link.build)) {
                link.build.configure(tile.pos());
            }

            lastBuild = this;
        }

        @Override
        public void updateTile() {
            if (owners.size == 0 && link == -1) heat = 0;
            checkOwner();

            Building linked = world.build(this.link);
            Tile other = world.tile(link);
            boolean hasLink = linkValid(tile, other);

            if (hasLink) {
                HeatDirectionBridgeBuild otherOne = (HeatDirectionBridgeBuild) linked;
                if (otherOne.checkOneOwner(this)) otherOne.owners.add(this);
                this.updateTransfer();
                otherOne.updateTransfer();
                if(this.heat > 0) {
                    timeSpeed = Mathf.approachDelta(timeSpeed, 1, 1f / 60f);
                    time += timeSpeed * delta();
                    warmup = Mathf.approachDelta(warmup, efficiency, 1f / 30f);
                }
            } else {
                warmup = Mathf.slerpDelta(warmup, 0, 0.02f);
            }
        }

        public void updateTransfer() {
            if (owners.size > 0) {
                float totalHeat = 0f;
                for (int i = 0; i < owners.size; i++) {
                    Building o = owners.get(i);
                    HeatDirectionBridgeBuild owner = (HeatDirectionBridgeBuild) o;
                    float dst = this.dst(owner) / (range * tilesize);
                    totalHeat += owner.heat * (1 - dst * lost);
                    totalHeat*= owner.block.hasPower? owner.power.status:1;
                }
                heat = totalHeat;
            } else {
                updateHeat();
            }
        }

        public void updateHeat() {
            if (lastHeatUpdate == Vars.state.updateId) return;
            lastHeatUpdate = Vars.state.updateId;
            heat = calculateHeat(sideHeat, cameFrom);
        }

        @Override
        public float heatRequirement() {
            Tile other = world.tile(link);
            return linkValid(tile, other) ? visualMaxHeat : Float.MAX_VALUE;
        }

        @Override
        public float warmup() {
            return heat;
        }

        @Override
        public float heat() {
            return (owners.size > 0 && link == -1) ? heat : 0;
        }

        @Override
        public float heatFrac() {
            return (heat / visualMaxHeat) / (splitHeat ? 3f : 1);
        }

        @Override
        public float[] sideHeat() {
            return sideHeat;
        }

        @Override
        public void drawSelect() {
            if (linkValid(tile, world.tile(link))) {
                drawInput(world.tile(link));
            }

            incoming.each(pos -> drawInput(world.tile(pos)));

            Draw.reset();
        }

        private void drawInput(Tile other) {
            if (!linkValid(tile, other, false)) return;
            boolean linked = other.pos() == link;

            Tmp.v2.trns(tile.angleTo(other), 2f);
            float tx = tile.drawx(), ty = tile.drawy();
            float ox = other.drawx(), oy = other.drawy();
            float alpha = Math.abs((linked ? 100 : 0) - (Time.time * 2f) % 100f) / 100f;
            float x = Mathf.lerp(ox, tx, alpha);
            float y = Mathf.lerp(oy, ty, alpha);

            Tile otherLink = linked ? other : tile;
            int rel = (linked ? tile : other).absoluteRelativeTo(otherLink.x, otherLink.y);

            //draw "background"
            Draw.color(Pal.gray);
            Lines.stroke(2.5f);
            Lines.square(ox, oy, 2f, 45f);
            Lines.stroke(2.5f);
            Lines.line(tx + Tmp.v2.x, ty + Tmp.v2.y, ox - Tmp.v2.x, oy - Tmp.v2.y);

            float color = (linked ? Pal.place : Pal.accent).toFloatBits();

            //draw foreground colors
            Draw.color(color);
            Lines.stroke(1f);
            Lines.line(tx + Tmp.v2.x, ty + Tmp.v2.y, ox - Tmp.v2.x, oy - Tmp.v2.y);

            Lines.square(ox, oy, 2f, 45f);

            Draw.mixcol(color);
            Draw.color();
            Draw.rect(arrowRegion, x, y, rel * 90);
            Draw.mixcol();
        }

        @Override
        public void drawConfigure() {
            Drawf.select(x, y, tile.block().size * tilesize / 2f + 2f, Pal.accent);

            for (int i = 1; i <= range; i++) {
                Tile other = tile.nearby(Geometry.d4x(rotation) * i, Geometry.d4y(rotation) * i);
                if (linkValid(tile, other)) {
                    boolean linked = other.pos() == link;

                    Drawf.select(other.drawx(), other.drawy(),
                            other.block().size * tilesize / 2f + 2f + (linked ? 0f : Mathf.absin(Time.time, 4f, 1f)), linked ? Pal.place : Pal.breakInvalid);
                }
            }
        }


        @Override
        public boolean onConfigureBuildTapped(Building other) {

            if (other instanceof HeatDirectionBridgeBuild b && b.rotation == this.rotation) {
                //reverse connection
                if (b.link == pos()) {
                    configure(other.pos());
                    other.configure(-1);
                    return true;
                }

                if (linkValid(tile, other.tile)) {
                    if (link == other.pos()) {
                        configure(-1);
                    } else {
                        configure(other.pos());
                    }
                    return false;
                }
            }
            return true;
        }


        public void checkOwner() {
            for (int i = 0; i < owners.size; i++) {
                int pos = owners.get(i).pos();
                Building build = world.build(pos);
                if (build instanceof HeatDirectionBridgeBuild owner) {
                    if (owner.block != block || owner.link != this.pos()) owners.remove(i);
                } else {
                    owners.remove(i);
                }
            }
        }

        protected boolean checkOneOwner(Building other) {
            if (owners.size == 0) return true;
            return !owners.contains(other);
        }

        @Override
        public Point2 config() {
            return Point2.unpack(link).sub(tile.x, tile.y);
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.i(link);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            link = read.i();
            warmup = read.f();
        }
    }
}
