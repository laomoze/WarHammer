package wh.entities.world.entities;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import wh.gen.RevengeUnit;

public class RevengeUnitType extends SuperHeavyUnitType {
    public TextureRegion armorRegion;

    public RevengeUnitType(String name) {
        super(name);
    }

    @Override
    public void load() {
        super.load();
        armorRegion = Core.atlas == null ? null : Core.atlas.find(this.name + "-energyArmor");
    }

    @Override
    public void draw(Unit unit) {
        super.draw(unit);
        if (unit instanceof RevengeUnit re) {
            if (armorRegion != null) {
                float width = armorRegion.width * Draw.scl * re.drawSize,
                        height = armorRegion.height * Draw.scl * re.drawSize;

                if (Vars.renderer.animateShields) {
                    Draw.z(Layer.shields + 0.01f);
                    Draw.color(Tmp.c1.set(re.team.color.cpy()).lerp(Color.white, Mathf.absin(4f, 0.3f)));
                    Draw.rect(armorRegion, re.x, re.y, width, height, re.rotation - 90);
                } else {
                    Draw.z(Layer.shields);
                    Draw.color(Tmp.c1.set(re.team.color.cpy()).lerp(Color.white, Mathf.absin(4f, 0.3f)));
                    Draw.alpha(0.5f);
                    Draw.rect(armorRegion, re.x, re.y, width, height, re.rotation - 90);
                }
            }
        }

    }
}
