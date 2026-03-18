package wh.pipelinePlanet.karvex;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.type.*;
import mindustry.world.*;
import wh.content.*;
import wh.core.*;
import wh.pipelinePlanet.core.*;
import wh.pipelinePlanet.passes.*;
import wh.pipelinePlanet.passes.tile.*;

import static mindustry.Vars.mods;
import static mindustry.Vars.state;

public class KarvexPlanetGenerator extends PipelinePlanetGenerator{
    private static final int fixedSectorSize = 600;
    private static final String sCoreSchematicPath = "assets/schematics/s-core.msch";
    private static final String sCoreSchematicPathFallback = "schematics/s-core.msch";
    private static final String legacyDefaultLoadoutBase64 =
    "bXNjaAF4nGNgY2BjYWDJS8xNZWDL0U3OL0pl4ErOzytJzSvxTSxgYKquZeBOSS1OLsosKMnMz2NgACpLTErNKWZgjX6/cHksIwNneYYuVCcDAyMDAxMQMgAAEs8WlA==";

    protected final KarvexSurfaceTable surface = new KarvexSurfaceTable();
    protected final Seq<Sector> emptySectors = new Seq<>();

    public KarvexPlanetGenerator(){

        config.minRooms = 1;
        config.maxRooms = 2;
        config.enemyRoomScale = 1f;

        config.enableLakes = true;
        config.enableTechGrid = true;
        config.techGridCellSize = 24;
        config.techGridWallChance = 0.22f;

        baseSeed = 2;
        defaultLoadout = loadFrontlineCoreLoadout();
    }

    @Override
    protected void configurePipeline(PassRunner runner){
        TilePassStage surfaceStage = new TilePassStage("KarvexDecorationStage")
        .add(new KarvexDecorationTilePass(surface));

        addTerrainAndLayoutPasses(runner)
        .add(new TechGridPass())
        .add(surfaceStage)
        .add(new KarvexOreBalancePass())
        .add(new KarvexFinalizePass(defaultLoadout));
    }

    private PassRunner addTerrainAndLayoutPasses(PassRunner runner){
        return runner
        .add(new BaseTerrainPass())
        .add(new CellularCavesPass(4))
        .add(new RoomPlacementPass())
        .add(new ConnectivityPass())
        .add(new DistortPass(9f, 12f))
        .add(new DistortPass(5f, 7f))
        .add(new KarvexHydrologyPass())
        .add(new KarvexBasinPass())
        .add(new DistortPass(3.2f, 4.8f))
        .add(new KarvexTerrainPolishPass())
        .add(new GameplayFixPass())
        .add(new KarvexVentPass())
        .add(new KarvexValidationPass());
    }

    private Schematic loadFrontlineCoreLoadout(){
        Schematic fromFile = tryReadModSchematicFile();
        if(fromFile != null){
            return fromFile;
        }
        return Schematics.readBase64(legacyDefaultLoadoutBase64);
    }

    private Schematic tryReadModSchematicFile(){
        try{
            if(mods == null) return null;
            Mods.LoadedMod mod = mods.getMod(WarHammerMod.class);
            if(mod == null || mod.root == null) return null;

            Fi primary = mod.root.child(sCoreSchematicPath);
            if(primary != null && primary.exists()){
                return Schematics.read(primary);
            }

            Fi fallback = mod.root.child(sCoreSchematicPathFallback);
            if(fallback != null && fallback.exists()){
                return Schematics.read(fallback);
            }
        }catch(Throwable ignored){
            // Fallback is handled by base64 default loadout.
        }
        return null;
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
        boolean hasLargeSavedCore = s.info.bestCoreType != null && s.info.bestCoreType.size >= 4;
        boolean hasLargeLiveCore = s.isBeingPlayed() &&
        state != null &&
        state.rules != null &&
        state.rules.defaultTeam != null &&
        state.rules.defaultTeam.cores().contains(b -> b.block.size >= 4);

        return s.hasBase() && !s.isAttacked() && (hasLargeSavedCore || hasLargeLiveCore);
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
            }else{
                return destination.near().find(this::allowNumberedLaunch);
            }
        }else{
            return super.findLaunchCandidate(destination, selected);
        }
    }

    @Override
    public void getLockedText(Sector hovered, StringBuilder out){
        if((hovered.preset == null || !hovered.preset.requireUnlock) && hovered.near().contains(Sector::hasBase)){
            out.append("[red]").append(Iconc.cancel).append("[]").append(Blocks.coreFoundation.emoji()).append(Core.bundle.get("sector.foundationrequired"));
        }else{
            super.getLockedText(hovered, out);
        }
    }

    @Override
    public float getHeight(Vec3 position){
        float height = surface.rawHeight(seed, position);
        return Math.max(height, surface.water);
    }

    @Override
    public void getColor(Vec3 position, Color out){
        surface.pickSurfaceColor(seed, position, renderSectors(), out);
    }

    @Override
    public void getEmissiveColor(Vec3 position, Color out){
        Seq<Sector> sectors = renderSectors();
        Block block = surface.pickSurfaceBlock(seed, position, sectors, true);
        float pulse = 0.78f + Simplex.noise3d(seed + 77, 1, 1f, 8.2f, position.x, position.y + 41f, position.z) * 0.22f;
        out.set(0f, 0f, 0f, 0f);

        if(block == WHBlocksEnvironment.radiationWater
        || block == WHBlocksEnvironment.radiationWaterDeep
        || block == WHBlocksEnvironment.radiationSandWater
        || block == WHBlocksEnvironment.mineralSandRadiationWater){
            out.set(0.25f, 0.78f, 0.92f, 1f).mul(0.36f * pulse);
        }else if(block == WHBlocksEnvironment.promethium){
            out.set(0.94f, 0.63f, 0.30f, 1f).mul(0.24f * pulse);
        }else if(block == WHBlocksEnvironment.effluent
        || block == WHBlocksEnvironment.effluentDeep
        || block == WHBlocksEnvironment.mineralSandEffluentWater
        || block == Blocks.darksandTaintedWater
        || block == Blocks.deepTaintedWater){
            out.set(0.30f, 0.58f, 0.82f, 1f).mul(0.18f * pulse);
        }
    }

    @Override
    protected void genTile(Vec3 position, TileGen tile){

        tile.floor = surface.pickSurfaceBlock(seed, position, renderSectors(), false);
        tile.overlay = Blocks.air;
        Block wall = tile.floor.asFloor().wall;
        tile.block = wall == null ? Blocks.air : wall;

        if(Ridged.noise3d(seed + 1, position.x, position.y, position.z, 2, 22) > 0.28f){
            tile.block = Blocks.air;
        }
    }

    protected Seq<Sector> renderSectors(){
        if(sector != null && sector.planet != null) return sector.planet.sectors;
        return emptySectors;
    }

    @Override
    public int getSectorSize(Sector sector){
        return fixedSectorSize;
    }
}
