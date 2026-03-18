# Modgen Pipeline Notes

This note describes the current folder layout under:

`src/wh/pipelinePlanet`

## Folder layout

```text
modgenpipeline/
  core/
    GenConfig.java
    GenContext.java
    GenPass.java
    PassRunner.java
    PipelinePlanetGenerator.java
    TilePass.java
    TilePassStage.java
  data/
    RoomAnchor.java
  passes/
    BaseTerrainPass.java
    RoomPlacementPass.java
    ConnectivityPass.java
    HydrologyPass.java
    OrePass.java
    DecorationPass.java
    GameplayFixPass.java
    ValidationPass.java
    tile/
      OreTilePass.java
      DecorationTilePass.java
  filters/
    GenerateFilterPass.java
  serpulo/
    SerpuloSurfaceTable.java
    SerpuloDecomposedPipelinePlanetGenerator.java
```

## Responsibility by folder

- `core`: pipeline framework and execution model.
- `data`: shared lightweight data structures.
- `passes`: reusable generation passes.
- `passes/tile`: per-tile units designed for low allocation hot loops.
- `filters`: adapter for `mindustry.maps.filters` integration.
- `serpulo`: explicit Serpulo style building blocks (arr/dec/tars + decomposed generator).

## Why this is more readable

- Boundary is explicit: framework vs pass logic vs compatibility layer.
- New files are easier to place correctly.
- Filter-based post-processing can be added without touching core classes.

## Notes

- This layout improves maintainability and discoverability.
- It does not remove all ordering constraints between passes.
- The current setup still uses sequential pass execution by design.
- `TilePassStage` can fuse multiple lightweight tile rules into one full-map scan.

## Decomposed Serpulo style

Use:

- `wh.pipelinePlanet.serpulo.SerpuloDecomposedPipelinePlanetGenerator`

Execution flow:

1. `genTile` applies Serpulo surface lookup (`arr`) and tar/metal special rules (`tars`).
2. Pipeline passes run in order: terrain fix -> room placement -> connectivity -> hydrology -> tech-grid -> ore+Serpulo decoration (`dec`) -> gameplay fix -> validation.
3. Planet mesh coloring/height/emissive uses the same Serpulo surface table for consistent visual style.

