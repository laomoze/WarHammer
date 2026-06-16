package wh.entities.world.Psy;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import wh.ui.PsychicBar;

import static mindustry.Vars.tilesize;

public class PsychicValveBlock extends PsychicNode {
    public float forwardBoost = 1.2f;

    public PsychicValveBlock(String name) {
        super(name);
        rotate = true;
        configurable = true;
        saveConfig = true;
        clearOnDoubleTap = true;

        config(Integer.class, (PsychicValveBuild build, Integer packed) -> build.applyValveConfig(packed));
        configClear((PsychicValveBuild build) -> build.resetValveConfig());

        buildType = PsychicValveBuild::new;
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-valve", (PsychicValveBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-valve", build.directionText()),
                () -> psychicColor,
                () -> build.validValvePath() ? 1f : 0f
        ));
    }

    protected String directionName(int direction) {
        switch (Mathf.mod(direction, 4)) {
            case 0:
                return bundleFormat("bar.wh-psychic-direction-right");
            case 1:
                return bundleFormat("bar.wh-psychic-direction-up");
            case 2:
                return bundleFormat("bar.wh-psychic-direction-left");
            default:
                return bundleFormat("bar.wh-psychic-direction-down");
        }
    }

    public class PsychicValveBuild extends PsychicNodeBuild {
        public int inputSide = -1;
        public int outputSide = -1;
        public boolean selectingOutput;

        protected int selectedInputSide() {
            if (inputSide < 0) inputSide = Mathf.mod(rotation + 2, 4);
            return inputSide;
        }

        @Override
        protected int outputSide() {
            if (outputSide < 0) outputSide = Mathf.mod(rotation, 4);
            if (outputSide == selectedInputSide()) outputSide = Mathf.mod(inputSide + 2, 4);
            return outputSide;
        }

        public boolean validValvePath() {
            return selectedInputSide() != outputSide();
        }

        public String directionText() {
            return directionName(selectedInputSide()) + " -> " + directionName(outputSide());
        }

        public int packValveConfig() {
            return selectedInputSide() | (outputSide() << 2);
        }

        public void applyValveConfig(int packed) {
            if (packed < 0) {
                resetValveConfig();
                return;
            }

            inputSide = Mathf.mod(packed, 4);
            outputSide = Mathf.mod(packed >> 2, 4);
            if (inputSide == outputSide) {
                outputSide = Mathf.mod(inputSide + 2, 4);
            }
        }

        public void resetValveConfig() {
            inputSide = Mathf.mod(rotation + 2, 4);
            outputSide = Mathf.mod(rotation, 4);
            selectingOutput = false;
        }

        @Override
        public boolean onConfigureBuildTapped(Building other) {
            if (other == this) {
                if (!selectingOutput) deselect();
                configure(-1);
                return false;
            }

            int direction = linkedDirection(other);
            if (direction < 0) return true;

            if (!selectingOutput) {
                inputSide = direction;
                if (outputSide() == inputSide) {
                    outputSide = Mathf.mod(inputSide + 2, 4);
                }
                selectingOutput = true;
            } else if (direction != selectedInputSide()) {
                outputSide = direction;
                selectingOutput = false;
            }

            configure(packValveConfig());
            return false;
        }

        protected int linkedDirection(Building other) {
            if (!linkValid(this, other)) return -1;

            for (int i = 0; i < 4; i++) {
                if (findAnyLinkInDirection(i) == other) return i;
            }

            return -1;
        }

        @Override
        public Object config() {
            return packValveConfig();
        }

        @Override
        public int energyPriority() {
            return validValvePath() ? 2 : 0;
        }

        @Override
        public float energyTransferScale() {
            return super.energyTransferScale() * (validValvePath() ? forwardBoost : 0f);
        }

        @Override
        public void drawConfigure() {
            super.drawConfigure();

            int input = selectedInputSide();
            int output = outputSide();
            float sin = Mathf.absin(6f, 1f);
            float radius = block.size * tilesize * 0.72f;

            var in = Geometry.d4[input];
            var out = Geometry.d4[output];

            for (int i = 0; i < 4; i++) {
                Building other = i == output ? links[i] : inputLink(i);
                if (!linkValid(this, other)) continue;

                Color color = i == input ? Pal.heal : i == output ? psychicColor : Color.lightGray;
                Drawf.circles(other.x, other.y, (other.block.size / 2f + 1f) * tilesize + sin - 2f, color);
            }

            Drawf.square(x + in.x * radius, y + in.y * radius, 5f, Pal.heal);
            Drawf.square(x + out.x * radius, y + out.y * radius, 5f, psychicColor);
            Drawf.arrow(x + in.x * radius, y + in.y * radius, x + out.x * radius, y + out.y * radius, radius * 2f, 5f, psychicColor);
        }

        @Override
        protected String debugText() {
            return super.debugText() +
                    "\n" + bundleFormat("bar.wh-psychic-valve", directionText()) +
                    " | " + bundleFormat("bar.wh-psychic-transfer-scale", Strings.autoFixed(energyTransferScale() * 100f, 0));
        }

        @Override
        protected boolean isInputSide(int direction) {
            return direction == selectedInputSide();
        }

        @Override
        public byte version() {
            return 3;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.b(selectedInputSide());
            write.b(outputSide());
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            if (revision >= 3) {
                inputSide = Mathf.mod(read.b(), 4);
                outputSide = Mathf.mod(read.b(), 4);
                if (inputSide == outputSide) outputSide = Mathf.mod(inputSide + 2, 4);
            } else {
                resetValveConfig();
            }
        }
    }
}
