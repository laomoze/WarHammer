package wh.maps.filters;

import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.func.Prov;
import arc.input.KeyCode;
import arc.scene.ui.Button;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.maps.filters.FilterOption;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;

import static mindustry.Vars.*;

public class RuinFilterUI {

    public static void showBlockPicker(String title, Prov<Block> supplier, Cons<Block> consumer, Boolf<Block> filter, Runnable changed) {
        BaseDialog dialog = new BaseDialog(title);
        dialog.cont.pane(t -> {
            int i = 0;
            for (Block block : content.blocks()) {
                if (!filter.get(block)) continue;

                t.image(block == Blocks.air ? Icon.none.getRegion() : block.uiIcon).size(iconMed).pad(3).tooltip(block == Blocks.air ? "@none" : block.localizedName).get().clicked(() -> {
                    consumer.get(block);
                    dialog.hide();
                    changed.run();
                });
                if (++i % 10 == 0) t.row();
            }
            dialog.setFillParent(i > 100);
        }).scrollX(false);

        dialog.addCloseButton();
        dialog.show();
    }

    public static void bindBlockButton(Button button, Prov<Block> supplier, Cons<Block> consumer, Boolf<Block> filter, Runnable changed) {
        button.clicked(KeyCode.mouseMiddle, () -> {
            Block block = supplier.get();
            if (block != null && block != Blocks.air) {
                Core.app.setClipboardText(block.name);
                ui.showInfoFade("@copied");
            }
        });

        button.clicked(KeyCode.mouseRight, () -> {
            Block block = content.block(Core.app.getClipboardText());
            if (block != null && filter.get(block)) {
                consumer.get(block);
                changed.run();
            }
        });
    }

    public static void showFloorPicker(RuinStep step, Runnable changed) {
        showBlockPicker("@filter.option.floor", () -> floorDisplay(step), block -> {
            if (block == Blocks.removeWall) {
                step.floor = Blocks.removeWall;
            } else if (block == Blocks.air) {
                step.floor = null;
            } else {
                step.floor = block;
            }
            changed.run();
        }, b -> b == Blocks.removeWall || FilterOption.floorsOptional.get(b), changed);
    }

    public static void showWallPicker(RuinStep step, Runnable changed) {
        showBlockPicker("@filter.option.wall", () -> {
            if (step.removesWall()) return Blocks.removeWall;
            return step.wall == null ? Blocks.air : step.wall;
        }, block -> {
            if (block == Blocks.removeWall) {
                step.removeWall = true;
                step.wall = null;
            } else if (block == Blocks.air) {
                step.removeWall = false;
                step.wall = null;
            } else {
                step.removeWall = false;
                step.wall = block;
            }
            changed.run();
        }, b -> b == Blocks.removeWall || FilterOption.wallsOptional.get(b), changed);
    }

    public static Block floorDisplay(RuinStep step) {
        if (step.preservesFloor()) return Blocks.removeWall;
        if (step.floor == null) return Blocks.air;
        return step.floor;
    }

    public static Block wallDisplay(RuinStep step) {
        if (step.removesWall()) return Blocks.removeWall;
        if (step.wall == null) return Blocks.air;
        return step.wall;
    }
}

