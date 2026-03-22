package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

/**
 * 中文说明：Karvex 地表细化阶段，统一地貌过渡与主色块连贯性。
 */
public class KarvexTerrainRefinePass implements GenPass{
    private static final int protectedRoomPadding = 3;

    @Override
    public String name(){
        return "KarvexTerrainRefinePass";
    }

    @Override
    public void apply(GenContext ctx){
        // Keep corridors open; this pass now focuses on floor richness and speckle cleanup.
        rebalanceMetalFloors(ctx);
        consolidateMetalStoneFields(ctx, 3);
        stabilizeMetalFieldCoverage(ctx);
        roughenChromiteMasses(ctx, 2);
        compressVolcanicFields(ctx);
        biasTowardDarkRockBase(ctx, 1);
        seedMineralSandFields(ctx);
        seedYellowRhyoliteBelts(ctx);
        smoothFloorTransitions(ctx, 1);
        mergeMacroFloorPatches(ctx, 1);
        harmonizeYellowRhyoliteTransitions(ctx, 1);
        rebalanceRedPurpleCoverage(ctx, 1);
        softenLargeRedBands(ctx, 2);
        restoreDarkRockPresence(ctx, 2);
        harmonizeMainlandPalette(ctx, 1);
        clampTrachyteDominance(ctx, 2);
        roughenTransitionContours(ctx, 2);
        pruneIsolatedGeothermalTiles(ctx, 2);
        pruneSpecialFloorSpeckles(ctx, 5);
        seedDryRadiationFields(ctx);
        sanitizeYellowNearRadiation(ctx);
        seedCrystalFieldsOnDarkRock(ctx);
        solidifyDarkRockSlagBasins(ctx);
        clampTrachyteDominance(ctx, 1);
        consolidateScorchedMainland(ctx, 2);
    }

    private void warpBoundaries(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        boolean[] open = snapshotOpen(ctx);

        for(int x = 1; x < width - 1; x++){
            for(int y = 1; y < height - 1; y++){
                if(isProtected(ctx, x, y)) continue;

                Tile tile = ctx.tiles.getn(x, y);
                if(!tile.floor().hasSurface()) continue;

                int index = indexOf(x, y, width);
                boolean isOpen = open[index];
                int around = countOpen8(open, width, height, x, y);
                if(around < 3 || around > 5) continue;

                float macro = Simplex.noise2d(ctx.seed + 91, 2, 0.6f, 1f / 22f, x + 17.3f, y - 9.1f);
                float detail = Simplex.noise2d(ctx.seed + 97, 2, 0.62f, 1f / 8f, x - 6.4f, y + 11.8f);
                float edgeNoise = macro * 0.76f + detail * 0.24f;

                if(!isOpen && around >= 5 && edgeNoise > 0.28f){
                    tile.setBlock(Blocks.air);
                }else if(isOpen && around <= 4 && edgeNoise < -0.16f){
                    setWallFromFloor(tile);
                }
            }
        }
    }

    private void smoothOpenMask(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            boolean[] open = snapshotOpen(ctx);

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(isProtected(ctx, x, y)) continue;

                    Tile tile = ctx.tiles.getn(x, y);
                    if(!tile.floor().hasSurface()) continue;

                    boolean isOpen = open[indexOf(x, y, width)];
                    int around = countOpen8(open, width, height, x, y);

                    if(isOpen){
                        if(around <= 2){
                            setWallFromFloor(tile);
                        }
                    }else if(tile.block().isStatic() && around >= 8){
                        tile.setBlock(Blocks.air);
                    }
                }
            }
        }
    }

    private void cleanupIsolatedTiles(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        boolean[] open = snapshotOpen(ctx);

        for(int x = 1; x < width - 1; x++){
            for(int y = 1; y < height - 1; y++){
                if(isProtected(ctx, x, y)) continue;

                Tile tile = ctx.tiles.getn(x, y);
                if(!tile.floor().hasSurface()) continue;

                boolean isOpen = open[indexOf(x, y, width)];
                int d4Open = countOpen4(open, width, height, x, y);

                if(isOpen){
                    if(d4Open <= 1){
                        setWallFromFloor(tile);
                    }
                }else if(tile.block().isStatic() && d4Open == 4){
                    int around = countOpen8(open, width, height, x, y);
                    if(around >= 7 && ctx.rand.chance(0.45f)){
                        tile.setBlock(Blocks.air);
                    }
                }
            }
        }
    }

    /**
     * Reduces over-fragmented wall edges by sealing tiny holes and smoothing noisy wall boundaries.
     */
    private void solidifyTerrainWalls(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            boolean[] open = snapshotOpen(ctx);

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(isProtected(ctx, x, y)) continue;

                    Tile tile = ctx.tiles.getn(x, y);
                    if(!tile.floor().hasSurface()) continue;

                    boolean isOpen = open[indexOf(x, y, width)];
                    int open4 = countOpen4(open, width, height, x, y);
                    int open8 = countOpen8(open, width, height, x, y);
                    int wall8 = countWall8(open, width, height, x, y);

                    if(isOpen){
                        if(open8 <= 1){
                            setWallFromFloor(tile);
                        }else if(open4 <= 1 && wall8 >= 6 && ctx.rand.chance(0.72f)){
                            setWallFromFloor(tile);
                        }
                    }else if(tile.block().isStatic()){
                        // Keep thick walls but allow removing tiny wall spikes in open areas.
                        if(open4 >= 3 && open8 >= 7 && wall8 <= 2 && ctx.rand.chance(0.28f)){
                            tile.setBlock(Blocks.air);
                        }
                    }
                }
            }
        }
    }

    private void rebalanceMetalFloors(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isProtected(ctx, tile.x, tile.y)) continue;
            if(nearLiquid(ctx, tile.x, tile.y, 1)) continue;

            Block floor = tile.floor();
            if(floor == WHBlocksEnvironment.chromiteStone){
                int same = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.chromiteStone);
                if(same <= 1 && ctx.rand.chance(0.84f)){
                    tile.setFloor((ctx.rand.chance(0.42f) ? WHBlocksEnvironment.manganeseStone : WHBlocksEnvironment.darkRock).asFloor());
                }else if(same <= 3 && ctx.rand.chance(0.24f)){
                    tile.setFloor((ctx.rand.chance(0.48f) ? WHBlocksEnvironment.manganeseStone : WHBlocksEnvironment.darkRock).asFloor());
                }
            }else if(floor == WHBlocksEnvironment.cobaltStone){
                int same = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.cobaltStone);
                if(same <= 1 && ctx.rand.chance(0.82f)){
                    tile.setFloor((ctx.rand.chance(0.72f) ? WHBlocksEnvironment.chromiteStone : WHBlocksEnvironment.darkRock).asFloor());
                }else if(same <= 2 && ctx.rand.chance(0.34f)){
                    tile.setFloor((ctx.rand.chance(0.66f) ? WHBlocksEnvironment.chromiteStone : WHBlocksEnvironment.darkRock).asFloor());
                }
            }else if(floor == Blocks.shale){
                int same = countNearFloor(ctx, tile.x, tile.y, Blocks.shale);
                if(same <= 4 && ctx.rand.chance(0.68f)){
                    tile.setFloor((ctx.rand.chance(0.72f) ? WHBlocksEnvironment.darkRock : Blocks.stone).asFloor());
                }
            }
        }
    }

    private void consolidateMetalStoneFields(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            Block[] floors = snapshotFloors(ctx);

            for(int x = 2; x < width - 2; x++){
                for(int y = 2; y < height - 2; y++){
                    if(isProtected(ctx, x, y)) continue;
                    if(nearLiquid(ctx, x, y, 1)) continue;

                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isMetalFieldConvertible(floor)) continue;

                    int manganese = 0, chromite = 0, cobalt = 0;
                    for(int ox = -2; ox <= 2; ox++){
                        for(int oy = -2; oy <= 2; oy++){
                            if(ox == 0 && oy == 0) continue;
                            Block near = floors[indexOf(x + ox, y + oy, width)];
                            if(near == WHBlocksEnvironment.manganeseStone){
                                manganese++;
                            }else if(near == WHBlocksEnvironment.chromiteStone){
                                chromite++;
                            }else if(near == WHBlocksEnvironment.cobaltStone){
                                cobalt++;
                            }
                        }
                    }

                    Block target = null;
                    if(manganese >= 9 && manganese >= chromite + 2 && manganese >= cobalt + 2){
                        target = WHBlocksEnvironment.manganeseStone;
                    }else if(chromite >= 10 && chromite >= manganese + 2 && chromite >= cobalt + 1){
                        target = WHBlocksEnvironment.chromiteStone;
                    }else if(cobalt >= 10 && cobalt >= chromite + 1 && cobalt >= manganese + 2){
                        target = WHBlocksEnvironment.cobaltStone;
                    }

                    if(target == null) continue;
                    if(floor == target) continue;

                    float chance = target == WHBlocksEnvironment.manganeseStone
                    ? 0.72f
                    : (target == WHBlocksEnvironment.chromiteStone ? 0.44f : 0.58f);
                    if(ctx.rand.chance(chance)){
                        tile.setFloor(target.asFloor());
                    }
                }
            }
        }
    }

    /**
     * Clamp manganese/chromite/cobalt coverage into stable bands so different seeds do not swing too hard.
     */
    private void stabilizeMetalFieldCoverage(GenContext ctx){
        int domain = countMetalCoverageDomainTiles(ctx);
        if(domain <= 0) return;

        int manganeseMin = Math.min(domain, Math.max(24, Math.round(domain * 0.042f)));
        int manganeseMax = Math.min(domain, Math.max(manganeseMin + 14, Math.round(domain * 0.096f)));

        int chromiteMin = Math.min(domain, Math.max(18, Math.round(domain * 0.030f)));
        int chromiteMax = Math.min(domain, Math.max(chromiteMin + 10, Math.round(domain * 0.066f)));

        int cobaltMin = Math.min(domain, Math.max(8, Math.round(domain * 0.008f)));
        int cobaltMax = Math.min(domain, Math.max(cobaltMin + 8, Math.round(domain * 0.030f)));

        stabilizeSingleMetalCoverage(ctx, WHBlocksEnvironment.manganeseStone, manganeseMin, manganeseMax, 1103);
        stabilizeSingleMetalCoverage(ctx, WHBlocksEnvironment.chromiteStone, chromiteMin, chromiteMax, 1147);
        stabilizeSingleMetalCoverage(ctx, WHBlocksEnvironment.cobaltStone, cobaltMin, cobaltMax, 1189);
    }

    private void stabilizeSingleMetalCoverage(GenContext ctx, Block target, int minCount, int maxCount, int noiseSeed){
        int width = ctx.width();
        int height = ctx.height();
        int count = countMetalCoverageTiles(ctx, target);

        if(count < minCount){
            for(int pass = 0; pass < 3 && count < minCount; pass++){
                Block[] floors = snapshotFloors(ctx);
                int need = minCount - count;
                float threshold = 0.14f - pass * 0.18f;
                int minNear = Math.max(1, 3 - pass);

                for(int x = 1; x < width - 1 && need > 0; x++){
                    for(int y = 1; y < height - 1 && need > 0; y++){
                        if(!isMetalCoverageAdjustable(ctx, x, y)) continue;

                        Block floor = floors[indexOf(x, y, width)];
                        if(floor == target || !isMetalCoverageHost(floor)) continue;

                        int nearTarget = countNearFloorFromSnapshot(floors, width, height, x, y, target);
                        if(nearTarget < minNear) continue;

                        float shape = Simplex.noise2d(ctx.seed + noiseSeed + pass * 17, 2, 0.60f, 1f / 13f, x + 19.7f, y - 11.9f);
                        if(shape < threshold) continue;

                        ctx.tiles.getn(x, y).setFloor(target.asFloor());
                        need--;
                        count++;
                    }
                }
            }
        }

        if(count > maxCount){
            int trimPasses = target == WHBlocksEnvironment.cobaltStone ? 6 : (target == WHBlocksEnvironment.chromiteStone ? 5 : 3);
            int nearBase = target == WHBlocksEnvironment.cobaltStone ? 6 : (target == WHBlocksEnvironment.chromiteStone ? 4 : 3);
            float thresholdBase = target == WHBlocksEnvironment.cobaltStone ? -0.22f : (target == WHBlocksEnvironment.chromiteStone ? -0.18f : -0.10f);

            for(int pass = 0; pass < trimPasses && count > maxCount; pass++){
                Block[] floors = snapshotFloors(ctx);
                int trim = count - maxCount;
                int maxNear = nearBase + pass;
                float threshold = thresholdBase + pass * 0.14f;

                for(int x = 1; x < width - 1 && trim > 0; x++){
                    for(int y = 1; y < height - 1 && trim > 0; y++){
                        if(!isMetalCoverageAdjustable(ctx, x, y)) continue;
                        if(floors[indexOf(x, y, width)] != target) continue;

                        int nearTarget = countNearFloorFromSnapshot(floors, width, height, x, y, target);
                        if(nearTarget > maxNear) continue;

                        float shape = Simplex.noise2d(ctx.seed + noiseSeed + 101 + pass * 13, 2, 0.60f, 1f / 14f, x - 7.4f, y + 16.2f);
                        if(shape < threshold) continue;

                        Block fallback = pickMetalFallbackFloor(floors, width, height, x, y, target);
                        ctx.tiles.getn(x, y).setFloor(fallback.asFloor());
                        trim--;
                        count--;
                    }
                }
            }
        }
    }

    /**
     * Breaks up overly large smooth chromite blobs to avoid big round patches.
     * Keeps a manganese/dark-rock transition so metal terrain still reads coherent.
     */
    private void roughenChromiteMasses(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 2; x < width - 2; x++){
                for(int y = 2; y < height - 2; y++){
                    if(!isMetalCoverageAdjustable(ctx, x, y)) continue;

                    int idx = indexOf(x, y, width);
                    Block floor = floors[idx];
                    if(floor != WHBlocksEnvironment.chromiteStone) continue;

                    int nearChromite = countNearFloorFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.chromiteStone);
                    int nearManganese = countNearFloorFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.manganeseStone);
                    int nearDark = 0;
                    for(Point2 p : Geometry.d8){
                        Block near = floors[indexOf(x + p.x, y + p.y, width)];
                        if(near == WHBlocksEnvironment.darkRock || near == WHBlocksEnvironment.trachyte || near == Blocks.carbonStone){
                            nearDark++;
                        }
                    }

                    float macro = Simplex.noise2d(ctx.seed + 1201 + it * 19, 2, 0.58f, 1f / 33f, x + 7.1f, y - 13.4f);
                    float detail = Simplex.noise2d(ctx.seed + 1217 + it * 23, 2, 0.62f, 1f / 9f, x - 11.8f, y + 6.5f);
                    float carve = macro * 0.68f + detail * 0.32f;

                    Block target = null;
                    if(nearChromite >= 7){
                        if(carve > 0.16f || (nearDark <= 1 && carve > -0.04f)){
                            target = (nearManganese >= 2 || carve > 0.52f) ? WHBlocksEnvironment.manganeseStone : WHBlocksEnvironment.darkRock;
                        }
                    }else if(nearChromite >= 5){
                        if(carve > 0.34f && ctx.rand.chance(0.62f)){
                            target = (nearManganese >= 3 && ctx.rand.chance(0.58f)) ? WHBlocksEnvironment.manganeseStone : WHBlocksEnvironment.darkRock;
                        }
                    }else if(nearChromite <= 1){
                        if(ctx.rand.chance(0.74f)){
                            target = pickMetalFallbackFloor(floors, width, height, x, y, WHBlocksEnvironment.chromiteStone);
                        }
                    }else if(nearChromite <= 3 && nearDark >= 4 && carve < -0.24f && ctx.rand.chance(0.52f)){
                        target = WHBlocksEnvironment.darkRock;
                    }

                    if(target != null && target != floor){
                        writes[idx] = target;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    private void compressVolcanicFields(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isProtected(ctx, tile.x, tile.y)) continue;

            Block floor = tile.floor();
            if(!isGeothermalFloor(floor)) continue;

            int geothermalNear = countGeothermalNeighbors(ctx, tile.x, tile.y);
            if(geothermalNear <= 4) continue;
            if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 1)) continue;

            float broad = Simplex.noise2d(ctx.seed + 613, 2, 0.58f, 1f / 43f, tile.x + 91.3f, tile.y - 44.7f);
            float detail = Simplex.noise2d(ctx.seed + 617, 2, 0.62f, 1f / 17f, tile.x - 18.1f, tile.y + 27.5f);
            float strength = (geothermalNear - 4) * 0.18f + broad * 0.30f + detail * 0.18f;

            if(strength < 0.28f) continue;

            if(floor == WHBlocksEnvironment.darkMagmaRock || floor == Blocks.magmarock){
                tile.setFloor((ctx.rand.chance(0.7f) ? WHBlocksEnvironment.darkHotRock : WHBlocksEnvironment.scorchedEarth).asFloor());
            }else if(floor == WHBlocksEnvironment.darkHotRock || floor == Blocks.hotrock){
                tile.setFloor((ctx.rand.chance(0.62f) ? WHBlocksEnvironment.scorchedEarth : WHBlocksEnvironment.darkRock).asFloor());
            }else{
                tile.setFloor((ctx.rand.chance(0.58f) ? WHBlocksEnvironment.scorchedEarth : WHBlocksEnvironment.darkRock).asFloor());
            }
        }
    }

    private void pruneSpecialFloorSpeckles(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            Block[] floors = snapshotFloors(ctx);
            Block[] candidates = new Block[25];
            int[] counts = new int[25];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(isProtected(ctx, x, y)) continue;
                    if(nearLiquid(ctx, x, y, 1)) continue;

                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isSpeckleSensitiveFloor(floor)) continue;

                    int same = 0;
                    Block best = null;
                    int bestCount = 0;
                    int unique = 0;

                    for(int ox = -2; ox <= 2; ox++){
                        for(int oy = -2; oy <= 2; oy++){
                            if(ox == 0 && oy == 0) continue;
                            if(x + ox < 0 || y + oy < 0 || x + ox >= width || y + oy >= height) continue;
                            Block near = floors[indexOf(x + ox, y + oy, width)];

                            if(near == floor){
                                same++;
                            }
                            if(!near.asFloor().hasSurface() || near.asFloor().isLiquid) continue;
                            if(!isFloorCompatible(floor, near)) continue;

                            int slot = -1;
                            for(int n = 0; n < unique; n++){
                                if(candidates[n] == near){
                                    slot = n;
                                    break;
                                }
                            }
                            if(slot == -1){
                                if(unique >= candidates.length) continue;
                                slot = unique++;
                                candidates[slot] = near;
                                counts[slot] = 0;
                            }

                            int count = ++counts[slot];
                            if(near != floor && count > bestCount){
                                bestCount = count;
                                best = near;
                            }
                        }
                    }

                    int keepThreshold = isGeothermalFloor(floor) ? 9 : 7;
                    if(same <= keepThreshold && best != null && bestCount >= 7){
                        tile.setFloor(best.asFloor());
                    }
                }
            }
        }
    }

    private void smoothFloorTransitions(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            Block[] floors = snapshotFloors(ctx);
            Block[] candidates = new Block[8];
            int[] counts = new int[8];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(isProtected(ctx, x, y)) continue;
                    if(nearLiquid(ctx, x, y, 1)) continue;

                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isBlendableFloor(floor)) continue;

                    int same = 0;
                    int bestCount = 0;
                    Block best = null;
                    int unique = 0;

                    for(Point2 p : Geometry.d8){
                        Block near = floors[indexOf(x + p.x, y + p.y, width)];
                        if(!isBlendableFloor(near)) continue;
                        if(!isFloorCompatible(floor, near)) continue;

                        if(near == floor){
                            same++;
                        }

                        int slot = -1;
                        for(int n = 0; n < unique; n++){
                            if(candidates[n] == near){
                                slot = n;
                                break;
                            }
                        }

                        if(slot == -1){
                            slot = unique++;
                            candidates[slot] = near;
                            counts[slot] = 0;
                        }

                        int count = ++counts[slot];
                        if(near != floor && count > bestCount){
                            bestCount = count;
                            best = near;
                        }
                    }

                    if(best == null) continue;

                    if(bestCount >= 6 && same <= 3){
                        tile.setFloor(best.asFloor());
                    }else if(bestCount >= 5 && same <= 2 && ctx.rand.chance(0.62f)){
                        tile.setFloor(best.asFloor());
                    }
                }
            }
        }
    }

    private void mergeMacroFloorPatches(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            Block[] floors = snapshotFloors(ctx);
            Block[] candidates = new Block[25];
            int[] counts = new int[25];

            for(int x = 2; x < width - 2; x++){
                for(int y = 2; y < height - 2; y++){
                    if(isProtected(ctx, x, y)) continue;
                    if(nearLiquid(ctx, x, y, 1)) continue;

                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isBlendableFloor(floor)) continue;

                    int same = 0;
                    int bestCount = 0;
                    Block best = null;
                    int unique = 0;

                    for(int ox = -2; ox <= 2; ox++){
                        for(int oy = -2; oy <= 2; oy++){
                            Block near = floors[indexOf(x + ox, y + oy, width)];
                            if(!isBlendableFloor(near)) continue;
                            if(!isFloorCompatible(floor, near)) continue;

                            if(near == floor){
                                same++;
                            }

                            int slot = -1;
                            for(int n = 0; n < unique; n++){
                                if(candidates[n] == near){
                                    slot = n;
                                    break;
                                }
                            }

                            if(slot == -1){
                                slot = unique++;
                                candidates[slot] = near;
                                counts[slot] = 0;
                            }

                            int count = ++counts[slot];
                            if(near != floor && count > bestCount){
                                bestCount = count;
                                best = near;
                            }
                        }
                    }

                    if(best == null) continue;
                    if(bestCount >= 12 && same <= 8){
                        float chance = same <= 5 ? 0.72f : 0.54f;
                        if(ctx.rand.chance(chance)){
                            tile.setFloor(best.asFloor());
                        }
                    }
                }
            }
        }
    }

    /**
     * Slightly roughens over-rounded boundaries between compatible biome groups.
     * Keeps changes light so transitions read natural without becoming noisy.
     */
    private void roughenTransitionContours(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];
            Block[] candidates = new Block[8];
            int[] counts = new int[8];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(!isTransitionNoiseAdjustable(ctx, x, y)) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isBlendableFloor(floor)) continue;

                    int same = 0;
                    int bestCount = 0;
                    Block best = null;
                    int unique = 0;

                    for(Point2 p : Geometry.d8){
                        Block near = floors[indexOf(x + p.x, y + p.y, width)];
                        if(!isBlendableFloor(near)) continue;
                        if(!isFloorCompatible(floor, near)) continue;
                        if(near == floor){
                            same++;
                            continue;
                        }

                        int slot = -1;
                        for(int n = 0; n < unique; n++){
                            if(candidates[n] == near){
                                slot = n;
                                break;
                            }
                        }

                        if(slot == -1){
                            slot = unique++;
                            candidates[slot] = near;
                            counts[slot] = 0;
                        }

                        int count = ++counts[slot];
                        if(count > bestCount){
                            bestCount = count;
                            best = near;
                        }
                    }

                    if(best == null || bestCount < 3 || same < 2) continue;

                    int group = floorGroup(floor);
                    int bestGroup = floorGroup(best);
                    if(group == 0 || bestGroup == 0 || group == bestGroup) continue;

                    float macro = Simplex.noise2d(ctx.seed + 1069 + it * 11, 2, 0.60f, 1f / 16f, x + 9.2f, y - 12.4f);
                    float detail = Simplex.noise2d(ctx.seed + 1073 + it * 13, 2, 0.63f, 1f / 7f, x - 4.8f, y + 6.1f);
                    float field = macro * 0.72f + detail * 0.28f + (bestCount - same) * 0.04f;
                    if(field < 0.08f || field > 0.58f) continue;

                    float chance = same <= 2 ? 0.34f : 0.22f;
                    if(ctx.rand.chance(chance)){
                        writes[indexOf(x, y, width)] = best;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    /**
     * Expands floor masses around wall boundaries so "outside wall" areas read as larger chunks instead of thin rings.
     */
    private void expandOuterWallBiomeMass(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];
            Block[] candidates = new Block[16];
            int[] counts = new int[16];

            for(int x = 2; x < width - 2; x++){
                for(int y = 2; y < height - 2; y++){
                    if(!isTransitionNoiseAdjustable(ctx, x, y)) continue;

                    int wallNear1 = countWallNeighborsInRadius(ctx, x, y, 1);
                    int wallNear2 = countWallNeighborsInRadius(ctx, x, y, 2);
                    if(wallNear1 == 0 && wallNear2 < 6) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isBlendableFloor(floor)) continue;

                    int same = 0;
                    int bestCount = 0;
                    Block best = null;
                    int unique = 0;

                    for(int ox = -3; ox <= 3; ox++){
                        for(int oy = -3; oy <= 3; oy++){
                            int nx = x + ox, ny = y + oy;
                            if(nx < 0 || ny < 0 || nx >= width || ny >= height) continue;

                            Tile nearTile = ctx.tiles.getn(nx, ny);
                            if(nearTile.block() != Blocks.air) continue;

                            Block near = floors[indexOf(nx, ny, width)];
                            if(!isBlendableFloor(near)) continue;
                            if(!isFloorCompatible(floor, near) && near != floor) continue;

                            if(near == floor){
                                same++;
                                continue;
                            }

                            int slot = -1;
                            for(int n = 0; n < unique; n++){
                                if(candidates[n] == near){
                                    slot = n;
                                    break;
                                }
                            }

                            if(slot == -1){
                                slot = unique++;
                                candidates[slot] = near;
                                counts[slot] = 0;
                            }

                            int count = ++counts[slot];
                            if(count > bestCount){
                                bestCount = count;
                                best = near;
                            }
                        }
                    }

                    if(best == null || bestCount < 9 || same > 16) continue;

                    float macro = Simplex.noise2d(ctx.seed + 1091 + it * 11, 2, 0.60f, 1f / 18f, x + 10.6f, y - 11.5f);
                    float detail = Simplex.noise2d(ctx.seed + 1097 + it * 13, 2, 0.62f, 1f / 8f, x - 4.4f, y + 9.1f);
                    float field = macro * 0.74f + detail * 0.26f + wallNear1 * 0.03f;
                    if(field < -0.06f || field > 0.66f) continue;

                    float chance = wallNear1 >= 2 ? 0.52f : 0.36f;
                    if(ctx.rand.chance(chance)){
                        writes[indexOf(x, y, width)] = best;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    /**
     * Removes single/few-tile geothermal speckles and blends them back into nearby host floors.
     */
    private void pruneIsolatedGeothermalTiles(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(!isTransitionNoiseAdjustable(ctx, x, y)) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isGeothermalFloor(floor)) continue;
                    if(nearFloor(ctx, x, y, Blocks.slag, 2)) continue;

                    int same = countNearFloorFromSnapshot(floors, width, height, x, y, floor);
                    int geothermalNear = countGeothermalNearFromSnapshot(floors, width, height, x, y);
                    if(same > 2 || geothermalNear >= 4) continue;

                    Block fallback = dominantNonGeothermalFloor(floors, width, height, x, y);
                    if(fallback != null && fallback != floor){
                        writes[indexOf(x, y, width)] = fallback;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    /**
     * Bias mixed volcanic/metal mainland back to dark rock so the map reads as larger coherent masses.
     */
    private void biasTowardDarkRockBase(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            Block[] floors = snapshotFloors(ctx);

            for(int x = 2; x < width - 2; x++){
                for(int y = 2; y < height - 2; y++){
                    if(isProtected(ctx, x, y)) continue;
                    if(nearLiquid(ctx, x, y, 1)) continue;
                    if(nearFloor(ctx, x, y, Blocks.slag, 2)) continue;

                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isDarkRockConvertible(floor)) continue;

                    int dark = 0, geothermal = 0, same = 0;
                    for(int ox = -2; ox <= 2; ox++){
                        for(int oy = -2; oy <= 2; oy++){
                            Block near = floors[indexOf(x + ox, y + oy, width)];
                            if(near == WHBlocksEnvironment.darkRock) dark++;
                            if(near == floor) same++;
                            if(isGeothermalFloor(near)) geothermal++;
                        }
                    }

                    if(isGeothermalFloor(floor)){
                        if(dark >= 10 && geothermal <= 11 && ctx.rand.chance(0.78f)){
                            tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
                        }
                    }else if(isMetalFieldFloor(floor)){
                        if(same <= 8 && dark >= 9 && ctx.rand.chance(0.72f)){
                            tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
                        }
                    }else if(dark >= 13 && same <= 9 && geothermal <= 8 && ctx.rand.chance(0.64f)){
                        tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
                    }
                }
            }
        }
    }

    /**
     * Seed and spread yellow-stone/rhyolite transition belts so hot-dry regions do not collapse into one flat palette.
     */
    private void seedYellowRhyoliteBelts(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        Block[] writes = new Block[width * height];

        for(Tile tile : ctx.tiles){
            if(!isYellowRhyoliteAdjustable(ctx, tile.x, tile.y)) continue;

            Block floor = tile.floor();
            int yellowNear = countNearFloor(ctx, tile.x, tile.y, Blocks.yellowStone)
            + countNearFloor(ctx, tile.x, tile.y, Blocks.yellowStonePlates);
            int rhyNear = countNearFloor(ctx, tile.x, tile.y, Blocks.rhyolite)
            + countNearFloor(ctx, tile.x, tile.y, Blocks.roughRhyolite);
            int hotNear = countGeothermalNeighbors(ctx, tile.x, tile.y);

            float macro = Simplex.noise2d(ctx.seed + 931, 2, 0.58f, 1f / 46f, tile.x + 18.5f, tile.y - 31.2f);
            float detail = Simplex.noise2d(ctx.seed + 937, 2, 0.62f, 1f / 12f, tile.x - 9.3f, tile.y + 7.8f);
            float field = macro * 0.72f + detail * 0.28f + hotNear * 0.05f + rhyNear * 0.04f;

            int idx = indexOf(tile.x, tile.y, width);
            if(floor == Blocks.rhyolite){
                if(field > 0.52f && (rhyNear >= 4 || hotNear >= 2) && ctx.rand.chance(0.36f)){
                    writes[idx] = Blocks.yellowStonePlates;
                }else if((field > 0.46f && (yellowNear >= 2 || hotNear >= 1) && ctx.rand.chance(0.40f))
                || (field > 0.74f && ctx.rand.chance(0.24f))){
                    writes[idx] = Blocks.roughRhyolite;
                }
                continue;
            }

            if(floor == Blocks.roughRhyolite){
                if(field > 0.34f && rhyNear >= 4 && hotNear >= 1 && ctx.rand.chance(0.28f)){
                    writes[idx] = Blocks.yellowStonePlates;
                }else if(field < -0.24f && yellowNear <= 1 && hotNear == 0 && ctx.rand.chance(0.64f)){
                    writes[idx] = Blocks.rhyolite;
                }
                continue;
            }

            if(!isYellowRhyoliteHost(floor)) continue;

            if(field > 0.68f && (hotNear >= 2 || rhyNear >= 3)){
                writes[idx] = field > 0.88f
                ? Blocks.roughRhyolite
                : (ctx.rand.chance(0.56f) ? Blocks.rhyolite : Blocks.yellowStonePlates);
            }else if(field > 0.52f && (yellowNear >= 5 || rhyNear >= 4)){
                writes[idx] = ctx.rand.chance(0.58f) ? Blocks.yellowStonePlates : Blocks.rhyolite;
            }
        }

        applyFloorWrites(ctx, writes, width);

        for(int it = 0; it < 2; it++){
            Block[] spread = new Block[width * height];
            Block[] floors = snapshotFloors(ctx);

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(!isYellowRhyoliteAdjustable(ctx, x, y)) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isYellowRhyoliteHost(floor) && !isYellowRhyoliteFloor(floor)) continue;

                    int yellowNear = countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.yellowStone)
                    + countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.yellowStonePlates);
                    int rhyNear = countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.rhyolite)
                    + countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.roughRhyolite);
                    if(yellowNear + rhyNear < 4) continue;

                    float shape = Simplex.noise2d(ctx.seed + 943 + it * 11, 2, 0.60f, 1f / 11f, x + 6.7f, y - 8.4f);
                    if(shape < -0.18f) continue;

                    int idx = indexOf(x, y, width);
                    if(rhyNear >= 5 && shape > 0.24f){
                        spread[idx] = shape > 0.68f
                        ? Blocks.roughRhyolite
                        : (shape > 0.34f && yellowNear >= 2 ? Blocks.yellowStonePlates : Blocks.rhyolite);
                    }else if(yellowNear >= 5){
                        spread[idx] = shape > 0.44f ? Blocks.yellowStonePlates : Blocks.rhyolite;
                    }else if(rhyNear >= 4 && yellowNear >= 2){
                        spread[idx] = shape > 0.56f ? Blocks.roughRhyolite : (shape > 0.30f ? Blocks.yellowStonePlates : Blocks.rhyolite);
                    }
                }
            }

            applyFloorWrites(ctx, spread, width);
        }
    }

    private void harmonizeYellowRhyoliteTransitions(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(!isYellowRhyoliteAdjustable(ctx, x, y)) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isYellowRhyoliteFloor(floor)) continue;

                    int yellowNear = countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.yellowStone)
                    + countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.yellowStonePlates);
                    int rhyNear = countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.rhyolite)
                    + countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.roughRhyolite);
                    int hotNear = countGeothermalNearFromSnapshot(floors, width, height, x, y);
                    float warp = Simplex.noise2d(ctx.seed + 961 + it * 7, 2, 0.60f, 1f / 13f, x - 6.2f, y + 14.8f);

                    Block target = null;
                    if(floor == Blocks.yellowStone){
                        if(rhyNear >= 3 && warp > -0.06f){
                            target = Blocks.yellowStonePlates;
                        }else if(yellowNear <= 2 && rhyNear <= 1 && hotNear == 0){
                            target = pickYellowRhyoliteFallback(floors, width, height, x, y);
                        }
                    }else if(floor == Blocks.yellowStonePlates){
                        if(yellowNear >= 8 && warp > 0.48f){
                            target = Blocks.yellowStone;
                        }else if(yellowNear <= 2 && rhyNear <= 2 && hotNear == 0){
                            target = pickYellowRhyoliteFallback(floors, width, height, x, y);
                        }
                    }else if(floor == Blocks.rhyolite){
                        if(yellowNear >= 2 && rhyNear >= 3 && warp > 0.22f && ctx.rand.chance(0.36f)){
                            target = Blocks.yellowStonePlates;
                        }else if(rhyNear >= 5 && hotNear >= 1 && warp > 0.16f){
                            target = Blocks.roughRhyolite;
                        }else if(rhyNear <= 1 && yellowNear <= 1 && hotNear == 0){
                            target = pickYellowRhyoliteFallback(floors, width, height, x, y);
                        }
                    }else if(floor == Blocks.roughRhyolite){
                        if(yellowNear >= 4 && warp > 0.32f && ctx.rand.chance(0.34f)){
                            target = Blocks.yellowStonePlates;
                        }else if((rhyNear <= 3 && hotNear == 0 && warp < 0.14f) || (yellowNear >= 3 && warp < 0.46f)){
                            target = Blocks.rhyolite;
                        }else if(rhyNear <= 1 && yellowNear <= 1 && hotNear == 0){
                            target = pickYellowRhyoliteFallback(floors, width, height, x, y);
                        }
                    }

                    if(target != null && target != floor){
                        writes[indexOf(x, y, width)] = target;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    private boolean isYellowRhyoliteAdjustable(GenContext ctx, int x, int y){
        Tile tile = ctx.tiles.getn(x, y);
        if(tile.block() != Blocks.air) return false;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        if(isProtected(ctx, x, y)) return false;
        if(nearLiquid(ctx, x, y, 1)) return false;
        if(countRadiationRelatedInRadius(ctx, x, y, 2) > 0) return false;
        return !nearFloor(ctx, x, y, Blocks.slag, 1);
    }

    private boolean isYellowRhyoliteFloor(Block floor){
        return floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite;
    }

    private boolean isYellowRhyoliteHost(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.stone
        || floor == Blocks.shale
        || floor == Blocks.carbonStone
        || floor == Blocks.dacite
        || floor == Blocks.craters
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private Block pickYellowRhyoliteFallback(Block[] floors, int width, int height, int x, int y){
        Block fallback = dominantNonGeothermalFloor(floors, width, height, x, y);
        if(isYellowRhyoliteHost(fallback)) return fallback;
        return WHBlocksEnvironment.darkRock;
    }

    /**
     * Prevents trachyte from dominating or wrapping other biomes into thin shells.
     */
    private void clampTrachyteDominance(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(!isTransitionNoiseAdjustable(ctx, x, y)) continue;

                    int idx = indexOf(x, y, width);
                    Block floor = floors[idx];
                    if(floor != WHBlocksEnvironment.trachyte) continue;

                    int nearTrachyte = countNearFloorFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.trachyte);
                    int nearDark = countNearFloorFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.darkRock)
                    + countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.stone)
                    + countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.shale)
                    + countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.carbonStone);
                    int nearRhy = countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.rhyolite)
                    + countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.roughRhyolite);
                    int radNear = countRadiationRelatedInRadius(ctx, x, y, 2);

                    Block target = null;
                    if(radNear > 0){
                        target = radNear >= 4 ? WHBlocksEnvironment.radiationRockFloor : WHBlocksEnvironment.darkRock;
                    }else if(nearTrachyte <= 2 && nearDark >= 4){
                        target = WHBlocksEnvironment.darkRock;
                    }else if(nearTrachyte <= 3 && nearRhy >= 3){
                        target = Blocks.rhyolite;
                    }else{
                        int trachyteMass = countFloorInRadiusFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.trachyte, 3);
                        float carve = Simplex.noise2d(ctx.seed + 1117 + it * 11, 2, 0.60f, 1f / 18f, x + 9.1f, y - 12.2f);
                        if(trachyteMass >= 30 && carve < 0.60f){
                            target = nearRhy >= 2 ? Blocks.rhyolite : WHBlocksEnvironment.darkRock;
                        }
                    }

                    if(target != null && target != floor){
                        writes[idx] = target;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }

        int domain = 0;
        int trachyteCount = 0;
        for(Tile tile : ctx.tiles){
            if(!isTransitionNoiseAdjustable(ctx, tile.x, tile.y)) continue;
            if(!isMainlandPaletteFloor(tile.floor())) continue;
            domain++;
            if(tile.floor() == WHBlocksEnvironment.trachyte){
                trachyteCount++;
            }
        }

        int maxTrachyte = Math.max(160, domain / 12);
        int trim = trachyteCount - maxTrachyte;
        if(trim <= 0) return;

        for(Tile tile : ctx.tiles){
            if(trim <= 0) break;
            if(tile.floor() != WHBlocksEnvironment.trachyte) continue;
            if(!isTransitionNoiseAdjustable(ctx, tile.x, tile.y)) continue;

            int nearRhy = countNearFloor(ctx, tile.x, tile.y, Blocks.rhyolite)
            + countNearFloor(ctx, tile.x, tile.y, Blocks.roughRhyolite);
            int nearDark = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.darkRock)
            + countNearFloor(ctx, tile.x, tile.y, Blocks.stone)
            + countNearFloor(ctx, tile.x, tile.y, Blocks.shale);

            Block target = nearRhy >= 2 ? Blocks.rhyolite : (nearDark >= 2 ? WHBlocksEnvironment.darkRock : Blocks.stone);
            tile.setFloor(target.asFloor());
            trim--;
        }
    }

    /**
     * Hard rule: yellow floors must not sit in/near radiation zones.
     */
    private void sanitizeYellowNearRadiation(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            Block floor = tile.floor();
            if(floor != Blocks.yellowStone && floor != Blocks.yellowStonePlates) continue;

            int radNear = countRadiationRelatedInRadius(ctx, tile.x, tile.y, 2);
            if(radNear <= 0) continue;

            int radCore = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.radiationSand)
            + countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.radiationRockFloor)
            + countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.radiationCraters);
            int rhyNear = countNearFloor(ctx, tile.x, tile.y, Blocks.rhyolite)
            + countNearFloor(ctx, tile.x, tile.y, Blocks.roughRhyolite);

            Block target;
            if(radCore >= 3){
                target = WHBlocksEnvironment.radiationRockFloor;
            }else if(rhyNear >= 2){
                target = Blocks.rhyolite;
            }else{
                target = WHBlocksEnvironment.darkRock;
            }

            if(target != floor){
                tile.setFloor(target.asFloor());
            }
        }
    }

    /**
     * Pulls overly large red/purple masses back into neutral/yellow host floors so biome coverage stays balanced.
     */
    private void rebalanceRedPurpleCoverage(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(!isYellowRhyoliteAdjustable(ctx, x, y)) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isRedColorFloor(floor) && !isPurpleColorFloor(floor)) continue;

                    int nearRed = 0, nearPurple = 0, nearYellow = 0, nearBase = 0;
                    for(Point2 p : Geometry.d8){
                        Block near = floors[indexOf(x + p.x, y + p.y, width)];
                        if(isRedColorFloor(near)) nearRed++;
                        if(isPurpleColorFloor(near)) nearPurple++;
                        if(near == Blocks.yellowStone || near == Blocks.yellowStonePlates || near == Blocks.rhyolite){
                            nearYellow++;
                        }
                        if(isRedPurpleFallbackHost(near)){
                            nearBase++;
                        }
                    }

                    float balance = Simplex.noise2d(ctx.seed + 973 + it * 9, 2, 0.60f, 1f / 24f, x + 14.3f, y - 5.8f);
                    Block target = null;

                    if(isRedColorFloor(floor)){
                        if(nearRed >= 6 && (nearPurple >= 2 || nearYellow <= 2) && balance < 0.38f){
                            if(nearYellow >= 4){
                                target = balance > 0.10f ? Blocks.rhyolite : Blocks.yellowStonePlates;
                            }else{
                                target = pickRedPurpleFallbackFloor(floors, width, height, x, y);
                            }
                        }else if(nearRed <= 1 && nearBase >= 3){
                            target = pickRedPurpleFallbackFloor(floors, width, height, x, y);
                        }
                    }else if(isPurpleColorFloor(floor)){
                        if(nearPurple >= 6 && nearYellow <= 2 && balance < 0.42f){
                            target = nearRed >= 2 ? Blocks.rhyolite : pickRedPurpleFallbackFloor(floors, width, height, x, y);
                        }else if(nearPurple <= 1 && nearBase >= 3){
                            target = pickRedPurpleFallbackFloor(floors, width, height, x, y);
                        }else if(floor == Blocks.roughRhyolite && nearYellow >= 3 && balance < 0.50f){
                            target = Blocks.rhyolite;
                        }
                    }

                    if(target != null && target != floor){
                        writes[indexOf(x, y, width)] = target;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    /**
     * Breaks broad continuous red bands (especially near map edges) into darker/rhyolite transitions.
     */
    private void softenLargeRedBands(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(!isYellowRhyoliteAdjustable(ctx, x, y)) continue;

                    int idx = indexOf(x, y, width);
                    Block floor = floors[idx];
                    if(!isRedColorFloor(floor)) continue;

                    int nearRed = 0, nearPurple = 0, nearBase = 0;
                    for(Point2 p : Geometry.d8){
                        Block near = floors[indexOf(x + p.x, y + p.y, width)];
                        if(isRedColorFloor(near)) nearRed++;
                        if(isPurpleColorFloor(near)) nearPurple++;
                        if(isRedPurpleFallbackHost(near)) nearBase++;
                    }

                    int redArea = countRedInRadiusFromSnapshot(floors, width, height, x, y, 3);
                    int edgeDist = Math.min(Math.min(x, width - 1 - x), Math.min(y, height - 1 - y));
                    float carve = Simplex.noise2d(ctx.seed + 989 + it * 11, 2, 0.60f, 1f / 18f, x + 6.3f, y - 9.1f);

                    boolean edgeBand = edgeDist <= 8 && nearRed >= 4;
                    boolean broadMass = redArea >= 34 && nearBase <= 2;
                    boolean overFlat = redArea >= 40 && nearPurple <= 1 && carve < 0.68f;
                    if(!(edgeBand || broadMass || overFlat)) continue;

                    Block target = nearPurple >= 2 && carve > 0.06f
                    ? Blocks.rhyolite
                    : pickRedPurpleFallbackFloor(floors, width, height, x, y);

                    if(target != floor){
                        writes[idx] = target;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    private boolean isRedColorFloor(Block floor){
        return floor == Blocks.redmat
        || floor == Blocks.redStone
        || floor == Blocks.denseRedStone;
    }

    private boolean isPurpleColorFloor(Block floor){
        return floor == Blocks.roughRhyolite
        || floor == WHBlocksEnvironment.cobaltStone;
    }

    private boolean isRedPurpleFallbackHost(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.rhyolite
        || floor == Blocks.carbonStone
        || floor == Blocks.stone
        || floor == Blocks.shale
        || floor == Blocks.dacite
        || floor == Blocks.craters
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private Block pickRedPurpleFallbackFloor(Block[] floors, int width, int height, int x, int y){
        int dark = 0, rhyolite = 0, yellow = 0, stone = 0;

        for(Point2 p : Geometry.d8){
            int nx = x + p.x, ny = y + p.y;
            if(nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
            Block near = floors[indexOf(nx, ny, width)];

            if(near == WHBlocksEnvironment.darkRock || near == WHBlocksEnvironment.trachyte || near == Blocks.carbonStone){
                dark++;
            }else if(near == Blocks.rhyolite){
                rhyolite++;
            }else if(near == Blocks.yellowStone || near == Blocks.yellowStonePlates){
                yellow++;
            }else if(near == Blocks.stone || near == Blocks.shale || near == Blocks.dacite || near == Blocks.craters){
                stone++;
            }
        }

        if(dark >= 2) return WHBlocksEnvironment.darkRock;
        if(rhyolite >= 3) return Blocks.rhyolite;
        if(yellow >= 5) return Blocks.yellowStonePlates;
        if(yellow >= 2) return Blocks.rhyolite;
        if(stone >= 3) return Blocks.stone;
        return WHBlocksEnvironment.darkRock;
    }

    private int countRedInRadiusFromSnapshot(Block[] floors, int width, int height, int x, int y, int radius){
        int count = 0;
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                int wx = x + rx, wy = y + ry;
                if(wx < 0 || wy < 0 || wx >= width || wy >= height) continue;
                if(isRedColorFloor(floors[indexOf(wx, wy, width)])){
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Restore dark-rock backbone when yellow/rhyolite expansion grows too broad.
     */
    private void restoreDarkRockPresence(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(!isYellowRhyoliteAdjustable(ctx, x, y)) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(floor != Blocks.yellowStone && floor != Blocks.yellowStonePlates && floor != Blocks.rhyolite && floor != Blocks.roughRhyolite){
                        continue;
                    }

                    int nearDark = 0, nearYellow = 0, nearRhy = 0;
                    for(Point2 p : Geometry.d8){
                        Block near = floors[indexOf(x + p.x, y + p.y, width)];
                        if(near == WHBlocksEnvironment.darkRock || near == WHBlocksEnvironment.trachyte || near == Blocks.carbonStone){
                            nearDark++;
                        }
                        if(near == Blocks.yellowStone || near == Blocks.yellowStonePlates){
                            nearYellow++;
                        }
                        if(near == Blocks.rhyolite || near == Blocks.roughRhyolite){
                            nearRhy++;
                        }
                    }

                    float carve = Simplex.noise2d(ctx.seed + 1007 + it * 13, 2, 0.60f, 1f / 17f, x + 12.5f, y - 14.1f);
                    Block target = null;

                    if((floor == Blocks.yellowStone || floor == Blocks.yellowStonePlates) && nearDark >= 3 && nearYellow >= 4 && carve < 0.40f){
                        target = nearRhy >= 3 && carve > 0.08f ? Blocks.rhyolite : WHBlocksEnvironment.darkRock;
                    }else if(floor == Blocks.yellowStone && nearDark >= 2 && nearYellow <= 2 && carve < 0.58f){
                        target = WHBlocksEnvironment.darkRock;
                    }else if(floor == Blocks.yellowStonePlates && nearDark >= 4 && nearYellow <= 3 && carve < 0.56f){
                        target = nearRhy >= 2 ? Blocks.rhyolite : WHBlocksEnvironment.darkRock;
                    }else if(floor == Blocks.roughRhyolite && nearDark >= 2 && nearYellow >= 4 && carve < 0.48f){
                        target = Blocks.rhyolite;
                    }else if(floor == Blocks.rhyolite && nearDark >= 4 && nearYellow <= 2 && carve < 0.52f){
                        target = WHBlocksEnvironment.darkRock;
                    }

                    if(target != null && target != floor){
                        writes[indexOf(x, y, width)] = target;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    /**
     * Unify mainland palette transitions so dark/metal/yellow/rhyolite regions connect coherently
     * without flattening into one dominant color.
     */
    private void harmonizeMainlandPalette(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(!isYellowRhyoliteAdjustable(ctx, x, y)) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(!isMainlandPaletteFloor(floor)) continue;

                    int darkNear = 0, metalNear = 0, yellowNear = 0, rhyNear = 0, neutralNear = 0;
                    for(Point2 p : Geometry.d8){
                        Block near = floors[indexOf(x + p.x, y + p.y, width)];
                        if(near == WHBlocksEnvironment.darkRock || near == WHBlocksEnvironment.trachyte || near == Blocks.carbonStone){
                            darkNear++;
                        }
                        if(near == WHBlocksEnvironment.manganeseStone
                        || near == WHBlocksEnvironment.chromiteStone
                        || near == WHBlocksEnvironment.cobaltStone
                        || near == Blocks.ferricStone
                        || near == Blocks.ferricCraters){
                            metalNear++;
                        }
                        if(near == Blocks.yellowStone || near == Blocks.yellowStonePlates){
                            yellowNear++;
                        }
                        if(near == Blocks.rhyolite || near == Blocks.roughRhyolite){
                            rhyNear++;
                        }
                        if(near == Blocks.stone || near == Blocks.shale || near == Blocks.dacite || near == Blocks.craters){
                            neutralNear++;
                        }
                    }

                    float shape = Simplex.noise2d(ctx.seed + 1029 + it * 11, 2, 0.60f, 1f / 16f, x + 9.4f, y - 12.6f);
                    Block target = null;

                    if(floor == Blocks.yellowStone){
                        if(darkNear + metalNear >= 5 && yellowNear <= 2 && shape < 0.64f){
                            target = rhyNear >= 2 ? Blocks.rhyolite : WHBlocksEnvironment.darkRock;
                        }
                    }else if(floor == Blocks.yellowStonePlates){
                        if(darkNear >= 4 && yellowNear <= 2 && shape < 0.52f){
                            target = rhyNear >= 2 ? Blocks.rhyolite : WHBlocksEnvironment.darkRock;
                        }
                    }else if(floor == Blocks.rhyolite || floor == Blocks.roughRhyolite){
                        if(yellowNear == 0 && darkNear >= 5 && shape < 0.50f){
                            target = WHBlocksEnvironment.darkRock;
                        }
                    }else if(floor == WHBlocksEnvironment.cobaltStone){
                        if(yellowNear >= 4 && metalNear <= 2 && shape < 0.56f){
                            target = WHBlocksEnvironment.chromiteStone;
                        }
                    }else if(floor == WHBlocksEnvironment.manganeseStone || floor == WHBlocksEnvironment.chromiteStone){
                        if(yellowNear >= 5 && metalNear <= 2 && shape < 0.44f){
                            target = WHBlocksEnvironment.darkRock;
                        }
                    }else if(floor == WHBlocksEnvironment.trachyte){
                        if(darkNear >= 3 && shape < 0.72f){
                            target = WHBlocksEnvironment.darkRock;
                        }else if(yellowNear >= 4 && rhyNear >= 1 && shape < 0.56f){
                            target = Blocks.rhyolite;
                        }
                    }else if(floor == WHBlocksEnvironment.darkRock){
                        if(yellowNear >= 5 && rhyNear >= 2 && metalNear <= 2 && shape > 0.24f){
                            target = shape > 0.70f ? Blocks.yellowStonePlates : Blocks.rhyolite;
                        }
                    }else if(floor == Blocks.stone || floor == Blocks.shale || floor == Blocks.dacite || floor == Blocks.craters){
                        if(darkNear >= 4 && yellowNear + rhyNear >= 2 && neutralNear <= 3 && shape < 0.38f){
                            target = WHBlocksEnvironment.darkRock;
                        }
                    }

                    if(target != null && target != floor){
                        writes[indexOf(x, y, width)] = target;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    /**
     * Break large one-color mainland blobs so the map keeps readable mixed biomes instead of giant flat patches.
     */
    private void breakMonochromeMainlandPatches(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 3; x < width - 3; x++){
                for(int y = 3; y < height - 3; y++){
                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;
                    if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                    if(isProtected(ctx, x, y)) continue;
                    if(nearLiquid(ctx, x, y, 1)) continue;
                    if(nearFloor(ctx, x, y, Blocks.slag, 2)) continue;

                    int idx = indexOf(x, y, width);
                    Block floor = floors[idx];
                    if(!isMainlandPaletteFloor(floor)) continue;

                    int same = countFloorInRadiusFromSnapshot(floors, width, height, x, y, floor, 3);
                    if(same < 34) continue;

                    int darkNear = 0, yellowNear = 0, rhyNear = 0, manganeseNear = 0, chromiteNear = 0, cobaltNear = 0;
                    for(Point2 p : Geometry.d8){
                        Block near = floors[indexOf(x + p.x, y + p.y, width)];
                        if(near == WHBlocksEnvironment.darkRock || near == WHBlocksEnvironment.trachyte || near == Blocks.carbonStone){
                            darkNear++;
                        }
                        if(near == Blocks.yellowStone || near == Blocks.yellowStonePlates){
                            yellowNear++;
                        }
                        if(near == Blocks.rhyolite || near == Blocks.roughRhyolite){
                            rhyNear++;
                        }
                        if(near == WHBlocksEnvironment.manganeseStone) manganeseNear++;
                        if(near == WHBlocksEnvironment.chromiteStone) chromiteNear++;
                        if(near == WHBlocksEnvironment.cobaltStone) cobaltNear++;
                    }

                    float carve = Simplex.noise2d(ctx.seed + 1043 + it * 13, 2, 0.60f, 1f / 19f, x + 8.6f, y - 10.2f);
                    if(carve < 0.22f) continue;

                    Block target = null;
                    if(floor == WHBlocksEnvironment.chromiteStone){
                        target = manganeseNear >= 2 && carve < 0.62f ? WHBlocksEnvironment.manganeseStone : WHBlocksEnvironment.darkRock;
                    }else if(floor == WHBlocksEnvironment.manganeseStone){
                        target = chromiteNear >= 2 && carve < 0.58f ? WHBlocksEnvironment.chromiteStone : WHBlocksEnvironment.darkRock;
                    }else if(floor == WHBlocksEnvironment.cobaltStone){
                        target = chromiteNear >= 1 ? WHBlocksEnvironment.chromiteStone : WHBlocksEnvironment.darkRock;
                    }else if(floor == WHBlocksEnvironment.darkRock || floor == WHBlocksEnvironment.trachyte || floor == Blocks.carbonStone){
                        if(yellowNear >= 4){
                            target = Blocks.rhyolite;
                        }else if(rhyNear >= 3){
                            target = Blocks.roughRhyolite;
                        }else if(chromiteNear + manganeseNear + cobaltNear >= 3){
                            target = chromiteNear >= manganeseNear ? WHBlocksEnvironment.chromiteStone : WHBlocksEnvironment.manganeseStone;
                        }else{
                            target = WHBlocksEnvironment.trachyte;
                        }
                    }else if(floor == Blocks.yellowStone || floor == Blocks.yellowStonePlates){
                        target = rhyNear >= 2 ? Blocks.rhyolite : WHBlocksEnvironment.darkRock;
                    }else if(floor == Blocks.rhyolite || floor == Blocks.roughRhyolite){
                        target = yellowNear >= 3 ? Blocks.yellowStonePlates : WHBlocksEnvironment.darkRock;
                    }else if(floor == Blocks.stone || floor == Blocks.shale || floor == Blocks.dacite || floor == Blocks.craters){
                        target = darkNear >= 4 ? WHBlocksEnvironment.darkRock : Blocks.rhyolite;
                    }else if(floor == Blocks.ferricStone || floor == Blocks.ferricCraters){
                        target = darkNear >= 3 ? WHBlocksEnvironment.darkRock : WHBlocksEnvironment.chromiteStone;
                    }

                    if(target != null && target != floor){
                        writes[idx] = target;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    private boolean isMainlandPaletteFloor(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.carbonStone
        || floor == Blocks.stone
        || floor == Blocks.shale
        || floor == Blocks.dacite
        || floor == Blocks.craters
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private int countFloorInRadiusFromSnapshot(Block[] floors, int width, int height, int x, int y, Block floor, int radius){
        int count = 0;
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                int wx = x + rx, wy = y + ry;
                if(wx < 0 || wy < 0 || wx >= width || wy >= height) continue;
                if(floors[indexOf(wx, wy, width)] == floor){
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isProtected(GenContext ctx, int x, int y){
        if(ctx.spawnRoom != null){
            int radius = ctx.spawnRoom.radius + protectedRoomPadding;
            if(Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, radius)){
                return true;
            }
        }

        for(RoomAnchor enemy : ctx.enemyRooms){
            int radius = enemy.radius + 1;
            if(Mathf.within(x, y, enemy.x, enemy.y, radius)){
                return true;
            }
        }

        return false;
    }

    private Block[] snapshotFloors(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        Block[] floors = new Block[width * height];

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                floors[indexOf(x, y, width)] = ctx.tiles.getn(x, y).floor();
            }
        }

        return floors;
    }

    private boolean[] snapshotOpen(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        boolean[] open = new boolean[width * height];

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                open[indexOf(x, y, width)] = ctx.tiles.getn(x, y).block() == Blocks.air;
            }
        }

        return open;
    }

    private int countOpen4(boolean[] open, int width, int height, int x, int y){
        int result = 0;
        for(Point2 point : Geometry.d4){
            int wx = x + point.x;
            int wy = y + point.y;
            if(wx < 0 || wy < 0 || wx >= width || wy >= height) continue;
            if(open[indexOf(wx, wy, width)]){
                result++;
            }
        }
        return result;
    }

    private int countOpen8(boolean[] open, int width, int height, int x, int y){
        int result = 0;
        for(Point2 point : Geometry.d8){
            int wx = x + point.x;
            int wy = y + point.y;
            if(wx < 0 || wy < 0 || wx >= width || wy >= height) continue;
            if(open[indexOf(wx, wy, width)]){
                result++;
            }
        }
        return result;
    }

    private int countWall8(boolean[] open, int width, int height, int x, int y){
        int result = 0;
        for(Point2 point : Geometry.d8){
            int wx = x + point.x;
            int wy = y + point.y;
            if(wx < 0 || wy < 0 || wx >= width || wy >= height){
                result++;
            }else if(!open[indexOf(wx, wy, width)]){
                result++;
            }
        }
        return result;
    }

    private int countWallNeighborsInRadius(GenContext ctx, int x, int y, int radius){
        int result = 0;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox == 0 && oy == 0) continue;
                int wx = x + ox;
                int wy = y + oy;
                Tile near = ctx.tiles.get(wx, wy);
                if(near == null || near.block() != Blocks.air){
                    result++;
                }
            }
        }
        return result;
    }

    private int countNearFloor(GenContext ctx, int x, int y, Block floor){
        int count = 0;
        for(Point2 point : Geometry.d8){
            Tile near = ctx.tiles.get(x + point.x, y + point.y);
            if(near != null && near.floor() == floor){
                count++;
            }
        }
        return count;
    }

    private int countNearFloorFromSnapshot(Block[] floors, int width, int height, int x, int y, Block floor){
        int count = 0;
        for(Point2 point : Geometry.d8){
            int wx = x + point.x;
            int wy = y + point.y;
            if(wx < 0 || wy < 0 || wx >= width || wy >= height) continue;
            if(floors[indexOf(wx, wy, width)] == floor){
                count++;
            }
        }
        return count;
    }

    private int countGeothermalNeighbors(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 point : Geometry.d8){
            Tile near = ctx.tiles.get(x + point.x, y + point.y);
            if(near != null && isGeothermalFloor(near.floor())){
                count++;
            }
        }
        return count;
    }

    private int countGeothermalNearFromSnapshot(Block[] floors, int width, int height, int x, int y){
        int count = 0;
        for(Point2 point : Geometry.d8){
            int wx = x + point.x;
            int wy = y + point.y;
            if(wx < 0 || wy < 0 || wx >= width || wy >= height) continue;
            if(isGeothermalFloor(floors[indexOf(wx, wy, width)])){
                count++;
            }
        }
        return count;
    }

    private int indexOf(int x, int y, int width){
        return x + y * width;
    }

    private boolean nearFloor(GenContext ctx, int x, int y, Block floor, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.floor() == floor){
                    return true;
                }
            }
        }
        return false;
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

    private boolean isTransitionNoiseAdjustable(GenContext ctx, int x, int y){
        Tile tile = ctx.tiles.getn(x, y);
        if(tile.block() != Blocks.air) return false;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        if(isProtected(ctx, x, y)) return false;
        if(nearLiquid(ctx, x, y, 1)) return false;
        return !nearFloor(ctx, x, y, Blocks.slag, 1);
    }

    private int countMetalCoverageDomainTiles(GenContext ctx){
        int count = 0;
        for(Tile tile : ctx.tiles){
            if(!isMetalCoverageAdjustable(ctx, tile.x, tile.y)) continue;
            if(isMetalCoverageDomain(tile.floor())){
                count++;
            }
        }
        return count;
    }

    private int countMetalCoverageTiles(GenContext ctx, Block floor){
        int count = 0;
        for(Tile tile : ctx.tiles){
            if(tile.floor() != floor) continue;
            if(isMetalCoverageAdjustable(ctx, tile.x, tile.y)){
                count++;
            }
        }
        return count;
    }

    private boolean isMetalCoverageAdjustable(GenContext ctx, int x, int y){
        Tile tile = ctx.tiles.getn(x, y);
        if(tile.block() != Blocks.air) return false;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        if(isProtected(ctx, x, y)) return false;
        if(nearLiquid(ctx, x, y, 1)) return false;
        return !nearFloor(ctx, x, y, Blocks.slag, 2);
    }

    private Block pickMetalFallbackFloor(Block[] floors, int width, int height, int x, int y, Block target){
        int dark = 0, manganese = 0, chromite = 0, cobalt = 0;

        for(Point2 point : Geometry.d8){
            int wx = x + point.x;
            int wy = y + point.y;
            if(wx < 0 || wy < 0 || wx >= width || wy >= height) continue;

            Block near = floors[indexOf(wx, wy, width)];
            if(near == WHBlocksEnvironment.darkRock){
                dark++;
            }else if(near == WHBlocksEnvironment.manganeseStone){
                manganese++;
            }else if(near == WHBlocksEnvironment.chromiteStone){
                chromite++;
            }else if(near == WHBlocksEnvironment.cobaltStone){
                cobalt++;
            }
        }

        if(target != WHBlocksEnvironment.manganeseStone && manganese >= 4 && manganese >= chromite && manganese >= cobalt){
            return WHBlocksEnvironment.manganeseStone;
        }
        if(target != WHBlocksEnvironment.chromiteStone && chromite >= 4 && chromite >= manganese && chromite >= cobalt){
            return WHBlocksEnvironment.chromiteStone;
        }
        if(target != WHBlocksEnvironment.cobaltStone && cobalt >= 5 && cobalt >= chromite + 1 && cobalt >= manganese + 1){
            return WHBlocksEnvironment.cobaltStone;
        }
        if(dark >= 3){
            return WHBlocksEnvironment.darkRock;
        }
        return WHBlocksEnvironment.darkRock;
    }

    private Block dominantNonGeothermalFloor(Block[] floors, int width, int height, int x, int y){
        Block[] candidates = new Block[8];
        int[] counts = new int[8];
        int unique = 0;
        int bestCount = 0;
        Block best = null;

        for(Point2 point : Geometry.d8){
            int wx = x + point.x;
            int wy = y + point.y;
            if(wx < 0 || wy < 0 || wx >= width || wy >= height) continue;

            Block near = floors[indexOf(wx, wy, width)];
            if(near == null || !near.asFloor().hasSurface() || near.asFloor().isLiquid) continue;
            if(isGeothermalFloor(near)) continue;
            if(near.asFloor().attributes.get(Attribute.steam) != 0f) continue;

            int slot = -1;
            for(int n = 0; n < unique; n++){
                if(candidates[n] == near){
                    slot = n;
                    break;
                }
            }

            if(slot == -1){
                slot = unique++;
                candidates[slot] = near;
                counts[slot] = 0;
            }

            int count = ++counts[slot];
            if(count > bestCount){
                bestCount = count;
                best = near;
            }
        }

        if(best != null) return best;
        return WHBlocksEnvironment.darkRock;
    }

    private boolean isBlendableFloor(Block floor){
        if(floor == null) return false;
        return floor.asFloor().hasSurface()
        && !floor.asFloor().isLiquid
        && floor.asFloor().attributes.get(Attribute.steam) == 0f
        && floorGroup(floor) != 0;
    }

    private boolean isFloorCompatible(Block a, Block b){
        int ga = floorGroup(a), gb = floorGroup(b);
        if(ga == 0 || gb == 0) return false;
        if(ga == gb) return true;

        return (ga == 1 && gb == 3) || (ga == 3 && gb == 1)
        || (ga == 3 && gb == 4) || (ga == 4 && gb == 3)
        || (ga == 2 && gb == 3) || (ga == 3 && gb == 2);
    }

    private int floorGroup(Block floor){
        if(floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.darksand){
            return 1;
        }

        if(floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters){
            return 2;
        }

        if(floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.carbonStone
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.shale
        || floor == Blocks.stone
        || floor == Blocks.craters
        || floor == Blocks.crystallineStone
        || floor == Blocks.crystalFloor
        || floor == Blocks.dacite
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters){
            return 3;
        }

        if(floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock){
            return 4;
        }

        if(floor == Blocks.redmat
        || floor == Blocks.redStone
        || floor == Blocks.denseRedStone
        || floor == Blocks.redIce){
            return 5;
        }

        return 0;
    }

    private boolean isGeothermalFloor(Block floor){
        return floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock;
    }

    private boolean isDarkRockConvertible(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.carbonStone
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.shale
        || floor == Blocks.stone
        || floor == Blocks.craters
        || floor == Blocks.dacite
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters
        || isGeothermalFloor(floor);
    }

    private boolean isMetalFieldFloor(Block floor){
        return floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private boolean isMetalCoverageDomain(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == Blocks.stone
        || floor == Blocks.shale
        || floor == Blocks.carbonStone
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private boolean isMetalCoverageHost(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.stone
        || floor == Blocks.shale
        || floor == Blocks.carbonStone
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private boolean isMetalFieldConvertible(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == Blocks.stone
        || floor == Blocks.shale
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.carbonStone
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private boolean isSpeckleSensitiveFloor(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.scorchedEarth;
    }

    private void seedDryRadiationFields(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        Block[] writes = new Block[width * height];

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!isDryRadiationConvertible(tile.floor())) continue;
            if(isProtected(ctx, tile.x, tile.y)) continue;
            if(nearLiquid(ctx, tile.x, tile.y, 2)) continue;
            if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 6)) continue;

            int host = countDryRadiationHostInRadius(ctx, tile.x, tile.y, 3);
            if(host < 27) continue;

            float macro = Simplex.noise2d(ctx.seed + 881, 2, 0.58f, 1f / 52f, tile.x + 17.2f, tile.y - 29.4f);
            float detail = Simplex.noise2d(ctx.seed + 887, 2, 0.62f, 1f / 15f, tile.x - 13.8f, tile.y + 22.6f);
            float field = macro * 0.76f + detail * 0.24f;
            if(field < 0.60f) continue;

            writes[indexOf(tile.x, tile.y, width)] = radiationFloorFromField(field);
        }

        applyFloorWrites(ctx, writes, width);

        for(int it = 0; it < 2; it++){
            Block[] spread = new Block[width * height];

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;
                if(!isDryRadiationConvertible(tile.floor())) continue;
                if(isProtected(ctx, tile.x, tile.y)) continue;
                if(nearLiquid(ctx, tile.x, tile.y, 1)) continue;
                if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 5)) continue;

                int radNear = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.radiationSand)
                + countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.radiationRockFloor)
                + countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.radiationCraters);
                if(radNear < 5) continue;

                float shape = Simplex.noise2d(ctx.seed + 893 + it * 13, 2, 0.60f, 1f / 11f, tile.x + 6.1f, tile.y - 8.3f);
                float chance = radNear >= 6 ? 0.42f : 0.24f;
                if(shape > -0.08f && ctx.rand.chance(chance)){
                    spread[indexOf(tile.x, tile.y, width)] = radiationFloorFromField(shape + 0.6f);
                }
            }

            applyFloorWrites(ctx, spread, width);
        }

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!isRadiationFloor(tile.floor())) continue;
            if(isProtected(ctx, tile.x, tile.y)) continue;

            int near = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.radiationSand)
            + countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.radiationRockFloor)
            + countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.radiationCraters);
            int darkNear = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.darkRock);
            if(near <= 3 && ctx.rand.chance(0.92f)){
                tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
            }else if(darkNear >= 4 && ctx.rand.chance(0.42f)){
                tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
            }
        }
    }

    private Block radiationFloorFromField(float field){
        if(field > 0.76f) return WHBlocksEnvironment.radiationRockFloor;
        if(field > 0.61f) return WHBlocksEnvironment.radiationCraters;
        return WHBlocksEnvironment.radiationSand;
    }

    private int countDryRadiationHostInRadius(GenContext ctx, int x, int y, int radius){
        int count = 0;
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && isDryRadiationConvertible(near.floor()) && !near.floor().isLiquid){
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isDryRadiationConvertible(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == Blocks.stone
        || floor == Blocks.shale
        || floor == Blocks.craters
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private boolean isRadiationFloor(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters;
    }

    private int countRadiationRelatedInRadius(GenContext ctx, int x, int y, int radius){
        int count = 0;
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && isRadiationRelatedFloor(near.floor())){
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isRadiationRelatedFloor(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters
        || floor == WHBlocksEnvironment.radiationSandWater
        || floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationWater
        || floor == WHBlocksEnvironment.radiationWaterDeep;
    }

    private void seedMineralSandFields(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        Block[] writes = new Block[width * height];

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(tile.floor() != WHBlocksEnvironment.darkRock) continue;
            if(isProtected(ctx, tile.x, tile.y)) continue;
            if(nearLiquid(ctx, tile.x, tile.y, 2)) continue;
            if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 7)) continue;

            int darkArea = countFloorInRadius(ctx, tile.x, tile.y, WHBlocksEnvironment.darkRock, 4);
            if(darkArea < 34) continue;

            float macro = Simplex.noise2d(ctx.seed + 843, 2, 0.58f, 1f / 48f, tile.x + 11.3f, tile.y - 27.7f);
            float detail = Simplex.noise2d(ctx.seed + 847, 2, 0.62f, 1f / 16f, tile.x - 23.1f, tile.y + 19.5f);
            float field = macro * 0.78f + detail * 0.22f;
            if(field < 0.41f) continue;

            Block target = field > 0.50f ? WHBlocksEnvironment.mineralSand : WHBlocksEnvironment.mineralSandstone;
            writes[indexOf(tile.x, tile.y, width)] = target;
        }

        applyFloorWrites(ctx, writes, width);

        for(int it = 0; it < 2; it++){
            Block[] spread = new Block[width * height];

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;
                if(tile.floor() != WHBlocksEnvironment.darkRock) continue;
                if(isProtected(ctx, tile.x, tile.y)) continue;
                if(nearLiquid(ctx, tile.x, tile.y, 1)) continue;
                if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 6)) continue;

                int sandNear = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.mineralSand)
                + countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.mineralSandstone);
                if(sandNear < 3) continue;

                float shape = Simplex.noise2d(ctx.seed + 851 + it * 11, 2, 0.60f, 1f / 10f, tile.x + 4.9f, tile.y - 8.6f);
                float chance = sandNear >= 5 ? 0.78f : 0.54f;
                if(shape > -0.02f && ctx.rand.chance(chance)){
                    spread[indexOf(tile.x, tile.y, width)] = shape > 0.02f ? WHBlocksEnvironment.mineralSand : WHBlocksEnvironment.mineralSandstone;
                }
            }

            applyFloorWrites(ctx, spread, width);
        }

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(tile.floor() == WHBlocksEnvironment.quartzSand){
                int nearQuartz = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.quartzSand);
                if(nearQuartz <= 2 && ctx.rand.chance(0.86f)){
                    tile.setFloor(WHBlocksEnvironment.mineralSandstone.asFloor());
                }
                continue;
            }

            if(tile.floor() != WHBlocksEnvironment.mineralSand && tile.floor() != WHBlocksEnvironment.mineralSandstone) continue;
            if(isProtected(ctx, tile.x, tile.y)) continue;

            int nearSand = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.mineralSand)
            + countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.mineralSandstone);
            if(nearSand <= 1 && ctx.rand.chance(0.82f)){
                tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
            }else if(tile.floor() == WHBlocksEnvironment.mineralSandstone){
                int nearQuartz = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.quartzSand);
                if(nearSand >= 7 && nearQuartz >= 2 && ctx.rand.chance(0.03f)){
                    tile.setFloor(WHBlocksEnvironment.quartzSand.asFloor());
                }
            }
        }
    }

    private void seedCrystalFieldsOnDarkRock(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        Block[] writes = new Block[width * height];

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(tile.floor() != WHBlocksEnvironment.darkRock) continue;
            if(isProtected(ctx, tile.x, tile.y)) continue;
            if(nearLiquid(ctx, tile.x, tile.y, 2)) continue;
            if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 5)) continue;

            int darkArea = countFloorInRadius(ctx, tile.x, tile.y, WHBlocksEnvironment.darkRock, 3);
            if(darkArea < 25) continue;

            float macro = Simplex.noise2d(ctx.seed + 811, 2, 0.58f, 1f / 34f, tile.x + 21.7f, tile.y - 14.3f);
            float detail = Simplex.noise2d(ctx.seed + 817, 2, 0.62f, 1f / 12f, tile.x - 33.5f, tile.y + 29.9f);
            float field = macro * 0.74f + detail * 0.26f;

            if(field > 0.57f){
                writes[indexOf(tile.x, tile.y, width)] = field > 0.72f ? Blocks.crystalFloor : Blocks.crystallineStone;
            }
        }

        applyFloorWrites(ctx, writes, width);

        for(int i = 0; i < 2; i++){
            Block[] spread = new Block[width * height];

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;
                if(tile.floor() != WHBlocksEnvironment.darkRock) continue;
                if(isProtected(ctx, tile.x, tile.y)) continue;
                if(nearLiquid(ctx, tile.x, tile.y, 1)) continue;
                if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 4)) continue;

                int crystalNear = countNearFloor(ctx, tile.x, tile.y, Blocks.crystallineStone)
                + countNearFloor(ctx, tile.x, tile.y, Blocks.crystalFloor);
                if(crystalNear < 3) continue;

                float shape = Simplex.noise2d(ctx.seed + 823 + i * 7, 2, 0.60f, 1f / 9f, tile.x + 9.3f, tile.y - 7.4f);
                float chance = crystalNear >= 5 ? 0.72f : 0.48f;
                if(shape > -0.18f && ctx.rand.chance(chance)){
                    spread[indexOf(tile.x, tile.y, width)] = shape > 0.34f ? Blocks.crystalFloor : Blocks.crystallineStone;
                }
            }

            applyFloorWrites(ctx, spread, width);
        }

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(tile.floor() != Blocks.crystallineStone && tile.floor() != Blocks.crystalFloor) continue;
            if(isProtected(ctx, tile.x, tile.y)) continue;

            int near = countNearFloor(ctx, tile.x, tile.y, Blocks.crystallineStone)
            + countNearFloor(ctx, tile.x, tile.y, Blocks.crystalFloor);
            if(near <= 1 && ctx.rand.chance(0.86f)){
                tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
            }
        }
    }

    // Finalize large dark-rock basins around slag after all other recolor passes.
    private void solidifyDarkRockSlagBasins(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < 2; it++){
            Block[] writes = new Block[width * height];

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                if(tile.floor() == Blocks.slag) continue;
                if(isProtected(ctx, tile.x, tile.y)) continue;
                if(isDarkRockSolidifyBlocked(tile.floor())) continue;
                if(!nearFloor(ctx, tile.x, tile.y, Blocks.slag, 8)) continue;

                int slagNear = countFloorInRadius(ctx, tile.x, tile.y, Blocks.slag, 4);
                if(slagNear <= 0) continue;

                float macro = Simplex.noise2d(ctx.seed + 971 + it * 13, 2, 0.60f, 1f / 26f, tile.x + 15.1f, tile.y - 31.7f);
                float chance = slagNear >= 6 ? 0.95f : (slagNear >= 3 ? 0.82f : 0.64f);
                if(macro > -0.32f && ctx.rand.chance(chance)){
                    writes[indexOf(tile.x, tile.y, width)] = WHBlocksEnvironment.darkRock;
                }
            }

            applyFloorWrites(ctx, writes, width);
        }

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(tile.floor() == WHBlocksEnvironment.darkRock || tile.floor() == Blocks.slag) continue;
            if(isProtected(ctx, tile.x, tile.y)) continue;
            if(isDarkRockSolidifyBlocked(tile.floor())) continue;
            if(!nearFloor(ctx, tile.x, tile.y, Blocks.slag, 7)) continue;

            int darkNear = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.darkRock);
            if(darkNear >= 6 && ctx.rand.chance(0.86f)){
                tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
            }
        }
    }

    private void consolidateScorchedMainland(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;
                    if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                    if(isProtected(ctx, x, y)) continue;

                    Block floor = floors[indexOf(x, y, width)];
                    if(isScorchedCleanupBlocked(floor)) continue;

                    int nearEarth = countNearFloorFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.scorchedEarth);
                    int nearStone = countNearFloorFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.scorchedStone);
                    int nearScorched = nearEarth + nearStone;
                    int nearHot = countGeothermalNearFromSnapshot(floors, width, height, x, y);
                    int nearSlag = countFloorInRadiusFromSnapshot(floors, width, height, x, y, Blocks.slag, 2);

                    Block target = null;
                    if(nearScorched >= 5 && (nearHot >= 4 || nearSlag >= 1)){
                        if(floor != WHBlocksEnvironment.scorchedEarth && floor != WHBlocksEnvironment.scorchedStone){
                            target = nearStone >= 4 ? WHBlocksEnvironment.scorchedStone : WHBlocksEnvironment.scorchedEarth;
                        }else if(floor == WHBlocksEnvironment.scorchedEarth && nearStone >= 5){
                            target = WHBlocksEnvironment.scorchedStone;
                        }else if(floor == WHBlocksEnvironment.scorchedStone && nearEarth >= 5){
                            target = WHBlocksEnvironment.scorchedEarth;
                        }
                    }else if((floor == WHBlocksEnvironment.scorchedEarth || floor == WHBlocksEnvironment.scorchedStone)
                    && nearScorched <= 1 && nearHot <= 2 && nearSlag == 0){
                        target = WHBlocksEnvironment.darkRock;
                    }

                    if(target != null && target != floor){
                        writes[indexOf(x, y, width)] = target;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    private boolean isDarkRockSolidifyBlocked(Block floor){
        return floor == Blocks.slag
        || floor == Blocks.tar
        || floor == WHBlocksEnvironment.promethium
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock
        || floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationWaterDeep
        || floor == WHBlocksEnvironment.radiationWater
        || floor == WHBlocksEnvironment.radiationSandWater;
    }

    private boolean isScorchedCleanupBlocked(Block floor){
        return floor == Blocks.slag
        || floor == Blocks.tar
        || floor == WHBlocksEnvironment.promethium
        || floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters
        || floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationWaterDeep
        || floor == WHBlocksEnvironment.radiationWater
        || floor == WHBlocksEnvironment.radiationSandWater;
    }

    private int countFloorInRadius(GenContext ctx, int x, int y, Block floor, int radius){
        int count = 0;
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.floor() == floor){
                    count++;
                }
            }
        }
        return count;
    }

    private void applyFloorWrites(GenContext ctx, Block[] writes, int width){
        for(Tile tile : ctx.tiles){
            Block write = writes[indexOf(tile.x, tile.y, width)];
            if(write != null && write != tile.floor()){
                tile.setFloor(write.asFloor());
            }
        }
    }

    private void setWallFromFloor(Tile tile){
        Block wall = tile.floor().asFloor().wall;
        if(wall != null && wall != Blocks.air){
            tile.setBlock(wall);
        }
    }
}

