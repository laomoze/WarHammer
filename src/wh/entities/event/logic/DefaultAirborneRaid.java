package wh.entities.event.logic;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import wh.entities.*;
import wh.entities.event.logic.actionLogic.*;
import wh.entities.event.objective.*;
import wh.net.*;
import wh.ui.*;
import wh.util.*;
import wh.util.struct.*;

import java.util.concurrent.atomic.*;

import static mindustry.Vars.*;

/**
 * 默认空降袭击逻辑：
 * defaultairraid <flag> <timer> <alertTime> <raidTime> <spawnerCount> <spawnerInterval> <spawnUnits> <inaccuracy>
 * <p>
 * spawnUnits 格式：
 * - 单个空降器编组： "@alpha,@beta"
 * - 多个空降器编组： "@alpha,@beta;@gamma;@delta,@epsilon"
 * 每个 ';' 分段对应一次 AirborneSpawner 投放；当投放次数超过分组数量时用第一组填充。
 */
public class DefaultAirborneRaid extends LStatement{
    private static final int maxUnitsPerGroup = 4;

    public String flag = "turret";
    public String timer = "air-born-timer";
    public String alertTime = "30";
    public String raidTime = "5";
    public String spawnerCount = "2";
    public String spawnerInterval = "0.8";
    public String spawnUnits = "@alpha";
    public String inaccuracy = "30";

    public DefaultAirborneRaid(String[] tokens){
        if(tokens.length > 1) flag = tokens[1];
        if(tokens.length > 2) timer = tokens[2];
        if(tokens.length > 3) alertTime = tokens[3];
        if(tokens.length > 4) raidTime = tokens[4];
        if(tokens.length > 5) spawnerCount = tokens[5];
        if(tokens.length > 6) spawnerInterval = tokens[6];
        if(tokens.length > 7) spawnUnits = tokens[7];
        if(tokens.length > 8) inaccuracy = tokens[8];
    }

    public DefaultAirborneRaid(){
    }

    @Override
    public void build(Table table){
        rebuild(table);
    }

    private void rebuild(Table table){
        table.clearChildren();

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
            t.add("Spawner Count: ");
            fields(t, spawnerCount, str -> spawnerCount = str);
        }).left().row();

        table.table(t -> {
            t.add("Spawner Interval(s): ");
            fields(t, spawnerInterval, str -> spawnerInterval = str);
        }).left().row();

        table.table(t -> {
            t.add("Spawner Units(groups): ");
            TextField unitsField = new TextField(spawnUnits == null ? "" : spawnUnits);
            unitsField.setMessageText("@alpha,@beta;@gamma");
            unitsField.changed(() -> spawnUnits = unitsField.getText() == null ? "" : unitsField.getText().trim());
            t.add(unitsField).width(250f).height(40f).pad(2f);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> showUnitPicker(unit -> {
                appendSpawnUnit("@" + unit.name);
                rebuild(table);
            }));
            t.add(pick).size(64f, 32f).padLeft(2f);
            TextButton detail = new TextButton("detail", Styles.logict);
            detail.clicked(() -> showSpawnUnitsDetailDialog(table));
            t.add(detail).size(74f, 32f).padLeft(2f);
        }).left().row();

        table.table(t -> {
            t.add("Drop Inaccuracy Radius(tiles): ");
            fields(t, inaccuracy, str -> inaccuracy = str);
        }).left().row();

    }

    private void showUnitPicker(arc.func.Cons<UnitType> onSelect){
        Seq<UnitType> all = Vars.content.units();
        if(all == null || all.isEmpty()) return;

        Seq<UnitType> visible = all.select(unit -> unit != null && !unit.internal && !unit.isHidden());
        if(visible.isEmpty()){
            visible = all.select(unit -> unit != null && !unit.internal);
        }
        if(visible.isEmpty()) return;
        final Seq<UnitType> pickUnits = visible;

        BaseDialog dialog = new BaseDialog("pick unit");
        dialog.cont.table(root -> {
            root.left().top();

            TextField search = new TextField("");
            search.setMessageText("search unit...");
            root.add(search).growX().height(34f).padBottom(4f).row();

            Table list = new Table();
            list.left().top();
            list.defaults().growX().pad(1f);

            UIUtils.bindContentSearch(search, list, pickUnits, unit -> {
                list.button(b -> {
                    b.left();
                    TextureRegion iconRegion = unit.fullIcon == null ? unit.uiIcon : unit.fullIcon;
                    Drawable icon = iconRegion != null && iconRegion.found() ? new TextureRegionDrawable(iconRegion) : Icon.units;
                    b.image(icon).size(18f).padRight(6f);
                    String display = unit.localizedName == null ? unit.name : unit.localizedName;
                    Label label = new Label(compactLabelText(display));
                    label.setFontScale(1.05f);
                    b.add(label).left().growX();
                }, Styles.logicTogglet, () -> {
                    onSelect.get(unit);
                    dialog.hide();
                }).growX().height(34f);
                list.row();
            });

            ScrollPane pane = new ScrollPane(list, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setFadeScrollBars(false);
            root.add(pane).width(Vars.mobile ? 360f : 320f).maxHeight(Vars.mobile ? 420f : 300f).left();
        });

        dialog.buttons.defaults().size(120f, 54f);
        dialog.buttons.button("@cancel", dialog::hide);
        dialog.show();
    }

    private String compactLabelText(String text){
        if(text == null) return "";
        String out = text.trim();
        if(out.length() <= 20) return out;
        return out.substring(0, 19) + "...";
    }

    private void appendSpawnUnit(String token){
        if(token == null || token.trim().isEmpty()) return;
        String out = token.trim();
        String current = spawnUnits == null ? "" : spawnUnits.trim();
        if(current.isEmpty()){
            spawnUnits = out;
            return;
        }

        String[] groups = current.split(";");
        String lastGroup = groups.length == 0 ? "" : groups[groups.length - 1].trim();
        int count = rawUnitCount(lastGroup);

        if(current.endsWith(";") || lastGroup.isEmpty()){
            spawnUnits = current + out;
        }else if(count >= maxUnitsPerGroup){
            spawnUnits = current + ";" + out;
        }else{
            spawnUnits = current + "," + out;
        }
    }

    private void showSpawnUnitsDetailDialog(Table owner){
        BaseDialog dialog = new BaseDialog("spawnUnits detail");
        final String[] lastRaw = {null};
        final Runnable[] refresh = {null};

        dialog.cont.pane(root -> {
            refresh[0] = () -> {
                root.clearChildren();
                root.left().top();
                root.defaults().left().pad(2f);

                String raw = spawnUnits == null ? "" : spawnUnits.trim();
                lastRaw[0] = raw;

                root.add("raw: " + (raw.isEmpty() ? "(empty)" : raw)).wrap().width(Vars.mobile ? 420f : 340f).left().row();
                root.table(btns -> {
                    btns.left();
                    btns.defaults().size(118f, 32f).padRight(4f);
                    btns.button("append pick", Styles.logict, () -> showUnitPicker(unit -> {
                        appendSpawnUnit("@" + unit.name);
                        if(owner != null) rebuild(owner);
                        if(refresh[0] != null) refresh[0].run();
                    }));
                    btns.button("new group", Styles.logict, () -> showUnitPicker(unit -> {
                        appendSpawnUnitAsNewGroup("@" + unit.name);
                        if(owner != null) rebuild(owner);
                        if(refresh[0] != null) refresh[0].run();
                    }));
                    btns.button("clear", Styles.logict, () -> {
                        spawnUnits = "";
                        if(owner != null) rebuild(owner);
                        if(refresh[0] != null) refresh[0].run();
                    });
                }).left().row();
                root.row();

                if(raw.isEmpty()){
                    root.add("groups: 0").left().row();
                    return;
                }

                String[] groups = raw.split(";");
                root.add("groups: " + groups.length + ", max " + maxUnitsPerGroup + " units/group").left().row();
                root.row();

                for(int i = 0; i < groups.length; i++){
                    String group = groups[i] == null ? "" : groups[i].trim();
                    Seq<UnitType> parsed = parseGroupUnits(group);
                    int rawCount = rawUnitCount(group);

                    StringBuilder names = new StringBuilder();
                    for(int j = 0; j < parsed.size; j++){
                        UnitType unit = parsed.get(j);
                        if(j > 0) names.append(", ");
                        names.append(unit == null ? "null" : (unit.localizedName == null ? unit.name : unit.localizedName));
                    }
                    if(parsed.isEmpty()){
                        names.append("(empty)");
                    }

                    int groupIndex = i;
                    root.table(line -> {
                        line.left();
                        line.add((groupIndex + 1) + ". " + names + " (" + parsed.size + "/" + maxUnitsPerGroup + ")")
                        .wrap().width(Vars.mobile ? 370f : 290f).left();
                        TextButton addToGroup = new TextButton("+", Styles.logict);
                        addToGroup.clicked(() -> showUnitPicker(unit -> {
                            appendSpawnUnitToGroup(groupIndex, "@" + unit.name);
                            if(owner != null) rebuild(owner);
                            if(refresh[0] != null) refresh[0].run();
                        }));
                        addToGroup.setDisabled(parsed.size >= maxUnitsPerGroup);
                        line.add(addToGroup).size(30f, 30f).padLeft(4f);
                    }).left().row();
                    if(rawCount > maxUnitsPerGroup){
                        root.add("   truncated to first " + maxUnitsPerGroup + " units").left().row();
                    }
                }
            };

            refresh[0].run();
            root.update(() -> {
                String now = spawnUnits == null ? "" : spawnUnits.trim();
                if(lastRaw[0] == null || !lastRaw[0].equals(now)){
                    if(refresh[0] != null) refresh[0].run();
                }
            });
        }).width(Vars.mobile ? 460f : 380f).maxHeight(Vars.mobile ? 420f : 300f);

        dialog.buttons.defaults().size(120f, 54f);
        dialog.buttons.button("@ok", dialog::hide);
        dialog.show();
    }

    private void appendSpawnUnitAsNewGroup(String token){
        if(token == null || token.trim().isEmpty()) return;
        String out = token.trim();
        String current = spawnUnits == null ? "" : spawnUnits.trim();
        if(current.isEmpty()){
            spawnUnits = out;
            return;
        }

        while(current.endsWith(";")){
            current = current.substring(0, current.length() - 1).trim();
        }
        if(current.isEmpty()){
            spawnUnits = out;
        }else{
            spawnUnits = current + ";" + out;
        }
    }

    private void appendSpawnUnitToGroup(int groupIndex, String token){
        if(token == null || token.trim().isEmpty()) return;
        if(groupIndex < 0) return;

        String out = token.trim();
        String raw = spawnUnits == null ? "" : spawnUnits.trim();
        String[] split = raw.isEmpty() ? new String[0] : raw.split(";", -1);
        Seq<String> groups = new Seq<>();
        for(String group : split){
            groups.add(group == null ? "" : group.trim());
        }

        while(groups.size <= groupIndex){
            groups.add("");
        }

        String target = groups.get(groupIndex);
        if(rawUnitCount(target) >= maxUnitsPerGroup){
            return;
        }

        if(target.isEmpty()){
            groups.set(groupIndex, out);
        }else{
            groups.set(groupIndex, target + "," + out);
        }

        int lastNonEmpty = -1;
        for(int i = 0; i < groups.size; i++){
            String g = groups.get(i);
            if(g != null && !g.trim().isEmpty()) lastNonEmpty = i;
        }
        if(lastNonEmpty < 0){
            spawnUnits = "";
            return;
        }

        StringBuilder rebuilt = new StringBuilder();
        for(int i = 0; i <= lastNonEmpty; i++){
            if(i > 0) rebuilt.append(";");
            String group = groups.get(i);
            rebuilt.append(group == null ? "" : group.trim());
        }
        spawnUnits = rebuilt.toString();
    }

    private Seq<UnitType> parseGroupUnits(String groupRaw){
        Seq<UnitType> parsed = new Seq<>();
        String text = groupRaw == null ? "" : groupRaw.trim();
        if(text.isEmpty()) return parsed;

        String[] tokens = text.split("[,|\\s]+");
        for(String token : tokens){
            if(token == null || token.trim().isEmpty()) continue;
            parsed.add(parseUnitTypeToken(token));
            if(parsed.size >= maxUnitsPerGroup) break;
        }
        return parsed;
    }

    private int rawUnitCount(String groupRaw){
        String text = groupRaw == null ? "" : groupRaw.trim();
        if(text.isEmpty()) return 0;
        int count = 0;
        String[] tokens = text.split("[,|\\s]+");
        for(String token : tokens){
            if(token != null && !token.trim().isEmpty()) count++;
        }
        return count;
    }

    private UnitType parseUnitTypeToken(String token){
        String text = token == null ? "" : token.trim();
        if(text.isEmpty()) return UnitTypes.alpha;
        if(text.startsWith("@")) text = text.substring(1);

        UnitType byName = Vars.content.getByName(ContentType.unit, text);
        if(byName != null) return byName;

        Seq<UnitType> all = Vars.content.units();
        for(UnitType unit : all){
            if(unit == null) continue;
            if(unit.name != null && unit.name.equalsIgnoreCase(text)) return unit;
        }
        return UnitTypes.alpha;
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
        builder.append("defaultairraid").append(" ")
        .append(flag).append(" ").append(timer).append(" ")
        .append(alertTime).append(" ").append(raidTime).append(" ")
        .append(spawnerCount).append(" ").append(spawnerInterval).append(" ")
        .append(spawnUnits).append(" ").append(inaccuracy);
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder){
        return new DefaultAirborneRaidInstruction(
        builder.var(flag), builder.var(timer), builder.var(alertTime), builder.var(raidTime),
        builder.var(spawnerCount), builder.var(spawnerInterval), builder.var(spawnUnits), builder.var(inaccuracy)
        );
    }

    public static class DefaultAirborneRaidInstruction implements LExecutor.LInstruction{
        public LVar flag;
        public LVar timer;
        public LVar alertTime;
        public LVar raidTime;
        public LVar count;
        public LVar interval;
        public LVar units;
        public LVar inaccuracy;

        public int raidCounter = 0;
        public float curTime = 0f;
        public boolean iconShown = false;
        public boolean labelShown = false;
        public int threatLevel = 1;
        private final Vec2 source = new Vec2();
        private final Vec2 target = new Vec2();

        public DefaultAirborneRaidInstruction(LVar flag, LVar timer, LVar alertTime, LVar raidTime, LVar count, LVar interval, LVar units, LVar inaccuracy){
            this.flag = flag;
            this.timer = timer;
            this.alertTime = alertTime;
            this.raidTime = raidTime;
            this.count = count;
            this.interval = interval;
            this.units = units;
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

            if(!iconShown){
                showAlert(alert);
            }

            if(curTime > alert){
                if(!labelShown){
                    showLabel();
                }

                float raidTimer = curTime - alert;
                int planned = Math.max(0, Mathf.round(Math.max(0f, count.numf()) * threatScl()));
                float intervalSec = Math.max(0f, interval.numf());
                int totalSpawns;
                if(intervalSec <= 0.0001f){
                    totalSpawns = planned;
                }else{
                    totalSpawns = Math.min(planned, Mathf.floor(raidTimer / intervalSec) + 1);
                }

                int delta = totalSpawns - raidCounter;
                raidCounter = totalSpawns;
                for(int i = 0; i < delta; i++){
                    createAirborneSpawner(raidCounter - delta + i);
                }
            }
        }

        private void showAlert(float alertSeconds){
            updatePosition();
            iconShown = true;
            raidCounter = 0;

            WHCall.warnHudPacket(key(timer), "Airborne", alertSeconds, inaccuracy.numf(), source.x, source.y, target.x, target.y);
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

            AtomicReference<BlockFlag> targetFlag = new AtomicReference<>(BlockFlag.factory);
            WeightedRandom.random(
            new WeightedOption(3f, () -> targetFlag.set(BlockFlag.turret)),
            new WeightedOption(2f, () -> targetFlag.set(BlockFlag.drill)),
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
            return Mathf.sqrt(threatLevel) / 3;
        }

        private void showLabel(){
            WHCall.alertToastTable(2, -1, "[#ff7b69]Airborne: []<" + (int)(target.x / tilesize) + ", " + (int)(target.y / tilesize) + ">");
            labelShown = true;
        }

        private void createAirborneSpawner(int spawnIndex){
            Team spawnTeam = state.rules.waveTeam;
            if(spawnTeam == null) spawnTeam = state.rules.defaultTeam;
            if(spawnTeam == null) return;

            Seq<UnitType[]> groups = parseSpawnerGroups(ActionLogicSupport.valueText(units));
            UnitType[] loadout;
            if(groups.isEmpty()){
                loadout = new UnitType[]{UnitTypes.alpha};
            }else if(spawnIndex < groups.size){
                loadout = groups.get(spawnIndex);
            }else{
                // When spawner count exceeds configured groups, fill remainder with group 1.
                loadout = groups.get(0);
            }

            float spread = Math.max(0f, inaccuracy.numf()) * tilesize;
            Tmp.v1.trns(Mathf.random(360f), Mathf.random(spread));
            float sx = target.x + Tmp.v1.x;
            float sy = target.y + Tmp.v1.y;
            float rot = Angles.angle(source.x, source.y, target.x, target.y);

            AirborneSpawner spawner = new AirborneSpawner();
            spawner.init(spawnTeam, new Vec2(sx, sy), rot, 90f, loadout);
            spawner.add();
        }

        private Seq<UnitType[]> parseSpawnerGroups(String raw){
            Seq<UnitType[]> out = new Seq<>();
            String text = raw == null ? "" : raw.trim();
            if(text.isEmpty()){
                out.add(new UnitType[]{UnitTypes.alpha});
                return out;
            }

            String[] groups = text.split(";");
            for(String group : groups){
                String g = group == null ? "" : group.trim();
                if(g.isEmpty()) continue;
                String[] unitTokens = g.split("[,|\\s]+");
                Seq<UnitType> seq = new Seq<>();
                for(String token : unitTokens){
                    UnitType type = parseUnitTypeToken(token);
                    if(type != null) seq.add(type);
                    if(seq.size >= 4) break;
                }
                if(seq.isEmpty()){
                    seq.add(UnitTypes.alpha);
                }
                UnitType[] arr = seq.toArray(UnitType.class);
                out.add(arr);
            }

            if(out.isEmpty()){
                out.add(new UnitType[]{UnitTypes.alpha});
            }
            return out;
        }

        private UnitType parseUnitTypeToken(String token){
            if(token == null) return null;
            String text = token.trim();
            if(text.isEmpty()) return null;
            if(text.startsWith("@")) text = text.substring(1);
            UnitType type = Vars.content.getByName(ContentType.unit, text);
            if(type != null) return type;

            Seq<UnitType> units = Vars.content.units();
            for(UnitType u : units){
                if(u == null) continue;
                if(u.name != null && u.name.equalsIgnoreCase(text)) return u;
            }
            return UnitTypes.alpha;
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
