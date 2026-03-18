package wh.pipelinePlanet.core;

import arc.math.*;
import arc.struct.*;
import wh.pipelinePlanet.data.*;
import mindustry.type.*;
import mindustry.world.*;

/**
 * 单次地图生成执行期间的上下文。
 * pass 间共享状态统一放在这里，避免隐式全局耦合。
 */
public class GenContext{
    public final Tiles tiles;
    public final Sector sector;
    public final Rand rand;
    public final GenConfig cfg;
    public final int seed;
    public final int baseSeed;

    public RoomAnchor spawnRoom;
    public final Seq<RoomAnchor> enemyRooms = new Seq<>();
    public final Seq<RoomAnchor> allRooms = new Seq<>();

    public GenContext(Tiles tiles, Sector sector, Rand rand, GenConfig cfg, int seed, int baseSeed){
        this.tiles = tiles;
        this.sector = sector;
        this.rand = rand;
        this.cfg = cfg;
        this.seed = seed;
        this.baseSeed = baseSeed;
    }

    public int width(){
        return tiles.width;
    }

    public int height(){
        return tiles.height;
    }
}
