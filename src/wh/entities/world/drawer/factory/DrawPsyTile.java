package wh.entities.world.drawer.factory;

import arc.graphics.Color;
import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.draw.DrawBlock;
import wh.entities.world.Psy.*;
import wh.util.WHUtils;

public class DrawPsyTile extends DrawBlock {
    public Liquid placeholderLiquid = Liquids.cyanogen;
    public Color drawColor = Color.white.cpy();
    public float fill = 1f;
    public boolean usePsychicAmount = true;
    public float padding;
    public float padLeft = -1f, padRight = -1f, padTop = -1f, padBottom = -1f;
    public float alpha = 1f;

    public DrawPsyTile(Color drawColor, float padding) {
        this.drawColor = drawColor;
        this.padding = padding;
    }

    public DrawPsyTile(Liquid placeholderLiquid, Color drawColor, float padding) {
        this.placeholderLiquid = placeholderLiquid;
        this.drawColor = drawColor;
        this.padding = padding;
    }

    public DrawPsyTile(Color drawColor) {
        this.drawColor = drawColor;
    }

    public DrawPsyTile(Liquid placeholderLiquid, Color drawColor) {
        this.placeholderLiquid = placeholderLiquid;
        this.drawColor = drawColor;
    }

    public DrawPsyTile() {
    }

    @Override
    public void draw(Building build) {
        Liquid drawn = placeholderLiquid != null ? placeholderLiquid : build.liquids.current();
        if (drawn == null) return;

        float amount = usePsychicAmount ? psychicFill(build) * alpha : fill * alpha;

        if (amount <= 0.001f) return;

        WHUtils.drawTiledFramesGas(build.block.size, build.x, build.y, padLeft, padRight, padTop, padBottom, drawn, drawColor, amount);
    }

    protected float psychicFill(Building build) {
        if (build instanceof PsychicBlock.PsychicBuild psy) {
            return psy.psychicFraction();
        }

        if (build instanceof PsychicRegenProjectorBlock.PsychicRegenProjectorBuild psy) {
            return psy.psychicFraction();
        }

        if (build instanceof PsychicMultiCrafterBlock.PsychicMultiCrafterBuild psy) {
            return psy.psychicFraction();
        }

        if (build instanceof PsychicUnitFactory.PsychicUnitFactoryBuild psy) {
            return psy.psychicFraction();
        }

        if (build instanceof PsychicRepairTowerBlock.PsychicRepairTowerBuild psy) {
            float capacity = ((PsychicRepairTowerBlock) psy.block).psychicCapacity;
            return capacity <= PsychicNetworkNode.epsilon ? 0f : psy.psychic.amount() / capacity;
        }

        return fill;
    }

    @Override
    public void load(Block block) {
        if (padLeft < 0f) padLeft = padding;
        if (padRight < 0f) padRight = padding;
        if (padTop < 0f) padTop = padding;
        if (padBottom < 0f) padBottom = padding;
    }
}
