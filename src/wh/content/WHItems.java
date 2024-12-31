package wh.content;

import mindustry.type.*;

@SuppressWarnings("unused")
public final class WHItems {
    public static Item titaniumSteel, imperium, vibranium, ceramite, refineCeramite, adamantium;

    /** WHItems should not be instantiated. */
    private WHItems() {}

    /**
     * Instantiates all contents. Called in the main thread in {@link wh.core.WarHammerMod#loadContent()}.
     * <p>Remember not to execute it a second time, I did not take any precautionary measures.
     */
    public static void load() {
        titaniumSteel = new Item("titanium-steel");
        imperium = new Item("imperium");
        vibranium = new Item("vibranium");
        ceramite = new Item("ceramite");
        refineCeramite = new Item("refine-ceramite");
        adamantium = new Item("adamantium");
    }
}
