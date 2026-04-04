package wh.entities.bullet;


import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import wh.entities.world.blocks.defense.turrets.*;
import wh.graphics.*;


public class HealCone extends BulletType{
    public float findRange;
    public float findAngle;
    public boolean percentHeal;
    public float healAmount = 20;
    public Color healColor = Pal.heal;
    public boolean healUnit = true;

    public HealCone(float findAngle, float findRange){
        this(findAngle, findRange, true);
    }

    public HealCone(float findAngle, float findRange, boolean percentHeal){
        this.findAngle = findAngle;
        this.findRange = findRange;
        this.percentHeal = percentHeal;
    }

    {
        speed = 0;
        damage = 0;
        collides = false;
        collidesGround = collidesAir = true;
        absorbable = false;
        hittable = false;
        keepVelocity = false;
        despawnEffect = shootEffect = smokeEffect = Fx.none;
        healPercent = 12;
        drawSize = findRange;
        maxRange = range = findRange;
    }

    public HealCone(){
        this(45, 160);
        maxRange = range = findRange;
    }

    @Override
    protected float calculateRange(){
        return findRange;
    }

    @Override
    public void update(Bullet b){
        float ratio = 60 * 100;

        float in = b.time < b.lifetime - 10 ? Math.min(1, b.time / 10) : (b.lifetime - b.time) / 10;
        in = Interp.fastSlow.apply(in);

        float amountMt = b.owner instanceof MendTurret.MendTurretBuild mt ? mt.amountMti() : 1;
        float angleMt = (b.owner instanceof MendTurret.MendTurretBuild mt ? mt.angleMti() : 1) * in;
        if(healUnit) Units.nearby(b.team, b.x, b.y, findRange * in, unit -> {
            if(unit.damaged() && Angles.within(b.rotation(), b.angleTo(unit), (findAngle * angleMt) / 2) && unit != b.owner){
                if(percentHeal) unit.heal((unit.maxHealth < 1000 ? 1000 : unit.maxHealth) * ((healPercent * amountMt) / ratio) * Time.delta);
                unit.heal((healAmount * amountMt) / 60 * Time.delta);
            }
        });
        boolean healFx = b.timer.get(30);
        Vars.indexer.eachBlock(b, findRange * in,
        other -> other.health < other.maxHealth - 0.001f && Angles.within(b.rotation(), b.angleTo(other), (findAngle * angleMt) / 2),
        other -> {
            if(percentHeal) other.heal((healPercent / ratio) * other.maxHealth * Time.delta);
            other.heal((healAmount * amountMt) / 60 * Time.delta);
            if(healFx && other.block != null){
                Fx.healBlockFull.at(other.x, other.y, 0, Pal.heal, other.block);
            }
        });
    }

    private static final Blending noStackBlend = new Blending(Gl.one, Gl.one, Gl.one, Gl.one);

    @Override
    public void draw(Bullet b){
        float in = b.time < b.lifetime - 10 ? Math.min(1, b.time / 10) : (b.lifetime - b.time) / 10;
        in = Interp.fastSlow.apply(in);
        float angleMt = b.data instanceof Float f ? f : 1;
        float range = findRange * in;
        float angle = findAngle * angleMt * in;

        Draw.color(healColor);
        Draw.z(Layer.buildBeam);
        Draw.reset();
        Draw.alpha(0.7f);
        Draw.blend(Blending.disabled);
        Draw.color(healColor);
        Fill.circle(b.x, b.y, 4 * in);
        Fill.arc(b.x, b.y, range, angle / 360, b.rotation() - angle / 2);
        Draw.blend();
        Drawn.wireCube(b.x, b.y, 10f * in, Time.time + 10f * in, 1.8f, healColor);
        Draw.alpha(1);
    }
}
