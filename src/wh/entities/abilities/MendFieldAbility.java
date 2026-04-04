package wh.entities.abilities;

import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import static mindustry.Vars.indexer;

/** 周期性修复附近受损建筑，并绘制修复高亮。 */
public class MendFieldAbility extends Ability {
    public Color baseColor = Color.valueOf("84f491");
    public Color phaseColor = Color.valueOf("ffd59e");

    public float range = 180f;
    public float reload = 60f;
    public float healPercent = 10f;

    public MendFieldAbility() {}

    public MendFieldAbility(float ran, float rel, float hel) {
        range = ran;
        reload = rel;
        healPercent = hel;
    }

    @Override
    public void update(Unit unit) {
        indexer.eachBlock(unit, range, Building::damaged, other -> {
            // data 在这里是公共冷却，范围内任意受损建筑都共用这次触发。
            data += Time.delta;
            if(data >= reload){
                data = 0f;
                other.heal((healPercent / 100) * other.block.health);
                Fx.healBlockFull.at(other.x, other.y, other.block.size, Tmp.c1.set(baseColor).lerp(phaseColor, 0.3f));
            }
        });
    }

    @Override
    public void draw(Unit unit) {
        indexer.eachBlock(unit, range, Building::damaged, other -> {
            Color tmp = Tmp.c1.set(baseColor);
            tmp.a = Mathf.absin(4f, 1f);
            Drawf.selected(other, tmp);
        });
    }
}
