package wh.entities.world.Psy;

import arc.Core;
import arc.func.Cons2;
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
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.core.WHSettings;
import wh.graphics.PositionLightning;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;
import wh.util.WHUtils;

import java.util.Arrays;

import static mindustry.Vars.tilesize;

public class PsychicNode extends PsychicBlock {
    public int timerCheck = timers++;
    public int linkRange = 10;
    public float transferRate = 1.2f;
    public float distanceFalloff = 0.65f;
    public float connectSpeed = 0.08f;
    public float linkReload = 60f;

    public TextureRegion laser;
    public TextureRegion laserEnd;

    public Color laserColor1 = Color.white;
    public Color laserColor2 = Pal.sapBulletBack;
    public float pulseScl = 7f;
    public float pulseMag = 0.05f;
    public float laserWidth = 0.4f;

    public float downstreamPressureBoost = 0.55f;

    public float overloadFlowGain = 0.015f;
    public float overloadTransferSpeedBoost = 1.5f;

    public float overloadLightningChance = 0.03f;
    public float overloadLightningThreshold = 0.2f;
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
    }

    @Override
    public void setStats() {
        super.setStats();
        PsychicStatValues.add(stats, WHStats.psychicTransferRate, transferRate, StatUnit.perSecond);
        PsychicStatValues.add(stats, WHStats.psychicLinkRange, linkRange, StatUnit.blocks);
        stats.add(Stat.output, table -> table.add(bundleFormat("bar.wh-psychic-budget", Strings.autoFixed(transferRate, 2))));
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-flow", (PsychicNodeBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-budget", Strings.autoFixed(build.displayFlowRate(), 2)),
                () -> laserColor1,
                () -> transferRate <= 0.0001f ? 0f : Mathf.clamp(build.displayFlowRate() / transferRate)
        ));

        addBar("psychic-overload", (PsychicNodeBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-overload", Strings.autoFixed(Mathf.clamp(build.overload) * 100f, 0)),
                () -> overloadColor,
                () -> Mathf.clamp(build.overload)
        ));
    }

    @Override
    public void load() {
        super.load();
        laser = Core.atlas.find(name + "-beam", Core.atlas.find("wh-psychic-laser"));
        laserEnd = Core.atlas.find(name + "-beam-end", Core.atlas.find("wh-psychic-laser-end"));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        drawBasePlacement(x, y, rotation, valid);
        drawDefaultNodePlacementPreview(x, y, rotation);
    }

    /**
     * 绘制方块通用的放置底图，子类可复用后叠加自己的方向预览。
     */
    protected void drawBasePlacement(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
    }

    /**
     * 绘制普通灵能节点的默认放置预览：一侧输出，其余三侧输入。
     */
    protected void drawDefaultNodePlacementPreview(int x, int y, int rotation) {
        drawPlacementLinkPreview(x, y, rotation, outputSide(rotation), true, Pal.placing);
        for (int i = 0; i < 4; i++) {
            if (i != outputSide(rotation)) {
                drawPlacementLinkPreview(x, y, rotation, i, false, Pal.place);
            }
        }
    }

    /**
     * 沿指定方向绘制一条放置期的连接预览线，并在可连接目标上标出框选。
     */
    protected void drawPlacementLinkPreview(int x, int y, int rotation, int direction, boolean output, Color color) {
        int maxLen = linkRange + size / 2;
        var dir = Geometry.d4[direction];
        Building dest = findPlacementLinkInDirection(x, y, direction, output, Vars.player == null ? null : Vars.player.team());

        Tile target = dest == null ? Vars.world.tile(x + dir.x * maxLen, y + dir.y * maxLen) : dest.tile;

        if (dest != null) {
            boolean[] drew = {false};
            eachPlacementAlignedBeamTilePair(x, y, dest, direction, Mathf.mod(direction + 2, 4), (fromTile, toTile) -> {
                drew[0] = true;
                resolveTileEdgePoint(fromTile, direction, Tmp.v1);
                resolveTileEdgePoint(toTile, Mathf.mod(direction + 2, 4), Tmp.v2);
                Drawf.dashLine(color, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
            });

            if (!drew[0]) {
                resolvePlacementSourceEdgePoint(x, y, direction, target, Tmp.v1);
                resolvePlacementTargetEdgePoint(dest, x, y, Mathf.mod(direction + 2, 4), Tmp.v2);
                Drawf.dashLine(color, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
            }
        } else {
            resolvePlacementSourceEdgePoint(x, y, direction, target, Tmp.v1);
            Tmp.v2.set(x * tilesize + dir.x * maxLen * tilesize, y * tilesize + dir.y * maxLen * tilesize);
            Drawf.dashLine(color, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
        }

        if (dest != null) {
            Drawf.square(dest.x, dest.y, dest.block.size * tilesize / 2f + 2.5f, 0f, color);
        }
    }

    protected Building findPlacementLinkInDirection(int sourceX, int sourceY, int direction, boolean output, mindustry.game.Team team) {
        if (team == null) return null;

        Point2 dir = Geometry.d4[direction];
        boolean horizontal = direction == 0 || direction == 2;
        int sourceFixed = horizontal
                ? (direction == 0 ? maxPlacementTileOnAxis(sourceX, size) : minPlacementTileOnAxis(sourceX, size))
                : (direction == 1 ? maxPlacementTileOnAxis(sourceY, size) : minPlacementTileOnAxis(sourceY, size));
        int axisMin = horizontal ? minPlacementTileOnAxis(sourceY, size) : minPlacementTileOnAxis(sourceX, size);
        int axisMax = horizontal ? maxPlacementTileOnAxis(sourceY, size) : maxPlacementTileOnAxis(sourceX, size);

        boolean[] blocked = new boolean[Math.max(0, axisMax - axisMin + 1)];

        for (int dist = 1; dist <= linkRange; dist++) {
            Building best = null;
            float bestAxisDistance = Float.MAX_VALUE;
            boolean anyOpenLane = false;

            for (int axis = axisMin; axis <= axisMax; axis++) {
                int index = axis - axisMin;
                if (blocked[index]) continue;
                anyOpenLane = true;

                int tx = horizontal ? sourceFixed + dir.x * dist : axis;
                int ty = horizontal ? axis : sourceFixed + dir.y * dist;

                Building other = Vars.world.build(tx, ty);
                if (other == null) continue;
                if (other.isInsulated()) {
                    blocked[index] = true;
                    continue;
                }
                if (!linkValidPreview(other, team, output)) continue;
                if (!previewLinkMatchesDirection(direction, output, other)) {
                    blocked[index] = true;
                    continue;
                }

                float axisDistance = horizontal ? Math.abs(other.y - sourceY) : Math.abs(other.x - sourceX);
                if (best == null || axisDistance < bestAxisDistance) {
                    best = other;
                    bestAxisDistance = axisDistance;
                }

                blocked[index] = true;
            }

            if (best != null) return best;
            if (!anyOpenLane) break;
        }
        return null;
    }

    /**
     * 求放置中本方朝向目标的边缘发射点。
     */
    protected void resolvePlacementSourceEdgePoint(int x, int y, int direction, Tile other, Vec2 out) {
        if (other == null) {
            resolveBlockSidePoint(x * tilesize + offset, y * tilesize + offset, size, direction, out);
            return;
        }

        Tile edge = Edges.getFacingEdge(this, x, y, other);
        resolveTileEdgePoint(edge, direction, out);
    }

    /**
     * 求放置中目标方块朝向本方的边缘接收点。
     */
    protected void resolvePlacementTargetEdgePoint(Building dest, int sourceX, int sourceY, int direction, Vec2 out) {
        Tile source = Vars.world.tile(sourceX, sourceY);
        Tile edge = source == null ? dest.tile : Edges.getFacingEdge(dest.block, dest.tileX(), dest.tileY(), source);
        resolveTileEdgePoint(edge, direction, out);
    }

    /**
     * 根据方块中心、尺寸和方向，求这一侧正中的边缘点。
     */
    protected void resolveBlockSidePoint(float centerX, float centerY, int blockSize, int direction, Vec2 out) {
        Point2 dir = Geometry.d4[Mathf.mod(direction, 4)];
        float half = blockSize * tilesize / 2f;
        out.set(centerX + dir.x * half, centerY + dir.y * half);
    }

    /**
     * 根据边缘 tile 和方向，求该 tile 外侧的连线端点。
     */
    protected void resolveTileEdgePoint(Tile edge, int direction, Vec2 out) {
        if (edge == null) {
            out.setZero();
            return;
        }

        Point2 dir = Geometry.d4[Mathf.mod(direction, 4)];
        out.set(edge.worldx() + dir.x * tilesize / 2f, edge.worldy() + dir.y * tilesize / 2f);
    }

    /**
     * 按目标向量主轴方向，求更贴近目标侧的边缘点。
     */
    protected void resolveAxisAlignedSidePoint(float centerX, float centerY, int blockSize, Vec2 toward, Vec2 out) {
        float half = blockSize * tilesize / 2f;
        if (Math.abs(toward.x) >= Math.abs(toward.y)) {
            out.set(centerX + Mathf.sign(toward.x) * half, centerY);
        } else {
            out.set(centerX, centerY + Mathf.sign(toward.y) * half);
        }
    }

    protected int minPlacementTileOnAxis(int centerTile, int blockSize) {
        return centerTile - (blockSize - 1) / 2;
    }

    protected int maxPlacementTileOnAxis(int centerTile, int blockSize) {
        return centerTile + blockSize / 2;
    }

    protected int minBuildTileOnAxis(Building build, boolean xAxis) {
        return (xAxis ? build.tileX() : build.tileY()) - (build.block.size - 1) / 2;
    }

    protected int maxBuildTileOnAxis(Building build, boolean xAxis) {
        return (xAxis ? build.tileX() : build.tileY()) + build.block.size / 2;
    }

    protected void eachPlacementAlignedBeamTilePair(int sourceX, int sourceY, Building dest, int fromDirection, int toDirection, Cons2<Tile, Tile> consumer) {
        if (dest == null) return;

        boolean horizontal = fromDirection == 0 || fromDirection == 2;
        int sourceFixed = horizontal
                ? (fromDirection == 0 ? maxPlacementTileOnAxis(sourceX, size) : minPlacementTileOnAxis(sourceX, size))
                : (fromDirection == 1 ? maxPlacementTileOnAxis(sourceY, size) : minPlacementTileOnAxis(sourceY, size));
        int destFixed = horizontal
                ? (toDirection == 0 ? maxBuildTileOnAxis(dest, true) : minBuildTileOnAxis(dest, true))
                : (toDirection == 1 ? maxBuildTileOnAxis(dest, false) : minBuildTileOnAxis(dest, false));

        int sourceMin = horizontal ? minPlacementTileOnAxis(sourceY, size) : minPlacementTileOnAxis(sourceX, size);
        int sourceMax = horizontal ? maxPlacementTileOnAxis(sourceY, size) : maxPlacementTileOnAxis(sourceX, size);
        int destMin = horizontal ? minBuildTileOnAxis(dest, false) : minBuildTileOnAxis(dest, true);
        int destMax = horizontal ? maxBuildTileOnAxis(dest, false) : maxBuildTileOnAxis(dest, true);

        int overlapMin = Math.max(sourceMin, destMin);
        int overlapMax = Math.min(sourceMax, destMax);

        if (overlapMin <= overlapMax) {
            for (int axis = overlapMin; axis <= overlapMax; axis++) {
                Tile sourceTile = horizontal ? Vars.world.tile(sourceFixed, axis) : Vars.world.tile(axis, sourceFixed);
                Tile destTile = horizontal ? Vars.world.tile(destFixed, axis) : Vars.world.tile(axis, destFixed);
                if (sourceTile != null && destTile != null) {
                    consumer.get(sourceTile, destTile);
                }
            }
            return;
        }

        Tile sourceEdge = Vars.world.tile(sourceX, sourceY);
        Tile destEdge = sourceEdge == null ? dest.tile : Edges.getFacingEdge(dest.block, dest.tileX(), dest.tileY(), sourceEdge);
        if (destEdge == null) return;

        Tile placementEdge = Edges.getFacingEdge(this, sourceX, sourceY, destEdge);
        if (placementEdge != null) {
            consumer.get(placementEdge, destEdge);
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
        return output ? node.canReceiveFromSide(sideFacingThis) : node.canOutputToSide(sideFacingThis);
    }

    protected boolean linkValid(Building self, Building other) {
        return other != null &&
                other != self &&
                other.team == self.team &&
                other.isAdded() &&
                other instanceof PsychicNetworkNode;
    }

    protected boolean canReceiveLink(PsychicNetworkNode node) {
        return node != null && node.acceptsPsychicLinks();
    }

    protected boolean canOutputLink(PsychicNetworkNode node) {
        return node != null && node.outputsPsychicLinks();
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
        public Building[] links = new Building[4];
        public Tile[] dests = new Tile[4];
        public Building[] inputs = new Building[4];
        public float[] connection = new float[4];
        public float[] beamFlowRate = new float[4];
        public float[] beamPotentialDiff = new float[4];
        public float[] beamEffectAlpha = new float[4];
        public int lastTileChanges = -1;
        public float lastPull;
        public float lastPush;
        public float displayFlowRate;
        public byte beamFlowSign;

        /**
         * 获取方块在 x 或 y 轴上所覆盖的最小 tile 坐标。
         */
        protected int minTileOnAxis(Building build, boolean xAxis) {
            return (xAxis ? build.tileX() : build.tileY()) - (build.block.size - 1) / 2;
        }

        /**
         * 获取方块在 x 或 y 轴上所覆盖的最大 tile 坐标。
         */
        protected int maxTileOnAxis(Building build, boolean xAxis) {
            return (xAxis ? build.tileX() : build.tileY()) + build.block.size / 2;
        }

        /**
         * 枚举两块在相对边缘上真正重合的 tile 对。
         * 如果没有重合边缘，就退回到 facing edge 的单对连接。
         */
        protected void eachAlignedBeamTilePair(Building from, Building to, int fromDirection, int toDirection, Cons2<Tile, Tile> consumer) {
            if (from == null || to == null) return;

            boolean horizontal = fromDirection == 0 || fromDirection == 2;
            int fromFixed = horizontal ? (fromDirection == 0 ? maxTileOnAxis(from, true) : minTileOnAxis(from, true))
                    : (fromDirection == 1 ? maxTileOnAxis(from, false) : minTileOnAxis(from, false));
            int toFixed = horizontal ? (toDirection == 0 ? maxTileOnAxis(to, true) : minTileOnAxis(to, true))
                    : (toDirection == 1 ? maxTileOnAxis(to, false) : minTileOnAxis(to, false));

            int fromMin = horizontal ? minTileOnAxis(from, false) : minTileOnAxis(from, true);
            int fromMax = horizontal ? maxTileOnAxis(from, false) : maxTileOnAxis(from, true);
            int toMin = horizontal ? minTileOnAxis(to, false) : minTileOnAxis(to, true);
            int toMax = horizontal ? maxTileOnAxis(to, false) : maxTileOnAxis(to, true);

            int overlapMin = Math.max(fromMin, toMin);
            int overlapMax = Math.min(fromMax, toMax);

            if (overlapMin <= overlapMax) {
                for (int axis = overlapMin; axis <= overlapMax; axis++) {
                    Tile fromTile = horizontal ? Vars.world.tile(fromFixed, axis) : Vars.world.tile(axis, fromFixed);
                    Tile toTile = horizontal ? Vars.world.tile(toFixed, axis) : Vars.world.tile(axis, toFixed);
                    if (fromTile != null && toTile != null) {
                        consumer.get(fromTile, toTile);
                    }
                }
                return;
            }

            Tile fromEdge = Edges.getFacingEdge(from, to);
            Tile toEdge = Edges.getFacingEdge(to, from);
            if (fromEdge != null && toEdge != null) {
                consumer.get(fromEdge, toEdge);
            }
        }

        /**
         * 选出一对代表性的主连线端点，供粒子和方向判断复用。
         */
        protected boolean resolvePrimaryBeamEndpoints(Building from, Building to, int fromDirection, int toDirection) {
            final Tile[] bestFrom = {null};
            final Tile[] bestTo = {null};
            final float[] bestDistance = {Float.MAX_VALUE};
            boolean horizontal = fromDirection == 0 || fromDirection == 2;
            float centerAxis = horizontal ? (from.y + to.y) * 0.5f : (from.x + to.x) * 0.5f;

            Tmp.v1.setZero();
            Tmp.v2.setZero();

            eachAlignedBeamTilePair(from, to, fromDirection, toDirection, (fromTile, toTile) -> {
                float axis = horizontal ? (fromTile.worldy() + toTile.worldy()) * 0.5f : (fromTile.worldx() + toTile.worldx()) * 0.5f;
                float distance = Math.abs(axis - centerAxis);
                if (distance < bestDistance[0]) {
                    bestDistance[0] = distance;
                    bestFrom[0] = fromTile;
                    bestTo[0] = toTile;
                }
            });

            if (bestFrom[0] == null) return false;

            resolveTileEdgePoint(bestFrom[0], fromDirection, Tmp.v1);
            resolveTileEdgePoint(bestTo[0], toDirection, Tmp.v2);
            return true;
        }

        @Override
        public void updateTile() {
            super.updateTile();

            updateLinks();
            updateBeamCache();
            updateConnectionProgress();

            if (!enabled) {
                lastPull = 0f;
                lastPush = 0f;
                displayFlowRate = Mathf.lerpDelta(displayFlowRate, 0f, 0.08f);
                return;
            }

            lastPull = transferBudget() - pullInputs(transferBudget());
            lastPush = transferBudget() - pushOutput(transferBudget());
            updateTransferOverload();
            displayFlowRate = Mathf.lerpDelta(displayFlowRate, ratePerSecond(Math.max(lastPull, lastPush)), 0.12f);
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

            Draw.z(Layer.power - 0.05f);
            drawBeamVisuals();
            Draw.reset();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            int out = outputSide();
            Building target = links[out];
            if (linkValid(this, target)) {
                drawDashedLink(this, target, out, Mathf.mod(out + 2, 4), Pal.heal);
                Drawf.square(target.x, target.y, target.block.size * tilesize / 2f + 2.5f, 0f, Pal.heal);
            }

            for (int i = 0; i < 4; i++) {
                if (!isInputSide(i)) continue;

                Building source = inputLink(i);
                if (!linkValid(this, source)) continue;

                drawDashedLink(source, this, Mathf.mod(i + 2, 4), i, Pal.place);
                Drawf.square(source.x, source.y, source.block.size * tilesize / 2f + 2.5f, 0f, Pal.place);
            }

            drawSelectText(
                    bundleFormat("bar.wh-psychic-storage",
                            Strings.autoFixed(psychicStored(), 2),
                            Strings.autoFixed(psychicCapacity(), 0)),
                    bundleFormat("bar.wh-psychic-links", linkedCount()),
                    bundleFormat("bar.wh-psychic-budget", Strings.autoFixed(displayFlowRate, 2)),
                    bundleFormat("bar.wh-psychic-overload", Strings.autoFixed(Mathf.clamp(overload) * 100f, 0))
            );
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
            displayFlowRate = 0f;
            beamFlowSign = 0;
        }

        @Override
        public void placed() {
            super.placed();
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
        }


        protected float transferBudget() {
            float overloadSpeed = 1f + Mathf.clamp(overload) * overloadTransferSpeedBoost;
            return transferRate / 60f * delta() * overloadSpeed;
        }

        protected void updateTransferOverload() {
            if (transferRate <= PsychicNetworkNode.epsilon) return;

            float currentRate = ratePerSecond(Math.max(lastPull, lastPush));
            float excess = Mathf.clamp(currentRate / transferRate - 1f);
            if (excess > PsychicNetworkNode.epsilon) {
                addPsychicOverload(excess * overloadFlowGain / 60f * delta());
            }
        }

        public float displayFlowRate() {
            return displayFlowRate;
        }

        @Override
        public float warmup() {
            float linkWarmup = 0f;
            for (int i = 0; i < 4; i++) {
                linkWarmup = Math.max(linkWarmup, connection[i]);
            }

            float flowWarmup = transferRate <= 0.0001f ? 0f : Mathf.clamp(displayFlowRate / transferRate);
            return Mathf.clamp(Math.max(super.warmup(), Math.max(linkWarmup, flowWarmup)));
        }

        @Override
        public float progress() {
            return transferRate <= 0.0001f ? 0f : Mathf.clamp(displayFlowRate / transferRate);
        }

        protected void updateBeamCache() {
            boolean active = false;
            for (int i = 0; i < 4; i++) {
                beamFlowRate[i] = Mathf.approachDelta(beamFlowRate[i], 0f, Math.max(transferRate * 0.04f, flowParticleMinRate));
                beamPotentialDiff[i] = Mathf.approachDelta(beamPotentialDiff[i], 0f, flowParticleMinPotential * 0.04f);
                beamEffectAlpha[i] = Mathf.approachDelta(beamEffectAlpha[i], 0f, flowParticleFadeSpeed);

                if (beamEffectAlpha[i] <= PsychicNetworkNode.epsilon) {
                    beamFlowRate[i] = 0f;
                    beamPotentialDiff[i] = 0f;
                } else {
                    active = true;
                }
            }

            if (!active) beamFlowSign = 0;
        }

        protected void updateLinks() {
            if (timer.get(timerCheck, linkReload) || lastTileChanges != Vars.world.tileChanges) {
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
            Point2 dir = Geometry.d4[direction];
            boolean horizontal = direction == 0 || direction == 2;
            int fixed = horizontal
                    ? (direction == 0 ? maxTileOnAxis(this, true) : minTileOnAxis(this, true))
                    : (direction == 1 ? maxTileOnAxis(this, false) : minTileOnAxis(this, false));
            int axisMin = horizontal ? minTileOnAxis(this, false) : minTileOnAxis(this, true);
            int axisMax = horizontal ? maxTileOnAxis(this, false) : maxTileOnAxis(this, true);
            float axisCenter = horizontal ? y : x;

            boolean[] blocked = new boolean[Math.max(0, axisMax - axisMin + 1)];

            for (int dist = 1; dist <= linkRange; dist++) {
                Building best = null;
                float bestAxisDistance = Float.MAX_VALUE;
                boolean anyOpenLane = false;

                for (int axis = axisMin; axis <= axisMax; axis++) {
                    int index = axis - axisMin;
                    if (blocked[index]) continue;
                    anyOpenLane = true;

                    int tx = horizontal ? fixed + dir.x * dist : axis;
                    int ty = horizontal ? axis : fixed + dir.y * dist;

                    Building other = Vars.world.build(tx, ty);
                    if (other == null) continue;
                    if (other.isInsulated()) {
                        blocked[index] = true;
                        continue;
                    }
                    if (!linkValid(this, other)) continue;
                    if (!linkMatchesDirection(direction, output, other)) {
                        blocked[index] = true;
                        continue;
                    }

                    PsychicNetworkNode node = (PsychicNetworkNode) other;
                    if (!(output ? canReceiveLink(node) : canOutputLink(node))) {
                        blocked[index] = true;
                        continue;
                    }

                    float axisDistance = horizontal ? Math.abs(other.y - axisCenter) : Math.abs(other.x - axisCenter);
                    if (best == null || axisDistance < bestAxisDistance) {
                        best = other;
                        bestAxisDistance = axisDistance;
                    }

                    blocked[index] = true;
                }

                if (best != null) return best;
                if (!anyOpenLane) break;
            }
            return null;
        }

        protected boolean linkMatchesDirection(int direction, boolean output, Building other) {
            if (!(other instanceof PsychicNodeBuild node)) return true;
            int sideFacingThis = Mathf.mod(direction + 2, 4);
            return output ? node.canReceiveFromSide(sideFacingThis) : node.canOutputToSide(sideFacingThis);
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
                float moved = node.moveEnergyTo(this, budget, efficiency);
                if (moved <= PsychicNetworkNode.epsilon) continue;

                lastPull += moved;
                rememberInputFlow(i, moved, pressure);
                afterBeamTransfer(i, other, moved * efficiency, false);
                budget -= moved;
            }
            return budget;
        }

        protected float pushOutput(float budget) {
            int out = outputSide();
            Building other = links[out];
            if (!(other instanceof PsychicNetworkNode node) || !node.acceptEnergy(this)) return budget;

            float efficiency = linkEfficiency(out, other);
            float pressure = getEnergyPressure(node);
            float moved = moveEnergyTo(node, budget, efficiency);
            if (moved <= PsychicNetworkNode.epsilon) return budget;

            lastPush += moved;
            rememberOutputFlow(moved, pressure);
            afterBeamTransfer(out, other, moved * efficiency, true);
            return budget - moved;
        }

        protected boolean isInputSide(int direction) {
            return direction != outputSide();
        }

        protected int outputSide() {
            return PsychicNode.this.outputSide(rotation);
        }

        protected boolean canReceiveFromSide(int direction) {
            return isInputSide(direction);
        }

        protected boolean canOutputToSide(int direction) {
            return direction == outputSide();
        }

        protected float linkEfficiency(int direction, Building other) {
            return other instanceof PsychicNodeBuild ? 1f : buildingEfficiency(other);
        }

        protected void rememberInputFlow(int direction, float moved, float pressure) {
            if (pressure <= PsychicNetworkNode.epsilon || moved <= PsychicNetworkNode.epsilon) return;
            float rate = ratePerSecond(moved);
            beamFlowRate[direction] = Math.max(beamFlowRate[direction], rate);
            beamPotentialDiff[direction] = Math.max(beamPotentialDiff[direction], pressure);
            beamEffectAlpha[direction] = 1f;
            beamFlowSign = 1;
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
            if (!(other instanceof PsychicNetworkNode node) || !linkValid(this, other) || dests[out] == null) return;

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
            PositionLightning.createEffect(Tmp.v1, Tmp.v2, WHPal.PsyColor, overloadLightningBolts, overloadLightningWidth);
        }

        protected void drawBeamVisuals() {
            int out = outputSide();

            drawInputBeams();
            drawInputFlowParticles();

            if (linkValid(this, links[out]) && dests[out] != null && connection[out] > PsychicNetworkNode.epsilon) {
                drawLaser(out, (laserWidth + Mathf.absin(pulseScl, pulseMag)) * connection[out]);
                drawFlowParticles(out);
            }
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
            if (amount <= PsychicNetworkNode.epsilon || !WHSettings.effectEnabled()) return;

            float normalized = Mathf.clamp(ratePerSecond(amount) / Math.max(transferRate, 0.0001f));
            if (!Mathf.chanceDelta(overloadLightningChance * normalized)) return;

            Building from = pushing ? this : other;
            Building to = pushing ? other : this;
            int fromDirection = pushing ? direction : Mathf.mod(direction + 2, 4);
            int toDirection = pushing ? Mathf.mod(direction + 2, 4) : direction;

            if (!resolvePrimaryBeamEndpoints(from, to, fromDirection, toDirection)) return;

            Vec2 fromPoint = new Vec2(Tmp.v1);
            Vec2 toPoint = new Vec2(Tmp.v2);
            PositionLightning.createEffect(fromPoint, toPoint, WHPal.PsyColor, Math.max(1, overloadLightningBolts - 1), Math.max(0.9f, overloadLightningWidth * 0.8f));
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

        protected void drawInputFlowParticles() {
            for (int i = 0; i < 4; i++) {
                if (!isInputSide(i)) continue;

                Building source = inputLink(i);
                if (!linkValid(this, source)) continue;

                drawFlowParticles(i, source, this, Mathf.mod(i + 2, 4), i);
            }
        }

        protected void drawInputLaser(int direction, Building source, float width) {
            if (!resolvePrimaryBeamEndpoints(source, this, Mathf.mod(direction + 2, 4), direction)) return;

            float pulse = (1f - Math.max(psychicFraction(), source instanceof PsychicBuild build ? build.psychicFraction() : 0f)) * 0.86f +
                    Mathf.absin(3f, pulseMag);
            Draw.color(Tmp.c1.set(laserColor1).lerp(laserColor2, pulse).a(Renderer.laserOpacity));
            eachAlignedBeamTilePair(source, this, Mathf.mod(direction + 2, 4), direction, (fromTile, toTile) -> {
                resolveTileEdgePoint(fromTile, Mathf.mod(direction + 2, 4), Tmp.v3);
                resolveTileEdgePoint(toTile, direction, Tmp.v4);
                Drawf.laser(laser, laserEnd, laserEnd, Tmp.v3.x, Tmp.v3.y, Tmp.v4.x, Tmp.v4.y, width, true);
            });
        }

        protected void drawLaser(int direction, float width) {
            Building other = links[direction];
            if (!linkValid(this, other) || !resolvePrimaryBeamEndpoints(this, other, direction, Mathf.mod(direction + 2, 4)))
                return;

            Draw.color(Tmp.c1.set(laserColor1).lerp(laserColor2, beamDrawPower(other, pulseMag)).a(Renderer.laserOpacity));
            eachAlignedBeamTilePair(this, other, direction, Mathf.mod(direction + 2, 4), (fromTile, toTile) -> {
                resolveTileEdgePoint(fromTile, direction, Tmp.v3);
                resolveTileEdgePoint(toTile, Mathf.mod(direction + 2, 4), Tmp.v4);
                Drawf.laser(laser, laserEnd, laserEnd, Tmp.v3.x, Tmp.v3.y, Tmp.v4.x, Tmp.v4.y, width, true);
            });
        }

        protected void drawFlowParticles(int direction) {
            Building other = links[direction];
            if (!linkValid(this, other)) return;
            drawFlowParticles(direction, this, other, direction, Mathf.mod(direction + 2, 4));
        }

        protected void drawFlowParticles(int direction, Building from, Building to, int fromDirection, int toDirection) {
            if (!WHSettings.effectEnabled() || beamEffectAlpha[direction] <= PsychicNetworkNode.epsilon) return;
            if (!resolvePrimaryBeamEndpoints(from, to, fromDirection, toDirection)) return;

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
                float slope = Interp.pow2Out.apply(Mathf.slope(fin));
                float len = flowParticleLen * lenScl * (0.45f + 0.55f * slope);
                float offsetLen = hitSize() / 2.5f * rand.random(-1f, 1f);
                float cx = Mathf.lerp(Tmp.v1.x, Tmp.v2.x, fin) + Angles.trnsx(angle + 90f, offsetLen);
                float cy = Mathf.lerp(Tmp.v1.y, Tmp.v2.y, fin) + Angles.trnsy(angle + 90f, offsetLen);
                float hx = Angles.trnsx(angle, len / 2f);
                float hy = Angles.trnsy(angle, len / 2f);

                Draw.color(Tmp.c1.set(WHPal.PsyColor).lerp(laserColor1, 0.35f + 0.25f * widthScl).a(alpha * alphaScl));
                Lines.stroke(flowParticleWidth * widthScl * slope);
                Lines.line(cx - hx, cy - hy, cx + hx, cy + hy, false);
            }
            Lines.stroke(1f);
        }

        protected void drawDashedLink(Building from, Building to, Color color) {
            int dir = directionTo(from, to);
            if (dir == -1) return;
            drawDashedLink(from, to, dir, Mathf.mod(dir + 2, 4), color);
        }

        protected void drawDashedLink(Building from, Building to, int fromDirection, int toDirection, Color color) {
            eachAlignedBeamTilePair(from, to, fromDirection, toDirection, (fromTile, toTile) -> {
                resolveTileEdgePoint(fromTile, fromDirection, Tmp.v3);
                resolveTileEdgePoint(toTile, toDirection, Tmp.v4);
                Drawf.dashLine(color, Tmp.v3.x, Tmp.v3.y, Tmp.v4.x, Tmp.v4.y);
            });
        }

        protected float linkDistanceScale(int direction) {
            Building other = links[direction];
            if (other == null) return 0f;

            float distance = Math.max(Math.abs(other.tileX() - tile.x), Math.abs(other.tileY() - tile.y));
            return Mathf.clamp((distance - 1f) / Math.max(linkRange - 1f, 1f));
        }

        protected boolean beamEnds(int direction) {
            return resolvePrimaryBeamEndpoints(this, links[direction], direction, Mathf.mod(direction + 2, 4));
        }

        protected int directionTo(Building from, Building to) {
            int dx = to.tileX() - from.tileX(), dy = to.tileY() - from.tileY();
            if (dx == 0 && dy != 0) return dy > 0 ? 1 : 3;
            if (dy == 0 && dx != 0) return dx > 0 ? 0 : 2;
            return -1;
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

    }
}
