package wh.entities.bullet.laser;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.*;
import wh.entities.bullet.laser.DelayedPointBulletType.*;
import wh.graphics.*;

public class LightingLaserBulletType extends LaserBulletType {
    public Color lightningColor= Pal.lancerLaser;

    public boolean renderingDistortion = false;
    public float pathDistortionRectWidthScale = 1.5f;
    public float pathDistortionRectStrength = 0.3f;
    public LightingLaserBulletType(float damage){
        super(damage);
    }

    public LightingLaserBulletType(){}

    public void addRectPathDistortion(Bullet b, float rot){
        if(renderingDistortion){
            float rectLength = b.fdata;
            float rectWidth = Math.max(width * pathDistortionRectWidthScale, 1f);

            Tmp.v2.trns(rot, -width);
            Tmp.v3.trns(rot, rectLength * 0.5f + width / 2);
            MainRenderer.addShockRect(
            b.x + Tmp.v2.x + Tmp.v3.x, b.y + Tmp.v2.y + Tmp.v3.y,
            rectLength, rectWidth, rot, Math.max(lifetime, 60), pathDistortionRectStrength);
        }
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        PositionLightning.createEffect(b, b.fdata * 0.95f, b.rotation(), lightningColor, 2, Mathf.random(2, 3));
        addRectPathDistortion(b, b.rotation());
    }
}