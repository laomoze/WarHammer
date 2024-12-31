package wh.world.blocks.storage;

import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;

import static mindustry.Vars.*;

public class FrontlineCoreBlock extends CoreBlock {
    public int max = 7;
    public boolean killed = false;

    public String showStr = "oh no";
    public Color showCol = Color.valueOf("FF5B5B");

    public FrontlineCoreBlock(String name) {
        super(name);
    }

    @Override
    public boolean canBreak(Tile tile) {
        return state.teams.cores(tile.team()).size > 1;
    }

    @Override
    public boolean canReplace(Block other) {
        return other.alwaysReplace;
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        return state.teams.cores(team).size < max;
    }

    public class FrontlineCoreBuild extends CoreBuild {
        public boolean kill = false;
        public float num = 1, time = 60 * num;

        @Override
        public void update() {
            super.update();
            if (killed) {
                if (state.teams.cores(team).size > (max + 2)) kill = true;
                if (kill) {
                    if (!headless) {
                        ui.showLabel(showStr, 0.015f, x, y);
                    }
                    time--;
                    if (time == 0) {
                        kill();
                    }
                }
            }
        }

        @Override
        public void draw() {
            super.draw();
            if (killed) {
                Draw.z(Layer.effect);
                Lines.stroke(2, showCol);
                Draw.alpha(kill ? 1 : state.teams.cores(team).size > (max + 1) ? 1 : 0);
                Lines.arc(x, y, 16, time * (6 / num) / 360, 90);
            }
        }
    }
}
