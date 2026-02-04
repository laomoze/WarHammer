package wh.gen;

import arc.graphics.g2d.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.entities.effect.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import wh.content.*;
import wh.entities.bullet.*;
import wh.entities.world.drawer.part.*;
import wh.entities.world.entities.*;
import wh.graphics.*;

import static wh.core.WarHammerMod.name;

public class TankEn2UnitType extends WHUnitType{
    public TankEn2UnitType(String name){
        super(name);
    }

    public Weapon coaxialWeapon = new Weapon(name("tankEn2-weapon2")){{
        layerOffset = 1.1f;//shit
        y = -12 / 4f;
        x = 0;
        reload = 6;
        mirror = false;
        shootSound = WHSounds.machineGunShoot;
        inaccuracy = 4f;
        velocityRnd = 0.15f;
        xRand = 0.3f;
        recoils = 2;
        recoil = 0;
        heatColor = WHPal.thurmixRed;

        float basex = -22 / 4f, basey = 0;

        shootY = 79 / 4f;
        shootX = basex;

        rotate = true;
        rotateSpeed = 2f;

        shoot = new ShootAlternate(){{
            barrels = 3;
            spread = 10 / 4f;
        }};


        float x1 = -12 / 4f, x2 = 0 / 4f, x3 = 12 / 4f,
        move = 12 / 4f, y1 = 64 / 4f;

        parts.addAll(
        new BarrelPart("-barrel"){{
            x = x2 + basex;
            y = y1 + basey;
            intervalWidth = 9 / 4f;
            recoilProgress = reloadProgress = heatProgress = progress = PartProgress.reload;
            heatColor = WHPal.thurmixRed;
        }}

        /*new RegionPart("-barrel"){{
            layerOffset = -0.001f;
            x = x1 + basex;
            y = y1 + basey;
            heatProgress = progress = PartProgress.reload;
            heatColor = WHPal.thurmixRed;
            moveX = move;
            recoilIndex = 0;
        }},
        new RegionPart("-barrel"){{
            layerOffset = -0.002f;
            x = x2 + basex;
            y = y1 + basey;
            heatProgress = progress = PartProgress.reload;
            heatColor = WHPal.thurmixRed;
            moveX = move;
            recoilIndex = 1;
        }},
        new RegionPart("-barrel"){{
            layerOffset = -0.003f;
            x = x3 + basex;
            y = y1 + basey;
            heatProgress = progress = PartProgress.reload;
            heatColor = WHPal.thurmixRed;
            moveX = -2 * move;
            recoilIndex = 2;
        }}*/

        );

        bullet = new CritBulletType(16f, 90, name("pierce")){{
            pierceArmor = true;
            lifetime = 300 / speed;
            keepVelocity = false;
            splashDamage = damage / 2;
            pierceCap = 3;
            width = 5;
            height = width * 2;
            trailWidth = width / 3;
            trailLength = 3;
            shrinkX = shrinkY = 0;
            frontColor = WHPal.ShootOrangeLight;
            trailColor = hitColor = backColor = lightColor = lightningColor = WHPal.ShootOrange;
            mixColorFrom = mixColorTo = hitColor;
            shootEffect = new MultiEffect(
            WHFx.shootLine(10, 20),
            Fx.shootBig);
            hitEffect = despawnEffect = new MultiEffect(
            WHFx.square(hitColor, 35, 10, splashDamageRadius, 6),
            WHFx.generalExplosion(7, hitColor, 20, 3, false),
            WHFx.instHit(hitColor, true, 2, splashDamageRadius)
            );
            hitSound = Sounds.none;
        }};
    }};

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        coaxialWeapon.parts.forEach(part -> part.getOutlines(out));
        super.getRegionsToOutline(out);
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
    }

    @Override
    public void load(){
        super.load();
        coaxialWeapon.load();
    }
}
