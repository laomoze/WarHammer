package wh.pipelinePlanet.karvex;

import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec3;
import arc.struct.IntSeq;
import arc.struct.IntSet;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.noise.Simplex;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;
import wh.content.WHBlocksEnvironment;
import wh.pipelinePlanet.core.GenContext;
import wh.pipelinePlanet.core.GenPass;

/**
 * Erekir-inspired coherent ore fields (chunky deposits, not speckles).
 */
public class KarvexOreBalancePass implements GenPass{
    private static class OreSpec{
        final Block ore;
        final int minTotal;
        final int maxTotal;
        final int clusterMin;
        final int clusterMax;
        final int sizeMin;
        final int sizeMax;

        OreSpec(Block ore, int minTotal, int maxTotal, int clusterMin, int clusterMax, int sizeMin, int sizeMax){
            this.ore = ore;
            this.minTotal = minTotal;
            this.maxTotal = maxTotal;
            this.clusterMin = clusterMin;
            this.clusterMax = clusterMax;
            this.sizeMin = sizeMin;
            this.sizeMax = sizeMax;
        }
    }

    @Override
    public String name(){
        return "KarvexOreBalancePass";
    }

    @Override
    public void apply(GenContext ctx){
        Seq<OreSpec> specs = buildSpecs(ctx);
        if(specs.isEmpty()) return;

        clearExistingOres(ctx);

        for(int i = 0; i < specs.size; i++){
            generateForSpec(ctx, specs.get(i));
        }

        enforceMinimums(ctx, specs);
        resolveAdjacentOreConflicts(ctx);
        cleanupSparseFragments(ctx);
        fillOreHoles(ctx);
        clampMaximums(ctx, specs);
        paintOreFloorAuras(ctx);
        reinforceMetalThemeNearOres(ctx);
        seedEditorStyleMetalPatches(ctx);
        blendChromiteTransitions(ctx, 2);
        softenChromiteBorders(ctx, 1);
        limitMetalFloorCoverage(ctx, 0.12f);
        sanitizeInvalidOverlays(ctx);
    }

    private Seq<OreSpec> buildSpecs(GenContext ctx){
        Seq<OreSpec> out = new Seq<>();
        int area = ctx.width() * ctx.height();
        float threat = Mathf.clamp(ctx.sector.threat);

        Block mn = ore(WHBlocksEnvironment.manganeseOre);
        Block coal = ore(Blocks.oreCoal);
        Block cr = ore(WHBlocksEnvironment.chromiumOre);
        Block co = ore(WHBlocksEnvironment.cobaltOre);
        Block w = ore(Blocks.oreTungsten);
        Block ur = ore(WHBlocksEnvironment.uraniumOre);
        Block mo = ore(WHBlocksEnvironment.molybdenumOre);

        if(mn != Blocks.air){
            out.add(new OreSpec(
            mn,
            Math.max(72, area / 1700),
            Math.max(270, area / 300),
            3,
            6,
            24,
            58
            ));
        }
        if(coal != Blocks.air){
            out.add(new OreSpec(
            coal,
            Math.max(84, area / 1600),
            Math.max(290, area / 280),
            3,
            6,
            28,
            62
            ));
        }
        if(cr != Blocks.air){
            out.add(new OreSpec(
            cr,
            Math.max(66, area / 1900),
            Math.max(235, area / 320),
            2,
            5,
            24,
            52
            ));
        }
        if(co != Blocks.air){
            out.add(new OreSpec(
            co,
            Math.max(58, area / 2100),
            Math.max(210, area / 350),
            2,
            4,
            22,
            46
            ));
        }

        if(w != Blocks.air){
            out.add(new OreSpec(
            w,
            threat > 0.28f ? Math.max(8, area / 7000) : 0,
            Math.max(32, area / 2200),
            1,
            2,
            10,
            24
            ));
        }
        if(ur != Blocks.air){
            out.add(new OreSpec(
            ur,
            threat > 0.34f ? Math.max(6, area / 8000) : 0,
            Math.max(26, area / 2500),
            1,
            2,
            10,
            22
            ));
        }
        if(mo != Blocks.air){
            out.add(new OreSpec(
            mo,
            threat > 0.48f ? Math.max(3, area / 12000) : 0,
            Math.max(12, area / 4200),
            1,
            1,
            6,
            15
            ));
        }

        return out;
    }

    private void paintMetalThemeZones(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 12f, 9f)) continue;
            if(!isMetalThemeHost(tile.floor())) continue;

            float manganeseField = sample(ctx, ctx.seed + 1201, tile.x + 240f, tile.y - 120f, 2, 0.63f, 210f)
            + sample(ctx, ctx.seed + 1203, tile.x - 90f, tile.y + 70f, 2, 0.68f, 86f) * 0.18f;

            float chromiteField = sample(ctx, ctx.seed + 1211, tile.x - 310f, tile.y + 430f, 2, 0.62f, 240f)
            + sample(ctx, ctx.seed + 1213, tile.x + 180f, tile.y - 140f, 2, 0.66f, 98f) * 0.20f;

            float cobaltField = sample(ctx, ctx.seed + 1221, tile.x + 510f, tile.y + 330f, 2, 0.61f, 290f)
            + sample(ctx, ctx.seed + 1227, tile.x - 220f, tile.y + 260f, 2, 0.67f, 112f) * 0.24f;

            Block next = tile.floor();
            if(cobaltField > 0.82f){
                next = cobaltField > 0.93f ? WHBlocksEnvironment.cobaltStone : WHBlocksEnvironment.cobaltFloor;
            }else if(chromiteField > 0.74f){
                if(chromiteField > 0.87f){
                    next = WHBlocksEnvironment.chromiteStone;
                }else if(chromiteField > 0.81f){
                    next = WHBlocksEnvironment.chromiteFloorDark;
                }else{
                    next = WHBlocksEnvironment.chromiteFloor;
                }
            }else if(manganeseField > 0.78f){
                next = manganeseField > 0.90f ? WHBlocksEnvironment.manganeseStone : WHBlocksEnvironment.manganeseFloor;
            }

            if(next != tile.floor() && next.asFloor() != null && next.asFloor().hasSurface() && !next.asFloor().isLiquid){
                tile.setFloor(next.asFloor());
            }
        }
    }

    private void blendMetalThemeZones(GenContext ctx, int iterations){
        int w = ctx.width();

        for(int it = 0; it < iterations; it++){
            short[] next = new short[w * ctx.height()];
            for(Tile tile : ctx.tiles){
                next[tile.x + tile.y * w] = tile.floor().id;
            }

            for(Tile tile : ctx.tiles){
                Block floor = tile.floor();
                if(!isMetalFamily(floor)) continue;
                if(tile.block() != Blocks.air) continue;

                int mn = 0;
                int chf = 0;
                int chd = 0;
                int chs = 0;
                int co = 0;

                for(Point2 p : Geometry.d8){
                    Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                    if(near == null) continue;
                    Block nf = near.floor();
                    if(nf == WHBlocksEnvironment.manganeseFloor || nf == WHBlocksEnvironment.manganeseStone) mn++;
                    else if(nf == WHBlocksEnvironment.chromiteFloor) chf++;
                    else if(nf == WHBlocksEnvironment.chromiteFloorDark) chd++;
                    else if(nf == WHBlocksEnvironment.chromiteStone) chs++;
                    else if(nf == WHBlocksEnvironment.cobaltFloor || nf == WHBlocksEnvironment.cobaltStone) co++;
                }

                Block best = floor;
                int bestScore = -1;

                int scoreMn = mn;
                int scoreChf = chf + chd / 2;
                int scoreChd = chd + chs / 2 + chf / 2;
                int scoreChs = chs + chd;
                int scoreCo = co;

                if(scoreMn > bestScore){
                    best = floor == WHBlocksEnvironment.manganeseStone ? WHBlocksEnvironment.manganeseStone : WHBlocksEnvironment.manganeseFloor;
                    bestScore = scoreMn;
                }
                if(scoreChf > bestScore){
                    best = WHBlocksEnvironment.chromiteFloor;
                    bestScore = scoreChf;
                }
                if(scoreChd > bestScore){
                    best = WHBlocksEnvironment.chromiteFloorDark;
                    bestScore = scoreChd;
                }
                if(scoreChs > bestScore){
                    best = WHBlocksEnvironment.chromiteStone;
                    bestScore = scoreChs;
                }
                if(scoreCo > bestScore){
                    best = floor == WHBlocksEnvironment.cobaltStone ? WHBlocksEnvironment.cobaltStone : WHBlocksEnvironment.cobaltFloor;
                }

                next[tile.x + tile.y * w] = best.id;
            }

            for(Tile tile : ctx.tiles){
                Block floor = Vars.content.block(next[tile.x + tile.y * w]);
                if(floor != null && floor.asFloor() != null){
                    tile.setFloor(floor.asFloor());
                }
            }
        }
    }

    private void generateForSpec(GenContext ctx, OreSpec spec){
        int clusters = ctx.rand.random(spec.clusterMin, spec.clusterMax);
        int placed = 0;

        for(int i = 0; i < clusters && placed < spec.maxTotal; i++){
            int target = ctx.rand.random(spec.sizeMin, spec.sizeMax);
            target = Math.min(target, Math.max(0, spec.maxTotal - placed));
            if(target <= 0) break;
            placed += placeCluster(ctx, spec.ore, target);
        }
    }

    private void enforceMinimums(GenContext ctx, Seq<OreSpec> specs){
        for(int i = 0; i < specs.size; i++){
            OreSpec spec = specs.get(i);
            if(spec.minTotal <= 0) continue;

            int current = countOre(ctx, spec.ore);
            int guard = 0;
            while(current < spec.minTotal && guard++ < 14){
                int need = Math.min(spec.sizeMax, spec.minTotal - current + ctx.rand.random(spec.sizeMin / 2, spec.sizeMax / 2));
                int placed = placeCluster(ctx, spec.ore, Math.max(spec.sizeMin, need));
                if(placed <= 0) break;
                current += placed;
            }

            // hard fallback for mandatory basic ores
            if(current < spec.minTotal && isMandatoryOre(spec.ore)){
                current += forceStampOre(ctx, spec.ore, spec.minTotal - current);
            }
        }
    }

    private int placeCluster(GenContext ctx, Block ore, int target){
        Tile seed = pickClusterSeed(ctx, ore);
        if(seed == null || target <= 0) return 0;

        int sx = seed.x;
        int sy = seed.y;

        IntSeq frontier = new IntSeq();
        IntSet visited = new IntSet();
        IntSeq placed = new IntSeq();

        int startPos = Point2.pack(sx, sy);
        frontier.add(startPos);
        visited.add(startPos);

        int guard = 0;
        int guardMax = Math.max(240, target * 48);

        while(!frontier.isEmpty() && placed.size < target && guard++ < guardMax){
            int fidx = ctx.rand.random(frontier.size - 1);
            int pos = frontier.removeIndex(fidx);
            int x = Point2.x(pos);
            int y = Point2.y(pos);

            Tile tile = ctx.tiles.get(x, y);
            if(tile != null && canPlaceOre(ctx, tile, ore)){
                float edge = Mathf.dst(x, y, sx, sy) / Math.max(1f, Mathf.sqrt(target) * 1.05f);
                if(setOreTile(ctx, tile, ore, Mathf.clamp(edge))){
                    placed.add(pos);
                }
            }

            for(Point2 p : Geometry.d8){
                int nx = x + p.x;
                int ny = y + p.y;
                if(nx < 1 || ny < 1 || nx >= ctx.width() - 1 || ny >= ctx.height() - 1) continue;

                int npos = Point2.pack(nx, ny);
                if(visited.contains(npos)) continue;
                visited.add(npos);

                Tile near = ctx.tiles.get(nx, ny);
                if(!canExpandCandidate(ctx, near, ore)) continue;

                float dist = Mathf.dst(nx, ny, sx, sy) / Math.max(1f, Mathf.sqrt(target) * 1.18f);
                float noise = sample(ctx, ctx.seed + ore.id * 11 + 701, nx, ny, 2, 0.64f, 22f) * 0.14f;
                float chance = 0.90f - dist * 0.58f + noise;
                if(ctx.rand.chance(Mathf.clamp(chance, 0.04f, 0.96f))){
                    frontier.add(npos);
                }
            }

            if(frontier.isEmpty() && placed.size < target * 0.6f){
                Tile reseed = findNearbyCandidate(ctx, sx, sy, ore, Mathf.ceil(Mathf.sqrt(target) * 1.4f));
                if(reseed != null){
                    int rp = Point2.pack(reseed.x, reseed.y);
                    if(!visited.contains(rp)){
                        visited.add(rp);
                        frontier.add(rp);
                    }
                }
            }
        }

        if(!placed.isEmpty()){
            paintClusterHalo(ctx, ore, placed, sx, sy, target);
        }

        return placed.size;
    }

    private Tile pickClusterSeed(GenContext ctx, Block ore){
        int spawnX = ctx.spawnRoom != null ? ctx.spawnRoom.x : ctx.width() / 2;
        int spawnY = ctx.spawnRoom != null ? ctx.spawnRoom.y : ctx.height() / 2;

        for(int i = 0; i < 420; i++){
            int x = ctx.rand.random(2, ctx.width() - 3);
            int y = ctx.rand.random(2, ctx.height() - 3);
            if(Mathf.within(x, y, spawnX, spawnY, 13f)) continue;
            if(isCoreMetalOre(ore) && nearAnyCoreMetalOre(ctx, x, y, 14)) continue;

            Tile tile = ctx.tiles.get(x, y);
            if(!canExpandCandidate(ctx, tile, ore)) continue;
            return tile;
        }

        return null;
    }

    private Tile findNearbyCandidate(GenContext ctx, int cx, int cy, Block ore, int radius){
        for(int r = 1; r <= radius; r++){
            for(int ox = -r; ox <= r; ox++){
                for(int oy = -r; oy <= r; oy++){
                    Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                    if(canExpandCandidate(ctx, tile, ore)){
                        return tile;
                    }
                }
            }
        }
        return null;
    }

    private void paintClusterHalo(GenContext ctx, Block ore, IntSeq cluster, int cx, int cy, int target){
        int radius = auraRadiusForOre(ore);

        for(int i = 0; i < cluster.size; i++){
            int pos = cluster.get(i);
            int x = Point2.x(pos);
            int y = Point2.y(pos);

            for(int ox = -radius; ox <= radius; ox++){
                for(int oy = -radius; oy <= radius; oy++){
                    if(ox * ox + oy * oy > radius * radius) continue;

                    Tile near = ctx.tiles.get(x + ox, y + oy);
                    if(near == null) continue;
                    if(near.block() != Blocks.air) continue;
                    if(!near.floor().hasSurface() || near.floor().isLiquid) continue;
                    if(isNearRoom(ctx, near.x, near.y, 11f, 8f)) continue;
                    if(near.overlay() != Blocks.air && near.overlay() != ore) continue;

                    float edge = Mathf.dst(ox, oy) / Math.max(1f, radius);
                    Block themed = themedFloorForOre(ore, near.floor(), Math.max(0.5f, edge));
                    if(themed == null || themed == near.floor() || themed.asFloor() == null) continue;
                    if(!themed.asFloor().hasSurface() || themed.asFloor().isLiquid) continue;

                    float chance = edge < 0.42f ? 0.78f : edge < 0.75f ? 0.46f : 0.22f;
                    if(near.overlay() == ore) chance += 0.12f;
                    if(ctx.rand.chance(Mathf.clamp(chance))){
                        near.setFloor(themed.asFloor());
                    }
                }
            }
        }

        // reinforce center floor identity for larger ore fields
        int centerRadius = Math.max(2, Mathf.round(Mathf.sqrt(target) * 0.35f));
        for(int ox = -centerRadius; ox <= centerRadius; ox++){
            for(int oy = -centerRadius; oy <= centerRadius; oy++){
                if(ox * ox + oy * oy > centerRadius * centerRadius) continue;
                Tile near = ctx.tiles.get(cx + ox, cy + oy);
                if(near == null) continue;
                if(near.block() != Blocks.air) continue;
                if(!near.floor().hasSurface() || near.floor().isLiquid) continue;
                if(near.overlay() != ore && near.overlay() != Blocks.air) continue;

                Block themed = themedFloorForOre(ore, near.floor(), 0.2f);
                if(themed != null && themed.asFloor() != null && themed.asFloor().hasSurface() && !themed.asFloor().isLiquid){
                    near.setFloor(themed.asFloor());
                }
            }
        }
    }

    private int forceStampOre(GenContext ctx, Block ore, int missing){
        int placed = 0;

        for(int i = 0; i < 220 && placed < missing; i++){
            Tile seed = pickClusterSeed(ctx, ore);
            if(seed == null) break;

            int radius = ctx.rand.random(2, 4);
            for(int ox = -radius; ox <= radius && placed < missing; ox++){
                for(int oy = -radius; oy <= radius && placed < missing; oy++){
                    if(ox * ox + oy * oy > radius * radius) continue;
                    Tile tile = ctx.tiles.get(seed.x + ox, seed.y + oy);
                    if(tile == null) continue;
                    if(tile.block() != Blocks.air) continue;
                    if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                    if(tile.overlay() != Blocks.air) continue;
                    if(isNearRoom(ctx, tile.x, tile.y, 11f, 8f)) continue;

                    float edge = Mathf.dst(ox, oy) / Math.max(1f, radius);
                    if(setOreTile(ctx, tile, ore, edge)){
                        placed++;
                    }
                }
            }
        }

        return placed;
    }

    private void resolveAdjacentOreConflicts(GenContext ctx){
        IntSeq toClear = new IntSeq();

        for(Tile tile : ctx.tiles){
            Block ore = tile.overlay();
            if(ore == Blocks.air) continue;

            ObjectIntMap<Block> counts = new ObjectIntMap<>();
            int same = 0;
            int other = 0;

            for(Point2 p : Geometry.d8){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near == null) continue;
                Block no = near.overlay();
                if(no == Blocks.air) continue;
                counts.increment(no, 0, 1);
                if(no == ore) same++;
                else other++;
            }

            if(other <= 0) continue;

            Block dominant = ore;
            int dominantCount = same;
            for(ObjectIntMap.Entry<Block> entry : counts.entries()){
                if(entry.key == null || entry.key == Blocks.air) continue;
                if(entry.value > dominantCount){
                    dominant = entry.key;
                    dominantCount = entry.value;
                }
            }

            if(dominant != ore || same <= 1){
                toClear.add(tile.pos());
            }
        }

        for(int i = 0; i < toClear.size; i++){
            Tile tile = ctx.tiles.getp(toClear.get(i));
            if(tile != null){
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void cleanupSparseFragments(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block ore = tile.overlay();
            if(ore == Blocks.air) continue;

            int near = oreNeighborCount(ctx, tile.x, tile.y, ore);
            if(near <= 1){
                if(ctx.rand.chance(0.72f)){
                    tile.setOverlay(Blocks.air);
                }
            }else if(near == 2){
                if(ctx.rand.chance(0.18f)){
                    tile.setOverlay(Blocks.air);
                }
            }
        }
    }

    private void fillOreHoles(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.overlay() != Blocks.air) continue;
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 11f, 8f)) continue;

            ObjectIntMap<Block> counts = new ObjectIntMap<>();
            Block best = Blocks.air;
            int bestCount = 0;

            for(Point2 p : Geometry.d8){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near == null) continue;
                Block ore = near.overlay();
                if(ore == Blocks.air) continue;
                int value = counts.increment(ore, 0, 1);
                if(value > bestCount){
                    bestCount = value;
                    best = ore;
                }
            }

            if(best == Blocks.air || bestCount < 5) continue;
            if(!canPlaceOre(ctx, tile, best)) continue;

            if(ctx.rand.chance(0.45f)){
                setOreTile(ctx, tile, best, 0.78f);
            }
        }
    }

    private void clampMaximums(GenContext ctx, Seq<OreSpec> specs){
        for(int i = 0; i < specs.size; i++){
            OreSpec spec = specs.get(i);
            int count = countOre(ctx, spec.ore);
            if(count <= spec.maxTotal) continue;

            int remove = count - spec.maxTotal;
            for(Tile tile : ctx.tiles){
                if(remove <= 0) break;
                if(tile.overlay() != spec.ore) continue;

                int near = oreNeighborCount(ctx, tile.x, tile.y, spec.ore);
                if(near <= 1 || (near <= 2 && ctx.rand.chance(0.55f)) || (near <= 3 && ctx.rand.chance(0.18f))){
                    tile.setOverlay(Blocks.air);
                    remove--;
                }
            }
        }
    }

    private void paintOreFloorAuras(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block ore = tile.overlay();
            if(ore == Blocks.air) continue;

            int radius = ore == Blocks.oreCoal ? 3 : isCoreMetalOre(ore) ? 4 : auraRadiusForOre(ore);
            for(int ox = -radius; ox <= radius; ox++){
                for(int oy = -radius; oy <= radius; oy++){
                    if(ox * ox + oy * oy > radius * radius) continue;

                    Tile near = ctx.tiles.get(tile.x + ox, tile.y + oy);
                    if(near == null) continue;
                    if(near.block() != Blocks.air) continue;
                    if(!near.floor().hasSurface() || near.floor().isLiquid) continue;
                    if(isNearRoom(ctx, near.x, near.y, 11f, 8f)) continue;
                    if(near.overlay() != Blocks.air && near.overlay() != ore) continue;

                    float edge = Mathf.dst(ox, oy) / Math.max(1f, radius);
                    Block themed = themedFloorForOre(ore, near.floor(), Math.max(edge, 0.55f));
                    if(themed == null || themed == near.floor() || themed.asFloor() == null) continue;
                    if(!themed.asFloor().hasSurface() || themed.asFloor().isLiquid) continue;

                    float chance;
                    if(isCoreMetalOre(ore)){
                        chance = edge < 0.45f ? 0.42f : edge < 0.8f ? 0.22f : 0.09f;
                    }else if(ore == Blocks.oreCoal){
                        chance = edge < 0.45f ? 0.36f : edge < 0.8f ? 0.20f : 0.07f;
                    }else{
                        chance = edge < 0.45f ? 0.42f : edge < 0.8f ? 0.24f : 0.09f;
                    }
                    if(ctx.rand.chance(chance)){
                        near.setFloor(themed.asFloor());
                    }
                }
            }
        }
    }

    private void reinforceMetalThemeNearOres(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block ore = tile.overlay();
            if(!isCoreMetalOre(ore)) continue;

            int radius = 3;
            for(int ox = -radius; ox <= radius; ox++){
                for(int oy = -radius; oy <= radius; oy++){
                    if(ox * ox + oy * oy > radius * radius) continue;

                    Tile near = ctx.tiles.get(tile.x + ox, tile.y + oy);
                    if(near == null) continue;
                    if(near.block() != Blocks.air) continue;
                    if(!near.floor().hasSurface() || near.floor().isLiquid) continue;
                    if(isNearRoom(ctx, near.x, near.y, 11f, 8f)) continue;
                    if(near.overlay() != Blocks.air && near.overlay() != ore) continue;

                    float edge = Mathf.dst(ox, oy) / Math.max(1f, radius);
                    Block themed = themedFloorForOre(ore, near.floor(), Math.max(0.2f, edge));
                    if(themed == null || themed == near.floor() || themed.asFloor() == null) continue;
                    if(!themed.asFloor().hasSurface() || themed.asFloor().isLiquid) continue;

                    float chance = edge < 0.36f ? 0.62f : edge < 0.72f ? 0.34f : 0.14f;
                    if(near.overlay() == ore){
                        chance += 0.10f;
                    }

                    if(ctx.rand.chance(Mathf.clamp(chance, 0f, 0.98f))){
                        near.setFloor(themed.asFloor());
                    }
                }
            }
        }
    }

    private void seedEditorStyleMetalPatches(GenContext ctx){
        int area = ctx.width() * ctx.height();
        IntSeq anchors = new IntSeq();

        int boundaryPatches = Math.max(6, area / 32000);
        int wallPatches = Math.max(3, area / 48000);
        int outerPatches = Math.max(3, area / 62000);

        placeBoundaryMetalPatches(ctx, anchors, boundaryPatches, 24, 6, 10);
        placeMetalPatches(ctx, anchors, wallPatches, true, 24, 5, 8);
        placeMetalPatches(ctx, anchors, outerPatches, false, 28, 5, 8);
    }

    private void placeBoundaryMetalPatches(GenContext ctx, IntSeq anchors, int target, int spacing, int radiusMin, int radiusMax){
        int placed = 0;
        int attempts = target * 300;

        for(int i = 0; i < attempts && placed < target; i++){
            int x = ctx.rand.random(3, ctx.width() - 4);
            int y = ctx.rand.random(3, ctx.height() - 4);

            Tile tile = ctx.tiles.get(x, y);
            if(tile == null) continue;
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearRoom(ctx, x, y, 13f, 10f)) continue;
            if(!isBoundaryHost(tile.floor())) continue;
            if(!hasBiomeContrastNeighbor(ctx, x, y)) continue;
            if(nearAnyAnchor(anchors, x, y, spacing)) continue;
            if(nearMetalFamilyFloor(ctx, x, y, 10)) continue;

            float gate = sample(ctx, ctx.seed + 1321, x + 23f, y - 37f, 2, 0.64f, 44f)
            + sample(ctx, ctx.seed + 1327, x - 71f, y + 19f, 1, 1f, 16f) * 0.20f;
            if(gate < 0.10f) continue;

            Block type = pickPatchMetalType(ctx, x, y, true);
            int radius = ctx.rand.random(radiusMin, radiusMax);
            paintNoisyMetalPatch(ctx, x, y, type, radius, 0.05f);

            anchors.add(Point2.pack(x, y));
            placed++;
        }
    }

    private void placeMetalPatches(GenContext ctx, IntSeq anchors, int target, boolean wallBased, int spacing, int radiusMin, int radiusMax){
        int placed = 0;
        int attempts = target * 260;

        for(int i = 0; i < attempts && placed < target; i++){
            int x = ctx.rand.random(2, ctx.width() - 3);
            int y = ctx.rand.random(2, ctx.height() - 3);

            Tile tile = ctx.tiles.get(x, y);
            if(tile == null) continue;
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearRoom(ctx, x, y, 13f, 10f)) continue;
            if(!isMetalThemeHost(tile.floor())) continue;
            if(nearAnyAnchor(anchors, x, y, spacing)) continue;
            if(nearMetalFamilyFloor(ctx, x, y, 10)) continue;

            int wallDist = distanceToStaticWall(ctx, x, y, 5);
            if(wallBased){
                if(wallDist > 1) continue;
            }else{
                if(wallDist <= 2) continue;
            }

            float gateA = sample(ctx, ctx.seed + (wallBased ? 1331 : 1337), x + 31f, y - 43f, 2, 0.64f, wallBased ? 42f : 58f);
            float gateB = sample(ctx, ctx.seed + (wallBased ? 1343 : 1349), x - 87f, y + 23f, 1, 1f, wallBased ? 18f : 25f);
            float gate = gateA + gateB * 0.24f;
            if(gate < (wallBased ? 0.06f : 0.18f)) continue;

            Block type = pickPatchMetalType(ctx, x, y, wallBased);
            int radius = ctx.rand.random(radiusMin, radiusMax);
            paintNoisyMetalPatch(ctx, x, y, type, radius, wallBased ? 0.08f : 0.16f);

            anchors.add(Point2.pack(x, y));
            placed++;
        }
    }

    private Block pickPatchMetalType(GenContext ctx, int x, int y, boolean wallBased){
        float mn = sample(ctx, ctx.seed + 1357, x + 17f, y - 29f, 2, 0.64f, 120f);
        float ch = sample(ctx, ctx.seed + 1361, x - 23f, y + 37f, 2, 0.63f, 128f) + (wallBased ? 0.10f : 0f);
        float co = sample(ctx, ctx.seed + 1367, x + 41f, y + 19f, 2, 0.62f, 136f) + (wallBased ? -0.04f : 0.04f);

        if(co > ch && co > mn) return WHBlocksEnvironment.cobaltFloor;
        if(ch > mn) return WHBlocksEnvironment.chromiteFloor;
        return WHBlocksEnvironment.manganeseFloor;
    }

    private void paintNoisyMetalPatch(GenContext ctx, int cx, int cy, Block baseFloor, int radius, float thresholdBoost){
        if(baseFloor == null || baseFloor == Blocks.air) return;

        int r2 = radius * radius;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;

                int x = cx + ox;
                int y = cy + oy;
                Tile tile = ctx.tiles.get(x, y);
                if(tile == null) continue;
                if(tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                if(isNearRoom(ctx, x, y, 12f, 9f)) continue;
                if(tile.overlay() != Blocks.air && !isCoreMetalOre(tile.overlay())) continue;
                if(!isBoundaryHost(tile.floor()) && !isMetalFamily(tile.floor())) continue;

                float edge = Mathf.dst(ox, oy) / Math.max(1f, radius);
                float n1 = sample(ctx, ctx.seed + baseFloor.id * 13 + 1379, x + 11f, y - 13f, 2, 0.63f, 11f);
                float n2 = sample(ctx, ctx.seed + baseFloor.id * 17 + 1387, x - 47f, y + 29f, 1, 1f, 30f) * 0.32f;
                float editorRand = sample(ctx, ctx.seed + baseFloor.id * 19 + 1399, x - 7f, y + 5f, 1, 1f, 5.5f);
                float mask = (1f - edge) + n1 * 0.44f + n2 + editorRand * 0.16f;

                if(mask < 0.34f + thresholdBoost) continue;
                if(nearIncompatibleMetal(ctx, x, y, baseFloor, 2)) continue;

                int nearSame = neighborMetalTypeCount(ctx, x, y, baseFloor);
                float chance = 0.42f + (1f - edge) * 0.40f + editorRand * 0.18f + nearSame * 0.03f;
                if(!ctx.rand.chance(Mathf.clamp(chance, 0.04f, 0.97f))) continue;

                Block themed = floorTierForPatch(baseFloor, edge, editorRand, nearSame, ctx.rand);
                if(themed == null || themed.asFloor() == null) continue;
                if(!themed.asFloor().hasSurface() || themed.asFloor().isLiquid) continue;

                tile.setFloor(themed.asFloor());
            }
        }
    }

    private Block floorTierForPatch(Block baseFloor, float edge, float editorRand, int nearSame, Rand rand){
        if(baseFloor == WHBlocksEnvironment.chromiteFloor){
            if((edge < 0.26f && editorRand > -0.20f) || (edge < 0.34f && nearSame >= 4 && rand.chance(0.55f))){
                return WHBlocksEnvironment.chromiteStone;
            }
            if(edge < 0.64f && editorRand > -0.70f) return WHBlocksEnvironment.chromiteFloorDark;
            return WHBlocksEnvironment.chromiteFloor;
        }
        if(baseFloor == WHBlocksEnvironment.cobaltFloor){
            if((edge < 0.34f && editorRand > -0.10f) || (edge < 0.44f && nearSame >= 4 && rand.chance(0.50f))){
                return WHBlocksEnvironment.cobaltStone;
            }
            return WHBlocksEnvironment.cobaltFloor;
        }
        if(baseFloor == WHBlocksEnvironment.manganeseFloor){
            if((edge < 0.32f && editorRand > -0.12f) || (edge < 0.42f && nearSame >= 4 && rand.chance(0.52f))){
                return WHBlocksEnvironment.manganeseStone;
            }
            return WHBlocksEnvironment.manganeseFloor;
        }
        return baseFloor;
    }

    private void softenChromiteBorders(GenContext ctx, int iterations){
        int w = ctx.width();
        int h = ctx.height();

        for(int it = 0; it < iterations; it++){
            short[] next = new short[w * h];
            for(Tile tile : ctx.tiles){
                next[tile.x + tile.y * w] = tile.floor().id;
            }

            for(Tile tile : ctx.tiles){
                Block floor = tile.floor();
                if(tile.block() != Blocks.air) continue;
                if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) continue;

                int chromite = 0;
                int metal = 0;
                for(Point2 p : Geometry.d8){
                    Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                    if(near == null) continue;
                    Block nf = near.floor();
                    if(isChromiteFamily(nf)) chromite++;
                    if(isMetalFamily(nf)) metal++;
                }

                int idx = tile.x + tile.y * w;
                if(floor == WHBlocksEnvironment.chromiteStone){
                    if(chromite <= 2){
                        next[idx] = WHBlocksEnvironment.chromiteFloorDark.id;
                    }
                }else if(floor == WHBlocksEnvironment.chromiteFloorDark){
                    if(chromite <= 1){
                        next[idx] = WHBlocksEnvironment.chromiteFloor.id;
                    }
                }else if(floor == WHBlocksEnvironment.chromiteFloor){
                    if(chromite == 0 && metal <= 2 && ctx.rand.chance(0.55f)){
                        next[idx] = WHBlocksEnvironment.darkMineralSandstone.id;
                    }
                }else if(!isMetalFamily(floor)){
                    if(chromite >= 5 && ctx.rand.chance(0.34f)){
                        next[idx] = WHBlocksEnvironment.chromiteFloor.id;
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
    }

    private void blendChromiteTransitions(GenContext ctx, int iterations){
        int w = ctx.width();

        for(int it = 0; it < iterations; it++){
            short[] next = new short[w * ctx.height()];
            for(Tile tile : ctx.tiles){
                next[tile.x + tile.y * w] = tile.floor().id;
            }

            for(Tile tile : ctx.tiles){
                Block floor = tile.floor();
                if(!isChromiteFamily(floor)) continue;

                int f = 0;
                int d = 0;
                int s = 0;

                for(Point2 p : Geometry.d8){
                    Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                    if(near == null) continue;
                    Block nf = near.floor();
                    if(nf == WHBlocksEnvironment.chromiteFloor) f++;
                    else if(nf == WHBlocksEnvironment.chromiteFloorDark) d++;
                    else if(nf == WHBlocksEnvironment.chromiteStone) s++;
                }

                Block out = floor;
                if(s >= 5){
                    out = WHBlocksEnvironment.chromiteStone;
                }else if(d + s >= 5){
                    out = WHBlocksEnvironment.chromiteFloorDark;
                }else if(f + d >= 4){
                    out = WHBlocksEnvironment.chromiteFloor;
                }

                next[tile.x + tile.y * w] = out.id;
            }

            for(Tile tile : ctx.tiles){
                Block floor = Vars.content.block(next[tile.x + tile.y * w]);
                if(floor != null && floor.asFloor() != null){
                    tile.setFloor(floor.asFloor());
                }
            }
        }
    }

    private void limitMetalFloorCoverage(GenContext ctx, float maxRatio){
        int surface = 0;
        int metal = 0;

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            surface++;
            if(isMetalFamily(tile.floor())) metal++;
        }

        if(surface <= 0) return;
        int target = Math.round(surface * maxRatio);
        if(metal <= target) return;

        for(float threshold = 0.75f; threshold >= -0.25f && metal > target; threshold -= 0.08f){
            for(Tile tile : ctx.tiles){
                if(metal <= target) break;
                if(tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                if(!isMetalFamily(tile.floor())) continue;
                if(isProtectedMetalTile(ctx, tile)) continue;

                float field = sample(ctx, ctx.seed + 1291, tile.x + 190f, tile.y - 230f, 2, 0.62f, 90f);
                if(field < threshold) continue;

                tile.setFloor(WHBlocksEnvironment.defaultMineralFloor().asFloor());
                metal--;
            }
        }
    }

    private boolean isProtectedMetalTile(GenContext ctx, Tile tile){
        if(tile.overlay() != Blocks.air) return true;
        if(isNearRoom(ctx, tile.x, tile.y, 12f, 9f)) return true;
        if(distanceToStaticWall(ctx, tile.x, tile.y, 2) <= 1) return true;

        for(int ox = -5; ox <= 5; ox++){
            for(int oy = -5; oy <= 5; oy++){
                Tile near = ctx.tiles.get(tile.x + ox, tile.y + oy);
                if(near != null && near.overlay() != Blocks.air){
                    return true;
                }
            }
        }

        return false;
    }

    private boolean nearAnyAnchor(IntSeq anchors, int x, int y, int spacing){
        int s2 = spacing * spacing;
        for(int i = 0; i < anchors.size; i++){
            int pos = anchors.get(i);
            int ax = Point2.x(pos);
            int ay = Point2.y(pos);
            int dx = x - ax;
            int dy = y - ay;
            if(dx * dx + dy * dy <= s2){
                return true;
            }
        }
        return false;
    }

    private int neighborMetalTypeCount(GenContext ctx, int x, int y, Block type){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near == null) continue;
            Block nf = near.floor();
            if(type == WHBlocksEnvironment.chromiteFloor){
                if(nf == WHBlocksEnvironment.chromiteFloor || nf == WHBlocksEnvironment.chromiteFloorDark || nf == WHBlocksEnvironment.chromiteStone){
                    count++;
                }
            }else if(type == WHBlocksEnvironment.cobaltFloor){
                if(nf == WHBlocksEnvironment.cobaltFloor || nf == WHBlocksEnvironment.cobaltStone){
                    count++;
                }
            }else if(type == WHBlocksEnvironment.manganeseFloor){
                if(nf == WHBlocksEnvironment.manganeseFloor || nf == WHBlocksEnvironment.manganeseStone){
                    count++;
                }
            }
        }
        return count;
    }

    private boolean nearMetalFamilyFloor(GenContext ctx, int x, int y, int radius){
        int r2 = radius * radius;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near != null && isMetalFamily(near.floor())){
                    return true;
                }
            }
        }
        return false;
    }

    private int distanceToStaticWall(GenContext ctx, int x, int y, int maxRadius){
        for(int r = 1; r <= maxRadius; r++){
            int r2 = r * r;
            for(int ox = -r; ox <= r; ox++){
                for(int oy = -r; oy <= r; oy++){
                    if(ox * ox + oy * oy > r2) continue;
                    Tile near = ctx.tiles.get(x + ox, y + oy);
                    if(near != null && near.block().isStatic()){
                        return r;
                    }
                }
            }
        }
        return maxRadius + 1;
    }

    private boolean nearIncompatibleMetal(GenContext ctx, int x, int y, Block baseFloor, int radius){
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near == null) continue;
                Block floor = near.floor();
                if(!isMetalFamily(floor)) continue;
                if(baseFloor == WHBlocksEnvironment.chromiteFloor && (floor == WHBlocksEnvironment.cobaltFloor || floor == WHBlocksEnvironment.cobaltStone || floor == WHBlocksEnvironment.manganeseFloor || floor == WHBlocksEnvironment.manganeseStone)) return true;
                if(baseFloor == WHBlocksEnvironment.cobaltFloor && (floor == WHBlocksEnvironment.chromiteFloor || floor == WHBlocksEnvironment.chromiteFloorDark || floor == WHBlocksEnvironment.chromiteStone || floor == WHBlocksEnvironment.manganeseFloor || floor == WHBlocksEnvironment.manganeseStone))
                    return true;
                if(baseFloor == WHBlocksEnvironment.manganeseFloor && (floor == WHBlocksEnvironment.chromiteFloor || floor == WHBlocksEnvironment.chromiteFloorDark || floor == WHBlocksEnvironment.chromiteStone || floor == WHBlocksEnvironment.cobaltFloor || floor == WHBlocksEnvironment.cobaltStone))
                    return true;
            }
        }
        return false;
    }

    private boolean hasBiomeContrastNeighbor(GenContext ctx, int x, int y){
        Tile center = ctx.tiles.get(x, y);
        if(center == null) return false;

        Block floor = center.floor();
        boolean centerPlain = isBoundaryHost(floor);
        boolean centerDark = isDarkBiomeFloor(floor);
        if(!centerPlain && !centerDark) return false;

        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near == null) continue;
            Block nf = near.floor();
            if(centerPlain && isDarkBiomeFloor(nf)) return true;
            if(centerDark && isBoundaryHost(nf)) return true;
        }
        return false;
    }

    private boolean isBoundaryHost(Block floor){
        return WHBlocksEnvironment.isMineralCoreFloor(floor)
        || floor == WHBlocksEnvironment.mineralSand
                || floor == WHBlocksEnvironment.darkMineralSandstone
        || floor == WHBlocksEnvironment.gravel
                || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.oreSalt;
    }

    private boolean isDarkBiomeFloor(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.trachyte
                || WHBlocksEnvironment.isOreShaleFloor(floor)
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock;
    }

    private boolean nearAnyCoreMetalOre(GenContext ctx, int x, int y, int radius){
        int r2 = radius * radius;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near == null) continue;
                if(isCoreMetalOre(near.overlay())){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean setOreTile(GenContext ctx, Tile tile, Block ore, float edge){
        if(tile == null || ore == null || ore == Blocks.air) return false;
        if(tile.overlay() != Blocks.air) return false;
        if(tile.block() != Blocks.air) return false;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        if(isNearRoom(ctx, tile.x, tile.y, 11f, 8f)) return false;
        if(hasDifferentOreNeighbor(ctx, tile.x, tile.y, ore)) return false;

        boolean fallbackCoal = ore == Blocks.oreCoal && isCoalFallbackFloor(tile.floor());
        if(!isCompatibleFloor(tile.floor(), ore) && !fallbackCoal) return false;

        Block themed = themedFloorForOre(ore, tile.floor(), edge);
        if(themed != null && themed.asFloor() != null && themed.asFloor().hasSurface() && !themed.asFloor().isLiquid){
            tile.setFloor(themed.asFloor());
        }

        tile.setOverlay(ore);
        return true;
    }

    private boolean canPlaceOre(GenContext ctx, Tile tile, Block ore){
        if(tile == null) return false;
        if(tile.block() != Blocks.air) return false;
        if(tile.overlay() != Blocks.air) return false;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        if(isNearRoom(ctx, tile.x, tile.y, 11f, 8f)) return false;
        if(hasDifferentOreNeighbor(ctx, tile.x, tile.y, ore)) return false;

        return isCompatibleFloor(tile.floor(), ore) || (ore == Blocks.oreCoal && isCoalFallbackFloor(tile.floor()));
    }

    private boolean canExpandCandidate(GenContext ctx, Tile tile, Block ore){
        if(tile == null) return false;
        if(tile.block() != Blocks.air) return false;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        if(tile.overlay() != Blocks.air && tile.overlay() != ore) return false;
        if(isNearRoom(ctx, tile.x, tile.y, 11f, 8f)) return false;
        if(hasDifferentOreNeighbor(ctx, tile.x, tile.y, ore)) return false;

        return isCompatibleFloor(tile.floor(), ore) || (ore == Blocks.oreCoal && isCoalFallbackFloor(tile.floor()));
    }

    private int auraRadiusForOre(Block ore){
        if(ore == Blocks.oreCoal) return 4;
        if(ore == WHBlocksEnvironment.manganeseOre || ore == WHBlocksEnvironment.chromiumOre || ore == WHBlocksEnvironment.cobaltOre) return 5;
        return 4;
    }

    private boolean isCoreMetalOre(Block ore){
        return ore == WHBlocksEnvironment.manganeseOre
        || ore == WHBlocksEnvironment.chromiumOre
        || ore == WHBlocksEnvironment.cobaltOre;
    }

    private boolean isMandatoryOre(Block ore){
        return ore == WHBlocksEnvironment.manganeseOre
        || ore == Blocks.oreCoal
        || ore == WHBlocksEnvironment.chromiumOre
        || ore == WHBlocksEnvironment.cobaltOre;
    }

    private int countOre(GenContext ctx, Block ore){
        int count = 0;
        for(Tile tile : ctx.tiles){
            if(tile.overlay() == ore){
                count++;
            }
        }
        return count;
    }

    private int oreNeighborCount(GenContext ctx, int x, int y, Block ore){
        int near = 0;
        for(Point2 p : Geometry.d8){
            Tile tile = ctx.tiles.get(x + p.x, y + p.y);
            if(tile != null && tile.overlay() == ore){
                near++;
            }
        }
        return near;
    }

    private void clearExistingOres(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block overlay = tile.overlay();
            if(overlay == Blocks.air) continue;
            if(overlay == Blocks.oreCoal
            || overlay == Blocks.oreTungsten
            || overlay == WHBlocksEnvironment.manganeseOre
            || overlay == WHBlocksEnvironment.chromiumOre
            || overlay == WHBlocksEnvironment.cobaltOre
            || overlay == WHBlocksEnvironment.uraniumOre
            || overlay == WHBlocksEnvironment.molybdenumOre
            || overlay == WHBlocksEnvironment.vibraniumOre){
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void sanitizeInvalidOverlays(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block overlay = tile.overlay();
            if(overlay == Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid){
                tile.setOverlay(Blocks.air);
            }
        }
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

    private boolean hasDifferentOreNeighbor(GenContext ctx, int x, int y, Block ore){
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near == null) continue;
            Block no = near.overlay();
            if(no != Blocks.air && no != ore){
                return true;
            }
        }
        return false;
    }

    private boolean isMetalThemeHost(Block floor){
        return WHBlocksEnvironment.isMineralCoreFloor(floor)
        || floor == WHBlocksEnvironment.mineralSand
                || floor == WHBlocksEnvironment.darkMineralSandstone
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.trachyte
                || WHBlocksEnvironment.isOreShaleFloor(floor)
        || floor == WHBlocksEnvironment.manganeseFloor
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteFloor
        || floor == WHBlocksEnvironment.chromiteFloorDark
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltFloor
        || floor == WHBlocksEnvironment.cobaltStone;
    }

    private boolean isMetalFamily(Block floor){
        return floor == WHBlocksEnvironment.manganeseFloor
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteFloor
        || floor == WHBlocksEnvironment.chromiteFloorDark
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltFloor
        || floor == WHBlocksEnvironment.cobaltStone;
    }

    private boolean isChromiteFamily(Block floor){
        return floor == WHBlocksEnvironment.chromiteFloor
        || floor == WHBlocksEnvironment.chromiteFloorDark
        || floor == WHBlocksEnvironment.chromiteStone;
    }

    private boolean isCoalFallbackFloor(Block floor){
        return WHBlocksEnvironment.isMineralCoreFloor(floor)
        || floor == WHBlocksEnvironment.mineralSand
                || floor == WHBlocksEnvironment.darkMineralSandstone
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.trachyte;
    }

    private boolean isCompatibleFloor(Block floor, Block ore){
        if(floor == null || floor.asFloor() == null) return false;
        if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) return false;

        if(ore == Blocks.oreCoal){
            return WHBlocksEnvironment.isOreShaleFloor(floor);
        }

        if(ore == WHBlocksEnvironment.manganeseOre){
            return WHBlocksEnvironment.isMineralCoreFloor(floor)
            || floor == WHBlocksEnvironment.mineralSand
                    || floor == WHBlocksEnvironment.darkMineralSandstone
            || floor == WHBlocksEnvironment.quartzSand
            || floor == WHBlocksEnvironment.gravel
                    || floor == WHBlocksEnvironment.quartzSand
            || floor == WHBlocksEnvironment.cementFloor
            || floor == WHBlocksEnvironment.manganeseFloor
            || floor == WHBlocksEnvironment.manganeseStone
            || floor == WHBlocksEnvironment.darkRock
            || floor == WHBlocksEnvironment.trachyte
                    || WHBlocksEnvironment.isOreShaleFloor(floor);
        }

        if(ore == WHBlocksEnvironment.chromiumOre){
            return floor == WHBlocksEnvironment.manganeseFloor
            || floor == WHBlocksEnvironment.manganeseStone
            || floor == WHBlocksEnvironment.chromiteFloor
            || floor == WHBlocksEnvironment.chromiteFloorDark
            || floor == WHBlocksEnvironment.chromiteStone
            || floor == WHBlocksEnvironment.darkRock
            || floor == WHBlocksEnvironment.trachyte
                    || WHBlocksEnvironment.isOreShaleFloor(floor)
                    || floor == WHBlocksEnvironment.darkMineralSandstone;
        }

        if(ore == WHBlocksEnvironment.cobaltOre){
            return floor == WHBlocksEnvironment.chromiteFloorDark
            || floor == WHBlocksEnvironment.chromiteStone
            || floor == WHBlocksEnvironment.cobaltFloor
            || floor == WHBlocksEnvironment.cobaltStone
            || floor == WHBlocksEnvironment.darkRock
            || floor == WHBlocksEnvironment.trachyte
                    || WHBlocksEnvironment.isOreShaleFloor(floor)
            || floor == WHBlocksEnvironment.scorchedEarth
            || floor == WHBlocksEnvironment.scorchedStone;
        }

        if(ore == Blocks.oreTungsten){
            return floor == WHBlocksEnvironment.scorchedEarth
            || floor == WHBlocksEnvironment.scorchedStone
            || floor == WHBlocksEnvironment.darkHotRock
            || floor == WHBlocksEnvironment.darkMagmaRock
            || floor == Blocks.slag
            || floor == WHBlocksEnvironment.trachyte
            || floor == WHBlocksEnvironment.darkRock
                    || WHBlocksEnvironment.isOreShaleFloor(floor);
        }

        if(ore == WHBlocksEnvironment.uraniumOre){
            return floor == WHBlocksEnvironment.radiationSand
            || floor == WHBlocksEnvironment.radiationRockFloor
            || floor == WHBlocksEnvironment.radiationCraters
            || floor == WHBlocksEnvironment.darkRock
                    || WHBlocksEnvironment.isOreShaleFloor(floor)
            || floor == WHBlocksEnvironment.scorchedStone
            || floor == WHBlocksEnvironment.darkHotRock;
        }

        if(ore == WHBlocksEnvironment.molybdenumOre){
            return floor == WHBlocksEnvironment.scorchedEarth
            || floor == WHBlocksEnvironment.scorchedStone
            || floor == WHBlocksEnvironment.darkHotRock
            || floor == WHBlocksEnvironment.darkMagmaRock
            || floor == Blocks.slag
            || floor == WHBlocksEnvironment.promethiumSand
            || floor == WHBlocksEnvironment.promethium;
        }

        return true;
    }

    private Block themedFloorForOre(Block ore, Block current, float edge){
        if(ore == Blocks.oreCoal){
            return WHBlocksEnvironment.oreShale;
        }
        if(ore == WHBlocksEnvironment.chromiumOre){
            if(edge < 0.24f) return WHBlocksEnvironment.chromiteStone;
            if(edge < 0.56f) return WHBlocksEnvironment.chromiteFloorDark;
            if(edge < 0.86f) return WHBlocksEnvironment.chromiteFloor;
            return WHBlocksEnvironment.chromiteFloor;
        }
        if(ore == WHBlocksEnvironment.manganeseOre){
            if(edge < 0.40f) return WHBlocksEnvironment.manganeseStone;
            return WHBlocksEnvironment.manganeseFloor;
        }
        if(ore == WHBlocksEnvironment.cobaltOre){
            if(edge < 0.38f) return WHBlocksEnvironment.cobaltStone;
            return WHBlocksEnvironment.cobaltFloor;
        }
        if(ore == Blocks.oreTungsten){
            if(edge < 0.40f) return WHBlocksEnvironment.scorchedStone;
            if(edge < 0.72f) return WHBlocksEnvironment.darkHotRock;
            return WHBlocksEnvironment.trachyte;
        }
        if(ore == WHBlocksEnvironment.uraniumOre){
            if(edge < 0.34f) return WHBlocksEnvironment.radiationRockFloor;
            if(edge < 0.68f) return WHBlocksEnvironment.radiationCraters;
            return WHBlocksEnvironment.radiationSand;
        }
        if(ore == WHBlocksEnvironment.molybdenumOre){
            if(edge < 0.42f) return WHBlocksEnvironment.scorchedStone;
            if(edge < 0.75f) return WHBlocksEnvironment.scorchedEarth;
            return WHBlocksEnvironment.darkHotRock;
        }
        return current;
    }

    private Block ore(Block block){
        return block == null ? Blocks.air : block;
    }

    private float sample(GenContext ctx, int seed, float x, float y, double octaves, double falloff, double scl){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z);
    }
}
