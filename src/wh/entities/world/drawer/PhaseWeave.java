package wh.entities.world.drawer;

import arc.graphics.Blending;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.draw.DrawBlock;


public class PhaseWeave extends DrawBlock {
    public float moveScale = 10f;
    public float moveScaleRand = 3f;
    public Blending blending = Blending.additive;
    @Override
    public void draw(Building build) {

            Draw.color(Pal.accent);
            Draw.blend(blending);

            Draw.alpha((0.5f + Mathf.absin(8f, 0.3f) ));

            float moveX, moveY;
            moveX = build.x + Mathf.sin(build.totalProgress() * 0.3f, 6 + Mathf.randomSeed(build.id, 1, moveScaleRand), Vars.tilesize / 6f * build.block.size);
            moveY = build.y + Mathf.sin(build.totalProgress() * 0.3f + Mathf.randomSeed(build.id >> 1, moveScale), 6 + Mathf.randomSeed(build.id, 1, moveScaleRand), Vars.tilesize / 6f * build.block.size);

            Lines.beginLine();
            Lines.lineAngleCenter(
                    build.x + Mathf.sin(build.totalProgress() * 0.7f, 10f, Vars.tilesize * build.block.size / 3f),
                    build.y, 90, Vars.tilesize * build.block.size * 0.9f);

            Lines.lineAngleCenter(build.x, moveY, build.rotdeg(), build.block.size * Vars.tilesize * 0.9f);
            Lines.lineAngleCenter(build.x, moveY + 7f * Mathf.sin(build.totalProgress() * 0.3f, 6, 1), build.rotdeg(), build.block.size * Vars.tilesize * 0.9f);


            Lines.lineAngleCenter(moveX, build.y, build.rotdeg() + 90, build.block.size * Vars.tilesize * 0.9f);
            Lines.lineAngleCenter(moveX + 7f * Mathf.sin(build.totalProgress() * 0.3f, 6, 1), build.y, build.rotdeg() + 90, build.block.size * Vars.tilesize * 0.9f);

            Lines.endLine(true);

            Draw.blend();
            Draw.reset();
        }
    }
