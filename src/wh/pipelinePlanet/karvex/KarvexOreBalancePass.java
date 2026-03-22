package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.data.*;

/**
 * 中文说明：Karvex 矿物平衡阶段，控制矿种密度与可达性。
 */
public class KarvexOreBalancePass implements GenPass{
    private static class OreDef{
        final Block ore;
        final int tier;
        final float bias;

        OreDef(Block ore, int tier, float bias){
            this.ore = ore;
            this.tier = tier;
            this.bias = bias;
        }
    }

    @Override
    public String name(){
        return "KarvexOreBalancePass";
    }

    @Override
    public void apply(GenContext ctx){
        Seq<OreDef> ores = buildOreTiers(ctx);
        if(ores.isEmpty()) return;

        clearExistingOverlays(ctx, ores);
        seedOreHostFloors(ctx, ores);
        seedTieredOres(ctx, ores);
        smoothOreBlobs(ctx, ores, 3);
        trimFragments(ctx, ores);

        for(OreDef def : ores){
            ensureMinimum(ctx, def, minimumFor(ctx, def));
        }

        smoothOreBlobs(ctx, ores, 2);
        trimFragments(ctx, ores);
        enforceOreFloorCompatibility(ctx, ores);
        growOreHostFloorsAroundDeposits(ctx, ores, 2);

        for(OreDef def : ores){
            trimToCap(ctx, def.ore, maximumFor(ctx, def));
        }

        sanitizeCriticalRoomHeat(ctx);
        seedCoreStarterOres(ctx);
        enforceOreFloorCompatibility(ctx, ores);
    }

    private Seq<OreDef> buildOreTiers(GenContext ctx){
        Seq<OreDef> ores = new Seq<>();
        float threat = Mathf.clamp(ctx.sector.threat);
        float poles = Math.abs(ctx.sector.tile.v.y);
        float seedNoise = Simplex.noise3d(ctx.seed + 17, 2, 0.56f, 1f / 2.8f,
        ctx.sector.tile.v.x,
        ctx.sector.tile.v.y,
        ctx.sector.tile.v.z);

        Block manganese = ore(WHBlocksEnvironment.manganeseOre);
        Block chromium = ore(WHBlocksEnvironment.chromiumOre);
        Block cobalt = ore(WHBlocksEnvironment.cobaltOre);
        Block uranium = ore(WHBlocksEnvironment.uraniumOre);
        Block molybdenum = ore(WHBlocksEnvironment.molybdenumOre);
        Block vibranium = ore(WHBlocksEnvironment.vibraniumOre);

        if(manganese != Blocks.air) ores.add(new OreDef(manganese, 1, -0.05f));
        if(chromium != Blocks.air) ores.add(new OreDef(chromium, 2, 0f));
        if(cobalt != Blocks.air) ores.add(new OreDef(cobalt, 3, 0f));

        // Vanilla strategic ore, always present.
        ores.add(new OreDef(Blocks.oreTungsten, 4, 0.04f));

        // Strategic mod ores kept available; rarity is handled by thresholds/caps.
        if(uranium != Blocks.air) ores.add(new OreDef(uranium, 5, 0.08f));
        if(molybdenum != Blocks.air) ores.add(new OreDef(molybdenum, 6, 0.09f));
        if(vibranium != Blocks.air && threat > 0.52f) ores.add(new OreDef(vibranium, 7, 0.13f));

        return ores;
    }

    private void clearExistingOverlays(GenContext ctx, Seq<OreDef> ores){
        ObjectSet<Block> oreSet = new ObjectSet<>();
        for(OreDef def : ores){
            oreSet.add(def.ore);
        }

        for(Tile tile : ctx.tiles){
            Block overlay = tile.overlay();
            if(overlay == Blocks.spawn) continue;

            if(overlay.itemDrop != null || oreSet.contains(overlay)){
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void seedTieredOres(GenContext ctx, Seq<OreDef> ores){
        for(int i = ores.size - 1; i >= 0; i--){
            OreDef def = ores.get(i);
            int seeds = oreSeedCount(ctx, def);
            int tries = Math.max(240, seeds * 32);
            int placedSeeds = 0;

            for(int t = 0; t < tries && placedSeeds < seeds; t++){
                int x = ctx.rand.random(3, ctx.width() - 4);
                int y = ctx.rand.random(3, ctx.height() - 4);
                Tile tile = ctx.tiles.getn(x, y);

                if(!canPlaceAtTile(ctx, tile, def.ore)) continue;
                if(nearOverlay(ctx, x, y, def.ore, 4)) continue;

                float value = oreFieldValue(ctx, def, i, x - 4, y + 23);
                boolean onHostFloor = onPreferredHostFloor(ctx, def.ore, tile);
                float threshold = oreFieldThreshold(ctx, def) + (onHostFloor ? -0.025f : 0.025f);
                if(value <= threshold) continue;
                if(!onHostFloor && !ctx.rand.chance(0.12f)) continue;

                int patch = orePatchSize(def, value - threshold);
                int placed = placePatch(ctx, x, y, def.ore, patch);
                if(placed > patch / 3){
                    placedSeeds++;
                }
            }
        }
    }

    private void seedOreHostFloors(GenContext ctx, Seq<OreDef> ores){
        for(int i = ores.size - 1; i >= 0; i--){
            OreDef def = ores.get(i);
            int patches = floorPatchCount(ctx, def);
            int tries = Math.max(300, patches * 34);
            int paintedPatches = 0;

            for(int t = 0; t < tries && paintedPatches < patches; t++){
                int x = ctx.rand.random(4, ctx.width() - 5);
                int y = ctx.rand.random(4, ctx.height() - 5);
                Tile tile = ctx.tiles.getn(x, y);

                if(!canPaintHostFloorAt(ctx, tile, def.ore)) continue;

                float value = oreFieldValue(ctx, def, i, x - 4, y + 23);
                float threshold = oreFieldThreshold(ctx, def) - floorFieldPadding(def) - 0.04f;
                if(value <= threshold) continue;

                int patch = hostFloorPatchSize(def, value - threshold);
                int painted = paintHostFloorPatch(ctx, def, x, y, patch);
                if(painted > patch / 3){
                    paintedPatches++;
                }
            }
        }
    }

    private int paintHostFloorPatch(GenContext ctx, OreDef def, int x, int y, int target){
        if(target <= 0) return 0;

        Tile center = ctx.tiles.get(x, y);
        if(!canPaintHostFloorAt(ctx, center, def.ore)) return 0;

        float angle = patchDirectionAngle(ctx, def.ore, x, y, 811);
        float dirX = Mathf.cos(angle);
        float dirY = Mathf.sin(angle);

        IntSeq frontier = new IntSeq();
        IntSet visited = new IntSet();
        frontier.add(center.pos());
        visited.add(center.pos());

        int painted = 0;
        while(frontier.size > 0 && painted < target){
            int idx = ctx.rand.random(frontier.size - 1);
            int packed = frontier.removeIndex(idx);
            Tile tile = ctx.tiles.getp(packed);
            if(tile == null) continue;
            if(!canPaintHostFloorAt(ctx, tile, def.ore)) continue;

            Block hostFloor = hostFloorForOre(ctx, def.ore, tile.x, tile.y, tile.floor());
            if(hostFloor == null) continue;
            if(tile.floor() != hostFloor){
                tile.setFloor(hostFloor.asFloor());
                painted++;
            }

            for(Point2 p : Geometry.d8){
                if(!acceptPatchStep(ctx, def.ore, tile.x, tile.y, p, dirX, dirY, 0.78f, 0.36f)) continue;

                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(!canPaintHostFloorAt(ctx, near, def.ore)) continue;

                int nearPos = near.pos();
                if(visited.contains(nearPos)) continue;

                visited.add(nearPos);
                frontier.add(nearPos);
            }
        }

        return painted;
    }

    private int oreSeedCount(GenContext ctx, OreDef def){
        int area = ctx.width() * ctx.height();
        if(def.tier <= 2) return Math.max(6, area / 26000);
        if(def.tier <= 4) return Math.max(5, area / 30000);
        if(def.tier <= 6) return Math.max(4, area / 36000);
        return Math.max(3, area / 42000);
    }

    private int orePatchSize(OreDef def, float margin){
        int base = patchSize(def.tier) + 3;
        return base + Mathf.round(Mathf.clamp(margin * 125f, 0f, base));
    }

    private int floorPatchCount(GenContext ctx, OreDef def){
        int area = ctx.width() * ctx.height();
        if(def.tier <= 2) return Math.max(8, area / 18000);
        if(def.tier <= 4) return Math.max(7, area / 22000);
        if(def.tier <= 6) return Math.max(6, area / 26000);
        return Math.max(5, area / 30000);
    }

    private int hostFloorPatchSize(OreDef def, float margin){
        int base = patchSize(def.tier) + 18;
        return base + Mathf.round(Mathf.clamp(margin * 132f, 0f, base * 1.1f));
    }

    private boolean canPaintHostFloorAt(GenContext ctx, Tile tile, Block ore){
        if(!canSeedOreFloorOnTile(ctx, tile)) return false;
        if(tile.overlay() == Blocks.spawn) return false;
        if(isHotOre(ore) && isNearCriticalRoom(ctx, tile.x, tile.y, 30f, 18f)) return false;
        return canRethemeForOre(ore, tile.floor());
    }

    private boolean onPreferredHostFloor(GenContext ctx, Block ore, Tile tile){
        Block host = hostFloorForOre(ctx, ore, tile.x, tile.y, tile.floor());
        return host != null && tile.floor() == host;
    }

    private boolean canSeedOreFloorOnTile(GenContext ctx, Tile tile){
        if(tile == null) return false;
        if(tile.block() != Blocks.air) return false;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        if(nearWall(ctx, tile.x, tile.y)) return false;
        return !isNearCriticalRoom(ctx, tile.x, tile.y, 14f, 10f);
    }

    private float oreFieldValue(GenContext ctx, OreDef def, int index, int x, int y){
        float scale = oreFieldScale(def);
        float warpScale = scale * 0.34f;

        // Domain-warp ore fields to avoid long directional streaks from linear x/y mixing.
        float warpX = Simplex.noise2d(ctx.seed + 304 + index * 17, 2, 0.58f, 1f / (scale * 0.92f),
        x + 67.1f + index * 13f,
        y - 41.3f - index * 11f) * warpScale;
        float warpY = Simplex.noise2d(ctx.seed + 313 + index * 19, 2, 0.58f, 1f / (scale * 0.92f),
        x - 53.7f - index * 7f,
        y + 92.4f + index * 9f) * warpScale;

        float sx = x + warpX;
        float sy = y + warpY;

        // One dominant field + secondary breakup fields, sampled in warped isotropic space.
        float primary = noise(ctx, ctx.seed + 140 + index * 13,
        sx + 150f + index * 37f,
        sy + 100f - index * 41f,
        4, 0.8f, scale);

        float secondary = noise(ctx, ctx.seed + 220 + index * 11,
        sx - 420f - index * 53f,
        sy + 620f + index * 29f,
        3, 0.70f, scale * 0.88f);

        float breakup = noise(ctx, ctx.seed + 268 + index * 7,
        sx + 870f + index * 19f,
        sy - 330f - index * 27f,
        2, 0.63f, scale * 0.76f);

        return primary * 0.62f + secondary * 0.26f + breakup * 0.12f;
    }

    private float oreFieldScale(OreDef def){
        if(def.ore == Blocks.oreTungsten) return 56f;
        if(def.ore == WHBlocksEnvironment.uraniumOre) return 64f;
        if(def.ore == WHBlocksEnvironment.molybdenumOre) return 66f;
        if(def.ore == WHBlocksEnvironment.vibraniumOre) return 70f;

        return 60f + def.tier * 2.5f;
    }

    private float oreFieldThreshold(GenContext ctx, OreDef def){
        float threshold = 0.74f + def.tier * 0.013f + def.bias * 0.36f;
        float threat = Mathf.clamp(ctx.sector.threat);

        if(def.ore == Blocks.oreTungsten){
            threshold -= 0.02f;
        }else if(def.ore == WHBlocksEnvironment.manganeseOre){
            threshold -= 0.026f;
        }else if(def.ore == WHBlocksEnvironment.cobaltOre){
            threshold -= 0.021f;
        }else if(def.ore == WHBlocksEnvironment.uraniumOre || def.ore == WHBlocksEnvironment.molybdenumOre){
            threshold -= threat * 0.03f;
        }else if(def.ore == WHBlocksEnvironment.vibraniumOre){
            threshold += 0.02f;
        }

        return threshold;
    }

    private float floorFieldPadding(OreDef def){
        if(def.ore == WHBlocksEnvironment.vibraniumOre) return 0.05f;
        if(def.tier >= 5) return 0.065f;
        return 0.08f;
    }

    private void trimFragments(GenContext ctx, Seq<OreDef> ores){
        for(OreDef def : ores){
            int minNeighbors = def.tier <= 4 ? 2 : 1;

            for(Tile tile : ctx.tiles){
                if(tile.overlay() != def.ore) continue;
                if(countNearOverlay(ctx, tile.x, tile.y, def.ore) <= minNeighbors){
                    tile.setOverlay(Blocks.air);
                }
            }
        }
    }

    private void ensureMinimum(GenContext ctx, OreDef def, int min){
        if(min <= 0) return;

        int existing = countOre(ctx, def.ore);
        int need = min - existing;
        if(need <= 0) return;

        boolean hasAny = existing > 0;
        int patchSize = patchSize(def.tier);
        int tries = Math.max(need * 16, 900);

        for(int i = 0; i < tries && need > 0; i++){
            int x = ctx.rand.random(3, ctx.width() - 4);
            int y = ctx.rand.random(3, ctx.height() - 4);
            Tile tile = ctx.tiles.getn(x, y);

            if(!canPlaceAtTile(ctx, tile, def.ore)) continue;
            if(hasAny && !nearOverlay(ctx, x, y, def.ore, 2) && !ctx.rand.chance(def.tier >= 5 ? 0.08f : 0.11f)) continue;

            int placed = placePatch(ctx, x, y, def.ore, patchSize);
            if(placed > 0){
                need -= placed;
                hasAny = true;
            }
        }

        if(need <= 0) return;

        for(Tile tile : ctx.tiles){
            if(need <= 0) break;
            if(!canPlaceAtTile(ctx, tile, def.ore)) continue;
            if(hasAny && !nearOverlay(ctx, tile.x, tile.y, def.ore, 2)) continue;

            int placed = placePatch(ctx, tile.x, tile.y, def.ore, Math.max(8, patchSize - 6));
            if(placed > 0){
                need -= placed;
                hasAny = true;
            }
        }
    }

    private int placePatch(GenContext ctx, int x, int y, Block ore, int target){
        if(target <= 0) return 0;

        Tile center = ctx.tiles.get(x, y);
        if(!canPlaceAtTile(ctx, center, ore)) return 0;

        float angle = patchDirectionAngle(ctx, ore, x, y, 937);
        float dirX = Mathf.cos(angle);
        float dirY = Mathf.sin(angle);

        IntSeq frontier = new IntSeq();
        IntSet visited = new IntSet();

        int centerPos = center.pos();
        frontier.add(centerPos);
        visited.add(centerPos);

        int placed = 0;
        while(frontier.size > 0 && placed < target){
            int idx = ctx.rand.random(frontier.size - 1);
            int packed = frontier.removeIndex(idx);
            Tile tile = ctx.tiles.getp(packed);
            if(tile == null) continue;

            if(placeOreAt(ctx, tile, ore)){
                placed++;
            }

            for(Point2 p : Geometry.d8){
                if(!acceptPatchStep(ctx, ore, tile.x, tile.y, p, dirX, dirY, 0.82f, 0.42f)) continue;

                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(!canPlaceAtTile(ctx, near, ore)) continue;

                int nearPos = near.pos();
                if(visited.contains(nearPos)) continue;

                visited.add(nearPos);
                frontier.add(nearPos);
            }
        }

        return placed;
    }

    private float patchDirectionAngle(GenContext ctx, Block ore, int x, int y, int salt){
        float n = Simplex.noise2d(ctx.seed + salt + ore.id * 13, 2, 0.58f, 1f / 23f, x + 19.3f, y - 27.1f);
        return (n * 0.5f + 0.5f) * Mathf.PI2;
    }

    private boolean acceptPatchStep(GenContext ctx, Block ore, int x, int y, Point2 step, float dirX, float dirY, float cardinalBase, float diagonalBase){
        boolean diagonal = Math.abs(step.x) + Math.abs(step.y) == 2;
        float base = diagonal ? diagonalBase : cardinalBase;
        float align = (step.x * dirX + step.y * dirY) * 0.24f;
        float wobble = Simplex.noise2d(ctx.seed + ore.id * 41 + 17, 2, 0.55f, 1f / 15f, x + step.x * 11.7f, y + step.y * 9.4f) * 0.1f;
        float chance = Mathf.clamp(base + align + wobble, 0.10f, 0.95f);
        return ctx.rand.chance(chance);
    }

    private boolean placeOreAt(GenContext ctx, Tile tile, Block ore){
        if(!canPlaceAtTile(ctx, tile, ore)) return false;

        Block hostFloor = hostFloorForOre(ctx, ore, tile.x, tile.y, tile.floor());
        if(hostFloor != null && hostFloor != tile.floor() && canRethemeForOre(ore, tile.floor())){
            tile.setFloor(hostFloor.asFloor());
        }

        tile.setOverlay(ore);
        return true;
    }

    private void growOreHostFloorsAroundDeposits(GenContext ctx, Seq<OreDef> ores, int iterations){
        if(iterations <= 0) return;

        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            Block[] writes = new Block[width * height];

            for(int oi = ores.size - 1; oi >= 0; oi--){
                OreDef def = ores.get(oi);
                Block ore = def.ore;

                for(Tile tile : ctx.tiles){
                    if(tile.block() != Blocks.air) continue;
                    if(tile.overlay() != Blocks.air) continue;
                    if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                    if(isNearCriticalRoom(ctx, tile.x, tile.y, 14f, 10f)) continue;
                    if(!canRethemeForOre(ore, tile.floor())) continue;

                    int near1 = countNearOverlay(ctx, tile.x, tile.y, ore);
                    int near2 = countOverlayRadius(ctx, tile.x, tile.y, ore, 2);
                    if(near1 == 0 && near2 < 3) continue;

                    float chance = near1 >= 3 ? 0.78f : (near1 >= 1 ? 0.56f : 0.38f);
                    if(!ctx.rand.chance(chance)) continue;

                    Block hostFloor = hostFloorForOre(ctx, ore, tile.x, tile.y, tile.floor());
                    if(hostFloor == null || hostFloor == tile.floor()) continue;

                    writes[tile.x + tile.y * width] = hostFloor;
                }
            }

            for(Tile tile : ctx.tiles){
                Block write = writes[tile.x + tile.y * width];
                if(write != null){
                    tile.setFloor(write.asFloor());
                }
            }
        }
    }

    private void smoothOreBlobs(GenContext ctx, Seq<OreDef> ores, int iterations){
        if(iterations <= 0) return;
        int width = ctx.width();
        int height = ctx.height();

        for(int it = 0; it < iterations; it++){
            for(OreDef def : ores){
                Block ore = def.ore;
                boolean[] write = new boolean[width * height];

                for(Tile tile : ctx.tiles){
                    int idx = tile.x + tile.y * width;
                    int near = countNearOverlay(ctx, tile.x, tile.y, ore);

                    if(tile.overlay() == ore){
                        int keepThreshold = def.tier <= 4 ? 2 : 1;
                        write[idx] = near >= keepThreshold;
                    }else{
                        if(tile.overlay() != Blocks.air){
                            write[idx] = false;
                            continue;
                        }
                        int growThreshold = def.tier <= 4 ? 4 : 3;
                        write[idx] = near >= growThreshold && canPlaceAtTile(ctx, tile, ore);
                    }
                }

                for(Tile tile : ctx.tiles){
                    int idx = tile.x + tile.y * width;
                    if(write[idx]){
                        if(tile.overlay() != ore){
                            placeOreAt(ctx, tile, ore);
                        }
                    }else if(tile.overlay() == ore){
                        tile.setOverlay(Blocks.air);
                    }
                }
            }
        }
    }

    private int minimumFor(GenContext ctx, OreDef def){
        int area = ctx.width() * ctx.height();
        float threat = Mathf.clamp(ctx.sector.threat);

        if(def.ore == WHBlocksEnvironment.manganeseOre){
            return Math.max(150, area / 2700);
        }else if(def.ore == WHBlocksEnvironment.chromiumOre){
            return Math.max(96, area / 3800);
        }else if(def.ore == WHBlocksEnvironment.cobaltOre){
            return Math.max(96, Mathf.round(area / 4200f * (0.95f + threat * 0.48f)));
        }else if(def.ore == Blocks.oreTungsten){
            return Math.max(72, area / 6200);
        }else if(def.ore == WHBlocksEnvironment.uraniumOre){
            return Math.max(28, Mathf.round(area / 13000f * (0.62f + threat * 0.86f)));
        }else if(def.ore == WHBlocksEnvironment.molybdenumOre){
            return Math.max(24, Mathf.round(area / 15000f * (0.56f + threat * 0.84f)));
        }else if(def.ore == WHBlocksEnvironment.vibraniumOre){
            return Math.max(14, Mathf.round(area / 22000f * Math.max(0.32f, threat * 0.86f)));
        }

        return 0;
    }

    private int maximumFor(GenContext ctx, OreDef def){
        int area = ctx.width() * ctx.height();
        float threat = Mathf.clamp(ctx.sector.threat);

        if(def.ore == WHBlocksEnvironment.manganeseOre){
            return Math.max(460, area / 760);
        }else if(def.ore == WHBlocksEnvironment.chromiumOre){
            return Math.max(300, area / 1100);
        }else if(def.ore == WHBlocksEnvironment.cobaltOre){
            return Math.max(320, area / 1100);
        }else if(def.ore == Blocks.oreTungsten){
            return Math.max(180, area / 1900);
        }else if(def.ore == WHBlocksEnvironment.uraniumOre){
            return Math.max(84, Mathf.round(area / 4200f * (0.70f + threat * 0.42f)));
        }else if(def.ore == WHBlocksEnvironment.molybdenumOre){
            return Math.max(70, Mathf.round(area / 5200f * (0.68f + threat * 0.40f)));
        }else if(def.ore == WHBlocksEnvironment.vibraniumOre){
            return Math.max(48, Mathf.round(area / 8600f * Math.max(0.30f, threat * 0.60f)));
        }

        return Integer.MAX_VALUE;
    }

    private int patchSize(int tier){
        if(tier <= 1) return 34;
        if(tier == 2) return 30;
        if(tier == 3) return 26;
        if(tier == 4) return 22;
        if(tier == 5) return 18;
        if(tier == 6) return 16;
        return 14;
    }

    private boolean canPlaceAtTile(GenContext ctx, Tile tile, Block ore){
        if(tile == null) return false;
        if(tile.block() != Blocks.air) return false;
        if(tile.overlay() != Blocks.air) return false;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        if(nearWall(ctx, tile.x, tile.y)) return false;
        if(isHotOre(ore) && isNearCriticalRoom(ctx, tile.x, tile.y, 30f, 18f)) return false;
        if(isNearCriticalRoom(ctx, tile.x, tile.y, 14f, 10f)) return false;
        return canPlaceOnFloor(ore, tile.floor());
    }

    private boolean canPlaceOnFloor(Block ore, Block floor){
        if(ore == WHBlocksEnvironment.manganeseOre){
            return floor == WHBlocksEnvironment.manganeseStone
            || floor == WHBlocksEnvironment.darkRock
            || floor == WHBlocksEnvironment.mineralSandstone
            || floor == WHBlocksEnvironment.promethiumSand
            || floor == Blocks.yellowStone
            || floor == Blocks.yellowStonePlates;
        }else if(ore == WHBlocksEnvironment.chromiumOre){
            return floor == WHBlocksEnvironment.chromiteStone
            || floor == WHBlocksEnvironment.manganeseStone
            || floor == WHBlocksEnvironment.darkRock
            || floor == WHBlocksEnvironment.promethiumSand
            || floor == Blocks.ferricStone
            || floor == Blocks.ferricCraters;
        }else if(ore == WHBlocksEnvironment.cobaltOre){
            return floor == WHBlocksEnvironment.cobaltStone
            || floor == WHBlocksEnvironment.chromiteStone
            || floor == WHBlocksEnvironment.darkRock
            || floor == Blocks.carbonStone
            || floor == Blocks.rhyolite
            || floor == Blocks.roughRhyolite;
        }else if(ore == Blocks.oreTungsten){
            return floor == WHBlocksEnvironment.darkRock
            || floor == WHBlocksEnvironment.scorchedEarth
            || floor == WHBlocksEnvironment.scorchedStone
            || floor == WHBlocksEnvironment.darkHotRock
            || floor == WHBlocksEnvironment.darkMagmaRock
            || floor == Blocks.rhyolite
            || floor == Blocks.roughRhyolite
            || floor == Blocks.dacite
            || floor == Blocks.ferricStone
            || floor == Blocks.ferricCraters
            || floor == Blocks.magmarock;
        }else if(ore == WHBlocksEnvironment.uraniumOre){
            return isRadiationFloor(floor)
            || floor == Blocks.redmat
            || floor == Blocks.redStone
            || floor == Blocks.denseRedStone
            || floor == Blocks.redIce
            || floor == WHBlocksEnvironment.darkRock
            || floor == Blocks.stone
            || floor == Blocks.ferricStone
            || floor == Blocks.ferricCraters
            || floor == Blocks.rhyolite
            || floor == Blocks.roughRhyolite;
        }else if(ore == WHBlocksEnvironment.molybdenumOre){
            return floor == WHBlocksEnvironment.darkRock
            || floor == WHBlocksEnvironment.darkHotRock
            || floor == WHBlocksEnvironment.darkMagmaRock
            || floor == WHBlocksEnvironment.scorchedEarth
            || floor == WHBlocksEnvironment.scorchedStone
            || floor == WHBlocksEnvironment.trachyte
            || floor == Blocks.dacite
            || floor == Blocks.ferricStone
            || floor == Blocks.ferricCraters
            || floor == Blocks.rhyolite
            || floor == Blocks.roughRhyolite;
        }else if(ore == WHBlocksEnvironment.vibraniumOre){
            return floor == WHBlocksEnvironment.darkMagmaRock
            || floor == WHBlocksEnvironment.darkHotRock
            || floor == Blocks.magmarock
            || floor == Blocks.crystallineStone
            || floor == Blocks.crystalFloor
            || floor == WHBlocksEnvironment.cobaltStone
            || floor == Blocks.ferricCraters;
        }

        return false;
    }

    private boolean canRethemeForOre(Block ore, Block floor){
        if(ore == WHBlocksEnvironment.manganeseOre) return canRethemeManganese(floor);
        if(ore == WHBlocksEnvironment.chromiumOre) return canRethemeChromium(floor);
        if(ore == WHBlocksEnvironment.cobaltOre) return canRethemeCobalt(floor);
        if(ore == Blocks.oreTungsten) return canRethemeTungsten(floor);
        if(ore == WHBlocksEnvironment.uraniumOre) return canPlaceOnFloor(ore, floor);
        if(ore == WHBlocksEnvironment.molybdenumOre) return canRethemeMoly(floor);
        if(ore == WHBlocksEnvironment.vibraniumOre) return canRethemeVibranium(floor);
        return false;
    }

    private Block hostFloorForOre(GenContext ctx, Block ore, int x, int y, Block currentFloor){
        if(ore == WHBlocksEnvironment.manganeseOre){
            return WHBlocksEnvironment.manganeseStone;
        }else if(ore == WHBlocksEnvironment.chromiumOre){
            return WHBlocksEnvironment.chromiteStone;
        }else if(ore == WHBlocksEnvironment.cobaltOre){
            return WHBlocksEnvironment.cobaltStone;
        }else if(ore == WHBlocksEnvironment.uraniumOre){
            return WHBlocksEnvironment.radiationRockFloor;
        }else if(ore == WHBlocksEnvironment.molybdenumOre){
            return WHBlocksEnvironment.darkRock;
        }else if(ore == WHBlocksEnvironment.vibraniumOre){
            float n = Simplex.noise2d(ctx.seed + 751, 2, 0.60f, 1f / 14f, x + 31.7f, y - 17.9f);
            return n > 0.06f ? WHBlocksEnvironment.darkMagmaRock : WHBlocksEnvironment.cobaltStone;
        }else if(ore == Blocks.oreTungsten){
            return WHBlocksEnvironment.darkRock;
        }

        return currentFloor;
    }

    private boolean canRethemeManganese(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates;
    }

    private boolean canRethemeChromium(Block floor){
        return floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.darkRock
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters
        || floor == Blocks.stone
        || floor == Blocks.craters
        || floor == Blocks.carbonStone
        || floor == WHBlocksEnvironment.promethiumSand;
    }

    private boolean canRethemeCobalt(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == Blocks.carbonStone
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.ferricCraters;
    }

    private boolean canRethemeMoly(Block floor){
        return floor == Blocks.stone
        || floor == Blocks.shale
        || floor == Blocks.craters
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.dacite;
    }

    private boolean canRethemeVibranium(Block floor){
        return floor == WHBlocksEnvironment.cobaltStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.magmarock
        || floor == Blocks.crystallineStone
        || floor == Blocks.crystalFloor
        || floor == Blocks.ferricCraters
        || floor == Blocks.rhyolite;
    }

    private boolean canRethemeTungsten(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.dacite
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters
        || floor == Blocks.magmarock;
    }

    private boolean isRadiationFloor(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters;
    }

    private void sanitizeCriticalRoomHeat(GenContext ctx){
        if(ctx.spawnRoom != null){
            sanitizeSingleHotZone(ctx, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + 26f);
        }

        for(RoomAnchor enemy : ctx.enemyRooms){
            sanitizeSingleHotZone(ctx, enemy.x, enemy.y, enemy.radius + 20f);
        }
    }

    private void sanitizeSingleHotZone(GenContext ctx, int cx, int cy, float radius){
        int range = Mathf.ceil(radius);
        float radius2 = radius * radius;

        for(int ox = -range; ox <= range; ox++){
            for(int oy = -range; oy <= range; oy++){
                float dst2 = ox * ox + oy * oy;
                if(dst2 > radius2) continue;

                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null || tile.block() != Blocks.air) continue;

                if(isHotOre(tile.overlay())){
                    tile.setOverlay(Blocks.air);
                }

                if(isHotFloor(tile.floor())){
                    tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
                }
            }
        }
    }

    private void seedCoreStarterOres(GenContext ctx){
        if(ctx.spawnRoom == null) return;

        placeCoreStarterOre(ctx, ore(WHBlocksEnvironment.manganeseOre), 42f, 20, 28);
        placeCoreStarterOre(ctx, ore(WHBlocksEnvironment.chromiumOre), 170f, 14, 15);
        placeCoreStarterOre(ctx, ore(WHBlocksEnvironment.cobaltOre), 292f, 24, 34);
    }

    private void placeCoreStarterOre(GenContext ctx, Block ore, float baseAngleDeg, int minPatch, int maxPatch){
        if(ore == Blocks.air || ctx.spawnRoom == null) return;

        int sx = ctx.spawnRoom.x;
        int sy = ctx.spawnRoom.y;
        float minDist = ctx.spawnRoom.radius + 7f;
        float maxDist = ctx.spawnRoom.radius + 13f;

        for(int i = 0; i < 36; i++){
            float angle = baseAngleDeg + ctx.rand.random(-34f, 34f);
            float dist = ctx.rand.random(minDist, maxDist);
            int x = sx + Mathf.round(Mathf.cosDeg(angle) * dist);
            int y = sy + Mathf.round(Mathf.sinDeg(angle) * dist);

            Tile center = ctx.tiles.get(x, y);
            if(!canPlaceStarterAt(ctx, center, ore)) continue;

            int target = ctx.rand.random(minPatch, maxPatch);
            int placed = placeStarterPatch(ctx, x, y, ore, target);
            if(placed >= minPatch / 2){
                return;
            }
        }
    }

    private int placeStarterPatch(GenContext ctx, int x, int y, Block ore, int target){
        if(target <= 0) return 0;

        Tile center = ctx.tiles.get(x, y);
        if(!canPlaceStarterAt(ctx, center, ore)) return 0;

        IntSeq frontier = new IntSeq();
        IntSet visited = new IntSet();
        frontier.add(center.pos());
        visited.add(center.pos());

        int placed = 0;
        while(frontier.size > 0 && placed < target){
            int idx = ctx.rand.random(frontier.size - 1);
            int packed = frontier.removeIndex(idx);
            Tile tile = ctx.tiles.getp(packed);
            if(tile == null || !canPlaceStarterAt(ctx, tile, ore)) continue;

            if(placeStarterOreAt(ctx, tile, ore)){
                placed++;
            }

            for(Point2 p : Geometry.d8){
                if(Math.abs(p.x) + Math.abs(p.y) == 2 && !ctx.rand.chance(0.5f)) continue;

                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(!canPlaceStarterAt(ctx, near, ore)) continue;

                int nearPos = near.pos();
                if(visited.contains(nearPos)) continue;
                visited.add(nearPos);
                frontier.add(nearPos);
            }
        }

        return placed;
    }

    private boolean canPlaceStarterAt(GenContext ctx, Tile tile, Block ore){
        if(tile == null) return false;
        if(tile.block() != Blocks.air) return false;
        if(tile.overlay() != Blocks.air) return false;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        if(nearWall(ctx, tile.x, tile.y)) return false;
        if(isNearEnemyRoom(ctx, tile.x, tile.y, 8f)) return false;
        if(!isNearSpawnRing(ctx, tile.x, tile.y, 5f, 18f)) return false;
        return canPlaceOnFloor(ore, tile.floor()) || canRethemeForOre(ore, tile.floor());
    }

    private boolean placeStarterOreAt(GenContext ctx, Tile tile, Block ore){
        if(!canPlaceStarterAt(ctx, tile, ore)) return false;

        Block hostFloor = hostFloorForOre(ctx, ore, tile.x, tile.y, tile.floor());
        if(hostFloor != null && hostFloor != tile.floor() && canRethemeForOre(ore, tile.floor())){
            tile.setFloor(hostFloor.asFloor());
        }

        if(!canPlaceOnFloor(ore, tile.floor())) return false;
        tile.setOverlay(ore);
        return true;
    }

    private boolean isNearSpawnRing(GenContext ctx, int x, int y, float innerPadding, float outerPadding){
        if(ctx.spawnRoom == null) return false;
        float dst = Mathf.dst(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y);
        float inner = ctx.spawnRoom.radius + innerPadding;
        float outer = ctx.spawnRoom.radius + outerPadding;
        return dst >= inner && dst <= outer;
    }

    private boolean isNearEnemyRoom(GenContext ctx, int x, int y, float padding){
        for(RoomAnchor enemy : ctx.enemyRooms){
            if(Mathf.within(x, y, enemy.x, enemy.y, enemy.radius + padding)){
                return true;
            }
        }
        return false;
    }

    private boolean isHotFloor(Block floor){
        return floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.hotrock
        || floor == Blocks.magmarock;
    }

    private boolean isHotOre(Block ore){
        return ore == Blocks.oreTungsten
        || ore == WHBlocksEnvironment.molybdenumOre
        || ore == WHBlocksEnvironment.vibraniumOre;
    }

    private void enforceOreFloorCompatibility(GenContext ctx, Seq<OreDef> ores){
        ObjectSet<Block> oreSet = new ObjectSet<>();
        for(OreDef def : ores){
            oreSet.add(def.ore);
        }

        for(Tile tile : ctx.tiles){
            Block overlay = tile.overlay();
            if(!oreSet.contains(overlay)) continue;

            if(tile.block() != Blocks.air
            || tile.floor().isLiquid
            || !tile.floor().hasSurface()
            || nearWall(ctx, tile.x, tile.y)
            || isNearCriticalRoom(ctx, tile.x, tile.y, 13f, 9f)){
                tile.setOverlay(Blocks.air);
                continue;
            }

            if(!isNearCriticalRoom(ctx, tile.x, tile.y, 16f, 12f)){
                Block hostFloor = hostFloorForOre(ctx, overlay, tile.x, tile.y, tile.floor());
                if(hostFloor != null && hostFloor != tile.floor() && canRethemeForOre(overlay, tile.floor())){
                    tile.setFloor(hostFloor.asFloor());
                }
            }

            if(!canPlaceOnFloor(overlay, tile.floor())){
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private int countOre(GenContext ctx, Block ore){
        int count = 0;
        for(Tile tile : ctx.tiles){
            if(tile.overlay() == ore) count++;
        }
        return count;
    }

    private int countNearOverlay(GenContext ctx, int x, int y, Block overlay){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.overlay() == overlay){
                count++;
            }
        }
        return count;
    }

    private int countOverlayRadius(GenContext ctx, int x, int y, Block overlay, int radius){
        int count = 0;
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                if(rx == 0 && ry == 0) continue;
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.overlay() == overlay){
                    count++;
                }
            }
        }
        return count;
    }

    private void trimToCap(GenContext ctx, Block ore, int cap){
        if(cap <= 0 || ore == null || ore == Blocks.air) return;

        int over = countOre(ctx, ore) - cap;
        if(over <= 0) return;

        for(int pass = 1; pass <= 3 && over > 0; pass++){
            for(Tile tile : ctx.tiles){
                if(over <= 0) break;
                if(tile.overlay() != ore) continue;
                if(countNearOverlay(ctx, tile.x, tile.y, ore) <= pass){
                    tile.setOverlay(Blocks.air);
                    over--;
                }
            }
        }

        if(over <= 0) return;

        for(Tile tile : ctx.tiles){
            if(over <= 0) break;
            if(tile.overlay() != ore) continue;

            int hash = Math.abs((tile.x * 73856093) ^ (tile.y * 19349663) ^ (ore.id * 83492791));
            if((hash & 3) == 0){
                tile.setOverlay(Blocks.air);
                over--;
            }
        }
    }

    private boolean nearOverlay(GenContext ctx, int x, int y, Block overlay, int radius){
        for(int rx = -radius; rx <= radius; rx++){
            for(int ry = -radius; ry <= radius; ry++){
                Tile near = ctx.tiles.get(x + rx, y + ry);
                if(near != null && near.overlay() == overlay){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean nearWall(GenContext ctx, int x, int y){
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.block() != Blocks.air){
                return true;
            }
        }
        return false;
    }

    private boolean isNearCriticalRoom(GenContext ctx, int x, int y, float spawnDst, float enemyDst){
        if(ctx.spawnRoom != null && Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + spawnDst)){
            return true;
        }
        for(RoomAnchor enemy : ctx.enemyRooms){
            if(Mathf.within(x, y, enemy.x, enemy.y, enemy.radius + enemyDst)){
                return true;
            }
        }
        return false;
    }

    private float noise(GenContext ctx, int seed, float x, float y, double octaves, double falloff, double scl){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z);
    }

    private Block ore(Block ore){
        return ore == null ? Blocks.air : ore;
    }
}
