package wh.entities.abilities;

import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;

public class PointDefenseAbility extends Ability {
    public float px, py;
    public float reloadTime = 60f, range = 180f, bulletDamage = 10f;

    public String suffix = "-full";

    public Color color = Color.white;
    public Bullet target;
    public float rotation = 90f, timer = 90f, reload = 60f;

    public PointDefenseAbility() {}

    public PointDefenseAbility(float px, float py, float reloadTime, float range, float bulletDamage, String sprite) {
        this.px = px;
        this.py = py;
        this.reloadTime = reloadTime;
        this.range = range;
        this.bulletDamage = bulletDamage;
        this.suffix = sprite;
    }

    @Override
    public void update(Unit unit) {
        float x = unit.x + Angles.trnsx(unit.rotation, py, px);
        float y = unit.y + Angles.trnsy(unit.rotation, py, px);
        target = Groups.bullet.intersect(unit.x - range, unit.y - range, range * 2, range * 2).min(b -> b.team != unit.team && b.type.hittable, b -> b.dst2(unit));

        if (target != null && !target.isAdded()) {
            target = null;
        }
        if (target == null) {
            if (timer >= 90) {
                rotation = Angles.moveToward(rotation, unit.rotation, 3f);
            } else {
                timer += Time.delta;
            }
        }
        if (target != null && target.within(unit, range) && target.team != unit.team && target.type != null && target.type.hittable) {
            timer = 0f;
            reload += Time.delta;
            //float dest = unit.angleTo(target);
            float dest = target.angleTo(x, y) - 180;
            rotation = Angles.moveToward(rotation, dest, 20f);
            if (Angles.within(rotation, dest, 3f) && reload >= reloadTime) {
                if (target.damage > bulletDamage) {
                    target.damage -= bulletDamage;
                } else {
                    target.remove();
                }
                Tmp.v1.trns(rotation, 6f);
                Fx.pointBeam.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation, color, target);
                Fx.sparkShoot.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation, color);
                Fx.pointHit.at(target.x, target.y, color);
                Sounds.lasershoot.at(x, y, Mathf.random(0.9f, 1.1f));
                reload = 0f;
            }
        }
    }
}
