package wh.entities.world.blocks.storage;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;
import wh.content.*;
import wh.graphics.*;

import static mindustry.Vars.state;

public class FrontlineCoreBlock extends CoreBlock{
    public int max = 6;
    public boolean killed = true;
    public float killTime = 600f;

    public String showStr = "Core overload";

    public FrontlineCoreBlock(String name){
        super(name);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("team-cores", (FrontlineCoreBuild entity) -> new Bar(
        () -> Core.bundle.get("bar.wh-amount") + state.teams.cores(entity.team).size + " / " + max,
        () -> Pal.accent,
        () -> Math.min(1f, (float)state.teams.cores(entity.team).size / Math.max(max, 1))
        ));
    }

    @Override
    public boolean canBreak(Tile tile){
        return state.teams.cores(tile.team()).size > 1;
    }

    @Override
    public boolean canReplace(Block other){
        return other.alwaysReplace;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return state.teams.cores(team).size < max;
    }

    public class FrontlineCoreBuild extends CoreBuild{
        public boolean kill = false;
        public float time = 0, alpha;

        @Override
        public void update(){
            super.update();
            if(killed){
                if(state.teams.cores(team).size > (max + 2)) kill = true;
                if(kill){
                   /* if(!headless){
                        ui.showLabel(showStr, 0.015f, x, y);
                    }*/
                    time += Time.delta;
                    if(time > killTime){
                        kill();
                    }
                }
            }
            alpha = Mathf.approachDelta(alpha, kill ? 1 : 0, 0.07f);
        }

        @Override
        public void draw(){
            super.draw();
            if(killed){
                Draw.z(WHFx.EFFECT_MASK);
                Draw.color(team.color.cpy(), Pal.remove, time / killTime);
                Lines.stroke(2 * alpha);
                Draw.alpha(alpha);
                Drawn.circlePercent(x, y, hitSize() * 0.52f, time / killTime, 0);
                Drawn.overlayText(Fonts.tech, String.valueOf(Mathf.ceil((killTime - time) / 60f)), x, y, 0, 0, 0.25f, team.color, false, true);
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(kill);
            write.f(time);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(revision >= 1){
                kill = read.bool();
                time = read.f();
            }
        }
    }
}
