package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.noise.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;

import java.util.*;

/**
 * Lake-focused hydrology pass with controlled count and irregular shorelines.
 */
public class KarvexHydrologyPass implements GenPass{
    private static class LakeSpec{
        int x;
        int y;
        float rx;
        float ry;
        float angle;
        boolean radiation;
        int shapeSeed;
        boolean lobe4;
        float lobe2Bias;
        float lobe3Bias;
        float lobe4Bias;
        float roughScale;
        float detailScale;
        float macroScale;
        float jaggedScale;
        float roughWeight;
        float detailWeight;
        float jaggedWeight;
        float warpWeight;
        float cutBase;
        float cutMacro;
        float cutErode;
    }

    @Override
    public String name(){
        return "KarvexHydrologyPass";
    }

    @Override
    public void apply(GenContext ctx){
        clearBaselineHydrologyLiquids(ctx);

        seedLakes(ctx);

        int minComponent = Math.max(36, ctx.width() * ctx.height() / 6800);
        removeTinyLiquidComponents(ctx, minComponent);
        removeEdgeLiquidComponents(ctx, 10, minComponent);
        smoothLiquidContours(ctx, 0);
        deepenLakeCenters(ctx, 0);
        protectCriticalRooms(ctx);
        applyShoreTransitions(ctx);
        expandEffluentSandBelts(ctx);
        enforceRadiationWaterTerrainCoupling(ctx);
        removeTinyLiquidComponents(ctx, minComponent);
    }

    private void clearBaselineHydrologyLiquids(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            if(!isHydrologyLiquid(floor)) continue;

            Block replacement = findNearbyLand(ctx, tile.x, tile.y, 9, isRadiationWater(floor));
            tile.setFloor(replacement.asFloor());
            tile.setOverlay(Blocks.air);
            if(tile.block().solid){
                tile.setBlock(Blocks.air);
            }
        }
    }

    private void seedLakes(GenContext ctx){
        int area = ctx.width() * ctx.height();
        boolean wantRadiation = hasSufficientRadiationTerrain(ctx);

        int desired = ctx.rand.random(1, 2);
        if(area < 52000 && desired > 1 && ctx.rand.chance(0.74f)){
            desired = 1;
        }

        int totalCap = Math.min(Math.max(260, area / 34), 2600);
        int perLakeCap = Math.min(Math.max(130, area / 80), 1300);
        int used = 0;

        Seq<LakeSpec> lakes = new Seq<>();
        boolean placedRadiation = false;

        for(int i = 0; i < desired && used < totalCap; i++){
            boolean preferRadiation = wantRadiation && !placedRadiation && (i == 0 || ctx.rand.chance(0.48f));
            LakeSpec spec = findLakeSpec(ctx, lakes, preferRadiation);
            if(spec == null && preferRadiation){
                spec = findLakeSpec(ctx, lakes, false);
            }
            if(spec == null) continue;

            int cap = Math.min(perLakeCap, totalCap - used);
            int placed = carveLake(ctx, spec, cap);
            if(placed <= 0) continue;

            lakes.add(spec);
            used += placed;
            placedRadiation |= spec.radiation;
        }

        if(wantRadiation && !placedRadiation && used < totalCap){
            LakeSpec forced = findLakeSpec(ctx, lakes, true);
            if(forced != null){
                int cap = Math.min(perLakeCap, totalCap - used);
                int placed = carveLake(ctx, forced, Math.max(cap, 110));
                if(placed > 0){
                    lakes.add(forced);
                    used += placed;
                    placedRadiation = true;
                }
            }
        }

        if(lakes.isEmpty() && ctx.rand.chance(0.40f)){
            LakeSpec fallback = findLakeSpec(ctx, lakes, wantRadiation && ctx.rand.chance(0.50f));
            if(fallback == null){
                fallback = findLakeSpec(ctx, lakes, false);
            }
            if(fallback != null){
                carveLake(ctx, fallback, Math.min(Math.max(95, area / 100), 950));
            }
        }
    }

    private LakeSpec findLakeSpec(GenContext ctx, Seq<LakeSpec> existing, boolean preferRadiation){
        int w = ctx.width();
        int h = ctx.height();
        int minDim = Math.min(w, h);

        int margin = Math.max(16, minDim / 12);
        int radiusMin = Math.max(10, minDim / 18);
        int radiusMax = Math.max(radiusMin + 2, minDim / 10);

        for(int attempt = 0; attempt < 280; attempt++){
            int x = ctx.rand.random(margin, w - margin - 1);
            int y = ctx.rand.random(margin, h - margin - 1);

            if(!isInsidePlayableBounds(ctx, x, y, margin)) continue;
            if(isNearRoom(ctx, x, y, 19f, 11f)) continue;

            Tile tile = ctx.tiles.get(x, y);
            if(tile == null || tile.block().solid) continue;
            if(!isLakeHost(tile.floor())) continue;
            if(nearHeat(ctx, x, y, 5)) continue;

            int radTerrain = radiationTerrainCount(ctx, x, y, 5);
            if(preferRadiation && radTerrain < 18) continue;
            if(!preferRadiation && radTerrain > 30 && ctx.rand.chance(0.68f)) continue;

            float rx = ctx.rand.random(radiusMin, radiusMax) * ctx.rand.random(0.82f, 1.16f);
            float ry = ctx.rand.random(radiusMin, radiusMax) * ctx.rand.random(0.82f, 1.16f);
            float clearance = Math.max(rx, ry) + 19f;

            boolean overlaps = false;
            for(int i = 0; i < existing.size; i++){
                LakeSpec other = existing.get(i);
                float spacing = Math.max(other.rx, other.ry) + clearance;
                if(Mathf.within(x, y, other.x, other.y, spacing)){
                    overlaps = true;
                    break;
                }
            }
            if(overlaps) continue;

            LakeSpec spec = new LakeSpec();
            spec.x = x;
            spec.y = y;
            spec.rx = rx;
            spec.ry = ry;
            spec.angle = ctx.rand.random(360f);
            spec.radiation = preferRadiation || (radTerrain >= 24 && ctx.rand.chance(0.66f));
            spec.shapeSeed = ctx.rand.random(1, Integer.MAX_VALUE - 2);
            spec.lobe4 = ctx.rand.chance(0.56f);
            spec.lobe2Bias = ctx.rand.random(0.02f, 0.16f);
            spec.lobe3Bias = ctx.rand.random(0.06f, 0.22f);
            spec.lobe4Bias = ctx.rand.random(0.10f, 0.28f);
            spec.roughScale = ctx.rand.random(11f, 24f);
            spec.detailScale = ctx.rand.random(22f, 48f);
            spec.macroScale = ctx.rand.random(72f, 130f);
            spec.jaggedScale = ctx.rand.random(4.7f, 9.2f);
            spec.roughWeight = ctx.rand.random(0.32f, 0.56f);
            spec.detailWeight = ctx.rand.random(0.18f, 0.36f);
            spec.jaggedWeight = ctx.rand.random(0.09f, 0.24f);
            spec.warpWeight = ctx.rand.random(0.04f, 0.15f);
            spec.cutBase = ctx.rand.random(0.80f, 0.94f);
            spec.cutMacro = ctx.rand.random(0.11f, 0.23f);
            spec.cutErode = ctx.rand.random(0.03f, 0.11f);
            return spec;
        }

        return null;
    }

    private int carveLake(GenContext ctx, LakeSpec spec, int maxTiles){
        if(maxTiles <= 0) return 0;

        float baseRadius = Math.max(spec.rx, spec.ry);
        float lobe2RadiusX = spec.rx * ctx.rand.random(0.52f, 0.78f);
        float lobe2RadiusY = spec.ry * ctx.rand.random(0.52f, 0.78f);
        float lobe2Angle = spec.angle + ctx.rand.range(38f);
        float lobe2Distance = baseRadius * ctx.rand.random(0.16f, 0.36f);
        float lobe2x = spec.x + Angles.trnsx(lobe2Angle, lobe2Distance);
        float lobe2y = spec.y + Angles.trnsy(lobe2Angle, lobe2Distance);

        float lobe3RadiusX = spec.rx * ctx.rand.random(0.36f, 0.62f);
        float lobe3RadiusY = spec.ry * ctx.rand.random(0.36f, 0.62f);
        float lobe3Angle = spec.angle + 128f + ctx.rand.range(30f);
        float lobe3Distance = baseRadius * ctx.rand.random(0.12f, 0.28f);
        float lobe3x = spec.x + Angles.trnsx(lobe3Angle, lobe3Distance);
        float lobe3y = spec.y + Angles.trnsy(lobe3Angle, lobe3Distance);

        float lobe4RadiusX = spec.rx * ctx.rand.random(0.26f, 0.55f);
        float lobe4RadiusY = spec.ry * ctx.rand.random(0.26f, 0.55f);
        float lobe4Angle = spec.angle + 210f + ctx.rand.range(62f);
        float lobe4Distance = baseRadius * ctx.rand.random(0.20f, 0.46f);
        float lobe4x = spec.x + Angles.trnsx(lobe4Angle, lobe4Distance);
        float lobe4y = spec.y + Angles.trnsy(lobe4Angle, lobe4Distance);

        int bound = Mathf.ceil(baseRadius * 1.42f + 6f);
        float cos = Mathf.cosDeg(spec.angle);
        float sin = Mathf.sinDeg(spec.angle);

        IntSeq candidates = new IntSeq();
        FloatSeq candidateDepths = new FloatSeq();

        for(int ox = -bound; ox <= bound; ox++){
            for(int oy = -bound; oy <= bound; oy++){
                int x = spec.x + ox;
                int y = spec.y + oy;

                if(!isInsidePlayableBounds(ctx, x, y, 8)) continue;
                if(isNearRoom(ctx, x, y, 9f, 7f)) continue;

                Tile tile = ctx.tiles.get(x, y);
                if(tile == null) continue;
                if(!tile.floor().hasSurface() && !tile.floor().isLiquid) continue;
                if(tile.floor() == Blocks.tar || tile.floor() == WHBlocksEnvironment.promethium || isHeatFloor(tile.floor())) continue;

                float ux = cos * ox + sin * oy;
                float uy = -sin * ox + cos * oy;
                float d0 = Mathf.sqrt((ux * ux) / (spec.rx * spec.rx) + (uy * uy) / (spec.ry * spec.ry));

                float dx2 = x - lobe2x;
                float dy2 = y - lobe2y;
                float d2 = Mathf.sqrt((dx2 * dx2) / (lobe2RadiusX * lobe2RadiusX) + (dy2 * dy2) / (lobe2RadiusY * lobe2RadiusY));

                float dx3 = x - lobe3x;
                float dy3 = y - lobe3y;
                float d3 = Mathf.sqrt((dx3 * dx3) / (lobe3RadiusX * lobe3RadiusX) + (dy3 * dy3) / (lobe3RadiusY * lobe3RadiusY));

                float dist = Math.min(d0, Math.min(d2 + spec.lobe2Bias, d3 + spec.lobe3Bias));
                if(spec.lobe4){
                    float dx4 = x - lobe4x;
                    float dy4 = y - lobe4y;
                    float d4 = Mathf.sqrt((dx4 * dx4) / (lobe4RadiusX * lobe4RadiusX) + (dy4 * dy4) / (lobe4RadiusY * lobe4RadiusY));
                    dist = Math.min(dist, d4 + spec.lobe4Bias);
                }

                float warpA = sample(ctx, spec.shapeSeed + 857, x + 11f, y - 9f, 2, 0.66f, spec.roughScale * 0.92f);
                float warpB = sample(ctx, spec.shapeSeed + 863, x - 27f, y + 33f, 1, 1f, spec.detailScale * 0.6f);
                float distWarped = dist + warpA * spec.warpWeight + warpB * (spec.warpWeight * 0.7f);

                int baseSeed = spec.shapeSeed + (spec.radiation ? 1000 : 0);
                float rough = sample(ctx, baseSeed + 821, x + spec.x * 0.31f, y - spec.y * 0.27f, 2, 0.67f, spec.roughScale);
                float detail = sample(ctx, baseSeed + 829, x - spec.x * 0.15f, y + spec.y * 0.19f, 2, 0.63f, spec.detailScale);
                float macro = sample(ctx, baseSeed + 839, x, y, 1, 1f, spec.macroScale);
                float erosion = sample(ctx, baseSeed + 853, x + 9f, y - 13f, 1, 1f, spec.roughScale * 0.58f);

                float jagged = sample(ctx, baseSeed + 847, x + 13f, y - 17f, 1, 1f, spec.jaggedScale);
                float warped = distWarped + rough * spec.roughWeight + detail * spec.detailWeight + jagged * spec.jaggedWeight + Math.abs(erosion) * 0.06f + Math.abs(macro) * 0.05f;
                float cut = spec.cutBase + macro * spec.cutMacro + erosion * spec.cutErode;
                if(warped > cut) continue;

                float depth = cut - warped;
                Block liquid = chooseLakeLiquid(spec.radiation, depth);
                if(liquid == null || liquid.asFloor() == null) continue;
                candidates.add(tile.pos());
                candidateDepths.add(depth);
            }
        }

        if(candidates.size < 65){
            return 0;
        }

        int placed = 0;

        if(candidates.size <= maxTiles){
            for(int i = 0; i < candidates.size; i++){
                placed += applyLakeCandidate(ctx, candidates.get(i), spec.radiation, candidateDepths.get(i));
            }
            return placed;
        }

        // Select by depth rather than scan order to avoid directional clipping seams.
        float[] sortedDepths = candidateDepths.toArray();
        Arrays.sort(sortedDepths);
        float threshold = sortedDepths[Math.max(0, sortedDepths.length - maxTiles)];

        int left = maxTiles;
        for(int i = 0; i < candidates.size && left > 0; i++){
            float depth = candidateDepths.get(i);
            if(depth <= threshold) continue;
            placed += applyLakeCandidate(ctx, candidates.get(i), spec.radiation, depth);
            left--;
        }

        for(int i = 0; i < candidates.size && left > 0; i++){
            float depth = candidateDepths.get(i);
            if(depth != threshold) continue;
            placed += applyLakeCandidate(ctx, candidates.get(i), spec.radiation, depth);
            left--;
        }

        return placed;
    }

    private int applyLakeCandidate(GenContext ctx, int pos, boolean radiation, float depth){
        Tile tile = ctx.tiles.getp(pos);
        if(tile == null) return 0;

        Block liquid = chooseLakeLiquid(radiation, depth);
        if(liquid == null || liquid.asFloor() == null) return 0;

        if(tile.block().solid){
            tile.setBlock(Blocks.air);
        }

        if(tile.floor() == liquid) return 0;
        tile.setFloor(liquid.asFloor());
        tile.setOverlay(Blocks.air);
        return 1;
    }

    private Block chooseLakeLiquid(boolean radiation, float depth){
        if(radiation){
            if(depth > 0.28f) return WHBlocksEnvironment.radiationWaterDeep;
            if(depth > 0.13f) return WHBlocksEnvironment.radiationWater;
            return WHBlocksEnvironment.radiationSandWater;
        }else{
            if(depth > 0.28f) return WHBlocksEnvironment.effluentDeep;
            if(depth > 0.13f) return WHBlocksEnvironment.effluent;
            return WHBlocksEnvironment.mineralSandEffluentWater;
        }
    }

    private void removeTinyLiquidComponents(GenContext ctx, int minSize){
        int w = ctx.width();
        int h = ctx.height();
        boolean[] visited = new boolean[w * h];
        IntSeq queue = new IntSeq();
        IntSeq component = new IntSeq();

        for(Tile tile : ctx.tiles){
            int start = tile.x + tile.y * w;
            if(visited[start]) continue;
            if(!isPollutedWater(tile.floor())) continue;

            queue.clear();
            component.clear();
            visited[start] = true;
            queue.add(start);

            int radTiles = 0;

            while(!queue.isEmpty()){
                int idx = queue.pop();
                component.add(idx);
                int x = idx % w;
                int y = idx / w;

                Tile cur = ctx.tiles.get(x, y);
                if(cur != null && isRadiationWater(cur.floor())) radTiles++;

                for(Point2 p : Geometry.d8){
                    int nx = x + p.x;
                    int ny = y + p.y;
                    if(nx < 0 || ny < 0 || nx >= w || ny >= h) continue;

                    int nidx = nx + ny * w;
                    if(visited[nidx]) continue;

                    Tile near = ctx.tiles.get(nx, ny);
                    if(near == null || !isPollutedWater(near.floor())) continue;

                    visited[nidx] = true;
                    queue.add(nidx);
                }
            }

            if(component.size >= minSize) continue;

            boolean preferRad = radTiles > component.size / 2;
            for(int i = 0; i < component.size; i++){
                int pos = component.get(i);
                int x = pos % w;
                int y = pos / w;
                Tile t = ctx.tiles.get(x, y);
                if(t != null){
                    t.setFloor(findNearbyLand(ctx, x, y, 9, preferRad).asFloor());
                }
            }
        }
    }

    private void removeEdgeLiquidComponents(GenContext ctx, int margin, int minKeepSize){
        int w = ctx.width();
        int h = ctx.height();
        boolean[] visited = new boolean[w * h];
        IntSeq queue = new IntSeq();
        IntSeq component = new IntSeq();

        for(Tile tile : ctx.tiles){
            int start = tile.x + tile.y * w;
            if(visited[start]) continue;
            if(!isPollutedWater(tile.floor())) continue;

            queue.clear();
            component.clear();
            visited[start] = true;
            queue.add(start);

            boolean touchesEdge = false;
            boolean outsidePlayable = false;
            int rad = 0;

            while(!queue.isEmpty()){
                int idx = queue.pop();
                component.add(idx);
                int x = idx % w;
                int y = idx / w;

                if(x < margin || y < margin || x >= w - margin || y >= h - margin){
                    touchesEdge = true;
                }
                if(!isInsidePlayableBounds(ctx, x, y, 5)){
                    outsidePlayable = true;
                }

                Tile cur = ctx.tiles.get(x, y);
                if(cur != null && isRadiationWater(cur.floor())) rad++;

                for(Point2 p : Geometry.d8){
                    int nx = x + p.x;
                    int ny = y + p.y;
                    if(nx < 0 || ny < 0 || nx >= w || ny >= h) continue;

                    int nidx = nx + ny * w;
                    if(visited[nidx]) continue;

                    Tile near = ctx.tiles.get(nx, ny);
                    if(near == null || !isPollutedWater(near.floor())) continue;

                    visited[nidx] = true;
                    queue.add(nidx);
                }
            }

            boolean drop = outsidePlayable || (touchesEdge && component.size < Math.max(minKeepSize * 2, 260));
            if(!drop) continue;

            boolean preferRad = rad > component.size / 2;
            for(int i = 0; i < component.size; i++){
                int pos = component.get(i);
                int x = pos % w;
                int y = pos / w;
                Tile t = ctx.tiles.get(x, y);
                if(t != null){
                    t.setFloor(findNearbyLand(ctx, x, y, 10, preferRad).asFloor());
                }
            }
        }
    }

    private void smoothLiquidContours(GenContext ctx, int iterations){
        int w = ctx.width();

        for(int it = 0; it < iterations; it++){
            short[] next = new short[w * ctx.height()];
            for(Tile tile : ctx.tiles){
                next[tile.x + tile.y * w] = tile.floor().id;
            }

            for(Tile tile : ctx.tiles){
                int idx = tile.x + tile.y * w;
                Block floor = tile.floor();

                if(isPollutedWater(floor)){
                    int card = pollutedCardinalNeighbors(ctx, tile.x, tile.y);
                    int ring = pollutedNeighbors(ctx, tile.x, tile.y);
                    if(card <= 1 && ring <= 2 && !isDeepPolluted(floor)){
                        next[idx] = findNearbyLand(ctx, tile.x, tile.y, 8, isRadiationWater(floor)).id;
                    }
                    continue;
                }

                if(tile.block().solid) continue;
                if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) continue;
                if(isHeatFloor(floor)) continue;
                if(isNearRoom(ctx, tile.x, tile.y, 11f, 8f)) continue;

                int radCard = radiationCardinalNeighbors(ctx, tile.x, tile.y);
                int effCard = effluentCardinalNeighbors(ctx, tile.x, tile.y);

                if(radCard >= 3){
                    next[idx] = WHBlocksEnvironment.radiationSandWater.id;
                }else if(effCard >= 3){
                    next[idx] = WHBlocksEnvironment.mineralSandEffluentWater.id;
                }
            }

            for(Tile tile : ctx.tiles){
                Block floor = Vars.content.block(next[tile.x + tile.y * w]);
                if(floor != null && floor.asFloor() != null){
                    tile.setFloor(floor.asFloor());
                }
            }
        }
    }

    private void deepenLakeCenters(GenContext ctx, int deepRadius){
        if(deepRadius <= 0) return;
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            int idx = tile.x + tile.y * w;
            Block floor = tile.floor();
            next[idx] = floor.id;

            if(!isShallowPollutedWater(floor)) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 9f, 7f)) continue;

            boolean surrounded = true;
            for(int ox = -deepRadius; ox <= deepRadius && surrounded; ox++){
                for(int oy = -deepRadius; oy <= deepRadius; oy++){
                    if(ox * ox + oy * oy > deepRadius * deepRadius) continue;
                    Tile near = ctx.tiles.get(tile.x + ox, tile.y + oy);
                    if(near == null || !isPollutedWater(near.floor()) || near.block().solid){
                        surrounded = false;
                        break;
                    }
                }
            }

            if(surrounded){
                next[idx] = deepVersion(floor).id;
            }
        }

        for(Tile tile : ctx.tiles){
            Block floor = Vars.content.block(next[tile.x + tile.y * w]);
            if(floor != null && floor.asFloor() != null){
                tile.setFloor(floor.asFloor());
            }
        }
    }

    private void applyShoreTransitions(GenContext ctx){
        applyShoreLayer(ctx, 1, 1f);
        applyShoreLayer(ctx, 2, 0.58f);
        applyShoreLayer(ctx, 3, 0.24f);
    }

    private void applyShoreLayer(GenContext ctx, int radius, float mul){
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            int idx = tile.x + tile.y * w;
            Block floor = tile.floor();
            next[idx] = floor.id;

            if(tile.block().solid) continue;
            if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) continue;
            if(isHeatFloor(floor) || floor == Blocks.tar) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 11f, 8f)) continue;

            int rad = radiationWaterCount(ctx, tile.x, tile.y, radius);
            int eff = effluentWaterCount(ctx, tile.x, tile.y, radius);
            if(rad <= 0 && eff <= 0) continue;

            if(rad >= eff){
                if(radius == 1){
                    next[idx] = rad >= 6 ? WHBlocksEnvironment.radiationRockFloor.id : WHBlocksEnvironment.radiationSand.id;
                }else if(ctx.rand.chance((rad >= 4 ? 0.78f : 0.52f) * mul)){
                    next[idx] = rad >= 7 ? WHBlocksEnvironment.radiationRockFloor.id : WHBlocksEnvironment.radiationSand.id;
                }
            }else{
                if(radius == 1){
                    next[idx] = eff >= 8 ? WHBlocksEnvironment.mineralSandstone.id : WHBlocksEnvironment.mineralSand.id;
                }else if(ctx.rand.chance((eff >= 4 ? 0.88f : 0.56f) * mul)){
                    float salt = sample(ctx, ctx.seed + 917, tile.x + 71f, tile.y - 43f, 1, 1f, 27f);
                    if(eff >= 9 && salt > 0.84f){
                        next[idx] = WHBlocksEnvironment.oreSalt.id;
                    }else{
                        next[idx] = eff >= 7 ? WHBlocksEnvironment.mineralSand.id : WHBlocksEnvironment.mineralSandstone.id;
                    }
                }
            }
        }

        for(Tile tile : ctx.tiles){
            Block floor = Vars.content.block(next[tile.x + tile.y * w]);
            if(floor != null && floor.asFloor() != null){
                tile.setFloor(floor.asFloor());
            }
        }
    }

    private void enforceRadiationWaterTerrainCoupling(GenContext ctx){
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            int idx = tile.x + tile.y * w;
            Block floor = tile.floor();
            next[idx] = floor.id;

            if(!isRadiationWater(floor)) continue;

            int terrain = radiationTerrainCount(ctx, tile.x, tile.y, 2);
            int support = radiationWaterCount(ctx, tile.x, tile.y, 1);
            if(terrain <= 0 && support <= 1){
                next[idx] = effluentEquivalent(floor).id;
            }
        }

        for(Tile tile : ctx.tiles){
            Block floor = Vars.content.block(next[tile.x + tile.y * w]);
            if(floor != null && floor.asFloor() != null){
                tile.setFloor(floor.asFloor());
            }
        }
    }

    private void expandEffluentSandBelts(GenContext ctx){
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            int idx = tile.x + tile.y * w;
            Block floor = tile.floor();
            next[idx] = floor.id;

            if(tile.block().solid) continue;
            if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) continue;
            if(isHeatFloor(floor) || floor == Blocks.tar) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 11f, 8f)) continue;

            int eff = effluentWaterCount(ctx, tile.x, tile.y, 4);
            if(eff <= 1) continue;
            if(radiationWaterCount(ctx, tile.x, tile.y, 2) > eff) continue;

            float field = sample(ctx, ctx.seed + 931, tile.x + 38f, tile.y - 12f, 2, 0.63f, 21f);
            float chance = Mathf.clamp((eff - 1) / 15f) * 0.92f;
            if(radiusDistanceToEffluent(ctx, tile.x, tile.y, 5) <= 2){
                chance += 0.18f;
            }
            if(!ctx.rand.chance(Mathf.clamp(chance + field * 0.12f, 0f, 0.98f))) continue;

            if(eff >= 10 && field > 0.78f){
                next[idx] = WHBlocksEnvironment.oreSalt.id;
            }else if(eff >= 7 && field < -0.34f){
                next[idx] = WHBlocksEnvironment.mineralSandstone.id;
            }else{
                next[idx] = WHBlocksEnvironment.mineralSand.id;
            }
        }

        for(Tile tile : ctx.tiles){
            Block floor = Vars.content.block(next[tile.x + tile.y * w]);
            if(floor != null && floor.asFloor() != null){
                tile.setFloor(floor.asFloor());
            }
        }
    }

    private void protectCriticalRooms(GenContext ctx){
        if(ctx.spawnRoom != null){
            clearRoomLiquid(ctx, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + 6);
        }
        for(int i = 0; i < ctx.enemyRooms.size; i++){
            clearRoomLiquid(ctx, ctx.enemyRooms.get(i).x, ctx.enemyRooms.get(i).y, 9);
        }
    }

    private void clearRoomLiquid(GenContext ctx, int cx, int cy, int radius){
        int r2 = radius * radius;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null) continue;

                if(tile.floor().isLiquid){
                    tile.setFloor(findNearbyLand(ctx, tile.x, tile.y, 10, false).asFloor());
                }
                if(tile.floor().isLiquid && tile.block().solid){
                    tile.setBlock(Blocks.air);
                }
            }
        }
    }

    private boolean isInsidePlayableBounds(GenContext ctx, int x, int y, float margin){
        float cx = ctx.width() / 2f;
        float cy = ctx.height() / 2f;
        float radius = Math.min(ctx.width(), ctx.height()) * 0.5f / Mathf.sqrt3 - margin;
        return Mathf.within(x, y, cx, cy, radius);
    }

    private boolean nearHeat(GenContext ctx, int x, int y, int radius){
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near != null && isHeatFloor(near.floor())){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNearRoom(GenContext ctx, int x, int y, float spawnRadius, float enemyRadius){
        if(ctx.spawnRoom != null && Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + spawnRadius)){
            return true;
        }
        for(int i = 0; i < ctx.enemyRooms.size; i++){
            if(Mathf.within(x, y, ctx.enemyRooms.get(i).x, ctx.enemyRooms.get(i).y, enemyRadius)){
                return true;
            }
        }
        return false;
    }

    private int pollutedNeighbors(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isPollutedWater(near.floor())) count++;
        }
        return count;
    }

    private int pollutedCardinalNeighbors(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d4){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isPollutedWater(near.floor())) count++;
        }
        return count;
    }

    private int radiationCardinalNeighbors(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d4){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isRadiationWater(near.floor())) count++;
        }
        return count;
    }

    private int effluentCardinalNeighbors(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d4){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isEffluent(near.floor())) count++;
        }
        return count;
    }

    private int radiationWaterCount(GenContext ctx, int x, int y, int radius){
        int count = 0;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near != null && isRadiationWater(near.floor())) count++;
            }
        }
        return count;
    }

    private int effluentWaterCount(GenContext ctx, int x, int y, int radius){
        int count = 0;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near != null && isEffluent(near.floor())) count++;
            }
        }
        return count;
    }

    private int radiationTerrainCount(GenContext ctx, int x, int y, int radius){
        int count = 0;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near != null && isRadiationTerrain(near.floor())) count++;
            }
        }
        return count;
    }

    private int radiusDistanceToEffluent(GenContext ctx, int x, int y, int maxRadius){
        for(int r = 1; r <= maxRadius; r++){
            int r2 = r * r;
            for(int ox = -r; ox <= r; ox++){
                for(int oy = -r; oy <= r; oy++){
                    if(ox * ox + oy * oy > r2) continue;
                    Tile near = ctx.tiles.get(x + ox, y + oy);
                    if(near != null && isEffluent(near.floor())){
                        return r;
                    }
                }
            }
        }
        return maxRadius + 1;
    }

    private boolean hasSufficientRadiationTerrain(GenContext ctx){
        int need = Math.max(40, (ctx.width() * ctx.height()) / 1300);
        for(Tile tile : ctx.tiles){
            if(isRadiationTerrain(tile.floor())){
                if(--need <= 0){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLakeHost(Block floor){
        return floor != null
        && floor.asFloor() != null
        && floor.asFloor().hasSurface()
        && !floor.asFloor().isLiquid
        && !isHeatFloor(floor)
        && floor != Blocks.tar
        && floor != WHBlocksEnvironment.promethium;
    }

    private boolean isHydrologyLiquid(Block floor){
        if(floor == null || floor.asFloor() == null || !floor.asFloor().isLiquid) return false;
        if(floor == Blocks.slag || floor == Blocks.tar || floor == WHBlocksEnvironment.promethium) return false;
        if(isPollutedWater(floor)) return true;
        return floor.asFloor().liquidDrop == Liquids.water;
    }

    private boolean isRadiationTerrain(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters;
    }

    private boolean isHeatFloor(Block floor){
        return floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.slag;
    }

    private boolean isShallowPollutedWater(Block floor){
        return floor == WHBlocksEnvironment.mineralSandEffluentWater
        || floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationSandWater
        || floor == WHBlocksEnvironment.effluent
        || floor == WHBlocksEnvironment.radiationWater;
    }

    private boolean isPollutedWater(Block floor){
        return isEffluent(floor) || isRadiationWater(floor);
    }

    private boolean isDeepPolluted(Block floor){
        return floor == WHBlocksEnvironment.effluentDeep
        || floor == WHBlocksEnvironment.radiationWaterDeep;
    }

    private Block deepVersion(Block floor){
        if(floor == WHBlocksEnvironment.mineralSandEffluentWater || floor == WHBlocksEnvironment.effluent){
            return WHBlocksEnvironment.effluentDeep;
        }
        if(floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationWater
        || floor == WHBlocksEnvironment.radiationSandWater){
            return WHBlocksEnvironment.radiationWaterDeep;
        }
        return floor;
    }

    private Block effluentEquivalent(Block radiationFloor){
        if(radiationFloor == WHBlocksEnvironment.radiationWaterDeep) return WHBlocksEnvironment.effluentDeep;
        if(radiationFloor == WHBlocksEnvironment.radiationWater) return WHBlocksEnvironment.effluent;
        return WHBlocksEnvironment.mineralSandEffluentWater;
    }

    private boolean isEffluent(Block floor){
        return floor == WHBlocksEnvironment.mineralSandEffluentWater
        || floor == WHBlocksEnvironment.effluent
        || floor == WHBlocksEnvironment.effluentDeep;
    }

    private boolean isRadiationWater(Block floor){
        return floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationWater
        || floor == WHBlocksEnvironment.radiationWaterDeep
        || floor == WHBlocksEnvironment.radiationSandWater;
    }

    private Block findNearbyLand(GenContext ctx, int x, int y, int radius, boolean preferRadiation){
        Block fallback = preferRadiation ? WHBlocksEnvironment.radiationSand : WHBlocksEnvironment.defaultMineralFloor();

        for(int r = 1; r <= radius; r++){
            for(int ox = -r; ox <= r; ox++){
                for(int oy = -r; oy <= r; oy++){
                    Tile near = ctx.tiles.get(x + ox, y + oy);
                    if(near == null) continue;
                    Block floor = near.floor();
                    if(floor == null || floor.asFloor() == null) continue;
                    if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) continue;

                    if(preferRadiation && isRadiationTerrain(floor)) return floor;
                    fallback = floor;
                }
            }
        }

        return fallback;
    }

    private float sample(GenContext ctx, int seed, float x, float y, double octaves, double falloff, double scl){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z);
    }
}
