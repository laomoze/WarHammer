package wh.entities.bullet.laser;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.Damage;
import mindustry.entities.bullet.ContinuousLaserBulletType;
import mindustry.gen.Bullet;
import wh.graphics.PositionLightning;

public class lightingContinuousLaserBullet extends ContinuousLaserBulletType{
    public Color lightningColor;

    @Override
    public void draw(Bullet b){
        float fout = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f);
        float realLength = Damage.findLength(b, length * fout, laserAbsorb, pierceCap);
        if(colors.length > 0 && b.timer.get(2, 5)){
            PositionLightning.createEffect(b, Tmp.v2.trns(b.rotation(), realLength).add(b), lightningColor, 1, Mathf.random(2, 3));
        }
        super.draw(b);
    }
}