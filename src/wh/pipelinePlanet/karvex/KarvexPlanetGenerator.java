package wh.pipelinePlanet.karvex;

import arc.Core;
import arc.files.Fi;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Vec3;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.noise.Ridged;
import arc.util.noise.Simplex;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.game.Schematics;
import mindustry.mod.Mods;
import mindustry.type.Sector;
import mindustry.world.Block;
import mindustry.world.TileGen;
import wh.content.WHBlocksEnvironment;
import wh.core.WarHammerMod;
import wh.pipelinePlanet.core.PassRunner;
import wh.pipelinePlanet.core.PipelinePlanetGenerator;
import wh.pipelinePlanet.core.TilePassStage;
import wh.pipelinePlanet.passes.*;

public class KarvexPlanetGenerator extends PipelinePlanetGenerator{
    private static final float CHUNK_SCALE_MIN = 450f;
    private static final float CHUNK_SCALE_MAX = 1200f;
    private static final float CHUNK_SCALE_SEED_MAX = 2000f;

    private static final String S_CORE_SCHEMATIC_PATH = "assets/schematics/s-core.msch";
    private static final String S_CORE_SCHEMATIC_PATH_FALLBACK = "schematics/s-core.msch";
    private static final String LEGACY_DEFAULT_LOADOUT_BASE64 = "bXNjaAF4nGNgYWABorzE3FQGtmLd5PyiVAau5Py8ktS8Et/EAgam6loG7pTU4uSizIKSzPw8BgYGtpzEpNScYgbW6PcLl8cyMnCWZ+hCdTIwMIIQkAAAE/MWnA==";

    protected final KarvexSurfaceProfile surfaceProfile = new KarvexSurfaceProfile();
    protected final Seq<Sector> emptySectors = new Seq<>();

    public KarvexPlanetGenerator(){
        configureGenerationDefaults();
        baseSeed = 2;
        defaultLoadout = loadFrontlineCoreLoadout();
    }

    private void configureGenerationDefaults(){
        config.minRooms = 1;
        config.maxRooms = 2;
        config.enemyRoomScale = 1f;

        config.enableLakes = false;
        config.enableTechGrid = true;
        config.techGridCellSize = 20;
        config.techGridThresholdA = 0.63f;
        config.techGridThresholdB = 0.6f;
        config.techGridWallChance = 0.7f;
        config.techGridInnerOffset = 2f;
        config.enablePassTimingLog = false;
    }

    @Override
    protected void configurePipeline(PassRunner runner){
        TilePassStage decoration = new TilePassStage("KarvexDecorationStage")
        .add(new KarvexDecorationTilePass(surfaceProfile));

        runner
        .add(new BaseTerrainPass())
        .add(new CellularCavesPass(4))
        .add(new DistortPass(10f, 12f))
        .add(new RoomPlacementPass())
        .add(new ConnectivityPass())
        .add(new DistortPass(10f, 6f))
        .add(new KarvexHazardBasinPass())
        .add(new KarvexHydrologyPass())
        .add(new GameplayFixPass())
        .add(new KarvexVentPass())
        .add(new KarvexMapValidationPass())
                .add(new TechGridPass())
        .add(decoration)
        .add(new KarvexFinalizePass(defaultLoadout));
    }

    private Schematic loadFrontlineCoreLoadout(){
        Schematic fromFile = tryReadModSchematicFile();
        if(fromFile != null) return fromFile;
        return Schematics.readBase64(LEGACY_DEFAULT_LOADOUT_BASE64);
    }

    private Schematic tryReadModSchematicFile(){
        try{
            if(Vars.mods == null) return null;
            Mods.LoadedMod mod = Vars.mods.getMod(WarHammerMod.class);
            if(mod == null || mod.root == null) return null;

            Schematic primary = readSchematicIfExists(mod.root.child(S_CORE_SCHEMATIC_PATH));
            if(primary != null) return primary;
            return readSchematicIfExists(mod.root.child(S_CORE_SCHEMATIC_PATH_FALLBACK));
        }catch(Throwable ignored){
            return null;
        }
    }

    @Nullable
    private Schematic readSchematicIfExists(@Nullable Fi file){
        if(file == null || !file.exists()) return null;
        try{
            return Schematics.read(file);
        }catch(Throwable ignored){
            return null;
        }
    }

    @Override
    public void onSectorCaptured(Sector sector){
        sector.planet.reloadMeshAsync();
    }

    @Override
    public void onSectorLost(Sector sector){
        sector.planet.reloadMeshAsync();
    }

    @Override
    public void beforeSaveWrite(Sector sector){
        sector.planet.reloadMeshAsync();
    }

    @Override
    public boolean isEmissive(){
        return true;
    }

    public boolean allowNumberedLaunch(Sector s){
        return s.hasBase() && !s.isAttacked() && (hasLargeSavedCore(s) || hasLargeLiveCore(s));
    }

    private boolean hasLargeSavedCore(Sector sector){
        return sector.info.bestCoreType != null && sector.info.bestCoreType.size >= 4;
    }

    private boolean hasLargeLiveCore(Sector sector){
        return sector.isBeingPlayed()
        && Vars.state != null
        && Vars.state.rules != null
        && Vars.state.rules.defaultTeam != null
        && Vars.state.rules.defaultTeam.cores().contains(b -> b.block.size >= 4);
    }

    @Override
    public boolean allowLanding(Sector sector){
        return sector.planet.allowLaunchToNumbered && (sector.hasBase() || sector.near().contains(this::allowNumberedLaunch));
    }

    @Override
    public @Nullable Sector findLaunchCandidate(Sector destination, @Nullable Sector selected){
        if(destination.preset == null || !destination.preset.requireUnlock){
            if(selected != null && selected.isNear(destination) && allowNumberedLaunch(selected)){
                return selected;
            }
            return destination.near().find(this::allowNumberedLaunch);
        }
        return super.findLaunchCandidate(destination, selected);
    }

    @Override
    public void getLockedText(Sector hovered, StringBuilder out){
        if((hovered.preset == null || !hovered.preset.requireUnlock) && hovered.near().contains(Sector::hasBase)){
            out.append("[red]").append('\ue815').append("[]").append(Blocks.coreFoundation.emoji()).append(Core.bundle.get("sector.foundationrequired"));
        }else{
            super.getLockedText(hovered, out);
        }
    }

    @Override
    public float getHeight(Vec3 position){
        float height = surfaceProfile.sampleRawHeight(seed, position);
        return Math.max(height, surfaceProfile.seaLevel);
    }

    @Override
    public void getColor(Vec3 position, Color out){
        surfaceProfile.sampleSurfaceColor(seed, position, renderSectors(), out);
    }

    @Override
    public void getEmissiveColor(Vec3 position, Color out){
        Block block = surfaceProfile.selectSurfaceBlock(seed, position, renderSectors(), true);
        float pulse = 0.78f + Simplex.noise3d(seed + 77, 1, 1f, 8.2f, position.x, position.y + 41f, position.z) * 0.22f;

        out.set(0f, 0f, 0f, 0f);
        if(isRadiationLiquid(block)){
            out.set(0.25f, 0.78f, 0.92f, 1f).mul(0.34f * pulse);
        }else if(block == WHBlocksEnvironment.promethium){
            out.set(0.94f, 0.63f, 0.30f, 1f).mul(0.24f * pulse);
        }else if(isPollutedLiquid(block)){
            out.set(0.30f, 0.58f, 0.82f, 1f).mul(0.19f * pulse);
        }else if(block == Blocks.slag){
            out.set(0.95f, 0.44f, 0.16f, 1f).mul(0.16f * pulse);
        }
    }

    @Override
    protected void genTile(Vec3 position, TileGen tile){
        tile.floor = surfaceProfile.selectSurfaceBlock(seed, position, renderSectors(), false);
        tile.overlay = Blocks.air;

        Block wall = resolveWall(tile.floor);
        tile.block = wall == null ? Blocks.air : wall;

        if(Ridged.noise3d(seed + 1, position.x, position.y, position.z, 2, 22f) > 0.18f){
            tile.block = Blocks.air;
        }
    }

    private Block resolveWall(Block floor){
        if(floor == null || floor.asFloor() == null) return Blocks.air;

        Block wall = floor.asFloor().wall;
        if(wall != null && wall != Blocks.air) return wall;

        if(WHBlocksEnvironment.isMineralCoreFloor(floor)
        || floor == WHBlocksEnvironment.quartzSand
                || floor == WHBlocksEnvironment.quartzSand
        || floor == WHBlocksEnvironment.cementFloor
        || floor == WHBlocksEnvironment.mineralSand
                || floor == WHBlocksEnvironment.darkMineralSandstone
        || floor == WHBlocksEnvironment.gravel
        || floor == WHBlocksEnvironment.oreSalt){
            return Blocks.air;
        }

        if(floor == WHBlocksEnvironment.trachyte) return WHBlocksEnvironment.trachyteWall;
        if(floor == WHBlocksEnvironment.oreShale) return WHBlocksEnvironment.oreShaleWall;
        if(floor == WHBlocksEnvironment.manganeseFloor) return WHBlocksEnvironment.manganeseWall;
        if(floor == WHBlocksEnvironment.chromiteFloor || floor == WHBlocksEnvironment.chromiteFloorDark) return WHBlocksEnvironment.chromiteWall;
        if(floor == WHBlocksEnvironment.cobaltFloor) return WHBlocksEnvironment.cobaltWall;
        if(floor == WHBlocksEnvironment.radiationSand || floor == WHBlocksEnvironment.radiationRockFloor || floor == WHBlocksEnvironment.radiationCraters){
            return WHBlocksEnvironment.radiationRockWall;
        }
        if(floor == WHBlocksEnvironment.scorchedEarth || floor == WHBlocksEnvironment.scorchedStone){
            return WHBlocksEnvironment.scorchedEarthWall;
        }
        if(floor == WHBlocksEnvironment.darkRock || floor == WHBlocksEnvironment.darkHotRock || floor == WHBlocksEnvironment.darkMagmaRock){
            return WHBlocksEnvironment.darkRockWall;
        }
        return Blocks.air;
    }

    protected Seq<Sector> renderSectors(){
        if(sector != null && sector.planet != null) return sector.planet.sectors;
        return emptySectors;
    }

    @Override
    public int getSectorSize(Sector sector){
        return super.getSectorSize(sector);
    }

    static float seedDrivenChunkScale(int seed){
        float normalized = Mathf.clamp(Math.max(seed, 0f) / CHUNK_SCALE_SEED_MAX);
        return Mathf.lerp(CHUNK_SCALE_MIN, CHUNK_SCALE_MAX, normalized);
    }

    private boolean isRadiationLiquid(Block block){
        return block == WHBlocksEnvironment.radiationWater
        || block == WHBlocksEnvironment.radiationWaterDeep
        || block == WHBlocksEnvironment.radiationSandWater
        || block == WHBlocksEnvironment.mineralSandRadiationWater;
    }

    private boolean isPollutedLiquid(Block block){
        return block == WHBlocksEnvironment.effluent
        || block == WHBlocksEnvironment.effluentDeep
        || block == WHBlocksEnvironment.mineralSandEffluentWater;
    }
}
