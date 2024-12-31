package wh.gen;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import wh.content.*;

import static mindustry.Vars.*;
import static wh.util.WHUtils.*;

@SuppressWarnings("unused")
public class UltFire extends Fire {
    public static final float baseLifetime = 1200f;
    public static TextureRegion[] ultRegion;
    public static final Effect remove = new Effect(70f, e -> {
        Draw.alpha(e.fout());
        Draw.rect(ultRegion[((int) (e.rotation + e.fin() * Fire.frames)) % Fire.frames], e.x + Mathf.randomSeedRange((int) e.y, 2), e.y + Mathf.randomSeedRange((int) e.x, 2));
        Drawf.light(e.x, e.y, 50f + Mathf.absin(5f, 5f), Pal.techBlue, 0.6f * e.fout());
    });

    static {
        ultRegion = split("wh-ult-fire", 160, 10, 4);
    }

    public static void create(float x, float y, Team team) {
        Tile tile = world.tile(World.toTile(x), World.toTile(y));

        if (tile != null && tile.build != null && tile.build.team != team) create(tile);
    }

    public static void createChance(Position pos, double chance) {
        if (Mathf.chanceDelta(chance)) UltFire.create(pos);
    }

    public static void createChance(float x, float y, float range, float chance, Team team) {
        indexer.eachBlock(null, x, y, range, other -> other.team != team && Mathf.chanceDelta(chance), other -> UltFire.create(other.tile));
    }

    public static void createChance(Teamc teamc, float range, float chance) {
        indexer.eachBlock(null, teamc.x(), teamc.y(), range, other -> other.team != teamc.team() && Mathf.chanceDelta(chance), other -> UltFire.create(other.tile));
    }

    public static void create(float x, float y, float range, Team team) {
        indexer.eachBlock(null, x, y, range, other -> other.team != team, other -> UltFire.create(other.tile));
    }

    public static void create(float x, float y, float range) {
        indexer.eachBlock(null, x, y, range, other -> true, other -> UltFire.create(other.tile));
    }

    public static void create(Teamc teamc, float range) {
        indexer.eachBlock(null, teamc.x(), teamc.y(), range, other -> other.team != teamc.team(), other -> UltFire.create(other.tile));
    }

    public static void create(Position position) {
        create(World.toTile(position.getX()), World.toTile(position.getY()));
    }

    public static void create(int x, int y) {
        create(world.tile(x, y));
    }

    public static void create(Tile tile) {
        if (net.client() || tile == null || !state.rules.fire) return; //not clientside.

        Fire fire = Fires.get(tile.x, tile.y);

        if (!(fire instanceof UltFire)) {
            fire = UltFire.create();
            fire.tile = tile;
            fire.lifetime = baseLifetime;
            fire.set(tile.worldx(), tile.worldy());
            fire.add();
            Fires.register(fire);
        } else {
            fire.lifetime = baseLifetime;
            fire.time = 0f;
        }
    }

    public static UltFire create() {
        return Pools.obtain(UltFire.class, UltFire::new);
    }

    @Override
    public void draw() {
        Draw.alpha(0.35f);
        Draw.alpha(Mathf.clamp(warmup / 20f));
        Draw.z(110f);
        Draw.rect(ultRegion[Math.min((int) animation, ultRegion.length - 1)], x + Mathf.randomSeedRange((int) y, 2f), y + Mathf.randomSeedRange((int) x, 2f));
        Draw.reset();
        Drawf.light(x, y, 50f + Mathf.absin(5f, 5f), Pal.techBlue, 0.6f * Mathf.clamp(warmup / 20f));
    }

    @Override
    public void update() {
        animation += Time.delta / 2.25f;
        warmup += Time.delta;
        animation %= 40f;
        if (!headless) {
            control.sound.loop(Sounds.fire, this, 0.07F);
        }

        float speedMultiplier = 1f + Math.max(state.envAttrs.get(Attribute.water) * 10f, 0f);
        time = Mathf.clamp(time + Time.delta * speedMultiplier, 0f, lifetime);
        if (!net.client()) {
            if (!(time >= lifetime) && tile != null && !Float.isNaN(lifetime)) {
                Building entity = tile.build;
                boolean damage = entity != null;

                float flammability = puddleFlammability;
                if (!damage && flammability <= 0f) {
                    time += Time.delta * 8f;
                }

                if (damage) {
                    lifetime += Mathf.clamp(flammability / 16f, 0.1f, 0.5f) * Time.delta;
                }

                if (flammability > 1f && (spreadTimer += Time.delta * Mathf.clamp(flammability / 5f, 0.5f, 1f)) >= 22f) {
                    spreadTimer = 0f;
                    Point2 p = Geometry.d4[Mathf.random(3)];
                    Tile other = world.tile(tile.x + p.x, tile.y + p.y);
                    UltFire.create(other);
                }

                if (flammability > 0f && (fireballTimer += Time.delta * Mathf.clamp(flammability / 10f, 0f, 1.5f)) >= 40f) {
                    fireballTimer = 0f;
                    WHBullets.ultFireball.createNet(Team.derelict, x, y, Mathf.random(360f), 1f, 1f, 1f);
                }

                if ((damageTimer += Time.delta) >= 40f) {
                    damageTimer = 0f;
                    Puddlec p = Puddles.get(tile);
                    puddleFlammability = p != null ? p.getFlammability() / 3f : 0f;
                    if (damage) {
                        entity.damage(25f);
                    }

                    Damage.damageUnits(null, tile.worldx(), tile.worldy(), 8f, 15f, (unit) -> !unit.isFlying() && !unit.isImmune(WHStatusEffects.ultFireBurn), (unit) -> {
                        unit.apply(WHStatusEffects.ultFireBurn, 300f);
                    });
                }
            } else {
                remove();
            }
        }

        if (net.client() && !isLocal() || isRemote()) {
            interpolate();
        }

        time = Math.min(time + Time.delta, lifetime);
        if (time >= lifetime) {
            remove();
        }

    }

    @Override
    public int classId() {
        return EntityRegister.getId(UltFire.class);
    }

    @Override
    public void remove() {
        if (added) {
            Groups.all.remove(this);
            Groups.sync.remove(this);
            Groups.draw.remove(this);
            Groups.fire.remove(this);
            removeEffect();

            if (net.client()) {
                netClient.addRemovedEntity(id());
            }

            added = false;
            Groups.queueFree(this);
            Fires.remove(tile);
        }
    }

    public void removeEffect() {
        remove.at(x, y, animation);
    }
}
