package wh.entities.world.entities.weapon;

import arc.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;

public class MultipleWeapon extends Weapon{
    public boolean uesMaxRange = false;
    public Seq<Weapon> weapons = new Seq<>();

    public MultipleWeapon(String name){
        super(name);
    }

    public void addWeapon(Weapon... weapon){
        weapons.addAll(weapon);
    }

    @Override
    public void addStats(UnitType u, Table t){
        super.addStats(u, t);
        if(weapons.isEmpty()) return;
        t.add(Core.bundle.format("stat.wh-extraWeapons"));
        t.row();
        for(Weapon weapon : weapons){
            weapon.addStats(u, t);
            t.row();
        }
    }

    @Override
    public void drawOutline(Unit unit, WeaponMount mount){
        super.drawOutline(unit, mount);
        if(weapons.isEmpty()) return;
        for(Weapon weapon : weapons){
            weapon.drawOutline(unit, mount);
        }
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){
        super.draw(unit, mount);
        if(weapons.isEmpty()) return;
        for(Weapon weapon : weapons){
            weapon.draw(unit, mount);
        }
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        super.update(unit, mount);
        if(weapons.isEmpty()) return;
        for(Weapon weapon : weapons){
            weapon.update(unit, mount);
        }
    }

    @Override
    public void init(){
        super.init();
        if(weapons.isEmpty()) return;
        for(Weapon weapon : weapons){
            weapon.init();
            weapon.rotate = rotate;
            weapon.rotateSpeed = rotateSpeed;
            weapon.controllable = controllable;
            weapon.x = x;
            weapon.y = y;
        }
    }

    @Override
    public void load(){
        super.load();
        if(weapons.isEmpty()) return;
        for(Weapon weapon : weapons){
            weapon.load();
        }
    }

    @Override
    public float range(){
        if(weapons == null || weapons.isEmpty()){
            return super.range();
        }
        if(uesMaxRange){
            weapons.sort();
            return Math.max(weapons.max(Weapon::range).range(), super.range());
        }else return super.range();
    }
}
