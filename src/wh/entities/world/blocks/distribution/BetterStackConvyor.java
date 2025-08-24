package wh.entities.world.blocks.distribution;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;

import static mindustry.Vars.*;

public class BetterStackConvyor extends StackConveyor{
    public TextureRegion edge2Region;
    public BetterStackConvyor(String name){
        super(name);
    }
    @Override
    public void load() {
        super.load();
        edge2Region = Core.atlas.find(name + "-edge-2");
    }


  public class BetterStackConvyorBuilding extends StackConveyorBuild{
      @Override
      public void draw(){
          Draw.z(Layer.block - 0.2f);

          Draw.rect(regions[state], x, y, rotdeg());

          for (int i = 0; i < 4; i++) {
              if ((blendprox & (1 << i)) == 0) {
                  int rot = Mathf.mod(rotation - i, 4);
                  if (rot <= 1) Draw.rect(edge2Region, x, y, rot * 90);
                  else Draw.rect(edgeRegion, x, y, rot * 90);
              }
          }

          //draw inputs
          if(state == stateLoad){
              for(int i = 0; i < 4; i++){
                  int dir = rotation - i;
                  var near = nearby(dir);
                  if((blendprox & (1 << i)) != 0 && i != 0 && near != null && !near.block.squareSprite){
                      Draw.rect(sliced(regions[0], SliceMode.bottom), x + Geometry.d4x(dir) * tilesize*0.75f, y + Geometry.d4y(dir) * tilesize*0.75f, (float)(dir*90));
                  }
              }
          }else if(state == stateUnload){ //front unload
              //TOOD hacky front check
              if((blendprox & (1)) != 0 && !front().block.squareSprite){
                  Draw.rect(sliced(regions[0], SliceMode.top), x + Geometry.d4x(rotation) * tilesize*0.75f, y + Geometry.d4y(rotation) * tilesize*0.75f, rotation * 90f);
              }
          }

          Draw.z(Layer.block - 0.1f);

          Tile from = world.tile(link);

          //TODO do not draw for certain configurations?
          if(glowRegion.found() && power != null && power.status > 0f){
              Draw.z(Layer.blockAdditive);
              Draw.color(glowColor, glowAlpha * power.status);
              Draw.blend(Blending.additive);
              Draw.rect(state == stateLoad ? edgeGlowRegion : glowRegion, x, y, rotation * 90);
              Draw.blend();
              Draw.color();
              Draw.z(Layer.block - 0.1f);
          }

          if(link == -1 || from == null || lastItem == null) return;

          int fromRot = from.build == null ? rotation : from.build.rotation;

          //offset
          Tmp.v1.set(from.worldx(), from.worldy());
          Tmp.v2.set(x, y);
          Tmp.v1.interpolate(Tmp.v2, 1f - cooldown, Interp.linear);

          //rotation
          float a = (fromRot%4) * 90;
          float b = (rotation%4) * 90;
          if((fromRot%4) == 3 && (rotation%4) == 0) a = -1 * 90;
          if((fromRot%4) == 0 && (rotation%4) == 3) a =  4 * 90;

          if(glowRegion.found()){
              Draw.z(Layer.blockAdditive + 0.01f);
          }

          //stack
          Draw.rect(stackRegion, Tmp.v1.x, Tmp.v1.y, Mathf.lerp(a, b, Interp.smooth.apply(1f - Mathf.clamp(cooldown * 2, 0f, 1f))));

          //item
          float size = itemSize * Mathf.lerp(Math.min((float)items.total() / itemCapacity, 1), 1f, 0.4f);
          Drawf.shadow(Tmp.v1.x, Tmp.v1.y, size * 1.2f);
          Draw.rect(lastItem.fullIcon, Tmp.v1.x, Tmp.v1.y, size, size, 0);
      }
  }

}
