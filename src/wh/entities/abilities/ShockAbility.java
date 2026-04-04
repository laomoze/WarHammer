package wh.entities.abilities;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import static mindustry.Vars.tilesize;
import static wh.core.WarHammerMod.name;

/** 定期释放震爆波，清空周围敌方子弹或削减其伤害。 */
public class ShockAbility extends Ability{
    public float range = 110f;
    public float reload = 60f * 1.5f;
    public float bulletDamage = 160;
    // 目标越多，每个目标分到的伤害越低。
    public float falloffCount = 20f;
    public float shake = 2f;

    public Sound shootSound = Sounds.explosion;
    public Color waveColor = Pal.accent;
    public Effect hitEffect = Fx.hitSquaresColor;
    public Effect waveEffect = Fx.pointShockwave;

    // 临时缓存本轮会被震爆波命中的子弹。
    public Seq<Bullet> targets = new Seq<>();

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(abilityStat("firingrate", Strings.autoFixed(60f / reload, 2)));
        t.row();
        t.add(Core.bundle.format("bullet.range", Strings.autoFixed(range / tilesize, 2)));
        t.row();
        t.add(Core.bundle.format("bullet.damage", bulletDamage));
    }


    @Override
    public void update(Unit unit){
        super.update(unit);
        // data 在这里表示震爆波冷却进度。
        if((data += Time.delta * unit.reloadMultiplier) >= reload){
            targets.clear();
            Groups.bullet.intersect(unit.x - range, unit.y - range, range * 2, range * 2, b -> {
                if(b.team != unit.team && b.type.hittable){
                    targets.add(b);
                }
            });

            if(targets.size > 0){
                data = 0f;
                waveEffect.at(unit.x, unit.y, range, waveColor, unit);
                shootSound.at(unit);
                Effect.shake(shake, shake, unit);
                float waveDamage = Math.min(bulletDamage, bulletDamage * falloffCount / targets.size);

                for(var target : targets){
                    if(target.damage > waveDamage){
                        target.damage -= waveDamage;
                    }else{
                        target.remove();
                    }
                    hitEffect.at(target.x, target.y, waveColor);
                }
            }
        }
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability." + name("ShockAbility"));
    }

    @Override
    public ShockAbility copy(){
        ShockAbility out = (ShockAbility)super.copy();
        out.data = 0f;
        out.targets = new Seq<>();
        return out;
    }
}

