package wh.entities.world.entities;

import arc.graphics.Color;
import mindustry.graphics.Pal;
import wh.graphics.WHPal;

public class CMoonUnitType extends SuperHeavyUnitType {
    public float voidShieldCapacity = 300f;
    public float voidShieldRegen = 15f;
    public float voidShieldDamagePerSecond = 30;
    public float voidShieldFreeDamage = 300;
    public float voidShieldMediumDamage = 600;
    public float voidShieldFreeCost = 0f;
    public float voidShieldMediumCost = 0.5f;
    public float voidShieldLargeCost = 1f;
    public float voidShieldLongAxis = 270f;
    public float voidShieldMinorAxis = 155f;
    public float voidShieldRotationOffset = 0f;
    public float voidShieldOverloadDuration = 180f;
    public float voidShieldRecoveryDuration = 240f;
    public float lastStandRange = 500;
    public float lastStandHealth = 100_000f;
    public int lastStandUnits = 10;
    public Color voidShieldColor = WHPal.SkyBlue.cpy().lerp(Pal.techBlue, 0.35f);

    public CMoonUnitType(String name) {
        super(name);
    }
}
