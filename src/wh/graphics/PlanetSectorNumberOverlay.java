package wh.graphics;

import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.util.Align;
import mindustry.Vars;
import mindustry.game.EventType.Trigger;
import mindustry.type.Planet;
import mindustry.type.Sector;
import mindustry.ui.Fonts;

public class PlanetSectorNumberOverlay {
    private static final Color numberColor = Color.white.cpy();

    private PlanetSectorNumberOverlay() {
    }

    public static void init() {
        Events.run(Trigger.universeDrawEnd, PlanetSectorNumberOverlay::draw);
    }

    private static void draw() {
        if (Vars.headless || Vars.ui == null || Vars.ui.planet == null || !Vars.ui.planet.isShown()) return;

        var dialog = Vars.ui.planet;
        var params = dialog.state;
        Planet planet = params.planet;
        if (planet == null || !params.drawUi || !planet.hasGrid() || params.uiAlpha <= 0.001f) return;

        for (Sector sector : planet.sectors) {
            dialog.planets.drawPlane(sector, () -> {
                Fonts.outline.getData().setScale(0.5f);
                Fonts.outline.setColor(numberColor.r, numberColor.g, numberColor.b, numberColor.a * params.uiAlpha);
                Fonts.outline.draw(Integer.toString(sector.id), 0f, 0f, Align.center);
                Fonts.outline.setColor(Color.white);
                Fonts.outline.getData().setScale(1f);
            });
        }

        Draw.reset();
    }
}
