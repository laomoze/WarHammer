package wh.entities.event.logic;

import arc.math.*;
import arc.math.geom.*;
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
import mindustry.world.*;
import wh.content.*;
import wh.entities.*;

public class WHSpawnerStatement extends LStatement{
    public String run = "0";
    public String team = "@crux";
    public String unit = "@air4";
    public String amount = "1";
    public String x = "-1";
    public String y = "-1";
    public String spread = "8";
    public String spawnerCfg = "12|0";
    public String shield = "-1";
    public String status = "@none";
    public String statusDuration = "0";

    public WHSpawnerStatement(){
    }

    public WHSpawnerStatement(String[] tokens){
        if(tokens.length > 1) run = tokens[1];
        if(tokens.length > 2) team = tokens[2];
        if(tokens.length > 3) unit = tokens[3];
        if(tokens.length > 4) amount = tokens[4];
        if(tokens.length > 5) x = tokens[5];
        if(tokens.length > 6) y = tokens[6];
        if(tokens.length > 7) spread = tokens[7];
        if(tokens.length > 8) spawnerCfg = tokens[8];
        if(tokens.length > 9) shield = tokens[9];
        if(tokens.length > 10){
            String packed = tokens[10];
            int split = packed.indexOf('|');
            if(split >= 0){
                status = packed.substring(0, split);
                if(split + 1 < packed.length()) statusDuration = packed.substring(split + 1);
            }else{
                status = packed;
                if(tokens.length > 11) statusDuration = tokens[11];
            }
        }
    }

    @Override
    public void build(Table table){
        float width = 220f;

        table.table(t -> {
            t.add("Run: ");
            fields(t, run, value -> run = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Team: ");
            fields(t, team, value -> team = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Unit: ");
            fields(t, unit, value -> unit = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Amount: ");
            fields(t, amount, value -> amount = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("X: ");
            fields(t, x, value -> x = value).width(100f);
            t.add(" Y: ");
            fields(t, y, value -> y = value).width(100f);
        }).left().row();

        table.table(t -> {
            t.add("Spread(tiles): ");
            fields(t, spread, value -> spread = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("SpawnerCfg: ");
            fields(t, spawnerCfg, value -> spawnerCfg = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Shield: ");
            fields(t, shield, value -> shield = value).width(width);
        }).left().row();

        table.table(t -> {
            t.add("Status: ");
            fields(t, status, value -> status = value).width(100f);
            t.add(" Duration(s): ");
            fields(t, statusDuration, value -> statusDuration = value).width(100f);
        }).left();
    }

    @Override
    public void write(StringBuilder builder){
        builder.append("wh-spawner ");
        builder.append(safe(run, "0")).append(" ");
        builder.append(safe(team, "@crux")).append(" ");
        builder.append(safe(unit, "@air4")).append(" ");
        builder.append(safe(amount, "1")).append(" ");
        builder.append(safe(x, "-1")).append(" ");
        builder.append(safe(y, "-1")).append(" ");
        builder.append(safe(spread, "8")).append(" ");
        builder.append(safe(spawnerCfg, "12|0")).append(" ");
        builder.append(safe(shield, "-1")).append(" ");
        builder.append(safe(status, "@none")).append("|").append(safe(statusDuration, "0"));
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
        builder.var(spread),
        builder.var(spawnerCfg),
        builder.var(shield),
        builder.var(status),
        builder.var(statusDuration)
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

    public static class WHSpawnerInstruction implements LExecutor.LInstruction{
        public LVar run;
        public LVar team;
        public LVar unit;
        public LVar amount;
        public LVar x;
        public LVar y;
        public LVar spread;
        public LVar spawnerCfg;
        public LVar shield;
        public LVar status;
        public LVar statusDuration;

        private boolean executed;
        private final Vec2 pos = new Vec2();
        private final Vec2 target = new Vec2();

        public WHSpawnerInstruction(
        LVar run, LVar team, LVar unit, LVar amount, LVar x, LVar y, LVar spread,
        LVar spawnerCfg, LVar shield, LVar status, LVar statusDuration
        ){
            this.run = run;
            this.team = team;
            this.unit = unit;
            this.amount = amount;
            this.x = x;
            this.y = y;
            this.spread = spread;
            this.spawnerCfg = spawnerCfg;
            this.shield = shield;
            this.status = status;
            this.statusDuration = statusDuration;
        }

        @Override
        public void run(LExecutor exec){
            if(Vars.net.client() || Vars.state == null || Vars.state.rules == null || Vars.world == null) return;

            boolean shouldRun = run.numi() != 0;
            if(!shouldRun){
                executed = false;
                return;
            }
            if(executed) return;

            UnitType unitType = resolveUnitType(unit);
            Team spawnTeam = resolveTeam(team);
            int count = Math.max(0, amount.numi());
            float spreadWorld = Math.max(0f, spread.numf()) * Vars.tilesize;
            float life = parseSpawnerLifetime(spawnerCfg);
            boolean airdrop = parseSpawnerAirdrop(spawnerCfg);
            float shieldToApply = shield.numf();
            StatusEffect statusToApply = resolveStatus(status);
            float duration = Math.max(0f, statusDuration.numf()) * Time.toSeconds;

            float baseX = x.numf();
            float baseY = y.numf();
            boolean useSpawnPoint = baseX < 0f || baseY < 0f;

            for(int i = 0; i < count; i++){
                if(useSpawnPoint){
                    if(!pickSpawnPosition(spreadWorld, pos)) continue;
                }else{
                    pos.set(baseX + Mathf.range(spreadWorld), baseY + Mathf.range(spreadWorld));
                }

                resolveTarget(target, spawnTeam);
                float rot = Angles.angle(pos.x, pos.y, target.x, target.y);

                Spawner spawner = new Spawner()
                .init(unitType, spawnTeam, pos, rot, life, airdrop)
                .setShieldToApply(shieldToApply);

                if(statusToApply != null && statusToApply != StatusEffects.none && duration > 0f){
                    spawner.setStatus(statusToApply, duration);
                }

                spawner.add();
            }

            executed = true;
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

            if(!value.isobj){
                int rawId = value.numi();
                if(rawId >= 10000){
                    UnitType mapped = Vars.content.unit(rawId - 10000);
                    if(mapped != null) return mapped;
                }
                UnitType byId = Vars.content.unit(rawId);
                if(byId != null) return byId;
            }

            return WHUnitTypes.air4;
        }

        private StatusEffect resolveStatus(LVar value){
            if(value.obj() instanceof StatusEffect effect) return effect;

            String name = extractContentName(value);
            if(name != null){
                if(name.equals("none")) return StatusEffects.none;
                StatusEffect byName = Vars.content.statusEffect(name);
                if(byName != null) return byName;
            }

            if(!value.isobj){
                int rawId = value.numi();
                int statusId = rawId >= 10000 ? rawId - 10000 : rawId;
                if(statusId < 0) return StatusEffects.none;
                StatusEffect byId = Vars.content.getByID(ContentType.status, statusId);
                if(byId != null) return byId;
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
            if(raw == null || raw.trim().isEmpty()) return new String[]{"12", "0"};
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

        private void resolveTarget(Vec2 out, Team spawnTeam){
            Team enemy = spawnTeam == Vars.state.rules.defaultTeam ? Vars.state.rules.waveTeam : Vars.state.rules.defaultTeam;
            Building core = enemy.core();
            if(core != null){
                out.set(core.x, core.y);
            }else{
                out.set(Vars.world.width() * Vars.tilesize * 0.5f, Vars.world.height() * Vars.tilesize * 0.5f);
            }
        }
    }
}
