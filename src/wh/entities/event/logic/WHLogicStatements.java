package wh.entities.event.logic;

import arc.func.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import wh.entities.event.logic.actionLogic.*;

/**
 * 事件逻辑语句注册中心。
 * 在这里集中注册自动触发、刷怪、以及拆分后的 action 语句。
 */
public class WHLogicStatements{
    public static LCategory autoTriggerCategory;

    public static void load(){
        autoTriggerCategory = new LCategory("wh-autotrigger", Pal.surge.cpy().lerp(Pal.gray, 0.25f));

        registerStatement("wh-raidcontrol", RaidControl::new, RaidControl::new);
        registerStatement("wh-defaultraids", DefaultRaids::new, DefaultRaids::new);
        registerStatement("defaultairraid", DefaultAirborneRaid::new, DefaultAirborneRaid::new);

        registerStatement("wh-raid-unit", WHRaidUnitStatement::new, WHRaidUnitStatement::new);

        // Backward-compat parser for old typo opcode (write() used to emit this).
        LAssembler.customParsers.put("wh-default-airborn", DefaultAirborneRaid::new);
        ActionStatements.load();
    }

    /**
     * 同时注册文本解析器与语句提供器。
     */
    public static void registerStatement(String name, Func<String[], LStatement> parser, Prov<LStatement> provider){
        LAssembler.customParsers.put(name, parser);
        LogicIO.allStatements.addUnique(provider);
    }
}
