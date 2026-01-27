package wh.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
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
        CTrail[] trails = (CTrail[])e.data;

        float width = Mathf.curve(e.fin(), 0, 0.1f) * this.width;

        if(!state.isPaused()){
            float f = e.fout();
            if(f > 0f){
                for(int i = 0; i < trails.length; i++){
                    CTrail trail = trails[i];
                    trailUpdater.update(e, trail, e.x, e.y, width, length, i);
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

        for(CTrail trail : trails){
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

        CTrail[] trails = new CTrail[amount];
        for(int i = 0; i < amount; i++){
            trails[i] = new CTrail(length);
        }

        entity.data = trails;

        if(followParent && data instanceof Posc p){
            entity.parent = p;
            entity.rotWithParent = rotWithParent;
        }
        entity.add();
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
                Draw.rect("circle", x1, y1, w, w, -Mathf.radDeg * lastAngle + 180f);
                if(drawTri) Drawn.tri(x1, y1, w / 1.7f, w * 2.1f, -Mathf.radDeg * lastAngle + 180f);
                if(drawTri) Drawn.tri(x1, y1, w / 1.7f, w * 0.5f, -Mathf.radDeg * lastAngle);
                Draw.reset();
            }
        }
    }
}
