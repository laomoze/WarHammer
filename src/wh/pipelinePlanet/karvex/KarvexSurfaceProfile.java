package wh.pipelinePlanet.karvex;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import wh.content.*;

/**
 * 中文说明：Karvex 地表采样配置：高度、温度、污染与配色。
 */
public class KarvexSurfaceProfile{
    public final float heightYOffset = 12f;
    public final float scl = 5.15f;
    public final float waterOffset = 0.03f;
    public final float heightScl = 1.04f;

    public final int heightOctaves = 8;
    public final float heightPersistence = 0.58f;
    public final float heightScale = 1f / 2.75f;
    public final float heightPower = 1.82f;

    public final int tempOctaves = 6;
    public final float tempPersistence = 0.60f;
    public final float tempScale = 1f / 3.1f;

    public final int pollutionOctaves = 5;
    public final float pollutionPersistence = 0.58f;
    public final float pollutionScale = 1f / 2.15f;

    public final Block[][] surfaceLut = createSurfaceLut();

    public final ObjectMap<Block, Block> decorationByFloor = new ObjectMap<Block, Block>(){{
        put(WHBlocksEnvironment.mineralSand, WHBlocksEnvironment.mineralSandBoulder);
        put(WHBlocksEnvironment.mineralSandstone, WHBlocksEnvironment.mineralSandBoulder);
        put(WHBlocksEnvironment.quartzSand, WHBlocksEnvironment.quartzCrystalCluster);
        put(WHBlocksEnvironment.manganeseStone, WHBlocksEnvironment.manganeseBoulder);
        put(WHBlocksEnvironment.chromiteStone, WHBlocksEnvironment.chromiteBoulder);
        put(WHBlocksEnvironment.cobaltStone, WHBlocksEnvironment.cobaltBoulder);
        put(WHBlocksEnvironment.darkRock, WHBlocksEnvironment.darkRockBoulder);
        put(WHBlocksEnvironment.darkHotRock, WHBlocksEnvironment.darkRockBoulder);
        put(WHBlocksEnvironment.darkMagmaRock, WHBlocksEnvironment.darkRockBoulder);
        put(WHBlocksEnvironment.radiationSand, WHBlocksEnvironment.radiationBoulder);
        put(WHBlocksEnvironment.radiationRockFloor, WHBlocksEnvironment.radiationBoulder);
        put(Blocks.dacite, Blocks.daciteBoulder);
        put(Blocks.ferricStone, Blocks.ferricBoulder);
        put(Blocks.ferricCraters, Blocks.ferricBoulder);
        put(Blocks.crystalFloor, Blocks.crystalCluster);
    }};

    public final float seaLevel = 2.35f / surfaceLut[0].length;
    public final Vec3 enemyCoreRingCenter = new Vec3(0.9341721f, 0f, 0.3568221f);

    public float sampleRawHeight(int seed, Vec3 position){
        float noise = Simplex.noise3d(seed, heightOctaves, heightPersistence, heightScale,
        position.x * scl,
        position.y * scl + heightYOffset,
        position.z * scl);
        float shaped = Mathf.pow(Mathf.clamp(noise * heightScl), heightPower);
        return (shaped + waterOffset) / (1f + waterOffset);
    }

    public Block selectSurfaceBlock(int seed, Vec3 position, Seq<Sector> sectors){
        return selectSurfaceBlock(seed, position, sectors, true);
    }

    public void sampleSurfaceColor(int seed, Vec3 position, Seq<Sector> sectors, Color out){
        Block block = selectSurfaceBlock(seed, position, sectors, true);
        out.set(block.mapColor).a(1f - block.albedo);

        if(block.asFloor().isLiquid){
            out.lerp(block.mapColor, 0.34f);
            out.b = Mathf.clamp(out.b + 0.04f);
        }

        // Keep the planet in a tighter, colder palette to avoid noisy "rainbow" patches.
        out.r = Mathf.lerp(out.r, 0.56f, 0.10f);
        out.g = Mathf.lerp(out.g, 0.60f, 0.12f);
        out.b = Mathf.lerp(out.b, 0.66f, 0.14f);

        float gray = (out.r + out.g + out.b) / 3f;
        out.r = Mathf.lerp(out.r, gray, 0.22f);
        out.g = Mathf.lerp(out.g, gray, 0.30f);
        out.b = Mathf.lerp(out.b, gray, 0.36f);

        out.r = Mathf.clamp(out.r * 0.84f);
        out.g = Mathf.clamp(out.g * 0.84f);
        out.b = Mathf.clamp(out.b * 0.84f);
    }

    public Block selectSurfaceBlock(int seed, Vec3 position, Seq<Sector> sectors, boolean allowLiquidEnrichment){
        float height = Mathf.clamp(sampleRawHeight(seed, position) * 1.04f);
        float temp = sampleRawTemperature(seed, position);

        float px = position.x * scl;
        float py = position.y * scl;
        float pz = position.z * scl;

        int tempIndex = Mathf.clamp((int)(temp * surfaceLut.length), 0, surfaceLut.length - 1);
        int heightIndex = Mathf.clamp((int)(height * surfaceLut[0].length), 0, surfaceLut[0].length - 1);

        Block result = sanitizeExcludedTerrain(surfaceLut[tempIndex][heightIndex]);
        result = applyVolcanicBias(seed, px, py, pz, temp, result);
        result = applyRadiationScatter(seed, px, py, pz, height, result);
        result = applyPolarRedBand(seed, position, px, py, pz, temp, height, result);

        if(allowLiquidEnrichment){
            result = applyLiquidEnrichment(seed, position, px, py, pz, temp, height, result);
        }

        return applyEnemyCoreRing(seed, position, sectors, result);
    }

    public Block decorationForFloor(Block floor){
        return decorationByFloor.get(floor, floor.asFloor().decoration);
    }

    private float sampleRawTemperature(int seed, Vec3 position){
        float px = position.x * scl;
        float py = position.y * scl;
        float pz = position.z * scl;

        float base = 1f - Math.abs(position.y);
        float tempNoise = Simplex.noise3d(seed + 31, tempOctaves, tempPersistence, tempScale, px, py + 851f, pz) * 0.19f;
        float heatRift = Simplex.noise3d(seed + 47, 3, 0.57f, 1f / 5.8f, px, py + 203f, pz) * 0.14f;
        return Mathf.clamp(base + tempNoise + heatRift);
    }

    private float samplePollution(int seed, Vec3 position, float px, float py, float pz, float temp, float height){
        float noisy = Simplex.noise3d(seed + 79, pollutionOctaves, pollutionPersistence, pollutionScale, px, py + 999f, pz) * 0.58f;
        float polar = Math.abs(position.y) * 0.22f;
        float lowland = (seaLevel - height) * 1.8f;
        float heat = (temp - 0.55f) * 0.28f;
        return noisy + polar + lowland + heat;
    }

    private Block applyLiquidEnrichment(int seed, Vec3 position, float px, float py, float pz, float temp, float height, Block block){
        float polluted = samplePollution(seed, position, px, py, pz, temp, height);
        float radField = Simplex.noise3d(seed + 109, 3, 0.58f, 1f / 4.6f, px, py + 731f, pz);
        float basinMask = Simplex.noise3d(seed + 113, 2, 0.60f, 1f / 7.8f, px, py + 417f, pz);

        if(height <= seaLevel + 0.016f){
            boolean deep = height < seaLevel - 0.020f;

            if((radField > 0.76f || (isRadiationFloor(block) && radField > 0.58f)) && basinMask > 0.08f){
                return deep ? WHBlocksEnvironment.radiationWaterDeep : WHBlocksEnvironment.mineralSandRadiationWater;
            }

            if(polluted > 0.56f && basinMask > -0.04f){
                if(block == Blocks.darksand || block == Blocks.shale || block == WHBlocksEnvironment.darkRock){
                    return deep ? Blocks.deepTaintedWater : Blocks.darksandTaintedWater;
                }
                return deep ? WHBlocksEnvironment.effluentDeep : WHBlocksEnvironment.mineralSandEffluentWater;
            }
        }

        if(block == WHBlocksEnvironment.promethiumSand){
            float prom = Simplex.noise3d(seed + 121, 2, 0.60f, 1f / 4.2f, px, py + 179f, pz);
            if(prom > 0.92f && height <= seaLevel + 0.05f){
                return WHBlocksEnvironment.promethium;
            }
        }

        return block;
    }

    private Block applyMetalBelts(int seed, float px, float py, float pz, Block block){
        if(block == null || !block.asFloor().hasSurface() || block.asFloor().isLiquid) return block;
        if(block == Blocks.redIce || block == Blocks.redStone || block == Blocks.denseRedStone || block == Blocks.redmat) return block;
        if(isRadiationFloor(block)) return block;

        float belt = Simplex.noise3d(seed + 149, 3, 0.57f, 1f / 9.8f, px, py + 487f, pz)
        + Simplex.noise3d(seed + 150, 2, 0.60f, 1f / 20.0f, px, py + 121f, pz) * 0.20f;
        if(belt <= 0.81f) return block;

        float region = Simplex.noise3d(seed + 154, 2, 0.60f, 1f / 17.5f, px, py + 213f, pz);
        float local = Simplex.noise3d(seed + 151, 2, 0.59f, 1f / 8.2f, px, py + 73f, pz)
        + Simplex.noise3d(seed + 152, 1, 1f, 1f / 18.0f, px, py + 931f, pz) * 0.16f;
        float selector = region * 0.72f + local * 0.28f;

        if(selector > 0.63f){
            return WHBlocksEnvironment.cobaltStone;
        }else if(selector > 0.22f){
            return WHBlocksEnvironment.chromiteStone;
        }else if(selector > -0.10f){
            return WHBlocksEnvironment.manganeseStone;
        }else if(block == Blocks.shale){
            return WHBlocksEnvironment.darkRock;
        }

        return block;
    }

    private Block[][] createSurfaceLut(){
        Block ed = WHBlocksEnvironment.effluentDeep;
        Block es = WHBlocksEnvironment.effluent;
        Block msw = WHBlocksEnvironment.mineralSandEffluentWater;
        Block ms = WHBlocksEnvironment.mineralSand;
        Block mst = WHBlocksEnvironment.mineralSandstone;

        Block dr = WHBlocksEnvironment.darkRock;
        Block mn = WHBlocksEnvironment.manganeseStone;
        Block ch = WHBlocksEnvironment.chromiteStone;
        Block co = WHBlocksEnvironment.cobaltStone;

        Block cb = Blocks.carbonStone;
        Block rh = Blocks.rhyolite;
        Block rr = Blocks.roughRhyolite;

        Block se = WHBlocksEnvironment.scorchedEarth;
        Block ss = WHBlocksEnvironment.scorchedStone;
        Block hr = WHBlocksEnvironment.darkHotRock;
        Block mr = WHBlocksEnvironment.darkMagmaRock;

        Block fs = Blocks.ferricStone;
        Block rs = Blocks.redStone;
        Block drs = Blocks.denseRedStone;
        Block ri = Blocks.redIce;

        return new Block[][]{
        {ed, msw, ms, mst, mst, dr, mn, ch, cb, fs, rs, ri},
        {ed, es, msw, ms, mst, mst, dr, ch, cb, fs, rs, ri},
        {es, msw, ms, mst, mst, mst, dr, ch, cb, rh, fs, se},
        {msw, ms, mst, mst, mst, dr, ch, cb, rh, rr, se, ss},
        {ms, mst, mst, mst, dr, mn, ch, cb, rh, rr, hr, ss},
        {mst, mst, dr, dr, mn, ch, co, cb, rh, hr, mr, Blocks.magmarock},
        {mst, dr, dr, ch, ch, cb, rh, rr, hr, mr, Blocks.magmarock, Blocks.crystallineStone},
        {dr, dr, ch, ch, cb, rh, rr, hr, mr, Blocks.magmarock, Blocks.crystallineStone, Blocks.crystalFloor},
        {dr, dr, ch, cb, rh, rr, hr, mr, Blocks.magmarock, Blocks.crystallineStone, Blocks.crystalFloor, Blocks.crystalFloor}
        };
    }

    private Block applyVolcanicBias(int seed, float px, float py, float pz, float temp, Block block){
        if(block == null || !block.asFloor().hasSurface() || block.asFloor().isLiquid) return block;
        if(temp < 0.71f) return block;

        float field = Simplex.noise3d(seed + 171, 3, 0.58f, 1f / 3.0f, px, py + 187f, pz)
        + Simplex.noise3d(seed + 173, 2, 0.56f, 1f / 7.8f, px, py + 61f, pz) * 0.2f
        + (temp - 0.71f) * 0.20f;

        if(field > 0.95f) return WHBlocksEnvironment.darkMagmaRock;
        if(field > 0.84f) return WHBlocksEnvironment.darkHotRock;
        if(block == WHBlocksEnvironment.darkRock) return block;
        if(field > 0.71f && block != Blocks.redIce) return WHBlocksEnvironment.scorchedEarth;
        if(field > 0.64f && block != Blocks.redIce) return WHBlocksEnvironment.scorchedEarth;

        return block;
    }

    private Block applyRadiationScatter(int seed, float px, float py, float pz, float height, Block block){
        if(block == null || !block.asFloor().hasSurface() || block.asFloor().isLiquid) return block;
        if(block == Blocks.redmat || block == Blocks.redStone || block == Blocks.denseRedStone || block == Blocks.redIce) return block;
        if(block == WHBlocksEnvironment.chromiteStone || block == WHBlocksEnvironment.manganeseStone || block == WHBlocksEnvironment.cobaltStone) return block;
        if(!isRadiationScatterHost(block)) return block;

        float mask = Simplex.noise3d(seed + 199, 2, 0.57f, 1f / 48f, px, py + 173f, pz)
        + Simplex.noise3d(seed + 201, 2, 0.60f, 1f / 79f, px, py + 631f, pz) * 0.16f;
        if(mask < 0.973f) return block;

        float fieldA = Simplex.noise3d(seed + 193, 3, 0.58f, 1f / 10.5f, px, py + 333f, pz);
        float fieldB = Simplex.noise3d(seed + 197, 2, 0.56f, 1f / 21f, px, py + 919f, pz);
        float field = fieldA * 0.72f + fieldB * 0.28f + (height - 0.52f) * 0.05f;

        if(field > 0.992f) return WHBlocksEnvironment.radiationRockFloor;
        if(field > 0.962f) return WHBlocksEnvironment.radiationCraters;
        if(field > 0.934f) return WHBlocksEnvironment.radiationSand;

        return block;
    }

    private Block applyPolarRedBand(int seed, Vec3 position, float px, float py, float pz, float temp, float height, Block block){
        if(block == null || !block.asFloor().hasSurface() || block.asFloor().isLiquid) return block;

        float polar = Math.abs(position.y);
        if(polar < 0.85f) return block;

        float bandNoise = Simplex.noise3d(seed + 211, 2, 0.58f, 1f / 4.9f, px, py + 701f, pz);
        float force = (polar - 0.85f) / 0.15f + bandNoise * 0.10f + (temp - 0.55f) * 0.12f + (height - 0.55f) * 0.04f;

        if(force > 1.30f) return Blocks.redIce;
        if(force > 1.06f) return Blocks.denseRedStone;
        if(force > 0.86f) return Blocks.redStone;
        if(force > 0.68f) return Blocks.redmat;

        return block;
    }

    private Block applyEnemyCoreRing(int seed, Vec3 position, Seq<Sector> sectors, Block block){
        if(!position.within(enemyCoreRingCenter, 0.65f)) return block;

        float dst = nearestEnemyCoreDistance(position, sectors);
        float freq = 0.05f;
        float ring = dst * 0.85f
        + Simplex.noise3d(seed, 3, 0.4f, 5.5f, position.x, position.y + 200f, position.z) * 0.015f
        + ((enemyCoreRingCenter.dst(position) % freq) < freq / 2f ? 1f : 0f) * 0.07f;

        if(ring < 0.15f){
            float freq2 = 0.07f;
            return ((enemyCoreRingCenter.dst(position) + 0.01f) % freq2 < freq2 * 0.65f) ? Blocks.metalFloor : Blocks.darkPanel6;
        }

        return block;
    }

    private Block sanitizeExcludedTerrain(Block block){
        if(block == WHBlocksEnvironment.cementFloor
        || block == WHBlocksEnvironment.cementTile1
        || block == WHBlocksEnvironment.cementTile2
        || block == WHBlocksEnvironment.cementTile3
        || block == WHBlocksEnvironment.cementTile4
        || block == WHBlocksEnvironment.cementVent){
            return WHBlocksEnvironment.darkRock;
        }
        return block;
    }

    private boolean isRadiationFloor(Block block){
        return block == WHBlocksEnvironment.radiationSand
        || block == WHBlocksEnvironment.radiationRockFloor
        || block == WHBlocksEnvironment.radiationCraters;
    }

    private boolean isRadiationScatterHost(Block block){
        return block == WHBlocksEnvironment.mineralSand
        || block == WHBlocksEnvironment.mineralSandstone
        || block == WHBlocksEnvironment.quartzSand
        || block == Blocks.stone
        || block == Blocks.shale
        || block == Blocks.craters
        || block == Blocks.ferricStone
        || block == Blocks.ferricCraters;
    }

    private float nearestEnemyCoreDistance(Vec3 position, Seq<Sector> sectors){
        float dst = 999f;
        if(sectors == null) return dst;

        Object[] sectorArray = sectors.items;
        int size = sectors.size;
        for(int i = 0; i < size; i++){
            Sector sector = (Sector)sectorArray[i];
            if(sector.hasEnemyBase()){
                dst = Math.min(dst, position.dst(sector.tile.v));
            }
        }
        return dst;
    }
}

