package wh.pipelinePlanet.karvex;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.ObjectMap;
import arc.util.noise.Simplex;
import mindustry.content.Blocks;
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

    public Block selectSurfaceBlock(int seed, Vec3 position) {
        return selectSurfaceBlock(seed, position, true);
    }

    public void sampleSurfaceColor(int seed, Vec3 position, Color out) {
        Block block = selectSurfaceBlock(seed, position, true);
        out.set(block.mapColor).a(1f - block.albedo);
    }

    public Block selectSurfaceBlock(int seed, Vec3 position, boolean allowLiquidEnrichment) {
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

        if (heat > 1.08f) {
            return Blocks.slag;
        } else if (heat > 1.00f) {
            return WHBlocksEnvironment.darkMagmaRock;
        } else if (heat > 0.93f) {
            return WHBlocksEnvironment.darkHotRock;
        } else if (heat > 0.86f) {
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
            if (field > 1.18f && temp > 0.46f && height > seaLevel + 0.03f)
                return WHBlocksEnvironment.oilMineralSandWater;
            if (field > 1.06f && temp > 0.42f && height > seaLevel + 0.02f) return WHBlocksEnvironment.oilMineralSand;
            if (field < -1.16f && temp > 0.56f) return WHBlocksEnvironment.promethiumSand;
            if (field < -1.04f && temp > 0.48f) return WHBlocksEnvironment.rustSand;
            if (field > 1.00f && temp < 0.38f && height > seaLevel + 0.02f) return WHBlocksEnvironment.apatiteCoarse;
            if(field > 0.62f) return WHBlocksEnvironment.mineralSandFloor;
            if (field > 0.46f) return WHBlocksEnvironment.darkMineralFloor;
            if (field < -0.70f) return WHBlocksEnvironment.darkMineralSandstone;
            if(field < -0.92f && temp < 0.55f && height > seaLevel + 0.01f) return WHBlocksEnvironment.oreSalt;
            if(field > 1.08f && temp < 0.44f && height > seaLevel + 0.02f) return WHBlocksEnvironment.quartzSand;
            return floor;
        }

        if(WHBlocksEnvironment.isMineralCoreFloor(floor)){
            float metalField = Simplex.noise3d(seed + 401, 3, 0.60f, 1f / 12f, px + 37f, py - 91f, pz)
                    + Simplex.noise3d(seed + 409, 2, 0.62f, 1f / 25f, px - 113f, py + 47f, pz) * 0.24f;
            float cobaltField = Simplex.noise3d(seed + 421, 3, 0.58f, 1f / 15f, px - 71f, py + 149f, pz)
                    + Simplex.noise3d(seed + 431, 2, 0.60f, 1f / 29f, px + 53f, py - 37f, pz) * 0.22f;

            if (cobaltField > 0.78f && temp > 0.46f) return WHBlocksEnvironment.cobaltFloor;
            if (metalField > 0.58f && temp > 0.30f) return WHBlocksEnvironment.manganeseFloor;
            if (metalField < -0.58f && temp < 0.78f) return WHBlocksEnvironment.chromiteFloor;

            if (field > 1.10f && temp > 0.48f) return WHBlocksEnvironment.oilMineralSand;
            if (field < -1.10f && temp > 0.56f) return WHBlocksEnvironment.promethiumSand;
            if(field > 0.90f) return WHBlocksEnvironment.mineralSand;
            if(field > 0.64f) return WHBlocksEnvironment.mineralSandFloor;
            if(field > 1.03f && temp < 0.40f && height > seaLevel + 0.04f) return WHBlocksEnvironment.quartzSand;
            if (field < -0.68f) return WHBlocksEnvironment.quartzSand;
            if (field < -0.50f) return WHBlocksEnvironment.gravel;
            if(field < -0.84f && temp < 0.55f) return WHBlocksEnvironment.cementFloor;
            return floor;
        }

        if (floor == WHBlocksEnvironment.oilMineralSand) {
            if (field > 0.94f && temp > 0.52f) return WHBlocksEnvironment.oilMineralSandWater;
            if (field < -0.78f) return WHBlocksEnvironment.mineralSand;
            return floor;
        }

        if (floor == WHBlocksEnvironment.promethiumSand) {
            if (field > 0.92f && temp > 0.62f) return WHBlocksEnvironment.promethium;
            if (field < -0.72f) return WHBlocksEnvironment.radiationSand;
            return floor;
        }

        if (floor == WHBlocksEnvironment.rustSand) {
            if (field > 1.02f) return WHBlocksEnvironment.rustSandWater;
            if (field < -0.78f) return WHBlocksEnvironment.mineralSand;
            return floor;
        }

        if (floor == WHBlocksEnvironment.quartzSand) {
            if (field > 0.86f && temp < 0.42f) return WHBlocksEnvironment.apatiteCoarse;
            if (field < -0.78f) return WHBlocksEnvironment.mineralSand;
            return floor;
        }

        if (floor == WHBlocksEnvironment.apatiteCoarse) {
            if (field < -0.82f) return WHBlocksEnvironment.quartzSand;
            if (field > 1.02f && temp > 0.48f) return WHBlocksEnvironment.oilMineralSand;
            return floor;
        }

        if (floor == WHBlocksEnvironment.darkMineralSandstone) {
            if(field > 0.70f) return WHBlocksEnvironment.mineralSandFloor;
            if (field > 0.62f) return WHBlocksEnvironment.darkMineralFloor;
            if(field > 0.52f) return WHBlocksEnvironment.mineralSand;
            if(field < -0.60f && temp < 0.62f) return WHBlocksEnvironment.trachyte;
            return floor;
        }

        if(floor == WHBlocksEnvironment.darkRock){
            if (field > 1.10f && temp > 0.50f) return WHBlocksEnvironment.oreShale2;
            if (field > 1.02f && temp > 0.46f) return WHBlocksEnvironment.oreShale1;
            if (field > 0.96f && temp > 0.42f) return WHBlocksEnvironment.oreShale;
            if (field < -1.00f && temp > 0.60f) return WHBlocksEnvironment.darkDacite;
            if (field < -0.88f) return WHBlocksEnvironment.darkRockCraters;
            if(field < -0.68f && temp < 0.62f) return WHBlocksEnvironment.trachyte;
            return floor;
        }

        if (floor == WHBlocksEnvironment.darkRockCraters) {
            if (field > 0.82f) return WHBlocksEnvironment.darkRock;
            if (field < -0.82f) return WHBlocksEnvironment.darkDacite;
            return floor;
        }

        if (floor == WHBlocksEnvironment.darkDacite) {
            if (field > 0.78f) return WHBlocksEnvironment.darkRock;
            if (field < -0.84f) return WHBlocksEnvironment.trachyte;
            return floor;
        }

        if(floor == WHBlocksEnvironment.trachyte){
            if (field > 1.08f && temp > 0.48f) return WHBlocksEnvironment.oreShale1;
            if (field > 0.86f) return WHBlocksEnvironment.darkRock;
            return floor;
        }

        if (floor == WHBlocksEnvironment.scorchedEarth) {
            if (field > 0.86f) return WHBlocksEnvironment.scorchedEarthFloor;
            if (field < -0.82f) return WHBlocksEnvironment.scorchedStone;
            return floor;
        }

        if (floor == WHBlocksEnvironment.scorchedEarthFloor) {
            if (field > 0.78f) return WHBlocksEnvironment.scorchedStone;
            if (field < -0.72f) return WHBlocksEnvironment.trachyte;
            return floor;
        }

        if (floor == WHBlocksEnvironment.scorchedStone) {
            if (field < -0.76f) return WHBlocksEnvironment.darkHotRock;
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
        Block ewd = WHBlocksEnvironment.effluentDeep;
        Block ew = WHBlocksEnvironment.effluent;
        Block rsw = WHBlocksEnvironment.radiationSandWater;
        Block rs = WHBlocksEnvironment.radiationSand;
        Block rrf = WHBlocksEnvironment.radiationRockFloor;
        Block rrc = WHBlocksEnvironment.radiationCraters;

        Block ms = WHBlocksEnvironment.mineralSand;
        Block msf = WHBlocksEnvironment.mineralSandFloor;
        Block dmf = WHBlocksEnvironment.darkMineralFloor;
        Block mss = WHBlocksEnvironment.darkMineralSandstone;
        Block tr = WHBlocksEnvironment.trachyte;
        Block dr = WHBlocksEnvironment.darkRock;
        Block hr = WHBlocksEnvironment.darkHotRock;
        Block mr = WHBlocksEnvironment.darkMagmaRock;
        Block slag = Blocks.slag;

        return new Block[][]{
                {rwd, rw, rsw, rs, rs, ms, ms, ms, msf, dmf, tr, mss, tr},
                {ewd, ew, rsw, rs, ms, ms, ms, dmf, tr, tr, mss, tr, tr},
                {rsw, rs, rs, ms, ms, ms, msf, dmf, tr, tr, mss, tr, tr},
                {rs, rs, ms, ms, ms, msf, dmf, tr, tr, tr, mss, tr, tr},
                {rs, ms, ms, ms, dmf, msf, msf, tr, tr, mss, tr, tr, rrf},
                {rs, ms, ms, ms, msf, dmf, tr, tr, tr, mss, tr, rrf, rrf},
                {ms, ms, ms, ms, msf, msf, tr, tr, tr, tr, rrf, rrf, rrc},
                {ms, ms, ms, msf, msf, tr, tr, tr, tr, rrf, rrf, rrc, rrc},
                {ms, ms, msf, msf, tr, tr, tr, tr, mss, rrf, rrf, rrc, rrc},
                {ms, msf, msf, tr, tr, tr, tr, mss, mss, rrf, rrc, rrc, hr},
                {msf, msf, tr, tr, tr, tr, mss, mss, rrf, rrc, rrc, hr, mr},
                {msf, tr, tr, tr, mss, mss, mss, rrf, rrf, rrc, hr, mr, slag},
                {tr, tr, tr, mss, mss, mss, rrf, rrf, rrc, hr, mr, slag, slag}
        };
    }
}
