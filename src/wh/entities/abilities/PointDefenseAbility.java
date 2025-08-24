package wh.entities.abilities;

import arc.*;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;

public class PointDefenseAbility extends Ability {
    public float px;
    public float py;
    public float reloadTime = 60.0F;
    public float range = 180.0F;
    public float bulletDamage = 10.0F;
    public String suffix = "-full";
    public Color color;
    public Bullet target;
    public float rotation;
    public float timer;
    public float reload;

    public PointDefenseAbility() {
        color = Color.white;
        rotation = 90.0F;
        timer = 90.0F;
        reload = 60.0F;
    }

    public PointDefenseAbility(float px, float py, float reloadTime, float range, float bulletDamage, String sprite) {
        color = Color.white;
        rotation = 90.0F;
        timer = 90.0F;
        reload = 60.0F;
        this.px = px;
        this.py = py;
        this.reloadTime = reloadTime;
        this.range = range;
        this.bulletDamage = bulletDamage;
        suffix = sprite;
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);
        float x = unit.x + Angles.trnsx(unit.rotation, py, px);
        float y = unit.y + Angles.trnsy(unit.rotation, py, px);
        Draw.rect(Core.atlas.find(suffix), x, y, rotation);
    }

    @Override
    public void update(Unit unit) {
        float x = unit.x + Angles.trnsx(unit.rotation, py, px);
        float y = unit.y + Angles.trnsy(unit.rotation, py, px);
        target = Groups.bullet.intersect(unit.x - range, unit.y - range, range * 2.0F, range * 2.0F).min((b) ->
        b.team != unit.team && b.type.hittable,
        (b) -> b.dst2(unit));
        if (target != null && !target.isAdded()) {
            target = null;
        }

        if (target == null) {
            if (timer >= 90.0F) {
                rotation = Angles.moveToward(rotation, unit.rotation, 3.0F);
            } else {
                timer += Time.delta;
            }
        }

        if (target != null && target.within(unit, range) && target.team != unit.team && target.type != null && target.type.hittable) {
            timer = 0.0F;
            reload += Time.delta;
            float dest = target.angleTo(x, y) - 180.0F;
            rotation = Angles.moveToward(rotation, dest, 20.0F);
            if (Angles.within(rotation, dest, 3.0F) && reload >= reloadTime) {
                if (target.damage > bulletDamage) {
                    target.damage -= bulletDamage;
                } else {
                    target.remove();
                }

                Tmp.v1.trns(rotation, 6.0F);
                Fx.pointBeam.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation, color, target);
                Fx.sparkShoot.at(x + Tmp.v1.x, y + Tmp.v1.y, rotation, color);
                Fx.pointHit.at(target.x, target.y, color);
                Sounds.lasershoot.at(x, y, Mathf.random(0.9F, 1.1F));
                reload = 0.0F;
            }
        }
    }
}