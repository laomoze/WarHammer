package wh.entities.world.consumers;

import arc.scene.ui.layout.Table;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.MultiReqImage;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValues;
import mindustry.world.meta.Stats;

public class ConsumeAnyLiquid extends ConsumeLiquidFilter {
    public final LiquidStack[] stacks;

    public ConsumeAnyLiquid(LiquidStack... stacks) {
        super(liq -> false, stacks.length > 0 ? stacks[0].amount : 0f);
        this.stacks = stacks;
        this.filter = this::matches;
    }

    private boolean matches(Liquid liquid) {
        if (liquid == null) return false;
        for (LiquidStack stack : stacks) {
            if (stack.liquid == liquid) return true;
        }
        return false;
    }

    @Override
    public boolean consumes(Liquid liquid) {
        return matches(liquid);
    }

    public float amountFor(Liquid liquid) {
        for (LiquidStack stack : stacks) {
            if (stack.liquid == liquid) return stack.amount;
        }
        return 0f;
    }

    @Override
    public Liquid getConsumed(Building build) {
        Liquid current = build.liquids.current();
        if (current != null && consumes(current) && build.liquids.get(current) > 0f) {
            return current;
        }

        for (LiquidStack stack : stacks) {
            if (build.liquids.get(stack.liquid) > 0f) return stack.liquid;
        }
        return null;
    }

    @Override
    public void update(Building build) {
        Liquid liq = getConsumed(build);
        if (liq != null) {
            build.liquids.remove(liq, amountFor(liq) * build.edelta() * multiplier.get(build));
        }
    }

    @Override
    public float efficiency(Building build) {
        Liquid liq = getConsumed(build);
        float delta = build.edelta();
        if (liq == null || delta <= 1e-8f) return 0f;

        float need = amountFor(liq) * delta * multiplier.get(build);
        return need <= 1e-8f ? 0f : Math.min(build.liquids.get(liq) / need, 1f);
    }

    @Override
    public void build(Building build, Table table) {
        MultiReqImage image = new MultiReqImage();
        for (LiquidStack stack : stacks) {
            image.add(new ReqImage(
                    StatValues.displayLiquid(stack.liquid, stack.amount * 60f, true),
                    () -> {
                        float need = stack.amount * build.edelta() * multiplier.get(build);
                        return build.liquids.get(stack.liquid) >= need;
                    }
            ));
        }
        table.add(image).size(32f);
    }

    @Override
    public void display(Stats stats) {
        Stat stat = booster ? Stat.booster : Stat.input;
        for (LiquidStack stack : stacks) {
            stats.add(stat, StatValues.liquid(stack.liquid, stack.amount * 60f, true));
        }
    }
}
