package wh.entities.world.blocks.production;

import arc.*;
import arc.graphics.Color;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.production.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.entities.world.drawer.*;

import static mindustry.Vars.*;

public class OverheatGenericCrafter extends GenericCrafter{
    // 热量参数
    public float heatCapacity = 1000f;
    public float baseHeatProduction = 0.8f;
    public float overloadHeatProduction = 0.6f;
    public float nearbyHeatProduction = 0.3f;
    public float baseHeatLoss = 2.5f;
    public float overloadThreshold = 0.5f;
    public float overloadEfficiency = 0.3f;

    public int proximityRange = size;

    public float smokeEffectChance = 0.05f;
    public Effect smokeEffect = Fx.fuelburn;

    public float warmupRate = 0.08f;
    public float wasteHeatOutput = 0.5f;

    public OverheatGenericCrafter(String name){
        super(name);
        drawArrow = true;
        rotate = true;
        rotateDraw = false;
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("wh-overheat", (OverheatGenericCrafterBuild entity) -> new Bar("wh-overheat", Pal.slagOrange, () -> entity.overHeat / heatCapacity));
        addBar("heat", (OverheatGenericCrafterBuild entity) -> new Bar("bar.heat", Pal.lightOrange, () -> entity.heat / wasteHeatOutput));
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.heatCapacity, heatCapacity, StatUnit.heatUnits);
        stats.add(Stat.output, wasteHeatOutput, StatUnit.heatUnits);
        stats.add(WHStats.baseHeatProduction,   baseHeatProduction + Core.bundle.get("stat.wh-tick"));
        stats.add(WHStats.overloadHeatProduction,  + overloadHeatProduction + Core.bundle.get("stat.wh-tick") );
        stats.add(WHStats.heatLoss,  baseHeatLoss+ Core.bundle.get("stat.wh-tick"));
        stats.add(WHStats.overloadThreshold, Mathf.round(1+overloadThreshold* 100) , StatUnit.percent);
        stats.add(WHStats.overloadEfficiency, (1 + overloadEfficiency)*100, StatUnit.percent);

    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        Color baseColor = Pal.lightOrange;
        x *= tilesize;
        y *= tilesize;
        x += (int)offset;
        y += (int)offset;

        float range = ((proximityRange * 2 + size) * 0.9f) * tilesize;

        Drawf.dashSquare(baseColor, x, y, range);
        indexer.eachBlock(player.team(), Tmp.r1.setCentered(x, y, range), b -> true,
        t -> Drawf.selected(t, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
    }


    @Override
    public void init(){
        super.init();
    }

    public class OverheatGenericCrafterBuild extends GenericCrafterBuild implements HeatBlock{
        public float heat;
        public float overHeat = 0f;
        public float overHeatReduce;
        public boolean overloaded = false;
        public boolean cooling = false;
        private int nearbySameType;
        private int lastChange = -2;
        protected transient float boostDuration;
        protected transient float overloadThresholdReduce;

        @Override
        public void draw(){
            super.draw();
        }

        public void Boost(float overHeatReduce, float boostDuration, float overloadThresholdReduce){
            this.boostDuration = boostDuration;
            this.overHeatReduce = overHeatReduce;
            this.overloadThresholdReduce = overloadThresholdReduce;
        }

        public void updateTargets(){
            nearbySameType = 0;
            float range = ((proximityRange * 2 + size) * 0.9f) * tilesize;
            indexer.eachBlock(player.team(), Tmp.r1.setCentered(x, y, range), b -> true,
            t -> {
                if(t instanceof OverheatGenericCrafterBuild &&
                ((OverheatGenericCrafterBuild)t).overHeat > 0){
                    nearbySameType++;
                }
            });
        }

        public float  charge;
        @Override
        public void updateTile(){
            if(boostDuration > 0){
                boostDuration -= Time.delta;
            }else{
                overHeatReduce = 0;
                boostDuration = 0;
                overloadThresholdReduce = 0;
            }

            float actualHeatProduction = baseHeatProduction * (1 + nearbyHeatProduction * nearbySameType);
            float heatReduce = (boostDuration > 0 ? overHeatReduce * edelta() : 0f);
            if(!cooling){
                overHeat += actualHeatProduction * edelta() - heatReduce;
                super.updateTile();
            }

            if(lastChange != world.tileChanges){
                lastChange = world.tileChanges;
                updateTargets();
            }

            if(overHeat > heatCapacity) cooling = true;
            overHeat = Mathf.clamp(overHeat, 0, heatCapacity);
            boolean isOverloaded = overHeat > (overloadThreshold - (boostDuration > 0 ? overloadThresholdReduce : 0)) * heatCapacity;


            if( isOverloaded && !cooling){
                overloaded = true;
                applyBoost(1 + overloadEfficiency, 60f);
                overHeat += overloadHeatProduction * edelta();
            }

           /* charge += Time.delta;
            if(charge >= 20f){
                charge = 0f;
                Log.info("boostDuration  " + boostDuration);
                Log.info("overloadThresholdReduce  " + overloadThresholdReduce);
                Log.info("isOverloaded  "+isOverloaded);
                Log.info("cooling " + cooling);
            }*/

            if(isOverloaded || cooling){
                heat = Mathf.approachDelta(heat, wasteHeatOutput * efficiency, warmupRate * edelta());
            }else{
                heat = Mathf.approachDelta(heat, 0, warmupRate*1.5f * edelta());
            }

            if(cooling){
                if(wasVisible && Mathf.chanceDelta(smokeEffectChance)){
                    smokeEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread));
                }
                overHeat -= baseHeatLoss * edelta();
                if(overHeat < 0){
                    overloaded = false;
                    cooling = false;
                }
            }

        }

        @Override
        public float heat(){
            return heat;
        }

        @Override
        public float heatFrac(){
            float progress = overHeat / heatCapacity;
            return Interp.smoother.apply(Interp.slope.apply(progress));
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            float realRange = ((proximityRange * 2 + size) * 0.9f) * tilesize;
            Color baseColor = Pal.lightOrange;
            Drawf.dashSquare(baseColor, x, y, realRange);
            indexer.eachBlock(player.team(), Tmp.r1.setCentered(x, y, realRange), b -> b instanceof OverheatGenericCrafterBuild,
            t -> Drawf.selected(t, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
        }


        @Override
        public void write(Writes write){
            super.write(write);
            write.f(boostDuration);
            write.f(heat);
            write.f(overHeat);
            write.f(overHeatReduce);
            write.bool(overloaded);
            write.bool(cooling);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            boostDuration = read.f();
            heat = read.f();
            overHeat = read.f();
            overHeatReduce = read.f();
            overloaded = read.bool();
            cooling = read.bool();
        }
    }
}