package wh.net.packet;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.net.Packet;
import wh.gen.RevengeUnit;

import static mindustry.io.TypeIO.*;

public class RevengeOrbitCreatePacket extends Packet {
    private static final int createRetryCount = 20;
    private static final float createRetryStep = 2f;

    public int ownerId;
    public int bulletId;
    public BulletType type;
    public Team team;
    public float x;
    public float y;
    public float angle;
    public float velocityScl;
    public float aimX;
    public float aimY;

    private byte[] data = NODATA;

    @Override
    public void write(Writes write) {
        write.i(ownerId);
        write.i(bulletId);
        writeBulletType(write, type);
        writeTeam(write, team);
        write.f(x);
        write.f(y);
        write.f(angle);
        write.f(velocityScl);
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
        bulletId = READ.i();
        type = readBulletType(READ);
        team = readTeam(READ);
        x = READ.f();
        y = READ.f();
        angle = READ.f();
        velocityScl = READ.f();
        aimX = READ.f();
        aimY = READ.f();
    }

    @Override
    public void handleClient() {
        Unit owner = Groups.unit.getByID(ownerId);
        if (!(owner instanceof RevengeUnit revenge)) return;
        if (revenge.applyOrbitBulletCreate(type, team, bulletId, x, y, angle, velocityScl, aimX, aimY)) return;

        for (int i = 1; i <= createRetryCount; i++) {
            final float delay = i * createRetryStep;
            final int syncOwnerId = ownerId;
            final int syncBulletId = bulletId;
            final BulletType syncType = type;
            final Team syncTeam = team;
            final float syncX = x;
            final float syncY = y;
            final float syncAngle = angle;
            final float syncVelocityScl = velocityScl;
            final float syncAimX = aimX;
            final float syncAimY = aimY;
            Time.run(delay, () -> {
                Unit retryOwner = Groups.unit.getByID(syncOwnerId);
                if (!(retryOwner instanceof RevengeUnit retryRevenge)) return;
                retryRevenge.applyOrbitBulletCreate(syncType, syncTeam, syncBulletId, syncX, syncY, syncAngle, syncVelocityScl, syncAimX, syncAimY);
            });
        }
    }

    @Override
    public boolean allow(boolean server) {
        return !server;
    }
}
