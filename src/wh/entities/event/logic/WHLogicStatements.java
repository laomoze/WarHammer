package wh.entities.event.logic;

import arc.func.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;

public class WHLogicStatements{
    public static LCategory autoTriggerCategory;

    private static boolean loaded = false;

    public static void load(){
        if(loaded) return;
        loaded = true;

        autoTriggerCategory = new LCategory("wh-autotrigger", Pal.heal.cpy().lerp(Pal.gray, 0.25f));

        registerStatement("wh-raid-unit", WHRaidUnitStatement::new, WHRaidUnitStatement::new);
        // compatibility for old saved scripts that used the previous statement token.
        LAssembler.customParsers.put("whraidunit", WHRaidUnitStatement::new);
    }

    public static void registerStatement(String name, Func<String[], LStatement> parser, Prov<LStatement> provider){
        LAssembler.customParsers.put(name, parser);
        LogicIO.allStatements.addUnique(provider);
    }
}
