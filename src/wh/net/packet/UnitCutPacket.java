package wh.net.packet;

import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.net.Packet;
import wh.entities.cutter.UnitCutter;

/**
 * 同步单位的动态切割线。
 */
public class UnitCutPacket extends Packet {
    private static final int retryCount = 20;
    private static final float retryStep = 2f;

    public int unitId;
    public float x1, y1, x2, y2;
    private byte[] data = NODATA;

    @Override
    public void write(Writes write) {
        write.i(unitId);
        write.f(x1);
        write.f(y1);
        write.f(x2);
        write.f(y2);
    }

    @Override
    public void read(Reads read, int length) {
        data = read.b(length);
    }

    @Override
    public void handled() {
        BAIS.setBytes(data);
        unitId = READ.i();
        x1 = READ.f();
        y1 = READ.f();
        x2 = READ.f();
        y2 = READ.f();
    }

    @Override
    public void handleClient() {
        if (apply()) return;

        for (int index = 1; index <= retryCount; index++) {
            Time.run(index * retryStep, () -> {
                if (apply()) return;
            });
        }
    }

    @Override
    public boolean allow(boolean server) {
        return !server;
    }

    private boolean apply() {
        Unit unit = Groups.unit.getByID(unitId);
        if (unit == null) return false;
        UnitCutter.cutRemote(unit, x1, y1, x2, y2);
        return true;
    }
}
