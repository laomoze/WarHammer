package wh.util;

import arc.math.*;
import arc.struct.*;
import wh.util.struct.*;

/**
 * Utility for weighted random selection.
 * Readable two-step flow:
 * 1) pick one option by weight
 * 2) run its callback
 */
public final class WeightedRandom{
    private static final Rand rand = new Rand();

    private WeightedRandom(){
    }

    public static void random(WeightedOption... options){
        WeightedOption selected = pick(options);
        if(selected != null && selected.option != null){
            selected.option.run();
        }
    }

    public static void random(Seq<WeightedOption> options){
        if(options == null || options.isEmpty()) return;
        random(options.toArray(WeightedOption.class));
    }

    public static WeightedOption pick(WeightedOption... options){
        if(options == null || options.length == 0) return null;

        float totalWeight = 0f;
        for(WeightedOption option : options){
            if(option == null) continue;
            if(option.weight <= 0f) continue;
            totalWeight += option.weight;
        }
        if(totalWeight <= 0f) return null;

        float roll = rand.nextFloat() * totalWeight;
        float cumulative = 0f;
        for(WeightedOption option : options){
            if(option == null) continue;
            if(option.weight <= 0f) continue;

            cumulative += option.weight;
            if(roll <= cumulative){
                return option;
            }
        }

        // Floating-point fallback: return last valid option.
        for(int i = options.length - 1; i >= 0; i--){
            WeightedOption option = options[i];
            if(option != null && option.weight > 0f){
                return option;
            }
        }
        return null;
    }
}
