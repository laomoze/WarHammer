package wh.entities.world.Psy;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import mindustry.core.Renderer;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;

import java.util.Arrays;

import static mindustry.Vars.tilesize;

public class PsychicRouterBlock extends PsychicNode {
    public PsychicRouterBlock(String name) {
        super(name);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        drawBasePlacement(x, y, rotation, valid);

        int input = outputSide(rotation);
        drawPlacementLinkPreview(x, y, rotation, input, false, Pal.place);
        for (int i = 0; i < 4; i++) {
            if (i == input) continue;
            drawPlacementLinkPreview(x, y, rotation, i, true, Pal.placing);
        }
    }

    public class PsychicRouterBuild extends PsychicNodeBuild {
        protected int inputSide() {
            return PsychicRouterBlock.this.outputSide(rotation);
        }

        protected boolean isOutputSide(int direction) {
            return direction != inputSide();
        }

        @Override
        protected boolean isInputSide(int direction) {
            return direction == inputSide();
        }

        @Override
        protected int outputSide() {
            return inputSide();
        }

        @Override
        protected boolean canReceiveFromSide(int direction) {
            return isInputSide(direction);
        }

        @Override
        protected boolean canOutputToSide(int direction) {
            return isOutputSide(direction);
        }

        @Override
        protected void refreshLinks() {
            Arrays.fill(links, null);
            Arrays.fill(dests, null);
            Arrays.fill(inputs, null);

            int input = inputSide();
            inputs[input] = findLinkInDirection(input, false);

            for (int i = 0; i < 4; i++) {
                if (!isOutputSide(i)) continue;
                links[i] = findLinkInDirection(i, true);
                dests[i] = linkTile(i, links[i]);
            }
        }

        @Override
        protected void updateBeamCache() {
            for (int i = 0; i < 4; i++) {
                if (!isOutputSide(i)) continue;

                beamFlowRate[i] = Mathf.approachDelta(beamFlowRate[i], 0f, Math.max(transferRate * 0.04f, flowParticleMinRate));
                beamPotentialDiff[i] = Mathf.approachDelta(beamPotentialDiff[i], 0f, flowParticleMinPotential * 0.04f);
                beamEffectAlpha[i] = Mathf.approachDelta(beamEffectAlpha[i], 0f, flowParticleFadeSpeed);

                if (beamEffectAlpha[i] <= PsychicNetworkNode.epsilon) {
                    beamFlowRate[i] = 0f;
                    beamPotentialDiff[i] = 0f;
                }
            }

            if (beamEffectAlpha[0] <= PsychicNetworkNode.epsilon &&
                    beamEffectAlpha[1] <= PsychicNetworkNode.epsilon &&
                    beamEffectAlpha[2] <= PsychicNetworkNode.epsilon &&
                    beamEffectAlpha[3] <= PsychicNetworkNode.epsilon) {
                beamFlowSign = 0;
            }
        }

        @Override
        protected void updateConnectionProgress() {
            for (int i = 0; i < 4; i++) {
                boolean active = isOutputSide(i)
                        ? linkValid(this, links[i]) && dests[i] != null
                        : isInputSide(i) && linkValid(this, inputs[i]);

                connection[i] = Mathf.approachDelta(
                        connection[i],
                        active ? 1f : 0f,
                        active ? connectSpeed : connectSpeed * 1.5f
                );
            }
        }

        @Override
        protected float pushOutput(float budget) {
            int active = 0;
            for (int i = 0; i < 4; i++) {
                if (!isOutputSide(i)) continue;
                Building other = links[i];
                if (other instanceof PsychicNetworkNode node && node.acceptEnergy(this)) {
                    active++;
                }
            }

            if (active == 0) return budget;

            for (int i = 0; i < 4 && budget > PsychicNetworkNode.epsilon; i++) {
                if (!isOutputSide(i)) continue;

                Building other = links[i];
                if (!(other instanceof PsychicNetworkNode node) || !node.acceptEnergy(this)) continue;

                float efficiency = linkEfficiency(i, other);
                float pressure = getEnergyPressure(node);
                float share = budget / active;
                float moved = moveEnergyTo(node, share, efficiency);
                if (moved <= PsychicNetworkNode.epsilon) continue;

                lastPush += moved;
                rememberRouterOutputFlow(i, moved, pressure);
                afterBeamTransfer(i, other, moved * efficiency, true);
                budget -= moved;
                active--;
            }
            return budget;
        }

        protected void rememberRouterOutputFlow(int direction, float moved, float pressure) {
            if (pressure <= PsychicNetworkNode.epsilon || moved <= PsychicNetworkNode.epsilon) return;
            float rate = ratePerSecond(moved);
            if (pressure < flowParticleMinPotential || rate < flowParticleMinRate) return;

            beamFlowRate[direction] = Math.max(beamFlowRate[direction], rate);
            beamPotentialDiff[direction] = Math.max(beamPotentialDiff[direction], pressure);
            beamEffectAlpha[direction] = 1f;
            beamFlowSign = 1;
        }

        @Override
        protected void updateBeamVisuals() {
            for (int i = 0; i < 4; i++) {
                if (!isOutputSide(i)) continue;

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
                if (isOutputSide(i) && linkValid(this, links[i])) count++;
                if (isInputSide(i) && linkValid(this, inputs[i])) count++;
            }
            return count;
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
                if (!isOutputSide(i)) continue;
                if (dests[i] == null || links[i] == null || connection[i] <= PsychicNetworkNode.epsilon) continue;

                drawLaser(i, (laserWidth + Mathf.absin(pulseScl, pulseMag)) * connection[i]);
                drawFlowParticles(i);
            }
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            for (int i = 0; i < 4; i++) {
                if (!isOutputSide(i)) continue;

                Building target = links[i];
                if (!linkValid(this, target)) continue;

                drawDashedLink(this, target, Pal.heal);
                Drawf.square(target.x, target.y, target.block.size * tilesize / 2f + 2.5f, 0f, Pal.heal);
            }
        }
    }
}
