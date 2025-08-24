package wh.entities.world.blocks.unit;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.core.World;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.io.TypeIO;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.consumers.*;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;
import mindustry.world.modules.ItemModule;
import wh.content.WHFx;
import wh.entities.world.blocks.defense.AirRaiderCallBlock.*;
import wh.graphics.Drawn;
import wh.ui.UIUtils;
import wh.ui.display.ItemImageDynamic;
import wh.util.WHUtils;

import java.util.Arrays;
import java.util.Objects;

import static arc.graphics.g2d.Draw.scl;
import static mindustry.Vars.*;
import static mindustry.type.ItemStack.with;
import static wh.content.WHContent.arrowRegion;
import static wh.content.WHContent.pointerRegion;
import static wh.util.WHUtils.regSize;

public class UnitCallBlock extends Block {
    public boolean useCoreItems = true;
    public final IntMap<UnitSet> calls = new IntMap<>();

    public Vec2 commandPos = null;
    protected static final Vec2 linkVec = new Vec2();
    public float cooldownTime = 300f;
    public float spawnDelay = 90f;
    public float spawnReloadTime = cooldownTime;
    public float range = 200f;
    public float spawnRange = 120f;
    public float buildSpeedMultiplierCoefficient = 1;

    public enum ProductionType {
        single, plan, cancel
    }

    public UnitCallBlock(String name) {
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
        consumePower(10f);

        requirements(Category.units, with(Items.copper, 1200, Items.lead, 350, Items.graphite, 300, Items.surgeAlloy, 325, Items.silicon, 325));

        config(Boolean.class, (UnitCallBlockBuild tile, Boolean i) -> {
            if (i) tile.spawn(tile.getSet());
            else tile.startBuild(0, 0);
        });

     /*   config(IntSeq.class, (UnitCallBlockBuild tile, IntSeq seq) -> {
            if (seq.size < 3) return;
            if (seq.get(0) == 0) {
                tile.startBuild(seq.get(1), seq.get(2));
            } else if (seq.get(0) == 1) {
                tile.planSpawnID = seq.get(1);
                tile.planSpawnNum = seq.get(2);
                tile.continuousProduction = false;
            } else if (seq.get(0) == 2) {
                tile.planSpawnID = seq.get(1);
                tile.planSpawnNum = seq.get(2);
                tile.continuousProduction = true;
            }
        });
        configClear((UnitCallBlockBuild tile) -> tile.startBuild(0, 0));*/

        config(ProductionType.class, (UnitCallBlockBuild tile, ProductionType type) -> {
            switch(type){
                case single -> tile.startBuild(tile.planSpawnID, tile.planSpawnNum);
                case plan -> {
                    tile.continuousProduction = true;
                    tile.startBuild(tile.planSpawnID, tile.planSpawnNum);
                }
                case cancel -> {
                    tile.continuousProduction = false;
                    tile.startBuild(0, 0);
                }
            }
        });

        configClear((UnitCallBlockBuild tile) -> tile.configure(ProductionType.cancel));
    }

    public static class UnitSet implements Comparable<UnitSet> {
        public Seq<ItemStack> requirements = new Seq<>();
        public UnitType type;
        public float costTime;
        public final byte[] sortIndex;
        public boolean airdrop;

        public UnitSet(UnitType type, byte[] sortIndex, float costTime, boolean airdrop, ItemStack... requirements) {
            Arrays.sort(requirements, Structs.comparingInt(j -> j.item.id));
            this.type = type;
            this.sortIndex = sortIndex;
            this.costTime = costTime;
            this.airdrop = airdrop;
            this.requirements.addAll(requirements);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(type.name.hashCode());
            result = 31 * result + Arrays.hashCode(sortIndex);
            return result;
        }

        public ItemStack[] dynamicRequirements(Team team) {
            return ItemStack.mult(requirements.toArray(ItemStack.class), state.rules.unitCost(team));
        }

        @Override
        public int compareTo(UnitSet o) {
            return type.compareTo(o.type);
        }

        public float costTime() {
            return costTime;
        }
    }

    public void addSets(UnitSet... sets) {
        for (UnitSet set : sets) {
            calls.put(set.hashCode(), set);
        }
    }


    public float range() {
        return range;
    }


    public class UnitCallBlockBuild extends Building {
        public int spawnID = 0;
        public float buildProgress;
        public int buildingSpawnNum = 0;
        public Vec2 Pos;
        public boolean jammed = false;
        public int link = -1;

        public float cooldown;
        public boolean cooling;

        public float warmup;
        public float totalProgress;

        public int planSpawnID = 0;
        public int planSpawnNum = 0;
        public int spawnNum = 1;
        public boolean continuousProduction = false;
        public float fadeAlpha = 0f;

        @Override
        public void configure(Object value) {
            if(value instanceof ProductionType type){
                switch(type){
                    case single -> {
                        if(continuousProduction){
                            continuousProduction = false;
                            startBuild(0, 0);
                        }else{
                            startBuild(planSpawnID, planSpawnNum);
                        }
                    }
                    case plan -> {
                        continuousProduction = true;
                        startBuild(planSpawnID, planSpawnNum);
                    }
                    case cancel -> {
                        continuousProduction = false;
                        startBuild(0, 0);
                    }
                }
            }
            super.configure(value);
        }



      /*  @Override
        public void configure(Object value) {
            if (value instanceof IntSeq seq) {
                if (seq.size >= 3) {
                    int mode = seq.get(0);
                    if (mode == 2) {
                        if (continuousProduction && planSpawnID == seq.get(1)) {
                            continuousProduction = false;
                            planSpawnID = 0;
                            planSpawnNum = 0;
                            spawnID = 0;
                            startBuild(0, 0);
                            return;
                        } else {
                            continuousProduction = true;
                            planSpawnID = seq.get(1);
                            planSpawnNum = seq.get(2);
                        }
                        startBuild(planSpawnID, planSpawnNum);
                    } else if (mode == 3) {
                        continuousProduction = false;
                        planSpawnNum = 0;
                    }
                }
            }
            super.configure(value);
        }*/

        @Override
        public void onCommand(Vec2 target) {
            hitbox(Tmp.r1);
            if (Tmp.r1.contains(target)) commandPos = null;
            else commandPos = target;
        }

        @Override
        public Vec2 getCommandPosition() {
            return commandPos;
        }

        @Override
        public void draw() {
            super.draw();
           /* if (fadeAlpha > 0.01f) {
                Draw.z(Layer.effect);
                Color colorFrom = Color.red;
                Color colorTo = colorFrom.cpy().a(0f).lerp(Pal.surge, Mathf.absin(Time.time, 1f, 0.12f));
                float a = fadeAlpha;

                Color tmpC1 = colorFrom.cpy().mulA(a);
                Color tmpC2 = colorTo.cpy().mulA(a);
                float c1 = tmpC1.toFloatBits();
                float c2 = tmpC2.toFloatBits();
                for (int i = 0; i < 2; i++) {
                    float baseWidth = 4f;
                    float pulse = Mathf.absin(Time.time + i * 40f, 6f, 5f);
                    float width = baseWidth + pulse / 2;
                    float length = (25f + pulse * 2f)*fadeAlpha;
                    float rot = Time.time * 4 % 360f + i * 180f;

                    float angle = rot + 90f;
                    Tmp.v1.trns(angle, width / 2f);
                    Tmp.v2.trns(angle, -width / 2f);
                    Tmp.v3.trns(rot, length);
                    Fill.quad(
                            x, y, c1,
                            x, y, c1,
                            x + Tmp.v3.x + Tmp.v1.x, y + Tmp.v3.y + Tmp.v1.y, c2,
                            x + Tmp.v3.x + Tmp.v2.x, y + Tmp.v3.y + Tmp.v2.y, c2
                    );
                }
                Draw.blend();
                Draw.reset();
            }*/

            if (isCalling()) {
                Draw.z(Layer.bullet);
                Draw.color(getColor(getSet()));
                for (int l = 0; l < 4; l++) {
                    float angle = 45 + 90 * l;
                    float regSize = regSize(getType()) / 3f + scl;
                    for (int i = 0; i < 4; i++) {
                        Tmp.v1.trns(angle, (i - 4) * tilesize * 2);
                        float f = (100 - (totalProgress - 25 * i) % 100) / 100;
                        Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, pointerRegion.width * regSize * f * scl, pointerRegion.height * regSize * f * scl, angle - 90);
                    }
                }
                if (jammed || !Units.canCreate(team, getType())) {
                    Draw.color(getColor(getSet()));
                    float signSize = 0.75f + Mathf.absin(totalProgress + 8f, 8f, 0.15f);
                    for (int i = 0; i < 4; i++) {
                        Draw.rect(arrowRegion, x, y, arrowRegion.width * scl * signSize * scl, arrowRegion.height * scl * signSize * scl, 90 * i);
                    }
                }
                Drawn.circlePercent(x, y, size * tilesize / 1.5f, buildProgress / costTime(getSet(), true), 0);
            }
            Draw.reset();

            Drawf.light(tile, size * tilesize * 4 * warmup, team.color, 0.95f);
        }

        public UnitType getType() {
            UnitSet set = calls.get(spawnID);
            return set == null ? null : set.type;
        }

        @Override
        public void drawConfigure() {
            Color color = getColor(getSet());
            Drawf.dashCircle(x, y, range(), color);
            Draw.color(color);
            Lines.square(x, y, block.size * tilesize / 2f + 1.0f);

            Vec2 target = link();
            Draw.alpha(1f);
            Drawf.dashCircle(target.x, target.y, spawnRange, color);

            float angle = Tmp.v1.angle();

            Draw.color(Pal.gray);
            Drawn.posSquareLink(color, 1.5f, 3.5f, true, this, target);
            Draw.color();

            if (core() != null)
                Drawn.posSquareLinkArr(color, 1.5f, 3.5f, true, false, this, core());

            if (jammed) Drawn.overlayText(Core.bundle.get("spawn-error"), x, y, size * tilesize / 2.0F, color, true);

            Draw.reset();
        }

        public Color getColor(UnitSet set) {
            if (cooling) return Pal.lancerLaser;
            if (jammed || (set != null && !canSpawn(set, true)))
                return Tmp.c1.set(team.color).lerp(Pal.ammo, Mathf.absin(10f, 0.3f) + 0.1f);
            else return team.color;
        }

        public boolean canSpawn(UnitSet set, boolean buildingParma) {
            return team.data().countType(set.type) + (buildingParma ? buildingSpawnNum : spawnNum) <= Units.getCap(team);
        }

        @Override
        public void created() {
            super.created();
            UnitSet set;
            if (!cheating() && (set = calls.get(planSpawnID)) != null && hideSet(set.type))

                spawnID = 0;
        }

        public  boolean hideSet(UnitType type) {
            return state.rules.bannedUnits.contains(type) || type.locked() && !state.rules.infiniteResources && state.isCampaign();
        }

        @Override
        public IntSeq config() {
            return IntSeq.with(1, planSpawnID, planSpawnNum);
        }

        public ItemModule realItems() {
            return useCoreItems && team.data().hasCore() ?
                    team.core().items :
                    items;
        }

        public boolean isCalling() {
            UnitSet set = getSet();
            return buildProgress > 0f && set != null && Units.canCreate(team, set.type);
        }


        public void updateTile() {
            UnitSet set = getSet();
            totalProgress += (efficiency + warmup) * delta() * Mathf.curve(Time.delta, 0f, 0.5f);
            if (!cooling && isCalling()) {
                buildProgress += efficiency * state.rules.unitBuildSpeedMultiplier * delta() * warmup * state.rules.unitBuildSpeed(team);
                if (buildProgress >= costTime(getSet(), true) && !jammed) {
                    spawn(getSet());
                }
            }

            if (getSet() != null && planSpawnID != 0 && planSpawnNum != 0 && isCalling() && set != null) {
                float remainingTime = costTime(getSet(), true) - buildProgress;

                if (remainingTime <= 3f * 60f) {
                    fadeAlpha = Mathf.lerpDelta(fadeAlpha, 0, 0.08f);
                } else if (buildProgress >= costTime(getSet(), true) - 15f * 60f) {
                    fadeAlpha = Mathf.approachDelta(fadeAlpha, 1, 0.02f);
                }
            } else {
                fadeAlpha = Mathf.lerpDelta(fadeAlpha, 0, 0.05f);
            }

            if (cooling) {
                if (Mathf.chanceDelta(0.2f))
                    Fx.reactorsmoke.at(x + Mathf.range(tilesize * size / 2), y + Mathf.range(tilesize * size / 2));
                if (timer.get(0, 4)) for (int i = 0; i < 4; i++) {
                    Fx.shootSmallSmoke.at(x, y, i * 90);
                }

                cooldown += warmup * delta();
                if (cooldown > cooldownTime) {
                    cooling = false;
                    cooldown = 0;
                }
            }

            if (efficiency > 0 && power.status > 0.5f) {
                if (Mathf.equal(warmup, 1, 0.0015F)) warmup = 1f;
                else warmup = Mathf.lerpDelta(warmup, 1, 0.01f);
            } else {
                if (Mathf.equal(warmup, 0, 0.0015F)) warmup = 0f;
                else warmup = Mathf.lerpDelta(warmup, 0, 0.03f);
            }

            if (timer(1, 20) && calls.containsKey(planSpawnID) && planSpawnNum > 0
                    && power.status > 0.5f && hasConsume(calls.get(planSpawnID), planSpawnNum)) {

                if (!isCalling() && !cooling && set != null) {
                    float progressDelta = efficiency * state.rules.unitBuildSpeedMultiplier * delta() * warmup * state.rules.unitBuildSpeed(team);
                    buildProgress = Mathf.clamp(buildProgress + progressDelta, 0, costTime(set, true));
                }

                if (jammed) {
                    Tile t = null;
                    while (t == null) {
                        Tmp.v1.set(1, 1).rnd(range()).add(this).clamp(0, 0, world.unitWidth(), world.unitHeight());
                        t = world.tile(World.toTile(Tmp.v1.x), World.toTile(Tmp.v1.y));
                    }
                    link = t.pos();
                    spawn(getSet());
                }
            }
        }

        public void startBuild(int set, int spawnNum) {
            jammed = false;

            if (isCalling()) cooling = true;

            if (!calls.keys().toArray().contains(set)) {
                if (isCalling()) {
                    if (getSet() != null) {
                        Building target = team.data().hasCore() ? team.core() : self();

                        for (ItemStack stack : ItemStack.mult(getSet().dynamicRequirements(team), buildingSpawnNum * (set - buildProgress) / costTime(getSet(), true))) {
                            realItems().add(stack.item, Math.min(stack.amount, target.getMaximumAccepted(stack.item) - realItems().get(stack.item)));
                        }
                    }
                }

                spawnID = 0;
                buildProgress = 0;
            } else {
                spawnID = set;
                buildProgress = 1;
                buildingSpawnNum = spawnNum;
                if (!continuousProduction) {
                    consumeItems();
                }
            }
        }

        private void callBuild(int id, int num) {
            if (continuousProduction && !cooling && isValid()) {
                UnitSet set = calls.get(id);
                if (set != null && canSpawn(set, true) && hasConsume(set, num)) {
                    startBuild(id, num);
                }
            }
        }

        public void spawn(UnitSet set) {
            if (!isValid()) return;
            boolean success;

            Vec2 target = link();

            WHFx.spawn.at(x, y, regSize(set.type), team.color, this);

            success = WHUtils.spawnUnit(team, target.x, target.y, angleTo(target), spawnRange, spawnReloadTime, spawnDelay, set.type, buildingSpawnNum, set.airdrop, s -> {
                if (commandPos != null) s.commandPos.set(commandPos);
            });

            if (success) {
                if (!continuousProduction) {
                    spawnID = 0;
                }
                buildProgress = 0;
                buildingSpawnNum = spawnNum;
                jammed = false;
                cooling = true;
                if (continuousProduction && hasConsume(set, planSpawnNum)) {
                    callBuild(planSpawnID, planSpawnNum);
                    consumeItems();
                } else {
                    continuousProduction = false;
                }
            } else jammed = true;
        }

        public boolean hasConsume(UnitSet set, int amount) {
            if (set == null) return false;
            for (ItemStack stack : set.dynamicRequirements(team)) {
                if (realItems().get(stack.item) < stack.amount * amount) return false;
            }
            return true;
        }

        public void consumeItems() {
            if (!cheating()) {
                ItemStack[] stacks = getSet().dynamicRequirements(team);
                if (stacks != null) {
                    realItems().remove(ItemStack.mult(stacks, buildingSpawnNum));
                }
            }
        }

        public UnitSet getSet() {
            return calls.get(spawnID);
        }


        public float speedMultiplier(int spawnNum) {
            return Mathf.sqrt(spawnNum) * buildSpeedMultiplierCoefficient;
        }

        public float costTime(UnitSet set, boolean buildingParma) {

            return (buildingParma ? buildingSpawnNum : spawnNum) * set.costTime() / speedMultiplier(buildingParma ? buildingSpawnNum : spawnNum);
        }

        public Vec2 link() {
            return Pos != null ? Pos : linkVec.set(this);
        }

        public void buildConfiguration(Table table) {

            Seq<UnitSet> units = new Seq<>(calls.values().toArray());
            units.retainAll((UnitSet u) ->
                    u.type.unlockedNow() && !u.type.isBanned()
            );

            if (units.any()) {
                Table unitList = new Table();
                unitList.background(Tex.pane);
                unitList.defaults().growX().left().pad(5);

                for (Integer hashcode : getSortedKeys()) {
                    UnitSet set = calls.get(hashcode);
                    unitList.table(unit -> {
                        unit.left();
                        unit.image(set.type.uiIcon).size(40).scaling(Scaling.fit).padRight(10);
                        unit.table(info -> {
                            info.add("[accent]" + set.type.localizedName + "[]").left().row();
                            info.add("[gray]" + set.type.description).left().wrap().width(350f);
                            info.add(new Label(() ->
                                    "@[lightgray]buildtime: " +
                                            Strings.fixed(costTime(set, false) / 60f, 1) + "秒 ×" +
                                            spawnNum + "[]"
                            )).left();
                        }).growX();

                        Table buttonContainer = new Table();
                        Button continuous = unit.button(Icon.refresh, Styles.clearNonei, () -> {
                            configure(IntSeq.with(2, hashcode, spawnNum));
                        }).tooltip("@circular-production").update(b -> {
                            b.setChecked(continuousProduction && planSpawnID == hashcode);
                            b.setDisabled(!hasConsume(set, spawnNum));
                        }).width(40).get();

                        Button add = unit.button(Icon.add, Styles.clearNonei, () -> {
                            configure(IntSeq.with(1, hashcode, spawnNum));
                            startBuild(hashcode, spawnNum);
                        }).update(b ->
                                b.setDisabled(team.data().countType(set.type) + spawnNum > Units.getCap(team)
                                        || !hasConsume(set, spawnNum)
                                        || cooling)
                        ).tooltip("@produce").get();

                        buttonContainer.add(continuous).padLeft(10);
                        buttonContainer.add(add).padLeft(10);
                        unit.add(buttonContainer).right().padLeft(10);
                    }).row();

                    unitList.table(consumption -> {
                        consumption.background(Styles.black3);
                        consumption.defaults().pad(2).left();
                        for (ItemStack stack : set.dynamicRequirements(team)) {
                            consumption.add(new ItemImageDynamic(stack.item, () -> stack.amount * spawnNum, realItems())).left();
                        }
                        consumption.update(() -> {
                            if (planSpawnID == hashcode) {
                                if (planSpawnNum > 0) {
                                    if (hasConsume(set, planSpawnNum)) consumption.color.set(Pal.accent);
                                    else consumption.color.set(Pal.ammo);
                                } else consumption.color.set(Pal.lightishGray);
                            } else consumption.color.set(Pal.gray);
                        });
                    }).growX().padBottom(5).row();
                }

                ScrollPane scrollPane = new ScrollPane(unitList);
                scrollPane.setScrollingDisabled(true, false);
                table.add(scrollPane).maxHeight(400).growX().row();

                Table amountTable = new Table();
                amountTable.defaults().pad(5);
                amountTable.background(Tex.pane);
                amountTable.add("@produce-amount").color(Pal.accent);

                TextField amountField = new TextField(String.valueOf(spawnNum));
                amountField.setFilter((textField, c) ->
                        Character.isDigit(c) && textField.getText().length() < 2
                );

                Slider slider = new Slider(1, 10, 1, false);
                slider.setValue(spawnNum);
                slider.changed(() -> {
                    int newValue = (int) slider.getValue();
                    buildingSpawnNum = newValue;
                    spawnNum = newValue;
                    amountField.setText(String.valueOf(newValue));

                });
                amountField.changed(() -> {
                    if (!amountField.getText().isEmpty()) {
                        int newValue = Math.min(Integer.parseInt(amountField.getText()), 10);
                        spawnNum = newValue;
                        slider.setValue(newValue);
                    }
                });

                amountTable.add(amountField).width(60).padRight(10);
                amountTable.add(slider).width(200);
                table.add(amountTable).growX().padTop(10).row();

                Table buttons = new Table();
                buttons.defaults().growX().height(40).pad(2);
                buttons.button("@select-position", Icon.move, () -> {
                    UIUtils.selectPos(table, pos -> {
                        Vec2 worldPos = new Vec2(
                                pos.x * tilesize + tilesize / 2f,
                                pos.y * tilesize + tilesize / 2f
                        );

                        float dst = worldPos.dst(this);
                        if (dst > range()) {
                            worldPos.sub(this).setLength(range()).add(this);
                        }
                        configure(worldPos);
                    });
                });
                buttons.button("@cancel", Icon.cancel, () -> {
                    configure(IntSeq.with(3, 0, 0));
                }).update(b -> {
                    b.setDisabled(planSpawnID == 0);
                    b.getLabel().setColor(continuousProduction ? Color.white : Color.gray);
                }).tooltip("@reset").padRight(4);

                table.add(buttons).growX().left();
            } else {
                table.table(Styles.black3, t -> t.add("@none").color(Color.lightGray));
            }
        }

        public Seq<Integer> getSortedKeys() {
            Seq<UnitSet> keys = calls.values().toArray().sort();
            Seq<Integer> hashs = new Seq<>();
            for (UnitSet set : keys) {
                hashs.add(set.hashCode());
            }
            return hashs;
        }

        @Override
        public void write(Writes write) {
            write.s(0);

            write.i(spawnID);
            write.i(link);
            write.f(buildProgress);
            write.f(warmup);
            write.i(buildingSpawnNum);

            write.bool(cooling);
            write.f(cooldown);

            write.i(planSpawnID);
            write.i(planSpawnNum);
            write.bool(continuousProduction);
            write.i(spawnNum);

            TypeIO.writeVecNullable(write, commandPos);
        }

        @Override
        public void read(Reads read, byte revision) {
            short ver = read.s();

            spawnID = read.i();
            link = read.i();
            buildProgress = read.f();
            warmup = read.f();
            buildingSpawnNum = read.i();

            cooling = read.bool();
            cooldown = read.f();

            planSpawnID = read.i();
            planSpawnNum = read.i();

            continuousProduction = read.bool();
            spawnNum = read.i();

            commandPos = TypeIO.readVecNullable(read);
        }
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("progress",
                (UnitCallBlockBuild entity) -> new Bar(
                        () -> entity.isCalling() ?
                                Core.bundle.get("bar.progress") : "[lightgray]" + Iconc.cancel,
                        () -> entity.isCalling() && Units.canCreate(entity.team, entity.getType()) ? Pal.power : Pal.redderDust,
                        () -> entity.isCalling() ? entity.buildProgress / entity.costTime(entity.getSet(), true) : 0
                )
        );
        addBar("cooldown",
                (UnitCallBlockBuild entity) -> new Bar(
                        () -> Core.bundle.get("stat.cooldowntime"),
                        () -> Pal.lancerLaser,
                        () -> entity.cooling ? (cooldownTime - entity.cooldown) / cooldownTime : 0
                )
        );
    }

    @Override
    public void init() {
        super.init();

        if (calls.isEmpty()) throw new IllegalArgumentException("Seq @calls is [red]EMPTY[].");

        Seq<UnitSet> keys = calls.values().toArray();
        calls.clear();
        keys.sort();
        for (UnitSet set : keys) calls.put(set.hashCode(), set);
        config(UnitSet.class, (UnitCallBlockBuild build, UnitSet set) -> {
            if (set != null) {
                build.spawnID = Arrays.hashCode(set.sortIndex);
            }
        });
        config(Vec2.class, (UnitCallBlockBuild build, Vec2 pos) -> {
            build.Pos = pos.clamp(0f, 0f, world.unitWidth(), world.unitHeight());
        });

    }
}