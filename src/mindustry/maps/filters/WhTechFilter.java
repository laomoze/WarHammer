package mindustry.maps.filters;

import arc.Core;
import arc.func.*;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Strings;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.*;
import static mindustry.maps.filters.FilterOption.floorsOnly;
import static mindustry.maps.filters.FilterOption.wallsOptional;

/**
 * Editor auto-generation filter that mirrors BasicGenerator.tech(...).
 */
public class WhTechFilter extends GenerateFilter {
    public float cellSize = 20f;
    public float thresholdA = 0.63f;
    public float thresholdB = 0.6f;
    public float wallChance = 0.7f;
    public float innerOffset = 2f;
    public Block floor = Blocks.darkPanel3, floor2 = Blocks.darkPanel4, wall = Blocks.darkMetal;

    @Override
    public String name() {
        if (Core.bundle != null && Core.bundle.has("wh.filter.tech")) {
            return Core.bundle.get("wh.filter.tech");
        }
        return "Tech Grid";
    }

    @Override
    public FilterOption[] options() {
        return new FilterOption[]{
                new WhSliderOption("scale", () -> cellSize, f -> cellSize = f, 2f, 120f, 1f),
                new WhSliderOption("threshold", () -> thresholdA, f -> thresholdA = f, 0f, 1f, 0.005f),
                new WhSliderOption("threshold2", () -> thresholdB, f -> thresholdB = f, 0f, 1f, 0.005f),
                new WhSliderOption("chance", () -> wallChance, f -> wallChance = f, 0f, 1f, 0.005f),
                new WhSliderOption("radius", () -> innerOffset, f -> innerOffset = f, 0f, 12f, 0.1f),
                new WhBlockOption("floor", () -> floor, b -> floor = b, floorsOnly),
                new WhBlockOption("floor2", () -> floor2, b -> floor2 = b, floorsOnly),
                new WhBlockOption("wall", () -> wall, b -> wall = b, wallsOptional)
        };
    }

    @Override
    public char icon() {
        return Iconc.blockMetalWall1;
    }

    @Override
    public void apply(GenerateInput in) {
        if (!(in.floor instanceof Floor)) return;
        Floor floorBase = (Floor) in.floor;
        if (!floorBase.hasSurface()) return;

        int secSize = Math.max(2, Mathf.round(cellSize));
        int mx = Math.floorMod(in.x, secSize), my = Math.floorMod(in.y, secSize);
        int sclx = in.x / secSize, scly = in.y / secSize;
        boolean onEdge = (mx == 0 || my == 0 || mx == secSize - 1 || my == secSize - 1);
        if (!onEdge) return;

        if (noise(sclx, scly, 0.2f, 1f, 1f, 1f) > thresholdA && noise(sclx, scly + 999f, 200f, 1f, 1f, 1f) > thresholdB) {
            float floorNoiseChance = Mathf.clamp(noise(in.x + 0x231523f, in.y, 40f, 1f, 1f, 1f));
            if (chance(in.x, in.y) < floorNoiseChance) {
                Block selected = Mathf.dst(mx, my, secSize / 2f, secSize / 2f) > secSize / 2f + innerOffset ? floor2 : floor;
                if (selected != null && selected.isFloor()) {
                    in.floor = selected;
                }
            }

            if (wall != null && wall != Blocks.air && in.block.solid && chance(in.x, in.y + 1337) < wallChance) {
                in.block = wall;
            }
        }
    }

    static class WhSliderOption extends FilterOption {
        final String name;
        final Floatp getter;
        final Floatc setter;
        final float min, max, step;

        WhSliderOption(String name, Floatp getter, Floatc setter, float min, float max, float step) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        @Override
        public void build(Table table) {
            Table base = new Table().marginLeft(11f).marginRight(11f);
            base.add("@filter.option." + name).growX().wrap().style(Styles.outlineLabel);
            base.label(() -> Strings.autoFixed(getter.get(), 2)).style(Styles.outlineLabel).right().labelAlign(Align.right).padLeft(6);
            base.touchable = Touchable.disabled;

            Slider slider = new Slider(min, max, step, false);
            slider.moved(setter);
            slider.setValue(getter.get());
            if (updateEditorOnChange) {
                slider.changed(changed);
            } else {
                slider.released(changed);
            }

            table.stack(slider, base).colspan(2).pad(3).growX().row();
        }
    }

    static class WhBlockOption extends FilterOption {
        final String name;
        final Prov<Block> supplier;
        final Cons<Block> consumer;
        final Boolf<Block> filter;

        WhBlockOption(String name, Prov<Block> supplier, Cons<Block> consumer, Boolf<Block> filter) {
            this.name = name;
            this.supplier = supplier;
            this.consumer = consumer;
            this.filter = filter;
        }

        @Override
        public void build(Table table) {
            Button button = table.button(b -> b.image(supplier.get().uiIcon).update(i -> ((TextureRegionDrawable) i.getDrawable())
                    .setRegion(supplier.get() == Blocks.air ? Icon.none.getRegion() : supplier.get().uiIcon)).size(iconSmall), () -> {
                BaseDialog dialog = new BaseDialog("@filter.option." + name);
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
            }).pad(4).margin(12f).get();

            button.clicked(KeyCode.mouseMiddle, () -> {
                Core.app.setClipboardText(supplier.get().name);
                ui.showInfoFade("@copied");
            });

            button.clicked(KeyCode.mouseRight, () -> {
                Block block = content.block(Core.app.getClipboardText());
                if (block != null && filter.get(block)) {
                    consumer.get(block);
                    changed.run();
                }
            });

            table.add("@filter.option." + name);
        }
    }
}
