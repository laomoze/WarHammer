package wh.entities.abilities;

import mindustry.gen.*;

/** 由 WHEvents 统一分发子弹击杀事件，能力只处理自己的效果。 */
public interface BulletKillListener{
    void onBulletKill(Unit unit, Healthc target, Bullet bullet);
}
