package wh.gen;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.*;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ai.types.MissileAI;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import mindustry.world.blocks.defense.Wall.WallBuild;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockGroup;
import wh.content.WHBulletsOther;
import wh.content.WHFx;
import wh.entities.bullet.ApproachBullet;
import wh.entities.bullet.ApproachBullet.AB;
import wh.entities.world.drawer.GeminiUnitType;

import static mindustry.io.TypeIO.readEntity;
import static mindustry.io.TypeIO.writeEntity;

public class GeminiUnit extends UnitEntity {
    public static final float PRIMARY_RETARGET_INTERVAL = 8f * 60f;
    public static final float ATTACKER_MEMORY_DURATION = 20f * 60f;
    public static final float PRIMARY_LINK_DURATION = 18f * 60f;
    public static final float SECONDARY_RANGE = 220f;
    public static final float SECONDARY_REFRESH_INTERVAL = 30f;
    public static final float SECONDARY_TRANSFER_INTERVAL = 1f;
    public static final float SECONDARY_TRANSFER_DAMAGE_MULTIPLIER = 2f;
    public static final float SECONDARY_MIN_SCORE = 3500f;
    public static final float SELF_TO_PRIMARY_DAMAGE = 0.8f;
    public static final float ATTACKER_PRUNE_INTERVAL = 20f;

    public static final int MAX_SECONDARY_LINKS = 3;

    public static final float MAIN_STROKE = 2.6f;
    public static final float SECONDARY_STROKE = 1.8f;
    public static final float MAIN_STROKE_IN_LERP = 0.06f;
    public static final float MAIN_STROKE_OUT_LERP = 0.04f;
    public static final float SECONDARY_STROKE_IN_LERP = 0.08f;
    public static final float SECONDARY_STROKE_OUT_LERP = 0.05f;
    public static final float EARLY_LINK_RESET_PROGRESS = 1f / 3f;
    public static final float PHASE_INTERVAL = 25f * 60f;
    public static final float PHASE_DURATION = 7f * 60f;
    public static final float PHASE_ALPHA = 0.35f;
    public static final float PHASE_IN_LERP = 0.09f;
    public static final float PHASE_OUT_LERP = 0.06f;
    public static final float LINK_RING_LAYER = Layer.flyingUnitLow + 0.01f;
    public static final float LINK_RING_STROKE = 1.35f;
    public static final float LINK_RING_PULSE = 0.12f;
    public static final float LOW_HEALTH_EYE_THRESHOLD = 0.5f;
    public static final float LOW_HEALTH_EYE_LAYER = Layer.effect + 0.005f;
    public static final float LOW_HEALTH_EYE_OFFSET = 0.32f;
    public static final float LOW_HEALTH_EYE_OPEN_IN_SPEED = 0.08f;
    public static final float LOW_HEALTH_EYE_OPEN_OUT_SPEED = 0.06f;
    public static final float LOW_HEALTH_SPECIAL_INTERVAL = 6f * 60f;
    public static final int LOW_HEALTH_SPECIAL_MAX_BULLETS = 4;
    public static final int LOW_HEALTH_SPECIAL_BURST = 3;
    public static final float LOW_HEALTH_SPECIAL_SPREAD = 20f;
    public static final float LOW_HEALTH_SPECIAL_SCAN_RANGE = 200;
    public static final float LOW_HEALTH_SPECIAL_SCAN_INTERVAL = 120;
    public static final Rand seededRand = new Rand();

    public final ObjectFloatMap<Healthc> attackers = new ObjectFloatMap<>();
    public final ObjectFloatMap<Healthc> attackerHatred = new ObjectFloatMap<>();
    public final Seq<Healthc> secondaryLinks = new Seq<>();
    public final Seq<Healthc> secondaryCandidates = new Seq<>();
    public final Seq<Healthc> pendingRemove = new Seq<>();

    public final IntFloatMap secondaryLastHealth = new IntFloatMap();
    public final IntSet seenSecondary = new IntSet();
    public final IntSeq staleSecondary = new IntSeq();
    public final IntFloatMap secondaryStrokeFade = new IntFloatMap();
    public final IntFloatMap secondaryGhostX = new IntFloatMap();
    public final IntFloatMap secondaryGhostY = new IntFloatMap();
    public final IntFloatMap secondaryGhostRadius = new IntFloatMap();
    public final IntSet seenSecondaryStroke = new IntSet();
    public final IntSet drawnSecondaryStroke = new IntSet();
    public final IntSeq staleSecondaryStroke = new IntSeq();
    public final Seq<Bullet> lowHealthSpecialBullets = new Seq<>();
    public final Seq<Bullet> lowHealthSpecialDraw = new Seq<>();

    public Healthc primaryLink;
    public float primaryLinkTimeLeft = 0f;
    public float primaryStrokeFade = 0f;
    public float retargetTimer = PRIMARY_RETARGET_INTERVAL;
    public float secondaryRefreshTimer = 0f;
    public float secondaryTransferTimer = SECONDARY_TRANSFER_INTERVAL;
    public float attackerPruneTimer = ATTACKER_PRUNE_INTERVAL;
    public float phaseCooldownTimer = PHASE_INTERVAL;
    public float phaseTimeLeft = 0f;
    public float phaseVisualFade = 0f;
    public float primaryGhostX = Float.NaN;
    public float primaryGhostY = Float.NaN;
    public float primaryGhostRadius = Float.NaN;
    public ApproachBullet lowHealthSpecialBulletType;
    public float lowHealthSpecialTimer = LOW_HEALTH_SPECIAL_INTERVAL;
    public float lowHealthSpecialScanTimer = 0f;
    public float lowHealthEyeOpen = 0f;

    @Override
    public int classId() {
        return EntityRegister.getId(GeminiUnit.class);
    }

    @Override
    public void setType(UnitType type) {
        super.setType(type);
        lowHealthSpecialBulletType = resolveLowHealthSpecialBulletType(type);
        retargetTimer = Mathf.random(0f, PRIMARY_RETARGET_INTERVAL);
        secondaryRefreshTimer = 0f;
        secondaryTransferTimer = SECONDARY_TRANSFER_INTERVAL;
        attackerPruneTimer = Mathf.random(0f, ATTACKER_PRUNE_INTERVAL);
        phaseCooldownTimer = Mathf.random(PHASE_INTERVAL * 0.2f, PHASE_INTERVAL);
        phaseTimeLeft = 0f;
        phaseVisualFade = 0f;
        primaryStrokeFade = 0f;
        primaryGhostX = Float.NaN;
        primaryGhostY = Float.NaN;
        primaryGhostRadius = Float.NaN;
        lowHealthSpecialTimer = Mathf.random(LOW_HEALTH_SPECIAL_INTERVAL * 0.35f, LOW_HEALTH_SPECIAL_INTERVAL);
        lowHealthSpecialScanTimer = Mathf.random(0f, LOW_HEALTH_SPECIAL_SCAN_INTERVAL);
        lowHealthEyeOpen = 0f;
        secondaryStrokeFade.clear();
        secondaryGhostX.clear();
        secondaryGhostY.clear();
        secondaryGhostRadius.clear();
        drawnSecondaryStroke.clear();
        lowHealthSpecialBullets.clear();
        lowHealthSpecialDraw.clear();
    }

    public ApproachBullet resolveLowHealthSpecialBulletType(UnitType fromType) {
        if (fromType instanceof GeminiUnitType gType && gType.lowHealthSpecialBullet != null) {
            return gType.lowHealthSpecialBullet;
        }
        if (WHBulletsOther.RevengeBullet3 instanceof ApproachBullet) {
            return (ApproachBullet) WHBulletsOther.RevengeBullet3;
        }
        return null;
    }

    public boolean setLowHealthSpecialBulletType(BulletType bulletType) {
        if (!(bulletType instanceof ApproachBullet)) return false;
        lowHealthSpecialBulletType = (ApproachBullet) bulletType;
        lowHealthSpecialBullets.clear();
        lowHealthSpecialDraw.clear();
        return true;
    }

    @Override
    public boolean collides(Hitboxc other) {
        return !isPhasing() && super.collides(other);
    }

    @Override
    public boolean targetable(Team targeter) {
        return !isPhasing() && super.targetable(targeter);
    }

    @Override
    public boolean hittable() {
        return !isPhasing() && super.hittable();
    }

    @Override
    public void collision(Hitboxc other, float x, float y) {
        if (isPhasing()) return;

        super.collision(other, x, y);

        if (!(other instanceof Bullet bullet)) return;
        if (bullet.team == team) return;

        Healthc owner = findOwner(bullet);
        if (isEnemyTarget(owner)) {
            attackers.put(owner, Time.time + ATTACKER_MEMORY_DURATION);
            float hatred = bulletHatred(bullet);
            attackerHatred.increment(owner, hatred, hatred);
        }
    }

    @Override
    public void rawDamage(float amount) {
        if (amount <= 0f) {
            super.rawDamage(amount);
            return;
        }

        // Clients keep vanilla application; damage split is server-authoritative.
        if (Vars.net.client() || isPhasing() || !isEnemyTarget(primaryLink)) {
            super.rawDamage(amount);
            return;
        }

        float share = Mathf.clamp(SELF_TO_PRIMARY_DAMAGE, 0f, 1f);
        float selfPortion = 1f - share;
        if (selfPortion <= 0.0001f) {
            damageLinkedTarget(amount);
            damageSecondaryTargets(amount);
            return;
        }

        float before = health + Math.max(shield, 0f);
        super.rawDamage(amount * selfPortion);
        float after = health + Math.max(shield, 0f);
        float selfTaken = Math.max(before - after, 0f);

        // Keep split ratio stable based on actual durability loss on self.
        if (selfTaken > 0.001f) {
            float transfer = selfTaken * (share / selfPortion);
            damageLinkedTarget(transfer);
            damageSecondaryTargets(transfer);
        }
    }

    public void damageSecondaryTargets(float amount) {
        if (amount <= 0.001f || !isEnemyTarget(primaryLink)) return;

        for (int i = 0; i < secondaryLinks.size; i++) {
            Healthc secondary = secondaryLinks.get(i);
            if (!isSecondaryTarget(secondary)) continue;

            secondary.damage(amount);

            int sid = entityId(secondary);
            if (sid >= 0) {
                // 立即刷新快照，避免将“主链转移造成的掉血”再次当作次级转移来源重复结算。
                secondaryLastHealth.put(sid, currentDurability(secondary));
            }
        }
    }

    @Override
    public void update() {
        super.update();

        if (Vars.net.client()) {
            updateClientVisualState();
            return;
        }

        updateLowHealthEyeAnimation();
        updatePhaseState();
        updateLowHealthSpecialAttack();

        attackerPruneTimer -= Time.delta;
        if (attackerPruneTimer <= 0f) {
            attackerPruneTimer = ATTACKER_PRUNE_INTERVAL;
            pruneExpiredAttackers();
        }

        if (isPhasing()) {
            if (primaryLink != null) {
                losePrimaryLink(true);
            }
            updateStrokeFade(false);
            return;
        }

        // 主链接目标死亡/失效时统一入口处理，避免分散逻辑。
        onPrimaryTargetDeadOrInvalid();

        retargetTimer -= Time.delta;
        if (retargetTimer <= 0f) {
            retargetTimer = PRIMARY_RETARGET_INTERVAL;
            choosePrimaryLink();
        }

        boolean hasPrimary = isEnemyTarget(primaryLink);
        if (hasPrimary) {
            primaryLinkTimeLeft -= Time.delta;
            if (primaryLinkTimeLeft <= 0f) {
                expirePrimaryLink();
                hasPrimary = false;
            }
        }

        if (hasPrimary) {
            secondaryRefreshTimer -= Time.delta;
            if (secondaryRefreshTimer <= 0f) {
                secondaryRefreshTimer = SECONDARY_REFRESH_INTERVAL;
                refreshSecondaryLinks();
            }

            secondaryTransferTimer -= Time.delta;
            if (secondaryTransferTimer <= 0f) {
                secondaryTransferTimer = SECONDARY_TRANSFER_INTERVAL;
                processSecondaryTransfers();
            }
        }

        updateStrokeFade(hasPrimary);
    }

    public void updateClientVisualState() {
        updateLowHealthEyeAnimation();
        cleanupLowHealthSpecialBullets();

        // 客户端也保持同样的主链失效处理，避免视觉状态分叉。
        onPrimaryTargetDeadOrInvalid();

        if (phaseTimeLeft > 0f) {
            phaseTimeLeft = Math.max(phaseTimeLeft - Time.delta, 0f);
        }

        boolean phased = isPhasing();
        phaseVisualFade = Mathf.lerpDelta(phaseVisualFade, phased ? 1f : 0f, phased ? PHASE_IN_LERP : PHASE_OUT_LERP);

        if (primaryLinkTimeLeft > 0f) {
            primaryLinkTimeLeft = Math.max(primaryLinkTimeLeft - Time.delta, 0f);
        }

        updateStrokeFade(isEnemyTarget(primaryLink));
    }

    @Override
    public void draw() {
        float bodyAlpha = Mathf.lerp(1f, PHASE_ALPHA, phaseVisualFade);
        Draw.alpha(bodyAlpha);
        super.draw();
        Draw.alpha(1f);

        if (Vars.headless) return;

        float eyeDanger = Mathf.clamp((LOW_HEALTH_EYE_THRESHOLD - healthf()) / LOW_HEALTH_EYE_THRESHOLD);
        float eyeCx = x;
        float eyeCy = y + hitSize * (0.58f + eyeDanger * 0.08f + LOW_HEALTH_EYE_OFFSET);
        drawLowHealthEye(eyeCx, eyeCy, hitSize * 1.02f);

        Color mainColor = Tmp.c1.set(team.color).lerp(Color.white, Mathf.absin(4.2f, 0.34f));
        Color secondaryColor = Tmp.c2.set(mainColor).lerp(Color.white, 0.2f);
        float z = Draw.z();
        boolean hasPrimary = isEnemyTarget(primaryLink);
        boolean hasAnchor = hasPrimary || hasPrimaryGhost();
        if (hasAnchor) {
            float primaryX = hasPrimary ? primaryLink.getX() : primaryGhostX;
            float primaryY = hasPrimary ? primaryLink.getY() : primaryGhostY;
            float primaryRadius = hasPrimary ? targetHitSize(primaryLink) * 0.5f : Math.max(primaryGhostRadius, 2f);

            float primaryFadeVisual = Mathf.pow(primaryStrokeFade, 1.35f);
            drawnSecondaryStroke.clear();

            Draw.z(LINK_RING_LAYER);
            if (hasPrimary) {
                drawLinkRing(primaryLink, mainColor, bodyAlpha * primaryFadeVisual);
            }
            for (int i = 0; i < secondaryLinks.size; i++) {
                Healthc linked = secondaryLinks.get(i);
                if (!isSecondaryRenderTarget(linked)) continue;
                int linkId = entityId(linked);
                float secondaryFade = linkId < 0 ? 0f : secondaryStrokeFade.get(linkId, 0f);
                if (secondaryFade <= 0.01f) continue;
                float secondaryFadeVisual = Mathf.pow(secondaryFade, 1.35f);
                drawLinkRing(linked, secondaryColor, bodyAlpha * secondaryFadeVisual * 0.84f);
            }

            Draw.z(Layer.effect + 0.02f);
            float mainStroke = MAIN_STROKE * Mathf.pow(primaryStrokeFade, 1.45f);
            if (mainStroke > 0.01f) {
                long primarySeed = ((long) id << 32) ^ (hasPrimary ? entityId(primaryLink) : 911L);
                drawLinkCurvesSized(
                        x, y, hitSize * 0.5f,
                        primaryX, primaryY, primaryRadius,
                        4,
                        primarySeed,
                        mainStroke,
                        mainColor,
                        bodyAlpha
                );
            }

            for (int i = 0; i < secondaryLinks.size; i++) {
                Healthc linked = secondaryLinks.get(i);
                int linkId = entityId(linked);
                float secondaryFade = linkId < 0 ? 0f : secondaryStrokeFade.get(linkId, 0f);
                float secondaryStroke = SECONDARY_STROKE * Mathf.pow(secondaryFade, 1.55f);
                if (secondaryStroke <= 0.01f) continue;

                float tx = linked.getX();
                float ty = linked.getY();
                float tr = targetHitSize(linked) * 0.5f;
                if (linkId >= 0) {
                    drawnSecondaryStroke.add(linkId);
                    tx = secondaryGhostX.get(linkId, tx);
                    ty = secondaryGhostY.get(linkId, ty);
                    tr = secondaryGhostRadius.get(linkId, tr);
                    if (isSecondaryRenderTarget(linked)) {
                        tx = linked.getX();
                        ty = linked.getY();
                        tr = targetHitSize(linked) * 0.5f;
                    }
                }

                drawLinkCurvesSized(
                        primaryX, primaryY, primaryRadius,
                        tx, ty, tr,
                        3,
                        ((long) id << 28) ^ entityId(linked) ^ (long) i * 131L,
                        secondaryStroke,
                        secondaryColor,
                        0.82f * bodyAlpha
                );
            }

            // Residual trails: draw to last known positions for links already removed from secondaryLinks.
            for (IntFloatMap.Entry entry : secondaryStrokeFade) {
                int linkId = entry.key;
                if (drawnSecondaryStroke.contains(linkId)) continue;

                float secondaryFade = entry.value;
                float secondaryStroke = SECONDARY_STROKE * Mathf.pow(secondaryFade, 1.55f);
                if (secondaryStroke <= 0.01f) continue;

                float tx = secondaryGhostX.get(linkId, Float.NaN);
                float ty = secondaryGhostY.get(linkId, Float.NaN);
                float tr = secondaryGhostRadius.get(linkId, 2f);
                if (Float.isNaN(tx) || Float.isNaN(ty)) continue;

                drawLinkCurvesSized(
                        primaryX, primaryY, primaryRadius,
                        tx, ty, tr,
                        3,
                        ((long) id << 28) ^ linkId ^ 7331L,
                        secondaryStroke,
                        secondaryColor,
                        0.82f * bodyAlpha
                );
            }
        }

        drawLowHealthSpecialLinks(mainColor, bodyAlpha);

        Draw.z(z);
        Draw.reset();
    }

    public boolean isPhasing() {
        return phaseTimeLeft > 0.001f;
    }

    public void updatePhaseState() {
        if (phaseTimeLeft > 0f) {
            phaseTimeLeft -= Time.delta;
            if (phaseTimeLeft <= 0f) {
                phaseTimeLeft = 0f;
                phaseCooldownTimer = PHASE_INTERVAL;
            }
        } else {
            phaseCooldownTimer -= Time.delta;
            if (phaseCooldownTimer <= 0f) {
                activatePhase();
            }
        }

        boolean phased = isPhasing();
        phaseVisualFade = Mathf.lerpDelta(phaseVisualFade, phased ? 1f : 0f, phased ? PHASE_IN_LERP : PHASE_OUT_LERP);
    }

    public void activatePhase() {
        phaseTimeLeft = PHASE_DURATION;
        phaseCooldownTimer = PHASE_INTERVAL;
        if (!Vars.headless) {
            WHFx.spawn.at(x, y, rotation, team.color, type);
        }
    }

    public void pruneExpiredAttackers() {
        pendingRemove.clear();
        for (ObjectFloatMap.Entry<Healthc> entry : attackers.entries()) {
            Healthc target = entry.key;
            if (entry.value <= Time.time || !isEnemyTarget(target)) {
                pendingRemove.add(target);
            }
        }

        for (Healthc target : pendingRemove) {
            attackers.remove(target, 0f);
            attackerHatred.remove(target, 0f);
        }
    }

    public void choosePrimaryLink() {
        pruneExpiredAttackers();

        Healthc best = null;
        float bestHatred = -1f;

        for (ObjectFloatMap.Entry<Healthc> entry : attackers.entries()) {
            if (entry.value <= Time.time) continue;
            Healthc target = entry.key;
            if (!isEnemyTarget(target)) continue;

            float hatred = attackerHatred.get(target, 0f);
            if (best == null || hatred > bestHatred || (Mathf.equal(hatred, bestHatred, 0.001f) && targetScore(target) > targetScore(best))) {
                best = target;
                bestHatred = hatred;
            }
        }

        if (best == primaryLink) return;

        if (best == null) {
            losePrimaryLink(true);
            return;
        }

        primaryLink = best;
        primaryLinkTimeLeft = PRIMARY_LINK_DURATION;
        secondaryRefreshTimer = 0f;
        secondaryTransferTimer = 0f;
        primaryStrokeFade = 0f;
        primaryGhostX = Float.NaN;
        primaryGhostY = Float.NaN;
        primaryGhostRadius = Float.NaN;
        secondaryStrokeFade.clear();
        secondaryGhostX.clear();
        secondaryGhostY.clear();
        secondaryGhostRadius.clear();
        drawnSecondaryStroke.clear();
        clearSecondaryActiveLinks();
    }

    public void expirePrimaryLink() {
        if (primaryLink != null) {
            attackers.remove(primaryLink, 0f);
            attackerHatred.remove(primaryLink, 0f);
        }
        losePrimaryLink(false);
    }

    public void refreshSecondaryLinks() {
        if (onPrimaryTargetDeadOrInvalid()) {
            return;
        }
        if (!(primaryLink instanceof Teamc)) {
            losePrimaryLink(true);
            return;
        }

        Team enemyTeam = ((Teamc) primaryLink).team();
        float px = primaryLink.getX(), py = primaryLink.getY();
        secondaryCandidates.clear();

        Units.nearby(enemyTeam, px - SECONDARY_RANGE, py - SECONDARY_RANGE, SECONDARY_RANGE * 2f, SECONDARY_RANGE * 2f, other -> {
            if (other == this || other == primaryLink || other.dead()) return;
            if (!other.isValid() || targetScore(other) < SECONDARY_MIN_SCORE) return;
            secondaryCandidates.add(other);
        });

        Units.nearbyBuildings(px, py, SECONDARY_RANGE, other -> {
            if (other == primaryLink || other.team != enemyTeam || other.dead || other.block == null) return;
            if (other.block.group == BlockGroup.walls || !other.block.targetable || other.block instanceof CoreBlock)
                return;
            if (targetScore(other) < SECONDARY_MIN_SCORE) return;
            secondaryCandidates.add(other);
        });

        secondaryCandidates.sort(e -> -targetScore(e));
        secondaryCandidates.truncate(MAX_SECONDARY_LINKS);

        for (int i = 0; i < secondaryCandidates.size; i++) {
            Healthc candidate = secondaryCandidates.get(i);
            if (!secondaryLinks.contains(candidate, true)) {
                secondaryLinks.add(candidate);
            }
            int candidateId = entityId(candidate);
            if (candidateId >= 0) {
                secondaryStrokeFade.put(candidateId, secondaryStrokeFade.get(candidateId, 0f));
                secondaryGhostX.put(candidateId, candidate.getX());
                secondaryGhostY.put(candidateId, candidate.getY());
                secondaryGhostRadius.put(candidateId, targetHitSize(candidate) * 0.5f);
            }
        }
        syncSecondarySnapshots();
    }

    /**
     * 次级链接伤害转移核心：
     * 1) 维护 secondaryLinks 的有效性（失效目标先记录幽灵坐标，再移除活动列表）。
     * 2) 对仍在次级范围内的目标，比较“上一帧耐久快照”和“当前耐久”。
     * 3) 目标掉血 -> 按倍率把伤害转移到主链接，同时自身也承受同量（反噬/回灌效果）。
     * 4) 清理离开监控集合的快照，避免 map 长期积累。
     */
    public void processSecondaryTransfers() {
        if (Vars.net.client()) return;

        if (onPrimaryTargetDeadOrInvalid()) {
            return;
        }

        if (secondaryLinks.isEmpty()) {
            secondaryLastHealth.clear();
            return;
        }

        pendingRemove.clear();
        seenSecondary.clear();

        // 扫描所有活动次级链接，更新快照并执行伤害转移。
        for (int i = 0; i < secondaryLinks.size; i++) {
            Healthc secondary = secondaryLinks.get(i);
            if (!isSecondaryRenderTarget(secondary)) {
                int sid = entityId(secondary);
                if (sid >= 0) {
                    secondaryGhostX.put(sid, secondary.getX());
                    secondaryGhostY.put(sid, secondary.getY());
                    secondaryGhostRadius.put(sid, targetHitSize(secondary) * 0.5f);
                }
                pendingRemove.add(secondary);
                continue;
            }

            // 不在次级生效范围内时，不做转移，但保留链接用于后续淡出表现。
            if (!isSecondaryTarget(secondary)) {
                continue;
            }

            int entityId = entityId(secondary);
            if (entityId < 0) {
                pendingRemove.add(secondary);
                continue;
            }

            seenSecondary.add(entityId);

            float durabilityNow = currentDurability(secondary);
            float lastDurability = secondaryLastHealth.get(entityId, Float.NaN);
            if (Float.isNaN(lastDurability)) {
                secondaryLastHealth.put(entityId, durabilityNow);
                continue;
            }

            float damageTaken = Math.max(lastDurability - durabilityNow, 0f);
            if (damageTaken > 0.001f) {
                // 目标掉血 -> 转移到主链接 + 自身反噬。
                float transfer = damageTaken * SECONDARY_TRANSFER_DAMAGE_MULTIPLIER;
                damageLinkedTarget(transfer);
                super.rawDamage(transfer);
            }

            secondaryLastHealth.put(entityId, durabilityNow);
        }

        // 从活动链接列表移除失效目标（视觉残影仍由 ghost + fade 维护）。
        for (Healthc target : pendingRemove) {
            secondaryLinks.remove(target);
        }

        // 清理快照中已不再被本轮扫描到的条目，避免无效历史数据堆积。
        staleSecondary.clear();
        for (IntFloatMap.Entry entry : secondaryLastHealth) {
            if (!seenSecondary.contains(entry.key)) {
                staleSecondary.add(entry.key);
            }
        }

        for (int i = 0; i < staleSecondary.size; i++) {
            secondaryLastHealth.remove(staleSecondary.get(i), 0f);
        }
    }

    public void capturePrimaryGhost() {
        if (primaryLink != null) {
            primaryGhostX = primaryLink.getX();
            primaryGhostY = primaryLink.getY();
            primaryGhostRadius = targetHitSize(primaryLink) * 0.5f;
        }
    }

    public boolean hasPrimaryGhost() {
        return !Float.isNaN(primaryGhostX) && !Float.isNaN(primaryGhostY) && !Float.isNaN(primaryGhostRadius);
    }

    public float primaryLinkProgress() {
        return Mathf.clamp(1f - primaryLinkTimeLeft / PRIMARY_LINK_DURATION);
    }

    public boolean shouldHardResetPrimaryLoss() {
        return primaryLinkProgress() < EARLY_LINK_RESET_PROGRESS;
    }

    /**
     * 主链接目标死亡/失效统一处理入口。
     *
     * @return 是否已处理断链（true 表示 primaryLink 已被清空）
     */
    public boolean onPrimaryTargetDeadOrInvalid() {
        if (primaryLink == null) return false;
        if (isEnemyTarget(primaryLink)) return false;
        losePrimaryLink(true);
        return true;
    }

    /**
     * 主链接丢失统一收尾：
     * 1) 早期窗口（EARLY_LINK_RESET_PROGRESS）可选硬重置，直接清空残影与次链缓存；
     * 2) 非硬重置保留主链 ghost，用于线条自然淡出。
     */
    public void losePrimaryLink(boolean allowEarlyHardReset) {
        if (primaryLink == null) {
            boolean hardReset = allowEarlyHardReset && shouldHardResetPrimaryLoss();
            if (hardReset) {
                primaryGhostX = Float.NaN;
                primaryGhostY = Float.NaN;
                primaryGhostRadius = Float.NaN;
                primaryStrokeFade = 0f;
                clearSecondaryLinks();
            } else {
                clearSecondaryActiveLinks();
            }
            primaryLinkTimeLeft = 0f;
            return;
        }

        boolean hardReset = allowEarlyHardReset && shouldHardResetPrimaryLoss();
        if (hardReset) {
            primaryGhostX = Float.NaN;
            primaryGhostY = Float.NaN;
            primaryGhostRadius = Float.NaN;
            primaryStrokeFade = 0f;
            clearSecondaryLinks();
        } else {
            capturePrimaryGhost();
            clearSecondaryActiveLinks();
        }

        primaryLink = null;
        primaryLinkTimeLeft = 0f;
    }

    public void clearSecondaryActiveLinks() {
        secondaryLinks.clear();
        secondaryCandidates.clear();
        secondaryLastHealth.clear();
        seenSecondary.clear();
        staleSecondary.clear();
    }

    public void clearSecondaryLinks() {
        clearSecondaryActiveLinks();
        secondaryStrokeFade.clear();
        secondaryGhostX.clear();
        secondaryGhostY.clear();
        secondaryGhostRadius.clear();
        drawnSecondaryStroke.clear();
        seenSecondaryStroke.clear();
        staleSecondaryStroke.clear();
    }

    /**
     * 线条淡入/淡出状态机（主链 + 次链 + 残影）：
     * 1) 主链根据 hasPrimary 插值到 1 或 0。
     * 2) 次链对“仍有效且在范围”的目标淡入，对其它目标淡出。
     * 3) 即使实体死亡/离场，也保留 ghost 坐标直到 fade 归零，避免“瞬断”。
     * 4) fade 归零后再删缓存，防止 map 与链接状态不同步。
     */
    public void updateStrokeFade(boolean hasPrimary) {
        primaryStrokeFade = Mathf.lerpDelta(primaryStrokeFade, hasPrimary ? 1f : 0f, hasPrimary ? MAIN_STROKE_IN_LERP : MAIN_STROKE_OUT_LERP);
        if (hasPrimary && primaryLink != null) {
            primaryGhostX = primaryLink.getX();
            primaryGhostY = primaryLink.getY();
            primaryGhostRadius = targetHitSize(primaryLink) * 0.5f;
        }

        seenSecondaryStroke.clear();
        // 第一阶段：更新活动次链的目标态（active/inactive）并推进 fade。
        for (int i = 0; i < secondaryLinks.size; i++) {
            Healthc linked = secondaryLinks.get(i);
            int linkId = entityId(linked);
            if (linkId < 0) continue;

            if (isSecondaryRenderTarget(linked)) {
                secondaryGhostX.put(linkId, linked.getX());
                secondaryGhostY.put(linkId, linked.getY());
                secondaryGhostRadius.put(linkId, targetHitSize(linked) * 0.5f);
            }

            boolean active = hasPrimary && isSecondaryTarget(linked);
            if (active) seenSecondaryStroke.add(linkId);

            float now = secondaryStrokeFade.get(linkId, 0f);
            float target = active ? 1f : 0f;
            float speed = active ? SECONDARY_STROKE_IN_LERP : SECONDARY_STROKE_OUT_LERP;
            secondaryStrokeFade.put(linkId, Mathf.lerpDelta(now, target, speed));
        }

        // 第二阶段：对本帧未命中的旧条目继续淡出（残影尾巴）。
        staleSecondaryStroke.clear();
        for (IntFloatMap.Entry entry : secondaryStrokeFade) {
            if (seenSecondaryStroke.contains(entry.key)) continue;

            float out = Mathf.lerpDelta(entry.value, 0f, SECONDARY_STROKE_OUT_LERP);
            if (out <= 0.01f) {
                staleSecondaryStroke.add(entry.key);
            } else {
                secondaryStrokeFade.put(entry.key, out);
            }
        }

        // 第三阶段：淡出完成后才真正删除缓存。
        for (int i = 0; i < staleSecondaryStroke.size; i++) {
            int linkId = staleSecondaryStroke.get(i);
            secondaryStrokeFade.remove(linkId, 0f);
            secondaryGhostX.remove(linkId, 0f);
            secondaryGhostY.remove(linkId, 0f);
            secondaryGhostRadius.remove(linkId, 0f);
        }

        // 第四阶段：从活动链接中移除“不可渲染且 fade 已几乎为 0”的目标。
        pendingRemove.clear();
        for (int i = 0; i < secondaryLinks.size; i++) {
            Healthc linked = secondaryLinks.get(i);
            int linkId = entityId(linked);
            float fade = linkId < 0 ? 0f : secondaryStrokeFade.get(linkId, 0f);

            if (!isSecondaryRenderTarget(linked)) {
                if (fade <= 0.01f) {
                    pendingRemove.add(linked);
                }
                continue;
            }

            if (linkId < 0) {
                if (!isSecondaryTarget(linked)) {
                    pendingRemove.add(linked);
                }
                continue;
            }

            if (!isSecondaryTarget(linked) && fade <= 0.01f) {
                pendingRemove.add(linked);
            }
        }

        for (Healthc target : pendingRemove) {
            secondaryLinks.remove(target);
        }
    }

    public void syncSecondarySnapshots() {
        seenSecondary.clear();

        for (int i = 0; i < secondaryLinks.size; i++) {
            Healthc target = secondaryLinks.get(i);
            if (!isSecondaryTarget(target)) continue;

            int entityId = entityId(target);
            if (entityId < 0) continue;

            seenSecondary.add(entityId);
            secondaryLastHealth.put(entityId, currentDurability(target));
        }

        staleSecondary.clear();
        for (IntFloatMap.Entry entry : secondaryLastHealth) {
            if (!seenSecondary.contains(entry.key)) {
                staleSecondary.add(entry.key);
            }
        }

        for (int i = 0; i < staleSecondary.size; i++) {
            secondaryLastHealth.remove(staleSecondary.get(i), 0f);
        }
    }

    public void damageLinkedTarget(float amount) {
        if (amount <= 0.001f || !isEnemyTarget(primaryLink)) return;
        primaryLink.damage(amount);
    }

    public Healthc findOwner(Entityc source) {
        Entityc current = source;
        Healthc target = null;

        for (int itr = 0; itr < 6; itr++) {
            if (!(current instanceof Bullet bullet)) break;
            Entityc owner = bullet.owner();

            if (owner instanceof Bullet) {
                current = owner;
                continue;
            }

            if (owner instanceof Unit unit) {
                if (unit.controller() instanceof MissileAI ai) {
                    Unit shooter = ai.shooter;
                    target = shooter == null ? unit : shooter;
                } else {
                    target = unit;
                }
            } else if (owner instanceof Building building) {
                target = building;
            }

            break;
        }

        return target;
    }

    public boolean isEnemyTarget(Healthc target) {
        if (target == null || target.dead() || !target.isValid()) return false;
        if (!(target instanceof Teamc teamTarget)) return false;
        if (teamTarget.team() == team) return false;
        if (target instanceof WallBuild) return false;
        if (target instanceof Unit unit && !unit.hittable()) return false;
        if (target instanceof Building building && (building.block == null || building.block.group == BlockGroup.walls || building.block instanceof CoreBlock))
            return false;
        return true;
    }

    public boolean isSecondaryRenderTarget(Healthc target) {
        if (target == null || target == primaryLink || target.dead() || !target.isValid()) return false;
        if (!(target instanceof Teamc targetTeam) || !(primaryLink instanceof Teamc primaryTeam)) return false;
        if (targetTeam.team() != primaryTeam.team()) return false;
        if (target instanceof WallBuild) return false;
        if (target instanceof Unit unit && !unit.hittable()) return false;
        if (target instanceof Building building) {
            if (building.block == null || building.block.group == BlockGroup.walls || building.block instanceof CoreBlock || !building.block.targetable)
                return false;
        }
        return true;
    }

    public boolean isSecondaryTarget(Healthc target) {
        if (!isSecondaryRenderTarget(target)) return false;

        float range = SECONDARY_RANGE + targetHitSize(target) * 0.5f;
        return Mathf.within(primaryLink.getX(), primaryLink.getY(), target.getX(), target.getY(), range);
    }

    public int entityId(Healthc target) {
        return target == null ? -1 : target.id();
    }

    public float currentHealth(Healthc target) {
        return target == null ? 0f : Math.max(target.health(), 0f);
    }

    public float currentShield(Healthc target) {
        return target instanceof Unit unit ? Math.max(unit.shield, 0f) : 0f;
    }

    public float currentDurability(Healthc target) {
        return currentHealth(target) + currentShield(target);
    }

    public float targetScore(Healthc target) {
        if (target == null) return 0f;
        return target.maxHealth() + currentShield(target);
    }

    public float bulletHatred(Bullet bullet) {
        if (bullet == null) return 1f;

        BulletType type = bullet.type;
        if (type == null) return Math.max(bullet.damage, 1f);

        float typeDamage = Math.max(type.damage, 0f);
        float ratio = typeDamage <= 0.001f ? 1f : Mathf.clamp(bullet.damage / typeDamage, 0.75f, 1.25f);
        float base = typeDamage + Math.max(type.splashDamage, 0f) + Math.max(type.lightningDamage, 0f);
        if (base <= 0.001f) {
            base = Math.max(bullet.damage, 1f);
        }

        return Math.max(ratio * base, 1f);
    }

    public void updateLowHealthSpecialAttack() {
        cleanupLowHealthSpecialBullets();

        if (Vars.net.client()) return;
        if (dead() || isPhasing()) {
            lowHealthSpecialTimer = LOW_HEALTH_SPECIAL_INTERVAL;
            return;
        }

        if (healthf() >= LOW_HEALTH_EYE_THRESHOLD) {
            lowHealthSpecialTimer = Math.min(lowHealthSpecialTimer + Time.delta * 0.45f, LOW_HEALTH_SPECIAL_INTERVAL);
            return;
        }

        lowHealthSpecialTimer -= Time.delta;
        if (lowHealthSpecialTimer > 0f) return;

        lowHealthSpecialTimer = LOW_HEALTH_SPECIAL_INTERVAL;
        if (lowHealthSpecialBullets.size >= LOW_HEALTH_SPECIAL_MAX_BULLETS) return;

        fireLowHealthSpecialAttack();
    }

    public void cleanupLowHealthSpecialBullets() {
        for (int i = lowHealthSpecialBullets.size - 1; i >= 0; i--) {
            if (!isOwnedLowHealthSpecialBullet(lowHealthSpecialBullets.get(i))) {
                lowHealthSpecialBullets.remove(i);
            }
        }

        lowHealthSpecialScanTimer -= Time.delta;
        if (lowHealthSpecialScanTimer > 0f) return;

        lowHealthSpecialScanTimer = LOW_HEALTH_SPECIAL_SCAN_INTERVAL;
        collectLowHealthSpecialBullets();
    }

    public void collectLowHealthSpecialBullets() {
        float range = LOW_HEALTH_SPECIAL_SCAN_RANGE;
        Groups.bullet.intersect(x - range, y - range, range * 2f, range * 2f, bullet -> {
            if (!isOwnedLowHealthSpecialBullet(bullet)) return;
            if (!lowHealthSpecialBullets.contains(bullet, true)) {
                lowHealthSpecialBullets.add(bullet);
            }
        });
    }

    public boolean isOwnedLowHealthSpecialBullet(Bullet bullet) {
        if (bullet == null || !bullet.isAdded()) return false;
        if (lowHealthSpecialBulletType == null || bullet.type != lowHealthSpecialBulletType) return false;
        if (bullet.team != team) return false;

        Entityc owner = bullet.owner();
        if (owner == this) return true;
        return owner instanceof Unit && ((Unit) owner).id == id;
    }

    public Teamc findLowHealthSpecialTarget() {
        if (isEnemyTarget(primaryLink) && primaryLink instanceof Teamc) return (Teamc) primaryLink;
        return null;
    }

    public void fireLowHealthSpecialAttack() {
        if (Vars.net.client()) return;
        if (lowHealthSpecialBulletType == null) return;

        int remain = Math.max(0, LOW_HEALTH_SPECIAL_MAX_BULLETS - lowHealthSpecialBullets.size);
        int burst = Math.min(LOW_HEALTH_SPECIAL_BURST, remain);
        if (burst == 0) return;

        Teamc target = findLowHealthSpecialTarget();
        float tx = target != null ? target.x() : aimX;
        float ty = target != null ? target.y() : aimY;
        if (Mathf.within(x, y, tx, ty, 1f)) {
            tx = x + Angles.trnsx(rotation, 120f);
            ty = y + Angles.trnsy(rotation, 120f);
        }
        float baseAngle = Angles.angle(x, y, tx, ty);
        float spawnRadius = hitSize * 0.5f;
        for (int i = 0; i < burst; i++) {
            float spreadOffset = burst <= 1 ? 0f : (i - (burst - 1) * 0.5f) * LOW_HEALTH_SPECIAL_SPREAD;
            float fireAngle = baseAngle + spreadOffset;
            float forwardOffset = spawnRadius + Mathf.random(hitSize * 0.15f, hitSize * 0.5f);
            float sideBase = (i - (burst - 1) * 0.5f) * (hitSize * 0.3f);
            float sideOffset = sideBase + Mathf.range(hitSize * 0.18f);
            float bulletX = x + Angles.trnsx(fireAngle, forwardOffset) + Angles.trnsx(fireAngle + 90f, sideOffset);
            float bulletY = y + Angles.trnsy(fireAngle, forwardOffset) + Angles.trnsy(fireAngle + 90f, sideOffset);

            Bullet bullet = lowHealthSpecialBulletType.create(
                    this, this, team,
                    bulletX, bulletY, fireAngle,
                    -1f, 1f, 1f,
                    null, null,
                    tx, ty, target
            );

            if (bullet == null) continue;

            if (bullet instanceof AB ab) {
                ab.target = target;
                ab.find = false;
                float launchAngle = fireAngle + Mathf.range(Math.max(0f, lowHealthSpecialBulletType.initAngleRand));
                float speedRand = Math.max(0f, lowHealthSpecialBulletType.initSpeedRand);
                float launchSpeed = bullet.type.speed * Mathf.random(Math.max(0f, 1f - speedRand), 1f + speedRand);
                ab.initVel(launchAngle, launchSpeed);
            }

            if (!lowHealthSpecialBullets.contains(bullet, true)) {
                lowHealthSpecialBullets.add(bullet);
            }
        }
    }

    public void drawLowHealthSpecialLinks(Color baseColor, float bodyAlpha) {
        if (lowHealthSpecialBullets.isEmpty() || bodyAlpha <= 0.001f) return;

        lowHealthSpecialDraw.clear();
        for (int i = 0; i < lowHealthSpecialBullets.size; i++) {
            Bullet bullet = lowHealthSpecialBullets.get(i);
            if (isOwnedLowHealthSpecialBullet(bullet)) {
                lowHealthSpecialDraw.add(bullet);
            }
        }

        if (lowHealthSpecialDraw.isEmpty()) return;

        float z = Draw.z();
        Draw.z(Layer.effect + 0.021f);

        Color specialColor = Tmp.c3.set(baseColor).lerp(Color.white, 0.35f);
        float alpha = bodyAlpha * 0.72f;
        float stroke = (SECONDARY_STROKE + 0.22f) * (1f + Mathf.absin(9.2f, 0.2f));
        float sourceRadius = hitSize * 0.5f;

        for (int i = 0; i < lowHealthSpecialDraw.size; i++) {
            Bullet bullet = lowHealthSpecialDraw.get(i);
            float linkFade = bullet.fin() <= 0.7f ? 1f : Mathf.clamp((1f - bullet.fin()) / 0.3f);
            if (linkFade <= 0.001f) continue;
            float targetRadius = Math.max(bullet.hitSize(), 2f) * 0.5f;

            drawLinkCurvesSized(
                    x, y, sourceRadius,
                    bullet.x, bullet.y, targetRadius,
                    2,
                    ((long) id << 18) ^ bullet.id ^ 4099L,
                    stroke,
                    specialColor,
                    alpha * linkFade
            );
        }

        Draw.z(z);
    }

    public void updateLowHealthEyeAnimation() {
        boolean showEye = !dead() && healthf() < LOW_HEALTH_EYE_THRESHOLD;
        float danger = showEye ? Mathf.clamp((LOW_HEALTH_EYE_THRESHOLD - healthf()) / LOW_HEALTH_EYE_THRESHOLD) : 0f;
        float targetOpen = showEye ? Mathf.clamp(0.48f + danger * 0.36f) : 0f;
        float speed = targetOpen > lowHealthEyeOpen ? LOW_HEALTH_EYE_OPEN_IN_SPEED : LOW_HEALTH_EYE_OPEN_OUT_SPEED;
        lowHealthEyeOpen = Mathf.approachDelta(lowHealthEyeOpen, targetOpen, speed);
    }

    public void drawLowHealthEye(float cx, float cy, float width) {
        if (lowHealthEyeOpen <= 0.001f) return;

        float danger = Mathf.clamp((LOW_HEALTH_EYE_THRESHOLD - healthf()) / LOW_HEALTH_EYE_THRESHOLD);
        float open = Mathf.clamp(lowHealthEyeOpen);
        drawLowHealthEye(cx, cy, width, danger, open, team.color);
    }

    public static void drawLowHealthEye(float cx, float cy, float width, float danger, float open, Color c) {
        if (width <= 0.001f) return;

        float eyeWidth = width * (1f + danger * 0.24f);
        float lidHeight = eyeWidth * (0.15f + open * 0.22f);
        float lx = cx - eyeWidth * 0.5f;
        float rx = cx + eyeWidth * 0.5f;

        float z = Draw.z();
        Draw.z(LOW_HEALTH_EYE_LAYER);

        Color eyeColor = Tmp.c3.set(Color.white.cpy().lerp(c.cpy(), 0.3f)).lerp(c, open);
        Draw.color(eyeColor);
        Draw.alpha(0.7f + danger * 0.3f);
        Lines.stroke((0.5f + danger * 0.5f) * width / 12f);

        // Eyelids.
        Lines.curve(lx, cy, cx - eyeWidth * 0.22f, cy + lidHeight, cx + eyeWidth * 0.22f, cy + lidHeight, rx, cy, 18);
        Lines.curve(lx, cy, cx - eyeWidth * 0.22f, cy - lidHeight, cx + eyeWidth * 0.22f, cy - lidHeight, rx, cy, 18);

        // Side arrow-like tips.
        float tip = eyeWidth * 0.13f;
        Fill.tri(lx + tip * 0.2f, cy + tip * 0.24f, lx - tip, cy, lx + tip * 0.2f, cy - tip * 0.24f);
        Fill.tri(rx - tip * 0.2f, cy + tip * 0.24f, rx + tip, cy, rx - tip * 0.2f, cy - tip * 0.24f);

        // Pupil: vertical split blade.
        float pupilTop = cy + lidHeight * 0.8f;
        float pupilBottom = cy - lidHeight * 0.8f;
        float pupilHalf = eyeWidth * 0.055f;
        Fill.tri(cx - pupilHalf, cy, cx + pupilHalf, cy, cx, pupilTop);
        Fill.tri(cx - pupilHalf, cy, cx + pupilHalf, cy, cx, pupilBottom);

        // Top marker triangle, aligned with upper eyelid.
        float capBaseY = cy + lidHeight;
        float capHalf = eyeWidth * 0.11f;
        float capHeight = eyeWidth * 0.18f;
        Fill.tri(cx - capHalf, capBaseY, cx + capHalf, capBaseY, cx, capBaseY + capHeight);

        // For this symmetric cubic eyelid, midpoint y is cy - 0.75 * lidHeight.
        float stemTop = cy - lidHeight * 0.75f;
        float stemLen = eyeWidth * 0.1f;
        Lines.line(cx, stemTop, cx, stemTop - stemLen);
        Fill.circle(cx, stemTop, 1f + danger * 0.4f);
        float triHalf = eyeWidth * 0.1f;
        float triY = stemTop - stemLen;
        Fill.tri(cx - triHalf, triY + eyeWidth * 0.05f, cx + triHalf, triY + eyeWidth * 0.05f, cx, triY - eyeWidth * 0.16f);

        Draw.z(z);
        Draw.reset();
    }

    public float targetHitSize(Healthc target) {
        if (target instanceof Hitboxc) {
            return Math.max(((Hitboxc) target).hitSize(), 2f);
        }
        return Math.max(hitSize * 0.7f, 2f);
    }

    public void drawLinkRing(Healthc target, Color color, float alpha) {
        if (target == null || alpha <= 0.001f) return;

        float size = targetHitSize(target);
        long seed = Math.max(entityId(target), 0);
        float pulse = 1f + Mathf.absin(5.6f + (seed & 7L) * 0.32f, LINK_RING_PULSE);
        float radius = size * pulse;
        float stroke = LINK_RING_STROKE + size * 0.035f;

        Draw.color(color);
        Draw.alpha(alpha * 0.75f);
        Lines.stroke(stroke);
        Lines.circle(target.getX(), target.getY(), radius);

        Draw.alpha(alpha * 0.28f);
        Lines.stroke(stroke * 0.65f);
        Lines.circle(target.getX(), target.getY(), radius * 0.72f);
    }

    public void drawLinkCurvesSized(float x1, float y1, float startRadius, float x2, float y2, float endRadius, int amount, long seedBase, float stroke, Color color, float alpha) {
        float dst = Mathf.dst(x1, y1, x2, y2);
        if (dst <= 2f) return;

        float sr = Float.isNaN(startRadius) ? 0f : Math.max(startRadius, 0f);
        float er = Float.isNaN(endRadius) ? 0f : Math.max(endRadius, 0f);
        float trimStart = Math.min(sr, dst * 0.46f);
        float trimEnd = Math.min(er, dst * 0.46f);
        float angle = Angles.angle(x1, y1, x2, y2);

        Tmp.v1.trns(angle, trimStart);
        Tmp.v2.trns(angle, trimEnd);
        float sx = x1 + Tmp.v1.x;
        float sy = y1 + Tmp.v1.y;
        float ex = x2 - Tmp.v2.x;
        float ey = y2 - Tmp.v2.y;

        drawLinkCurves(sx, sy, ex, ey, amount, seedBase, stroke, color, alpha);
    }

    public static void drawLinkCurves(float x1, float y1, float x2, float y2, int amount, long seedBase, float stroke, Color color, float alpha) {
        float dst = Mathf.dst(x1, y1, x2, y2);
        if (dst <= 2f || stroke <= 0.001f || alpha <= 0.001f) return;

        for (int i = 0; i < amount; i++) {
            long strandSeed = seedBase ^ ((long) (i + 1) * 0x9E3779B97F4A7C15L);
            float strandStroke = stroke * (1f - i * 0.18f);
            float strandAlpha = alpha * (1f - i * 0.2f);
            float phaseShift = (Mathf.PI2 / amount) * i;
            drawSingleLinkCurve(x1, y1, x2, y2, strandSeed, strandStroke, color, strandAlpha, phaseShift);
        }
    }

    public static void drawSingleLinkCurve(float x1, float y1, float x2, float y2, long seedBase, float stroke, Color color, float alpha, float phaseShift) {
        float dst = Mathf.dst(x1, y1, x2, y2);
        if (dst <= 2f || stroke <= 0.001f || alpha <= 0.001f) return;

        int segments = Math.max(22, (int) (dst / 4f));
        float angle = Angles.angle(x1, y1, x2, y2);
        float time = Time.time;
        seededRand.setSeed(seedBase);

        float side = seededRand.random(1f) > 0.5f ? 1f : -1f;
        float waveCycles = seededRand.random(0.7f, 1.3f);
        float waveK = waveCycles * Mathf.PI2;
        float waveSpeed = seededRand.random(0.03f, 0.05f);
        float phase = time * waveSpeed + seededRand.random(Mathf.PI2) + phaseShift;

        float endpointAmp = Math.min(dst * 0.055f, 7f) * seededRand.random(0.75f, 1.05f);
        float w0 = Mathf.sin(phase);
        float w3 = Mathf.sin(phase - waveK);

        float p0x = x1 + Angles.trnsx(angle + 90f, w0 * endpointAmp * side);
        float p0y = y1 + Angles.trnsy(angle + 90f, w0 * endpointAmp * side);
        float p3x = x2 + Angles.trnsx(angle + 90f, w3 * endpointAmp * side);
        float p3y = y2 + Angles.trnsy(angle + 90f, w3 * endpointAmp * side);

        float localDst = Mathf.dst(p0x, p0y, p3x, p3y);
        if (localDst <= 2f) return;
        float localAngle = Angles.angle(p0x, p0y, p3x, p3y);

        float t1 = seededRand.random(0.28f, 0.38f);
        float t2 = seededRand.random(0.62f, 0.72f);
        float w1 = Mathf.sin(phase - waveK * t1);
        float w2 = Mathf.sin(phase - waveK * t2);

        float controlAmp = Math.min(localDst * seededRand.random(0.28f, 0.38f), 110f);
        float tangentAmp = localDst * seededRand.random(0.02f, 0.05f);
        float tangent1 = Mathf.cos(phase - waveK * t1) * tangentAmp;
        float tangent2 = Mathf.cos(phase - waveK * t2) * tangentAmp * 0.7f;

        float c1x = p0x + Angles.trnsx(localAngle, localDst * t1 + tangent1) + Angles.trnsx(localAngle + 90f, w1 * controlAmp * side);
        float c1y = p0y + Angles.trnsy(localAngle, localDst * t1 + tangent1) + Angles.trnsy(localAngle + 90f, w1 * controlAmp * side);
        float c2x = p0x + Angles.trnsx(localAngle, localDst * t2 + tangent2) + Angles.trnsx(localAngle + 90f, w2 * controlAmp * side);
        float c2y = p0y + Angles.trnsy(localAngle, localDst * t2 + tangent2) + Angles.trnsy(localAngle + 90f, w2 * controlAmp * side);

        Draw.color(color);
        Draw.alpha(alpha * (0.72f + Mathf.absin(11f, 0.28f)));
        Lines.stroke(stroke * (1f + Mathf.absin(10.8f, 0.32f)));
        Lines.curve(p0x, p0y, c1x, c1y, c2x, c2y, p3x, p3y, segments);

        float travel = (phase / Mathf.PI2) % 1f;
        if (travel < 0f) travel += 1f;
        float inv = 1f - travel;
        float px = inv * inv * inv * p0x + 3f * inv * inv * travel * c1x + 3f * inv * travel * travel * c2x + travel * travel * travel * p3x;
        float py = inv * inv * inv * p0y + 3f * inv * inv * travel * c1y + 3f * inv * travel * travel * c2y + travel * travel * travel * p3y;
        Draw.alpha(alpha * 0.85f);
        Fill.circle(px, py, Math.max(0.75f, stroke * 0.7f));

        Draw.alpha(alpha * 0.45f);
        Fill.circle(p0x, p0y, Math.max(0.45f, stroke * 0.42f));
        Fill.circle(p3x, p3y, Math.max(0.4f, stroke * 0.36f));
    }

    public void writeHealthTarget(Writes write, Healthc target) {
        writeEntity(write, target);
    }

    public Healthc readHealthTarget(Reads read) {
        Entityc entity = readEntity(read);
        return entity instanceof Healthc ? (Healthc) entity : null;
    }

    public void sanitizeLinkState() {
        if (!onPrimaryTargetDeadOrInvalid()) {
            pendingRemove.clear();
            for (int i = 0; i < secondaryLinks.size; i++) {
                Healthc secondary = secondaryLinks.get(i);
                if (!isSecondaryRenderTarget(secondary)) {
                    int sid = entityId(secondary);
                    if (sid >= 0) {
                        secondaryGhostX.put(sid, secondary.getX());
                        secondaryGhostY.put(sid, secondary.getY());
                        secondaryGhostRadius.put(sid, targetHitSize(secondary) * 0.5f);
                    }
                    pendingRemove.add(secondary);
                }
            }
            for (Healthc secondary : pendingRemove) {
                secondaryLinks.remove(secondary);
            }
            syncSecondarySnapshots();
        }

        updateStrokeFade(isEnemyTarget(primaryLink));
    }

    @Override
    public void write(Writes write) {
        super.write(write);

        writeHealthTarget(write, primaryLink);
        write.f(primaryLinkTimeLeft);
        write.f(primaryStrokeFade);
        write.f(retargetTimer);
        write.f(secondaryRefreshTimer);
        write.f(secondaryTransferTimer);
        write.f(attackerPruneTimer);
        write.f(phaseCooldownTimer);
        write.f(phaseTimeLeft);
        write.f(phaseVisualFade);

        int validAttackers = 0;
        // Use one timestamp snapshot so count and payload stay in sync in the same packet.
        float now = Time.time;
        for (ObjectFloatMap.Entry<Healthc> entry : attackers.entries()) {
            if (entry.key != null && entry.value > now) {
                validAttackers++;
            }
        }

        write.s(Math.min(validAttackers, 32767));
        int written = 0;
        for (ObjectFloatMap.Entry<Healthc> entry : attackers.entries()) {
            if (entry.key == null || entry.value <= now) continue;
            if (written >= 32767) break;

            writeHealthTarget(write, entry.key);
            write.f(entry.value - now);
            write.f(attackerHatred.get(entry.key, 0f));
            written++;
        }

        write.b(Math.min(secondaryLinks.size, 255));
        for (int i = 0; i < secondaryLinks.size && i < 255; i++) {
            writeHealthTarget(write, secondaryLinks.get(i));
        }
    }

    @Override
    public void read(Reads read) {
        super.read(read);

        primaryLink = readHealthTarget(read);
        primaryLinkTimeLeft = read.f();
        primaryStrokeFade = read.f();
        retargetTimer = read.f();
        secondaryRefreshTimer = read.f();
        secondaryTransferTimer = read.f();
        attackerPruneTimer = read.f();
        phaseCooldownTimer = read.f();
        phaseTimeLeft = read.f();
        phaseVisualFade = read.f();

        attackers.clear();
        attackerHatred.clear();
        int attackerSize = read.us();
        for (int i = 0; i < attackerSize; i++) {
            Healthc attacker = readHealthTarget(read);
            float remain = read.f();
            float hatred = read.f();
            if (attacker != null && remain > 0f) {
                attackers.put(attacker, Time.time + remain);
                attackerHatred.put(attacker, Math.max(hatred, 0f));
            }
        }

        secondaryLinks.clear();
        int secondarySize = read.ub();
        for (int i = 0; i < secondarySize; i++) {
            Healthc secondary = readHealthTarget(read);
            if (secondary != null) {
                secondaryLinks.add(secondary);
            }
        }

        sanitizeLinkState();
    }

    @Override
    public void writeSync(Writes write) {
        super.writeSync(write);

        writeHealthTarget(write, primaryLink);
        write.f(primaryLinkTimeLeft);
        write.f(phaseTimeLeft);
        write.f(phaseCooldownTimer);
        write.f(phaseVisualFade);

        write.b(Math.min(secondaryLinks.size, 255));
        for (int i = 0; i < secondaryLinks.size && i < 255; i++) {
            writeHealthTarget(write, secondaryLinks.get(i));
        }
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);

        Healthc syncPrimary = readHealthTarget(read);
        float syncPrimaryTimeLeft = read.f();
        float syncPhaseTimeLeft = read.f();
        float syncPhaseCooldown = read.f();
        float syncPhaseVisual = read.f();

        int secondarySize = read.ub();
        secondaryLinks.clear();
        for (int i = 0; i < secondarySize; i++) {
            Healthc secondary = readHealthTarget(read);
            if (secondary != null) {
                secondaryLinks.add(secondary);
            }
        }

        if (!isLocal()) {
            Healthc previousPrimary = primaryLink;
            boolean primaryChanged = previousPrimary != syncPrimary;
            primaryLink = syncPrimary;
            primaryLinkTimeLeft = syncPrimaryTimeLeft;
            phaseTimeLeft = syncPhaseTimeLeft;
            phaseCooldownTimer = syncPhaseCooldown;
            phaseVisualFade = syncPhaseVisual;
            if (primaryChanged) {
                if (previousPrimary != null && syncPrimary == null) {
                    primaryGhostX = previousPrimary.getX();
                    primaryGhostY = previousPrimary.getY();
                    primaryGhostRadius = targetHitSize(previousPrimary) * 0.5f;
                    clearSecondaryActiveLinks();
                } else {
                    primaryStrokeFade = 0f;
                    primaryGhostRadius = Float.NaN;
                    secondaryStrokeFade.clear();
                    secondaryGhostX.clear();
                    secondaryGhostY.clear();
                    secondaryGhostRadius.clear();
                    drawnSecondaryStroke.clear();
                }
            }
            sanitizeLinkState();
        }
    }

}
