//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.core;

import mindustry.mod.*;
import wh.content.*;
import wh.entities.abilities.*;
import wh.entities.bullet.*;
import wh.entities.bullet.laser.*;
import wh.entities.world.blocks.defense.*;
import wh.entities.world.blocks.defense.turrets.*;
import wh.entities.world.blocks.storage.*;
import wh.entities.world.drawer.part.*;
import wh.entities.world.entities.*;
import wh.util.*;

final class WHClassMap {

    private WHClassMap() {
    }

    static void load() {
        ClassMap.classes.put("AdaptedHealAbility", AdaptedHealAbility.class);
        ClassMap.classes.put("MendFieldAbility", MendFieldAbility.class);
        ClassMap.classes.put("ShockWaveAbility", ShockWaveAbility.class);
        ClassMap.classes.put("EffectBulletType", EffectBulletType.class);
        ClassMap.classes.put("BlackHoleBulletType", BlackHoleBulletType.class);
        ClassMap.classes.put("LightningLinkerBulletType", LightningLinkerBulletType.class);
        ClassMap.classes.put("ChainLightingBulletType", ChainLightingBulletType.class);
        ClassMap.classes.put("PositionLightningBulletType", PositionLightningBulletType.class);
        ClassMap.classes.put("ShieldBreakerType", ShieldBreakerType.class);
        ClassMap.classes.put("TrailFadeBulletType", TrailFadeBulletType.class);
        ClassMap.classes.put("SuperHeavyUnitType", SuperHeavyUnitType.class);
        ClassMap.classes.put("AncientEngine", AncientEngine.class);
        ClassMap.classes.put("NucleoidUnitType", NucleoidUnitType.class);
        ClassMap.classes.put("PesterUnitType", PesterUnitType.class);
        ClassMap.classes.put("AirRaider", AirRaider.class);
        ClassMap.classes.put("AirRaiderBuild", AirRaider.AirRaiderBuild.class);
        ClassMap.classes.put("BombLauncher", BombLauncher.class);
        ClassMap.classes.put("BombLauncherBuild", BombLauncher.BombLauncherBuild.class);
        ClassMap.classes.put("SpeedupTurret", SpeedupTurret.class);
        ClassMap.classes.put("SpeedupTurretBuild", SpeedupTurret.SpeedupTurretBuild.class);
        ClassMap.classes.put("CritBulletType", CritBulletType.class);
        ClassMap.classes.put("CritMissileBulletType", CritMissileBulletType.class);

        ClassMap.classes.put("UnloaderF", UnloaderF.class);
        ClassMap.classes.put("UnloaderBuildF", UnloaderF.UnloaderBuildF.class);

        ClassMap.classes.put("FrontlineCoreBlock", FrontlineCoreBlock.class);
        ClassMap.classes.put("FrontlineCoreBuild", FrontlineCoreBlock.FrontlineCoreBuild.class);

        ClassMap.classes.put("WHFx", WHFx.class);
        ClassMap.classes.put("DrawArrowSequence", DrawArrowSequence.class);
        ClassMap.classes.put("WorldDef", WorldDef.class);
    }
}
