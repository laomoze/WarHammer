package wh.net.packet;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.net.Packet;
import wh.gen.RevengeUnit;

import static mindustry.io.TypeIO.*;

public class RevengeOrbitBulletPacket extends Packet {
    public int ownerId;
    public BulletType type;
    public Team team;
    public float x;
    public float y;
    public float angle;
    public float damage;
    public float velocityScl;
    public float lifetimeScl;
    public float aimX;
    public float aimY;

    private byte[] data = NODATA;

    @Override
    public void write(Writes write) {
        write.i(ownerId);
        writeBulletType(write, type);
        writeTeam(write, team);
        write.f(x);
        write.f(y);
        write.f(angle);
        write.f(damage);
        write.f(velocityScl);
        write.f(lifetimeScl);
        write.f(aimX);
        write.f(aimY);
    }

    @Override
    public void read(Reads read, int length) {
        data = read.b(length);
    }

    @Override
    public void handled() {
        BAIS.setBytes(data);
        ownerId = READ.i();
        type = readBulletType(READ);
        team = readTeam(READ);
        x = READ.f();
        y = READ.f();
        angle = READ.f();
        damage = READ.f();
        velocityScl = READ.f();
        lifetimeScl = READ.f();
        aimX = READ.f();
        aimY = READ.f();
    }

    @Override
    public void handleClient() {
        if (type == null) return;

        Unit owner = Groups.unit.getByID(ownerId);
        if (!(owner instanceof RevengeUnit revenge)) return;

        Team spawnTeam = team == null ? revenge.team : team;
        Bullet bullet = type.create(revenge, revenge, spawnTeam, x, y, angle, damage, velocityScl, lifetimeScl, null, null, aimX, aimY, null);
        if (bullet == null) return;

        bullet.owner(revenge);
        bullet.team(spawnTeam);
        revenge.addOrbitBullet(bullet);
    }

    @Override
    public boolean allow(boolean server) {
        return !server;
    }
}

