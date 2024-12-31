package wh.core;

import wh.entities.abilities.*;
import wh.entities.bullet.*;
import wh.entities.effect.*;
import wh.type.*;
import wh.type.unit.*;
import wh.world.blocks.defense.*;
import wh.world.blocks.defense.turrets.*;
import wh.world.blocks.distribution.*;
import wh.world.blocks.production.*;
import wh.world.blocks.storage.*;

import static mindustry.mod.ClassMap.*;

/** Generated class. Maps simple class names to concrete classes. For use in JSON attached mods. */
final class WHClassMap {
    /** WHClassMap should not be instantiated. */
    private WHClassMap() {}

    static void load() {
        //abilities
        classes.put("AdaptedHealAbility", AdaptedHealAbility.class);
        classes.put("MendFieldAbility", MendFieldAbility.class);
        classes.put("PointDefenseAbility", PointDefenseAbility.class);
        classes.put("ShockWaveAbility", ShockWaveAbility.class);
        //effect
        classes.put("WrapperEffect", WrapperEffect.class);
        //bullet
        classes.put("AccelBulletType", AccelBulletType.class);
        classes.put("BoidBulletType", BoidBulletType.class);
        classes.put("EffectBulletType", EffectBulletType.class);
        classes.put("LightningLinkerBulletType", LightningLinkerBulletType.class);
        classes.put("PositionLightningBulletType", PositionLightningBulletType.class);
        classes.put("ShieldBreakerType", ShieldBreakerType.class);
        classes.put("StrafeLaserBulletType", StrafeLaserBulletType.class);
        classes.put("TrailFadeBulletType", TrailFadeBulletType.class);
        //type
        classes.put("ExtraSectorPreset", ExtraSectorPreset.class);
        //type-unit
        classes.put("AncientUnitType", AncientUnitType.class);
        classes.put("AncientEngine", AncientUnitType.AncientEngine.class);
        classes.put("NucleoidUnitType", NucleoidUnitType.class);
        classes.put("PesterUnitType", PesterUnitType.class);
        //block
        classes.put("AirRaider", AirRaider.class);
        classes.put("AirRaiderBuild", AirRaider.AirRaiderBuild.class);
        classes.put("BombLauncher", BombLauncher.class);
        classes.put("BombLauncherBuild", BombLauncher.BombLauncherBuild.class);
        classes.put("SpeedupTurret", SpeedupTurret.class);
        classes.put("SpeedupTurretBuild", SpeedupTurret.SpeedupTurretBuild.class);
        classes.put("BeltConveyor", BeltConveyor.class);
        classes.put("BeltConveyorBuild", BeltConveyor.BeltConveyorBuild.class);
        classes.put("CoveredConveyor", CoveredConveyor.class);
        classes.put("CoveredConveyorBuild", CoveredConveyor.CoveredConveyorBuild.class);
        classes.put("TubeConveyor", TubeConveyor.class);
        classes.put("TubeConveyorBuild", TubeConveyor.TubeConveyorBuild.class);
        classes.put("UnloaderF", UnloaderF.class);
        classes.put("UnloaderBuildF", UnloaderF.UnloaderBuildF.class);
        classes.put("MultiCrafterE", MultiCrafterE.class);
        classes.put("MultiCrafterBuildE", MultiCrafterE.MultiCrafterBuildE.class);
        classes.put("Formula", MultiCrafterE.Formula.class);
        classes.put("MultiCrafterD", MultiCrafterD.class);
        classes.put("MultiCrafterBuildD", MultiCrafterD.MultiCrafterBuildD.class);
        classes.put("DirectionalUnloaderF", DirectionalUnloaderF.class);
        classes.put("DirectionalUnloaderBuildF", DirectionalUnloaderF.DirectionalUnloaderBuildF.class);
        classes.put("FrontlineCoreBlock", FrontlineCoreBlock.class);
        classes.put("FrontlineCoreBuild", FrontlineCoreBlock.FrontlineCoreBuild.class);
    }
}
