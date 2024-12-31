package wh.content;

import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.environment.*;
import wh.entities.bullet.*;
import wh.graphics.*;
import wh.world.blocks.distribution.*;

@SuppressWarnings("unused")
public final class WHBlocks {
    public static Block
            promethium, vibraniumOre,

            steelDust,

            flash;

    /** WHBlocks should not be instantiated. */
    private WHBlocks() {}

    /**
     * Instantiates all contents. Called in the main thread in {@link wh.core.WarHammerMod#loadContent()}.
     * <p>Remember not to execute it a second time, I did not take any precautionary measures.
     */
    public static void load() {
        promethium = new Floor("promethium");
        vibraniumOre = new OreBlock("vibranium-ore");

        steelDust = new CoveredConveyor("steel-dust");

        flash = new PowerTurret("Flash") {{
            shootType = new PositionLightningBulletType(50f) {{
                maxRange = 300;
                rangeOverride = 300;
                lightningColor = WHPal.rim3;
                lightningDamage = 50;
                lightning = 3;
                lightningLength = 6;
                lightningLengthRand = 6;
            }};
        }};
    }
}
