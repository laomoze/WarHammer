package wh.entities.world.drawer;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.DrawBlock;
import wh.entities.WHBaseEntity;
import wh.util.*;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

public class AdmantiumMillDrawer extends DrawBlock {
    public Color color;
    public float move;
    public AdmantiumMillDrawer(Color color, float move){
        this.color = color;
        this.move = move;
    }
    @Override
    public void draw(Building build) {
        GenericCrafter block = (GenericCrafter) build.block;
        GenericCrafter.GenericCrafterBuild b = (GenericCrafter.GenericCrafterBuild) build;
        realDraw(block, b);
    }

    public void realDraw(GenericCrafter block, GenericCrafter.GenericCrafterBuild build) {
        float bx = build.x, by = build.y;


        Draw.alpha(build.progress * 1.2f);
        float sin = Mathf.sin(build.totalProgress, 30f / Math.max(build.efficiency, 0.00001f), 6f);
        Lines.stroke(build.warmup);
        Draw.color(color);
        //Draw.z(Layer.effect);
        for (int i = -1; i <= 1; i++) {
            if (i == 0) continue;
            Lines.lineAngleCenter(bx + i * sin, by, 90, 7);
            Lines.lineAngleCenter(bx, by + i * sin, 0, 7);
        }
        if (move == 0) move = block.size / 2f * tilesize;
        Lines.stroke(1f);
        if (build.efficiency > 0) {
            if (build.wasVisible && !state.isPaused() && Mathf.chance(block.updateEffectChance)) {
                float range = block.size * tilesize / 2f;
                float x1 = bx + Mathf.random(-range, range),
                        x2 = bx + Mathf.random(-range, range),
                        y1 = by + Mathf.random(-range, range),
                        y2 = by + Mathf.random(-range, range);
                Fx.chainLightning.at(x1, y1, 0, color, WHUtils.pos(x2, y2));
            }
        }
        Draw.color();
    }
    }