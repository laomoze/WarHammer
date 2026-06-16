package wh.ui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.core.UI;
import mindustry.ui.Styles;
import wh.core.WarHammerMod;

public class PsychicImage extends Table {
    public final float amount;

    public PsychicImage(float amount) {
        this.amount = amount;

        add(new Stack() {{
            add(new Table(o -> {
                o.left();
                o.add(new Image(region())).size(32f).scaling(Scaling.fit).color(Color.white);
            }));

            if (amount != 0f) {
                add(new Table(t -> {
                    t.left().bottom();
                    t.add(amount >= 1000f ? UI.formatAmount((int) amount) : PsychicStatValues.format(amount)).style(Styles.outlineLabel);
                    t.pack();
                }));
            }
        }});
    }

    public static TextureRegion region() {
        return Core.atlas.find(WarHammerMod.name("psychic-ui"), Core.atlas.find("clear"));
    }
}
