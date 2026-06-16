package wh.ui;

import arc.util.Strings;
import mindustry.core.UI;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;

public class PsychicStatValues {

    public static void add(Stats stats, Stat stat, float amount, StatUnit unit) {
        stats.add(stat, table -> {
            table.add(new PsychicImage(amount)).padRight(4f);
            if (unit != StatUnit.none) {
                table.add(unit.localized()).left();
            }
        });
    }

    public static String format(float amount) {
        float abs = Math.abs(amount);
        if (abs >= 1000f) return UI.formatAmount((long) amount);
        if (abs >= 100f) return Strings.autoFixed(amount, amount % 1f == 0f ? 0 : 1);
        if (abs >= 1f) return Strings.autoFixed(amount, amount % 1f == 0f ? 0 : 2);
        if (abs >= 0.1f) return Strings.autoFixed(amount, 3);
        if (abs >= 0.01f) return Strings.autoFixed(amount, 4);
        return Strings.autoFixed(amount, 5);
    }
}
