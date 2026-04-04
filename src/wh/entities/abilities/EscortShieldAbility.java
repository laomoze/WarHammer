package wh.entities.abilities;

import arc.*;
import arc.audio.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;

import static mindustry.Vars.tilesize;
import static wh.core.WarHammerMod.name;

/** 周期性检查附近非同类型友军最近承受的伤害，并将其中一部分转移到自己身上。 */
public class EscortShieldAbility extends Ability{
    // 记录“本次掉血其实来自护航转移”的量，避免链式护航重复结算。
    protected static final IntMap<Float> pendingTransferredLoss = new IntMap<>();
    // 防止同一帧内多个护航者对同一目标重复回填。
    protected static final IntMap<Float> redirectedThisFrame = new IntMap<>();
    protected static long redirectedFrame = -1L;

    public float range = 120f;
    public float redirectPercent = 0.35f;
    // 护航者对“转移过来的伤害”额外享受的减伤比例。
    public float selfDamageReduction = 0.85f;
    public float interval = 30;
    public float maxRedirect = -1f;

    public Effect transferEffect = Fx.healWaveDynamic;
    public Effect absorbEffect = Fx.shieldApply;
    public Sound transferSound = Sounds.none;

    protected transient Interval timer = new Interval();
    // 保存友军上次扫描时的血量/护盾快照。
    protected transient IntMap<AllyState> trackedAllies = new IntMap<>();
    protected transient IntSet seen = new IntSet();
    protected transient IntSeq stale = new IntSeq();

    public EscortShieldAbility(){
    }

    public EscortShieldAbility(float range, float redirectPercent){
        this.range = range;
        this.redirectPercent = redirectPercent;
    }

    @Override
    public void update(Unit unit){
        if(unit.dead || redirectPercent <= 0f || range <= 0f){
            return;
        }

        if(timer.get(0, interval)){
            scan(unit);
        }
    }

    protected void scan(Unit unit){
        beginRedirectFrame();
        seen.clear();
        final boolean[] playedSound = {false};

        Units.nearby(unit.team, unit.x - range, unit.y - range, range * 2f, range * 2f, other -> {
            if(unit.dead || other == unit || other.dead || !other.isValid() || other.type == unit.type || !other.within(unit, range)){
                return;
            }

            seen.add(other.id);

            float currentHealth = Math.max(other.health, 0f);
            float currentShield = Math.max(other.shield, 0f);
            AllyState state = trackedAllies.get(other.id);

            if(state == null){
                trackedAllies.put(other.id, new AllyState(currentHealth, currentShield));
                return;
            }

            float healthLoss = Math.max(state.health - currentHealth, 0f);
            float shieldLoss = Math.max(state.shield - currentShield, 0f);
            float totalLoss = healthLoss + shieldLoss;

            Float ignored = pendingTransferredLoss.get(other.id);
            if(ignored != null){
                pendingTransferredLoss.remove(other.id);
                totalLoss = Math.max(totalLoss - ignored, 0f);
            }

            if(totalLoss > 0.001f){
                // 这里是“事后补偿”逻辑：先按快照算出友军刚刚失去的血/盾，再决定替他回填多少。
                float desiredRedirect = totalLoss * Mathf.clamp(redirectPercent);
                float alreadyRedirected = getRedirectedThisFrame(other.id);
                float remainingLoss = Math.max(totalLoss - alreadyRedirected, 0f);
                float redirect = Math.min(desiredRedirect, remainingLoss);

                if(maxRedirect >= 0f){
                    redirect = Math.min(redirect, maxRedirect);
                }

                redirect = Math.min(redirect, selfCapacity(unit));

                if(redirect > 0.001f){
                    float totalObservedLoss = Math.max(healthLoss + shieldLoss, 0.001f);
                    float shieldShare = redirect * shieldLoss / totalObservedLoss;
                    float healthShare = redirect - shieldShare;
                    float selfDamageTaken = redirect * (1f - Mathf.clamp(selfDamageReduction, 0f, 0.95f));

                    if(shieldShare > 0f){
                        other.shield = Math.min(state.shield, Math.max(other.shield, 0f) + shieldShare);
                    }

                    if(healthShare > 0f){
                        other.heal(Math.min(healthShare, Math.max(state.health - other.health, 0f)));
                    }

                    // 护航者自己承受的是减伤后的转移伤害，仍然会正常经过护盾/装甲/生命结算。
                    float beforePool = unit.health + Math.max(unit.shield, 0f);
                    unit.damage(selfDamageTaken);
                    float afterPool = unit.health + Math.max(unit.shield, 0f);
                    float absorbed = Math.max(beforePool - afterPool, 0f);

                    if(absorbed > 0.001f){
                        Float pending = pendingTransferredLoss.get(unit.id);
                        pendingTransferredLoss.put(unit.id, (pending == null ? 0f : pending) + absorbed);
                    }

                    redirectedThisFrame.put(other.id, alreadyRedirected + redirect);

                    if(transferEffect != Fx.none){
                        transferEffect.at(other.x, other.y, other.hitSize, unit.team.color, other);
                    }

                    if(absorbEffect != Fx.none){
                        absorbEffect.at(unit.x, unit.y, unit.hitSize, unit.team.color, unit);
                    }

                    if(!playedSound[0] && transferSound != Sounds.none){
                        transferSound.at(unit);
                        playedSound[0] = true;
                    }

                    currentHealth = Math.max(other.health, 0f);
                    currentShield = Math.max(other.shield, 0f);
                }
            }

            state.health = currentHealth;
            state.shield = currentShield;
        });

        stale.clear();
        for(IntMap.Entry<AllyState> entry : trackedAllies.entries()){
            if(!seen.contains(entry.key)){
                stale.add(entry.key);
            }
        }
        for(int i = 0; i < stale.size; i++){
            trackedAllies.remove(stale.get(i));
        }
    }

    protected void beginRedirectFrame(){
        long frameId = Core.graphics.getFrameId();
        if(redirectedFrame != frameId){
            redirectedFrame = frameId;
            redirectedThisFrame.clear();
        }
    }

    protected float getRedirectedThisFrame(int id){
        Float value = redirectedThisFrame.get(id);
        return value == null ? 0f : value;
    }

    protected float selfCapacity(Unit unit){
        return Math.max(unit.health + Math.max(unit.shield, 0f), 0f);
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(Core.bundle.format("stat.wh-range", Strings.autoFixed(range / tilesize, 2)));
        t.row();
        t.add(Core.bundle.format("stat.wh-escort-shield-redirect", Strings.autoFixed(redirectPercent * 100f, 2) + "%"));
        t.row();
        t.add(Core.bundle.format("stat.wh-escort-shield-self-reduction", Strings.autoFixed(selfDamageReduction * 100f, 2) + "%"));
        if(maxRedirect >= 0f){
            t.row();
            t.add(Core.bundle.format("stat.wh-escort-shield-max-redirect", Strings.autoFixed(maxRedirect, 2)));
        }
    }

    @Override
    public EscortShieldAbility copy(){
        EscortShieldAbility out = (EscortShieldAbility)super.copy();
        out.timer = new Interval();
        out.trackedAllies = new IntMap<>();
        out.seen = new IntSet();
        out.stale = new IntSeq();
        return out;
    }

    @Override
    public String localized(){
        return Core.bundle.get(getBundle());
    }

    @Override
    public String getBundle(){
        return "ability." + name("escort-shield-ability");
    }

    protected static class AllyState{
        float health;
        float shield;

        AllyState(float health, float shield){
            this.health = health;
            this.shield = shield;
        }
    }
}
