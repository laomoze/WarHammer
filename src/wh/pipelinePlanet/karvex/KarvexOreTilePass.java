package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;

/**
 * 中文说明：Karvex 单格矿脉判定逻辑，用于装饰阶段矿覆写。
 */
public class KarvexOreTilePass implements TilePass{
    @Override
    public String name(){
        return "KarvexOreTilePass";
    }

    @Override
    public void apply(GenContext ctx, int x, int y, Tile tile){
        if(ctx.spawnRoom == null) return;
        if(tile.block() != Blocks.air) return;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return;

        int dx = x - ctx.spawnRoom.x;
        int dy = y - ctx.spawnRoom.y;
        int d2 = dx * dx + dy * dy;
        if(d2 <= 9 * 9) return;

        Block floor = tile.floor();
        float threat = ctx.sector.threat;

        Block manganese = modOre(WHBlocksEnvironment.manganeseOre);
        Block chromium = modOre(WHBlocksEnvironment.chromiumOre);
        Block cobalt = modOre(WHBlocksEnvironment.cobaltOre);
        Block uranium = modOre(WHBlocksEnvironment.uraniumOre);
        Block molybdenum = modOre(WHBlocksEnvironment.molybdenumOre);
        Block vibranium = modOre(WHBlocksEnvironment.vibraniumOre);

        Block selected = Blocks.air;

        if(d2 <= 42 * 42){
            boolean starterFloor = floor == WHBlocksEnvironment.mineralSand
            || floor == WHBlocksEnvironment.mineralSandstone
            || floor == WHBlocksEnvironment.quartzSand
            || floor == Blocks.darksand
            || floor == WHBlocksEnvironment.darkRock
            || floor == WHBlocksEnvironment.manganeseStone
            || floor == WHBlocksEnvironment.chromiteStone;

            if(manganese != Blocks.air && starterFloor && oreBlob(ctx, x, y, 11, 72f, 14f, 0.61f, 4)){
                selected = manganese;
            }else if(chromium != Blocks.air && (floor == WHBlocksEnvironment.chromiteStone || floor == WHBlocksEnvironment.manganeseStone || floor == Blocks.ferricStone)
            && oreBlob(ctx, x, y, 17, 77f, 15f, 0.63f, 4)){
                selected = chromium;
            }
        }

        if(selected == Blocks.air && (floor == WHBlocksEnvironment.chromiteStone || floor == Blocks.ferricStone || floor == Blocks.ferricCraters)
        && chromium != Blocks.air && oreBlob(ctx, x, y, 23, 83f, 15f, 0.63f, 4)){
            selected = chromium;
        }
        if(selected == Blocks.air && (floor == WHBlocksEnvironment.manganeseStone || floor == Blocks.yellowStone || floor == Blocks.yellowStonePlates)
        && manganese != Blocks.air && oreBlob(ctx, x, y, 29, 84f, 15f, 0.63f, 4)){
            selected = manganese;
        }
        if(selected == Blocks.air && (floor == WHBlocksEnvironment.cobaltStone || floor == Blocks.carbonStone || floor == Blocks.rhyolite)
        && cobalt != Blocks.air && oreBlob(ctx, x, y, 31, 90f, 15f, 0.66f, 5)){
            selected = cobalt;
        }

        if(selected == Blocks.air && threat > 0.08f && isRadiationFloor(floor) && uranium != Blocks.air && oreBlob(ctx, x, y, 37, 90f, 16f, 0.65f, 5)){
            selected = uranium;
        }

        if(selected == Blocks.air && isPolarRedFloor(floor) && d2 > 14 * 14){
            if(manganese != Blocks.air && oreBlob(ctx, x, y, 67, 80f, 13f, 0.65f, 5)){
                selected = manganese;
            }else if(chromium != Blocks.air && threat > 0.30f && oreBlob(ctx, x, y, 69, 84f, 13f, 0.67f, 5)){
                selected = chromium;
            }else if(uranium != Blocks.air && floor == Blocks.redIce && threat > 0.32f && oreBlob(ctx, x, y, 71, 88f, 14f, 0.66f, 5)){
                selected = uranium;
            }
        }

        if(selected == Blocks.air && isRadiationFloor(floor) && d2 > 16 * 16){
            boolean nearWater = nearLiquid(ctx, x, y, 1);
            if(!nearWater && uranium != Blocks.air && oreBlob(ctx, x, y, 59, 78f, 13f, 0.63f, 4)){
                selected = uranium;
            }else if(chromium != Blocks.air && oreBlob(ctx, x, y, 61, 76f, 12f, 0.65f, 5)){
                selected = chromium;
            }
        }

        // vanilla tungsten is always allowed
        if(selected == Blocks.air && isTungstenLikeFloor(floor) && oreBlob(ctx, x, y, 41, 95f, 12f, 0.73f, 6)){
            selected = Blocks.oreTungsten;
        }

        if(selected == Blocks.air && threat > 0.22f && isTungstenLikeFloor(floor) && molybdenum != Blocks.air && oreBlob(ctx, x, y, 47, 92f, 15f, 0.64f, 5)){
            selected = molybdenum;
        }

        if(selected == Blocks.air && threat > 0.60f && isDeepTechFloor(floor) && vibranium != Blocks.air && oreBlob(ctx, x, y, 53, 98f, 16f, 0.70f, 6)){
            selected = vibranium;
        }

        float placeChance = orePlaceChance(selected, manganese, chromium, cobalt, uranium, molybdenum, vibranium);
        if(selected != Blocks.air && ctx.rand.chance(placeChance)){
            tile.setOverlay(selected);
        }
    }

    private float orePlaceChance(Block ore, Block manganese, Block chromium, Block cobalt, Block uranium, Block molybdenum, Block vibranium){
        if(ore == Blocks.oreTungsten) return 0.14f;
        if(ore == manganese || ore == chromium) return 0.42f;
        if(ore == cobalt) return 0.34f;
        if(ore == uranium) return 0.44f;
        if(ore == molybdenum) return 0.42f;
        if(ore == vibranium) return 0.32f;
        return 0.30f;
    }

    private Block modOre(Block ore){
        return ore == null ? Blocks.air : ore;
    }

    private boolean oreBlob(GenContext ctx, int x, int y, int seedOffset, float scl, float warp, float threshold, int minSupport){
        float value = oreBlobValue(ctx, x, y, seedOffset, scl, warp);
        if(value < threshold) return false;

        int support = 0;
        float supportThreshold = threshold - 0.07f;
        for(Point2 p : Geometry.d8){
            float nearValue = oreBlobValue(ctx, x + p.x, y + p.y, seedOffset, scl, warp);
            if(nearValue >= supportThreshold){
                support++;
            }
        }
        return support >= minSupport;
    }

    private float oreBlobValue(GenContext ctx, int x, int y, int seedOffset, float scl, float warp){
        int seed = ctx.seed + seedOffset;

        float wx = x + (noise(ctx, seed + 37, x + seedOffset * 5f, y - seedOffset * 3f, 1, 1f, scl * 0.6f) - 0.5f) * warp;
        float wy = y + (noise(ctx, seed + 53, x - seedOffset * 4f, y + seedOffset * 6f, 1, 1f, scl * 0.6f) - 0.5f) * warp;

        float n1 = noise(ctx, seed, wx + seedOffset * 13f, wy - seedOffset * 9f, 2, 0.7f, scl);
        float n2 = noise(ctx, seed + 811, wx - seedOffset * 7f, wy + seedOffset * 5f, 1, 1f, scl * 0.62f);
        float n3 = noise(ctx, seed + 1523, wx + seedOffset * 3f, wy + seedOffset * 11f, 3, 0.6f, scl * 1.45f);

        return Mathf.clamp(n1 * 0.55f + n2 * 0.30f + n3 * 0.15f);
    }

    private float noise(GenContext ctx, int seed, float x, float y, double octaves, double falloff, double scl){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z);
    }

    private boolean nearLiquid(GenContext ctx, int x, int y, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.floor().isLiquid){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRadiationFloor(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationSandWater;
    }

    private boolean isPolarRedFloor(Block floor){
        return floor == Blocks.redmat
        || floor == Blocks.redStone
        || floor == Blocks.denseRedStone
        || floor == Blocks.redIce;
    }

    private boolean isTungstenLikeFloor(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.carbonStone
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock
        || floor == Blocks.dacite
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private boolean isDeepTechFloor(Block floor){
        return floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.magmarock
        || floor == Blocks.crystalFloor
        || floor == Blocks.crystallineStone
        || floor == Blocks.ferricCraters
        || floor == WHBlocksEnvironment.cobaltStone;
    }
}
