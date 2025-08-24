//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.util;

import arc.func.*;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.content.Fx;
import mindustry.core.World;
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import org.jetbrains.annotations.*;
import wh.entities.Spawner;

import java.util.function.*;

import static arc.Core.atlas;
import static mindustry.Vars.*;
import static mindustry.core.World.toTile;
import static wh.graphics.Drawn.cycle_100;

public final class WHUtils{
    public static final Rand rand = new Rand(0);

    private static Tile tileParma;
    private static Posc result;
    private static float cDistance=0;
    private static int idx;
    private static final Vec2 tV = new Vec2(), tV2 = new Vec2(), tV3 = new Vec2();
    private static final IntSet collidedBlocks = new IntSet();
    private static final Rect rect = new Rect(), hitRect = new Rect();
    private static final Seq<Tile> tiles = new Seq<>();
    private static final Seq<Unit> units = new Seq<>();
    private static Building tmpBuilding;
    private static Unit tmpUnit;
    private static boolean hit, hitB;
    private static final BoolGrid collideLineCollided = new BoolGrid();
    private static final IntSeq lineCast = new IntSeq(), lineCastNext = new IntSeq();
    private static final Seq<Hit> hitEffects = new Seq<>();
    private static final FloatSeq distances = new FloatSeq();


    private WHUtils(){
    }

    @Contract(pure = true)
    public static int reverse(int rotation){
        return switch(rotation){
            case 0 -> 2;
            case 2 -> 0;
            case 1 -> 3;
            case 3 -> 1;
            default -> throw new IllegalStateException("Unexpected value: " + rotation);
        };
    }

    public static TextureRegion[][] splitLayers(String name, int size, int layerCount){
        TextureRegion[][] layers = new TextureRegion[layerCount][];

        for(int i = 0; i < layerCount; i++){
            layers[i] = split(name, size, i);
        }
        return layers;
    }

    public static TextureRegion[] split(String name, int size, int layer){
        TextureRegion textures = atlas.find(name);
        int margin = 0;
        int countX = textures.width / size;
        TextureRegion[] tiles = new TextureRegion[countX];

        for(int i = 0; i < countX; i++){
            tiles[i] = new TextureRegion(textures, i * (margin + size), layer * (margin + size), size, size);
        }
        return tiles;
    }

    //

    /**
     * Gets multiple regions inside a {@link TextureRegion}.
     * @param width The amount of regions horizontally.
     * @param height The amount of regions vertically.
     */
    //删掉 ultFire
    public static TextureRegion[] split(String name, int size, int width, int height){
        TextureRegion textures = atlas.find(name);
        int textureSize = width * height;
        TextureRegion[] regions = new TextureRegion[textureSize];

        float tileWidth = (textures.u2 - textures.u) / width;
        float tileHeight = (textures.v2 - textures.v) / height;

        for(int i = 0; i < textureSize; i++){
            float tileX = ((float)(i % width)) / width;
            float tileY = ((float)(i / width)) / height;
            TextureRegion region = new TextureRegion(textures);

            //start coordinate
            region.u = Mathf.map(tileX, 0f, 1f, region.u, region.u2) + tileWidth * 0.02f;
            region.v = Mathf.map(tileY, 0f, 1f, region.v, region.v2) + tileHeight * 0.02f;
            //end coordinate
            region.u2 = region.u + tileWidth * 0.96f;
            region.v2 = region.v + tileHeight * 0.96f;

            region.width = region.height = size;

            regions[i] = region;
        }
        return regions;
    }

    /** {@link Tile#relativeTo(int, int)} does not account for building rotation. */
    public static int relativeDirection(Building from, Building to){
        if(from == null || to == null) return -1;
        if(from.x == to.x && from.y > to.y) return (7 - from.rotation) % 4;
        if(from.x == to.x && from.y < to.y) return (5 - from.rotation) % 4;
        if(from.x > to.x && from.y == to.y) return (6 - from.rotation) % 4;
        if(from.x < to.x && from.y == to.y) return (4 - from.rotation) % 4;
        return -1;
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Position pos(float x, float y){
        return new Position() {
            @Override
            public float getX() {
                return x;
            }

            @Override
            public float getY() {
                return y;
            }
        };
    }

    /**
     * 计算x轴偏移后的坐标
     * @param px 原始x坐标
     * @param r 偏移距离
     * @param angle 偏移角度(度)
     * @return 计算得到的x坐标
     */
    public static float dx(float px, float r, float angle){
        return px + r * (float)Math.cos(angle * Math.PI / 180);
    }

    public static float dy(float py, float r, float angle){
        return py + r * (float)Math.sin(angle * Math.PI / 180);
    }

    /**
     * 计算椭圆上某点旋转后的坐标
     * @param px 椭圆中心x坐标
     * @param py 椭圆中心y坐标
     * @param a 椭圆长轴半径
     * @param b 椭圆短轴半径
     * @param rotation 椭圆旋转角度(度)
     * @param angle 要计算的点在椭圆上的角度(度)
     * @param xy 返回坐标类型(0=x坐标, 1=y坐标)
     * @return 旋转后的x或y坐标
     */
    public static float ellipseXY(float px, float py, float a, float b, float rotation, float angle, int xy){
        float x = a * Mathf.cosDeg(angle);
        float y = b * Mathf.sinDeg(angle);
        float xRotated = x * Mathf.cosDeg(rotation) - y * Mathf.sinDeg(rotation) + px;
        float yRotated = x * Mathf.sinDeg(rotation) + y * Mathf.cosDeg(rotation) + py;
        return xy == 0 ? xRotated : yRotated;
    }


    /**
     * 人为移动子弹
     * @param b 要移动的子弹
     * @param endX 结束坐标X
     * @param endY 结束坐标Y
     * @param speed 速度
     */
    public static void movePoint(Bullet b, float endX, float endY, float speed){
        // 计算两点之间的距离
        float distance = (float)Math.sqrt(Math.pow(endX - b.x, 2) + Math.pow(endY - b.y, 2));

        float moveSpeed = distance * speed;

        // 计算移动方向的单位向量
        float dx = (endX - b.x) / distance;
        float dy = (endY - b.y) / distance;

        // 计算每个tick内移动的距离
        float moveDistance = moveSpeed * Time.delta;

        // 更新子弹的位置
        b.move(dx * moveDistance, dy * moveDistance);

        // 检查是否到达或超过终点
        if(Math.abs(b.x - endX) < 1e-4f && Math.abs(b.y - endY) < 1e-4f){
            b.set(endX, endY);
        }
    }

    public static Bullet anyOtherCreate(Bullet bullet, BulletType bt, Entityc shooter, Entityc owner, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, Mover mover, float aimX, float aimY, @Nullable Teamc target){
        if(bt == null) return null;
        bullet.type = bt;
        bullet.owner = owner;
        bullet.shooter = (shooter == null ? owner : shooter);
        bullet.team = team;
        bullet.time = 0f;
        bullet.originX = x;
        bullet.originY = y;
        if(!(aimX == -1f && aimY == -1f)){
            bullet.aimTile = target instanceof Building b ? b.tile : world.tileWorld(aimX, aimY);
        }
        bullet.aimX = aimX;
        bullet.aimY = aimY;

        bullet.initVel(angle, bt.speed * velocityScl * (bt.velocityScaleRandMin != 1f || bt.velocityScaleRandMax != 1f ? Mathf.random(bt.velocityScaleRandMin, bt.velocityScaleRandMax) : 1f));
        if(bt.backMove){
            bullet.set(x - bullet.vel.x * Time.delta, y - bullet.vel.y * Time.delta);
        }else{
            bullet.set(x, y);
        }
        bullet.lifetime = bt.lifetime * lifetimeScl * (bt.lifeScaleRandMin != 1f || bt.lifeScaleRandMax != 1f ? Mathf.random(bt.lifeScaleRandMin, bt.lifeScaleRandMax) : 1f);
        bullet.data = data;
        bullet.drag = bt.drag;
        bullet.hitSize = bt.hitSize;
        bullet.mover = mover;
        bullet.damage = (damage < 0 ? bt.damage : damage) * bullet.damageMultiplier();
        //reset trail
        if(bullet.trail != null){
            bullet.trail.clear();
        }
        bullet.add();

        if(bt.keepVelocity && owner instanceof Velc v) bullet.vel.add(v.vel());
        return bullet;
    }

    public static void drawTiledFramesBar(float w, float h, float x, float y, Liquid liquid, float alpha){
        TextureRegion region = renderer.fluidFrames[liquid.gas ? 1 : 0][liquid.getAnimationFrame()];

        Draw.color(liquid.color, liquid.color.a * alpha);
        Draw.rect(region, x + w / 2f, y + h / 2f, w, h);
        Draw.color();
    }

    public static float findLaserLength(Bullet b, float angle, float length){
        Tmp.v1.trnsExact(angle, length);

        tileParma = null;

        boolean found = World.raycast(b.tileX(), b.tileY(), World.toTile(b.x + Tmp.v1.x), World.toTile(b.y + Tmp.v1.y),
        (x, y) -> (tileParma = world.tile(x, y)) != null && tileParma.team() != b.team && tileParma.block().absorbLasers);

        return found && tileParma != null ? Math.max(6f, b.dst(tileParma.worldx(), tileParma.worldy())) : length;
    }

    public static void collideLine(Bullet hitter, Team team, Effect effect, float x, float y, float angle, float length, boolean large, boolean laser){
        if(laser) length = findLaserLength(hitter, angle, length);

        collidedBlocks.clear();
        tV.trnsExact(angle, length);

        Intc2 collider = (cx, cy) -> {
            Building tile = world.build(cx, cy);
            boolean collide = tile != null && collidedBlocks.add(tile.pos());

            if(hitter.damage > 0){
                float health = !collide ? 0 : tile.health;

                if(collide && tile.team != team && tile.collide(hitter)){
                    tile.collision(hitter);
                    hitter.type.hit(hitter, tile.x, tile.y);
                }

                //try to heal the tile
                if(collide && hitter.type.testCollision(hitter, tile)){
                    hitter.type.hitTile(hitter, tile, cx * tilesize, cy * tilesize, health, false);
                }
            }
        };

        if(hitter.type.collidesGround){
            tV2.set(x, y);
            tV3.set(tV2).add(tV);
            World.raycastEachWorld(x, y, tV3.x, tV3.y, (cx, cy) -> {
                collider.get(cx, cy);

                for(Point2 p : Geometry.d4){
                    Tile other = world.tile(p.x + cx, p.y + cy);
                    if(other != null && (large || Intersector.intersectSegmentRectangle(tV2, tV3, other.getBounds(Tmp.r1)))){
                        collider.get(cx + p.x, cy + p.y);
                    }
                }
                return false;
            });
        }

        rect.setPosition(x, y).setSize(tV.x, tV.y);
        float x2 = tV.x + x, y2 = tV.y + y;

        if(rect.width < 0){
            rect.x += rect.width;
            rect.width *= -1;
        }

        if(rect.height < 0){
            rect.y += rect.height;
            rect.height *= -1;
        }

        float expand = 3f;

        rect.y -= expand;
        rect.x -= expand;
        rect.width += expand * 2;
        rect.height += expand * 2;

        Cons<Unit> cons = unit -> {
            unit.hitbox(hitRect);

            Vec2 vec = Geometry.raycastRect(x, y, x2, y2, hitRect.grow(expand * 2));

            if(vec != null && hitter.damage > 0){
                effect.at(vec.x, vec.y);
                unit.collision(hitter, vec.x, vec.y);
                hitter.collision(unit, vec.x, vec.y);
            }
        };

        units.clear();

        Units.nearbyEnemies(team, rect, unit -> {
            if(unit.checkTarget(hitter.type.collidesAir, hitter.type.collidesGround)){
                units.add(unit);
            }
        });

        units.sort(unit -> unit.dst2(hitter));
        units.each(cons);
    }


    public static Rand rand(long id){
        rand.setSeed(id);
        return rand;
    }

    public static <T> void shuffle(Seq<T> seq, Rand rand){
        T[] items = seq.items;
        for(int i = seq.size - 1; i >= 0; i--){
            int ii = Mathf.random(i);
            T temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }

    public static Seq<Tile> getAcceptableTiles(int x, int y, int range, Boolf<Tile> bool){
        Seq<Tile> tiles = new Seq<>(true, (int)(Mathf.pow(range, 2) * Mathf.pi), Tile.class);
        Geometry.circle(x, y, range, (x1, y1) -> {
            if((tileParma = world.tile(x1, y1)) != null && bool.get(tileParma)){
                tiles.add(world.tile(x1, y1));
            }
        });
        return tiles;
    }//有效位置

    private static void clearTmp(){
        tileParma = null;
        collidedBlocks.clear();
        tiles.clear();
    }

    public static void limitRangeWithoutNew(ItemTurret turret, float margin){
        for(ObjectMap.Entry<Item, BulletType> entry : turret.ammoTypes.entries()){
            entry.value.lifetime = (turret.range + margin) / entry.value.speed;
        }
    }

    public static float regSize(UnitType type){
        return type.hitSize / tilesize / tilesize / 3.25f;
    }

    /** [0]For flying, [1] for navy, [2] for ground */
    public static Seq<Boolf<Tile>> formats(){
        Seq<Boolf<Tile>> seq = new Seq<>(3);

        seq.add(
        tile -> world.getQuadBounds(Tmp.r1).contains(tile.getBounds(Tmp.r2)),
        tile -> tile.floor().isLiquid && !tile.cblock().solid && !tile.floor().solid && !tile.overlay().solid && !tile.block().solidifes,
        tile -> !tile.floor().isDeep() && !tile.cblock().solid && !tile.floor().solid && !tile.overlay().solid && !tile.block().solidifes
        );

        return seq;
    }

    public static Boolf<Tile> ableToSpawn(UnitType type){
        Boolf<Tile> boolf;

        Seq<Boolf<Tile>> boolves = formats();

        if(type.flying){
            boolf = boolves.get(0);
        }else if(WaterMovec.class.isAssignableFrom(type.constructor.get().getClass())){
            boolf = boolves.get(1);
        }else{
            boolf = boolves.get(2);
        }

        return boolf;
    }

    public static Seq<Tile> ableToSpawn(UnitType type, float x, float y, float range){
        Seq<Tile> tSeq = new Seq<>(Tile.class);
        Boolf<Tile> boolf = ableToSpawn(type);
        return tSeq.addAll(getAcceptableTiles(toTile(x), toTile(y), toTile(range), boolf));
    }

    public static boolean hasAnyValidSpawnPosition(UnitType type, float x, float y, float range){
        if(type == null) return false;
        //获取可生成的位置
        Seq<Tile> validTiles = ableToSpawn(type, x, y, range);
        return validTiles.any();
    }

    public static boolean ableToSpawnPoints(Seq<Vec2> spawnPoints, UnitType type,
                                            float x, float y, float range, int num, long seed){
        Seq<Tile> tSeq = ableToSpawn(type, x, y, range);
        rand.setSeed(seed);
        for(int i = 0; i < num; i++){
            // 将 Tile 序列转换为数组并清空原序列
            Tile[] positions = tSeq.shrink();
            if(positions.length < num) return false;
            spawnPoints.add(new Vec2().set(positions[rand.nextInt(positions.length)]));
        }

        return true;
    }


    public static boolean spawnUnit(Team team, float x, float y, float angle, float spawnRange, float spawnReloadTime, float spawnDelay, UnitType type, int spawnNum, boolean airdrop, Cons<Spawner> modifier){
        if(type == null) return false;
        clearTmp();
        Seq<Vec2> vectorSeq = new Seq<>();

        // 获取有效生成位置
        if(!ableToSpawnPoints(vectorSeq, type, x, y, spawnRange, spawnNum, rand.nextLong())) return false;

        int i = 0;
        for(Vec2 s : vectorSeq){
            Spawner spawner = Pools.obtain(Spawner.class, Spawner::new);
            spawner.init(type, team, s, angle, spawnReloadTime + i * spawnDelay, airdrop);

            modifier.get(spawner);
            if(!net.client()) spawner.add();
            i++;
        }
        return true;
    }

    public static float rotator_90(){
        return 90 * Interp.pow5.apply(Mathf.curve(cycle_100(), 0.15f, 0.85f));
    }


    public static void tri(float x, float y, float width, float length, float angle){
        float wx = Angles.trnsx(angle + 90, width), wy = Angles.trnsy(angle + 90, width);
        Fill.tri(x + wx, y + wy, x - wx, y - wy, Angles.trnsx(angle, length) + x, Angles.trnsy(angle, length) + y);
    }

    public static void randLenVectors(long seed, int amount, float length, float minLength, float angle, float range, Floatc2 cons){
        rand.setSeed(seed);
        for(int i = 0; i < amount; i++){
            tV.trns(angle + rand.range(range), minLength + rand.random(length));
            cons.get(tV.x, tV.y);
        }
    }

    public static void collideLineRawEnemyRatio(Team team, float x, float y, float x2, float y2, float width, Boolf3<Building, Float, Boolean> buildingCons, Boolf2<Unit, Float> unitCons, Floatc2 effectHandler){
        float minRatio = 0.05f;
        collideLineRawEnemy(team, x, y, x2, y2, width, (building, direct) -> {
            float size = (building.block.size * tilesize / 2f);
            float dst = Mathf.clamp(1f - ((Intersector.distanceSegmentPoint(x, y, x2, y2, building.x, building.y) - width) / size), minRatio, 1f);
            return buildingCons.get(building, dst, direct);
        }, unit -> {
            float size = (unit.hitSize / 2f);
            float dst = Mathf.clamp(1f - ((Intersector.distanceSegmentPoint(x, y, x2, y2, unit.x, unit.y) - width) / size), minRatio, 1f);
            return unitCons.get(unit, dst);
        }, effectHandler, true);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRawEnemy(Team team, float x, float y, float x2, float y2, float width, boolean hitTiles, boolean hitUnits, boolean stopSort, HitHandler handler){
        collideLineRaw(x, y, x2, y2, width, width, b -> b.team != team, u -> u.team != team, hitTiles, hitUnits, h -> h.dst2(x, y), handler, stopSort);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRawEnemy(Team team, float x, float y, float x2, float y2, Boolf2<Building, Boolean> buildingCons, Cons<Unit> unitCons, Floatc2 effectHandler, boolean stopSort){
        collideLineRaw(x, y, x2, y2, 3f, b -> b.team != team, u -> u.team != team, buildingCons, unitCons, healthc -> healthc.dst2(x, y), effectHandler, stopSort);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRawEnemy(Team team, float x, float y, float x2, float y2, float width, Boolf2<Building, Boolean> buildingCons, Boolf<Unit> unitCons, Floatc2 effectHandler, boolean stopSort){
        collideLineRaw(x, y, x2, y2, width, width, b -> b.team != team, u -> u.team != team, buildingCons, unitCons, healthc -> healthc.dst2(x, y), effectHandler, stopSort);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRawEnemy(Team team, float x, float y, float x2, float y2, float width, Boolf2<Building, Boolean> buildingCons, Boolf<Unit> unitCons, Floatf<Healthc> sort, Floatc2 effectHandler, boolean stopSort){
        collideLineRaw(x, y, x2, y2, width, width, b -> b.team != team, u -> u.team != team, buildingCons, unitCons, sort, effectHandler, stopSort);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRawEnemy(Team team, float x, float y, float x2, float y2, float unitWidth, float tileWidth, Boolf2<Building, Boolean> buildingCons, Boolf<Unit> unitCons, Floatc2 effectHandler, boolean stopSort){
        collideLineRaw(x, y, x2, y2, unitWidth, tileWidth, b -> b.team != team, u -> u.team != team, buildingCons, unitCons, healthc -> healthc.dst2(x, y), effectHandler, stopSort);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRawEnemy(Team team, float x, float y, float x2, float y2, Boolf2<Building, Boolean> buildingCons, Boolf<Unit> unitCons, Floatc2 effectHandler, boolean stopSort){
        collideLineRaw(x, y, x2, y2, 3f, b -> b.team != team, u -> u.team != team, buildingCons, unitCons, healthc -> healthc.dst2(x, y), effectHandler, stopSort);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRawEnemy(Team team, float x, float y, float x2, float y2, Boolf2<Building, Boolean> buildingCons, Cons<Unit> unitCons, Floatf<Healthc> sort, Floatc2 effectHandler){
        collideLineRaw(x, y, x2, y2, 3f, b -> b.team != team, u -> u.team != team, buildingCons, unitCons, sort, effectHandler);
    }

    public static void collideLineRawEnemy(Team team, float x, float y, float x2, float y2, float width, Boolf<Healthc> pred, Boolf2<Building, Boolean> buildingCons, Boolf<Unit> unitCons, Floatc2 effectHandler, boolean stopSort){
        collideLineRaw(x, y, x2, y2, width, width, b -> b.team != team && pred.get(b), u -> u.team != team && pred.get(u), buildingCons, unitCons, healthc -> healthc.dst2(x, y), effectHandler, stopSort);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRaw(float x, float y, float x2, float y2, float unitWidth, Boolf<Building> buildingFilter, Boolf<Unit> unitFilter, Boolf2<Building, Boolean> buildingCons, Cons<Unit> unitCons, Floatf<Healthc> sort, Floatc2 effectHandler){
        collideLineRaw(x, y, x2, y2, unitWidth, buildingFilter, unitFilter, buildingCons, unitCons, sort, effectHandler, false);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRaw(float x, float y, float x2, float y2, float unitWidth, Boolf<Building> buildingFilter, Boolf<Unit> unitFilter, Boolf2<Building, Boolean> buildingCons, Cons<Unit> unitCons, Floatf<Healthc> sort, Floatc2 effectHandler, boolean stopSort){
        collideLineRaw(x, y, x2, y2, unitWidth, buildingFilter, unitFilter, buildingCons, unitCons == null ? null : unit -> {
            unitCons.get(unit);
            return false;
        }, sort, effectHandler, stopSort);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRaw(float x, float y, float x2, float y2, float unitWidth, Boolf<Building> buildingFilter, Boolf<Unit> unitFilter, Boolf2<Building, Boolean> buildingCons, Boolf<Unit> unitCons, Floatf<Healthc> sort, Floatc2 effectHandler, boolean stopSort){
        collideLineRaw(x, y, x2, y2, unitWidth, 0f, buildingFilter, unitFilter, buildingCons, unitCons, sort, effectHandler, stopSort);
    }

    /** @author EyeOfDarkness */
    public static void collideLineRaw(float x, float y, float x2, float y2, float unitWidth, float tileWidth, Boolf<Building> buildingFilter, Boolf<Unit> unitFilter, Boolf2<Building, Boolean> buildingCons, Boolf<Unit> unitCons, Floatf<Healthc> sort, Floatc2 effectHandler, boolean stopSort){
        collideLineRaw(x, y, x2, y2, unitWidth, tileWidth,
        buildingFilter, unitFilter, buildingCons != null, unitCons != null,
        sort, (ex, ey, ent, direct) -> {
            boolean hit = false;
            if(unitCons != null && direct && ent instanceof Unit){
                hit = unitCons.get((Unit)ent);
            }else if(buildingCons != null && ent instanceof Building){
                hit = buildingCons.get((Building)ent, direct);
            }

            if(effectHandler != null && direct) effectHandler.get(ex, ey);
            return hit;
        }, stopSort
        );
    }

    /** @author EyeOfDarkness */
    public static void collideLineRaw(float x, float y, float x2, float y2, float unitWidth, float tileWidth, Boolf<Building> buildingFilter, Boolf<Unit> unitFilter, boolean hitTile, boolean hitUnit, Floatf<Healthc> sort, HitHandler hitHandler, boolean stopSort){
        hitEffects.clear();
        lineCast.clear();
        lineCastNext.clear();
        collidedBlocks.clear();

        tV.set(x2, y2);
        if(hitTile){
            collideLineCollided.clear();
            Runnable cast = () -> {
                hitB = false;
                lineCast.each(i -> {// 遍历当前需要检测的格子
                    int tx = Point2.x(i), ty = Point2.y(i); // 获取格子坐标
                    Building build = world.build(tx, ty);// 获取该位置的建筑物

                    boolean hit = false;
                    if(build != null && (buildingFilter == null || buildingFilter.get(build)) && collidedBlocks.add(build.pos())){
                        if(sort == null){
                            hit = hitHandler.get(tx * tilesize, ty * tilesize, build, true);
                        }else{
                            hit = hitHandler.get(tx * tilesize, ty * tilesize, build, false);
                            Hit he = Pools.obtain(Hit.class, Hit::new);
                            he.ent = build;
                            he.x = tx * tilesize;
                            he.y = ty * tilesize;

                            hitEffects.add(he);
                        }
                        // 如果命中且是第一次命中，调整射线终点
                        if(hit && !hitB){
                            tV.trns(Angles.angle(x, y, x2, y2), Mathf.dst(x, y, build.x, build.y)).add(x, y);
                            hitB = true;
                        }
                    }
                    // 处理相邻格子的碰撞检测
                    Vec2 segment = Intersector.nearestSegmentPoint(x, y, tV.x, tV.y, tx * tilesize, ty * tilesize, tV2);
                    if(!hit && tileWidth > 0f){
                        for(Point2 p : Geometry.d8){
                            int newX = (p.x + tx);
                            int newY = (p.y + ty);
                            boolean within = !hitB || Mathf.within(x / tilesize, y / tilesize, newX, newY, tV.dst(x, y) / tilesize);
                            if(segment.within(newX * tilesize, newY * tilesize, tileWidth) && collideLineCollided.within(newX, newY) && !collideLineCollided.get(newX, newY) && within){
                                lineCastNext.add(Point2.pack(newX, newY));
                                collideLineCollided.set(newX, newY, true);
                            }
                        }
                    }
                });

                lineCast.clear();
                lineCast.addAll(lineCastNext);
                lineCastNext.clear();
            };
            // 初始射线投射
            world.raycastEachWorld(x, y, x2, y2, (cx, cy) -> {
                if(collideLineCollided.within(cx, cy) && !collideLineCollided.get(cx, cy)){
                    lineCast.add(Point2.pack(cx, cy));
                    collideLineCollided.set(cx, cy, true);
                }

                cast.run();
                return hitB;
            });

            while(!lineCast.isEmpty()) cast.run();
        }

        if(hitUnit){
            rect.setPosition(x, y).setSize(tV.x - x, tV.y - y);

            if(rect.width < 0){
                rect.x += rect.width;
                rect.width *= -1;
            }

            if(rect.height < 0){
                rect.y += rect.height;
                rect.height *= -1;
            }

            rect.grow(unitWidth * 2f);
            Groups.unit.intersect(rect.x, rect.y, rect.width, rect.height, unit -> {
                if(unitFilter == null || unitFilter.get(unit)){
                    unit.hitbox(hitRect);
                    hitRect.grow(unitWidth * 2);

                    // 计算射线与单位碰撞盒的交点
                    Vec2 vec = Geometry.raycastRect(x, y, tV.x, tV.y, hitRect);

                    if(vec != null){
                        float scl = (unit.hitSize - unitWidth) / unit.hitSize;
                        vec.sub(unit).scl(scl).add(unit);
                        if(sort == null){
                            hitHandler.get(vec.x, vec.y, unit, true);
                        }else{
                            Hit he = Pools.obtain(Hit.class, Hit::new);
                            he.ent = unit;
                            he.x = vec.x;
                            he.y = vec.y;
                            hitEffects.add(he);
                        }
                    }
                }
            });
        }
        // 排序和处理命中效果
        if(sort != null){
            hit = false;
            hitEffects.sort(he -> sort.get(he.ent)).each(he -> {
                if(!stopSort || !hit){
                    hit = hitHandler.get(he.x, he.y, he.ent, true);
                }

                Pools.free(he);
            });
        }

        hitEffects.clear();
    }

    static class Hit implements Pool.Poolable{
        Healthc ent;
        float x, y;

        @Override
        public void reset(){
            ent = null;
            x = y = 0f;
        }
    }

    public interface HitHandler{
        boolean get(float x, float y, Healthc ent, boolean direct);
    }

}
