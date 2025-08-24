package wh.entities.world.blocks.distribution;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.*;
import arc.math.geom.Geometry;
import arc.struct.IntSet;
import arc.struct.Seq;
import arc.util.Eachable;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.*;
import mindustry.Vars;
import mindustry.entities.TargetPriority;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.Autotiler;
import mindustry.world.blocks.distribution.ChainedBuilding;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.heat.HeatConductor.*;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import wh.content.WHBlocks;
import wh.entities.world.blocks.production.FlammabilityHeatProducer;

import java.util.*;

import static mindustry.Vars.*;
import static mindustry.input.Placement.isSidePlace;

public class HeatBelt extends Block implements Autotiler{
    public TextureRegion[] topRegions;
    public TextureRegion[] botRegions;
    public TextureRegion[] heatRegions;
    public TextureRegion capRegion;
    public Color heatColor1 = new Color(1f, 0.3f, 0.3f);
    public Color heatColor2 = Pal.turretHeat;
    public float heatPulse = 0.5f, heatPulseScl = 10f;

    public float visualMaxHeat = 60f;
    public DrawBlock drawer = new DrawDefault();
    public boolean splitHeat = false;
    public float warmupRate = 0.8f;

    public @Nullable Block bridgeReplacement;

    public HeatBelt(String name){
        super(name);

        group = BlockGroup.heat;
        update = true;
        solid = false;
        hasItems = true;
        conveyorPlacement = true;
        noUpdateDisabled = true;
        underBullets = true;
        rotate = true;
        rotateDraw = true;
        isDuct = true;
        priority = TargetPriority.transport;
        envEnabled = Env.space | Env.terrestrial | Env.underwater;
    }

    @Override
    public void load(){
        super.load();
        topRegions = new TextureRegion[5];
        botRegions = new TextureRegion[5];
        heatRegions = new TextureRegion[5];
        for(int i = 0; i < 5; i++){
            topRegions[i] = Core.atlas.find(name + "-top-" + i);
            botRegions[i] = Core.atlas.find(name + "-bottom-" + i);
            heatRegions[i] = Core.atlas.find(name + "-heat-" + i);
        }
        capRegion = Core.atlas.find(name + "-cap");
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("heat", (
        HeatBeltBuilding entity) -> new Bar(() -> Core.bundle.format("bar.heatamount", (int)(entity.heat + 0.001f)), () -> Pal.lightOrange, () -> entity.heat / visualMaxHeat));
    }

    @Override
    public void init(){
        super.init();
        bridgeReplacement = WHBlocks.heatBridge;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        int[] bits = getTiling(plan, list);

        if(bits == null) return;

        Draw.scl(bits[1], bits[2]);
        Draw.alpha(0.5f);
        Draw.rect(botRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.color();
        Draw.rect(topRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.scl();
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        boolean heatRelated = otherblock instanceof HeatProducer
        || otherblock instanceof HeatBelt
        || otherblock instanceof FlammabilityHeatProducer
        || otherblock instanceof HeatDirectionBridge;

        boolean directionMatch = lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock);
        boolean canTransfer = (otherblock.outputsItems() || otherblock.hasLiquids)
        || (lookingAt(tile, rotation, otherx, othery, otherblock) && (otherblock.hasItems || otherblock.hasLiquids));

        return heatRelated && directionMatch && canTransfer;
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{Core.atlas.find("duct-bottom"), topRegions[0]};
    }

    @Override
    public void handlePlacementLine(Seq<BuildPlan> plans){
        if(bridgeReplacement == null) return;
        if(bridgeReplacement instanceof HeatDirectionBridge bridge)
            PlacementHB.calculateBridges(plans, bridge, false, b -> b instanceof HeatDirectionBridge);
    }

    public class HeatBeltBuilding extends Building implements HeatBlock, HeatConsumer, ChainedBuilding{
        public int blendbits, xscl = 1, yscl = 1, blending;
        public boolean capped, backCapped = false;
        public @Nullable Building next;
        public @Nullable HeatBeltBuilding nextc;
        public float heat = 0f;
        public float[] sideHeat = new float[4];
        public IntSet cameFrom = new IntSet();
        public long lastHeatUpdate = -1;

        @Override
        public float[] sideHeat(){
            return sideHeat;
        }

        @Override
        public float heatRequirement(){
            return visualMaxHeat;
        }

        @Override
        public void updateTile(){
            updateHeat();
        }

        public void updateHeat(){
            if(lastHeatUpdate == Vars.state.updateId) return;

            lastHeatUpdate = Vars.state.updateId;
            float cHeat = calculateHeat(sideHeat,cameFrom);
            heat = Mathf.approach(heat, cHeat,
            Interp.smooth.apply(Mathf.approach(0,warmupRate,warmupRate))*2 * delta());
        }

        @Override
        public float heat(){
            return heat;
        }

        @Override
        public float heatFrac(){
            return (heat / visualMaxHeat) / (splitHeat ? 3f : 1);
        }

        @Override
        public void payloadDraw(){
            Draw.rect(fullIcon, x, y);
        }

        @Override
        public void draw(){

            float rotation = rotdeg();
            int r = this.rotation;

            //draw extra ducts facing this one for tiling purposes
            for(int i = 0; i < 4; i++){
                if((blending & (1 << i)) != 0){
                    int dir = r - i;
                    float rot = i == 0 ? rotation : (dir) * 90;
                    drawAt(x + Geometry.d4x(dir) * tilesize * 0.75f, y + Geometry.d4y(dir) * tilesize * 0.75f, 0, rot, i != 0 ? SliceMode.bottom : SliceMode.top);
                }
            }

            Draw.scl(xscl, yscl);
            drawAt(x, y, blendbits, rotation, SliceMode.none);
            Draw.reset();
            Draw.z(Layer.blockUnder - 0.01f);
            if(capped && capRegion.found()) Draw.rect(capRegion, x, y, rotdeg());
            if(backCapped && capRegion.found()) Draw.rect(capRegion, x, y, rotdeg() + 180);

            super.draw();
        }

        protected void drawAt(float x, float y, int bits, float rotation, Autotiler.SliceMode slice){
            Draw.z(Layer.blockUnder + 0.2f);
            Draw.color();
            Draw.rect(sliced(topRegions[bits], slice), x, y, rotation);
            Draw.z(Layer.blockUnder);
            Draw.rect(sliced(botRegions[bits], slice), x, y, rotation);

            Draw.blend(Blending.additive);
            Draw.tint(heatColor1, heatColor2, Mathf.clamp(heatFrac() / 2) * Mathf.absin(Time.time, heatPulse));
            Draw.alpha(Mathf.curve(heat/visualMaxHeat, 0, 1f) * (1f - Mathf.absin(Time.time,  (3*heat/visualMaxHeat+3*heatPulseScl*3)/heatPulseScl , heatPulse)));
            Draw.rect(sliced(heatRegions[bits], slice), x, y, rotation);
            Draw.blend();
            Draw.reset();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(heat);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            heat = read.f();
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();

            int[] bits = buildBlending(tile, rotation, null, true);
            blendbits = bits[0];
            xscl = bits[1];
            yscl = bits[2];
            blending = bits[4];
            next = front();
            nextc = next instanceof HeatBeltBuilding d ? d : null;

            Building next = front(), prev = back();
            capped = next == null || next.team != team;
            backCapped = blendbits == 0 && (prev == null || prev.team != team);

        }

        @Nullable
        @Override
        public Building next(){
            Tile next = tile.nearby(rotation);
            if(next != null && next.build instanceof HeatBeltBuilding){
                return next.build;
            }
            return null;
        }
    }
}

class PlacementHB{

    private static final Seq<BuildPlan> plans1 = new Seq<>();

    public static void calculateBridges(Seq<BuildPlan> plans, HeatDirectionBridge bridge, boolean hasJunction, Boolf<Block> avoid){
        if(isSidePlace(plans) || plans.size == 0) return;

        //check for orthogonal placement + unlocked state
        if(!(plans.first().x == plans.peek().x || plans.first().y == plans.peek().y) || !bridge.unlockedNow()){
            return;
        }

        Boolf<BuildPlan> placeable = plan ->
        (plan.placeable(player.team()) || (plan.tile() != null && plan.tile().block() == plan.block)) &&  //don't count the same block as inaccessible
        !(plan != plans.first() && plan.build() != null && plan.build().rotation != plan.rotation && avoid.get(plan.tile().block()));

        var result = plans1.clear();

        outer:
        for(int i = 0; i < plans.size; ){
            var cur = plans.get(i);
            result.add(cur);

            //gap found
            if(i < plans.size - 1 && placeable.get(cur) && !placeable.get(plans.get(i + 1))){
                boolean wereSame = true;

                //find the closest valid position within range
                for(int j = i + 1; j < plans.size; j++){
                    var other = plans.get(j);

                    //out of range now, set to current position and keep scanning forward for next occurrence
                    if(!bridge.positionsValid(cur.x, cur.y, other.x, other.y)){
                        //add 'missed' conveyors
                        for(int k = i + 1; k < j; k++){
                            result.add(plans.get(k));
                        }
                        i = j;
                        continue outer;
                    }else if(placeable.get(other)){

                        if(wereSame && hasJunction){
                            //the gap is fake, it's just conveyors that can be replaced with junctions
                            i++;
                            continue outer;
                        }else{
                            //found a link, assign bridges
                            cur.block = bridge;
                            other.block = bridge;
                            i = j;
                            continue outer;
                        }
                    }

                    if(other.tile() != null && !avoid.get(other.tile().block())){
                        wereSame = false;
                    }
                }

                //if it got here, that means nothing was found. this likely means there's a bunch of stuff at the end; add it and bail out
                for(int j = i + 1; j < plans.size; j++){
                    result.add(plans.get(j));
                }
                break;
            }else{
                i++;
            }
        }

        plans.set(result);
    }
}

