package wh.entities.world.Psy;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.core.Renderer;
import mindustry.gen.Building;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.input.Placement;
import mindustry.world.Edges;
import mindustry.world.Tile;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.core.WHSettings;
import wh.graphics.Drawn;
import wh.graphics.PositionLightning;
import wh.ui.PsychicStatValues;
import wh.util.WHUtils;

import java.util.Arrays;

import static mindustry.Vars.tilesize;

public class PsychicNode extends PsychicBlock {
    public int linkRange = 10;
    public float transferRate = 1.2f;
    public float distanceFalloff = 0.65f;
    public float connectSpeed = 0.08f;
    public float linkReload = 10f;

    public TextureRegion laser;
    public TextureRegion laserEnd;

    public Color laserColor1 = Color.white;
    public Color laserColor2 = Pal.sapBulletBack;
    public float pulseScl = 7f;
    public float pulseMag = 0.05f;
    public float laserWidth = 0.4f;

    public float flowMemoryBoost = 0.45f;
    public float flowMemoryGain = 0.18f;
    public float flowMemoryDecay = 0.025f;
    public float downstreamPressureBoost = 0.55f;

    public float overloadLightningChance = 0.03f;
    public float overloadLightningThreshold = 0.08f;
    public float overloadLightningRange = 8f;
    public float overloadLightningWidth = 1.35f;
    public int overloadLightningBolts = 2;

    public float flowParticleLife = 42f;
    public float flowParticleLen = 7f;
    public float flowParticleWidth = 0.9f;
    public float flowParticleMinPotential = 6f;
    public float flowParticleMinRate = 0.08f;
    public float flowParticleFadeSpeed = 0.035f;
    public int maxFlowParticles = 12;

    public PsychicNode(String name) {
        super(name);
        acceptsPsychicLinks = true;
        outputsPsychicLinks = true;
        swapDiagonalPlacement = true;
        allowDiagonal = false;
        rotate = true;
        rotateDraw = false;
        regionRotated1 = 1;
        configurable = false;
        update = true;
        sync = true;
        drawArrow = false;
        buildType = PsychicNodeBuild::new;
    }

    @Override
    public void setStats() {
        super.setStats();
        PsychicStatValues.add(stats, WHStats.psychicTransferRate, transferRate, StatUnit.perSecond);
        PsychicStatValues.add(stats, WHStats.psychicLinkRange, linkRange, StatUnit.blocks);
    }

    @Override
    public void load() {
        super.load();
        laser = Core.atlas.find(name + "-beam", Core.atlas.find("wh-psychic-laser"));
        laserEnd = Core.atlas.find(name + "-beam-end", Core.atlas.find("wh-psychic-laser-end"));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        drawPlaceDirection(x, y, rotation, outputSide(rotation), true, Pal.placing);
        for (int i = 0; i < 4; i++) {
            if (i != outputSide(rotation)) {
                drawPlaceDirection(x, y, rotation, i, false, Pal.place);
            }
        }

        drawPlaceMarkers(x, y, rotation);
    }

    protected void drawPlaceDirection(int x, int y, int rotation, int direction, boolean output, Color color) {
        int maxLen = linkRange + size / 2;
        Building dest = null;
        var dir = Geometry.d4[direction];
        int offset = size / 2;

        for (int j = 1 + offset; j <= linkRange + offset; j++) {
            Building other = Vars.world.build(x + j * dir.x, y + j * dir.y);
            if (other != null && other.isInsulated()) break;

            if (linkValidPreview(other, Vars.player.team(), output) && previewLinkMatchesDirection(direction, output, other)) {
                maxLen = j;
                dest = other;
                break;
            }
        }

        Tile target = dest == null ? Vars.world.tile(x + dir.x * maxLen, y + dir.y * maxLen) : dest.tile;
        previewEdgePoint(x, y, target, Tmp.v1);

        if (dest != null) {
            previewTargetEdgePoint(dest, x, y, Tmp.v2);
        } else {
            Tmp.v2.set(x * tilesize + dir.x * maxLen * tilesize, y * tilesize + dir.y * maxLen * tilesize);
        }

        Drawf.dashLine(color, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);

        if (dest != null) {
            Drawf.square(dest.x, dest.y, dest.block.size * tilesize / 2f + 2.5f, 0f, color);
        }
    }

    protected void previewEdgePoint(int x, int y, Tile other, Vec2 out) {
        Tile edge = other == null ? null : Edges.getFacingEdge(this, x, y, other);
        if (edge == null) {
            out.set(x * tilesize + offset, y * tilesize + offset);
        } else {
            edgePoint(edge, other, out);
        }
    }

    protected void previewTargetEdgePoint(Building dest, int x, int y, Vec2 out) {
        Tile other = Vars.world.tile(x, y);
        Tile edge = other == null ? null : Edges.getFacingEdge(dest.block, dest.tileX(), dest.tileY(), other);
        if (edge == null) {
            out.set(dest.x, dest.y);
        } else {
            edgePoint(edge, other, out);
        }
    }

    protected void edgePoint(Tile edge, Tile other, Vec2 out) {
        if (edge == null || other == null) {
            if (edge == null) {
                out.setZero();
            } else {
                out.set(edge.worldx(), edge.worldy());
            }
            return;
        }

        int dx = Integer.compare(other.x, edge.x);
        int dy = Integer.compare(other.y, edge.y);
        if (Math.abs(other.x - edge.x) >= Math.abs(other.y - edge.y)) {
            dy = 0;
        } else {
            dx = 0;
        }

        out.set(edge.worldx() + dx * tilesize / 2f, edge.worldy() + dy * tilesize / 2f);
    }

    protected void drawPlaceMarkers(int x, int y, int rotation) {
        float worldX = x * tilesize + offset;
        float worldY = y * tilesize + offset;
        float radius = size * tilesize * 0.78f;

        var out = Geometry.d4[outputSide(rotation)];
        Drawf.arrow(worldX, worldY, worldX + out.x * radius, worldY + out.y * radius, radius, 4f, Pal.placing);
        Drawf.square(worldX + out.x * radius * 0.72f, worldY + out.y * radius * 0.72f, 3.5f, 0f, Pal.placing);

        for (int i = 0; i < 4; i++) {
            if (i == outputSide(rotation)) continue;

            var dir = Geometry.d4[i];
            Drawf.arrow(worldX + dir.x * radius, worldY + dir.y * radius, worldX, worldY, radius * 0.72f, 3.5f, Pal.place);
            Drawf.square(worldX + dir.x * radius * 0.72f, worldY + dir.y * radius * 0.72f, 3f, 45f, Pal.place);
        }
    }

    protected boolean linkValidPreview(Building other, mindustry.game.Team team, boolean output) {
        if (other == null || other.team != team || !other.isAdded() || !(other instanceof PsychicNetworkNode node))
            return false;
        return output ? canReceiveLink(node) : canOutputLink(node);
    }

    protected boolean previewLinkMatchesDirection(int direction, boolean output, Building other) {
        if (!(other instanceof PsychicNodeBuild node)) return true;
        int sideFacingThis = Mathf.mod(direction + 2, 4);
        return output ? node.isInputSide(sideFacingThis) : node.outputSide() == sideFacingThis;
    }

    protected boolean linkValid(Building self, Building other) {
        return other != null &&
                other != self &&
                other.team == self.team &&
                other.isAdded() &&
                other instanceof PsychicNetworkNode;
    }

    protected boolean canReceiveLink(PsychicNetworkNode node) {
        return node != null && node.acceptEnergy(null);
    }

    protected boolean canOutputLink(PsychicNetworkNode node) {
        return node != null && node.outputEnergy();
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation) {
        Placement.calculateNodes(points, this, rotation, (point, other) ->
                Math.max(Math.abs(point.x - other.x), Math.abs(point.y - other.y)) <= linkRange + size - 1);
    }

    protected int outputSide(int rotation) {
        return Mathf.mod(rotation, 4);
    }

    public class PsychicNodeBuild extends PsychicBuild {
        public final Building[] links = new Building[4];
        public final Tile[] dests = new Tile[4];
        public final Building[] inputs = new Building[4];
        public final float[] connection = new float[4];
        public final float[] beamFlowRate = new float[4];
        public final float[] beamPotentialDiff = new float[4];
        public final float[] beamEffectAlpha = new float[4];
        public final float[] flowMemory = new float[4];
        public int lastTileChanges = -1;
        public float lastPull;
        public float lastPush;
        public byte beamFlowSign;

        @Override
        public void updateTile() {
            super.updateTile();

            updateBeamCache();
            updateFlowMemory();
            updateLinks();
            updateConnectionProgress();

            if (!enabled) {
                lastPull = 0f;
                lastPush = 0f;
                return;
            }

            lastPull = transferBudget() - pullInputs(transferBudget());
            lastPush = transferBudget() - pushOutput(transferBudget());
            updateBeamVisuals();
            updateOverloadLightning();
        }

        @Override
        public float getEnergyNeed() {
            return acceptEnergy(null) ? psychicSpace() : 0f;
        }

        @Override
        public float inputPotential() {
            return super.inputPotential();
        }

        @Override
        public float outputPotential() {
            float potential = super.outputPotential();
            Building other = links[outputSide()];
            if (other instanceof PsychicNetworkNode node) {
                potential += Math.min(node.getEnergyNeed(), psychicCapacity() * 0.25f) * downstreamPressureBoost;
            }
            return potential;
        }

        @Override
        public void draw() {
            super.draw();

            if (team == mindustry.game.Team.derelict || Mathf.zero(Renderer.laserOpacity)) return;

            int out = outputSide();
            Draw.z(Layer.power - 0.05f);

            drawInputBeams();

            if (dests[out] != null && links[out] != null && connection[out] > PsychicNetworkNode.epsilon) {
                drawLaser(out, (laserWidth + Mathf.absin(pulseScl, pulseMag)) * connection[out]);
                drawFlowParticles(out);
            }

            Draw.reset();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            int out = outputSide();
            Building target = links[out];
            if (linkValid(this, target)) {
                drawDashedLink(this, target, Pal.heal);
                Drawf.square(target.x, target.y, target.block.size * tilesize / 2f + 2.5f, 0f, Pal.heal);
            }

            for (int i = 0; i < 4; i++) {
                if (!isInputSide(i)) continue;

                Building source = inputLink(i);
                if (!linkValid(this, source)) continue;

                drawDashedLink(source, this, Pal.place);
                Drawf.square(source.x, source.y, source.block.size * tilesize / 2f + 2.5f, 0f, Pal.place);
            }

            Drawn.overlayText(debugText(), x, y, block.size * tilesize * 1.1f, psychicColor, false);
        }

        @Override
        public void pickedUp() {
            Arrays.fill(links, null);
            Arrays.fill(dests, null);
            Arrays.fill(inputs, null);
            Arrays.fill(connection, 0f);
            Arrays.fill(beamFlowRate, 0f);
            Arrays.fill(beamPotentialDiff, 0f);
            Arrays.fill(beamEffectAlpha, 0f);
            Arrays.fill(flowMemory, 0f);
            beamFlowSign = 0;
        }

        protected float transferBudget() {
            return transferRate / 60f * delta();
        }

        protected void updateBeamCache() {
            int out = outputSide();
            beamFlowRate[out] = Mathf.approachDelta(beamFlowRate[out], 0f, Math.max(transferRate * 0.04f, flowParticleMinRate));
            beamPotentialDiff[out] = Mathf.approachDelta(beamPotentialDiff[out], 0f, flowParticleMinPotential * 0.04f);
            beamEffectAlpha[out] = Mathf.approachDelta(beamEffectAlpha[out], 0f, flowParticleFadeSpeed);

            if (beamEffectAlpha[out] <= PsychicNetworkNode.epsilon) {
                beamFlowRate[out] = 0f;
                beamPotentialDiff[out] = 0f;
                beamFlowSign = 0;
            }
        }

        protected void updateFlowMemory() {
            for (int i = 0; i < 4; i++) {
                flowMemory[i] = Mathf.approachDelta(flowMemory[i], 0f, flowMemoryDecay);
            }
        }

        protected void updateLinks() {
            if (lastTileChanges != Vars.world.tileChanges || timer(0, linkReload)) {
                lastTileChanges = Vars.world.tileChanges;
                refreshLinks();
            }
        }

        protected void refreshLinks() {
            Arrays.fill(links, null);
            Arrays.fill(dests, null);
            Arrays.fill(inputs, null);

            int out = outputSide();
            links[out] = findLinkInDirection(out, true);
            dests[out] = linkTile(out, links[out]);

            for (int i = 0; i < 4; i++) {
                if (i == out) continue;
                inputs[i] = findLinkInDirection(i, false);
            }
        }

        protected Building findLinkInDirection(int direction, boolean output) {
            var dir = Geometry.d4[direction];
            int offset = size / 2;

            for (int j = 1 + offset; j <= linkRange + offset; j++) {
                Building other = Vars.world.build(tile.x + j * dir.x, tile.y + j * dir.y);
                if (other != null && other.isInsulated()) break;
                if (!linkValid(this, other)) continue;
                if (!linkMatchesDirection(direction, output, other)) continue;
                if (!output && sourceClaimedByBetterInput(other, direction)) continue;
                PsychicNetworkNode node = (PsychicNetworkNode) other;
                if (output ? canReceiveLink(node) : canOutputLink(node)) return other;
            }
            return null;
        }

        protected boolean linkMatchesDirection(int direction, boolean output, Building other) {
            if (!(other instanceof PsychicNodeBuild node)) return true;
            int sideFacingThis = Mathf.mod(direction + 2, 4);
            return output ? node.isInputSide(sideFacingThis) : node.outputSide() == sideFacingThis;
        }

        protected boolean sourceClaimedByBetterInput(Building source, int direction) {
            int myDistance = linkDistance(source);
            int max = linkRange + size / 2 + Math.max(source.block.size, size);

            for (int i = 0; i < 4; i++) {
                var dir = Geometry.d4[i];

                for (int j = 1; j <= max; j++) {
                    Building build = Vars.world.build(source.tileX() + j * dir.x, source.tileY() + j * dir.y);
                    if (build == null || build == this || build.team != team || !(build instanceof PsychicNodeBuild other) || !other.enabled)
                        continue;

                    int otherDirection = other.directionTo(source);
                    if (otherDirection == -1 || !other.isInputSide(otherDirection)) continue;
                    if (!other.canReachLink(source, otherDirection, false)) continue;

                    int otherDistance = other.linkDistance(source);
                    if (otherDistance < myDistance || (otherDistance == myDistance && other.id < id)) {
                        return true;
                    }
                }
            }

            return false;
        }

        protected boolean canReachLink(Building target, int direction, boolean output) {
            var dir = Geometry.d4[direction];
            int offset = size / 2;

            for (int j = 1 + offset; j <= linkRange + offset; j++) {
                Building other = Vars.world.build(tile.x + j * dir.x, tile.y + j * dir.y);
                if (other != null && other.isInsulated()) return false;
                if (other != target) continue;

                if (!linkValid(this, other) || !linkMatchesDirection(direction, output, other)) return false;
                PsychicNetworkNode node = (PsychicNetworkNode) other;
                return output ? canReceiveLink(node) : canOutputLink(node);
            }

            return false;
        }

        protected int directionTo(Building other) {
            int dx = other.tileX() - tile.x, dy = other.tileY() - tile.y;
            if (dx == 0 && dy != 0) return dy > 0 ? 1 : 3;
            if (dy == 0 && dx != 0) return dx > 0 ? 0 : 2;
            return -1;
        }

        protected int linkDistance(Building other) {
            return Math.max(Math.abs(other.tileX() - tile.x), Math.abs(other.tileY() - tile.y));
        }

        protected Building findAnyLinkInDirection(int direction) {
            var dir = Geometry.d4[direction];
            int offset = size / 2;

            for (int j = 1 + offset; j <= linkRange + offset; j++) {
                Building other = Vars.world.build(tile.x + j * dir.x, tile.y + j * dir.y);
                if (other != null && other.isInsulated()) break;
                if (linkValid(this, other)) return other;
            }
            return null;
        }

        protected Building inputLink(int direction) {
            return inputs[direction];
        }

        protected Tile linkTile(int direction, Building other) {
            if (other == null) return null;
            return Vars.world.tile(other.tileX(), other.tileY());
        }

        protected void updateConnectionProgress() {
            int out = outputSide();
            if (linkValid(this, links[out]) && dests[out] != null) {
                connection[out] = Mathf.approachDelta(connection[out], 1f, connectSpeed);
            } else {
                connection[out] = Mathf.approachDelta(connection[out], 0f, connectSpeed * 1.5f);
            }
        }

        protected float pullInputs(float budget) {
            for (int i = 0; i < 4 && budget > PsychicNetworkNode.epsilon && psychicSpace() > PsychicNetworkNode.epsilon; i++) {
                if (!isInputSide(i)) continue;

                Building other = inputs[i];
                if (!(other instanceof PsychicNetworkNode node) || !node.outputEnergy()) continue;

                float efficiency = linkEfficiency(i, other);
                float pressure = node.getEnergyPressure(this);
                float memoryScale = flowMemoryScale(i);
                float moved = node.moveEnergyTo(this, budget * memoryScale, efficiency);
                if (moved <= PsychicNetworkNode.epsilon) continue;

                lastPull += moved;
                rememberFlowDirection(i, moved, pressure);
                rememberInputFlow(moved, pressure);
                afterBeamTransfer(i, other, moved * efficiency, false);
                budget -= moved / memoryScale;
            }
            return budget;
        }

        protected float pushOutput(float budget) {
            int out = outputSide();
            Building other = links[out];
            if (!(other instanceof PsychicNetworkNode node) || !node.acceptEnergy(this)) return budget;

            float efficiency = linkEfficiency(out, other);
            float pressure = getEnergyPressure(node);
            float memoryScale = flowMemoryScale(out);
            float moved = moveEnergyTo(node, budget * memoryScale, efficiency);
            if (moved <= PsychicNetworkNode.epsilon) return budget;

            lastPush += moved;
            rememberFlowDirection(out, moved, pressure);
            rememberOutputFlow(moved, pressure);
            afterBeamTransfer(out, other, moved * efficiency, true);
            return budget - moved / memoryScale;
        }

        protected boolean isInputSide(int direction) {
            return direction != outputSide();
        }

        protected int outputSide() {
            return PsychicNode.this.outputSide(rotation);
        }

        protected float linkEfficiency(int direction, Building other) {
            return other instanceof PsychicNodeBuild ? 1f : buildingEfficiency(other);
        }

        protected float flowMemoryScale(int direction) {
            return 1f + flowMemory[Mathf.mod(direction, 4)] * flowMemoryBoost;
        }

        protected void rememberFlowDirection(int direction, float moved, float pressure) {
            if (moved <= PsychicNetworkNode.epsilon || pressure <= PsychicNetworkNode.epsilon) return;

            float rateScale = Mathf.clamp(ratePerSecond(moved) / Math.max(transferRate, 1f));
            float pressureScale = Mathf.clamp(pressure / 60f);
            flowMemory[Mathf.mod(direction, 4)] = Mathf.clamp(
                    flowMemory[Mathf.mod(direction, 4)] + flowMemoryGain * (0.35f + rateScale * 0.4f + pressureScale * 0.25f)
            );
        }

        protected void rememberInputFlow(float moved, float pressure) {
            if (pressure <= PsychicNetworkNode.epsilon || moved <= PsychicNetworkNode.epsilon) return;
            int out = outputSide();
            beamPotentialDiff[out] = Math.max(beamPotentialDiff[out], pressure);
        }

        protected void rememberOutputFlow(float moved, float pressure) {
            if (pressure <= PsychicNetworkNode.epsilon || moved <= PsychicNetworkNode.epsilon) return;
            float rate = ratePerSecond(moved);
            if (pressure < flowParticleMinPotential || rate < flowParticleMinRate) return;

            int out = outputSide();
            beamFlowRate[out] = Math.max(beamFlowRate[out], rate);
            beamPotentialDiff[out] = Math.max(beamPotentialDiff[out], pressure);
            beamEffectAlpha[out] = 1f;
            beamFlowSign = 1;
        }

        protected void updateBeamVisuals() {
            int out = outputSide();
            Building other = links[out];
            if (!(other instanceof PsychicNetworkNode node) || connection[out] < 0.999f) return;

            float pressure = getEnergyPressure(node);
            if (pressure <= flowParticleMinPotential) return;

            beamFlowRate[out] = Math.max(beamFlowRate[out], Math.max(flowParticleMinRate, Math.min(pressure, transferRate)));
            beamPotentialDiff[out] = Math.max(beamPotentialDiff[out], pressure);
            beamEffectAlpha[out] = 1f;
            beamFlowSign = 1;
        }

        protected void updateOverloadLightning() {
            if (overload <= overloadLightningThreshold) return;
            if (!Mathf.chanceDelta(overloadLightningChance * Mathf.clamp(overload))) return;

            float range = overloadLightningRange * (0.5f + Mathf.clamp(overload));
            float angle = Mathf.random(360f);
            Tmp.v1.trns(angle, Mathf.random(range * 0.2f, range * 0.65f)).add(x, y);
            Tmp.v2.trns(angle + Mathf.random(100f, 180f), Mathf.random(range * 0.2f, range * 0.65f)).add(x, y);
            PositionLightning.createEffect(Tmp.v1, Tmp.v2, psychicColor, overloadLightningBolts, overloadLightningWidth);
        }

        protected float buildingEfficiency(Building other) {
            float distance = Math.max(Math.abs(other.tileX() - tile.x), Math.abs(other.tileY() - tile.y));
            float normalized = Mathf.clamp((distance - 1f) / Math.max(linkRange - 1f, 1f));
            return Mathf.clamp(1f - normalized * distanceFalloff, 0.15f, 1f);
        }

        protected float beamColorPower(Building other) {
            float fraction = psychicFraction();
            if (other instanceof PsychicBuild build) {
                fraction = Math.max(fraction, build.psychicFraction());
            }
            return fraction;
        }

        protected float beamDrawPower(Building other, float pulse) {
            return (1f - beamColorPower(other)) * 0.86f + Mathf.absin(3f, pulse);
        }

        protected void afterBeamTransfer(int direction, Building other, float amount, boolean pushing) {
        }

        protected void drawInputBeams() {
            for (int i = 0; i < 4; i++) {
                if (!isInputSide(i)) continue;
                Building source = inputLink(i);
                if (!linkValid(this, source)) continue;
                if (source instanceof PsychicNodeBuild) continue;

                drawInputLaser(i, source, laserWidth + Mathf.absin(pulseScl, pulseMag));
            }
        }

        protected void drawInputLaser(int direction, Building source, float width) {
            if (!beamEnds(source, this)) return;

            float pulse = (1f - Math.max(psychicFraction(), source instanceof PsychicBuild build ? build.psychicFraction() : 0f)) * 0.86f +
                    Mathf.absin(3f, pulseMag);
            Draw.color(Tmp.c1.set(laserColor1).lerp(laserColor2, pulse).a(Renderer.laserOpacity));
            Drawf.laser(laser, laserEnd, laserEnd, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, width, true);
        }

        protected void drawLaser(int direction, float width) {
            Building other = links[direction];
            if (!linkValid(this, other) || !beamEnds(direction)) return;

            Draw.color(Tmp.c1.set(laserColor1).lerp(laserColor2, beamDrawPower(other, pulseMag)).a(Renderer.laserOpacity));
            Drawf.laser(laser, laserEnd, laserEnd, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y, width, true);
        }

        protected void drawFlowParticles(int direction) {
            if (!WHSettings.effectEnabled() || beamEffectAlpha[direction] <= PsychicNetworkNode.epsilon || beamFlowSign == 0)
                return;
            if (!beamEnds(direction)) return;

            float rate = Math.max(beamFlowRate[direction], flowParticleMinRate);
            float pressure = Math.max(beamPotentialDiff[direction], flowParticleMinPotential);
            float amountScl = Mathf.clamp(rate / Math.max(transferRate, flowParticleMinRate));
            int particles = Mathf.clamp(Mathf.ceil(maxFlowParticles * amountScl), 1, maxFlowParticles);
            float alpha = Renderer.laserOpacity * Mathf.clamp(beamEffectAlpha[direction]) *
                    Mathf.clamp(pressure / Math.max(flowParticleMinPotential * 2f, 1f));
            float angle = Angles.angle(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
            float dst = Tmp.v1.dst(Tmp.v2);
            if (dst <= flowParticleLen) return;

            Rand rand = WHUtils.rand((long) id() * 31L + direction * 9973L);

            for (int i = 0; i < particles; i++) {
                float offset = rand.random(flowParticleLife);
                float widthScl = rand.random(0.45f, 1.25f);
                float lenScl = rand.random(0.65f, 1.35f);
                float alphaScl = rand.random(0.55f, 1f);
                float fin = ((Time.time + offset) % flowParticleLife) / flowParticleLife;
                if (beamFlowSign < 0) fin = 1f - fin;
                float slope = Interp.pow2Out.apply(Mathf.slope(fin));
                float len = flowParticleLen * lenScl * (0.45f + 0.55f * slope);

                Draw.color(Tmp.c1.set(psychicColor).lerp(laserColor1, 0.35f + 0.25f * widthScl).a(alpha * alphaScl));
                Tmp.v3.set(Tmp.v1).lerp(Tmp.v2, fin);
                Tmp.v4.trns(angle, len / 2f);
                Lines.stroke(flowParticleWidth * widthScl * slope);
                Lines.line(Tmp.v3.x - Tmp.v4.x, Tmp.v3.y - Tmp.v4.y, Tmp.v3.x + Tmp.v4.x, Tmp.v3.y + Tmp.v4.y, false);
            }
            Lines.stroke(1f);
        }

        protected void drawDashedLink(Building from, Building to, Color color) {
            if (!beamEnds(from, to)) return;
            Drawf.dashLine(color, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
        }

        protected float linkDistanceScale(int direction) {
            Building other = links[direction];
            if (other == null) return 0f;

            float distance = Math.max(Math.abs(other.tileX() - tile.x), Math.abs(other.tileY() - tile.y));
            return Mathf.clamp((distance - 1f) / Math.max(linkRange - 1f, 1f));
        }

        protected boolean beamEnds(int direction) {
            return beamEnds(this, links[direction]);
        }

        protected boolean beamEnds(Building from, Building to) {
            if (from == null || to == null) return false;

            Tile fromEdge = Edges.getFacingEdge(from, to);
            Tile toEdge = Edges.getFacingEdge(to, from);
            if (fromEdge == null || toEdge == null || fromEdge == toEdge) return false;

            int dst = Math.max(Math.abs(fromEdge.x - toEdge.x), Math.abs(fromEdge.y - toEdge.y));
            if (dst <= 1) return false;

            edgePoint(fromEdge, toEdge, Tmp.v1);
            edgePoint(toEdge, fromEdge, Tmp.v2);
            return true;
        }

        protected int linkedCount() {
            int count = linkValid(this, links[outputSide()]) ? 1 : 0;
            for (Building input : inputs) {
                if (linkValid(this, input)) count++;
            }
            return count;
        }

        protected float ratePerSecond(float moved) {
            return moved * 60f / Math.max(delta(), 0.0001f);
        }

        protected String debugText() {
            return bundleFormat("bar.wh-psychic-storage",
                    Strings.autoFixed(psychicStored(), 2),
                    Strings.autoFixed(psychicCapacity(), 0)) +
                    "\n" + bundleFormat("bar.wh-psychic-links", linkedCount()) +
                    " | " + bundleFormat("bar.wh-psychic-pull", Strings.autoFixed(ratePerSecond(lastPull), 2)) +
                    "\n" + bundleFormat("bar.wh-psychic-push", Strings.autoFixed(ratePerSecond(lastPush), 2)) +
                    " | " + bundleFormat("bar.wh-psychic-overload", Strings.autoFixed(Mathf.clamp(overload) * 100f, 0)) +
                    "\n" + bundleFormat("bar.wh-psychic-disorder", Strings.autoFixed(Mathf.clamp(disorder) * 100f, 0));
        }
    }
}
