package wh.entities.event.logic;

import arc.flabel.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.actions.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.entities.event.objective.*;
import wh.entities.event.ui.*;

import static mindustry.Vars.*;

/**
 * NH-compat parser:
 * defaultraid <flag> <timer> <alertTime> <raidTime> <bulletDamage> <bulletSpeed> <bulletCount> <inaccuracy>
 */
public class DefaultRaid extends LStatement{
    public String flag = "raid-flag";
    public String timer = "raid-timer";
    public String alertTime = "12";
    public String raidTime = "6";
    public String bulletDamage = "300";
    public String bulletSpeed = "1";
    public String bulletCount = "8";
    public String inaccuracy = "8";

    public DefaultRaid(String[] tokens){
        if(tokens.length > 1) flag = tokens[1];
        if(tokens.length > 2) timer = tokens[2];
        if(tokens.length > 3) alertTime = tokens[3];
        if(tokens.length > 4) raidTime = tokens[4];
        if(tokens.length > 5) bulletDamage = tokens[5];
        if(tokens.length > 6) bulletSpeed = tokens[6];
        if(tokens.length > 7) bulletCount = tokens[7];
        if(tokens.length > 8) inaccuracy = tokens[8];
    }

    public DefaultRaid(){
    }

    @Override
    public void build(Table table){
        table.table(t -> {
            t.add("Executor Flag: ");
            fields(t, flag, str -> flag = str).width(180f);
        }).left().row();

        table.table(t -> {
            t.add("Timer Name: ");
            fields(t, timer, str -> timer = str).width(180f);
        }).left().row();

        table.table(t -> {
            t.add("Alert(s): ");
            fields(t, alertTime, str -> alertTime = str);
            t.add(" Raid(s): ");
            fields(t, raidTime, str -> raidTime = str);
        }).left().row();

        table.table(t -> {
            t.add("Bullet Damage: ");
            fields(t, bulletDamage, str -> bulletDamage = str);
        }).left().row();

        table.table(t -> {
            t.add("Bullet Speed: ");
            fields(t, bulletSpeed, str -> bulletSpeed = str);
        }).left().row();

        table.table(t -> {
            t.add("Raid Count: ");
            fields(t, bulletCount, str -> bulletCount = str);
        }).left().row();

        table.table(t -> {
            t.add("Inaccuracy Radius(tiles): ");
            fields(t, inaccuracy, str -> inaccuracy = str);
        }).left();
    }

    @Override
    public boolean privileged(){
        return true;
    }

    @Override
    public LCategory category(){
        return WHLogicStatements.autoTriggerCategory;
    }

    @Override
    public void write(StringBuilder builder){
        builder.append("defaultraid").append(" ")
        .append(flag).append(" ").append(timer).append(" ")
        .append(alertTime).append(" ").append(raidTime).append(" ")
        .append(bulletDamage).append(" ").append(bulletSpeed).append(" ")
        .append(bulletCount).append(" ").append(inaccuracy);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder){
        return new DefaultRaidInstruction(
        builder.var(flag), builder.var(timer), builder.var(alertTime), builder.var(raidTime),
        builder.var(bulletDamage), builder.var(bulletSpeed), builder.var(bulletCount), builder.var(inaccuracy)
        );
    }

    public static class DefaultRaidInstruction implements LExecutor.LInstruction{
        public LVar flag;
        public LVar timer;
        public LVar alertTime;
        public LVar raidTime;
        public LVar damage;
        public LVar speed;
        public LVar count;
        public LVar inaccuracy;

        public int raidCounter = 0;
        public float curTime = 0f;
        public boolean alertShown = false;

        private final Vec2 source = new Vec2();
        private final Vec2 target = new Vec2();

        public DefaultRaidInstruction(LVar flag, LVar timer, LVar alertTime, LVar raidTime, LVar damage, LVar speed, LVar count, LVar inaccuracy){
            this.flag = flag;
            this.timer = timer;
            this.alertTime = alertTime;
            this.raidTime = raidTime;
            this.damage = damage;
            this.speed = speed;
            this.count = count;
            this.inaccuracy = inaccuracy;
        }

        @Override
        public void run(LExecutor exec){
            if(state == null || state.rules == null){
                return;
            }

            String flagKey = key(flag);
            if(flagKey.isEmpty() || !state.rules.objectiveFlags.contains(flagKey)){
                exec.counter.numval--;
                exec.yield = true;
                return;
            }

            float alert = Math.max(0f, alertTime.numf());
            float raid = Math.max(0.001f, raidTime.numf());
            float total = alert + raid;

            if(curTime >= total){
                reset(flagKey);
                return;
            }

            exec.counter.numval--;
            exec.yield = true;
            curTime += Time.delta / 60f;

            if(!alertShown){
                showAlert(alert);
            }

            if(curTime > alert){
                float raidTimer = curTime - alert;
                int totalShots = Mathf.round((raidTimer / raid) * Math.max(0, count.numi()) * threatScl());
                int delta = totalShots - raidCounter;
                raidCounter = totalShots;
                for(int i = 0; i < delta; i++){
                    createBullet();
                }
            }
        }

        private void showAlert(float alertSeconds){
            updatePosition();
            alertShown = true;
            raidCounter = 0;

            String timerKey = key(timer);
            if(!timerKey.isEmpty()){
                TriggerObjective.obtain(timerKey).trigger(alertSeconds * Time.toSeconds);
            }

            if(Vars.headless) return;

            ActionContext.cutsceneUI.ensureSetup();
            String txt = "<< Raid: <" + (int)(target.x / tilesize) + ", " + (int)(target.y / tilesize) + "> >>";
            ActionContext.cutsceneUI.textLabel = new FLabel(txt);
            ActionContext.cutsceneUI.textArea.clear();
            ActionContext.cutsceneUI.textArea.add(ActionContext.cutsceneUI.textLabel).pad(4f, 32f, 4f, 32f);
            ActionContext.cutsceneUI.textTable.actions(
            Actions.fadeIn(0.15f),
            Actions.delay(2.4f),
            Actions.fadeOut(0.35f)
            );
            Sounds.uiChat.play();
        }

        private void reset(String flagKey){
            curTime = 0f;
            raidCounter = 0;
            alertShown = false;
            if(!flagKey.isEmpty()){
                state.rules.objectiveFlags.remove(flagKey);
            }
            TriggerObjective objective = TriggerObjective.find(key(timer));
            if(objective != null){
                objective.finish();
            }
        }

        private void updatePosition(){
            if(Vars.spawner != null){
                var spawns = Vars.spawner.getSpawns();
                if(spawns != null && !spawns.isEmpty()){
                    Tile tile = spawns.random();
                    source.set(tile.worldx(), tile.worldy());
                }else{
                    source.set(Vars.world.unitWidth() * 0.5f, Vars.world.unitHeight() * 0.5f);
                }
            }else{
                source.set(Vars.world.unitWidth() * 0.5f, Vars.world.unitHeight() * 0.5f);
            }

            float wx = Mathf.random(0f, Vars.world.unitWidth());
            float wy = Mathf.random(0f, Vars.world.unitHeight());
            BlockFlag flag = chooseFlag();
            Building building = Geometry.findClosest(wx, wy, Vars.indexer.getEnemy(state.rules.waveTeam, flag));
            if(building == null){
                Team def = state.rules.defaultTeam;
                building = def == null ? null : def.core();
            }
            if(building != null){
                target.set(building.x, building.y);
            }else{
                target.set(Vars.world.unitWidth() * 0.5f, Vars.world.unitHeight() * 0.5f);
            }
        }

        private BlockFlag chooseFlag(){
            float v = Mathf.random(10f);
            if(v < 3f) return BlockFlag.turret;
            if(v < 6f) return BlockFlag.generator;
            if(v < 9f) return BlockFlag.factory;
            return BlockFlag.core;
        }

        private float threatScl(){
            Team team = state.rules.defaultTeam;
            if(team == null) return 1f;
            float unitCount = team.data() == null ? 0f : team.data().units.size;
            float coreCount = team.cores() == null ? 0f : team.cores().size;
            float score = Math.max(1f, unitCount + coreCount * 5f);
            return Mathf.sqrt(score);
        }

        private void createBullet(){
            BulletType bullet = WHBullets.airRaiderMissile;
            if(bullet == null) return;

            float spread = Math.max(0f, inaccuracy.numf()) * tilesize;
            Tmp.v1.trns(Mathf.random(360f), Mathf.random(spread));

            float sx = source.x;
            float sy = source.y;
            float tx = target.x;
            float ty = target.y;
            float dst = Mathf.dst(sx, sy, tx, ty);
            float ang = Angles.angle(sx, sy, tx, ty);

            float speedScl = Math.max(0.1f, speed.numf());
            float lifetimeScl = dst / Math.max(0.0001f, bullet.speed * bullet.lifetime * speedScl);
            float dmg = Math.max(0f, damage.numf()) * threatScl();

            Team shootTeam = state.rules.waveTeam;
            if(shootTeam == null) shootTeam = state.rules.defaultTeam;
            if(shootTeam == null) return;

            Call.createBullet(bullet, shootTeam, sx + Tmp.v1.x, sy + Tmp.v1.y, ang, dmg, speedScl, lifetimeScl);
        }

        private String key(LVar value){
            if(value == null) return "";
            Object raw = value.obj();
            if(raw instanceof String s){
                String out = s.trim();
                if(!out.isEmpty()) return out;
            }
            if(value.name == null) return "";
            return value.name.trim();
        }
    }
}
