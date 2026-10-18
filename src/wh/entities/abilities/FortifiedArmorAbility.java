package wh.entities.abilities;

import arc.Core;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Interval;
import arc.util.Strings;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import wh.content.WHStats;

import static wh.core.WarHammerMod.name;

public class FortifiedArmorAbility extends Ability{
    public float armorMultiplier = 2f;
    public float armorActivationHealth = 0.7f;
    public float restoreTrigger = 0.2f;
    public float restoreHealth = 0.99f;
    public float interval = 15f;

    public Effect armorBreakEffect = Fx.unitShieldBreak;
    public Effect armorRestoreEffect = Fx.shieldApply;

    protected transient Interval timer = new Interval();
    protected transient boolean initialized;
    protected transient boolean armored;
    protected transient boolean canRestore;
    protected transient float baseArmor;

    public FortifiedArmorAbility(){
    }

    public FortifiedArmorAbility(float armorMultiplier) {
        this.armorMultiplier = armorMultiplier;
    }

    @Override
    public void created(Unit unit){
        initialize(unit);
        applyArmor(unit);
    }

    @Override
    public void update(Unit unit){
        if(!initialized){
            initialize(unit);
        }

        if(timer.get(0, interval)){
            updateState(unit);
        }

        applyArmor(unit);
    }

    @Override
    public void displayBars(Unit unit, Table bars){
        if(!armored && canRestore){
            bars.add(new Bar(
            () -> Core.bundle.format("bar.wh-fortified-armor-recover", Mathf.round(restoreFraction(unit) * 100f)),
            () -> Pal.heal,
            () -> restoreFraction(unit)
            )).row();
        }
    }

    protected void initialize(Unit unit) {
        baseArmor = unit.armorOverride >= 0f ? unit.armorOverride : unit.type.armor;
        armored = unit.healthf() > armorActivationHealth;
        canRestore = false;
        initialized = true;
    }

    protected void updateState(Unit unit){
        boolean wasArmored = armored;

        if (armored && unit.healthf() <= armorActivationHealth) {
            setArmored(unit, false);
        }

        if(unit.healthf() <= restoreTrigger){
            canRestore = true;
        }

        if(!armored && canRestore && unit.healthf() >= restoreHealth){
            setArmored(unit, true);
            canRestore = false;
        }

        if(wasArmored && !armored && armorBreakEffect != Fx.none){
            armorBreakEffect.at(unit.x, unit.y, unit.hitSize, unit.team.color, unit);
        }

        if(!wasArmored && armored && armorRestoreEffect != Fx.none){
            armorRestoreEffect.at(unit.x, unit.y, unit.hitSize, unit.team.color, unit);
        }
    }

    protected void applyArmor(Unit unit){
        unit.armorOverride = armored ? baseArmor * armorMultiplier : baseArmor;
    }

    protected float restoreFraction(Unit unit){
        if(restoreHealth <= 0f){
            return 1f;
        }
        return Mathf.clamp(unit.healthf() / restoreHealth);
    }

    protected void setArmored(Unit unit, boolean value){
        if(armored == value){
            applyArmor(unit);
            return;
        }

        armored = value;
        applyArmor(unit);
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.row();
        t.add(WHStats.format("wh-armor-multiplier", Strings.autoFixed(armorMultiplier, 2)));
        t.row();
        t.add(WHStats.format("wh-restore-health-threshold", Strings.autoFixed(restoreTrigger * 100f, 2) + "%"));
    }

    @Override
    public FortifiedArmorAbility copy(){
        FortifiedArmorAbility out = (FortifiedArmorAbility)super.copy();
        out.timer = new Interval();
        out.initialized = false;
        out.armored = false;
        out.canRestore = false;
        out.baseArmor = 0f;
        return out;
    }

    @Override
    public String localized(){
        return Core.bundle.get(getBundle());
    }

    @Override
    public String getBundle(){
        return "ability." + name("fortified-armor-ability");
    }
}
