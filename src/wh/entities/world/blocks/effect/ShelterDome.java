package wh.entities.world.blocks.effect;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.Fx;
import mindustry.ctype.Content;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.logic.Ranged;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.PayloadBlock;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.entities.world.Psy.PsychicModule;
import wh.entities.world.Psy.PsychicNetworkNode;
import wh.entities.world.blocks.unit.UnitCallBlock.UnitCallBlockBuild;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.*;

public class ShelterDome extends PayloadBlock{
    public float psychicCapacity = 40f;
    public float psychicPerUnit = 15f;
    public float maxPayloadSize = 3.9f;
    public float range = 160f;
    public float blockMaxSpeedBoost = 1.5f;
    public float payloadBlockMaxSpeedBoost = 2f;
    public float buildSpeedRate = 4f;
    public float deconstructSpeed = 1.5f;
    public Color baseColor = WHPal.ShootOrange;
    public Color baseColor2 = WHPal.ShootOrangeLight;

    public ShelterDome(String name){
        super(name);
        outputsPayload = false;
        acceptsPayload = true;
        update = true;
        rotate = false;
        solid = true;
        size = 4;
        payloadSpeed = 1f;
        //make sure to display large units.
        clipSize = 120;
        hasItems = false;
        hasPower = true;
        canOverdrive = false;
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, topRegion};
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("progress", (ShelterDomeBuild e) -> new Bar("bar.progress", Pal.ammo, e::reload));
        addBar("psychic", (ShelterDomeBuild e) -> new PsychicBar(
                () -> Core.bundle.format("bar.wh-psychic-storage",
                        Strings.autoFixed(e.psychicStored(), 2),
                        Strings.autoFixed(psychicCapacity, 0)),
                () -> WHPal.PsyColor,
                e::psychicFraction
        ));
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.speedIncrease, Core.bundle.get("stat.wh-block-boost") + "+" + (int)(blockMaxSpeedBoost * 100f - 100) + "%");
        stats.add(Stat.speedIncrease, Core.bundle.get("stat.wh-unit-block-boost") + "+" + (int)(payloadBlockMaxSpeedBoost * 100f - 100) + "%");
        stats.add(WHStats.payloadIsBuildRate, (int)(buildSpeedRate * 100f) + "%");
        PsychicStatValues.add(stats, WHStats.psychicProduction, psychicPerUnit, StatUnit.none);
        stats.add(Stat.range, range / tilesize, StatUnit.blocks);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, baseColor);

        indexer.eachBlock(player.team(), x * tilesize + offset, y * tilesize + offset, range, other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));
    }

    public class ShelterDomeBuild extends PayloadBlockBuild<Payload> implements Ranged, PsychicNetworkNode {
        public float heat, progress = 0f, time;
        public @Nullable Payload consumePayload;
        public final PsychicModule psychic = new PsychicModule();


        @Override
        public void draw(){
            Draw.rect(region, x, y);

            //draw input
            for(int i = 0; i < 4; i++){
                if(blends(i)){
                    Draw.rect(inRegion, x, y, (i * 90));
                }
            }

            Draw.z(Layer.blockOver);
            drawPayload();

            if(consumePayload != null){
                consumePayload.set(x + payVector.x, y + payVector.y, payRotation);

                Draw.z(Layer.blockOver);
                consumePayload.drawShadow(1 - reload());

                Draw.draw(Layer.blockOver, () -> {
                    Drawf.construct(x, y, consumePayload.icon(), Pal.remove, consumePayload instanceof BuildPayload ? 0f : payRotation - 90f,
                    1 - reload(), 1 - reload(), time);
                    Draw.color(Pal.remove);
                    Draw.alpha(1f);

                    Lines.lineAngleCenter(x + Mathf.sin(time, 20f, tilesize / 2f * block.size - 3f), y, 90f, block.size * tilesize - 6f);

                    Draw.reset();
                });
            }

            Draw.rect(topRegion, x, y);
            Draw.reset();

            float f = 1f - (Time.time / 100f) % 1f;
            Draw.color(baseColor, baseColor2, Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
            Draw.alpha(heat * Mathf.absin(Time.time, 50f / Mathf.PI2, 1f) * 0.5f);
            Draw.alpha(1f);
            Lines.stroke((2f * f + 0.1f) * heat);

            float r = Math.max(0f, Mathf.clamp(2f - f * 2f) * size * tilesize / 2f - f - 0.2f), w = Mathf.clamp(0.5f - f) * size * tilesize;
            Lines.beginLine();
            for(int i = 0; i < 4; i++){
                Lines.linePoint(x + Geometry.d4(i).x * r + Geometry.d4(i).y * w, y + Geometry.d4(i).y * r - Geometry.d4(i).x * w);
                if(f < 0.5f) Lines.linePoint(x + Geometry.d4(i).x * r - Geometry.d4(i).y * w, y + Geometry.d4(i).y * r + Geometry.d4(i).x * w);
            }
            Lines.endLine(true);

            Draw.reset();
        }

        @Override
        public void updateTile(){
            super.updateTile();
            heat = Mathf.lerpDelta(heat, consumePayload != null && efficiency > 0f ? 1f : 0f, 0.05f);
            psychic.clamp(psychicCapacity);

            if(consumePayload != null){
                float reload = consumePayload.buildTime() / deconstructSpeed;
                float rate = consumePayload instanceof BuildPayload ? buildSpeedRate : 1f;
                progress += heat * Time.delta * rate;
                time += Time.delta * heat;
                payRotation = Angles.moveToward(payRotation, 90f, payloadRotateSpeed * edelta());
                float p = progress / reload;
                if(p >= 1){
                    progress = 0f;
                    indexer.eachBlock(this, range(), other -> other.block.canOverdrive && (other instanceof PayloadBlockBuild<?> || other instanceof UnitCallBlockBuild),
                    other -> other.applyBoost(getHeat() * payloadBlockMaxSpeedBoost, reload + 1f));

                    indexer.eachBlock(this, range(), other -> other.block.canOverdrive &&
                    !(other instanceof TurretBuild) && !(other instanceof PayloadBlockBuild<?>) && !(other instanceof UnitCallBlockBuild),
                    other -> other.applyBoost(getHeat() * blockMaxSpeedBoost, reload + 1f));

                    if (consumePayload instanceof UnitPayload) {
                        addPsychic(psychicPerUnit);
                    }

                    Fx.breakBlock.at(x, y, consumePayload.size() / tilesize);
                    consumePayload = null;
                }
            }else if(moveInPayload(false) && payload != null){
                consumePayload = payload;
                payload = null;
                progress = 0;
            }
        }

        public float reload(){
            if(consumePayload != null){
                float reload = consumePayload.buildTime() / deconstructSpeed;
                return Mathf.clamp(progress / reload, 0f, 1f);
            }
            return 0f;
        }

        public float getHeat(){
            return efficiency;
        }

        public float psychicStored() {
            return psychic.amount();
        }

        public float psychicFraction() {
            return psychic.fraction(psychicCapacity);
        }

        public float addPsychic(float amount) {
            return psychic.add(amount, psychicCapacity);
        }

        public float drainPsychic(float amount) {
            return psychic.remove(amount);
        }

        @Override
        public boolean acceptEnergy(PsychicNetworkNode source) {
            return false;
        }

        @Override
        public boolean outputEnergy() {
            return enabled && psychicStored() > PsychicNetworkNode.epsilon;
        }

        @Override
        public float getEnergyNeed() {
            return 0f;
        }

        @Override
        public float getEnergy() {
            return psychicStored();
        }

        @Override
        public float getEnergyPotential() {
            return psychicStored();
        }

        @Override
        public float handleEnergy(float amount) {
            return 0f;
        }

        @Override
        public float removeEnergy(float amount) {
            return drainPsychic(amount);
        }

        @Override
        public boolean acceptUnitPayload(Unit unit){
            return payload == null && consumePayload == null && unit.type.allowedInPayloads && !unit.spawnedByCore
            && unit.type.getTotalRequirements().length > 0 && unit.hitSize / tilesize <= maxPayloadSize;
        }

        @Override
        public void handlePayload(Building source, Payload payload){
            super.handlePayload(source, payload);
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload){
            return consumePayload == null && this.payload == null && super.acceptPayload(source, payload)
            && payload.requirements().length > 0 && payload.fits(maxPayloadSize);
        }


        @Override
        public double sense(Content content){
            if(consumePayload instanceof UnitPayload up) return up.unit.type == content ? 1 : 0;
            if(consumePayload instanceof BuildPayload bp) return bp.build.block == content ? 1 : 0;
            return super.sense(content);
        }

        @Override
        public double sense(LAccess sensor){
            if(sensor == LAccess.progress) return progress;
            return super.sense(sensor);
        }

        @Override
        public boolean shouldConsume(){
            return consumePayload != null && enabled;
        }

        @Override
        public float range(){
            return range * heat;
        }

        @Override
        public void drawSelect(){
            float realRange = range();

            indexer.eachBlock(this, realRange, other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(baseColor).a(Mathf.absin(4f, 1f))));

            Drawf.dashCircle(x, y, realRange, baseColor);
        }


        @Override
        public void write(Writes write){
            super.write(write);

            write.f(progress);
            write.f(heat);
            write.f(time);
            Payload.write(consumePayload, write);
            psychic.write(write);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            progress = read.f();
            heat = read.f();
            time = read.f();
            consumePayload = Payload.read(read);
            psychic.read(read);
        }
    }
}
