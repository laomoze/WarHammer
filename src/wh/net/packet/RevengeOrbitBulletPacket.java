package wh.net.packet;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.net.Packet;
import wh.gen.RevengeUnit;

public class RevengeOrbitBulletPacket extends Packet {
    private static final int launchRetryCount = 20;
    private static final float launchRetryStep = 2f;

    public int ownerId;
    public int bulletId;
    public float launchAngle;
    public int targetId = -1;
    public boolean forceFind;

    private byte[] data = NODATA;

    @Override
    public void write(Writes write) {
        write.i(ownerId);
        write.i(bulletId);
        write.f(launchAngle);
        write.i(targetId);
        write.bool(forceFind);
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
        launchAngle = READ.f();
        targetId = READ.i();
        forceFind = READ.bool();
    }

    @Override
    public void handleClient() {
        Unit owner = Groups.unit.getByID(ownerId);
        if (!(owner instanceof RevengeUnit revenge)) return;
        if (revenge.applyOrbitLaunchEvent(bulletId, launchAngle, targetId, forceFind)) return;

        for (int i = 1; i <= launchRetryCount; i++) {
            final float delay = i * launchRetryStep;
            final int syncOwnerId = ownerId;
            final int syncBulletId = bulletId;
            final float syncLaunchAngle = launchAngle;
            final int syncTargetId = targetId;
            final boolean syncForceFind = forceFind;
            Time.run(delay, () -> {
                Unit retryOwner = Groups.unit.getByID(syncOwnerId);
                if (!(retryOwner instanceof RevengeUnit retryRevenge)) return;
                retryRevenge.applyOrbitLaunchEvent(syncBulletId, syncLaunchAngle, syncTargetId, syncForceFind);
            });
        }
    }

    @Override
    public boolean allow(boolean server) {
        return !server;
    }
}
