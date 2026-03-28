
package wh.entities;

import arc.*;
import arc.struct.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import wh.entities.world.blocks.defense.AirRaiderCallBlock.*;
import wh.entities.world.blocks.defense.CommandableBlock.*;

public class WorldRegister {
    public static final Seq<Runnable> afterLoad = new Seq<>();
    public static final Seq<CommandableBlockBuild> commandableBuilds = new Seq<>();
    public static final Seq<AirRaiderUnitBuild> ARBuilds = new Seq<>();

    private static final ObjectMap<Team, ObjectIntMap<Block>> teamBlockCounts = new ObjectMap<>();
    private static final ObjectIntMap<Block> blockCounts = new ObjectIntMap<>();

    public static boolean worldLoaded = false;

    private WorldRegister() {
    }


    public static void clear() {
        commandableBuilds.clear();
        ARBuilds.clear();
        teamBlockCounts.clear();
        blockCounts.clear();
    }

    public static void registerBuild(Building build){
        if(build == null || build.block == null || build.team == null) return;

        blockCounts.increment(build.block, 1);

        ObjectIntMap<Block> teamMap = teamBlockCounts.get(build.team);
        if(teamMap == null){
            teamMap = new ObjectIntMap<>();
            teamBlockCounts.put(build.team, teamMap);
        }
        teamMap.increment(build.block, 1);
    }

    public static void unregisterBuild(Building build){
        if(build == null || build.block == null || build.team == null) return;

        int total = Math.max(0, blockCounts.get(build.block, 0) - 1);
        if(total <= 0){
            blockCounts.remove(build.block, 0);
        }else{
            blockCounts.put(build.block, total);
        }

        ObjectIntMap<Block> teamMap = teamBlockCounts.get(build.team);
        if(teamMap != null){
            int next = Math.max(0, teamMap.get(build.block, 0) - 1);
            if(next <= 0){
                teamMap.remove(build.block, 0);
            }else{
                teamMap.put(build.block, next);
            }
            if(teamMap.isEmpty()){
                teamBlockCounts.remove(build.team);
            }
        }
    }

    public static int blockCount(Block block){
        return block == null ? 0 : blockCounts.get(block, 0);
    }

    public static int teamBlockCount(Team team, Block block){
        if(team == null || block == null) return 0;
        ObjectIntMap<Block> teamMap = teamBlockCounts.get(team);
        return teamMap == null ? 0 : teamMap.get(block, 0);
    }

    public static void load() {
        Events.on(EventType.ResetEvent.class, (e) -> {
            WorldRegister.clear();
            worldLoaded = true;
        });
    }
}
