package wh.entities.world.blocks.distribution;

import arc.math.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.blocks.heat.*;

/** 热路由器，有3个可配置输出方向（相对于旋转的左/前/右）。 */
public class ConfigurableHeatRouter extends HeatConductor{
    /** 左/前/右三个方向的开关位。 */
    public static final int leftBit = 1;
    public static final int frontBit = 1 << 1;
    public static final int rightBit = 1 << 2;
    public static final int allBits = leftBit | frontBit | rightBit;

    public ConfigurableHeatRouter(String name){
        super(name);
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;
        splitHeat = true;

        config(Integer.class, (ConfigurableHeatRouterBuild build, Integer mask) -> build.outputMask = mask & allBits);
        configClear((ConfigurableHeatRouterBuild build) -> build.outputMask = allBits);
    }

    public class ConfigurableHeatRouterBuild extends HeatConductorBuild{
        /** 当前哪些方向是开启状态（默认全开）。 */
        public int outputMask = allBits;

        @Override
        public void buildConfiguration(Table table){
            table.defaults().size(48f);

            addToggle(table, Icon.left, leftBit);
            addToggle(table, Icon.upOpen, frontBit);
            addToggle(table, Icon.right, rightBit);
        }

        protected void addToggle(Table table, Drawable icon, int bit){
            // 点一下切换这个方向：开/关。
            ImageButton button = table.button(icon, Styles.clearTogglei, () -> configure(outputMask ^ bit)).get();
            button.update(() -> button.setChecked((outputMask & bit) != 0));
        }

        @Override
        public Integer config(){
            return outputMask;
        }

        @Override
        public void updateHeat(){
            if(lastHeatUpdate == Vars.state.updateId) return;

            lastHeatUpdate = Vars.state.updateId;
            heat = calculateHeat(sideHeat, cameFrom);

            // 关闭的方向不往外传热：把对应邻居标记到 cameFrom 里。
            for(Building other : proximity){
                if(other == null || other.team != team) continue;

                int bit = directionBit(Mathf.mod(relativeTo(other), 4));
                if(bit != 0 && (outputMask & bit) == 0){
                    cameFrom.add(other.id);
                }
            }
        }

        @Override
        public float heat(){
            int sides = outputSides();
            if(sides <= 0) return 0f;
            // 引擎默认按 3 个方向平分，这里按“实际开启数量”补回来。
            return heat * 3f / sides;
        }

        @Override
        public float heatFrac(){
            return (heat / visualMaxHeat) / Math.max(outputSides(), 1);
        }

        public int outputSides(){
            return Integer.bitCount(outputMask);
        }

        /** 把方向（本块 -> 邻居）映射到左/前/右开关。 */
        protected int directionBit(int direction){
            int rel = Mathf.mod(direction - rotation, 4);

            if(rel == 1) return leftBit;
            if(rel == 0) return frontBit;
            if(rel == 3) return rightBit;
            return 0; // 后方只进，不出。
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.b(outputMask);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            // 老存档没有这个值时，默认全开。
            outputMask = revision >= 1 ? (read.ub() & allBits) : allBits;
        }
    }
}
