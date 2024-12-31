package wh.world.blocks.defense;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.io.*;
import mindustry.logic.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.draw.*;
import wh.gen.*;

import static mindustry.Vars.*;

public abstract class CommandableBlock extends Block {
    protected static final Seq<CommandableBlockBuild> participantsTmp = new Seq<>();
    public DrawBlock drawer = new DrawDefault();
    public float warmupSpeed = 0.02f;
    public float warmupFallSpeed = 0.0075f;
    public float range;
    public float reloadTime = 60;
    public float configureChargeTime = 60;
    protected int commandPos = -1;

    public CommandableBlock(String name) {
        super(name);
        timers = 4;
        update = sync = configurable = solid = true;

        config(Vec2.class, CommandableBlockBuild::commandAll);
        config(Point2.class, (CommandableBlockBuild tile, Point2 point) -> {
            tile.setTarget(point);
            commandPos = point.pack();
        });
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    @Override
    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    @Override
    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
    }

    public boolean sameGroup(Block other) {
        return equals(other);
    }

    public abstract class CommandableBlockBuild extends Building implements Ranged {
        protected final Vec2 targetVec = new Vec2().set(this);
        protected final Vec2 lastConfirmedTarget = new Vec2();

        public boolean initiateConfigure = false;
        public float configureChargeProgress = 0;

        public float reload;
        public float warmup;
        public float totalProgress;
        public BlockUnitc unit;

        public int target = -1;
        public float logicControlTime = -1;

        public int getTarget() {
            return target;
        }

        public void setTarget(Point2 point2) {
            target = point2.pack();
            target();
        }

        public abstract void command(Vec2 pos);

        public abstract void commandAll(Vec2 pos);

        public abstract boolean canCommand(Vec2 target);

        public abstract boolean isCharging();

        public abstract boolean shouldCharge();

        public boolean isChargingConfigure() {
            return initiateConfigure;
        }

        public boolean shouldChargeConfigure() {
            return configureChargeProgress < configureChargeTime && initiateConfigure;
        }

        public boolean configureChargeComplete() {
            return configureChargeProgress >= configureChargeTime && initiateConfigure;
        }

        public Vec2 target() {
            Tile tile = world.tile(target);
            return tile != null ? targetVec.set(tile) : targetVec.set(this);
        }

        @Override
        public void updateTile() {
            if (unit != null) {
                unit.health(health);
                unit.rotation(rotation);
                unit.team(team);
                unit.set(x, y);
            }

            if (shouldCharge()) {
                reload += edelta() * warmup;
            }

            if (efficiency > 0) {
                warmup = Mathf.lerpDelta(warmup, 1, warmupSpeed);
                totalProgress += warmup * edelta();
            } else warmup = Mathf.lerpDelta(warmup, 0, warmupFallSpeed);
        }

        public void updateShoot() {
        }

        public void updateControl() {
        }

        @Override
        public void created() {
            unit = (BlockUnitc) UnitTypes.block.create(team);
            unit.tile(this);
        }

        @Override
        public void read(Reads read, byte v) {
            warmup = read.f();
            reload = read.f();
            TypeIO.readVec2(read, targetVec);

            super.read(read);
        }

        @Override
        public void write(Writes write) {
            write.f(warmup);
            write.f(reload);
            TypeIO.writeVec2(write, targetVec);
        }

        @Override
        public float range() {
            return range;
        }

        @Override
        public void draw() {
            drawer.draw(this);
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float totalProgress() {
            return totalProgress;
        }

        @Override
        public float progress() {
            return 0;
        }

        @Override
        public void add() {
            if (!added) WorldRegister.commandableBuilds.add(this);

            super.add();
        }

        @Override
        public void remove() {
            if (added) WorldRegister.commandableBuilds.remove(this);

            super.remove();
        }
    }

    @SuppressWarnings("unchecked")
    public abstract class CommandEntity implements Drawc, Timedc, Teamc {
        public Cons<Teamc> act;

        public boolean added;
        public transient int id = EntityGroup.nextId();
        public transient float time, lifetime;
        public transient float x, y;
        public transient Team team;

        @Override
        public float clipSize() {
            return 500f;
        }

        @Override
        public void draw() {}

        @Override
        public void update() {
            time = Math.min(time + Time.delta, lifetime);
            if (time >= lifetime) {
                remove();
            }
        }

        @Override
        public void remove() {
            Groups.draw.remove(this);
            Groups.all.remove(this);
            added = false;
        }

        @Override
        public void add() {
            if (added) return;
            Groups.all.add(this);
            Groups.draw.add(this);
            added = true;
        }
        @Override public boolean isNull(){ return false; }
        @Override
        public boolean isLocal() {
            return this instanceof Unitc u && u.controller() == player;
        }

        @Override
        public boolean isRemote() {
            return this instanceof Unitc u && u.isPlayer() && !isLocal();
        }

        @Override
        public float fin() {
            return time / lifetime;
        }

        @Override
        public float time() {
            return time;
        }

        @Override
        public void time(float time) {
            this.time = time;
        }

        @Override
        public float lifetime() {
            return lifetime;
        }

        @Override
        public void lifetime(float lifetime) {
            this.lifetime = lifetime;
        }


        @Override
        public <T extends Entityc> T self() {
            return (T) this;
        }

        @Override
        public <T> T as() {
            return (T) this;
        }

        @Override
        public void set(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void set(Position pos) {
            set(pos.getX(), pos.getY());
        }

        @Override
        public void trns(float x, float y) {
            set(this.x + x, this.y + y);
        }

        @Override
        public void trns(Position pos) {
            trns(pos.getX(), pos.getY());
        }

        @Override
        public int tileX() {
            return 0;
        }

        @Override
        public int tileY() {
            return 0;
        }

        @Override
        public Floor floorOn() {
            return null;
        }

        @Override
        public Building buildOn() {
            return null;
        }

        @Override
        public Block blockOn() {
            return null;
        }

        @Override
        public boolean onSolid() {
            return false;
        }

        @Override
        public Tile tileOn() {
            return null;
        }

        @Override
        public float getX() {
            return x;
        }

        @Override
        public float getY() {
            return y;
        }

        @Override
        public float x() {
            return x;
        }

        @Override
        public void x(float x) {
            this.x = x;
        }

        @Override
        public float y() {
            return y;
        }

        @Override
        public void y(float y) {
            this.y = y;
        }

        @Override
        public boolean isAdded() {
            return added;
        }

        @Override
        public int classId() {
            return 1001;
        }

        @Override
        public boolean serialize() {
            return false;
        }

        @Override
        public void read(Reads read) {}

        @Override
        public void write(Writes write) {}

        @Override
        public void afterRead() {}

        @Override
        public int id() {
            return id;
        }

        @Override
        public void id(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "CommandEntity{" + "added=" + added + ", id=" + id + ", x=" + x + ", y=" + y + ", lifetime=" + lifetime + '}';
        }

        @Override
        public boolean inFogTo(Team viewer) {
            return this.team != viewer && !fogControl.isVisible(viewer, this.x, this.y);
        }

        @Override
        public boolean cheating() {
            return team.rules().cheat;
        }

        @Override
        public CoreBuild core() {
            return team.core();
        }

        @Override
        public CoreBuild closestCore() {
            return team.core();
        }

        @Override
        public CoreBuild closestEnemyCore() {
            return state.teams.closestEnemyCore(x, y, team);
        }

        @Override
        public Team team() {
            return team;
        }

        @Override
        public void team(Team team) {
            this.team = team;
        }
    }
}
