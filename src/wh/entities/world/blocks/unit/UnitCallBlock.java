package wh.entities.world.blocks.unit;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.ai.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import mindustry.world.modules.*;
import wh.graphics.*;
import wh.ui.*;
import wh.util.*;

import static arc.Core.bundle;
import static mindustry.Vars.*;
import static wh.ui.UIUtils.LEN;

public class UnitCallBlock extends Block{
    public boolean useCoreItems = true;

    public Seq<UnitPlan> plans = new Seq<>(4);

    public float cooldownTime = 300f;
    public float spawnDelay = 90f;
    public float spawnReloadTime = cooldownTime;
    public float range = 200f;
    public float spawnRange = 120f;

    public int[] capacities = {};

    public drawer drawBlock = b -> {
    };

    public DrawBlock drawer = new DrawMulti(new DrawRegion("-bottom"));

    public UnitCallBlock(String name){
        super(name);
        size = 3;
        copyConfig = true;
        update = true;
        sync = true;
        configurable = true;
        acceptsItems = true;
        unloadable = true;
        solid = true;
        commandable = true;
        hasPower = hasItems = true;
        timers = 3;
        envEnabled = Env.any;
        category = Category.units;
        logicConfigurable = true;
        separateItemCapacity = true;
        group = BlockGroup.units;

        itemCapacity = 0;

        config(Vec2.class, (UnitCallBlockBuild build, Vec2 pos) -> {
            build.spawnPos = pos.clamp(0f, 0f, world.unitWidth(), world.unitHeight());
        });

        config(Integer.class, (UnitCallBlockBuild build, Integer i) -> {
            build.selectPlan(i);
        });

        config(UnitType.class, (UnitCallBlockBuild build, UnitType val) -> {
            int next = plans.indexOf(p -> p.unit == val);
            build.selectPlan(next);
        });

        config(UnitCommand.class, (UnitCallBlockBuild build, UnitCommand command) -> build.command = command);
        configClear((UnitCallBlockBuild build) -> build.command = null);

    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return super.canPlaceOn(tile, team, rotation);
    }

    @Override
    public void init(){
        super.init();
        capacities = new int[Vars.content.items().size];
        for(UnitPlan plan : plans){
            for(ItemStack stack : plan.requirements){
                int perItem = Math.max(1, stack.amount) * 2;
                capacities[stack.item.id] = Math.max(capacities[stack.item.id], perItem);
                itemCapacity = Math.max(itemCapacity, capacities[stack.item.id]);
            }
        }
        consumeBuilder.each(c -> c.multiplier = b -> state.rules.unitCost(b.team));
    }

    @Override
    public void getPlanConfigs(Seq<UnlockableContent> options){
        for(var plan : plans){
            if(!plan.unit.isBanned()){
                options.add(plan.unit);
            }
        }
    }

    @Override
    public TextureRegion[] icons(){
        return drawer.finalIcons(this);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("warmup", (UnitCallBlockBuild e) -> new Bar(
        () -> (bundle.has("bar.warmup") ? bundle.get("bar.warmup") :
        (bundle.has("bar.wh-warmup") ? bundle.get("bar.wh-warmup") : "warmup")) +
        ": " + Mathf.round(e.warmup * 100) + "%",
        () -> Pal.lightOrange,
        () -> e.warmup
        ));
        addBar("progress", (UnitCallBlockBuild e) -> new Bar("bar.progress", Pal.ammo, e::fraction));

        addBar("units", (UnitCallBlockBuild e) ->
        new Bar(
        () -> e.unit() == null ? "[lightgray]" + Iconc.cancel :
        Core.bundle.format("bar.unitcap",
        Fonts.getUnicodeStr(e.unit().name),
        e.team.data().countType(e.unit()),
        e.unit() == null ? Units.getStringCap(e.team) : (e.unit().useUnitCap ? Units.getStringCap(e.team) : "inf")
        ),
        () -> Pal.power,
        () -> e.unit() == null ? 0f : (e.unit().useUnitCap ? (float)e.team.data().countType(e.unit()) / Units.getCap(e.team) : 1f)
        ));
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
        stats.add(Stat.output, table -> {
            table.left();
            table.defaults().left().growX().pad(4f);
            table.row();
            for(UnitPlan plan : plans){
                if(plan.unit == null || plan.unit.isBanned()) continue;
                table.table(Styles.grayPanel, panel -> {
                    panel.left().top();
                    panel.defaults().left().top().pad(3f);
                    panel.margin(6f);

                    panel.image(plan.unit.uiIcon).size(40f).pad(4f).scaling(Scaling.fit);
                    panel.table(info -> {
                        info.left();
                        info.defaults().left().padBottom(2f);
                        info.add(plan.unit.localizedName).left().row();
                        info.add("[lightgray]" + Core.bundle.get("stat.productiontime") + ": [white]" +
                        Strings.autoFixed(plan.time / 60f, 2) + "s").left();
                    }).growX().left().top().pad(4f);

                    panel.table(cost -> {
                        cost.left();
                        if(plan.requirements.length == 0){
                            cost.add("[lightgray]-").left();
                        }else{
                            for(ItemStack stack : plan.requirements){
                                cost.add(StatValues.stack(stack.item, stack.amount, true)).pad(3f);
                            }
                        }
                    }).left().top().padLeft(8f);
                }).left().growX().fillX().pad(2f);
                table.row();
            }
        });
    }

    @Override
    public void load(){
        super.load();

        drawer.load(this);
    }


    @Override
    public boolean outputsItems(){
        return false;
    }

    public static class UnitPlan{
        public UnitType unit;
        public ItemStack[] requirements;
        public float time;
        public boolean airdrop;

        public UnitPlan(UnitType unit, float time, boolean airdrop, ItemStack[] requirements){
            this.unit = unit;
            this.time = time;
            this.airdrop = airdrop;
            this.requirements = requirements;
        }

        UnitPlan(){
        }
    }

    public interface drawer{
        void draw(UnitCallBlockBuild build);
    }

    public class UnitCallBlockBuild extends Building{
        public Vec2 spawnPos = new Vec2().set(this);

        public float warmup;
        public float unitBuildProgress;

        public @Nullable Vec2 commandPos;
        public @Nullable UnitCommand command;
        public int currentPlan = -1;
        public boolean canSpawn;

        @Override
        public Vec2 getCommandPosition(){
            return commandPos;
        }

        @Override
        public void onCommand(Vec2 target){
            commandPos = target;
        }

        public @Nullable UnitType unit(){
            return currentPlan == -1 ? null : plans.get(currentPlan).unit;
        }

        public void selectPlan(int next){
            int resolved = next < 0 || next >= plans.size ? -1 : next;
            if(currentPlan == resolved) return;

            currentPlan = resolved;
            unitBuildProgress = 0f;

            UnitType selected = unit();
            if(command != null && (selected == null || !selected.commands.contains(command))){
                command = null;
            }
            updateCanSpawnState();
        }

        public boolean usesCoreItemsNow(){
            return useCoreItems && team.data().hasCore() && team.core() != null;
        }

        public int requirementAmount(ItemStack stack){
            return Math.max(1, Mathf.ceil(stack.amount * state.rules.unitCost(team)));
        }

        public ItemModule realItems(){
            return usesCoreItemsNow() ? team.core().items : items;
        }

        public boolean hasRequirements(UnitPlan plan){
            if(plan == null) return false;
            if((state != null && state.rules != null) && (state.rules.infiniteResources || state.rules.editor)) return true;
            ItemModule source = realItems();
            for(ItemStack stack : plan.requirements){
                if(source.get(stack.item) < requirementAmount(stack)){
                    return false;
                }
            }
            return true;
        }

        @Override
        public Object config(){
            return currentPlan;
        }

        public float fraction(){
            return currentPlan == -1 ? 0 : unitBuildProgress / plans.get(currentPlan).time;
        }

        @Override
        public void draw(){
            super.draw();
            drawer.draw(this);
            drawBlock.draw(this);
        }

        @Override
        public void drawLight(){
            super.drawLight();
            drawer.drawLight(this);
        }

        @Override
        public float warmup(){
            return warmup;
        }

        @Override
        public void updateTile(){
            super.updateTile();
            if(efficiency > 0 && power.status > 0.5f){
                if(Mathf.equal(warmup, 1, 0.0015F)) warmup = 1f;
                else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
            }else{
                if(Mathf.equal(warmup, 0, 0.0015F)) warmup = 0f;
                else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
            }

            if(currentPlan < 0 || currentPlan >= plans.size){
                currentPlan = -1;
            }

            if(currentPlan != -1){
                updateCanSpawnState();
            }else{
                canSpawn = false;
            }

            if(warmup > 0.98 && currentPlan != -1 && canSpawn){
                unitBuildProgress += edelta() * Vars.state.rules.unitBuildSpeed(team);
            }

            if(currentPlan != -1 && canSpawn){
                UnitPlan plan = plans.get(currentPlan);

                //make sure to reset plan when the unit got banned after placement
                if(plan.unit.isBanned()){
                    currentPlan = -1;
                    return;
                }

                if(unitBuildProgress >= plan.time){
                    unitBuildProgress %= 1f;
                    spawn(plan);
                }
                unitBuildProgress = Mathf.clamp(unitBuildProgress, 0, plan.time);
            }else{
                unitBuildProgress = 0f;
            }
        }

        @Override
        public void drawConfigure(){
            Color color = team.color;
            Drawf.dashCircle(x, y, range, color);
            Draw.color(color);
            Lines.square(x, y, block.size * tilesize / 2f + 1.0f);

            Vec2 target = spawnPos;
            Draw.alpha(1f);
            Drawf.dashCircle(target.x, target.y, spawnRange, color);

            Draw.color(Pal.gray);
            Drawn.posSquareLink(color, 1.5f, 3.5f, true, this, target);
            Draw.color();

            String ableText = bundle.has("wh-able-to-spawn") ? bundle.get("wh-able-to-spawn") : "spawn ok";
            String unableText = bundle.has("wh-unable-to-spawn") ? bundle.get("wh-unable-to-spawn") : "cannot spawn";
            if(!canSpawn){
                Drawn.overlayText(unableText,
                target.x, target.y, tilesize * 2f, Pal.remove, true);
            }else{
                Drawn.overlayText(ableText,
                target.x, target.y, tilesize * 2f, Pal.accent, true);
            }

            if(core() != null) Drawn.posSquareLinkArr(color, 1.5f, 3.5f, true, false, this, core());
            Draw.reset();
        }

        @Override
        public Object senseObject(LAccess sensor){
            if(sensor == LAccess.config) return currentPlan == -1 ? null : plans.get(currentPlan).unit;
            return super.senseObject(sensor);
        }

        @Override
        public void created(){
            super.created();
            //auto-set to the first plan, it's better than nothing.
            if(currentPlan == -1){
                currentPlan = plans.indexOf(u -> u.unit.unlockedNow());
            }
            spawnPos.set(this);
            updateCanSpawnState();
        }


        @Override
        public int getMaximumAccepted(Item item){
            if(usesCoreItemsNow()) return 0;
            int base = capacities[item.id];
            return Math.min(base, Mathf.ceil(base * state.rules.unitCost(team)));
        }

        @Override
        public boolean shouldConsume(){
            if(currentPlan == -1) return false;
            return enabled && hasRequirements(plans.get(currentPlan));
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return !usesCoreItemsNow() && currentPlan != -1 && items.get(item) < getMaximumAccepted(item) &&
            Structs.contains(plans.get(currentPlan).requirements, stack -> stack.item == item);
        }

        public void costItems(){
            if(currentPlan == -1) return;
            if((state != null && state.rules != null) && (state.rules.infiniteResources || state.rules.editor)) return;
            ItemModule source = realItems();
            for(ItemStack stack : plans.get(currentPlan).requirements){
                source.remove(stack.item, requirementAmount(stack));
            }
        }

        @Override
        public void buildConfiguration(Table table){

            Seq<UnitType> units = Seq.with(plans).map(u -> u.unit).retainAll(u -> u.unlockedNow() && !u.isBanned());
            final float panelWidth = LEN * 8.2f;

            if(units.any()){
                Table unitList = new Table();
                unitList.background(Styles.black6);
                unitList.defaults().growX().left().pad(4f);

                unitPlan(unitList);

                ScrollPane scrollPane = new ScrollPane(unitList);
                scrollPane.setScrollingDisabled(true, false);
                scrollPane.setFadeScrollBars(false);
                scrollPane.setOverscroll(false, false);
                table.add(scrollPane).width(panelWidth).minWidth(panelWidth).maxWidth(panelWidth).maxHeight(LEN * 7f).left().row();

                Table buttons = new Table();
                buttons.defaults().growX().height(LEN * 0.95f).pad(3f);
                buttons.button("@wh-airborne-select-pos", Icon.move, Styles.flatt, () -> {
                    UIUtils.selectPos(table, pos -> {
                        Vec2 worldPos = new Vec2(
                        pos.x * tilesize + tilesize / 2f,
                        pos.y * tilesize + tilesize / 2f
                        );

                        float dst = worldPos.dst(this);
                        if(dst > range){
                            worldPos.sub(this).setLength(range).add(this);
                        }

                        configure(worldPos);
                        updateCanSpawnState();
                    });
                });
                table.add(buttons).width(panelWidth).minWidth(panelWidth).maxWidth(panelWidth).left();
            }else{
                table.table(Styles.black3, t -> t.add("@none").color(Color.lightGray));
            }
        }

        public void unitPlan(Table table){
            ButtonGroup<Button> group = new ButtonGroup<>();
            group.setMinCheckCount(0);
            table.clearChildren();
            Button.ButtonStyle planStyle = new Button.ButtonStyle(Styles.black8, Styles.black8, Styles.black8);
            planStyle.over = Styles.black8;
            planStyle.checked = Styles.black8;
            planStyle.disabled = Styles.black8;

            Runnable rebuild = () -> {
                group.clear();
                for(int i = 0; i < plans.size; i++){
                    UnitPlan plan = plans.get(i);
                    int planIndex = i;
                    table.button(b -> {
                        b.left();
                        b.defaults().left().growX();

                        b.stack(
                        new UIUtils.PlanBackBar(
                        () -> planIndex == currentPlan ? (canSpawn ? Pal.accent : Pal.remove) : Pal.gray,
                        () -> {
                            float timeSeconds = plan.time / 60f;
                            if(planIndex == currentPlan){
                                return "[white]" + plan.unit.localizedName + " [lightgray]- " +
                                Strings.autoFixed(Mathf.clamp(fraction()) * 100f, 0) + "%";
                            }
                            return "[white]" + plan.unit.localizedName + " [lightgray]- " + Strings.autoFixed(timeSeconds, 1) + "s";
                        },
                        () -> planIndex == currentPlan ? Mathf.clamp(fraction()) : 0f
                        ),
                        new Table(icon -> icon.left().image(plan.unit.uiIcon).size(LEN * 0.75f).padLeft(8f).padTop(4f).padBottom(4f).scaling(Scaling.fit)),
                        new Table(time -> {
                            time.right();
                            time.label(() -> "[lightgray]" + Strings.autoFixed(plan.time / 60f, 1) + "s").padRight(8f);
                        }),
                        new Table(){
                            {
                                touchable = Touchable.disabled;
                            }

                            @Override
                            public void draw(){
                                super.draw();
                                if(planIndex != currentPlan) return;

                                Color edge = canSpawn ? Pal.accent : Pal.remove;
                                float alpha = parentAlpha * color.a;

                                Draw.color(edge, 0.95f * alpha);
                                Lines.stroke(2f);
                                Lines.rect(x + 1f, y + 1f, Math.max(0f, width - 2f), Math.max(0f, height - 2f));

                                Draw.color(edge, 0.4f * alpha);
                                Lines.stroke(1f);
                                Lines.rect(x + 3f, y + 3f, Math.max(0f, width - 6f), Math.max(0f, height - 6f));
                                Draw.reset();
                            }
                        }
                        ).height(LEN * 0.72f).growX();
                    }, planStyle, () -> {
                        configure(planIndex);
                        updateCanSpawnState();
                    }).update(b -> b.setChecked(currentPlan == planIndex)).group(group).growX().pad(3f).row();
                }
            };
            rebuild.run();

        }

        public boolean updateCanSpawnState(){
            UnitType current = unit();
            if(current == null || spawnPos == null || currentPlan < 0 || currentPlan >= plans.size){
                canSpawn = false;
            }else{
                UnitPlan plan = plans.get(currentPlan);
                canSpawn = plan != null
                && plan.unit != null
                && !plan.unit.isBanned()
                && hasRequirements(plan)
                && (team == Vars.state.rules.waveTeam || Units.canCreate(team, current))
                && WHUtils.hasAnyValidSpawnPosition(current, spawnPos.x, spawnPos.y, spawnRange);
            }
            return canSpawn;
        }

        public boolean CanSpawn(){
            return updateCanSpawnState();
        }

        public void spawn(UnitPlan plan){
            if(!isValid()) return;

            Vec2 target = spawnPos;
            boolean spawned = false;

            if(canSpawn){
                spawned = WHUtils.spawnUnit(team, target.x, target.y, angleTo(target), spawnRange, spawnReloadTime, spawnDelay, plan.unit, 1, plan.airdrop, s -> {
                    if(commandPos != null) s.commandPos.set(commandPos);
                });
            }

            if(spawned){
                costItems();
            }else{
                canSpawn = false;
            }
        }

        @Override
        public byte version(){
            return 3;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(warmup);
            write.f(unitBuildProgress);
            write.s(currentPlan);
            write.bool(canSpawn);
            TypeIO.writeVecNullable(write, spawnPos);
            TypeIO.writeVecNullable(write, commandPos);
            TypeIO.writeCommand(write, command);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            warmup = read.f();
            unitBuildProgress = read.f();
            currentPlan = read.s();
            canSpawn = read.bool();
            spawnPos = TypeIO.readVecNullable(read);
            if(spawnPos == null){
                spawnPos = new Vec2(x, y);
            }
            if(revision >= 2){
                commandPos = TypeIO.readVecNullable(read);
            }

            if(revision >= 3){
                command = TypeIO.readCommand(read);
            }
        }
    }
}
