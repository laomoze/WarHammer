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

        registerStatement("linetarget", LineTarget::new, LineTarget::new);
        registerStatement("randspawn", RandomSpawn::new, RandomSpawn::new);
        registerStatement("randtarget", RandomTarget::new, RandomTarget::new);
        registerStatement("teamthreat", TeamThreat::new, TeamThreat::new);
        registerStatement("raidcontrol", RaidControl::new, RaidControl::new);
        registerStatement("defaultraid", DefaultRaid::new, DefaultRaid::new);

        registerStatement("wh-raid-unit", WHRaidUnitStatement::new, WHRaidUnitStatement::new);
        registerStatement("wh-spawner-unit", WHSpawnerStatement::new, WHSpawnerStatement::new);
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
