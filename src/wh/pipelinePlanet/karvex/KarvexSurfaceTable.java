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

public class KarvexSurfaceTable{
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

    public final Block[][] arr = {
    {WHBlocksEnvironment.effluentDeep, WHBlocksEnvironment.mineralSandEffluentWater, WHBlocksEnvironment.mineralSand, WHBlocksEnvironment.mineralSandstone, WHBlocksEnvironment.darkRock, WHBlocksEnvironment.manganeseStone, WHBlocksEnvironment.chromiteStone, Blocks.ferricStone, Blocks.redStone, Blocks.denseRedStone, Blocks.redIce, Blocks.redIce},
    {WHBlocksEnvironment.effluentDeep, WHBlocksEnvironment.effluent, WHBlocksEnvironment.mineralSandEffluentWater, WHBlocksEnvironment.mineralSand, WHBlocksEnvironment.mineralSandstone, WHBlocksEnvironment.darkRock, WHBlocksEnvironment.manganeseStone, WHBlocksEnvironment.chromiteStone, Blocks.carbonStone, Blocks.ferricStone, Blocks.redStone, Blocks.redIce},
    {WHBlocksEnvironment.effluent, WHBlocksEnvironment.mineralSandEffluentWater, WHBlocksEnvironment.mineralSand, WHBlocksEnvironment.mineralSandstone, WHBlocksEnvironment.quartzSand, Blocks.darksand, WHBlocksEnvironment.darkRock, WHBlocksEnvironment.chromiteStone, Blocks.carbonStone, Blocks.rhyolite, Blocks.ferricStone, WHBlocksEnvironment.scorchedEarth},
    {WHBlocksEnvironment.mineralSandEffluentWater, WHBlocksEnvironment.mineralSand, WHBlocksEnvironment.mineralSandstone, WHBlocksEnvironment.quartzSand, WHBlocksEnvironment.promethiumSand, Blocks.darksand, WHBlocksEnvironment.darkRock, Blocks.carbonStone, Blocks.rhyolite, Blocks.roughRhyolite, WHBlocksEnvironment.scorchedEarth, WHBlocksEnvironment.scorchedStone},
    {WHBlocksEnvironment.mineralSand, WHBlocksEnvironment.mineralSandstone, WHBlocksEnvironment.quartzSand, WHBlocksEnvironment.promethiumSand, WHBlocksEnvironment.darkRock, WHBlocksEnvironment.manganeseStone, WHBlocksEnvironment.chromiteStone, Blocks.carbonStone, Blocks.rhyolite, Blocks.roughRhyolite, WHBlocksEnvironment.darkHotRock, WHBlocksEnvironment.scorchedStone},
    {WHBlocksEnvironment.mineralSandstone, WHBlocksEnvironment.quartzSand, WHBlocksEnvironment.promethiumSand, WHBlocksEnvironment.darkRock, WHBlocksEnvironment.manganeseStone, WHBlocksEnvironment.chromiteStone, WHBlocksEnvironment.cobaltStone, Blocks.carbonStone, Blocks.rhyolite, WHBlocksEnvironment.darkHotRock, WHBlocksEnvironment.darkMagmaRock, Blocks.magmarock},
    {WHBlocksEnvironment.mineralSandstone, WHBlocksEnvironment.promethiumSand, WHBlocksEnvironment.darkRock, WHBlocksEnvironment.chromiteStone, WHBlocksEnvironment.cobaltStone, Blocks.carbonStone, Blocks.rhyolite, Blocks.roughRhyolite, WHBlocksEnvironment.darkHotRock, WHBlocksEnvironment.darkMagmaRock, Blocks.magmarock, Blocks.crystallineStone},
    {WHBlocksEnvironment.promethiumSand, WHBlocksEnvironment.darkRock, WHBlocksEnvironment.chromiteStone, WHBlocksEnvironment.cobaltStone, Blocks.carbonStone, Blocks.rhyolite, Blocks.roughRhyolite, WHBlocksEnvironment.darkHotRock, WHBlocksEnvironment.darkMagmaRock, Blocks.magmarock, Blocks.crystallineStone, Blocks.crystalFloor},
    {WHBlocksEnvironment.promethiumSand, WHBlocksEnvironment.darkRock, WHBlocksEnvironment.cobaltStone, Blocks.carbonStone, Blocks.rhyolite, Blocks.roughRhyolite, WHBlocksEnvironment.darkHotRock, WHBlocksEnvironment.darkMagmaRock, Blocks.magmarock, Blocks.crystallineStone, Blocks.crystalFloor, Blocks.crystalFloor}
    };

    public final ObjectMap<Block, Block> dec = new ObjectMap<Block, Block>(){{
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

    public final float water = 2.35f / arr[0].length;
    public final Vec3 basePos = new Vec3(0.9341721f, 0f, 0.3568221f);

    public float rawHeight(int seed, Vec3 position){
        float noise = Simplex.noise3d(seed, heightOctaves, heightPersistence, heightScale,
        position.x * scl,
        position.y * scl + heightYOffset,
        position.z * scl);
        float shaped = Mathf.pow(Mathf.clamp(noise * heightScl), heightPower);
        return (shaped + waterOffset) / (1f + waterOffset);
    }

    public Block pickSurfaceBlock(int seed, Vec3 position, Seq<Sector> sectors){
        return pickSurfaceBlock(seed, position, sectors, true);
    }

    public void pickSurfaceColor(int seed, Vec3 position, Seq<Sector> sectors, Color out){
        Block block = pickSurfaceBlock(seed, position, sectors, true);
        out.set(block.mapColor).a(1f - block.albedo);

        if(block.asFloor().isLiquid){
            out.lerp(block.mapColor, 0.34f);
            out.b = Mathf.clamp(out.b + 0.04f);
        }

        float gray = (out.r + out.g + out.b) / 3f;
        out.r = Mathf.lerp(out.r, gray, 0.10f);
        out.g = Mathf.lerp(out.g, gray, 0.16f);
        out.b = Mathf.lerp(out.b, gray, 0.22f);

        out.r = Mathf.clamp(out.r * 0.9f);
        out.g = Mathf.clamp(out.g * 0.9f);
        out.b = Mathf.clamp(out.b * 0.9f);
    }

    public Block pickSurfaceBlock(int seed, Vec3 position, Seq<Sector> sectors, boolean allowLiquidEnrichment){
        float height = Mathf.clamp(rawHeight(seed, position) * 1.04f);
        float temp = rawTemp(seed, position);

        float px = position.x * scl;
        float py = position.y * scl;
        float pz = position.z * scl;

        int tempIndex = Mathf.clamp((int)(temp * arr.length), 0, arr.length - 1);
        int heightIndex = Mathf.clamp((int)(height * arr[0].length), 0, arr[0].length - 1);

        Block result = sanitizeExcludedTerrain(arr[tempIndex][heightIndex]);
        result = applyMetalBelts(seed, px, py, pz, result);
        result = applyVolcanicBias(seed, px, py, pz, temp, result);
        result = applyRadiationScatter(seed, px, py, pz, height, result);
        result = applyPolarRedBand(seed, position, px, py, pz, temp, height, result);

        if(allowLiquidEnrichment){
            result = applyLiquidEnrichment(seed, position, px, py, pz, temp, height, result);
        }

        return applyCoreRing(seed, position, sectors, result);
    }

    public Block decorationFor(Block floor){
        return dec.get(floor, floor.asFloor().decoration);
    }

    private float rawTemp(int seed, Vec3 position){
        float px = position.x * scl;
        float py = position.y * scl;
        float pz = position.z * scl;

        float base = 1f - Math.abs(position.y);
        float tempNoise = Simplex.noise3d(seed + 31, tempOctaves, tempPersistence, tempScale, px, py + 851f, pz) * 0.19f;
        float heatRift = Simplex.noise3d(seed + 47, 3, 0.57f, 1f / 5.8f, px, py + 203f, pz) * 0.14f;
        return Mathf.clamp(base + tempNoise + heatRift);
    }

    private float pollution(int seed, Vec3 position, float px, float py, float pz, float temp, float height){
        float noisy = Simplex.noise3d(seed + 79, pollutionOctaves, pollutionPersistence, pollutionScale, px, py + 999f, pz) * 0.58f;
        float polar = Math.abs(position.y) * 0.22f;
        float lowland = (water - height) * 1.8f;
        float heat = (temp - 0.55f) * 0.28f;
        return noisy + polar + lowland + heat;
    }

    private Block applyLiquidEnrichment(int seed, Vec3 position, float px, float py, float pz, float temp, float height, Block block){
        float polluted = pollution(seed, position, px, py, pz, temp, height);
        float radField = Simplex.noise3d(seed + 109, 3, 0.58f, 1f / 4.6f, px, py + 731f, pz);
        float basinMask = Simplex.noise3d(seed + 113, 2, 0.60f, 1f / 7.8f, px, py + 417f, pz);

        if(height <= water + 0.016f){
            boolean deep = height < water - 0.020f;

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
            if(prom > 0.87f && height <= water + 0.05f){
                return WHBlocksEnvironment.promethium;
            }
        }

        return block;
    }

    private Block applyMetalBelts(int seed, float px, float py, float pz, Block block){
        if(block == null || !block.asFloor().hasSurface() || block.asFloor().isLiquid) return block;
        if(block == Blocks.redIce || block == Blocks.redStone || block == Blocks.denseRedStone || block == Blocks.redmat) return block;
        if(isRadiationFloor(block)) return block;

        float belt = Simplex.noise3d(seed + 149, 3, 0.57f, 1f / 6.1f, px, py + 487f, pz);
        if(belt <= 0.61f) return block;

        float local = Simplex.noise3d(seed + 151, 2, 0.59f, 1f / 3.0f, px, py + 73f, pz);
        if(local > 0.78f){
            return WHBlocksEnvironment.cobaltStone;
        }else if(local > 0.46f){
            return WHBlocksEnvironment.chromiteStone;
        }else if(local > 0.12f){
            return WHBlocksEnvironment.manganeseStone;
        }else if(block == Blocks.shale){
            return WHBlocksEnvironment.darkRock;
        }

        return block;
    }

    private Block applyVolcanicBias(int seed, float px, float py, float pz, float temp, Block block){
        if(block == null || !block.asFloor().hasSurface() || block.asFloor().isLiquid) return block;
        if(temp < 0.66f) return block;

        float field = Simplex.noise3d(seed + 171, 3, 0.58f, 1f / 3.0f, px, py + 187f, pz)
        + Simplex.noise3d(seed + 173, 2, 0.56f, 1f / 7.8f, px, py + 61f, pz) * 0.2f
        + (temp - 0.66f) * 0.36f;

        if(field > 0.80f) return WHBlocksEnvironment.darkMagmaRock;
        if(field > 0.66f) return WHBlocksEnvironment.darkHotRock;
        if(field > 0.56f && block != Blocks.redIce) return WHBlocksEnvironment.scorchedStone;
        if(field > 0.50f && block != Blocks.redIce) return WHBlocksEnvironment.scorchedEarth;

        return block;
    }

    private Block applyRadiationScatter(int seed, float px, float py, float pz, float height, Block block){
        if(block == null || !block.asFloor().hasSurface() || block.asFloor().isLiquid) return block;
        if(block == Blocks.redmat || block == Blocks.redStone || block == Blocks.denseRedStone || block == Blocks.redIce) return block;
        if(block == WHBlocksEnvironment.chromiteStone || block == WHBlocksEnvironment.manganeseStone || block == WHBlocksEnvironment.cobaltStone) return block;

        float mask = Simplex.noise3d(seed + 199, 2, 0.57f, 1f / 20.5f, px, py + 173f, pz);
        if(mask < 0.82f) return block;

        float fieldA = Simplex.noise3d(seed + 193, 3, 0.58f, 1f / 6.5f, px, py + 333f, pz);
        float fieldB = Simplex.noise3d(seed + 197, 2, 0.56f, 1f / 13.2f, px, py + 919f, pz);
        float field = fieldA * 0.7f + fieldB * 0.3f + (height - 0.52f) * 0.08f;

        if(field > 0.90f) return WHBlocksEnvironment.radiationRockFloor;
        if(field > 0.84f) return WHBlocksEnvironment.radiationCraters;
        if(field > 0.79f) return WHBlocksEnvironment.radiationSand;

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

    private Block applyCoreRing(int seed, Vec3 position, Seq<Sector> sectors, Block block){
        if(!position.within(basePos, 0.65f)) return block;

        float dst = nearestEnemyBaseDistance(position, sectors);
        float freq = 0.05f;
        float ring = dst * 0.85f
        + Simplex.noise3d(seed, 3, 0.4f, 5.5f, position.x, position.y + 200f, position.z) * 0.015f
        + ((basePos.dst(position) % freq) < freq / 2f ? 1f : 0f) * 0.07f;

        if(ring < 0.15f){
            float freq2 = 0.07f;
            return ((basePos.dst(position) + 0.01f) % freq2 < freq2 * 0.65f) ? Blocks.metalFloor : Blocks.darkPanel6;
        }

        return block;
    }

    private Block sanitizeExcludedTerrain(Block block){
        if(block == WHBlocksEnvironment.parasiticTrachyte
        || block == WHBlocksEnvironment.cementFloor
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

    private float nearestEnemyBaseDistance(Vec3 position, Seq<Sector> sectors){
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
