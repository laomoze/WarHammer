package wh.gen;

import arc.func.*;
import arc.struct.*;
import mindustry.gen.*;
import wh.entities.world.entities.*;
import wh.entities.world.entities.powerArmorComp.*;

public final class EntityRegister {
    private static final ObjectIntMap<Class<? extends Entityc>> ids = new ObjectIntMap<>();
    private static final ObjectMap<String, Prov<? extends Entityc>> map = new ObjectMap<>();

    /** EntityRegister should not be instantiated. */
    private EntityRegister() {}

    public static <T extends Entityc> Prov<T> get(Class<T> type) {
        return get(type.getCanonicalName());
    }
    public static <T extends Entityc> Prov<T> get(String name) {
        //noinspection unchecked
        return (Prov<T>) map.get(name);
    }
    public static <T extends Entityc> void register(String name, Class<T> type, Prov<? extends T> prov) {
        map.put(name, prov);
        ids.put(type, EntityMapping.register(name, prov));
    }

    public static int getId(Class<? extends Entityc> type) {
        return ids.get(type, -1);
    }

    public static void load() {
        register("PlaFire", PlasmaFire.class, PlasmaFire::new);
        register("EnergyUnit", EnergyUnit.class, EnergyUnit::new);
        register("PesterUnit", PesterUnit.class, PesterUnit::new);
        register("NucleoidUnit", NucleoidUnit.class, NucleoidUnit::new);
        register("AirRaiderUnit", AirRaiderUnitType.class, AirRaiderUnitType::new);
        register("StarrySkyUnit", StarrySkyEntity.class, StarrySkyEntity::new);
        register("HoverPayloadUnit", HoverPayloadUnit.class, HoverPayloadUnit::new);
        register("TitanUnit", TitanUnit.class, TitanUnit::create);
        register("PowerArmourUnit", PowerArmourUnit.class, PowerArmourUnit::new);
    }
}