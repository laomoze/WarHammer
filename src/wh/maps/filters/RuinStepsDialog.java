package wh.maps.filters;

import arc.Core;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.maps.filters.FilterOption;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;

import static mindustry.Vars.iconSmall;

public class RuinStepsDialog extends BaseDialog {
    private final RuinGenerateFilter filter;
    private final Runnable changed;
    private final Seq<RuinStep> editing = new Seq<>();
    private Table list;

    public RuinStepsDialog(RuinGenerateFilter filter, Runnable changed) {
        super("@nh.filter.steps.title");
        this.filter = filter;
        this.changed = changed;

        if (filter.steps != null) {
            for (RuinStep step : filter.steps) {
                editing.add(step.copy());
            }
        }

        cont.pane(p -> p.table(t -> {
            list = t;
            rebuildList();
        })).grow();

        buttons.defaults().size(200f, 54f);
        buttons.button("@add", Icon.add, () -> {
            editing.add(new RuinStep());
            rebuildList();
        });
        buttons.button("@back", Icon.left, this::hide);

        hidden(this::save);
    }

    private void save() {
        filter.steps = editing.toArray(RuinStep.class);
        changed.run();
    }

    private void rebuildList() {
        list.clear();
        list.defaults().pad(4);

        if (editing.isEmpty()) {
            list.add("@nh.filter.steps.empty").wrap().width(280f).row();
            return;
        }

        for (int i = 0; i < editing.size; i++) {
            buildRow(list, editing.get(i), i);
            list.row();
        }
    }

    private void buildRow(Table table, RuinStep step, int index) {
        table.add("#" + (index + 1)).width(28f);

        table.field(String.valueOf((int) step.radius), text -> {
            if (Strings.canParsePositiveInt(text)) {
                step.radius = Integer.parseInt(text);
                changed.run();
            }
        }).width(55f).padRight(6);

        table.add("@nh.filter.step.radius").padRight(8);

        Label modeLabel = new Label(Core.bundle.get("nh.filter.step.mode." + step.stepMode.name()));
        modeLabel.setStyle(Styles.outlineLabel);
        table.button(b -> b.add(modeLabel).update(i -> modeLabel.setText(Core.bundle.get("nh.filter.step.mode." + step.stepMode.name()))), Styles.flatBordert, () -> {
            step.stepMode = step.stepMode.next();
            changed.run();
        }).width(96f).padRight(8);

        Button floorButton = table.button(b -> b.image(floorIcon(step)).update(i -> ((TextureRegionDrawable) i.getDrawable())
                .setRegion(floorIcon(step))).size(iconSmall), () -> {
            RuinFilterUI.showFloorPicker(step, () -> {
                rebuildList();
                changed.run();
            });
        }).size(48f).padRight(4).get();

        RuinFilterUI.bindBlockButton(floorButton, () -> RuinFilterUI.floorDisplay(step), block -> {
            if (block == Blocks.removeWall) {
                step.floor = Blocks.removeWall;
            } else if (block == Blocks.air) {
                step.floor = null;
            } else {
                step.floor = block;
            }
            rebuildList();
            changed.run();
        }, b -> b == Blocks.removeWall || FilterOption.floorsOptional.get(b), () -> {
            rebuildList();
            changed.run();
        });

        Button wallButton = table.button(b -> b.image(wallIcon(step)).update(i -> ((TextureRegionDrawable) i.getDrawable())
                .setRegion(wallIcon(step))).size(iconSmall), () -> {
            RuinFilterUI.showWallPicker(step, () -> {
                rebuildList();
                changed.run();
            });
        }).size(48f).padRight(4).get();

        RuinFilterUI.bindBlockButton(wallButton, () -> RuinFilterUI.wallDisplay(step), block -> {
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
            rebuildList();
            changed.run();
        }, b -> b == Blocks.removeWall || FilterOption.wallsOptional.get(b), () -> {
            rebuildList();
            changed.run();
        });

        table.button(Icon.up, Styles.clearNoneTogglei, () -> {
            if (index <= 0) return;
            editing.swap(index, index - 1);
            rebuildList();
            changed.run();
        }).size(40f).disabled(b -> index <= 0);

        table.button(Icon.down, Styles.clearNoneTogglei, () -> {
            if (index >= editing.size - 1) return;
            editing.swap(index, index + 1);
            rebuildList();
            changed.run();
        }).size(40f).disabled(b -> index >= editing.size - 1);

        table.button(Icon.trash, Styles.clearNoneTogglei, () -> {
            editing.remove(index);
            rebuildList();
            changed.run();
        }).size(40f).padLeft(4);
    }

    private static arc.graphics.g2d.TextureRegion floorIcon(RuinStep step) {
        Block block = RuinFilterUI.floorDisplay(step);
        return block == Blocks.air ? Icon.none.getRegion() : block.uiIcon;
    }

    private static arc.graphics.g2d.TextureRegion wallIcon(RuinStep step) {
        Block block = RuinFilterUI.wallDisplay(step);
        return block == Blocks.air ? Icon.none.getRegion() : block.uiIcon;
    }
}

