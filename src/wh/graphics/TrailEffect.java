package wh.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.RotBlock;
import wh.content.*;

import java.util.*;

import static mindustry.Vars.state;

public class TrailEffect extends Effect{
    /** How many trails to create. */
    public int amount = 10;
    /** How many points long each trail is. */
    public int length = 10;
    /** Radius of the trail. */
    public float width = 1;

    public @Nullable Color colorFrom;
    /** If null, uses effect color. */
    public @Nullable Color colorTo;

    public Interp ColorInterp = Interp.linear;

    /** Custom trail movement function */
    public TrailUpdater trailUpdater = (e, trail, x, y, width, length, index) -> {
    };

    public Boolean drawTri = false;


    public TrailEffect(float lifetime, float clipSize){
        this.lifetime = lifetime;
        this.clip = clipSize;
    }


    public TrailEffect(float lifetime, float clipSize, int amount){
        this(lifetime, clipSize);
        this.amount = amount;
    }


    public TrailEffect(float lifetime, float clipSize, int amount, int length, float width){
        this(lifetime, clipSize, amount);
        this.length = length;
        this.width = width;
        this.colorFrom = null;
        this.colorTo = null;
    }

    public TrailEffect(float lifetime, float clipSize, Color colorFrom, Color colorTo, int amount, int length, float width){
        this(lifetime, clipSize, amount);
        this.length = length;
        this.width = width;
        this.colorFrom = colorFrom;
        this.colorTo = colorTo;
    }

    public TrailEffect trailUpdater(TrailUpdater updater){
        this.trailUpdater = updater;
        return this;
    }

    public TrailEffect layer(float l){
        layer = l;
        return this;
    }

    public TrailEffect startDelay(float d){
        startDelay = d;
        return this;
    }

    public TrailEffect colorInterp(Interp e){
        ColorInterp = e;
        return this;
    }

    public TrailEffect drawTri(boolean b){
        drawTri = b;
        return this;
    }

    @Override
    public void render(EffectContainer e){
        if(length <= 0 || width <= 0) return;
        Trail[] trails = new Trail[0];
        float followX = e.x;
        float followY = e.y;
        float followRotation = e.rotation;
        if(e.data instanceof TrailData data){
            trails = data.trails;
            if(data.manualBulletFollow){
                if(!state.isPaused()){
                    updateBulletFollow(data);
                }
                followX = data.followX;
                followY = data.followY;
                followRotation = data.followRotation;
                // Keep container coordinates synced for updater code that reads e.x/e.y directly.
                e.x = followX;
                e.y = followY;
                e.rotation = followRotation;
            }
        }

        float width = Mathf.curve(e.fin(), 0, 0.1f) * this.width;

        if(!state.isPaused()){
            float f = e.fout();
            if(f > 0f){
                for(int i = 0; i < trails.length; i++){
                    Trail trail = trails[i];
                    trailUpdater.update(e, trail, followX, followY, width, length, i);
                }
            }else{
                Arrays.fill(trails, null);
            }
        }

        float l = e.fin(ColorInterp);
        if(colorFrom != null || colorTo != null){
            Tmp.c1.set(colorFrom == null ? e.color : colorFrom).lerp(colorTo == null ? e.color : colorTo, l);
        }else{
            Tmp.c1.set(e.color);
        }

        for(Trail trail : trails){
            if(trail != null){
                trail.draw(Tmp.c1, width);
                trail.drawCap(Tmp.c1, width * WHFx.fout(e.fin(), 0.15f));
            }
        }
    }

    public interface TrailUpdater{
        void update(EffectContainer e, Trail trail, float x, float y, float width, float length, int index);
    }

    @Override
    protected void add(float x, float y, float rotation, Color color, Object data){
        EffectState entity = EffectState.create();
        entity.effect = this;
        entity.rotation = baseRotation + rotation;
        entity.lifetime = lifetime;
        entity.set(x, y);
        entity.color.set(color);

        Trail[] trails = new Trail[amount];
        for(int i = 0; i < amount; i++){
            trails[i] = new CTrail(length);
        }

        float worldRotation = baseRotation + rotation;
        entity.data = new TrailData(trails, data, x, y, worldRotation, followParent, rotWithParent);
        TrailData d = (TrailData)entity.data;
        if(followParent && !d.manualBulletFollow){
            Posc parentPos = resolveFollowParent(d.sourceData);
            if(parentPos != null){
                entity.parent = parentPos;
                boolean parentSupportsRotation = parentPos instanceof Rotc || parentPos instanceof RotBlock;
                entity.rotWithParent = rotWithParent && parentSupportsRotation;
            }
        }
        entity.add();
    }

    private @Nullable Posc resolveFollowParent(Object sourceData){
        if(sourceData instanceof Posc posc){
            return posc;
        }
        return null;
    }

    /**
     * 子弹作为 sourceData 时不再使用 EffectState.parent 跟随，避免子弹回收复用导致的坐标跳变。
     */
    private void updateBulletFollow(TrailData data){
        if(!(data.sourceData instanceof Bullet bullet)) return;
        if(!bullet.isAdded() || bullet.id != data.sourceEntityId) return;

        if(data.keepRotationWithSource){
            float sourceRot = bullet.rotation();
            data.followX = bullet.x + Angles.trnsx(sourceRot + data.offsetPos, data.offsetX, data.offsetY);
            data.followY = bullet.y + Angles.trnsy(sourceRot + data.offsetPos, data.offsetX, data.offsetY);
            data.followRotation = sourceRot + data.offsetRot;
        }else{
            data.followX = bullet.x + data.offsetX;
            data.followY = bullet.y + data.offsetY;
            data.followRotation = data.baseRotation;
        }
    }

    private static class TrailData{
        final Trail[] trails;
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

        TrailData(Trail[] trails, Object sourceData, float effectX, float effectY, float effectRotation, boolean followParent, boolean rotWithParent){
            this.trails = trails;
            this.sourceData = sourceData;
            this.baseRotation = effectRotation;
            this.followX = effectX;
            this.followY = effectY;
            this.followRotation = effectRotation;

            if(followParent && sourceData instanceof Bullet bullet){
                manualBulletFollow = true;
                keepRotationWithSource = rotWithParent;
                sourceEntityId = bullet.id;
                offsetX = effectX - bullet.x;
                offsetY = effectY - bullet.y;
                if(keepRotationWithSource){
                    float sourceRot = bullet.rotation();
                    offsetPos = -sourceRot;
                    offsetRot = effectRotation - sourceRot;
                }else{
                    offsetPos = 0f;
                    offsetRot = 0f;
                }
            }else{
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

    public class CTrail extends Trail{
        public CTrail(int length){
            super(length);
        }

        @Override
        public void drawCap(Color color, float width){
            if(points.size > 0){
                Draw.color(color);
                float[] items = points.items;
                int i = points.size - 3;
                float x1 = items[i], y1 = items[i + 1], w1 = items[i + 2], w = w1 * width / ((float)points.size / 3) * i / 3f * 2f;
                if(w1 <= 0.001f) return;
                Draw.rect("circle-bullet", x1, y1, w, w, -Mathf.radDeg * lastAngle + 180f);
                if(drawTri) Drawn.tri(x1, y1, w / 1.7f, w * 2.1f, -Mathf.radDeg * lastAngle + 180f);
                if(drawTri) Drawn.tri(x1, y1, w / 1.7f, w * 0.5f, -Mathf.radDeg * lastAngle);
                Draw.reset();
            }
        }
    }
}
