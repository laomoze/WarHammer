package wh.entities.event.logic;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;
import wh.content.*;
import wh.entities.*;
import wh.entities.event.*;
import wh.ui.*;

public class WHRaidUnitStatement extends LStatement{
    public String team = "@crux";
    public String unit = "@air1";
    public String count = "2";
    public String spread = "8";
    public String shield = "-1";
    public String status = "@none";
    public String statusDuration = "90";

    public String checkTeam = "@sharded";
    public String minWave = "0";
    public String needItem = "@copper";
    public String needItemAmount = "0";
    public String needBlock = "@duo";
    public String needBlockAmount = "0";

    public String warnMode = "centered";
    public String warnText = "";
    public String spawnerCfg = "180|0";

    public WHRaidUnitStatement(){
    }

    public WHRaidUnitStatement(String[] tokens){
        try{
            team = tokens[1];
            unit = tokens[2];
            count = tokens[3];
            spread = tokens[4];
            checkTeam = tokens[5];
            minWave = tokens[6];
            parseItemPacked(tokens[7]);
            parseBlockPacked(tokens[8]);
            shield = tokens[9];
            parseStatusPacked(tokens[10]);
            parseWarnPacked(tokens[11]);
            spawnerCfg = tokens[12];
        }catch(ArrayIndexOutOfBoundsException e){
            Log.err(e);
        }
    }

    private void parseItemPacked(String packed){
        String[] pair = packed.split("\\|", 2);
        if(pair.length > 0 && !pair[0].isEmpty()) needItem = pair[0];
        if(pair.length > 1 && !pair[1].isEmpty()) needItemAmount = pair[1];
    }

    private void parseBlockPacked(String packed){
        String[] pair = packed.split("\\|", 2);
        if(pair.length > 0 && !pair[0].isEmpty()) needBlock = pair[0];
        if(pair.length > 1 && !pair[1].isEmpty()) needBlockAmount = pair[1];
    }

    private void parseStatusPacked(String packed){
        int split = packed.indexOf('|');
        if(split >= 0){
            status = packed.substring(0, split);
            if(split + 1 < packed.length()){
                statusDuration = packed.substring(split + 1);
            }
        }else{
            status = packed;
        }
    }

    private void parseWarnPacked(String packed){
        int split = packed.indexOf('|');
        if(split >= 0){
            warnMode = packed.substring(0, split);
            warnText = split + 1 < packed.length() ? packed.substring(split + 1) : "";
        }else{
            warnMode = packed;
        }
    }

    @Override
    public void build(Table table){
        rebuild(table);
    }

    private void rebuild(Table table){
        table.clearChildren();
        float width = 240f;

        table.table(t -> {
            t.add("Spawn Team: ");
            fields(t, team, value -> team = value).width(width);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                showTeamPicker(pick, selected -> {
                    team = "@" + selected.name;
                    rebuild(table);
                });
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("Unit Name: ");
            fields(t, unit, value -> unit = value).width(width);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                showUnitPicker(pick, selected -> {
                    unit = "@" + selected.name;
                    rebuild(table);
                });
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("Raid Count: ");
            fields(t, count, value -> count = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Spread(tiles): ");
            fields(t, spread, value -> spread = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Unit Shield: ");
            fields(t, shield, value -> shield = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Unit Buff: ");
            fields(t, status, value -> status = value).width(110f);
            t.add(" Duration(tick): ");
            fields(t, statusDuration, value -> statusDuration = value).width(110f);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                showContentPicker(pick, Vars.content.statusEffects(), selected -> {
                    status = "@" + selected.name;
                    rebuild(table);
                });
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("Check Team: ");
            fields(t, checkTeam, value -> checkTeam = value).width(width);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                showTeamPicker(pick, selected -> {
                    checkTeam = "@" + selected.name;
                    rebuild(table);
                });
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("Min Wave: ");
            fields(t, minWave, value -> minWave = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Need Item(csv): ");
            fields(t, needItem, value -> needItem = value).width(110f);
            t.add(" Amount(csv): ");
            fields(t, needItemAmount, value -> needItemAmount = value).width(110f);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                showContentPicker(pick, Vars.content.items(), selected -> {
                    needItem = appendCsvToken(needItem, "@" + selected.name);
                    rebuild(table);
                });
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
            TextButton detail = new TextButton("detail", Styles.logict);
            detail.clicked(() -> showItemDetailDialog(() -> rebuild(table)));
            t.add(detail).size(84f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("Need Block(csv): ");
            fields(t, needBlock, value -> needBlock = value).width(110f);
            t.add(" Amount(csv): ");
            fields(t, needBlockAmount, value -> needBlockAmount = value).width(110f);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                showBlockPicker(pick, selected -> {
                    needBlock = appendCsvToken(needBlock, "@" + selected.name);
                    rebuild(table);
                });
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
            TextButton detail = new TextButton("detail", Styles.logict);
            detail.clicked(() -> showBlockDetailDialog(() -> rebuild(table)));
            t.add(detail).size(84f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("Warn HUD: ");
            fields(t, warnMode, value -> warnMode = value).width(110f);
            t.add(" Text: ");
            fields(t, warnText, value -> warnText = value).width(110f);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                showWarnModePicker(pick, selected -> {
                    warnMode = selected;
                    rebuild(table);
                });
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("SpawnerCfg: ");
            fields(t, spawnerCfg, value -> spawnerCfg = value).width(width);
        }).left();
    }

    private void showWarnModePicker(Button button, Cons<String> setter){
        String[] options = {"legacy", "centered"};
        String current = warnMode == null || warnMode.isEmpty() ? "centered" : warnMode;
        showSelect(button, options, current, setter, 2, cell -> cell.size(90f, 28f));
    }

    private void showTeamPicker(Button button, Cons<Team> setter){
        Team[] teams = Team.baseTeams;
        if(teams == null || teams.length == 0) return;

        showCompactSelectTable(button, (table, hide) -> {
            for(Team candidate : teams){
                if(candidate == null) continue;
                table.button(b -> {
                    b.left();
                    b.image().size(14f).color(candidate.color).padRight(6f);
                    Label name = new Label(compactText(candidate.name));
                    name.setFontScale(1.05f);
                    b.add(name).left().growX();
                }, Styles.logicTogglet, () -> {
                    setter.get(candidate);
                    hide.run();
                }).growX().height(34f);
                table.row();
            }
        });
    }

    private <T extends UnlockableContent> void showContentPicker(Button button, Seq<T> contents, Cons<T> setter){
        if(contents == null || contents.isEmpty()) return;

        showCompactSelectTable(button, (table, hide) -> {
            for(T candidate : contents){
                if(candidate == null) continue;
                table.button(b -> {
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
                table.row();
            }
        });
    }

    private void showUnitPicker(Button button, Cons<UnitType> setter){
        Seq<UnitType> units = Vars.content.units();
        if(units == null || units.isEmpty()) return;

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

            UIUtils.bindContentSearch(search, list, units, candidate -> {
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

    private void showBlockPicker(Button button, Cons<Block> setter){
        Seq<Block> blocks = Vars.content.blocks();
        if(blocks == null || blocks.isEmpty()) return;

        showSelectTable(button, (table, hide) -> {
            table.clearChildren();
            table.margin(2f);

            Table root = new Table();
            root.left().top();

            TextField search = new TextField("");
            search.setMessageText("search block...");
            root.add(search).growX().height(34f).padBottom(4f).row();

            Table list = new Table();
            list.left().top();
            list.defaults().growX().pad(1f);

            UIUtils.bindContentSearch(search, list, blocks, candidate -> {
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

    private void showItemDetailDialog(Runnable onApply){
        BaseDialog dialog = new BaseDialog("Item Requirements");
        final TextArea[] itemAreaRef = {null};
        final TextArea[] amountAreaRef = {null};
        dialog.cont.pane(root -> {
            root.defaults().left().growX().pad(4f);
            root.add("Need Item(csv):").row();

            TextArea itemArea = new TextArea(safeCsvText(needItem));
            itemArea.setMessageText("@copper,@lead");
            itemAreaRef[0] = itemArea;
            root.add(itemArea).height(72f).row();

            root.add("Amount(csv):").padTop(2f).row();
            TextArea amountArea = new TextArea(safeCsvText(needItemAmount));
            amountArea.setMessageText("1000,200");
            amountAreaRef[0] = amountArea;
            root.add(amountArea).height(72f).row();

            Table actions = new Table();
            actions.left();
            TextButton pick = new TextButton("add item", Styles.logict);
            pick.clicked(() -> showContentPicker(pick, Vars.content.items(), selected -> {
                itemArea.setText(appendCsvToken(itemArea.getText(), "@" + selected.name));
            }));
            TextButton clear = new TextButton("clear", Styles.logict);
            clear.clicked(() -> {
                itemArea.setText("");
                amountArea.setText("");
            });
            actions.add(pick).size(96f, 34f).padRight(4f);
            actions.add(clear).size(80f, 34f);
            root.add(actions).padTop(4f).row();

            root.add("Preview:").padTop(4f).row();
            Table preview = new Table();
            preview.left().defaults().left().pad(1f);
            root.add(preview).row();

            Runnable rebuild = () -> rebuildCsvPreview(preview, itemArea.getText(), amountArea.getText());
            rebuild.run();
            itemArea.changed(rebuild);
            amountArea.changed(rebuild);
        }).width(Vars.mobile ? 620f : 700f).maxHeight(Vars.mobile ? 520f : 560f);

        dialog.buttons.defaults().size(130f, 54f);
        dialog.buttons.button("@cancel", dialog::hide);
        dialog.buttons.button("@ok", () -> {
            needItem = normalizeCsvText(itemAreaRef[0] == null ? "" : itemAreaRef[0].getText());
            needItemAmount = normalizeCsvText(amountAreaRef[0] == null ? "" : amountAreaRef[0].getText());
            dialog.hide();
            onApply.run();
        });
        dialog.show();
    }

    private void showBlockDetailDialog(Runnable onApply){
        BaseDialog dialog = new BaseDialog("Block Requirements");
        final TextArea[] blockAreaRef = {null};
        final TextArea[] amountAreaRef = {null};
        dialog.cont.pane(root -> {
            root.defaults().left().growX().pad(4f);
            root.add("Need Block(csv):").row();

            TextArea blockArea = new TextArea(safeCsvText(needBlock));
            blockArea.setMessageText("@duo,@scatter");
            blockAreaRef[0] = blockArea;
            root.add(blockArea).height(72f).row();

            root.add("Amount(csv):").padTop(2f).row();
            TextArea amountArea = new TextArea(safeCsvText(needBlockAmount));
            amountArea.setMessageText("5,2");
            amountAreaRef[0] = amountArea;
            root.add(amountArea).height(72f).row();

            Table actions = new Table();
            actions.left();
            TextButton pick = new TextButton("add block", Styles.logict);
            pick.clicked(() -> showBlockPicker(pick, selected -> {
                blockArea.setText(appendCsvToken(blockArea.getText(), "@" + selected.name));
            }));
            TextButton clear = new TextButton("clear", Styles.logict);
            clear.clicked(() -> {
                blockArea.setText("");
                amountArea.setText("");
            });
            actions.add(pick).size(96f, 34f).padRight(4f);
            actions.add(clear).size(80f, 34f);
            root.add(actions).padTop(4f).row();

            root.add("Preview:").padTop(4f).row();
            Table preview = new Table();
            preview.left().defaults().left().pad(1f);
            root.add(preview).row();

            Runnable rebuild = () -> rebuildCsvPreview(preview, blockArea.getText(), amountArea.getText());
            rebuild.run();
            blockArea.changed(rebuild);
            amountArea.changed(rebuild);
        }).width(Vars.mobile ? 620f : 700f).maxHeight(Vars.mobile ? 520f : 560f);

        dialog.buttons.defaults().size(130f, 54f);
        dialog.buttons.button("@cancel", dialog::hide);
        dialog.buttons.button("@ok", () -> {
            needBlock = normalizeCsvText(blockAreaRef[0] == null ? "" : blockAreaRef[0].getText());
            needBlockAmount = normalizeCsvText(amountAreaRef[0] == null ? "" : amountAreaRef[0].getText());
            dialog.hide();
            onApply.run();
        });
        dialog.show();
    }

    private void rebuildCsvPreview(Table preview, String namesCsv, String amountsCsv){
        preview.clearChildren();
        String[] names = splitCsvEditor(namesCsv);
        int[] amounts = splitCsvIntEditor(amountsCsv, names.length);
        if(names.length == 0){
            preview.add("(empty)");
            return;
        }

        for(int i = 0; i < names.length; i++){
            int required = i < amounts.length ? Math.max(0, amounts[i]) : 0;
            preview.add((i + 1) + ". " + names[i] + " x " + required).row();
        }
    }

    private String[] splitCsvEditor(String raw){
        if(raw == null) return new String[0];
        String text = normalizeCsvText(raw);
        if(text.isEmpty()) return new String[0];
        Seq<String> out = new Seq<>();
        for(String part : text.split(",")){
            String p = part == null ? "" : part.trim();
            if(!p.isEmpty()) out.add(p);
        }
        return out.toArray(String.class);
    }

    private int[] splitCsvIntEditor(String raw, int minSize){
        String[] parts = splitCsvEditor(raw);
        int size = Math.max(parts.length, minSize);
        int[] out = new int[size];
        for(int i = 0; i < parts.length; i++){
            try{
                out[i] = Integer.parseInt(parts[i].trim());
            }catch(Exception ignored){
                out[i] = 0;
            }
        }
        return out;
    }

    private String normalizeCsvText(String raw){
        if(raw == null) return "";
        return raw.trim().replace('\uFF0C', ',');
    }

    private String safeCsvText(String raw){
        return raw == null ? "" : raw;
    }

    private void showCompactSelectTable(Button button, Cons2<Table, Runnable> builder){
        showSelectTable(button, (table, hide) -> {
            table.clearChildren();
            table.margin(2f);

            Table list = new Table();
            list.left().top();
            list.defaults().growX().pad(1f);
            builder.get(list, hide);

            ScrollPane pane = new ScrollPane(list, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setFadeScrollBars(false);

            float paneWidth = Vars.mobile ? 300f : 270f;
            float paneHeight = Vars.mobile ? 330f : 250f;
            table.add(pane).width(paneWidth).maxHeight(paneHeight).left();
        });
    }

    private String compactText(String text){
        if(text == null) return "";
        String out = text.trim();
        if(out.length() <= 20) return out;
        return out.substring(0, 19) + "...";
    }

    @Override
    public LCategory category(){
        return WHLogicStatements.autoTriggerCategory;
    }

    @Override
    public boolean privileged(){
        return true;
    }

    @Override
    public void write(StringBuilder builder){
        builder.append("wh-raid-unit ");
        builder.append(safeToken(team, "@crux")).append(" ");
        builder.append(safeToken(unit, "@air4")).append(" ");
        builder.append(safeToken(count, "8")).append(" ");
        builder.append(safeToken(spread, "8")).append(" ");
        builder.append(safeToken(checkTeam, "@sharded")).append(" ");
        builder.append(safeToken(minWave, "0")).append(" ");
        builder.append(safeToken(needItem, "@copper")).append("|").append(safeToken(needItemAmount, "0")).append(" ");
        builder.append(safeToken(needBlock, "@duo")).append("|").append(safeToken(needBlockAmount, "0")).append(" ");
        builder.append(safeToken(shield, "-1")).append(" ");
        builder.append(safeToken(status, "@none")).append("|").append(safeToken(statusDuration, "10")).append(" ");
        // Keep total tokens below parser hard limit by packing warn mode + text.
        builder.append(safeToken(warnMode, "centered")).append("|").append(safeToken(warnText, "")).append(" ");
        builder.append(safeToken(spawnerCfg, "12|0"));
    }

    private String safeToken(String value, String fallback){
        if(value == null) return fallback;

        String out = value.trim();
        if(out.isEmpty()) return fallback;

        out = out.replaceAll("\\s+", "_");
        return out;
    }

    private String appendCsvToken(String csv, String token){
        String t = token == null ? "" : token.trim();
        if(t.isEmpty()) return csv == null ? "" : csv;
        if(csv == null || csv.trim().isEmpty()) return t;

        String[] parts = csv.split(",");
        for(String part : parts){
            if(part.trim().equalsIgnoreCase(t)){
                return csv;
            }
        }
        return csv + "," + t;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder){
        return new WHRaidUnitInstruction(
        builder.var(team),
        builder.var(unit),
        builder.var(count),
        builder.var(spread),
        builder.var(checkTeam),
        builder.var(minWave),
        builder.var(needItem),
        builder.var(needItemAmount),
        builder.var(needBlock),
        builder.var(needBlockAmount),
        builder.var(shield),
        builder.var(status),
        builder.var(statusDuration),
        builder.var(warnMode),
        builder.var(warnText),
        builder.var(spawnerCfg)
        );
    }

    public static class WHRaidUnitInstruction implements LExecutor.LInstruction{
        public LVar team;
        public LVar unit;
        public LVar count;
        public LVar spread;
        public LVar checkTeam;
        public LVar minWave;
        public LVar needItem;
        public LVar needItemAmount;
        public LVar needBlock;
        public LVar needBlockAmount;
        public LVar shield;
        public LVar status;
        public LVar statusDuration;
        public LVar warnMode;
        public LVar warnText;
        public LVar spawnerCfg;

        private boolean cyclePrepared = false;
        private boolean executed = false;
        private String persistentOnceKey;
        private UnitType cachedUnitType;
        private Team cachedSpawnTeam;
        private float cachedSpreadWorld;
        private float cachedShield;
        private StatusEffect cachedStatus;
        private float cachedStatusDuration;
        private PortableAutoEventTrigger.FleetWarnHudMode cachedWarnMode;
        private String cachedWarnText;
        private float cachedSpawnerLifetime = 12f;
        private boolean cachedSpawnerAirdrop = false;

        private static final float blockScanInterval = 500f;
        private final Interval blockScanTimer = new Interval(1);
        private boolean blockScanPrimed = false;
        private int cachedBlockMetCount = 0;
        private String cachedBlockDetail = "off";

        private final ObjectIntMap<Block> blockNeedScratch = new ObjectIntMap<>();
        private final ObjectIntMap<Block> blockCountScratch = new ObjectIntMap<>();

        private final Vec2 pos = new Vec2();
        private final Vec2 target = new Vec2();
        private final Vec2 targetBase = new Vec2();

        public WHRaidUnitInstruction(
        LVar team, LVar unit, LVar count, LVar spread,
        LVar checkTeam, LVar minWave, LVar needItem, LVar needItemAmount, LVar needBlock, LVar needBlockAmount,
        LVar shield, LVar status, LVar statusDuration, LVar warnMode, LVar warnText, LVar spawnerCfg
        ){
            this.team = team;
            this.unit = unit;
            this.count = count;
            this.spread = spread;
            this.checkTeam = checkTeam;
            this.minWave = minWave;
            this.needItem = needItem;
            this.needItemAmount = needItemAmount;
            this.needBlock = needBlock;
            this.needBlockAmount = needBlockAmount;
            this.shield = shield;
            this.status = status;
            this.statusDuration = statusDuration;
            this.warnMode = warnMode;
            this.warnText = warnText;
            this.spawnerCfg = spawnerCfg;
        }

        @Override
        public void run(LExecutor exec){
            if(shouldSkipRun()) return;
            if(isAlreadyTriggered(exec)) return;

            ConditionSnapshot snapshot = inspectConditions();
            if(snapshot != null && !snapshot.ok){
                resetRoundState();
                holdForRetry(exec);
                return;
            }

            if(executed) return;

            prepareCycleIfNeeded();
            showWarnHud();

            if(!spawnBatch()){
                holdForRetry(exec);
                return;
            }

            executed = true;
            cyclePrepared = false;
            markTriggered(exec);
        }

        private boolean shouldSkipRun(){
            int winWave = Vars.state.rules.winWave;
            if(winWave <= 0) return false;
            boolean editorMode = Vars.state.isEditor();
            if(Vars.net.client() && !editorMode) return true;
            return Vars.state == null || Vars.state.rules == null || Vars.world == null;
        }

        private void holdForRetry(LExecutor exec){
            exec.counter.numval--;
            exec.yield = true;
        }

        private void resetRoundState(){
            executed = false;
            cyclePrepared = false;
        }

        private boolean isAlreadyTriggered(LExecutor exec){
            String key = resolvePersistentOnceKey(exec);
            if(key == null || Vars.state == null || Vars.state.rules == null) return false;
            return Vars.state.rules.objectiveFlags.contains(key);
        }

        private void markTriggered(LExecutor exec){
            String key = resolvePersistentOnceKey(exec);
            if(key == null || Vars.state == null || Vars.state.rules == null) return;
            Vars.state.rules.objectiveFlags.add(key);
        }

        // 持久化一次触发键：同一地图同一逻辑语句只会触发一次。
        // 键由“处理器位置 + 指令索引 + 当前语句配置哈希”组成；
        // 当你修改逻辑内容时，哈希变化，会重新触发一次。
        private String resolvePersistentOnceKey(LExecutor exec){
            if(persistentOnceKey != null) return persistentOnceKey;
            if(Vars.state == null || Vars.state.rules == null) return null;

            int instructionIndex = Math.max(0, exec.counter.numi() - 1);
            String buildPart;
            if(exec.build != null && exec.build.tile != null){
                buildPart = exec.build.tileX() + "_" + exec.build.tileY();
            }else{
                buildPart = "nobuild";
            }

            String cfgRaw = buildConfigFingerprintRaw();
            String cfgHash = Integer.toHexString(cfgRaw.hashCode());
            persistentOnceKey = "wh.raid.once." + buildPart + "." + instructionIndex + "." + cfgHash;
            return persistentOnceKey;
        }

        private String buildConfigFingerprintRaw(){
            StringBuilder out = new StringBuilder(256);
            out.append(valueText(team)).append('|');
            out.append(valueText(unit)).append('|');
            out.append(valueText(count)).append('|');
            out.append(valueText(spread)).append('|');
            out.append(valueText(checkTeam)).append('|');
            out.append(valueText(minWave)).append('|');
            out.append(valueText(needItem)).append('|');
            out.append(valueText(needItemAmount)).append('|');
            out.append(valueText(needBlock)).append('|');
            out.append(valueText(needBlockAmount)).append('|');
            out.append(valueText(shield)).append('|');
            out.append(valueText(status)).append('|');
            out.append(valueText(statusDuration)).append('|');
            out.append(valueText(warnMode)).append('|');
            out.append(valueText(warnText)).append('|');
            out.append(valueText(spawnerCfg));
            return out.toString();
        }

        private void prepareCycleIfNeeded(){
            if(!cyclePrepared){
                prepareCycle();
            }
        }

        private boolean spawnBatch(){
            int totalCount = Math.max(0, count.numi());
            int spawned = 0;
            for(int i = 0; i < totalCount; i++){
                if(spawnOne()){
                    spawned++;
                }
            }
            return spawned > 0;
        }

        private boolean spawnOne(){
            if(!pickSpawnPosition(cachedSpreadWorld, cachedSpawnTeam, pos)){
                return false;
            }
            if(!canCreateUnitNow(cachedSpawnTeam, cachedUnitType)){
                return false;
            }
            target.set(targetBase);

            float rotation = Angles.angle(pos.x, pos.y, target.x, target.y);
            Spawner spawner = new Spawner()
            .init(cachedUnitType, cachedSpawnTeam, pos, rotation, cachedSpawnerLifetime, cachedSpawnerAirdrop)
            .setShieldToApply(cachedShield);

            if(cachedStatus != null && cachedStatus != StatusEffects.none && cachedStatusDuration > 0f){
                spawner.setStatus(cachedStatus, cachedStatusDuration);
            }

            spawner.add();
            return true;
        }

        private boolean canCreateUnitNow(Team spawnTeam, UnitType unitType){
            if(spawnTeam == null || unitType == null || Vars.state == null || Vars.state.rules == null) return false;
            if(Vars.state.isEditor()) return true;

            return Units.canCreate(spawnTeam, unitType) || spawnTeam == Vars.state.rules.waveTeam;
        }

        private void prepareCycle(){
            cachedUnitType = resolveUnitType(unit);
            cachedSpawnTeam = resolveTeam();
            cachedSpreadWorld = Math.max(0f, spread.numf()) * Vars.tilesize;
            cachedShield = shield.numf();
            cachedStatus = resolveStatus(status);
            cachedStatusDuration = Math.max(0f, statusDuration.numf()) * Time.toSeconds;
            cachedWarnMode = parseWarnMode(warnMode);
            cachedWarnText = decodeWarnText(warnText);
            cachedSpawnerLifetime = parseSpawnerLifetime(spawnerCfg);
            cachedSpawnerAirdrop = parseSpawnerAirdrop(spawnerCfg);
            resolveTarget(targetBase, cachedSpawnTeam);
            cyclePrepared = true;
        }

        private void showWarnHud(){
            Team spawnTeam = cachedSpawnTeam == null ? resolveTeam() : cachedSpawnTeam;
            PortableAutoEventTrigger.showFleetWarnHudNow(
            spawnTeam, 3f, cachedWarnMode, cachedWarnText);
        }

        private PortableAutoEventTrigger.FleetWarnHudMode parseWarnMode(LVar value){
            if(value == null) return PortableAutoEventTrigger.FleetWarnHudMode.centered;
            String raw = value.obj() == null ? value.name : String.valueOf(value.obj());
            if(raw == null) return PortableAutoEventTrigger.FleetWarnHudMode.centered;
            String normalized = raw.trim().toLowerCase();
            if(normalized.equals("legacy")) return PortableAutoEventTrigger.FleetWarnHudMode.legacy;
            return PortableAutoEventTrigger.FleetWarnHudMode.centered;
        }

        private String decodeWarnText(LVar value){
            if(value == null) return "";
            String raw = value.obj() == null ? value.name : String.valueOf(value.obj());
            if(raw == null) return "";
            String text = raw.trim();
            if(text.isEmpty()) return "";
            return text.replace('_', ' ');
        }

        private float parseSpawnerLifetime(LVar value){
            String[] cfg = splitCfg(value);
            String life = cfg[0];
            if(life == null || life.isEmpty()) return 12f;
            try{
                return Math.max(0.001f, Float.parseFloat(life));
            }catch(Exception ignored){
                return 12f;
            }
        }

        private boolean parseSpawnerAirdrop(LVar value){
            String[] cfg = splitCfg(value);
            String flag = cfg[1];
            if(flag == null) return false;
            String v = flag.trim().toLowerCase();
            return v.equals("1") || v.equals("true") || v.equals("on") || v.equals("yes");
        }

        private String[] splitCfg(LVar value){
            if(value == null) return new String[]{"12", "0"};
            String raw = value.obj() == null ? value.name : String.valueOf(value.obj());
            if(raw == null || raw.trim().isEmpty()) return new String[]{"12", "0"};
            String[] out = raw.trim().split("\\|", 2);
            if(out.length == 1) return new String[]{out[0], "0"};
            return out;
        }

        private UnitType resolveUnitType(LVar value){
            // 优先走对象：这是最标准、最稳定的输入（例如 @air1）。
            if(value.obj() instanceof UnitType type){
                return type;
            }

            // 兼容字符串名称输入：允许通过名字解析单位。
            String byName = extractContentName(value);
            if(byName != null){
                UnitType mappedByName = Vars.content.unit(byName);
                if(mappedByName != null) return mappedByName;
            }

            // 不再支持数字ID兜底，避免把普通数字误判为单位ID。
            return WHUnitTypes.air1;
        }

        private Team resolveTeam(){
            Team resolved = team.team();
            if(resolved != null) return resolved;
            return Vars.state.rules.waveTeam;
        }

        private Team resolveConditionTeam(){
            Team resolved = checkTeam.team();
            if(resolved != null) return resolved;
            return Vars.state.rules.defaultTeam;
        }

        private StatusEffect resolveStatus(LVar value){
            // 优先走对象：这是最标准、最稳定的输入（例如 @burning 对应的对象）。
            if(value.obj() instanceof StatusEffect effect){
                return effect;
            }

            // 兼容字符串名称输入：允许通过名字解析状态效果。
            String byName = extractContentName(value);
            if(byName != null){
                if(byName.equals("none")) return StatusEffects.none;
                StatusEffect mappedByName = Vars.content.statusEffect(byName);
                if(mappedByName != null) return mappedByName;
            }

            // 不再支持数字ID（包括 10000 偏移）兜底，避免脏数据触发错误映射。
            return StatusEffects.none;
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

            String out = Strings.stripColors(raw).trim();
            if(out.endsWith("[]")){
                out = out.substring(0, out.length() - 2).trim();
            }
            if(out.isEmpty()) return null;
            if(out.startsWith("___")) return null;

            if(out.startsWith("@")){
                out = out.substring(1);
            }

            if(out.isEmpty()) return null;
            return out;
        }

        private boolean pickSpawnPosition(float spreadWorld, Team spawnTeam, Vec2 out){
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

        private void resolveTarget(Vec2 out, Team spawnTeam){
            Team enemy = spawnTeam == Vars.state.rules.defaultTeam ? Vars.state.rules.waveTeam : Vars.state.rules.defaultTeam;
            Building core = enemy.core();
            if(core != null){
                out.set(core.x, core.y);
            }else{
                out.set(Vars.world.width() * Vars.tilesize * 0.5f, Vars.world.height() * Vars.tilesize * 0.5f);
            }
        }

        private void debugLogState(String phase, String text, Object... args){
        }

        private ConditionSnapshot inspectConditions(){
            if(blockScanTimer.get(0, blockScanInterval)){
                ConditionSnapshot out = new ConditionSnapshot();
                fillBaseSnapshot(out);
                evaluateItemRequirements(out);
                evaluateBlockRequirements(out);

                boolean itemOk = isItemConditionMet(out);
                boolean blockOk = isBlockConditionMet(out);
                boolean waveOk = isWaveConditionMet(out);
                out.itemOk = itemOk;
                out.blockOk = blockOk;
                out.waveOk = waveOk;
                out.ok = itemOk && blockOk && waveOk;
                return out;
            }
            return null;
        }

        private void fillBaseSnapshot(ConditionSnapshot out){
            out.team = resolveConditionTeam();
            out.wave = Vars.state.wave;
            out.minWaveRequired = Math.max(0, minWave.numi());
        }

        private void evaluateItemRequirements(ConditionSnapshot out){
            String[] itemNames = splitCsv(valueText(needItem));
            int[] itemAmounts = splitCsvInt(valueText(needItemAmount), itemNames.length);
            int itemRequiredCount = 0;
            int itemMetCount = 0;
            StringBuilder detail = new StringBuilder();

            for(int i = 0; i < itemNames.length; i++){
                int required = i < itemAmounts.length ? Math.max(0, itemAmounts[i]) : 0;
                if(required <= 0) continue;
                itemRequiredCount++;

                Item itemType = resolveItemByText(itemNames[i]);
                if(itemType == null){
                    debugLogState(
                    "item-name-miss",
                    "[WH][RaidLogic][warn] unresolved item token=@ (raw needItem csv).",
                    itemNames[i]
                    );
                }
                int owned = countTeamCoreItemTotal(out.team, itemType);
                if(detail.length() > 0) detail.append(";");
                detail.append(itemType == null ? itemNames[i] : itemType.name).append(":").append(owned).append("/").append(required);
                if(owned >= required){
                    itemMetCount++;
                }
            }

            out.itemRequired = itemRequiredCount;
            out.itemOwned = itemMetCount;
            out.itemDetail = detail.length() == 0 ? "off" : detail.toString();
        }

        private int countTeamCoreItemTotal(Team teamValue, Item item){
            if(teamValue == null || item == null) return -1;
            Seq<CoreBlock.CoreBuild> cores = teamValue.cores();
            if(cores == null || cores.isEmpty()) return -1;

            int total = 0;
            for(CoreBlock.CoreBuild core : cores){
                if(core != null && core.items != null){
                    total += core.items.get(item);
                }
            }
            return total;
        }

        private void evaluateBlockRequirements(ConditionSnapshot out){
            String[] blockNames = splitCsv(valueText(needBlock));
            int[] blockAmounts = splitCsvInt(valueText(needBlockAmount), blockNames.length);
            rebuildBlockNeedMap(blockNames, blockAmounts);
            debugLogState(
            "block-need-map",
            "[WH][RaidLogic][trace] block need map team=@ rawBlock=@ rawAmount=@ parsed=@",
            out.team == null ? "null" : out.team.name,
            valueText(needBlock),
            valueText(needBlockAmount),
            formatBlockNeedMap()
            );

            int blockRequiredCount = blockNeedScratch.size;
            int blockMetCount = 0;
            if(blockRequiredCount > 0){
                if(!blockScanPrimed){
                    BlockScanResult scanned = scanBlockRequirements(out.team);
                    cachedBlockMetCount = scanned.met;
                    cachedBlockDetail = scanned.detail;
                    blockScanPrimed = true;
                    debugLogState(
                    "block-scan",
                    "[WH][RaidLogic][trace] block scan team=@ required=@ met=@ detail=@",
                    out.team == null ? "null" : out.team.name,
                    blockRequiredCount,
                    cachedBlockMetCount,
                    cachedBlockDetail
                    );
                }else{
                    debugLogState(
                    "block-cache",
                    "[WH][RaidLogic][trace] block scan cached interval=@t, use cached met=@/@ detail=@",
                    (int)blockScanInterval,
                    cachedBlockMetCount,
                    blockRequiredCount,
                    cachedBlockDetail
                    );
                }
                blockMetCount = cachedBlockMetCount;
            }else{
                cachedBlockMetCount = 0;
                cachedBlockDetail = "off";
                blockScanPrimed = false;
            }

            out.blockRequired = blockRequiredCount;
            out.blockOwned = blockMetCount;
            out.blockDetail = cachedBlockDetail;
        }

        private void rebuildBlockNeedMap(String[] blockNames, int[] blockAmounts){
            blockNeedScratch.clear();
            for(int i = 0; i < blockNames.length; i++){
                int required = i < blockAmounts.length ? Math.max(0, blockAmounts[i]) : 0;
                if(required <= 0) continue;
                Block blockType = resolveBlockByText(blockNames[i]);
                if(blockType == null){
                    debugLogState(
                    "block-name-miss",
                    "[WH][RaidLogic][warn] unresolved block token=@ (raw needBlock csv).",
                    blockNames[i]
                    );
                    continue;
                }
                blockNeedScratch.increment(blockType, 0, required);
            }
        }

        private String formatBlockNeedMap(){
            if(blockNeedScratch.isEmpty()) return "off";
            StringBuilder out = new StringBuilder();
            for(ObjectIntMap.Entry<Block> req : blockNeedScratch.entries()){
                if(out.length() > 0) out.append(";");
                out.append(req.key == null ? "null" : req.key.name).append(":").append(req.value);
            }
            return out.toString();
        }

        private boolean isItemConditionMet(ConditionSnapshot out){
            return out.itemRequired <= 0 || out.itemOwned == out.itemRequired;
        }

        private boolean isBlockConditionMet(ConditionSnapshot out){
            return out.blockRequired <= 0 || out.blockOwned == out.blockRequired;
        }

        private boolean isWaveConditionMet(ConditionSnapshot out){
            boolean waveOk = out.wave >= out.minWaveRequired;
            if(waveOk){
                debugLogState(
                "wave-gate-pass",
                "[WH][RaidLogic][trace] wave gate pass key=@ wave=@ >= minWave=@",
                "auto",
                out.wave,
                out.minWaveRequired
                );
            }
            return waveOk;
        }

        private BlockScanResult scanBlockRequirements(Team teamValue){
            BlockScanResult out = new BlockScanResult();
            if(teamValue == null || blockNeedScratch.isEmpty()) return out;

            if(blockNeedScratch.size == 1){
                ObjectIntMap.Entry<Block> single = blockNeedScratch.entries().next();
                int count = Groups.build.count(build -> build.team == teamValue && build.block == single.key);
                out.met = count >= single.value ? 1 : 0;
                out.detail = single.key.name + ":" + count + "/" + single.value;
                return out;
            }

            blockCountScratch.clear();
            Groups.build.each(build -> {
                if(build.team != teamValue) return;
                int need = blockNeedScratch.get(build.block, 0);
                if(need <= 0) return;

                int now = blockCountScratch.get(build.block, 0);
                if(now < need){
                    blockCountScratch.put(build.block, now + 1);
                }
            });

            int met = 0;
            StringBuilder detail = new StringBuilder();
            for(ObjectIntMap.Entry<Block> req : blockNeedScratch.entries()){
                int owned = blockCountScratch.get(req.key, 0);
                if(detail.length() > 0) detail.append(";");
                detail.append(req.key == null ? "null" : req.key.name).append(":").append(owned).append("/").append(req.value);
                if(owned >= req.value){
                    met++;
                }
            }
            out.met = met;
            out.detail = detail.length() == 0 ? "off" : detail.toString();
            return out;
        }

        private String valueText(LVar value){
            if(value == null) return "";
            Object raw = value.obj();
            if(raw != null){
                if(raw instanceof UnlockableContent content && content.name != null){
                    return content.name.trim();
                }
                if(raw instanceof Team t && t.name != null){
                    return t.name.trim();
                }
                String text = String.valueOf(raw);
                return text == null ? "" : text.trim();
            }

            String name = value.name == null ? "" : value.name.trim();
            if(name.isEmpty()) return "";

            // Logic constants can be serialized as internal names (e.g. ___...).
            // Read numf() to recover their numeric value.
            if(name.startsWith("___")){
                float num = value.numf();
                int asInt = Mathf.round(num);
                if(Math.abs(num - asInt) < 0.0001f){
                    return String.valueOf(asInt);
                }
                return Strings.autoFixed(num, 3);
            }

            return name;
        }

        private String[] splitCsv(String raw){
            if(raw == null) return new String[0];
            String text = raw.trim().replace('\uFF0C', ',');
            if(text.isEmpty()) return new String[0];

            Seq<String> out = new Seq<>();
            String[] parts = text.split(",");
            for(String part : parts){
                String p = part == null ? "" : part.trim();
                if(!p.isEmpty()){
                    out.add(p);
                }
            }
            return out.toArray(String.class);
        }

        private int[] splitCsvInt(String raw, int minSize){
            String[] parts = splitCsv(raw);
            int size = Math.max(parts.length, minSize);
            int[] out = new int[size];
            for(int i = 0; i < parts.length; i++){
                try{
                    out[i] = Integer.parseInt(parts[i].trim());
                }catch(Exception ignored){
                    out[i] = 0;
                }
            }
            return out;
        }

        private Item resolveItemByText(String raw){
            String name = normalizeContentName(raw);
            if(name == null) return null;
            Item byName = Vars.content.item(name);
            if(byName != null) return byName;
            Seq<Item> items = Vars.content.items();
            if(items != null){
                for(Item item : items){
                    if(item == null) continue;
                    String localized = normalizeContentName(item.localizedName);
                    if(localized != null && localized.equalsIgnoreCase(name)) return item;
                }
            }
            try{
                int rawId = Integer.parseInt(name);
                return Vars.content.item(rawId);
            }catch(Exception ignored){
                return null;
            }
        }

        private Block resolveBlockByText(String raw){
            String name = normalizeContentName(raw);
            if(name == null) return null;
            Block byName = Vars.content.block(name);
            if(byName != null) return byName;
            Seq<Block> blocks = Vars.content.blocks();
            if(blocks != null){
                for(Block block : blocks){
                    if(block == null) continue;
                    String localized = normalizeContentName(block.localizedName);
                    if(localized != null && localized.equalsIgnoreCase(name)) return block;
                }
            }
            try{
                int rawId = Integer.parseInt(name);
                return Vars.content.block(rawId);
            }catch(Exception ignored){
                return null;
            }
        }

        private static class ConditionSnapshot{
            Team team;
            int wave;
            int minWaveRequired;
            int itemRequired;
            int itemOwned;
            String itemDetail = "off";
            int blockRequired;
            int blockOwned;
            String blockDetail = "off";
            boolean itemOk;
            boolean blockOk;
            boolean waveOk;
            boolean ok;
        }

        private static class BlockScanResult{
            int met = 0;
            String detail = "off";
        }
    }

}
