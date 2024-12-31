package wh.util;

import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;

import static arc.Core.*;

@SuppressWarnings("unused")
public final class SpriteUtils {
    public static final int[] atlasIndex44 = {
            0, 2, 10, 8,
            4, 6, 14, 12,
            5, 7, 15, 13,
            1, 3, 11, 9
    };

    public static final int[] atlasIndex412raw = {
            0, 2, 10, 8, 143, 46, 78, 31, 38, 111, 110, 76,
            4, 6, 14, 12, 39, 127, 239, 77, 55, 95, 175, 207,
            5, 7, 15, 13, 23, 191, 223, 141, 63, 255, 240, 205,
            1, 3, 11, 9, 79, 27, 139, 47, 19, 155, 159, 137
    };

    public static final int[] atlasIndex412 = new int[atlasIndex412raw.length];
    public static final IntIntMap atlasIndex412map = new IntIntMap();

    public static final byte[] tileMap = {
            39, 36, 39, 36, 27, 16, 27, 24, 39, 36, 39, 36, 27, 16, 27, 24,
            38, 37, 38, 37, 17, 41, 17, 43, 38, 37, 38, 37, 26, 21, 26, 25,
            39, 36, 39, 36, 27, 16, 27, 24, 39, 36, 39, 36, 27, 16, 27, 24,
            38, 37, 38, 37, 17, 41, 17, 43, 38, 37, 38, 37, 26, 21, 26, 25,
            3, 4, 3, 4, 15, 40, 15, 20, 3, 4, 3, 4, 15, 40, 15, 20,
            5, 28, 5, 28, 29, 10, 29, 23, 5, 28, 5, 28, 31, 11, 31, 32,
            3, 4, 3, 4, 15, 40, 15, 20, 3, 4, 3, 4, 15, 40, 15, 20,
            2, 30, 2, 30, 9, 47, 9, 22, 2, 30, 2, 30, 14, 44, 14, 6,
            39, 36, 39, 36, 27, 16, 27, 24, 39, 36, 39, 36, 27, 16, 27, 24,
            38, 37, 38, 37, 17, 41, 17, 43, 38, 37, 38, 37, 26, 21, 26, 25,
            39, 36, 39, 36, 27, 16, 27, 24, 39, 36, 39, 36, 27, 16, 27, 24,
            38, 37, 38, 37, 17, 41, 17, 43, 38, 37, 38, 37, 26, 21, 26, 25,
            3, 0, 3, 0, 15, 42, 15, 12, 3, 0, 3, 0, 15, 42, 15, 12,
            5, 8, 5, 8, 29, 35, 29, 33, 5, 8, 5, 8, 31, 34, 31, 7,
            3, 0, 3, 0, 15, 42, 15, 12, 3, 0, 3, 0, 15, 42, 15, 12,
            2, 1, 2, 1, 9, 45, 9, 19, 2, 1, 2, 1, 14, 18, 14, 13
    };

    public static final Point2[] orthogonalPos = {
            new Point2(0, 1),
            new Point2(1, 0),
            new Point2(0, -1),
            new Point2(-1, 0),
    };

    public static final Point2[][] diagonalPos = {
            new Point2[]{new Point2(1, 0), new Point2(1, 1), new Point2(0, 1)},
            new Point2[]{new Point2(1, 0), new Point2(1, -1), new Point2(0, -1)},
            new Point2[]{new Point2(-1, 0), new Point2(-1, -1), new Point2(0, -1)},
            new Point2[]{new Point2(-1, 0), new Point2(-1, 1), new Point2(0, 1)},
    };

    public static final Point2[] proximityPos = {
            new Point2(0, 1),
            new Point2(1, 0),
            new Point2(0, -1),
            new Point2(-1, 0),

            new Point2(1, 1),
            new Point2(1, -1),
            new Point2(-1, -1),
            new Point2(-1, 1),
    };

    public static Rand rand = new Rand();

    static int[][] joinschkdirs = {
            {-1, 1}, {0, 1}, {1, 1},
            {-1, 0}, {1, 0},
            {-1, -1}, {0, -1}, {1, -1},
    };

    public static int[] joinsMap = {//not sure how to format this.
            39, 39, 27, 27, 39, 39, 27, 27, 38, 38, 17, 26, 38, 38, 17, 26, 36,
            36, 16, 16, 36, 36, 24, 24, 37, 37, 41, 21, 37, 37, 43, 25, 39,
            39, 27, 27, 39, 39, 27, 27, 38, 38, 17, 26, 38, 38, 17, 26, 36,
            36, 16, 16, 36, 36, 24, 24, 37, 37, 41, 21, 37, 37, 43, 25, 3,
            3, 15, 15, 3, 3, 15, 15, 5, 5, 29, 31, 5, 5, 29, 31, 4,
            4, 40, 40, 4, 4, 20, 20, 28, 28, 10, 11, 28, 28, 23, 32, 3,
            3, 15, 15, 3, 3, 15, 15, 2, 2, 9, 14, 2, 2, 9, 14, 4,
            4, 40, 40, 4, 4, 20, 20, 30, 30, 47, 44, 30, 30, 22, 6, 39,
            39, 27, 27, 39, 39, 27, 27, 38, 38, 17, 26, 38, 38, 17, 26, 36,
            36, 16, 16, 36, 36, 24, 24, 37, 37, 41, 21, 37, 37, 43, 25, 39,
            39, 27, 27, 39, 39, 27, 27, 38, 38, 17, 26, 38, 38, 17, 26, 36,
            36, 16, 16, 36, 36, 24, 24, 37, 37, 41, 21, 37, 37, 43, 25, 3,
            3, 15, 15, 3, 3, 15, 15, 5, 5, 29, 31, 5, 5, 29, 31, 0,
            0, 42, 42, 0, 0, 12, 12, 8, 8, 35, 34, 8, 8, 33, 7, 3,
            3, 15, 15, 3, 3, 15, 15, 2, 2, 9, 14, 2, 2, 9, 14, 0,
            0, 42, 42, 0, 0, 12, 12, 1, 1, 45, 18, 1, 1, 19, 13
    };

    static {
        Integer[] indices = new Integer[atlasIndex412raw.length];
        for (int i = 0; i < atlasIndex412raw.length; i++) {
            indices[i] = i;
        }

        for (int i = 1; i < indices.length; i++) {
            int key = indices[i];
            int keyValue = atlasIndex412raw[key];
            int j = i - 1;

            while (j >= 0 && atlasIndex412raw[indices[j]] > keyValue) {
                indices[j + 1] = indices[j];
                j = j - 1;
            }
            indices[j + 1] = key;
        }

        for (int i = 0; i < indices.length; i++) {
            atlasIndex412[indices[i]] = i;
        }

        for (int i = 0; i < atlasIndex412raw.length; i++) {
            atlasIndex412map.put(atlasIndex412raw[i], atlasIndex412[i]);
        }
    }

    /** SpriteUtils should not be instantiated. */
    private SpriteUtils() {}

    public static TextureRegion[] jSplit(String name, int tileWidth, int tileHeight) {
        return jSplit(name, tileWidth, tileHeight, 0);
    }

    public static TextureRegion[] jSplit(String name, int tileWidth, int tileHeight, int pad) {
        return jSplit(name, tileWidth, tileHeight, pad, null);
    }

    public static TextureRegion[] jSplit(String name, int tileWidth, int tileHeight, int pad, int[] indexMap) {
        TextureRegion region = atlas.find(name);

        int x = region.getX();
        int y = region.getY();
        int width = region.width;
        int height = region.height;

        int pWidth = tileWidth + pad * 2;
        int pHeight = tileHeight + pad * 2;

        int sw = width / pWidth;
        int sh = height / pHeight;

        int startX = x;
        TextureRegion[] tiles = new TextureRegion[sw * sh];
        for (int cy = 0; cy < sh; cy++, y += pHeight) {
            x = startX;
            for (int cx = 0; cx < sw; cx++, x += pWidth) {
                int index = cx + cy * sw;
                if (indexMap != null) {
                    tiles[indexMap[index]] = new TextureRegion(region.texture, x + pad, y + pad, tileWidth, tileHeight);
                } else {
                    tiles[index] = new TextureRegion(region.texture, x + pad, y + pad, tileWidth, tileHeight);
                }
            }
        }

        return tiles;
    }

    /**
     * Rotate one {@link Pixmap} by a multiple of 90 degrees. This method does not change the original pixmap and returns a copy.
     *
     * @param target The target pixmap to be rotated.
     * @param rotate Rotation angle coefficient, the actual rotation angle is 90 * rotate.
     * @return A rotated pixmap copy.
     */
    public static Pixmap rotatePixmap90(Pixmap target, int rotate) {
        Pixmap res = new Pixmap(target.width, target.height);

        for (int i = 0; i < target.width; i++) {
            for (int j = 0; j < target.height; j++) {
                int c = target.get(i, j);
                switch (Mathf.mod(-rotate, 4)) {
                    case 0 -> res.set(i, j, c);
                    case 1 -> res.set(target.width - j - 1, i, c);
                    case 2 -> res.set(target.width - i - 1, target.height - j - 1, c);
                    case 3 -> res.set(j, target.height - i - 1, c);
                }
            }
        }

        return res;
    }

    /** reads every single pixel on a textureRegion from bottom left to top right. */
    public static void readTexturePixels(PixmapRegion pixmap, Intc2 cons) {
        for (int j = 0; j < pixmap.height; j++) {
            for (int i = 0; i < pixmap.width; i++) {
                cons.get(pixmap.get(i, j), i + pixmap.width * (pixmap.height - 1 - j));
            }
        }
    }

    /**
     * Original code from Project Unity
     * Author: @Xeloboyo
     * Creates connections for blocks
     */
    public static TextureRegion[] getRegions(TextureRegion region, int w, int h, int tilesize) {
        int size = w * h;
        TextureRegion[] regions = new TextureRegion[size];

        float tileW = (region.u2 - region.u) / w;
        float tileH = (region.v2 - region.v) / h;

        for (int i = 0; i < size; i++) {
            float tileX = ((float) (i % w)) / w;
            float tileY = ((float) (i / w)) / h;
            TextureRegion reg = new TextureRegion(region);

            //start coordinate
            reg.u = Mathf.map(tileX, 0f, 1f, reg.u, reg.u2) + tileW * 0.01f;
            reg.v = Mathf.map(tileY, 0f, 1f, reg.v, reg.v2) + tileH * 0.01f;
            //end coordinate
            reg.u2 = reg.u + tileW * 0.98f;
            reg.v2 = reg.v + tileH * 0.98f;

            reg.width = reg.height = tilesize;

            regions[i] = reg;
        }
        return regions;
    }

    public static <T> int getMaskIndex(T[][] map, int x, int y, Boolf<T> canConnect) {
        int index = 0, ax, ay;
        T t;

        for (int i = 0; i < joinschkdirs.length; i++) {
            ax = joinschkdirs[i][0] + x;
            ay = joinschkdirs[i][1] + y;
            t = null;

            if (ax >= 0 && ay >= 0 && ax < map.length && ay < map[0].length) {
                t = map[ax][ay];
            }

            index += canConnect.get(t) ? (1 << i) : 0;
        }

        return index;
    }

    public static <T> int getTilingIndex(T[][] map, int x, int y, Boolf<T> canConnect) {
        return joinsMap[getMaskIndex(map, x, y, canConnect)];
    }

    public static void bubbles(int seed, float x, float y, int bubblesAmount, float bubblesSize, float baseLife, float baseSize) {
        rand.setSeed(seed);

        for (int i = 0; i < bubblesAmount; i++) {
            float
                    angle = rand.random(360f),
                    fin = (rand.random(0.8f) * (Time.time / baseLife)) % rand.random(0.1f, 0.6f),
                    len = rand.random(baseSize / 2f, baseSize) / fin,

                    trnsx = x + Angles.trnsx(angle, len, rand.random(baseSize / 4f, baseSize / 4f)),
                    trnsy = y + Angles.trnsy(angle, len, rand.random(baseSize / 4f, baseSize / 4f));

            Fill.poly(trnsx, trnsy, 18, Interp.sine.apply(fin * 3.5f) * bubblesSize);
        }
    }

    public static void l(float layer) {
        Draw.z(layer);
    };
}
