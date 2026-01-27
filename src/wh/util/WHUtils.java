//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package wh.util;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.Nullable;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.ai.types.*;
import mindustry.core.*;
import mindustry.entities.Damage.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.defense.turrets.*;
import org.jetbrains.annotations.*;
import wh.entities.*;
import wh.entities.bullet.laser.LaserBeamBulletType.*;

import static arc.Core.atlas;
import static mindustry.Vars.*;
import static mindustry.core.World.toTile;
import static wh.graphics.Drawn.cycle_100;

public final class WHUtils{
    public static final Rand rand = new Rand(0);

    private static Tile tileParma;
    private static final Vec2 tV = new Vec2(), tV2 = new Vec2(), tV3 = new Vec2();
    private static final IntSet collidedBlocks = new IntSet();
    private static final Rect rect = new Rect(), hitRect = new Rect();
    private static final Seq<Tile> tiles = new Seq<>();
    private static final Seq<Unit> units = new Seq<>();
    private static Building tmpBuilding;
    private static Unit tmpUnit;

    static final Vec2 v1 = new Vec2();
    static final Vec2 v2 = new Vec2();
    static final Vec2 v3 = new Vec2();
    static final Vec2 v4 = new Vec2();
    static final Vec2 v5 = new Vec2();
    private static final FloatSeq distances = new FloatSeq();
    private static float maxDst = 0f;


    private static EntityCollisions mover = new EntityCollisions();


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

    public static TextureRegion[][] splitUnLayers(String name, int size){
        return splitUnLayers(Core.atlas.find(name), size);
    }

    public static TextureRegion[][] splitUnLayers(TextureRegion region, int size){
        int x = region.getX();
        int y = region.getY();
        int width = region.width;
        int height = region.height;

        int sw = width / size;
        int sh = height / size;

        int startX = x;
        TextureRegion[][] tiles = new TextureRegion[sw][sh];
        for(int cy = 0; cy < sh; cy++, y += size){
            x = startX;
            for(int cx = 0; cx < sw; cx++, x += size){
                tiles[cx][cy] = new TextureRegion(region.texture, x, y, size, size);
            }
        }

        return tiles;
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

    public static TextureRegion[][] splitLayers2(String name, int size, int width, int height){
        TextureRegion base = Core.atlas.find(name);
        if(base == null) throw new IllegalArgumentException("Texture not found: " + name);

        TextureRegion[][] layers = new TextureRegion[height / size][width / size];

        for(int y = 0; y < height / size; y++){
            for(int x = 0; x < width / size; x++){
                layers[y][x] = new TextureRegion(base, x * size, y * size, size, size);
            }
        }
        return layers;
    }

    /**
     * Gets multiple regions inside a {@link TextureRegion}.
     * @param width The amount of regions horizontally.
     * @param height The amount of regions vertically.
     */

    public static TextureRegion[] split(String name, int size, int width, int height){
        TextureRegion reg = Core.atlas.find(name);
        int textureSize = width * height;
        TextureRegion[] regions = new TextureRegion[textureSize];

        float tileWidth = (reg.u2 - reg.u) / width;
        float tileHeight = (reg.v2 - reg.v) / height;

        for(int i = 0; i < textureSize; i++){
            float tileX = ((float)(i % width)) / width;
            float tileY = ((float)(i / width)) / height;
            TextureRegion region = new TextureRegion(reg);

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
        return new Position(){
            @Override
            public float getX(){
                return x;
            }

            @Override
            public float getY(){
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

    public static void superEllipse(float angle, float r, float centerX, float centerY, float rotation, Vec2 out){
        superEllipse(angle, r, r, 4, centerX, centerY, rotation, out);
    }

    /**
     * 返回旋转 + 平移后的超椭圆上任意一点
     * @param angle 参数角度 0~2π（0 对应最右端）
     * @param a x 半轴
     * @param b y 半轴
     * @param n 超椭圆指数（2=椭圆，4≈矩形）
     * @param centerX 中心偏移 x
     * @param centerY 中心偏移 y
     * @param rotation 即时旋转角（rad）
     * @param out 输出向量
     */
    public static void superEllipse(float angle, float a, float b, float n, float centerX, float centerY, float rotation, Vec2 out){
        float cosT = Mathf.cosDeg(angle);
        float sinT = Mathf.sinDeg(angle);
        float x = a * Mathf.sign(cosT) * Mathf.pow(Math.abs(cosT), 2f / n);
        float y = b * Mathf.sign(sinT) * Mathf.pow(Math.abs(sinT), 2f / n);

        float cosR = Mathf.cosDeg(rotation);
        float sinR = Mathf.sinDeg(rotation);
        out.set(x * cosR - y * sinR,
        x * sinR + y * cosR);

        out.add(centerX, centerY);
    }

    public static float findLaserPierceLength(Bullet b, int pierceCap, boolean laser, float length, float angle){
        if(!(b instanceof LaserDataBullet data)) return 0;
        v1.trnsExact(angle, length);
        rect.setPosition(b.x, b.y).setSize(v1.x, v1.y).normalize().grow(3f);
        data.stop = false;
        maxDst = Float.POSITIVE_INFINITY;

        distances.clear();

        if(b.type.collidesGround && b.type.collidesTiles){
            World.raycast(b.tileX(), b.tileY(), World.toTile(b.x + v1.x), World.toTile(b.y + v1.y), (x, y) -> {
                //add distance to list so it can be processed
                var build = world.build(x, y);

                if(build != null && build.team != b.team && build.collide(b) && b.checkUnderBuild(build, x * tilesize, y * tilesize)){
                    Tmp.v2.trns(b.rotation(), length * 1.5f).add(b);
                    float dst = Intersector.distanceLinePoint(b.x, b.y, Tmp.v2.x, Tmp.v2.y, build.x, build.y);
                    float dst2 = b.dst(build)/*-build.hitSize()*0.7f+dst*/;
                    distances.add(dst2);
                    data.stop = false;

                    if(laser && build.absorbLasers()){

                        maxDst = dst2;
                        data.stop = true;
                        return true;
                    }
                }
                return false;
            });
        }

        Units.nearbyEnemies(b.team, rect, u -> {
            u.hitbox(hitRect);

            if(u.checkTarget(b.type.collidesAir, b.type.collidesGround) && u.hittable() &&
            Intersector.intersectSegmentRectangle(b.x, b.y, b.x + v1.x, b.y + v1.y, hitRect)){
                Tmp.v2.trns(b.rotation(), length * 1.5f).add(b);
                float dst = Intersector.distanceLinePoint(b.x, b.y, Tmp.v2.x, Tmp.v2.y, u.x, u.y);
                float dst2 = b.dst(u) - u.hitSize() * 0.7f + dst;
                distances.add(b.dst(u));
            }
        });

        data.stop = distances.size > pierceCap && !data.stop;

        distances.sort();

        //return either the length when not enough things were pierced,
        //or the last pierced object if there were enough blockages
        return Math.min(distances.size < pierceCap || pierceCap < 0 ? length : Math.max(6f, distances.get(pierceCap - 1)), maxDst);
    }

    public static float findLaserPierceLength2(Bullet b, int pierceCap, boolean laser, float length, float angle){
        v1.trnsExact(angle, length);
        rect.setPosition(b.x, b.y).setSize(v1.x, v1.y).normalize().grow(3f);
        maxDst = Float.POSITIVE_INFINITY;

        distances.clear();

        if(b.type.collidesGround && b.type.collidesTiles){
            World.raycast(b.tileX(), b.tileY(), World.toTile(b.x + v1.x), World.toTile(b.y + v1.y), (x, y) -> {
                //add distance to list so it can be processed
                var build = world.build(x, y);

                if(build != null && build.team != b.team && build.collide(b) && b.checkUnderBuild(build, x * tilesize, y * tilesize)){
                    Tmp.v2.trns(b.rotation(), length * 1.5f).add(b);
                    float dst = Intersector.distanceLinePoint(b.x, b.y, Tmp.v2.x, Tmp.v2.y, build.x, build.y);
                    float dst2 = b.dst(build)/*-build.hitSize()*0.7f+dst*/;
                    distances.add(dst2);

                    if(laser && build.absorbLasers()){

                        maxDst = dst2;
                        return true;
                    }
                }
                return false;
            });
        }

        Units.nearbyEnemies(b.team, rect, u -> {
            u.hitbox(hitRect);

            if(u.checkTarget(b.type.collidesAir, b.type.collidesGround) && u.hittable() &&
            Intersector.intersectSegmentRectangle(b.x, b.y, b.x + v1.x, b.y + v1.y, hitRect)){
                Tmp.v2.trns(b.rotation(), length * 1.5f).add(b);
                float dst = Intersector.distanceLinePoint(b.x, b.y, Tmp.v2.x, Tmp.v2.y, u.x, u.y);
                float dst2 = b.dst(u) - u.hitSize() * 0.7f + dst;
                distances.add(b.dst(u));
            }
        });


        distances.sort();

        //return either the length when not enough things were pierced,
        //or the last pierced object if there were enough blockages
        return Math.min(distances.size < pierceCap || pierceCap < 0 ? length : Math.max(6f, distances.get(pierceCap - 1)), maxDst);
    }

    private static final Seq<Collided> collided = new Seq<>();
    private static final Pool<Collided> collidePool = Pools.get(Collided.class, Collided::new);

    public static void collideLine(Bullet hitter, Team team, float x, float y, float angle, float length, boolean large, boolean laser, int pierceCap){
        length = findLaserPierceLength(hitter, pierceCap, laser, length, angle);

        collidedBlocks.clear();
        v1.trnsExact(angle, length);

        if(hitter.type.collidesGround && hitter.type.collidesTiles){
            v2.set(x, y);
            v3.set(v2).add(v1);
            World.raycastEachWorld(x, y, v3.x, v3.y, (cx, cy) -> {
                Building tile = world.build(cx, cy);
                boolean collide = tile != null && tile.collide(hitter) && hitter.checkUnderBuild(tile, cx * tilesize, cy * tilesize)
                && ((tile.team != team && tile.collide(hitter)) || hitter.type.testCollision(hitter, tile)) && collidedBlocks.add(tile.pos());
                if(collide){
                    collided.add(collidePool.obtain().set(cx * tilesize, cy * tilesize, tile));

                    for(Point2 p : Geometry.d4){
                        Tile other = world.tile(p.x + cx, p.y + cy);
                        if(other != null && (large || Intersector.intersectSegmentRectangle(v2, v3, other.getBounds(Tmp.r1)))){
                            Building build = other.build;
                            if(build != null && hitter.checkUnderBuild(build, cx * tilesize, cy * tilesize) && collidedBlocks.add(build.pos())){
                                collided.add(collidePool.obtain().set((p.x + cx * tilesize), (p.y + cy) * tilesize, build));
                            }
                        }
                    }
                }
                return false;
            });
        }

        float expand = 3f;

        rect.setPosition(x, y).setSize(v1.x, v1.y).normalize().grow(expand * 2f);
        float x2 = v1.x + x, y2 = v1.y + y;

        Units.nearbyEnemies(team, rect, u -> {
            if(u.checkTarget(hitter.type.collidesAir, hitter.type.collidesGround) && u.hittable()){
                u.hitbox(hitRect);

                Vec2 vec = Geometry.raycastRect(x, y, x2, y2, hitRect.grow(expand * 2));

                if(vec != null){
                    collided.add(collidePool.obtain().set(vec.x, vec.y, u));
                }
            }
        });

        int[] collideCount = {0};
        collided.sort(c -> hitter.dst2(c.x, c.y));
        collided.each(c -> {
            if(hitter.damage > 0 && (pierceCap <= 0 || collideCount[0] < pierceCap)){
                if(c.target instanceof Unit u){
                    u.collision(hitter, c.x, c.y);
                    hitter.collision(u, c.x, c.y);
                    collideCount[0]++;
                }else if(c.target instanceof Building tile){
                    float health = tile.health;

                    if(tile.team != team && tile.collide(hitter)){
                        tile.collision(hitter);
                        hitter.type.hit(hitter, c.x, c.y);
                        collideCount[0]++;
                    }

                    //try to heal the tile
                    if(hitter.type.testCollision(hitter, tile)){
                        hitter.type.hitTile(hitter, tile, c.x, c.y, health, false);
                    }
                }
            }
        });

        collidePool.freeAll(collided);
        collided.clear();
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

       /* mover.move(b, dx * moveDistance, dy * moveDistance,(x,y)-> false);

        if(Math.abs(b.x - endX) < 1e-4f && Math.abs(b.y - endY) < 1e-4f){
            b.set(endX, endY);
        }
*/
        if(Math.abs(b.x - endX) < 1e-4f && Math.abs(b.y - endY) < 1e-4f){
            b.set(endX, endY);
        }else{
            b.set(b.x + dx * moveDistance, b.y + dy * moveDistance);
        }
    }


    public static Bullet anyOtherCreate(Bullet bullet, BulletType bt, Entityc shooter, Entityc owner, Team team, float x, float y, float angle, float damage, float velocityScl, float lifetimeScl, Object data, Mover mover, float aimX, float aimY, @Nullable Teamc target){
        if(bt == null) return null;
        angle += bt.angleOffset + Mathf.range(bt.randomAngleOffset);

        if(!Mathf.chance(bt.createChance)) return null;
        if(bt.ignoreSpawnAngle) angle = 0;
        if(bt.spawnUnit != null){
            //don't spawn units clientside!
            if(!net.client()){
                Unit spawned = bt.spawnUnit.create(team);
                spawned.set(x, y);
                spawned.rotation = angle;
                //immediately spawn at top speed, since it was launched
                if(bt.spawnUnit.missileAccelTime <= 0f){
                    spawned.vel.trns(angle, bt.spawnUnit.speed);
                }
                //assign unit owner
                if(spawned.controller() instanceof MissileAI ai){
                    if(shooter instanceof Unit unit){
                        ai.shooter = unit;
                    }

                    if(shooter instanceof ControlBlock control){
                        ai.shooter = control.unit();
                    }

                }
                spawned.add();
                Units.notifyUnitSpawn(spawned);
            }
            //Since bullet init is never called, handle killing shooter here
            if(bt.killShooter && owner instanceof Healthc h && !h.dead()) h.kill();

            //no bullet returned
            return null;
        }

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
        bullet.set(x, y);
        bullet.lastX = x;
        bullet.lastY = y;
        bullet.lifetime = bt.lifetime * lifetimeScl * (bt.lifeScaleRandMin != 1f || bt.lifeScaleRandMax != 1f ? Mathf.random(bt.lifeScaleRandMin, bt.lifeScaleRandMax) : 1f);
        bullet.data = data;
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

   /* public static float findLaserLength(Bullet b, float angle, float length){
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
*/

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
    }

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

}
