package wh.entities.world.blocks.unit;

import arc.*;
import arc.Graphics.*;
import arc.Graphics.Cursor.*;
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
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.production.GenericCrafter.*;
import mindustry.world.blocks.units.*;
import mindustry.world.blocks.units.UnitFactory.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import wh.graphics.*;

import static mindustry.Vars.*;
import static wh.content.WHFx.rand;

public class MultipleConsumerReconstructor extends UnitBlock{
    public ObjectMap<UnitType[], ItemStack[]> upgradeCosts = new ObjectMap<>();
    public float constructTime = 60 * 2;
    public Seq<UnitType[]> upgrades = new Seq<>();
    public int[] capacities = {};

    public MultipleConsumerReconstructor(String name){
        super(name);
        regionRotated1 = 1;
        regionRotated2 = 2;
        commandable = true;
        ambientSound = Sounds.respawning;
        configurable = true;
        config(UnitCommand.class, (MultipleConsumerReconstructorBuild build, UnitCommand command) -> build.command = command);
        configClear((MultipleConsumerReconstructorBuild build) -> build.command = null);

    }

    public void addUpgrade(UnitType from, UnitType to, ItemStack... costs){
        upgrades.add(new UnitType[]{from, to});
        upgradeCosts.put(new UnitType[]{from, to}, costs);

        consume(new ConsumeItemDynamic((MultipleConsumerReconstructorBuild e) -> {
            if(e.payload != null && e.payload.unit.type == from){
                return costs;
            }
            return ItemStack.empty;
        }));
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(inRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.rect(outRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.rect(topRegion, plan.drawx(), plan.drawy());
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, inRegion, outRegion, topRegion};
    }


    @Override
    public void setBars(){
        super.setBars();

        addBar("progress", (MultipleConsumerReconstructorBuild entity) -> new Bar("bar.progress", Pal.ammo, entity::fraction));
        addBar("units", (MultipleConsumerReconstructorBuild e) ->
        new Bar(
        () -> e.unit() == null ? "[lightgray]" + Iconc.cancel :
        Core.bundle.format("bar.unitcap",
        Fonts.getUnicodeStr(e.unit().name),
        e.team.data().countType(e.unit()),
        e.unit() == null || e.unit().useUnitCap ? Units.getStringCap(e.team) : "∞"
        ),
        () -> Pal.power,
        () -> e.unit() == null ? 0f : (e.unit().useUnitCap ? (float)e.team.data().countType(e.unit()) / Units.getCap(e.team) : 1f)
        ));
    }


    @Override
    public void setStats(){
        stats.timePeriod = constructTime;
        super.setStats();

        stats.add(Stat.productionTime, constructTime / 60f, StatUnit.seconds);
        stats.add(Stat.output, table -> {
            table.row();
            for(var upgrade : upgradeCosts.keys()){
                if(upgrade[0].unlockedNow() && upgrade[1].unlockedNow()){
                    table.table(Styles.grayPanel, t -> {
                        t.left();

                        t.image(upgrade[0].uiIcon).size(40).pad(10f).left().scaling(Scaling.fit).with(i -> StatValues.withTooltip(i, upgrade[0]));
                        t.table(info -> {
                            info.add(upgrade[0].localizedName).left();
                            info.row();
                        }).pad(10).left();
                    }).fill().padTop(5).padBottom(5);

                    table.table(Styles.grayPanel, t -> {

                        t.image(Icon.right).color(Pal.darkishGray).size(40).pad(10f);
                    }).fill().padTop(5).padBottom(5);

                    table.table(Styles.grayPanel, t -> {
                        t.left();

                        t.image(upgrade[1].uiIcon).size(40).pad(10f).right().scaling(Scaling.fit).with(i -> StatValues.withTooltip(i, upgrade[1]));
                        t.table(info -> {
                            info.add(upgrade[1].localizedName).right();
                            info.row();
                        }).pad(10).right();
                    }).fill().padTop(5).padBottom(5);

                    table.table(Styles.grayPanel, t -> {
                        t.table(req -> {
                            req.right();
                            for(int i = 0; i < upgradeCosts.get(upgrade).length; i++){
                                if(i % 6 == 0){
                                    req.row();
                                }

                                ItemStack stack = upgradeCosts.get(upgrade)[i];
                                req.add(StatValues.displayItem(stack.item, stack.amount, constructTime, true)).pad(5);
                            }
                        }).right().grow().pad(10f);
                    });
                    table.row();
                }
            }
        });
    }

    @Override
    public void init(){
        capacities = new int[Vars.content.items().size];
        for(ObjectMap.Entry<UnitType[], ItemStack[]> plan : upgradeCosts.entries()){
            for(ItemStack stack : plan.value){
                capacities[stack.item.id] = Math.max(capacities[stack.item.id], stack.amount * 2);
                itemCapacity = Math.max(itemCapacity, stack.amount * 2);
            }
        }
        consumeBuilder.each(c -> c.multiplier = b -> state.rules.unitCost(b.team));
        super.init();
    }

    public class MultipleConsumerReconstructorBuild extends UnitBuild{
        public @Nullable Vec2 commandPos;
        public @Nullable UnitCommand command;

        boolean constructing;

        public float alpha;

        public float fraction(){
            return progress / constructTime;
        }

        @Override
        public Vec2 getCommandPosition(){
            return commandPos;
        }

        @Override
        public void onCommand(Vec2 target){
            commandPos = target;
        }

        @Override
        public boolean acceptUnitPayload(Unit unit){
            return hasUpgrade(unit.type) && !upgrade(unit.type).isBanned();
        }

        public boolean canSetCommand(){
            var output = unit();
            return output != null && output.commands.size > 1 && output.allowChangeCommands;
        }

        @Override
        public Cursor getCursor(){
            return canSetCommand() ? super.getCursor() : SystemCursor.arrow;
        }

        @Override
        public boolean shouldShowConfigure(Player player){
            return canSetCommand();
        }

        @Override
        public void buildConfiguration(Table table){
            var unit = unit();

            if(unit == null){
                deselect();
                return;
            }

            var group = new ButtonGroup<ImageButton>();
            group.setMinCheckCount(0);
            int i = 0, columns = 4;

            table.background(Styles.black6);

            var list = unit().commands;
            for(var item : list){
                ImageButton button = table.button(item.getIcon(), Styles.clearNoneTogglei, 40f, () -> {
                    configure(item);
                    deselect();
                }).tooltip(item.localized()).group(group).get();

                button.update(() -> button.setChecked(command == item || (command == null && unit.defaultCommand == item)));

                if(++i % columns == 0){
                    table.row();
                }
            }
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            if(!(this.payload == null
            && (this.enabled || source == this)
            && relativeTo(source) != rotation
            && payload instanceof UnitPayload pay)){
                return false;
            }

            var upgrade = upgrade(pay.unit.type);

            if(upgrade != null){
                if(!upgrade.unlockedNowHost() && !team.isAI()){
                    //flash "not researched"
                    pay.showOverlay(Icon.tree);
                }

                if(upgrade.isBanned()){
                    //flash an X, meaning 'banned'
                    pay.showOverlay(Icon.cancel);
                }
            }

            return upgrade != null && (team.isAI() || upgrade.unlockedNowHost()) && !upgrade.isBanned();
        }

        @Override
        public int getMaximumAccepted(Item item){
            return Mathf.round(capacities[item.id] * state.rules.unitCost(team));
        }

        @Override
        public void overwrote(Seq<Building> builds){
            if(builds.first().block == block){
                items.add(builds.first().items);
            }
        }

        @Override
        public void draw(){
            Draw.rect(region, x, y);

            //draw input
            boolean fallback = true;
            for(int i = 0; i < 4; i++){
                if(blends(i) && i != rotation){
                    Draw.rect(inRegion, x, y, (i * 90) - 180);
                    fallback = false;
                }
            }
            if(fallback) Draw.rect(inRegion, x, y, rotation * 90);

            Draw.rect(outRegion, x, y, rotdeg());


            if(constructing() && hasArrived()){
                Draw.draw(Layer.blockOver, () -> {
                    Draw.alpha(1f - progress / constructTime);
                    Draw.rect(payload.unit.type.fullIcon, x, y, payload.rotation() - 90);
                    Draw.reset();
                    Drawf.construct(this, upgrade(payload.unit.type), payload.rotation() - 90f, progress / constructTime, speedScl, time);

                });
            }else{
                Draw.z(Layer.blockOver);

                drawPayload();
            }

            Draw.z(Layer.blockOver + 0.1f);
            Draw.rect(topRegion, x, y);
            if(constructing() && hasArrived()){
                float move = block.size * 0.83f / 2 * tilesize;

                Draw.color(Pal.accent);
                for(float mx : new float[]{move, -move}){
                    for(float my : new float[]{move, -move}){
                        Draw.z(Layer.buildBeam);
                        Draw.alpha((Mathf.sin(Time.time, 6, 0.1f)+0.8f) * alpha);
                        float bx = x + mx, by = y + my;
                        Drawf.buildBeam(bx, by, x, y, block.size * 0.3f / 2 * tilesize);
                    }
                }
                Fill.square(x, y, block.size * 0.3f / 2 * tilesize);
                for(float mx : new float[]{move, -move}){
                    for(float my : new float[]{move, -move}){
                        //i++;
                        Draw.alpha(alpha);
                        Draw.color(Pal.accent.cpy().lerp(Items.surgeAlloy.color, 0.3f));
                        Draw.z(Layer.buildBeam + 1f);
                        float x1 = x + mx, y1 = y + my;
                        Fill.circle(x1, y1, block.size * 0.1f / 2 * tilesize);
                        Drawn.drawBeam(this, 114514, x1, y1, Angles.angle(x1, y1, x, y), dst(x1, y1) * 2,
                        efficiency, 1f, new Vec2(), new Vec2(),
                        Pal.accent.cpy().lerp(Items.surgeAlloy.color, 0.3f));
                    }
                }
            }
        }

        @Override
        public Object senseObject(LAccess sensor){
            if(sensor == LAccess.config) return unit();
            return super.senseObject(sensor);
        }

        @Override
        public void updateTile(){
            //cache value to prevent repeated calls and multithreading issues
            constructing = constructing();
            boolean valid = false;

            if(payload != null){
                //check if offloading
                if(!hasUpgrade(payload.unit.type)){
                    moveOutPayload();
                }else{ //update progress
                    if(moveInPayload()){
                        if(efficiency > 0){
                            valid = true;
                            progress += edelta() * state.rules.unitBuildSpeed(team);
                            alpha = Mathf.lerpDelta(alpha, 1f, 0.08f);
                        }else {
                            alpha = Mathf.lerpDelta(alpha, 0f, 0.1f);
                        }

                        //upgrade the unit
                        if(progress >= constructTime){
                            payload.unit = upgrade(payload.unit.type).create(payload.unit.team());

                            if(payload.unit.isCommandable()){
                                if(commandPos != null){
                                    payload.unit.command().commandPosition(commandPos);
                                }
                                //this already checks if it is a valid command for the unit type
                                payload.unit.command().command(command == null && payload.unit.type.defaultCommand != null ? payload.unit.type.defaultCommand : command);
                            }

                            progress %= 1f;

                            Effect.shake(2f, 3f, this);
                            Fx.producesmoke.at(this);
                            consume();
                            Events.fire(new UnitCreateEvent(payload.unit, this));
                        }
                    }
                }
            }
            speedScl = Mathf.lerpDelta(speedScl, Mathf.num(valid), 0.02f);
            time += edelta() * speedScl * state.rules.unitBuildSpeed(team);
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.progress) return Mathf.clamp(fraction());
            if(sensor == LAccess.itemCapacity) return Mathf.round(itemCapacity * state.rules.unitCost(team));
            return super.sense(sensor);
        }

        @Override
        public boolean shouldConsume(){
            return constructing && enabled;
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if(payload != null && payload.unit != null){
                UnitType currentUnit = payload.unit.type;
                for(ObjectMap.Entry<UnitType[], ItemStack[]> plan : upgradeCosts.entries()){
                    if(plan.key[0] == currentUnit){
                        if(Structs.contains(plan.value, stack -> stack.item == item)){
                            return constructing && items.get(item) < getMaximumAccepted(item);
                        }
                    }
                }
            }
            return super.acceptItem(source, item);
        }

        ;

        @Override
        public Object config(){
            return command;
        }

        public UnitType unit(){
            if(payload == null) return null;

            UnitType t = upgrade(payload.unit.type);
            return t != null && (t.unlockedNowHost() || team.isAI()) ? t : null;
        }

        public boolean constructing(){
            return payload != null && hasUpgrade(payload.unit.type);
        }

        public boolean hasUpgrade(UnitType type){
            UnitType t = upgrade(type);
            return t != null && (t.unlockedNowHost() || team.isAI()) && !type.isBanned();
        }

        public UnitType upgrade(UnitType type){
            UnitType[] r = upgrades.find(u -> u[0] == type);
            return r == null ? null : r[1];
        }

        @Override
        public byte version(){
            return 3;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(progress);
            TypeIO.writeVecNullable(write, commandPos);
            TypeIO.writeCommand(write, command);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 1){
                progress = read.f();
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