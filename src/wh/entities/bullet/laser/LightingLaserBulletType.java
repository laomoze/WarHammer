package wh.entities.bullet.laser;

import arc.graphics.Color;
import arc.math.Mathf;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.*;
import wh.graphics.PositionLightning;

public class LightingLaserBulletType extends LaserBulletType {
    public Color lightningColor= Pal.lancerLaser;

    @Override
    public void init(Bullet b){
        super.init(b);
        PositionLightning.createEffect(b, b.fdata * 0.95f, b.rotation(), lightningColor, 2, Mathf.random(2, 3));
    }
}