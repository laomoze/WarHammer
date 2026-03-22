/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  arc.Core
 *  arc.files.Fi
 *  arc.graphics.Color
 *  arc.math.Mathf
 *  arc.math.geom.Vec3
 *  arc.struct.Seq
 *  arc.util.Nullable
 *  arc.util.noise.Ridged
 *  arc.util.noise.Simplex
 *  mindustry.Vars
 *  mindustry.content.Blocks
 *  mindustry.game.Schematic
 *  mindustry.game.Schematics
 *  mindustry.mod.Mods$LoadedMod
 *  mindustry.type.Sector
 *  mindustry.world.Block
 *  mindustry.world.TileGen
 */
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
import wh.pipelinePlanet.karvex.KarvexFinalizePass;
import wh.pipelinePlanet.karvex.KarvexHazardBasinPass;
import wh.pipelinePlanet.karvex.KarvexHydrologyPass;
import wh.pipelinePlanet.karvex.KarvexMapValidationPass;
import wh.pipelinePlanet.karvex.KarvexOreBalancePass;
import wh.pipelinePlanet.karvex.KarvexSurfaceProfile;
import wh.pipelinePlanet.karvex.KarvexTerrainRefinePass;
import wh.pipelinePlanet.karvex.KarvexVentPass;
import wh.pipelinePlanet.passes.BaseTerrainPass;
import wh.pipelinePlanet.passes.ConnectivityPass;
import wh.pipelinePlanet.passes.DistortPass;
import wh.pipelinePlanet.passes.ErekirWallPass;
import wh.pipelinePlanet.passes.GameplayFixPass;
import wh.pipelinePlanet.passes.RoomPlacementPass;
import wh.pipelinePlanet.passes.TechGridPass;
import wh.pipelinePlanet.passes.tile.KarvexDecorationTilePass;

public class KarvexPlanetGenerator
extends PipelinePlanetGenerator {
    private static final int FIXED_SECTOR_SIZE = 500;
    private static final float CHUNK_SCALE_MIN = 450.0f;
    private static final float CHUNK_SCALE_MAX = 1200.0f;
    private static final float CHUNK_SCALE_SEED_MAX = 2000.0f;
    private static final String S_CORE_SCHEMATIC_PATH = "assets/schematics/s-core.msch";
    private static final String S_CORE_SCHEMATIC_PATH_FALLBACK = "schematics/s-core.msch";
    private static final String LEGACY_DEFAULT_LOADOUT_BASE64 = "bXNjaAF4nGNgYWABorzE3FQGtmLd5PyiVAau5Py8ktS8Et/EAgam6loG7pTU4uSizIKSzPw8BgYGtpzEpNScYgbW6PcLl8cyMnCWZ+hCdTIwMIIQkAAAE/MWnA==";
    protected final KarvexSurfaceProfile surfaceProfile = new KarvexSurfaceProfile();
    protected final Seq<Sector> emptySectors = new Seq();

    public KarvexPlanetGenerator() {
        this.configureGenerationDefaults();
        this.baseSeed = 2;
        this.defaultLoadout = this.loadFrontlineCoreLoadout();
    }

    private void configureGenerationDefaults() {
        this.config.minRooms = 1;
        this.config.maxRooms = 2;
        this.config.enemyRoomScale = 1.0f;
        this.config.enableLakes = true;
        this.config.enableTechGrid = true;
        this.config.techGridCellSize = 20;
        this.config.techGridThresholdA = 0.63f;
        this.config.techGridThresholdB = 0.6f;
        this.config.techGridWallChance = 0.7f;
        this.config.techGridInnerOffset = 2.0f;
        this.config.enablePassTimingLog = false;
    }

    @Override
    protected void configurePipeline(PassRunner runner) {
        TilePassStage surfaceStage = this.createDecorationStage();
        this.addTerrainAndLayoutPasses(runner).add(new ErekirWallPass()).add(surfaceStage).add(new KarvexOreBalancePass()).add(new KarvexFinalizePass(this.defaultLoadout));
    }

    private TilePassStage createDecorationStage() {
        return new TilePassStage("KarvexDecorationStage").add(new KarvexDecorationTilePass(this.surfaceProfile));
    }

    private PassRunner addTerrainAndLayoutPasses(PassRunner runner) {
        return runner.add(new BaseTerrainPass()).add(new RoomPlacementPass()).add(new ConnectivityPass()).add(new DistortPass(16.0f, 5.0f)).add(new DistortPass(10.0f, 3.0f)).add(new KarvexHydrologyPass()).add(new KarvexHazardBasinPass()).add(new KarvexTerrainRefinePass()).add(new TechGridPass()).add(new GameplayFixPass()).add(new KarvexVentPass()).add(new KarvexMapValidationPass());
    }

    private Schematic loadFrontlineCoreLoadout() {
        Schematic fromFile = this.tryReadModSchematicFile();
        if (fromFile != null) {
            return fromFile;
        }
        return Schematics.readBase64((String)LEGACY_DEFAULT_LOADOUT_BASE64);
    }

    private Schematic tryReadModSchematicFile() {
        try {
            if (Vars.mods == null) {
                return null;
            }
            Mods.LoadedMod mod = Vars.mods.getMod(WarHammerMod.class);
            if (mod == null || mod.root == null) {
                return null;
            }
            Schematic primary = this.readSchematicIfExists(mod.root.child(S_CORE_SCHEMATIC_PATH));
            if (primary != null) {
                return primary;
            }
            return this.readSchematicIfExists(mod.root.child(S_CORE_SCHEMATIC_PATH_FALLBACK));
        }
        catch (Throwable throwable) {
            return null;
        }
    }

    @Nullable
    private Schematic readSchematicIfExists(@Nullable Fi file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            return Schematics.read((Fi)file);
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    public void onSectorCaptured(Sector sector) {
        sector.planet.reloadMeshAsync();
    }

    public void onSectorLost(Sector sector) {
        sector.planet.reloadMeshAsync();
    }

    public void beforeSaveWrite(Sector sector) {
        sector.planet.reloadMeshAsync();
    }

    public boolean isEmissive() {
        return true;
    }

    public boolean allowNumberedLaunch(Sector s) {
        return s.hasBase() && !s.isAttacked() && (this.hasLargeSavedCore(s) || this.hasLargeLiveCore(s));
    }

    private boolean hasLargeSavedCore(Sector sector) {
        return sector.info.bestCoreType != null && sector.info.bestCoreType.size >= 4;
    }

    private boolean hasLargeLiveCore(Sector sector) {
        return sector.isBeingPlayed() && Vars.state != null && Vars.state.rules != null && Vars.state.rules.defaultTeam != null && Vars.state.rules.defaultTeam.cores().contains(b -> b.block.size >= 4);
    }

    public boolean allowLanding(Sector sector) {
        return sector.planet.allowLaunchToNumbered && (sector.hasBase() || sector.near().contains(this::allowNumberedLaunch));
    }

    @Nullable
    public Sector findLaunchCandidate(Sector destination, @Nullable Sector selected) {
        if (destination.preset == null || !destination.preset.requireUnlock) {
            if (selected != null && selected.isNear(destination) && this.allowNumberedLaunch(selected)) {
                return selected;
            }
            return (Sector)destination.near().find(this::allowNumberedLaunch);
        }
        return super.findLaunchCandidate(destination, selected);
    }

    public void getLockedText(Sector hovered, StringBuilder out) {
        if ((hovered.preset == null || !hovered.preset.requireUnlock) && hovered.near().contains(Sector::hasBase)) {
            out.append("[red]").append('\ue815').append("[]").append(Blocks.coreFoundation.emoji()).append(Core.bundle.get("sector.foundationrequired"));
        } else {
            super.getLockedText(hovered, out);
        }
    }

    public float getHeight(Vec3 position) {
        float height = this.surfaceProfile.sampleRawHeight(this.seed, position);
        return Math.max(height, this.surfaceProfile.seaLevel);
    }

    public void getColor(Vec3 position, Color out) {
        this.surfaceProfile.sampleSurfaceColor(this.seed, position, this.renderSectors(), out);
    }

    public void getEmissiveColor(Vec3 position, Color out) {
        Seq<Sector> sectors = this.renderSectors();
        Block block = this.surfaceProfile.selectSurfaceBlock(this.seed, position, sectors, true);
        float pulse = 0.78f + Simplex.noise3d((int)(this.seed + 77), (double)1.0, (double)1.0, (double)8.2f, (double)position.x, (double)(position.y + 41.0f), (double)position.z) * 0.22f;
        out.set(0.0f, 0.0f, 0.0f, 0.0f);
        if (this.isRadiationLiquid(block)) {
            out.set(0.25f, 0.78f, 0.92f, 1.0f).mul(0.36f * pulse);
        } else if (block == WHBlocksEnvironment.promethium) {
            out.set(0.94f, 0.63f, 0.3f, 1.0f).mul(0.24f * pulse);
        } else if (this.isPollutedLiquid(block)) {
            out.set(0.3f, 0.58f, 0.82f, 1.0f).mul(0.18f * pulse);
        }
    }

    private boolean isRadiationLiquid(Block block) {
        return block == WHBlocksEnvironment.radiationWater || block == WHBlocksEnvironment.radiationWaterDeep || block == WHBlocksEnvironment.radiationSandWater || block == WHBlocksEnvironment.mineralSandRadiationWater;
    }

    private boolean isPollutedLiquid(Block block) {
        return block == WHBlocksEnvironment.effluent || block == WHBlocksEnvironment.effluentDeep || block == WHBlocksEnvironment.mineralSandEffluentWater || block == Blocks.darksandTaintedWater || block == Blocks.deepTaintedWater;
    }

    @Override
    protected void genTile(Vec3 position, TileGen tile) {
        tile.floor = this.surfaceProfile.selectSurfaceBlock(this.seed, position, this.renderSectors(), false);
        tile.overlay = Blocks.air;
        Block wall = tile.floor.asFloor().wall;
        Block block = tile.block = wall == null ? Blocks.air : wall;
        if (Ridged.noise3d((int)(this.seed + 1), (double)position.x, (double)position.y, (double)position.z, (int)2, (float)14.0f) > 0.13f) {
            tile.block = Blocks.air;
        }
    }

    protected Seq<Sector> renderSectors() {
        if (this.sector != null && this.sector.planet != null) {
            return this.sector.planet.sectors;
        }
        return this.emptySectors;
    }

    public int getSectorSize(Sector sector) {
        return 500;
    }

    static float seedDrivenChunkScale(int seed) {
        float normalized = Mathf.clamp((float)((float)Math.max(seed, 0) / 2000.0f));
        return Mathf.lerp((float)450.0f, (float)1200.0f, (float)normalized);
    }
}
