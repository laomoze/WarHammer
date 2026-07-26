package wh.entities.world.Psy;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.scene.ui.ImageButton;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.content.StatusEffects;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
import mindustry.logic.Ranged;
import mindustry.type.StatusEffect;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import wh.content.WHStats;
import wh.content.WHStatusEffects;
import wh.graphics.WHPal;
import wh.ui.PsychicBar;
import wh.ui.PsychicImage;
import wh.ui.PsychicStatValues;

import static mindustry.Vars.player;
import static mindustry.Vars.tilesize;

public class PsychicBeaconBlock extends PsychicBlock {
    public float range = 10f;
    public float psychicUse = 0.7f;
    public float boost = 1.18f;
    public float statusDuration = 12f;
    public float warmupSpeed = 0.05f;
    public boolean preventOverlap = true;
    public StatusEffect defaultStatus = WHStatusEffects.assault;
    public Seq<BeaconRecipe> recipes = new Seq<>();

    public PsychicBeaconBlock(String name) {
        super(name);
        acceptsPsychicLinks = true;
        outputsPsychicLinks = false;
        drawArrow = false;
        configurable = true;

        config(Integer.class, (PsychicBeaconBuild build, Integer value) -> {
            if (build == null) return;
            build.recipeIndex = recipes.isEmpty() ? 0 : Mathf.clamp(value, 0, recipes.size - 1);
        });
    }

    public void addRecipe(StatusEffect status, float boost, float psychicUse, float duration) {
        BeaconRecipe recipe = new BeaconRecipe(status, boost, psychicUse, duration);
        recipes.add(recipe);
        if (recipes.size == 1) {
            defaultStatus = status;
            this.boost = boost;
            this.psychicUse = psychicUse;
            this.statusDuration = duration;
        }
    }

    protected BeaconRecipe currentRecipe(int index) {
        if (recipes.isEmpty()) return new BeaconRecipe(defaultStatus, boost, psychicUse, statusDuration);
        return recipes.get(Mathf.clamp(index, 0, recipes.size - 1));
    }

    protected void buildBeaconRecipe(Table table, BeaconRecipe recipe) {
        table.left().defaults().left();
        table.table(line -> {
            line.left().defaults().left().padBottom(2f);
            line.image(recipe.status.uiIcon != null ? recipe.status.uiIcon : PsychicImage.region()).size(22f).padRight(6f);
            line.add("[white]" + recipe.status.localizedName).style(Styles.outlineLabel).left().growX();
        }).growX().row();

        table.table(detail -> {
            detail.left().defaults().left().padRight(10f);
            detail.add("[accent]+" + Strings.autoFixed((recipe.boost - 1f) * 100f, 0) + "%").style(Styles.outlineLabel).left();
            detail.add("[lightgray]" + Strings.autoFixed(recipe.psychicUse, 2) + "/s").style(Styles.outlineLabel).left();
            detail.add("[gray]" + Strings.autoFixed(recipe.duration, 1) + "s").style(Styles.outlineLabel).left();
        }).left().padTop(2f);
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.range, range, StatUnit.blocks);
        PsychicStatValues.add(stats, WHStats.psychicStability, boost, StatUnit.none);
        PsychicStatValues.add(stats, WHStats.psychicConsumption, psychicUse, StatUnit.perSecond);
        stats.add(WHStats.psychicCoverageRange, range, StatUnit.blocks);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("psychic-use", (PsychicBeaconBuild build) -> new PsychicBar(
                () -> bundleFormat("bar.wh-psychic-use", Strings.autoFixed(build.useRate, 2)),
                () -> WHPal.PsyColor,
                () -> build.recipe().psychicUse <= 0.0001f ? 0f : build.useRate / build.recipe().psychicUse
        ));
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);
        float worldX = x * tilesize + offset;
        float worldY = y * tilesize + offset;
        Team placeTeam = player == null ? Team.derelict : player.team();
        boolean overlap = preventOverlap && overlapsAt(worldX, worldY, placeTeam);
        drawRange(worldX, worldY, !overlap);
        drawPlaceText(bundleFormat("bar.wh-psychic-beacon-range", Strings.autoFixed(range, 1)), x, y, valid && !overlap);
        if (overlap) {
            drawPlaceText(bundleFormat("bar.wh-psychic-beacon-overlap"), x, y + 1, false);
        }
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (!super.canPlaceOn(tile, team, rotation)) return false;
        if (!preventOverlap) return true;
        return !overlapsAt(tile.worldx() + offset, tile.worldy() + offset, team);
    }

    protected void drawRange(float worldX, float worldY, boolean valid) {
        float rangeWorld = range * tilesize;
        Draw.alpha(0.75f);
        Drawf.dashRect(valid ? Pal.heal : Pal.remove, worldX - rangeWorld, worldY - rangeWorld, rangeWorld * 2f, rangeWorld * 2f);
        Draw.reset();
    }

    protected boolean overlapsAt(float worldX, float worldY, Team team) {
        float rangeWorld = range * tilesize;
        final boolean[] overlap = {false};
        Groups.build.each(b -> {
            if (overlap[0] || b.team != team || !(b.block instanceof PsychicBeaconBlock block) || !b.isAdded()) return;
            float otherRange = block.range * tilesize;
            if (Math.abs(b.x - worldX) < rangeWorld + otherRange && Math.abs(b.y - worldY) < rangeWorld + otherRange) {
                overlap[0] = true;
            }
        });
        return overlap[0];
    }

    public static class BeaconRecipe {
        public final StatusEffect status;
        public float boost;
        public float psychicUse;
        public float duration;

        public BeaconRecipe(StatusEffect status, float boost, float psychicUse, float duration) {
            this.status = status == null ? StatusEffects.none : status;
            this.boost = boost;
            this.psychicUse = psychicUse;
            this.duration = duration;
        }
    }

    public class PsychicBeaconBuild extends PsychicBuild implements Ranged {
        public float warmup;
        public float useRate;
        public int recipeIndex;

        public BeaconRecipe recipe() {
            return currentRecipe(recipeIndex);
        }

        @Override
        public void buildConfiguration(Table table) {
            Table cont = new Table().top();
            cont.left().defaults().growX().padBottom(2f);

            if (recipes.isEmpty()) {
                cont.table(Styles.black3, t -> t.add("@none").color(Color.lightGray)).growX();
            } else {
                for (int i = 0; i < recipes.size; i++) {
                    BeaconRecipe recipe = recipes.get(i);
                    int index = i;

                    var button = new ImageButton();
                    button.table(info -> {
                        info.left().defaults().left().growX();
                        buildBeaconRecipe(info, recipe);
                    }).grow().left().pad(4f);

                    button.setStyle(Styles.clearNoneTogglei);
                    button.changed(() -> configure(index));
                    button.update(() -> button.setChecked(recipeIndex == index));
                    cont.add(button).growX();
                    cont.row();
                }
            }

            Table main = new Table().background(Styles.black);
            ScrollPane pane = new ScrollPane(cont, Styles.smallPane);
            pane.setScrollingDisabled(true, false);
            pane.setFadeScrollBars(false);
            pane.setOverscroll(false, false);

            if (block != null) {
                pane.setScrollYForce(block.selectScroll);
                pane.update(() -> block.selectScroll = pane.getScrollY());
            }

            main.add(pane).growX().maxHeight(62f * Math.min(recipes.size, 5)).pad(4f);
            table.top().add(main);
        }

        public int lastTileChanges = -1;
        @Override
        public void updateTile() {
            super.updateTile();

            BeaconRecipe recipe = recipe();
            boolean active = updateConsumeRecipe(enabled && hasPsychic(recipe.psychicUse / 60f * delta()));
            if (active) {
                float used = drainPsychic(recipe.psychicUse / 60f * delta());
                useRate = Mathf.lerpDelta(useRate, used * 60f / Math.max(delta(), 0.0001f), 0.16f);
                warmup = Mathf.approachDelta(warmup, 1f, warmupSpeed);

                Units.nearby(team, x, y, range * tilesize, unit -> unit.apply(recipe.status, recipe.duration));

                eachNearbyPsychicBuild(range, other -> {
                    if (other != this) {
                        other.addPsychicStability((recipe.boost - 1f) * 0.08f);
                        other.addPsychicPressure((recipe.boost - 1f) * 0.04f);
                    }
                });

            } else {
                useRate = Mathf.lerpDelta(useRate, 0f, 0.16f);
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }
        }

        @Override
        public void draw() {
            super.draw();
        }

        @Override
        public void drawSelect() {
            super.drawSelect();
            drawRange(x, y, true);
        }

        @Override
        public float warmup() {
            return warmup;
        }

        @Override
        public float progress() {
            float use = recipe().psychicUse;
            return use <= 0.0001f ? 0f : Mathf.clamp(useRate / use);
        }

        @Override
        public float range() {
            return range;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(warmup);
            write.f(useRate);
            write.i(recipeIndex);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision >= 1 ? (byte) 4 : revision);

            if (revision >= 1) {
                warmup = read.f();
                useRate = read.f();
                recipeIndex = recipes.isEmpty() ? 0 : Mathf.clamp(read.i(), 0, recipes.size - 1);
            } else {
                warmup = 0f;
                useRate = 0f;
                recipeIndex = 0;
            }
        }

        @Override
        public byte version() {
            return 6;
        }
    }
}
