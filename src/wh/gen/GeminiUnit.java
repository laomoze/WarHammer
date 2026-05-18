package wh.gen;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.struct.ObjectFloatMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.ai.types.MissileAI;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.units.UnitController;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Trail;
import mindustry.type.UnitType;
import mindustry.world.blocks.defense.Wall.WallBuild;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockGroup;
import wh.content.WHFx;
import wh.core.WHSettings;
import wh.entities.bullet.ApproachBullet;
import wh.entities.bullet.ApproachBullet.AB;
import wh.entities.world.entities.GeminiUnitType;
import wh.net.packet.GeminiSpecialBulletPacket;
import wh.util.WHUtils;

import static mindustry.io.TypeIO.readEntity;
import static mindustry.io.TypeIO.writeEntity;
import static wh.content.WHFx.rand;

public class GeminiUnit extends UnitEntity {
    public static final java.lang.reflect.Field controllerField = findControllerField();

    public static class GeminiSyncState {
        public Healthc primaryLink;
        public float primaryLinkTimeLeft;
        public float phaseTimeLeft;
        public GeminiUnit pairLinkUnit;
        public float pairLinkLastX;
        public float pairLinkLastY;
        public float pairLinkLastRadius;
        public float pairLinkLastTime;
        public final Seq<Healthc> secondaryLinks = new Seq<>();
    }

    public static class GhostTrail {
        public int id = -1;
        public float x;
        public float y;
        public float radius;
        public float fade;
        public float stroke;
        public boolean secondary;
        public long seed;
    }

    public static final float PRIMARY_RETARGET_INTERVAL = 16f * 60f;
    public static final float ATTACKER_MEMORY_DURATION = 10f * 60f;
    public static final float PRIMARY_LINK_DURATION = 12f * 60f;
    public static final float SECONDARY_RANGE = 300;
    public static final float SECONDARY_REFRESH_INTERVAL = 30f;
    public static final float SECONDARY_TRANSFER_INTERVAL = 1f;
    public static final float SECONDARY_TRANSFER_DAMAGE_MULTIPLIER = 0.75f;
    public static final float SECONDARY_MIN_SCORE = 3500f;
    public static final float SELF_TO_PRIMARY_DAMAGE = 0.75f;
    public static final float ATTACKER_PRUNE_INTERVAL = 20f;

    public static final int MAX_SECONDARY_LINKS = 3;

    public static final float MAIN_STROKE = 2.6f;
    public static final float SECONDARY_STROKE = 1.8f;
    public static final float SECONDARY_STROKE_IN_LERP = 0.08f;
    public static final float SECONDARY_STROKE_OUT_LERP = 0.05f;
    public static final float PRIMARY_GHOST_OUT_LERP = 0.05f;

    public static final float EARLY_LINK_RESET_PROGRESS = 1f / 2f;

    public static final float PHASE_INTERVAL = 20f * 60f;
    public static final float PHASE_DURATION = 5f * 60f;
    public static final float PHASE_ALPHA = 0.4f;

    public static final float LINK_RING_STROKE = 1.35f;
    public static final float LINK_RING_PULSE = 0.08f;
    public static final float LOW_HEALTH_EYE_THRESHOLD = 0.6f;
    public static final float LOW_HEALTH_EYE_OFFSET = 0.32f;

    public static final float LOW_HEALTH_SPECIAL_INTERVAL = 6f * 60f;
    public static final int LOW_HEALTH_SPECIAL_MAX_BULLETS = 4;
    public static final int LOW_HEALTH_SPECIAL_BURST = 3;
    public static final float LOW_HEALTH_SPECIAL_SPREAD = 20f;
    public static final float LOW_HEALTH_SPECIAL_SCAN_RANGE = 320;
    public static final float LOW_HEALTH_SPECIAL_SCAN_INTERVAL = 10f;

    public static final float PAIR_LINK_RANGE = 800;
    public static final float PAIR_LINK_REFRESH_INTERVAL = 60;
    public static final float PAIR_LINK_RADIUS_APPROACH = 0.02f;
    public static final float PAIR_LINK_DAMAGE_REDUCTION = 0.2f;
    public static final float PAIR_LINK_TRANSFER_FRACTION = 0.17f;
    public static final float PAIR_LINK_LAST_HOLD = 20f;

    public static final float LOW_HEALTH_DAMAGE_CAP = 5000;
    public static final float LOW_HEALTH_ARMOR_MULTIPLIER = 1.5f;
    public static final int PAIR_LINK_TRAIL_COUNT = 3;
    public static final int PRIMARY_GHOST_TRAIL_MAX = 6;
    public static final Rand seededRand = new Rand();

    public final ObjectFloatMap<Healthc> attackers = new ObjectFloatMap<>();
    public final ObjectFloatMap<Healthc> attackerHatred = new ObjectFloatMap<>();
    public final Seq<Healthc> secondaryLinks = new Seq<>();
    public final Seq<Healthc> secondaryCandidates = new Seq<>();

    public final Seq<Bullet> lowHealthSpecialBullets = new Seq<>();
    public final Seq<Bullet> lowHealthSpecialDraw = new Seq<>();
    public final Seq<GhostTrail> ghostTrails = new Seq<>();
    public final Trail[] pairLinkTrails = new Trail[PAIR_LINK_TRAIL_COUNT];
    public final Vec2[] pairLinkTrailPoints = new Vec2[PAIR_LINK_TRAIL_COUNT];
    public final float[] pairLinkTrailRadius = new float[PAIR_LINK_TRAIL_COUNT];

    public Healthc primaryLink;
    public GeminiUnit pairLinkUnit;
    public boolean pairLinking = false;
    public boolean pairLinked = false;
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
    public float primaryDurabilitySnapshot = Float.NaN;
    public float pendingSecondaryTransferDamage = 0f;
    public ApproachBullet lowHealthSpecialBulletType;
    public float lowHealthSpecialTimer = LOW_HEALTH_SPECIAL_INTERVAL;
    public float lowHealthSpecialScanTimer = 0f;
    public float lowHealthEyeOpen = 0f;
    public float pairLinkRefreshTimer = 0f;
    public float pairLinkFade = 0f;
    public float pairLinkLastX = Float.NaN;
    public float pairLinkLastY = Float.NaN;
    public float pairLinkLastRadius = Float.NaN;
    public float pairLinkLastTime = 0f;
    public long primaryGhostTrailSeed = 1L;
    public transient final GeminiSyncState syncStateScratch = new GeminiSyncState();

    public static java.lang.reflect.Field findControllerField() {
        Class<?> current = UnitEntity.class;
        while (current != null) {
            try {
                java.lang.reflect.Field field = current.getDeclaredField("controller");
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            } catch (Throwable t) {
                Log.err(t);
                return null;
            }
        }
        return null;
    }

    @Override
    public int classId() {
        return EntityRegister.getId(GeminiUnit.class);
    }

    @Override
    public void setType(UnitType type) {
        boolean typeChanged = this.type != type;
        super.setType(type);
        lowHealthSpecialBulletType = resolveLowHealthSpecialBulletType(type);
        if (!typeChanged) return;

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
        primaryDurabilitySnapshot = Float.NaN;
        pendingSecondaryTransferDamage = 0f;
        lowHealthSpecialTimer = Mathf.random(LOW_HEALTH_SPECIAL_INTERVAL * 0.35f, LOW_HEALTH_SPECIAL_INTERVAL);
        lowHealthSpecialScanTimer = Mathf.random(0f, LOW_HEALTH_SPECIAL_SCAN_INTERVAL);
        lowHealthEyeOpen = 0f;
        pairLinkUnit = null;
        pairLinking = false;
        pairLinked = false;
        pairLinkRefreshTimer = Mathf.random(0f, PAIR_LINK_REFRESH_INTERVAL);
        pairLinkFade = 0f;
        pairLinkLastX = Float.NaN;
        pairLinkLastY = Float.NaN;
        pairLinkLastRadius = Float.NaN;
        pairLinkLastTime = 0f;
        lowHealthSpecialBullets.clear();
        lowHealthSpecialDraw.clear();
        ghostTrails.clear();
        primaryGhostTrailSeed = 1L;
        for (int i = 0; i < PAIR_LINK_TRAIL_COUNT; i++) {
            if (pairLinkTrails[i] != null) pairLinkTrails[i].clear();
            pairLinkTrailPoints[i] = null;
            pairLinkTrailRadius[i] = 0f;
        }
    }

    public boolean hasPairLinkLastEndpoint() {
        return pairLinkLastTime > 0.001f
                && !Float.isNaN(pairLinkLastX)
                && !Float.isNaN(pairLinkLastY)
                && !Float.isNaN(pairLinkLastRadius)
                && pairLinkLastRadius > 0.01f;
    }

    public void capturePairLinkLastEndpoint(float tx, float ty, float radius) {
        if (Float.isNaN(tx) || Float.isNaN(ty) || Float.isNaN(radius)) return;
        pairLinkLastX = tx;
        pairLinkLastY = ty;
        pairLinkLastRadius = Math.max(radius, 2f);
        pairLinkLastTime = Vars.net.client() ? Math.min(PAIR_LINK_LAST_HOLD, 6f) : PAIR_LINK_LAST_HOLD;
    }

    public void updatePairLinkLastEndpoint() {
        if (hasValidPairLink()) {
            GeminiUnit other = pairLinkUnit;
            capturePairLinkLastEndpoint(other.x, other.y, other.hitSize * 0.52f);
            return;
        }
        if (pairLinkLastTime > 0f) {
            pairLinkLastTime = Math.max(pairLinkLastTime - Time.delta, 0f);
        }
    }

    public ApproachBullet resolveLowHealthSpecialBulletType(UnitType fromType) {
        if (fromType instanceof GeminiUnitType gType && gType.lowHealthSpecialBullet != null) {
            return gType.lowHealthSpecialBullet;
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

    public Object rawControllerObject() {
        if (controllerField == null) return null;
        try {
            return controllerField.get(this);
        } catch (Throwable t) {
            Log.err(t);
            return null;
        }
    }

    public void ensureValidController(String stage) {
        Object raw = rawControllerObject();
        if (raw == null || raw instanceof UnitController) return;

        UnitController replacement = null;
        if (type != null && type.controller != null) {
            replacement = type.controller.get(this);
        }

        if (replacement != null) {
            Log.warn("Recovered invalid GeminiUnit controller (@) at @ with @.", raw.getClass().getName(), stage, replacement.getClass().getName());
            controller(replacement);
        } else {
            Log.warn("Invalid GeminiUnit controller (@) at @, but no replacement controller was available.", raw.getClass().getName(), stage);
        }
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
    public void damage(float amount) {
        rawDamage(Damage.applyArmor(amount, effectiveArmor(1f)) / healthMultiplier / Vars.state.rules.unitHealth(team));
    }

    @Override
    public void damage(float amount, boolean withEffect) {
        float pre = hitTime;
        damage(amount);
        if (!withEffect) {
            hitTime = pre;
        }
    }

    @Override
    public void damageArmorMult(float amount, float armorMult) {
        damageArmorMult(amount, armorMult, true);
    }

    @Override
    public void damageArmorMult(float amount, float armorMult, boolean withEffect) {
        float pre = hitTime;
        rawDamage(Damage.applyArmor(amount, effectiveArmor(armorMult)) / healthMultiplier / Vars.state.rules.unitHealth(team));
        if (!withEffect) {
            hitTime = pre;
        }
    }

    @Override
    public void damagePierce(float amount) {
        damagePierce(amount, true);
    }

    @Override
    public void damagePierce(float amount, boolean withEffect) {
        float pre = hitTime;
        rawDamage(amount / healthMultiplier / Vars.state.rules.unitHealth(team));
        if (!withEffect) {
            hitTime = pre;
        }
    }

    @Override
    public void rawDamage(float amount) {
        rawDamageInternal(amount, true);
    }

    public void rawDamageInternal(float amount, boolean allowPairTransfer) {
        if (amount <= 0f) {
            super.rawDamage(amount);
            return;
        }

        amount = clampLowHealthRawDamage(amount);

        // Clients keep vanilla application; damage split is server-authoritative.
        if (Vars.net.client()) {
            super.rawDamage(amount);
            return;
        }

        if (allowPairTransfer && hasValidPairLink()) {
            float reduced = amount * (1f - PAIR_LINK_DAMAGE_REDUCTION);
            float redirected = reduced * PAIR_LINK_TRANSFER_FRACTION;
            if (redirected > 0.001f) {
                pairLinkUnit.shield(pairLinkUnit.shield() + redirected);
            }
            amount = reduced - redirected;
            if (amount <= 0.001f) {
                return;
            }
        }

        if (isPhasing() || !isEnemyTarget(primaryLink)) {
            super.rawDamage(amount);
            return;
        }

        float share = Mathf.clamp(SELF_TO_PRIMARY_DAMAGE, 0f, 1f);
        float selfPortion = 1f - share;
        if (selfPortion <= 0.0001f) {
            damageLinkedTarget(amount);
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
        }
    }


    public void damageSecondaryTargets(float amount) {
        if (amount <= 0.001f || !isEnemyTarget(primaryLink)) return;

        for (int i = 0; i < secondaryLinks.size; i++) {
            Healthc secondary = secondaryLinks.get(i);
            if (!isSecondaryTarget(secondary)) continue;

            secondary.damage(amount);
        }
    }

    @Override
    public void update() {
        ensureValidController("update");
        super.update();
        boolean authority = isAuthority();

        updateLowHealthEyeAnimation();
        updateAimCoordinates();
        updateLowHealthSpecialAttack();
        updateEffect();
        updateLowHealthBullet();

        if (authority) {
            updatePhaseState();
        } else {
            updatePhaseVisualState();
        }

        if (authority) {
            sanitizePairLinkState(false);
            updatePairLinkLastEndpoint();
        } else {
            updateClientPairLinkState();
        }
        updatePairLinkVisuals();

        if (!authority) {
            updateStrokeFade(isEnemyTarget(primaryLink));
            return;
        }

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

        updatePairLinkState();

        if (primaryLink != null && !isEnemyTarget(primaryLink)) {
            losePrimaryLink(true);
        }
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

    public void updateClientPairLinkState() {
        sanitizePairLinkState(true);
        if (pairLinkLastTime > 0f) {
            pairLinkLastTime = Math.max(pairLinkLastTime - Time.delta, 0f);
        }
    }

    public boolean isAuthority() {
        return !Vars.net.client();
    }

    public boolean hasPairLinkRole() {
        return pairLinking || pairLinked;
    }

    public boolean hasValidPairLink() {
        if (Vars.net.client() && !isLocal()) {
            return !dead() && isValid() && isValidPairLinkUnit(pairLinkUnit);
        }
        return !dead() && isValid() && pairLinkUnit != null && hasPairLinkRole() && isValidPairLinkUnit(pairLinkUnit);
    }

    public boolean hasPairLinkVisualUnit() {
        return pairLinkUnit != null && isValidPairLinkUnit(pairLinkUnit);
    }

    public boolean isValidPairLinkUnit(GeminiUnit other) {
        return !dead() && isValid() && other != null && other != this && !other.dead() && other.isValid() && other.team == team;
    }

    public boolean isValidPairCandidate(GeminiUnit other) {
        if (!isValidPairLinkUnit(other) || other.isPhasing()) return false;

        float range = PAIR_LINK_RANGE + hitSize * 0.5f + other.hitSize * 0.5f;
        return Mathf.within(x, y, other.x, other.y, range);
    }

    public void sanitizePairLinkState(boolean preservePartner) {
        boolean clientVisualOnly = Vars.net.client();
        if (pairLinkUnit == this) {
            pairLinkUnit = null;
            if (!clientVisualOnly) {
                pairLinking = false;
                pairLinked = false;
            }
            return;
        }

        if (clientVisualOnly) {
            if (pairLinkUnit != null && !isValidPairLinkUnit(pairLinkUnit)) {
                capturePairLinkLastEndpoint(pairLinkUnit.x, pairLinkUnit.y, pairLinkUnit.hitSize * 0.52f);
                pairLinkUnit = null;
            }
            return;
        }

        if (pairLinkUnit == null) {
            if (hasPairLinkRole()) {
                GeminiUnit recovered = recoverPairLinkUnit();
                if (recovered != null) {
                    pairLinkUnit = recovered;
                    return;
                }
            }
            pairLinking = false;
            pairLinked = false;
            return;
        }

        if (!isValidPairLinkUnit(pairLinkUnit)) {
            clearPairLink(preservePartner);
            return;
        }

        if (!pairLinking && !pairLinked) {
            pairLinkUnit = null;
            return;
        }

        // Save/load path: preserve pair reference this tick, defer reciprocal checks.
        if (preservePartner) {
            return;
        }

        if (pairLinking) {
            if (pairLinkUnit.pairLinkUnit != this || !pairLinkUnit.pairLinked) {
                clearPairLink(false);
            }
            return;
        }

        if (pairLinkUnit.pairLinkUnit != this || !pairLinkUnit.pairLinking) {
            clearPairLink(false);
        }
    }

    public GeminiUnit recoverPairLinkUnit() {
        if (!hasPairLinkRole()) return null;

        final GeminiUnit[] best = {null};
        final int[] bestScore = {-1};
        final float[] bestDst2 = {Float.MAX_VALUE};
        float range = PAIR_LINK_RANGE + hitSize * 1.5f;

        Units.nearby(team, x - range, y - range, range * 2f, range * 2f, unit -> {
            if (!(unit instanceof GeminiUnit other)) return;
            if (other == this) return;
            if (!isValidPairLinkUnit(other) || other.isPhasing()) return;

            // Roles are directional: linking -> linked.
            boolean roleMatch = pairLinking ? (other.pairLinked && !other.pairLinking) : (other.pairLinking && !other.pairLinked);
            if (!roleMatch) return;

            float maxRange = PAIR_LINK_RANGE + hitSize * 0.5f + other.hitSize * 0.5f;
            if (!Mathf.within(x, y, other.x, other.y, maxRange)) return;

            int score = 0;
            if (other.pairLinkUnit == this) score += 3;
            else if (other.pairLinkUnit == null) score += 1;
            else return;

            float dst2 = Mathf.dst2(x, y, other.x, other.y);
            if (score > bestScore[0] || (score == bestScore[0] && dst2 < bestDst2[0])) {
                best[0] = other;
                bestScore[0] = score;
                bestDst2[0] = dst2;
            }
        });

        GeminiUnit recovered = best[0];
        if (recovered == null) return null;

        // Repair reciprocal state so later strict sanitize passes.
        recovered.pairLinkUnit = this;
        if (pairLinking) {
            recovered.pairLinking = false;
            recovered.pairLinked = true;
        } else {
            recovered.pairLinking = true;
            recovered.pairLinked = false;
        }
        return recovered;
    }

    public void clearPairLink(boolean preservePartner) {
        GeminiUnit other = pairLinkUnit;
        if (other != null) {
            capturePairLinkLastEndpoint(other.x, other.y, other.hitSize * 0.52f);
        }
        pairLinkUnit = null;
        pairLinking = false;
        pairLinked = false;

        if (!preservePartner && other != null && other.pairLinkUnit == this) {
            other.pairLinkUnit = null;
            other.pairLinking = false;
            other.pairLinked = false;
        }
    }

    public GeminiUnit findPairLinkCandidate() {
        final GeminiUnit[] best = {null};
        final float[] bestDst2 = {Float.MAX_VALUE};
        float range = PAIR_LINK_RANGE;

        Units.nearby(team, x - range, y - range, range * 2f, range * 2f, unit -> {
            if (!(unit instanceof GeminiUnit other)) return;
            if (other == this) return;
            if (!isValidPairCandidate(other)) return;
            if (other.pairLinkUnit != null || other.pairLinking || other.pairLinked) return;
            if (pairLinked || pairLinking || pairLinkUnit != null) return;

            float dst2 = Mathf.dst2(x, y, other.x, other.y);
            if (best[0] == null || dst2 < bestDst2[0] || (Mathf.equal(dst2, bestDst2[0], 0.001f) && other.id < best[0].id)) {
                best[0] = other;
                bestDst2[0] = dst2;
            }
        });

        return best[0];
    }

    public void establishPairLink(GeminiUnit other) {
        if (other == null || !isValidPairCandidate(other)) return;

        clearPairLink(false);
        other.clearPairLink(false);

        pairLinkUnit = other;
        pairLinking = true;
        pairLinked = false;
        pairLinkFade = 0f;
        pairLinkRefreshTimer = PAIR_LINK_REFRESH_INTERVAL;

        other.pairLinkUnit = this;
        other.pairLinking = false;
        other.pairLinked = true;
        other.pairLinkFade = 0f;
        other.pairLinkRefreshTimer = PAIR_LINK_REFRESH_INTERVAL;
    }

    public void updatePairLinkState() {
        sanitizePairLinkState(false);

        pairLinkRefreshTimer -= Time.delta;
        if (pairLinkRefreshTimer > 0f) return;

        pairLinkRefreshTimer = PAIR_LINK_REFRESH_INTERVAL;
        if (pairLinked || pairLinkUnit != null) return;

        GeminiUnit candidate = findPairLinkCandidate();
        if (candidate != null) {
            establishPairLink(candidate);
        }
    }

    public void updatePairLinkVisuals() {
        boolean active = hasValidPairLink() || (Vars.net.client() && hasPairLinkLastEndpoint());
        pairLinkFade = Mathf.lerpDelta(pairLinkFade, active ? 1f : 0f, active ? 0.05f : 0.08f);
        if (Vars.headless || (!active && pairLinkFade <= 0.01f)) return;

        for (int i = 0; i < PAIR_LINK_TRAIL_COUNT; i++) {
            if (pairLinkTrails[i] == null) pairLinkTrails[i] = new Trail(15);
            if (pairLinkTrailPoints[i] == null) pairLinkTrailPoints[i] = new Vec2(x, y);

            pairLinkTrails[i].length = 30;
            seededRand.setSeed((long) id * i);
            float targetRadius = hitSize() * seededRand.random(1f, 1.5f);
            float speedScale = seededRand.random(0.9f, 2);
            float baseAngle = Mathf.randomSeed(id + i, 360f) + 360f / PAIR_LINK_TRAIL_COUNT * i;
            float radiusProgress = pairLinkTrailRadius[i];
            radiusProgress = Mathf.approachDelta(radiusProgress, active ? 1f : 0f, PAIR_LINK_RADIUS_APPROACH);
            pairLinkTrailRadius[i] = Mathf.clamp(radiusProgress);
            float radius = pairLinkTrailRadius[i] * targetRadius;
            float angle = Time.time * speedScale * Mathf.sign(seededRand.random(1) > 0.5f) + baseAngle;
            float tg = Mathf.randomSeed(id, 360);
            float tx = WHUtils.ellipseXY(x, y, radius, radius / 2.5f, tg, angle, 0);
            float ty = WHUtils.ellipseXY(x, y, radius, radius / 2.5f, tg, angle, 1);

            pairLinkTrails[i].update(tx, ty);
            pairLinkTrailPoints[i].set(tx, ty);
        }
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

        Draw.z(WHFx.EFFECT_BOTTOM);
        drawLowHealth(team.color.cpy(), lowHealthEyeOpen);

        Color mainColor = Tmp.c1.set(team.color).lerp(Color.white, Mathf.absin(4.2f, 0.34f));
        Color secondaryColor = Tmp.c2.set(mainColor).lerp(Color.white, 0.2f);
        float z = Draw.z();
        boolean hasPrimary = Vars.net.client() ? hasClientPrimaryVisual() : isEnemyTarget(primaryLink);
        boolean hasAnchor = Vars.net.client() ? (hasPrimary || hasPrimaryGhost()) : (hasPrimary || hasPrimaryGhost());
        if (hasAnchor) {
            float primaryX = hasPrimary ? primaryLink.getX() : primaryGhostX;
            float primaryY = hasPrimary ? primaryLink.getY() : primaryGhostY;
            float primaryRadius = hasPrimary ? targetHitSize(primaryLink) * 0.5f : Math.max(primaryGhostRadius, 2f);

            float primaryFadeVisual = Mathf.pow(primaryStrokeFade, 1.35f);

            Draw.z(WHFx.EFFECT_BOTTOM);
            if (!Vars.net.client() && hasPrimary) {
                drawLinkRing(primaryLink, mainColor, bodyAlpha * primaryFadeVisual);
            } else if (hasPrimaryGhost()) {
                drawLinkRing(primaryGhostX, primaryGhostY, primaryGhostRadius * 2f, 911L, mainColor, bodyAlpha * primaryFadeVisual);
            }
            for (int i = 0; i < secondaryLinks.size; i++) {
                Healthc linked = secondaryLinks.get(i);
                int linkId = entityId(linked);
                GhostTrail secondaryGhost = linkId < 0 ? null : findGhostTrail(linkId, true);
                boolean renderable = Vars.net.client() ? isSecondaryClientVisualTarget(linked) : isSecondaryRenderTarget(linked);
                if (!renderable && secondaryGhost == null) continue;
                float secondaryFade = secondaryGhost != null ? secondaryGhost.fade : ((Vars.net.client() ? renderable : isSecondaryTarget(linked)) ? 1f : 0f);
                if (secondaryFade <= 0.01f) continue;
                float secondaryFadeVisual = Mathf.pow(secondaryFade, 1.35f);
                if (renderable) {
                    drawLinkRing(linked, secondaryColor, bodyAlpha * secondaryFadeVisual * 0.84f);
                } else {
                    drawLinkRing(secondaryGhost.x, secondaryGhost.y, secondaryGhost.radius * 2f, secondaryGhost.seed, secondaryColor, bodyAlpha * secondaryFadeVisual * 0.84f);
                }
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
                GhostTrail secondaryGhost = linkId < 0 ? null : findGhostTrail(linkId, true);
                float secondaryFade = secondaryGhost != null ? secondaryGhost.fade : (isSecondaryTarget(linked) ? 1f : 0f);
                float secondaryStroke = SECONDARY_STROKE * Mathf.pow(secondaryFade, 1.55f);
                if (secondaryStroke <= 0.01f) continue;

                float tx = linked.getX();
                float ty = linked.getY();
                float tr = targetHitSize(linked) * 0.5f;
                if (secondaryGhost != null) {
                    tx = secondaryGhost.x;
                    ty = secondaryGhost.y;
                    tr = secondaryGhost.radius;
                }
                if (Vars.net.client() ? isSecondaryClientVisualTarget(linked) : isSecondaryRenderTarget(linked)) {
                    tx = linked.getX();
                    ty = linked.getY();
                    tr = targetHitSize(linked) * 0.5f;
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

        }

        drawPrimaryGhostTrails(mainColor, secondaryColor, bodyAlpha);
        drawLowHealthSpecialLinks(mainColor, bodyAlpha);
        drawPairLinkEffects(mainColor, bodyAlpha);

        Draw.z(z);
        Draw.reset();
    }

    public void drawPairLinkEffects(Color baseColor, float bodyAlpha) {
        if (Vars.headless) return;
        boolean hasCachedEndpoint = Vars.net.client() && hasPairLinkLastEndpoint();
        boolean hasResolvedPair = hasValidPairLink();
        if (pairLinkFade <= 0.01f && !hasResolvedPair && !hasCachedEndpoint) return;

        float z = Draw.z();
        Color pairColor = Tmp.c3.set(baseColor).lerp(Color.white, 0.3f);
        float trailWidth = Math.max(1.2f, hitSize * 0.035f) * Math.max(pairLinkFade, 0.25f);

        Draw.z(Layer.effect + 0.019f);
        for (int i = 0; i < PAIR_LINK_TRAIL_COUNT; i++) {
            Trail trail = pairLinkTrails[i];
            Vec2 point = pairLinkTrailPoints[i];
            if (trail == null || point == null) continue;

            trail.drawCap(pairColor, trailWidth);
            trail.draw(pairColor, trailWidth);
            Draw.color(pairColor);
            Fill.circle(point.x, point.y, trailWidth * 0.8f);
        }

        if (hasResolvedPair) {
            GeminiUnit other = pairLinkUnit;
            Draw.z(Layer.flyingUnitLow - 0.001f);
            drawLinkRing(this, pairColor, bodyAlpha * pairLinkFade * 0.3f);
            drawLinkRing(other, pairColor, bodyAlpha * pairLinkFade * 0.3f);

            Draw.z(Layer.effect + 0.03f);
            drawLinkCurvesSized(
                    x, y, hitSize * 0.52f,
                    other.x, other.y, other.hitSize * 0.52f,
                    4,
                    ((long) id << 24) ^ other.id ^ 0x51F15EEDL,
                    5 * (1 + Mathf.sin(8, 0.25f)) * pairLinkFade,
                    pairColor,
                    bodyAlpha * pairLinkFade,
                    Math.min(Mathf.dst(x, y, other.x, other.y) * 0.03f, 8f)
            );
        } else if (hasCachedEndpoint) {
            float cacheFade = Mathf.clamp(pairLinkLastTime / PAIR_LINK_LAST_HOLD);
            Draw.z(Layer.flyingUnitLow - 0.001f);
            drawLinkRing(this, pairColor, bodyAlpha * pairLinkFade * 0.3f * cacheFade);
            drawLinkRing(pairLinkLastX, pairLinkLastY, pairLinkLastRadius * 2f, ((long) id << 11) ^ 0x91B3L, pairColor, bodyAlpha * pairLinkFade * 0.3f * cacheFade);

            Draw.z(Layer.effect + 0.03f);
            drawLinkCurvesSized(
                    x, y, hitSize * 0.52f,
                    pairLinkLastX, pairLinkLastY, pairLinkLastRadius,
                    4,
                    ((long) id << 24) ^ 0x51F15EEDL,
                    5 * (1 + Mathf.sin(8, 0.25f)) * pairLinkFade * cacheFade,
                    pairColor,
                    bodyAlpha * pairLinkFade * cacheFade,
                    Math.min(Mathf.dst(x, y, pairLinkLastX, pairLinkLastY) * 0.03f, 8f)
            );
        }

        Draw.z(z);
    }

    public void drawPrimaryGhostTrails(Color mainColor, Color secondaryColor, float bodyAlpha) {
        if (Vars.net.client()) return;
        if (ghostTrails.isEmpty() || bodyAlpha <= 0.001f) return;

        float z = Draw.z();
        Draw.z(WHFx.EFFECT_BOTTOM);
        for (int i = 0; i < ghostTrails.size; i++) {
            GhostTrail ghost = ghostTrails.get(i);
            if (ghost.secondary && hasSecondaryLinkId(ghost.id)) continue;
            Color color = ghost.secondary ? secondaryColor : mainColor;
            float fadeVisual = Mathf.pow(ghost.fade, 1.35f);
            float alpha = bodyAlpha * fadeVisual;
            if (alpha <= 0.001f) continue;
            drawLinkRing(ghost.x, ghost.y, ghost.radius * 2f, ghost.seed, color, alpha);
        }

        Draw.z(Layer.effect + 0.02f);
        for (int i = 0; i < ghostTrails.size; i++) {
            GhostTrail ghost = ghostTrails.get(i);
            if (ghost.secondary && hasSecondaryLinkId(ghost.id)) continue;
            Color color = ghost.secondary ? secondaryColor : mainColor;
            int amount = ghost.secondary ? 3 : 4;
            float alphaMul = ghost.secondary ? 0.82f : 1f;
            float stroke = ghost.stroke * Mathf.pow(ghost.fade, 1.45f);
            if (stroke <= 0.01f) continue;

            drawLinkCurvesSized(
                    x, y, hitSize * 0.5f,
                    ghost.x, ghost.y, ghost.radius,
                    amount,
                    ((long) id << 32) ^ ghost.seed,
                    stroke,
                    color,
                    bodyAlpha * Mathf.pow(ghost.fade, 1.2f) * alphaMul
            );
        }

        Draw.z(z);
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
        phaseVisualFade = Mathf.lerpDelta(phaseVisualFade, phased ? 1f : 0f, phased ? 0.08f : 0.02f);
    }

    public void updatePhaseVisualState() {
        if (phaseTimeLeft > 0f) {
            phaseTimeLeft = Math.max(phaseTimeLeft - Time.delta, 0f);
        }
        boolean phased = isPhasing();
        phaseVisualFade = Mathf.lerpDelta(phaseVisualFade, phased ? 1f : 0f, phased ? 0.08f : 0.02f);
    }

    public void activatePhase() {
        phaseTimeLeft = PHASE_DURATION;
        phaseCooldownTimer = PHASE_INTERVAL;
        if (!Vars.headless) {
            WHFx.GeminiPhaseEffect.at(x, y, rotation, team.color, type);
        }
    }

    public void pruneExpiredAttackers() {
        Seq<Healthc> expired = new Seq<>();
        for (ObjectFloatMap.Entry<Healthc> entry : attackers.entries()) {
            Healthc target = entry.key;
            if (entry.value <= Time.time || !isEnemyTarget(target)) {
                expired.add(target);
            }
        }

        for (Healthc target : expired) {
            attackers.remove(target, 0f);
            attackerHatred.remove(target, 0f);
        }
    }

    public void choosePrimaryLink() {
        pruneExpiredAttackers();
        Healthc previousPrimary = primaryLink;

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

        if (previousPrimary != null && previousPrimary != best) {
            archiveCurrentPrimaryGhost();
        }
        primaryLink = best;
        primaryLinkTimeLeft = PRIMARY_LINK_DURATION;
        secondaryRefreshTimer = 0f;
        secondaryTransferTimer = 0f;
        primaryStrokeFade = 0f;
        primaryGhostX = Float.NaN;
        primaryGhostY = Float.NaN;
        primaryGhostRadius = Float.NaN;
        clearSecondaryGhostTrails();
        clearSecondaryActiveLinks();
        primaryDurabilitySnapshot = currentDurability(primaryLink);
    }

    public void expirePrimaryLink() {
        if (primaryLink != null) {
            attackers.remove(primaryLink, 0f);
            attackerHatred.remove(primaryLink, 0f);
        }
        losePrimaryLink(false);
    }

    public void refreshSecondaryLinks() {
        if (primaryLink != null && !isEnemyTarget(primaryLink)) {
            losePrimaryLink(true);
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
        }
    }

    /**
     * Accumulate primary durability loss and transfer to secondary links once per transfer tick.
     */
    public void processSecondaryTransfers() {
        if (Vars.net.client()) return;
        if (!isEnemyTarget(primaryLink)) {
            if (primaryLink != null) {
                losePrimaryLink(true);
            }
            primaryDurabilitySnapshot = Float.NaN;
            pendingSecondaryTransferDamage = 0f;
            return;
        }

        float primaryDurabilityNow = currentDurability(primaryLink);
        if (Float.isNaN(primaryDurabilitySnapshot)) {
            primaryDurabilitySnapshot = primaryDurabilityNow;
            return;
        }

        float primaryDurabilityLoss = Math.max(primaryDurabilitySnapshot - primaryDurabilityNow, 0f);
        primaryDurabilitySnapshot = primaryDurabilityNow;
        pendingSecondaryTransferDamage += primaryDurabilityLoss;

        if (pendingSecondaryTransferDamage > 0.001f) {
            float secondaryDamage = pendingSecondaryTransferDamage * SECONDARY_TRANSFER_DAMAGE_MULTIPLIER;
            pendingSecondaryTransferDamage = 0f;
            damageSecondaryTargets(secondaryDamage);
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

    public boolean hasClientPrimaryVisual() {
        if (!Vars.net.client()) return false;
        return isEnemyTarget(primaryLink);
    }

    public GhostTrail findGhostTrail(int trailId, boolean secondary) {
        for (int i = 0; i < ghostTrails.size; i++) {
            GhostTrail ghost = ghostTrails.get(i);
            if (ghost.secondary == secondary && ghost.id == trailId) {
                return ghost;
            }
        }
        return null;
    }

    public boolean hasSecondaryLinkId(int linkId) {
        for (int i = 0; i < secondaryLinks.size; i++) {
            if (entityId(secondaryLinks.get(i)) == linkId) return true;
        }
        return false;
    }

    public void clearSecondaryGhostTrails() {
        for (int i = ghostTrails.size - 1; i >= 0; i--) {
            if (ghostTrails.get(i).secondary) {
                ghostTrails.remove(i);
            }
        }
    }

    public void addGhostTrail(float gx, float gy, float radius, float fade, float stroke, boolean secondary, int trailId, long seedHint) {
        if (Float.isNaN(gx) || Float.isNaN(gy) || Float.isNaN(radius)) return;
        if (fade <= 0.01f) return;

        GhostTrail ghost = trailId >= 0 ? findGhostTrail(trailId, secondary) : null;
        if (ghost == null) {
            ghost = new GhostTrail();
            ghost.secondary = secondary;
            ghost.id = trailId;
            ghost.seed = (Math.max(seedHint, 0L) << 16) ^ (primaryGhostTrailSeed++);
            ghostTrails.add(ghost);
        }

        ghost.x = gx;
        ghost.y = gy;
        ghost.radius = Math.max(radius, 2f);
        ghost.fade = Mathf.clamp(fade);
        ghost.stroke = Math.max(stroke, 0.01f);

        if (ghostTrails.size > PRIMARY_GHOST_TRAIL_MAX) {
            ghostTrails.remove(0);
        }
    }

    public void addGhostTrail(float gx, float gy, float radius, float fade, float stroke, boolean secondary, long seedHint) {
        addGhostTrail(gx, gy, radius, fade, stroke, secondary, -1, seedHint);
    }

    public void addPrimaryGhostTrail(float gx, float gy, float radius, float fade, long seedHint) {
        addGhostTrail(gx, gy, radius, fade, MAIN_STROKE, false, seedHint);
    }

    public void addSecondaryGhostTrail(float gx, float gy, float radius, float fade, float stroke, int linkId) {
        addGhostTrail(gx, gy, radius, fade, stroke, true, linkId, linkId);
    }

    public void archiveCurrentPrimaryGhost() {
        if (!hasPrimaryGhost()) return;
        addPrimaryGhostTrail(primaryGhostX, primaryGhostY, primaryGhostRadius, primaryStrokeFade, entityId(primaryLink));
    }

    public void updateGhostTrails() {
        for (int i = ghostTrails.size - 1; i >= 0; i--) {
            GhostTrail ghost = ghostTrails.get(i);
            if (ghost.secondary) continue;
            ghost.fade = Mathf.lerpDelta(ghost.fade, 0f, PRIMARY_GHOST_OUT_LERP);
            if (ghost.fade <= 0.01f) {
                ghostTrails.remove(i);
            }
        }
    }

    public float primaryLinkProgress() {
        return Mathf.clamp(1f - primaryLinkTimeLeft / PRIMARY_LINK_DURATION);
    }

    public boolean shouldHardResetPrimaryLoss() {
        return primaryLinkProgress() < EARLY_LINK_RESET_PROGRESS;
    }

    public boolean isPrimaryDeathReset() {
        return primaryLink != null && primaryLink.dead();
    }

    public void losePrimaryLink(boolean hardResetRequested) {
        if (primaryLink == null) {
            if (hardResetRequested) {
                primaryGhostX = Float.NaN;
                primaryGhostY = Float.NaN;
                primaryGhostRadius = Float.NaN;
                primaryStrokeFade = 0f;
                ghostTrails.clear();
                clearSecondaryLinks();
            } else {
                clearSecondaryActiveLinks();
            }
            primaryLinkTimeLeft = 0f;
            return;
        }

        boolean primaryDied = isPrimaryDeathReset();
        // If primary dies early in the link cycle, hard-reset link visuals/state.
        boolean hardReset = primaryDied
                ? (hardResetRequested && shouldHardResetPrimaryLoss())
                : hardResetRequested;
        if (hardReset) {
            primaryGhostX = Float.NaN;
            primaryGhostY = Float.NaN;
            primaryGhostRadius = Float.NaN;
            primaryStrokeFade = 0f;
            ghostTrails.clear();
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
        primaryDurabilitySnapshot = Float.NaN;
        pendingSecondaryTransferDamage = 0f;
    }

    public void clearSecondaryLinks() {
        clearSecondaryActiveLinks();
        clearSecondaryGhostTrails();
    }

    /**
     * Update link stroke fade state (primary + secondary + residual ghost).
     * 1) Primary stroke fades in/out based on hasPrimary.
     * 2) Active secondary links update fade and ghost position.
     * 3) Unseen secondary entries continue to fade out.
     * 4) Fully faded caches are removed.
     * 5) Invalid/inactive secondary links are pruned from active list.
     */
    public void updateStrokeFade(boolean hasPrimary) {
        boolean clientVisual = Vars.net.client();
        // Primary stroke fade and primary ghost snapshot.
        primaryStrokeFade = Mathf.lerpDelta(primaryStrokeFade, hasPrimary ? 1f : 0f, hasPrimary ? 0.04f : 0.06f);
        if (hasPrimary && primaryLink != null) {
            primaryGhostX = primaryLink.getX();
            primaryGhostY = primaryLink.getY();
            primaryGhostRadius = targetHitSize(primaryLink) * 0.5f;
        }
        updateGhostTrails();

        // Keep active links simple: update secondary ghost by id, and prune invalid links.
        boolean hasAnchor = clientVisual ? (hasPrimary || hasPrimaryGhost()) : (hasPrimary || primaryLinkTimeLeft > 0.001f || hasPrimaryGhost());
        for (int i = secondaryLinks.size - 1; i >= 0; i--) {
            Healthc linked = secondaryLinks.get(i);
            int linkId = entityId(linked);
            boolean renderable = clientVisual
                    ? (hasAnchor && isSecondaryClientVisualTarget(linked))
                    : (hasPrimary && isSecondaryRenderTarget(linked));
            boolean active = clientVisual ? (hasPrimary && renderable) : (hasPrimary && renderable && isSecondaryTarget(linked));

            if (linkId >= 0) {
                GhostTrail ghost = findGhostTrail(linkId, true);
                float now = ghost == null ? (active ? 1f : 0f) : ghost.fade;
                float target = active ? 1f : 0f;
                float speed = active ? SECONDARY_STROKE_IN_LERP : SECONDARY_STROKE_OUT_LERP;
                float fade = Mathf.lerpDelta(now, target, speed);
                addSecondaryGhostTrail(
                        linked.getX(), linked.getY(), targetHitSize(linked) * 0.5f,
                        fade, SECONDARY_STROKE, linkId
                );
            }

            if (!renderable) {
                secondaryLinks.remove(i);
            }
        }

        // Secondary ghost trails not linked anymore continue fading out until removed.
        for (int i = ghostTrails.size - 1; i >= 0; i--) {
            GhostTrail ghost = ghostTrails.get(i);
            if (!ghost.secondary) continue;
            if (hasSecondaryLinkId(ghost.id)) continue;

            ghost.fade = Mathf.lerpDelta(ghost.fade, 0f, SECONDARY_STROKE_OUT_LERP);
            if (ghost.fade <= 0.01f) {
                ghostTrails.remove(i);
            }
        }

        if (clientVisual && !hasPrimary && hasPrimaryGhost() && primaryStrokeFade <= 0.02f) {
            primaryGhostX = Float.NaN;
            primaryGhostY = Float.NaN;
            primaryGhostRadius = Float.NaN;
            clearSecondaryGhostTrails();
            secondaryLinks.clear();
        }
    }

    public void damageLinkedTarget(float amount) {
        if (amount <= 0.001f || !isEnemyTarget(primaryLink)) return;
        primaryLink.damage(amount);
    }


    public void updateAimCoordinates() {
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
        return !(target instanceof Building building) || (building.block != null && building.block.group != BlockGroup.walls && !(building.block instanceof CoreBlock));
    }

    public boolean isSecondaryRenderTarget(Healthc target) {
        if (target == null || target == primaryLink || target.dead() || !target.isValid()) return false;
        if (!isEnemyTarget(primaryLink)) return false;
        if (!(target instanceof Teamc targetTeam) || !(primaryLink instanceof Teamc primaryTeam)) return false;
        if (primaryTeam.team() == team) return false;
        if (targetTeam.team() != primaryTeam.team()) return false;
        if (target instanceof WallBuild) return false;
        if (target instanceof Unit unit && !unit.hittable()) return false;
        if (target instanceof Building building) {
            return building.block != null
                    && building.block.group != BlockGroup.walls
                    && !(building.block instanceof CoreBlock)
                    && building.block.targetable;
        }
        return true;
    }

    public boolean isSecondaryClientVisualTarget(Healthc target) {
        if (target == null || target.dead() || !target.isValid()) return false;
        if (!(target instanceof Teamc teamTarget)) return false;
        if (teamTarget.team() == team) return false;
        if (target instanceof WallBuild) return false;
        if (target instanceof Unit unit && !unit.hittable()) return false;
        if (target instanceof Building building) {
            return building.block != null
                    && building.block.group != BlockGroup.walls
                    && !(building.block instanceof CoreBlock)
                    && building.block.targetable;
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

    public boolean isLowHealthState() {
        return healthf() < LOW_HEALTH_EYE_THRESHOLD;
    }

    public float effectiveArmor(float armorMult) {
        float value = armorOverride >= 0f ? armorOverride : armor;
        if (value > 0f && isLowHealthState()) {
            value *= LOW_HEALTH_ARMOR_MULTIPLIER;
        }
        return value * armorMult;
    }

    public float clampLowHealthRawDamage(float amount) {
        if (amount > maxHealth * 0.05f)
            return Math.min(amount, LOW_HEALTH_DAMAGE_CAP / healthMultiplier / Vars.state.rules.unitHealth(team));
        if (!isLowHealthState()) return amount;
        return Math.min(amount, LOW_HEALTH_DAMAGE_CAP / healthMultiplier / Vars.state.rules.unitHealth(team));
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

    public void updateEffect() {
        if (lowHealthEyeOpen > 0.6f && WHSettings.effectEnabled()) {
            float pulse = 1f + Mathf.absin(10, LINK_RING_PULSE);
            float radius = hitSize * 1.5f;
            Tmp.v3.setToRandomDirection().scl(radius * pulse);
            if (Mathf.chanceDelta(0.1f)) {
                WHFx.tentacleCorona(120, 65, 17, 1, 0.7f, null, null)
                        .layer(WHFx.EFFECT_BOTTOM - 0.0001f).at(
                                x + Tmp.v3.x, y + Tmp.v3.y, Tmp.v3.angle(), team.color.cpy().a(0.23f), this);
            }
            if (Mathf.chanceDelta(0.08f)) {
                new Effect(90, (e) -> {
                    rand.setSeed(e.id);
                    Draw.color(e.color);
                    Tmp.v2.trns(e.rotation, rand.random(35));
                    Fill.circle(e.x + x, e.y + y, e.fout() * 8 * rand.random(0.5f, 1.5f));
                }).layer(WHFx.EFFECT_BOTTOM - 0.0001f).at(x + Tmp.v3.x, y + Tmp.v3.y, Tmp.v3.angle(), team.color.cpy().a(0.23f), this);
            }
        }
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

    public void updateLowHealthBullet() {
        for (Bullet b : lowHealthSpecialBullets) {
            if (b instanceof AB ab) {
                {
                    if (ab.target instanceof Healthc h && h.dead() && !ab.find) {
                        ab.find = true;
                    }
                }
            }
        }
    }

    public void cleanupLowHealthSpecialBullets() {
        for (int i = lowHealthSpecialBullets.size - 1; i >= 0; i--) {
            if (!isOwnedLowHealthSpecialBullet(lowHealthSpecialBullets.get(i))) {
                lowHealthSpecialBullets.remove(i);
            }
        }

        if (Vars.net.client() && lowHealthSpecialBullets.isEmpty()) {
            collectLowHealthSpecialBullets();
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
        if (lowHealthSpecialBulletType == null) {
            lowHealthSpecialBulletType = resolveLowHealthSpecialBulletType(type);
        }
        if (lowHealthSpecialBulletType == null || bullet.type != lowHealthSpecialBulletType) return false;
        if (bullet.team != team) return false;

        Entityc owner = bullet.owner();
        if (owner == this) return true;
        if (owner instanceof Unit && ((Unit) owner).id == id) return true;
        if (!Vars.net.client() || owner != null) return false;

        float range = LOW_HEALTH_SPECIAL_SCAN_RANGE + hitSize * 0.9f;
        return Mathf.within(x, y, bullet.x, bullet.y, range);
    }

    public Teamc findLowHealthSpecialTarget() {
        if (isEnemyTarget(primaryLink) && primaryLink instanceof Teamc) return (Teamc) primaryLink;
        return null;
    }

    public Teamc resolveSyncTeamTarget(int targetId) {
        if (targetId < 0) return null;
        try {
            Entityc entity = Groups.sync.getByID(targetId);
            if (entity instanceof Teamc) {
                return (Teamc) entity;
            }
        } catch (RuntimeException ignored) {
            // Target mapping may be unavailable for a frame on client.
        }
        return null;
    }

    public boolean applyLowHealthSpecialBulletCreate(
            BulletType bulletType, Team bulletTeam,
            float bulletX, float bulletY, float fireAngle,
            float aimX, float aimY,
            int targetId, float launchAngle, float launchSpeed,
            boolean forceFind
    ) {
        if (!(bulletType instanceof ApproachBullet approach)) return false;
        Team spawnTeam = bulletTeam == null ? team : bulletTeam;

        Bullet bullet = approach.create(
                this, this, spawnTeam,
                bulletX, bulletY, fireAngle,
                -1f, 1f, 1f,
                null, null,
                aimX, aimY, null
        );
        if (!(bullet instanceof AB ab)) return false;

        Teamc resolvedTarget = resolveSyncTeamTarget(targetId);
        ab.target = resolvedTarget;
        ab.find = forceFind || resolvedTarget == null;
        ab.initVel(launchAngle, launchSpeed);

        if (!lowHealthSpecialBullets.contains(bullet, true)) {
            lowHealthSpecialBullets.add(bullet);
        }
        return true;
    }

    public void sendLowHealthSpecialBulletCreate(
            BulletType bulletType, Team bulletTeam,
            float bulletX, float bulletY, float fireAngle,
            float aimX, float aimY,
            int targetId, float launchAngle, float launchSpeed,
            boolean forceFind
    ) {
        if (!Vars.net.server() || bulletType == null) return;

        GeminiSpecialBulletPacket packet = new GeminiSpecialBulletPacket();
        packet.ownerId = id;
        packet.type = bulletType;
        packet.team = bulletTeam;
        packet.x = bulletX;
        packet.y = bulletY;
        packet.angle = fireAngle;
        packet.aimX = aimX;
        packet.aimY = aimY;
        packet.targetId = targetId;
        packet.launchAngle = launchAngle;
        packet.launchSpeed = launchSpeed;
        packet.forceFind = forceFind;
        Vars.net.send(packet, true);
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
            float spreadOffset = (i - (burst - 1) * 0.5f) * LOW_HEALTH_SPECIAL_SPREAD;
            if (burst <= 1) {
                spreadOffset = 0f;
            }
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
                ab.find = target == null;
                float launchAngle = fireAngle + Mathf.range(180f);
                float speedRand = 0.5f;
                float launchSpeed = bullet.type.speed * Mathf.random(Math.max(0f, 1f - speedRand), 1f + speedRand);
                ab.initVel(launchAngle, launchSpeed);
                int targetId = target == null ? -1 : target.id();
                sendLowHealthSpecialBulletCreate(
                        bullet.type, bullet.team,
                        bulletX, bulletY, fireAngle,
                        tx, ty,
                        targetId, launchAngle, launchSpeed,
                        ab.find
                );
            }

            if (!lowHealthSpecialBullets.contains(bullet)) {
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
        float targetOpen = showEye ? Mathf.clamp(0.72f + danger * 0.28f) : 0f;
        float speed = targetOpen > lowHealthEyeOpen ? 0.03f : 0.06f;
        lowHealthEyeOpen = Mathf.approachDelta(lowHealthEyeOpen, targetOpen, speed);
    }

    public void drawLowHealthEye(float cx, float cy, float width) {
        if (lowHealthEyeOpen <= 0.08f) return;

        float danger = Mathf.clamp((LOW_HEALTH_EYE_THRESHOLD - healthf()) / LOW_HEALTH_EYE_THRESHOLD);
        float open = Mathf.clamp(lowHealthEyeOpen);
        drawLowHealthEye(cx, cy, width, danger, open, team.color);
    }

    public static void drawLowHealthEye(float cx, float cy, float width, float danger, float open, Color c) {
        if (width <= 0.001f || open <= 0.08f) return;

        float eyeWidth = width * (1f + danger * 0.24f);
        float lidHeight = eyeWidth * (0.2f + open * 0.2f);
        float lx = cx - eyeWidth * 0.5f;
        float rx = cx + eyeWidth * 0.5f;

        float z = Draw.z();
        Draw.z(Layer.effect + 0.0001f);

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

    public void drawLowHealth(Color color, float alpha) {
        if (alpha <= 0.001f) return;

        float open = Mathf.clamp(lowHealthEyeOpen);
        float size = hitSize * 1.5f;
        float pulse = 1f + Mathf.absin(10, LINK_RING_PULSE);
        float radius = size * pulse * (0.7f + 0.3f * open);
        float stroke = LINK_RING_STROKE + size * 0.035f;

        Draw.color(color);
        Draw.alpha(alpha * 0.5f);
        Lines.stroke(stroke * 1.5f * open);
        Lines.circle(x, y, radius);

        for (int i = 0; i < 4; i++) {
            float an = i * 90 + (Time.time * 0.3f) % 360f;
            Tmp.v1.trns(an, radius);
            Drawf.tri(x + Tmp.v1.x, y + Tmp.v1.y, stroke * 1.5f, stroke * 10 * open, an);
        }

        Draw.alpha(alpha * 0.25f);
        Lines.stroke(stroke * open);
        Lines.circle(x, y, radius * 0.72f);
        Draw.reset();
    }

    public void drawLinkRing(Healthc target, Color color, float alpha) {
        if (target == null || alpha <= 0.001f) return;

        float size = targetHitSize(target);
        long seed = Math.max(entityId(target), 0);
        drawLinkRing(target.getX(), target.getY(), size, seed, color, alpha);
    }

    public void drawLinkRing(float tx, float ty, float size, long seed, Color color, float alpha) {
        if (alpha <= 0.001f) return;

        float clampedSize = Math.max(size, 2f);
        float pulse = 1f + Mathf.absin(8 + (seed & 7L) * 0.32f, LINK_RING_PULSE);
        float radius = clampedSize * pulse;
        float stroke = LINK_RING_STROKE + clampedSize * 0.035f;

        Draw.color(color);
        Draw.alpha(alpha * 0.75f);
        Lines.stroke(stroke);
        Lines.circle(tx, ty, radius);

        Draw.alpha(alpha * 0.28f);
        Lines.stroke(stroke * 0.65f);
        Lines.circle(tx, ty, radius * 0.72f);
    }

    public void drawLinkCurvesSized(float x1, float y1, float startRadius, float x2, float y2, float endRadius, int amount, long seedBase, float stroke, Color color, float alpha) {
        drawLinkCurvesSized(x1, y1, startRadius, x2, y2, endRadius, amount, seedBase, stroke, color, alpha, -1f);
    }

    public void drawLinkCurvesSized(float x1, float y1, float startRadius, float x2, float y2, float endRadius, int amount, long seedBase, float stroke, Color color, float alpha, float amplitudeWidth) {
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

        if (amplitudeWidth < 0f) {
            drawLinkCurves(sx, sy, ex, ey, amount, seedBase, stroke, color, alpha);
        } else {
            drawLinkCurves(sx, sy, ex, ey, amount, seedBase, stroke, color, alpha, amplitudeWidth);
        }
    }

    public static void drawLinkCurves(float x1, float y1, float x2, float y2, int amount, long seedBase, float stroke, Color color, float alpha) {
        drawLinkCurves(x1, y1, x2, y2, amount, seedBase, stroke, color, alpha, -1f);
    }

    public static void drawLinkCurves(float x1, float y1, float x2, float y2, int amount, long seedBase, float stroke, Color color, float alpha, float amplitudeWidth) {
        float dst = Mathf.dst(x1, y1, x2, y2);
        if (dst <= 2f || stroke <= 0.001f || alpha <= 0.001f) return;

        for (int i = 0; i < amount; i++) {
            long strandSeed = seedBase ^ ((long) (i + 1) * 0x9E3779B97F4A7C15L);
            float strandStroke = stroke * (1f - i * 0.18f);
            float strandAlpha = alpha * (1f - i * 0.2f);
            float phaseShift = (Mathf.PI2 / amount) * i;
            drawSingleLinkCurve(x1, y1, x2, y2, strandSeed, strandStroke, color, strandAlpha, phaseShift, amplitudeWidth);
        }
    }

    public static void drawSingleLinkCurve(float x1, float y1, float x2, float y2, long seedBase, float stroke, Color color, float alpha, float phaseShift) {
        drawSingleLinkCurve(x1, y1, x2, y2, seedBase, stroke, color, alpha, phaseShift, -1f);
    }

    public static void drawSingleLinkCurve(float x1, float y1, float x2, float y2, long seedBase, float stroke, Color color, float alpha, float phaseShift, float amplitudeWidth) {
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

        float endpointAmp = amplitudeWidth < 0f ? Math.min(dst * 0.055f, 7f) * seededRand.random(0.75f, 1.05f) : amplitudeWidth * seededRand.random(0.75f, 1.05f);
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
        Fill.circle(p0x, p0y, stroke * 1.5f);
        Fill.circle(p3x, p3y, stroke * 1.5f);
    }

    public void writeHealthTarget(Writes write, Healthc target) {
        writeEntity(write, target);
    }

    public Healthc readHealthTarget(Reads read) {
        Entityc entity = readEntity(read);
        return entity instanceof Healthc ? (Healthc) entity : null;
    }

    public void writePairLinkUnit(Writes write, GeminiUnit target) {
        writeEntity(write, target);
    }

    public GeminiUnit readPairLinkUnit(Reads read) {
        Entityc entity = readEntity(read);
        return entity instanceof GeminiUnit ? (GeminiUnit) entity : null;
    }

    public void writePairLinkState(Writes write, boolean includeRefreshTimer) {
        writePairLinkUnit(write, pairLinkUnit);
        write.bool(pairLinking);
        write.bool(pairLinked);
        if (includeRefreshTimer) {
            write.f(pairLinkRefreshTimer);
        }
    }

    public void readPairLinkState(Reads read, boolean includeRefreshTimer) {
        pairLinkUnit = readPairLinkUnit(read);
        pairLinking = read.bool();
        pairLinked = read.bool();
        if (includeRefreshTimer) {
            pairLinkRefreshTimer = read.f();
        }
    }

    public void writeHealthTargets(Writes write, Seq<Healthc> targets) {
        write.b(Math.min(targets.size, 255));
        for (int i = 0; i < targets.size && i < 255; i++) {
            writeHealthTarget(write, targets.get(i));
        }
    }

    public void readHealthTargets(Reads read, Seq<Healthc> targets) {
        targets.clear();
        int size = read.ub();
        for (int i = 0; i < size; i++) {
            Healthc target = readHealthTarget(read);
            if (target != null) {
                targets.add(target);
            }
        }
    }

    public void writeAttackerState(Writes write) {
        int validAttackers = 0;
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
    }

    public void readAttackerState(Reads read) {
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
    }

    public void writeFullState(Writes write) {
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
        writePairLinkState(write, true);
        writeAttackerState(write);
        writeHealthTargets(write, secondaryLinks);
    }

    public void readFullState(Reads read) {
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
        readPairLinkState(read, true);
        readAttackerState(read);
        readHealthTargets(read, secondaryLinks);
    }

    public void writeSyncState(Writes write) {
        writeHealthTarget(write, primaryLink);
        write.f(primaryLinkTimeLeft);
        write.f(phaseTimeLeft);
        writePairLinkUnit(write, pairLinkUnit);
        write.f(pairLinkLastX);
        write.f(pairLinkLastY);
        write.f(pairLinkLastRadius);
        write.f(pairLinkLastTime);
        writeHealthTargets(write, secondaryLinks);
    }

    public GeminiSyncState readSyncState(Reads read, GeminiSyncState state) {
        state.primaryLink = readHealthTarget(read);
        state.primaryLinkTimeLeft = read.f();
        state.phaseTimeLeft = read.f();
        state.pairLinkUnit = readPairLinkUnit(read);
        state.pairLinkLastX = read.f();
        state.pairLinkLastY = read.f();
        state.pairLinkLastRadius = read.f();
        state.pairLinkLastTime = read.f();
        readHealthTargets(read, state.secondaryLinks);
        return state;
    }

    public void handlePrimarySyncChange(Healthc previousPrimary, Healthc nextPrimary) {
        if (previousPrimary == nextPrimary) return;
        primaryDurabilitySnapshot = Float.NaN;
        pendingSecondaryTransferDamage = 0f;

        if (Vars.net.client()) {
            boolean previousGone = previousPrimary != null && (previousPrimary.dead() || !previousPrimary.isValid());
            if (isEnemyTarget(nextPrimary)) {
                primaryGhostX = Float.NaN;
                primaryGhostY = Float.NaN;
                primaryGhostRadius = Float.NaN;
                clearSecondaryGhostTrails();
            } else if (previousGone) {
                primaryGhostX = previousPrimary.getX();
                primaryGhostY = previousPrimary.getY();
                primaryGhostRadius = targetHitSize(previousPrimary) * 0.5f;
            } else {
                primaryGhostX = Float.NaN;
                primaryGhostY = Float.NaN;
                primaryGhostRadius = Float.NaN;
                ghostTrails.clear();
                clearSecondaryGhostTrails();
                secondaryLinks.clear();
            }
            return;
        }

        if (previousPrimary != null && nextPrimary == null) {
            primaryGhostX = previousPrimary.getX();
            primaryGhostY = previousPrimary.getY();
            primaryGhostRadius = targetHitSize(previousPrimary) * 0.5f;
            clearSecondaryActiveLinks();
        } else {
            if (previousPrimary != null) {
                archiveCurrentPrimaryGhost();
                if (previousPrimary.dead()) {
                    addPrimaryGhostTrail(
                            previousPrimary.getX(),
                            previousPrimary.getY(),
                            targetHitSize(previousPrimary) * 0.5f,
                            Math.max(primaryStrokeFade, 0.85f),
                            entityId(previousPrimary)
                    );
                }
            }
            primaryStrokeFade = 0f;
            primaryGhostX = Float.NaN;
            primaryGhostY = Float.NaN;
            primaryGhostRadius = Float.NaN;
            clearSecondaryGhostTrails();
        }
    }

    public void applySyncState(GeminiSyncState state) {
        Healthc previousPrimary = primaryLink;
        GeminiUnit previousPair = pairLinkUnit;

        primaryLink = resolveSyncedPrimary(previousPrimary, state.primaryLink);
        primaryLinkTimeLeft = state.primaryLinkTimeLeft;
        applySyncedPhaseTimeLeft(state.phaseTimeLeft);

        pairLinkUnit = resolveSyncedPair(previousPair, state.pairLinkUnit);
        if (Vars.net.client() && !isLocal()) {
            boolean hasPair = pairLinkUnit != null;
            pairLinking = hasPair;
            pairLinked = hasPair;
        }
        if (!Float.isNaN(state.pairLinkLastX)
                && !Float.isNaN(state.pairLinkLastY)
                && state.pairLinkLastRadius > 0.01f
                && state.pairLinkLastTime > 0.001f) {
            pairLinkLastX = state.pairLinkLastX;
            pairLinkLastY = state.pairLinkLastY;
            pairLinkLastRadius = state.pairLinkLastRadius;
            pairLinkLastTime = Mathf.clamp(state.pairLinkLastTime, 0f, PAIR_LINK_LAST_HOLD);
        } else if (Vars.net.client() && !isLocal() && !hasValidPairLink()) {
            pairLinkLastTime = 0f;
            pairLinkLastX = Float.NaN;
            pairLinkLastY = Float.NaN;
            pairLinkLastRadius = Float.NaN;
        }
        mergeSyncedSecondaryLinks(primaryLink, state.secondaryLinks);

        handlePrimarySyncChange(previousPrimary, primaryLink);
        if (Vars.net.client() && !isEnemyTarget(primaryLink)) {
            primaryLinkTimeLeft = 0f;
        }
        sanitizeLinkState(Vars.net.client());
    }

    @Override
    public void killed() {
        clearPairLink(false);
        super.killed();
    }

    @Override
    public void remove() {
        clearPairLink(false);
        super.remove();
    }

    public void applySyncedPhaseTimeLeft(float syncedPhaseTimeLeft) {
        float incoming = Math.max(syncedPhaseTimeLeft, 0f);
        if (!Vars.net.client() || isLocal()) {
            phaseTimeLeft = incoming;
            return;
        }

        float current = Math.max(phaseTimeLeft, 0f);
        if (incoming <= current + 0.001f) {
            phaseTimeLeft = incoming;
            return;
        }

        // Ignore backward packets that would re-extend an active phase.
        if (current <= 0.001f) {
            phaseTimeLeft = incoming;
        }
    }

    public Healthc resolveSyncedPrimary(Healthc previous, Healthc synced) {
        if (!Vars.net.client() || isLocal()) return synced;
        if (synced != null && synced.isValid() && !synced.dead()) return synced;
        if (previous != null && previous.isValid() && !previous.dead()) return previous;
        return null;
    }

    public GeminiUnit resolveSyncedPair(GeminiUnit previous, GeminiUnit synced) {
        if (!Vars.net.client() || isLocal()) return synced;
        if (isValidPairLinkUnit(synced)) return synced;
        if (isValidPairLinkUnit(previous)) return previous;
        return null;
    }

    public void mergeSyncedSecondaryLinks(Healthc nextPrimary, Seq<Healthc> synced) {
        if (!Vars.net.client() || isLocal()) {
            secondaryLinks.clear();
            secondaryLinks.addAll(synced);
            return;
        }
        if (!synced.isEmpty()) {
            secondaryLinks.clear();
            secondaryLinks.addAll(synced);
            return;
        }
        if (hasPrimaryGhost()) return;
        secondaryLinks.clear();
    }

    public void sanitizeLinkState() {
        sanitizeLinkState(false);
    }

    public void sanitizeLinkState(boolean preservePairState) {
        sanitizePairLinkState(preservePairState);
        if (Vars.net.client() && preservePairState) {
            updateStrokeFade(isEnemyTarget(primaryLink));
            return;
        }

        if (primaryLink != null && !isEnemyTarget(primaryLink)) {
            losePrimaryLink(true);
        } else {
            for (int i = secondaryLinks.size - 1; i >= 0; i--) {
                Healthc secondary = secondaryLinks.get(i);
                if (!isSecondaryRenderTarget(secondary)) {
                    int sid = entityId(secondary);
                    if (sid >= 0) {
                        GhostTrail existing = findGhostTrail(sid, true);
                        float fade = existing == null ? 0.85f : Math.max(existing.fade, 0.85f);
                        addSecondaryGhostTrail(
                                secondary.getX(), secondary.getY(), targetHitSize(secondary) * 0.5f,
                                fade, SECONDARY_STROKE, sid
                        );
                    }
                    secondaryLinks.remove(i);
                }
            }
        }

        updateStrokeFade(isEnemyTarget(primaryLink));
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        writeFullState(write);
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        ensureValidController("read");
        readFullState(read);
        sanitizeLinkState(true);
    }

    @Override
    public void writeSync(Writes write) {
        ensureValidController("writeSync");
        super.writeSync(write);
        writeSyncState(write);
    }

    @Override
    public void readSync(Reads read) {
        super.readSync(read);
        ensureValidController("readSync");
        GeminiSyncState syncState = readSyncState(read, syncStateScratch);
        if (isLocal()) return;
        applySyncState(syncState);
    }

}
