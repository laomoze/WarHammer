package wh.entities.world.blocks;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

import static mindustry.Vars.state;

public class Road extends Block{
    protected static final Point2[] neighbors8 = Geometry.d8;
    protected TextureRegion[] autotileRegions;

    public Road(String name){
        super(name);
        size = 4;
        breakable = true;
        solid = false;
        underBullets = true;
        squareSprite = false;
        canPickup = false;
    }

    @Override
    public boolean hasBuilding(){
        return true;
    }

    @Override
    public void load(){
        super.load();
        autotileRegions = TileBitmask.load(name);
    }

    @Override
    public boolean canBreak(Tile tile){
        return state != null && state.rules != null && state.rules.editor;
    }

    public class RoadBuild extends Building{
        private int cachedRegionIndex;
        private boolean regionCached;

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            cachedRegionIndex = calcRegionIndex();
            regionCached = true;
        }

        @Override
        public void draw(){
            if(!regionCached){
                cachedRegionIndex = calcRegionIndex();
                regionCached = true;
            }
            Draw.rect(autotileRegions[cachedRegionIndex], x, y);
        }

        private int calcRegionIndex(){
            int bits = 0;

            for(int i = 0; i < 8; i++){
                Point2 near = neighbors8[i];
                Building other = nearby(near.x * block.size, near.y * block.size);
                if(other != null && other != this && other.block == block){
                    bits |= (1 << i);
                }
            }

            return TileBitmask.values[bits];
        }

        @Override
        public void damage(float damage){
        }
    }
}
