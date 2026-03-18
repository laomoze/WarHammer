package wh.entities.world.entities.powerArmorComp;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import wh.entities.world.entities.*;

public class PowerArmourUnitType extends WHUnitType{

    public static final float shadowTX = -12f, shadowTY = -13f;
    public boolean drawLinkedPartsInUnitType = true;
    public int bodyMoveLockWeaponIndex = 0;

    public PowerArmourUnitType(String name){
        super(name);
        constructor = PowerArmourUnit::new;
        drawBody = false;
        shadowElevation = 0.25f;
    }

    @Override
    public void load(){
        super.load();
        for(Weapon weapon : weapons){
            PowerArmourWeaponData data = PowerArmourWeaponData.get(weapon);
            if(data != null){
                for(DrawUnitPart part : data.unitParts){
                    part.turretShading = false;
                    part.load(name);
                }
            }
        }
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out){
        super.getRegionsToOutline(out);
        for(Weapon weapon : weapons){
            PowerArmourWeaponData data = PowerArmourWeaponData.get(weapon);
            if(data != null){
                for(DrawUnitPart part : data.unitParts){
                    part.getOutlines(out);
                }
            }
        }
    }

    @Override
    public void drawWeapons(Unit unit){
        super.drawWeapons(unit);
        if(!drawLinkedPartsInUnitType) return;
        if(!(unit instanceof PowerArmourUnit powerUnit)) return;

        int mountCount = Math.min(unit.mounts.length, weapons.size);
        for(int wi = 0; wi < mountCount; wi++){
            Weapon weapon = weapons.get(wi);
            PowerArmourWeaponData data = PowerArmourWeaponData.get(weapon);
            if(data == null || data.unitParts.isEmpty()) continue;
            WeaponMount mount = unit.mounts[wi];

            float rotation = unit.rotation - 90f;
            float realRecoil = Mathf.pow(mount.recoil, weapon.recoilPow) * weapon.recoil;
            float weaponRotation = rotation + (weapon.rotate ? mount.rotation : weapon.baseRotation);

            for(int i = 0; i < data.unitParts.size; i++){
                DrawUnitPart part = data.unitParts.get(i);
                int sourceIndex = part.weaponIndex >= 0 && part.weaponIndex < unit.mounts.length ? part.weaponIndex : wi;
                WeaponMount source = unit.mounts[sourceIndex];
                PowerArmourWeaponData sourceData = sourceIndex < weapons.size ? PowerArmourWeaponData.get(weapons.get(sourceIndex)) : null;
                PowerArmourUnit.WeaponAnimState sourceState = powerUnit.weaponAnimState(sourceIndex);

                float partWarmup = source.warmup;
                float partReload = source.weapon.reload <= 0f ? 0f : source.reload / source.weapon.reload;
                float partSmoothReload = source.smoothReload;
                float partHeat = source.heat;
                float partRecoil = source.recoil;
                float partCharge = source.charge;

                float partAction = 1f;
                float partSmoothHeat = partHeat;
                if(sourceData != null && sourceData.melee && sourceState != null){
                    partAction = sourceState.actionInterpProgress;
                    partSmoothHeat = sourceState.smoothHeat;
                }

                DrawUnitPart.params.set(powerUnit, this, partWarmup, partReload, partSmoothReload, partSmoothHeat,
                partHeat, partRecoil, partCharge, partAction, powerUnit.x, powerUnit.y, weaponRotation + 90f);
                DrawUnitPart.params.sideMultiplier = weapon.flipSprite ? -1 : 1;

                if(part.recoilIndex >= 0 && source.recoils != null && part.recoilIndex < source.recoils.length){
                    DrawUnitPart.params.setRecoil(source.recoils[part.recoilIndex]);
                }else{
                    DrawUnitPart.params.setRecoil(partRecoil);
                }

                applyColor(unit);
                part.draw(DrawUnitPart.params);
            }
        }
    }

    @Override
    public void drawShadow(Unit unit){
        float elevation = Math.max(unit.elevation, shadowElevation);
        float shadowSize = hitSize * (1.35f + elevation * 0.2f);
        Drawf.shadow(unit.x + shadowTX * elevation, unit.y + shadowTY * elevation, shadowSize);
    }

    @Override
    public void drawCell(Unit unit){
    }
}
