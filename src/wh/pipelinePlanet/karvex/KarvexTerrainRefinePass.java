package wh.pipelinePlanet.karvex;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec3;
import arc.struct.ObjectIntMap;
import arc.util.noise.Simplex;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.Tile;
import wh.content.WHBlocksEnvironment;
import wh.pipelinePlanet.core.GenContext;
import wh.pipelinePlanet.core.GenPass;

/**
 * Keeps terrain transitions broad and coherent after base LUT assignment.
 */
public class KarvexTerrainRefinePass implements GenPass{
    @Override
    public String name(){
        return "KarvexTerrainRefinePass";
    }

    @Override
    public void apply(GenContext ctx){
        paintTransitionBelts(ctx);
        paintMetalBelts(ctx);
        smoothTransitions(ctx, 3);
        enforceMineralMainland(ctx, 0.46f);
        cleanupSingles(ctx);
    }

    private void paintTransitionBelts(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;

            Block floor = tile.floor();
            if(isHeatOrRadiation(floor)) continue;

            float macro = sample(ctx, ctx.seed + 301, tile.x, tile.y, 2, 0.60f, 180f);
            float detail = sample(ctx, ctx.seed + 307, tile.x, tile.y, 2, 0.65f, 62f) * 0.22f;
            float field = macro + detail;

            Block next = floor;
            if(WHBlocksEnvironment.isMineralCoreFloor(floor)){
                if(field > 0.58f){
                    next = WHBlocksEnvironment.quartzSand;
                }else if(field > 0.32f){
                    next = WHBlocksEnvironment.mineralSand;
                }else if(field < -0.58f){
                    next = WHBlocksEnvironment.cementFloor;
                }else if(field < -0.38f){
                    next = WHBlocksEnvironment.quartzSand;
                }else if(Math.abs(field) < 0.10f && detail > 0.06f){
                    next = WHBlocksEnvironment.gravel;
                }
            }else if(floor == WHBlocksEnvironment.mineralSand){
                if (field < -0.34f) next = WHBlocksEnvironment.darkMineralSandstone;
                if(field > 0.62f) next = WHBlocksEnvironment.quartzSand;
            } else if (floor == WHBlocksEnvironment.darkMineralSandstone) {
                if(field < -0.62f) next = WHBlocksEnvironment.oreSalt;
                if(field > 0.56f) next = WHBlocksEnvironment.trachyte;
            }else if(floor == WHBlocksEnvironment.trachyte){
                if(field < -0.56f) next = WHBlocksEnvironment.oreShale;
            }else if(floor == WHBlocksEnvironment.darkRock){
                if(field > 0.56f) next = WHBlocksEnvironment.oreShale;
                if(field < -0.56f) next = WHBlocksEnvironment.trachyte;
            }

            if(next != floor){
                tile.setFloor(next.asFloor());
            }
        }
    }

    private void paintMetalBelts(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;

            Block floor = tile.floor();
            if(isHeatOrRadiation(floor)) continue;

            float belt = sample(ctx, ctx.seed + 313, tile.x, tile.y, 2, 0.61f, 130f);
            float detail = sample(ctx, ctx.seed + 317, tile.x, tile.y, 3, 0.68f, 72f);

            Block next = floor;
            if(floor == WHBlocksEnvironment.darkRock || floor == WHBlocksEnvironment.trachyte || floor == WHBlocksEnvironment.manganeseFloor || floor == WHBlocksEnvironment.chromiteFloor || floor == WHBlocksEnvironment.cobaltFloor){
                if(belt > 0.70f){
                    next = WHBlocksEnvironment.cobaltFloor;
                }else if(belt > 0.52f){
                    next = WHBlocksEnvironment.chromiteFloor;
                }else if(belt > 0.34f){
                    next = WHBlocksEnvironment.manganeseFloor;
                }
            }

            if(next == WHBlocksEnvironment.manganeseFloor && detail > 0.62f){
                next = WHBlocksEnvironment.manganeseStone;
            }else if(next == WHBlocksEnvironment.chromiteFloor && detail > 0.48f){
                next = WHBlocksEnvironment.chromiteFloorDark;
            }else if(next == WHBlocksEnvironment.chromiteFloorDark && detail > 0.66f){
                next = WHBlocksEnvironment.chromiteStone;
            }else if(next == WHBlocksEnvironment.cobaltFloor && detail > 0.62f){
                next = WHBlocksEnvironment.cobaltStone;
            }

            if(next != floor){
                tile.setFloor(next.asFloor());
            }
        }
    }

    private void smoothTransitions(GenContext ctx, int iterations){
        int w = ctx.width();
        int h = ctx.height();

        for(int it = 0; it < iterations; it++){
            short[] next = new short[w * h];

            for(Tile tile : ctx.tiles){
                int idx = tile.x + tile.y * w;
                Block floor = tile.floor();
                next[idx] = floor.id;

                if(tile.block() != Blocks.air) continue;
                if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) continue;
                if(isHeatOrRadiation(floor)) continue;

                Block selected = majorityNeighborFloor(ctx, tile.x, tile.y, floor);
                next[idx] = selected.id;
            }

            for(Tile tile : ctx.tiles){
                Block floor = Vars.content.block(next[tile.x + tile.y * w]);
                if(floor != null && floor.asFloor() != null){
                    tile.setFloor(floor.asFloor());
                }
            }
        }
    }

    private void enforceMineralMainland(GenContext ctx, float targetRatio){
        int surface = 0;
        int darkMineralFloor = 0;

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            surface++;
            if (WHBlocksEnvironment.isMineralCoreFloor(tile.floor())) darkMineralFloor++;
        }

        if(surface <= 0) return;
        int target = Math.round(surface * targetRatio);
        if (darkMineralFloor >= target) return;

        for (float threshold = 0.70f; threshold >= -0.25f && darkMineralFloor < target; threshold -= 0.10f) {
            for(Tile tile : ctx.tiles){
                if (darkMineralFloor >= target) break;
                if(tile.block() != Blocks.air) continue;
                if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
                if(WHBlocksEnvironment.isMineralCoreFloor(tile.floor())) continue;
                if(isHeatOrRadiation(tile.floor())) continue;
                if(isMetalFamily(tile.floor())) continue;
                if (WHBlocksEnvironment.isOreShaleFloor(tile.floor()) || tile.floor() == WHBlocksEnvironment.darkRock)
                    continue;

                float field = sample(ctx, ctx.seed + 331, tile.x, tile.y, 2, 0.60f, 170f);
                if(field < threshold) continue;

                tile.setFloor(WHBlocksEnvironment.defaultMineralFloor().asFloor());
                darkMineralFloor++;
            }
        }
    }

    private void cleanupSingles(GenContext ctx){
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            int idx = tile.x + tile.y * w;
            next[idx] = floor.id;

            int similar = similarCount(ctx, tile.x, tile.y, floor);
            if(isHeatFloor(floor) && similar <= 1){
                next[idx] = WHBlocksEnvironment.darkRock.id;
            }else if(isRadiationFloor(floor) && similar <= 1){
                next[idx] = WHBlocksEnvironment.darkMineralSandstone.id;
            }
        }

        for(Tile tile : ctx.tiles){
            Block floor = Vars.content.block(next[tile.x + tile.y * w]);
            if(floor != null && floor.asFloor() != null){
                tile.setFloor(floor.asFloor());
            }
        }
    }

    private Block majorityNeighborFloor(GenContext ctx, int x, int y, Block current){
        ObjectIntMap<Block> counts = new ObjectIntMap<>();
        counts.increment(current, 0, 1);

        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near == null) continue;
            Block floor = near.floor();
            if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) continue;
            if(isHeatOrRadiation(floor)) continue;
            counts.increment(floor, 0, 1);
        }

        Block best = current;
        int bestCount = 0;
        for(ObjectIntMap.Entry<Block> entry : counts.entries()){
            if(entry.value > bestCount){
                best = entry.key;
                bestCount = entry.value;
            }
        }
        return best;
    }

    private int similarCount(GenContext ctx, int x, int y, Block floor){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.floor() == floor){
                count++;
            }
        }
        return count;
    }

    private boolean isMetalFamily(Block floor){
        return floor == WHBlocksEnvironment.manganeseFloor
        || floor == WHBlocksEnvironment.manganeseStone
        || floor == WHBlocksEnvironment.chromiteFloor
        || floor == WHBlocksEnvironment.chromiteFloorDark
        || floor == WHBlocksEnvironment.chromiteStone
        || floor == WHBlocksEnvironment.cobaltFloor
        || floor == WHBlocksEnvironment.cobaltStone;
    }

    private boolean isHeatOrRadiation(Block floor){
        return isHeatFloor(floor) || isRadiationFloor(floor);
    }

    private boolean isHeatFloor(Block floor){
        return floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == Blocks.slag;
    }

    private boolean isRadiationFloor(Block floor){
        return floor == WHBlocksEnvironment.radiationSand
        || floor == WHBlocksEnvironment.radiationRockFloor
        || floor == WHBlocksEnvironment.radiationCraters;
    }

    private float sample(GenContext ctx, int seed, float x, float y, double octaves, double falloff, double scl){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z);
    }
}
