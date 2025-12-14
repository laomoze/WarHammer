package wh.entities.world.blocks.production;

import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.entities.world.blocks.production.OverheatGenericCrafter.*;
import wh.graphics.*;

import static arc.Core.atlas;
import static mindustry.Vars.*;
import static wh.content.WHFx.rand;

public class OverheatBooster extends Block{
    public float boostRange = 16;
    public float reload = 60f;
    public float useTime = 400f;
    public float heatReduceMax = 0.8f;

    public float heatRequirement = 6f;
    public float dymamicHeat = 1f;
    public DrawBlock drawer = new DrawDefault();

    public OverheatBooster(String name){
        super(name);

        hasPower = true;
        hasItems = true;
        solid = true;
        configurable = true;
        saveConfig = update = true;
        flags = EnumSet.of(BlockFlag.factory);

        //point2 is relative
        config(Point2.class, (OverheatBoosterBuild tile, Point2 point) -> tile.link = Point2.pack(point.x + tile.tileX(), point.y + tile.tileY()));
        config(Integer.class, (OverheatBoosterBuild tile, Integer point) -> tile.link = point);

        config(Float.class, (OverheatBoosterBuild build, Float value) -> {
            build.heatReduce = value;
        });
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        Drawf.dashSquare(Pal.accent, x, y, 2*boostRange * tilesize);
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("heat", (OverheatBoosterBuild entity) -> new Bar("bar.heatpercent"+": "+Mathf.round(entity.heatRequirement() * 10) / 10f, Pal.lightOrange, () -> Mathf.clamp(entity.heat / entity.heatRequirement(),0,1)));
    }

    @Override
    public void setStats(){
        stats.timePeriod = useTime;
        super.setStats();

        stats.add(WHStats.heatReduceMax, Core.bundle.format("stat.wh-heat-reduce-max",heatReduceMax),StatUnit.heatUnits);
        stats.add(Stat.range, boostRange, StatUnit.blocks);
        stats.add(Stat.productionTime, useTime / 60f, StatUnit.seconds);

    }

    public Rect getRect(Rect rect, float x, float y, float range){
        rect.setCentered(x, y, range * 2 * tilesize);

        return rect;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        Rect rect = getRect(Tmp.r1, tile.worldx() + offset, tile.worldy() + offset, boostRange).grow(0.1f);
        return !indexer.getFlagged(team, BlockFlag.factory).contains(b -> {
            if(b instanceof OverheatBoosterBuild build) {
                OverheatBooster block = (OverheatBooster) b.block;
                return getRect(Tmp.r2, build.x, build.y, block.boostRange).overlaps(rect);
            }
            return false;
        });
    }

    public class OverheatBoosterBuild extends Building implements Ranged, HeatConsumer{
        public float smoothEfficiency;
        public float warmUp, charge = Mathf.random(reload), useProgress;
        public float[] sideHeat = new float[4];
        public float heat = 0;
        public float heatReduce;

        public int link = -1;

        @Override
        public void updateTile(){
            super.updateTile();
            smoothEfficiency = Mathf.lerpDelta(smoothEfficiency, efficiency, 0.08f);
            warmUp = Mathf.lerpDelta(warmUp, efficiency > 0 ? 1f : 0f, 0.08f);
            charge += warmUp * Time.delta;
            if(charge >= reload){
                float realRange = 2*boostRange * tilesize;
                charge = 0f;
                indexer.eachBlock(this, realRange,
                other -> other instanceof OverheatGenericCrafterBuild && linkBuild(link) != null &&
                other.block.name.equals(linkBuild(link).block.name),
                other -> {
                    ((OverheatGenericCrafterBuild)other).Boost(heatReduce, 60,0.4f);
               /*     Log.info("heatReduce: " + heatReduce);
                    Log.info(other.block.name + " " + linkBuild(link).block.name);*/
                });
            }

            if(efficiency > 0){
                useProgress += delta();
            }

            if(useProgress >= useTime){
                consume();
                useProgress %= useTime;
            }
            heat = calculateHeat(sideHeat);
        }

        @Override
        public void configure(Object value){
            if(value instanceof Float){
                heatReduce = (Float)value;
            }
            super.configure(value);
        }

        @Override
        public void buildConfiguration(Table table){
            super.buildConfiguration(table);
            Table b = new Table();
            b.background(Tex.pane);
            b.defaults().width(250).scaling(Scaling.fit);

            b.slider(0f, heatReduceMax, heatReduceMax / 50, heatReduce, this::configure).width(200f).padTop(4f);
            b.row();
            b.add("wh-heatReduce: " + Mathf.round(heatReduce * 100) / 100f+" tick/s").padTop(4f);

            table.add(b);
        }

        @Override
        public Point2 config(){
            if(tile == null) return null;
            return Point2.unpack(link).sub(tile.x, tile.y);
        }

        @Override
        public boolean onConfigureBuildTapped(Building other){
            if(other != null && within(other, range()*tilesize)){
                configure(other.pos());
                return false;
            }
            return true;
        }

        public Building linkBuild(int p){
            if(p == -1) return null;
            Building other = world.build(p);
            if(other instanceof OverheatGenericCrafterBuild && within(other, range()*tilesize)){
                return other;
            }
            return null;
        }

        private int directionLR(Building b){
            float angle = angleTo(b);
            if(angle > 90 && angle < 270) return -1;
            else return 1;
        }

        private int directionUD(Building b){
            float angle = angleTo(b);
            if(angle > 180 && angle < 360) return -1;
            else return 1;
        }

        @Override
        public void drawConfigure(){
            super.drawConfigure();
            Color baseColor = Pal.slagOrange;

            float offset = size * tilesize / 2f + 1f;

            Lines.stroke(3f, Pal.gray);
            Lines.square(x, y, offset + 1f);

            Building b = linkBuild(link);
            if(b != null){
                float targetOffset = b.block.size * tilesize / 2f + 1f;
                float angle = angleTo(b);
                boolean horizontal = Mathf.equal(angle, 0, 45) || Mathf.equal(angle, 180, 45) || Mathf.equal(angle, 360, 45);

                float edgeX = b.x + Mathf.num(!horizontal) * -directionLR(b) * targetOffset;
                float edgeY = b.y + Mathf.num(horizontal) * -directionUD(b) * targetOffset;

                boolean verticalY = (edgeX >= x - offset && edgeX <= x + offset);
                boolean verticalX = (edgeY >= y - offset && edgeY <= y + offset);

                boolean onSameXAxis = Mathf.zero(b.x - x, 0.1f);
                boolean onSameYAxis = Mathf.zero(b.y - y, 0.1f);

                float fromX, toX, fromY, toY;

                fromX = x + Mathf.num(horizontal) * directionLR(b) * offset;
                if(verticalY || verticalX){
                    toX = b.x + Mathf.num(horizontal) * -directionLR(b) * targetOffset;
                    fromY = y + Mathf.num(!horizontal) * directionUD(b) * offset;
                    toY = b.y + Mathf.num(!horizontal) * -directionUD(b) * targetOffset;
                }else{
                    toX = b.x + Mathf.num(!horizontal) * -directionLR(b) * targetOffset;
                    fromY = y + Mathf.num(!horizontal) * directionUD(b) * offset;
                    toY = b.y + Mathf.num(horizontal) * -directionUD(b) * targetOffset;
                }


                if(verticalY){
                    Tmp.v1.set(fromX, (fromY + toY) / 2);
                    Tmp.v2.set(toX, (fromY + toY) / 2);
                }else if(verticalX){
                    Tmp.v1.set((fromX + toX) / 2, fromY);
                    Tmp.v2.set((fromX + toX) / 2, toY);
                }else{
                    Tmp.v1.set(horizontal ? toX : fromX, !horizontal ? toY : fromY);
                }

                Draw.color(Pal.gray);
                Lines.stroke(3);
                Lines.beginLine();
                Lines.linePoint(fromX, fromY);
                if((verticalX || verticalY) && !onSameXAxis && !onSameYAxis){
                    Lines.linePoint(Tmp.v1.x, Tmp.v1.y);
                    Lines.linePoint(Tmp.v2.x, Tmp.v2.y);
                }else
                    Lines.linePoint(Tmp.v1.x - (horizontal ? 0.15f * (Tmp.v1.x - fromX) : 0), Tmp.v1.y - (!horizontal ? 0.15f * (Tmp.v1.y - fromY) : 0));
                Lines.linePoint(toX, toY);
                Lines.endLine();
                Lines.stroke(3);
                Lines.square(b.x, b.y, b.block.size * tilesize / 2f + 2.0f);

                Draw.reset();
                Draw.color(Pal.gray);
                float ang;
                if(verticalX || verticalY){
                    ang = Angles.angle(Tmp.v2.x, Tmp.v2.y, toX, toY);
                }else ang = Angles.angle(Tmp.v1.x, Tmp.v1.y, toX, toY);
                Draw.rect(atlas.find("logic-node"), toX, toY, 12 / 2f, 12 / 2f, ang);
            }

            if(b != null){
                float targetOffset = b.block.size * tilesize / 2f + 1f;
                float angle = angleTo(b);
                boolean horizontal = Mathf.equal(angle, 0, 45) || Mathf.equal(angle, 180, 45) || Mathf.equal(angle, 360, 45);

                float edgeX = b.x + Mathf.num(!horizontal) * -directionLR(b) * targetOffset;
                float edgeY = b.y + Mathf.num(horizontal) * -directionUD(b) * targetOffset;

                boolean verticalY = (edgeX >= x - offset && edgeX <= x + offset);
                boolean verticalX = (edgeY >= y - offset && edgeY <= y + offset);

                boolean onSameXAxis = Mathf.zero(b.x - x, 0.1f);
                boolean onSameYAxis = Mathf.zero(b.y - y, 0.1f);

                float fromX, toX, fromY, toY;

                fromX = x + Mathf.num(horizontal) * directionLR(b) * offset;
                if(verticalY || verticalX){
                    toX = b.x + Mathf.num(horizontal) * -directionLR(b) * targetOffset;
                    fromY = y + Mathf.num(!horizontal) * directionUD(b) * offset;
                    toY = b.y + Mathf.num(!horizontal) * -directionUD(b) * targetOffset;
                }else{
                    toX = b.x + Mathf.num(!horizontal) * -directionLR(b) * targetOffset;
                    fromY = y + Mathf.num(!horizontal) * directionUD(b) * offset;
                    toY = b.y + Mathf.num(horizontal) * -directionUD(b) * targetOffset;
                }

                if(verticalY){
                    Tmp.v1.set(fromX, (fromY + toY) / 2);
                    Tmp.v2.set(toX, (fromY + toY) / 2);
                }else if(verticalX){
                    Tmp.v1.set((fromX + toX) / 2, fromY);
                    Tmp.v2.set((fromX + toX) / 2, toY);
                }else{
                    Tmp.v1.set(horizontal ? toX : fromX, !horizontal ? toY : fromY);
                }

                Draw.color(baseColor);
                Lines.stroke(1);
                Lines.beginLine();
                Lines.linePoint(fromX, fromY);

                if((verticalX || verticalY) && !onSameXAxis && !onSameYAxis){
                    Lines.linePoint(Tmp.v1.x, Tmp.v1.y);
                    Lines.linePoint(Tmp.v2.x, Tmp.v2.y);
                }else
                    Lines.linePoint(Tmp.v1.x - (horizontal ? 0.15f * (Tmp.v1.x - fromX) : 0), Tmp.v1.y - (!horizontal ? 0.15f * (Tmp.v1.y - fromY) : 0));

                Lines.linePoint(toX, toY);
                Lines.endLine();
                Lines.stroke(1);
                Lines.square(b.x, b.y, b.block.size * tilesize / 2f + 2.0f);
                Draw.reset();
                Draw.color(baseColor);
                float ang;
                if(verticalX || verticalY){
                    ang = Angles.angle(Tmp.v2.x, Tmp.v2.y, toX, toY);
                }else ang = Angles.angle(Tmp.v1.x, Tmp.v1.y, toX, toY);
                Draw.rect(atlas.find("logic-node"), toX, toY, 12 / 2f, 12 / 2f, ang);

            }
            Lines.stroke(1f, baseColor);
            Lines.square(x, y, offset);
            Drawf.dashSquare(Pal.accent, x, y, 2*boostRange * tilesize);
        }

        public int particles = 15;
        public float particleSize = 1.3f;
        public float particleLen = 10f;
        public float rotateScl = 3f;
        public float particleLife = 110f;
        public float orbRadius = 8, orbMidScl = 0.33f, orbSinScl = 8f, orbSinMag = 1f;
        public Interp particleInterp = f -> Interp.circleOut.apply(Interp.slope.apply(f));
        public Color particleColor = Pal.lightOrange;
        @Override
        public void draw(){
            super.draw();
            Color color = particleColor;

            Draw.z(Layer.effect);

            float rad = orbRadius+heatReduce*2 + Mathf.absin(orbSinScl, orbSinMag);
            Tmp.v1.set(x, y);
            float rx = Tmp.v1.x, ry = Tmp.v1.y;

            float base = (Time.time / particleLife);
            Rand rand=new Rand();
            rand.setSeed(id + hashCode());
            Draw.color(particleColor);
            for(int i = 0; i < particles; i++){
                float fin = (rand.random(1f) + base) % 1f, fout = 1f - fin;
                float angle = rand.random(360f) + (Time.time / rotateScl + this.rotation) % 360f;
                float len = particleLen * particleInterp.apply(fout)*warmUp;
                Fill.circle(
                rx + Angles.trnsx(angle, len),
                ry + Angles.trnsy(angle, len),
                (particleSize+heatReduce*2) * Mathf.slope(fin)*warmUp
                );
            }

            Lines.stroke(2f*warmUp);

            Draw.color(color);
            Lines.circle(rx, ry, rad*warmUp);

            Draw.color(color);
            Fill.circle(rx, ry, rad * orbMidScl*warmUp);

            Draw.reset();
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            Color baseColor = Pal.accent;
            Drawf.dashSquare(Pal.accent, x, y, 2*boostRange * tilesize);
            indexer.eachBlock(this, 2*boostRange * tilesize,
            other -> other instanceof OverheatGenericCrafterBuild,
            other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
        }

        @Override
        public float efficiencyScale(){
            return Mathf.clamp(heat / heatRequirement, 0, 1);
        }

        @Override
        public float heatRequirement(){
            return heatRequirement +Interp.linear.apply( heatReduce)*dymamicHeat*heatRequirement;
        }

        @Override
        public float[] sideHeat(){
            return sideHeat;
        }

        @Override
        public float range(){
            return boostRange;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(warmUp);
            write.f(heat);
            write.f(charge);
            write.f(useProgress);
            write.f(heatReduce);
            write.i(link);

        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            warmUp = read.f();
            heat = read.f();
            charge = read.f();
            useProgress = read.f();
            heatReduce = read.f();
            link = read.i();
        }

    }
}