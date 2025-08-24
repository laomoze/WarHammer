package wh.entities.abilities;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;

import static mindustry.Vars.indexer;

public class MendFieldAbility extends Ability {
    public Color baseColor = Color.valueOf("84f491");
    public Color phaseColor = Color.valueOf("ffd59e");

    public float range = 180f;
    public float reload = 60f;
    public float healPercent = 10f;

    protected float timer = 0f;

    public MendFieldAbility() {}

    public MendFieldAbility(float ran, float rel, float hel) {
        range = ran;
        reload = rel;
        healPercent = hel;
    }

    @Override
    public void update(Unit unit) {
        indexer.eachBlock(unit, range, Building::damaged, other -> {
            timer += Time.delta;
            if (timer >= reload) {
                timer = 0f;
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
