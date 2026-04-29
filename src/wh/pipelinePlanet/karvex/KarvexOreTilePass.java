package wh.pipelinePlanet.karvex;

import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;
import wh.content.WHBlocksEnvironment;
import wh.pipelinePlanet.core.GenContext;
import wh.pipelinePlanet.core.TilePass;

/**
 * Optional tile-level ore painter used by decoration stages.
 */
public class KarvexOreTilePass implements TilePass{
    @Override
    public String name(){
        return "KarvexOreTilePass";
    }

    @Override
    public void apply(GenContext ctx, int x, int y, Tile tile){
        if(tile.block() != Blocks.air) return;
        if(tile.overlay() != Blocks.air) return;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return;

        if(ctx.spawnRoom != null && Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, 10f)) return;

        Block ore = pickOre(ctx, x, y, tile.floor());
        if(ore != Blocks.air){
            tile.setOverlay(ore);
        }
    }

    private Block pickOre(GenContext ctx, int x, int y, Block floor){
        float threat = Mathf.clamp(ctx.sector.threat);

        float cl = oreField(ctx, x, y, 695, 76f);
        float mn = oreField(ctx, x, y, 701, 80f);
        float cr = oreField(ctx, x, y, 709, 88f);
        float co = oreField(ctx, x, y, 719, 96f);
        float ur = oreField(ctx, x, y, 727, 110f);
        float mo = oreField(ctx, x, y, 733, 120f);
        float vb = oreField(ctx, x, y, 739, 136f);

        if(canHostCoal(floor) && cl > 0.68f) return Blocks.oreCoal;
        if(canHostManganese(floor) && mn > 0.70f && WHBlocksEnvironment.manganeseOre != null) return WHBlocksEnvironment.manganeseOre;
        if(canHostChromium(floor) && cr > 0.73f && WHBlocksEnvironment.chromiumOre != null) return WHBlocksEnvironment.chromiumOre;
        if(canHostCobalt(floor) && co > 0.76f && WHBlocksEnvironment.cobaltOre != null) return WHBlocksEnvironment.cobaltOre;

        if(threat > 0.18f && canHostUranium(floor) && ur > 0.79f && WHBlocksEnvironment.uraniumOre != null) return WHBlocksEnvironment.uraniumOre;
        if(threat > 0.35f && canHostHeatOre(floor) && mo > 0.82f && WHBlocksEnvironment.molybdenumOre != null) return WHBlocksEnvironment.molybdenumOre;
        if(threat > 0.58f && canHostVibranium(floor) && vb > 0.85f && WHBlocksEnvironment.vibraniumOre != null) return WHBlocksEnvironment.vibraniumOre;

        return Blocks.air;
    }

    private float oreField(GenContext ctx, int x, int y, int seedAdd, float scale){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        float a = Simplex.noise3d(ctx.seed + seedAdd, 3, 0.66f, 1f / scale, v.x, v.y, v.z);
        float b = Simplex.noise3d(ctx.seed + seedAdd + 1, 2, 0.74f, 1f / (scale * 0.42f), v.x, v.y, v.z) * 0.22f;
        return a + b;
    }

    private boolean canHostCoal(Block floor){
        return WHBlocksEnvironment.isMineralCoreFloor(floor)
        || floor == WHBlocksEnvironment.mineralSand
                || floor == WHBlocksEnvironment.darkMineralSandstone
        || floor == WHBlocksEnvironment.gravel
        || floor == WHBlocksEnvironment.trachyte
        || floor == WHBlocksEnvironment.oreShale
        || floor == WHBlocksEnvironment.darkRock;
    }

    private boolean canHostManganese(Block floor){
        return WHBlocksEnvironment.isMineralCoreFloor(floor)
        || floor == WHBlocksEnvironment.mineralSand
                || floor == WHBlocksEnvironment.darkMineralSandstone
        || floor == WHBlocksEnvironment.manganeseFloor
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.darkRock;
    }

    private boolean canHostChromium(Block floor){
        return floor == WHBlocksEnvironment.chromiteFloor
        || floor == WHBlocksEnvironment.chromiteFloorDark
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.manganeseFloor
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.darkRock;
    }

    private boolean canHostCobalt(Block floor){
        return floor == WHBlocksEnvironment.cobaltFloor
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.chromiteFloorDark
        || floor == WHBlocksEnvironment.trachyte
        || floor == WHBlocksEnvironment.darkRock;
    }

    private boolean canHostUranium(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters
        || floor == WHBlocksEnvironment.oreShale
        || floor == WHBlocksEnvironment.darkRock;
    }

    private boolean canHostHeatOre(Block floor){
        return floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.slag;
    }

    private boolean canHostVibranium(Block floor){
        return floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters
        || floor == WHBlocksEnvironment.darkMagmaRock;
    }
}
