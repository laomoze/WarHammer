package wh.entities.abilities;

import arc.*;
import arc.audio.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.graphics.*;

import static mindustry.Vars.tilesize;


public class ShockWaveAbility extends Ability{
    protected static final Seq<Unit> all = new Seq<>();

    public ObjectFloatMap<StatusEffect> status = new ObjectFloatMap<>();

    public boolean targetGround = true, targetAir = true;
    public float x, y;

    public float reload = 500f;
    public float range = 400f;
    public float damage = 400f;

    public float knockback = 20f;
    public float rotKnock = 10f;

    public Color hitColor = WHPal.ShootOrangeLight;

    public Sound shootSound = WHSounds.shock;

    public Effect shootEffect = WHFx.lineCircleOut(30, 30, 3);
    public Effect hitEffect = WHFx.hitSparkLarge;

    public float maxSpeed = -1;

    public int boltNum = 2;
    public float boltWidth = 2;

    public ShockWaveAbility(float reload, float range, float damage, Color hitColor){
        this.reload = reload;
        this.range = range;
        this.damage = damage;
        this.hitColor = hitColor;
    }

    public Cons2<Position, Position> effect = (from, to) -> {
        PositionLightning.createEffect(from, to, hitColor, boltNum, boltWidth);
    };

    public ShockWaveAbility modify(Cons<ShockWaveAbility> m){
        m.get(this);

        return this;
    }

    public ShockWaveAbility status(Object... values){
        for(int i = 0; i < values.length / 2; i++){
            status.put((StatusEffect)values[i * 2], (Float)values[i * 2 + 1]);
        }

        return this;
    }

    @Override
    public void init(UnitType type){
        super.init(type);
        if(maxSpeed > 0)maxSpeed = maxSpeed * maxSpeed;
    }

    protected float timer = 0;

    @Override
    public void update(Unit unit){
        if(unit.disarmed)return;

        timer += Time.delta * unit.reloadMultiplier;

        if(maxSpeed > 0 && unit.vel().len2() > maxSpeed){
            timer = 0;
        }else if(timer > reload){
            all.clear();

            Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
            float rx = Tmp.v1.x, ry = Tmp.v1.y;

            Units.nearby(null, rx, ry, range, other -> {
                if(other.team != unit.team && other.checkTarget(targetAir, targetGround) && other.targetable(unit.team)){
                    all.add(other);
                }
            });

            if(all.any()){
                timer = 0;
                shootSound.at(rx, ry, 1 + Mathf.range(0.15f), 3);

                shootEffect.at(rx, ry, range, hitColor);
                for(Unit u : all){
                    for(ObjectFloatMap.Entry<StatusEffect> s : status.entries()){
                        u.apply(s.key, s.value);
                    }

                    Tmp.v3.set(unit).sub(Tmp.v1).nor().scl(knockback * 80f);
                    u.impulse(Tmp.v3);
                    u.damage(damage);
                    hitEffect.at(u.x, u.y, hitColor);
                    effect.get(Tmp.v1, u);
                }
            }
        }
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(Core.bundle.format("bullet.damage", damage));
        t.row();
        t.add(abilityStat("firingrate", Strings.autoFixed(60f / reload, 2)));
        t.row();
        t.add(Core.bundle.format("bullet.range", Strings.autoFixed(range / tilesize, 2)));
        if(status != null && status.size > 0){
            for(StatusEffect statu : status.keys()){
                t.row();
                t.add((statu.hasEmoji() ? statu.emoji() : "") + "[stat]" + statu.localizedName).
                with(l -> StatValues.withTooltip(l, statu));
            }
        }
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);
    }

    @Override
    public String localized(){
        return super.localized();
    }
}
