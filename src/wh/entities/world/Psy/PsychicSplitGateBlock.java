package wh.entities.world.Psy;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Strings;
import mindustry.core.Renderer;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public class PsychicSplitGateBlock extends PsychicNode {
    public float safePressure = 20f;
    public float splitDamageThreshold = 0.999f;

    public PsychicSplitGateBlock(String name) {
        super(name);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        drawBasePlacement(x, y, rotation, valid);

        int input = Mathf.mod(rotation + 2, 4);
        int main = outputSide(rotation);
        int left = Mathf.mod(main + 1, 4);
        int right = Mathf.mod(main + 3, 4);

        drawPlacementLinkPreview(x, y, rotation, input, false, Pal.place);
        drawPlacementLinkPreview(x, y, rotation, main, true, Pal.placing);
        drawPlacementLinkPreview(x, y, rotation, left, true, Pal.heal);
        drawPlacementLinkPreview(x, y, rotation, right, true, Pal.heal);
    }

    @Override
    public void setStats() {
        super.setStats();
        PsychicStatValues.add(stats, WHStats.psychicCoverageCount, 3, StatUnit.none);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("psychic-split", (PsychicSplitGateBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-transfer-scale", Strings.autoFixed(build.sideFlowRatio() * 100f, 0)),
                () -> WHPal.PsyColor,
                build::sideFlowRatio
        ));
    }

    public class PsychicSplitGateBuild extends PsychicNodeBuild {
        protected int inputSide() {
            return Mathf.mod(rotation + 2, 4);
        }

        protected boolean isMainOutput(int direction) {
            return direction == outputSide();
        }

        protected boolean isSideOutput(int direction) {
            int left = Mathf.mod(outputSide() + 1, 4);
            int right = Mathf.mod(outputSide() + 3, 4);
            return direction == left || direction == right;
        }

        protected boolean isAnyOutput(int direction) {
            return isMainOutput(direction) || isSideOutput(direction);
        }

        protected float sideFlowRatio() {
            Building main = links[outputSide()];
            if (!(main instanceof PsychicNetworkNode node) || !node.acceptEnergy(this)) {
                return hasSideOutput() ? 1f : 0f;
            }

            float pressure = getEnergyPressure(node);
            if (pressure <= safePressure) return 0f;
            return Mathf.clamp((pressure - safePressure) / Math.max(pressure, 0.0001f));
        }

        protected boolean hasSideOutput() {
            for (int i = 0; i < 4; i++) {
                if (isSideOutput(i) && links[i] instanceof PsychicNetworkNode) return true;
            }
            return false;
        }

        @Override
        protected boolean isInputSide(int direction) {
            return direction == inputSide();
        }

        @Override
        protected boolean canReceiveFromSide(int direction) {
            return isInputSide(direction);
        }

        @Override
        protected boolean canOutputToSide(int direction) {
            return isAnyOutput(direction);
        }

        @Override
        protected void refreshLinks() {
            for (int i = 0; i < 4; i++) {
                links[i] = null;
                dests[i] = null;
                inputs[i] = null;
            }

            inputs[inputSide()] = findLinkInDirection(inputSide(), false);

            for (int i = 0; i < 4; i++) {
                if (!isAnyOutput(i)) continue;
                links[i] = findLinkInDirection(i, true);
                dests[i] = linkTile(i, links[i]);
            }
        }

        @Override
        protected void updateBeamCache() {
            for (int i = 0; i < 4; i++) {
                if (!isAnyOutput(i)) continue;

                beamFlowRate[i] = Mathf.approachDelta(beamFlowRate[i], 0f, Math.max(transferRate * 0.04f, flowParticleMinRate));
                beamPotentialDiff[i] = Mathf.approachDelta(beamPotentialDiff[i], 0f, flowParticleMinPotential * 0.04f);
                beamEffectAlpha[i] = Mathf.approachDelta(beamEffectAlpha[i], 0f, flowParticleFadeSpeed);

                if (beamEffectAlpha[i] <= PsychicNetworkNode.epsilon) {
                    beamFlowRate[i] = 0f;
                    beamPotentialDiff[i] = 0f;
                }
            }
        }

        @Override
        protected void updateConnectionProgress() {
            for (int i = 0; i < 4; i++) {
                if (!isAnyOutput(i)) continue;

                if (linkValid(this, links[i]) && dests[i] != null) {
                    connection[i] = Mathf.approachDelta(connection[i], 1f, connectSpeed);
                } else {
                    connection[i] = Mathf.approachDelta(connection[i], 0f, connectSpeed * 1.5f);
                }
            }
        }

        @Override
        protected float pushOutput(float budget) {
            int mainDirection = outputSide();
            Building main = links[mainDirection];
            boolean mainValid = main instanceof PsychicNetworkNode node && node.acceptEnergy(this);

            float sideBudget = budget * (mainValid ? sideFlowRatio() : 1f);
            float mainBudget = budget - sideBudget;

            int sideCount = 0;
            for (int i = 0; i < 4; i++) {
                if (isSideOutput(i) && links[i] instanceof PsychicNetworkNode) sideCount++;
            }

            if (sideCount > 0) {
                float eachSideBudget = sideBudget / sideCount;
                for (int i = 0; i < 4 && budget > PsychicNetworkNode.epsilon; i++) {
                    if (!isSideOutput(i)) continue;
                    budget = pushDirection(i, Math.min(eachSideBudget, budget));
                }
            }

            if (mainValid && mainBudget > PsychicNetworkNode.epsilon) {
                budget = pushDirection(mainDirection, Math.min(mainBudget, budget));
            }
            return budget;
        }

        protected float pushDirection(int direction, float budget) {
            Building other = links[direction];
            if (!(other instanceof PsychicNetworkNode node) || !node.acceptEnergy(this)) return budget;

            float efficiency = linkEfficiency(direction, other);
            float pressure = getEnergyPressure(node);
            float memoryScale = flowMemoryScale(direction);
            float moved = moveEnergyTo(node, budget * memoryScale, efficiency);
            if (moved <= PsychicNetworkNode.epsilon) return budget;

            lastPush += moved;
            rememberFlowDirection(direction, moved, pressure);
            beamFlowRate[direction] = Math.max(beamFlowRate[direction], ratePerSecond(moved));
            beamPotentialDiff[direction] = Math.max(beamPotentialDiff[direction], pressure);
            beamEffectAlpha[direction] = 1f;
            beamFlowSign = 1;
            afterBeamTransfer(direction, other, moved * efficiency, true);
            return budget - moved / memoryScale;
        }

        @Override
        protected void updateBeamVisuals() {
            for (int i = 0; i < 4; i++) {
                if (!isAnyOutput(i)) continue;
                Building other = links[i];
                if (!(other instanceof PsychicNetworkNode node) || connection[i] < 0.999f) continue;

                float pressure = getEnergyPressure(node);
                if (pressure <= flowParticleMinPotential) continue;

                beamFlowRate[i] = Math.max(beamFlowRate[i], Math.max(flowParticleMinRate, Math.min(pressure, transferRate)));
                beamPotentialDiff[i] = Math.max(beamPotentialDiff[i], pressure);
                beamEffectAlpha[i] = 1f;
                beamFlowSign = 1;
            }
        }

        @Override
        protected int linkedCount() {
            int count = 0;
            for (int i = 0; i < 4; i++) {
                if (isAnyOutput(i) && linkValid(this, links[i])) count++;
                if (i == inputSide() && linkValid(this, inputs[i])) count++;
            }
            return count;
        }

        @Override
        protected boolean shouldTakeOverloadDamage() {
            return overload >= 0.999f && sideFlowRatio() >= splitDamageThreshold && hasSideOutput();
        }

        @Override
        protected float overloadDamageSeverity() {
            return 1f;
        }

        @Override
        public void draw() {
            super.draw();

            if (team == mindustry.game.Team.derelict || Mathf.zero(Renderer.laserOpacity)) return;

            Draw.z(Layer.power - 0.05f);
            drawBeamVisuals();
            Draw.reset();
        }

        @Override
        protected void drawBeamVisuals() {
            drawInputBeams();
            drawInputFlowParticles();

            for (int i = 0; i < 4; i++) {
                if (!isAnyOutput(i)) continue;
                if (dests[i] == null || links[i] == null || connection[i] <= PsychicNetworkNode.epsilon) continue;

                drawLaser(i, (laserWidth + Mathf.absin(pulseScl, pulseMag)) * connection[i]);
                drawFlowParticles(i);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            for (int i = 0; i < 4; i++) {
                if (!isAnyOutput(i)) continue;
                Building target = links[i];
                if (!linkValid(this, target)) continue;

                drawDashedLink(this, target, isMainOutput(i) ? Pal.placing : Pal.heal);
                Drawf.square(target.x, target.y, target.block.size * tilesize / 2f + 2.5f, 0f, isMainOutput(i) ? Pal.placing : Pal.heal);
            }
        }
    }
}
