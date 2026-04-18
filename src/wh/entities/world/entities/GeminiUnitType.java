package wh.entities.world.entities;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Log;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Unit;
import wh.entities.bullet.ApproachBullet;
import wh.gen.GeminiUnit;

public class GeminiUnitType extends SuperHeavyUnitType {
    public ApproachBullet lowHealthSpecialBullet;

    public GeminiUnitType(String name) {
        super(name);
    }

    public GeminiUnitType lowHealthSpecialBullet(BulletType bullet) {
        if (bullet == null) {
            lowHealthSpecialBullet = null;
            return this;
        }

        if (bullet instanceof ApproachBullet) {
            lowHealthSpecialBullet = (ApproachBullet) bullet;
        } else {
            Log.warn("GeminiUnitType @ lowHealthSpecialBullet must be ApproachBullet, got @", name, bullet.getClass().getSimpleName());
        }
        return this;
    }

    protected float phaseAlpha(Unit unit) {
        if (unit instanceof GeminiUnit g) {
            return Mathf.lerp(1f, GeminiUnit.PHASE_ALPHA, g.phaseVisualFade);
        }
        return 1f;
    }

    @Override
    public void drawBody(Unit unit) {
        super.drawBody(unit);
        Draw.alpha(Draw.getColor().a * phaseAlpha(unit));
    }

    @Override
    public void drawWeapons(Unit unit) {
        super.drawWeapons(unit);
        Draw.alpha(Draw.getColor().a * phaseAlpha(unit));
    }

    @Override
    public void drawEngines(Unit unit) {
        super.drawEngines(unit);
        Draw.alpha(Draw.getColor().a * phaseAlpha(unit));
    }

    @Override
    public void applyColor(Unit unit) {
        super.applyColor(unit);
        Draw.alpha(Draw.getColor().a * phaseAlpha(unit));
    }

    @Override
    public void applyOutlineColor(Unit unit) {
        super.applyOutlineColor(unit);
        Draw.alpha(Draw.getColor().a * phaseAlpha(unit));
    }
}
