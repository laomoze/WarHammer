package wh.pipelinePlanet.core;

import arc.math.*;
import arc.struct.*;
import mindustry.type.*;
import mindustry.world.*;
import wh.pipelinePlanet.data.*;

/**
 * 中文说明：单次地图生成上下文，封装 tiles、sector、随机源与房间锚点。
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
