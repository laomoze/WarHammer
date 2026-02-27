package wh.entities.event.logic;

import arc.math.*;
import arc.math.geom.*;
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
import mindustry.world.*;
import wh.content.*;
import wh.entities.*;
import wh.entities.event.*;

public class WHRaidUnitStatement extends LStatement{
    public String alertTime = "60";
    public String raidTime = "5*60";
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
    public String spawnerCfg = "12|0";

    public WHRaidUnitStatement(){
    }

    public WHRaidUnitStatement(String[] tokens){
        int base = 1;
        // Legacy scripts used a flag as token[1]; auto mode ignores it.
        if(tokens.length > 1 && !containsDigit(tokens[1])){
            base = 2;
        }

        if(tokens.length > base) alertTime = tokens[base];
        if(tokens.length > base + 1) raidTime = tokens[base + 1];
        if(tokens.length > base + 2) team = tokens[base + 2];
        if(tokens.length > base + 3) unit = tokens[base + 3];
        if(tokens.length > base + 4) count = tokens[base + 4];
        if(tokens.length > base + 5) spread = tokens[base + 5];
        if(tokens.length > base + 6) checkTeam = tokens[base + 6];
        if(tokens.length > base + 7) minWave = tokens[base + 7];

        if(tokens.length > base + 8){
            if(tokens[base + 8].indexOf('|') >= 0){
                String[] pair = tokens[base + 8].split("\\|", 2);
                if(pair.length > 0 && !pair[0].isEmpty()) needItem = pair[0];
                if(pair.length > 1 && !pair[1].isEmpty()) needItemAmount = pair[1];

                if(tokens.length > base + 9){
                    pair = tokens[base + 9].split("\\|", 2);
                    if(pair.length > 0 && !pair[0].isEmpty()) needBlock = pair[0];
                    if(pair.length > 1 && !pair[1].isEmpty()) needBlockAmount = pair[1];
                }
                if(tokens.length > base + 10) shield = tokens[base + 10];
                if(tokens.length > base + 11){
                    String packed = tokens[base + 11];
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
                if(tokens.length > base + 12) warnMode = tokens[base + 12];
                if(tokens.length > base + 13) warnText = tokens[base + 13];
                if(tokens.length > base + 14) spawnerCfg = tokens[base + 14];
            }else{
                // Legacy fallback format.
                needItem = tokens[base + 8];
                if(tokens.length > base + 9) needItemAmount = tokens[base + 9];
                if(tokens.length > base + 10) needBlock = tokens[base + 10];
                if(tokens.length > base + 11) needBlockAmount = tokens[base + 11];
                if(tokens.length > base + 12) shield = tokens[base + 12];
                if(tokens.length > base + 13){
                    String packed = tokens[base + 13];
                    int split = packed.indexOf('|');
                    if(split >= 0){
                        status = packed.substring(0, split);
                        if(split + 1 < packed.length()){
                            statusDuration = packed.substring(split + 1);
                        }
                    }else{
                        status = packed;
                        if(tokens.length > base + 14) statusDuration = tokens[base + 14];
                    }
                }
                if(tokens.length > base + 15) warnMode = tokens[base + 15];
                if(tokens.length > base + 16) warnText = tokens[base + 16];
                if(tokens.length > base + 17) spawnerCfg = tokens[base + 17];
            }
        }
    }

    @Override
    public void build(Table table){
        float width = 240f;

        table.table(t -> {
            t.add("Alert Time(s): ");
            fields(t, alertTime, value -> alertTime = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Raid Time(s): ");
            fields(t, raidTime, value -> raidTime = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Spawn Team: ");
            fields(t, team, value -> team = value).width(width);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                Team[] teams = Team.baseTeams;
                if(teams == null || teams.length == 0) return;
                showSelect(pick, teams, teams[0], selected -> team = "@" + selected.name, 3, cell -> cell.size(130f, 40f));
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("Unit Name: ");
            fields(t, unit, value -> unit = value).width(width);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                UnitType[] units = Vars.content.units().toArray(UnitType.class);
                if(units.length == 0) return;
                showSelect(pick, units, units[0], selected -> unit = "@" + selected.name, 3, cell -> cell.size(150f, 40f));
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
            t.add(" Duration(s): ");
            fields(t, statusDuration, value -> statusDuration = value).width(110f);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                StatusEffect[] effects = Vars.content.statusEffects().toArray(StatusEffect.class);
                if(effects.length == 0) return;
                showSelect(pick, effects, effects[0], selected -> status = "@" + selected.name, 3, cell -> cell.size(150f, 40f));
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("Check Team: ");
            fields(t, checkTeam, value -> checkTeam = value).width(width);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                Team[] teams = Team.baseTeams;
                if(teams == null || teams.length == 0) return;
                showSelect(pick, teams, teams[0], selected -> checkTeam = "@" + selected.name, 3, cell -> cell.size(130f, 40f));
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
                Item[] items = Vars.content.items().toArray(Item.class);
                if(items.length == 0) return;
                showSelect(pick, items, items[0], selected -> {
                    needItem = appendCsvToken(needItem, "@" + selected.name);
                }, 3, cell -> cell.size(150f, 40f));
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("Need Block(csv): ");
            fields(t, needBlock, value -> needBlock = value).width(110f);
            t.add(" Amount(csv): ");
            fields(t, needBlockAmount, value -> needBlockAmount = value).width(110f);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                Block[] blocks = Vars.content.blocks().toArray(Block.class);
                if(blocks.length == 0) return;
                showSelect(pick, blocks, blocks[0], selected -> {
                    needBlock = appendCsvToken(needBlock, "@" + selected.name);
                }, 3, cell -> cell.size(150f, 40f));
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("Warn HUD: ");
            fields(t, warnMode, value -> warnMode = value).width(110f);
            t.add(" Text: ");
            fields(t, warnText, value -> warnText = value).width(110f);
            TextButton pick = new TextButton("pick", Styles.logict);
            pick.clicked(() -> {
                String[] options = {"legacy", "centered"};
                String current = warnMode == null || warnMode.isEmpty() ? "centered" : warnMode;
                showSelect(pick, options, current, selected -> warnMode = selected, 2, cell -> cell.size(120f, 40f));
            });
            t.add(pick).size(70f, 32f).padLeft(4f);
        }).left().row();

        table.table(t -> {
            t.add("SpawnerCfg: ");
            fields(t, spawnerCfg, value -> spawnerCfg = value).width(width);
        }).left();
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
        builder.append(safeToken(alertTime, "10")).append(" ");
        builder.append(safeToken(raidTime, "5")).append(" ");
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
        // 15 parameters (plus instruction token) keeps this line at mlog's 16-token limit.
        builder.append(safeToken(warnMode, "centered")).append(" ").append(safeToken(warnText, "")).append(" ");
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
        builder.var(alertTime),
        builder.var(raidTime),
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
        public LVar alertTime;
        public LVar raidTime;
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

        public int raidCounter = 0;
        public float curTime = 0f;
        private String debugPhase = "";
        private float nextDebugLogTick = 0f;
        private float nextProgressLog = 0f;
        private boolean warnShown = false;

        private boolean cyclePrepared = false;
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

        private int blockCountScratch = 0;

        private final Vec2 pos = new Vec2();
        private final Vec2 target = new Vec2();
        private final Vec2 targetBase = new Vec2();

        public WHRaidUnitInstruction(
        LVar alertTime, LVar raidTime, LVar team, LVar unit, LVar count, LVar spread,
        LVar checkTeam, LVar minWave, LVar needItem, LVar needItemAmount, LVar needBlock, LVar needBlockAmount,
        LVar shield, LVar status, LVar statusDuration, LVar warnMode, LVar warnText, LVar spawnerCfg
        ){
            this.alertTime = alertTime;
            this.raidTime = raidTime;
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
            if(Vars.net.client() || Vars.state == null || Vars.state.rules == null || Vars.world == null){
                return;
            }

            if(curTime <= 0.0001f){
                ConditionSnapshot snapshot = inspectConditions();
                if(!snapshot.ok){
                    debugLogState(
                    "wait-condition",
                    "[WH][RaidLogic][debug] conditions not met key=@ @",
                    "auto",
                    formatCondition(snapshot)
                    );
                    exec.counter.numval--;
                    exec.yield = true;
                    return;
                }

                if(!cyclePrepared){
                    prepareCycle();
                }
            }

            float alert = Math.max(0f, alertTime.numf());
            float raid = Math.max(0.001f, raidTime.numf());
            float total = alert + raid;

            if(!warnShown){
                showWarnHud();
                warnShown = true;
            }

            if(curTime >= total){
                debugLogState(
                "finish",
                "[WH][RaidLogic][debug] finished key=@ spawned=@/@.",
                "auto",
                raidCounter,
                Math.max(0, count.numi())
                );
                reset();
                return;
            }

            exec.counter.numval--;
            exec.yield = true;
            curTime += Time.delta / 60f;

            if(curTime <= alert){
                logProgress(alert, total);
                return;
            }

            int totalCount = Math.max(0, count.numi());
            float raidTimer = curTime - alert;
            int raidCount = Mathf.round((raidTimer / raid) * totalCount);
            int delta = Math.max(0, raidCount - raidCounter);
            raidCounter = raidCount;
            logProgress(alert, total);

            for(int i = 0; i < delta; i++){
                spawnOne();
            }
        }

        private void reset(){
            curTime = 0f;
            raidCounter = 0;
            debugPhase = "";
            nextDebugLogTick = 0f;
            nextProgressLog = 0f;
            warnShown = false;
            cyclePrepared = false;
        }

        private void spawnOne(){
            if(!pickSpawnPosition(cachedSpreadWorld, pos)){
                debugLogState(
                "spawn-missing-point",
                "[WH][RaidLogic][debug] cannot spawn key=@ no spawn points found.",
                "auto"
                );
                return;
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
            debugLogState(
            "spawned",
            "[WH][RaidLogic][debug] spawned @ for key=@ at (@,@).",
            cachedUnitType.name,
            "auto",
            Strings.autoFixed(pos.x, 1),
            Strings.autoFixed(pos.y, 1)
            );
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
            spawnTeam,
            3f,
            cachedWarnMode,
            cachedWarnText
            );
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
            if(value.obj() instanceof UnitType type){
                return type;
            }

            String byName = extractContentName(value);
            if(byName != null){
                UnitType mappedByName = Vars.content.unit(byName);
                if(mappedByName != null) return mappedByName;
            }

            if(!value.isobj){
                int rawId = value.numi();
                if(rawId >= 10000){
                    UnitType mapped = Vars.content.unit(rawId - 10000);
                    if(mapped != null) return mapped;
                }

                UnitType unitById = Vars.content.unit(rawId);
                if(unitById != null) return unitById;
            }

            return WHUnitTypes.air4;
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

        private Item resolveItem(LVar value){
            if(value.obj() instanceof Item item){
                return item;
            }

            String byName = extractContentName(value);
            if(byName != null){
                Item mappedByName = Vars.content.item(byName);
                if(mappedByName != null) return mappedByName;
            }

            if(!value.isobj){
                int rawId = value.numi();
                if(rawId >= 10000){
                    Item mapped = Vars.content.item(rawId - 10000);
                    if(mapped != null) return mapped;
                }
                return Vars.content.item(rawId);
            }

            return null;
        }

        private Block resolveBlock(LVar value){
            if(value.obj() instanceof Block block){
                return block;
            }

            String byName = extractContentName(value);
            if(byName != null){
                Block mappedByName = Vars.content.block(byName);
                if(mappedByName != null) return mappedByName;
            }

            if(!value.isobj){
                int rawId = value.numi();
                if(rawId >= 10000){
                    Block mapped = Vars.content.block(rawId - 10000);
                    if(mapped != null) return mapped;
                }
                return Vars.content.block(rawId);
            }

            return null;
        }

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

            if(!value.isobj){
                int rawId = value.numi();
                int statusId = rawId >= 10000 ? rawId - 10000 : rawId;
                if(statusId < 0) return StatusEffects.none;

                StatusEffect resolved = Vars.content.getByID(ContentType.status, statusId);
                if(resolved != null) return resolved;
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

            String out = raw.trim();
            if(out.isEmpty()) return null;
            if(out.startsWith("___")) return null;

            if(out.startsWith("@")){
                out = out.substring(1);
            }

            if(out.isEmpty()) return null;
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

        private void resolveTarget(Vec2 out, Team spawnTeam){
            Team enemy = spawnTeam == Vars.state.rules.defaultTeam ? Vars.state.rules.waveTeam : Vars.state.rules.defaultTeam;
            Building core = enemy.core();
            if(core != null){
                out.set(core.x, core.y);
            }else{
                out.set(Vars.world.width() * Vars.tilesize * 0.5f, Vars.world.height() * Vars.tilesize * 0.5f);
            }
        }

        private boolean logicDebugEnabled(){
            return PortableAutoEventTrigger.debugForceAnyMode || PortableAutoEventTrigger.debugBypassMeet;
        }

        private void logProgress(float alert, float total){
            if(!logicDebugEnabled()) return;
            if(curTime < nextProgressLog) return;

            nextProgressLog = curTime + 1f;
            String phase = curTime <= alert ? "alert" : "raid";
            Log.info(
            "[WH][RaidLogic][debug] key=auto phase=@ t=@/@ spawned=@/@",
            phase,
            Strings.autoFixed(curTime, 2),
            Strings.autoFixed(total, 2),
            raidCounter,
            Math.max(0, count.numi())
            );
        }

        private void debugLogState(String phase, String text, Object... args){
            if(!logicDebugEnabled()) return;

            float now = Time.time;
            if(!phase.equals(debugPhase) || now >= nextDebugLogTick){
                debugPhase = phase;
                nextDebugLogTick = now + 60f;
                Log.info(text, args);
            }
        }

        private ConditionSnapshot inspectConditions(){
            ConditionSnapshot out = new ConditionSnapshot();
            out.team = resolveConditionTeam();
            out.wave = Vars.state.wave;
            out.minWaveRequired = Math.max(0, minWave.numi());

            // Supports multi item requirements with CSV:
            // needItem="@copper,@lead", needItemAmount="200,100"
            String[] itemNames = splitCsv(valueText(needItem));
            int[] itemAmounts = splitCsvInt(valueText(needItemAmount), itemNames.length);
            int itemRequiredCount = 0;
            boolean itemAllMet = true;
            for(int i = 0; i < itemNames.length; i++){
                int required = i < itemAmounts.length ? Math.max(0, itemAmounts[i]) : 0;
                if(required <= 0) continue;
                itemRequiredCount++;

                Item itemType = resolveItemByText(itemNames[i]);
                Building core = out.team.core();
                int owned = (itemType != null && core != null && core.items != null) ? core.items.get(itemType) : -1;
                if(owned < required){
                    itemAllMet = false;
                }
            }
            out.itemRequired = itemRequiredCount;
            out.itemOwned = itemAllMet ? itemRequiredCount : -1;

            // Supports multi block requirements with CSV:
            // needBlock="@duo,@scatter", needBlockAmount="2,1"
            String[] blockNames = splitCsv(valueText(needBlock));
            int[] blockAmounts = splitCsvInt(valueText(needBlockAmount), blockNames.length);
            int blockRequiredCount = 0;
            boolean blockAllMet = true;
            for(int i = 0; i < blockNames.length; i++){
                int required = i < blockAmounts.length ? Math.max(0, blockAmounts[i]) : 0;
                if(required <= 0) continue;
                blockRequiredCount++;

                Block blockType = resolveBlockByText(blockNames[i]);
                int owned = blockType == null ? -1 : countTeamBlocks(out.team, blockType);
                if(owned < required){
                    blockAllMet = false;
                }
            }
            out.blockRequired = blockRequiredCount;
            out.blockOwned = blockAllMet ? blockRequiredCount : -1;

            boolean waveOk = out.minWaveRequired <= 0 || out.wave >= out.minWaveRequired;
            boolean itemOk = out.itemRequired <= 0 || out.itemOwned == out.itemRequired;
            boolean blockOk = out.blockRequired <= 0 || out.blockOwned == out.blockRequired;
            out.ok = waveOk && itemOk && blockOk;
            return out;
        }

        private int countTeamBlocks(Team teamValue, Block block){
            blockCountScratch = 0;
            Groups.build.each(build -> {
                if(build.team == teamValue && build.block == block){
                    blockCountScratch++;
                }
            });
            return blockCountScratch;
        }

        private String formatCondition(ConditionSnapshot c){
            String teamName = c.team == null ? "null" : c.team.name;
            String itemState = c.itemRequired <= 0 ? "off" : c.itemOwned + "/" + c.itemRequired + "@csv";
            String blockState = c.blockRequired <= 0 ? "off" : c.blockOwned + "/" + c.blockRequired + "@csv";
            return Strings.format("team=@ wave=@/@ item=@ block=@", teamName, c.wave, c.minWaveRequired, itemState, blockState);
        }

        private String valueText(LVar value){
            if(value == null) return "";
            Object raw = value.obj();
            String text = raw == null ? value.name : String.valueOf(raw);
            return text == null ? "" : text.trim();
        }

        private String[] splitCsv(String raw){
            if(raw == null) return new String[0];
            String text = raw.trim();
            if(text.isEmpty()) return new String[0];
            return text.split(",");
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
            int blockRequired;
            int blockOwned;
            boolean ok;
        }
    }

    private static boolean containsDigit(String value){
        if(value == null) return false;
        for(int i = 0; i < value.length(); i++){
            if(Character.isDigit(value.charAt(i))) return true;
        }
        return false;
    }
}
