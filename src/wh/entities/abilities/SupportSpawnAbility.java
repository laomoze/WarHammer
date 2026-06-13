package wh.entities.abilities;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Interval;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import wh.content.WHContent;
import wh.content.WHStats;
import wh.content.WHUnitTypes;
import wh.graphics.Drawn;
import wh.util.WHUtils;

import static mindustry.Vars.tilesize;
import static wh.core.WarHammerMod.name;

/** 单位低血时触发一次支援召唤，回满血且冷却结束后才可再次触发。 */
public class SupportSpawnAbility extends Ability{
    // 召唤出的支援单位类型与数量。
    public UnitType unit = WHUnitTypes.airA1;
    public int spawnCount = 3;
    public float healthTrigger = 0.3f;
    public float reload = 60f * 60;
    public float interval = 15f;

    public float spawnRange = 80f;
    public float spawnWarmup = 12f;
    public float spawnEachDelay = 6f;
    public float spawnAngleOffset = 0f;

    public boolean airdrop = false;
    public float spawnShield = -1f;
    public double spawnFlag = Double.NaN;
    public StatusEffect spawnStatus = StatusEffects.none;
    public float spawnStatusDuration = 0f;

    public Effect triggerEffect = Fx.spawn;
    public Sound triggerSound = Sounds.none;
    public SupportDraw draw = SupportSpawnAbility::defaultDraw;

    protected transient Interval timer = new Interval();
    // ready 为 true 时，表示本次低血触发资格已经恢复。
    protected transient boolean ready = true;

    public SupportSpawnAbility(){
    }

    public SupportSpawnAbility(UnitType unit, int spawnCount, float healthTrigger, float reload){
        this.unit = unit;
        this.spawnCount = spawnCount;
        this.healthTrigger = healthTrigger;
        this.reload = reload;
    }

    @Override
    public void update(Unit unit){
        if(this.unit == null || spawnCount <= 0 || unit.dead) return;

        data = Math.max(0f, data - Time.delta);

        // 重新触发前必须同时满足冷却结束和生命值回满。
        if(!ready && data <= 0f && unit.healthf() >= 0.999f){
            ready = true;
        }

        // 固定间隔检查，避免每帧都去扫一遍召唤位置。
        boolean canSpawnNow = Units.canCreate(unit.team, this.unit) || unit.team == Vars.state.rules.waveTeam;

        if(!timer.get(0, interval) || !ready || unit.healthf() > healthTrigger || !canSpawnNow){
            return;
        }

        boolean spawned = WHUtils.spawnUnit(
        unit.team,
        unit.x,
        unit.y,
        unit.rotation + spawnAngleOffset,
        spawnRange,
        spawnWarmup,
        spawnEachDelay,
        this.unit,
        spawnCount,
        airdrop,
        spawner -> {
            if(spawnShield >= 0f){
                spawner.setShieldToApply(spawnShield);
            }
            if(!Double.isNaN(spawnFlag)){
                spawner.setFlagToApply(spawnFlag);
            }
            if(spawnStatus != null && spawnStatus != StatusEffects.none){
                spawner.setStatus(spawnStatus, spawnStatusDuration);
            }
        });

        // 只有真的生成成功才消耗这次触发。
        if(spawned){
            ready = false;
            data = reload;

            if(triggerEffect != Fx.none){
                triggerEffect.at(unit.x, unit.y, unit.hitSize, unit.team.color, unit);
            }

            if(triggerSound != Sounds.none){
                triggerSound.at(unit);
            }
        }
    }

    @Override
    public void draw(Unit unit){
        if(this.unit == null || draw == null) return;
        draw.draw(unit, this);
    }

    public boolean ready(){
        return ready;
    }

    public boolean waitingForFullHealth(Unit unit){
        return !ready && unit.healthf() < 0.999f;
    }

    public float reloadFraction(){
        return reload <= 0f ? 1f : Mathf.clamp(1f - data / reload);
    }

    public float drawX(Unit unit){
        return unit.x + Angles.trnsx(unit.rotation, unit.hitSize * 0.8f);
    }

    public float drawY(Unit unit){
        return unit.y + Angles.trnsy(unit.rotation, unit.hitSize * 0.8f);
    }

    public static void defaultDraw(Unit unit, SupportSpawnAbility ability){
        float z = Draw.z();
        Draw.z(Layer.bullet);

        Tmp.c1.set(unit.team.color).lerp(Color.white, Mathf.absin(4f, 0.15f));
        Draw.color(Tmp.c1);
        Lines.stroke(3f);
        Drawn.circlePercent(unit.x, unit.y, unit.hitSize * 1.45f, ability.reloadFraction(), 0);

        float scl = Interp.pow3Out.apply(Mathf.curve(ability.reloadFraction(), 0.96f, 1f)) / 5;
        TextureRegion arrowRegion = WHContent.arrowRegion;

        for(int l : Mathf.signs){
            float angle = 90 + 90 * l;
            for(int i = 0; i < 4; i++){
                Tmp.v1.trns(angle, i * unit.hitSize / 2 + unit.hitSize * 1.5f);
                float f = (100 - (Time.time + 25 * i) % 100) / 100;

                Draw.rect(arrowRegion, unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, arrowRegion.width * f * scl, arrowRegion.height * f * scl, angle + 90);
            }
        }

        Draw.z(z);
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(WHStats.format("wh-last-stand-health-trigger", Strings.autoFixed(healthTrigger * 100f, 2) + "%"));
        t.row();
        t.add(WHStats.format("wh-cooldown", Strings.autoFixed(reload / 60f, 2) + " " + mindustry.world.meta.StatUnit.seconds.localized()));
        t.row();
        t.add(WHStats.format("wh-range", Strings.autoFixed(spawnRange / tilesize, 2) + " " + mindustry.world.meta.StatUnit.blocks.localized()));
        t.row();
        t.add(WHStats.format("wh-support-spawn-count", spawnCount));
        t.row();
        t.add((unit.hasEmoji() ? unit.emoji() : "") + "[stat]" + unit.localizedName);
    }

    @Override
    public SupportSpawnAbility copy(){
        SupportSpawnAbility out = (SupportSpawnAbility)super.copy();
        out.timer = new Interval();
        out.data = 0f;
        out.ready = true;
        return out;
    }

    @Override
    public String localized(){
        return Core.bundle.format(getBundle(), unit.localizedName);
    }

    @Override
    public String getBundle(){
        return "ability." + name("support-spawn-ability");
    }

    @FunctionalInterface
    /** 支援召唤的自定义绘制接口。 */
    public interface SupportDraw{
        void draw(Unit unit, SupportSpawnAbility ability);
    }
}
