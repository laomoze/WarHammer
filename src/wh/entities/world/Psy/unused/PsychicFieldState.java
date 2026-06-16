package wh.entities.world.Psy.unused;

import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Syncc;
import wh.entities.WHBaseEntity;
import wh.gen.EntityRegister;

import java.nio.FloatBuffer;

public class PsychicFieldState extends WHBaseEntity implements Syncc {
    public int worldWidth;
    public int worldHeight;
    public int sampleSpacing;
    public int influenceSpacing;
    public int seed;
    public float fieldTime;
    public float targetFieldTime;
    public float rebuildTimer;
    public float transition;
    public float globalOffset;
    public float[] samples;
    public float[] targets;
    public float[] influences;

    public long lastUpdated;
    public long updateSpacing;

    @Override
    public void add() {
        super.add();
        Groups.sync.add(this);
        PsychicField.attachStateEntity(this);
    }

    @Override
    public void remove() {
        super.remove();
        Groups.sync.remove(this);
        if (PsychicField.stateEntity() == this) {
            PsychicField.attachStateEntity(null);
        }
    }

    @Override
    public void update() {
    }

    @Override
    public void draw() {
    }

    @Override
    public void write(Writes write) {
        PsychicField.copyToStateEntity(this);
        super.write(write);
        write.i(worldWidth);
        write.i(worldHeight);
        write.i(sampleSpacing);
        write.i(influenceSpacing);
        write.i(seed);
        write.f(fieldTime);
        write.f(targetFieldTime);
        write.f(rebuildTimer);
        write.f(transition);
        write.f(globalOffset);
        writeFloatArray(write, samples);
        writeFloatArray(write, targets);
        writeFloatArray(write, influences);
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        worldWidth = read.i();
        worldHeight = read.i();
        sampleSpacing = read.i();
        influenceSpacing = read.i();
        seed = read.i();
        fieldTime = read.f();
        targetFieldTime = read.f();
        rebuildTimer = read.f();
        transition = read.f();
        globalOffset = read.f();
        samples = readFloatArray(read);
        targets = readFloatArray(read);
        influences = readFloatArray(read);
        PsychicField.attachStateEntity(this);
        afterRead();
    }

    @Override
    public int classId() {
        return EntityRegister.getId(PsychicFieldState.class);
    }

    @Override
    public boolean isSyncHidden(Player player) {
        return true;
    }

    @Override
    public long lastUpdated() {
        return lastUpdated;
    }

    @Override
    public long updateSpacing() {
        return updateSpacing;
    }

    @Override
    public void afterSync() {
    }

    @Override
    public void handleSyncHidden() {
    }

    @Override
    public void interpolate() {
    }

    @Override
    public void lastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public void readSync(Reads read) {
    }

    @Override
    public void readSyncManual(FloatBuffer buffer) {
    }

    @Override
    public void snapInterpolation() {
    }

    @Override
    public void snapSync() {
    }

    @Override
    public void updateSpacing(long updateSpacing) {
        this.updateSpacing = updateSpacing;
    }

    @Override
    public void writeSync(Writes write) {
    }

    @Override
    public void writeSyncManual(FloatBuffer buffer) {
    }

    private static void writeFloatArray(Writes write, float[] values) {
        if (values == null) {
            write.i(-1);
            return;
        }

        write.i(values.length);
        for (float value : values) {
            write.f(value);
        }
    }

    private static float[] readFloatArray(Reads read) {
        int length = read.i();
        if (length < 0) return null;

        float[] values = new float[length];
        for (int i = 0; i < length; i++) {
            values[i] = read.f();
        }
        return values;
    }
}
