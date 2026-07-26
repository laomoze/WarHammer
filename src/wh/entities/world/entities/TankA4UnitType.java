package wh.entities.world.entities;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.part.DrawPart;
import mindustry.entities.part.RegionPart;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import wh.content.WHFx;
import wh.content.WHUnitCommands;
import wh.gen.TankA4.TankA4;
import wh.gen.TankA4.TankA4AI;
import wh.gen.TankA4.TankA4GroundAI;

public class TankA4UnitType extends WHTankUnitType {
    public float deployTime = 240;
    public float undeployTime = 300;
    public float deployHoldTime = 60 * 8;
    public float deployHitSizeMultiplier = 1.25f;
    public float deployArmorMultiplier = 2;
    public float deployMassMultiplier = 10000;
    public float deployedMoveMultiplier = 0f;
    public float deployPartShadowAlpha = 1f;
    public Effect deployEffect = WHFx.hitCircle(45, Pal.lightishGray, Pal.gray, 3, 80, 5);
    public Effect deployFinishEffect = WHFx.circleOut(45, 60, 2);
    public Effect undeployEffect = Fx.unitDespawn;
    public Seq<DrawPart> deployParts = new Seq<>(DrawPart.class);

    public TankA4UnitType(String name) {
        super(name);
        constructor = TankA4::create;
        aiController = TankA4GroundAI::new;
        controller = u -> !playerControllable || (u.team.isAI() && !u.team.rules().rtsAi) ? aiController.get() : new TankA4AI();
    }

    @Override
    public void init() {
        super.init();
        commands.add(WHUnitCommands.deploy);
    }

    @Override
    public void load() {
        super.load();
        for (var part : deployParts) {
            part.load(name);
        }
    }

    public void drawDeployParts(Unit unit) {
        if (!(unit instanceof TankA4 tank)) return;
        DrawPart.params.set(tank.deploymentProgress(), 0f, 0f, 0f, 0f, 0f, unit.x, unit.y, unit.rotation);
        for (var part : deployParts) {
            applyColor(unit);
            if (part instanceof RegionPart regionPart) {
                drawDeploySoftShadow(unit, regionPart, DrawPart.params);
            }
            part.draw(DrawPart.params);
        }
    }


    public void getRegionsToOutline(Seq<TextureRegion> out) {
        super.getRegionsToOutline(out);
        for (var part : deployParts) {
            part.getOutlines(out);
        }

    }

    public void drawDeploySoftShadow(Unit unit, RegionPart part, DrawPart.PartParams params) {
        if (!(unit instanceof TankA4 tank)) return;

        float progress = tank.deploymentProgress();
        if (progress <= 0.001f || !drawSoftShadow) return;
        if (!part.drawRegion || part.regions.length == 0) return;

        float z = Draw.z();
        Draw.z(Math.min(Layer.darkness, groundLayer - 1f));

        float prog = part.progress.getClamp(params, part.clampProgress);
        float sclProg = part.growProgress.getClamp(params, part.clampProgress);
        float mx = part.moveX * prog, my = part.moveY * prog;
        float gx = part.growX * sclProg, gy = part.growY * sclProg;

        if (part.moves.size > 0) {
            for (int i = 0; i < part.moves.size; i++) {
                var move = part.moves.get(i);
                float p = move.progress.getClamp(params, part.clampProgress);
                mx += move.x * p;
                my += move.y * p;
                gx += move.gx * p;
                gy += move.gy * p;
            }
        }

        int len = part.mirror && params.sideOverride == -1 ? 2 : 1;
        float preXscl = Draw.xscl, preYscl = Draw.yscl;
        Draw.xscl *= part.xScl + gx;
        Draw.yscl *= part.yScl + gy;
        Draw.color(0f, 0f, 0f, 0.4f * progress * deployPartShadowAlpha);

        for (int s = 0; s < len; s++) {
            int side = params.sideOverride == -1 ? s : params.sideOverride;
            TextureRegion region = part.regions[Math.min(side, part.regions.length - 1)];
            if (!region.found()) continue;

            float sign = (side == 0 ? 1f : -1f) * params.sideMultiplier;
            Tmp.v1.set((part.x + mx) * sign, part.y + my).rotateRadExact((params.rotation - 90f) * Mathf.degRad);
            Draw.xscl *= sign;

            if (part.originX != 0f || part.originY != 0f) {
                Tmp.v1.sub(Tmp.v2.set(-part.originX * Draw.xscl, -part.originY * Draw.yscl).rotate(params.rotation - 90f).add(part.originX * Draw.xscl, part.originY * Draw.yscl));
            }

            float rx = params.x + Tmp.v1.x;
            float ry = params.y + Tmp.v1.y;
            float size = Math.max(region.width, region.height) * region.scl() * softShadowScl;
            Draw.rect(softShadowRegion, rx, ry, size * 1.6f * Math.abs(Draw.xscl), size * 1.6f * Math.abs(Draw.yscl), params.rotation - 90f);

            Draw.xscl *= sign;
        }

        Draw.scl(preXscl, preYscl);
        Draw.color();
        Draw.z(z);
    }

}
