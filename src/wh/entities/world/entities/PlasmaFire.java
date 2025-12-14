package wh.entities.world.entities;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.util.Time;
import arc.util.pooling.Pools;
import mindustry.core.World;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Fires;
import mindustry.entities.Puddles;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Tile;
import mindustry.world.meta.Attribute;
import wh.content.WHBullets;
import wh.content.WHStatusEffects;
import wh.gen.*;
import wh.graphics.WHPal;

import static mindustry.Vars.*;

public class PlasmaFire extends Fire{
    public float tileDamage = 25f, damageDelay = 15f;
    public static final float baseLifetime = 9 * 60f;
    public static final int frames = 80;

    protected transient float animation = (float)Mathf.random(44);
    public static final TextureRegion[] PlaRegion = new TextureRegion[frames];

    public static final Effect remove = new Effect(70f, e -> {
        Draw.alpha(e.fout());
        Draw.rect(PlaRegion[((int) (e.rotation + e.fin() * PlasmaFire.frames)) % PlasmaFire.frames], e.x + Mathf.randomSeedRange((int) e.y, 2), e.y + Mathf.randomSeedRange((int) e.x, 2));
        Drawf.light(e.x, e.y, 50f + Mathf.absin(5f, 5f), WHPal.SkyBlue, 0.6f * e.fout());
    });
@Override
    public void draw() {
        {
            if (PlaRegion[0] == null) {
                for (int i = 0; i < frames; i++) {
                    PlaRegion[i] = Core.atlas.find("wh-PlaFire-" + i);
                }
            }
            Draw.alpha(0.35f);
            Draw.alpha(Mathf.clamp(warmup / 20f));
            Draw.color(1.0F, 1.0F, 1.0F, Mathf.clamp(warmup / warmupDuration));
            Draw.z(Layer.effect);
            Draw.rect(PlaRegion[Math.min((int)animation, PlaRegion.length - 1)], x + Mathf.randomSeedRange((int)y, 2), y + Mathf.randomSeedRange((int)x, 2));
            Draw.reset();
            Drawf.light(x, y, 50.0F + Mathf.absin(5.0F, 5.0F), WHPal.SkyBlue, 0.6F * Mathf.clamp(warmup / warmupDuration));
        }
    }
    public static void create(float x, float y, Team team) {
        Tile tile = world.tile(World.toTile(x), World.toTile(y));

        if (tile != null && tile.build != null && tile.build.team != team) create(tile);
    }

    public static void createChance(Position pos, double chance) {
        if (Mathf.chanceDelta(chance)) PlasmaFire.create(pos);
    }

    public static void createChance(float x, float y, float range, float chance, Team team) {
        indexer.eachBlock(null, x, y, range, other -> other.team != team && Mathf.chanceDelta(chance), other -> PlasmaFire.create(other.tile));
    }

    public static void createChance(Teamc teamc, float range, float chance) {
        indexer.eachBlock(null, teamc.x(), teamc.y(), range, other -> other.team != teamc.team() && Mathf.chanceDelta(chance), other -> PlasmaFire.create(other.tile));
    }

    public static void create(float x, float y, float range, Team team) {
        indexer.eachBlock(null, x, y, range, other -> other.team != team, other -> PlasmaFire.create(other.tile));
    }

    public static void create(float x, float y, float range) {
        indexer.eachBlock(null, x, y, range, other -> true, other -> PlasmaFire.create(other.tile));
    }

    public static void create(Teamc teamc, float range) {
        indexer.eachBlock(null, teamc.x(), teamc.y(), range, other -> other.team != teamc.team(), other -> PlasmaFire.create(other.tile));
    }

    public static void create(Position position) {
        create(World.toTile(position.getX()), World.toTile(position.getY()));
    }

    public static void create(int x, int y) {
        create(world.tile(x, y));
    }

    public static PlasmaFire create() {
        return Pools.obtain(PlasmaFire.class, PlasmaFire::new);
    }

    public static void create(Tile tile) {
        if (net.client() || tile == null || !state.rules.fire) return;

        Fire fire = Fires.get(tile.x, tile.y);

        if (fire instanceof PlasmaFire) {
            fire.lifetime = baseLifetime;
            fire.time = 0f;
        } else {
            fire = PlasmaFire.create();
            fire.tile = tile;
            fire.lifetime = baseLifetime;
            fire.set(tile.worldx(), tile.worldy());
            fire.add();
            Fires.register(fire);
        }
    }
    @Override
    public void update() {
        animation += Time.delta / ticksPerFrame;
        warmup += Time.delta;
        animation %= frames;
        if (!headless) {
            control.sound.loop(Sounds.fire, this, 0.07F);
        }
        float speedMultiplier = 1.0F + Math.max(state.envAttrs.get(Attribute.water) * 10.0F, 0);
        time = Mathf.clamp(time + Time.delta * speedMultiplier, 0, lifetime);
        if (!net.client()) {
            Building entity = tile.build;
            boolean damage = entity != null;
            float flammability = tile.getFlammability() + puddleFlammability;
            if (!damage && flammability <= 0) {
                time += Time.delta * 8;
            }
            if (damage) {
                lifetime += Mathf.clamp(flammability / 8.0F, 0.0F, 0.6F) * Time.delta;
            }

            if (flammability > 1.0F && (spreadTimer += Time.delta * Mathf.clamp(flammability / 5.0F, 0.3F, 2.0F)) >= spreadDelay) {
                spreadTimer = 0.0F;
                Point2 p = Geometry.d4[Mathf.random(3)];
                Tile other = world.tile(tile.x + p.x, tile.y + p.y);
                PlasmaFire.create(other);
            }

            if (flammability > 0 && (fireballTimer += Time.delta * Mathf.clamp(flammability / 10.0F, 0.0F, 0.5F)) >= fireballDelay) {
                fireballTimer = 0.0F;
                WHBullets.PlasmaFireBall.createNet(Team.derelict, x, y, Mathf.random(360.0F), -1.0F, 1, 1);
            }

            if ((damageTimer += Time.delta) >= damageDelay) {
                damageTimer = 0.0F;
                Puddle p = Puddles.get(tile);
                puddleFlammability = p != null ? p.getFlammability() / 3f : 0;
                if (damage) {
                    entity.damage(tileDamage);
                }
                Damage.damageUnits(null, tile.worldx(), tile.worldy(), 8f,
                15f, (unit) -> !unit.isFlying() && !unit.isImmune(WHStatusEffects.plasmaFireBurn), (unit) -> {
                    unit.apply(WHStatusEffects.plasmaFireBurn, 300f);

                });
            }
        }
        else {
            remove();
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
        return EntityRegister.getId(PlasmaFire.class);
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

