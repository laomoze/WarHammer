package wh.entities.world.Psy;

import arc.math.Mathf;
import arc.util.Strings;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.ui.PsychicBar;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.tilesize;

public class PsychicFrontlineNodeBlock extends PsychicNode {
    public float battleRange = 12f;
    public float damageGainScale = 0.018f;
    public float maxBattleGain = 1.5f;
    public float deathBaseGain = 0.8f;
    public float deathHealthScale = 0.03f;
    public float surgeThreshold = 0.35f;
    public float surgeBoost = 1.45f;

    public PsychicFrontlineNodeBlock(String name) {
        super(name);
        buildType = PsychicFrontlineNodeBuild::new;
    }

    public static void handleUnitDamaged(Unit unit, Bullet bullet) {
        if (unit == null) return;

        Groups.build.each(build -> {
            if (build instanceof PsychicFrontlineNodeBuild node) {
                node.collectBattle(unit, bullet);
            }
        });
    }

    public static void handleUnitDeath(Unit unit) {
        if (unit == null) return;

        Groups.build.each(build -> {
            if (build instanceof PsychicFrontlineNodeBuild node) {
                node.collectDeath(unit);
            }
        });
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, battleRange, StatUnit.blocks);
        PsychicStatValues.add(stats, WHStats.psychicThreshold, surgeThreshold * 100f, StatUnit.percent);
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("psychic-frontline", (PsychicFrontlineNodeBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-frontline", Strings.autoFixed(build.gainRate, 2)),
                () -> psychicColor,
                () -> Mathf.clamp(build.gainRate / Math.max(maxBattleGain * 2f, 0.0001f))
        ));
    }

    public class PsychicFrontlineNodeBuild extends PsychicNodeBuild {
        public float gainRate;
        public float gainedThisFrame;
        public float surge;

        @Override
        public void updateTile() {
            surge = psychicFraction() >= surgeThreshold ? Mathf.approachDelta(surge, 1f, 0.08f) : Mathf.approachDelta(surge, 0f, 0.05f);
            super.updateTile();

            float actual = gainedThisFrame * 60f / Math.max(delta(), 0.0001f);
            gainRate = Mathf.lerpDelta(gainRate, actual, 0.15f);
            gainedThisFrame = 0f;
        }

        @Override
        public int energyPriority() {
            return surge > 0.5f ? 5 : 2;
        }

        @Override
        public float energyTransferScale() {
            return super.energyTransferScale() * Mathf.lerp(1f, surgeBoost, surge);
        }

        public void collectBattle(Unit unit, Bullet bullet) {
            if (!canHarvest(unit)) return;

            float gain = bullet == null ? damageGainScale : Math.min(Math.max(bullet.damage, 0f) * damageGainScale, maxBattleGain);
            receiveBattlePsychic(gain);
        }

        public void collectDeath(Unit unit) {
            if (!canHarvest(unit)) return;

            float gain = deathBaseGain + Mathf.sqrt(Math.max(unit.maxHealth(), 1f)) * deathHealthScale;
            receiveBattlePsychic(gain);
        }

        protected void receiveBattlePsychic(float gain) {
            float accepted = addPsychic(gain);
            if (accepted > PsychicNetworkNode.epsilon) {
                gainedThisFrame += accepted;
                addPsychicOverload(accepted / Math.max(psychicCapacity(), 1f) * 0.04f);
            }
        }

        protected boolean canHarvest(Unit unit) {
            if (unit == null || !enabled || !isAdded()) return false;
            if (unit.team == team) return false;

            float range = battleRange * tilesize;
            return Mathf.dst2(x, y, unit.x, unit.y) <= range * range;
        }

        @Override
        protected String debugText() {
            return super.debugText() +
                    "\n" + bundleFormat("bar.wh-psychic-frontline", Strings.autoFixed(gainRate, 2)) +
                    " | " + bundleFormat("bar.wh-psychic-surge", Strings.autoFixed(surge * 100f, 0));
        }
    }
}
