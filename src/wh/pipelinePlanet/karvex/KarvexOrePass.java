package wh.pipelinePlanet.karvex;

import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;
import wh.content.WHBlocksEnvironment;
import wh.pipelinePlanet.core.GenContext;
import wh.pipelinePlanet.core.GenPass;

/**
 * Places sparse, coherent surface ore veins after terrain and water generation.
 */
public class KarvexOrePass implements GenPass {
    private static final int MANGANESE = 1;
    private static final int CHROMITE = 2;
    private static final int COAL = 3;
    private static final int COBALT = 4;
    private static final int URANIUM = 5;
    private static final int MOLYBDENUM = 6;

    private static class OreRule {
        final Block ore;
        final int terrain;
        final int seedOffset;
        final float firstThreshold;
        final float secondThreshold;
        final float scale;

        OreRule(Block ore, int terrain, int seedOffset, float firstThreshold, float secondThreshold, float scale) {
            this.ore = ore;
            this.terrain = terrain;
            this.seedOffset = seedOffset;
            this.firstThreshold = firstThreshold;
            this.secondThreshold = secondThreshold;
            this.scale = scale;
        }
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void apply(GenContext ctx) {
        placeSaltShore(ctx);

        Seq<OreRule> rules = new Seq<>();
        rules.add(new OreRule(WHBlocksEnvironment.molybdenumOre, MOLYBDENUM, 587, 0.46f, 0.49f, 62f));
        rules.add(new OreRule(WHBlocksEnvironment.uraniumOre, URANIUM, 571, 0.44f, 0.48f, 58f));
        rules.add(new OreRule(WHBlocksEnvironment.cobaltOre, COBALT, 557, 0.43f, 0.48f, 54f));
        rules.add(new OreRule(Blocks.oreCoal, COAL, 541, 0.43f, 0.48f, 50f));
        rules.add(new OreRule(WHBlocksEnvironment.chromiumOre, CHROMITE, 523, 0.43f, 0.48f, 46f));
        rules.add(new OreRule(WHBlocksEnvironment.manganeseOre, MANGANESE, 511, 0.43f, 0.48f, 44f));

        int[] placed = new int[rules.size];

        for (Tile tile : ctx.tiles) {
            if (!canPlaceOre(ctx, tile)) continue;

            for (int i = 0; i < rules.size; i++) {
                OreRule rule = rules.get(i);
                if (rule.ore == null || !matchesTerrain(tile.floor(), rule.terrain)) continue;
                if (!hasTerrainNeighbor(ctx, tile, rule.terrain)) continue;

                float first = oreNoise(ctx, tile.x, tile.y, rule.seedOffset, rule.scale, 0);
                float second = oreNoise(ctx, tile.x, tile.y, rule.seedOffset + 37, rule.scale * 0.52f, 911);
                float firstThreshold = rule.firstThreshold;
                float secondThreshold = rule.secondThreshold;

                // Coal is shale-dominant, but can occur sparsely in other host terrain.
                if (rule.terrain == COAL && tile.floor() != WHBlocksEnvironment.oreShale1) {
                    firstThreshold += 0.045f;
                    secondThreshold += 0.015f;
                }

                if (Math.abs(0.5f - first) > firstThreshold
                        && Math.abs(0.5f - second) > secondThreshold) {
                    tile.setOverlay(rule.ore);
                    placed[i]++;
                    break;
                }
            }
        }

        ensureMandatoryVein(ctx, rules.get(rules.size - 2), placed[rules.size - 2]);
        ensureMandatoryVein(ctx, rules.get(rules.size - 1), placed[rules.size - 1]);
    }

    private void placeSaltShore(GenContext ctx) {
        for (Tile tile : ctx.tiles) {
            if (tile.floor() != WHBlocksEnvironment.mineralSand || tile.block() != Blocks.air || tile.overlay() != Blocks.air) {
                continue;
            }

            int distance = nearestLiquidDistance(ctx, tile.x, tile.y, 3);
            if (distance < 1) continue;

            float field = oreNoise(ctx, tile.x, tile.y, 607, 34f, 1207);
            if (field > 0.84f - distance * 0.025f) {
                tile.setFloor(WHBlocksEnvironment.oreSalt.asFloor());
            }
        }
    }

    private void ensureMandatoryVein(GenContext ctx, OreRule rule, int placed) {
        if (rule == null || rule.ore == null || placed > 0) return;

        Tile best = null;
        float bestScore = -Float.MAX_VALUE;
        for (Tile tile : ctx.tiles) {
            if (!canPlaceOre(ctx, tile) || !matchesTerrain(tile.floor(), rule.terrain)) continue;
            if (!hasTerrainNeighbor(ctx, tile, rule.terrain)) continue;

            float score = oreNoise(ctx, tile.x, tile.y, rule.seedOffset, rule.scale, 0);
            if (score > bestScore) {
                bestScore = score;
                best = tile;
            }
        }

        if (best != null) best.setOverlay(rule.ore);
    }

    private boolean canPlaceOre(GenContext ctx, Tile tile) {
        if (tile.block() != Blocks.air || tile.overlay() != Blocks.air) return false;
        if (!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        return ctx.spawnRoom == null || !Mathf.within(tile.x, tile.y, ctx.spawnRoom.x, ctx.spawnRoom.y, 10f);
    }

    private boolean hasTerrainNeighbor(GenContext ctx, Tile tile, int terrain) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                Tile near = ctx.tiles.get(tile.x + dx, tile.y + dy);
                if (near != null && matchesTerrain(near.floor(), terrain)) count++;
            }
        }
        return count >= 3;
    }

    private boolean matchesTerrain(Block floor, int terrain) {
        if (terrain == MANGANESE) {
            return floor == WHBlocksEnvironment.manganeseFloor || floor == WHBlocksEnvironment.manganeseStone;
        }
        if (terrain == CHROMITE) {
            return floor == WHBlocksEnvironment.chromiteFloor
                    || floor == WHBlocksEnvironment.chromiteFloorDark
                    || floor == WHBlocksEnvironment.chromiteStone;
        }
        if (terrain == COAL) {
            return floor == WHBlocksEnvironment.oreShale1
                    || floor == WHBlocksEnvironment.darkRock
                    || floor == WHBlocksEnvironment.trachyte
                    || floor == WHBlocksEnvironment.darkMineralSandstone
                    || floor == WHBlocksEnvironment.darkMineralFloor
                    || floor == WHBlocksEnvironment.mineralSandFloor
                    || floor == WHBlocksEnvironment.mineralSand
                    || floor == WHBlocksEnvironment.gravel;
        }
        if (terrain == COBALT) {
            return floor == WHBlocksEnvironment.cobaltFloor || floor == WHBlocksEnvironment.cobaltStone;
        }
        if (terrain == URANIUM) {
            return floor == WHBlocksEnvironment.radiationSand
                    || floor == WHBlocksEnvironment.radiationRockFloor
                    || floor == WHBlocksEnvironment.radiationCraters;
        }
        return terrain == MOLYBDENUM && (floor == WHBlocksEnvironment.radiationSand
                || floor == WHBlocksEnvironment.radiationRockFloor
                || floor == WHBlocksEnvironment.radiationCraters
                || floor == WHBlocksEnvironment.darkRock
                || floor == WHBlocksEnvironment.darkHotRock
                || floor == WHBlocksEnvironment.darkMagmaRock
                || floor == WHBlocksEnvironment.darkRockCraters);
    }

    private float oreNoise(GenContext ctx, int x, int y, int seedOffset, float scale, float yOffset) {
        Vec3 position = ctx.sector.rect.project(x, y).scl(5f);
        float broad = Simplex.noise3d(ctx.seed + seedOffset, 3, 0.66f, 1f / scale,
                position.x, position.y + yOffset, position.z);
        float detail = Simplex.noise3d(ctx.seed + seedOffset + 1, 2, 0.72f, 1f / (scale * 0.46f),
                position.x + 31f, position.y + yOffset - 59f, position.z) * 0.18f;
        return Mathf.clamp(broad + detail);
    }

    private int nearestLiquidDistance(GenContext ctx, int x, int y, int radius) {
        for (int distance = 1; distance <= radius; distance++) {
            for (int dx = -distance; dx <= distance; dx++) {
                for (int dy = -distance; dy <= distance; dy++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != distance) continue;
                    Tile near = ctx.tiles.get(x + dx, y + dy);
                    if (near != null && near.floor().isLiquid) return distance;
                }
            }
        }
        return -1;
    }
}
