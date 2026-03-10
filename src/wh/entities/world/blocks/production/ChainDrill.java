package wh.entities.world.blocks.production;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.production.*;

import static mindustry.Vars.content;

public class ChainDrill extends Drill{
    private static final float networkRefreshInterval = 360f;
    private static final float liquidEpsilon = 0.0001f;
    // 一条链最多包含多少台钻头。
    public int maxChainSize = 10;
    public final int timerNetwork = timers++;
    protected final Seq<ChainDrillBuild> tmpNetwork = new Seq<>();
    protected final Queue<ChainDrillBuild> tmpQueue = new Queue<>();

    public ChainDrill(String name){
        super(name);
    }

    @Override
    public void setBars(){
        super.setBars();
    }

    public class ChainDrillBuild extends DrillBuild{
        // 这条链的根节点，链内物品统一存放在这里。
        public @Nullable ChainDrillBuild chainRoot;
        // 当前链的钻头数量。
        public int chainSize = 1;

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            updateNetwork();
        }

        @Override
        public boolean shouldConsume(){
            ChainDrillBuild root = root();
            return enabled && dominantItem != null && root.items.total() < root.sharedCapacity();
        }

        @Override
        public boolean shouldAmbientSound(){
            ChainDrillBuild root = root();
            return efficiency > 0.01f && root.items.total() < root.sharedCapacity();
        }

        @Override
        public void drawSelect(){
            super.drawSelect();

            ChainDrillBuild root = root();
            for(Building other : proximity){
                if(other instanceof ChainDrillBuild drill && drill.team == team && drill.root() == root && id < drill.id){
                    Drawf.dashLine(Pal.accent, x, y, drill.x, drill.y);
                }
            }

            if(root != this){
                Drawf.dashLine(Pal.place, x, y, root.x, root.y);
            }

            Drawf.selected(root, root == this ? Pal.place : Pal.accent);
        }

        @Override
        public void updateTile(){
            refreshNetwork();
            ChainDrillBuild root = root();

            // 物品走共享仓，液体按当前液体在链内自动均压流动。
            syncItems(root);
            dumpLiquidToChain();
            tryDump(root);

            if(dominantItem == null) return;

            // 钻进逻辑保持原版，主要改的是共享存储与液体流动。
            timeDrilled += warmup * delta();
            float delay = getDrillTime(dominantItem);

            if(!canDrill(root)){
                stopDrilling();
                return;
            }

            float speed = drillSpeed();
            drillStep(speed, delay);
            tryProduce(root, delay);
        }

        protected void refreshNetwork(){
            if(timer(timerNetwork, networkRefreshInterval)){
                updateNetwork();
            }
        }

        protected void syncItems(ChainDrillBuild root){
            if(root == this || items.total() <= 0) return;
            moveLocalItemsTo(root);
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            if(isChainDrill(source)){
                Liquid current = liquids.current();
                return (current == null || current == liquid || liquids.get(current) <= liquidEpsilon) &&
                liquids.get(liquid) < block.liquidCapacity - liquidEpsilon;
            }
            return super.acceptLiquid(source, liquid);
        }

        @Override
        public boolean canDumpLiquid(Building to, Liquid liquid){
            return isChainDrill(to);
        }

        protected void dumpLiquidToChain(){
            Liquid current = liquids.current();
            if(current == null) return;
            dumpLiquid(current);
        }

        protected void tryDump(ChainDrillBuild root){
            if(!timer(timerDump, dumpTime / timeScale)) return;

            Item preferred = dominantItem != null && root.items.has(dominantItem) ? dominantItem : null;
            if(!dumpFromNetwork(root, preferred) && preferred != null){
                dumpFromNetwork(root, null);
            }
        }

        protected boolean canDrill(ChainDrillBuild root){
            return root.items.total() < root.sharedCapacity() && dominantItems > 0 && efficiency > 0;
        }

        protected float drillSpeed(){
            return Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency) * efficiency;
        }

        protected void stopDrilling(){
            lastDrillSpeed = 0f;
            warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
        }

        protected void drillStep(float speed, float delay){
            lastDrillSpeed = (speed * dominantItems * warmup) / delay;
            warmup = Mathf.approachDelta(warmup, speed, warmupSpeed);
            progress += delta() * dominantItems * speed * warmup;

            if(Mathf.chanceDelta(updateEffectChance * warmup)){
                updateEffect.at(x + Mathf.range(size * 2f), y + Mathf.range(size * 2f));
            }
        }

        protected void tryProduce(ChainDrillBuild root, float delay){
            if(dominantItems <= 0 || progress < delay) return;

            int amount = (int)(progress / delay);
            int free = Math.max(0, root.sharedCapacity() - root.items.total());
            int moved = Math.min(amount, free);

            if(moved <= 0) return;

            root.items.add(dominantItem, moved);
            produced(dominantItem, moved);
            progress -= moved * delay;

            if(wasVisible && Mathf.chanceDelta(drillEffectChance * warmup)){
                drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
            }
        }

        protected int sharedCapacity(){
            // Shared item capacity = single drill capacity * chain size.
            return itemCapacity * Math.max(chainSize, 1);
        }

        protected ChainDrillBuild root(){
            // Fall back to self when cached root is invalid.
            if(chainRoot == null || !chainRoot.isValid() || chainRoot.block != block || chainRoot.team != team){
                chainRoot = this;
                chainSize = 1;
            }
            return chainRoot;
        }

        protected void updateNetwork(){
            Seq<ChainDrillBuild> component = tmpNetwork;
            Queue<ChainDrillBuild> queue = tmpQueue;

            component.clear();
            queue.clear();

            collectComponent(component, queue);
            applyNetwork(component);
        }

        protected void collectComponent(Seq<ChainDrillBuild> component, Queue<ChainDrillBuild> queue){
            component.add(this);
            queue.addLast(this);

            while(queue.size > 0){
                ChainDrillBuild next = queue.removeFirst();
                for(Building other : next.proximity){
                    if(!isChainDrill(other)) continue;

                    ChainDrillBuild drill = (ChainDrillBuild)other;
                    if(!component.contains(drill, true)){
                        component.add(drill);
                        queue.addLast(drill);
                    }
                }
            }
        }

        protected void applyNetwork(Seq<ChainDrillBuild> network){
            ChainDrillBuild fallback = network.first();
            ChainDrillBuild output = null;
            // Prefer drill with external output; otherwise pick smallest ID.
            for(int i = 0; i < network.size; i++){
                ChainDrillBuild drill = network.get(i);
                if(drill.id < fallback.id){
                    fallback = drill;
                }
                if(drill.hasExternalOutput() && (output == null || drill.id < output.id)){
                    output = drill;
                }
            }

            ChainDrillBuild root = output != null ? output : fallback;
            int size = maxChainSize > 0 ? Math.min(network.size, maxChainSize) : network.size;
            for(int i = 0; i < network.size; i++){
                ChainDrillBuild drill = network.get(i);
                drill.chainRoot = root;
                drill.chainSize = size;
            }
        }

        protected boolean hasExternalOutput(){
            for(Building other : proximity){
                if(isOutputTarget(other)){
                    return true;
                }
            }
            return false;
        }

        protected boolean isChainDrill(Building other){
            return other instanceof ChainDrillBuild drill && drill.team == team;
        }

        protected boolean isOutputTarget(Building other){
            return other.team == team && !(other instanceof ChainDrillBuild) &&
            (other.block.hasItems || other.block.instantTransfer || other.block.outputsItems());
        }

        protected void moveLocalItemsTo(ChainDrillBuild root){
            Seq<Item> allItems = content.items();
            for(int i = 0; i < allItems.size; i++){
                Item item = allItems.get(i);
                int amount = items.get(item);
                if(amount <= 0) continue;

                int free = Math.max(0, root.sharedCapacity() - root.items.total());
                if(free <= 0) return;

                int moved = Math.min(amount, free);
                root.items.add(item, moved);
                items.remove(item, moved);
            }
        }

        protected boolean dumpFromNetwork(ChainDrillBuild root, @Nullable Item preferred){
            // Dump only to non-chain targets to avoid ping-pong inside the chain.
            if(root.items.total() == 0 || proximity.size == 0 || (preferred != null && !root.items.has(preferred))){
                return false;
            }

            int dump = cdump;
            Seq<Item> allItems = content.items();

            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + dump) % proximity.size);
                if(!isOutputTarget(other)) continue;

                if(preferred != null){
                    if(trySend(root, other, preferred)){
                        incrementDump(proximity.size);
                        return true;
                    }
                }else{
                    for(int j = 0; j < allItems.size; j++){
                        Item item = allItems.get(j);
                        if(!root.items.has(item)) continue;
                        if(trySend(root, other, item)){
                            incrementDump(proximity.size);
                            return true;
                        }
                    }
                }

                incrementDump(proximity.size);
            }

            return false;
        }

        protected boolean trySend(ChainDrillBuild root, Building other, Item item){
            if(!other.acceptItem(this, item)) return false;

            other.handleItem(this, item);
            root.items.remove(item, 1);
            return true;
        }
    }
}




