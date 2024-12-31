package wh.content;

import mindustry.graphics.*;
import mindustry.type.*;

public final class WHStatusEffects {
    public static StatusEffect ultFireBurn, assault, bless, distort, energyAmplification, forcesOfChaos, melta, palsy, plasma;

    private WHStatusEffects() {}

    public static void load() {
        ultFireBurn = new StatusEffect("ult-fire-burn") {{
            color = Pal.techBlue;
            damage = 15f;
            speedMultiplier = 1.2f;
            effect = WHFx.ultFireBurn;
        }};
        assault = new StatusEffect("assault");
        bless = new StatusEffect("bless");
        distort = new StatusEffect("distort");
        energyAmplification = new StatusEffect("energy-amplification");
        forcesOfChaos = new StatusEffect("forces-of-chaos");
        melta = new StatusEffect("melta");
        palsy = new StatusEffect("palsy");
        plasma = new StatusEffect("plasma");
    }
}
