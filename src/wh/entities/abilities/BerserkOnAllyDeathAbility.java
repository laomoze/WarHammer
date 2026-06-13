package wh.entities.abilities;

import arc.Core;
import arc.audio.Sound;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import wh.content.WHStats;

import static mindustry.Vars.tilesize;
import static wh.core.WarHammerMod.name;

/** 附近友军阵亡后，单位短时间进入狂暴状态。 */
public class BerserkOnAllyDeathAbility extends Ability implements AllyDeathListener{
    // 监听友军死亡的判定范围。
    public float range = 90;
    // 一次触发后的持续时间。
    public float duration = 90;
    public float speedMultiplier = 1.4f;
    public float reloadMultiplier = 1.2f;
    // 狂暴期间的承伤减免。
    public float damageReduction = 0.5f;

    public Effect triggerEffect = Fx.overdriven;
    public Sound triggerSound = Sounds.none;

    public BerserkOnAllyDeathAbility(){
    }

    public BerserkOnAllyDeathAbility(float range, float duration, float speedMultiplier, float reloadMultiplier, float damageReduction){
        this.range = range;
        this.duration = duration;
        this.speedMultiplier = speedMultiplier;
        this.reloadMultiplier = reloadMultiplier;
        this.damageReduction = damageReduction;
    }

    @Override
    public void update(Unit unit){
        data = Math.max(0f, data - Time.delta);
        if(unit.dead || data <= 0f){
            return;
        }

        unit.speedMultiplier *= speedMultiplier;
        unit.reloadMultiplier *= reloadMultiplier;

        if(damageReduction > 0f){
            float clamped = Mathf.clamp(damageReduction, 0f, 0.95f);
            unit.healthMultiplier *= 1f / Math.max(1f - clamped, 0.001f);
        }
    }

    @Override
    public void onAllyDeath(Unit unit, Unit deadAlly){
        if(unit == null || deadAlly == null || unit == deadAlly || unit.dead || deadAlly.team != unit.team || !deadAlly.within(unit, range)){
            return;
        }

        // 多次友军死亡会刷新/延长持续时间，但只在首次进入时播放特效和音效。
        boolean activating = data <= 0f;
        data = Math.max(data, duration);

        if(activating && triggerEffect != Fx.none){
            triggerEffect.at(unit.x, unit.y, unit.hitSize, unit.team.color, unit);
        }

        if(activating && triggerSound != Sounds.none){
            triggerSound.at(unit);
        }
    }

    @Override
    public void displayBars(Unit unit, Table bars){
        if(data <= 0f || duration <= 0f){
            return;
        }

        bars.add(new Bar(
        () -> Core.bundle.format("bar.wh-berserk", Mathf.round(Mathf.clamp(data / duration) * 100f)),
        () -> Pal.remove,
        () -> Mathf.clamp(data / duration)
        )).row();
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(WHStats.format("wh-range", Strings.autoFixed(range / tilesize, 2) + " " + mindustry.world.meta.StatUnit.blocks.localized()));
        t.row();
        t.add(WHStats.format("wh-berserk-duration", Strings.autoFixed(duration / 60f, 2) + " " + mindustry.world.meta.StatUnit.seconds.localized()));
        t.row();
        t.add(WHStats.format("wh-berserk-speed", Strings.autoFixed((speedMultiplier - 1f) * 100f, 2) + "%"));
        t.row();
        t.add(WHStats.format("wh-berserk-reload", Strings.autoFixed((reloadMultiplier - 1f) * 100f, 2) + "%"));
        if(damageReduction > 0f){
            t.row();
            t.add(WHStats.format("wh-berserk-damage-reduction", Strings.autoFixed(damageReduction * 100f, 2) + "%"));
        }
    }

    @Override
    public BerserkOnAllyDeathAbility copy(){
        BerserkOnAllyDeathAbility out = (BerserkOnAllyDeathAbility)super.copy();
        out.data = 0f;
        return out;
    }

    @Override
    public String localized(){
        return Core.bundle.get(getBundle());
    }

    @Override
    public String getBundle(){
        return "ability." + name("berserk-on-ally-death-ability");
    }
}
