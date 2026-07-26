package wh.graphics;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.entities.Effect;
import mindustry.gen.Bullet;
import mindustry.gen.EffectState;
import mindustry.gen.Posc;
import mindustry.gen.Rotc;
import mindustry.graphics.Trail;
import mindustry.world.blocks.RotBlock;
import wh.content.WHFx;
import wh.core.WHSettings;
import wh.util.WHUtils;

public class TrailEffect extends Effect {
    /**
     * How many trails to create.
     */
    public int amount = 10;
    /**
     * How many points long each trail is.
     */
    public int length = 10;
    /**
     * Radius of the trail.
     */
    public float width = 1;

    public @Nullable Color colorFrom;
    /**
     * If null, uses effect color.
     */
    public @Nullable Color colorTo;

    public Interp ColorInterp = Interp.linear;

    /**
     * Custom trail movement function
     */
    public TrailUpdater trailUpdater = (e, trail, x, y, width, length, index) -> {
    };

    public Boolean drawTri = false;


    public TrailEffect(float lifetime, float clipSize) {
        this.lifetime = lifetime;
        this.clip = clipSize;
    }


    public TrailEffect(float lifetime, float clipSize, int amount) {
        this(lifetime, clipSize);
        this.amount = amount;
    }


    public TrailEffect(float lifetime, float clipSize, int amount, int length, float width) {
        this(lifetime, clipSize, amount);
        this.length = length;
        this.width = width;
        this.colorFrom = null;
        this.colorTo = null;
    }

    public TrailEffect(float lifetime, float clipSize, Color colorFrom, Color colorTo, int amount, int length, float width) {
        this(lifetime, clipSize, amount);
        this.length = length;
        this.width = width;
        this.colorFrom = colorFrom;
        this.colorTo = colorTo;
    }

    public TrailEffect trailUpdater(TrailUpdater updater) {
        this.trailUpdater = updater;
        return this;
    }

    public TrailEffect layer(float l) {
        layer = l;
        return this;
    }

    public TrailEffect startDelay(float d) {
        startDelay = d;
        return this;
    }

    public TrailEffect colorInterp(Interp e) {
        ColorInterp = e;
        return this;
    }

    public TrailEffect drawTri(boolean b) {
        drawTri = b;
        return this;
    }

    protected void updateTrails(TrailEffectState state) {
        if (!WHSettings.effectEnabled()) return;
        if (length <= 0 || width <= 0) return;
        if (!(state.data instanceof TrailData data)) return;

        float followX = state.x;
        float followY = state.y;
        float followRotation = state.rotation;
        if (data.manualBulletFollow) {
            updateBulletFollow(data);
            followX = data.followX;
            followY = data.followY;
            followRotation = data.followRotation;
            state.x = followX;
            state.y = followY;
            state.rotation = followRotation;
        }

        float trailWidth = Mathf.curve(state.fin(), 0f, 0.1f) * this.width;
        float f = state.fout();
        if (f > 0.025f) {
            data.container.set(state.id, state.color, state.time, state.lifetime, followRotation, followX, followY, data.sourceData);
            for (int i = 0; i < data.trails.length; i++) {
                CTrail trail = (CTrail) data.trails[i];
                trailUpdater.update(data.container, trail, followX, followY, trailWidth, length, i);
            }
        } else {
            for (int i = 0; i < data.trails.length; i++) {
                CTrail trail = (CTrail) data.trails[i];
                trail.clear();
            }
            /*  Arrays.fill(data.trails, null);*/
        }
    }

    @Override
    public void render(EffectContainer e) {
        if (!WHSettings.effectEnabled()) return;
        if (length <= 0 || width <= 0) return;
        Trail[] trails = new Trail[0];
        if (e.data instanceof TrailData data) {
            trails = data.trails;
        }

        float l = e.fin(ColorInterp);
        if (colorFrom != null || colorTo != null) {
            Tmp.c1.set(colorFrom == null ? e.color : colorFrom).lerp(colorTo == null ? e.color : colorTo, l);
        } else {
            Tmp.c1.set(e.color);
        }

        for (Trail trail : trails) {
            if (trail != null) {
                trail.draw(Tmp.c1, width);
                trail.drawCap(Tmp.c1, width * WHFx.fout(e.fin(), 0.15f));
            }
        }
    }

    public interface TrailUpdater {
        void update(EffectContainer e, CTrail trail, float x, float y, float width, float length, int index);
    }

    @Override
    protected void add(float x, float y, float rotation, Color color, Object data) {
        if (!WHSettings.effectEnabled()) return;
        TrailEffectState entity = TrailEffectState.create();
        entity.effect = this;
        entity.rotation = baseRotation + rotation;
        entity.lifetime = lifetime;
        entity.set(x, y);
        entity.color.set(color);

        Trail[] trails = new Trail[amount];
        for (int i = 0; i < amount; i++) {
            trails[i] = new CTrail(length);
        }

        float worldRotation = baseRotation + rotation;
        entity.data = new TrailData(trails, data, x, y, worldRotation, followParent, rotWithParent);
        TrailData d = (TrailData) entity.data;
        if (followParent && !d.manualBulletFollow) {
            Posc parentPos = resolveFollowParent(d.sourceData);
            if (parentPos != null) {
                entity.parent = parentPos;
                boolean parentSupportsRotation = parentPos instanceof Rotc || parentPos instanceof RotBlock;
                entity.rotWithParent = rotWithParent && parentSupportsRotation;
            }
        }
        entity.add();
    }

    private @Nullable Posc resolveFollowParent(Object sourceData) {
        if (sourceData instanceof Posc posc) {
            return posc;
        }
        return null;
    }

    /**
     * 子弹作为 sourceData 时不再使用 EffectState.parent 跟随，避免子弹回收复用导致的坐标跳变。
     */
    private void updateBulletFollow(TrailData data) {
        if (!(data.sourceData instanceof Bullet bullet)) return;
        if (!bullet.isAdded() || bullet.id != data.sourceEntityId) return;

        if (data.keepRotationWithSource) {
            float sourceRot = bullet.rotation();
            data.followX = bullet.x + Angles.trnsx(sourceRot + data.offsetPos, data.offsetX, data.offsetY);
            data.followY = bullet.y + Angles.trnsy(sourceRot + data.offsetPos, data.offsetX, data.offsetY);
            data.followRotation = sourceRot + data.offsetRot;
        } else {
            data.followX = bullet.x + data.offsetX;
            data.followY = bullet.y + data.offsetY;
            data.followRotation = data.baseRotation;
        }
    }

    private static class TrailData {
        final Trail[] trails;
        final EffectContainer container = new EffectContainer();
        final Object sourceData;
        final boolean manualBulletFollow;
        final boolean keepRotationWithSource;
        final int sourceEntityId;
        final float offsetX;
        final float offsetY;
        final float offsetPos;
        final float offsetRot;
        final float baseRotation;
        float followX;
        float followY;
        float followRotation;

        TrailData(Trail[] trails, Object sourceData, float effectX, float effectY, float effectRotation, boolean followParent, boolean rotWithParent) {
            this.trails = trails;
            this.sourceData = sourceData;
            this.baseRotation = effectRotation;
            this.followX = effectX;
            this.followY = effectY;
            this.followRotation = effectRotation;

            if (followParent && sourceData instanceof Bullet bullet) {
                manualBulletFollow = true;
                keepRotationWithSource = rotWithParent;
                sourceEntityId = bullet.id;
                offsetX = effectX - bullet.x;
                offsetY = effectY - bullet.y;
                if (keepRotationWithSource) {
                    float sourceRot = bullet.rotation();
                    offsetPos = -sourceRot;
                    offsetRot = effectRotation - sourceRot;
                } else {
                    offsetPos = 0f;
                    offsetRot = 0f;
                }
            } else {
                manualBulletFollow = false;
                keepRotationWithSource = false;
                sourceEntityId = -1;
                offsetX = 0f;
                offsetY = 0f;
                offsetPos = 0f;
                offsetRot = 0f;
            }
        }
    }

    public static class TrailEffectState extends EffectState {
        public static TrailEffectState create() {
            return Pools.obtain(TrailEffectState.class, TrailEffectState::new);
        }

        @Override
        public void update() {
            super.update();
            if (effect instanceof TrailEffect trail && isAdded()) {
                trail.updateTrails(this);
            }
        }
    }

    public class CTrail extends Trail {
        public float lastZ = 0f;

        public CTrail(int length) {
            super(length);
        }

        @Override
        public void update(float x, float y, float width) {
            update(x, y, 0f, width);
        }

     /*   public void update(float x, float y, float z, float width) {
            int count = (int) (counter += Time.delta);
            counter -= count;

            if (count > 0) {
                int toRemove = points.size + (count - 1 - length) * 4;
                if (toRemove > 0 && points.size > 0) {
                    points.removeRange(0, Math.min(toRemove - 1, points.size - 1));
                }

                if (count == 1 || lastX == -1f) {
                    points.add(x, y, z, width);
                } else {
                    for (int i = 0; i < count; i++) {
                        float f = (i + 1f) / count;
                        points.add(Mathf.lerp(lastX, x, f), Mathf.lerp(lastY, y, f), Mathf.lerp(lastZ, z, f), Mathf.lerp(lastW, width, f));
                    }
                }
            }

            lastAngle = -Angles.angleRad(x, y, lastX, lastY);
            lastX = x;
            lastY = y;
            lastZ = z;
            lastW = width;
        }*/

        public void update(float x, float y, float z, float width) {
            int count = (int) (counter += Time.delta);
            counter -= count;

            if (count > 0) {
                int toRemove = points.size + (count - 1 - length) * 4;
                if (toRemove > 0 && points.size > 0) {
                    points.removeRange(0, Math.min(toRemove - 1, points.size - 1));
                }

                if (count == 1 || lastX == -1f) {
                    points.add(x, y, z, width);
                } else {
                    for (int i = 0; i < count; i++) {
                        float f = (i + 1f) / count;
                        points.add(
                                Mathf.lerp(lastX, x, f),
                                Mathf.lerp(lastY, y, f),
                                Mathf.lerp(lastZ, z, f),
                                Mathf.lerp(lastW, width, f)
                        );
                    }
                }
            }

            if (lastX != -1f && lastY != -1f) {
                float dx = x - lastX, dy = y - lastY;
                if (dx * dx + dy * dy > 0.0001f) {
                    lastAngle = Mathf.atan2(dy, dx);
                }
            }

            lastX = x;
            lastY = y;
            lastZ = z;
            lastW = width;
        }


        @Override
        public void draw(Color color, float width) {
            if (points.size >= 8) {
                Draw.color(color);
                float[] items = points.items;
                float lastAngle = this.lastAngle;
                float size = width / (points.size / 4f);

                for (int i = 0; i < points.size; i += 4) {
                    float x1 = items[i], y1 = items[i + 1], z1 = items[i + 2], w1 = items[i + 3];
                    float x2, y2, z2, w2;

                    if (i < points.size - 4) {
                        x2 = items[i + 4];
                        y2 = items[i + 5];
                        z2 = items[i + 6];
                        w2 = items[i + 7];
                    } else {
                        x2 = lastX;
                        y2 = lastY;
                        z2 = lastZ;
                        w2 = lastW;
                    }

                    if (w1 <= 0.001f || w2 <= 0.001f) continue;

                    Tmp.v1.set(x1, y1);
                    Tmp.v2.set(x2, y2);
                    float p1 = Math.max(z1, 0f);
                    float p2 = Math.max(z2, 0f);
                    WHUtils.getParallaxFrom(Tmp.v1, Core.camera.position, p1 / 32f);
                    WHUtils.getParallaxFrom(Tmp.v2, Core.camera.position, p2 / 32f);
                    if (Tmp.v1.dst2(Tmp.v2) < 0.025f) continue;

                    float z2a = -Angles.angleRad(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
                    float z1a = i == 0 ? z2a : lastAngle;

                    float cx = Mathf.sin(z1a) * i / 4f * size * w1;
                    float cy = Mathf.cos(z1a) * i / 4f * size * w1;
                    float nx = Mathf.sin(z2a) * (i / 4f + 1f) * size * w2;
                    float ny = Mathf.cos(z2a) * (i / 4f + 1f) * size * w2;

                    Fill.quad(
                            Tmp.v1.x - cx, Tmp.v1.y - cy,
                            Tmp.v1.x + cx, Tmp.v1.y + cy,
                            Tmp.v2.x + nx, Tmp.v2.y + ny,
                            Tmp.v2.x - nx, Tmp.v2.y - ny
                    );
                    lastAngle = z2a;
                }
                Draw.reset();
            }
        }

        /*   @Override
           public void drawCap(Color color, float width){
               if (points.size >= 4) {
                   Draw.color(color);
                   float[] items = points.items;
                   int i = points.size - 4;
                   float x1 = items[i], y1 = items[i + 1], z1 = items[i + 2], w1 = items[i + 3];
                   float w = w1 * width / ((float) points.size / 4f) * i / 4f * 2f;
                   if(w1 <= 0.001f) return;
                   Tmp.v1.set(x1, y1);
                   WHUtils.getParallaxFrom(Tmp.v1, Core.camera.position, Math.max(z1, 0f) / 32f);
                   float angle = Mathf.radDeg * lastAngle;

                   Draw.rect("circle-bullet", Tmp.v1.x, Tmp.v1.y, w, w, angle + 180f);
                   if (drawTri) Drawn.tri(Tmp.v1.x, Tmp.v1.y, w / 1.7f, w * 2.1f, angle + 180f);
                   if (drawTri) Drawn.tri(Tmp.v1.x, Tmp.v1.y, w / 1.7f, w * 0.5f, angle);
                   Draw.reset();
               }
           }*/
        @Override
        public void drawCap(Color color, float width) {
            if (points.size < 8) return;

            Draw.color(color);
            float[] items = points.items;

            int i1 = points.size - 4;
            int i0 = points.size - 8;

            float x0 = items[i0], y0 = items[i0 + 1], z0 = items[i0 + 2], w0 = items[i0 + 3];
            float x1 = items[i1], y1 = items[i1 + 1], z1 = items[i1 + 2], w1 = items[i1 + 3];

            float pointScale = width / Math.max(length, 1f);
            float w = w1 * pointScale * (i1 / 4f) * 2f;
            if (w1 <= 0.001f) return;

            Tmp.v2.set(x0, y0);
            Tmp.v1.set(x1, y1);

            WHUtils.getParallaxFrom(Tmp.v2, Core.camera.position, Math.max(z0, 0f) / 32f);
            WHUtils.getParallaxFrom(Tmp.v1, Core.camera.position, Math.max(z1, 0f) / 32f);

            float angle = Angles.angle(Tmp.v2.x, Tmp.v2.y, Tmp.v1.x, Tmp.v1.y);

            /* Draw.rect("circle-bullet", Tmp.v1.x, Tmp.v1.y, w, w, angle + 180f);*/
            if (!drawTri) {
                Fill.circle(Tmp.v1.x, Tmp.v1.y, w / 2);
            } else {
                Drawn.tri(Tmp.v1.x, Tmp.v1.y, w / 1.7f, w * 2.1f, angle);
                Drawn.tri(Tmp.v1.x, Tmp.v1.y, w / 1.7f, w * 0.5f, angle + 180f);
            }

            Draw.reset();
        }

    }
}
