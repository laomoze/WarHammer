package wh.entities.event.logic;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.game.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import wh.content.*;
import wh.entities.*;
import wh.ui.*;

public class WHSpawnerStatement extends LStatement{
    public String run = "1";
    public String result = "result";
    public String team = "@crux";
    public String unit = "@air1";
    public String amount = "1";
    public String x = "-1";
    public String y = "-1";
    public String rotation = "90";
    public String spread = "8";
    public String spawnerCfg = "120|0";

    public WHSpawnerStatement(){
    }

    public WHSpawnerStatement(String[] tokens){
        try{
            run = tokens[1];
            team = tokens[2];
            unit = tokens[3];
            amount = tokens[4];
            x = tokens[5];
            y = tokens[6];
            rotation = tokens[7];
            spread = tokens[8];
            spawnerCfg = tokens[9];
            result = tokens[10];
        }catch(ArrayIndexOutOfBoundsException e){
            Log.err(e);
        }
    }

    @Override
    public void build(Table table){
        rebuild(table);
    }

    private void rebuild(Table table){
        table.clearChildren();

        fields(table, result, str -> result = str);
        table.add(" = spawner ");

        field(table, unit, str -> unit = str).width(130f);
        TextButton unitPick = new TextButton("pick", Styles.logict);
        unitPick.clicked(() -> showUnitPicker(unitPick, selected -> {
            unit = "@" + selected.name;
            rebuild(table);
        }));
        table.add(unitPick).size(72f, 34f).padLeft(2f);

        table.row();

        table.add("at ");
        fields(table, x, str -> x = str).width(64f);
        table.add(",");
        fields(table, y, str -> y = str).width(64f);

        table.add("team ");
        field(table, team, str -> team = str).width(108f);
        TextButton teamPick = new TextButton("pick", Styles.logict);
        teamPick.clicked(() -> showTeamPicker(teamPick, selected -> {
            team = "@" + selected.name;
            rebuild(table);
        }));
        table.add(teamPick).size(72f, 34f).padLeft(2f);

        table.row();

        table.add("rot ");
        fields(table, rotation, str -> rotation = str).width(58f);

        table.add("amount ");
        fields(table, amount, str -> amount = str).width(52f);

        table.add("spread ");
        fields(table, spread, str -> spread = str).width(52f);

        table.row();

        table.add("run ");
        fields(table, run, str -> run = str).width(110f);

        table.add("cfg ");
        fields(table, spawnerCfg, str -> spawnerCfg = str).width(120f);
    }

    private void showTeamPicker(Button button, Cons<Team> setter){
        Team[] teams = Team.baseTeams;
        if(teams == null || teams.length == 0) return;

        showSelectTable(button, (table, hide) -> {
            table.clearChildren();
            table.margin(2f);

            Table root = new Table();
            root.left().top();

            TextField search = new TextField("");
            search.setMessageText("search team...");
            root.add(search).growX().height(34f).padBottom(4f).row();

            Table list = new Table();
            list.left().top();
            list.defaults().growX().pad(1f);

            Runnable rebuild = () -> {
                list.clearChildren();
                String query = search.getText() == null ? "" : search.getText().trim().toLowerCase();

                for(Team candidate : teams){
                    if(candidate == null) continue;
                    String name = candidate.name == null ? "" : candidate.name;
                    if(!query.isEmpty() && !name.toLowerCase().contains(query)) continue;

                    list.button(b -> {
                        b.left();
                        b.image().size(14f).color(candidate.color).padRight(6f);
                        Label label = new Label(compactText(name));
                        label.setFontScale(1.05f);
                        b.add(label).left().growX();
                    }, Styles.logicTogglet, () -> {
                        setter.get(candidate);
                        hide.run();
                    }).growX().height(34f);
                    list.row();
                }
            };

            rebuild.run();
            search.changed(rebuild);

            ScrollPane pane = new ScrollPane(list, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setFadeScrollBars(false);

            float paneWidth = Vars.mobile ? 300f : 270f;
            float paneHeight = Vars.mobile ? 330f : 250f;
            root.add(pane).width(paneWidth).maxHeight(paneHeight).left().row();

            table.add(root).left();
        });
    }

    private void showUnitPicker(Button button, Cons<UnitType> setter){
        Seq<UnitType> units = Vars.content.units();
        if(units == null || units.isEmpty()) return;

        Seq<UnitType> visible = units.select(u -> u != null && !u.internal && !u.isHidden());

        showSelectTable(button, (table, hide) -> {
            table.clearChildren();
            table.margin(2f);

            Table root = new Table();
            root.left().top();

            TextField search = new TextField("");
            search.setMessageText("search unit...");
            root.add(search).growX().height(34f).padBottom(4f).row();

            Table list = new Table();
            list.left().top();
            list.defaults().growX().pad(1f);

            UIUtils.bindContentSearch(search, list, visible, candidate -> {
                list.button(b -> {
                    b.left();
                    b.image(candidate.fullIcon == null ? candidate.uiIcon : candidate.fullIcon).size(18f).padRight(6f);
                    String name = candidate.localizedName == null ? candidate.name : candidate.localizedName;
                    Label label = new Label(compactText(name));
                    label.setFontScale(1.05f);
                    b.add(label).left().growX();
                }, Styles.logicTogglet, () -> {
                    setter.get(candidate);
                    hide.run();
                }).growX().height(34f);
                list.row();
            });

            ScrollPane pane = new ScrollPane(list, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setFadeScrollBars(false);

            float paneWidth = Vars.mobile ? 300f : 270f;
            float paneHeight = Vars.mobile ? 330f : 250f;
            root.add(pane).width(paneWidth).maxHeight(paneHeight).left().row();

            table.add(root).left();
        });
    }

    private String compactText(String text){
        if(text == null) return "";
        String out = text.trim();
        if(out.length() <= 20) return out;
        return out.substring(0, 19) + "...";
    }

    @Override
    public void write(StringBuilder builder){
        builder.append("wh-spawner-unit ");
        builder.append(safe(run, "1")).append(" ");
        builder.append(safe(team, "@crux")).append(" ");
        builder.append(safe(unit, "@air1")).append(" ");
        builder.append(safe(amount, "1")).append(" ");
        builder.append(safe(x, "-1")).append(" ");
        builder.append(safe(y, "-1")).append(" ");
        builder.append(safe(rotation, "90")).append(" ");
        builder.append(safe(spread, "8")).append(" ");
        builder.append(safe(spawnerCfg, "120|0")).append(" ");
        builder.append(safe(result, "result"));
    }

    private String safe(String value, String fallback){
        if(value == null) return fallback;
        String out = value.trim();
        if(out.isEmpty()) return fallback;
        return out.replaceAll("\\s+", "_");
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder){
        return new WHSpawnerInstruction(
        builder.var(run),
        builder.var(team),
        builder.var(unit),
        builder.var(amount),
        builder.var(x),
        builder.var(y),
        builder.var(rotation),
        builder.var(spread),
        builder.var(spawnerCfg),
        builder.var(result)
        );
    }

    @Override
    public boolean privileged(){
        return false;
    }

    @Override
    public LCategory category(){
        return WHLogicStatements.autoTriggerCategory;
    }

    public static class WHSpawnerInstruction implements LExecutor.LInstruction{
        public LVar run;
        public LVar team;
        public LVar unit;
        public LVar amount;
        public LVar x;
        public LVar y;
        public LVar rotation;
        public LVar spread;
        public LVar spawnerCfg;
        public LVar result;

        private final Vec2 pos = new Vec2();

        public WHSpawnerInstruction(
        LVar run, LVar team, LVar unit, LVar amount, LVar x, LVar y, LVar rotation, LVar spread,
        LVar spawnerCfg, LVar result
        ){
            this.run = run;
            this.team = team;
            this.unit = unit;
            this.amount = amount;
            this.x = x;
            this.y = y;
            this.rotation = rotation;
            this.spread = spread;
            this.spawnerCfg = spawnerCfg;
            this.result = result;
        }

        @Override
        public void run(LExecutor exec){
            if(getSkipReason() != null) return;

            if(getRunGateReason() != null) return;

            SpawnRequest request = buildRequest();
            SpawnBatchResult batch = spawnBatch(request);
            result.setnum(batch.spawned);
        }

        private SpawnRequest buildRequest(){
            SpawnRequest request = new SpawnRequest();
            request.unitType = resolveUnitType(unit);
            request.spawnTeam = resolveTeam(team);
            request.count = Math.max(0, amount.numi());
            request.spreadWorld = Math.max(0f, spread.numf()) * Vars.tilesize;
            request.life = parseSpawnerLifetime(spawnerCfg);
            request.airdrop = parseSpawnerAirdrop(spawnerCfg);
            request.rotation = rotation.numf();

            request.baseX = World.unconv(x.numf());
            request.baseY = World.unconv(y.numf());
            request.useSpawnPoint = request.baseX < 0f || request.baseY < 0f;
            return request;
        }

        private SpawnBatchResult spawnBatch(SpawnRequest request){
            SpawnBatchResult out = new SpawnBatchResult();
            for(int i = 0; i < request.count; i++){
                if(!prepareSpawnPos(request)){
                    out.skippedNoSpawnPos++;
                    continue;
                }
                if(request.unitType == null || request.spawnTeam == null){
                    out.skippedMissingTarget++;
                    continue;
                }

                Spawner spawner = new Spawner()
                .init(request.unitType, request.spawnTeam, pos, request.rotation, request.life, request.airdrop);
                spawner.add();
                out.spawned++;
            }
            return out;
        }

        private boolean prepareSpawnPos(SpawnRequest request){
            if(request.useSpawnPoint){
                return pickSpawnPosition(request.spreadWorld, pos);
            }
            pos.set(request.baseX + Mathf.range(request.spreadWorld), request.baseY + Mathf.range(request.spreadWorld));
            return true;
        }

        private String getSkipReason(){
            boolean editorMode = Vars.state != null && Vars.state.isEditor();
            if(Vars.net.client() && !editorMode) return "client mode";
            if(Vars.state == null) return "Vars.state is null";
            if(Vars.state.rules == null) return "Vars.state.rules is null";
            if(Vars.world == null) return "Vars.world is null";
            if(Vars.state.gameOver) return "game over";
            if(hasReachedWaveVictory()) return "wave victory reached";
            return null;
        }

        private String getRunGateReason(){
            if(run.numi() == 0){
                return "run == 0";
            }
            return null;
        }

        private boolean hasReachedWaveVictory(){
            if(Vars.state == null || Vars.state.rules == null) return false;
            if(!Vars.state.rules.waves || Vars.state.rules.winWave <= 0) return false;
            if(Vars.state.wave < Vars.state.rules.winWave) return false;

            return Vars.state.enemies <= 0 && (Vars.spawner == null || !Vars.spawner.isSpawning());
        }

        private Team resolveTeam(LVar value){
            Team resolved = value.team();
            if(resolved != null) return resolved;
            return Vars.state.rules.waveTeam;
        }

        private UnitType resolveUnitType(LVar value){
            if(value.obj() instanceof UnitType type) return type;

            String name = extractContentName(value);
            if(name != null){
                UnitType byName = Vars.content.unit(name);
                if(byName != null) return byName;
            }

            return WHUnitTypes.air1;
        }

        private String extractContentName(LVar value){
            if(value.obj() instanceof String text){
                String normalized = normalizeContentName(text);
                if(normalized != null) return normalized;
            }
            return normalizeContentName(value.name);
        }

        private String normalizeContentName(String raw){
            if(raw == null) return null;
            String out = raw.trim();
            if(out.isEmpty()) return null;
            if(out.startsWith("___")) return null;
            if(out.startsWith("@")) out = out.substring(1);
            if(out.isEmpty()) return null;
            return out;
        }

        private float parseSpawnerLifetime(LVar value){
            String[] cfg = splitCfg(value);
            try{
                return Math.max(0.001f, Float.parseFloat(cfg[0]));
            }catch(Exception ignored){
                return 12f;
            }
        }

        private boolean parseSpawnerAirdrop(LVar value){
            String[] cfg = splitCfg(value);
            String v = cfg[1].trim().toLowerCase();
            return v.equals("1") || v.equals("true") || v.equals("on") || v.equals("yes");
        }

        private String[] splitCfg(LVar value){
            String raw = value.obj() == null ? value.name : String.valueOf(value.obj());
            if(raw == null || raw.trim().isEmpty()) return new String[]{"120", "0"};
            String[] out = raw.trim().split("\\|", 2);
            if(out.length == 1) return new String[]{out[0], "0"};
            return out;
        }

        private boolean pickSpawnPosition(float spreadWorld, Vec2 out){
            if(Vars.spawner != null && Vars.state.hasSpawns()){
                Seq<Tile> spawns = Vars.spawner.getSpawns();
                if(spawns != null && !spawns.isEmpty()){
                    Tile tile = spawns.random();
                    out.set(tile.worldx(), tile.worldy());
                    out.x += Mathf.range(spreadWorld);
                    out.y += Mathf.range(spreadWorld);
                    return true;
                }
            }
            return false;
        }

        private static class SpawnRequest{
            UnitType unitType;
            Team spawnTeam;
            int count;
            float spreadWorld;
            float life;
            boolean airdrop;
            float rotation;
            float baseX;
            float baseY;
            boolean useSpawnPoint;
        }

        private static class SpawnBatchResult{
            int spawned;
            int skippedNoSpawnPos;
            int skippedMissingTarget;
        }
    }
}
