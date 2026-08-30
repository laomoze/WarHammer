package wh.entities.event.logic;

import arc.flabel.FLabel;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Vec2;
import arc.scene.actions.Actions;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Sounds;
import mindustry.logic.*;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import wh.content.WHBullets;
import wh.entities.event.logic.actionLogic.BusLogicInstruction;
import wh.entities.event.mapmarker.RaidIndicator;
import wh.entities.event.objective.RaidEventObjective;
import wh.entities.event.ui.ActionContext;
import wh.net.WHCall;
import wh.util.WeightedRandom;
import wh.util.struct.WeightedOption;

import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;

/**
 * WH parser (prefixed to avoid mod conflicts):
 * wh-defaultraids <flag> <timer> <alertTime> <raidTime> <bulletDamage> <bulletSpeed> <bulletCount> <inaccuracy>
 */
public class DefaultRaids extends LStatement{
    public String flag = "turret";
    public String timer = "event-timer";
    public String alertTime = "30";
    public String raidTime = "5";
    public String bulletDamage = "200";
    public String bulletSpeed = "1";
    public String bulletCount = "2";
    public String inaccuracy = "10";

    public DefaultRaids(String[] tokens){
        if(tokens.length > 1) flag = tokens[1];
        if(tokens.length > 2) timer = tokens[2];
        if(tokens.length > 3) alertTime = tokens[3];
        if(tokens.length > 4) raidTime = tokens[4];
        if(tokens.length > 5) bulletDamage = tokens[5];
        if(tokens.length > 6) bulletSpeed = tokens[6];
        if(tokens.length > 7) bulletCount = tokens[7];
        if(tokens.length > 8) inaccuracy = tokens[8];
    }

    public DefaultRaids(){
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
        builder.append("wh-defaultraids").append(" ")
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

    public static class DefaultRaidInstruction extends BusLogicInstruction {
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
        public boolean iconShown = false;
        public boolean labelShown = false;
        public int threatLevel = 1;
        /** prevent ungated raids from auto-looping forever after first completion */
        public boolean oneShotFinished = false;

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
        protected boolean canStart() {
            if(state == null || state.rules == null){
                return false;
            }

            String flagKey = key(flag);
            boolean gated = !flagKey.isEmpty() && !flagKey.equalsIgnoreCase("null");
            if(!gated && oneShotFinished){
                return false;
            }
            if(gated && !state.rules.objectiveFlags.contains(flagKey)){
                return false;
            }

            return true;
        }

        @Override
        protected boolean canUpdate() {
            if (state == null || state.rules == null) return false;

            String flagKey = key(flag);
            boolean gated = !flagKey.isEmpty() && !flagKey.equalsIgnoreCase("null");
            return !gated || state.rules.objectiveFlags.contains(flagKey);
        }

        @Override
        protected boolean updateAction() {
            String flagKey = key(flag);
            boolean gated = !flagKey.isEmpty() && !flagKey.equalsIgnoreCase("null");

            float alert = Math.max(0f, alertTime.numf());
            float raid = Math.max(0.001f, raidTime.numf());
            float total = alert + raid;

            if(curTime >= total){
                reset(flagKey, gated);
                return true;
            }

            curTime += Time.delta / 60f;

            if(!iconShown){
                showAlert(alert);
            }

            if(curTime > alert){
                if(!labelShown){
                    showLabel();
                }

                float raidTimer = curTime - alert;
                int totalShots = Mathf.round((raidTimer / raid) * Math.max(0, count.numi()) * threatScl());
                int delta = totalShots - raidCounter;
                raidCounter = totalShots;
                for(int i = 0; i < delta; i++){
                    createBullet();
                }
            }

            return false;
        }

        private void showAlert(float alertSeconds){
            updatePosition();
            iconShown = true;
            raidCounter = 0;

            WHCall.warnHudPacket(key(timer), "Raid", alertSeconds, inaccuracy.numf(), source.x, source.y, target.x, target.y);
        }

        private void reset(String flagKey, boolean gated){
            curTime = 0f;
            raidCounter = 0;
            iconShown = false;
            labelShown = false;
            if(gated && !flagKey.isEmpty()){
                state.rules.objectiveFlags.remove(flagKey);
            }
            RaidEventObjective objective = RaidEventObjective.find(key(timer));
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

            AtomicReference<BlockFlag> targetFlag = new AtomicReference<>(BlockFlag.core);
            WeightedRandom.random(
            new WeightedOption(3f, () -> targetFlag.set(BlockFlag.turret)),
            new WeightedOption(3f, () -> targetFlag.set(BlockFlag.generator)),
            new WeightedOption(3f, () -> targetFlag.set(BlockFlag.factory)),
            new WeightedOption(2f, () -> targetFlag.set(BlockFlag.storage)),
            new WeightedOption(2f, () -> targetFlag.set(BlockFlag.drill)),
            new WeightedOption(1f, () -> targetFlag.set(BlockFlag.repair)),
            new WeightedOption(1f, () -> targetFlag.set(BlockFlag.battery)),
            new WeightedOption(1f, () -> targetFlag.set(BlockFlag.reactor)),
            new WeightedOption(1f, () -> targetFlag.set(BlockFlag.core))
            );

            Building building = Geometry.findClosest(wx, wy, Vars.indexer.getEnemy(state.rules.waveTeam, targetFlag.get()));
            if(building == null){
                Team def = state.rules.defaultTeam;
                building = def == null ? null : def.core();
            }
            if(building != null){
                target.set(building.x, building.y);
            }else{
                target.set(Vars.world.unitWidth() * 0.5f, Vars.world.unitHeight() * 0.5f);
            }

            threatLevel = Math.max(ThreatLevel.getTeamThreat(state.rules.defaultTeam), 1);
        }

        private float threatScl(){
            return Mathf.sqrt(threatLevel);
        }

        private void showLabel(){
            WHCall.alertToastTable(-1, -1, "[#ff7b69]Raid: []<" + (int)(target.x / tilesize) + ", " + (int)(target.y / tilesize) + ">");
            labelShown = true;
        }

        private void createBullet(){
            BulletType bullet = WHBullets.raidBulletType;
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
            float dmg = Math.max(0f, damage.numf()) * threatLevel;

            Team shootTeam = state.rules.waveTeam;
            if(shootTeam == null) shootTeam = state.rules.defaultTeam;
            if(shootTeam == null) return;

            Call.createBullet(bullet, shootTeam, sx + Tmp.v1.x, sy + Tmp.v1.y, ang, dmg, speedScl, lifetimeScl);
            bullet.shootEffect.at(sx + Tmp.v1.x, sy + Tmp.v1.y, ang);
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

    public static void clientAlertHud(String timerName, String text, float time, float range, float sx, float sy, float tx, float ty){
        if(state == null || state.rules == null){
            return;
        }
        if(!(Float.isFinite(time) && Float.isFinite(range) && Float.isFinite(sx) && Float.isFinite(sy) && Float.isFinite(tx) && Float.isFinite(ty))){
            return;
        }

        Team markerTeam = state.rules.waveTeam != null ? state.rules.waveTeam : Team.crux;
        float markerTicks = Math.max(1f, Math.max(0f, time) * Time.toSeconds);
        float markerRadius = Mathf.clamp(Math.max(26f, Math.max(0f, range) * tilesize * 0.45f), 22f, 80f);

        String timerKey = timerName == null ? "" : timerName.trim();
        if(!timerKey.isEmpty()){
            RaidEventObjective objective = RaidEventObjective.obtain(timerKey);
            objective.trigger(markerTicks);

            RaidIndicator indicator = objective.raidIndicator();
            if(indicator != null){
                indicator
                .init(markerTeam.id, resolveHudIcon(text), markerRadius, timerKey)
                .setPosition(Tmp.v2.set(sx, sy), Tmp.v3.set(tx, ty));
            }
        }

        if(Vars.headless) return;

        ActionContext.cutsceneUI.ensureSetup();
        String txt = "<<" + text + ": <" + (int)(tx / tilesize) + ", " + (int)(ty / tilesize) + "> >>";
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

    private static int resolveHudIcon(String text){
        if(text == null) return -1;
        String t = text.trim();
        if(t.equalsIgnoreCase("Airborne")) return 2;
        if(t.equalsIgnoreCase("Raid")) return -1;
        return -1;
    }
}
