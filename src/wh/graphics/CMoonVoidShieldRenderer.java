package wh.graphics;

import arc.Core;
import arc.Events;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.FrameBuffer;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.EventType;
import wh.gen.CMoonUnit;

import static wh.content.WHContent.VOID_SHIELD;

public final class CMoonVoidShieldRenderer {
    private static final Seq<CMoonUnit> units = new Seq<>(false, 4, CMoonUnit.class);
    private static final FrameBuffer shieldBuffer = new FrameBuffer();

    private CMoonVoidShieldRenderer() {
    }

    public static void init() {
        if (!Vars.headless) {
            Events.on(EventType.WorldLoadEvent.class, event -> units.clear());
            Events.run(EventType.Trigger.draw, CMoonVoidShieldRenderer::draw);
        }
    }

    public static void add(CMoonUnit unit) {
        if (!units.contains(unit, true)) units.add(unit);
    }

    public static void remove(CMoonUnit unit) {
        units.remove(unit, true);
    }

    private static void draw() {
        if (!Vars.renderer.animateShields) return;

        WHShaders.CMoonVoidShieldShader shader = WHShaders.cMoonVoidShield;
        if (shader == null) return;

        shader.clear();
        for (int index = 0; index < units.size; index++) {
            CMoonUnit moon = units.get(index);
            if (!moon.isAdded() || moon.dead) continue;

            shader.add(moon.x, moon.y, moon.shieldLongAxis(), moon.shieldMinorAxis(),
                    -moon.shieldRotation(), moon.shieldRenderState(), moon.shieldStateProgress(),
                    moon.shieldFraction(), moon.voidShieldAlpha, moon.voidShieldColor());
        }

        if (!shader.hasShields()) return;

        shieldBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        int batchCount = shader.batchCount();
        for (int batch = 0; batch < batchCount; batch++) {
            int batchIndex = batch;
            Draw.drawRange(VOID_SHIELD + batch * 0.0001f, 1f,
                    () -> shieldBuffer.begin(Color.clear),
                    () -> {
                        shieldBuffer.end();
                        Blending previousBlending = Draw.getBlend();
                        Draw.blend(Blending.normal);
                        shader.setBatch(batchIndex);
                        try {
                            shieldBuffer.blit(shader);
                        } finally {
                            Draw.blend(previousBlending);
                        }
                    });
        }
    }
}