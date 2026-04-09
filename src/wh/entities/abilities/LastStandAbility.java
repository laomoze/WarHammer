package wh.entities.abilities;

import arc.*;
import arc.audio.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.core.*;
import wh.graphics.*;

import java.util.*;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Angles.randLenVectors;
import static wh.content.WHFx.rand;
import static wh.core.WarHammerMod.name;

/** 在低血且短时间承受高额伤害时触发一次保命无敌。 */
public class LastStandAbility extends Ability{
    // 当前生命比例低于此值时，才允许触发保命。
    public float healthTrigger = 0.3f;
    // damageWindow 内累计承伤达到该比例时触发。
    public float damageThreshold = 0.15f;
    // 统计爆发承伤的时间窗长度。
    public float damageWindow = 120;
    public float invincibleDuration = 180f;
    public float cooldown = 60f * 30f;
    public float interval = 25;

    public Effect triggerEffect =
    new Effect(35, e -> {
        color(WHPal.ShootOrange);
        stroke(e.fout() * 3f);
        Lines.poly(e.x, e.y, 4, e.rotation, 0);

        rand.setSeed(e.id);
        float size = 4;
        int intensity = WHSettings.detailCount(4, 1);
        randLenVectors(e.id, intensity, e.rotation * e.finpow(), (x, y) -> {
            float s = Mathf.curve(e.fin(), 0f, 0.1f) * e.fout(Interp.pow3In) * (size + rand.range(size / 3.0F));
            Fill.square(e.x + x, e.y + y, s, 45.0F);
            Drawf.light(e.x + x, e.y + y, s * 2.25F, WHPal.ShootOrange, 0.7F);
        });
    });
    public Sound triggerSound = Sounds.healWave;

    protected transient Interval timer = new Interval();
    // 环形数组，记录每个采样段里的承伤。
    protected transient float[] samples = new float[0];
    protected transient float damageSum;
    protected transient int sampleIndex;
    // 一个采样窗口的起始血量与窗口内最低血量。
    protected transient float sampleStartHealth = -1f;
    protected transient float lowestHealth = -1f;
    protected transient boolean ready = true;

    public LastStandAbility(){
    }

    public LastStandAbility(float healthTrigger, float damageThreshold, float damageWindow, float invincibleDuration){
        this.healthTrigger = healthTrigger;
        this.damageThreshold = damageThreshold;
        this.damageWindow = damageWindow;
        this.invincibleDuration = invincibleDuration;
    }

    @Override
    public void update(Unit unit){
        ensureSamples();

        if(sampleStartHealth < 0f){
            // 记录当前采样窗口的起始血量和窗口内最低血量。
            sampleStartHealth = unit.health;
            lowestHealth = unit.health;
        }

        // 触发过后，必须再次回到满血才允许下次触发。
        if(!ready && unit.healthf() > 0.99f){
            ready = true;
        }

        // 只记录窗口里的最低血量，这样中途回血也不会覆盖爆发承伤。
        lowestHealth = Math.min(lowestHealth, unit.health);
        // data 在这里表示技能冷却剩余时间。
        data = Math.max(0f, data - Time.delta);

        if(timer.get(0, interval)){
            float lost = Math.max(0f, sampleStartHealth - lowestHealth);

            // 用定长环形队列维护最近 damageWindow 内的累计承伤。
            // 先减去最旧采样，再写入当前采样。
            damageSum -= samples[sampleIndex];
            samples[sampleIndex] = lost;
            damageSum += lost;
            sampleIndex = (sampleIndex + 1) % samples.length;

            sampleStartHealth = unit.health;
            lowestHealth = unit.health;

            if(ready &&
            data <= 0f &&
            unit.healthf() <= healthTrigger &&
            damageSum >= unit.maxHealth * damageThreshold &&
            !unit.hasEffect(StatusEffects.invincible)){
                unit.apply(StatusEffects.invincible, invincibleDuration);
                data = cooldown;
                ready = false;
                clearSamples();

                if(triggerEffect != Fx.none){
                    triggerEffect.at(unit.x, unit.y, unit.hitSize, unit.team.color, unit);
                }

                if(triggerSound != Sounds.none){
                    triggerSound.at(unit);
                }
            }
        }
    }

    protected void ensureSamples(){
        int length = sampleCount();
        if(samples.length != length){
            // 滑窗长度固定后不再重复分配，减少 update 里的额外开销。
            samples = new float[length];
            damageSum = 0f;
            sampleIndex = 0;
        }
    }

    protected void clearSamples(){
        Arrays.fill(samples, 0f);
        damageSum = 0f;
        sampleIndex = 0;
    }

    protected int sampleCount(){
        return Math.max(1, (int)Math.ceil(damageWindow / Math.max(interval, 1f)));
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(Core.bundle.format("stat.wh-last-stand-health-trigger", Strings.autoFixed(healthTrigger * 100f, 2) + "%"));
        t.row();
        t.add(Core.bundle.format("stat.wh-damage-threshold", Strings.autoFixed(damageThreshold * 100f, 2) + "%"));
        t.row();
        t.add(Core.bundle.format("stat.wh-invincible-duration", Strings.autoFixed(invincibleDuration / 60f, 2)));
        t.row();
        t.add(Core.bundle.format("stat.wh-cooldown", Strings.autoFixed(cooldown / 60f, 2)));
    }

    @Override
    public LastStandAbility copy(){
        LastStandAbility out = (LastStandAbility)super.copy();
        out.timer = new Interval();
        out.samples = new float[0];
        out.damageSum = 0f;
        out.sampleIndex = 0;
        out.sampleStartHealth = -1f;
        out.lowestHealth = -1f;
        out.data = 0f;
        out.ready = true;
        return out;
    }

    @Override
    public String localized(){
        return Core.bundle.get(getBundle());
    }

    @Override
    public String getBundle(){
        return "ability." + name("last-stand-ability");
    }
}
