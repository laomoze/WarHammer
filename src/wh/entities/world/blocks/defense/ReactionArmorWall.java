package wh.entities.world.blocks.defense;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.struct.Queue;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Call;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHFx;
import wh.content.WHStats;

import static mindustry.Vars.*;

public class ReactionArmorWall extends Wall{
    public int frequency = 10;
    public int immunityAccount = 2;
    public float immunityDuration = 120f;
    public float damageReduction = 0.03f;
    public float maxShareStep = 2f;
    public boolean shareDamage = false;

    public ReactionArmorWall(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

        // trigger condition in current logic is hitCount > frequency, i.e. frequency + 1 hits
        stats.add(WHStats.reactionArmorTriggerHits, frequency);
        stats.add(WHStats.reactionArmorLayers, immunityAccount);

        if(shareDamage){
            stats.add(WHStats.sharedDamageReduction, damageReduction * 100f, StatUnit.percent);
            stats.add(Stat.range, maxShareStep, StatUnit.blocks);
        }
    }

    public class ReactionArmorWallBuild extends WallBuild{
        public int hitCount, immunity;
        public boolean isImmune;
        public transient float immunityTimer;
        public Seq<Building> toDamage = new Seq<>();
        public Queue<Building> queue = new Queue<Building>();

        @Override
        public void pickedUp(){
            hitCount = 0;
            immunity = 0;
            isImmune = false;
            immunityTimer = 0f;
        }

        @Override
        public void damage(float damage){
            if(!isImmune){
                super.damage(damage);
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            if (isImmune && immunityDuration > 0f) {
                immunityTimer -= Time.delta;
                if (immunityTimer <= 0f) {
                    immunity = 0;
                    isImmune = false;
                    immunityTimer = 0f;
                }
            }
        }

        @Override
        public boolean collision(Bullet bullet){
            // Resolve this hit first, then update reaction armor state.
            boolean collided = super.collision(bullet);
            if(isImmune){
                // Immunity layers are consumed per hit.
                immunity = Math.max(0, immunity - 1);
                if(immunity <= 0){
                    immunity = 0;
                    isImmune = false;
                    immunityTimer = 0f;
                }
                return collided;
            }

            hitCount++;
            int triggerHits = Math.max(1, frequency);
            if(hitCount >= triggerHits){
                hitCount = 0;
                immunity = Math.max(0, immunityAccount);
                isImmune = immunity > 0;
                immunityTimer = isImmune ? Math.max(immunityDuration, 0f) : 0f;
            }
            return collided;
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
                findLinkWalls();
                for(Building wall : toDamage){
                    Draw.z(Layer.effect);
                    Draw.color(team.color.cpy().lerp(Pal.techBlue, 0.2f));
                    Draw.alpha(0.7f);
                    Fill.square(wall.x, wall.y, 2.5f);
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
