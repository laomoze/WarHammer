package wh.entities.event.logic;

import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.logic.*;
import wh.content.*;
import wh.entities.event.objective.*;
import wh.net.*;

import static mindustry.Vars.*;

/**
 * NH-compat parser:
 * raidcontrol <flag> <timer> <alertTime> <raidTime> <team> <type> <count> <sourceX> <sourceY> <targetX> <targetY> <inaccuracy>
 */
public class RaidControl extends LStatement{
    public String flag = "flag";
    public String timer = "event-timer";
    public String alertTime = "10";
    public String raidTime = "5";
    public String team = "@sharded";
    public String type = "0";
    public String count = "10";
    public String sourceX = "sx";
    public String sourceY = "sy";
    public String targetX = "tx";
    public String targetY = "ty";
    public String inaccuracy = "0";

    public RaidControl(String[] tokens){
        if(tokens.length > 1) flag = tokens[1];
        if(tokens.length > 2) timer = tokens[2];
        if(tokens.length > 3) alertTime = tokens[3];
        if(tokens.length > 4) raidTime = tokens[4];
        if(tokens.length > 5) team = tokens[5];
        if(tokens.length > 6) type = tokens[6];
        if(tokens.length > 7) count = tokens[7];
        if(tokens.length > 8) sourceX = tokens[8];
        if(tokens.length > 9) sourceY = tokens[9];
        if(tokens.length > 10) targetX = tokens[10];
        if(tokens.length > 11) targetY = tokens[11];
        if(tokens.length > 12) inaccuracy = tokens[12];
    }

    public RaidControl(){
    }

    @Override
    public void build(Table table){
        float width = 280f;

        table.table(t -> {
            t.add("Objective Flag: ");
            fields(t, flag, str -> flag = str).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Timer Name: ");
            fields(t, timer, str -> timer = str).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Alert Time(s): ");
            fields(t, alertTime, str -> alertTime = str).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Raid Time(s): ");
            fields(t, raidTime, str -> raidTime = str).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Source Team: ");
            fields(t, team, str -> team = str).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Raid Bullet Type: ");
            fields(t, type, str -> type = str).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Raid Count: ");
            fields(t, count, str -> count = str).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Inaccuracy(tiles): ");
            fields(t, inaccuracy, str -> inaccuracy = str).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Source Position(tile): ");
            fields(t, sourceX, str -> sourceX = str).width(120f);
            t.add(", ");
            fields(t, sourceY, str -> sourceY = str).width(120f);
        }).left().row();

        table.table(t -> {
            t.add("Target Position(tile): ");
            fields(t, targetX, str -> targetX = str).width(120f);
            t.add(", ");
            fields(t, targetY, str -> targetY = str).width(120f);
        }).left();
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder){
        return new RaidControlInstruction(
        builder.var(flag), builder.var(timer), builder.var(alertTime), builder.var(raidTime), builder.var(team),
        builder.var(type), builder.var(count), builder.var(sourceX), builder.var(sourceY), builder.var(targetX),
        builder.var(targetY), builder.var(inaccuracy)
        );
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
        builder.append("raidcontrol").append(" ").append(flag).append(" ").append(timer).append(" ")
        .append(alertTime).append(" ").append(raidTime).append(" ")
        .append(team).append(" ").append(type).append(" ").append(count).append(" ")
        .append(sourceX).append(" ").append(sourceY).append(" ").append(targetX).append(" ").append(targetY).append(" ")
        .append(inaccuracy);
    }

    public static class RaidControlInstruction implements LExecutor.LInstruction{
        public LVar flag;
        public LVar timer;
        public LVar alertTime;
        public LVar raidTime;
        public LVar team;
        public LVar type;
        public LVar count;
        public LVar sourceX;
        public LVar sourceY;
        public LVar targetX;
        public LVar targetY;
        public LVar inaccuracy;

        public int raidCounter = 0;
        public float curTime = 0f;
        public boolean alertShown = false;

        public RaidControlInstruction(
        LVar flag, LVar timer, LVar alertTime, LVar raidTime, LVar team, LVar type, LVar count,
        LVar sourceX, LVar sourceY, LVar targetX, LVar targetY, LVar inaccuracy
        ){
            this.flag = flag;
            this.timer = timer;
            this.alertTime = alertTime;
            this.raidTime = raidTime;
            this.team = team;
            this.type = type;
            this.count = count;
            this.sourceX = sourceX;
            this.sourceY = sourceY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.inaccuracy = inaccuracy;
        }

        @Override
        public void run(LExecutor exec){
            if(state == null || state.rules == null){
                return;
            }

            String flagKey = key(flag);
            boolean gated = !flagKey.isEmpty() && !flagKey.equalsIgnoreCase("null");
            if(gated && !state.rules.objectiveFlags.contains(flagKey)){
                exec.counter.numval--;
                exec.yield = true;
                return;
            }

            float alert = Math.max(0f, alertTime.numf());
            float raid = Math.max(0.001f, raidTime.numf());
            float total = alert + raid;
            if(curTime >= total){
                reset(flagKey, gated);
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
                int totalShots = Mathf.round((raidTimer / raid) * Math.max(0, count.numi()));
                int delta = totalShots - raidCounter;
                raidCounter = totalShots;

                for(int i = 0; i < delta; i++){
                    createBullet();
                }
            }
        }

        private void reset(String flagKey, boolean gated){
            curTime = 0f;
            raidCounter = 0;
            alertShown = false;
            if(gated && !flagKey.isEmpty()){
                state.rules.objectiveFlags.remove(flagKey);
            }
            RaidEventObjective objective = RaidEventObjective.find(key(timer));
            if(objective != null){
                objective.finish();
            }
        }

        private void showAlert(float alertSeconds){
            alertShown = true;
            raidCounter = 0;

            String timerKey = key(timer);
            float sx = sourceX.numf() * tilesize;
            float sy = sourceY.numf() * tilesize;
            float tx = targetX.numf() * tilesize;
            float ty = targetY.numf() * tilesize;
            WHCall.warnHudPacket(timerKey, "Raid", alertSeconds, inaccuracy.numf(), sx, sy, tx, ty);
        }

        private void createBullet(){
            BulletType bullet = bulletType();
            Team shootTeam = team.team() != null ? team.team() : state.rules.waveTeam;
            if(bullet == null || shootTeam == null){
                return;
            }

            float spread = Math.max(0f, inaccuracy.numf()) * tilesize;
            Tmp.v1.trns(Mathf.random(360f), Mathf.random(spread));

            float sx = sourceX.numf() * tilesize;
            float sy = sourceY.numf() * tilesize;
            float tx = targetX.numf() * tilesize;
            float ty = targetY.numf() * tilesize;
            float dst = Mathf.dst(sx, sy, tx, ty);
            float ang = Angles.angle(sx, sy, tx, ty);
            float base = Math.max(0.0001f, bullet.speed * bullet.lifetime);
            float lifetimeScl = dst / base;

            Call.createBullet(bullet, shootTeam, sx + Tmp.v1.x, sy + Tmp.v1.y, ang, -1f, 1f, lifetimeScl);
            bullet.shootEffect.at(sx, sy, ang);
        }

        private BulletType bulletType(){
            int typeId = type.numi();
            if(typeId >= 10000){
                BulletType byId = Vars.content.bullet(typeId - 10000);
                if(byId != null) return byId;
            }

            return switch(typeId){
                case 1 -> WHBullets.airRaiderBomb;
                case 2 -> WHBullets.airRaiderMissile;
                case 3 -> WHBullets.CycloneMissleLauncherMissile1;
                case 4 -> WHBullets.CycloneMissleLauncherMissile2;
                case 5 -> WHBullets.CycloneMissleLauncherMissile3;
                case 6 -> WHBullets.SSWordTiSteel;
                case 7 -> WHBullets.SSWordPyratite;
                case 8 -> WHBullets.SSWordSurgeAlloy;
                default -> WHBullets.airRaiderMissile;
            };
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
