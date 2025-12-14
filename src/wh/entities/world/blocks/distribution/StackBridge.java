package wh.entities.world.blocks.distribution;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.StackConveyor.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class StackBridge extends Block{
    private static BuildPlan otherReq;

    public final int timerCheckMoved = timers++;

    public int range;
    public float speed;
    public float baseEfficiency = 0f;

    public TextureRegion endRegion, bridgeRegion, arrowRegion, stackRegion;

    public boolean fadeIn = true, pulse = true;
    public float arrowSpacing = 4f, arrowOffset = 2f, arrowPeriod = 0.4f;
    public float arrowTimeScl = 6.2f;
    public float bridgeWidth = 6.5f;

    public Effect loadEffect = Fx.conveyorPoof;
    public Effect unloadEffect = Fx.conveyorPoof;

    //for autolink
    public @Nullable StackBridgeBuild lastBuild;

    public StackBridge(String name){
        super(name);

        rotateDraw = false;
        rotate = true;

        update = true;
        solid = true;
        underBullets = true;
        hasPower = true;
        itemCapacity = 10;
        configurable = true;
        hasItems = true;
        unloadable = false;
        group = BlockGroup.transportation;
        noUpdateDisabled = true;
        allowDiagonal = false;
        copyConfig = false;
        allowConfigInventory = false;
        priority = TargetPriority.transport;

        config(Point2.class, (StackBridgeBuild tile, Point2 i) -> tile.link = Point2.pack(i.x + tile.tileX(), i.y + tile.tileY()));
        config(Integer.class, (StackBridgeBuild tile, Integer i) -> tile.link = i);
        config(Integer.class, (StackBridgeBuild tile, Integer i) -> {
            tile.link = i;
            if(i != -1 && world.tile(i) != null && world.tile(i).build != null){
                tile.caculateBridgeDst(world.tile(i).build);
            }
        });
    }

    @Override
    public void load(){
        super.load();
        endRegion = Core.atlas.find(name + "-end");
        bridgeRegion = Core.atlas.find(name + "-bridge");
        arrowRegion = Core.atlas.find(name + "-arrow");
        stackRegion = Core.atlas.find(name + "-stack");
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.itemsMoved, Mathf.round(itemCapacity * (60 / speed)), StatUnit.itemsSecond);
    }

    @Override
    public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list){
        otherReq = null;
        list.each(other -> {
            if(other.block == this && plan != other && plan.config instanceof Point2 p && p.equals(other.x - plan.x, other.y - plan.y)){
                otherReq = other;
            }
        });

        if(otherReq != null){
            drawBridge(plan, otherReq.drawx(), otherReq.drawy(), 0);
        }
    }

    public void drawBridge(BuildPlan req, float ox, float oy, float flip){
        if(Mathf.zero(Renderer.bridgeOpacity)) return;
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
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Tile link = findLink(x, y);

        for(int i = 0; i < 4; i++){
            Drawf.dashLine(Pal.placing,
            x * tilesize + Geometry.d4[i].x * (tilesize / 2f + 2),
            y * tilesize + Geometry.d4[i].y * (tilesize / 2f + 2),
            x * tilesize + Geometry.d4[i].x * (range) * tilesize,
            y * tilesize + Geometry.d4[i].y * (range) * tilesize);
        }

        Draw.reset();
        Draw.color(Pal.placing);
        Lines.stroke(1f);
        if(link != null && Math.abs(link.x - x) + Math.abs(link.y - y) > 1){
            int rot = link.absoluteRelativeTo(x, y);
            float w = (link.x == x ? tilesize : Math.abs(link.x - x) * tilesize - tilesize);
            float h = (link.y == y ? tilesize : Math.abs(link.y - y) * tilesize - tilesize);
            Lines.rect((x + link.x) / 2f * tilesize - w / 2f, (y + link.y) / 2f * tilesize - h / 2f, w, h);

            Draw.rect("bridge-arrow", link.x * tilesize + Geometry.d4(rot).x * tilesize, link.y * tilesize + Geometry.d4(rot).y * tilesize, link.absoluteRelativeTo(x, y) * 90);
        }
        Draw.reset();
    }

    public boolean linkValid(Tile tile, Tile other){
        return linkValid(tile, other, true);
    }

    public boolean linkValid(Tile tile, Tile other, boolean checkDouble){
        if(other == null || tile == null || !positionsValid(tile.x, tile.y, other.x, other.y)) return false;

        return ((other.block() == tile.block() && tile.block() == this) ||
        (!(tile.block() instanceof StackBridge) && other.block() == this))
        && (other.team() == tile.team() || tile.block() != this)
        && (!checkDouble || ((StackBridgeBuild)other.build).link != tile.pos() || ((StackBridgeBuild)other.build).bridgeDst == 0);
    }

    public boolean positionsValid(int x1, int y1, int x2, int y2){
        if(x1 == x2){
            return Math.abs(y1 - y2) <= range;
        }else if(y1 == y2){
            return Math.abs(x1 - x2) <= range;
        }else{
            return false;
        }
    }

    public Tile findLink(int x, int y){
        Tile tile = world.tile(x, y);
        if(tile != null && lastBuild != null && linkValid(tile, lastBuild.tile) && lastBuild.tile != tile
        && lastBuild.link == -1 /*&& (lastBuild.bridgeDst == 0 || lastBuild.bridgeSeg == 0)*/){
            return lastBuild.tile;
        }
        return null;
    }

    @Override
    public void init(){
        super.init();
        updateClipRadius((range + 0.5f) * tilesize);
    }

    @Override
    public void handlePlacementLine(Seq<BuildPlan> plans){
        for(int i = 0; i < plans.size - 1; i++){
            var cur = plans.get(i);
            var next = plans.get(i + 1);
            if(positionsValid(cur.x, cur.y, next.x, next.y)){
                cur.config = new Point2(next.x - cur.x, next.y - cur.y);
            }
        }
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation){
        Placement.calculateNodes(points, this, rotation, (point, other) -> Math.max(Math.abs(point.x - other.x), Math.abs(point.y - other.y)) <= range);
    }

    public class StackBridgeBuild extends Building{
        public int link = -1;
        public float cooldown;
        public Item lastItem;

        public IntSeq incoming = new IntSeq(false, 4);

        public Queue<ItemStacker> bridgeItems = new Queue<>();
        public float progress = 0f;
        public float bridgeDst;
        public float bridgeSeg;

        public float warmup, time = -8f, timeSpeed;

        public boolean wasMoved, moved;

        @Override
        public void pickedUp(){
            link = -1;
            bridgeItems.clear();
        }

        @Override
        public void created(){
            super.created();
            bridgeItems = new Queue<>();
        }

        @Override
        public void drawSelect(){
            if(linkValid(tile, world.tile(link))){
                drawInput(world.tile(link));
            }

            incoming.each(pos -> drawInput(world.tile(pos)));

            Draw.reset();
        }

        private void drawInput(Tile other){
            if(!linkValid(tile, other, false)) return;
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
        public void drawConfigure(){
            Drawf.select(x, y, tile.block().size * tilesize / 2f + 2f, Pal.accent);

            for(int i = 1; i <= range; i++){
                for(int j = 0; j < 4; j++){
                    Tile other = tile.nearby(Geometry.d4[j].x * i, Geometry.d4[j].y * i);
                    if(linkValid(tile, other)){
                        boolean linked = other.pos() == link;

                        Drawf.select(other.drawx(), other.drawy(),
                        other.block().size * tilesize / 2f + 2f + (linked ? 0f : Mathf.absin(Time.time, 4f, 1f)), linked ? Pal.place : Pal.breakInvalid);
                    }
                }
            }
        }

        @Override
        public void playerPlaced(Object config){
            super.playerPlaced(config);

            Tile link = findLink(tile.x, tile.y);
            if(link != null && this.link != link.pos() && !(proximity.contains(link.build))){
                link.build.configure(tile.pos());
                caculateBridgeDst(link.build);
            }

            lastBuild = this;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(other instanceof StackBridgeBuild b && b.link == pos()){
                caculateBridgeDst(other);
                configure(other.pos());
                other.configure(-1);
                return true;
            }

            if(linkValid(tile, other.tile)){
                if(link == other.pos()){
                    configure(-1);
                }else{
                    caculateBridgeDst(other);
                    configure(other.pos());
                }
                return false;
            }
            return true;
        }

        public void caculateBridgeDst(Building other){
           /* Tmp.v1.trns(rotdeg(), tilesize / 2f).add(x, y);
            Tmp.v2.trns(other.rotdeg(), -tilesize / 2f).add(other);*/
            bridgeDst = this.dst(other);
            bridgeSeg = bridgeDst / tilesize;
        }


        public void checkIncoming(){
            int idx = 0;
            while(idx < incoming.size){
                int i = incoming.items[idx];
                Tile other = world.tile(i);
                if(!linkValid(tile, other, false) || ((StackBridgeBuild)other.build).link != tile.pos()){
                    incoming.removeIndex(idx);
                    idx--;
                }
                idx++;
            }
        }

        public float framePeriod(){
            return 60f / speed;
        }

        @Override
        public void updateTile(){
            float eff = enabled ? (efficiency + baseEfficiency) : 1f;

            checkIncoming();

            if(lastItem == null || !items.has(lastItem)){
                lastItem = items.first();
            }

            if(progress <= framePeriod()) progress += edelta() * eff * warmup;

            if(timer(timerCheckMoved, 30f)){
                wasMoved = moved;
                moved = false;
            }

            timeSpeed = Mathf.approachDelta(timeSpeed, wasMoved ? 1f : 0f, 1f / 60f);

            time += timeSpeed * delta();

            Tile other = world.tile(link);

            if(other != null && other.build instanceof StackBridgeBuild b && bridgeDst == 0){
                caculateBridgeDst(b);
            }

            int stateLoad = 1;
            if(!linkValid(tile, other)){
                for(var p : proximity){
                    if(!(back() instanceof StackBridgeBuild) && p instanceof StackConveyorBuild e && e.team == team
                    && e.link == -1){
                        e.items.add(items);
                        e.lastItem = lastItem;
                        e.link = tile.pos();
                        e.cooldown = 1;

                        //▲ to | from ▼
                        items.clear();
                        link = -1;
                        poofOut();
                    }else{
                        link = -1;
                        warmup = 0f;
                        dump(lastItem);
                    }
                }
            }else{
                warmup = Mathf.approachDelta(warmup, efficiency, 1f / 30f);

                if(progress >= framePeriod() && bridgeCanInsert()){
                    if(lastItem != null){
                        bridgeQueueItem(lastItem, stackCount());
                        items.remove(lastItem, stackCount());
                    }
                    progress %= framePeriod();
                }

                Building target = other.build;

                if(target != null && target.team == team && target instanceof StackBridgeBuild bridge){
                    var inc = bridge.incoming;
                    int pos = tile.pos();
                    if(!inc.contains(pos)){
                        inc.add(pos);
                    }
                    updateBridge(bridge, eff);
                }
            }
        }

        public boolean bridgeCanInsert(){
            if(bridgeItems.isEmpty()){
                return bridgeSeg > 0;
            }else{
                return bridgeItems.last().progress > framePeriod();
            }
        }


        public void bridgeQueueItem(Item item, int count){
            bridgeItems.addLast(new ItemStacker(item, count));
        }

        public void updateBridge(StackBridgeBuild other, float eff){
            if(bridgeItems.isEmpty()) return;

            for(int i = 0; i < bridgeItems.size; i++){
                float maxProgressLimit = (bridgeSeg - i) * framePeriod();
                float progress = bridgeItems.get(i).progress;
                if(progress < maxProgressLimit){
                    moved = true;
                    bridgeItems.get(i).addProgress(edelta() * eff);
                }
            }

            ItemStacker first = bridgeItems.first();
            if(first.progress > bridgeSeg * framePeriod()
            && other.acceptBridge()/*other.items.get(first.itemStack.item) < other.getMaximumAccepted(first.itemStack.item)*/){
                other.handleBridge(first.itemStack.item, first.itemStack.amount);
                other.progress = framePeriod();
                bridgeItems.removeFirst();
            }
        }

        public boolean acceptBridge(){
            return items.empty();
        }

        public void handleBridge(Item item, int count){
            items.add(item, count);
        }

        protected void poofIn(){
            loadEffect.at(this);
        }

        protected void poofOut(){
            unloadEffect.at(this);
        }

        @Override
        public void draw(){
            super.draw();

            Draw.z(Layer.power);

            Tile other = world.tile(link);
            if(!linkValid(tile, other)) return;

            if(Mathf.zero(Renderer.bridgeOpacity)) return;

            int i = relativeTo(other.x, other.y);

            if(pulse){
                Draw.color(Color.white, Color.black, Mathf.absin(Time.time, 6f, 0.07f));
            }

            float warmup = hasPower ? this.warmup : 1f;

            Draw.alpha((fadeIn ? Math.max(warmup, 0.25f) : 1f) * Renderer.bridgeOpacity);

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

            int arrows = (int)(dist * tilesize / arrowSpacing), dx = Geometry.d4x(i), dy = Geometry.d4y(i);

            for(int a = 0; a < arrows; a++){
                Draw.alpha(Mathf.absin(a - time / arrowTimeScl, arrowPeriod, 1f) * warmup * Renderer.bridgeOpacity);
                Draw.rect(arrowRegion,
                x + dx * (tilesize / 2f + a * arrowSpacing + arrowOffset),
                y + dy * (tilesize / 2f + a * arrowSpacing + arrowOffset),
                i * 90f);
            }

            Draw.reset();

            Draw.z(Layer.power + 0.001f);

            for(int a = 0; a < bridgeItems.size; a++){
                ItemStacker stack = bridgeItems.get(a);
                float progressRatio = stack.progress / (bridgeSeg * framePeriod());
                progressRatio = Mathf.clamp(progressRatio, 0f, 1f);

                Interp slightSmooth = a1 -> a1 * a1 * (2 - a1);

                int thisRotation = (int)this.angleTo(other);
                int nextRotation;
                if(other.build instanceof StackBridgeBuild nextBridge){
                    Tile other2 = world.tile(nextBridge.link);
                    if(other2 != null){
                        nextRotation = (int)other.angleTo(other2);
                    }else{
                        nextRotation = thisRotation;
                    }
                }else{
                    nextRotation = thisRotation;
                }

                float rot = Mathf.slerp(thisRotation, nextRotation,
                Mathf.curve(Interp.smooth.apply(slightSmooth.apply(progressRatio)), 0.9f, 1f));

                Tmp.v3.set(x, y).lerp(other.worldx(), other.worldy(), slightSmooth.apply(progressRatio));

                ItemStack item = stack.itemStack;

                Drawf.shadow(Tmp.v3.x, Tmp.v3.y, size * 1.2f);

                Draw.rect(stackRegion, Tmp.v3.x, Tmp.v3.y, thisRotation == nextRotation ? thisRotation : rot);

                float size = itemSize * Mathf.lerp(Math.min((float)item.amount / itemCapacity, 1), 1f, 0.4f);
                Draw.rect(item.item.fullIcon, Tmp.v3.x, Tmp.v3.y, size, size, 0);
            }
            Draw.reset();
        }

        @Override
        public void handleItem(Building source, Item item){
            if(items.empty() && tile != null) poofIn();
            super.handleItem(source, item);
            lastItem = item;
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source){
            if(amount <= 0) return;
            if(items.empty() && tile != null) poofIn();
            super.handleStack(item, amount, source);
            lastItem = item;
        }


        public int stackCount(){
            if(items.empty()) return 0;
            return items.get(lastItem);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if(this == source) return items.total() < itemCapacity && (!items.any() || items.has(item)); //player threw items
            return !((items.any() && !items.has(item)) //incompatible items
            || (items.total() >= getMaximumAccepted(item)) //filled to capacity
            || (progress >= framePeriod()));
        }

        protected boolean checkAccept(Building source, Tile link){
            if(tile == null || linked(source)) return true;

            if(linkValid(tile, link)){
                int rel = relativeTo(link);
                var facing = Edges.getFacingEdge(source, this);
                int rel2 = facing == null ? -1 : relativeTo(facing);

                return rel != rel2;
            }

            return false;
        }

        protected boolean linked(Building source){
            return source instanceof StackBridgeBuild && linkValid(source.tile, tile) && ((StackBridgeBuild)source).link == pos();
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source){
            if(items.any() && !items.has(item)) return 0;
            return super.acceptStack(item, amount, source);
        }

        @Override
        public boolean shouldConsume(){
            return linkValid(tile, world.tile(link)) && enabled;
        }

        @Override
        public Point2 config(){
            return Point2.unpack(link).sub(tile.x, tile.y);
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(link);
            write.f(warmup);
            write.f(progress);
            write.f(bridgeDst);
            write.f(bridgeSeg);

            write.b(bridgeItems.size);
            for(int i = 0; i < bridgeItems.size; i++){
                bridgeItems.get(i).write(write);
            }
            write.b(incoming.size);

            for(int i = 0; i < incoming.size; i++){
                write.i(incoming.items[i]);
            }

            write.bool(wasMoved || moved);

          /*  Log.info("write");
            Log.info("link: " + link);
            Log.info("warmup: " + warmup);
            Log.info("progress: " + progress);
            Log.info("bridgeDst: " + bridgeDst);
            Log.info("bridgeSeg: " + bridgeSeg);
            Log.info("bridgeItems: " + bridgeItems.size);
            Log.info("incoming: " + incoming.size);
            Log.info("wasMoved: " + wasMoved);
            Log.info("moved: " + moved);
            Log.info("-------------------");*/
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            link = read.i();
            warmup = read.f();
            progress = read.f();
            bridgeDst = read.f();
            bridgeSeg = read.f();

            int bridgeItemsSize = read.b();
            bridgeItems.clear();
            for(int i = 0; i < bridgeItemsSize; i++){
                ItemStacker stack = new ItemStacker(content.item(read.s()), read.i());
                stack.progress = read.f();
                bridgeItems.add(stack);
            }

            byte links = read.b();
            for(int i = 0; i < links; i++){
                incoming.add(read.i());
            }
            wasMoved = moved = read.bool();


            /*Log.info("read");
            Log.info("link: " + link);
            Log.info("warmup: " + warmup);
            Log.info("progress: " + progress);
            Log.info("bridgeDst: " + bridgeDst);
            Log.info("bridgeSeg: " + bridgeSeg);
            Log.info("bridgeItems: " + bridgeItems.size);
            Log.info("incoming: " + incoming.size);
            Log.info("wasMoved: " + wasMoved);
            Log.info("moved: " + moved);
            Log.info("-------------------");*/
        }
    }

    public class ItemStacker{
        public ItemStack itemStack;
        public float progress;

        public ItemStacker(Item item, int amount){
            this.itemStack = new ItemStack(item, amount);
            this.progress = 0f;
        }

        public void addProgress(float delta){
            progress += delta;
        }

        public void write(Writes write){
            write.s(itemStack.item.id);
            write.i(itemStack.amount);
            write.f(progress);
        }

        public void read(Reads read){
            Item item = content.item(read.s());
            int amount = read.i();
            float progress = read.f();

            this.itemStack = new ItemStack(item, amount);
            this.progress = progress;
        }
    }
}