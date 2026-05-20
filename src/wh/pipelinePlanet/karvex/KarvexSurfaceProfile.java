package wh.pipelinePlanet.karvex;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.noise.Simplex;
import mindustry.type.Sector;
import mindustry.world.Block;
import wh.content.WHBlocksEnvironment;

/**
 * Serpulo-equivalent terrain logic using mod floors.
 */
public class KarvexSurfaceProfile{
    public final float heightYOffset = 42.7f;
    public final float scl = 5f;
    public final float waterOffset = 0.04f;
    public final float heightScl = 1.01f;

    public final Block[][] arr = createSurfaceLut();

    public final ObjectMap<Block, Block> decorationByFloor = new ObjectMap<Block, Block>(){{
        put(WHBlocksEnvironment.quartzSand, WHBlocksEnvironment.quartzSandBoulder);
        put(WHBlocksEnvironment.cementFloor, WHBlocksEnvironment.quartzSandBoulder);
        put(WHBlocksEnvironment.gravel, WHBlocksEnvironment.darkMineralSandBoulder);
        put(WHBlocksEnvironment.darkMineralFloor, WHBlocksEnvironment.darkMineralSandBoulder);
        put(WHBlocksEnvironment.mineralSandFloor, WHBlocksEnvironment.mineralSandFloorBoulder);
        put(WHBlocksEnvironment.mineralSand, WHBlocksEnvironment.darkMineralSandBoulder);
        put(WHBlocksEnvironment.darkMineralSandstone, WHBlocksEnvironment.darkMineralSandBoulder);
        put(WHBlocksEnvironment.scorchedEarth, WHBlocksEnvironment.scorchedEarthBoulder);
        put(WHBlocksEnvironment.scorchedStone, WHBlocksEnvironment.scorchedEarthBoulder);
        put(WHBlocksEnvironment.quartzSand, WHBlocksEnvironment.quartzCrystalCluster);
        put(WHBlocksEnvironment.darkRock, WHBlocksEnvironment.darkRockBoulder);
        put(WHBlocksEnvironment.darkHotRock, WHBlocksEnvironment.darkRockBoulder);
        put(WHBlocksEnvironment.darkMagmaRock, WHBlocksEnvironment.darkRockBoulder);
        put(WHBlocksEnvironment.manganeseFloor, WHBlocksEnvironment.manganeseBoulder);
        put(WHBlocksEnvironment.manganeseStone, WHBlocksEnvironment.manganeseBoulder);
        put(WHBlocksEnvironment.chromiteFloor, WHBlocksEnvironment.chromiteBoulder);
        put(WHBlocksEnvironment.chromiteFloorDark, WHBlocksEnvironment.chromiteBoulder);
        put(WHBlocksEnvironment.chromiteStone, WHBlocksEnvironment.chromiteBoulder);
        put(WHBlocksEnvironment.cobaltFloor, WHBlocksEnvironment.cobaltBoulder);
        put(WHBlocksEnvironment.cobaltStone, WHBlocksEnvironment.cobaltBoulder);
        put(WHBlocksEnvironment.radiationSand, WHBlocksEnvironment.radiationBoulder);
        put(WHBlocksEnvironment.radiationRockFloor, WHBlocksEnvironment.radiationBoulder);
        put(WHBlocksEnvironment.radiationCraters, WHBlocksEnvironment.radiationBoulder);
    }};

    public final ObjectMap<Block, Block> tars = ObjectMap.of(
    WHBlocksEnvironment.trachyte, WHBlocksEnvironment.oreShale,
    WHBlocksEnvironment.darkRock, WHBlocksEnvironment.oreShale
    );

    public final float seaLevel = 2f / arr[0].length;

    public float sampleRawHeight(int seed, Vec3 position){
        return (Mathf.pow(Simplex.noise3d(seed, 7, 0.5f, 1f / 3f,
        position.x * scl,
        position.y * scl + heightYOffset,
        position.z * scl) * heightScl, 2.3f) + waterOffset) / (1f + waterOffset);
    }

    public Block selectSurfaceBlock(int seed, Vec3 position, Seq<Sector> sectors){
        return selectSurfaceBlock(seed, position, sectors, true);
    }

    public void sampleSurfaceColor(int seed, Vec3 position, Seq<Sector> sectors, Color out){
        Block block = selectSurfaceBlock(seed, position, sectors, true);
        out.set(block.mapColor).a(1f - block.albedo);
    }

    public Block selectSurfaceBlock(int seed, Vec3 position, Seq<Sector> sectors, boolean allowLiquidEnrichment){
        float height = sampleRawHeight(seed, position);
        float px = position.x * scl;
        float py = position.y * scl;
        float pz = position.z * scl;

        // Break long diagonal banding by warping sampling coordinates before LUT lookup.
        float wx = px + Simplex.noise3d(seed + 301, 2, 0.62f, 1f / 2.8f, px, py + 211f, pz) * 0.55f;
        float wy = py + Simplex.noise3d(seed + 307, 2, 0.62f, 1f / 2.6f, px + 91f, py - 117f, pz) * 0.55f;
        float wz = pz + Simplex.noise3d(seed + 311, 2, 0.62f, 1f / 2.7f, px - 53f, py + 47f, pz) * 0.55f;

        float rad = scl;
        float temp = Mathf.clamp(Math.abs(wy * 2f) / rad);
        float tnoise = Simplex.noise3d(seed, 7, 0.56f, 1f / 3f, wx, wy + 999f - 0.1f, wz);
        temp = Mathf.lerp(temp, tnoise, 0.5f);

        height *= 1.2f;
        float bandBreak = Simplex.noise3d(seed + 317, 2, 0.67f, 1f / 7f, wx, wy + 333f, wz) * 0.08f
        + Simplex.noise3d(seed + 331, 1, 1f, 1f / 15f, wx + 17f, wy - 19f, wz) * 0.05f;
        temp = Mathf.clamp(temp + bandBreak);
        height = Mathf.clamp(height + bandBreak * 0.45f);

        float tar = Simplex.noise3d(seed, 4, 0.55f, 1f / 2f, wx, wy + 999f, wz) * 0.3f + position.dst(0f, 0f, 1f) * 0.2f;

        Block result = arr[
        Mathf.clamp((int)(temp * arr.length), 0, arr.length - 1)
        ][
        Mathf.clamp((int)(height * arr[0].length), 0, arr[0].length - 1)
        ];

        if(tar > 0.68f){
            result = tars.get(result, result);
        }

        result = applyDetailNoise(seed, wx, wy, wz, temp, height, result);

        if(allowLiquidEnrichment){
            result = applyHeatSlag(seed, wx, wy, wz, temp, result);
        }

        return applyCoastalRadiation(seed, wx, wy, wz, height, result);
    }

    public Block decorationForFloor(Block floor){
        return decorationByFloor.get(floor, floor.asFloor().decoration);
    }

    private Block applyHeatSlag(int seed, float px, float py, float pz, float temp, Block floor){
        if(floor == null || !floor.asFloor().hasSurface() || floor.asFloor().isLiquid) return floor;

        float heat = Simplex.noise3d(seed + 101, 3, 0.58f, 1f / 6f, px, py + 301f, pz) + (temp - 0.70f) * 0.5f;

        if(heat > 1.06f){
            return WHBlocksEnvironment.darkMagmaRock;
        }else if(heat > 0.98f){
            return WHBlocksEnvironment.darkHotRock;
        }else if(heat > 0.90f){
            return WHBlocksEnvironment.scorchedEarth;
        }
        return floor;
    }

    private Block applyDetailNoise(int seed, float px, float py, float pz, float temp, float height, Block floor){
        if(floor == null || !floor.asFloor().hasSurface() || floor.asFloor().isLiquid) return floor;

        float n1 = Simplex.noise3d(seed + 17, 3, 0.58f, 1f / 7f, px, py + 213f, pz);
        float n2 = Simplex.noise3d(seed + 19, 2, 0.62f, 1f / 17f, px, py + 617f, pz) * 0.25f;
        float n3 = Simplex.noise3d(seed + 23, 2, 0.60f, 1f / 33f, px, py + 901f, pz) * 0.30f;
        float field = n1 + n2 + n3;

        if(floor == WHBlocksEnvironment.mineralSand){
            if(field > 0.62f) return WHBlocksEnvironment.mineralSandFloor;
            if (field > 0.46f) return WHBlocksEnvironment.darkMineralFloor;
            if (field < -0.70f) return WHBlocksEnvironment.darkMineralSandstone;
            if(field < -0.92f && temp < 0.55f && height > seaLevel + 0.01f) return WHBlocksEnvironment.oreSalt;
            if(field > 1.08f && temp < 0.44f && height > seaLevel + 0.02f) return WHBlocksEnvironment.quartzSand;
            return floor;
        }

        if(WHBlocksEnvironment.isMineralCoreFloor(floor)){
            if(field > 0.90f) return WHBlocksEnvironment.mineralSand;
            if(field > 0.64f) return WHBlocksEnvironment.mineralSandFloor;
            if(field > 1.03f && temp < 0.40f && height > seaLevel + 0.04f) return WHBlocksEnvironment.quartzSand;
            if(field < -0.50f) return WHBlocksEnvironment.gravel;
            if (field < -0.68f) return WHBlocksEnvironment.quartzSand;
            if(field < -0.94f && temp < 0.50f && height > seaLevel + 0.01f) return WHBlocksEnvironment.oreSalt;
            if(field < -0.84f && temp < 0.55f) return WHBlocksEnvironment.cementFloor;
            return floor;
        }

        if (floor == WHBlocksEnvironment.darkMineralSandstone) {
            if(field > 0.70f) return WHBlocksEnvironment.mineralSandFloor;
            if (field > 0.62f) return WHBlocksEnvironment.darkMineralFloor;
            if(field > 0.52f) return WHBlocksEnvironment.mineralSand;
            if(field < -0.60f && temp < 0.62f) return WHBlocksEnvironment.trachyte;
            if(field < -0.74f && temp < 0.58f) return WHBlocksEnvironment.oreSalt;
            return floor;
        }

        if(floor == WHBlocksEnvironment.darkRock){
            if(field > 0.82f) return WHBlocksEnvironment.oreShale;
            if(field < -0.90f && temp < 0.52f) return WHBlocksEnvironment.oreSalt;
            if(field < -0.68f && temp < 0.62f) return WHBlocksEnvironment.trachyte;
            return floor;
        }

        if(floor == WHBlocksEnvironment.trachyte){
            if(field > 0.80f) return WHBlocksEnvironment.darkRock;
            if(field < -0.82f && temp < 0.56f) return WHBlocksEnvironment.oreSalt;
            return floor;
        }

        if(floor == WHBlocksEnvironment.manganeseFloor){
            if(field > 0.56f) return WHBlocksEnvironment.manganeseStone;
            if(field < -0.80f) return WHBlocksEnvironment.defaultMineralFloor();
        }
        if(floor == WHBlocksEnvironment.chromiteFloor){
            if(field > 0.48f) return WHBlocksEnvironment.chromiteFloorDark;
            if(field < -0.80f) return WHBlocksEnvironment.defaultMineralFloor();
        }
        if(floor == WHBlocksEnvironment.chromiteFloorDark){
            if(field > 0.72f) return WHBlocksEnvironment.chromiteStone;
            if(field < -0.70f) return WHBlocksEnvironment.chromiteFloor;
        }
        if(floor == WHBlocksEnvironment.cobaltFloor){
            if(field > 0.62f) return WHBlocksEnvironment.cobaltStone;
            if(field < -0.82f) return WHBlocksEnvironment.defaultMineralFloor();
        }
        if(floor == WHBlocksEnvironment.radiationSand && field > 0.78f && height > seaLevel + 0.02f) return WHBlocksEnvironment.radiationRockFloor;
        if(floor == WHBlocksEnvironment.radiationRockFloor && field < -0.78f) return WHBlocksEnvironment.radiationCraters;

        return floor;
    }

    private Block applyCoastalRadiation(int seed, float px, float py, float pz, float height, Block floor){
        if(floor == null || !floor.asFloor().hasSurface() || floor.asFloor().isLiquid) return floor;
        if(height > seaLevel + 0.09f) return floor;

        float coast = (seaLevel + 0.09f - height) / 0.09f;
        float field = Simplex.noise3d(seed + 111, 2, 0.62f, 1f / 11f, px, py + 411f, pz) + coast * 0.30f;

        if(field > 0.88f) return WHBlocksEnvironment.radiationRockFloor;
        if(field > 0.74f) return WHBlocksEnvironment.radiationSand;
        return floor;
    }

    private Block[][] createSurfaceLut(){
        Block rwd = WHBlocksEnvironment.radiationWaterDeep;
        Block rw = WHBlocksEnvironment.radiationWater;
        Block rsw = WHBlocksEnvironment.radiationSandWater;
        Block rs = WHBlocksEnvironment.radiationSand;
        Block rrf = WHBlocksEnvironment.radiationRockFloor;
        Block rrc = WHBlocksEnvironment.radiationCraters;

        Block ap = WHBlocksEnvironment.apatiteCoarse;
        Block ce = WHBlocksEnvironment.cementFloor;
        Block gv = WHBlocksEnvironment.gravel;

        Block mf = WHBlocksEnvironment.darkMineralFloor;
        Block ms = WHBlocksEnvironment.mineralSand;
        Block mss = WHBlocksEnvironment.darkMineralSandstone;

        Block os = WHBlocksEnvironment.oreSalt;
        Block tr = WHBlocksEnvironment.trachyte;
        Block dr = WHBlocksEnvironment.darkRock;
        Block sh = WHBlocksEnvironment.oreShale;

        Block mnf = WHBlocksEnvironment.manganeseFloor;
        Block mns = WHBlocksEnvironment.manganeseStone;
        Block chf = WHBlocksEnvironment.chromiteFloor;
        Block chd = WHBlocksEnvironment.chromiteFloorDark;
        Block chs = WHBlocksEnvironment.chromiteStone;
        Block cof = WHBlocksEnvironment.cobaltFloor;
        Block cos = WHBlocksEnvironment.cobaltStone;

        Block se = WHBlocksEnvironment.scorchedEarth;
        Block ss = WHBlocksEnvironment.scorchedStone;
        Block hr = WHBlocksEnvironment.darkHotRock;
        Block mr = WHBlocksEnvironment.darkMagmaRock;

        return new Block[][]{
        {rwd, rw, rsw, rs, rs, mf, mf, mf, ce, ap, gv, mss, tr},
        {rwd, rw, rsw, rs, rs, mf, mf, mf, ap, ce, gv, mss, tr},
        {rwd, rw, rsw, rs, rs, mf, mf, mf, mf, ms, os, tr, dr},
        {rw, rw, rsw, rs, rs, mf, mf, mf, ms, os, tr, dr, dr},
        {rw, rsw, rs, rs, mf, mf, mf, ms, os, ms, tr, dr, mnf},
        {rsw, rs, rs, rs, mf, mf, mf, ms, mss, tr, dr, mnf, chf},
        {rsw, rs, rs, mf, mf, mf, ms, mss, tr, dr, mnf, mns, chd},
        {rs, rs, mf, mf, mf, mf, ms, mss, tr, dr, mnf, chf, chd},
        {rs, mf, mf, mf, mf, ms, mss, tr, dr, sh, chf, chd, cof},
        {mf, mf, mf, mf, ms, os, tr, dr, sh, chf, chd, cof, cos},
        {mf, mf, mf, ms, mss, tr, dr, sh, chd, chf, chs, cof, se},
        {mf, mf, ms, mss, tr, dr, sh, chd, chf, chs, cof, cos, ss},
        {mf, ms, mss, tr, dr, sh, chf, chd, chs, cof, cos, rrf, hr}
        };
    }
}
