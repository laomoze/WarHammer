package wh.entities.event.mapmarker;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.game.*;
import wh.content.*;
import wh.entities.event.objective.*;
import wh.graphics.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import static mindustry.Vars.*;

/**
 * NH-style raid marker bound to raid/trigger timer objectives.
 */
public class RaidIndicator extends MapObjectives.PosMarker{
    public Vec2 source = new Vec2();
    public Vec2 target = new Vec2();
    public int teamID = Team.crux.id;
    public int icon = 0;
    public float radius = 50f;
    public String timerName = "event-timer";
    public boolean enabled = false;

    public RaidIndicator(String name){
        timerName = name;
    }

    public RaidIndicator(){
    }

    public TextureRegion icon(){
        TextureRegion raid = WHContent.raid == null ? WHContent.fleet : WHContent.raid;
        return switch(icon){
            case 1 -> raid;
            case 2 -> WHContent.fleet;
            case 3 -> WHContent.objective;
            default -> WHContent.objective;
        };
    }

    public RaidIndicator init(int teamID, int icon, float radius, String timerName){
        this.teamID = teamID;
        this.icon = icon;
        this.radius = radius;
        this.timerName = timerName;
        this.enabled = true;
        return this;
    }

    public RaidIndicator setPosition(Vec2 source, Vec2 target){
        this.source.set(source);
        this.target.set(target);
        return this;
    }

    public void clear(){
        enabled = false;
    }

    @Override
    public void draw(float scaleFactor){
        if(!enabled) return;
        draw();
        drawArrow();
    }

    public void draw(){
        if(!enabled || state == null || state.rules == null) return;

        Team team = Team.get(teamID);
        float fin = progress();

        Draw.blend(Blending.additive);
        Draw.z(drawLayer);
        Draw.color(team.color, Color.white, 0.075f);
        Draw.alpha(0.65f);

        float f = Interp.pow3Out.apply(Mathf.curve(1f - fin, 0f, 0.01f));
        TextureRegion region = icon();
        if(region != null){
            Draw.rect(region, target, region.width * f * Draw.scl, region.height * f * Draw.scl, 0f);
        }

        Lines.stroke(5f * f);
        Lines.circle(target.x, target.y, radius * (1f + Mathf.absin(4f, 0.055f)));
        Drawn.circlePercent(target.x, target.y, radius * 0.875f, fin, 0f);

        Draw.reset();
        Draw.blend();
    }

    public void drawArrow(){
        if(!enabled || WHContent.arrowRegion == null) return;

        float ang = source.angleTo(target);

        Draw.z(drawLayer);
        Draw.color(Team.get(teamID).color, Color.white, 0.075f);
        Draw.blend(Blending.additive);

        float size = Math.max(WHContent.arrowRegion.height, 0.0001f);
        int count = Math.max(0, (int)(radius / size * tilesize));

        for(int i = 0; i < count; i++){
            float s = (1f - ((Time.time + 25f * i) % 100f) / 100f) * scale() * Draw.scl * 1.75f;
            Tmp.v1.trns(ang + 180f, 36f + 12f * i).add(target);
            Draw.rect(WHContent.arrowRegion, Tmp.v1, size * s, size * s, ang - 90f);
        }

        Draw.blend();
    }

    public float progress(){
        if(!enabled || state == null || state.rules == null) return 0f;

        AtomicReference<Float> progress = new AtomicReference<>(0f);
        state.rules.objectives.each(mapObjective -> {
            if(mapObjective instanceof RaidEventObjective obj && Objects.equals(obj.key, timerName)){
                progress.set(Mathf.clamp(obj.getCountup() / Math.max(1f, obj.duration)));
            }
        });
        return progress.get();
    }

    public float scale(){
        return Interp.pow3Out.apply(Mathf.curve(1f - progress(), 0f, 0.05f));
    }
}
