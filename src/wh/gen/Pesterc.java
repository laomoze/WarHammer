package wh.gen;

import arc.math.geom.*;
import arc.struct.*;
import mindustry.gen.*;
import mindustry.graphics.*;

@SuppressWarnings("unused")
public interface Pesterc extends Unitc {
    Healthc findOwner(Entityc ent);

    void shootBossTarget();

    void shootAtHatred();

    void drawBossWeapon();

    void shoot(Healthc h);

    Teamc bossTarget();

    Teamc lastTarget();

    Vec2 lastTargetPos();

    float bossWeaponReload();

    float bossWeaponWarmup();

    float bossWeaponProgress();

    float bossTargetShiftLerp();

    float bossTargetSearchReload();

    float bossWeaponReloadLast();

    float bossWeaponReloadTarget();

    float hatredCheckReload();

    float salvoReload();

    float salvoReloadLast();

    float salvoReloadTarget();

    ObjectFloatMap<Healthc> hatred();

    Seq<Healthc> nextTargets();

    Trail[] trails();

    void isBoss(boolean value);

    void bossTarget(Teamc value);

    void lastTarget(Teamc value);

    void lastTargetPos(Vec2 value);

    void bossWeaponReload(float value);

    void bossWeaponWarmup(float value);

    void bossWeaponProgress(float value);

    void bossTargetShiftLerp(float value);

    void bossTargetSearchReload(float value);

    void bossWeaponReloadLast(float value);

    void bossWeaponReloadTarget(float value);

    void hatredCheckReload(float value);

    void salvoReload(float value);

    void salvoReloadLast(float value);

    void salvoReloadTarget(float value);

    void hatred(ObjectFloatMap<Healthc> value);

    void nextTargets(Seq<Healthc> value);

    void trails(Trail[] value);
}
