package wh.entities.world.blocks.others;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.g2d.TextureRegion;
import mindustry.graphics.MultiPacker;
import mindustry.world.blocks.environment.ShallowLiquid;

public class WHShallowLiquid extends ShallowLiquid {

    public WHShallowLiquid(String name) {
        super(name);
    }

    @Override
    public void createIcons(MultiPacker packer) {
        if (blendGroup != this) {
            return;
        }

        if (liquidBase == null || floorBase == null) {
            return;
        }

        var overlay = Core.atlas.getPixmap(liquidBase.region);
        Pixmap firstProcessed = null;
        int index = 0;

        for (TextureRegion region : floorBase.variantRegions()) {
            var res = Core.atlas.getPixmap(region).crop();
            for (int x = 0; x < res.width; x++) {
                for (int y = 0; y < res.height; y++) {
                    res.setRaw(x, y, Pixmap.blend((overlay.getRaw(x, y) & 0xffffff00) | (int) (liquidOpacity * 255), res.getRaw(x, y)));
                }
            }

            String baseName = this.name + (++index);
            packer.add(MultiPacker.PageType.environment, baseName, res);

            if (firstProcessed == null) {
                firstProcessed = res.crop(0, 0, res.width, res.height);
            }

            res.dispose();
        }

        if (firstProcessed != null && !Core.atlas.has(name + "-edge")) {
            var edge = Core.atlas.getPixmap(Core.atlas.find(name + "-edge-stencil", "edge-stencil"));
            Pixmap result = new Pixmap(edge.width, edge.height);

            for (int x = 0; x < edge.width; x++) {
                for (int y = 0; y < edge.height; y++) {
                    result.set(x, y, Color.muli(edge.get(x, y), firstProcessed.get(x % firstProcessed.width, y % firstProcessed.height)));
                }
            }

            packer.add(MultiPacker.PageType.environment, name + "-edge", result);
            result.dispose();
        }

        if (firstProcessed != null) {
            firstProcessed.dispose();
        }
    }
}
