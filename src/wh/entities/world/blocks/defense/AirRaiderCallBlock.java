package wh.entities.world.blocks.defense;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.io.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import wh.content.*;
import wh.core.*;
import wh.entities.world.blocks.effect.SelectForceProjector.*;
import wh.entities.world.unit.*;
import wh.gen.*;
import wh.graphics.*;
import wh.ui.*;
import wh.ui.display.*;

import static arc.Core.bundle;
import static mindustry.Vars.*;
import static mindustry.type.ItemStack.with;
import static wh.content.WHContent.*;
import static wh.core.WarHammerMod.name;
import static wh.ui.UIUtils.LEN;

public class AirRaiderCallBlock extends Block{
    public AttackMode defaultMode = null;
    public int maxMount = 8;
    public float range = 1000f;
    public float starfRange = 300f;
    public UnitType strafeUnit = WHUnitTypes.airRaiderS;
    public UnitType missileUnit = WHUnitTypes.airRaiderM;
    public UnitType bombUnit = WHUnitTypes.airRaiderB;
    public Seq<AttackModeUnitPlan> plans = new Seq<>();
    public StatusEntry statusEntry = new StatusEntry().set(StatusEffects.none, 0);

    public enum AttackMode{
        strafe, missile, bomb;

        public static AttackMode safeValueOf(int ordinal){
            if(ordinal < 0 || ordinal >= values().length){
                return null;
            }
            return values()[ordinal];
        }
    }

    public AirRaiderCallBlock(String name){
        super(name);
        size = 3;
        hasPower = true;
        hasItems = true;
        update = true;
        configurable = true;
        solid = true;
        itemCapacity = 50;


        config(Integer.class, (AirRaiderUnitBuild build, Integer planId) -> {
            if(planId >= 0 && planId < plans.size){
                build.setMode(plans.get(planId).mode);
            }
        });
        config(AttackMode.class, AirRaiderUnitBuild::setMode);

        consume(new ConsumeItemDynamic((AirRaiderUnitBuild e) -> {
            AttackModeUnitPlan currentPlan = plans.find(p -> p.mode == e.currentMode);
            return currentPlan != null ? currentPlan.requirements : ItemStack.empty;
        }));
    }

    @Override
    public void init(){
        super.init();
        intPlans();
    }

    public void intPlans(){
        TextureRegion strafeRegion = Core.atlas.find(name("strafe-mode"));
        TextureRegion missileRegion = Core.atlas.find(name("missile-mode"));
        TextureRegion bombRegion = Core.atlas.find(name("bomb-mode"));

        plans = Seq.with(
        new AttackModeUnitPlan(strafeUnit, Core.bundle.get("wh-strafe-mode"), strafeRegion, AttackMode.strafe, 600, with(Items.silicon, 10)),
        new AttackModeUnitPlan(missileUnit, Core.bundle.get("wh-missile-mode"), missileRegion, AttackMode.missile, 600, with(Items.silicon, 8)),
        new AttackModeUnitPlan(bombUnit,  Core.bundle.get("wh-bomb-mode"), bombRegion, AttackMode.bomb, 600, with(Items.silicon, 30))
        );
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("reload", (AirRaiderUnitBuild build) -> new Bar(
        () -> Core.bundle.get("bar.wh-reload"),
        () -> Pal.ammo,
        () -> {
            AttackModeUnitPlan currentPlan = plans.find(p -> p.mode == build.currentMode);
            if(currentPlan == null) return 0f;
            return Mathf.clamp(build.reload / currentPlan.time);
        }
        ));
        addBar("mode", (AirRaiderUnitBuild build) -> new Bar(
        () -> build.currentMode == null ? Core.bundle.get("none") : Core.bundle.get("bar.wh-mode") + plans.find(p -> p.mode == build.currentMode).name,
        () -> Pal.accent,
        () -> build.currentMode == null ? 0f : 1f
        ));

        addBar("team-builds", (AirRaiderUnitBuild entity) -> new Bar(
        () -> Core.bundle.get("bar.wh-amount")+ Groups.build.count(b -> b.team == entity.team && b.block == this)+" / " + maxMount,
        () -> Pal.accent,
        () -> (float)Groups.build.count(b -> b.team == entity.team && b.block == this) / maxMount
        ));
    }



        public boolean canPlaceOn(Tile tile, Team team, int rotation) {
            if (team != Team.derelict && Groups.build.count(b -> b.team == team && b.block == this) >= maxMount) {
                drawPlaceText("Maximum Placement Quantity Reached", tile.x, tile.y, false);
                return false;
            }
            return super.canPlaceOn(tile, team, rotation);
        };



    @Override
    public void setStats(){
        super.setStats();
        plans.each(plan -> {
            stats.add(Stat.ammo, StatValues.items(plan.requirements));
            if(plan.unit.weapons.any()){
                stats.add(Stat.unitType, StatValues.content(plan.unit));
                stats.add(Stat.weapons, StatValues.weapons(plan.unit, plan.unit.weapons));
            }
        });
    }

    public static class AttackModeUnitPlan{
        public UnitType unit;
        public ItemStack[] requirements;
        public float time;
        public AttackMode mode;
        public String name;
        public TextureRegion icon;


        public AttackModeUnitPlan(UnitType unit, String name, TextureRegion icon, AttackMode mode, float time, ItemStack[] requirements){
            this.unit = unit;
            this.requirements = requirements;
            this.time = time;
            this.mode = mode;
            this.name = name;
            this.icon = icon;
        }

        public AttackModeUnitPlan(){
        }
    }

    public class AirRaiderUnitBuild extends Building implements Ranged{
        public AttackMode currentMode = defaultMode;
        public boolean canSpawn;
        protected final Vec2 firstTarget = new Vec2().set(this);
        protected final Vec2 SecondTarget = new Vec2().set(this);
        protected final Vec2 extendPos = new Vec2();
        public float delayTime = 240f;
        public float delay;
        public float reload;
        public float warmup;
        public float totalProgress;
        public float warmupSpeed = 0.07f;
        public float warmupFallSpeed = 0.1f;

        public Seq<Unit> spawnedUnits = new Seq<>();
        public int savedUnitsCount = 0;
        protected IntSeq readUnits = new IntSeq();

        @Override
        public float range(){
            return range;
        }


        @Override
        public void add(){
            if(!added) WorldRegister.ARBuilds.add(this);

            super.add();
        }

        @Override
        public void remove(){
            if(added) WorldRegister.ARBuilds.remove(this);
            if(!spawnedUnits.isEmpty())
                for(var unit : spawnedUnits){
                    if(unit.controller() instanceof AirRaiderAI ai){
                        ai.hasReachedEnd = true;
                        for(var mount : unit.mounts){
                            mount.shoot = false;
                        }
                        if(unit instanceof AirRaiderUnitType u){
                            u.end = true;
                        }
                    }
                }
            super.remove();
        }

        @Override
        public void updateTile(){

            if(!readUnits.isEmpty()){
                spawnedUnits.clear();
                readUnits.each(i -> {
                    var unit = Groups.unit.getByID(i);
                    if(unit != null){
                        spawnedUnits.add(unit);
                        spawnedUnits.addUnique(unit);
                        selectUnitAI();
                    }
                });
                readUnits.clear();
            }

            if(efficiency > 0 || power.status >= 0.99f){
                warmup = Mathf.lerpDelta(warmup, 1, warmupSpeed);
                totalProgress += warmup * edelta();
            }else warmup = Mathf.lerpDelta(warmup, 0, warmupFallSpeed);

            if(currentMode != null && shouldCharge() && !canSpawn){
                reload += edelta() * warmup;
            }
            if(canSpawn){
                delay += Time.delta;
                if(delay >= delayTime){
                    SpawnRaiderUnit();
                    selectUnitAI();
                    delay = 0f;
                }
            }
            spawnedUnits.removeAll(u -> u.dead || !u.isAdded() || u.team() != team);
        }


        public void selectUnitAI(){
            for(int a = 0; a < spawnedUnits.size; a++){
                var unit1 = spawnedUnits.get(a);
                var ai = (AirRaiderAI)unit1.controller();
                if(currentMode == AttackMode.strafe){
                    ai.setStrafingPath(firstTarget, SecondTarget, new Vec2(x, y));
                }else if(currentMode == AttackMode.bomb){
                    ai.setBombTarget(firstTarget, extendPos);
                }else if(currentMode == AttackMode.missile){
                    ai.setMissileTarget(firstTarget, extendPos);
                }
            }
        }

        public boolean shouldCharge(){
            AttackModeUnitPlan currentPlan = plans.find(p -> p.mode == currentMode);
            return reload < currentPlan.time;
        }

        public boolean isCharging(){
            AttackModeUnitPlan currentPlan = plans.find(p -> p.mode == currentMode);
            return reload >= currentPlan.time;
        }

        public boolean Chargeing(){
            AttackModeUnitPlan currentPlan = plans.find(p -> p.mode == currentMode);
            if(currentPlan == null) return false;
            return reload >= currentPlan.time;
        }


        public void setMode(AttackMode mode){
            currentMode = mode;
            SecondTarget.set(this);
        }

        public void selectOnePosition(Table table){
            UIUtils.selectPos(table, pos -> {
                firstTarget.set(convertToWorldPos(pos));
            });
        }

        public void selectTwoPosition(Table table){
            UIUtils.selectTwoPos(table, (point1, point2) -> {
                Lines.line(point1.x * tilesize, point1.y * tilesize,
                point2.x * tilesize, point2.y * tilesize);
                firstTarget.set(convertToWorldPos(point1));
                SecondTarget.set(convertToWorldPos2(point2));
            });
        }

        public Vec2 convertToWorldPos(Point2 pos){
            Vec2 worldPos = new Vec2(
            pos.x * tilesize + tilesize / 2f,
            pos.y * tilesize + tilesize / 2f
            );

            float dst = worldPos.dst(this);
            if(dst > range()){
                worldPos.sub(this).setLength(range()).add(this);
            }
            configure(worldPos);
            return worldPos;
        }

        public Vec2 convertToWorldPos2(Point2 pos){
            Vec2 worldPos = new Vec2(
            pos.x * tilesize + tilesize / 2f,
            pos.y * tilesize + tilesize / 2f
            );
            if(firstTarget != null){
                float dst = worldPos.dst(firstTarget);
                if(dst > starfRange){
                    worldPos.sub(firstTarget).setLength(starfRange).add(firstTarget);
                }
            }
            configure(worldPos);
            return worldPos;
        }

        private void clampPosition(Vec2 position, Vec2 center, float maxDistance){
            if(position.dst(center) > maxDistance){
                position.sub(center).setLength(maxDistance).add(center);
            }
        }

        @Override
        public void buildConfiguration(Table table){
            control.input.selectedBlock();
            DrawPlan(table);
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            AttackModeUnitPlan currentPlan = plans.find(p -> p.mode == currentMode);
            return currentPlan != null && items.get(item) < getMaximumAccepted(item) &&
            Structs.contains(currentPlan.requirements, stack -> stack.item == item);
        }

        public void DrawPlan(Table table){
            table.table(t -> {
                t.defaults().fill();

                for(AttackModeUnitPlan plan : plans){
                    addModeRow(t, plan.name, plan.icon, plan);
                    t.row();
                }
                t.button(Icon.modeAttack, Styles.defaulti, () -> {
                    AttackModeUnitPlan currentPlan = plans.find(p -> p.mode == currentMode);
                    if(currentPlan != null){
                        boolean canConsume = true;
                        for(ItemStack stack : currentPlan.requirements){
                            if(items.get(stack.item) < stack.amount){
                                canConsume = false;
                                break;
                            }
                        }
                        if(canConsume && isCharging()){
                            consume();
                            canSpawn = true;
                            reload = Math.max(0, reload - plans.find(p -> p.mode == currentMode).time);
                            commandAll(firstTarget, SecondTarget);
                        }
                    }
                }).size(LEN).disabled(b -> {
                    AttackModeUnitPlan currentPlan = plans.find(p -> p.mode == currentMode);
                    if(currentPlan == null){
                        return true;
                    }
                    boolean delayCheck = canSpawn && delay <= delayTime;
                    for(ItemStack stack : currentPlan.requirements){
                        if(items.get(stack.item) < stack.amount){
                            return true;
                        }
                    }
                    boolean sufficient = Seq.with(currentPlan.requirements).allMatch(stack -> items.get(stack.item) >= stack.amount);

                    if(currentMode == AttackMode.strafe){
                        boolean targetsSet = firstTarget.dst(this) >= 1f && SecondTarget.dst(this) >= 1f;
                        return !isCharging() || !sufficient || !targetsSet || delayCheck;
                    }else{
                        boolean targetSet = firstTarget.dst(this) >= 1f;
                        return !isCharging() || !sufficient || !targetSet || delayCheck;
                    }
                });
            });

        }

        private void addModeRow(Table parent, String name, TextureRegion icon, AttackModeUnitPlan plan){
            parent.table(t -> {
                t.background(Styles.black6);
                TextureRegionDrawable iconD = new TextureRegionDrawable(icon){
                    @Override
                    public void draw(float x, float y, float width, float height){
                        Draw.color(Tmp.c1.set(team.color).mul(Draw.getColor()).toFloatBits());
                        Draw.rect(region, x + width / 2f, y + height / 2f, width, height);
                    }
                };
                t.table(left -> {
                    left.button(name, iconD, LEN, () -> {
                        setMode(plan.mode);
                        if(plan.mode == AttackMode.strafe){
                            selectTwoPosition(parent);
                        }else{
                            selectOnePosition(parent);
                        }
                    }).size(LEN * 4, LEN).left().disabled(b -> {
                        if(currentMode == null) return false;
                        return !isCharging();
                    });
                });
                t.table(req -> {
                    req.defaults().pad(2).left();
                    for(ItemStack stack : plan.requirements){
                        req.add(new ItemImageDynamic(stack.item, () -> stack.amount)).left();
                    }
                }).size(LEN * 1.5f, LEN);

            }).fill();
        }


        @Override
        public void drawConfigure(){
            super.drawConfigure();
            Seq<AirRaiderUnitBuild> builds = new Seq<>();
            for(AirRaiderUnitBuild build : WorldRegister.ARBuilds){
                if(build != this && build != null && build.team == team && build.canCommand(firstTarget)){
                    if(build.firstTarget.dst(this) >= 1f && build.SecondTarget.dst(this) >= 1f){
                        if(currentMode != null) build.currentMode = currentMode;
                        builds.add(build);
                        Drawn.posSquareLink(Pal.gray, 3, 4, false, build.x, build.y, firstTarget.x, firstTarget.y);

                        Draw.color(Pal.accent);
                        Lines.stroke(2);
                        Lines.square(build.x, build.y, build.block.size * tilesize / 3f, 45 + Time.time % 360);
                    }
                }
            }

            for(AirRaiderUnitBuild build : builds){
                if(build != this){
                    Drawn.posSquareLink(Pal.heal, 1, 2, false, build.x, build.y, firstTarget.x, firstTarget.y);
                    if(currentMode == AttackMode.strafe){
                        Drawn.posSquareLink(Pal.heal, 1, 2, true, firstTarget.x, firstTarget.y, SecondTarget.x, SecondTarget.y);
                    }
                }
            }

            Drawf.dashCircle(x, y, range, team.color);
            Drawf.dashCircle(firstTarget.x, firstTarget.y, starfRange, Pal.accent);
            Drawn.posSquareLink(Pal.accent, 1, 2, true, x, y, firstTarget.x, firstTarget.y);
            Drawn.drawConnected(firstTarget.x, firstTarget.y, 10f, Pal.accent);

            if(currentMode == AttackMode.strafe){
                Drawn.posSquareLink(Pal.accent, 1, 2, true, firstTarget.x, firstTarget.y, SecondTarget.x, SecondTarget.y);
                Drawn.drawConnected(SecondTarget.x, SecondTarget.y, 10f, Pal.accent);
            }


            if(canCommand(firstTarget)) builds.add(this);

            if(builds.any())
                Drawn.overlayText(bundle.format("wh-participants", builds.size),
                firstTarget.x, firstTarget.y, tilesize * 2f, Pal.accent, true);

        }

        public boolean canCommand(Vec2 target){
            return Chargeing() && warmup > 0.25f && within(target, range());
        }

        public void command(AirRaiderUnitBuild build, Vec2 pos1, Vec2 pos2){
            Rand random = new Rand();
            float rand = random.random(-15, 15) * tilesize;
            Vec2 tmpFirst = new Vec2(pos1).add(rand, rand);
            Vec2 tmpSecond = new Vec2(pos2).add(rand, rand);
            build.currentMode = this.currentMode;
            build.firstTarget.set(tmpFirst);
            build.SecondTarget.set(tmpSecond);
            build.canSpawn = true;
            build.reload = Math.max(0, build.reload - plans.find(p -> p.mode == build.currentMode).time);

            WHFx.attackWarningPos.at(pos1.x, pos1.y, delayTime, team.color, tile);

        }


        public void commandAll(Vec2 pos1, Vec2 pos2){

            for(AirRaiderUnitBuild build : WorldRegister.ARBuilds){
                if(build.team == team && build.canCommand(pos1) && within(this, this.range())){
                    build.command(build, pos1, pos2);
                    lastAccessed(Iconc.modeAttack + "");
                }
            }
            if(!headless){
                AttackModeUnitPlan currentPlan = plans.find(p -> p.mode == currentMode);
                if(currentPlan != null){
                    WHFx.warningRange(currentPlan.icon, delayTime * 1.5f, 80)
                    .at(firstTarget.x, firstTarget.y, 0, team.color);
                }
                /*WHFx.attackWarningRange.at(firstTarget.x, firstTarget.y, 80, team.color);*/
                WHFx.attackWarningPos.at(firstTarget.x, firstTarget.y, delayTime, team.color, tile);
            }
        }

        public void lastAccessed(String lastAccessed){
            this.lastAccessed = lastAccessed;
        }

        public void SpawnRaiderUnit(){

            if(currentMode == null) return;
            Unit raider = plans.find(p -> p.mode == currentMode).unit.spawn(team, x, y);

            raider.set(randomSpawnPos(raider));
            raider.rotation = randomSpawnAngle(raider);

            calculateEndPos(raider);
            if(!Vars.net.client()) raider.add();
            raider.apply(StatusEffects.unmoving, Fx.unitSpawn.lifetime);
            raider.apply(statusEntry.effect, statusEntry.time);
            spawnedUnits.add(raider);

            canSpawn = false;
        }

        public Vec2 randomSpawnPos(Unit unit){
            Rand random = new Rand();
            Vec2 pos = new Vec2();
            random.setSeed((long)(unit.id + 9999 + x + y));
            pos.set(currentMode == AttackMode.strafe ? firstTarget : this);
            float spawnAngle = randomSpawnAngle(unit);
            float distance = AttackMode.strafe == currentMode ? random.random(30, 40) : random.random(60, 90);
            Tmp.v1.trns(spawnAngle, -distance * tilesize);
            pos.add(Tmp.v1);
            return pos;
        }

        public void endPos(Vec2 pos){
            Rand random = new Rand();
            random.setSeed((long)(id + 9999 + x + y));
            float distance = AttackMode.strafe == currentMode ? random.random(30, 40) : random.random(60, 90);
            Tmp.v1.trns(firstTarget.angleTo(this) + random.random(-45, 45), -distance * tilesize);
            pos.add(Tmp.v1);
            Vec2 endPos = new Vec2();
            Tmp.v2.add(5 * distance * tilesize, 5 * distance * tilesize);
            endPos.set(pos).add(Tmp.v2);
            extendPos.set(endPos);
        }

        public void calculateEndPos(Unit unit){
            endPos(randomSpawnPos(unit));
        }

        public float randomSpawnAngle(Unit unit){
            Rand random = new Rand();
            random.setSeed((long)(unit.id + 9999 + x + y));
            if(currentMode == AttackMode.strafe){
                return firstTarget.angleTo(SecondTarget) + random.random(-45, 45);
            }else return firstTarget.angleTo(this) + 180 + random.random(-45, 45);
        }


        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void read(Reads read, byte v){
            super.read(read, v);

            canSpawn = read.bool();
            delay = read.f();
            reload = read.f();
            warmup = read.f();
            totalProgress = read.f();

            int count = read.b();
            savedUnitsCount = read.i();

            readUnits.clear();
            for(int i = 0; i < count; i++){
                readUnits.add(read.i());
            }

            TypeIO.readVec2(read, firstTarget);
            TypeIO.readVec2(read, SecondTarget);
            TypeIO.readVec2(read, extendPos);

            byte modeOrdinal = read.b();
            currentMode = AttackMode.safeValueOf(modeOrdinal);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(canSpawn);
            write.f(delay);
            write.f(reload);
            write.f(warmup);
            write.f(totalProgress);
            write.b(spawnedUnits.size);
            write.i(spawnedUnits.size);
            for(var unit : spawnedUnits){
                write.i(unit.id);
            }

            TypeIO.writeVec2(write, firstTarget);
            TypeIO.writeVec2(write, SecondTarget);
            TypeIO.writeVec2(write, extendPos);

            write.b(currentMode != null ? (byte)currentMode.ordinal() : (byte)-1);
        }

    }
}