package wh.entities.world.blocks.production;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.struct.EnumSet;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.power.NuclearReactor;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Env;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHFx;
import wh.content.WHItems;

import static mindustry.Vars.tilesize;

/**
 * A reactor that also behaves like a heat producer:
 * - Uses NuclearReactor safety/cooling/meltdown logic.
 * - Outputs stable heat while running, scaled by production efficiency.
 */
public class HeatProducerReactor extends NuclearReactor{
    /** Heat network output at full efficiency. */
    public float workHeatOutput = 10f;
    /** How fast output heat approaches target heat. */
    public float workHeatWarmupRate = 0.15f;
    /**
     * Whether output heat scales with stored fuel fullness instead of any-valid-operation.
     */
    public boolean scaleHeatWithFuel = true;

    public HeatProducerReactor(String name){
        super(name);
        heatOutput = 0f;
        heatWarmupRate = 0f;
        itemCapacity = 30;
        liquidCapacity = 30;
        hasItems = true;
        hasLiquids = true;
        rebuildable = false;
        emitLight = true;

        rotateDraw = false;
        rotate = true;
        canOverdrive = false;
        drawArrow = true;

        flags = EnumSet.of(BlockFlag.reactor, BlockFlag.factory);
        schematicPriority = -5;
        envEnabled = Env.any;

        explosionShake = 6f;
        explosionShakeDuration = 16f;

        explosionRadius = 19;
        explosionDamage = 1250 * 4;
        Color c = WHItems.uranium.color.cpy().lerp(Pal.lighterOrange, 0.5f);
        float life = 300;
        explodeEffect = new MultiEffect(
        WHFx.hitSpark(90, c, 40, explosionRadius * Vars.tilesize, 2, 20),
        WHFx.hitCircle(life, c, c, 15, explosionRadius * Vars.tilesize, 25),
        WHFx.generalExplosion(life, c, explosionRadius * Vars.tilesize, 25, false),
        WHFx.circleOut(life, c, explosionRadius * Vars.tilesize)
        );
        explodeSound = Sounds.explosionReactor;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.output, workHeatOutput, StatUnit.heatUnits);
    }

    @Override
    public void setBars(){
        super.setBars();
        removeBar("power");
        addBar("workHeat", (HeatProducerReactorBuild entity) ->
        new Bar("bar.heat", Pal.accent, () -> workHeatOutput <= 0f ? 0f : entity.workHeat / workHeatOutput));
    }

    public class HeatProducerReactorBuild extends NuclearReactorBuild{
        public float workHeat, warmup;

        @Override
        public void updateTile(){
            super.updateTile();
            int fuel = items.get(fuelItem);
            boolean valid = enabled && fuel > 0;

            warmup = Mathf.lerpDelta(warmup, valid ? 1f : 0f, 0.09f);

            if (!valid) {
                workHeat = Mathf.approachDelta(workHeat, 0f, 0.2f);
            } else {
                float heatScale = scaleHeatWithFuel ? (float) fuel / Math.max(itemCapacity, 1) : 1f;
                float target = workHeatOutput * heatScale;
                workHeat = Mathf.approachDelta(workHeat, target, workHeatWarmupRate);
            }
        }

        @Override
        public void draw(){
            drawer.draw(this);

            Draw.color(coolColor, hotColor, heat);
            Fill.rect(x, y, size * tilesize, size * tilesize);

            if(heat > flashThreshold){
                flash += (1f + ((heat - flashThreshold) / (1f - flashThreshold)) * 5.4f) * Time.delta;
                Draw.color(Color.red, Color.yellow, Mathf.absin(flash, 9f, 1f));
                Draw.alpha(0.3f);
                Draw.rect(lightsRegion, x, y);
            }

            Draw.reset();
        }

        @Override
        public float heatFrac(){
            return workHeatOutput <= 0f ? 0f : workHeat / workHeatOutput;
        }

        @Override
        public float heat(){
            return workHeat;
        }

        @Override
        public float warmup(){
            return warmup;
        }

        @Override
        public float ambientVolume(){
            return Mathf.clamp(warmup);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(workHeat);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            workHeat = read.f();
            warmup = read.f();
        }
    }
}
