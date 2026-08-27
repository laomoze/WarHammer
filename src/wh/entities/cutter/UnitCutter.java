package wh.entities.cutter;

import arc.Events;
import arc.audio.Sound;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.EventType.Trigger;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import wh.gen.CMoonUnit;
import wh.net.packet.UnitCutPacket;

/**
 * 单位切割入口。
 * <p>
 * 被切单位继续保留在 Groups.unit 中，使用原版死亡和坠落更新；但每帧截获
 * 当前 draw 调用并重新裁成两半，因此不会使用切割瞬间的静态截图。
 */
public final class UnitCutter {
    private static final float cutTransitionTime = 14f;
    private static final Seq<CutRequest> queuedCuts = new Seq<>();
    private static final Seq<CutExecution> activeCuts = new Seq<>();

    public static float fragmentEffectScale = 0.3f;

    static {
        // 两端更新，客户端负责绘制。
        Events.run(Trigger.update, UnitCutter::update);
        if (!Vars.headless) Events.run(Trigger.drawOver, UnitCutter::drawActiveCuts);
    }

    private UnitCutter() {
    }

    public static void cut(Unit unit, Effect explosionEffect, float x1, float y1, float x2, float y2) {
        cut(unit, x1, y1, x2, y2, explosionEffect, null);
    }

    public static void cut(Unit unit, float x1, float y1, float x2, float y2, Effect explosionEffect, Sound sound) {
        if (Vars.net.client()) return;
        if (unit instanceof CMoonUnit moon && moon.voidShieldProtects()) return;
        if (unit == null || !unit.isAdded() || unit.dead) return;
        if (findActiveCut(unit) != null) return;
        for (CutRequest request : queuedCuts) if (request.unit == unit) return;

        queuedCuts.add(new CutRequest(unit, x1, y1, x2, y2, explosionEffect, sound));
    }

    /**
     * 客户端接收切割同步。
     */
    public static void cutRemote(Unit unit, float x1, float y1, float x2, float y2) {
        if (unit == null || !unit.isAdded()) return;
        if (findActiveCut(unit) != null) return;
        for (CutRequest request : queuedCuts) if (request.unit == unit) return;
        queuedCuts.add(new CutRequest(unit, x1, y1, x2, y2, unit.type.deathExplosionEffect, null));
    }

    /**
     * 更新切割状态并同步客户端。
     */
    private static void update() {
        if (Vars.state.isPaused()) return;
        flushQueuedCuts();
        updateActiveCuts();
    }

    private static void flushQueuedCuts() {
        for (int index = queuedCuts.size - 1; index >= 0; index--) {
            CutRequest request = queuedCuts.remove(index);
            if (request.unit == null || !request.unit.isAdded()) continue;

            activeCuts.add(new CutExecution(request));
            if (Vars.net.server()) {
                UnitCutPacket packet = new UnitCutPacket();
                packet.unitId = request.unit.id;
                packet.x1 = request.x1;
                packet.y1 = request.y1;
                packet.x2 = request.x2;
                packet.y2 = request.y2;
                Vars.net.send(packet, false);
            }
            // 隐藏本体，保留单位更新。
            Groups.draw.remove(request.unit);

            // 标记死亡，关闭碰撞并保留坠机更新。
            request.unit.health = 0f;
            request.unit.dead = true;
        }
    }

    private static void drawActiveCuts() {
        for (CutExecution execution : activeCuts) {
            if (execution.unit != null && execution.unit.isAdded()) {
                drawExecution(execution);
            }
        }
    }

    private static void drawExecution(CutExecution execution) {
        Unit unit = execution.unit;
        if (execution.liveCapture == null) return;
        execution.lastX = unit.x;
        execution.lastY = unit.y;
        float cos = Mathf.cosDeg(unit.rotation);
        float sin = Mathf.sinDeg(unit.rotation);
        float x1 = execution.localX1 * cos - execution.localY1 * sin + unit.x;
        float y1 = execution.localX1 * sin + execution.localY1 * cos + unit.y;
        float x2 = execution.localX2 * cos - execution.localY2 * sin + unit.x;
        float y2 = execution.localX2 * sin + execution.localY2 * cos + unit.y;
        float fallProgress = execution.startElevation <= 0.0001f
                ? Mathf.clamp(execution.time / 30f)
                : 1f - Mathf.clamp(unit.elevation / execution.startElevation);
        float cutProgress = Mathf.clamp(execution.time / cutTransitionTime);
        cutProgress = cutProgress * cutProgress * (3f - 2f * cutProgress);
        float separation = Mathf.clamp(unit.bounds() * 0.035f, 2f, 8f)
                * (0.8f + fallProgress * 0.5f) * cutProgress;
        float openingAngle = Mathf.clamp(2f + fallProgress * 4f, 2f, 6f) * cutProgress;

        execution.liveCapture.captureAndDraw(unit, unit::draw, x1, y1, x2, y2, separation, openingAngle);
    }

    private static void updateActiveCuts() {
        for (int index = activeCuts.size - 1; index >= 0; index--) {
            CutExecution execution = activeCuts.get(index);
            execution.time += Time.delta;
            if (execution.unit == null || !execution.unit.isAdded()) {
                activeCuts.remove(index);
                continue;
            }

            Unit unit = execution.unit;
            float fallStep = unit.type.fallSpeed * Time.delta;
            // 在原版落地销毁前执行碎块爆炸。
            if (unit.type.fallSpeed <= 0f || unit.elevation <= fallStep + 0.01f) {
                finishExecution(execution);
                activeCuts.remove(index);
            }
        }
    }

    /**
     * 播放两侧爆炸并移除本体。
     */
    private static void finishExecution(CutExecution execution) {
        Unit unit = execution.unit;
        if (unit == null || !unit.isAdded()) return;

        float directionX = execution.localX2 - execution.localX1;
        float directionY = execution.localY2 - execution.localY1;
        float length = Mathf.len(directionX, directionY);
        float normalX = 0f;
        float normalY = 0f;
        if (!Mathf.zero(length)) {
            float cos = Mathf.cosDeg(unit.rotation);
            float sin = Mathf.sinDeg(unit.rotation);
            float worldDirectionX = directionX * cos - directionY * sin;
            float worldDirectionY = directionX * sin + directionY * cos;
            float separation = Mathf.clamp(unit.bounds() * 0.08f, 4f, 16f);
            normalX = -worldDirectionY / length * separation;
            normalY = worldDirectionX / length * separation;
        }

        Effect effect = execution.explosionEffect == null ? Fx.none : execution.explosionEffect;
        Sound sound = execution.explosionSound == null ? Sounds.none : execution.explosionSound;
        float size = Mathf.clamp(unit.bounds() / 6f * fragmentEffectScale, 0.6f, 5f);
        if (effect != Fx.none) {
            effect.at(unit.x + normalX, unit.y + normalY, size);
            effect.at(unit.x - normalX, unit.y - normalY, size);
        }
        if (sound != Sounds.none) {
            sound.at(unit.x, unit.y, Mathf.random(0.9f, 1.1f), Mathf.clamp(size / 1.1f));
        }

        unit.health = 0f;
        unit.remove();
    }

    private static CutExecution findActiveCut(Unit unit) {
        for (CutExecution execution : activeCuts) {
            if (execution.unit == unit) return execution;
        }
        return null;
    }

    private static class CutRequest {
        final Unit unit;
        final float x1, y1, x2, y2;
        final float localX1, localY1, localX2, localY2;
        final Effect explosionEffect;
        final Sound explosionSound;

        CutRequest(Unit unit, float x1, float y1, float x2, float y2, Effect explosionEffect, Sound explosionSound) {
            this.unit = unit;
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.explosionEffect = explosionEffect;
            this.explosionSound = explosionSound;
            float cos = Mathf.cosDeg(unit.rotation);
            float sin = Mathf.sinDeg(unit.rotation);
            float dx1 = x1 - unit.x, dy1 = y1 - unit.y;
            float dx2 = x2 - unit.x, dy2 = y2 - unit.y;
            localX1 = dx1 * cos + dy1 * sin;
            localY1 = -dx1 * sin + dy1 * cos;
            localX2 = dx2 * cos + dy2 * sin;
            localY2 = -dx2 * sin + dy2 * cos;
        }
    }

    private static class CutExecution {
        final Unit unit;
        final float localX1, localY1, localX2, localY2;
        final float startElevation;
        final Effect explosionEffect;
        final Sound explosionSound;
        /**
         * 每个切割独占一个捕获器。
         */
        final LiveUnitCutBatch liveCapture;
        float time;
        float lastX, lastY;

        CutExecution(CutRequest request) {
            unit = request.unit;
            localX1 = request.localX1;
            localY1 = request.localY1;
            localX2 = request.localX2;
            localY2 = request.localY2;
            explosionEffect = request.explosionEffect;
            explosionSound = request.explosionSound;
            startElevation = Math.max(unit.elevation, 0f);
            lastX = unit.x;
            lastY = unit.y;
            liveCapture = Vars.headless ? null : new LiveUnitCutBatch();
        }
    }
}
