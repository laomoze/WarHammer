package wh.net.packet;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.net.Packet;
import wh.gen.GeminiUnit;

import static mindustry.io.TypeIO.*;

public class GeminiSpecialBulletPacket extends Packet {
    private static final int createRetryCount = 20;
    private static final float createRetryStep = 2f;

    public int ownerId;
    public BulletType type;
    public Team team;
    public float x;
    public float y;
    public float angle;
    public float aimX;
    public float aimY;
    public int targetId = -1;
    public float launchAngle;
    public float launchSpeed;
    public boolean forceFind;

    private byte[] data = NODATA;

    @Override
    public void write(Writes write) {
        write.i(ownerId);
        writeBulletType(write, type);
        writeTeam(write, team);
        write.f(x);
        write.f(y);
        write.f(angle);
        write.f(aimX);
        write.f(aimY);
        write.i(targetId);
        write.f(launchAngle);
        write.f(launchSpeed);
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
        type = readBulletType(READ);
        team = readTeam(READ);
        x = READ.f();
        y = READ.f();
        angle = READ.f();
        aimX = READ.f();
        aimY = READ.f();
        targetId = READ.i();
        launchAngle = READ.f();
        launchSpeed = READ.f();
        forceFind = READ.bool();
    }

    @Override
    public void handleClient() {
        Unit owner = Groups.unit.getByID(ownerId);
        if (!(owner instanceof GeminiUnit gemini)) return;
        if (gemini.applyLowHealthSpecialBulletCreate(type, team, x, y, angle, aimX, aimY, targetId, launchAngle, launchSpeed, forceFind))
            return;

        for (int i = 1; i <= createRetryCount; i++) {
            final float delay = i * createRetryStep;
            final int syncOwnerId = ownerId;
            final BulletType syncType = type;
            final Team syncTeam = team;
            final float syncX = x;
            final float syncY = y;
            final float syncAngle = angle;
            final float syncAimX = aimX;
            final float syncAimY = aimY;
            final int syncTargetId = targetId;
            final float syncLaunchAngle = launchAngle;
            final float syncLaunchSpeed = launchSpeed;
            final boolean syncForceFind = forceFind;

            Time.run(delay, () -> {
                Unit retryOwner = Groups.unit.getByID(syncOwnerId);
                if (!(retryOwner instanceof GeminiUnit retryGemini)) return;
                retryGemini.applyLowHealthSpecialBulletCreate(
                        syncType, syncTeam,
                        syncX, syncY, syncAngle,
                        syncAimX, syncAimY,
                        syncTargetId, syncLaunchAngle, syncLaunchSpeed,
                        syncForceFind
                );
            });
        }
    }

    @Override
    public boolean allow(boolean server) {
        return !server;
    }
}
