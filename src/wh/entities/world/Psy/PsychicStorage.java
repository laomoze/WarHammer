package wh.entities.world.Psy;

public interface PsychicStorage extends PsychicNetworkNode {
    PsychicModule psychicModule();

    float psychicCapacity();

    default float psychicStored() {
        return psychicModule().amount();
    }

    default float psychicFraction() {
        return psychicModule().fraction(psychicCapacity());
    }

    default float psychicSpace() {
        return Math.max(psychicCapacity() - psychicStored(), 0f);
    }

    default float addPsychic(float amount) {
        return psychicModule().add(amount, psychicCapacity());
    }

    default float drainPsychic(float amount) {
        return psychicModule().remove(amount);
    }

    default boolean hasPsychic(float amount) {
        return psychicModule().has(amount);
    }
}
