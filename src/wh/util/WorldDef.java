package wh.util;

import arc.math.geom.Vec2;
import arc.struct.IntSeq;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.world.Tile;

import static mindustry.Vars.world;

public class WorldDef {

    public static boolean toBlock(Building block, Building other) {
        return !other.block.rotate || other.relativeTo(block) == other.rotation;
    }

    public static Seq<Tile> getAreaTile(Vec2 pos, int width, int height) {
        Seq<Tile> tilesGet = new Seq<>();
        int dx = (int) pos.x;
        int dy = (int) pos.y;
        for (int ix = 0; ix < width; ix++) {
            for (int iy = 0; iy < height; iy++) {
                Tile other = world.tile(ix + dx + 1, iy + dy + 1);
                if (other != null) tilesGet.add(other);
            }
        }
        return tilesGet;
    }

    public static <T> Seq<T> listItem(Seq<T> array) {
        Seq<T> result = new Seq<>();
        array.each(a -> {
            if (!result.contains(a) && a != null) {
                result.add(a);
            }
        });
        return result;
    }


}
