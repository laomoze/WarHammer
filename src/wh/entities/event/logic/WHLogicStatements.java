package wh.entities.event.logic;

import arc.func.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;

public class WHLogicStatements{
    public static LCategory autoTriggerCategory;

    public static void load(){
        autoTriggerCategory = new LCategory("wh-autotrigger", Pal.surge.cpy().lerp(Pal.gray, 0.25f));

        registerStatement("wh-raid-unit", WHRaidUnitStatement::new, WHRaidUnitStatement::new);
        registerStatement("wh-spawner-unit", WHSpawnerStatement::new, WHSpawnerStatement::new);
    }

    public static void registerStatement(String name, Func<String[], LStatement> parser, Prov<LStatement> provider){
        LAssembler.customParsers.put(name, parser);
        LogicIO.allStatements.addUnique(provider);
    }
}
