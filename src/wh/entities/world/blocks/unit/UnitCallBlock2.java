package wh.entities.world.blocks.unit;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
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
import mindustry.world.meta.*;
import mindustry.world.modules.*;
import wh.graphics.*;
import wh.ui.*;
import wh.ui.display.*;
import wh.util.*;

import static arc.Core.bundle;
import static mindustry.Vars.*;
import static wh.ui.UIUtils.*;

public class UnitCallBlock2 extends Block{
    public boolean useCoreItems = true;

    public Seq<UnitPlan> plans = new Seq<>(4);


    public float cooldownTime = 300f;
    public float spawnDelay = 90f;
    public float spawnReloadTime = cooldownTime;
    public float range = 200f;
    public float spawnRange = 120f;

    public int[] capacities = {};


    public UnitCallBlock2(String name){
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

        config(Vec2.class, (UnitCallBlockBuild build, Vec2 pos) -> {
            build.spawnPos = pos.clamp(0f, 0f, world.unitWidth(), world.unitHeight());
        });

        config(Integer.class, (UnitCallBlockBuild build, Integer i) -> {
            if(!configurable) return;

            if(build.currentPlan == i) return;
            build.currentPlan = i < 0 || i >= plans.size ? -1 : i;
            build.unitBuildProgress = 0;
            if(build.command != null && (build.unit() == null || !build.unit().commands.contains(build.command))){
                build.command = null;
            }
            build.CanSpawn();
        });

        config(UnitType.class, (UnitCallBlockBuild build, UnitType val) -> {
            if(!configurable) return;

            int next = plans.indexOf(p -> p.unit == val);
            if(build.currentPlan == next) return;
            build.currentPlan = next;
            build.unitBuildProgress = 0;
            if(build.command != null && !val.commands.contains(build.command)){
                build.command = null;
            }
            build.CanSpawn();
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
    public void setBars(){
        super.setBars();

        addBar("warmup", (UnitCallBlockBuild e) -> new Bar(
        () -> Core.bundle.get("bar.warmup") + ": " + Mathf.round(e.warmup * 100) + "%",
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
        e.unit() == null ? Units.getStringCap(e.team) : (e.unit().useUnitCap ? Units.getStringCap(e.team) : "∞")
        ),
        () -> Pal.power,
        () -> e.unit() == null ? 0f : (e.unit().useUnitCap ? (float)e.team.data().countType(e.unit()) / Units.getCap(e.team) : 1f)
        ));
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

        public ItemModule realItems(){
            return useCoreItems && team.data().hasCore() ? team.core().items : items;
        }

        @Override
        public Object config(){
            return currentPlan;
        }

        public float fraction(){
            return currentPlan == -1 ? 0 : unitBuildProgress / plans.get(currentPlan).time;
        }

        @Override
        public void updateTile(){

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

            if(!canSpawn){
                Drawn.overlayText(bundle.format("wh-unable-to-spawn"),
                target.x, target.y, tilesize * 2f, Pal.remove, true);
            }else{
                Drawn.overlayText(bundle.format("wh-able-to-spawn"),
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
            //auto-set to the first plan, it's better than nothing.
            if(currentPlan == -1){
                currentPlan = plans.indexOf(u -> u.unit.unlockedNow());
            }
            spawnPos.set(this);
        }

        @Override
        public int getMaximumAccepted(Item item){
            Building core = team.data().hasCore() ? team.core() : self();
            if(core == null) return Math.min(capacities[item.id], Mathf.round(capacities[item.id] * state.rules.unitCost(team)));
            return super.getMaximumAccepted(item);
        }

        @Override
        public boolean shouldConsume(){
            if(currentPlan == -1) return false;
            Building core = team.data().hasCore() ? team.core() : self();
            if(core != null){
                for(ItemStack stack : plans.get(currentPlan).requirements){
                    if(core.items.get(stack.item) < stack.amount){
                        return false;
                    }
                }
            }
            return enabled;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            return currentPlan != -1 && items.get(item) < getMaximumAccepted(item) &&
            Structs.contains(plans.get(currentPlan).requirements, stack -> stack.item == item);
        }

        public void costItems(){
            Building core = team.data().hasCore() ? team.core() : self();
            if(team.core() != null){
                for(ItemStack stack : plans.get(currentPlan).requirements){
                    core.items.remove(stack.item, stack.amount);
                }
            }else{
                consume();
            }
        }

        @Override
        public void buildConfiguration(Table table){

            Seq<UnitType> units = Seq.with(plans).map(u -> u.unit).retainAll(u -> u.unlockedNow() && !u.isBanned());

            if(units.any()){
                Table unitList = new Table();
                unitList.background(Tex.pane);
                unitList.defaults().growX().left().pad(OFFSET / 2);

                unitPlan(unitList);

                ScrollPane scrollPane = new ScrollPane(unitList);
                scrollPane.setScrollingDisabled(true, false);
                table.add(scrollPane).maxHeight(LEN * 7).growX().row();

                Table buttons = new Table();
                buttons.defaults().growX().height(LEN).pad(2);
                buttons.button("@select-position", Icon.move, () -> {
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
                    });
                });
                table.add(buttons).growX().left();
            }else{
                table.table(Styles.black3, t -> t.add("@none").color(Color.lightGray));
            }
        }

        public void unitPlan(Table table){
            ButtonGroup<ImageButton> group = new ButtonGroup<>();
            group.setMinCheckCount(0);
            table.clearChildren();

            Runnable rebuild = () -> {
                group.clear();
                for(UnitPlan plan : plans){
                    table.button(b -> {
                        b.image(plan.unit.uiIcon).size(LEN).scaling(Scaling.fit).left();
                        b.table(info -> {
                            info.add("[accent]" + plan.unit.localizedName + "[]").left().row();
                            info.add("[gray]" + plan.unit.description).left().wrap().width(350f);
                            info.add(new Label(() ->
                            "@[lightgray]buildtime: " +
                            Strings.fixed(plan.time / 60f, 0) + "*second"
                            )).left().padBottom(OFFSET);
                        }).growX().left();

                        b.table(consumption -> {
                            consumption.background(Styles.none);
                            consumption.defaults().pad(2).left();
                            for(ItemStack stack : plan.requirements){
                                consumption.add(new ItemImageDynamic(stack.item, () -> stack.amount, realItems())).left();
                            }
                        }).growX().padBottom(OFFSET);
                    }, Styles.flatTogglet, () -> {
                        configure(plans.indexOf(plan));
                        CanSpawn();
                    }).update(b -> b.setChecked(currentPlan == plans.indexOf(plan))).group(group).width(LEN * 14f).pad(OFFSET).row();
                }
            };
            rebuild.run();

        }

        public boolean CanSpawn(){
            if(!spawnPos.equals(this)){
             /*   Log.info("Spawning at " + spawnPos+" Units "+unit()+" CanSpawn "+canSpawn);*/
                canSpawn = WHUtils.hasAnyValidSpawnPosition(unit(),spawnPos.x, spawnPos.y, spawnRange);
            }else{
                canSpawn = false;
            }
            return canSpawn;
        }

        public void spawn(UnitPlan plan){
            if(!isValid()) return;

            Vec2 target = spawnPos;

            if(canSpawn) WHUtils.spawnUnit(team, target.x, target.y, angleTo(target), spawnRange, spawnReloadTime, spawnDelay, plan.unit, 1, plan.airdrop, s -> {
                if(commandPos != null) s.commandPos.set(commandPos);
            });
            costItems();
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
            if(revision >= 2){
                commandPos = TypeIO.readVecNullable(read);
            }

            if(revision >= 3){
                command = TypeIO.readCommand(read);
            }
        }
    }
}
