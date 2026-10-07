package wh.entities.abilities;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Interval;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;

public class BulletDeflectAbility extends Ability {
    public float range = 70;
    public float deflectAngle = 15f;
    public float deflectChance = 0.5f;
    public float deflectCooldown = 12f;
    public boolean onlyApproaching = true;

    public Effect deflectEffect = Fx.absorb;
    public Color effectColor = Pal.accent;

    protected transient Interval timer = new Interval();

    @Override
    public void update(Unit unit) {
        if (range <= 0f || deflectAngle == 0f) return;

        float diameter = range * 2f;
        Groups.bullet.intersect(unit.x - range, unit.y - range, diameter, diameter, bullet -> {
            if (!canDeflect(unit, bullet)) return;

            float dx = unit.x - bullet.x;
            float dy = unit.y - bullet.y;
            if (unit.dst(bullet) > range) return;

            if (onlyApproaching && bullet.vel.x * dx + bullet.vel.y * dy <= 0f) return;

            if (deflectChance < 1f && !Mathf.chance(deflectChance)) return;
            if (!timer.get(0, deflectCooldown)) return;

            float cross = bullet.vel.x * dy - bullet.vel.y * dx;
            float side = cross == 0f ? (Mathf.random(1f) < 0.5f ? -1f : 1f) : cross > 0f ? -1f : 1f;
            float angle = bullet.rotation() + side * deflectAngle;
            bullet.rotation(angle);

            if (deflectEffect != null) {
                deflectEffect.at(bullet.x, bullet.y, angle, effectColor);
            }
        });

    }

    protected boolean canDeflect(Unit unit, Bullet bullet) {
        return bullet != null && bullet.isAdded() && !bullet.absorbed
                && bullet.team != unit.team && bullet.type != null && bullet.type.collides;
    }

    @Override
    public BulletDeflectAbility copy() {
        BulletDeflectAbility out = (BulletDeflectAbility) super.copy();
        out.timer = new Interval();
        return out;
    }

}
