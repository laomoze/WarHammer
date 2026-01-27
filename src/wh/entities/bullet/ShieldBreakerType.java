package wh.entities.bullet;

import arc.graphics.*;
import arc.math.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import wh.content.*;


public class ShieldBreakerType extends BasicBulletType{
    public float fragSpawnSpacing = 5;
    public float maxShieldDamage;

    protected static BulletType breakType = new EffectBulletType(3f){{
        this.absorbable = true;
        collides = false;
        lifetime = 8f;
        drawSize = 0;
        this.damage = 1;
    }
        @Override
        public void despawned(Bullet b){
            if(b.absorbed && b.data instanceof Color){
                WHFx.shuttle.at(b.x, b.y, Mathf.random(360f), (Color)b.data, b.damage / Vars.tilesize / 2f);
                Effect.shake(b.damage / 100, b.damage / 100, b);
                Sounds.explosionArtilleryShock.at(b);
            }
        }
    };

    public ShieldBreakerType(float speed, float damage, String bulletSprite, float shieldDamage) {
        super(speed, damage, bulletSprite);
        this.splashDamage = this.splashDamageRadius = -1f;
        this.maxShieldDamage = shieldDamage;
        this.absorbable = false;
    }

    public ShieldBreakerType(float speed, float damage, float shieldDamage) {
        this(speed, damage, "bullet", shieldDamage);
    }

    public ShieldBreakerType() {
        this(1.0F, 1.0F, "bullet", 500f);
    }

    @Override
    public void init(){
        super.init();
    }

    @Override
    public void update(Bullet b) {
        super.update(b);
        if(b.timer(5, fragSpawnSpacing))breakType.create(b, b.team, b.x, b.y, 0, maxShieldDamage, 0, 1, backColor);
    }
}
