package wh.gen;

import arc.func.Prov;
import arc.struct.ObjectIntMap;
import arc.struct.ObjectMap;
import mindustry.gen.EntityMapping;
import mindustry.gen.Entityc;
import wh.content.WHBulletsOther.TrailBullet;
import wh.entities.AirborneSpawner;
import wh.entities.RiftSpawner;
import wh.entities.Spawner;
import wh.entities.bullet.ApproachBullet.AB;
import wh.entities.event.Trigger;
import wh.entities.world.entities.AirRaiderUnitType;
import wh.entities.world.entities.powerArmorComp.PowerArmourUnit;

public final class EntityRegister{
    private static final ObjectIntMap<Class<? extends Entityc>> ids = new ObjectIntMap<>();
    private static final ObjectMap<String, Prov<? extends Entityc>> map = new ObjectMap<>();

    /** EntityRegister should not be instantiated. */
    private EntityRegister(){
    }

    public static <T extends Entityc> Prov<T> get(Class<T> type){
        return get(type.getCanonicalName());
    }

    public static <T extends Entityc> Prov<T> get(String name){
        //noinspection unchecked
        return (Prov<T>)map.get(name);
    }

    public static <T extends Entityc> void register(String name, Class<T> type, Prov<? extends T> prov){
        map.put(name, prov);
        ids.put(type, EntityMapping.register(name, prov));
    }

    public static int getId(Class<? extends Entityc> type){
        return ids.get(type, -1);
    }

    public static void load(){
        register("PlaFire", PlasmaFire.class, PlasmaFire::new);
        register("AirRaiderUnit", AirRaiderUnitType.class, AirRaiderUnitType::new);

        register("HoverPayloadUnit", HoverPayloadUnit.class, HoverPayloadUnit::new);
        register("CarrierPayloadUnit", CarrierPayloadUnit.class, CarrierPayloadUnit::new);
        register("TankEn2Unit", TankEn2Unit.class, TankEn2Unit::new);

        register("TitanUnit", TitanUnit.class, TitanUnit::create);
        register("PowerArmourUnit", PowerArmourUnit.class, PowerArmourUnit::new);

        register("RevengeUnit", RevengeUnit.class, RevengeUnit::new);
        register("GeminiUnit", GeminiUnit.class, GeminiUnit::new);
        register("CarrierFighterUnit", CarrierFighterUnit.class, CarrierFighterUnit::new);

        register("ApproachB", AB.class, AB::new);
        register("TrailBullet", TrailBullet.class, TrailBullet::new);
        register("Spawner", Spawner.class, Spawner::new);
        register("AirborneSpawner", AirborneSpawner.class, AirborneSpawner::new);
        register("RiftSpawner", RiftSpawner.class, RiftSpawner::new);

        register("PortableAutoEventTrigger", Trigger.class, Trigger::new);

        /* register("StarrySkyUnit", StarrySkyEntity.class, StarrySkyEntity::new);*/

    }
}
