package wh.entities.world.blocks.defense;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.*;
import wh.content.*;

import static mindustry.Vars.*;

public class ReactionArmorWall extends Wall{
    public int frequency = 10;
    public int immunityAccount = 2;
    public float damageReduction = 0f;
    public float maxShareStep = 2f;
    public boolean shareDamage = false;

    public ReactionArmorWall(String name){
        super(name);
    }

    public class ReactionArmorWallBuild extends WallBuild{
        public int hitCount, immunity;
        public boolean isImmune;
        public Seq<Building> toDamage = new Seq<>();
        public Queue<Building> queue = new Queue<Building>();

        @Override
        public void pickedUp(){
            isImmune = false;
        }

        @Override
        public void damage(float damage){
            if(!isImmune){
                super.damage(damage);
            }
        }

        @Override
        public boolean collision(Bullet bullet){
            if(isImmune){
                immunity++;
            }else hitCount++;

            if(hitCount > frequency){
                hitCount = 0;
                immunity = immunityAccount;
                isImmune = true;
            }

            if(immunity > immunityAccount){
                immunity = 0;
                isImmune = false;
            }

            return super.collision(bullet);
        }

        public void findLinkWalls(){
            toDamage.clear();
            queue.clear();

            queue.addLast(this);
            while(queue.size > 0){
                Building wall = queue.removeFirst();
                toDamage.addUnique(wall);
                for(Building next : wall.proximity){
                    if(linkValid(next) && !toDamage.contains(next)){
                        toDamage.add(next);
                        queue.addLast(next);
                    }
                }
            }
        }

        public boolean linkValid(Building build){
            return checkWall(build) && Mathf.dstm(tileX(), tileY(), build.tileX(), build.tileY()) <= maxShareStep;
        }

        public boolean checkWall(Building build){
            return build != null && build.block == this.block;
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            if(shareDamage){
                for(Building wall : toDamage){
                    Draw.color(team.color);
                    Draw.alpha(0.5f);
                    Fill.square(wall.x, wall.y, 2);
                }
                Draw.reset();
            }
        }

        @Override
        public float handleDamage(float amount){
            if(shareDamage){
                findLinkWalls();
                float shareDamage = (amount / toDamage.size) * (1 - damageReduction);
                for(Building b : toDamage){
                    damageShared(b, shareDamage);
                }
                return shareDamage;
            }else return super.handleDamage(amount);
        }

        //todo healthChanged sometimes not trigger properly
        public void damageShared(Building building, float damage){
            if(building.dead()) return;
            float dm = state.rules.blockHealth(team);
            if(Mathf.zero(dm)){
                damage = building.health + 1;
            }else{
                damage /= dm;
            }
            if(!net.client()){
                building.health -= damage;
            }
            if(damaged()){
                healthChanged();
            }
            if(building.health <= 0){
                Call.buildDestroyed(building);
            }
            WHFx.shareDamage.at(building.x, building.y, building.block.size * tilesize / 2f, team.color, Mathf.clamp(damage / (block.health * 0.1f)));
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            hitCount = read.i();
            immunity = read.i();
            isImmune = read.bool();
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(hitCount);
            write.i(immunity);
            write.bool(isImmune);
        }
    }
}
