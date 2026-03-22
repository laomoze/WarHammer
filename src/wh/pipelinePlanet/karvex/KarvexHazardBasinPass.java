package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

/**
 * 中文说明：Karvex 危险盆地塑形：钷、油、熔渣与热域分布。
 */
public class KarvexHazardBasinPass implements GenPass{
    /** @return Stable pass id used in pipeline logs/debug output. */
    @Override
    public String name(){
        return "KarvexHazardBasinPass";
    }

    /** Runs hazard shaping in a fixed order so later steps can assume stabilized floor states. */
    @Override
    public void apply(GenContext ctx){
        seedHazardBasins(ctx);
        expandHazardBasins(ctx, 3);
        enforceHazardMinimums(ctx);
        compactTarAndPromethiumBasins(ctx, 2);
        cleanupIsolatedHazardTiles(ctx);
        softenPromethiumCoreEdges(ctx);
        reinforcePromethiumEdgeTransitions(ctx);
        limitPromethiumSandCoverage(ctx);
        settleGeothermalDarkBase(ctx);
        expandDarkRockNearSlag(ctx, 2);
        scatterHeatNearSlag(ctx);
        seedIndependentHotRockClusters(ctx, 2);
        shapeCoherentScorchedFields(ctx, 3);
        consolidateScorchedFields(ctx, 2);
        pruneIsolatedHeatSpeckles(ctx, 2);
        ensurePromethiumSandHalo(ctx, 5);
    }

    /** Seeds the initial hazard nuclei (promethium, tar, slag, radiation water) from low-frequency fields. */
    private void seedHazardBasins(GenContext ctx){
        float regionScale = KarvexPlanetGenerator.seedDrivenChunkScale(ctx.seed);

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 34f, 20f)) continue;

            Block floor = tile.floor();

            float promA = noise(ctx, ctx.seed + 451, tile.x + 310f, tile.y - 220f, 5, 0.74f, 150f);
            float promB = noise(ctx, ctx.seed + 457, tile.x - 130f, tile.y + 500f, 3, 0.78f, 46f);
            float promRegion = noise(ctx, ctx.seed + 459, tile.x + 710f, tile.y - 510f, 2, 0.64f, regionScale);
            if(isPromethiumCandidate(floor) && promRegion > 0.18f && promA > 0.75f && promB > 0.45f){
                tile.setFloor(WHBlocksEnvironment.promethium.asFloor());
                tile.setOverlay(Blocks.air);
                continue;
            }

            float oilA = noise(ctx, ctx.seed + 401, tile.x - 440f, tile.y + 210f, 5, 0.72f, 125f);
            float oilB = noise(ctx, ctx.seed + 409, tile.x + 91f, tile.y - 370f, 2, 0.92f, 35f);
            float oilC = noise(ctx, ctx.seed + 433, tile.x - 180f, tile.y + 620f, 5, 0.78f, 105f);
            float oilD = noise(ctx, ctx.seed + 439, tile.x + 250f, tile.y - 540f, 2, 0.91f, 32f);
            float oilRegion = noise(ctx, ctx.seed + 445, tile.x + 310f, tile.y - 610f, 2, 0.62f, regionScale);

            boolean oilMatch = isTarCandidate(floor)
            && oilRegion > 0.15f
            && ((oilA > 0.72f && oilB > 0.49f) || (oilC > 0.68f && oilD > 0.46f));
            if(oilMatch && !nearFloor(ctx, tile.x, tile.y, Blocks.slag, 1)){
                tile.setFloor(Blocks.tar.asFloor());
                tile.setOverlay(Blocks.air);
                continue;
            }

            float slagA = noise(ctx, ctx.seed + 421, tile.x + 720f, tile.y - 190f, 6, 0.74f, 140f);
            float slagB = noise(ctx, ctx.seed + 429, tile.x - 210f, tile.y + 470f, 3, 0.78f, 44f);
            if(isSlagCandidate(floor) && slagA > 0.69f && slagB > 0.45f){
                if(nearFloor(ctx, tile.x, tile.y, Blocks.tar, 1) || nearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.promethium, 1)){
                    continue;
                }
                tile.setFloor(Blocks.slag.asFloor());
                tile.setOverlay(Blocks.air);
                continue;
            }

            // Radiation water is generated in KarvexHydrologyPass, not hazard pass.
        }
    }

    /** Grows each hazard type from neighbors using a mark-and-apply pass to avoid order bias. */
    private void expandHazardBasins(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            byte[] marks = new byte[width * height];

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                if(isNearCriticalRoom(ctx, tile.x, tile.y, 30f, 16f)) continue;

                int tar = 0, slag = 0, prom = 0;
                for(Point2 p : Geometry.d8){
                    Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                    if(near == null) continue;

                    if(near.floor() == Blocks.tar) tar++;
                    if(near.floor() == Blocks.slag) slag++;
                    if(near.floor() == WHBlocksEnvironment.promethium) prom++;
                }

                int idx = tile.x + tile.y * width;
                if(prom >= 3 && isPromethiumCandidate(tile.floor()) && ctx.rand.chance(0.82f)){
                    marks[idx] = 3;
                }else if(tar >= 5 && isTarCandidate(tile.floor()) && !nearFloor(ctx, tile.x, tile.y, Blocks.slag, 1) && ctx.rand.chance(0.76f)){
                    marks[idx] = 1;
                }else if(slag >= 4 && isSlagCandidate(tile.floor()) && !nearFloor(ctx, tile.x, tile.y, Blocks.tar, 1) && ctx.rand.chance(0.68f)){
                    marks[idx] = 2;
                }
            }

            for(Tile tile : ctx.tiles){
                int mark = marks[tile.x + tile.y * width];
                if(mark == 0) continue;

                if(mark == 1){
                    tile.setFloor(Blocks.tar.asFloor());
                }else if(mark == 2){
                    tile.setFloor(Blocks.slag.asFloor());
                }else if(mark == 3){
                    tile.setFloor(WHBlocksEnvironment.promethium.asFloor());
                }
                tile.setOverlay(Blocks.air);
            }
        }
    }

    /** Enforces per-hazard minimum area so map gameplay remains resource/terrain consistent. */
    private void enforceHazardMinimums(GenContext ctx){
        int area = ctx.width() * ctx.height();
        int minTar = Math.max(128, area / 2350);
        int minSlag = Math.max(96, area / 2300);
        int minPromethium = Math.max(100, area / 2700);

        int tarCount = countTilesWithFloor(ctx, Blocks.tar);
        int slagCount = countTilesWithFloor(ctx, Blocks.slag);
        int promethiumCount = countTilesWithFloor(ctx, WHBlocksEnvironment.promethium);

        int tries = 0;
        while(tarCount < minTar && tries++ < 18){
            tarCount += stampHazardPatch(ctx, 0);
        }

        tries = 0;
        while(slagCount < minSlag && tries++ < 20){
            slagCount += stampHazardPatch(ctx, 1);
        }

        tries = 0;
        while(promethiumCount < minPromethium && tries++ < 24){
            promethiumCount += stampHazardPatch(ctx, 2);
        }
    }

    /** Paints one irregular hazard patch by type and returns how many tiles were written. */
    private int stampHazardPatch(GenContext ctx, int type){
        if(type < 0 || type > 2) return 0;

        int cx = ctx.rand.random(12, ctx.width() - 13);
        int cy = ctx.rand.random(12, ctx.height() - 13);
        if(isNearCriticalRoom(ctx, cx, cy, 30f, 16f)) return 0;

        int radius;
        if(type == 0){
            radius = ctx.rand.random(10, 15);
        }else if(type == 1){
            radius = ctx.rand.random(5, 8);
        }else{
            radius = ctx.rand.random(9, 14);
        }
        int r2 = radius * radius;
        int placed = 0;

        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null || tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;

                float edge = Mathf.dst(ox, oy) / Math.max(radius, 1f);
                float edgeChance;
                if(type == 1){
                    edgeChance = Mathf.lerp(0.78f, 0.24f, edge);
                }else if(type == 0){
                    edgeChance = Mathf.lerp(0.93f, 0.40f, edge);
                }else{
                    edgeChance = Mathf.lerp(0.95f, 0.42f, edge);
                }
                if(!ctx.rand.chance(edgeChance)) continue;

                if(type == 0){
                    if(!isTarCandidate(tile.floor())) continue;
                    tile.setFloor(Blocks.tar.asFloor());
                }else if(type == 1){
                    if(!isSlagCandidate(tile.floor())) continue;
                    tile.setFloor(Blocks.slag.asFloor());
                }else{
                    if(!isPromethiumCandidate(tile.floor())) continue;
                    tile.setFloor(WHBlocksEnvironment.promethium.asFloor());
                }

                tile.setOverlay(Blocks.air);
                placed++;
            }
        }

        return placed;
    }

    /** Densifies tar/promethium basins so they read as coherent masses instead of noisy blobs. */
    private void compactTarAndPromethiumBasins(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int i = 0; i < iterations; i++){
            byte[] marks = new byte[width * height];

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                if(isNearCriticalRoom(ctx, tile.x, tile.y, 28f, 16f)) continue;

                int tar = 0, prom = 0;
                for(int ox = -2; ox <= 2; ox++){
                    for(int oy = -2; oy <= 2; oy++){
                        if(ox == 0 && oy == 0) continue;
                        Tile near = ctx.tiles.get(tile.x + ox, tile.y + oy);
                        if(near == null) continue;
                        if(near.floor() == Blocks.tar) tar++;
                        if(near.floor() == WHBlocksEnvironment.promethium) prom++;
                    }
                }

                int idx = tile.x + tile.y * width;
                if(isPromethiumCandidate(tile.floor()) && prom >= 6){
                    marks[idx] = 2;
                }else if(isTarCandidate(tile.floor()) && tar >= 8 && !nearFloor(ctx, tile.x, tile.y, Blocks.slag, 1)){
                    marks[idx] = 1;
                }
            }

            for(Tile tile : ctx.tiles){
                int mark = marks[tile.x + tile.y * width];
                if(mark == 2){
                    tile.setFloor(WHBlocksEnvironment.promethium.asFloor());
                    tile.setOverlay(Blocks.air);
                }else if(mark == 1){
                    tile.setFloor(Blocks.tar.asFloor());
                    tile.setOverlay(Blocks.air);
                }
            }
        }
    }

    /** Removes tiny isolated hazard leftovers and restores a suitable nearby dry floor. */
    private void cleanupIsolatedHazardTiles(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            if(floor != Blocks.tar && floor != Blocks.slag && floor != WHBlocksEnvironment.promethium) continue;

            int same = 0;
            for(Point2 p : Geometry.d8){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near == null) continue;
                if(near.floor() == floor) same++;
            }

            int keepThreshold;
            if(floor == Blocks.tar){
                keepThreshold = 3;
            }else if(floor == WHBlocksEnvironment.promethium){
                keepThreshold = 2;
            }else{
                keepThreshold = 1;
            }
            if(same > keepThreshold) continue;

            if(floor == Blocks.slag){
                tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
            }else if(floor == Blocks.tar){
                tile.setFloor((nearFloor(ctx, tile.x, tile.y, Blocks.darksand, 1) || nearFloor(ctx, tile.x, tile.y, Blocks.shale, 1)
                ? Blocks.darksand
                : WHBlocksEnvironment.mineralSandstone).asFloor());
            }else if(floor == WHBlocksEnvironment.promethium){
                tile.setFloor(WHBlocksEnvironment.promethiumSand.asFloor());
            }

            tile.setOverlay(Blocks.air);
        }
    }

    private void softenPromethiumCoreEdges(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(tile.floor() != WHBlocksEnvironment.promethium) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 22f, 12f)) continue;

            int promNear = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.promethium);
            if(promNear >= 7) continue;

            float shape = noise(ctx, ctx.seed + 473, tile.x + 79f, tile.y - 41f, 2, 0.62f, 18f);
            if((shape < -0.20f || promNear <= 1) && ctx.rand.chance(0.52f)){
                float roll = ctx.rand.random(1f);
                if(roll < 0.54f){
                    tile.setFloor(WHBlocksEnvironment.promethiumSand.asFloor());
                }else if(roll < 0.86f){
                    tile.setFloor(WHBlocksEnvironment.mineralSand.asFloor());
                }else{
                    tile.setFloor(WHBlocksEnvironment.mineralSandstone.asFloor());
                }
                tile.setOverlay(Blocks.air);
            }
        }
    }

    /** Softens promethium borders by tinting adjacent terrain to promethium-compatible floors. */
    private void reinforcePromethiumEdgeTransitions(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            if(floor != WHBlocksEnvironment.promethium) continue;

            for(Point2 p : Geometry.d8){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near == null || near.block() != Blocks.air) continue;

                if(near.floor().isLiquid){
                    if(near.floor() != Blocks.slag && near.floor() != WHBlocksEnvironment.promethium){
                        near.setFloor(WHBlocksEnvironment.mineralSandstone.asFloor());
                    }
                    continue;
                }

                if(!near.floor().hasSurface()) continue;
                if(isNearCriticalRoom(ctx, near.x, near.y, 24f, 12f)) continue;

                boolean cardinal = Math.abs(p.x) + Math.abs(p.y) == 1;
                int promNear = countNearFloor(ctx, near.x, near.y, WHBlocksEnvironment.promethium);
                float chance = cardinal ? 0.34f : 0.18f;
                if(promNear >= 2) chance += 0.12f;
                if(!ctx.rand.chance(chance)) continue;

                if(canPromethiumTint(near.floor())){
                    float roll = ctx.rand.random(1f);
                    if(roll < 0.55f){
                        near.setFloor(WHBlocksEnvironment.promethiumSand.asFloor());
                    }else if(roll < 0.84f){
                        near.setFloor(WHBlocksEnvironment.mineralSand.asFloor());
                    }else{
                        near.setFloor(WHBlocksEnvironment.mineralSandstone.asFloor());
                    }
                }
            }
        }
    }

    /**
     * Ensures promethium cores keep a readable surrounding belt of promethium sand.
     * This runs late so later geothermal/scorched passes do not eat the safety halo.
     */
    private void ensurePromethiumSandHalo(GenContext ctx, int radius){
        if(radius <= 0) return;

        int width = ctx.width();
        int height = ctx.height();
        int r2 = radius * radius;
        boolean[] marked = new boolean[width * height];

        for(Tile tile : ctx.tiles){
            if(tile.floor() != WHBlocksEnvironment.promethium) continue;

            for(int ox = -radius; ox <= radius; ox++){
                for(int oy = -radius; oy <= radius; oy++){
                    if(ox * ox + oy * oy > r2) continue;

                    int x = tile.x + ox, y = tile.y + oy;
                    if(x < 0 || y < 0 || x >= width || y >= height) continue;
                    marked[x + y * width] = true;
                }
            }
        }

        for(Tile tile : ctx.tiles){
            int idx = tile.x + tile.y * width;
            if(!marked[idx]) continue;
            if(tile.block() != Blocks.air) continue;
            if(tile.floor() == WHBlocksEnvironment.promethium) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 24f, 12f)) continue;

            tile.setFloor(WHBlocksEnvironment.promethiumSand.asFloor());
            tile.setOverlay(Blocks.air);
        }
    }

    private void limitPromethiumSandCoverage(GenContext ctx){
        int area = ctx.width() * ctx.height();
        int maxPromSand = Math.max(520, area / 280);

        int count = countTilesWithFloor(ctx, WHBlocksEnvironment.promethiumSand);
        if(count <= maxPromSand) return;

        for(int pass = 0; pass < 3 && count > maxPromSand; pass++){
            for(Tile tile : ctx.tiles){
                if(count <= maxPromSand) break;
                if(tile.block() != Blocks.air) continue;
                if(tile.floor() != WHBlocksEnvironment.promethiumSand) continue;
                if(isNearCriticalRoom(ctx, tile.x, tile.y, 20f, 12f)) continue;

                int nearProm = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.promethium);
                int nearPromSand = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.promethiumSand);
                if(nearProm > 1) continue;

                float keepMask = noise(ctx, ctx.seed + 497 + pass * 9, tile.x + 130f, tile.y - 80f, 2, 0.64f, 170f);
                if(nearPromSand >= (pass == 0 ? 5 : 6) && keepMask > 0.08f) continue;

                if(nearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.mineralSand, 2)){
                    tile.setFloor(WHBlocksEnvironment.mineralSand.asFloor());
                }else{
                    tile.setFloor(WHBlocksEnvironment.mineralSandstone.asFloor());
                }
                count--;
            }
        }
    }

    /** Collapses broad geothermal zones back to dark-rock base, preserving heat only near slag. */
    private void settleGeothermalDarkBase(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 20f, 12f)) continue;

            Block floor = tile.floor();
            if(!isGeothermalFloor(floor)) continue;

            if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 1)){
                if(floor != WHBlocksEnvironment.darkHotRock){
                    tile.setFloor(WHBlocksEnvironment.darkHotRock.asFloor());
                }
                continue;
            }

            if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 2)){
                if(floor == WHBlocksEnvironment.darkMagmaRock
                || floor == Blocks.magmarock
                || floor == WHBlocksEnvironment.darkHotRock
                || floor == Blocks.hotrock
                || floor == WHBlocksEnvironment.scorchedEarth
                || floor == WHBlocksEnvironment.scorchedStone){
                    tile.setFloor(WHBlocksEnvironment.darkHotRock.asFloor());
                }else{
                    tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
                }
                continue;
            }

            tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
        }
    }

    /** Builds large dark-rock masses around slag fields so slag zones read as coherent basins. */
    private void expandDarkRockNearSlag(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] writes = new Block[width * height];

            for(Tile source : ctx.tiles){
                if(source.floor() != Blocks.slag || source.block() != Blocks.air) continue;
                if(isNearCriticalRoom(ctx, source.x, source.y, 20f, 12f)) continue;
                if(!ctx.rand.chance(it == 0 ? 0.06f : 0.04f)) continue;

                int radius = ctx.rand.random(6, 11);
                int r2 = radius * radius;

                for(int ox = -radius; ox <= radius; ox++){
                    for(int oy = -radius; oy <= radius; oy++){
                        int rx = source.x + ox, ry = source.y + oy;
                        Tile tile = ctx.tiles.get(rx, ry);
                        if(tile == null || tile.block() != Blocks.air) continue;
                        if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                        if(isNearCriticalRoom(ctx, rx, ry, 20f, 12f)) continue;
                        if(isDarkRockBasinBlocked(tile.floor())) continue;
                        if(!nearFloor(ctx, rx, ry, Blocks.slag, radius + 1)) continue;

                        float warp = noise(ctx, ctx.seed + 933 + it * 7, rx + source.x * 0.35f, ry - source.y * 0.31f, 2, 0.68f, 9f);
                        if(ox * ox + oy * oy > r2 - warp * radius * 1.35f) continue;

                        float edge = Mathf.dst(ox, oy) / Math.max(radius, 1f);
                        float chance = Mathf.lerp(0.88f, 0.34f, edge);
                        if(!ctx.rand.chance(chance)) continue;

                        writes[rx + ry * width] = WHBlocksEnvironment.darkRock;
                    }
                }
            }

            for(Tile tile : ctx.tiles){
                if(tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                if(tile.floor() == Blocks.slag) continue;
                if(isNearCriticalRoom(ctx, tile.x, tile.y, 20f, 12f)) continue;
                if(isDarkRockBasinBlocked(tile.floor())) continue;
                if(nearFloor(ctx, tile.x, tile.y, Blocks.slag, 1)) continue;
                if(!nearFloor(ctx, tile.x, tile.y, Blocks.slag, 6)) continue;

                int darkNear = countNearFloor(ctx, tile.x, tile.y, WHBlocksEnvironment.darkRock);
                if(darkNear >= 5 && ctx.rand.chance(0.64f)){
                    writes[tile.x + tile.y * width] = WHBlocksEnvironment.darkRock;
                }
            }

            for(Tile tile : ctx.tiles){
                Block write = writes[tile.x + tile.y * width];
                if(write != null && tile.floor() != write){
                    tile.setFloor(write.asFloor());
                }
            }
        }
    }

    /** Adds sparse heated stone only around slag to keep the heat signal local and predictable. */
    private void scatterHeatNearSlag(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();

        byte[] marks = new byte[width * height];

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(tile.floor() != WHBlocksEnvironment.darkRock) continue;
            if(!nearFloor(ctx, tile.x, tile.y, Blocks.slag, 2)) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 20f, 12f)) continue;

            boolean cardinalNearSlag = false;
            for(Point2 p : Geometry.d4){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near != null && near.floor() == Blocks.slag){
                    cardinalNearSlag = true;
                    break;
                }
            }

            float noise = Simplex.noise2d(ctx.seed + 913, 2, 0.62f, 1f / 21f, tile.x + 11.3f, tile.y - 19.6f);
            int geothermalNear = countNearGeothermal(ctx, tile.x, tile.y);
            float chance = cardinalNearSlag ? 0.44f : 0.16f;
            if(geothermalNear <= 1) chance *= 0.72f;

            int idx = tile.x + tile.y * width;
            if(noise > 0.10f && ctx.rand.chance(chance)){
                marks[idx] = 2;
            }
        }

        for(Tile tile : ctx.tiles){
            int idx = tile.x + tile.y * width;
            byte mark = marks[idx];
            if(mark == 0) continue;

            if(mark == 2){
                tile.setFloor(WHBlocksEnvironment.darkHotRock.asFloor());
            }
        }

        // Remove isolated hot singletons; keep only coherent hot edges near slag.
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(tile.floor() != WHBlocksEnvironment.darkHotRock) continue;

            boolean cardinalNearSlag = false;
            int hotNear = 0;
            for(Point2 p : Geometry.d4){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near == null || near.block() != Blocks.air) continue;
                if(near.floor() == Blocks.slag){
                    cardinalNearSlag = true;
                }
                if(near.floor() == WHBlocksEnvironment.darkHotRock){
                    hotNear++;
                }
            }

            if(!cardinalNearSlag && hotNear <= 0){
                tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
            }
        }

        // Guarantee at least one hot neighbor for each slag cluster.
        for(Tile tile : ctx.tiles){
            if(tile.floor() != Blocks.slag || tile.block() != Blocks.air) continue;

            int hotAdj = 0;
            Tile first = null;
            for(Point2 p : Geometry.d4){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near == null || near.block() != Blocks.air) continue;
                if(isGeothermalFloor(near.floor())){
                    hotAdj++;
                }else if(near.floor() == WHBlocksEnvironment.darkRock){
                    if(first == null) first = near;
                }
            }

            if(hotAdj < 1 && first != null && !isNearCriticalRoom(ctx, first.x, first.y, 20f, 12f) && ctx.rand.chance(0.72f)){
                first.setFloor(WHBlocksEnvironment.darkHotRock.asFloor());
            }
        }
    }

    /**
     * Adds sparse non-singleton hot-rock clusters in dark basins (not hugging slag), so dark zones keep rare heat accents.
     */
    private void seedIndependentHotRockClusters(GenContext ctx, int iterations){
        int area = ctx.width() * ctx.height();
        int seedsPerIter = Math.max(2, area / 70000);

        for(int it = 0; it < iterations; it++){
            for(int s = 0; s < seedsPerIter; s++){
                int cx = ctx.rand.random(6, ctx.width() - 7);
                int cy = ctx.rand.random(6, ctx.height() - 7);
                Tile center = ctx.tiles.get(cx, cy);
                if(center == null || center.block() != Blocks.air) continue;
                if(center.floor() != WHBlocksEnvironment.darkRock) continue;
                if(isNearCriticalRoom(ctx, cx, cy, 24f, 14f)) continue;
                if(nearFloor(ctx, cx, cy, Blocks.slag, 4)) continue;
                if(nearFloor(ctx, cx, cy, Blocks.tar, 3) || nearFloor(ctx, cx, cy, WHBlocksEnvironment.promethium, 3)) continue;
                if(nearFloor(ctx, cx, cy, WHBlocksEnvironment.radiationWater, 2) || nearFloor(ctx, cx, cy, WHBlocksEnvironment.radiationWaterDeep, 2)
                || nearFloor(ctx, cx, cy, WHBlocksEnvironment.mineralSandRadiationWater, 2) || nearFloor(ctx, cx, cy, WHBlocksEnvironment.radiationSandWater, 2)){
                    continue;
                }

                float field = noise(ctx, ctx.seed + 981 + it * 11, cx + 17.4f, cy - 23.1f, 2, 0.62f, 23f);
                if(field < 0.52f) continue;

                int radius = ctx.rand.random(1, 2);
                int r2 = radius * radius;
                int placed = 0;
                for(int ox = -radius; ox <= radius; ox++){
                    for(int oy = -radius; oy <= radius; oy++){
                        if(ox * ox + oy * oy > r2) continue;
                        Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                        if(tile == null || tile.block() != Blocks.air) continue;
                        if(tile.floor() != WHBlocksEnvironment.darkRock) continue;
                        if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;

                        float edge = Mathf.dst(ox, oy) / Math.max(radius, 1f);
                        float chance = Mathf.lerp(0.82f, 0.34f, edge);
                        if(!ctx.rand.chance(chance)) continue;

                        tile.setFloor(WHBlocksEnvironment.darkHotRock.asFloor());
                        placed++;
                    }
                }

                // Enforce non-singleton cluster.
                if(placed <= 1){
                    for(Point2 p : Geometry.d4){
                        Tile near = ctx.tiles.get(cx + p.x, cy + p.y);
                        if(near == null || near.block() != Blocks.air) continue;
                        if(near.floor() != WHBlocksEnvironment.darkRock) continue;
                        near.setFloor(WHBlocksEnvironment.darkHotRock.asFloor());
                        placed++;
                        if(placed >= 2) break;
                    }
                }
            }
        }
    }

    /**
     * Removes isolated hot-stone speckles so heat reads as small coherent patches instead of single-cell noise.
     */
    private void pruneIsolatedHeatSpeckles(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;
                    if(isNearCriticalRoom(ctx, x, y, 20f, 12f)) continue;

                    Block floor = floors[x + y * width];
                    if(!isHotCoreFloor(floor)) continue;

                    int nearSlag = countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.slag);
                    int nearHotCore = countNearHotCoreFromSnapshot(floors, width, height, x, y);
                    int nearGeothermal = countNearGeothermalFromSnapshot(floors, width, height, x, y);

                    Block target = null;
                    if(nearSlag == 0){
                        if(nearHotCore <= 1){
                            target = WHBlocksEnvironment.darkRock;
                        }else if(floor == WHBlocksEnvironment.darkMagmaRock || floor == Blocks.magmarock){
                            target = WHBlocksEnvironment.darkHotRock;
                        }
                    }else if(nearSlag <= 1 && nearHotCore == 0){
                        target = WHBlocksEnvironment.darkRock;
                    }else if(nearSlag <= 1 && nearHotCore <= 1 && nearGeothermal <= 2){
                        target = WHBlocksEnvironment.darkHotRock;
                    }else if(floor == WHBlocksEnvironment.darkMagmaRock && nearSlag <= 1){
                        target = WHBlocksEnvironment.darkHotRock;
                    }

                    if(target != null && target != floor){
                        writes[x + y * width] = target;
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    private void consolidateScorchedFields(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;
                    if(isNearCriticalRoom(ctx, x, y, 20f, 12f)) continue;

                    Block floor = floors[x + y * width];
                    if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) continue;
                    if(floor == Blocks.slag || floor == Blocks.tar || floor == WHBlocksEnvironment.promethium) continue;
                    if(isRadiationWater(floor)) continue;

                    int nearEarth = countNearFloorFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.scorchedEarth);
                    int nearStone = countNearFloorFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.scorchedStone);
                    int nearSlag = countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.slag);
                    int nearHot = countNearHotFromSnapshot(floors, width, height, x, y);

                    if(nearEarth + nearStone >= 5 && (nearSlag >= 1 || nearHot >= 4)){
                        if(floor != WHBlocksEnvironment.scorchedEarth && floor != WHBlocksEnvironment.scorchedStone){
                            writes[x + y * width] = nearStone >= 4 ? WHBlocksEnvironment.scorchedStone : WHBlocksEnvironment.scorchedEarth;
                        }else if(floor == WHBlocksEnvironment.scorchedEarth && nearStone >= 5){
                            writes[x + y * width] = WHBlocksEnvironment.scorchedStone;
                        }else if(floor == WHBlocksEnvironment.scorchedStone && nearEarth >= 5){
                            writes[x + y * width] = WHBlocksEnvironment.scorchedEarth;
                        }
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    /**
     * Expands scorched floors from slag-side geothermal seeds into coherent patches.
     */
    private void shapeCoherentScorchedFields(GenContext ctx, int iterations){
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] floors = snapshotFloors(ctx);
            Block[] writes = new Block[width * height];

            for(int x = 1; x < width - 1; x++){
                for(int y = 1; y < height - 1; y++){
                    if(isNearCriticalRoom(ctx, x, y, 20f, 12f)) continue;

                    Tile tile = ctx.tiles.getn(x, y);
                    if(tile.block() != Blocks.air) continue;

                    Block floor = floors[x + y * width];
                    if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) continue;
                    if(!isScorchedHost(floor)) continue;

                    int nearSlag = countNearFloorFromSnapshot(floors, width, height, x, y, Blocks.slag);
                    int nearEarth = countNearFloorFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.scorchedEarth);
                    int nearStone = countNearFloorFromSnapshot(floors, width, height, x, y, WHBlocksEnvironment.scorchedStone);
                    int nearHot = countNearHotFromSnapshot(floors, width, height, x, y);

                    float macro = noise(ctx, ctx.seed + 941 + it * 17, x + 210f, y - 160f, 2, 0.60f, 40f);
                    float detail = noise(ctx, ctx.seed + 947 + it * 13, x - 70f, y + 330f, 2, 0.64f, 18f);
                    float field = macro * 0.74f + detail * 0.26f;

                    if(floor == WHBlocksEnvironment.darkRock){
                        if((nearSlag >= 2 || nearHot >= 5) && nearEarth + nearStone >= 3 && field > -0.03f){
                            writes[x + y * width] = WHBlocksEnvironment.scorchedEarth;
                        }
                    }else if(floor == WHBlocksEnvironment.darkHotRock
                    || floor == WHBlocksEnvironment.darkMagmaRock
                    || floor == Blocks.hotrock
                    || floor == Blocks.magmarock){
                        if((nearSlag >= 1 || nearHot >= 4) && nearEarth + nearStone >= 2 && field > -0.10f){
                            writes[x + y * width] = WHBlocksEnvironment.scorchedEarth;
                        }
                    }else if(floor == WHBlocksEnvironment.scorchedEarth){
                        if(nearEarth + nearStone <= 1 && nearSlag == 0 && nearHot <= 2){
                            writes[x + y * width] = WHBlocksEnvironment.darkRock;
                        }else if(nearEarth >= 4 && nearHot >= 5 && field > 0.12f){
                            writes[x + y * width] = WHBlocksEnvironment.scorchedStone;
                        }
                    }else if(floor == WHBlocksEnvironment.scorchedStone){
                        if(nearEarth + nearStone <= 1 && nearSlag == 0){
                            writes[x + y * width] = WHBlocksEnvironment.darkRock;
                        }else if(nearStone <= 1 && nearEarth >= 1){
                            writes[x + y * width] = WHBlocksEnvironment.scorchedEarth;
                        }
                    }
                }
            }

            applyFloorWrites(ctx, writes, width);
        }
    }

    private void applyFloorWrites(GenContext ctx, Block[] writes, int width){
        for(Tile tile : ctx.tiles){
            Block write = writes[tile.x + tile.y * width];
            if(write != null && tile.floor() != write){
                tile.setFloor(write.asFloor());
            }
        }
    }

    private Block[] snapshotFloors(GenContext ctx){
        int width = ctx.width();
        int height = ctx.height();
        Block[] floors = new Block[width * height];
        for(Tile tile : ctx.tiles){
            floors[tile.x + tile.y * width] = tile.floor();
        }
        return floors;
    }

    private int countNearFloorFromSnapshot(Block[] floors, int width, int height, int x, int y, Block floor){
        int count = 0;
        for(Point2 p : Geometry.d8){
            int nx = x + p.x, ny = y + p.y;
            if(nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
            if(floors[nx + ny * width] == floor){
                count++;
            }
        }
        return count;
    }

    private int countNearHotFromSnapshot(Block[] floors, int width, int height, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            int nx = x + p.x, ny = y + p.y;
            if(nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
            if(isGeothermalFloor(floors[nx + ny * width]) || floors[nx + ny * width] == Blocks.slag){
                count++;
            }
        }
        return count;
    }

    private int countNearHotCoreFromSnapshot(Block[] floors, int width, int height, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            int nx = x + p.x, ny = y + p.y;
            if(nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
            if(isHotCoreFloor(floors[nx + ny * width])){
                count++;
            }
        }
        return count;
    }

    private int countNearGeothermalFromSnapshot(Block[] floors, int width, int height, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            int nx = x + p.x, ny = y + p.y;
            if(nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
            if(isGeothermalFloor(floors[nx + ny * width])){
                count++;
            }
        }
        return count;
    }

    private int countNearGeothermal(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isGeothermalFloor(near.floor())){
                count++;
            }
        }
        return count;
    }

    private boolean isHotCoreFloor(Block floor){
        return floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock;
    }

    private boolean isScorchedHost(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock;
    }

    /** @return Whether this floor can be converted to tar during hazard passes. */
    private boolean isTarCandidate(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.carbonStone
        || floor == Blocks.shale
        || floor == Blocks.stone
        || floor == Blocks.darksand;
    }

    /** @return Whether this floor can be converted to slag during hazard passes. */
    private boolean isSlagCandidate(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock
        || floor == Blocks.dacite
        || floor == Blocks.craters;
    }

    /** @return Whether this floor can be converted to promethium core tiles. */
    private boolean isPromethiumCandidate(Block floor){
        return floor == WHBlocksEnvironment.promethiumSand
        || floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor;
    }

    /** @return Whether this floor can host radiation-water hazards. */
    private boolean isRadiationCandidate(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == WHBlocksEnvironment.darkRock;
    }

    /** @return Whether this floor may be heat-tinted in the slag heat-ring stage. */
    private boolean canHeatTint(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.trachyte
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock
        || floor == Blocks.dacite
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite;
    }

    /** @return Floors that should not be overwritten by slag-basin dark-rock expansion. */
    private boolean isDarkRockBasinBlocked(Block floor){
        return floor == Blocks.slag
        || floor == Blocks.tar
        || floor == WHBlocksEnvironment.promethium
        || isRadiationWater(floor);
    }

    /** @return Whether this floor may be tinted around promethium borders. */
    private boolean canPromethiumTint(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == Blocks.darksand
        || floor == Blocks.shale;
    }

    /** @return Whether the floor belongs to the geothermal palette. */
    private boolean isGeothermalFloor(Block floor){
        return floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock;
    }

    /** @return True when the floor is any radiation-water variant. */
    private boolean isRadiationWater(Block floor){
        return floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationWaterDeep
        || floor == WHBlocksEnvironment.radiationSandWater
        || floor == WHBlocksEnvironment.radiationWater;
    }

    /** Counts exact floor occurrences for minimum-coverage enforcement. */
    private int countTilesWithFloor(GenContext ctx, Block block){
        int count = 0;
        for(Tile tile : ctx.tiles){
            if(tile.floor() == block) count++;
        }
        return count;
    }

    /** Counts 8-neighbor floor matches around a tile. */
    private int countNearFloor(GenContext ctx, int x, int y, Block floor){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.floor() == floor){
                count++;
            }
        }
        return count;
    }

    /** Counts all radiation-water tiles for minimum-coverage checks. */
    private int countRadiationWaterTiles(GenContext ctx){
        int count = 0;
        for(Tile tile : ctx.tiles){
            if(isRadiationWater(tile.floor())) count++;
        }
        return count;
    }

    /** Radius-based floor proximity test used by most hazard shaping stages. */
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

    /** @return True when a solid/surface tile exists nearby (used as a cheap enclosure hint). */
    private boolean nearSolid(GenContext ctx, int x, int y, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.floor().hasSurface() && !near.floor().isLiquid){
                    return true;
                }
            }
        }
        return false;
    }

    /** Keeps hazard generation away from spawn/enemy room protected gameplay zones. */
    private boolean isNearCriticalRoom(GenContext ctx, int x, int y, float spawnDst, float enemyDst){
        if(ctx.spawnRoom != null && Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + spawnDst)) return true;
        for(RoomAnchor enemy : ctx.enemyRooms){
            if(Mathf.within(x, y, enemy.x, enemy.y, enemy.radius + enemyDst)) return true;
        }
        return false;
    }

    /** Samples sector-projected simplex noise so 2D map generation follows planet-space continuity. */
    private float noise(GenContext ctx, int seed, float x, float y, double octaves, double falloff, double scl){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z);
    }

}

