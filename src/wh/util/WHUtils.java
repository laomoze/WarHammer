package wh.util;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import org.jetbrains.annotations.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public final class WHUtils {
    public static final Rand rand = new Rand(0);

    private static Tile tileParma;
    private static Posc result;
    private static float cdist;
    private static int idx;
    private static final Vec2 tV = new Vec2(), tV2 = new Vec2(), tV3 = new Vec2();
    private static final IntSet collidedBlocks = new IntSet();
    private static final Rect rect = new Rect(), hitRect = new Rect();
    private static final Seq<Tile> tiles = new Seq<>();
    private static final Seq<Unit> units = new Seq<>();
    private static Building tmpBuilding;
    private static Unit tmpUnit;
    private static boolean hit, hitB;

    private WHUtils() {}

    @Contract(pure = true)
    public static int reverse(int rotation) {
        return switch (rotation) {
            case 0 -> 2; case 2 -> 0; case 1 -> 3; case 3 -> 1;
            default -> throw new IllegalStateException("Unexpected value: " + rotation);
        };
    }

    public static TextureRegion[][] splitLayers(String name, int size, int layerCount) {
        TextureRegion[][] layers = new TextureRegion[layerCount][];

        for (int i = 0; i < layerCount; i++) {
            layers[i] = split(name, size, i);
        }
        return layers;
    }

    public static TextureRegion[] split(String name, int size, int layer) {
        TextureRegion textures = atlas.find(name);
        int margin = 0;
        int countX = textures.width / size;
        TextureRegion[] tiles = new TextureRegion[countX];

        for (int i = 0; i < countX; i++) {
            tiles[i] = new TextureRegion(textures, i * (margin + size), layer * (margin + size), size, size);
        }
        return tiles;
    }

    /**
     * Gets multiple regions inside a {@link TextureRegion}.
     *
     * @param width  The amount of regions horizontally.
     * @param height The amount of regions vertically.
     */
    public static TextureRegion[] split(String name, int size, int width, int height) {
        TextureRegion textures = atlas.find(name);
        int textureSize = width * height;
        TextureRegion[] regions = new TextureRegion[textureSize];

        float tileWidth = (textures.u2 - textures.u) / width;
        float tileHeight = (textures.v2 - textures.v) / height;

        for (int i = 0; i < textureSize; i++) {
            float tileX = ((float) (i % width)) / width;
            float tileY = ((float) (i / width)) / height;
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
    public static int relativeDirection(Building from, Building to) {
        if (from == null || to == null) return -1;
        if (from.x == to.x && from.y > to.y) return (7 - from.rotation) % 4;
        if (from.x == to.x && from.y < to.y) return (5 - from.rotation) % 4;
        if (from.x > to.x && from.y == to.y) return (6 - from.rotation) % 4;
        if (from.x < to.x && from.y == to.y) return (4 - from.rotation) % 4;
        return -1;
    }

    public static void drawTiledFramesBar(float w, float h, float x, float y, Liquid liquid, float alpha) {
        TextureRegion region = renderer.fluidFrames[liquid.gas ? 1 : 0][liquid.getAnimationFrame()];

        Draw.color(liquid.color, liquid.color.a * alpha);
        Draw.rect(region, x + w / 2f, y + h / 2f, w, h);
        Draw.color();
    }

    public static float findLaserLength(Bullet b, float angle, float length) {
        Tmp.v1.trnsExact(angle, length);

        tileParma = null;

        boolean found = World.raycast(b.tileX(), b.tileY(), World.toTile(b.x + Tmp.v1.x), World.toTile(b.y + Tmp.v1.y),
                (x, y) -> (tileParma = world.tile(x, y)) != null && tileParma.team() != b.team && tileParma.block().absorbLasers);

        return found && tileParma != null ? Math.max(6f, b.dst(tileParma.worldx(), tileParma.worldy())) : length;
    }

    public static void collideLine(Bullet hitter, Team team, Effect effect, float x, float y, float angle, float length, boolean large, boolean laser) {
        if (laser) length = findLaserLength(hitter, angle, length);

        collidedBlocks.clear();
        tV.trnsExact(angle, length);

        Intc2 collider = (cx, cy) -> {
            Building tile = world.build(cx, cy);
            boolean collide = tile != null && collidedBlocks.add(tile.pos());

            if (hitter.damage > 0) {
                float health = !collide ? 0 : tile.health;

                if (collide && tile.team != team && tile.collide(hitter)) {
                    tile.collision(hitter);
                    hitter.type.hit(hitter, tile.x, tile.y);
                }

                //try to heal the tile
                if (collide && hitter.type.testCollision(hitter, tile)) {
                    hitter.type.hitTile(hitter, tile, cx * tilesize, cy * tilesize, health, false);
                }
            }
        };

        if (hitter.type.collidesGround) {
            tV2.set(x, y);
            tV3.set(tV2).add(tV);
            World.raycastEachWorld(x, y, tV3.x, tV3.y, (cx, cy) -> {
                collider.get(cx, cy);

                for (Point2 p : Geometry.d4) {
                    Tile other = world.tile(p.x + cx, p.y + cy);
                    if (other != null && (large || Intersector.intersectSegmentRectangle(tV2, tV3, other.getBounds(Tmp.r1)))) {
                        collider.get(cx + p.x, cy + p.y);
                    }
                }
                return false;
            });
        }

        rect.setPosition(x, y).setSize(tV.x, tV.y);
        float x2 = tV.x + x, y2 = tV.y + y;

        if (rect.width < 0) {
            rect.x += rect.width;
            rect.width *= -1;
        }

        if (rect.height < 0) {
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

            if (vec != null && hitter.damage > 0) {
                effect.at(vec.x, vec.y);
                unit.collision(hitter, vec.x, vec.y);
                hitter.collision(unit, vec.x, vec.y);
            }
        };

        units.clear();

        Units.nearbyEnemies(team, rect, unit -> {
            if (unit.checkTarget(hitter.type.collidesAir, hitter.type.collidesGround)) {
                units.add(unit);
            }
        });

        units.sort(unit -> unit.dst2(hitter));
        units.each(cons);
    }

    public static Rand rand(long id) {
        rand.setSeed(id);
        return rand;
    }
}
