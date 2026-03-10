package wh.entities.world.blocks.unit;

import arc.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.ctype.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.units.*;

public class ConfigurableUnitAssembler extends UnitAssembler{
    public ConfigurableUnitAssembler(String name){
        super(name);
        configurable = true;
        clearOnDoubleTap = true;
        saveConfig = true;
        logicConfigurable = true;

        config(Integer.class, (ConfigurableUnitAssemblerBuild build, Integer plan) -> {
            if(!configurable) return;

            int next = build.validatePlan(plan);
            if(build.currentPlan == next) return;
            build.currentPlan = next;
            build.progress = 0f;
        });

        config(UnitType.class, (ConfigurableUnitAssemblerBuild build, UnitType unit) -> {
            if(!configurable) return;

            int next = plans.indexOf(p -> p.unit == unit && !p.unit.isBanned());
            if(next == -1) next = build.defaultPlan();
            if(build.currentPlan == next) return;
            build.currentPlan = next;
            build.progress = 0f;
        });
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

        removeBar("units");
        addBar("units", (ConfigurableUnitAssemblerBuild e) ->
        new Bar(
        () -> e.unit() == null ? "[lightgray]" + Iconc.cancel :
        Core.bundle.format("bar.unitcap",
        Fonts.getUnicodeStr(e.unit().name),
        e.team.data().countType(e.unit()),
        e.unit().useUnitCap ? Units.getStringCap(e.team) : "∞"
        ),
        () -> Pal.power,
        () -> e.unit() == null ? 0f : (e.unit().useUnitCap ? ((float)e.team.data().countType(e.unit()) / Units.getCap(e.team)) : 1f)
        ));
    }

    public class ConfigurableUnitAssemblerBuild extends UnitAssemblerBuild{
        public int currentPlan = -1;

        public int defaultPlan(){
            int next = plans.indexOf(p -> p.unit.unlockedNow() && !p.unit.isBanned());
            if(next == -1) next = plans.indexOf(p -> !p.unit.isBanned());
            if(next == -1 && !plans.isEmpty()) next = 0;
            return next;
        }

        public int validatePlan(int plan){
            if(plans.isEmpty()) return -1;
            if(plan < 0 || plan >= plans.size || plans.get(plan).unit.isBanned()){
                return defaultPlan();
            }
            return plan;
        }

        public void ensurePlan(){
            currentPlan = validatePlan(currentPlan);
        }

        @Override
        public void created(){
            super.created();
            ensurePlan();
        }

        @Override
        public AssemblerUnitPlan plan(){
            ensurePlan();
            if(plans.isEmpty()) throw new IllegalStateException(block.name + " has no assembly plans.");
            return plans.get(currentPlan);
        }

        @Override
        public UnitType unit(){
            return plans.isEmpty() ? null : plan().unit;
        }

        @Override
        public void updateTile(){
            ensurePlan();
            // Module tiers are ignored; selected plan decides output.
            currentTier = 0;
            super.updateTile();
        }

        @Override
        public Object senseObject(LAccess sensor){
            if(sensor == LAccess.config){
                return currentPlan == -1 ? null : plans.get(currentPlan).unit;
            }
            return super.senseObject(sensor);
        }

        @Override
        public Object config(){
            return currentPlan;
        }

        @Override
        public void buildConfiguration(Table table){
            Seq<UnitType> units = Seq.with(plans).map(p -> p.unit).retainAll(u -> u.unlockedNow() && !u.isBanned());

            if(units.any()){
                ItemSelection.buildTable(ConfigurableUnitAssembler.this, table, units,
                () -> currentPlan == -1 ? null : plans.get(currentPlan).unit,
                unit -> configure(plans.indexOf(p -> p.unit == unit)),
                selectionRows, selectionColumns);
            }else{
                table.table(Styles.black3, t -> t.add("@none").color(Color.lightGray));
            }
        }

        @Override
        public byte version(){
            return 2;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.s(currentPlan);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(revision >= 2){
                currentPlan = read.s();
            }
            ensurePlan();
        }
    }
}
