package wh.entities.abilities;

import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class MendFieldAbility extends Ability {
    public Color baseColor = Color.valueOf("84f491"), phaseColor = Color.valueOf("ffd59e");

    public float range = 180f, reload = 60f, healP = 10f;
    public float timer;

    public MendFieldAbility() {}

    public MendFieldAbility(float range, float reload, float healP) {
        this.range = range;
        this.reload = reload;
        this.healP = healP;
    }

    @Override
    public void update(Unit unit) {
        Vars.indexer.eachBlock(unit, range, Building::damaged, other -> {
            timer += Time.delta;
            if (timer >= reload) {
                timer = 0f;
                other.heal((healP / 100) * other.block.health);
                Fx.healBlockFull.at(other.x, other.y, other.block.size, Tmp.c1.set(baseColor).lerp(phaseColor, 0.3f));
            }
        });
    }

    @Override
    public Ability copy() {
        return new MendFieldAbility(range, reload, healP);
    }

    @Override
    public void draw(Unit unit) {
        Vars.indexer.eachBlock(unit, range, Building::damaged, other -> {
            Color tmp = Tmp.c1.set(baseColor);
            tmp.a = Mathf.absin(4f, 1f);
            Drawf.selected(other, tmp);
        });
    }
}
