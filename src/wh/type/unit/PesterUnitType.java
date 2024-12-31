package wh.type.unit;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import wh.content.*;

import static mindustry.Vars.*;

public class PesterUnitType extends AncientUnitType {
    public float bossWeaponRange = 80f * tilesize;
    public float reflectRange = 120f * tilesize;

    public float checkReload = 12f;
    public float checkBossReload = 60f;
    public float checkDamage = 3000f;
    public float salvoReload = 480f;
    public float bossReload = 600f;
    public float shootDelay = 60f;

    public float checkRange = 320f;

    public BulletType hitterBullet = WHBullets.hitter;

    public Effect toBeBlastedEffect = new Effect(shootDelay, e -> {
        Draw.color(e.color, Color.white, e.fin());

        Lines.stroke(2 * e.fin());
        Lines.circle(e.x, e.y, e.rotation * Interp.pow4Out.apply(e.fout()));

        Lines.spikes(e.x, e.y, 1.82f * e.rotation * Interp.pow2Out.apply(e.fout()), e.fin() * e.rotation / 6f, 4, 45);
    }).followParent(true);

    public PesterUnitType(String name) {
        super(name);
    }
}
