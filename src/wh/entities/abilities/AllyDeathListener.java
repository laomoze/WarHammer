package wh.entities.abilities;

import mindustry.gen.*;

/** 由 WHEvents 在友军死亡时统一回调，实现者只处理自身效果。 */
public interface AllyDeathListener{
    void onAllyDeath(Unit unit, Unit deadAlly);
}
