package wh.entities.world.blocks.defense.turrets;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import wh.core.*;
import wh.graphics.*;

import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.*;
import static wh.content.WHFx.rand;

public class ReckoningTurret extends WHItemTurret{
    public float windupSpeed = 0.5f / 60f, windDownSpeed = 0.75f / 60f;
    public float minFiringWarmup = 0.5f, logicSpeedScl = 0.2f, maxReloadMultiple = 5;
    public float barrelWidth = 12 / 4f;
    public TextureRegion barrel;

    public float effectRange = 32;
    public float speedBoost = 0.25f;
    public Color baseColor = WHPal.ShootOrange;

    public Effect applyEffect = new Effect(90, e -> {
        if(!(e.data instanceof Float)) return;
        float range = (float)e.data;
        rand.setSeed(e.id);
        Draw.color(baseColor);
        Lines.stroke(e.fout() * 2.5f * rand.random(0.5f, 1f));
        randLenVectors(e.id, 3, 15 * e.fin() * rand.random(1) + range * rand.random(0.45f, 1), (x, y) -> {
            Tmp.v1.trns(90, e.fin() * 25 * rand.random(0.5f, 1.2f));
            Lines.lineAngle(e.x + x + Tmp.v1.x, e.y + y + Tmp.v1.y, 90, e.finpow() * 20);
            Drawf.light(e.x + x + Tmp.v1.x, e.y + y + Tmp.v1.y, 10 * e.fout(), Draw.getColor(), 0.8f);
        });
        Draw.reset();
    });

    @Override
    public void init(){
        WHItemTurret.intTurret(this);
        super.init();
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.speedIncrease, Core.bundle.get("stat.wh-block-boost") + "+" + Mathf.round(speedBoost * 100f) + "%");
        stats.add(Stat.range, effectRange, StatUnit.blocks);
        stats.add(Stat.productionTime, reload / 60f, StatUnit.seconds);
    }

    public ReckoningTurret(String name){
        super(name);
        canOverdrive = false;
        drawer = new DrawTurret(WarHammerMod.name("turret-")){
            TextureRegion barrel, barrelOutline;
            public final float x = -51 / 4f, y = 30 / 4f;

            @Override
            public void getRegionsToOutline(Block block, Seq<TextureRegion> out){
                super.getRegionsToOutline(block, out);
                out.add(barrel);
            }

            @Override
            public void load(Block block){
                super.load(block);
                heat = Core.atlas.find(block.name + "-barrel-heat");
                barrel = Core.atlas.find(block.name + "-barrel");
                barrelOutline = Core.atlas.find(block.name + "-barrel-outline");
            }

            @Override
            public void drawTurret(Turret block, TurretBuild build){
                if(!(build instanceof ReckoningTurretBuild m)) return;

                Vec2 v = Tmp.v1;
                Vec2 v2 = Tmp.v2;

                Draw.z(Layer.turret - 0.01f);
                Draw.rect(outline, build.x + m.recoilOffset.x, build.y + m.recoilOffset.y, build.drawrot());
                for(int a : Mathf.signs){
                    for(int i = 0; i < 5; i++){
                        float offset = 350 / 5f * i;
                        v2.trns(m.rotation - 90f, x * a, y);
                        v.trns(m.rotation - 90f, barrelWidth * Mathf.cosDeg(m.spin - offset), recoil * Mathf.sinDeg(m.spin - offset)).add(m.recoilOffset).add(v2);

                        if(Mathf.chanceDelta(0.002 * m.reloadMultiple)){
                            coolEffect.at(m.x + v.x + Mathf.range(barrelWidth * tilesize / 2f), m.x + v.x + Mathf.range(barrelWidth * tilesize / 2f));
                        }

                        Draw.z(Layer.turret - 0.008f - Mathf.sinDeg(m.spin - offset) / 1000f);
                        Draw.rect(barrelOutline, m.x + v.x, m.y + v.y, m.drawrot());
                        Draw.rect(barrel, m.x + v.x, m.y + v.y, m.drawrot());
                        if(m.heats[i] > 0.001f){
                            Draw.z(Layer.turret - 0.005f - Mathf.sinDeg(m.spin - offset) / 1000f);
                            Drawf.additive(heat, heatColor.write(Tmp.c1).a(m.heats[i]), m.x + v.x, m.y + v.y, m.drawrot(), Draw.z());
                        }
                    }
                }

                Draw.z(Layer.turret);
                super.drawTurret(block, build);
            }

            @Override
            public void drawHeat(Turret block, TurretBuild build){
                //Don't
            }
        };
    }

    @Override
    public void load(){
        super.load();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        x *= tilesize;
        y *= tilesize;
        x += offset;
        y += offset;

        Drawf.dashSquare(baseColor, x, y, effectRange * tilesize);
        indexer.eachBlock(player.team(), Tmp.r1.setCentered(x, y, effectRange * tilesize), b -> true, t -> {
            Drawf.selected(t, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f)));
        });
    }

    public class ReckoningTurretBuild extends ItemTurretBuild{
        protected float[] heats = {0f, 0f, 0f, 0f, 0f};
        public float spinSpeed, spin;
        public float reloadMultiple, charge = Mathf.random(reload);

        @Override
        public void draw(){
            super.draw();
        }

        @Override
        public void updateTile(){
            boolean notShooting = !hasAmmo() || !isShooting() || !isActive();
            if(notShooting){
                float speed = windDownSpeed * timeScale();
                spinSpeed = Mathf.approachDelta(spinSpeed, 0, speed * 1.3f);
                reloadMultiple = Mathf.approachDelta(reloadMultiple, 0, speed);
            }

            if(spinSpeed > getMaxSpeed()){
                spinSpeed = Mathf.approachDelta(spinSpeed, getMaxSpeed(), windDownSpeed * timeScale());
            }

            if(reloadMultiple >= maxReloadMultiple / 2){
                charge += efficiency * Time.delta;
                float range = effectRange * tilesize / 2;
                if(Mathf.chanceDelta(0.02f)){
                    applyEffect.at(x, y, 0, baseColor, range);
                }
            }

            if(charge >= reload){
                charge = 0f;
                indexer.eachBlock(team, Tmp.r1.setCentered(x, y, effectRange * tilesize * efficiency), other ->
                other instanceof ReloadTurretBuild && other.block.canOverdrive,
                other -> other.applyBoost(realBoost(), reload + 1f));
            }

            for(int i = 0; i < 5; i++){
                heats[i] = Math.max(heats[i] - Time.delta / cooldownTime, 0);
            }

            super.updateTile();
        }


        @Override
        protected void updateShooting(){
            if(!hasAmmo()) return;
            float m = (!isControlled() && logicControlled() && logicShooting ? logicSpeedScl : 1f);
            reloadMultiple = Mathf.approachDelta(reloadMultiple, maxReloadMultiple * m, windupSpeed * peekAmmo().reloadMultiplier * timeScale());

            spinSpeed = Mathf.approachDelta(spinSpeed, getMaxSpeed(), windupSpeed * 1.3f * peekAmmo().reloadMultiplier * timeScale());

            if(reloadCounter >= reload && spinSpeed > getMaxSpeed() * minFiringWarmup){
                BulletType type = peekAmmo();

                shoot(type);

                reloadCounter %= reload;

                heats[(int)(Mathf.floor(Math.max(spin - reload, 0)) % 360f / reload) % 5] = 1f;
            }
        }

        @Override
        protected void updateReload(){
            float add = delta() * (1 + reloadMultiple) * ammoReloadMultiplier();
            reloadCounter += add * baseReloadSpeed();
            spin += add * spinSpeed;
            reloadCounter = Math.min(reloadCounter, reload);
        }

        @Override
        protected void updateCooling(){
            boolean shooting = hasAmmo() && isShooting() && isActive();
            if(shooting && reloadCounter < reload && coolant != null && coolant.efficiency(this) > 0 && efficiency > 0){
                float capacity = coolant instanceof ConsumeLiquidFilter filter ? filter.getConsumed(this).heatCapacity : (coolant.consumes(liquids.current()) ? liquids.current().heatCapacity : 0.4f);
                float amount = coolant.amount * coolant.efficiency(this);
                coolant.update(this);
                reloadCounter += amount * edelta() * capacity * coolantMultiplier * ammoReloadMultiplier();

                if(Mathf.chance(0.06 * amount)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        @Override
        public void drawSelect(){
            super.drawSelect();
            float realRange = effectRange * tilesize;
            Drawf.dashSquare(baseColor, x, y, realRange);

            indexer.eachBlock(team, Tmp.r1.setCentered(x, y, effectRange * tilesize * efficiency),
            other -> other instanceof ReloadTurretBuild && other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
        }

        public float realBoost(){
            return speedBoost * efficiency;
        }


        protected float getMaxSpeed(){
            return maxReloadMultiple * reloadMultiple * reload / 360f * (!isControlled() && logicControlled() && logicShooting ? logicSpeedScl : 1f);
        }


        @Override
        public void write(Writes write){
            super.write(write);
            write.f(spinSpeed);
            write.f(spin % 360f);
            write.f(reloadMultiple);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 2){
                spinSpeed = read.f();
                if(revision >= 3){
                    spin = read.f();
                    reloadMultiple = read.f();
                }
            }
        }

        @Override
        public byte version(){
            return 3;
        }

    }
}
