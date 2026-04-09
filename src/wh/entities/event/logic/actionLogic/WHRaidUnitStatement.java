package wh.entities.event.logic.actionLogic;

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
import wh.entities.event.logic.*;
import wh.entities.event.objective.*;
import wh.ui.*;

public class WHRaidUnitStatement extends LStatement{
    public String team = "@crux";
    public String unit = "@flare";
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
    public String timer = "180";
    public String timerDuration = "3";

    private static final float fullFieldWidth = 240f;
    private static final float pairFieldWidth = 110f;
    private static final float pickButtonWidth = 70f;
    private static final float detailButtonWidth = 84f;
    private static final float rowButtonHeight = 32f;

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
            if(tokens.length > 13) timer = tokens[13];
            if(tokens.length > 14) timerDuration = tokens[14];
        }catch(ArrayIndexOutOfBoundsException e){
            Log.err(e);
        }
    }

    private void parseItemPacked(String packed){
        parsePairPacked(packed, value -> needItem = value, value -> needItemAmount = value);
    }

    private void parseBlockPacked(String packed){
        parsePairPacked(packed, value -> needBlock = value, value -> needBlockAmount = value);
    }

    private void parsePairPacked(String packed, Cons<String> leftSetter, Cons<String> rightSetter){
        String[] pair = packed.split("\\|", 2);
        if(pair.length > 0 && !pair[0].isEmpty()) leftSetter.get(pair[0]);
        if(pair.length > 1 && !pair[1].isEmpty()) rightSetter.get(pair[1]);
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

    /** 重建配置 UI，所有输入控件都从当前字段值重新绘制。 */
    private void rebuild(Table table){
        table.clearChildren();

        addSingleFieldRowWithPick(table, "Spawn Team: ", team, value -> team = value, fullFieldWidth, pick ->
        showTeamPicker(pick, selected -> {
            team = "@" + selected.name;
            rebuild(table);
        }));

        addSingleFieldRowWithPick(table, "Unit Name: ", unit, value -> unit = value, fullFieldWidth, pick ->
        showUnitPicker(pick, selected -> {
            unit = "@" + selected.name;
            rebuild(table);
        }));

        addSingleFieldRow(table, "Raid Count: ", count, value -> count = value, fullFieldWidth);
        addSingleFieldRow(table, "Spread(tiles): ", spread, value -> spread = value, fullFieldWidth);
        addSingleFieldRow(table, "Unit Shield: ", shield, value -> shield = value, fullFieldWidth);

        addDualFieldRow(table,
        "Unit Buff: ", status, value -> status = value,
        " Duration(tick): ", statusDuration, value -> statusDuration = value,
        pick -> showContentPicker(pick, Vars.content.statusEffects(), selected -> {
            status = "@" + selected.name;
            rebuild(table);
        }),
        null);

        addSingleFieldRowWithPick(table, "Check Team: ", checkTeam, value -> checkTeam = value, fullFieldWidth, pick ->
        showTeamPicker(pick, selected -> {
            checkTeam = "@" + selected.name;
            rebuild(table);
        }));

        addSingleFieldRow(table, "Min Wave: ", minWave, value -> minWave = value, fullFieldWidth);

        addDualFieldRow(table,
        "Need Item(csv): ", needItem, value -> needItem = value,
        " Amount(csv): ", needItemAmount, value -> needItemAmount = value,
        pick -> showContentPicker(pick, Vars.content.items(), selected -> {
            needItem = appendCsvToken(needItem, "@" + selected.name);
            rebuild(table);
        }),
        () -> showItemDetailDialog(() -> rebuild(table)));

        addDualFieldRow(table,
        "Need Block(csv): ", needBlock, value -> needBlock = value,
        " Amount(csv): ", needBlockAmount, value -> needBlockAmount = value,
        pick -> showBlockPicker(pick, selected -> {
            needBlock = appendCsvToken(needBlock, "@" + selected.name);
            rebuild(table);
        }),
        () -> showBlockDetailDialog(() -> rebuild(table)));

        addDualFieldRow(table,
        "Warn HUD: ", warnMode, value -> warnMode = value,
        " Text: ", warnText, value -> warnText = value,
        pick -> showWarnModePicker(pick, selected -> {
            warnMode = selected;
            rebuild(table);
        }),
        null);

        addSingleFieldRow(table, "SpawnerCfg: ", spawnerCfg, value -> spawnerCfg = value, fullFieldWidth);
        addDualFieldRow(table,
        "Trigger Timer: ", timer, value -> timer = value,
        " Duration(sec): ", timerDuration, value -> timerDuration = value,
        null,
        null);
    }

    private void addSingleFieldRow(Table table, String label, String value, Cons<String> setter, float width){
        table.table(t -> {
            t.add(label);
            fields(t, value, setter).width(width);
        }).left().row();
    }

    private void addSingleFieldRowWithPick(Table table, String label, String value, Cons<String> setter, float width, Cons<Button> onPick){
        table.table(t -> {
            t.add(label);
            fields(t, value, setter).width(width);
            t.add(createActionButton("pick", onPick)).size(pickButtonWidth, rowButtonHeight).padLeft(4f);
        }).left().row();
    }

    private void addDualFieldRow(
    Table table,
    String leftLabel, String leftValue, Cons<String> leftSetter,
    String rightLabel, String rightValue, Cons<String> rightSetter,
    Cons<Button> onPick, Runnable onDetail
    ){
        table.table(t -> {
            t.add(leftLabel);
            fields(t, leftValue, leftSetter).width(pairFieldWidth);
            t.add(rightLabel);
            fields(t, rightValue, rightSetter).width(pairFieldWidth);

            if(onPick != null){
                t.add(createActionButton("pick", onPick)).size(pickButtonWidth, rowButtonHeight).padLeft(4f);
            }
            if(onDetail != null){
                TextButton detail = new TextButton("detail", Styles.logict);
                detail.clicked(onDetail);
                t.add(detail).size(detailButtonWidth, rowButtonHeight).padLeft(4f);
            }
        }).left().row();
    }

    private TextButton createActionButton(String text, Cons<Button> onClick){
        TextButton button = new TextButton(text, Styles.logict);
        button.clicked(() -> onClick.get(button));
        return button;
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
                addContentOptionRow(table, candidate, () -> {
                    setter.get(candidate);
                    hide.run();
                });
            }
        });
    }

    private <T extends UnlockableContent> void addContentOptionRow(Table table, T candidate, Runnable onSelect){
        table.button(b -> {
            b.left();
            b.image(candidate.fullIcon == null ? candidate.uiIcon : candidate.fullIcon).size(18f).padRight(6f);
            String name = candidate.localizedName == null ? candidate.name : candidate.localizedName;
            Label label = new Label(compactText(name));
            label.setFontScale(1.05f);
            b.add(label).left().growX();
        }, Styles.logicTogglet, onSelect).growX().height(34f);
        table.row();
    }

    private void showUnitPicker(Button button, Cons<UnitType> setter){
        showSearchContentPicker(button, Vars.content.units(), "search unit...", setter);
    }

    private void showBlockPicker(Button button, Cons<Block> setter){
        showSearchContentPicker(button, Vars.content.blocks(), "search block...", setter);
    }

    private <T extends UnlockableContent> void showSearchContentPicker(Button button, Seq<T> contents, String searchHint, Cons<T> setter){
        if(contents == null || contents.isEmpty()) return;

        showSelectTable(button, (table, hide) -> {
            table.clearChildren();
            table.margin(2f);

            Table root = new Table();
            root.left().top();

            TextField search = new TextField("");
            search.setMessageText(searchHint);
            root.add(search).growX().height(34f).padBottom(4f).row();

            Table list = new Table();
            list.left().top();
            list.defaults().growX().pad(1f);

            UIUtils.bindContentSearch(search, list, contents, candidate -> addContentOptionRow(list, candidate, () -> {
                setter.get(candidate);
                hide.run();
            }));

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
        showCsvDetailDialog(
        "Item Requirements",
        "Need Item(csv):",
        safeCsvText(needItem),
        "@copper,@lead",
        safeCsvText(needItemAmount),
        "1000,200",
        "add item",
        (pick, namesArea) -> showContentPicker(pick, Vars.content.items(), selected ->
        namesArea.setText(appendCsvToken(namesArea.getText(), "@" + selected.name))),
        (names, amounts) -> {
            needItem = names;
            needItemAmount = amounts;
        },
        onApply
        );
    }

    private void showBlockDetailDialog(Runnable onApply){
        showCsvDetailDialog(
        "Block Requirements",
        "Need Block(csv):",
        safeCsvText(needBlock),
        "@duo,@scatter",
        safeCsvText(needBlockAmount),
        "5,2",
        "add block",
        (pick, namesArea) -> showBlockPicker(pick, selected ->
        namesArea.setText(appendCsvToken(namesArea.getText(), "@" + selected.name))),
        (names, amounts) -> {
            needBlock = names;
            needBlockAmount = amounts;
        },
        onApply
        );
    }

    /** 通用 CSV 明细弹窗：编辑名称与数量两列，支持预览并回写。 */
    private void showCsvDetailDialog(
    String title,
    String namesLabel,
    String namesValue,
    String namesHint,
    String amountsValue,
    String amountsHint,
    String addButtonText,
    Cons2<Button, TextArea> onAddClick,
    Cons2<String, String> onSave,
    Runnable onApply
    ){
        BaseDialog dialog = new BaseDialog(title);
        final TextArea[] namesAreaRef = {null};
        final TextArea[] amountsAreaRef = {null};

        dialog.cont.pane(root -> {
            root.defaults().left().growX().pad(4f);
            root.add(namesLabel).row();

            TextArea namesArea = new TextArea(namesValue);
            namesArea.setMessageText(namesHint);
            namesAreaRef[0] = namesArea;
            root.add(namesArea).height(72f).row();

            root.add("Amount(csv):").padTop(2f).row();
            TextArea amountsArea = new TextArea(amountsValue);
            amountsArea.setMessageText(amountsHint);
            amountsAreaRef[0] = amountsArea;
            root.add(amountsArea).height(72f).row();

            Table actions = new Table();
            actions.left();
            TextButton pick = new TextButton(addButtonText, Styles.logict);
            pick.clicked(() -> onAddClick.get(pick, namesArea));

            TextButton clear = new TextButton("clear", Styles.logict);
            clear.clicked(() -> {
                namesArea.setText("");
                amountsArea.setText("");
            });
            actions.add(pick).size(96f, 34f).padRight(4f);
            actions.add(clear).size(80f, 34f);
            root.add(actions).padTop(4f).row();

            root.add("Preview:").padTop(4f).row();
            Table preview = new Table();
            preview.left().defaults().left().pad(1f);
            root.add(preview).row();

            Runnable rebuild = () -> rebuildCsvPreview(preview, namesArea.getText(), amountsArea.getText());
            rebuild.run();
            namesArea.changed(rebuild);
            amountsArea.changed(rebuild);
        }).width(Vars.mobile ? 620f : 700f).maxHeight(Vars.mobile ? 520f : 560f);

        dialog.buttons.defaults().size(130f, 54f);
        dialog.buttons.button("@cancel", dialog::hide);
        dialog.buttons.button("@ok", () -> {
            String names = normalizeCsvText(namesAreaRef[0] == null ? "" : namesAreaRef[0].getText());
            String amounts = normalizeCsvText(amountsAreaRef[0] == null ? "" : amountsAreaRef[0].getText());
            onSave.get(names, amounts);
            dialog.hide();
            onApply.run();
        });
        dialog.show();
    }

    private void rebuildCsvPreview(Table preview, String namesCsv, String amountsCsv){
        preview.clearChildren();
        String[] names = splitCsvTokens(namesCsv);
        int[] amounts = splitCsvInts(amountsCsv, names.length);
        if(names.length == 0){
            preview.add("(empty)");
            return;
        }

        for(int i = 0; i < names.length; i++){
            int required = i < amounts.length ? Math.max(0, amounts[i]) : 0;
            preview.add((i + 1) + ". " + names[i] + " x " + required).row();
        }
    }

    private String normalizeCsvText(String raw){
        if(raw == null) return "";
        return raw.trim().replace('\uFF0C', ',');
    }

    private String safeCsvText(String raw){
        return raw == null ? "" : raw;
    }

    /** 按逗号切分 CSV 文本并去掉空白 token。 */
    private static String[] splitCsvTokens(String raw){
        if(raw == null) return new String[0];
        String text = raw.trim().replace('\uFF0C', ',');
        if(text.isEmpty()) return new String[0];

        Seq<String> out = new Seq<>();
        for(String part : text.split(",")){
            String token = part == null ? "" : part.trim();
            if(!token.isEmpty()) out.add(token);
        }
        return out.toArray(String.class);
    }

    private static int[] splitCsvInts(String raw, int minSize){
        String[] parts = splitCsvTokens(raw);
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
        builder.append(safeToken(unit, "@airA4")).append(" ");
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
        builder.append(safeToken(spawnerCfg, "12|0")).append(" ");
        builder.append(safeToken(timer, "_")).append(" ");
        builder.append(safeToken(timerDuration, "3"));
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
        builder.var(spawnerCfg),
        builder.var(timer),
        builder.var(timerDuration)
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
        public LVar timer;
        public LVar timerDuration;

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
        private static final String onceTriggerKeyPrefix = "wh.raid.once.";

        public WHRaidUnitInstruction(
        LVar team, LVar unit, LVar count, LVar spread,
        LVar checkTeam, LVar minWave, LVar needItem, LVar needItemAmount, LVar needBlock, LVar needBlockAmount,
        LVar shield, LVar status, LVar statusDuration, LVar warnMode, LVar warnText, LVar spawnerCfg,
        LVar timer, LVar timerDuration
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
            this.timer = timer;
            this.timerDuration = timerDuration;
        }

        @Override
        /* 执行主流程：条件检查 -> 缓存准备 -> 预警 -> 刷怪 -> 触发计时目标。 */
        public void run(LExecutor exec){
            if(shouldSkipRun()) return;
            if(isAlreadyTriggered(exec)) return;

            ConditionSnapshot snapshot = inspectConditions();
            if(!snapshot.ok){
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

            triggerTimerObjectiveIfNeeded();
            executed = true;
            cyclePrepared = false;
            markTriggered(exec);
        }

        /** 判断当前帧是否应跳过执行（无效状态或客户端非编辑模式）。 */
        private boolean shouldSkipRun(){
            if(Vars.state == null || Vars.state.rules == null || Vars.world == null) return true;

            int winWave = Vars.state.rules.winWave;
            if(winWave <= 0) return false;

            boolean editorMode = Vars.state.isEditor();
            boolean multiplayerClient = Vars.net.client();
            return multiplayerClient && !editorMode;
        }

        /** 回退指令计数并让处理器 yield，一帧后重试。 */
        private void holdForRetry(LExecutor exec){
            exec.counter.numval--;
            exec.yield = true;
        }

        /** 条件不满足时重置本轮执行状态。 */
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

        /** 生成并缓存一次性触发键，保证同位置同指令同配置只触发一次。 */
        private String resolvePersistentOnceKey(LExecutor exec){
            if(persistentOnceKey != null) return persistentOnceKey;
            if(Vars.state == null || Vars.state.rules == null) return null;

            int instructionIndex = Math.max(0, exec.counter.numi() - 1);
            String buildPart = (exec.build != null && exec.build.tile != null)
            ? exec.build.tileX() + "_" + exec.build.tileY()
            : "nobuild";

            String cfgHash = Integer.toHexString(buildConfigFingerprintRaw().hashCode());
            persistentOnceKey = onceTriggerKeyPrefix + buildPart + "." + instructionIndex + "." + cfgHash;
            return persistentOnceKey;
        }

        /** 按固定顺序拼接关键配置，用于计算稳定指纹。 */
        private String buildConfigFingerprintRaw(){
            return buildFingerprint(
            team, unit, count, spread, checkTeam, minWave,
            needItem, needItemAmount, needBlock, needBlockAmount,
            shield, status, statusDuration, warnMode, warnText,
            spawnerCfg, timer, timerDuration
            );
        }

        /** 把多个 LVar 转为文本并拼成统一指纹串。 */
        private String buildFingerprint(LVar... vars){
            StringBuilder out = new StringBuilder(vars.length * 12);
            for(int i = 0; i < vars.length; i++){
                if(i > 0) out.append('|');
                out.append(valueText(vars[i]));
            }
            return out.toString();
        }

        /** 满足触发后按 timer/timerDuration 触发计时目标。 */
        private void triggerTimerObjectiveIfNeeded(){
            if(Vars.state == null || Vars.state.rules == null) return;

            String key = normalizeTimerKey(valueText(timer));
            if(key.isEmpty()) return;

            float sec = parseDurationSeconds(timerDuration);
            if(sec <= 0f) return;

            TriggerObjective.obtain(key).trigger(sec * Time.toSeconds);
        }

        private String normalizeTimerKey(String raw){
            if(raw == null) return "";
            String out = raw.trim();
            if(out.isEmpty()) return "";
            if(out.equals("_") || out.equalsIgnoreCase("none")) return "";
            if(out.startsWith("___")) return "";
            if(out.startsWith("@")) out = out.substring(1);
            return out;
        }

        private float parseDurationSeconds(LVar value){
            if(value == null) return 0f;
            String text = valueText(value);
            if(text.isEmpty()) return 0f;
            try{
                return Math.max(0f, Float.parseFloat(text));
            }catch(Exception ignored){
                return Math.max(0f, value.numf());
            }
        }

        /** 仅在未准备时执行一次缓存预处理。 */
        private void prepareCycleIfNeeded(){
            if(!cyclePrepared){
                prepareCycle();
            }
        }

        /** 按数量循环调用 spawnOne，至少成功一次才算成功。 */
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

        /** 尝试生成单个单位；选点或创建失败时返回 false。 */
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

        /** 解析本轮所需配置并缓存，避免循环内重复读取 LVar。 */
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

        /** 解析 HUD 预警模式。 */
        private PortableAutoEventTrigger.FleetWarnHudMode parseWarnMode(LVar value){
            if(value == null) return PortableAutoEventTrigger.FleetWarnHudMode.centered;
            String raw = rawText(value);
            if(raw == null) return PortableAutoEventTrigger.FleetWarnHudMode.centered;
            String normalized = raw.trim().toLowerCase();
            if(normalized.equals("legacy")) return PortableAutoEventTrigger.FleetWarnHudMode.legacy;
            return PortableAutoEventTrigger.FleetWarnHudMode.centered;
        }

        private String decodeWarnText(LVar value){
            if(value == null) return "";
            String raw = rawText(value);
            if(raw == null) return "";
            String text = raw.trim();
            if(text.isEmpty()) return "";
            return text.replace('_', ' ');
        }

        /** 解析 spawnerCfg 的生命周期，默认 12 秒。 */
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

        /** 解析 spawnerCfg 的空投开关。 */
        private boolean parseSpawnerAirdrop(LVar value){
            String[] cfg = splitCfg(value);
            String flag = cfg[1];
            if(flag == null) return false;
            String v = flag.trim().toLowerCase();
            return v.equals("1") || v.equals("true") || v.equals("on") || v.equals("yes");
        }

        private String[] splitCfg(LVar value){
            if(value == null) return new String[]{"12", "0"};
            String raw = rawText(value);
            if(raw == null || raw.trim().isEmpty()) return new String[]{"12", "0"};
            String[] out = raw.trim().split("\\|", 2);
            if(out.length == 1) return new String[]{out[0], "0"};
            return out;
        }

        private String rawText(LVar value){
            Object raw = value.obj();
            return raw == null ? value.name : String.valueOf(raw);
        }

        /** 解析单位类型，失败时回退到默认单位。 */
        private UnitType resolveUnitType(LVar value){
            if(value.obj() instanceof UnitType type){
                return type;
            }

            String byName = extractContentName(value);
            if(byName != null){
                UnitType mappedByName = Vars.content.unit(byName);
                if(mappedByName != null) return mappedByName;
            }

            return WHUnitTypes.airA1;
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

        /** 解析状态效果，失败时回退为 none。 */
        private StatusEffect resolveStatus(LVar value){
            if(value.obj() instanceof StatusEffect effect){
                return effect;
            }

            String byName = extractContentName(value);
            if(byName != null){
                if(byName.equals("none")) return StatusEffects.none;
                StatusEffect mappedByName = Vars.content.statusEffect(byName);
                if(mappedByName != null) return mappedByName;
            }

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

        /** 从出生点附近随机一个位置作为刷怪坐标。 */
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

        /** 解析单位朝向目标：优先敌方核心，否则地图中心。 */
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

        /** 汇总并计算当前触发条件快照。 */
        private ConditionSnapshot inspectConditions(){
            boolean allowBlockRescan = blockScanTimer.get(0, blockScanInterval);
            ConditionSnapshot out = new ConditionSnapshot();
            fillBaseSnapshot(out);
            evaluateItemRequirements(out);
            evaluateBlockRequirements(out, allowBlockRescan);

            boolean itemOk = isItemConditionMet(out);
            boolean blockOk = isBlockConditionMet(out);
            boolean waveOk = isWaveConditionMet(out);
            out.itemOk = itemOk;
            out.blockOk = blockOk;
            out.waveOk = waveOk;
            out.ok = itemOk && blockOk && waveOk;
            return out;
        }

        private void fillBaseSnapshot(ConditionSnapshot out){
            out.team = resolveConditionTeam();
            out.wave = Vars.state.wave;
            out.minWaveRequired = Math.max(0, minWave.numi());
        }

        /** 统计物品条件达成情况并生成明细。 */
        private void evaluateItemRequirements(ConditionSnapshot out){
            String[] itemNames = splitCsvTokens(valueText(needItem));
            int[] itemAmounts = splitCsvInts(valueText(needItemAmount), itemNames.length);
            int itemRequiredCount = 0;
            int itemMetCount = 0;
            StringBuilder detail = new StringBuilder();

            for(int i = 0; i < itemNames.length; i++){
                int required = requiredAmount(itemAmounts, i);
                if(required == 0) continue;
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
                if(detail.length() != 0) detail.append(";");
                detail.append(itemType == null ? itemNames[i] : itemType.name).append(":").append(owned).append("/").append(required);
                if(owned >= required){
                    itemMetCount++;
                }
            }

            out.itemRequired = itemRequiredCount;
            out.itemOwned = itemMetCount;
            out.itemDetail = detail.length() == 0 ? "off" : detail.toString();
        }

        /** 统计指定队伍所有核心中的某物品总量。 */
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

        /** 统计建筑条件，使用缓存避免每帧全图扫描。 */
        private void evaluateBlockRequirements(ConditionSnapshot out, boolean allowBlockRescan){
            String[] blockNames = splitCsvTokens(valueText(needBlock));
            int[] blockAmounts = splitCsvInts(valueText(needBlockAmount), blockNames.length);
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
            if(blockRequiredCount <= 0){
                clearBlockScanCache();
            }else if(shouldRefreshBlockScan(allowBlockRescan)){
                refreshBlockScanCache(out.team, blockRequiredCount);
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

            out.blockRequired = blockRequiredCount;
            out.blockOwned = cachedBlockMetCount;
            out.blockDetail = cachedBlockDetail;
        }

        /** 判断是否需要刷新建筑扫描缓存。 */
        private boolean shouldRefreshBlockScan(boolean allowBlockRescan){
            return !blockScanPrimed || allowBlockRescan;
        }

        /** 执行一次建筑扫描并刷新缓存结果。 */
        private void refreshBlockScanCache(Team teamValue, int blockRequiredCount){
            BlockScanResult scanned = scanBlockRequirements(teamValue);
            cachedBlockMetCount = scanned.met;
            cachedBlockDetail = scanned.detail;
            blockScanPrimed = true;

            debugLogState(
            "block-scan",
            "[WH][RaidLogic][trace] block scan team=@ required=@ met=@ detail=@",
            teamValue == null ? "null" : teamValue.name,
            blockRequiredCount,
            cachedBlockMetCount,
            cachedBlockDetail
            );
        }

        /** 清空建筑扫描缓存。 */
        private void clearBlockScanCache(){
            cachedBlockMetCount = 0;
            cachedBlockDetail = "off";
            blockScanPrimed = false;
        }

        private void rebuildBlockNeedMap(String[] blockNames, int[] blockAmounts){
            blockNeedScratch.clear();
            for(int i = 0; i < blockNames.length; i++){
                int required = requiredAmount(blockAmounts, i);
                if(required == 0) continue;
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

        private int requiredAmount(int[] amounts, int index){
            return index < amounts.length ? Math.max(0, amounts[index]) : 0;
        }

        private String formatBlockNeedMap(){
            if(blockNeedScratch.isEmpty()) return "off";
            StringBuilder out = new StringBuilder();
            for(ObjectIntMap.Entry<Block> req : blockNeedScratch.entries()){
                if(out.length() != 0) out.append(";");
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

        /** 扫描队伍建筑并统计各目标方块达成数。 */
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
                if(detail.length() != 0) detail.append(";");
                detail.append(req.key == null ? "null" : req.key.name).append(":").append(owned).append("/").append(req.value);
                if(owned >= req.value){
                    met++;
                }
            }
            out.met = met;
            out.detail =
            detail.length() == 0 ? "off" : detail.toString();
            return out;
        }

        /** 将 LVar 转为稳定文本，兼容内容对象与数值常量。 */
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

        private Item resolveItemByText(String raw){
            return resolveContentByText(raw,
            name -> Vars.content.item(name),
            Vars.content.items(),
            id -> Vars.content.item(id));
        }

        private Block resolveBlockByText(String raw){
            return resolveContentByText(raw,
            name -> Vars.content.block(name),
            Vars.content.blocks(),
            id -> Vars.content.block(id));
        }

        /** 按名称、本地名、ID 顺序解析内容对象。 */
        private <T extends UnlockableContent> T resolveContentByText(
        String raw,
        Func<String, T> byNameResolver,
        Seq<T> contents,
        ContentByIdResolver<T> byIdResolver
        ){
            String name = normalizeContentName(raw);
            if(name == null) return null;

            T byName = byNameResolver.get(name);
            if(byName != null) return byName;

            if(contents != null){
                for(T content : contents){
                    if(content == null) continue;
                    String localized = normalizeContentName(content.localizedName);
                    if(localized != null && localized.equalsIgnoreCase(name)) return content;
                }
            }

            try{
                return byIdResolver.get(Integer.parseInt(name));
            }catch(Exception ignored){
                return null;
            }
        }

        @FunctionalInterface
        private interface ContentByIdResolver<T>{
            T get(int id);
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
