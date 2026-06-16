package wh.entities.world.Psy;

import arc.math.Mathf;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.modules.BlockModule;

public class PsychicModule extends BlockModule {
    private float amount;

    public float amount() {
        return amount;
    }

    public void set(float amount) {
        this.amount = Math.max(amount, 0f);
    }

    public void clear() {
        amount = 0f;
    }

    public float add(float value, float capacity) {
        if (value <= 0f) return 0f;

        float accepted = Math.min(value, Math.max(capacity - amount, 0f));
        amount += accepted;
        return accepted;
    }

    public float remove(float value) {
        if (value <= 0f) return 0f;

        float removed = Math.min(value, amount);
        amount -= removed;
        return removed;
    }

    public float clamp(float capacity) {
        amount = Mathf.clamp(amount, 0f, Math.max(capacity, 0f));
        return amount;
    }

    public float fraction(float capacity) {
        return capacity <= 0.0001f ? 0f : Mathf.clamp(amount / capacity);
    }

    public boolean has(float value) {
        return amount + 0.0001f >= value;
    }

    @Override
    public void write(Writes write) {
        write.f(amount);
    }

    @Override
    public void read(Reads read) {
        amount = Math.max(read.f(), 0f);
    }
}
