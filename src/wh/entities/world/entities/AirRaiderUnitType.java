package wh.entities.world.entities;

import arc.Events;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.entities.part.DrawPart;
import mindustry.entities.units.StatusEntry;
import mindustry.entities.units.WeaponMount;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.UnitDestroyEvent;
import mindustry.game.Team;
import mindustry.gen.Payloadc;
import mindustry.gen.Segmentc;
import mindustry.gen.TimedKillUnit;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.io.TypeIO;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.blocks.ExplosionShield;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.BlockFlag;
import wh.gen.EntityRegister;

import static arc.graphics.g2d.Draw.xscl;
import static mindustry.Vars.*;

/** 史 **/
public class AirRaiderUnitType extends TimedKillUnit{
    private static final float targetableAlpha = 0.9f;
    private static final float minimumFadeTime = 0.0001f;
    private static final float initialAlpha = 0.01f;

    public float alpha = initialAlpha;
    public float fadeOutTime;
    public float fadeInTime;
    public boolean end;
    public static final float reduceDamageRatio = 0.4f;

    //AIsave
    public boolean aiIsStrafing = false;
    public boolean aiHasReachedEnd = false;
    public Vec2 aiTargetPos = new Vec2();
    public Vec2 aiStartPos = new Vec2(), aiEndPos = new Vec2();
    public Vec2 aiExtendPos = new Vec2();


    public int classId(){
        return EntityRegister.getId(AirRaiderUnitType.class);
    }

    @Override
    public void update(){
        super.update();
        // 统一显隐流程：进入离场后逐帧降低 alpha，归零即销毁。
        if(end){
            alpha = Math.max(0f, alpha - Time.delta / Math.max(fadeOutTime, minimumFadeTime));
            if(alpha <= 0){
                kill();
            }
        }else{
            alpha = Math.min(1f, alpha + Time.delta / Math.max(fadeInTime, minimumFadeTime));
        }
    }

    @Override
    public void destroy(){
        if(!isAdded() || !type.killable) return;
        float explosiveness = 2.0F + item().explosiveness * stack().amount * 1.53F;
        float flammability = item().flammability * stack().amount / 1.9F;
        float power = item().charge * Mathf.pow(stack().amount, 1.11F) * 160.0F;
        if(!spawnedByCore && !end){
            Damage.dynamicExplosion(x, y, flammability, explosiveness, power, (bounds() + type.legLength / 1.7F) / 2.0F, state.rules.damageExplosions && state.rules.unitCrashDamage(team) > 0, item().flammability > 1, team, type.deathExplosionEffect);
        }else if(!end && alpha > 0){
            type.deathExplosionEffect.at(x, y, bounds() / 2.0F / 8.0F);
        }
        float shake = type.deathShake < 0 ? hitSize / 3.0F : type.deathShake;
        if(!end) Effect.shake(shake, shake, this);
        if(!end) type.deathSound.at(this);
        Events.fire(new UnitDestroyEvent(this));
        if(explosiveness > 7.0F && (isLocal() || wasPlayer)){
            Events.fire(Trigger.suicideBomb);
        }
        for(WeaponMount mount : mounts){
            if(mount.weapon.shootOnDeath && !(mount.weapon.bullet.killShooter && mount.totalShots > 0)){
                mount.reload = 0.0F;
                mount.shoot = true;
                mount.weapon.update(this, mount);
            }
        }
        if(type.flying && !spawnedByCore && type.createWreck && state.rules.unitCrashDamage(team) > 0){
            var shields = indexer.getEnemy(team, BlockFlag.shield);
            float crashDamage = Mathf.pow(hitSize, 0.75F) * type.crashDamageMultiplier * 2.5F * state.rules.unitCrashDamage(team);
            if(shields.isEmpty() || !shields.contains((b) -> b instanceof ExplosionShield s && s.absorbExplosion(x, y, crashDamage))){
                Damage.damage(team, x, y, Mathf.pow(hitSize, 0.94F) * 1.25F, crashDamage, true, false, true);
            }
        }
        if(!headless && !end){
            Effect.scorch(x, y, (int)(hitSize / 5));
            for(int i = 0; i < type.wreckRegions.length; i++){
                if(type.wreckRegions[i].found()){
                    float range = type.hitSize / 4.0F;
                    Tmp.v1.rnd(range);
                    Effect.decal(type.wreckRegions[i], x + Tmp.v1.x, y + Tmp.v1.y, rotation - 90);
                }
            }
        }
        for(Ability a : abilities){
            a.death(this);
        }
        type.killed(this);
        remove();
    }

    @Override
    public boolean targetable(Team targeter){
        return (!end && alpha >= targetableAlpha) && super.targetable(targeter);
    }

    @Override
    public boolean hittable(){
        return (!end && alpha >= targetableAlpha) && super.hittable();
    }

    @Override
    public void setType(UnitType type){
        super.setType(type);
        alpha = initialAlpha;
        end = false;
    }

    @Override
    public void damage(float amount){
        rawDamage(Damage.applyArmor(amount * (1 - reduceDamageRatio), armorOverride >= 0.0F ? armorOverride : armor) / healthMultiplier / Vars.state.rules.unitHealth(team));
    }

    @Override
    public void draw(){

        for(StatusEntry e : statuses){
            e.effect.draw(this, e.time);
        }
        if(inFogTo(Vars.player.team())) return;

        Draw.z(Layer.effect);
        Draw.alpha(alpha);
        float scl = xscl;

        if(type.buildSpeed > 0f){
            drawBuilding();
        }

        boolean isPayload = !isAdded();

        float z = drawDieZ(this, isPayload);

        if(!isPayload && (isFlying() || type.shadowElevation > 0)){
            Draw.z(Math.min(Layer.darkness, z - 1f));
            drawShadow(this);
        }

        Draw.z(Math.min(z - 0.01f, Layer.bullet - 1f));

        if(this instanceof Payloadc){
            type.drawPayload((Unit & Payloadc)this);
        }

        if(type.drawSoftShadow) drawSoftShadow(this);

        Draw.z(z);

        if(type.drawBody) type.drawOutline(this);
        drawWeaponOutlines(this);
        if(type.engineLayer > 0) Draw.z(type.engineLayer);
        if(type.trailLength > 0 && !type.naval && (this.isFlying() || !type.useEngineElevation)){
            drawTrail(this);
        }
        if(type.engines.size > 0) drawEngines(this);
        Draw.z(z);
        drawBody(this);
        drawCell(this);
        Draw.scl(scl);
        drawWeapons(this);
        type.drawLight(this);

        if(shieldAlpha > 0 && type.drawShields){
            type.drawShield(this);
        }

        drawParts();
        drawAbilities(isPayload);

        Draw.reset();
    }

    private float drawDieZ(Unit unit, boolean isPayload){
        Segmentc seg = unit instanceof Segmentc c ? c : null;
        float z =
        isPayload ? Draw.z() :
        //dead flying units are assumed to be falling, and to prevent weird clipping issue with the dark "fog", they always draw above it
        unit.elevation > 0.5f || (type.flying && unit.dead) ? (type.flyingLayer) :
        seg != null ? type.groundLayer + seg.segmentIndex() / 4000f * Mathf.sign(type.segmentLayerOrder) + (!type.segmentLayerOrder ? 0.01f : 0f) :
        type.groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);
        return z;
    }

    private void drawParts(){
        if(type.parts.isEmpty()) return;

        for(int i = 0; i < type.parts.size; i++){
            var part = type.parts.get(i);
            WeaponMount mount = mounts.length > part.weaponIndex ? mounts[part.weaponIndex] : null;

            if(mount != null){
                DrawPart.params.set(mount.warmup, mount.reload / mount.weapon.reload, mount.smoothReload, mount.heat, mount.recoil, mount.charge, x, y, rotation);
            }else{
                DrawPart.params.set(0f, 0f, 0f, 0f, 0f, 0f, x, y, rotation);
            }

            DrawPart.params.life = fin();
            type.applyColor(this);
            part.draw(DrawPart.params);
        }
    }

    private void drawAbilities(boolean isPayload){
        if(isPayload) return;

        for(Ability ability : abilities){
            Draw.reset();
            ability.draw(this);
        }
    }

    public void drawBody(Unit unit){
        type.applyColor(this);

        Draw.alpha(alpha);
        Draw.rect(type.region, unit.x, unit.y, unit.rotation - 90);
        Draw.reset();
    }


    public void drawCell(Unit unit){
        type.applyColor(unit);

        Draw.color(type.cellColor(unit));
        Draw.alpha(alpha);
        Draw.rect(type.cellRegion, unit.x, unit.y, unit.rotation - 90);
        Draw.reset();
    }


    public void drawWeapons(Unit unit){
        type.applyColor(unit);
        Draw.alpha(alpha);
        for(WeaponMount mount : unit.mounts){
            mount.weapon.draw(unit, mount);
        }
    }

    public void drawWeaponOutlines(Unit unit){
        type.applyColor(unit);
        type.applyOutlineColor(unit);

        for(WeaponMount mount : unit.mounts){
            if(!mount.weapon.top){
                //apply layer offset, roll it back at the end
                float z = Draw.z();
                Draw.z(z + mount.weapon.layerOffset);

                mount.weapon.drawOutline(unit, mount);
                Draw.alpha(alpha);

                Draw.z(z);
            }
        }
        Draw.reset();
    }


    public void drawTrail(Unit unit){
        if(unit.trail == null){
            unit.trail = new Trail(type.trailLength);
        }
        Color trailColor = Tmp.c1.set(type.trailColor == null ? unit.team.color : type.trailColor);
        trailColor.a *= alpha;

        trail.draw(trailColor,
        (type.engineSize*alpha + Mathf.absin(Time.time, 2f, type.engineSize * alpha / 4f) * (type.useEngineElevation ? unit.elevation : 1f)) * type.trailScl
        );
    }

    public void drawEngines(Unit unit){
        if((type.useEngineElevation ? unit.elevation : 1f) <= 0.0001f) return;
        for(var engine : type.engines){
            Draw.alpha(alpha);
            engine.draw(unit);
        }
        Draw.color();
    }


    public void drawSoftShadow(Unit unit){
        drawSoftShadow(unit.x, unit.y, unit.rotation);
    }

    public void drawSoftShadow(float x, float y, float rotation){
        Draw.color(0, 0, 0, 0.4f * alpha);
        float rad = 1.6f;
        float size = Math.max(type.region.width, type.region.height) * type.region.scl() * type.softShadowScl;
        Draw.rect(type.softShadowRegion, x, y, size * rad * Draw.xscl, size * rad * Draw.yscl, rotation - 90);
        Draw.color();
    }

    public void drawShadow(Unit unit){
        float e = Mathf.clamp(unit.elevation, type.shadowElevation, 1f) * type.shadowElevationScl * (1f - unit.drownTime);
        float x = unit.x + UnitType.shadowTX * e, y = unit.y + UnitType.shadowTY * e;
        Floor floor = world.floorWorld(x, y);

        float dest = floor.canShadow ? 1f : 0f;
        //yes, this updates state in draw()... which isn't a problem, because I don't want it to be obvious anyway
        unit.shadowAlpha = unit.shadowAlpha < 0 ? dest : Mathf.approachDelta(unit.shadowAlpha, dest, 0.11f);
        Draw.color(Pal.shadow, Pal.shadow.a * unit.shadowAlpha * alpha);
        Draw.rect(type.shadowRegion, unit.x + UnitType.shadowTX * e, unit.y + UnitType.shadowTY * e, unit.rotation - 90);
        Draw.color();
    }

    private static final byte version = 1;

    private void readState(Reads read, boolean withAi){
        byte revision = read.b();
        if(revision != version) throw new IllegalArgumentException("Unknown revision '" + revision + "'");

        alpha = read.f();
        fadeOutTime = read.f();
        fadeInTime = read.f();
        end = read.bool();

        if(withAi){
            aiIsStrafing = read.bool();
            aiHasReachedEnd = read.bool();
            TypeIO.readVec2(read, aiTargetPos);
            TypeIO.readVec2(read, aiStartPos);
            TypeIO.readVec2(read, aiEndPos);
            TypeIO.readVec2(read, aiExtendPos);
        }
    }

    private void writeState(Writes write, boolean withAi){
        write.b(version);
        write.f(alpha);
        write.f(fadeOutTime);
        write.f(fadeInTime);
        write.bool(end);

        if(withAi){
            write.bool(aiIsStrafing);
            write.bool(aiHasReachedEnd);
            TypeIO.writeVec2(write, aiTargetPos);
            TypeIO.writeVec2(write, aiStartPos);
            TypeIO.writeVec2(write, aiEndPos);
            TypeIO.writeVec2(write, aiExtendPos);
        }
    }

    @Override
    public void read(Reads read){
        super.read(read);
        readState(read, true);
    }

    @Override
    public void write(Writes write){
        super.write(write);
        writeState(write, true);
    }

    @Override
    public void readSync(Reads read){
        super.readSync(read);
        readState(read, false);
    }

    @Override
    public void writeSync(Writes write){
        super.writeSync(write);
        writeState(write, false);
    }


    public static class ARWeapon extends Weapon{
        private static float alphaOf(Unit unit){
            return unit instanceof AirRaiderUnitType ar ? ar.alpha : 1f;
        }

        private void setupPartParams(WeaponMount mount, float wx, float wy, float weaponRotation){
            DrawPart.params.set(mount.warmup, mount.reload / reload, mount.smoothReload, mount.heat, mount.recoil, mount.charge, wx, wy, weaponRotation + 90f);
            DrawPart.params.sideMultiplier = flipSprite ? -1 : 1;
        }

        private void drawWeaponParts(Unit unit, WeaponMount mount, boolean under, float alpha){
            if(parts.isEmpty()) return;

            for(int i = 0; i < parts.size; i++){
                var part = parts.get(i);
                DrawPart.params.setRecoil(part.recoilIndex >= 0 && mount.recoils != null ? mount.recoils[part.recoilIndex] : mount.recoil);
                if(part.under == under){
                    Draw.alpha(alpha);
                    unit.type.applyColor(unit);
                    part.draw(DrawPart.params);
                }
            }
        }

        public ARWeapon(String name){
            this.name = name;
        }

        public ARWeapon(){}

        @Override
        public void draw(Unit unit, WeaponMount mount){
            float alpha = alphaOf(unit);
            Draw.alpha(alpha);
            float z = Draw.z();
            Draw.z(z + layerOffset);

            float
            rotation = unit.rotation - 90,
            realRecoil = Mathf.pow(mount.recoil, recoilPow) * recoil,
            weaponRotation = rotation + (rotate ? mount.rotation : baseRotation),
            wx = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, -realRecoil),
            wy = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, -realRecoil);

            if(shadow > 0){
                Drawf.shadow(wx, wy, shadow);
            }

            if(top){
                drawOutline(unit, mount);
            }

            setupPartParams(mount, wx, wy, weaponRotation);
            drawWeaponParts(unit, mount, true, alpha);

            float prev = Draw.xscl;

            Draw.xscl *= -Mathf.sign(flipSprite);

            //fix color
            unit.type.applyColor(unit);
            Draw.alpha(alpha);
            if(region.found()) Draw.rect(region, wx, wy, weaponRotation);

            if(cellRegion.found()){

                Draw.color(unit.type.cellColor(unit).a(alpha));
                Draw.rect(cellRegion, wx, wy, weaponRotation);
                Draw.color();
            }

            if(heatRegion.found() && mount.heat > 0){
                Draw.alpha(alpha);
                Draw.color(heatColor, mount.heat);
                Draw.blend(Blending.additive);
                Draw.rect(heatRegion, wx, wy, weaponRotation);
                Draw.blend();
                Draw.color();
            }

            Draw.xscl = prev;
            drawWeaponParts(unit, mount, false, alpha);

            Draw.xscl = 1f;

            Draw.z(z);
        }

        @Override
        public void drawOutline(Unit unit, WeaponMount mount){
            if(!outlineRegion.found()) return;

            float alpha = alphaOf(unit);
            Draw.alpha(alpha);

            float
            rotation = unit.rotation - 90,
            realRecoil = Mathf.pow(mount.recoil, recoilPow) * recoil,
            weaponRotation = rotation + (rotate ? mount.rotation : baseRotation),
            wx = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, -realRecoil),
            wy = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, -realRecoil);

            Draw.xscl = -Mathf.sign(flipSprite);
            Draw.rect(outlineRegion, wx, wy, weaponRotation);
            Draw.xscl = 1f;
        }
    }
}
