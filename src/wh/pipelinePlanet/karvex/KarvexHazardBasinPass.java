package wh.pipelinePlanet.karvex;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.noise.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.world.*;
import wh.content.*;
import wh.pipelinePlanet.core.*;

/**
 * Hazard shaping pass:
 * - Erekir-inspired slag and heated fields.
 * - Serpulo-inspired tar/promethium basins.
 */
public class KarvexHazardBasinPass implements GenPass{
    @Override
    public String name(){
        return "KarvexHazardBasinPass";
    }

    @Override
    public void apply(GenContext ctx){
        seedSlagAndHeat(ctx);
        seedTarAndPromethium(ctx);
        spreadHazards(ctx, 1);
        polishHazardEdges(ctx);
        removeTinySlagComponents(ctx, 18);
        enforceSlagGradient(ctx);
        reinforceHotRockAroundSlag(ctx);
        restorePromethiumPockets(ctx);
        protectRooms(ctx);
    }

    private void seedSlagAndHeat(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 24f, 14f)) continue;
            if(nearPollutedWater(ctx, tile.x, tile.y, 2)) continue;

            float hot = sample(ctx, ctx.seed + 421, tile.x + 782f, tile.y, 4, 0.72f, 360f);
            float aux = sample(ctx, ctx.seed + 429, tile.x - 310f, tile.y + 250f, 2, 0.66f, 150f);
            float field = hot + aux * 0.12f;

            if(field > 0.79f){
                if(field > 0.87f){
                    tile.setFloor(WHBlocksEnvironment.darkMagmaRock.asFloor());
                }else if(field > 0.84f){
                    tile.setFloor(WHBlocksEnvironment.darkHotRock.asFloor());
                }else if(field > 0.81f){
                    tile.setFloor(WHBlocksEnvironment.scorchedStone.asFloor());
                }else{
                    tile.setFloor(WHBlocksEnvironment.scorchedEarth.asFloor());
                }
                tile.setOverlay(Blocks.air);
            }
        }
    }

    private void seedTarAndPromethium(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 18f, 12f)) continue;

            Block floor = tile.floor();
            float tarA = sample(ctx, ctx.seed + 451, tile.x + 90f, tile.y - 60f, 3, 0.70f, 190f);
            float tarB = sample(ctx, ctx.seed + 453, tile.x + 510f, tile.y + 730f, 2, 0.66f, 320f);

            if((floor == WHBlocksEnvironment.darkRock || floor == WHBlocksEnvironment.trachyte)
            && tarA > 0.78f && tarB > 0.18f){
                tile.setFloor(WHBlocksEnvironment.oreShale.asFloor());
                floor = tile.floor();
            }

            if(isTarHost(floor) && tarA > 0.76f && tarB > 0.16f && !nearFloor(ctx, tile.x, tile.y, Blocks.slag, 2)){
                tile.setFloor(Blocks.tar.asFloor());
                tile.setOverlay(Blocks.air);
                continue;
            }

            float promA = sample(ctx, ctx.seed + 461, tile.x - 210f, tile.y + 310f, 3, 0.72f, 170f);
            float promB = sample(ctx, ctx.seed + 467, tile.x + 180f, tile.y - 510f, 2, 0.60f, 260f);

            if(isPromethiumHost(floor) && promA > 0.77f && promB > 0.14f){
                tile.setFloor(WHBlocksEnvironment.promethiumSand.asFloor());
                tile.setOverlay(Blocks.air);

                if(promA > 0.86f && (nearLiquid(ctx, tile.x, tile.y, 2) || nearRadiationWater(ctx, tile.x, tile.y, 3))){
                    tile.setFloor(WHBlocksEnvironment.promethium.asFloor());
                }
            }
        }
    }

    private void spreadHazards(GenContext ctx, int iterations){
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];

        for(int it = 0; it < iterations; it++){
            for(Tile tile : ctx.tiles){
                next[tile.x + tile.y * w] = tile.floor().id;
            }

            for(Tile tile : ctx.tiles){
                int idx = tile.x + tile.y * w;
                Block floor = tile.floor();

                if(isHeatFloor(floor)){
                    for(Point2 p : Geometry.d4){
                        Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                        if(near == null || near.block() != Blocks.air) continue;
                        if(!near.floor().hasSurface() || near.floor().isLiquid) continue;
                        if(isNearRoom(ctx, near.x, near.y, 16f, 10f)) continue;
                        if(nearPollutedWater(ctx, near.x, near.y, 2)) continue;

                        float chance = floor == Blocks.slag ? 0.15f : 0.11f;
                        if(!ctx.rand.chance(chance)) continue;

                        int nidx = near.x + near.y * w;
                        if(floor == Blocks.slag){
                            next[nidx] = ctx.rand.chance(0.52f) ? WHBlocksEnvironment.darkMagmaRock.id : WHBlocksEnvironment.darkHotRock.id;
                        }else if(floor == WHBlocksEnvironment.darkMagmaRock){
                            next[nidx] = WHBlocksEnvironment.darkHotRock.id;
                        }else{
                            next[nidx] = WHBlocksEnvironment.scorchedEarth.id;
                        }
                    }
                }

                if(floor == Blocks.tar){
                    for(Point2 p : Geometry.d8){
                        Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                        if(near == null || near.block() != Blocks.air) continue;
                        if(!near.floor().hasSurface() || near.floor().isLiquid) continue;
                        if(isNearRoom(ctx, near.x, near.y, 16f, 10f)) continue;
                        if(nearPollutedWater(ctx, near.x, near.y, 1)) continue;

                        if(ctx.rand.chance(0.06f)){
                            next[near.x + near.y * w] = ctx.rand.chance(0.7f) ? WHBlocksEnvironment.darkRock.id : WHBlocksEnvironment.trachyte.id;
                        }
                    }
                }

                if(floor == Blocks.slag){
                    for(Point2 p : Geometry.d8){
                        Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                        if(near == null || near.block() != Blocks.air) continue;
                        if(!near.floor().isLiquid) continue;

                        int nidx = near.x + near.y * w;
                        if(isRadiationWater(near.floor())){
                            next[nidx] = WHBlocksEnvironment.radiationWater.id;
                        }else if(isEffluentWater(near.floor())){
                            next[nidx] = WHBlocksEnvironment.effluent.id;
                        }
                    }
                }

                if(floor == WHBlocksEnvironment.promethiumSand
                && nearLiquid(ctx, tile.x, tile.y, 2)
                && ctx.rand.chance(nearRadiationWater(ctx, tile.x, tile.y, 2) ? 0.07f : 0.04f)){
                    next[idx] = WHBlocksEnvironment.promethium.id;
                }
            }

            for(Tile tile : ctx.tiles){
                Block floor = Vars.content.block(next[tile.x + tile.y * w]);
                if(floor != null && floor.asFloor() != null){
                    tile.setFloor(floor.asFloor());
                }
            }
        }
    }

    private void polishHazardEdges(GenContext ctx){
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            Block floor = tile.floor();
            int idx = tile.x + tile.y * w;
            next[idx] = floor.id;

            if(floor == Blocks.slag){
                int around = similarNeighbors(ctx, tile.x, tile.y, Blocks.slag);
                if(around <= 1){
                    next[idx] = WHBlocksEnvironment.darkMagmaRock.id;
                }
            }else if(floor == WHBlocksEnvironment.promethium){
                int around = similarNeighbors(ctx, tile.x, tile.y, WHBlocksEnvironment.promethium);
                if(around <= 1){
                    next[idx] = WHBlocksEnvironment.promethiumSand.id;
                }
            }else if(floor == Blocks.tar){
                int around = similarNeighbors(ctx, tile.x, tile.y, Blocks.tar);
                if(around <= 1){
                    next[idx] = WHBlocksEnvironment.darkRock.id;
                }
            }
        }

        for(Tile tile : ctx.tiles){
            Block floor = Vars.content.block(next[tile.x + tile.y * w]);
            if(floor != null && floor.asFloor() != null){
                tile.setFloor(floor.asFloor());
            }
        }
    }

    private void enforceSlagGradient(GenContext ctx){
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            next[tile.x + tile.y * w] = tile.floor().id;
        }

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(tile.floor() == Blocks.slag) continue;

            int d = nearestSlagDistance(ctx, tile.x, tile.y, 10);
            if(d <= 0) continue;
            if(nearPollutedWater(ctx, tile.x, tile.y, 1)) continue;

            float roughA = sample(ctx, ctx.seed + 583, tile.x + 31f, tile.y - 47f, 2, 0.65f, 22f) * 1.35f;
            float roughB = sample(ctx, ctx.seed + 587, tile.x - 18f, tile.y + 29f, 2, 0.67f, 11f) * 0.95f;
            float chip = Math.abs(sample(ctx, ctx.seed + 593, tile.x + 12f, tile.y - 19f, 1, 1f, 7f)) * 0.62f;
            float band = d + roughA + roughB + chip;
            int idx = tile.x + tile.y * w;
            if(d <= 2 || band <= 2.35f){
                next[idx] = ctx.rand.chance(0.58f) ? WHBlocksEnvironment.darkMagmaRock.id : WHBlocksEnvironment.darkHotRock.id;
            }else if(band <= 4.8f){
                if(ctx.rand.chance(0.24f + chip * 0.15f)){
                    next[idx] = WHBlocksEnvironment.darkMagmaRock.id;
                }else{
                    next[idx] = ctx.rand.chance(0.64f) ? WHBlocksEnvironment.darkHotRock.id : WHBlocksEnvironment.scorchedStone.id;
                }
            }else if(band <= 7.5f){
                if(ctx.rand.chance(0.20f + Math.max(0f, roughA) * 0.10f)){
                    next[idx] = WHBlocksEnvironment.scorchedStone.id;
                }else{
                    next[idx] = ctx.rand.chance(0.68f) ? WHBlocksEnvironment.darkRock.id : WHBlocksEnvironment.scorchedEarth.id;
                }
            }else if(band <= 9.8f){
                next[idx] = chip > 0.44f && ctx.rand.chance(0.38f) ? WHBlocksEnvironment.trachyte.id : WHBlocksEnvironment.scorchedEarth.id;
            }

            if(d >= 2 && d <= 6){
                float fleck = sample(ctx, ctx.seed + 599, tile.x + 14f, tile.y - 11f, 1, 1f, 6.5f)
                + sample(ctx, ctx.seed + 601, tile.x - 29f, tile.y + 23f, 1, 1f, 15f) * 0.45f;

                if(fleck > 0.94f){
                    next[idx] = WHBlocksEnvironment.darkHotRock.id;
                }else if(fleck < -0.93f && d >= 4){
                    next[idx] = WHBlocksEnvironment.scorchedStone.id;
                }
            }
        }

        for(Tile tile : ctx.tiles){
            Block floor = Vars.content.block(next[tile.x + tile.y * w]);
            if(floor != null && floor.asFloor() != null){
                tile.setFloor(floor.asFloor());
            }
        }
        roughenSlagPerimeter(ctx);
    }

    private void removeTinySlagComponents(GenContext ctx, int minSize){
        int w = ctx.width();
        int h = ctx.height();
        boolean[] visited = new boolean[w * h];
        IntSeq queue = new IntSeq();
        IntSeq comp = new IntSeq();

        for(Tile tile : ctx.tiles){
            int start = tile.x + tile.y * w;
            if(visited[start]) continue;
            if(tile.floor() != Blocks.slag) continue;

            queue.clear();
            comp.clear();
            visited[start] = true;
            queue.add(start);

            while(!queue.isEmpty()){
                int idx = queue.pop();
                comp.add(idx);
                int x = idx % w;
                int y = idx / w;

                for(Point2 p : Geometry.d8){
                    int nx = x + p.x;
                    int ny = y + p.y;
                    if(nx < 0 || ny < 0 || nx >= w || ny >= h) continue;

                    int nidx = nx + ny * w;
                    if(visited[nidx]) continue;

                    Tile near = ctx.tiles.get(nx, ny);
                    if(near == null || near.floor() != Blocks.slag) continue;

                    visited[nidx] = true;
                    queue.add(nidx);
                }
            }

            if(comp.size >= minSize) continue;

            for(int i = 0; i < comp.size; i++){
                int pos = comp.get(i);
                Tile t = ctx.tiles.get(pos % w, pos / w);
                if(t == null) continue;
                t.setFloor((ctx.rand.chance(0.68f) ? WHBlocksEnvironment.darkRock : WHBlocksEnvironment.darkHotRock).asFloor());
            }
        }
    }

    private void roughenSlagPerimeter(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(nearPollutedWater(ctx, tile.x, tile.y, 1)) continue;

            int d = nearestSlagDistance(ctx, tile.x, tile.y, 8);
            if(d < 1 || d > 8) continue;
            if(heatNeighborCount(ctx, tile.x, tile.y) <= 0) continue;

            float jag = sample(ctx, ctx.seed + 607, tile.x + 23f, tile.y - 17f, 1, 1f, 8f)
            + sample(ctx, ctx.seed + 613, tile.x - 31f, tile.y + 29f, 1, 1f, 19f) * 0.40f;

            if(d <= 2){
                if(jag > 0.75f){
                    tile.setFloor((ctx.rand.chance(0.52f) ? WHBlocksEnvironment.darkMagmaRock : WHBlocksEnvironment.darkHotRock).asFloor());
                }else if(jag < -0.80f){
                    tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
                }
            }else if(d <= 5){
                if(jag > 0.90f){
                    tile.setFloor(WHBlocksEnvironment.darkHotRock.asFloor());
                }else if(jag < -0.90f){
                    tile.setFloor(WHBlocksEnvironment.scorchedStone.asFloor());
                }else{
                    tile.setFloor(WHBlocksEnvironment.darkRock.asFloor());
                }
            }else{
                if(jag > 0.90f){
                    tile.setFloor(WHBlocksEnvironment.scorchedEarth.asFloor());
                }else if(jag < -0.92f){
                    tile.setFloor((ctx.rand.chance(0.55f) ? WHBlocksEnvironment.darkRock : WHBlocksEnvironment.trachyte).asFloor());
                }
            }
        }
    }

    private void reinforceHotRockAroundSlag(GenContext ctx){
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];
        for(Tile tile : ctx.tiles){
            next[tile.x + tile.y * w] = tile.floor().id;
        }

        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(nearPollutedWater(ctx, tile.x, tile.y, 1)) continue;
            if(tile.floor() == Blocks.slag) continue;

            int d = nearestSlagDistance(ctx, tile.x, tile.y, 6);
            if(d < 1 || d > 4) continue;

            float noise = sample(ctx, ctx.seed + 643, tile.x + 27f, tile.y - 21f, 2, 0.66f, 10f)
            + sample(ctx, ctx.seed + 647, tile.x - 33f, tile.y + 17f, 1, 1f, 26f) * 0.35f;

            int idx = tile.x + tile.y * w;
            if(d <= 2){
                if(noise > 0.56f){
                    next[idx] = ctx.rand.chance(0.54f) ? WHBlocksEnvironment.darkHotRock.id : WHBlocksEnvironment.darkMagmaRock.id;
                }else if(noise < -0.72f){
                    next[idx] = WHBlocksEnvironment.scorchedStone.id;
                }else{
                    next[idx] = WHBlocksEnvironment.darkRock.id;
                }
            }else{
                if(noise > 0.84f){
                    next[idx] = WHBlocksEnvironment.darkHotRock.id;
                }else if(noise < -0.80f){
                    next[idx] = WHBlocksEnvironment.scorchedStone.id;
                }else{
                    next[idx] = WHBlocksEnvironment.darkRock.id;
                }
            }
        }

        for(Tile tile : ctx.tiles){
            Block floor = Vars.content.block(next[tile.x + tile.y * w]);
            if(floor != null && floor.asFloor() != null){
                tile.setFloor(floor.asFloor());
            }
        }
    }

    private void preservePollutedShoreBand(GenContext ctx){
        int w = ctx.width();
        short[] next = new short[w * ctx.height()];

        for(Tile tile : ctx.tiles){
            int idx = tile.x + tile.y * w;
            Block floor = tile.floor();
            next[idx] = floor.id;

            if(tile.block() != Blocks.air) continue;
            if(!floor.asFloor().hasSurface() || floor.asFloor().isLiquid) continue;
            if(isHeatFloor(floor) || floor == Blocks.tar) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 12f, 8f)) continue;

            int rad = radiationWaterNeighbors(ctx, tile.x, tile.y);
            int eff = effluentWaterNeighbors(ctx, tile.x, tile.y);
            if(rad >= 1){
                next[idx] = rad >= 3 ? WHBlocksEnvironment.radiationRockFloor.id : WHBlocksEnvironment.radiationSand.id;
            }else if(eff >= 1){
                next[idx] = eff >= 3 ? WHBlocksEnvironment.mineralSandstone.id : WHBlocksEnvironment.mineralSand.id;
            }
        }

        for(Tile tile : ctx.tiles){
            Block floor = Vars.content.block(next[tile.x + tile.y * w]);
            if(floor != null && floor.asFloor() != null){
                tile.setFloor(floor.asFloor());
            }
        }
    }

    private void protectRooms(GenContext ctx){
        if(ctx.spawnRoom != null){
            clearRoomHazards(ctx, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + 6);
        }
        for(int i = 0; i < ctx.enemyRooms.size; i++){
            clearRoomHazards(ctx, ctx.enemyRooms.get(i).x, ctx.enemyRooms.get(i).y, 9);
        }
    }

    private void clearRoomHazards(GenContext ctx, int cx, int cy, int radius){
        int r2 = radius * radius;
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                if(ox * ox + oy * oy > r2) continue;
                Tile tile = ctx.tiles.get(cx + ox, cy + oy);
                if(tile == null) continue;

                if(tile.floor() == Blocks.slag
                || tile.floor() == WHBlocksEnvironment.promethium
                || tile.floor() == Blocks.tar
                || tile.floor() == WHBlocksEnvironment.darkMagmaRock
                || tile.floor() == WHBlocksEnvironment.darkHotRock){
                    tile.setFloor(WHBlocksEnvironment.mineralSandstone.asFloor());
                }
            }
        }
    }

    private int similarNeighbors(GenContext ctx, int x, int y, Block floor){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && near.floor() == floor) count++;
        }
        return count;
    }

    private boolean isNearRoom(GenContext ctx, int x, int y, float spawnRadius, float enemyRadius){
        if(ctx.spawnRoom != null && Mathf.within(x, y, ctx.spawnRoom.x, ctx.spawnRoom.y, ctx.spawnRoom.radius + spawnRadius)) return true;
        for(int i = 0; i < ctx.enemyRooms.size; i++){
            if(Mathf.within(x, y, ctx.enemyRooms.get(i).x, ctx.enemyRooms.get(i).y, enemyRadius)) return true;
        }
        return false;
    }

    private boolean nearFloor(GenContext ctx, int x, int y, Block floor, int radius){
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near != null && near.floor() == floor){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean nearLiquid(GenContext ctx, int x, int y, int radius){
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near != null && near.floor().isLiquid){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean nearPollutedWater(GenContext ctx, int x, int y, int radius){
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near != null && (isRadiationWater(near.floor()) || isEffluentWater(near.floor()))){
                    return true;
                }
            }
        }
        return false;
    }

    private int nearestSlagDistance(GenContext ctx, int x, int y, int maxRadius){
        for(int r = 1; r <= maxRadius; r++){
            int r2 = r * r;
            for(int ox = -r; ox <= r; ox++){
                for(int oy = -r; oy <= r; oy++){
                    if(ox * ox + oy * oy > r2) continue;
                    Tile near = ctx.tiles.get(x + ox, y + oy);
                    if(near != null && near.floor() == Blocks.slag){
                        return r;
                    }
                }
            }
        }
        return -1;
    }

    private int radiationWaterNeighbors(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isRadiationWater(near.floor())){
                count++;
            }
        }
        return count;
    }

    private int effluentWaterNeighbors(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isEffluentWater(near.floor())){
                count++;
            }
        }
        return count;
    }

    private int heatNeighborCount(GenContext ctx, int x, int y){
        int count = 0;
        for(Point2 p : Geometry.d8){
            Tile near = ctx.tiles.get(x + p.x, y + p.y);
            if(near != null && isHeatFloor(near.floor())){
                count++;
            }
        }
        return count;
    }

    private void restorePromethiumPockets(GenContext ctx){
        for(Tile tile : ctx.tiles){
            if(tile.block() != Blocks.air) continue;
            if(!tile.floor().hasSurface() || tile.floor().isLiquid) continue;
            if(isHeatFloor(tile.floor()) || tile.floor() == Blocks.tar) continue;
            if(isNearRoom(ctx, tile.x, tile.y, 14f, 10f)) continue;

            int rad = radiationWaterNeighbors(ctx, tile.x, tile.y);
            if(rad <= 0) continue;

            float field = sample(ctx, ctx.seed + 619, tile.x - 41f, tile.y + 37f, 2, 0.64f, 33f)
            + sample(ctx, ctx.seed + 631, tile.x + 16f, tile.y - 53f, 1, 1f, 84f) * 0.26f;

            if(field > 0.84f && rad >= 2){
                tile.setFloor(WHBlocksEnvironment.promethiumSand.asFloor());
                if(field > 0.97f && rad >= 4 && ctx.rand.chance(0.45f)){
                    tile.setFloor(WHBlocksEnvironment.promethium.asFloor());
                }
            }
        }
    }

    private boolean nearRadiationWater(GenContext ctx, int x, int y, int radius){
        for(int ox = -radius; ox <= radius; ox++){
            for(int oy = -radius; oy <= radius; oy++){
                Tile near = ctx.tiles.get(x + ox, y + oy);
                if(near != null && isRadiationWater(near.floor())){
                    return true;
                }
            }
        }
        return false;
    }

    private void smoothSlagGradient(GenContext ctx, int iterations){
        int w = ctx.width();

        for(int it = 0; it < iterations; it++){
            short[] next = new short[w * ctx.height()];
            for(Tile tile : ctx.tiles){
                int idx = tile.x + tile.y * w;
                Block floor = tile.floor();
                next[idx] = floor.id;
                if(floor == Blocks.slag) continue;
                if(!isSlagBandFloor(floor)) continue;

                int darkRock = 0;
                int scorchEarth = 0;

                for(Point2 p : Geometry.d8){
                    Tile near = ctx.tiles.get(tile.x + p.x, tile.y + p.y);
                    if(near == null) continue;
                    Block nf = near.floor();
                    if(nf == Blocks.slag){
                        darkRock += 2;
                    }else if(nf == WHBlocksEnvironment.darkRock){
                        darkRock++;
                    }else if(nf == WHBlocksEnvironment.scorchedEarth){
                        scorchEarth++;
                    }
                }

                Block best = floor;
                int bestScore = -1;
                if(darkRock > bestScore){
                    best = WHBlocksEnvironment.darkRock;
                    bestScore = darkRock;
                }
                if(scorchEarth > bestScore){
                    best = WHBlocksEnvironment.scorchedEarth;
                }

                next[idx] = best.id;
            }

            for(Tile tile : ctx.tiles){
                Block floor = Vars.content.block(next[tile.x + tile.y * w]);
                if(floor != null && floor.asFloor() != null){
                    tile.setFloor(floor.asFloor());
                }
            }
        }
    }

    private boolean isSlagBandFloor(Block floor){
        return floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.scorchedEarth;
    }

    private boolean isTarHost(Block floor){
        return floor == WHBlocksEnvironment.oreShale;
    }

    private boolean isPromethiumHost(Block floor){
        return floor == WHBlocksEnvironment.mineralSand
        || floor == WHBlocksEnvironment.mineralSandstone
        || floor == WHBlocksEnvironment.darkRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.quartzSand;
    }

    private boolean isHeatFloor(Block floor){
        return floor == Blocks.slag
        || floor == WHBlocksEnvironment.darkMagmaRock
        || floor == WHBlocksEnvironment.darkHotRock
        || floor == WHBlocksEnvironment.scorchedEarth
        || floor == WHBlocksEnvironment.scorchedStone;
    }

    private boolean isRadiationWater(Block floor){
        return floor == WHBlocksEnvironment.mineralSandRadiationWater
        || floor == WHBlocksEnvironment.radiationWater
        || floor == WHBlocksEnvironment.radiationWaterDeep
        || floor == WHBlocksEnvironment.radiationSandWater;
    }

    private boolean isEffluentWater(Block floor){
        return floor == WHBlocksEnvironment.mineralSandEffluentWater
        || floor == WHBlocksEnvironment.effluent
        || floor == WHBlocksEnvironment.effluentDeep;
    }

    private float sample(GenContext ctx, int seed, float x, float y, double octaves, double falloff, double scl){
        Vec3 v = ctx.sector.rect.project(x, y).scl(5f);
        return Simplex.noise3d(seed, octaves, falloff, 1f / scl, v.x, v.y, v.z);
    }
}
