package wh.gen;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.payloads.*;

import static mindustry.Vars.world;

public class HoverPayloadUnit extends ElevationMoveUnit implements Payloadc, WaterMovec{
    public Seq<Payload> payloads = new Seq<>();
    public float LandSpeedMultiplier = 0.7f;
    protected transient Trail tleft = new Trail(1);
    protected transient Color trailColor = Blocks.water.mapColor.cpy().mul(1.5F);
    protected transient Trail tright = new Trail(1);

    @Override
    public int classId(){
        return EntityRegister.getId(HoverPayloadUnit.class);
    }

    @Override
    public Seq<Payload> payloads(){
        return payloads;
    }

    @Override
    public void payloads(Seq<Payload> payloads){
        this.payloads = payloads;
    }

    public boolean canPickup(Building build){

        return false;
        /* payloadUsed() + build.block.size * build.block.size * Vars.tilesize * Vars.tilesize <= type.payloadCapacity + 0.001F && build.canPickup() && build.team == team;*/
    }

    public boolean canPickup(Unit unit){

        return type.pickupUnits && payloadUsed() + unit.hitSize * unit.hitSize <= type.payloadCapacity + 0.001F && unit.team == team() && unit.isAI();
    }

    public boolean canPickupPayload(Payload pay){

        return payloadUsed() + pay.size() * pay.size() <= type.payloadCapacity + 0.001F && (type.pickupUnits || !(pay instanceof UnitPayload));
    }


    public boolean dropBlock(BuildPayload payload){

      /*  Building tile = payload.build;
        int tx = World.toTile(x - tile.block.offset);
        int ty = World.toTile(y - tile.block.offset);
        Tile on = Vars.world.tile(tx, ty);
        if (on != null && Build.validPlace(tile.block, tile.team, tx, ty, tile.rotation, false)) {
            payload.place(on, tile.rotation);
            Events.fire(new PayloadDropEvent(this, tile));
            if (getControllerName() != null) {
                payload.build.lastAccessed = getControllerName();
            }
            Fx.unitDrop.at(tile);
            on.block().placeEffect.at(on.drawx(), on.drawy(), on.block().size);
            return true;
        }*/
        return false;
    }

    public boolean dropLastPayload(){

        if(payloads.isEmpty()) return false;
        Payload load = payloads.peek();
        if(tryDropPayload(load)){
            payloads.pop();
            return true;
        }
        return false;
    }

    public boolean dropUnit(UnitPayload payload){

        Unit u = payload.unit;
        Tmp.v1.rnd(Mathf.random(2.0F));
        if(!u.canPass(World.toTile(x + Tmp.v1.x), World.toTile(y + Tmp.v1.y)) || Units.count(x, y, u.physicSize(), (o) -> o.isGrounded() && o.hitSize > 14.0F) > 1){
            return false;
        }
        Fx.unitDrop.at(this);
        if(Vars.net.client()) return true;
        u.set(x + Tmp.v1.x, y + Tmp.v1.y);
        u.rotation(rotation);
        u.id = EntityGroup.nextId();
        if(!u.isAdded()) u.team.data().updateCount(u.type, -1);
        u.add();
        u.unloaded();
        Events.fire(new PayloadDropEvent(this, u));
        return true;
    }

    public boolean hasPayload(){

        return payloads.size > 0;
    }

    @Override
    public boolean tryDropPayload(Payload payload){

        Tile on = tileOn();
        if(Vars.net.client() && payload instanceof UnitPayload u){
            Vars.netClient.clearRemovedEntity(u.unit.id);
        }
        if(on != null && on.build != null && on.build.team == team && on.build.acceptPayload(on.build, payload)){
            Fx.unitDrop.at(on.build);
            on.build.handlePayload(on.build, payload);
            return true;
        }
        if(payload instanceof BuildPayload b){
            return dropBlock(b);
        }else if(payload instanceof UnitPayload p){
            return dropUnit(p);
        }
        return false;
    }

    public float payloadUsed(){

        return payloads.sumf((p) -> p.size() * p.size());
    }

    public void addPayload(Payload load){

        payloads.add(load);
    }

    public void contentInfo(Table table, float itemSize, float width){

        table.clear();
        table.top().left();
        float pad = 0;
        float items = payloads.size;
        if(itemSize * items + pad * items > width){
            pad = (width - (itemSize) * items) / items;
        }
        for(Payload p : payloads){
            table.image(p.icon()).size(itemSize).padRight(pad);
        }
    }

    @Override
    public void destroy(){
        if(Vars.state.rules.unitPayloadsExplode) payloads.each(Payload::destroyed);
        super.destroy();
    }


    @Override
    public void pickup(Building tile){

      /*  tile.pickedUp();
        tile.tile.remove();
        tile.afterPickedUp();
        addPayload(new BuildPayload(tile));
        Fx.unitPickup.at(tile);
        Events.fire(new PickupEvent(this, tile));*/
    }

    @Override
    public void pickup(Unit unit){

        if(unit.isAdded()) unit.team.data().updateCount(unit.type, 1);
        unit.remove();
        addPayload(new UnitPayload(unit));
        Fx.unitPickup.at(unit);
        if(Vars.net.client()){
            Vars.netClient.clearRemovedEntity(unit.id);
        }
        Events.fire(new PickupEvent(this, unit));
    }

    public boolean onLiquid(){

        Tile tile = tileOn();
        return tile != null && tile.floor().isLiquid;
    }

    @Override
    public void add(){
        super.add();
        tleft.clear();
        tright.clear();
    }

    @Override
    public void draw(){
        super.draw();

        float z = Draw.z();
        Draw.z(Layer.debris);
        Floor floor = tileOn() == null ? Blocks.air.asFloor() : tileOn().floor();
        Color color = Tmp.c1.set(floor.mapColor.equals(Color.black) ? Blocks.water.mapColor : floor.mapColor).mul(1.5F);
        trailColor.lerp(color, Mathf.clamp(Time.delta * 0.04F));
        tleft.draw(trailColor, type.trailScl);
        tright.draw(trailColor, type.trailScl);
        Draw.z(z);

    }

    @Override
    public float floorSpeedMultiplier(){

        Floor on = isFlying() || type.hovering ? Blocks.air.asFloor() : floorOn();
        float liquidSpeed = onLiquid() ? 1 : LandSpeedMultiplier;
        return (float)Math.pow(on.speedMultiplier, type.floorMultiplier) * liquidSpeed * speedMultiplier;
    }

    @Override
    public void update(){
        super.update();

        boolean flying = isFlying();
        for(int i = 0; i < 2; i++){
            Trail t = i == 0 ? tleft : tright;
            t.length = type.trailLength;
            int sign = i == 0 ? -1 : 1;
            float cx = Angles.trnsx(rotation - 90, type.waveTrailX * sign, type.waveTrailY) + x;
            float cy = Angles.trnsy(rotation - 90, type.waveTrailX * sign, type.waveTrailY) + y;
            t.update(cx, cy, world.floorWorld(cx, cy).isLiquid && onLiquid() && !flying ? 1 : 0);
        }
    }

    @Override
    public void read(Reads read){
        super.read(read);

        int payloads_LENGTH = read.i();
        this.payloads.clear();
        for(int INDEX = 0; INDEX < payloads_LENGTH; INDEX++){
            mindustry.world.blocks.payloads.Payload payloads_ITEM = mindustry.io.TypeIO.readPayload(read);
            if(payloads_ITEM != null) this.payloads.add(payloads_ITEM);
        }
    }

    @Override
    public void write(Writes write){
        super.write(write);
        write.i(this.payloads.size);
        for(int INDEX = 0; INDEX < this.payloads.size; INDEX++){
            mindustry.io.TypeIO.writePayload(write, this.payloads.get(INDEX));
        }
    }
}
