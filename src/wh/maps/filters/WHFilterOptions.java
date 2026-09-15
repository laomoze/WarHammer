package wh.maps.filters;

import arc.func.*;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Strings;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.maps.filters.FilterOption;
import mindustry.ui.Styles;
import mindustry.world.Block;

import static mindustry.Vars.iconSmall;
import static mindustry.Vars.updateEditorOnChange;

public class WHFilterOptions {

    public static class SliderOption extends FilterOption {
        final String name;
        final Floatp getter;
        final Floatc setter;
        final float min, max, step;
        boolean display = true;

        public SliderOption(String name, Floatp getter, Floatc setter, float min, float max) {
            this(name, getter, setter, min, max, (max - min) / 200f);
        }

        public SliderOption(String name, Floatp getter, Floatc setter, float min, float max, float step) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.min = min;
            this.max = max;
            this.step = step;
        }

        @Override
        public void build(Table table) {
            Element base;
            if (!display) {
                Label l = new Label("@filter.option." + name);
                l.setWrap(true);
                l.setStyle(Styles.outlineLabel);
                base = l;
            } else {
                Table t = new Table().marginLeft(11f).marginRight(11f);
                base = t;
                t.add("@filter.option." + name).growX().wrap().style(Styles.outlineLabel);
                t.label(() -> Strings.autoFixed(getter.get(), 2)).style(Styles.outlineLabel).right().labelAlign(Align.right).padLeft(6);
            }
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

    public static class BlockOption extends FilterOption {
        final String name;
        final Prov<Block> supplier;
        final Cons<Block> consumer;
        final Boolf<Block> filter;

        public BlockOption(String name, Prov<Block> supplier, Cons<Block> consumer, Boolf<Block> filter) {
            this.name = name;
            this.supplier = supplier;
            this.consumer = consumer;
            this.filter = filter;
        }

        @Override
        public void build(Table table) {
            var button = table.button(b -> b.image(supplier.get().uiIcon).update(i -> ((TextureRegionDrawable) i.getDrawable())
                    .setRegion(supplier.get() == Blocks.air ? Icon.none.getRegion() : supplier.get().uiIcon)).size(iconSmall), () -> {
                RuinFilterUI.showBlockPicker("@filter.option." + name, supplier, consumer, filter, changed);
            }).pad(4).margin(12f).get();

            RuinFilterUI.bindBlockButton(button, supplier, consumer, filter, changed);

            table.add("@filter.option." + name);
        }
    }

    public static class StepsEditOption extends FilterOption {
        final RuinGenerateFilter filter;

        public StepsEditOption(RuinGenerateFilter filter) {
            this.filter = filter;
        }

        @Override
        public void build(Table table) {
            table.button(b -> b.image(Icon.edit).size(iconSmall), () -> {
                new RuinStepsDialog(filter, changed).show();
            }).pad(4).margin(12f);

            table.add("@nh.filter.option.steps");
        }
    }
}

