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

public class KarvexOreBalancePass implements GenPass{
    private static class OreDef{
        final Block ore;
        final int tier;

        OreDef(Block ore, int tier){
            this.ore = ore;
            this.tier = tier;
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

        FloatSeq frequencies = buildFrequencies(ctx, ores);
        seedTieredOres(ctx, ores, frequencies);

        for(OreDef def : ores){
            expandOrePatches(ctx, def, def.tier <= 2 ? 1 : 0);
        }

        trimFragments(ctx, ores);

        for(OreDef def : ores){
            ensureMinimum(ctx, def, minimumFor(ctx, def));
        }

        for(OreDef def : ores){
            trimToCap(ctx, def.ore, maximumFor(ctx, def));
        }

        reinforceOreBiomes(ctx);
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

        if(manganese != Blocks.air) ores.add(new OreDef(manganese, 1));
        if(chromium != Blocks.air) ores.add(new OreDef(chromium, 2));
        if(cobalt != Blocks.air && (threat > 0.08f || poles > 0.24f || seedNoise > 0.14f)) ores.add(new OreDef(cobalt, 3));

        ores.add(new OreDef(Blocks.oreTungsten, 4));

        if(uranium != Blocks.air && (threat > 0.16f || seedNoise > 0.32f)) ores.add(new OreDef(uranium, 5));
        if(molybdenum != Blocks.air && threat > 0.26f) ores.add(new OreDef(molybdenum, 6));
        if(vibranium != Blocks.air && threat > 0.52f) ores.add(new OreDef(vibranium, 7));

        return ores;
    }

    private FloatSeq buildFrequencies(GenContext ctx, Seq<OreDef> ores){
        FloatSeq frequencies = new FloatSeq(ores.size);
        float poles = Math.abs(ctx.sector.tile.v.y);
        float threat = Mathf.clamp(ctx.sector.threat);

        for(int i = 0; i < ores.size; i++){
            frequencies.add(ctx.rand.random(-0.1f, 0.01f) - i * 0.014f + poles * 0.04f + threat * 0.03f);
        }
        return frequencies;
    }

    private void clearExistingOverlays(GenContext ctx, Seq<OreDef> ores){
        ObjectSet<Block> oreSet = new ObjectSet<>();
        for(OreDef def : ores){
            oreSet.add(def.ore);
        }

        for(Tile tile : ctx.tiles){
            Block overlay = tile.overlay();
            if(overlay == Blocks.spawn) continue;

            boolean clear = overlay.itemDrop != null || oreSet.contains(overlay);

            if(clear){
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void seedTieredOres(GenContext ctx, Seq<OreDef> ores, FloatSeq frequencies){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 14f, 10f)) continue;

            int offsetX = tile.x - 4;
            int offsetY = tile.y + 23;

            for(int i = ores.size - 1; i >= 0; i--){
                OreDef def = ores.get(i);
                if(!canPlaceOnFloor(def.ore, tile.floor())) continue;

                if(matchesBand(ctx, offsetX, offsetY, frequencies.get(i), i)){
                    placeOreAt(ctx, tile, def.ore);
                    break;
                }
            }
        }
    }

    private boolean matchesBand(GenContext ctx, int x, int y, float freq, int index){
        float a = Math.abs(0.5f - noise(ctx, ctx.seed + 100 + index * 13, x, y + index * 999f, 2, 0.7f, 40f + index * 3f));
        float b = Math.abs(0.5f - noise(ctx, ctx.seed + 200 + index * 11, x, y - index * 999f, 1, 1f, 30f + index * 4f));

        float thresholdA = 0.26f + index * 0.015f;
        float thresholdB = 0.40f + freq + index * 0.01f;

        if(a <= thresholdA || b <= thresholdB) return false;

        float threat = Mathf.clamp(ctx.sector.threat);
        float chance = Mathf.clamp((0.52f - index * 0.08f) + threat * 0.1f, 0.07f, 0.65f);
        return ctx.rand.chance(chance);
    }

    private void expandOrePatches(GenContext ctx, OreDef def, int iterations){
        float spreadChance = spreadChance(def.tier);

        for(int i = 0; i < iterations; i++){
            IntSeq toAdd = new IntSeq();

            for(Tile tile : ctx.tiles){
                if(tile.overlay() != def.ore) continue;

                for(Point2 p : Geometry.d4){
                    Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                    if(!canPlaceAtTile(near, def.ore)) continue;
                    if(isNearCriticalRoom(ctx, near.x, near.y, 14f, 10f)) continue;
                    if(!ctx.rand.chance(spreadChance)) continue;
                    toAdd.add(near.pos());
                }

                for(Point2 p : Geometry.d8){
                    if(Math.abs(p.x) + Math.abs(p.y) != 2) continue;
                    Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                    if(!canPlaceAtTile(near, def.ore)) continue;
                    if(isNearCriticalRoom(ctx, near.x, near.y, 14f, 10f)) continue;
                    if(!ctx.rand.chance(spreadChance * 0.52f)) continue;
                    toAdd.add(near.pos());
                }
            }

            for(int n = 0; n < toAdd.size; n++){
                Tile tile = ctx.tiles.getp(toAdd.items[n]);
                placeOreAt(ctx, tile, def.ore);
            }
        }
    }

    private void trimFragments(GenContext ctx, Seq<OreDef> ores){
        for(OreDef def : ores){
            int minNeighbors = def.tier <= 3 ? 2 : 1;
            float keepChance = def.tier <= 3 ? 0.14f : 0.08f;

            for(Tile tile : ctx.tiles){
                if(tile.overlay() != def.ore) continue;
                int same = countNearOverlay(ctx, tile.x, tile.y, def.ore);
                if(same <= minNeighbors && !ctx.rand.chance(keepChance)){
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
        int tries = need * 220;

        for(int i = 0; i < tries && need > 0; i++){
            int x = ctx.rand.random(2, ctx.width() - 3);
            int y = ctx.rand.random(2, ctx.height() - 3);
            Tile tile = ctx.tiles.getn(x, y);

            if(!canPlaceAtTile(tile, def.ore)) continue;
            if(isNearCriticalRoom(ctx, x, y, 14f, 10f)) continue;

            boolean nearSame = nearOverlay(ctx, x, y, def.ore, 1);
            if(hasAny && !nearSame && !ctx.rand.chance(def.tier >= 5 ? 0.36f : 0.16f)) continue;

            int placed = placePatch(ctx, x, y, def.ore, patchSize);
            if(placed > 0){
                hasAny = true;
                need -= placed;
            }
        }

        if(need <= 0) return;

        for(Tile tile : ctx.tiles){
            if(need <= 0) break;
            if(!canPlaceAtTile(tile, def.ore)) continue;
            if(isNearCriticalRoom(ctx, tile.x, tile.y, 12f, 8f)) continue;
            if(def.tier < 5 && !nearOverlay(ctx, tile.x, tile.y, def.ore, 1) && !ctx.rand.chance(0.08f)) continue;

            int placed = placePatch(ctx, tile.x, tile.y, def.ore, Math.max(2, patchSize - 1));
            need -= placed;
        }
    }

    private int placePatch(GenContext ctx, int x, int y, Block ore, int target){
        int placed = 0;

        Tile center = ctx.tiles.get(x, y);
        if(placeOreAt(ctx, center, ore)){
            placed++;
        }

        if(placed >= target) return placed;

        for(Point2 p : Geometry.d4){
            if(placed >= target) break;
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(!canPlaceAtTile(near, ore)) continue;
            if(ctx.rand.chance(0.66f) && placeOreAt(ctx, near, ore)){
                placed++;
            }
        }

        if(placed >= target) return placed;

        for(Point2 p : Geometry.d8){
            if(placed >= target) break;
            if(Math.abs(p.x) + Math.abs(p.y) != 2) continue;
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(!canPlaceAtTile(near, ore)) continue;
            if(ctx.rand.chance(0.38f) && placeOreAt(ctx, near, ore)){
                placed++;
            }
        }

        return placed;
    }

    private boolean placeOreAt(GenContext ctx, Tile tile, Block ore){
        if(!canPlaceAtTile(tile, ore)) return false;

        Block floor = tile.floor();

        if(ore == WHBlocksEnvironment.uraniumOre){
            if(!isRadiationFloor(floor)){
                tile.setFloor(WHBlocksEnvironment.radiationRockFloor.asFloor());
            }
        }else if(ore == WHBlocksEnvironment.manganeseOre){
            if(canRethemeManganese(floor)) tile.setFloor(WHBlocksEnvironment.manganeseStone.asFloor());
        }else if(ore == WHBlocksEnvironment.chromiumOre){
            if(canRethemeChromium(floor)) tile.setFloor(WHBlocksEnvironment.chromiteStone.asFloor());
        }else if(ore == WHBlocksEnvironment.cobaltOre){
            if(canRethemeCobalt(floor)) tile.setFloor(WHBlocksEnvironment.cobaltStone.asFloor());
        }else if(ore == WHBlocksEnvironment.molybdenumOre){
            if(canRethemeMoly(floor)) tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
        }else if(ore == WHBlocksEnvironment.vibraniumOre){
            if(canRethemeVibranium(floor)){
                tile.setFloor((ctx.rand.chance(0.66f) ? WHBlocksEnvironment.darkMagmaRock : WHBlocksEnvironment.cobaltStone).asFloor());
            }
        }else if(ore == Blocks.oreTungsten){
            if(canRethemeTungsten(floor) && ctx.rand.chance(0.52f)){
                tile.setFloor((ctx.rand.chance(0.54f) ? WHBlocksEnvironment.scorchedStone : WHBlocksEnvironment.darkRock).asFloor());
            }
        }

        tile.setOverlay(ore);
        return true;
    }

    private void reinforceOreBiomes(GenContext ctx){
        for(Tile tile : ctx.tiles){
            Block ore = tile.overlay();
            if(ore == Blocks.air || ore == Blocks.spawn) continue;

            Block target = themeFloorForOre(ore, ctx);
            if(target == null) continue;

            for(Point2 p : Geometry.d8){
                Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                if(near == null || near.block() != Blocks.air) continue;
                if(!near.floor().hasSurface() || near.floor().isLiquid) continue;
                if(isNearCriticalRoom(ctx, near.x, near.y, 12f, 9f)) continue;
                if(!canRethemeFloor(near.floor())) continue;

                boolean cardinal = Math.abs(p.x) + Math.abs(p.y) == 1;
                float chance = cardinal ? 0.42f : 0.22f;
                if(!ctx.rand.chance(chance)) continue;

                near.setFloor(target.asFloor());
            }
        }
    }

    private Block themeFloorForOre(Block ore, GenContext ctx){
        if(ore == WHBlocksEnvironment.manganeseOre) return WHBlocksEnvironment.manganeseStone;
        if(ore == WHBlocksEnvironment.chromiumOre) return WHBlocksEnvironment.chromiteStone;
        if(ore == WHBlocksEnvironment.cobaltOre) return WHBlocksEnvironment.cobaltStone;
        if(ore == WHBlocksEnvironment.uraniumOre) return ctx.rand.chance(0.70f) ? WHBlocksEnvironment.radiationRockFloor : WHBlocksEnvironment.radiationSand;
        if(ore == WHBlocksEnvironment.molybdenumOre) return WHBlocksEnvironment.darkRock;
        if(ore == WHBlocksEnvironment.vibraniumOre) return ctx.rand.chance(0.64f) ? WHBlocksEnvironment.darkMagmaRock : WHBlocksEnvironment.cobaltStone;
        if(ore == Blocks.oreTungsten) return ctx.rand.chance(0.58f) ? WHBlocksEnvironment.scorchedStone : WHBlocksEnvironment.darkRock;
        return null;
    }

    private int minimumFor(GenContext ctx, OreDef def){
        int area = ctx.width() * ctx.height();
        float threat = Mathf.clamp(ctx.sector.threat);

        if(def.ore == WHBlocksEnvironment.manganeseOre){
            return Math.max(9, area / 42000);
        }else if(def.ore == WHBlocksEnvironment.chromiumOre){
            return Math.max(7, area / 52000);
        }else if(def.ore == WHBlocksEnvironment.cobaltOre){
            return Math.max(5, Mathf.round(area / 70000f * (0.8f + threat * 0.4f)));
        }else if(def.ore == Blocks.oreTungsten){
            return Math.max(5, area / 60000);
        }else if(def.ore == WHBlocksEnvironment.uraniumOre){
            return Math.max(3, Mathf.round(area / 90000f * (0.75f + threat)));
        }else if(def.ore == WHBlocksEnvironment.molybdenumOre){
            return Math.max(2, Mathf.round(area / 120000f * (0.75f + threat)));
        }else if(def.ore == WHBlocksEnvironment.vibraniumOre){
            return Math.max(1, Mathf.round(area / 180000f * Math.max(0.25f, threat)));
        }

        return 0;
    }

    private int maximumFor(GenContext ctx, OreDef def){
        int area = ctx.width() * ctx.height();
        float threat = Mathf.clamp(ctx.sector.threat);

        if(def.ore == WHBlocksEnvironment.manganeseOre){
            return Math.max(48, area / 5200);
        }else if(def.ore == WHBlocksEnvironment.chromiumOre){
            return Math.max(36, area / 6800);
        }else if(def.ore == WHBlocksEnvironment.cobaltOre){
            return Math.max(24, area / 9200);
        }else if(def.ore == Blocks.oreTungsten){
            return Math.max(22, area / 12000);
        }else if(def.ore == WHBlocksEnvironment.uraniumOre){
            return Math.max(12, Mathf.round(area / 18000f * (0.85f + threat * 0.25f)));
        }else if(def.ore == WHBlocksEnvironment.molybdenumOre){
            return Math.max(8, Mathf.round(area / 26000f * (0.8f + threat * 0.2f)));
        }else if(def.ore == WHBlocksEnvironment.vibraniumOre){
            return Math.max(4, Mathf.round(area / 42000f * Math.max(0.25f, threat * 0.35f)));
        }

        return Integer.MAX_VALUE;
    }

    private int patchSize(int tier){
        if(tier <= 2) return 3;
        if(tier <= 4) return 2;
        if(tier <= 6) return 2;
        return 2;
    }

    private float spreadChance(int tier){
        if(tier <= 1) return 0.34f;
        if(tier == 2) return 0.30f;
        if(tier == 3) return 0.24f;
        if(tier == 4) return 0.20f;
        if(tier == 5) return 0.16f;
        if(tier == 6) return 0.12f;
        return 0.08f;
    }

    private boolean canPlaceAtTile(Tile tile, Block ore){
        if(tile == null) return false;
        if(tile.block() != Blocks.air) return false;
        if(tile.overlay() != Blocks.air) return false;
        if(!tile.floor().hasSurface() || tile.floor().isLiquid) return false;
        return canPlaceOnFloor(ore, tile.floor());
    }

    private boolean canPlaceOnFloor(Block ore, Block floor){
        if(ore == WHBlocksEnvironment.manganeseOre){
            return floor == WHBlocksEnvironment.manganeseStone
            || floor == WHBlocksEnvironment.darkRock
            || floor == WHBlocksEnvironment.mineralSandstone
            || floor == Blocks.yellowStone
            || floor == Blocks.yellowStonePlates;
        }else if(ore == WHBlocksEnvironment.chromiumOre){
            return floor == WHBlocksEnvironment.chromiteStone
            || floor == WHBlocksEnvironment.manganeseStone
            || floor == WHBlocksEnvironment.darkRock
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
            || floor == WHBlocksEnvironment.radiationSandWater
            || floor == WHBlocksEnvironment.mineralSandRadiationWater
            || floor == Blocks.redmat
            || floor == Blocks.redStone
            || floor == Blocks.denseRedStone
            || floor == Blocks.redIce;
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

    private boolean canRethemeFloor(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.promethiumSand
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltStone
        || floor == Blocks.stone
        || floor == Blocks.shale
        || floor == Blocks.craters
        || floor == Blocks.darksand
        || floor == Blocks.yellowStone
        || floor == Blocks.yellowStonePlates
        || floor == Blocks.carbonStone
        || floor == Blocks.rhyolite
        || floor == Blocks.roughRhyolite
        || floor == Blocks.dacite
        || floor == Blocks.ferricStone
        || floor == Blocks.ferricCraters;
    }

    private boolean canRethemeManganese(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.darkRock
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
        || floor == Blocks.carbonStone;
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

    private void trimToCap(GenContext ctx, Block ore, int cap){
        if(cap <= 0 || ore == null || ore == Blocks.air) return;

        int over = countOre(ctx, ore) - cap;
        if(over <= 0) return;

        for(Tile tile : ctx.tiles){
            if(over <= 0) break;
            if(tile.overlay() != ore) continue;
            if(countNearOverlay(ctx, tile.x, tile.y, ore) <= 2){
                tile.setOverlay(Blocks.air);
                over--;
            }
        }

        for(Tile tile : ctx.tiles){
            if(over <= 0) break;
            if(tile.overlay() != ore) continue;
            if(ctx.rand.chance(0.46f)){
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
